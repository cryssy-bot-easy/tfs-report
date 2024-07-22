package com.incuventure.utilities

class ReportDataQueryService {
	
	static transactional='true'
    def sessionFactory

    def getReportData(String reportName, String queryParamString, Boolean fieldsEmpty ) {
		println "getReportData"
		if(!fieldsEmpty){
			println "fieldsEmpty"
			String qryStatement = getQuery(reportName)
	        println 'qryStatement:'+qryStatement
	
	        List qryParams = queryParamString.tokenize('||')
	        println 'qryParams:'+qryParams
	
	
	        if(qryStatement){
				def session = sessionFactory.currentSession
						
				println qryStatement
				def qry = session.createSQLQuery(qryStatement)
	            qryParams.eachWithIndex {parm ,ctr ->
	                qry.setString(ctr,parm)
	            }
	
	            println qry.toString()
				
				return qry.list()
				//TODO:: 
	            //return []
				
	//			return getSampleData(reportName)
			} 
			
		}
		
		//return [];
		return null;
    }
	
	
	
	//temp service method to return query
	
	String getQuery(String reportName){
        println 'getQuery reportName:'+reportName
		String result = ''		
		switch(reportName){
			    case "sample-jasper-plugin":
					result = "SELECT * FROM temp where ? = ?"
					break
				case "Debit_Memo":
					result = "   SELECT tradeserviceid, serviceinstructionid, documentnumber, details FROM TradeService where serviceinstructionid = ?"
					break		
				case "Transmittal Letter (advice only; no documents list)":
					result = "SELECT * FROM angol where ? = ?"
					break
					// lets fall through			
			}
		
		
		return result
    }
	
	List getSampleData(String reportName){
		println 'getSampleData reportName:'+reportName
		List result
		switch(reportName){
				case "sample-jasper-plugin":
					result = [
								[a:'1',b:'3'],
								[a:'2',b:'4']
								]
					break
				case "angol":
					result = [
								[a:'1',b:'3'],
								[a:'2',b:'4']
								]
					break
				case "Transmittal Letter (advice only; no documents list)":
					result = []	
					
					// lets fall through
			}
		
		
		return result
	}
	
	
}

