/**
 * Author:   xianlong.zhang
 * Date:     2018/9/21 15:06
 * Description: x测相关服务
 * History:
 */
package com.voxlearning.utopia.agent.service.exam;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.athena.LoadXTestServiceClient;
import com.voxlearning.utopia.agent.bean.xtest.MapXTestData;
import com.voxlearning.utopia.agent.bean.xtest.XTestData;
import com.voxlearning.utopia.agent.bean.xtest.school.SchoolXTestData;
import com.voxlearning.utopia.agent.bean.xtest.sum.SumXTestData;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.indicator.IndicatorService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.support.AgentSchoolLevelSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class XTestService  extends AbstractAgentService {
    @Inject
    private PerformanceService performanceService;
    @Inject
    private LoadXTestServiceClient loadXTestServiceClient;

    @Inject
    private BaseOrgService baseOrgService;

    @Inject
    private IndicatorService indicatorService;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;

    //汇总
    public MapMessage loadXOrgSumData(List<Long> ids, Integer idType, List<Integer> schoolLevel, Integer testType, Integer sumType, Integer date){
//        Integer day = performanceService.lastSuccessDataDay();
//        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(getCurrentUserId());
       MapMessage msg = loadXTestServiceClient.loadXOrgSumData(ids, idType,schoolLevel,testType,sumType,date);
       return msg;
    }
    //汇总专员
    public MapMessage loadXOrgSumDataDev(){
        Integer day = performanceService.lastSuccessDataDay();
//        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(getCurrentUserId());
        MapMessage msg = loadXTestServiceClient.loadXOrgSumData(Arrays.asList(1761l,2576l), 2,Arrays.asList(1),1,1,20180916);
        return msg;
    }


    //学校
    public MapMessage loadXSchoolDetailData(List<Long> schoolIds, Integer testType, Integer sumType, Integer date){
//        Integer day = performanceService.lastSuccessDataDay();
//        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(getCurrentUserId());
        MapMessage msg = loadXTestServiceClient.loadXSchoolDetailData(schoolIds, testType,sumType,date);
        return msg;
    }

    //年级
    public MapMessage loadXGradeDetailData(){
        Integer day = performanceService.lastSuccessDataDay();
//        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(getCurrentUserId());
        MapMessage msg = loadXTestServiceClient.loadXGradeDetailData(114041l, 1,1,20180916);
        return msg;
    }

    //班级
    public MapMessage loadXClassDetailData(){
        Integer day = performanceService.lastSuccessDataDay();
//        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(getCurrentUserId());
        MapMessage msg = loadXTestServiceClient.loadXClassDetailData(Arrays.asList(31976955l), 1,1,20180916);
        return msg;
    }

    //老师
    public MapMessage loadXTeacherDetailData(){
        Integer day = performanceService.lastSuccessDataDay();
//        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(getCurrentUserId());
        MapMessage msg = loadXTestServiceClient.loadXTeacherDetailData(Arrays.asList(13918465l), 1,1,20180916);
        return msg;
    }

    //班组
    public MapMessage loadXGroupDetailData(){
        Integer day = performanceService.lastSuccessDataDay();
//        AgentRoleType roleType = baseOrgService.getUserRole(getCurrentUserId());
        List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(getCurrentUserId());
        MapMessage msg = loadXTestServiceClient.loadXGroupDetailData(Arrays.asList(13918977l), 1,1,20180916);
        return msg;
    }

    public SumXTestData loadUserSumData(Long userId ,Integer day, List<Integer> schoolLevels,Integer sumType){
        Map<Long,SumXTestData> resultMap = loadXTestServiceClient.loadUserSumData(Arrays.asList(userId),day,schoolLevels,sumType);
        return resultMap.get(userId);

    }

    public SumXTestData loadGroupSumData(Long groupId ,Integer day, List<Integer> schoolLevels,Integer sumType){
        Map<Long,SumXTestData> resultMap = loadXTestServiceClient.loadGroupSumData(Arrays.asList(groupId),day,schoolLevels,sumType);
        return resultMap.get(groupId);
    }

    public Map<Long,SumXTestData> loadUserListSumData(List<Long> userIds ,Integer day, List<Integer> schoolLevels,Integer sumType){
        return loadXTestServiceClient.loadUserSumData(userIds,day,schoolLevels,sumType);

    }

    public Map<Long,SumXTestData> loadGroupListSumData(List<Long> groupIds ,Integer day, List<Integer> schoolLevels,Integer sumType){
        return loadXTestServiceClient.loadGroupSumData(groupIds,day,schoolLevels,sumType);
    }

    public MapMessage getXTestFirstPageData(Integer idType,Long id,Integer schoolLevel , Integer dataType){
        MapMessage mapMessage = MapMessage.successMessage();
        Integer day = performanceService.lastSuccessDataDay();
        mapMessage.put("day", DateUtils.dateToString(performanceService.lastSuccessDataDate(), "yyyy-MM-dd"));
        mapMessage.put("idType",idType);
        mapMessage.put("id",id);
        //取统考数据
        SumXTestData sumXTestData = null;
        if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
            sumXTestData = loadGroupSumData(id,day, Arrays.asList(schoolLevel),dataType);
        }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
            sumXTestData = loadUserSumData(id, day, Arrays.asList(schoolLevel),dataType);
        }
        if(sumXTestData != null){
            Map<String, Object> examMap = new HashMap<>();
            Map<String, Object> activityMap = new HashMap<>();
            MapXTestData examData = sumXTestData.fetchExameData();
            if(examData != null){
                XTestData allData = examData.getXTestDataMap().get(AgentConstants.XTEST_DATA_ALL);
                XTestData cityData = examData.getXTestDataMap().get(AgentConstants.XTEST_DATA_CITY);
                XTestData countyData = examData.getXTestDataMap().get(AgentConstants.XTEST_DATA_COUNTY);
                XTestData schoolData = examData.getXTestDataMap().get(AgentConstants.XTEST_DATA_SCHOOL);
                Map<String,Object> allDataMap = new HashMap<>();//汇总数据map
                //总场次
                putMapWithDefaultValue(allDataMap,"xtestNumEng",allData == null ? 0 : allData.getXtestNumEng());
                putMapWithDefaultValue(allDataMap,"xtestNumMath",allData == null ? 0 : allData.getXtestNumMath());
                putMapWithDefaultValue(allDataMap,"xtestNumChn",allData == null ? 0 : allData.getXtestNumChn());

                //参与学生数
                putMapWithDefaultValue(allDataMap,"settleStuNumEng",allData == null ? 0 : allData.getSettleStuNumEng());
                putMapWithDefaultValue(allDataMap,"settleStuNumMath",allData == null ? 0 : allData.getSettleStuNumMath());
                putMapWithDefaultValue(allDataMap,"settleStuNumChn",allData == null ? 0 : allData.getSettleStuNumChn());

                //新学生
                putMapWithDefaultValue(allDataMap,"newSettlementNumEng",allData == null ? 0 : allData.getNewSettlementNumEng());
                putMapWithDefaultValue(allDataMap,"newSettlementNumMath",allData == null ? 0 : allData.getNewSettlementNumMath());
                putMapWithDefaultValue(allDataMap,"newSettlementNumChn",allData == null ? 0 : allData.getNewSettlementNumChn());

                //沉默学生
                putMapWithDefaultValue(allDataMap,"silenceSettlementNumEng",allData == null ? 0 : allData.getSilenceSettlementNumEng());
                putMapWithDefaultValue(allDataMap,"silenceSettlementNumMath",allData == null ? 0 : allData.getSilenceSettlementNumMath());
                putMapWithDefaultValue(allDataMap,"silenceSettlementNumChn",allData == null ? 0 : allData.getSilenceSettlementNumChn());
                examMap.put("all",allDataMap);

                Map<String,Object> cityDataMap = new HashMap<>();//市级数据map
                //市级参与学生数
                putMapWithDefaultValue(cityDataMap,"xtestNumEng",cityData == null ? 0 : cityData.getXtestNumEng());
                putMapWithDefaultValue(cityDataMap,"xtestNumMath",cityData == null ? 0 : cityData.getXtestNumMath());
                putMapWithDefaultValue(cityDataMap,"xtestNumChn",cityData == null ? 0 : cityData.getXtestNumChn());
                examMap.put("city",cityDataMap);

                Map<String,Object> countyDataMap = new HashMap<>();//市级数据map
                //区级与学生数
                putMapWithDefaultValue(countyDataMap,"xtestNumEng",countyData == null ? 0 : countyData.getXtestNumEng());
                putMapWithDefaultValue(countyDataMap,"xtestNumMath",countyData == null ? 0 : countyData.getXtestNumMath());
                putMapWithDefaultValue(countyDataMap,"xtestNumChn",countyData == null ? 0 : countyData.getXtestNumChn());
                examMap.put("county",countyDataMap);

                Map<String,Object> schoolDataMap = new HashMap<>();//校级级数据map
                //校级参与学生数
                putMapWithDefaultValue(schoolDataMap,"xtestNumEng",schoolData == null ? 0 : schoolData.getXtestNumEng());
                putMapWithDefaultValue(schoolDataMap,"xtestNumMath",schoolData == null ? 0 : schoolData.getXtestNumMath());
                putMapWithDefaultValue(schoolDataMap,"xtestNumChn",schoolData == null ? 0 : schoolData.getXtestNumChn());
                examMap.put("school",schoolDataMap);
            }
            mapMessage.put("exam",examMap);
            MapXTestData activityData = sumXTestData.fetchActivityData();
            if(activityData != null){
                XTestData allData = activityData.getXTestDataMap().get(AgentConstants.XTEST_DATA_ALL);
                Map<String,Object> allDataMap = new HashMap<>();//汇总数据map
                //总场次
                putMapWithDefaultValue(allDataMap,"xtestNumEng",allData == null ? 0 : allData.getXtestNumEng());
                putMapWithDefaultValue(allDataMap,"xtestNumMath",allData == null ? 0 : allData.getXtestNumMath());
                putMapWithDefaultValue(allDataMap,"xtestNumChn",allData == null ? 0 : allData.getXtestNumChn());

                //参与学生数
                putMapWithDefaultValue(allDataMap,"settleStuNumEng",allData == null ? 0 : allData.getSettleStuNumEng());
                putMapWithDefaultValue(allDataMap,"settleStuNumMath",allData == null ? 0 : allData.getSettleStuNumMath());
                putMapWithDefaultValue(allDataMap,"settleStuNumChn",allData == null ? 0 : allData.getSettleStuNumChn());

                //新学生
                putMapWithDefaultValue(allDataMap,"newSettlementNumEng",allData == null ? 0 : allData.getNewSettlementNumEng());
                putMapWithDefaultValue(allDataMap,"newSettlementNumMath",allData == null ? 0 : allData.getNewSettlementNumMath());
                putMapWithDefaultValue(allDataMap,"newSettlementNumChn",allData == null ? 0 : allData.getNewSettlementNumChn());

                //沉默学生
                putMapWithDefaultValue(allDataMap,"silenceSettlementNumEng",allData == null ? 0 : allData.getSilenceSettlementNumEng());
                putMapWithDefaultValue(allDataMap,"silenceSettlementNumMath",allData == null ? 0 : allData.getSilenceSettlementNumMath());
                putMapWithDefaultValue(allDataMap,"silenceSettlementNumChn",allData == null ? 0 : allData.getSilenceSettlementNumChn());
                activityMap.put("all",allDataMap);
            }
            mapMessage.put("activity",activityMap);
        }
        return mapMessage;
    }

    private void putMapWithDefaultValue(Map<String,Object> map,String key ,Object value){
        if(Objects.isNull(value)){
            map.put(key,0);
        }
        map.put(key,value);
    }

    // 部门下专员数据
    public List<Map<String, Object>> generateGroupUserIndicatorViewList(Long groupId, Integer day, Integer schoolLevelFlag, int indicator, int subject, int dataType){
        List<Map<String, Object>> resultList = new ArrayList<>();

        List<Long> userList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());

         resultList.addAll(generateUserIndicatorViewList(userList, day, schoolLevelFlag,indicator, subject, dataType));

        return resultList;
    }

    // 获取用户指标数据
    public List<Map<String, Object>>  generateUserIndicatorViewList(List<Long> userIds, Integer day, Integer schoolLevelFlag, int indicator, int subject, int dataType){
        List<Map<String, Object>> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }
        Map<Long,SumXTestData> dataMap = loadUserListSumData(userIds,day,Arrays.asList(schoolLevelFlag),dataType);
        generateGroupOrUserIndicatotList(resultList ,dataMap,indicator,subject);
        return resultList;
    }

    // 获取部门的指标数据
    public List<Map<String, Object>> generateGroupIndicatorViewList(Long groupId, AgentGroupRoleType groupRoleType, Integer day, Integer schoolLevelFlag, int dimension, int indicator, int subject, int dataType){
        List<Map<String, Object>> resultList = new ArrayList<>();

        Collection<Long> targetGroupList = indicatorService.fetchGroupList(groupId, groupRoleType, dimension);
        if(CollectionUtils.isNotEmpty(targetGroupList)){

            Map<Long, Integer> groupCompositeLevelMap = agentSchoolLevelSupport.fetchTargetCompositeSchoolLevel(targetGroupList, AgentConstants.INDICATOR_TYPE_GROUP, schoolLevelFlag);
            Map<Integer, List<Long>> levelGroupListMap = new HashMap<>();
            groupCompositeLevelMap.forEach((k, v) -> {
                List<Long> groupList = levelGroupListMap.get(v);
                if (CollectionUtils.isEmpty(groupList)) {
                    groupList = new ArrayList<>();
                    levelGroupListMap.put(v, groupList);
                }
                groupList.add(k);
            });
            List<Long> groupList = new ArrayList<>();
            levelGroupListMap.forEach((k,v) -> {
                if(k == SchoolLevel.JUNIOR.getLevel()){
                    groupList.addAll(v);
                }
            });
            Map<Long,SumXTestData> dataMap = loadGroupListSumData(groupList,day,Arrays.asList(schoolLevelFlag),dataType);
            generateGroupOrUserIndicatotList(resultList ,dataMap,indicator,subject);
        }
        return resultList;
    }

    private void getStudentOldNewUnsilence(Map<String,Object> itemMap,XTestData allData,Integer subject){
        switch (subject){
            case 04://汇总
                putMapWithDefaultValue(itemMap,"newSettlementNum",allData == null ? 0 : allData.getNewSettlementNumSglSubj());//新学生
                putMapWithDefaultValue(itemMap,"unsilenceSettlementNum",allData == null ? 0 : allData.getSilenceSettlementNumSglSubj());//沉默学生
                //老学生 = 参与学生-新学生-沉默学生
                Integer oldSettlementNum = allData == null ? 0 : (allData.getSettleStuNumSglSubj() == null ? 0 : allData.getSettleStuNumSglSubj()) - (allData.getNewSettlementNumSglSubj() == null ? 0 : allData.getNewSettlementNumSglSubj()) -
                        (allData.getSilenceSettlementNumSglSubj() == null ? 0 : allData.getSilenceSettlementNumSglSubj());
                putMapWithDefaultValue(itemMap,"oldSettlementNum",oldSettlementNum);//老学生
                break;
            case 01://英语
                putMapWithDefaultValue(itemMap,"newSettlementNum",allData == null ? 0 : allData.getNewSettlementNumEng());//新学生
                putMapWithDefaultValue(itemMap,"unsilenceSettlementNum",allData == null ? 0 : allData.getSilenceSettlementNumEng());//沉默学生
                //老学生 = 参与学生-新学生-沉默学生
                Integer oldSettlementNumEn = allData == null ? 0 : (allData.getSettleStuNumEng() == null ? 0 : allData.getSettleStuNumEng()) - (allData.getNewSettlementNumEng() == null ? 0 : allData.getNewSettlementNumEng()) -
                        (allData.getSilenceSettlementNumEng() == null ? 0 : allData.getSilenceSettlementNumEng());
                putMapWithDefaultValue(itemMap,"oldSettlementNum",oldSettlementNumEn);//老学生
                break;
            case 02://数学
                putMapWithDefaultValue(itemMap,"newSettlementNum",allData == null ? 0 : allData.getNewSettlementNumMath());//新学生
                putMapWithDefaultValue(itemMap,"unsilenceSettlementNum",allData == null ? 0 : allData.getSilenceSettlementNumMath());//沉默学生
                //老学生 = 参与学生-新学生-沉默学生
                Integer oldSettlementNumMath =allData == null ? 0 : (allData.getSettleStuNumMath() == null ? 0 : allData.getSettleStuNumMath()) - (allData.getNewSettlementNumMath() == null ? 0 : allData.getNewSettlementNumMath()) -
                        (allData.getSilenceSettlementNumMath() == null ? 0 : allData.getSilenceSettlementNumMath());
                putMapWithDefaultValue(itemMap,"oldSettlementNum",oldSettlementNumMath);//老学生
                break;
            case 03://语文
                putMapWithDefaultValue(itemMap,"newSettlementNum",allData == null ? 0 : allData.getNewSettlementNumChn());//新学生
                putMapWithDefaultValue(itemMap,"unsilenceSettlementNum",allData == null ? 0 : allData.getSilenceSettlementNumChn());//沉默学生
                //老学生 = 参与学生-新学生-沉默学生
                Integer oldSettlementNumCn = allData == null ? 0 : (allData.getSettleStuNumChn() == null ? 0 : allData.getSettleStuNumChn()) - (allData.getNewSettlementNumChn() == null ? 0 : allData.getNewSettlementNumChn()) -
                        (allData.getSilenceSettlementNumChn() == null ? 0 : allData.getSilenceSettlementNumChn());
                putMapWithDefaultValue(itemMap,"oldSettlementNum",oldSettlementNumCn);//老学生
                break;
            default:
                putMapWithDefaultValue(itemMap,"newSettlementNum",allData == null ? 0 : allData.getNewSettlementNumSglSubj());//新学生
                putMapWithDefaultValue(itemMap,"unsilenceSettlementNum",allData == null ? 0 : allData.getSilenceSettlementNumSglSubj());//沉默学生
                //老学生 = 参与学生-新学生-沉默学生
                Integer oldSettlementNumAll = allData == null ? 0 : (allData.getSettleStuNumSglSubj() == null ? 0 : allData.getSettleStuNumSglSubj()) - (allData.getNewSettlementNumSglSubj() == null ? 0 : allData.getNewSettlementNumSglSubj()) -
                        (allData.getSilenceSettlementNumSglSubj() == null ? 0 : allData.getSilenceSettlementNumSglSubj());
                putMapWithDefaultValue(itemMap,"oldSettlementNum",oldSettlementNumAll);//老学生
                break;
        }
    }
    private void generateGroupOrUserIndicatotList(List<Map<String, Object>> resultList ,Map<Long,SumXTestData> dataMap ,int indicator,int subject){
        dataMap.forEach((k,v)->{
            Map<String,Object> itemMap = new HashMap<>();
            resultList.add(itemMap);
            itemMap.put("id",k);
            itemMap.put("name",v.getName());
            itemMap.put("idType",v.getDataType());
            MapXTestData examData = v.fetchExameData();
            MapXTestData activityData = v.fetchActivityData();
            if(examData != null){
                XTestData allData = examData.getXTestDataMap().get(AgentConstants.XTEST_DATA_ALL);
                XTestData cityData = examData.getXTestDataMap().get(AgentConstants.XTEST_DATA_CITY);
                XTestData countyData = examData.getXTestDataMap().get(AgentConstants.XTEST_DATA_COUNTY);
                XTestData schoolData = examData.getXTestDataMap().get(AgentConstants.XTEST_DATA_SCHOOL);
                if(indicator == 01){
                    switch (subject){
                        case 04://汇总
                            putMapWithDefaultValue(itemMap,"allStuNum",allData == null ? 0 : allData.getSettleStuNumSglSubj());//参与学生数
                            putMapWithDefaultValue(itemMap,"allExamNum",allData == null ? 0 : allData.getXtestNumSglSubj());//统考总场数
                            putMapWithDefaultValue(itemMap,"cityExamNum",cityData == null ? 0 : cityData.getXtestNumSglSubj());//市级场次
                            putMapWithDefaultValue(itemMap,"countyExamNum",countyData == null ? 0 : countyData.getXtestNumSglSubj());//区级场次
                            putMapWithDefaultValue(itemMap,"schoolExamNum",schoolData == null ? 0 : schoolData.getXtestNumSglSubj());//校级场次
                            break;
                        case 01://英语
                            putMapWithDefaultValue(itemMap,"allStuNum",allData == null ? 0 : allData.getSettleStuNumEng());//参与学生数
                            putMapWithDefaultValue(itemMap,"allExamNum",allData == null ? 0 : allData.getXtestNumEng());//统考总场数
                            putMapWithDefaultValue(itemMap,"cityExamNum",cityData == null ? 0 : cityData.getXtestNumEng());//市级场次
                            putMapWithDefaultValue(itemMap,"countyExamNum",countyData == null ? 0 : countyData.getXtestNumEng());//区级场次
                            putMapWithDefaultValue(itemMap,"schoolExamNum",schoolData == null ? 0 : schoolData.getXtestNumEng());//校级场次
                            break;
                        case 02://数学
                            putMapWithDefaultValue(itemMap,"allStuNum",allData == null ? 0 : allData.getSettleStuNumMath());//参与学生数
                            putMapWithDefaultValue(itemMap,"allExamNum",allData == null ? 0 : allData.getXtestNumMath());//统考总场数
                            putMapWithDefaultValue(itemMap,"cityExamNum",cityData == null ? 0 : cityData.getXtestNumMath());//市级场次
                            putMapWithDefaultValue(itemMap,"countyExamNum",countyData == null ? 0 : countyData.getXtestNumMath());//区级场次
                            putMapWithDefaultValue(itemMap,"schoolExamNum",schoolData == null ? 0 : schoolData.getXtestNumMath());//校级场次
                            break;
                        case 03://语文
                            putMapWithDefaultValue(itemMap,"allStuNum",allData == null ? 0 : allData.getSettleStuNumChn());//参与学生数
                            putMapWithDefaultValue(itemMap,"allExamNum",allData == null ? 0 : allData.getXtestNumChn());//统考总场数
                            putMapWithDefaultValue(itemMap,"cityExamNum",cityData == null ? 0 : cityData.getXtestNumChn());//市级场次
                            putMapWithDefaultValue(itemMap,"countyExamNum",countyData == null ? 0 : countyData.getXtestNumChn());//区级场次
                            putMapWithDefaultValue(itemMap,"schoolExamNum",schoolData == null ? 0 : schoolData.getXtestNumChn());//校级场次
                            break;
                        default:
                            putMapWithDefaultValue(itemMap,"allStuNum",allData == null ? 0 : allData.getSettleStuNumSglSubj());//参与学生数
                            putMapWithDefaultValue(itemMap,"allExamNum",allData == null ? 0 : allData.getXtestNumSglSubj());//统考总场数
                            putMapWithDefaultValue(itemMap,"cityExamNum",cityData == null ? 0 : cityData.getXtestNumSglSubj());//市级场次
                            putMapWithDefaultValue(itemMap,"countyExamNum",countyData == null ? 0 : countyData.getXtestNumSglSubj());//区级场次
                            putMapWithDefaultValue(itemMap,"schoolExamNum",schoolData == null ? 0 : schoolData.getXtestNumSglSubj());//校级场次
                            break;
                    }
                }else if(indicator == 02){
                    getStudentOldNewUnsilence(itemMap,allData,subject);
                }
            }
            if(activityData != null){
                XTestData allData = activityData.getXTestDataMap().get(AgentConstants.XTEST_DATA_ALL);
                if(indicator == 03){
                    switch (subject){
                        case 04://汇总
                            putMapWithDefaultValue(itemMap,"allStuNum",allData == null ? 0 : allData.getSettleStuNumSglSubj());//参与学生数
                            putMapWithDefaultValue(itemMap,"allExamNum",allData == null ? 0 : allData.getXtestNumSglSubj());//统考总场数
                            break;
                        case 01://英语
                            putMapWithDefaultValue(itemMap,"allStuNum",allData == null ? 0 : allData.getSettleStuNumEng());//参与学生数
                            putMapWithDefaultValue(itemMap,"allExamNum",allData == null ? 0 : allData.getXtestNumEng());//统考总场数
                            break;
                        case 02://数学
                            putMapWithDefaultValue(itemMap,"allStuNum",allData == null ? 0 : allData.getSettleStuNumMath());//参与学生数
                            putMapWithDefaultValue(itemMap,"allExamNum",allData == null ? 0 : allData.getXtestNumMath());//统考总场数
                            break;
                        case 03://语文
                            putMapWithDefaultValue(itemMap,"allStuNum",allData == null ? 0 : allData.getSettleStuNumChn());//参与学生数
                            putMapWithDefaultValue(itemMap,"allExamNum",allData == null ? 0 : allData.getXtestNumChn());//统考总场数
                            break;
                        default:
                            putMapWithDefaultValue(itemMap,"allStuNum",allData == null ? 0 : allData.getSettleStuNumSglSubj());//参与学生数
                            putMapWithDefaultValue(itemMap,"allExamNum",allData == null ? 0 : allData.getXtestNumSglSubj());//统考总场数
                            break;
                    }
                }else if(indicator == 04){
                    getStudentOldNewUnsilence(itemMap,allData,subject);
                }
            }
        });
    }

    // 获取学校单一指标数据
    public List<Map<String, Object>> generateSchoolIndicatorViewList(List<Long> schoolIds, Integer day, int indicator, int subject, int monthOrDay) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(schoolIds)){
            return resultList;
        }

        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();

        Map<Long, SchoolXTestData> indicatorMap = loadXTestServiceClient.loadXSchoolDetailDataByIds(schoolIds,monthOrDay, day);
        indicatorMap.values().forEach(p -> {
            Map<String, Object> map = generateSchoolDetailData(p, indicator, subject);
            if(MapUtils.isNotEmpty(map)){
                resultList.add(map);
            }
        });

        resultList.forEach(p -> {
            Long schoolId = SafeConverter.toLong(p.get("id"));
            School school = schoolMap.get(schoolId);
            if(school != null){
                p.put("name", school.getCname());
            }
        });

        return resultList;
    }

    //indicator 01 统考学生 02 统考新学生 03 活动学生 04 活动新学生
    //subject 04：全部  01：英语  02：数学  03：语文
    private Map<String,Object> generateSchoolDetailData(SchoolXTestData schoolXTestData, int indicator, int subject){
        Map<String, Object> resultMap = new HashMap<>();
        if(schoolXTestData == null){
            return resultMap;
        }
        int indicatorValue = 0;
        MapXTestData mapXTestData = null;
        if(indicator == 01 || indicator == 02){
            mapXTestData = schoolXTestData.fetchExameData();
        }else {
            mapXTestData = schoolXTestData.fetchActivityData();
        }

        if(mapXTestData != null) {
            XTestData allData = mapXTestData.getXTestDataMap().get(AgentConstants.XTEST_DATA_ALL);
            if (indicator == 01 || indicator == 03) {
                switch (subject) {
                    case 04://汇总
                        indicatorValue = allData == null ? 0 : allData.getSettleStuNumSglSubj();//参与学生数
                    case 01://英语
                        indicatorValue = allData == null ? 0 : allData.getSettleStuNumEng();//英语参与学生数
                        break;
                    case 02://数学
                        indicatorValue = allData == null ? 0 : allData.getSettleStuNumMath();//数学参与学生数
                        break;
                    case 03://语文
                        indicatorValue = allData == null ? 0 : allData.getSettleStuNumChn();//语文参与学生数
                        break;
                    default:
                        indicatorValue = allData == null ? 0 : allData.getSettleStuNumSglSubj();//总参与学生数
                        break;
                }
            } else if (indicator == 02 || indicator == 04) {
                switch (subject) {
                    case 04://汇总
                        indicatorValue = allData == null ? 0 : allData.getNewSettlementNumSglSubj();//新学生
                        break;
                    case 01://英语
                        indicatorValue = allData == null ? 0 : allData.getNewSettlementNumEng();//英语
                        break;
                    case 02://数学
                        indicatorValue = allData == null ? 0 : allData.getNewSettlementNumMath();//新学生
                        break;
                    case 03://语文
                        indicatorValue = allData == null ? 0 : allData.getNewSettlementNumChn();//新学生
                        break;
                    default:
                        indicatorValue = allData == null ? 0 : allData.getNewSettlementNumSglSubj();//新学生
                        break;
                }
            }
        }
        resultMap.put("id", schoolXTestData.getId());
        resultMap.put("indicatorValue",indicatorValue);
        return resultMap;
    }


}
