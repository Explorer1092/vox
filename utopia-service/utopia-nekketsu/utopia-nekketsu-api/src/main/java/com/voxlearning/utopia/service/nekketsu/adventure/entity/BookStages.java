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

package com.voxlearning.utopia.service.nekketsu.adventure.entity;


import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UseEqualsValidateCache;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 人、教材、关卡信息
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/15 18:38
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-nekketsu-adventure")
@DocumentCollection(collection = "vox_adventure_bookstages")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20151026")
@EqualsAndHashCode(of = "id")
@UseEqualsValidateCache
public class BookStages implements Serializable, Cloneable {
    private static final long serialVersionUID = 5380698754636606622L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE) private String id;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;
    private Long userId;                                //用户ID
    private Long bookId;                                //当前教材ID
    private Integer currentStage;                       //当前关卡
    private Integer openedStage;                        //已解锁的最大关卡
    private Map<Integer, Stage> stages;                 //关卡挑战信息

    public static String generateId(Long userId, Long bookId) {
        return userId + "_" + bookId;
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(BookStages.class, id);
    }

    @Override
    public BookStages clone() {
        try {
            return (BookStages) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException();
        }
    }

    // 将stages里面的数据放到stageList中，并将stages赋值为null
    @DocumentFieldIgnore
    public List<Stage> getStageList() {
        if (getStages() == null) {
            return new LinkedList<>();
        }
        return new LinkedList<>(getStages().values());
    }
}
