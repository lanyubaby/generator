package com.dinpay.dpp.csp.instruction.service.pay.instruction.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.dinpay.dpp.csp.api.response.GetPayCommandResponse;
import com.dinpay.dpp.csp.api.response.ServiceResponse;
import com.dinpay.dpp.csp.instruction.comm.util.cspay.CSPayScanProperties;
import com.dinpay.dpp.csp.instruction.comm.util.cspay.CSPayUtil;
import com.dinpay.dpp.csp.instruction.dao.IOrderInstructionDAO;
import com.dinpay.dpp.csp.instruction.service.pay.instruction.IGetPayInstructionService;
import com.dinpay.dpp.domain.order.Order;
import com.nbtv.commons.factory.SupportCodes;
import com.nbtv.commons.http.HttpClientUtils;

import net.sf.json.JSONObject;

/**
 * CSPAY-京东扫码，京东H5，支付宝扫码
 * @ClassName CSPayScanInstruction
 * @author 曹治平
 * @date 2018年4月11日 下午12:17:20
 */
@SupportCodes({"CS_JDSCAN","CS_JDH5","CS_AliSCAN","CS_JDSCAN_H5","CS_JDSCAN_H5_2","CS_UNIONSCAN"})
@Component
public class CSPayScanInstruction implements IGetPayInstructionService {

	private static final Log log = LogFactory.getLog(CSPayScanInstruction.class);
	
	@Resource(name="orderInstructionDAO")
	private IOrderInstructionDAO orderInstructionDao ;
	
