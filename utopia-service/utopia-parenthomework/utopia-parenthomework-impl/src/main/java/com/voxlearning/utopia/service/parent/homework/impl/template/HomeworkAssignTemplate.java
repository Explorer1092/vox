package com.voxlearning.utopia.service.parent.homework.impl.template;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;

/**
 * 布置作业模板
 *
 * @author Wenlong Meng
 * @date 20181120
 */
public interface HomeworkAssignTemplate {

    /**
     * 布置作业
     *
     * @param param
     * @return
     */
    MapMessage assign(HomeworkParam param);
}
