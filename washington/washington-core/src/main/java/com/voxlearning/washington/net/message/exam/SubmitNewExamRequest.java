package com.voxlearning.washington.net.message.exam;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by tanguohong on 2016/3/15.
 */
@Data
public class SubmitNewExamRequest implements Serializable {

    private static final long serialVersionUID = -3666030735445126635L;
    private String newExamId; // 考试ID
    private String clientType;    // 客户端类型:pc,mobile
    private String clientName;    // 客户端名称:***app
}
