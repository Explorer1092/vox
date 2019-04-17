/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.controller.parent;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.FlowerSourceType;
import com.voxlearning.utopia.entity.smartclazz.SmartClazzIntegralHistory;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.clazz.client.SmartClazzServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.homework.api.mapper.WechatHomeworkMapper;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkProcessResultLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkResultLoaderClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.mapper.QuestionMapper;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Flower;
import com.voxlearning.utopia.service.user.api.entities.FlowerGratitude;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.wechat.controller.AbstractParentWebController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.UNKNOWN;

/**
 * @author Xin Xin
 * @since 10/23/15
 */
@Controller
@RequestMapping(value = "/parent/homework")
public class ParentHomeworkController extends AbstractParentWebController {

    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject private NewHomeworkProcessResultLoaderClient newHomeworkProcessResultLoaderClient;
    @Inject private NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private FlowerServiceClient flowerServiceClient;
    @Inject private SmartClazzServiceClient smartClazzServiceClient;

    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index() {
        return "/parent/downloadGuide";
    }

    //查询学生的各科作业列表
    @RequestMapping(value = "/loadhomeworks.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadHomeworks() {
//        Long studentId = getRequestLong("sid");
//
//        if (0L == studentId) {
//            return MapMessage.errorMessage("参数无效");
//        }
//
//        try {
//            //取老师
//            List<Teacher> teachers = userAggregationLoaderClient.loadStudentTeachers(studentId).stream().map(ClazzTeacher::getTeacher).collect(Collectors.toList());
//            Set<Subject> subjects = new HashSet<>();
//            teachers.forEach(t -> subjects.add(t.getSubject()));
//            //取作业
//            MapMessage mapMessage = new MapMessage();
//            for (Subject subject : subjects) {
//                if (subject != Subject.ENGLISH && subject != Subject.MATH) continue;
//
//                List<WechatHomeworkMapper> lst = new ArrayList<>();
//                if (subject == Subject.MATH) {
//                    lst = newHomeworkLoaderClient.loadHomeworksInOneMonth(studentId, Subject.MATH);
//                    buildMoreInfo(lst, studentId);
//                } else {
//                    lst.addAll(getHomeworkWithSubject(studentId, subject));
//                }
//                //将WechatHomeworkMapper添加前端需要的参数
//                List<Map<String, Object>> mapperList = new ArrayList<>();
//                for (WechatHomeworkMapper mapper : lst) {
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("isFuture", mapper.getStartTime().toInstant().isAfter(Instant.now()));
//                    //新体系重构中。直接全部先显示无.
////                    map.put("quiz", mapper.getHomeworkLocation().getHomeworkType().name().contains("QUIZ"));
//                    map.put("quiz", Boolean.FALSE);
////                    map.put("workbook", mapper.getHomeworkLocation().getHomeworkType().name().contains("WORKBOOK"));
//                    map.put("workbook", Boolean.FALSE);
////                    map.put("subjective", mapper.getHomeworkLocation().getHomeworkType().name().contains("SUBJECTIVE"));
//                    map.put("subjective", Boolean.FALSE);
//                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//                    map.put("createDate", formatter.format(mapper.getCreateTime()));
//                    formatter = new SimpleDateFormat("HH:mm");
//                    map.put("createTime", formatter.format(mapper.getCreateTime()));
//                    formatter = new SimpleDateFormat("MM月dd日");
//                    map.put("startDate", formatter.format(mapper.getStartTime()));
//                    //过期得和已检查的不能送花
//                    if (mapper.isChecked() || mapper.getEndTime().before(new Date())) {
//                        map.put("canSendFlower", false);
//                    }
//                    if (null == mapper.getWrongCount()) {
//                        mapper.setWrongCount(0);
//                    }
//                    map.put("homework", mapper);
//                    mapperList.add(map);
//                }
//
//                Map<String, Object> homeworks = new HashMap<>();
//                homeworks.put("homeworks", mapperList);
//                homeworks.put("unfinishedCount", lst.stream().filter(e -> Boolean.FALSE.equals(e.isFinished())).count());
//                homeworks.put("todayHomeworkCount", lst.stream().filter(h -> {
//                    Calendar c = Calendar.getInstance();
//                    c.setTime(h.getCreateTime());
//                    return LocalDate.now().getDayOfYear() == c.get(Calendar.DAY_OF_YEAR);
//                }).count());
//                mapMessage.add(subject.name(), homeworks);
//            }
//            Clazz clazz = clazzLoaderClient.loadStudentClazz(studentId);
//            mapMessage.put("isGraduate", clazz != null && clazz.isTerminalClazz());
//            mapMessage.setSuccess(true);
//            return mapMessage;
//        } catch (Exception ex) {
//            logger.error("load student's homeworks failed. studentId:{}", studentId, ex);
//            return MapMessage.errorMessage("查询学生作业失败");
//        }
        return MapMessage.errorMessage("功能已下线");
    }

