package com.voxlearning.utopia.service.newhomework.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkProcessMapper;
import com.voxlearning.utopia.service.newhomework.api.service.BDHomeworkService;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessResultLoaderFactory;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessResultTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2016/8/29
 */
@Named
@Service(interfaceClass = BDHomeworkService.class)
@ExposeService(interfaceClass = BDHomeworkService.class)
public class BDHomeworkServiceImpl implements BDHomeworkService {

    @Inject private ProcessResultLoaderFactory processResultLoaderFactory;

    private final static List<String> goalList = Arrays.asList("JUNIOR_MATH_GOAL", "JUNIOR_ENGLISH_GOAL");

    @Override
    public List<HomeworkProcessMapper> getProcessResultMapper(Collection<String> processId, SchoolLevel schoolLevel) {
        ProcessResultTemplate template = processResultLoaderFactory.getTemplate(schoolLevel);
        return template.getProcessResult(processId);
    }

    @Override
    public List<HomeworkProcessMapper> getProcessResultMapperWithGoal(Collection<String> processId, SchoolLevel schoolLevel, String goal) {
        if (StringUtils.isBlank(goal) || !goalList.contains(goal)) {
            return Collections.emptyList();
        }
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "mod1", processId.size(),
                "mod2", schoolLevel,
                "mod3", goal,
                "op", "BDHomeworkService"
        ));

        ProcessResultTemplate template = processResultLoaderFactory.getTemplate(schoolLevel);
        return template.getProcessResult(processId);
    }
}
