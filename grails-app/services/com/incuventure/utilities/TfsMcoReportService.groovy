package com.incuventure.utilities

import java.math.RoundingMode;
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import org.hibernate.Query
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.hibernate.Session
import ucpb.tfs.reports.mco.McoReportLoggerService

class TfsMcoReportService {

	def sessionFactory
	def webAPIService
	def mcoReportLoggerService
	private static List<Map<String,Object>> ratesList = new ArrayList<Map<String,Object>>()
	private DecimalFormat decimalFormat=new DecimalFormat("#,##0.00;(#,##0.00)")
	private SimpleDateFormat dateConverterFormatter = new SimpleDateFormat("yyyy-MM-dd")
	private SimpleDateFormat normalDateFormatter = new SimpleDateFormat("MM/dd/yyyy")
	private int rowNumbers = 1
	
	/**
	 * Load processed values to ext_params['foreignSight']
	 * 
	 * @param ext_params
	 * @return ext_params attached with ['foreignSight']
	 */
	def processForeignSight(ext_params){
		String reportDate = dateConverterFormatter.format(getEndOfMonthDate(ext_params['onlineReportDate'],ext_params['specialMco']));
		String query="SELECT LC.EXPIRYDATE,LC.ISSUEDATE, "+
			"LC.OUTSTANDINGBALANCE,TP.CURRENCY,LC.DOCUMENTNUMBER "+
			"FROM LETTEROFCREDIT AS LC "+
			"INNER JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+ 
			"WHERE LC.DOCUMENTTYPE = 'FOREIGN' AND LC.TENOR = 'SIGHT' AND LC.OUTSTANDINGBALANCE > 0 " +
			"AND DATE(LC.EXPIRYDATE) >= '" + reportDate  + "' " +
			"AND LC.TYPE IN ('REGULAR') AND TP.STATUS NOT IN ('EXPIRED','CANCELLED') " +
			"AND DATE(LC.ISSUEDATE) <= '" + reportDate  + "' " +
			"ORDER BY TP.CURRENCY"
		if(ext_params['specialMco'].toString().equalsIgnoreCase('special')){
			query = "SELECT MC.DOCUMENTNUMBER,MC.EXPIRYDATE,MC.REPORTDATE AS ISSUEDATE,MC.CURRENCY,MC.OUTSTANDINGBALANCE,MC.DOCUMENTTYPE "+
					"FROM MCO_MONTH_END_DATA AS MC "+
					"WHERE MC.TRANSACTIONTYPE='FOREIGN_SIGHT' AND DATE(MC.REPORTDATE) = '"+ reportDate +  "'" 
		}else{
			def responseMap = webAPIService.dummySendCommand(
				[query:query,type:"FOREIGN_SIGHT",reportDate:reportDate], "processMonthEnd", "mco/")
			if(responseMap.success != null && responseMap.success == false){
				throw new RuntimeException("Failed saving to TFS DB")
			}
		}
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>()
		Map<String,Object> resultMap =new HashMap<String,Object>()
		Map<String,Object> mirrorMap =new HashMap<String,Object>()
		BigDecimal grandTotal = new BigDecimal("0.00")
		BigDecimal convertedBalance
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		
		Map<String,String> bucket = ["UPTO6DAYS":'0.00',"UPTO4WEEKS":'0.00',
			"UPTO2MONTHS":'0.00',"UPTO3MONTHS":'0.00',"UPTO4MONTHS":'0.00',"UPTO6MONTHS":'0.00',"UPTO12MONTHS":'0.00',
			"UPTO4YEARS":'0.00',"UPTO9YEARS":'0.00',"UPTO9YEARSBEYOND":'0.00']

		//put constant values
		resultMap.put("transactionType", "Foreign Sight")
		resultMap.put("transactionCode", "U-C-1")
		resultMap.put("currency", "USD")
		
		//put values in mirror map
		mirrorMap.put("transactionType", "Foreign Sight nego")
		mirrorMap.put("transactionCode", "U-C-2")
		mirrorMap.put("currency", "USD")
		
		//process data
		def queryResult = getResultFromQuery(query, "",
				['EXPIRYDATE','ISSUEDATE','OUTSTANDINGBALANCE','CURRENCY','DOCUMENTNUMBER'])
		
		McoReportLoggerService foreignSightLog = new McoReportLoggerService()
		foreignSightLog.setTitleHeader("FOREIGN SIGHT")

		queryResult.each {
			if(it.EXPIRYDATE != null && it.ISSUEDATE != null && it.OUTSTANDINGBALANCE != null && it.CURRENCY != null){
				convertedBalance = convertOutstandingBalance(it.OUTSTANDINGBALANCE, it.CURRENCY,"USD")
				bucket << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']), convertedBalance,bucket)
				grandTotal = grandTotal.add(convertedBalance)
				foreignSightLog.store(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']),
					 it.DOCUMENTNUMBER.toString(), it.CURRENCY.toString(), it.OUTSTANDINGBALANCE.toString(), "USD", convertedBalance.toString(),
					  normalDateFormatter.format(it.EXPIRYDATE.getTime()), normalDateFormatter.format(it.ISSUEDATE.getTime()), reportDate)
			}
		}
//		mirrorMap << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime())), convertedBalance,false)
		
		mirrorMap << formatBucket(bucket)
		resultMap << negateBucket(bucket)
		//put grand total in maps
		mirrorMap.put("grandTotal", getFormatter().format(grandTotal))
		resultMap.put("grandTotal",  getFormatter().format(BigDecimal.ZERO < grandTotal ? grandTotal.negate() : grandTotal))
		
		foreignSightLog.startLog(getFormatter().format(grandTotal))
		mcoReportLoggerService.appendToBuilder(foreignSightLog.getBuilder())

		
		resultList.add(resultMap)
		resultList.add(mirrorMap)
		ext_params << ['foreignSight':resultList]
		println "FINISH FOREIGN SIGHT"
		return ext_params
	}
	
