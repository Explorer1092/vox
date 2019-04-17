package com.voxlearning.utopia.mapper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TeacherTaskEntry implements Serializable{

    private static final long serialVersionUID = 8057933581108846255L;

    private Long taskId;
    private String name;
    private String buttonName;
    private String actionName;
    private Integer sort;
    private String desc;
    private Progress progress;
    private String status;
    private List<Reward> rewardList;
    private String instruction;
    private boolean showSubTask;
    private Map<String,Object> skip;
    private String deadline;
    private String type;
    private long subTaskFinishedNum;
    private Map<String, Object> taskParams;
    private long taskTplId;
    private List<TeacherTaskProgress.SubTaskProgress> subTaskProgressList;
    private String receiveDate;     // 领取任务的时间yyyy-MM-dd HH:mm:ss
    private String finishDate;      // 完成任务的时间yyyy-MM-dd HH:mm:ss
    private String expireDate;      // 任务过期的时间yyyy-MM-dd HH:mm:ss
    private boolean autoReceive;
    private boolean cycle;
    private String cycleUnit;
    private String rewardPrefix;    // 奖励前缀(“每邀请1人奖励”)
    private String progressPrefix;  // 进度前缀(已唤醒 已邀请)
    private String tipText;         // “限时奖励翻倍”

    /** 以下为CRM专供使用 **/
    private List<CrmProgress> crmProgressList;
    private boolean crmIsDisplay;
    private List<String> crmTaskDesc;

    public void addReward(String content, String unit, String quantity) {
        if (CollectionUtils.isEmpty(rewardList)) {
            rewardList = new ArrayList<>();
        }

        Reward reward = new Reward();
        reward.content = content;
        reward.unit = unit;
        reward.quantity = quantity;

        this.rewardList.add(reward);
    }

    @Setter
    @Getter
    @EqualsAndHashCode(of = {"unit","quantity"})
    public static class Reward implements Serializable {
        private static final long serialVersionUID = 1544223073283252443L;
        private String content;
        private String unit;
        private String quantity;
    }

    @Getter
    @Setter
    public static class Progress implements Serializable {
        private static final long serialVersionUID = 6525537232170193792L;
        private Integer target;                 //目标进度值
        private Integer curr;                   //当前进度值
        private String q;                       //单位，主要作为显示
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CrmProgress implements  Serializable {
        private static final long serialVersionUID = -1041214152769500442L;
        private String desc;
        private Integer target;
        private Integer curr;
        private String Q;
    }
}
