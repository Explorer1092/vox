package com.voxlearning.utopia.service.newhomework.impl.template.internal.content.termreview;

import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;

import javax.inject.Named;

/**
 * 小学数学，重点复习->班级错题
 *
 * @author zhangbin
 * @since 2017/11/9
 */

@Named
public class TermReviewMathClazzErrorContentLoader extends AbstractTermReviewContentLoader {
    @Override
    public TermReviewContentType getTermReviewContentType() {
        return TermReviewContentType.MATH_CLAZZ_ERROR;
    }
}
