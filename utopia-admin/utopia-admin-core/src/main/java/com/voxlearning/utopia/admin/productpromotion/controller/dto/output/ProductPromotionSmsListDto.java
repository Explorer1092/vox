package com.voxlearning.utopia.admin.productpromotion.controller.dto.output;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.admin.productpromotion.domain.model.ProductPromotionSms;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.inject.Named;
import java.io.Serializable;
import java.util.Objects;

/**
 * @description: 客服推广短信列表dto
 * @author: kaibo.he
 * @create: 2019-01-29 16:29
 **/
@Data
public class ProductPromotionSmsListDto implements Serializable{
    private String id;
    private String bizType;
    private String phone;
    private String targetUserType;
    private Long targetUserId;
    private String smsContent;
    private String status;
    private String sendTime;
    private String operationUserName;

    @Named
    public static class Builder {
        public static ProductPromotionSmsListDto build(ProductPromotionSms model) {
            ProductPromotionSmsListDto dto = new ProductPromotionSmsListDto();
            BeanUtils.copyProperties(model, dto);
            dto.setBizType(model.getBizType().getDesc());
            dto.setTargetUserType(model.getTargetUserType().getDescription());
            dto.setStatus(model.getStatus().name());
            dto.setSendTime(Objects.isNull(model.getSendTime()) ? "":DateUtils.dateToString(model.getSendTime(), DateUtils.FORMAT_SQL_DATETIME));
            return dto;
        }
    }
}
