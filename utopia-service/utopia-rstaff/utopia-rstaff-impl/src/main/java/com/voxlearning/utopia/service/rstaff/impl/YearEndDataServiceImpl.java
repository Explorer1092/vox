package com.voxlearning.utopia.service.rstaff.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.LoadYearDataService;
import com.voxlearning.utopia.service.rstaff.api.service.YearEndDataService;
import lombok.Getter;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

@Named
@Service(interfaceClass = YearEndDataService.class)
@ExposeService(interfaceClass = YearEndDataService.class)
public class YearEndDataServiceImpl implements YearEndDataService {

    @Getter
    @ImportService(interfaceClass = LoadYearDataService.class)
    private LoadYearDataService loadYearDataService;

    @Override
    public Map<String, Object> loadTeacherYearData(Long teacherId) {
//        MapMessage map = loadYearDataService.loadTeacherYearData(14670895L);
        MapMessage map = loadYearDataService.loadTeacherYearData(teacherId);
        if(map.isSuccess()) {
            Map<String, Object> dataMap = (Map<String, Object>) map.get("dataMap");
            Iterator<String> it = dataMap.keySet().iterator();
            while(it.hasNext()){
                String key = it.next();
                Object val = dataMap.get(key);
                if(val instanceof String){
                    if("NULL".equals(val)){
                        dataMap.put(key,null);
                    }
                }
            }
            List<Map<String,String>> assignHomeworkTimesPieData = (List<Map<String, String>>) dataMap.get("assignHomeworkTimesPieData");
            List<String> legendDatas = new LinkedList<>();
            List<Integer> assignTimes = new LinkedList<>();
            List<String> homeworkTypes = new ArrayList<>();//作业类型列表
            int count = 0;
            if(CollectionUtils.isNotEmpty(assignHomeworkTimesPieData)){
                for(Map<String,String> temp : assignHomeworkTimesPieData){
                    String name = temp.get("name");
                    String value = temp.get("value");
                    legendDatas.add(name);
                    assignTimes.add(new BigDecimal(value).intValue());
                    if(count < 3){
                        homeworkTypes.add(name);
                    }
                    count++;
                }
            }
            //作业类型
            dataMap.put("homeworkTypes",homeworkTypes);
            return dataMap;
        }else{
            return null;
        }
    }

    @Override
    public Map<String, Object> loadStudentYearData(Long studentId) {
//        MapMessage map = loadYearDataService.loadStudentYearData(3100158612L);
        MapMessage map = loadYearDataService.loadStudentYearData(studentId);
        if(map.isSuccess()){
            Map<String,Object> dataMap = (Map<String, Object>) map.get("dataMap");
            Iterator<String> it = dataMap.keySet().iterator();
            while(it.hasNext()){
                String key = it.next();
                Object val = dataMap.get(key);
                if(val instanceof String){
                    if("NULL".equals(val)){
                        dataMap.put(key,null);
                    }
                }
            }
            List<Map<String,Object>> doHomeworkTimeLineData = (List<Map<String, Object>>) dataMap.get("doHomeworkTimeLineData");
            Map<String,List<Object>> lineData = new LinkedHashMap<>();
            List<Object> times = new ArrayList<>();
            List<Object> days = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(doHomeworkTimeLineData)){
                for(Map<String,Object> temp : doHomeworkTimeLineData){
                    String time = SafeConverter.toString(temp.get("name"));
                    String day = SafeConverter.toString(temp.get("value"));
                    times.add(time);
                    days.add(day);
                }
            }
            lineData.put("xAxisData",times);
            lineData.put("seriesData",days);
            dataMap.put("doHomeworkTimeLineData",lineData);
            String transcendPercent = (String) dataMap.get("transcendPercent");
            Double transcend = SafeConverter.toDouble(transcendPercent);
            String studentTitle = "";
            if(transcend > 90){
                studentTitle = "学习模范";
            }else if(transcend > 70 && transcend < 90){
                studentTitle = "学习达人";
            }else if(transcend > 50 && transcend < 70){
                studentTitle = "学习小能手";
            }else if(transcend > 30 && transcend < 50){
                studentTitle = "学习积极分子";
            }else{
                studentTitle = "学习潜力股";
            }
            dataMap.put("studentTitle",studentTitle);

            List<Map<String,String>> homeworkSurveys = (List<Map<String, String>>) dataMap.get("homeworkSurvey");
            List<String> homeworkSurveyList = new ArrayList<>();
            List<String> homeworkSurveyBackupList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(homeworkSurveys)){
                for(Map<String,String> temp : homeworkSurveys){
                    String name = temp.get("name");
                    String value = temp.get("value");
                    String homeworkSurvey = "";
                    String homeworkSurveyBackup = "";
                    if("英语绘本".equals(name)){
                        homeworkSurvey = "你读了<span class=\"num\">"+value+"</span>本英语绘本";
                        homeworkSurveyBackup = value+"本英语绘本";
                    }else if("数学视频".equals(name)){
                        homeworkSurvey = "学了<span class=\"num\">"+value+"</span>节数学视频";
                        homeworkSurveyBackup = value+"节数学视频";
                    }else if("语文篇章".equals(name)){
                        homeworkSurvey = "搞定<span class=\"num\">"+value+"</span>篇语文篇章";
                        homeworkSurveyBackup = value+"篇语文篇章";
                    }
                    if(StringUtils.isNotBlank(homeworkSurvey)){
                        homeworkSurveyList.add(homeworkSurvey);
                    }
                    if(StringUtils.isNotBlank(homeworkSurveyBackup)){
                        homeworkSurveyBackupList.add(homeworkSurveyBackup);
                    }
                }
            }
            dataMap.put("homeworkSurveyBackup",homeworkSurveyBackupList);
            dataMap.put("homeworkSurvey",homeworkSurveyList);

            List<String> topRightKnowledgePoints = (List<String>) dataMap.get("topRightKnowledgePoints");
            if(CollectionUtils.isNotEmpty(topRightKnowledgePoints)){
                if(topRightKnowledgePoints.size() == 1){
                    String firstKnowledge = topRightKnowledgePoints.get(0);
                    if(StringUtils.isBlank(firstKnowledge)){
                        topRightKnowledgePoints = new ArrayList<>();
                        dataMap.put("topRightKnowledgePoints",topRightKnowledgePoints);
                    }
                }
            }
            return dataMap;
        }else{
            return null;
        }
    }
}
