package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;

/**
 * Created by tanguohong on 2016/3/8.
 */
public class GetPaperByIdRequest implements Serializable {

    private static final long serialVersionUID = -1078433395600724523L;

    /** 试卷ID */
    public String id;
}
