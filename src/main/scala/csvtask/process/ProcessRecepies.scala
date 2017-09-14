package csvtask.process

import akka.NotUsed
import akka.stream.SourceShape
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, MergePreferred, Partition, Source}
import csvtask.AFailure
import csvtask.preprocess.PriceInfo

case class CalcFailure(msg: String) extends AFailure

object ProcessRecepies {
  type CalcData = Either[AFailure, PriceInfo]

  def dailyPriceX: Flow[CalcData, Either[AFailure, BigDecimal], NotUsed] =
    Flow[CalcData].map {x ⇒
      x.map(_.close)
    }

  def dailyReturnX: Flow[Either[AFailure, BigDecimal], Either[AFailure, BigDecimal], NotUsed] = {
    val zero = Right((0, 0)).asInstanceOf[Either[AFailure, (BigDecimal, BigDecimal)]]
    Flow[Either[AFailure, BigDecimal]].scan(zero){(acc, next) ⇒
      for {
        accVals ← acc
        nextVal ← next
      } yield (nextVal - accVals._2,  nextVal)
    }.map(_.map(_._1))
  }

  def dailyPrice: Flow[PriceInfo, BigDecimal, NotUsed] = Flow[PriceInfo].map(_.close)

  def dailyReturn: Flow[PriceInfo, BigDecimal, NotUsed] = {
    val zero = (BigDecimal(0), BigDecimal(0))
    dailyPrice.scan(zero){(acc, next) ⇒
      (next - acc._2, next)
    }.map(_._1)
  }

  def mainSource(source: Source[Either[AFailure, PriceInfo], NotUsed],
                 transFlow: Flow[PriceInfo, BigDecimal, NotUsed]) = GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] ⇒
    import GraphDSL.Implicits._

    val generator = b.add(source.takeWhile(_.isRight, true))
    val cleanser = b.add(Flow[Either[AFailure, PriceInfo]].map(_.right.get))
    val transformer = b.add(transFlow)
    val repackRight = b.add(Flow[BigDecimal].map(Right(_).asInstanceOf[Either[AFailure, BigDecimal]]))
    val repackLeft = b.add(Flow[Either[AFailure, PriceInfo]].map(_.asInstanceOf[Either[AFailure, BigDecimal]]))

    val partition = b.add(Partition[Either[AFailure, PriceInfo]](2, (x) ⇒ if(x.isLeft) 0 else 1))
    val merge = b.add(Merge[Either[AFailure, BigDecimal]](2))

    generator.out ~> partition.in
    partition.out(0) ~> repackLeft ~> merge.in(0)
    partition.out(1) ~> cleanser ~> transformer ~> repackRight ~> merge.in(1)

    SourceShape(merge.out)
  }

}