    //查询某次作业详情
    @RequestMapping(value = "/detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    @RequestMapping(value = "/common.vpage", method = RequestMethod.GET)
    public String errorList(Model model) {
        Long parentId = getRequestContext().getUserId();
        // 验证是否全部选择了家长角色
        String url = callNameAvailable(parentId);
        if (StringUtils.isNotBlank(url)) {
            return "redirect:" + url;
        }
        String page = getRequestString("page");
        if (StringUtils.isBlank(page)) {
            return "redirect:/parent/homework/index.vpage";
        }

        //获取学生列表
        List<User> students = studentLoaderClient.loadParentStudents(parentId);
        if (students.size() == 0) {
            //跳去绑学生页面
            return "redirect:/parent/ucenter/bindchild.vpage";
        }
        List<Map<String, Object>> stdInfos = mapChildInfos(students);
        model.addAttribute("students", stdInfos);

        //获取孩子id，并返回当前选中学生ID，如果孩子id不在孩子列表中则返回第一个孩子及其位置
        Long studentId = getRequestLong("sid");
        Map<Long, User> studentMap = students.stream().collect(Collectors.toMap(User::getId, Function.identity(), (u, v) -> {
            throw new IllegalStateException("Duplicate key " + u);
        }, LinkedHashMap::new));
        if (0 == studentId || !studentMap.keySet().contains(studentId)) {
            model.addAttribute("currentStd", students.get(0));
            studentId = students.get(0).getId();
        } else {
            model.addAttribute("currentStd", studentMap.get(studentId));
        }

        //学习报告页,如果有报告要显示提示语(拖慢页面产品不在乎)
        if ("reportindex".equals(page)) {
            return "/parent/downloadGuide";
        }

        //课堂表现页,检查有没有班级
        if ("smart".equals(page)) {
            List<ClazzTeacher> teachers = userAggregationLoaderClient.loadStudentTeachers(studentId);
            model.addAttribute("hasTeacher", !CollectionUtils.isEmpty(teachers));
        }

        return "/parent/homework/" + page;
    }

    @RequestMapping(value = "/load_weekly_report.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadWeeklyReport() {

        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "/sharereport.vpage", method = RequestMethod.GET)
    public String share(Model model) throws IOException {
        return "redirect:/parent/homework/index.vpage";
    }

    private void log() {
        String _from = getRequestString("_from");
        if (StringUtils.isNotBlank(_from) && "shareButton".equals(_from)) {
            Map<String, String> log = new HashMap<>();
            log.put("module", "weekly_report");
            log.put("op", "weekly_report_click_share_report");
            log.put("s0", getRequestContext().getAuthenticatedOpenId());
            super.log(log);
        }
    }

    @RequestMapping(value = "/errordetail.vpage")
    public String errorDetail(Model model) {
        String index = getRequestString("index");
        String wrongList = getRequestString("wrongList");
        String homeworkId = getRequestString("hid");
        String homeworkType = getRequestString("ht");
        Long studentId = getRequestLong("sid");

        if (StringUtils.isBlank(wrongList) || StringUtils.isBlank(homeworkId) || StringUtils.isBlank(homeworkType) || 0 == studentId) {
            return redirectWithMsg("参数错误", model);
        }

        model.addAttribute("completeUrl", "parent/homework/answers/" + studentId + "/" + homeworkType + "/" + homeworkId + ".vpage");

        model.addAttribute("index", index);
        model.addAttribute("wrongList", wrongList);
        return "/parent/homework/errordetail";
    }

    //通过试题ID列表查询试题列表
    @RequestMapping(value = "/loadquestion.vpage")
    @ResponseBody
    public MapMessage loadQuestion() {
        String data = getRequestString("data");
        try {
            Map<String, Object> dataMap = JsonUtils.fromJson(data);
            Object obj = dataMap.get("ids");
            if (obj instanceof List) {
                List<String> ids = (List<String>) obj;
                // 这里是布置出去后，然后再load所有最好不进行过滤
                Map<String, QuestionMapper> result = questionLoaderClient.loadQuestionMapperByQids(ids, false, false, true);
                if (!MapUtils.isEmpty(result)) {
                    MapMessage message = MapMessage.successMessage();
                    message.put("result", result);
                    return message;
                }
            }
        } catch (Exception ex) {
            logger.error("load question failed, data:{}", data);
        }
        return MapMessage.errorMessage("查询失败");
    }

    @RequestMapping(value = "/answers/{studentId}/{homeworkType}/{homeworkId}.vpage")
    @ResponseBody
    public Map<String, Object> loadAnswers(@PathVariable Long studentId, @PathVariable String homeworkType, @PathVariable String homeworkId) {
        HomeworkType ht = HomeworkType.of(homeworkType);

        if (ht == UNKNOWN) {
            return MapMessage.errorMessage("未知作业类型");
        }

        try {
            Map<String, Object> paperExamMap = new HashMap<>();
            if (StringUtils.isNotBlank(homeworkId) && studentId != null) {

                NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
                if(newHomework == null) return MapMessage.errorMessage("作业不存在");
                Set<String> processIds = new HashSet<>();
                NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
                if (newHomeworkResult != null) {

                    LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();
                    if (MapUtils.isNotEmpty(practices)) {
                        practices.values().stream().filter(p -> p.getAnswers() != null).forEach(p -> processIds.addAll(p.getAnswers().values()));
                    }
                }
                Map<String, NewHomeworkProcessResult> processId2ProcessMap = newHomeworkProcessResultLoaderClient.loads(homeworkId, processIds);

                if (MapUtils.isEmpty(processId2ProcessMap)) {
                    return Collections.emptyMap();
                }

                Map<String, NewHomeworkProcessResult> wrongQuestionId2ProcessMap = processId2ProcessMap.values().stream()
                        .filter(p -> Boolean.FALSE.equals(p.getGrasp()))
                        .filter(p -> p.getScore() != null)
                        .collect(Collectors.toMap(NewHomeworkProcessResult::getQuestionId, Function.identity(), (a, b) -> a));

                Map<String, NewQuestion> wrongQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(wrongQuestionId2ProcessMap.keySet());

                for (Map.Entry<String, NewHomeworkProcessResult> entry : wrongQuestionId2ProcessMap.entrySet()) {
                    List<List<String>> standerAnswer = wrongQuestionMap.get(entry.getKey()).getContent().getSubContents()
                            .stream()
                            .map(o -> o.getAnswerList(newHomework.getSubject()))
                            .collect(Collectors.toList());

                    NewHomeworkProcessResult processResult = entry.getValue();
                    if (processResult != null) {
                        Map<String, Object> mapExam = new HashMap<>();
                        mapExam.put("subMaster", processResult.getSubGrasp());
                        mapExam.put("master", processResult.getGrasp());
                        mapExam.put("userAnswers", processResult.getUserAnswers());
                        mapExam.put("answers", standerAnswer);
                        mapExam.put("fullScore", processResult.getStandardScore());
                        mapExam.put("score", processResult.getScore());
                        paperExamMap.put(entry.getKey(), mapExam);
                    }
                }

            }
            return paperExamMap;
        } catch (Exception ex) {
            logger.error("Load answers failed,hid:{},ht:{}", homeworkId, homeworkType, ex);
            return Collections.emptyMap();
        }
    }

    @RequestMapping(value = "/loadsmart.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadSmart() {
        Long studentId = getRequestLong("studentId");
        if (0 == studentId) {
            return MapMessage.errorMessage("无效的参数");
        }

        try {
            List<SmartClazzIntegralHistory> all = smartClazzServiceClient.getSmartClazzService()
                    .findSmartClazzIntegralHistoryListByUserId(studentId)
                    .getUninterruptibly()
                    .stream()
                    .filter(SmartClazzIntegralHistory::isDisplayTrue)
                    .collect(Collectors.toList());
            List<SmartClazzIntegralHistory> histories = all.stream()
                    .filter(e -> e.getSubject() == Subject.ENGLISH)
                    .collect(Collectors.toList());
            List<SmartClazzIntegralHistory> mathHistories = all.stream()
                    .filter(e -> e.getSubject() == Subject.MATH)
                    .collect(Collectors.toList());
            histories.addAll(mathHistories);

            histories = histories.stream().sorted((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())).collect(Collectors.toList());

            List<Map<String, Object>> lstHistory = new ArrayList<>();
            histories.stream().forEach(h -> {
                LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(h.getCreateDatetime().getTime() / 1000), ZoneId.systemDefault());
                String date = dt.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String time = dt.format(DateTimeFormatter.ISO_LOCAL_TIME);
                Map<String, Object> map = new HashMap<>();
                map.put("date", date);
                map.put("time", time);
                map.put("history", h);
                lstHistory.add(map);
            });

            MapMessage mapMessage = new MapMessage();
            mapMessage.add("smart", lstHistory);

            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            mapMessage.add("isGraduate", clazz != null && clazz.isTerminalClazz());
            mapMessage.setSuccess(true);
            return mapMessage;
        } catch (Exception ex) {
            logger.error("load smart failed, studentId:{]", studentId, ex);
            return MapMessage.errorMessage("查询失败");
        }
    }

