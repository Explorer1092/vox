package com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/7/23
 * \* Time: 下午6:29
 * \* Description:生字认读报告详情
 * \
 */
@Getter
@Setter
public class WordRecognitionAndReadingAppPart implements Serializable {
    private static final long serialVersionUID = -3343670514492476842L;
    private String lessonName = "";
    private int standardUserCount;
    private int userCount;
    private List<WordRecognitionAndReadingAppPart.WordRecognitionAndReadingAppPartUser> users = new LinkedList<>();
    @Getter
    @Setter
    public static class WordRecognitionAndReadingAppPartUser implements Serializable {
        private static final long serialVersionUID = -6514052021189448462L;
        private Long userId;
        private String userName = "";
        private boolean standard;
        private List<String> voices = new LinkedList<>();
        private String duration;
    }
}
