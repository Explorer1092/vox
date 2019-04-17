package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AppNewHomeworkStudentDetail implements Serializable {
    private static final long serialVersionUID = 424517877903173821L;
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
    private boolean showCheckHomework = true;//是否显示查看作业
    private boolean checked;   //是否已检查
    private String shareReportUrl;
    private String detailUrl;
    private String checkHomeworkUrl;//查看作业地址
    private long homeworkDuration;
    private String correctUrl;
    private List<StudentDetail> studentDetails = new LinkedList<>();
    private List<Map<String, Object>> diagnosisHabits = new LinkedList<>();


    public void handlerResult(Map<Long, User> userMap, NewHomework newHomework, boolean allSubjective) {
        AppNewHomeworkStudentDetail appNewHomeworkStudentDetail = this;
        appNewHomeworkStudentDetail.setChecked(newHomework.isHomeworkChecked());
        appNewHomeworkStudentDetail.setHomeworkDuration(SafeConverter.toLong(newHomework.getDuration()));
        appNewHomeworkStudentDetail.setEntTime(newHomework.getEndTime());
        appNewHomeworkStudentDetail.setHid(hid);
        appNewHomeworkStudentDetail.setUnfinishedNum(userMap.size() - appNewHomeworkStudentDetail.getFinishedNum());


        //平均成绩和平时时长
        if (appNewHomeworkStudentDetail.getFinishedNum() > 0) {
            if (!allSubjective) {
                int averScore = new BigDecimal(appNewHomeworkStudentDetail.getTotalScore()).divide(new BigDecimal(appNewHomeworkStudentDetail.getFinishedNum()), 0, BigDecimal.ROUND_HALF_UP).intValue();
                appNewHomeworkStudentDetail.setAverScore(averScore);
                appNewHomeworkStudentDetail.setAverScoreStr(averScore + "");
            }
            long averDuration = new BigDecimal(appNewHomeworkStudentDetail.getTotalDuration()).divide(new BigDecimal(appNewHomeworkStudentDetail.getFinishedNum()), 0, BigDecimal.ROUND_HALF_UP).longValue();
            appNewHomeworkStudentDetail.setAverDuration(averDuration);
        }

        //分数倒序，时间正序
        appNewHomeworkStudentDetail.getStudentDetails().sort((o1, o2) -> {
            int compare = Integer.compare(o2.getScore(), o1.getScore());
            if (compare != 0) {
                return compare;
            }
            compare = Long.compare(o1.getDuration(), o2.getDuration());
            if (compare != 0) {
                return compare;
            }
            long t1 = o1.getFinishAt() != null ? o1.getFinishAt().getTime() : Long.MAX_VALUE;
            long t2 = o2.getFinishAt() != null ? o2.getFinishAt().getTime() : Long.MAX_VALUE;
            return Long.compare(t1, t2);
        });

        // showUrgeFinishHomework
        appNewHomeworkStudentDetail.setShowUrgeFinishHomework(appNewHomeworkStudentDetail.isShowUrgeFinishHomework() && (appNewHomeworkStudentDetail.getUnfinishedNum() > 0));

        //showUrgeCorrectHomework
        if (showCorrect) {
            appNewHomeworkStudentDetail.setShowUrgeCorrectHomework(appNewHomeworkStudentDetail.isShowUrgeCorrectHomework() && (appNewHomeworkStudentDetail.getUnfinishedCorrectNum() > 0));
        }


    }


    @Getter
    @Setter
    public static class StudentDetail implements Serializable {
        private static final long serialVersionUID = -6384632615041390830L;
        private Long sid;
        private String sname;
        private int score;
        private String scoreStr = UN_FINISHED_STR1;
        private long duration = Long.MAX_VALUE;
        private String durationStr = UN_FINISHED_STR2;
        private String correctInfo = UN_FINISHED_STR2;
        private String correctRate = UN_FINISHED_STR2;
        private int wrongNum;
        private List<String> typeInfo = new LinkedList<>();
        private Date finishAt;
        private String finishStr = UN_FINISHED_STR2;
        private boolean finished;
        private boolean repair;
        private boolean needCorrect;
        private boolean finishCorrect;
        private String personReportUrl;
    }


    public static final String UN_FINISHED_STR1 = "未完成";
    public static final String UN_FINISHED_STR2 = "-";


}
