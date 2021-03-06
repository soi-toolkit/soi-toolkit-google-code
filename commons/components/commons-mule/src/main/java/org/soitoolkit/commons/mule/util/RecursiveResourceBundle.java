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
package org.soitoolkit.commons.mule.util;

import static org.soitoolkit.commons.mule.util.MiscUtil.convertResourceBundleToProperties;
import static org.soitoolkit.commons.mule.util.MiscUtil.parseStringValue;

import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.file.FileUtil;

/**
 * Adds the capability to recursively replace placeholders in a key's value.
 * 
 * @author magnuslarsson
 *
 */
public class RecursiveResourceBundle {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	public RecursiveResourceBundle() {}

	private Properties properties = new Properties();
	
	public RecursiveResourceBundle(String... baseNames) {
		for (String baseName : baseNames) {
			logger.debug("Loading properties from ResourceBundle: {}", baseName);
			ResourceBundle bundle = null;
			try {
				bundle = ResourceBundle.getBundle(baseName);
			} catch (MissingResourceException e) {
				logger.warn("Failed to laod properties from ResourceBundle: {}, continue with the next bundle", baseName);
			}
			if (bundle != null) {
				Properties p = convertResourceBundleToProperties(bundle);
				for (Entry<Object, Object> entry : p.entrySet()) {
					logger.debug("Adding property: {} = {}", entry.getKey(), entry.getValue());
					properties.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	public String getString(String key) {
		String value = properties.getProperty(key);
		String parsedValue = (value == null) ? null : parseStringValue(value, properties);
		logger.debug("{} = {}", key, parsedValue);
		return parsedValue;
	}

	public Properties getProperties() {
		return properties;
	}
}