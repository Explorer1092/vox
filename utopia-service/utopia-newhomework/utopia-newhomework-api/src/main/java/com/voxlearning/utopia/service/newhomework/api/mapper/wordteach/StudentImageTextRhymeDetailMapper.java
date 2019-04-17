package com.voxlearning.utopia.service.newhomework.api.mapper.wordteach;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @Description: 图文入韵-个人分享详情
 * @author: Mr_VanGogh
 * @date: 2018/12/25 下午6:39
 */
@Getter
@Setter
public class StudentImageTextRhymeDetailMapper implements Serializable {
    private static final long serialVersionUID = 8215203927296250108L;

    private Long studentId;
    private String studentName;
    private String chapterId;
    private String title;
    private String imageUrl;
    private Integer star;
    private List<StudentImageTextRhymeQuestionInfo> studentImageTextRhymeQuestionInfoList;

    @Getter
    @Setter
    public static class StudentImageTextRhymeQuestionInfo implements Serializable {
        private static final long serialVersionUID = 4162289742446827659L;

        private String questionId;
        private String coverPic;
        private LinkedList<QuestionContentAudioUrl> contentAudioUrls;
    }

    @Getter
    @Setter
    public static class QuestionContentAudioUrl implements Serializable {
        private static final long serialVersionUID = -3017702804192274076L;

        private String content;
        private String audioUrl;
    }
}
