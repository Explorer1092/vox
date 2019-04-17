package com.voxlearning.washington.controller.schoolmaster;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.question.api.constant.NewExamRegionLevel;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.api.entity.NewPaper;
import com.voxlearning.utopia.service.question.api.entity.XxBaseRegion;
import com.voxlearning.utopia.service.question.consumer.NewExamLoaderClient;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.rstaff.consumer.SchoolMasterServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/examReport")
public class ExamReportController extends SchoolMasterBaseController {

    @Inject private RaikouSystem raikouSystem;
    @Inject protected NewExamLoaderClient newExamLoaderClient;
    @Inject private PaperLoaderClient paperLoaderClient;
    @Inject private SchoolMasterServiceClient schoolMasterServiceClient;

    @RequestMapping(value = "loadExamListCondition.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadResearchHomeworkCondition(Model model) {
        MapMessage result = new MapMessage();
        //月份  ，如果当前时间是六月四号，就是包含六月份，如果当前时间是六月三号之前不包含当前月份
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);

        List<Map<String, String>> dateList = new LinkedList<>();
        for (int i = 0; i < 7; i++) {
            String value = DateUtils.dateToString(cal.getTime(), "yyyyMM");
            String text = DateUtils.dateToString(cal.getTime(), "yyyy年MM月");
            Map<String, String> dateMap = new LinkedHashMap<>();
            dateMap.put("name", text);
            dateMap.put("value", value);
            if (value.compareTo("201806") >= 0) {
                dateList.add(dateMap);
            }
            cal.add(Calendar.MONTH, 1);
        }
        result.add("dateList", dateList);

