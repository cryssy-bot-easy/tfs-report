package ucbp.tfs.reports.fxform1

import grails.converters.JSON;
import java.text.SimpleDateFormat
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils

/***
 * <pre>
 * - program that extract data for fx form 1 schedule 3 report
 * - includes import advance refund and export advance payment
 * </pre>
 * @author Eric Taytayan
 */
class FxForm1Schedule3Service {
	
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
	 * - program that extracts data from tfs for bsp report fx form 1 schedule 3
	 * - transactions included:
	 * 	- Import Advance Refund
	 *	- Export Advance Payment
	 * </pre>
	 * @param onlineReportDate
	 * @param cbReportType
	 * 	report or txt file
	 * @return result map list
	 */
	public List<Map<String, Object>> getFxForm1Schedule3DataList(String onlineReportDate, String cbReportType) {
		String getSchedule3Query = 
		"""
		SELECT
			VARCHAR(DETAILS) AS DETAILS
		FROM TRADESERVICE
		WHERE
			(
				(DOCUMENTCLASS='EXPORT_ADVANCE' AND SERVICETYPE='PAYMENT')
				OR (DOCUMENTCLASS='IMPORT_ADVANCE' AND SERVICETYPE='REFUND')
			)
			AND STATUS IN ('APPROVED', 'POSTED', 'POST_APPROVED')
			AND DATE(PROCESSDATE) = '$onlineReportDate'
		"""
			
		List<Map<String, Object>> schedule3ResultList = new ArrayList<Map<String, Object>>()
		def schedule3DetailsList = reportDbService.getResultFromQuery(getSchedule3Query, null)
		
		String documentClass = ""
		String tranCode = ""
		String documentNumber = ""
		String creditFacility = ""
		String creditFacilityCode = ""
		String exporterCbCode = ""
		String importerCbCode = ""
		String debtorCode = ""
		String creditorCode = ""
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
		 * that was extracted from query string getSchedule3Query
		 */
		schedule3DetailsList.each {
			Map<String, Object> parsedDetails = JSON.parse(it.toString())
			Map<String, Object> resultMap = new HashMap<String, Object>()
			resultMap.put("BANKCDE", "000470")
			resultMap.put("REFDATE", new SimpleDateFormat("yyyy/MM/dd").format(new Date(parsedDetails.processDate)))
			resultMap.put("FORMNO", "TRD03")
			
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
			
			amountOrig = new BigDecimal(parsedDetails.amount?.replaceAll(",", "") ?: 0)
			if(currency?.equalsIgnoreCase("USD")) {
				amountDolr = amountOrig
			} else if(currency.equalsIgnoreCase("PHP")) {
				amountDolr = amountOrig / new BigDecimal(parsedDetails.("USD-PHP_urr") ?: 0)
			} else {
				amountDolr = amountOrig * new BigDecimal(parsedDetails.(currency + "-USD_text_special_rate") ?: 0)
			}
			resultMap.put("AMTORIG", amountOrig)
			resultMap.put("AMTDOLR", amountDolr)
			
			debtorTin = (exporterCbCode==null || exporterCbCode=="null" || exporterCbCode?.trim()=="") ? ""
				: fxForm1DataQueryService.getTinFromTfs(exporterCbCode?.trim())?.trim()
			debtorTin = (debtorTin!=null && debtorTin!="null" && debtorTin?.trim()!="" && documentClass=="EXPORT_ADVANCE") ?
				fxForm1DataQueryService.formatTinNumber(debtorTin.replaceAll("-", ""), "EXPORT_ADVANCE") : StringUtils.rightPad("", 20, " ")
			creditorTin = (importerCbCode==null || importerCbCode=="null" || importerCbCode?.trim()=="") ? ""
				: fxForm1DataQueryService.getTinFromTfs(importerCbCode?.trim())?.trim()
			creditorTin = (creditorTin!=null && creditorTin!="null" && creditorTin?.trim()!="" && documentClass=="IMPORT_ADVANCE") ?
				fxForm1DataQueryService.formatTinNumber(creditorTin.replaceAll("-", ""), "IMPORT_ADVANCE") : StringUtils.rightPad("", 25, " ")
			resultMap.put("DOCNO", debtorTin)
			resultMap.put("COMDESC", creditorTin)
			
			// for report fields
			resultMap.put("DEBTORNAME", debtorName)
			resultMap.put("CREDITORNAME", creditorName)
			
			println "schedule3ResultList: >>>>> " + resultMap
			schedule3ResultList.add(resultMap)
		}
		
		// sorts the result list by book code then by trans code and credit facility
		Collections.sort(schedule3ResultList, fxForm1DataQueryService.sortCreditFacility)
		Collections.sort(schedule3ResultList, fxForm1DataQueryService.sortTranCode)
		Collections.sort(schedule3ResultList, fxForm1DataQueryService.sortBookCode)
		return schedule3ResultList
	}
	
	public List<Map<String,Object>> getFieldHeaders() {
		List<Map<String,Object>> fieldList = new ArrayList<Map<String,Object>>()
		Map<String,Object> resultMap = new HashMap<String,Object>()
		
		//TO DO:
	}
}
