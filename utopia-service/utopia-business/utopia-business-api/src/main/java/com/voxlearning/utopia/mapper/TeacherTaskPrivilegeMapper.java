package com.voxlearning.utopia.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by zhouwei on 2018/9/18
 **/
@Getter
@Setter
public class TeacherTaskPrivilegeMapper implements Serializable {
    private static final long serialVersionUID = -4920749828361546660L;
    private Long id;                                //任务模板ID
    private String name;                            //特权名称
    private String subName;                         //附表提
    private String type;                            //特权类型
    private String instruction;                     //特权说明
    private Map<String,Object> skip;                //跳转的信息，给前端提供的
    private Boolean loop;                           //是否为循环特权
    private String cycleUnit;                       //循环单位:W:按周循环 M：按月循环，O:其他
    private Boolean timesLimit;                     //是否有使用次数限制
    private Integer times;                          //总的使用次数
    private Integer useTime;                        //已经使用的次数
    private String quantifier;                      //次数的单位
    private Integer sort;                           //排序
}
