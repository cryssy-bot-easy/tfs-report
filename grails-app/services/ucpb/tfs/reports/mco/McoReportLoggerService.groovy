package ucpb.tfs.reports.mco

import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils
import org.apache.commons.io.IOUtils
import com.ucpb.tfs.report.mco.LogRecord

class McoReportLoggerService {
	
	def grailsApplication
	private StringBuilder builder = new StringBuilder()
	private final String NEWLINE = "\r\n"
	private final String SEPARATOR = "\t"
	private final String EMPTY = ""
	private final String PADCHAR = " "
	private int padSize = 20
	private final int EXTRAPADFORDOCUMENTNUMBER = 21
	
	private List<LogRecord> UPTO6DAYS = new ArrayList<LogRecord>();
	private List<LogRecord> UPTO4WEEKS = new ArrayList<LogRecord>();
	private List<LogRecord> UPTO2MONTHS = new ArrayList<LogRecord>();
	private List<LogRecord> UPTO3MONTHS = new ArrayList<LogRecord>();
	private List<LogRecord> UPTO4MONTHS = new ArrayList<LogRecord>();
	private List<LogRecord> UPTO6MONTHS = new ArrayList<LogRecord>();
	private List<LogRecord> UPTO12MONTHS = new ArrayList<LogRecord>();
	private List<LogRecord> UPTO4YEARS = new ArrayList<LogRecord>();
	private List<LogRecord> UPTO9YEARS = new ArrayList<LogRecord>();
	private List<LogRecord> UPTO9YEARSBEYOND = new ArrayList<LogRecord>();

	private DecimalFormat decimalFormat = new DecimalFormat("#,##0.00;(#,##0.00)")
	
	public McoReportLoggerService(){
		decimalFormat.setParseBigDecimal(true)
	}
	
