package com.voxlearning.utopia.service.newhomework.impl.template.daite;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;
import com.voxlearning.utopia.service.newhomework.impl.template.DaiTeTypeTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.question.api.entity.FeeCourse;
import com.voxlearning.utopia.service.question.api.entity.FeeCourseContent;
import com.voxlearning.utopia.service.question.consumer.FeeCourseLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.*;
import java.util.stream.Collectors;


/**
 * \* Created: liuhuichao
 * \* Date: 2019/2/27
 * \* Time: 3:27 PM
 * \* Description: 错题宝
 * \
 */
@Named
public class DaiTeCuoTiBaoTypeTemple implements DaiTeTypeTemplate {

    @Inject
    private FeeCourseLoaderClient feeCourseLoaderClient;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Override
    public DaiTeType getDaiTeType() {
        return DaiTeType.CUO_TI_BAO;
    }

    @Override
    public Object getDaiTeDataByType(NewHomeworkContentLoaderMapper mapper, Map params) {
        NewBookProfile book = null;
        Map<String, NewBookProfile> bookMap = newContentLoaderClient.loadBooks(Collections.singleton(mapper.getBookId()));
        if (MapUtils.isNotEmpty(bookMap)) {
            book = bookMap.get(mapper.getBookId());
        }
        if (book == null) {
            return null;
        }
        //status:"ONLINE" , book_series : "COMMON" ，去掉六年级下的数学和语文的数据
        Subject subject = mapper.getTeacher().getSubject();
        Integer subjectId = subject.getId();
        Integer clazzLevel = SafeConverter.toInt(params.get("clazzLevel"));
        String bookSeries = "COMMON";
        Integer termType = SafeConverter.toInt(params.get("termType"));
        if ((clazzLevel == 6 && termType == 2) && (subject == Subject.MATH || subject == Subject.CHINESE)) {
            return null;
        }
        if (clazzLevel == 0 || termType == 0) {
            return null;
        }

        List<FeeCourse> feeCourseList = feeCourseLoaderClient.loadFeeCoursesByConditions(subjectId, clazzLevel, bookSeries, termType);
        if (CollectionUtils.isEmpty(feeCourseList)) {
            return null;
        }
        List<String> feeCourseIds = feeCourseList.stream().filter(Objects::nonNull).map(FeeCourse::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(feeCourseIds)) {
            return null;
        }
        List<FeeCourseContent> feeCourseContentList = feeCourseLoaderClient.loadFeeCourseContentByFeeCourseIds(feeCourseIds);
        if (CollectionUtils.isEmpty(feeCourseContentList)) {
            return null;
        }
        return feeCourseContentList
                .stream()
                .filter(Objects::nonNull)
                .filter(content -> "LESSON".equals(content.getNodeType()))
                .filter(content -> !content.isDeleted())
                .collect(Collectors.toList());
    }
}
