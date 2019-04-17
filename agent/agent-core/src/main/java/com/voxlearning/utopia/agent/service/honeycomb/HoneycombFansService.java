package com.voxlearning.utopia.agent.service.honeycomb;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombFans;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombUser;
import com.voxlearning.utopia.agent.persist.honeycomb.HoneycombFansDao;
import com.voxlearning.utopia.agent.persist.honeycomb.HoneycombUserDao;
import com.voxlearning.utopia.core.utils.MQUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Named
public class HoneycombFansService {

    @Inject
    private HoneycombFansDao fansDao;
    @Inject
    private HoneycombUserDao userDao;
    @Inject
    private HoneycombUserService userService;

    // 只关注市场专员对应的蜂巢账号的直接粉丝， 其他的不关注
    public void handleMessageData(Long honeycombId, Long fansId, Date fansTime){
        if(honeycombId == null || honeycombId < 1 || fansId == null || fansId < 1){
            return;
        }

        HoneycombUser user = userDao.load(honeycombId);
        if(user == null || user.getAgentUserId() == null){    // 非HoneycombUser表中用户的粉丝不进行关注，直接丢弃
            return;
        }

        List<HoneycombFans> fansList = fansDao.loadByHid(honeycombId);
        if(CollectionUtils.isNotEmpty(fansList) && fansList.stream().anyMatch(p -> Objects.equals(p.getFansId(), fansId))){
            return;
        }
        HoneycombFans fans = new HoneycombFans();
        fans.setHoneycombId(honeycombId);
        fans.setFansId(fansId);
        fans.setFansTime(fansTime == null ? new Date() : fansTime);
        fansDao.insert(fans);

        // 粉丝用户进入蜂巢用户表
        AlpsThreadPool.getInstance().submit(() -> userService.saveHoneycombUser(fansId));

        // 粉丝统计
        MQUtils.send(AgentConstants.AGENT_INNER_TOPIC, MapUtils.m("type", "HoneycombFans", "data", JsonUtils.toJson(fans)));

    }
}
