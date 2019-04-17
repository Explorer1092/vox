package com.voxlearning.utopia.agent.service.indicator.support.offline;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicatorWithBudget;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * OfflineIndicatorFactory
 *
 * @author song.wang
 * @date 2018/11/7
 */
public abstract class OfflineIndicatorFactory {
    @Inject
    protected LoadNewSchoolServiceClient loadNewSchoolServiceClient;

    public abstract SumOfflineIndicatorWithBudget generateOverview(Long id, Integer dataType, Integer day, Integer schoolLevelFlag);

    public abstract Map<Long, SumOfflineIndicatorWithBudget> generateGroupDataList(Collection<Long> groupIds, Integer day, Integer schoolLevelFlag);

    public abstract Map<Long, SumOfflineIndicatorWithBudget> generateUserDataList(Collection<Long> userIds, Integer day, Integer schoolLevelFlag);

    public abstract SumOfflineIndicatorWithBudget generateUnallocatedData(Long groupId, Integer day, Integer schoolLevelFlag);

    public Map<Long, SchoolOfflineIndicator> generateSchoolData(Long id, Integer dataType, Integer day, Integer schoolLevelFlag){
        Collection<Long> schoolIds = fetchSchoolList(id, dataType, schoolLevelFlag);
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        return loadNewSchoolServiceClient.loadSchoolOfflineIndicator(schoolIds, day);
    }

    public abstract Collection<Long> fetchSchoolList(Long id, Integer dataType, Integer schoolLevelFlag);
}
