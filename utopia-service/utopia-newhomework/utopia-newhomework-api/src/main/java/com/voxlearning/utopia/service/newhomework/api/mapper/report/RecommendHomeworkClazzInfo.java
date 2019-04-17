package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RecommendHomeworkClazzInfo implements Serializable {
    private static final long serialVersionUID = -2453816782324845684L;
    private Long groupId;
    private String homeworkId;
    private String clazzName;
    private boolean hasRecommendHomework;   //是否有需要推荐的讲练测巩固练习
}
