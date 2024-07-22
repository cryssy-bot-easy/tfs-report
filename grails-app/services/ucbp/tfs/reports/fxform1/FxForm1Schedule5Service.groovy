package ucbp.tfs.reports.fxform1

import java.text.SimpleDateFormat
import java.util.List;
import java.util.Map;
import grails.converters.JSON
import org.apache.commons.lang.StringUtils

/***
 * <pre>
 * - program that extract data for fx form 1 schedule 5 report
 * - includes rebates and settlement of actual corres charge
 * </pre>
 * @author Eric Taytayan
 */
class FxForm1Schedule5Service {
	
	/***
	 * imports this class that performs several select scripts in tfs and sibs db
	 */
	def fxForm1DataQueryService
	
	/***
	 * imports this class that performs sql operations in tfs db based on query string
	 */
	def reportDbService
	
	/***
	 * <pre>
	 * - program that extracts data from tfs for bsp report fx form 1 schedule 5
	 * - transactions included:
	 * 	- Rebates
	 * 	- Settlement of Actual Corres Charge
	 * </pre>
	 * @param onlineReportDate
	 * @param cbReportType
	 * 	report or txt file
	 * @return result map list
	 */
	public List<Map<String, Object>> getFxForm1Schedule5DataList(String onlineReportDate, String cbReportType) {
		String getSchedule5DetailsQuery = 
		"""
		SELECT 
			VARCHAR(DETAILS) AS DETAILS 
		FROM TRADESERVICE 
		WHERE
			(
				(DOCUMENTCLASS='REBATE' AND SERVICETYPE='REBATE')
				OR (DOCUMENTCLASS='CORRES_CHARGE' AND SERVICETYPE='SETTLEMENT')
			)
			AND STATUS IN ('APPROVED', 'POSTED', 'POST_APPROVED')
			AND DATE(PROCESSDATE)='$onlineReportDate'
		"""

		List<Map<String, Object>> schedule5ResultList = new ArrayList<Map<String, Object>>()
		def schedule5DetailsList = reportDbService.getResultFromQuery(getSchedule5DetailsQuery, null)
		
		String bookCode = ""
		String currency = ""
		String benefiary = ""
		String remitter = ""
		String benificiaryTin = ""
		String remitterTin = ""
		BigDecimal amount = 0
		
		/*
		 * create a loop that parse and validate each key value
		 * that was extracted from query string getSchedule5DetailsQuery
		 */
		schedule5DetailsList.each {
			Map<String, Object> parsedDetails = JSON.parse(it.toString())
			Map<String, Object> resultMap = new HashMap<String,Object>()
			resultMap.put("BANKCDE", "000470")
			resultMap.put("REFDATE", new SimpleDateFormat("yyyy/MM/dd").format(new Date(parsedDetails.processDate)))
			resultMap.put("FORMNO", "TRD05")
			resultMap.put("TRANCDE", parsedDetails.tranCode ?: "684")
			resultMap.put("DESC40", StringUtils.rightPad("FINANCIAL SERVICES", 40, " "))
			resultMap.put("CTRYCDE", parsedDetails.countryCode ?: "026")
			
			if(parsedDetails.tranCode.equals("184")){
				benefiary = StringUtils.rightPad((parsedDetails.beneficiary ?: "UCPB"), 35, " ")
				remitter = StringUtils.rightPad(parsedDetails.corresBankCode, 35, " ")
				benificiaryTin = fxForm1DataQueryService.formatTinNumber(parsedDetails.beneficiaryTin, "REBATE")
				remitterTin = StringUtils.rightPad("", 25, " ")
			} else {
				benefiary = StringUtils.rightPad(parsedDetails.corresBankCode ?: parsedDetails.reimbursingBank, 35, " ")
				remitter = StringUtils.rightPad((parsedDetails.beneficiary ?: "UCPB"), 35, " ")
				benificiaryTin = StringUtils.rightPad("", 20, " ")
				remitterTin = "000-507-736-000"
			}
			resultMap.put("BENEF", benefiary)
			resultMap.put("REMIT", remitter)
			
			if(parsedDetails.accountType.toString().equalsIgnoreCase("RBU")) {
				bookCode = "1"
			} else if(parsedDetails.accountType.toString().equalsIgnoreCase("FCDU")) {
				bookCode = "2"
			} else {
				bookCode = ""
			}
			resultMap.put("BOOKCDE", bookCode)
			
			currency = parsedDetails.currency.toString()
			amount = new BigDecimal(parsedDetails.amount.replaceAll(",", ""))
			if(currency == "USD") {
				amount = amount
			} else if(currency == "PHP") {
				amount = amount / new BigDecimal(parsedDetails.("USD-PHP_urr") ?: 0)
			} else {
				amount = amount * new BigDecimal(parsedDetails.(currency + "-USD_text_special_rate") ?: 0)
			}
			resultMap.put("CURRCDE", currency)
			resultMap.put("AMTDOLR", amount)
	
			resultMap.put("DOCNO", benificiaryTin ?: null)
			resultMap.put("COMMDESC", remitterTin ?: null)
			resultMap.put("COMMCDE", '0000000' ?: null)
						
			// for report fields
			if(cbReportType == 'report') {
				resultMap.put("COUNTRYNAME", fxForm1DataQueryService.getCountryFromTfs(parsedDetails.countryCode) ?: "USA")
			}
			
			println "schedule5ResultList: >>>>> " + resultMap
			schedule5ResultList.add(resultMap)
		}
		
		// sorts the result list by book code then by trans code
		Collections.sort(schedule5ResultList, fxForm1DataQueryService.sortBookCode)
		Collections.sort(schedule5ResultList, fxForm1DataQueryService.sortTranCode)
		return schedule5ResultList
	}
	
	/***
	 * <pre>
	 * creates a map list for dbf output of the report
	 * that contains the format/structure of the fx form 1 schedule 5
	 * </pre>
	 * @return result map list
	 */
	public List<Map<String,Object>> getFieldHeaders(){
		List<Map<String,Object>> fieldList = new ArrayList<Map<String,Object>>()
		Map<String,Object> resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "BANKCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 6)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "REFDATE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 8)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "FORMNO")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 5)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "TRANCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 3)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "DESC40")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 40)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "CTRYCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 3)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "BENEF")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 35)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "REMIT")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 35)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "BOOKCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 1)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "CURRCDE")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 3)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "AMTDOLR")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 15)
		resultMap.put("Decimal", 2)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "DOCNO")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 20)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "COMMDESC")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 25)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "COMMCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 7)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		return fieldList
	}
}
