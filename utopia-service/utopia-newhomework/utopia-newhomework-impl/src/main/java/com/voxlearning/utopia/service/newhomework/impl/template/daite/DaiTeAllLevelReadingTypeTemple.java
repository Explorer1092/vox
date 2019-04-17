package com.voxlearning.utopia.service.newhomework.impl.template.daite;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkContentServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.template.DaiTeTypeTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/3/12
 * \* Time: 5:50 PM
 * \* Description: 全部绘本内容加载
 * \
 */
@Named
public class DaiTeAllLevelReadingTypeTemple implements DaiTeTypeTemplate {
    @Override
    public DaiTeType getDaiTeType() {
        return DaiTeType.ALL_LEVEL_READINGS;
    }

    @Inject
    private NewHomeworkContentServiceImpl newHomeworkContentService;

    @Override
    public Object getDaiTeDataByType(NewHomeworkContentLoaderMapper mapper, Map params) {
        Pageable page = params.get("page") == null ? null : (Pageable) params.get("page");
        String levelReadingsClazzLevel = SafeConverter.toString(params.get("levelReadingsClazzLevel"));
        String topicIds = SafeConverter.toString(params.get("topicIds"));
        String seriesIds = SafeConverter.toString(params.get("seriesIds"));
        List<String> topicIdList = Arrays.stream(topicIds.trim().split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        List<String> seriesIdList = Arrays.stream(seriesIds.trim().split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        TeacherDetail teacherDetail = mapper.getTeacher();
        return newHomeworkContentService.searchPictureBookPlus(teacherDetail, levelReadingsClazzLevel, topicIdList, seriesIdList, "", mapper.getBookId(), mapper.getUnitId(), page, "", "");
    }
}
