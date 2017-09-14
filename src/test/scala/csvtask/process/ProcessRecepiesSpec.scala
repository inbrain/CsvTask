package csvtask.process

import akka.stream.scaladsl.Sink
import csvtask.BaseSpec
import csvtask.preprocess.Preprocess

import scala.concurrent.Await
import scala.concurrent.duration._

class ProcessRecepiesSpec extends BaseSpec{

  "Valid data" should {
    "allow daily prices to be retrieved" in {
      val data = """Date,Open,High,Low,Close,Volume
        |13-Sep-17,0,0,0,1,0
        |12-Sep-17,0,0,0,2,0
        |11-Sep-17,0,0,0,3,0
        |8-Sep-17,0,0,0,4,0
        |7-Sep-17,0,0,0,5,0""".stripMargin

      val preprocessedSource = testSource(data).via(Preprocess.preprocess)
      val dailyPriceFut = ProcessRecepies.mainSource(preprocessedSource, ProcessRecepies.dailyPrice).runWith(Sink.seq)

      val expected = List(1, 2, 3, 4, 5).map(x ⇒ Right(BigDecimal(x)))

      Await.result(dailyPriceFut, 10.seconds) mustEqual expected
    }
  }
}
