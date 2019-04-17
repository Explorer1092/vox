package com.voxlearning.utopia.service.rstaff.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.athena.api.LoadPrdMockDataService;
import com.voxlearning.utopia.service.rstaff.api.MockDataService;
import lombok.Getter;
import util.CoefficientClient;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;


/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-08-07 14:30
 */

@Named
@Service(interfaceClass = MockDataService.class)
@ExposeService(interfaceClass = MockDataService.class)
public class MockDataServiceImpl extends SpringContainerSupport implements MockDataService {

    @Getter
    @ImportService(interfaceClass = LoadPrdMockDataService.class)
    private LoadPrdMockDataService loadPrdMockDataService;


    @Override
    public Map<String, Object> loadExamSurvey(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        MapMessage mapMessage = loadPrdMockDataService.loadExamSurvey(cityCode,regionCode,schoolId,examId);
        boolean isSuccess = (boolean) mapMessage.get("success");
        Map<String,Object> dataMap = (Map<String, Object>) mapMessage.get("dataMap");
        if(isSuccess){
            Map<String,Object> data = new LinkedHashMap<>();
            String examSurvey = (String) dataMap.get("examSurvey");
            Map<String,Object> examSurveyMap = JsonUtils.fromJson(examSurvey);
            if(examSurveyMap == null){
                examSurveyMap = new LinkedHashMap<>();
            }
            String examSurveyDetail = (String) dataMap.get("examSurveyDetail");
            List<Map> examSurveyDetailList = JsonUtils.fromJsonToList(examSurveyDetail,Map.class);
            if(examSurveyDetailList == null){
                examSurveyDetailList = new ArrayList<>();
            }
            data.put("examSurvey",examSurveyMap);

            List<Map<String,Object>> examSurveyDetailList1 = new ArrayList<>();
            examSurveyDetailList.forEach(temp -> {
                Map<String,Object> detail = new HashMap<>(temp);
                examSurveyDetailList1.add(detail);
            });

            if(schoolId != null ){
                //班级根据班级名称排序
                sortClazz(examSurveyDetailList1);
            }else{
                //Map里面包含 地区ID，地区名称，按照地区名称和学校名称的字母顺序去排序
                Collections.sort(examSurveyDetailList1, (o1, o2) -> {
                    int id1 = SafeConverter.toInt(o1.get("id"));
                    int id2 = SafeConverter.toInt(o2.get("id"));
                    return id1-id2;
                });
            }

            for(int i=0; i<examSurveyDetailList1.size(); i++){
                Map<String,Object> map = examSurveyDetailList1.get(i);
                map.put("orderNum",i+1);
            }

            data.put("examSurveyDetail",examSurveyDetailList1);
            return data;
        }else{
            return null;
        }
    }

    @Override
    public Map<String, Object> loadExamScoreState(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        MapMessage mapMessage = loadPrdMockDataService.loadExamScoreState(cityCode,regionCode,schoolId,examId);
        boolean isSuccess = (boolean) mapMessage.get("success");
        Map<String,Object> dataMap = (Map<String, Object>) mapMessage.get("dataMap");
        if(isSuccess){
            Map<String,Object> data = new LinkedHashMap<>();
            String wholeScoreStr = (String) dataMap.get("wholeScore");
            Map<String, Object> wholeScoreMap = JsonUtils.fromJson(wholeScoreStr);
            if(wholeScoreMap == null){
                wholeScoreMap = new LinkedHashMap<>();
            }
            Double fullMarks = SafeConverter.toDouble(wholeScoreMap.get("fullmarks"));
            Double averageScore = SafeConverter.toDouble(wholeScoreMap.get("averagescore"));
            BigDecimal wholeScoreRateDecimal = new BigDecimal(SafeConverter.toDouble(wholeScoreMap.get("wholescorerate")));
            wholeScoreRateDecimal = wholeScoreRateDecimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            Double wholeScoreRate = wholeScoreRateDecimal.doubleValue();
            Map<String, Double> wholeScoreDoubleMap = new LinkedHashMap<>();
            wholeScoreDoubleMap.put("fullMarks",fullMarks);
            wholeScoreDoubleMap.put("averageScore",averageScore);
            wholeScoreDoubleMap.put("wholeScoreRate",wholeScoreRate);

            String wholeScoreDetailStr = (String) dataMap.get("wholeScoreDetail");
            List<Map> wholeScoreDetailMap = JsonUtils.fromJsonToList(wholeScoreDetailStr,Map.class);
            if(CollectionUtils.isNotEmpty(wholeScoreDetailMap)) {
                for (Map scoreMap : wholeScoreDetailMap) {
                    Integer id = SafeConverter.toInt(scoreMap.get("id"));
                    scoreMap.put("id", id);
                    BigDecimal scoreRateDecimal = new BigDecimal(SafeConverter.toDouble(scoreMap.get("scorerate")));
                    scoreRateDecimal = scoreRateDecimal.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    Double scoreRate = scoreRateDecimal.doubleValue();
                    scoreMap.put("scorerate", scoreRate);
                    scoreMap.put("topScoreRate", 0);
                }

                Collections.sort(wholeScoreDetailMap, new Comparator<Map>() {
                    @Override
                    public int compare(Map o1, Map o2) {
                        Double scorerate1 = SafeConverter.toDouble(o1.get("scorerate"));
                        Double scorerate2 = SafeConverter.toDouble(o2.get("scorerate"));
                        return scorerate2.compareTo(scorerate1);
                    }
                });
                Map<String, Object> topScoreRateMap = wholeScoreDetailMap.get(0);
                topScoreRateMap.put("topScoreRate", 1);

                data.put("wholeScore", wholeScoreDoubleMap);
                data.put("wholeScoreDetail", wholeScoreDetailMap);

                List<Map<String, Object>> wholeScoreDetail = (List<Map<String, Object>>) data.get("wholeScoreDetail");
                if (schoolId != null) {
                    //班级根据班级名称排序
                    sortClazz(wholeScoreDetail);
                } else {
                    //Map里面包含 地区ID，地区名称，按照地区名称和学校名称的字母顺序去排序
                    Collections.sort(wholeScoreDetail, new Comparator<Map>() {
                        @Override
                        public int compare(Map o1, Map o2) {
                            Integer id1 = SafeConverter.toInt(o1.get("id"));
                            Integer id2 = SafeConverter.toInt(o2.get("id"));
                            return id1.compareTo(id2);
                        }
                    });
                }

                //按照得分率排序前三名
                List<Map<String, Object>> sortWholeScoreDetail = new LinkedList<>();
                //按照标准差计算第一名和最后一名
                List<Map<String, Object>> sortStandarddeviationDetail = new LinkedList<>();
                //折线图的所需的数据
                List<Integer> xAxisData = new LinkedList<>();
                List<Double> seriesData = new LinkedList<>();
                Set<Double> diffDataSet = new LinkedHashSet<>();//数据差异校验标志，长度》1则表示数据有差异
                for (int i = 0; i < wholeScoreDetail.size(); i++) {
                    Map<String, Object> map = wholeScoreDetail.get(i);
                    map.put("orderNum", i + 1);
                    sortWholeScoreDetail.add(map);
                    sortStandarddeviationDetail.add(map);
                    xAxisData.add(i + 1);
                    Double scoreRate = (Double) map.get("scorerate");
                    diffDataSet.add(scoreRate);
                    seriesData.add(scoreRate);
                }
                data.put("wholeScoreDetail", wholeScoreDetail);    // 1
                //折线图的数据包含在 wholeScore 和  wholeScoreDetail 中 ，平均分为wholeScore中的wholeScoreRate字段，其他的地区得分率在wholeScoreDetail 中
                String legendData = "各区得分率";
                data.put("legendData", legendData);
                data.put("xAxisData", xAxisData);
                data.put("seriesData", seriesData);

                //排名前三的区域，按照得分率排序
                Collections.sort(sortWholeScoreDetail, (Comparator<Map>) (o1, o2) -> {
                    Double scoreRate1 = SafeConverter.toDouble(o1.get("scorerate"));
                    Double scoreRate2 = SafeConverter.toDouble(o2.get("scorerate"));
                    return scoreRate2.compareTo(scoreRate1);
                });

                //长度大于等于2，并且数据有差异则有如下逻辑
                if (sortWholeScoreDetail.size() >= 2 && diffDataSet.size() > 1) {
                    if (sortWholeScoreDetail.size() == 2) {
                        data.put("topThreeData", sortWholeScoreDetail.get(0));
                        data.put("lastData", sortWholeScoreDetail.get(1));
                    } else if (sortWholeScoreDetail.size() == 3) {
                        Map<String, Object> second = sortWholeScoreDetail.get(1);
                        Map<String, Object> third = sortWholeScoreDetail.get(2);
                        //第二三名进行比较相等则只展示最好的第一名，
                        Double secondScorerate = (Double) second.get("scorerate");
                        Double thirdScorerate = (Double) third.get("scorerate");
                        if (Objects.equals(secondScorerate, thirdScorerate)) {
                            data.put("topThreeData", sortWholeScoreDetail.get(0));
                            data.put("lastData", new LinkedHashMap<>());
                        } else {
                            data.put("topThreeData", sortWholeScoreDetail.subList(0, 2));
                            data.put("lastData", sortWholeScoreDetail.get(sortWholeScoreDetail.size() - 1));
                        }
                    } else if (sortWholeScoreDetail.size() >= 4) {
                        //第二名和最后一名比较，如果相等，只需要取最好的去第一个，最后一名不要
                        Map<String, Object> second = sortWholeScoreDetail.get(1);
                        Double secondScorerate = (Double) second.get("scorerate");
                        Map<String, Object> third = sortWholeScoreDetail.get(2);
                        Double thirdScorerate = (Double) third.get("scorerate");
                        Map<String, Object> last = sortWholeScoreDetail.get(sortWholeScoreDetail.size() - 1);
                        Double lastScorerate = (Double) last.get("scorerate");
                        if (Objects.equals(secondScorerate, lastScorerate)) {
                            data.put("topThreeData", sortWholeScoreDetail.get(0));
                            data.put("lastData", new LinkedHashMap<>());
                        } else if (Objects.equals(thirdScorerate, lastScorerate)) {
                            //第三名和最后一名比较，如果相等则最好的取前两个，最后一名不取
                            data.put("topThreeData", sortWholeScoreDetail.subList(0, 2));
                            data.put("lastData", new LinkedHashMap<>());
                        } else {
                            data.put("topThreeData", sortWholeScoreDetail.subList(0, 3));
                            data.put("lastData", sortWholeScoreDetail.get(sortWholeScoreDetail.size() - 1));
                        }
                    }
                } else {
                    data.put("topThreeData", new LinkedList<>());
                    data.put("lastData", new LinkedHashMap<>());
                }

                //标准差数据，二位数据 list<list>
                List<List> scatterPoint = new LinkedList<>();
                for (Map<String, Object> scoreMap : wholeScoreDetail) {
                    List<Object> point = new LinkedList<>();
                    Double averageScore1 = SafeConverter.toDouble(scoreMap.get("averagescore"));
                    Double standardDeviation = SafeConverter.toDouble(scoreMap.get("standarddeviation"));
                    point.add(averageScore1);
                    point.add(standardDeviation);
                    point.add(scoreMap.get("name"));
                    scatterPoint.add(point);
                }
                data.put("scatterPointData", scatterPoint);

                //根据标准差计算学生成绩差异最小和最大的地区,按照标准差进行排序
                Collections.sort(sortStandarddeviationDetail, (Comparator<Map>) (o1, o2) -> {
                    Double standardDeviation1 = SafeConverter.toDouble(o1.get("standarddeviation"));
                    Double standardDeviation2 = SafeConverter.toDouble(o2.get("standarddeviation"));
                    return standardDeviation2.compareTo(standardDeviation1);
                });

                Map<String, Object> firstStandardPoint = sortStandarddeviationDetail.get(0);
                Map<String, Object> scatterPointMaxMap = new LinkedHashMap<>();
                scatterPointMaxMap.put("name", firstStandardPoint.get("name"));
                Integer firstId = (Integer) firstStandardPoint.get("id");

                Map<String, Object> lastStandardPoint = sortStandarddeviationDetail.get(sortStandarddeviationDetail.size() - 1);
                Map<String, Object> scatterPointMinMap = new LinkedHashMap<>();
                scatterPointMinMap.put("name", lastStandardPoint.get("name"));
                Integer lastId = (Integer) lastStandardPoint.get("id");

                int firstScoreRateLevel = 0;
                int lastScoreRateLevel = 0;
                for (int i = 0; i < sortWholeScoreDetail.size(); i++) {
                    Map<String, Object> temp = sortWholeScoreDetail.get(i);
                    Integer id = (Integer) temp.get("id");
                    if (Objects.equals(id, firstId)) {
                        firstScoreRateLevel = i + 1;
                    }
                    if (Objects.equals(id, lastId)) {
                        lastScoreRateLevel = i + 1;
                    }
                }
                scatterPointMaxMap.put("scoreRateLevel", firstScoreRateLevel);
                scatterPointMinMap.put("scoreRateLevel", lastScoreRateLevel);
                data.put("scatterPointMaxMap", scatterPointMaxMap);
                data.put("scatterPointMinMap", scatterPointMinMap);

                Double coefficient = CoefficientClient.calcCoefficent(scatterPoint);
                data.put("coefficient", coefficient);
            }
            return data;
        }else{
            return null;
        }
    }

