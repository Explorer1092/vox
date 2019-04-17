package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkProcessMapper;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

import java.util.Collection;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2016/8/4
 */
abstract public class ProcessResultTemplate extends NewHomeworkSpringBean {

    abstract public SchoolLevel getSchoolLevel();

    abstract public List<HomeworkProcessMapper> getProcessResult(Collection<String> processIds);
}
