package ucbp.tfs.reports.fxform1

import org.apache.commons.lang.StringUtils
import java.math.RoundingMode

/***
 * - generates credex report
 * @author Eric Taytayan
 */
class CredexService {
	
	/***
	 * - imports FxForm1DataQueryService
	 * <br/> - contains select scripts from tfs and sibs db
	 *
	 */
	def fxForm1DataQueryService
	
	/***
	 * - extracts/selects in TFS DB all LCs and BGs with outstanding balance
	 * <br/> - it also extracts fields in sibs db given a parameter coming from tfs
	 * @param onlineReportDate
	 * 	date parameter
	 * @return returns a map list containing all results from tfs and sibs db
	 */
	public List<Map<String, Object>> getCredexDataList(String onlineReportDate) {
		
		// main query that selects in TFS DB all LCs and BGs with outstanding balance.
		// query selects from audit table, some fields has validation by using CASE or NVL function,
		/*
		 * if fields from audit is not empty
		 * 		then select from audit
		 * else 
		 * 		select from main table
		 */
		// some fields that are in SIBS DB were specified as well but they are set in empty string 
		String getCredexQuery =
		"""
		SELECT
			'TF' AS SYSCODE,
			'470' AS BKCODE,
			'$onlineReportDate' AS REFDATE,
			CASE
				WHEN LC.DOCUMENTTYPE='DOMESTIC' AND TP.PRODUCTTYPE='LC' THEN '11'
				WHEN LC.DOCUMENTTYPE='FOREIGN' AND TP.PRODUCTTYPE='LC' THEN '12'
				ELSE ''
			END AS BOOKCDE,
			NVL(TP.LONGNAME, (
				SELECT
					TPMAIN.LONGNAME
					FROM TRADEPRODUCT TPMAIN
				WHERE
					TPMAIN.DOCUMENTNUMBER = TP.DOCUMENTNUMBER
			)) AS ACCTEE,
			NVL(TP.TINNUMBER, (
				SELECT
					TPMAIN.TINNUMBER
					FROM TRADEPRODUCT TPMAIN
				WHERE
					TPMAIN.DOCUMENTNUMBER = TP.DOCUMENTNUMBER
			)) AS TIN,
			'' AS INDUSTRY,
			CASE
				WHEN LC.TYPE='STANDBY' THEN '20'
				WHEN LC.TYPE='REGULAR' THEN '25'
				ELSE ''
			END AS CONTYPE,
			'' AS VALPESO,
			'' AS VALDOL,
			(LC.TOTALNEGOTIATEDAMOUNT + LC.OUTSTANDINGBALANCE) AS VALORIG,
			'' AS AMTPESO,
			'' AS AMTDOLR,
			LC.TOTALNEGOTIATEDAMOUNT AS AMTORIG,
			'' AS UNAVPESO,
			'' AS UNAVDOLR,
			LC.OUTSTANDINGBALANCE AS UNAVORIG,
			0 AS DRVPESO,
			0 AS DRVDOLR,
			0 AS DRVORIG,
			TP.CURRENCY AS CURR,
			'' AS SECURITY,
			'' AS APPRSVAL,
			'' AS APPRSDTE,
			'' AS DOSRIND,
		
			-- for report fields
			LC.LASTTRANSACTION,
			LN.NEGOTIATIONNUMBER AS NEGOREFERENCENUMBER,
			TP.DOCUMENTNUMBER,
			TP.MAINCIFNUMBER,
			TP.CIFNUMBER,
			CASE
				WHEN TP.FACILITYID IS NOT NULL AND TP.FACILITYID <> '' THEN TP.FACILITYID
				ELSE '0'
			END AS FACILITYID,
			TP.PRODUCTTYPE
		FROM (
			SELECT 
				DISTINCT LCA1.DOCUMENTNUMBER,
				RI1.REV AS CURRENT_REVID
			FROM LETTEROFCREDIT_AUDIT LCA1
			LEFT JOIN REVINFO RI1 ON RI1.REV=LCA1.REV_ID
			WHERE RI1.REVTSTMP=(
				SELECT 
					MAX(REVTSTMP) 
				FROM REVINFO RI2 
				LEFT JOIN LETTEROFCREDIT_AUDIT LCA2 ON RI2.REV=LCA2.REV_ID 
				WHERE 
					LCA2.DOCUMENTNUMBER=LCA1.DOCUMENTNUMBER
					AND (TIMESTAMP(DATE('1970-01-01'), TIME('00:00:00')) + (INT(CURRENT TIMEZONE/10000)) HOURS + (REVTSTMP/1000) SECONDS)<='$onlineReportDate'
			)
		) AS CR
		LEFT JOIN LETTEROFCREDIT_AUDIT LC ON LC.REV_ID=CR.CURRENT_REVID
		LEFT JOIN LCNEGOTIATION_AUDIT LN ON LN.REV_ID=CR.CURRENT_REVID
		LEFT JOIN TRADEPRODUCT_AUDIT TP ON TP.REV_ID=CR.CURRENT_REVID
		WHERE
			TP.PRODUCTTYPE='LC'
			AND TP.STATUS IN ('OPEN', 'REINSTATED')
			AND LC.TYPE IN ('REGULAR', 'STANDBY')
			AND LC.OUTSTANDINGBALANCE > 0
			AND DATE(LC.EXPIRYDATE)>='$onlineReportDate'
			-- AND REPLACE(TP.DOCUMENTNUMBER, '-', '') IN ('9090392912006130', '9090392914000099', '9090392914000156', '9090192814000771', '9090192814001027', '9090192914000168', '9090493114001851', 'DM56202014000415', '9090292914000042', '9092192814000134', '9092292914000099')

		UNION ALL
		
		SELECT
			'TF' AS SYSCODE,
			'470' AS BKCODE,
			'$onlineReportDate' AS REFDATE,
			'12' AS BOOKCDE,
			NVL(TP.LONGNAME, (
				SELECT
					TPMAIN.LONGNAME
					FROM TRADEPRODUCT TPMAIN
				WHERE
					TPMAIN.DOCUMENTNUMBER = TP.DOCUMENTNUMBER
			)) AS ACCTEE,
			NVL(TP.TINNUMBER, (
				SELECT
					TPMAIN.TINNUMBER
					FROM TRADEPRODUCT TPMAIN
				WHERE
					TPMAIN.DOCUMENTNUMBER = TP.DOCUMENTNUMBER
			)) AS TIN,
			'' AS INDUSTRY,
			CASE
				WHEN BG.TRANSPORTMEDIUM='SEA' THEN '31'
				WHEN BG.TRANSPORTMEDIUM='AIR' THEN '32'
				ELSE NVL(BG.TRANSPORTMEDIUM, (
					SELECT
						CASE
							WHEN BGMAIN.TRANSPORTMEDIUM='SEA' THEN '31'
							ELSE '32'
						END AS TRANSPORTMEDIUM
						FROM INDEMNITY BGMAIN
					WHERE
						BGMAIN.INDEMNITYNUMBER = BG.INDEMNITYNUMBER
				))
			END AS CONTYPE,
			'' AS VALPESO,
			'' AS VALDOL,
			NVL(BG.SHIPMENTAMOUNT, (
				SELECT
					BGMAIN.SHIPMENTAMOUNT
					FROM INDEMNITY BGMAIN
				WHERE
					BGMAIN.INDEMNITYNUMBER = BG.INDEMNITYNUMBER
			)) AS VALORIG,
			'' AS AMTPESO,
			'' AS AMTDOLR,
			NVL(BG.SHIPMENTAMOUNT, (
				SELECT
					BGMAIN.SHIPMENTAMOUNT
					FROM INDEMNITY BGMAIN
				WHERE
					BGMAIN.INDEMNITYNUMBER = BG.INDEMNITYNUMBER
			)) AS AMTORIG,
			'' AS UNAVPESO,
			'' AS UNAVDOLR,
			0 AS UNAVORIG,
			0 AS DRVPESO,
			0 AS DRVDOLR,
			0 AS DRVORIG,
			TP.CURRENCY AS CURR,
			'' AS SECURITY,
			'' AS APPRSVAL,
			'' AS APPRSDTE,
			'' AS DOSRIND,
		
			-- for report fields
			'FXLC INDEMNITY ISSUANCE' AS LASTTRANSACTION,
			CASE
				WHEN BG.REFERENCENUMBER IS NOT NULL AND BG.REFERENCENUMBER <> '' THEN BG.REFERENCENUMBER
				ELSE (
					SELECT
						BGMAIN.REFERENCENUMBER
					FROM INDEMNITY BGMAIN
					WHERE
						BGMAIN.INDEMNITYNUMBER = BG.INDEMNITYNUMBER
				)
			END AS REFERENCENUMBER,
			TP.DOCUMENTNUMBER,
			TP.MAINCIFNUMBER,
			TP.CIFNUMBER,
			CASE
				WHEN TP.FACILITYID IS NOT NULL AND TP.FACILITYID <> '' THEN TP.FACILITYID
				ELSE '0'
			END AS FACILITYID,
			TP.PRODUCTTYPE
		FROM (
			SELECT 
				DISTINCT IA1.INDEMNITYNUMBER,
				RI1.REV AS CURRENT_REVID
			FROM INDEMNITY_AUDIT IA1
			LEFT JOIN REVINFO RI1 ON RI1.REV=IA1.REV_ID
			WHERE RI1.REVTSTMP=(
				SELECT 
					MAX(REVTSTMP) 
				FROM REVINFO RI2 
				LEFT JOIN INDEMNITY_AUDIT IA2 ON RI2.REV=IA2.REV_ID 
				WHERE 
					IA1.INDEMNITYNUMBER=IA2.INDEMNITYNUMBER
					AND (TIMESTAMP(DATE('1970-01-01'), TIME('00:00:00')) + (INT(CURRENT TIMEZONE/10000)) HOURS + (REVTSTMP/1000) SECONDS)<='$onlineReportDate'
			)		
		) AS CR
		LEFT JOIN INDEMNITY_AUDIT BG ON BG.REV_ID=CR.CURRENT_REVID
		LEFT JOIN TRADEPRODUCT_AUDIT TP ON TP.REV_ID=CR.CURRENT_REVID
		WHERE	
			TP.PRODUCTTYPE='INDEMNITY'
			AND TP.STATUS IN ('OPEN', 'REINSTATED')
			-- AND REPLACE(TP.DOCUMENTNUMBER, '-', '') IN ('9090392912006130', '9090392914000099', '9090392914000156', '9090192814000771', '9090192814001027', '9090192914000168', '9090493114001851', 'DM56202014000415', '9090292914000042', '9092192814000134', '9092292914000099')
		ORDER BY 
			CONTYPE,
			BOOKCDE
		"""
		
		List<Map<String, Object>> cifFromSibsQuery = null
		
		// map list result of rates from SIBS DB with date parameter onlineReportDate
		/*
		 * if date param exist in daily rates table
		 * 		then use daily rates
		 * else
		 * 		use historical rates
		 */
		List<Map<String, Object>> ratesResultFromSibsQuery = fxForm1DataQueryService.getRatesToPhpFromSibs(onlineReportDate)
		List<Map<String, Object>> appraisalFromSibsQuery = null
				
		List<Map<String, Object>> credexResultList = new ArrayList<Map<String, Object>>()
		
		// map list result of the getCredexQuery string above
		List<Map<String, Object>> credexResultFromQuery = fxForm1DataQueryService.getTfsResultFromQuery(getCredexQuery, null)
		
		String acctee = ""
		String tin = ""
		String checkTin = ""
		String industry = ""
		String bookCode = ""
		String conType = ""
		String security = ""
		BigDecimal appraisalValue = 0
		String appraisalDate = ""
		String dosrind = ""
		String currency = ""
		
		BigDecimal rates = 0
		BigDecimal usdToPhpRate = 0
		
		BigDecimal valOrig = 0
		BigDecimal unavOrig = 0
		BigDecimal amtOrig = 0
		
		BigDecimal unavDolr = 0
		BigDecimal amtDolr = 0
		BigDecimal valDolr = 0
		
		BigDecimal unavPeso = 0
		BigDecimal amtPeso = 0
		BigDecimal valPeso = 0
		
		BigDecimal lcTransaction = 0
		BigDecimal bgTransaction = 0
		BigDecimal totalTransaction = 0
		
		// creates a map list to validate empty fields to provide values
		// empty SIBS fields from main query were validated and 
		// given values in this map list by extracting select snippets in fxForm1DataQueryService 
		for(Map<String, Object> credexTempMap : credexResultFromQuery) {
			
			cifFromSibsQuery = fxForm1DataQueryService.getCifFromSibs(credexTempMap.get("CIFNUMBER").toString().trim())
			
			if(!cifFromSibsQuery.isEmpty()) {
				for(Map<String, Object> cifSibsTempMap : cifFromSibsQuery) {
					if(credexTempMap.get("ACCTEE") != null && credexTempMap.get("ACCTEE") != "") {
						acctee = credexTempMap.get("ACCTEE").toString().trim()
					} else {
						acctee = cifSibsTempMap.get("CIFLONGNAME").toString().trim()
					}
					
					if(credexTempMap.get("TIN") != null && credexTempMap.get("TIN") != "") {
						tin = credexTempMap.get("TIN").toString().trim()
					} else {
						tin = cifSibsTempMap.get("CIFTIN").toString().trim()
					}					
					dosrind = cifSibsTempMap.get("DOSRI").toString().trim()
				}			
			} else {
				acctee = credexTempMap.get("ACCTEE").toString().trim()
				tin = credexTempMap.get("TIN").toString().trim()
				dosrind = ""
			}
			credexTempMap.put("ACCTEE", acctee)
			
			tin = tin.replaceAll("-", "")
			// tin = StringUtils.rightPad(tin, 12, "0")
			// tin = tin.substring(0, 3) + "-" + tin.substring(3, 6) + "-" + tin.substring(6, 9) + "-" + tin.substring(9, 12)			
			credexTempMap.put("TIN", (tin != null && tin != "null" && tin != "") ? tin : "")
			
			bookCode = credexTempMap.get("BOOKCDE").toString().trim()
			conType = credexTempMap.get("CONTYPE").toString().trim()
			if (bookCode.equals("11") && conType.equals("20")) {
				if (dosrind != null && !dosrind.trim().isEmpty()) {
					dosrind = "1"
				} else {
					dosrind = "2"
				}
			} else {
				dosrind = ""
			}
			credexTempMap.put("DOSRIND", dosrind)
			
			industry = fxForm1DataQueryService.getIndustryFromSibs(credexTempMap.get("MAINCIFNUMBER").toString().trim(), credexTempMap.get("FACILITYID").toString().trim())
			credexTempMap.put("INDUSTRY", industry.equals("null") ? "0" : StringUtils.leftPad(industry.trim(), 5, '0'))
						
			currency = credexTempMap.get("CURR").toString().trim()
			valOrig = new BigDecimal(credexTempMap.get("VALORIG").toString().trim())
			amtOrig = new BigDecimal(credexTempMap.get("AMTORIG").toString().trim())
			unavOrig = new BigDecimal(credexTempMap.get("UNAVORIG").toString().trim())
			
			for(Map<String, Object> ratesTempMap : ratesResultFromSibsQuery) {
				if(ratesTempMap.get("CURRENCY").toString().trim().equals(currency)) {
					rates = new BigDecimal(ratesTempMap.get(currency))
				}
				if(ratesTempMap.get("CURRENCY").toString().trim().equals("USD")) {
					usdToPhpRate = new BigDecimal(ratesTempMap.get("USD"))
				}
			}
			
			if(currency == "USD") {
				unavDolr = unavOrig.setScale(2, RoundingMode.HALF_UP)
				amtDolr = amtOrig.setScale(2, RoundingMode.HALF_UP)
				unavPeso = (unavOrig * rates).setScale(2, RoundingMode.HALF_UP)
				amtPeso = (amtOrig * rates).setScale(2, RoundingMode.HALF_UP)
			} else if(currency == "PHP") {
				unavDolr = (unavOrig / usdToPhpRate).setScale(2, RoundingMode.HALF_UP)
				amtDolr = (amtOrig / usdToPhpRate).setScale(2, RoundingMode.HALF_UP)
				unavPeso = unavOrig.setScale(2, RoundingMode.HALF_UP)
				amtPeso = amtOrig.setScale(2, RoundingMode.HALF_UP)
			} else {
				unavDolr = ((unavOrig * rates) / usdToPhpRate).setScale(2, RoundingMode.HALF_UP)
				amtDolr = ((amtOrig * rates) / usdToPhpRate).setScale(2, RoundingMode.HALF_UP)
				unavPeso = (unavOrig * rates).setScale(2, RoundingMode.HALF_UP)
				amtPeso = (amtOrig * rates).setScale(2, RoundingMode.HALF_UP)
			}
			
			valDolr = unavDolr + amtDolr
			valPeso = unavPeso + amtPeso
			
			credexTempMap.put("UNAVDOLR", unavDolr)
			credexTempMap.put("AMTDOLR", amtDolr)
			credexTempMap.put("VALDOL", valDolr)
			credexTempMap.put("UNAVPESO", unavPeso)
			credexTempMap.put("AMTPESO", amtPeso)
			credexTempMap.put("VALPESO", valPeso)
			
			appraisalFromSibsQuery = fxForm1DataQueryService.getAppraisalFromSibs(credexTempMap.get("MAINCIFNUMBER").toString().trim())
			if(appraisalFromSibsQuery.size() == 0) {
				security = "0"
				appraisalValue = 0
				appraisalDate = "0"
			}else {
				for(Map<String, Object> appraisalTempMap : appraisalFromSibsQuery) {
					security = appraisalTempMap.get("COLLATERALTYPE").toString().trim()
					if (security.equals("001") || security.equals("002") || security.equals("003") ||
						security.equals("01") || security.equals("02") || security.equals("03") ||
						security.equals("1") || security.equals("2") || security.equals("3")) {
						security = "22"
					} else if (security.equals("004") || security.equals("04") || security.equals("4")) {
						security = "21"
					} else {
						security = "29"
					}
					
					if (security.equalsIgnoreCase("29")) {
						appraisalValue = new BigDecimal(appraisalTempMap.get("APPRAISEDVALUE").toString().trim())
						appraisalDate = appraisalTempMap.get("APPRAISALDATE").toString().trim()
					} else {
						appraisalValue = 0
						appraisalDate = "0"
					}
				}
			}
			
			credexTempMap.put("SECURITY", security)
			credexTempMap.put("APPRSVAL", appraisalValue)
			credexTempMap.put("APPRSDTE", appraisalDate)

			// provides transaction counts
			if(credexTempMap.get("PRODUCTTYPE").toString().trim().equalsIgnoreCase("LC")) {
				lcTransaction++
			} else if(credexTempMap.get("PRODUCTTYPE").toString().trim().equalsIgnoreCase("INDEMNITY")) {
				bgTransaction++
			}
			totalTransaction++
			credexTempMap.put("LCTRANSACTION", lcTransaction)
			credexTempMap.put("BGTRANSACTION", bgTransaction)
			credexTempMap.put("TOTALTRANSACTION", totalTransaction)
			
			println "credexResultList: " + credexTempMap
			credexResultList.add(credexTempMap)
		}
		
		return credexResultList
	}

}
