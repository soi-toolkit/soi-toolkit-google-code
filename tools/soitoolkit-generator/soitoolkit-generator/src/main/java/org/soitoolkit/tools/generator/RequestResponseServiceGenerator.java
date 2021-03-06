/* 
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soitoolkit.tools.generator;

import static org.soitoolkit.commons.xml.XPathUtil.appendXmlFragment;
import static org.soitoolkit.commons.xml.XPathUtil.createDocument;
import static org.soitoolkit.commons.xml.XPathUtil.getXPathResult;
import static org.soitoolkit.commons.xml.XPathUtil.getXml;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.JMS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.RESTHTTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.RESTHTTPS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SOAPHTTP;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SOAPHTTPS;
import static org.soitoolkit.tools.generator.model.enums.TransportEnum.SOAPSERVLET;
import static org.soitoolkit.tools.generator.util.FileUtil.openFileForOverwrite;
import static org.soitoolkit.tools.generator.util.PropertyFileUtil.openPropertyFileForAppend;
import static org.soitoolkit.tools.generator.util.PropertyFileUtil.updateMuleDeployPropertyFileWithNewService;
import static org.soitoolkit.tools.generator.util.XmlFileUtil.updateCommonFileWithSpringImport;
import static org.soitoolkit.tools.generator.util.XmlFileUtil.updateJaxbContextInConfigFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.soitoolkit.tools.generator.model.IModel;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.util.SourceFormatterUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RequestResponseServiceGenerator implements Generator {

	GeneratorUtil gu;
	IModel m;
	
	public RequestResponseServiceGenerator(PrintStream ps, String groupId, String artifactId, String serviceName, MuleVersionEnum muleVersion, TransportEnum inboundTransport, TransportEnum outboundTransport, TransformerEnum transformerType, String outputFolder) {

		String templateFolder = "/requestResponseService";
		if ((inboundTransport == TransportEnum.RESTHTTP || inboundTransport == RESTHTTPS) && (outboundTransport == TransportEnum.SOAPHTTP || outboundTransport == SOAPHTTPS)) {
			templateFolder = "/requestResponseService-NEW";
		}

		gu = new GeneratorUtil(ps, groupId, artifactId, null, serviceName, muleVersion, inboundTransport, outboundTransport, transformerType, templateFolder, outputFolder);
		m = gu.getModel();
	}
	
    public void startGenerator() {
		TransportEnum inboundTransport  = TransportEnum.valueOf(m.getInboundTransport());
		TransportEnum outboundTransport = TransportEnum.valueOf(m.getOutboundTransport());
		
		if ((inboundTransport == TransportEnum.RESTHTTP || inboundTransport == RESTHTTPS) && (outboundTransport == TransportEnum.SOAPHTTP || outboundTransport == SOAPHTTPS)) {
			startNewGenerator();
		} else {
			startOldGenerator();
		}
	}
	
    protected void startOldGenerator() {
    	gu.logInfo("Creates a OLD Request/Response-service, inbound transport: " + m.getInboundTransport() + ", outbound transport: " + m.getOutboundTransport() + ", type of transformer: " + m.getTransformerType());
		TransportEnum inboundTransport  = TransportEnum.valueOf(m.getInboundTransport());
		TransportEnum outboundTransport = TransportEnum.valueOf(m.getOutboundTransport());
		TransformerEnum transformerType = TransformerEnum.valueOf(m.getTransformerType());

		// FIXME. MULE STUDIO.
//    	gu.generateContentAndCreateFile("src/main/resources/services/__service__-service.xml.gt");
    	gu.generateContentAndCreateFile("src/main/app/__service__-service.xml.gt");
    	gu.generateContentAndCreateFileUsingGroovyGenerator(getClass().getResource("GenerateMinimalMflow.groovy"), "flows/__service__-service.mflow");
    	if (transformerType == TransformerEnum.SMOOKS) {
	    	gu.generateContentAndCreateFile("src/main/resources/transformers/__service__-request-transformer.xml.gt");
	    	gu.generateContentAndCreateFile("src/main/resources/transformers/__service__-response-transformer.xml.gt");
    	} else {
	    	gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__RequestTransformer.java.gt");
			gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__ResponseTransformer.java.gt");
    	}

		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/request-input.xml.gt");
	    if (inboundTransport == RESTHTTP || inboundTransport == RESTHTTPS) {
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/request-fault-invalid-input.xml.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/request-fault-timeout-input.xml.gt");
	    }
	    gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/request-expected-result.csv.gt");
	    if (outboundTransport == RESTHTTP || outboundTransport == RESTHTTPS) {
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/response-input.rest.gt");
	    } else {
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/response-input.csv.gt");
	    }
		gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/response-expected-result.xml.gt");
	    if (outboundTransport == JMS) {
	    	gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/fault-response-input.csv.gt");
			gu.generateContentAndCreateFile("src/test/resources/testfiles/__service__/fault-response-expected-result.xml.gt");
	    }
		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__IntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__RequestTransformerTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__ResponseTransformerTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestConsumer.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestProducer.java.gt");
		
		updateOldPropertyFiles(inboundTransport, outboundTransport);
		
		// Update mule-deploy.properties files with the new service
		// (Everything in the folder src/main/app is loaded by mule-deploy.properties) so skip updating *ConfigXmlFile.
		// updateConfigXmlFileWithNewService(gu.getOutputFolder(), m.getArtifactId(), m.getService());
		// updateTeststubsAndServicesConfigXmlFileWithNewService(gu.getOutputFolder(), m.getArtifactId(), m.getService());
		updateMuleDeployPropertyFileWithNewService(gu.getOutputFolder(), m.getService());

		// Add http-connector to common file (one and the same for junit-tests and running mule server) if http-transport is used for the first time
		if (inboundTransport == TransportEnum.RESTHTTP || inboundTransport == TransportEnum.SOAPHTTP || outboundTransport == RESTHTTP || outboundTransport == SOAPHTTP) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the HTTP-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-http-connector.xml");
		}
		
		if (inboundTransport == TransportEnum.RESTHTTPS || inboundTransport == TransportEnum.SOAPHTTPS || outboundTransport == RESTHTTPS || outboundTransport == SOAPHTTPS) {
			
			// Add a https-connector if not already existing + properties for it. 
			// Also add a http connector for the case where a developer wants to temporary degrade https to plain http, e.g. for troubleshooting
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the HTTPS-transport";
    		boolean isUpdated = updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-https-connector.xml");
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-http-connector.xml");
    		
    		if (isUpdated) {
    			gu.copyContentAndCreateFile("src/test/certs/client.jks.gt");
    	    	gu.copyContentAndCreateFile("src/test/certs/truststore.jks.gt");
    	    	gu.copyContentAndCreateFile("src/test/certs/server.jks.gt");
    	    	gu.copyContentAndCreateFile("src/test/certs/readme.txt.gt");
    		
    	    	String httpsProperties =
    	    		"# Properties for the default soitoolkit-https-connector's\n" +
    	    		"# TODO: Update to reflect your settings\n" +
    	    		"SOITOOLKIT_HTTPS_CLIENT_SO_TIMEOUT=${SERVICE_TIMEOUT_MS}\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_KEYSTORE=src/test/certs/server.jks\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_KEY_TYPE=jks\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_TRUSTSTORE=src/test/certs/truststore.jks\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_TRUSTSTORE_REQUIRE_CLIENT_AUTH=true\n" +

    	    		"SOITOOLKIT_HTTPS_TLS_KEYSTORE_PASSWORD=password\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_KEY_PASSWORD=password\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_TRUSTSTORE_PASSWORD=password\n";
    	    	
    	    	addProperties(httpsProperties);
    		}
    		
	    	if (inboundTransport == TransportEnum.SOAPHTTPS) {
	    		File cxfFile = new File("src/test/resources/cxf-test-consumer-config.xml");
	    		
	    		if (!cxfFile.exists())    		
	    			gu.generateContentAndCreateFile("src/test/resources/cxf-test-consumer-config.xml.gt");
	    	}
    		
		}

		String file = gu.getOutputFolder() + "/pom.xml";
		String xmlFragment = 
			"\n" +
			"\n" +
			"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
			"			<groupId>org.soitoolkit.refapps.sd</groupId>\n" +
			"			<artifactId>soitoolkit-refapps-sample-schemas</artifactId>\n" +
			"			<version>${soitoolkit.version}</version>\n" +
			"		</dependency>\n";
		addDependency(file, xmlFragment, "soitoolkit-refapps-sample-schemas");

    	if (transformerType == TransformerEnum.SMOOKS) {
			xmlFragment =
				"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
				"			<groupId>org.mule.modules.mule-module-smooks</groupId>\n" +
				"			<artifactId>smooks-4-mule-3</artifactId>\n" +
				"			<version>1.3-RC1</version>\n" +
				"		</dependency>\n";
			addDependency(file, xmlFragment, "smooks-4-mule-3");
	
			xmlFragment =
				"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
				"			<groupId>org.milyn</groupId>\n" +
				"			<artifactId>milyn-smooks-templating</artifactId>\n" +
				"			<version>1.4</version>\n" +
				"		</dependency>\n";
			addDependency(file, xmlFragment, "milyn-smooks-templating");
	
			xmlFragment =
				"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
				"			<groupId>org.milyn</groupId>\n" +
				"			<artifactId>milyn-smooks-csv</artifactId>\n" +
				"			<version>1.4</version>\n" +
				"		</dependency>\n";
			addDependency(file, xmlFragment, "milyn-smooks-csv");
    	}
	}
	
    protected void startNewGenerator() {

    	gu.logInfo("Creates a NEW Request/Response-service, inbound transport: " + m.getInboundTransport() + ", outbound transport: " + m.getOutboundTransport() + ", type of transformer: " + m.getTransformerType());
		TransportEnum inboundTransport  = TransportEnum.valueOf(m.getInboundTransport());
		TransportEnum outboundTransport = TransportEnum.valueOf(m.getOutboundTransport());
		TransformerEnum transformerType = TransformerEnum.valueOf(m.getTransformerType());

    	gu.generateContentAndCreateFile("__artifactId__-__service__-soapui-project.xml.gt");
    	gu.generateContentAndCreateFile("src/main/app/__service__-service.xml.gt");
    	gu.generateContentAndCreateFileUsingGroovyGenerator(getClass().getResource("GenerateMinimalMflow.groovy"), "flows/__service__-service.mflow");
    	if (transformerType == TransformerEnum.SMOOKS) {
	    	gu.generateContentAndCreateFile("src/main/resources/transformers/__service__-request-transformer.xml.gt");
	    	gu.generateContentAndCreateFile("src/main/resources/transformers/__service__-response-transformer.xml.gt");
    	} else {
	    	gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__RequestTransformer.java.gt");
			gu.generateContentAndCreateFile("src/main/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__ResponseTransformer.java.gt");
    	}

		gu.generateContentAndCreateFile("src/test/resources/teststub-services/__service__-teststub-service.xml.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__IntegrationTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__RequestTransformerTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__ResponseTransformerTest.java.gt");
		gu.generateContentAndCreateFile("src/test/java/__javaPackageFilepath__/__lowercaseJavaService__/__capitalizedJavaService__TestProducer.java.gt");
		
		updateNewPropertyFiles(inboundTransport, outboundTransport);
		
		// Update mule-deploy.properties files with the new service
		// (Everything in the folder src/main/app is loaded by mule-deploy.properties) so skip updating *ConfigXmlFile.
		// updateConfigXmlFileWithNewService(gu.getOutputFolder(), m.getArtifactId(), m.getService());
		// updateTeststubsAndServicesConfigXmlFileWithNewService(gu.getOutputFolder(), m.getArtifactId(), m.getService());
		updateMuleDeployPropertyFileWithNewService(gu.getOutputFolder(), m.getService());

		// Add http-connector to common file (one and the same for junit-tests and running mule server) if http-transport is used for the first time
		if (inboundTransport == TransportEnum.RESTHTTP || inboundTransport == TransportEnum.SOAPHTTP || outboundTransport == RESTHTTP || outboundTransport == SOAPHTTP) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the HTTP-transport";
    		updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-http-connector.xml");
		}
		
		if (inboundTransport == TransportEnum.RESTHTTPS || inboundTransport == TransportEnum.SOAPHTTPS || outboundTransport == RESTHTTPS || outboundTransport == SOAPHTTPS) {
			String comment = "Added " + new Date() + " since flow " + m.getService() + " uses the HTTPS-transport";
    		boolean isUpdated = updateCommonFileWithSpringImport(gu, comment, "soitoolkit-mule-https-connector.xml");
    		
    		if (isUpdated) {
    			gu.copyContentAndCreateFile("src/test/certs/client.jks.gt");
    	    	gu.copyContentAndCreateFile("src/test/certs/truststore.jks.gt");
    	    	gu.copyContentAndCreateFile("src/test/certs/server.jks.gt");
    	    	gu.copyContentAndCreateFile("src/test/certs/readme.txt.gt");
    		
    	    	String httpsProperties =
    	    		"# Properties for the default soitoolkit-https-connector's\n" +
    	    		"# TODO: Update to reflect your settings\n" +
    	    		"SOITOOLKIT_HTTPS_CLIENT_SO_TIMEOUT=${SERVICE_TIMEOUT_MS}\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_KEYSTORE=src/test/certs/server.jks\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_KEY_TYPE=jks\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_TRUSTSTORE=src/test/certs/truststore.jks\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_TRUSTSTORE_REQUIRE_CLIENT_AUTH=true\n" +

    	    		"SOITOOLKIT_HTTPS_TLS_KEYSTORE_PASSWORD=password\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_KEY_PASSWORD=password\n" +
    	    		"SOITOOLKIT_HTTPS_TLS_TRUSTSTORE_PASSWORD=password\n";
    	    	
    	    	addProperties(httpsProperties);
    		}
    		
    		if (inboundTransport == TransportEnum.SOAPHTTPS) {
	    		File cxfFile = new File("src/test/resources/cxf-test-consumer-config.xml");
	    		
	    		if (!cxfFile.exists())    		
	    			gu.generateContentAndCreateFile("src/test/resources/cxf-test-consumer-config.xml.gt");
	    	}
		}

		String xmlFile     = gu.getOutputFolder() + "/src/main/app/" + gu.getModel().getArtifactId() + "-common.xml";
		String javaPackage = "org.soitoolkit.refapps.sd.crudsample.schema.v1";
		updateJaxbContextInConfigFile(gu, xmlFile, javaPackage);
		
		String file = gu.getOutputFolder() + "/pom.xml";
		String xmlFragment = 
			"\n" +
			"\n" +
			"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
			"			<groupId>org.soitoolkit.refapps.sd</groupId>\n" +
			"			<artifactId>soitoolkit-refapps-sample-schemas</artifactId>\n" +
			"			<version>${soitoolkit.version}</version>\n" +
			"		</dependency>\n";
		addDependency(file, xmlFragment, "soitoolkit-refapps-sample-schemas");

    	if (transformerType == TransformerEnum.SMOOKS) {
			xmlFragment =
				"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
				"			<groupId>org.mule.modules.mule-module-smooks</groupId>\n" +
				"			<artifactId>smooks-4-mule-3</artifactId>\n" +
				"			<version>1.3-RC1</version>\n" +
				"		</dependency>\n";
			addDependency(file, xmlFragment, "smooks-4-mule-3");
	
			xmlFragment =
				"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
				"			<groupId>org.milyn</groupId>\n" +
				"			<artifactId>milyn-smooks-templating</artifactId>\n" +
				"			<version>1.4</version>\n" +
				"		</dependency>\n";
			addDependency(file, xmlFragment, "milyn-smooks-templating");
	
			xmlFragment =
				"		<dependency xmlns=\"http://maven.apache.org/POM/4.0.0\">\n" +
				"			<groupId>org.milyn</groupId>\n" +
				"			<artifactId>milyn-smooks-csv</artifactId>\n" +
				"			<version>1.4</version>\n" +
				"		</dependency>\n";
			addDependency(file, xmlFragment, "milyn-smooks-csv");
    	}
    }
    
    private void addProperties(String properties) {
    	properties = SourceFormatterUtil.formatSource(properties);
    	PrintWriter cfg = null;
		try {
			cfg = openPropertyFileForAppend(gu.getOutputFolder(), m.getConfigPropertyFile());
			cfg.println("");
			cfg.println(properties);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (cfg != null) {cfg.close();}
		}
    }

	private void updateOldPropertyFiles(TransportEnum inboundTransport, TransportEnum outboundTransport) {
		
		PrintWriter cfg = null;
		try {
			cfg = openPropertyFileForAppend(gu.getOutputFolder(), m.getConfigPropertyFile());

			String artifactId     = m.getArtifactId();
			String service        = m.getUppercaseService();
		    String serviceName    = m.getInitialLowercaseService();

			// Print header for this service's properties
		    cfg.println("");
		    cfg.println("# Properties for service \"" + m.getService() + "\"");
		    cfg.println("# TODO: Update to reflect your settings");

		    if (inboundTransport == SOAPHTTP || inboundTransport == RESTHTTP) {
			    cfg.println(service + "_INBOUND_URL=http://localhost:" + m.getHttpPort() + "/" + artifactId + "/services/" + serviceName + "/v1");
		    } else if (inboundTransport == SOAPHTTPS || inboundTransport == RESTHTTPS) {
		    	cfg.println(service + "_INBOUND_URL=https://localhost:" + m.getHttpPort() + "/" + artifactId + "/services/" + serviceName + "/v1");
		    } else if (inboundTransport == SOAPSERVLET) {
			    cfg.println(service + "_INBOUND_URL=servlet://" + serviceName + "/v1");
		    }
		    
		    if (outboundTransport == SOAPHTTP) {
			    cfg.println(service + "_OUTBOUND_URL=${" + service + "_TESTSTUB_INBOUND_URL}");
			    cfg.println(service + "_TESTSTUB_INBOUND_URL=http://localhost:" + m.getHttpTeststubPort() + "/" + artifactId + "/services/" + serviceName + "-soap-teststub/v1");
		    } else if (outboundTransport == SOAPHTTPS) {
		    	cfg.println(service + "_OUTBOUND_URL=${" + service + "_TESTSTUB_INBOUND_URL}");
		    	cfg.println(service + "_TESTSTUB_INBOUND_URL=https://localhost:" + m.getHttpTeststubPort() + "/" + artifactId + "/services/" + serviceName + "-soap-teststub/v1");
		    } else if (outboundTransport == RESTHTTP) {
			    cfg.println(service + "_OUTBOUND_URL=${" + service + "_TESTSTUB_INBOUND_URL}");
			    cfg.println(service + "_TESTSTUB_INBOUND_URL=http://localhost:" + m.getHttpTeststubPort() + "/" + artifactId + "/services/" + serviceName + "-rest-teststub/v1");

		    } else if (outboundTransport == RESTHTTPS) { 
		    	cfg.println(service + "_OUTBOUND_URL=${" + service + "_TESTSTUB_INBOUND_URL}");
		    	cfg.println(service + "_TESTSTUB_INBOUND_URL=https://localhost:" + m.getHttpTeststubPort() + "/" + artifactId + "/services/" + serviceName + "-rest-teststub/v1");
		    } else if (outboundTransport == JMS) {
			    cfg.println(service + "_REQUEST_QUEUE="  + m.getJmsRequestQueue());
			    cfg.println(service + "_RESPONSE_QUEUE=" + m.getJmsResponseQueue());
		    }
		    
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (cfg != null) {cfg.close();}
		}
	}
	
	private void updateNewPropertyFiles(TransportEnum inboundTransport, TransportEnum outboundTransport) {
		
		PrintWriter cfg = null;
		try {
			cfg = openPropertyFileForAppend(gu.getOutputFolder(), m.getConfigPropertyFile());

			String artifactId     = m.getArtifactId();
			String service        = m.getUppercaseService();
		    String serviceName    = m.getInitialLowercaseService();

			// Print header for this service's properties
		    cfg.println("");
		    cfg.println("# Properties for service \"" + m.getService() + "\"");
		    cfg.println("# TODO: Update to reflect your settings");

		    if (inboundTransport == SOAPHTTP || inboundTransport == RESTHTTP) {
/*
		    	ML2_INBOUND_BASE_URL=http://localhost:8081
		    	ML2_BASE_PATH=/mobile/rest
		    	ML2_SAMPLE_PATH=${ML2_BASE_PATH}/sample
		    	ML2_INBOUND_URL=${ML2_INBOUND_BASE_URL}${ML2_BASE_PATH}
*/
			    cfg.println(service + "_BASE_URL=http://localhost:" + m.getHttpPort());
			    cfg.println(service + "_BASE_PATH=/" + artifactId + "/" + serviceName + "/rest");
			    cfg.println(service + "_SAMPLE_PATH=${" + service + "_BASE_PATH}/sample");
			    cfg.println(service + "_INBOUND_URL=${" + service + "_BASE_URL}${" + service + "_BASE_PATH}");

		    } else if (inboundTransport == SOAPHTTPS || inboundTransport == RESTHTTPS) { 
		    	cfg.println(service + "_BASE_URL=https://localhost:" + m.getHttpPort());
		    	cfg.println(service + "_BASE_PATH=/" + artifactId + "/" + serviceName + "/rest");
		    	cfg.println(service + "_SAMPLE_PATH=${" + service + "_BASE_PATH}/sample");
		    	cfg.println(service + "_INBOUND_URL=${" + service + "_BASE_URL}${" + service + "_BASE_PATH}");
		    } else if (inboundTransport == SOAPSERVLET) {
			    cfg.println(service + "_INBOUND_URL=servlet://" + serviceName + "/v1");
		    }
		    
		    if (outboundTransport == SOAPHTTP) {
			    cfg.println(service + "_OUTBOUND_URL=${" + service + "_TESTSTUB_INBOUND_URL}");
			    cfg.println(service + "_TESTSTUB_INBOUND_URL=http://localhost:" + m.getHttpTeststubPort() + "/" + artifactId + "/services/" + serviceName + "-soap-teststub/v1");
		    } else if (outboundTransport == SOAPHTTPS) {
		    	cfg.println(service + "_OUTBOUND_URL=${" + service + "_TESTSTUB_INBOUND_URL}");
		    	cfg.println(service + "_TESTSTUB_INBOUND_URL=https://localhost:" + m.getHttpTeststubPort() + "/" + artifactId + "/services/" + serviceName + "-soap-teststub/v1");
		    } else if (outboundTransport == RESTHTTP) {
			    cfg.println(service + "_OUTBOUND_URL=${" + service + "_TESTSTUB_INBOUND_URL}");
			    cfg.println(service + "_TESTSTUB_INBOUND_URL=http://localhost:" + m.getHttpTeststubPort() + "/" + artifactId + "/services/" + serviceName + "-rest-teststub/v1");
		    } else if (outboundTransport == RESTHTTPS) {
		    	cfg.println(service + "_OUTBOUND_URL=${" + service + "_TESTSTUB_INBOUND_URL}");
		    	cfg.println(service + "_TESTSTUB_INBOUND_URL=https://localhost:" + m.getHttpTeststubPort() + "/" + artifactId + "/services/" + serviceName + "-rest-teststub/v1");
		    } else if (outboundTransport == JMS) {
			    cfg.println(service + "_REQUEST_QUEUE="  + m.getJmsRequestQueue());
			    cfg.println(service + "_RESPONSE_QUEUE=" + m.getJmsResponseQueue());
		    }
		    
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (cfg != null) {cfg.close();}
		}
	}
	
	private void addDependency(String file, String xmlFragment, String artifactId) {
		InputStream content = null;
		String xml = null;
		try {
			
			gu.logDebug("Add: " + xmlFragment + " to " + file);
			content = new FileInputStream(file);
			
			Document doc = createDocument(content);

			Map<String, String> namespaceMap = new HashMap<String, String>();
			namespaceMap.put("ns", "http://maven.apache.org/POM/4.0.0");

			// First verify that the dependency does not exist already
			NodeList testList = getXPathResult(doc, namespaceMap, "/ns:project/ns:dependencies/ns:dependency/ns:artifactId[.='" + artifactId + "']");
			if (testList.getLength() > 0) {
				gu.logDebug("Fragment already exists, bail out!!!");
				return;
			}
			
			NodeList rootList = getXPathResult(doc, namespaceMap, "/ns:project/ns:dependencies");
			Node root = rootList.item(0);
		    
		    appendXmlFragment(root, xmlFragment);
			
		    xml = getXml(doc);
		    xml = SourceFormatterUtil.formatSource(xml);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (content != null) {try {content.close();} catch (IOException e) {}}
		}

		PrintWriter pw = null;
		try {
			gu.logDebug("Overwrite file: " + file);
			pw = openFileForOverwrite(file);
			pw.print(xml);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (pw != null) {pw.close();}
		}
	}
}