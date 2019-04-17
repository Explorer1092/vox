package com.voxlearning.utopia.entity.task;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.CollectionUtils;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

import static com.voxlearning.utopia.entity.task.TeacherTask.Status.INIT;
import static com.voxlearning.utopia.entity.task.TeacherTask.Status.ONGOING;

/**
 * 结构需要与TeacherTaskProgress保持一致
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-teacher-app")
@DocumentCollection(collection = "vox_teacher_task_progress_log")
@UtopiaCacheRevision("20181023")
public class TeacherTaskProgressLog implements CacheDimensionDocument{

    private static final long serialVersionUID = -5919405806087169016L;
    @DocumentId private String id;
    private Long teacherId;
    private Long taskId;                                    // 关联的任务ID，ID为vox_teacher_task表中的主键ID
    private Long tplId;                                     // 任务模板ID，ID为vox_teacher_task_tpl表中的主键ID
    private String status;                                  // 状态，任务的整体状态
    private List<SubTaskProgress> subTaskProgresses;        // 子任务进度
    private Long expireTime;                                // 过期时间
    private List<Reward> rewards;                           // 任务的奖励数据，一个任务可能会有多种奖励，积分、园丁豆、话费
    private Map<String,Object> extAttr;                     // 以防万一，额外添加的属性
    private String receiveDate;                             // 领取任务的时间yyyy-MM-dd HH:mm:ss
    private Long receiveTime;                               // 领取任务的时间戳

    @DocumentCreateTimestamp private Long createTime;       //创建时间
    @DocumentUpdateTimestamp private Long updateTime;       //上次修改事件

    /** 奖励 **/
    @Getter
    @Setter
    public static class Reward implements Serializable {
        private static final long serialVersionUID = -7815243747386731922L;
        private Long id;                    // ID, 关联着TeacherTaskTpl.Reward.id
        private Boolean open;               // 开启状态，有些奖励在领取的时候计算不出来，true表示不用计算，直接根据[value]字段发就行，false，需要根据tpl表中的expression重新计算出来，需要到任务达标的时候再计算发放
        private Integer value;              // 奖励数值
        private String unit;                // 奖励类型，积分：exp，园丁豆：integral，现金：cash
        private Boolean received;           // 是否领取的标志，true：已领取，false：未领取
        private Map<String,Object> extAttr; // 以防万一，额外添加的属性
    }

    /** 根据进度发奖励 **/
    @Getter
    @Setter
    public static class ProgressReward implements Serializable {
        private static final long serialVersionUID = 2204833892980623234L;
        private Long id;                    // ID, 关联着TeacherTaskTpl.Reward.id
        private Boolean open;               // 开启状态，有些奖励在领取的时候计算不出来，true表示不用计算，直接根据[value]字段发就行，false，需要根据tpl表中的expression重新计算出来，需要到任务达标的时候再计算发放
        private Integer value;              // 每完成一个进度的奖励数值，即每个进度的奖励
        private String unit;                // 奖励类型，积分：exp，园丁豆：integral，现金：cash
        private Integer receivedValue;      // 已经领取了多少奖励
        private Integer receivedCurr;       // 已经领取的奖励进度
        private Map<String,Object> extAttr; // 以防万一，额外添加的属性
    }

    /**
     * 进度信息
     */
    @Getter
    @Setter
    public static class Progress implements Serializable {
        private static final long serialVersionUID = 938756611785956403L;
        private Integer target;                 //目标进度值
        private Integer curr;                   //当前进度值
        private String q;                       //单位，主要作为显示
        private List<ProgressReward> rewards;   //任务的奖励数据，一个任务可能会有多种奖励，积分、园丁豆、话费
        private Map<String,Object> extAttr;     // 以防万一，额外添加的属性
    }

    /** 子任务 **/
    @Getter
    @Setter
    public static class SubTaskProgress implements Serializable{
        private static final long serialVersionUID = 5915845531863377397L;
        private Long id;                        //关联的是tpl表中子任务的id
        private String status;                  //子任务的状态
        private Map<String,Object> vars;        //任务的过程数据
        private Progress progress;              //任务的进度的相关数据，存目标数、完成数，量词等
        private List<Reward> rewards;           //任务的奖励数据，一个任务可能会有多种奖励，积分、园丁豆、话费
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{newCacheKey("TEACHER_ID",teacherId)};
    }

}
