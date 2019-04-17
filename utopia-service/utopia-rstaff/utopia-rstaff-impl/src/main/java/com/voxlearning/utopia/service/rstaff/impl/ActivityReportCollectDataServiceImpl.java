package com.voxlearning.utopia.service.rstaff.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.rstaff.api.entity.ActivityReportCollectData;
import com.voxlearning.utopia.service.rstaff.api.service.ActivityReportCollectDataService;
import com.voxlearning.utopia.service.rstaff.impl.dao.ActivityReportCollectDataPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Named
@Service(interfaceClass = ActivityReportCollectDataService.class)
@ExposeService(interfaceClass = ActivityReportCollectDataService.class)
public class ActivityReportCollectDataServiceImpl extends SpringContainerSupport implements ActivityReportCollectDataService {

    @Inject
    private ActivityReportCollectDataPersistence activityReportCollectDataPersistence;

    @Inject
    private ActivityConfigServiceClient activityConfigServiceClient;


    @Override
    public Map<Long, Long> loadParticipateCountMapByClazzIds(String activityId) {
        return activityReportCollectDataPersistence.loadParticipateCountByClazzIds(activityId);
    }

    @Override
    public MapMessage saveActivityReportCollectDatas(List<ActivityReportCollectData> inserts) {
        activityReportCollectDataPersistence.inserts(inserts);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteAll() {
        activityReportCollectDataPersistence.deleteAll();
        return MapMessage.successMessage();
    }

    @Override
    public void batchUpdateActivityReportCollectDatas(List<ActivityReportCollectData> collectDatas) {
        if(CollectionUtils.isNotEmpty(collectDatas)){
            for(ActivityReportCollectData temp : collectDatas){
                activityReportCollectDataPersistence.upsert(temp);
            }
        }
    }

    @Override
    public Map<String, Object> loadActivityReportSurvey(String regionLevel, String regionCode, String id) {
        //统计的指标有，市级别统计指标有，参与区域数，参与学校数，参与班级数，参与学生数，人均参与次数
        //区级别的  参与学校数，参与班级数，参与学生数，人均参与次数
        //校级 参与班级数，参与学生数，人均参与次数
        ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(id);
        List<Integer> configClazzLevels = activityConfig.getClazzLevels();
        Map<String,Object> dataMap = new LinkedHashMap<>();
        List<ActivityReportCollectData> collectDatas = activityReportCollectDataPersistence.loadActivityReportCollectDatasByRegionCode(regionLevel,regionCode,id);
        //计算各指标
        Map<Integer,List<ActivityReportCollectData>> regionCollectDatas = collectDatas.stream().collect(Collectors.groupingBy(o -> o.getRegionCode()));
        Integer regions = regionCollectDatas.keySet().size();
        Map<Long,List<ActivityReportCollectData>> schoolCollectDatas = collectDatas.stream().collect(Collectors.groupingBy(o -> o.getSchoolId()));
        Integer schools = schoolCollectDatas.keySet().size();
        Integer clazzs = collectDatas.size();
        Integer students = collectDatas.stream().mapToInt(ActivityReportCollectData::getParticipantStuds).sum();
        //总参与次数
        Integer participantTimes = collectDatas.stream().mapToInt(ActivityReportCollectData::getParticipantTimes).sum();
        BigDecimal avgTimes = new BigDecimal(participantTimes).divide(new BigDecimal(students),2, RoundingMode.HALF_UP);
        dataMap.put("clazzs",clazzs);
        dataMap.put("students",students);
        dataMap.put("participationNums",avgTimes);
        String[] gridHeadList = null;
        //表格数据
        List<List<Object>> gridDataList = new LinkedList<>();
        //图表基础数据
        Map<Integer,List<Map<String,Object>>> barBaseMap = new LinkedHashMap<>(); //年级-- 数据
        Map<Integer,BigDecimal> clazzLevelAvgTimesMap = new LinkedHashMap<>();
        List<Map<String,Object>> clazzAvgTimesList = new LinkedList<>();
        Map<Integer,Set<BigDecimal>> diffSetMap = new LinkedHashMap<>();
        Map<Integer,List<ActivityReportCollectData>> clazzLevelMapTemp = collectDatas.stream().collect(Collectors.groupingBy(o -> o.getClazzLevel()));
        Map<Integer,List<ActivityReportCollectData>> clazzLevelMap = new LinkedHashMap<>();
        for(Integer temp : configClazzLevels){
            List<ActivityReportCollectData> data = clazzLevelMapTemp.get(temp);
            if(CollectionUtils.isNotEmpty(data)){
                clazzLevelMap.put(temp,data);
            }
        }
        if("city".equals(regionLevel)){
            dataMap.put("regions",regions);
            dataMap.put("schools",schools);
            gridHeadList = new String[]{"序号","年级","区县","实际参与学校数","实际参与班级数","实际参与学生数","人均参与次数"};
            //表格数据
            int count = 1;
            //按年级分组
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            while(clazzLevelIt.hasNext()){
                //计算年级平均参与次数
                List<Map<String,Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelRegionDatas = clazzLevelMap.get(clazzLevel);

                Integer clazzLevelStuds = clazzLevelRegionDatas.stream().mapToInt(ActivityReportCollectData::getParticipantStuds).sum();
                Integer clazzLevelTimes = clazzLevelRegionDatas.stream().mapToInt(ActivityReportCollectData::getParticipantTimes).sum();
                BigDecimal clazzLevelAvgTimes = new BigDecimal(clazzLevelTimes).divide(new BigDecimal(clazzLevelStuds),2, RoundingMode.HALF_UP);
                clazzLevelAvgTimesMap.put(clazzLevel,clazzLevelAvgTimes);
                Map<String,Object> clazzLevelAvgTimesTemp = new LinkedHashMap<>();
                clazzLevelAvgTimesTemp.put("clazzLevel",ClazzLevel.getDescription(clazzLevel));
                clazzLevelAvgTimesTemp.put("avgTimes",clazzLevelAvgTimes);
                clazzAvgTimesList.add(clazzLevelAvgTimesTemp);

                Map<Integer,List<ActivityReportCollectData>> regionDatasMap = clazzLevelRegionDatas.stream().collect(Collectors.groupingBy(o -> o.getRegionCode()));
                List<Integer> tempRegions = new ArrayList<>(regionDatasMap.keySet());//按照区县ID排序
                Collections.sort(tempRegions, (o1, o2) -> o1.compareTo(o2));
                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for(int i=0; i<tempRegions.size(); i++){
                    Integer tempRegionCode = tempRegions.get(i);
                    List<ActivityReportCollectData> regionDataList = regionDatasMap.get(tempRegionCode);
                    ActivityReportCollectData activityReportCollectData = regionDataList.get(0);
                    //统计，每个区县，参与学校数，参与班级数，板与学生数
                    Map<Long,List<ActivityReportCollectData>> clazzLevelDataMaptoSchoolId = regionDataList.stream().collect(Collectors.groupingBy(o -> o.getSchoolId()));
                    //学校数
                    Integer tempSchools = clazzLevelDataMaptoSchoolId.keySet().size();
                    //班级数
                    Integer tempClazzs = regionDataList.size();
                    //学上数
                    Integer tempStudents = regionDataList.stream().mapToInt(ActivityReportCollectData::getParticipantStuds).sum();
                    //参与次数
                    Integer tempTimes = regionDataList.stream().mapToInt(ActivityReportCollectData::getParticipantTimes).sum();
                    //人均参与次数
                    BigDecimal tempavgTimes = new BigDecimal(tempTimes).divide(new BigDecimal(tempStudents),2, RoundingMode.HALF_UP);
                    diffSet.add(tempavgTimes);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(activityReportCollectData.getRegionName());
                    tempData.add(tempSchools);
                    tempData.add(tempClazzs);
                    tempData.add(tempStudents);
                    tempData.add(tempavgTimes);
                    gridDataList.add(tempData);
                    Map<String,Object> barMap = new LinkedHashMap<>();
                    barMap.put("no",count);
                    barMap.put("region",tempRegionCode);
                    barMap.put("regionName",activityReportCollectData.getRegionName());
                    barMap.put("avgTimes",tempavgTimes);
                    barDatas.add(barMap);
                    count++;
                }
                diffSetMap.put(clazzLevel,diffSet);
                barBaseMap.put(clazzLevel,barDatas);
            }
        }else if("county".equals(regionLevel)){
            dataMap.put("schools",schools);
            gridHeadList = new String[]{"序号","年级","学校","实际参与班级数","实际参与学生数","人均参与次数"};
            int count = 1;
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            while(clazzLevelIt.hasNext()){
                List<Map<String,Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelData = clazzLevelMap.get(clazzLevel);

                Integer clazzLevelStuds = clazzLevelData.stream().mapToInt(ActivityReportCollectData::getParticipantStuds).sum();
                Integer clazzLevelTimes = clazzLevelData.stream().mapToInt(ActivityReportCollectData::getParticipantTimes).sum();
                BigDecimal clazzLevelAvgTimes = new BigDecimal(clazzLevelTimes).divide(new BigDecimal(clazzLevelStuds),2, RoundingMode.HALF_UP);
                clazzLevelAvgTimesMap.put(clazzLevel,clazzLevelAvgTimes);

                Map<Long,List<ActivityReportCollectData>> schoolDatasMap = clazzLevelData.stream().collect(Collectors.groupingBy(o -> o.getSchoolId()));
                List<Long> tempSchools = new ArrayList<>(schoolDatasMap.keySet());//按照学校ID排序
                Collections.sort(tempSchools, (o1, o2) -> o1.compareTo(o2));
                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for(int i=0; i<tempSchools.size(); i++) {
                    Long tempSchoolId = tempSchools.get(i);

                    List<ActivityReportCollectData> regionDataList = schoolDatasMap.get(tempSchoolId);
                    ActivityReportCollectData activityReportCollectData = regionDataList.get(0);
                    //班级数
                    Integer tempClazzs = regionDataList.size();
                    //学生数
                    Integer tempStudents = regionDataList.stream().mapToInt(ActivityReportCollectData::getParticipantStuds).sum();
                    //参与次数
                    Integer tempTimes = regionDataList.stream().mapToInt(ActivityReportCollectData::getParticipantTimes).sum();
                    //人均参与次数
                    BigDecimal tempavgTimes = new BigDecimal(tempTimes).divide(new BigDecimal(tempStudents),2, RoundingMode.HALF_UP);
                    diffSet.add(tempavgTimes);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(activityReportCollectData.getSchoolName());
                    tempData.add(tempClazzs);
                    tempData.add(tempStudents);
                    tempData.add(tempavgTimes);
                    gridDataList.add(tempData);
                    Map<String,Object> barMap = new LinkedHashMap<>();
                    barMap.put("no",count);
                    barMap.put("region",tempSchoolId);
                    barMap.put("regionName",activityReportCollectData.getSchoolName());
                    barMap.put("avgTimes",tempavgTimes);
                    barDatas.add(barMap);
                    count++;
                }
                diffSetMap.put(clazzLevel,diffSet);
                barBaseMap.put(clazzLevel,barDatas);
            }
        }else{
            //校级
            gridHeadList = new String[]{"序号","年级","班级","实际参与学生数","人均参与次数"};
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            int count = 1;
            while(clazzLevelIt.hasNext()){
                List<Map<String,Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelData = clazzLevelMap.get(clazzLevel);

                Integer clazzLevelStuds = clazzLevelData.stream().mapToInt(ActivityReportCollectData::getParticipantStuds).sum();
                Integer clazzLevelTimes = clazzLevelData.stream().mapToInt(ActivityReportCollectData::getParticipantTimes).sum();
                BigDecimal clazzLevelAvgTimes = new BigDecimal(clazzLevelTimes).divide(new BigDecimal(clazzLevelStuds),2, RoundingMode.HALF_UP);
                clazzLevelAvgTimesMap.put(clazzLevel,clazzLevelAvgTimes);
                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for(int i=0; i<clazzLevelData.size(); i++){
                    ActivityReportCollectData temp = clazzLevelData.get(i);
                    //统计，参与学生数
                    //学生数
                    Integer tempStudents = temp.getParticipantStuds();
                    //参与次数
                    Integer tempTimes = temp.getParticipantTimes();
                    //人均参与次数
                    BigDecimal tempavgTimes = new BigDecimal(tempTimes).divide(new BigDecimal(tempStudents),2, RoundingMode.HALF_UP);
                    diffSet.add(tempavgTimes);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);

                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(temp.getClazzName());
                    tempData.add(tempStudents);
                    tempData.add(tempavgTimes);
                    gridDataList.add(tempData);
                    Map<String,Object> barMap = new LinkedHashMap<>();
                    barMap.put("no",count);
                    barMap.put("region",temp.getClazzId());
                    barMap.put("regionName",temp.getClazzName());
                    barMap.put("avgTimes",tempavgTimes);
                    barDatas.add(barMap);
                    count ++;
                }
                diffSetMap.put(clazzLevel,diffSet);
                barBaseMap.put(clazzLevel,barDatas);
            }
        }
        //表格数据
        Map<String,Object> gridDataMap = new LinkedHashMap<>();
        gridDataMap.put("gridHead",gridHeadList);
        gridDataMap.put("gridData",gridDataList);
        dataMap.put("grid",gridDataMap);

        //根据表格的数据拼接柱图，按年级去区分数据
        List<Map<String,Object>> viewBarList = new LinkedList<>();
        Iterator<Integer> clazzLevels = barBaseMap.keySet().iterator();
        //年级的数据对比
        List<String> clazzLevelxAxisData = new LinkedList<>();
        List<Object> clazzLevelseriesData = new LinkedList<>();
        List<String> clazzLevelLegendData = new LinkedList<>();
        Set<BigDecimal> clazzLevelDiffSet = new LinkedHashSet<>();
        while (clazzLevels.hasNext()){
            Integer clazzLevel = clazzLevels.next();
            Map<String,Object> viewBar = new LinkedHashMap<>();
            List<Map<String,Object>> barDatas = barBaseMap.get(clazzLevel);
            Set<BigDecimal> diffSet = diffSetMap.get(clazzLevel);
            BigDecimal clazzLevelAvgTimes = clazzLevelAvgTimesMap.get(clazzLevel);
            clazzLevelxAxisData.add(ClazzLevel.getDescription(clazzLevel));
            clazzLevelseriesData.add(clazzLevelAvgTimes);
            clazzLevelDiffSet.add(clazzLevelAvgTimes);

            List<String> legendData = new LinkedList<>();
            legendData.add("整体");
            if("city".equals(regionLevel)){
                viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各区县-人均参与次数");
                viewBar.put("viewRegion","区/县");
                legendData.add("各区域");
            }else if("county".equals(regionLevel)){
                viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各学校-人均参与次数");
                viewBar.put("viewRegion","学校");
                legendData.add("各学校");
            }else{
                viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各班级-人均参与次数");
                viewBar.put("viewRegion","班级");
                legendData.add("各班级");
            }
            //横轴 ，区域，学校，班级
            viewBar.put("legendData",legendData);
            List<Object> xAxisData = new LinkedList<>();
            List<Object> seriesData = new LinkedList<>();
            for(Map<String,Object> temp : barDatas){
                seriesData.add(temp.get("avgTimes"));
                Map<String,Object> xData = new LinkedHashMap<>();
                xData.put("no",temp.get("no"));
                xData.put("name",temp.get("regionName"));
                xAxisData.add(xData);
            }
            viewBar.put("xAxisData",xAxisData);
            viewBar.put("seriesData",seriesData);
            viewBar.put("legendData",legendData);
            viewBar.put("clazzLevel",ClazzLevel.getDescription(clazzLevel));
            viewBar.put("regions",barDatas.size());
            viewBar.put("unit","次/人");
            viewBar.put("wholeAvgNums",clazzLevelAvgTimes);
            //计算topThree
            getTopTree(viewBar,barDatas,diffSet,clazzLevelAvgTimes);
            List<Map<String,Object>> topThree = (List<Map<String, Object>>) viewBar.get("topThree");
            Map<String,Object> lastOne = (Map<String, Object>) viewBar.get("lastOne");
            if(CollectionUtils.isNotEmpty(topThree)){
                //拼接topThree
                editTopThree(viewBar,topThree,"次/人");
            }
            if(MapUtils.isNotEmpty(lastOne)){
                editLastOne(viewBar,lastOne,"次/人");
            }
            viewBarList.add(viewBar);
        }

        clazzLevelLegendData.add("各年级");
        clazzLevelLegendData.add("整体");

        Map<String,Object> clazzLevelViewBar = new LinkedHashMap<>();
        clazzLevelViewBar.put("xAxisData",clazzLevelxAxisData);
        clazzLevelViewBar.put("seriesData",clazzLevelseriesData);
        clazzLevelViewBar.put("legendData",clazzLevelLegendData);
        clazzLevelViewBar.put("unit","次/人");
        clazzLevelViewBar.put("grades",barBaseMap.keySet().size());

        getTopTree(clazzLevelViewBar,clazzAvgTimesList,clazzLevelDiffSet,avgTimes);
        viewBarList.add(clazzLevelViewBar);
        dataMap.put("viewVarList",viewBarList);
        return dataMap;
    }

