package ucbp.tfs.reports.fxform1

import java.text.SimpleDateFormat
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.hibernate.Query;

/***
 * - contains select scripts for tfs and sibs db
 * @author Eric Taytayan
 */
class FxForm1DataQueryService {

	/***
	 * imports sessionFactory, uses TFS DB
	 */
	def sessionFactory
	
	/***
	 * imports sessionFactory, uses SIBS DB
	 */
	def sessionFactory_silverlake
	
	/***
	 * imports grailsApplication, it handles internal grails application
	 */
	def grailsApplication
	
	/***
	 * <pre>
	 * - get rates from sibs db, either in daily or historical rates: 
	 * if date parameter exist in daily rates table
	 * 	then use daily rates
	 * else
	 * 	use historical rates
	 * </pre>
	 * @param onlineReportDate
	 * 	date parameter
	 * @return map list of rates
	 */
	public List<Map<String,Object>> getRatesToPhpFromSibs(String onlineReportDate) {
		String dailyRatesSchema = ""
		String historicalRatesSchema = ""
		
		// check what is active schema, dev or prod
		if(grailsApplication.config.sibs.rates.schema == "UCPARUCMN1") {
			dailyRatesSchema = "UCPARUCMN.JHFXDTTF "
			historicalRatesSchema = "UCDATUBWC2.JHYXDTTF "
			println "accessing dev schema..... daily >>>>> " + dailyRatesSchema + " historical >>>>> " + historicalRatesSchema
		} else {
			dailyRatesSchema = grailsApplication.config.sibs.rates.schema + ".JHFXDT "
			historicalRatesSchema = grailsApplication.config.sibs.cif.schema + ".JHYXDT "
			println "accessing dev schema..... daily >>>>> " + dailyRatesSchema + " historical >>>>> " + historicalRatesSchema
		}
		// hard coded date 92,314
		// onlineReportDate = '92314'
		onlineReportDate = new SimpleDateFormat("MMddyy").format(new Date(onlineReportDate)).replaceAll("^0", "")
		println "date >>>>> " + onlineReportDate
		
		// daily rates
		String getRatesToPhpSibsFromSibsQuery = "SELECT TRIM(VARCHAR(JFXDCD)) AS CURRENCY, JFXDCR AS RATES FROM " +
		dailyRatesSchema +
		"WHERE JHVDT6='$onlineReportDate' AND JFXDBC='PHP' AND JFXDRN=18"
		
		List<Map<String, Object>> ratesToPhpSibsResultFromQuery = getSibs2ResultFromQuery(getRatesToPhpSibsFromSibsQuery, "")
		println "dailyRates: " + ratesToPhpSibsResultFromQuery.size() + " " + ratesToPhpSibsResultFromQuery
		
		if(ratesToPhpSibsResultFromQuery.size().equals(0)) {
			// historical rates
			getRatesToPhpSibsFromSibsQuery = "SELECT TRIM(VARCHAR(JFXDCD)) AS CURRENCY, JFXDCR AS RATES FROM " +
				historicalRatesSchema +
				"WHERE JFXCD6='$onlineReportDate' AND JFXDBC='PHP' AND JFXDRN=18"
			
			ratesToPhpSibsResultFromQuery = getSibs2ResultFromQuery(getRatesToPhpSibsFromSibsQuery, "")
			println "historicalRates: " + ratesToPhpSibsResultFromQuery.size() + " " + ratesToPhpSibsResultFromQuery
		}

		// create a map where key is currency and value is rates
		List<Map<String, Object>> ratesToPhpSibs = new ArrayList<Map<String, Object>>()
		for(Map<String, Object> ratesToPhpTempMap : ratesToPhpSibsResultFromQuery) {
			ratesToPhpTempMap.put(ratesToPhpTempMap.get("CURRENCY").toString().trim(), ratesToPhpTempMap.get("RATES").toString().trim())
			ratesToPhpSibs.add(ratesToPhpTempMap)
		}
				
		return ratesToPhpSibs
	}
	
