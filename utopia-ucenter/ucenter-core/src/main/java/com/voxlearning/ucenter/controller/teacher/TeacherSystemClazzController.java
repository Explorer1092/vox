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

package com.voxlearning.ucenter.controller.teacher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.api.constant.*;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.ugc.client.TeacherUgcServiceClient;
import com.voxlearning.utopia.service.user.api.TeacherAlterationService;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService.SystemClazzInfo;
import com.voxlearning.utopia.service.user.api.constants.ClazzCreateSourceType;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ClazzIntegralHistoryPagination;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.entities.log.UserOperatorType;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeProcessorType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用于老师管理系统自建班级
 *
 * @author changyuan.liu
 * @since 2015/6/15
 */
@Slf4j
@Controller
@RequestMapping("teacher/systemclazz")
public class TeacherSystemClazzController extends AbstractWebController {

    @Inject private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private SpecialTeacherServiceClient specialTeacherServiceClient;
    @Inject private SpecialTeacherLoaderClient specialTeacherLoaderClient;
    @Inject private TeacherSystemClazzServiceClient teacherSystemClazzServiceClient;
    @Inject private TeacherSystemClazzInfoServiceClient teacherSystemClazzInfoServiceClient;
    @Inject private TeacherUgcServiceClient teacherUgcServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private WechatLoaderClient wechatLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private ClazzInfoLoaderClient clazzInfoLoaderClient;
    @Inject private StudentInfoLoaderClient studentInfoLoaderClient;
    @Inject private RaikouSDK raikouSDK;

    //TODO 改成以关联关系(group)为主导
    //TODO 不过目前application都是以clazzId纪录，改成关系主导需要纪录groupId

    /**
     * 老师直接加入班级
     * 1. 建立老师与班级关系
     * 2. 建立老师默认关联关系
     * 3. 指定班级教材
     */
    @RequestMapping(value = "joinclazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage joinClazz() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("No teacher info found.");
        }

        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("No clazzId specified.");
        }

        int clazzLevel = getRequestInt("clazzLevel");
        if (clazzLevel == 0) {
            return MapMessage.errorMessage("No clazz level specified.");
        }

        // 老师与班级建立关系
        // 建立默认关联关系(分组)
        return clazzServiceClient.teacherJoinSystemClazz(teacher.getId(), clazzId, OperationSourceType.pc);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "findclazzinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findClazzInfo(@RequestBody Map<String, Object> clazzMap) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("No teacher info found.");
        }

        Set<Long> clazzIdSet = new LinkedHashSet<>();
        clazzMap.forEach((k, v) -> {
            List<Object> levelClazzs = (List<Object>) v;
            levelClazzs.forEach(lc -> {
                Map<String, Object> m = (Map<String, Object>) lc;
                List<SystemClazzInfo> clazzs = JsonUtils.fromJsonToList(JsonUtils.toJson(m.get("clazzs")), SystemClazzInfo.class);
                clazzs.forEach(c -> clazzIdSet.add(c.getId()));
            });
        });

        //TODO 封装返回
        MapMessage message = teacherSystemClazzInfoServiceClient.getNewAddAndAdjustClazzs(teacher.getId(), clazzIdSet, ClazzCreateSourceType.pc, false);
        MapMessage mapMessage = MapMessage.successMessage();

        mapMessage.add("groupStuInfo", message.get("groupStuInfo"));
        mapMessage.add("newClazzs", message.get("newClazzs"));
        mapMessage.add("adjustClazzs", message.get("adjustClazzs"));
        return mapMessage;
    }

    @RequestMapping(value = "findclazzinfobygrade.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findClazzInfoByLevel() {
        int clazzLevel = getRequestInt("clazzLevel");
        if (clazzLevel == 0) {
            return MapMessage.errorMessage("年级参数错误");
        }

        ClazzLevel cl = ClazzLevel.parse(clazzLevel);
        if (cl == null) {
            return MapMessage.errorMessage("年级参数错误");
        }

        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("老师信息不存在");
        }

        List<Map<String, Object>> clazzInfo = teacherSystemClazzServiceClient
                .findSystemClazzInfoByClazzLevel(teacher.getId(), cl);
        MapMessage mapMessage = MapMessage.successMessage();
        clazzInfo = clazzInfo.stream().filter(stringObjectMap -> stringObjectMap.containsKey("teachers") && CollectionUtils.isNotEmpty((List) stringObjectMap.get("teachers"))).collect(Collectors.toList());
        mapMessage.add("clazzs", clazzInfo);
        if (teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
            //获取本学校本年级中的班级信息,条件是班级必须有没有老师的group,并且group下有学生
            TeacherDetail teacherDetail = currentTeacherDetail();
            MapMessage tempMapMessage = clazzInfoLoaderClient.getClazzInfoLoader().loadClazzInfoIfGroupNoTeacherHasStudent(teacherDetail.getTeacherSchoolId(), cl, teacher.getSubject(), teacherDetail.getId());
            mapMessage.add("clazzInfoList", tempMapMessage.get("clazzInfoList"));
            mapMessage.add("groupInfosList", tempMapMessage.get("groupInfosList"));
        }

        return mapMessage;
    }

    @RequestMapping(value = "findGroupInfoNoTeacherByClazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findGroupInfoNoTeacherByClazz() {
        long clazzId = getRequestLong("clazzId");
        Teacher teacher = getSubjectSpecifiedTeacher();
        List<Map<String, Object>> groupinfoList = studentInfoLoaderClient.getStudentInfoLoader().loadStudentsInfoNothasTeacher(Arrays.asList(clazzId), Arrays.asList(teacher.getSubject()), teacher.getId());
        return MapMessage.successMessage().add("groupinfoList", groupinfoList);
    }

    @RequestMapping(value = "takeoverclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage takeOverClazz() {
        Long clazzId = getRequestLong("clazzId");
        Long groupId = getRequestLong("groupId");
        Long teacherId = getSubjectSpecifiedTeacherId();

        if (clazzId == 0L || groupId == 0L || teacherId == null) {
            return MapMessage.errorMessage().setInfo("参数错误");
        }
        if (raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(clazzId) == null) {
            return MapMessage.errorMessage().setInfo("班级不存在");
        }

        return groupServiceClient.joinTeacherIntoGroups(teacherId, Arrays.asList(groupId));
    }

    @RequestMapping(value = "sendTCACode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendTeacherClazzApplicationCode() {
        String mobile = getRequestString("mobile");
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("错误的手机号");
        }
        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.TEACHER_CLAZZ_APPLICATION.name());
    }

    @RequestMapping(value = "verifyTCACode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyTeacherClazzApplicationCode() {
        String code = getRequestString("code");
        if (code == null) {
            return MapMessage.errorMessage("验证码错误");
        }

        return smsServiceClient.getSmsService().verifyValidateCode(currentUserId(), code, SmsType.TEACHER_CLAZZ_APPLICATION.name());
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "adjustclazzs.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adjustClazzs2(@RequestBody String json) throws IOException {
        Teacher curTeacher = getSubjectSpecifiedTeacher();
        if (curTeacher == null) {
            return MapMessage.errorMessage("账号错误，请重新登录");
        }

        ObjectMapper mapper = JsonObjectMapper.OBJECT_MAPPER;
        JsonNode rootNode = mapper.readTree(json);
        CollectionType LongListType = mapper.getTypeFactory().constructCollectionType(List.class, Long.class);

        List<Long> adjustClazzIds = new LinkedList<>();

        // 检查教务老师
//        MapMessage checkMsg = specialTeacherServiceClient.checkExistAffairTeacher(curTeacher, "学校已有教务老师，请联系教务{}老师进行班级调整");
//        if (!checkMsg.isSuccess()) {
//            return checkMsg;
//        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(currentTeacherDetail().getTeacherSchoolId()).getUninterruptibly();
        if (schoolExtInfo != null && schoolExtInfo.fetchAdjustClazz()) {
            return MapMessage.errorMessage("您不可自主调整班级或学生，如有疑问请联系一起客服");
        }

        //处理加入无老师的group
        JsonNode groupClazzInfo = rootNode.get("groupClazzInfo");
        if (groupClazzInfo != null && groupClazzInfo.size() > 0) {
            Set<Long> groupIdSet = new HashSet<>();
            for (JsonNode childNode : groupClazzInfo) {
                Long clazzId = childNode.get("clazzId").asLong();
                Long groupId = childNode.get("groupId").asLong();
                groupIdSet.add(groupId);
                adjustClazzIds.add(clazzId); //避免加入后又在后面的逻辑中被删了
            }
            MapMessage mapMessage = groupServiceClient.joinTeacherIntoGroups(curTeacher.getId(), groupIdSet);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }
        }

        // 处理加入班级已有资源请求
        JsonNode newClazzs = rootNode.get("newClazzs");
        if (newClazzs != null && newClazzs.size() > 0) {
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(curTeacher.getId());

            for (JsonNode childNode : newClazzs) {
                Long clazzId = childNode.get("clazzId").asLong();
                adjustClazzIds.add(clazzId);// 对于发送请求的加入班级，老师先进班，请求是另外一回事
                List<Long> teacherIds = new LinkedList<>();
                for (JsonNode groupNode : childNode.get("groups")) {
                    if (groupNode.get("teachers") != null) {
                        for (JsonNode teacherNode : groupNode.get("teachers")) {
                            teacherIds.add(teacherNode.get("id").asLong());
                        }
                    }
                }

                // fix redmine 29376
                // 对于包班制老师，当进班的时候是直接替换原有同学科老师，不需要处理请求了
                if (CollectionUtils.isNotEmpty(teacherIds) && !CollectionUtils.containsAny(relTeacherIds, teacherIds)) {
                    MapMessage msg = handleTeacherRequestStudentResource(curTeacher, clazzId, teacherIds);
                    if (!msg.isSuccess()) {
                        return msg;
                    }
                }
            }
        }

        JsonNode adjustClazzs = rootNode.get("adjustClazzs");
        adjustClazzIds.addAll(mapper.readValue(adjustClazzs.traverse(), LongListType));

        JsonNode adjustWalkingClazzs = rootNode.get("adjustWalkingClazzs");
        Map<String, List<Map<String, Object>>> walkingClazzMap = mapper.convertValue(adjustWalkingClazzs, Map.class);

        // 老师UGC信息 - 执教班级数
