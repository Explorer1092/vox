package com.voxlearning.utopia.mapper;

import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TeacherSubTaskEntry implements Serializable{

    private static final long serialVersionUID = -1228336651711185405L;

    private Long id;
    private String name;
    private String status;
    private Progress progress;
    private List<Reward> rewards;
    private Map<String,Object> skip;

    @Getter
    @Setter
    public static class Progress implements Serializable {
        private static final long serialVersionUID = -5690842447477267041L;
        private Integer target;                 //目标进度值
        private Integer curr;                   //当前进度值
        private String q;                       //单位，主要作为显示
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reward implements Serializable {
        private static final long serialVersionUID = -1434432825532246653L;
        private Long id;            //ID
        private String unit;        //单位，integral：园丁豆、exp：经验、cash：现金
        private Integer value;      //奖励
        private Integer sort;       //排序字段
        private String extString;   //额外的话术
    }

}
