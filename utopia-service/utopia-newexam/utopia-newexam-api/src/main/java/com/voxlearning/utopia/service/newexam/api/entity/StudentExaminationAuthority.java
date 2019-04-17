package com.voxlearning.utopia.service.newexam.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentClient;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@DocumentClient
@DocumentDatabase(database = "vox-newexam")
@DocumentCollection(collection = "student_examination_authority_{}", dynamic = true)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170710")
//重考补考权限表
//1.ID和newExamResult表ID一致
//当disabled is false 的时候权限有限
public class StudentExaminationAuthority implements Serializable {
    private static final long serialVersionUID = -12272678987246039L;
    @DocumentId
    private String id;
    @DocumentCreateTimestamp
    private Date createAt;                                      // 生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;                                      // 更新时间
    private Boolean disabled;                                   // 默认false，删除true

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"month", "subject", "eid", "userId"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ID implements Serializable {

        private String month;
        private Subject subject;
        private String eid;
        private String userId;

        @Override
        public String toString() {
            return month + "-" + subject + "-" + eid + "-" + userId;
        }
    }

    public ID parseID() {
        if (id == null || id.trim().length() == 0) return null;
        String[] segments = id.split("-");
        if (segments.length != 4) return null;
        String month = segments[0];
        Subject subject = Subject.safeParse(segments[1]);
        String hid = segments[2];
        String uid = segments[3];
        return new ID(month, subject, hid, uid);
    }

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(StudentExaminationAuthority.class, id);
    }
}
