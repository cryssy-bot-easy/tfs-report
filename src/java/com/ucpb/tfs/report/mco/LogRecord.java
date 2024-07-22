package com.ucpb.tfs.report.mco;

public class LogRecord {

	private String documentNumber;
	private String originalCurrency;
	private String originalAmount;
	private String convertedCurrency;
	private String convertedAmount;
	private String expiryDate;
	private String issueDate;
	private String runDate;
	private int age;
	
	public LogRecord(String documentNumber, String originalCurrency,
			String originalAmount, String convertedCurrency,
			String convertedAmount, String expiryDate, String issueDate,
			String runDate, int age) {
		super();
		this.documentNumber = documentNumber;
		this.originalCurrency = originalCurrency;
		this.originalAmount = originalAmount;
		this.convertedCurrency = convertedCurrency;
		this.convertedAmount = convertedAmount;
		this.expiryDate = expiryDate;
		this.issueDate = issueDate;
		this.runDate = runDate;
		this.age = age;
	}
	
	public String getDocumentNumber() {
		return documentNumber;
	}
	public String getOriginalCurrency() {
		return originalCurrency;
	}
	public String getOriginalAmount() {
		return originalAmount;
	}
	public String getConvertedCurrency() {
		return convertedCurrency;
	}
	public String getConvertedAmount() {
		return convertedAmount;
	}
	public String getExpiryDate() {
		return expiryDate;
	}
	public String getIssueDate() {
		return issueDate;
	}
	public String getRunDate() {
		return runDate;
	}
	public int getAge() {
		return age;
	}
}
