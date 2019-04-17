package com.voxlearning.washington.net.message.exam;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ruib
 * @since 2016/12/21
 */
@Data
public class AfentiTermQuizRequest implements Serializable {
    private static final long serialVersionUID = -2164058995123416605L;

    private String subject; // 科目
    private Boolean finished; // 是否是最后一题
    private String questionId; // 题ID
    private List<List<String>> answer;  // 答案
    private Long duration; // 完成时长
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String bookId;           //课本ID
    private String unitId;           //单元ID
}
