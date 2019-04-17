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

package com.voxlearning.utopia.admin.controller.audit;

import com.alibaba.fastjson.JSONArray;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.tag.UserTagService;
import com.voxlearning.athena.bean.tag.TagTree;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.crm.api.bean.JPushTag;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.crm.consumer.loader.crm.CrmAppPushLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmAppPushServiceClient;
import com.voxlearning.utopia.service.crm.tools.AppPushWorkFlowUtils;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.mizar.consumer.service.OfficialAccountsServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.JpushUserTag;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowContext;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/audit/apppush")
public class AppPushWorkflowController extends CrmAbstractController {

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private CrmAppPushLoaderClient crmAppPushLoaderClient;
    @Inject private CrmAppPushServiceClient crmAppPushServiceClient;
    @Inject private OfficialAccountsServiceClient officialAccountsServiceClient;
    @Inject private UserBlacklistServiceClient userBlacklistServiceClient;
    @Inject private WorkFlowDataServiceClient workFlowDataServiceClient;
    @Inject private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject private WorkFlowServiceClient workFlowServiceClient;

    @ImportService(interfaceClass = UserTagService.class)
    private UserTagService userTagService; // 大数据标签接口...


    private Map<String, Object> convertTagTree(TagTree tagTree) {
        Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("key", tagTree.getTagId());
        nodeData.put("title", tagTree.getTagName());
        if (CollectionUtils.isNotEmpty(tagTree.getChildTagTree())) {
            List<Map<String, Object>> children = new ArrayList<>();
            tagTree.getChildTagTree().forEach(p -> children.add(convertTagTree(p)));
            nodeData.put("children", children);
            nodeData.put("checkbox", false);
            nodeData.put("unselectableIgnore", true);
            nodeData.put("unselectable", true);
            nodeData.put("folder", true);
        } else {
            nodeData.put("checkbox", true);
        }

        return nodeData;
    }

    @RequestMapping(value = "apppushapply.vpage", method = RequestMethod.GET)
    public String appPushApply(Model model) {
        Long recordId = getRequestLong("id");
        AppPushWfMessage pushWfMessage = crmAppPushLoaderClient.findByRecord(recordId);
        if (pushWfMessage == null) {
            model.addAttribute("targetRegion", JsonUtils.toJson(crmRegionService.buildRegionTree(null)));
            List<TagTree> treeList = userTagService.getTagTree();
            List<Map<String, Object>> tagTree = new ArrayList<>();
            for (TagTree tree : treeList) {
                tagTree.add(convertTagTree(tree));
            }
            model.addAttribute("tagTree", JsonUtils.toJson(tagTree));
            return "audit/apppush/apppushedit";
        }

        model.addAttribute("pushMsg", pushWfMessage);
        model.addAttribute("prePath", getMainHostBaseUrl() + "/gridfs/");

        List<String> regions = raikouSystem.getRegionBuffer().loadRegions(pushWfMessage.getTargetRegion()).values()
                .stream()
                .map(region -> String.format("%-10s( %d )", region.getName(), region.getCode()))
                .collect(Collectors.toList());
        model.addAttribute("targetRegion", regions);


        List<String> schools = schoolLoaderClient.getSchoolLoader()
                .loadSchools(pushWfMessage.getTargetSchool()).getUninterruptibly()
                .values().stream()
                .map(school -> String.format("%s ( %d )", school.getCmainName(), school.getId()))
                .collect(Collectors.toList());
        model.addAttribute("targetSchool", schools);

        List<String> targetTagNameGroups = new ArrayList<>();
        List<List<Map<String, Object>>> targetLabelGroups = pushWfMessage.getTargetTagGroups();
        if (CollectionUtils.isNotEmpty(targetLabelGroups)) {
            targetLabelGroups.forEach(p -> {
                if (CollectionUtils.isNotEmpty(p)) {
                    List<String> tagNameList = p.stream().filter(MapUtils::isNotEmpty).map(k -> SafeConverter.toString(k.get("tagName"), "")).collect(Collectors.toList());
                    targetTagNameGroups.add(StringUtils.join(tagNameList, ","));
                }
            });
        }
        model.addAttribute("targetTagNameGroups", targetTagNameGroups);
        model.addAttribute("workflowRecord", workFlowLoaderClient.loadWorkFlowRecord(recordId));

        // 是否可以审核
        boolean audit = (StringUtils.equals("lv1", pushWfMessage.getStatus())
                || StringUtils.equals("lv2", pushWfMessage.getStatus()));

        audit &= Objects.isNull(getRequestParameter("ct", null));
        model.addAttribute("audit", audit);
        List<WorkFlowProcessHistory> histories = workFlowLoaderClient.loadWorkFlowProcessHistoryByWorkFlowId(recordId)
                .stream()
                .sorted(Comparator.comparing(WorkFlowProcessHistory::getUpdateDatetime).reversed())
                .collect(Collectors.toList());
        model.addAttribute("histories", histories);

        // tag
        if (pushWfMessage.canSendPush()) {
            model.addAttribute("jpushTag", JsonUtils.toJsonPretty(crmAppPushLoaderClient.generateTag(pushWfMessage)));
        }
        return "audit/apppush/apppushview";
    }

