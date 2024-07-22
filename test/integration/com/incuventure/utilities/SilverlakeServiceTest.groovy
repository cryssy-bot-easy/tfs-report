package com.incuventure.utilities

import org.springframework.transaction.annotation.Transactional

/**
 */
class SilverlakeServiceTest extends GroovyTestCase{

    def silverlakeService


    public void testSuccessfulRetrievalOfUrr(){
        def results = silverlakeService.getURR();
        assertNotNull(results);
        assertFalse(results.isEmpty());

        results.each(){
            println it.getClass()
            for(Map.Entry<String, Object> entry : it.entrySet()){
                println entry.getKey().getClass().toString() + ":" + entry.getKey() + ":" + entry.getValue();
                assertNotNull(entry.getValue());
                assertNotNull(entry.getKey());
            }
//            println it.getMetaClass();
//           println it.BASE_CURRENCY + " " + it.CURRENCY_CODE + " " + it.RATE_NUMBER + " " + it.CONVERSION_RATE
        }
        println results.getClass();

    }

}