    private void editLastOne(Map<String, Object> viewBar, Map<String, Object> lastOne, String unit) {
        String regionName = (String) lastOne.get("regionName");
        BigDecimal avgTimes = (BigDecimal) lastOne.get("avgTimes");
        String lastOneStr = "("+avgTimes+unit+")";
        Map<String,Object> lastOneTemp = new LinkedHashMap<>();
        lastOneTemp.put("lastName",regionName);
        lastOneTemp.put("lastValue",lastOneStr);
        viewBar.put("lastOne",lastOneTemp);
    }

    private void editTopThree(Map<String, Object> viewBar, List<Map<String, Object>> topThree, String unit) {
        List<String> newTopThree = new LinkedList<>();
        for(Map<String,Object> temp : topThree){
            String regionName = (String) temp.get("regionName");
            BigDecimal avgTimes = (BigDecimal) temp.get("avgTimes");
            newTopThree.add(regionName+"("+avgTimes+unit+")");
        }
        viewBar.put("topThree",newTopThree);
    }

    @Override
    public Map<String, Object> loadActivityScoreState(String regionLevel, String regionCode, String id) {
        ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(id);
        List<Integer> configClazzLevels = activityConfig.getClazzLevels();
        Map<String,Object> dataMap = new LinkedHashMap<>();
        List<ActivityReportCollectData> collectDatas = activityReportCollectDataPersistence.loadActivityReportCollectDatasByRegionCode(regionLevel,regionCode,id);
        //topScoreSum   ,   topScoreStudSum   ,班级维度的最高分的人数的汇总数据，和最高分总数的汇总
        //计算总分
        Integer fullMarks = activityConfig.getRules().getLimitAmount();
        Map<String,Object> wholeScoreMap = new LinkedHashMap<>();
        //计算整体平均分,平均分总分，总人数
        Integer wholeAvgScore = collectDatas.stream().mapToInt(ActivityReportCollectData::getTopScoreSum).sum();
        Integer wholeStuds = collectDatas.stream().mapToInt(ActivityReportCollectData::getTopScoreStudSum).sum();
        //总体平均分
        BigDecimal highAvgScore = new BigDecimal(wholeAvgScore).divide(new BigDecimal(wholeStuds),2, RoundingMode.HALF_UP);
        wholeScoreMap.put("highAvgScore",highAvgScore);
        if(activityConfig.getType()== ActivityTypeEnum.SUDOKU){
            wholeScoreMap.put("fullMarks",fullMarks);
            //得分率
            BigDecimal wholeScoreRate = highAvgScore.divide(new BigDecimal(fullMarks),4,RoundingMode.HALF_UP).multiply(new BigDecimal(100));
            wholeScoreMap.put("wholeScoreRate",wholeScoreRate);
        }else{
            wholeScoreMap.put("fullMarks","不限总分");
        }

        //表格数据
        Map<String,Object> gridDataMap = new LinkedHashMap<>();
        String[] gridHeadList = null;
        List<List<Object>> gridDataList = new LinkedList<>();
        //图表基础数据
        Map<Integer,List<Map<String,Object>>> barBaseMap = new LinkedHashMap<>(); //年级-- 数据
        Map<Integer,BigDecimal> clazzLevelAvgTopScoreMap = new LinkedHashMap<>();//年级--平均分
        Map<Integer,Set<BigDecimal>> diffSetMap = new LinkedHashMap<>();
        Map<Integer,List<ActivityReportCollectData>> clazzLevelMapTemp = collectDatas.stream().collect(Collectors.groupingBy(o -> o.getClazzLevel()));
        Map<Integer,List<ActivityReportCollectData>> clazzLevelMap = new LinkedHashMap<>();
        for(Integer temp : configClazzLevels){
            List<ActivityReportCollectData> data = clazzLevelMapTemp.get(temp);
            if(CollectionUtils.isNotEmpty(data)){
                clazzLevelMap.put(temp,data);
            }
        }
        //参与总年级数目
        wholeScoreMap.put("grades",clazzLevelMap.keySet().size());
        if("city".equals(regionLevel)){
            wholeScoreMap.put("wholeViewRegion","区/县");
            //参与总区域数据
            Map<Integer,List<ActivityReportCollectData>> regionsToDatas = collectDatas.stream().collect(Collectors.groupingBy(ActivityReportCollectData::getRegionCode));
            wholeScoreMap.put("regions",regionsToDatas.keySet().size());
            gridHeadList = new String[]{"序号","年级","区/县","最高分平均分"};
            //表格数据

            int count = 1;
            //按年级分组
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            while(clazzLevelIt.hasNext()){
                //计算年级平均参与次数
                List<Map<String,Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelRegionDatas = clazzLevelMap.get(clazzLevel);

                Integer clazzLevelStuds = clazzLevelRegionDatas.stream().mapToInt(ActivityReportCollectData::getTopScoreStudSum).sum();
                Integer clazzLevelScores = clazzLevelRegionDatas.stream().mapToInt(ActivityReportCollectData::getTopScoreSum).sum();
                //年级平均分
                BigDecimal clazzLevelAvgScore = new BigDecimal(clazzLevelScores).divide(new BigDecimal(clazzLevelStuds),2, RoundingMode.HALF_UP);
                clazzLevelAvgTopScoreMap.put(clazzLevel,clazzLevelAvgScore);

                Map<Integer,List<ActivityReportCollectData>> regionDatasMap = clazzLevelRegionDatas.stream().collect(Collectors.groupingBy(o -> o.getRegionCode()));
                List<Integer> tempRegions = new ArrayList<>(regionDatasMap.keySet());//按照区县ID排序
                Collections.sort(tempRegions, (o1, o2) -> o1.compareTo(o2));
                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for(int i=0; i<tempRegions.size(); i++){
                    Integer tempRegionCode = tempRegions.get(i);
                    List<ActivityReportCollectData> regionDataList = regionDatasMap.get(tempRegionCode);
                    ActivityReportCollectData activityReportCollectData = regionDataList.get(0);
                    //统计，每个区县，最高分平均分，各区平均分的总分，除以总人数
                    Integer tempScores = regionDataList.stream().mapToInt(ActivityReportCollectData::getTopScoreSum).sum();
                    Integer tempStuds = regionDataList.stream().mapToInt(ActivityReportCollectData::getTopScoreStudSum).sum();
                    BigDecimal tempRegionAvgScore = new BigDecimal(tempScores).divide(new BigDecimal(tempStuds),2, RoundingMode.HALF_UP);

                    diffSet.add(tempRegionAvgScore);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(activityReportCollectData.getRegionName());
                    tempData.add(tempRegionAvgScore);
                    gridDataList.add(tempData);
                    Map<String,Object> barMap = new LinkedHashMap<>();
                    barMap.put("no",count);
                    barMap.put("region",tempRegionCode);
                    barMap.put("regionName",activityReportCollectData.getRegionName());
                    barMap.put("avgTimes",tempRegionAvgScore);
                    barDatas.add(barMap);
                    count++;
                }
                diffSetMap.put(clazzLevel,diffSet);
                barBaseMap.put(clazzLevel,barDatas);
            }
        }else if("county".equals(regionLevel)){
            wholeScoreMap.put("wholeViewRegion","学校");
            Map<Long,List<ActivityReportCollectData>> schoolsToDatas = collectDatas.stream().collect(Collectors.groupingBy(ActivityReportCollectData::getSchoolId));
            wholeScoreMap.put("regions",schoolsToDatas.keySet().size());
            gridHeadList = new String[]{"序号","年级","学校","最高分平均分"};

            int count = 1;
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            while(clazzLevelIt.hasNext()){
                List<Map<String,Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelSchoolData = clazzLevelMap.get(clazzLevel);

                Integer clazzLevelStuds = clazzLevelSchoolData.stream().mapToInt(ActivityReportCollectData::getTopScoreStudSum).sum();
                Integer clazzLevelScores = clazzLevelSchoolData.stream().mapToInt(ActivityReportCollectData::getTopScoreSum).sum();
                //年级平均分
                BigDecimal clazzLevelAvgScore = new BigDecimal(clazzLevelScores).divide(new BigDecimal(clazzLevelStuds),2, RoundingMode.HALF_UP);
                clazzLevelAvgTopScoreMap.put(clazzLevel,clazzLevelAvgScore);

                Map<Long,List<ActivityReportCollectData>> schoolDatasMap = clazzLevelSchoolData.stream().collect(Collectors.groupingBy(o -> o.getSchoolId()));
                List<Long> tempSchools = new ArrayList<>(schoolDatasMap.keySet());//按照学校ID排序
                Collections.sort(tempSchools, (o1, o2) -> o1.compareTo(o2));
                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for(int i=0; i<tempSchools.size(); i++) {
                    Long tempSchoolId = tempSchools.get(i);
                    List<ActivityReportCollectData> regionDataList = schoolDatasMap.get(tempSchoolId);
                    ActivityReportCollectData activityReportCollectData = regionDataList.get(0);
                    //统计，每个区县，最高分平均分，各学校平均分的总分，除以总人数
                    Integer tempScores = regionDataList.stream().mapToInt(ActivityReportCollectData::getTopScoreSum).sum();
                    Integer tempStuds = regionDataList.stream().mapToInt(ActivityReportCollectData::getTopScoreStudSum).sum();
                    BigDecimal tempSchoolAvgScore = new BigDecimal(tempScores).divide(new BigDecimal(tempStuds),2, RoundingMode.HALF_UP);

                    diffSet.add(tempSchoolAvgScore);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(activityReportCollectData.getSchoolName());
                    tempData.add(tempSchoolAvgScore);
                    gridDataList.add(tempData);
                    Map<String,Object> barMap = new LinkedHashMap<>();
                    barMap.put("no",count);
                    barMap.put("region",tempSchoolId);
                    barMap.put("regionName",activityReportCollectData.getSchoolName());
                    barMap.put("avgTimes",tempSchoolAvgScore);
                    barDatas.add(barMap);
                    count++;
                }
                diffSetMap.put(clazzLevel,diffSet);
                barBaseMap.put(clazzLevel,barDatas);
            }

        }else{
            wholeScoreMap.put("wholeViewRegion","班级");
            Map<Long,List<ActivityReportCollectData>> clazzsToDatas = collectDatas.stream().collect(Collectors.groupingBy(ActivityReportCollectData::getClazzId));
            wholeScoreMap.put("regions",clazzsToDatas.keySet().size());
            gridHeadList = new String[]{"序号","年级","班级","最高分平均分"};

            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            int count = 1;
            while(clazzLevelIt.hasNext()){
                List<Map<String,Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelClazzData = clazzLevelMap.get(clazzLevel);

                //统计，每个区县，最高分平均分，各学校平均分的总分，除以总人数
                Integer clazzLevelScores = clazzLevelClazzData.stream().mapToInt(ActivityReportCollectData::getTopScoreSum).sum();
                Integer clazzLevelStuds = clazzLevelClazzData.stream().mapToInt(ActivityReportCollectData::getTopScoreStudSum).sum();
                //年级平均分
                BigDecimal tempClazzAvgScore = new BigDecimal(clazzLevelScores).divide(new BigDecimal(clazzLevelStuds),2, RoundingMode.HALF_UP);
                clazzLevelAvgTopScoreMap.put(clazzLevel,tempClazzAvgScore);
                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for(int i=0; i<clazzLevelClazzData.size(); i++){
                    ActivityReportCollectData temp = clazzLevelClazzData.get(i);
                    //统计，每个班级的平均分
                    Integer tempScores = temp.getTopScoreSum();
                    Integer tempStuds = temp.getTopScoreStudSum();
                    //人均参与次数
                    BigDecimal tempavgScore = new BigDecimal(tempScores).divide(new BigDecimal(tempStuds),2, RoundingMode.HALF_UP);
                    diffSet.add(tempavgScore);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(temp.getClazzName());
                    tempData.add(tempavgScore);
                    gridDataList.add(tempData);
                    Map<String,Object> barMap = new LinkedHashMap<>();
                    barMap.put("no",count);
                    barMap.put("region",temp.getClazzId());
                    barMap.put("regionName",temp.getClazzName());
                    barMap.put("avgTimes",tempavgScore);
                    barDatas.add(barMap);
                    count ++;
                }
                diffSetMap.put(clazzLevel,diffSet);
                barBaseMap.put(clazzLevel,barDatas);
            }
        }
        gridDataMap.put("gridHead",gridHeadList);
        gridDataMap.put("gridData",gridDataList);
        dataMap.put("gridMap",gridDataMap);
        dataMap.put("wholeScoreMap",wholeScoreMap);

        //根据表格的数据拼接柱图，按年级去区分数据
        List<Map<String,Object>> viewBarList = new LinkedList<>();
        Iterator<Integer> clazzLevels = barBaseMap.keySet().iterator();
        while (clazzLevels.hasNext()){
            Integer clazzLevel = clazzLevels.next();
            Map<String,Object> viewBar = new LinkedHashMap<>();
            List<Map<String,Object>> barDatas = barBaseMap.get(clazzLevel);
            Set<BigDecimal> diffSet = diffSetMap.get(clazzLevel);
            BigDecimal clazzLevelAvgScore = clazzLevelAvgTopScoreMap.get(clazzLevel);

            List<String> legendData = new LinkedList<>();
            legendData.add("整体");
            if("city".equals(regionLevel)){
                viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各区县-最高分平均分");
                viewBar.put("viewRegion","区/县");
                legendData.add("各区域");
            }else if("county".equals(regionLevel)){
                viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各学校-最高分平均分");
                viewBar.put("viewRegion","学校");
                legendData.add("各学校");
            }else{
                viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各班级-最高分平均分");
                viewBar.put("viewRegion","班级");
                legendData.add("各班级");
            }
            //横轴 ，区域，学校，班级
            viewBar.put("legendData",legendData);
            List<Object> xAxisData = new LinkedList<>();
            List<Object> seriesData = new LinkedList<>();
            for(Map<String,Object> temp : barDatas){
                seriesData.add(temp.get("avgTimes"));
                Map<String,Object> xData = new LinkedHashMap<>();
                xData.put("no",temp.get("no"));
                xData.put("name",temp.get("regionName"));
                xAxisData.add(xData);
            }
            viewBar.put("xAxisData",xAxisData);
            viewBar.put("seriesData",seriesData);
            viewBar.put("legendData",legendData);
            viewBar.put("clazzLevel",ClazzLevel.getDescription(clazzLevel));
            viewBar.put("regions",barDatas.size());
            viewBar.put("unit","分");
            viewBar.put("wholeAvgNums",clazzLevelAvgScore);
            //计算topThree
            getTopTree(viewBar,barDatas,diffSet,clazzLevelAvgScore);
            List<Map<String,Object>> topThree = (List<Map<String, Object>>) viewBar.get("topThree");
            Map<String,Object> lastOne = (Map<String, Object>) viewBar.get("lastOne");
            if(CollectionUtils.isNotEmpty(topThree)){
                //拼接topThree
                editTopThree(viewBar,topThree,"分");
            }
            if(MapUtils.isNotEmpty(lastOne)){
                editLastOne(viewBar,lastOne,"分");
            }
            viewBarList.add(viewBar);
        }
        dataMap.put("viewBarList",viewBarList);
        return dataMap;
    }

