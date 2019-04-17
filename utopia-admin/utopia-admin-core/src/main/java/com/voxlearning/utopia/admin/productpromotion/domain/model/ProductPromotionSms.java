package com.voxlearning.utopia.admin.productpromotion.domain.model;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.output.ProductPromotionCreateSmsDto;
import com.voxlearning.utopia.admin.productpromotion.dao.entity.ProductPromotionSmsEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 产品推广创建短信model
 * @author: kaibo.he
 * @create: 2019-01-29 10:58
 **/
@Data
public class ProductPromotionSms implements Serializable{
    private String id;
    private BizType bizType;
    private String phone;
    private UserType targetUserType;
    private Long targetUserId;
    private String smsContent;
    private Status status;
    private Date sendTime;
    private String operationUserName;

    @AllArgsConstructor
    public enum BizType {
        JZT("家长通推荐"),
        LIVECAST("直播课推荐"),
        IMPERIAL_PALACE("故宫24节气课程"),
        POINT_READER("点读机推荐"),
        NOTICE("通知类信息"),
        ;
        @Getter
        private String desc;
    }

    @AllArgsConstructor
    public enum Status {
        PENDING_SEND("待发送"),
        ALREADY_SEND("已发送"),
        FAILED_SEND("发送失败"),
        ;
        @Getter
        private String desc;
    }

    public static class Builder {
        public static ProductPromotionSms build(ProductPromotionCreateSmsDto dto, AuthCurrentAdminUser user) {
            ProductPromotionSms sms = new ProductPromotionSms();
            BeanUtils.copyProperties(dto, sms);
            sms.setBizType(BizType.valueOf(dto.getBizType()));
            sms.setTargetUserType(UserType.valueOf(dto.getTargetUserType()));
            sms.setStatus(Status.valueOf(dto.getStatus()));
            sms.setOperationUserName(user.getAdminUserName());
            return sms;
        }

        public static ProductPromotionSms build(ProductPromotionSmsEntity entity) {
            ProductPromotionSms promotionSms = new ProductPromotionSms();
            BeanUtils.copyProperties(entity, promotionSms);
            promotionSms.setBizType(BizType.valueOf(entity.getBizType()));
            promotionSms.setTargetUserType(UserType.valueOf(entity.getTargetUserType()));
            promotionSms.setStatus(Status.valueOf(entity.getStatus()));
            return promotionSms;
        }
    }
}
