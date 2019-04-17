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

package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.*;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by jiangpeng on 16/7/15.
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-jxt")
@DocumentDatabase(database = "vox-jxt")
@DocumentCollection(collection = "parent_share_text_read")
@DocumentIndexes({
        @DocumentIndex(def = "{'parent_id':1,'file_md5':1}", unique = true,background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160723")
public class ParentShareTextRead implements Serializable {


    private static final long serialVersionUID = -7961787605558977392L;

    @DocumentId
    private String id;

    @DocumentField("parent_id")
    private Long parentId;     //上传的家长id

    @DocumentField("paragraph_id")
    private String paragraphId;     //上传对应的段落id

    @DocumentField("student_id")
    private Long studentId;        //学生id

    @DocumentField("file_md5")
    private String fileMd5;         //文件md5值

    @DocumentField("voice_url")
    private String voiceUrl;           //语音文件url

    @DocumentField("duration")
    private Integer duration;        //语音时长


    @DocumentField("ct")
    @DocumentCreateTimestamp
    private Date createDate;

    @DocumentField("up")
    @DocumentUpdateTimestamp
    private Date updateDate;


    @DocumentFieldIgnore
    public ParentIdMd5 getParentIdMd5(){
        return new ParentIdMd5(this.getParentId(),this.getFileMd5());
    }

    public static ParentShareTextRead newInstance(Long parentId,Long studentId,String paragraphId,String fileMd5,Integer duration){
        ParentShareTextRead instance = new ParentShareTextRead();
        instance.setParentId(parentId);
        instance.setStudentId(studentId);
        instance.setParagraphId(paragraphId);
        instance.setFileMd5(fileMd5);
        instance.setDuration(duration);
        return instance;
    }

    public static String generateCacheKey(ParentIdMd5 parentIdMd5) {
        return CacheKeyGenerator.generateCacheKey(ParentShareTextRead.class, new String[]{"PID", "MD5"}, new Object[]{parentIdMd5.getParentId(), parentIdMd5.getFileMd5()});
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"parentId", "fileMd5"})
    @Data
    public static class ParentIdMd5 implements Serializable {
        private static final long serialVersionUID = -7662412194135898342L;
        private Long parentId;
        private String fileMd5;
    }
}
