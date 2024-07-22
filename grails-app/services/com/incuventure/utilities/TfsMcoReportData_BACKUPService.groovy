package com.incuventure.utilities

import java.text.DecimalFormat

@Deprecated
class TfsMcoReportData_BACKUPService {
	
	static transactional='true'
    def sessionFactory
	
	@Deprecated
	/**
	 * Prepare Tfs Mco Report Parameters.
	 * Deprecated. Improper Handling of Mco
	 * 
	 * @param ext_params
	 * 
	 */
	def prepareTfsMcoFields(ext_params){
		String normalQuery
		String negoQuery
		String qryParams
		String billsPayableQuery
		String foreignOutstandingQuery
		
		//For Derby
		normalQuery="SELECT TP.CURRENCY,LC.OUTSTANDINGBALANCE,"+
			"({fn TIMESTAMPDIFF(SQL_TSI_DAY,CAST(LC.EXPIRYDATE AS DATE),CURRENT_DATE)}) AS AGE "+
			"FROM LETTEROFCREDIT AS LC LEFT JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+
			"WHERE LC.DOCUMENTTYPE=? AND LC.TENOR=? ORDER BY TP.CURRENCY"
			
		negoQuery="SELECT TP.CURRENCY,NG.OUTSTANDINGBALANCE,"+
			"({fn TIMESTAMPDIFF(SQL_TSI_DAY,CAST(NG.EXPIRYDATE AS DATE),CURRENT_DATE)}) AS AGE "+
			"FROM LETTEROFCREDIT AS LC LEFT JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+
			"LEFT JOIN LCNEGOTIATION AS NG ON LC.DOCUMENTNUMBER=NG.DOCUMENTNUMBER "+
			"WHERE LC.DOCUMENTTYPE=? AND LC.TENOR=? ORDER BY TP.CURRENCY"
		
		billsPayableQuery="SELECT TP.CURRENCY,LC.OUTSTANDINGBALANCE,"+
			"({fn TIMESTAMPDIFF(SQL_TSI_DAY,CAST(LC.EXPIRYDATE AS DATE),CURRENT_DATE)}) AS AGE "+
			"FROM LETTEROFCREDIT AS LC LEFT JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+
			"LEFT JOIN TRADESERVICE AS TS ON LC.DOCUMENTNUMBER=TS.DOCUMENTNUMBER "+
			"WHERE LC.DOCUMENTTYPE='FOREIGN' AND TS.DOCUMENTSUBTYPE1='CASH' AND TP.PRODUCTTYPE='LC' ORDER BY TP.CURRENCY"
			
		foreignOutstandingQuery="SELECT TP.CURRENCY,NG.OUTSTANDINGBALANCE,"+
			"({fn TIMESTAMPDIFF(SQL_TSI_DAY,CAST(NG.LOANMATURITYDATE AS DATE),CURRENT_DATE)}) AS AGE "+
			"FROM LETTEROFCREDIT AS LC LEFT JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+
			"LEFT JOIN LCNEGOTIATION AS NG ON LC.DOCUMENTNUMBER=NG.DOCUMENTNUMBER "+
			"WHERE LC.DOCUMENTTYPE=? AND LC.TENOR=? ORDER BY TP.CURRENCY"
		
		//For DB2
//		normalQuery="SELECT TP.CURRENCY,LC.OUTSTANDINGBALANCE,"+
//					"TIMESTAMPDIFF(16,CAST(LC.EXPIRYDATE-CURRENT_TIMESTAMP as CHAR(22))) AS AGE "+
//					"FROM LETTEROFCREDIT AS LC LEFT JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+
//					"WHERE LC.DOCUMENTTYPE=? AND LC.TENOR=? ORDER BY TP.CURRENCY"
//			
//		negoQuery="SELECT TP.CURRENCY,NG.OUTSTANDINGBALANCE,"+
//				"TIMESTAMPDIFF(16,CAST(NG.EXPIRYDATE-CURRENT_TIMESTAMP as CHAR(22))) AS AGE "+
//				"FROM LETTEROFCREDIT AS LC LEFT JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+
//				"LEFT JOIN LCNEGOTIATION AS NG ON LC.DOCUMENTNUMBER=NG.DOCUMENTNUMBER "+
//				"WHERE LC.DOCUMENTTYPE=? AND LC.TENOR=? ORDER BY TP.CURRENCY"
//				
//		billsPayableQuery="SELECT TP.CURRENCY,LC.OUTSTANDINGBALANCE,"+
//				"TIMESTAMPDIFF(16,CAST(LC.EXPIRYDATE-CURRENT_TIMESTAMP as CHAR(22))) AS AGE "+
//				"FROM LETTEROFCREDIT AS LC LEFT JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+
//				"LEFT JOIN TRADESERVICE AS TS ON LC.DOCUMENTNUMBER=TS.DOCUMENTNUMBER "+
//				"WHERE LC.DOCUMENTTYPE='FOREIGN' AND TS.DOCUMENTSUBTYPE1='CASH' AND TP.PRODUCTTYPE='LC' ORDER BY TP.CURRENCY"
//				
//		foreignOutstandingQuery="SELECT TP.CURRENCY,NG.OUTSTANDINGBALANCE,"+
//				"TIMESTAMPDIFF(16,CAST(NG.LOANMATURITYDATE-CURRENT_TIMESTAMP as CHAR(22))) AS AGE "+
//				"FROM LETTEROFCREDIT AS LC LEFT JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+
//				"LEFT JOIN LCNEGOTIATION AS NG ON LC.DOCUMENTNUMBER=NG.DOCUMENTNUMBER "+
//				"WHERE LC.DOCUMENTTYPE=? AND LC.TENOR=? ORDER BY TP.CURRENCY"
		
				
		qryParams="FOREIGN||SIGHT"
		def foreignSight=getResultFromQuery(normalQuery, qryParams)
		def foreignSightNego=getResultFromQuery(negoQuery, qryParams)
		qryParams="FOREIGN||USANCE"
		def foreignUsance=getResultFromQuery(normalQuery, qryParams)
		def foreignUsanceNego=getResultFromQuery(negoQuery, qryParams)
		def foreignOutstanding=getResultFromQuery(foreignOutstandingQuery, qryParams)
		qryParams="DOMESTIC||SIGHT"
		def domesticSight=getResultFromQuery(normalQuery, qryParams)
		def domesticSightNego=getResultFromQuery(negoQuery, qryParams)
		qryParams="DOMESTIC||USANCE"
		def domesticUsance=getResultFromQuery(normalQuery, qryParams)
		def domesticUsanceNego=getResultFromQuery(negoQuery, qryParams)
		qryParams=""
		def billsPayable=getResultFromQuery(billsPayableQuery, qryParams)
		
		if(!foreignSight.empty){
			foreignSight=convertOutstandingBalance(foreignSight,"foreign")
			ext_params=processTfsMco(foreignSight,ext_params,"foreignSight")
		}		
		if(!foreignSightNego.empty){
			foreignSightNego=convertOutstandingBalance(foreignSightNego,"foreign")
			ext_params=processTfsMco(foreignSightNego,ext_params,"foreignSightNego")
		}
		if(!foreignUsance.empty){
			foreignUsance=convertOutstandingBalance(foreignUsance,"foreign")
			ext_params=processTfsMco(foreignUsance,ext_params,"foreignUsance")
		}
		if(!foreignUsanceNego.empty){
			foreignUsanceNego=convertOutstandingBalance(foreignUsanceNego,"foreign")
			ext_params=processTfsMco(foreignUsanceNego,ext_params,"foreignUsanceNego")
		}
		if(!foreignOutstanding.empty){
			foreignOutstanding=convertOutstandingBalance(foreignOutstanding,"foreign")
			ext_params=processTfsMco(foreignOutstanding,ext_params,"foreignOutstanding")
		}
		if(!domesticSight.empty){
			domesticSight=convertOutstandingBalance(domesticSight,"domestic")
			ext_params=groupDomesticTfsMco(domesticSight,ext_params,"domesticSight")
		}
		if(!domesticSightNego.empty){
			domesticSightNego=convertOutstandingBalance(domesticSightNego,"domestic")
			ext_params=groupDomesticTfsMco(domesticSight,ext_params,"domesticSightNego")
		}
		if(!domesticUsance.empty){
			domesticUsance=convertOutstandingBalance(domesticUsance,"domestic")
			ext_params=groupDomesticTfsMco(domesticUsance,ext_params,"domesticUsance")
		}
		if(!domesticUsanceNego.empty){
			domesticUsanceNego=convertOutstandingBalance(domesticUsanceNego,"domestic")
			ext_params=groupDomesticTfsMco(domesticUsanceNego,ext_params,"domesticUsanceNego")
		}
		if(!billsPayable.empty){
			billsPayable=convertOutstandingBalance(billsPayable,"foreign")
			ext_params=processTfsMco(billsPayable,ext_params,"billsPayable")
		}
		
		return ext_params
	}
	
