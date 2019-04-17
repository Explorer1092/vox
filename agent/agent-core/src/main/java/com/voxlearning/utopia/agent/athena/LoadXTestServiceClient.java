/**
 * Author:   xianlong.zhang
 * Date:     2018/9/21 14:46
 * Description: X测相关接口
 * History:
 */
package com.voxlearning.utopia.agent.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtilsBean2;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.athena.api.LoadXTestService;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicator;
import com.voxlearning.utopia.agent.bean.xtest.MapXTestData;
import com.voxlearning.utopia.agent.bean.xtest.XTestData;
import com.voxlearning.utopia.agent.bean.xtest.school.SchoolXTestData;
import com.voxlearning.utopia.agent.bean.xtest.sum.SumXTestData;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
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

@Named
public class LoadXTestServiceClient {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private BaseOrgService baseOrgService;
    @ImportService(interfaceClass = LoadXTestService.class)
    private LoadXTestService loadXTestService;
    private static final List<Integer> DIMENSION_LIST = Arrays.asList(1, 2);//0:x测，2:统考，1:活动
    private static final List<String> AREA_LIST = Arrays.asList(AgentConstants.XTEST_DATA_ALL,AgentConstants.XTEST_DATA_CITY,AgentConstants.XTEST_DATA_COUNTY,AgentConstants.XTEST_DATA_SCHOOL);

    //根据部门或人员查看汇总后的x测相关指标
    public MapMessage loadXOrgSumData(List<Long> ids, Integer idType, List<Integer> schoolLevel, Integer testType, Integer sumType, Integer date){
        MapMessage msg =  loadXTestService.loadXOrgSumData(ids,idType,schoolLevel,testType,sumType,date);
        return msg;
    }

    public Map<Long, SumXTestData> loadUserSumData(List<Long> userIds, Integer day, List<Integer> schoolLevels,Integer sumType){
        return loadSumDataByIds(userIds, AgentConstants.INDICATOR_TYPE_USER, schoolLevels,sumType,day);
    }

    public Map<Long, SumXTestData> loadGroupSumData(List<Long> groupIds, Integer day, List<Integer> schoolLevels,Integer sumType){
        return loadSumDataByIds(groupIds, AgentConstants.INDICATOR_TYPE_GROUP, schoolLevels,sumType,day);
    }

