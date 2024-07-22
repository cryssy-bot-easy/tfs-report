package ucbp.tfs.reports.fxform1

import java.text.SimpleDateFormat
import java.util.List;
import java.util.Map;
import grails.converters.JSON
import org.apache.commons.lang.StringUtils

/***
 * <pre>
 * - program that extract data for fx form 1 schedule 11 report
 * - reports payment transactions
 * - includes lc negotiation, lc ua loan settlement, lc negotiation and non lc settlement
 * </pre>
 * @author Eric Taytayan
 */
class FxForm1Schedule11Service {
	
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
	 * - program that extracts data from tfs for bsp report fx form 1 schedule 11
	 * - transactions included:
	 * 	- LC Negotiation Cash/Sight
	 * 	- LC UA Loan Settlement
	 * 	- LC Negotiation Discrepancy Regular Sight
	 * 	- Non-LC Settlement
	 * </pre>
	 * @param onlineReportDate
	 * 	date parameter
	 * @return result map list
	 */
	public List<Map<String, Object>> getFxForm1Schedule11DataList(String onlineReportDate) {
		String getSchedule11Query =
		"""
		SELECT
			VARCHAR(DETAILS) AS DETAILS
		FROM TRADESERVICE
		WHERE
			DOCUMENTTYPE='FOREIGN' AND
			(	
				(
					DOCUMENTCLASS='LC' AND
					(
						(SERVICETYPE='NEGOTIATION' AND DOCUMENTSUBTYPE2!='USANCE')
						OR (SERVICETYPE='UA_LOAN_SETTLEMENT' AND DOCUMENTSUBTYPE2='USANCE')
						OR (SERVICETYPE='NEGOTIATION_DISCREPANCY' AND DOCUMENTSUBTYPE1='REGULAR' AND DOCUMENTSUBTYPE2='SIGHT')
					)
				)

				OR

				(DOCUMENTCLASS IN ('DP', 'DA', 'OA', 'DR') AND SERVICETYPE='SETTLEMENT')
			)
			
			AND PAYMENTSTATUS='PAID'
			AND STATUS IN ('APPROVED', 'POST_APPROVED', 'POSTED')
			AND DATE(PROCESSDATE)='$onlineReportDate'
		"""
		
		List<Map<String, Object>> schedule11ResultList = new ArrayList<Map<String, Object>>()
		def schedule11DetailsList = reportDbService.getResultFromQuery(getSchedule11Query, null)
				
		String payMode = ""
		String documentNumber = ""
		String blDate = ""
		String importerCifNumber = ""
		String importerCbCode = ""
		String importerName = ""
		String importerTinNumber = ""
		String countryCode = ""
		String country = ""
		String currency = ""
		String serviceType = ""
		BigDecimal amountOrig = 0
		BigDecimal amountDlr = 0
		
		/*
		 * create a loop that parse and validate each key value
		 * that was extracted from query string getSchedule11Query
		 */
		schedule11DetailsList.each {
			Map<String, Object> parsedDetails = JSON.parse(it.toString())
			Map<String, Object> resultMap = new HashMap<String, Object>()
			resultMap.put("BANKCDE", "000470")
			resultMap.put("REFDATE", new SimpleDateFormat("yyyy/MM/dd").format(new Date(parsedDetails.processDate)))
			resultMap.put("FORMNO", "TRD11")
			resultMap.put("TRANCDE", "010")
			
			if(parsedDetails.documentClass == "LC") {
				payMode = "1"
			} else if(parsedDetails.documentClass == "DP") {
				payMode = "2"
			} else if(parsedDetails.documentClass == "DA") {
				payMode = "3"
			} else if(parsedDetails.documentClass == "OA") {
				payMode = "4"
			} else {
				payMode = "5"
			}
			resultMap.put("PAYMODE", payMode)
			
			documentNumber = (parsedDetails.tradeProductNumber ?: parsedDetails.documentNumber).replaceAll("-", "")
			resultMap.put("LCNO", StringUtils.leftPad((parsedDetails.documentClass.toString()=="LC" ? documentNumber : ""), 20, " "))
			resultMap.put("RGIBRN", StringUtils.leftPad((parsedDetails.documentClass.toString()!="LC" ? documentNumber : ""), 20, " "))
			
			if(parsedDetails.documentClass.toString().equalsIgnoreCase("LC")) {
				blDate = "        "
			} else {
				blDate = getNonLcBlDateFromTfs(documentNumber, parsedDetails.documentClass.toString())
			}
			resultMap.put("BLDATE", blDate.toString())
			
			if(parsedDetails.documentClass.equalsIgnoreCase("LC")) {
				for(Map importerLcTempMap : getImporterFromLetterOfCreditTfs(documentNumber)) {
					importerCifNumber = importerLcTempMap.get("IMPORTERCIFNUMBER")
					importerCbCode = importerLcTempMap.get("IMPORTERCBCODE")
					importerName = importerLcTempMap.get("IMPORTERNAME")
					countryCode = importerLcTempMap.get("EXPIRYCOUNTRYCODE")
				}
			} else {
				importerCifNumber = parsedDetails.importerCifNumber
				importerCbCode = parsedDetails.importerCbCode
				importerName = parsedDetails.importerName
				countryCode = parsedDetails.originalPort
			}
			
			if(importerCifNumber?.toString().trim() != null && importerCifNumber?.toString().trim() != "") {
				if(fxForm1DataQueryService.getCbCodeTinFromSibs(importerCifNumber.toString().trim()).isEmpty()) {
					importerCbCode = null
					importerTinNumber = null
				}
				for(Map cbCodeTinTempMap : fxForm1DataQueryService.getCbCodeTinFromSibs(importerCifNumber.toString().trim())) {
					importerCbCode = cbCodeTinTempMap.get("CBCODE")
					importerTinNumber = cbCodeTinTempMap.get("TIN")
				}
			} else {
				importerCbCode = importerCbCode
				importerTinNumber = fxForm1DataQueryService.getTinFromTfs(importerCbCode.toString().trim())?.trim()
			}			
			importerCbCode = (importerCbCode==null || importerCbCode=="null" || importerCbCode?.trim()=="") ? "0000000000" : StringUtils.leftPad(importerCbCode.trim(), 10, "0")
			resultMap.put("IMPCDE", importerCbCode)
			
			countryCode = StringUtils.leftPad(StringUtils.right(countryCode, 3), 3, "0")
			if(countryCode.matches("^[0-9]*")) {
				countryCode = countryCode
				country = fxForm1DataQueryService.getCountryFromTfs(countryCode)
			} else {
				countryCode = fxForm1DataQueryService.getCountryCodeFromTfs(parsedDetails.originalPort)
				country = parsedDetails.originalPort
			}
			resultMap.put("CTRYCDE", countryCode)
			
			resultMap.put("COMMCDE", '0000000')
			resultMap.put("COMMDESC", '                         ')
			resultMap.put("LCBNKCDE", '000000000000')
			resultMap.put("BOOKCDE", '1')
			
			serviceType = parsedDetails.serviceType.toString().replaceAll("_", " ")
			if(serviceType.toUpperCase().contains("NEGOTIATION")) {
				currency = 	parsedDetails.negotiationCurrency.toString()
				amountOrig = new BigDecimal(parsedDetails.negotiationAmount.toString().replaceAll(",", "") ?: 0)
			} else if(serviceType.equalsIgnoreCase("UA LOAN SETTLEMENT")) {
				currency = 	parsedDetails.fxUaLoanCurrency ?: parsedDetails.currency 
				amountOrig = new BigDecimal(parsedDetails.amount.toString().replaceAll(",", "") ?: 0)
			} else {
				currency = parsedDetails.currency.toString()
				amountOrig = new BigDecimal(parsedDetails.productAmount.toString().replaceAll(",", "") ?: 0)
			}
			
			// As per Ms. Claire, reported currency must be in USD.
			// resultMap.put("CURRCDE", currency)
			resultMap.put("CURRCDE", "USD")
			
			if(currency == "USD") {
				amountDlr = amountOrig
			} else {
				amountDlr = amountOrig * new BigDecimal(parsedDetails.(currency + "-USD_text_special_rate") ?: 0)
			}
			resultMap.put("AMTDOLR", amountDlr)
			
			importerTinNumber = (importerTinNumber==null || importerTinNumber=="null" || importerTinNumber?.trim()=="") ? StringUtils.rightPad("", 20, " ")
				: fxForm1DataQueryService.formatTinNumber(importerTinNumber.replaceAll("-", ""), parsedDetails.documentClass.toString())
			resultMap.put("DOCNO", importerTinNumber)

			// for report fields
			resultMap.put("DOCUMENTCLASS", parsedDetails.documentClass)
			resultMap.put("IMPORTERNAME", importerName?.toUpperCase())
			resultMap.put("COUNTRYNAME", country)
						
			println "schedule11ResultList: >>>>> " + resultMap
			schedule11ResultList.add(resultMap)	
		}
		
		// sorts the result list by payment mode then by pay mode
		Collections.sort(schedule11ResultList, fxForm1DataQueryService.sortPayMode)
		return schedule11ResultList
	}
	
