package com.voxlearning.utopia.service.newhomework.impl.template.internal.content.termreview;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.constant.TermReviewContentType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Named;
import java.util.List;

/**
 * @author guoqiang.li
 * @since 2017/11/14
 */
@Named
public class TermReviewBasicWordContentLoader extends AbstractTermReviewContentLoader {
    @Override
    public TermReviewContentType getTermReviewContentType() {
        return TermReviewContentType.BASIC_WORD;
    }

    @Override
    public MapMessage loadNewContent(Teacher teacher, List<Long> groupIds, String bookId, TermReviewContentType termReviewContentType) {
        return loadBasicContent(teacher, groupIds, bookId);
    }
}