        List<Map<String, String>> gradeList = new LinkedList<>();
        List<String> tempGrade = getGradeList();
        for (int i = 1; i <= tempGrade.size(); i++) {
            Map<String, String> temp = new LinkedHashMap<>();
            temp.put("name", tempGrade.get(i - 1));
            temp.put("value", "" + i);
            gradeList.add(temp);
        }
        result.add("gradeList", gradeList);
        //如果是校长增加学科信息
        ResearchStaff researchStaff = currentResearchStaff();
        if (researchStaff.isPresident()) {
            //学科
            Map<String, String> subjectMap1 = new LinkedHashMap<>();
            subjectMap1.put("name", Subject.ENGLISH.getValue());
            subjectMap1.put("value", Subject.ENGLISH.toString());
            Map<String, String> subjectMap2 = new LinkedHashMap<>();
            subjectMap2.put("name", Subject.MATH.getValue());
            subjectMap2.put("value", Subject.MATH.toString());
            Map<String, String> subjectMap3 = new LinkedHashMap<>();
            subjectMap3.put("name", Subject.CHINESE.getValue());
            subjectMap3.put("value", Subject.CHINESE.toString());
            List<Map<String, String>> subjectList = new LinkedList<>();
            subjectList.add(subjectMap1);
            subjectList.add(subjectMap2);
            subjectList.add(subjectMap3);
            result.add("subjectList", subjectList);
        }
        result.add("result", true);
        return result;
    }


    @RequestMapping(value = "loadExamList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getExamList() {
        MapMessage mapMessage = new MapMessage();
        String dateStr = getRequestString("schoolYearTerm");//和成绩发布时间进行比较
        Integer grade = getRequestInt("grade");
        String subject = getRequestString("subject");
        ResearchStaff researchStaff = currentResearchStaff();
        if (StringUtils.isBlank(subject)) {
            subject = researchStaff.getSubject().name();
        }
        //只有数学才会有两份报告，英语和语文只有我们自己的报告
        List<NewExam> allExamList2 = getExamList2(researchStaff, dateStr, subject, grade);
        List<Map<String, Object>> examList = new LinkedList<>();
        getExamMapList2(examList, dateStr, grade, subject, allExamList2, researchStaff);


        if (RuntimeMode.current().le(Mode.TEST)) {
            Map<String, Object> examInfo = new LinkedHashMap<>();
            examInfo.put("examRegionLevel", NewExamRegionLevel.city);
            examInfo.put("cityCode", 210200);
            examInfo.put("regionLevel", "city");
//        examInfo.put("cityCode",0);
            examInfo.put("regionCode", 0);
            examInfo.put("schoolId", 0);
//        examInfo.put("schoolId",115404);
            examInfo.put("id", "E_10200301480230");
//        examInfo.put("id","E_10300303521063");
            examInfo.put("name", "数学月考");
            examInfo.put("examTime", DateUtils.dateToString(new Date()) + "~" + DateUtils.dateToString(new Date()));  //时间
            examInfo.put("totalScore", 100);  //总分
            examInfo.put("totalNum", 50);      //总题目数
            examInfo.put("subject", Subject.MATH.getId());
            examInfo.put("grade", 3);
            examInfo.put("durationMinutes", 120);    //答卷时长
            examList.add(examInfo);
        } else {
            //按时间倒序
            Collections.sort(examList, new Comparator<Map>() {
                @Override
                public int compare(Map o1, Map o2) {
                    Date examStart1 = (Date) o1.get("examStart");
                    Date examStart2 = (Date) o2.get("examStart");
                    return examStart2.compareTo(examStart1);
                }
            });
            //处理相同考试的多个学校或者多区域，考试名称后面添加学校名称
            Map<String, List<Map<String, Object>>> examListMap = examList.stream().collect(Collectors.groupingBy(m -> SafeConverter.toString(m.get("id"))));
            for (Map.Entry<String, List<Map<String, Object>>> entry : examListMap.entrySet()) {
                String examId = entry.getKey();
                List<Map<String, Object>> tempList = entry.getValue();
                if (tempList.size() > 1) {
                    //存在一个考试多个区域的情况，在考试名称后面追加地区名称
                    for (Map<String, Object> newExam : examList) {
                        String id = (String) newExam.get("id");
                        String name = (String) newExam.get("name");
                        String regionLevel = (String) newExam.get("regionLevel");
                        if (examId.equals(id)) {
                            if ("city".equals(regionLevel)) {
                                Integer cityCode = (Integer) newExam.get("cityCode");
                                ExRegion region = raikouSystem.loadRegion(cityCode);
                                newExam.put("name", name + "(" + region.getCityName() + ")");
                            } else if ("county".equals(regionLevel)) {
                                Integer regionCode = (Integer) newExam.get("regionCode");
                                ExRegion region = raikouSystem.loadRegion(regionCode);
                                newExam.put("name", name + "(" + region.getCountyName() + ")");
                            } else {
                                //学校
                                Long schoolIdtemp = (Long) newExam.get("schoolId");
                                School school = raikouSystem.loadSchool(schoolIdtemp);
                                newExam.put("name", name + "(" + school.getShortName() + ")");
                            }
                        }
                    }
                }
            }
        }

//        if(RuntimeMode.isStaging()){
//            String[] countyExams = new String[]{"E_10300310213139"};
//            for(String examId : countyExams){
//                Map<String,Object> examInfo = new LinkedHashMap<>();
//                examInfo.put("examRegionLevel","county");
//                examInfo.put("cityCode",0);
//                examInfo.put("regionCode",410103);
//                examInfo.put("schoolId",0);
//                NewExam newExam = newExamLoaderClient.load(examId);
//                List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
//                NewExam.EmbedPaper paper = papers.get(0);
//                String paperId = paper.getPaperId();
//                NewPaper newPaper = paperLoaderClient.loadLatestPaperByDocId(paperId);
//                examInfo.put("id",examId);
//                examInfo.put("name",newExam.getName());
//                examInfo.put("examTime", DateUtils.dateToString(newExam.getExamStartAt(),"yyyy-MM-dd HH:mm")+"~"+DateUtils.dateToString(newExam.getExamStopAt(),"yyyy-MM-dd HH:mm"));  //时间
//                examInfo.put("totalScore",newPaper.getTotalScore());  //总分
//                examInfo.put("totalNum",newPaper.getTotalNum());      //总题目数
//                examInfo.put("subject", newExam.getSubject());
//                examInfo.put("grade",newExam.getClazzLevels().get(0));
//                examInfo.put("durationMinutes",newExam.getDurationMinutes());    //答卷时长
//                examList.add(examInfo);
//            }
//        }

        mapMessage.add("result", true);
        checkReportData(examList);
        mapMessage.add("examList", examList);
        return mapMessage;
    }

    private void checkReportData(List<Map<String, Object>> examList) {
        for (Map<String, Object> temp : examList) {

            ResearchStaff researchStaff = currentResearchStaff();
            String regionLevel = "school";
            String regionCode = "";
            if (researchStaff.isPresident()) {
                regionCode = SafeConverter.toString(temp.get("schoolId"));
            }
            String examId = SafeConverter.toString(temp.get("id"));
            Set<Long> cityCodes = researchStaff.getManagedRegion().getCityCodes();//市code
            if (CollectionUtils.isNotEmpty(cityCodes)) {
                regionLevel = "city";
                regionCode = SafeConverter.toString(temp.get("cityCode"));
            }
            Set<Long> regionCodes = researchStaff.getManagedRegion().getAreaCodes();//区code
            if (CollectionUtils.isNotEmpty(regionCodes)) {
                regionLevel = "county";
                regionCode = SafeConverter.toString(temp.get("regionCode"));
            }
            Set<Long> schoolIds = researchStaff.getManagedRegion().getSchoolIds();
            if (CollectionUtils.isNotEmpty(schoolIds)) {
                regionLevel = "school";
                regionCode = SafeConverter.toString(temp.get("schoolId"));
            }
            Map<String, String> paramMap = new HashMap<>();

            String apiURL = "http://10.7.4.240:8116/api/v1/assessmentReport";
            if (RuntimeMode.ge(Mode.STAGING)) {
                apiURL = "http://yqc.17zuoye.net/api/v1/assessmentReport";
            }
            paramMap.put("moduleName", "loadEvaluationStruct");
            paramMap.put("examId", examId);
            paramMap.put("regionLevel", regionLevel);
            paramMap.put("regionCode", regionCode);
            paramMap.put("reportType", "assessmentReport");
            try {
                String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
                AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
                String retStr = response.getResponseString();
                Map dataMap = JsonUtils.fromJson(retStr);

                temp.put("result", dataMap.get("success"));
                temp.put("info", dataMap.get("msg"));
            } catch (Exception e) {
                temp.put("result", false);
                temp.put("info", "请求失败");
            }
        }
    }

    //根据教研员的级别去判断考试是否显示在该列表中
    private void getExamMapList2(List<Map<String, Object>> examList, String dateStr, Integer grade, String subject, List<NewExam> allEewExam, ResearchStaff researchStaff) {
        List<Integer> cityCodes = getCityCodesParam(researchStaff);//市code
        List<Integer> regionCodes = getRegionCodesParam(researchStaff);//区code

        for (NewExam newExam : allEewExam) {
            //成绩发布时间的第二天六点以后数据都跑完了，所以当前时间大于成绩发布时间第二天的六点
            if (newExam.getResultIssueAt() == null) {
                continue;
            }
            Date resultIssueAt = DateUtils.addDays(newExam.getResultIssueAt(), 1);
            String newResultIssueAtStr = DateUtils.dateToString(resultIssueAt, DateUtils.FORMAT_SQL_DATE) + " 6:00:00";
            Date newResultIssueAt = DateUtils.stringToDate(newResultIssueAtStr, DateUtils.FORMAT_SQL_DATETIME);
            if (new Date().before(newResultIssueAt)) {
                continue;
            }
            //界面上赛选的时间跟考试开始时间进行比较，符合筛选时间的
            String targetDate = DateUtils.dateToString(newExam.getExamStartAt(), "yyyyMM");
            if (!targetDate.equals(dateStr)) {
                continue;
            }
            if (newExam.getClazzLevels().size() > 1) {
                continue;
            }
            List<NewExam.EmbedPaper> papers = newExam.obtainEmbedPapers();
            NewExam.EmbedPaper paper = papers.get(0);
            String paperId = paper.getPaperId();
            NewPaper newPaper = paperLoaderClient.loadLatestPaperByDocId(paperId);
            //根据教研员的级别去判断考试列表
            //市教研员,只需要匹配考试中配置的区域（省，市）
            //区教研员,只需要匹配考试中配置的区域（省，市，区），
            //街道教研员,只需要匹配考试中配置的区域（省，市，区，校）
            //校长，只需要匹配考试中配置的区域（省，市，区，校），在查询考试列表中，查询到的考试我添加的区域都放在各自的级别中
            Map<String, Object> examInfo = new LinkedHashMap<>();
            if (researchStaff.isPresident()) {
                //校长
                List<Long> examSchoolIds = newExam.getSchoolIds();
                Long examSchoolId = newExam.getSchoolIds().get(examSchoolIds.size() - 1);
                Long schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
                if (Objects.equals(examSchoolId, schoolId)) {
                    examInfo.put("cityCode", 0);
                    examInfo.put("name", newExam.getName());
                    examInfo.put("regionCode", 0);
                    examInfo.put("schoolId", schoolId);
                    examInfo.put("regionLevel", "school");
                } else {
                    continue;
                }
            } else {
                //市教研员
                List<XxBaseRegion> regions = newExam.getRegions();
                XxBaseRegion xxBaseRegion = regions.get(regions.size() - 1);
                if (CollectionUtils.isNotEmpty(cityCodes)) {
                    Integer cityCode = xxBaseRegion.getCityId();
                    if (cityCodes.contains(cityCode)) {
                        examInfo.put("cityCode", cityCode);
                        examInfo.put("name", newExam.getName());
                        examInfo.put("regionCode", 0);
                        examInfo.put("schoolId", 0);
                        examInfo.put("regionLevel", "city");
                    } else {
                        continue;
                    }
                } else if (CollectionUtils.isNotEmpty(regionCodes)) {
                    Integer regionCode = xxBaseRegion.getRegionId();
                    if (regionCodes.contains(regionCode)) {
                        examInfo.put("cityCode", 0);
                        examInfo.put("name", newExam.getName());
                        examInfo.put("regionCode", regionCode);
                        examInfo.put("schoolId", 0);
                        examInfo.put("regionLevel", "county");
                    } else {
                        continue;
                    }
                } else {
                    //街道教研员
                    List<Long> examSchoolIds = newExam.getSchoolIds();
                    Long examSchoolId = newExam.getSchoolIds().get(examSchoolIds.size() - 1);
                    Set<Long> schoolIds = researchStaff.getManagedRegion().getSchoolIds();
                    if (schoolIds.contains(examSchoolId)) {
                        examInfo.put("cityCode", 0);
                        examInfo.put("name", newExam.getName());
                        examInfo.put("regionCode", 0);
                        examInfo.put("schoolId", examSchoolId);
                        examInfo.put("regionLevel", "school");
                    } else {
                        continue;
                    }
                }
            }
            String newExamRegionLevel = newExam.getRegionLevel().name();
            if (newExam.getRegionLevel() == NewExamRegionLevel.country) {
                newExamRegionLevel = "county";
            }
            examInfo.put("examRegionLevel", newExamRegionLevel);
            examInfo.put("id", newExam.getId());
            examInfo.put("name", newExam.getName());
            examInfo.put("examTime", DateUtils.dateToString(newExam.getExamStartAt(), "yyyy-MM-dd HH:mm") + "~" + DateUtils.dateToString(newExam.getExamStopAt(), "yyyy-MM-dd HH:mm"));  //时间
            examInfo.put("totalScore", newPaper.getTotalScore());  //总分
            examInfo.put("totalNum", newPaper.getTotalNum());      //总题目数
            examInfo.put("subject", newExam.getSubject());
            examInfo.put("durationMinutes", newExam.getDurationMinutes());    //答卷时长
            examInfo.put("grade", newExam.getClazzLevels().get(0));
            examInfo.put("examStart", newExam.getExamStartAt());
            examList.add(examInfo);
        }
    }

    private List<NewExam> getExamList2(ResearchStaff researchStaff, String dateStr, String subject, Integer grade) {
        //市级考试，要生成市级报告，区级报告，校级报告，区级考试，要生成区级报告，校级报告，校级考试，就生成校级报告
        //所以校级教研员要查看该校市级考试的校级报告，区级考试的校级报告，当然还有校级考试的校级报告，依次类推......
        Date date = DateUtils.stringToDate(dateStr, "yyyyMM");
        Subject subject1 = Subject.safeParse(subject);
        List<NewExam> allNewExam = new ArrayList<>();
        if (researchStaff != null && researchStaff.isPresident()) {
            //校长
            Long schoolId = researchStaff.getManagedRegion().getSchoolIds().iterator().next();
            School school = raikouSystem.loadSchool(schoolId);
            ExRegion exRegion = new ExRegion();
            //查询校级考试报告
            List<NewExam> schoolExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, school.getId(), subject1, exRegion, grade);
            for (NewExam temp : schoolExamList) {
                temp.getSchoolIds().add(schoolId);
            }
            allNewExam.addAll(schoolExamList);
            //只有数学科目才有展示下级报告
            if (Subject.MATH == subject1) {
                //区级考试考试报告
                ExRegion exRegion1 = new ExRegion();
                exRegion1.setCountyCode(school.getRegionCode());
                List<NewExam> countyExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion1, grade);
                for (NewExam temp : countyExamList) {
                    temp.getSchoolIds().add(schoolId);
                }
                allNewExam.addAll(countyExamList);
                //市级考试考试报告
                ExRegion exRegion2 = new ExRegion();
                ExRegion cityExRegion = raikouSystem.loadRegion(school.getRegionCode());
                exRegion2.setCityCode(cityExRegion.getCityCode());
                List<NewExam> cityExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion2, grade);
                for (NewExam temp : cityExamList) {
                    temp.getSchoolIds().add(schoolId);
                }
                //省级考试考试报告
                ExRegion exRegion3 = new ExRegion();
                exRegion3.setProvinceCode(cityExRegion.getProvinceCode());
                List<NewExam> provinceExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion3, grade);
                for (NewExam temp : provinceExamList) {
                    temp.getSchoolIds().add(schoolId);
                }
                allNewExam.addAll(provinceExamList);
            }
        } else {
            //市教研员的逻辑，区教研员的逻辑
            //教研员
            List<Integer> cityCodes = getCityCodesParam(researchStaff);//市code
            List<Integer> regionCodes = getRegionCodesParam(researchStaff);//区code
            Set<Long> schoolIds = researchStaff.getManagedRegion().getSchoolIds();//配置的多学校
            //市教研员查询市级考试和省级的考试
            ExRegion exRegion = new ExRegion();
            if (CollectionUtils.isNotEmpty(cityCodes)) {
                for (Integer cityCode : cityCodes) {
                    exRegion.setCityCode(cityCode);
                    List<NewExam> cityExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion, grade);
                    for (NewExam temp : cityExamList) {
                        XxBaseRegion xxBaseRegion = new XxBaseRegion();
                        xxBaseRegion.setCityId(cityCode);
                        temp.getRegions().add(xxBaseRegion);
                    }
                    allNewExam.addAll(cityExamList);

                    if (Subject.MATH == subject1) {
                        ExRegion provinceExRegion = raikouSystem.loadRegion(cityCode);
                        ExRegion exRegion2 = new ExRegion();
                        exRegion2.setProvinceCode(provinceExRegion.getProvinceCode());
                        List<NewExam> provinceExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion2, grade);
                        for (NewExam temp : provinceExamList) {
                            XxBaseRegion xxBaseRegion = new XxBaseRegion();
                            xxBaseRegion.setCityId(cityCode);
                            temp.getRegions().add(xxBaseRegion);
                        }
                        allNewExam.addAll(provinceExamList);
                    }
                }
            } else if (CollectionUtils.isNotEmpty(regionCodes)) {
                for (Integer regionCode : regionCodes) {
                    exRegion.setCountyCode(regionCode);
                    List<NewExam> regionExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion, grade);
                    for (NewExam temp : regionExamList) {
                        XxBaseRegion xxBaseRegion = new XxBaseRegion();
                        xxBaseRegion.setRegionId(regionCode);
                        temp.getRegions().add(xxBaseRegion);
                    }
                    allNewExam.addAll(regionExamList);

                    if (Subject.MATH == subject1) {
                        //还要查询市级考试的报告
                        ExRegion exRegion1 = new ExRegion();
                        ExRegion regionExRegion = raikouSystem.loadRegion(regionCode);
                        exRegion1.setCityCode(regionExRegion.getCityCode());
                        List<NewExam> cityExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion1, grade);
                        for (NewExam temp : cityExamList) {
                            XxBaseRegion xxBaseRegion = new XxBaseRegion();
                            xxBaseRegion.setRegionId(regionCode);
                            temp.getRegions().add(xxBaseRegion);
                        }
                        allNewExam.addAll(cityExamList);
                        //还要查询省级考试的报告
                        ExRegion exRegion2 = new ExRegion();
                        exRegion2.setProvinceCode(regionExRegion.getProvinceCode());
                        List<NewExam> provinceExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion2, grade);
                        for (NewExam temp : provinceExamList) {
                            XxBaseRegion xxBaseRegion = new XxBaseRegion();
                            xxBaseRegion.setRegionId(regionCode);
                            temp.getRegions().add(xxBaseRegion);
                        }
                        allNewExam.addAll(provinceExamList);
                    }
                }
            } else if (CollectionUtils.isNotEmpty(schoolIds)) {
                Iterator<Long> schoolIdIts = schoolIds.iterator();
                while (schoolIdIts.hasNext()) {
                    Long schoolId = schoolIdIts.next();
                    List<NewExam> newExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, schoolId, subject1, exRegion, grade);
                    for (NewExam temp : newExamList) {
                        temp.getSchoolIds().add(schoolId);
                    }
                    allNewExam.addAll(newExamList);

                    if (Subject.MATH == subject1) {
                        School school = raikouSystem.loadSchool(schoolId);
                        //区级考试考试报告
                        ExRegion exRegion1 = new ExRegion();
                        exRegion1.setCountyCode(school.getRegionCode());
                        List<NewExam> countyExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion1, grade);
                        for (NewExam temp : countyExamList) {
                            temp.getSchoolIds().add(schoolId);
                        }
                        allNewExam.addAll(countyExamList);
                        //市级考试考试报告
                        ExRegion exRegion2 = new ExRegion();
                        ExRegion cityExRegion = raikouSystem.loadRegion(school.getRegionCode());
                        exRegion2.setCityCode(cityExRegion.getCityCode());
                        List<NewExam> cityExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion2, grade);
                        for (NewExam temp : cityExamList) {
                            temp.getSchoolIds().add(schoolId);
                        }
                        allNewExam.addAll(cityExamList);
                        //省级考试考试报告
                        ExRegion exRegion3 = new ExRegion();
                        exRegion3.setProvinceCode(cityExRegion.getProvinceCode());
                        List<NewExam> provinceExamList = newExamLoaderClient.crmGetExamByPage(NewExamType.unify, date, null, subject1, exRegion3, grade);
                        for (NewExam temp : provinceExamList) {
                            temp.getSchoolIds().add(schoolId);
                        }
                        allNewExam.addAll(provinceExamList);
                    }
                }
            }
        }
        return allNewExam;
    }

    @RequestMapping(value = "loadExamSurvey.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadExamSurvey() {
        MapMessage result = new MapMessage();
        try {
            Integer cityCode = getRequestInt("cityCode");
            cityCode = cityCode == 0 ? null : cityCode;
            Integer regionCode = getRequestInt("regionCode");
            regionCode = regionCode == 0 ? null : regionCode;
            Long schoolId = getRequestLong("schoolId");
            schoolId = schoolId == 0 ? null : schoolId;
            String examId = getRequestString("examId");
            NewExam newExam = newExamLoaderClient.load(examId);

            String name = "";
            NewExamRegionLevel dataLevel = null;
            if (RuntimeMode.current().le(Mode.TEST)) {
                name = "测试报告";
                result.add("name", name);
                dataLevel = NewExamRegionLevel.city;
//                result.add("examRegionLevel", newExam.getRegionLevel().getDesc());//考试范围
//                int grade = newExam.getClazzLevels().get(0);
//                result.add("grade",grade);             //年级
//                result.add("subject", newExam.getSubject().getValue());    //科目
//                result.add("examTime",DateUtils.dateToString(newExam.getExamStartAt())+"~"+DateUtils.dateToString(newExam.getExamStopAt()));   //考试时间
//                result.add("durationMinutes",newExam.getDurationMinutes());    //答卷时长
            } else {
                name = newExam.getName();
                dataLevel = newExam.getRegionLevel();
                result.add("name", name);
                result.add("examRegionLevel", newExam.getRegionLevel().getDesc());//考试范围
                int grade = newExam.getClazzLevels().get(0);
                result.add("grade", grade);             //年级
                result.add("subject", newExam.getSubject().getValue());    //科目
                result.add("examTime", DateUtils.dateToString(newExam.getExamStartAt(), "yyyy-MM-dd HH:mm") + "~" + DateUtils.dateToString(newExam.getExamStopAt(), "yyyy-MM-dd HH:mm"));   //考试时间
                result.add("durationMinutes", newExam.getDurationMinutes());    //答卷时长
            }

            result.add("dataLevel", dataLevel);
            //显示的区域，区，学校，班级
            String viewRegionLevel = "区/县";
            if (cityCode != null) {
                viewRegionLevel = "区/县";
            } else if (regionCode != null) {
                viewRegionLevel = "学校";
            } else {
                viewRegionLevel = "班级";
            }

            result.add("examFullName", name);
            result.add("viewRegionLevel", viewRegionLevel);
            Map<String, Object> data = schoolMasterServiceClient.loadExamSurvey(cityCode, regionCode, schoolId, examId);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }
            Map<String, Object> examSurvey = (Map<String, Object>) data.get("examSurvey");
            result.add("examSurvey", examSurvey);

            List<Map<String, Object>> examSurveyDetail = (List<Map<String, Object>>) data.get("examSurveyDetail");

            result.add("result", true);
            result.add("examSurveyDetail", examSurveyDetail);
        } catch (Exception e) {
            logger.error("获得模考统测概况数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    @RequestMapping(value = "loadExamScoreState.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadExamScoreState() {
        MapMessage result = new MapMessage();
        try {
            Integer cityCode = getRequestInt("cityCode");
            cityCode = cityCode == 0 ? null : cityCode;
            Integer regionCode = getRequestInt("regionCode");
            regionCode = regionCode == 0 ? null : regionCode;
            Long schoolId = getRequestLong("schoolId");
            schoolId = schoolId == 0 ? null : schoolId;
            String examId = getRequestString("examId");
            Map<String, Object> data = schoolMasterServiceClient.loadExamScoreState(cityCode, regionCode, schoolId, examId);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }
            NewExam newExam = newExamLoaderClient.load(examId);
            NewExamRegionLevel dataLevel = null;
            if (RuntimeMode.current().le(Mode.TEST)) {
                dataLevel = NewExamRegionLevel.city;
            } else {
                dataLevel = newExam.getRegionLevel();
            }
            result.add("dataLevel", dataLevel);
            //显示的区域，区，学校，班级
            String viewRegionLevel = "区/县";
            if (regionCode != null) {
                viewRegionLevel = "学校";
            } else if (schoolId != null) {
                viewRegionLevel = "班级";
            }
            result.add("viewRegionLevel", viewRegionLevel);
            Map<String, Double> wholeScore = (Map<String, Double>) data.get("wholeScore");
            result.add("wholeScore", wholeScore);

            //详细表格数据
            result.add("wholeScoreDetail", data.get("wholeScoreDetail"));
            //得分率线图
            result.add("legendData", data.get("legendData"));
            result.add("xAxisData", data.get("xAxisData"));
            result.add("seriesData", data.get("seriesData"));
            result.add("topThreeData", data.get("topThreeData"));
            result.add("lastData", data.get("lastData"));

            //散点图数据
            result.add("scatterPointData", data.get("scatterPointData"));
            result.add("scatterPointMaxMap", data.get("scatterPointMaxMap"));
            result.add("scatterPointMinMap", data.get("scatterPointMinMap"));
            result.add("coefficient", data.get("coefficient"));

            result.add("result", true);
        } catch (Exception e) {
            logger.error("获得模考统测得分状况数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    @RequestMapping(value = "loadStudyLevelInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadStudyLevelInfo() {
        MapMessage result = new MapMessage();
        try {
            Integer cityCode = getRequestInt("cityCode");
            cityCode = cityCode == 0 ? null : cityCode;
            Integer regionCode = getRequestInt("regionCode");
            regionCode = regionCode == 0 ? null : regionCode;
            Long schoolId = getRequestLong("schoolId");
            schoolId = schoolId == 0 ? null : schoolId;
            String examId = getRequestString("examId");
            Map<String, Object> data = schoolMasterServiceClient.loadStudyLevelInfo(cityCode, regionCode, schoolId, examId);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }
            //显示的区域，区，学校，班级
            String viewRegionLevel = "区/县";
            if (regionCode != null) {
                viewRegionLevel = "学校";
            } else if (schoolId != null) {
                viewRegionLevel = "班级";
            }
            result.add("viewRegionLevel", viewRegionLevel);
            NewExam newExam = newExamLoaderClient.load(examId);
            NewExamRegionLevel dataLevel = null;
            if (RuntimeMode.current().le(Mode.TEST)) {
                dataLevel = NewExamRegionLevel.city;
            } else {
                dataLevel = newExam.getRegionLevel();
            }
            result.add("dataLevel", dataLevel);
            List<Map<String, Object>> studyLevelInfo = (List<Map<String, Object>>) data.get("studyLevelInfo");
            //整体表格数据
            result.add("studyLevelInfo", studyLevelInfo);
            //整体合格率计
            BigDecimal wholeQualifiledRatio = (BigDecimal) data.get("wholeQualifiledRatio");
            result.add("wholeQualifiledRatio", wholeQualifiledRatio);

            //获得明细表格数据
            result.add("gridDataList", data.get("gridDataList"));
            //整体柱图
            result.add("wholeBarMap", data.get("wholeBarMap"));
            //优秀率柱图
            result.add("excellentBarMap", data.get("excellentBarMap"));
            //良好率柱图
            result.add("excellgoodBarMap", data.get("excellgoodBarMap"));
            //合格率柱图
            result.add("excellgoodqulifiledBarMap", data.get("excellgoodqulifiledBarMap"));
            //未合格率
            result.add("unqulifiledBarMap", data.get("unqulifiledBarMap"));
            result.add("result", true);
        } catch (Exception e) {
            logger.error("获得模考统测散点数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    @RequestMapping(value = "loadSubjectAbilityInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadSubjectAbilityInfo() {
        MapMessage result = new MapMessage();
        try {
            Integer cityCode = getRequestInt("cityCode");
            cityCode = cityCode == 0 ? null : cityCode;
            Integer regionCode = getRequestInt("regionCode");
            regionCode = regionCode == 0 ? null : regionCode;
            Long schoolId = getRequestLong("schoolId");
            schoolId = schoolId == 0 ? null : schoolId;
            String examId = getRequestString("examId");
            Map<String, Object> data = schoolMasterServiceClient.loadSubjectAbilityInfo(cityCode, regionCode, schoolId, examId);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }
            //显示的区域，区，学校，班级
            String viewRegionLevel = "区/县";
            if (regionCode != null) {
                viewRegionLevel = "学校";
            } else if (schoolId != null) {
                viewRegionLevel = "班级";
            }
            result.add("viewRegionLevel", viewRegionLevel);
            NewExam newExam = newExamLoaderClient.load(examId);
            NewExamRegionLevel dataLevel = null;
            if (RuntimeMode.current().le(Mode.TEST)) {
                dataLevel = NewExamRegionLevel.city;
            } else {
                dataLevel = newExam.getRegionLevel();
            }
            result.add("dataLevel", dataLevel);
            //表格数据
            List<Map<String, Object>> subjectAbilityInfo = (List<Map<String, Object>>) data.get("subjectAbilityInfo");
            result.add("subjectAbilityInfo", subjectAbilityInfo);
            //最大和最小值
            result.add("subjectAbilityMax", data.get("subjectAbilityMax"));
            result.add("subjectAbilityMin", data.get("subjectAbilityMin"));
            if (subjectAbilityInfo.size() > 2) {
                //雷达图
                Map<String, Object> radarMap = new LinkedHashMap<>();
                String legendData = "学科能力";
                radarMap.put("legendData", legendData);
                List<String> indicatorData = new LinkedList<>();
                List<Double> seriesData = new LinkedList<>();
                for (Map<String, Object> temp : subjectAbilityInfo) {
                    String subjectability = (String) temp.get("subjectability");
                    indicatorData.add(subjectability);
                    BigDecimal scoreRate = (BigDecimal) temp.get("scorerate");
                    seriesData.add(scoreRate.doubleValue());
                }
                radarMap.put("indicatorData", indicatorData);
                radarMap.put("seriesData", seriesData);
                result.add("radarMap", radarMap);
            } else {
                //柱图
                Map<String, Object> barMap = new LinkedHashMap<>();
                barMap.put("legendData", "学科能力");
                List<String> xAxisData = new LinkedList<>();
                List<Double> seriesData = new LinkedList<>();
                for (Map<String, Object> temp : subjectAbilityInfo) {
                    xAxisData.add((String) temp.get("subjectability"));
                    BigDecimal scoreRate = (BigDecimal) temp.get("scorerate");
                    seriesData.add(scoreRate.doubleValue());
                }
                barMap.put("xAxisData", xAxisData);
                barMap.put("seriesData", seriesData);
                result.add("barMap", barMap);
            }

            //明细表格数据
            Map<String, Object> subjectAbilityGrid = new LinkedHashMap<>();
            subjectAbilityGrid.put("gridHead", data.get("subjectAbilityList"));
            subjectAbilityGrid.put("gridData", data.get("subjectAbilitydetailGrid"));
            result.add("subjectAbilityGrid", subjectAbilityGrid);
            //各学科能力线图数据列表
            result.add("subjectAbilityDataMapList", data.get("subjectAbilityDataMapList"));
            result.add("result", true);
        } catch (Exception e) {
            logger.error("获得模考学科能力数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

    @RequestMapping(value = "loadKnowledgePlateInfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map loadKnowledgePlateInfo() {
        MapMessage result = new MapMessage();
        try {
            Integer cityCode = getRequestInt("cityCode");
            cityCode = cityCode == 0 ? null : cityCode;
            Integer regionCode = getRequestInt("regionCode");
            regionCode = regionCode == 0 ? null : regionCode;
            Long schoolId = getRequestLong("schoolId");
            schoolId = schoolId == 0 ? null : schoolId;
            String examId = getRequestString("examId");
            Map<String, Object> data = schoolMasterServiceClient.loadKnowledgePlateInfo(cityCode, regionCode, schoolId, examId);
            if (data == null) {
                result.add("result", false);
                result.add("info", "暂无数据");
                return result;
            }
            //显示的区域，区，学校，班级
            String viewRegionLevel = "区/县";
            if (regionCode != null) {
                viewRegionLevel = "学校";
            } else if (schoolId != null) {
                viewRegionLevel = "班级";
            }
            result.add("viewRegionLevel", viewRegionLevel);
            NewExam newExam = newExamLoaderClient.load(examId);
            NewExamRegionLevel dataLevel = null;
            if (RuntimeMode.current().le(Mode.TEST)) {
                dataLevel = NewExamRegionLevel.city;
            } else {
                dataLevel = newExam.getRegionLevel();
            }
            result.add("dataLevel", dataLevel);
            //表格数据
            List<Map<String, Object>> knowledgePlateInfo = (List<Map<String, Object>>) data.get("knowledgePlateInfo");
            result.add("knowledgePlateInfo", knowledgePlateInfo);
            //最大和最小值
            result.add("knowledgePlateMax", data.get("knowledgePlateMax"));
            result.add("knowledgePlateMin", data.get("knowledgePlateMin"));
            if (knowledgePlateInfo.size() > 2) {
                //雷达图
                Map<String, Object> radarMap = new LinkedHashMap<>();
                String legendData = "知识板块";
                radarMap.put("legendData", legendData);
                List<String> indicatorData = new LinkedList<>();
                List<Double> seriesData = new LinkedList<>();
                for (Map<String, Object> temp : knowledgePlateInfo) {
                    String knowledgePlate = (String) temp.get("knowledgeplate");
                    indicatorData.add(knowledgePlate);
                    BigDecimal scoreRate = (BigDecimal) temp.get("scorerate");
                    seriesData.add(scoreRate.doubleValue());
                }
                radarMap.put("indicatorData", indicatorData);
                radarMap.put("seriesData", seriesData);
                result.add("radarMap", radarMap);
            } else {
                //柱图
                Map<String, Object> barMap = new LinkedHashMap<>();
                barMap.put("legendData", "知识板块");
                List<String> xAxisData = new LinkedList<>();
                List<Double> seriesData = new LinkedList<>();
                for (Map<String, Object> temp : knowledgePlateInfo) {
                    xAxisData.add((String) temp.get("knowledgeplate"));
                    BigDecimal scoreRate = (BigDecimal) temp.get("scorerate");
                    seriesData.add(scoreRate.doubleValue());
                }
                barMap.put("xAxisData", xAxisData);
                barMap.put("seriesData", seriesData);
                result.add("barMap", barMap);
            }
            //明细表格数据
            Map<String, Object> knowledgePlateGrid = new LinkedHashMap<>();
            knowledgePlateGrid.put("gridHead", data.get("knowledgePlateList"));
            knowledgePlateGrid.put("gridData", data.get("knowledgePlatedetailGrid"));
            result.add("knowledgePlateGrid", knowledgePlateGrid);
            //各知识板块线图数据列表
            result.add("knowledgePlateDataMapList", data.get("knowledgePlateDataMapList"));

            result.add("result", true);
        } catch (Exception e) {
            logger.error("获得模考知识板块数据异常", e);
            result.add("result", false);
            result.add("info", "暂无数据");
        }
        return result;
    }

}
