package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-04-26 14:39
 */

@Named
@ScheduledJobDefinition(
        jobName = "自动计算第三方分成产品",
        jobDescription = "自动计算第三方分成产品，每月1号上午8点执行",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.STAGING, Mode.TEST},
        cronExpression = "0 0 8 1 * ?"
)
public class AutoCalcThirdProductJob   extends ScheduledJobWithJournalSupport {

    @Inject private EmailServiceClient emailServiceClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql utopiaSqlOrder;


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("order");
    }



    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        /**
         * 计算的产品如下
         * 产品名称	分成比例    	留存比例
         * 人教	        70%	    30%
         * 外研	        80%	    20%
         * 悟空识字	    70%	    30%
         * 悟空拼音	    70%	    30%
         * 酷跑学单词	80%	    20%
         */
        //根据当前时间计算每月月初第一天的时间
        //获得当天的前一天的时间
        Date date = DateUtils.calculateDateDay(new Date(),-1) ;
        Date endDate = DateUtils.getDayEnd(date);
        Date monthStartDate = DateUtils.getFirstDayOfMonth(date);

        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("beginDate",monthStartDate);
        paramsMap.put("endDate",endDate);

        String sql = "SELECT a.ORDER_PRODUCT_SERVICE_TYPE as productName,PAYMENT_STATUS as paymentStatus,SUM(a.`PAY_AMOUNT`) as mount FROM VOX_USER_ORDER_AMORTIZE_HISTORY a \n" +
                "WHERE a.`PAYMENT_STATUS` IN('Paid','Refund')\n" +
                "AND \n" +
                "PAY_DATETIME <= :endDate\n" +
                "AND \n" +
                "PAY_DATETIME >= :beginDate\n" +
                "AND ORDER_PRODUCT_SERVICE_TYPE  IN('GreatAdventure','WukongShizi','WukongPinyin')\n"+
                "GROUP BY ORDER_PRODUCT_SERVICE_TYPE,PAYMENT_STATUS\n" +
                "UNION ALL\n" +
                "SELECT 'renjiao',a.`PAYMENT_STATUS` as paymentStatus ,SUM(a.`PAY_AMOUNT`) FROM VOX_USER_ORDER_AMORTIZE_HISTORY a \n" +
                "LEFT JOIN VOX_ORDER_PRODUCT_ITEM i ON a.`PRODUCT_ITEM_ID`=i.`ID`\n" +
                "WHERE i.`NAME` LIKE '%人教%'\n" +
                "AND a.`PAYMENT_STATUS` IN('Paid','Refund')\n" +
                "AND \n" +
                "PAY_DATETIME <= :endDate\n" +
                "AND \n" +
                "PAY_DATETIME >= :beginDate\n" +
                "GROUP BY PAYMENT_STATUS\n"+
                "UNION ALL \n" +
                "SELECT 'waiyan',a.`PAYMENT_STATUS` as paymentStatus ,SUM(a.`PAY_AMOUNT`) FROM VOX_USER_ORDER_AMORTIZE_HISTORY a \n" +
                "LEFT JOIN VOX_ORDER_PRODUCT_ITEM i ON a.`PRODUCT_ITEM_ID`=i.`ID`\n" +
                "WHERE i.`NAME` LIKE '%外研%'\n" +
                "AND a.`PAYMENT_STATUS` IN('Paid','Refund')\n" +
                "AND \n" +
                "PAY_DATETIME <= :endDate\n" +
                "AND \n" +
                "PAY_DATETIME >= :beginDate\n"+
                "GROUP BY PAYMENT_STATUS ";

        List<Map<String, Object>> result = utopiaSqlOrder.withSql(sql).useParams(paramsMap).queryAll();
        logger.info("第三方分成产品sql计算结果:{}",JsonUtils.toJson(result));


        Map<String,Map<String,BigDecimal>> resultMap = new LinkedHashMap<>();
        StringBuffer calcThirdProductRes = new StringBuffer("<table border='1' cellpadding='0' cellspacing='0'><tr><th>产品名称</th><th>收入金额</th><th>退款金额</th><th>可分成金额</th><th>分成金额</th><th>留存金额</th></tr>");
        //计算 产品名称，分成金额，留存金额
        for(Map<String,Object> map : result) {
            String productName = (String) map.get("productName");
            String paymentStatus = (String) map.get("paymentStatus");
            BigDecimal mount = (BigDecimal) map.get("mount");
            if (productName != null) {
                Map<String,BigDecimal> temp = resultMap.get(productName);
                if(temp == null){
                    temp = new LinkedHashMap<>();
                }
                temp.put(paymentStatus,mount);
                resultMap.put(productName,temp);
            }
        }

        DecimalFormat df = new DecimalFormat("#,##0.00");
        if(resultMap.containsKey("renjiao")){
            BigDecimal paidMount = resultMap.get("renjiao").get("Paid");
            BigDecimal refundMount = resultMap.get("renjiao").get("Refund");
            if(refundMount == null){
                refundMount = BigDecimal.ZERO;
            }
            BigDecimal profitMount = paidMount.subtract(refundMount);
            BigDecimal fenchengJine = profitMount.multiply(new BigDecimal(0.7)).setScale(2,BigDecimal.ROUND_HALF_UP);;
            BigDecimal liucunJine = profitMount.multiply(new BigDecimal(0.3)).setScale(2,BigDecimal.ROUND_HALF_UP);;
            calcThirdProductRes.append("<tr><td>人教</td><td>")
                    .append(df.format(paidMount.doubleValue())+"</td><td>")
                    .append(df.format(refundMount.doubleValue())+"</td><td>")
                    .append(df.format(profitMount.doubleValue())+"</td><td>")
                    .append(df.format(fenchengJine.doubleValue()) + "</td><td>")
                    .append(df.format(liucunJine.doubleValue()) + "</td></tr>");
        }else{
            calcThirdProductRes.append("<tr><td>人教</td><td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("0</td></tr>");
        }

        if(resultMap.containsKey("waiyan")){
            BigDecimal paidMount = resultMap.get("waiyan").get("Paid");
            BigDecimal refundMount = resultMap.get("waiyan").get("Refund");
            if(refundMount == null){
                refundMount = BigDecimal.ZERO;
            }
            BigDecimal profitMount = paidMount.subtract(refundMount);
            BigDecimal fenchengJine = profitMount.multiply(new BigDecimal(0.8)).setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal liucunJine = profitMount.multiply(new BigDecimal(0.2)).setScale(2,BigDecimal.ROUND_HALF_UP);
            calcThirdProductRes.append("<tr><td>外研</td><td>")
                    .append(df.format(paidMount.doubleValue())+"</td><td>")
                    .append(df.format(refundMount.doubleValue())+"</td><td>")
                    .append(df.format(profitMount.doubleValue())+"</td><td>")
                    .append(df.format(fenchengJine.doubleValue()) + "</td><td>")
                    .append(df.format(liucunJine.doubleValue()) + "</td></tr>");
        }else{
            calcThirdProductRes.append("<tr><td>外研</td><td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("0</td></tr>");
        }

        if(resultMap.containsKey("WukongShizi")){
            BigDecimal paidMount = resultMap.get("WukongShizi").get("Paid");
            BigDecimal refundMount = resultMap.get("WukongShizi").get("Refund");
            if(refundMount == null){
                refundMount = BigDecimal.ZERO;
            }
            BigDecimal profitMount = paidMount.subtract(refundMount);
            BigDecimal fenchengJine = profitMount.multiply(new BigDecimal(0.7)).setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal liucunJine = profitMount.multiply(new BigDecimal(0.3)).setScale(2,BigDecimal.ROUND_HALF_UP);
            calcThirdProductRes.append("<tr><td>悟空识字</td><td>")
                    .append(df.format(paidMount.doubleValue())+"</td><td>")
                    .append(df.format(refundMount.doubleValue())+"</td><td>")
                    .append(df.format(profitMount.doubleValue())+"</td><td>")
                    .append(df.format(fenchengJine.doubleValue()) + "</td><td>")
                    .append(df.format(liucunJine.doubleValue()) + "</td></tr>");
        }else{
            calcThirdProductRes.append("<tr><td>悟空识字</td><td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("0</td></tr>");
        }

        if(resultMap.containsKey("WukongPinyin")){
            BigDecimal paidMount = resultMap.get("WukongPinyin").get("Paid");
            BigDecimal refundMount = resultMap.get("WukongPinyin").get("Refund");
            if(refundMount == null){
                refundMount = BigDecimal.ZERO;
            }
            BigDecimal profitMount = paidMount.subtract(refundMount);
            BigDecimal fenchengJine = profitMount.multiply(new BigDecimal(0.7)).setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal liucunJine = profitMount.multiply(new BigDecimal(0.3)).setScale(2,BigDecimal.ROUND_HALF_UP);
            calcThirdProductRes.append("<tr><td>悟空拼音</td><td>")
                    .append(df.format(paidMount.doubleValue())+"</td><td>")
                    .append(df.format(refundMount.doubleValue())+"</td><td>")
                    .append(df.format(profitMount.doubleValue())+"</td><td>")
                    .append(df.format(fenchengJine.doubleValue()) + "</td><td>")
                    .append(df.format(liucunJine.doubleValue()) + "</td></tr>");
        }else{
            calcThirdProductRes.append("<tr><td>悟空拼音</td><td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("0</td></tr>");
        }

        if(resultMap.containsKey("GreatAdventure")){
            BigDecimal paidMount = resultMap.get("GreatAdventure").get("Paid");
            BigDecimal refundMount = resultMap.get("GreatAdventure").get("Refund");
            if(refundMount == null){
                refundMount = BigDecimal.ZERO;
            }
            BigDecimal profitMount = paidMount.subtract(refundMount);
            BigDecimal fenchengJine = profitMount.multiply(new BigDecimal(0.7)).setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal liucunJine = profitMount.multiply(new BigDecimal(0.3)).setScale(2,BigDecimal.ROUND_HALF_UP);
            calcThirdProductRes.append("<tr><td>酷跑学单词</td><td>")
                    .append(df.format(paidMount.doubleValue())+"</td><td>")
                    .append(df.format(refundMount.doubleValue())+"</td><td>")
                    .append(df.format(profitMount.doubleValue())+"</td><td>")
                    .append(df.format(fenchengJine.doubleValue()) + "</td><td>")
                    .append(df.format(liucunJine.doubleValue()) + "</td></tr>");
        }else{
            calcThirdProductRes.append("tr><td>酷跑学单词</td><td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("</td>0<td>")
                    .append("0</td></tr>");
        }
        calcThirdProductRes.append("</table>");

        //发送邮件
        if (RuntimeMode.isProduction()) {
            //发邮件
            Map<String, Object> content = new HashMap<>();
            content.put("startDate",DateUtils.dateToString(monthStartDate,DateUtils.FORMAT_SQL_DATE));
            content.put("endDate",DateUtils.dateToString(endDate,DateUtils.FORMAT_SQL_DATE));
            content.put("statisticResult", calcThirdProductRes.toString());
            emailServiceClient.createTemplateEmail(EmailTemplate.statisticsthirdproduct)
                    .to("lijing.cui@17zuoye.com;nan.jiang@17zuoye.com")
                    .cc("yong.liu@17zuoye.com;zhilong.hu@17zuoye.com")
                    .subject("第三方分成产品统计结果")
                    .content(content).send();
        }

    }
}
