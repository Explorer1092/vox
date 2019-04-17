package com.voxlearning.utopia.service.reward.mapper;

import com.voxlearning.utopia.service.reward.api.mapper.TeacherJoinStatusMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PGActivityMapper implements Serializable {

    private static final long serialVersionUID = -6362264530627983783L;

    private Long id;
    private String name;
    private Integer partakeNums;
    private String imgUrl;
    private Long raisedMoney;
    private Long targetMoney;
    private String status;
    private String summary;                             // 简介(概要)
    private List<TeacherJoinStatusMapper> tchJoinList;  // 老师参与情况
    private List<Map<String,Object>> childList;         // 孩子参与情况
    private String collectId;
    private String model;                               // 模式，用来切换新老项目

}
