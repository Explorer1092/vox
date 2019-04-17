package com.voxlearning.utopia.service.newhomework.api.mapper.assign;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2017/1/10 16:59
 */
@Getter
@Setter
public class WeekWrongQuestionBO implements Serializable {
    private static final long serialVersionUID = 8520229907633726523L;
    private Long groupId;   //班组id
    private String groupName;   //班级名称
    private String id;  //题包id
    private String name;    //题包名
    private String lossRate;    //失分率
    private Integer questionNum;    //总题数
    private Long seconds;   //总用时长
    private List<Map<String, Object>> questions;  //班组错题集合
}
