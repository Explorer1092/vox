package com.voxlearning.utopia.admin.mapper.yiqijt;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class YiqiJTConfCourseListMapper {
    private Long id;
    private String titlePictureUrl;
    private String title;
    private Date updateDatetime;
    private String subjectNames;
    private String gradeNames;
    private Integer status;
    private Integer topNum;
    private Boolean featuring;     // 首页推荐
    private String label;     // 标签
}
