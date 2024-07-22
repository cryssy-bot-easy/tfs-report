package com.incuventure.utilities

import org.hibernate.Query
import org.hibernate.transform.AliasToEntityMapResultTransformer
import org.hibernate.SQLQuery
import org.hibernate.type.StringType
import org.hibernate.type.IntegerType

/**
 */
class SilverlakeService {

    private static final RATES_QUERY = "select " +
            "RATE.JFXDCD AS BASE_CURRENCY, " +
            "RATE.JFXDRN AS RATE_NUMBER, " +
            "RATE.JFXDCR AS CONVERSION_RATE, " +
            "RATE.JFXDBC AS CURRENCY_CODE, " +
            "RATE.JHVDT6 AS CALENDAR_DATE, " +
            "RATE.JHVDT7 AS JULIAN_DATE, " +
            "REF_DEF.JFXRDS AS RATE_DEFINITION " +
            "from " +
            "UCPARUCMN2.JHFXDT RATE " +
            "INNER JOIN UCPARUCMN2.JHFXPR REF_DEF ON " +
            "RATE.JFXDRN = REF_DEF.JFXSEQ " +
            "where RATE.JHVDT6 = 83112 and JFXDRN = 3";

    def sessionFactory_silverlake

    public List<Map<String,Object>> getURR(){
        StringType stringTypeInstance = StringType.class.newInstance();
        SQLQuery query = sessionFactory_silverlake.currentSession.createSQLQuery(RATES_QUERY);
        query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
        query.addScalar("CURRENCY_CODE",stringTypeInstance)
        .addScalar("BASE_CURRENCY",stringTypeInstance)
        .addScalar("CONVERSION_RATE",org.hibernate.type.BigDecimalType.class.newInstance())
        .addScalar("RATE_NUMBER",IntegerType.class.newInstance());
        return query.list();
    }

}
