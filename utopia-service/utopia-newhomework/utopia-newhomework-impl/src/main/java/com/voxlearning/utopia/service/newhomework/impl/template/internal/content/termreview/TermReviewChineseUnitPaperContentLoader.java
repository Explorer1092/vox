package com.voxlearning.utopia.service.newhomework.impl.template.internal.content.termreview;

import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;

import javax.inject.Named;

/**
 * 小学语文，重点复习->单元模考卷
 *
 * @author zhangbin
 * @since 2017/11/10
 */

@Named
public class TermReviewChineseUnitPaperContentLoader extends AbstractTermReviewContentLoader {
    @Override
    public TermReviewContentType getTermReviewContentType() {
        return TermReviewContentType.CH_UNIT_PAPER;
    }
}