    def writeToFile(String onlineReportDate){
		String month = ''
		String date = ''
		if(onlineReportDate != null && !onlineReportDate.isEmpty()){
			String[] dateGenerated = onlineReportDate.split("/")
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
		def fileName = prefix + "TFS_MCO_REPORT_LOG"

		File f = new File(grailsApplication.config.tfs.batch.output.directory + File.separator + fileName + ".txt")
		File batchDir = new File(grailsApplication.config.tfs.batch.output.directory)

		if(!batchDir.exists()){
			batchDir.mkdir()
		}
		FileOutputStream fos = null
		try{
			fos = new FileOutputStream(f)
			IOUtils.write(builder.toString(), fos)
			fos.close()
		}catch(Exception e){
			if(!e.printStackTrace()){
				println e.getCause()?.getMessage()
			}
		}finally{
			IOUtils.closeQuietly(fos)
		}
    }
	
	private void setAgeLegend(){
		builder.append(NEWLINE).
			append("AGE COLUMNS:").append(NEWLINE).
			append("A - O/N - 6Days: 1-6 days").append(NEWLINE).
			append("B - 1 - 4 Weeks: 7-30 days").append(NEWLINE).
			append("C - Greater than 1 mo up to 2 mos: 31-60 days").append(NEWLINE).
			append("D - Greater than 2 mo up to 3 mos: 61-90 days").append(NEWLINE).
			append("E - Greater than 3 mo up to 4 mos: 91-120 days").append(NEWLINE).
			append("F - Greater than 4 mo up to 6 mos: 121-180 days").append(NEWLINE).
			append("G - Greater than 6 mo up to 12 mos: 181-365 days").append(NEWLINE).
			append("H - Greater than 1 yr up to 4 yrs: 366-above").append(NEWLINE).
			append("I - Greater than 4 yr up to 9 yrs: 1461-above").append(NEWLINE).
			append("J - Greater than 9 yrs: 3288-above").append(NEWLINE)
	}
	
	public void setRates(List<Map> ratesList){
		builder = new StringBuilder();
		builder.append("RATES:").append(NEWLINE).
				append(addString("CALENDAR DATE",PADCHAR,padSize)).append(SEPARATOR).
				append(addString("RATE NUMBER",PADCHAR,padSize)).append(SEPARATOR).
				append(addString("CONVERSION RATE",PADCHAR,padSize)).append(SEPARATOR).
				append(addString("BASE CURRENCY",PADCHAR,padSize)).append(SEPARATOR).
				append("CURRENCY CODE").
				append(NEWLINE)
		ratesList.each {
			builder.append(addString(it.CALENDAR_DATE.toString(),PADCHAR,padSize)).append(SEPARATOR).
					append(addString(it.RATE_NUMBER.toString(),PADCHAR,padSize)).append(SEPARATOR).
					append(addString(it.CONVERSION_RATE.toString(),PADCHAR,padSize)).append(SEPARATOR).
					append(addString(it.BASE_CURRENCY.toString(),PADCHAR,padSize)).append(SEPARATOR).
					append(it.CURRENCY_CODE.toString()).
					append(NEWLINE)
		}
	}
	
	public void setTitleHeader(String title){
		builder.append(NEWLINE).
				append("=========== "+ title + " ===========").
				append(NEWLINE)
				setHeaders()
	}
	
	public void setTotal(String total){
		builder.append(addString(EMPTY,PADCHAR,padSize+EXTRAPADFORDOCUMENTNUMBER)).append(SEPARATOR).
				append(addString(EMPTY,PADCHAR,padSize)).append(SEPARATOR).
				append(addString(EMPTY,PADCHAR,padSize)).append(SEPARATOR).
				append(addLeftString("TOTAL:",PADCHAR,padSize)).append(SEPARATOR).
				append(addString(total,PADCHAR,padSize)).append(NEWLINE).append(NEWLINE)
	}
	
	public void setTotal(String label,String total){
		builder.append(addString(EMPTY,PADCHAR,padSize+EXTRAPADFORDOCUMENTNUMBER)).append(SEPARATOR).
				append(addString(EMPTY,PADCHAR,padSize)).append(SEPARATOR).
				append(addString(EMPTY,PADCHAR,padSize)).append(SEPARATOR).
				append(addLeftString(label,PADCHAR,padSize)).append(SEPARATOR).
				append(addString(total,PADCHAR,padSize)).append(NEWLINE).append(NEWLINE)
	}
	
	private void store(int age,String documentNumber,String originalCurrency,String originalAmount,
		String convertedCurrency,String convertedAmount,String expiryDate,String issueDate,String runDate){
		if(age<=6){
			UPTO6DAYS.add(new LogRecord(
					documentNumber,originalCurrency,originalAmount,convertedCurrency,convertedAmount,
					expiryDate,issueDate,runDate,age
				));
		}
		else if(age>6&&age<=30){
			UPTO4WEEKS.add(new LogRecord(
				documentNumber,originalCurrency,originalAmount,convertedCurrency,convertedAmount,
				expiryDate,issueDate,runDate,age
			));
		}
		else if(age>30&&age<=60){
			UPTO2MONTHS.add(new LogRecord(
				documentNumber,originalCurrency,originalAmount,convertedCurrency,convertedAmount,
				expiryDate,issueDate,runDate,age
			));
		}
		else if(age>60&&age<=90){
			UPTO3MONTHS.add(new LogRecord(
				documentNumber,originalCurrency,originalAmount,convertedCurrency,convertedAmount,
				expiryDate,issueDate,runDate,age
			));
		}
		else if(age>90&&age<=120){
			UPTO4MONTHS.add(new LogRecord(
				documentNumber,originalCurrency,originalAmount,convertedCurrency,convertedAmount,
				expiryDate,issueDate,runDate,age
			));
		}
		else if(age>120&&age<=180){
			UPTO6MONTHS.add(new LogRecord(
				documentNumber,originalCurrency,originalAmount,convertedCurrency,convertedAmount,
				expiryDate,issueDate,runDate,age
			));
		}
		else if(age>180&&age<=360){
			UPTO12MONTHS.add(new LogRecord(
				documentNumber,originalCurrency,originalAmount,convertedCurrency,convertedAmount,
				expiryDate,issueDate,runDate,age
			));
		}
		else if(age>360&&age<=1440){
			UPTO4YEARS.add(new LogRecord(
				documentNumber,originalCurrency,originalAmount,convertedCurrency,convertedAmount,
				expiryDate,issueDate,runDate,age
			));
		}
		else if(age>1440&&age<=3240){
			UPTO9YEARS.add(new LogRecord(
				documentNumber,originalCurrency,originalAmount,convertedCurrency,convertedAmount,
				expiryDate,issueDate,runDate,age
			));
		}
		else if(age>3240){
			UPTO9YEARSBEYOND.add(new LogRecord(
				documentNumber,originalCurrency,originalAmount,convertedCurrency,convertedAmount,
				expiryDate,issueDate,runDate,age
			));
		}
	}

	private void setHeaders(){
		builder.append(addString("Document Number",PADCHAR,padSize+EXTRAPADFORDOCUMENTNUMBER)).append(SEPARATOR).
				append(addString("Original Currency",PADCHAR,padSize)).append(SEPARATOR).
				append(addString("Original Amount",PADCHAR,padSize)).append(SEPARATOR).
				append(addString("Converted Currency",PADCHAR,padSize)).append(SEPARATOR).
				append(addString("Converted Amount",PADCHAR,padSize)).append(SEPARATOR).
				append(addString("Expiry Date",PADCHAR,padSize)).append(SEPARATOR).
				append(addString("Issue Date",PADCHAR,padSize)).append(SEPARATOR).
				append(addString("Run Date",PADCHAR,padSize)).append(SEPARATOR).
				append(addString("Age in Days",PADCHAR,padSize)).
				append(NEWLINE)
	}
	
	public void startLog(String total){
		writeDuration(UPTO6DAYS,"A")
		writeDuration(UPTO4WEEKS,"B")
		writeDuration(UPTO2MONTHS,"C")
		writeDuration(UPTO3MONTHS,"D")
		writeDuration(UPTO4MONTHS,"E")
		writeDuration(UPTO6MONTHS,"F")
		writeDuration(UPTO12MONTHS,"G")
		writeDuration(UPTO4YEARS,"H")
		writeDuration(UPTO9YEARS,"I")
		writeDuration(UPTO9YEARSBEYOND,"J")
		setTotal(total)
	}
	
	private void writeDuration(List<LogRecord> duration,String durationKey){
		BigDecimal totalTemp = new BigDecimal("0.00")
		String columnKey = "COLUMN"
		if(!duration.isEmpty()){
			for(LogRecord lr : duration){
				builder.append(addString(lr.getDocumentNumber(),PADCHAR,padSize+EXTRAPADFORDOCUMENTNUMBER)).append(SEPARATOR).
						append(addString(lr.getOriginalCurrency(),PADCHAR,padSize)).append(SEPARATOR).
						append(addString(decimalFormat.format(new BigDecimal(lr.getOriginalAmount())),PADCHAR,padSize)).append(SEPARATOR).
						append(addString(lr.getConvertedCurrency(),PADCHAR,padSize)).append(SEPARATOR).
						append(addString(decimalFormat.format(new BigDecimal(lr.getConvertedAmount())),PADCHAR,padSize)).append(SEPARATOR).
						append(addString(lr.getExpiryDate(),PADCHAR,padSize)).append(SEPARATOR).
						append(addString(lr.getIssueDate(),PADCHAR,padSize)).append(SEPARATOR).
						append(addString(lr.getRunDate(),PADCHAR,padSize)).append(SEPARATOR).
						append(addString(lr.getAge().toString(),PADCHAR,padSize)).
						append(NEWLINE)
				totalTemp= totalTemp.add(new BigDecimal(lr.getConvertedAmount()))
			}
			
			builder.append(addString("COLUMN TOTAL: ",PADCHAR,padSize)).append(SEPARATOR).
					append(addString(EMPTY,PADCHAR,padSize+EXTRAPADFORDOCUMENTNUMBER)).append(SEPARATOR).
					append(addString(EMPTY,PADCHAR,padSize)).append(SEPARATOR).
					append(addLeftString(columnKey + " " + durationKey,PADCHAR,padSize)).append(SEPARATOR).
					append(addString(decimalFormat.format(totalTemp),PADCHAR,padSize)).append(NEWLINE).append(NEWLINE)
		}
	}
	
	private String convertAge(int age){
		String result
		int convertedAge = 0
		if((age / 30) < 1){
			convertedAge = age
			result = "$convertedAge DAY/S"
		}else{
			convertedAge = age / 30
			result = "$convertedAge MONTH/S"
		}
		result = "$age DAY/S"
		return result
	}
	
	private String addString(String source,String padChar,int repeat){
		return StringUtils.rightPad(source, repeat, padChar)
	}
	
	private String addLeftString(String source,String padChar,int repeat){
		return StringUtils.leftPad(source, repeat, padChar)
	}
	
	public void appendToBuilder(StringBuilder builder){
		this.builder.append(builder.toString() + NEWLINE)
	}
	
	public StringBuilder getBuilder(){
		return this.builder
	}
	
	public void setPadSize(int padSize){
		this.padSize=padSize;
	}
}