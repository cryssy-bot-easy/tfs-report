package com.incuventure.dto

/**
 */
class Rate {

    private RateDefinition rateDefinition;

    private BigDecimal rate;

    public Rate(String sourceCurrency, String targetCurrency, Integer rateNumber, BigDecimal rate){
        this.rateDefinition = new RateDefinition(sourceCurrency,targetCurrency,rateNumber);
        this.rate = rate;
    }

    public Rate(RateDefinition rateDefinition, BigDecimal rate){
        this.rateDefinition = rateDefinition;
        this.rate = rate;
    }

    public BigDecimal multiply(BigDecimal multiplier){
        return rate.multiply(multiplier);
    }

    BigDecimal getRate() {
        return rate
    }

    void setRate(BigDecimal rate) {
        this.rate = rate
    }

    RateDefinition getRateDefinition() {
        return rateDefinition
    }

    void setRateDefinition(RateDefinition rateDefinition) {
        this.rateDefinition = rateDefinition
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Rate rate = (Rate) o

        if (rateDefinition != rate.rateDefinition) return false

        return true
    }

    int hashCode() {
        return (rateDefinition != null ? rateDefinition.hashCode() : 0)
    }
}
