package com.incuventure.dto

import org.apache.commons.lang.StringUtils

/**
 */
class RateDefinition {

    private Integer rateNumber;

    private Currency sourceCurrency;

    private Currency targetCurrency;

    RateDefinition(Currency sourceCurrency, Currency targetCurrency,Integer rateNumber) {
        this.rateNumber = rateNumber
        this.sourceCurrency = sourceCurrency
        this.targetCurrency = targetCurrency
    }

    RateDefinition(String sourceCurrency, String targetCurrency,Integer rateNumber) {
        this.rateNumber = rateNumber
        this.sourceCurrency = Currency.getInstance(StringUtils.trim(sourceCurrency));
        this.targetCurrency = Currency.getInstance(StringUtils.trim(targetCurrency));
    }

    Integer getRateNumber() {
        return rateNumber
    }

    void setRateNumber(Integer rateNumber) {
        this.rateNumber = rateNumber
    }

    Currency getSourceCurrency() {
        return sourceCurrency
    }

    void setSourceCurrency(Currency sourceCurrency) {
        this.sourceCurrency = sourceCurrency
    }

    Currency getTargetCurrency() {
        return targetCurrency
    }

    void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        RateDefinition that = (RateDefinition) o

        if (rateNumber != that.rateNumber) return false
        if (sourceCurrency != that.sourceCurrency) return false
        if (targetCurrency != that.targetCurrency) return false

        return true
    }

    int hashCode() {
        int result
        result = (sourceCurrency != null ? sourceCurrency.hashCode() : 0)
        result = 31 * result + (targetCurrency != null ? targetCurrency.hashCode() : 0)
        return result
    }
}
