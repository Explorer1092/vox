package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * APP报告-按照题目查看详情
 * @Description: 字词讲练
 * @author: Mr_VanGogh
 * @date: 2019/1/7 下午4:20
 */
@Setter
@Getter
public class WordTeachAndPracticeTypePart implements Serializable {
    private static final long serialVersionUID = -403227533129935658L;

    private ObjectiveConfigType type;
    private String typeName;
    private boolean showUrl;                //是否显示链接
    private String url;                     //链接地址
    private int tapType = 7;                //模板类型
    private boolean hasFinishUser;          //该类型是否有人完成
    private int finishedUserCount;          //完成人数
    private int unCorrect;
    private boolean showCorrectUrl;         //是否显示一键批改
    private boolean showScore;              //是否显示分数
    private int averScore;                  //平均分
    private long averDuration;              //平均时间(min)
    private String subContent;
    private List<WordTeachSectionDataPart> types;

    @Setter
    @Getter
    public static class WordTeachSectionDataPart implements Serializable {
        private static final long serialVersionUID = 4621678100574365163L;

        private String title;       //课时名称
        private String typeName;
        private int tapType;
        private boolean showUrl;        //是否显示链接
        private String url;             //链接地址
        private List<TabPart> tabs;

        //字词训练模块
        private boolean hasFinishUser;  //该类型是否有人完成
        private int averScore;          //平均分
        private boolean showScore;      //是否显示分数
        private long averDuration;              //平均时间(min)
        private String subContent;
        private String rateContent;
        private List<WordExerciseQuestion> question = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class TabPart implements Serializable {
        private static final long serialVersionUID = -8414097627925218926L;
        private String tabName = "";
        private List<WordTeachAndPracticeTypePart.TabInfoPart> tabs;
    }

    @Getter
    @Setter
    public static class TabInfoPart implements Serializable {
        private static final long serialVersionUID = -5232055569535838406L;
        private String tabName;
        private String tabValue;
        private boolean showUrl = true;
        private String url;
    }

    @Getter
    @Setter
    public static class WordExerciseQuestion implements Serializable {
        private static final long serialVersionUID = -1801269988076149537L;
        private String qid;
        private int num;
        private boolean showUrl = true;
        private String url;
        private int rightNum;               //订正前的正确数
        private int interventionRightNum;   //订正后的正确数
        private int index;
        private int rate;               //最终正确率||分数
        private int firstRate;          //正确率
        private boolean interventionReSubmit;   //是否有干预
        private boolean scoreType = false;
    }
}