    @Override
    public Map<String, Object> loadExamScatterPoint(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        MapMessage mapMessage = loadPrdMockDataService.loadExamScatterPoint(cityCode,regionCode,schoolId,examId);
        boolean isSuccess = (boolean) mapMessage.get("success");
        Map<String,Object> dataMap = (Map<String, Object>) mapMessage.get("dataMap");
        if(isSuccess){
            Map<String,Object> data = new LinkedHashMap<>();
            String scatterPointDataStr = (String) dataMap.get("ScatterPointData");
            List<Map> scatterPointDataMap = JsonUtils.fromJsonToList(scatterPointDataStr,Map.class);
            if(scatterPointDataMap == null){
                scatterPointDataMap = new ArrayList<>();
            }
            for(Map sactterMap : scatterPointDataMap){
                sactterMap.remove("scorerate");
                Integer id = SafeConverter.toInt(sactterMap.get("id"));
                sactterMap.put("id",id);
            }
            data.put("scatterPointData",scatterPointDataMap);
            return data;
        }else{
            return null;
        }
    }

    @Override
    public Map<String, Object> loadStudyLevelInfo(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        MapMessage mapMessage = loadPrdMockDataService.loadStudyLevelInfo(cityCode,regionCode,schoolId,examId);
        boolean isSuccess = (boolean) mapMessage.get("success");
        Map<String,Object> dataMap = (Map<String, Object>) mapMessage.get("dataMap");
        if(isSuccess){
            Map<String,Object> data = new LinkedHashMap<>();
            String studyLevelInfo = (String) dataMap.get("studyLevelInfo");
            List<Map> studyLevelInfoMapList = JsonUtils.fromJsonToList(studyLevelInfo,Map.class);
            if(studyLevelInfoMapList == null){
                studyLevelInfoMapList = new ArrayList<>();
            }
            //处理百分比
            //整体合格率 合格以上的人数除以总人数
            BigDecimal wholeQualifiledRatio = BigDecimal.ZERO;
            //整体良好率
            BigDecimal wholeGoodRatio = BigDecimal.ZERO;
            //优秀率
            BigDecimal wholeExcellentRatio = BigDecimal.ZERO;
            //待合格率
            BigDecimal wholeUnqualifiledRatio = BigDecimal.ZERO;
            for(Map levelInfoMap : studyLevelInfoMapList){
                BigDecimal levelRate = new BigDecimal(SafeConverter.toDouble(levelInfoMap.get("levelrate")));
                levelRate = levelRate.multiply(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

                levelInfoMap.put("levelrate",levelRate);
                String levelName = (String) levelInfoMap.get("levelname");
                if(!"unqualified".equals(levelName)){
                    wholeQualifiledRatio = wholeQualifiledRatio.add(levelRate);
                }
                if("excellent".equals(levelName) || "good".equals(levelName)){
                    wholeGoodRatio = wholeGoodRatio.add(levelRate);
                }
                if("unqualified".equals(levelName)){
                    wholeUnqualifiledRatio = levelRate;
                    levelInfoMap.put("levelCname","待合格");
                }
                if("excellent".equals(levelName)){
                    levelInfoMap.put("levelCname","优秀");
                    wholeExcellentRatio = levelRate;
                }
                if("good".equals(levelName)){
                    levelInfoMap.put("levelCname","良好");
                }
                if("qualified".equals(levelName)){
                    levelInfoMap.put("levelCname","合格");
                }
            }
            //整体合格率
            data.put("wholeQualifiledRatio",wholeQualifiledRatio);
            data.put("wholeGoodRatio",wholeGoodRatio);
            data.put("wholeExcellentRatio",wholeExcellentRatio);
            data.put("wholeUnqualifiledRatio",wholeUnqualifiledRatio);

            //排序学业水平，按照优秀，良好，合格，待合格进行排序
            List<String> levelNames = new ArrayList<>();
            levelNames.add("优秀");
            levelNames.add("良好");
            levelNames.add("合格");
            levelNames.add("待合格");
            List<Map<String,Object>> newStudyLevelInfoMapList = new LinkedList<>();
            for(String levelCname : levelNames){
                for(Map levelInfoMap : studyLevelInfoMapList){
                    String templevelCname = (String) levelInfoMap.get("levelCname");
                    if(levelCname.equals(templevelCname)){
                        newStudyLevelInfoMapList.add(new LinkedHashMap<>(levelInfoMap));
                    }
                }
            }

            String studyLevelInfoDetail = (String) dataMap.get("studyLevelInfoDetail");
            List<Map> studyLevelInfoDetailMapList = JsonUtils.fromJsonToList(studyLevelInfoDetail,Map.class);
            if(CollectionUtils.isNotEmpty(studyLevelInfoDetailMapList)){
                List<Map<String,Object>> newStudyLevelInfoDetailMapList = new LinkedList<>();
                for(Map levelInfoDetailMap : studyLevelInfoDetailMapList){
                    Integer id = SafeConverter.toInt(levelInfoDetailMap.get("id"));
                    levelInfoDetailMap.put("id",id);
                    levelInfoDetailMap.put("topExcellentStuRatio",0);
                    levelInfoDetailMap.put("topGoodStuRatio",0);
                    levelInfoDetailMap.put("topQualifiledStuRatio",0);
                    levelInfoDetailMap.put("topUnqulifiledStuRatio",0);
                    //计算各地区个级别的人数比例
                    //各区总学生数
                    Double studentNum = SafeConverter.toDouble(levelInfoDetailMap.get("studentnum"));
                    //优秀学生数
                    Double excellentStudentNum = SafeConverter.toDouble(levelInfoDetailMap.get("excellentstudentnum"));
                    Double excellentStuRatioTemp = excellentStudentNum / studentNum * 100;
                    BigDecimal excellentStuRatio = new BigDecimal(excellentStuRatioTemp);
                    excellentStuRatio = excellentStuRatio.setScale(2, BigDecimal.ROUND_HALF_UP);
                    levelInfoDetailMap.put("excellentStuRatio",excellentStuRatio);

                    //良好学生数
                    Double goodStudentNum = SafeConverter.toDouble(levelInfoDetailMap.get("goodstudentnum"));
                    Double goodStuRatioTemp = goodStudentNum / studentNum  * 100;
                    BigDecimal goodStuRatio = new BigDecimal(goodStuRatioTemp);
                    goodStuRatio = goodStuRatio.setScale(2, BigDecimal.ROUND_HALF_UP);
                    levelInfoDetailMap.put("goodStuRatio",goodStuRatio);
                    //合格学生数
                    Double qualifiledStudentNum = SafeConverter.toDouble(levelInfoDetailMap.get("qualifiledstudentnum"));
                    Double qualifiledStuRatioTemp = qualifiledStudentNum / studentNum  * 100;
                    BigDecimal qualifiledStuRatio = new BigDecimal(qualifiledStuRatioTemp);
                    qualifiledStuRatio = qualifiledStuRatio.setScale(2, BigDecimal.ROUND_HALF_UP);
                    levelInfoDetailMap.put("qualifiledStuRatio",qualifiledStuRatio);
                    //不合格的学上数
    //                Double unqulifiledStudentNum = SafeConverter.toDouble(levelInfoDetailMap.get("unqualifiledstudentnum"));
    //                Double unqulifiledStuRatioTemp = unqulifiledStudentNum / studentNum  * 100;
                    BigDecimal unqulifiledStuRatio = new BigDecimal(100).subtract(excellentStuRatio).subtract(goodStuRatio).subtract(qualifiledStuRatio);
    //                unqulifiledStuRatio = unqulifiledStuRatio.setScale(2, BigDecimal.ROUND_HALF_UP);
                    levelInfoDetailMap.put("unqulifiledStuRatio",unqulifiledStuRatio);

                    //===============计算良好率（优秀+良好）/总数 和合格率（优秀+良好+合格）/总数
                    Double excellentgoodRatioTemp = (excellentStudentNum + goodStudentNum)/studentNum * 100;
                    BigDecimal excellentgoodRatio = new BigDecimal(excellentgoodRatioTemp);
                    levelInfoDetailMap.put("excellentgoodRatio",excellentgoodRatio.setScale(2, BigDecimal.ROUND_HALF_UP));

                    Double excellentgoodqualifiledRatioTemp = (excellentStudentNum + goodStudentNum + qualifiledStudentNum)/studentNum * 100;
                    BigDecimal excellentgoodqualifiledRatio = new BigDecimal(excellentgoodqualifiledRatioTemp);
                    levelInfoDetailMap.put("excellentgoodqualifiledRatio",excellentgoodqualifiledRatio.setScale(2, BigDecimal.ROUND_HALF_UP));

                    levelInfoDetailMap.remove("studentnum");
                    levelInfoDetailMap.remove("goodstudentnum");
                    levelInfoDetailMap.remove("excellentstudentnum");
                    levelInfoDetailMap.remove("qualifiledstudentnum");
                    levelInfoDetailMap.remove("unqualifiledstudentnum");

                    Map<String,Object> newLevelInfoDetailMap = new LinkedHashMap<>(levelInfoDetailMap);
                    newStudyLevelInfoDetailMapList.add(newLevelInfoDetailMap);
                }
                //找出每个比例项最高的数值
                Collections.sort(newStudyLevelInfoDetailMapList, new Comparator<Map>() {
                    @Override
                    public int compare(Map o1, Map o2) {
                        BigDecimal excellentStuRatio1 = (BigDecimal) o1.get("excellentStuRatio");
                        BigDecimal excellentStuRatio2 = (BigDecimal) o2.get("excellentStuRatio");
                        return excellentStuRatio2.compareTo(excellentStuRatio1);
                    }
                });

                Map<String,Object> topExcellentRatioMap = newStudyLevelInfoDetailMapList.get(0);
                topExcellentRatioMap.put("topExcellentStuRatio",1);

                Collections.sort(newStudyLevelInfoDetailMapList, new Comparator<Map>() {
                    @Override
                    public int compare(Map o1, Map o2) {
                        BigDecimal goodStuRatio1 = (BigDecimal)o1.get("goodStuRatio");
                        BigDecimal goodStuRatio2 = (BigDecimal)o2.get("goodStuRatio");
                        return goodStuRatio2.compareTo(goodStuRatio1);
                    }
                });
                Map<String,Object> topGoodRatioMap = newStudyLevelInfoDetailMapList.get(0);
                topGoodRatioMap.put("topGoodStuRatio",1);

                Collections.sort(newStudyLevelInfoDetailMapList, new Comparator<Map>() {
                    @Override
                    public int compare(Map o1, Map o2) {
                        BigDecimal qualifiledStuRatio1 = (BigDecimal)o1.get("qualifiledStuRatio");
                        BigDecimal qualifiledStuRatio2 = (BigDecimal)o2.get("qualifiledStuRatio");
                        return qualifiledStuRatio2.compareTo(qualifiledStuRatio1);
                    }
                });
                Map<String,Object> topQualifiledRatioMap = newStudyLevelInfoDetailMapList.get(0);
                topQualifiledRatioMap.put("topQualifiledStuRatio",1);

                Collections.sort(newStudyLevelInfoDetailMapList, new Comparator<Map>() {
                    @Override
                    public int compare(Map o1, Map o2) {
                        BigDecimal unqulifiledStuRatio1 = (BigDecimal)o1.get("unqulifiledStuRatio");
                        BigDecimal unqulifiledStuRatio2 = (BigDecimal)o2.get("unqulifiledStuRatio");
                        return unqulifiledStuRatio2.compareTo(unqulifiledStuRatio1);
                    }
                });
                Map<String,Object> topUnqulifiledRatioMap = newStudyLevelInfoDetailMapList.get(0);
                topUnqulifiledRatioMap.put("topUnqulifiledStuRatio",1);


                data.put("studyLevelInfo",newStudyLevelInfoMapList);

                //data.put("studyLevelInfoDetail",studyLevelInfoDetailMapList);
                //List<Map<String,Object>> studyLevelInfoDetail = (List<Map<String, Object>>) data.get("studyLevelInfoDetail");

                if(schoolId != null){
                    //班级根据班级名称排序
                    sortClazz(newStudyLevelInfoDetailMapList);
                }else{
                    //Map里面包含 地区ID，地区名称，按照地区名称和学校名称的字母顺序去排序
                    Collections.sort(newStudyLevelInfoDetailMapList, new Comparator<Map>() {
                        @Override
                        public int compare(Map o1, Map o2) {
                            Integer id1 = SafeConverter.toInt(o1.get("id"));
                            Integer id2 = SafeConverter.toInt(o2.get("id"));
                            return id1.compareTo(id2);
                        }
                    });
                }

                List<Map<String,Object>> gridDataList = new LinkedList<>();
                for(int i=0; i<newStudyLevelInfoDetailMapList.size(); i++){
                    Map<String,Object> map = newStudyLevelInfoDetailMapList.get(i);
                    map.put("orderNum",i+1);
                    Map<String,Object> newMap = new LinkedHashMap<>(map);
                    newMap.remove("excellentgoodRatio");
                    newMap.remove("excellentgoodqualifiledRatio");
                    gridDataList.add(newMap);
                }
                //详细表格数据
                data.put("gridDataList",gridDataList);     //页面需要的数据  1

                List<Integer> xAxisData = new LinkedList<>();
                List<Map<String,Object>> barDataList = new LinkedList<>();
                Map<String,Object> excellentStuRatioMap = new LinkedHashMap<>();
                excellentStuRatioMap.put("ratioName","优秀率");
                List<Object>  excellentRatioData = new LinkedList<>();
                excellentStuRatioMap.put("data",excellentRatioData);

                Map<String,Object> goodStuRatioMap = new LinkedHashMap<>();
                goodStuRatioMap.put("ratioName","良好率");
                List<Object>  goodRatioData = new LinkedList<>();
                goodStuRatioMap.put("data",goodRatioData);
                Map<String,Object> qualifiledStuRatioMap = new LinkedHashMap<>();
                qualifiledStuRatioMap.put("ratioName","合格率");
                List<Object>  qualifiledRatioData = new LinkedList<>();
                qualifiledStuRatioMap.put("data",qualifiledRatioData);
                Map<String,Object> unqualifiledStuRatioMap = new LinkedHashMap<>();
                unqualifiledStuRatioMap.put("ratioName","待合格率");
                List<Object>  unqualifiledRatioData = new LinkedList<>();
                unqualifiledStuRatioMap.put("data",unqualifiledRatioData);
                barDataList.add(excellentStuRatioMap);
                barDataList.add(goodStuRatioMap);
                barDataList.add(qualifiledStuRatioMap);
                barDataList.add(unqualifiledStuRatioMap);
                List<Map<String,Object>> excellentRatioInfoDetail = new LinkedList<>();
                List<Map<String,Object>> excellentgoodRatioInfoDetail = new LinkedList<>();
                List<Map<String,Object>> excellgoodqualifiledRatioInfoDetail = new LinkedList<>();
                List<Map<String,Object>> unqualifiledRatioInfoDetail = new LinkedList<>();

                //良好率柱图数据
                Map<String,Object> excellentgoodStuRatioMap = new LinkedHashMap<>();
                excellentgoodStuRatioMap.put("ratioName","良好率");
                List<Object>  excellentgoodRatioData = new LinkedList<>();
                List<Object>  excellentgoodqualifiledRatioData = new LinkedList<>();
                Set<String> excellentRatioDiffSet = new LinkedHashSet<>();
                Set<String> excellentgoodRatioDiffSet = new LinkedHashSet<>();
                Set<String> excellentgoodqualifiledRatioDiffSet = new LinkedHashSet<>();
                Set<String> unqualifiledRatioDiffSet = new LinkedHashSet<>();
                for(int i=0; i< newStudyLevelInfoDetailMapList.size(); i++){
                    Map<String,Object> map = newStudyLevelInfoDetailMapList.get(i);
                    xAxisData.add(i+1);
                    excellentRatioData.add(map.get("excellentStuRatio"));
                    excellentRatioDiffSet.add(map.get("excellentStuRatio").toString());
                    goodRatioData.add(map.get("goodStuRatio"));
                    qualifiledRatioData.add(map.get("qualifiledStuRatio"));
                    unqualifiledRatioData.add(map.get("unqulifiledStuRatio"));
                    unqualifiledRatioDiffSet.add(map.get("unqulifiledStuRatio").toString());
                    //良好率柱图数据
                    excellentgoodRatioData.add(map.get("excellentgoodRatio"));
                    excellentgoodRatioDiffSet.add(map.get("excellentgoodRatio").toString());
                    //合格率柱图数据
                    excellentgoodqualifiledRatioData.add(map.get("excellentgoodqualifiledRatio"));
                    excellentgoodqualifiledRatioDiffSet.add(map.get("excellentgoodqualifiledRatio").toString());

                    excellentRatioInfoDetail.add(map);
                    excellentgoodRatioInfoDetail.add(map);
                    excellgoodqualifiledRatioInfoDetail.add(map);
                    unqualifiledRatioInfoDetail.add(map);
                }

                //1，整体柱图，拼接多段柱图数据
                //优秀、良好、合格、不合格 四段数据
                Map<String,Object> wholeBarMap = new LinkedHashMap<>();
                List<String> wholeLegendDataList = new LinkedList<>();
                wholeLegendDataList.add("优秀率");
                wholeLegendDataList.add("良好率");
                wholeLegendDataList.add("合格率");
                wholeLegendDataList.add("待合格率");
                wholeBarMap.put("legendData",wholeLegendDataList);
                wholeBarMap.put("xAxisData",xAxisData);
                wholeBarMap.put("seriesData",barDataList);
                data.put("wholeBarMap",wholeBarMap);                  //2

                //2，优秀率柱图   ，，优秀率数据，整体优秀率,前三名优秀率
                Map<String,Object> excellentBarMap = new LinkedHashMap<>();
                excellentBarMap.put("legendData","优秀率");
                excellentBarMap.put("xAxisData",xAxisData);
                excellentBarMap.put("seriesData",excellentRatioData);
                excellentBarMap.put("averageData",data.get("wholeExcellentRatio"));
                Collections.sort(excellentRatioInfoDetail, (Comparator<Map>) (o1, o2) -> {
                    BigDecimal excellentStuRatio1 = (BigDecimal) o1.get("excellentStuRatio");
                    BigDecimal excellentStuRatio2 = (BigDecimal) o2.get("excellentStuRatio");
                    return excellentStuRatio2.compareTo(excellentStuRatio1);
                });
                for(Map<String,Object> temp : excellentRatioInfoDetail){
                    temp.remove("topExcellentStuRatio");
                    temp.remove("topGoodStuRatio");
                    temp.remove("topQualifiledStuRatio");
                    temp.remove("topUnqulifiledStuRatio");
                    temp.remove("topUnqulifiledStuRatio");
                    temp.remove("goodStuRatio");
                    temp.remove("qualifiledStuRatio");
                }
                getExcellentRatioTopthree(excellentRatioInfoDetail,excellentRatioDiffSet,excellentBarMap);
                data.put("excellentBarMap",excellentBarMap);                          //3

                //3，良好率柱图，良好率数据，整体良好率,前三名良好率
                Map<String,Object> excellgoodBarMap = new LinkedHashMap<>();
                excellgoodBarMap.put("legendData","良好率");
                excellgoodBarMap.put("xAxisData",xAxisData);
                excellgoodBarMap.put("seriesData",excellentgoodRatioData);
                excellgoodBarMap.put("averageData",data.get("wholeGoodRatio"));
                Collections.sort(excellentgoodRatioInfoDetail, (Comparator<Map>) (o1, o2) -> {
                    BigDecimal excellentgoodRatio1 = (BigDecimal) o1.get("excellentgoodRatio");
                    BigDecimal excellentgoodRatio2 = (BigDecimal) o2.get("excellentgoodRatio");
                    return excellentgoodRatio2.compareTo(excellentgoodRatio1);
                });
                //获得良好率的topTree
                getExcellentGoodRatioTopthree(excellentgoodRatioInfoDetail, excellentgoodRatioDiffSet, excellgoodBarMap);
                data.put("excellgoodBarMap",excellgoodBarMap);                //4

                //3，合格率柱图，合格率数据，合格率,前三名合格率
                Map<String,Object> excellgoodqulifiledBarMap = new LinkedHashMap<>();
                excellgoodqulifiledBarMap.put("legendData","合格率");
                excellgoodqulifiledBarMap.put("xAxisData",xAxisData);
                excellgoodqulifiledBarMap.put("seriesData",excellentgoodqualifiledRatioData);
                excellgoodqulifiledBarMap.put("averageData",wholeQualifiledRatio);
                Collections.sort(excellgoodqualifiledRatioInfoDetail, (Comparator<Map>) (o1, o2) -> {
                    BigDecimal excellentgoodqualifiledRatio1 = (BigDecimal) o1.get("excellentgoodqualifiledRatio");
                    BigDecimal excellentgoodqualifiledRatio2 = (BigDecimal) o2.get("excellentgoodqualifiledRatio");
                    return excellentgoodqualifiledRatio2.compareTo(excellentgoodqualifiledRatio1);
                });

                //获得合格率的toptree 和 last 和差值
                getExcellgoodqualifiledRatioTopTree(wholeQualifiledRatio, excellgoodqualifiledRatioInfoDetail, excellentgoodqualifiledRatioDiffSet, excellgoodqulifiledBarMap);
                data.put("excellgoodqulifiledBarMap",excellgoodqulifiledBarMap);               //5

                //4，待合格率柱图，待合格率数据，总待合格率,前三名待合格率
                Map<String,Object> unqulifiledBarMap = new LinkedHashMap<>();
                unqulifiledBarMap.put("legendData","待合格率");
                unqulifiledBarMap.put("xAxisData",xAxisData);
                unqulifiledBarMap.put("seriesData",unqualifiledRatioData);
                unqulifiledBarMap.put("averageData",data.get("wholeUnqualifiledRatio"));
                Collections.sort(unqualifiledRatioInfoDetail, (Comparator<Map>) (o1, o2) -> {
                    BigDecimal unqulifiledStuRatio1 = (BigDecimal) o1.get("unqulifiledStuRatio");
                    BigDecimal unqulifiledStuRatio2 = (BigDecimal) o2.get("unqulifiledStuRatio");
                    return unqulifiledStuRatio2.compareTo(unqulifiledStuRatio1);
                });
                data.put("unqulifiledBarMap",unqulifiledBarMap);                //6
            }

            return data;
        }else{
            return null;
        }
    }

    private void getExcellgoodqualifiledRatioTopTree(BigDecimal wholeQualifiledRatio, List<Map<String, Object>> excellgoodqualifiledRatioInfoDetail, Set<String> excellentgoodqualifiledRatioDiffSet, Map<String, Object> excellgoodqulifiledBarMap) {
        //长度大于等于2，并且数据有差异则有如下逻辑
        if(excellgoodqualifiledRatioInfoDetail.size() >=2 && excellentgoodqualifiledRatioDiffSet.size()>1){
            if(excellgoodqualifiledRatioInfoDetail.size() == 2){
                excellgoodqulifiledBarMap.put("topThreeExcellentgoodqualifiledRatio",excellgoodqualifiledRatioInfoDetail.get(0));
                Map<String,Object> last = excellgoodqualifiledRatioInfoDetail.get(1);
                excellgoodqulifiledBarMap.put("lastExcellentgoodqualifiledRatio",last);
                BigDecimal diff = wholeQualifiledRatio.subtract((BigDecimal) last.get("excellentgoodqualifiledRatio"));
                excellgoodqulifiledBarMap.put("diff",diff);
            }else if(excellgoodqualifiledRatioInfoDetail.size() == 3){
                Map<String,Object> second = excellgoodqualifiledRatioInfoDetail.get(1);
                Map<String,Object> third = excellgoodqualifiledRatioInfoDetail.get(2);
                //第二三名进行比较相等则只展示最好的第一名，
                BigDecimal  secondScorerate = (BigDecimal) second.get("excellentgoodqualifiledRatio");
                BigDecimal  thirdScorerate = (BigDecimal) third.get("excellentgoodqualifiledRatio");
                if(Objects.equals(secondScorerate,thirdScorerate)){
                    excellgoodqulifiledBarMap.put("topThreeExcellentgoodqualifiledRatio",excellgoodqualifiledRatioInfoDetail.get(0));
                    excellgoodqulifiledBarMap.put("lastExcellentgoodqualifiledRatio",new LinkedHashMap<>());
                }else{
                    excellgoodqulifiledBarMap.put("topThreeExcellentgoodqualifiledRatio",excellgoodqualifiledRatioInfoDetail.subList(0,2));
                    excellgoodqulifiledBarMap.put("lastExcellentgoodqualifiledRatio",third);
                    BigDecimal diff = wholeQualifiledRatio.subtract((BigDecimal) third.get("excellentgoodqualifiledRatio"));
                    excellgoodqulifiledBarMap.put("diff",diff);
                }
            }else if(excellgoodqualifiledRatioInfoDetail.size()>=4){
                //第二名和最后一名比较，如果相等，只需要取最好的去第一个，最后一名不要
                Map<String,Object> second = excellgoodqualifiledRatioInfoDetail.get(1);
                BigDecimal secondScorerate = (BigDecimal) second.get("excellentgoodqualifiledRatio");
                Map<String,Object> third = excellgoodqualifiledRatioInfoDetail.get(2);
                BigDecimal thirdScorerate = (BigDecimal) third.get("excellentgoodqualifiledRatio");
                Map<String,Object> last = excellgoodqualifiledRatioInfoDetail.get(excellgoodqualifiledRatioInfoDetail.size()-1);
                BigDecimal lastScorerate = (BigDecimal) last.get("excellentgoodqualifiledRatio");
                if(Objects.equals(secondScorerate,lastScorerate)){
                    excellgoodqulifiledBarMap.put("topThreeExcellentgoodqualifiledRatio",excellgoodqualifiledRatioInfoDetail.get(0));
                    excellgoodqulifiledBarMap.put("lastExcellentgoodqualifiledRatio",new LinkedHashMap<>());
                }else if(Objects.equals(thirdScorerate,lastScorerate)){
                    //第三名和最后一名比较，如果相等则最好的取前两个，最后一名不取
                    excellgoodqulifiledBarMap.put("topThreeExcellentgoodqualifiledRatio",excellgoodqualifiledRatioInfoDetail.subList(0,2));
                    excellgoodqulifiledBarMap.put("lastExcellentgoodqualifiledRatio",new LinkedHashMap<>());
                }else{
                    excellgoodqulifiledBarMap.put("topThreeExcellentgoodqualifiledRatio",excellgoodqualifiledRatioInfoDetail.subList(0,3));
                    excellgoodqulifiledBarMap.put("lastExcellentgoodqualifiledRatio",last);
                    BigDecimal diff = wholeQualifiledRatio.subtract((BigDecimal) last.get("excellentgoodqualifiledRatio"));
                    excellgoodqulifiledBarMap.put("diff",diff);
                }
            }
        }else{
            excellgoodqulifiledBarMap.put("topThreeExcellentgoodqualifiledRatio",new LinkedList<>());
            excellgoodqulifiledBarMap.put("lastExcellentgoodqualifiledRatio",new LinkedHashMap<>());
        }
    }

    private void getExcellentRatioTopthree(List<Map<String, Object>> excellentRatioInfoDetail, Set<String> excellentRatioDiffSet, Map<String, Object> excellentBarMap){
        //长度大于等于2，并且数据有差异则有如下逻辑
        if(excellentRatioInfoDetail.size() >=2 && excellentRatioDiffSet.size()>1){
            if(excellentRatioInfoDetail.size() == 2){
                excellentBarMap.put("topThreeExcellentRatio",excellentRatioInfoDetail.get(0));
            }else if(excellentRatioInfoDetail.size() == 3){
                Map<String,Object> second = excellentRatioInfoDetail.get(1);
                Map<String,Object> third = excellentRatioInfoDetail.get(2);
                //第二三名进行比较相等则只展示最好的第一名，
                BigDecimal  secondScorerate = (BigDecimal) second.get("excellentStuRatio");
                BigDecimal  thirdScorerate = (BigDecimal) third.get("excellentStuRatio");
                if(Objects.equals(secondScorerate,thirdScorerate)){
                    excellentBarMap.put("topThreeExcellentRatio",excellentRatioInfoDetail.get(0));
                }else{
                    excellentBarMap.put("topThreeExcellentRatio",excellentRatioInfoDetail.subList(0,2));
                }
            }else if(excellentRatioInfoDetail.size()>=4){
                //第二名和最后一名比较，如果相等，只需要取最好的去第一个，最后一名不要
                Map<String,Object> second = excellentRatioInfoDetail.get(1);
                BigDecimal secondScorerate = (BigDecimal) second.get("excellentStuRatio");
                Map<String,Object> third = excellentRatioInfoDetail.get(2);
                BigDecimal thirdScorerate = (BigDecimal) third.get("excellentStuRatio");
                Map<String,Object> last = excellentRatioInfoDetail.get(excellentRatioInfoDetail.size()-1);
                BigDecimal lastScorerate = (BigDecimal) last.get("excellentStuRatio");
                if(Objects.equals(secondScorerate,lastScorerate)){
                    excellentBarMap.put("topThreeExcellentRatio",excellentRatioInfoDetail.get(0));
                }else if(Objects.equals(thirdScorerate,lastScorerate)){
                    //第三名和最后一名比较，如果相等则最好的取前两个，最后一名不取
                    excellentBarMap.put("topThreeExcellentRatio",excellentRatioInfoDetail.subList(0,2));
                }else{
                    excellentBarMap.put("topThreeExcellentRatio",excellentRatioInfoDetail.subList(0,3));
                }
            }
        }else{
            excellentBarMap.put("topThreeExcellentRatio",new LinkedList<>());
        }
    }

    private void getExcellentGoodRatioTopthree(List<Map<String, Object>> excellentgoodRatioInfoDetail, Set<String> excellentgoodRatioDiffSet, Map<String, Object> excellgoodBarMap) {
        //长度大于等于2，并且数据有差异则有如下逻辑
        if(excellentgoodRatioInfoDetail.size() >=2 && excellentgoodRatioDiffSet.size()>1){
            if(excellentgoodRatioInfoDetail.size() == 2){
                excellgoodBarMap.put("topThreeExcellentgoodRatio",excellentgoodRatioInfoDetail.get(0));
            }else if(excellentgoodRatioInfoDetail.size() == 3){
                Map<String,Object> second = excellentgoodRatioInfoDetail.get(1);
                Map<String,Object> third = excellentgoodRatioInfoDetail.get(2);
                //第二三名进行比较相等则只展示最好的第一名，
                BigDecimal  secondScorerate = (BigDecimal) second.get("excellentgoodRatio");
                BigDecimal  thirdScorerate = (BigDecimal) third.get("excellentgoodRatio");
                if(Objects.equals(secondScorerate,thirdScorerate)){
                    excellgoodBarMap.put("topThreeExcellentgoodRatio",excellentgoodRatioInfoDetail.get(0));
                }else{
                    excellgoodBarMap.put("topThreeExcellentgoodRatio",excellentgoodRatioInfoDetail.subList(0,2));
                }
            }else if(excellentgoodRatioInfoDetail.size()>=4){
                //第二名和最后一名比较，如果相等，只需要取最好的去第一个，最后一名不要
                Map<String,Object> second = excellentgoodRatioInfoDetail.get(1);
                BigDecimal secondScorerate = (BigDecimal) second.get("excellentgoodRatio");
                Map<String,Object> third = excellentgoodRatioInfoDetail.get(2);
                BigDecimal thirdScorerate = (BigDecimal) third.get("excellentgoodRatio");
                Map<String,Object> last = excellentgoodRatioInfoDetail.get(excellentgoodRatioInfoDetail.size()-1);
                BigDecimal lastScorerate = (BigDecimal) last.get("excellentgoodRatio");
                if(Objects.equals(secondScorerate,lastScorerate)){
                    excellgoodBarMap.put("topThreeExcellentgoodRatio",excellentgoodRatioInfoDetail.get(0));
                }else if(Objects.equals(thirdScorerate,lastScorerate)){
                    //第三名和最后一名比较，如果相等则最好的取前两个，最后一名不取
                    excellgoodBarMap.put("topThreeExcellentgoodRatio",excellentgoodRatioInfoDetail.subList(0,2));
                }else{
                    excellgoodBarMap.put("topThreeExcellentgoodRatio",excellentgoodRatioInfoDetail.subList(0,3));
                }
            }
        }else{
            excellgoodBarMap.put("topThreeExcellentgoodRatio",new LinkedList<>());
        }
    }

    @Override
    public Map<String, Object> loadSubjectAbilityInfo(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        MapMessage mapMessage = loadPrdMockDataService.loadSubjectAbilityInfo(cityCode,regionCode,schoolId,examId);
        boolean isSuccess = (boolean) mapMessage.get("success");
        Map<String,Object> dataMap = (Map<String, Object>) mapMessage.get("dataMap");
        if(isSuccess){
            Map<String,Object> data = new LinkedHashMap<>();
            String subjectAbilityInfoStr = (String) dataMap.get("subjectAbilityInfo");
            List<Map> subjectAbilityInfo = JsonUtils.fromJsonToList(subjectAbilityInfoStr,Map.class);
            if(CollectionUtils.isNotEmpty(subjectAbilityInfo)) {

                List<String> subjectAbilityList = new LinkedList<>();
                for (Map<String, Object> temp : subjectAbilityInfo) {
                    BigDecimal averagescore = new BigDecimal(SafeConverter.toDouble(temp.get("averagescore"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal totalscore = new BigDecimal(SafeConverter.toDouble(temp.get("totalscore"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal scorerate = new BigDecimal(SafeConverter.toDouble(temp.get("scorerate")) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);

                    temp.put("averagescore", averagescore);
                    temp.put("totalscore", totalscore);
                    temp.put("scorerate", scorerate);
                    subjectAbilityList.add((String) temp.get("subjectability"));
                }
                Collections.sort(subjectAbilityInfo, (o1, o2) -> {
                    BigDecimal scorerate1 = (BigDecimal) o1.get("scorerate");
                    BigDecimal scorerate2 = (BigDecimal) o2.get("scorerate");
                    return scorerate2.compareTo(scorerate1);
                });
                data.put("subjectAbilityInfo", subjectAbilityInfo);
                Map<String, Object> subjectAbilityMax = subjectAbilityInfo.get(0);
                BigDecimal maxScorerate = (BigDecimal) subjectAbilityMax.get("scorerate");
                String rateLevelNameOfAbilityMax = getRateLevelNameOfAbility(maxScorerate);
                subjectAbilityMax.put("rateLevelName", rateLevelNameOfAbilityMax);
                Map<String, Object> subjectAbilityMin = subjectAbilityInfo.get(subjectAbilityInfo.size() - 1);
                BigDecimal minScorerate = (BigDecimal) subjectAbilityMin.get("scorerate");
                String rateLevelNameOfAbilityMin = getRateLevelNameOfAbility(minScorerate);
                subjectAbilityMin.put("rateLevelName", rateLevelNameOfAbilityMin);

                data.put("subjectAbilityMax", subjectAbilityMax);
                data.put("subjectAbilityMin", subjectAbilityMin);
                data.put("subjectAbilityList", subjectAbilityList);

                String subjectAbilityDetailListStr = (String) dataMap.get("subjectAbilityDetailList");
                List<Map> subjectAbilityDetailList = JsonUtils.fromJsonToList(subjectAbilityDetailListStr, Map.class);
                if (subjectAbilityDetailList == null) {
                    subjectAbilityDetailList = new ArrayList<>();
                }
                List<Map<String, Object>> subjectAbilityData = new LinkedList<>();

                for (Map<String, Object> temp : subjectAbilityDetailList) {
                    Map<String, Object> item = new LinkedHashMap();
                    Integer id = SafeConverter.toInt(temp.get("id"));
                    String name = (String) temp.get("name");
                    List<Map> abilitydetail = (List<Map>) temp.get("abilitydetail");
                    item.put("id", id);
                    item.put("name", name);
                    for (int i = 1; i <= subjectAbilityList.size(); i++) {
                        String ability = subjectAbilityList.get(i - 1);
                        for (Map map : abilitydetail) {
                            String abilityName = (String) map.get("subjectability");
                            if (ability.equals(abilityName)) {
                                BigDecimal averagescore = new BigDecimal(SafeConverter.toDouble(map.get("averagescore"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                                BigDecimal scorerate = new BigDecimal(SafeConverter.toDouble(map.get("scorerate")) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
                                map.put("averagescore", averagescore);
                                map.put("scorerate", scorerate);
                                item.put("averagescore_" + i, averagescore);
                                item.put("scorerate_" + i, scorerate);
                                item.put("topScorerate_" + i, 0);
                            }
                        }
                    }
                    subjectAbilityData.add(item);
                }
                //排序每个学科能力的得分率
                for (int i = 1; i <= subjectAbilityList.size(); i++) {
                    final int orderNo = i;
                    Function<Map, BigDecimal> fun_c = o -> (BigDecimal) o.get("scorerate_" + orderNo);
                    Collections.sort(subjectAbilityData, (Comparator<Map>) (o1, o2) -> fun_c.apply(o2).compareTo(fun_c.apply(o1)));
                    Map<String, Object> item = subjectAbilityData.get(0);
                    item.put("topScorerate_" + orderNo, 1);
                }
                if (schoolId != null) {
                    sortClazz(subjectAbilityData);
                } else {
                    Collections.sort(subjectAbilityData, (Comparator<Map>) (o1, o2) -> {
                        Integer id1 = (Integer) o1.get("id");
                        Integer id2 = (Integer) o2.get("id");
                        return id1.compareTo(id2);
                    });
                }

                List<Integer> xAxisData = new LinkedList<>();
                for (int i = 1; i <= subjectAbilityData.size(); i++) {
                    Map<String, Object> item = subjectAbilityData.get(i - 1);
                    item.put("orderNo", i);
                    xAxisData.add(i);
                }
                //每个能力的线图数据，包含 legendData ，xAxisData，seriesData，markLineData,topTree,last
                List<Map<String, Object>> subjectAbilityDataMapList = new LinkedList<>();
                for (int i = 1; i <= subjectAbilityList.size(); i++) {
                    String subjectAbility = subjectAbilityList.get(i - 1);
                    Map<String, Object> subjectAbilityDataMap = new LinkedHashMap<>();
                    String legendData = "能力-" + subjectAbility;
                    subjectAbilityDataMap.put("legendData", legendData);
                    List<BigDecimal> seriesData = new LinkedList<>();
                    List<Map<String, Object>> sortDataList = new LinkedList<>();
                    Set<BigDecimal> diffSet = new LinkedHashSet<>();
                    for (Map<String, Object> item : subjectAbilityData) {
                        BigDecimal scorerate = (BigDecimal) item.get("scorerate_" + i);
                        seriesData.add(scorerate);
                        Map<String, Object> sortMap = new LinkedHashMap<>();
                        sortMap.put("id", item.get("id"));
                        sortMap.put("name", item.get("name"));
                        sortMap.put("scorerate", scorerate);
                        diffSet.add(scorerate);
                        sortDataList.add(sortMap);
                    }
                    subjectAbilityDataMap.put("seriesData", seriesData);
                    BigDecimal markLineData = BigDecimal.ZERO;
                    for (Map<String, Object> temp : subjectAbilityInfo) {
                        String name = (String) temp.get("subjectability");
                        if (subjectAbility.equals(name)) {
                            markLineData = (BigDecimal) temp.get("scorerate");
                            break;
                        }
                    }
                    subjectAbilityDataMap.put("markLineData", markLineData);
                    //排序前三的
                    Collections.sort(sortDataList, (Comparator<Map>) (o1, o2) -> {
                        BigDecimal scorerate1 = (BigDecimal) o1.get("scorerate");
                        BigDecimal scorerate2 = (BigDecimal) o2.get("scorerate");
                        return scorerate2.compareTo(scorerate1);
                    });

                    //长度大于等于2，并且数据有差异则有如下逻辑
                    getTopTree(subjectAbilityDataMap, sortDataList, diffSet, markLineData);
                    subjectAbilityDataMap.put("xAxisData", xAxisData);
                    subjectAbilityDataMapList.add(subjectAbilityDataMap);
                }
                //表格的数据
                List<Map<String, Object>> subjectAbilitydetailGrid = new LinkedList<>();
                for (Map<String, Object> temp : subjectAbilityData) {
                    Map<String, Object> abilityDetailMap = new LinkedHashMap<>();
                    abilityDetailMap.put("id", temp.get("id"));
                    abilityDetailMap.put("name", temp.get("name"));
                    abilityDetailMap.put("orderNo", temp.get("orderNo"));
                    List<Map<String, Object>> abilityDataList = new LinkedList<>();
                    for (int i = 1; i <= subjectAbilityList.size(); i++) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("averagescore", temp.get("averagescore_" + i));
                        map.put("scorerate", temp.get("scorerate_" + i));
                        map.put("topScorerate", temp.get("topScorerate_" + i));
                        abilityDataList.add(map);
                    }
                    abilityDetailMap.put("abilityList", abilityDataList);
                    subjectAbilitydetailGrid.add(abilityDetailMap);
                }
                data.put("subjectAbilityList", subjectAbilityList);
                data.put("subjectAbilitydetailGrid", subjectAbilitydetailGrid);
                data.put("subjectAbilityDataMapList", subjectAbilityDataMapList);
            }
            return data;
        }else{
            return null;
        }
    }

    private String getRateLevelNameOfAbility(BigDecimal maxScorerate) {
        String rateLevel = "待提升";
        if(maxScorerate.compareTo(new BigDecimal(85)) >=0 ){
            rateLevel = "优秀";
        }else if(maxScorerate.compareTo(new BigDecimal(85))<0&&maxScorerate.compareTo(new BigDecimal(75))>=0){
            rateLevel = "良好";
        }else if(maxScorerate.compareTo(new BigDecimal(75))<0&&maxScorerate.compareTo(new BigDecimal(60))>=0){
            rateLevel = "合格";
        }
        return rateLevel;
    }

    @Override
    public Map<String, Object> loadKnowledgePlateInfo(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        MapMessage mapMessage = loadPrdMockDataService.loadKnowledgePlateInfo(cityCode,regionCode,schoolId,examId);
        boolean isSuccess = (boolean) mapMessage.get("success");
        Map<String,Object> dataMap = (Map<String, Object>) mapMessage.get("dataMap");
        if(isSuccess){
            Map<String,Object> data = new LinkedHashMap<>();
            String knowledgePlateInfoStr = (String) dataMap.get("knowledgePlateInfo");
            List<Map> knowledgePlateInfo = JsonUtils.fromJsonToList(knowledgePlateInfoStr,Map.class);
            if(CollectionUtils.isNotEmpty(knowledgePlateInfo)) {
                List<String> knowledgePlateList = new LinkedList<>();
                for (Map<String, Object> temp : knowledgePlateInfo) {
                    BigDecimal averagescore = new BigDecimal(SafeConverter.toDouble(temp.get("averagescore"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal totalscore = new BigDecimal(SafeConverter.toDouble(temp.get("totalscore"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal scorerate = new BigDecimal(SafeConverter.toDouble(temp.get("scorerate")) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);

                    temp.put("averagescore", averagescore);
                    temp.put("totalscore", totalscore);
                    temp.put("scorerate", scorerate);
                    knowledgePlateList.add((String) temp.get("knowledgeplate"));
                }
                Collections.sort(knowledgePlateInfo, new Comparator<Map>() {
                    @Override
                    public int compare(Map o1, Map o2) {
                        BigDecimal scorerate1 = (BigDecimal) o1.get("scorerate");
                        BigDecimal scorerate2 = (BigDecimal) o2.get("scorerate");
                        return scorerate2.compareTo(scorerate1);
                    }
                });
                data.put("knowledgePlateInfo", knowledgePlateInfo);
                Map<String, Object> knowledgePlateMax = knowledgePlateInfo.get(0);
                BigDecimal maxScorerate = (BigDecimal) knowledgePlateMax.get("scorerate");
                String rateLevelNameOfAbilityMax = getRateLevelNameOfAbility(maxScorerate);
                knowledgePlateMax.put("rateLevelName", rateLevelNameOfAbilityMax);
                Map<String, Object> knowledgePlateMin = knowledgePlateInfo.get(knowledgePlateInfo.size() - 1);
                BigDecimal minScorerate = (BigDecimal) knowledgePlateMin.get("scorerate");
                String rateLevelNameOfAbilityMin = getRateLevelNameOfAbility(minScorerate);
                knowledgePlateMin.put("rateLevelName", rateLevelNameOfAbilityMin);

                data.put("knowledgePlateMax", knowledgePlateMax);
                data.put("knowledgePlateMin", knowledgePlateMin);
                data.put("knowledgePlateList", knowledgePlateList);

                String knowledgePlateDetailListStr = (String) dataMap.get("knowledgePlateDetailList");
                List<Map> knowledgePlateDetailList = JsonUtils.fromJsonToList(knowledgePlateDetailListStr, Map.class);
                if (knowledgePlateDetailList == null) {
                    knowledgePlateDetailList = new ArrayList<>();
                }
                List<Map<String, Object>> knowledgePlateData = new LinkedList<>();

                for (Map<String, Object> temp : knowledgePlateDetailList) {
                    Map<String, Object> item = new LinkedHashMap();
                    Integer id = SafeConverter.toInt(temp.get("id"));
                    String name = (String) temp.get("name");
                    List<Map> knowledgedetail = (List<Map>) temp.get("knowledgemoduledetail");
                    item.put("id", id);
                    item.put("name", name);
                    for (int i = 1; i <= knowledgePlateList.size(); i++) {
                        String knowledge = knowledgePlateList.get(i - 1);
                        for (Map map : knowledgedetail) {
                            String knowledgeName = (String) map.get("knowledgeplate");
                            if (knowledge.equals(knowledgeName)) {
                                BigDecimal averagescore = new BigDecimal(SafeConverter.toDouble(map.get("averagescore"))).setScale(2, BigDecimal.ROUND_HALF_UP);
                                BigDecimal scorerate = new BigDecimal(SafeConverter.toDouble(map.get("scorerate")) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
                                map.put("averagescore", averagescore);
                                map.put("scorerate", scorerate);
                                item.put("averagescore_" + i, averagescore);
                                item.put("scorerate_" + i, scorerate);
                                item.put("topScorerate_" + i, 0);
                            }
                        }
                    }
                    knowledgePlateData.add(item);
                }
                //排序每个知识板块的得分率
                for (int i = 1; i <= knowledgePlateList.size(); i++) {
                    final int orderNo = i;
                    Function<Map, BigDecimal> fun_c = o -> (BigDecimal) o.get("scorerate_" + orderNo);
                    Collections.sort(knowledgePlateData, (Comparator<Map>) (o1, o2) -> fun_c.apply(o2).compareTo(fun_c.apply(o1)));
                    Map<String, Object> item = knowledgePlateData.get(0);
                    item.put("topScorerate_" + orderNo, 1);
                }

                if (schoolId != null) {
                    sortClazz(knowledgePlateData);
                } else {
                    Collections.sort(knowledgePlateData, (Comparator<Map>) (o1, o2) -> {
                        Integer id1 = (Integer) o1.get("id");
                        Integer id2 = (Integer) o2.get("id");
                        return id1.compareTo(id2);
                    });
                }

                List<Integer> xAxisData = new LinkedList<>();
                for (int i = 1; i <= knowledgePlateData.size(); i++) {
                    Map<String, Object> item = knowledgePlateData.get(i - 1);
                    item.put("orderNo", i);
                    xAxisData.add(i);
                }
                //每个知识板块的线图数据，包含 legendData ，xAxisData，seriesData，markLineData,topTree,last
                List<Map<String, Object>> knowledgePlateDataMapList = new LinkedList<>();
                for (int i = 1; i <= knowledgePlateList.size(); i++) {
                    String knowledgePlate = knowledgePlateList.get(i - 1);
                    Map<String, Object> knowledgePlateDataMap = new LinkedHashMap<>();
                    String legendData = "知识板块-" + knowledgePlate;
                    knowledgePlateDataMap.put("legendData", legendData);
                    List<BigDecimal> seriesData = new LinkedList<>();
                    List<Map<String, Object>> sortDataList = new LinkedList<>();
                    Set<BigDecimal> diffSet = new LinkedHashSet<>();
                    for (Map<String, Object> item : knowledgePlateData) {
                        BigDecimal scorerate = (BigDecimal) item.get("scorerate_" + i);
                        seriesData.add(scorerate);
                        Map<String, Object> sortMap = new LinkedHashMap<>();
                        sortMap.put("id", item.get("id"));
                        sortMap.put("name", item.get("name"));
                        sortMap.put("scorerate", scorerate);
                        diffSet.add(scorerate);
                        sortDataList.add(sortMap);
                    }
                    knowledgePlateDataMap.put("seriesData", seriesData);
                    BigDecimal markLineData = BigDecimal.ZERO;
                    for (Map<String, Object> temp : knowledgePlateInfo) {
                        String name = (String) temp.get("knowledgeplate");
                        if (knowledgePlate.equals(name)) {
                            markLineData = (BigDecimal) temp.get("scorerate");
                            break;
                        }
                    }
                    knowledgePlateDataMap.put("markLineData", markLineData);
                    //排序前三的
                    Collections.sort(sortDataList, (Comparator<Map>) (o1, o2) -> {
                        BigDecimal scorerate1 = (BigDecimal) o1.get("scorerate");
                        BigDecimal scorerate2 = (BigDecimal) o2.get("scorerate");
                        return scorerate2.compareTo(scorerate1);
                    });
                    getTopTree(knowledgePlateDataMap, sortDataList, diffSet, markLineData);

                    knowledgePlateDataMap.put("xAxisData", xAxisData);
                    knowledgePlateDataMapList.add(knowledgePlateDataMap);
                }

                //表格的数据
                List<Map<String, Object>> knowledgePlatedetailGrid = new LinkedList<>();
                for (Map<String, Object> temp : knowledgePlateData) {
                    Map<String, Object> knowledgePlateDetailMap = new LinkedHashMap<>();
                    knowledgePlateDetailMap.put("id", temp.get("id"));
                    knowledgePlateDetailMap.put("name", temp.get("name"));
                    knowledgePlateDetailMap.put("orderNo", temp.get("orderNo"));
                    List<Map<String, Object>> knowledgePlateDataList = new LinkedList<>();
                    for (int i = 1; i <= knowledgePlateList.size(); i++) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("averagescore", temp.get("averagescore_" + i));
                        map.put("scorerate", temp.get("scorerate_" + i));
                        map.put("topScorerate", temp.get("topScorerate_" + i));
                        knowledgePlateDataList.add(map);
                    }
                    knowledgePlateDetailMap.put("knowledgePlateList", knowledgePlateDataList);
                    knowledgePlatedetailGrid.add(knowledgePlateDetailMap);
                }

                data.put("knowledgePlateList", knowledgePlateList);
                data.put("knowledgePlatedetailGrid", knowledgePlatedetailGrid);
                data.put("knowledgePlateDataMapList", knowledgePlateDataMapList);
            }
            return data;
        }else{
            return null;
        }
    }

    private void getTopTree(Map<String, Object> knowledgePlateDataMap, List<Map<String, Object>> sortDataList, Set<BigDecimal> diffSet, BigDecimal markLineData) {
        //长度大于等于2，并且数据有差异则有如下逻辑
        if(sortDataList.size() >=2 && diffSet.size()>1){
            if(sortDataList.size() == 2){
                knowledgePlateDataMap.put("topThree",sortDataList.get(0));
                Map<String,Object> lastOne = sortDataList.get(1);
                knowledgePlateDataMap.put("lastOne",lastOne);
                BigDecimal scorerate = (BigDecimal) lastOne.get("scorerate");
                BigDecimal diff = markLineData.subtract(scorerate);
                knowledgePlateDataMap.put("diff",diff);
            }else if(sortDataList.size() == 3){
                Map<String,Object> second = sortDataList.get(1);
                Map<String,Object> third = sortDataList.get(2);
                //第二三名进行比较相等则只展示最好的第一名，
                BigDecimal  secondScorerate = (BigDecimal) second.get("scorerate");
                BigDecimal  thirdScorerate = (BigDecimal) third.get("scorerate");
                if(Objects.equals(secondScorerate,thirdScorerate)){
                    knowledgePlateDataMap.put("topThree",sortDataList.get(0));
                    knowledgePlateDataMap.put("lastOne",new LinkedHashMap<>());
                }else{
                    knowledgePlateDataMap.put("topThree",sortDataList.subList(0,2));
                    Map<String,Object> lastOne = sortDataList.get(sortDataList.size()-1);
                    knowledgePlateDataMap.put("lastOne",lastOne);
                    BigDecimal scorerate = (BigDecimal) lastOne.get("scorerate");
                    BigDecimal diff = markLineData.subtract(scorerate);
                    knowledgePlateDataMap.put("diff",diff);
                }
            }else if(sortDataList.size()>=4){
                //第二名和最后一名比较，如果相等，只需要取最好的去第一个，最后一名不要
                Map<String,Object> second = sortDataList.get(1);
                BigDecimal secondScorerate = (BigDecimal) second.get("scorerate");
                Map<String,Object> third = sortDataList.get(2);
                BigDecimal thirdScorerate = (BigDecimal) third.get("scorerate");
                Map<String,Object> last = sortDataList.get(sortDataList.size()-1);
                BigDecimal lastScorerate = (BigDecimal) last.get("scorerate");
                if(Objects.equals(secondScorerate,lastScorerate)){
                    knowledgePlateDataMap.put("topThree",sortDataList.get(0));
                    knowledgePlateDataMap.put("lastOne",new LinkedHashMap<>());
                }else if(Objects.equals(thirdScorerate,lastScorerate)){
                    //第三名和最后一名比较，如果相等则最好的取前两个，最后一名不取
                    knowledgePlateDataMap.put("topThree",sortDataList.subList(0,2));
                    knowledgePlateDataMap.put("lastOne",new LinkedHashMap<>());
                }else{
                    knowledgePlateDataMap.put("topThree",sortDataList.subList(0,3));
                    Map<String,Object> lastOne = sortDataList.get(sortDataList.size()-1);
                    knowledgePlateDataMap.put("lastOne",lastOne);
                    BigDecimal scorerate = (BigDecimal) lastOne.get("scorerate");
                    BigDecimal diff = markLineData.subtract(scorerate);
                    knowledgePlateDataMap.put("diff",diff);
                }
            }
        }else{
            knowledgePlateDataMap.put("topThree",new LinkedList<>());
            knowledgePlateDataMap.put("lastOne",new LinkedHashMap<>());
        }
    }

    private void sortClazz(List<Map<String,Object>> clazzList){
        Collections.sort(clazzList, (x, y) -> {
            String fileA = SafeConverter.toString(x.get("name"));
            String fileB = SafeConverter.toString(y.get("name"));
            char[] arr1 = fileA.toCharArray();
            char[] arr2 = fileB.toCharArray();
            int i = 0, j =0;
            while( i < arr1.length && j < arr2.length){
                if(Character.isDigit(arr1[i]) && Character.isDigit(arr2[j])){
                    String s1 = "",s2 = "";
                    while (i < arr1.length && Character.isDigit(arr1[i])){
                        s1 += arr1[i];
                        i++;
                    }
                    while (j < arr2.length && Character.isDigit(arr2[j])){
                        s2 += arr2[j];
                        j++;
                    }
                    if(Integer.parseInt(s1) > Integer.parseInt(s2)){
                        return 1;
                    }
                    if (Integer.parseInt(s1) < Integer.parseInt(s2)){
                        return -1;
                    }
                }else{
                    if(arr1[i] > arr2[j]){
                        return 1;
                    }
                    if (arr1[i] < arr2[j]){
                        return -1;
                    }
                    i++;
                    j++;
                }
            }
            if (arr1.length == arr2.length){
                return 0;
            }else{
                return arr1.length > arr2.length? 1: -1;
            }
        });
    }
}
