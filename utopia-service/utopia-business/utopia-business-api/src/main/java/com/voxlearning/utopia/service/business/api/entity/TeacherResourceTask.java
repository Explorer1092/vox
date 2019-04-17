package com.voxlearning.utopia.service.business.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 教学资源任务
 */
@Getter
@Setter
@DocumentDatabase(database = "vox_o2o")
@DocumentCollection(collection = "vox_teacher_resource_task")
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190319")
public class TeacherResourceTask implements Serializable {
    private static final long serialVersionUID = -7246127492421057979L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.OBJECT_ID)
    private String id;

    @DocumentField("userId")
    private Long userId;

    @DocumentField("resourceId")
    private String resourceId;

    @DocumentField("task")
    private String task;

    @DocumentField("status")
    private String status;

    @DocumentField("expiryDate")
    private Date expiryDate;

    @DocumentCreateTimestamp
    @DocumentField("createAt")
    private Date createAt;

    @DocumentUpdateTimestamp
    private Date updateAt;

    @Getter
    @AllArgsConstructor
    public enum Status {
        INIT("未领取"),
        ONGOING("进行中"),
        FINISH("已完成"),
        EXPIRED("已过期");

        private String desc;

        public static Status safeValueOf(String name) {
            for (Status value : Status.values()) {
                if (Objects.equals(name, value.name())) {
                    return value;
                }
            }
            return Status.INIT;
        }
    }

    public static String ck_uid(Long userId) {
        return CacheKeyGenerator.generateCacheKey(TeacherResourceTask.class, "USER_ID", userId);
    }

    public Status convertViewStatus() {
        if (StringUtils.isBlank(status) || Objects.equals(status, Status.EXPIRED.name())) {
            return Status.INIT;
        }
        return Status.safeValueOf(status);
    }

    public String getLeftTimeExpr() {
        if (this.expiryDate == null) {
            return null;
        }
        Date now = new Date();
        long leftTime = DateUtils.dayDiff(this.expiryDate, now);
        String leftTimeExpr = leftTime + "天";

        // 小于两天用小于表示
        if (leftTime < 2) {
            leftTime = DateUtils.hourDiff(this.expiryDate, now);
            leftTimeExpr = leftTime + "小时";
        }

        return leftTimeExpr;
    }
}
