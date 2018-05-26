package org.czp.cspti;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.czp.utils.MapParamGenerator;
import org.czp.utils.PropertiesUtils;
import org.czp.utils.ValidateGenerator;

public class CSPTICodeGenerator 
{	
			
	//扫码支付指令文件名
	static String className = "WZYFPayTransferServiceImpl";
	//配置文件工具类名
	static String propertiesName = "WZYFPayProperties";
	//工具类名
	static String utilsName = "WZYFPayUtil";	
	//判断交易金额是元还是分 : yuan/fen
	static String moneyFlag = "";	
	static String fenStr = "";
					
	//1 导包
	public static String packageGenerator() {
		StringBuilder sb = new StringBuilder();
		sb.append("package com.dinpay.dpp.csp.transfer.services.impl; \n")		
		.append("import java.math.BigDecimal; \n ")
		.append("import java.text.DecimalFormat; \n ")
		.append("import java.util.Date; \n ")
		.append("import java.util.HashMap; \n ")
		.append("import java.util.Map; \n ")
		.append("import java.util.TreeMap; \n ")
		.append("import org.apache.commons.codec.digest.DigestUtils; \n ")
		.append("import org.apache.commons.lang.StringUtils; \n ")
		.append("import org.slf4j.Logger; \n ")
		.append("import org.slf4j.LoggerFactory; \n ")
		.append("import org.springframework.stereotype.Component; \n ")
		.append("import com.alibaba.fastjson.JSON; \n ")
		.append("import com.alibaba.fastjson.TypeReference; \n ")
		.append("import com.dinpay.dpp.csp.api.response.ServiceResponse; \n ")
		.append("import com.dinpay.dpp.csp.api.response.TransferResultResponse; \n ")
		.append("import com.dinpay.dpp.csp.transfer.commons.cspay.CSPayProperties; \n ")
		.append("import com.dinpay.dpp.csp.transfer.commons.cspay.CSPayUtil; \n ")
		.append("import com.dinpay.dpp.csp.transfer.commons.mm.MMVerifyUtils; \n ")
		.append("import com.dinpay.dpp.csp.transfer.commons.utils.InstructionUtil; \n ")
		.append("import com.dinpay.dpp.csp.transfer.services.ITransferServices; \n ")
		.append("import com.dinpay.dpp.domain.member.business.Transfer; \n ")
		.append("import com.dinpay.dpp.domain.member.config.MemberSettleconfig; \n ")
		.append("import com.dinpay.dpp.domain.member.enums.TransferOperateStatus; \n ")
		.append("import com.nbtv.commons.factory.SupportCodes; \n ")
		.append("import com.nbtv.commons.http.HttpClientUtils; \n ")
		.append("import org.slf4j.Logger; \n ")
		.append("import org.slf4j.LoggerFactory; \n ")
		
		
				
		//动态生成对应的properties工具类
		.append(" 动态生成对应的properties工具类  \n")
		//动态生成对应的Util工具类
		.append(" 动态生成对应的Util工具类 \n")		
		.append(" \n");
		
		return sb.toString();
	}
	
	
	//2 注释
	/**
	 * 万众云付-扫码
	 * @ClassName WZYFPayScanInstruction
	 * @author 曹治平
	 * @date 2017年10月24日 上午10:17:20
	 * 
	 */
	public static String remarkGenerator() {
		
		String taskName = "";
		String className = "";
		String author = "曹治平";
		String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		
		StringBuilder sb = new StringBuilder();
		sb.append("/** \n")
		.append("* ")
		.append(taskName + "\n")
		.append("* @ClassName ")
		.append(className + "\n")
		.append("* @author ")
		.append(author + "\n")
		.append("* @date ")
		.append(date + "\n")
		.append("* \n")
		.append("*/ \n");
		
		return sb.toString();
	}
	
	
	//3 标签
	//	@SupportCodes({"ZG2_WXSCAN","ZG2_QQSCAN", "ZG2_JDH5"})
	//	@Component
	public static String labelGenerator() {
		
		StringBuilder channelSb = new StringBuilder();
		
		List<String> channelCodeList = new ArrayList<String>();		
		channelCodeList.add("WZYFPAY1");	
				
		for(String channelCode : channelCodeList) {
			channelSb.append("\"")
			.append(channelCode)
			.append("\"")
			.append(",");
		}
		
		String channelSbStr = channelSb.substring(0,channelSb.length() - 1);
			
		StringBuilder labelSb = new StringBuilder();
		labelSb.append("@SupportCodes({")		
		.append( channelSbStr)		
		.append("}) \n")
		.append("@Component \n");
		return labelSb.toString();
	}
	
	
	
