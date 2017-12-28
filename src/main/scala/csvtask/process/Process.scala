package csvtask.process

import akka.NotUsed
import akka.stream.SourceShape
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Partition, Source}
import csvtask.failure.AFailure
import csvtask.preprocess.PriceInfo

case class CalcFailure(msg: String) extends AFailure

object Process {

  def dailyPrice: Flow[PriceInfo, BigDecimal, NotUsed] = Flow[PriceInfo].map(_.close)

  def dailyReturn: Flow[PriceInfo, BigDecimal, NotUsed] = dailyPrice.scan(Return(None, None)) { (acc, next) ⇒
    val calc = acc.fut.map(PriceMath.dailyReturn(_, next))
    Return(Some(next), calc)
  }.collect {
    case Return(_, Some(calc)) ⇒ calc
  }

  def periodMean: Flow[PriceInfo, BigDecimal, NotUsed] = dailyPrice.fold(Option.empty[Mean]) {(acc, next) ⇒
    acc.map(x ⇒ Mean(x.total + next, x.num + 1)).orElse(Some(Mean(next, 1)))
  }.collect {
    case Some(mean) ⇒ PriceMath.totalMean(mean.total, mean.num)
  }

  /**
    * Allows for application of a flow that is written without invalid input handling to the source that emits validated
    * output
    */
  def applyFlow(source: Source[Either[AFailure, PriceInfo], NotUsed],
                transFlow: Flow[PriceInfo, BigDecimal, NotUsed]):
  Source[Either[AFailure, BigDecimal], NotUsed] = Source.fromGraph(
    GraphDSL.create() { implicit b: GraphDSL.Builder[NotUsed] ⇒
      import GraphDSL.Implicits._

      val gen = b.add(source)
      // Avoid `get` on `Option` and `*Projection`.
      // You could use `collect` instead to get correct type
      // val collectRight = a.add(Flow[Result[PriceInfo]].collect{ case Right(r) => r })
      // partition.out(1) ~> collectRight ~> cleanser
      // Note that with `collect` you could use `Broadcast` instead of `Partition`.
      // Alternatively you could use `PartitionWith` from `akka-stream-contrib`
      val cleanser = b.add(Flow[Either[AFailure, PriceInfo]].map(_.right.get))
      val transformer = b.add(transFlow)
      // Avoid casts.
      // map[Either[AFailure, BigDecimal]](Right(_)) is enough
      // alternative approach: map(Right(_): Either[AFailure, BigDecimal])
      val repackRight = b.add(Flow[BigDecimal].map(Right(_).asInstanceOf[Either[AFailure, BigDecimal]]))
      // Avoid casts. See `collect` above.
      val repackLeft = b.add(Flow[Either[AFailure, PriceInfo]].map(_.asInstanceOf[Either[AFailure, BigDecimal]]))

      // Consider `PartitionWith` or `Broadcast` with `collect`s instead.
      val partition = b.add(Partition[Either[AFailure, PriceInfo]](2, (x) ⇒ if (x.isLeft) 0 else 1))
      val merge = b.add(Merge[Either[AFailure, BigDecimal]](2))

      gen.out          ~> partition.in
      partition.out(0) ~> repackLeft    ~> merge.in(0)
      partition.out(1) ~> cleanser      ~> transformer ~> repackRight ~> merge.in(1)

      // Consider Flow instead of Source
      // FlowShape(partition.in, merge.out)
      SourceShape(merge.out)
    }
  )

  private case class Return(fut: Option[BigDecimal], calc: Option[BigDecimal])
  private case class Mean(total: BigDecimal, num: Long)

}
