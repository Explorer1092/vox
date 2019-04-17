package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.CorrectHomeworkService;
import com.voxlearning.utopia.service.parent.homework.api.model.BizType;
import com.voxlearning.utopia.service.parent.homework.api.model.Command;
import com.voxlearning.utopia.service.parent.homework.api.model.CorrectParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectBaseTemplate;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * 订正作业服务实现：主要提供订正作业功能
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Named
@ExposeService(interfaceClass = CorrectHomeworkService.class)
@Slf4j
public class CorrectHomeworkServiceImpl implements CorrectHomeworkService {

    //local variables

    /**
     * 做作业
     *
     * @param param
     * @return
     */
    @Override
    public MapMessage dos(CorrectParam param) {
        return CorrectBaseTemplate.exec(param);
    }
}