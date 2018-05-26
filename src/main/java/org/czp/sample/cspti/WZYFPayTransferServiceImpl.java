package com.dinpay.dpp.csp.transfer.services.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.StringEntity;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.dinpay.dpp.csp.api.response.ServiceResponse;
import com.dinpay.dpp.csp.api.response.TransferResultResponse;
import com.dinpay.dpp.csp.transfer.commons.utils.InstructionUtil;
import com.dinpay.dpp.csp.transfer.commons.wzyf.WZYFPayProperties;
import com.dinpay.dpp.csp.transfer.commons.wzyf.WZYFUtil;
import com.dinpay.dpp.csp.transfer.dao.ITransferDAO;
import com.dinpay.dpp.csp.transfer.services.ITransferServices;
import com.dinpay.dpp.domain.member.business.Transfer;
import com.dinpay.dpp.domain.member.config.MemberSettleconfig;
import com.dinpay.dpp.domain.member.enums.TransferOperateStatus;
import com.nbtv.commons.factory.SupportCodes;
import com.nbtv.commons.http.HttpClientUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName
 * @author 曹治平
 * @date 2018-05-23 10:08:27
 * 
 */
@SupportCodes({ "WZYFPAY1"})
@Component
public class WZYFPayTransferServiceImpl implements ITransferServices {

	private final static Logger log = LoggerFactory.getLogger(CSPayTransferServiceImpl.class);
	
	@Resource(name="transferDao")
	private ITransferDAO transferDao;
	