//        JsonNode actualTeachClazzCountNode = rootNode.get("actualTeachClazzCount");
//        if (actualTeachClazzCountNode != null) {
//            int actualTeachClazzCount = actualTeachClazzCountNode.asInt();
//            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(curTeacher.getId());// 执教班级数仅对应主账号
//            if (mainTeacherId == null) {
//                mainTeacherId = curTeacher.getId();
//            }
//            teacherServiceClient.setTeacherUGCTeachClazzCount(mainTeacherId, actualTeachClazzCount);
//        }

        // 处理直接加入班级请求
        MapMessage message = MapMessage.successMessage();
        // 当教学班有班级时，允许退出所有行政班
        if (CollectionUtils.isNotEmpty(adjustClazzIds) || MapUtils.isNotEmpty(walkingClazzMap)) {
            message = clazzServiceClient.teacherAdjustSystemClazzs(curTeacher.getId(), adjustClazzIds, OperationSourceType.pc);
        }

        // 处理直接加入教学班级
        if (curTeacher.isJuniorTeacher() || curTeacher.isSeniorTeacher()) {
            // 当行政班不为空时，允许退出所有教学班级
            if (message.isSuccess() && (MapUtils.isNotEmpty(walkingClazzMap) || CollectionUtils.isNotEmpty(adjustClazzIds))) {
                message = clazzServiceClient.teacherAdjustWalkingCLazz(curTeacher.getId(), walkingClazzMap);
            }
        }

        return message;
    }

    /**
     * 老师再加回到已退出班级
     */
    @RequestMapping(value = "joinbackclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage joinBackClazz() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("找不到老师信息");
        }

        long clazzId = getRequestLong("clazzId");
        return clazzServiceClient.teacherJoinSystemClazz(teacher.getId(), clazzId, OperationSourceType.pc);
    }

    /**
     * 加入班级->申请加入
     *
     * @param json 输入的json参数：
     *             {
     *             "teachers": [
     *             {
     *             "subject": "英语",
     *             "name": "英语空",
     *             "id": 127298
     *             }
     *             ],
     *             "clazzId": 38247,
     *             "hasRequest": false,
     *             "name": "一年级2班",
     *             "teacherName": "英淘淘",
     *             "teacherSubject": "英语"
     *             }
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "requestjoinclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage requestJoinClazz(@RequestBody String json) throws IOException {
        Teacher curTeacher = getSubjectSpecifiedTeacher();
        if (curTeacher == null) {
            return MapMessage.errorMessage("找不到老师信息");
        }

        ObjectMapper mapper = JsonObjectMapper.OBJECT_MAPPER;
        JsonNode rootNode = mapper.readTree(json);
        // 处理加入班级已有资源请求
        Long clazzId = rootNode.get("clazzId").asLong();

        JsonNode teacherNameNode = rootNode.get("teacherName");
        JsonNode teacherSubjectNode = rootNode.get("teacherSubject");
        String teacherName = null;
        String teacherSubject = null;
        if (teacherNameNode != null) {
            teacherName = teacherNameNode.asText();
            teacherSubject = teacherSubjectNode.asText();
        }

        List<Long> teacherIds = new LinkedList<>();
        for (JsonNode groupNode : rootNode.get("teachers")) {
            long id = groupNode.get("id").asLong();
            teacherIds.add(id);
            if (teacherName != null && teacherSubject != null) {
                // 校验老师输入姓名
                // 当此活跃group有与发出者相同科目的老师时，校验填写的名字是否与此活跃group的同科老师姓名一样
                // 当此活跃group没有与发出者相同科目的老师时，校验填写的名字是否与此活跃group的异科老师姓名一样
                String subject = groupNode.get("subject").asText();
                if (StringUtils.equals(subject, teacherSubject)) {
                    Teacher teacher = teacherLoaderClient.loadTeacher(id);
                    if (!StringUtils.equals(teacherName, teacher.fetchRealname())) {
                        return MapMessage.errorMessage("请填写正确的老师姓名");
                    }
                }
            }
        }

        // 判断老师班级是否已达上限
        long clazzCount = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(curTeacher.getId()).stream()
                .filter(c -> !c.isDisabledTrue())
                .filter(c -> !c.isTerminalClazz())
                .count();
        if (curTeacher.getSubject() != null && clazzCount >= ClazzConstants.MAX_CLAZZ_COUNT.get(curTeacher.getSubject())) {
            return MapMessage.errorMessage("班级已达上限，不能再加入新的班级。如需帮助请咨询客服。");
        }

        MapMessage msg = handleTeacherRequestStudentResource(curTeacher, clazzId, teacherIds);
        if (!msg.isSuccess()) {
            return msg;
        }

        if (!SafeConverter.toBoolean(msg.get("join"))) {// 认证老师已经成功共享学生资源
            if (!teacherLoaderClient.isTeachingClazz(curTeacher.getId(), clazzId)) {
                // 需要发送请求情况，老师不在班级中，自动进班
                msg = clazzServiceClient.teacherJoinSystemClazz(curTeacher.getId(), clazzId, OperationSourceType.pc);
            }
            msg.put("join", false);
        }

        return msg;
    }

    /**
     * 设置是否允许学生加入
     */
    @RequestMapping(value = "setfreejoin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setClazzFreeJoin() {
        Long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("clazzId cannot be null.");
        }

        boolean freeJoin = getRequestBool("freeJoin");

        return clazzServiceClient.updateSystemClazzFreeJoin(currentUserId(), clazzId, freeJoin);
    }

    /**
     * 老师退出该班级
     */
    @RequestMapping(value = "exitclazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage exitClazz() {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("No teacher info found.");
        }

        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("No clazzId specified.");
        }

        return clazzServiceClient.teacherExitSystemClazz(teacher.getId(), clazzId, Boolean.FALSE, OperationSourceType.pc);
    }

    /**
     * 班级管理主页
     */
    @RequestMapping(value = "clazzindex.vpage", method = RequestMethod.GET)
    public String listManagedClazzs(Model model) {
        TeacherDetail teacher = currentTeacherDetail();

        if (!teacher.hasValidSubject() || StringUtils.isBlank(teacher.getTeacherSchoolName())) {
            return "redirect:" + ProductConfig.getMainSiteBaseUrl() + "/teacher/index.vpage";
        }
        model.addAttribute("isShensz", teacher.isShensz());

        School school = schoolLoaderClient.getSchoolLoader().loadSchool(teacher.getTeacherSchoolId()).getUninterruptibly();
        String schoolEduSystem = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
        model.addAttribute("eduSystem", schoolEduSystem);
        EduSystemType eduSystem = EduSystemType.of(schoolEduSystem);
        List<Long> teacherIds = new ArrayList<>();
        teacherIds.add(teacher.getId());
        teacherIds.addAll(teacherLoaderClient.loadSubTeacherIds(teacher.getId()));

        // 读取分组所对应clazz信息
        List<Map<String, Object>> teacherClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherIds)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(c -> !c.isTerminalClazz())
                .filter(c -> !c.isDisabledTrue())
                .filter(c -> c.matchEduSystem(eduSystem))
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .distinct()
                .map(c -> MapUtils.m("clazzId", c.getId(), "clazzName", c.formalizeClazzName()))
                .collect(Collectors.toList());

        // Enhancement #64429 老师注册至有教务学校的学校后,除英语学科老师外,允许老师不带班
        boolean ignored = CollectionUtils.isNotEmpty(specialTeacherLoaderClient.findSchoolAffairTeachers(teacher.getTeacherSchoolId()))
                && Subject.ENGLISH != teacher.getSubject();
        boolean showTip = StringUtils.equals(getRequestString("step"), "showtip");
        // 取消所谓的showtip标签
        if (ignored && showTip) {
            return "redirect:/teacher/systemclazz/clazzindex.vpage";
        }
        if (!ignored && CollectionUtils.isEmpty(teacherClazzs) && !showTip) {
            return "redirect:/teacher/systemclazz/clazzindex.vpage?step=showtip";
        }
        model.addAttribute("isCjlSchool", isCJLSchool(teacher.getTeacherSchoolId()));
        model.addAttribute("isSeiueSchool", isSeiueSchool(teacher.getTeacherSchoolId()));
        model.addAttribute("teachClazzs", teacherClazzs);

        // 假老师标记
        model.addAttribute("fakeTeacher", teacherLoaderClient.isFakeTeacher(teacher.getId()));

        if (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()) {
            return "teacherv3/systemclazz/kuailexue/clazzindex";
        } else {
            return "teacherv3/systemclazz/clazzindex";
        }
    }

    /**
     * 班级学豆页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/integral/clazzintegral.vpage", method = RequestMethod.GET)
    public String clazzIntegral(Model model) {
        Teacher teacher = getSubjectSpecifiedTeacher();
        if (teacher == null) {
            return "redirect:/teacher/index.vpage";
        }
        Long teacherId = teacher.getId();

        // 没有班级的校验
        List<GroupTeacherMapper> groups = groupLoaderClient.loadTeacherGroups(teacherId, false);
        if (CollectionUtils.isEmpty(groups)) {
            return "redirect:/teacher/index.vpage";
        }
        Set<Long> clazzIds = groups.stream().map(GroupMapper::getClazzId).collect(Collectors.toSet());
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupMapper group : groups) {
            Clazz clazz = clazzs.get(group.getClazzId());
            if (clazz == null || clazz.isTerminalClazz()) continue;
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", group.getId());
            map.put("clazzId", group.getClazzId());
            map.put("clazzName", clazz.formalizeClazzName());
            result.add(map);
        }
        model.addAttribute("clazzs", result);
        //多学科支持
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());
        return "teacherv3/systemclazz/integral/list";
    }

    // 获取组的学豆历史数据
    @RequestMapping(value = "/integral/clazzintegralchip.vpage", method = RequestMethod.GET)
    public String loadGroupIntegralHistory(Model model) {
        Long clazzId = getRequestLong("clazzId");
        Long groupId = getRequestLong("groupId");
        int pageNumber = getRequestInt("pageNumber", 1);
        boolean ge0 = getRequestBool("ge0", true);
        // 获取前三个月的历史数据
        ClazzIntegralHistoryPagination pagination = clazzIntegralServiceClient.getClazzIntegralService()
                .loadClazzIntegralHistories(groupId, 3, pageNumber - 1, 5, ge0)
                .getUninterruptibly();
        model.addAttribute("pagination", pagination);
        model.addAttribute("integral", pagination.getTotalIntegral());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("clazzId", clazzId);
        model.addAttribute("ge0", ge0);

        //多学科支持
        Teacher teacher = getSubjectSpecifiedTeacher();
        model.addAttribute("curSubject", teacher.getSubject());
        model.addAttribute("curSubjectText", teacher.getSubject().getValue());
        return "teacherv3/systemclazz/integral/clazzintegralchip";
    }

    // 调整班级页面的班级信息获取标志值
    private static final int CHOOSE_CLAZZ_LEVEL_CLAZZ_FLAG = 0b0000_0000_0000_0001;     // 指定年级下的班级信息flag
    private static final int CHOOSE_CLAZZ_TEACHER_CLAZZ_FLAG = 0b0000_0000_0000_0010;   // 老师关联班级信息flag
    private static final int CHOOSE_CLAZZ_NORMAL_CLAZZ_FLAG = 0b0000_0000_1000_0000;    // 返回行政班信息标识
    private static final int CHOOSE_CLAZZ_WALKING_CLAZZ_FLAG = 0b0000_0000_0100_0000;   // 返回教学班信息标识

    /**
     * 班级管理 -> 调整班级
     */
    @RequestMapping(value = "chooseclazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage chooseClazz() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();

        // 需要的信息类型
        // 用二进制标志位表示
        // 00000001 - 年级班级信息 -- 001
        // 00000010 - 老师班级信息 -- 010
        // 00000011 - 年级班级信息以及老师班级信息
        // 10000000 - 行政班信息
        // 01000000 - 教学班信息
        // 默认为0
        int infoType = getRequestInt("infoType");
        if (infoType == 0) {
            return MapMessage.errorMessage("infoType is wrong.");
        }

        // 学校
        Long schoolId = teacher.getTeacherSchoolId();
        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("No school for this teacher found.");
        }
        EduSystemType eduSystem = EduSystemType.of(
                schoolExtServiceClient.getSchoolExtService()
                        .getSchoolEduSystem(school)
                        .getUninterruptibly()
        );

        List<Clazz> teachClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacher.getId());

        // 过滤掉毕业班级
        teachClazzs = teachClazzs.stream()
                .filter(e -> !e.isTerminalClazz())
                .filter(c -> c.matchEduSystem(eduSystem))
                .collect(Collectors.toList());

        MapMessage message = MapMessage.successMessage();
        if ((infoType & CHOOSE_CLAZZ_LEVEL_CLAZZ_FLAG) != 0) {
            // 年级
            int clazzLevel = getRequestInt("clazzLevel");
            if (clazzLevel == 0) {
                return MapMessage.errorMessage("clazzLevel cannot be null.");
            }
            ClazzLevel level = ClazzLevel.parse(clazzLevel);
            if ((infoType & CHOOSE_CLAZZ_NORMAL_CLAZZ_FLAG) != 0) {// 行政班
                int jie = ClassJieHelper.fromClazzLevel(level);
                List<SystemClazzInfo> clazzList;
                Set<Long> teachClazzIds = teachClazzs.stream().map(Clazz::getId).collect(Collectors.toSet());
//                if (!teacher.isKLXTeacher()) {
//                    List<Clazz> clazzs = teacherSystemClazzServiceClient.loadSystemClazzsInfoByTeacherIdAndSchoolId(teacher.getId(), schoolId, jie, jie);
//                    clazzList = new LinkedList<>();
//                    for (Clazz clazz : clazzs) {
//                        SystemClazzInfo systemClazzInfo = new SystemClazzInfo(clazz.getId(), clazz.getClassName());
//                        systemClazzInfo.setChecked(teachClazzIds.contains(clazz.getId()));
//                        clazzList.add(systemClazzInfo);
//                    }
//                } else {
                clazzList = teacherSystemClazzServiceClient.loadSystemClazzsInfoForCreatingClazz(teacher.getId(), schoolId, jie, jie);
                clazzList.forEach(c -> c.setChecked(teachClazzIds.contains(c.getId())));
//                }

                message.add("clazzs", clazzList);
            }
            if ((infoType & CHOOSE_CLAZZ_WALKING_CLAZZ_FLAG) != 0) {// 教学班
                List<SystemClazzInfo> clazzList = (List<SystemClazzInfo>) message.get("clazzs");
                if (clazzList == null) {
                    clazzList = new ArrayList<>();
                    message.add("clazzs", clazzList);
                }
                clazzList.addAll(teacherSystemClazzServiceClient.loadWalkingClazzsInfoByTeacherIdAndSchoolId(
                        teacher.getId(), teacher.getTeacherSchoolId(), level, teacher.getSubject()));
            }
        }

        if ((infoType & CHOOSE_CLAZZ_TEACHER_CLAZZ_FLAG) != 0) {
            // 返回行政班数据
            if ((infoType & CHOOSE_CLAZZ_NORMAL_CLAZZ_FLAG) != 0) {
                List<Map<String, Object>> teacheClazzList = new LinkedList<>();
                Map<String, List<Clazz>> levelTeachClazzs = teachClazzs.stream()
                        .filter(c -> Objects.equals(c.getClassType(), ClazzType.PUBLIC.getType()))
                        .collect(Collectors.groupingBy(Clazz::getClassLevel));

                levelTeachClazzs.forEach((k, v) -> {
                    List<SystemClazzInfo> list = v.stream()
                            .sorted(new Clazz.ClazzLevelAndNameComparator())
                            .map(c -> new SystemClazzInfo(c.getId(), c.getClassName()))
                            .collect(Collectors.toList());
                    Map<String, Object> levelObj = new HashMap<>();
                    levelObj.put("clazzLevel", k);
                    levelObj.put("clazzs", list);
                    teacheClazzList.add(levelObj);
                });
                message.add("teachClazzs", teacheClazzList);
            }
            // 返回教学班数据
            if ((infoType & CHOOSE_CLAZZ_WALKING_CLAZZ_FLAG) != 0) {
                Map<String, List<SystemClazzInfo>> teacherWalkingClazzList = new LinkedHashMap<>();
                Map<String, List<Clazz>> levelTeachClazzs = teachClazzs.stream()
                        .filter(c -> Objects.equals(c.getClassType(), ClazzType.WALKING.getType()))
                        .collect(Collectors.groupingBy(Clazz::getClassLevel));

                levelTeachClazzs.forEach((k, v) -> {
                    List<SystemClazzInfo> list = v.stream()
                            .sorted(new Clazz.ClazzLevelAndNameComparator())
                            .map(c -> new SystemClazzInfo(c.getId(), c.getClassName()))
                            .collect(Collectors.toList());
                    teacherWalkingClazzList.put(k, list);
                });
                message.add("teachWalkingClazzs", teacherWalkingClazzList);
            }

//            if (teacher.isKLXTeacher()) {
//                // 返回文理科属性
//                GroupTeacherMapper groupTeacherMapper = groupLoaderClient.loadTeacherGroups(teacher.getId(), false).stream().findFirst().orElse(null);
//                if (groupTeacherMapper != null) {
//                    message.add("artScienceType", groupTeacherMapper.getArtScienceType());
//                } else {
//                    message.add("artScienceType", ArtScienceType.UNKNOWN);
//                }
//            }

            // ugc info
            // 执教班级数
//            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());// 执教班级数仅对应主账号
//            if (mainTeacherId == null) {
//                mainTeacherId = teacher.getId();
//            }
//            TeacherUGCInfo teacherUGCInfo = teacherUgcServiceClient.getTeacherUgcService()
//                    .loadTeacherUGCInfo(mainTeacherId)
//                    .getUninterruptibly();
//            int actualTeachClazzCount = teacherUGCInfo == null ? 0 : teacherUGCInfo.getTeachClazzNum();
//            message.add("actualTeachClazzCount", actualTeachClazzCount);
        }

        return message;
    }

    /**
     * 教师转让班级 -- 获取班级列表以及同校同科教师
     */
    @RequestMapping(value = "teacherlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSchoolTeacherList(Model model) {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (isCJLSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入数校平台，暂时无法进行调整。有疑问请联系客服。");
        }
        // 希悦学校也不能操作
        if (isSeiueSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入第三方平台，暂时无法进行调整，若有疑问请联系客服。");
        }
        MapMessage message = new MapMessage();
        try {
            // 班级列表
            // TODO 需要改成能读取已退出班级列表
            List<Map<String, Object>> clazzList = teacherAlterationServiceClient.getClazzListWithState(teacher.getId());
            model.addAttribute("clazzs", JsonUtils.toJson(clazzList));
            // 教师列表
            List<Teacher> list = teacherAlterationServiceClient.getTeacherOfSpecificSubjectInTheSameSchool(teacher.getId(), teacher.getSubject(), true);
            // 过滤假老师
            list = list.stream()
                    .filter(p -> !teacherLoaderClient.isFakeTeacher(p.getId()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> authTeachers = list.stream()
                    .filter(e -> e.fetchCertificationState() == AuthenticationState.SUCCESS)
                    .map(t -> MapUtils.m("id", t.getId(), "name", t.fetchRealname()))
                    .collect(Collectors.toList());
            List<Map<String, Object>> unauthTeachers = list.stream()
                    .filter(e -> e.fetchCertificationState() != AuthenticationState.SUCCESS)
                    .map(t -> MapUtils.m("id", t.getId(), "name", t.fetchRealname()))
                    .collect(Collectors.toList());
            message.add("authTeachers", authTeachers);
            message.add("unauthTeachers", unauthTeachers);
            message.setSuccess(Boolean.TRUE);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return message;
    }

    /**
     * 取消对应班级的相关请求
     *
     * @return
     * @author changyuan.liu
     */
    @RequestMapping(value = "cancelclazzapps.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage cancelClazzApplications(@RequestBody Map<String, Object> map) {
        Long applicantId = getSubjectSpecifiedTeacher().getId();

        try {
            List<Map<String, Object>> teachers = (List<Map<String, Object>>) map.get("teachers");
            for (Map<String, Object> teacherInfo : teachers) {
                Map<String, Object> application = (Map<String, Object>) teacherInfo.get("application");
                if (application != null) {
                    String type = (String) application.get("type");
                    Long recordId = SafeConverter.toLong(application.get("id"));

                    ClazzTeacherAlterationType applicationType;
                    try {
                        applicationType = ClazzTeacherAlterationType.valueOf(type);
                    } catch (Exception e) {
                        return MapMessage.errorMessage("申请类型错误");
                    }

                    MapMessage msg = cancelApplication(applicantId, recordId, applicationType);
                    if (!msg.isSuccess()) {
                        return msg;
                    }
                }
            }
        } catch (Exception e) {
            return MapMessage.errorMessage("取消申请错误");
        }
        return MapMessage.successMessage();
    }

    /**
     * 老师发送关联学生申请
     * !!应该是老师发送班级关联其他老师申请吧!!!!!! 哭晕在厕所....
     * 作者本人解释一下,这里的关联学生是指与其他老师关联共享学生的意思,最开始产品(王老师)是这种方式描述的
     * 后来关联这词逐渐演化成现在这样了...指老师与老师关联了
     * 其实关联,接管,转让,三者的对象都是老师的学生资源,当前的实现也是映正了这一含义,真正的关系与操作都是挂在group(代指学生资源)上的
     */
    @RequestMapping(value = "sendlinkapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage sendLinkApplication() {
        Long applicantId = getSubjectSpecifiedTeacherId();

        // 检查当前老师是否是假老师
        if (teacherLoaderClient.isFakeTeacher(applicantId)) {
            return MapMessage.errorMessage("您的账号使用存在异常，该功能受限<br/>如有疑议，请进行申诉")
                    .setErrorCode("fake");
        }

        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("No clazzId specified.");
        }

        // 支持学号，手机号
        long respondentId = getRequestLong("respondentId");
        if (respondentId == 0) {
            return MapMessage.errorMessage("No respondentId found.");
        }

        String possibleMobileStr = String.valueOf(respondentId);
        if (MobileRule.isMobile(possibleMobileStr)) {// 手机号情况
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(possibleMobileStr, UserType.TEACHER);
            if (ua == null) {
                return MapMessage.errorMessage("The mobile number is wrong.");
            }
            respondentId = ua.getId();
        }

        // 添加老师学科
        Subject linkSubject = Subject.of(SafeConverter.toString(getRequestString("linkSubject")));

        MapMessage message = teacherAlterationServiceClient.sendLinkApplication(applicantId, respondentId, linkSubject, clazzId, OperationSourceType.pc);

        // 发送消息通知及弹窗
        if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
            ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("{}老师申请一起教{}",
                            applicant.getProfile().getRealname(),
                            clazz.formalizeClazzName());
                }
            };
            sendAppMessageAndJpushForApplication(message, messageBuilder);
            sendApplicationMessageToRespondent(message, messageBuilder, true, true);
        }

        return message;
    }

    /**
     * 老师取消关联学生申请
     */
    @RequestMapping(value = "cancellinkapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage cancelLinkApplication() {
        Long applicantId = getSubjectSpecifiedTeacher().getId();
        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        return cancelApplication(applicantId, recordId, ClazzTeacherAlterationType.LINK);
    }


    /**
     * 老师拒绝其他老师关联学生申请
     */
    @RequestMapping(value = "rejectlinkapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rejectLinkApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) {
            respondentId = getSubjectSpecifiedTeacher().getId();
        }

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = rejectApplication(respondentId, recordId, ClazzTeacherAlterationType.LINK);

        if (message.isSuccess()) {
            ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请和{}老师一起教{}的请求，已被{}老师拒绝",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            };
            sendAppMessageForDealApplication(message, messageBuilder);
            sendApplicationMessageToApplicant(message, messageBuilder, false, false);
        }

        return message;
    }

    /**
     * 老师同意其他老师的关联学生申请
     */
    @RequestMapping(value = "approvelinkapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage approveLinkApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) {
            respondentId = getSubjectSpecifiedTeacher().getId();
        }

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = approveApplication(respondentId, recordId, ClazzTeacherAlterationType.LINK);

        if (message.isSuccess()) {
            ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请和{}老师一起教{}的请求，已被{}老师接受",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            };
            sendAppMessageForDealApplication(message, messageBuilder);
            sendApplicationMessageToApplicant(message, messageBuilder, false, true);
        }

        return message;
    }

    /**
     * 老师发送接管学生申请
     */
    @RequestMapping(value = "sendreplaceapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage sendReplaceApplication() {
        Long applicantId = getSubjectSpecifiedTeacher().getId();

        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("No clazzId specified.");
        }

        long respondentId = getRequestLong("respondentId");
        if (respondentId == 0) {
            return MapMessage.errorMessage("No old teacher id found.");
        }

        MapMessage message = sendApplication(applicantId, clazzId, respondentId, ClazzTeacherAlterationType.REPLACE,
                "此申请发送失败，请重试！");


        // 发送消息通知及弹窗
        if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
            ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("{}老师申请接管您任课的{}",
                            applicant.getProfile().getRealname(),
                            clazz.formalizeClazzName());
                }
            };
            sendAppMessageAndJpushForApplication(message, messageBuilder);
            sendApplicationMessageToRespondent(message, messageBuilder, true, true);
        }

        return message;
    }

    /**
     * 老师取消接管学生资源申请
     */
    @RequestMapping(value = "cancelreplaceapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage cancelReplaceApplication() {
        Long applicantId = getSubjectSpecifiedTeacher().getId();
        Long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        return cancelApplication(applicantId, recordId, ClazzTeacherAlterationType.REPLACE);
    }

    /**
     * 老师拒绝其他老师的接管学生申请
     */
    @RequestMapping(value = "rejectreplaceapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rejectReplaceApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) {
            respondentId = getSubjectSpecifiedTeacher().getId();
        }

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = rejectApplication(respondentId, recordId, ClazzTeacherAlterationType.REPLACE);

        if (message.isSuccess()) {
            ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请接管{}老师的{}的请求，已被{}老师拒绝",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            };
            sendAppMessageForDealApplication(message, messageBuilder);
            sendApplicationMessageToApplicant(message, messageBuilder, false, false);
        }

        return message;
    }

    /**
     * 老师同意其他老师的接管学生申请
     */
    @RequestMapping(value = "approvereplaceapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage approveReplaceApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) {
            respondentId = getSubjectSpecifiedTeacher().getId();
        }

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = approveApplication(respondentId, recordId, ClazzTeacherAlterationType.REPLACE);

        if (message.isSuccess()) {
            ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请接管{}老师的{}的请求，已被{}老师接受",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            };
            sendAppMessageForDealApplication(message, messageBuilder);
            sendApplicationMessageToApplicant(message, messageBuilder, false, true);
        }

        return message;
    }

    /**
     * 发送转让给其他老师申请
     */
    @RequestMapping(value = "sendtransferapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage sendTransferApplication() {
        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();
        if (isCJLSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入数校平台，暂时无法进行调整。有疑问请联系客服。");
        }
        // 希悦学校也不能操作
        if (isSeiueSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入第三方平台，暂时无法进行调整，若有疑问请联系客服。");
        }
        Long applicantId = teacher.getId();
        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("No clazzId specified.");
        }

        long respondentId = getRequestLong("respondentId");
        if (respondentId == 0) {
            return MapMessage.errorMessage("No old teacher id found.");
        }

        MapMessage message = teacherAlterationServiceClient.sendTransferApplication(applicantId, respondentId, clazzId, OperationSourceType.pc);
        if (!message.isSuccess() && StringUtils.isBlank(message.getInfo())) {
            message = MapMessage.errorMessage("此申请发送失败，请重试！");
        }

        // 发送消息通知及弹窗
        if (message.isSuccess()) {// 表示发送了申请
            boolean sendMsg = SafeConverter.toBoolean(message.get("sendMsg"));

            if (sendMsg) {
                ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                    @Override
                    String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                        return StringUtils.formatMessage("{}老师申请转让{}给你",
                                applicant.getProfile().getRealname(),
                                clazz.formalizeClazzName());
                    }
                };
                sendAppMessageAndJpushForApplication(message, messageBuilder);
                sendApplicationMessageToRespondent(message, messageBuilder, true, true);
            }
        }

        return message;
    }

    /**
     * 取消转让给其他老师申请
     */
    @RequestMapping(value = "canceltransferapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage cancelTransferApplication() {
        Long applicantId = getSubjectSpecifiedTeacher().getId();
        Long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        return cancelApplication(applicantId, recordId, ClazzTeacherAlterationType.TRANSFER);
    }

    /**
     * 拒绝其他老师的转让班级申请
     */
    @RequestMapping(value = "rejecttransferapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rejectTransferApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) {
            respondentId = getSubjectSpecifiedTeacher().getId();
        }

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = rejectApplication(respondentId, recordId, ClazzTeacherAlterationType.TRANSFER);

        if (message.isSuccess()) {
            ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请转让给{}老师的{}的请求，已被{}拒绝",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            };
            sendAppMessageForDealApplication(message, messageBuilder);
            sendApplicationMessageToApplicant(message, messageBuilder, false, false);
        }

        return message;
    }

    /**
     * 同意其他老师的转让班级申请
     */
    @RequestMapping(value = "approvetransferapp.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage approveTransferApplication() {
        // 兼容校园大使
        Long respondentId = getRequestLong("respondentId");
        if (respondentId <= 0) {
            respondentId = getSubjectSpecifiedTeacher().getId();
        }

        long recordId = getRequestLong("recordId");
        if (recordId == 0) {
            return MapMessage.errorMessage("No recordId found.");
        }

        MapMessage message = approveApplication(respondentId, recordId, ClazzTeacherAlterationType.TRANSFER);

        if (message.isSuccess()) {
            ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                @Override
                String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                    return StringUtils.formatMessage("您申请转让给{}老师的{}的请求，已被{}接受",
                            respondent.getProfile().getRealname(),
                            clazz.formalizeClazzName(),
                            respondent.getProfile().getRealname());
                }
            };
            sendAppMessageForDealApplication(message, messageBuilder);
            sendApplicationMessageToApplicant(message, messageBuilder, false, true);
        }

        return message;
    }

    /**
     * 关联老师 -> 确定
     * 通过老师id或手机号查找老师
     * 返回:
     * 1. 通过姓名和手机找到对应老师，且姓名和手机号一致
     * 2. 通过姓名和手机找到对应老师，但是姓名和手机号不一致，返回所有姓名相同的老师以及手机号对应老师
     * 3. 通过姓名或手机号找到老师，优先手机号查询
     * 4. 通过手机号找不到老师，提示邀请
     * 5. 找不到老师
     */
    @RequestMapping(value = "findlinkteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findTeacher() {
        // 老师的姓名
        String name = StringUtils.trim(getRequestString("name"));
        // 老师手机号
        String mobile = StringUtils.trim(getRequestString("mobile"));
        if (StringUtils.isEmpty(name) && StringUtils.isEmpty(mobile)) {
            return MapMessage.errorMessage("name and mobile cannot be null.");
        }

        // 关联的学科
        String subjectStr = getRequestString("targetSubject");
        if (StringUtils.isEmpty(subjectStr)) {
            return MapMessage.errorMessage("subject cannot be null.");
        }

        // 关联的班级
        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("clazzId cannot be null.");
        }

        TeacherDetail teacher = getSubjectSpecifiedTeacherDetail();

        if (isCJLSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入数校平台，暂时无法进行调整。有疑问请联系客服。");
        }
        // 希悦学校也不能操作
        if (isSeiueSchool(teacher.getTeacherSchoolId())) {
            return MapMessage.errorMessage("已接入第三方平台，暂时无法进行调整，若有疑问请联系客服。");
        }
        return teacherSystemClazzServiceClient
                .handleFindTeacherProcess(name,
                        mobile,
                        Subject.valueOf(subjectStr),
                        teacher.getTeacherSchoolId(),
                        clazzId);
    }

