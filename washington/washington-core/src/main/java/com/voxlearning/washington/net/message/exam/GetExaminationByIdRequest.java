package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;

/**
 * Created by tanguohong on 2016/3/8.
 */
public class GetExaminationByIdRequest implements Serializable {

    private static final long serialVersionUID = 7629143590970344624L;

    /** 考试ID */
    public String id;
}
