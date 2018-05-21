package com.dinpay.dpp.csp.instruction.service.pay.validate.impl;

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.dinpay.dpp.csp.instruction.comm.util.cspay.CSPayProperties;
import com.dinpay.dpp.csp.instruction.comm.util.cspay.CSPayScanProperties;
import com.dinpay.dpp.csp.instruction.comm.util.cspay.CSPayUtil;
import com.dinpay.dpp.csp.instruction.dao.IOrderInstructionDAO;
import com.dinpay.dpp.csp.instruction.pojo.BankRetValidateResult;
import com.dinpay.dpp.csp.instruction.service.pay.validate.SuperValidateService;
import com.dinpay.dpp.domain.order.Order;
import com.dinpay.dpp.domain.order.enums.OrderStatus;
import com.dinpay.dpp.domain.system.enums.PayChannelCode;
import com.nbtv.commons.factory.SupportCodes;
import com.nbtv.orm.dao.IGenericDao;

import net.sf.json.JSONObject;

/**
 * CSPAY-扫码支付-验签
 * @ClassName CSPaymentResultValidateImpl
 * @author 曹治平
 * @date 2018年04月12日 下午5:25:41
 */
@SupportCodes({"CS_JDSCAN","CS_JDH5","CS_AliSCAN","CS_JDSCAN_H5","CS_JDSCAN_H5_2","CS_UNIONSCAN","CS_B2C"})
@Service
public class CSPaymentResultValidateImpl extends SuperValidateService<BankRetValidateResult, Order> {

	private static final Log log = LogFactory.getLog(CSPaymentResultValidateImpl.class);
	
	@Override
	protected Log getLoger() {
		return log;
	}
	
	@Resource(name="baseDaoOrder")
	@Override
	public void setOrderDao(IGenericDao orderDao) {
		super.setOrderDao(orderDao);
	}

	@Resource(name="baseDaoSys")
	@Override
	public void setSysDao(IGenericDao sysDao) {
		super.setSysDao(sysDao);
	}
	
	@Resource(name="orderInstructionDAO")
	private IOrderInstructionDAO orderInstructionDAO;

	@SuppressWarnings({ "rawtypes", "unchecked", })
	@Override
	protected BankRetValidateResult validateBankMsg(Map paramsMap) {
		
		BankRetValidateResult brvr = new BankRetValidateResult();
		
		try {
			
			log.info("CSPaymentResultValidateImpl validate data:" + paramsMap);
			
			setUpAllowChannelCodes(PayChannelCode.CS_JDH5.getCode(),PayChannelCode.CS_JDSCAN_H5.getCode(),PayChannelCode.CS_JDSCAN_H5_2.getCode(),
								   PayChannelCode.CS_UNIONSCAN.getCode(),PayChannelCode.CS_B2C.getCode());
			
			String channelCode = String.valueOf(paramsMap.get("channelCode"));
			
			JSONObject params = JSONObject.fromObject(paramsMap.get(BANK_DATA_KEY));
			if(params.isEmpty()){
				log.info("CSPaymentResultValidateImpl : parameter object is empty!");
				return brvr;
			}
			log.info("CSPaymentResultValidateImpl response params : " + params);
			
			//订单号
			String orderId = params.getString("out_trade_no");
			
			//商家号
			String MERCHANT_ID = "";
			if("CS_B2C".equals(channelCode)){
				MERCHANT_ID = CSPayProperties.getPorpertiesValueByName(channelCode, "MERCHANT_ID");
			}else{
				MERCHANT_ID = orderInstructionDAO.getRemarkByOrderId(orderId);
			}
					
			//判断商户号是否为空
			if(StringUtils.isEmpty(MERCHANT_ID)){
				log.warn("CSPaymentResultValidateImpl validateBankMsg mercNo is empty! orderId:" + orderId);
				return brvr;
			}
			
			//密钥
			String KEY = "";
			if("CS_B2C".equals(channelCode)){
				KEY = CSPayProperties.getPorpertiesValueByName(channelCode, "KEY");
			}else{
				KEY = CSPayScanProperties.getPropertiesKeyByMerchantNo(channelCode,MERCHANT_ID);
			}
			
			//判断秘钥是否为空
			if(StringUtils.isEmpty(KEY)){
				log.warn("CSPaymentResultValidateImpl validateBankMsg md5key is empty! mercNo :" + MERCHANT_ID);
				return brvr;
			}
			
			//验签
			String returnSign = String.valueOf(params.remove("sign"));
			if(!CSPayUtil.verify(returnSign,params, KEY)){
				log.info("CSPaymentResultValidateImpl: signature verification failed!");
				return brvr;
			}
			//验签通过
			brvr.setValidated(true);
			
			//检验交易状态:100
			String status = params.getString("tradeStatus");
			if(!"TRADE_SUCCESS".equals(status)){
				log.info(new StringBuilder("CSPaymentResultValidateImpl request failed!")
						.append(", status:").append(status));
				return brvr;
			}
			
			//金额
			String amount = params.getString("total_fee");
			//商户ID
			String mch_id = params.getString("mch_id");
			
			if("CS_B2C".equals(channelCode)){
				orderId = orderId.replaceAll("^[0]+", "");
			}

			setUpDefaultBankMerchantIds(MERCHANT_ID);
			brvr.setBankMerchantId(mch_id);
			brvr.setAmount(new BigDecimal(amount).divide(new BigDecimal("100")));
			brvr.setOrderId(orderId);
			brvr.setChannelOrderId(null);
			brvr.setOrderStatus(OrderStatus.OrderStatus_PAY_SUCCESS);
			setUnValidator(PAY_CURRENCY);
		} catch (Exception e) {
			log.error("csp.CSPaymentResultValidateImpl.exception 异常：", e);
		}
		return brvr;
		
	}
	
}
