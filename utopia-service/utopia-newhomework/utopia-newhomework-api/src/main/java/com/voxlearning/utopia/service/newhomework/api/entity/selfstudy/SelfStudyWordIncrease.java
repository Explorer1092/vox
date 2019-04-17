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

package com.voxlearning.utopia.service.newhomework.api.entity.selfstudy;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xuesong.zhang
 * @since 2017/2/15
 */
@Getter
@Setter
@DocumentConnection(configName = "homework")
@DocumentTable(table = "VOX_SELFSTUDY_WORDS_INCREASE")
public class SelfStudyWordIncrease implements Serializable {
    private static final long serialVersionUID = 673433393716348268L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;
    @DocumentField("SUBJECT") private Subject subject;
    @DocumentField("CLAZZ_GROUP_ID") private Long clazzGroupId;
    @DocumentField("BOOK_ID") private String bookId;
    @DocumentField("UNIT_ID") private String unitId;
    @DocumentField("KNOWLEDGE_POINT_ID") private String knowledgePointId;
}
