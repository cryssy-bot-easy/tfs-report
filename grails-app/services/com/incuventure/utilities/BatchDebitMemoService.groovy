package com.incuventure.utilities

import org.codehaus.groovy.grails.plugins.jasper.JasperReportDef
import org.codehaus.groovy.grails.plugins.jasper.JasperExportFormat
import net.sf.jasperreports.engine.JasperExportManager
import org.apache.commons.io.IOUtils
import net.ipc.utils.DateUtils
import net.ipc.utils.JsonUtil
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.List
import java.util.Map
import report.utils.ReportUtility
import java.util.zip.*

class BatchDebitMemoService {
	def jasperService
	def grailsApplication
	def reportDbService
	def servletContext
	def commandBusService
	def reportDirectory

	public List<Map<String,Object>> getBatchDebitMemoDataList(String onlineReportDate, String unitCode){
		String getBatchDebitMemoDetailsQuery = 
		"""
		SELECT 
			DATE(CP.DATEPAID) AS DATEPAID, 
			CP.PAYMENT_REF, 
			PD.ACCOUNTNAME, 
			CP.CLIENT_NAME,
			PD.REFERENCENUMBER, 
			PD.CURRENCY, 
			PD.AMOUNT, 
			'' AS AMOUNTINWORDS,
			NVL(CP.AMOUNT, 0) AS CDTAMOUNT,
			NVL(CP.BANKCHARGE, 0) AS BANKCHARGE,
			(NVL(CP.AMOUNT, 0) + NVL(CP.BANKCHARGE, 0)) AS TOTALAMOUNTDUE,
			CP.IEDIEIRDNO,	
			CP.DOCNUMBER, 
			SE.FULLNAME, 
			TS.CREATEDBY
		FROM TRADESERVICE TS
		LEFT JOIN PAYMENT PA ON PA.TRADESERVICEID=TS.TRADESERVICEID
		LEFT JOIN PAYMENTDETAIL PD ON PD.PAYMENTID=PA.ID
		LEFT JOIN CDTPAYMENTREQUEST CP ON CP.IEDIEIRDNO=TS.TRADESERVICEREFERENCENUMBER
		LEFT JOIN SEC_EMPLOYEE SE ON UPPER(SE.ID)=UPPER(TS.CREATEDBY)
		WHERE 
			TS.DOCUMENTCLASS='CDT' 
			AND TS.SERVICETYPE='PAYMENT' 
			AND PD.PAYMENTINSTRUMENTTYPE='CASA' 
			AND CP.STATUS='SENTTOBOC'
			AND DATE(CP.DATEPAID)='$onlineReportDate'
			AND CP.UNITCODE='$unitCode' 
		"""

		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>()
		Map<String, Object> resultMap = new HashMap<String, Object>()
		List<Object> tempResultList = new ArrayList<Object>()
		def batchDebitMemoDetailsList = reportDbService.getResultFromQuery(getBatchDebitMemoDetailsQuery, "")

		Iterator<Object> iterator = batchDebitMemoDetailsList.iterator();
		while (iterator.hasNext()) {
			tempResultList = iterator.next()
			resultMap = new HashMap<String, Object>()
			resultMap.put("PAIDDATE", tempResultList.get(0)?: null)
			resultMap.put("PAYMENT_REF", tempResultList.get(1)?: null)
			resultMap.put("ACCOUNTNAME", tempResultList.get(2)?: null)
			resultMap.put("CLIENT_NAME", tempResultList.get(3)?: null)
			resultMap.put("ACCOUNTNUMBER", tempResultList.get(4)?: null)
			resultMap.put("CURRENCY", tempResultList.get(5)?: null)
			resultMap.put("AMOUNT", tempResultList.get(6)?: null)
			resultMap.put("AMOUNTINWORDS",ReportUtility.convert(tempResultList.get(6) ?: new BigDecimal(0.00), tempResultList.get(5) ?: "PHP") ?: null)
			resultMap.put("CDTAMOUNT", tempResultList.get(8)?: null)
			resultMap.put("BANKCHARGE", tempResultList.get(9)?: null)
			resultMap.put("TOTALAMOUNTDUE", tempResultList.get(10)?: null)
			resultMap.put("IEDIEIRDNO", tempResultList.get(11)?: null)
			resultMap.put("DOCNUMBER", tempResultList.get(12)?: null)
			resultMap.put("FULLNAME", tempResultList.get(13)?: null)
			resultMap.put("CREATEDBY", tempResultList.get(14)?: null)
			resultList.add(resultMap)
			tempResultList = null
		}
		return resultList
	}
	
