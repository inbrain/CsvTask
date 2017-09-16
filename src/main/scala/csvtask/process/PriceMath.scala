package csvtask.process

object PriceMath {
  /**
    * this method is based on pre-validation assumption, so it does throw exception on attempt of division by zero
    */
   def dailyReturn(currPrice: BigDecimal, prevPrice: BigDecimal): BigDecimal =
     (currPrice - prevPrice) / currPrice

}
