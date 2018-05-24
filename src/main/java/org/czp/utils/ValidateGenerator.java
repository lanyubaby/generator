package org.czp.utils;

import org.apache.commons.lang.StringUtils;

public class ValidateGenerator {
	
	
//	 if(StringUtils.isBlank(result)){
//		log.info(new StringBuilder(preLogMsg).append("response result is null!").toString());
//		response.setRetMsg("请求银行异常");
//		return response;
//	}
//   log.info(new StringBuilder(preLogMsg).append("response result:").append(result).toString());
	
	public String resultIsBlankValidate(String className) {		
		StringBuilder valiSb = new StringBuilder();		
		valiSb.append("//判断请求是否为空 \n")
    	.append("if(StringUtils.isBlank(result)){ \n")    	  	
    	.append("log.info(\" ").append(className).append(" : response result is null! \"); \n")
    	.append("response.setRetMsg(\"请求银行异常\"); \n") 	
    	.append("return response; \n")
    	.append("} \n")
    	.append("\n");   			
		return valiSb.toString();   			
	}
	
	
//	String ret_code = String.valueOf(returnMap.get("ret_code"));
//    String ret_message = String.valueOf(returnMap.get("ret_message"));
//    if(null != ret_code && !"SUCCESS".equals(ret_code)){
//    	StringBuilder msg = new StringBuilder(preLogMsg);
//		msg.append("response result retCode:").append(ret_code);
//		msg.append("; message:").append(ret_message);
//		msg.append("; trade_no:").append(trade_no);
//		log.info(msg.toString());
//		response.setRetMsg(ret_message);
//		return response;
//    }
	
	public String retCodeValidate(String validateFileName,String className) {
		
		StringBuilder valiSb = new StringBuilder();		
		valiSb.append("//判断通讯状态 \n");
		
		//应答码
    	String ret_code= PropertiesUtils.getProperty(validateFileName, "retCode");
    	String ret_code_right = PropertiesUtils.getProperty(validateFileName, ret_code);
    	//应答信息
    	String ret_message= PropertiesUtils.getProperty(validateFileName, "retMessage");		
		
		valiSb.append(" String ret_code = String.valueOf(returnMap.get(\"").append(ret_code).append("\")); \n");
		
		if(!StringUtils.isEmpty(ret_message)) {
			valiSb.append(" String ret_message = String.valueOf(returnMap.get(\"").append(ret_message).append(" \")); \n");
		}
    	   	
		valiSb.append("if(null != ret_code && !\"").append(ret_code_right).append("\".equals(ret_code)){ \n")
    	.append("log.info( \" ")
    	
    	.append(className).append(" response result retCode:  \" + ret_code ");
    	
		if(!StringUtils.isEmpty(ret_message)) {
			valiSb.append(" + \" message:  \" + ret_message ");
		}		
		
    	valiSb.append(" ); \n");
    	
    	if(!StringUtils.isEmpty(ret_message)) {
    		valiSb.append(" response.setRetMsg(ret_message);\n");
    	}else {   	   		
    		valiSb.append(" response.setRetMsg(\" response result retCode: \" + ret_code ); \n");
    	}
    	
    	valiSb.append(" return response;\n")
    	.append("} \n")
    	.append("\n");   
    	
    	return valiSb.toString();		
	}
	
	

	
//	//验签
//    String retSign = String.valueOf(returnMap.remove("sign"));
//    if(!CSPayUtil.verify(retSign, returnMap, key)){
//    	StringBuilder msg = new StringBuilder(preLogMsg);
//    	msg.append("signature verification failed! ");
//    	msg.append("traceno:").append(trade_no);
//    	log.info(msg.toString());
//		response.setRetMsg("verification failed");
//		return response;
//    }
	
	
	public String signValidate(String validateFileName , String utilsName , String className) {
		
		//验签
    	String sign= PropertiesUtils.getProperty(validateFileName, "sign");
    	
		StringBuilder valiSb = new StringBuilder();		
		valiSb.append(" //验签 \n")
		.append(" String retSign = String.valueOf(returnMap.remove(\"").append(sign).append("\"));\n")		
		.append(" if(! ").append(utilsName).append(" .verify(retSign, returnMap, key)){ \n")				
		.append("log.info( \" ").append(className).append(" : signature verification failed! trade_no: \" + trade_no); \n")   				
		.append(" response.setRetMsg(\"verification failed\");\n")
		.append(" return response; \n")
		.append(" } \n")
		.append(" \n");
				
		return valiSb.toString();
	}
	
	
	
//	//校验交易状态
//    String tradeStatus = String.valueOf(returnMap.get("tradeStatus"));
//    if(!("3".equals(tradeStatus))){
//    	StringBuilder msg = new StringBuilder(preLogMsg);
//    	msg.append("not success! ");
//    	msg.append("transStatus:").append(tradeStatus);
//    	msg.append("; trade_no:").append(trade_no);
//    	log.info(msg.toString());
//    	response.setRetMsg("apply not success!");
//    	return response;
//    }
	
