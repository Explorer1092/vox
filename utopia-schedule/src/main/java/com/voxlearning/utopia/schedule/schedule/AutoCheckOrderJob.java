package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.IOUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.http.client.factory.HttpClientFactory;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.alps.util.CollectionUtils;
import com.voxlearning.alps.util.StringUtils;
import com.voxlearning.utopia.core.helper.XmlConvertUtils;
import com.voxlearning.utopia.payment.PaymentGatewayManager;
import com.voxlearning.utopia.payment.WechatAbstractPaymentGateway;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.payment.gateway.AlipayPaymentGateway;
import com.voxlearning.utopia.payment.gateway.AlipayPaymentGateway_App;
import com.voxlearning.utopia.payment.support.Certification;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.*;
import com.voxlearning.utopia.service.order.consumer.UserOrderCheckAccountClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2017/11/6.
 */
@Named
@ScheduledJobDefinition(
        jobName = "自动对账",
        jobDescription = "自动对账，每晚凌晨2点下载对账文件并且进行对账",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.STAGING, Mode.TEST},
        cronExpression = "0 0 2 * * ?"
)
public class AutoCheckOrderJob  extends ScheduledJobWithJournalSupport {

    private static final String downbillUrl = "https://api.mch.weixin.qq.com/pay/downloadbill";
    private static final String alipayDownServicePage = "account.page.query";
    private static final String alipayDownService = "export_trade_account_report";
    public static String input_charset = "utf-8";
    private static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";

