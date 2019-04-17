package com.voxlearning.utopia.agent.service.indicator.support.online;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicatorWithBudget;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author song.wang
 * @date 2018/11/6
 */
public abstract class OnlineIndicatorFactory {

    @Inject
    protected LoadNewSchoolServiceClient loadNewSchoolServiceClient;

    /**
     * 生成概览数据
     * @param id id
     * @param dataType AgentConstants.INDICATOR_TYPE_USER
     * @param day 日期：20190101
     * @param schoolLevelFlag 1， 24
     * @return
     */
    public abstract SumOnlineIndicatorWithBudget generateOverview(Long id, Integer dataType, Integer day, Integer schoolLevelFlag);

    /**
     * 生成部门数据
     * @param groupIds ids
     * @param day  日期：20190101
     * @param schoolLevelFlag
     * @return
     */
    public abstract Map<Long, SumOnlineIndicatorWithBudget> generateGroupDataList(Collection<Long> groupIds, Integer day, Integer schoolLevelFlag);

    /**
     * 生成用户数据
     * @param userIds ids
     * @param day 日期：20190101
     * @param schoolLevelFlag
     * @return
     */
    public abstract Map<Long, SumOnlineIndicatorWithBudget> generateUserDataList(Collection<Long> userIds, Integer day, Integer schoolLevelFlag);

    /**
     * 生成未分配数据
     * @param groupId id
     * @param day 日期：20190101
     * @param schoolLevelFlag
     * @return
     */
    public abstract SumOnlineIndicatorWithBudget generateUnallocatedData(Long groupId, Integer day, Integer schoolLevelFlag);

    /**
     * 生成学校数据
     * @param id id
     * @param dataType
     * @param day 日期：20190101
     * @param schoolLevelFlag
     * @return
     */
    public Map<Long, SchoolOnlineIndicator> generateSchoolData(Long id, Integer dataType, Integer day, Integer schoolLevelFlag){
        Collection<Long> schoolIds = fetchSchoolList(id, dataType, schoolLevelFlag);
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        return loadNewSchoolServiceClient.loadSchoolOnlineIndicator(schoolIds, day);
    }

    public abstract Collection<Long> fetchSchoolList(Long id, Integer dataType, Integer schoolLevelFlag);
}
