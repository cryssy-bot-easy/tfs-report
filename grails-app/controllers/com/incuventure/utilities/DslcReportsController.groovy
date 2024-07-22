package com.incuventure.utilities

/**
*
* @author Giancarlo Angulo <gian.angulo@incuventure.net>
*
*/


import com.incuventure.utilities.ReportDirectory
import org.codehaus.groovy.grails.plugins.jasper.JasperReportDef
import org.codehaus.groovy.grails.plugins.jasper.JasperExportFormat
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter
import net.sf.jasperreports.engine.export.JRTextExporterParameter
import net.sf.jasperreports.engine.export.JRXlsExporterParameter
import net.sf.jasperreports.engine.util.JRProperties
import org.springframework.core.io.Resource
import net.sf.jasperreports.engine.*
import grails.converters.JSON
import org.hibernate.transform.Transformers
import com.incuventure.dto.TradeServiceDTO
import org.hibernate.type.StandardBasicTypes
import java.text.SimpleDateFormat
import java.text.DecimalFormat

class DslcReportsController {
	
	def reportDataQueryService
	def jasperService
	def commandBusService
	def reportDirectory
	def incuventureJasperService
	def sessionFactory
	def tfsMcoReportData_BACKUPService

		
	//Bidding Security
	def runBiddingSecurity = {
		
		def reportName='Bidding_Security_'+params.reportID
				
		def token = params.token ?: reportName
		
		params['P_REPORTNAME'] = reportName
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'DMLC/DSLC Bidding/'       //Follow the '/' format
		
		def reportID=params.reportID
		
		if(reportID.equalsIgnoreCase('H')||reportID.equalsIgnoreCase('J')||reportID.equalsIgnoreCase('K')||reportID.equalsIgnoreCase('L')||reportID.equalsIgnoreCase('M')||reportID.equalsIgnoreCase('N')){
			params['lcNumber'] = '123-12-12345'
		}
				
		if(reportID.equalsIgnoreCase('H')||reportID.equalsIgnoreCase('N')){
			params['bidderName'] = 'Bidder Name'
		}
		
		if(reportID.equalsIgnoreCase('H')||reportID.equalsIgnoreCase('I')||reportID.equalsIgnoreCase('M')||reportID.equalsIgnoreCase('N')){
			params['bidderAddress'] = 'Bidder\'s Address'
		}
		
		if(reportID.equalsIgnoreCase('H')||reportID.equalsIgnoreCase('J')){
			params['bankBranch'] = 'Makati Ave.'
		}
		
		if(reportID.equalsIgnoreCase('H')||reportID.equalsIgnoreCase('I')||reportID.equalsIgnoreCase('J')||reportID.equalsIgnoreCase('K')||reportID.equalsIgnoreCase('L')||reportID.equalsIgnoreCase('M')){
			params['bankAddress'] = 'Makati Ave., Makati City'
		}
		
		if(reportID.equalsIgnoreCase('H')||reportID.equalsIgnoreCase('I')||reportID.equalsIgnoreCase('J')||reportID.equalsIgnoreCase('K')||reportID.equalsIgnoreCase('L')||reportID.equalsIgnoreCase('M')||reportID.equalsIgnoreCase('N')){
			params['bidder'] = 'Bidder'
			params['bid'] = '10,000,000.00'
			params['bank'] = 'United Coconut Planters Bank'
			params['currencyInWords'] = 'PHP'
			params['amountInWords'] = 'TEN MILLION PESOS'
			params['currency'] = 'PHP'
			params['amountInDigits'] = '10,000,000.00'
			params['entityName'] = 'Entity Name'
		}
		
		if(reportID.equalsIgnoreCase('H')||reportID.equalsIgnoreCase('J')||reportID.equalsIgnoreCase('K')||reportID.equalsIgnoreCase('L')||reportID.equalsIgnoreCase('N')){
			params['periodDate'] = '12/12/2012'
		}
		
		if(reportID.equalsIgnoreCase('I')){
			params['bidInvitationNo'] = '12122012'
			params['bidInviationDate'] = '12/12/2012'
			params['guaranteePeriodDays'] = '10'
		}
		
		if(reportID.equalsIgnoreCase('I')||reportID.equalsIgnoreCase('J')||reportID.equalsIgnoreCase('K')||reportID.equalsIgnoreCase('L')||reportID.equalsIgnoreCase('M')){
			params['biddingDate'] = '12/12/2012'
		}
		
		if(reportID.equalsIgnoreCase('I')||reportID.equalsIgnoreCase('J')||reportID.equalsIgnoreCase('K')||reportID.equalsIgnoreCase('L')||reportID.equalsIgnoreCase('M')||reportID.equalsIgnoreCase('n')){
			params['entityAddress'] = 'Entity Address'
		}
		
		if(reportID.equalsIgnoreCase('J')){
			params['officerName'] = 'Officer Name'
			params['officerOfficialDesignation'] = 'Officer Designation'
			params['authorizedRep'] = 'Authorized Representative'
			params['authorizedRepOfficialDesignation'] = 'Representative Designation'
		}
		
		if(reportID.equalsIgnoreCase('K')){
			params['conforme'] = 'Conforme'
			params['taxCertExhibitDate'] = 'Tax Exhibit Date'
			params['taxCertExhibitPlace'] = 'Tax Exhibit Place'
		}
		
		if(reportID.equalsIgnoreCase('L')){
			params['asstVP1'] = 'Asst. Vice President 1'
			params['asstVP2'] = 'Asst. Vice President 2'
		}
		
		if(reportID.equalsIgnoreCase('M')){
			params['entityRepresentativePosition'] = 'Position'
		}
		
		if(reportID.equalsIgnoreCase('N')){
			params['branchManager'] = 'Branch Manager'
			params['branchOfficer'] = 'Branch Officer'
			params['branch'] = 'Branch'
		}
		
		
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
		ext_params['rptPath'] = 'web-app/reports/'
		ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
		
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
	
	
	//ADVANCE PAYMENT
	def runAdvancePayment = {
		
		def token = params.token ?: 'Advance_Payment'
		
		params['P_REPORTNAME'] = "Advance_Payment"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
				
		params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
		params['beneficiaryName'] = "COMPANY NAME"	  
		params['beneficiaryAddress'] = 'COMPANY ADDRESS'
		params['beneficiaryRepresentative'] = 'COMPANY REPRESENTATIVE'
		params['beneficiaryRepresentativePositionFull'] = 'REPRESENTATIVE FULL POSITION'
		params['beneficiaryRepresentativePosition'] = 'REPRESENTATIVE POSITION'
		params['beneficiaryRepresentativePosition2'] = 'REPRESENTATIVE POSITION 2'
		params['projectName'] = 'PROJECT NAME'
		params['package'] = 'PACKAGE'
		params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
		params['accounteeName'] = 'CONTRACTED COMPANY NAME'
		params['accounteeAddress'] = 'CONTRACTED COMPANY ADDRESS'
		params['beneficiaryBranchName'] = 'COMPANY BRANCH NAME'
		params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
		params['financialInstitution1'] = 'FINANCIAL INSTITUTION 1'
		params['financialInstitution2'] = 'FINANCIAL INSTITUTION 2'
		params['issueDate'] = new Date()
		params['expiryDate'] = new Date()
		params['branchManager'] = 'BRANCH MANAGER'
		params['format'] = "docx"
				
		println ""
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
		
		
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'web-app/reports/'
		ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
		
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
	
	//ADVANCE PAYMENT 2
	def runAdvancePayment2 = {
			
		def token = params.token ?: 'Advance_Payment_2'
			
		params['P_REPORTNAME'] = "Advance_Payment_2"
		//params['QUERY_REPORT'] = "1000||Laguna"
		params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
		
		params['beneficiaryName'] = "COMPANY NAME"	  
		params['beneficiaryAddress'] = 'COMPANY ADDRESS'
		params['beneficiaryRepresentative'] = 'COMPANY REPRESENTATIVE'
		params['date'] = new Date()
		params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
		params['business'] = 'BUSINESS'
		params['accounteeAddress'] = 'BUSINESS ADDRESS'
		params['accounteeRepresentativePositon'] = 'BUSINESS REPRESENTATIVE POSITION'
		params['accounteeRepresentative'] = 'BUSINESS REPRESENTATIVE'
		params['ucpbBankBranch'] = 'UCPB BRANCH ADDRESS'
		params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
		params['contractID'] = 'CONTRACT ID'
		params['sealDate'] = 'SEAL DATE'
		params['location'] = 'LOCATION'
		params['accounteeRepresentative2'] = 'BUSINESS REPRESENTATIVE 2'
		params['accounteeRepresentativePositon2'] = 'BUSINESS REPRESENTATIVE POSITION 2'
		params['location2'] = 'LOCATION 2'
		params['swornDate'] = 'SWORN DATE'
		params['country'] = 'COUNTRY'
		params['communityTaxCertificateNo'] = 'COMMUNITY TAX CERTIFICATE NUMBER'
		params['communityTaxIssueDate'] = new Date()
		params['format'] = "docx"
		
		println ""
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
						
						
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'web-app/reports/'
		ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
				
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
	
	def runAdvancePayment3 = {
		
	def token = params.token ?: 'Advance_Payment_3'
		
	params['P_REPORTNAME'] = "Advance_Payment_3"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = "COMPANY NAME"
	params['beneficiaryAddress'] = 'COMPANY ADDRESS'
	params['beneficiaryRepresentative'] = 'COMPANY REPRESENTATIVE'
	params['lcNo2'] = 'LC NUMBER 2'
	params['accounteeName'] = 'ACCOUNTEE NAME'
	params['accounteeAddress'] = 'ACCOUNTEE ADDRESS'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'ACTION'
	params['contractDate'] = new Date()
	params['contract2'] = 'ACTION 2'
	params['contractDate2'] = new Date()
	params['ucpbBankBranch'] = 'UCPB BRANCH'
	params['expiryDate'] = new Date()
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runFaithfulPerformance2 = {
		
	def token = params.token ?: 'Faithful_Performance_2'
		
	params['P_REPORTNAME'] = "Faithful_Performance_2"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['issueDate'] = new Date()
	params['beneficiaryName'] = "DESIGNATION NAME"
	params['beneficiaryAddress'] = 'DESIGNATION ADDRESS'
	params['accounteeName'] = 'ACCOUNT NAME'
	params['accounteeAddress'] = 'ACCOUNT ADDRESS'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['contract'] = 'ACTION'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['expiryDate'] = new Date()
	params['ucpbBankBranch'] = 'UCPB DEPARTMENT'
	params['officer'] = 'OFFICER'
	params['officer2'] = 'OFFICER 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runFaithfulPerformanceAdvancePayment = {
		
	def token = params.token ?: 'Faithful_Performance_Advance_Payment'
		
	params['P_REPORTNAME'] = "Faithful_Performance_Advance_Payment"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'DESIGNATION NAME'
	params['beneficiaryAddress'] = 'DESIGNATION ADDRESS'
	params['beneficiaryRepresentative'] = 'ATTENTION NAME'
	params['beneficiaryRepresentativePosition'] = 'ATTENTION POSITION'
	params['accounteeName'] = 'ACCOUNT NAME'
	params['accounteeAddress'] = 'ACCOUNT ADDRESS'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['contract'] = 'ACTION'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['program'] = 'PROGRAM'
	params['beneficiaryName2'] = 'DESIGNATION NAME 2'
	params['expiryDate'] = new Date()
	params['ucpbBankBranch'] = 'UCPB DEPARTMENT'
	params['officer'] = 'OFFICER'
	params['officer2'] = 'OFFICER 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runFirstDataCorpFormat = {
		
	def token = params.token ?: 'First_Data_Corp_Format'
		
	params['P_REPORTNAME'] = "First_Data_Corp_Format"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['bankGuarantyRefNo'] = 'REFERENCE NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'ACCOUNTEE NAME'
	params['accounteeAddress'] = 'ACCOUNTEE ADDRESS'
	params['contract'] = 'ACTION'
	params['noticeDate'] = new Date()
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runFp3 = {
		
	def token = params.token ?: 'FP_3'
		
	params['P_REPORTNAME'] = "FP_3"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'COMPANY NAME'
	params['beneficiaryAddress'] = 'COMPANY ADDRESS'
	params['accounteeName'] = 'SUPPLIER NAME'
	params['accounteeAddress'] = 'SUPPLIER ADDRESS'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['expiryDate'] = new Date()
	params['officer'] = 'OFFICER'
	params['officer2'] = 'OFFICER 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runFp4StandardFormat = {
		
	def token = params.token ?: 'FP_4_Standard_Format'
		
	params['P_REPORTNAME'] = "FP_4_Standard_Format"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'DESIGNATION NAME'
	params['beneficiaryAddress'] = 'DESIGNATION ADDRESS'
	params['accounteeName'] = 'ACCOUNT NAME'
	params['accounteeAddress'] = 'ACCOUNT ADDRESS'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['expiryDate'] = new Date()
	params['ucpbBankBranch'] = 'UCPB DEPARTMENT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runFp5 = {
		
	def token = params.token ?: 'FP_5'
		
	params['P_REPORTNAME'] = "FP_5"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['letterDate'] = new Date()
	params['beneficiaryName'] = 'COMPANY NAME'
	params['beneficiaryAddress'] = 'COMPANY ADDRESS'
	params['beneficiaryRepresentative'] = 'COMPANY REPRESENTATIVE'
	params['beneficiaryRepresentativePosition'] = 'COMPANY REPRESENTATIVE POSITION'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['beneficiaryName2'] = 'COMPANY NAME 2'
	params['accounteeName'] = 'ACCOUNT NAME'
	params['accounteeAddress'] = 'ACCOUNT ADDRESS'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['supply'] = 'SUPPLIES'
	params['under'] = 'UNDER'
	params['beneficiaryName3'] = 'COMPANY NAME 3'
	params['accounteeName2'] = 'ACCOUNT NAME 2'
	params['issueDate'] = 'ISSUE DATE'
	params['issuePlace'] = 'ISSUE PLACE'
	params['signatoryName'] = 'SIGNATORY NAME'
	params['signatoryPosition'] = 'SIGNATORY POSITION'
	params['beneficiaryAddress2'] = 'COMPANY ADDRESS 2'
	params['notarizedDate'] = 'NOTARIZED DATE'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runFp6 = {
		
	def token = params.token ?: 'FP_6'
		
	params['P_REPORTNAME'] = "FP_6"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['letterDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryName2'] = 'BENEFICIARY NAME 2'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['beneficiaryRepresentative'] = 'BENEFICIARY REPRESENTATIVE'
	params['beneficiaryRepresentativePosition'] = 'BENEFICIARY REPRESENTATIVE POSITION'
	params['accounteeName'] = 'COMPANY NAME'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'ACTION'
	params['contractNumber'] = 'CONTRACT NUMBER'
	params['expiryDate'] = new Date()
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['conformeName'] = 'CONFORME NAME'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runFp7 = {
		
	def token = params.token ?: 'FP_7'
		
	params['P_REPORTNAME'] = "FP_7"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryName2'] = 'BENEFICIARY NAME 2'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['beneficiaryRepresentative'] = 'BENEFICIARY REPRESENTATIVE'
	params['accounteeName'] = 'COMPANY NAME'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'ACTION'
	params['accounteeName2'] = 'COMPANY NAME 2'
	params['beneficiaryName3'] = 'BENEFICIARY NAME 3'
	params['expiryDate'] = new Date()
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['confirmer'] = 'CONFIRMER'
	params['confirmerPosition'] = 'CONFIRMER POSITION'
	params['notarizedDate'] = 'NOTARIZED DATE'
	params['communityTaxCertificateNo'] = 'RESIDENCE CERTIFICATE NUMBER'
	params['communityTaxIssuePlace'] = 'ISSUE PLACE'
	params['communityTaxIssueDate'] = 'ISSUE DATE'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runFpStandardFormat2 = {
		
	def token = params.token ?: 'FP_Standard_Format_2'
		
	params['P_REPORTNAME'] = "FP_Standard_Format_2"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'DESIGNATION NAME'
	params['beneficiaryAddress'] = 'DESIGNATION ADDRESS'
	params['accounteeName'] = 'ACCOUNT NAME'
	params['accounteeAddress'] = 'ACCOUNT ADDRESS'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['contract'] = 'ACTION'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['expiryDate'] = new Date()
	params['ucpbBankBranch'] = 'UCPB DEPARTMENT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runOthers = {
		
	def token = params.token ?: 'Others'
		
	params['P_REPORTNAME'] = "Others"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['beneficiaryRepresentative'] = 'BENEFICIARY REPRESENTATIVE'
	params['beneficiaryRepresentativePosition'] = 'BENEFICIARY REPRESENTATIVE POSITION'
	params['accounteeName'] = 'CONTRACTOR NAME'
	params['accounteeAddress'] = 'CONTRACTOR ADDRESS'
	params['contract'] = 'ACTION'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['expiryDate'] = new Date()
	params['guarantor'] = 'GUARANTOR'
	params['nameOfBank'] = 'BANK NAME'
	params['bankAddress'] = 'BANK ADDRESS'
	params['issueDate'] = new Date()
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runOthers5 = {
		
	def token = params.token ?: 'Others_5'
		
	params['P_REPORTNAME'] = "Others_5"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'DESIGNATION NAME'
	params['beneficiaryAddress'] = 'DESIGNATION ADDRESS'
	params['beneficiaryAddress2'] = 'DESIGNATION ADDRESS 2'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['accounteeName'] = 'CONCESSIONAIRE NAME'
	params['accounteeAddress'] = 'CONCESSIONAIRE ADDRESS'
	params['contract'] = 'ACTION'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['expiryDate'] = new Date()
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runOthersStandby = {
		
	def token = params.token ?: 'Others_Standby'
		
	params['P_REPORTNAME'] = "Others_Standby"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['expiryDate'] = new Date()
	params['beneficiaryRepresentativePosition'] = 'BENEFICIARY REPRESENTATIVE POSITION'
	params['beneficiaryName2'] = 'BENEFICIARY NAME 2'
	params['raNo'] = 'RA NUMBER'
	params['obligationRules'] = 'OBLIGATION RULES'
	params['obligationContract'] = 'OBLIGATION CONTRACT'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['beneficiaryName3'] = 'BENEFICIARY NAME 3'
	params['accounteeName'] = 'ACCOUNTEE NAME'
	params['amountNominal'] = 'AMOUNT NOMINAL'
	params['ucpbBankBranch'] = 'UCPB BANK BRANCH'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runOthersStandby3Caltex = {
		
	def token = params.token ?: 'Others_Standby_3_Caltex'
		
	params['P_REPORTNAME'] = "Others_Standby_3_Caltex"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['beneficiaryName'] = 'BUYER NAME'
	params['beneficiaryAddress'] = 'BUYER ADDRESS'
	params['accounteeName'] = 'SELLER NAME'
	params['accounteeAddress'] = 'SELLER ADDRESS'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'ACTION'
	params['bankName'] = 'BANK NAME'
	params['issueDate'] = 'ISSUE DATE'
	params['expiryDate'] = 'EXPIRY DATE'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPaymentGuaranty2 = {
		
	def token = params.token ?: 'Payment_Guaranty_2'
		
	params['P_REPORTNAME'] = "Payment_Guaranty_2"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['letterDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'ACCOUNTEE NAME'
	params['accounteeAddress'] = 'ACCOUNTEE ADDRESS'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['expiryDate'] = new Date()
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPaymentGuarranty = {
		
	def token = params.token ?: 'Payment_Guarranty'
		
	params['P_REPORTNAME'] = "Payment_Guarranty"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['letterDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['accounteeName'] = 'ACCOUNTEE NAME'
	params['accounteeAddress'] = 'ACCOUNTEE ADDRESS'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'ACTION'
	params['accounteeName2'] = 'ACCOUNTEE NAME 2'
	params['issueDate'] = new Date()
	params['ucpbBankBranch'] = 'UCPB BANK BRANCH'
	params['expiryDate'] = new Date()
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPaymentGuarranty3 = {
		
	def token = params.token ?: 'Payment_Guarranty_3'
		
	params['P_REPORTNAME'] = "Payment_Guarranty_3"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['letterDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['accounteeName'] = 'ACCOUNTEE NAME'
	params['accounteeRepresentative'] = 'ACCOUNTEE REPRESENTATIVE'
	params['accounteeAddress'] = 'ACCOUNTEE ADDRESS'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['issueDate'] = new Date()
	params['expiryDate'] = new Date()
	params['ucpbBankBranch'] = 'BANK BRANCH'
	params['authorizedSignatory'] = 'AUTHORIZED SIGNATORY'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPaymentGuarranty4 = {
		
	def token = params.token ?: 'Payment_Guarranty_4'
		
	params['P_REPORTNAME'] = "Payment_Guarranty_4"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'DESIGNATION NAME'
	params['beneficiaryAddress'] = 'DESIGNATION ADDRESS'
	params['accounteeName'] = 'ACCOUNT NAME'
	params['accounteeAddress'] = 'ACCOUNT ADDRESS'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['expiryDate'] = new Date()
	params['ucpbBankBranch'] = 'UCPB DEPARTMENT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['city1'] = 'CITY 1'
	params['city2'] = 'CITY 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPaymentGuarranty5 = {
		
	def token = params.token ?: 'Payment_Guarranty_5'
		
	params['P_REPORTNAME'] = "Payment_Guarranty_5"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['receiver'] = 'RECEIVER'
	params['sequenceOfTotal'] = 'SEQUENCE OF TOTAL'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['furtherIdentification'] = 'FURTHER IDENTIFICATION'
	params['issueDate'] = new Date()
	params['applicableRules'] = 'APPLICABLE RULES'
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'APPLICANT NAME'
	params['accounteeAddress'] = 'APPLICANT ADDRESS'
	params['accounteeAddress2'] = 'APPLICANT ADDRESS 2'
	params['beneficiaryName2'] = 'BENEFICIARY NAME 2'
	params['beneficiaryBranch'] = 'BENEFICIARY BRANCH'
	params['beneficiaryAddress2'] = 'BENEFICIARY ADDRESS 2'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['expiryDate'] = new Date()
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPerformanceBond = {
		
	def token = params.token ?: 'Performance_Bond'
		
	params['P_REPORTNAME'] = "Performance_Bond"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['letterDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['beneficiaryRepresentative'] = 'BENEFICIARY REPRESENTATIVE'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['accounteeName'] = 'ACCOUNTEE NAME'
	params['accounteeAddress'] = 'ACCOUNTEE ADDRESS'
	params['accounteeRepresentative'] = 'ACCOUNTEE REPRESENTATIVE'
	params['accounteeRepresentativePosition'] = 'ACCOUNTEE REPRESENTATIVE POSITION'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'ACTION'
	params['ucpbBankBranch'] = 'UCPB BANK BRANCH'
	params['expiryDate'] = new Date()
	params['authorizedSignatory'] = 'AUTHORIZED SIGNATORY'
	params['authorizedSignatoryPosition'] = 'AUTHORIZED SIGNATORY POSITION'
	params['notarizedDate'] = 'NOTARIZED DATE'
	params['communityTaxCertificateNo'] = 'CERTIFICATE NUMBER'
	params['communityTaxIssueDate'] = 'ISSUE DATE'
	params['communityTaxIssuePlace'] = 'ISSUE PLACE'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPerformanceSecurity1 = {
		
	def token = params.token ?: 'Performance_Security_1'
		
	params['P_REPORTNAME'] = "Performance_Security_1"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['beneficiaryRepresentative'] = 'BENEFICIARY REPRESENTATIVE'
	params['accounteeName'] = 'CONTRACTOR NAME'
	params['accounteeAddress'] = 'CONTRACTOR ADDRESS'
	params['contract'] = 'CONTRACT'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['expiryDate'] = new Date()
	params['guarantorName'] = 'GUARANTOR NAME'
	params['guarantorPosition'] = 'GUARANTOR POSITION'
	params['ucpbBankName'] = 'BANK NAME'
	params['ucpbBankBranch'] = 'BANK ADDRESS'
	params['issueDate'] = new Date()
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPerformanceSecurity2 = {
		
	def token = params.token ?: 'Performance_Security_2'
		
	params['P_REPORTNAME'] = "Performance_Security_2"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryRepresentative'] = 'BENEFICIARY REPRESENTATIVE'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['accounteeName'] = 'ACCOUNTEE NAME'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['expiryDate'] = new Date()
	params['ucpbBankBranch'] = 'UCPB BANK BRANCH'
	params['authorizedSignatory'] = 'AUTHORIZED SIGNATORY'
	params['conforme'] = 'CONFORME'
	params['conformeBy'] = 'CONFORME BY'
	params['conformeLocation'] = 'CONFORME LOCATION'
	params['notarizedDate'] = 'NOTARIZED DATE'
	params['notarizedPlace'] = 'NOTARIZED PLACE'
	params['communityTaxCertificateNo'] = 'CERTIFICATE NUMBER'
	params['communityTaxIssueDate'] = 'ISSUE DATE'
	params['communityTaxIssuePlace'] = 'ISSUE PLACE'
	params['docNo'] = 'DOC NO'
	params['pageNo'] = 'PAGE NO'
	params['bookNo'] = 'BOOK NO'
	params['seriesOf'] = 'SERIES'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPs2 = {
			
	def token = params.token ?: 'Ps_2'
		
	params['P_REPORTNAME'] = "Ps_2"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNumber'] = 'LC NUMBER'
	params['recipient'] = 'RECIPIENT'
	params['recipientAddress'] = 'RECIPIENT ADDRESS'
	params['supplier'] = 'SUPPLIER'
	params['supplierAddress'] = 'SUPPLIER ADDRESS'
	params['contract'] = 'CONTRACT'
	params['bankGuaranteeWords'] = 'BANK GUARANTEE WORDS'
	params['bankGuaranteeAmount'] = 'BANK GUARANTEE AMOUNT'
	params['bankGuaranteeExpiration'] = 'BANK GUARANTEE EXPIRATION'
	params['sendersName'] = 'SENDER\'S NAME'
	params['sendersPosition'] = 'SENDER\'S POSITION'
	params['sendersCompany'] = 'SENDER\'S COMPANY'
	params['sendersCompanyStreet'] = 'SENDER\'S COMPANY STREET'
	params['sendersCompanyCity'] = 'SENDER\'S COMPANY CITY'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
			
			
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
	
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
	
	def runPs3 = {
			
	def token = params.token ?: 'Ps_3'
		
	params['P_REPORTNAME'] = "Ps_3"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['date'] = new Date()
	params['recipient'] = 'RECIPIENT'
	params['recipientStreet	'] = 'RECIPIENT STREET'
	params['recipientCity'] = 'RECIPIENT CITY'
	params['lcNumber'] = 'LC NUMBER'
	params['greetings'] = 'GREETINGS'
	params['client'] = 'CLIENT'
	params['contract'] = 'CONTRACT'
	params['bankGuaranteeWords'] = 'BANK GUARANTEE WORDS'
	params['bankGuaranteeAmount'] = 'BANK GUARANTEE AMOUNT'
	params['contractPrice'] = 'CONTRACT PRICE'
	params['expirationDate'] = new Date()
	params['authorizedSignatory1'] = 'AUTHORIZED SIGNATORY 1'
	params['authorizedSignatory2'] = 'AUTHORIZED SIGNATORY 2'
	params['conforme'] = 'CONFOMRE'
	params['conformesCompany'] = 'CONFORME\'S COMPANY'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
			
			
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
	
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
	
	def runPs4 = {
			
	def token = params.token ?: 'Ps_4'
		
	params['P_REPORTNAME'] = "Ps_4"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNumber'] = 'LC NUMBER'
	params['date'] = new Date()
	params['recipient'] = 'RECIPIENT'
	params['recipientsStreet'] = 'RECIPIENT STREET'
	params['recipientsCity'] = 'RECIPIENT CITY'
	params['attnName'] = 'ATTN NAME'
	params['attnPosition'] = 'ATTN POSIION'
	params['greetings'] = 'GREETINGS'
	params['letterDate'] = new Date()
	params['accountName'] = 'ACCOUNT NAME'
	params['accountCity'] = 'ACCOUNT CITY'
	params['contract'] = 'CONTRACT'
	params['bankGuaranteeWords'] = 'BANK GUARANTEE WORDS'
	params['bankGuaranteeAmount'] = 'BANK GUARANTEE AMOUNT'
	params['senderName'] = 'SENDER NAME'
	params['sendersPosition'] = 'SENDER\'S POSITION'
	params['sendersCompany'] = 'SENDER\'S COMPANY'
	params['conforme'] = 'CONFORME'
	params['conformesPosition'] = 'CONFORME\'S POSITION'
	params['affidavitDate'] = new Date()
	params['affidavitAddress'] = 'AFFIDAVIT ADDRESS'
	params['affidavitID'] = 'AFFIDAVIT ID'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
			
			
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPs6 = {
			
	def token = params.token ?: 'Ps_6'
		
	params['P_REPORTNAME'] = "Ps_6"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNumber'] = 'LC NUMBER'
	params['date'] = new Date()
	params['recipient'] = 'RECIPIENT'
	params['recipientsCity'] = 'RECIPIENT\'S CITY'
	params['attnName'] = 'ATTN NAME'
	params['attnPosition'] = 'ATTN POSITION'
	params['guarantor'] = 'GUARANTOR'
	params['bankName'] = 'BANK NAME'
	params['guarantorsAddress'] = 'GUARANTOR\'S ADDRESS'
	params['guaranteeDate'] = new Date()
	params['contractor'] = 'CONTRACTOR'
	params['contract'] = 'CONTRACT'
	params['bankGuaranteeWords'] = 'BANK GUARANTEE WORDS'
	params['bankGuaranteeAmount'] = 'BANK GUARANTEE AMOUNT'
	params['expirationDate'] = new Date()
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runPs7 = {
		
	def token = params.token ?: 'Ps_7'
		
	params['P_REPORTNAME'] = "Ps_7"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'SUPPLIER NAME'
	params['contract'] = 'CONTRACT'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['expiryDate'] = new Date()
	params['authorizedSignatory'] = 'AUTHORIZED SIGNATORY'
	params['authorizedSignatoryPosition'] = 'AUTHORIZED SIGNATORY POSITION'
	params['conforme'] = 'CONFORME'
	params['conformePosition'] = 'CONFORME POSITION'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runReleaseRetention = {
		
	def token = params.token ?: 'Release_Retention'
		
	params['P_REPORTNAME'] = "Release_Retention"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryName2'] = 'BENEFICIARY NAME 2'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['beneficiaryRepresentative'] = 'BENEFICIARY REPRESENTATIVE'
	params['beneficiaryRepresentativePosition'] = 'BENEFICIARY REPRESENTATIVE POSITION'
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['contract'] = 'CONTRACT'
	params['accounteeName'] = 'CONTRACTOR NAME'
	params['accounteeAddress'] = 'CONTRACTOR ADDRESS'
	params['beneficiaryName3'] = 'BENEFICIARY NAME 3'
	params['beneficiaryRepresentative2'] = 'BENEFICIARY REPRESENTATIVE 2'
	params['beneficiaryRepresentativePosition2'] = 'BENEFICIARY REPRESENTATIVE POSITION 2'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['ucpbBankBranch'] = 'UCPB BANK BRANCH'
	params['beneficiaryRepresentativePosition3'] = 'BENEFICIARY REPRESENTATIVE POSITION 3'
	params['expiryDate'] = new Date()
	params['authorizedSignatory'] = 'AUTHORIZED SIGNATORY'
	params['authorizedSignatoryPosition'] = 'AUTHORIZED SIGNATORY POSITION'
	params['ucpbBankBranch2'] = 'UCPB BANK BRANCH 2'
	params['issueDate'] = new Date()
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runRetention2_1 = {
		
	def token = params.token ?: 'Retention_2_1'
		
	params['P_REPORTNAME'] = "Retention_2_1"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runRetention2_2 = {
		
	def token = params.token ?: 'Retention_2_2'
		
	params['P_REPORTNAME'] = "Retention_2_2"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runDslcBidding={
		
		def token = params.token ?: 'Bidding_Security'
		
		if(params.biddingSecurity == 'A'){
			params['P_REPORTNAME'] = "Bidding_Security_A"
		}
		else if(params.biddingSecurity == 'B'){
			params['P_REPORTNAME'] = "Bidding_Security_B"
		}
		if(params.biddingSecurity == 'C'){
			params['P_REPORTNAME'] = "Bidding_Security_C"
		}
		else if(params.biddingSecurity == 'D'){
			params['P_REPORTNAME'] = "Bidding_Security_D"
		}
		if(params.biddingSecurity == 'E'){
			params['P_REPORTNAME'] = "Bidding_Security_E"
		}
		else if(params.biddingSecurity == 'F'){
			params['P_REPORTNAME'] = "Bidding_Security_F"
		}
		else if(params.biddingSecurity == 'G'){
			params['P_REPORTNAME'] = "Bidding_Security_G"
		}
		
		params['specificRptPath'] = 'DSLC Bidding' 
		
		params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
		params['documentNumber'] = params.documentNumber
		params['etsDate'] = params.etsDate
		
		params['entityRepresentative'] = 'Cesar M. Hipona, Jr.'
		params['entityRepresentativePosition'] = 'OIC - Assistant District Engineer'
		params['biddingAddress'] = 'Bayugan Section, Agusan del Sur'
		params['bank'] = 'United Coconut Planters Bank'
		params['bankBranch'] = 'Ozamiz'
		params['bankAddress'] = 'Trade Services Department, UCPB Executive Offices, Makati Avenue, Makati City'
		params['currencyInWords'] = 'PESOS'
		params['amountInWords'] = 'FIFTY NINE THOUSAND'
		params['approver'] = 'Jonathan Maligaya'
		params['approverPosition'] = 'Branch Manager'
		params['approverBranch'] = 'Daet Branch'
		params['branchManager'] = 'MERVYN NICASIO M. MAGNO, JR.'
		params['swornDate'] = new SimpleDateFormat("dd'th Day of' MMMMM, yyyy").format(new Date())
		params['swornAddress'] = 'Butuan City, Philippines'
		params['evidenceOfIdentity'] = '0808283948'
		
		params['format'] = "docx"
		
		println ""
		println "params: "+params
		
		String fieldsEmptyString = params['fieldsEmpty']
		Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
		
		
		def ext_params = [:]
		ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
		ext_params['userId'] = params['userId']?:"123456"
		ext_params['name'] = params['name']?:"giancarlo"
		ext_params['schconid'] = params['schconid']?:"angulo"
		ext_params['rptPath'] = 'web-app/reports/'
		ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
		ext_params['SUBREPORT_DIR']=ext_params['reportFolder'][-1].equals("/")?ext_params['reportFolder']: ext_params['reportFolder']+"/"

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
	
	//bren--------------------------------------------------------------------------
	def runPs8 = {
		
	def token = params.token ?: 'PS_8'
		
	params['P_REPORTNAME'] = "PS_8"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = 'docu1'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['approver1'] = 'OFFICER'
	params['approverPosition1'] = 'OFFICER POSITION'
	params['approver2'] = 'OFFICER 2'
	params['approverPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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

	def runPs9 = {
		
	def token = params.token ?: 'PS_9'
		
	params['P_REPORTNAME'] = "PS_9"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = 'docu1'
	params['amountInWords'] = 'AMOUNT IN WORDS TEST'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['authorizedOfficer1'] = 'OFFICER'
	params['authorizedOfficerPosition1'] = 'OFFICER POSITION'
	params['authorizedOfficer2'] = 'OFFICER 2'
	params['authorizedOfficerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	
	def runPs10 = {
		
	def token = params.token ?: 'PS_10'
		
	params['P_REPORTNAME'] = "PS_10"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = 'docu1'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['amountInWords'] = 'AMOUNT WORDS'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['signatory1'] = 'OFFICER'
	params['signatoryPosition1'] = 'OFFICER POSITION'
	params['signatory2'] = 'OFFICER 2'
	params['signatoryPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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

	
	
	def runRetention3 = {
		
	def token = params.token ?: 'Retention_3'
		
	params['P_REPORTNAME'] = "Retention_3"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runRetention4 = {
		
	def token = params.token ?: 'Retention_4'
		
	params['P_REPORTNAME'] = "Retention_4"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runRetention5 = {
		
	def token = params.token ?: 'Retention_5'
		
	params['P_REPORTNAME'] = "Retention_5"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runRetention6 = {
		
	def token = params.token ?: 'Retention_6'
		
	params['P_REPORTNAME'] = "Retention_6"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runRetentionMoney3 = {
		
	def token = params.token ?: 'Retention_Money_3'
		
	params['P_REPORTNAME'] = "Retention_Money_3"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runRetention = {
		
	def token = params.token ?: 'Retention'
		
	params['P_REPORTNAME'] = "Retention"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runWarrantyBond2 = {
		
	def token = params.token ?: 'Warranty_Bond_2'
		
	params['P_REPORTNAME'] = "Warranty_Bond_2"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format

	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runWarrantyBond = {
		
	def token = params.token ?: 'Warranty_Bond'
		
	params['P_REPORTNAME'] = "Warranty_Bond"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runSuretyBond2 = {
		
	def token = params.token ?: 'Surety_Bond_2'
		
	params['P_REPORTNAME'] = "Surety_Bond_2"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	
	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runSuretyBond = {
		
	def token = params.token ?: 'Surety_Bond'
		
	params['P_REPORTNAME'] = "Surety_Bond"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format

	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	
	def runWarrantySecurity = {
		
	def token = params.token ?: 'Warranty_Security'
		
	params['P_REPORTNAME'] = "Warranty_Security"
	//params['QUERY_REPORT'] = "1000||Laguna"
	params['specificRptPath'] = 'DMLC/DMLC Opening/DSLC Performance/'       //Follow the '/' format
	

	params['logo'] = servletContext.getRealPath('reports\\img\\ucpb_report_logo.jpg')
	params['lcNo'] = params.etsNumber ?: 'LC NUMBER'
	params['issueDate'] = new Date()
	params['beneficiaryName'] = 'BENEFICIARY NAME'
	params['beneficiaryAddress'] = 'BENEFICIARY ADDRESS'
	params['accounteeName'] = 'CLIENT NAME'
	params['accounteeAddress'] = 'CLIENT ADDRESS'
	params['noticeOfAwardDate'] = new Date()
	params['amountLiteralNominal'] = 'AMOUNT LITERAL NOMINAL'
	params['contract'] = 'CONTRACT'
	params['ucpbBankBranch'] = 'BANK NAME'
	params['expiryDate'] = new Date()
	params['placeOfCourt'] = 'PLACE OF COURT'
	params['officer'] = 'OFFICER'
	params['officerPosition'] = 'OFFICER POSITION'
	params['officer2'] = 'OFFICER 2'
	params['officerPosition2'] = 'OFFICER POSITION 2'
	params['format'] = "docx"
	
	println ""
	println "params: "+params
	
	String fieldsEmptyString = params['fieldsEmpty']
	Boolean fieldsEmpty = fieldsEmptyString=='1'?false:true
					
					
	def ext_params = [:]
	ext_params['P_REPORTNAME'] = params['P_REPORTNAME'] ?: "sample-jasper-plugin"
	ext_params['userId'] = params['userId']?:"123456"
	ext_params['name'] = params['name']?:"giancarlo"
	ext_params['schconid'] = params['schconid']?:"angulo"
	ext_params['rptPath'] = 'web-app/reports/'
	ext_params['reportFolder'] = 'web-app/reports/' + params['specificRptPath']
			
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
	

//end ---------------------------------------------------------------	
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
			def repPath = ext_params['rptPath'] ?: 'web-app/reports/'
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
	
	
	private generateGenericReport( reportData, format, ext_params) {

		def repNameTemplate = ext_params['P_REPORTNAME']
		def repName = ext_params['rptFilename'] ?: repNameTemplate
		def repPath = ext_params['rptPath'] ?: 'web-app/reports/'
		def userId = ext_params['userId']
		def dateTime = "1983_08_04" //Update to proper date time
		//def fileName = repName + "_" + userId + "_" + dateTime
		def fileName = ext_params['token']
		def fileExtension = format

		String reportFolder = ext_params['reportFolder']
		println "reportFolder:"+reportFolder


		def file = repPath + fileName + "." + fileExtension

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

		//For including logo and other graphics
		//ext_params['P_IMAGE_LOGO'] = servletContext.getRealPath('reports/NHMFC-Logo.png')

		JasperReportDef reportDef = new JasperReportDef(folder: reportFolder, name: repNameTemplate, fileFormat: formatx)
		reportDef.parameters = ext_params
		if (reportData != null) {
			println(reportData.size)
			reportDef.reportData = reportData // list of objects to be displayed in the report
		}
		reportDef.contentStream = jasperService.generateReport(reportDef)

		response.setHeader "Content-disposition", contentDisp + "; filename=" + file
		response.contentType = contentType
		response.characterEncoding = "UTF-8"
		response.outputStream << reportDef.contentStream.toByteArray()

	}
}
