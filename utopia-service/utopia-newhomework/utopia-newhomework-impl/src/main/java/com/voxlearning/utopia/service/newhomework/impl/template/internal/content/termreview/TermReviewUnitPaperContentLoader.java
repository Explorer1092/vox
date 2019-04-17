package com.voxlearning.utopia.service.newhomework.impl.template.internal.content.termreview;

import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;

import javax.inject.Named;

/**
 * 小学英语，模考卷->单元模拟卷
 *
 * @author zhangbin
 * @since 2017/11/9
 */

@Named
public class TermReviewUnitPaperContentLoader extends AbstractTermReviewContentLoader {
    @Override
    public TermReviewContentType getTermReviewContentType() {
        return TermReviewContentType.UNIT_PAPER;
    }
}
