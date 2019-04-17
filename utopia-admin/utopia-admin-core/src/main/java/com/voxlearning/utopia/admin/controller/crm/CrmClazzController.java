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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.*;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.api.concurrent.AlpsFutureMap;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.athena.api.SummerMarketLoadSummaryService;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.helper.ClassJieHelper;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.clazz.client.NewClazzServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.TeacherSystemClazzService;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.constants.UserConstants;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.entities.log.UserOperatorType;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueLoaderClient;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.mapper.JournalPagination;
import com.voxlearning.utopia.service.zone.client.ClazzJournalLoaderClient;
import com.voxlearning.utopia.service.zone.client.ClazzJournalManagerClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Longlong Yu
 * @since 下午7:49,13-6-25.
 */
@Controller
@RequestMapping("/crm/clazz")
public class CrmClazzController extends CrmAbstractController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;
    @Inject private ClazzJournalLoaderClient clazzJournalLoaderClient;
    @Inject private ClazzJournalManagerClient clazzJournalManagerClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private NewClazzBookLoaderClient newClazzBookLoaderClient;
    @Inject private NewClazzServiceClient newClazzServiceClient;
    @Inject private NewKuailexueLoaderClient newKuailexueLoaderClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private SmsServiceClient smsServiceClient;

    @ImportService(interfaceClass = SummerMarketLoadSummaryService.class)
    private SummerMarketLoadSummaryService stuAuthQueryService;

    /**
     * ***********************查询班级*****************************************************************
     */

    @RequestMapping(value = "groupinfo.vpage")
    public String groupInfo(Model model) {
        Long groupId = getRequestLong("groupId");
        Long teacherId = getRequestLong("teacherId");
        Long clazzId = getRequestLong("clazzId");
        Long studentId = getRequestLong("studentId");
        Map<Long, GroupMapper> clazzGroups = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        if (groupId != 0) {
            GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
            if (groupMapper != null) {
                clazzGroups.put(groupMapper.getId(), groupMapper);
            }
        } else if (studentId != 0) {
            // 学生列表中的班级入口---直接Load跟学生相关的所有组
            clazzGroups = groupLoaderClient.loadStudentGroups(studentId, false)
                    .stream()
                    .collect(Collectors.toMap(GroupMapper::getId, Function.identity()));
        } else if (teacherId == 0) {
            // 学校下的班级列表入口---通过clazzId load下面的grouplist
            clazzGroups = groupLoaderClient.loadClazzGroups(clazzId)
                    .stream()
                    .collect(Collectors.toMap(GroupMapper::getId, Function.identity()));
        } else {
            // 学生/老师各个详情页入口，teacherId+clazzId load唯一的一个group
            GroupMapper groupMapper = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
            if (groupMapper != null) {
                clazzGroups.put(groupMapper.getId(), groupMapper);
                groupId = groupMapper.getId();
            }
        }
        //组的班级信息
        Ktwelve ktwelve = Ktwelve.PRIMARY_SCHOOL;
        ClazzType clazzType = ClazzType.PUBLIC;
        Map<String, Object> groupClazzInfo = new HashMap<>();
        List<Map<String, Object>> groupInfoList = new ArrayList<>();
        School school = null;
        //把这些组的班级ID拿出来
        clazzId = clazzGroups.values().stream().map(GroupMapper::getClazzId).findFirst().orElse(0L);
        if (clazzId != 0) {
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            if (clazz != null) {
                groupClazzInfo.put("clazzId", clazz.getId());
                groupClazzInfo.put("clazzLevel", String.valueOf(ClassJieHelper.toClazzLevel(SafeConverter.toInt(clazz.getJie()), clazz.getEduSystem()).getLevel()));
                groupClazzInfo.put("clazzName", clazz.formalizeClazzName());
                groupClazzInfo.put("clazzLockShowRank", clazz.isShowRankLocked());
                groupClazzInfo.put("schoolLevel", SchoolLevel.JUNIOR.getLevel());
                if (clazz.getSchoolId() != null) {
                    school = schoolLoaderClient.getSchoolLoader()
                            .loadSchool(clazz.getSchoolId())
                            .getUninterruptibly();
                    if (school != null) {
                        groupClazzInfo.put("schoolId", school.getId());
                        groupClazzInfo.put("schoolName", school.getCname());
                        groupClazzInfo.put("schoolLevel", school.getLevel());
                    }
                }
                groupClazzInfo.put("titleName", clazz.formalizeClazzName() + "(" + clazz.getId() + ")");
                ktwelve = clazz.getEduSystem().getKtwelve();
                clazzType = clazz.getClazzType();
            }

            resultMap.put("groupClazzInfo", groupClazzInfo);
        }

        if (MapUtils.isNotEmpty(clazzGroups)) {
            for (Long gid : clazzGroups.keySet()) {
                Map<String, Object> groupInfo = getGroupInfo(gid);
                groupInfoList.add(groupInfo);
            }

            // 按照学生人数从大到小排序
            Collections.sort(groupInfoList, (o1, o2) ->
                    Integer.compare((int) o2.get("studentCount"), (int) o1.get("studentCount"))
            );

        }
        resultMap.put("groupInfoList", groupInfoList);

        model.addAttribute("resultMap", resultMap);

        // 中小学支持
        List<KeyValuePair<Integer, String>> paris = getClazzLevels(ktwelve);
        model.addAttribute("clazzLevels", paris);
        model.addAttribute("seniorSchool", school != null && school.isSeniorSchool());
        model.addAttribute("groupId", groupId);
        model.addAttribute("teacherId", teacherId);
        model.addAttribute("clazzId", clazzId);
        model.addAttribute("ktwelve", ktwelve.name());
        model.addAttribute("clazzType", clazzType.name());
        model.addAttribute("cjlSchool", school != null && isCJLSchool(school.getId()));
        model.addAttribute("isSeiueSchool", school != null && isSeiueSchool(school.getId()));
        return "crm/clazz/groupinfo";
    }

    /**
     * 给指定班级指派老师
     */
    @RequestMapping(value = "joinclass.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage joinClass(@RequestParam Long teacherId, @RequestParam Long groupId) {

        //校验制定老师是否与指定班同校
        School teacherSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (null == teacherSchool) {
            return MapMessage.errorMessage("学校不存在");
        }

        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            return MapMessage.errorMessage("组不存在");
        }

        if (groupMapper.getGroupType() == GroupType.WALKING_GROUP) {
            return MapMessage.errorMessage("教学班不允许添加老师");
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(groupMapper.getClazzId());
        if (null == clazz) {
            return MapMessage.errorMessage("班级不存在");
        }

        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (null == teacher) {
            return MapMessage.errorMessage("教师不存在");
        }

        if (!clazz.getSchoolId().equals(teacherSchool.getId())) {
            return MapMessage.errorMessage("老师与班级必须同校");
        }

        // 读取分组老师
        List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(groupId);
        // 读取对应共享分组
        Set<Long> sharedGroupIds = new HashSet<>();
        sharedGroupIds.add(groupId);

        Set<Long> sids = groupLoaderClient.loadSharedGroupIds(groupId);
        if (CollectionUtils.isNotEmpty(sids)) {
            sharedGroupIds.addAll(sids);
        }

        Map<Long, GroupMapper> sharedGroups = groupLoaderClient.loadGroups(sharedGroupIds, false);

        // FIXME 临时加一个校验
        if (CollectionUtils.isEmpty(teachers) && groupMapper.getSubject() != teacher.getSubject()) {
            for (Long gid : sharedGroupIds) {
                if (CollectionUtils.isNotEmpty(raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient().findByGroupId(gid))) {
                    return MapMessage.errorMessage("请去有老师的组去做添加操作");
                }
            }
        }

        // FIXME 判断组下是否已经有同科老师了， 要逐步抽出到基础服务中
        for (GroupMapper gm : sharedGroups.values()) {
            if (gm.getSubject() != teacher.getSubject()) {
                continue;
            }
            List<GroupTeacherTuple> gtfs = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient().findByGroupId(gm.getId());
            if (CollectionUtils.isNotEmpty(gtfs)) {
                return MapMessage.errorMessage("已添加该科目老师");
            }
        }

        // 验证该老师是否在该班级中
        boolean isTeachingClazz = teacherLoaderClient.isTeachingClazz(teacherId, clazz.getId());
        MapMessage message = MapMessage.errorMessage();
        // 在班级中，需要验证是否已关联其他同科老师
        if (isTeachingClazz) {
            List<Teacher> sharedTeachers = teacherLoaderClient.loadSharedTeachers(teacherId, clazz.getId(), true);
            Set<Subject> sharedSubjects = sharedTeachers.stream().map(Teacher::getSubject).collect(Collectors.toSet());
            if (sharedSubjects.contains(teacher.getSubject())) {
                return MapMessage.errorMessage("所添加老师已在该班级关联了其他同科目老师");
            }
        }

        // 不在班级中，加入班级
        if (!isTeachingClazz) {
            message = clazzServiceClient.teacherJoinSystemClazzForce(teacherId, clazz.getId());
        }

        if (CollectionUtils.isNotEmpty(teachers)) {
            Teacher t = teachers.get(0);
            groupServiceClient.shareTeacherGroup(t.getId(), teacherId, t.getSubject(), teacher.getSubject(), clazz.getId(), getCurrentAdminUser().getAdminUserName(), UserOperatorType.CRM_ADMIN);
        }

        if (message.isSuccess()) {
            String operation = "老师" + teacherId + "加入班" + groupMapper.getClazzId();

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
            userServiceRecord.setOperationContent("老师加入班级");
            userServiceRecord.setComments(operation);
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }
        return MapMessage.successMessage("加入班级成功").add("applicant", teacher).add("clazz", clazz);
    }

    /**
     * 为无老师Group添加老师,在加入的老师备注记录操作
     */
    @RequestMapping(value = "jointeacherintogroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage joinTeacherIntoGroup() {
        Long teacherId = getRequestLong("teacherId", 0);
        Long groupId = getRequestLong("groupId", 0);
        Long clazzId = getRequestLong("clazzId", 0);
        String joinTeacherIntoEmptyGroupDesc = getRequestString("joinTeacherIntoEmptyGroupDesc");
        if (teacherId == 0 || groupId == 0 || clazzId == 0 || "".equals(joinTeacherIntoEmptyGroupDesc)) {
            return MapMessage.errorMessage().setInfo("参数填写错误!");
        }

        //为无老师groupId添加老师teacerId
        GroupMapper mapper = groupLoaderClient.loadGroup(groupId, false);
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (mapper == null || teacher == null) {
            return MapMessage.errorMessage("参数填写错误!");
        }

        if (mapper.getSubject() != teacher.getSubject()) {
            return MapMessage.errorMessage("老师学科和班组学科不一样!");
        }

        MapMessage mapMessage = groupServiceClient.joinTeacherIntoGroups(teacherId, Arrays.asList(groupId));
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }

        // 记录 UserServiceRecord
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(teacherId);
        userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        userServiceRecord.setOperationType(UserServiceRecordOperationType.老师换班.name());
        userServiceRecord.setOperationContent("管理员操作老师加入班级");
        userServiceRecord.setComments("班级:" + clazzId + ", 组" + groupId);
        userServiceClient.saveUserServiceRecord(userServiceRecord);

        return MapMessage.successMessage().setInfo("修改成功!");
    }


