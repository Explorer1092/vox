package com.voxlearning.utopia.service.zone.api.entity.giving;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 班级吃鸡进度统计返回类
 * @author dongfeng.xue
 * @date 2018-11-14
 */
@Getter@Setter
public class ChickenClazzProgressResponse implements Serializable {
    private static final long serialVersionUID = -2817707488165114925L;
    private Integer count;
    private Integer clazzCount;
}