    //送花后老师表示感谢，家长收到感谢消息后查看
    @RequestMapping(value = "checkflowerthanks.vpage", method = RequestMethod.GET)
    public String checkFlowerThanks(Model model) {
        Long teacherId = getRequestLong("teacherId");
        String activityDate = getRequestString("activityDate");
        // 查询老师感谢的一句话
        if (StringUtils.isBlank(activityDate) || teacherId == 0) {
            redirectWithMsg("参数错误", model);
        }
        FlowerGratitude flowerGratitude = flowerServiceClient.getFlowerService()
                .loadSenderFlowerGratitudes(teacherId)
                .getUninterruptibly()
                .stream()
                .filter(t -> StringUtils.equals(activityDate, t.getActivityDate()))
                .sorted((o1, o2) -> Long.compare(o2.fetchCreateTimestamp(), o1.fetchCreateTimestamp()))
                .findFirst()
                .orElse(null);
        model.addAttribute("gratitude", flowerGratitude == null ? "" : flowerGratitude.getContent());
        return "/parent/homework/checkflowerthanks";
    }

    //家长给老师送花
    @RequestMapping(value = "/sendflower.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendFlower() {
        Long studentId = getRequestLong("studentId");
        String homeworkType = getRequestString("homeworkType");
        String homeworkId = getRequestString("homeworkId");
        Long teacherId = getRequestLong("teacherId");

        if (0 == studentId || StringUtils.isBlank(homeworkType) || StringUtils.isBlank(homeworkId) || 0 == teacherId) {
            return MapMessage.errorMessage("无效参数");
        }

        Long parentId = getRequestContext().getUserId();
        try {
            NewHomework newHomework = newHomeworkLoaderClient.loadNewHomework(homeworkId);
            if (newHomework == null) {
                return MapMessage.errorMessage("要送花的作业不存在");
            }
            if (newHomework.isHomeworkChecked()) {
                return MapMessage.errorMessage("已检查的作业不能送花");
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            FlowerSourceType type = FlowerSourceType.of(homeworkType);
            String flowerKey = homeworkType + "-" + homeworkId;

            //之前作业送花的限制。现在从底层移到业务层了。
            long count = flowerServiceClient.getFlowerService().loadHomeworkFlowers(flowerKey)
                    .getUninterruptibly()
                    .stream()
                    .filter(t -> Objects.equals(studentId, t.getSenderId()))
                    .count();
            if (count > 0) {
                return MapMessage.errorMessage("不能重复送花！");
            }

            return flowerServiceClient.getFlowerService()
                    .sendFlower(studentId, parentId, teacherId, studentDetail.getClazzId(), type, flowerKey)
                    .getUninterruptibly();
        } catch (Exception ex) {
            logger.error("student {} send flower to teacher {} for homework {} failed.", studentId, teacherId, homeworkId);
            return MapMessage.errorMessage("送花失败");
        }
    }

    @RequestMapping(value = "/wrongquestionlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage wrongQuestionList() {
        Long studentId = getRequestLong("sid");

        if (0 == studentId) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            //取老师
            MapMessage message = MapMessage.successMessage();
            List<Teacher> teachers = userAggregationLoaderClient.loadStudentTeachers(studentId).stream().map(ClazzTeacher::getTeacher).collect(Collectors.toList());
            Set<String> subjects = new HashSet<>();
            teachers.stream().forEach(t -> subjects.add(t.getSubject().name()));
            for (String subject : subjects) {
                List<Map<String, Object>> wql = getWrongQuestionListForSubject(subject, studentId);

                message.add(subject, wql);
            }

            //判断孩子是否可以购买afenti
            message.add("available", orderService.canBuyAfentiExam(studentId));

            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            message.add("isGraduate", clazz != null && clazz.isTerminalClazz());
            return message;
        } catch (Exception ex) {
            logger.error("Load wrong question list failed.");
        }
        return MapMessage.errorMessage("查询失败");
    }

