package com.voxlearning.utopia.admin.productpromotion.service.impl;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageInfo;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageResult;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.input.ProductPromotionExportSmsParams;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.output.ProductPromotionCreateSmsDto;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.Result;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.output.ProductPromotionSmsListDto;
import com.voxlearning.utopia.admin.productpromotion.dao.entity.ProductPromotionSmsEntity;
import com.voxlearning.utopia.admin.productpromotion.domain.ProductPromotionSmsDomain;
import com.voxlearning.utopia.admin.productpromotion.domain.model.ProductPromotionSms;
import com.voxlearning.utopia.admin.productpromotion.service.ProductPromotionSmsService;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-29 14:07
 **/
@Named
public class ProductPromotionSmsServiceImpl implements ProductPromotionSmsService{
    @Inject
    private ProductPromotionSmsDomain productPromotionSmsDomain;
    @Inject
    private SmsServiceClient smsServiceClient;

    @Override
    public MapMessage create(ProductPromotionCreateSmsDto dto, AuthCurrentAdminUser user) {
        MapMessage result = MapMessage.successMessage();
        ProductPromotionSms promotionSms = ProductPromotionSms.Builder.build(dto, user);

        //如果是需要直接发送短信则先发送
        if (Objects.equals(promotionSms.getStatus(), ProductPromotionSms.Status.ALREADY_SEND)) {
            result = this.sendAndUpsert(promotionSms, user);
        } else {
            productPromotionSmsDomain.create(promotionSms);
        }
        return result;
    }

    @Override
    public Boolean send(String id, AuthCurrentAdminUser user) {
        ProductPromotionSms promotionSms = productPromotionSmsDomain.detail(id);
        return this.sendAndUpsert(promotionSms, user).isSuccess();
    }

    @Override
    public PageResult<ProductPromotionSmsListDto> queryPage(String operationUserName, PageInfo pageInfo) {
        List<ProductPromotionSms> promotionSmsList = productPromotionSmsDomain.query(operationUserName, pageInfo);
        Integer count = productPromotionSmsDomain.count(operationUserName);
        List<ProductPromotionSmsListDto> dtoList = Optional.ofNullable(promotionSmsList).orElse(Collections.emptyList())
                .stream()
                .map(model -> ProductPromotionSmsListDto.Builder.build(model))
                .collect(Collectors.toList());
        return PageResult.success((ArrayList)dtoList, pageInfo, count);
    }

    @Override
    public HSSFWorkbook export(ProductPromotionExportSmsParams params) {
        Date beginTime; Date endTime;
        beginTime = DateUtils.stringToDate(params.getBeginDay(), DateUtils.FORMAT_SQL_DATE);
        endTime = DateUtils.stringToDate(params.getEndDay(), DateUtils.FORMAT_SQL_DATE);
        endTime =DayRange.newInstance(endTime.getTime()).getEndDate();
        List<ProductPromotionSms> promotionSmsList = productPromotionSmsDomain.queryBySendTime(beginTime, endTime);

        return convertToHSS(promotionSmsList);
    }

    private HSSFWorkbook convertToHSS(List<ProductPromotionSms> promotionSmsLis) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow firstRow = hssfSheet.createRow(0);
        firstRow.setHeightInPoints(20);
        firstRow.createCell(0).setCellValue("序号");
        firstRow.createCell(1).setCellValue("发送时间");
        firstRow.createCell(2).setCellValue("目的");
        firstRow.createCell(3).setCellValue("手机号码");
        firstRow.createCell(4).setCellValue("用户类型");
        firstRow.createCell(5).setCellValue("用户ID");
        firstRow.createCell(6).setCellValue("短信内容");
        firstRow.createCell(7).setCellValue("发送人");
        firstRow.createCell(8).setCellValue("状态");

        int rowNum = 1;
        RewardProductType productType;
        double total = 0;
        for (ProductPromotionSms sms : promotionSmsLis) {
            HSSFRow hssfRow = hssfSheet.createRow(rowNum++);
            hssfRow.setHeightInPoints(20);
            hssfRow.createCell(0).setCellValue(sms.getId());
            hssfRow.createCell(1).setCellValue(DateUtils.dateToString(sms.getSendTime(), DateUtils.FORMAT_SQL_DATETIME));
            hssfRow.createCell(2).setCellValue(sms.getBizType().getDesc());
            hssfRow.createCell(3).setCellValue(sms.getPhone());
            hssfRow.createCell(4).setCellValue(sms.getTargetUserType().getDescription());
            hssfRow.createCell(5).setCellValue(Optional.ofNullable(sms.getTargetUserId()).orElse(0L));
            hssfRow.createCell(6).setCellValue(sms.getSmsContent());
            hssfRow.createCell(7).setCellValue(sms.getOperationUserName());
            hssfRow.createCell(8).setCellValue(sms.getStatus().getDesc());
        }
        //1-8行的列宽为256像素 15在这里表示一个像素
        for (int i = 0; i < 8; i++) {
            hssfSheet.setColumnWidth(i, 400 * 15);
        }
        return hssfWorkbook;
    }

    private MapMessage sendAndUpsert(ProductPromotionSms sms, AuthCurrentAdminUser user) {

        SmsMessage msg = new SmsMessage();
        msg.setMobile(sms.getPhone());
        msg.setSmsContent(sms.getSmsContent());
        msg.setType(SmsType.CRM_PLATFORM_GENERAL.name());
        MapMessage message = smsServiceClient.getSmsService().sendSms(msg);
        if (message.isSuccess()) {
            sms.setStatus(ProductPromotionSms.Status.ALREADY_SEND);
        } else {
            sms.setStatus(ProductPromotionSms.Status.FAILED_SEND);
        }
        sms.setOperationUserName(user.getAdminUserName());
        sms.setSendTime(new Date());

        productPromotionSmsDomain.upsert(sms);
        return message;
    }
}
