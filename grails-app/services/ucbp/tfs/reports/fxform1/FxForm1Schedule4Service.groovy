package ucbp.tfs.reports.fxform1

import java.text.SimpleDateFormat
import java.util.List;
import java.util.Map;
import grails.converters.JSON
import org.apache.commons.lang.StringUtils

/***
 * <pre>
 * - program that extract data for fx form 1 schedule 4 report
 * - includes import advance payment and export advance refund
 * </pre>
 * @author Eric Taytayan
 */
class FxForm1Schedule4Service {
	
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
	 * - program that extracts data from tfs for bsp report fx form 1 schedule 4
	 * - transactions included:
	 * 	- Import Advance Payment
	 *	- Export Advance Refund
	 * </pre>
	 * @param onlineReportDate
	 * @param cbReportType
	 * 	report or txt file
	 * @return result map list
	 */
	public List<Map<String, Object>> getFxForm1Schedule4DataList(String onlineReportDate, String cbReportType) {
		String getSchedule4Query =
		"""
		SELECT
			VARCHAR(DETAILS) AS DETAILS
		FROM TRADESERVICE
		WHERE
			(
				(DOCUMENTCLASS='EXPORT_ADVANCE' AND SERVICETYPE='REFUND')
				OR (DOCUMENTCLASS='IMPORT_ADVANCE' AND SERVICETYPE='PAYMENT')
			)
			AND STATUS IN ('APPROVED', 'POSTED', 'POST_APPROVED')
			AND DATE(PROCESSDATE) = '$onlineReportDate'
		"""

		List<Map<String, Object>> schedule4ResultList = new ArrayList<Map<String, Object>>()
		def schedule4DetailsList = reportDbService.getResultFromQuery(getSchedule4Query, null)
		
		String documentClass = ""
		String tranCode = ""
		String documentNumber = ""
		String creditFacility = ""
		String creditFacilityCode = ""
		String exporterCbCode = ""
		String importerCbCode = ""
		String debtorCode = ""
		String creditorCode = ""
		String countryCode = ""
		String country = ""
		String shipmentDate = ""
		String currency = ""
		String accountType = ""
		String debtorTin = ""
		String creditorTin = ""
		String debtorName = ""
		String creditorName = ""
		BigDecimal amountOrig = 0
		BigDecimal amountDolr = 0
		
		List<Map<String, Object>> advancePayment = null
		
		/*
		 * create a loop that parse and validate each key value
		 * that was extracted from query string getSchedule4Query
		 */
		schedule4DetailsList.each {
			Map<String, Object> parsedDetails = JSON.parse(it.toString())
			Map<String, Object> resultMap = new HashMap<String, Object>()
			resultMap.put("BANKCDE", "000470")
			resultMap.put("REFDATE", new SimpleDateFormat("yyyy/MM/dd").format(new Date(parsedDetails.processDate)))
			resultMap.put("FORMNO", "TRD04")
			
			documentClass = parsedDetails.documentClass?.toUpperCase()
			if(documentClass == "EXPORT_ADVANCE") {
				tranCode = "020"
			} else {
				tranCode = "040"
			}
			resultMap.put("TRANCDE", tranCode)
			
			if(cbReportType == "report") {
				documentNumber = parsedDetails.documentNumber?.replaceAll("-", "")
			} else {
				documentNumber = StringUtils.right(parsedDetails.documentNumber?.replaceAll("-", ""), 15)
			}
			resultMap.put("REFNO", documentNumber)
			resultMap.put("CDAN", "          ")
			
			advancePayment = fxForm1DataQueryService.getAdvancePayment(parsedDetails.documentNumber)
			for(Map advancePaymentTempMap : advancePayment) {
				creditFacility = advancePaymentTempMap.get("CREDITFACILITY")
				exporterCbCode = advancePaymentTempMap.get("EXPORTERCBCODE")
				importerCbCode = advancePaymentTempMap.get("IMPORTERCBCODE")
				shipmentDate = advancePaymentTempMap.get("SHIP_DATE")
				accountType = advancePaymentTempMap.get("ACCT_TYPE")
				debtorName = advancePaymentTempMap.get("DEBTORNAME")
				creditorName = advancePaymentTempMap.get("CREDITORNAME")
			}
			
			if(creditFacility == "SHORTTERM") {
				creditFacilityCode = "1"
			} else {
				creditFacilityCode = "2"
			}
			resultMap.put("CRFCDE", creditFacilityCode)
			
			debtorCode = (exporterCbCode==null || exporterCbCode=="null" || exporterCbCode?.trim()=="") ? "0000000000" : StringUtils.leftPad(exporterCbCode?.trim(), 10, "0")
			creditorCode = (importerCbCode==null || importerCbCode=="null" || importerCbCode?.trim()=="") ? "000000000000" : StringUtils.leftPad(importerCbCode?.trim(), 12, "0")
			resultMap.put("DEBTCDE", debtorCode)
			resultMap.put("CREDCDE", creditorCode)
			
			countryCode = StringUtils.leftPad(StringUtils.right(parsedDetails.countryCode.toString(), 3), 3, "0")
			if(countryCode.matches("^[0-9]*")) {
				countryCode = countryCode
				country = fxForm1DataQueryService.getCountryFromTfs(countryCode)
			} else {
				countryCode = "000"
				country = parsedDetails.countryCode
			}
			resultMap.put("CTRYCDE", countryCode)
			
			shipmentDate = (shipmentDate != null && shipmentDate != "null" && shipmentDate != "") ? shipmentDate : "00000000"
			resultMap.put("DUEDTE", shipmentDate)
			
			currency = parsedDetails.currency
			resultMap.put("CURRCDE", currency)
			
			if(accountType?.equalsIgnoreCase("RBU")) {
				accountType = "1"
			} else {
				accountType = "2"
			}
			resultMap.put("BOOKCDE", accountType)
			resultMap.put("FEECDE", 0)
			resultMap.put("FEEORIG", 0)
			resultMap.put("FEEDOLR", 0)
			resultMap.put("INTORIG", 0)
			resultMap.put("INTDOLR", 0)
			
			amountOrig = new BigDecimal(parsedDetails.amount?.replaceAll(",", "") ?: 0)
			if(currency?.equalsIgnoreCase("USD")) {
				amountDolr = amountOrig
			} else if(currency?.equalsIgnoreCase("PHP")) {
				amountDolr = amountOrig / new BigDecimal(parsedDetails.("USD-PHP_urr") ?: 0)
			} else {
				amountDolr = amountOrig * new BigDecimal(parsedDetails.(currency + "-USD_text_special_rate") ?: 0)
			}
			resultMap.put("PRINORIG", amountOrig)
			resultMap.put("PRINDOLR", amountDolr)

			debtorTin = (exporterCbCode==null || exporterCbCode=="null" || exporterCbCode?.trim()=="") ? ""
				: fxForm1DataQueryService.getTinFromTfs(exporterCbCode?.trim())?.trim()
			debtorTin = (debtorTin!=null && debtorTin!="null" && debtorTin?.trim()!="" && documentClass=="EXPORT_ADVANCE") ?
				fxForm1DataQueryService.formatTinNumber(debtorTin.replaceAll("-", ""), "EXPORT_ADVANCE") : StringUtils.rightPad("", 20, " ")
			creditorTin = (importerCbCode==null || importerCbCode=="null" || importerCbCode?.trim()=="") ? ""
				: fxForm1DataQueryService.getTinFromTfs(importerCbCode?.trim())?.trim()
			creditorTin = (creditorTin!=null && creditorTin!="null" && creditorTin?.trim()!="" && documentClass=="IMPORT_ADVANCE") ?
				fxForm1DataQueryService.formatTinNumber(creditorTin.replaceAll("-", ""), "IMPORT_ADVANCE") : StringUtils.rightPad("", 25, " ")
			resultMap.put("DOCNO", debtorTin)
			resultMap.put("COMMDESC", creditorTin)
			
			// for report fields
			resultMap.put("DEBTORNAME", debtorName)
			resultMap.put("CREDITORNAME", creditorName)
			resultMap.put("COUNTRYNAME", !country?.equalsIgnoreCase("ull") ? country : "")
			
			println "schedule4ResultList: >>>>> " + resultMap
			
			schedule4ResultList.add(resultMap)
		}
		
		// sorts the result list by book code then by trans code and credit facility
		Collections.sort(schedule4ResultList, fxForm1DataQueryService.sortCreditFacility)
		Collections.sort(schedule4ResultList, fxForm1DataQueryService.sortTranCode)
		Collections.sort(schedule4ResultList, fxForm1DataQueryService.sortBookCode)
		return schedule4ResultList
	}
	
	
	/***
	 * <pre>
	 * creates a map list for dbf output of the report 
	 * that contains the format/structure of the fx form 1 schedule 4
	 * </pre>
	 * @return result map list
	 */
	public List<Map<String,Object>> getFieldHeaders() {
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
		resultMap.put("Name", "REFNO")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 15)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "CDAN")
		resultMap.put("Type", 'C')
		resultMap.put("Length", 10)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "CRFCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 1)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "DEBTCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 10)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "CREDCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 12)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "CTRYCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 3)
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
		resultMap.put("Name", "BOOKCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 1)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "FEECDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 1)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "FEEORIG")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 15)
		resultMap.put("Decimal", 2)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "FEEDOLR")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 15)
		resultMap.put("Decimal", 2)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "INTORIG")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 15)
		resultMap.put("Decimal", 2)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "INTDOLR")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 15)
		resultMap.put("Decimal", 2)
		fieldList.add(resultMap)
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "PRINORIG")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 15)
		resultMap.put("Decimal", 2)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "PRINDOLR")
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
		
		return fieldList
	}
}