    @RequestMapping(value = "checktag.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage checkTag() {
        try {
            AppPushWfMessage appPushMsg = requestAppPushMessage();
            // 校验
            MapMessage validMsg = AppPushWorkFlowUtils.validateMessage(appPushMsg);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            JPushTag jPushTag = crmAppPushLoaderClient.generateTag(appPushMsg);
            Set<String> userTag = new HashSet<>();
            Long userId = getRequestLong("userId");
            if (userId != 0L) {
                userTag = getUserMessageTagList(userId);
            }
            return MapMessage.successMessage()
                    .add("match", jPushTag.match(userTag))
                    .add("jpushTag", JsonUtils.toJsonPretty(jPushTag))
                    .add("userTag", JsonUtils.toJsonPretty(userTag));
        } catch (Exception ex) {
            logger.error("Failed calculate tag of Jpush", ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    // 新建App push消息
    @RequestMapping(value = "createapppush.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createAppPush() {
        try {
            AppPushWfMessage appPushMsg = requestAppPushMessage();
            // 校验
            MapMessage validMsg = AppPushWorkFlowUtils.validateMessage(appPushMsg);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            // 校验结束

            AuthCurrentAdminUser adminUser = getCurrentAdminUser();
            // 保存一条记录
            WorkFlowRecord workFlowRecord = new WorkFlowRecord();
            workFlowRecord.setStatus("init");
            workFlowRecord.setSourceApp("admin");
            workFlowRecord.setTaskName("发送AppPush消息");
            workFlowRecord.setTaskContent(AppPushWorkFlowUtils.generateWorkFlowContent(appPushMsg));
            workFlowRecord.setTaskDetailUrl(appPushMsg.getLink()); // 查看详情
            workFlowRecord.setLatestProcessorName(adminUser.getRealName());
            workFlowRecord.setCreatorName(adminUser.getRealName());
            workFlowRecord.setCreatorAccount(adminUser.getAdminUserName());
            workFlowRecord.setWorkFlowType(WorkFlowType.ADMIN_SEND_APP_PUSH);
            MapMessage mapMessage = workFlowDataServiceClient.addWorkFlowRecord(workFlowRecord);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }

            workFlowRecord = (WorkFlowRecord) mapMessage.get("workFlowRecord");
            appPushMsg.setRecordId(workFlowRecord.getId());
            appPushMsg.setStatus(workFlowRecord.getStatus());
            crmAppPushServiceClient.insert(appPushMsg);

            WorkFlowContext workFlowContext = new WorkFlowContext();
            workFlowContext.setWorkFlowRecord(workFlowRecord);
            workFlowContext.setWorkFlowName(WorkFlowType.ADMIN_SEND_APP_PUSH.getWorkflowName());
            workFlowContext.setSourceApp("admin");
            workFlowContext.setProcessNotes("创建AppPush消息");
            workFlowContext.setProcessorAccount(adminUser.getAdminUserName());
            workFlowContext.setProcessorName(adminUser.getAdminUserName());
            return workFlowServiceClient.agree(workFlowContext);
        } catch (Exception ex) {
            logger.error("Failed to create App Push Message", ex);
            return MapMessage.errorMessage("提交失败");
        }
    }

    // 直接发出去了，都不用再存一道了， 快速推送只能根据userId推送
    @RequestMapping(value = "fastpush.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage fastPush() {
        AppPushWfMessage appPushMsg = requestAppPushMessage();
        // 校验
        MapMessage validMsg = AppPushWorkFlowUtils.validateFast(appPushMsg);
        if (!validMsg.isSuccess()) {
            return validMsg;
        }
        if (appPushMsg.getSendTime() == null) {
            appPushMsg.setSendTime(new Date());
        }
        appPushMsg.setFast(true);
        appPushMsg.setFileUrl(""); // 不接受上传的文件
        // 还是存一条记录好了。。走工作流而已
        crmAppPushServiceClient.insert(appPushMsg);
        return crmAppPushServiceClient.publish(appPushMsg);
    }

    @RequestMapping(value = "uploadexcel.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadSource(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return MapMessage.errorMessage("请选择上传的文件！");
        }
        int rowIndex = 0;
        StringBuilder errorMsg = new StringBuilder();
        Set<Long> keySet = new HashSet<>();
        try {
            String suffix = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            if (!"xls".equals(suffix) && !"xlsx".equals(suffix)) {
                return MapMessage.errorMessage("无效的文件");
            }

            // 再简单校验一下内容
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getLastRowNum() > 20000) {
                return MapMessage.errorMessage("单个文件请控制在2W行以内");
            }

            while (true) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    break;
                }
                Long uid = getCellValueLong(row.getCell(0));
                if (uid == null) break;
                if (keySet.contains(uid)) {
                    errorMsg.append("第").append(rowIndex + 1).append("行，重复的用户ID").append("\n");
                } else {
                    keySet.add(uid);
                }
                rowIndex++;
            }