	/***
	 * method that extracts importer details from letter of credit table
	 * @param documentNumber
	 * @return result map list
	 */
	private List<Map<String, Object>> getImporterFromLetterOfCreditTfs(String documentNumber) {
		String getImporterQueryString =
		"""
		SELECT
			IMPORTERCIFNUMBER, 
			IMPORTERCBCODE, 
			IMPORTERNAME,
			EXPIRYCOUNTRYCODE
		FROM LETTEROFCREDIT
		WHERE 
			REPLACE(DOCUMENTNUMBER, '-', '')='$documentNumber'
		"""
		List<Map<String, Object>> importerLc = fxForm1DataQueryService.getTfsResultFromQuery(getImporterQueryString, "")
	}
	
	/***
	 * method for extracting bl date in non lc's main table 
	 * @param documentNumber
	 * @param documentClass
	 * @return result map list
	 */
	private String getNonLcBlDateFromTfs(String documentNumber, String documentClass) {
		if(documentClass == "DA") {
			documentClass = "DOCUMENTAGAINSTACCEPTANCE"
		} else if(documentClass == "DP") {
			documentClass = "DOCUMENTAGAINSTPAYMENT"
		} else if(documentClass == "OA") {
			documentClass = "OPENACCOUNT"
		} else {
			documentClass = "DIRECTREMITTANCE"
		}
		
		String getBlDateQueryString = "SELECT VARCHAR_FORMAT(DATEOFBLAIRWAYBILL, 'YYYY/MM/DD') AS BLDATE FROM $documentClass WHERE REPLACE(DOCUMENTNUMBER, '-', '')='$documentNumber'"
		
		List<Map<String, Object>> blDate = fxForm1DataQueryService.getTfsResultFromQuery(getBlDateQueryString, "")
		
		for(Map<String, Object> blDateTempMap : blDate) {
			return blDateTempMap.get("BLDATE")
		}
	}
	
	/***
	 * <pre>
	 * creates a map list for dbf output of the report
	 * that contains the format/structure of the fx form 1 schedule 11
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
		resultMap.put("Name", "PAYMODE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 1)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "LCNO")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 20)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "RGIBRN")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 20)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "BLDATE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 8)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "IMPCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 10)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "CTRYCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 3)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "COMMCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 7)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "COMMDESC")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 25)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "LCBNKCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 12)
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
		
		return fieldList
	}
}


