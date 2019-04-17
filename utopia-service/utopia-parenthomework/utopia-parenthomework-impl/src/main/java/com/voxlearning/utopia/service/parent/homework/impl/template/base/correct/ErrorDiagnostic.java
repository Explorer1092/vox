package com.voxlearning.utopia.service.parent.homework.impl.template.base.correct;

import lombok.Getter;
import lombok.Setter;

/**
 * 错题诊断
 *
 * @author Wenlong Meng
 * @since Mar 22, 2019
 */
@Getter
@Setter
public class ErrorDiagnostic {
    private String qId; //原题ID
    private String errorCause; //错因
    private String courseId; //课程id
    private String courseOrder; //课程顺序-- int，从0开始计数
    private String similarQid; //类题ID
}
