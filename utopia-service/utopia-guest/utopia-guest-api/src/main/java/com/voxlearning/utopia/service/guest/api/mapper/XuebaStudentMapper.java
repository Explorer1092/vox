package com.voxlearning.utopia.service.guest.api.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * 翻转课堂学生mapper
 *
 * @author changyuan
 * @since 2017/2/28
 */
@Data
public class XuebaStudentMapper implements Serializable {

    private static final long serialVersionUID = 8995051257408652302L;

    String name;                        // 姓名
    String mobile;                      // 手机号
    Long clazzId;                       // 班级id
    String clazzName;                   // 班级名称
    String autoNamePrefix;              // 如果需要自动生成姓名，对应的前缀
    Boolean needBindMobile;                 // 是否绑定手机号
}
