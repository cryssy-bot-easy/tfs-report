package report.enums;

@Deprecated
public enum BatchReport{
	Daily_Summary_of_Accounting_Entries("runDailySummaryOfAccountingEntries","pdf","D"),
	Trade_Services_AMLA_Reported_Transactions("runTradeServicesAMLAReportedTransactions","pdf","D"),
	Outstanding_Inward_Bills_for_Collection_DM_DP("runOutstandingInwardBillsForCollectionDmDaDpPerCurrency","pdf","M"),
	Outstanding_Inward_Bills_for_Collection_FX_DA_DP_OA_DR("runOutstandingInwardBillsForCollectionFxDaDpPerCurrency","pdf","M"),
	DM_Non_LCs_Negotiated_for_the_Year_Classified_by_Top_30_Importer_and_Other_Local_Bank("runDmNonLcsNegoForTheYear","pdf","M"),
	FX_Non_LCs_Negotiated_for_the_Year_Classified_by_Top_30_Importer_and_Remitting_Bank("runFxNonLcsNegoForTheYear","pdf","M");
	
	
	
	private String functionName,fileExt,category;
	
	private BatchReport(String functionName,String fileExt,String category){
		this.functionName=functionName;
		this.fileExt=fileExt;
		this.category=category;
	}
	
	public String getFunctionName(){
		return this.functionName;
	}
	
	public String getFileExtension(){
		return this.fileExt;
	}

	public String getCategory(){
		return this.category;
	}
	
	@Override
	public String toString(){
		return this.toString().replaceAll("_", " ");
	}
}
