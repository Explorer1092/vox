package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.utopia.service.ai.constant.WeekType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Summer on 2018/3/28
 * 回答题目的薄弱点
 */
@Getter
@Setter
public class QuestionWeekPoint implements Serializable{

    private static final long serialVersionUID = 1463075046727136805L;

    private WeekType weekType;
    private String content;
    private String original;
    private String answerUrl;   // 用户回答
    private String suggestUrl;  // 建议回答

}