	/***
	 * - select query that extracts appraisal/collateral fields from sibs
	 * @param mainCifNumber
	 * @return map list of appraisal/collateral fields in sibs db
	 */
	public List<Map<String,Object>> getAppraisalFromSibs(String mainCifNumber) {
		String getAppraisalFromSibsQuery = "SELECT VARCHAR(COLLATERALS.CCDCID) AS COLLATERALID, VARCHAR(COLLATERALS.CCAPNO) AS MAINCIFNUMBER, " +
		"APPRAISALS.CCDAPV AS APPRAISEDVALUE, APPRAISALS.CCDAP6 AS APPRAISALDATE, VARCHAR(APPRAISALS.CCDCOD) AS COLLATERALTYPE FROM " +
		grailsApplication.config.sibs.loans.schema + ".LNCOLX COLLATERALS INNER JOIN " +
		grailsApplication.config.sibs.cif.schema + ".CFCOLD APPRAISALS ON APPRAISALS.CCDCID=COLLATERALS.CCDCID " +
		"WHERE TRIM(VARCHAR(COLLATERALS.CCAPNO))='$mainCifNumber' ORDER BY CCDAP6 DESC, CCDAPV DESC FETCH FIRST 1 ROWS ONLY"
		
		List<Map<String, Object>> appraisalSibs = getSibs2ResultFromQuery(getAppraisalFromSibsQuery, null)
		
		return appraisalSibs
	}

	/***
	 * - select query that extracts cb code and tin from sibs
	 * @param cifNumber
	 * @return map list of cb code and tin
	 */
	public List<Map<String, Object>> getCbCodeTinFromSibs(String cifNumber) {
		
		String getCbCodeTinFromSibsQuery = "SELECT VARCHAR(BIMPOR) AS CBCODE, VARCHAR(CFTINN) AS TIN FROM " +
			 grailsApplication.config.sibs.cif.schema + ".CFMAST CF " +
			 "LEFT JOIN " + grailsApplication.config.sibs.loans.schema + ".LNSCOD LN ON CF.CFCIF#=LN.BCIF# " +
			 "WHERE BCIF#!='' AND BCIF# IS NOT NULL AND BCIF#='$cifNumber'"
			 /*
			 SELECT VARCHAR(BIMPOR) AS CBCODE, VARCHAR(CFTINN) AS TIN
			 FROM UCDATUBWC1.CFMAST CF LEFT JOIN UCDATULNS1.LNSCOD LN ON CF.CFCIF#=LN.BCIF#
			 "WHERE BCIF#!='' AND BCIF# IS NOT NULL AND BCIF#='$cifNumber'"
			 */
		List<Map<String, Object>> cbCodeTinSibs = getSibs2ResultFromQuery(getCbCodeTinFromSibsQuery, "")
				
		return cbCodeTinSibs
	}
	
	/***
	 * - select query that extracts cif name, cif tin and dosri from sibs
	 * @param cifNumber
	 * @return map list of cif name, cif tin and dosri
	 */
	public List<Map<String, Object>> getCifFromSibs(String cifNumber) {
		String getCifFromSibsQuery = "SELECT (TRIM(VARCHAR(CFNA1)) || ' ' || TRIM(VARCHAR(CFNA1A)) || ' ' || TRIM(VARCHAR(CFNA1B))) AS CIFLONGNAME, " +
			"TRIM(VARCHAR(CFTINN)) AS CIFTIN, TRIM(VARCHAR(CFUIC2)) AS DOSRI FROM " +
			grailsApplication.config.sibs.cif.schema + ".CFMAST " + 
			"WHERE TRIM(VARCHAR(CFCIF#))='$cifNumber'"
		List<Map<String, Object>> cifSibs = getSibs2ResultFromQuery(getCifFromSibsQuery, "")
		
		return cifSibs
	}
	
	/***
	 * - select query that extracts cbcode from sibs
	 * @param cifNumber
	 * @return string
	 */
	public String getCbCodeFromSibs(String cifNumber) {
		
		String getCbCodeFromSibsQuery =	"SELECT VARCHAR(BIMPOR) FROM " + 
			grailsApplication.config.sibs.loans.schema + ".LNSCOD " +
			"WHERE BCIF#!='' AND BCIF# IS NOT NULL AND BCIF#='$cifNumber'"
			/*
			SELECT VARCHAR(BIMPOR) FROM UCDATULNS1.LNSCOD WHERE BCIF#!='' AND BCIF# IS NOT NULL AND BCIF#=''
			*/
		def importerCbCodeSibs = getSibsResultFromQuery(getCbCodeFromSibsQuery, "")
		
		return importerCbCodeSibs
	}
	
