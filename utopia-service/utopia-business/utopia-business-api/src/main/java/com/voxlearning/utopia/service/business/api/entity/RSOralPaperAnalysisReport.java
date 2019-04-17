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

package com.voxlearning.utopia.service.business.api.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.utopia.service.business.api.entity.embedded.RSOralPaperAnalysisReportPattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author changyuan.liu
 * @since 2015/5/18
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-rstaff")
@DocumentCollection(collection = "rs_oral_paper_analysis_report")
@DocumentIndexes({
        @DocumentIndex(def = "{'pid':1}", background = true)
})
@Deprecated
public class RSOralPaperAnalysisReport implements Serializable {
    private static final long serialVersionUID = -5871439048632115868L;

    @DocumentId private String id;
    @DocumentField("pid") private Long pushId;                                              // 教研员口语试卷id （push id）
    @DocumentField("schid") private Long schoolId;                                          // 学校id
    @DocumentField("schn") private String schoolName;                                       // 学校名称
    @DocumentField("stunum") private Integer studentCount;                                  // 完成人数
    @DocumentField("allnum") private Integer totalCount;                                    // 应完成人数
    @DocumentField("pats") private Map<String, RSOralPaperAnalysisReportPattern> patterns;  // 题型数据
    @DocumentField("ct") @DocumentCreateTimestamp private Date createAt;                    // 创建时间
    @DocumentField("ut") @DocumentUpdateTimestamp private Date updateAt;                    // 更新时间

    public String generateKeyByPushIdAndSchoolId() {
        return pushId + "_" + schoolId;
    }
}
