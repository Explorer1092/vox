package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.service.mobile.AgentHiddenTeacherService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * HideAndShowTeacherHandler
 *
 * @author song.wang
 * @date 2018/1/11
 */
@Named
public class HideAndShowTeacherHandler extends SpringContainerSupport {

    @Inject
    private AgentHiddenTeacherService agentHiddenTeacherService;

    public void handle(Collection<Long> teacherIds){
        agentHiddenTeacherService.hideAndShowTeacher(teacherIds);
    }
}
