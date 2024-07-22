package ucpb.tfs.reports.spad

import grails.converters.JSON;

class ActualCorresChargeService {
	
	def reportDbService

	public List<Map<String,Object>> getCorresChargesDataList(String onlineReportDate){
		String getDetailsQuery = "SELECT VARCHAR(TS.DETAILS) AS DETAILS FROM TRADESERVICE TS " +
			"INNER JOIN CORRESCHARGEACTUAL CA ON TS.TRADESERVICEID=CA.TRADESERVICEID " +
			"WHERE DATE(TS.MODIFIEDDATE)='" + onlineReportDate + "'"

		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>()
		def detailsList = reportDbService.getResultFromQuery(getDetailsQuery, null)
		detailsList.each{
			Map<String,Object> parsedDetails = JSON.parse(it.toString())
			Map<String,Object> resultMap = new HashMap<String,Object>()
			resultMap.put("BANKCDE","000470" ?: null)
			resultMap.put("REFDATE",parsedDetails.processDate ?: null)
			resultMap.put("FORMNO","TRD05" ?: null)
			resultMap.put("TRANCDE","684" ?: null)
			resultMap.put("DESC40","FINANCIAL SERVICES" ?: null)
			resultMap.put("CTRYCDE",'026' ?: null)
			resultMap.put("BENEF",parsedDetails.corresBankCode ?: parsedDetails.reimbursingBank ?: null)
			resultMap.put("REMIT","UCPB" ?: null)
			resultMap.put("BOOKCDE",parsedDetails.accountType ?: null)
			resultMap.put("CURRCDE",parsedDetails.currency ?: null)
			resultMap.put("AMTDOLR",parsedDetails.amount ?: null)
			resultMap.put("DOCNO",parsedDetails.remittingBankReferenceNumber ?: null)
			resultMap.put("COMDES",'' ?: null)
			resultMap.put("COMMCDE",'0000000' ?: null)
			resultList.add(resultMap)
		}
		
		return resultList
	}
}