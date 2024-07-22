package ucbp.tfs.reports.fxform1

import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.List
import java.util.Map

class FxForm1DbfWriterService {
	
	BufferedOutputStream stream
	def grailsApplication
	
	public String generateDbfFile(reportData, fieldHeaders, Map dbfParams){
		stream = null
		int fxform1Output = 0
		String filePath = grailsApplication.config.tfs.glfxform1.directory + File.separator + dbfParams.get("fileName") + ".DBF"
		
		File dbfDir = new File(grailsApplication.config.tfs.glfxform1.directory + File.separator)
		if(!dbfDir.exists()){
			dbfDir.mkdir()
		}
		
		while(fxform1Output < 2){
			if(fxform1Output > 0){
				SimpleDateFormat simpledateformat = new SimpleDateFormat("MMdd")
				filePath = grailsApplication.config.tfs.glfxform1.backup + File.separator + "TFS_" + simpledateformat.format(dbfParams.get("reportDate")) + "_SCHED" + dbfParams.get("scheduleNo") + "_" + dbfParams.get("fileName") + ".DBF"
			
				File dbfBackupDir = new File(grailsApplication.config.tfs.glfxform1.backup + File.separator)
				if(!dbfBackupDir.exists()){
					dbfBackupDir.mkdir()
				}
			}
		
			init(new FileOutputStream(filePath), fieldHeaders)
	
			int counter = 0
			Object[][] records = new Object[reportData.size()][fieldHeaders.size()]
	
			for(Map<String, Object> mapReportData: reportData){
	
				for (int c = 0; c < fieldHeaders.size(); c++) {
					String fieldName = fieldHeaders.get(c).get("Name")
					records[counter][c] = mapReportData.get(fieldName)
				}
				counter++
			}

			int recCount = 0
		
			for (int x = 0; x < reportData.size(); x++) {
				addRecord(records[x], fieldHeaders)
				recCount++
			}
			close(recCount, filePath)
			fxform1Output++
			
			println ">>>>>>" + filePath + " write finished......."
		}
		
		return dbfParams.get("fileName") + ".DBF"
	}

	public void init(OutputStream outputstream, List<Map<String,Object>> fieldList)throws FxForm1DbfExceptionService {
		try {
			stream = new BufferedOutputStream(outputstream)
			writeHeader(fieldList)
		
			for (int i = 0; i < fieldList.size(); i++) {
				writeFieldHeader(fieldList.get(i))
			}
			
			stream.write(13)
			stream.flush()
		} catch (Exception exception) {
			throw new FxForm1DbfExceptionService(exception)
		}
	}

	private void writeHeader(List<Map<String,Object>> fieldList) throws IOException {
		byte[] abyte0 = new byte[16]
		abyte0[0] = 3
		Calendar calendar = Calendar.getInstance()
		abyte0[1] = (byte) (calendar.get(1) - 1900)
		abyte0[2] = (byte) calendar.get(2)
		abyte0[3] = (byte) calendar.get(5)
		abyte0[4] = 0
		abyte0[5] = 0
		abyte0[6] = 0
		abyte0[7] = 0
		int i = (fieldList.size() + 1) * 32 + 1
		abyte0[8] = (byte) (i % 256)
		abyte0[9] = (byte) (i / 256)
		int j = 1
		
		for (int k = 0; k < fieldList.size(); k++) {
			j += fieldList.get(k).get("Length")
		}
		
		abyte0[10] = (byte) (j % 256)
		abyte0[11] = (byte) (j / 256)
		abyte0[12] = 0
		abyte0[13] = 0
		abyte0[14] = 0
		abyte0[15] = 0
		stream.write(abyte0, 0, abyte0.length)
		
		for (int l = 0; l < 16; l++) {
			abyte0[l] = 0
		}
		stream.write(abyte0, 0, abyte0.length)
	}

	private void writeFieldHeader(Map fieldHeaders) throws IOException {
		byte[] abyte0 = new byte[16]
		String headerName = fieldHeaders.get("Name")
		char headerType = fieldHeaders.get("Type")
		Integer headerLength = fieldHeaders.get("Length")
		Integer headerDecimal = fieldHeaders.get("Decimal")
		int i = headerName.length()
		
		if (i > 10) {
			i = 10
		}
		for (int j = 0; j < i; j++) {
			abyte0[j] = (byte) headerName.charAt(j)
		}
		for (int k = i; k <= 10; k++) {
			abyte0[k] = 0
		}
		abyte0[11] = (byte) headerType
		abyte0[12] = 0
		abyte0[13] = 0
		abyte0[14] = 0
		abyte0[15] = 0
		stream.write(abyte0, 0, abyte0.length)
		for (int l = 0; l < 16; l++) {
			abyte0[l] = 0
		}
		abyte0[0] = (byte) headerLength
		abyte0[1] = (byte) headerDecimal
		stream.write(abyte0, 0, abyte0.length)
	}

