package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * @Description: 纸质作业报告详情
 * @author: Mr_VanGogh
 * @date: 2019/2/14 下午5:14
 */
@Getter
@Setter
public class OcrNewHomeworkStudentDetail implements Serializable {
    private static final long serialVersionUID = -5738015402779361787L;

    private String hid;
    private Date entTime;//时间
    private int averScore;//平均分
    private String averScoreStr = "--";
    private long averDuration;
    private String averDurationStr;// 时间
    private int totalScore;
    private long totalDuration;
    private int finishedNum;//完成人数
    private int unfinishedNum;//未完成人数
    private boolean showCorrect;
    private int unfinishedCorrectNum;//未完成订正人数
    private int finishCorrectNum;//完成人数
    private int unMarkNum;//没有批改的人数
    private boolean showUrgeFinishHomework = true;//是否显示催促完成作业
    private boolean showUrgeCorrectHomework = false;//是否显示催促
    private boolean showCheckHomework = false;//是否显示查看作业
    private boolean checked;   //是否已检查
    private String shareReportUrl;
    private long homeworkDuration;
    private String detailUrl;
    private String correctUrl;
    private String checkHomeworkUrl;//查看作业地址

    private List<WrongQuestionPart> wrongQuestionParts = new ArrayList<>();
    private List<OcrStudentDetail> ocrHomeworkStudentDetails = new ArrayList<>();//OTO纸质作业学生详情信息

    @Getter
    @Setter
    public static class OcrStudentDetail implements Serializable {
        private static final long serialVersionUID = 2838071018034605498L;

        private Long userId;
        private String userName;
        private int score;
        private int identifyCount;
        private int errorCount;
        private boolean manualCorrect;
        private boolean finished;
        private String personReportUrl;
        private boolean repair; //是否补做

        public static Comparator<OcrStudentDetail> comparator = (Comparator<OcrStudentDetail>) (o1, o2) -> {
            int compare = Boolean.compare(o2.isFinished(), o1.isFinished());
            if (compare != 0) {
                return compare;
            }
            return Integer.compare(o2.getScore(), o1.getScore());
        };
    }

    @Getter
    @Setter
    public static class WrongQuestionPart implements Serializable {
        private static final long serialVersionUID = -4761153092806307413L;

        private String typeName;
        private List<WrongQuestionInfo> wrongQuestionInfos;
    }

    @Getter
    @Setter
    public static class WrongQuestionInfo implements Serializable {
        private static final long serialVersionUID = -2496640288359704566L;

        private String pointId;   //数学易错知识点ID
        private String wrongInfo; //数学易错知识点名称|常见错因|英语易拼错单词
        private Integer studentNum;     //人数

    }

    public void handlerResult(Map<Long, User> userMap, NewHomework newHomework) {
        OcrNewHomeworkStudentDetail ocrNewHomeworkStudentDetail = this;
        ocrNewHomeworkStudentDetail.setChecked(newHomework.isHomeworkChecked());
        ocrNewHomeworkStudentDetail.setHomeworkDuration(SafeConverter.toLong(newHomework.getDuration()));
        ocrNewHomeworkStudentDetail.setEntTime(newHomework.getEndTime());
        ocrNewHomeworkStudentDetail.setHid(hid);
        ocrNewHomeworkStudentDetail.setUnfinishedNum(userMap.size() - ocrNewHomeworkStudentDetail.getFinishedNum());

        //平均成绩和平时时长
        if (ocrNewHomeworkStudentDetail.getFinishedNum() > 0) {
            int averScore = new BigDecimal(ocrNewHomeworkStudentDetail.getTotalScore()).divide(new BigDecimal(ocrNewHomeworkStudentDetail.getFinishedNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
            ocrNewHomeworkStudentDetail.setAverScore(averScore);
            ocrNewHomeworkStudentDetail.setAverScoreStr(averScore + "");
            long averDuration = new BigDecimal(ocrNewHomeworkStudentDetail.getTotalDuration()).divide(new BigDecimal(ocrNewHomeworkStudentDetail.getFinishedNum()), 0, BigDecimal.ROUND_HALF_UP).longValue();
            ocrNewHomeworkStudentDetail.setAverDuration(averDuration);
        }

        // showUrgeFinishHomework
        ocrNewHomeworkStudentDetail.setShowUrgeFinishHomework(ocrNewHomeworkStudentDetail.isShowUrgeFinishHomework() && (ocrNewHomeworkStudentDetail.getUnfinishedNum() > 0));

        //showUrgeCorrectHomework
        if (showCorrect) {
            ocrNewHomeworkStudentDetail.setShowUrgeCorrectHomework(ocrNewHomeworkStudentDetail.isShowUrgeCorrectHomework() && (ocrNewHomeworkStudentDetail.getUnfinishedCorrectNum() > 0));
        }
    }
}
