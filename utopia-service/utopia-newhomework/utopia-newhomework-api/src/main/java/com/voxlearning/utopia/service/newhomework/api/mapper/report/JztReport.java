package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.mapper.PronunciationRecord;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * @deprecated 当前版本家长通报告不再维护. 新家长通报告 {@link JztStudentHomeworkReport}
 */
@Setter
@Getter
@Deprecated
public class JztReport implements Serializable {
    private static final long serialVersionUID = -5369949162651532042L;
    private boolean success;
    private String error;
    private String errorCode;

    private boolean finished;//是否完成
    private boolean checked;//是否检查
    private boolean today;
    private Long sid;
    private boolean repair;//是否补做
    private boolean expired;//是否过期
    private String assignDate;//布置日期
    private Long assignedToNow;//布置日期距今的天数
    private Long teacherId;
    private Subject subject;//学科
    private String studentName; //学生姓名
    private boolean hasSign;//家长是否签字
    private boolean blackRegion;//是否灰度地区
    private Long groupId;

    private HomeworkDescription homeworkDescription = new HomeworkDescription();
    private ReportDataPart reportDataPart = new ReportDataPart();//总体表现
    private TeacherSummaryPart teacherSummaryPart = new TeacherSummaryPart();//老师总结
    private RecommendPart recommendPart = new RecommendPart();//一起作业推荐
    private SummaryReward summaryReward = new SummaryReward();    //作业小结与奖励
    private FlowerModule flowerModule = new FlowerModule();//鲜花模块
    private List<Map<String, Object>> readReciteInformation = new LinkedList<>();// 语文读背
    private boolean scoreRegionFlag;//true的时候显示分数
    private List<Map<String, Object>> naturalSpelling = new LinkedList<>();
    private boolean termReview;
    private List<Map<String, Object>> basicReviewTabList = new LinkedList<>();
    // 趣味配音
    private Map<String, Object> dubbingInfo = new LinkedHashMap<>();
    private List<Map<String, Object>> readReciteData = new LinkedList<>();
    private boolean showJumpBtn;
    private String bookId;
    //纠正粗心马虎错误题目数量(家长通报告直播导流用)
    private long interventionGraspQuestionCount;
    //前端说返回的参数为null的时候不兼容, 所以没有的话就给默认值, 很是奇怪
    private Object errorReasonFlowGuide = Collections.emptyMap();//报告错因导流试验
    private String selfStudyUrl = "";//订正作业跳转url
    private String selfStudyIndexUrl = "";//订正作业跳转接口

    /**
     * 简介
     */
    @Setter
    @Getter
    public static class HomeworkDescription implements Serializable {
        private static final long serialVersionUID = 7256037486090258855L;
        private String endTime;
        private int totalUser;
        private long completeNum;
        private String planFinishTime;
        private Set<String> homeworkContent;   //作业内容
        private List<String> practiceList;  //练习要求
        private Map<String, Object> jxtNewsBookInfoMap;    //同步内容
    }


    /**
     * 总体表现
     */
    @Setter
    @Getter
    public static class ReportDataPart implements Serializable {
        private static final long serialVersionUID = -4345848561103041690L;
        private int score;//个人成绩
        // 是否只有趣味配音形式
        private boolean onlyDubbing;
        private String scoreStr = "--";
        private int averScore;//平均成绩
        private String averScoreStr;
        private int highestScore;//最高成绩
        private String highestScoreStr;
        private int wrongNum;//错题数目
        private int unStandardWordNum;//未达标的数
        private List<Map<String, Object>> scoreDetail;//作业类型和对应分数和情况
    }

    /**
     * 老师总结
     */
    @Setter
    @Getter
    public static class TeacherSummaryPart implements Serializable {
        private static final long serialVersionUID = 6038231245395406489L;
        private String teacherName;
        private String teacherPicUrl;
        private String teacherComment;  //老师评语
        private ErrorModule errorModule = new ErrorModule();//错题模块
        private TalkingModule talkingModule = new TalkingModule();//说模块
        private KnowledgePointModule knowledgePointModule = new KnowledgePointModule();//知识点部分

        /**
         * 错题模块
         */
        @Setter
        @Getter
        public static class ErrorModule implements Serializable {
            private static final long serialVersionUID = 3615518978966474930L;
            private int wrongQuestionNum;              //错题
            private boolean flag;              //是否显示
            private int semesterWrongNum;      //学期错题数
        }

        /**
         * 说模块
         */
        @Setter
        @Getter
        public static class TalkingModule implements Serializable {
            private static final long serialVersionUID = 5110409513247155483L;
            private List<Map<String, Object>> oralList = new ArrayList<>();


            private boolean flag;//是否在全部pc
            private boolean hasVoice;//是否有音频
            private List<PronunciationRecord.Word> words;
            private int weakCount;
            private List<PronunciationRecord.Line> lines;
            private List<Map> unitAndSentenceList;
            private boolean voiceFlag;//是否需要查询语音数据
            private String bookId;
            private boolean hasPicListenContent;//是否在点读机有匹配教材
            private boolean showTask;//未达标数据是否匹配达到75%
            private boolean finishTask;//是否完成了点读机任务
            private boolean needPay;    //是否需要付费
            private boolean hasPay;  //是否已付费
        }

        /**
         * 知识点模块
         */
        @Setter
        @Getter
        public static class KnowledgePointModule implements Serializable {
            private static final long serialVersionUID = -6646504192041958597L;
            private int knowledgePointNum;
            private boolean flag;
            private Collection<String> knowledgePointNames = new LinkedList<>();
        }
    }

    /**
     * 推荐模块
     */
    @Setter
    @Getter
    public static class RecommendPart implements Serializable {
        private static final long serialVersionUID = -5920488960589511239L;
        private OlympiadContent olympiadContent = new OlympiadContent();//奥数
        private ClassroomContent classroomContent = new ClassroomContent();// 翻讲课堂

        /**
         * 奥数
         */
        @Setter
        @Getter
        public static class OlympiadContent implements Serializable {
            private static final long serialVersionUID = 5560070163895641675L;
            private int score;//分数
            private boolean flag;//是否显示

        }

        /**
         * 翻讲课堂
         */
        @Getter
        @Setter
        public static class ClassroomContent implements Serializable {
            private static final long serialVersionUID = 4911374701742535487L;
            private boolean flag;
            private String unitName;
            private String kpName;
            private String url = "";
        }

    }

    /**
     * 作业小结与奖励
     */
    @Setter
    @Getter
    public static class SummaryReward implements Serializable {
        private static final long serialVersionUID = 7110224990224343068L;
        private int extraIntegral;  //完成作业可领取的学豆，这个只会存在一段时间
        private boolean hasReceived;    //是否领取过额外学豆
        private int homeworkIntegral;   //作业奖励学豆
        private int teacherIntegral;    //老师奖励学豆
    }

    /**
     * 送花模块
     */
    @Setter
    @Getter
    public static class FlowerModule implements Serializable {
        private static final long serialVersionUID = 6354171342053331845L;
        private String title;
        private boolean sendFlower;//是否送过花
        private boolean closeFlower; //是否关闭鲜花
        private int senderCount;//已送花人数
        private String flowerText;//送花文案，因为涉及到家长身份，所以由后端返回给前端
        private String secondFlowerText;
        private String thirdFlowerText;
    }

}