	public void addRecord(Object records, List<Map<String,Object>> fieldList) throws FxForm1DbfExceptionService {
		if (records.length != fieldList.size()) {
			throw new FxForm1DbfExceptionService("Error adding record: Wrong number of values. Expected "
						+ fieldList.size() + ", got " + records.length + ".")
		}
		int i = 0
	
		for (int j = 0; j < fieldList.size(); j++) {
			i += fieldList.get(j).get("Length")
		}

		byte[] totalByteLength = new byte[i]
		int k = 0

		for (int l = 0; l < fieldList.size(); l++) {
			char fieldType = fieldList.get(l).get("Type")
			String recordToOutput = formatRecord(records[l], fieldType , fieldList.get(l).get("Decimal"), fieldList.get(l).get("Length"), fieldList.get(l).get("Name"))
		
			byte[] byteLength
			try {
				byteLength = recordToOutput.getBytes()
			} catch (UnsupportedEncodingException unsupportedencodingexception) {
				throw new FxForm1DbfExceptionService(unsupportedencodingexception)
			}
			
			for (int x = 0; x < fieldList.get(l).get("Length"); x++) {
				totalByteLength[k + x] = byteLength[x]
			}
			k += fieldList.get(l).get("Length")
		}

		try {
			stream.write(32)
			stream.write(totalByteLength, 0, totalByteLength.length)
			stream.flush()
		} catch (IOException ioexception) {
			throw new FxForm1DbfExceptionService(ioexception)
		}
		
	}

	public void close(int recCount, String filePath) throws FxForm1DbfExceptionService {
		try {
			stream.write(26)
			stream.close()
			stream = null
			RandomAccessFile randomaccessfile = new RandomAccessFile(filePath, "rw")
			randomaccessfile.seek(4L)
			
			byte[] totalByteLength = new byte[4]
			totalByteLength[0] = (byte) (recCount % 256)
			totalByteLength[1] = (byte) (new Float(recCount / 256) % 256) 
			totalByteLength[2] = (byte) (new Float(recCount / 0x10000) % 256)
			totalByteLength[3] = (byte) (new Float(recCount / 0x1000000) % 256)
			
			randomaccessfile.write(totalByteLength, 0, totalByteLength.length)
			randomaccessfile.close()
		} catch (IOException ioexception) {
			throw new FxForm1DbfExceptionService(ioexception)
		}
	}

	public String formatRecord(Object record, char fieldType, Integer fieldDecimal, Integer fieldLength, String fieldName) throws FxForm1DbfExceptionService {

		if (fieldType == 'N') {
			if (record == null || record.toString().trim() == "") {
				record = new Double(0.0D)
			}
		
			if(fieldName.contains("DATE") || fieldName.contains("DTE")){
				Date date
				try{
					//convert '04/01/2014' to 20140401
					date = new Date(record)
					SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd")
					record = simpledateformat.format(date)
				}catch(Exception e){
					// do nothing
				}
			}
		
			record = new Double(record.toString().replaceAll(",", ""))
		
			if (record instanceof Number) {
				Number number = (Number) record
				StringBuffer stringbuffer = new StringBuffer(fieldLength)
			
				for (int i = 0; i < fieldLength; i++) {
					stringbuffer.append("#")
				}
			
				if (fieldDecimal > 0) {
					char decimalPoint = '.'
					stringbuffer.setCharAt(fieldLength - fieldDecimal - 1,decimalPoint)
				}
			
				DecimalFormat decimalformat = new DecimalFormat(stringbuffer.toString())
				String s1 = decimalformat.format(number)
				int k = fieldLength - s1.length()
			
				if (k < 0) {
					throw new FxForm1DbfExceptionService("Value " + number + " cannot fit in pattern: '" + stringbuffer + "'.")
				}
			
				StringBuffer stringbuffer2 = new StringBuffer(k)
		
				for (int l = 0; l < k; l++) {
					stringbuffer2.append(" ")
				}
			
				return stringbuffer2 + s1
		
			} else {
				throw new FxForm1DbfExceptionService("Expected a Number, got " + record.getClass() + ".")
			}
		}
	
		if (fieldType == 'C') {
			if (record == null) {
				record = ""
			}
			if (record instanceof String) {
				String s = (String) record
			
				if (s.length() > fieldLength) {
					throw new FxForm1DbfExceptionService("'" + record + "' is longer than " + fieldLength + " characters.")
				}
			
				StringBuffer stringbuffer1 = new StringBuffer(fieldLength - s.length())
			
				for (int j = 0; j < fieldLength - s.length(); j++) {
					stringbuffer1.append(' ')
				}
			
				return s + stringbuffer1
			} else {
				throw new FxForm1DbfExceptionService("Expected a String, got " + record.getClass() + ".")
			}
		}
	}
}
