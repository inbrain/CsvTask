package csvtask.preprocess

import akka.NotUsed
import akka.stream.scaladsl.Flow

object Preprocess {
  def preprocess: Flow[String, Either[PriceParseFailure, PriceInfo], NotUsed] =
    Flow[String].map(PriceInfo.fromString)
}
