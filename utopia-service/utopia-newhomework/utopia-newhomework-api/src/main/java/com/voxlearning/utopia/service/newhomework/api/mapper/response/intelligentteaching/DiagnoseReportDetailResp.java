package com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/6/29
 */
@Getter
@Setter
public class DiagnoseReportDetailResp extends BaseResp {
    private static final long serialVersionUID = -6802027842541009926L;

    private String hid;//作业ID
    private String questionBoxId;//题包ID(数学即为sectionId)
    private String questionBoxName;//题包名称
    private Integer accuracy;//题包当前正确率
    private Integer preAccuracy;//诊断前正确率
    private Integer promoteAccuracy;//提升正确率
    private String objectiveConfigType;//题包作业类型

    private QuestionAnalysis questionAnalysis;//题目诊断分析
    private List<StudentAnalysis> studentAnalyses = new LinkedList<>();//学生诊断情况

    @Getter
    @Setter
    public static class QuestionAnalysis implements Serializable {
        private static final long serialVersionUID = -8095511156353126660L;
        private List<Variant> variants = new LinkedList<>();//正确率（不含即时干预后做对）低于80%的题目对应的变式. 注:只有数学有
        private List<String> studentNames = new LinkedList<>();//正确率（不含即时干预后做对）低于75%的学生
        private List<QuestionCourse> questionCourses = new LinkedList<>();//题目诊断详情 注:一个课程对应一条数据
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class StudentAnalysis implements Serializable {
        private static final long serialVersionUID = -7045638344664265950L;
        private Long studentId;//学生id
        private String studentName;//学生姓名
        private String preAccuracy;//诊断前正确率
        private String accuracy;//课程后正确率

        public static class StudentComparator implements Comparator<StudentAnalysis> {
            @Override
            public int compare(StudentAnalysis o1, StudentAnalysis o2) {
                String str1 = "未提交", str2 = "通过", str3 = "未通过";
                if (str1.equals(o1.getPreAccuracy())) {
                    if (str1.equals(o2.getPreAccuracy())) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else {
                    if (str1.equals(o2.getPreAccuracy())) {
                        return -1;
                    } else {
                        if (str2.equals(o1.getPreAccuracy())) {
                            if (str2.equals(o2.getPreAccuracy())) {
                                return 0;
                            } else {
                                return -1;
                            }
                        } else {
                            if (str2.equals(o2.getPreAccuracy())) {
                                return 1;
                            } else if (str3.equals(o1.getPreAccuracy()) && str3.equals(o2.getPreAccuracy())) {
                                return 0;
                            } else {
                                return Integer.compare(SafeConverter.toInt(o1.getPreAccuracy()), SafeConverter.toInt(o2.getPreAccuracy()));
                            }
                        }
                    }
                }
            }
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Variant implements Serializable {
        private static final long serialVersionUID = 9162022991448545681L;
        private String variantId;//变式ID
        private String variantName;//变式名称
        private Integer accuracy;//正确率
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class QuestionCourse implements Serializable {
        private static final long serialVersionUID = -3965286420037128770L;
        private Integer interventionCount;//及时干预纠正的人数
        private Integer courseLearnCount;//辅导课程学习人数
        private String courseName;//课程名称
        private Long simQCorrectCount;//课程后测题全部做对人数
        private List<Packet> packets;//题目详情分组
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Packet implements Serializable {
        private static final long serialVersionUID = 4163345473118079581L;
        private String packetId;//题目分组ID(英语:题型ID, 数学:变式ID)
        private String packetName;//题目分组名(英语:题型名, 数学:变式名)
        private List<Question> questions;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Question implements Serializable {
        private static final long serialVersionUID = 7280527910088033586L;
        private String questionId;//题目id
        private Integer accuracy;//正确率or通过率
        private Integer errorCount;//本题做错or未通过学生数
    }
}