//    /**
//     * 老师退出指定班级
//     */
//    @RequestMapping(value = "quitclass.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage removeTeacherFromClass(@RequestParam Long teacherId, @RequestParam Long classId) {
//        //TODO 这个方法确认了只在班级详情页clazzinfo.ftl页面有调用。
//        //TODO 现在改成groupinfo页面了，没有这个请求的入口了，什么时候给干掉吧。
//        //TODO 同时，这个请求存在一个问题====如果老师退出最后一个班级，是没有向USER_SCHOOL_REF写记录的。
//        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
//        Clazz clazz = clazzLoaderClient.loadClazz(classId);
//        if (teacher == null || clazz == null) {
//            return MapMessage.errorMessage().setInfo("不存在老师或者班级");
//        }
//        List<Teacher> clazzTeachers = ClazzTeacher.toTeacherList(teacherLoaderClient.loadClazzTeachers(classId));
//        ClazzTeacher ct = teacherLoaderClient.loadClazzCreator(classId);
//        Teacher creator = ct == null ? null : ct.getTeacher();
//        if (creator != null && creator.getId().equals(teacherId)) {
//            //不用判断了，有的老师就要求无条件删除
////            if (clazzTeachers.size() == 1) {
////                //先去添加一个老师，再来删
////                return MapMessage.errorMessage().setInfo("班级只有一个老师并且是班级创建者，不能删除")
////            }else {
//            for (Teacher clazzTeacher : clazzTeachers) {
//                if (!clazzTeacher.getId().equals(teacherId)) {
//                    ClazzTeacherRef clazzTeacherRef = clazzTeacherRefPersistence.findByUserIdAndClazzId(clazzTeacher.getId(), classId);
//                    if (clazzTeacherRef != null) {
//                        clazzTeacherRef.setRefType(RefType.CREATOR.getKey());
//                        clazzTeacherRefPersistence.update(clazzTeacherRef.getId(), clazzTeacherRef);
//                        break;
//                    }
//                }
//            }
////            }
//        }
//        //对该班级下所有学生奖品中心订单做取消操作
//        if (clazzTeachers.size() == 1) {
//            String sql = "SELECT o.ID,o.BUYER_ID,o.TOTAL_PRICE FROM VOX_REWARD_ORDER o,VOX_CLASS_STUDENT_REF r WHERE o.BUYER_ID = r.USER_ID " +
//                    "AND r.DISABLED = FALSE AND o.STATUS='SUBMIT'  AND o.DISABLED = FALSE AND r.CLAZZ_ID=? ";
//            List<Map<String, Object>> orderList = utopiaSql.withSql(sql).useParamsArgs(classId).queryAll();
//            for (Map<String, Object> order : orderList) {
//                Double totalPrice = ConversionUtils.toDouble(order.get("TOTAL_PRICE"), 0);
//                IntegralHistory integralHistory = new IntegralHistory((Long) order.get("BUYER_ID"), IntegralType.奖品相关, totalPrice.intValue());
//                integralHistory.setComment("奖品中心取消订单加学豆");
//                MapMessage msg = integralServiceClient.changeIntegral(integralHistory);
//                if (msg.isSuccess()) {
//                    logger.debug("Add integral:");
//                    logger.debug("  integral      -> {}", order.get("TOTAL_PRICE"));
//                    logger.debug("  integral type -> {}", IntegralType.奖品相关);
//                    logger.debug("  user id       -> {}", order.get("BUYER_ID"));
//                } else {
//                    logger.warn("给学生{}补加学豆失败", order.get("BUYER_ID"));
//                }
//                //发系统消息
//                String content = "你所在的班级没有老师了，由于你在奖品中心兑换的奖品没有人收货，很遗憾的通知你，订单被取消了。";
//                messageQueueSystem.sendUserMessage((Long) order.get("BUYER_ID"), content);
//                //删除订单
//                rewardManagementClient.removeRewardOrder((Long) order.get("ID"));
//            }
//        }
//
//        customerServiceRecordPersistence.addCustomerServiceRecord(teacherId,
//                getCurrentAdminUser().getAdminUserName(), RecordType.老师操作, "", "从班级中删除老师");
//
//        clazzTeacherRefPersistence.disable(teacherId, classId);
//        return MapMessage.successMessage().setInfo("操作成功");
//    }

    /**
     * 删除组---删除假老师的情况
     */
    @RequestMapping(value = "deletegroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteGroup(@RequestParam Long teacherId, @RequestParam Long groupId) {
        boolean force = getRequestBool("force");

        GroupMapper group = groupLoaderClient.loadGroup(groupId, false);
        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(group.getClazzId());

        long realStudentCount = studentLoaderClient.loadGroupStudents(groupId)
                .stream()
                .filter(e -> !StringUtils.equals(e.fetchRealname(), UserConstants.EXPERIENCE_ACCOUNT_NAME))
                .count();

        Set<Long> sharedGroupIds = groupLoaderClient.loadSharedGroupIds(groupId);
        if (!force && CollectionUtils.isEmpty(sharedGroupIds) && realStudentCount > 0 && !clazz.isTerminalClazz()) {
            return MapMessage.errorMessage("老师在该班级未与其他老师关联，禁止删除该老师分组");
        }

        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (teacher.isJuniorTeacher() || teacher.isSeniorTeacher()) {
            //快乐学删除组时 虚拟的学生处理和真实学生的处理
            newKuailexueServiceClient.deleteKlxStudentsByTeacherAndClazz(teacherId, group.getClazzId());
        }

        // 删除老师与组关系
        MapMessage msg = clazzServiceClient.teacherExitSystemClazz(teacherId, group.getClazzId(), true, OperationSourceType.crm);
        if (msg.isSuccess()) {
            String operation = "删除老师的组[" + groupId + "]并移除了组内的学生";

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
            userServiceRecord.setOperationContent("删除班级");
            userServiceRecord.setComments(operation);
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }
        return msg;
    }

    /**
     * GROUP（组）转移功能 将group从一个clazz迁移到同校同年级的另一个clazz
     *
     * @return
     */
    @RequestMapping(value = "transfergrouptoclazz.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage transferGroup2Clazz() {
        Long groupId = getRequestLong("groupId");
        Long clazzId = getRequestLong("clazzId");
        String operationDesc = getRequestParameter("operationDesc", "");
        Boolean checked = getRequestBool("checked");
        if (groupId == 0 || clazzId == 0 || StringUtils.isBlank(operationDesc)) {
            return MapMessage.errorMessage("请填写完整信息");
        }
        if (!checked) { //为false时,说明要做检查
            Set<Long> sharedGroupIds = groupLoaderClient.loadSharedGroupIds(groupId);
            if (CollectionUtils.isNotEmpty(sharedGroupIds)) {
                MapMessage mapMessage = MapMessage.successMessage();
                Map<Long, Teacher> sharedTeacher = teacherLoaderClient.loadGroupSingleTeacher(sharedGroupIds);
                List<Map<String, String>> sharedTeacherInfo = new ArrayList<>();
                if (MapUtils.isNotEmpty(sharedTeacher)) {
                    sharedTeacher.entrySet().forEach(longTeacherEntry -> {
                        Map<String, String> map = new HashedMap<>();
                        map.put("groupId", longTeacherEntry.getKey().toString());
                        map.put("teacherName", longTeacherEntry.getValue().fetchRealname());
                        map.put("teacherId", longTeacherEntry.getValue().getId().toString());
                        sharedTeacherInfo.add(map);
                    });
                }
                mapMessage.add("sharedTeacherInfo", sharedTeacherInfo);
                return mapMessage;
            }
        }

        // 这里还是加个检查，发现有客服把老师的两个组都放到一个班级下的，防不胜防啊
        // 这里还有个关联组问题，暂时先不处理了
        GroupTeacherTuple gtr = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient()
                .findByGroupId(groupId)
                .stream()
                .filter(GroupTeacherTuple::isValidTrue)
                .min((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .orElse(null);
        if (gtr != null) {
            if (teacherLoaderClient.isTeachingClazz(gtr.getTeacherId(), clazzId)) {
                return MapMessage.errorMessage("关联老师在班级下已经有组了");
            }
        }

        MapMessage mapMessage = clazzServiceClient.transferGroup2Clazz(groupId, clazzId);
        List<Long> teacherIds = new ArrayList<>();
        if (mapMessage.isSuccess()) {
            Clazz oldClazz = (Clazz) mapMessage.get("oldClazz");
            Clazz newClazz = (Clazz) mapMessage.get("newClazz");
            Set<Long> groupIds = (Set<Long>) mapMessage.get("groupIds");
            Map<Long, List<Teacher>> longTeacherListMap = teacherLoaderClient.loadGroupTeacher(groupIds);
            for (Long tempGroupId : groupIds) {
                Long teacherId = null;
                if (CollectionUtils.isNotEmpty(longTeacherListMap.get(tempGroupId))) {
                    teacherId = longTeacherListMap.get(tempGroupId).get(0).getId();
                    teacherIds.add(teacherId);
                }
                ;
                crmTeacherSystemClazzService.updateBusinessForTeacherMoveClazz(teacherId, null, oldClazz.getId(), newClazz.getId(), tempGroupId);//保证数据一致
                String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "将" + oldClazz.formalizeClazzName() + "(clazzId:" + oldClazz.getId() + ",groupId:" + tempGroupId + ")转移到" + newClazz.formalizeClazzName() + "(clazzId:" + newClazz.getId() + ")";

                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(teacherId);
                userServiceRecord.setOperatorId("system");
                userServiceRecord.setOperationType(UserServiceRecordOperationType.转移分组.name());
                userServiceRecord.setOperationContent("转移分组");
                userServiceRecord.setComments(operation);
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }
        }
        mapMessage.add("teacherIds", teacherIds);
        return mapMessage;
    }


    /**
     * 换分组的班级
     * 这块的调用在页面上屏蔽,只是后台技术调用
     */
    @RequestMapping(value = "changegroupclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    @SuppressWarnings("unchecked")
    public MapMessage changeGroupClazz() {
        long teacherId = getRequestLong("teacherId");
        long groupId = getRequestLong("groupId");
        long schoolId = getRequestLong("schoolId");
        long clazzId = getRequestLong("clazzId");

        Set<Long> studentIds = raikouSDK.getClazzClient().getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByGroupIdIncludeDisabled(groupId)
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .map(GroupStudentTuple::getStudentId)
                .collect(Collectors.toSet());
        Map<Long, List<GroupStudentTuple>> grouped = raikouSDK.getClazzClient().getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByStudentIdsIncludeDisabled(studentIds)
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .collect(Collectors.groupingBy(GroupStudentTuple::getStudentId));

        for (Map.Entry<Long, List<GroupStudentTuple>> e : grouped.entrySet()) {
            if (e.getValue().size() > 1) {
                return MapMessage.errorMessage("分组里学生{}关联了多个group", e.getKey());
            }
        }

        MapMessage message = null;
        if (schoolId != 0) {
            message = teacherSystemClazzServiceClient.changeTeacherSchool(teacherId, Collections.singleton(groupId), schoolId, getCurrentAdminUser().getAdminUserName(), OperationSourceType.crm);
            if (message != null && message.isSuccess()) {
                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                if (teacher == null) {
                    return message;
                }
                // 更换班组关系成功,此时需要更新作业等clazz id
                // 即使中间有错误,也保证作业等数据一致
                List<TeacherSystemClazzService.TeacherGroupChangeRecord> records = (List<TeacherSystemClazzService.TeacherGroupChangeRecord>) message.remove("records");
                for (TeacherSystemClazzService.TeacherGroupChangeRecord record : records) {
                    crmTeacherSystemClazzService.updateBusinessForTeacherMoveClazz(teacherId, teacher.getSubject(),
                            record.getOldClazzId(), record.getNewClazzId(), record.getGroupId());
                }
            }
        } else if (clazzId != 0) {
            GroupMapper group = groupLoaderClient.loadGroup(groupId, false);
            message = teacherSystemClazzServiceClient.moveTeacherBetweenClazzs(teacherId, group.getClazzId(), clazzId);
            if (message != null && message.isSuccess()) {
                Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
                if (teacher == null) {
                    return message;
                }
                // 更换班组关系成功,此时需要更新作业等clazz id
                // 即使中间有错误,也保证作业等数据一致
                TeacherSystemClazzService.TeacherGroupChangeRecord record = (TeacherSystemClazzService.TeacherGroupChangeRecord) message.remove("record");
                crmTeacherSystemClazzService.updateBusinessForTeacherMoveClazz(teacherId, teacher.getSubject(),
                        record.getOldClazzId(), record.getNewClazzId(), record.getGroupId());
            }
        }

        return message;
    }

    /**
     * 任课历史
     */
    @RequestMapping(value = "changeteacherhistory.vpage", method = RequestMethod.GET)
    public String changeTeacherHistory(Model model) {
        long groupId = getRequestLong("groupId");
        if (groupId > 0) {
            model.addAttribute("changeTeacherHistoryList", crmTeacherService.getChangeTeacherHistoryList(groupId));
            model.addAttribute("groupId", groupId);
            return "crm/clazz/changeteacherhistory";
        } else {
            getAlertMessageManager().addMessageError(getRequestParameter("groupId", "") + "不是有效的组ID");
            return redirect("/crm/index.vpage");
        }
    }

    /**
     * ***********************班级操作*****************************************************************
     */
    @RequestMapping(value = "deletestudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteStudent(@RequestParam Long studentId,
                                    @RequestParam Long groupId,
                                    @RequestParam String deleteDesc) {
        deleteDesc = deleteDesc.replaceAll("\\s", "");
        if (StringUtils.isBlank(deleteDesc)) {
            return MapMessage.errorMessage("请填写备注");
        }

        try {
            MapMessage message = MapMessage.successMessage();
            // TODO removeManager fixed
            final Teacher teacher;
            List<GroupTeacherTuple> teacherRefList = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient()
                    .getGroupTeacherTupleService()
                    .dbFindByGroupIdIncludeDisabled(groupId)
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> !e.isDisabledTrue())
                    .collect(Collectors.toList());
            GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
            GroupTeacherTuple teacherRef = teacherRefList.size() > 0 ? teacherRefList.get(0) : null;

            if (teacherRef == null) {
                return MapMessage.successMessage();
            }

            Long teacherId = teacherRef.getTeacherId();
            Long clazzId = groupMapper.getClazzId();
            if (teacherId > 0) {
                teacher = teacherLoaderClient.loadTeacher(teacherId);
            } else {
                teacher = MiscUtils.firstElement(ClazzTeacher.toTeacherList(teacherLoaderClient.loadClazzTeachers(clazzId)));
            }
            //clazzId ===目前是groupStudentRef和clazzStudentRef同时存在的
            //所以这里要删两次
            Clazz c = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            MapMessage mm = teacherServiceClient.deleteClazzStudent(teacher, c, studentId, null);
            if (mm.isSuccess()) {
                Clazz clazz = (Clazz) mm.get("clazz");
                User student = (User) mm.get("student");
                String m = "{}老师将你移出了{}班，他/她可能觉得你不是这个班级的学生或者你已经有学号，如有疑问请直接联系老师！";
                m = StringUtils.formatMessage(m, teacher.fetchRealname(), clazz.formalizeClazzName());
                messageCommandServiceClient.getMessageCommandService().sendUserMessage(student.getId(), m);
            }


            message.add("studentId", studentId);

            String operation = "管理员" + getCurrentAdminUser().getAdminUserName() +
                    "删除班级学生：组ID：" + groupId + "，学生ID：" + studentId + "班级ID," + clazzId;

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
            userServiceRecord.setOperationContent("删除学生");
            userServiceRecord.setComments(operation + "；描述[" + deleteDesc + "]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            return message;
        } catch (Exception ex) {
            logger.error("Failed delete student from group, student={}, group={}", studentId, groupId, ex);
            return MapMessage.errorMessage("删除失败，请联系管理员");
        }
    }

    /**
     * *********************删除学生历史相关*****************************************************************
     */
    @RequestMapping(value = "deletedstudentlist.vpage", method = RequestMethod.GET)
    public String deleteStudentList(Model model) {

        long groupId = getRequestLong("groupId");
        if (groupId <= 0) {
            getAlertMessageManager().addMessageError("班级" + getRequestParameter("groupId", "") + "不存在");
            return redirect("crm/index.vpage");
        }
        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            getAlertMessageManager().addMessageError("班级" + getRequestParameter("groupId", "") + "不存在");
            return redirect("crm/index.vpage");
        }
//        String query = "SELECT csr.ID, csr.STUDENT_ID AS studentId, csr.UPDATE_DATETIME AS updateDatetime, uu.REALNAME AS studentName FROM VOX_GROUP_STUDENT_REF csr " +
//                "INNER JOIN UCT_USER uu ON uu.ID = csr.STUDENT_ID AND uu.DISABLED = 0 " +
//                "WHERE csr.CLAZZ_GROUP_ID = ? AND csr.DISABLED = 1 " +
//                "AND NOT EXISTS ( SELECT 1 FROM VOX_GROUP_STUDENT_REF tt WHERE tt.STUDENT_ID = csr.STUDENT_ID AND CLAZZ_GROUP_ID=? AND tt.DISABLED = 0 ) " +
//                "AND NOT EXISTS ( SELECT 1 FROM VOX_CLASS_STUDENT_REF tt WHERE tt.USER_ID = csr.STUDENT_ID AND CLAZZ_ID <> ? AND DISABLED=FALSE)" +
//                "GROUP BY csr.STUDENT_ID ORDER BY csr.UPDATE_DATETIME DESC ";
//        List<Map<String, Object>> deletedStudentList = utopiaSql.withSql(query).useParamsArgs(groupId, groupId, groupMapper.getClazzId()).queryAll();

//        List<GroupStudentRef>
        List<GroupStudentTuple> deletedStudentRefs = asyncStudentServiceClient.getAsyncStudentService().loadGroupsDeletedStudents2(Collections.singleton(groupId))
                .getUninterruptibly()
                .getOrDefault(groupId, Collections.emptyList());

        Map<Long, User> students = userLoaderClient.loadUsers(deletedStudentRefs.stream().map(GroupStudentTuple::getStudentId).collect(Collectors.toSet()));

        List<Map<String, Object>> deletedStudentList = new ArrayList<>();
        Set<Long> addedStudentIds = new HashSet<>();// 用于去除重复的student
        deletedStudentRefs.stream()
                .sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()))
                .forEach(r -> {
                    User user = students.get(r.getStudentId());
                    if (user != null && !addedStudentIds.contains(r.getStudentId())) {
                        deletedStudentList.add(MapUtils.m("id", r.getId(),
                                "studentId", r.getStudentId(),
                                "updateDatetime", r.getUpdateTime(),
                                "studentName", user.fetchRealname()));
                        addedStudentIds.add(r.getStudentId());
                    }
                });
        List<Long> teacherIds = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient().getGroupTeacherIds(groupId);
        Set<Teacher> teachers = new HashSet<>(teacherLoaderClient.loadTeachers(teacherIds).values());
        List<Map<String, Object>> deletedStudentResult = new LinkedList<>(deletedStudentList);
        if (CollectionUtils.isEmpty(teachers) || teachers.stream().anyMatch(Teacher::isKLXTeacher)) {
            Date date = DateUtils.addDays(new Date(), -30);
            List<GroupKlxStudentRef> groupKlxStudentRefs = asyncGroupServiceClient.getAsyncGroupService()
                    .loadDeletedGroupKlxStudentRefs(groupId)
                    .getUninterruptibly()
                    .stream()
                    .filter(ref -> ref.getUpdateDatetime().after(date))
                    .collect(Collectors.toList());

            Map<String, GroupKlxStudentRef> groupKlxStudentRefMap = new HashMap<>();
            for (GroupKlxStudentRef ref : groupKlxStudentRefs) {
                if (!groupKlxStudentRefMap.containsKey(ref.getKlxStudentId())) {
                    groupKlxStudentRefMap.put(ref.getKlxStudentId(), ref);
                }
            }

            Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(groupKlxStudentRefMap.keySet())
                    .values()
                    .stream()
                    .collect(Collectors.toMap(KlxStudent::getId, Function.identity()));

            Set<Long> klxRealStudent = new HashSet<>();
            List<Map<String, Object>> deletedKlxStudentList = new ArrayList<>();
            klxStudentMap.forEach((key, klxStudent) -> {
                Map<String, Object> map = new HashMap<>();
                map.put("refId", groupKlxStudentRefMap.get(key).getId());
                map.put("updateTime", groupKlxStudentRefMap.get(key).getUpdateDatetime());
                map.put("klxStudentId", klxStudent.getId());
                map.put("klxStudentName", klxStudent.getName());
                if (klxStudent.isRealStudent()) {
                    map.put("studentId", klxStudent.getA17id());
                    klxRealStudent.add(klxStudent.getA17id());
                }
                deletedKlxStudentList.add(map);
            });

            model.addAttribute("deletedKlxStudentList", deletedKlxStudentList);
            // 过滤掉被删除学生的记录
            deletedStudentResult = deletedStudentResult.stream()
                    .filter(stu -> !klxRealStudent.contains(SafeConverter.toLong(stu.get("studentId"))))
                    .collect(Collectors.toList());
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(groupMapper.getClazzId());

        model.addAttribute("clazzId", groupMapper.getClazzId());
        model.addAttribute("clazzName", clazz == null ? null : clazz.formalizeClazzName());
        model.addAttribute("groupId", groupId);
        model.addAttribute("deletedStudentList", deletedStudentResult);
        return "crm/clazz/deletedstudentlist";
    }

    @RequestMapping(value = "recoverdeletedstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recoverDeletedStudent(@RequestParam String recoverStudentDesc,
                                            @RequestParam Long groupId,
                                            @RequestParam Long clazzId) {

        if (StringUtils.isBlank(recoverStudentDesc))
            return MapMessage.errorMessage("请填写问题描述");

        String[] ids = getRequest().getParameterValues("groupStudentRefIds[]");
        String[] klxRefIds = getRequest().getParameterValues("groupKlxStudentRefIds[]");
        if (ids == null || ids.length == 0) {
            if (klxRefIds == null || klxRefIds.length == 0) {
                return MapMessage.errorMessage("请选择要恢复的学生");
            }
        }
        MapMessage message = MapMessage.successMessage();
        if (ids != null && ids.length > 0) {
            List<String> groupStudentRefIds = Arrays.asList(ids);
            if (CollectionUtils.isEmpty(groupStudentRefIds)) {
                return MapMessage.errorMessage("恢复学生失败");
            }

            boolean forceEnable = getRequestBool("forceEnable");

            message = groupServiceClient.enableGroupStudentsRefs2(groupStudentRefIds, forceEnable);
            if (!message.isSuccess()) {
                return message;
            }
        }
        if (klxRefIds != null && klxRefIds.length > 0) {
            Set<Long> groupKlxStudentRefIds = Arrays.stream(klxRefIds).map(Long::valueOf).collect(Collectors.toSet());
            message = newKuailexueServiceClient.recoverGroupKlxStudentRefs(groupId, groupKlxStudentRefIds);
            if (!message.isSuccess()) {
                return message;
            }
        }


        List<GroupTeacherTuple> teacherRefList = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient().findByGroupId(groupId);
        GroupTeacherTuple teacherRef = teacherRefList.size() > 0 ? teacherRefList.get(0) : null;
        Long teacherId;
        if (teacherRef != null) {
            teacherId = teacherRef.getTeacherId();
            String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "恢复学生账号";

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
            userServiceRecord.setOperationContent("恢复删除的学生");
            userServiceRecord.setComments(operation + "；描述[" + recoverStudentDesc + "]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }

        StringBuilder info = new StringBuilder("成功恢复学生。");
        if (message.containsKey("skip")) {
            @SuppressWarnings("unchecked")
            Map<String, Set<Long>> skipReasonStudentIds = (Map<String, Set<Long>>) message.get("skip");
            skipReasonStudentIds.forEach((reason, skipStudentIds) -> {
                if (CollectionUtils.isNotEmpty(skipStudentIds)) {
                    info.append("跳过学生：").append(StringUtils.join(skipStudentIds, ",")).append(";");
                    info.append("原因:").append(reason);
                }
            });
        }

        return message.setInfo(info.toString());
    }

    /**
     * *********************private method*****************************************************************
     */

    private Map<String, Object> getGroupInfo(Long groupId) {
        Map<String, Object> groupInfo = new HashMap<>();
        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, true);
        //组内的老师

        Set<Long> sharedGroupId = new HashSet<>(groupLoaderClient.loadSharedGroupIds(groupId));
        sharedGroupId.add(groupId);
        List<GroupTeacherTuple> groupTeacherRefList = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient().findByGroupIds(sharedGroupId);

        List<Map<String, Object>> teacherInfoList = new ArrayList<>();
        for (GroupTeacherTuple teacherRef : groupTeacherRefList) {
            Map<String, Object> teacherInfo = new HashMap<>();
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherRef.getTeacherId());
            if (teacher == null) return null;
            teacherInfo.put("teacherId", teacherRef.getTeacherId());
            teacherInfo.put("subject", (teacher.getSubject() == null) ? null : teacher.getSubject().getValue());
            teacherInfo.put("teacherName", (teacher.getProfile() == null) ? null : teacher.getProfile().getRealname());
            if (teacher.getSubject() != null) {
                List<Map<String, Object>> clazzBookInfoList = new ArrayList<>();
                List<NewClazzBookRef> newClazzBookRefs = newClazzBookLoaderClient.loadGroupBookRefs(Collections.singleton(groupId))
                        .subject(teacher.getSubject())
                        .toList()
                        .stream()
                        .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp())).collect(Collectors.toList());
                Map<String, NewBookProfile> bookMap = newContentLoaderClient.loadBooks(newClazzBookRefs != null ? newClazzBookRefs.stream().map(NewClazzBookRef::getBookId).collect(Collectors.toList()) : CollectionUtils.emptyCollection());
                if (newClazzBookRefs != null) {
                    for (NewClazzBookRef ebook : newClazzBookRefs) {
                        Map<String, Object> groupBookInfo = new HashMap<>();
                        groupBookInfo.put("bookId", ebook.getBookId());
                        NewBookProfile book = bookMap.get(ebook.getBookId());
                        groupBookInfo.put("bookName", book != null ? book.getName() : "");
                        groupBookInfo.put("compulsoryTextbook", ebook.getCompulsoryTextbook());
                        clazzBookInfoList.add(groupBookInfo);
                    }
                }
                teacherInfo.put("currentTeacher", Objects.equals(groupId, teacherRef.getGroupId()));
                teacherInfo.put("clazzBookInfoList", clazzBookInfoList);
            }
            teacherInfoList.add(teacherInfo);
        }
        Set<Long> teacherIds = groupTeacherRefList.stream().map(GroupTeacherTuple::getTeacherId).collect(Collectors.toSet());
        SchoolLevel schoolLevel = asyncTeacherServiceClient.getAsyncTeacherService().loadTeacherSchools(teacherIds)
                .getUninterruptibly()
                .values()
                .stream()
                .filter(Objects::nonNull)
                .map(school -> SchoolLevel.safeParse(school.getLevel()))
                .findAny().orElse(SchoolLevel.JUNIOR);

        // 学生添加 加入班级的时间 By Wyc 2016-06-06

        Map<Long, Date> stuTimeMap = raikouSDK.getClazzClient().getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByGroupIdIncludeDisabled(groupId)
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .collect(Collectors.toMap(
                        GroupStudentTuple::getStudentId, GroupStudentTuple::getCreateTime,
                        (u, v) -> u.after(v) ? u : v, LinkedHashMap::new)
                );


        Set<Long> studentIds = groupMapper.getStudents().stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
        // 数据接口参考wiki --> http://wiki.17zuoye.net/pages/viewpage.action?pageId=37394807
        Map<Long, Map<String, Object>> authStuMap = null;
        try {
            MapMessage authStuResult = stuAuthQueryService.loadStudentAuthInfoData(studentIds, groupMapper.getSubject().name(), schoolLevel.getLevel());
            authStuMap = (Map<Long, Map<String, Object>>) authStuResult.get("dataMap");
        } catch (Exception ex) {
            logger.error("Failed invoke athena stuAuthQueryService, please check it.", ex);
        }
        Map<Long, String> studentAuthDate = new HashMap<>();
        if (MapUtils.isNotEmpty(authStuMap)) {
            for (Long studentId : studentIds) {
                Map<String, Object> info = authStuMap.get(studentId);
                if (info == null) continue;
                String authDate = SafeConverter.toString(info.get("auth_date"));
                if (StringUtils.isBlank(authDate)) continue;
                studentAuthDate.put(studentId, authDate);
            }
        }

        // 组内的学生
        List<Map<String, Object>> studentList = groupMapper.getStudents()
                .stream()
                .sorted((o1, o2) -> {
                    String n1 = SafeConverter.toString(o1.getName());
                    String n2 = SafeConverter.toString(o2.getName());
                    return Collator.getInstance(Locale.CHINESE).compare(n1, n2);// 按拼音排序
                })
                .map(mapper -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", mapper.getId());
                    info.put("name", mapper.getName());
                    info.put("joinTime", stuTimeMap.get(mapper.getId()));
                    info.put("authTime", studentAuthDate.get(mapper.getId()));
                    return info;
                }).collect(Collectors.toList());
        // 组内的快乐学学生
        List<KlxStudent> klxStudents = newKuailexueLoaderClient.loadKlxGroupStudents(groupId)
                .stream()
                .sorted((o1, o2) -> {
                    String n1 = SafeConverter.toString(o1.getName());
                    String n2 = SafeConverter.toString(o2.getName());
                    return Collator.getInstance(Locale.CHINESE).compare(n1, n2);// 按拼音排序
                }).collect(Collectors.toList());
        List<Map<String, Object>> klxStudentList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(klxStudents)) {
            klxStudents.forEach(klxStudent -> {
                Map<String, Object> klxStudentInfo = new HashMap<>();
                klxStudentInfo.put("id", klxStudent.getId());
                klxStudentInfo.put("name", klxStudent.getName());
                klxStudentInfo.put("scanNumber", klxStudent.getScanNumber());
                klxStudentInfo.put("studentId", klxStudent.getA17id());
                klxStudentList.add(klxStudentInfo);
            });
            groupInfo.put("klxStudentList", klxStudentList);
        }

        groupInfo.put("groupId", groupId);
        groupInfo.put("groupSubject", groupMapper.getSubject().getValue());
        groupInfo.put("artScienceType", groupMapper.getArtScienceType());
        groupInfo.put("teacherInfoList", teacherInfoList);
        groupInfo.put("studentList", studentList);
        groupInfo.put("studentCount", studentList.size());
        groupInfo.put("groupType", groupMapper.getGroupType().name());

        return groupInfo;

    }

    /**
     * ***********************查询班级动态图片*****************************************************************
     */
    @RequestMapping(value = "photoManagment.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String photoMng(Model model) {
        String prePath = RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net";
        int pageNumber = getRequestInt("pageNumber", 1);
        long studentId = getRequestLong("studentId", 0);
        JournalPagination pagination = clazzJournalLoaderClient.getClazzJournalLoader().getClazzJournals(
                studentId, ClazzJournalType.STUDENT_UPLOAD_PHOTO, pageNumber - 1, 10);
        model.addAttribute("journalPage", pagination);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("prePath", prePath);
        return "crm/clazz/clazzphoto";
    }

    /**
     * ***********************清除班级动态图片*****************************************************************
     */
    @RequestMapping(value = "cleanjournalphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage cleanJournalPhoto(@RequestParam String journalIds) {
        Set<Long> realIds = StringUtils.toLongList(journalIds).stream().collect(Collectors.toSet());
        if (realIds.isEmpty()) {
            return MapMessage.errorMessage("参数错误");
        }
        try {
            realIds.forEach(clazzJournalManagerClient.getClazzJournalManager()::deleteClazzJournal);
            return MapMessage.successMessage("清除成功");
        } catch (Exception ex) {
            logger.error("清除班级动态照片异常", ex);
            return MapMessage.errorMessage("后台异常，请联系管理员");
        }
    }

    /**
     * ***********************修改年级*****************************************************************
     */
    @RequestMapping(value = "changeclazzlevel.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeClazzLevel(@RequestParam Long groupId,
                                       @RequestParam Integer clazzLevel,
                                       @RequestParam int oldClazzLevel) {
        try {
            ClazzLevel level = ClazzLevel.parse(clazzLevel);
            if (level == null) {
                return MapMessage.errorMessage("年级不正确");
            }
            MapMessage mapMessage = crmTeacherSystemClazzService.changeTeacherClazzLevel(groupId, level);
            if (!mapMessage.isSuccess()) {
                return mapMessage.setInfo("修改年级失败");
            }

            mapMessage.setInfo("修改年级成功");

            GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
            if (groupMapper != null) {
                Set<Long> sharedGroupIds = groupLoaderClient.loadSharedGroupIds(groupMapper.getId());
                if (CollectionUtils.isNotEmpty(sharedGroupIds)) {
                    Set<Long> allGroupIdSet = new HashSet<>(sharedGroupIds);
                    allGroupIdSet.add(groupMapper.getId());
                }
            }

            List<GroupTeacherTuple> teacherRefList = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient().findByGroupId(groupId);

            List<Long> teacherIds = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(teacherRefList)) {
                teacherIds.addAll(teacherRefList.stream().map(GroupTeacherTuple::getTeacherId).collect(Collectors.toList()));
            }

            for (Long teacherId : teacherIds) {
                String oldClazzLevelDesc = ClazzLevel.getDescription(oldClazzLevel);
                String newClazzLevelDesc = ClazzLevel.getDescription(clazzLevel);
                String operation = "修改老师年级,之前年级为:" + oldClazzLevelDesc + "修改之后为:" + newClazzLevelDesc;
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(teacherId);
                userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
                userServiceRecord.setOperationContent("修改老师所在组的年级");
                userServiceRecord.setComments(operation);
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }

            // 清理老师和学生的缓存
            asyncUserServiceClient.getAsyncUserService().evictUserCache(teacherIds).awaitUninterruptibly();
            Map<Long, List<User>> teacherStudents = studentLoaderClient.loadTeacherStudents(teacherIds);
            for (List<User> students : teacherStudents.values()) {
                Set<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(studentIds)) {
                    asyncUserServiceClient.getAsyncUserService().evictUserCache(studentIds).awaitUninterruptibly();
                }
            }

            return mapMessage;
        } catch (Exception ignore) {
            logger.error("修改年级异常{}", ignore.getMessage());
            return MapMessage.errorMessage("后台异常，请联系管理员");
        }
    }

    @RequestMapping(value = "changeartsicencetype.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeArtSicenceType() {
        Long groupId = getRequestLong("groupId");
        String artScienceTypeStr = getRequestString("artScienceType");
        if (groupId == 0L || StringUtils.isBlank(artScienceTypeStr)) {
            return MapMessage.errorMessage("参数不能为空");
        }

        ArtScienceType artScienceType = ArtScienceType.of(artScienceTypeStr);
        MapMessage mapMessage = groupServiceClient.updateGroupArtScienceType(groupId, artScienceType);

        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }

        return MapMessage.successMessage("修改成功");
    }

    @RequestMapping(value = "changeclazzedusystem.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeClazzEduSystem() {
        Long clazzId = getRequestLong("clazzId");
        String eduSystem = getRequestParameter("eduSystem", "");
        EduSystemType eduSystemType = EduSystemType.of(eduSystem);
        if (clazzId == 0 || eduSystemType == null) {
            return MapMessage.errorMessage("参数出错");
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzIncludeDisabled(clazzId);

        EduSystemType oldEduSystem = clazz == null ? null : clazz.getEduSystem();

        MapMessage message = clazzServiceClient.updateClazzEduSystem(clazzId, eduSystemType);

        if (message.isSuccess()) {
            newKuailexueServiceClient.changeKlxClazzEduSystem(clazzId, oldEduSystem);
        }
        return message;
    }

    @RequestMapping(value = "changeclazzname.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeClazzName() {
        Long clazzId = getRequestLong("clazzId");
        String clazzName = getRequestParameter("clazzName", "");
        String modifyTeacherNameDesc = getRequestParameter("modifyTeacherNameDesc", "").replaceAll("\\s", "");
        if (clazzId == 0 || StringUtils.isEmpty(clazzName) || StringUtils.isBlank(modifyTeacherNameDesc)) {
            return MapMessage.errorMessage("参数出错");
        }
        newClazzServiceClient.getNewClazzService().updateClazzName(clazzId, clazzName);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "transfergroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage transferGroup() {

        Long teacherId = getRequestLong("teacherId");
        Long clazzId = getRequestLong("clazzId");
        Long newTeacherId = getRequestLong("newTeacherId");
        String delegateClazzDesc = getRequestString("delegateClazzDesc");

        delegateClazzDesc = delegateClazzDesc.replaceAll("\\s", "");

        if (StringUtils.isBlank(delegateClazzDesc))
            return MapMessage.errorMessage("问题描述不能为空");

        Teacher oldTeacher = teacherLoaderClient.loadTeacher(teacherId);
        if (oldTeacher == null) {
            return MapMessage.errorMessage("{}老师不存在", teacherId);
        }

        Teacher newTeacher = teacherLoaderClient.loadTeacher(newTeacherId);
        if (newTeacher == null) {
            return MapMessage.errorMessage("{}老师不存在", newTeacherId);
        }

        if (!Objects.equals(oldTeacher.getSubject(), newTeacher.getSubject())) {
            return MapMessage.errorMessage("禁止异科老师转让");
        }

        // if two teachers are not in the same school, return

        AlpsFutureMap<Long, School> schoolFutures = AlpsFutureBuilder.<Long, School>newBuilder()
                .ids(Arrays.asList(teacherId, newTeacherId))
                .generator(id -> asyncTeacherServiceClient.getAsyncTeacherService()
                        .loadTeacherSchool(id))
                .buildMap();

        School oldTeacherSchool = schoolFutures.getUninterruptibly(teacherId);
        School newTeacherSchool = schoolFutures.getUninterruptibly(newTeacherId);

        if ((null == oldTeacherSchool) || (null == newTeacherSchool) || !oldTeacherSchool.getId().equals(newTeacherSchool.getId()))
            return MapMessage.errorMessage(teacherId + "老师与" + newTeacherId + "老师不在同一所学校");

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(clazzId);
        if (clazz == null) {
            return MapMessage.errorMessage("班级不存在");
        }

        if (!teacherLoaderClient.isTeachingClazz(oldTeacher.getId(), clazzId)) {
            GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(oldTeacher.getId(), clazzId, false);
            if (group == null) {
                return MapMessage.errorMessage("{}老师不在该班级中", teacherId);
            }
        }

        GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacherId, clazzId, false);
        if (group == null) {
            return MapMessage.errorMessage("获取{}老师组信息错误", teacherId);
        }

//        if (group.getGroupType() == GroupType.WALKING_GROUP) {
//            return MapMessage.errorMessage("无法转让教学班级");
//        }

        if (group.getSubject() != newTeacher.getSubject()) {
            return MapMessage.errorMessage("老师和班组学科不匹配");
        }

        // 这部分需要判断所有学生是否已关联同科老师
        Set<Long> studentIds = raikouSDK.getClazzClient().getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .findByGroupId(group.getId())
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .map(GroupStudentTuple::getStudentId)
                .collect(Collectors.toSet());
        Map<Long, List<GroupMapper>> studentGroups = groupLoaderClient.loadStudentGroups(studentIds, false);
        for (Map.Entry<Long, List<GroupMapper>> entry : studentGroups.entrySet()) {
            boolean ret = entry.getValue().stream()
                    .filter(e -> !Objects.equals(e.getId(), group.getId()))
                    .map(GroupMapper::getSubject)
                    .anyMatch(e -> e == newTeacher.getSubject());
            if (ret) {
                return MapMessage.errorMessage("学生" + entry.getKey() + "已关联了其他同科目老师");
            }
        }

        // 自动检查作业
        List<String> homeworkIds = newHomeworkLoaderClient.loadGroupHomeworks(group.getId(), group.getSubject()).originalLocationsAsList()
                .stream()
                .map(NewHomework.Location::getId)
                .collect(Collectors.toList());
        homeworkIds.forEach(id -> {
            newHomeworkServiceClient.checkHomework(oldTeacher, id, HomeworkSourceType.CRM);
        });


        // 新老师不在班级中，先加入班级
//        if (!teacherLoaderClient.isTeachingClazz(newTeacherId, clazzId)) {
//            MapMessage msg = clazzServiceClient.teacherJoinSystemClazzForce(newTeacherId, clazzId);
//            if (!msg.isSuccess()) {
//                return msg;
//            }
//        }

        MapMessage message = groupServiceClient.replaceTeacherGroupForTransfer(teacherId, newTeacherId, clazzId, getCurrentAdminUser().getAdminUserName(), UserOperatorType.CRM_ADMIN);

        if (message.isSuccess()) {
            //前老师退出班级
            clazzServiceClient.teacherExitSystemClazz(teacherId, clazzId, Boolean.FALSE, OperationSourceType.crm);

            //异科转让时，更新group学科
            if (group.getSubject() != newTeacher.getSubject()) {
                group.setSubject(newTeacher.getSubject());
                groupServiceClient.updateGroupSubject(group.getId(), newTeacher.getSubject());
            }

            //记录进线日志
            String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "转让" + teacherId + "老师的" + clazzId + "班级给" + newTeacherId + "老师";

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("转让班级");
            userServiceRecord.setComments(operation + "；说明[" + delegateClazzDesc + "]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            // 记录 UserServiceRecord
            userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(newTeacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("转让班级");
            userServiceRecord.setComments(operation + "；说明[" + delegateClazzDesc + "]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }

        return MapMessage.successMessage("转让成功");
    }

    @RequestMapping(value = "movestudentscheck.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage moveStudentsCheck(@RequestParam Long srcGroupId,
                                        @RequestParam String studentIds,
                                        @RequestParam String desc) {
        long targetGroupId = getRequestLong("targetGroupId");
        MapMessage mapMessage = doMoveStudentsCheck(srcGroupId, targetGroupId, studentIds, desc);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }

        return mapMessage;
    }

    /**
     * CRM分组（班级）管理页面->更换班级
     */
    @RequestMapping(value = "movestudents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage moveStudents(@RequestParam Long srcGroupId,
                                   @RequestParam String studentIds,
                                   @RequestParam String desc) {
        long targetGroupId = getRequestLong("targetGroupId");
        String scanNumber = getRequestString("scanNumber");
        String[] studetnIdArr = StringUtils.split(studentIds, ",");
        MapMessage mapMessage = doMoveStudentsCheck(srcGroupId, targetGroupId, studentIds, desc);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        boolean needScanNumber = (boolean) mapMessage.get("needScanNumber");
        if (needScanNumber) {
            if (StringUtils.isBlank(scanNumber)) {
                return MapMessage.errorMessage("请输入正确的填涂号");
            }
        }

        Teacher teacher = (Teacher) mapMessage.get("teacher");

        Set<Long> studentIdSet = new HashSet<>();
        for (String str : studetnIdArr) {
            studentIdSet.add(SafeConverter.toLong(str));
        }

        if (needScanNumber) {//关联阅卷机号
            //todo 校验输入的填涂号是否在group下未被使用,校验成功后关联
//            MapMessage checkMsg = kuailexueServiceClient.scanNumberInGroup(scanNumber, targetGroupId);
            MapMessage checkMsg = newKuailexueServiceClient.checkScanNumberInGroup(scanNumber, targetGroupId);
            if (!checkMsg.isSuccess()) {
                return checkMsg;
            }
            if (!SafeConverter.toBoolean(checkMsg.get("find"))) {
                return MapMessage.errorMessage("老师班级下不存在此阅卷机号");
            }
            Long tempStudentId = null;
            for (Long studentId : studentIdSet) {
                tempStudentId = studentId;
            }
            if (tempStudentId != null) {
//                checkMsg = kuailexueServiceClient.linkScanNumber(tempStudentId, scanNumber, targetGroupId);
                checkMsg = newKuailexueServiceClient.linkKlxStudentByScanNumber(targetGroupId, tempStudentId, scanNumber);
                if (!checkMsg.isSuccess()) {
                    return checkMsg;
                }
            }
        }

        MapMessage message = groupServiceClient.moveStudentsBetweenGroup(srcGroupId, targetGroupId, studentIdSet);

        if (message.isSuccess()) {
            // 记录 UserServiceRecord
            String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "更换" + srcGroupId + "分组下的学生[" + studentIds + "]到" + targetGroupId + ",描述[" + desc + "]";
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacher.getId());
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
            userServiceRecord.setOperationContent("学生换组");
            userServiceRecord.setComments(operation);
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            // 按照学生再记录一下吧，方便查询
            for (Long studentId : studentIdSet) {
                operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "更换学生分组，从" + srcGroupId + "到" + targetGroupId + ",描述[" + desc + "]";
                userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(studentId);
                userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
                userServiceRecord.setOperationContent("学生换组");
                userServiceRecord.setComments(operation);
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }

        }
        return MapMessage.successMessage();
    }

    /**
     * CRM分组（班级）管理页面->导入学生检查
     */
    @RequestMapping(value = "importStudentsTeacherCheck.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importStudentsTeacherCheck(@RequestParam Long groupId) {
        if (groupId == null) {
            return MapMessage.errorMessage("请输入分组id");
        }

        GroupMapper groupMapper = groupLoaderClient.loadGroup(groupId, false);
        if (groupMapper == null) {
            return MapMessage.errorMessage("分组不存在");
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(groupMapper.getClazzId());
        if (clazz == null) {
            return MapMessage.errorMessage("找不到分组班级");
        }

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(clazz.getSchoolId())
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("找不到分组学校");
        }

//        if (school.isJuniorSchool() || school.isSeniorSchool()) {
//            return MapMessage.errorMessage("暂不支持初高中导入，如有问题请联系产品同学");
//        }

        List<GroupTeacherTuple> groupTeacherRefs = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient().findByGroupId(groupId);
        if (CollectionUtils.isEmpty(groupTeacherRefs)) {
            return MapMessage.errorMessage("找不到分组老师");
        }

        // 老师id
        Long teacherId = groupTeacherRefs.get(0).getTeacherId();

        // 主副账号,检查主账号绑定手机号情况
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId != null) {
            teacherId = mainTeacherId;
        }

        // 检查老师是否绑定手机号
        String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(teacherId);
        if (StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("该老师未绑定手机号，无法执行此操作");
        }

        return MapMessage.successMessage();
    }

    /**
     * CRM分组（班级）管理页面->导入学生
     */
    @RequestMapping(value = "importstudents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage importStudents(@RequestParam Long groupId,
                                     @RequestParam String content,
                                     @RequestParam String desc) {
        if (groupId == null) {
            return MapMessage.errorMessage("请输入分组id");
        }

        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("学生列表为空");
        }

        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("描述不能为空");
        }

        List<GroupTeacherTuple> groupTeacherRefs = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient().findByGroupId(groupId);
        if (CollectionUtils.isEmpty(groupTeacherRefs)) {
            return MapMessage.errorMessage("找不到分组老师");
        }

        // 老师id
        Long teacherId = groupTeacherRefs.get(0).getTeacherId();

        // 导入学生
        MapMessage message = crmTeacherSystemClazzService.importStudents(teacherId, groupId, content);

        if (message.isSuccess()) {
            int successCount = SafeConverter.toInt(message.get("successCount"));
            if (successCount > 0) {
                String clazzName = SafeConverter.toString(message.get("clazzName"));

                // 主副账号支持
                Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
                if (mainTeacherId != null) {
                    teacherId = mainTeacherId;
                }

                // 发送成功短信
                String phone = sensitiveUserDataServiceClient.loadUserMobile(teacherId);
                if (phone != null && phone != null) {
                    String text = "【一起作业网】尊敬的老师您好，已为您名下" + clazzName + "转入" + successCount + "名学生，如有疑问，请联系一起作业客服400-160-1717。";
                    smsServiceClient.createSmsMessage(phone).content(text).type(SmsType.CRM_IMPORT_STUDENTS_TO_GROUP.name()).send();
                }
            }

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(teacherId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
            userServiceRecord.setOperationContent("管理员导入学生到分组");
            userServiceRecord.setComments("group[" + groupId + "],学生[" + content + "]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }

        return MapMessage.successMessage().add("result", message.get("failedResults"));
    }

    /**
     * 移除分组中的快乐学学生
     */
    @RequestMapping(value = "klxstudentexitgroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage klxStudentExitGroup() {
        Long groupId = getRequestLong("groupId");
        String klxStudentId = getRequestString("klxStudentId");
        String desc = getRequestString("desc");
        if (groupId == 0 || StringUtils.isBlank(klxStudentId) || StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("参数错误");
        }

        MapMessage mapMessage = newKuailexueServiceClient.deleteKlxStudent(groupId, klxStudentId);
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }

        // 日志记录 UserServiceRecord
        KlxStudent klxStudent = newKuailexueLoaderClient.loadKlxStudentsByIds(Collections.singleton(klxStudentId)).get(klxStudentId);
        String name = klxStudent == null ? "" : StringUtils.defaultString(klxStudent.getName());
        raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByGroupId(groupId)
                .forEach(groupTeacherRef -> {
                    String operation = "删除快乐学学生;管理员" + getCurrentAdminUser().getAdminUserName() + "从分组[" + groupId + "]删除快乐学学生[姓名:" + name + ";" + klxStudentId + "]";
                    UserServiceRecord userServiceRecord = new UserServiceRecord();
                    userServiceRecord.setUserId(groupTeacherRef.getTeacherId());
                    userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                    userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
                    userServiceRecord.setOperationContent("删除快乐学学生");
                    userServiceRecord.setComments(operation + "；说明[" + desc + "]");
                    userServiceClient.saveUserServiceRecord(userServiceRecord);
                });

        return mapMessage;
    }

    /**
     * 移除分组中的快乐学学生
     */
    @RequestMapping(value = "onlydeleteklxstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage onlyDeleteKlxStudent() {
        String desc = getRequestParameter("desc", "批量删除");
        Long schoolId = getRequestLong("schoolId");
        Long groupId = getRequestLong("groupId");
        Set<String> klxStudentIds = Stream.of(getRequestString("klxStudentIds").split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (schoolId == 0 || groupId == 0 || CollectionUtils.isEmpty(klxStudentIds)) {
            return MapMessage.errorMessage("参数错误");
        }

        School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("无效的学校信息");
        }

        Set<Long> groupIds = new HashSet<>(deprecatedGroupLoaderClient.loadSharedGroupIds(groupId));
        groupIds.add(groupId);

        Set<Long> refIds = asyncGroupServiceClient.getAsyncGroupService()
                .findGroupKlxStudentRefsByGroups(groupIds)
                .getUninterruptibly()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(ref -> klxStudentIds.contains(ref.getKlxStudentId()))
                .map(GroupKlxStudentRef::getId)
                .collect(Collectors.toSet());

        asyncGroupServiceClient.getAsyncGroupService()
                .disableGroupKlxStudentRefs(refIds)
                .getUninterruptibly();

        // 检查要不要清除填涂号
        Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(klxStudentIds);
        Map<String, List<GroupKlxStudentRef>> klxGroupRefMap = asyncGroupServiceClient.getAsyncGroupService()
                .findGroupKlxStudentRefsByStudents(klxStudentIds)
                .getUninterruptibly();

        StringBuilder builder = new StringBuilder("[");
        int count = 0;
        for (String klxStudentId : klxStudentIds) {
            KlxStudent klxStudent = klxStudentMap.get(klxStudentId);
            if (klxStudent == null || StringUtils.isBlank(klxStudent.getScanNumber())) {
                continue;
            }
            if (count++ <= 3) {
                builder.append("(姓名:").append(klxStudent.getName()).append(",").append(klxStudentId).append("); ");
            }
            // 如果没有班组关联了，那就可以清除填涂号了
            if (CollectionUtils.isEmpty(klxGroupRefMap.get(klxStudentId))) {
                newKuailexueServiceClient.removeScanNumberFromSchool(schoolId, klxStudent.getScanNumber());
            }
        }

        Set<Long> teacherIds = raikouSDK.getClazzClient()
                .getGroupTeacherTupleServiceClient()
                .findByGroupIds(groupIds)
                .stream()
                .map(GroupTeacherTuple::getTeacherId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(teacherIds)) {
            if (klxStudentIds.size() > 3) {
                builder.append("等共").append(klxStudentIds.size()).append("名学生");
            }
            builder.append("]");

            String studentInfo = builder.toString();
            teacherIds.forEach(teacherId -> {
                String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "把快乐学学生从分组" + groupId + "删除;学生信息是" + studentInfo;
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(teacherId);
                userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
                userServiceRecord.setOperationContent("删除快乐学学生");
                userServiceRecord.setComments(operation + "; 说明[" + desc + "]");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            });
        }

        return MapMessage.successMessage();
    }

    /**
     * 快乐学学生换班
     */
    @RequestMapping(value = "changeklxstudentsclazz.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeKlxStudentsClazz() {
        long srcGroupId = getRequestLong("srcGroupId");
        long targetGroupId = getRequestLong("targetGroupId");
        String desc = getRequestString("desc");
        String klxStudentIds = getRequestString("klxStudentIds");

        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("问题描述不能为空");
        }

        Set<String> klxStudentIdSet = Stream.of(klxStudentIds.split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(klxStudentIdSet)) {
            return MapMessage.errorMessage("没有选择要换班级的快乐学学生");
        }

        MapMessage mapMessage = newKuailexueServiceClient.moveKlxVirtualStudents(srcGroupId, targetGroupId, klxStudentIdSet);
        if (mapMessage.isSuccess()) {
            //日志记录
            Map<String, KlxStudent> klxStudentMap = newKuailexueLoaderClient.loadKlxStudentsByIds(klxStudentIdSet);
            Set<Long> teacherIds = new HashSet<>();
            List<Long> groupTeacherIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .getGroupTeacherIds(srcGroupId);
            if (CollectionUtils.isNotEmpty(groupTeacherIds)) {
                teacherIds.addAll(groupTeacherIds);
            }
            groupTeacherIds = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .getGroupTeacherIds(targetGroupId);
            if (CollectionUtils.isNotEmpty(groupTeacherIds)) {
                teacherIds.addAll(groupTeacherIds);
            }

            StringBuilder builder = new StringBuilder("[");
            for (String klxId : klxStudentIdSet) {
                KlxStudent klxStudent = klxStudentMap.get(klxId);
                String name = "";
                if (klxStudent != null) {
                    name = klxStudent.getName();
                }
                builder.append("(姓名:").append(name).append(",").append(klxId).append("); ");
            }
            builder.append("]");

            String studentInfo = builder.toString();
            teacherIds.forEach(teacherId -> {
                String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "把快乐学学生从" + srcGroupId + "分组换到分组" + targetGroupId + ";学生信息是" + studentInfo;
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(teacherId);
                userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.班组信息变更.name());
                userServiceRecord.setOperationContent("快乐学学生换组");
                userServiceRecord.setComments(operation + "; 说明[" + desc + "]");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            });
        }

        return mapMessage;
    }

    /**
     * 合并OTO单个学生
     */
    @RequestMapping(value = "mergeotosinglestudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mergeOtoSingleStudent() {
        Long groupId = getRequestLong("groupId");
        Long studentId = getRequestLong("studentId");
        String klxStudentId = getRequestString("klxStudentId");
        String desc = getRequestString("desc");
        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("备注不能为空");
        }

        if (StringUtils.isBlank(klxStudentId) || studentId == 0L) {
            return MapMessage.errorMessage("请选择有效的学生");
        }

        MapMessage retMsg;
        try {
            AtomicLockManager.getInstance().acquireLock("CRM:MergeKlxStudent:student:" + studentId);
            AtomicLockManager.getInstance().acquireLock("CRM:MergeKlxStudent:klx:" + klxStudentId);
            retMsg = newKuailexueServiceClient.mergeOTOSingleStudent(klxStudentId, studentId);
        } catch (CannotAcquireLockException cae) {
            retMsg = MapMessage.errorMessage("正在处理请勿重复操作");
        } catch (Exception ex) {
            retMsg = MapMessage.errorMessage("系统异常：" + ex.getMessage());
        } finally {
            AtomicLockManager.getInstance().releaseLock("CRM:MergeKlxStudent:student:" + studentId);
            AtomicLockManager.getInstance().releaseLock("CRM:MergeKlxStudent:klx:" + klxStudentId);
        }
        if (!retMsg.isSuccess()) {
            return retMsg;
        }

        //添加老师用户备注
        teacherLoaderClient.loadGroupTeacher(groupId).forEach(teacher -> {
            String tempOperation = "合并快乐学学生" + klxStudentId + "到学生" + studentId;
            UserServiceRecord tempServiceRecord = new UserServiceRecord();
            tempServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            tempServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            tempServiceRecord.setUserId(teacher.getId());
            tempServiceRecord.setOperationContent("合并OTO学生");
            tempServiceRecord.setComments(tempOperation + "；说明[" + desc + "]");
            userServiceClient.saveUserServiceRecord(tempServiceRecord);

        });
        //添加学生用户备注

        String operation = "合并快乐学学生" + klxStudentId + "到学生" + studentId;
        UserServiceRecord serviceRecord = new UserServiceRecord();
        serviceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        serviceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
        serviceRecord.setUserId(studentId);
        serviceRecord.setOperationContent("合并OTO学生");
        serviceRecord.setComments(operation + "；说明[" + desc + "]");
        userServiceClient.saveUserServiceRecord(serviceRecord);

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "mergeotogroupstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mergeOtoGroupStudent() {
        Long sourceGroupId = getRequestLong("sourceGroupId");
        Long targetGroupId = getRequestLong("targetGroupId");
        String mode = getRequestString("mode");
        String desc = getRequestString("desc");

        if (sourceGroupId == 0L || targetGroupId == 0L) {
            return MapMessage.errorMessage("请选择有效的班组");
        }

        if (StringUtils.isBlank(mode)) {
            return MapMessage.errorMessage("请选择合并模式");
        }

        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("备注不能为空");
        }
        try {
            MapMessage resultMsg = newKuailexueServiceClient.mergeKlxGroupStudent(sourceGroupId, targetGroupId, mode);
            if (resultMsg.isSuccess()) {
                String operation = StringUtils.formatMessage("选择班组:{}, 目标班组:{}, 模式:{}", sourceGroupId, targetGroupId, mode);
                addAdminLog("MergeOtoGroup", null, null, desc, operation);
            }
            return resultMsg;
        } catch (Exception ex) {
            return MapMessage.errorMessage("系统异常, 请联系管理员：" + StringUtils.firstLine(ex.getMessage()));
        }
    }

    /**
     * 同步当前组下的User和KlxStudent数据关系
     */
    @RequestMapping(value = "syncotogroupstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage syncOTOGroupStudent() {
        Long groupId = getRequestLong("groupId");

        GroupMapper group = deprecatedGroupLoaderClient.loadGroup(groupId, false);

        if (group == null || group.getClazzId() == null) {
            return MapMessage.errorMessage("无效的班级信息");
        }

        Clazz clazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(group.getClazzId());
        if (clazz == null || clazz.getEduSystem() == null || clazz.getEduSystem().getKtwelve() == null) {
            return MapMessage.errorMessage("未知的班级信息");
        }

//        Ktwelve ktwelve = clazz.getEduSystem().getKtwelve();
//        if (Ktwelve.JUNIOR_SCHOOL != ktwelve) {
//            return MapMessage.errorMessage("只允许初中班组操作");
//        }

        try {
            return newKuailexueServiceClient.syncKlxGroupStudent(groupId);
        } catch (Exception ex) {
            logger.error("Failed sync user to o2o student group, groupId={}", groupId, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    /**
     * 将这个组的学生和其他共享组的 KlxStudent 同步
     */
    @RequestMapping(value = "syncsharegroupstudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage syncShareGroupStudent() {
        Long groupId = getRequestLong("groupId");
        try {
            return newKuailexueServiceClient.syncSharedGroupStudent(groupId);
        } catch (Exception ex) {
            logger.error("Failed sync shared o2o group klxStudent, groupId={}", groupId, ex);
            return MapMessage.errorMessage("同步失败");
        }
    }

    private List<KeyValuePair<Integer, String>> getClazzLevels(Ktwelve ktwelve) {
        List<KeyValuePair<Integer, String>> paris = new ArrayList<>();
        if (ktwelve == Ktwelve.PRIMARY_SCHOOL) {
            for (ClazzLevel level : ClazzLevel.values()) {
                if (level.getLevel() <= 6) {
                    paris.add(new KeyValuePair<>(level.getLevel(), level.getDescription()));
                }
            }
            // 毕业支持
            paris.add(new KeyValuePair<>(ClazzLevel.PRIMARY_GRADUATED.getLevel(), ClazzLevel.PRIMARY_GRADUATED.getDescription()));
        } else if (ktwelve == Ktwelve.JUNIOR_SCHOOL) {
            for (ClazzLevel level : ClazzLevel.values()) {
                if ((level.getLevel() >= 6 && level.getLevel() <= 9)) {
                    paris.add(new KeyValuePair<>(level.getLevel(), level.getDescription()));
                }
            }
            // 毕业支持
            paris.add(new KeyValuePair<>(ClazzLevel.MIDDLE_GRADUATED.getLevel(), ClazzLevel.MIDDLE_GRADUATED.getDescription()));
        } else if (ktwelve == Ktwelve.INFANT) {
            for (ClazzLevel level : ClazzLevel.values()) {
                if ((level.getLevel() >= ClazzLevel.INFANT_FIRST.getLevel() && level.getLevel() <= ClazzLevel.INFANT_FOURTH.getLevel())) {
                    paris.add(new KeyValuePair<>(level.getLevel(), level.getDescription()));
                }
            }
            // 毕业支持
            paris.add(new KeyValuePair<>(ClazzLevel.INFANT_GRADUATED.getLevel(), ClazzLevel.INFANT_GRADUATED.getDescription()));
        }
        return paris;
    }

    private MapMessage doMoveStudentsCheck(Long srcGroupId, Long targetGroupId, String studentIds, String desc) {
        if (srcGroupId == null) {
            return MapMessage.errorMessage("请输入学生转出的分组id");
        }

        if (targetGroupId == 0) {
            return MapMessage.errorMessage("请输入学生要进入的分组id");
        }

        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("请输入描述");
        }

        String[] studetnIdArr = StringUtils.split(studentIds, ",");
        if (studetnIdArr.length == 0) {
            return MapMessage.errorMessage("没有学生需要更换班级");
        }

        GroupMapper srcGroup = groupLoaderClient.loadGroup(srcGroupId, false);
        if (srcGroup == null) {
            return MapMessage.errorMessage("没有找到该分组{}", srcGroupId);
        }

        Clazz srcClazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(srcGroup.getClazzId());
        if (srcClazz == null) {
            return MapMessage.errorMessage("找不到班级{}", srcGroup.getClazzId());
        }

        GroupMapper targetGroup = groupLoaderClient.loadGroup(targetGroupId, false);
        if (targetGroup == null) {
            return MapMessage.errorMessage("没有找到该分组{}", targetGroupId);
        }

        Clazz targetClazz = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazz(targetGroup.getClazzId());
        if (targetClazz == null) {
            return MapMessage.errorMessage("找不到班级{}", targetGroup.getClazzId());
        }

        List<Teacher> teachers = teacherLoaderClient.loadGroupTeacher(targetGroupId);
        if (CollectionUtils.isEmpty(teachers)) {
            return MapMessage.errorMessage("该分组没有老师");
        }
        School srcSchool = schoolLoaderClient.getSchoolLoader()
                .loadSchool(srcClazz.getSchoolId())
                .getUninterruptibly();
        School targetSchool = schoolLoaderClient.getSchoolLoader()
                .loadSchool(targetClazz.getSchoolId())
                .getUninterruptibly();
        //异校 并且 targetgroup下的老师是初中数理化生/高中老师
        boolean needScanNumber = false;
        if (srcSchool != null && targetSchool != null && !Objects.equals(srcSchool.getId(), targetSchool.getId())) {
            for (Teacher tempTeacher : teachers) {
                if (tempTeacher.isKLXTeacher() && !tempTeacher.isMathTeacher()) { //要求
                    needScanNumber = true;
                    break;
                }
            }
        }

        return MapMessage.successMessage()
                .add("teacher", teachers.get(0))
                .add("needScanNumber", needScanNumber);
    }

}
