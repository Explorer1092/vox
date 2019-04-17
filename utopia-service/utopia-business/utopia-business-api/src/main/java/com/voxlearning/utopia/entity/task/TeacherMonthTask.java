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
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 月活跃任务
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-teacher-app")
@DocumentCollection(collection = "vox_teacher_month_task")
@UtopiaCacheRevision("20190214")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TeacherMonthTask implements CacheDimensionDocument {

    private static final long serialVersionUID = -5027953634720094438L;


    public static TeacherMonthTask newInstance(Long teacherId) {
        Date now = new Date();
        TeacherMonthTask teacherMonthTask = new TeacherMonthTask();
        teacherMonthTask.setStatus(TeacherMonthTask.Status.ONGOING.name());
        teacherMonthTask.setTeacherId(teacherId);
        teacherMonthTask.setCreateTime(now);
        teacherMonthTask.setRestartDate(now);
        teacherMonthTask.setExpireDate(MonthRange.current().getEndDate());
        teacherMonthTask.setReceiveDate(now);
        teacherMonthTask.setGroups(new ArrayList<>());
        return teacherMonthTask;
    }

    public void restartTask() {
        Date now = new Date();
        this.setRestartDate(now);
        this.setExpireDate(MonthRange.current().getEndDate());
        this.setGroups(new ArrayList<>());
    }

    @DocumentId
    private Long teacherId;
    private String status;                  //  任务状态

    private Date receiveDate;               //  任务领取时间
    private Date expireDate;                //  任务过期时间(月底最后一秒,第二月load时会重置整条数据)
    private Date cancelDate;                //  任务取消时间
    private Date finishedDate;              //  任务完成时间
    private Date restartDate;              //  任务完成时间

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    private List<GroupDetail> groups;       // 每个班级都有3个子任务

    @Getter
    @Setter
    public static class GroupDetail implements java.io.Serializable {
        private static final long serialVersionUID = -5027953634720094438L;

        public GroupDetail() {
            this.homework = new ArrayList<>();
            this.first = false;
            this.second = false;
            this.third = false;
            homework = new ArrayList<>();
        }

        //private Long groupId;
        private Long clazzId;
        private Integer clazzLevel;
        private String clazzName;

        private List<Homework> homework;

        private Boolean first;  // 第一个子任务是否完成
        private Boolean second; // 第二个子任务是否完成
        private Boolean third;  // 第三个子任务是否完成
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Homework implements java.io.Serializable {
        private static final long serialVersionUID = -5027953634720094438L;

        private String id;              // 作业ID
        private Integer studentCount;   // 学生完成情况
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                ckTeacherIdTypeId(this.teacherId)
        };
    }

    private static String ckTeacherIdTypeId(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(
                TeacherMonthTask.class,
                teacherId
        );
    }

    public enum Status {
        ONGOING,    // 进行中
        FINISHED,   // 已经完成
        EXPIRED,    // 过期
        CANCEL,     // 取消
    }

}
