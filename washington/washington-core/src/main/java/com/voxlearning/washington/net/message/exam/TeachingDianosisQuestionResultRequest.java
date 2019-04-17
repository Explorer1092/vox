package com.voxlearning.washington.net.message.exam;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tanguohong on 2015/7/16.
 */
@Setter
@Getter
public class TeachingDianosisQuestionResultRequest implements Serializable {

    private static final long serialVersionUID = 6475068066742729607L;
    private Boolean last; //是否最后
    private String qid; //题ID
    private String clientType;  // 客户端类型:pc,mobile
    private String clientName;  // 客户端名称:***app
    private String subject; //科目
    private List<QuestionResultMapper> examResults;//做题结果

}
