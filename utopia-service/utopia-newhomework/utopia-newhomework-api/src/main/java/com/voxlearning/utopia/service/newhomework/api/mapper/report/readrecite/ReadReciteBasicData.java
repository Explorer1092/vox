package com.voxlearning.utopia.service.newhomework.api.mapper.report.readrecite;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 朗读背诵报告基础结构数据
 */
@Getter
@Setter
public class ReadReciteBasicData implements Serializable {
    private static final long serialVersionUID = -5992269883907902899L;

    private String lessonName;//课文名字

    private String paragraphDescription;//段落的描述

    private String questionBoxId;

    private List<String> personalVoiceToApp = new LinkedList<>();//学生整个包的语音（用于个人详情）

    private boolean corrected;//是否批改（用于个人详情）

    private String correctionInfo;//批改信息(用于个人详情)：优良

    private List<ReadReciteBasicData.ParagraphDetailed> paragraphDetaileds = new LinkedList<>();//段落信息

    private List<String> masterIndex = new LinkedList<>();

    private List<UserVoice> users = new LinkedList<>();

    private int finishedNum; // 完成的人数

    private int totalNum;// 一共的人数

    private int correctionData;//


    @Getter
    @Setter
    public static class UserVoice implements Serializable {
        private static final long serialVersionUID = -3691596043236235466L;
        private Long userId;
        private String userName = "";

        private String duration;

        private List<String> showPics = new LinkedList<>();
        private boolean review;
        private String correction;
        private String correct_des;
    }


    /**
     * 每一个段落的信息
     */
    @Getter
    @Setter
    public static class ParagraphDetailed implements Serializable {
        private static final long serialVersionUID = -4158127465980577957L;

        private String paragraphOrder;//段落次序:第一段

        private String questionId;

        private boolean paragraphDifficultyType;//段落困难描述： 重点段落

        private List<String> personalVoiceToParagraph;//段落个人的语音

    }

}
