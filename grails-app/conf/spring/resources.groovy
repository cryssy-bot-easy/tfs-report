// Place your Spring DSL code here
import com.incuventure.utilities.ReportDirectory
beans = {
	//    dateService(org.springframework.remoting.rmi.RmiProxyFactoryBean) {
	//        serviceUrl = "rmi://localhost:1199/DateServiceRMI"
	//        serviceInterface = "com.earldouglas.springremoting.DateService"
	//    }
//		commandBusService(org.springframework.remoting.rmi.RmiProxyFactoryBean) {
//			serviceUrl = "rmi://localhost:1099/commandBusService"
//			serviceInterface = "com.incuventure.cqrs.command.CommandBus"
//		}
	
//		queryBusService(org.springframework.remoting.rmi.RmiProxyFactoryBean) {
//			serviceUrl = "rmi://localhost:1099/queryBusService"
//			serviceInterface = "com.incuventure.cqrs.query.QueryBus"
//		}
	
	reportDirectory(ReportDirectory)

    // importBeans('file:grails-app/conf/spring/integration.xml')
}