	/**
	 * Group domestic reports according to currency(PHP and USD are allowed for domestic contingent)
	 * 
	 * @param tfsMco
	 * @param ext_params
	 * @param tfsMcoKey
	 * @return
	 */
	def groupDomesticTfsMco(tfsMco,ext_params,tfsMcoKey){
		ext_params["domestic_counter"]=ext_params["domestic_counter"]?:1
		ArrayList php_params=new ArrayList()
		ArrayList usd_params=new ArrayList()
	
		def typeAndCode=getDomesticTypeAndCode(tfsMcoKey)
		String domesticType=typeAndCode[0]
		String domesticCode=typeAndCode[1]
		
		
		tfsMco.each{entry ->
			if(entry[0]=="PHP") php_params.push(entry)
			else if(entry[0]=="USD") usd_params.push(entry)
		}
		println "-BEGIN PROCESS DOMESTIC-:"
		println "Domestic Key: "+tfsMcoKey
		println "PHP PARAMS: "+php_params
		println "USD PARAMS: "+usd_params
		
		if(!php_params.empty){
			ext_params=processTfsMco(php_params,ext_params,tfsMcoKey,true,domesticType,domesticCode)
			ext_params["domestic_counter"]++
		}
		if(!usd_params.empty){
			ext_params=processTfsMco(usd_params,ext_params,tfsMcoKey,true,domesticType,domesticCode)
			ext_params["domestic_counter"]++
		}
		
		return ext_params
	}
	