    @Override
    public Map<String, Object> loadActivityScoreLevel(String regionLevel, String regionCode, String id) {
        Map<String,Object> dataMap = new LinkedHashMap<>();
        List<ActivityReportCollectData> collectDatas = activityReportCollectDataPersistence.loadActivityReportCollectDatasByRegionCode(regionLevel,regionCode,id);
        getScoreLevelCollectData(dataMap, collectDatas);

        //表格数据
        Map<String,Object> gridDataMap = new LinkedHashMap<>();
        String[] gridHeadList = null;
        List<List<Object>> gridDataList = new LinkedList<>();
        //图表基础数据
        Map<Integer,List<Map<String,Object>>> barBaseMap = new LinkedHashMap<>(); //年级-- 数据
        Map<Integer,BigDecimal> clazzLevelAvgTopScoreLevelMap = new LinkedHashMap<>();
        Map<Integer,Set<BigDecimal>> diffSetMap = new LinkedHashMap<>();
        Map<Integer,List<ActivityReportCollectData>> clazzLevelMapTemp = collectDatas.stream().collect(Collectors.groupingBy(o -> o.getClazzLevel()));
        Map<Integer,List<ActivityReportCollectData>> clazzLevelMap = new LinkedHashMap<>();
        ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(id);
        List<Integer> configClazzLevels = activityConfig.getClazzLevels();
        for(Integer temp : configClazzLevels){
            List<ActivityReportCollectData> data = clazzLevelMapTemp.get(temp);
            if(CollectionUtils.isNotEmpty(data)){
                clazzLevelMap.put(temp,data);
            }
        }
        //最高分的各个等级，根据整体的数据决定
        Integer sumScoreLevel1 = collectDatas.stream().filter(o->configClazzLevels.contains(o.getClazzLevel())).mapToInt(ActivityReportCollectData::getScoreLevelStuds1).sum();
        Integer sumScoreLevel2 = collectDatas.stream().filter(o->configClazzLevels.contains(o.getClazzLevel())).mapToInt(ActivityReportCollectData::getScoreLevelStuds2).sum();
        Integer sumScoreLevel3 = collectDatas.stream().filter(o->configClazzLevels.contains(o.getClazzLevel())).mapToInt(ActivityReportCollectData::getScoreLevelStuds3).sum();
        Integer sumScoreLevel4 = collectDatas.stream().filter(o->configClazzLevels.contains(o.getClazzLevel())).mapToInt(ActivityReportCollectData::getScoreLevelStuds4).sum();
        Integer sumScoreLevel5 = collectDatas.stream().filter(o->configClazzLevels.contains(o.getClazzLevel())).mapToInt(ActivityReportCollectData::getScoreLevelStuds5).sum();
        Integer sumScoreLevel6 = collectDatas.stream().filter(o->configClazzLevels.contains(o.getClazzLevel())).mapToInt(ActivityReportCollectData::getScoreLevelStuds6).sum();
        //在这里计算每个年级的最高分是哪一段的分数
        String topScoreDefine = "";
        if(!Objects.equals(sumScoreLevel6,0) || !Objects.equals(sumScoreLevel5,0)){
            topScoreDefine = "scoreLevel5";
        }else if(!Objects.equals(sumScoreLevel4,0)){
            topScoreDefine = "scoreLevel4";
        }else if(!Objects.equals(sumScoreLevel3,0)){
            topScoreDefine = "scoreLevel3";
        }else if(!Objects.equals(sumScoreLevel2,0)){
            topScoreDefine = "scoreLevel2";
        }else{
            topScoreDefine = "scoreLevel1";
        }

        if("city".equals(regionLevel)) {
            gridHeadList = new String[]{"序号", "年级", "区/县","0-9分","10-19分","20-29分","30-39分","40-49分","50分以上"};
            int count = 1;
            //按年级分组
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            while(clazzLevelIt.hasNext()){
                List<Map<String,Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelRegionDatas = clazzLevelMap.get(clazzLevel);

                Integer clazzLevelscoreLevel1 = clazzLevelRegionDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds1).sum();
                Integer clazzLevelscoreLevel2 = clazzLevelRegionDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds2).sum();
                Integer clazzLevelscoreLevel3 = clazzLevelRegionDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds3).sum();
                Integer clazzLevelscoreLevel4 = clazzLevelRegionDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds4).sum();
                Integer clazzLevelscoreLevel5 = clazzLevelRegionDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds5).sum();
                Integer clazzLevelscoreLevel6 = clazzLevelRegionDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds6).sum();
                //在这里计算每个年级的最高分是哪一段的分数
                Integer totalTopScore = 0;
                if(Objects.equals(topScoreDefine,"scoreLevel5")){
                    totalTopScore = clazzLevelscoreLevel5+clazzLevelscoreLevel6;
                }else if(Objects.equals(topScoreDefine,"scoreLevel4")){
                    totalTopScore = clazzLevelscoreLevel4;
                }else if(Objects.equals(topScoreDefine,"scoreLevel3")){
                    totalTopScore = clazzLevelscoreLevel3;
                }else if(Objects.equals(topScoreDefine,"scoreLevel2")){
                    totalTopScore = clazzLevelscoreLevel2;
                }else{
                    totalTopScore = 0;
                }

                BigDecimal clazzLevelScoreRate = new BigDecimal(totalTopScore).multiply(new BigDecimal(100)).
                        divide(new BigDecimal(clazzLevelscoreLevel1+clazzLevelscoreLevel2+clazzLevelscoreLevel3+clazzLevelscoreLevel4+clazzLevelscoreLevel5+clazzLevelscoreLevel6),
                                2, RoundingMode.HALF_UP);
                    clazzLevelAvgTopScoreLevelMap.put(clazzLevel,clazzLevelScoreRate);

                Map<Integer,List<ActivityReportCollectData>> regionDatasMap = clazzLevelRegionDatas.stream().collect(Collectors.groupingBy(o -> o.getRegionCode()));
                List<Integer> tempRegions = new ArrayList<>(regionDatasMap.keySet());//按照区县ID排序
                Collections.sort(tempRegions, (o1, o2) -> o1.compareTo(o2));
                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for(int i=0; i<tempRegions.size(); i++){
                    Integer tempRegionCode = tempRegions.get(i);
                    List<ActivityReportCollectData> regionDataList = regionDatasMap.get(tempRegionCode);
                    ActivityReportCollectData activityReportCollectData = regionDataList.get(0);
                    //统计，每个区县，各分数段的人数
                    Integer scoreLevel1 = regionDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds1).sum();
                    Integer scoreLevel2 = regionDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds2).sum();
                    Integer scoreLevel3 = regionDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds3).sum();
                    Integer scoreLevel4 = regionDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds4).sum();
                    Integer scoreLevel5 = regionDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds5).sum();
                    Integer scoreLevel6 = regionDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds6).sum();
                    Integer allScoreLevelStuds = scoreLevel1 + scoreLevel2 +scoreLevel3 + scoreLevel4 + scoreLevel5 + scoreLevel6;
                    //高分人数比例
                    Integer tempTopScoreTotal = 0;
                    if(Objects.equals(topScoreDefine,"scoreLevel5")){
                        tempTopScoreTotal = scoreLevel5+scoreLevel6;
                    }else if(Objects.equals(topScoreDefine,"scoreLevel4")){
                        tempTopScoreTotal = scoreLevel4;
                    }else if(Objects.equals(topScoreDefine,"scoreLevel3")){
                        tempTopScoreTotal = scoreLevel3;
                    }else if(Objects.equals(topScoreDefine,"scoreLevel2")){
                        tempTopScoreTotal = scoreLevel2;
                    }else{
                        tempTopScoreTotal = 0;
                    }
                    BigDecimal topScoreLevelRate = new BigDecimal(tempTopScoreTotal).multiply(new BigDecimal(100)).divide(new BigDecimal(allScoreLevelStuds),2,RoundingMode.HALF_UP);
                    diffSet.add(topScoreLevelRate);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(activityReportCollectData.getRegionName());
                    tempData.add(scoreLevel1);
                    tempData.add(scoreLevel2);
                    tempData.add(scoreLevel3);
                    tempData.add(scoreLevel4);
                    tempData.add(scoreLevel5);
                    tempData.add(scoreLevel6);
                    gridDataList.add(tempData);
                    Map<String,Object> barMap = new LinkedHashMap<>();
                    barMap.put("no",count);
                    barMap.put("region",tempRegionCode);
                    barMap.put("regionName",activityReportCollectData.getRegionName());
                    barMap.put("avgTimes",topScoreLevelRate);
                    barDatas.add(barMap);
                    count++;
                }
                diffSetMap.put(clazzLevel,diffSet);
                barBaseMap.put(clazzLevel,barDatas);
            }
        }else if("county".equals(regionLevel)){
            gridHeadList = new String[]{"序号", "年级", "学校", "0-9分","10-19分","20-29分","30-39分","40-49分","50分以上"};
            int count = 1;
            //按年级分组
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            while(clazzLevelIt.hasNext()){
                List<Map<String,Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelSchoolDatas = clazzLevelMap.get(clazzLevel);

                Integer clazzLevelscoreLevel1 = clazzLevelSchoolDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds1).sum();
                Integer clazzLevelscoreLevel2 = clazzLevelSchoolDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds2).sum();
                Integer clazzLevelscoreLevel3 = clazzLevelSchoolDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds3).sum();
                Integer clazzLevelscoreLevel4 = clazzLevelSchoolDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds4).sum();
                Integer clazzLevelscoreLevel5 = clazzLevelSchoolDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds5).sum();
                Integer clazzLevelscoreLevel6 = clazzLevelSchoolDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds6).sum();
                Integer totalTopScore = 0;
                if(Objects.equals(topScoreDefine,"scoreLevel5")){
                    totalTopScore = clazzLevelscoreLevel5+clazzLevelscoreLevel6;
                }else if(Objects.equals(topScoreDefine,"scoreLevel4")){
                    totalTopScore = clazzLevelscoreLevel4;
                }else if(Objects.equals(topScoreDefine,"scoreLevel3")){
                    totalTopScore = clazzLevelscoreLevel3;
                }else if(Objects.equals(topScoreDefine,"scoreLevel2")){
                    totalTopScore = clazzLevelscoreLevel2;
                }else{
                    totalTopScore = 0;
                }

                BigDecimal clazzLevelScoreRate = new BigDecimal(totalTopScore).multiply(new BigDecimal(100)).
                        divide(new BigDecimal(clazzLevelscoreLevel1+clazzLevelscoreLevel2+clazzLevelscoreLevel3+clazzLevelscoreLevel4+clazzLevelscoreLevel5+clazzLevelscoreLevel6),
                                2, RoundingMode.HALF_UP);
                clazzLevelAvgTopScoreLevelMap.put(clazzLevel,clazzLevelScoreRate);

                Map<Long,List<ActivityReportCollectData>> schoolDatasMap = clazzLevelSchoolDatas.stream().collect(Collectors.groupingBy(o -> o.getSchoolId()));
                List<Long> schoolIds = new ArrayList<>(schoolDatasMap.keySet());//按照区县ID排序
                Collections.sort(schoolIds, (o1, o2) -> o1.compareTo(o2));
                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for(int i=0; i<schoolIds.size(); i++){
                    Long tempSchoolId = schoolIds.get(i);
                    List<ActivityReportCollectData> schoolDataList = schoolDatasMap.get(tempSchoolId);
                    ActivityReportCollectData activityReportCollectData = schoolDataList.get(0);
                    //统计，每个区县，各分数段的人数
                    Integer scoreLevel1 = schoolDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds1).sum();
                    Integer scoreLevel2 = schoolDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds2).sum();
                    Integer scoreLevel3 = schoolDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds3).sum();
                    Integer scoreLevel4 = schoolDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds4).sum();
                    Integer scoreLevel5 = schoolDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds5).sum();
                    Integer scoreLevel6 = schoolDataList.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds6).sum();
                    Integer allScoreLevelStuds = scoreLevel1 + scoreLevel2 +scoreLevel3 + scoreLevel4 + scoreLevel5 + scoreLevel6;
                    //高分人数比例
                    Integer tempTopScoreTotal = 0;
                    if(Objects.equals(topScoreDefine,"scoreLevel5")){
                        tempTopScoreTotal = scoreLevel6+scoreLevel5;
                    }else if(Objects.equals(topScoreDefine,"scoreLevel4")){
                        tempTopScoreTotal = scoreLevel4;
                    }else if(Objects.equals(topScoreDefine,"scoreLevel3")){
                        tempTopScoreTotal = scoreLevel3;
                    }else if(Objects.equals(topScoreDefine,"scoreLevel2")){
                        tempTopScoreTotal = scoreLevel2;
                    }else{
                        tempTopScoreTotal = 0;
                    }
                    BigDecimal topScoreLevelRate = new BigDecimal(tempTopScoreTotal).multiply(new BigDecimal(100)).divide(new BigDecimal(allScoreLevelStuds),2,RoundingMode.HALF_UP);
                    diffSet.add(topScoreLevelRate);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(activityReportCollectData.getSchoolName());
                    tempData.add(scoreLevel1);
                    tempData.add(scoreLevel2);
                    tempData.add(scoreLevel3);
                    tempData.add(scoreLevel4);
                    tempData.add(scoreLevel5);
                    tempData.add(scoreLevel6);
                    gridDataList.add(tempData);
                    Map<String,Object> barMap = new LinkedHashMap<>();
                    barMap.put("no",count);
                    barMap.put("region",tempSchoolId);
                    barMap.put("regionName",activityReportCollectData.getSchoolName());
                    barMap.put("avgTimes",topScoreLevelRate);
                    barDatas.add(barMap);
                    count++;
                }
                diffSetMap.put(clazzLevel,diffSet);
                barBaseMap.put(clazzLevel,barDatas);
            }
        }else{
            gridHeadList = new String[]{"序号", "年级", "班级","0-9分","10-19分","20-29分","30-39分","40-49分","50分以上"};
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            int count = 1;
            while(clazzLevelIt.hasNext()) {
                List<Map<String, Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelData = clazzLevelMap.get(clazzLevel);

                Integer clazzLevelscoreLevel1 = clazzLevelData.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds1).sum();
                Integer clazzLevelscoreLevel2 = clazzLevelData.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds2).sum();
                Integer clazzLevelscoreLevel3 = clazzLevelData.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds3).sum();
                Integer clazzLevelscoreLevel4 = clazzLevelData.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds4).sum();
                Integer clazzLevelscoreLevel5 = clazzLevelData.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds5).sum();
                Integer clazzLevelscoreLevel6 = clazzLevelData.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds6).sum();
                Integer totalTopScore = 0;
                if(Objects.equals(topScoreDefine,"scoreLevel5")){
                    totalTopScore = clazzLevelscoreLevel5+clazzLevelscoreLevel6;
                }else if(Objects.equals(topScoreDefine,"scoreLevel4")){
                    totalTopScore = clazzLevelscoreLevel4;
                }else if(Objects.equals(topScoreDefine,"scoreLevel3")){
                    totalTopScore = clazzLevelscoreLevel3;
                }else if(Objects.equals(topScoreDefine,"scoreLevel2")){
                    totalTopScore = clazzLevelscoreLevel2;
                }else{
                    totalTopScore = 0;
                }
                BigDecimal clazzLevelScoreRate = new BigDecimal(totalTopScore).multiply(new BigDecimal(100)).
                        divide(new BigDecimal(clazzLevelscoreLevel1 + clazzLevelscoreLevel2 + clazzLevelscoreLevel3 + clazzLevelscoreLevel4 + clazzLevelscoreLevel5 + clazzLevelscoreLevel6),
                                2, RoundingMode.HALF_UP);
                ;
                clazzLevelAvgTopScoreLevelMap.put(clazzLevel, clazzLevelScoreRate);

                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for (int i = 0; i < clazzLevelData.size(); i++) {
                    ActivityReportCollectData temp = clazzLevelData.get(i);

                    Integer scoreLevel1 = temp.getScoreLevelStuds1();
                    Integer scoreLevel2 = temp.getScoreLevelStuds2();
                    Integer scoreLevel3 = temp.getScoreLevelStuds3();
                    Integer scoreLevel4 = temp.getScoreLevelStuds4();
                    Integer scoreLevel5 = temp.getScoreLevelStuds5();
                    Integer scoreLevel6 = temp.getScoreLevelStuds6();
                    Integer allScoreLevelStuds = scoreLevel1 + scoreLevel2 + scoreLevel3 + scoreLevel4 + scoreLevel5 + scoreLevel6;
                    //高分人数比例
                    Integer tempTopScoreTotal = 0;
                    if(Objects.equals(topScoreDefine,"scoreLevel5")){
                        tempTopScoreTotal = scoreLevel5+scoreLevel6;
                    }else if(Objects.equals(topScoreDefine,"scoreLevel4")){
                        tempTopScoreTotal = scoreLevel4;
                    }else if(Objects.equals(topScoreDefine,"scoreLevel3")){
                        tempTopScoreTotal = scoreLevel3;
                    }else if(Objects.equals(topScoreDefine,"scoreLevel2")){
                        tempTopScoreTotal = scoreLevel2;
                    }else{
                        tempTopScoreTotal = 0;
                    }
                    BigDecimal topScoreLevelRate = new BigDecimal(tempTopScoreTotal).divide(new BigDecimal(allScoreLevelStuds), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                    diffSet.add(topScoreLevelRate);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(temp.getClazzName());
                    tempData.add(scoreLevel1);
                    tempData.add(scoreLevel2);
                    tempData.add(scoreLevel3);
                    tempData.add(scoreLevel4);
                    tempData.add(scoreLevel5);
                    tempData.add(scoreLevel6);
                    gridDataList.add(tempData);
                    Map<String, Object> barMap = new LinkedHashMap<>();
                    barMap.put("no", count);
                    barMap.put("region", temp.getClazzId());
                    barMap.put("regionName",temp.getClazzName());
                    barMap.put("avgTimes", topScoreLevelRate);
                    barDatas.add(barMap);
                    count++;
                }
                diffSetMap.put(clazzLevel, diffSet);
                barBaseMap.put(clazzLevel, barDatas);
            }
        }
        gridDataMap.put("gridHead",gridHeadList);
        gridDataMap.put("gridData",gridDataList);
        dataMap.put("grid",gridDataMap);

        //根据表格的数据拼接柱图，按年级去区分数据
        List<Map<String,Object>> viewBarList = new LinkedList<>();
        Iterator<Integer> clazzLevels = barBaseMap.keySet().iterator();
        while (clazzLevels.hasNext()){
            Integer clazzLevel = clazzLevels.next();
            Map<String,Object> viewBar = new LinkedHashMap<>();
            //是否展示图表,最高分是多少的标志
            if("scoreLevel1".equals(topScoreDefine)){
                //bar没有数据
                viewBar.put("isHaveData","暂无数据");
                viewBar.put("regions",2);
                viewBar.put("message",ClazzLevel.getDescription(clazzLevel)+"，学生得分均在0~9分之间，无人得分为10分以上");
            }else{
                List<Map<String,Object>> barDatas = barBaseMap.get(clazzLevel);
                List<Object> xAxisData = new LinkedList<>();
                List<Object> seriesData = new LinkedList<>();
                Set<BigDecimal> allZeroScore = new HashSet<>();//所有分数为零分的判断标志
                for(Map<String,Object> temp : barDatas){
                    BigDecimal avgTimes = (BigDecimal) temp.get("avgTimes");
                    seriesData.add(avgTimes);
                    Map<String,Object> xData = new LinkedHashMap<>();
                    xData.put("no",temp.get("no"));
                    xData.put("name",temp.get("regionName"));
                    xAxisData.add(xData);
                    allZeroScore.add(avgTimes);
                }
                //校验所有数据为0的时候，也是暂无数据的情况，（实际情况是，某个年级的分数都没有整体最高分定义的级别的分数，
                //所以高分比例都为0了。

                if(allZeroScore.size()==1 && allZeroScore.iterator().next().compareTo(BigDecimal.ZERO) == 0){
                    //bar没有数据
                    viewBar.put("isHaveData","暂无数据");
                    viewBar.put("regions",2);
                    String noDataScoreLevel = getScoreDefineMsg2(topScoreDefine);
                    viewBar.put("message",ClazzLevel.getDescription(clazzLevel)+"，学生得分均在"+noDataScoreLevel+"分以下，无人得分为"+noDataScoreLevel+"分以上");
                }else{
                    String topScoreDefineMsg = getScoreDefineMsg(topScoreDefine);

                    Set<BigDecimal> diffSet = diffSetMap.get(clazzLevel);
                    BigDecimal clazzLevelScoreRate = clazzLevelAvgTopScoreLevelMap.get(clazzLevel);

                    List<String> legendData = new LinkedList<>();
                    legendData.add("整体");
                    if("city".equals(regionLevel)){
                        viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各区县-高分人数占比图");
                        viewBar.put("viewRegion","区/县");
                        legendData.add("各区域");
                    }else if("county".equals(regionLevel)){
                        viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各学校-高分人数占比图");
                        viewBar.put("viewRegion","学校");
                        legendData.add("各学校");
                    }else{
                        viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各班级-高分人数占比图");
                        viewBar.put("viewRegion","班级");
                        legendData.add("各班级");
                    }
                    //横轴 ，区域，学校，班级
                    viewBar.put("legendData",legendData);
                    viewBar.put("xAxisData",xAxisData);
                    viewBar.put("seriesData",seriesData);
                    viewBar.put("legendData",legendData);
                    viewBar.put("clazzLevel",ClazzLevel.getDescription(clazzLevel));
                    viewBar.put("regions",barDatas.size());
                    viewBar.put("unit","%");
                    viewBar.put("wholeAvgNums",clazzLevelScoreRate);
                    viewBar.put("topScoreDefineMsg",topScoreDefineMsg);
                    //计算topThree
                    getTopTree(viewBar,barDatas,diffSet,clazzLevelScoreRate);
                    List<Map<String,Object>> topThree = (List<Map<String, Object>>) viewBar.get("topThree");
                    Map<String,Object> lastOne = (Map<String, Object>) viewBar.get("lastOne");
                    if(CollectionUtils.isNotEmpty(topThree)){
                        //拼接topThree
                        editTopThree(viewBar,topThree,"%");
                    }
                    if(MapUtils.isNotEmpty(lastOne)){
                        editLastOne(viewBar,lastOne,"%");
                    }
                }
            }
            viewBarList.add(viewBar);
        }
        dataMap.put("viewBarList",viewBarList);

        return dataMap;
    }

    private String getScoreDefineMsg(String isViewScoreLevel) {
        String topScoreDefineMsg = "";
        if("scoreLevel5".equals(isViewScoreLevel)){
            topScoreDefineMsg = "40分以上为高分";
        }else if("scoreLevel4".equals(isViewScoreLevel)){
            topScoreDefineMsg = "30分以上为高分";
        }else if("scoreLevel3".equals(isViewScoreLevel)){
            topScoreDefineMsg = "20分以上为高分";
        }else{
            topScoreDefineMsg = "10分以上为高分";
        }
        return topScoreDefineMsg;
    }

    private String getScoreDefineMsg2(String isViewScoreLevel) {
        String topScoreDefineMsg = "";
        if("scoreLevel5".equals(isViewScoreLevel)){
            topScoreDefineMsg = "40";
        }else if("scoreLevel4".equals(isViewScoreLevel)){
            topScoreDefineMsg = "30";
        }else if("scoreLevel3".equals(isViewScoreLevel)){
            topScoreDefineMsg = "20";
        }else{
            topScoreDefineMsg = "10";
        }
        return topScoreDefineMsg;
    }

    private void getScoreLevelCollectData(Map<String, Object> dataMap, List<ActivityReportCollectData> collectDatas) {
        //各个分数级别的数据 0-9分的人数，所占整体比例
        Integer scoreLevel1Studs = collectDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds1).sum();
        Integer scoreLevel2Studs = collectDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds2).sum();
        Integer scoreLevel3Studs = collectDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds3).sum();
        Integer scoreLevel4Studs = collectDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds4).sum();
        Integer scoreLevel5Studs = collectDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds5).sum();
        Integer scoreLevel6Studs = collectDatas.stream().mapToInt(ActivityReportCollectData::getScoreLevelStuds6).sum();
        Integer totalStuds = scoreLevel1Studs + scoreLevel2Studs +scoreLevel3Studs + scoreLevel4Studs +scoreLevel5Studs + scoreLevel6Studs;
        BigDecimal scoreLevel1Rate = new BigDecimal(scoreLevel1Studs).multiply(new BigDecimal(100)).divide(new BigDecimal(totalStuds),2, RoundingMode.HALF_UP);
        BigDecimal scoreLevel2Rate = new BigDecimal(scoreLevel2Studs).multiply(new BigDecimal(100)).divide(new BigDecimal(totalStuds),2,RoundingMode.HALF_UP);
        BigDecimal scoreLevel3Rate = new BigDecimal(scoreLevel3Studs).multiply(new BigDecimal(100)).divide(new BigDecimal(totalStuds),2,RoundingMode.HALF_UP);
        BigDecimal scoreLevel4Rate = new BigDecimal(scoreLevel4Studs).multiply(new BigDecimal(100)).divide(new BigDecimal(totalStuds),2,RoundingMode.HALF_UP);
        BigDecimal scoreLevel5Rate = new BigDecimal(scoreLevel5Studs).multiply(new BigDecimal(100)).divide(new BigDecimal(totalStuds),2,RoundingMode.HALF_UP);
        BigDecimal scoreLevel6Rate = new BigDecimal(scoreLevel6Studs).multiply(new BigDecimal(100)).divide(new BigDecimal(totalStuds),2,RoundingMode.HALF_UP);
        Map<String,Object> wholeGrid = new LinkedHashMap<>();
        String[] gridHead = new String[]{"分数","人数","比例"};
        List<Object[]> gridData = new LinkedList<>();
        Object[] row1 = new Object[]{"0-9分",scoreLevel1Studs,scoreLevel1Rate.toString()+"%"};
        Object[] row2 = new Object[]{"10-19分",scoreLevel2Studs,scoreLevel2Rate.toString()+"%"};
        Object[] row3 = new Object[]{"20-29分",scoreLevel3Studs,scoreLevel3Rate.toString()+"%"};
        Object[] row4 = new Object[]{"30-39分",scoreLevel4Studs,scoreLevel4Rate.toString()+"%"};
        Object[] row5 = new Object[]{"40-49分",scoreLevel5Studs,scoreLevel5Rate.toString()+"%"};
        Object[] row6 = new Object[]{"50分以上",scoreLevel6Studs,scoreLevel6Rate.toString()+"%"};
        gridData.add(row1);
        gridData.add(row2);
        gridData.add(row3);
        gridData.add(row4);
        gridData.add(row5);
        gridData.add(row6);
        List<Map<String,Object>> wholeMapList = new LinkedList<>();
        for(int i=0; i<gridData.size(); i++){
            Object[] obj = gridData.get(i);
            Map<String,Object> temp = new LinkedHashMap<>();
            temp.put("name",obj[0]);
            temp.put("value", SafeConverter.toString(obj[2]).replace("%",""));
            wholeMapList.add(temp);
        }
        Collections.sort(wholeMapList, (o1, o2) -> {
            BigDecimal data1 = new BigDecimal(o1.get("value").toString());
            BigDecimal data2 = new BigDecimal(o2.get("value").toString());
            return data2.compareTo(data1);
        });

        wholeGrid.put("gridHead",gridHead);
        wholeGrid.put("gridData",gridData);
        wholeGrid.put("topOne",wholeMapList.get(0).get("name"));
        wholeGrid.put("lastOne",wholeMapList.get(wholeMapList.size()-1).get("name"));
        dataMap.put("wholeGrid",wholeGrid);
        //饼图数据数据
        Map<String,Object> pieMap = new LinkedHashMap<>();
        String[] pieLegendData = new String[]{"0-9分","10-19分","20-29分","30-39分","40-49分","50分以上"};
        pieMap.put("pieLegendData",pieLegendData);
        pieMap.put("pieSeriesData",wholeMapList);
        dataMap.put("pieMap",pieMap);
    }

    @Override
    public Map<String, Object> loadActivityAnswerSpeed(String regionLevel, String regionCode, String id) {
        Map<String,Object> dataMap = new LinkedHashMap<>();
        List<ActivityReportCollectData> collectDatas = activityReportCollectDataPersistence.loadActivityReportCollectDatasByRegionCode(regionLevel,regionCode,id);
        ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(id);
        List<Integer> configClazzLevels = activityConfig.getClazzLevels();
        //总体表格数据
        Map<String,Object> wholeGrid = new LinkedHashMap<>();
        String[] gridHead = new String[]{"年级","每分钟答对题目数量"};
        List<List<Object>> gridData = new LinkedList<>();
        Map<Integer,List<ActivityReportCollectData>> clazzLevelMaps = collectDatas.stream().filter(o->configClazzLevels.contains(o.getClazzLevel())).collect(Collectors.groupingBy(ActivityReportCollectData::getClazzLevel));
        List<Integer> clazzLevels = new ArrayList<>(clazzLevelMaps.keySet());
        Collections.sort(clazzLevels, (o1, o2) -> o2.compareTo(o1));
        //每个年级的总时间/总题目数
        List<Map<String,Object>> clazzLevelSpeedList = new LinkedList<>();
        Map<Integer,BigDecimal> clazzLevelSpeedMap = new LinkedHashMap<>();
        List<Object> wholexAxisData = new LinkedList<>();
        List<Object> wholeSeriesData = new LinkedList<>();
        for(int i=0; i<clazzLevels.size(); i++){
            List<Object> row = new LinkedList<>();
            Integer temp = clazzLevels.get(i);
            List<ActivityReportCollectData> clazzLeveltoDatas = clazzLevelMaps.get(temp);
            Integer sumClazzTakeTimes = clazzLeveltoDatas.stream().mapToInt(ActivityReportCollectData::getClazzTakeTimes).sum();
            Integer sumClazzExercises = clazzLeveltoDatas.stream().mapToInt(ActivityReportCollectData::getClazzExercises).sum();
            //总题目数/总时间,各年级的答题速度
            BigDecimal clazzLevelSpeed = BigDecimal.ZERO;
            if(!Objects.equals(sumClazzExercises,0)){
                clazzLevelSpeed = new BigDecimal(sumClazzExercises).multiply(new BigDecimal(60)).divide(new BigDecimal(sumClazzTakeTimes),2, RoundingMode.HALF_UP);
            }
            row.add(ClazzLevel.getDescription(temp));
            row.add(clazzLevelSpeed);
            gridData.add(row);
            Map<String,Object> tempMap = new LinkedHashMap<>();
            tempMap.put("clazzLevel",ClazzLevel.getDescription(temp));
            tempMap.put("clazzLevelSpeed",clazzLevelSpeed);
            clazzLevelSpeedList.add(tempMap);
            wholexAxisData.add(ClazzLevel.getDescription(temp));
            wholeSeriesData.add(clazzLevelSpeed);
            clazzLevelSpeedMap.put(temp,clazzLevelSpeed);
        }
        //整体答题速度
        Integer wholeTakeTimes = collectDatas.stream().mapToInt(ActivityReportCollectData::getClazzTakeTimes).sum();
        Integer wholeExercise = collectDatas.stream().mapToInt(ActivityReportCollectData::getClazzExercises).sum();
        BigDecimal wholeSpeed = BigDecimal.ZERO;
        if(!Objects.equals(wholeTakeTimes,0)){
            wholeSpeed = new BigDecimal(wholeExercise).multiply(new BigDecimal(60)).divide(new BigDecimal(wholeTakeTimes),2, RoundingMode.HALF_UP);
        }
        List<Object> row = new LinkedList<>();
        row.add("整体");
        row.add(wholeSpeed);
        gridData.add(row);
        wholeGrid.put("gridHead",gridHead);
        wholeGrid.put("gridData",gridData);
        Collections.sort(clazzLevelSpeedList, (o1, o2) -> {
            BigDecimal clazzLevelSpeed1 = (BigDecimal) o1.get("clazzLevelSpeed");
            BigDecimal clazzLevelSpeed2 = (BigDecimal) o2.get("clazzLevelSpeed");
            return clazzLevelSpeed2.compareTo(clazzLevelSpeed1);
        });
        Map<String,Object> topOneData = clazzLevelSpeedList.get(0);
        topOneData.put("topOneName",topOneData.get("clazzLevel"));
        BigDecimal topClazzLevelSpeed = (BigDecimal) topOneData.get("clazzLevelSpeed");
        topOneData.put("topOneVal",topClazzLevelSpeed.toString()+"题/分钟");
        topOneData.put("topDiff",topClazzLevelSpeed.subtract(wholeSpeed)+"题/分钟");
        wholeGrid.put("topOneData",topOneData);
        Map<String,Object> lastOneData = clazzLevelSpeedList.get(clazzLevelSpeedList.size()-1);
        BigDecimal lastClazzLevelSpeed = (BigDecimal) lastOneData.get("clazzLevelSpeed");
        lastOneData.put("lastOneName",lastOneData.get("clazzLevel"));
        lastOneData.put("lastOneVal",lastClazzLevelSpeed.toString()+"题/分钟");
        lastOneData.put("lastDiff",wholeSpeed.subtract(lastClazzLevelSpeed)+"题/分钟");
        wholeGrid.put("lastOneData",lastOneData);
        dataMap.put("wholeGrid",wholeGrid);
        //整体数据的柱图
        Map<String,Object> wholeBarMap = new LinkedHashMap<>();
        wholeBarMap.put("title","答题速度");
        String[] wholeLengedData = new String[]{"年级","整体"};
        wholeBarMap.put("xAxisData",wholexAxisData);
        wholeBarMap.put("legendData",wholeLengedData);
        wholeBarMap.put("seriesData",wholeSeriesData);
        wholeBarMap.put("wholeAnswerSpeed",wholeSpeed);
        dataMap.put("wholeBarMap",wholeBarMap);

        //表格数据
        Map<String,Object> gridDataMap = new LinkedHashMap<>();
        String[] gridHeadList = null;
        List<List<Object>> gridDataList = new LinkedList<>();
        //图表基础数据
        Map<Integer,List<Map<String,Object>>> barBaseMap = new LinkedHashMap<>();      //年级-- 数据  ,组装一下的柱图使用
        Map<Integer,Set<BigDecimal>> diffSetMap = new LinkedHashMap<>();

        Map<Integer,List<ActivityReportCollectData>> clazzLevelMapTemp = collectDatas.stream().collect(Collectors.groupingBy(o -> o.getClazzLevel()));
        Map<Integer,List<ActivityReportCollectData>> clazzLevelMap = new LinkedHashMap<>();
        for(Integer temp : configClazzLevels){
            List<ActivityReportCollectData> data = clazzLevelMapTemp.get(temp);
            if(CollectionUtils.isNotEmpty(data)){
                clazzLevelMap.put(temp,data);
            }
        }

        if("city".equals(regionLevel)) {
            gridHeadList = new String[]{"序号", "年级", "区/县","答题速度"};
            int count = 1;
            //按年级分组
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            while(clazzLevelIt.hasNext()){
                List<Map<String,Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelRegionDatas = clazzLevelMap.get(clazzLevel);

                Map<Integer,List<ActivityReportCollectData>> regionDatasMap = clazzLevelRegionDatas.stream().collect(Collectors.groupingBy(o -> o.getRegionCode()));
                List<Integer> tempRegions = new ArrayList<>(regionDatasMap.keySet());//按照区县ID排序
                Collections.sort(tempRegions, (o1, o2) -> o1.compareTo(o2));
                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for(int i=0; i<tempRegions.size(); i++){
                    Integer tempRegionCode = tempRegions.get(i);
                    List<ActivityReportCollectData> regionDataList = regionDatasMap.get(tempRegionCode);
                    ActivityReportCollectData activityReportCollectData = regionDataList.get(0);
                    //统计，每个区县，答题速度
                    Integer regionExercises = regionDataList.stream().mapToInt(ActivityReportCollectData::getClazzExercises).sum();
                    Integer regionTakeTimes = regionDataList.stream().mapToInt(ActivityReportCollectData::getClazzTakeTimes).sum();
                    //答题速度
                    BigDecimal regionSpeed = BigDecimal.ZERO;
                    if(!Objects.equals(regionTakeTimes,0)){
                        regionSpeed = new BigDecimal(regionExercises).multiply(new BigDecimal(60)).divide(new BigDecimal(regionTakeTimes),2,RoundingMode.HALF_UP);;
                    }
                    diffSet.add(regionSpeed);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(activityReportCollectData.getRegionName());
                    tempData.add(regionSpeed + "题/分钟");
                    gridDataList.add(tempData);
                    Map<String,Object> barMap = new LinkedHashMap<>();
                    barMap.put("no",count);
                    barMap.put("region",tempRegionCode);
                    barMap.put("regionName",activityReportCollectData.getRegionName());
                    barMap.put("avgTimes",regionSpeed);
                    barDatas.add(barMap);
                    count++;
                }
                diffSetMap.put(clazzLevel,diffSet);
                barBaseMap.put(clazzLevel,barDatas);
            }
        }else if("county".equals(regionLevel)){
            gridHeadList = new String[]{"序号", "年级", "学校","答题速度"};
            int count = 1;
            //按年级分组
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            while(clazzLevelIt.hasNext()){
                List<Map<String,Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelSchoolDatas = clazzLevelMap.get(clazzLevel);
                Map<Long,List<ActivityReportCollectData>> schoolDatasMap = clazzLevelSchoolDatas.stream().collect(Collectors.groupingBy(o -> o.getSchoolId()));
                List<Long> schoolIds = new ArrayList<>(schoolDatasMap.keySet());//按照区县ID排序
                Collections.sort(schoolIds, (o1, o2) -> o1.compareTo(o2));
                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for(int i=0; i<schoolIds.size(); i++){
                    Long tempSchoolId = schoolIds.get(i);
                    List<ActivityReportCollectData> schoolDataList = schoolDatasMap.get(tempSchoolId);
                    ActivityReportCollectData activityReportCollectData = schoolDataList.get(0);
                    //统计，每个学校，答题速度
                    Integer schoolExercises = schoolDataList.stream().mapToInt(ActivityReportCollectData::getClazzExercises).sum();
                    Integer schoolTakeTimes = schoolDataList.stream().mapToInt(ActivityReportCollectData::getClazzTakeTimes).sum();
                    //答题速度
                    BigDecimal schoolSpeed = BigDecimal.ZERO;
                    if(!Objects.equals(schoolTakeTimes,0)){
                        schoolSpeed = new BigDecimal(schoolExercises).multiply(new BigDecimal(60)).divide(new BigDecimal(schoolTakeTimes),2,RoundingMode.HALF_UP);
                    }
                    diffSet.add(schoolSpeed);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(activityReportCollectData.getSchoolName());
                    tempData.add(schoolSpeed + "题/分钟");

                    gridDataList.add(tempData);
                    Map<String,Object> barMap = new LinkedHashMap<>();
                    barMap.put("no",count);
                    barMap.put("region",tempSchoolId);
                    barMap.put("regionName",activityReportCollectData.getSchoolName());
                    barMap.put("avgTimes",schoolSpeed);
                    barDatas.add(barMap);
                    count++;
                }
                diffSetMap.put(clazzLevel,diffSet);
                barBaseMap.put(clazzLevel,barDatas);
            }
        }else{
            gridHeadList = new String[]{"序号", "年级", "班级","答题速度"};
            Iterator<Integer> clazzLevelIt = clazzLevelMap.keySet().iterator();
            int count = 1;
            while(clazzLevelIt.hasNext()) {
                List<Map<String, Object>> barDatas = new LinkedList<>();
                Integer clazzLevel = clazzLevelIt.next();
                List<ActivityReportCollectData> clazzLevelData = clazzLevelMap.get(clazzLevel);

                Set<BigDecimal> diffSet = new LinkedHashSet<>();
                for (int i = 0; i < clazzLevelData.size(); i++) {
                    ActivityReportCollectData temp = clazzLevelData.get(i);
                    //统计，每个班级，答题速度
                    Integer clazzExercises = temp.getClazzExercises();
                    Integer clazzTakeTimes = temp.getClazzTakeTimes();
                    //答题速度
                    BigDecimal clazzSpeed = BigDecimal.ZERO;
                    if(!Objects.equals(clazzTakeTimes,0)){
                        clazzSpeed = new BigDecimal(clazzExercises).multiply(new BigDecimal(60)).divide(new BigDecimal(clazzTakeTimes),2,RoundingMode.HALF_UP);
                    }
                    diffSet.add(clazzSpeed);
                    List<Object> tempData = new LinkedList<>();
                    tempData.add(count);
                    tempData.add(ClazzLevel.getDescription(clazzLevel));
                    tempData.add(temp.getClazzName());
                    tempData.add(clazzSpeed+"题/分钟");
                    gridDataList.add(tempData);
                    Map<String, Object> barMap = new LinkedHashMap<>();
                    barMap.put("no", count);
                    barMap.put("region", temp.getClazzId());
                    barMap.put("regionName",temp.getClazzName());
                    barMap.put("avgTimes", clazzSpeed);
                    barDatas.add(barMap);
                    count++;
                }
                diffSetMap.put(clazzLevel, diffSet);
                barBaseMap.put(clazzLevel, barDatas);
            }
        }
        gridDataMap.put("gridHead",gridHeadList);
        gridDataMap.put("gridData",gridDataList);
        dataMap.put("grid",gridDataMap);
        //图表数据
        //根据表格的数据拼接柱图，按年级去区分数据
        List<Map<String,Object>> viewBarList = new LinkedList<>();
        Iterator<Integer> barClazzLevels = barBaseMap.keySet().iterator();
        while (barClazzLevels.hasNext()){
            Integer clazzLevel = barClazzLevels.next();
            Map<String,Object> viewBar = new LinkedHashMap<>();
            List<Map<String,Object>> barDatas = barBaseMap.get(clazzLevel);
            Set<BigDecimal> diffSet = diffSetMap.get(clazzLevel);
            BigDecimal clazzLevelSpeed = clazzLevelSpeedMap.get(clazzLevel);

            List<String> legendData = new LinkedList<>();
            legendData.add("整体");
            if("city".equals(regionLevel)){
                viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各区县-答题速度");
                viewBar.put("viewRegion","区/县");
                legendData.add("各区域");
            }else if("county".equals(regionLevel)){
                viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各学校-答题速度");
                viewBar.put("viewRegion","学校");
                legendData.add("各学校");
            }else{
                viewBar.put("title",ClazzLevel.getDescription(clazzLevel)+"各班级-答题速度");
                viewBar.put("viewRegion","班级");
                legendData.add("各班级");
            }
            //横轴 ，区域，学校，班级
            viewBar.put("legendData",legendData);
            List<Object> xAxisData = new LinkedList<>();
            List<Object> seriesData = new LinkedList<>();
            for(Map<String,Object> temp : barDatas){
                seriesData.add(temp.get("avgTimes"));
                Map<String,Object> xData = new LinkedHashMap<>();
                xData.put("no",temp.get("no"));
                xData.put("name",temp.get("regionName"));
                xAxisData.add(xData);
            }
            viewBar.put("xAxisData",xAxisData);
            viewBar.put("seriesData",seriesData);
            viewBar.put("legendData",legendData);
            viewBar.put("regions",barDatas.size());
            viewBar.put("clazzLevel",ClazzLevel.getDescription(clazzLevel));
            viewBar.put("unit","题/分钟");
            viewBar.put("wholeAvgNums",clazzLevelSpeed);
            //计算topThree
            getTopTree(viewBar,barDatas,diffSet,clazzLevelSpeed);
            List<Map<String,Object>> topThree = (List<Map<String, Object>>) viewBar.get("topThree");
            Map<String,Object> lastOne = (Map<String, Object>) viewBar.get("lastOne");
            if(CollectionUtils.isNotEmpty(topThree)){
                //拼接topThree
                editTopThree(viewBar,topThree,"题/分钟");
            }
            if(MapUtils.isNotEmpty(lastOne)){
                editLastOne(viewBar,lastOne,"题/分钟");
            }

            viewBarList.add(viewBar);
        }
        dataMap.put("viewBarList",viewBarList);
        return dataMap;
    }

    private void getTopTree(Map<String, Object> viewBar, List<Map<String, Object>> sortDataList, Set<BigDecimal> diffSet, BigDecimal markLineData) {
        //针对sortDataList排序
        Collections.sort(sortDataList, (o1, o2) -> {
            BigDecimal avgTimes1 = (BigDecimal) o1.get("avgTimes");
            BigDecimal avgTimes2 = (BigDecimal) o2.get("avgTimes");
            return avgTimes2.compareTo(avgTimes1);
        });
        //长度大于等于2，并且数据有差异则有如下逻辑
        if(sortDataList.size() >=2 && diffSet.size()>1){
            if(sortDataList.size() == 2){
                List<Map<String,Object>> tempList = new LinkedList<Map<String,Object>>();
                tempList.add(sortDataList.get(0));
                viewBar.put("topThree",tempList);
                Map<String,Object> lastOne = sortDataList.get(1);
                viewBar.put("lastOne",lastOne);
                BigDecimal scorerate = (BigDecimal) lastOne.get("avgTimes");
                BigDecimal diff = markLineData.subtract(scorerate);
                viewBar.put("diff",diff);
            }else if(sortDataList.size() == 3){
                Map<String,Object> second = sortDataList.get(1);
                Map<String,Object> third = sortDataList.get(2);
                //第二三名进行比较相等则只展示最好的第一名，
                BigDecimal  secondScorerate = (BigDecimal) second.get("avgTimes");
                BigDecimal  thirdScorerate = (BigDecimal) third.get("avgTimes");
                if(Objects.equals(secondScorerate,thirdScorerate)){
                    List<Map<String,Object>> tempList = new LinkedList<Map<String,Object>>();
                    tempList.add(sortDataList.get(0));
                    viewBar.put("topThree",tempList);
                    viewBar.put("lastOne",new LinkedHashMap<>());
                }else{
                    viewBar.put("topThree",sortDataList.subList(0,2));
                    Map<String,Object> lastOne = sortDataList.get(sortDataList.size()-1);
                    viewBar.put("lastOne",lastOne);
                    BigDecimal scorerate = (BigDecimal) lastOne.get("avgTimes");
                    BigDecimal diff = markLineData.subtract(scorerate);
                    viewBar.put("diff",diff);
                }
            }else if(sortDataList.size()>=4){
                //第二名和最后一名比较，如果相等，只需要取最好的去第一个，最后一名不要
                Map<String,Object> second = sortDataList.get(1);
                BigDecimal secondScorerate = (BigDecimal) second.get("avgTimes");
                Map<String,Object> third = sortDataList.get(2);
                BigDecimal thirdScorerate = (BigDecimal) third.get("avgTimes");
                Map<String,Object> last = sortDataList.get(sortDataList.size()-1);
                BigDecimal lastScorerate = (BigDecimal) last.get("avgTimes");
                if(Objects.equals(secondScorerate,lastScorerate)){
                    List<Map<String,Object>> tempList = new LinkedList<Map<String,Object>>();
                    tempList.add(sortDataList.get(0));
                    viewBar.put("topThree",tempList);
                    viewBar.put("lastOne",new LinkedHashMap<>());
                }else if(Objects.equals(thirdScorerate,lastScorerate)){
                    //第三名和最后一名比较，如果相等则最好的取前两个，最后一名不取
                    viewBar.put("topThree",sortDataList.subList(0,2));
                    viewBar.put("lastOne",new LinkedHashMap<>());
                }else{
                    viewBar.put("topThree",sortDataList.subList(0,3));
                    Map<String,Object> lastOne = sortDataList.get(sortDataList.size()-1);
                    viewBar.put("lastOne",lastOne);
                    BigDecimal scorerate = (BigDecimal) lastOne.get("avgTimes");
                    BigDecimal diff = markLineData.subtract(scorerate);
                    viewBar.put("diff",diff);
                }
            }
        }else{
            viewBar.put("topThree",new LinkedList<>());
            viewBar.put("lastOne",new LinkedHashMap<>());
        }
    }
}
