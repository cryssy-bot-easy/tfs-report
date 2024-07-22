package com.incuventure.utilities

//import java.beans.java_awt_BorderLayout_PersistenceDelegate;

import grails.converters.JSON

class GenericReportsQueryService{
	
	static transactional='true'
	def sessionFactory
	
	def getDetailsFromTradeService(String susi,String tradeServiceId){
		if((susi == null || "" == susi) || (tradeServiceId == null || "" == tradeServiceId)){
			return "";
		}
		String query = "SELECT CAST(DETAILS AS VARCHAR(30000)) AS DETAILS "+
			"FROM TRADESERVICE WHERE TRADESERVICEID=?"
		List<Map<String,Object>> lst= getResultFromQuery(query,tradeServiceId)
		if(lst != null && !lst.isEmpty()){
			Map<String, Object> details = JSON.parse(lst[0])
					for(Map.Entry m : details){
						if(susi == m.key){
							return m.value
									break
						}
					}
		}
		return "";
	}
	
	def getDetailsFromServiceInstruction(String susi,String serviceInstructionId){
		if((susi == null || "" == susi) || (serviceInstructionId == null || "" == serviceInstructionId)){
			return "";
		}
		String query = "SELECT CAST(DETAILS AS VARCHAR(30000)) AS DETAILS "+
			"FROM SERVICEINSTRUCTION WHERE SERVICEINSTRUCTIONID=?"
		List<Map<String,Object>> lst= getResultFromQuery(query,serviceInstructionId)
		if(lst != null && !lst.isEmpty()){
			Map<String, Object> details = JSON.parse(lst[0])
					for(Map.Entry m : details){
						if(susi == m.key){
							return m.value
									break
						}
					}
		}
		return "";
	}
	
	def getResultFromQuery(String queryString,String queryParamString) {
		if(!queryString) return []
		
		List qryParams = queryParamString.tokenize('||')
		
		def session = sessionFactory.currentSession
		def query =  session.createSQLQuery(queryString)
		
		qryParams.eachWithIndex {parm ,ctr ->
			query.setString(ctr,parm)
		}
		
		return query.list()
	}
}
