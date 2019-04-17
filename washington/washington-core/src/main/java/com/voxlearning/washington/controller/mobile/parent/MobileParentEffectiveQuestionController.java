package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.EffectiveQuestionResultService;
import com.voxlearning.athena.bean.parent.EffectiveQuestionClassResult;
import com.voxlearning.athena.bean.parent.EffectiveQuestionStudentResult;
import com.voxlearning.athena.bean.parent.StudyTypeQuestionCount;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsAlbum;
import com.voxlearning.washington.mapper.AlbumAbilityTagPlanBConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: wei.jiang
 * @Date: Created on 2017/10/13
 */
@Controller
@RequestMapping(value = "/parentMobile/effectiveQuestion")
@Slf4j
public class MobileParentEffectiveQuestionController extends AbstractMobileParentController {

    private static Map<Integer, OrderProductServiceType> afentiImprovedMap = new HashMap<>();

    static {
        afentiImprovedMap.put(103, OrderProductServiceType.AfentiExamImproved);
        afentiImprovedMap.put(102, OrderProductServiceType.AfentiMathImproved);
        afentiImprovedMap.put(101, OrderProductServiceType.AfentiChineseImproved);
    }

    @ImportService(interfaceClass = EffectiveQuestionResultService.class)
    private EffectiveQuestionResultService effectiveQuestionResultService;

    @Inject private RaikouSDK raikouSDK;

