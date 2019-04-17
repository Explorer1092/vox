package com.voxlearning.utopia.service.newhomework.api.entity.vacation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2016/11/24
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_MONGO_CONFIG_NAME)
@DocumentDatabase(database = "vox-winter-vacation-2019")
@DocumentCollection(collection = "vacation_homework_package")
@DocumentIndexes({
        @DocumentIndex(def = "{'teacherId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'subject':1}", background = true),
        @DocumentIndex(def = "{'clazzGroupId':1,'createAt':-1}", background = true),
        @DocumentIndex(def = "{'bookId':1}", background = true),
        @DocumentIndex(def = "{'createAt':-1}", background = true),
        @DocumentIndex(def = "{'disabled':1}", background = true)
})
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181128")
public class VacationHomeworkPackage implements Serializable {

    private static final long serialVersionUID = 7744781484086897311L;

    @DocumentId
    private String id;                      // 主键
    private String actionId;                // 布置id，"teacherId_${批量布置时间点}"。大数据专用属性
    private Subject subject;                // 学科
    private HomeworkSourceType source;      // 作业的布置来源
    private Long teacherId;                 // 老师id
    private Long clazzGroupId;              // 班组id
    private String remark;                  // 备注
    private Integer plannedDays;               // 计划天数
    private String bookId;                  // 课本id
    private Date startTime;                 // 作业布置的开始时间
    private Date endTime;                   // 假期作业的结束时间
    private Boolean disabled;               // 默认false，删除true
    private String detail;                  // 作业详情

    @DocumentCreateTimestamp
    private Date createAt;                  // 作业生成时间
    @DocumentUpdateTimestamp
    private Date updateAt;                  // 作业更新时间

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(VacationHomeworkPackage.class, id);
    }

    public static String ck_clazzGroupId(Long clazzGroupId) {
        return CacheKeyGenerator.generateCacheKey(VacationHomeworkPackage.class, "CG", clazzGroupId);
    }

    public static String ck_teacherId(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(VacationHomeworkPackage.class, "TID", teacherId);
    }


    @JsonIgnore
    public boolean isDisabledTrue() {
        return Boolean.TRUE.equals(disabled);
    }

    public VacationHomeworkPackage.Location toLocation() {
        VacationHomeworkPackage.Location location = new VacationHomeworkPackage.Location();
        location.id = id;
        location.teacherId = (teacherId == null ? 0 : teacherId);
        location.clazzGroupId = (clazzGroupId == null ? 0 : clazzGroupId);
        location.remark = remark;
        location.plannedDays = plannedDays;
        location.bookId = bookId;
        location.createTime = (createAt == null ? 0 : createAt.getTime());
        location.startTime = (startTime == null ? 0 : startTime.getTime());
        location.endTime = (endTime == null ? 0 : endTime.getTime());
        location.actionId = actionId;
        location.subject = subject;
        location.disabled = (disabled == null ? false : disabled);
        return location;
    }

    @Getter
    @Setter
    @EqualsAndHashCode(of = "id")
    @ToString
    public static class Location implements Serializable {
        private static final long serialVersionUID = -1082171328746381069L;
        private String id;
        private long teacherId;
        private long clazzGroupId;
        private String remark;
        private Integer plannedDays;
        private String bookId;
        private long createTime;
        private long startTime;
        private long endTime;
        private String actionId;
        private Subject subject;
        private Boolean disabled;               // 默认false，删除true
    }

}
