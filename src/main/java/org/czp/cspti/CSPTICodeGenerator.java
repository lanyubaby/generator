package org.czp.cspti;

import org.czp.utils.PropertiesUtils;

public class CSPTICodeGenerator 
{
    public static void main( String[] args )
    {
    	String configFileName = "properties/1.properties";
    	String a = PropertiesUtils.getProperty(configFileName, "1");
    	System.out.println(a);    	
    }
}
