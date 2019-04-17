package com.voxlearning.utopia.agent.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtilsBean2;
import com.voxlearning.athena.api.LoadVacationHwService;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumVacationHwIndicator;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * LoadVacationHwClient
 *
 * @author deliang.che
 * @since  2019/1/2
 */
@Named
public class LoadVacationHwClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ImportService(interfaceClass = LoadVacationHwService.class)
    private LoadVacationHwService loadVacationHwService;

    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private LoadNewSchoolServiceClient loadNewSchoolServiceClient;


    public Map<Long, SumVacationHwIndicator> loadVacationHwSumIndicator(Collection<Long> ids, Integer idType, Integer day, Collection<Integer> schoolLevels, Integer subjectCode){
        Map<Long, SumVacationHwIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || CollectionUtils.isEmpty(schoolLevels) || idType == null){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);

        Map<Long, AgentGroup> groupMap = new HashMap<>();
        Map<Long, AgentUser> userMap = new HashMap<>();
        if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
            Map<Long, AgentGroup> tmpGroupMap = baseOrgService.getGroupByIds(ids).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
            if(MapUtils.isNotEmpty(tmpGroupMap)){
                groupMap.putAll(tmpGroupMap);
            }
        }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
            Map<Long, AgentUser> tmpUserMap = baseOrgService.getUsers(ids).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));
            if(MapUtils.isNotEmpty(tmpUserMap)){
                userMap.putAll(tmpUserMap);
            }
        }

        ids.forEach(p -> {
            SumVacationHwIndicator sumVacationHwIndicator = new SumVacationHwIndicator();
            sumVacationHwIndicator.setId(p);
            sumVacationHwIndicator.setIdType(idType);
            sumVacationHwIndicator.setDay(day);
            sumVacationHwIndicator.setSchoolLevel(schoolLevel);
            if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
                AgentGroup group = groupMap.get(p);
                sumVacationHwIndicator.setName(group != null && StringUtils.isNotBlank(group.getGroupName()) ? group.getGroupName() : "");
            }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
                AgentUser user = userMap.get(p);
                sumVacationHwIndicator.setName(user != null && StringUtils.isNotBlank(user.getRealName()) ? user.getRealName() : "");
            }
            resultMap.put(p, sumVacationHwIndicator);
        });

        List<List<Long>> splitIds = loadNewSchoolServiceClient.splitListPub(ids, 100);

        List<Future<MapMessage>> futureList = new ArrayList<>();
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadVacationHwService.vacationHwSummary(itemList, idType, schoolLevels, day, subjectCode)));
        }

        for(Future<MapMessage> future : futureList) {
            try {
                MapMessage msg = future.get();
                if (msg == null || !msg.isSuccess()) {
                    continue;
                }

                Map<Long, Object> dataMap = (Map<Long, Object>) msg.get("dataMap");
                if (MapUtils.isEmpty(dataMap)) {
                    continue;
                }

                dataMap.forEach((k, v) -> {
                    SumVacationHwIndicator sumVacationHwIndicator = resultMap.get(k);
                    if (sumVacationHwIndicator == null) {
                        return;
                    }
                    try {
                        BeanUtilsBean2.getInstance().copyProperties(sumVacationHwIndicator, v);
                    } catch (Exception e) {
                        logger.error("sumVacationHwIndicator copy error", e);
                        return;
                    }
                });

            } catch (Exception e) {
                String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                logger.error(methodName + " error", e);
                loadNewSchoolServiceClient.sendMethodErrorEmailPub(methodName, ids, idType, schoolLevels, day,subjectCode);
            }
        }
        return resultMap;
    }

}
