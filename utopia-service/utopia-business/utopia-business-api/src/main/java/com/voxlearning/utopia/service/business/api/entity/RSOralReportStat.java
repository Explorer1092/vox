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

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.core.util.CollectionUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-rstaff")
@DocumentCollection(collection = "rs_oral_report_stat_{}", dynamic = true)
@DocumentIndexes({
        @DocumentIndex(def = "{'taskId':1,'docId':1,'acode':1,'schoolId':1}", unique = true, background = true),
        @DocumentIndex(def = "{'taskId':1,'docId':1,'ccode':1}", background = true),
})
@DocumentRangeable(range = DateRangeType.M)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151201")
public class RSOralReportStat implements Serializable {

    private static final long serialVersionUID = -187837560407393705L;

    @DocumentId private String id;
    @DocumentField("createday") private String createday;                      // 试卷有效期的结束时间
    @DocumentField("taskId") private String taskId;                            // 任务ID
    @DocumentField("docId") private String docId;                              // 试卷DOC_ID
    @DocumentField("ccode") private Integer ccode;                             // 市编码
    @DocumentField("cname") private String cname;                              // 市名称
    @DocumentField("acode") private Integer acode;                             // 区域编码
    @DocumentField("aname") private String aname;                              // 区域名称
    @DocumentField("schoolId") private Long schoolId;                          // 学校ID
    @DocumentField("schoolName") private String schoolName;                    // 学校名称
    @DocumentField("parts") private List<PartDetail> parts;                    // 模块
    @DocumentField("joinNum") private Integer joinNum;                         // 参与人数
    @DocumentField("stuNum") private Integer stuNum;                           // 学生人数
    @DocumentField("ct") @DocumentCreateTimestamp private Date createAt;       // 创建时间

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"title", "score"})
    public static class PartDetail implements Serializable {
        private static final long serialVersionUID = 5363916623926018296L;

        @DocumentField("title") private String title;  //模块名称
        @DocumentField("score") private Integer score; //
    }

    public int fetchTotalScore() {
        if (CollectionUtils.isEmpty(parts)) {
            return 0;
        }
        return parts.stream().mapToInt(PartDetail::getScore).sum();
    }

    public Map<String, Integer> fetchPartsMap() {
        if (CollectionUtils.isEmpty(parts)) {
            return new LinkedHashMap<>();
        }
        Map<String, Integer> partMap = new LinkedHashMap<>();
        for (PartDetail partDetail : parts) {
            if (!partMap.containsKey(partDetail.getTitle())) {
                partMap.put(partDetail.getTitle(), partDetail.getScore());
            }
        }
        return partMap;
    }
}
