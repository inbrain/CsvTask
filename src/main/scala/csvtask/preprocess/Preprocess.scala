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
  // You have a lot of `Either[AFailure, T]` types. Consider using an alias: type Result[T] = Either[AFailure, T]
  def preprocess: Flow[String, Either[AFailure, PriceInfo], NotUsed] =
    Flow[String].
      // Are you sure you have the same fields order every time?
      // Consider using header for mapping field name to field index.
      filterNot(_.contains("Date,Open,High,Low,Close,Volume")).
      map(PriceInfo.fromString).
      map(_.flatMap(x ⇒ if (x.close == 0) Left(ZeroPrice) else Right(x))).
      takeWhile(_.isRight, true)
}