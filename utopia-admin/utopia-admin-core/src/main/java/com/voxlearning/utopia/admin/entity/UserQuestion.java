package com.voxlearning.utopia.admin.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author guangqing
 * @since 2018/7/23
 */
@Getter
@Setter
public class UserQuestion implements Serializable {

    private List<Map<String, Object>> answerList;
    private int page;
    private Double avgAnswerTime;
    private int answerSize;
}
