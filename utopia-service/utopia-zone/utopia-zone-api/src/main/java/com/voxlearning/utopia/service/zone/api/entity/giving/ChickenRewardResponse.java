package com.voxlearning.utopia.service.zone.api.entity.giving;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 吃鸡助力类
 * @author dongfeng.xue
 * @date 2018-11-14
 */
@Getter@Setter
public class ChickenRewardResponse implements Serializable {
    private static final long serialVersionUID = -2817707488165114925L;
    private List<ChickenStudentResponse> studentList;
    private Integer type; //类型 1：烤箱 2：托盘 3：火鸡
}
