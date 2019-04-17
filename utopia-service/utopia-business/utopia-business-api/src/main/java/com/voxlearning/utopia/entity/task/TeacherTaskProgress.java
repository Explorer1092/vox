package com.voxlearning.utopia.entity.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * 结构需要与TeacherTaskProgressLog保持一致
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-teacher-app")
@DocumentCollection(collection = "vox_teacher_task_progress")
@UtopiaCacheRevision("20181026")
public class TeacherTaskProgress implements CacheDimensionDocument{

    private static final long serialVersionUID = 2448840938227615438L;

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
        private static final long serialVersionUID = 3253539357379644159L;
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
        private static final long serialVersionUID = -8891964168181671286L;
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
        private static final long serialVersionUID = 3006520481660205512L;
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

        private static final long serialVersionUID = 2270367325949818878L;

        private Long id;                        //关联的是tpl表中子任务的id
        private String status;                  //子任务的状态
        private Map<String,Object> vars;        //任务的过程数据
        private Progress progress;              //任务的进度的相关数据，存目标数、完成数，量词等
        private List<Reward> rewards;           //任务的奖励数据，一个任务可能会有多种奖励，积分、园丁豆、话费

        @DocumentFieldIgnore
        @JsonIgnore
        public Map<String,Object> getVarMap(){
            if(this.vars == null)
                this.vars = new HashMap<>();

            return this.vars;
        }

        public void finish(){
            this.status = TeacherTask.Status.FINISHED.name();
        }

        @DocumentFieldIgnore
        public boolean isFinish(){
            return Objects.equals(this.status, TeacherTask.Status.FINISHED.name());
        }

        public void addReward(Long id,
                              String unit,
                              Integer value,
                              Boolean open) {
            Reward reward = new Reward();
            reward.id = id;
            reward.open = open;
            reward.unit = unit;
            reward.received = false;
            reward.value = value;

            if(CollectionUtils.isEmpty(rewards))
                rewards = new ArrayList<>();

            this.rewards.add(reward);
        }

        public List<Reward> getRewards(){
            return Optional.ofNullable(rewards).orElse(Collections.emptyList());
        }
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{newCacheKey("TEACHER_ID",teacherId)};
    }

    public static String ck_teacher(Long teacherId) {
        return CacheKeyGenerator.generateCacheKey(TeacherTaskProgress.class,"TEACHER_ID", teacherId);
    }

    public SubTaskProgress getSubTaskProgress(Long id){
        return Optional.ofNullable(subTaskProgresses)
                .orElse(Collections.emptyList())
                .stream()
                .filter(tp -> Objects.equals(tp.id,id))
                .findFirst()
                .orElseGet(null);
    }

    public void forEachSubProgress(Consumer<SubTaskProgress> consumer){
        Optional.ofNullable(subTaskProgresses).orElse(Collections.emptyList()).forEach(consumer);
    }

    @DocumentFieldIgnore
    public boolean isFinish(){
        return Objects.equals(this.status, TeacherTask.Status.FINISHED.name());
    }

    @DocumentFieldIgnore
    public boolean isExpired(){
        return Objects.equals(this.status, TeacherTask.Status.EXPIRED.name());
    }

    @DocumentFieldIgnore
    public boolean isOnGoing(){
        return Objects.equals(this.status, TeacherTask.Status.ONGOING.name());
    }

    @DocumentFieldIgnore
    public int getSubTaskNum(){
        return subTaskProgresses == null ? 0 : subTaskProgresses.size();
    }

    public boolean addSubTaskProgress(SubTaskProgress stp){
        if(CollectionUtils.isEmpty(subTaskProgresses)){
            subTaskProgresses = new ArrayList<>();
        }

        return this.subTaskProgresses.add(stp);
    }

    public void setStatus(String status){
        // 如果是从INIT变成ONGOING，则子任务要一起变
        if(Objects.equals(INIT.name(),this.status) && Objects.equals(ONGOING.name(),status)){
            this.subTaskProgresses.forEach(stp -> stp.setStatus(ONGOING.name()));
        }

        this.status = status;
    }

    @DocumentFieldIgnore
    public SubTaskProgress getOngoingSubTask(){
        return Optional.ofNullable(subTaskProgresses)
                .orElse(Collections.emptyList())
                .stream()
                .filter(stp -> Objects.equals(stp.getStatus(),ONGOING.name()))
                .findFirst()
                .orElse(null);
    }

    @DocumentFieldIgnore
    @Deprecated
    public TeacherTaskProgress.Progress getProgress(TeacherTaskTpl tpl){
        // 如果有多个子任务则不显示进度
        if(CollectionUtils.isEmpty(subTaskProgresses) || subTaskProgresses.size() > 1) {
            return null;
        }
        if (tpl.getSubTaskList().get(0).getShowProgress()) {
            return subTaskProgresses.get(0).getProgress();
        } else {
            return null;
        }
    }

}