    /**
     *
     * @param ids
     * @param idType  id类型	1:部门，2:专员
     * @param schoolLevels  1:小学，2:中学
     * @param sumType  1:日维度，2:月维度
     * @param day  日期
     * @return
     */
    private Map<Long, SumXTestData> loadSumDataByIds(List<Long> ids, Integer idType, List<Integer> schoolLevels, Integer sumType, Integer day){
      Map<Long, SumXTestData> resultMap = new HashMap<>();
      if(CollectionUtils.isEmpty(ids) || day == null || idType == null || CollectionUtils.isEmpty(schoolLevels) || sumType == null || day == null){
            return resultMap;
        }
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
            SumXTestData sumXTestData = new SumXTestData();
            sumXTestData.setDataType(idType);
            sumXTestData.setDay(day);
//            sumXTestData.setSchoolLevel(schoolLevel);
            if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
                AgentGroup group = groupMap.get(p);
                sumXTestData.setName(group != null && StringUtils.isNotBlank(group.getGroupName()) ? group.getGroupName() : "");
            }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
                AgentUser user = userMap.get(p);
                sumXTestData.setName(user != null && StringUtils.isNotBlank(user.getRealName()) ? user.getRealName() : "");
            }
            resultMap.put(p, sumXTestData);
        });
        List<List<Long>> splitIds = new ArrayList<>();
        if(ids.size() > 100){
            int count = ids.size() / 100 + 1;
            splitIds.addAll(CollectionUtils.splitList(new ArrayList<>(ids), count));
        }else {
            splitIds.add(new ArrayList<>((ids)));
        }
        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadXTestService.loadXOrgSumData(itemList,idType,schoolLevels,dimension,sumType,day)));
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
                            SumXTestData sumXTestData = resultMap.get(k);
                            if(sumXTestData == null){
                                return;
                            }
                            MapXTestData mapXTestData = new MapXTestData();
                            try{
                                Map<String,Object> areaDataMap = (Map<String,Object>)v;

                                for (String area : AREA_LIST){
                                    Object object = areaDataMap.get(area);
                                    if(object != null){
                                        XTestData xTestData = new XTestData();
                                        BeanUtilsBean2.getInstance().copyProperties(xTestData, object);
                                        mapXTestData.getXTestDataMap().put(area,xTestData);
                                    }
                                }

                            }catch (Exception e){
                                logger.error("online indicator error", e);
                            }
                            sumXTestData.getXTestMap().put(dimension, mapXTestData);

                        });
                    }catch (Exception e){
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                    }
                }
            }
        }
        return resultMap;
    }
    //学校明细接口：根据学校id查看x测相关指标
    public Map<Long, SchoolXTestData>  loadXSchoolDetailDataByIds(List<Long> schoolIds, Integer sumType, Integer date){
        Map<Long, SchoolXTestData> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(schoolIds) || sumType == null || date == null){
            return resultMap;
        }

        schoolIds.forEach(p -> {
            SchoolXTestData schoolXTestData = new SchoolXTestData();
            schoolXTestData.setDay(date);
            schoolXTestData.setId(p);
            resultMap.put(p, schoolXTestData);
        });
        List<List<Long>> splitIds = new ArrayList<>();
        if(schoolIds.size() > 100){
            int count = schoolIds.size() / 100 + 1;
            splitIds.addAll(CollectionUtils.splitList(new ArrayList<>(schoolIds), count));
        }else {
            splitIds.add(new ArrayList<>((schoolIds)));
        }
        Map<Integer, List<Future<MapMessage>>> futureDataMap = new HashMap<>();
        for(Integer dimension : DIMENSION_LIST){
            List<Future<MapMessage>> futureList = new ArrayList<>();
            for(List<Long> itemList : splitIds){
                futureList.add(AlpsThreadPool.getInstance().submit(() -> loadXTestService.loadXSchoolDetailData(itemList,dimension,sumType,date)));
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
                            SchoolXTestData schoolXTestData = resultMap.get(k);
                            if(schoolXTestData == null){
                                return;
                            }
                            MapXTestData mapXTestData = new MapXTestData();
                            try{
                                Map<String,Object> areaDataMap = (Map<String,Object>)v;

                                for (String area : AREA_LIST){
                                    Object object = areaDataMap.get(area);
                                    if(object != null){
                                        XTestData xTestData = new XTestData();
                                        BeanUtilsBean2.getInstance().copyProperties(xTestData, object);
                                        mapXTestData.getXTestDataMap().put(area,xTestData);
                                    }
                                }

                            }catch (Exception e){
                                logger.error("online indicator error", e);
                            }
                            schoolXTestData.getXTestMap().put(dimension, mapXTestData);

                        });
                    }catch (Exception e){
                        String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                        logger.error(methodName + " error", e);
                    }
                }
            }
        }
        return resultMap;
    }
    //学校明细接口：根据学校id查看x测相关指标
    public MapMessage loadXSchoolDetailData(List<Long> schoolIds, Integer testType, Integer sumType, Integer date){
        MapMessage msg =  loadXTestService.loadXSchoolDetailData(schoolIds,testType,sumType,date);
        return msg;
    }

    //年级明细接口：根据学校id查看该校各年级x测相关指标
    public MapMessage loadXGradeDetailData(Long schoolId, Integer testType, Integer sumType, Integer date){
        MapMessage msg =  loadXTestService.loadXGradeDetailData(schoolId,testType,sumType,date);
        return msg;
    }

    //班级明细接口：根据班级id查看x测相关指标
    public MapMessage loadXClassDetailData(List<Long> classIds, Integer testType, Integer sumType, Integer date){
        MapMessage msg =  loadXTestService.loadXClassDetailData(classIds,testType,sumType,date);
        return msg;
    }

    //班组明细接口：根据班组id查看x测相关指标
    public MapMessage loadXGroupDetailData(List<Long> groupIds, Integer testType, Integer sumType, Integer date){
        MapMessage msg =  loadXTestService.loadXGroupDetailData(groupIds,testType,sumType,date);
        return msg;
    }

    //老师明细接口：根据老师id查看x测相关指标
    public MapMessage loadXTeacherDetailData(List<Long> teacherIds, Integer testType, Integer sumType, Integer date){
        MapMessage msg =  loadXTestService.loadXTeacherDetailData(teacherIds,testType,sumType,date);
        return msg;
    }

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


    //借用下 查大数据那边返回数据变化问题   用完就删
    private void sendMethodErrorEmail(String method, Object ... parameters){
        try {

//            String cacheKey = "METHOD_ERROR_EMAIL:" + method;
//            Boolean cache = agentCacheSystem.CBS.flushable.load(cacheKey);
//            if(cache != null){
//                return;
//            }

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

//            agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), Boolean.TRUE);
        }catch (Exception ignored){

        }
    }
}
