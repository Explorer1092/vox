package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;


/**
 * 保存答题结果
 */
public class SaveTeachingDiagnosisPreQuestionResultRequest implements Serializable {


    private static final long serialVersionUID = 1034571336263025045L;
    /**
     * 答题结果列表
     */
    public TeachingDianosisPreQuestionResultRequest result;

}