package csvtask.preprocess

import java.time.LocalDate

import akka.stream.scaladsl.Sink
import csvtask.BaseSpec
import csvtask.failure.AFailure
import org.specs2.specification.Scope

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

//noinspection TypeAnnotation
class PreprocessSpec extends BaseSpec{
  "Valid data preprocessing" should {
    "properly handle data without header" in new TestScope{
      val data = s"""$sep13Line
                    |$sep12Line""".stripMargin

      val seqFut = preprocess(data)

      val expected = List(
        parsedMap(sep13Line),
        parsedMap(sep12Line)
      ).map(Right(_))

      Await.result(seqFut, 10.seconds) mustEqual expected
    }

    "properly handle data with header" in new TestScope{
      val data = s"""$header
                    |$sep13Line
                    |$sep12Line""".stripMargin

      val seqFut = preprocess(data)

      val expected = List(
        parsedMap(sep13Line),
        parsedMap(sep12Line)
      ).map(Right(_))

      Await.result(seqFut, 10.seconds) mustEqual expected
    }

    "pass on real data sample" in new TestScope {
      val seqFut = preprocessFile("goog.csv")

      Await.result(seqFut, 10.seconds) must not(contain(beLeft[AFailure]))
    }

  }

  "Invalid data preprocessing" should {
    "diagnose unparseable strings" in new TestScope{
      val data = s"""$header
                    |$completeRubbish""".stripMargin

      val seqFut = preprocess(data)

      val expected = List(Left(PriceParseFailure("Not a valid string", "Wrong number of parts")))

      Await.result(seqFut, 10.seconds) mustEqual expected
    }

    "returns problem even if there is only rubbish" in new TestScope{
      val data = s"""$completeRubbish""".stripMargin

      val seqFut = preprocess(data)

      val expected = List(Left(PriceParseFailure("Not a valid string", "Wrong number of parts")))

      Await.result(seqFut, 10.seconds) mustEqual expected
    }

    "stop after first problem" in new TestScope{
      val data = s"""$header
                    |$completeRubbish
                    |$sep12Line""".stripMargin

      val seqFut = preprocess(data)

      val expected = List(Left(PriceParseFailure("Not a valid string", "Wrong number of parts")))

      Await.result(seqFut, 10.seconds) mustEqual expected
    }

    "dislike zero close price" in new TestScope {
      val data = s"""$header
                    |$zeroPriceLine""".stripMargin

      val seqFut = preprocess(data)

      val expected = List(Left(ZeroPrice))

      Await.result(seqFut, 10.seconds) mustEqual expected
    }

    "emmit everything before first problem" in new TestScope{
      val data = s"""$header
                    |$sep12Line
                    |$completeRubbish""".stripMargin

      val seqFut = preprocess(data)

      val expected = List(
        Right(parsedMap(sep12Line)),
        Left(PriceParseFailure("Not a valid string", "Wrong number of parts")))

      Await.result(seqFut, 10.seconds) mustEqual expected
    }
  }

  trait TestScope extends Scope {
    val sep13Line = "13-Sep-17,1,1,1,1,1"
    val sep12Line = "12-Sep-17,1,1,1,1,1"
    val zeroPriceLine = "12-Sep-17,1,1,1,0,1"
    val completeRubbish = "Not a valid string"
    val header = "Date,Open,High,Low,Close,Volume"

    val parsedMap = Map(
      sep13Line → PriceInfo(LocalDate.of(2017, 9, 13), 1, 1, 1, 1, 1),
      sep12Line → PriceInfo(LocalDate.of(2017, 9, 12), 1, 1, 1, 1, 1)
    )

    def preprocess(data: String): Future[immutable.Seq[Either[AFailure, PriceInfo]]] =
      testSource(data).via(Preprocess.preprocess).runWith(Sink.seq)

    def preprocessFile(path: String): Future[immutable.Seq[Either[AFailure, PriceInfo]]] =
      testSourceFromPath(path).via(Preprocess.preprocess).runWith(Sink.seq)
  }

}
