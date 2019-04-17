package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;

/*
 * 学生 或者 班级 对知识点的掌握信息
 * ek：知识点,包括grammar 和 word
 * master:对知识点ek的掌握程度,范围 [0,1]
 * count:该知识点的曝光次数,以此根据来影响ek的推荐频率及次数
 * ver=1,level:掌握度分级, 0未掌握,1基本掌握,2掌握:该用户至少做对过一道该知识点下的题, 3掌握：未做过该知识点下的题
 * ver=2,level:掌握度分级, 1未掌握,2基本掌握,3掌握:该用户至少做对过一道该知识点下的题, 4掌握：未做过该知识点下的题
 * 当前使用的ver=2
 */
@Data
public class UserEkContent implements Serializable {

    private static final long serialVersionUID = -1514024055778158104L;

    private String ek;
    private double master;
    private short count;
    private short level;
}
