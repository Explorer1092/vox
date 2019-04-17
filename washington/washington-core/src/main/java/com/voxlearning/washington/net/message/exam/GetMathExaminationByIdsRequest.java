package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;
import java.util.Collection;

public class GetMathExaminationByIdsRequest implements Serializable {
    private static final long serialVersionUID = 0L ;

    /** 试题ID列表 */
    public Collection<String> questions;
}
