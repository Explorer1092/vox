package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author shiwei.liao
 * @since 2017-4-7
 */
@Getter
@Setter
public class TeacherLearningAlbumConfig {
    public TeacherLearningAlbumConfig() {
    }

    private String categoryName;    //分类名称
    private String subjectName;     //学科
    private Integer clazzLevel;     //年级
    private Integer rank;           //分类排序
    private List<String> albumIds;  //分类包含的专辑ID
}
