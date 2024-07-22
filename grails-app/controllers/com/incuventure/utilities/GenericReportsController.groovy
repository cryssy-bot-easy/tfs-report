package com.incuventure.utilities
import grails.converters.JSON
import net.sf.jasperreports.engine.JasperExportManager
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.plugins.jasper.JasperExportFormat
import org.codehaus.groovy.grails.plugins.jasper.JasperReportDef
import org.springframework.util.Assert
/**
 *
 * @author Giancarlo Angulo <gian.angulo@incuventure.net>
 *
 */
import net.ipc.utils.DateUtils
import net.ipc.utils.JsonUtil
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import com.incuventure.dto.DocumentsEnclosedDTO
import com.incuventure.dto.InstructionsDTO
import org.apache.commons.lang.WordUtils
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/*import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;*/

//import com.ucpb.tfs.application.command.PrintReportCommand

/*	PROLOGUE:
 * 	(revision)
	SCR/ER Number: IBD-15-0828-01
	SCR/ER Description: Comparison of Balances in DW and SIBS-GL
	[Revised by:] Jesse James Joson
	[Date revised:] 09/17/2015
	Program [Revision] Details: add new methods that generate the reports
	INPUT: runDailyMasterGLSummary and  runDailyMasterGLDailyBalanceSummary
	OUTPUT: Daily_Master_GL_Summary.xls & Daily_Master_GL_DailyBalance_Summary.xls
	PROCESS: generate the reports
 */


class GenericReportsController {

	
	
	def jasperService
	def commandBusService
	def reportDirectory
	def incuventureJasperService
	def reportDataQueryService
	def sessionFactory
	def reportIdService
	def debitMemoReportDataService
	def silverlakeService
	def genericReportsQueryService
	def servletContext
	def ratesService
	def batchGeneratorService
	def grailsApplication
	def tfsMcoReportService
	def actualCorresChargeService
	def fxForm1Schedule3Service
	def fxForm1Schedule4Service
	def fxForm1Schedule5Service
	def fxForm1Schedule7Service
	def fxForm1Schedule9Service
	def fxForm1Schedule10Service
	def fxForm1Schedule11Service
	def fxForm1DbfWriterService
	def batchDebitMemoService
	def credexService
//	def tfsMcoReportData_BACKUPService
	
    private static ArrayList<String> successList = new ArrayList<String>();
	
	def runConsolidatedReportDailyCollectionsExportDocumentaryStampFees = {

		def token = params.token ?: 'Consolidated_Report_On_Daily_Collections_Of_Export_Documentary_Stamp_Fees'
		
		if(params.consolidatedReportType == 'remittance') {
			params['P_REPORTNAME'] = "Consolidated_Report_On_Daily_Collections_Of_Export_Documentary_Stamp_Fees"
			params['specificRptPath'] = 'BATCHES/Other Imports'
			params['dateOfRemittancePopup'] = params.onlineReportDate
			params['reportId'] = reportIdService.generateReportId("CCDSC")
		} else {
			params['P_REPORTNAME'] = "Consolidated_Report_On_Daily_Collections_Of_Export_Documentary_Stamp_Fees_2"
			params['specificRptPath'] = 'BATCHES/Other Imports'
			params['dateOfCollectionPopup'] = params.onlineReportDate
			Date nextBusinessDate = getNextBusinessDay(params.onlineReportDate)
			params['remittanceDateDueOn'] = new SimpleDateFormat("MM/dd/yyyy").format(nextBusinessDate)
			params['reportId'] = reportIdService.generateReportId("CCDEC")
			token = 'Consolidated_Report_On_Daily_Collections_Of_Export_Documentary_Stamp_Fees_2'
		}
		params['format'] = "pdf"
		
		println "ONLINE REPORT DATE:"+params.onlineReportDate
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runConsolidatedDailyReportDepositsCollected = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Consolidated_Daily_Report_on_Deposits_Collected'

		if(params.consolidatedReportType == 'remittance') {
			params['P_REPORTNAME'] = "Consolidated_Daily_Report_on_Deposits_Collected"
			params['specificRptPath'] = 'BATCHES/Other Imports'
			params['dateOfRemittancePopup'] = params.onlineReportDate
			
			params['reportId'] = reportIdService.generateReportId("CRDCR")
		} else {
			params['P_REPORTNAME'] = "Consolidated_Daily_Report_on_Deposits_Collected_2"
			params['specificRptPath'] = 'BATCHES/Other Imports'
			params['dateOfCollectionPopup'] = params.onlineReportDate
			Date nextBusinessDate = getNextBusinessDay(params.onlineReportDate)
			params['remittanceDateDueOn'] = new SimpleDateFormat("MM/dd/yyyy").format(nextBusinessDate)
			params['reportId'] = reportIdService.generateReportId("CRDCC")
			token = 'Consolidated_Daily_Report_on_Deposits_Collected_2'
		}
		params['format'] = "pdf"

		println "ONLINE REPORT DATE:"+params.onlineReportDate
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runConsolidatedReportDaliyCollectionsCdtOtherLevies = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Consolidated_Report_on_Daily_Collections_of_Customs_Duties_Taxes_and_Other_Levies'

		if(params.consolidatedReportType == 'remittance') {
			params['P_REPORTNAME'] = "Consolidated_Report_on_Daily_Collections_of_Customs_Duties_Taxes_and_Other_Levies"
			params['specificRptPath'] = 'BATCHES/Other Imports'
			params['dateOfRemittancePopup'] = params.onlineReportDate
			params['reportId'] = reportIdService.generateReportId("CCDTC")
		} else {
			params['P_REPORTNAME'] = "Consolidated_Report_on_Daily_Collections_of_Customs_Duties_Taxes_and_Other_Levies_2"
			params['specificRptPath'] = 'BATCHES/Other Imports'
			params['dateOfCollectionPopup'] = params.onlineReportDate
			Date nextBusinessDate = getNextBusinessDay(params.onlineReportDate)
			params['remittanceDateDueOn'] = new SimpleDateFormat("MM/dd/yyyy").format(nextBusinessDate)
			params['reportId'] = reportIdService.generateReportId("CCDCC")
			token =  'Consolidated_Report_on_Daily_Collections_of_Customs_Duties_Taxes_and_Other_Levies_2'
		}
		params['format'] = "pdf"

		println "ONLINE REPORT DATE:"+params.onlineReportDate
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runConsolidatedReportDailyCollectionsImportProcessingFees = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Consolidated_Report_on_Daily_Collections_of_Import_Processing_Fees'

		if(params.consolidatedReportType == 'remittance') {
			params['P_REPORTNAME'] = "Consolidated_Report_on_Daily_Collections_of_Import_Processing_Fees"
			params['specificRptPath'] = 'BATCHES/Other Imports'
			params['dateOfRemittancePopup'] = params.onlineReportDate
			params['reportId'] = reportIdService.generateReportId("CCIPC")
		} else {
			params['P_REPORTNAME'] = "Consolidated_Report_on_Daily_Collections_of_Import_Processing_Fees_2"
			params['specificRptPath'] = 'BATCHES/Other Imports'
			params['dateOfCollectionPopup'] = params.onlineReportDate
			Date nextBusinessDate = getNextBusinessDay(params.onlineReportDate)
			params['remittanceDateDueOn'] = new SimpleDateFormat("MM/dd/yyyy").format(nextBusinessDate)
			params['reportId'] = reportIdService.generateReportId("CCDIC")
			token =  'Consolidated_Report_on_Daily_Collections_of_Import_Processing_Fees_2'
		}
		params['format'] = "pdf"

		println "ONLINE REPORT DATE:"+params.onlineReportDate
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runConsolidatedReportDailyCollectionsImportExport = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Consolidated_Report_on_Daily_Collections_of_Customs_Duties_Taxes_and_Other_Levies_4'

		params['P_REPORTNAME'] = "Consolidated_Report_on_Daily_Collections_of_Customs_Duties_Taxes_and_Other_Levies_4"
		params['specificRptPath'] = 'BATCHES/Other Imports'
		params['dateOfRemittancePopup'] = params.onlineReportDate
		params['reportId'] = reportIdService.generateReportId("CCIER")
		params['format'] = "pdf"

		println "ONLINE REPORT DATE:"+params.onlineReportDate
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runConsolidatedReportDailyCollectionsFinalAdvance = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Consolidated_Report_on_Daily_Collections_of_Customs_Duties_Taxes_and_Other_Levies_5'

		params['P_REPORTNAME'] = "Consolidated_Report_on_Daily_Collections_of_Customs_Duties_Taxes_and_Other_Levies_5"
		params['specificRptPath'] = 'BATCHES/Other Imports'
		params['dateOfRemittancePopup'] = params.onlineReportDate
		params['reportId'] = reportIdService.generateReportId("CCFAR")
		params['format'] = "pdf"

		println "ONLINE REPORT DATE:"+params.onlineReportDate
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
		
	}

	def runConsolidatedReportDailyCollectionsAll = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Consolidated_Report_on_Daily_Collections_of_Customs_Duties_Taxes_and_Other_Levies_3'

		params['P_REPORTNAME'] = "Consolidated_Report_on_Daily_Collections_of_Customs_Duties_Taxes_and_Other_Levies_3"
		params['specificRptPath'] = 'BATCHES/Other Imports'
		params['dateOfRemittancePopup'] = params.onlineReportDate
		params['reportId'] = reportIdService.generateReportId("CCALL")
		params['format'] = "pdf"
		
		println "ONLINE REPORT DATE:"+params.onlineReportDate
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	
	def runCustomsDutiesAndTaxesAndOtherLevies = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'Customs_Duties_And_Taxes_And_Other_Levies'

		params['P_REPORTNAME'] = "Customs_Duties_And_Taxes_And_Other_Levies"
		params['specificRptPath'] = 'BATCHES/Other Imports'
		params['reportId'] = reportIdService.generateReportId("MCDTL")
		Date onlineReportDate = params.onlineReportDate ? new Date(params.onlineReportDate) : new Date()
		params['currentDate'] = onlineReportDate
		 params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = "pdf"
		
		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		params['brUnitCode'] = params.branchUnitCode
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	
	def runYtdCustomsDutiesAndTaxesAndOtherLevies = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'YTD_Customs_Duties_And_Taxes_And_Other_Levies'

		params['P_REPORTNAME'] = "YTD_Customs_Duties_And_Taxes_And_Other_Levies"
		params['specificRptPath'] = 'BATCHES/Other Imports'       //Follow the '/' format
		params['reportId'] = reportIdService.generateReportId("YCDTL")
		Date onlineReportDate = params.onlineReportDate ? new Date(params.onlineReportDate) : new Date()
		params['currentDate'] = onlineReportDate
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = "pdf"
		
		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		params['brUnitCode'] = params.branchUnitCode
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runDailyFxlcOpenedReportCdtDetails = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Daily_Foreign_LCs_Opened_Report_with_CDT_Details'

		params['P_REPORTNAME'] = "Daily_Foreign_LCs_Opened_Report_with_CDT_Details"
		params['specificRptPath'] = 'BATCHES/Other Imports'       //Follow the '/' format
		params['asOfDate'] = params.onlineReportDate
		params['reportId'] = reportIdService.generateReportId("DFCDT")
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runDailyReportProcessedCdtCollections = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Daily_Report_on_Processed_CDT_Collections'

		params['P_REPORTNAME'] = "Daily_Report_on_Processed_CDT_Collections"
		params['specificRptPath'] = 'BATCHES/Other Imports'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("DRPCC")
		params['reportDate'] = params.onlineReportDate
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runDailyReportProcessedRefunds = {		//for Daily Report on Processed Refunds generated from accordion

		def token = params.token ?: 'Daily_Report_on_Processed_Refund'

		params['P_REPORTNAME'] = "Daily_Report_on_Processed_Refund"
		params['specificRptPath'] = 'BATCHES/Other Imports'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("DRPRR")
		params['dateOfTransactionTo'] = params.dateOfTransactionTo
		params['dateOfTransactionFrom'] = params.dateOfTransactionFrom
		//params['dateGenerated'] = params.dateOfTransactionFrom
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	
	def runDailyReportProcessedRefundsBatch = {		//for Daily Report on Processed Refunds generated from batch user
		
		def token = params.token ?: 'Daily_Report_on_Processed_Refund'

		params['P_REPORTNAME'] = "Daily_Report_on_Processed_Refund_2"
		params['specificRptPath'] = 'BATCHES/Other Imports'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("DRPRR")
		params['dateGenerated'] = params.onlineReportDate
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	
	//Daily Master vs GL
	def runDailyMasterGLSummary={
		def token = params.token ?: 'Daily_Master_GL_Summary'

		params['P_REPORTNAME'] = 'Daily_Master_GL_Summary'

		params['specificRptPath'] = ''       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("DCCDO")
		
		String newAppDate = params.onlineReportDate
		if(newAppDate== null) {
			newAppDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date())
		}
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy")
		Date startDate = null
		
		try {
			startDate = df.parse(newAppDate);
			String newDateString = df.format(startDate)
			//System.out.println(newDateString);
		} catch (ParseException e) {
			System.out.println(">>>>>Error Parsing Date<<<<<")
			e.printStackTrace()
		}
			
		Calendar cal = Calendar.getInstance()
		cal.setTime(startDate)
		cal.add(Calendar.DATE, -1)
		Date dateBefore = cal.getTime()
		
		newAppDate = df.format(dateBefore)
		
		params['dateGenerated'] = newAppDate
		
		newAppDate= newAppDate.substring(3, 5)
		
		
		
		params['day'] = newAppDate
		params['format'] = "xls"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	
	//Daily Master vs GL vs Daily Balance
	def runDailyMasterGLDailyBalanceSummary={
		def token = params.token ?: 'Daily_Master_GL_DailyBalance_Summary'

		params['P_REPORTNAME'] = 'Daily_Master_GL_DailyBalance_Summary'

		params['specificRptPath'] = ''       //Follow the '/' format
		
		String newAppDate = params.onlineReportDate
		if(newAppDate== null) {
			newAppDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date())
		}
		
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy")
		Date startDate = null
		
		try {
			startDate = df.parse(newAppDate);
			String newDateString = df.format(startDate)
			//System.out.println(newDateString);
		} catch (ParseException e) {
			System.out.println(">>>>>Error Parsing Date<<<<<")
			e.printStackTrace()
		}
			
		Calendar cal = Calendar.getInstance()
		cal.setTime(startDate)
		cal.add(Calendar.DATE, -1)
		Date dateBefore = cal.getTime()
		
		newAppDate = df.format(dateBefore)
		
		params['dateGenerated'] = newAppDate
		System.out.println(newAppDate)
		
		newAppDate= newAppDate.substring(3, 5)
		
		params['day'] = newAppDate

		params['reportId'] = reportIdService.generateReportId("DCCDO")
		
		params['format'] = "xls"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	

	//
	//

	def runWeeklyScheduleDocStamps = {		//for Reports with no Fields Data from DataBase

		def token

		switch(params.reportType){
			case "viewWeeklyScheduleDocStampsTR":
				token = params.token ?: 'Weekly_Schedule_of_Doc_Stamps_TR'
				params['reportId'] = reportIdService.generateReportId("WDSTR")
				break;
			case "viewWeeklyScheduleDocStamps108":
				token = params.token ?: 'Weekly_Schedule_of_Doc_Stamps_108'
				params['reportId'] = reportIdService.generateReportId("WD108")
				break;
			case "viewWeeklyScheduleDocStamps113":
				token = params.token ?: 'Weekly_Schedule_of_Doc_Stamps_113'
				params['reportId'] = reportIdService.generateReportId("WD113")
				break;
		}


		params['P_REPORTNAME'] = token
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format
		params['currentWeek'] = new SimpleDateFormat("ww").format(new Date())
		params['currentMonth'] = new SimpleDateFormat("M").format(new Date())
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date())
		params['format'] = "csv"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runListOfTransactionsWithNoCifNumber = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'List_of_Transactions_With_No_CIF_Number'

		params['P_REPORTNAME'] = "List of Transactions With No CIF Number"
		params['forMonth']=new SimpleDateFormat("MMMMM").format(new Date(params.onlineReportDate)).toUpperCase();
		params['forYear']=new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate));
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MLNCF")
		params['currentMonth'] = new SimpleDateFormat("M").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['brUnitCode'] = params.branchUnitCode
		params['format'] = "xls"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runProfitabilityMonitoringReport = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Profitability_Monitoring_Report'

