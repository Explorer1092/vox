package com.voxlearning.utopia.service.zone.api.entity.plot;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 班级进度返回类
 * @author dongfeng.xue
 * @date 2018-11-14
 */
@Getter@Setter
public class PlotClazzProgressResponse implements Serializable {
    private static final long serialVersionUID = -2817707488165114925L;
    private Integer type; //1：进度
    private Integer count; //数量
}
