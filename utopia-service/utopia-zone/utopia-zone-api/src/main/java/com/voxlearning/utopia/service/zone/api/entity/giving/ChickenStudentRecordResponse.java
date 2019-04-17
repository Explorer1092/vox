package com.voxlearning.utopia.service.zone.api.entity.giving;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 吃鸡用户记录
 * @author dongfeng.xue
 * @date 2018-11-16
 */
@Getter@Setter
public class ChickenStudentRecordResponse implements Serializable {
    private static final long serialVersionUID = -2817707488135114925L;
    private String name;
    private Integer type;
}