	//4 代码
	public static String codeGenerator() {
				
						
		StringBuilder headSb = new StringBuilder();
		String headSbStr = headSb.append("public class ")
		.append(className)
		.append(" ")
		.append("implements ITransferServices{ \n")
//		private final static Logger log = LoggerFactory.getLogger(CSPayTransferServiceImpl.class);
		.append("private final static Logger log = LoggerFactory.getLogger(").append(className).append(".class); \n")
		.append("\n")
		.append("@SuppressWarnings(\"unchecked\") \n")
		.append("@Override \n")
		.append("public TransferResultResponse transfer(Transfer transfer) { \n")
		.append("//代付通道code \n")
		.append("String channelCode = transfer.getPayBankCode(); \n")
		.append("//获取转账ID \n")
		.append("String transferId = transfer.getId(); \n")
		.append("String preLogMsg = \"TransferResultResponse transfer[\" + transferId + \"] \";")
		.append("//默认代付状态 \n")
		.append("TransferResultResponse response = new TransferResultResponse(); \n")
		.append("response.setRetCode(ServiceResponse.RETCODE_SUCCESS); \n")
		.append("response.setResult(TransferOperateStatus.TRANSFER_DOING.getStatus()); \n")
		.append("response.setRetMsg(TransferOperateStatus.TRANSFER_DOING.getTitle()); \n")
		.append("response.setBankRetInfo(TransferOperateStatus.TRANSFER_DOING.getTitle()); \n")									
		.append("try { \n").toString();
		
		//#################################从transfer取值#################################
//		//代付金额(单位为分)
//		BigDecimal trans_money_yuan = transfer.getTransferMoney();
//		String trans_money = String.valueOf(trans_money_yuan.multiply(new BigDecimal("100")).intValue());
//		//商户交易单号
//		String trade_no = transfer.getBankTradeId();
//		//户名
//		String account_name = transfer.getReceiveAccountName();
//		//银行卡号
//		String bank_card = transfer.getReceiveAccount();
//		//支行名称
//		String bank_name = transfer.getBankSubbranch();
//		//联行号
//		String bank_linked = transfer.getUniteBankId();		
		
		StringBuilder transferSb = new StringBuilder();
		String transferSbStr = transferSb.append("//代付金额(单位为分) \n")
		.append("BigDecimal trans_money_yuan = transfer.getTransferMoney(); \n")
		.append("String trans_money = String.valueOf(trans_money_yuan.multiply(new BigDecimal(\"100\")).intValue()); \n")
		.append("//商户交易单号 \n")
		.append("String trade_no = transfer.getBankTradeId(); \n")
		.append("//户名 \n")
		.append("String account_name = transfer.getReceiveAccountName(); \n")
		.append("//银行卡号 \n")
		.append("String bank_card = transfer.getReceiveAccount(); \n")
		.append("//支行名称 \n")
		.append("String bank_name = transfer.getBankSubbranch(); \n")
		.append("//联行号 \n")
		.append("String bank_linked = transfer.getUniteBankId(); \n").toString();
						
		//#################################从properties取值#################################
//		//商户号
//		String mch_id = CSPayProperties.getPorpertiesValueByName(channelCode, "MERCHANT_ID");
//		//MD5签名key
//		String key = CSPayProperties.getPorpertiesValueByName(channelCode, "KEY");
//		//代付地址
//		String REQUEST_URL = CSPayProperties.getPorpertiesValueByName(channelCode, "TRANSFER_URL");
		
		StringBuilder propertiesSb = new StringBuilder();
		String propertiesSbStr = propertiesSb.append("//商户号 \n")
		.append("String mch_id =  ").append(propertiesName).append(".getPorpertiesValueByName(channelCode, \"MERCHANT_ID\"); \n")
		.append("//MD5签名key \n")
		.append("String KEY = ").append(propertiesName).append(" .getPorpertiesValueByName(channelCode, \"KEY\");\n")
		.append("//代付地址 \n")
		.append("String REQUEST_URL =  ").append(propertiesName).append(" .getPorpertiesValueByName(channelCode, \"TRANSFER_URL\"); \n")
		.toString();
		
		
		//################################拼接请求参数#######################################
				
		//读取上游属性配置文件
    	String configFileName = "cspti/properties/upstreamProperties.properties";
    	
    	MapParamGenerator mGenerator = new MapParamGenerator();
    	String mapStr = mGenerator.mapParamGenerator(configFileName);
    	
    	StringBuilder paramSb = new StringBuilder();
    	paramSb.append(mapStr)		
    	.append(" //签名 \n")
    	.append("String sign =  ").append(utilsName).append(".getSign(params, KEY); \n")  	
    	.append("if (StringUtils.isEmpty(sign.trim())) { \n")
    	.append("log.info(\"csp.instructionservice.").append(className).append(".sign failed\"); \n")
    	.append("response.setRetCode(ServiceResponse.RETCODE_SYSERR); \n")
    	.append("response.setRetMsg(\"签名失败！\"); \n")
    	.append("return response; \n")
    	.append("} \n")
    	.append("params.put(\"sign\",sign); \n")
    	.append(" \n").toString(); 
    	
    	 String paramSbStr = paramSb.toString();
    	    	   	   	
    	
    	//################################发送请求#######################################
    	
    	StringBuilder httpSb = new StringBuilder();
    	//log.info("CSPayScanInstruction request params : " + params);
    	String httpSbStr = httpSb.append("log.info(\"").append(className).append(" request params : \" + params); \n")
    	.append(" \n")
    	// String result = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, params,"UTF-8");
    	.append("String result = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, params,\"UTF-8\"); \n").toString();
    	
    	
    	//################################ 拼接验证代码  #######################################	
    	
    	
    	String validateFileName = "cspti/properties/validateProperties.properties";   	
    	
    	
    	
    	//订单号
    	String tradeNo= PropertiesUtils.getProperty(validateFileName, "tradeNo");
    	//应答码
    	String ret_code= PropertiesUtils.getProperty(validateFileName, "retCode");
    	String ret_code_right = PropertiesUtils.getProperty(validateFileName, ret_code);
    	//应答信息
    	String ret_message= PropertiesUtils.getProperty(validateFileName, "retMessage");
    	//验签
    	String sign= PropertiesUtils.getProperty(validateFileName, "sign");
    	//交易状态
    	String trade_status= PropertiesUtils.getProperty(validateFileName, "tradeStatus");
    	String trade_status_right = PropertiesUtils.getProperty(validateFileName, trade_status);
    	//交易信息
    	String tradeMsg= PropertiesUtils.getProperty(validateFileName, "tradeMsg");
    	
    	
    	StringBuilder valiSb = new StringBuilder();
    	
    	//判断请求是否为空
    	ValidateGenerator va = new ValidateGenerator();
		String reStr = va.resultIsBlankValidate(className);
		valiSb.append(reStr)
		
		//打印返回参数
		.append("log.info(").append(className).append(" : response result: ).append(result).toString()); \n")
    	.append("\n")    	
    	
//    	 //json转map
    	.append("//json转map")
    	.append("TreeMap<String, Object> returnMap = JSON.parseObject(result,new TypeReference<TreeMap<String, Object>>(){} );")
    	.append("\n");
    	    	
    	//判断请求状态    	
    	String retCodeV = va.retCodeValidate(validateFileName, className);
    	valiSb.append(retCodeV);
    	
//    	//验签
    	String signV = va.signValidate(validateFileName, utilsName, className);
    	valiSb.append(signV);
    	   	   	
//    	//校验交易状态
    	String tradeV = va.tradeStatusValidate(validateFileName, className);
    	valiSb.append(tradeV);
    	    		
//    	//校验订单号
    	String orderV = va.orderNumValidate(validateFileName, className);
    	valiSb.append(orderV);
    	 
    	String valiSbStr = valiSb.toString();
    	    	   	   	
    	//################################结尾代码 #######################################
    	
    	
//    	log.info("TransferResultResponse transfer[" + transferId + "] doing! traceno:" + trade_no);		
//		response.setBankRetInfo(ret_message);
//		response.setBankTradeId(retOrderId);
    	 
    	StringBuilder endSb = new StringBuilder();   	
    	endSb.append(" log.info(\"TransferResultResponse transfer[\" + transferId + \"] doing! traceno:\" + trade_no);	 \n")
    	.append(" response.setBankRetInfo(ret_message); \n")
    	.append(" response.setBankTradeId(retOrderId); \n")
    	
    	
//	} catch (Exception e) {
//    	log.error("TransferResultResponse transfer[" + transferId + "] throw exception:", e);
//		response.setRetCode(ServiceResponse.RETCODE_SYSERR);
//		response.setResult(TransferOperateStatus.TRANSFER_EXCEPTION.getStatus());
//		response.setBankRetInfo(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle()+ e.getMessage());
//	}
//	return response;
//}    	
    	
    	.append(" } catch (Exception e) { \n")
    	.append(" log.error(\"TransferResultResponse transfer[\" + transferId + \"] throw exception:\", e); \n")
    	.append(" response.setRetCode(ServiceResponse.RETCODE_SYSERR); \n")
    	.append(" response.setResult(TransferOperateStatus.TRANSFER_EXCEPTION.getStatus()); \n")
    	.append(" response.setBankRetInfo(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle()+ e.getMessage()); \n")
    	.append(" } \n")
    	.append(" return response; \n")
    	.append(" } \n")
    	.append(" \n").toString();
    	
    	String endSbStr = endSb.toString();
    	    	    	
    	
    	StringBuilder codeStr= new StringBuilder();
    	codeStr
//    	.append(headSbStr);
//    	.append(transferSbStr)
//    	.append(propertiesSbStr)
    	.append(paramSbStr);
//    	.append(httpSbStr);
//    	.append(valiSbStr);
//    	.append(endSbStr);
        	
    	return codeStr.toString();
	}
	
	
	
