package org.czp.sample.ippb;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dinpay.dpp.csp.api.response.ServiceResponse;
import com.dinpay.ipp.bankGateway.bankback.constant.ErrorCodeEnumForBankReturn;
import com.dinpay.ipp.bankGateway.bankback.service.BankReturnService;
import com.dinpay.ipp.bankGateway.bankback.util.Util;
import com.nbtv.commons.ip.IPUtils;

/**
 * CSPay-扫码支付-后台通知
 * @ClassName BankCSPayScanReturn
 * @author 曹治平
 * @date 2018年04月11日 下午5:04:05
 */
public class BankCSPayScanReturn extends HttpServlet{
	
	private static final long serialVersionUID = 7255115523489555119L;
	private static Log log = LogFactory.getLog(BankCSPayScanReturn.class);
	private static final String Bank_Data_Key = "bank_data";
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		doPost(request, response);
	}
	
	@SuppressWarnings("all")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{
		PrintWriter pw = null;
		try{
			String servletPath = request.getServletPath();//请求地址
			
			log.info(Util.getRequestParamts(request));
			String referer = request.getHeader("Referer");

			String url = request.getRequestURI();
			log.info("request URL:" + url);
			String[] rr = url.split("/");

			String channelId = rr[(rr.length - 1)];
			
			//接收参数
			Map<String,String> params = Util.getParamsMap(request);
			log.info("BankCSPayScanReturn request params:" + params);		
			
			String bankIp = IPUtils.getClientIP(request);
			log.info(new StringBuilder("referer:").append(referer).append("|channelId:").append(channelId).append("|BankCSPayScanReturn Back Notify=").append(params).append("; IP=").append(bankIp));			
			
			
			// 页面通知 
			if (servletPath.endsWith("/CSScanPage")) {				
				
				request.getRequestDispatcher("/pay_succeed.jsp").forward(request, response);
				log.info("BankCSPayScanReturn Handle Success");
				return;
				
			//后台通知
			}else if(servletPath.endsWith("/CSScanPayed")){	
				
				if (params.isEmpty()){
					log.info(ErrorCodeEnumForBankReturn.BANK_RETURN_MESSAGE__IS_NULL.getTitle());
					request.setAttribute("errorDis", ErrorCodeEnumForBankReturn.BANK_RETURN_MESSAGE__IS_NULL.getTitle());
					request.getRequestDispatcher("/returnWrong.jsp").forward(request, response);
					return;
				}
				
				Map<String, Object> requestMap = new HashMap<String,Object>();
				requestMap.put(Bank_Data_Key, params);
					
				Map<String, Object> returnMap = BankReturnService.pageNotifyBussinessHandler(requestMap, channelId);
				String errorCode = (String)returnMap.get("errorCode");
				String paying = (String)returnMap.get("paying");
				if ((StringUtils.isNotEmpty(errorCode)) || (StringUtils.isNotEmpty(paying))){
					log.info(errorCode);
					return;
				}
				pw = response.getWriter();
				pw.write("success");
				pw.flush();
				log.info("BankCSPayScanReturn Handle Success");
			}			
		}
		catch (Exception e){
			log.error(e.getMessage(), e);
			request.setAttribute("errorDis", ServiceResponse.RETCODE_SYSERR);
			try{
				request.getRequestDispatcher("/returnWrong.jsp").forward(request, response);
			}catch (Exception e1) {}
		}finally {
			if(null != pw){
				pw.close();
			}
		}
	}
}
