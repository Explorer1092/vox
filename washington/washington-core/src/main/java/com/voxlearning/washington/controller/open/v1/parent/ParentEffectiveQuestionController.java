package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.athena.api.EffectiveQuestionResultService;
import com.voxlearning.athena.bean.parent.EffectiveQuestionClassResult;
import com.voxlearning.athena.bean.parent.EffectiveQuestionStudentResult;
import com.voxlearning.athena.bean.parent.StudyTypeQuestionCount;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author shiwei.liao
 * @since 2017-10-12
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/parent/effective_question/")
public class ParentEffectiveQuestionController extends AbstractParentApiController {

    @ImportService(interfaceClass = EffectiveQuestionResultService.class)
    private EffectiveQuestionResultService effectiveQuestionResultService;

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @RequestMapping(value = "clazz_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getClazzInfo() {
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        User parent = getApiRequestUser();
        String system = getRequestString(REQ_SYS);
        if (!studentIsParentChildren(parent.getId(), studentId)) {
            return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
        }
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        //没有班级不显示有效做题量。20001审核账号也没有班级。
        if (CollectionUtils.isEmpty(groupMappers)) {
            return successMessage().add(RES_RESULT_SHOW_EFFECTIVE_QUESTION, Boolean.FALSE);
        }
        //毕业班、中学用户不展示做题量
        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);
        if (student.getClazz() == null || student.getClazz().isTerminalClazz() || student.isJuniorStudent() || student.isSeniorStudent()) {
            return successMessage().add(RES_RESULT_SHOW_EFFECTIVE_QUESTION, Boolean.FALSE);
        }
        Integer homeworkCount = generateEffectiveHomeworkCount(studentId, groupMappers);
        Set<Long> groupIds = groupMappers.stream().map(GroupMapper::getId).collect(Collectors.toSet());
        List<String> mondays = getMondays(4);
        MapMessage mapMessage = successMessage();
        //个人4周的做题统计
        Map<String, Integer> studentTotalCountMap = effectiveQuestionResultService.loadStudentWeeksResult(studentId, mondays);
        List<Map<String, Object>> mapList = new ArrayList<>();
        mondays.stream()
                .sorted(StringUtils::compare)
                .forEach(key -> {
                    //把周一的日期转化成一周的日期段
                    String weekDateStr = weekToDate(key);
                    Map<String, Object> map = new HashMap<>();
                    map.put(RES_RESULT_WEEK_DATE_STR, weekDateStr);
                    map.put(RES_RESULT_MY_WEEK_COUNT, MapUtils.isEmpty(studentTotalCountMap) ? 0 : SafeConverter.toInt(studentTotalCountMap.get(key)));
                    mapList.add(map);
                });
        mapMessage.add(RES_RESULT_WEEK_COUNT_LIST, mapList);
        //班级本周做题统计
        //这里周一的时候取上周的数据。所以周一的时候去第1个。其他时候取第0个
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int i = calendar.get(Calendar.DAY_OF_WEEK);
        EffectiveQuestionClassResult classResult;
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        if (i == Calendar.MONDAY && VersionUtil.compareVersion(ver, "2.0.8") > 0) {
            classResult = effectiveQuestionResultService.loadClassResult(mondays.get(1), groupIds, studentId);
            mapMessage.add(RES_RESULT_SHOW_TITLE, "上周有效做题量");
        } else {
            classResult = effectiveQuestionResultService.loadClassResult(mondays.get(0), groupIds, studentId);
            mapMessage.add(RES_RESULT_SHOW_TITLE, "本周有效做题量");
        }
        if (RuntimeMode.current().le(Mode.STAGING)) {
            mapMessage.add("big_data_json", classResult == null ? "" : JsonUtils.toJson(classResult));
        }
        mapMessage.add(RES_RESULT_EFFECTIVE_QUESTION_FAQ_URL, "/view/mobile/parent/question?hash=%E4%BB%80%E4%B9%88%E6%98%AF%E6%9C%89%E6%95%88%E7%BB%83%E4%B9%A0%E9%87%8F");
        mapMessage.add(RES_RESULT_EFFECTIVE_QUESTION_DETAIL_URL, "/view/mobile/parent/exercise_quantity/detail.vpage");
        mapMessage.add(RES_RESULT_SHOW_EFFECTIVE_QUESTION, Boolean.TRUE);
        //如果当天加入新班级。只需要个人数据
        for (Long groupId : groupIds) {
            GroupStudentTuple tuple = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .findByStudentId(studentId)
                    .stream()
                    .filter(e -> Objects.equals(groupId, e.getGroupId()))
                    .findFirst()
                    .orElse(null);
            if (tuple == null || (tuple.getUpdateTime() != null && DayRange.current().contains(tuple.getUpdateTime()))) {
                mapMessage.add(RES_RESULT_CLAZZ_BEST_STUDENT_IMG, "");
                mapMessage.add(RES_RESULT_CLAZZ_BEST_COUNT, 0);
                mapMessage.add(RES_RESULT_MY_QUESTION_COUNT, classResult == null ? 0 : SafeConverter.toInt(classResult.getMyCount()));
                mapMessage.add(RES_RESULT_CLAZZ_EXCELLENT_MIN_COUNT, 0);
                mapMessage.add(RES_RESULT_CLAZZ_GOOD_MIN_COUNT, 0);
                mapMessage.add(RES_RESULT_SHOW_CONTENT, "今日加入了新班级，暂无班级对比数据哦");
                return mapMessage;
            }
        }
        if (classResult == null || (SafeConverter.toInt(classResult.getEffectiveClassmateCount()) <= 5 && SafeConverter.toInt(classResult.getEffectiveGrademateCount()) <= 5)) {
            //没有任何数据。直接返回
            mapMessage.add(RES_RESULT_CLAZZ_BEST_STUDENT_IMG, "");
            mapMessage.add(RES_RESULT_CLAZZ_BEST_COUNT, 0);
            mapMessage.add(RES_RESULT_MY_QUESTION_COUNT, classResult == null ? 0 : SafeConverter.toInt(classResult.getMyCount()));
            String homeworkText = generateEffectiveHomeworkText(student.fetchRealnameIfBlankId(), classResult == null ? 0 : SafeConverter.toInt(classResult.getMyCount()), homeworkCount, system);
            if (StringUtils.isNotBlank(homeworkText)) {
                mapMessage.add(RES_RESULT_MY_QUESTION_TEXT, homeworkText);
            }
            mapMessage.add(RES_RESULT_CLAZZ_EXCELLENT_MIN_COUNT, 0);
            mapMessage.add(RES_RESULT_CLAZZ_GOOD_MIN_COUNT, 0);
            mapMessage.add(RES_RESULT_SHOW_CONTENT, "努力做题争当班级最佳吧");
            return mapMessage;
        }
        //个人做题量
        mapMessage.add(RES_RESULT_MY_QUESTION_COUNT, SafeConverter.toInt(classResult.getMyCount()));
        String homeworkText = generateEffectiveHomeworkText(student.fetchRealnameIfBlankId(), SafeConverter.toInt(classResult.getMyCount()), homeworkCount, system);
        if (StringUtils.isNotBlank(homeworkText)) {
            mapMessage.add(RES_RESULT_MY_QUESTION_TEXT, homeworkText);
        }
        //区排名信息。
        mapMessage.add(RES_RESULT_REGION_EXCELLENT_MIN_COUNT, SafeConverter.toInt(classResult.getRegionExcellentMinCount()));
        mapMessage.add(RES_RESULT_REGION_EXCELLENT_MAX_COUNT, SafeConverter.toInt(classResult.getRegionExcellentMaxCount()));
        //班级人数不足用年级的数据
        if (SafeConverter.toInt(classResult.getEffectiveClassmateCount()) <= 5) {
            Long bestStudentId = classResult.getGradeBestStudentId();
            User bestUser = raikouSystem.loadUser(bestStudentId);
            mapMessage.add(RES_RESULT_GRADE_BEST_STUDENT_NAME, bestUser == null ? "" : bestUser.fetchRealname());
            mapMessage.add(RES_RESULT_GRADE_BEST_STUDENT_IMG, bestUser == null ? "" : getUserAvatarImgUrl(bestUser));
            mapMessage.add(RES_RESULT_GRADE_BEST_COUNT, SafeConverter.toInt(classResult.getGradeBestCount()));
            mapMessage.add(RES_RESULT_GRADE_EXCELLENT_MIN_COUNT, SafeConverter.toInt(classResult.getGradeExcellentMinCount()));
            mapMessage.add(RES_RESULT_GRADE_GOOD_MIN_COUNT, SafeConverter.toInt(classResult.getGradeGoodMinCount()));
            mapMessage.add(RES_RESULT_SHOW_CONTENT, "当前班级做题人数较少，看看年级对比吧～");
        } else {
            Long bestStudentId = classResult.getBestStudentId();
            User bestUser = raikouSystem.loadUser(bestStudentId);
            mapMessage.add(RES_RESULT_CLAZZ_BEST_STUDENT_NAME, bestUser == null ? "" : bestUser.fetchRealname());
            mapMessage.add(RES_RESULT_CLAZZ_BEST_STUDENT_IMG, bestUser == null ? "" : getUserAvatarImgUrl(bestUser));
            mapMessage.add(RES_RESULT_CLAZZ_BEST_COUNT, SafeConverter.toInt(classResult.getBestCount()));
            mapMessage.add(RES_RESULT_CLAZZ_EXCELLENT_MIN_COUNT, SafeConverter.toInt(classResult.getExcellentMinCount()));
            mapMessage.add(RES_RESULT_CLAZZ_GOOD_MIN_COUNT, SafeConverter.toInt(classResult.getGoodMinCount()));
        }
        return mapMessage;
    }

    private String weekToDate(String s) {
        Date startDate = DateUtils.stringToDate(s, "yyyy-MM-dd");
        if (DateUtils.isSameDay(startDate, WeekRange.current().getStartDate())) {
            return "本周";
        }
        Date endDate = DateUtils.addDays(startDate, 6);
        String start = DateUtils.dateToString(startDate, "MM.dd");
        String end = DateUtils.dateToString(endDate, "MM.dd");
        return start + " - " + end;
    }

    private List<String> getMondays(int count) {
        WeekRange weekRange = WeekRange.current();
        int i = 1;
        List<String> days = new ArrayList<>();
        while (i <= count) {
            String day = DateUtils.dateToString(weekRange.getStartDate(), "yyyy-MM-dd");
            days.add(day);
            i++;
            weekRange = weekRange.previous();
        }
        return days;
    }

    private Integer generateEffectiveHomeworkCount(Long sid, List<GroupMapper> groupMappers) {
        if (sid == null || sid == 0L) {
            return 0;
        }
        String currentWeekStart = DateUtils.dateToString(WeekRange.current().getStartDate(), "yyyy-MM-dd");
        Integer homeworkCount = 0;
        for (GroupMapper groupMapper : groupMappers) {
            Map<Long, EffectiveQuestionStudentResult> studentResultMap = effectiveQuestionResultService.loadStudentResult(currentWeekStart, groupMapper.getId(), Collections.singleton(sid));
            if (studentResultMap.get(sid) != null && CollectionUtils.isNotEmpty(studentResultMap.get(sid).getQuestionCountList())) {
                StudyTypeQuestionCount studyTypeQuestionCount = studentResultMap.get(sid).getQuestionCountList().stream().filter(e -> StringUtils.equals(Subject.fromSubjectId(groupMapper.getSubject().getId()).getValue() + "作业", e.getName())).findFirst().orElse(null);
                if (studyTypeQuestionCount == null) {
                    continue;
                }
                homeworkCount += SafeConverter.toInt(studyTypeQuestionCount.getCount());
            }
        }
        return homeworkCount;
    }


    private String generateEffectiveHomeworkText(String studentName, Integer totalCount, Integer homeworkCount, String system) {
        Integer selfStudyCount = totalCount - homeworkCount;
        String htmlTemplate;
        if ("android".equals(system)) {
            htmlTemplate = "<font face='PingFangSC-Regular' size='28px' color='#4A5060'>%s本周作业</font><font face='PingFangSC-Regular' size='28px' color='#00B38A'>%s</font><font face='PingFangSC-Regular' size='28px' color='#4A5060'>有效练习，自学</font><font face='PingFangSC-Regular' size='28px' color='#00B38A'>%s</font><font face='PingFangSC-Regular' size='28px' color='#4A5060'>有效练习</font>";
        } else {
            htmlTemplate = "<span style='font-family:.PingFangSC-Regular;font-size: 28px;color:#4A5060;letter-spacing:0;line-height:44px;'>%s本周作业<span style='color: #00B38A;'>%s</span>有效练习，自学<span style='color: #00B38A;'>%s</span>有效练习</span>";
        }
        return String.format(htmlTemplate, studentName, homeworkCount, selfStudyCount);
    }


}
