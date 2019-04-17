package com.voxlearning.utopia.service.newhomework.api.mapper.termplan;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author zhangbin
 * @since 2018/3/6
 */

@Setter
@Getter
public class TermPlanClazzBO implements Serializable {
    private static final long serialVersionUID = 2218966660876737468L;

    private Integer subjectId;
    private Subject subject;
    private String subjectName;
    private Integer clazzLevel;
    private Integer groupLevel;
    private Long clazzId;
    private Long groupId;
    private String clazzName;
    private Boolean selected;
}
