package com.dinpay.dpp.csp.transfer.services.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dinpay.dpp.csp.api.response.ServiceResponse;
import com.dinpay.dpp.csp.api.response.TransferResultResponse;
import com.dinpay.dpp.csp.transfer.commons.cspay.CSPayProperties;
import com.dinpay.dpp.csp.transfer.commons.cspay.CSPayUtil;
import com.dinpay.dpp.csp.transfer.commons.mm.MMVerifyUtils;
import com.dinpay.dpp.csp.transfer.commons.utils.InstructionUtil;
import com.dinpay.dpp.csp.transfer.services.ITransferServices;
import com.dinpay.dpp.domain.member.business.Transfer;
import com.dinpay.dpp.domain.member.config.MemberSettleconfig;
import com.dinpay.dpp.domain.member.enums.TransferOperateStatus;
import com.nbtv.commons.factory.SupportCodes;
import com.nbtv.commons.http.HttpClientUtils;

/**
 * CSPAY代付接口
 * @ClassName TransferResultResponse
 * @author 曹治平
 * @date 2018年04月17日 下午3:12:29
 */
@SupportCodes({"CSPAY1","CSPAY2","CSPAY3"})
@Component
public class CSPayTransferServiceImpl implements ITransferServices {
	private final static Logger log = LoggerFactory.getLogger(CSPayTransferServiceImpl.class);

