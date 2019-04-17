package com.voxlearning.utopia.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 14-7-7.
 */
@Data
public class ReadingDraftQuestionTemp implements Serializable{

    private static final long serialVersionUID = -5457644758205259053L;
    private Integer type; // 题目类型 1:单选 2:判断 3:填空
    private String content; // 题干
    private String contentPic; // 图片
    private Map<String,Object> answerOptions; // 答题选项
    private List<String> rightAnswer; // 答案
    private String questionComment; // 题目解析
    private Integer rank;  // 题目排序
}
