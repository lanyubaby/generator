package org.czp.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertiesUtils {
	
	private static Log log = LogFactory.getLog(PropertiesUtils.class);
	
	private static Map<String, PropertiesConfiguration> configMap = new HashMap<String, PropertiesConfiguration>();
	
	private static PropertiesConfiguration initConfigFile(String configFileName) throws ConfigurationException {
		PropertiesConfiguration propConfig = new PropertiesConfiguration();
		propConfig.setDelimiterParsingDisabled(true);		
		propConfig.setFileName(configFileName);
		propConfig.load();
		configMap.put(configFileName, propConfig);
		return propConfig;
	}
	
	public static void setProperty(String configFileName, String key, String value) {
		PropertiesConfiguration propConfig = configMap.get(configFileName);		
		try {
			if(propConfig == null) {
				propConfig = initConfigFile(configFileName);
			}
			
			propConfig.setProperty(key, value);
			propConfig.save();
		} catch(ConfigurationException ex) {
			log.error("save property value error,file name:"+configFileName+" key:"+key+" value:"+value, ex);
		}
	}
	
	public static String getProperty(String configFileName, String key) {
		PropertiesConfiguration propConfig = configMap.get(configFileName);
		String value = null;
		try {
			if(propConfig == null) {
				propConfig = initConfigFile(configFileName);
			}
			if(propConfig.containsKey(key)) {
				Object propValue = propConfig.getProperty(key);
				value = (String)propValue;
			}			
		} catch(ConfigurationException ex) {
			log.error("get property value error,file name:"+configFileName+" key:"+key, ex);
		}
		
		return value;		
	}
	
	public static Map<String, Object> getProperties(String configFileName) {
		PropertiesConfiguration propConfig = configMap.get(configFileName);
		try {
			if(propConfig == null) {
				propConfig = initConfigFile(configFileName);
			}
		} catch(ConfigurationException ex) {
			log.error("init config file error,file name:"+configFileName, ex);
			return null;
		}
		Map<String, Object> propMap = new HashMap<String, Object>();
		Iterator iter = propConfig.getKeys();
		while(iter.hasNext()) {
			String key = (String)iter.next();
			propMap.put(key, propConfig.getProperty(key));
		}
		return propMap;
	}
	
	
	public static void main(String[] args) {
		Map props = PropertiesUtils.getProperties("F:\\workspace\\HDFSClient\\src\\hdfs.properties");
		System.out.println(props.size());
		String value = PropertiesUtils.getProperty("F:\\workspace\\HDFSClient\\src\\hdfs.properties","nameNodeURL");
		System.out.println(value);
	}
	

}