//    /**
//     * 关联老师
//     * 通过姓名查找老师
//     */
//    @RequestMapping(value = "findteacherbyname.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage findTeacherByName() {
//        String name = getRequestString("name");
//        if (StringUtils.isEmpty(name)) {
//            return MapMessage.errorMessage("name cannot be null.");
//        }
//
//        String subjectStr = getRequestString("subject");
//        if (StringUtils.isEmpty(subjectStr)) {
//            return MapMessage.errorMessage("subject cannot be null.");
//        }
//
//        TeacherDetail teacher = currentTeacherDetail();
//
//        List<Teacher> teachers = teacherSystemClazzServiceClient
//                .findTeacherByName(name, Subject.valueOf(subjectStr), teacher.getTeacherSchoolId());
//
//        if (CollectionUtils.isEmpty(teachers)) {// 未找到老师，返回error
//            return MapMessage.errorMessage();
//        } else {
//            return MapMessage.successMessage().add("teachers", teachers);
//        }
//    }

    /**
     * 邀请老师
     */
    @RequestMapping(value = "invitelinkteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage inviteLinkTeacher() {
        // 老师手机号
        String mobile = StringUtils.trim(getRequestString("mobile"));
        if (StringUtils.isEmpty(mobile)) {
            return MapMessage.errorMessage("name and mobile cannot be null.");
        }

        // 关联的学科
        String subjectStr = getRequestString("subject");
        if (StringUtils.isEmpty(subjectStr)) {
            return MapMessage.errorMessage("subject cannot be null.");
        }

        // 关联的班级
        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("clazzId cannot be null.");
        }

        // 老师的姓名
        String name = StringUtils.trim(getRequestString("name"));

        TeacherDetail teacher = currentTeacherDetail();

        MapMessage message = teacherSystemClazzServiceClient
                .inviteAndLinkTeacher(mobile, Subject.valueOf(subjectStr), teacher.getTeacherSchoolId(), teacher.getId(), teacher.getSubject(), clazzId, name);

        if (message.isSuccess()) {
            if (StringUtils.equals(TeacherSystemClazzService.FIND_STATE_TEACHER_CREATE, SafeConverter.toString(message.get("type")))) {
                User user = (User) message.remove("teacher");
                String password = (String) message.remove("password");

                String smsPayload = StringUtils.formatMessage(
                        "{}老师你好！{}老师邀请您使用一起作业网(17zuoye.com)辅助教学，系统已为您注册账号{}密码{}",
                        name,
                        teacher.getProfile().getRealname(),
                        user.getId(),
                        password
                );
                smsServiceClient.createSmsMessage(mobile)
                        .content(smsPayload)
                        .type(SmsType.CREATE_TEACHER_AND_HANDOVER_CLAZZ.name())
                        .send();
            }
        }

        return message;
    }

    /**
     * 邀请老师
     */
    @RequestMapping(value = "invitetransferteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage inviteTransferTeacher() {
        // 老师手机号
        String mobile = StringUtils.trim(getRequestString("mobile"));
        if (StringUtils.isEmpty(mobile)) {
            return MapMessage.errorMessage("name and mobile cannot be null.");
        }

        // 关联的班级
        long clazzId = getRequestLong("clazzId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("clazzId cannot be null.");
        }

        // 老师的姓名
        String name = StringUtils.trim(getRequestString("name"));

        TeacherDetail teacher = currentTeacherDetail();

        MapMessage message = teacherSystemClazzServiceClient
                .inviteAndTransferTeacher(mobile, teacher.getSubject(), teacher.getTeacherSchoolId(), teacher.getId(),
                        teacher.getSubject(), clazzId, name, OperationSourceType.pc);

        if (message.isSuccess()) {
            if (StringUtils.equals(TeacherSystemClazzService.FIND_STATE_TEACHER_CREATE, SafeConverter.toString(message.get("type")))) {
                User user = (User) message.remove("teacher");
                String password = (String) message.remove("password");

                String smsPayload = StringUtils.formatMessage(
                        "{}老师您好，{}老师邀请您使用一起作业网(17zuoye.com)辅助教学，系统已为您注册账号{}密码{}",
                        name,
                        teacher.getProfile().getRealname(),
                        user.getId(),
                        password
                );
                smsServiceClient.createSmsMessage(mobile)
                        .content(smsPayload)
                        .type(SmsType.CREATE_TEACHER_AND_HANDOVER_CLAZZ.name())
                        .send();
            }
        }

        return message;
    }

    /////////////////////////////////////////private methods/////////////////////////////////////////////////////

    /**
     * 发送申请
     *
     * @param applicantId  申请人ID
     * @param clazzId      班级ID
     * @param respondentId 被申请人ID
     * @param type         申请类型
     * @param errMsg       出错信息
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     */
    private MapMessage sendApplication(long applicantId,
                                       long clazzId,
                                       long respondentId,
                                       ClazzTeacherAlterationType type,
                                       String errMsg) {
        MapMessage msg = teacherAlterationServiceClient.sendApplication(applicantId, respondentId, clazzId, type, OperationSourceType.pc);
        if (!msg.isSuccess() && StringUtils.isBlank(msg.getInfo())) {
            msg = MapMessage.errorMessage(errMsg);
        }
        return msg;
    }


    /**
     * 取消申请
     *
     * @param applicantId 申请人ID
     * @param recordId    申请ID
     * @param type        申请类型
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     */
    private MapMessage cancelApplication(long applicantId,
                                         long recordId,
                                         ClazzTeacherAlterationType type) {
        MapMessage msg = teacherAlterationServiceClient.cancelApplication(applicantId, recordId, type, OperationSourceType.pc);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("操作失败");
        }
        return msg;
    }

    /**
     * 拒绝申请
     *
     * @param respondentId 被申请人ID
     * @param recordId     申请ID
     * @param type         申请类型
     * @return 成功返回map，包含以下基本信息：
     * {
     * "recordId": 申请记录ID
     * "applicant": 申请人信息
     * "respondent": 被申请人信息
     * "clazz": 班级信息
     * }
     * 失败返回error message
     */
    private MapMessage rejectApplication(long respondentId,
                                         long recordId,
                                         ClazzTeacherAlterationType type) {
        MapMessage msg = teacherAlterationServiceClient.rejectApplication(respondentId, recordId, type, OperationSourceType.pc);
        if (!msg.isSuccess() && !Objects.equals(msg.getInfo(), TeacherAlterationService.DUP_OPERATION_ERR_MSG)) {
            msg = MapMessage.errorMessage("拒绝申请失败");
        }
        return msg;
    }

    /**
     * 同意申请
     *
     * @param respondentId 被申请人ID
     * @param recordId     申请ID
     * @param type         申请类型
     */
    private MapMessage approveApplication(long respondentId,
                                          long recordId,
                                          ClazzTeacherAlterationType type) {
        return teacherAlterationServiceClient.approveApplication(respondentId, recordId, type, OperationSourceType.pc);
    }

    /**
     * 给申请发送者发送消息提醒
     * <p>
     * 注意，这里会remove掉message里的objects
     *
     * @param message              申请消息
     * @param messageBuilder       发送消息生成器
     * @param appendCheckDetailBtn 是否在消息提醒中添加‘查看详情’按钮
     * @param needPopup            是否弹窗
     */
    private void sendApplicationMessageToRespondent(MapMessage message,
                                                    ApplicationMessageBuilder messageBuilder,
                                                    boolean appendCheckDetailBtn,
                                                    boolean needPopup) {
        try {
            Teacher applicant = (Teacher) message.remove("applicant");
            Teacher respondent = (Teacher) message.remove("respondent");
            Clazz clazz = (Clazz) message.remove("clazz");
            String sendMsg = messageBuilder.buildMessage(applicant, respondent, clazz);

            doSendApplicationMessage(respondent, sendMsg, appendCheckDetailBtn, needPopup);

            Map<Long, List<UserWechatRef>> tid_refs_map = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(respondent.getId()));
            List<UserWechatRef> refs = tid_refs_map.get(respondent.getId());
            if (CollectionUtils.isNotEmpty(refs)) {
                //发送微信模版消息，如果applicant绑定了微信的话
                doSendApplicationMessageByWechat(applicant, respondent);
            } else {
                //否则，发送短信提醒消息
                doSendApplicationMessageBySMS(applicant, respondent);
            }
        } catch (Exception ex) {
            // 申请其实已经成功了。。。记下log就好了吧
            log.error("Send application succeed but send message failed.", ex.getMessage(), ex);
        }
    }

    /**
     * 给申请接受者发送消息提醒
     * <p>
     * 注意，这里会remove掉message里的objects
     *
     * @param message              申请消息
     * @param messageBuilder       发送消息生成器
     * @param appendCheckDetailBtn 是否在消息提醒中添加‘查看详情’按钮
     * @param needPopup            是否弹窗
     */
    private void sendApplicationMessageToApplicant(MapMessage message,
                                                   ApplicationMessageBuilder messageBuilder,
                                                   boolean appendCheckDetailBtn,
                                                   boolean needPopup) {
        try {
            Teacher applicant = (Teacher) message.remove("applicant");
            Teacher respondent = (Teacher) message.remove("respondent");
            Clazz clazz = (Clazz) message.remove("clazz");
            String sendMsg = messageBuilder.buildMessage(applicant, respondent, clazz);
            //发送站内信
            doSendApplicationMessage(applicant, sendMsg, appendCheckDetailBtn, needPopup);
            //发送微信消息通知
            Map<String, Object> extensionInfo = MapUtils.m("first", "班级申请通知",
                    "keyword1", "一起作业",
                    "keyword2", "您向" + respondent.fetchRealname() + "老师发出的班级请求已有结果",
                    "url", ProductConfig.get("wechat.url") + "/teacher/message/list.vpage?_from=wechatnotice");
            Map<Long, List<UserWechatRef>> userWechatRefs = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(applicant.getId()), WechatType.TEACHER);
            List<UserWechatRef> refs = userWechatRefs.get(applicant.getId());
            if (CollectionUtils.isNotEmpty(refs)) {
                wechatServiceClient.processWechatNotice(
                        WechatNoticeProcessorType.TeacherOperationNotice, applicant.getId(), extensionInfo, WechatType.TEACHER);
            }
        } catch (Exception ex) {
            // 申请其实已经成功了。。。记下log就好了吧
            log.error("Send application succeed but send message failed.", ex.getMessage(), ex);
        }
    }

    private void doSendApplicationMessage(Teacher user,
                                          String message,
                                          boolean appendCheckDetailBtn,
                                          boolean needPopup) {
        // 需要加链接的
        if (appendCheckDetailBtn) {
            message = StringUtils.formatMessage(
                    message + " <a href=\"{}\" class=\"w-blue\" target=\"_blank\">【查看详情】</a>",
                    "/teacher/systemclazz/clazzindex.vpage"
            );
            // 发送站内信  带链接
            sendMessage(user, message);
        } else {
            // 发送站内信  不带链接
            sendMessage(user, message);
        }
        // 发右下角弹窗
        if (needPopup) {
            userPopupServiceClient.createPopup(user.getId())
                    .content(message)
                    .type(PopupType.TEACHER_ALTERATION_FOR_RESPONDENT)
                    .category(PopupCategory.LOWER_RIGHT)
                    .create();
        }
    }

    //发送微信模版消息通知
    private void doSendApplicationMessageByWechat(Teacher applicant, Teacher respondent) {
        // 查询微信
        Map<String, Object> extensionInfo = MapUtils.m("applicantId", applicant.getId(),
                "applicantName", applicant.fetchRealname(),
                "respondentId", respondent.getId(),
                "respondentName", respondent.fetchRealname());
        wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.TeacherClazzAlterationNotice,
                respondent.getId(), extensionInfo, WechatType.TEACHER);
    }

    //发送短信通知
    private void doSendApplicationMessageBySMS(Teacher applicant, Teacher respondent) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(respondent.getId());
        if (ua.isMobileAuthenticated()) {
            // 每个手机号，每天最多收到3个类似的班级调整的短信
            String phone = sensitiveUserDataServiceClient.loadUserMobile(ua.getId());
            if (smsServiceClient.getSmsService().canSendClazzManagement(phone).getUninterruptibly()) {
                String content = "您收到" + applicant.fetchRealname() + "老师的班级请求，点击链接立刻处理，" + "http://www.17zyw.cn/ABvANn";
                userSmsServiceClient.buildSms().to(ua)
                        .content(content)
                        .type(SmsType.CLAZZ_ALTERATION_NOTIFY)
                        .send();
            }
        }
    }

    /**
     * 处理老师请求加入有学生资源的老师
     * 注意，如果老师需要加入班级，调用的是强行加入班级，从而可以跳过班级数量检查
     * 见redmine 28964
     * <p>
     * TODO 这个代码重复了3遍，分别在ucenter、washington、wechat三个项目中
     * TODO 原因是发消息部分，JPUSH依赖vendor，微信消息依赖微信，实际上这些基础的服务，应该在更底层，上层只是负责解析出发送所需的对象
     * TODO 找时间必须重构了
     *
     * @param curTeacher
     * @param clazzId
     * @param teacherIds
     * @return
     */
    @SuppressWarnings("unchecked")
    private MapMessage handleTeacherRequestStudentResource(Teacher curTeacher, Long clazzId, List<Long> teacherIds) {
        Map<Long, Teacher> teachers = teacherLoaderClient.loadTeachers(teacherIds);
        Set<Subject> subjectSet = teachers.values().stream().map(Teacher::getSubject).collect(Collectors.toSet());

        // 如果teacherIds中包含curTeacher.getId()，表示老师想重新加回到组中(退班不退组)，把该班级id直接返回回去，让后面的逻辑处理
        if (teacherIds.contains(curTeacher.getId())) return MapMessage.successMessage();

        // 当无同科老师时，认证老师可以直接关联
        if (currentTeacher().getAuthenticationState().equals(AuthenticationState.SUCCESS.getState())) {
            Teacher respondent = teachers.values().stream().filter(t -> t.getSubject() != curTeacher.getSubject()).findFirst().orElse(null);
            if (respondent != null) {
                // 处理老师当前的关联状况
                MapMessage result = clazzServiceClient.handleTeacherLinkOperation(curTeacher, respondent, clazzId, OperationSourceType.pc);
                if (!result.isSuccess()) {
                    return result;
                }

                Map<Teacher, Teacher> replaceTeachers = (Map<Teacher, Teacher>) result.remove("replaceTeachers");
                Map<Teacher, Teacher> linkTeachers = (Map<Teacher, Teacher>) result.remove("linkTeachers");

                if (MapUtils.isNotEmpty(replaceTeachers) || MapUtils.isNotEmpty(linkTeachers)) {
                    boolean needSendApp = SafeConverter.toBoolean(result.remove("needSendApp"));

                    if (!needSendApp) {// 不需要发送请求的状况，直接接管
                        List<Long> addClazzTeacherIds = (List<Long>) result.remove("addClazzTeacherIds");
                        for (Long teacherId : addClazzTeacherIds) {
                            MapMessage message = clazzServiceClient.teacherJoinSystemClazzForce(teacherId, clazzId);// 强制加入
                            if (!message.isSuccess()) {
                                return message;
                            }
                        }
                        MapMessage m = MapMessage.successMessage();
                        for (Map.Entry<Teacher, Teacher> entry : replaceTeachers.entrySet()) {
                            Teacher fromT = entry.getKey();
                            Teacher toT = entry.getValue();
                            m = groupServiceClient.replaceTeacherGroupForReplace(fromT.getId(), toT.getId(), clazzId, toT.getId().toString(), UserOperatorType.TEACHER);
                        }
                        for (Map.Entry<Teacher, Teacher> entry : linkTeachers.entrySet()) {
                            Teacher fromT = entry.getKey();
                            Teacher toT = entry.getValue();
                            m = groupServiceClient.shareTeacherGroup(fromT.getId(),
                                    toT.getId(), fromT.getSubject(), toT.getSubject(), clazzId, curTeacher.getId().toString(), UserOperatorType.TEACHER);
                        }
                        if (m.isSuccess()) {// 已加入班级，不需要发送请求
                            return MapMessage.successMessage().add("join", true);
                        }
                    }
                }
            }

        }

        for (Teacher teacher : teachers.values()) {
            if (teacher.getSubject().equals(curTeacher.getSubject())) {// 同学科，进行接管申请
                MapMessage message = sendApplication(curTeacher.getId(), clazzId, teacher.getId(), ClazzTeacherAlterationType.REPLACE,
                        "向班级任课教师申请接管学生资源失败");
                // 发送消息通知及弹窗
                if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
                    ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                        @Override
                        String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                            return StringUtils.formatMessage("{}老师申请接管您任课的{}的学生资源",
                                    applicant.getProfile().getRealname(),
                                    clazz.formalizeClazzName());
                        }
                    };
                    sendAppMessageAndJpushForApplication(message, messageBuilder);
                    sendApplicationMessageToRespondent(message, messageBuilder, true, true);
                } else {
                    return message;
                }
            } else {// 不同学科，判断是否有同学科的老师，没有发送关联申请
                // 或者老师为认证老师，则发送关联请求，一旦关联，之前的关联老师被接管
                if (teacher.getAuthenticationState() == AuthenticationState.SUCCESS.getState()
                        || !subjectSet.contains(curTeacher.getSubject())) {
                    MapMessage message = sendApplication(curTeacher.getId(), clazzId, teacher.getId(),
                            ClazzTeacherAlterationType.LINK, "向班级任课教师申请关联学生资源失败");

                    // 发送消息通知及弹窗
                    if (message.isSuccess() && message.containsKey("recordId")) {// 表示发送了申请
                        ApplicationMessageBuilder messageBuilder = new ApplicationMessageBuilder() {
                            @Override
                            String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz) {
                                return StringUtils.formatMessage("{}老师申请关联您任课的{}的学生资源",
                                        applicant.getProfile().getRealname(),
                                        clazz.formalizeClazzName());
                            }
                        };
                        sendAppMessageAndJpushForApplication(message, messageBuilder);
                        sendApplicationMessageToRespondent(message, messageBuilder, true, true);
                    } else {
                        return message;
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 申请消息生成器
     *
     * @author changyuan.liu
     */
    private abstract class ApplicationMessageBuilder {
        /**
         * 生成申请消息
         *
         * @param applicant  申请人
         * @param respondent 被申请人
         * @param clazz      班级
         * @return 申请消息
         */
        abstract String buildMessage(Teacher applicant, Teacher respondent, Clazz clazz);
    }

    private void sendMessage(User receiver, String payload) {
        if (StringUtils.isBlank(payload)) {
            return;
        }
        payload = StringUtils.replace(payload, "老师老师", "老师");
        teacherLoaderClient.sendTeacherMessage(receiver.getId(), payload);
    }

    /**
     * 发班级申请时,给对方发送app消息,同时推送jpush.
     * 发link replace 以及transfer类申请.
     */
    private void sendAppMessageAndJpushForApplication(MapMessage message, ApplicationMessageBuilder applicationMessageBuilder) {

        Teacher applicant = (Teacher) message.get("applicant");
        Teacher respondent = (Teacher) message.get("respondent");
        Clazz clazz = (Clazz) message.get("clazz");
        Long recordId = (Long) message.get("recordId");

        //TODO 新前端域名处理
        String messageContent = applicationMessageBuilder.buildMessage(applicant, respondent, clazz);

        String messageUrl = TeacherMessageType.getApplicationUrlTemplate() + recordId;
        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(respondent.getId());
        appMessage.setLinkUrl(messageUrl);
        appMessage.setLinkType(1);
        appMessage.setMessageType(TeacherMessageType.APPLICATION.getType());
        appMessage.setContent(messageContent);
        appMessage.setTitle("班级请求");
        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);

        List<Long> userIdList = new ArrayList<>();
        userIdList.add(respondent.getId());
        String key = "";
        if (Ktwelve.JUNIOR_SCHOOL.equals(respondent.getKtwelve()))
            key = "m";
        if (Ktwelve.PRIMARY_SCHOOL.equals(respondent.getKtwelve()))
            key = "j";
        if (Ktwelve.INFANT.equals(respondent.getKtwelve()))
            key = "i";

        Map<String, Object> extroInfo = MapUtils.m("s", TeacherMessageType.APPLICATION.getType(), "key", key, "link", fetchMainsiteUrlByCurrentSchema() + messageUrl, "t", "h5");
        appMessageServiceClient.sendAppJpushMessageByIds(messageContent, AppMessageSource.JUNIOR_TEACHER, userIdList, extroInfo);

    }

    /**
     * 处理班级请求时,给申请者发app消息,不发jpush
     */
    private void sendAppMessageForDealApplication(MapMessage message, ApplicationMessageBuilder applicationMessageBuilder) {

        Teacher applicant = (Teacher) message.get("applicant");
        Teacher respondent = (Teacher) message.get("respondent");
        Clazz clazz = (Clazz) message.get("clazz");

        String messageContent = applicationMessageBuilder.buildMessage(applicant, respondent, clazz);

        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(applicant.getId());
        appMessage.setMessageType(TeacherMessageType.CLAZZNEWS.getType());
        appMessage.setContent(messageContent);
        appMessage.setTitle(TeacherMessageType.CLAZZNEWS.getDescription());
        messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);

    }


}