    @Inject  private PaymentGatewayManager paymentGatewayManager;
    @Inject private UserOrderCheckAccountClient userOrderCheckAccountClient;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private EmailServiceClient emailServiceClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql utopiaSqlOrder;
    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("order");
    }

    private static List<String> paymentNames;
    private static List<String> paymentMethod;

    static{
        paymentNames = new LinkedList<>();
        //添加微信和支付宝账号
        paymentNames.add(PaymentConstants.PaymentGatewayName_Wechat_ParentApp);
        paymentNames.add(PaymentConstants.PaymentGatewayName_Wechat_StudentApp);
        paymentNames.add(PaymentConstants.PaymentGatewayName_Wechat_PcNative);
        paymentNames.add(PaymentConstants.PaymentGatewayname_Alipay_StudentApp);
        paymentNames.add(PaymentConstants.PaymentGatewayName_Alipay);
        paymentNames.add(PaymentConstants.PaymentGatewayName_Wechat_Chips);
        paymentNames.add(PaymentConstants.PaymentGatewayName_Wechat_StudyTogether);
        paymentNames.add(PaymentConstants.PaymentGatewayName_Wechat_StudentApp_Junior);

        paymentMethod = new LinkedList<>();
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_ParentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_StudentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_PcNative);
        paymentMethod.add(PaymentConstants.PaymentGatewayname_Alipay_StudentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayname_Alipay_ParentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Alipay);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_Chips);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_Piclisten);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_StudyTogether);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_StudentApp_Junior);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Alipay_Wap_StudentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_H5_StudentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Alipay_Wap_ParentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_H5_ParentApp);
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        //当前时间的前两天的对账文件,此版逻辑先排除一起学的订单，和金额小于5毛的订单
        logger.info("====对账开始====");
        String emailContent = "对账任务执行完成";
        String billDate = DateUtils.dateToString(DateUtils.calculateDateDay(new Date(),-2),"yyyyMMdd");
        String billType = "ALL";
        StringBuffer records = new StringBuffer("");

        try{
            List<UserOrderPaymentHistory> checkPaymentHistoryList = userOrderCheckAccountClient.loadUserOrderPaymentHistoryListBypayDatetime(DateUtils.stringToDate(billDate,"yyyyMMdd"));

            //我方订单微信和支付宝所有记录数
            List<UserOrderPaymentHistory> paidUserPaymentHistoryList = checkPaymentHistoryList.stream()
                    .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                    .filter(o -> o.getOrderId().length() < 25)
                    .filter(o -> paymentMethod.contains(o.getPayMethod()))
                    .collect(Collectors.toList());

            List<UserOrderPaymentHistory> refundcheckPaymentHistoryList = loadUserOrderPaymentHistoryListByupDatetime(DateUtils.stringToDate(billDate,"yyyyMMdd"));
            List<UserOrderPaymentHistory> refundUserPaymentHistoryList = refundcheckPaymentHistoryList.stream()
                    .filter(o -> o.getPaymentStatus() == PaymentStatus.Refund)
                    .filter(o -> o.getOrderId().length() < 25)
                    .filter(o -> paymentMethod.contains(o.getPayMethod()))
                    .collect(Collectors.toList());

            records.append(" \n 我方收款订单记录数：").append(paidUserPaymentHistoryList.size());
            records.append("；\n 我方退款订单记录数：").append(refundUserPaymentHistoryList.size());
            //学贝充值记录
            List<FinanceFlow> financeFlowList =  buildFinanceFlowList(DateUtils.stringToDate(billDate,"yyyyMMdd"),paymentMethod);
            records.append("；\n 我方充值订单记录数：").append(financeFlowList.size());

            //微信的交易记录
            List<WechatExternalOrder> checkWechatOrderList = new ArrayList<>();
            userOrderCheckAccountClient.deleteWechatExternalOrders(DateUtils.stringToDate(billDate,"yyyyMMdd"));
            for(String paymentName : paymentNames){
                if(paymentName.startsWith("wechat")){
                    WechatAbstractPaymentGateway paymentGateway = (WechatAbstractPaymentGateway) paymentGatewayManager.getPaymentGateway(paymentName);
                    List<List<WechatExternalOrder>> result =  getWechatExternalOrderList(paymentGateway,billDate,billType);
                    for(List wechatExternalOrderList : result){
                        userOrderCheckAccountClient.saveWechatExternalOrders(wechatExternalOrderList);
                        checkWechatOrderList.addAll(wechatExternalOrderList);
                    }
                }else{
                    if(PaymentConstants.PaymentGatewayname_Alipay_StudentApp.equals(paymentName)){
                        AlipayPaymentGateway_App alipayPaymentGateway_app = (AlipayPaymentGateway_App) paymentGatewayManager.getPaymentGateway(paymentName);
                        List<WechatExternalOrder> result = getAlipayExternalOrderList(alipayPaymentGateway_app.getPartner(),alipayPaymentGateway_app.getPriKey(),paymentName,billDate);
                        userOrderCheckAccountClient.saveWechatExternalOrders(result);
                        checkWechatOrderList.addAll(result);
                    }else{
                        AlipayPaymentGateway alipayPaymentGateway = (AlipayPaymentGateway) paymentGatewayManager.getPaymentGateway(paymentName);
                        List<WechatExternalOrder> result = getAlipayExternalOrderList2(alipayPaymentGateway.getPartner(),alipayPaymentGateway.getKey(),paymentName,billDate);
                        userOrderCheckAccountClient.saveWechatExternalOrders(result);
                        checkWechatOrderList.addAll(result);
                    }
                }
            }

            checkWechatOrderList = checkWechatOrderList.stream().filter(o -> o.getOutTradeNo().length() < 32).collect(Collectors.toList());
            records.append("；\n 微信和支付宝订单记录数：").append(checkWechatOrderList.size());
            //收款订单
            List<WechatExternalOrder> paidCheckWechatOrderList = checkWechatOrderList.stream().filter(o -> o.getTradeState().equals("SUCCESS")).collect(Collectors.toList());
            records.append("；\n 微信和支付宝收款订单记录数：").append(paidCheckWechatOrderList.size());
            //退款订单
            List<WechatExternalOrder> refundCheckWechatOrderList = checkWechatOrderList.stream().filter(o -> o.getTradeState().equals("REFUND")).collect(Collectors.toList());
            records.append("；\n 微信和支付宝退款订单记录数：").append(refundCheckWechatOrderList.size());

            //进行对账，解决时间差的订单数据 微信数据是 2018-04-29 23:59:59 ,我方的数据是2018-04-30 00:00:00
            //找处差异表中相同的记录，然后删除
            List<CheckOrderDiff> checkOrderDiffList = userOrderCheckAccountClient.loadAllCheckOrderDiff();
            //对收款账务
            Map<String,Object> paidDiffOrderMap = checkPaidOrder(financeFlowList,paidUserPaymentHistoryList,paidCheckWechatOrderList,checkOrderDiffList);
            //对退款账务
            Map<String,Object> refundDiffOrderMap = checkRefundOrder(refundUserPaymentHistoryList,refundCheckWechatOrderList);
            
            List<UserOrderPaymentHistory> paymentHistoryResList = (List<UserOrderPaymentHistory>)paidDiffOrderMap.get("paymentHistoryList");
            List<WechatExternalOrder> wechatExternalOrderResList = (List<WechatExternalOrder>)paidDiffOrderMap.get("wechatExternalOrderList");
            List<FinanceFlow> financeFlowResList = (List<FinanceFlow>) paidDiffOrderMap.get("financeFlowList");
            List<CheckOrderDiff> delCheckOrderDiffs = (List<CheckOrderDiff>) paidDiffOrderMap.get("delCheckOrderDiffs");

            List<UserOrderPaymentHistory> refundPaymentList = (List<UserOrderPaymentHistory>)refundDiffOrderMap.get("refundPaymentList");
            List<WechatExternalOrder> refundExternalList = (List<WechatExternalOrder>)refundDiffOrderMap.get("refundExternalList");

            records.append("；\n 收款对账差异我方订单记录数：").append(paymentHistoryResList.size());
            records.append("；\n 收款对账差异微信和支付宝订单记录数：").append(wechatExternalOrderResList.size());
            records.append("；\n 收款对账差异我方充值记录数：").append(financeFlowResList.size());
            records.append("；\n 收款对账差异需要删除差异记录数：").append(delCheckOrderDiffs.size());
            records.append("；\n 收款对账差异我方订单记录数：").append(refundPaymentList.size());
            records.append("；\n 收款对账差异微信和支付宝订单记录数：").append(refundExternalList.size());

            //删除差异数据
            userOrderCheckAccountClient.deleteCheckOrderDiffs(DateUtils.stringToDate(billDate,"yyyyMMdd"));
            if(CollectionUtils.isNotEmpty(delCheckOrderDiffs)){
                List<String> ids = delCheckOrderDiffs.stream().map(CheckOrderDiff::getId).collect(Collectors.toList());
                userOrderCheckAccountClient.deleteCheckOrderDiffsByIds(ids);
            }

            //将差异结果保存到差异表中
            List<CheckOrderDiff> paidCheckOrderDiffList = buildCheckOrderDiffList(financeFlowResList,paymentHistoryResList,wechatExternalOrderResList);
            List<CheckOrderDiff> refundCheckOrderDiffList = buildCheckOrderDiffList(null,refundPaymentList,refundExternalList);

            List<CheckOrderDiff> checkOrderDiffs = new LinkedList<>();
            checkOrderDiffs.addAll(paidCheckOrderDiffList);
            checkOrderDiffs.addAll(refundCheckOrderDiffList);

            records.append("；\n 收款对账差异记录数：").append(paidCheckOrderDiffList.size());
            records.append("；\n 退款对账差异记录数：").append(refundCheckOrderDiffList.size());

            checkOrderDiffs = checkOrderDiffs.stream().filter(o->o.getPayAmount().compareTo(new BigDecimal(0.5)) >=0).collect(Collectors.toList());
            records.append("；\n 支付金额大于0.5元的对账差异记录数：").append(checkOrderDiffs.size());

            if (CollectionUtils.isNotEmpty(checkOrderDiffs)) {
                userOrderCheckAccountClient.saveCheckOrderDiff(checkOrderDiffs);
            }

        }catch(Exception e){
            logger.error("对账任务出错{}",e);
            emailContent = e.getMessage();
            emailContent += "，错误堆栈信息："+JsonUtils.toJson(e.getStackTrace());
        }
        String subject = "对账任务通知邮件";
        //跑完发送邮件
        emailServiceClient.createPlainEmail()
                .to("yong.liu@17zuoye.com")
                .cc("zhilong.hu@17zuoye.com")
                .subject(subject)
                .body(billDate+","+emailContent+","+records.toString())
                .send();
        logger.info("====对账结束====");
    }

    private List<UserOrderPaymentHistory> loadUserOrderPaymentHistoryListByupDatetime(Date billDate) {
        List<UserOrderPaymentHistory> userOrderPaymentHistoryList = new ArrayList<>();
        Date startDate = DateUtils.getDayStart(billDate);
        Date endDate = DateUtils.getDayEnd(billDate);
        int size = 0;
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            size = 2;
        } else {
            size = 100;
        }
        Map paramsMap = new HashMap<>();
        paramsMap.put("endDate",endDate);
        paramsMap.put("startDate",startDate);

        for(int i=0; i<size; i++){
            String tableName = "VOX_USER_ORDER_PAYMENT_HISTORY_";
            tableName += String.valueOf(i);
            String sql = "SELECT ID,USER_ID,ORDER_ID,PAY_AMOUNT,PAY_DATETIME,PAY_METHOD,OUTER_TRADE_ID,UPDATE_DATETIME FROM "+tableName+" WHERE PAYMENT_STATUS='Refund' AND UPDATE_DATETIME <= :endDate AND UPDATE_DATETIME >= :startDate ";
            List<Map<String, Object>> result = utopiaSqlOrder.withSql(sql).useParams(paramsMap).queryAll();
            for(Map<String,Object> temp : result){
                UserOrderPaymentHistory paymentHistory = new UserOrderPaymentHistory();
                String id = (String) temp.get("ID");
                paymentHistory.setId(id);
                Long userId = (Long) temp.get("USER_ID");
                paymentHistory.setUserId(userId);
                paymentHistory.setPaymentStatus(PaymentStatus.Refund);
                BigDecimal paymentAmount = new BigDecimal(temp.get("PAY_AMOUNT").toString());
                paymentHistory.setPayAmount(paymentAmount);
                Date payDatetime = (Date) temp.get("PAY_DATETIME");
                paymentHistory.setPayDatetime(payDatetime);
                String payMethod = (String) temp.get("PAY_METHOD");
                paymentHistory.setPayMethod(payMethod);
                String orderId = (String) temp.get("ORDER_ID");
                paymentHistory.setOrderId(orderId);
                String outerTradeId = (String) temp.get("OUTER_TRADE_ID");
                paymentHistory.setOuterTradeId(outerTradeId);
                Date updateDatetime = (Date) temp.get("UPDATE_DATETIME");
                paymentHistory.setUpdateDatetime(updateDatetime);
                userOrderPaymentHistoryList.add(paymentHistory);
            }
        }
        return userOrderPaymentHistoryList;
    }

    private Map<String,Object> checkRefundOrder(List<UserOrderPaymentHistory> refundPaymentHistoryList,List<WechatExternalOrder> refundWechatOrderList) {
        Map<String,Object> refundDiffOrderMap = new LinkedHashMap<>();

        Set<String> refundPaymentOutTradeIds1 = refundPaymentHistoryList.stream().map(UserOrderPaymentHistory :: getOuterTradeId ).collect(Collectors.toSet());
        Set<String> refundPaymentOutTradeIds2 = refundPaymentHistoryList.stream().map(UserOrderPaymentHistory :: getOuterTradeId ).collect(Collectors.toSet());

        Set<String> refundWechatOrderTransactionIds1 = refundWechatOrderList.stream().map(WechatExternalOrder::getTransactionId).collect(Collectors.toSet());

        //分别排除相同部分的数据
        refundPaymentOutTradeIds1.removeAll(refundWechatOrderTransactionIds1);
        refundWechatOrderTransactionIds1.removeAll(refundPaymentOutTradeIds2);

        refundDiffOrderMap.put("refundPaymentList",refundPaymentHistoryList.stream().filter(o->refundPaymentOutTradeIds1.contains(o.getOuterTradeId())).collect(Collectors.toList()));
        refundDiffOrderMap.put("refundExternalList",refundWechatOrderList.stream().filter(o->refundWechatOrderTransactionIds1.contains(o.getTransactionId())).collect(Collectors.toList()));
        return refundDiffOrderMap;
    }

    private List<WechatExternalOrder> getAlipayExternalOrderList2(String partner, String key, String paymentName, String billDate) throws Exception{
        List<WechatExternalOrder> alipayExternalOrders = new LinkedList<>();
        String startDate = DateUtils.dateToString(DateUtils.stringToDate(billDate,"yyyyMMdd"),"yyyy-MM-dd")+" 00:00:00";
        String endDate =  DateUtils.dateToString(DateUtils.stringToDate(billDate,"yyyyMMdd"),"yyyy-MM-dd")+" 23:59:59";
        Map<String, String> sParaTemp = new HashMap<String, String>();
        sParaTemp.put("service", alipayDownService);
        sParaTemp.put("partner", "2088101461558595");
        sParaTemp.put("_input_charset", input_charset);
        sParaTemp.put("gmt_create_start", startDate);
        sParaTemp.put("gmt_create_end", endDate);
        Map<String, String> sPara = buildRequestPara(sParaTemp,paymentName,"tio6n1a0hruw9zhn8w83u44dfgk0qoct");
        org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
        // 设置连接超时
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(8000);
        // 设置回应超时
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(30000);
        // 设置等待ConnectionManager释放connection的时间
        httpClient.getParams().setConnectionManagerTimeout(3000);
        String url = ALIPAY_GATEWAY_NEW+"_input_charset="+input_charset;
        HttpMethod method = new PostMethod(url);
        ((PostMethod) method).addParameters(generatNameValuePair(sPara));
        method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; text/html; charset=" + input_charset);
        // 设置Http Header中的User-Agent属性
        method.addRequestHeader("User-Agent", "Mozilla/4.0");
        String resultStr = "";
        try {
            httpClient.executeMethod(method);
            byte[] resultbytes = method.getResponseBody();
            resultStr = new String(resultbytes,input_charset);
            logger.info(resultStr);
            //解析xml
            List<Map<String,String>> dataList = XmlConvertUtils.getMapbyElementName(resultStr,"//csv_data");
            Map<String,String> data = dataList.get(0);
            String aliOrders = data.get("csv_data");
            String[] orderArr=aliOrders.split("\n");
            Map<String,WechatExternalOrder> alipayOrdersMap = new LinkedHashMap<>();
            Map<String,BigDecimal> serviceFeeMap = new LinkedHashMap<>();
            for(int i=1;i<orderArr.length-1;i++){
                String orderStr = orderArr[i];
                String[] order = orderStr.split(",");
                //外部订单号,账户余额（元）,时间,流水号,支付宝交易号,交易对方Email,交易对方,用户编号,收入（元）,支出（元）,交易场所,商品名称,类型,说明
                WechatExternalOrder wechatExternalOrder = new WechatExternalOrder();
                String transCodeMsg = order[12];
                String orderNo = order[0];

                wechatExternalOrder.setTradeDate(DateUtils.stringToDate(order[2],"yyyy年MM月dd日 HH:mm:ss"));
                wechatExternalOrder.setFeeType("CNY");
                wechatExternalOrder.setPaymentOrgan("alipay");
                wechatExternalOrder.setMchId("2088101461558595");
                wechatExternalOrder.setBody(order[11]);
                if("收费".equals(transCodeMsg)){
                    //手续费订单
                    serviceFeeMap.put(orderNo,new BigDecimal(order[9]).abs());
                }else if("在线支付".equals(transCodeMsg)){
                    //支付订单
                    wechatExternalOrder.setTransactionId(order[4]);
                    wechatExternalOrder.setOutTradeNo(orderNo);
                    wechatExternalOrder.setTotalFee(new BigDecimal(order[8]));
                    wechatExternalOrder.setTradeState("SUCCESS");
                    alipayOrdersMap.put(wechatExternalOrder.getTransactionId()+"_在线支付",wechatExternalOrder);
                }else{
                    //退款
                    wechatExternalOrder.setTransactionId(order[4]);
                    wechatExternalOrder.setOutTradeNo(orderNo);
                    wechatExternalOrder.setRefundFee(new BigDecimal(order[9]).abs());
                    wechatExternalOrder.setTotalFee(BigDecimal.ZERO);
                    wechatExternalOrder.setTradeState("REFUND");
                    wechatExternalOrder.setRefundStatus("SUCCESS");
                    alipayOrdersMap.put(wechatExternalOrder.getTransactionId()+"_退款",wechatExternalOrder);
                }
            }

            //手续费记录补齐
            for(String  transactionId : alipayOrdersMap.keySet()){
                WechatExternalOrder wechatExternalOrder = alipayOrdersMap.get(transactionId);
                BigDecimal serviceFee = serviceFeeMap.get(transactionId.substring(0,transactionId.indexOf("_")));
                if(serviceFee == null){
                    serviceFee = BigDecimal.ZERO;
                }
                wechatExternalOrder.setServiceCharge(serviceFee);
            }

            List l = alipayOrdersMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
            alipayExternalOrders.addAll(l);
        } catch (Exception ex) {
            throw ex;
        } finally {
            method.releaseConnection();
        }
        return alipayExternalOrders;
    }

    private List<WechatExternalOrder> getAlipayExternalOrderList(String partner, String key, String paymentName,String billDate)throws Exception {
        boolean hasNextPage = true;
        int pageNo = 1;
        List<WechatExternalOrder> alipayExternalOrders = new LinkedList<>();
        Map<String,BigDecimal> serviceFeeMap = new LinkedHashMap<>();
        while(hasNextPage){
            String startDate = DateUtils.dateToString(DateUtils.stringToDate(billDate,"yyyyMMdd"),"yyyy-MM-dd")+" 00:00:00";
            String endDate =  DateUtils.dateToString(DateUtils.stringToDate(billDate,"yyyyMMdd"),"yyyy-MM-dd")+" 23:59:59";
            Map<String, String> sParaTemp = new HashMap<String, String>();
            sParaTemp.put("service", alipayDownServicePage);
            sParaTemp.put("partner", partner);
            sParaTemp.put("_input_charset", input_charset);
            sParaTemp.put("page_no", ""+pageNo);
            sParaTemp.put("gmt_start_time", startDate);
            sParaTemp.put("gmt_end_time", endDate);
            Map<String, String> sPara = buildRequestPara(sParaTemp,paymentName,key);

            org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
            // 设置连接超时
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(8000);
            // 设置回应超时
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(30000);
            // 设置等待ConnectionManager释放connection的时间
            httpClient.getParams().setConnectionManagerTimeout(3000);
            String url = ALIPAY_GATEWAY_NEW+"_input_charset="+input_charset;
            HttpMethod method = new PostMethod(url);
            ((PostMethod) method).addParameters(generatNameValuePair(sPara));
            method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; text/html; charset=" + input_charset);
            // 设置Http Header中的User-Agent属性
            method.addRequestHeader("User-Agent", "Mozilla/4.0");
            String resultStr = "";
            try {
                httpClient.executeMethod(method);
                byte[] resultbytes = method.getResponseBody();
                resultStr = new String(resultbytes,input_charset);
                //解析xml
                List<Map<String,String>> hastNextPageL = XmlConvertUtils.getMapbyElementName(resultStr,"//has_next_page");
                Map<String,String> hastNextPage = hastNextPageL.get(0);
                String hastNextPageValue = hastNextPage.get("has_next_page");
                if("F".equals(hastNextPageValue)){
                    hasNextPage = false;
                }
                List<Map<String,String>> AccountQueryAccountLogVOList = XmlConvertUtils.getMapbyElementName(resultStr,"//AccountQueryAccountLogVO");
                //将手续费订单并列到支付订单的手续费上
                Map<String,WechatExternalOrder> wechatExternalOrderMap = new LinkedHashMap<>();
                for(Map<String,String> accountQueryAccountLogVo : AccountQueryAccountLogVOList){
                    WechatExternalOrder wechatExternalOrder = new WechatExternalOrder();
                    wechatExternalOrder.setTransactionId(accountQueryAccountLogVo.get("trade_no"));
                    wechatExternalOrder.setOutTradeNo(accountQueryAccountLogVo.get("merchant_out_order_no"));
                    wechatExternalOrder.setTradeDate(DateUtils.stringToDate(accountQueryAccountLogVo.get("trans_date")));
                    wechatExternalOrder.setMchId(accountQueryAccountLogVo.get("partner_id"));
                    wechatExternalOrder.setBody(accountQueryAccountLogVo.get("goods_title"));
                    String currency = accountQueryAccountLogVo.get("currency");
                    if("156".equals(currency)){
                        wechatExternalOrder.setFeeType("CNY");
                    }else{
                        wechatExternalOrder.setFeeType(currency);
                    }
                    wechatExternalOrder.setPaymentOrgan("alipay");

                    if("在线支付".equals(accountQueryAccountLogVo.get("trans_code_msg"))){
                        wechatExternalOrder.setTotalFee(new BigDecimal(accountQueryAccountLogVo.get("total_fee")));
                        wechatExternalOrder.setTradeState("SUCCESS");
                        wechatExternalOrder.setRate(accountQueryAccountLogVo.get("rate"));
                        wechatExternalOrderMap.put(wechatExternalOrder.getTransactionId()+"_在线支付",wechatExternalOrder);
                    }else if("转账".equals(accountQueryAccountLogVo.get("trans_code_msg"))){
                        wechatExternalOrder.setRefundFee(new BigDecimal(accountQueryAccountLogVo.get("total_fee")));
                        wechatExternalOrder.setTotalFee(BigDecimal.ZERO);
                        wechatExternalOrder.setTradeState("REFUND");
                        wechatExternalOrder.setRefundStatus("SUCCESS");
                        wechatExternalOrderMap.put(wechatExternalOrder.getTransactionId()+"_收费",wechatExternalOrder);
                    }else if("收费".equals(accountQueryAccountLogVo.get("trans_code_msg"))
                            && new BigDecimal(accountQueryAccountLogVo.get("trade_refund_amount")).compareTo(BigDecimal.ZERO)==0){
                        serviceFeeMap.put(wechatExternalOrder.getTransactionId(),new BigDecimal(accountQueryAccountLogVo.get("outcome")));
                    }
                }

                //手续费记录补齐
                for(String  transactionId : wechatExternalOrderMap.keySet()){
                    WechatExternalOrder wechatExternalOrder = wechatExternalOrderMap.get(transactionId);
                    BigDecimal serviceFee = serviceFeeMap.get(transactionId.substring(0,transactionId.indexOf("_")));
                    if(serviceFee == null){
                        serviceFee = BigDecimal.ZERO;
                    }
                    wechatExternalOrder.setServiceCharge(serviceFee);
                }

                List l = wechatExternalOrderMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
                alipayExternalOrders.addAll(l);
            } catch (Exception ex) {
                throw ex;
            } finally {
                method.releaseConnection();
            }
            pageNo++;
        }
        return alipayExternalOrders;
    }

    private static NameValuePair[] generatNameValuePair(Map<String, String> properties) {
        NameValuePair[] nameValuePair = new NameValuePair[properties.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            nameValuePair[i++] = new NameValuePair(entry.getKey(), entry.getValue());
        }
        return nameValuePair;
    }

    private static Map<String, String> buildRequestPara(Map<String, String> sParaTemp,String paymentName,String key) {
        //除去数组中的空值和签名参数
        try {
            Map<String, String> sPara = paraFilter(sParaTemp);
            String signType = "MD5";
            if(!PaymentConstants.PaymentGatewayName_Alipay.equals(paymentName)){
                signType  = "RSA";
            }
            String mysign = buildRequestMysign(sPara,key,signType);

            sPara.put("sign_type",signType);
            sPara.put("sign", mysign);
            return sPara;
        }catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String buildRequestMysign(Map<String, String> sPara,String key,String signType) throws Exception {
        String prestr = createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String mysign = "";
        if(signType.equals("MD5") ) {
            mysign = md5Sign(prestr, key);
        }else{
            mysign = rsaSign(prestr,key);
        }
        return mysign;
    }

    public static String rsaSign(String text,String priKey) {
        String mysign = "";
        try {
            mysign = encrypt(text,priKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mysign;
    }

    private static String encrypt(String content,String priKey) throws Exception {
        PKCS8EncodedKeySpec pkcs8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(priKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey pk = keyFactory.generatePrivate(pkcs8);
        Signature signer = Signature.getInstance("SHA1WithRSA");
        signer.initSign(pk);
        signer.update(content.getBytes(input_charset));
        byte[] signed = signer.sign();
        return new String(Base64.getEncoder().encode(signed));
    }

    public static String md5Sign(String text,String key) throws Exception{
        text = text + key;
        return DigestUtils.md5Hex(text.getBytes(input_charset));
    }


    public static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }


    public static Map<String, String> paraFilter(Map<String, String> sArray) {
        Map<String, String> result = new HashMap<String, String>();
        if (sArray == null || sArray.size() <= 0) {
            return result;
        }
        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                    || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }
        return result;
    }

    private List<FinanceFlow> buildFinanceFlowList(Date fillDate, List<String> paymentNames) {
        Date startDate = DateUtils.getDayStart(fillDate);
        Date endDate = DateUtils.getDayEnd(fillDate);
        List<FinanceFlow> financeFlowList = new LinkedList<>();
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("endDate",endDate);
        paramsMap.put("startDate",startDate);
        String sql = "select * from VOX_FINANCE_FLOW WHERE UPDATE_DATETIME <= :endDate AND UPDATE_DATETIME >= :startDate ";
        List<Map<String, Object>> result = utopiaSql.withSql(sql).useParams(paramsMap).queryAll();
        for(Map<String,Object> temp : result){
            FinanceFlow financeFlow = new FinanceFlow();
            String id = (String) temp.get("ID");
            financeFlow.setId(id);
            Long userId = (Long) temp.get("USER_ID");
            financeFlow.setUserId(userId);
            String type = (String) temp.get("TYPE");
            financeFlow.setType(type);
            BigDecimal paymentAmount = new BigDecimal(temp.get("PAYMENT_AMOUNT").toString());
            financeFlow.setPaymentAmount(paymentAmount);
            BigDecimal amount = new BigDecimal(temp.get("AMOUNT").toString());
            financeFlow.setAmount(amount);
            String source = (String) temp.get("SOURCE");
            financeFlow.setSource(source);
            String outerId = (String) temp.get("OUTER_ID");
            financeFlow.setOuterId(outerId);
            String orderId = (String) temp.get("ORDER_ID");
            financeFlow.setOrderId(orderId);
            String state = (String) temp.get("STATE");
            financeFlow.setState(state);
            Date createDatetime = (Date) temp.get("CREATE_DATETIME");
            financeFlow.setCreateDatetime(createDatetime);
            Date updateDatetime = (Date) temp.get("UPDATE_DATETIME");
            financeFlow.setUpdateDatetime(updateDatetime);
            financeFlowList.add(financeFlow);
        }
        financeFlowList = financeFlowList.stream().filter( o -> paymentNames.contains(o.getSource())).filter( o -> o.getState().equals("SUCCESS")).collect(Collectors.toList());
        return financeFlowList;
    }

    private List<CheckOrderDiff> buildCheckOrderDiffList(List<FinanceFlow> financeFlowResList,
                                                         List<UserOrderPaymentHistory> paymentHistoryResList,
                                                         List<WechatExternalOrder> wechatExternalOrderResList) {
        List<OrderProduct> orderProductList = userOrderLoaderClient.loadAllOrderProductIncludeOfflineForCrm();
        List<CheckOrderDiff> checkOrderDiffList = new ArrayList<>();
        for(UserOrderPaymentHistory userOrderPaymentHistory : paymentHistoryResList){
            CheckOrderDiff checkOrderDiff = new CheckOrderDiff();
            UserOrder userOrder = userOrderLoaderClient.loadUserOrder(userOrderPaymentHistory.genUserOrderId());
            if(userOrder != null){
                checkOrderDiff.setProductName(userOrder.getProductName());
                OrderProduct orderProduct = orderProductList.stream().filter(o -> o.getId().equals(userOrder.getProductId())).findFirst().orElse(null);
                if(orderProduct != null){
                    checkOrderDiff.setOriginalPrice(orderProduct.getOriginalPrice());
                }
            }
            checkOrderDiff.setOrderId(userOrderPaymentHistory.getOrderId());
            checkOrderDiff.setOutTradeNo(userOrderPaymentHistory.getOuterTradeId());
            checkOrderDiff.setPayAmount(userOrderPaymentHistory.getPayAmount());
            checkOrderDiff.setTradeStatus(userOrderPaymentHistory.getPaymentStatus().name());
            if(userOrderPaymentHistory.getPaymentStatus() == PaymentStatus.Refund){
                checkOrderDiff.setTradeDate(userOrderPaymentHistory.getUpdateDatetime());
            }else{
                checkOrderDiff.setTradeDate(userOrderPaymentHistory.getPayDatetime());
            }
            if(userOrderPaymentHistory.getPayMethod().startsWith("alipay")){
                checkOrderDiff.setChannel("alipay");
            }else{
                checkOrderDiff.setChannel("wechat");
            }
            checkOrderDiff.setDiffType("2");
            checkOrderDiffList.add(checkOrderDiff);
        }

        for(WechatExternalOrder wechatExternalOrder : wechatExternalOrderResList){
            CheckOrderDiff checkOrderDiff = new CheckOrderDiff();
            checkOrderDiff.setTradeDate(wechatExternalOrder.getTradeDate());
            checkOrderDiff.setProductName(wechatExternalOrder.getBody());
            checkOrderDiff.setOrderId(wechatExternalOrder.getOutTradeNo());
            checkOrderDiff.setOutTradeNo(wechatExternalOrder.getTransactionId());
            checkOrderDiff.setPayAmount(wechatExternalOrder.getTotalFee());
            checkOrderDiff.setTradeStatus(wechatExternalOrder.getTradeState());
            checkOrderDiff.setChannel(wechatExternalOrder.getPaymentOrgan());
            checkOrderDiff.setDiffType("1");
            OrderProduct orderProduct = orderProductList.stream().filter(o -> o.getName().equals(wechatExternalOrder.getBody())).findFirst().orElse(null);
            if(orderProduct != null){
                checkOrderDiff.setOriginalPrice(orderProduct.getOriginalPrice());
            }
            checkOrderDiffList.add(checkOrderDiff);
        }

        if(financeFlowResList != null){
            for(FinanceFlow financeFlow : financeFlowResList){
                CheckOrderDiff checkOrderDiff = new CheckOrderDiff();
                checkOrderDiff.setTradeDate(financeFlow.getUpdateDatetime());
                checkOrderDiff.setProductName("学贝充值");
                checkOrderDiff.setOrderId(financeFlow.getId());
                checkOrderDiff.setOutTradeNo(financeFlow.getOuterId());
                checkOrderDiff.setPayAmount(financeFlow.getPaymentAmount());
                checkOrderDiff.setTradeStatus(financeFlow.getState());
                if(financeFlow.getSource().startsWith("alipay")){
                    checkOrderDiff.setChannel("alipay");
                }else{
                    checkOrderDiff.setChannel("wechat");
                }
                checkOrderDiff.setDiffType("2");
                checkOrderDiffList.add(checkOrderDiff);
            }
        }
        return checkOrderDiffList;
    }

    private Map<String,Object> checkPaidOrder(List<FinanceFlow> financeFlowList,
                                          List<UserOrderPaymentHistory> checkPaymentHistoryList,
                                          List<WechatExternalOrder> checkWechatOrderList,
                                          List<CheckOrderDiff> checkOrderDiffList) {
        Map<String,Object> diffOrderMap = new LinkedHashMap<>();

        Set<String> paymentHistoryOutTradeIds1 = checkPaymentHistoryList.stream().map(UserOrderPaymentHistory :: getOuterTradeId ).collect(Collectors.toSet());
        Set<String> paymentHistoryOutTradeIds2 = checkPaymentHistoryList.stream().map(UserOrderPaymentHistory :: getOuterTradeId ).collect(Collectors.toSet());

        Set<String> wechatOrderTransactionIds1 = checkWechatOrderList.stream().map(WechatExternalOrder::getTransactionId).collect(Collectors.toSet());

        //分别排除相同部分的数据
        paymentHistoryOutTradeIds1.removeAll(wechatOrderTransactionIds1);
        wechatOrderTransactionIds1.removeAll(paymentHistoryOutTradeIds2);

        logger.info("paymenthistory与微信对账文件我方剩余记录，paymentHistoryOutTradeIds1记录数 ：{}",paymentHistoryOutTradeIds1.size());
        logger.info("paymenthistory与微信对账文件微信剩余记录, wechatOrderTransactionIds1记录数 ：{}",wechatOrderTransactionIds1.size());

        //比对学贝充值记录
        Set<String> financeFlowOuterId1 = financeFlowList.stream().map(FinanceFlow::getOuterId).collect(Collectors.toSet());
        Set<String> financeFlowOuterId2 = financeFlowList.stream().map(FinanceFlow::getOuterId).collect(Collectors.toSet());

        financeFlowOuterId1.removeAll(wechatOrderTransactionIds1);
        wechatOrderTransactionIds1.removeAll(financeFlowOuterId2);

        logger.info("学贝充值与微信对账文件我方剩余记录 financeFlowOuterId1记录数：{}",financeFlowOuterId1.size());
        logger.info("学贝充值与微信对账文件微信剩余记录 wechatOrderTransactionIds1记录数：{}",wechatOrderTransactionIds1.size());

        //比对差异表中的数据，找出差异表中与各个列表中的相同的记录
        Set<String> checkOrderDiffOutTradeNos = checkOrderDiffList.stream().map(CheckOrderDiff :: getOutTradeNo ).collect(Collectors.toSet());
        //保存交集的集合
        Set<String> intersectionOutTradeNos = new HashSet<>();
        intersectionOutTradeNos.addAll(paymentHistoryOutTradeIds1);
//        intersectionOutTradeNos.addAll(wechatOrderTransactionIds1);
        intersectionOutTradeNos.addAll(financeFlowOuterId1);
        //交集，删除差异数据
        intersectionOutTradeNos.retainAll(checkOrderDiffOutTradeNos);
        logger.info("差异表与各列表的交集数据，intersectionOutTradeNos的长度：{}，intersectionOutTradeNos：{}",intersectionOutTradeNos.size(),JsonUtils.toJson(intersectionOutTradeNos));

        //其他剩余集合删除差异中相同的数据
        paymentHistoryOutTradeIds1.removeAll(intersectionOutTradeNos);
        wechatOrderTransactionIds1.removeAll(intersectionOutTradeNos);
        financeFlowOuterId1.removeAll(intersectionOutTradeNos);
        logger.info("其他剩余集合删除差异中相同的数据，paymentHistoryOutTradeIds1的长度：{},wechatOrderTransactionIds1的长度：{}，financeFlowOuterId1的长度：{}"
                ,paymentHistoryOutTradeIds1.size(),wechatOrderTransactionIds1.size(),financeFlowOuterId1.size());

        diffOrderMap.put("paymentHistoryList",checkPaymentHistoryList.stream().filter(o->paymentHistoryOutTradeIds1.contains(o.getOuterTradeId())).collect(Collectors.toList()));
        diffOrderMap.put("wechatExternalOrderList",checkWechatOrderList.stream().filter(o->wechatOrderTransactionIds1.contains(o.getTransactionId())).collect(Collectors.toList()));
        diffOrderMap.put("financeFlowList",financeFlowList.stream().filter(o->financeFlowOuterId1.contains(o.getOuterId())).collect(Collectors.toList()));
        diffOrderMap.put("delCheckOrderDiffs",checkOrderDiffList.stream().filter(o->intersectionOutTradeNos.contains(o.getOutTradeNo())).collect(Collectors.toList()));
        return diffOrderMap;
    }

    private List<List<WechatExternalOrder>> getWechatExternalOrderList(WechatAbstractPaymentGateway paymentGateway,String billDate,String billType) throws IOException {
        List<List<WechatExternalOrder>> result = new ArrayList<>();

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("appid", paymentGateway.getAppId());
        params.put("mch_id", paymentGateway.getMchId());
        params.put("nonce_str", DigestUtils.md5Hex(Long.toString(System.currentTimeMillis())).toUpperCase());
        params.put("bill_date",billDate);
        params.put("bill_type",billType);
        params.put("sign", buildSignMD5(params,paymentGateway.getSignKey()));
        String xmlData = XmlConvertUtils.mapToXml(params);

        HttpClient httpClient = null;
        HttpResponse response = null;
        InputStream inStream = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            inStream = Certification.openCertificationInputStream(paymentGateway.getMchId());
            keyStore.load(inStream, paymentGateway.getMchId().toCharArray());
            // Trust own CA and all self-signed certs
            SSLContext sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, paymentGateway.getMchId().toCharArray())
                    .build();
            // Allow TLSv1 protocol only
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslcontext,
                    new String[]{"TLSv1"},
                    null,
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            httpClient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build();
            HttpPost httpPost = new HttpPost(downbillUrl);
            StringEntity entity = new StringEntity(xmlData, "utf-8");
            entity.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseEntity.getContent(), "UTF-8"));
                String text;
                int i=0;
                List<WechatExternalOrder> wechatExternalOrderList = null;
                while ((text = bufferedReader.readLine()) != null) {
                    if(text.contains("FAIL")){
                        break;
                    }
                    if(text.contains("交易时间")){
                        continue;
                    }
                    if (text.contains("总交易单数")){
                        break;
                    }
                    if(text.split(",").length<20){
                        continue;
                    }

                    WechatExternalOrder wechatExternalOrder = buildWechatExternalOrder(text);
                    if(i%1000 == 0){
                        wechatExternalOrderList = new LinkedList<>();
                        result.add(wechatExternalOrderList);
                    }
                    wechatExternalOrderList.add(wechatExternalOrder);
                    i++;
                }
            }
        } catch (Exception ex) {
            logger.error("微信对账文件下载失败",ex);
        } finally {
            if (response instanceof Closeable) {
                IOUtils.closeQuietly((Closeable) response);
            }
            HttpClientFactory.instance().destroy(httpClient);
            if (inStream != null) {
                inStream.close();
            }
        }
        return result;
    }

    private WechatExternalOrder buildWechatExternalOrder(String record){
        String[] records = record.replace("`","").split(",");
        WechatExternalOrder wechatExternalOrder = new WechatExternalOrder();
        wechatExternalOrder.setTradeDate(DateUtils.stringToDate(records[0]));
        wechatExternalOrder.setAppId(records[1]);
        wechatExternalOrder.setMchId(records[2]);
        wechatExternalOrder.setSubMchId(records[3]);
        wechatExternalOrder.setDeviceInfo(records[4]);
        wechatExternalOrder.setTransactionId(records[5]);
        wechatExternalOrder.setOutTradeNo(records[6]);
        wechatExternalOrder.setOpenId(records[7]);
        wechatExternalOrder.setTradeType(records[8]);
        wechatExternalOrder.setTradeState(records[9]);
        wechatExternalOrder.setBankType(records[10]);
        wechatExternalOrder.setFeeType(records[11]);
        wechatExternalOrder.setTotalFee(new BigDecimal(records[12]));
        wechatExternalOrder.setRedPacketFee(new BigDecimal(records[13]));
        wechatExternalOrder.setRefundId(records[14]);
        wechatExternalOrder.setOutRefundNo(records[15]);
        wechatExternalOrder.setRefundFee(new BigDecimal(records[16]));
        wechatExternalOrder.setRefundRedPacketFee(new BigDecimal(records[17]));
        wechatExternalOrder.setRefundChannel(records[18]);
        wechatExternalOrder.setRefundStatus(records[19]);
        wechatExternalOrder.setBody(records[20]);
        wechatExternalOrder.setAttach(records[21]);
        wechatExternalOrder.setServiceCharge(new BigDecimal(records[22]));
        wechatExternalOrder.setRate(records[23]);
        wechatExternalOrder.setPaymentOrgan("wechat");
        return wechatExternalOrder;
    }

    private String buildSignMD5(Map<String, Object> params, String signKey) {
        List<String> keys = new ArrayList<>(params.keySet());
        // 排序
        Collections.sort(keys);
        StringBuilder preStr = new StringBuilder();
        for (String key : keys) {
            String value = (String) params.get(key);
            preStr.append(key).append("=").append(value).append("&");
        }
        if (preStr.length() > 0) {
            preStr.setLength(preStr.length() - 1);
        }
        preStr.append("&key=").append(signKey);
        return DigestUtils.md5Hex(preStr.toString().getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }
}
