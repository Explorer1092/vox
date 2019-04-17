package com.voxlearning.utopia.entity.task;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * 新手任务表
 * 任务中心那一套有点复杂,新手任务比较折腾,拆开比较能放得开手脚
 * 希望没有挖坑
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-teacher-app")
@DocumentCollection(collection = "vox_teacher_rookie_task")
@UtopiaCacheRevision("20190214")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TeacherRookieTask implements CacheDimensionDocument {

    public static final String CHECK_STUDENT_SIZE = "checkStudentSize";
    public static final String HOME_WORK_ID = "homeworkId";

    private static final long serialVersionUID = -5027953634720094438L;

    @DocumentId
    private Long teacherId;
    private Long typeId;                    //  1：2019上学期的新手任务
    private String status;                  //  任务状态

    private Date receiveDate;               //  任务领取时间
    private Date expireDate;                //  任务过期时间
    @Deprecated
    private Date cancelDate;                //  任务取消时间
    private Date finishedDate;              //  任务完成时间
    private Boolean share;                  //  是否产生过分享行为(又是一个奇葩的逻辑)

    private List<SubTask> subTask;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SubTask implements java.io.Serializable {
        private static final long serialVersionUID = -5027953634720094438L;

        public SubTask(Integer index, String name) {
            this.index = index;
            this.name = name;
            this.status = Status.ONGOING.name();
            this.showProgress = false;
            this.sendReward = false;
            this.exts = new LinkedHashMap<>();
        }

        private Integer index;
        private String name;
        private String status;
        private Boolean showProgress;
        private Integer target;
        private Integer curr;
        private Boolean sendReward;
        private Integer rewardNum;
        private RookieTaskTrigger trigger;
        private Map<String, Object> exts; // 拓展属性
        //private String buttonName;

        public boolean fetchFinished() {
            return Objects.equals(status, Status.FINISHED.name());
        }

        public boolean fetchOnGoing() {
            return Objects.equals(status, Status.ONGOING.name());
        }
    }

    public enum RookieTaskTrigger {
        ASSIGN_HOMEWORK,
        CHECK_HOMEWORK,
        COMMENT_STUDENT,
        SHARE_HOMEWORK
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ckTeacherIdTypeId(this.teacherId, this.typeId)
        };
    }

    private static String ckTeacherIdTypeId(Long teacherId, Long typeId) {
        return CacheKeyGenerator.generateCacheKey(
                TeacherRookieTask.class,
                new String[]{"TID", "TYID"},
                new Object[]{teacherId, typeId}
        );
    }

    public enum Status {
        ONGOING,    // 进行中
        FINISHED,   // 已经完成
        EXPIRED,    // 过期
        CANCEL,     // 取消
        NEXT,       // 这个是展示前端用的(新手任务当前要做的任务是这个状态)
    }

    public boolean fetchOnGoing() {
        return Objects.equals(this.status, Status.ONGOING.name());
    }

    public boolean fetchFinished() {
        return Objects.equals(this.status, Status.FINISHED.name());
    }
}
