package csvtask.preprocess

import akka.NotUsed
import akka.stream.scaladsl.Flow

object Preprocess {
  def preprocess: Flow[String, Either[PriceParseFailure, PriceInfo], NotUsed] =
    Flow[String].
      filterNot(_.contains("Date,Open,High,Low,Close,Volume")).
      map(PriceInfo.fromString)
}
