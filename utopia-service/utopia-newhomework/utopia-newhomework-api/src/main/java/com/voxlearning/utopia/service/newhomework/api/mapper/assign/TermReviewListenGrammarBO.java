package com.voxlearning.utopia.service.newhomework.api.mapper.assign;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2017/5/8 15:58
 */
@Setter
@Getter
public class TermReviewListenGrammarBO implements Serializable {

    private static final long serialVersionUID = -2902622302062397424L;

    private String id;  //题包id
    private String name;    //题包名
    private Integer questionNum;    //总题数
    private Long seconds;   //总用时长
    private List<Map<String, Object>> questions;  //题目集合
}
