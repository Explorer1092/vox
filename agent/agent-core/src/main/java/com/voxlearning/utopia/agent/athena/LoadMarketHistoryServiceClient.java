package com.voxlearning.utopia.agent.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtilsBean2;
import com.voxlearning.athena.api.LoadMarketHistoryService;
import com.voxlearning.utopia.agent.bean.indicator.history.HistoryOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.history.HistoryParentIndicator;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class LoadMarketHistoryServiceClient {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ImportService(interfaceClass = LoadMarketHistoryService.class)
    private LoadMarketHistoryService loadMarketHistoryService;

    @Inject
    private LoadNewSchoolServiceClient loadNewSchoolServiceClient;

    public Map<Integer, HistoryParentIndicator> loadParentHistory(Long id, Integer idType, Collection<Integer> days, Collection<Integer> schoolLevels){
        if(CollectionUtils.isEmpty(days) || id == null || CollectionUtils.isEmpty(schoolLevels) || idType == null){
            return Collections.emptyMap();
        }
        List<HistoryParentIndicator> parentIndicatorList = new ArrayList<>();
        try{
            MapMessage msg = loadMarketHistoryService.loadParentHistory(Collections.singleton(id), idType, days, schoolLevels);
            if (msg == null || !msg.isSuccess()) {
                return Collections.emptyMap();
            }
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) msg.get("dataMap");
            if (CollectionUtils.isEmpty(dataList)) {
                return Collections.emptyMap();
            }
            dataList.forEach(item -> {
                HistoryParentIndicator parentIndicator = new HistoryParentIndicator();
                try {
                    Map<String, Object> dataMap = (Map<String, Object>) item;
                    if (MapUtils.isNotEmpty(dataMap)){
                        BeanUtilsBean2.getInstance().copyProperties(parentIndicator, dataMap);
                    }
                } catch (Exception e) {
                    logger.error("history parent indicator error", e);
                    return;
                }
                parentIndicatorList.add(parentIndicator);
            });
        }catch (Exception e){
            String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
            logger.error(methodName + " error", e);
            loadNewSchoolServiceClient.sendMethodErrorEmailPub(methodName,id, idType, days,schoolLevels);
        }
        return parentIndicatorList.stream().collect(Collectors.toMap(HistoryParentIndicator::getDate, Function.identity(), (o1, o2) -> o1));
    }

    public Map<Integer,HistoryOnlineIndicator> loadHwHistory(Long id, Integer idType, Collection<Integer> days, Collection<Integer> schoolLevels){
        if(CollectionUtils.isEmpty(days) || id == null || CollectionUtils.isEmpty(schoolLevels) || idType == null){
            return Collections.emptyMap();
        }
        List<HistoryOnlineIndicator> onlineIndicatorList = new ArrayList<>();
        try{
            MapMessage msg = loadMarketHistoryService.loadHwHistory(Collections.singleton(id), idType, days,schoolLevels);
            if (msg == null || !msg.isSuccess()) {
                return Collections.emptyMap();
            }
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) msg.get("dataMap");
            if (CollectionUtils.isEmpty(dataList)) {
                return Collections.emptyMap();
            }
            dataList.forEach(item -> {
                HistoryOnlineIndicator onlineIndicator = new HistoryOnlineIndicator();
                try {
                    Map<String, Object> dataMap = (Map<String, Object>) item;
                    if (MapUtils.isNotEmpty(dataMap)){
                        BeanUtilsBean2.getInstance().copyProperties(onlineIndicator, dataMap);
                    }
                } catch (Exception e) {
                    logger.error("history online indicator error", e);
                    return;
                }
                onlineIndicatorList.add(onlineIndicator);
            });
        }catch (Exception e){
            String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
            logger.error(methodName + " error", e);
            loadNewSchoolServiceClient.sendMethodErrorEmailPub(methodName,id, idType, days,schoolLevels);
        }
        return onlineIndicatorList.stream().collect(Collectors.toMap(HistoryOnlineIndicator::getDate, Function.identity(), (o1, o2) -> o1));
    }
}
