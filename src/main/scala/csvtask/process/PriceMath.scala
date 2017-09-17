package csvtask.process

private[process] object PriceMath {
  /**
    * this method is based on pre-validation assumption, so it does throw exception on attempt of division by zero
    */
   def dailyReturn(currPrice: BigDecimal, prevPrice: BigDecimal): BigDecimal =
     (currPrice - prevPrice) / prevPrice

  /**
    * this method is based on pre-validation assumption that num is not zero, so it does throw exception on attempt
    * of division by zero
    */
  def totalMean(total: BigDecimal, num: Long) : BigDecimal = total / num

}