	@SuppressWarnings("unchecked")
	@Override
	public TransferResultResponse transfer(Transfer transfer) {

		// 代付通道code
		String channelCode = transfer.getPayBankCode();
		// 获取转账ID
		String transferId = transfer.getId();
		String preLogMsg = "TransferResultResponse transfer[" + transferId + "] ";
		// 默认代付状态
		TransferResultResponse response = new TransferResultResponse();
		response.setRetCode(ServiceResponse.RETCODE_SUCCESS);
		response.setResult(TransferOperateStatus.TRANSFER_DOING.getStatus());
		response.setRetMsg(TransferOperateStatus.TRANSFER_DOING.getTitle());
		response.setBankRetInfo(TransferOperateStatus.TRANSFER_DOING.getTitle());

		try {

			// 代付金额(单位为分)
			BigDecimal trans_money_yuan = transfer.getTransferMoney();
			String trans_money = String.valueOf(trans_money_yuan.multiply(new BigDecimal("100")).intValue());
			// 商户交易单号
			String trade_no = transfer.getBankTradeId();
			// 户名
			String account_name = transfer.getReceiveAccountName();
			// 银行卡号
			String bank_card = transfer.getReceiveAccount();
			// 联行号
			String bank_linked = transfer.getUniteBankId();
			// 银行名称
			String bankName = transfer.getBankName();
			// 银行所属城市
			String cityNameStr = transfer.getCityName();			
			String cityName = cityNameStr.replace("市", "");
						
			// 支行名称
			String bankSubbranch = transfer.getBankSubbranch();

			// 商户号
			String mch_id = WZYFPayProperties.getPorpertiesValueByName(channelCode, "APPID");
			// MD5签名key
			String key = WZYFPayProperties.getPorpertiesValueByName(channelCode, "KEY");
			// 代付地址
			String TRANSFER_URL = WZYFPayProperties.getPorpertiesValueByName(channelCode, "TRANSFER_URL");

			// 请求时间戳
			Long date = System.currentTimeMillis() / 1000L;
			String timestamp = date.toString();
			// 请求随机数
			String nonce = InstructionUtil.getSeqNo();
			// 订单描述
			String body = "订单编号-" + trade_no;			
			
			Map<String, Object> params = new HashMap<String, Object>();
			// 身份证号(乱写)
			params.put("quickCerdId", "350881199222111111");
			// 商户请求流水号
			params.put("orderNo", trade_no);
			// 支行联行号
			params.put("payeeCardUnionNo", bank_linked);
			// 商品描述
			params.put("body", body);
			// 请求随机数
			params.put("nonce", nonce);
			// 交易金额
			params.put("totalAmount", trans_money);
			// 姓名
			params.put("payeeName", account_name);
			// 银行名称
			params.put("payeeBankName", bankName);
			// 银行所属城市
			params.put("cityName", cityName);
			// 商户请求服务
			params.put("service", "withdrawals.normal");
			// 手机号(乱写)
			params.put("quickPhoneNumber", "18310002020");
			// 商户应用标识
			params.put("appId", mch_id);
			// 客户端IP
			params.put("clientIp", "14.21.44.83");
			// 回调地址
			params.put("notifyUrl", "http://xxx");
			// 支付完成后跳转地址
			params.put("callbackUrl", "http://xxx");
			// 收款人账号
			params.put("payeeCardNo", bank_card);
			// 支行名称
			params.put("payeeBranchBankName", bankSubbranch);
			// 请求时间戳
			params.put("timestamp", timestamp);
			// 账户类型 1 对私帐号 2 对公帐号
			params.put("payeeAccType", "1");

			// 签名
//			String sign = WZYFUtil.getSign(params, key);
			
			String sign = InstructionUtil.signWithMD5(params,key).toUpperCase();
			
			if (StringUtils.isEmpty(sign.trim())) {
				log.info("csp.instructionservice.WZYFPayTransferServiceImpl.sign failed");
				response.setRetCode(ServiceResponse.RETCODE_SYSERR);
				response.setRetMsg("签名失败！");
				return response;
			}
			params.put("sign", sign);

			log.info("WZYFPayTransferServiceImpl request params : " + params);

			JSONObject json = new JSONObject(params);
			StringEntity entity = new StringEntity(json.toString(), "UTF-8");
			entity.setContentEncoding("UTF-8");
			
			Map<String, String> reqHeads = new HashMap<String, String>();
			reqHeads.put("Content-type", "application/json;charset=UTF-8");

			String result = HttpClientUtils.getInstance().sendPostRequest(TRANSFER_URL, entity, reqHeads, "UTF-8");

			if (StringUtils.isBlank(result)) {
				log.info(new StringBuilder(preLogMsg).append("response result is null!").toString());
				response.setRetMsg("请求银行异常");
				return response;
			}
			log.info(new StringBuilder(preLogMsg).append("response result:").append(result).toString());

			// json转map
//			TreeMap<String, Object> returnMap = JSON.parseObject(result, new TypeReference<TreeMap<String, Object>>() {
//			});
						
			TreeMap<String, Object> returnMap = JSON.parseObject(result,new TypeReference<TreeMap<String, Object>>(){} );
						
			// 判断请求状态
			String ret_code = String.valueOf(returnMap.get("success"));
//			String ret_message = String.valueOf(returnMap.get("errorMsg"));			
			
			if (null != ret_code && !"true".equals(ret_code)) {
				StringBuilder msg = new StringBuilder(preLogMsg);
				msg.append("response result retCode:").append(ret_code);				
				log.info(msg.toString());
				response.setRetMsg("apply not success!");
				return response;
			}
						
			//保存平台订单号
			Transfer tf= new Transfer();
			tf.setId(transfer.getId());
			String platformNo = (String) returnMap.get("platformOrderNo");
			tf.setBankTradeId(platformNo);
			transferDao.updateTransferResponseInfo(tf);			

			// 验签
			String retSign = String.valueOf(returnMap.remove("sign"));
			if (!WZYFUtil.verify(retSign, returnMap, key)) {
				StringBuilder msg = new StringBuilder(preLogMsg);
				msg.append("signature verification failed! ");
				msg.append("trade_no:").append(trade_no);
				log.info(msg.toString());
				response.setRetMsg("verification failed");
				return response;
			}

			// 校验交易状态 业务结果：业务结果：0（成功）、1（失败）、2（处理中）
			String tradeStatus = String.valueOf(returnMap.get("status"));
			if (!("0 ".equals(tradeStatus))) {
				StringBuilder msg = new StringBuilder(preLogMsg);
				msg.append("not success! ");
				msg.append("transStatus:").append(tradeStatus);
				msg.append("trade_no:").append(trade_no);
				log.info(msg.toString());
				response.setRetMsg("apply not success!");
				return response;
			}

			log.info("TransferResultResponse transfer[" + transferId + "] doing! traceno:" + trade_no);
			
			response.setBankTradeId(trade_no);

		} catch (Exception e) {
			log.error("TransferResultResponse transfer[" + transferId + "] throw exception:", e);
			response.setRetCode(ServiceResponse.RETCODE_SYSERR);
			response.setResult(TransferOperateStatus.TRANSFER_EXCEPTION.getStatus());
			response.setBankRetInfo(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle() + e.getMessage());
		}
		return response;
	}