	/***
	 * - select query that extracts tin from sibs
	 * @param cbCode
	 * @return string
	 */
	public String getTinFromSibs(String cbCode) { 
		
		String getTinFromSibsQuery = "SELECT VARCHAR(CFTINN) FROM " + 
			 grailsApplication.config.sibs.cif.schema + ".CFMAST CF " +
			 "LEFT JOIN " + grailsApplication.config.sibs.loans.schema + ".LNSCOD LN ON CF.CFCIF#=LN.BCIF# " +
			 "WHERE BIMPOR!='' AND BIMPOR IS NOT NULL AND BIMPOR='$cbCode'"
			 /*
			 SELECT VARCHAR(CFTINN) 
			 FROM UCDATUBWC1.CFMAST CF LEFT JOIN UCDATULNS1.LNSCOD LN ON CF.CFCIF#=LN.BCIF# 
			 WHERE BIMPOR!='' AND BIMPOR IS NOT NULL AND BIMPOR='$cbCode'
			 */
		def tinNumberSibs = getSibsResultFromQuery(getTinFromSibsQuery, "")
				
		return tinNumberSibs
	}
	
	/***  
	 * - select query that extracts industry code from sibs
	 * @param mainCifNumber
	 * @param facilityId
	 * @return string
	 */
	public String getIndustryFromSibs(String mainCifNumber, String facilityId) {
		
		String getCbCodeFromSibsQuery =	"SELECT ECOMCD FROM " +
			grailsApplication.config.sibs.loans.schema + ".LNCBAP " +
			"WHERE FACCD='FCN' AND TRIM(VARCHAR(APPNO))='$mainCifNumber' AND SEQNO='$facilityId'"
			/*
			SELECT * FROM UCDATULNS1.LNCBAP WHERE FACCD='FCN' AND APPNO=${mainCifNumber} AND SEQNO=${facilityId}
			*/
		def industry = getSibsResultFromQuery(getCbCodeFromSibsQuery, "")
		
		return industry
	}
	
	/***
	 * - select query that extracts tin from tfs ref_tfcustmr table
	 * @param cbCode
	 * @return string
	 */
	public String getTinFromTfs(String cbCode) {
		
		String getTinFromTfsQuery =	"SELECT CL_TAN FROM REF_TFCUSTMR WHERE CB_CD='$cbCode'"
		
		List<Map<String, Object>> tinNumberResultFromQuery = getTfsResultFromQuery(getTinFromTfsQuery, "")
		
		for(Map<String, Object> tinNumberTempMap : tinNumberResultFromQuery) {
			return tinNumberTempMap.get("CL_TAN")	
		}	
	}
	
	/***
	 * - select query that extracts country code from tfs
	 * @param country
	 * @return string
	 */
	public String getCountryCodeFromTfs(String country) {
		
		String getCountryCodeFromTfs = "SELECT CNTRY_CD FROM REF_TFCNTRY WHERE CNTRY_NAME='$country'"
		
		List<Map<String, Object>> countryCode = getTfsResultFromQuery(getCountryCodeFromTfs, "")
		
		for(Map<String, Object> countryCodeTempMap : countryCode) {
			return countryCodeTempMap.get("CNTRY_CD")
		}
	}
	
	/***
	 * - select query that extracts country name from tfs
	 * @param countryCode
	 * @return string
	 */
	public String getCountryFromTfs(String countryCode) {
		
		String getCountryFromTfs =	"SELECT CNTRY_NAME FROM REF_TFCNTRY WHERE CNTRY_CD='$countryCode'"
		
		List<Map<String, Object>> country = getTfsResultFromQuery(getCountryFromTfs, "")
		
		for(Map<String, Object> countryTempMap : country) {
			return countryTempMap.get("CNTRY_NAME")
		}
	}
	
	/***
	 * - select query that extracts fields from tfs advance payment table
	 * @param documentNumber
	 * @return map list of advance payment
	 */
	public List<Map<String, Object>> getAdvancePayment(String documentNumber) {
		String getAdvancePaymentQueryString =
		"""
		SELECT
			CREDITFACILITY,
			EXPORTERCBCODE,
			IMPORTERCBCODE,
			VARCHAR_FORMAT(SHIP_DATE, 'YYYY/MM/DD') AS SHIP_DATE,
			ACCT_TYPE,
			CASE
				WHEN EXPORTERNAME!='' AND EXPORTERNAME IS NOT NULL THEN EXPORTERNAME
				ELSE BENEFICIARY_NAME
			END AS DEBTORNAME,
			IMPORTERNAME AS CREDITORNAME
		FROM ADVANCEPAYMENT
		WHERE
			DOCUMENTNUMBER='$documentNumber'
		"""
		
		List advancePaymentResultList = getTfsResultFromQuery(getAdvancePaymentQueryString, "")
		
		return advancePaymentResultList
	}

