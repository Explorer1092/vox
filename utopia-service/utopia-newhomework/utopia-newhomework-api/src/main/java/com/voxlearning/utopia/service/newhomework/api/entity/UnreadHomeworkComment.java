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
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于记录学生未读作业评语的数据结构。
 * 注意：这里只存储学生的未读数据结构。
 *
 * 源自于UnreadHomeworkComment，由于数据过大，仅删除数据并不能释放空间。
 * 所以挪一套新的，老表删掉，释放空间
 *
 * @author Xiaohai Zhang
 * @author xuesong.zhang
 * @serial
 * @since Oct 19, 2015
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-homework")
@DocumentCollection(collection = "unread_homework_comment_2017")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170817")
public class UnreadHomeworkComment implements Serializable {
    private static final long serialVersionUID = -354834695895373861L;

    @DocumentId private String id;
    @DocumentCreateTimestamp @DocumentField("ctt") private Date createTime;
    @DocumentField("sid") private Long studentId;
    @DocumentField("tid") private Long teacherId;
    @DocumentField("hwl") private String homeworkLocation;
    @DocumentField("cmt") private String comment;
    @DocumentField("rwd") private Integer reward;

    // ========================================================================
    // Cache support
    // ========================================================================

    public static String ck_studentId(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(UnreadHomeworkComment.class, "S", studentId);
    }

    public static String ck_studentId_count(Long studentId) {
        return CacheKeyGenerator.generateCacheKey(UnreadHomeworkComment.class, "C", studentId);
    }
}
