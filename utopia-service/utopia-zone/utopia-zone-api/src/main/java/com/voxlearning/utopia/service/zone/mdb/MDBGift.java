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

package com.voxlearning.utopia.service.zone.mdb;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentDDL;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.service.zone.api.constant.GiftCategory;
import com.voxlearning.utopia.service.zone.api.entity.Gift;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentConnection(configName = "utopia")
@DocumentTable(table = "MDB_GIFT")
@DocumentDDL(path = "ddl/mdb/MDB_GIFT.ddl")
public class MDBGift implements Serializable {
    private static final long serialVersionUID = -1286500303592492765L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private Long id;
    private Date createDatetime;
    private Date updateDatetime;
    private Boolean disabled;
    private String name;
    private Integer gold;
    private Integer silver;
    private String imgUrl;
    private GiftCategory giftCategory;
    private Boolean studentAvailable;
    private Boolean teacherAvailable;

    public Gift transform() {
        Gift t = new Gift();
        t.setId(id);
        t.setCreateDatetime(createDatetime);
        t.setUpdateDatetime(updateDatetime);
        t.setDisabled(disabled);
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