	@SuppressWarnings("unchecked")
	@Override
	public GetPayCommandResponse getPayCommand(Order order) {
		//订单ID
		String orderId = order.getOrderId();
		//默认返回信息
		GetPayCommandResponse response = new GetPayCommandResponse();
		
		response.setRetCode(ServiceResponse.RETCODE_SYSERR);
		response.setRetMsg("csp.instructionservice.CSPayScanInstruction.exception");		
		
		Map<String, Object> forwordMap = new HashMap<String, Object>();
		
		try {
			//获取通道名称
			String channelCode = order.getChannelCode();				
			//请求地址
			String REQUEST_URL = CSPayScanProperties.getPropertiesValueByName("REQUEST_URL");									
			//商户信息
			String MERCHANT_ID = CSPayScanProperties.getMerchantNo(channelCode);	
			//秘钥
			String KEY = CSPayScanProperties.getPropertiesKeyByMerchantNo(channelCode, MERCHANT_ID);
			//回调URL
			String NOTIFY_URL = CSPayScanProperties.getPropertiesValueByName("NOTIFY_URL");
			//交易金额（分）
			BigDecimal txMoney = order.getTxMoney();
			String amount = String.valueOf(txMoney.multiply(new BigDecimal("100")).intValue());			
			//订单描述
			String goodsName = "订单编号-" + orderId;			
			//拼凑通知地址
			String notifyUrl = String.format("%s/%s", NOTIFY_URL,channelCode);
			String return_url = CSPayScanProperties.getPropertiesValueByName("RETURN_URL");	
					
//			业务类型
//			SYB_WEIXIN_SM：微信扫码支付
//			SYB_ALI_SM：支付宝扫码支付
//			XFZF_JD：京东扫码支付
//			XFZF_JD_H5:京东H5支付
//			FFT_UNION_SM 银联扫码
			String service = "";
			if("CS_JDSCAN".equals(channelCode)){
				service = "XFZF_JD";
			}else if("CS_AliSCAN".equals(channelCode)){
				service = "SYB_ALI_SM";
			}else if("CS_JDH5".equals(channelCode)) {
				service = "XFZF_JD_H5";
			}else if("CS_UNIONSCAN".equals(channelCode)) {
				service = "FFT_UNION_SM";
			//扫码场景下的京东H5
			}else if("CS_JDSCAN_H5".equals(channelCode) || "CS_JDSCAN_H5_2".equals(channelCode)) {
				service = "XFZF_JD_H5";
			}
												
			//请求参数
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("body",goodsName);
			params.put("mch_id",MERCHANT_ID);
			params.put("notify_url",notifyUrl);	
			params.put("out_trade_no",orderId);
			params.put("service",service);
			params.put("sign_type","MD5");
			params.put("total_fee",amount);		
			params.put("return_url", return_url);
			
			//签名
			String sign = CSPayUtil.getSign(params, KEY);
			
			if (StringUtils.isEmpty(sign.trim())) {
				log.info("csp.instructionservice.CSPayScanInstruction.sign failed");
				response.setRetCode(ServiceResponse.RETCODE_SYSERR);
				response.setRetMsg("签名失败！");
				return response;
			}
			
			params.put("sign",sign);
			
			log.info("CSPayScanInstruction request params : " + params);

			String respStr = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, params, null);
			
			//判断返回是否为空
			if (StringUtils.isEmpty(respStr)) {
				log.info("CSPayScanInstruction the response payment is null! orderId=" + orderId);
				response.setParams(forwordMap);
				response.setRetCode(ServiceResponse.RETCODE_SYSERR);
				response.setRetMsg("连接异常");
				return response;
			}

			JSONObject result = JSONObject.fromObject(respStr);
			log.info("CSPayScanInstruction return: " + result + "; orderId=" + orderId);			
			
			//判断请求是否成功：SUCCESS
			String respCode = result.getString("ret_code");
			if (!"SUCCESS".equals(respCode)) {
				//请求错误信息
				String message = result.getString("ret_message");
				log.warn("CSPayScanInstruction payment failed! return ret_code:" + respCode + "; ret_message:" + message);
				response.setRetMsg(message);
				return response;
			}		
			
			//验签
			String returnSign = String.valueOf(result.remove("sign"));													
			if (!CSPayUtil.verify(returnSign,result,KEY)) {
				log.warn("CSPayScanInstruction signature verification failed! orderId=" + orderId);
				response.setParams(forwordMap);
				response.setRetCode(ServiceResponse.RETCODE_SYSERR);
				response.setRetMsg("验名失败");
				return response;
			}
			
			//商户号校验
			String merchantId = result.getString("mch_id");
			if(!StringUtils.equals(MERCHANT_ID,merchantId)){
				log.warn("CSPayScanInstruction merchantId verification failed! orderId=" + orderId + ", mch_id = " + merchantId);
				response.setParams(forwordMap);
				response.setRetMsg("商户号不匹配");
				return response;
			}
			
			//订单号校验
			String returnOrderId = result.getString("orderNo");
			if (!StringUtils.equals(orderId, returnOrderId)) {
				log.warn("CSPayScanInstruction orderId verification failed! orderId=" + orderId + ", ret_OrderNo=" + returnOrderId);
				response.setParams(forwordMap);
				response.setRetMsg("订单号不匹配");
				return response;
			}
			
			String retAmount = result.getString("total_fee");
			if(new BigDecimal(retAmount).compareTo(txMoney.multiply(new BigDecimal("100"))) != 0){
				log.warn("CSPayScanInstruction amount verification failed! total_fee=" + amount + ", ret_total_fee=" + retAmount);
				response.setParams(forwordMap);
				response.setRetMsg("金额不匹配");
				return response;
			}
			
			//保存轮询的商户号
			int num = orderInstructionDao.updateAttachmentRemarkByOrderId(orderId, MERCHANT_ID);
			if(num != 1){
				log.warn("CSPayScanInstruction update order remark failed! orderId=" + orderId);
				response.setParams(forwordMap);
				response.setRetMsg("更新数据异常");
				return response;
			}

			String qrcode = result.getString("payinfo");
			if (StringUtils.isEmpty(qrcode)) {
				log.warn("CSPayScanInstruction payment get qrcode is null! orderId=" + orderId);
				response.setParams(forwordMap);
				response.setRetMsg("二维码地址为空");
				return response;
			}
			       			
//			forwordMap.put("payURL", qrcode);
//			response.setPostAddress(qrcode);
			
			forwordMap.put("qrcode", qrcode);
			response.setParams(forwordMap);
			response.setRetCode(ServiceResponse.RETCODE_SUCCESS);
			response.setRetMsg("获取二维码成功");
			log.info("CSPayScanInstruction payment get qrcode success! orderId=" + orderId);
			return response;
		} catch (Exception e) {
			log.error("CSPayScanInstruction: " + orderId + ",payment cause an exception:", e);
			response.setRetCode(GetPayCommandResponse.RETCODE_SYSERR);
			response.setRetMsg("系统异常");
		}
		return response;
	}


}