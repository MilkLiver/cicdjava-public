package com.milkliver.deploytest;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = { "com.milkliver.deploytest", "scheduleds", "utils" })
@EnableScheduling
public class MainConfiguration {

	@Value("${server.port}")
	String serverPort;

	@Value("${server.http.port}")
	String serverHttpPort;

	// ===============================轉接HTTP至HTTPS用=====================================
	@Bean
	public Connector connectorHttp() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setScheme("http");
		connector.setPort(Integer.valueOf(serverHttpPort));
		connector.setSecure(false);
//		connector.setRedirectPort(Integer.valueOf(serverPort));

//		connector.setProperty("relaxedPathChars", "&;(),/:<=>?@[\\]{}|\"\'");
//		connector.setProperty("relaxedQueryChars", ";(),/:<=>?@[\\]{}|\"\'");
		connector.setProperty("rejectIllegalHeader", "false");
		return connector;
	}

	@Bean
	public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
			// 這段可使HTTP無法使用POST方法
			// @Override
			// protected void postProcessContext(Context context) {
			// SecurityConstraint securityConstraint = new SecurityConstraint();
			// securityConstraint.setUserConstraint("CONFIDENTIAL");
			// SecurityCollection collection = new SecurityCollection();
			// collection.addPattern("/*");
			// securityConstraint.addCollection(collection);
			// context.addConstraint(securityConstraint);
			// }
		};
		tomcat.addAdditionalTomcatConnectors(connectorHttp());
		return tomcat;
	}
	// ==================================================================================

}
