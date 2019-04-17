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

package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/2/29
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-newhomework")
@DocumentCollection(collection = "teacher_assignment_record")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20161101")
public class TeacherAssignmentRecord implements Serializable {
    private static final long serialVersionUID = -1129976879395516972L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;
    private Long userId;                                    // 用户Id
    private String bookId;                                  // 课本Id
    private Integer year;                                   // 学年
    private Term term;                                      // 上下学期
    private Subject subject;                                // 科目
    private Map<String, Integer> questionInfo;              // 布置作业试题次数 (试题Id,库里面存的是docId 布置次数)
    private Map<String, Integer> packageInfo;               // 布置作业精选包次数 (精选包Id 布置次数)
    private Map<String, Integer> paperInfo;                 // 布置作业试卷次数 (试卷Id,库里面存的是docId 布置次数)
    private Map<String, Integer> appInfo;                   // 布置作业应用次数 (lessonId + "-" + categoryId 布置次数)
    private Map<String, Integer> pictureBookInfo;           // 布置作业绘本次数 (pictureBookId 布置次数)
    private Map<String, Integer> mentalKpInfo;              // 布置作业口算知识点次数 (kpId 布置次数)
    @DocumentCreateTimestamp
    private Long createTimestamp;                           // 创建时间
    @DocumentUpdateTimestamp
    private Long updateTimestamp;                           // 修改时间

    public void initializeIfNecessary() {
        if (getQuestionInfo() == null) {
            setQuestionInfo(new HashMap<>());
        }
        if (getPackageInfo() == null) {
            setPackageInfo(new HashMap<>());
        }
        if (getPaperInfo() == null) {
            setPaperInfo(new HashMap<>());
        }
        if (getAppInfo() == null) {
            setAppInfo(new HashMap<>());
        }
        if (getPictureBookInfo() == null) {
            setPictureBookInfo(new HashMap<>());
        }
        if (getMentalKpInfo() == null) {
            setMentalKpInfo(new HashMap<>());
        }
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(TeacherAssignmentRecord.class, id);
    }

    /**
     * 题目id或试卷id转docId
     * 去掉最后一个中划线后面的版本号
     */
    public static String id2DocId(String id) {
        if (id == null || id.trim().length() == 0) {
            return null;
        } else {
            int index = id.lastIndexOf("-");
            if (index != -1) {
                return id.substring(0, index);
            } else {
                return id;
            }
        }
    }
}