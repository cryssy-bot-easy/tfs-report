package com.incuventure.report;

import com.incuventure.repository.RatesRepository;
import groovy.util.GroovyTestCase;

//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;

/**
 */
public class RatesRepositoryTest extends GroovyTestCase {

    private RatesRepository ratesRepository;


//    public void testSuccessfulMappingOfRates(){
//        List<Map<String,Object>> rates = new ArrayList<Map<String,Object>>();
//        Map rate = new HashMap();
//        rate.put("BASE_CURRENCY","USD");
//        rate.put("CURRENCY_CODE","PHP");
//        rate.put("CONVERSION_RATE",new BigDecimal("12.01"));
//        rate.put("RATE_NUMBER",Integer.valueOf(3));
//
//        rates.add(rate);
//
//        ratesRepository = new RatesRepository(rates);
//
//        assertTrue(ratesRepository.exists("USD","PHP",3));
//
//    }
//
//    public void successfullyConvertToTargetCurrency(){
//        List<Map<String,Object>> rates = new ArrayList<Map<String,Object>>();
//        Map rate = new HashMap();
//        rate.put("BASE_CURRENCY","USD");
//        rate.put("CURRENCY_CODE","PHP");
//        rate.put("CONVERSION_RATE",new BigDecimal("12"));
//        rate.put("RATE_NUMBER",Integer.valueOf(3));
//
//        rates.add(rate);
//
//        ratesRepository = new RatesRepository(rates);
//        assertEquals(0,new BigDecimal("36").compareTo(ratesRepository.convertUsingUrr("USD","PHP",new BigDecimal(3))));
//    }

}
