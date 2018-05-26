package com.dinpay.dpp.csp.transfer.services.impl.ext;

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
import com.dinpay.dpp.csp.api.model.TransferBalanceDTO;
import com.dinpay.dpp.csp.api.response.ServiceResponse;
import com.dinpay.dpp.csp.api.response.TransferResultResponse;
import com.dinpay.dpp.csp.transfer.commons.cspay.CSPayProperties;
import com.dinpay.dpp.csp.transfer.commons.cspay.CSPayUtil;
import com.dinpay.dpp.csp.transfer.commons.utils.InstructionUtil;
import com.dinpay.dpp.csp.transfer.services.ITransferServiceExts;
import com.dinpay.dpp.domain.member.enums.TransferOperateStatus;
import com.nbtv.commons.factory.SupportCodes;
import com.nbtv.commons.http.HttpClientUtils;

/**
 * CSPAY代付余额查询
 * @ClassName CSPayTransferExServiceImpl
 * @author 曹治平
 * @date 2018年04月17日 下午3:12:29
 */
@SupportCodes({"CSPAY1","CSPAY2","CSPAY3"})
@Component
public class CSPayTransferExServiceImpl implements ITransferServiceExts {
	private final static Logger log = LoggerFactory.getLogger(CSPayTransferExServiceImpl.class);

	@Override
	public TransferResultResponse getBalance(TransferBalanceDTO dto) {
		String bankCode = dto.getBankcode();
		log.info("start CSPAY getBalance method,request params:" + bankCode);
		TransferResultResponse response = new TransferResultResponse();
		// 初始化获取余额响应返回值
		response.setRetCode(ServiceResponse.RETCODE_SUCCESS);
		response.setResult(TransferOperateStatus.TRANSFER_DOING.getStatus());
		response.setRetMsg(TransferOperateStatus.TRANSFER_DOING.getTitle());
		response.setBankRetInfo(TransferOperateStatus.TRANSFER_DOING.getTitle());	
		
		try {
			//虚拟账号
			String MERCHANT_ID = CSPayProperties.getPorpertiesValueByName(bankCode, "MERCHANT_ID");
			//MD5签名key
			String key = CSPayProperties.getPorpertiesValueByName(bankCode, "KEY");
			//代付地址
			String REQUEST_URL = CSPayProperties.getPorpertiesValueByName(bankCode, "BALANCE_URL");
			
			Map<String,Object> params = new HashMap<String,Object>();
			
			params.put("mch_id", MERCHANT_ID);			
			//随机的，避免签名一致
			params.put("out_trade_no", new Date().getTime() + "");
			
			String origStr = InstructionUtil.getParamsStr(params, key);
			String sign = DigestUtils.md5Hex(origStr.getBytes("UTF-8")).toUpperCase();
			
			params.put("sign", sign);
			
			log.info("CSPayTransferExServiceImpl getBalance request data:" + params.toString());
			
			//发送post请求
			String resultStr = HttpClientUtils.getInstance().sendPostRequest(REQUEST_URL, params,"UTF-8");
			
			if(StringUtils.isBlank(resultStr)){
				log.info("CSPayTransferExServiceImpl getBalance response result is null!" );
				response.setRetMsg("request bank exception!");
				return response;
			}
			
			log.info("CSPayTransferExServiceImpl getBalance response:" + resultStr);
			
			//json转map
			TreeMap<String, Object> returnMap = JSON.parseObject(resultStr,new TypeReference<TreeMap<String, Object>>(){} );
			
			//返回响应状态:SUCCESS/FAIL
			String retCode = String.valueOf(returnMap.get("ret_code"));
			if(!"SUCCESS".equals(retCode)){
				String retMsg = String.valueOf(returnMap.get("ret_message"));
				log.info("CSPayTransferExServiceImpl getBalance fail result msg:" + retMsg);
				response.setRetMsg(retMsg);
				return response;
			}
			
		    //验签
			String retSign = String.valueOf(returnMap.remove("sign"));
			if(!CSPayUtil.verify(retSign, returnMap, key)){
				log.info("CSPayTransferExServiceImpl getBalance signature verification failed! mercNo:" + MERCHANT_ID);
				response.setRetMsg("verification failed");
				return response;
			}
			
			//金额
			DecimalFormat decimalFormat = new DecimalFormat("0.00");			
		   
			//可用余额:单位分
		    String restbalance = String.valueOf(returnMap.get("restbalance"));		   
		    //垫资余额:单位分
		    String cashbalance = String.valueOf(returnMap.get("cashbalance"));
		    
		    response.setBalance(decimalFormat.format(new BigDecimal(restbalance).divide(new BigDecimal("100"))));
			response.setAvailBalance(decimalFormat.format(new BigDecimal(restbalance).divide(new BigDecimal("100"))));		
			response.setFreezeBalance("0.00");
			log.info("CSPAY query balance success!");
			
		} catch (Exception e) {
			log.error("get CSPAY balance exception:", e);
			response.setRetCode(ServiceResponse.RETCODE_SYSERR);
			response.setResult(TransferOperateStatus.TRANSFER_CONN_BANK_EXCEPTION.getStatus());
			response.setRetMsg(TransferOperateStatus.TRANSFER_CONN_BANK_EXCEPTION.getTitle());
			response.setBalance(TransferOperateStatus.TRANSFER_EXCEPTION.getTitle());			
			return response;
		}
		return response;
	}

}
