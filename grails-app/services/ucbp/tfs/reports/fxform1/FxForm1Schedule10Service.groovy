package ucbp.tfs.reports.fxform1

import java.text.SimpleDateFormat
import java.util.List;
import java.util.Map;
import grails.converters.JSON
import org.apache.commons.lang.StringUtils

/***
 * <pre>
 * - program that extract data for fx form 1 schedule 10 report
 * - reports booking transactions
 * - includes lc opening, lc amendment and non lc (da and oa) negotiation
 * </pre>
 * @author Eric Taytayan
 */
class FxForm1Schedule10Service {
	
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
	 * - program that extracts data from tfs for bsp report fx form 1 schedule 10
	 * - transactions included:
	 * 	- LC Opening
	 * 	- LC Amendment (amount or expiry date that was amended)
	 * 	- Non-LC Negotiation (DA and OA only)
	 * </pre>
	 * @param onlineReportDate
	 * 	date parameter
	 * @return result map list
	 */
	public List<Map<String, Object>> getFxForm1Schedule10DataList(String onlineReportDate) {
		
		String getSchedule10Query =
		"""
		SELECT 
			VARCHAR(DETAILS) AS DETAILS
		FROM TRADESERVICE
		WHERE 
			DOCUMENTTYPE='FOREIGN' AND
			(
				(DOCUMENTCLASS='LC' AND SERVICETYPE='OPENING' AND DOCUMENTSUBTYPE1!='STANDBY') 
				OR (DOCUMENTCLASS='LC' AND SERVICETYPE='AMENDMENT' AND DOCUMENTSUBTYPE1!='STANDBY' AND (VARCHAR(DETAILS) LIKE ? OR VARCHAR(DETAILS) LIKE ?))
				OR (DOCUMENTCLASS IN ('DA', 'OA') AND SERVICETYPE IN ('NEGOTIATION', 'NEGOTIATION_ACKNOWLEDGEMENT'))
			)
			AND STATUS IN ('APPROVED', 'POST_APPROVED', 'POSTED')
			AND DATE(PROCESSDATE)='$onlineReportDate'
		"""

		List<Map<String, Object>> schedule10ResultList = new ArrayList<Map<String, Object>>()
		def schedule10DetailsList = reportDbService.getResultFromQuery(getSchedule10Query, '%"amountSwitch":"on"%||%"expiryDateSwitch":"on"%')
		
		String payMode = ""
		String statCde = ""
		String documentNumber = ""
		String blDate = ""
		String importerCifNumber = ""
		String importerCbCode = ""
		String importerName = ""
		String importerTinNumber = ""
		String countryCode = ""
		String country = ""
		String expiryDate = ""
		String maturityDate = ""
		String currency = ""
		String serviceType = ""
		BigDecimal amountOrig = 0
		BigDecimal amountDlr = 0
		
		/*
		 * create a loop that parse and validate each key value
		 * that was extracted from query string getSchedule9Query
		 */
		schedule10DetailsList.each {
			Map<String, Object> parsedDetails = JSON.parse(it.toString())
			Map<String, Object> resultMap = new HashMap<String, Object>()
			resultMap.put("BANKCDE", "000470")
			resultMap.put("REFDATE", new SimpleDateFormat("yyyy/MM/dd").format(new Date(onlineReportDate)))
			resultMap.put("FORMNO", "TRD10")
			
			if(parsedDetails.documentClass == "LC") {
				payMode = "1"
			} else if(parsedDetails.documentClass == "DA") {
				payMode = "3"
			} else {
				payMode = "4"
			}
			resultMap.put("PAYMODE", payMode)
			
			if(parsedDetails.serviceType.toString().equalsIgnoreCase("AMENDMENT")) {
				statCde = "2"
			} else if(parsedDetails.documentSubType1.toString().equalsIgnoreCase("CASH")) {
				statCde = "4"
			} else if(parsedDetails.documentClass.toString() in ["DA", "OA"]) {
				statCde = "6"
			} else if(parsedDetails.documentSubType2.toString().equalsIgnoreCase("SIGHT") || parsedDetails.tenor.toString().equalsIgnoreCase("SIGHT")) {
				statCde = "7"
			} else {
				statCde = "8"
			}
			resultMap.put("STATCDE", statCde)
			
			documentNumber = (parsedDetails.tradeProductNumber ?: parsedDetails.documentNumber).replaceAll("-", "")
			resultMap.put("LCNO", StringUtils.leftPad((parsedDetails.documentClass.toString()=="LC" ? documentNumber : ""), 20, " "))
			resultMap.put("RGIBRN", StringUtils.leftPad((parsedDetails.documentClass.toString()!="LC" ? documentNumber : ""), 20, " "))
			
			if(parsedDetails.documentClass.toString().equalsIgnoreCase("LC")) {
				blDate = "        "
			} else {
				blDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date(parsedDetails.dateOfBlAirwayBill.toString()))
			}
			resultMap.put("BLDATE", blDate.toString())
		
			if(parsedDetails.importerCifNumber?.toString().trim() != null && parsedDetails.importerCifNumber?.toString().trim() != "") {
				if(fxForm1DataQueryService.getCbCodeTinFromSibs(parsedDetails.importerCifNumber.toString().trim()).isEmpty()) {
					importerCbCode = null
					importerTinNumber = null
				}
				for(Map cbCodeTinTempMap : fxForm1DataQueryService.getCbCodeTinFromSibs(parsedDetails.importerCifNumber.toString().trim())) {
					importerCbCode = cbCodeTinTempMap.get("CBCODE")
					importerTinNumber = cbCodeTinTempMap.get("TIN")
				}
			} else {
				importerCbCode = parsedDetails.importerCbCode
				importerTinNumber = fxForm1DataQueryService.getTinFromTfs(importerCbCode.toString().trim())
			}
			
			importerCbCode = (importerCbCode==null || importerCbCode=="null" || importerCbCode?.trim()=="") ? "0000000000" : StringUtils.leftPad(importerCbCode.trim(), 10, "0")
			resultMap.put("IMPCDE", importerCbCode)
			
			countryCode = parsedDetails.bspCountryCode ?: parsedDetails.originalPort_bspCode ?: parsedDetails.originalPort ?: ""
			countryCode = StringUtils.leftPad(StringUtils.right(countryCode, 3), 3, "0")
			if(countryCode.matches("^[0-9]*")) {
				countryCode = countryCode
				country = fxForm1DataQueryService.getCountryFromTfs(countryCode)
			} else {
				countryCode = fxForm1DataQueryService.getCountryCodeFromTfs(parsedDetails.originalPort)
				country = parsedDetails.originalPort
			}
			resultMap.put("CTRYCDE", countryCode)
			
			resultMap.put("COMMCDE", "0000000")
			resultMap.put("COMMDESC", "                         ")
			
			currency = parsedDetails.currency.toString()
			amountOrig = new BigDecimal(parsedDetails.amount.toString().replaceAll(",", ""))
			expiryDate = parsedDetails.expiryDate
						
			if(parsedDetails.serviceType.toString().equalsIgnoreCase("AMENDMENT") && (parsedDetails.amountSwitch == "on" || parsedDetails.expiryDateSwitch == "on")) {
				if(parsedDetails.amountSwitch == "on") {
					amountOrig = new BigDecimal(parsedDetails.amountTo.toString().replaceAll(",", ""))					
				} 
				if(parsedDetails.expiryDateSwitch == "on") {
					expiryDate = parsedDetails.expiryDateTo
				}
			}
			
			if(parsedDetails.documentClass.equalsIgnoreCase("LC")) {
				expiryDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date(expiryDate.toString()))
				maturityDate = "        "
			} else {
				expiryDate = "        "
				maturityDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date(parsedDetails.maturityDate.toString()))
			}		
			resultMap.put("LCEXPDTE", expiryDate)
			resultMap.put("DUEDTE",  maturityDate)
			
			resultMap.put("CURRCDE", currency)			
			if(currency == "USD") {
				amountDlr = amountOrig
			} else {
				if(parsedDetails.documentClass in ["DA", "OA"]) {
					amountDlr = amountOrig * new BigDecimal(parsedDetails.(currency + "-USD") ?: 0)
				} else {
					amountDlr = amountOrig * new BigDecimal(parsedDetails.(currency + "-USD_text_special_rate") ?: 0)
				}
			}
			resultMap.put("AMTDOLR", amountDlr)
			
			importerTinNumber = (importerTinNumber==null || importerTinNumber=="null" || importerTinNumber?.trim()=="") ? StringUtils.rightPad("", 20, " ")
				: fxForm1DataQueryService.formatTinNumber(importerTinNumber.replaceAll("-", ""), parsedDetails.documentClass.toString())
			resultMap.put("DOCNO", importerTinNumber)

			// for report fields
			resultMap.put("DOCUMENTCLASS", parsedDetails.documentClass)
			resultMap.put("IMPORTERNAME", parsedDetails.importerName?.toUpperCase())
			resultMap.put("COUNTRYNAME", country)
						
			println "schedule10ResultList: >>>>> " + resultMap
			schedule10ResultList.add(resultMap)	
		}
		
		// sorts the result list by payment mode then by stat code
		Collections.sort(schedule10ResultList, fxForm1DataQueryService.sortStatCode)
		Collections.sort(schedule10ResultList, fxForm1DataQueryService.sortPayMode)
		return schedule10ResultList
	}
	
	/***
	 * <pre>
	 * creates a map list for dbf output of the report
	 * that contains the format/structure of the fx form 1 schedule 10
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
		resultMap.put("Name", "PAYMODE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 1)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "STATCDE")
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
		resultMap.put("Name", "LCEXPDTE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 8)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "DUEDTE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 8)
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
