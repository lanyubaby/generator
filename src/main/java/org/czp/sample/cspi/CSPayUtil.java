package com.dinpay.dpp.csp.instruction.comm.util.cspay;

import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dinpay.dpp.csp.instruction.comm.util.InstructionUtil;

public class CSPayUtil {
	
	private static final Log log = LogFactory.getLog(CSPayUtil.class);

	public static String getSign(Map<String, ?> params, String key) throws Exception {
		
		//判断空值
		if(params.isEmpty()){log.warn("CSPayUtil.getSign:The column params is empty!");}
		if(StringUtils.isEmpty(key)){log.warn("CSPayUtil.getSign:The column key is empty!");}
		
		String origStr = InstructionUtil.getParamsStr(params, key);
		String sign = DigestUtils.md5Hex(origStr.getBytes("UTF-8")).toUpperCase();
		  
		return sign;
	}
	
	public static boolean verify(String retSign,Map<String, ?> params, String key) throws Exception {
		
		//判断空值
		if(StringUtils.isEmpty(retSign)){log.warn("CSPayUtil.verify:The column retSign is empty!");return false;}
		if(params.isEmpty()){log.warn("CSPayUtil.verify:The column params is empty!");return false;}
		if(StringUtils.isEmpty(key)){log.warn("CSPayUtil.verify:The column key is empty!");return false;}
		
		String origStr = InstructionUtil.getParamsStr(params, key);
		String sign = DigestUtils.md5Hex(origStr.getBytes("UTF-8")).toUpperCase();
		
		return retSign.equals(sign); 
	}
	
}