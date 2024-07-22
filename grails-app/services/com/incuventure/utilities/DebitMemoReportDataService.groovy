package com.incuventure.utilities

class DebitMemoReportDataService {
	
	static transactional = 'true'	
	def sessionFactory
	
	String debitMemoQueryAction(String documentNumber, String tradeServiceId) {
		
		if((documentNumber == "" || documentNumber == null) || (tradeServiceId == "" || tradeServiceId == null)) {
			return ""
		}
		
		String debitMemoQuery = "select ts.approvers from tradeservice ts" +
		 " where ts.tradeproductnumber=?" +
		 " and ts.tradeserviceid=?"
		String debitMemoQuery2="select firstname,lastname from sec_employee where id=?"
		String[] approvers=[]
		String approver=" " 
		 
		 
		def result = getResultFromQuery(debitMemoQuery, documentNumber + "||" + tradeServiceId)
		println "result: " + result.size()
		if (result.size()==0) return ""
		approvers = result[0].split(",")
		println "approver: "+approvers[approvers.size()-1]
		if(approvers.size()!=0 && approvers[approvers.size()-1]!=""){
			approver=approvers[approvers.size()-1]
		}
	
		result = getResultFromQuery(debitMemoQuery2, approver)
		
		if(result.size()==0) return ""
		println "DEBIT MEMO RESULT:"+result
		
		return result[0][0] + " " + result[0][1]
	}
	
	def getResultFromQuery(String queryString,String queryParamString) {
		if(!queryString) return []
		println "QueryString: "+queryString
		
		List qryParams = queryParamString.tokenize('||')
		println 'qryParams:'+qryParams
		
		def session = sessionFactory.currentSession
		def query =  session.createSQLQuery(queryString)
		
		qryParams.eachWithIndex {parm ,ctr ->
			query.setString(ctr,parm)
		}
		
		println "Query: "+query
		
		return query.list()
	}

}