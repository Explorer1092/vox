/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.zone.api.constant.GiftCategory;
import com.voxlearning.utopia.service.zone.mdb.MDBGift;
import lombok.Getter;
import lombok.Setter;

/**
 * FIXME:这张表中的gold字段和silver字段可能为null，0表示礼物免费。
 * FIXME:注意用晓光写的对象更新的方式，不能把一个非null字段更新成null。
 * <p>
 * FIXME: http://project.17zuoye.net/redmine/issues/8680
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @serial
 * @since Sep 2, 2013
 */
@Getter
@Setter
@DocumentConnection(configName = "main")
@DocumentTable(table = "VOX_GIFT")
public class Gift extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {
    private static final long serialVersionUID = 7605883354724703005L;

    private String name;
    private Integer gold;
    private Integer silver;
    private String imgUrl;
    private GiftCategory giftCategory;
    private Boolean studentAvailable;
    private Boolean teacherAvailable;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }

    @JsonIgnore
    public boolean isAvailableForStudent() {
        return Boolean.TRUE.equals(studentAvailable);
    }

    @JsonIgnore
    public boolean isAvailableForTeacher() {
        return Boolean.TRUE.equals(teacherAvailable);
    }

    @JsonIgnore
    public boolean isNeedPay() {
        return silver != null && silver > 0;
    }

    public MDBGift transform() {
        MDBGift t = new MDBGift();
        t.setId(id);
        t.setCreateDatetime(createDatetime);
        t.setUpdateDatetime(updateDatetime);
        t.setDisabled(getDisabled());
        t.setName(name);
        t.setGold(gold);
        t.setSilver(silver);
        t.setImgUrl(imgUrl);
        t.setGiftCategory(giftCategory);
        t.setStudentAvailable(studentAvailable);
        t.setTeacherAvailable(teacherAvailable);
        return t;
    }
}