    @RequestMapping(value = "/result.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getEffectiveQuestionResult() {
        long sid = getRequestLong("sid");
        int subject_id = getRequestInt("subject_id");
        /*
         * week_flag:
         * 0:本周（本周情况下给前端三科所有数据，其余情况按subject_id给出数据）
         * 1:前一周
         * 2:前前一周
         * 3:前前前一周
         * */
        int week_flag = getRequestInt("week_flag");
        User parent = currentParent();
        if (sid == 0L || parent == null) {
            return MapMessage.errorMessage();
        }
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(sid, Boolean.FALSE);
        List<Map<String, Object>> returnList = new ArrayList<>();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        //根据前端传的week_flag，处理每周首日
        String weekResult = generateYearWeekResult(week_flag);
        if (StringUtils.isBlank(weekResult)) {
            return MapMessage.errorMessage();
        }
        Set<Integer> subjectIdSet;
        if (week_flag == 0) {
            subjectIdSet = new HashSet<>(afentiImprovedMap.keySet());
        } else {
            subjectIdSet = new HashSet<>();
            subjectIdSet.add(subject_id);
        }
        List<Long> groupIds = groupMappers.stream().sorted((o1, o2) -> Integer.compare(o2.getSubject().getId(), o1.getSubject().getId())).map(GroupMapper::getId).collect(Collectors.toList());
        Map<Long, EffectiveQuestionStudentResult> studentResultMap = new HashMap<>();
        Map<Long, EffectiveQuestionClassResult> resultMap = effectiveQuestionResultService.loadGroupResult(weekResult, groupIds, sid);
        //查处年级最佳的groupId，后面用做查年级最佳的详情
        List<Long> gradeBestIds = resultMap.values().stream().filter(e -> e.getGradeBestStudentId() != null && e.getGradeBestStudentId() != 0L).map(EffectiveQuestionClassResult::getGradeBestStudentId).collect(Collectors.toList());
        Map<Long, List<GroupMapper>> gradeBestStudentGroups = deprecatedGroupLoaderClient.loadStudentGroups(gradeBestIds, Boolean.FALSE);
        //获取黑名单信息
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentDetail.getId());
        Boolean studentInGrowthWorldBlacklist = studentExtAttribute != null ? studentExtAttribute.fairylandClosed() : Boolean.FALSE;
        Boolean studentInProductBlackList = userBlacklistServiceClient.isInBlackListByStudent(Collections.singletonList(studentDetail)).getOrDefault(studentDetail.getId(), false);
        for (Integer subjectId : subjectIdSet) {
            Map<String, Object> currentWeekResult = new HashMap<>();
            Long groupId = groupMappers.stream().filter(e -> e.getSubject().getId() == subjectId).map(GroupMapper::getId).findFirst().orElse(null);
            Map<String, Object> effectiveResult;
            List<NewHomework.Location> locations = new ArrayList<>();
            if (groupId != null) {
                EffectiveQuestionClassResult effectiveQuestionClassResult = resultMap.get(groupId);
                Date startDate = DateUtils.stringToDate(weekResult, DateUtils.FORMAT_SQL_DATE);
                Date endDate = WeekRange.newInstance(startDate.getTime()).getEndDate();
                locations = newHomeworkPartLoaderClient.loadNewHomeworkByClazzGroupId(Collections.singleton(groupId), startDate, endDate);
                currentWeekResult.put("big_data_class_data", effectiveQuestionClassResult == null ? "" : JsonUtils.toJson(effectiveQuestionClassResult));
                Set<Long> sidSet = new HashSet<>();
                sidSet.add(sid);
                Long gradeBestGroupId = null;
                if (effectiveQuestionClassResult != null) {
                    sidSet.add(effectiveQuestionClassResult.getBestStudentId());
                    sidSet.add(effectiveQuestionClassResult.getGradeBestStudentId());
                    List<GroupMapper> gradeBestGroups = gradeBestStudentGroups.get(effectiveQuestionClassResult.getGradeBestStudentId());
                    if (CollectionUtils.isNotEmpty(gradeBestGroups)) {
                        gradeBestGroupId = gradeBestGroups.stream().filter(e -> e.getSubject().getId() == subjectId).map(GroupMapper::getId).findFirst().orElse(null);
                    }
                }
                studentResultMap = effectiveQuestionResultService.loadStudentResult(weekResult, groupId, sidSet);
                if (gradeBestGroupId != null) {
                    studentResultMap.putAll(effectiveQuestionResultService.loadStudentResult(weekResult, gradeBestGroupId, sidSet));
                }
                effectiveResult = generateEffectiveResult(effectiveQuestionClassResult, studentDetail, groupId, studentResultMap, weekResult, subjectId, studentInGrowthWorldBlacklist, studentInProductBlackList, locations.size());
            } else {
                EffectiveQuestionStudentResult effectiveQuestionStudentResult = effectiveQuestionResultService.loadStudentResultWithoutGroup(weekResult, subjectId, sid);
                studentResultMap.put(sid, effectiveQuestionStudentResult);
                effectiveResult = generateEffectiveResult(null, studentDetail, null, studentResultMap, weekResult, subjectId, studentInGrowthWorldBlacklist, studentInProductBlackList, locations.size());
            }
            currentWeekResult.put("effective_result", effectiveResult);
            currentWeekResult.put("subject_id", subjectId);
            currentWeekResult.put("big_data_student_data", MapUtils.isEmpty(studentResultMap) ? "" : JsonUtils.toJson(studentResultMap));
            if (week_flag == 0) {
                EffectiveQuestionStudentResult selfDetail = studentResultMap.get(sid);
                Map<String, Object> homeworkDetailMap = new HashMap<>();
                Map<String, Integer> countMap = new HashMap<>();
                if (selfDetail != null && CollectionUtils.isNotEmpty(selfDetail.getQuestionCountList())) {
                    countMap = selfDetail.getQuestionCountList().stream().collect(Collectors.toMap(StudyTypeQuestionCount::getName, StudyTypeQuestionCount::getCount));
                }
                currentWeekResult.put("study_detail", new ArrayList<>());
                if (groupId != null) {
                    Subject subject = groupMappers.stream().filter(e -> Objects.equals(e.getId(), groupId)).map(GroupMapper::getSubject).findFirst().orElse(null);
                    String homeworkSubjectName = subject.getValue() + "作业";
                    homeworkDetailMap = generateHomeworkDetail(studentDetail, locations, countMap.get(homeworkSubjectName));
                    currentWeekResult.put("homework_detail", homeworkDetailMap);
                }
                if (MapUtils.isEmpty(homeworkDetailMap)) {
                    currentWeekResult.put("album_detail", generateAlbumListDetail(studentDetail, subjectId));
                }
            }
            returnList.add(currentWeekResult);
        }
        returnList = returnList.stream().sorted((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("subject_id")), SafeConverter.toInt(o1.get("subject_id")))).collect(Collectors.toList());
        return MapMessage.successMessage().add("effective_list", returnList);
    }


    private Map<String, Object> generateEffectiveResult(EffectiveQuestionClassResult effectiveQuestionClassResult,
                                                        StudentDetail studentDetail,
                                                        Long groupId,
                                                        Map<Long, EffectiveQuestionStudentResult> studentResultMap,
                                                        String weekYear,
                                                        Integer subjectId,
                                                        Boolean growthWorldBlackList,
                                                        Boolean productBlackList,
                                                        Integer homeworkCount) {
        if (studentDetail == null || StringUtils.isBlank(weekYear)) {
            return Collections.emptyMap();
        }
        String dateString = generateDateString(weekYear);
        if (StringUtils.isBlank(dateString)) {
            return Collections.emptyMap();
        }
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("self_name", studentDetail.fetchRealname());
        returnMap.put("self_img", getUserAvatarImgUrl(studentDetail));
        returnMap.put("week_date", dateString);
        //没有effectiveQuestionClassResult就是无学科group的情况了，只返回自己的数据
        if (effectiveQuestionClassResult == null) {
            EffectiveQuestionStudentResult effectiveQuestionStudentResult = studentResultMap.get(studentDetail.getId());
            Integer selfCount = 0;
            if (effectiveQuestionStudentResult != null && CollectionUtils.isNotEmpty(effectiveQuestionStudentResult.getQuestionCountList())) {
                for (StudyTypeQuestionCount questionCount : effectiveQuestionStudentResult.getQuestionCountList()) {
                    selfCount += questionCount.getCount() != null ? questionCount.getCount() : 0;
                }
                returnMap.put("self_count_detail", generateCountDetail(effectiveQuestionStudentResult.getQuestionCountList(), studentDetail, subjectId, growthWorldBlackList, productBlackList, homeworkCount, SafeConverter.toInt(effectiveQuestionStudentResult.getRepairCount()), Boolean.FALSE));
            }
            returnMap.put("self_count", selfCount);
            //FIXME:按需求处理显示内容
            returnMap.put("is_without_group", Boolean.TRUE);
            return returnMap;
        }
        Long bestStudentId = effectiveQuestionClassResult.getBestStudentId();
        Long sid = studentDetail.getId();
        Long gradeBestStudentId = effectiveQuestionClassResult.getGradeBestStudentId();
        GroupStudentTuple tuple = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByStudentId(sid)
                .stream()
                .filter(e -> Objects.equals(groupId, e.getGroupId()))
                .findFirst()
                .orElse(null);
        //今天是否换过班
        boolean isCurrentDay = false;
        if (tuple != null) {
            isCurrentDay = DateUtils.isSameDay(tuple.getUpdateTime(), new Date());
        }
        Integer bestCount = effectiveQuestionClassResult.getBestCount() != null ? effectiveQuestionClassResult.getBestCount() : 0;
        Integer gradeBestCount = effectiveQuestionClassResult.getGradeBestCount() != null ? effectiveQuestionClassResult.getGradeBestCount() : 0;
        Integer myCount = effectiveQuestionClassResult.getMyCount() != null ? effectiveQuestionClassResult.getMyCount() : 0;

        //班级做题人数是否小于5
        Boolean isLess = effectiveQuestionClassResult.getEffectiveClassmateCount() == null || effectiveQuestionClassResult.getEffectiveClassmateCount() <= 5 ? Boolean.TRUE : Boolean.FALSE;
        returnMap.put("is_less", isLess);
        Boolean isGradeLess = effectiveQuestionClassResult.getEffectiveGrademateCount() == null || effectiveQuestionClassResult.getEffectiveGrademateCount() <= 5 ? Boolean.TRUE : Boolean.FALSE;
        //年级做题人数是否小于5
        returnMap.put("is_grade_less", isGradeLess);
        returnMap.put("is_today_change_group", isCurrentDay);
        EffectiveQuestionStudentResult selfResult = studentResultMap.get(sid);
        EffectiveQuestionStudentResult bestResult = studentResultMap.get(bestStudentId);
        EffectiveQuestionStudentResult gradeBestResult = studentResultMap.get(gradeBestStudentId);
        StudentDetail bestStudentDetail = null;
        /*
         * 1、今日无换班且班级人数大于最小限制，则返回班级最佳
         * 2、今日无换班且班级人数小于最小限制，则返回年级最佳
         * */
        if (!isCurrentDay && !isLess) {
            bestStudentDetail = studentLoaderClient.loadStudentDetail(bestStudentId);
            returnMap.put("best_count", effectiveQuestionClassResult.getBestCount());
            Boolean countBest = bestCount != 0 && myCount != 0 && Objects.equals(bestCount, myCount) ? Boolean.TRUE : Boolean.FALSE;
            if (Objects.equals(bestStudentId, sid) || countBest) {
                returnMap.put("self_best", Boolean.TRUE);
            }
        }
        if (!isCurrentDay && isLess) {
            bestStudentDetail = studentLoaderClient.loadStudentDetail(gradeBestStudentId);
            returnMap.put("best_count", effectiveQuestionClassResult.getGradeBestCount());
            Boolean gradeCountBest = gradeBestCount != 0 && myCount != 0 && Objects.equals(gradeBestCount, myCount) ? Boolean.TRUE : Boolean.FALSE;
            if (Objects.equals(gradeBestStudentId, sid) || gradeCountBest) {
                returnMap.put("self_best", Boolean.TRUE);
            }
        }
        if (bestStudentDetail != null) {
            returnMap.put("best_img", getUserAvatarImgUrl(bestStudentDetail));
            returnMap.put("best_name", bestStudentDetail.fetchRealname());
        }

        returnMap.put("self_count", SafeConverter.toInt(effectiveQuestionClassResult.getMyCount()));
        //FIXME:按需求处理显示内容
        if (selfResult != null) {
            returnMap.put("self_count_detail", generateCountDetail(selfResult.getQuestionCountList(), studentDetail, subjectId, growthWorldBlackList, productBlackList, homeworkCount, SafeConverter.toInt(selfResult.getRepairCount()), Boolean.FALSE));
        }
        if (!isLess && !isCurrentDay) {
            if (bestResult != null) {
                returnMap.put("best_count_detail", generateCountDetail(bestResult.getQuestionCountList(), bestStudentDetail, subjectId, Boolean.FALSE, Boolean.FALSE, homeworkCount, SafeConverter.toInt(bestResult.getRepairCount()), Boolean.FALSE));
            }
            returnMap.put("excellent_min_count", SafeConverter.toInt(effectiveQuestionClassResult.getExcellentMinCount()));
            returnMap.put("good_min_count", SafeConverter.toInt(effectiveQuestionClassResult.getGoodMinCount()));
            returnMap.put("region_excellent_min_count", SafeConverter.toInt(effectiveQuestionClassResult.getRegionExcellentMinCount()));
            returnMap.put("region_excellent_max_count", SafeConverter.toInt(effectiveQuestionClassResult.getRegionExcellentMaxCount()));
        }
        if (isLess && !isCurrentDay) {
            if (gradeBestResult != null) {
                returnMap.put("best_count_detail", generateCountDetail(gradeBestResult.getQuestionCountList(), bestStudentDetail, subjectId, Boolean.FALSE, Boolean.FALSE, homeworkCount, SafeConverter.toInt(gradeBestResult.getRepairCount()), Boolean.TRUE));
            }
            returnMap.put("excellent_min_count", SafeConverter.toInt(effectiveQuestionClassResult.getGradeExcellentMinCount()));
            returnMap.put("good_min_count", SafeConverter.toInt(effectiveQuestionClassResult.getGradeGoodMinCount()));
        }
        returnMap.put("is_without_group", Boolean.FALSE);
        return returnMap;
    }

    /**
     * 作业详情数据
     */
    private Map<String, Object> generateHomeworkDetail(StudentDetail studentDetail, List<NewHomework.Location> locations, Integer count) {
        if (studentDetail == null || CollectionUtils.isEmpty(locations)) {
            return Collections.emptyMap();
        }
        Set<String> accomplishmentIds = new HashSet<>();
        locations.forEach(p -> accomplishmentIds.add(NewAccomplishment.ID.build(p.getCreateTime(),
                p.getSubject(), p.getId()).toString()));
        Map<String, NewAccomplishment> accomplishmentMap = newAccomplishmentLoaderClient.loadNewAccomplishments(accomplishmentIds);
        Map<Subject, List<NewHomework.Location>> subjectHomeworkList = locations.stream().collect(Collectors.groupingBy(NewHomework.Location::getSubject));
        Map<String, Object> finishMapper = new HashMap<>();
        if (MapUtils.isEmpty(subjectHomeworkList)) {
            finishMapper.put("total_count", 0);
        }
        finishMapper.put("icon", getCdnBaseUrlStaticSharedWithSep() + "public/skin/parentMobile/images/app_icon/homeworkicon.png");
        subjectHomeworkList.forEach((k, v) -> {
            finishMapper.put("total_count", v.size());
            Integer finishCount = 0;
            for (NewHomework.Location e : v) {
                NewAccomplishment newAccomplishment = accomplishmentMap.get(NewAccomplishment.ID.build(e.getCreateTime(), e.getSubject(), e.getId()).toString());
                Boolean selfFinish = newAccomplishment == null ? Boolean.FALSE : newAccomplishment.contains(studentDetail.getId());
                if (selfFinish) {
                    finishCount++;
                }
            }
            finishMapper.put("finish_count", finishCount);
            finishMapper.put("count", count != null ? count : 0);
        });
        return finishMapper;
    }

    /**
     * 格式化周数据
     */
    private String generateYearWeekResult(Integer weekFlag) {
        if (weekFlag == null) {
            return "";
        }
        WeekRange current = WeekRange.current();
        String result;
        if (weekFlag == 0) {
            result = DateUtils.dateToString(current.getStartDate(), "yyyy-MM-dd");
        } else {
            result = DateUtils.dateToString(DateUtils.addDays(current.getStartDate(), -weekFlag * 7), "yyyy-MM-dd");
        }
        return result;
    }

    private String generateDateString(String s) {
        Date startDate = DateUtils.stringToDate(s, "yyyy-MM-dd");
        if (WeekRange.current().getStartDate().equals(startDate)) {
            return "本周";
        }
        Date endDate = DateUtils.addDays(startDate, 6);
        String start = DateUtils.dateToString(startDate, "MM.dd");
        String end = DateUtils.dateToString(endDate, "MM.dd");
        return start + " - " + end;
    }

    /**
     * 根据条件取相应的增值产品
     * 1、过滤付费、成长世界黑名单
     * 2、根据StudyGrowthType过滤需要的产品
     */
    private Map<StudyGrowthType, Boolean> generateAvailableProduct(StudentDetail studentDetail, Integer subjectId) {
        if (studentDetail == null) {
            return Collections.emptyMap();
        }
        Map<StudyGrowthType, Boolean> growthTypeMap = new HashMap<>();
        Map<SelfStudyType, DayRange> selfStudyTypeDayRangeMap = parentSelfStudyPublicHelper.moneySSTLastDayMap(studentDetail.getId(), false);
        Set<String> appkeySet = selfStudyTypeDayRangeMap.keySet().stream().map(SelfStudyType::getOrderProductServiceType).collect(Collectors.toSet());
        if (MapUtils.isNotEmpty(selfStudyTypeDayRangeMap)) {
            Set<StudyGrowthType> studyGrowthTypeSet = StudyGrowthType.getTypeBySubjectId(subjectId).stream().filter(e -> StringUtils.isNotBlank(e.appKey)).collect(Collectors.toSet());
            studyGrowthTypeSet.forEach(p -> {
                SelfStudyType selfStudyType = SelfStudyType.fromOrderType(OrderProductServiceType.safeParse(p.appKey));
                if (selfStudyType != null) {
                    DayRange dayRange = selfStudyTypeDayRangeMap.get(selfStudyType);
                    if (dayRange != null) {
                        if (dayRange.getEndTime() > DayRange.current().getEndTime()) {
                            growthTypeMap.put(p, Boolean.FALSE);
                        }
                        if (dayRange.getEndTime() < DayRange.current().getEndTime() && dayRange.getEndTime() > DayRange.newInstance(DateUtils.addDays(new Date(), -10).getTime()).getEndTime()) {
                            growthTypeMap.put(p, Boolean.TRUE);
                        }
                    }

                }
            });
            //对小U提高版的特殊处理
            String afentiImprovedAppkey = afentiImprovedMap.get(subjectId).name();
            StudyGrowthType afentiBySubjectId = StudyGrowthType.getAfentiBySubjectId(subjectId);
            //这里是处理没有购买普通版afenti或者普通版阿分提已过期的情况
            if ((!growthTypeMap.keySet().contains(afentiBySubjectId) || growthTypeMap.get(afentiBySubjectId) == Boolean.TRUE) && appkeySet.contains(afentiImprovedAppkey)) {
                SelfStudyType selfStudyType = SelfStudyType.fromOrderType(OrderProductServiceType.safeParse(afentiImprovedAppkey));
                if (selfStudyType != null) {
                    DayRange dayRange = selfStudyTypeDayRangeMap.get(selfStudyType);
                    if (dayRange != null) {
                        if (dayRange.getEndTime() > DayRange.current().getEndTime()) {
                            growthTypeMap.put(afentiBySubjectId, Boolean.FALSE);
                        }
                        if (dayRange.getEndTime() < DayRange.current().getEndTime() && dayRange.getEndTime() > DayRange.newInstance(DateUtils.addDays(new Date(), -10).getTime()).getEndTime()) {
                            growthTypeMap.put(afentiBySubjectId, Boolean.TRUE);
                        }
                    }

                }
            }
        }
        return growthTypeMap;
    }

    /**
     * 根据大数据接口返回的详情，来取相应的产品数据
     */
    private List<Map<String, Object>> generateCountDetail(List<StudyTypeQuestionCount> countList, StudentDetail studentDetail, Integer subjectId, Boolean growthWorldBlackList,
                                                          Boolean productBlackList, Integer homeworkCount, Integer repairCount, Boolean isGradeBest) {
        if (CollectionUtils.isEmpty(countList) || studentDetail == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        //先处理作业
        StudyTypeQuestionCount studyTypeQuestionCount = countList.stream().filter(e -> StringUtils.equals(Subject.fromSubjectId(subjectId).getValue() + "作业", e.getName())).findFirst().orElse(null);
        if (studyTypeQuestionCount != null) {
            Map<String, Object> homeworkMap = new HashMap<>();
            homeworkMap.put("name", studyTypeQuestionCount.getName());
            if (homeworkCount == 0 && repairCount == 0 && !isGradeBest) {
                homeworkMap.put("count", "老师未布置");
            } else {
                homeworkMap.put("count", studyTypeQuestionCount.getCount() != null ? studyTypeQuestionCount.getCount() : 0);
                //添加补做作业数
                homeworkMap.put("repair_count", repairCount != null ? repairCount : 0);
            }
            returnList.add(homeworkMap);
        }
        //再处理增值产品
        List<String> bigDataTypes = new ArrayList<>();
        //一个新逻辑：大数据返回的结果分为两类：1、如果count不为0则直接显示，不做黑名单判断；2、如果count有0，则做黑名单判断。
        List<String> zeroCountList = new ArrayList<>();
        countList.stream().filter(e -> !StringUtils.equals(Subject.fromSubjectId(subjectId).getValue() + "作业", e.getName())).forEach(e -> {
            if (e.getCount() == null || e.getCount() == 0) {
                zeroCountList.add(e.getName());
            } else {
                bigDataTypes.add(e.getName());
            }
        });
        //加入免费产品的type
        if (!growthWorldBlackList && !productBlackList) {
            bigDataTypes.addAll(StudyGrowthType.getTypeBySubjectId(subjectId).stream().filter(e -> StringUtils.isBlank(e.getAppKey()) && zeroCountList.contains(e.getBigDataType())).map(StudyGrowthType::getBigDataType).collect(Collectors.toList()));
        }
        if (!productBlackList) {
            // 这里是付费产品的type
            bigDataTypes.addAll(StudyGrowthType.getTypeBySubjectId(subjectId).stream().filter(e -> StringUtils.isNotBlank(e.getAppKey()) && zeroCountList.contains(e.getBigDataType())).map(StudyGrowthType::getBigDataType).collect(Collectors.toList()));
        }
        List<StudyTypeQuestionCount> studyTypeCountList = countList.stream().filter(e -> bigDataTypes.contains(e.getName())).collect(Collectors.toList());
        studyTypeCountList.forEach(e -> {
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put("name", e.getName());
            typeMap.put("count", e.getCount() != null ? e.getCount() : 0);
            returnList.add(typeMap);
        });

        return returnList;
    }

    /**
     * 如果作业、和增值产品均未取得数据，则展示资讯专辑的兜底方案
     */
    private List<Map<String, Object>> generateAlbumListDetail(StudentDetail studentDetail, Integer subjectId) {
        if (studentDetail == null || subjectId == null || subjectId == 0) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> returnList = new ArrayList<>();
        List<String> albumIds = getFitJxtNewsAlbumWithConfig(Collections.singleton(studentDetail.getClazzLevelAsInteger()))
                .stream()
                .filter(e -> Objects.equals(e.getSubjectId(), subjectId))
                .map(AlbumAbilityTagPlanBConfig::getAlbumIds)
                .findFirst()
                .orElse(Collections.emptyList());
        Map<String, JxtNewsAlbum> albumMap = jxtNewsLoaderClient.getJxtNewsAlbumByAlbumIds(albumIds);
        albumMap.forEach((k, v) -> {
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("album_id", v.getId());
            returnMap.put("title", v.getTitle());
            returnMap.put("icon", ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host") + v.getHeadImg());
            //副标题
            returnMap.put("sub_title", StringUtils.isNotBlank(v.getSubTitle()) ? v.getSubTitle() : "");
            returnMap.put("is_album", Boolean.TRUE);
            returnMap.put("album_type", v.getJxtNewsAlbumContentType());
            returnList.add(returnMap);
        });
        return returnList;
    }


    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private enum StudyGrowthType {
        AfentiEnglish(
                "小U英语同步练习",
                "今日“成长世界-英语岛”自学任务：",
                "AfentiExam",
                "",
                103,
                "小U英语同步练习",
                "/public/skin/parentMobile/images/app_icon/icon_afenti_jzen.png"),
        AfentiMath(
                "小U数学同步练习",
                "今日“成长世界-数学岛”自学任务：",
                "AfentiMath",
                "",
                102,
                "小U数学同步练习",
                "/public/skin/parentMobile/images/app_icon/icon_afenti_jzmath.png"),
        AfentiChinese(
                "小U语文同步练习",
                "今日“成长世界-语文岛”自学任务：",
                "AfentiChinese",
                "",
                101,
                "小U语文同步练习",
                "/public/skin/parentMobile/images/app_icon/icon_afenti_jzcn.png"),
        UsaAdventure(
                "走遍美国学英语",
                "今日“成长世界-英语岛”自学任务：",
                "UsaAdventure",
                "",
                103,
                "走遍美国学英语",
                "/public/skin/parentMobile/images/app_icon/icon_usa_app.png"),
        Arithmetic(
                "速算脑力王",
                "今日“成长世界-数学岛”自学任务：",
                "Arithmetic",
                "",
                102,
                "速算脑力王",
                "/public/skin/parentMobile/images/app_icon/icon_arithmetic.png"),
        ChineseHero(
                "字词英雄",
                "今日“成长世界-语文岛”自学任务：",
                "ChineseHero",
                "",
                101,
                "字词英雄",
                "/public/skin/parentMobile/images/app_icon/icon_arithmetic.png"),
        ChineseSynPractice(
                "语文同步字词练习",
                "今日“成长世界-语文岛”自学任务：",
                "ChineseSynPractice",
                "",
                101,
                "语文同步练",
                "/public/skin/parentMobile/images/app_icon/chinesetblx.png"),
        AfentiEnglish_Reading(
                "成长世界“英语岛”每日免费自学",
                "今日绘本阅读任务（3-10题）：",
                "",
                "reading",
                103,
                "成长世界免费自学",
                "public/skin/parentMobile/images/app_icon/growthworldicon.png"),
        AfentiEnglish_CTB(
                "成长世界“英语岛”每日免费自学",
                "今日错题本任务（3题）：",
                "",
                "finish_ctb_homework",
                103,
                "成长世界免费自学",
                "public/skin/parentMobile/images/app_icon/growthworldicon.png"),
        AfentiMath_Reading(
                "成长世界“数学岛”每日免费自学",
                "今日绘本阅读任务（3-10题）：",
                "",
                "reading",
                102,
                "成长世界免费自学",
                "public/skin/parentMobile/images/app_icon/growthworldicon.png"),
        AfentiMath_CTB(
                "成长世界“数学岛”每日免费自学",
                "今日错题本任务（3题）：",
                "",
                "finish_ctb_homework",
                102,
                "成长世界免费自学",
                "public/skin/parentMobile/images/app_icon/growthworldicon.png"),
        AfentiChinese_CTB(
                "成长世界“语文岛”每日免费自学",
                "今日错题本任务（3题）：",
                "",
                "finish_ctb_homework",
                101,
                "成长世界免费自学",
                "public/skin/parentMobile/images/app_icon/growthworldicon.png");
        private final String title;
        private final String statusText;
        private final String appKey; //付费产品
        private final String eventType; //免费产品
        private final Integer subjectId;
        private final String bigDataType; //大数据标识
        private final String icon;

        public static List<StudyGrowthType> getTypeBySubjectId(Integer subjectId) {
            return Stream.of(values()).filter(e -> Objects.equals(e.getSubjectId(), subjectId)).collect(Collectors.toList());
        }

        public static StudyGrowthType getAfentiBySubjectId(Integer subjectId) {
            return Stream.of(values()).filter(e -> Objects.equals(e.getSubjectId(), subjectId) && e.getAppKey().contains("Afenti")).findFirst().orElse(null);
        }

        public static List<StudyGrowthType> getTypeByBigDataTypesAndSubjectId(Collection<String> bigDataTypes, Integer subjectId) {
            return Stream.of(values()).filter(e -> Objects.equals(e.getSubjectId(), subjectId) && bigDataTypes.contains(e.getBigDataType()) && StringUtils.isNotBlank(e.getAppKey())).collect(Collectors.toList());
        }
    }
}
