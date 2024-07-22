package com.incuventure.repository

import com.incuventure.dto.Rate
import com.incuventure.dto.RateDefinition

/**
 * Serves as a cache for the retrieved rates.
 * Used to prevent multiple queries to the database
 *
 * @author Robbie Anonuevo
 */
class RatesRepository {

    private List<Rate> availableRates;

    public RatesRepository(List<Map<String,Object>> rates){
        availableRates = new ArrayList<Rate>();
        for (Map<String,Object> row : rates){
            availableRates.add(new Rate(row.get("BASE_CURRENCY"),row.get("CURRENCY_CODE"),row.get("RATE_NUMBER"),row.get("CONVERSION_RATE")));
        }
    }

    public BigDecimal convertUsingUrr(String source, String target,BigDecimal base){
        RateDefinition rateDefinition = new RateDefinition(source,target,Integer.valueOf(3));
        for (Rate rate : availableRates){
            if(rate.equals(rateDefinition)){
                return rate.multiply(base);
            }
        }
        return null;
    }
	
//	public BigDecimal convertUsingUrr(String source, String target,String base){
//		return convertUsingUrr(source,target,new BigDecimal(base));
//	}
		

    public boolean exists(String source,String target,Integer rateNumber){
        RateDefinition rateDefinition = new RateDefinition(source,target,rateNumber);
        for (Rate rate : availableRates){
            if(rate.getRateDefinition().equals(rateDefinition)){
                return true;
            }
        }
        return false;
    }

}
