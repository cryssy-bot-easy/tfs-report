package com.incuventure.utilities

import java.util.List;
import org.hibernate.Query

class ReportDbService {
	def sessionFactory
	
	/**
	 *
	 * Perform SQL operations based on query string
	 *
	 * @param queryString
	 * @param queryParamString - multiple parameters should be concatenated with ||
	 * @return result list
	 */
	private List getResultFromQuery(String queryString,String queryParamString){
		if(!queryString) return []
		
		List qryParams = queryParamString?.tokenize('||')
		
		def session = sessionFactory.currentSession
		Query query =  session.createSQLQuery(queryString)
		
		if(qryParams != null && qryParams.size() > 0){
			qryParams.eachWithIndex {parm ,ctr ->
			query.setString(ctr,parm)
			}
		}
		
		return query.list()
	}

}
