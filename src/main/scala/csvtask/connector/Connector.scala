package csvtask.connector

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Framing, Source}
import akka.util.ByteString

class Connector(implicit actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) {
  // ActorSystem::dispatcher is implicit, so import actorSystem.dispatcher is enough
  private implicit val ec = actorSystem.dispatcher
  private val separator = "\n"
  private def pricesURL(ticker: String) =
    // In general one should use URLEncoder.encode for Url concatenation.
    // Even if you don't expect "interesting" parameters right now it's just a code smell.
    s"https://finance.google.com/finance/historical?q=NASDAQ:$ticker&output=csv"

  /**
    * Here we are getting a source emmiting line by line content of csv file with pricing data
    */
  def priceStream(ticker: String): Source[String, NotUsed] = {
    val perLineFraming = Framing.delimiter(
      ByteString(separator), maximumFrameLength = Int.MaxValue, allowTruncation = true)

    Source.fromFuture(Http().singleRequest(HttpRequest(uri = pricesURL(ticker)))).
      flatMapConcat(_.entity.dataBytes).
      via(perLineFraming).
      map(_.utf8String)
  }
}