	/***
	 * - formats tin number 		
	 * @param tinNumber
	 * @param documentClass
	 * @return string
	 */
	// formats tin
	public String formatTinNumber(String tinNumber, String documentClass) {
		tinNumber = StringUtils.rightPad(tinNumber.trim(), 12, "0")
		
		if(documentClass in ["LC", "DP", "DA", "OA", "DR", "BC", "BP", "REBATE", "EXPORT_ADVANCE", ""]) {
			tinNumber = StringUtils.rightPad(tinNumber, 20, " ")
			tinNumber = tinNumber.substring(0, 3) + "-" + tinNumber.substring(3, 6) + "-" + tinNumber.substring(6, 9) + "-" + tinNumber.substring(9, 12)
		} else {
			tinNumber = StringUtils.rightPad(tinNumber, 25, " ")
			tinNumber = tinNumber.substring(0, 3) + "-" + tinNumber.substring(3, 6) + "-" + tinNumber.substring(6, 9) + "-" + tinNumber.substring(9, 12)
		}
		
		return tinNumber
	}
	
	/***
	 * sorts maplist by bookCode
	 */
	public Comparator<Map<String, String>> sortBookCode = new Comparator<Map<String, String>>() {
		public int compare(Map<String, String> m1, Map<String, String> m2) {
			return m1.get("BOOKCDE").compareTo(m2.get("BOOKCDE"));
		}
	}
	
	/***
	 * sorts maplist by tranCode
	 */
	public Comparator<Map<String, String>> sortTranCode = new Comparator<Map<String, String>>() {
		public int compare(Map<String, String> m1, Map<String, String> m2) {
			return m1.get("TRANCDE").compareTo(m2.get("TRANCDE"));
		}
	}
	
	/***
	 * sorts maplist by paymode
	 */
	public Comparator<Map<String, String>> sortPayMode = new Comparator<Map<String, String>>() {
		public int compare(Map<String, String> m1, Map<String, String> m2) {
			return m1.get("PAYMODE").compareTo(m2.get("PAYMODE"));
		}
	}
	
	/***
	 * sorts maplist by statcode
	 */
	public Comparator<Map<String, String>> sortStatCode = new Comparator<Map<String, String>>() {
		public int compare(Map<String, String> m1, Map<String, String> m2) {
			return m1.get("STATCDE").compareTo(m2.get("STATCDE"));
		}
	}
	
	/***
	 * sorts maplist by creditfacility
	 */
	public Comparator<Map<String, String>> sortCreditFacility = new Comparator<Map<String, String>>() {
		public int compare(Map<String, String> m1, Map<String, String> m2) {
			return m1.get("CRFCDE").compareTo(m2.get("CRFCDE"));
		}
	}
	
	/***
	 * - creates a session hibernate of tfs db
	 * @param queryString
	 * @param queryParamString
	 * @return list
	 */
	private List getTfsResultFromQuery(String queryString, String queryParamString){
		if(!queryString) return []
		
		List qryParams = queryParamString?.tokenize('||')
		
		def session = sessionFactory.currentSession
		Query query =  session.createSQLQuery(queryString)
		query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)
		
		
		if(qryParams != null && qryParams.size() > 0){
			qryParams.eachWithIndex {parm ,ctr ->
			query.setString(ctr,parm)
			}
		}
		return query.list()
	}
	
	/***
	 * - creates a session hibernate of sibs db
	 * @param queryString
	 * @param queryParamString
	 * @return string
	 */
	private String getSibsResultFromQuery(String queryString, String queryParamString){
		if(!queryString) return []

		List qryParams = queryParamString?.tokenize('||')
		
		def session = sessionFactory_silverlake.currentSession
		Query query =  session.createSQLQuery(queryString)
		
		if(qryParams != null && qryParams.size() > 0){
			qryParams.eachWithIndex {parm ,ctr ->
			query.setString(ctr,parm)
			}
		}
		
		return query.list()[0].toString()
	}
	
	/***
	 * - creates a session hibernate of sibs db
	 * @param queryString
	 * @param queryParamString
	 * @return list
	 */
	private List getSibs2ResultFromQuery(String queryString, String queryParamString){
		if(!queryString) return []
		
		List qryParams = queryParamString?.tokenize('||')
		
		def session = sessionFactory_silverlake.currentSession
		Query query =  session.createSQLQuery(queryString)
		query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE)
		
		
		if(qryParams != null && qryParams.size() > 0){
			qryParams.eachWithIndex {parm ,ctr ->
			query.setString(ctr,parm)
			}
		}
		return query.list()
	}
}