	@Override
	public TransferResultResponse transfer(Transfer transfer) {
		
		//代付通道code
		String channelCode = transfer.getPayBankCode();
		//获取转账ID
		String transferId = transfer.getId();
		String preLogMsg = "TransferResultResponse transfer[" + transferId + "] ";
		//默认代付状态
		TransferResultResponse response = new TransferResultResponse();
		response.setRetCode(ServiceResponse.RETCODE_SUCCESS);
		response.setResult(TransferOperateStatus.TRANSFER_DOING.getStatus());
		response.setRetMsg(TransferOperateStatus.TRANSFER_DOING.getTitle());
		response.setBankRetInfo(TransferOperateStatus.TRANSFER_DOING.getTitle());
		
		try {
											
			//商户号
			String mch_id = CSPayProperties.getPorpertiesValueByName(channelCode, "MERCHANT_ID");
			
			//代付金额(单位为分)
			BigDecimal trans_money_yuan = transfer.getTransferMoney();
			String trans_money = String.valueOf(trans_money_yuan.multiply(new BigDecimal("100")).intValue());
								
			//交易类型：垫资代付：XFZF_DF_DZ ， 余额代付：XFZF_DF_NO
			String service = "XFZF_DF_NO";	
			if("CSPAY3".equals(channelCode)){
				service = "TS_WAP_DF";
			}
			
			//商户交易单号
			String out_trade_no = transfer.getBankTradeId();
			
			//户名
			String account_name = transfer.getReceiveAccountName();
			
			//银行卡号
			String bank_card = transfer.getReceiveAccount();
			
			//支行名称
			String bank_name = transfer.getBankSubbranch();
			
			//联行号
			String bank_linked = transfer.getUniteBankId();						
			
			//MD5签名key
			String key = CSPayProperties.getPorpertiesValueByName(channelCode, "KEY");
			//代付地址
			String REQUEST_URL = CSPayProperties.getPorpertiesValueByName(channelCode, "TRANSFER_URL");
					
			//请求参数
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("mch_id", mch_id);
			params.put("trans_money", trans_money);
			params.put("service", service);
			params.put("out_trade_no", out_trade_no);
			params.put("account_name", account_name);
			params.put("bank_card", bank_card);
			params.put("bank_name", bank_name);
			params.put("bank_linked", bank_linked);									
			
			//签名
			String origStr = InstructionUtil.getParamsStr(params, key);
			String sign = DigestUtils.md5Hex(origStr.getBytes("UTF-8")).toUpperCase();			
			params.put("sign", sign);								
			
			log.info(new StringBuilder(preLogMsg).append("request data:").append(params).toString());
				
			
			//post请求
		    String result = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, params,"UTF-8");
		    if(StringUtils.isBlank(result)){
				log.info(new StringBuilder(preLogMsg).append("response result is null!").toString());
				response.setRetMsg("请求银行异常");
				return response;
			}
		    log.info(new StringBuilder(preLogMsg).append("response result:").append(result).toString());
		    
		    //json转map
			TreeMap<String, Object> returnMap = JSON.parseObject(result,new TypeReference<TreeMap<String, Object>>(){} );
				
			//判断状态：SUCCESS
		    Object ret_code = returnMap.get("ret_code");
		    String ret_message = String.valueOf(returnMap.get("ret_message"));
		    if(null != ret_code && !"SUCCESS".equals(ret_code)){
		    	StringBuilder msg = new StringBuilder(preLogMsg);
				msg.append("response result retCode:").append(ret_code);
				msg.append("; message:").append(ret_message);
				msg.append("; out_trade_no:").append(out_trade_no);
				log.info(msg.toString());
				response.setRetMsg(ret_message);
				return response;
		    }
		    
			//验签
		    String retSign = String.valueOf(returnMap.remove("sign"));
		    if(!CSPayUtil.verify(retSign, returnMap, key)){
		    	StringBuilder msg = new StringBuilder(preLogMsg);
		    	msg.append("signature verification failed! ");
		    	msg.append("traceno:").append(out_trade_no);
		    	log.info(msg.toString());
				response.setRetMsg("verification failed");
				return response;
		    }
		    
		    //校验交易状态
		    String tradeStatus = String.valueOf(returnMap.get("tradeStatus"));
		    if(!("3".equals(tradeStatus))){
		    	StringBuilder msg = new StringBuilder(preLogMsg);
		    	msg.append("not success! ");
		    	msg.append("transStatus:").append(tradeStatus);
		    	msg.append("; out_trade_no:").append(out_trade_no);
		    	log.info(msg.toString());
		    	response.setRetMsg("apply not success!");
		    	return response;
		    }
		    
			//校验订单号
		    String retOrderId = String.valueOf(returnMap.get("orderNo"));
			if(!out_trade_no.equals(retOrderId)){
				StringBuilder msg = new StringBuilder(preLogMsg);
				msg.append("return bankTradeId is not match. ");
				msg.append("traceno:").append(out_trade_no);
				msg.append("; returnTraceno:").append(retOrderId);
				log.info(msg.toString());
				return response;
			}
			
			log.info("TransferResultResponse transfer[" + transferId + "] doing! traceno:" + out_trade_no);
			
			response.setBankRetInfo(ret_message);
			response.setBankTradeId(retOrderId);
			
		} catch (Exception e) {
			log.error("TransferResultResponse transfer[" + transferId + "] throw exception:", e);
			response.setRetCode(ServiceResponse.RETCODE_SYSERR);
			response.setResult(TransferOperateStatus.TRANSFER_EXCEPTION.getStatus());
			response.setBankRetInfo(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle()+ e.getMessage());
		}
		return response;
	}

	@Override
	public TransferResultResponse queryTransferResult(Transfer transfer) {
		log.info("---CSPAY query transfer result start, transferId=" + transfer.getId());
		
		TransferResultResponse response = new TransferResultResponse();
		response.setRetCode(ServiceResponse.RETCODE_SUCCESS);
		response.setResult(TransferOperateStatus.TRANSFER_DOING.getStatus());
		response.setRetMsg(TransferOperateStatus.TRANSFER_DOING.getTitle());
		response.setBankRetInfo(TransferOperateStatus.TRANSFER_DOING.getTitle());
		
		try {
			//通道CODE
			String channelCode = transfer.getPayBankCode();
			//虚拟账号
			String MERCHANT_ID = CSPayProperties.getPorpertiesValueByName(channelCode, "MERCHANT_ID");
			//MD5签名key
			String key = CSPayProperties.getPorpertiesValueByName(channelCode, "KEY");
			//代付查询地址
			String REQUEST_URL = CSPayProperties.getPorpertiesValueByName(channelCode, "QUERY_URL");
			//订单号
			String tranceno = transfer.getBankTradeId(); 
			
			//金额
			String transferMoney = String.valueOf(transfer.getTransferMoney().multiply(new BigDecimal("100")).intValue());
			
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("mch_id", MERCHANT_ID);
			dataMap.put("out_trade_no", tranceno);
			
			String origStr = InstructionUtil.getParamsStr(dataMap, key);
			String sign = DigestUtils.md5Hex(origStr.getBytes("UTF-8")).toUpperCase();	
			
			dataMap.put("sign", sign);
			
			log.info("TransferResultResponse quertyTransferResult request data:" + dataMap.toString());
			
			//post请求
		    String result = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, dataMap,"UTF-8");
		    
		    if(StringUtils.isBlank(result)){
				log.info("TransferResultResponse queryTransferResult response result is null!" );
				response.setRetMsg("request bank exception!");
				return response;
			}
		    
		    log.info("TransferResultResponse queryTransferResult response result:" + result);
		    
		    //json转map
			TreeMap<String, Object> returnMap = JSON.parseObject(result,new TypeReference<TreeMap<String, Object>>(){} );
			String ret_code = String.valueOf(returnMap.get("ret_code"));
			String ret_message = String.valueOf(returnMap.get("ret_message"));
			//校验状态:SUCCESS
			if(!"SUCCESS".equals(ret_code)){
				log.info("TransferResultResponse transfer result resCodeRe:" + ret_code 
						+ ";msg:" + ret_message+",bankTradeId:"+tranceno);
				response.setRetMsg(ret_message);
				return response;
			}
			
			//验签
			String retSign = String.valueOf(returnMap.remove("sign"));
			if(!CSPayUtil.verify(retSign, returnMap, key)){
				log.info("TransferResultResponse transfer signature verification failed! bankTradeId:"
						+ tranceno);
				response.setRetMsg("verification failed");
				return response;
			}
			
			//交易状态(1-处理中，2-失败，3-成功)
		    String payStatus = String.valueOf(returnMap.get("payStatus"));
		    if(!"3".equals(payStatus)){
		    	log.info(new StringBuilder("TransferResultResponse: transfer not success status:").append(payStatus)
		    			.append(" ; bankTradeId :").append(tranceno).toString());
		    	response.setRetMsg(ret_message);
		    	return response;
		    }
			
		    //校验bankTradeId
			String retOrderId = String.valueOf(returnMap.get("orderNo"));
		    if(!tranceno.equals(retOrderId)){
		    	log.info(new StringBuilder("TransferResultResponse: return bankTradeId is not match. orderNo:")
		    	.append(retOrderId).append("; bankTradeId:").append(tranceno).toString());
		    	response.setRetMsg("bankTradeId is not match!");
		    	return response;
		    }
		    
		    //校验商户号
			String retMerchantId = String.valueOf(returnMap.get("mch_id"));
		    if(!MERCHANT_ID.equals(retMerchantId)){
		    	log.info(new StringBuilder("TransferResultResponse: return merchantId is not match. merchantId:")
		    	.append(MERCHANT_ID).append("; bankMerchantId:").append(retMerchantId).toString());
		    	response.setRetMsg("merchantId is not match!");
		    	return response;
		    }
		    
		    //校验金额
		    String  resFee = (String) returnMap.get("total_fee");
		    BigDecimal total_fee = new BigDecimal(resFee);		    
		    if (new BigDecimal(transferMoney).compareTo(total_fee) != 0) {
				log.info(new StringBuilder("TransferResultResponse queryTransferResult amount mismatch! total_fee:")
				.append(total_fee).append("transferMoney:").append(transferMoney).toString());				
				response.setRetMsg("amount mismatch!");
				return response;
			}
		    		    
		    log.info("CSPAYTransfer success! bankTradeId:" + tranceno);
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
	public Map<String, TransferResultResponse> batchQueryResult(Date startTime,Date endTime) {
		return null;
	}

	@Override
	public TransferResultResponse getBalance() {
		log.info("MMPAY query balance move to ext service!");
		return null;
	}

	@Override
	public int verityAccountNo(MemberSettleconfig config) {
		return 0;
	}

	@Override
	public TransferResultResponse queryVerityResult(String reqSeqNo,String dealDate) {
		return null;
	}

}
