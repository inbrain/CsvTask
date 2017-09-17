package csvtask.process

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink}
import csvtask.BaseSpec
import csvtask.preprocess.{Preprocess, PriceInfo}
import org.specs2.specification.Scope

import scala.concurrent.Await
import scala.concurrent.duration._

//noinspection TypeAnnotation
class ProcessSpec extends BaseSpec{

  "Valid data" should {
    "allow daily prices to be retrieved" in new TestScope {
      val data = """Date,Open,High,Low,Close,Volume
        |11-Sep-17,0,0,0,1,0
        |8-Sep-17,0,0,0,2,0
        |7-Sep-17,0,0,0,3,0""".stripMargin

      val dailyPriceFut = testRun(data, Process.dailyPrice)

      val expected = List(1, 2, 3).map(x ⇒ Right(BigDecimal(x)))

      Await.result(dailyPriceFut, 10.seconds) mustEqual expected
    }

    "allow daily return to be calculated" in new TestScope {
      val data = """Date,Open,High,Low,Close,Volume
                   |11-Sep-17,0,0,0,2,0
                   |8-Sep-17,0,0,0,4,0
                   |7-Sep-17,0,0,0,1,0""".stripMargin

      val dailyReturnFut = testRun(data, Process.dailyReturn)

      val expected = List("-0.5", "3").map(x ⇒ Right(BigDecimal(x)))

      Await.result(dailyReturnFut, 10.seconds) mustEqual expected
    }

    "allow total mean" in new TestScope {
      val data = """Date,Open,High,Low,Close,Volume
                   |11-Sep-17,0,0,0,1,0
                   |8-Sep-17,0,0,0,2,0
                   |7-Sep-17,0,0,0,3,0""".stripMargin

      val dailyReturnFut = testRun(data, Process.periodMean)

      val expected = List(Right(BigDecimal(2)))

      Await.result(dailyReturnFut, 10.seconds) mustEqual expected
    }
  }

  "Invalid data" should {
    "stop on first failure" in new TestScope {
      val data = """Date,Open,High,Low,Close,Volume
                   |11-Sep-17,0,0,0,1,0
                   |8-Sep-17,0,0,0,2,0
                   |7-Sep-17,0,0,0,3,0""".stripMargin

      ok
    }
  }

  trait TestScope extends Scope {
    def testRun(data: String, flow: Flow[PriceInfo, BigDecimal, NotUsed]) = {
      val preprocessedSource = testSource(data).via(Preprocess.preprocess)
      Process.applyFlow(preprocessedSource, flow).runWith(Sink.seq)
    }
  }

}
