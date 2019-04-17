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

package com.voxlearning.utopia.entity.crm;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * CrmReviewResult
 *
 * @author song.wang
 * @date 2016/7/5
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_review_result")
public class CrmReviewResult implements Serializable, TimestampTouchable {

    private static final long serialVersionUID = 5010248043028834883L;

    @DocumentId
    private String id;
    private Long schoolId;                       // 学校ID
    private String schoolName;                   // 学校名schoolPhase
    private String reviewUser;                   // 回访人
    private String reviewUserName;               // 回访人姓名
    private Boolean gradeDistributionFlag;       // 年级分布是否一致
    private Boolean schoolingLengthFlag;         // 学制是否一致
    private Boolean externOrBoarderFlag;         // 走读是否一致
    private Boolean englishStartGradeFlag;       // 英语起始年级是否一致
    private Boolean schoolSizeFlag;              // 学校规模（学生数量）是否一致
    private Boolean branchSchoolIdsFlag;         // 关联分校是否一致
    private List<CrmReviewResultDetail> resultDetailList;
    private Date createTime;
    private Date updateTime;

    @Override
    public void touchCreateTime(long timestamp) {
        if (createTime == null) {
            createTime = new Date(timestamp);
        }
    }

    @Override
    public void touchUpdateTime(long timestamp) {
        updateTime = new Date(timestamp);
    }

    @Getter
    @Setter
    public static class CrmReviewResultDetail implements Serializable{
        private String gradeDistribution;            // 年级分布
        private Integer schoolingLength;             // 1：5年制 、2： 6年制 、3： 3年制 4：4年制
        private Integer externOrBoarder;             // 走读 or 寄宿 1:走读 、2:寄宿、3 走读/寄宿
        private Integer englishStartGrade;           // 英语起始年级
        private Integer schoolSize;                  // 学校规模（学生数量）
        private Integer gradeClassCount;             // 本年级班级数
        private Integer classStudentCount;           // 本班人数
        private Set<Long> branchSchoolIds;           // 分校ID
        public CrmReviewResultDetail(){

        }
    }

}
