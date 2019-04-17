package com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * 分享报告>>讲练测总结
 *
 * @author majianxin
 * @version V1.0
 * @date 2018/8/14
 */
@Getter
@Setter
public class IntelligentTeachingReport implements Serializable {
    private static final long serialVersionUID = -7138331315702386426L;

    private int hasCourseGraspUserCount;     //订正完成且有课程掌握学生数
    private int hitCourseStudentCount;      //命中课程学生数
    private double advanceAccuracy;         //进步学生比率(四舍五入取整)
    private List<AdvanceStudent> advanceStudents; //进步学生详情

    @Getter
    @Setter
    public static class AdvanceStudent implements Serializable {
        private static final long serialVersionUID = 4118888741394521614L;

        private Long studentId;   //学生ID
        private String studentName; //学生姓名
        private String advanceType; //掌握类型 QUESTION:掌握题目数, ORAL
        private int count;          //掌握数量
    }

    public static class AdvanceStudentComparator implements Comparator<AdvanceStudent> {
        @Override
        public int compare(AdvanceStudent o1, AdvanceStudent o2) {
            String oral = "ORAL";
            if (o1.getAdvanceType().equals(o2.getAdvanceType())) {
                return o1.getStudentId().compareTo(o2.getStudentId());
            } else {
                if (oral.equals(o1.getAdvanceType())) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

}
