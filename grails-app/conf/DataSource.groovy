dataSource {
    pooled = true
    //driverClassName = "org.h2.Driver"	
    //username = "sa"
    //password = ""	
	//driverClassName = "org.apache.derby.jdbc.AutoloadedDriver"
	//driverClassName = "org.apache.derby.jdbc.EmbeddedDriver";
	//dialect = org.hibernate.dialect.DerbyDialect.class
	
	//for WAR deployment
	//driverClassName = "com.ibm.db2.jcc.DB2Driver"
	
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            //dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
            //url = "jdbc:h2:mem:devDb;MVCC=TRUE"
			//dbCreate = "create" // one of 'create', 'create-drop', 'update', 'validate', ''
			//url = "jdbc:derby:tfsdb;"
			//url = "jdbc:derby://150.30.8.22:1527/tfsdb;"
			//default_schema='tfsx'
			//url = "jdbc:derby://localhost:1527/tfsdb;"//default ni Derby -- 1527 //old url in tfs.properties
			//driverClassName = "org.apache.derby.jdbc.ClientDriver"
			//url = "jdbc:derby://localhost:1527/tfsdb;"//default ni Derby -- 1527 //change url to get data from derby database
			//username='tfs'
			//password='tfs'
			dialect = "org.hibernate.dialect.DB2Dialect"
			driverClassName = "com.ibm.db2.jcc.DB2Driver"
			url = "jdbc:db2://10.80.80.165:50000/TFS2212"
			username='tfsdb2c'
			password='tfs*0*dssZvuLK'
			properties {
				validationQuery = "select chargeId from charge fetch first 1 row only"
			}
        }
        dataSource_silverlake {
            //dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
            //url = "jdbc:h2:mem:devDb;MVCC=TRUE"
            //dbCreate = "create" // one of 'create', 'create-drop', 'update', 'validate', ''
            //url = "jdbc:derby:tfsdb;"
            //url = "jdbc:derby://150.30.8.22:1527/tfsdb;"
            //default_schema='tfsx'
            //url = "jdbc:derby://localhost:1527/silverlakedb;"//default ni Derby -- 1257
            //username='tfs'
            //password='tfs'
			dialect = "org.hibernate.dialect.DB2Dialect"
			driverClassName = "com.ibm.db2.jcc.DB2Driver"
			url = "jdbc:db2://10.80.80.165:50000/TFS2212"//SIBS
			username='tfsdb2c'
			password='tfs*0*dssZvuLK'
        }

    }
    test {
        dataSource {
			dialect = "org.hibernate.dialect.DB2Dialect"
			driverClassName = "com.ibm.db2.jcc.DB2Driver"
            url = "jdbc:db2://10.80.80.165:50000/TFS2212"
			username='tfsdb2c'
			password='tfs*0*dssZvuLK'
			properties {
				validationQuery = "select chargeId from charge fetch first 1 row only"
			}
        }
        dataSource_silverlake {	
			dialect = "org.hibernate.dialect.DB2Dialect"
			driverClassName = "com.ibm.db2.jcc.DB2Driver"
			url = "jdbc:db2://10.80.80.165:50000/TFS2212"//SIBS
			username='tfsdb2c'
			password='tfs*0*dssZvuLK'
		}
		
    }
    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:h2:prodDb;MVCC=TRUE"
            pooled = true
            properties {
               maxActive = -1
               minEvictableIdleTimeMillis=1800000
               timeBetweenEvictionRunsMillis=1800000
               numTestsPerEvictionRun=3
               testOnBorrow=true
               testWhileIdle=true
               testOnReturn=true
               //validationQuery="SELECT 1"
			   validationQuery="select chargeid from charge fetch first 1 row only"
            }
			
        }
				
		dataSource_silverlake {			
			url = "jdbc:db2://130.130.2.164:50000/UCICBSD1;"//default ni Derby -- 1257
			username='tfsdb2c'
			password='abc123'
		}
    }
}
