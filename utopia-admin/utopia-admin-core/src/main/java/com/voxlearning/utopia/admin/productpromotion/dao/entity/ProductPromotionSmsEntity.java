package com.voxlearning.utopia.admin.productpromotion.dao.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.productpromotion.domain.model.ProductPromotionSms;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @description: 产品推广短信实体
 * @author: kaibo.he
 * @create: 2019-01-29 11:47
 **/
@Data
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_product_promotion_sms")
public class ProductPromotionSmsEntity implements Serializable{
    @DocumentId
    private String id;
    private String bizType;
    private String phone;
    private String targetUserType;
    private Long targetUserId;
    private String smsContent;
    private String status;
    private Date sendTime;
    private String operationUserName;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    public static class Builder {

        public static ProductPromotionSmsEntity build(ProductPromotionSms model) {
            ProductPromotionSmsEntity entity = new ProductPromotionSmsEntity();
            BeanUtils.copyProperties(model, entity);
            entity.setStatus(model.getStatus().name());
            entity.setTargetUserType(model.getTargetUserType().name());
            entity.setBizType(model.getBizType().name());
            return entity;
        }

        public static ProductPromotionSmsEntity build(ProductPromotionSms model, AuthCurrentAdminUser user) {
            ProductPromotionSmsEntity entity = new ProductPromotionSmsEntity();
            BeanUtils.copyProperties(model, entity);
            entity.setOperationUserName(user.getAdminUserName());
            return entity;
        }

        public static ProductPromotionSmsEntity build(ProductPromotionSms model, User user) {
            ProductPromotionSmsEntity entity = new ProductPromotionSmsEntity();
            BeanUtils.copyProperties(model, entity);
            entity.setBizType(model.getBizType().name());
            entity.setTargetUserType(model.getTargetUserType().name());
            entity.setStatus(model.getStatus().name());
            if (Objects.nonNull(user)) {
                entity.setTargetUserId(user.getId());
            }
            return entity;
        }
    }
}
