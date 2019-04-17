package com.voxlearning.utopia.service.newhomework.impl.template.daite;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;
import com.voxlearning.utopia.service.newhomework.impl.template.DaiTeTypeTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.content.NewHomeworkBasicAppContentLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/2/26
 * \* Time: 3:37 PM
 * \* Description: 基础练习
 * \
 */
@Named
public class DaiTeBasicAppTypeTemple implements DaiTeTypeTemplate {

    @Inject
    private NewHomeworkBasicAppContentLoader newHomeworkBasicAppContentLoader;

    private static List<Integer> FILTER_CATEGORY_IDS = Arrays.asList(10310, 10305, 10303, 10304);

    @Override
    public DaiTeType getDaiTeType() {
        return DaiTeType.BASIC_APP;
    }

    @Override
    public Object getDaiTeDataByType(NewHomeworkContentLoaderMapper mapper, Map params) {
        List<Map<String, Object>> content = newHomeworkBasicAppContentLoader.getHomeworkContent(mapper.getTeacher(), mapper.getUnitId(), mapper.getBookId());
        if (CollectionUtils.isNotEmpty(content)) {
            // 过滤掉跟读类应用
            List<Map<String, Object>> filteredContent = new ArrayList<>();
            for (Map<String, Object> contentGroup : content) {
                List<Map<String, Object>> lessons = (List<Map<String, Object>>) contentGroup.get("lessons");
                if (CollectionUtils.isNotEmpty(lessons)) {
                    List<Map<String, Object>> filteredLessons = new ArrayList<>();
                    for (Map<String, Object> lesson : lessons) {
                        List<Map<String, Object>> categories = (List<Map<String, Object>>) lesson.get("categories");
                        if (CollectionUtils.isNotEmpty(categories)) {
                            List<Map<String, Object>> filteredCategories = new ArrayList<>();
                            for (Map<String, Object> category : categories) {
                                int categoryId = SafeConverter.toInt(category.get("categoryId"));
                                if (!FILTER_CATEGORY_IDS.contains(categoryId)) {
                                    filteredCategories.add(category);
                                }
                            }
                            if (CollectionUtils.isNotEmpty(filteredCategories)) {
                                lesson.put("categories", filteredCategories);
                                filteredLessons.add(lesson);
                            }
                        }
                    }
                    if (CollectionUtils.isNotEmpty(filteredLessons)) {
                        contentGroup.put("lessons", filteredLessons);
                        filteredContent.add(contentGroup);
                    }
                }
            }
            content = filteredContent;
        }
        return content;
    }
}
