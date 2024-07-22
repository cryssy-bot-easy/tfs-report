package ucbp.tfs.reports.fxform1

import java.text.SimpleDateFormat
import java.util.List;
import java.util.Map;
import grails.converters.JSON
import org.apache.commons.lang.StringUtils

/***
 * <pre>
 * - program that extract data for fx form 1 schedule 9 report
 * - includes ebc settlement and ebp negotiation
 * </pre>
 * @author Eric Taytayan
 */
class FxForm1Schedule9Service {

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
	 * - program that extracts data from tfs for bsp report fx form 1 schedule 9
	 * - transactions included:
	 * 	- Export Bills Collection Settlement
	 * 	- Export Bills Purchase Negotiation
	 * </pre>
	 * @param onlineReportDate
	 * 	date parameter
	 * @return result map list
	 */
	public List<Map<String, Object>> getFxForm1Schedule9DataList(String onlineReportDate) {
		String getSchedule9Query =
		"""
		SELECT
			VARCHAR(DETAILS) AS DETAILS
		FROM TRADESERVICE
		WHERE
			DOCUMENTTYPE='FOREIGN' AND 
			(
				(DOCUMENTCLASS='BC' AND SERVICETYPE='SETTLEMENT') 
				OR (DOCUMENTCLASS='BP' AND SERVICETYPE='NEGOTIATION')
			)
			AND PAYMENTSTATUS='PAID'
			AND STATUS IN ('APPROVED', 'POST_APPROVED', 'POSTED')
			AND DATE(PROCESSDATE) = '$onlineReportDate'
		"""

		List<Map<String,Object>> schedule9ResultList = new ArrayList<Map<String,Object>>()
		def schedule9DetailsList = reportDbService.getResultFromQuery(getSchedule9Query, null)
		
		String tranCode = ""
		String exporterCbCode = ""
		String exporterName = ""
		String countryCode = ""
		String paymentMode = ""
		String currency = ""
		String bookCode = ""
		String exporterTin = ""
		String country = ""
		BigDecimal amountOrig = 0
		BigDecimal amountDlr = 0
		
		/*
		 * create a loop that parse and validate each key value
		 * that was extracted from query string getSchedule9Query
		 */
		schedule9DetailsList.each {
			Map<String, Object> parsedDetails = JSON.parse(it.toString())
			Map<String, Object> resultMap = new HashMap<String, Object>()
			resultMap.put("BANKCDE", "000470")
			resultMap.put("REFDATE", new SimpleDateFormat("yyyy/MM/dd").format(new Date(parsedDetails.processDate)))
			resultMap.put("FORMNO", "TRD09")
			
			if(parsedDetails.documentClass.equals("BP")) {
				tranCode = "010"
			} else {
				tranCode = "020"
			}
			resultMap.put("TRANCDE", tranCode)
			
			for(Map exporterTempMap : getExporterFromExportBillsTfs(parsedDetails.documentNumber.toString())) {
				exporterCbCode = exporterTempMap.get("EXPORTERCBCODE")
				exporterName = exporterTempMap.get("SELLERNAME")
				countryCode = exporterTempMap.get("COUNTRYCODE")
			}
			
			if(exporterCbCode != null && exporterCbCode != "") {
				exporterCbCode = exporterCbCode
				exporterTin = fxForm1DataQueryService.getTinFromTfs(exporterCbCode)?.trim()
			} else {
				if(fxForm1DataQueryService.getCbCodeTinFromSibs(parsedDetails.cifNumber.toString().trim()).isEmpty()) {
					exporterCbCode = null
					exporterTin = null
				}
				for(Map cbCodeTinTempMap : fxForm1DataQueryService.getCbCodeTinFromSibs(parsedDetails.cifNumber.toString().trim())) {
					exporterCbCode = cbCodeTinTempMap.get("CBCODE")
					exporterTin = cbCodeTinTempMap.get("TIN")
				}
			}
			exporterCbCode = (exporterCbCode==null || exporterCbCode=="null" || exporterCbCode?.trim()=="") ? "0000000000" : StringUtils.leftPad(exporterCbCode.trim(), 10, "0")
			resultMap.put("XPRTCDE", exporterCbCode)
			
			countryCode = StringUtils.leftPad(StringUtils.right(parsedDetails.countryCode ?: "0", 3), 3, "0")
			if(countryCode.matches("^[0-9]*") && !countryCode.equals("000")) {
				countryCode = countryCode
				country = fxForm1DataQueryService.getCountryFromTfs(countryCode)
			} else {
				countryCode = null
				country = ""
			}
			resultMap.put("CTRYCDE", countryCode)
			
			paymentMode = parsedDetails.paymentMode.toString()
			if(paymentMode.equalsIgnoreCase("LC")) {
				paymentMode = "1"
			} else if(paymentMode.equalsIgnoreCase("DP")) {
				paymentMode = "2"
			} else if(paymentMode.equalsIgnoreCase("DA")) {
				paymentMode = "3"
			} else if(paymentMode.equalsIgnoreCase("OA")) {
				paymentMode = "4"
			} else {
				paymentMode = "5"
			}
			resultMap.put("PAYMODE", paymentMode)
			resultMap.put("COMMCDE", "0000000")
			
			currency = parsedDetails.currency.toString().trim()
			if(currency == "PHP") {
				bookCode = "1"
			} else {
				bookCode = "2"
			}
			resultMap.put("BOOKCDE", bookCode)
			resultMap.put("CURRCDE", currency)
			
			amountOrig = new BigDecimal(parsedDetails.amount.toString().replaceAll(",", "") ?: 0)
			if(currency == "USD") {
				amountDlr = amountOrig
			} else if(currency == "PHP") {
				amountDlr = amountOrig / new BigDecimal(parsedDetails.("USD-PHP_urr") ?: 1)
			} else {
				amountDlr = amountOrig * new BigDecimal(parsedDetails.(currency + "-USD_text_special_rate") ?: 0)
			}
			resultMap.put("AMTDOLR", amountDlr)
			
			exporterTin = (exporterTin==null || exporterTin=="null" || exporterTin?.trim()=="") ? StringUtils.rightPad("", 20, " ")
				: fxForm1DataQueryService.formatTinNumber(exporterTin.replaceAll("-", ""), parsedDetails.documentClass.toString())
			resultMap.put("DOCNO", exporterTin)
			
			// for report fields
			resultMap.put("EXPORTERNAME", exporterName.toUpperCase())
			resultMap.put("COUNTRYNAME", country)
			resultMap.put("DOCUMENTNUMER", parsedDetails.documentNumber)
			
			println "schedule9ResultList: >>>>> " + resultMap
			schedule9ResultList.add(resultMap)
			
		}
		
		// sorts the result list by payment mode then by trans code and book code
		Collections.sort(schedule9ResultList, fxForm1DataQueryService.sortBookCode)
		Collections.sort(schedule9ResultList, fxForm1DataQueryService.sortTranCode)
		Collections.sort(schedule9ResultList, fxForm1DataQueryService.sortPayMode)
		return schedule9ResultList
	}
	
	/***
	 * <pre>
	 * method that has select script that extracts
	 * EXPORTERCBCODE, SELLERNAME and COUNTRYCODE from EXPORTBILLS
	 * </pre>
	 * @param documentNumber
	 * @return result map list
	 */
	private List<Map<String, Object>> getExporterFromExportBillsTfs(String documentNumber) {
		String getExporterQueryString =
		"""
		SELECT
			EXPORTERCBCODE,
			SELLERNAME,
			COUNTRYCODE
		FROM EXPORTBILLS
		WHERE
			DOCUMENTNUMBER = '$documentNumber'
		"""	
		
		List<Map<String, Object>> exporterResultList = fxForm1DataQueryService.getTfsResultFromQuery(getExporterQueryString, null)
	}
	
	/***
	 * <pre>
	 * creates a map list for dbf output of the report
	 * that contains the format/structure of the fx form 1 schedule 9
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
		resultMap.put("Name", "XPRTCDE")
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
		resultMap.put("Name", "PAYMODE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 1)
		resultMap.put("Decimal", 0)
		fieldList.add(resultMap)
		
		resultMap = new HashMap<String,Object>()
		resultMap.put("Name", "COMMCDE")
		resultMap.put("Type", 'N')
		resultMap.put("Length", 7)
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