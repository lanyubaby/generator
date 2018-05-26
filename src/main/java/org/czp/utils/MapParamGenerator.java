package org.czp.utils;

import java.util.Map;
import java.util.Map.Entry;

public class MapParamGenerator {
	
	public String mapParamGenerator(String configFileName) {
		
		Map<String, Object> map = PropertiesUtils.getProperties(configFileName);
	    
    	StringBuilder paramSb = new StringBuilder();
    	paramSb.append("Map<String,Object> params = new HashMap<String,Object>(); ");
    	
    	for(Entry<String,Object> entry : map.entrySet()) {
    		String key = entry.getKey();
    		Object value = entry.getValue();
    		paramSb.append(" \n")
    		.append("// ").append(value + "\n")
    		.append("params.put(\""+key+"\",\"\"); ");
    	}
    	   	
    	paramSb.append("\n");
    	
    	return paramSb.toString();  	
	}
	
	
	
	
}
