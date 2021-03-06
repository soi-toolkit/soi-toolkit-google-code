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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;
import org.soitoolkit.tools.generator.GeneratorUtil;
import org.soitoolkit.tools.generator.model.enums.MuleVersionEnum;
import org.soitoolkit.tools.generator.model.enums.TransformerEnum;
import org.soitoolkit.tools.generator.model.enums.TransportEnum;
import org.soitoolkit.tools.generator.util.MiscUtil;
import org.soitoolkit.tools.generator.util.PreferencesUtil;

public class GroovyGeneratorUtilTest {

	private static final String TEST_OUT_FOLDER = PreferencesUtil.getDefaultRootFolder() + "/jUnitTests/GroovyGeneratorUtilTest";
	
	@Test
	public void testGroovyOneWayGenerator() throws FileNotFoundException {
		haveARunOneWay(TransportEnum.VM,   TransportEnum.VM);
		haveARunOneWay(TransportEnum.JMS,  TransportEnum.JMS);  // Expect some extra file-proeprties
		haveARunOneWay(TransportEnum.FILE, TransportEnum.FILE); // Expect outputPattern=#[header:originalFilename]
		haveARunOneWay(TransportEnum.JMS,  TransportEnum.FILE); // Expect outputPattern=${${uppercaseService}_OUTBOUND_FILE}
	}

	@Test
	public void testGroovyRequestResponseGenerator() throws FileNotFoundException {
		haveARunRequestRespons(TransportEnum.RESTHTTP,   TransportEnum.RESTHTTP);
	}

	@Test
	public void testGroovyMinimalGenerator() throws FileNotFoundException {
		haveARunMinimal(TransportEnum.VM, TransportEnum.VM);
	}

	private void haveARunOneWay(TransportEnum in, TransportEnum out) throws FileNotFoundException {
		haveARun(in, out, "GenerateOneWayMflow.groovy");
	}

	private void haveARunRequestRespons(TransportEnum in, TransportEnum out) throws FileNotFoundException {
		haveARun(in, out, "GenerateRequestResponseMflow.groovy");
	}

	private void haveARunMinimal(TransportEnum in, TransportEnum out) throws FileNotFoundException {
		haveARun(in, out, "GenerateMinimalMflow.groovy");
	}
	
	private void haveARun(TransportEnum in, TransportEnum out, String groovyScript) throws FileNotFoundException {
		GeneratorUtil gu = new GeneratorUtil(System.out, "myGroup", "myArtifact", null, "myService", MuleVersionEnum.MAIN_MULE_VERSION, in, out, TransformerEnum.JAVA, "template-folder", TEST_OUT_FOLDER);
		gu.generateContentAndCreateFileUsingGroovyGenerator(getClass().getResource(groovyScript), "myGroovyoutput.txt");
		
		InputStream is = new FileInputStream(TEST_OUT_FOLDER + "/" + "myGroovyoutput.txt");
		String content = MiscUtil.convertStreamToString(is);
		System.err.println(content);
	}
}
