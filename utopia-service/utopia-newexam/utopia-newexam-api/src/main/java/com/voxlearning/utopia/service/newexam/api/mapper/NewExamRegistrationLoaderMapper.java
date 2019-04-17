package com.voxlearning.utopia.service.newexam.api.mapper;

import lombok.Data;

import java.io.Serializable;

/**
 * @author guoqiang.li
 * @since 2016/3/23
 */
@Data
public class NewExamRegistrationLoaderMapper implements Serializable{
    private static final long serialVersionUID = -1870703317321439405L;

    String newExamId;       // 考试id
    Long studentId;         // 学生id
    String studentName;     // 学生姓名
    Integer provinceId;     // 所属地区
    Integer cityId;         // 所在市
    Integer regionId;       // 所在区
    Long schoolId;          // 学校id
    Integer currentPage;    // 第几页
    Integer pageSize;       // 每页数量
    Boolean filterNotStart; // 过滤掉未参加考试的学生
}
