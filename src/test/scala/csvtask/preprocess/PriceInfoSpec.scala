package csvtask.preprocess

import java.time.LocalDate

import csvtask.BaseSpec

class PriceInfoSpec extends BaseSpec {

  "PriceInfo" should {
    "have proper formatter for sample dates" in {
      LocalDate.parse("13-Sep-17", PriceInfo.dateFormat) mustEqual LocalDate.of(2017, 9, 13)
      LocalDate.parse("9-Mar-17", PriceInfo.dateFormat) mustEqual LocalDate.of(2017, 3, 9)
    }

    "parse sample line from data csv properly" in {
      val sample = "15-Mar-17,847.59,848.63,840.77,847.20,1381474"
      val expected = PriceInfo(
        date = LocalDate.of(2017, 3, 15),
        open = 847.59,
        high = 848.63,
        low = 840.77,
        close = 847.20,
        volume = 1381474)

      PriceInfo.fromString(sample) must beRight(expected)
    }

    "report error when str is malformed" in {
      val sample = "wrong string"

      PriceInfo.fromString(sample) must beLeft(PriceParseFailure(sample,"Wrong number of parts"))
    }

    "report error when date is unparseable" in {
      val sample ="Not Date,847.59,848.63,840.77,847.20,1381474"

      PriceInfo.fromString(sample) must
        beLeft(PriceParseFailure(sample,"Can not parse dateText 'Not Date' could not be parsed at index 0"))
    }

    "report error when one of fields is not number" in {
      val brokenOpen = "15-Mar-17,whoops,848.63,840.77,847.20,1381474"
      val brokenHigh = "15-Mar-17,847.59,whoops,840.77,847.20,1381474"
      val brokenLow = "15-Mar-17,847.59,848.63,whoops,847.20,1381474"
      val brokenClose = "15-Mar-17,847.59,848.63,840.77,whoops,1381474"
      val brokenVolume = "15-Mar-17,847.59,848.63,840.77,847.20,whoops"

      PriceInfo.fromString(brokenOpen) must beLeft(PriceParseFailure(brokenOpen,"'Open' is not a big decimal"))
      PriceInfo.fromString(brokenHigh) must beLeft(PriceParseFailure(brokenHigh,"'High' is not a big decimal"))
      PriceInfo.fromString(brokenLow) must beLeft(PriceParseFailure(brokenLow,"'Low' is not a big decimal"))
      PriceInfo.fromString(brokenClose) must beLeft(PriceParseFailure(brokenClose,"'Close' is not a big decimal"))
      PriceInfo.fromString(brokenVolume) must beLeft(PriceParseFailure(brokenVolume,"'Volume' is not a big decimal"))
    }
  }
}
