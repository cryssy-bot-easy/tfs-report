package net.ipc.utils

import java.text.SimpleDateFormat
import java.util.Date;

import org.apache.commons.lang.StringUtils;

class DateUtils {

	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat();
	
	private static final String DB2_DATE_FORMAT = "yyyy-MM-dd";
	private static final String SHORT_DATE_FORMAT = "MM/dd/yyyy";
	private static final String TIME_FORMAT = "hh:mm a";
	/**
	 * 
	 * @param date *format: MM/dd/yyyy
	 * @return
	 */
	public static String formatToDb2Date(String date){
		try{
			SIMPLE_DATE_FORMAT.applyPattern(DB2_DATE_FORMAT);
			return SIMPLE_DATE_FORMAT.format(parse(date) ?: new Date());
		}catch(Exception e){
			e.printStackTrace();
		}
		return date;
	}
	
	private static Date parse(String date){
		try{
			SimpleDateFormat parser=new SimpleDateFormat(SHORT_DATE_FORMAT);
			return parser.parse(date);
		}catch(Exception e){
			return null; 
		}
	}
	
	// apply time format
	public static String timeFormat(Date dateToConvert) {
		try {
			SIMPLE_DATE_FORMAT.applyPattern(TIME_FORMAT);

			return SIMPLE_DATE_FORMAT.format(dateToConvert);
		} catch(Exception e) {
			// do nothing
		}

		return StringUtils.EMPTY;
	}
}