	public static String queryMethodGenerator() {
		
		
	 	
    	//################################ CSPTI-QUERY #######################################
    	
    	StringBuilder beginSb = new StringBuilder();      	
    	String beginSbStr =  beginSb.append(" @Override \n")
    	.append(" public TransferResultResponse queryTransferResult(Transfer transfer) { \n")
    	   	
//    	log.info("---CSPAY query transfer result start, transferId=" + transfer.getId());		
//		TransferResultResponse response = new TransferResultResponse();
//		response.setRetCode(ServiceResponse.RETCODE_SUCCESS);
//		response.setResult(TransferOperateStatus.TRANSFER_DOING.getStatus());
//		response.setRetMsg(TransferOperateStatus.TRANSFER_DOING.getTitle());
//		response.setBankRetInfo(TransferOperateStatus.TRANSFER_DOING.getTitle());
    	      	 	
    	
    	.append("log.info(\"---").append(className).append("  query transfer result start, transferId=\" + transfer.getId());\n")
    	.append(" TransferResultResponse response = new TransferResultResponse(); \n")
    	.append(" response.setRetCode(ServiceResponse.RETCODE_SUCCESS); \n")
    	.append(" response.setResult(TransferOperateStatus.TRANSFER_DOING.getStatus()); \n")
    	.append(" response.setRetMsg(TransferOperateStatus.TRANSFER_DOING.getTitle()); \n")
    	.append(" response.setBankRetInfo(TransferOperateStatus.TRANSFER_DOING.getTitle()); \n")
    	.toString();
    	
    	
    	
    	
    	//漏了try
    	
    	
    	
    	//#################################从properties取值#################################
//    	//虚拟账号
//		String MERCHANT_ID = CSPayProperties.getPorpertiesValueByName(channelCode, "MERCHANT_ID");
//		//MD5签名key
//		String key = CSPayProperties.getPorpertiesValueByName(channelCode, "KEY");
//		//代付查询地址
//		String REQUEST_URL = CSPayProperties.getPorpertiesValueByName(channelCode, "QUERY_URL");
//    	
    	StringBuilder queryPropertiesSb = new StringBuilder();
    	String queryPropertiesSbStr = queryPropertiesSb.append("//虚拟账号 \n")
    	.append(" String MERCHANT_ID = ").append(propertiesName).append(" .getPorpertiesValueByName(channelCode, \"MERCHANT_ID\");\n")
    	.append(" //MD5签名key \n")
    	.append(" String key = ").append(propertiesName).append(" .getPorpertiesValueByName(channelCode, \"KEY\"); \n")
    	.append(" //代付查询地址 \n")
    	.append(" String REQUEST_URL = ").append(propertiesName).append(" .getPorpertiesValueByName(channelCode, \"QUERY_URL\");\n")
    	.toString();    	    	
    	
    	//#################################从transfer取值#################################
//    	//通道CODE
//		String channelCode = transfer.getPayBankCode();
//    	//订单号
//		String tranceno = transfer.getBankTradeId(); 		
//		//金额
//		String transferMoney = String.valueOf(transfer.getTransferMoney().multiply(new BigDecimal("100")).intValue());
    	
    	StringBuilder queryTransferSb = new StringBuilder();
    	String queryTransferSbStr = queryTransferSb.append(" //通道CODE \n")
    	.append(" String channelCode = transfer.getPayBankCode(); \n")
    	.append(" \n")
    	.append(" //订单号 \n")
    	.append(" String tranceno = transfer.getBankTradeId();  \n")
    	.append(" //金额 \n")
    	.append(" String transferMoney = String.valueOf(transfer.getTransferMoney().multiply(new BigDecimal(\"100\")).intValue()); \n")
    	.toString();
    	
    		
    	//################################拼接请求参数#######################################
		//读取上游属性配置文件
    	String configFileName = "cspti/properties/upstreamQueryProperties.properties";
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
    	   	
    	paramSb.append("\n")
		
    	.append(" //签名 \n")
    	.append("String sign =  ").append(utilsName).append(".getSign(params, KEY); \n")
    	
    	.append("if (StringUtils.isEmpty(sign.trim())) { \n")
    	//log.info("csp.instructionservice.CSPayScanInstruction.sign failed");
    	.append("log.info(\"csp.instructionservice.").append(className).append(".sign failed\"); \n")
    	.append("response.setRetCode(ServiceResponse.RETCODE_SYSERR); \n")
    	.append("response.setRetMsg(\"签名失败！\"); \n")
    	.append("return response; \n")
    	.append("} \n")
    	.append("params.put(\"sign\",sign); \n")
    	.append(" \n").toString();  
		
		String paramSbStr = paramSb.toString();
    	//################################发送请求#######################################
//		log.info("TransferResultResponse quertyTransferResult request data:" + params.toString());		
//		//post请求
//	    String result = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, params,"UTF-8");
    	
    	
    	StringBuilder httpSb = new StringBuilder();
//		log.info("TransferResultResponse quertyTransferResult request data:" + params.toString());	
    	httpSb.append("log.info(\"TransferResultResponse quertyTransferResult request data:\" + params.toString());	")
    	.append(" \n")
    	// String result = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, params,"UTF-8");
    	.append("String result = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, params,\"UTF-8\"); \n")
    	.toString();
    	String httpSbStr = httpSb.toString();
    	
    	
    	//################################ 拼接验证代码  #######################################	
    	String validateFileName = "cspti/properties/validateQueryProperties.properties";
    	
    	//商户号
    	String merchantId= PropertiesUtils.getProperty(validateFileName, "merchantId");
    	//订单号
    	String tradeNo= PropertiesUtils.getProperty(validateFileName, "tradeNo");
    	//应答码
    	String ret_code= PropertiesUtils.getProperty(validateFileName, "retCode");
    	String ret_code_right = PropertiesUtils.getProperty(validateFileName, "ret_code");
    	//应答信息
    	String ret_message= PropertiesUtils.getProperty(validateFileName, "retMessage");
    	//验签
    	String sign= PropertiesUtils.getProperty(validateFileName, "sign");
    	//交易状态
    	String trade_status= PropertiesUtils.getProperty(validateFileName, "tradeStatus");
    	String trade_status_right = PropertiesUtils.getProperty(validateFileName, "trade_status");
    	//交易信息
    	String tradeMsg= PropertiesUtils.getProperty(validateFileName, "tradeMsg");
    	//金额
    	String resFee= PropertiesUtils.getProperty(validateFileName, "resFee");
    	
    	StringBuilder valiSb = new StringBuilder();
    	
//    	if(StringUtils.isBlank(result)){
//				log.info("TransferResultResponse queryTransferResult response result is null!" );
//				response.setRetMsg("request bank exception!");
//				return response;
//			}
//		    
//	    log.info("TransferResultResponse queryTransferResult response result:" + result);
	    		    
    	
    	valiSb.append("//判断请求是否为空 \n")
    	.append(" if(StringUtils.isBlank(result)){ \n")
    	.append(" log.info(\"TransferResultResponse queryTransferResult response result is null!\" ); \n")
    	.append(" response.setRetMsg(\"request bank exception!\"); \n")
    	.append(" return response; \n") 
    	.append(" } \n")
    	.append(" \n")
    	.append(" log.info(\"TransferResultResponse queryTransferResult response result:\" + result); \n")
    	.append(" \n")
    	
//    	 //json转map
//		TreeMap<String, Object> returnMap = JSON.parseObject(result,new TypeReference<TreeMap<String, Object>>(){} );
    	.append("//json转map \n")
    	.append("TreeMap<String, Object> returnMap = JSON.parseObject(result,new TypeReference<TreeMap<String, Object>>(){} );")
    	.append("\n")
    	
//    	//校验状态:SUCCESS
//    	String ret_code = String.valueOf(returnMap.get("ret_code"));
//		String ret_message = String.valueOf(returnMap.get("ret_message"));
//		if(!"SUCCESS".equals(ret_code)){
//			log.info("TransferResultResponse transfer result resCodeRe:" + ret_code 
//					+ ";msg:" + ret_message+",bankTradeId:"+tranceno);
//			response.setRetMsg(ret_message);
//			return response;
//		}
    	
    	
    	.append("//判断请求状态  \n")
    	.append(" String ret_code = String.valueOf(returnMap.get(\"").append(ret_code).append("\")); \n")
    	.append(" String ret_message = String.valueOf(returnMap.get(\"").append(ret_message).append("\")); \n")   	
//	    if(null != ret_code && !"SUCCESS".equals(ret_code)){    	
    	.append("if(null != ret_code && !\"").append(ret_code_right).append("\".equals(ret_code)){ \n")  	
    	.append(" log.info(\"TransferResultResponse transfer result resCodeRe:\" + ret_code + \";msg:\" + ret_message+\",bankTradeId:\"+tranceno); \n")
    	.append(" response.setRetMsg(ret_message); \n")
    	.append(" return response; \n")
    	.append(" }\n")
    	.append(" \n")
    	
    	.append("//校验验签 \n")    	
//    	String retSign = String.valueOf(returnMap.remove("sign"));
//		if(!CSPayUtil.verify(retSign, returnMap, key)){
//			log.info("TransferResultResponse transfer signature verification failed! bankTradeId:"
//					+ tranceno);
//			response.setRetMsg("verification failed");
//			return response;
//		}
    	    	
    	.append(" String retSign = String.valueOf(returnMap.remove(\"").append(sign).append("\"));\n")
    	.append(" if(! ").append(utilsName).append(" .verify(retSign, returnMap, key)){ \n")
    	.append(" log.info(\"TransferResultResponse transfer signature verification failed! bankTradeId:\"+ tranceno); \n")   	
      	.append(" response.setRetMsg(\"verification failed\");\n")
    	.append(" return response; \n")
    	.append(" } \n")
    	.append(" \n")
    	    	   	
    	
    	.append(" //校验交易状态 \n")
    	
//    	String payStatus = String.valueOf(returnMap.get("payStatus"));
//	    if(!"3".equals(payStatus)){
//	    	log.info(new StringBuilder("TransferResultResponse: transfer not success status:").append(payStatus)
//	    			.append(" ; bankTradeId :").append(tranceno).toString());
//	    	response.setRetMsg(ret_message);
//	    	return response;
//	    }
    	
    	.append(" String tradeStatus = String.valueOf(returnMap.get(\"").append(trade_status).append("\")); \n")
      	.append(" if(!(\"").append(trade_status_right).append(" \".equals(tradeStatus))){ \n")    	
      	.append(" log.info(new StringBuilder(\"TransferResultResponse: transfer not success status:\").append(payStatus).append(\" ; bankTradeId :\").append(tranceno).toString()); \n")
      	.append(" \n")     	    	
    	.append("response.setRetMsg(ret_message); \n")
    	.append("return response; \n")
    	.append(" } \n")
    	.append("\n")
    	
    	
    	
    	
    	
    	.append("//校验商户号 \n")
//    	 //校验商户号
//		String retMerchantId = String.valueOf(returnMap.get("mch_id"));
//	    if(!MERCHANT_ID.equals(retMerchantId)){
//	    	log.info(new StringBuilder("TransferResultResponse: return merchantId is not match. merchantId:")
//	    	.append(MERCHANT_ID).append("; bankMerchantId:").append(retMerchantId).toString());
//	    	response.setRetMsg("merchantId is not match!");
//	    	return response;
//	    }
    	
    	.append(" String retMerchantId = String.valueOf(returnMap.get(\"").append(merchantId).append("\")); \n")
    	.append(" if(!MERCHANT_ID.equals(retMerchantId)){ \n")
    	.append(" log.info(new StringBuilder(\"TransferResultResponse: return merchantId is not match. merchantId:\") ")
    	.append(" .append(MERCHANT_ID).append(\"; bankMerchantId:\").append(retMerchantId).toString()); \n")
    	.append(" response.setRetMsg(\"merchantId is not match!\"); \n")
    	.append(" return response; \n")
    	.append("} \n")
    	    	    	
    	
    	
    	.append("//校验金额  \n")
    	
//    	//校验金额
//	    String  resFee = (String) returnMap.get("total_fee");
//	    BigDecimal total_fee = new BigDecimal(resFee);		    
//	    if (new BigDecimal(transferMoney).compareTo(total_fee) != 0) {
//			log.info(new StringBuilder("TransferResultResponse queryTransferResult amount mismatch! total_fee:")
//			.append(total_fee).append("transferMoney:").append(transferMoney).toString());				
//			response.setRetMsg("amount mismatch!");
//			return response;
//		}
    	
    	
    	.append(" String  resFee = (String) returnMap.get(\"").append(resFee).append("\"); \n")
    	.append(" BigDecimal total_fee = new BigDecimal(resFee); \n")
    	.append(" if (new BigDecimal(transferMoney).compareTo(total_fee) != 0) { \n")
    	.append(" log.info(new StringBuilder(\"TransferResultResponse queryTransferResult amount mismatch! total_fee:\") ")
    	.append(" .append(total_fee).append(\"transferMoney:\").append(transferMoney).toString()); \n")
    	.append(" response.setRetMsg(\"amount mismatch!\"); \n")
    	.append(" return response; \n")
    	.append(" } \n")
    	.append(" \n").toString();
    	
    	String valiSbStr = valiSb.toString();
    	
    	//################################结尾代码 #######################################
    	
//    	 log.info("CSPAYTransfer success! bankTradeId:" + tranceno);
//			response.setBankTradeId(tranceno);
//			response.setResult(TransferOperateStatus.TRANSFER_SUCCESS.getStatus());
//			response.setRetMsg(TransferOperateStatus.TRANSFER_SUCCESS.getTitle());
//			response.setBankRetInfo(TransferOperateStatus.TRANSFER_SUCCESS.getTitle());
//			
//		} catch (Exception e) {
//			log.error("TransferResultResponse queryTransferResult transfer result throw exception:", e);
//			response.setResult(ServiceResponse.RETCODE_SYSERR);
//			response.setRetMsg(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle());
//			response.setBankRetInfo(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle());
//			return response;
//		}		
//		return response;
//	}

    	 
    	StringBuilder endSb = new StringBuilder();   	
    	endSb.append(" \n")
    	.append(" log.info(\" ").append(className).append(" success! bankTradeId:\" + tranceno); \n")
    	.append(" response.setBankTradeId(tranceno); \n")
    	.append(" response.setResult(TransferOperateStatus.TRANSFER_SUCCESS.getStatus()); \n")
    	.append(" response.setRetMsg(TransferOperateStatus.TRANSFER_SUCCESS.getTitle()); \n")
    	.append(" response.setBankRetInfo(TransferOperateStatus.TRANSFER_SUCCESS.getTitle()); \n")
    	.append(" } catch (Exception e) { \n")
    	.append(" log.error(\"TransferResultResponse queryTransferResult transfer result throw exception:\", e); \n")
    	.append(" response.setResult(ServiceResponse.RETCODE_SYSERR); \n")
    	.append(" response.setRetMsg(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle()); \n")
    	.append(" response.setBankRetInfo(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle()); \n")
    	.append(" return response; \n")
    	.append(" } \n")
    	.append(" return response; \n")
    	.append("}").toString();

		String endSbStr = endSb.toString();
		
		return valiSbStr;
		
		
		
	}
	
	
	public static String extraGenerator() {
		StringBuilder extraSb = new StringBuilder();
		
		return extraSb.append("@Override \n"
				+ "public Map<String, TransferResultResponse> batchQueryResult(Date startTime,Date endTime) { \n"
				+ "return null; \n"
				+ "} \n"
				+" \n"
				)
		
		.append("@Override \n"
				+ "public TransferResultResponse getBalance() { \n"
				+ "return null; \n"
				+ "} \n"
				+" \n"
				)
		
		.append("@Override \n"
				+ "public int verityAccountNo(MemberSettleconfig config) { \n"
				+ "return 0; \n"
				+ "} \n"
				+" \n"
				)
		
		.append("@Override \n"
				+ "public TransferResultResponse queryVerityResult(String reqSeqNo,String dealDate) { \n"
				+ "return null; \n"
				+ "} \n"
				+" \n"
				)
		
		.toString();
			
	}
	

	
	
    public static void main( String[] args )
    {
//    	String configFileName = "properties/1.properties";
//    	String a = PropertiesUtils.getProperty(configFileName, "1");
//    	System.out.println(a);   
    	   	
//    	//导包
//    	String packageStr = packageGenerator();
//    	//注释
//    	String remarkStr = remarkGenerator();
//    	//标签
//    	String labelStr = labelGenerator();
//    	//代码
//    	String codeStr = codeGenerator();    	   	 	
//    	//extra
//    	String extraSbStr = extraGenerator();
    	
    	String str = codeGenerator();
    	System.out.println(str);
    	
    }
}
