package com.voxlearning.utopia.service.newhomework.impl.template.internal.content.termreview;

import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;

import javax.inject.Named;

/**
 * 小学英语，重点复习->阅读专项
 *
 * @author zhangbin
 * @since 2017/11/9
 */

@Named
public class TermReviewReadingContentLoader extends AbstractTermReviewContentLoader {
    @Override
    public TermReviewContentType getTermReviewContentType() {
        return TermReviewContentType.READING;
    }
}