	/**
	 * Get domestic type and code based on key
	 * 
	 * @param tfsMcoKey
	 * @return
	 */
	def getDomesticTypeAndCode(tfsMcoKey){
		def temp=[]
		
		switch(tfsMcoKey){
			case "domesticSight":
				temp[0]="Domestic Sight"
				temp[1]="P-C-1"
				break;
			case "domesticSightNego":
				temp[0]="Domestic Sight Nego"
				temp[1]="P-C-2"
				break;
			case "domesticUsance":
				temp[0]="Domestic Usance"
				temp[1]="P-C-3"
				break;
			case "domesticUsanceNego":
				temp[0]="Domestic Usance Nego"
				temp[1]="P-C-4"
				break;
				
		}
		return temp
	}
	
	
	/**
	 * Fills ext_params with parameter values
	 * 
	 * @param tfsMco
	 * @param ext_params
	 * @param tfsMcoKey
	 * @param domesticFlag=false OPTIONAL *FOR DOMESTIC CONTINGENT
	 * @param domesticType=""    OPTIONAL *FOR DOMESTIC CONTINGENT
	 * @param domesticCode=""    OPTIONAL *FOR DOMESTIC CONTINGENT
	 * @return ext_params
	 */
	def processTfsMco(tfsMco,ext_params,String tfsMcoKey,domesticFlag=false,domesticType="",domesticCode=""){
		DecimalFormat df=new DecimalFormat("#,##0.00;(#,##0.00)")
		df.setParseBigDecimal(true)
		
		println "Process "+tfsMcoKey+": "+tfsMco
		println "Domestic Flag:"+domesticFlag
		println "Domestic Type:"+domesticType
		println "Domestic Code:"+domesticCode
		
		BigDecimal sixD=new BigDecimal("0.0"),
					fourW=new BigDecimal("0.0"),
					twoM=new BigDecimal("0.0"),
					threeM=new BigDecimal("0.0"),
					fourM=new BigDecimal("0.0"),
					sixM=new BigDecimal("0.0"),
					twelveM=new BigDecimal("0.0"),
					fourY=new BigDecimal("0.0"),
					nineY=new BigDecimal("0.0"),
					nineG=new BigDecimal("0.0"),
					grandTotal=new BigDecimal("0.0"),
					temp1=new BigDecimal("0.0")
		
		Integer temp2
		
		
		//evaluate and get sum of duration
		tfsMco.each{ entry ->
			temp1=entry[1]?:0.0
			temp2=entry[2]?:0
			
			switch(getTfsMcoDuration(Math.abs(temp2))){
				case "6D":
					sixD+=temp1
					break;
				case "4W":
					fourW+=temp1
					break;
				case "2M":
					twoM+=temp1
					break;
				case "3M":
					threeM+=temp1
					break;
				case "4M":
					fourM+=temp1
					break;
				case "6M":
					sixM+=temp1
					break;
				case "12M":
					twelveM+=temp1
					break;
				case "4Y":
					fourY+=temp1
					break;
				case "9Y":
					nineY+=temp1
					break;
				case "9B":
					nineG+=temp1
					break;
			}
		}
		
		grandTotal=sixD+fourW+twoM+threeM+fourM+sixM+twelveM+fourY+nineY+nineG
		
		println "sixD:"+sixD
		println "fourW:"+fourW
		println "twoM:"+twoM
		println "threeM:"+threeM
		println "fourM:"+fourM
		println "sixM:"+sixM
		println "twelveM:"+twelveM
		println "fourY:"+fourY
		println "nineY:"+nineY
		println "nineG:"+nineG
		println "grandTotal:"+grandTotal
		
		
		//Round up to nearest half(5.5->6,5.4->5)
		sixD=sixD.setScale(2,java.math.RoundingMode.HALF_UP)
		fourW=fourW.setScale(2,java.math.RoundingMode.HALF_UP)
		twoM=twoM.setScale(2,java.math.RoundingMode.HALF_UP)
		threeM=threeM.setScale(2,java.math.RoundingMode.HALF_UP)
		fourM=fourM.setScale(2,java.math.RoundingMode.HALF_UP)
		sixM=sixM.setScale(2,java.math.RoundingMode.HALF_UP)
		twelveM=twelveM.setScale(2,java.math.RoundingMode.HALF_UP)
		fourY=fourY.setScale(2,java.math.RoundingMode.HALF_UP)
		nineY=nineY.setScale(2,java.math.RoundingMode.HALF_UP)
		nineG=nineG.setScale(2,java.math.RoundingMode.HALF_UP)
		grandTotal=grandTotal.setScale(2,java.math.RoundingMode.HALF_UP)
		
		
		//sight/usance contingents not nego are all negative
		if(!tfsMcoKey.contains("Nego")&&!tfsMcoKey.contains("LC")&&!tfsMcoKey.contains("Outstanding")&&!tfsMcoKey.contains("bills")){
			println "NEGATE: "+tfsMcoKey
			
			sixD=sixD.negate()
			fourW=fourW.negate()
			twoM=twoM.negate()
			threeM=threeM.negate()
			fourM=fourM.negate()
			sixM=sixM.negate()
			twelveM=twelveM.negate()
			fourY=fourY.negate()
			nineY=nineY.negate()
			nineG=nineG.negate()
			grandTotal=grandTotal.negate()
		}
		
		//insert parameters to ext_params
		if(domesticFlag){	
			ext_params["domesticType"+ext_params["domestic_counter"]]=domesticType
			ext_params["domesticCode"+ext_params["domestic_counter"]]=domesticCode
			ext_params["domesticCurrency"+ext_params["domestic_counter"]]=tfsMco[0][0]?:"N/A"
			ext_params["domestic6d"+ext_params["domestic_counter"]]=df.format(sixD)
			ext_params["domestic4w"+ext_params["domestic_counter"]]=df.format(fourW)
			ext_params["domestic2m"+ext_params["domestic_counter"]]=df.format(twoM)
			ext_params["domestic3m"+ext_params["domestic_counter"]]=df.format(threeM)
			ext_params["domestic4m"+ext_params["domestic_counter"]]=df.format(fourM)
			ext_params["domestic6m"+ext_params["domestic_counter"]]=df.format(sixM)
			ext_params["domestic12m"+ext_params["domestic_counter"]]=df.format(twelveM)
			ext_params["domestic4y"+ext_params["domestic_counter"]]=df.format(fourY)
			ext_params["domestic9y"+ext_params["domestic_counter"]]=df.format(nineY)
			ext_params["domestic9g"+ext_params["domestic_counter"]]=df.format(nineG)
			ext_params["domesticTotal"+ext_params["domestic_counter"]]=df.format(grandTotal)
		}else{
			ext_params[tfsMcoKey+"Currency"]=tfsMco[0][0]?:"N/A"
			ext_params[tfsMcoKey+"6d"]=df.format(sixD)
			ext_params[tfsMcoKey+"4w"]=df.format(fourW)
			ext_params[tfsMcoKey+"2m"]=df.format(twoM)
			ext_params[tfsMcoKey+"3m"]=df.format(threeM)
			ext_params[tfsMcoKey+"4m"]=df.format(fourM)
			ext_params[tfsMcoKey+"6m"]=df.format(sixM)
			ext_params[tfsMcoKey+"12m"]=df.format(twelveM)
			ext_params[tfsMcoKey+"4y"]=df.format(fourY)
			ext_params[tfsMcoKey+"9y"]=df.format(nineY)
			ext_params[tfsMcoKey+"9g"]=df.format(nineG)
			ext_params[tfsMcoKey+"Total"]=df.format(grandTotal)
		}
		
		println "ext_params: "+ext_params
		
		return ext_params
	}

		
	/**
	 * Convert each entry of tfsMco according to currency:
	 * FOREIGN=USD
	 * DOMESTIC=PHP or USD
	 *  
	 * @param tfsMco
	 * @param tfsMcoKey
	 * @return modified tfsMco
	 */
	def convertOutstandingBalance(tfsMco,String tfsMcoKey){
		
		BigDecimal temp1
		String temp0
		
		tfsMco.each{ entry ->
			if(entry[1]) temp1=new BigDecimal(entry[1])
			else temp1=new BigDecimal("0.0")
			
			temp0=entry[0]?:"N/A"
			
			if(tfsMcoKey.contains('foreign')){
				entry[1]=temp1.divide(toUsd(temp0),java.math.RoundingMode.HALF_UP)
				entry[0]="USD"
			}
			else if(tfsMcoKey.contains('domestic')){
				if(!temp0.equals("USD")){
					entry[1]=temp1.divide(toPhp(temp0),java.math.RoundingMode.HALF_UP)
					entry[0]="PHP"
				}
			}
		}
		
		return tfsMco
	}
	