	private String generateBatchDebitReport( reportData, format, ext_params) {
		def repNameTemplate = ext_params['P_REPORTNAME']
		def repName = ext_params['rptFilename'] ?: repNameTemplate
		def repPath = ext_params['rptPath'] ?: 'reports/'
		def userId = ext_params['userId']
		def dateTime = "1983_08_04"
		def fileExtension = format
		def fileName = "Batch_Debit_Memo"
		def formatx = JasperExportFormat.PDF_FORMAT

		for(Map<String, Object> mapReportData: reportData) {
			 String clientName = mapReportData.get("CLIENT_NAME") ?: "Batch_Debit_Memo"
			 String idNo = mapReportData.get("IEDIEIRDNO") ? mapReportData.get("IEDIEIRDNO").toString() : ""
			 String date = mapReportData.get("PAIDDATE") ? mapReportData.get("PAIDDATE").toString() : ""
			 
			 String tempPath = servletContext.getRealPath("/").toString()
			 
			 if(tempPath.substring(tempPath.length() - 1) != "/" && tempPath.substring(tempPath.length() - 1) != "\\") {
				 if(tempPath.contains("/")) {
					 tempPath = tempPath + "/"
				 } else {
				 	tempPath = tempPath + "\\"
				 }
			 } 
			 
			 String reportFolder = tempPath  + ext_params['reportFolder']
			 
			 println "reportFolder >>>>>>>>>> " + tempPath + " " + ext_params['reportFolder']
			 
			 String file = ''
  
			 fileName = clientName.replaceAll("/" , " ") + " " + idNo + " " + date
			 file = fileName + "." + fileExtension

			 JasperReportDef reportDef = new JasperReportDef(folder: reportFolder, name: repNameTemplate, fileFormat: formatx)
			 reportDef.parameters = ext_params

			 if (reportData != null) {
				 reportDef.reportData = reportData
			 } else {
				  println "null reportData"
			 }
		
			 int fileNameCounter = 1;
			 String tempFileName = grailsApplication.config.tfs.batchdebitmemo.directory + File.separator + fileName + ".${fileExtension}"
			 while(Exists(tempFileName)){
				 tempFileName = grailsApplication.config.tfs.batchdebitmemo.directory + File.separator + fileName + "(" + fileNameCounter + ")" + ".${fileExtension}"
				 fileNameCounter++;
			 }
			 
			 File f = null
			 f = new File(tempFileName)
			 File batchDir = new File(grailsApplication.config.tfs.batchdebitmemo.directory)
			 if(!batchDir.exists()){
				 batchDir.mkdir()
			 }

			 FileOutputStream fos = null
		
			 try{
				 reportDef.contentStream = jasperService.generateReport(reportDef)
				 fos = new FileOutputStream(f)
				 IOUtils.write(reportDef.contentStream.toByteArray(), fos)
				 fos.close()
			 }catch(Exception e){
				 if(!e.printStackTrace()){
					 println "\n-------" + e + "-------\n"
					 println e.getCause()?.getMessage()
				 }
			 }finally{
				  IOUtils.closeQuietly(fos)
			 }
		}
	}
	
	public static boolean Exists(String tempFileName) {
		return( (tempFileName.length()) > 0 && (new File(tempFileName).exists()) )
	}
	
	public static byte[] zipDirectory(String directoryPath) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		
		File fileFolder = new File(directoryPath)
		String[] listOfFiles = fileFolder.list()
		
		if (listOfFiles != null) {
			ZipOutputStream zos = new ZipOutputStream(baos)
			for(int i = 0; i < listOfFiles.length; i++)
			{
				File generatedReport = new File(fileFolder,listOfFiles[i])
				zos.putNextEntry(new ZipEntry(generatedReport.getName()))
				
				FileInputStream fis = new FileInputStream(generatedReport)
				BufferedInputStream bis = new BufferedInputStream(fis)
				
				int bufferSize = 2048
				byte[] buffer = new byte[bufferSize]
				int count
			
				while((count = bis.read(buffer)) != -1)
				{
					zos.write(buffer,0,count)
				}
				zos.closeEntry()
				bis.close()
				fis.close()
			}
			zos.flush()
			zos.close()
		}
		baos.flush()
		baos.close()

		return baos.toByteArray()
	}
	
	public static void deleteDirectory(String directoryPath) throws IOException {
		File fileFolder = new File(directoryPath)
		
		if(fileFolder.exists()){
			String[] listOfFiles = fileFolder.list()
		
			if (listOfFiles != null) {
				for(int i = 0; i < listOfFiles.length; i++)
				{
					File generatedReport = new File(fileFolder,listOfFiles[i])
					generatedReport.delete()
				}
			}
			fileFolder.delete()
		}
	}
	
}