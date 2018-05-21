package org.czp.cspi;

import org.czp.utils.PropertiesUtils;

public class CSPIH5CodeGenerator 
{
    public static void main( String[] args )
    {
    	String configFileName = "properties/1.properties";
    	String a = PropertiesUtils.getProperty(configFileName, "1");
    	System.out.println(a);    	
    }
}