	/**
	 * Load processed values to ext_params['foreignUsance']
	 *
	 * @param ext_params
	 * @return ext_params attached with ['foreignUsance']
	 */
	def processForeignUsance(ext_params){
		String reportDate = dateConverterFormatter.format(getEndOfMonthDate(ext_params['onlineReportDate'],ext_params['specialMco']))
		String query="SELECT LC.EXPIRYDATE,LC.ISSUEDATE, "+
			"LC.OUTSTANDINGBALANCE,TP.CURRENCY,LC.DOCUMENTNUMBER "+
			"FROM LETTEROFCREDIT AS LC "+
			"INNER JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+ 
			"WHERE LC.DOCUMENTTYPE = 'FOREIGN' AND LC.TENOR = 'USANCE' AND LC.OUTSTANDINGBALANCE > 0 " +
			"AND DATE(LC.EXPIRYDATE) >= '" + reportDate + "' " +
			"AND LC.TYPE IN ('REGULAR') AND TP.STATUS NOT IN ('EXPIRED','CANCELLED') " +
			"AND DATE(LC.ISSUEDATE) <= '" + reportDate  + "' " +
			"ORDER BY TP.CURRENCY"
			
		if(ext_params['specialMco'].toString().equalsIgnoreCase('special')){
			query = "SELECT MC.DOCUMENTNUMBER,MC.EXPIRYDATE,MC.REPORTDATE AS ISSUEDATE,MC.CURRENCY,MC.OUTSTANDINGBALANCE,MC.DOCUMENTTYPE "+
					"FROM MCO_MONTH_END_DATA AS MC "+
					"WHERE MC.TRANSACTIONTYPE='FOREIGN_USANCE' AND DATE(MC.REPORTDATE) = '"+ reportDate +  "'"
		}else{
			def responseMap = webAPIService.dummySendCommand(
				[query:query,type:"FOREIGN_USANCE",reportDate:reportDate], "processMonthEnd", "mco/")
		
		}
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>()
		Map<String,Object> resultMap =new HashMap<String,Object>()
		Map<String,Object> mirrorMap =new HashMap<String,Object>()
		BigDecimal grandTotal = new BigDecimal("0.00")
		BigDecimal convertedBalance
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		Map<String,String> bucket = ["UPTO6DAYS":'0.00',"UPTO4WEEKS":'0.00',
			"UPTO2MONTHS":'0.00',"UPTO3MONTHS":'0.00',"UPTO4MONTHS":'0.00',"UPTO6MONTHS":'0.00',"UPTO12MONTHS":'0.00',
			"UPTO4YEARS":'0.00',"UPTO9YEARS":'0.00',"UPTO9YEARSBEYOND":'0.00']

		//put constant values
		resultMap.put("transactionType", "Foreign Usance")
		resultMap.put("transactionCode", "U-C-3")
		resultMap.put("currency", "USD")
		
		//put values in mirror map
		mirrorMap.put("transactionType", "Foreign Usance nego")
		mirrorMap.put("transactionCode", "U-C-4")
		mirrorMap.put("currency", "USD")
		
		//process data
		def queryResult = getResultFromQuery(query, "",
				['EXPIRYDATE','ISSUEDATE','OUTSTANDINGBALANCE','CURRENCY','DOCUMENTNUMBER'])		
		
		McoReportLoggerService foreignUsanceLog = new McoReportLoggerService()
		foreignUsanceLog.setTitleHeader("FOREIGN USANCE")
		
		queryResult.each {
			if(it.EXPIRYDATE != null && it.ISSUEDATE != null && it.OUTSTANDINGBALANCE != null && it.CURRENCY != null){
				convertedBalance = convertOutstandingBalance(it.OUTSTANDINGBALANCE, it.CURRENCY,"USD")
				bucket << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']), convertedBalance,bucket)
				grandTotal = grandTotal.add(convertedBalance)
				foreignUsanceLog.store(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']),
					 it.DOCUMENTNUMBER.toString(), it.CURRENCY.toString(), it.OUTSTANDINGBALANCE.toString(), "USD", convertedBalance.toString(),
					  normalDateFormatter.format(it.EXPIRYDATE.getTime()), normalDateFormatter.format(it.ISSUEDATE.getTime()), reportDate)
			}
		}
//		mirrorMap << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime())), convertedBalance,false)
		
		mirrorMap << formatBucket(bucket)
		resultMap << negateBucket(bucket)
		//put grand total in maps
		mirrorMap.put("grandTotal", getFormatter().format(grandTotal))
		resultMap.put("grandTotal", getFormatter().format(BigDecimal.ZERO < grandTotal ? grandTotal.negate() : grandTotal))
		
		foreignUsanceLog.startLog(getFormatter().format(grandTotal))
		mcoReportLoggerService.appendToBuilder(foreignUsanceLog.getBuilder())

		resultList.add(resultMap)
		resultList.add(mirrorMap)
		ext_params << ['foreignUsance':resultList]
		println "FINISH FOREIGN USANCE"
		return ext_params
	}
	
