package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.constants.AgentMemorandumGenre;
import com.voxlearning.utopia.agent.persist.entity.memorandum.AgentMemorandum;
import com.voxlearning.utopia.agent.service.memorandum.AgentMemorandumService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 修改老师的备忘录的时间
 * Created by yaguang.wang
 * on 2017/6/16.
 */
@Named
public class UpdateTeacherMemorandumHandler {
    @Inject private AgentMemorandumService agentMemorandumService;

    public void handle() {
        for (int i = 1; i <= 12; i++) {
            List<AgentMemorandum> allMemorandum = agentMemorandumService.loadAll(i, 10000);
            if (CollectionUtils.isEmpty(allMemorandum)) {
                continue;
            }
            allMemorandum.forEach(p -> {
                if (p.getTeacherId() == null) {
                    p.setGenre(AgentMemorandumGenre.SCHOOL);
                } else {
                    p.setGenre(AgentMemorandumGenre.TEACHER);
                }
                agentMemorandumService.updateMemorandumNoWriteTime(p);
            });
        }
    }
}
