package com.voxlearning.utopia.service.newhomework.impl.template.internal.content.termreview;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.util.List;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/11/10
 * \* Time: 11:25 AM
 * \* Description: 语文-课文读背
 * \
 */
@Named
public class TermReviewBasicReadReciteWithScoreContentLoader extends AbstractTermReviewContentLoader{
    @Override
    public TermReviewContentType getTermReviewContentType() {
        return TermReviewContentType.BASIC_READ_RECITE_WITH_SCORE;
    }

    @Override
    public MapMessage loadNewContent(Teacher teacher, List<Long> groupIds, String bookId, TermReviewContentType termReviewContentType) {
        return loadBasicContent(teacher, groupIds, bookId);
    }
}
