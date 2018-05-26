package org.czp.cspi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.time.DateFormatUtils;
import org.czp.utils.PropertiesUtils;

public class CSPIScanCodeGenerator 
{
	
	//1 导包
	public static String packageGenerator() {
		StringBuilder sb = new StringBuilder();
		sb.append("package com.dinpay.dpp.csp.instruction.service.pay.instruction.impl; \n")
		.append("import java.math.BigDecimal; \n")
		.append("import java.util.HashMap; \n")
		.append("import java.util.Map; \n")
		.append("import javax.annotation.Resource; \n")
		.append("import org.apache.commons.lang.StringUtils; \n")
		.append("import org.apache.commons.logging.Log; \n")
		.append("import org.apache.commons.logging.LogFactory; \n")
		.append("import org.apache.http.entity.StringEntity; \n")
		.append("import org.springframework.stereotype.Component; \n")
		.append("import com.dinpay.dpp.csp.api.response.GetPayCommandResponse; \n")
		.append("import com.dinpay.dpp.csp.api.response.ServiceResponse; \n")
		.append("import com.dinpay.dpp.csp.instruction.comm.util.InstructionUtil; \n")
		.append("import com.dinpay.dpp.csp.instruction.dao.IOrderInstructionDAO; \n")
		.append("import com.dinpay.dpp.csp.instruction.service.pay.instruction.IGetPayInstructionService; \n")
		.append("import com.dinpay.dpp.domain.order.Order; \n")
		.append("import com.nbtv.commons.factory.SupportCodes; \n")
		.append("import com.nbtv.commons.http.HttpClientUtils; \n")
		.append("import net.sf.json.JSONObject; \n")
		//动态生成对应的properties工具类
		.append(" \n")
		//动态生成对应的Util工具类
		.append(" \n")		
		.append("");
		
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
		channelCodeList.add("ZG2_WXSCAN");	
		channelCodeList.add("ZG2_WXSCAN1");	
		channelCodeList.add("ZG2_WXSCAN2");	
		channelCodeList.add("ZG2_WXSCAN3");	
		
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
	
//	public class MMScanPayInstruction implements IGetPayInstructionService {
//
//		private static final Log log = LogFactory.getLog(MMScanPayInstruction.class);
//		
//		@Resource(name="orderInstructionDAO")
//		private IOrderInstructionDAO orderInstructionDao ;
//		
//		@SuppressWarnings("unchecked")
//		@Override
//		public GetPayCommandResponse getPayCommand(Order order) {
//			//订单ID
//			String orderId = order.getOrderId();
//			//默认返回信息
//			GetPayCommandResponse response = new GetPayCommandResponse();
//			response.setRetCode(ServiceResponse.RETCODE_SYSERR);
//			response.setRetMsg("csp.instructionservice.MMScanPayInstruction.exception");
//			Map<String, Object> forwordMap = new HashMap<String, Object>();

	public static String codeGenerator() {
		
		//扫码支付指令文件名
		String className = "MMScanPayInstruction";
		//配置文件工具类名
		String propertiesName = "CSPayScanProperties";
		//工具类名
		String utilsName = "CSPayUtil";
		
		//判断交易金额是元还是分 : yuan/fen
		String moneyFlag = "";
		String fenStr = "";
		if("fen".equals(moneyFlag)){
			fenStr = "String amount = String.valueOf(txMoney.multiply(new BigDecimal(\"100\")).intValue());";
		}
						
		StringBuilder headSb = new StringBuilder();
		headSb.append("public class ")
		.append(className)
		.append(" ")
		.append("implements IGetPayInstructionService{ \n")
		.append("private static final Log log = LogFactory.getLog(")
		.append(className)
		.append(".class); \n")
		.append("@Resource(name=\"orderInstructionDAO\") \n")
		.append("private IOrderInstructionDAO orderInstructionDao; \n")
		.append("\n")
		.append("@SuppressWarnings(\"unchecked\") \n")
		.append("@Override \n")
		.append("public GetPayCommandResponse getPayCommand(Order order) { \n")
		.append("//订单ID \n")
		.append("String orderId = order.getOrderId(); \n")
		.append("//默认返回信息 \n")
		.append("GetPayCommandResponse response = new GetPayCommandResponse(); \n")
		.append("response.setRetCode(ServiceResponse.RETCODE_SYSERR); \n")
		//response.setRetMsg("csp.instructionservice.MMScanPayInstruction.exception");
		.append("response.setRetMsg(\"csp.instructionservice.")
		.append(className)
		.append(".exception\"); \n")
		.append("Map<String, Object> forwordMap = new HashMap<String, Object>(); \n")
									
		.append("try { \n").toString();
		
		//#################################上游字段和#################################
		
		
		
		
		
		//#################################从order取值#################################
		StringBuilder orderSb = new StringBuilder();
		orderSb.append("//获取通道名称 \n")
		.append("String channelCode = order.getChannelCode(); \n")
		.append("//交易金额 \n")
		//		交易金额（分）		
		.append("BigDecimal txMoney = order.getTxMoney(); \n")
		.append(fenStr +"\n").toString();	
		
		//#################################从properties取值#################################
		StringBuilder propertiesSb = new StringBuilder();
		propertiesSb.append("//获取请求地址 \n")
		//String REQUEST_URL = CSPayScanProperties.getPropertiesValueByName("REQUEST_URL");
		.append("String REQUEST_URL =  ").append(propertiesName).append(".getPropertiesValueByName(\"REQUEST_URL\"); \n")			
		.append("//商户信息 \n")
		//String MERCHANT_ID = CSPayScanProperties.getMerchantNo(channelCode);
		.append("String MERCHANT_ID = ").append(propertiesName).append(".getMerchantNo(channelCode); \n")
		.append("//秘钥 \n")
		//String KEY = CSPayScanProperties.getPropertiesKeyByMerchantNo(channelCode, MERCHANT_ID);
		.append("String KEY = ").append(propertiesName).append(".getPropertiesKeyByMerchantNo(channelCode, MERCHANT_ID); \n")
		.append("//回调URL \n")
		//String NOTIFY_URL = CSPayScanProperties.getPropertiesValueByName("NOTIFY_URL");
		.append("String NOTIFY_URL = ").append(propertiesName).append(".getPropertiesValueByName(\"NOTIFY_URL\"); \n")
		.append("//拼凑通知地址 \n")
		//String notifyUrl = String.format("%s/%s", NOTIFY_URL,channelCode);
		.append("String notifyUrl = String.format(\"%s/%s\", NOTIFY_URL,channelCode); \n")
		.append("//前台通知地址 \n")
		//String return_url = CSPayScanProperties.getPropertiesValueByName("RETURN_URL");	
		.append("String RETURN_URL = ").append(propertiesName).append(".getPropertiesValueByName(\"RETURN_URL\"); \n").toString();
		
		//################################拼接请求参数#######################################		
		
		//读取上游属性配置文件
    	String configFileName = "cspi/properties/upstreamProperties.properties";
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
    	.append(" \n")
    	//log.info("CSPayScanInstruction request params : " + params);
    	.append("log.info(\"").append(className).append(" request params : \" + params); \n")
    	.append(" \n")
    	.append("String respStr = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, params, null); \n");
    	paramSb.toString();
        
    	//################################ 拼接验证代码  #######################################	
    	
    	StringBuilder valiSb = new StringBuilder();
    	   	
//    	//判断返回是否为空
//		if (StringUtils.isEmpty(respStr)) {
//			log.info("CSPayScanInstruction the response payment is null! orderId=" + orderId);
//			response.setParams(forwordMap);
//			response.setRetCode(ServiceResponse.RETCODE_SYSERR);
//			response.setRetMsg("连接异常");
//			return response;
//		}
//    	
//    	JSONObject result = JSONObject.fromObject(respStr);
//		log.info("CSPayScanInstruction return: " + result + "; orderId=" + orderId);		
    	    	
    	valiSb.append("//判断返回是否为空 \n")
    	.append("if (StringUtils.isEmpty(respStr)) { \n")
//    	/log.info("CSPayScanInstruction the response payment is null! orderId=" + orderId);
    	.append("log.info(\"").append(className).append(" the response payment is null! orderId=\" + orderId); \n")
    	.append("response.setParams(forwordMap); \n")
    	.append("response.setRetCode(ServiceResponse.RETCODE_SYSERR); \n")
    	.append("response.setRetMsg(\"连接异常\"); \n")
    	.append("return response; \n")
    	.append("} \n")
    	
    	.append("JSONObject result = JSONObject.fromObject(respStr); \n")
    	.append("log.info(\"").append(className).append(" return: \" + result + \"; orderId=\" + orderId); \n")
    	.append(" \n");
    	   	   	
    	//映射上游返回字段和本地字段关系
    	//1.响应状态码
    	//2.响应信息
    	//3.金额
    	//4.支付链接
    	//5.   	    		
    	
//		//判断请求是否成功：SUCCESS
//		String respCode = result.getString("ret_code");
//		if (!"SUCCESS".equals(respCode)) {
//			//请求错误信息
//			String respMsg = result.getString("ret_message");
//			log.warn("CSPayScanInstruction payment failed! return respCode:" + respCode + "; respMsg:" + respMsg);
//			response.setRetMsg(respMsg);
//			return response;
//		}
    	
    	
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
//    	.append(" \n")
    	
    	
    	
    	
    	
	
		
    	
//    	String retAmount = result.getString("total_fee");
//		if(new BigDecimal(retAmount).compareTo(txMoney.multiply(new BigDecimal("100"))) != 0){
//			log.warn("CSPayScanInstruction amount verification failed! total_fee=" + amount + ", ret_total_fee=" + retAmount);
//			response.setParams(forwordMap);
//			response.setRetMsg("金额不匹配");
//			return response;
//		}
//		
//		//保存轮询的商户号
//		int num = orderInstructionDao.updateAttachmentRemarkByOrderId(orderId, MERCHANT_ID);
//		if(num != 1){
//			log.warn("CSPayScanInstruction update order remark failed! orderId=" + orderId);
//			response.setParams(forwordMap);
//			response.setRetMsg("更新数据异常");
//			return response;
//		}
//
//		String qrcode = result.getString("payinfo");
//		if (StringUtils.isEmpty(qrcode)) {
//			log.warn("CSPayScanInstruction payment get qrcode is null! orderId=" + orderId);
//			response.setParams(forwordMap);
//			response.setRetMsg("二维码地址为空");
//			return response;
//		}
//    	
//    	forwordMap.put("qrcode", qrcode);
//		response.setParams(forwordMap);
//		response.setRetCode(ServiceResponse.RETCODE_SUCCESS);
//		response.setRetMsg("获取二维码成功");
//		log.info("CSPayScanInstruction payment get qrcode success! orderId=" + orderId);
//		return response;
		
		
		
		
		return valiSb.toString();
		
	}
	
	
    public static void main( String[] args )
    {
    	String packageStr = codeGenerator();
    	System.out.println(packageStr);
    	
//    	String configFileName = "cspi/properties/1.properties";
//    	Map<String, Object> map = PropertiesUtils.getProperties(configFileName);
//    	System.out.println(map);    	
    }
}
