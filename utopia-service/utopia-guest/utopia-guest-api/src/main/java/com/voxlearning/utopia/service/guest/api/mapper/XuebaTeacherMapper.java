package com.voxlearning.utopia.service.guest.api.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * @author changyuan
 * @since 2017/2/28
 */
@Data
public class XuebaTeacherMapper implements Serializable {

    private static final long serialVersionUID = -2664466230481332579L;

    String name;                        // 姓名
    String mobile;                      // 老师手机，如果没指定，生成账号时将会创建假手机号绑定
}
