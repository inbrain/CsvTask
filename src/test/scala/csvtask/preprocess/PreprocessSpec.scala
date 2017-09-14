package csvtask.preprocess

import java.time.LocalDate

import akka.stream.scaladsl.Sink
import csvtask.BaseSpec

import scala.concurrent.duration._
import scala.concurrent.Await

class PreprocessSpec extends BaseSpec{
  "Preprocessing" should {
    "properly handle data without header" in {
      val data = """13-Sep-17,1,2,3,4,5
                    |12-Sep-17,6,7,8,9,10""".stripMargin

      val seqFut = testSource(data).via(Preprocess.preprocess).runWith(Sink.seq)

      val expected = List(
        PriceInfo(LocalDate.of(2017, 9, 13), 1, 2, 3, 4, 5),
        PriceInfo(LocalDate.of(2017, 9, 12), 6, 7, 8, 9, 10)
      ).map(Right(_))

      Await.result(seqFut, 10.seconds) mustEqual expected
    }

    "properly handle data with header" in {
      val data = """Date,Open,High,Low,Close,Volume
                    |13-Sep-17,1,2,3,4,5
                    |12-Sep-17,6,7,8,9,10""".stripMargin

      val seqFut = testSource(data).via(Preprocess.preprocess).runWith(Sink.seq)

      val expected = List(
        PriceInfo(LocalDate.of(2017, 9, 13), 1, 2, 3, 4, 5),
        PriceInfo(LocalDate.of(2017, 9, 12), 6, 7, 8, 9, 10)
      ).map(Right(_))

      Await.result(seqFut, 10.seconds) mustEqual expected
    }
  }
}
