/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.reward.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by XiaoPeng.Yang on 14-7-21.
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_reward")
@DocumentTable(table = "VOX_REWARD_INDEX")
public class RewardIndex implements CacheDimensionDocument {
    private static final long serialVersionUID = 4909185034891194006L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;
    @DocumentCreateTimestamp
    @DocumentField("CREATE_DATETIME") private Date createDatetime;
    @DocumentField private Long productId;
    @DocumentField private String location;
    @DocumentField private String indexType;
    @DocumentField private Integer displayOrder;

    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
