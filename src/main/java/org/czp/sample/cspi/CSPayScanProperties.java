package com.dinpay.dpp.csp.instruction.comm.util.cspay;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.dinpay.dpp.domain.system.enums.PayChannelCode;
import com.nbtv.commons.configuration.PropertiesUtils;

/**
 * @ClassName: CSPayScanProperties
 * @Description: cspay读取配置文件值工具类
 * @author czp-t269	
 * @date 2018年3月26日 上午
 */

public  class CSPayScanProperties {

	public static final String FILENAME_MSG = "bankproperties/cspay/CSPAYSCAN.properties";
	public static final String FILENAME_JDSCAN_H5_MERCHANT1 = "bankproperties/cspay/JDSCAN_H5_MERCHANT1.properties";
	public static final String FILENAME_JDSCAN_H5_MERCHANT2 = "bankproperties/cspay/JDSCAN_H5_MERCHANT2.properties";	
	public static final String FILENAME_UNION_MERCHANT = "bankproperties/cspay/UNION_MERCHANT.properties";
	
	private static Map<String, Object> basicInfoMap = new HashMap<String, Object>();
	private static Map<String, Map<String,Object>> resultMap = new HashMap<String, Map<String,Object>>();
	
	
	public static String channel[] = {PayChannelCode.CS_JDSCAN_H5.getCode(),PayChannelCode.CS_JDSCAN_H5_2.getCode(),PayChannelCode.CS_UNIONSCAN.getCode()};
	public static String fileProperties[] = { FILENAME_JDSCAN_H5_MERCHANT1,FILENAME_JDSCAN_H5_MERCHANT2,FILENAME_UNION_MERCHANT};

	/**
	 * 初始化读取配置文件参数值
	 * 
	 * @param channelCode
	 * @param debitChannelCode
	 * @return
	 */
	private static void initMerNoProperties(String[] channelCode, String[] fileProperties) {

		for (int i = 0; i < channelCode.length; i++) {
			Map<String, Object> map = PropertiesUtils.getProperties(fileProperties[i]);
			resultMap.put(channelCode[i], map);
		}
	}
	
	
	public static void init(){
		basicInfoMap = PropertiesUtils.getProperties(FILENAME_MSG);
		initMerNoProperties(channel, fileProperties);
	}

	/**
	 * 
	 * @Title: getPorpertiesValueByKey
	 * @Description: 获取通道基本配置
	 * @param key
	 * @return String
	 */
	public static String getPropertiesValueByName(String key) {
		if(basicInfoMap.isEmpty()){
			init();
		}
		return (String) basicInfoMap.get(key);
	}
	
	/**
	 * 
	 * @Title: getPorpertiesKeyByMerchantNo
	 * @Description: 获取商户号对应的秘钥
	 * @param key
	 * @return String
	 */
	public static String getPropertiesKeyByMerchantNo(String channelCode,String key) {
		if(resultMap.get(channelCode)==null || resultMap.get(channelCode).isEmpty()){
			init();
		}
		Map<String,Object> merNoMap = resultMap.get(channelCode);
		return (String) merNoMap.get(key);
	}
	
	/**
	 * @Title: getMerchantNo
	 * @Description: 获取商户号
	 * @param channleCode
	 * @return String
	 */
	public static String getMerchantNo(String channelCode) {
		if(resultMap.get(channelCode)==null || resultMap.get(channelCode).isEmpty()){
			init();
		}
		Map<String,Object> merNoMap = resultMap.get(channelCode);
		if(merNoMap==null || merNoMap.isEmpty()){
			return null;
		}
		Object [] subMerNos = merNoMap.keySet().toArray();
		int randomMer = new Random().nextInt(merNoMap.size());
		return String.valueOf(subMerNos[randomMer]);
	}
	
}
