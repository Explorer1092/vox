package com.voxlearning.utopia.admin.productpromotion.domain.impl;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageInfo;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.PageResult;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.input.ProductPromotionExportSmsParams;
import com.voxlearning.utopia.admin.productpromotion.controller.dto.output.ProductPromotionSmsListDto;
import com.voxlearning.utopia.admin.productpromotion.dao.ProductPromotionSmsDao;
import com.voxlearning.utopia.admin.productpromotion.dao.entity.ProductPromotionSmsEntity;
import com.voxlearning.utopia.admin.productpromotion.domain.ProductPromotionSmsDomain;
import com.voxlearning.utopia.admin.productpromotion.domain.model.ProductPromotionSms;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.UserType.TEACHER;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-29 14:47
 **/
@Named
public class ProductPromotionSmsDomainImpl implements ProductPromotionSmsDomain{
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private ProductPromotionSmsDao productPromotionSmsDao;

    @Override
    public void create(ProductPromotionSms sms) {
        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(sms.getPhone(), sms.getTargetUserType());
        User user = ua == null ? null : userLoaderClient.loadUser(ua.getId());
        ProductPromotionSmsEntity entity = ProductPromotionSmsEntity.Builder.build(sms, user);
        productPromotionSmsDao.insert(entity);
    }

    @Override
    public void upsert(ProductPromotionSms sms) {
        UserAuthentication ua = userLoaderClient.loadMobileAuthentication(sms.getPhone(), sms.getTargetUserType());
        User user = ua == null ? null : userLoaderClient.loadUser(ua.getId());
        ProductPromotionSmsEntity entity = ProductPromotionSmsEntity.Builder.build(sms, user);
        productPromotionSmsDao.upsert(entity);
    }

    @Override
    public ProductPromotionSms detail(String id) {
        ProductPromotionSmsEntity entity = productPromotionSmsDao.load(id);
        return ProductPromotionSms.Builder.build(entity);
    }

    @Override
    public List<ProductPromotionSms> query(String operationUserName, PageInfo pageInfo) {
        List<ProductPromotionSmsEntity> entities = productPromotionSmsDao.query(operationUserName, pageInfo);
        return Optional.ofNullable(entities).orElse(Collections.emptyList())
                .stream().map(entity -> ProductPromotionSms.Builder.build(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductPromotionSms> queryBySendTime(Date beginTime, Date endTime) {
        return productPromotionSmsDao.query(beginTime, endTime)
                .stream()
                .map(entity -> ProductPromotionSms.Builder.build(entity))
                .collect(Collectors.toList());
    }

    @Override
    public Integer count() {
        return productPromotionSmsDao.query().size();
    }

    @Override
    public Integer count(String operationUserName) {
        return productPromotionSmsDao.count(operationUserName);
    }
}