		if(params.profitMonitoringReportType == "notException") {
			params['P_REPORTNAME'] = "Profitability_Monitoring_Report"
			params['reportId'] = reportIdService.generateReportId("MPMRE")
		} else {
			params['P_REPORTNAME'] = "Profitability_Monitoring_Exception_Report"
			params['reportId'] = reportIdService.generateReportId("MPMEX")
			token = 'Profitability_Monitoring_Exception_Report'
		}
		params['forMonth']=new SimpleDateFormat("MMMMM").format(new Date(params.onlineReportDate)).toUpperCase();
		params['forYear']=new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate));
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format

		params['brUnitCode'] = params.branchUnitCode
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['dateGenerated'] = params.onlineReportDate
		params['format'] = "xls"
		//params['currentDate'] = new SimpleDateFormat("MM-dd-yyyy").format(new Date())
		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runMonthlyTransactionCountReport = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Monthly_Transaction_Count'

		params['P_REPORTNAME'] = "Monthly_Transaction_Count"
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MMTCT")
		params['reportDate'] = params.onlineReportDate
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runTfsCasaPostingReport = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'TFS_CASA_Posting_Report'

		if(params.tellerID == 'ALL') {
			params['P_REPORTNAME'] = "TFS_CASA_Posting_Report_All"
			token =  'TFS_CASA_Posting_Report_All'
		} else {
			params['P_REPORTNAME'] = "TFS_CASA_Posting_Report"
		}
		
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format
		params['dateGenerated'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['tellerID'] = params.tellerID
		params['reportId'] = reportIdService.generateReportId("ATCPR")
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runYtdTransactionCountImportExportReport = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'YTD_Transaction_Count_Import_and_Export'

		params['P_REPORTNAME'] = "YTD_Transaction_Count_Import_and_Export"
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("YYTCT")
		params['reportDate'] = params.onlineReportDate
		params['currentMonth'] = new SimpleDateFormat("M").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runCollectedTwoPercentCwt = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Collected_2%_CWT'


		params['P_REPORTNAME'] = token
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format
		params['forYear']=new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate));
		params['forMonth']=new SimpleDateFormat("MMMMM").format(new Date(params.onlineReportDate)).toUpperCase();
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))

		params['reportId'] = reportIdService.generateReportId("MCTPC")
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runProductAvailmentsReport = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Product_Availments_Report'

		if(params.productAvailReportType == "notException") {
			params['P_REPORTNAME'] = "Product_Availments_Report"
			params['reportId'] = reportIdService.generateReportId("MPARN")
		} else {
			params['P_REPORTNAME'] = "Product_Availments_Exception_Report"
			params['reportId'] = reportIdService.generateReportId("MPAEE")
			token = 'Product_Availments_Exception_Report'
		}
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format
		
		params['currentDate'] = new SimpleDateFormat("MM-dd-yyyy").format(new Date(params.onlineReportDate))
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['forMonth'] = new SimpleDateFormat("MMMMM").format(new Date(params.onlineReportDate))
		params['forYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))

		params['format'] = "xls"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runDwExceptionReport = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'DW_Exception_Report'

		params['P_REPORTNAME'] = "DW_Exception_Report"
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MDWER")
		params['format'] = "xls"
		params['currentDate'] = new SimpleDateFormat("MM-dd-yyyy").format(new Date())

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		params['brUnitCode'] = params.branchUnitCode
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//ARVIN
	//Outstanding_Domestic_Cash_LCs_Per_Currency
	def runOutstandingDomesticCashLcPerCurrency={
		def token = params.token ?: 'Outstanding_Domestic_Cash_LCs_Per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Domestic_Cash_LCs_Per_Currency'
		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MDCAC")
		params['asOfDate'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println "REPORT ID: "+params['reportId']
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding_Foreign_Cash_LCs_Per_Currency
	def runOutstandingForeignCashLcPerCurrency={
		def token = params.token ?: 'Outstanding_Foreign_Cash_LCs_per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Foreign_Cash_LCs_per_Currency'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] =  reportIdService.generateReportId("MFCAC")
		params['date'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println "REPORT ID: "+params['reportId']
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Domestic Cash LC Per Importer
	def runOutstandingDomesticCashLcPerImporter={
		def token = params.token ?: 'Outstanding_Domestic_Cash_LCs_Per_Importer'

		params['P_REPORTNAME'] = 'Outstanding_Domestic_Cash_LCs_Per_Importer'

		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MDMCI")
		println "REPORT ID: "+params['reportId']
		params['asOfDate'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println "REPORT ID: "+params['reportId']
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		//List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		//println "reportDataList: "+reportDataList

		generateGenericReport([], ext_params["format"], ext_params)
	}

	//Outstanding Foreign Cash LC Per Importer
	def runOutstandingForeignCashLcPerImporter={
		def token = params.token ?: 'Outstanding_Foreign_Cash_LCs_per_Importer2'
		
				params['P_REPORTNAME'] = 'Outstanding_Foreign_Cash_LCs_per_Importer2'
		
				params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format
		
				params['reportId'] = reportIdService.generateReportId("MFXCI")
				println "REPORT ID: "+params['reportId']
				params['asOfDate'] = params.onlineReportDate
				params['brUnitCode'] = params.branchUnitCode
				params['brUnitName'] = params.branchUnitName
				params['format'] = "pdf"
		
				println "REPORT ID: "+params['reportId']
				println "params: "+params
		
				String fieldsEmptyString = params['fieldsEmpty']
				Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
		
		
				def ext_params = [:]
				ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
				ext_params['userId'] = params['userId']?:"123456"
				ext_params['name'] = params['name']?:"giancarlo"
				ext_params['schconid'] = params['schconid']?:"angulo"
				ext_params['rptPath'] = 'reports/'
				ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		
				ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"
		
				ext_params['token'] = token
		
				def pkeyset = params.keySet()
				pkeyset.each{ kkey ->
					ext_params[kkey] = params[kkey]
				}
		
				println "ext_params['rptPath']: "+ext_params['rptPath']
				println "ext_params: "+ext_params
		
				//List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
				//println "reportDataList: "+reportDataList
		
				generateGenericReport([], ext_params["format"], ext_params)
	}

	//Outstanding Domestic Standby LC Per Currency
	def runOutstandingDomesticStandbyLcPerCurrency={
		def token = params.token ?: 'Outstanding_Domestic_Standby_LCs_Per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Domestic_Standby_LCs_Per_Currency'

		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] =  reportIdService.generateReportId("MDSTC")
		println "REPORT ID: "+params['reportId']
		params['asOfDate'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Foreign Standby LC Per Currency
	def runOutstandingForeignStandbyLcPerCurrency={
		def token = params.token ?: 'Outstanding_Foreign_Standby_LCs_per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Foreign_Standby_LCs_per_Currency'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MFSTC")
		println "REPORT ID: "+params['reportId']
		params['date'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Domestic Standby LC Per Importer
	def runOutstandingDomesticStandbyLcPerImporter={
		def token = params.token ?: 'Outstanding_Domestic_Standby_LCs_Per_Importer'

		params['P_REPORTNAME'] = 'Outstanding_Domestic_Standby_LCs_Per_Importer'

		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MDMSI")
		println "REPORT ID: "+params['reportId']
		params['asOfDate'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token
		//used for reports containing subreports
		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Foreign Standby LC Per Importer
	def runOutstandingForeignStandbyLcPerImporter={
		def token = params.token ?: 'Outstanding_Foreign_Standby_LCs_per_Importer'

		params['P_REPORTNAME'] = 'Outstanding_Foreign_Standby_LCs_per_Importer'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MFXSI")
		println "REPORT ID: "+params['reportId']
		params['asOfDate'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token
		//used for reports containing subreports
		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runExportNegotiationAdviceEbcFxLcNonLcNegotiation={
		def token = params.token ?: 'Export_Negotiation_Advice_EBC_FX_LC_Non_LC_Negotiation'

		params['P_REPORTNAME'] = "Export_Negotiation_Advice_EBC_FX_LC_Non_LC_Negotiation"

		params['specificRptPath'] = 'EXPORTS/Advise on Export - FXLC Cancellation'       //Follow the '/' format

		params['refNo'] = 'Reference Number'
		params['issuingCollectingBank'] = 'Issuing Collecting Bank'				  //for Decimalsparams['amount'] = '10000'
		params['issuingCollectingBankAddress'] = 'Issuing Collecting Bank Address'
		params['lcNumber'] = 'LC Number'
		params['issuedBy'] = 'Issued By'
		params['draftAmount'] = new BigDecimal('2000');
		params['drawer'] = 'Drawer'
		params['drawee'] = 'Drawee'
		params['usanceTenorTerm'] = 'Usance Tenor Term'
		params['instructions']='Instructions'
		params['authorizedSignatory']='Authorized Signatory'
		params['signaturePortion']='Signature Portion'


		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
		}

		render toBeRendered as JSON
	}

	def runDbcNegotiationAdviceToIssuingBank={
		def token = params.token ?: 'DBC_Negotiation_Advice_to_Issuing_Bank'

		params['P_REPORTNAME'] = "DBC_Negotiation_Advice_to_Issuing_Bank"

		params['specificRptPath'] = 'EXPORTS/DBC-OLB LC Negotiation'       //Follow the '/' format

		params['refNo'] = 'Reference Number'
		params['issuingCollectingBank'] = 'Issuing Collecting Bank'				  //for Decimalsparams['amount'] = '10000'
		params['issuingCollectingBankAddress'] = 'Issuing Collecting Bank Address'
		params['lcNumber'] = 'LC Number	'
		params['draftAmount'] = new BigDecimal('2000');
		params['seller'] = 'Seller'
		params['buyer'] = 'Buyer'
		params['usanceTerm'] = 'Usance Term'
		params['instructions']='Instructions'
		params['authorizedSignatory']='Authorized Signatory'
		params['signaturePortion']='Signature Portion'


		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
		}

		render toBeRendered as JSON
	}

	def runExportNegotiationAdviceforIssuingCollectingBank={
		def token = params.token ?: 'Export_Negotiation_Advice_for_Issuing_Collecting_Bank'

		params['P_REPORTNAME'] = "Export_Negotiation_Advice_for_Issuing_Collecting_Bank"

		params['specificRptPath'] = 'EXPORTS/EBC-FX LC Non-LC Negotiation'       //Follow the '/' format

		params['refNo'] = 'Reference Number'
		params['issuingCollectingBank'] = 'Issuing Collecting Bank'				  //for Decimalsparams['amount'] = '10000'
		params['issuingCollectingBankAddress'] = 'Issuing Collecting Bank Address'
		params['lcNumber'] = 'LC Number'
		params['draftAmount'] = new BigDecimal('2000');
		params['shipment'] = 'Shipment'
		params['exporterName']='Exporter Name'
		params['importerName']='Importer Name'
		params['usanceTenorTerm'] = 'Usance Tenor Term'
		params['draftOrig1Lot'] = 'Draft Orig 1 Lot'
		params['draftOrig2Lot'] = 'Draft Orig 2 Lot'
		params['draftDup1Lot'] = 'Draft Dup 1 Lot'
		params['draftDup2Lot'] = 'Draft Dup 2 Lot'
		params['commercialOrig1Lot'] = 'Commercial Orig 1 Lot'
		params['commercialOrig2Lot'] = 'Commercial Orig 2 Lot'
		params['commercialDup1Lot'] = 'Commercial Dup 1 Lot'
		params['commercialDup2Lot'] = 'Commercial Dup 2 Lot'
		params['insurancelOrig1Lot'] = 'Insurancel Orig 1 Lot'
		params['insuranceOrig2Lot'] = 'Insurance Orig 2 Lot'
		params['insuranceDup1Lot'] = 'Insurance Dup 1 Lot'
		params['insuranceDup2Lot'] = 'Insurance Dup 2 Lot'
		params['woodOrig1Lot'] = 'Wood Orig 1 Lot'
		params['woodOrig2Lot'] = 'Wood Orig 2 Lot'
		params['woodDup1Lot'] = 'Wood Dup 1 Lot'
		params['woodDup2Lot'] = 'Wood Dup 2 Lot'
		params['billOrig1Lot']='Bill Orig 1 Lot'
		params['billOrigl2Lot']='Bill Orig 2 Lot'
		params['billDup1Lot']='Bill Dup 1 Lot'
		params['billDup2Lot']='Bill Dup 2 Lot'
		params['packingOrig1Lot'] = 'Packing Orig 1 Lot'
		params['packingOrig2Lot'] = 'Packing Orig 2 Lot'
		params['packingDup1Lot'] = 'Packing Dup 1 Lot'
		params['packingDup2Lot'] = 'Packing Dup 2 Lot'
		params['certAnalysisOrig1Lot']='Cert Analysis Orig 1 Lot'
		params['certAnalysisOrig2Lot']='Cert Analysis Orig 2 Lot'
		params['certAnalysisDup1Lot']='Cert Analysis Dup 1 Lot'
		params['certAnalysisDup2Lot']='Cert Analysis Dup 2 Lot'
		params['certOriginOrig1Lot']='Cert Origin Orig 1 Lot'
		params['certOriginOrig2Lot']='Cert Origin Orig 2 Lot'
		params['certOriginDup1Lot']='Cert Origin Dup 1 Lot'
		params['certOriginDup2Lot']='Cert Origin Dup 2 Lot'
		params['approver']='Approver'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
		}

		render toBeRendered as JSON
	}

	def runExportNegotiationAdviceForReimbursingBank={
		def token = params.token ?: 'Export_Negotiation_Advice_for_Reimbursing_Bank'

		params['P_REPORTNAME'] = "Export_Negotiation_Advice_for_Reimbursing_Bank"

		params['specificRptPath'] = 'EXPORTS/EBC-FX LC Non-LC Negotiation'       //Follow the '/' format

		params['refNo'] = 'Reference Number'
		params['issuingCollectingBank'] = 'Issuing Collecting Bank'				  //for Decimalsparams['amount'] = '10000'
		params['issuingCollectingBankAddress'] = 'Issuing Collecting Bank Address'
		params['lcNumber'] = 'LC Number'
		params['draftAmount'] = new BigDecimal('2000');
		params['shipment'] = 'Shipment'
		params['exporterName']='Exporter Name'
		params['importerName']='Importer Name'
		params['usanceTenorTerm'] = 'Usance Tenor Term'
		params['draftOrig1Lot'] = 'Draft Orig 1 Lot'
		params['draftOrig2Lot'] = 'Draft Orig 2 Lot'
		params['draftDup1Lot'] = 'Draft Dup 1 Lot'
		params['draftDup2Lot'] = 'Draft Dup 2 Lot'
		params['billOrig1Lot']='Bill Orig 1 Lot'
		params['billOrigl2Lot']='Bill Orig 2 Lot'
		params['billDup1Lot']='Bill Dup 1 Lot'
		params['billDup2Lot']='Bill Dup 2 Lot'
		params['airwayBillOrig1Lot']='Airway Bill Orig 1 Lot'
		params['airwayBillOrig2Lot']='Airway Bill Orig 2 Lot'
		params['airwayBillDup1Lot']='Airway Bill Dup 1 Lot'
		params['airwayBillDup2Lot']='Airway Bill Dup 2 Lot'
		params['commercialOrig1Lot'] = 'Commercial Orig 1 Lot'
		params['commercialOrig2Lot'] = 'Commercial Orig 2 Lot'
		params['commercialDup1Lot'] = 'Commercial Dup 1 Lot'
		params['commercialDup2Lot'] = 'Commercial Dup 2 Lot'
		params['packingOrig1Lot'] = 'Packing Orig 1 Lot'
		params['packingOrig2Lot'] = 'Packing Orig 2 Lot'
		params['packingDup1Lot'] = 'Packing Dup 1 Lot'
		params['packingDup2Lot'] = 'Packing Dup 2 Lot'
		params['insurancelOrig1Lot'] = 'Insurancel Orig 1 Lot'
		params['insuranceOrig2Lot'] = 'Insurance Orig 2 Lot'
		params['insuranceDup1Lot'] = 'Insurance Dup 1 Lot'
		params['insuranceDup2Lot'] = 'Insurance Dup 2 Lot'
		params['origCertOrig1Lot'] = 'Orig Cert Orig 1 Lot'
		params['origCertOrig2Lot'] = 'Orig Cert Orig 2 Lot'
		params['origCertDup1Lot'] = 'Orig Cert Dup 1 Lot'
		params['origCertDup2Lot'] = 'Orig Cert Dup 2 Lot'
		params['weightOrig1Lot'] = 'Weight Orig 1 Lot'
		params['weightOrig2Lot'] = 'Weight Orig 2 Lot'
		params['weightDup1Lot'] = 'Weight Dup 1 Lot'
		params['weightDup2Lot'] = 'Weight Dup 2 Lot'
		params['cleanTightFitOrig1Lot'] = 'Clean Tight Fit Orig 1 Lot'
		params['cleanTightFitOrig2Lot'] = 'Clean Tight Fit Orig 2 Lot'
		params['cleanTightFitDup1Lot'] = 'Clean Tight Fit Dup 1 Lot'
		params['cleanTightFitDup2Lot'] = 'Clean Tight Fit Dup 2 Lot'
		params['analysisOrig1Lot'] = 'Analysis Orig 1 Lot'
		params['analysisOrig2Lot'] = 'Analysis Orig 2 Lot'
		params['analysisDup1Lot'] = 'Analysis Dup 1 Lot'
		params['analysisDup2Lot'] = 'Analysis Dup 2 Lot'
		params['loadingOrig1Lot'] = 'Loading Orig 1 Lot'
		params['loadingOrig2Lot'] = 'Loading Orig 2 Lot'
		params['loadingDup1Lot'] = 'Loading Dup 1 Lot'
		params['loadingDup2Lot'] = 'Loading Dup 2 Lot'
		params['beneficiaryOrig1Lot'] = 'Beneficiary Orig 1 Lot'
		params['beneficiaryOrig2Lot'] = 'Beneficiary Orig 2 Lot'
		params['beneficiaryDup1Lot'] = 'Beneficiary Dup 1 Lot'
		params['beneficiaryDup2Lot'] = 'Beneficiary Dup 2 Lot'
		params['inspectionOrig1Lot'] = 'Inspection Orig 1 Lot'
		params['inspectionOrig21Lot'] ='Inspection Orig 2 Lot'
		params['inspectionDup1Lot'] = 'Inspection Dup 1 Lot'
		params['inspectionDup2Lot'] = 'Inspection Dup 2 Lot'
		params['masterAuthorityOrig1Lot'] = 'Master Authority Orig 1 Lot'
		params['masterAuthorityOrig2Lot'] = 'Master Authority Orig 2 Lot'
		params['masterAuthorityDup1Lot'] = 'Master Authority Dup 1 Lot'
		params['masterAuthorityDup2Lot'] = 'Master Authority Dup 2 Lot'
		params['heatingOrig1Lot'] = 'Heating Orig 1 Lot'
		params['heatingOrig2Lot'] = 'Heating Orig 2 Lot'
		params['heatingDup1Lot'] = 'Heating Dup 1 Lot'
		params['heatingDup2Lot'] = 'Heating Dup 2 Lot'
		params['charterOrig1Lot']='Charter Orig 1 Lot'
		params['charterOrig2Lot']='Charter Orig 2Lot'
		params['charterDup1Lot']='Charter Dup 1 Lot'
		params['charterDup2Lot']='Charter Dup 2 Lot'
		params['customsOrig1Lot']='Customs Orig 1 Lot'
		params['customsOrig2Lot']='Customs Orig 2 Lot'
		params['customsDup1Lot']='Customs Dup 1 Lot'
		params['customsDup2Lot']='Customs Dup 2 Lot'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
		}

		render toBeRendered as JSON

	}


	/*def runDailyOutstandingFXLCsTreasuryReport = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'Daily_Outstanding_FXLCs_Treasury_Report'
		
		params['P_REPORTNAME'] = "Daily_Outstanding_FXLCs_Treasury_Report"
		 
		params['specificRptPath'] = ''       //Follow the '/' format

						
		params['asOfDate'] = 'September 17,2012'		  //for Dates
			params['reportId'] = reportIdService.generateReportId("DFXOS")
		println "REPORT ID: "+params['reportId']
		//for Decimalsparams['amount'] = '10000'				  //for Decimalsparams['amount'] = '10000'
						
		println ""
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
		
		
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		
		ext_params['token'] = token
		
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
			}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
		
		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
		
		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
			}
		
		render toBeRendered as JSON
		
	}*/

/*	def runDailyOutstandingLCsCCBDReport = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'Daily_Outstanding_LCs_CCBD_Report'
		
		params['P_REPORTNAME'] = "Daily_Outstanding_LCs_CCBD_Report"
		 
		params['specificRptPath'] = 'FXLC/FXLC Adjustment'       //Follow the '/' format
				
		
		params['documentNumber'] = 'Doc No.'
		params['cifName'] = 'CIF Name'
		params['lcTransactionType'] = 'Transaction Type'
		params['lcExpiryDate'] = 'September 17,2012'
		params['lcCurrency'] = 'Peso'
		params['originalLcAmountFrom'] = 'Orig LC Amount From'
		params['originalLcAmountTo'] = 'Orig LC Amount To'
		params['osLcAmountFrom'] = 'OS Lc Amount From'
		params['osLcAmountTo'] = 'OS Lc Amount To'
		params['openingDateFrom'] = 'September 17,2012'		  //for Dates
		params['openingDateTo'] = 'September 22,2012'				  //for Decimalsparams['amount'] = '10000'
						
		println ""
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
		
		
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		
		ext_params['token'] = token
		
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
			}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
		
		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
		
		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
			}
		
		render toBeRendered as JSON
	}
*/
	
	/*def runDailyCashFXLCsOpenedTreasuryReport={
		def token = params.token ?: 'Daily_Cash_FXLCs_Opened_Treasury_Report'
		
		params['P_REPORTNAME'] = "Daily_Cash_FXLCs_Opened_Treasury_Report"
		 
		params['specificRptPath'] = ''
		
		//Follow the '/' format
		params['reportId'] = reportIdService.generateReportId("DFXCO")
		println "REPORT ID: "+params['reportId']
		params['asOfDate'] = 'September 17,2012'
						
		println ""
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
		
		
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		
		ext_params['token'] = token
		
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
			}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
		
		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
		
		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
			}
		
		render toBeRendered as JSON
		}
*/
/*	def runDailyFXLCsOpenedTreasuryReport={
		
		def token = params.token ?: 'Daily_FXLCs_Opened_Treasury_Report'
		
		params['P_REPORTNAME'] = "Daily_FXLCs_Opened_Treasury_Report"
		 
		params['specificRptPath'] = ''
		
		//Follow the '/' format
		params['reportId'] = params.reportId

						
		println ""
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
		
		
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		
		ext_params['token'] = token
		
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
			}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
		
		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
		
		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
			}
		
		render toBeRendered as JSON
		}
*/

	def runApAgingReport={
		def token = params.token ?: 'AP Aging Report'

		params['P_REPORTNAME'] = "AP Aging Report"

		params['specificRptPath'] = 'IMPORTS OTHERS/AP Monitoring'

		//Follow the '/' format
		params['reportId'] = params.reportId
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
		}

		render toBeRendered as JSON
	}


	def runQuarterlyAgingReport={
		def token = params.token ?: 'Quarterly Aging Report'

		params['P_REPORTNAME'] = "Quarterly Aging Report"

		params['specificRptPath'] = 'IMPORTS OTHERS/AR Monitoring'

		//Follow the '/' format
		params['reportId'] = params.reportId
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
		}

		render toBeRendered as JSON
	}

	//Confirmation Letter
	def runConfirmationLetter={
		def token = params.token ?: 'Confirmation_Letter'

		params['P_REPORTNAME'] = "Confirmation_Letter"

		params['specificRptPath'] = ''

		//Follow the '/' format
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['importerName'] = 'Juan Dela Cruz'
		params['importerAddress'] = 'Dela Cruz St. Dela Cruz Subdivision, Dela Cruz City'
		params['lcNumber'] = 'LC Number'
		params['currency'] = 'EUR'
		params['amount'] = '20000'
		params['dateOpened'] = 'September 26, 2012'
		params['serviceType'] = 'Service Type'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		Boolean isReportGenerated = generatePDFFile(reportDataList, JasperExportFormat.PDF_FORMAT, ext_params)
		def toBeRendered = ['isReportGenerated':'false']
		if(isReportGenerated){
			toBeRendered = ['isReportGenerated':'true']
		}

		render toBeRendered as JSON

	}

	//Alvin

	//TFS MCO REPORTS
	def runTfsMcoReport={

		//TODO: create separate date for generating mco at
		//end of month and generating mco from previous month
		
		def token = params.token ?: 'Tfs_Mco_Report'

		params['P_REPORTNAME'] = 'Tfs_Mco_Report'
		params['specificRptPath'] = 'BATCHES/Mco'       //Follow the '/' format

		params['specialMco'] = ''

		params['reportId']  = reportIdService.generateReportId("MTMCO")
		params['asOfDate'] = new Date()
		params['format'] = "xls"
//		params['useDefaultParameters'] = "true"
		
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		//ADDED SUBREPORT_DIR param
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "\n--- START: TFS MCO REPORT PARAMETERS PROCESSING , TIME:"+ DateUtils.timeFormat(new Date()) + " ---"
		//Populate Data List to be passed to report xml
		//Populate rates List
		
		tfsMcoReportService.populateRatesList(ext_params['onlineReportDate'],false,'')
		tfsMcoReportService.deleteAllMcoData()
		ext_params = tfsMcoReportService.processForeignSight(ext_params)
		ext_params = tfsMcoReportService.processForeignUsance(ext_params)
		ext_params = tfsMcoReportService.processInwardBills(ext_params)
		ext_params = tfsMcoReportService.processDomesticSight(ext_params)
		ext_params = tfsMcoReportService.processDomesticUsance(ext_params)
		ext_params = tfsMcoReportService.processCashLc(ext_params)
		ext_params = tfsMcoReportService.processOutstandingAcceptance(ext_params)
		ext_params['creationDate'] = tfsMcoReportService.getEndOfMonthDate(ext_params['onlineReportDate'],'')
		println "TFS MCO REPORT SELECTED DATE: "+ ext_params['onlineReportDate']
		println "TFS MCO REPORT CREATION DATE: "+ ext_params['creationDate']
		println "\n--- END: TFS MCO REPORT PARAMETERS PROCESSING , TIME:"+ DateUtils.timeFormat(new Date()) + " ---"
		generateGenericReport(ext_params['foreignSight'], ext_params["format"], ext_params)
	}
	
	def runTfsMcoReportSpecial={
			
		//TODO: create separate date for generating mco at
		//end of month and generating mco from previous month
		
		def token = params.token ?: 'Tfs_Mco_Report'
			
		params['P_REPORTNAME'] = 'Tfs_Mco_Report'
		params['specificRptPath'] = 'BATCHES/Mco'       //Follow the '/' format
		
		params['specialMco'] = 'special'
		
		params['reportId']  = reportIdService.generateReportId("MTMCO")
		params['asOfDate'] = new Date()
		params['format'] = "xls"
//		params['useDefaultParameters'] = "true"
		
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
						
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		//ADDED SUBREPORT_DIR param
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['token'] = token
		
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "\n--- START: TFS MCO REPORT PARAMETERS PROCESSING , TIME:"+ DateUtils.timeFormat(new Date()) + " ---"
		//Populate Data List to be passed to report xml
		//Populate rates List
		tfsMcoReportService.populateRatesList(ext_params['onlineReportDate'],true,'special')
		ext_params = tfsMcoReportService.processForeignSight(ext_params)
		ext_params = tfsMcoReportService.processForeignUsance(ext_params)
		ext_params = tfsMcoReportService.processInwardBills(ext_params)
		ext_params = tfsMcoReportService.processDomesticSight(ext_params)
		ext_params = tfsMcoReportService.processDomesticUsance(ext_params)
		ext_params = tfsMcoReportService.processCashLc(ext_params)
		ext_params = tfsMcoReportService.processOutstandingAcceptance(ext_params)
		ext_params['creationDate'] = tfsMcoReportService.getEndOfMonthDate(ext_params['onlineReportDate'],'special')
		println "TFS MCO REPORT SELECTED DATE: "+ ext_params['onlineReportDate']
		println "TFS MCO REPORT CREATION DATE: "+ ext_params['creationDate']
		println "\n--- END: TFS MCO REPORT PARAMETERS PROCESSING , TIME:"+ DateUtils.timeFormat(new Date()) + " ---"
		generateGenericReport(ext_params['foreignSight'], ext_params["format"], ext_params)
	}

	//Daily Foreign Regular LC Opened
	def runDailyForeignRegularLcOpened={
		def token = params.token ?: 'Daily_Foreign_Regular_LCs_Opened'

		params['P_REPORTNAME'] = 'Daily_Foreign_Regular_LCs_Opened'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format
		params['asOfDate'] = params.onlineReportDate
		params['reportId'] = reportIdService.generateReportId("DFXRO")
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Daily Foreign Cash LC Opened
	def runDailyForeignCashLcOpened={
		def token = params.token ?: 'Daily_Foreign_Cash_LCs_Opened'

		params['P_REPORTNAME'] = 'Daily_Foreign_Cash_LCs_Opened'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format
		params['asOfDate'] = params.onlineReportDate
		params['reportId'] = reportIdService.generateReportId("DFXCO")
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Daily Outstanding Foreign LC
	def runDailyOutstandingForeignLc={
		def token = params.token ?: 'Daily_Outstanding_Foreign_LCs'

		params['P_REPORTNAME'] = 'Daily_Outstanding_Foreign_LCs'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format
		params['asOfDate'] = params.onlineReportDate
		params['reportId'] = reportIdService.generateReportId("DFXOS")
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Daily Foreign LC Negotiations
	def runDailyFunding={
		def token = params.token ?: 'Daily_Funding_Report'

		params['P_REPORTNAME'] = 'Daily_Funding'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format
		params['dateGenerated'] = params.onlineReportDate
		params['reportId'] = reportIdService.generateReportId("DFXLN")
		params['currentDate'] = new SimpleDateFormat("yyyy-MM-dd").format(new Date(params.onlineReportDate))
		params['asOfDate'] = params.onlineReportDate
		params['format'] = "pdf"

		println "params: "+params['dateGenerated']
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Daily Outstanding CCBD
	def dailyOutstandingCCBD={
		def token = params.token ?: 'Daily_Outstanding_LCs_CCBD_Report'

		params['P_REPORTNAME'] = 'Daily_Outstanding_LCs_CCBD_Report'

		params['specificRptPath'] = ''       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("DCCDO")
		params['reportDate'] = params.onlineReportDate
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}


	//Outstanding Foreign Sight LC Per Importer
	def runOutstandingForeignSightLcPerImporter={
		def token = params.token ?: 'Outstanding_Foreign_Sight_LCs_per_Importer'

		params['P_REPORTNAME'] = 'Outstanding_Foreign_Sight_LCs_per_Importer'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MFRSI")
		params['asOfDate'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		//ADDED SUBREPORT_DIR param
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/" +"/"
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Foreign Sight LC Per Currency
	def runOutstandingForeignSightLcPerCurrency={
		def token = params.token ?: 'Outstanding_Foreign_Sight_LCs_per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Foreign_Sight_LCs_per_Currency'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MFRSC")
		params['date'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Domestic Sight LC Per Importer
	def runOutstandingDomesticSightLcPerImporter={
		def token = params.token ?: 'Outstanding_Domestic_Sight_LCs_Per_Importer'

		params['P_REPORTNAME'] = 'Outstanding_Domestic_Sight_LCs_Per_Importer'

		params['specificRptPath'] = 'BATCHES/DMLC/'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MDRSI")
		params['asOfDate'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		//ADDED SUBREPORT_DIR param
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Domestic Sight LC Per Currency
	def runOutstandingDomesticSightLcPerCurrency={
		def token = params.token ?: 'Outstanding_Domestic_Sight_LCs_Per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Domestic_Sight_LCs_Per_Currency'

		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MDRSC")
		params['asOfDate'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Foreign Usance LC Per Importer
	def runOutstandingForeignUsanceLcPerImporter={
		def token = params.token ?: 'Outstanding_Foreign_Usance_LCs_per_Importer'

		params['P_REPORTNAME'] = 'Outstanding_Foreign_Usance_LCs_per_Importer'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MFRUI")
		params['asOfDate'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		//ADDED SUBREPORT_DIR param
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/" + "/"
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Foreign Usance LC Per Currency
	def runOutstandingForeignUsanceLcPerCurrency={
		def token = params.token ?: 'Outstanding_Foreign_Usance_LCs_per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Foreign_Usance_LCs_per_Currency'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MFRUC")
		params['date'] = new SimpleDateFormat("MM/dd/yyyy").format(new Date(params.onlineReportDate))
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Domestic Usance LC Per Importer
	def runOutstandingDomesticUsanceLcPerImporter={
		def token = params.token ?: 'Outstanding_Domestic_Usance_LCs_Per_Importer'

		params['P_REPORTNAME'] = 'Outstanding_Domestic_Usance_LCs_Per_Importer'

		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MDRUI")
		params['asOfDate'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']



		//ADDED SUBREPORT_DIR param
		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"
		//	ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Outstanding Domestic Usance LC Per Currency
	def runOutstandingDomesticUsanceLcPerCurrency={
		def token = params.token ?: 'Outstanding_Domestic_Usance_LCs_Per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Domestic_Usance_LCs_Per_Currency'

		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MDSUC")
		params['asOfDate'] = new SimpleDateFormat("MM/dd/yyyy").format(new Date(params.onlineReportDate))
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}



	//Outstanding Inward Bills for Collection FXLC per Currency
	def runOutstandingInwardBillsforCollectionFXLCperCurrency={
		def token = params.token ?: 'Outstanding_Inward_Bills_for_Collection_FXLC_per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Inward_Bills_for_Collection_FXLC_per_Currency'

		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("FXSO")
		params['date'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		println "SUBREPORT DIR:"+ ext_params['SUBREPORT_DIR']
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//BREN
	//Foreign LCs Opened for the Month
	def runForeignLcOpenedForTheMonth={
		def token = params.token ?: 'Foreign_LCs_Opened_for_the_Month'

		params['P_REPORTNAME'] = 'Foreign_LCs_Opened_for_the_Month'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MFXLO")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['date'] = params.onlineReportDate
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"
		ext_params['token'] = token


		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Domestic LCs Opened for the Month
	def runDomesticLcOpenedForTheMonth={
		def token = params.token ?: 'Domestic_LCs_Opened_for_the_Month'

		params['P_REPORTNAME'] = 'Domestic_LCs_Opened_for_the_Month'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MDMLO")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['month'] = params.onlineReportDate
		params['year'] = params.onlineReportDate
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

//		ext_params['SUBREPORT_DIR'] = "C:/ucpb-tfs/workspace/tfs-report/reports/BATCHES/DMLC/"	//tentative - subreport directory location needs to be standardized
		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Consolidated_Report_on_Domestic_LCs_Opened_for_the_Month
	def runConsolidatedReportDomesticLcOpenedForMonth={
		def token = params.token ?: 'Consolidated_Report_on_Domestic_LCs_Opened_for_the_Month'

		params['P_REPORTNAME'] = "Consolidated_Report_on_Domestic_LCs_Opened_for_the_Month"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MCDMO")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['month'] = new SimpleDateFormat("MMMM").format(new Date(params.onlineReportDate))
		params['year'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['yearNow'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

//		ext_params['SUBREPORT_DIR'] = "C:/ucpb-tfs/workspace/tfs-report/reports/BATCHES/DMLC/"	//tentative - subreport directory location needs to be standardized
		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"


		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Consolidated Report on Foreign LCs Opened For the Month
	def runConsolidatedReportOnForeignLcOpenedForMonth={
		def token = params.token ?: 'Consolidated_Report_on_Foreign_LCs_Opened_for_the_Month'

		params['P_REPORTNAME'] = 'Consolidated_Report_on_Foreign_LCs_Opened_for_the_Month'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MCFXO")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['yearNow'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['year'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['month'] = new SimpleDateFormat("MMMM").format(new Date(params.onlineReportDate))
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		//params['forTheMonthYear'] = new Date()
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//YTD Report on Foreign LC Opened
	def runYtdReportOnForeignLcOpened={
		def token = params.token ?: 'YTD_Report_on_Foreign_LCs_Opened'

		params['P_REPORTNAME'] = 'YTD_Report_on_Foreign_LCs_Opened'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MYFXO")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['ytdMonthYear'] = params.onlineReportDate
		params['month'] = 'Month'
		params['year'] = 'Year'
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['yearNow'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"
		ext_params['token'] = token



		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//YTD Report on Domestic LC Opened
	def runYtdReportOnDomesticLcOpened={
		def token = params.token ?: 'YTD_Report_on_Domestic_LCs_Opened'

		params['P_REPORTNAME'] = 'YTD_Report_on_Domestic_LCs_Opened'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MYDMO")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['ytdMonthYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['yearNow'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//YTD Report on Foreign LC Opened Classified By Bank Top 30
	def runYtdReportOnForeignLcOpenedClassifiedByBank={
		def token = params.token ?: 'YTD_Report_on_Foreign_LCs_Opened_Classified_by_top_30_Importer_and_Advising_Bank_in_USD'

		params['P_REPORTNAME'] = 'YTD_Report_on_Foreign_LCs_Opened_Classified_by_top_30_Importer_and_Advising_Bank_in_USD'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MYF30")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['ytdMonthYear'] = params.onlineReportDate
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['yearNow'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//YTD Report on Domestic LC Opened Classified By Bank Top 30
	def runYtdReportOnDomesticLcOpenedClassifiedByBank={
		def token = params.token ?: 'YTD_Report_on_Domestic_LCs_Opened_Classified_by_top_30_Importer_and_Advising_Local_Bank_in_PHP'

		params['P_REPORTNAME'] = 'YTD_Report_on_Domestic_LCs_Opened_Classified_by_top_30_Importer_and_Advising_Local_Bank_in_PHP'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/DMLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MYD30")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['month'] = new SimpleDateFormat("MMMM").format(new Date(params.onlineReportDate))
		params['year'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['yearNow'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Weekly Report for Open LC's Foreign and Domestic
	def runWeeklyForeignDomesticOpenLC={
		def token = params.token ?: 'Weekly_Foreign_Domestic_LCs_Opened'
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date dateToday;
		String strDateToday
//		dateTomorrow.format()
		dateToday = sdf.parse(sdf.format(new Date(params.onlineReportDate.toString())));
		Calendar c = Calendar.getInstance();
		c.setTime(dateToday);
		c.add(Calendar.DATE, 1);
		String dateTomorrow = sdf.format(c.getTime())
		c.setTime(dateToday);
		c.add(Calendar.DATE, 7);
		String dateAfter7Days =  sdf.format(c.getTime())
		
		params['P_REPORTNAME'] = 'Weekly_Foreign_Domestic_LCs_Opened'
		
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("WFDLC")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		
		params['currentWeek'] = new SimpleDateFormat("ww").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		
		
		if(params.currentWeek == null)
		params['currentWeek'] = new SimpleDateFormat("ww").format(new Date())
		if(params.currentYear == null)
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date())
		
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		def choosenWeek = params.currentWeek as int
		def choosenYear = params.currentYear as int
		Integer paramValue = params.int
		calendar.set(Calendar.WEEK_OF_YEAR, choosenWeek);
		calendar.set(Calendar.YEAR, choosenYear);

		SimpleDateFormat formatter = new SimpleDateFormat("ddMMM yyyy"); // PST`
		Date startDate = calendar.getTime();
		def startDateInStr = formatter.format(startDate);


		calendar.add(Calendar.DATE, 6);
		Date enddate = calendar.getTime();
		def endDaString = formatter.format(enddate);
	
		
	
		params['startDaterep'] =  startDateInStr.toString()
		params['endDaterep'] =  endDaString.toString()
		
		params['currentMonth'] = new SimpleDateFormat("M").format(new Date())
		params['format'] = 'csv'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

		
		
		
	}
	
	
	
	//Weekly Report on Maturing Usance
	def runWeeklyReportOnMaturingUsancLc={
		def token = params.token ?: 'Weekly_Report_on_Maturing_Usance_LCs'
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date dateToday;
		String strDateToday
//		dateTomorrow.format()
		dateToday = sdf.parse(sdf.format(new Date(params.onlineReportDate.toString())));
		Calendar c = Calendar.getInstance();
		c.setTime(dateToday);
		c.add(Calendar.DATE, 1);
		String dateTomorrow = sdf.format(c.getTime())
		c.setTime(dateToday);
		c.add(Calendar.DATE, 7);
		String dateAfter7Days =  sdf.format(c.getTime())
		params['P_REPORTNAME'] = 'Weekly_Report_on_Maturing_Usance_LCs'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/FXLC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("WMUAL")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['week1'] = dateTomorrow
		params['week2'] = dateAfter7Days
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}


	//Ap Others
	def runApOthers={
		def token = params.token ?: 'AP_Others'

		params['P_REPORTNAME'] = 'AP_Others'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MOTAP")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['forMonth'] = new SimpleDateFormat("MMMMM").format(new Date(params.onlineReportDate))
		params['forYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//runArOthers
	def runArOthers={
		def token = params.token ?: 'AR_Others'

		params['P_REPORTNAME'] = 'AR_Others'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MOTAR")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['currentMonth'] = new SimpleDateFormat("M").format(new Date())
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date())
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['forMonth'] = new SimpleDateFormat("MMMMM").format(new Date(params.onlineReportDate))
		params['forYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//ERIC
	//BILLING STATEMENT
	def runBillingStatement={
		def token = params.token ?: 'Billing_Statement'

		params['P_REPORTNAME'] = "Billing_Statement"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format

		def currency = params.currency
		def usdToPhp = genericReportsQueryService.getDetailsFromTradeService("USD-PHP_text_special_rate",params.tradeServiceId)
		def usdToPhpLength
		def usdToPhpSpecialRate
		
		if(usdToPhp != "") {
			usdToPhpLength = new BigDecimal(usdToPhp).stripTrailingZeros().toString().length()
			if(usdToPhpLength <= 5) {
				usdToPhpSpecialRate = new DecimalFormat("#,##0.00").format(new BigDecimal(usdToPhp))
			} else {
				usdToPhpSpecialRate = new BigDecimal(usdToPhp).stripTrailingZeros()
			}
		} else {
			usdToPhpSpecialRate = ""
		}
		
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['longName']=genericReportsQueryService.getDetailsFromTradeService("longName",params.tradeServiceId)
		params['importerName']=genericReportsQueryService.getDetailsFromTradeService("importerName",params.tradeServiceId)
		params['documentType']=genericReportsQueryService.getDetailsFromTradeService("documentType",params.tradeServiceId)
		params['documentClass']=genericReportsQueryService.getDetailsFromTradeService("documentClass",params.tradeServiceId)
		params['documentSubType1']=genericReportsQueryService.getDetailsFromTradeService("documentSubType1",params.tradeServiceId)
		params['lcCurrency']=genericReportsQueryService.getDetailsFromTradeService("lcCurrency",params.tradeServiceId)
		params['lcAmount']=genericReportsQueryService.getDetailsFromTradeService("lcAmount",params.tradeServiceId)
		params['lcExpiryDate']=genericReportsQueryService.getDetailsFromTradeService("lcExpiryDate",params.tradeServiceId)
		params['usanceTerm']=genericReportsQueryService.getDetailsFromTradeService("usanceTerm",params.tradeServiceId)
		params['THIRD-USD_text_special_rate']= genericReportsQueryService.getDetailsFromTradeService(currency+"-USD_text_special_rate",params.tradeServiceId)
		params['USD-PHP_text_special_rate']= usdToPhpSpecialRate
		params['urr']= genericReportsQueryService.getDetailsFromTradeService("urr",params.tradeServiceId)
		params['USD-PHP_urr']= genericReportsQueryService.getDetailsFromTradeService("USD-PHP_urr",params.tradeServiceId)
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//BILLING STATEMENT FOR CASH
	def runBillingStatementCash={
		def token = params.token ?: 'Billing_Statement_For_Cash'

		params['P_REPORTNAME'] = "Billing_Statement_For_Cash"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format
		
		def currency = params.currency
		def usdToPhp = genericReportsQueryService.getDetailsFromTradeService("USD-PHP_text_special_rate",params.tradeServiceId)
		def usdToPhpLength
		def usdToPhpSpecialRate
		
		if(usdToPhp != "") {
			usdToPhpLength = new BigDecimal(usdToPhp).stripTrailingZeros().toString().length()
			if(usdToPhpLength <= 5) {
				usdToPhpSpecialRate = new DecimalFormat("#,##0.00").format(new BigDecimal(usdToPhp))
			} else {
				usdToPhpSpecialRate = new BigDecimal(usdToPhp).stripTrailingZeros()
			}
		} else {
			usdToPhpSpecialRate = ""
		}

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['documentType']= genericReportsQueryService.getDetailsFromTradeService("documentType",params.tradeServiceId)
		params['documentClass']= genericReportsQueryService.getDetailsFromTradeService("documentClass",params.tradeServiceId)
		params['documentSubType1']=genericReportsQueryService.getDetailsFromTradeService("documentSubType1",params.tradeServiceId)
		params['serviceType']= genericReportsQueryService.getDetailsFromTradeService("serviceType",params.tradeServiceId)
		params['expiryDate']= genericReportsQueryService.getDetailsFromTradeService("expiryDate",params.tradeServiceId)
		params['priceTerm']= genericReportsQueryService.getDetailsFromTradeService("priceTerm",params.tradeServiceId)
		params['THIRD-USD_text_special_rate']= genericReportsQueryService.getDetailsFromTradeService(currency+"-USD_text_special_rate",params.tradeServiceId)
		params['USD-PHP_text_special_rate']= usdToPhpSpecialRate
		params['urr']= genericReportsQueryService.getDetailsFromTradeService("urr",params.tradeServiceId)
		params['USD-PHP_urr']= genericReportsQueryService.getDetailsFromTradeService("USD-PHP_urr",params.tradeServiceId)
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//BILLING STATEMENT FOR NEGO AMOUNT
	def runBillingStatementNegoAmount={
		def token = params.token ?: 'Billing_Statement_For_Nego_Amount'

		params['P_REPORTNAME'] = "Billing_Statement_For_Nego_Amount"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format

		def currency = params.currency
		def usdToPhp = genericReportsQueryService.getDetailsFromTradeService("USD-PHP_text_special_rate",params.tradeServiceId)
		def usdToPhpLength
		def usdToPhpSpecialRate
		
		if(usdToPhp != "") {
			usdToPhpLength = new BigDecimal(usdToPhp).stripTrailingZeros().toString().length()
			if(usdToPhpLength <= 5) {
				usdToPhpSpecialRate = new DecimalFormat("#,##0.00").format(new BigDecimal(usdToPhp))
			} else {
				usdToPhpSpecialRate = new BigDecimal(usdToPhp).stripTrailingZeros()
			}
		} else {
			usdToPhpSpecialRate = ""
		}

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['longName']=genericReportsQueryService.getDetailsFromTradeService("longName",params.tradeServiceId)
		params['importerName']=genericReportsQueryService.getDetailsFromTradeService("importerName",params.tradeServiceId)
		params['THIRD-USD_text_special_rate']= genericReportsQueryService.getDetailsFromTradeService(currency+"-USD_text_special_rate",params.tradeServiceId)
		params['USD-PHP_text_special_rate']= usdToPhpSpecialRate
		params['urr']= genericReportsQueryService.getDetailsFromTradeService("urr",params.tradeServiceId)
		params['USD-PHP_urr']= genericReportsQueryService.getDetailsFromTradeService("USD-PHP_urr",params.tradeServiceId)
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//BILLING STATEMENT FOR MARGINAL DEPOSIT
	def runBillingStatementForMarginalDeposit={
		def token = params.token ?: 'Billing_Statement_For_Marginal_Deposit'

		params['P_REPORTNAME'] = "Billing_Statement_For_Marginal_Deposit"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//CREDIT MEMO...
	def runCreditMemoReport = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Credit_Memo'

		if(params.creditMemoType == "creditMemo") {
			params['P_REPORTNAME'] = "Credit_Memo"
		} else if(params.creditMemoType == "creditMemoCdtSendToBoc") {
			params['P_REPORTNAME'] = "Credit_Memo_Cdt_Send_To_Boc"
		} else {
			params['P_REPORTNAME'] = "Credit_Memo_Cdt_Refund"
		}
		
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['speechBubble'] = servletContext.getRealPath('reports/img/speech_bubble.jpg')
		params['documentNumber'] = params.documentNumber
		params['longName']=genericReportsQueryService.getDetailsFromTradeService("longName",params.tradeServiceId)
		params['cifName']=genericReportsQueryService.getDetailsFromTradeService("cifName",params.tradeServiceId)
		params['beneficiaryName']=genericReportsQueryService.getDetailsFromTradeService("beneficiaryName",params.tradeServiceId)
		params['approvedBy']=debitMemoReportDataService.debitMemoQueryAction(params.documentNumber, params.tradeServiceId)
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//DEBIT MEMO...
	def runDebitMemoReport = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Debit_Memo'
		
		if(params.debitMemoType == 'debitMemoCdtPayment') {
			params['P_REPORTNAME'] = "Debit_Memo_Cdt_Payment"
		} else if(params.debitMemoType == 'debitMemoCdtRemittance') {
			params['P_REPORTNAME'] = "Debit_Memo_Cdt_Remittance"
		} else if(params.debitMemoType == 'debitMemoCdtRefund') {
			params['P_REPORTNAME'] = "Debit_Memo_Cdt_Refund"
		} else {
			params['P_REPORTNAME'] = "Debit_Memo"
		}
		
		params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['speechBubble'] = servletContext.getRealPath('reports/img/speech_bubble.jpg')
		params['cifName'] = genericReportsQueryService.getDetailsFromTradeService("cifName", params.tradeServiceId)
		params['longName'] = genericReportsQueryService.getDetailsFromTradeService("longName", params.tradeServiceId)
		params['importerName'] = genericReportsQueryService.getDetailsFromTradeService("importerName", params.tradeServiceId)
		params['documentType'] = genericReportsQueryService.getDetailsFromTradeService("documentType", params.tradeServiceId)
		params['documentClass'] = genericReportsQueryService.getDetailsFromTradeService("documentClass", params.tradeServiceId)
		params['documentSubType1'] = genericReportsQueryService.getDetailsFromTradeService("documentSubType1", params.tradeServiceId)
		params['approvedBy']=debitMemoReportDataService.debitMemoQueryAction(params.documentNumber, params.tradeServiceId)
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"
		ext_params['cdtPaymentClientName'] = params.cdtPaymentClientName
		ext_params['cdtPaymentFileName'] = params.cdtPaymentClientName.toString().replaceAll("ampersand", "&") + " " + params.transactionReferenceNumber + " " + params.cdtPaymentPaidDate
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	
	def runBatchDebitMemoReport = {
		def token = params.token ?: 'Debit_Memo_Batch'
		
		params['P_REPORTNAME'] = "Debit_Memo_Batch"
				
		params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['speechBubble'] = servletContext.getRealPath('reports/img/speech_bubble.jpg')
	   
		params['format'] = "pdf"

		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
		
		def reportDataList = batchDebitMemoService.getBatchDebitMemoDataList(params.onlineReportDate, params.unitCode)
		
		batchDebitMemoService.deleteDirectory(grailsApplication.config.tfs.batchdebitmemo.directory)

		for(Map<String, Object> mapReportDataList: reportDataList) {
			List<Map<String,Object>> singleReportDataList = new ArrayList<Map<String,Object>>()
			singleReportDataList.add(mapReportDataList)
			batchDebitMemoService.generateBatchDebitReport(singleReportDataList, ext_params["format"], ext_params)
		}
		
		OutputStream os
		try {
			println "success in creating pdf files" 
			byte[] zip = batchDebitMemoService.zipDirectory(grailsApplication.config.tfs.batchdebitmemo.directory)
			batchDebitMemoService.deleteDirectory(grailsApplication.config.tfs.batchdebitmemo.directory)
			
			
			os = response.getOutputStream()
			response.setContentType("application/zip")

			if(zip.size() == 0){
				response.addHeader("Content-Disposition", "attachment; filename=No Report on " + params.onlineReportDate.replaceAll("/" , "-") + ".zip")
			} else {
				response.addHeader("Content-Disposition", "attachment; filename=BatchDebitMemo " + params.onlineReportDate.replaceAll("/" , "-") + ".zip")
			}
			os.write(zip)
            os.flush()
		} catch(Exception e) {
			println(e.getMessage())
		} finally {
			os.close()
		}
	}
		
	//LC Confirmation Regular Cash Opening
	def runLcConfirmationRegularCashOpening={
		def token = params.token ?: 'LC_Confirmation_Opening'

		params['P_REPORTNAME'] = "LC_Confirmation_Opening"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format
		
		params['issueDate']=genericReportsQueryService.getDetailsFromTradeService("issueDate",params.tradeServiceId)
		params['processDate']=genericReportsQueryService.getDetailsFromTradeService("processDate",params.tradeServiceId)
		params['expiryDate']=genericReportsQueryService.getDetailsFromTradeService("expiryDate",params.tradeServiceId)
		params['expiryPlace']=genericReportsQueryService.getDetailsFromTradeService("expiryPlace",params.tradeServiceId)
		params['applicantName']=genericReportsQueryService.getDetailsFromTradeService("applicantName",params.tradeServiceId)
		params['applicantAddress']=genericReportsQueryService.getDetailsFromTradeService("applicantAddress",params.tradeServiceId)
		params['beneficiaryName']=genericReportsQueryService.getDetailsFromTradeService("beneficiaryName",params.tradeServiceId)
		params['beneficiaryAddress']=genericReportsQueryService.getDetailsFromTradeService("beneficiaryAddress",params.tradeServiceId)
		params['currency']=genericReportsQueryService.getDetailsFromTradeService("currency",params.tradeServiceId)
		params['amount']=genericReportsQueryService.getDetailsFromTradeService("amount",params.tradeServiceId)
		params['tenor']=genericReportsQueryService.getDetailsFromTradeService("tenor",params.tradeServiceId)
		params['usancePeriod']=genericReportsQueryService.getDetailsFromTradeService("usancePeriod",params.tradeServiceId)
		params['tenorOfDraftNarrative']=genericReportsQueryService.getDetailsFromTradeService("tenorOfDraftNarrative",params.tradeServiceId)
		params['partialDelivery']=genericReportsQueryService.getDetailsFromTradeService("partialDelivery",params.tradeServiceId)
		params['placeOfTakingDispatchOrReceipt']=genericReportsQueryService.getDetailsFromTradeService("placeOfTakingDispatchOrReceipt",params.tradeServiceId)
		params['placeOfFinalDestination']=genericReportsQueryService.getDetailsFromTradeService("placeOfFinalDestination",params.tradeServiceId)
		params['generalDescriptionOfGoods']=genericReportsQueryService.getDetailsFromTradeService("generalDescriptionOfGoods",params.tradeServiceId)
		params['otherDocumentsInstructions']=genericReportsQueryService.getDetailsFromTradeService("otherDocumentsInstructions",params.tradeServiceId)
	
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//LC Confirmation Standby Opening
	def runLcConfirmationStandbyOpening={
		
		def reportName = WordUtils.capitalizeFully(params.formatType).replaceAll(" ", "_")
		def amount1 = genericReportsQueryService.getDetailsFromTradeService("amount",params.tradeServiceId)
		def amount2 = new DecimalFormat("#,##0.00").format(new BigDecimal(amount1.replaceAll(",", "")))
		def token = params.token ?: reportName

		params['P_REPORTNAME'] = reportName

		params['specificRptPath'] = 'DSLC Reports'

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		
		// temp param name, still the value will be issueDate
		params['processDate'] = genericReportsQueryService.getDetailsFromTradeService("issueDate",params.tradeServiceId)
		params['beneficiaryName'] = genericReportsQueryService.getDetailsFromTradeService("beneficiaryName",params.tradeServiceId).toUpperCase()
		params['beneficiaryAddress'] = genericReportsQueryService.getDetailsFromTradeService("beneficiaryAddress",params.tradeServiceId).toUpperCase()
		params['applicantName'] = genericReportsQueryService.getDetailsFromTradeService("applicantName",params.tradeServiceId).toUpperCase()
		params['applicantAddress'] = genericReportsQueryService.getDetailsFromTradeService("applicantAddress",params.tradeServiceId).toUpperCase()
		params['purposeOfStandby'] = genericReportsQueryService.getDetailsFromTradeService("purposeOfStandby",params.tradeServiceId).toUpperCase()
		params['currency'] = genericReportsQueryService.getDetailsFromTradeService("currency",params.tradeServiceId)
		params['amount'] = amount2
		params['expiryDate'] = genericReportsQueryService.getDetailsFromTradeService("expiryDate",params.tradeServiceId)
		
		// new fields exclusive for dslc reports
		params['contractLocation'] = genericReportsQueryService.getDetailsFromTradeService("contractLocation" , params.tradeServiceId).toUpperCase()
		params['biddingDate'] = genericReportsQueryService.getDetailsFromTradeService("biddingDate" , params.tradeServiceId);
		params['bidInvitationNumber'] = genericReportsQueryService.getDetailsFromTradeService("bidInvitationNumber" , params.tradeServiceId)
		params['bidInvitationDate'] = genericReportsQueryService.getDetailsFromTradeService("bidInvitationDate" , params.tradeServiceId)
		params['attentionName'] = genericReportsQueryService.getDetailsFromTradeService("attentionName" , params.tradeServiceId).toUpperCase()
		params['attentionNamePosition'] = genericReportsQueryService.getDetailsFromTradeService("attentionNamePosition" , params.tradeServiceId).toUpperCase()
		params['bank'] = genericReportsQueryService.getDetailsFromTradeService("bank" , params.tradeServiceId).toUpperCase()
		params['bankBranch'] = genericReportsQueryService.getDetailsFromTradeService("bankBranch" , params.tradeServiceId).toUpperCase()
		params['bankAddress'] = genericReportsQueryService.getDetailsFromTradeService("bankAddress" , params.tradeServiceId).toUpperCase()
		params['city'] = genericReportsQueryService.getDetailsFromTradeService("city" , params.tradeServiceId).toUpperCase()
		params['proprietor'] = genericReportsQueryService.getDetailsFromTradeService("proprietor" , params.tradeServiceId).toUpperCase()
		params['proprietorPosition'] = genericReportsQueryService.getDetailsFromTradeService("proprietorPosition" , params.tradeServiceId).toUpperCase()
		params['authorizedSignatory1'] = genericReportsQueryService.getDetailsFromTradeService("authorizedSignatory1" , params.tradeServiceId).toUpperCase()
		params['authorizedSignatoryPosition1'] = genericReportsQueryService.getDetailsFromTradeService("authorizedSignatoryPosition1" , params.tradeServiceId).toUpperCase()
		params['authorizedSignatory2'] = genericReportsQueryService.getDetailsFromTradeService("authorizedSignatory2" , params.tradeServiceId).toUpperCase()
		params['authorizedSignatoryPosition2'] = genericReportsQueryService.getDetailsFromTradeService("authorizedSignatoryPosition2" , params.tradeServiceId).toUpperCase()
	
		params['format'] = "pdf"

		println "params['format'] = " +params['format']
		println "RDETAILS"+params['reportDetails']
		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//LC Confirmation Regular Cash Amendment
	def runLcConfirmationRegularCashAmendment={
		def token = params.token ?: 'LC_Confirmation_Amendment'

		params['P_REPORTNAME'] = "LC_Confirmation_Amendment"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format

		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//DISCREPANCY LETTER
	def runDiscrepancyLetter={
		def token = params.token ?: 'Discrepancy_Letter'

		params['P_REPORTNAME'] = "Discrepancy_Letter"

		params['specificRptPath'] = ''

		//Follow the '/' format
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['documentNumber']= genericReportsQueryService.getDetailsFromTradeService("documentNumber", params.tradeServiceId)
		params['longName']= genericReportsQueryService.getDetailsFromTradeService("longName", params.tradeServiceId)
		params['address1']= genericReportsQueryService.getDetailsFromTradeService("address1", params.tradeServiceId)
		params['address2']= genericReportsQueryService.getDetailsFromTradeService("address2", params.tradeServiceId)
		params['currency']= genericReportsQueryService.getDetailsFromTradeService("currency", params.tradeServiceId)
		params['negotiationCurrency']= genericReportsQueryService.getDetailsFromTradeService("negotiationCurrency", params.tradeServiceId)
		params['negotiationAmount']= genericReportsQueryService.getDetailsFromTradeService("negotiationAmount", params.tradeServiceId)
		params['icNumber']= genericReportsQueryService.getDetailsFromTradeService("icNumber", params.tradeServiceId)
		params['expiredLcSwitch']= genericReportsQueryService.getDetailsFromTradeService("expiredLcSwitch", params.tradeServiceId)
		params['overdrawnForAmountSwitch']= genericReportsQueryService.getDetailsFromTradeService("overdrawnForAmountSwitch", params.tradeServiceId)
		params['overdrawnForAmount']= genericReportsQueryService.getDetailsFromTradeService("overdrawnForAmount", params.tradeServiceId)
		params['descriptionOfGoodsNotPerLcSwitch']= genericReportsQueryService.getDetailsFromTradeService("descriptionOfGoodsNotPerLcSwitch", params.tradeServiceId)
		params['documentsNotPresentedSwitch']= genericReportsQueryService.getDetailsFromTradeService("documentsNotPresentedSwitch", params.tradeServiceId)
		params['documentsNotPresented']= genericReportsQueryService.getDetailsFromTradeService("documentsNotPresented", params.tradeServiceId)
		params['othersSwitch']= genericReportsQueryService.getDetailsFromTradeService("othersSwitch", params.tradeServiceId)
		params['otherDiscrepancy']= genericReportsQueryService.getDetailsFromTradeService("others", params.tradeServiceId)
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Letter of Advise
	def runLetterOfAdvise={

		def token = params.token ?: 'Letter_of_Advise'

		params['P_REPORTNAME'] = "Letter_of_Advise"
		params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format
		
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['documentSubType1'] = genericReportsQueryService.getDetailsFromTradeService("documentSubType1",params.tradeServiceId)
		params['serviceType'] = genericReportsQueryService.getDetailsFromTradeService("serviceType",params.tradeServiceId)
		params['lcNumber'] = genericReportsQueryService.getDetailsFromTradeService("lcNumber",params.tradeServiceId)
		params['newExporterName'] = genericReportsQueryService.getDetailsFromTradeService("newExporterName",params.tradeServiceId)
		params['newExporterAddress'] = genericReportsQueryService.getDetailsFromTradeService("newExporterAddress",params.tradeServiceId)
		params['advisingBank'] = genericReportsQueryService.getDetailsFromTradeService("advisingBank",params.tradeServiceId)
		params['advisingBankLabel'] = genericReportsQueryService.getDetailsFromTradeService("advisingBankLabel",params.tradeServiceId)
		params['advisingBankAddress'] = genericReportsQueryService.getDetailsFromTradeService("advisingBankAddress",params.tradeServiceId)
		params['issuingBank'] = genericReportsQueryService.getDetailsFromTradeService("issuingBank",params.tradeServiceId)
		params['issuingBankAddress'] = genericReportsQueryService.getDetailsFromTradeService("issuingBankAddress",params.tradeServiceId)
		params['totalBankCharges'] = genericReportsQueryService.getDetailsFromTradeService("totalBankCharges",params.tradeServiceId)
		params['exportsAdvisingFee']=genericReportsQueryService.getDetailsFromTradeService("ADVISING-EXPORT",params.tradeServiceId)
		params['cableFee']=genericReportsQueryService.getDetailsFromTradeService("CABLE",params.tradeServiceId)
		
		//mt700 fields
		params['sequenceOfTotal'] = genericReportsQueryService.getDetailsFromTradeService("sequenceOfTotal",params.tradeServiceId)
		params['formOfDocumentaryCredit'] = genericReportsQueryService.getDetailsFromTradeService("formOfDocumentaryCredit",params.tradeServiceId)
		params['documentaryCreditNumber'] = genericReportsQueryService.getDetailsFromTradeService("documentaryCreditNumber",params.tradeServiceId)
		params['referenceToPreAdvice'] = genericReportsQueryService.getDetailsFromTradeService("referenceToPreAdvice",params.tradeServiceId)
		params['lcIssueDate'] = genericReportsQueryService.getDetailsFromTradeService("lcIssueDate",params.tradeServiceId)
		params['applicableRules'] = genericReportsQueryService.getDetailsFromTradeService("applicableRules",params.tradeServiceId)
		params['dateAndPlaceOfExpiry'] = genericReportsQueryService.getDetailsFromTradeService("dateAndPlaceOfExpiry",params.tradeServiceId)
		params['importerName'] = genericReportsQueryService.getDetailsFromTradeService("importerName",params.tradeServiceId)
		params['importerAddress'] = genericReportsQueryService.getDetailsFromTradeService("importerAddress",params.tradeServiceId)
		params['exporterName'] = genericReportsQueryService.getDetailsFromTradeService("exporterName",params.tradeServiceId)
		params['exporterAddress'] = genericReportsQueryService.getDetailsFromTradeService("exporterAddress",params.tradeServiceId)
		params['lcCurrency'] = genericReportsQueryService.getDetailsFromTradeService("lcCurrency",params.tradeServiceId)
		params['lcAmount'] = genericReportsQueryService.getDetailsFromTradeService("lcAmount",params.tradeServiceId)
		params['percentageCreditAmountTolerance'] = genericReportsQueryService.getDetailsFromTradeService("percentageCreditAmountTolerance",params.tradeServiceId)
		params['maximumCreditAmount'] = genericReportsQueryService.getDetailsFromTradeService("maximumCreditAmount",params.tradeServiceId)
		params['additionalAmountsCovered'] = genericReportsQueryService.getDetailsFromTradeService("additionalAmountsCovered",params.tradeServiceId)
		params['availableWithBy'] = genericReportsQueryService.getDetailsFromTradeService("availableWithBy",params.tradeServiceId)
		params['usanceTerm'] = genericReportsQueryService.getDetailsFromTradeService("usanceTerm",params.tradeServiceId)
		params['drawee'] = genericReportsQueryService.getDetailsFromTradeService("drawee",params.tradeServiceId)
		params['mixedPaymentDetails'] = genericReportsQueryService.getDetailsFromTradeService("mixedPaymentDetails",params.tradeServiceId)
		params['deferredPaymentDetails'] = genericReportsQueryService.getDetailsFromTradeService("deferredPaymentDetails",params.tradeServiceId)
		params['partialShipments'] = genericReportsQueryService.getDetailsFromTradeService("partialShipments",params.tradeServiceId)
		params['transshipment'] = genericReportsQueryService.getDetailsFromTradeService("transshipment",params.tradeServiceId)
		params['placeOfTakingInCharge'] = genericReportsQueryService.getDetailsFromTradeService("placeOfTakingInCharge",params.tradeServiceId)
		params['portOfLoading'] = genericReportsQueryService.getDetailsFromTradeService("portOfLoading",params.tradeServiceId)
		params['placeOfFinalDestination'] = genericReportsQueryService.getDetailsFromTradeService("placeOfFinalDestination",params.tradeServiceId)
		params['latestDateOfShipment'] = genericReportsQueryService.getDetailsFromTradeService("latestDateOfShipment",params.tradeServiceId)
		params['shipmentPeriod'] = genericReportsQueryService.getDetailsFromTradeService("shipmentPeriod",params.tradeServiceId)
		params['charges'] = genericReportsQueryService.getDetailsFromTradeService("charges",params.tradeServiceId)
		params['periodForPresentation'] = genericReportsQueryService.getDetailsFromTradeService("periodForPresentation",params.tradeServiceId)
		params['confirmationInstructions'] = genericReportsQueryService.getDetailsFromTradeService("confirmationInstructions",params.tradeServiceId)
		params['reimbursingBank'] = genericReportsQueryService.getDetailsFromTradeService("reimbursingBank",params.tradeServiceId)
		params['adviseThroughBank'] = genericReportsQueryService.getDetailsFromTradeService("adviseThroughBank",params.tradeServiceId)
		params['senderToReceiverInformation'] = genericReportsQueryService.getDetailsFromTradeService("senderToReceiverInformation",params.tradeServiceId)

		def descriptionOfGoodsAndOrServices = genericReportsQueryService.getDetailsFromTradeService("descriptionOfGoodsAndOrServices",params.tradeServiceId)
		def documentsRequired = genericReportsQueryService.getDetailsFromTradeService("documentsRequired",params.tradeServiceId)
		def additionalConditions = genericReportsQueryService.getDetailsFromTradeService("additionalConditions",params.tradeServiceId)
		def instructionsToThePayingBank = genericReportsQueryService.getDetailsFromTradeService("instructionsToThePayingBank",params.tradeServiceId)

		params['descriptionOfGoodsAndOrServices'] = descriptionOfGoodsAndOrServices ? ("+ " + descriptionOfGoodsAndOrServices.substring(1, descriptionOfGoodsAndOrServices.length()).replaceAll("\\+", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\\+ ")) : ""
		params['documentsRequired'] = documentsRequired ? ("+ " + documentsRequired.substring(1, documentsRequired.length()).replaceAll("\\+", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\\+ ")) : ""
		params['additionalConditions'] = additionalConditions ? ("+ " + additionalConditions.substring(1, additionalConditions.length()).replaceAll("\\+", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\\+ ")) : ""
		params['instructionsToThePayingBank'] = instructionsToThePayingBank ? ("+ " + instructionsToThePayingBank.substring(1, instructionsToThePayingBank.length()).replaceAll("\\+", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\\+ ")) : ""

		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//Transmittal Letter
	def runTransmittalLetterReport={
		def token = params.token ?: 'Transmittal_Letter'

		params['P_REPORTNAME'] = "Transmittal_Letter"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['longName']=genericReportsQueryService.getDetailsFromTradeService("longName",params.tradeServiceId)
		params['address1']=genericReportsQueryService.getDetailsFromTradeService("address1",params.tradeServiceId)
		params['address2']=genericReportsQueryService.getDetailsFromTradeService("address2",params.tradeServiceId)
		params['applicantName']=genericReportsQueryService.getDetailsFromTradeService("applicantName",params.tradeServiceId)
		params['applicantAddress']=genericReportsQueryService.getDetailsFromTradeService("applicantAddress",params.tradeServiceId)
		params['importerName']=genericReportsQueryService.getDetailsFromTradeService("importerName",params.tradeServiceId)
		params['importerAddress']=genericReportsQueryService.getDetailsFromTradeService("importerAddress",params.tradeServiceId)
		params['importerNameTo']=genericReportsQueryService.getDetailsFromTradeService("importerNameTo",params.tradeServiceId)
		params['importerAddressTo']=genericReportsQueryService.getDetailsFromTradeService("importerAddressTo",params.tradeServiceId)
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//Letter of Transfer
	def runLetterOfTransfer={

		def token = params.token ?: 'Letter_of_Transfer'

		params['P_REPORTNAME'] = "Letter_of_Transfer"
		params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['longName']=genericReportsQueryService.getDetailsFromTradeService("longName",params.tradeServiceId)
		params['importerName']=genericReportsQueryService.getDetailsFromTradeService("importerName",params.tradeServiceId)
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}
	
	//MT Message Report
	def runMtMessageReport = {
		def token = params.token ?: 'Mt_Message'

		params['P_REPORTNAME'] = "Mt_Message"
		params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['format'] = "pdf"
		
		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	def runOutgoingMtReport = {
		def token = params.token ?: 'Outgoing_MTs'

		params['P_REPORTNAME'] = "Outgoing_MTs"
		params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format
		
		params['mtOutgoingDate'] = new SimpleDateFormat("MM/dd/yyyy").parse(params.telecomReportDate)

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['format'] = "pdf"
		
		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	private Date getPreviousBankingDate(String onlineReportDate) {
		Date currentBusinessDate = DateUtils.parse(onlineReportDate);
		
		Calendar currentCalendar = Calendar.getInstance();
		currentCalendar.setTime(currentBusinessDate);
		

		if (currentCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			currentCalendar.add(Calendar.DATE, -2);
		} else if (currentCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
			currentCalendar.add(Calendar.DATE, -3);
		} else {
			currentCalendar.add(Calendar.DATE, -1);
		}
		
		return currentCalendar.getTime();
		
	}
		
	//Trams Report
	def runTramsReport = {
		def token = params.token ?: 'TRAMS'

		params['P_REPORTNAME'] = "TRAMS"
		params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Other Imports'       //Follow the '/' format

		params['dateGenerated'] = new SimpleDateFormat("MM/dd/yyyy").format(getPreviousBankingDate(params.onlineReportDate))
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['format'] = "csv"
		
		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
 
		println params['userId']

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
		
		
		
		
		String fileName = getTramsFileName(params.onlineReportDate)
		//String fileName = getTramsFileName("12/06/2013")

		ext_params["fileName"] = fileName
		
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	
	private Date getNextBusinessDay(String onlineReportDate) {
		Calendar collectionDateCalendar = Calendar.getInstance()
		collectionDateCalendar.setTime(new Date(onlineReportDate))
		
		collectionDateCalendar.add(Calendar.DATE, 1)
		
		if (collectionDateCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
			collectionDateCalendar.add(Calendar.DATE, 2)
		}
		
		if (collectionDateCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			collectionDateCalendar.add(Calendar.DATE, 1)
		}
		
		
		
		Calendar remittanceDateCalendar = Calendar.getInstance()
		remittanceDateCalendar.setTime(collectionDateCalendar.getTime())
		
		return remittanceDateCalendar.getTime();
	}
	
	private String getTramsFileName(String onlineReportDate) {
		
		
		SimpleDateFormat reportDateFormat = new SimpleDateFormat("MMddyyyy")
		
		return "TBOC" + reportDateFormat.format(getNextBusinessDay(onlineReportDate)) + reportDateFormat.format(new Date(onlineReportDate)) + "C.csv"
		
	}

	//Outstanding Bank Guaranty
	def runOutstandingBankGuarantyReport={
		def token = params.token ?: 'Outstanding_Bank_Guaranty'

		params['P_REPORTNAME'] = "Outstanding_Bank_Guaranty"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Other Imports/'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MOUBG")
		params['processingUnitCode'] = params.branchUnitCode
		params['onlineReportDate'] = new SimpleDateFormat("MM/dd/yyyy").format(new Date(params.onlineReportDate))
		params['format'] = "pdf"
		
		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//Outstanding Marginal Deposits
	def runOutstandingMarginalDepositsReport={
		def token = params.token ?: 'Outstanding_Marginal_Deposits'

		params['P_REPORTNAME'] = "Outstanding_Marginal_Deposits"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Other Imports'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MOUMD")
		params['documentNumber'] = params.documentNumber
		params['format'] = "pdf"
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}
	
	private String getQuarter() {
		Calendar currentDate = Calendar.getInstance()
		
		def currentMonth = currentDate.get(Calendar.MONTH)
		
		if (currentMonth.value <= 2) {
			return "January - March"
		} else if (currentMonth.value > 2 && currentMonth.value <= 5) {
			return "April - June"
		} else if (currentMonth.value > 5 && currentMonth.value <= 8) {
			return "July - September"
		} else if (currentMonth.value > 8 && currentMonth.value <= 11) {
			return "October - December"
		}
	}

	//ARVIN
	def runQuarterlyReportOnForeignStandybyLcsOpened = {

		def token = params.token ?: 'Quarterly_Report_on_Foreign_Standby_LCs_Opened'

		params['P_REPORTNAME'] = "Quarterly_Report_on_Foreign_Standby_LCs_Opened"

		params['specificRptPath'] = 'BATCHES/FXLC'

		//Follow the '/' format
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')

		params['reportId'] = reportIdService.generateReportId("QFXSO")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['currentQuarterYear'] = new SimpleDateFormat("yyyy-MM-dd").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'
		
		// get current date
		params["MonthQuarter"] = getQuarter()
		
		
		println "currentQuarterYear: " +params['currentQuarterYear']
		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runYearEndReportOnForeignLcsOpenedPerCountry = {

		def token = params.token ?: 'Year_End_Report_on_Foreign_LCS_Opened_per_Country'

		params['P_REPORTNAME'] = "Year_End_Report_on_Foreign_LCS_Opened_per_Country"

		params['specificRptPath'] = 'BATCHES/FXLC'

		//Follow the '/' format
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')

		params['yearEndDate'] = params.onlineReportDate
		params['dateNow'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['reportId'] = reportIdService.generateReportId("YFXOC")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runYearEndReportOnForeignLcsOpenedPerAdvisingBank = {

		def token = params.token ?: 'Year_End_Report_on_Foreign_LCS_Opened_per_Advising_Bank'

		params['P_REPORTNAME'] = "Year_End_Report_on_Foreign_LCS_Opened_per_Advising_Bank"

		params['specificRptPath'] = 'BATCHES/FXLC'

		//Follow the '/' format
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')

		params['yearEndDate'] = params.onlineReportDate
		params['dateNow'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['reportId'] = reportIdService.generateReportId("YFOAB")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runYearEndReportOnForeignLcsOpenedPerConfirmingBank = {

		def token = params.token ?: 'Year_End_Report_on_Foreign_LCS_Opened_per_Confirming_Bank'

		params['P_REPORTNAME'] = "Year_End_Report_on_Foreign_LCS_Opened_per_Confirming_Bank"

		params['specificRptPath'] = 'BATCHES/FXLC'

		//Follow the '/' format
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')

		params['yearEndDate'] = params.onlineReportDate
		params['dateNow'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['reportId'] = reportIdService.generateReportId("YFOCB")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//PETER
	//DOCUMENTS CHECK LIST...
	def runDocumentsCheckListReport = {
		def token = params.token ?: 'Documents_Check_List'

		params['P_REPORTNAME'] = "Documents_Check_List"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = '' //root directory

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['longName']=genericReportsQueryService.getDetailsFromTradeService("longName",params.tradeServiceId)
		params['address1']=genericReportsQueryService.getDetailsFromTradeService("address1",params.tradeServiceId)
		params['address2']=genericReportsQueryService.getDetailsFromTradeService("address2",params.tradeServiceId)
		params['importerName']=genericReportsQueryService.getDetailsFromTradeService("importerName",params.tradeServiceId)
		params['importerAddress']=genericReportsQueryService.getDetailsFromTradeService("importerAddress",params.tradeServiceId)
		params['format'] = "pdf"

		println ""
		println "params: "+params
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//PDDTS
	def runPddts={
		def token = params.token ?: 'PDDTS_Report'

		params['P_REPORTNAME'] = "PDDTS_Report"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''      //root directory

		params['recipientOfPddtsReport'] = ''
		params['referenceNumber'] = params.documentNumber
		params['accountNo'] = params.accountNumber
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//PAYMENT SUMMARY
	def runPaymentSummaryReport = {

		def token = params.token ?: 'Payment_Summary'

		if(params.paymentSummary == 'A'){
			params['P_REPORTNAME'] = "Payment_Summary"
		}
		else if(params.paymentSummary == 'B'){
			params['P_REPORTNAME'] = "Payment_Summary_Without_Cash"
			token = 'Payment_Summary_Without_Cash'
		}
		else if(params.paymentSummary == 'MD'){
			params['P_REPORTNAME'] = "Payment_Summary_Other_Imports"
			token = 'Payment_Summary_Other_Imports'
		}
		else if(params.paymentSummary == 'WC'){
			params['P_REPORTNAME'] = "Payment_Summary_Without_Charges"
			token = 'Payment_Summary_Without_Charges'
		}else if(params.paymentSummary == 'WP'){
			params['P_REPORTNAME'] = "Payment_Summary_With_Proceeds"
			token = 'Payment_Summary_With_Proceeds'
		}

		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = '' //root directory

		if(params.paymentSummary != 'MD'){
			def currency = params.currency
			def thirdToUsd = genericReportsQueryService.getDetailsFromTradeService(currency+"-USD_text_special_rate",params.tradeServiceId)
			def thirdToUsdLength
			def thirdToUsdSpecialRate
			def usdToPhp = genericReportsQueryService.getDetailsFromTradeService("USD-PHP_text_special_rate",params.tradeServiceId)
			def usdToPhpLength
			def usdToPhpSpecialRate
			
			if(thirdToUsd != "") {
				thirdToUsdLength = new BigDecimal(thirdToUsd).stripTrailingZeros().toString().length()
				if(thirdToUsdLength <= 5) {
					thirdToUsdSpecialRate = new DecimalFormat("#,##0.00").format(new BigDecimal(thirdToUsd))
				} else {
					thirdToUsdSpecialRate = new BigDecimal(thirdToUsd).stripTrailingZeros()
				}
			} else {
				thirdToUsdSpecialRate = ""
			}
			
			if(usdToPhp != "") {
				usdToPhpLength = new BigDecimal(usdToPhp).stripTrailingZeros().toString().length()
				if(usdToPhpLength <= 5) {
					usdToPhpSpecialRate = new DecimalFormat("#,##0.00").format(new BigDecimal(usdToPhp))
				} else {
					usdToPhpSpecialRate = new BigDecimal(usdToPhp).stripTrailingZeros()
				}
			} else {
				usdToPhpSpecialRate = ""
			}
			
			params['THIRD-USD_text_special_rate']= thirdToUsdSpecialRate
			params['USD-PHP_text_special_rate']= usdToPhpSpecialRate
			params['urr']= genericReportsQueryService.getDetailsFromTradeService("urr",params.tradeServiceId)
			params['USD-PHP_urr']= genericReportsQueryService.getDetailsFromTradeService("USD-PHP_urr",params.tradeServiceId)

		}
		
		params['currency'] = params.currency
		
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['speechBubble'] = servletContext.getRealPath('reports/img/speech_bubble.jpg')
		params['longName']= genericReportsQueryService.getDetailsFromTradeService("longName",params.tradeServiceId)
		params['cifName']=genericReportsQueryService.getDetailsFromTradeService("cifName",params.tradeServiceId)
		params['importerName']= genericReportsQueryService.getDetailsFromTradeService("importerName",params.tradeServiceId)
		params['exporterName']= genericReportsQueryService.getDetailsFromTradeService("exporterName",params.tradeServiceId)
		params['preparedBy'] = params.preparedBy
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder']
		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//Export Negotiation Advice
	def runExportNegotiationAdvice={
		println "qwerty" + params.documentType
		
		def token = params.token ?: 'Export_Negotiation_Advice'
		
		params['P_REPORTNAME'] = 'Export_Negotiation_Advice'
		params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''       //Follow the '/' format
		
		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['documentType']=genericReportsQueryService.getDetailsFromTradeService("documentType",params.tradeServiceId)
		params['issuingBankName']=genericReportsQueryService.getDetailsFromTradeService("issuingBankName",params.tradeServiceId)
		params['issuingBankAddress']=genericReportsQueryService.getDetailsFromTradeService("issuingBankAddress",params.tradeServiceId)
		params['collectingBankName']=genericReportsQueryService.getDetailsFromTradeService("collectingBankName",params.tradeServiceId)
		params['collectingBankAddress']=genericReportsQueryService.getDetailsFromTradeService("collectingBankAddress",params.tradeServiceId)
		params['negoAdviceAddressee']=genericReportsQueryService.getDetailsFromTradeService("negoAdviceAddressee",params.tradeServiceId)
		params['negoAdviceAddresseeAddress']=genericReportsQueryService.getDetailsFromTradeService("negoAdviceAddresseeAddress",params.tradeServiceId)
		params['paymentMode']=genericReportsQueryService.getDetailsFromTradeService("paymentMode",params.tradeServiceId)
		params['lcNumber']=genericReportsQueryService.getDetailsFromTradeService("lcNumber",params.tradeServiceId)
		params['sellerName']=genericReportsQueryService.getDetailsFromTradeService("sellerName",params.tradeServiceId)
		params['drawee']=genericReportsQueryService.getDetailsFromTradeService("drawee",params.tradeServiceId)
		params['currency']=genericReportsQueryService.getDetailsFromTradeService("currency",params.tradeServiceId)
		params['amount']=genericReportsQueryService.getDetailsFromTradeService("amount",params.tradeServiceId)
		params['buyerName']= (params.drawee == "" || params.drawee.equalsIgnoreCase("undefined")) ? genericReportsQueryService.getDetailsFromTradeService("buyerName",params.tradeServiceId) : params.drawee
		params['buyerAddress']=genericReportsQueryService.getDetailsFromTradeService("buyerAddress",params.tradeServiceId)
		params['draftAmount'] = params['amount'] ? new BigDecimal(params['amount'].replaceAll(",","")) : BigDecimal.ZERO
		params['approver'] = params.authorizedSign + "\n" + params.authorizedSignPosition
		params['format'] = "pdf"
		
		println "Report content: "+params
		
		def paramDocEnclosed = []
		if (params['documentsEnclosedList']) {
			params['documentsEnclosedList'].split("\\|").each {
				paramDocEnclosed << JsonUtil.parseToMap(it)
			}
		}
		
		List reportDataList = new ArrayList()
		if (!paramDocEnclosed.isEmpty()) {
			println "paramDocEnclosed : " + paramDocEnclosed
			paramDocEnclosed.each {
				DocumentsEnclosedDTO documentsEnclosed = new DocumentsEnclosedDTO(it.documentName, it.original1, it.original2, it.duplicate1, it.duplicate2)
				reportDataList << documentsEnclosed
			}
		}
		
		def paramInstructions = []
		params['instructions'] = []
		if (params['instructionsList']) {
			println "><>< " + params['instructionsList'] + " ><><"
			params['instructionsList'].split("\\|").each {
				println ">> " + it
				println "<< " + JsonUtil.parseToMap(it)
				paramInstructions << JsonUtil.parseToMap(it)
			}
		}

		if (!paramInstructions.isEmpty()) {
			println "paramInstructions : " + paramInstructions
			paramInstructions.each {
				InstructionsDTO instruction = new InstructionsDTO(it.instruction.toString().replace("\\n", "\n"))
				params['instructions'] << instruction
			}
		}
		
		println ""
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
		
		println params['userId']
		
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		
		ext_params['token'] = token
		
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
			}
		
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
		
//		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
		
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	
	}

	//BANK CERTIFICATION
	def runBankCertication={
		def token = params.token ?: 'Bank_Certification'

		params['P_REPORTNAME'] = "Bank_Certification"

		params['specificRptPath'] = '' //root directory

		params['clientName'] = params.clientName
		params['dateOfTransactionFrom'] = params.dateOfTransactionFrom
		params['dateOfTransactionTo'] = params.dateOfTransactionTo
		//params['year'] = '' //waiting for reports clarification
		params['authorizedSignatory'] = params.authorizedSignatory
		params['authorizedSignatoryPosition'] = params.authorizedSignatoryPosition
		//params['department'] = '' //waiting for reports clarification
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}


	//DM non lcs Nego for the year classified by top 30 importer and other local bank
	def runDmNonLcsNegoForTheYear = {
		def token = params.token ?: 'Dm_Non_Lcs_Negotiated_For_The_Year_Classified_By_Top_30_Importer_And_Other_Local_Bank'

		params['P_REPORTNAME'] = 'Dm_Non_Lcs_Negotiated_For_The_Year_Classified_By_Top_30_Importer_And_Other_Local_Bank'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Non-LC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MDN30")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['month'] = new SimpleDateFormat("MMMMM").format(new Date(params.onlineReportDate))
		params['year'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date())
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		// ext_params['SUBREPORT_DIR'] = new File(ext_params['reportFolder']).absolutePath + "/"
		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//Fx non lcs Nego for the year classified by top 30 importer and other local bank
	def runFxNonLcsNegoForTheYear={
//        RatesRepository ratesRepository = new RatesRepository(silverlakeService.getURR());

		println "runFxNonLcsNegoForTheYear"

		def token = params.token ?: 'Fx_Non_Lcs_Negotiated_For_The_Year_Classified_By_Top_30_Importer_And_Remitting_Bank'

		params['P_REPORTNAME'] = 'Fx_Non_Lcs_Negotiated_For_The_Year_Classified_By_Top_30_Importer_And_Remitting_Bank'
		params['specificRptPath'] = 'BATCHES/Non-LC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MFN30")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['month'] = new SimpleDateFormat("MMMMM").format(new Date(params.onlineReportDate))
		params['year'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'
//		params['rates'] = ratesRepository

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['SUBREPORT_DIR'] = ext_params['reportFolder'] + "/"

		ext_params['token'] = token
//        ext_params['rates'] = ratesRepository;

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
//		println "params['rates']: "+params['rates']

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//Outstanding Inward Bills For Collection Dm Dp Per Currency
	def runOutstandingInwardBillsForCollectionDmDaDpPerCurrency={
		def token = params.token ?: 'Outstanding_Inward_Bills_For_Collection_Dm_Dp_Per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Inward_Bills_For_Collection_Dm_Dp_Per_Currency'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Non-LC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MOIDM")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['DateToday'] = params.onlineReportDate
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//Outstanding Inward Bills For Collection FX Da Dp Per Currency
	def runOutstandingInwardBillsForCollectionFxDaDpPerCurrency={
		def token = params.token ?: 'Outstanding_Inward_Bills_For_Collection_Fx_Da_Dp_Oa_Dr_Per_Currency'

		params['P_REPORTNAME'] = 'Outstanding_Inward_Bills_For_Collection_Fx_Da_Dp_Oa_Dr_Per_Currency'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Non-LC'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MOIFX")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['dateNow'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['DateToday'] = params.onlineReportDate
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

//batches/export
	//Export Nego for the Month per Client
	def runExportNegofortheMonthperClient={
		def token = params.token ?: 'Export_Negotiations_for_the_Month_per_Client'

		params['P_REPORTNAME'] = 'Export_Negotiations_for_the_Month_per_Client'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Export'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MEXNC")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['reportDate'] = new SimpleDateFormat("MMMMM, yyyy").format(new Date(params.onlineReportDate))
		params['month'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['year'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}

	//Export Negoper Collecting Bank
	def runexportNegoperCollectingBank={
		def token = params.token ?: 'Export_Negotiations_per_Collecting_Bank'

		params['P_REPORTNAME'] = 'Export_Negotiations_per_Collecting_Bank'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Export'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MEXNB")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitNameparams
		params['reportDate'] = new SimpleDateFormat("MMMMM, yyyy").format(new Date(params.onlineReportDate))
		params['month'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['year'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}
	//Export Payments for the Month per Client
	def runExportPaymentsfortheMonthperClient={
		def token = params.token ?: 'Export_Payments_for_the_Month_per_Client'

		params['P_REPORTNAME'] = 'Export_Payments_for_the_Month_per_Client'
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'BATCHES/Export'       //Follow the '/' format

		params['reportId'] = reportIdService.generateReportId("MEXPC")
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		params['reportDate'] = new SimpleDateFormat("MMMMM, yyyy").format(new Date(params.onlineReportDate))
		params['month'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['year'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = 'pdf'

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)

	}


	//CERTIFICATION
	def runCertificationReport = {		//for Reports with no Fields Data from DataBase

		def token = params.token ?: 'Certification'

		params['P_REPORTNAME'] = "Certification"
		params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = ''

		params['logo'] = servletContext.getRealPath('reports/img/ucpb_report_logo.jpg')
		params['currency']= genericReportsQueryService.getDetailsFromTradeService("currency",params.tradeServiceId)
		params['format'] = "pdf"

		println ""
		println "params: "+params

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true


		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		ext_params['token'] = token

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	//Daily Summary of Accounting Entries
	def runDailySummaryOfAccountingEntries={
		
		def token = ""
		if(params.reportType == "exception") {
			token = params.token ?: 'Daily_Exception_Report_of_Accounting_Entries'
			params['P_REPORTNAME'] = 'Daily_Exception_Report_of_Accounting_Entries'
			params['reportId'] = reportIdService.generateReportId("DDEAE")
		} else {
			token = params.token ?: 'Daily_Summary_of_Accounting_Entries'
			params['P_REPORTNAME'] = 'Daily_Summary_of_Accounting_Entries'
			params['reportId'] = reportIdService.generateReportId("DDSAE")
		}
		
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format
		params['dateGenerated'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode			//iba pinangalan ni Eric - see online_reports_popup.j)
		params['format'] = "pdf"


		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['token'] = token
		ext_params['inv'] = params.inv

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}


	//Trade Services AMLA Reported Transactions
	def runTradeServicesAMLAReportedTransactions={
		def token = params.token ?: 'Trade_Services_AMLA_Reported_Transactions'

		params['P_REPORTNAME'] = 'Trade_Services_AMLA_Reported_Transactions'
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format
		params['brUnitCode'] = params.branchUnitCode			//iba pinangalan ni Eric - see online_reports_popup.js

		params['dateGenerated'] = params.onlineReportDate

		params['reportId'] = reportIdService.generateReportId("DAMLA") //..?
		params['currentMonth'] = new SimpleDateFormat("M").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['format'] = "pdf"

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['token'] = token
		ext_params['inv'] = params.inv

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}


	//Volumetrics
	def runVolumetrics={
		def token = params.token ?: 'Volumetrics_Report'

		params['P_REPORTNAME'] = 'Volumetrics_Report'
		params['specificRptPath'] = 'BATCHES/Other Trade'       //Follow the '/' format
		params['brUnitCode'] = params.branchUnitCode			//iba pinangalan ni Eric - see online_reports_popup.js
		params['currentMonth'] = new SimpleDateFormat("MM").format(new Date(params.onlineReportDate))
		params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate))
		params['dateGenerated'] = params.onlineReportDate

		params['reportId'] = reportIdService.generateReportId("MDSAE")
		params['format'] = "pdf"

		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true

		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"tfs"
		ext_params['name'] = params['name']?:"tfs"
		ext_params['schconid'] = params['schconid']?:"derby"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['token'] = token

		
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList

		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	
	//Outstanding Export Negotiation Foreign
	def runOutstandingExportNegotiationForeign = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'Outstanding_Export_Negotiations_Foreign'
		
		
		params['P_REPORTNAME'] = token
		params['specificRptPath'] = 'BATCHES/Export'       //Follow the '/' format
		params['forYear']=new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate));
		params['forMonth']=new SimpleDateFormat("MMMMM").format(new Date(params.onlineReportDate)).toUpperCase();
		params['dateGenerated'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		//params['currentMonth'] = new SimpleDateFormat("M").format(new Date())
		//params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date())
		
		params['reportId'] = reportIdService.generateReportId("OENFX")
		params['format'] = "pdf"
		
		println ""
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
		
		println params['userId']
		
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		
		ext_params['token'] = token
		
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
		
		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
		
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
		
	//Outstanding Export Negotiation Domestic
	def runOutstandingExportNegotiationDomestic = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'Outstanding_Export_Negotiations_Domestic'
		
		
		params['P_REPORTNAME'] = token
		params['specificRptPath'] = 'BATCHES/Export'       //Follow the '/' format
		params['forYear']=new SimpleDateFormat("yyyy").format(new Date(params.onlineReportDate));
		params['forMonth']=new SimpleDateFormat("MMMMM").format(new Date(params.onlineReportDate)).toUpperCase();
		params['dateGenerated'] = params.onlineReportDate
		params['brUnitCode'] = params.branchUnitCode
		params['brUnitName'] = params.branchUnitName
		//params['currentMonth'] = new SimpleDateFormat("M").format(new Date())
		//params['currentYear'] = new SimpleDateFormat("yyyy").format(new Date())
		
		params['reportId'] = reportIdService.generateReportId("OENDM")
		params['format'] = "pdf"
		
		println ""
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
		
		println params['userId']
		
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		
		ext_params['token'] = token
		
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
		
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
		
		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
		
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runAdbBureauOfCustomsCollection = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'ADB_of_Bureau_of_Customs_Collection'
			
		params['P_REPORTNAME'] = "ADB_of_Bureau_of_Customs_Collection"
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
	
		params['reportId'] = reportIdService.generateReportId("ABOCC")
		params['format'] = "pdf"
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
	
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}



	def runScheduleOfMarginalDeposit = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'Schedule_of_Marginal_Deposit'
			
		params['P_REPORTNAME'] = "Schedule_of_Marginal_Deposit"
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
	
		params['reportId'] = reportIdService.generateReportId("SCOMD")
		params['format'] = "pdf"
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
	
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runRebatesFromCorresBankDataEntry = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'Schedule_5_Acquisition'
			
		params['P_REPORTNAME'] = "Rebates_from_Corres_Bank_Data_Entry"
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
	
		params['reportId'] = reportIdService.generateReportId("RCBDE")
		params['currentDate'] = params.onlineReportDate
		params['format'] = "xls"
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['rptFilename'] = 'Schedule_5_Acquisition'
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
	
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runActualCorresChargesDataEntry = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'Schedule_5_Disposition'
			
		params['P_REPORTNAME'] = "Actual_Corres_Charges_Data_Entry"
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
	
		params['reportId'] = reportIdService.generateReportId("ACCDE")
		params['currentDate'] = params.onlineReportDate
		params['format'] = "xls"
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['rptFilename'] = 'Schedule_5_Disposition'
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params

		List reportDataList = actualCorresChargeService.getCorresChargesDataList(params.currentDate)
		println "reportDataList: "+reportDataList
	
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
			
	def runFxForm1Schedule3 = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: (params.cbReportType == 'report' ? 'Schedule_3' : 'TRD03')
			
		if(params.cbReportType == 'report') {
			params['P_REPORTNAME'] = "Schedule_3_Report"
			params['reportId'] = reportIdService.generateReportId("FFS03")
			params['format'] = "pdf"
		} else {
			params['P_REPORTNAME'] = "Schedule_3_Text"
			params['format'] = "txt"
		}
		
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
		params['currentDate'] = params.onlineReportDate
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['rptFilename'] = params.cbReportType == 'report' ? 'Schedule_3' : 'TRD03'
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		// below data list will use jasper query instead the one in service 
		// List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		List reportDataList = fxForm1Schedule3Service.getFxForm1Schedule3DataList(params.onlineReportDate, params.cbReportType)
		println "reportDataList: "+reportDataList
	
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	
	def runFxForm1Schedule4 = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: (params.cbReportType == 'report' ? 'Schedule_4' : 'TRD04')
			
		if(params.cbReportType == 'report') {
			params['P_REPORTNAME'] = "Schedule_4_Report"
			params['reportId'] = reportIdService.generateReportId("FFS04")
			params['format'] = "pdf"
		} else {
			params['P_REPORTNAME'] = "Schedule_4_Text"
			params['format'] = "txt"
		}
		
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
		params['currentDate'] = params.onlineReportDate
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['rptFilename'] = params.cbReportType == 'report' ? 'Schedule_4' : 'TRD04'
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		// List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		List reportDataList = fxForm1Schedule4Service.getFxForm1Schedule4DataList(params.onlineReportDate, params.cbReportType)
		println "reportDataList: "+reportDataList
		
		if(params.cbReportType == 'report') { 
			generateGenericReport(reportDataList, ext_params["format"], ext_params)
		} else {			
			Map<String, Object> dbfParamsMap = new HashMap<>()
			dbfParamsMap.put("fileName", "LOANSRVD")
			dbfParamsMap.put("reportDate", new Date(params.onlineReportDate))
			dbfParamsMap.put("scheduleNo", "04")
			
			List fieldHeaderList = fxForm1Schedule4Service.getFieldHeaders()
			def reportName = fxForm1DbfWriterService.generateDbfFile(reportDataList, fieldHeaderList, dbfParamsMap)
			successList.add(reportName)
		}
	}
	
	def runFxForm1Schedule5 = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: (params.cbReportType == 'report' ? 'Schedule_5' : 'TRD05')
			
		if(params.cbReportType == 'report') {
			params['P_REPORTNAME'] = "Schedule_5_Report"
			params['reportId'] = reportIdService.generateReportId("FFS05")
			params['format'] = "pdf"
		} else {
			params['P_REPORTNAME'] = "Schedule_5_Text"
			params['format'] = "txt"
		}
		
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
		params['currentDate'] = params.onlineReportDate
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['rptFilename'] = params.cbReportType == 'report' ? 'Schedule_5' : 'TRD05'
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		List reportDataList = fxForm1Schedule5Service.getFxForm1Schedule5DataList(params.onlineReportDate, params.cbReportType)
		println "reportDataList: "+reportDataList
		
		if(params.cbReportType == 'report') {
			generateGenericReport(reportDataList, ext_params["format"], ext_params)
		} else {
			Map<String, Object> dbfParamsMap = new HashMap<>()
			dbfParamsMap.put("fileName", "CURRENTC")
			dbfParamsMap.put("reportDate", new Date(params.onlineReportDate))
			dbfParamsMap.put("scheduleNo", "05")
			
			List fieldHeaderList = fxForm1Schedule5Service.getFieldHeaders()
			def reportName = fxForm1DbfWriterService.generateDbfFile(reportDataList, fieldHeaderList, dbfParamsMap)
			successList.add(reportName)
		}
	}

	def runFxForm1Schedule7 = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: (params.cbReportType == 'report' ? 'Schedule_7' : 'TRD07')
			
		if(params.cbReportType == 'report') {
			params['P_REPORTNAME'] = "Schedule_7_Report"
			params['reportId'] = reportIdService.generateReportId("FFS07")
			params['format'] = "pdf"
		} else {
			params['P_REPORTNAME'] = "Schedule_7_Text"
			params['format'] = "txt"
		}
		
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
		params['currentDate'] = params.onlineReportDate
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['rptFilename'] = params.cbReportType == 'report' ? 'Schedule_7' : 'TRD07'
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		// List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		List reportDataList = fxForm1Schedule7Service.getFxForm1Schedule7DataList(params.onlineReportDate)
		
		println "reportDataList: "+reportDataList
	
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
	
	def runFxForm1Schedule9 = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: (params.cbReportType == 'report' ? 'Schedule_9' : 'TRD09')
			
		if(params.cbReportType == 'report') {
			params['P_REPORTNAME'] = "Schedule_9_Report"
			params['reportId'] = reportIdService.generateReportId("FFS09")
			params['format'] = "pdf"
		} else {
			params['P_REPORTNAME'] = "Schedule_9_Text"
			params['format'] = "txt"
		}
		
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
		params['currentDate'] = params.onlineReportDate
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['rptFilename'] = params.cbReportType == 'report' ? 'Schedule_9' : 'TRD09'
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		// List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		List reportDataList = fxForm1Schedule9Service.getFxForm1Schedule9DataList(params.onlineReportDate)
		println "reportDataList: "+reportDataList
			
		// comment below code to generate pdf and dbf files
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
		
		// uncomment below code to generate pdf and dbf files
//		if(params.cbReportType == 'report') {
//			generateGenericReport(reportDataList, ext_params["format"], ext_params)
//		} else {
//			Map<String, Object> dbfParamsMap = new HashMap<>()
//			dbfParamsMap.put("fileName", "EXPORTB")
//			dbfParamsMap.put("reportDate", new Date(params.onlineReportDate))
//			dbfParamsMap.put("scheduleNo", "09")
//			
//			List fieldHeaderList = fxForm1Schedule9Service.getFieldHeaders()
//			def reportName = fxForm1DbfWriterService.generateDbfFile(reportDataList, fieldHeaderList, dbfParamsMap)
//			successList.add(reportName)
//		}
	}

	def runFxForm1Schedule10 = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: (params.cbReportType == 'report' ? 'Schedule_10' : 'TRD10')
			
		if(params.cbReportType == 'report') {
			params['P_REPORTNAME'] = "Schedule_10_Report"
			params['reportId'] = reportIdService.generateReportId("FFS10")
			params['format'] = "pdf"
		} else {
			params['P_REPORTNAME'] = "Schedule_10_Text"
			params['format'] = "txt"
		}
		
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
		params['currentDate'] = params.onlineReportDate
		
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['rptFilename'] = (params.cbReportType == 'report' ? 'Schedule_10' : 'TRD10')
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		// List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		List reportDataList = fxForm1Schedule10Service.getFxForm1Schedule10DataList(params.onlineReportDate)
		println "reportDataList: "+reportDataList
	
		// comment below code to generate pdf and dbf files
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
		
		// uncomment below code to generate pdf and dbf files
//		if(params.cbReportType == 'report') {
//			generateGenericReport(reportDataList, ext_params["format"], ext_params)
//		} else {
//			Map<String, Object> dbfParamsMap = new HashMap<>()
//			dbfParamsMap.put("fileName", "LCDONEWB")
//			dbfParamsMap.put("reportDate", new Date(params.onlineReportDate))
//			dbfParamsMap.put("scheduleNo", "10")
//			
//			List fieldHeaderList = fxForm1Schedule10Service.getFieldHeaders()
//			def reportName = fxForm1DbfWriterService.generateDbfFile(reportDataList, fieldHeaderList, dbfParamsMap)
//			successList.add(reportName)
//		}
	}

	def runFxForm1Schedule11 = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: (params.cbReportType == 'report' ? 'Schedule_11' : 'TRD11')
			
		if(params.cbReportType == 'report') {
			params['P_REPORTNAME'] = "Schedule_11_Report"
			params['reportId'] = reportIdService.generateReportId("FFS11")
			params['format'] = "pdf"
		} else {
			params['P_REPORTNAME'] = "Schedule_11_Text"
			params['format'] = "txt"
		}
		
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
		params['currentDate'] = params.onlineReportDate
		
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['rptFilename'] = params.cbReportType == 'report' ? 'Schedule_11' : 'TRD11'
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		// List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		List reportDataList = fxForm1Schedule11Service.getFxForm1Schedule11DataList(params.onlineReportDate)
		println "reportDataList: "+reportDataList
	
		// comment below code to generate pdf and dbf files
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
		
		// uncomment below code to generate pdf and dbf files
//		if(params.cbReportType == 'report') {
//			generateGenericReport(reportDataList, ext_params["format"], ext_params)
//		} else {
//			Map<String, Object> dbfParamsMap = new HashMap<>()
//			dbfParamsMap.put("fileName", "IMPNEWB")
//			dbfParamsMap.put("reportDate", new Date(params.onlineReportDate))
//			dbfParamsMap.put("scheduleNo", "11")
//			
//			List fieldHeaderList = fxForm1Schedule11Service.getFieldHeaders()
//			def reportName = fxForm1DbfWriterService.generateDbfFile(reportDataList, fieldHeaderList, dbfParamsMap)
//			successList.add(reportName)
//		}
	}
	
	def runCredexReport = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: (params.cbReportType == 'report' ? 'CREDEX_Report' : 'FORM4')
			
		if(params.cbReportType == 'report') {
			params['P_REPORTNAME'] = "Credex_Report"
			params['reportId'] = reportIdService.generateReportId("CRDEX")
			params['format'] = "pdf"
		} else {
			params['P_REPORTNAME'] = "FORM4"
			params['format'] = "xls"
		}
		
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
		params['onlineReportDate'] = params.onlineReportDate
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
		ext_params['rptFilename'] = params.cbReportType == 'report' ? 'CREDEX_Report' : 'FORM4'
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		// List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		List reportDataList = credexService.getCredexDataList(params.onlineReportDate)
		println "reportDataList: "+reportDataList
	
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}

	def runTsdExportBankCommission = {		//for Reports with no Fields Data from DataBase
		
		def token = params.token ?: 'Export_Bank_Commission'
			
		params['P_REPORTNAME'] = "Export_Bank_Commission"
		params['specificRptPath'] = 'Manual_Reports_for_SPAD'       //Follow the '/' format
	
		params['reportId'] = reportIdService.generateReportId("EXBCM")
		params['format'] = "pdf"
	
		println ""
		println "params: "+params
	
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
	
		println params['userId']
	
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'] + "/"
	
		ext_params['token'] = token
	
		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}
	
		println "ext_params['rptPath']: "+ext_params['rptPath']
		println "ext_params: "+ext_params
	
		List reportDataList = reportDataQueryService.getReportData(params['P_REPORTNAME'],params['QUERY_REPORT'],fieldsEmpty)
		println "reportDataList: "+reportDataList
	
		generateGenericReport(reportDataList, ext_params["format"], ext_params)
	}
		
	def index = {

		def keys = params.keySet()
		def tmpkeys = []
		keys.each{
			if(params["${it}"].substr('tfs_'))
				tmpkeys<< params["${it}"]


		}

		//Function Get Report Parameters
		//Function Get Report Data Source

		//params version
		def ext_params = [:]

		ext_params['rptPath'] = params['rptPath']?: 'reports/'
		ext_params['reportFolder'] = 'reports/' + params['specificRptPath']

		def pkeyset = params.keySet()
		pkeyset.each{ kkey ->
			ext_params[kkey] = params[kkey]
		}

		// removes action and controller parameters
		params.remove("form")
		params.remove("action")
		params.remove("controller")

		//def command = new PrintReportCommand()

		Map<String, String> parameterMap = new HashMap<String, String>()

		(1..10).each{
			parameterMap.put(it,it)
		}

		// sets value of parameter map
		params.each {
			parameterMap.put(it.key, it.value)
		}

		// generate UUID
		UUID tokenValue = UUID.randomUUID()
		String tokenId = "mytoken"

		// add mytoken in parameters
		parameterMap.put(tokenId, String.valueOf(tokenValue))

		// sets parameter map to command
		//command.setParameterMap(parameterMap)

		// dispatches command
		try{
			println 'Dispatch Print Command'
			//commandBusService.dispatch(command)
		} catch(Exception e) {
			// TODO catch exception
		}

		//saves pdf to report directory
		//generateGenericReport([], JasperExportFormat.PDF_FORMAT, ext_params, true)
		//saves pdf to report directory
		generatePDFFile([], JasperExportFormat.PDF_FORMAT, ext_params)
		render('angol')

	}


	private  viewReport(JasperReportDef reportDef, String format, String filename) {

		def contentType = "application/vnd.ms-excel"
		def contentDisp = "attachment"
		if ((JasperExportFormat.PDF_FORMAT)?.toString().equalsIgnoreCase("" + format)) {
			contentType = "application/pdf"
			contentDisp = "inline"
		}

		response.setHeader "Content-disposition", contentDisp + "; filename=" + filename
		response.contentType = contentType
		response.characterEncoding = "UTF-8"
		response.outputStream << reportDef.contentStream.toByteArray()
	}

	private saveFile(JasperReportDef reportDef, String format, String filename) {

		//def reportDef = new JasperReportDef(folder:'reports/secs', name:'sycch', fileFormat:JasperExportFormat.PDF_FORMAT)
		//reportDef.parameters = ext_params

		reportDef.contentStream = jasperService.generateReport(reportDef)

		response.contentType = reportDef.fileFormat.mimeTyp
		response.characterEncoding = "UTF-8"
		response.outputStream << reportDef.contentStream.toByteArray()

	}

	private generatePDFFile(reportData, format, ext_params){
		try{
			def repNameTemplate = ext_params['P_REPORTNAME']
			def repName = ext_params['rptFilename'] ?: repNameTemplate
			def repPath = ext_params['rptPath'] ?: 'reports/'
			def userId = ext_params['userId']
			def dateTime = "1983_08_04" //Update to proper date time
			//def fileName = repName + "_" + userId + "_" + dateTime
			def fileName = ext_params['token']
			def fileExtension = "pdf"

			String reportFolder = ext_params['reportFolder']
			println "reportFolder:"+reportFolder


			def file = repPath + fileName + "." + fileExtension
			println 'filex:'+file
			def filex = new File(file)

			//def reportDef = new JasperReportDef(folder: 'reports', name: repNameTemplate, fileFormat: format)
			def reportDef = new JasperReportDef(folder: reportFolder, name: repNameTemplate, fileFormat: format)
			reportDef.parameters = ext_params
			if (reportData != null) {
				println"reportData.size:"+reportData.size
				reportDef.reportData = reportData // list of objects to be displayed in the report
			}
			//reportDef.contentStream = jasperService.generateReport(reportDef)
			println 'filex.canonicalPath:'+filex.canonicalPath


			JasperExportManager.exportReportToPdfFile(incuventureJasperService.generateJasperPrint(reportDef), filex.canonicalPath) //filex.absolutePath
			return true
		} catch (Exception e){
			e.printStackTrace()
			println e.getMessage()
			return false
		}
	}


	private String generateGenericReport( reportData, format, ext_params) {

		def repNameTemplate = ext_params['P_REPORTNAME']
		def repName = ext_params['rptFilename'] ?: repNameTemplate
		def repPath = ext_params['rptPath'] ?: 'reports/'
		def userId = ext_params['userId']
		def dateTime = "1983_08_04" //Update to proper date time
		//def fileName = repName + "_" + userId + "_" + dateTime
		def fileName = ext_params['token']
		def fileExtension = format

		def inv = ext_params['inv']

		// 20130317 replace with correct absolute paths
		//String reportFolder = ext_params['reportFolder']
		// String reportFolder = new File(servletContext.getRealPath("/")).getParent() + "/" + ext_params['reportFolder']
		String reportFolder = servletContext.getRealPath("/") + "/" + ext_params['reportFolder']

		// 20130317 replace with correct absolute paths
		// def file = repPath + fileName + "." + fileExtension
		// String file = new File(servletContext.getRealPath("/")).getParent() + "/" +  repPath + fileName + "." + fileExtension
		String file = ''
		if((repNameTemplate == 'Debit_Memo_Cdt_Payment' || repNameTemplate == 'Debit_Memo_Batch') && ext_params['cdtPaymentClientName'] != '' && ext_params['cdtPaymentClientName'] != null) {
			file = ext_params['cdtPaymentFileName'] + "." + fileExtension
		} else {
			file = servletContext.getRealPath("/") + "/" +  repPath + fileName + "." + fileExtension
		}

		ext_params['SUBREPORT_DIR'] = servletContext.getRealPath("/") + "/" + ext_params['SUBREPORT_DIR']

		println "xxxxxxxxxx file = " + file
		println "xxxxxxxxxx ext_params['SUBREPORT_DIR'] = " + ext_params['SUBREPORT_DIR']

		if(File.separator == '/') {
			file = file.replaceAll("\\\\", "/")
			reportFolder = reportFolder.replaceAll("\\\\", "/")
			ext_params['SUBREPORT_DIR'] = ext_params['SUBREPORT_DIR'].replaceAll("\\\\", "/")
		}
		else {
			file = file.replaceAll("/", "\\\\")
			reportFolder = reportFolder.replaceAll("/", "\\\\")
			ext_params['SUBREPORT_DIR'] = ext_params['SUBREPORT_DIR'].replaceAll("/", "\\\\")
		}
		println "reportFolder:"+reportFolder
		println "file:" + file

		def contentType = "application/pdf"
		def contentDisp = "attachment"
		def formatx = JasperExportFormat.PDF_FORMAT


		println "format:"+ format
		//TODO: Refactor this. Extract method
		if ("XLS".equalsIgnoreCase(format)) {
			println "IN XLS"
			fileExtension = "xls"
			contentType = "application/vnd.ms-excel"
			contentDisp = "attachment"
			formatx = JasperExportFormat.XLS_FORMAT

		}
		if ("CSV".equalsIgnoreCase(format)) {
			fileExtension = "csv"
			contentType = "application/vnd.ms-word"
			contentDisp = "attachment"
			formatx = JasperExportFormat.CSV_FORMAT
		}
		if ("DOCX".equalsIgnoreCase(format)) {
			fileExtension = "docx"
			contentType = "application/vnd.ms-word"
			contentDisp = "attachment"
			formatx = JasperExportFormat.DOCX_FORMAT
		}
		if ("XLSX".equalsIgnoreCase(format)) {
			fileExtension = "xslx"
			contentType = "application/vnd.ms-excel"
			contentDisp = "attachment"
			formatx = JasperExportFormat.XLSX_FORMAT
		}
		if ("ODT".equalsIgnoreCase(format)) {
			fileExtension = "odt"
			contentType = "application/vnd.oasis.opendocument.text"
			contentDisp = "attachment"
			formatx = JasperExportFormat.ODT_FORMAT
		}
		if ("TXT".equalsIgnoreCase(format)) {
			fileExtension = "txt"
			contentType = "text/plain"
			contentDisp = "attachment"
			formatx = JasperExportFormat.TEXT_FORMAT
		}
		//RTF (Rich Text Format)
		/*if ("RTF".equalsIgnoreCase(format)) {
			fileExtension = "rtf"
			contentType = "text/rtf"
			contentDisp = "attachment"
			formatx = JasperExportFormat.RTF_FORMAT
		}*/

		//For including logo and other graphics
		//ext_params['P_IMAGE_LOGO'] = servletContext.getRealPath('reports/NHMFC-Logo.png')
		println "reportFolder:"+reportFolder
		println "repNameTemplate:"+repNameTemplate
		println "formatx:"+formatx

		JasperReportDef reportDef = new JasperReportDef(folder: reportFolder, name: repNameTemplate, fileFormat: formatx)
		reportDef.parameters = ext_params

		if (reportData != null) {
			reportDef.reportData = reportData // list of objects to be displayed in the report
		} else {
			println "null reportData"
		}


		if (inv != null && inv.equals('batch')) {
			println "\n--- START: $repName" + (repName?.toString().contains("TRD") ? "" : ".$format") + ", TIME:"+ DateUtils.timeFormat(new Date()) + " ---"
			
			String month = ''
			String date = ''
			if (ext_params['dateGenerated'] != null && !((String)ext_params['dateGenerated']).isEmpty()) {
				String[] dateGenerated = ((String)ext_params['dateGenerated']).split("/")
				month = dateGenerated[0]
				date = dateGenerated[1]
			} else if(ext_params['onlineReportDate'] != null && !((String)ext_params['onlineReportDate']).isEmpty()){
				String[] dateGenerated = ((String)ext_params['onlineReportDate']).split("/")
				month = dateGenerated[0]
				date = dateGenerated[1]
			}else {
				GregorianCalendar calendar = GregorianCalendar.getInstance()
				month = (calendar.get(GregorianCalendar.MONTH) + 1)
				date = calendar.get(GregorianCalendar.DATE)
			}
			if (month != '') {
				month = StringUtils.leftPad(month, 2, '0')
			}
			if (date != '') {
				date = StringUtils.leftPad(date, 2, '0')
			}
			
			String prefix = 'TFS_' + month + date + '_'
			String fileNameWithoutMonthDate = fileName
			fileName = prefix + fileName
			int numberOfReportToGenerate = 0
			int totalNumberOfReport = 0

			File f = null
			String rptFilename = ext_params['rptFilename']?.toString()
			if(rptFilename?.contains("TRD")) {
				f = new File(grailsApplication.config.tfs.glfxform1.directory + File.separator + fileNameWithoutMonthDate)
				numberOfReportToGenerate = 2
				
				File fxForm1Dir = new File(grailsApplication.config.tfs.glfxform1.directory)
				if(!fxForm1Dir.exists()){
					fxForm1Dir.mkdir()
				}
			} else if(rptFilename?.equals("FORM4")) {
				f = new File(grailsApplication.config.tfs.credexform4.directory + File.separator + fileNameWithoutMonthDate + ".${fileExtension}")
				numberOfReportToGenerate = 2
				
				File fxForm1Dir = new File(grailsApplication.config.tfs.credexform4.directory)
				if(!fxForm1Dir.exists()){
					fxForm1Dir.mkdir()
				}
			} else {
				f = new File(grailsApplication.config.tfs.batch.output.directory + File.separator + fileName + ".${fileExtension}")
				numberOfReportToGenerate = 1
			}
			
			File batchDir = new File(grailsApplication.config.tfs.batch.output.directory)

			if(!batchDir.exists()){
				batchDir.mkdir()
			}
			
			while(totalNumberOfReport < numberOfReportToGenerate){
				if(totalNumberOfReport > 0) {				
					if(rptFilename?.contains("TRD")) {
						f = new File(grailsApplication.config.tfs.glfxform1.backup + File.separator + fileName)
						
						File trdBackupDir = new File(grailsApplication.config.tfs.glfxform1.backup + File.separator)
						if(!trdBackupDir.exists()){
							trdBackupDir.mkdir()
						}
					} else if(rptFilename?.equals("FORM4")) {
						f = new File(grailsApplication.config.tfs.credexform4.backup + File.separator + fileName + ".${fileExtension}")
						
						File trdBackupDir = new File(grailsApplication.config.tfs.credexform4.backup + File.separator)
						if(!trdBackupDir.exists()){
							trdBackupDir.mkdir()
						}
					}
				}
			
	            println ">>>>>>>> f.absolutePath = ${f.absolutePath}"
				println f
	
				FileOutputStream fos = null
	            try{
	                reportDef.contentStream = jasperService.generateReport(reportDef)
	           		fos = new FileOutputStream(f)
	                IOUtils.write(reportDef.contentStream.toByteArray(), fos)
	                fos.close()
				successList.add("$repName" + (repName?.toString().contains("TRD") ? "" : ".$format"));
					println "\n--- END: (SUCCESS): $repName" + (repName?.toString().contains("TRD") ? "" : ".$format") + ", TIME:"+ DateUtils.timeFormat(new Date()) + " ---"
	            }catch(Exception e){
	                //TODO:add to error collection
					if(!e.printStackTrace()){
						println "\n-------" + e + "-------\n"
						println e.getCause()?.getMessage()					
					}
	                println "\n--- END: (FAILED): $repName" + (repName?.toString().contains("TRD") ? "" : ".$format") + ", TIME:"+ DateUtils.timeFormat(new Date()) + " ---"
	            }finally{
	                IOUtils.closeQuietly(fos)
	            }
				totalNumberOfReport++
			}

			//TODO:edit
//            redirect(url: grailsApplication.config.tfs.batch.url.main + '?success=true&batchProcessingFlag='+ext_params['batchProcessingFlag'])

		} else {
			
			reportDef.contentStream = jasperService.generateReport(reportDef)

			if (ext_params["fileName"]) {
				file = ext_params["fileName"]
				println "filename " + file
			}
			
			response.setHeader "Content-disposition", contentDisp + "; filename=" + file
			response.contentType = contentType
			response.characterEncoding = "UTF-8"
			response.outputStream << reportDef.contentStream.toByteArray()
		}
	}


	def getJSONBatch(){
		try{
			switch(params.duration){
			case "daily":
				runConsolidatedDailyReportDepositsCollected(params)
				runConsolidatedReportDaliyCollectionsCdtOtherLevies(params)
				runConsolidatedReportDailyCollectionsAll(params)
				runConsolidatedReportDailyCollectionsImportExport(params)
				runConsolidatedReportDailyCollectionsFinalAdvance(params)
				runConsolidatedReportDailyCollectionsExportDocumentaryStampFees(params)
				runConsolidatedReportDailyCollectionsImportProcessingFees(params)
				
				params.put("consolidatedReportType", "remittance")
				runConsolidatedDailyReportDepositsCollected(params)
				runConsolidatedReportDaliyCollectionsCdtOtherLevies(params)
				runConsolidatedReportDailyCollectionsExportDocumentaryStampFees(params)
				runConsolidatedReportDailyCollectionsImportProcessingFees(params)
				
				params.put("reportType", "exception")
				runDailySummaryOfAccountingEntries(params)
				
				runDailyForeignCashLcOpened(params)
				runDailyFxlcOpenedReportCdtDetails(params)
				runDailyForeignRegularLcOpened(params)
				runDailyFunding(params)
				runDailyOutstandingForeignLc(params)
				dailyOutstandingCCBD(params)
				runDailyReportProcessedRefundsBatch(params)
				
				params.put("reportType", "")
				runDailySummaryOfAccountingEntries(params)
				
				// FX Form 1 Reports Schedules 3, 4, 5, 7 ,9 ,10, 11
				params.put("cbReportType", "report")
//				runFxForm1Schedule3(params)
//				runFxForm1Schedule4(params)
				runFxForm1Schedule5(params)
				// runFxForm1Schedule7(params)
				runFxForm1Schedule9(params)
				runFxForm1Schedule10(params)
				runFxForm1Schedule11(params)
				
				params.put("cbReportType", "text")
//				runFxForm1Schedule3(params)
//				runFxForm1Schedule4(params)
				runFxForm1Schedule5(params)
				// runFxForm1Schedule7(params)
				runFxForm1Schedule9(params)
				runFxForm1Schedule10(params)
				runFxForm1Schedule11(params)
				
				params.put("tellerID", "ALL")
				runTfsCasaPostingReport(params)
				runTradeServicesAMLAReportedTransactions(params)
				runTramsReport(params)
				break;
				
			case "dailySummary":
				runDailyMasterGLSummary(params)
				runDailyMasterGLDailyBalanceSummary(params)
				break;
				
			case "weekly":
				params.reportType= 'viewWeeklyScheduleDocStamps108'
				runWeeklyScheduleDocStamps(params)
				
				params.reportType= 'viewWeeklyScheduleDocStamps113'
				runWeeklyScheduleDocStamps(params)
				
				params << [reportType:'viewWeeklyScheduleDocStampsTR']
				runWeeklyScheduleDocStamps(params)
				
				runWeeklyForeignDomesticOpenLC(params)
				
				runWeeklyReportOnMaturingUsancLc(params)
				break;
				
			case "monthly":
				runCollectedTwoPercentCwt(params)
				runConsolidatedReportDomesticLcOpenedForMonth(params)
				runConsolidatedReportOnForeignLcOpenedForMonth(params)
				runCustomsDutiesAndTaxesAndOtherLevies(params)
				runDmNonLcsNegoForTheYear(params)
				runDomesticLcOpenedForTheMonth(params)
				runExportNegofortheMonthperClient(params)
				runexportNegoperCollectingBank(params)
				runExportPaymentsfortheMonthperClient(params)
				runForeignLcOpenedForTheMonth(params)
				runFxNonLcsNegoForTheYear(params)
				runListOfTransactionsWithNoCifNumber(params)
				runMonthlyTransactionCountReport(params)
				runOutstandingBankGuarantyReport(params)
				runOutstandingDomesticCashLcPerCurrency(params)
				runOutstandingDomesticCashLcPerImporter(params)
				runOutstandingDomesticSightLcPerCurrency(params)
				runOutstandingDomesticSightLcPerImporter(params)
				runOutstandingDomesticStandbyLcPerCurrency(params)
				runOutstandingDomesticStandbyLcPerImporter(params)
				runOutstandingDomesticUsanceLcPerCurrency(params)
				runOutstandingDomesticUsanceLcPerImporter(params)
				runOutstandingExportNegotiationDomestic(params)
				runOutstandingExportNegotiationForeign(params)
				runOutstandingForeignCashLcPerCurrency(params)
				runOutstandingForeignCashLcPerImporter(params)
				runOutstandingForeignSightLcPerCurrency(params)
				runOutstandingForeignSightLcPerImporter(params)
				runOutstandingForeignStandbyLcPerCurrency(params)
				runOutstandingForeignStandbyLcPerImporter(params)
				runOutstandingForeignUsanceLcPerCurrency(params)
				runOutstandingForeignUsanceLcPerImporter(params)
				runOutstandingInwardBillsForCollectionDmDaDpPerCurrency(params)
				runOutstandingInwardBillsForCollectionFxDaDpPerCurrency(params)
				runOutstandingInwardBillsforCollectionFXLCperCurrency(params)
				runOutstandingMarginalDepositsReport(params)
				
				runProductAvailmentsReport(params)
				params.put("productAvailReportType","notException")
				runProductAvailmentsReport(params)
				
				runProfitabilityMonitoringReport(params)
				params.put("profitMonitoringReportType","notException")
				runProfitabilityMonitoringReport(params)
				
				runVolumetrics(params)
				runYtdReportOnDomesticLcOpened(params)
				runYtdReportOnDomesticLcOpenedClassifiedByBank(params)
				runYtdReportOnForeignLcOpened(params)
				runYtdReportOnForeignLcOpenedClassifiedByBank(params)
				break;
				
			case "quarterly":
				runApOthers(params)
				runArOthers(params)
				
  				params.put("cbReportType", "text")
  				runCredexReport(params)
  				params.put("cbReportType", "report")
  				runCredexReport(params)
				
				runArOthers(params)
				runQuarterlyReportOnForeignStandybyLcsOpened(params)
				break;
				
			case "yearly":
				runYearEndReportOnForeignLcsOpenedPerAdvisingBank(params)
				runYearEndReportOnForeignLcsOpenedPerConfirmingBank(params)
				runYearEndReportOnForeignLcsOpenedPerCountry(params)
				runYtdCustomsDutiesAndTaxesAndOtherLevies(params)
				runYtdTransactionCountImportExportReport(params)
			default:
				break;
			}
		}catch(Exception e){
			println e
			e.printStackTrace()
		}
		def jsonParams=[:]
        jsonParams << [successList:successList.join(",")]
        successList.clear()

		render "${params.callback}(${jsonParams as JSON})"
	}
	
	def runIndividualReport(){
		GenericReportsController gr = new GenericReportsController()
		String methodName = params.methodName
		params.put("tellerID", "ALL")

		try{
			if(methodName != null && !methodName.isEmpty()){
				gr."$methodName"(params)
			}
		}catch(MissingMethodException mme){
			println mme
			mme.printStackTrace()
		}catch(Exception e){
			println e
			e.printStackTrace()
		}
		
		def jsonParams=[:]
		jsonParams << [successList:successList.join(",")]
		successList.clear()

		render "${params.callback}(${jsonParams as JSON})"
	}
	
	def runSpecialMco(){
		try{
			runTfsMcoReportSpecial(params)
		}catch(Exception e){
			e.printStackTrace()
		}
		
		def jsonParams = [:]
		jsonParams << [successList:successList.join(",")]
		successList.clear()
		
		render "${params.callback}(${jsonParams as JSON})"
	}
}