            if (keySet.isEmpty()) {
                return MapMessage.errorMessage("请认真填写Excel里的内容");
            }

            if (errorMsg.length() > 0) {
                return MapMessage.errorMessage(errorMsg.toString());
            }

            String fileName = AdminOssManageUtils.upload(file, "apppush");
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("文件保存失败！");
            }
            return MapMessage.successMessage().add("fileUrl", fileName);
        } catch (
                Exception ex) {
            logger.error("Failed to excel of wechat user info @Row={} ", rowIndex, ex);
            return MapMessage.errorMessage("上传文件失败：" + ex.getMessage());
        }

    }

    // 处理App push消息
    @RequestMapping(value = "checkapppushmsg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkAppPushMsg() {
        //workFlowRecordId  wechatWfMessageId 处理类型 几审
        Long workFlowRecordId = getRequestLong("workFlowRecordId", 0L);
        String operationType = getRequestString("operationType");
        String isTopState = getRequestString("isTopState");
        String topEndTime = getRequestString("topEndTime");

        if (workFlowRecordId == 0L || StringUtils.isBlank(operationType)) {
            return MapMessage.errorMessage("参数错误");
        }
        List<WorkFlowRecord> workFlowRecordList = new ArrayList<>(
                workFlowLoaderClient.loadWorkFlowRecords(Collections.singleton(workFlowRecordId)).values()
        );
        if (CollectionUtils.isEmpty(workFlowRecordList)) {
            return MapMessage.errorMessage("WorkFlowRecord:" + workFlowRecordId + "不存在");
        }

        AppPushWfMessage pushWfMessage = crmAppPushLoaderClient.findByRecord(workFlowRecordId);
        pushWfMessage.setIsTop(SafeConverter.toBoolean(isTopState));
        pushWfMessage.setTopEndTimeStr(topEndTime);
        crmAppPushServiceClient.updateIsTopStatus(pushWfMessage.getId(), SafeConverter.toBoolean(isTopState), topEndTime);

        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        WorkFlowRecord workFlowRecord = workFlowRecordList.get(0);
        WorkFlowContext workFlowContext = new WorkFlowContext();
        workFlowContext.setWorkFlowRecord(workFlowRecord);
        workFlowContext.setWorkFlowName(WorkFlowType.ADMIN_SEND_APP_PUSH.getWorkflowName());
        workFlowContext.setSourceApp("admin");
        workFlowContext.setProcessorAccount(adminUser.getAdminUserName());
        workFlowContext.setProcessorName(adminUser.getAdminUserName());

        MapMessage mapMessage;
        if (Objects.equals(operationType, "send")) {//直接发送
            workFlowContext.setProcessNotes("同意发送AppPush消息");
            mapMessage = workFlowServiceClient.agree(workFlowContext);
        } else if (Objects.equals(operationType, "reject")) {//拒绝
            workFlowContext.setProcessNotes("驳回AppPush消息申请");
            mapMessage = workFlowServiceClient.reject(workFlowContext);
        } else if (Objects.equals(operationType, "raiseup")) {//转给上一级
            workFlowContext.setProcessNotes("AppPush消息转上级审核");
            mapMessage = workFlowServiceClient.raiseup(workFlowContext);
        } else {
            return MapMessage.errorMessage("操作类型" + operationType + "不存在");
        }

        return mapMessage;
    }

    private AppPushWfMessage requestAppPushMessage() {
        // 赋值
        AppPushWfMessage appPushMsg = new AppPushWfMessage();
        requestFillEntity(appPushMsg);

        // 获取投放地区
        String regions = getRequestString("regionList");
        List<Integer> regionIds = Arrays.stream(regions.split(","))
                .map(SafeConverter::toInt)
                .filter(t -> t > 0)
                .distinct()
                .collect(Collectors.toList());
        appPushMsg.setTargetRegion(regionIds);
        // 获取投放用户

        UserType userType = appPushMsg.fetchTargetUserType();
        if (userType == null) {
            return null;
        }
        String users = getRequestString("userList");
        List<Long> userIds = Arrays.stream(users.split("\n"))
                .map(t -> t.replaceAll("\\s", ""))
                .filter(p -> p.startsWith(String.valueOf(userType.getType())))
                .filter(StringUtils::isNotBlank)
                .map(SafeConverter::toLong)
                .filter(t -> t > 0L)
                .distinct()
                .collect(Collectors.toList());
        appPushMsg.setTargetUser(userIds);

        // 获取投放学校
        String schools = getRequestString("schoolList");
        List<Long> schoolIds = Arrays.stream(schools.split("\n"))
                .map(t -> t.replaceAll("\\s", ""))
                .filter(StringUtils::isNotBlank)
                .map(SafeConverter::toLong)
                .filter(t -> t > 0L)
                .distinct()
                .collect(Collectors.toList());
        appPushMsg.setTargetSchool(schoolIds);
        appPushMsg.setSendStatus("waiting");

        String labelGroups = getRequestString("tagGroups");
        List<List<Map<String, Object>>> targetTagGroups = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(labelGroups);
        for (Object obj : jsonArray) {
            List<Map<String, Object>> itemList = new ArrayList<>();
            JSONArray subArray = (JSONArray) obj;
            for (Object subObj : subArray) {
                Map<String, Object> map = (Map<String, Object>) subObj;
                if (MapUtils.isNotEmpty(map) && map.containsKey("tagId")) {
                    itemList.add(map);
                }
            }
            if (CollectionUtils.isNotEmpty(itemList)) {
                targetTagGroups.add(itemList);
            }
        }
        appPushMsg.setTargetTagGroups(targetTagGroups);

        return appPushMsg;
    }

    /**
     * COPY FORM AbstractApiController.getUserMessageTagList()
     */
    private Set<String> getUserMessageTagList(Long userId) {
        Set<String> tagSet = new HashSet<>();
        User user = userLoaderClient.loadUser(userId);
        if (user == null) {
            return tagSet;
        }
        if (UserType.TEACHER == user.fetchUserType()) {
            //包班制老师 用主账号生成tag
            Long mainId = teacherLoaderClient.loadMainTeacherId(userId);
            if (mainId == null) {
                mainId = userId;
            }
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(mainId);
            return generateTeacherTag(teacherDetail, tagSet);
        }
        if (UserType.STUDENT == user.fetchUserType()) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            generateStudentTag(studentDetail, tagSet);
            //学生黑名单的恶心tag
            if (studentDetail.isInPaymentBlackListRegion()) {
                tagSet.add(JpushUserTag.PAYMENT_BLACK_LIST.tag);
            }
            if (!studentDetail.isInPaymentBlackListRegion()) {
                tagSet.add(JpushUserTag.NON_ANY_BLACK_LIST.tag);
            }
            return tagSet;
        }
        if (UserType.PARENT == user.fetchUserType()) {
            Set<Long> studentIds = studentLoaderClient.loadParentStudents(userId).stream().map(User::getId).collect(Collectors.toSet());
            Collection<StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds).values();
            studentDetails.forEach(p -> generateStudentTag(p, tagSet));
            tagSet.add(JpushUserTag.USER_ALL_ONLY_FOR_PARENT.tag);
            //家长的黑名单取全部孩子的区域
            if (userBlacklistServiceClient.isInBlackListByParent(user, new ArrayList<>(studentDetails))) {
                tagSet.add(JpushUserTag.PAYMENT_BLACK_LIST.tag);
            }
            //家长的孩子全部不在任何黑名单时。打下面这个tab
            if (!tagSet.contains(JpushUserTag.PAYMENT_BLACK_LIST.tag)) {
                tagSet.add(JpushUserTag.NON_ANY_BLACK_LIST.tag);
            }
            // 家长关注的或者系统自动关注的公众号tag
            List<OfficialAccounts> accountsList = officialAccountsServiceClient.loadUserOfficialAccounts(user.getId());
            if (CollectionUtils.isNotEmpty(accountsList)) {
                for (OfficialAccounts accounts : accountsList) {
                    tagSet.add(JpushUserTag.OFFICIAL_ACCOUNT_FOLLOW.generateTag(accounts.getAccountsKey()));
                }
            }
        }
        return tagSet;
    }

    //生成学生tag
    private Set<String> generateStudentTag(StudentDetail studentDetail, Set<String> tagSet) {
        if (studentDetail == null) {
            return tagSet;
        }
        if (studentDetail.getRootRegionCode() != null) {
            tagSet.add(JpushUserTag.PROVINCE.generateTag(studentDetail.getRootRegionCode().toString()));
        }
        if (studentDetail.getCityCode() != null) {
            tagSet.add(JpushUserTag.CITY.generateTag(studentDetail.getCityCode().toString()));
        }
        if (studentDetail.getStudentSchoolRegionCode() != null) {
            tagSet.add(JpushUserTag.COUNTY.generateTag(studentDetail.getStudentSchoolRegionCode().toString()));
        }
        if (studentDetail.getClazzLevelAsInteger() != null) {
            tagSet.add(JpushUserTag.CLAZZ_LEVEL.generateTag(studentDetail.getClazzLevelAsInteger().toString()));
        }
        if (studentDetail.getClazz() != null) {
            if (studentDetail.getClazz().getSchoolId() != null) {
                tagSet.add(JpushUserTag.SCHOOL.generateTag(studentDetail.getClazz().getSchoolId().toString()));
            }
            if (studentDetail.getClazz().getId() != null) {
                tagSet.add(JpushUserTag.CLAZZ.generateTag(studentDetail.getClazz().getId().toString()));
            }
        }
        List<GroupMapper> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        studentGroups.forEach(p -> {
            tagSet.add(JpushUserTag.ClAZZ_GROUP.generateTag(p.getId().toString()));
            tagSet.add(JpushUserTag.SUBJECT.generateTag(p.getSubject().name()));
        });
        if (studentDetail.isJuniorStudent()) {
            tagSet.add(JpushUserTag.JUNIOR_SCHOOL.tag);
        } else if (studentDetail.isSeniorStudent()) {
            tagSet.add(JpushUserTag.SENIOR_SCHOOL.tag);
        } else if (studentDetail.isPrimaryStudent()) {
            tagSet.add(JpushUserTag.PRIMARY_SCHOOL.tag);
        } else if (studentDetail.isInfantStudent()) {
            tagSet.add(JpushUserTag.INFANT_SCHOOL.tag);
        }
        return tagSet;
    }

    //生成老师tag
    private Set<String> generateTeacherTag(TeacherDetail teacherDetail, Set<String> tagSet) {
        if (teacherDetail == null) {
            return tagSet;
        }
        if (teacherDetail.getRootRegionCode() != null) {
            tagSet.add(JpushUserTag.PROVINCE.generateTag(teacherDetail.getRootRegionCode().toString()));
        }
        if (teacherDetail.getCityCode() != null) {
            tagSet.add(JpushUserTag.CITY.generateTag(teacherDetail.getCityCode().toString()));
        }
        if (teacherDetail.getRegionCode() != null) {
            tagSet.add(JpushUserTag.COUNTY.generateTag(teacherDetail.getRegionCode().toString()));
        }
        if (teacherDetail.getTeacherSchoolId() != null) {
            tagSet.add(JpushUserTag.SCHOOL.generateTag(teacherDetail.getTeacherSchoolId().toString()));
        }
        tagSet.add(JpushUserTag.AUTH.generateTag(teacherDetail.fetchCertificationState().name()));
        List<GroupTeacherMapper> teacherGroups = deprecatedGroupLoaderClient.loadTeacherGroups(teacherDetail.getId(), false);
        Set<Long> clazzIds = new HashSet<>();
        teacherGroups.stream().filter(Objects::nonNull).forEach(p -> {
            clazzIds.add(p.getClazzId());
            tagSet.add(JpushUserTag.CLAZZ.generateTag(p.getClazzId().toString()));
            tagSet.add(JpushUserTag.ClAZZ_GROUP.generateTag(p.getId().toString()));
            tagSet.add(JpushUserTag.SUBJECT.generateTag(p.getSubject().name()));
        });
        raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .forEach(p -> tagSet.add(JpushUserTag.CLAZZ_LEVEL.generateTag(p.getClassLevel())));
        if (teacherDetail.isJuniorTeacher()) {
            tagSet.add(JpushUserTag.JUNIOR_SCHOOL.tag);
        } else if (teacherDetail.isSeniorTeacher()) {
            tagSet.add(JpushUserTag.SENIOR_SCHOOL.tag);
        } else if (teacherDetail.isPrimarySchool()) {
            tagSet.add(JpushUserTag.PRIMARY_SCHOOL.tag);
        } else if (teacherDetail.isInfantTeacher()) {
            tagSet.add(JpushUserTag.INFANT_SCHOOL.tag);
        }
        return tagSet;
    }

    private Long getCellValueLong(Cell cell) {

        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return null;
        }
        try {
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return new Double(cell.getNumericCellValue()).longValue();
            }
            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                return ConversionUtils.toLong(cell.getStringCellValue().trim());
            }
        } catch (Exception ignored) {
            return 0L;
        }
        return null;
    }

}