    private void buildMoreInfo(List<WechatHomeworkMapper> mappers, Long studentId) {
        if (CollectionUtils.isEmpty(mappers)) return;

        Set<Long> teacherIds = mappers.stream().map(WechatHomeworkMapper::getTeacherId).collect(Collectors.toSet());
        Set<String> homeworkKeyOfFlower = mappers.stream().map(WechatHomeworkMapper::getKeyOfFlower).collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(teacherIds)) {
            Map<Long, User> userMap = userLoaderClient.loadUsers(teacherIds);
            mappers.stream().forEach(m -> {
                User user = userMap.get(m.getTeacherId());
                m.setCertificated(user != null && AuthenticationState.SUCCESS == user.fetchCertificationState());
            });
        }

        if (CollectionUtils.isNotEmpty(homeworkKeyOfFlower)) {
            Map<String, List<Flower>> flowerMap = flowerServiceClient.loadHomeworkFlowers(homeworkKeyOfFlower);
            mappers.stream().forEach(m -> {
                List<Flower> flowers = flowerMap.get(m.getKeyOfFlower());
                if (CollectionUtils.isNotEmpty(flowers)) {
                    m.setFlowerCount(flowers.size());
                    m.setSentFlag(flowers.stream().anyMatch(flower -> Objects.equals(studentId, flower.getSenderId())));
                }
            });
        }
    }



    //查询某一学科错题本
    private List<Map<String, Object>> getWrongQuestionListForSubject(String subject, Long studentId) {
        Map<String, List<Map<String, Object>>> homeworkWrongListMap =  newHomeworkLoaderClient.getStudentWrongQuestionIds(studentId, Subject.ofWithUnknown(subject), null);
        //排序日期
        List<String> dateList = new ArrayList<>(homeworkWrongListMap.keySet());
        Collections.sort(dateList, (o1, o2) -> {
            long t1 = com.voxlearning.alps.calendar.DateUtils.stringToDate(o1, "yyyy.MM.dd").getTime();
            long t2 = com.voxlearning.alps.calendar.DateUtils.stringToDate(o2, "yyyy.MM.dd").getTime();
            return Long.compare(t2, t1);
        });

        List<Map<String, Object>> returnMapList = new ArrayList<>();
        for (String date : dateList) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", date);
            map.put("timestamp", DateUtils.stringToDate(date, "yyyy.MM.dd").getTime());
            List<Map<String, Object>> homeworkMapList = new ArrayList<>();
            List<Map<String, Object>> quizMapList = new ArrayList<>();
            List<Map<String, Object>> homeworkWrongList = homeworkWrongListMap.get(date);
            //每天的作业错题列表
            if (CollectionUtils.isNotEmpty(homeworkWrongList)) {
                homeworkWrongList.stream().forEach(p -> {
                    Map<String, Object> wrongCountMap = new HashMap<>();
                    wrongCountMap.put("ids", p.get("qid"));
                    wrongCountMap.put("homeworkId", p.get("homeworkId"));
                    wrongCountMap.put("homeworkType", p.get("homeworkType"));
                    homeworkMapList.add(wrongCountMap);
                });
            }
            map.put("homeworkMapList", homeworkMapList);
            map.put("quizMapList", quizMapList);
            returnMapList.add(map);
        }
        return returnMapList;
    }
}
