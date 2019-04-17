package com.voxlearning.utopia.agent.athena;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtilsBean2;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.athena.api.LoadNewSchoolService;
import com.voxlearning.athena.api.LoadNewSchoolV2Service;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.clazz.ClassOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.clazz.ClassOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.grade.GradeOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.grade.GradeOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.group.GroupOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.group.GroupOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.teacher.TeacherOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.teacher.TeacherOnlineIndicator;
import com.voxlearning.utopia.agent.bean.school.AgentHighPotentialSchoolInfo;
import com.voxlearning.utopia.agent.bean.school.AgentMauTopSchoolInfo;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * LoadNewSchoolServiceClient
 *
 * @author song.wang
 * @date 2018/8/2
 */
@Named
public class LoadNewSchoolServiceClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ImportService(interfaceClass = LoadNewSchoolService.class)
    private LoadNewSchoolService loadNewSchoolV1Service;

    @ImportService(interfaceClass = LoadNewSchoolV2Service.class)
    private LoadNewSchoolV2Service loadNewSchoolService;

    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private BaseOrgService baseOrgService;

    private static final List<Integer> DIMENSION_LIST = Arrays.asList(1, 2, 3, 4);


    private List<List<Long>> splitList(Collection<Long> ids, int size){
        List<List<Long>> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(ids)){
            return resultList;
        }
        if(size < 1){
            List<Long> item = new ArrayList<>(ids);
            resultList.add(item);
            return resultList;
        }
        List<Long> idList = new ArrayList<>(ids);
        Map<Integer, List<Long>> map = idList.stream().collect(Collectors.groupingBy(p -> idList.indexOf(p) / size, Collectors.toList()));
        map.values().forEach(resultList::add);
        return resultList;
    }

    public List<List<Long>> splitListPub(Collection<Long> ids, int size){
        return splitList(ids,size);
    }

    public Map<Long, SchoolOnlineIndicator> loadSchoolOnlineIndicator(Collection<Long> ids, Integer day){
        Map<Long, SchoolOnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            SchoolOnlineIndicator schoolOnlineIndicator = new SchoolOnlineIndicator();
            schoolOnlineIndicator.setSchoolId(p);
            schoolOnlineIndicator.setDay(day);
            resultMap.put(p, schoolOnlineIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadSchoolDetailsData(itemList, day, AgentConstants.MODE_ONLINE, dimension)));
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
                            SchoolOnlineIndicator schoolOnlineIndicator = resultMap.get(k);
                            if(schoolOnlineIndicator == null){
                                return;
                            }

                            OnlineIndicator onlineIndicator = new OnlineIndicator();
                            try{
                                BeanUtilsBean2.getInstance().copyProperties(onlineIndicator, v);
                            }catch (Exception e){
                                logger.error("online indicator error", e);
                            }
                            schoolOnlineIndicator.getIndicatorMap().put(dimension, onlineIndicator);
                        });
                    }catch (Exception e){
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, day, dimension);
                    }
                }
            }
        }

        return resultMap;
    }

    public Map<Long, SchoolOnlineIndicator> loadSchoolOnlineIndicator(Collection<Long> ids, Integer day, Collection<Integer> dimensions){
        Map<Long, SchoolOnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || CollectionUtils.isEmpty(dimensions)){
            return resultMap;
        }

        ids.forEach(p -> {
            SchoolOnlineIndicator schoolOnlineIndicator = new SchoolOnlineIndicator();
            schoolOnlineIndicator.setSchoolId(p);
            schoolOnlineIndicator.setDay(day);
            resultMap.put(p, schoolOnlineIndicator);
        });

        Map<Integer, List<Future<Boolean>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : dimensions){
            List<Future<Boolean>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadSchoolOnlineData(resultMap, itemList, day, dimension)));
            }
            futureDataMap.put(dimension, futureList);
        }

        if(MapUtils.isNotEmpty(futureDataMap)){
            for(Integer dimension : dimensions){
                List<Future<Boolean>> futureList = futureDataMap.get(dimension);
                for(Future<Boolean> future : futureList){
                    try{
                        Boolean result = future.get();
                        if(!SafeConverter.toBoolean(result)){
                            String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                            sendMethodErrorEmail(methodName, ids, day, dimension);
                        }
                    }catch (Exception e){
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, day, dimension);
                    }
                }
            }
        }

        return resultMap;
    }

    private Boolean loadSchoolOnlineData(Map<Long, SchoolOnlineIndicator> resultMap, Collection<Long> schoolIds , Integer day, Integer dimension){
        MapMessage msg = loadNewSchoolService.loadSchoolDetailsData(schoolIds, day, AgentConstants.MODE_ONLINE, dimension);
        if(msg == null || !msg.isSuccess()){
            return false;
        }

        Map<Long, Object> dataMap = (Map<Long, Object>) msg.get("dataMap");
        if(MapUtils.isEmpty(dataMap)){
            return false;
        }

        dataMap.forEach((k, v) -> {
            SchoolOnlineIndicator schoolOnlineIndicator = resultMap.get(k);
            if(schoolOnlineIndicator == null){
                return;
            }

            OnlineIndicator onlineIndicator = new OnlineIndicator();
            try{
                BeanUtilsBean2.getInstance().copyProperties(onlineIndicator, v);
            }catch (Exception e){
                logger.error("online indicator error", e);
            }
            schoolOnlineIndicator.getIndicatorMap().put(dimension, onlineIndicator);
        });
        return true;
    }


    public Map<Long, OnlineIndicator> loadSchoolOnlineIndicator(Collection<Long> schoolIds, Integer day, Integer dimension){

        Map<Long, OnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(schoolIds) || day == null || dimension == null){
            return resultMap;
        }

        schoolIds.forEach(p -> {
            OnlineIndicator onlineIndicator = new OnlineIndicator();
            resultMap.put(p, onlineIndicator);
        });

        List<Future<MapMessage>> futureList = new ArrayList<>();
        List<List<Long>> splitIds = splitList(schoolIds, 400);
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadSchoolDetailsData(itemList, day, AgentConstants.MODE_ONLINE, dimension)));
        }

        for(Future<MapMessage> future : futureList) {
            try {
                MapMessage msg = future.get();
                if (msg == null || !msg.isSuccess()) {
                    return resultMap;
                }

                Map<Long, Object> dataMap = (Map<Long, Object>) msg.get("dataMap");
                if (MapUtils.isEmpty(dataMap)) {
                    return resultMap;
                }

                dataMap.forEach((k, v) -> {
                    OnlineIndicator onlineIndicator = resultMap.get(k);
                    if (onlineIndicator == null) {
                        return;
                    }
                    try {
                        BeanUtilsBean2.getInstance().copyProperties(onlineIndicator, v);
                    } catch (Exception e) {
                        logger.error("online indicator error", e);
                    }
                });
            } catch (Exception e) {
                String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                logger.error(methodName + " error", e);
                sendMethodErrorEmail(methodName, schoolIds, day, dimension);
                return resultMap;
            }
        }

        return resultMap;
    }

    public GradeOnlineIndicator loadGradeOnlineIndicator(Long schoolId, Integer day){

        if(schoolId == null || day == null){
            return null;
        }

        GradeOnlineIndicator result = new GradeOnlineIndicator();
        result.setSchoolId(schoolId);
        result.setDay(day);

        Map<Integer, Future<MapMessage>> futureDataMap = new HashMap<>();
        for(Integer dimension : DIMENSION_LIST){
            Future<MapMessage> futureData = AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadGradeDetailsData(schoolId, day, AgentConstants.MODE_ONLINE, dimension)) ;
            futureDataMap.put(dimension, futureData);
        }

        if(MapUtils.isNotEmpty(futureDataMap)){
            for(Integer dimension : DIMENSION_LIST){
                try {
                    MapMessage msg = futureDataMap.get(dimension).get();
                    if(msg == null || !msg.isSuccess()){
                        continue;
                    }

                    Map<Integer, Object> dataMap = (Map<Integer, Object>) msg.get("dataMap");
                    if(MapUtils.isEmpty(dataMap)){
                        continue;
                    }

                    dataMap.forEach((k, v) -> {
                        //高一、高二、高三年级与大数据对应不一致，中间做相应转化
                        if (k == 10 || k == 11 || k == 12){
                            k ++;
                        }
                        ClazzLevel grade = ClazzLevel.parse(k);
                        if(grade == null || grade == ClazzLevel.INFANT_GRADUATED|| grade == ClazzLevel.PRIMARY_GRADUATED|| grade == ClazzLevel.MIDDLE_GRADUATED){
                            return;
                        }

                        Map<Integer, OnlineIndicator> gradDataMap = result.getIndicatorMap().get(grade);
                        if(gradDataMap == null){
                            gradDataMap = new HashMap<>();
                            result.getIndicatorMap().put(grade, gradDataMap);
                        }

                        OnlineIndicator onlineIndicator = new OnlineIndicator();
                        try{
                            BeanUtilsBean2.getInstance().copyProperties(onlineIndicator, v);
                        }catch (Exception e){
                            logger.error("online indicator error", e);
                        }
                        gradDataMap.put(dimension, onlineIndicator);
                    });

                }catch (Exception e){
                    String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                    logger.error(methodName + " error", e);
                    sendMethodErrorEmail(methodName, schoolId, day, dimension);
                }
            }
        }

        return result;
    }

    public Map<Long, ClassOnlineIndicator> loadClassOnlineIndicator(Collection<Long> ids, Integer day){
        Map<Long, ClassOnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            ClassOnlineIndicator classOnlineIndicator = new ClassOnlineIndicator();
            classOnlineIndicator.setClassId(p);
            classOnlineIndicator.setDay(day);
            resultMap.put(p, classOnlineIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadClassDetailsData(itemList, day, AgentConstants.MODE_ONLINE, dimension)));
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
                            ClassOnlineIndicator classOnlineIndicator = resultMap.get(k);
                            if (classOnlineIndicator == null) {
                                return;
                            }

                            OnlineIndicator onlineIndicator = new OnlineIndicator();
                            try {
                                BeanUtilsBean2.getInstance().copyProperties(onlineIndicator, v);
                            } catch (Exception e) {
                                logger.error("online indicator error", e);
                            }
                            classOnlineIndicator.getIndicatorMap().put(dimension, onlineIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, day, dimension);
                    }
                }
            }
        }
        return resultMap;
    }

    public Map<Long, TeacherOnlineIndicator> loadTeacherOnlineIndicator(Collection<Long> ids, Integer day){
        Map<Long, TeacherOnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            TeacherOnlineIndicator teacherOnlineIndicator = new TeacherOnlineIndicator();
            teacherOnlineIndicator.setTeacherId(p);
            teacherOnlineIndicator.setDay(day);
            resultMap.put(p, teacherOnlineIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadTeacherDetailsData(itemList, day, AgentConstants.MODE_ONLINE, dimension)));
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
                            TeacherOnlineIndicator teacherOnlineIndicator = resultMap.get(k);
                            if (teacherOnlineIndicator == null) {
                                return;
                            }

                            OnlineIndicator onlineIndicator = new OnlineIndicator();
                            try{
                                Map<String,Object> map = (Map<String, Object>)v;
                                convertStr2Date(map,"registerTime");
                                convertStr2Date(map,"authTime");
                                convertStr2Date(map,"latestHwTime");
                                BeanUtilsBean2.getInstance().copyProperties(onlineIndicator, v);
                            }catch (Exception e){
                                logger.error("online indicator error", e);
                            }
                            teacherOnlineIndicator.getIndicatorMap().put(dimension, onlineIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, day, dimension);
                    }
                }
            }
        }

        return resultMap;
    }

    private void convertStr2Date(Map<String,Object> map ,String key){
        if(map.keySet().contains(key)){
            String strTime = SafeConverter.toString(map.get(key));
            map.put(key,(StringUtils.isNotBlank(strTime) && strTime.length() > 9) ? DateUtils.stringToDate(strTime,DateUtils.FORMAT_SQL_DATE) : null);
        }
    }

    public Map<Long, GroupOnlineIndicator> loadGroupOnlineIndicator(Collection<Long> ids, Integer day){
        Map<Long, GroupOnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            GroupOnlineIndicator groupOnlineIndicator = new GroupOnlineIndicator();
            groupOnlineIndicator.setGroupId(p);
            groupOnlineIndicator.setDay(day);
            resultMap.put(p, groupOnlineIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadGroupDetailsData(itemList, day, AgentConstants.MODE_ONLINE, dimension)));
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
                            GroupOnlineIndicator groupOnlineIndicator = resultMap.get(k);
                            if (groupOnlineIndicator == null) {
                                return;
                            }

                            OnlineIndicator onlineIndicator = new OnlineIndicator();
                            try {
                                BeanUtilsBean2.getInstance().copyProperties(onlineIndicator, v);
                            } catch (Exception e) {
                                logger.error("online indicator error", e);
                            }
                            groupOnlineIndicator.getIndicatorMap().put(dimension, onlineIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, day, dimension);
                    }
                }
            }
        }

        return resultMap;
    }

    /**
     * 新增高潜学校
     * @param ids       部门IDs or 人员IDs
     * @param idType    ID类型 1：部门ID 2：人员ID
     * @param subjectCode   学科 1： 单科 2：语文 3：数学 4：英语
     * @param topN      排序数目
     * @param model     1：online 2：offline
     * @return
     */
    public List<AgentHighPotentialSchoolInfo> loadHighPotentialSchoolData(Collection<Long> ids, Integer idType, Integer subjectCode,Integer topN,Integer model,List<Integer> schoolLevelList){
        List<AgentHighPotentialSchoolInfo> highPotentialSchoolInfoList = new ArrayList<>();
        MapMessage msg = loadNewSchoolV1Service.loadHighPotentialSchoolData(ids, idType, subjectCode, topN, model,schoolLevelList);
        if(msg == null || !msg.isSuccess()){
            return Collections.emptyList();
        }
        Map<Long, Object> dataMap = (Map<Long, Object>) msg.get("dataMap");
        if(MapUtils.isEmpty(dataMap)){
            return Collections.emptyList();
        }
        dataMap.forEach((k,v) -> {
            AgentHighPotentialSchoolInfo highPotentialSchoolInfo = new AgentHighPotentialSchoolInfo();
            highPotentialSchoolInfo.setSchoolId(k);
            highPotentialSchoolInfo.setMauPotentialValue(SafeConverter.toInt(v));
            highPotentialSchoolInfoList.add(highPotentialSchoolInfo);
        });
        return highPotentialSchoolInfoList;
    }


    /**
     *  月活TOP校
     * @param ids           部门IDs or 人员IDs
     * @param idType        ID类型 1：部门ID 2：人员ID
     * @param regionCodes   地区编码
     * @param subjectCode   学科 1： 单科 2：语文 3：数学 4：英语
     * @param topN          排序数目
     * @param model         1：online 2：offline
     * @param schoolLevelList
     * @return
     */
    public List<AgentMauTopSchoolInfo> loadMauTOPSchoolData(Collection<Long> ids, Integer idType, Collection<Integer> regionCodes , Integer subjectCode, Integer topN, Integer model,List<Integer> schoolLevelList) {
        List<AgentMauTopSchoolInfo> mauTopSchoolInfoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(regionCodes)){
            return mauTopSchoolInfoList;
        }
        MapMessage msg = loadNewSchoolV1Service.loadMauTOPSchoolData(ids, idType,regionCodes, subjectCode, topN, model,schoolLevelList);
        if (msg == null || !msg.isSuccess()) {
            String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
            sendMethodErrorEmail(methodName,"接口调用失败", ids, idType,regionCodes,subjectCode,topN,model);
            return Collections.emptyList();
        }
        Map<Long, Object> dataMap = (Map<Long, Object>) msg.get("dataMap");
        if (MapUtils.isEmpty(dataMap)) {
            return Collections.emptyList();
        }
        dataMap.forEach((k,v) -> {
            AgentMauTopSchoolInfo mauTopSchoolInfo = new AgentMauTopSchoolInfo();
            mauTopSchoolInfo.setSchoolId(k);
            mauTopSchoolInfo.setLastSixMonthMaxFinHwGte3StuCount(SafeConverter.toInt(v));
            mauTopSchoolInfoList.add(mauTopSchoolInfo);
        });
        return mauTopSchoolInfoList;
    }


    private void sendMethodErrorEmail(String method, Object ... parameters){
        try {

            String cacheKey = "METHOD_ERROR_EMAIL:" + method;
            Boolean cache = agentCacheSystem.CBS.flushable.load(cacheKey);
            if(cache != null){
                return;
            }

            StringBuilder body = new StringBuilder();
            body.append("接口名称：").append(method).append("\r\n");
            body.append("参数：\r\n");
            for(int i = 0; i < parameters.length; i++){
                Object parameter = parameters[i];
                body.append("参数").append(i).append("：");
                if(parameter instanceof Collection){
                    body.append("【分批请求的话则为类别中的部分数据】 ");
                    body.append(StringUtils.join(parameter, ","));
                }else if(parameter instanceof Integer || parameter instanceof Long){
                    body.append(parameter);
                }else {
                    body.append(parameter);
                }
                body.append("\r\n");
            }

            emailServiceClient.createPlainEmail()
                    .body(body.toString())
                    .subject("接口调用失败【" + RuntimeMode.current().getStageMode() + "】")
                    .to("song.wang@17zuoye.com;dongshuang.zhao@17zuoye.com;deliang.che@17zuoye.com;xianlong.zhang@17zuoye.com;guoguang.zhang@17zuoye.com;youwen.zhang@17zuoye.com;jianbo.liu@17zuoye.com")
                    .send();

            agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), Boolean.TRUE);
        }catch (Exception ignored){

        }
    }

    public void sendMethodErrorEmailPub(String method, Object ... parameters){
        sendMethodErrorEmail(method,parameters);
    }


    public Map<Long, SchoolOfflineIndicator> loadSchoolOfflineIndicator(Collection<Long> ids, Integer day){
        Map<Long, SchoolOfflineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            SchoolOfflineIndicator schoolOfflineIndicator = new SchoolOfflineIndicator();
            schoolOfflineIndicator.setSchoolId(p);
            schoolOfflineIndicator.setDay(day);
            resultMap.put(p, schoolOfflineIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadSchoolDetailsData(itemList, day, AgentConstants.MODE_OFFLINE, dimension)));
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
                            SchoolOfflineIndicator schoolOfflineIndicator = resultMap.get(k);
                            if (schoolOfflineIndicator == null) {
                                return;
                            }

                            OfflineIndicator offlineIndicator = new OfflineIndicator();
                            try {
                                BeanUtilsBean2.getInstance().copyProperties(offlineIndicator, v);
                            } catch (Exception e) {
                                logger.error("offline indicator error", e);
                            }
                            schoolOfflineIndicator.getIndicatorMap().put(dimension, offlineIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, day, dimension);
                    }
                }
            }
        }

        return resultMap;
    }

    public GradeOfflineIndicator loadGradeOfflineIndicator(Long schoolId, Integer day){

        if(schoolId == null || day == null){
            return null;
        }

        GradeOfflineIndicator result = new GradeOfflineIndicator();
        result.setSchoolId(schoolId);
        result.setDay(day);

        Map<Integer, Future<MapMessage>> futureDataMap = new HashMap<>();
        for(Integer dimension : DIMENSION_LIST){
            Future<MapMessage> futureData = AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadGradeDetailsData(schoolId, day, AgentConstants.MODE_OFFLINE, dimension)) ;
            futureDataMap.put(dimension, futureData);
        }

        if(MapUtils.isNotEmpty(futureDataMap)){
            for(Integer dimension : DIMENSION_LIST){
                try {
                    MapMessage msg = futureDataMap.get(dimension).get();
                    if(msg == null || !msg.isSuccess()){
                        continue;
                    }

                    Map<Integer, Object> dataMap = (Map<Integer, Object>) msg.get("dataMap");
                    if(MapUtils.isEmpty(dataMap)){
                        continue;
                    }

                    dataMap.forEach((k, v) -> {
                        //高一、高二、高三年级与大数据对应不一致，中间做相应转化
                        if (k == 10 || k == 11 || k == 12){
                            k ++;
                        }
                        ClazzLevel grade = ClazzLevel.parse(k);
                        if(grade == null || grade == ClazzLevel.INFANT_GRADUATED|| grade == ClazzLevel.PRIMARY_GRADUATED|| grade == ClazzLevel.MIDDLE_GRADUATED){
                            return;
                        }

                        Map<Integer, OfflineIndicator> gradDataMap = result.getIndicatorMap().get(grade);
                        if(gradDataMap == null){
                            gradDataMap = new HashMap<>();
                            result.getIndicatorMap().put(grade, gradDataMap);
                        }

                        OfflineIndicator offlineIndicator = new OfflineIndicator();
                        try{
                            BeanUtilsBean2.getInstance().copyProperties(offlineIndicator, v);
                        }catch (Exception e){
                            logger.error("offline indicator error", e);
                        }
                        gradDataMap.put(dimension, offlineIndicator);
                    });

                }catch (Exception e){
                    String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                    logger.error(methodName + " error", e);
                    sendMethodErrorEmail(methodName, schoolId, day, dimension);
                }
            }
        }

        return result;
    }

    public Map<Long, ClassOfflineIndicator> loadClassOfflineIndicator(Collection<Long> ids, Integer day){
        Map<Long, ClassOfflineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            ClassOfflineIndicator classOfflineIndicator = new ClassOfflineIndicator();
            classOfflineIndicator.setClassId(p);
            classOfflineIndicator.setDay(day);
            resultMap.put(p, classOfflineIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadClassDetailsData(itemList, day, AgentConstants.MODE_OFFLINE, dimension)));
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
                            ClassOfflineIndicator classOfflineIndicator = resultMap.get(k);
                            if (classOfflineIndicator == null) {
                                return;
                            }

                            OfflineIndicator offlineIndicator = new OfflineIndicator();
                            try {
                                BeanUtilsBean2.getInstance().copyProperties(offlineIndicator, v);
                            } catch (Exception e) {
                                logger.error("offline indicator error", e);
                            }
                            classOfflineIndicator.getIndicatorMap().put(dimension, offlineIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, day, dimension);
                    }
                }
            }
        }

        return resultMap;
    }

    public Map<Long, TeacherOfflineIndicator> loadTeacherOfflineIndicator(Collection<Long> ids, Integer day){
        Map<Long, TeacherOfflineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            TeacherOfflineIndicator teacherOfflineIndicator = new TeacherOfflineIndicator();
            teacherOfflineIndicator.setTeacherId(p);
            teacherOfflineIndicator.setDay(day);
            resultMap.put(p, teacherOfflineIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadTeacherDetailsData(itemList, day, AgentConstants.MODE_OFFLINE, dimension)));
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
                            TeacherOfflineIndicator teacherOfflineIndicator = resultMap.get(k);
                            if (teacherOfflineIndicator == null) {
                                return;
                            }

                            OfflineIndicator offlineIndicator = new OfflineIndicator();
                            try {
                                BeanUtilsBean2.getInstance().copyProperties(offlineIndicator, v);
                            } catch (Exception e) {
                                logger.error("offline indicator error", e);
                            }
                            teacherOfflineIndicator.getIndicatorMap().put(dimension, offlineIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, day, dimension);
                    }
                }
            }
        }

        return resultMap;
    }

    public Map<Long, GroupOfflineIndicator> loadGroupOfflineIndicator(Collection<Long> ids, Integer day){
        Map<Long, GroupOfflineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            GroupOfflineIndicator groupOfflineIndicator = new GroupOfflineIndicator();
            groupOfflineIndicator.setGroupId(p);
            groupOfflineIndicator.setDay(day);
            resultMap.put(p, groupOfflineIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadGroupDetailsData(itemList, day, AgentConstants.MODE_OFFLINE, dimension)));
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
                            GroupOfflineIndicator groupOfflineIndicator = resultMap.get(k);
                            if (groupOfflineIndicator == null) {
                                return;
                            }

                            OfflineIndicator offlineIndicator = new OfflineIndicator();
                            try {
                                BeanUtilsBean2.getInstance().copyProperties(offlineIndicator, v);
                            } catch (Exception e) {
                                logger.error("offline indicator error", e);
                            }
                            groupOfflineIndicator.getIndicatorMap().put(dimension, offlineIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, day, dimension);
                    }
                }
            }
        }

        return resultMap;
    }


    public Map<Long, SumOnlineIndicator> loadSumOnlineIndicator(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || CollectionUtils.isEmpty(schoolLevels) || dataType == null){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);

        Map<Long, AgentGroup> groupMap = new HashMap<>();
        Map<Long, AgentUser> userMap = new HashMap<>();
        if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
            Map<Long, AgentGroup> tmpGroupMap = baseOrgService.getGroupByIds(ids).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
            if(MapUtils.isNotEmpty(tmpGroupMap)){
                groupMap.putAll(tmpGroupMap);
            }
        }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
            Map<Long, AgentUser> tmpUserMap = baseOrgService.getUsers(ids).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));
            if(MapUtils.isNotEmpty(tmpUserMap)){
                userMap.putAll(tmpUserMap);
            }
        }

        ids.forEach(p -> {
            SumOnlineIndicator sumOnlineIndicator = new SumOnlineIndicator();
            sumOnlineIndicator.setId(p);
            sumOnlineIndicator.setDataType(dataType);
            sumOnlineIndicator.setDay(day);
            sumOnlineIndicator.setSchoolLevel(schoolLevel);
            if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
                AgentGroup group = groupMap.get(p);
                sumOnlineIndicator.setName(group != null && StringUtils.isNotBlank(group.getGroupName()) ? group.getGroupName() : "");
            }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
                AgentUser user = userMap.get(p);
                sumOnlineIndicator.setName(user != null && StringUtils.isNotBlank(user.getRealName()) ? user.getRealName() : "");
            }
            resultMap.put(p, sumOnlineIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadOrgSumData(itemList, dataType, schoolLevels, Collections.singleton(day), AgentConstants.MODE_ONLINE, dimension)));
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
                            SumOnlineIndicator sumOnlineIndicator = resultMap.get(k);
                            if (sumOnlineIndicator == null) {
                                return;
                            }

                            OnlineIndicator onlineIndicator = new OnlineIndicator();
                            try {
                                Map<String, Object> daysData = (Map<String, Object>) v;
                                Object dayData = daysData.get(ConversionUtils.toString(day));
                                if (dayData == null) {
                                    return;
                                }
                                BeanUtilsBean2.getInstance().copyProperties(onlineIndicator, dayData);
                            } catch (Exception e) {
                                logger.error("online indicator error", e);
                                return;
                            }
                            sumOnlineIndicator.getIndicatorMap().put(dimension, onlineIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, dataType, schoolLevels, day, dimension);
                    }
                }
            }
        }
        return resultMap;
    }


    public Map<Long, SumOfflineIndicator> loadSumOfflineIndicator(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOfflineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || CollectionUtils.isEmpty(schoolLevels) || dataType == null){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);

        Map<Long, AgentGroup> groupMap = new HashMap<>();
        Map<Long, AgentUser> userMap = new HashMap<>();
        if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
            Map<Long, AgentGroup> tmpGroupMap = baseOrgService.getGroupByIds(ids).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
            if(MapUtils.isNotEmpty(tmpGroupMap)){
                groupMap.putAll(tmpGroupMap);
            }
        }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
            Map<Long, AgentUser> tmpUserMap = baseOrgService.getUsers(ids).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));
            if(MapUtils.isNotEmpty(tmpUserMap)){
                userMap.putAll(tmpUserMap);
            }
        }

        ids.forEach(p -> {
            SumOfflineIndicator sumOfflineIndicator = new SumOfflineIndicator();
            sumOfflineIndicator.setId(p);
            sumOfflineIndicator.setDataType(dataType);
            sumOfflineIndicator.setDay(day);
            sumOfflineIndicator.setSchoolLevel(schoolLevel);
            if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
                AgentGroup group = groupMap.get(p);
                sumOfflineIndicator.setName(group != null && StringUtils.isNotBlank(group.getGroupName()) ? group.getGroupName() : "");
            }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
                AgentUser user = userMap.get(p);
                sumOfflineIndicator.setName(user != null && StringUtils.isNotBlank(user.getRealName()) ? user.getRealName() : "");
            }
            resultMap.put(p, sumOfflineIndicator);
        });

        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadOrgSumData(itemList, dataType, schoolLevels, Collections.singleton(day), AgentConstants.MODE_OFFLINE, dimension)));
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
                            SumOfflineIndicator sumOfflineIndicator = resultMap.get(k);
                            if (sumOfflineIndicator == null) {
                                return;
                            }

                            OfflineIndicator offlineIndicator = new OfflineIndicator();
                            try {
                                BeanUtilsBean2.getInstance().copyProperties(offlineIndicator, v);
                            } catch (Exception e) {
                                logger.error("offline indicator error", e);
                                return;
                            }
                            sumOfflineIndicator.getIndicatorMap().put(dimension, offlineIndicator);
                        });

                    } catch (Exception e) {
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                        sendMethodErrorEmail(methodName, ids, dataType, schoolLevel, day, dimension);
                    }
                }
            }
        }
        return resultMap;
    }

    // 获取各个学校的汇总数据
    public SumOnlineIndicator loadSchoolSumOnlineIndicator(Collection<Long> ids, Integer day){
        if(CollectionUtils.isEmpty(ids) || day == null){
            return null;
        }

        SumOnlineIndicator sumOnlineIndicator = new SumOnlineIndicator();
        sumOnlineIndicator.setDay(day);

        Map<Integer, Future<MapMessage>> futureDataMap = new HashMap<>();
        for(Integer dimension : DIMENSION_LIST){
            Future<MapMessage> futureData = AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadSchoolSumData(ids, day, AgentConstants.MODE_ONLINE, dimension)) ;
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

                    OnlineIndicator onlineIndicator = new OnlineIndicator();
                    try{
                        BeanUtilsBean2.getInstance().copyProperties(onlineIndicator, dataMap);
                    }catch (Exception e){
                        logger.error("online indicator error", e);
                        continue;
                    }
                    sumOnlineIndicator.getIndicatorMap().put(dimension, onlineIndicator);
                }catch (Exception e){
                    String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                    logger.error(methodName + " error", e);
                    sendMethodErrorEmail(methodName, ids, day, dimension);
                }
            }
        }
        return sumOnlineIndicator;
    }

    // 获取各个学校的汇总数据
    public SumOfflineIndicator loadSchoolSumOfflineIndicator(Collection<Long> ids, Integer day){
        if(CollectionUtils.isEmpty(ids) || day == null){
            return null;
        }

        SumOfflineIndicator sumOfflineIndicator = new SumOfflineIndicator();
        sumOfflineIndicator.setDay(day);

        Map<Integer, Future<MapMessage>> futureDataMap = new HashMap<>();
        for(Integer dimension : DIMENSION_LIST){
            Future<MapMessage> futureData = AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadSchoolSumData(ids, day, AgentConstants.MODE_OFFLINE, dimension)) ;
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

                    OfflineIndicator offlineIndicator = new OfflineIndicator();
                    try{
                        BeanUtilsBean2.getInstance().copyProperties(offlineIndicator, dataMap);
                    }catch (Exception e){
                        logger.error("offline indicator error", e);
                        continue;
                    }
                    sumOfflineIndicator.getIndicatorMap().put(dimension, offlineIndicator);
                }catch (Exception e){
                    String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                    logger.error(methodName + " error", e);
                    sendMethodErrorEmail(methodName, ids, day, dimension);
                }
            }
        }
        return sumOfflineIndicator;
    }


    // 获取具体维度的online指标
    public Map<Long, OnlineIndicator> loadOnlineIndicator(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels, Integer dimension){
        Map<Long, OnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || CollectionUtils.isEmpty(schoolLevels) || dataType == null || !DIMENSION_LIST.contains(dimension)){
            return resultMap;
        }

        ids.forEach(p -> {
            OnlineIndicator indicator = new OnlineIndicator();
            resultMap.put(p, indicator);
        });

        List<Future<MapMessage>> futureList = new ArrayList<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadOrgSumData(itemList, dataType, schoolLevels, Collections.singleton(day), AgentConstants.MODE_ONLINE, dimension)));
        }

        for(Future<MapMessage> future : futureList) {
            try {
                MapMessage msg = future.get();
                if (msg == null || !msg.isSuccess()) {
                    return resultMap;
                }

                Map<Long, Object> dataMap = (Map<Long, Object>) msg.get("dataMap");
                if (MapUtils.isEmpty(dataMap)) {
                    return resultMap;
                }
                dataMap.forEach((k, v) -> {
                    OnlineIndicator indicator = resultMap.get(k);
                    if (indicator == null) {
                        return;
                    }
                    try {
                        Map<String, Object> daysData = (Map<String, Object>) v;
                        Object dayData = daysData.get(ConversionUtils.toString(day));
                        if (dayData != null) {
                            BeanUtilsBean2.getInstance().copyProperties(indicator, dayData);
                        }
                    } catch (Exception e) {
                        logger.error("online indicator error", e);
                    }
                });
            } catch (Exception e) {
                String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                logger.error(methodName + " error", e);
                sendMethodErrorEmail(methodName, ids, dataType, schoolLevels, day, dimension);
                return resultMap;
            }
        }
        return resultMap;
    }

    // 获取具体维度的offline指标
    public Map<Long, OfflineIndicator> loadOfflineIndicator(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels, Integer dimension){
        Map<Long, OfflineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || CollectionUtils.isEmpty(schoolLevels) || dataType == null || !DIMENSION_LIST.contains(dimension)){
            return resultMap;
        }

        ids.forEach(p -> {
            OfflineIndicator indicator = new OfflineIndicator();
            resultMap.put(p, indicator);
        });

        List<Future<MapMessage>> futureList = new ArrayList<>();
        List<List<Long>> splitIds = splitList(ids, 100);
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadNewSchoolService.loadOrgSumData(itemList, dataType, schoolLevels, Collections.singleton(day), AgentConstants.MODE_OFFLINE, dimension)));
        }

        for(Future<MapMessage> future : futureList) {
            try {
                MapMessage msg = future.get();
                if (msg == null || !msg.isSuccess()) {
                    return resultMap;
                }

                Map<Long, Object> dataMap = (Map<Long, Object>) msg.get("dataMap");
                if (MapUtils.isEmpty(dataMap)) {
                    return resultMap;
                }
                dataMap.forEach((k, v) -> {
                    OfflineIndicator indicator = resultMap.get(k);
                    if (indicator == null) {
                        return;
                    }
                    try {
                        BeanUtilsBean2.getInstance().copyProperties(indicator, v);
                    } catch (Exception e) {
                        logger.error("offline indicator error", e);
                    }
                });
            } catch (Exception e) {
                String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                logger.error(methodName + " error", e);
                sendMethodErrorEmail(methodName, ids, dataType, schoolLevels, day, dimension);
                return resultMap;
            }
        }

        return resultMap;
    }


}
