package com.voxlearning.utopia.service.newhomework.impl.template.daite;

import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;
import com.voxlearning.utopia.service.newhomework.impl.template.DaiTeTypeTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.internal.content.NewHomeworkIntelligentTeachingContentLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;


/**
 * \* Created: liuhuichao
 * \* Date: 2019/2/26
 * \* Time: 3:36 PM
 * \* Description: 讲练测
 * \
 */
@Named
public class DaiTeIntelligentTeachingTypeTemple implements DaiTeTypeTemplate {

    @Inject
    private NewHomeworkIntelligentTeachingContentLoader newHomeworkIntelligentTeachingContentLoader;

    @Override
    public DaiTeType getDaiTeType() {
        return DaiTeType.INTELLIGENT_TEACHING;
    }

    @Override
    public Object getDaiTeDataByType(NewHomeworkContentLoaderMapper mapper, Map params) {
        return newHomeworkIntelligentTeachingContentLoader.loadContent(mapper);
    }
}
