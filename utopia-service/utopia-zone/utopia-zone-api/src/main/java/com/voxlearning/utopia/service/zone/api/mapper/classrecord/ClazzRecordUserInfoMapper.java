package com.voxlearning.utopia.service.zone.api.mapper.classrecord;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/3/2
 * Time: 16:24
 *
 */
@Setter
@Getter
public class ClazzRecordUserInfoMapper implements Serializable{
    private static final long serialVersionUID = -669008267212496979L;

    //学生头像
    private String image;
    //学生头饰
    private String headWear;
    //学生名称
    private String name;
}
