package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.IntelligentTeachingReport;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
public class NewHomeworkShareReport implements Serializable {
    private static final long serialVersionUID = 3494558135750840707L;
    private boolean success;

    private String homeworkId; // 作业ID

    private boolean share = true;// 是否分享

    // 该份作业是否只有主观题
    private Boolean onlySubjective;

    private Long teacherId;

    private String teacherName;

    private String teacherUrl;

    private String teacherShareMsg;

    private String time;//时间 格式MM.dd

    private int avgScore;// 平均分

    private String avgScoreStr;

    private int highScore;// 最高分数

    private String highScoreStr;

    private boolean hasMental;

    private int totalNum;// 一共的学生个数

    private int finishedNum;//完成的学生个数

    private Subject subject;//学科

    private String subjectName;

    private long finishedCorrectedNum = 0;// 完成订正的人数

    private List<StudentReport> achievementPart = new LinkedList<>(); //学科之星

    private List<StudentReport> achievementPartTwo = new LinkedList<>(); //90分及以上

    private List<StudentReport> fullMarksPart = new LinkedList<>(); //满分

    private List<StudentReport> submitTimePart = new LinkedList<>();

    private List<StudentReport> correctPart = new LinkedList<>(); //错题未订正

    private List<StudentReport> mentalPart = new LinkedList<>(); //口算之星

    private List<StudentReport> focusPart = new LinkedList<>(); //专注之星

    private List<StudentReport> positivePart = new LinkedList<>(); //积极之星

    private String planType; //老师分享微信群报告-配音资源外露&建议升级方案灰度配置A：默认、B：优质绘本资源、C：升级引导外加必备工具外露

    private IntelligentTeachingReport intelligentTeachingReport;//作业讲练测作业报告统计结果

    private boolean needScoreLevel;

    private String onm = "90分及以上";
    private String overNinety = "90分及以上";
    private String fullMark = "满分";

    private List<StudentReport> unFinishedStudentReports = new LinkedList<>(); //作业未完成
    private List<StudentReport> finishedStudentReports = new LinkedList<>(); //作业完成

    private Map<Long, StudentReport> studentReportMap = new LinkedHashMap<>();

    private MapMessage mapMessage = MapMessage.successMessage();

    private List<Map<String,Object>> excellentDubbingStudent;//优秀趣味配音

    private List<Integer> channels;//分享渠道

    @Getter
    @Setter
    public static class StudentReport implements Serializable {

        private int score; // 分数

        private String scoreStr;

        private long consumingTime;// 耗时

        private Long sid;

        private int mentalScore;

        private String mentalScoreStr;

        private int mentalDuration;

        private String mentalDurationStr; //时间 n'm'' n分m秒

        private String zdDurationStr; //中断时间 n'm'' n分m秒

        private String name;

        private String imageUrl;// 学生的图片

        private Date submitTime;//提交时间

        private String finishAtStr;//完成时间 MM-dd HH:mm

        private boolean needCorrect;//是否需要订正

        private boolean finishedCorrect;//是否完成订正

    }
}
