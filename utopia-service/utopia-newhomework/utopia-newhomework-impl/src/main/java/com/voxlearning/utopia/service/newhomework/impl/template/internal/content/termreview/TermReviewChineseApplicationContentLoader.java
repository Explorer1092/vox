package com.voxlearning.utopia.service.newhomework.impl.template.internal.content.termreview;

import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;

import javax.inject.Named;

/**
 * 小学语文，重点复习->积累运用
 *
 * @author zhangbin
 * @since 2017/11/10
 */

@Named
public class TermReviewChineseApplicationContentLoader extends AbstractTermReviewContentLoader {
    @Override
    public TermReviewContentType getTermReviewContentType() {
        return TermReviewContentType.APPLICATION;
    }
}
