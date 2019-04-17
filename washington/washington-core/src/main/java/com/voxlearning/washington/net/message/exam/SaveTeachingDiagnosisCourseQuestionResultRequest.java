package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;


/**
 * 保存答题结果
 */
public class SaveTeachingDiagnosisCourseQuestionResultRequest implements Serializable {


    private static final long serialVersionUID = 8061096525104108532L;
    /**
     * 答题结果列表
     */
    public TeachingDianosisCourseQuestionResultRequest result;

}