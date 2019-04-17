package com.voxlearning.utopia.service.newhomework.impl.template.internal.content.termreview;

import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;

import javax.inject.Named;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/11/15
 * \* Time: 8:31 PM
 * \* Description: UNIT_DIAGNOSIS
 * \
 */
@Named
public class TermmReviewUnitDiagnosisContentLoader  extends AbstractTermReviewContentLoader {
    @Override
    public TermReviewContentType getTermReviewContentType() {
        return TermReviewContentType.UNIT_DIAGNOSIS;
    }
}