	/**
	 * Classify each entry based on Age
	 * 
	 * @param tfsMcoAge
	 * @return duration key
	 */
	String getTfsMcoDuration(tfsMcoAge){
		if(tfsMcoAge<=6) return "6D";
		else if(tfsMcoAge>6&&tfsMcoAge<=30) return "4W"
		else if(tfsMcoAge>30&&tfsMcoAge<=60) return "2M"
		else if(tfsMcoAge>60&&tfsMcoAge<=90) return "3M"
		else if(tfsMcoAge>90&&tfsMcoAge<=120) return "4M"
		else if(tfsMcoAge>120&&tfsMcoAge<=180) return "6M"
		else if(tfsMcoAge>180&&tfsMcoAge<=360) return "12M"
		else if(tfsMcoAge>360&&tfsMcoAge<=1440) return "4Y"
		else if(tfsMcoAge>1440&&tfsMcoAge<=3240) return "9Y"
		else if(tfsMcoAge>3240) return "9B"
		else return "n/a"
	}
	
	/**
	 * 
	 * Perform SQL operations based on query string
	 * 
	 * @param queryString
	 * @param queryParamString
	 * @return query result(list)
	 */
	def getResultFromQuery(String queryString,String queryParamString){
		if(!queryString) return []
		println "QueryString: "+queryString
		
		List qryParams = queryParamString.tokenize('||')
		println 'qryParams:'+qryParams
		
		def session = sessionFactory.currentSession	
		def query =  session.createSQLQuery(queryString)
		
		qryParams.eachWithIndex {parm ,ctr ->
			query.setString(ctr,parm)
		}
		
		println "Query: "+query
		
		return query.list()
	}
	
	
	//TEMPORARY ACQUISITION OF CURRENCY CONVERSION
	/**
	 * Returns the value of the currency to usd
	 * @param currency
	 * @return currency value
	 */
	private BigDecimal toUsd(String currency){
		if(currency.equalsIgnoreCase("EUR")){
			return new BigDecimal("0.77")
		}
		else if(currency.equalsIgnoreCase("PHP")){
			return new BigDecimal("41.54")
		}
		else if(currency.equalsIgnoreCase("JPY")){
			return new BigDecimal("78.32")
		}
		else if(currency.equalsIgnoreCase("WON")){
			return new BigDecimal("1114.65")
		}
		else return new BigDecimal("1.0")
	}
	
	/**
	 * Returns the value of the currency to php
	 * @param currency
	 * @return currency value
	 */
	private BigDecimal toPhp(String currency){
		if(currency.equalsIgnoreCase("EUR")){
			return new BigDecimal("0.018")
		}
		else if(currency.equalsIgnoreCase("JPY")){
			return new BigDecimal("1.89")
		}
		else if(currency.equalsIgnoreCase("WON")){
			return new BigDecimal("26.83")
		}
		else return new BigDecimal("1.0")
	}
	
	
}

