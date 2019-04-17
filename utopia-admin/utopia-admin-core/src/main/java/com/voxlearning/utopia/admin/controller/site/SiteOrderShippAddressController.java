package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.order.api.entity.OrderShippingAddress;
import com.voxlearning.utopia.service.order.consumer.OrderShippingAddressServiceClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.impl.support.SensitiveCodec;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * describe:
 *
 * @author yong.liu
 * @date 2019/03/20
 */
@Controller
@RequestMapping("/site/orderShippAddress")
public class SiteOrderShippAddressController  extends SiteAbstractController {

    @Inject private OrderShippingAddressServiceClient orderShippingAddressServiceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @RequestMapping(value = "loadOrderShippAddress.vpage", method = RequestMethod.POST)
    public String loadOrderShippAddress(Model model) {
        Long userId = getRequestLong("userId");
        String activitySource = getRequestString("activitySource");
        String receiverName = getRequestString("receiverName");
        String orderId = getRequestString("orderId");
        String logisticsNum = getRequestString("logisticsNum");
        List<OrderShippingAddress> orderShippingAddresses = orderShippingAddressServiceClient
                .getOrderShippingAddressService()
                .loadOrderShippingAddress(activitySource,userId,orderId,receiverName,logisticsNum);
        for(OrderShippingAddress address : orderShippingAddresses){
            // FIXME
            address.setReceiverPhone(StringUtils.isNotBlank(address.getReceiverPhone())?SensitiveCodec.mobile.decode(address.getReceiverPhone()):"");
        }
        model.addAttribute("orderShippingAddresses",orderShippingAddresses);
        model.addAttribute("activitySource",activitySource);
        model.addAttribute("receiverName",receiverName);
        model.addAttribute("orderId",orderId);
        model.addAttribute("logisticsNum",logisticsNum);
        model.addAttribute("userId",userId!=0L?userId:"");
        return "site/batch/queryShippingAddressIndex";
    }


    @RequestMapping(value = "createOrderShippAddressXls.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createOrderShippAddressXls(Model model) {
        Long userId = getRequestLong("userId");
        String activitySource = getRequestString("activitySource");
        String receiverName = getRequestString("receiverName");
        String orderId = getRequestString("orderId");
        String logisticsNum = getRequestString("logisticsNum");
        List<OrderShippingAddress> orderShippingAddresses = orderShippingAddressServiceClient
                .getOrderShippingAddressService()
                .loadOrderShippingAddress(activitySource,userId,orderId,receiverName,logisticsNum);
        for(OrderShippingAddress address : orderShippingAddresses){
            address.setReceiverPhone(StringUtils.isNotBlank(address.getReceiverPhone())?SensitiveCodec.mobile.decode(address.getReceiverPhone()):"");
        }
        String filePath = writleXls(orderShippingAddresses);

        return MapMessage.successMessage().add("filePath",filePath);
    }

    private String writleXls(List<OrderShippingAddress> orderShippingAddresses) {
        String[] titles = new String[]{"用户ID","订单ID","收件人","收件人电话","收件人详细地址","邮编","省编号","省名称","城市编号","城市名称","区县编号","区县名称","物流公司","物流编号","活动来源"};
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("订单物流信息");
        XSSFRow topRow = sheet.createRow(0);
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            XSSFCell topCell0 = topRow.createCell(i);
            topCell0.setCellValue(title);
        }
        for (int i = 0; i < orderShippingAddresses.size(); i++) {
            OrderShippingAddress orderShippingAddress = orderShippingAddresses.get(i);
            XSSFRow dataRow = sheet.createRow(i + 1);
            XSSFCell dataCell0 = dataRow.createCell(0);
            dataCell0.setCellValue(orderShippingAddress.getUserId());
            XSSFCell dataCell1 = dataRow.createCell(1);
            dataCell1.setCellValue(orderShippingAddress.getOrderId());
            XSSFCell dataCell2 = dataRow.createCell(2);
            dataCell2.setCellValue(orderShippingAddress.getReceiverName());
            XSSFCell dataCell3 = dataRow.createCell(3);
            dataCell3.setCellValue(orderShippingAddress.getReceiverPhone());
            XSSFCell dataCell4 = dataRow.createCell(4);
            dataCell4.setCellValue(orderShippingAddress.getDetailAddress());
            XSSFCell dataCell5 = dataRow.createCell(5);
            dataCell5.setCellValue(orderShippingAddress.getPostCode());
            XSSFCell dataCell6 = dataRow.createCell(6);
            dataCell6.setCellValue(orderShippingAddress.getProvinceCode());
            XSSFCell dataCell7 = dataRow.createCell(7);
            dataCell7.setCellValue(orderShippingAddress.getProvinceName());
            XSSFCell dataCell8 = dataRow.createCell(8);
            dataCell8.setCellValue(orderShippingAddress.getCityCode());
            XSSFCell dataCell9 = dataRow.createCell(9);
            dataCell9.setCellValue(orderShippingAddress.getCityName());
            XSSFCell dataCell10 = dataRow.createCell(10);
            dataCell10.setCellValue(orderShippingAddress.getCountyCode());
            XSSFCell dataCell11 = dataRow.createCell(11);
            dataCell11.setCellValue(orderShippingAddress.getCountyName());
            XSSFCell dataCell12 = dataRow.createCell(12);
            dataCell12.setCellValue(orderShippingAddress.getLogisticsCompany());
            XSSFCell dataCell13 = dataRow.createCell(13);
            dataCell13.setCellValue(orderShippingAddress.getLogisticsNum());
            XSSFCell dataCell14 = dataRow.createCell(14);
            dataCell14.setCellValue(orderShippingAddress.getActivitySource());
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] content = os.toByteArray();
        InputStream is = new ByteArrayInputStream(content);
        String fileName = "orderShippingAddress";
        String filePath = null;
        try {
            filePath = AdminOssManageUtils.upload(is, content.length, fileName, "xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }


}
