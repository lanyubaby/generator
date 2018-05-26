package com.dinpay.dpp.csp.queryBank.service.query.query.impl;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.dinpay.dpp.csp.api.response.CheckOrderStatusResponse;
import com.dinpay.dpp.csp.queryBank.comm.util.cspay.CSPayProperties;
import com.dinpay.dpp.csp.queryBank.comm.util.cspay.CSPayScanProperties;
import com.dinpay.dpp.csp.queryBank.comm.util.cspay.CSPayUtil;
import com.dinpay.dpp.csp.queryBank.dao.IOrderQueryDAO;
import com.dinpay.dpp.csp.queryBank.service.query.query.IQueryOrderResultService;
import com.dinpay.dpp.domain.order.Order;
import com.dinpay.dpp.domain.order.enums.OrderStatus;
import com.nbtv.commons.factory.SupportCodes;
import com.nbtv.commons.http.HttpClientUtils;

import net.sf.json.JSONObject;

/**
 * 曹治平-支付-订单查询
 * @ClassName CSPayScanQueryOrderResultImpl
 * @author 曹治平
 * @date 2018年04月13日 下午6:17:04
 */
@SupportCodes({"CS_JDSCAN","CS_AliSCAN","CS_JDH5","CS_JDSCAN_H5","CS_JDSCAN_H5_2","CS_UNIONSCAN","CS_B2C","CS_B2C_BAK"})
@Service
public class CSPayScanQueryOrderResultImpl implements IQueryOrderResultService {

	private static final Log log = LogFactory.getLog(CSPayScanQueryOrderResultImpl.class);
	
	@Resource
	private IOrderQueryDAO orderQueryDAO;

