package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;

import java.util.List;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/2/26
 * \* Time: 3:20 PM
 * \* Description:处理戴特的题型数据获取
 * \
 */
public interface DaiTeTypeTemplate {

    DaiTeType getDaiTeType();

    Object getDaiTeDataByType(NewHomeworkContentLoaderMapper mapper,Map params);
}
