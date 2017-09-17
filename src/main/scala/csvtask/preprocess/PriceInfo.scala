package csvtask.preprocess

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import csvtask.failure.AFailure

import scala.util.Try

case class PriceInfo(date: LocalDate,
                     open: BigDecimal,
                     high: BigDecimal,
                     low: BigDecimal,
                     close: BigDecimal,
                     volume: BigDecimal)

case class PriceParseFailure(str: String, specificProblem: String) extends AFailure {
  val msg = s"$str can not be parsed as ${classOf[PriceInfo]}. $specificProblem"
}
object PriceInfo {
  val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d-MMM-uu")

  def fromString(strRepr: String): Either[PriceParseFailure, PriceInfo] = {
    def fail(specificMsg: String) = PriceParseFailure(strRepr, specificMsg)

    def parseDate(part: String) = Try(LocalDate.parse(part, dateFormat)).
      toEither.left.map(x ⇒ fail("Can not parse date" + x.getMessage))
    def parseDouble(part: String, fieldName: String) =
      Try(BigDecimal(part)).toEither.left.map(x ⇒ fail(s"'$fieldName' is not a big decimal"))

    val parts = strRepr.split(",")
    if (parts.length != 6) Left(PriceParseFailure(strRepr, "Wrong number of parts"))
    else {
      for {
        date   ← parseDate(parts(0))
        open   ← parseDouble(parts(1), "Open")
        high   ← parseDouble(parts(2), "High")
        low    ← parseDouble(parts(3), "Low")
        close  ← parseDouble(parts(4), "Close")
        volume ← parseDouble(parts(5), "Volume")
      } yield PriceInfo(date, open, high, low, close, volume)
    }
  }
}