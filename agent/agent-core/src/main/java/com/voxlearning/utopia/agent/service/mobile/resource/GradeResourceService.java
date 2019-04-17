package com.voxlearning.utopia.agent.service.mobile.resource;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.athena.LoadParentServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.ParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.clazz.ClassOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.clazz.ClassOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.clazz.ClassParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.grade.GradeOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.grade.GradeOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.grade.GradeParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.group.GroupOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.group.GroupOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.teacher.TeacherOnlineIndicator;
import com.voxlearning.utopia.agent.constants.AgentTagTargetType;
import com.voxlearning.utopia.agent.persist.entity.AgentHiddenTeacher;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTag;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.mobile.AgentHiddenTeacherService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.view.grade.*;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.UserAuthQueryServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 年级班级业务
 *
 * @author song.wang
 * @date 2018/1/31
 */
@Named
public class GradeResourceService extends AbstractAgentService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private PerformanceService performanceService;
    @Inject
    private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private GroupLoaderClient groupLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private AgentHiddenTeacherService agentHiddenTeacherService;
    @Inject
    private LoadNewSchoolServiceClient loadNewSchoolServiceClient;
    @Inject
    private UserAuthQueryServiceClient userAuthQueryServiceClient;
    @Inject
    private LoadParentServiceClient loadParentServiceClient;
    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Inject private RaikouSDK raikouSDK;
    @Inject
    private AgentTagService agentTagService;

    /**
     * 获取各年级的班级信息
     *
     * @param school school
     * @param mode   1:online  3:家长
     * @return 年级班级数据
     */
    public List<Object> generateGradeClassInfo(School school, Integer mode) {

        // 获取学校的年级分布
        List<Integer> gradeList = getGradeDistribute(school);
        List<Object> resultList = new ArrayList<>();

        // 取到年级规模
        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(school.getId())
                .getUninterruptibly();
        EduSystemType eduSystemType = getSchoolEduSystem(school);

        // 获取学校下的所有班级
        List<Clazz> classList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .toList();
        // 过滤掉disabled的数据和毕业班
        classList = classList.stream().filter(p -> !p.isDisabledTrue() && p.getClazzLevel() != ClazzLevel.INFANT_GRADUATED && p.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED && p.getClazzLevel() != ClazzLevel.MIDDLE_GRADUATED)
                .collect(Collectors.toList());

        // 过滤掉没有班组的班级
        Set<Long> allClassIds = classList.stream().map(Clazz::getId).collect(Collectors.toSet());
        Map<Long, List<Group>> classGroupMap = asyncGroupServiceClient.getAsyncGroupService().loadGroupsByClazzIds(allClassIds).getUninterruptibly();
        Set<Long> classIds = classGroupMap.values().stream().flatMap(List::stream).map(Group::getClazzId).collect(Collectors.toSet());

        classList = classList.stream().filter(p -> classIds.contains(p.getId())).collect(Collectors.toList());


        // 根据年级分组
        Map<ClazzLevel, List<Clazz>> gradeClassMap = classList.stream().collect(Collectors.groupingBy(Clazz::getClazzLevel, Collectors.toList()));


        Map<Long, Long> allGroupTeacherMap = new HashMap<>();

        // 获取各个班级中部门和老师的对应关系
        Map<Long, Map<Long, Long>> classGroupTeacherMap = fetchGroupTeacherDataByClassIds(classIds, false);

        classGroupTeacherMap.forEach((k, v) -> {
            v.forEach(allGroupTeacherMap::put);
        });
        Set<Long> allTeacherIds = new HashSet<>(allGroupTeacherMap.values());
        Map<Long, Teacher> allTeacherMap = teacherLoaderClient.loadTeachers(allTeacherIds);

        Integer day = performanceService.lastSuccessDataDay();

        GradeOnlineIndicator gradeOnlineIndicator = loadNewSchoolServiceClient.loadGradeOnlineIndicator(school.getId(), day);
        Map<Long, ClassOnlineIndicator> classOnlineIndicatorMap = loadNewSchoolServiceClient.loadClassOnlineIndicator(classIds, day);
        Map<Long, GroupOnlineIndicator> groupOnlineIndicatorMap = new HashMap<>();
        Map<Long, ClassParentIndicator> classParentIndicatorMap = new HashMap<>();
        GradeParentIndicator gradeParentIndicator = null;
        if (mode == 1) {
            groupOnlineIndicatorMap.putAll(loadNewSchoolServiceClient.loadGroupOnlineIndicator(allGroupTeacherMap.keySet(), day));
        }
        if (mode == 3) {
            gradeParentIndicator = loadParentServiceClient.loadGradeParentIndicator(school.getId(), day);
            classParentIndicatorMap.putAll(loadParentServiceClient.loadClassParentIndicator(classIds, day));
        }

        for (Integer p : gradeList) {
            ClazzLevel grade = ClazzLevel.parse(p);
            List<Clazz> gradeClassList = gradeClassMap.get(grade);

            Grade17InfoView gradeInfo = new Grade17InfoView();
            gradeInfo.setGrade(p);
            gradeInfo.setGradeName(grade.getDescription());
            gradeInfo.setClassList(new ArrayList<>());
            Integer gradeStudentNum = 0;
            if (extInfo != null) {
                gradeStudentNum = extInfo.fetchGradeStudentNum(grade, eduSystemType);
            }
            gradeInfo.setStuScale(SafeConverter.toInt(gradeStudentNum));

            if (gradeOnlineIndicator != null) {
                OnlineIndicator onlineIndicator = gradeOnlineIndicator.fetchSumData(ClazzLevel.parse(p));
                gradeInfo.setRegStuCount(onlineIndicator.getRegStuCount() != null ? onlineIndicator.getRegStuCount() : 0);
                gradeInfo.setAuStuCount(onlineIndicator.getAuStuCount() != null ? onlineIndicator.getAuStuCount() : 0);
            }

            if (gradeParentIndicator != null) {
                ParentIndicator parentIndicator = gradeParentIndicator.fetchSumData(ClazzLevel.parse(p));
                if (parentIndicator != null) {
                    gradeInfo.setBindParentStuNum(SafeConverter.toInt(parentIndicator.getBindParentStuNum()));
                }
                ParentIndicator monthParentIndicator = gradeParentIndicator.fetchMonthData(ClazzLevel.parse(p));
                if (monthParentIndicator != null) {
                    gradeInfo.setParentStuActiveSettlementNum(SafeConverter.toInt(monthParentIndicator.getParentStuActiveSettlementNum()));
                }
            }

            // 设置班级信息
            if (CollectionUtils.isNotEmpty(gradeClassList)) {
                for (Clazz clazz : gradeClassList) {
                    Map<Long, Long> groupTeacherMap = new HashMap<>();
                    Map<Long, Long> tmpGroupTeacherMap = classGroupTeacherMap.get(clazz.getId());
                    if (MapUtils.isNotEmpty(tmpGroupTeacherMap)) {
                        tmpGroupTeacherMap.forEach((k, v) -> {
                            if (this.isRealTeacher(v)) {
                                groupTeacherMap.put(k, v);
                            }
                        });
                    }
                    //累计班级指标
                    OnlineIndicator sumOnlineIndicator = null;
                    //本月班级指标
                    OnlineIndicator monthOnlineIndicator = null;
                    ClassOnlineIndicator classOnlineIndicator = classOnlineIndicatorMap.get(clazz.getId());
                    if (null != classOnlineIndicator) {
                        sumOnlineIndicator = classOnlineIndicator.fetchSumData();
                        monthOnlineIndicator = classOnlineIndicator.fetchMonthData();
                    }
                    // 班级里面注册学生数为 0 且 非隐藏老师中没有真老师的情况， 不展示改班级信息
                    if (MapUtils.isEmpty(groupTeacherMap) && (sumOnlineIndicator == null || sumOnlineIndicator.getRegStuCount() == null || sumOnlineIndicator.getRegStuCount() == 0)) {
                        continue;
                    }

                    GradeClass17InfoView classInfo = new GradeClass17InfoView();
                    classInfo.setClassId(clazz.getId());
                    classInfo.setClassName(clazz.getClassName());

                    //设置学科和老师的对应关系
                    Map<Subject, Map<String, Object>> subjectTeacherMap = fetchSubjectTeacher(groupTeacherMap);
                    // 设置老师数据
                    if (subjectTeacherMap.containsKey(Subject.ENGLISH)) {
                        classInfo.setHasEngTeacher(true);
                        Map<String, Object> teacherMap = subjectTeacherMap.get(Subject.ENGLISH);
                        classInfo.setEngTeacherId(SafeConverter.toLong(teacherMap.get("teacherId")));
                        classInfo.setEngTeacherName(SafeConverter.toString(teacherMap.get("teacherName")));
                        //该班级是否有认证英语老师
                        if (SafeConverter.toBoolean(teacherMap.get("isAuth"))) {
                            classInfo.setHasAuthEngTeacher(true);
                        }
                    }
                    if (subjectTeacherMap.containsKey(Subject.MATH)) {
                        classInfo.setHasMathTeacher(true);
                        Map<String, Object> teacherMap = subjectTeacherMap.get(Subject.MATH);
                        classInfo.setMathTeacherId(SafeConverter.toLong(teacherMap.get("teacherId")));
                        classInfo.setMathTeacherName(SafeConverter.toString(teacherMap.get("teacherName")));
                        //该班级是否有认证数学老师
                        if (SafeConverter.toBoolean(teacherMap.get("isAuth"))) {
                            classInfo.setHasAuthMathTeacher(true);
                        }
                    }
                    if (subjectTeacherMap.containsKey(Subject.CHINESE)) {
                        classInfo.setHasChnTeacher(true);
                        Map<String, Object> teacherMap = subjectTeacherMap.get(Subject.CHINESE);
                        classInfo.setChnTeacherId(SafeConverter.toLong(teacherMap.get("teacherId")));
                        classInfo.setChnTeacherName(SafeConverter.toString(teacherMap.get("teacherName")));
                        //该班级是否有认证语文老师
                        if (SafeConverter.toBoolean(teacherMap.get("isAuth"))) {
                            classInfo.setHasAuthChnTeacher(true);
                        }
                    }
                    // 设置班级指标数据
                    if (sumOnlineIndicator != null && monthOnlineIndicator != null) {
                        classInfo.setRegStuCount(sumOnlineIndicator.getRegStuCount() != null ? sumOnlineIndicator.getRegStuCount() : 0);
                        if (mode == 1) {
                            List<Map<String, Object>> teacherList = new ArrayList<>(subjectTeacherMap.values());
                            Set<Long> teacherIds = teacherList.stream().map(item -> SafeConverter.toLong(item.get("teacherId"))).collect(Collectors.toSet());
                            groupTeacherMap.forEach((k, v) -> {
                                if (teacherIds.contains(v)) {
                                    GroupOnlineIndicator groupOnlineIndicator = groupOnlineIndicatorMap.get(k);
                                    if (null != groupOnlineIndicator) {
                                        OnlineIndicator onlineIndicator = groupOnlineIndicator.fetchMonthData();
                                        if (null != onlineIndicator) {
                                            Teacher teacher = allTeacherMap.get(v);
                                            if (null != teacher) {
                                                if (teacher.getSubject() == Subject.ENGLISH) {
                                                    classInfo.setTmEngHwSc(onlineIndicator.getTmHwSc() != null ? onlineIndicator.getTmHwSc() : 0);
                                                }
                                                if (teacher.getSubject() == Subject.MATH) {
                                                    classInfo.setTmMathHwSc(onlineIndicator.getTmHwSc() != null ? onlineIndicator.getTmHwSc() : 0);
                                                }
                                                if (teacher.getSubject() == Subject.CHINESE) {
                                                    classInfo.setTmChnHwSc(onlineIndicator.getTmHwSc() != null ? onlineIndicator.getTmHwSc() : 0);
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                            // 英语
                            classInfo.setTmFinEngHwGte3AuStuCount(SafeConverter.toInt(monthOnlineIndicator.getIncSettlementEngStuCount()) + SafeConverter.toInt(monthOnlineIndicator.getReturnSettleNumEng()));
                            classInfo.setEngTermReviewFlag(sumOnlineIndicator.getTermReviewEngFlag() != null ? sumOnlineIndicator.getTermReviewEngFlag() : false);  //是否布置英语期末作业
                            classInfo.setEngVacnHwFlag(sumOnlineIndicator.getVacnEngHwFlag() != null ? sumOnlineIndicator.getVacnEngHwFlag() : false);              //是否布置英语假期作业

                            // 数学
                            classInfo.setTmFinMathHwGte3AuStuCount(SafeConverter.toInt(monthOnlineIndicator.getIncSettlementMathStuCount()) + SafeConverter.toInt(monthOnlineIndicator.getReturnSettleNumMath()));
                            classInfo.setMathTermReviewFlag(sumOnlineIndicator.getTermReviewMathFlag() != null ? sumOnlineIndicator.getTermReviewMathFlag() : false);   //是否布置数学期末作业
                            classInfo.setMathVacnHwFlag(sumOnlineIndicator.getVacnMathHwFlag() != null ? sumOnlineIndicator.getVacnMathHwFlag() : false);               //是否布置数学假期作业

                            // 语文
                            classInfo.setTmFinChnHwGte3AuStuCount(SafeConverter.toInt(monthOnlineIndicator.getIncSettlementChnStuCount()) + SafeConverter.toInt(monthOnlineIndicator.getReturnSettleNumChn()));
                            classInfo.setChnTermReviewFlag(sumOnlineIndicator.getTermReviewChnFlag() != null ? sumOnlineIndicator.getTermReviewChnFlag() : false);      //是否布置语文期末作业
                            classInfo.setChnVacnHwFlag(sumOnlineIndicator.getVacnChnHwFlag() != null ? sumOnlineIndicator.getVacnChnHwFlag() : false);                  //是否布置语文假期作业
                        }
                    }
                    //家长指标
                    if (mode == 3) {
                        ClassParentIndicator classParentIndicator = classParentIndicatorMap.get(clazz.getId());
                        if (classParentIndicator != null && classParentIndicator.fetchSumData() != null) {
                            ParentIndicator sumParentIndicator = classParentIndicator.fetchSumData();
                            ParentIndicator monthParentIndicator = classParentIndicator.fetchMonthData();
                            if (sumParentIndicator != null) {
                                classInfo.setBindParentStuNum(SafeConverter.toInt(sumParentIndicator.getBindParentStuNum()));
                            }
                            if (monthOnlineIndicator != null) {
                                classInfo.setParentStuActiveSettlementNum(SafeConverter.toInt(monthParentIndicator.getParentStuActiveSettlementNum()));
                            }
                        }
                    }

                    gradeInfo.getClassList().add(classInfo);
                }
            }
            resultList.add(gradeInfo);
        }

        // 对班级进行排序
        resultList.forEach(g -> {
            List<GradeClass17InfoView> list = ((Grade17InfoView) g).getClassList();
            list.sort((o1, o2) -> {
                String className1 = o1.getClassName();
                String className2 = o2.getClassName();
                int classNo1 = 99;
                int classNo2 = 99;
                if (StringUtils.isNotBlank(className1)) {
                    classNo1 = SafeConverter.toInt(className1.replaceAll("班", ""), 99);
                }
                if (StringUtils.isNotBlank(className2)) {
                    classNo2 = SafeConverter.toInt(className2.replaceAll("班", ""), 99);
                }
                return Integer.compare(classNo1, classNo2);
            });
        });
        return resultList;
    }

    // 设置学科和老师的对应关系
    // 1个班级若只有1个同科目老师，显示1个老师的姓名
    // 1个班级内若有大于等于1个同科目老师，优先显示已经认证了的老师姓名，若有多个认证老师，优先显示注册学生数多的老师，若注册学生数也相同，随机展示1个老师姓名。
    private Map<Subject, Map<String, Object>> fetchSubjectTeacher(Map<Long, Long> groupTeacherMap) {
        if (MapUtils.isEmpty(groupTeacherMap)) {
            return Collections.emptyMap();
        }
        Map<Subject, List<Map<String, Object>>> subjectTeacherDataList = new HashMap<>();

        Map<Long, GroupOnlineIndicator> groupOnlineIndicatorMap = loadNewSchoolServiceClient.loadGroupOnlineIndicator(groupTeacherMap.keySet(), performanceService.lastSuccessDataDay());

        if (CollectionUtils.isNotEmpty(groupTeacherMap.values())) {
            Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(groupTeacherMap.values());
            groupTeacherMap.forEach((k, v) -> {
                Teacher teacher = teacherMap.get(v);
                if (null != teacher) {
                    Map<String, Object> teacherData = new HashMap<>();
                    teacherData.put("teacherId", teacher.getId());
                    teacherData.put("teacherName", teacher.fetchRealname());
                    teacherData.put("subject", teacher.getSubject());
                    teacherData.put("isAuth", teacher.getAuthenticationState() == 1);

//                    AgentGroup17PerformanceData group17PerformanceData = group17DataMap.get(k);
//                    if(group17PerformanceData != null && group17PerformanceData.getIndicatorData() != null){
//                        teacherData.put("regStuCount", group17PerformanceData.getIndicatorData().getRegStuCount());
//                    }else {
//                        teacherData.put("regStuCount", 0);
//                    }

                    GroupOnlineIndicator groupOnlineIndicator = groupOnlineIndicatorMap.get(k);
                    if (null != groupOnlineIndicator && null != groupOnlineIndicator.fetchSumData()) {
                        OnlineIndicator onlineIndicator = groupOnlineIndicator.fetchSumData();
                        teacherData.put("regStuCount", onlineIndicator.getRegStuCount());
                    } else {
                        teacherData.put("regStuCount", 0);
                    }

                    List<Map<String, Object>> teacherDataList = subjectTeacherDataList.get(teacher.getSubject());
                    if (teacherDataList == null) {
                        teacherDataList = new ArrayList<>();
                        subjectTeacherDataList.put(teacher.getSubject(), teacherDataList);
                    }
                    teacherDataList.add(teacherData);
                }
            });
        }

        Map<Subject, Map<String, Object>> resultMap = new HashMap<>();
        subjectTeacherDataList.forEach((k, v) -> {
            if (v.size() == 1) {
                resultMap.put(k, v.get(0));
            } else {
                Map<String, Object> authTeacher = v.stream().filter(p -> (boolean) p.get("isAuth")).max((o1, o2) -> SafeConverter.toInt(o1.get("regStuCount")) - SafeConverter.toInt(o2.get("regStuCount"))).orElse(null);
                if (MapUtils.isNotEmpty(authTeacher)) {
                    resultMap.put(k, authTeacher);
                } else {
                    resultMap.put(k, v.stream().max((o1, o2) -> SafeConverter.toInt(o1.get("regStuCount")) - SafeConverter.toInt(o2.get("regStuCount"))).orElse(null));
                }
            }
        });
        return resultMap;
    }

    public Map<Integer, List<Object>> generateGradeClassInfoOffline(School school) {
        // 获取学校的年级分布
        List<Integer> gradeList = getGradeDistribute(school);
        Map<Integer, List<Object>> resultMap = new LinkedHashMap<>();
        gradeList.forEach(p -> resultMap.put(p, new ArrayList<>()));

        // 获取学校下的所有班级
        List<Clazz> classList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .toList();
        // 过滤掉disabled的数据和毕业班
        classList = classList.stream().filter(p -> !p.isDisabledTrue() && p.getClazzLevel() != ClazzLevel.INFANT_GRADUATED && p.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED && p.getClazzLevel() != ClazzLevel.MIDDLE_GRADUATED)
                .collect(Collectors.toList());

        // 过滤掉没有班组的班级
        Set<Long> allClassIds = classList.stream().map(Clazz::getId).collect(Collectors.toSet());
        Map<Long, List<Group>> classGroupMap = asyncGroupServiceClient.getAsyncGroupService().loadGroupsByClazzIds(allClassIds).getUninterruptibly();
        Set<Long> classIds = classGroupMap.values().stream().flatMap(List::stream).map(Group::getClazzId).collect(Collectors.toSet());

        classList = classList.stream().filter(p -> classIds.contains(p.getId())).collect(Collectors.toList());

        // 根据年级分组
        Map<ClazzLevel, List<Clazz>> gradeClassMap = classList.stream().collect(Collectors.groupingBy(Clazz::getClazzLevel, Collectors.toList()));

        Integer day = performanceService.lastSuccessDataDay();
        Map<Long, ClassOfflineIndicator> classOfflineIndicatorMap = loadNewSchoolServiceClient.loadClassOfflineIndicator(classIds, day);

        resultMap.forEach((k, v) -> {
            List<Clazz> list = gradeClassMap.get(ClazzLevel.parse(k));
            if (CollectionUtils.isNotEmpty(list)) {
                for (Clazz clazz : list) {
                    GradeClassKlxInfoView view = new GradeClassKlxInfoView();
                    view.setClassId(clazz.getId());
                    view.setClassName(clazz.getClassName());
                    ClassOfflineIndicator classOfflineIndicator = classOfflineIndicatorMap.get(clazz.getId());
                    if (classOfflineIndicator != null && classOfflineIndicator.fetchSumData() != null) {
                        OfflineIndicator offlineIndicator = classOfflineIndicator.fetchSumData();
                        view.setKlxTnCount(SafeConverter.toInt(offlineIndicator.getKlxTotalNum()));
                    }
                    if (classOfflineIndicator != null && classOfflineIndicator.fetchMonthData() != null) {
                        OfflineIndicator offlineIndicator = classOfflineIndicator.fetchMonthData();
                        view.setTmGte2Num(SafeConverter.toInt(offlineIndicator.getSettlementGte2NumSglSubj()) + SafeConverter.toInt(offlineIndicator.getUnsettlementGte2NumSglSubj()));
                    }
                    v.add(view);
                }
            }
        });
        return resultMap;
    }


    public EduSystemType getSchoolEduSystem(School school) {
        if (school == null) {
            return null;
        }
        String eduSystemStr = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
        if (StringUtils.isBlank(eduSystemStr)) {
            return null;
        }
        return EduSystemType.of(eduSystemStr);
    }

    /**
     * 获取学校的年级分布
     *
     * @param school school
     * @return 年级列表
     */
    private List<Integer> getGradeDistribute(School school) {
        EduSystemType eduSystemType = getSchoolEduSystem(school);
        if (eduSystemType == null) {
            return new ArrayList<>();
        }

        List<String> result = Arrays.asList(eduSystemType.getCandidateClazzLevel().split(","));
        return result.stream().map(SafeConverter::toInt).collect(Collectors.toList());
    }

    /**
     * 获取学校的年级分布
     *
     * @param school
     * @return
     */
    public List<ClazzLevel> getSchoolGradeList(School school) {
        List<ClazzLevel> clazzLevelList = new ArrayList<>();
        List<Integer> gradeList = getGradeDistribute(school);
        for (Integer p : gradeList) {
            clazzLevelList.add(ClazzLevel.parse(p));
        }
        return clazzLevelList;
    }

    /**
     * 获取学校实际班级数大于schoolExtInfo中班级数的年级列表
     *
     * @param school school
     * @return 年级列表
     */
    public List<Integer> judgeGradeClassCount(School school) {
        if (school == null) {
            return new ArrayList<>();
        }
        EduSystemType eduSystemType = getSchoolEduSystem(school);
        List<Integer> gradeList = getGradeDistribute(school);
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(school.getId()).getUninterruptibly();
        if (schoolExtInfo == null) {
            return gradeList;
        }

        List<Clazz> classList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .toList();
        // 过滤掉disabled的数据和毕业班
        classList = classList.stream().filter(p -> !p.isDisabledTrue() && p.getClazzLevel() != ClazzLevel.INFANT_GRADUATED && p.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED && p.getClazzLevel() != ClazzLevel.MIDDLE_GRADUATED)
                .collect(Collectors.toList());

        // 过滤掉没有班组的班级
        Set<Long> allClassIds = classList.stream().map(Clazz::getId).collect(Collectors.toSet());
        Map<Long, List<Group>> classGroupMap = asyncGroupServiceClient.getAsyncGroupService().loadGroupsByClazzIds(allClassIds).getUninterruptibly();
        Set<Long> classIds = classGroupMap.values().stream().flatMap(List::stream).map(Group::getClazzId).collect(Collectors.toSet());

        classList = classList.stream().filter(p -> classIds.contains(p.getId())).collect(Collectors.toList());

        Map<Integer, Long> gradeClassCountMap = classList.stream().collect(Collectors.groupingBy(p -> p.getClazzLevel().getLevel(), Collectors.counting()));

        List<Integer> resultList = new ArrayList<>();
        gradeList.forEach(p -> {
            Long classCount = gradeClassCountMap.get(p);
            int realClassCount = classCount == null ? 0 : classCount.intValue();
            int extClassCount = fetchGradeClassCountFromSchoolExtInfo(schoolExtInfo, p, eduSystemType);
            if (realClassCount > extClassCount) {
                resultList.add(p);
            }
        });
        return resultList;
    }

    /**
     * 获取schoolExtInfo中指定年级的班级数
     *
     * @param schoolExtInfo extInfo
     * @param grade         年级
     * @return 班级数量
     */
    private int fetchGradeClassCountFromSchoolExtInfo(SchoolExtInfo schoolExtInfo, Integer grade, EduSystemType eduSystemType) {
        if (schoolExtInfo == null) {
            return 0;
        }
        return SafeConverter.toInt(schoolExtInfo.fetchGradeClazzNum(ClazzLevel.parse(grade), eduSystemType));
    }

    /**
     * 生成年级中的柱状图数据
     *
     * @param school school
     * @param mode   1:online  2: offline
     * @return 图表数据
     */
    public Map<String, Object> generateGradeChartInfo(School school, int mode) {

        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(school.getId())
                .getUninterruptibly();
        Map<String, Object> resultMap = new HashMap<>();


        // 获取学校的年级分布
        EduSystemType eduSystemType = getSchoolEduSystem(school);
        List<Integer> gradeList = getGradeDistribute(school);

        // 获取各个年级规模
        Map<Integer, Integer> gradeScaleMap = new HashMap<>();
        if (extInfo != null) {
            for (Integer grade : gradeList) {
                gradeScaleMap.put(grade, SafeConverter.toInt(extInfo.fetchGradeStudentNum(ClazzLevel.parse(grade), eduSystemType)));
            }
        }


        List<String> xaxis = gradeList.stream().map(p -> ClazzLevel.parse(p).getDescription()).collect(Collectors.toList());
        resultMap.put("xAxis", xaxis);

        Integer day = performanceService.lastSuccessDataDay();
        List<String> legend = new ArrayList<>();
        if (mode == 1) {   // online
            legend.add("规模");
            legend.add("英活");
            legend.add("数活");

            // 规模数据
            List<Integer> scaleData = gradeList.stream().map(grade -> SafeConverter.toInt(gradeScaleMap.get(grade))).collect(Collectors.toList());

            List<Integer> engData = new ArrayList<>();
            List<Integer> mathData = new ArrayList<>();

            GradeOnlineIndicator gradeOnlineIndicator = loadNewSchoolServiceClient.loadGradeOnlineIndicator(school.getId(), day);
            if (null != gradeOnlineIndicator) {
                gradeList.forEach(p -> {
                    OnlineIndicator onlineIndicator = gradeOnlineIndicator.fetchMonthData(ClazzLevel.parse(p));
                    if (null != onlineIndicator) {
                        engData.add(SafeConverter.toInt(onlineIndicator.getIncSettlementEngStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNumEng()));
                        mathData.add(SafeConverter.toInt(onlineIndicator.getIncSettlementMathStuCount()) + SafeConverter.toInt(onlineIndicator.getReturnSettleNumMath()));
                    }
                });
            } else {
                for (Integer grade : gradeList) {
                    engData.add(0);
                    mathData.add(0);
                }
            }
            resultMap.put("规模", scaleData);
            resultMap.put("英活", engData);
            resultMap.put("数活", mathData);
        } else if (mode == 2) {   // offline 待扩展
            legend.add("规模");
            legend.add("周测（≥2套）");

            // 规模数据
            List<Integer> scaleData = gradeList.stream().map(grade -> SafeConverter.toInt(gradeScaleMap.get(grade))).collect(Collectors.toList());

            // 周测数据
            List<Integer> weekTestData = new ArrayList<>();

            GradeOfflineIndicator gradeOfflineIndicator = loadNewSchoolServiceClient.loadGradeOfflineIndicator(school.getId(), day);
            if (gradeOfflineIndicator != null) {
                gradeList.forEach(p -> {
                    OfflineIndicator offlineIndicator = gradeOfflineIndicator.fetchMonthData(ClazzLevel.parse(p));
                    if (offlineIndicator != null) {
                        weekTestData.add(SafeConverter.toInt(offlineIndicator.getSettlementGte2NumSglSubj()) + SafeConverter.toInt(offlineIndicator.getUnsettlementGte2NumSglSubj()));
                    } else {
                        weekTestData.add(0);
                    }
                });
            } else {
                weekTestData.addAll(gradeList.stream().map(ignored -> 0).collect(Collectors.toList()));
            }
            resultMap.put("规模", scaleData);
            resultMap.put("周测（≥2套）", weekTestData);
        }
        resultMap.put("legend", legend);
        return resultMap;
    }

    /**
     * 生成班级详情数据
     *
     * @param clazz 班级
     * @param mode  1:online  2: offline
     * @return 班级数据
     */
    public Map<String, Object> generateClassDetailInfo(Clazz clazz, int mode) {
        Map<String, Object> resultMap = new HashMap<>();
        if (clazz == null) {
            return resultMap;
        }
        resultMap.put("classId", clazz.getId());
        resultMap.put("classFullName", clazz.formalizeClazzName());

        Integer day = performanceService.lastSuccessDataDay();

        if (mode == 1) { // online
            Map<Long, ClassOnlineIndicator> classOnlineIndicatorMap = loadNewSchoolServiceClient.loadClassOnlineIndicator(Collections.singleton(clazz.getId()), day);
            ClassOnlineIndicator classOnlineIndicator = classOnlineIndicatorMap.get(clazz.getId());
            Map<Long, ClassParentIndicator> classParentIndicatorMap = loadParentServiceClient.loadClassParentIndicator(Collections.singleton(clazz.getId()), day);
            ClassParentIndicator classParentIndicator = classParentIndicatorMap.get(clazz.getId());
            int regStuNum = 0;
            int auStuNum = 0;
            int sglSubjMauc = 0;
            int bindParentStuNum = 0;
            int parentStuActiveSettlementNum = 0;
            if (classOnlineIndicator != null) {
                OnlineIndicator sumOnlineIndicator = classOnlineIndicator.fetchSumData();
                OnlineIndicator monthOnlineIndicator = classOnlineIndicator.fetchMonthData();
                if (sumOnlineIndicator != null) {
                    regStuNum = SafeConverter.toInt(sumOnlineIndicator.getRegStuCount());
                    auStuNum = SafeConverter.toInt(sumOnlineIndicator.getAuStuCount());
                }
                if (monthOnlineIndicator != null) {
                    sglSubjMauc = SafeConverter.toInt(monthOnlineIndicator.getIncSettlementSglSubjStuCount()) + SafeConverter.toInt(monthOnlineIndicator.getReturnSettleNumSglSubj());
                }
            }
            if (classParentIndicator != null) {
                ParentIndicator sumParentIndicator = classParentIndicator.fetchSumData();
                ParentIndicator monthParentIndicator = classParentIndicator.fetchMonthData();
                if (sumParentIndicator != null) {
                    bindParentStuNum = SafeConverter.toInt(sumParentIndicator.getBindParentStuNum());
                }
                if (monthParentIndicator != null) {
                    parentStuActiveSettlementNum = SafeConverter.toInt(monthParentIndicator.getParentStuActiveSettlementNum());
                }
            }
            resultMap.put("regStuNum", regStuNum);
            resultMap.put("auStuNum", auStuNum);
            resultMap.put("sglSubjMauc", sglSubjMauc);
            resultMap.put("bindParentStuNum", bindParentStuNum);
            resultMap.put("parentStuActiveSettlementNum", parentStuActiveSettlementNum);
        } else if (mode == 2) {   // offline 待扩展
            Map<Long, ClassOfflineIndicator> classOfflineIndicatorMap = loadNewSchoolServiceClient.loadClassOfflineIndicator(Collections.singleton(clazz.getId()), day);
            ClassOfflineIndicator classOfflineIndicator = classOfflineIndicatorMap.get(clazz.getId());
            if (classOfflineIndicator != null && classOfflineIndicator.fetchSumData() != null) {
                OfflineIndicator offlineIndicator = classOfflineIndicator.fetchSumData();
                resultMap.put("klxTnNum", SafeConverter.toInt(offlineIndicator.getKlxTotalNum()));
            } else {
                resultMap.put("klxTnNum", 0);
            }

            if (classOfflineIndicator != null && classOfflineIndicator.fetchMonthData() != null) {
                OfflineIndicator offlineIndicator = classOfflineIndicator.fetchMonthData();
                resultMap.put("tmGte2Num", SafeConverter.toInt(offlineIndicator.getSettlementGte2NumSglSubj()) + SafeConverter.toInt(offlineIndicator.getUnsettlementGte2NumSglSubj()));
            } else {
                resultMap.put("tmGte2Num", 0);
            }
        }
        return resultMap;
    }

    /**
     * 班级老师列表信息
     *
     * @param classId
     * @param mode
     * @return
     */
    public MapMessage generateClassTeacherList(Long classId, int mode) {
        MapMessage mapMessage = MapMessage.successMessage();
        Integer day = performanceService.lastSuccessDataDay();

        Date lastMonthDate = DayUtils.getLastDayOfMonth(DateUtils.addMonths(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), -1));
        Integer lastDayOfPreMonth = SafeConverter.toInt(DateUtils.dateToString(lastMonthDate, "yyyyMMdd"));

        // 获取该班级下的group和teacher
        Map<Long, Long> groupTeacherMap = fetchGroupTeacherDataByClassId(classId, false);

        //老师标签
        List<String> teacherIds = new ArrayList<>();
        groupTeacherMap.values().forEach(p -> teacherIds.add(SafeConverter.toString(p)));
        Map<String, List<AgentTag>> teacherTagMap = agentTagService.getTagListByTargetIdsAndType(teacherIds, AgentTagTargetType.TEACHER,true);

        if (mode == 1) { // online
            List<ClassTeacher17InfoView> teacherList = new ArrayList<>();
            List<ClassTeacher17InfoView> teacherListFinal = new ArrayList<>();
            if (MapUtils.isNotEmpty(groupTeacherMap)) {
                Map<Long, GroupOnlineIndicator> groupOnlineIndicatorMap = loadNewSchoolServiceClient.loadGroupOnlineIndicator(groupTeacherMap.keySet(), day);
                Map<Long, GroupOnlineIndicator> lmGroupOnlineIndicatorMap = loadNewSchoolServiceClient.loadGroupOnlineIndicator(groupTeacherMap.keySet(), lastDayOfPreMonth);

                Set<Long> allTeacherIds = new HashSet<>(groupTeacherMap.values());
                Map<Long, TeacherOnlineIndicator> teacherOnlineIndicatorMap = loadNewSchoolServiceClient.loadTeacherOnlineIndicator(allTeacherIds, day);

                //是否是真老师
                Map<Long, Boolean> isRealTeacherMap = isRealTeacher(groupTeacherMap.values());

                groupTeacherMap.forEach((k, v) -> {
                    ClassTeacher17InfoView teacherInfo = new ClassTeacher17InfoView();
                    teacherInfo.setGroupId(k);
                    teacherInfo.setTeacherId(v);
                    //如果是假老师
                    if (isRealTeacherMap.containsKey(v) && !isRealTeacherMap.get(v)) {
                        return;
                    }
                    Teacher teacher = teacherLoaderClient.loadTeacher(v);
                    if (teacher != null) {
                        teacherInfo.setTeacherName(teacher.fetchRealname());
                        teacherInfo.setSubject(teacher.getSubject());
                        teacherInfo.setSubjectName(teacher.getSubject() != null ? teacher.getSubject().getValue() : "");
                        teacherInfo.setIsAuth(teacher.getAuthenticationState() == 1);
                    }

                    GroupOnlineIndicator groupOnlineIndicator = groupOnlineIndicatorMap.get(k);
                    if (null != groupOnlineIndicator) {
                        OnlineIndicator sumOnlineIndicator = groupOnlineIndicator.fetchSumData();
                        OnlineIndicator monthOnlineIndicator = groupOnlineIndicator.fetchMonthData();
                        if (null != sumOnlineIndicator) {
                            teacherInfo.setVacnHwFlag(sumOnlineIndicator.getVacnHwFlag() != null ? sumOnlineIndicator.getVacnHwFlag() : false);
                        }
                        if (null != monthOnlineIndicator) {
                            teacherInfo.setTmHwSc(monthOnlineIndicator.getTmHwSc() != null ? monthOnlineIndicator.getTmHwSc() : 0);
                            teacherInfo.setFinCsHwEq1AuStuCount((monthOnlineIndicator.getFinSglSubjHwEq1UnSettleStuCount() != null ? monthOnlineIndicator.getFinSglSubjHwEq1UnSettleStuCount() : 0) + (monthOnlineIndicator.getFinSglSubjHwEq1SettleStuCount() != null ? monthOnlineIndicator.getFinSglSubjHwEq1SettleStuCount() : 0));
                            teacherInfo.setFinCsHwEq2AuStuCount((monthOnlineIndicator.getFinSglSubjHwEq2UnSettleStuCount() != null ? monthOnlineIndicator.getFinSglSubjHwEq2UnSettleStuCount() : 0) + (monthOnlineIndicator.getFinSglSubjHwEq2SettleStuCount() != null ? monthOnlineIndicator.getFinSglSubjHwEq2SettleStuCount() : 0));
                            teacherInfo.setFinCsHwGte3AuStuCount(SafeConverter.toInt(monthOnlineIndicator.getIncSettlementSglSubjStuCount()) + SafeConverter.toInt(monthOnlineIndicator.getReturnSettleNum()));
                        }
                    }

                    GroupOnlineIndicator lmGroupOnlineIndicator = lmGroupOnlineIndicatorMap.get(k);
                    if (null != lmGroupOnlineIndicator) {
                        OnlineIndicator onlineIndicator = lmGroupOnlineIndicator.fetchMonthData();
                        if (null != onlineIndicator) {
                            teacherInfo.setLmHwSc(onlineIndicator.getTmHwSc() != null ? onlineIndicator.getTmHwSc() : 0);
                        }
                    }

                    TeacherOnlineIndicator teacherOnlineIndicator = teacherOnlineIndicatorMap.get(v);
                    if (null != teacherOnlineIndicator) {
                        OnlineIndicator onlineIndicator = teacherOnlineIndicator.fetchSumData();
                        if (null != onlineIndicator) {
                            //布置假期作业的班组数
                            teacherInfo.setVacnHwGroupCount(onlineIndicator.getVacnHwGroupCount() != null ? onlineIndicator.getVacnHwGroupCount() : 0);
                            //布置期末作业的班组数
                            teacherInfo.setTermReviewGroupCount(onlineIndicator.getTermReviewGroupCount() != null ? onlineIndicator.getTermReviewGroupCount() : 0);
                        }
                    }
                    teacherInfo.setTagList(teacherTagMap.get(SafeConverter.toString(v)));
                    teacherList.add(teacherInfo);
                });
            }
            // 过滤掉隐藏的老师
            List<Long> teacherIdList = teacherList.stream().map(ClassTeacher17InfoView::getTeacherId).collect(Collectors.toList());
            Map<Long, Boolean> isHiddenTeacherMap = isHiddenTeacher(teacherIdList);
            teacherList.forEach(p -> {
                if (isHiddenTeacherMap.containsKey(p.getTeacherId()) && !isHiddenTeacherMap.get(p.getTeacherId())) {
                    teacherListFinal.add(p);
                }
            });
            mapMessage.add("teacherList", teacherListFinal);
        } else if (mode == 2) {   // offline 待扩展
            List<ClassTeacherKlxInfoView> teacherList = new ArrayList<>();
            List<ClassTeacherKlxInfoView> teacherListFinal = new ArrayList<>();
            if (MapUtils.isNotEmpty(groupTeacherMap)) {
                Map<Long, GroupOfflineIndicator> groupOfflineIndicatorMap = loadNewSchoolServiceClient.loadGroupOfflineIndicator(groupTeacherMap.keySet(), day);
                Map<Long, GroupOfflineIndicator> lmGroupOfflineIndicatorMap = loadNewSchoolServiceClient.loadGroupOfflineIndicator(groupTeacherMap.keySet(), lastDayOfPreMonth);
                groupTeacherMap.forEach((k, v) -> {
                    ClassTeacherKlxInfoView teacherInfo = new ClassTeacherKlxInfoView();
                    teacherInfo.setGroupId(k);
                    teacherInfo.setTeacherId(v);
                    Teacher teacher = teacherLoaderClient.loadTeacher(v);
                    if (teacher != null) {
                        teacherInfo.setTeacherName(teacher.fetchRealname());
                        teacherInfo.setSubject(teacher.getSubject());
                        teacherInfo.setIsAuth(teacher.getAuthenticationState() == 1);
                    }

                    GroupOfflineIndicator groupOfflineIndicator = groupOfflineIndicatorMap.get(k);
                    if (groupOfflineIndicator != null && groupOfflineIndicator.fetchMonthData() != null) {
                        OfflineIndicator offlineIndicator = groupOfflineIndicator.fetchMonthData();
                        teacherInfo.setTmGte1Num(SafeConverter.toInt(offlineIndicator.getSettlementNum()) + SafeConverter.toInt(offlineIndicator.getUnsettlementNum()));
                        teacherInfo.setTmGte2Num(SafeConverter.toInt(offlineIndicator.getSettlementGte2Num()) + SafeConverter.toInt(offlineIndicator.getUnsettlementGte2Num()));
                    }

                    GroupOfflineIndicator lmGroupOfflineIndicator = lmGroupOfflineIndicatorMap.get(k);
                    if (lmGroupOfflineIndicator != null && lmGroupOfflineIndicator.fetchMonthData() != null) {
                        OfflineIndicator offlineIndicator = lmGroupOfflineIndicator.fetchMonthData();
                        teacherInfo.setLmGte1Num(SafeConverter.toInt(offlineIndicator.getSettlementNum()) + SafeConverter.toInt(offlineIndicator.getUnsettlementNum()));
                        teacherInfo.setLmGte2Num(SafeConverter.toInt(offlineIndicator.getSettlementGte2Num()) + SafeConverter.toInt(offlineIndicator.getUnsettlementGte2Num()));
                    }
                    teacherInfo.setTagList(teacherTagMap.get(SafeConverter.toString(v)));
                    teacherList.add(teacherInfo);
                });
            }
            // 过滤掉隐藏的老师
            List<Long> teacherIdList = teacherList.stream().map(ClassTeacherKlxInfoView::getTeacherId).collect(Collectors.toList());
            Map<Long, Boolean> isHiddenTeacherMap = isHiddenTeacher(teacherIdList);
            teacherList.forEach(p -> {
                if (isHiddenTeacherMap.containsKey(p.getTeacherId()) && !isHiddenTeacherMap.get(p.getTeacherId())) {
                    teacherListFinal.add(p);
                }
            });
            mapMessage.add("teacherList", teacherListFinal);
        }
        return mapMessage;
    }

    /**
     * 实时获取指定班级下的group和teacher的数据
     *
     * @param classId               班级ID
     * @param containsHiddenTeacher 是否包含隐藏的老师
     * @return group和teacher的对应关系
     */
    private Map<Long, Long> fetchGroupTeacherDataByClassId(Long classId, boolean containsHiddenTeacher) {
        Map<Long, Long> groupTeacherMap = new HashMap<>();
        Map<Long, Map<Long, Long>> classGroupTeacherMap = fetchGroupTeacherDataByClassIds(Collections.singleton(classId), containsHiddenTeacher);
        if (MapUtils.isNotEmpty(classGroupTeacherMap)) {
            Map<Long, Long> tempMap = classGroupTeacherMap.get(classId);
            if (MapUtils.isNotEmpty(tempMap)) {
                groupTeacherMap.putAll(tempMap);
            }
        }
        return groupTeacherMap;
    }

    /**
     * 实时获取指定班级下的group和teacher的数据
     *
     * @param classIds              班级ID
     * @param containsHiddenTeacher 是否包含隐藏的老师
     * @return group和teacher的对应关系
     */
    private Map<Long, Map<Long, Long>> fetchGroupTeacherDataByClassIds(Collection<Long> classIds, boolean containsHiddenTeacher) {
        Map<Long, Map<Long, Long>> classGroupTeacherMap = new HashMap<>();
        Map<Long, List<Group>> classGroupMap = asyncGroupServiceClient.getAsyncGroupService().loadGroupsByClazzIds(classIds).getUninterruptibly();
        List<Long> groupIdList = classGroupMap.values().stream().flatMap(List::stream).map(Group::getId).collect(Collectors.toList());

        Map<Long, Long> groupTeacherMap = new HashMap<>();
        Map<Long, Long> groupTeacherNoHiddenMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(groupIdList)) {
            Map<Long, List<GroupTeacherTuple>> groupTeacherRefMap = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .groupByGroupIds(groupIdList);
            if (MapUtils.isNotEmpty(groupTeacherRefMap)) {
                Map<Long, GroupTeacherTuple> groupRefMap = groupTeacherRefMap.values().stream()
                        .flatMap(List::stream)
                        .filter(GroupTeacherTuple::isValidTrue)
                        .collect(Collectors.toMap(GroupTeacherTuple::getGroupId, Function.identity(), (o1, o2) -> {
                            if (o1.getCreateTime().after(o2.getCreateTime())) {
                                return o1;
                            }
                            return o2;
                        }));
                groupRefMap.forEach((k, v) -> groupTeacherMap.put(k, v.getTeacherId()));

                if (MapUtils.isNotEmpty(groupTeacherMap)) {
                    if (containsHiddenTeacher) {
                        groupTeacherNoHiddenMap.putAll(groupTeacherMap);
                    } else {
                        // 过滤掉隐藏的老师
                        Map<Long, AgentHiddenTeacher> hiddenTeacherMap = agentHiddenTeacherService.getAgentHiddenTeachers(groupTeacherMap.values());
                        groupTeacherMap.forEach((k, v) -> {
                            if (!hiddenTeacherMap.containsKey(v)) {
                                groupTeacherNoHiddenMap.put(k, v);
                            }
                        });
                    }
                }
            }
        }

        classGroupMap.forEach((k, v) -> {
            Map<Long, Long> tmpGroupTeacherMap = new HashMap<>();
            v.forEach(p -> {
                Long teacherId = groupTeacherNoHiddenMap.get(p.getId());
                if (teacherId != null) {
                    tmpGroupTeacherMap.put(p.getId(), teacherId);
                }
            });
            classGroupTeacherMap.put(k, tmpGroupTeacherMap);
        });

        return classGroupTeacherMap;
    }

    public Map<Long, Map<Long, Long>> fetchGroupTeacherDataByClassIdsPublic(Collection<Long> classIds, boolean containsHiddenTeacher) {
        return fetchGroupTeacherDataByClassIds(classIds, containsHiddenTeacher);
    }

    /**
     * 生成班级下学生信息（共享班级的老师列出一份数据，没共享的单独列出）
     *
     * @param clazz 班级
     * @param mode  1:online  2: offline
     * @return 班级下老师，学生的数据
     */
    public Map<String, Object> generateClassStudentInfo(Clazz clazz, int mode) {

        Map<String, Object> resultMap = new HashMap<>();
        if (clazz == null) {
            return resultMap;
        }

        resultMap.put("classId", clazz.getId());
        resultMap.put("classFullName", clazz.formalizeClazzName());

        Integer day = performanceService.lastSuccessDataDay();
        if (mode == 1) { // online
            Map<Long, ClassOnlineIndicator> classOnlineIndicatorMap = loadNewSchoolServiceClient.loadClassOnlineIndicator(Collections.singleton(clazz.getId()), day);
            ClassOnlineIndicator classOnlineIndicator = classOnlineIndicatorMap.get(clazz.getId());
            if (null != classOnlineIndicator && null != classOnlineIndicator.fetchSumData()) {
                OnlineIndicator onlineIndicator = classOnlineIndicator.fetchSumData();
                resultMap.put("regStuCount", onlineIndicator.getRegStuCount() != null ? onlineIndicator.getRegStuCount() : 0);
                resultMap.put("auStuCount", onlineIndicator.getAuStuCount() != null ? onlineIndicator.getAuStuCount() : 0);
            } else {
                resultMap.put("regStuCount", 0);
                resultMap.put("auStuCount", 0);
            }
        } else if (mode == 2) {   // offline 待扩展

        }

        List<Map<String, Object>> teacherStudentMapList = new ArrayList<>();
        resultMap.put("teacherStudentList", teacherStudentMapList);

        Map<Long, Long> groupTeacherMap = fetchGroupTeacherDataByClassId(clazz.getId(), false);
        if (MapUtils.isEmpty(groupTeacherMap)) {
            return resultMap;
        }
        Map<Long, CrmTeacherSummary> teacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(groupTeacherMap.values());

        Map<Long, Group> groupMap = groupLoaderClient.getGroupLoader().loadGroups(groupTeacherMap.keySet()).getUninterruptibly();
        // 获取共享班级的group
        List<Long> unSharedGroupIds = groupMap.values().stream().filter(p -> StringUtils.isBlank(p.getGroupParent())).map(Group::getId).collect(Collectors.toList());
        Map<String, List<Group>> shareGroupMap = groupMap.values().stream().filter(p -> StringUtils.isNotBlank(p.getGroupParent())).collect(Collectors.groupingBy(Group::getGroupParent, Collectors.toList()));

        Map<Long, List<Long>> groupSharedGroupList = new HashMap<>();
        unSharedGroupIds.forEach(p -> groupSharedGroupList.put(p, Collections.singletonList(p)));
        shareGroupMap.values().forEach(p -> {
            Long firstGroupId = p.get(0).getId();
            groupSharedGroupList.put(firstGroupId, p.stream().map(Group::getId).collect(Collectors.toList()));
        });

        Map<Long, List<Map<String, Object>>> groupStudentList = fetchStudentListByGroupIds(groupSharedGroupList.keySet());

        if (MapUtils.isNotEmpty(groupSharedGroupList)) {
            groupSharedGroupList.forEach((p, v) -> {
                Map<String, Object> teacherStudentMap = new HashMap<>();
                // 设置学生数据
                if (groupStudentList.containsKey(p)) {
                    teacherStudentMap.put("studentList", groupStudentList.get(p));
                } else {
                    teacherStudentMap.put("studentList", new ArrayList());
                }

                // 设置老师数据
                List<Map<String, Object>> teacherList = new ArrayList<>();
                for (Long gid : v) {
                    Map<String, Object> teacherData = new HashMap<>();
                    teacherData.put("groupId", gid);
                    Long teacherId = groupTeacherMap.get(gid);
                    teacherData.put("teacherId", teacherId);
                    CrmTeacherSummary teacherSummary = teacherSummaryMap.get(teacherId);
                    if (teacherSummary != null) {
                        teacherData.put("teacherName", teacherSummary.getRealName());
                        teacherData.put("subject", Subject.ofWithUnknown(teacherSummary.getSubject()).getValue());
                    } else {
                        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                        teacherData.put("teacherName", teacher.fetchRealname());
                        teacherData.put("subject", teacher.getSubject().getValue());
                    }
                    teacherList.add(teacherData);
                }
                teacherStudentMap.put("teacherList", teacherList);
                teacherStudentMapList.add(teacherStudentMap);
            });
        }
        return resultMap;
    }

    /**
     * 班组老师学生列表
     *
     * @param groupId
     * @return
     */
    public Map<String, Object> groupTeacherStudentInfo(Long groupId) {
        Map<String, Object> dataMap = new HashMap<>();
        Group group = groupLoaderClient.getGroupLoader().loadGroup(groupId).getUninterruptibly();
        if (group == null) {
            return dataMap;
        }
        Map<Long, Long> groupTeacherMap = fetchGroupTeacherDataByClassId(group.getClazzId(), false);
        if (MapUtils.isEmpty(groupTeacherMap)) {
            return dataMap;
        }
        Map<Long, Group> groupMap = groupLoaderClient.getGroupLoader().loadGroups(groupTeacherMap.keySet()).getUninterruptibly();

        List<Long> groupIds = new ArrayList<>();
        if (StringUtils.isBlank(group.getGroupParent())) {
            groupIds.add(groupId);
        } else {
            groupIds.addAll(groupMap.values().stream().filter(p -> StringUtils.isNotBlank(p.getGroupParent()) && Objects.equals(p.getGroupParent(), group.getGroupParent())).map(Group::getId).collect(Collectors.toList()));
        }

        Map<Long, CrmTeacherSummary> crmTeacherSummaryMap = crmSummaryLoaderClient.loadTeacherSummary(groupTeacherMap.values());

        // 设置老师数据
        List<Map<String, Object>> teacherList = new ArrayList<>();
        for (Long gid : groupIds) {
            Map<String, Object> teacherMap = new HashMap<>();
            Long teacherId = groupTeacherMap.get(gid);
            if (teacherId != null) {
                teacherMap.put("teacherId", teacherId);
                CrmTeacherSummary teacherSummary = crmTeacherSummaryMap.get(teacherId);
                if (teacherSummary != null) {
                    teacherMap.put("teacherName", teacherSummary.getRealName());
                    teacherMap.put("subject", Subject.ofWithUnknown(teacherSummary.getSubject()).getValue());
                } else {
                    Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                    if (teacher != null) {
                        teacherMap.put("teacherName", teacher.fetchRealname());
                        teacherMap.put("subject", teacher.getSubject().getValue());
                    }
                }
                teacherList.add(teacherMap);
            }
        }

        //设置学生数据
        Long teacherId = groupTeacherMap.get(groupId);
        List<Map<String, Object>> studentDataList = new ArrayList<>();

        List<User> studentList = studentLoaderClient.loadGroupStudents(Collections.singleton(groupId)).get(groupId);
        List<Long> allStudentIds = studentList.stream().map(User::getId).collect(toList());
        List<Long> authedStudentIds = new ArrayList<>();
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher != null) {
            List<Long> studentIds = userAuthQueryServiceClient.filterAuthedStudents(allStudentIds, teacher.getKtwelve() != null ? SchoolLevel.safeParse(teacher.getKtwelve().getLevel()) : SchoolLevel.JUNIOR);
            if (CollectionUtils.isNotEmpty(studentIds)) {
                authedStudentIds.addAll(studentIds);
            }
        }
        Map<Long, List<StudentParent>> stuParentsMap = parentLoaderClient.loadStudentParents(allStudentIds);
        Collator collator = Collator.getInstance(Locale.CHINA);
        Collections.sort(studentList, (o1, o2) -> {
            String name1 = StringUtils.defaultString(o1.getProfile().getRealname());
            String name2 = StringUtils.defaultString(o2.getProfile().getRealname());
            if (StringUtils.isNotBlank(name1) && StringUtils.isNotBlank(name2)) {
                return collator.compare(name1, name2);
            } else if (StringUtils.isBlank(name1) && StringUtils.isNotBlank(name2)) {
                return 1;
            } else if (StringUtils.isNotBlank(name1) && StringUtils.isBlank(name2)) {
                return -1;
            }
            return 0;
        });
        studentList.forEach(p -> {
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("studentId", p.getId());
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                studentMap.put("authState", p.getAuthenticationState());
            } else if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
                if (CollectionUtils.isNotEmpty(authedStudentIds)) {
                    studentMap.put("authState", authedStudentIds.contains(p.getId()));
                } else {
                    studentMap.put("authState", false);
                }
            }
            studentMap.put("studentName", StringUtils.defaultString(p.getProfile().getRealname()));
            //绑定家长数
            int bindParentNum = 0;
            List<StudentParent> studentParents = stuParentsMap.get(p.getId());
            if (CollectionUtils.isNotEmpty(studentParents)) {
                bindParentNum = studentParents.size();
            }
            studentMap.put("bindParentNum", bindParentNum);
            studentDataList.add(studentMap);
        });
        dataMap.put("teacherList", teacherList);
        dataMap.put("studentList", studentDataList);
        return dataMap;

    }

    /**
     * 根据groupIds获取班组下的学生列表兵排序
     *
     * @param groupIds 班组ids
     * @return 班组对应的学生列表
     */
    private Map<Long, List<Map<String, Object>>> fetchStudentListByGroupIds(Collection<Long> groupIds) {

        Map<Long, List<Map<String, Object>>> resultMap = new HashMap<>();
        if (CollectionUtils.isEmpty(groupIds)) {
            return resultMap;
        }
        Map<Long, List<User>> groupStudentMap = studentLoaderClient.loadGroupStudents(groupIds);
        groupStudentMap.forEach((k, v) -> {
            List<Map<String, Object>> studentDataList = new ArrayList<>();
            Collator collator = Collator.getInstance(Locale.CHINA);
            Collections.sort(v, (o1, o2) -> {
                String name1 = StringUtils.defaultString(o1.getProfile().getRealname());
                String name2 = StringUtils.defaultString(o2.getProfile().getRealname());
                if (StringUtils.isNotBlank(name1) && StringUtils.isNotBlank(name2)) {
                    return collator.compare(name1, name2);
                } else if (StringUtils.isBlank(name1) && StringUtils.isNotBlank(name2)) {
                    return 1;
                } else if (StringUtils.isNotBlank(name1) && StringUtils.isBlank(name2)) {
                    return -1;
                }
                return 0;
            });
            v.forEach(p -> {
                Map<String, Object> studentMap = new HashMap<>();
                studentMap.put("studentId", p.getId());
                studentMap.put("createTime", DateUtils.dateToString(p.getCreateTime(), DateUtils.FORMAT_SQL_DATETIME));
                studentMap.put("studentName", StringUtils.defaultString(p.getProfile().getRealname()));
                studentDataList.add(studentMap);
            });
            resultMap.put(k, studentDataList);
        });

        return resultMap;
    }


    /**
     * 家长年级柱状图
     *
     * @param school
     * @return
     */
    public Map<String, Object> generateParentGradeChartInfo(School school) {

        SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(school.getId())
                .getUninterruptibly();
        Map<String, Object> resultMap = new HashMap<>();

        // 获取学校的年级分布
        EduSystemType eduSystemType = getSchoolEduSystem(school);
        List<Integer> gradeList = getGradeDistribute(school);

        // 获取各个年级规模
        Map<Integer, Integer> gradeScaleMap = new HashMap<>();
        if (extInfo != null) {
            for (Integer grade : gradeList) {
                gradeScaleMap.put(grade, SafeConverter.toInt(extInfo.fetchGradeStudentNum(ClazzLevel.parse(grade), eduSystemType)));
            }
        }
        // 年级规模数据
        List<Integer> gradeScale = gradeList.stream().map(grade -> SafeConverter.toInt(gradeScaleMap.get(grade))).collect(Collectors.toList());

        //年级名称
        List<String> gradeName = gradeList.stream().map(p -> ClazzLevel.parse(p).getDescription()).collect(Collectors.toList());

        //指标
        List<String> index = new ArrayList<>();
        index.add("规模");
        index.add("累计绑定家长的学生数");

        //累计绑定家长的学生数
        List<Integer> bindParentStuNum = new ArrayList<>();
        Integer day = performanceService.lastSuccessDataDay();
        GradeParentIndicator gradeParentIndicator = loadParentServiceClient.loadGradeParentIndicator(school.getId(), day);
        if (gradeParentIndicator != null) {
            gradeList.forEach(p -> {
                ParentIndicator parentIndicator = gradeParentIndicator.fetchSumData(ClazzLevel.parse(p));
                if (parentIndicator != null) {
                    bindParentStuNum.add(SafeConverter.toInt(parentIndicator.getBindParentStuNum()));
                }
            });
        } else {
            for (Integer grade : gradeList) {
                bindParentStuNum.add(0);
            }
        }
        resultMap.put("xAxis", gradeName);
        resultMap.put("规模", gradeScale);
        resultMap.put("累计绑定家长的学生数", bindParentStuNum);
        resultMap.put("legend", index);
        return resultMap;
    }


    /**
     * 获取学校中，年级与老师的对应关系
     * @param school
     * @return
     */
    public Map<ClazzLevel,List<Long>> getGradeTeacherListMap(School school){
        // 获取学校的年级分布
        List<Integer> gradeList = getGradeDistribute(school);

        // 获取学校下的所有班级
        List<Clazz> classList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(school.getId())
                .toList();
        // 过滤掉disabled的数据和毕业班
        classList = classList.stream().filter(p -> !p.isDisabledTrue() && p.getClazzLevel() != ClazzLevel.INFANT_GRADUATED && p.getClazzLevel() != ClazzLevel.PRIMARY_GRADUATED && p.getClazzLevel() != ClazzLevel.MIDDLE_GRADUATED)
                .collect(Collectors.toList());

        // 过滤掉没有班组的班级
        Set<Long> allClassIds = classList.stream().map(Clazz::getId).collect(Collectors.toSet());
        Map<Long, List<Group>> classGroupMap = asyncGroupServiceClient.getAsyncGroupService().loadGroupsByClazzIds(allClassIds).getUninterruptibly();
        Set<Long> classIds = classGroupMap.values().stream().flatMap(List::stream).map(Group::getClazzId).collect(Collectors.toSet());

        classList = classList.stream().filter(p -> classIds.contains(p.getId())).collect(Collectors.toList());
        // 根据年级分组
        Map<ClazzLevel, List<Clazz>> gradeClassMap = classList.stream().collect(Collectors.groupingBy(Clazz::getClazzLevel, Collectors.toList()));

        // 获取各个班级中班组和老师的对应关系
        Map<Long,Map<Long,Long>> classGroupTeacherMap = fetchGroupTeacherDataByClassIds(classIds, false);

        //年级与老师对应关系
        Map<ClazzLevel,List<Long>> gradeTeacherListMap = new HashMap<>();
        for(Integer p : gradeList) {
            ClazzLevel grade = ClazzLevel.parse(p);
            List<Clazz> gradeClassList = gradeClassMap.get(grade);

            List<Long> teacherIdList = gradeTeacherListMap.get(grade);
            if (CollectionUtils.isEmpty(teacherIdList)){
                teacherIdList = new ArrayList<>();
            }
            // 设置班级信息
            if (CollectionUtils.isNotEmpty(gradeClassList)) {
                for (Clazz clazz : gradeClassList) {
                    Map<Long, Long> tmpGroupTeacherMap = classGroupTeacherMap.get(clazz.getId());
                    if (MapUtils.isNotEmpty(tmpGroupTeacherMap)) {
                        for (Long teacherId : tmpGroupTeacherMap.values()){
                            if (this.isRealTeacher(teacherId)) {
                                teacherIdList.add(teacherId);
                            }
                        }
                    }
                }
            }
            gradeTeacherListMap.put(grade,teacherIdList);
        }

        return gradeTeacherListMap;
    }
}
