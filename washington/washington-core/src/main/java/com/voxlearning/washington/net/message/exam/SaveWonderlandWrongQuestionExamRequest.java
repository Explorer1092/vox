package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;


/**
 * 保存答题结果
 */
public class SaveWonderlandWrongQuestionExamRequest implements Serializable {

    private static final long serialVersionUID = -8607717100648454990L;
    /**
     * 答题结果
     */
    public WonderlandWrongQuestionExamResultRequest result;


}