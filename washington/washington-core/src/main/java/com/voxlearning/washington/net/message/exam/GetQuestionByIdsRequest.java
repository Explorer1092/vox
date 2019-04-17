package com.voxlearning.washington.net.message.exam;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by tanguohong on 2015/7/17.
 */
public class GetQuestionByIdsRequest implements Serializable {


    private static final long serialVersionUID = 7361270004565154401L;

    /** 试题ID列表 */
    public Collection<String> ids;

    /*是否包含答案*/
    public Boolean containsAnswer;
}
