package csvtask.connector

import akka.stream.scaladsl.Sink
import csvtask.BaseSpec

import scala.concurrent.Await
import scala.concurrent.duration._

class ConnectorSpec extends BaseSpec {

  "connector" should {
    "verify header line" in {
      val conn = new Connector

      val headerFut = conn.priceStream("GOOG").runWith(Sink.head)
      Await.result(headerFut, 10.seconds) mustEqual "\uFEFFDate,Open,High,Low,Close,Volume"
    }
  }

}
