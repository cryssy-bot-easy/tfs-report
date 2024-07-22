package com.incuventure.utilities

import java.text.DecimalFormat
import java.text.SimpleDateFormat

class ReportIdService {

	//TEMPORARY - report number should be extracted from database because clients are
	//logged in through different computers.
	private static int reportNumber=0
	
	private DecimalFormat nf=new DecimalFormat("00000")
	private final String YEAR=new SimpleDateFormat('yy').format(new Date())
	
	
	
	/**
	 * Generates report ID 
	 * @param rptFreqCode - 5-letter code that contains the 1st letter as frequency, the rest of the letters are report code
	 * @return
	 */
	public String generateReportId(String rptFreqCode){
		reportNumber++	
		//return "NTF-$rptFreqCode-$YEAR-"+nf.format(reportNumber)
		return "NTF$rptFreqCode"
	}
}