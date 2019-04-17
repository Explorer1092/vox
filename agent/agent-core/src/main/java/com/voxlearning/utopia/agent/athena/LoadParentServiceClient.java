package com.voxlearning.utopia.agent.athena;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtilsBean2;
import com.voxlearning.athena.api.LoadMarket19ParentService;
import com.voxlearning.utopia.agent.bean.indicator.ParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.clazz.ClassParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.grade.GradeParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumParentIndicator;
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
 * LoadParentServiceClient
 *
 * @author deliang.che
 * @since  2019/2/21
 */
@Named
public class LoadParentServiceClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ImportService(interfaceClass = LoadMarket19ParentService.class)
    private LoadMarket19ParentService loadMarket19ParentService;

    @Inject
    private LoadNewSchoolServiceClient loadNewSchoolServiceClient;
    @Inject
    private BaseOrgService baseOrgService;

    private static final List<Integer> DIMENSION_LIST = Arrays.asList(1, 2, 3, 4);

    public Map<Long, SumParentIndicator> loadGroupSumData(Collection<Long> groupIds, Integer day, List<Integer> schoolLevels){
        return loadSumParentIndicator(groupIds, AgentConstants.INDICATOR_TYPE_GROUP,day, schoolLevels);
    }

    public Map<Long, SumParentIndicator> loadUserSumData(Collection<Long> userIds, Integer day, List<Integer> schoolLevels){
        return loadSumParentIndicator(userIds, AgentConstants.INDICATOR_TYPE_USER,day, schoolLevels);
    }

    public Map<Long, SchoolParentIndicator> loadSchoolParentIndicator(Collection<Long> ids, Integer day){
        Map<Long, SchoolParentIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            SchoolParentIndicator schoolParentIndicator = new SchoolParentIndicator();
            schoolParentIndicator.setSchoolId(p);
            schoolParentIndicator.setDay(day);
            resultMap.put(p, schoolParentIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = loadNewSchoolServiceClient.splitListPub(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadMarket19ParentService.loadSchoolDetailData(itemList, day, dimension)));
            }
            futureDataMap.put(dimension, futureList);
        }

        if(MapUtils.isNotEmpty(futureDataMap)){
            for(Integer dimension : DIMENSION_LIST){
                List<Future<MapMessage>> futureList = futureDataMap.get(dimension);
                for(Future<MapMessage> future : futureList){
                    try{
                        MapMessage msg = future.get();
                        if(msg == null || !msg.isSuccess()){
                            continue;
                        }

                        Map<Long, Object> dataMap = (Map<Long, Object>) msg.get("dataMap");
                        if(MapUtils.isEmpty(dataMap)){
                            continue;
                        }

                        dataMap.forEach((k, v) -> {
                            SchoolParentIndicator schoolParentIndicator = resultMap.get(k);
                            if(schoolParentIndicator == null){
                                return;
                            }

                            ParentIndicator parentIndicator = new ParentIndicator();
                            try{
                                BeanUtilsBean2.getInstance().copyProperties(parentIndicator, v);
                            }catch (Exception e){
                                logger.error("parent indicator error", e);
                            }
                            schoolParentIndicator.getIndicatorMap().put(dimension, parentIndicator);
                        });
                    }catch (Exception e){
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        loadNewSchoolServiceClient.sendMethodErrorEmailPub(methodName, ids, day, dimension);
                    }
                }
            }
        }
        return resultMap;
    }

    public GradeParentIndicator loadGradeParentIndicator(Long schoolId, Integer day){
        if(schoolId == null || day == null){
            return null;
        }

        GradeParentIndicator result = new GradeParentIndicator();
        result.setSchoolId(schoolId);
        result.setDay(day);

        Map<Integer, Future<MapMessage>> futureDataMap = new HashMap<>();
        for(Integer dimension : DIMENSION_LIST){
            Future<MapMessage> futureData = AlpsThreadPool.getInstance().submit(() -> loadMarket19ParentService.loadGradeDetailData(schoolId, day, dimension)) ;
            futureDataMap.put(dimension, futureData);
        }

        if(MapUtils.isNotEmpty(futureDataMap)){
            for(Integer dimension : DIMENSION_LIST){
                try {
                    MapMessage msg = futureDataMap.get(dimension).get();
                    if(msg == null || !msg.isSuccess()){
                        continue;
                    }

                    Map<Long, Object> dataMap = (Map<Long, Object>) msg.get("dataMap");
                    if(MapUtils.isEmpty(dataMap)){
                        continue;
                    }

                    dataMap.forEach((k, v) -> {
                        //高一、高二、高三年级与大数据对应不一致，中间做相应转化
                        if (k == 10 || k == 11 || k == 12){
                            k ++;
                        }
                        ClazzLevel grade = ClazzLevel.parse(SafeConverter.toInt(k));
                        if(grade == null || grade == ClazzLevel.INFANT_GRADUATED|| grade == ClazzLevel.PRIMARY_GRADUATED|| grade == ClazzLevel.MIDDLE_GRADUATED){
                            return;
                        }

                        Map<Integer, ParentIndicator> gradDataMap = result.getIndicatorMap().computeIfAbsent(grade, k1 -> new HashMap<>());

                        ParentIndicator parentIndicator = new ParentIndicator();
                        try{
                            BeanUtilsBean2.getInstance().copyProperties(parentIndicator, v);
                        }catch (Exception e){
                            logger.error("parent indicator error", e);
                        }
                        gradDataMap.put(dimension, parentIndicator);
                    });

                }catch (Exception e){
                    String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                    logger.error(methodName + " error", e);
                    loadNewSchoolServiceClient.sendMethodErrorEmailPub(methodName, schoolId, day, dimension);
                }
            }
        }
        return result;
    }


    public Map<Long, ClassParentIndicator> loadClassParentIndicator(Collection<Long> ids, Integer day){
        Map<Long, ClassParentIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            ClassParentIndicator classParentIndicator = new ClassParentIndicator();
            classParentIndicator.setClassId(p);
            classParentIndicator.setDay(day);
            resultMap.put(p, classParentIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = loadNewSchoolServiceClient.splitListPub(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadMarket19ParentService.loadClassDetailData(itemList, day, dimension)));
            }
            futureDataMap.put(dimension, futureList);
        }

        if(MapUtils.isNotEmpty(futureDataMap)){
            for(Integer dimension : DIMENSION_LIST){
                List<Future<MapMessage>> futureList = futureDataMap.get(dimension);
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
                            ClassParentIndicator ClassParentIndicator = resultMap.get(k);
                            if (ClassParentIndicator == null) {
                                return;
                            }

                            ParentIndicator parentIndicator = new ParentIndicator();
                            try {
                                BeanUtilsBean2.getInstance().copyProperties(parentIndicator, v);
                            } catch (Exception e) {
                                logger.error("parent indicator error", e);
                            }
                            ClassParentIndicator.getIndicatorMap().put(dimension, parentIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        loadNewSchoolServiceClient.sendMethodErrorEmailPub(methodName, ids, day, dimension);
                    }
                }
            }
        }
        return resultMap;
    }


    public Map<Long, SumParentIndicator> loadSumParentIndicator(Collection<Long> ids, Integer idType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumParentIndicator> resultMap = new HashMap<>();
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
            SumParentIndicator sumParentIndicator = new SumParentIndicator();
            sumParentIndicator.setId(p);
            sumParentIndicator.setIdType(idType);
            sumParentIndicator.setDay(day);
            sumParentIndicator.setSchoolLevel(schoolLevel);
            if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
                AgentGroup group = groupMap.get(p);
                sumParentIndicator.setName(group != null && StringUtils.isNotBlank(group.getGroupName()) ? group.getGroupName() : "");
            }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
                AgentUser user = userMap.get(p);
                sumParentIndicator.setName(user != null && StringUtils.isNotBlank(user.getRealName()) ? user.getRealName() : "");
            }
            resultMap.put(p, sumParentIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = loadNewSchoolServiceClient.splitListPub(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadMarket19ParentService.loadOrgSumData(itemList, idType, schoolLevels, day, dimension)));
            }
            futureDataMap.put(dimension, futureList);
        }

        if(MapUtils.isNotEmpty(futureDataMap)){
            for(Integer dimension : DIMENSION_LIST){
                List<Future<MapMessage>> futureList = futureDataMap.get(dimension);
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
                            SumParentIndicator sumParentIndicator = resultMap.get(k);
                            if (sumParentIndicator == null) {
                                return;
                            }

                            ParentIndicator parentIndicator = new ParentIndicator();
                            try {
                                BeanUtilsBean2.getInstance().copyProperties(parentIndicator, v);
                            } catch (Exception e) {
                                logger.error("parent indicator error", e);
                                return;
                            }
                            sumParentIndicator.getIndicatorMap().put(dimension, parentIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        loadNewSchoolServiceClient.sendMethodErrorEmailPub(methodName, ids, idType, schoolLevels, day, dimension);
                    }
                }
            }
        }
        return resultMap;
    }


    public SumParentIndicator loadSchoolSumParentIndicator(Collection<Long> ids, Integer day){
        if(CollectionUtils.isEmpty(ids) || day == null){
            return null;
        }

        SumParentIndicator sumParentIndicator = new SumParentIndicator();
        sumParentIndicator.setDay(day);

        Map<Integer, Future<MapMessage>> futureDataMap = new HashMap<>();
        for(Integer dimension : DIMENSION_LIST){
            Future<MapMessage> futureData = AlpsThreadPool.getInstance().submit(() -> loadMarket19ParentService.loadSchoolSumData(ids, day, dimension)) ;
            futureDataMap.put(dimension, futureData);
        }

        if(MapUtils.isNotEmpty(futureDataMap)){
            for(Integer dimension : DIMENSION_LIST){
                try {
                    MapMessage msg = futureDataMap.get(dimension).get();
                    if(msg == null || !msg.isSuccess()){
                        continue;
                    }

                    Map<String, Object> dataMap = (Map<String, Object>) msg.get("dataMap");
                    if(MapUtils.isEmpty(dataMap)){
                        continue;
                    }

                    ParentIndicator parentIndicator = new ParentIndicator();
                    try{
                        BeanUtilsBean2.getInstance().copyProperties(parentIndicator, dataMap);
                    }catch (Exception e){
                        logger.error("parent indicator error", e);
                        continue;
                    }
                    sumParentIndicator.getIndicatorMap().put(dimension, parentIndicator);
                }catch (Exception e){
                    String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                    logger.error(methodName + " error", e);
                    loadNewSchoolServiceClient.sendMethodErrorEmailPub(methodName, ids, day, dimension);
                }
            }
        }
        return sumParentIndicator;
    }
}
