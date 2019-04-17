package com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching;

import com.voxlearning.utopia.service.newhomework.api.constant.IntelligentRecommendStudentStatus;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 推荐巩固(老师端-布置个性化错因巩固作业)
 *
 * @author majianxin
 * @version V1.0
 * @date 2018/9/12
 */
@Getter
@Setter
public class IntelligentTeachingRecommendResp extends BaseResp {
    private static final long serialVersionUID = -7774844487680543776L;

    private int variantCount;                               //未掌握变式数
    private int totalStudentCount;                          //订正任务中含课程的学生总数
    private List<IntelligentTeachingSummary> summaryList = new LinkedList<>();   //讲练测总结
    private List<StudentFinishDetail> studentFinishDetails = new LinkedList<>(); //讲练测学生详情
    private boolean hasRecommend;                           //是否已推荐巩固

    @Getter
    @Setter
    public static class IntelligentTeachingSummary implements Serializable {
        private static final long serialVersionUID = 8520871915276990057L;

        private String courseId;        //课程ID
        private String variantName;     //变式名称
        private int studentCount;       //当前变式未掌握学生数
    }

    @Getter
    @Setter
    public static class StudentFinishDetail implements Comparable<StudentFinishDetail>, Serializable {
        private static final long serialVersionUID = 93615551996946748L;

        private Long studentId;         //学生ID
        private String studentName;     //学生名
        private int courseCount;        //命中课程数
        private String status;          //状态

        @Override
        public int compareTo(@NotNull StudentFinishDetail o) {
            if (this.status.equals(o.status)) {
                return this.studentId.compareTo(o.studentId);
            }
            //未学习
            if (this.status.equals(IntelligentRecommendStudentStatus.UN_FINISHED_SELF.name())) {
                return -1;
            } else if (o.status.equals(IntelligentRecommendStudentStatus.UN_FINISHED_SELF.name())) {
                return 1;
            }
            //已自学
            if (this.status.equals(IntelligentRecommendStudentStatus.SELF_STUDY.name())) {
                return -1;
            } else if (o.status.equals(IntelligentRecommendStudentStatus.SELF_STUDY.name())) {
                return 1;
            }
            //未检测到问题
            if (this.status.equals(IntelligentRecommendStudentStatus.UN_DETECTED.name())) {
                return -1;
            } else if (o.status.equals(IntelligentRecommendStudentStatus.UN_DETECTED.name())) {
                return 1;
            }
            //未完成作业
            if (this.status.equals(IntelligentRecommendStudentStatus.UN_FINISHED_HOMEWORK.name())) {
                return -1;
            } else if (o.status.equals(IntelligentRecommendStudentStatus.UN_FINISHED_HOMEWORK.name())) {
                return 1;
            }
            return 0;
        }
    }
}
