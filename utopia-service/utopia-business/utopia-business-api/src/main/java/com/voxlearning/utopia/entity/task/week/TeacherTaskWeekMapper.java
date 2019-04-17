package com.voxlearning.utopia.entity.task.week;

import com.voxlearning.utopia.service.user.api.entities.Clazz;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TeacherTaskWeekMapper implements java.io.Serializable {

    private Long clazzId;               // 班级ID
    private String clazzName;           // 班级名称
    private transient Clazz clazz;      // 只用来本地排序,不序列化传输
    private List<Homework> homeworkList;

    @Data
    @NoArgsConstructor
    public static class Homework implements java.io.Serializable {
        private String homeworkId = "";      // 作业ID
        private Integer finishNum = 0;       // 完成人数
        private Integer integralNum;         // 学豆
        private Integer expNum;              // 活跃值
        private boolean finish = false;      // 是否已完成
    }
}
