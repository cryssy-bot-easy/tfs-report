// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }



grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
    xml: ['text/xml', 'application/xml'],
    text: 'text/plain',
    js: 'text/javascript',
    rss: 'application/rss+xml',
    atom: 'application/atom+xml',
    css: 'text/css',
    csv: 'text/csv',
    all: '*/*',
    json: ['application/json','text/json'],
    form: 'application/x-www-form-urlencoded',
    multipartForm: 'multipart/form-data'
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']


// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// enable query caching by default
grails.hibernate.cache.queries = true

// set per-environment serverURL stem for creating absolute links
environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.changeme.com"
    }
}

// log4j configuration
log4j = {
    String logDir
    
    environments {
        development {
            logDir = 'target'
        }
        
        production {
            logDir = '/opt/tfs/logs'
        }
    }
    
    // Handle the stacktrace log correctly
    rollingFile name:'stacktrace',
    file: "${logDir}/tfs-report_stacktrace.log",
    layout: pattern(conversionPattern: "'%d [%t] %-5p %c{2} %x - %m%n'"),
                'append': true,
    threshold: org.apache.log4j.Level.ALL
    
    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
}

tfs.report.location.base='http://' + InetAddress.getLocalHost().getHostAddress() + ':8081/tfs-report'
tfs.batch.output.directory='batchFiles'
tfs.interface.output.directory='interfaceFiles'
tfs.batchdebitmemo.directory='batchDebitMemoFiles'
tfs.glfxform1.directory='interfaceFiles/GLFXFORM1'
tfs.glfxform1.backup='interfaceFiles/GLFXFORM1_BACKUP'
tfs.credexform4.directory='interfaceFiles/CREDEXFORM4'
tfs.credexform4.backup='interfaceFiles/CREDEXFORM4_BACKUP'
tfs.batch.url.main='http://'+ InetAddress.getLocalHost().getHostAddress() +':8080/tfs-web/batch/index'
grails.config.locations = ["classpath:tfs-report.properties"]
tfs.core.api.webapi.url = "http://"+ InetAddress.getLocalHost().getHostAddress() +":9090/tfs-core/api/"
sibs.cif.schema='UCDATUBWC1'
sibs.loans.schema='UCDATULNS1'
sibs.rates.schema='UCPARUCMN1'