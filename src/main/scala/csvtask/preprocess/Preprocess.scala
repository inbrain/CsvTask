package csvtask.preprocess

import akka.NotUsed
import akka.stream.scaladsl.Flow
import csvtask.failure.AFailure

case object ZeroPrice extends AFailure {
  val msg = "Zero price encountered"
}

object Preprocess {
  /**
    * Validating and transforming stream to free other streams from the burden of dealing with Either (and at the
    * same time to not throw exceptions)
    */
  def preprocess: Flow[String, Either[AFailure, PriceInfo], NotUsed] =
    Flow[String].
      filterNot(_.contains("Date,Open,High,Low,Close,Volume")).
      map(PriceInfo.fromString).
      map(_.flatMap(x ⇒ if (x.close == 0) Left(ZeroPrice) else Right(x))).
      takeWhile(_.isRight, true)
}