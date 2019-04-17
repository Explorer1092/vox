package com.voxlearning.utopia.agent.service.sysconfig;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.constants.AgentDateConfigType;
import com.voxlearning.utopia.agent.dao.mongo.AgentDateConfigDao;
import com.voxlearning.utopia.agent.persist.entity.AgentDateConfig;
import com.voxlearning.utopia.agent.service.AbstractAgentService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by yaguang.wang
 * on 2017/3/27.
 */
@Named
public class AgentDateConfigService extends AbstractAgentService {
    @Inject private AgentDateConfigDao agentDateConfigDao;

   /* public AgentDateConfig createDateConfig(Integer startTime, Integer endTime) {
        AgentDateConfig config = new AgentDateConfig();
        config.setConfigType(AgentDateConfigType.CITY_MANAGER_CONFIG_SCHOOL);
        config.setStartDay(startTime);
        config.setEndDay(endTime);
        return config;
    }*/

    public AgentDateConfig findDateConfigByType(AgentDateConfigType type) {
        if (type == null) {
            return null;
        }
        return agentDateConfigDao.loadDateConfigByType(type);
    }

    public MapMessage updateCityManagerConfigSchoolTime(Integer startTime, Integer endTime) {
        if (startTime == 0 || endTime == 0) {
            return MapMessage.errorMessage("日期格式错误！");
        }
        if (startTime > 31) {
            return MapMessage.errorMessage("开始日期不能大于31日！");
        }
        if (endTime > 31) {
            return MapMessage.errorMessage("结束日期不能大于31日！");
        }
        if (endTime < startTime) {
            return MapMessage.errorMessage("开始日期不能大于结束日期！");
        }
        AgentDateConfig config = findDateConfigByType(AgentDateConfigType.CITY_MANAGER_CONFIG_SCHOOL);
        if (config == null) {
            config = new AgentDateConfig();
            config.setConfigType(AgentDateConfigType.CITY_MANAGER_CONFIG_SCHOOL);
        }
        config.setStartDay(startTime);
        config.setEndDay(endTime);
        agentDateConfigDao.upsert(config);
        return MapMessage.successMessage();
    }
}
