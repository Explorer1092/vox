package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.util.JobOssManageUtils;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.CheckOrderDiff;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderCheckAccountClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import org.apache.http.impl.io.DefaultHttpRequestWriter;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-05-21 18:58
 */
@Named
@ScheduledJobDefinition(
        jobName = "发送对账结果",
        jobDescription = "将对账结果做成表格,每月3号上午4点执行",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.STAGING, Mode.TEST},
        cronExpression = "0 0 4 3 * ?"
)
public class AutoSendCheckOrderResultJob   extends ScheduledJobWithJournalSupport {

    @Inject private UserOrderCheckAccountClient userOrderCheckAccountClient;

    @Inject private UserOrderLoaderClient userOrderLoaderClient;

    @Inject private EmailServiceClient emailServiceClient;

    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql utopiaSqlOrder;
    private UtopiaSql utopiaSql;

    private static List<String> paymentMethod;

    static{
        paymentMethod = new LinkedList<>();
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_ParentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_StudentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Wechat_PcNative);
        paymentMethod.add(PaymentConstants.PaymentGatewayname_Alipay_StudentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayname_Alipay_ParentApp);
        paymentMethod.add(PaymentConstants.PaymentGatewayName_Alipay);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("order");
    }
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        //每月3号发送对账文件
        Date billDate = DateUtils.calculateDateDay(new Date(),-10);
        Date monthStartDate = DateUtils.getFirstDayOfMonth(billDate);
        Date monthendDate = DateUtils.getLastDayOfMonth(billDate);
        List<CheckOrderDiff> checkOrderDiffList = userOrderCheckAccountClient.loadCheckOrderDiffsByDate(monthStartDate,monthendDate);

        double sumPaidAmt = getAmountbyMonth(monthStartDate,monthendDate,"Paid");
        double sumRefundAmt = getAmountbyMonth(monthStartDate,monthendDate,"Refund");
        double sumFinanceFlow = getFinanceFlowAmount(monthStartDate,monthendDate);

        //创建Excel
        HSSFWorkbook workbook = new HSSFWorkbook();
        createSheet(workbook,checkOrderDiffList,sumPaidAmt+sumFinanceFlow,"Paid");
        createSheet(workbook,checkOrderDiffList,sumRefundAmt,"Refund");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] content = os.toByteArray();
        InputStream is = new ByteArrayInputStream(content);
        try {
            String filePath = JobOssManageUtils.upload(is,content.length,"xls");
            //跑完发送邮件
            emailServiceClient.createPlainEmail()
                    .to("lijing.cui@17zuoye.com;yong.liu@17zuoye.com")
                    .cc("siqinbatu@17zuoye.com;zhilong.hu@17zuoye.com")
                    .subject(DateUtils.dateToString(billDate,"yyyy年MM月")+"对账结果")
                    .body("各位好，对账文件请点击此链接下载："+filePath)
                    .send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSheet(HSSFWorkbook workbook,List<CheckOrderDiff> checkOrderDiffList,double sumAmt,String tradeType) {
        //7列，行数分两部分，上半部分为第三方平台有，业务平台没有的订单，下半部分是第三方平台没有，我方有的订单
        HSSFSheet sheet ;
        List<CheckOrderDiff> partA;
        List<CheckOrderDiff> partB;
        if("Paid".equals(tradeType)){
            sheet = workbook.createSheet("收款订单");
            partA = checkOrderDiffList.stream().filter(o -> "1".equals(o.getDiffType())).filter(o->o.getTradeStatus().equals("Paid") || o.getTradeStatus().equals("SUCCESS")).collect(Collectors.toList());
            partB = checkOrderDiffList.stream().filter(o -> "2".equals(o.getDiffType())).filter(o->o.getTradeStatus().equals("Paid") || o.getTradeStatus().equals("SUCCESS")).collect(Collectors.toList());
        }else{
            sheet = workbook.createSheet("退款订单");
            partA = checkOrderDiffList.stream().filter(o -> "1".equals(o.getDiffType())).filter(o->o.getTradeStatus().equals("Refund") || o.getTradeStatus().equals("REFUND")).collect(Collectors.toList());
            partB = checkOrderDiffList.stream().filter(o -> "2".equals(o.getDiffType())).filter(o->o.getTradeStatus().equals("Refund") || o.getTradeStatus().equals("REFUND")).collect(Collectors.toList());
        }

        HSSFCellStyle style = workbook.createCellStyle();
        //设置边框样式
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        //设置边框颜色
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setWrapText(true);

        HSSFCellStyle datafromatStyle = getDataFormatStyle(workbook);
        HSSFCellStyle backgroundStyle = getBackGroundGreyColor(workbook);

        HSSFCellStyle pinkStyle = getBackGroundpinkColor(workbook);

        HSSFRow topRow = sheet.createRow(0);
        HSSFCell topCell = topRow.createCell(0);
        topCell.setCellValue("业务订单金额");
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,3));
        topCell.setCellStyle(style);
        topCell.setCellStyle(pinkStyle);
        sheet.addMergedRegion(new CellRangeAddress(0,0,4,7));
        HSSFCell topCell3 = topRow.createCell(3);
        topCell3.setCellStyle(datafromatStyle);
        topCell3.setCellStyle(pinkStyle);
        HSSFCell topCell4 = topRow.createCell(4);
        topCell4.setCellStyle(datafromatStyle);
        topCell4.setCellStyle(pinkStyle);
        topCell4.setCellValue(sumAmt);
        HSSFCell topCell7 = topRow.createCell(7);
        topCell7.setCellStyle(pinkStyle);
        topCell7.setCellStyle(style);

        HSSFRow titleRow = sheet.createRow(1);
        HSSFCell cell0 = titleRow.createCell(0);
        cell0.setCellValue("第\n三\n方\n平\n台\n有\n，\n业\n务\n没\n有\n");
        cell0.setCellStyle(style);
        HSSFCell cell1 = titleRow.createCell(1);
        cell1.setCellValue("交易时间");
        cell1.setCellStyle(style);
        HSSFCell cell2 = titleRow.createCell(2);
        cell2.setCellValue("产品名称");
        cell2.setCellStyle(style);
        HSSFCell cell3 = titleRow.createCell(3);
        cell3.setCellValue("交易单号");
        cell3.setCellStyle(style);
        HSSFCell cell4 = titleRow.createCell(4);
        cell4.setCellValue("课程原价");
        cell4.setCellStyle(style);
        HSSFCell cell5 = titleRow.createCell(5);
        cell5.setCellValue("实付金额");
        cell5.setCellStyle(style);
        HSSFCell cell6 = titleRow.createCell(6);
        cell6.setCellValue("支付状态");
        cell6.setCellStyle(style);
        HSSFCell cell7 = titleRow.createCell(7);
        cell7.setCellValue("支付渠道");
        cell7.setCellStyle(style);

        BigDecimal sum1 = new BigDecimal(0);
        for(int i=0; i<partA.size(); i++){
            HSSFRow row = sheet.createRow(i+2);
            CheckOrderDiff checkOrderDiff = partA.get(i);
            HSSFCell temp1 = row.createCell(1);
            temp1.setCellValue(DateUtils.dateToString(checkOrderDiff.getTradeDate()));
            temp1.setCellStyle(style);
            HSSFCell temp2 = row.createCell(2);
            String orderId = checkOrderDiff.getOrderId();
            String userOrderId = orderId;
            if(!userOrderId.contains(UserOrder.SEP)){
                userOrderId = orderId+UserOrder.SEP+orderId.substring(orderId.length()-2,orderId.length());
            }
            UserOrder userOrder = userOrderLoaderClient.loadUserOrder(userOrderId);
            if(checkOrderDiff.getProductName() == null){
                if(userOrder != null){
                    temp2.setCellValue(userOrder.getProductName());
                }
            }else{
                temp2.setCellValue(checkOrderDiff.getProductName());
            }
            temp2.setCellStyle(style);
            HSSFCell temp3 = row.createCell(3);
            temp3.setCellValue(orderId);
            temp3.setCellStyle(style);
            HSSFCell temp4 = row.createCell(4);
            temp4.setCellStyle(style);
            BigDecimal originalPrice = checkOrderDiff.getOriginalPrice();
            if(originalPrice == null){
                if(userOrder !=null){
                    OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(userOrder.getProductId());
                    if(orderProduct != null){
                        temp4.setCellValue(orderProduct.getOriginalPrice().doubleValue());
                    }else{
                        temp4.setCellValue("无");
                    }
                }else{
                    temp4.setCellValue("无");
                }
            }else{
                temp4.setCellValue(originalPrice.doubleValue());
            }
            temp4.setCellStyle(style);
            HSSFCell temp5 = row.createCell(5);
            temp5.setCellValue(checkOrderDiff.getPayAmount().doubleValue());
            temp5.setCellStyle(datafromatStyle);
            sum1 = sum1.add(checkOrderDiff.getPayAmount());
            HSSFCell temp6 = row.createCell(6);
            if("SUCCESS".equals(checkOrderDiff.getTradeStatus())
                    || "Paid".equals(checkOrderDiff.getTradeStatus())){
                temp6.setCellValue("支付成功");
            }else if("Refund".equals(checkOrderDiff.getTradeStatus())){
                temp6.setCellValue("退款成功");
            }else{
                temp6.setCellValue(checkOrderDiff.getTradeStatus());
            }
            temp6.setCellStyle(style);
            HSSFCell temp7 = row.createCell(7);
            if("wechat".equals(checkOrderDiff.getChannel())){
                temp7.setCellValue("微信");
            }else if("alipay".equals(checkOrderDiff.getChannel())){
                temp7.setCellValue("支付宝");
            }else{
                temp7.setCellValue(checkOrderDiff.getChannel());
            }
            temp7.setCellStyle(style);
        }
        if(partA.size()>0){
            sheet.addMergedRegion(new CellRangeAddress(1,partA.size()+1,0,0));
        }

        HSSFRow sumRow1 = sheet.createRow(partA.size()+2);
        HSSFCell sumCell1 = sumRow1.createCell(0);
        sumCell1.setCellValue("小计");
        sheet.addMergedRegion(new CellRangeAddress(sumRow1.getRowNum(),sumRow1.getRowNum(),0,4));
        sumCell1.setCellStyle(backgroundStyle);
        HSSFCell sumCell2 = sumRow1.createCell(5);
        sumCell2.setCellStyle(datafromatStyle);
        sumCell2.setCellStyle(backgroundStyle);
        sumCell2.setCellValue(sum1.doubleValue());
        HSSFCell sumCell6 = sumRow1.createCell(6);
        sumCell6.setCellStyle(backgroundStyle);
        HSSFCell sumCell7 = sumRow1.createCell(7);
        sumCell7.setCellStyle(backgroundStyle);

        HSSFRow secondRow = sheet.createRow(sumRow1.getRowNum()+1);
        HSSFCell cell20 = secondRow.createCell(0);
        cell20.setCellValue("业\n务\n订\n单\n有\n，\n第\n三\n方\n平\n台\n订\n单\n没\n有\n");
        cell20.setCellStyle(style);
        HSSFCell cell21 = secondRow.createCell(1);
        cell21.setCellValue("交易时间");
        cell21.setCellStyle(style);
        HSSFCell cell22 = secondRow.createCell(2);
        cell22.setCellValue("产品名称");
        cell22.setCellStyle(style);
        HSSFCell cell23 = secondRow.createCell(3);
        cell23.setCellValue("交易单号");
        cell23.setCellStyle(style);
        HSSFCell cell24 = secondRow.createCell(4);
        cell24.setCellValue("课程原价");
        cell24.setCellStyle(style);
        HSSFCell cell25 = secondRow.createCell(5);
        cell25.setCellValue("实付金额");
        cell25.setCellStyle(style);
        HSSFCell cell26 = secondRow.createCell(6);
        cell26.setCellValue("支付状态");
        cell26.setCellStyle(style);
        HSSFCell cell27 = secondRow.createCell(7);
        cell27.setCellValue("支付渠道");
        cell27.setCellStyle(style);

        BigDecimal sum2 = new BigDecimal(0);
        for (int i=0; i<partB.size(); i++){
            CheckOrderDiff checkOrderDiff = partB.get(i);
            HSSFRow row = sheet.createRow(secondRow.getRowNum()+1+i);
            HSSFCell temp1 = row.createCell(1);
            temp1.setCellValue(DateUtils.dateToString(checkOrderDiff.getTradeDate()));
            temp1.setCellStyle(style);
            HSSFCell temp2 = row.createCell(2);
            String orderId = checkOrderDiff.getOrderId();
            String userOrderId = orderId;
            if(!userOrderId.contains(UserOrder.SEP)){
                userOrderId = orderId+UserOrder.SEP+orderId.substring(orderId.length()-2,orderId.length());
            }
            UserOrder userOrder = userOrderLoaderClient.loadUserOrderIncludeCanceled(userOrderId);
            HSSFCell temp4 = row.createCell(4);
            if(checkOrderDiff.getProductName() == null){
                if(userOrder != null){
                    OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(userOrder.getProductId());
                    if(orderProduct != null){
                        temp4.setCellValue(orderProduct.getOriginalPrice().doubleValue());
                    }
                    temp2.setCellValue(userOrder.getProductName());
                }
            }else{
                temp2.setCellValue(checkOrderDiff.getProductName());
            }
            temp2.setCellStyle(style);
            HSSFCell temp3 = row.createCell(3);
            temp3.setCellValue(checkOrderDiff.getOrderId());
            temp3.setCellStyle(style);

            if(checkOrderDiff.getOriginalPrice() == null){
                temp4.setCellValue("无");
            }else{
                temp4.setCellValue(checkOrderDiff.getOriginalPrice().doubleValue());
            }
            temp4.setCellStyle(style);
            HSSFCell temp5 = row.createCell(5);
            temp5.setCellStyle(datafromatStyle);
            temp5.setCellValue(checkOrderDiff.getPayAmount().doubleValue());
            sum2 = sum2.add(checkOrderDiff.getPayAmount());
            HSSFCell temp6 = row.createCell(6);
            if("SUCCESS".equals(checkOrderDiff.getTradeStatus())
                    || "Paid".equals(checkOrderDiff.getTradeStatus())){
                temp6.setCellValue("支付成功");
            }else if("Refund".equals(checkOrderDiff.getTradeStatus())){
                temp6.setCellValue("退款成功");
            }else{
                temp6.setCellValue(checkOrderDiff.getTradeStatus());
            }
            temp6.setCellStyle(style);
            HSSFCell temp7 = row.createCell(7);
            if("wechat".equals(checkOrderDiff.getChannel())){
                temp7.setCellValue("微信");
            }else if("alipay".equals(checkOrderDiff.getChannel())){
                temp7.setCellValue("支付宝");
            }else{
                temp7.setCellValue(checkOrderDiff.getChannel());
            }
            temp7.setCellStyle(style);
        }
        if (partB.size() > 0){
            sheet.addMergedRegion(new CellRangeAddress(sumRow1.getRowNum()+1,sumRow1.getRowNum()+1+partB.size(),0,0));
        }
        int dataLen = partA.size()+partB.size();
        HSSFRow sumRow2 = sheet.createRow(dataLen+4);
        HSSFCell sumCell21 = sumRow2.createCell(0);
        sumCell21.setCellValue("小计");
        sumCell21.setCellStyle(backgroundStyle);
        HSSFCell sumCell26 = sumRow2.createCell(5);
        sumCell26.setCellStyle(datafromatStyle);
        sumCell26.setCellStyle(backgroundStyle);
        sumCell26.setCellValue(sum2.doubleValue());
        sheet.addMergedRegion(new CellRangeAddress(dataLen+4,dataLen+4,0,4));
        HSSFCell sumCell27 = sumRow2.createCell(6);
        sumCell27.setCellStyle(backgroundStyle);
        HSSFCell sumCell28 = sumRow2.createCell(7);
        sumCell28.setCellStyle(backgroundStyle);

        HSSFCellStyle blueStyle = getBackGroundblueColor(workbook);
        HSSFRow sumRow3 = sheet.createRow(dataLen+5);
        HSSFCell sumCell31 = sumRow3.createCell(0);
        sumCell31.setCellValue("调整后订单余额");
        sumCell31.setCellStyle(blueStyle);
        HSSFCell sumCell32 = sumRow3.createCell(1);
        sumCell32.setCellStyle(blueStyle);
        HSSFCell sumCell33 = sumRow3.createCell(2);
        sumCell33.setCellStyle(blueStyle);
        HSSFCell sumCell34 = sumRow3.createCell(3);
        sumCell34.setCellStyle(blueStyle);
        HSSFCell sumCell35 = sumRow3.createCell(4);
        sumCell35.setCellStyle(blueStyle);
        HSSFCell sumCell36 = sumRow3.createCell(5);
        double sum3 = sumAmt + sum1.doubleValue() - sum2.doubleValue();
        sumCell36.setCellValue(sum3);
        sumCell36.setCellStyle(datafromatStyle);
        sumCell36.setCellStyle(blueStyle);
        sheet.addMergedRegion(new CellRangeAddress(dataLen+5,dataLen+5,0,4));
        HSSFCell sumCell37 = sumRow3.createCell(6);
        sumCell37.setCellStyle(blueStyle);
        HSSFCell sumCell38 = sumRow3.createCell(7);
        sumCell38.setCellStyle(blueStyle);


        sheet.autoSizeColumn((short)0);
        sheet.autoSizeColumn((short)1);
        sheet.autoSizeColumn((short)2);
        sheet.autoSizeColumn((short)3);
    }


    private HSSFCellStyle getBackGroundGreyColor(HSSFWorkbook workbook){
        HSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setWrapText(true);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return style;
    }

    private HSSFCellStyle getBackGroundblueColor(HSSFWorkbook workbook){
        HSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setWrapText(true);
        style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return style;
    }

    private HSSFCellStyle getBackGroundpinkColor(HSSFWorkbook workbook){
        HSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setWrapText(true);
        style.setFillForegroundColor(IndexedColors.PINK.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return style;
    }


    private HSSFCellStyle getDataFormatStyle(HSSFWorkbook workbook){
        HSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setWrapText(true);
        HSSFDataFormat df = workbook.createDataFormat();
        style.setDataFormat(df.getBuiltinFormat("#,##0.00"));
        return style;
    }

    private double getAmountbyMonth(Date startDate,Date endDate,String tradeType){
        //从头摊销表中计算金额，要减去点读和一去学的订单
        List payStatus = new ArrayList();
        if("Paid".equals(tradeType)){
            payStatus.add("Paid");
            payStatus.add("Consume");
        }else{
            payStatus.add("Refund");
        }
        String sql = "SELECT  sum(pay_amount) as sumamt FROM VOX_USER_ORDER_AMORTIZE_HISTORY where  " +
                "order_product_service_type not in ('PicListenBook','YiQiXue','WalkerMan','JuniorVipReport') " +
                "and pay_method in(:paymentMethod)" +
                "and payment_status in(:payStatus) " +
                "and disabled=0 " +
                "and pay_amount>=0.5 "+
                "and pay_datetime<= :endDate and pay_datetime>= :startDate ";
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("endDate",endDate);
        paramsMap.put("startDate",startDate);
        paramsMap.put("paymentMethod",paymentMethod);
        paramsMap.put("payStatus",payStatus);
        Map<String, Object> result = utopiaSqlOrder.withSql(sql).useParams(paramsMap).queryRow();
        Double sumAmt = 0d;
        if(result == null){
            sumAmt = 0d;
        }else{
            BigDecimal sumamt = (BigDecimal) result.get("sumamt");
            if(sumamt != null){
                sumAmt = sumamt.doubleValue();
            }else{
                sumAmt = 0d;
            }
        }
        return sumAmt;
    }

    private double getFinanceFlowAmount(Date monthStart,Date monthEnd) {
        List<FinanceFlow> financeFlowList = new LinkedList<>();
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("endDate",monthEnd);
        paramsMap.put("startDate",monthStart);
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
        financeFlowList = financeFlowList.stream().filter( o -> paymentMethod.contains(o.getSource())).filter( o -> o.getState().equals("SUCCESS")).collect(Collectors.toList());
        BigDecimal sum = financeFlowList.stream().map(FinanceFlow::getPaymentAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.doubleValue();
    }

}
