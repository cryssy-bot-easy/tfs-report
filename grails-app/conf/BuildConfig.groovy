grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins
		
		mavenLocal()
		
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()

        // uncomment these to enable remote dependency resolution from public Maven repositories
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
		mavenRepo "https://repository.jboss.org/nexus/content/repositories/thirdparty-releases"
		mavenRepo "http://archiva.fdvs.com.ar/repository/public1/" // packaged 7 well known fonts -- Bug #3213
		
		
		mavenRepo "https://repo.grails.org/grails/core"
		mavenRepo "https://repo.grails.org/grails/plugins"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.16'\
		//compile ('com.incuventure:dddcqrs:1.0-SNAPSHOT') { transitive=false }
		//compile ('com.ucpb:tfs-app:1.0-SNAPSHOT') { transitive=false }
		//compile ('org.springframework:spring-remoting:2.0.8')
//        compile("com.ibm.icu:icu4j:4.6")
//
//		compile('net.sf.jasperreports:jasperreports:4.5.1'){
//			excludes 'antlr', 'commons-beanutils', 'commons-collections', 'commons-logging',
//					'ant', 'mondrian', 'commons-javaflow','barbecue', 'xml-apis-ext','xml-apis', 'xalan', 'groovy-all', 'hibernate', 'saaj-api', 'servlet-api',
//					'xercesImpl','xmlParserAPIs','spring-core','bsh', 'spring-beans', 'jaxen', 'barcode4j','batik-svg-dom','batik-xml','batik-awt-util','batik-dom',
//					'batik-css','batik-gvt','batik-script', 'batik-svggen','batik-util','batik-bridge','persistence-api','jdtcore','bcmail-jdk16','bcprov-jdk16','bctsp-jdk16',
//					'bcmail-jdk14','bcprov-jdk14','bctsp-jdk14','xmlbeans'
//		  }
//
//		  compile('org.apache.poi:poi:3.7')
//		  //compile('org.apache.derby:derby:10.9.1.0')
//		  compile('com.google.code.gson:gson:2.2.2')
		compile('org.codehaus.groovy.modules.http-builder:http-builder:0.5.2') {
			excludes "commons-logging", "xml-apis", "groovy"
//            transitive=false
		}
		compile ('com.google.code.gson:gson:2.2.2')
        compile('net.sf.jasperreports:jasperreports:4.7.0'){
            excludes 'antlr', 'commons-beanutils', 'commons-collections', 'commons-logging',
                    'ant', 'mondrian', 'commons-javaflow','barbecue', 'xml-apis-ext','xml-apis', 'xalan', 'groovy-all', 'hibernate', 'saaj-api', 'servlet-api',
                    'xercesImpl','xmlParserAPIs','spring-core','bsh', 'spring-beans', 'jaxen', 'barcode4j','batik-svg-dom','batik-xml','batik-awt-util','batik-dom',
                    'batik-css','batik-gvt','batik-script', 'batik-svggen','batik-util','batik-bridge','persistence-api','jdtcore','bcmail-jdk16','bcprov-jdk16','bctsp-jdk16',
                    'bcmail-jdk14','bcprov-jdk14','bctsp-jdk14','xmlbeans'
        }

        compile('org.apache.poi:poi:3.8')
		compile("com.ibm.icu:icu4j:4.6") 
		compile("com.ibm.db2.jcc:jcc4:4.25.13")
//		compile("com.incuventure:report-utility:1.0")
		//compile("ar.com.fdvs:DynamicJasper-core-fonts:1.0") // packaged 7 well known fonts -- Bug #3213

/* Please do not delete below */
/*
        compile('org.springframework.integration:spring-integration-core:2.0.1.RELEASE')  {
            excludes 'commons-logging', 'spring-aop', 'spring-beans', 'spring-context', 'spring-context-support', 'spring-core', 'spring-tx'
        }
        compile('org.springframework.integration:spring-integration-file:2.0.1.RELEASE')  {
            excludes 'commons-logging', 'spring-aop', 'spring-beans', 'spring-context', 'spring-context-support', 'spring-core', 'spring-tx'
        }
        compile('org.springframework.integration:spring-integration-stream:2.0.1.RELEASE')  {
            excludes 'commons-logging', 'spring-aop', 'spring-beans', 'spring-context', 'spring-context-support', 'spring-core', 'spring-tx'
        }
*/
    }

    plugins {
        runtime ":hibernate:$grailsVersion"
        runtime ":jquery:1.7.1"
        // runtime ":resources:1.1.6"
		runtime ":resources:1.2.8"
		compile ":jasper:1.6.1"
		compile ":console:1.2"

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0"
        //runtime ":cached-resources:1.0"
        //runtime ":yui-minify-resources:0.1.4"

        build ":tomcat:$grailsVersion"
    }
}