	/**
	 * Load processed values to ext_params['inwardBills']
	 *
	 * @param ext_params
	 * @return ext_params attached with ['inwardBills']
	 */
	def processInwardBills(ext_params){
		String reportDate = dateConverterFormatter.format(getEndOfMonthDate(ext_params['onlineReportDate'],ext_params['specialMco'])) 
		String daQuery="SELECT DA.MATURITYDATE AS EXPIRYDATE,DA.PROCESSDATE AS ISSUEDATE, "+
			"DA.OUTSTANDINGAMOUNT AS OUTSTANDINGBALANCE,TP.CURRENCY,DA.DOCUMENTNUMBER "+
			"FROM DOCUMENTAGAINSTACCEPTANCE AS DA "+
			"INNER JOIN TRADEPRODUCT AS TP ON DA.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+ 
			"WHERE DA.DOCUMENTTYPE = 'FOREIGN' AND DA.OUTSTANDINGAMOUNT > 0 " +
			"AND TP.STATUS IN ('NEGOTIATED','ACCEPTED') " +
			"AND DATE(DA.PROCESSDATE) <= '" + reportDate  + "' " +
			"ORDER BY TP.CURRENCY"
		String dpQuery = "SELECT DP.MATURITYDATE AS EXPIRYDATE,DP.PROCESSDATE AS ISSUEDATE, "+ 
			"DP.OUTSTANDINGAMOUNT AS OUTSTANDINGBALANCE,TP.CURRENCY,DP.DOCUMENTNUMBER "+
			"FROM DOCUMENTAGAINSTPAYMENT AS DP "+
			"INNER JOIN TRADEPRODUCT AS TP ON DP.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+ 
			"WHERE DP.DOCUMENTTYPE = 'FOREIGN' AND DP.OUTSTANDINGAMOUNT > 0 " +
			"AND TP.STATUS IN ('NEGOTIATED','ACCEPTED') " + 
			"AND DATE(DP.PROCESSDATE) <= '" + reportDate  + "' " +
			"ORDER BY TP.CURRENCY"
			
		if(ext_params['specialMco'].toString().equalsIgnoreCase('special')){
			daQuery = "SELECT MC.DOCUMENTNUMBER,MC.EXPIRYDATE,MC.REPORTDATE AS ISSUEDATE,MC.CURRENCY,MC.OUTSTANDINGBALANCE,MC.DOCUMENTTYPE "+
					"FROM MCO_MONTH_END_DATA AS MC "+
					"WHERE MC.TRANSACTIONTYPE='INWARD_BILLS_DA' AND DATE(MC.REPORTDATE) = '"+ reportDate +  "'"
			dpQuery = "SELECT MC.DOCUMENTNUMBER,MC.EXPIRYDATE,MC.REPORTDATE AS ISSUEDATE,MC.CURRENCY,MC.OUTSTANDINGBALANCE,MC.DOCUMENTTYPE "+
					"FROM MCO_MONTH_END_DATA AS MC "+
					"WHERE MC.TRANSACTIONTYPE='INWARD_BILLS_DP' AND DATE(MC.REPORTDATE) = '"+ reportDate +  "'"
		}else{
			def responseMapDa = webAPIService.dummySendCommand(
				[query:daQuery,type:"INWARD_BILLS_DA",reportDate:reportDate], "processMonthEnd", "mco/")
			
			def responseMapDp = webAPIService.dummySendCommand(
				[query:dpQuery,type:"INWARD_BILLS_DP",reportDate:reportDate], "processMonthEnd", "mco/")
		}
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>()
		Map<String,Object> resultMap =new HashMap<String,Object>()
		Map<String,Object> mirrorMap =new HashMap<String,Object>()
		BigDecimal daGrandTotal = new BigDecimal("0.00")
		BigDecimal dpGrandTotal = new BigDecimal("0.00")
		BigDecimal convertedBalance
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		Map<String,String> bucket = ["UPTO6DAYS":'0.00',"UPTO4WEEKS":'0.00',
			"UPTO2MONTHS":'0.00',"UPTO3MONTHS":'0.00',"UPTO4MONTHS":'0.00',"UPTO6MONTHS":'0.00',"UPTO12MONTHS":'0.00',
			"UPTO4YEARS":'0.00',"UPTO9YEARS":'0.00',"UPTO9YEARSBEYOND":'0.00']

		//put constant values
		resultMap.put("transactionType", "Inward Bills for Collection DA DP")
		resultMap.put("transactionCode", "U-C-6")
		resultMap.put("currency", "USD")

		
		//process data
		def daQueryResult = getResultFromQuery(daQuery, "",
				['EXPIRYDATE','ISSUEDATE','OUTSTANDINGBALANCE','CURRENCY','DOCUMENTNUMBER'])
		
		McoReportLoggerService daLog = new McoReportLoggerService()
		daLog.setTitleHeader("INWARD BILLS DA")
		
		daQueryResult.each {
			if(it.ISSUEDATE != null && it.OUTSTANDINGBALANCE != null && it.CURRENCY != null){
				convertedBalance = convertOutstandingBalance(it.OUTSTANDINGBALANCE, it.CURRENCY,"USD")
				bucket << getBucket(getAge(new Date(it.EXPIRYDATE?.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']), convertedBalance,bucket)
				daGrandTotal = daGrandTotal.add(convertedBalance)
				daLog.store(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']),
					 it.DOCUMENTNUMBER.toString(), it.CURRENCY.toString(), it.OUTSTANDINGBALANCE.toString(), "USD", convertedBalance.toString(),
					  it.EXPIRYDATE ? normalDateFormatter.format(it.EXPIRYDATE.getTime()) : '', normalDateFormatter.format(it.ISSUEDATE.getTime()), reportDate)
			}
		}
		
		def dpQueryResult = getResultFromQuery(dpQuery, "",
			['EXPIRYDATE','ISSUEDATE','OUTSTANDINGBALANCE','CURRENCY','DOCUMENTNUMBER'])
		
		McoReportLoggerService dpLog = new McoReportLoggerService()
		dpLog.setTitleHeader("INWARD BILLS DP")
		
		dpQueryResult.each {
			if(it.ISSUEDATE != null && it.OUTSTANDINGBALANCE != null && it.CURRENCY != null){
				convertedBalance = convertOutstandingBalance(it.OUTSTANDINGBALANCE, it.CURRENCY,"USD")
				bucket << getBucket(1, convertedBalance,bucket)
				dpGrandTotal = dpGrandTotal.add(convertedBalance)
				dpLog.store(1,it.DOCUMENTNUMBER.toString(), it.CURRENCY.toString(), it.OUTSTANDINGBALANCE.toString(), "USD", convertedBalance.toString(),
					 it.EXPIRYDATE ? normalDateFormatter.format(it.EXPIRYDATE.getTime()) : '', normalDateFormatter.format(it.ISSUEDATE.getTime()), reportDate)
			}
		}
//		mirrorMap << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime())), convertedBalance,false)
		
		resultMap << formatBucket(bucket)
		//put grand total in maps
		resultMap.put("grandTotal",getFormatter().format(daGrandTotal.add(dpGrandTotal)))
		
		daLog.startLog(getFormatter().format(daGrandTotal))
		dpLog.startLog(getFormatter().format(dpGrandTotal))
		dpLog.setTotal("GRAND TOTAL:",getFormatter().format(daGrandTotal.add(dpGrandTotal)))
		
		mcoReportLoggerService.appendToBuilder(daLog.getBuilder())
		mcoReportLoggerService.appendToBuilder(dpLog.getBuilder())
		
		resultList.add(resultMap)
		ext_params << ['inwardBills':resultList]
		println "FINISH INWARD BILLS"
		return ext_params
	}
	
	/**
	 * Load processed values to ext_params['domesticSight']
	 *
	 * @param ext_params
	 * @return ext_params attached with ['domesticSight']
	 */
	def processDomesticSight(ext_params){
		String reportDate = dateConverterFormatter.format(getEndOfMonthDate(ext_params['onlineReportDate'],ext_params['specialMco']))
		String query="SELECT LC.EXPIRYDATE,LC.ISSUEDATE, "+
			"LC.OUTSTANDINGBALANCE,TP.CURRENCY,LC.DOCUMENTNUMBER "+
			"FROM LETTEROFCREDIT AS LC "+
			"INNER JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+ 
			"WHERE LC.DOCUMENTTYPE = 'DOMESTIC' AND LC.TENOR = 'SIGHT' AND LC.OUTSTANDINGBALANCE > 0 " +
			"AND DATE(LC.EXPIRYDATE) >= '" + reportDate + "' " +
			"AND LC.TYPE IN ('REGULAR') AND TP.STATUS NOT IN ('EXPIRED','CANCELLED') " +
			"AND DATE(LC.ISSUEDATE) <= '" + reportDate  + "' " +
			"ORDER BY TP.CURRENCY"
		if(ext_params['specialMco'].toString().equalsIgnoreCase('special')){
			query = "SELECT MC.DOCUMENTNUMBER,MC.EXPIRYDATE,MC.REPORTDATE AS ISSUEDATE,MC.CURRENCY,MC.OUTSTANDINGBALANCE,MC.DOCUMENTTYPE "+
					"FROM MCO_MONTH_END_DATA AS MC "+
					"WHERE MC.TRANSACTIONTYPE='DOMESTIC_SIGHT' AND DATE(MC.REPORTDATE) = '"+ reportDate +  "'"
		}else{
			def responseMap = webAPIService.dummySendCommand(
				[query:query,type:"DOMESTIC_SIGHT",reportDate:reportDate], "processMonthEnd", "mco/")
		}
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>()
		Map<String,Object> resultMap =new HashMap<String,Object>()
		Map<String,Object> mirrorMap =new HashMap<String,Object>()
		BigDecimal grandTotal = new BigDecimal("0.00")
		BigDecimal convertedBalance
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		Map<String,String> bucket = ["UPTO6DAYS":'0.00',"UPTO4WEEKS":'0.00',
			"UPTO2MONTHS":'0.00',"UPTO3MONTHS":'0.00',"UPTO4MONTHS":'0.00',"UPTO6MONTHS":'0.00',"UPTO12MONTHS":'0.00',
			"UPTO4YEARS":'0.00',"UPTO9YEARS":'0.00',"UPTO9YEARSBEYOND":'0.00']

		//put constant values
		resultMap.put("transactionType", "Domestic Sight")
		resultMap.put("transactionCode", "P-C-1")
		resultMap.put("currency", "PHP")
		
		//put values in mirror map
		mirrorMap.put("transactionType", "Domestic Sight nego")
		mirrorMap.put("transactionCode", "P-C-2")
		mirrorMap.put("currency", "PHP")
		
		//process data
		def queryResult = getResultFromQuery(query, "",
				['EXPIRYDATE','ISSUEDATE','OUTSTANDINGBALANCE','CURRENCY','DOCUMENTNUMBER'])
		
		McoReportLoggerService domesticSightLog = new McoReportLoggerService()
		domesticSightLog.setTitleHeader("DOMESTIC SIGHT")
		
		queryResult.each {
			if(it.EXPIRYDATE != null && it.ISSUEDATE != null && it.OUTSTANDINGBALANCE != null && it.CURRENCY != null){
				convertedBalance = convertOutstandingBalance(it.OUTSTANDINGBALANCE, it.CURRENCY,"PHP")
				bucket << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']), it.OUTSTANDINGBALANCE,bucket)
				grandTotal = grandTotal.add(convertedBalance)
				domesticSightLog.store(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']),
					 it.DOCUMENTNUMBER.toString(), it.CURRENCY.toString(), it.OUTSTANDINGBALANCE.toString(), "PHP", convertedBalance.toString(),
					  normalDateFormatter.format(it.EXPIRYDATE.getTime()), normalDateFormatter.format(it.ISSUEDATE.getTime()), reportDate)
			}
		}
//		mirrorMap << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime())), convertedBalance,false)
		
		mirrorMap << formatBucket(bucket)
		resultMap << negateBucket(bucket)
		//put grand total in maps
		mirrorMap.put("grandTotal", getFormatter().format(grandTotal))
		resultMap.put("grandTotal", getFormatter().format(BigDecimal.ZERO < grandTotal ? grandTotal.negate() : grandTotal))
		
		domesticSightLog.startLog(getFormatter().format(grandTotal))
		mcoReportLoggerService.appendToBuilder(domesticSightLog.getBuilder())
		
		resultList.add(resultMap)
		resultList.add(mirrorMap)
		ext_params << ['domesticSight':resultList]
		println "FINISH DOMESTIC SIGHT"
		return ext_params
	}
	
	/**
	 * Load processed values to ext_params['domesticUsance']
	 *
	 * @param ext_params
	 * @return ext_params attached with ['domesticUsance']
	 */
	def processDomesticUsance(ext_params){
		String reportDate = dateConverterFormatter.format(getEndOfMonthDate(ext_params['onlineReportDate'],ext_params['specialMco']))
		String query="SELECT LC.EXPIRYDATE,LC.ISSUEDATE, "+
			"LC.OUTSTANDINGBALANCE,TP.CURRENCY,LC.DOCUMENTNUMBER "+
			"FROM LETTEROFCREDIT AS LC "+
			"INNER JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+ 
			"WHERE LC.DOCUMENTTYPE = 'DOMESTIC' AND LC.TENOR = 'USANCE' AND LC.OUTSTANDINGBALANCE > 0 " +
			"AND DATE(LC.EXPIRYDATE) >= '" + reportDate + "' " +
			"AND LC.TYPE IN ('REGULAR') AND TP.STATUS NOT IN ('EXPIRED','CANCELLED') " +
			"AND DATE(LC.ISSUEDATE) <= '" + reportDate  + "' " +
			"ORDER BY TP.CURRENCY"
		if(ext_params['specialMco'].toString().equalsIgnoreCase('special')){
			query = "SELECT MC.DOCUMENTNUMBER,MC.EXPIRYDATE,MC.REPORTDATE AS ISSUEDATE,MC.CURRENCY,MC.OUTSTANDINGBALANCE,MC.DOCUMENTTYPE "+
					"FROM MCO_MONTH_END_DATA AS MC "+
					"WHERE MC.TRANSACTIONTYPE='DOMESTIC_USANCE' AND DATE(MC.REPORTDATE) = '"+ reportDate +  "'"
		}else{
			def responseMap = webAPIService.dummySendCommand(
				[query:query,type:"DOMESTIC_USANCE",reportDate:reportDate], "processMonthEnd", "mco/")
		}
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>()
		Map<String,Object> resultMap =new HashMap<String,Object>()
		Map<String,Object> mirrorMap =new HashMap<String,Object>()
		BigDecimal grandTotal = new BigDecimal("0.00")
		BigDecimal convertedBalance
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		Map<String,String> bucket = ["UPTO6DAYS":'0.00',"UPTO4WEEKS":'0.00',
			"UPTO2MONTHS":'0.00',"UPTO3MONTHS":'0.00',"UPTO4MONTHS":'0.00',"UPTO6MONTHS":'0.00',"UPTO12MONTHS":'0.00',
			"UPTO4YEARS":'0.00',"UPTO9YEARS":'0.00',"UPTO9YEARSBEYOND":'0.00']

		//put constant values
		resultMap.put("transactionType", "Domestic Usance")
		resultMap.put("transactionCode", "P-C-3")
		resultMap.put("currency", "PHP")
		
		//put values in mirror map
		mirrorMap.put("transactionType", "Domestic Usance nego")
		mirrorMap.put("transactionCode", "P-C-4")
		mirrorMap.put("currency", "PHP")
		
		//process data
		def queryResult = getResultFromQuery(query, "",
				['EXPIRYDATE','ISSUEDATE','OUTSTANDINGBALANCE','CURRENCY','DOCUMENTNUMBER'])
		
		McoReportLoggerService domesticUsanceLog = new McoReportLoggerService()
		domesticUsanceLog.setTitleHeader("DOMESTIC USANCE")
		
		queryResult.each {
			if(it.EXPIRYDATE != null && it.ISSUEDATE != null && it.OUTSTANDINGBALANCE != null && it.CURRENCY != null){
				convertedBalance = convertOutstandingBalance(it.OUTSTANDINGBALANCE, it.CURRENCY,"PHP")
				bucket << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']), it.OUTSTANDINGBALANCE,bucket)
				grandTotal = grandTotal.add(convertedBalance)
				domesticUsanceLog.store(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']),
					 it.DOCUMENTNUMBER.toString(), it.CURRENCY.toString(), it.OUTSTANDINGBALANCE.toString(), "PHP", convertedBalance.toString(),
					  normalDateFormatter.format(it.EXPIRYDATE.getTime()), normalDateFormatter.format(it.ISSUEDATE.getTime()), reportDate)
			}
		}
//		mirrorMap << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime())), convertedBalance,false)
		
		mirrorMap << formatBucket(bucket)
		resultMap << negateBucket(bucket)
		//put grand total in maps
		mirrorMap.put("grandTotal", getFormatter().format(grandTotal))
		resultMap.put("grandTotal", getFormatter().format(BigDecimal.ZERO < grandTotal ? grandTotal.negate() : grandTotal))
		
		domesticUsanceLog.startLog(getFormatter().format(grandTotal))
		mcoReportLoggerService.appendToBuilder(domesticUsanceLog.getBuilder())
		
		resultList.add(resultMap)
		resultList.add(mirrorMap)
		ext_params << ['domesticUsance':resultList]
		println "FINISH DOMESTIC USANCE"
		return ext_params
	}
	
	/**
	 * Load processed values to ext_params['cashLc']
	 *
	 * @param ext_params
	 * @return ext_params attached with ['cashLc']
	 */
	def processCashLc(ext_params){
		String reportDate = dateConverterFormatter.format(getEndOfMonthDate(ext_params['onlineReportDate'],ext_params['specialMco']))
		String query="SELECT LC.EXPIRYDATE,LC.ISSUEDATE, "+
			"LC.OUTSTANDINGBALANCE,TP.CURRENCY,LC.DOCUMENTTYPE,LC.DOCUMENTNUMBER "+ 
			"FROM LETTEROFCREDIT AS LC "+
			"INNER JOIN TRADEPRODUCT AS TP ON LC.DOCUMENTNUMBER=TP.DOCUMENTNUMBER "+ 
			"WHERE (LC.TYPE = 'CASH' OR LC.CASHFLAG = 1) AND LC.OUTSTANDINGBALANCE > 0 " +
			"AND TP.STATUS NOT IN ('EXPIRED','CANCELLED') " +
			"AND DATE(LC.ISSUEDATE) <= '" + reportDate  + "' " +
			"ORDER BY TP.CURRENCY"
		if(ext_params['specialMco'].toString().equalsIgnoreCase('special')){
			query = "SELECT MC.DOCUMENTNUMBER,MC.EXPIRYDATE,MC.REPORTDATE AS ISSUEDATE,MC.CURRENCY,MC.OUTSTANDINGBALANCE,MC.DOCUMENTTYPE "+
					"FROM MCO_MONTH_END_DATA AS MC "+
					"WHERE MC.TRANSACTIONTYPE='CASH_LC' AND DATE(MC.REPORTDATE) = '"+ reportDate +  "'"
		}else{
			def responseMap = webAPIService.dummySendCommand(
				[query:query,type:"CASH_LC",reportDate:reportDate], "processMonthEnd", "mco/")
		}
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>()
		Map<String,Object> usdMap =new HashMap<String,Object>()
		Map<String,Object> phpMap =new HashMap<String,Object>()
		BigDecimal grandTotalUsd = new BigDecimal("0.00")
		BigDecimal grandTotalPhp = new BigDecimal("0.00")
		BigDecimal convertedBalance
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		Map<String,String> usdBucket = ["UPTO6DAYS":'0.00',"UPTO4WEEKS":'0.00',
			"UPTO2MONTHS":'0.00',"UPTO3MONTHS":'0.00',"UPTO4MONTHS":'0.00',"UPTO6MONTHS":'0.00',"UPTO12MONTHS":'0.00',
			"UPTO4YEARS":'0.00',"UPTO9YEARS":'0.00',"UPTO9YEARSBEYOND":'0.00']
		Map<String,String> phpBucket = ["UPTO6DAYS":'0.00',"UPTO4WEEKS":'0.00',
		                                "UPTO2MONTHS":'0.00',"UPTO3MONTHS":'0.00',"UPTO4MONTHS":'0.00',"UPTO6MONTHS":'0.00',"UPTO12MONTHS":'0.00',
		                                "UPTO4YEARS":'0.00',"UPTO9YEARS":'0.00',"UPTO9YEARSBEYOND":'0.00']

		//put constant values
		usdMap.put("transactionType", "Bills Payable(From Cash LC)")
		usdMap.put("transactionCode", "U-L05-3")
		usdMap.put("currency", "USD")
		phpMap.put("transactionType", "Bills Payable(From Cash LC)")
		phpMap.put("transactionCode", "U-L05-3")
		phpMap.put("currency", "PHP")
		
		
		//process data
		def queryResult = getResultFromQuery(query, "",
				['EXPIRYDATE','ISSUEDATE','OUTSTANDINGBALANCE','CURRENCY','DOCUMENTTYPE','DOCUMENTNUMBER'])
		
		McoReportLoggerService cashLcUsd = new McoReportLoggerService()
		McoReportLoggerService cashLcPhp = new McoReportLoggerService()
		
		cashLcUsd.setTitleHeader("CASH LC USD")
		cashLcPhp.setTitleHeader("CASH LC PHP")
		
		queryResult.each {
			if(it.EXPIRYDATE != null && it.ISSUEDATE != null && it.OUTSTANDINGBALANCE != null && it.CURRENCY != null && it.DOCUMENTTYPE != null){
				if(it.DOCUMENTTYPE.equalsIgnoreCase("FOREIGN")){
					convertedBalance = convertOutstandingBalance(it.OUTSTANDINGBALANCE, it.CURRENCY,"USD")
					usdBucket << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']), convertedBalance,usdBucket)
					grandTotalUsd = grandTotalUsd.add(convertedBalance)
					cashLcUsd.store(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']),
					 it.DOCUMENTNUMBER.toString(), it.CURRENCY.toString(), it.OUTSTANDINGBALANCE.toString(), "USD", convertedBalance.toString(),
					  normalDateFormatter.format(it.EXPIRYDATE.getTime()), normalDateFormatter.format(it.ISSUEDATE.getTime()), reportDate)
				}else if(it.DOCUMENTTYPE.equalsIgnoreCase("DOMESTIC")){
					convertedBalance = convertOutstandingBalance(it.OUTSTANDINGBALANCE, it.CURRENCY,"PHP")
					phpBucket << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']), convertedBalance,phpBucket)
					grandTotalPhp = grandTotalPhp.add(convertedBalance)
					cashLcPhp.store(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco']),
					 it.DOCUMENTNUMBER.toString(), it.CURRENCY.toString(), it.OUTSTANDINGBALANCE.toString(), "PHP", convertedBalance.toString(),
					  normalDateFormatter.format(it.EXPIRYDATE.getTime()), normalDateFormatter.format(it.ISSUEDATE.getTime()), reportDate)
				}
			}
		}
//		mirrorMap << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime())), convertedBalance,false)
		
		usdMap << formatBucket(usdBucket)
		phpMap << formatBucket(phpBucket)
		//put grand total in maps
		usdMap.put("grandTotal", getFormatter().format(grandTotalUsd))
		phpMap.put("grandTotal", getFormatter().format(grandTotalPhp))
		
		cashLcUsd.startLog(getFormatter().format(grandTotalUsd))
		mcoReportLoggerService.appendToBuilder(cashLcUsd.getBuilder())

		cashLcPhp.startLog(getFormatter().format(grandTotalPhp))
		mcoReportLoggerService.appendToBuilder(cashLcPhp.getBuilder())
		
		resultList.add(usdMap)
		resultList.add(phpMap)
		ext_params << ['cashLc':resultList]
		println "FINISH CASH LC"
		return ext_params
	}
	
	/**
	 * Load processed values to ext_params['outstandingAcceptance']
	 *
	 * @param ext_params
	 * @return ext_params attached with ['outstandingAcceptance']
	 */
	def processOutstandingAcceptance(ext_params){
		String reportDate = dateConverterFormatter.format(getEndOfMonthDate(ext_params['onlineReportDate'],ext_params['specialMco']));
		String query="SELECT "+
			"PD.LOANMATURITYDATE AS EXPIRYDATE, "+ 
			"NG.PROCESSDATE AS ISSUEDATE, "+
			"PD.AMOUNT AS OUTSTANDINGBALANCE, "+ 
			"PD.CURRENCY AS CURRENCY, "+
			"TS.DOCUMENTTYPE AS DOCUMENTTYPE, "+ 
			"CONCAT(NG.DOCUMENTNUMBER,CONCAT('/',NG.NEGOTIATIONNUMBER)) AS DOCUMENTNUMBER "+
			"FROM LCNEGOTIATION NG "+ 
			"INNER JOIN TRADESERVICE TS ON NG.NEGOTIATIONNUMBER=TS.DOCUMENTNUMBER "+ 
			"INNER JOIN PAYMENT PT ON TS.TRADESERVICEID=PT.TRADESERVICEID "+
			"INNER JOIN PAYMENTDETAIL PD ON PT.ID=PD.PAYMENTID "+
			"WHERE PD.PAYMENTINSTRUMENTTYPE IN ('UA_LOAN') AND "+
			"PD.AMOUNT > 0 AND TS.DOCUMENTSUBTYPE1 NOT IN ('STANDBY') "+
			"AND NG.NEGOTIATIONNUMBER NOT IN(SELECT NG.NEGOTIATIONNUMBER FROM LCNEGOTIATION NG "+ 
				"INNER JOIN TRADESERVICE TS2 "+
					"ON NG.DOCUMENTNUMBER=TS2.TRADEPRODUCTNUMBER "+ 
						"AND NG.DOCUMENTNUMBER=TS2.DOCUMENTNUMBER "+
						"AND TS2.SERVICETYPE='UA_LOAN_SETTLEMENT' "+
						"AND TS2.STATUS IN ('APPROVED','POSTED') "+				
						"AND POSITION(NG.NEGOTIATIONNUMBER,VARCHAR(TS2.DETAILS),CODEUNITS16)>0) " +	
			"AND DATE(NG.PROCESSDATE) <= '" + reportDate  + "' "
			"GROUP BY PD.LOANMATURITYDATE, "+
			"NG.PROCESSDATE, "+
			"PD.AMOUNT, "+
			"PD.CURRENCY, "+
			"TS.DOCUMENTTYPE, "+ 
			"NG.NEGOTIATIONNUMBER, "+
			"NG.DOCUMENTNUMBER "+
			"ORDER BY PD.CURRENCY"
		if(ext_params['specialMco'].toString().equalsIgnoreCase('special')){
			query = "SELECT MC.DOCUMENTNUMBER,MC.EXPIRYDATE,MC.REPORTDATE AS ISSUEDATE,MC.CURRENCY,MC.OUTSTANDINGBALANCE,MC.DOCUMENTTYPE "+
					"FROM MCO_MONTH_END_DATA AS MC "+
					"WHERE MC.TRANSACTIONTYPE='FOREIGN_ACCEPTANCE' AND DATE(MC.REPORTDATE) = '"+ reportDate +  "'"
		}else{
			def responseMap = webAPIService.dummySendCommand(
				[query:query,type:"FOREIGN_ACCEPTANCE",reportDate:reportDate], "processMonthEnd", "mco/")
		}
		List<Map<String,Object>> resultList=new ArrayList<Map<String,Object>>()
		Map<String,Object> usdMap =new HashMap<String,Object>()
		Map<String,Object> phpMap =new HashMap<String,Object>()
		BigDecimal grandTotalUsd = new BigDecimal("0.00")
		BigDecimal grandTotalPhp = new BigDecimal("0.00")
		BigDecimal convertedBalance
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		Map<String,String> usdBucket = ["UPTO6DAYS":'0.00',"UPTO4WEEKS":'0.00',
			"UPTO2MONTHS":'0.00',"UPTO3MONTHS":'0.00',"UPTO4MONTHS":'0.00',"UPTO6MONTHS":'0.00',"UPTO12MONTHS":'0.00',
			"UPTO4YEARS":'0.00',"UPTO9YEARS":'0.00',"UPTO9YEARSBEYOND":'0.00']
		Map<String,String> phpBucket = ["UPTO6DAYS":'0.00',"UPTO4WEEKS":'0.00',
            "UPTO2MONTHS":'0.00',"UPTO3MONTHS":'0.00',"UPTO4MONTHS":'0.00',"UPTO6MONTHS":'0.00',"UPTO12MONTHS":'0.00',
            "UPTO4YEARS":'0.00',"UPTO9YEARS":'0.00',"UPTO9YEARSBEYOND":'0.00']

		//put constant values
		usdMap.put("transactionType", "Foreign Outstanding Acceptances")
		usdMap.put("transactionCode", "U-L-20")
		usdMap.put("currency", "USD")
		phpMap.put("transactionType", "Domestic Outstanding Acceptances")
		phpMap.put("transactionCode", "U-L-20")
		phpMap.put("currency", "PHP")
		
		//process data
		def queryResult = getResultFromQuery(query, "",
				['EXPIRYDATE','ISSUEDATE','OUTSTANDINGBALANCE','CURRENCY','DOCUMENTTYPE','NEGOTIATIONNUMBER','DOCUMENTNUMBER'])
		
		McoReportLoggerService foreignAcceptanceUsd = new McoReportLoggerService()
		McoReportLoggerService foreignAcceptancePhp = new McoReportLoggerService()
		foreignAcceptanceUsd.setTitleHeader("FOREIGN OUTSTANDING ACCEPTANCE USD")
		foreignAcceptancePhp.setTitleHeader("FOREIGN OUTSTANDING ACCEPTANCE PHP")
		
		int ageTemp = 0
		queryResult.each {
			if(it.ISSUEDATE != null && it.OUTSTANDINGBALANCE != null && it.CURRENCY != null && it.DOCUMENTTYPE != null){
				ageTemp = getAge(new Date(it.EXPIRYDATE?.getTime()),new Date(it.ISSUEDATE.getTime()),ext_params['onlineReportDate'],ext_params['specialMco'])
				if(ageTemp >= 0){
					if(it.DOCUMENTTYPE.equalsIgnoreCase("FOREIGN")){
						convertedBalance = convertOutstandingBalance(it.OUTSTANDINGBALANCE, it.CURRENCY,"USD")
								usdBucket << getBucket(ageTemp, convertedBalance,usdBucket)
								grandTotalUsd = grandTotalUsd.add(convertedBalance)		
								foreignAcceptanceUsd.store(ageTemp,
										it.DOCUMENTNUMBER.toString(), it.CURRENCY.toString(), it.OUTSTANDINGBALANCE.toString(), "USD", convertedBalance.toString(),
										it.EXPIRYDATE ? normalDateFormatter.format(it.EXPIRYDATE.getTime()) : '', normalDateFormatter.format(it.ISSUEDATE.getTime()), reportDate)
					}else if(it.DOCUMENTTYPE.equalsIgnoreCase("DOMESTIC")){
						convertedBalance = convertOutstandingBalance(it.OUTSTANDINGBALANCE, it.CURRENCY,"PHP")
								phpBucket << getBucket(ageTemp, convertedBalance,phpBucket)
								grandTotalPhp = grandTotalPhp.add(convertedBalance)
								foreignAcceptancePhp.store(ageTemp,
										it.DOCUMENTNUMBER.toString(), it.CURRENCY.toString(), it.OUTSTANDINGBALANCE.toString(), "PHP", convertedBalance.toString(),
										it.EXPIRYDATE ? normalDateFormatter.format(it.EXPIRYDATE.getTime()) : '' , normalDateFormatter.format(it.ISSUEDATE.getTime()), reportDate)
					}
				}
			}
		}
//		mirrorMap << getBucket(getAge(new Date(it.EXPIRYDATE.getTime()),new Date(it.ISSUEDATE.getTime())), convertedBalance,false)
		
		usdMap << formatBucket(usdBucket)
		phpMap << formatBucket(phpBucket)
		//put grand total in maps
		usdMap.put("grandTotal", getFormatter().format(grandTotalUsd))
		phpMap.put("grandTotal", getFormatter().format(grandTotalPhp))
		
		foreignAcceptanceUsd.startLog(getFormatter().format(grandTotalUsd))
		mcoReportLoggerService.appendToBuilder(foreignAcceptanceUsd.getBuilder())

		foreignAcceptancePhp.startLog(getFormatter().format(grandTotalPhp))
		mcoReportLoggerService.appendToBuilder(foreignAcceptancePhp.getBuilder())
		
		resultList.add(usdMap)
		resultList.add(phpMap)
		ext_params << ['outstandingAcceptance':resultList]
		mcoReportLoggerService.writeToFile(ext_params['onlineReportDate'])
		println "FINISH ACCEPTANCE"
		return ext_params
	}
	
	public void deleteAllMcoData(){
		def responseMap = webAPIService.dummySendCommand([:], "deleteAllData", "mco/")
		if(responseMap.success){
			println "SUCCESSFULLY DELETE ALL MCO DATA!"			
		}
	}
	
	/**
	 * Populate the rates list of mco service, always run first
	 */
	public void populateRatesList(String onlineReportDate,boolean historical,String special){
		def responseMap
		List<Map<String,Object>> ratesTemp = new ArrayList<Map<String,Object>>()
		ratesList = new ArrayList<Map<String,Object>>()
		
		responseMap = webAPIService.dummySendQuery(
				[onlineReportDate: new SimpleDateFormat("MM/dd/yyyy").format(getEndOfMonthDate(onlineReportDate,special)),
					rateType:18], "getAllConversionRateByRateNumber", "rates/")
		ratesTemp = (List<Map<String,Object>>) responseMap.response
		println "RATES TEMP DAILY: "+ratesTemp
		if(ratesTemp == null || ratesTemp.isEmpty()){
			responseMap = webAPIService.dummySendQuery(
					[onlineReportDate:new SimpleDateFormat("MM/dd/yyyy").format(getEndOfMonthDate(onlineReportDate,special)),
					 rateType:18], "getAllConversionRateByRateNumberHistorical", "rates/")
			ratesTemp = (List<Map<String,Object>>) responseMap.response
			println "RATES TEMP HISTORICAL: "+ratesTemp
		}		
		ratesList.addAll(ratesTemp)
		println "RATES DATE: "+new SimpleDateFormat("MM/dd/yyyy").format(getEndOfMonthDate(onlineReportDate,special))
		
		println "----RATES LIST----"
		println ratesList
		println "------------------"
		mcoReportLoggerService.setRates(ratesList)
		mcoReportLoggerService.setAgeLegend()
	}
	
	/**
	 * Convert currencies using reval rate 18
	 * @param outstandingBalance
	 * @param sourceCurrency
	 * @param targetCurrency
	 * @return converted outstandingBalance to targetCurrency
	 */
	private BigDecimal convertOutstandingBalance(BigDecimal outstandingBalance,String sourceCurrency,String targetCurrency){
		BigDecimal conversionRate,convertedBalance,phpBalance
		
		if(!ratesList.isEmpty() && outstandingBalance != null){
//			convert to php first
			phpBalance=convertToPhp(outstandingBalance,sourceCurrency)
			conversionRate = findRate("PHP", targetCurrency)
			convertedBalance = phpBalance.divide(conversionRate,2,RoundingMode.FLOOR)
		}else if(outstandingBalance != null){
			convertedBalance = outstandingBalance
		}else{
			convertedBalance = new BigDecimal("0.00")
		}
		return convertedBalance
	}
	
	/**
	 * Rate type #18 only has PHP as base currency, convert to PHP first
	 * before converting to target currency
	 * @param outstandingBalance
	 * @param sourceCurrency
	 * @return
	 */
	private BigDecimal convertToPhp(BigDecimal outstandingBalance,String targetCurrency){
		BigDecimal conversionRate,convertedBalance
		
		if(!ratesList.isEmpty() && outstandingBalance != null){
			conversionRate = findRate("PHP",targetCurrency)
			convertedBalance = outstandingBalance.multiply(conversionRate)
		}else if(outstandingBalance != null){
			convertedBalance = outstandingBalance
		}else{
			convertedBalance = new BigDecimal("0.00")
		}
		return convertedBalance
	}

	/**
	 * Format the bucket map
	 * @param bucket
	 * @return formatted map
	 */
	private Map formatBucket(Map bucket){
		BigDecimal bucketBalance
		Map<String,String> formattedMap = new HashMap<String,String>()
		for(Map.Entry m: bucket.entrySet()){
			bucketBalance=new BigDecimal(m.getValue())
			formattedMap.put(m.getKey(), getFormatter().format(bucketBalance))
		}
		return formattedMap
	}
	
	/**
	 * Negate the bucket map
	 * @param bucket
	 * @return negated map
	 */
	private Map negateBucket(Map bucket){
		BigDecimal bucketBalance
		Map<String,String> negatedMap = new HashMap<String,String>()
		for(Map.Entry m: bucket.entrySet()){
			bucketBalance=new BigDecimal(m.getValue())
			if(BigDecimal.ZERO < bucketBalance){
				bucketBalance=bucketBalance.negate()				
			}
			negatedMap.put(m.getKey(),getFormatter().format(bucketBalance))
		}
		return negatedMap
	}
	
	/**
	 * Classify each entry based on Age.
	 * @param tfsMcoAge
	 * @param outstandingBalance
	 * @param bucket
	 * @return bucket map
	 */
	private Map getBucket(Integer tfsMcoAge,BigDecimal outstandingBalance,Map bucket){
		BigDecimal existingBalance
		if(tfsMcoAge<=6){
			existingBalance=new BigDecimal(bucket["UPTO6DAYS"])
			bucket["UPTO6DAYS"] = outstandingBalance.add(existingBalance)
			return bucket
		}
		else if(tfsMcoAge>6&&tfsMcoAge<=30){
			existingBalance=new BigDecimal(bucket["UPTO4WEEKS"])
			bucket["UPTO4WEEKS"] = outstandingBalance.add(existingBalance)
			return bucket
		}
		else if(tfsMcoAge>30&&tfsMcoAge<=60){
			existingBalance=new BigDecimal(bucket["UPTO2MONTHS"])
			bucket["UPTO2MONTHS"] = outstandingBalance.add(existingBalance)
			return bucket
		}
		else if(tfsMcoAge>60&&tfsMcoAge<=90){
			existingBalance=new BigDecimal(bucket["UPTO3MONTHS"])
			bucket["UPTO3MONTHS"] = outstandingBalance.add(existingBalance)
			return bucket
		}
		else if(tfsMcoAge>90&&tfsMcoAge<=120){
			existingBalance=new BigDecimal(bucket["UPTO4MONTHS"])
			bucket["UPTO4MONTHS"] = outstandingBalance.add(existingBalance)
			return bucket
		}
		else if(tfsMcoAge>120&&tfsMcoAge<=180){
			existingBalance=new BigDecimal(bucket["UPTO6MONTHS"])
			bucket["UPTO6MONTHS"] = outstandingBalance.add(existingBalance)
			return bucket
		}
		else if(tfsMcoAge>180&&tfsMcoAge<=360){
			existingBalance=new BigDecimal(bucket["UPTO12MONTHS"])
			bucket["UPTO12MONTHS"] = outstandingBalance.add(existingBalance)
			return bucket
		}
		else if(tfsMcoAge>360&&tfsMcoAge<=1440){
			existingBalance=new BigDecimal(bucket["UPTO4YEARS"])
			bucket["UPTO4YEARS"] = outstandingBalance.add(existingBalance)
			return bucket
		}
		else if(tfsMcoAge>1440&&tfsMcoAge<=3240){
			existingBalance=new BigDecimal(bucket["UPTO9YEARS"])
			bucket["UPTO9YEARS"] = outstandingBalance.add(existingBalance)
			return bucket
		}
		else if(tfsMcoAge>3240){
			existingBalance=new BigDecimal(bucket["UPTO9YEARSBEYOND"])
			bucket["UPTO9YEARSBEYOND"] = outstandingBalance.add(existingBalance)
			return bucket
		}
		else return bucket
	}
	
	/**
	 * Get the Age of Lc
	 * @param expiryDate
	 * @param issueDate
	 * @return
	 */
	private int getAge(Date expiryDate,Date issueDate,String onlineReportDate,String special){
		if(issueDate == null || onlineReportDate == null){
			return null
		}
		int result=0
		if(expiryDate != null){
			Calendar endOfMonthDate = getEndOfMonthDate(onlineReportDate,special).toCalendar()
			Calendar expiryDateC = expiryDate.toCalendar()
			Calendar issueDateC = issueDate.toCalendar()
			
			int expMinIssue = expiryDateC - issueDateC
			int eomMinIssue = endOfMonthDate - issueDateC
//			result = expMinIssue - eomMinIssue
//			expiry date - report date
			result = expiryDateC - endOfMonthDate			
		}else{
			result = 1
		}
		return result
	}
	
	/**
	 *  Get the end of month date
	 * @return end of month date
	 */
	public Date getEndOfMonthDate(String onlineReportDate,String special){
		Calendar today=new SimpleDateFormat("MM/dd/yyyy").
			parse(onlineReportDate)?.toCalendar() ?: GregorianCalendar.instance
		int endOfMonthDay = 0
		
		if(special != null && special.equalsIgnoreCase("special")){
			return getPreviousMonth(onlineReportDate)
		}else{
			//get end of month day
			endOfMonthDay=today.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)
			//set the overall month end date
			today.set(today.get(today.YEAR),today.get(today.MONTH),endOfMonthDay)
			return today.getTime()
		}
	}
	
	/**
	 * Return previous month for special reports
	 * @param onlineReportDate
	 * @return
	 */
	public Date getPreviousMonth(String onlineReportDate){
		Calendar today = new SimpleDateFormat("MM/dd/yyyy").
		parse(onlineReportDate)?.toCalendar() ?: GregorianCalendar.instance

		
		if(today.get(today.MONTH).equals(today.JANUARY)){
			today.set((today.get(today.YEAR) - 1),today.DECEMBER,1)
			today.set(today.get(today.YEAR),today.get(today.MONTH),today.getActualMaximum(GregorianCalendar.DAY_OF_MONTH))
		}else{
			today.set(today.get(today.YEAR),(today.get(today.MONTH) - 1),1)
			today.set(today.get(today.YEAR),today.get(today.MONTH),today.getActualMaximum(GregorianCalendar.DAY_OF_MONTH))
		}
		return today.getTime()
	}
	
	/**
	 *
	 * Perform SQL operations based on query string
	 *
	 * @param queryString
	 * @param queryParamString - multiple parameters should be concatenated with ||
	 * @param headerName 
	 * @return result list
	 */
	private List getResultFromQuery(String queryString,String queryParamString,List headerNames){
		if(!queryString) return []
		
		List qryParams = queryParamString.tokenize('||')
		
		Session session = sessionFactory.currentSession
		Query query =  session.createSQLQuery(queryString)
		query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)
		
		if(qryParams.size() > 0){
			qryParams.eachWithIndex {parm ,ctr ->
			query.setString(ctr,parm)
			}
		}
		
		return query.list()
	}
	
	/**
	 * Map Header Names to Query result list 
	 * @param queryList
	 * @param headerNames
	 * @return
	 */
	private List convertToMap(List queryList,List headerNames){
		if(queryList == null || headerNames == null) return []
		Map<String,Object> rowMap = new HashMap<String,Object>()
		List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>()
		
		queryList.each {
			for(int x=0;x<headerNames.size();x++){
				rowMap.put(headerNames[x],it[x])
			}
			mapList.add(rowMap)
			rowMap=new HashMap<String,Object>()
		}

		return mapList
	}
	
	/**
	 * Sets decimal formatter to format big decimal
	 * @return formatter to format big decimal
	 */
	private DecimalFormat getFormatter(){
		if(decimalFormat.isParseBigDecimal()){
			return decimalFormat
		}else{
			decimalFormat.setParseBigDecimal(true)
			return decimalFormat
		}
	}

	private BigDecimal findRate(String sourceCurrency,String targetCurrency){
		//sourcecurrency=CURRENCY_CODE
		//targetcurrency=BASE_CURRENCY
		BigDecimal rate = new BigDecimal("1.00")
		if(!ratesList.isEmpty()){
			ratesList.each {
				if(it.CURRENCY_CODE?.toString().trim().equalsIgnoreCase(targetCurrency) && 
					it.BASE_CURRENCY?.toString().trim().equalsIgnoreCase(sourceCurrency)){
					rate=new BigDecimal(it.CONVERSION_RATE?.toString())
				}
			}
		}
		return rate
	}
}