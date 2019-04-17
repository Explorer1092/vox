package com.voxlearning.utopia.service.newhomework.api.entity.base;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/4/19
 */
@Getter
@Setter
public class BaseHomeworkReportQuestion implements Serializable {

    private static final long serialVersionUID = 302282035507375899L;

    private String processId;                 // 对应的processId
    private Boolean grasp;                    // 掌握
    private String questionId;                // 做题id
    public List<List<Boolean>> subGrasp;      // 作答区域的掌握情况
    public List<List<String>> userAnswers;    // 用户答案
}