	@Override
	public TransferResultResponse queryTransferResult(Transfer transfer) {
		log.info("---WZYFPayTransferServiceImpl  query transfer result start, transferId=" + transfer.getId());
		TransferResultResponse response = new TransferResultResponse();
		response.setRetCode(ServiceResponse.RETCODE_SUCCESS);
		response.setResult(TransferOperateStatus.TRANSFER_DOING.getStatus());
		response.setRetMsg(TransferOperateStatus.TRANSFER_DOING.getTitle());
		response.setBankRetInfo(TransferOperateStatus.TRANSFER_DOING.getTitle());

		try {

			// 通道CODE
			String channelCode = transfer.getPayBankCode();
			// 订单号
			String tranceno = transfer.getBankTradeId();
			// 金额
			String transferMoney = String
					.valueOf(transfer.getTransferMoney().multiply(new BigDecimal("100")).intValue());
			// 虚拟账号
			String MERCHANT_ID = WZYFPayProperties.getPorpertiesValueByName(channelCode, "APPID");
			// MD5签名key
			String key = WZYFPayProperties.getPorpertiesValueByName(channelCode, "KEY");
			// 代付查询地址
			String REQUEST_URL = WZYFPayProperties.getPorpertiesValueByName(channelCode, "QUERY_URL");
			
			// 请求随机数
			String nonce = InstructionUtil.getSeqNo();
			// 请求时间戳
			Long date = System.currentTimeMillis() / 1000L;
			String timestamp = date.toString();
						
			Map<String, Object> params = new HashMap<String, Object>();
			// 商户应用标识
			params.put("appId", MERCHANT_ID);
			// 平台订单号
			params.put("platformTradeNo", tranceno);
			// 请求随机数
			params.put("nonce", nonce);
			// 请求时间戳
			params.put("timestamp", timestamp);
			
			// 签名
//			String sign = WZYFUtil.getSign(params, key);
			String sign = InstructionUtil.signWithMD5(params,key).toUpperCase();
		
			if (StringUtils.isEmpty(sign.trim())) {
				log.info("csp.instructionservice.WZYFPayTransferServiceImpl.sign failed");
				response.setRetCode(ServiceResponse.RETCODE_SYSERR);
				response.setRetMsg("签名失败！");
				return response;
			}
			params.put("sign", sign);

			log.info("TransferResultResponse quertyTransferResult request data:" + params.toString());
									
			JSONObject json = new JSONObject(params);
			StringEntity entity = new StringEntity(json.toString(), "UTF-8");
			entity.setContentEncoding("UTF-8");

			Map<String, String> reqHeads = new HashMap<String, String>();
			reqHeads.put("Content-type", "application/json;charset=UTF-8");
						
			String result = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, entity, reqHeads, "UTF-8");			

			// 判断请求是否为空
			if (StringUtils.isBlank(result)) {
				log.info("TransferResultResponse queryTransferResult response result is null!");
				response.setRetMsg("request bank exception!");
				return response;
			}

			log.info("TransferResultResponse queryTransferResult response result:" + result);

			// json转map
			TreeMap<String, Object> returnMap = JSON.parseObject(result, new TypeReference<TreeMap<String, Object>>() {
			});

			// 判断请求状态
			String ret_code = String.valueOf(returnMap.get("success"));			
			if (null != ret_code && !"true".equals(ret_code)) {
				String errorMsg = String.valueOf(returnMap.get("errorMsg"));
				String errorCode = String.valueOf(returnMap.get("errorCode"));
				log.info("TransferResultResponse transfer result resCodeRe:" + errorCode + ";msg:" + errorMsg
						+ ",bankTradeId:" + tranceno);
				response.setRetMsg(errorMsg);
				return response;
			}

			// 校验验签
			String retSign = String.valueOf(returnMap.remove("sign"));
			if (!WZYFUtil.verify(retSign, returnMap, key)) {
				log.info("TransferResultResponse transfer signature verification failed! bankTradeId:" + tranceno);
				response.setRetMsg("verification failed");
				return response;
			}

			// 校验交易状态  处理中：0,待审核：1,成功：2,失败：3,
			String tradeStatus = String.valueOf(returnMap.get("tradeState"));
			if (!("2".equals(tradeStatus))) {
				log.info(new StringBuilder("TransferResultResponse: transfer not success status:").append(tradeStatus)
						.append(" ; bankTradeId :").append(tranceno).toString());				
				return response;
			}

			// 校验商户号
			String retMerchantId = String.valueOf(returnMap.get("appId"));
			if (!MERCHANT_ID.equals(retMerchantId)) {
				log.info(new StringBuilder("TransferResultResponse: return merchantId is not match. merchantId:")
						.append(MERCHANT_ID).append("; bankMerchantId:").append(retMerchantId).toString());
				response.setRetMsg("merchantId is not match!");
				return response;
			}
			
		  //校验金额
	      String amounts = String.valueOf(returnMap.get("totalAmount"));  //应答信息
	      
	      BigDecimal transferMoneyVali = new BigDecimal(transfer.getTransferMoney().multiply(new BigDecimal("100")).intValue());  // 转账金额
	      
	      BigDecimal charges = new BigDecimal(transfer.getCharges().multiply(new BigDecimal("100")).intValue());;  // 手续费
	      BigDecimal retAmount = new BigDecimal(amounts);  // 银行返回金额
	      String feeMethod = transfer.getFeeMethod();// 手续费扣除方式；0：从转账金额扣除；1：从余额扣除
	      
	      log.info(new StringBuilder().append("转账金额:").append(transferMoneyVali).append(",手续费:").append(charges).append(",银行返回金额:")
	          .append(amounts).append(",手续费扣除方式:").append(feeMethod).toString());
	      
	      if ("0".equals(feeMethod)) {// 从转账金额扣除手续费
	        retAmount = retAmount.add(charges);  // 银行返回的金额加上手续费，再与转账金额比较
	      }
	      if (transferMoneyVali.compareTo(retAmount) != 0) {
	        response.setBankRetInfo("返回的金额不一致!");
	        response.setRetMsg("返回的金额不一致!");
	        return response;
	      }	      	     
			
			
			log.info("WZYFPAYTransfer success! bankTradeId:" + tranceno);
			
			response.setBankTradeId(tranceno);
			response.setResult(TransferOperateStatus.TRANSFER_SUCCESS.getStatus());
			response.setRetMsg(TransferOperateStatus.TRANSFER_SUCCESS.getTitle());
			response.setBankRetInfo(TransferOperateStatus.TRANSFER_SUCCESS.getTitle());

		} catch (Exception e) {
			log.error("TransferResultResponse queryTransferResult transfer result throw exception:", e);
			response.setResult(ServiceResponse.RETCODE_SYSERR);
			response.setRetMsg(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle());
			response.setBankRetInfo(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle());
			return response;
		}
		return response;
	}

	@Override
	public Map<String, TransferResultResponse> batchQueryResult(Date startTime, Date endTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransferResultResponse getBalance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int verityAccountNo(MemberSettleconfig config) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TransferResultResponse queryVerityResult(String reqSeqNo, String dealDate) {
		// TODO Auto-generated method stub
		return null;
	}

}