	@SuppressWarnings("unchecked")
	@Override
	public CheckOrderStatusResponse queryOrderResult(Order order) {
		//订单号
		String orderId = order.getOrderId();
		//默认未支付
		CheckOrderStatusResponse response = new CheckOrderStatusResponse();
		response.setStatus(OrderStatus.OrderStatus_NOT_PAY.getStatus());
		response.setRetMsg(CheckOrderStatusResponse.ORDER_STATUS_UNPAY);
		response.setOrderId(orderId);

		try {
			String channelCode = order.getChannelCode();
			//请求地址
			String REQUEST_URL = "";
			//商家号
			String MERCHANT_ID = "";
			if("CS_B2C".equals(channelCode) || "CS_B2C_BAK".equals(channelCode)){
				REQUEST_URL = CSPayProperties.getPorpertiesValueByName(channelCode, "REQUEST_URL");
				MERCHANT_ID = CSPayProperties.getPorpertiesValueByName(channelCode, "MERCHANT_ID");
			}else{
				REQUEST_URL = CSPayScanProperties.getPropertiesValueByName("REQUEST_URL");
				MERCHANT_ID = orderQueryDAO.getRemarkByOrderId(orderId);
			}
			//判断商户号是否为空
			if(StringUtils.isEmpty(MERCHANT_ID)){
				log.info("CSPayScanQueryOrderResultImpl queryOrderResult mercNo is empty! orderId:" + orderId);
				response.setRetMsg("mercNo is empty!");
				return response;
			}
			
			//加密密钥
			String KEY = "";
			if("CS_B2C".equals(channelCode) || "CS_B2C_BAK".equals(channelCode)){
				KEY = CSPayProperties.getPorpertiesValueByName(channelCode, "KEY");
			}else{
				KEY = CSPayScanProperties.getPropertiesKeyByMerchantNo(channelCode,MERCHANT_ID);
			}
			
			//判断秘钥是否为空
			if(StringUtils.isEmpty(KEY)){
				log.info("CSPayScanQueryOrderResultImpl queryOrderResult md5key is empty! mercNo :" + MERCHANT_ID);
				response.setRetMsg("md5Key is empty!");
				return response;
			}
			if("CS_B2C".equals(channelCode) || "CS_B2C_BAK".equals(channelCode)){
				//B2C订单号不足10位，需补0
				orderId = StringUtils.leftPad(orderId, 10, "0");
			}
			
			//请求参数
			Map<String, Object> params = new LinkedHashMap<String, Object>();
			params.put("mch_id", MERCHANT_ID);
			params.put("out_trade_no", orderId); 
			//签名
			String sign = CSPayUtil.getSign(params, KEY);
			params.put("sign",sign);
			log.info("CSPayScanQueryOrderResultImpl request query transaction params: " + params);
			
			//发送请求
			String resultStr = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, params, null);
			
			if (StringUtils.isEmpty(resultStr)) {
				log.warn("CSPayScanQueryOrderResultImpl the response of Query OrderResult is null");
				response.setRetMsg("连接异常");
				return response;
			}
			
			JSONObject result = JSONObject.fromObject(resultStr);
			log.info(channelCode + " return info: "+result);
			
			String retCode = String.valueOf(result.get("ret_code"));
			if(!"SUCCESS".equals(retCode)){
				String message = String.valueOf(result.get("ret_message"));
				log.warn(new StringBuilder("CSPayScanQueryOrderResultImpl Query OrderResult failed,orderId:").append(orderId)
						.append(",respCode:").append(retCode).append(", msg:").append(message));
				response.setRetMsg(message);
				return response;
			}

			//验签
			String returnSign = String.valueOf(result.remove("sign"));
			if (!CSPayUtil.verify(returnSign,result, KEY)) {
				log.info("CSPayScanQueryOrderResultImpl signature verification failed! orderId :" + orderId);
				response.setRetMsg("验签失败");
				return response;
			}
			
			//判断业务成功状态：TRADE_SUCCESS：交易成功, TRADE_FAILE：交易失败, WAIT_BUYER_PAY：交易处理中
			String status = result.getString("payStatus");
			if(!"TRADE_SUCCESS".equals(status)){
				String message = "处理中";
				log.warn(new StringBuilder("CSPayScanQueryOrderResultImpl Query OrderResult failed,orderId:").append(orderId)
						.append(",ra_Status:").append(status).append(", msg:").append(message));
				response.setRetMsg(message);
				return response;
			}
			
			//校验金额
			BigDecimal txMoney = order.getTxMoney();
			String returnAmount = result.getString("total_fee");
			if(txMoney.compareTo(new BigDecimal(returnAmount).divide(new BigDecimal("100"))) != 0){
				log.warn(new StringBuilder("CSPayScanQueryOrderResultImpl ")
						.append("orderId=").append(orderId).append(" money mismatch!currAmount=")
						.append(txMoney.doubleValue()).append(",bankAmount=").append(returnAmount).toString());
				response.setRetMsg(CheckOrderStatusResponse.ORDER_STATUS_MISMATCH);
				return response;
			}

			// 订单号校验
			String returnOrderId = result.getString("orderNo");
			if (!orderId.equals(returnOrderId)) {
				log.info(new StringBuilder("CSPayScanQueryOrderResultImpl order_id verification failed! orderId :")
						.append(orderId).append(",bankRetOrderId:").append(returnOrderId));
				response.setRetMsg("订单号不匹配");
				return response;
			}
			
			String returnMerchantId = result.getString("mch_id");
			if(!MERCHANT_ID.equals(returnMerchantId)){
				log.info(new StringBuilder("CSPayScanQueryOrderResultImpl merchant_id mismatch! merchantId :")
						.append(MERCHANT_ID).append(",r1_MerchantNo:").append(returnMerchantId));
				response.setRetMsg("商户号不匹配");
				return response;
			}
			
			// 返回支付成功状态
			response.setChannelOrderId(returnOrderId);
			response.setStatus(OrderStatus.OrderStatus_PAY_SUCCESS.getStatus());
			response.setRetMsg(CheckOrderStatusResponse.ORDER_STATUS_PAY);
		} catch (Exception e) {
			// 程序异常，返回未支付
			log.error("CSPayScanQueryOrderResultImpl query [orderId=" + orderId + "] payment result cause an exception:", e);
			response.setRetMsg(CheckOrderStatusResponse.EXCEPTION);
		}
		return response;
	}
	
}