	public String tradeStatusValidate(String validateFileName,String className) {
		
		StringBuilder valiSb = new StringBuilder();		
		valiSb.append(" //校验交易状态 \n");
	
		//交易状态
    	String trade_status= PropertiesUtils.getProperty(validateFileName, "tradeStatus");
    	String trade_status_right = PropertiesUtils.getProperty(validateFileName, trade_status);
    	//交易信息
    	String trade_msg= PropertiesUtils.getProperty(validateFileName, "tradeMsg");   	
		
		valiSb.append(" String trade_status = String.valueOf(returnMap.get(\"").append(trade_status).append("\")); \n");
		
		if(!StringUtils.isEmpty(trade_msg)) {
			valiSb.append(" String trade_msg = String.valueOf(returnMap.get(\"").append(trade_msg).append(" \")); \n");
		}
    	   	
		valiSb.append("if(null != trade_status && !\"").append(trade_status_right).append("\".equals(trade_status)){ \n")
    	.append("log.info( \" ")
    	
    	.append(className).append(" response result trade_status:  \" + trade_status ");
    	
		if(!StringUtils.isEmpty(trade_msg)) {
			valiSb.append(" + \" message:  \" + trade_msg ");
		}		
		
    	valiSb.append(" ); \n");
    	
    	if(!StringUtils.isEmpty(trade_msg)) {
    		valiSb.append(" response.setRetMsg(trade_msg);\n");
    	}else {   	   		
    		valiSb.append(" response.setRetMsg(\" response result trade_status: \" + trade_status ); \n");
    	}
    	
    	valiSb.append(" return response;\n")
    	.append("} \n")
    	.append("\n");   
    	
    	return valiSb.toString();		
    	   		
	}
	
	
	
	
	//	//校验订单号
	//    String retTradeNo = String.valueOf(returnMap.get("orderNo"));
	//	if(!trade_no.equals(retTradeNo)){
	//		StringBuilder msg = new StringBuilder(preLogMsg);
	//		msg.append("return bankTradeId is not match. ");
	//		msg.append("traceno:").append(trade_no);
	//		msg.append("; returnTraceno:").append(retTradeNo);
	//		log.info(msg.toString());
	//		return response;
	//	}
	 
	
	public String orderNumValidate(String validateFileName,String className) {
		
		//订单号
    	String tradeNo= PropertiesUtils.getProperty(validateFileName, "tradeNo");
    	
		StringBuilder valiSb = new StringBuilder();	
		
		 valiSb.append("//校验订单号 \n")
		.append(" String retTradeNo = String.valueOf(returnMap.get(\"").append(tradeNo).append("\")); \n")
		.append(" if(!trade_no.equals(retTradeNo)){ \n")				
		
		
		
		.append("log.info( \" ").append(className).append(" : return bankTradeId is not match. trade_no: \" + trade_no +\" returnTraceno: \" + retTradeNo ); \n")  		
		.append(" response.setRetMsg(\"return bankTradeId is not match.\");\n")
		.append(" return response;\n")
		.append(" } \n")
		.append(" \n").toString();
		
		return valiSb.toString();
	
	}
	
			
	
	
	public static void main(String[] args) {
		String validateFileName = "cspti/properties/validateProperties.properties";  
		ValidateGenerator va = new ValidateGenerator();
		String vaStr = va.orderNumValidate(validateFileName,"CspayInstruction");
		System.out.println(vaStr);
	}
	
	
	
	
}
