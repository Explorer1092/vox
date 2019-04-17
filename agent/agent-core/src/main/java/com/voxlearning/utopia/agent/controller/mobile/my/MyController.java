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

package com.voxlearning.utopia.agent.controller.mobile.my;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.CreateUserAccount;
import com.voxlearning.utopia.agent.bean.DataPacketInfo;
import com.voxlearning.utopia.agent.bean.SchoolShortInfo;
import com.voxlearning.utopia.agent.constants.*;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentAppContentPacket;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.service.user.OrgConfigService;
import com.voxlearning.utopia.agent.service.workspace.AgentAppContentPacketService;
import com.voxlearning.utopia.data.CrmMainSubApplyStatus;
import com.voxlearning.utopia.entity.crm.CrmMainSubAccountApply;
import com.voxlearning.utopia.entity.crm.schoolrecord.SchoolServiceRecord;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolServiceRecordLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * App 我的标签
 * Created by yaguang.wang on 2016/8/4.
 */
@Controller
@RequestMapping(value = "/mobile/my")
public class MyController extends AbstractAgentController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AgentResourceService agentResourceService;
    @Inject private AgentAppContentPacketService agentAppContentPacketService;
    @Inject private SchoolServiceRecordLoaderClient schoolServiceRecordLoaderClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private SchoolResourceService schoolResourceService;
    @Inject private OrgConfigService orgConfigService;
    @Inject private EmailServiceClient emailServiceClient;

    @RequestMapping(value = "cs.vpage", method = RequestMethod.GET)
    public String customerServices(Model model) {
        model.addAttribute("userId", getCurrentUserId());
        return "rebuildViewDir/mobile/my/cs";
    }

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        AuthCurrentUser user = getCurrentUser();
        AgentUser userInfo = baseUserService.getById(user.getUserId());
        if (userInfo == null) {
            return errorInfoPage(AgentErrorCode.MY_PAGE_ERROR, "用户信息未找到", model);
        }
        model.addAttribute("avatar", userInfo.getAvatar());
        model.addAttribute("name", user.getRealName());
        model.addAttribute("userId", user.getUserId());
        model.addAttribute("userName", user.getUserName());
        return "rebuildViewDir/mobile/my/index";
    }

    @RequestMapping(value = "my_info.vpage", method = RequestMethod.GET)
    public String myInfo(Model model) {
        AuthCurrentUser user = getCurrentUser();
        AgentUser userInfo = baseUserService.getById(user.getUserId());
        if (userInfo == null) {
            return errorInfoPage(AgentErrorCode.MY_PAGE_ERROR, "用户信息未找到", model);
        }
        model.addAttribute("avatar", userInfo.getAvatar());
        model.addAttribute("name", user.getRealName());
        model.addAttribute("userId", user.getUserId());
        model.addAttribute("userName", user.getUserName());

        Long workingDayNum = null;
        if (userInfo.getContractStartDate() != null && userInfo.getContractStartDate().before(new Date())) {
            workingDayNum = DateUtils.dayDiff(new Date(), userInfo.getContractStartDate());
        }
        model.addAttribute("workingDayNum", workingDayNum); //在职天数=当前日期-合同开始日期
        return "rebuildViewDir/mobile/my/my_info";
    }

    @RequestMapping(value = "my_info_new.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage myInfoNew() {
        Map<String, Object> dataMap = new HashMap<>();
        AuthCurrentUser user = getCurrentUser();
        AgentUser userInfo = baseUserService.getById(user.getUserId());
        if (userInfo == null) {
            return MapMessage.errorMessage("用户信息未找到！");
        }
        dataMap.put("avatar", userInfo.getAvatar());
        dataMap.put("name", user.getRealName());
        dataMap.put("userId", user.getUserId());
        dataMap.put("userName", user.getUserName());

        Long workingDayNum = null;
        if (userInfo.getContractStartDate() != null && userInfo.getContractStartDate().before(new Date())) {
            workingDayNum = DateUtils.dayDiff(new Date(), userInfo.getContractStartDate());
        }
        dataMap.put("workingDayNum", workingDayNum); //在职天数=当前日期-合同开始日期
        return MapMessage.successMessage().add("dataMap", dataMap);
    }

    @RequestMapping(value = "upload_avatar.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadAvatar() {
//        return MapMessage.errorMessage("头像服务升级中，敬请期待!");

        AuthCurrentUser user = getCurrentUser();
        String avatar = getRequestString("avatar");
        AgentUser userInfo = baseUserService.getById(user.getUserId());
        if (userInfo == null) {
            return MapMessage.errorMessage("用户信息不存在");
        }
        userInfo.setAvatar(avatar);
        baseUserService.updateAgentUser(userInfo);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "user_apply.vpage", method = RequestMethod.GET)
    public String userApply() {
        return "rebuildViewDir/mobile/my/my_apply";
    }

    /**
     * 专员申请包班记录
     */
    @RequestMapping(value = "clazz_apply_record.vpage", method = RequestMethod.GET)
    public String mainSubAccountApplyRecord(Model model) {
        // 只有专员和市经理有权限
        if (!getCurrentUser().isBusinessDeveloper() && !getCurrentUser().isCityManager()) {
            return errorInfoPage(AgentErrorCode.NO_PERMISSION_TO_USE, "该功能只对市经理及其专员开放", model);
        }
        Long userId = getCurrentUserId();

        List<CrmMainSubAccountApply> applyRecord = agentResourceService.getUserApplyRecord(Collections.singleton(userId));
        // 待审核
        List<CrmMainSubAccountApply> pendingList = applyRecord.stream().filter(app -> CrmMainSubApplyStatus.PENDING.equals(app.getAuditStatus())).collect(Collectors.toList());
        model.addAttribute("pendingList", pendingList);
        // 获取待审核列表的审核人员
        // 包班申请有专员提交， 市经理审核
        List<AgentUser> userManagerList = baseOrgService.getUserManager(userId);
        if (CollectionUtils.isNotEmpty(userManagerList)) {
            model.addAttribute("userManager", userManagerList.get(0));
        }
        // 已通过
        List<CrmMainSubAccountApply> successList = applyRecord.stream().filter(app -> CrmMainSubApplyStatus.APPROVED.equals(app.getAuditStatus())).collect(Collectors.toList());
        model.addAttribute("successList", successList);
        // 已驳回
        List<CrmMainSubAccountApply> rejectList = applyRecord.stream().filter(app -> CrmMainSubApplyStatus.REJECT.equals(app.getAuditStatus())).collect(Collectors.toList());
        model.addAttribute("rejectList", rejectList);
        return "rebuildViewDir/mobile/my/clazz_apply_record";
    }

    /**
     * 处理申请包班记录
     */
    @RequestMapping(value = "clazz_apply_list.vpage", method = RequestMethod.GET)
    public String mainSubAccountApplyList(Model model) {
        // 只有市经理有权限
        if (!getCurrentUser().isCityManager()) {
            return errorInfoPage(AgentErrorCode.NO_PERMISSION_TO_USE, "该功能只对市经理开放", model);
        }
        Long userId = getCurrentUserId();
        Set<Long> managedUsers = baseOrgService.getManagedGroupUsers(userId, true)
                .stream().map(AgentUser::getId).collect(toSet());

        List<CrmMainSubAccountApply> applyRecord = agentResourceService.getUserApplyRecord(managedUsers);
        // 待审核
        List<CrmMainSubAccountApply> pendingList = applyRecord.stream().filter(app -> CrmMainSubApplyStatus.PENDING.equals(app.getAuditStatus())).collect(Collectors.toList());
        model.addAttribute("pendingList", pendingList);
        // 已通过
        List<CrmMainSubAccountApply> successList = applyRecord.stream().filter(app -> CrmMainSubApplyStatus.APPROVED.equals(app.getAuditStatus())).collect(Collectors.toList());
        model.addAttribute("successList", successList);
        // 已驳回
        List<CrmMainSubAccountApply> rejectList = applyRecord.stream().filter(app -> CrmMainSubApplyStatus.REJECT.equals(app.getAuditStatus())).collect(Collectors.toList());
        model.addAttribute("rejectList", rejectList);
        return "rebuildViewDir/mobile/my/clazz_apply_list";
    }

    //资料包
    @RequestMapping(value = "data_packet.vpage", method = RequestMethod.GET)
    public String dataPacket(Model model) {
        AuthCurrentUser user = getCurrentUser();
        List<Integer> roleList = user.getRoleList();
        if (CollectionUtils.isEmpty(roleList)) {
            errorInfoPage(AgentErrorCode.DATA_PACKET_ERROR, "该用户无角色信息", model);
        }
        Integer roleId = roleList.get(0);
        String datumType = getRequestString("datumType");
        model.addAttribute("datumType", agentDataPacketTypeList());
        List<AgentAppContentPacket> dataPacket = agentAppContentPacketService.loadByContentType(AgentAppContentType.DATA_PACKET);
        dataPacket = dataPacket.stream().filter(p -> !SafeConverter.toBoolean(p.getDisabled()) && p.getState() == AppContentStateType.RELEASE)
                .filter(p1 -> CollectionUtils.isEmpty(p1.getApplyRole()) || (AgentDataPacketRole.typeOf(roleId) != null && p1.getApplyRole()
                        .contains(AgentDataPacketRole.typeOf(roleId)))).collect(Collectors.toList());
        Map<Integer, List<AgentAppContentPacket>> groupDataPacket = dataPacket.stream().collect(Collectors.groupingBy(p -> p.getDatumType().getId()));
        Map<String, List<DataPacketInfo>> groupDataPacketInfo = new HashMap<>();
        for (Integer typeId : groupDataPacket.keySet()) {
            groupDataPacketInfo.put(SafeConverter.toString(typeId), createDataPacketInfo(groupDataPacket.get(typeId)));
        }
        model.addAttribute("dataPacket", groupDataPacketInfo);
        model.addAttribute("type", datumType);
        return "rebuildViewDir/mobile/my/tips";
    }


    private List<Map<String, String>> agentDataPacketTypeList() {
        List<Map<String, String>> result = new ArrayList<>();
        for (AgentDataPacketType type : AgentDataPacketType.values()) {
            Map<String, String> info = new HashMap<>();
            info.put("id", SafeConverter.toString(type.getId()));
            info.put("desc", SafeConverter.toString(type.getDesc()));
            result.add(info);
        }
        return result;
    }

    private List<DataPacketInfo> createDataPacketInfo(List<AgentAppContentPacket> dataPacket) {
        if (CollectionUtils.isEmpty(dataPacket)) {
            return Collections.emptyList();
        }
        List<DataPacketInfo> result = new ArrayList<>();
        dataPacket.forEach(p -> {
            DataPacketInfo info = new DataPacketInfo();
            info.setFileName(p.getContentTitle());
            info.setFileUrl("/mobile/notice/noticeReader.vpage?contentId=" + p.getId());
            info.setId(p.getId());
            result.add(info);
        });
        return result;
    }

    //首页设置页
    @RequestMapping(value = "setting.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String getSetting() {
        return "mobile/work_record/setting";
    }


    /**
     * @return
     */
    @RequestMapping(value = "school.vpage", method = RequestMethod.GET)
    @OperationCode("771be5adaac5499c")
    public String school(Model model) {
        AuthCurrentUser currentUser = getCurrentUser();
        List<SchoolServiceRecord> schoolServiceRecords = schoolServiceRecordLoaderClient.loadRecentCreatedSchools(String.valueOf(currentUser.getUserId()));
        //
        if (CollectionUtils.isNotEmpty(schoolServiceRecords)) {
            List<Long> schoolIds = new ArrayList<>();
            schoolServiceRecords.stream().forEach(item -> schoolIds.add(item.getSchoolId()));
            Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);
            List<SchoolShortInfo> schools = new ArrayList<>();
            schoolServiceRecords.stream().forEach(item -> {
                School school = schoolMap.get(item.getSchoolId());
                if (null != school) {
                    SchoolShortInfo shortInfo = new SchoolShortInfo();
                    shortInfo.setCreateTime(item.getCreateTime());
                    shortInfo.setLevel(school.getLevel());
                    ExRegion schoolRegion = raikouSystem.loadRegion(school.getRegionCode());
                    shortInfo.setRegionName(StringUtils.join(schoolRegion.getCityName(), schoolRegion.getCountyName()));
                    shortInfo.setSchoolId(item.getSchoolId());
                    shortInfo.setSchoolName(item.getSchoolName());
                    schools.add(shortInfo);
                }
            });
            model.addAttribute("schools", schools);
        }

        return "rebuildViewDir/mobile/my/createdSchoolList";
    }

    //客服协助列表页
    @RequestMapping(value = "customer.vpage", method = RequestMethod.GET)
    @OperationCode("1b015cbe72ff4fce")
    public String customerService(Model model) {
        long id = getRequestLong("id");
        int idType = getRequestInt("type");//idtype 1 学校 2 老师
        String source = requestString("source");
        //从老师详情页或者学校详情页过来
        if (StringUtils.isNotBlank(source) && "reffer".equals(source)) {
            if (id <= 0) {
                return errorInfoPage(AgentErrorCode.MY_PAGE_ERROR, "id未传", model);
            }
            if (idType <= 0) {
                return errorInfoPage(AgentErrorCode.MY_PAGE_ERROR, "id类型未传", model);
            }
            if (idType == 1) {
                School school = raikouSystem.loadSchool(id);
                if (null != school) {
                    model.addAttribute("title", school.getCname());
                    ExRegion schoolRegion = raikouSystem.loadRegion(school.getRegionCode());
                    model.addAttribute("desc", StringUtils.join(schoolRegion.getCityName(), schoolRegion.getCountyName()));
                    model.addAttribute("note", id);
                    SchoolExtInfo schoolExtInfo = schoolResourceService.getSchoolExtInfo(school.getId());
                    if (schoolExtInfo != null) {
                        model.addAttribute("photoUrl", schoolExtInfo.getPhotoUrl());
                    } else {
                        model.addAttribute("photoUrl", "");
                    }
                }
            } else if (idType == 2) {
                Teacher teacher = teacherLoaderClient.loadTeacher(id);
                if (teacher != null) {
                    model.addAttribute("title", teacher.getProfile().getRealname());
                    School school = asyncTeacherServiceClient.getAsyncTeacherService()
                            .loadTeacherSchool(id)
                            .getUninterruptibly();
                    if (school != null) {
                        model.addAttribute("desc", school.getCname());
                    }
                    model.addAttribute("note", id);
                    model.addAttribute("photoUrl", agentResourceService.getUserPhoto(teacher));
                }
            }
        }
        model.addAttribute("userId", getCurrentUserId());
        model.addAttribute("type", idType);
        return "rebuildViewDir/mobile/my/customer";
    }

    /**
     * 获取当前登录人
     *
     * @return
     */
    @RequestMapping(value = "current_user.vpage")
    @ResponseBody
    public MapMessage currentUser() {
        AuthCurrentUser currentUser = getCurrentUser();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("userId", currentUser.getUserId());
        dataMap.put("userName", currentUser.getUserName());
        dataMap.put("realName", currentUser.getRealName());
        List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(getCurrentUserId());
        dataMap.put("agentRoleType", groupUserByUser.size() > 0 ? groupUserByUser.get(0).getUserRoleType() : null);
        List<SchoolLevel> schoolLevels = baseOrgService.getUserServiceSchoolLevels(currentUser.getUserId());
        dataMap.put("schoolLevels", schoolLevels);
        List<Integer> roleIds = baseOrgService.getUserRoleList(currentUser.getUserId()).stream().map(AgentRoleType::getId).collect(Collectors.toList());
        dataMap.put("roleIds", roleIds);
        return MapMessage.successMessage().add("currentUser", dataMap);
    }

    // node端调用
    @RequestMapping(value = "fetch_user_element_codes.vpage", name = "获取用户拥有的页面元素编码")
    @ResponseBody
    public MapMessage fetchUserElementCodes() {
        return MapMessage.successMessage().add("elementCodeList", getCurrentUser().getPageElementCodes());
    }

    //客服协助列表页
    @RequestMapping(value = "customer_new.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage customerServiceNew() {
        long id = getRequestLong("id");
        int idType = getRequestInt("type");//idtype 1 学校 2 老师
        String source = requestString("source");
        //从老师详情页或者学校详情页过来
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(source) && "reffer".equals(source)) {
            if (id <= 0) {
                return MapMessage.errorMessage("id未传");
            }
            if (idType <= 0) {
                return MapMessage.errorMessage("id类型未传");
            }

            if (idType == 1) {
                School school = raikouSystem.loadSchool(id);
                if (null != school) {
                    map.put("title", school.getCname());
                    ExRegion schoolRegion = raikouSystem.loadRegion(school.getRegionCode());
                    map.put("desc", StringUtils.join(schoolRegion.getCityName(), schoolRegion.getCountyName()));
                    map.put("note", id);
                    SchoolExtInfo schoolExtInfo = schoolResourceService.getSchoolExtInfo(school.getId());
                    if (schoolExtInfo != null) {
                        map.put("photoUrl", schoolExtInfo.getPhotoUrl());
                    } else {
                        map.put("photoUrl", "");
                    }
                }
            } else if (idType == 2) {
                Teacher teacher = teacherLoaderClient.loadTeacher(id);
                if (teacher != null) {
                    map.put("title", teacher.getProfile().getRealname());
                    School school = asyncTeacherServiceClient.getAsyncTeacherService()
                            .loadTeacherSchool(id)
                            .getUninterruptibly();
                    if (school != null) {
                        map.put("desc", school.getCname());
                    }
                    map.put("note", id);
                    map.put("photoUrl", agentResourceService.getUserPhoto(teacher));
                }
            }
        }
        map.put("userId", getCurrentUserId());
        map.put("type", idType);
        return MapMessage.successMessage().add("dataMap", map);
    }

    /**
     * 初始化公司员工账号方法      先留着 还得发邮件  发完邮件删除
     *
     * @return
     */
    @RequestMapping(value = "batch_create_account1.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage batch_create_account1() {
        Integer status = getCountryDayOrderCount(1);
        if (status != null && Objects.equals(1, status)) {
            return MapMessage.errorMessage("方法正在执行了等会吧");
        }
        updateCountryDayOrderCount(1);
        Integer type = getRequestInt("type", 1);
        List<CreateUserAccount> list = new ArrayList<>();
        list.add(new CreateUserAccount("xuejiao.ma", "马雪娇", "xuejiao.ma@17zuoye.com", 0f, 0f, 1, "0001", "086379", 1605l, DateUtils.stringToDate("2009-07-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huishu.li", "李慧书", "huishu.li@17zuoye.com", 0f, 0f, 1, "0002", "623439", 1603l, DateUtils.stringToDate("2011-04-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guohong.tan", "谭国红", "guohong.tan@17zuoye.com", 0f, 0f, 1, "0003", "162957", 1602l, DateUtils.stringToDate("2011-06-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lu.zhang", "张璐", "lu.zhang@17zuoye.com", 0f, 0f, 1, "0005", "870594", 1604l, DateUtils.stringToDate("2011-10-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yifei.peng", "彭亦飞", "yifei.peng@17zuoye.com", 0f, 0f, 1, "0006", "949960", 1601l, DateUtils.stringToDate("2011-11-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.yang", "杨艳", "yan.yang@17zuoye.com", 0f, 0f, 1, "0007", "767135", 1604l, DateUtils.stringToDate("2011-11-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("caijuan.gao", "高彩娟", "caijuan.gao@17zuoye.com", 0f, 0f, 1, "0008", "430134", 1599l, DateUtils.stringToDate("2011-12-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaohai.zhang", "张晓海", "xiaohai.zhang@17zuoye.com", 0f, 0f, 1, "0011", "452646", 1599l, DateUtils.stringToDate("2012-02-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinqiang.wang", "王新强", "xinqiang.wang@17zuoye.com", 0f, 0f, 1, "0013", "874076", 1602l, DateUtils.stringToDate("2012-02-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rui.bao", "包瑞", "rui.bao@17zuoye.com", 0f, 0f, 1, "0014", "923046", 1602l, DateUtils.stringToDate("2012-02-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaojuan.jia", "贾小娟", "xiaojuan.jia@17zuoye.com", 0f, 0f, 1, "0016", "234938", 1603l, DateUtils.stringToDate("2012-03-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("miao.yu", "于淼", "miao.yu@17zuoye.com", 0f, 0f, 1, "0021", "699958", 1599l, DateUtils.stringToDate("2012-11-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yizhou.zhang", "张轶洲", "yizhou.zhang@17zuoye.com", 0f, 0f, 1, "0024", "473023", 1601l, DateUtils.stringToDate("2013-01-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("maofeng.lu", "路茂峰", "maofeng.lu@17zuoye.com", 0f, 0f, 1, "0031", "669647", 1602l, DateUtils.stringToDate("2013-05-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianjun.xiao", "肖建军", "jianjun.xiao@17zuoye.com", 0f, 0f, 1, "0040", "394020", 1602l, DateUtils.stringToDate("2013-09-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("sen.wang", "王延超", "sen.wang@17zuoye.com", 0f, 0f, 1, "0041", "309010", 1599l, DateUtils.stringToDate("2013-09-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junfeng.li", "李骏峰", "junfeng.li@17zuoye.com", 0f, 0f, 1, "0044", "414405", 1599l, DateUtils.stringToDate("2013-10-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shoudong.qi", "齐守东", "shoudong.qi@17zuoye.com", 0f, 0f, 1, "0049", "046443", 1599l, DateUtils.stringToDate("2013-11-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ava.huang", "黄翚", "ava.huang@17zuoye.com", 0f, 0f, 1, "0055", "354018", 1599l, DateUtils.stringToDate("2013-12-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xin.xin", "信新", "xin.xin@17zuoye.com", 0f, 0f, 1, "0057", "005171", 1603l, DateUtils.stringToDate("2015-09-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunlong.shen", "申春龙", "chunlong.shen@17zuoye.com", 0f, 0f, 1, "0059", "549488", 1599l, DateUtils.stringToDate("2014-02-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaopeng.yang", "杨霄鹏", "xiaopeng.yang@17zuoye.com", 0f, 0f, 1, "0069", "922307", 1599l, DateUtils.stringToDate("2014-02-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chong.liu", "刘充", "chong.liu@17zuoye.com", 0f, 0f, 1, "0082", "954790", 1599l, DateUtils.stringToDate("2014-04-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qianlong.yang", "杨前龙", "qianlong.yang@17zuoye.com", 0f, 0f, 1, "0083", "683293", 1601l, DateUtils.stringToDate("2014-03-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shan.jiang", "江珊", "shan.jiang@17zuoye.com", 0f, 0f, 1, "0088", "327888", 1600l, DateUtils.stringToDate("2014-03-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liang.zhang", "张亮", "liang.zhang@17zuoye.com", 0f, 0f, 1, "0089", "139552", 1599l, DateUtils.stringToDate("2014-04-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhilong.hu", "胡志龙", "zhilong.hu@17zuoye.com", 0f, 0f, 1, "0090", "437985", 1603l, DateUtils.stringToDate("2014-04-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhan.hu", "胡展", "zhan.hu@17zuoye.com", 0f, 0f, 1, "0091", "899574", 1599l, DateUtils.stringToDate("2014-03-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hui.zhang", "张慧", "hui.zhang@17zuoye.com", 0f, 0f, 1, "0099", "912209", 1599l, DateUtils.stringToDate("2012-01-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hui.mi", "米慧", "hui.mi@17zuoye.com", 0f, 0f, 1, "0101", "194992", 1603l, DateUtils.stringToDate("2012-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoning.liu", "刘小宁", "xiaoning.liu@17zuoye.com", 0f, 0f, 1, "0102", "733687", 1603l, DateUtils.stringToDate("2012-04-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuang.fan", "范爽", "shuang.fan@17zuoye.com", 0f, 0f, 1, "0103", "777888", 1602l, DateUtils.stringToDate("2012-04-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dongshuang.zhao", "赵东双", "dongshuang.zhao@17zuoye.com", 0f, 0f, 1, "0104", "330235", 1599l, DateUtils.stringToDate("2012-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bin.wang", "王斌", "bin.wang@17zuoye.com", 0f, 0f, 1, "0108", "184698", 1603l, DateUtils.stringToDate("2013-02-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("sainan.zhao", "赵赛楠", "sainan.zhao@17zuoye.com", 0f, 0f, 1, "0113", "323942", 1603l, DateUtils.stringToDate("2013-07-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaojie.chen", "陈晓婕", "xiaojie.chen@17zuoye.com", 0f, 0f, 1, "0121", "534963", 1602l, DateUtils.stringToDate("2014-02-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("li.yu", "于莉", "li.yu@17zuoye.com", 0f, 0f, 1, "0122", "039301", 1605l, DateUtils.stringToDate("2014-02-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qifeng.zhao", "赵启峰", "qifeng.zhao@17zuoye.com", 0f, 0f, 1, "0123", "705993", 1601l, DateUtils.stringToDate("2014-01-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junfang.du", "杜君芳", "junfang.du@17zuoye.com", 0f, 0f, 1, "0124", "549061", 1605l, DateUtils.stringToDate("2014-02-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wen.wan", "宛雯", "wen.wan@17zuoye.com", 0f, 0f, 1, "0130", "795706", 1599l, DateUtils.stringToDate("2014-03-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hongtao.li", "李红涛", "hongtao.li@17zuoye.com", 0f, 0f, 1, "0163", "480467", 1601l, DateUtils.stringToDate("2014-03-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhi.wang", "王志", "zhi.wang@17zuoye.com", 0f, 0f, 1, "0166", "956436", 1602l, DateUtils.stringToDate("2014-03-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zheng.ge", "葛正", "zheng.ge@17zuoye.com", 0f, 0f, 1, "0167", "349952", 1603l, DateUtils.stringToDate("2014-03-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.zhang", "张羽", "yu.zhang@17zuoye.com", 0f, 0f, 1, "0173", "021629", 1599l, DateUtils.stringToDate("2014-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuai.huan", "郇帅", "shuai.huan@17zuoye.com", 0f, 0f, 1, "0182", "243636", 1603l, DateUtils.stringToDate("2014-04-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanbin.li", "李艳斌", "yanbin.li@17zuoye.com", 0f, 0f, 1, "0186", "971787", 1599l, DateUtils.stringToDate("2014-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("honghao.zhang", "张洪浩", "honghao.zhang@17zuoye.com", 0f, 0f, 1, "0197", "235710", 1603l, DateUtils.stringToDate("2014-06-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chu.wang", "王楚", "chu.wang@17zuoye.com", 0f, 0f, 1, "0199", "470324", 1602l, DateUtils.stringToDate("2014-06-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haodong.wu", "吴浩东", "haodong.wu@17zuoye.com", 0f, 0f, 1, "0201", "352983", 1604l, DateUtils.stringToDate("2014-05-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junchen.feng", "冯俊晨", "junchen.feng@17zuoye.com", 0f, 0f, 1, "0209", "518048", 1599l, DateUtils.stringToDate("2017-04-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiong.gu", "顾雄", "xiong.gu@17zuoye.com", 0f, 0f, 1, "0210", "964994", 1599l, DateUtils.stringToDate("2014-07-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaojun.wang", "王小军", "xiaojun.wang@17zuoye.com", 0f, 0f, 1, "0214", "002242", 1599l, DateUtils.stringToDate("2014-07-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiangkun.yang", "杨江昆", "jiangkun.yang@17zuoye.com", 0f, 0f, 1, "0216", "314582", 1599l, DateUtils.stringToDate("2014-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenbin.tao", "陶文斌", "wenbin.tao@17zuoye.com", 0f, 0f, 1, "0217", "487773", 1601l, DateUtils.stringToDate("2014-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("murui.zhang", "张目锐", "murui.zhang@17zuoye.com", 0f, 0f, 1, "0218", "890309", 1599l, DateUtils.stringToDate("2014-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xu.yan", "闫旭", "xu.yan@17zuoye.com", 0f, 0f, 1, "0229", "147527", 1603l, DateUtils.stringToDate("2014-10-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shiyang.zhao", "赵诗扬", "shiyang.zhao@17zuoye.com", 0f, 0f, 1, "0230", "712578", 1599l, DateUtils.stringToDate("2014-10-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanying.ma", "马艳英", "yanying.ma@17zuoye.com", 0f, 0f, 1, "0241", "340471", 1603l, DateUtils.stringToDate("2014-12-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huiduo.cheng", "程会朵", "huiduo.cheng@17zuoye.com", 0f, 0f, 1, "0245", "277097", 1599l, DateUtils.stringToDate("2014-12-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaomeng.yi", "易晓萌", "xiaomeng.yi@17zuoye.com", 0f, 0f, 1, "0248", "267868", 1599l, DateUtils.stringToDate("2014-12-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingwei.qiu", "邱经纬", "jingwei.qiu@17zuoye.com", 0f, 0f, 1, "0253", "178796", 1603l, DateUtils.stringToDate("2014-12-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("changyuan.liu", "刘长远", "changyuan.liu@17zuoye.com", 0f, 0f, 1, "0254", "887160", 1599l, DateUtils.stringToDate("2015-01-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ge.nie", "聂鸽", "ge.nie@17zuoye.com", 0f, 0f, 1, "0276", "265233", 1605l, DateUtils.stringToDate("2015-03-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xin.zhang", "张昕", "xin.zhang@17zuoye.com", 0f, 0f, 1, "0277", "680398", 1604l, DateUtils.stringToDate("2015-03-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zheng.liu", "刘正", "zheng.liu@17zuoye.com", 0f, 0f, 1, "0288", "941943", 1599l, DateUtils.stringToDate("2015-04-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("na.ai", "艾娜", "na.ai@17zuoye.com", 0f, 0f, 1, "0300", "470389", 1600l, DateUtils.stringToDate("2015-04-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jinbao.yang", "杨金宝", "jinbao.yang@17zuoye.com", 0f, 0f, 1, "0304", "793985", 1599l, DateUtils.stringToDate("2015-04-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jia.liu", "刘佳", "jia.liu@17zuoye.com", 0f, 0f, 1, "0322", "558794", 1604l, DateUtils.stringToDate("2015-05-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenpeng.you", "游文鹏", "wenpeng.you@17zuoye.com", 0f, 0f, 1, "0326", "259965", 1599l, DateUtils.stringToDate("2015-05-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.zhao", "赵影", "ying.zhao@17zuoye.com", 0f, 0f, 1, "0339", "638705", 1599l, DateUtils.stringToDate("2015-06-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuesong.zhang", "张学松", "xuesong.zhang@17zuoye.com", 0f, 0f, 1, "0342", "290601", 1599l, DateUtils.stringToDate("2015-06-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mengmeng.chen", "陈萌萌", "mengmeng.chen@17zuoye.com", 0f, 0f, 1, "0345", "132516", 1599l, DateUtils.stringToDate("2015-06-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("longji.li", "李隆基", "longji.li@17zuoye.com", 0f, 0f, 1, "0354", "914382", 1599l, DateUtils.stringToDate("2015-06-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiao.cui", "崔晓", "xiao.cui@17zuoye.com", 0f, 0f, 1, "0362", "172275", 1600l, DateUtils.stringToDate("2015-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoquan.wang", "汪晓泉", "xiaoquan.wang@17zuoye.com", 0f, 0f, 1, "0369", "270498", 1604l, DateUtils.stringToDate("2015-07-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huaying.tao", "陶华英", "huaying.tao@17zuoye.com", 0f, 0f, 1, "0375", "817073", 1604l, DateUtils.stringToDate("2015-07-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jun.lin", "林俊", "jun.lin@17zuoye.com", 0f, 0f, 1, "0376", "622146", 1604l, DateUtils.stringToDate("2015-07-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ke.jiang", "姜烨", "ke.jiang@17zuoye.com", 0f, 0f, 1, "0380", "908025", 1600l, DateUtils.stringToDate("2015-07-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lingxiao.zhang", "张凌霄", "lingxiao.zhang@17zuoye.com", 0f, 0f, 1, "0400", "352094", 1603l, DateUtils.stringToDate("2015-07-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liyan.chang", "常丽艳", "liyan.chang@17zuoye.com", 0f, 0f, 1, "0401", "350191", 1603l, DateUtils.stringToDate("2015-07-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wentao.chen", "陈文涛", "wentao.chen@17zuoye.com", 0f, 0f, 1, "0402", "729637", 1599l, DateUtils.stringToDate("2015-07-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("baochang.yang", "杨宝长", "baochang.yang@17zuoye.com", 0f, 0f, 1, "0413", "062103", 1599l, DateUtils.stringToDate("2015-07-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hui.cheng", "程辉", "hui.cheng@17zuoye.com", 0f, 0f, 1, "0443", "632951", 1599l, DateUtils.stringToDate("2015-08-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yang.zhao.a", "赵杨", "yang.zhao.a@17zuoye.com", 0f, 0f, 1, "0449", "311140", 1603l, DateUtils.stringToDate("2015-08-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yalong.zhang", "张亚龙", "yalong.zhang@17zuoye.com", 0f, 0f, 1, "0456", "710410", 1599l, DateUtils.stringToDate("2015-08-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cong.ma", "马聪", "cong.ma@17zuoye.com", 0f, 0f, 1, "0457", "111933", 1603l, DateUtils.stringToDate("2015-08-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guojun.li", "李国君", "guojun.li@17zuoye.com", 0f, 0f, 1, "0459", "436317", 1603l, DateUtils.stringToDate("2015-08-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yonggang.zhang", "张永岗", "yonggang.zhang@17zuoye.com", 0f, 0f, 1, "0463", "277336", 1605l, DateUtils.stringToDate("2015-08-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.zhang", "张赢", "ying.zhang@17zuoye.com", 0f, 0f, 1, "0481", "251042", 1604l, DateUtils.stringToDate("2015-08-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hao.zhang", "张昊", "hao.zhang@17zuoye.com", 0f, 0f, 1, "0482", "519731", 1603l, DateUtils.stringToDate("2015-08-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("sha.zeng", "曾莎", "sha.zeng@17zuoye.com", 0f, 0f, 1, "0486", "343284", 1603l, DateUtils.stringToDate("2015-08-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qiangqiang.luan", "栾强强", "qiangqiang.luan@17zuoye.com", 0f, 0f, 1, "0498", "855279", 1599l, DateUtils.stringToDate("2015-08-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("luwei.li", "李路伟", "luwei.li@17zuoye.com", 0f, 0f, 1, "0509", "965386", 1603l, DateUtils.stringToDate("2015-08-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shasha.zhang", "张沙沙", "shasha.zhang@17zuoye.com", 0f, 0f, 1, "0514", "148654", 1599l, DateUtils.stringToDate("2015-08-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bowen.li", "李博文", "bowen.li@17zuoye.com", 0f, 0f, 1, "0522", "664835", 1599l, DateUtils.stringToDate("2015-09-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jinling.zhu", "朱金玲", "jinling.zhu@17zuoye.com", 0f, 0f, 1, "0523", "106628", 1601l, DateUtils.stringToDate("2015-09-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cuicui.he", "贺翠翠", "cuicui.he@17zuoye.com", 0f, 0f, 1, "0525", "194249", 1603l, DateUtils.stringToDate("2015-09-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.wang", "王磊", "lei.wang@17zuoye.com", 0f, 0f, 1, "0526", "338527", 1599l, DateUtils.stringToDate("2015-09-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoyan.xu", "许小艳", "xiaoyan.xu@17zuoye.com", 0f, 0f, 1, "0527", "134472", 1600l, DateUtils.stringToDate("2015-09-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingjiao.kang", "康明娇", "mingjiao.kang@17zuoye.com", 0f, 0f, 1, "0528", "012395", 1603l, DateUtils.stringToDate("2015-09-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("suqiang.wang", "王素强", "suqiang.wang@17zuoye.com", 0f, 0f, 1, "0533", "917766", 1603l, DateUtils.stringToDate("2015-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("nengfang.han", "韩能放", "nengfang.han@17zuoye.com", 0f, 0f, 1, "0535", "675136", 1599l, DateUtils.stringToDate("2015-09-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rong.song", "宋蓉", "rong.song@17zuoye.com", 0f, 0f, 1, "0537", "448863", 1603l, DateUtils.stringToDate("2015-09-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiuxiu.wang", "王秀秀", "xiuxiu.wang@17zuoye.com", 0f, 0f, 1, "0545", "327905", 1603l, DateUtils.stringToDate("2015-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("juanxia.liu", "刘娟霞", "juanxia.liu@17zuoye.com", 0f, 0f, 1, "0546", "386635", 1603l, DateUtils.stringToDate("2015-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lijing.wang", "王丽静", "lijing.wang@17zuoye.com", 0f, 0f, 1, "0552", "812409", 1603l, DateUtils.stringToDate("2015-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.wang", "王颖", "ying.wang@17zuoye.com", 0f, 0f, 1, "0553", "764467", 1603l, DateUtils.stringToDate("2015-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peipei.wang", "王培培", "peipei.wang@17zuoye.com", 0f, 0f, 1, "0554", "650156", 1603l, DateUtils.stringToDate("2015-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lihong.jin", "金立红", "lihong.jin@17zuoye.com", 0f, 0f, 1, "0555", "799701", 1603l, DateUtils.stringToDate("2015-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.zhang", "张静", "jing.zhang@17zuoye.com", 0f, 0f, 1, "0566", "468526", 1603l, DateUtils.stringToDate("2015-09-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guoguang.zhang", "张国光", "guoguang.zhang@17zuoye.com", 0f, 0f, 1, "0574", "289281", 1599l, DateUtils.stringToDate("2015-09-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaodan.liu", "刘晓丹", "xiaodan.liu@17zuoye.com", 0f, 0f, 1, "0580", "969194", 1599l, DateUtils.stringToDate("2015-10-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuhong.liu", "刘旭宏", "xuhong.liu@17zuoye.com", 0f, 0f, 1, "0582", "037958", 1602l, DateUtils.stringToDate("2015-10-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("binwei.liu", "刘彬伟", "binwei.liu@17zuoye.com", 0f, 0f, 1, "0583", "609083", 1599l, DateUtils.stringToDate("2015-10-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weiyi.shen", "沈玮一", "weiyi.shen@17zuoye.com", 0f, 0f, 1, "0585", "639608", 1599l, DateUtils.stringToDate("2015-10-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shaotao.zhang", "张少涛", "shaotao.zhang@17zuoye.com", 0f, 0f, 1, "0589", "457329", 1599l, DateUtils.stringToDate("2015-10-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yongna.hu", "户永娜", "yongna.hu@17zuoye.com", 0f, 0f, 1, "0593", "444180", 1603l, DateUtils.stringToDate("2015-10-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yawei.yu", "于亚威", "yawei.yu@17zuoye.com", 0f, 0f, 1, "0594", "893185", 1603l, DateUtils.stringToDate("2015-10-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yutong.ma", "马钰桐", "yutong.ma@17zuoye.com", 0f, 0f, 1, "0595", "929573", 1603l, DateUtils.stringToDate("2015-10-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("changqun.xu", "许昌群", "changqun.xu@17zuoye.com", 0f, 0f, 1, "0596", "358599", 1599l, DateUtils.stringToDate("2015-10-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuyan.yuan", "袁淑妍", "shuyan.yuan@17zuoye.com", 0f, 0f, 1, "0598", "247983", 1599l, DateUtils.stringToDate("2015-11-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guoqiang.wang", "王国强", "guoqiang.wang@17zuoye.com", 0f, 0f, 1, "0600", "408263", 1599l, DateUtils.stringToDate("2015-11-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lili.liu", "刘丽丽", "lili.liu@17zuoye.com", 0f, 0f, 1, "0603", "565312", 1603l, DateUtils.stringToDate("2015-11-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhao.liu", "刘钊", "zhao.liu@17zuoye.com", 0f, 0f, 1, "0604", "560182", 1599l, DateUtils.stringToDate("2015-11-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qiaoyun.liu", "刘巧云", "qiaoyun.liu@17zuoye.com", 0f, 0f, 1, "0608", "708388", 1599l, DateUtils.stringToDate("2015-11-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenjing.liu", "刘文婧", "wenjing.liu@17zuoye.com", 0f, 0f, 1, "0618", "840093", 1604l, DateUtils.stringToDate("2015-11-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("congsen.zeng", "曾从森", "congsen.zeng@17zuoye.com", 0f, 0f, 1, "0621", "214740", 1599l, DateUtils.stringToDate("2015-11-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("nan.jiang", "姜楠", "nan.jiang@17zuoye.com", 0f, 0f, 1, "0622", "842278", 1604l, DateUtils.stringToDate("2015-11-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guoqiang.li", "李国强", "guoqiang.li@17zuoye.com", 0f, 0f, 1, "0623", "503416", 1602l, DateUtils.stringToDate("2015-12-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kexin.xing", "邢柯鑫", "kexin.xing@17zuoye.com", 0f, 0f, 1, "0628", "013362", 1603l, DateUtils.stringToDate("2015-12-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("siqi.li", "李思琦", "siqi.li@17zuoye.com", 0f, 0f, 1, "0629", "961685", 1603l, DateUtils.stringToDate("2015-11-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bo.liu", "刘博", "bo.liu@17zuoye.com", 0f, 0f, 1, "0632", "344675", 1600l, DateUtils.stringToDate("2015-12-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanjiao.zhang", "张艳娇", "yanjiao.zhang@17zuoye.com", 0f, 0f, 1, "0633", "486518", 1600l, DateUtils.stringToDate("2015-12-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meng.chen", "陈萌", "meng.chen@17zuoye.com", 0f, 0f, 1, "0634", "602583", 1602l, DateUtils.stringToDate("2015-12-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yongzhi.xin", "辛勇志", "yongzhi.xin@17zuoye.com", 0f, 0f, 1, "0653", "078536", 1599l, DateUtils.stringToDate("2018-08-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingyuan.yao", "姚静媛", "jingyuan.yao@17zuoye.com", 0f, 0f, 1, "0659", "270213", 1602l, DateUtils.stringToDate("2016-01-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yiqun.huang", "黄轶群", "yiqun.huang@17zuoye.com", 0f, 0f, 1, "0680", "298110", 1604l, DateUtils.stringToDate("2016-02-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zijing.gong", "龚子敬", "zijing.gong@17zuoye.com", 0f, 0f, 1, "0682", "304494", 1599l, DateUtils.stringToDate("2016-02-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.jiang", "蒋鹏", "peng.jiang@17zuoye.com", 0f, 0f, 1, "0684", "632092", 1603l, DateUtils.stringToDate("2016-02-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xin.jin", "金鑫", "xin.jin@17zuoye.com", 0f, 0f, 1, "0704", "270457", 1601l, DateUtils.stringToDate("2016-02-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.an", "安静", "jing.an@17zuoye.com", 0f, 0f, 1, "0711", "371359", 1604l, DateUtils.stringToDate("2016-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kan.luo", "罗侃", "kan.luo@17zuoye.com", 0f, 0f, 1, "0715", "816411", 1603l, DateUtils.stringToDate("2016-03-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shanshan.yan", "闫闪闪", "shanshan.yan@17zuoye.com", 0f, 0f, 1, "0719", "173666", 1603l, DateUtils.stringToDate("2016-02-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("miaomiao.jing", "景苗苗", "miaomiao.jing@17zuoye.com", 0f, 0f, 1, "0724", "685686", 1603l, DateUtils.stringToDate("2016-02-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("gaoyang.li", "李高杨", "gaoyang.li@17zuoye.com", 0f, 0f, 1, "0727", "532034", 1602l, DateUtils.stringToDate("2016-03-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yahui.wang", "王亚辉", "yahui.wang@17zuoye.com", 0f, 0f, 1, "0729", "209208", 1603l, DateUtils.stringToDate("2016-02-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liming.zhang", "张利铭", "liming.zhang@17zuoye.com", 0f, 0f, 1, "0751", "346493", 1599l, DateUtils.stringToDate("2016-03-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haifu.li", "李海富", "haifu.li@17zuoye.com", 0f, 0f, 1, "0757", "797660", 1604l, DateUtils.stringToDate("2016-03-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haizhen.yan", "闫海珍", "haizhen.yan@17zuoye.com", 0f, 0f, 1, "0768", "323876", 1599l, DateUtils.stringToDate("2016-03-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hao.chen.a", "陈浩A", "hao.chen.a@17zuoye.com", 0f, 0f, 1, "0771", "002152", 1600l, DateUtils.stringToDate("2016-03-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yadi.li", "李亚娣", "yadi.li@17zuoye.com", 0f, 0f, 1, "0773", "474577", 1600l, DateUtils.stringToDate("2016-03-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhongxing.liu", "刘中兴", "zhongxing.liu@17zuoye.com", 0f, 0f, 1, "0780", "148752", 1602l, DateUtils.stringToDate("2016-03-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bei.li", "李蓓", "bei.li@17zuoye.com", 0f, 0f, 1, "0784", "409247", 1600l, DateUtils.stringToDate("2016-03-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("youwen.zhang", "张友文", "youwen.zhang@17zuoye.com", 0f, 0f, 1, "0788", "366847", 1599l, DateUtils.stringToDate("2016-04-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("song.wang", "王松", "song.wang@17zuoye.com", 0f, 0f, 1, "0794", "109089", 1603l, DateUtils.stringToDate("2016-04-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.wang", "王艳", "yan.wang@17zuoye.com", 0f, 0f, 1, "0797", "449249", 1599l, DateUtils.stringToDate("2016-04-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuai.zhou", "周帅", "shuai.zhou@17zuoye.com", 0f, 0f, 1, "0843", "515027", 1605l, DateUtils.stringToDate("2016-04-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("long.ma.a", "马龙", "long.ma.a@17zuoye.com", 0f, 0f, 1, "0845", "385211", 1603l, DateUtils.stringToDate("2016-04-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yang.zhang", "张杨", "yang.zhang@17zuoye.com", 0f, 0f, 1, "0849", "818556", 1603l, DateUtils.stringToDate("2016-04-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ming.wan", "万明", "ming.wan@17zuoye.com", 0f, 0f, 1, "0854", "561769", 1599l, DateUtils.stringToDate("2016-04-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinquan.chen", "陈新权", "xinquan.chen@17zuoye.com", 0f, 0f, 1, "0855", "812109", 1599l, DateUtils.stringToDate("2016-04-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qian.han", "韩倩", "qian.han@17zuoye.com", 0f, 0f, 1, "0860", "010391", 1603l, DateUtils.stringToDate("2016-05-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rongyan.yao", "姚荣燕", "rongyan.yao@17zuoye.com", 0f, 0f, 1, "0864", "997604", 1599l, DateUtils.stringToDate("2016-05-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hongyue.fang", "方红月", "hongyue.fang@17zuoye.com", 0f, 0f, 1, "0866", "470597", 1599l, DateUtils.stringToDate("2016-05-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ronghua.lv", "吕荣华", "ronghua.lv@17zuoye.com", 0f, 0f, 1, "0871", "682817", 1599l, DateUtils.stringToDate("2016-05-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xu.liu.a", "刘旭A", "xu.liu.a@17zuoye.com", 0f, 0f, 1, "0876", "833173", 1601l, DateUtils.stringToDate("2016-05-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiyang.wang", "王晞阳", "xiyang.wang@17zuoye.com", 0f, 0f, 1, "0880", "150282", 1599l, DateUtils.stringToDate("2016-05-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tianbo.yang", "杨天博", "tianbo.yang@17zuoye.com", 0f, 0f, 1, "0884", "331933", 1603l, DateUtils.stringToDate("2016-04-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.wang", "王伟", "wei.wang@17zuoye.com", 0f, 0f, 1, "0890", "668509", 1603l, DateUtils.stringToDate("2016-04-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lu.wang", "王璐", "lu.wang@17zuoye.com", 0f, 0f, 1, "0909", "885423", 1603l, DateUtils.stringToDate("2016-05-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qiaorong.yi", "易桥容", "qiaorong.yi@17zuoye.com", 0f, 0f, 1, "0912", "586014", 1601l, DateUtils.stringToDate("2016-05-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lining.zhang", "张立宁", "lining.zhang@17zuoye.com", 0f, 0f, 1, "0920", "504680", 1604l, DateUtils.stringToDate("2016-06-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fan.wang", "王凡", "fan.wang@17zuoye.com", 0f, 0f, 1, "0927", "280597", 1599l, DateUtils.stringToDate("2016-06-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaofan.zhang", "张晓帆", "xiaofan.zhang@17zuoye.com", 0f, 0f, 1, "0928", "382385", 1601l, DateUtils.stringToDate("2016-06-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("sha.li", "李莎", "sha.li@1zuoye.com", 0f, 0f, 1, "0931", "136597", 1599l, DateUtils.stringToDate("2016-06-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("han.han", "韩晗", "han.han@17zuoye.com", 0f, 0f, 1, "0933", "234575", 1604l, DateUtils.stringToDate("2016-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("richard.chen", "陈亮", "richard.chen@17zuoye.com", 0f, 0f, 1, "0938", "708081", 1599l, DateUtils.stringToDate("2016-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jinglan.liu", "刘竞澜", "jinglan.liu@17zuoye.com", 0f, 0f, 1, "0941", "391226", 1599l, DateUtils.stringToDate("2016-06-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("nannan.li", "李楠楠", "nannan.li@17zuoye.com", 0f, 0f, 1, "0942", "272114", 1599l, DateUtils.stringToDate("2016-06-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yujie.zhang", "张煜杰", "yujie.zhang@17zuoye.com", 0f, 0f, 1, "0988", "978505", 1599l, DateUtils.stringToDate("2016-07-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.cui", "崔岩", "yan.cui@17zuoye.com", 0f, 0f, 1, "0992", "162475", 1605l, DateUtils.stringToDate("2016-06-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fugui.chang", "常富贵", "fugui.chang@17zuoye.com", 0f, 0f, 1, "0994", "086874", 1602l, DateUtils.stringToDate("2016-07-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoyu.geng", "耿晓雨", "xiaoyu.geng@17zuoye.com", 0f, 0f, 1, "0999", "422628", 1601l, DateUtils.stringToDate("2016-07-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lidong.wang", "王立冬", "lidong.wang@17zuoye.com", 0f, 0f, 1, "1001", "853916", 1601l, DateUtils.stringToDate("2016-07-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.jiang", "蒋炜", "wei.jiang@17zuoye.com", 0f, 0f, 1, "1002", "876923", 1603l, DateUtils.stringToDate("2016-07-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.luo", "罗莹", "ying.luo@17zuoye.com", 0f, 0f, 1, "1003", "727321", 1601l, DateUtils.stringToDate("2016-07-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.gao", "高玉", "yu.gao@17zuoye.com", 0f, 0f, 1, "1005", "297497", 1601l, DateUtils.stringToDate("2016-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jinping.xian", "线金平", "jinping.xian@17zuoye.com", 0f, 0f, 1, "1011", "310555", 1599l, DateUtils.stringToDate("2016-07-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunying.li", "李春影", "chunying.li@17zuoye.com", 0f, 0f, 1, "1018", "624874", 1601l, DateUtils.stringToDate("2016-07-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qi.wang", "王齐", "qi.wang@17zuoye.com", 0f, 0f, 1, "1020", "158302", 1600l, DateUtils.stringToDate("2016-07-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.xu", "徐颖", "ying.xu@17zuoye.com", 0f, 0f, 1, "1021", "634160", 1600l, DateUtils.stringToDate("2016-07-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fang.zhang", "张昉", "fang.zhang@17zuoye.com", 0f, 0f, 1, "1022", "404306", 1603l, DateUtils.stringToDate("2016-07-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shijia.li", "李诗佳", "shijia.li@17zuoye.com", 0f, 0f, 1, "1026", "394053", 1599l, DateUtils.stringToDate("2016-07-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tong.li", "李桐", "tong.li@17zuoye.com", 0f, 0f, 1, "1027", "456039", 1601l, DateUtils.stringToDate("2016-07-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junwei.chai", "柴俊伟", "junwei.chai@17zuoye.com", 0f, 0f, 1, "1029", "727604", 1601l, DateUtils.stringToDate("2016-07-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuxin.nie", "聂玉昕", "yuxin.nie@17zuoye.com", 0f, 0f, 1, "1090", "360141", 1602l, DateUtils.stringToDate("2016-06-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weiwei.zhao", "赵伟伟", "weiwei.zhao@17zuoye.com", 0f, 0f, 1, "1105", "780453", 1603l, DateUtils.stringToDate("2016-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qi.yue", "岳琪", "qi.yue@17zuoye.com", 0f, 0f, 1, "1121", "210112", 1603l, DateUtils.stringToDate("2016-07-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shanshan.zhao", "赵珊珊", "shanshan.zhao@17zuoye.com", 0f, 0f, 1, "1134", "914555", 1605l, DateUtils.stringToDate("2016-07-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.han", "韩鹏", "peng.han@17zuoye.com", 0f, 0f, 1, "1168", "650049", 1604l, DateUtils.stringToDate("2016-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhengang.cai", "蔡振刚", "zhengang.cai@17zuoye.com", 0f, 0f, 1, "1170", "218856", 1603l, DateUtils.stringToDate("2016-08-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingyao.qu", "屈静遥", "jingyao.qu@17zuoye.com", 0f, 0f, 1, "1175", "195276", 1599l, DateUtils.stringToDate("2016-08-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lin.cao", "曹琳", "lin.cao@17zuoye.com", 0f, 0f, 1, "1184", "115277", 1604l, DateUtils.stringToDate("2016-08-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.lai", "来妍", "yan.lai@17zuoye.com", 0f, 0f, 1, "1185", "624474", 1604l, DateUtils.stringToDate("2016-08-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("an.zhang", "张岸", "an.zhang@17zuoye.com", 0f, 0f, 1, "1186", "215058", 1601l, DateUtils.stringToDate("2016-08-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bolei.yao", "要博磊", "bolei.yao@17zuoye.com", 0f, 0f, 1, "1194", "112262", 1603l, DateUtils.stringToDate("2016-08-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yibing.xing", "邢逸冰", "yibing.xing@17zuoye.com", 0f, 0f, 1, "1198", "927441", 1603l, DateUtils.stringToDate("2016-08-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xingcui.shen", "申兴翠", "xingcui.shen@17zuoye.com", 0f, 0f, 1, "1204", "130716", 1599l, DateUtils.stringToDate("2016-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yang.zhang.a", "张阳A", "yang.zhang.a@17zuoye.com", 0f, 0f, 1, "1209", "945434", 1599l, DateUtils.stringToDate("2016-08-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shichao.wang.a", "王世超A", "shichao.wang.a@17zuoye.com", 0f, 0f, 1, "1236", "301818", 1601l, DateUtils.stringToDate("2016-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xueren.zhang", "张学认", "xueren.zhang@17zuoye.com", 0f, 0f, 1, "1240", "568172", 1602l, DateUtils.stringToDate("2016-08-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaohui.yuan", "袁晓慧", "xiaohui.yuan@17zuoye.com", 0f, 0f, 1, "1241", "916261", 1601l, DateUtils.stringToDate("2016-08-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.zhao", "赵雷", "lei.zhao@17zuoye.com", 0f, 0f, 1, "1242", "379699", 1601l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanpei.fang", "方炎培", "yanpei.fang@17zuoye.com", 0f, 0f, 1, "1248", "881514", 1604l, DateUtils.stringToDate("2016-09-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yueping.qian", "钱月平", "yueping.qian@17zuoye.com", 0f, 0f, 1, "1260", "854321", 1603l, DateUtils.stringToDate("2016-08-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanan.yao", "姚亚男", "yanan.yao@17zuoye.com", 0f, 0f, 1, "1262", "109937", 1603l, DateUtils.stringToDate("2016-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fang.ma", "马放", "fang.ma@17zuoye.com", 0f, 0f, 1, "1264", "558809", 1603l, DateUtils.stringToDate("2016-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("li.li.a", "李莉A", "li.li.a@17zuoye.com", 0f, 0f, 1, "1268", "733038", 1603l, DateUtils.stringToDate("2016-08-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinyu.huang", "黄鑫宇", "xinyu.huang@17zuoye.com", 0f, 0f, 1, "1269", "682148", 1603l, DateUtils.stringToDate("2016-08-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tao.lv", "吕涛", "tao.lv@17zuoye.com", 0f, 0f, 1, "1276", "060933", 1604l, DateUtils.stringToDate("2016-09-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingxiao.duan", "段景孝", "jingxiao.duan@17zuoye.com", 0f, 0f, 1, "1277", "587786", 1603l, DateUtils.stringToDate("2016-09-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zeyu.zhu", "朱泽宇", "zeyu.zhu@17zuoye.com", 0f, 0f, 1, "1279", "582281", 1601l, DateUtils.stringToDate("2016-09-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingming.zhao", "赵明明", "mingming.zhao@17zuoye.com", 0f, 0f, 1, "1281", "490645", 1599l, DateUtils.stringToDate("2016-09-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanni.li", "李燕妮", "yanni.li@17zuoye.com", 0f, 0f, 1, "1283", "351732", 1601l, DateUtils.stringToDate("2016-09-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jialin.chen", "陈佳林", "jialin.chen@17zuoye.com", 0f, 0f, 1, "1288", "266359", 1603l, DateUtils.stringToDate("2016-09-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xu.tang", "汤旭", "xu.tang@17zuoye.com", 0f, 0f, 1, "1291", "004208", 1603l, DateUtils.stringToDate("2016-09-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunmei.liu", "刘春梅", "chunmei.liu@17zuoye.com", 0f, 0f, 1, "1319", "012300", 1601l, DateUtils.stringToDate("2016-09-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guochen.liang", "梁国臣", "guochen.liang@17zuoye.com", 0f, 0f, 1, "1327", "821286", 1601l, DateUtils.stringToDate("2016-10-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huang.huang", "黄璜", "huang.huang@17zuoye.com", 0f, 0f, 1, "1330", "731039", 1601l, DateUtils.stringToDate("2016-10-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiangyan.li", "李江燕", "jiangyan.li@17zuoye.com", 0f, 0f, 1, "1333", "850122", 1603l, DateUtils.stringToDate("2016-09-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ziqi.feng", "冯子奇", "ziqi.feng@17zuoye.com", 0f, 0f, 1, "1337", "222766", 1603l, DateUtils.stringToDate("2016-10-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinxin.feng", "冯鑫鑫", "xinxin.feng@17zuoye.com", 0f, 0f, 1, "1361", "870020", 1605l, DateUtils.stringToDate("2016-10-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tingzhen.lin", "林珽震", "tingzhen.lin@17zuoye.com", 0f, 0f, 1, "1362", "193931", 1605l, DateUtils.stringToDate("2016-10-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qiushi.xu", "徐秋实", "qiushi.xu@17zuoye.com", 0f, 0f, 1, "1363", "563895", 1599l, DateUtils.stringToDate("2016-10-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cheng.chen", "陈诚", "cheng.chen@17zuoye.com", 0f, 0f, 1, "1365", "986119", 1605l, DateUtils.stringToDate("2016-10-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("caihong.cao", "曹彩虹", "caihong.cao@17zuoye.com", 0f, 0f, 1, "1367", "585810", 1604l, DateUtils.stringToDate("2016-10-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junlin.yan", "闫俊霖", "junlin.yan@17zuoye.com", 0f, 0f, 1, "1369", "012614", 1605l, DateUtils.stringToDate("2016-10-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shanshan.zhong", "仲珊珊", "shanshan.zhong@17zuoye.com", 0f, 0f, 1, "1375", "855310", 1601l, DateUtils.stringToDate("2016-10-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lianglei.li", "李良磊", "lianglei.li@17zuoye.com", 0f, 0f, 1, "1381", "295579", 1603l, DateUtils.stringToDate("2016-10-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("honglu.dong", "董红璐", "honglu.dong@17zuoye.com", 0f, 0f, 1, "1382", "727226", 1603l, DateUtils.stringToDate("2016-10-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuangjiang.li.a", "李双江A", "shuangjiang.li.a@17zuoye.com", 0f, 0f, 1, "1383", "189992", 1603l, DateUtils.stringToDate("2016-10-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hailiang.fan", "范海亮", "hailiang.fan@17zuoye.com", 0f, 0f, 1, "1384", "470571", 1603l, DateUtils.stringToDate("2016-10-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dongxu.yu", "于东旭", "dongxu.yu@17zuoye.com", 0f, 0f, 1, "1386", "619131", 1603l, DateUtils.stringToDate("2016-10-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bokai.sun", "孙铂凯", "bokai.sun@17zuoye.com", 0f, 0f, 1, "1387", "650057", 1603l, DateUtils.stringToDate("2016-10-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lina.gao", "高立娜", "lina.gao@17zuoye.com", 0f, 0f, 1, "1388", "987581", 1603l, DateUtils.stringToDate("2016-10-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.li.a", "李燕A", "yan.li.a@17zuoye.com", 0f, 0f, 1, "1390", "980773", 1600l, DateUtils.stringToDate("2016-10-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rong.liu", "刘荣", "rong.liu@17zuoye.com", 0f, 0f, 1, "1394", "917572", 1603l, DateUtils.stringToDate("2016-10-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kunpeng.li", "李锟鹏", "kunpeng.li@17zuoye.com", 0f, 0f, 1, "1404", "793604", 1599l, DateUtils.stringToDate("2016-11-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingfei.duan", "段铭飞", "mingfei.duan@17zuoye.com", 0f, 0f, 1, "1410", "882482", 1601l, DateUtils.stringToDate("2016-11-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhijie.zhu", "朱志杰", "zhijie.zhu@17zuoye.com", 0f, 0f, 1, "1418", "268662", 1601l, DateUtils.stringToDate("2016-11-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.meng", "孟宇", "yu.meng@17zuoye.com", 0f, 0f, 1, "1422", "263787", 1599l, DateUtils.stringToDate("2016-11-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuai.zhang", "张帅", "shuai.zhang@17zuoye.com", 0f, 0f, 1, "1429", "470946", 1601l, DateUtils.stringToDate("2016-11-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuanyuan.wang", "王园园", "yuanyuan.wang@17zuoye.com", 0f, 0f, 1, "1432", "324646", 1599l, DateUtils.stringToDate("2016-11-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingjing.guo", "郭晶晶", "jingjing.guo@17zuoye.com", 0f, 0f, 1, "1452", "444668", 1601l, DateUtils.stringToDate("2016-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ruiying.xu", "徐瑞莹", "ruiying.xu@17zuoye.com", 0f, 0f, 1, "1454", "363162", 1603l, DateUtils.stringToDate("2016-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.zhang.a", "张雷", "lei.zhang.a@17zuoye.com", 0f, 0f, 1, "1458", "375685", 1605l, DateUtils.stringToDate("2016-11-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.zhang.b", "张晶", "jing.zhang.b@17zuoye.com", 0f, 0f, 1, "1463", "661145", 1600l, DateUtils.stringToDate("2016-12-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huishu.wang", "王慧姝", "huishu.wang@17zuoye.com", 0f, 0f, 1, "1465", "776413", 1605l, DateUtils.stringToDate("2016-12-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("gengsheng.zhao", "赵庚生", "gengsheng.zhao@17zuoye.com", 0f, 0f, 1, "1466", "408249", 1605l, DateUtils.stringToDate("2016-12-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianbin.zheng", "郑建彬", "jianbin.zheng@17zuoye.com", 0f, 0f, 1, "1469", "030987", 1601l, DateUtils.stringToDate("2016-12-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hongyu.chen", "陈宏玉", "hongyu.chen@17zuoye.com", 0f, 0f, 1, "1474", "677165", 1603l, DateUtils.stringToDate("2016-12-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wanqiu.xia", "夏婉秋", "wanqiu.xia@17zuoye.com", 0f, 0f, 1, "1475", "625030", 1601l, DateUtils.stringToDate("2016-12-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lu.chen", "陈露", "lu.chen@17zuoye.com", 0f, 0f, 1, "1478", "613258", 1605l, DateUtils.stringToDate("2016-12-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huihui.li", "李慧慧", "huihui.li@17zuoye.com", 0f, 0f, 1, "1483", "089506", 1603l, DateUtils.stringToDate("2016-12-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qianyu.zhao", "赵乾宇", "qianyu.zhao@17zuoye.com", 0f, 0f, 1, "1485", "572120", 1605l, DateUtils.stringToDate("2016-12-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shan.wang", "王珊", "shan.wang@17zuoye.com", 0f, 0f, 1, "1488", "238653", 1603l, DateUtils.stringToDate("2016-12-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jie.ren", "任洁", "jie.ren@17zuoye.com", 0f, 0f, 1, "1491", "901962", 1601l, DateUtils.stringToDate("2016-12-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinrui.song", "宋芯蕊", "xinrui.song@17zuoye.com", 0f, 0f, 1, "1492", "940858", 1601l, DateUtils.stringToDate("2016-12-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("li.zhang", "张丽", "li.zhang@17zuoye.com", 0f, 0f, 1, "1493", "813080", 1601l, DateUtils.stringToDate("2016-12-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaowei.liu", "刘晓威", "xiaowei.liu@17zuoye.com", 0f, 0f, 1, "1495", "171484", 1605l, DateUtils.stringToDate("2016-12-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wanglong.shi", "石望龙", "wanglong.shi@17zuoye.com", 0f, 0f, 1, "1497", "704445", 1605l, DateUtils.stringToDate("2016-12-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xina.gao", "高西娜", "xina.gao@17zuoye.com", 0f, 0f, 1, "1498", "780282", 1601l, DateUtils.stringToDate("2016-12-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cong.yu", "余聪", "cong.yu@17zuoye.com", 0f, 0f, 1, "1509", "966449", 1599l, DateUtils.stringToDate("2016-12-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dong.xue", "薛冬", "dong.xue@17zuoye.com", 0f, 0f, 1, "1512", "697294", 1605l, DateUtils.stringToDate("2017-01-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bin.yuan", "袁斌", "bin.yuan@17zuoye.com", 0f, 0f, 1, "1513", "026839", 1601l, DateUtils.stringToDate("2017-01-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fei.li", "李飞", "fei.li@17zuoye.com", 0f, 0f, 1, "1514", "710784", 1601l, DateUtils.stringToDate("2017-01-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhiwei.fan", "樊志伟", "zhiwei.fan@17zuoye.com", 0f, 0f, 1, "1517", "681382", 1601l, DateUtils.stringToDate("2017-01-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wanying.wang", "王婉莹", "wanying.wang@17zuoye.com", 0f, 0f, 1, "1518", "548849", 1599l, DateUtils.stringToDate("2017-01-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuan.yao", "姚远", "yuan.yao@17zuoye.com", 0f, 0f, 1, "1519", "579924", 1601l, DateUtils.stringToDate("2017-01-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dan.wang", "王丹", "dan.wang@17zuoye.com", 0f, 0f, 1, "1520", "328716", 1601l, DateUtils.stringToDate("2017-01-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaolei.qi", "齐孝磊", "xiaolei.qi@17zuoye.com", 0f, 0f, 1, "1521", "066420", 1601l, DateUtils.stringToDate("2017-01-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.wang.b", "王玉", "yu.wang.b@17zuoye.com", 0f, 0f, 1, "1523", "044871", 1601l, DateUtils.stringToDate("2017-01-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhijun.yu", "于志军", "zhijun.yu@17zuoye.com", 0f, 0f, 1, "1527", "600801", 1602l, DateUtils.stringToDate("2017-02-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fengyu.wang", "王凤宇", "fengyu.wang@17zuoye.com", 0f, 0f, 1, "1528", "626085", 1599l, DateUtils.stringToDate("2017-02-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yang.gao", "高阳", "yang.gao@17zuoye.com", 0f, 0f, 1, "1529", "237723", 1601l, DateUtils.stringToDate("2017-02-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("sui.chen", "陈岁", "sui.chen@17zuoye.com", 0f, 0f, 1, "1534", "108699", 1605l, DateUtils.stringToDate("2017-02-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dongdong.wu", "吴冬冬", "dongdong.wu@17zuoye.com", 0f, 0f, 1, "1536", "013442", 1605l, DateUtils.stringToDate("2017-02-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingjing.yang", "杨婧婧", "jingjing.yang@17zuoye.com", 0f, 0f, 1, "1537", "082936", 1599l, DateUtils.stringToDate("2017-02-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhaoyi.li", "李昭忆", "zhaoyi.li@17zuoye.com", 0f, 0f, 1, "1540", "011247", 1603l, DateUtils.stringToDate("2017-02-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shan.wang.a", "王珊A", "shan.wang.a@17zuoye.com", 0f, 0f, 1, "1541", "575863", 1603l, DateUtils.stringToDate("2017-02-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.wang.b", "王鹏B", "peng.wang.b@17zuoye.com", 0f, 0f, 1, "1545", "468122", 1601l, DateUtils.stringToDate("2017-02-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.yang", "杨磊", "lei.yang@17zuoye.com", 0f, 0f, 1, "1547", "680194", 1605l, DateUtils.stringToDate("2017-02-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fan.wang.b", "王凡B", "fan.wang.b@17zuoye.com", 0f, 0f, 1, "1548", "619762", 1605l, DateUtils.stringToDate("2017-02-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.sun", "孙炎", "yan.sun@17zuoye.com", 0f, 0f, 1, "1549", "984717", 1605l, DateUtils.stringToDate("2017-02-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuangshuang.yu", "喻双双", "shuangshuang.yu@17zuoye.com", 0f, 0f, 1, "1550", "409956", 1601l, DateUtils.stringToDate("2017-02-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xin.wei", "魏鑫", "xin.wei@17zuoye.com", 0f, 0f, 1, "1551", "723296", 1605l, DateUtils.stringToDate("2017-02-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shangqi.pan", "潘尚奇", "shangqi.pan@17zuoye.com", 0f, 0f, 1, "1552", "478021", 1605l, DateUtils.stringToDate("2017-02-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuo.ning", "宁硕", "shuo.ning@17zuoye.com", 0f, 0f, 1, "1558", "283810", 1605l, DateUtils.stringToDate("2017-02-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuan.liu", "刘源", "yuan.liu@17zuoye.com", 0f, 0f, 1, "1561", "394158", 1601l, DateUtils.stringToDate("2017-02-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lili.yao", "姚丽丽", "lili.yao@17zuoye.com", 0f, 0f, 1, "1562", "085576", 1603l, DateUtils.stringToDate("2017-02-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shenshen.bian", "卞申申", "shenshen.bian@17zuoye.com", 0f, 0f, 1, "1563", "533368", 1603l, DateUtils.stringToDate("2017-02-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yingqi.wang", "王英骐", "yingqi.wang@17zuoye.com", 0f, 0f, 1, "1564", "157980", 1603l, DateUtils.stringToDate("2017-02-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenxia.mu", "穆文霞", "wenxia.mu@17zuoye.com", 0f, 0f, 1, "1565", "237278", 1603l, DateUtils.stringToDate("2017-02-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liying.zhong", "钟立影", "liying.zhong@17zuoye.com", 0f, 0f, 1, "1576", "376483", 1599l, DateUtils.stringToDate("2017-03-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("daqing.pan", "潘大庆", "daqing.pan@17zuoye.com", 0f, 0f, 1, "1582", "531381", 1601l, DateUtils.stringToDate("2017-03-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuan.wang.a", "王轩A", "xuan.wang.a@17zuoye.com", 0f, 0f, 1, "1583", "963457", 1602l, DateUtils.stringToDate("2017-03-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhiyuan.yang", "杨志远", "zhiyuan.yang@17zuoye.com", 0f, 0f, 1, "1585", "154075", 1602l, DateUtils.stringToDate("2017-03-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ye.wang", "王野", "ye.wang@17zuoye.com", 0f, 0f, 1, "1587", "172956", 1604l, DateUtils.stringToDate("2017-03-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meizi.jin", "金美子", "meizi.jin@17zuoye.com", 0f, 0f, 1, "1588", "398303", 1599l, DateUtils.stringToDate("2017-03-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("sijia.cai", "蔡斯嘉", "sijia.cai@17zuoye.com", 0f, 0f, 1, "1590", "595118", 1603l, DateUtils.stringToDate("2017-03-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("feng.xiang", "向峰", "feng.xiang@17zuoye.com", 0f, 0f, 1, "1594", "371985", 1601l, DateUtils.stringToDate("2017-03-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoqing.duan", "段晓庆", "xiaoqing.duan@17zuoye.com", 0f, 0f, 1, "1611", "667638", 1603l, DateUtils.stringToDate("2017-03-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("runzhi.kang", "康润芝", "runzhi.kang@17zuoye.com", 0f, 0f, 1, "1612", "394972", 1603l, DateUtils.stringToDate("2017-03-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bingyao.li", "李冰瑶", "bingyao.li@17zuoye.com", 0f, 0f, 1, "1615", "090199", 1603l, DateUtils.stringToDate("2017-03-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhihong.li", "李志宏", "zhihong.li@17zuoye.com", 0f, 0f, 1, "1624", "354752", 1601l, DateUtils.stringToDate("2017-04-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weinan.piao", "朴玮南", "weinan.piao@17zuoye.com", 0f, 0f, 1, "1626", "184657", 1602l, DateUtils.stringToDate("2018-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dong.wang", "王东", "dong.wang@17zuoye.com", 0f, 0f, 1, "1627", "758065", 1603l, DateUtils.stringToDate("2017-04-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("miao.he", "何淼", "miao.he@17zuoye.com", 0f, 0f, 1, "1629", "451166", 1603l, DateUtils.stringToDate("2017-04-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liqian.kang", "康利倩", "liqian.kang@17zuoye.com", 0f, 0f, 1, "1632", "851830", 1603l, DateUtils.stringToDate("2017-03-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qiaolin.wang", "王巧琳", "qiaolin.wang@17zuoye.com", 0f, 0f, 1, "1641", "551817", 1601l, DateUtils.stringToDate("2017-04-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lianrui.chu", "楚连瑞", "lianrui.chu@17zuoye.com", 0f, 0f, 1, "1642", "761706", 1599l, DateUtils.stringToDate("2017-04-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.luo", "罗朋", "peng.luo@17zuoye.com", 0f, 0f, 1, "1661", "421947", 1602l, DateUtils.stringToDate("2017-05-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiao.zhang", "张娇", "jiao.zhang@17zuoye.com", 0f, 0f, 1, "1668", "741703", 1599l, DateUtils.stringToDate("2017-05-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("siyang.chen", "陈思杨", "siyang.chen@17zuoye.com", 0f, 0f, 1, "1669", "025027", 1599l, DateUtils.stringToDate("2017-05-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yiran.gao", "高一然", "yiran.gao@17zuoye.com", 0f, 0f, 1, "1673", "600123", 1599l, DateUtils.stringToDate("2017-05-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xu.shao", "邵旭", "xu.shao@17zuoye.com", 0f, 0f, 1, "1674", "808494", 1601l, DateUtils.stringToDate("2017-05-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bingjie.jiang", "江秉劼", "bingjie.jiang@17zuoye.com", 0f, 0f, 1, "1676", "381686", 1605l, DateUtils.stringToDate("2017-05-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mu.tian", "田木", "mu.tian@17zuoye.com", 0f, 0f, 1, "1677", "525635", 1602l, DateUtils.stringToDate("2017-05-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yue.qi.a", "齐悦", "yue.qi.a@17zuoye.com", 0f, 0f, 1, "1679", "405497", 1601l, DateUtils.stringToDate("2017-05-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ting.gao", "高婷", "ting.gao@17zuoye.com", 0f, 0f, 1, "1683", "723276", 1601l, DateUtils.stringToDate("2017-05-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lihong.ding", "丁丽红", "lihong.ding@17zuoye.com", 0f, 0f, 1, "1684", "527504", 1599l, DateUtils.stringToDate("2017-05-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hongjie.wan", "万鸿洁", "hongjie.wan@17zuoye.com", 0f, 0f, 1, "1691", "328732", 1601l, DateUtils.stringToDate("2017-05-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dan.wang.b", "王丹B", "dan.wang.b@17zuoye.com", 0f, 0f, 1, "1692", "992348", 1599l, DateUtils.stringToDate("2017-05-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("feng.rao", "饶丰", "feng.rao@17zuoye.com", 0f, 0f, 1, "1696", "031970", 1599l, DateUtils.stringToDate("2017-05-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("simiao.lou", "娄思邈", "simiao.lou@17zuoye.com", 0f, 0f, 1, "1704", "300821", 1599l, DateUtils.stringToDate("2017-05-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("siyi.peng", "彭斯宜", "siyi.peng@17zuoye.com", 0f, 0f, 1, "1710", "971612", 1599l, DateUtils.stringToDate("2017-06-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meixin.sun", "孙美欣", "meixin.sun@17zuoye.com", 0f, 0f, 1, "1712", "783182", 1601l, DateUtils.stringToDate("2017-06-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.hao.a", "郝静A", "jing.hao.a@17zuoye.com", 0f, 0f, 1, "1713", "411133", 1601l, DateUtils.stringToDate("2017-06-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuangjun.li", "李双君", "shuangjun.li@17zuoye.com", 0f, 0f, 1, "1716", "266690", 1601l, DateUtils.stringToDate("2017-06-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount(" jinyu.dong", "董金玉", " jinyu.dong@17zuoye.com", 0f, 0f, 1, "1720", "997737", 1605l, DateUtils.stringToDate("2017-06-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.zhang.a", "张玉A", "yu.zhang.a@17zuoye.com", 0f, 0f, 1, "1721", "228301", 1599l, DateUtils.stringToDate("2017-06-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yingxing.ji", "纪影星", "yingxing.ji@17zuoye.com", 0f, 0f, 1, "1724", "168405", 1599l, DateUtils.stringToDate("2017-06-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kui.zhang", "张魁", "kui.zhang@17zuoye.com", 0f, 0f, 1, "1725", "566687", 1605l, DateUtils.stringToDate("2017-06-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junyang.zhang", "张君羊", "junyang.zhang@17zuoye.com", 0f, 0f, 1, "1726", "320351", 1603l, DateUtils.stringToDate("2017-06-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chang.xu.a", "徐畅A", "chang.xu.a@17zuoye.com", 0f, 0f, 1, "1728", "020856", 1604l, DateUtils.stringToDate("2017-06-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yawen.du", "杜亚文", "yawen.du@17zuoye.com", 0f, 0f, 1, "1730", "453410", 1599l, DateUtils.stringToDate("2017-06-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.zhang", "张艳", "yan.zhang@17zuoye.com", 0f, 0f, 1, "1735", "865262", 1599l, DateUtils.stringToDate("2017-06-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lin.dai", "戴琳", "lin.dai@17zuoye.com", 0f, 0f, 1, "1738", "169535", 1599l, DateUtils.stringToDate("2017-06-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zheng.ma", "马铮", "zheng.ma@17zuoye.com", 0f, 0f, 1, "1741", "932504", 1602l, DateUtils.stringToDate("2017-06-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunbo.he", "何春波", "chunbo.he@17zuoye.com", 0f, 0f, 1, "1743", "666867", 1603l, DateUtils.stringToDate("2017-06-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qin.wu", "吴勤", "qin.wu@17zuoye.com", 0f, 0f, 1, "1744", "573077", 1603l, DateUtils.stringToDate("2017-06-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiyou.wang", "王西友", "xiyou.wang@17zuoye.com", 0f, 0f, 1, "1775", "213474", 1602l, DateUtils.stringToDate("2017-06-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiaxiang.wang", "王佳祥", "jiaxiang.wang@17zuoye.com", 0f, 0f, 1, "1777", "517951", 1602l, DateUtils.stringToDate("2017-06-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yang.yang", "杨阳", "yang.yang@17zuoye.com", 0f, 0f, 1, "1785", "957993", 1601l, DateUtils.stringToDate("2017-06-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tao.song", "宋涛", "tao.song@17zuoye.com", 0f, 0f, 1, "1789", "697269", 1599l, DateUtils.stringToDate("2017-06-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("congjian.fan", "范聪建", "congjian.fan@17zuoye.com", 0f, 0f, 1, "1791", "510314", 1601l, DateUtils.stringToDate("2017-06-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("senhao.niu", "牛森浩", "senhao.niu@17zuoye.com", 0f, 0f, 1, "1792", "553002", 1599l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.xue", "薛靖", "jing.xue@17zuoye.com", 0f, 0f, 1, "1793", "722946", 1603l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.wang.b", "王唯", "wei.wang.b@17zuoye.com", 0f, 0f, 1, "1794", "574122", 1599l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xing.wang.a", "王星", "xing.wang.a@17zuoye.com", 0f, 0f, 1, "1796", "511703", 1602l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.wang.a", "王宇", "yu.wang.a@17zuoye.com", 0f, 0f, 1, "1797", "964867", 1599l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinghao.qi", "齐兴豪", "xinghao.qi@17zuoye.com", 0f, 0f, 1, "1799", "325233", 1599l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dandan.song", "宋丹丹", "dandan.song@17zuoye.com", 0f, 0f, 1, "1801", "867644", 1601l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tongtong.liu", "刘彤彤", "tongtong.liu@17zuoye.com", 0f, 0f, 1, "1802", "962428", 1601l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.yuan", "原静", "jing.yuan@17zuoye.com", 0f, 0f, 1, "1810", "909027", 1599l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dan.zhou", "周丹", "dan.zhou@17zuoye.com", 0f, 0f, 1, "1812", "642273", 1603l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuilian.yu", "于水连", "shuilian.yu@17zuoye.com", 0f, 0f, 1, "1815", "152589", 1599l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jialu.li", "李嘉禄", "jialu.li@17zuoye.com", 0f, 0f, 1, "1816", "810006", 1602l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dongxue.zhao", "赵冬雪", "dongxue.zhao@17zuoye.com", 0f, 0f, 1, "1820", "100098", 1599l, DateUtils.stringToDate("2017-07-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tianshu.zhang", "张天舒", "tianshu.zhang@17zuoye.com", 0f, 0f, 1, "1825", "671574", 1599l, DateUtils.stringToDate("2017-07-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuanxiu.ren", "任拴秀", "shuanxiu.ren@17zuoye.com", 0f, 0f, 1, "1826", "826308", 1601l, DateUtils.stringToDate("2017-07-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lingling.xiang", "项玲玲", "lingling.xiang@17zuoye.com", 0f, 0f, 1, "1827", "614685", 1599l, DateUtils.stringToDate("2017-07-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("nuonan.tan", "谭诺男", "nuonan.tan@17zuoye.com", 0f, 0f, 1, "1830", "269151", 1601l, DateUtils.stringToDate("2017-07-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yue.yang", "杨越", "yue.yang@17zuoye.com", 0f, 0f, 1, "1832", "469383", 1599l, DateUtils.stringToDate("2017-07-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shilan.wang", "王诗兰", "shilan.wang@17zuoye.com", 0f, 0f, 1, "1834", "402199", 1602l, DateUtils.stringToDate("2017-07-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoxia.liu", "刘晓霞", "xiaoxia.liu@17zuoye.com", 0f, 0f, 1, "1836", "358096", 1603l, DateUtils.stringToDate("2017-07-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaotong.ye", "叶小同", "xiaotong.ye@17zuoye.com", 0f, 0f, 1, "1837", "400903", 1601l, DateUtils.stringToDate("2017-07-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiamin.shi", "石佳敏", "jiamin.shi@17zuoye.com", 0f, 0f, 1, "1839", "039712", 1602l, DateUtils.stringToDate("2017-07-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuhao.wang", "王宇豪", "yuhao.wang@17zuoye.com", 0f, 0f, 1, "1842", "837412", 1602l, DateUtils.stringToDate("2017-07-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tinggang.zhao", "赵廷港", "tinggang.zhao@17zuoye.com", 0f, 0f, 1, "1843", "824784", 1599l, DateUtils.stringToDate("2017-07-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zihan.wang.a", "王紫涵", "zihan.wang.a@17zuoye.com", 0f, 0f, 1, "1845", "502229", 1601l, DateUtils.stringToDate("2017-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.cao", "曹玮", "wei.cao@17zuoye.com", 0f, 0f, 1, "1846", "360226", 1599l, DateUtils.stringToDate("2017-07-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.zhang.d", "张伟D", "wei.zhang.d@17zuoye.com", 0f, 0f, 1, "1854", "605196", 1605l, DateUtils.stringToDate("2017-07-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("pengmin.chen", "陈鹏敏", "pengmin.chen@17zuoye.com", 0f, 0f, 1, "1859", "189442", 1603l, DateUtils.stringToDate("2017-07-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kui.xue", "薛葵", "kui.xue@17zuoye.com", 0f, 0f, 1, "1860", "735999", 1600l, DateUtils.stringToDate("2017-07-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenliang.li", "李文亮", "wenliang.li@17zuoye.com", 0f, 0f, 1, "1861", "254679", 1599l, DateUtils.stringToDate("2017-07-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuyu.lu", "路玉玉", "yuyu.lu@17zuoye.com", 0f, 0f, 1, "1862", "708056", 1601l, DateUtils.stringToDate("2017-07-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuming.liao", "廖育铭", "yuming.liao@17zuoye.com", 0f, 0f, 1, "1938", "808910", 1602l, DateUtils.stringToDate("2017-07-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shumin.liu", "刘淑敏", "shumin.liu@17zuoye.com", 0f, 0f, 1, "1939", "096658", 1605l, DateUtils.stringToDate("2017-08-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fangfang.ren", "任芳芳", "fangfang.ren@17zuoye.com", 0f, 0f, 1, "1941", "282671", 1599l, DateUtils.stringToDate("2017-08-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bo.xu", "徐博", "bo.xu@17zuoye.com", 0f, 0f, 1, "1942", "688419", 1603l, DateUtils.stringToDate("2017-08-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingyuan.zhao", "赵静远", "jingyuan.zhao@17zuoye.com", 0f, 0f, 1, "1943", "195131", 1603l, DateUtils.stringToDate("2017-08-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanyan.zhao", "赵燕燕", "yanyan.zhao@17zuoye.com", 0f, 0f, 1, "1946", "232135", 1601l, DateUtils.stringToDate("2017-08-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ling.pu", "蒲玲", "ling.pu@17zuoye.com", 0f, 0f, 1, "1947", "271451", 1599l, DateUtils.stringToDate("2017-08-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("pei.ouyang", "欧阳霈", "pei.ouyang@17zuoye.com", 0f, 0f, 1, "1948", "424608", 1599l, DateUtils.stringToDate("2017-08-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yinping.kong", "孔寅平", "yinping.kong@17zuoye.com", 0f, 0f, 1, "1950", "503373", 1603l, DateUtils.stringToDate("2017-08-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("changxue.ma", "马长雪", "changxue.ma@17zuoye.com", 0f, 0f, 1, "2019", "170716", 1605l, DateUtils.stringToDate("2017-08-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fanshuo.meng", "孟繁硕", "fanshuo.meng@17zuoye.com", 0f, 0f, 1, "2022", "591684", 1603l, DateUtils.stringToDate("2017-08-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("siqi.jiang", "姜思齐", "siqi.jiang@17zuoye.com", 0f, 0f, 1, "2025", "858200", 1603l, DateUtils.stringToDate("2017-08-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chao.li", "李超", "chao.li@17zuoye.com", 0f, 0f, 1, "2026", "170500", 1603l, DateUtils.stringToDate("2017-08-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinying.man", "满鑫颖", "xinying.man@17zuoye.com", 0f, 0f, 1, "2027", "339787", 1603l, DateUtils.stringToDate("2017-08-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuanyuan.du", "杜婵媛", "yuanyuan.du@17zuoye.com", 0f, 0f, 1, "2028", "016479", 1603l, DateUtils.stringToDate("2017-08-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jigang.liu", "刘继钢", "jigang.liu@17zuoye.com", 0f, 0f, 1, "2029", "696024", 1603l, DateUtils.stringToDate("2017-08-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuang.liu", "刘爽", "shuang.liu@17zuoye.com", 0f, 0f, 1, "2031", "207161", 1603l, DateUtils.stringToDate("2017-08-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.liu.b", "刘宇", "yu.liu.b@17zuoye.com", 0f, 0f, 1, "2033", "087374", 1603l, DateUtils.stringToDate("2017-08-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("piao.huang", "黄飘", "piao.huang@17zuoye.com", 0f, 0f, 1, "2037", "377395", 1603l, DateUtils.stringToDate("2017-08-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jialin.yu", "于佳琳", "jialin.yu@17zuoye.com", 0f, 0f, 1, "2041", "901387", 1603l, DateUtils.stringToDate("2017-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("na.li.a", "李娜", "na.li.a@17zuoye.com", 0f, 0f, 1, "2042", "420332", 1603l, DateUtils.stringToDate("2017-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("li.zhang.c", "张丽丽", "li.zhang.c@17zuoye.com", 0f, 0f, 1, "2043", "510250", 1603l, DateUtils.stringToDate("2017-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shulan.wei", "魏书兰", "shulan.wei@17zuoye.com", 0f, 0f, 1, "2046", "241172", 1603l, DateUtils.stringToDate("2017-08-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.yang", "杨鹰", "ying.yang@17zuoye.com", 0f, 0f, 1, "2048", "274718", 1601l, DateUtils.stringToDate("2017-09-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lili.ren", "任丽丽", "lili.ren@17zuoye.com", 0f, 0f, 1, "2050", "204223", 1601l, DateUtils.stringToDate("2017-09-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingjing.guo.a", "郭晶晶A", "jingjing.guo.a@17zuoye.com", 0f, 0f, 1, "2052", "260627", 1601l, DateUtils.stringToDate("2017-09-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaonan.zhi", "智小楠", "xiaonan.zhi@17zuoye.com", 0f, 0f, 1, "2054", "014116", 1599l, DateUtils.stringToDate("2017-09-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shoulin.lin", "林守林", "shoulin.lin@17zuoye.com", 0f, 0f, 1, "2055", "975050", 1602l, DateUtils.stringToDate("2017-09-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jun.guo", "郭俊", "jun.guo@17zuoye.com", 0f, 0f, 1, "2057", "940456", 1599l, DateUtils.stringToDate("2017-09-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaohua.zhai", "翟晓华", "xiaohua.zhai@17zuoye.com", 0f, 0f, 1, "2059", "102506", 1603l, DateUtils.stringToDate("2017-08-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("boshi.zhang", "张博士", "boshi.zhang@17zuoye.com", 0f, 0f, 1, "2060", "180387", 1603l, DateUtils.stringToDate("2017-08-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shengjia.yan", "严晟嘉", "shengjia.yan@17zuoye.com", 0f, 0f, 1, "2064", "906615", 1599l, DateUtils.stringToDate("2017-09-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("siyu.xia", "夏斯宇", "siyu.xia@17zuoye.com", 0f, 0f, 1, "2065", "444512", 1603l, DateUtils.stringToDate("2017-09-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuanyuan.bai", "白园园", "yuanyuan.bai@17zuoye.com", 0f, 0f, 1, "2075", "748063", 1599l, DateUtils.stringToDate("2017-10-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haochen.lu", "陆昊辰", "haochen.lu@17zuoye.com", 0f, 0f, 1, "2092", "898417", 1604l, DateUtils.stringToDate("2017-11-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qimeng.ning", "宁琦梦", "qimeng.ning@17zuoye.com", 0f, 0f, 1, "2093", "557899", 1601l, DateUtils.stringToDate("2017-10-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ran.nie", "聂冉", "ran.nie@17zuoye.com", 0f, 0f, 1, "2095", "466968", 1602l, DateUtils.stringToDate("2017-11-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiang.li.a", "李想", "xiang.li.a@17zuoye.com", 0f, 0f, 1, "2096", "770001", 1603l, DateUtils.stringToDate("2017-11-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanling.ma", "马艳玲", "yanling.ma@17zuoye.com", 0f, 0f, 1, "2112", "523278", 1605l, DateUtils.stringToDate("2017-11-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yaxiang.zhao", "赵亚香", "yaxiang.zhao@17zuoye.com", 0f, 0f, 1, "2113", "977225", 1603l, DateUtils.stringToDate("2017-11-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaolei.sun", "孙小蕾", "xiaolei.sun@17zuoye.com", 0f, 0f, 1, "2117", "100640", 1601l, DateUtils.stringToDate("2017-12-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zekui.jin", "金泽奎", "zekui.jin@17zuoye.com", 0f, 0f, 1, "2123", "071276", 1602l, DateUtils.stringToDate("2019-01-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fengjuan.song", "宋凤娟", "fengjuan.song@17zuoye.com", 0f, 0f, 1, "2124", "913601", 1605l, DateUtils.stringToDate("2017-12-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haoran.wang.a", "王浩然A", "haoran.wang.a@17zuoye.com", 0f, 0f, 1, "2125", "008957", 1603l, DateUtils.stringToDate("2017-12-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinbin.li", "李新彬", "xinbin.li@17zuoye.com", 0f, 0f, 1, "2126", "617610", 1601l, DateUtils.stringToDate("2017-12-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tong.wu", "吴桐", "tong.wu@17zuoye.com", 0f, 0f, 1, "2132", "620321", 1605l, DateUtils.stringToDate("2017-12-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenkai.yang", "杨文凯", "wenkai.yang@17zuoye.com", 0f, 0f, 1, "2133", "541189", 1601l, DateUtils.stringToDate("2017-12-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lianhua.li", "李连华", "lianhua.li@17zuoye.com", 0f, 0f, 1, "2134", "609539", 1599l, DateUtils.stringToDate("2017-12-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("sikai.zhang", "张嗣开", "sikai.zhang@17zuoye.com", 0f, 0f, 1, "2135", "469564", 1599l, DateUtils.stringToDate("2017-12-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yi.sun", "孙一", "yi.sun@17zuoye.com", 0f, 0f, 1, "2136", "071784", 1605l, DateUtils.stringToDate("2018-08-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanyan.wu", "仵言言", "yanyan.wu@17zuoye.com", 0f, 0f, 1, "2137", "944497", 1599l, DateUtils.stringToDate("2017-12-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.tian", "田磊", "lei.tian@17zuoye.com", 0f, 0f, 1, "2138", "994984", 1599l, DateUtils.stringToDate("2017-12-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qingqing.liu", "刘清清", "qingqing.liu@17zuoye.com", 0f, 0f, 1, "2141", "336296", 1602l, DateUtils.stringToDate("2018-01-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lijun.guo", "郭力军", "lijun.guo@17zuoye.com", 0f, 0f, 1, "2142", "867651", 1599l, DateUtils.stringToDate("2018-01-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhanlei.qiao", "乔占磊", "zhanlei.qiao@17zuoye.com", 0f, 0f, 1, "2143", "176091", 1605l, DateUtils.stringToDate("2018-01-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mengqi.xu", "徐梦琪", "mengqi.xu@17zuoye.com", 0f, 0f, 1, "2145", "607940", 1605l, DateUtils.stringToDate("2018-01-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("danna.zhu", "朱丹娜", "danna.zhu@17zuoye.com", 0f, 0f, 1, "2147", "354961", 1599l, DateUtils.stringToDate("2018-01-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cong.shen", "沈聪", "cong.shen@17zuoye.com", 0f, 0f, 1, "2150", "401266", 1603l, DateUtils.stringToDate("2018-01-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoya.li", "李筱雅", "xiaoya.li@17zuoye.com", 0f, 0f, 1, "2151", "254777", 1599l, DateUtils.stringToDate("2018-01-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("he.xu", "徐贺", "he.xu@17zuoye.com", 0f, 0f, 1, "2152", "366964", 1602l, DateUtils.stringToDate("2018-01-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunhui.xiong", "熊春晖", "chunhui.xiong@17zuoye.com", 0f, 0f, 1, "2153", "147825", 1603l, DateUtils.stringToDate("2018-01-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunqi.yang", "杨春祺", "chunqi.yang@17zuoye.com", 0f, 0f, 1, "2154", "879138", 1605l, DateUtils.stringToDate("2018-01-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yubo.liu", "刘宇波", "yubo.liu@17zuoye.com", 0f, 0f, 1, "2155", "911722", 1599l, DateUtils.stringToDate("2018-01-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zelong.hu", "胡泽龙", "zelong.hu@17zuoye.com", 0f, 0f, 1, "2156", "356482", 1599l, DateUtils.stringToDate("2018-01-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tong.liu", "刘彤", "tong.liu@17zuoye.com", 0f, 0f, 1, "2157", "077488", 1603l, DateUtils.stringToDate("2018-01-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hengyang.jin", "金恒扬", "hengyang.jin@17zuoye.com", 0f, 0f, 1, "2161", "773002", 1599l, DateUtils.stringToDate("2018-01-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yiyi.li", "李依依", "yiyi.li@17zuoye.com", 0f, 0f, 1, "2163", "550811", 1602l, DateUtils.stringToDate("2018-01-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("linrui.xia", "夏霖锐", "linrui.xia@17zuoye.com", 0f, 0f, 1, "2164", "509781", 1599l, DateUtils.stringToDate("2018-01-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("daoquan.zhang", "张道泉", "daoquan.zhang@17zuoye.com", 0f, 0f, 1, "2167", "274081", 1599l, DateUtils.stringToDate("2018-01-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lina.xue", "薛丽娜", "lina.xue@17zuoye.com", 0f, 0f, 1, "2168", "169865", 1601l, DateUtils.stringToDate("2018-01-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoyu.tang", "唐晓雨", "xiaoyu.tang@17zuoye.com", 0f, 0f, 1, "2170", "801707", 1604l, DateUtils.stringToDate("2018-01-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("deyang.xing", "邢德阳", "deyang.xing@17zuoye.com", 0f, 0f, 1, "2171", "626200", 1599l, DateUtils.stringToDate("2018-01-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hao.zheng", "郑豪", "hao.zheng@17zuoye.com", 0f, 0f, 1, "2173", "539575", 1602l, DateUtils.stringToDate("2018-01-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xue.sun", "孙雪", "xue.sun@17zuoye.com", 0f, 0f, 1, "2174", "509066", 1599l, DateUtils.stringToDate("2018-01-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fan.zhang", "张凡", "fan.zhang@17zuoye.com", 0f, 0f, 1, "2177", "667189", 1599l, DateUtils.stringToDate("2018-02-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chang.yang", "杨畅", "chang.yang@17zuoye.com", 0f, 0f, 1, "2178", "251938", 1605l, DateUtils.stringToDate("2018-02-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bo.zhang", "张博", "bo.zhang@17zuoye.com", 0f, 0f, 1, "2182", "288619", 1599l, DateUtils.stringToDate("2018-02-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenchuang.liu", "刘文闯", "wenchuang.liu@17zuoye.com", 0f, 0f, 1, "2183", "168758", 1599l, DateUtils.stringToDate("2018-02-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haihong.wu", "吴海宏", "haihong.wu@17zuoye.com", 0f, 0f, 1, "2184", "318580", 1604l, DateUtils.stringToDate("2018-02-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chao.li.a", "李超A", "chao.li.a@17zuoye.com", 0f, 0f, 1, "2186", "387113", 1599l, DateUtils.stringToDate("2018-02-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meiqi.liu", "刘美琦", "meiqi.liu@17zuoye.com", 0f, 0f, 1, "2187", "632367", 1603l, DateUtils.stringToDate("2018-02-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuo.zhang", "张硕", "shuo.zhang@17zuoye.com", 0f, 0f, 1, "2189", "093587", 1599l, DateUtils.stringToDate("2018-02-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ling.zhang", "张玲", "ling.zhang@17zuoye.com", 0f, 0f, 1, "2238", "209404", 1603l, DateUtils.stringToDate("2018-02-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingyi.zhang", "张京义", "jingyi.zhang@17zuoye.com", 0f, 0f, 1, "2240", "144757", 1599l, DateUtils.stringToDate("2018-02-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xi.yang", "杨熙", "xi.yang@17zuoye.com", 0f, 0f, 1, "2241", "534757", 1599l, DateUtils.stringToDate("2018-02-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhiqiang.fang", "方志强", "zhiqiang.fang@17zuoye.com", 0f, 0f, 1, "2242", "959524", 1605l, DateUtils.stringToDate("2018-02-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yiqun.zhang", "张轶群", "yiqun.zhang@17zuoye.com", 0f, 0f, 1, "2243", "066858", 1605l, DateUtils.stringToDate("2018-02-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peixin.chen", "陈培新", "peixin.chen@17zuoye.com", 0f, 0f, 1, "2252", "297637", 1605l, DateUtils.stringToDate("2018-02-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yun.liu", "刘云", "yun.liu@17zuoye.com", 0f, 0f, 1, "2253", "863319", 1599l, DateUtils.stringToDate("2018-02-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lili.liu.b", "刘丽莉", "lili.liu.b@17zuoye.com", 0f, 0f, 1, "2255", "082344", 1602l, DateUtils.stringToDate("2018-02-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.du", "杜鹏", "peng.du@17zuoye.com", 0f, 0f, 1, "2258", "943025", 1599l, DateUtils.stringToDate("2018-02-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingtian.ni", "倪景田", "jingtian.ni@17zuoye.com", 0f, 0f, 1, "2259", "554521", 1601l, DateUtils.stringToDate("2018-02-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenwen.chu", "储稳稳", "wenwen.chu@17zuoye.com", 0f, 0f, 1, "2294", "950045", 1601l, DateUtils.stringToDate("2018-03-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuai.zhang.a", "张帅A", "shuai.zhang.a@17zuoye.com", 0f, 0f, 1, "2295", "824898", 1601l, DateUtils.stringToDate("2018-03-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kang.liu", "刘康", "kang.liu@17zuoye.com", 0f, 0f, 1, "2297", "914232", 1599l, DateUtils.stringToDate("2018-03-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shulan.chen", "陈淑兰", "shulan.chen@17zuoye.com", 0f, 0f, 1, "2299", "208547", 1599l, DateUtils.stringToDate("2018-03-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lintao.cao", "曹霖涛", "lintao.cao@17zuoye.com", 0f, 0f, 1, "2300", "357612", 1599l, DateUtils.stringToDate("2018-03-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("deliang.che", "车德亮", "deliang.che@17zuoye.com", 0f, 0f, 1, "2301", "316911", 1603l, DateUtils.stringToDate("2018-03-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hongjie.zhang", "张红洁", "hongjie.zhang@17zuoye.com", 0f, 0f, 1, "2302", "916696", 1601l, DateUtils.stringToDate("2018-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingjing.zhou.a", "周晶晶", "jingjing.zhou.a@17zuoye.com", 0f, 0f, 1, "2303", "539789", 1601l, DateUtils.stringToDate("2018-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qilin.xiong", "熊麒麟", "qilin.xiong@17zuoye.com", 0f, 0f, 1, "2304", "599529", 1603l, DateUtils.stringToDate("2018-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yue.li.a", "李月", "yue.li.a@17zuoye.com", 0f, 0f, 1, "2305", "925267", 1603l, DateUtils.stringToDate("2018-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("beiping.shu", "疏北平", "beiping.shu@17zuoye.com", 0f, 0f, 1, "2306", "119727", 1599l, DateUtils.stringToDate("2018-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qi.yan", "闫琦", "qi.yan@17zuoye.com", 0f, 0f, 1, "2307", "220554", 1600l, DateUtils.stringToDate("2018-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chengyang.shi", "师澄洋", "chengyang.shi@17zuoye.com", 0f, 0f, 1, "2308", "650692", 1600l, DateUtils.stringToDate("2018-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yue.tian", "田悦", "yue.tian@17zuoye.com", 0f, 0f, 1, "2311", "213984", 1599l, DateUtils.stringToDate("2018-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xu.qiu", "邱旭", "xu.qiu@17zuoye.com", 0f, 0f, 1, "2312", "055063", 1603l, DateUtils.stringToDate("2018-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yi.zhong", "衷奕", "yi.zhong@17zuoye.com", 0f, 0f, 1, "2313", "399375", 1599l, DateUtils.stringToDate("2018-03-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lili.zhang.a", "张丽丽A", "lili.zhang.a@17zuoye.com", 0f, 0f, 1, "2355", "771325", 1601l, DateUtils.stringToDate("2018-03-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meilan.yuan", "袁美兰", "meilan.yuan@17zuoye.com", 0f, 0f, 1, "2356", "207687", 1603l, DateUtils.stringToDate("2018-03-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chao.ma.b", "马超B", "chao.ma.b@17zuoye.com", 0f, 0f, 1, "2359", "057534", 1605l, DateUtils.stringToDate("2018-03-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanjie.pan", "潘延杰", "yanjie.pan@17zuoye.com", 0f, 0f, 1, "2360", "329106", 1602l, DateUtils.stringToDate("2018-03-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huitong.zhang", "张慧童", "huitong.zhang@17zuoye.com", 0f, 0f, 1, "2361", "260513", 1605l, DateUtils.stringToDate("2018-03-12", "yyyy-MM-dd")));
        MapMessage mapMessage = createUserOrSendEmail(list, type);
        deleteCountryDayOrderCount(1);
        return mapMessage;
    }

    /**
     * 初始化公司员工账号方法      先留着 还得发邮件  发完邮件删除
     *
     * @return
     */
    @RequestMapping(value = "batch_create_account2.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage batch_create_account2() {
        Integer status = getCountryDayOrderCount(2);
        if (status != null && Objects.equals(1, status)) {
            return MapMessage.errorMessage("方法正在执行了等会吧");
        }
        updateCountryDayOrderCount(2);
        Integer type = getRequestInt("type", 1);
        List<CreateUserAccount> list = new ArrayList<>();
        list.add(new CreateUserAccount("mingyuan.xia", "夏明园", "mingyuan.xia@17zuoye.com", 0f, 0f, 1, "2362", "697126", 1603l, DateUtils.stringToDate("2018-03-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhilin.li", "李志林", "zhilin.li@17zuoye.com", 0f, 0f, 1, "2364", "583901", 1603l, DateUtils.stringToDate("2018-03-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiankun.zeng", "曾宪坤", "xiankun.zeng@17zuoye.com", 0f, 0f, 1, "2366", "877657", 1603l, DateUtils.stringToDate("2018-03-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shanshan.li", "李杉杉", "shanshan.li@17zuoye.com", 0f, 0f, 1, "2367", "048463", 1603l, DateUtils.stringToDate("2018-03-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chen.yang", "杨晨", "chen.yang@17zuoye.com", 0f, 0f, 1, "2368", "740199", 1603l, DateUtils.stringToDate("2018-03-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rui.cheng", "程瑞", "rui.cheng@17zuoye.com", 0f, 0f, 1, "2370", "252230", 1599l, DateUtils.stringToDate("2018-03-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("te.wang", "王特", "te.wang@17zuoye.com", 0f, 0f, 1, "2371", "435896", 1602l, DateUtils.stringToDate("2018-03-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yawei.wang", "王亚伟", "yawei.wang@17zuoye.com", 0f, 0f, 1, "2372", "514077", 1601l, DateUtils.stringToDate("2018-03-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhiqiang.zhang", "张智强", "zhiqiang.zhang@17zuoye.com", 0f, 0f, 1, "2376", "510131", 1605l, DateUtils.stringToDate("2018-03-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianrong.yang", "杨建荣", "jianrong.yang@17zuoye.com", 0f, 0f, 1, "2379", "665234", 1603l, DateUtils.stringToDate("2018-03-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zijia.zhou", "周子佳", "zijia.zhou@17zuoye.com", 0f, 0f, 1, "2380", "384507", 1603l, DateUtils.stringToDate("2018-03-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huanqing.wang", "王焕青", "huanqing.wang@17zuoye.com", 0f, 0f, 1, "2381", "451988", 1603l, DateUtils.stringToDate("2018-03-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("min.wang", "王敏", "min.wang@17zuoye.com", 0f, 0f, 1, "2382", "513006", 1603l, DateUtils.stringToDate("2018-03-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chao.li.b", "李超B", "chao.li.b@17zuoye.com", 0f, 0f, 1, "2406", "311703", 1601l, DateUtils.stringToDate("2018-03-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jie.wang.a", "汪杰", "jie.wang.a@17zuoye.com", 0f, 0f, 1, "2408", "916416", 1603l, DateUtils.stringToDate("2018-03-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiao.gong", "公骁", "xiao.gong@17zuoye.com", 0f, 0f, 1, "2409", "095794", 1599l, DateUtils.stringToDate("2018-03-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaobo.wu", "武晓波", "xiaobo.wu@17zuoye.com", 0f, 0f, 1, "2410", "488775", 1603l, DateUtils.stringToDate("2018-03-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("han.xue", "薛涵", "han.xue@17zuoye.com", 0f, 0f, 1, "2412", "208138", 1599l, DateUtils.stringToDate("2018-03-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunyan.wang", "王春燕", "chunyan.wang@17zuoye.com", 0f, 0f, 1, "2415", "471953", 1602l, DateUtils.stringToDate("2018-03-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zinan.guo", "郭子南", "zinan.guo@17zuoye.com", 0f, 0f, 1, "2416", "792824", 1601l, DateUtils.stringToDate("2018-03-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jun.shu", "疏骏", "jun.shu@17zuoye.com", 0f, 0f, 1, "2417", "456245", 1599l, DateUtils.stringToDate("2018-03-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.gong", "龚静", "jing.gong@17zuoye.com", 0f, 0f, 1, "2420", "596608", 1603l, DateUtils.stringToDate("2018-03-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wang.wang", "王旺", "wang.wang@17zuoye.com", 0f, 0f, 1, "2421", "478705", 1599l, DateUtils.stringToDate("2018-03-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuofu.zhou", "周烁夫", "shuofu.zhou@17zuoye.com", 0f, 0f, 1, "2424", "304587", 1605l, DateUtils.stringToDate("2018-03-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yueying.hu", "胡月颖", "yueying.hu@17zuoye.com", 0f, 0f, 1, "2445", "249562", 1603l, DateUtils.stringToDate("2018-03-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("biao.xue", "薛彪", "biao.xue@17zuoye.com", 0f, 0f, 1, "2450", "448730", 1604l, DateUtils.stringToDate("2018-03-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianbo.liu", "刘建波", "jianbo.liu@17zuoye.com", 0f, 0f, 1, "2451", "221746", 1599l, DateUtils.stringToDate("2018-03-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weilin.yin", "尹卫林", "weilin.yin@17zuoye.com", 0f, 0f, 1, "2452", "464259", 1605l, DateUtils.stringToDate("2018-03-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("luxiu.xu", "许路修", "luxiu.xu@17zuoye.com", 0f, 0f, 1, "2455", "058424", 1605l, DateUtils.stringToDate("2018-03-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.chong", "崇晶", "jing.chong@17zuoye.com", 0f, 0f, 1, "2456", "095378", 1605l, DateUtils.stringToDate("2018-03-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoshuan.chu", "褚小栓", "xiaoshuan.chu@17zuoye.com", 0f, 0f, 1, "2460", "492866", 1601l, DateUtils.stringToDate("2018-03-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cuiyan.xu", "徐翠艳", "cuiyan.xu@17zuoye.com", 0f, 0f, 1, "2466", "081431", 1602l, DateUtils.stringToDate("2018-03-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lin.wang", "王琳", "lin.wang@17zuoye.com", 0f, 0f, 1, "2468", "359868", 1599l, DateUtils.stringToDate("2018-03-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("pan.gao", "高攀", "pan.gao@17zuoye.com", 0f, 0f, 1, "2469", "758584", 1599l, DateUtils.stringToDate("2018-03-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huibin.yang", "杨卉彬", "huibin.yang@17zuoye.com", 0f, 0f, 1, "2475", "840214", 1603l, DateUtils.stringToDate("2018-03-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yong.liu", "刘勇", "yong.liu@17zuoye.com", 0f, 0f, 1, "2476", "481989", 1603l, DateUtils.stringToDate("2018-03-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingjing.tang", "唐精精", "jingjing.tang@17zuoye.com", 0f, 0f, 1, "2477", "351613", 1603l, DateUtils.stringToDate("2018-03-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haixu.zhang", "张海旭", "haixu.zhang@17zuoye.com", 0f, 0f, 1, "2478", "240466", 1602l, DateUtils.stringToDate("2018-03-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dong.han", "韩东", "dong.han@17zuoye.com", 0f, 0f, 1, "2480", "686223", 1599l, DateUtils.stringToDate("2018-03-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaochen.chu", "储晓琛", "xiaochen.chu@17zuoye.com", 0f, 0f, 1, "2484", "028786", 1601l, DateUtils.stringToDate("2018-03-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fuyu.yang", "杨富裕", "fuyu.yang@17zuoye.com", 0f, 0f, 1, "2485", "707905", 1601l, DateUtils.stringToDate("2018-03-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huimin.xian", "咸慧敏", "huimin.xian@17zuoye.com", 0f, 0f, 1, "2487", "112999", 1599l, DateUtils.stringToDate("2018-03-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zelong.hao", "郝泽龙", "zelong.hao@17zuoye.com", 0f, 0f, 1, "2488", "485725", 1603l, DateUtils.stringToDate("2018-03-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lin.lu", "卢淋", "lin.lu@17zuoye.com", 0f, 0f, 1, "2490", "570165", 1603l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weilin.yang", "杨伟林", "weilin.yang@17zuoye.com", 0f, 0f, 1, "2496", "639260", 1599l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoyu.wang", "王晓宇", "xiaoyu.wang@17zuoye.com", 0f, 0f, 1, "2497", "970300", 1603l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanping.chen", "陈艳苹", "yanping.chen@17zuoye.com", 0f, 0f, 1, "2499", "278401", 1599l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.zhang.c", "张燕", "yan.zhang.c@17zuoye.com", 0f, 0f, 1, "2501", "499014", 1601l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("he.wei", "魏贺", "he.wei@17zuoye.com", 0f, 0f, 1, "2502", "264326", 1601l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaokai.liu", "刘晓凯", "xiaokai.liu@17zuoye.com", 0f, 0f, 1, "2504", "573401", 1603l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mengyang.qin", "秦梦洋", "mengyang.qin@17zuoye.com", 0f, 0f, 1, "2505", "267651", 1602l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xingguang.tian", "田星光", "xingguang.tian@17zuoye.com", 0f, 0f, 1, "2506", "517132", 1601l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liujiang.zheng", "郑留蒋", "liujiang.zheng@17zuoye.com", 0f, 0f, 1, "2507", "597974", 1601l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junfei.li", "李俊飞", "junfei.li@17zuoye.com", 0f, 0f, 1, "2510", "478534", 1603l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiulong.cui", "崔久龙", "jiulong.cui@17zuoye.com", 0f, 0f, 1, "2511", "281067", 1599l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("li.zhang.d", "张丽D", "li.zhang.d@17zuoye.com", 0f, 0f, 1, "2512", "942736", 1603l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weinan.huang", "黄伟男", "weinan.huang@17zuoye.com", 0f, 0f, 1, "2514", "853914", 1605l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiatao.wang", "王嘉涛", "jiatao.wang@17zuoye.com", 0f, 0f, 1, "2515", "308647", 1605l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("linshan.yang", "杨林山", "linshan.yang@17zuoye.com", 0f, 0f, 1, "2520", "667457", 1603l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("di.chang", "常迪", "di.chang@17zuoye.com", 0f, 0f, 1, "2521", "930066", 1599l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bo.jiang", "江波", "bo.jiang@17zuoye.com", 0f, 0f, 1, "2522", "187713", 1599l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunbao.cai", "蔡春保", "chunbao.cai@17zuoye.com", 0f, 0f, 1, "2523", "993702", 1599l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chiyu.zhou", "周驰宇", "chiyu.zhou@17zuoye.com", 0f, 0f, 1, "2525", "337867", 1605l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chongfeng.qi", "祁冲锋", "chongfeng.qi@17zuoye.com", 0f, 0f, 1, "2526", "937048", 1603l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huimin.jia", "贾慧敏", "huimin.jia@17zuoye.com", 0f, 0f, 1, "2527", "501994", 1601l, DateUtils.stringToDate("2018-04-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yutao.qin", "秦玉涛", "yutao.qin@17zuoye.com", 0f, 0f, 1, "2545", "926810", 1599l, DateUtils.stringToDate("2018-04-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xian.zhang", "张弦", "xian.zhang@17zuoye.com", 0f, 0f, 1, "2547", "169685", 1599l, DateUtils.stringToDate("2018-04-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weiqun.huang", "黄魏群", "weiqun.huang@17zuoye.com", 0f, 0f, 1, "2548", "122578", 1605l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hang.li", "李航", "hang.li@17zuoye.com", 0f, 0f, 1, "2549", "001255", 1605l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinlei.niu", "牛鑫蕾", "xinlei.niu@17zuoye.com", 0f, 0f, 1, "2550", "922476", 1601l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yudan.zhu", "朱昱丹", "yudan.zhu@17zuoye.com", 0f, 0f, 1, "2551", "243057", 1604l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.hu", "胡鹏", "peng.hu@17zuoye.com", 0f, 0f, 1, "2552", "617705", 1603l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mengying.du", "杜梦颖", "mengying.du@17zuoye.com", 0f, 0f, 1, "2553", "692990", 1605l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhenyu.cai", "蔡振宇", "zhenyu.cai@17zuoye.com", 0f, 0f, 1, "2554", "559701", 1601l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shaofei.kong", "孔绍飞", "shaofei.kong@17zuoye.com", 0f, 0f, 1, "2557", "244459", 1605l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("caiyun.zhang", "张彩云", "caiyun.zhang@17zuoye.com", 0f, 0f, 1, "2558", "990719", 1605l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.yin", "尹昱", "yu.yin@17zuoye.com", 0f, 0f, 1, "2560", "493259", 1605l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("sirui.zhang", "张思睿", "sirui.zhang@17zuoye.com", 0f, 0f, 1, "2562", "126852", 1605l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yimei.liu", "柳一梅", "yimei.liu@17zuoye.com", 0f, 0f, 1, "2564", "394921", 1603l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianxin.ma", "马建鑫", "jianxin.ma@17zuoye.com", 0f, 0f, 1, "2565", "916185", 1602l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fuqiang.wang", "王富强", "fuqiang.wang@17zuoye.com", 0f, 0f, 1, "3012", "466118", 1603l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiangping.hu", "胡向平", "xiangping.hu@17zuoye.com", 0f, 0f, 1, "2571", "014401", 1599l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("miaowei.chen", "陈苗炜", "miaowei.chen@17zuoye.com", 0f, 0f, 1, "2575", "473131", 1605l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jun.wang", "王珺", "jun.wang@17zuoye.com", 0f, 0f, 1, "2577", "654306", 1603l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ran.yi", "伊冉", "ran.yi@17zuoye.com", 0f, 0f, 1, "2578", "318728", 1603l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junbao.zhang", "张军保", "junbao.zhang@17zuoye.com", 0f, 0f, 1, "2579", "066344", 1603l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jigang.yan", "闫继钢", "jigang.yan@17zuoye.com", 0f, 0f, 1, "2580", "520269", 1601l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("leyuan.dong", "董乐园", "leyuan.dong@17zuoye.com", 0f, 0f, 1, "2581", "538547", 1604l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("di.qin", "秦迪", "di.qin@17zuoye.com", 0f, 0f, 1, "2582", "415610", 1602l, DateUtils.stringToDate("2018-04-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chenguang.yang", "杨晨光", "chenguang.yang@17zuoye.com", 0f, 0f, 1, "2583", "242764", 1602l, DateUtils.stringToDate("2018-04-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bolin.li", "李柏林", "bolin.li@17zuoye.com", 0f, 0f, 1, "2585", "457222", 1599l, DateUtils.stringToDate("2018-04-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yao.cheng", "程瑶", "yao.cheng@17zuoye.com", 0f, 0f, 1, "2586", "234971", 1603l, DateUtils.stringToDate("2018-04-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("song.gong", "龚松", "song.gong@17zuoye.com", 0f, 0f, 1, "2589", "281410", 1605l, DateUtils.stringToDate("2018-04-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liang.wu", "吴亮", "liang.wu@17zuoye.com", 0f, 0f, 1, "2590", "066273", 1599l, DateUtils.stringToDate("2018-04-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meng.gu", "顾萌", "meng.gu@17zuoye.com", 0f, 0f, 1, "2591", "939807", 1601l, DateUtils.stringToDate("2018-04-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meili.cao", "曹美丽", "meili.cao@17zuoye.com", 0f, 0f, 1, "2594", "509403", 1599l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuan.yin", "殷媛", "yuan.yin@17zuoye.com", 0f, 0f, 1, "2596", "552053", 1601l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guangren.gu", "古光仁", "guangren.gu@17zuoye.com", 0f, 0f, 1, "2598", "977463", 1599l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingming.kan", "阚明明", "mingming.kan@17zuoye.com", 0f, 0f, 1, "2599", "766933", 1599l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xingnan.hu", "胡兴楠", "xingnan.hu@17zuoye.com", 0f, 0f, 1, "2601", "356602", 1605l, DateUtils.stringToDate("2018-04-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenbiao.jia", "贾文彪", "wenbiao.jia@17zuoye.com", 0f, 0f, 1, "2603", "479898", 1603l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meimei.li", "李美美", "meimei.li@17zuoye.com", 0f, 0f, 1, "2605", "751458", 1605l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yongyu.zhang", "张永钰", "yongyu.zhang@17zuoye.com", 0f, 0f, 1, "2609", "880766", 1605l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bing.xiao", "肖冰", "bing.xiao@17zuoye.com", 0f, 0f, 1, "2613", "277118", 1601l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yunjie.cai", "蔡云洁", "yunjie.cai@17zuoye.com", 0f, 0f, 1, "2614", "702802", 1603l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jie.sun.a", "孙杰", "jie.sun.a@17zuoye.com", 0f, 0f, 1, "2615", "385432", 1601l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingjing.wang", "王晶晶", "jingjing.wang@17zuoye.com", 0f, 0f, 1, "2616", "814247", 1601l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("limei.jiang", "姜立美", "limei.jiang@17zuoye.com", 0f, 0f, 1, "2618", "092136", 1599l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("sijin.an", "安思瑾", "sijin.an@17zuoye.com", 0f, 0f, 1, "2619", "211380", 1601l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tie.lv", "吕铁", "tie.lv@17zuoye.com", 0f, 0f, 1, "2620", "547028", 1599l, DateUtils.stringToDate("2018-04-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xingpeng.lv", "吕兴鹏", "xingpeng.lv@17zuoye.com", 0f, 0f, 1, "2622", "604545", 1599l, DateUtils.stringToDate("2018-04-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jialu.yao", "姚佳璐", "jialu.yao@17zuoye.com", 0f, 0f, 1, "2625", "993929", 1605l, DateUtils.stringToDate("2018-04-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("engui.xie", "谢恩贵", "engui.xie@17zuoye.com", 0f, 0f, 1, "2627", "689612", 1601l, DateUtils.stringToDate("2018-04-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yong.he", "何勇", "yong.he@17zuoye.com", 0f, 0f, 1, "2628", "474075", 1601l, DateUtils.stringToDate("2018-04-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bei.yang", "杨贝", "bei.yang@17zuoye.com", 0f, 0f, 1, "2635", "712981", 1603l, DateUtils.stringToDate("2018-04-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuai.song", "宋帅", "shuai.song@17zuoye.com", 0f, 0f, 1, "2636", "011312", 1602l, DateUtils.stringToDate("2018-04-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ming.nie", "聂明", "ming.nie@17zuoye.com", 0f, 0f, 1, "2637", "457142", 1599l, DateUtils.stringToDate("2018-04-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hantao.liu", "刘汉涛", "hantao.liu@17zuoye.com", 0f, 0f, 1, "2641", "438816", 1601l, DateUtils.stringToDate("2018-04-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mengmeng.zhang", "张萌萌", "mengmeng.zhang@17zuoye.com", 0f, 0f, 1, "2642", "388630", 1599l, DateUtils.stringToDate("2018-04-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rong.xue", "薛融", "rong.xue@17zuoye.com", 0f, 0f, 1, "2643", "392048", 1601l, DateUtils.stringToDate("2018-04-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ming.wang", "王鸣", "ming.wang@17zuoye.com", 0f, 0f, 1, "2644", "121431", 1602l, DateUtils.stringToDate("2018-04-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weipeng.rong", "荣伟鹏", "weipeng.rong@17zuoye.com", 0f, 0f, 1, "2660", "271434", 1603l, DateUtils.stringToDate("2018-04-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("da.pan", "潘达", "da.pan@17zuoye.com", 0f, 0f, 1, "2720", "115334", 1599l, DateUtils.stringToDate("2018-04-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chen.qi", "祁辰", "chen.qi@17zuoye.com", 0f, 0f, 1, "2721", "123985", 1601l, DateUtils.stringToDate("2018-04-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yajie.bai", "白亚洁", "yajie.bai@17zuoye.com", 0f, 0f, 1, "2722", "557876", 1599l, DateUtils.stringToDate("2018-04-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zewei.zheng", "郑泽卫", "zewei.zheng@17zuoye.com", 0f, 0f, 1, "2723", "959339", 1601l, DateUtils.stringToDate("2018-04-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuhui.zhang", "张旭晖", "xuhui.zhang@17zuoye.com", 0f, 0f, 1, "2726", "910204", 1599l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.wang.b", "王婧B", "jing.wang.b@17zuoye.com", 0f, 0f, 1, "2727", "644269", 1599l, DateUtils.stringToDate("2018-04-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuyu.jiang", "姜煦雨", "xuyu.jiang@17zuoye.com", 0f, 0f, 1, "2728", "642823", 1601l, DateUtils.stringToDate("2018-04-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kun.xu", "徐坤", "kun.xu@17zuoye.com", 0f, 0f, 1, "2730", "233475", 1599l, DateUtils.stringToDate("2018-04-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianan.yang.a", "杨佳楠A", "jianan.yang.a@17zuoye.com", 0f, 0f, 1, "2731", "054237", 1605l, DateUtils.stringToDate("2018-04-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiquan.zheng", "郑纪泉", "jiquan.zheng@17zuoye.com", 0f, 0f, 1, "2736", "191879", 1601l, DateUtils.stringToDate("2018-04-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shangying.li", "李尚英", "shangying.li@17zuoye.com", 0f, 0f, 1, "2739", "343777", 1603l, DateUtils.stringToDate("2018-04-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qingjuan.zhao", "赵清娟", "qingjuan.zhao@17zuoye.com", 0f, 0f, 1, "2741", "745436", 1601l, DateUtils.stringToDate("2018-04-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanfei.wang", "王艳菲", "yanfei.wang@17zuoye.com", 0f, 0f, 1, "2742", "748366", 1599l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ke.cao", "曹科", "ke.cao@17zuoye.com", 0f, 0f, 1, "2743", "651078", 1605l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yilin.kong", "孔依琳", "yilin.kong@17zuoye.com", 0f, 0f, 1, "2744", "168609", 1604l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shangyou.lu", "卢尚友", "shangyou.lu@17zuoye.com", 0f, 0f, 1, "2745", "964033", 1603l, DateUtils.stringToDate("2018-04-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoyu.xie", "谢小雨", "xiaoyu.xie@17zuoye.com", 0f, 0f, 1, "2749", "215577", 1601l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xincheng.bi", "毕新承", "xincheng.bi@17zuoye.com", 0f, 0f, 1, "2751", "477730", 1602l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuang.wu", "武爽", "shuang.wu@17zuoye.com", 0f, 0f, 1, "2752", "499184", 1599l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guang.yang.c", "杨光C", "guang.yang.c@17zuoye.com", 0f, 0f, 1, "2753", "862497", 1599l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhongkai.sun", "孙中凯", "zhongkai.sun@17zuoye.com", 0f, 0f, 1, "2754", "113148", 1601l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingkun.xie", "谢明坤", "mingkun.xie@17zuoye.com", 0f, 0f, 1, "2756", "021113", 1601l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lihong.li", "李力宏", "lihong.li@17zuoye.com", 0f, 0f, 1, "2759", "997795", 1603l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kunyi.li", "李坤燚", "kunyi.li@17zuoye.com", 0f, 0f, 1, "2760", "232263", 1599l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhao.liu.a", "刘钊A", "zhao.liu.a@17zuoye.com", 0f, 0f, 1, "2762", "085690", 1603l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.liu", "刘磊", "lei.liu@17zuoye.com", 0f, 0f, 1, "2765", "470560", 1602l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuerui.zhang", "张雪瑞", "xuerui.zhang@17zuoye.com", 0f, 0f, 1, "2767", "749453", 1603l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qi.li.a", "李琦", "qi.li.a@17zuoye.com", 0f, 0f, 1, "2768", "977979", 1599l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.wu", "伍颖", "ying.wu@17zuoye.com", 0f, 0f, 1, "2769", "308508", 1605l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bin.cheng", "程彬", "bin.cheng@17zuoye.com", 0f, 0f, 1, "2770", "567278", 1605l, DateUtils.stringToDate("2018-05-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiaojiao.wen", "文姣姣", "jiaojiao.wen@17zuoye.com", 0f, 0f, 1, "2771", "192320", 1599l, DateUtils.stringToDate("2018-04-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peipei.ding", "丁培培", "peipei.ding@17zuoye.com", 0f, 0f, 1, "2775", "280737", 1603l, DateUtils.stringToDate("2018-03-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaonan.yan", "闫小南", "xiaonan.yan@17zuoye.com", 0f, 0f, 1, "2776", "050810", 1603l, DateUtils.stringToDate("2018-03-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junxia.liu", "刘俊霞", "junxia.liu@17zuoye.com", 0f, 0f, 1, "2778", "885208", 1603l, DateUtils.stringToDate("2018-04-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingwen.wang", "王靖雯", "jingwen.wang@17zuoye.com", 0f, 0f, 1, "2779", "541702", 1603l, DateUtils.stringToDate("2018-04-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiwen.bao", "包熙雯", "xiwen.bao@17zuoye.com", 0f, 0f, 1, "2780", "781872", 1603l, DateUtils.stringToDate("2018-04-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.zhao.b", "赵莹", "ying.zhao.b@17zuoye.com", 0f, 0f, 1, "2782", "152505", 1603l, DateUtils.stringToDate("2018-04-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fan.yang", "杨帆", "fan.yang@17zuoye.com", 0f, 0f, 1, "2819", "994085", 1603l, DateUtils.stringToDate("2018-04-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenjun.tao", "陶文俊", "wenjun.tao@17zuoye.com", 0f, 0f, 1, "2820", "987470", 1603l, DateUtils.stringToDate("2018-04-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.tang", "汤鹏", "peng.tang@17zuoye.com", 0f, 0f, 1, "2852", "177260", 1599l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.zhou", "周磊", "lei.zhou@17zuoye.com", 0f, 0f, 1, "2853", "606052", 1603l, DateUtils.stringToDate("2018-05-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("muyun.he", "何沐芸", "muyun.he@17zuoye.com", 0f, 0f, 1, "2854", "079657", 1600l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jia.sun", "孙嘉", "jia.sun@17zuoye.com", 0f, 0f, 1, "2856", "993997", 1603l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaomin.zhao", "赵晓敏", "xiaomin.zhao@17zuoye.com", 0f, 0f, 1, "2859", "223119", 1603l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dan.xu", "许丹", "dan.xu@17zuoye.com", 0f, 0f, 1, "2864", "984271", 1605l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinli.gong", "宫新丽", "xinli.gong@17zuoye.com", 0f, 0f, 1, "2865", "908865", 1605l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shengyong.hu", "胡胜勇", "shengyong.hu@17zuoye.com", 0f, 0f, 1, "2868", "724573", 1605l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianping.wang", "王剑平", "jianping.wang@17zuoye.com", 0f, 0f, 1, "2869", "207954", 1605l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.wang.e", "王磊E", "lei.wang.e@17zuoye.com", 0f, 0f, 1, "2871", "930492", 1601l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kui.xu", "徐揆", "kui.xu@17zuoye.com", 0f, 0f, 1, "2872", "635063", 1599l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("honglin.shen", "申洪林", "honglin.shen@17zuoye.com", 0f, 0f, 1, "2875", "945710", 1603l, DateUtils.stringToDate("2018-05-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoqing.zeng", "曾筱箐", "xiaoqing.zeng@17zuoye.com", 0f, 0f, 1, "2876", "924824", 1603l, DateUtils.stringToDate("2018-05-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuxiong.lin", "林宇雄", "yuxiong.lin@17zuoye.com", 0f, 0f, 1, "2877", "955917", 1604l, DateUtils.stringToDate("2018-05-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haiying.li", "李海英", "haiying.li@17zuoye.com", 0f, 0f, 1, "2879", "225634", 1599l, DateUtils.stringToDate("2018-05-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanhong.chen", "陈艳红", "yanhong.chen@17zuoye.com", 0f, 0f, 1, "2881", "700304", 1601l, DateUtils.stringToDate("2018-05-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hankui.wang", "王汉魁", "hankui.wang@17zuoye.com", 0f, 0f, 1, "2884", "586733", 1601l, DateUtils.stringToDate("2018-05-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("maowei.guo", "郭懋伟", "maowei.guo@17zuoye.com", 0f, 0f, 1, "2885", "464830", 1601l, DateUtils.stringToDate("2018-05-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanyan.zhang", "张燕燕", "yanyan.zhang@17zuoye.com", 0f, 0f, 1, "2886", "030859", 1605l, DateUtils.stringToDate("2018-05-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dan.wei", "卫丹", "dan.wei@17zuoye.com", 0f, 0f, 1, "2887", "409878", 1602l, DateUtils.stringToDate("2018-05-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huiyang.li", "李会洋", "huiyang.li@17zuoye.com", 0f, 0f, 1, "2888", "866392", 1605l, DateUtils.stringToDate("2018-05-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ruiyi.zhang", "张瑞怡", "ruiyi.zhang@17zuoye.com", 0f, 0f, 1, "2889", "450124", 1599l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haijing.hou", "侯海静", "haijing.hou@17zuoye.com", 0f, 0f, 1, "2890", "514326", 1605l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yibiao.ying", "应燚标", "yibiao.ying@17zuoye.com", 0f, 0f, 1, "2891", "550458", 1599l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jie.li", "李洁", "jie.li@17zuoye.com", 0f, 0f, 1, "2893", "231152", 1603l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianlong.gao", "高健龙", "jianlong.gao@17zuoye.com", 0f, 0f, 1, "2895", "660404", 1601l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qingqing.guo", "郭晴晴", "qingqing.guo@17zuoye.com", 0f, 0f, 1, "2896", "012094", 1605l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingming.guang", "光明明", "mingming.guang@17zuoye.com", 0f, 0f, 1, "2899", "281231", 1599l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinfei.li", "李昕霏", "xinfei.li@17zuoye.com", 0f, 0f, 1, "2901", "773505", 1604l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("li.zuo", "左莉", "li.zuo@17zuoye.com", 0f, 0f, 1, "2902", "271477", 1603l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mianhua.wang", "王缅华", "mianhua.wang@17zuoye.com", 0f, 0f, 1, "2905", "020679", 1601l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chengxian.wang", "王成显", "chengxian.wang@17zuoye.com", 0f, 0f, 1, "2907", "228177", 1599l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingxing.liu", "刘明星", "mingxing.liu@17zuoye.com", 0f, 0f, 1, "2908", "862702", 1605l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("min.tan", "谭敏", "min.tan@17zuoye.com", 0f, 0f, 1, "2909", "663764", 1601l, DateUtils.stringToDate("2018-05-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cui.zhao", "赵翠", "cui.zhao@17zuoye.com", 0f, 0f, 1, "2911", "386742", 1603l, DateUtils.stringToDate("2018-05-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mengzhe.wu", "吴孟哲", "mengzhe.wu@17zuoye.com", 0f, 0f, 1, "2914", "142004", 1603l, DateUtils.stringToDate("2018-05-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("changmin.jin", "金昌玟", "changmin.jin@17zuoye.com", 0f, 0f, 1, "2915", "241255", 1599l, DateUtils.stringToDate("2018-05-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("gang.ren", "任钢", "gang.ren@17zuoye.com", 0f, 0f, 1, "2920", "416562", 1605l, DateUtils.stringToDate("2018-05-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haichao.wang", "王海潮", "haichao.wang@17zuoye.com", 0f, 0f, 1, "2921", "413729", 1603l, DateUtils.stringToDate("2018-05-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liqin.zhang", "章丽琴", "liqin.zhang@17zuoye.com", 0f, 0f, 1, "2924", "198519", 1599l, DateUtils.stringToDate("2018-05-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("pengyu.chen", "陈鹏羽", "pengyu.chen@17zuoye.com", 0f, 0f, 1, "2925", "142253", 1601l, DateUtils.stringToDate("2018-05-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaogang.fang", "方小刚", "xiaogang.fang@17zuoye.com", 0f, 0f, 1, "2926", "061823", 1605l, DateUtils.stringToDate("2018-05-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bing.wu", "吴冰", "bing.wu@17zuoye.com", 0f, 0f, 1, "2927", "988491", 1603l, DateUtils.stringToDate("2018-05-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ran.duan", "段然", "ran.duan@17zuoye.com", 0f, 0f, 1, "2928", "973300", 1605l, DateUtils.stringToDate("2018-05-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoqin.gao", "高晓琴", "xiaoqin.gao@17zuoye.com", 0f, 0f, 1, "2929", "617349", 1601l, DateUtils.stringToDate("2018-05-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianfeng.li", "李建锋", "jianfeng.li@17zuoye.com", 0f, 0f, 1, "2930", "470100", 1601l, DateUtils.stringToDate("2018-05-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunxiao.zhang", "张春晓", "chunxiao.zhang@17zuoye.com", 0f, 0f, 1, "2931", "326644", 1601l, DateUtils.stringToDate("2018-05-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuting.gao", "高玉婷", "yuting.gao@17zuoye.com", 0f, 0f, 1, "2932", "482733", 1605l, DateUtils.stringToDate("2018-05-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weiyang.zhong", "仲伟阳", "weiyang.zhong@17zuoye.com", 0f, 0f, 1, "2934", "257579", 1602l, DateUtils.stringToDate("2018-05-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaolu.zhang", "张晓璐", "xiaolu.zhang@17zuoye.com", 0f, 0f, 1, "2936", "119436", 1600l, DateUtils.stringToDate("2018-05-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinxin.feng.a", "冯欣欣", "xinxin.feng.a@17zuoye.com", 0f, 0f, 1, "2937", "756049", 1602l, DateUtils.stringToDate("2018-05-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiufen.li", "李秀芬", "xiufen.li@17zuoye.com", 0f, 0f, 1, "2938", "056885", 1601l, DateUtils.stringToDate("2018-05-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yi.su", "苏益", "yi.su@17zuoye.com", 0f, 0f, 1, "2939", "414759", 1602l, DateUtils.stringToDate("2018-05-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chao.zhu", "朱超", "chao.zhu@17zuoye.com", 0f, 0f, 1, "2940", "277749", 1602l, DateUtils.stringToDate("2018-05-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.guo.a", "郭晶", "jing.guo.a@17zuoy.com @17zuoye.com", 0f, 0f, 1, "2941", "990110", 1603l, DateUtils.stringToDate("2018-05-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qingcai.guo", "郭庆彩", "qingcai.guo@17zuoye.com", 0f, 0f, 1, "2942", "336885", 1603l, DateUtils.stringToDate("2018-05-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuanyuan.zhang", "张媛媛", "yuanyuan.zhang@17zuoye.com", 0f, 0f, 1, "2944", "583953", 1603l, DateUtils.stringToDate("2018-05-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuhui.sun", "孙玉会", "yuhui.sun@17zuoye.com", 0f, 0f, 1, "2945", "650252", 1603l, DateUtils.stringToDate("2018-05-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.fang", "房磊", "lei.fang@17zuoye.com", 0f, 0f, 1, "2946", "566578", 1603l, DateUtils.stringToDate("2018-05-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuan.ma", "马轩", "xuan.ma@17zuoye.com", 0f, 0f, 1, "2947", "080844", 1603l, DateUtils.stringToDate("2018-05-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("nannan.ma.a", "马楠楠", "nannan.ma.a@17zuoye.com", 0f, 0f, 1, "2949", "461952", 1603l, DateUtils.stringToDate("2018-05-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiyuan.fu", "傅纪元", "jiyuan.fu@17zuoye.com", 0f, 0f, 1, "3004", "660796", 1603l, DateUtils.stringToDate("2018-05-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.wang.f", "王雷F", "lei.wang.f@17zuoye.com", 0f, 0f, 1, "3051", "407217", 1603l, DateUtils.stringToDate("2018-05-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.an", "安维", "wei.an@17zuoye.com", 0f, 0f, 1, "3052", "883385", 1603l, DateUtils.stringToDate("2018-05-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kai.chen.b", "陈凯B", "kai.chen.b@17zuoye.com", 0f, 0f, 1, "3072", "234295", 1603l, DateUtils.stringToDate("2018-06-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ming.su", "苏明", "ming.su@17zuoye.com", 0f, 0f, 1, "3082", "600609", 1603l, DateUtils.stringToDate("2018-04-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiakun.xie", "谢侠锟", "xiakun.xie@17zuoye.com", 0f, 0f, 1, "3112", "918346", 1603l, DateUtils.stringToDate("2018-06-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("le.zhang", "张乐", "le.zhang@17zuoye.com", 0f, 0f, 1, "3176", "179763", 1603l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jigang.liu", "刘吉刚A", "jigang.liu@17zuoye.com", 0f, 0f, 1, "3286", "854166", 1603l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junhai.yang", "杨军海", "junhai.yang@17zuoye.com", 0f, 0f, 1, "4003", "752943", 1599l, DateUtils.stringToDate("2018-05-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kailu.zhao", "赵凯露", "kailu.zhao@17zuoye.com", 0f, 0f, 1, "4004", "278713", 1603l, DateUtils.stringToDate("2018-05-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuan.ping", "平原", "yuan.ping@17zuoye.com", 0f, 0f, 1, "4005", "617171", 1602l, DateUtils.stringToDate("2018-05-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qiufeng.cheng", "程秋风", "qiufeng.cheng@17zuoye.com", 0f, 0f, 1, "4008", "551333", 1603l, DateUtils.stringToDate("2018-05-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shisheng.huang", "黄世胜", "shisheng.huang@17zuoye.com", 0f, 0f, 1, "4009", "032732", 1601l, DateUtils.stringToDate("2018-05-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiujuan.lu", "卢秀娟", "xiujuan.lu@17zuoye.com", 0f, 0f, 1, "4011", "331531", 1603l, DateUtils.stringToDate("2018-05-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuaitang.geng", "耿帅堂", "shuaitang.geng@17zuoye.com", 0f, 0f, 1, "4012", "432871", 1603l, DateUtils.stringToDate("2018-05-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hongda.zhu", "朱洪达", "hongda.zhu@17zuoye.com", 0f, 0f, 1, "4013", "268668", 1603l, DateUtils.stringToDate("2018-05-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuanqing.wang", "王元青", "yuanqing.wang@17zuoye.com", 0f, 0f, 1, "4015", "228169", 1601l, DateUtils.stringToDate("2018-05-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiangtao.liang", "梁江涛", "jiangtao.liang@17zuoye.com", 0f, 0f, 1, "4016", "870338", 1599l, DateUtils.stringToDate("2018-05-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jie.liu.e", "刘杰E", "jie.liu.e@17zuoye.com", 0f, 0f, 1, "4018", "620259", 1599l, DateUtils.stringToDate("2018-05-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bo.guan", "贯波", "bo.guan@17zuoye.com", 0f, 0f, 1, "4019", "976727", 1599l, DateUtils.stringToDate("2018-05-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huihui.geng", "耿卉卉", "huihui.geng@17zuoye.com", 0f, 0f, 1, "4020", "037835", 1599l, DateUtils.stringToDate("2018-05-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huanlong.li", "李洹龙", "huanlong.li@17zuoye.com", 0f, 0f, 1, "4021", "459334", 1601l, DateUtils.stringToDate("2018-05-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chenyu.xu", "胥陈彧", "chenyu.xu@17zuoye.com", 0f, 0f, 1, "4022", "663376", 1599l, DateUtils.stringToDate("2018-05-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shaojia.yuan", "苑少佳", "shaojia.yuan@17zuoye.com", 0f, 0f, 1, "4023", "819339", 1602l, DateUtils.stringToDate("2018-05-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xianlong.zhang", "张现龙", "xianlong.zhang@17zuoye.com", 0f, 0f, 1, "4024", "544192", 1603l, DateUtils.stringToDate("2018-05-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liling.xin", "辛立岭", "liling.xin@17zuoye.com", 0f, 0f, 1, "4025", "309268", 1601l, DateUtils.stringToDate("2018-05-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qingdong.lian", "连庆东", "qingdong.lian@17zuoye.com", 0f, 0f, 1, "4026", "500645", 1605l, DateUtils.stringToDate("2018-05-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yachao.lu", "卢亚超", "yachao.lu@17zuoye.com", 0f, 0f, 1, "4027", "450291", 1603l, DateUtils.stringToDate("2018-06-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("sinan.chen", "陈司南", "sinan.chen@17zuoye.com", 0f, 0f, 1, "4028", "120764", 1602l, DateUtils.stringToDate("2018-08-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lin.wang.a", "王琳A", "lin.wang.a@17zuoye.com", 0f, 0f, 1, "4029", "005802", 1601l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ran.wang", "王然", "ran.wang@17zuoye.com", 0f, 0f, 1, "4031", "477907", 1605l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lifei.gong", "龚丽飞", "lifei.gong@17zuoye.com", 0f, 0f, 1, "4033", "584086", 1603l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ge.zhang", "张歌", "ge.zhang@17zuoye.com", 0f, 0f, 1, "4034", "350090", 1603l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ru.gao", "高茹", "ru.gao@17zuoye.com", 0f, 0f, 1, "4035", "852338", 1603l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yixin.tang", "唐艺芯", "yixin.tang@17zuoye.com", 0f, 0f, 1, "4036", "104757", 1605l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("daqing.dong", "董大庆", "daqing.dong@17zuoye.com", 0f, 0f, 1, "4037", "155323", 1605l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaofang.han", "韩晓芳", "xiaofang.han@17zuoye.com", 0f, 0f, 1, "4038", "918465", 1605l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bin.liu", "刘彬", "bin.liu@17zuoye.com", 0f, 0f, 1, "4039", "705653", 1605l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("congcong.wang", "王聪聪", "congcong.wang@17zuoye.com", 0f, 0f, 1, "4040", "783443", 1599l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.wang.c", "王晶C", "jing.wang.c@17zuoye.com", 0f, 0f, 1, "4044", "051541", 1601l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuang.li", "李爽", "shuang.li@17zuoye.com", 0f, 0f, 1, "4046", "526089", 1603l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qi.zhang.a", "张琪A", "qi.zhang.a@17zuoye.com", 0f, 0f, 1, "4047", "011933", 1603l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xin.li.d", "李鑫D", "xin.li.d@17zuoye.com", 0f, 0f, 1, "4048", "426112", 1602l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haoyu.zhang", "张昊宇", "haoyu.zhang@17zuoye.com", 0f, 0f, 1, "4049", "747355", 1601l, DateUtils.stringToDate("2018-06-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiaqi.li", "李佳琦", "jiaqi.li@17zuoye.com", 0f, 0f, 1, "4050", "110905", 1603l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jue.wang", "王珏", "jue.wang@17zuoye.com", 0f, 0f, 1, "4051", "243024", 1599l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuya.cao", "曹舒雅", "shuya.cao@17zuoye.com", 0f, 0f, 1, "4053", "964026", 1603l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lina.jia", "贾丽娜", "lina.jia@17zuoye.com", 0f, 0f, 1, "4056", "947178", 1601l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lingfeng.chen", "陈凌枫", "lingfeng.chen@17zuoye.com", 0f, 0f, 1, "4060", "946754", 1601l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiejie.zhu", "朱杰杰", "jiejie.zhu@17zuoye.com", 0f, 0f, 1, "4061", "316590", 1603l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tingting.cao", "曹婷婷", "tingting.cao@17zuoye.com", 0f, 0f, 1, "4062", "519607", 1601l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tingting.wu", "巫婷婷", "tingting.wu@17zuoye.com", 0f, 0f, 1, "4064", "674075", 1601l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuanping.chen", "陈园萍", "yuanping.chen@17zuoye.com", 0f, 0f, 1, "4066", "777243", 1603l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qike.zheng", "郑琪珂", "qike.zheng@17zuoye.com", 0f, 0f, 1, "4067", "526094", 1604l, DateUtils.stringToDate("2018-06-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaofeng.qiu ", "邱晓峰", "xiaofeng.qiu @17zuoye.com", 0f, 0f, 1, "4068", "914601", 1604l, DateUtils.stringToDate("2018-06-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.zheng", "郑颜", "yan.zheng@17zuoye.com", 0f, 0f, 1, "4069", "283433", 1601l, DateUtils.stringToDate("2018-06-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhinan.li", "李智楠", "zhinan.li@17zuoye.com", 0f, 0f, 1, "4072", "152499", 1603l, DateUtils.stringToDate("2018-06-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shaofen.feng", "封绍芬", "shaofen.feng@17zuoye.com", 0f, 0f, 1, "4073", "752642", 1605l, DateUtils.stringToDate("2018-06-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ruifeng.jin", "金锐锋", "ruifeng.jin@17zuoye.com", 0f, 0f, 1, "4074", "750459", 1599l, DateUtils.stringToDate("2018-06-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hong.zhang.a", "张宏A", "hong.zhang.a@17zuoye.com", 0f, 0f, 1, "4075", "992712", 1599l, DateUtils.stringToDate("2018-06-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuejing.li", "李雪景", "xuejing.li@17zuoye.com", 0f, 0f, 1, "4076", "378435", 1599l, DateUtils.stringToDate("2018-06-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rui.chu", "褚蕊", "rui.chu@17zuoye.com", 0f, 0f, 1, "4077", "099736", 1603l, DateUtils.stringToDate("2018-06-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiang.li.c", "李想C", "xiang.li.c@17zuoye.com", 0f, 0f, 1, "4078", "259130", 1599l, DateUtils.stringToDate("2018-06-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kaibo.he", "何开博", "kaibo.he@17zuoye.com", 0f, 0f, 1, "4079", "489775", 1603l, DateUtils.stringToDate("2018-06-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuai.han", "韩帅", "shuai.han@17zuoye.com", 0f, 0f, 1, "4080", "244513", 1599l, DateUtils.stringToDate("2018-06-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhenhua.li", "李振华", "zhenhua.li@17zuoye.com", 0f, 0f, 1, "4081", "286358", 1605l, DateUtils.stringToDate("2018-06-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiayan.long", "龙加燕", "jiayan.long@17zuoye.com", 0f, 0f, 1, "4082", "158058", 1605l, DateUtils.stringToDate("2018-06-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.pan", "潘薇", "wei.pan@17zuoye.com", 0f, 0f, 1, "4083", "796417", 1605l, DateUtils.stringToDate("2018-06-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kuan.li", "李宽", "kuan.li@17zuoye.com", 0f, 0f, 1, "4084", "877827", 1599l, DateUtils.stringToDate("2018-06-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.zhang.f", "张伟F", "wei.zhang.f@17zuoye.com", 0f, 0f, 1, "4085", "960044", 1599l, DateUtils.stringToDate("2018-06-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jieyu.chen", "陈杰宇", "jieyu.chen@17zuoye.com", 0f, 0f, 1, "4086", "562379", 1599l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dehang.jia", "贾德航", "dehang.jia@17zuoye.com", 0f, 0f, 1, "4087", "740033", 1599l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinyi.jiang", "蒋心怡", "xinyi.jiang@17zuoye.com", 0f, 0f, 1, "4091", "054538", 1603l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dan.jiang", "姜丹", "dan.jiang@17zuoye.com", 0f, 0f, 1, "4093", "587362", 1604l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hui.zheng", "郑晖", "hui.zheng@17zuoye.com", 0f, 0f, 1, "4094", "161340", 1605l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("quan.yuan.a", "袁全A", "quan.yuan.a@17zuoye.com", 0f, 0f, 1, "4095", "518300", 1605l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zunrui.liu", "刘尊瑞", "zunrui.liu@17zuoye.com", 0f, 0f, 1, "4097", "412487", 1605l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.lin", "林岩", "yan.lin@17zuoye.com", 0f, 0f, 1, "4098", "097545", 1601l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ming.li.a", "李明", "ming.li.a@17zuoye.com", 0f, 0f, 1, "4099", "475439", 1599l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yiran.wei", "魏一然", "yiran.wei@17zuoye.com", 0f, 0f, 1, "4101", "677364", 1599l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liwei.wang", "王丽微", "liwei.wang@17zuoye.com", 0f, 0f, 1, "4104", "204061", 1601l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoyue.bai", "白小玥", "xiaoyue.bai@17zuoye.com", 0f, 0f, 1, "4105", "320935", 1603l, DateUtils.stringToDate("2018-06-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiapei.zhao", "赵佳培", "jiapei.zhao@17zuoye.com", 0f, 0f, 1, "4109", "552719", 1602l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.ma", "马瑜", "yu.ma@17zuoye.com", 0f, 0f, 1, "4111", "202516", 1601l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zongying.fu", "付宗英", "zongying.fu@17zuoye.com", 0f, 0f, 1, "4112", "987139", 1601l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yidan.zhang", "张一丹", "yidan.zhang@17zuoye.com", 0f, 0f, 1, "4113", "569520", 1603l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hao.wei", "魏昊", "hao.wei@17zuoye.com", 0f, 0f, 1, "4114", "588488", 1603l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zisheng.wang", "王子胜", "zisheng.wang@17zuoye.com", 0f, 0f, 1, "4117", "208916", 1603l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xian.chen", "陈娴", "xian.chen@17zuoye.com", 0f, 0f, 1, "4118", "190271", 1599l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhuo.huang", "黄卓", "zhuo.huang@17zuoye.com", 0f, 0f, 1, "4119", "583410", 1600l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guanghua.jiang", "姜广华", "guanghua.jiang@17zuoye.com", 0f, 0f, 1, "4120", "743776", 1603l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yingwei.zhang", "张英伟", "yingwei.zhang@17zuoye.com", 0f, 0f, 1, "4121", "593530", 1601l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanyan.li", "李艳艳", "yanyan.li@17zuoye.com", 0f, 0f, 1, "4122", "124249", 1599l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yige.zhang", "张祎歌", "yige.zhang@17zuoye.com", 0f, 0f, 1, "4123", "838865", 1603l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tiantian.li", "李田田", "tiantian.li@17zuoye.com", 0f, 0f, 1, "4124", "690164", 1605l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhen.li", "李珍", "zhen.li@17zuoye.com", 0f, 0f, 1, "4125", "994117", 1601l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rui.su", "苏芮", "rui.su@17zuoye.com", 0f, 0f, 1, "4126", "978273", 1601l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiangfeng.jia", "贾祥凤", "xiangfeng.jia@17zuoye.com", 0f, 0f, 1, "4128", "271340", 1601l, DateUtils.stringToDate("2018-06-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lina.sun", "孙丽娜", "lina.sun@17zuoye.com", 0f, 0f, 1, "4131", "924149", 1603l, DateUtils.stringToDate("2018-06-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.ren", "任鹏", "peng.ren@17zuoye.com", 0f, 0f, 1, "4132", "964938", 1603l, DateUtils.stringToDate("2018-06-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuyan.yin", "殷玉燕", "yuyan.yin@17zuoye.com", 0f, 0f, 1, "4133", "262067", 1603l, DateUtils.stringToDate("2018-06-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ningning.kong", "孔宁宁", "ningning.kong@17zuoye.com", 0f, 0f, 1, "4134", "249538", 1603l, DateUtils.stringToDate("2018-06-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zuyue.liu", "刘祖岳", "zuyue.liu@17zuoye.com", 0f, 0f, 1, "4135", "940089", 1603l, DateUtils.stringToDate("2018-06-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jie.hui", "惠杰", "jie.hui@17zuoye.com", 0f, 0f, 1, "4138", "730527", 1601l, DateUtils.stringToDate("2018-06-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chen.qian", "钱琛", "chen.qian@17zuoye.com", 0f, 0f, 1, "4139", "530608", 1601l, DateUtils.stringToDate("2018-06-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fuyao.lv", "吕馥瑶", "fuyao.lv@17zuoye.com", 0f, 0f, 1, "4141", "901883", 1601l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yifan.feng", "封一凡", "yifan.feng@17zuoye.com", 0f, 0f, 1, "4142", "447520", 1601l, DateUtils.stringToDate("2018-06-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount(" bo.zhang.a​", "张波A", " bo.zhang.a​@17zuoye.com", 0f, 0f, 1, "4150", "537898", 1601l, DateUtils.stringToDate("2018-06-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yiyan.wang", "汪义燕", "yiyan.wang@17zuoye.com", 0f, 0f, 1, "4151", "616911", 1601l, DateUtils.stringToDate("2019-01-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingshi.wang", "王竟实", "jingshi.wang@17zuoye.com", 0f, 0f, 1, "4156", "573665", 1601l, DateUtils.stringToDate("2018-06-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ruixin.wang", "王瑞鑫", "ruixin.wang@17zuoye.com", 0f, 0f, 1, "4157", "496904", 1605l, DateUtils.stringToDate("2018-06-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huining.yang", "杨蕙宁", "huining.yang@17zuoye.com", 0f, 0f, 1, "4158", "869535", 1605l, DateUtils.stringToDate("2018-06-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("donghe.wang", "王东鹤", "donghe.wang@17zuoye.com", 0f, 0f, 1, "4159", "899633", 1601l, DateUtils.stringToDate("2018-06-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiya.wang", "王夕雅", "xiya.wang@17zuoye.com", 0f, 0f, 1, "4161", "322105", 1603l, DateUtils.stringToDate("2018-06-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinping.wen", "温新平", "xinping.wen@17zuoye.com", 0f, 0f, 1, "4162", "504896", 1605l, DateUtils.stringToDate("2018-06-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ke.zhang.a", "张可A", "ke.zhang.a@17zuoye.com", 0f, 0f, 1, "4164", "596870", 1602l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("biao.ye", "叶飙", "biao.ye@17zuoye.com", 0f, 0f, 1, "4166", "569379", 1603l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guangxiang.jin", "金光祥", "guangxiang.jin@17zuoye.com", 0f, 0f, 1, "4167", "124388", 1599l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinying.cheng", "程馨莹", "xinying.cheng@17zuoye.com", 0f, 0f, 1, "4168", "112834", 1605l, DateUtils.stringToDate("2018-06-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuxia.zhao", "赵淑霞", "shuxia.zhao@17zuoye.com", 0f, 0f, 1, "4169", "747367", 1599l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tiantian.wu", "吴甜甜", "tiantian.wu@17zuoye.com", 0f, 0f, 1, "4170", "604915", 1605l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhicheng.zhang", "张志成", "zhicheng.zhang@17zuoye.com", 0f, 0f, 1, "4171", "672281", 1599l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("miaomiao.zhen", "甄淼淼", "miaomiao.zhen@17zuoye.com", 0f, 0f, 1, "4172", "042989", 1599l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuwei.huang", "黄俞卫", "yuwei.huang@17zuoye.com", 0f, 0f, 1, "4173", "400423", 1599l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yike.zhu", "祝艺珂", "yike.zhu@17zuoye.com", 0f, 0f, 1, "4174", "998364", 1605l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenxue.ding", "丁文雪", "wenxue.ding@17zuoye.com", 0f, 0f, 1, "4175", "396083", 1599l, DateUtils.stringToDate("2018-06-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haojie.guo", "郭浩杰", "haojie.guo@17zuoye.com", 0f, 0f, 1, "4176", "236980", 1600l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhou.ji", "冀洲", "zhou.ji@17zuoye.com", 0f, 0f, 1, "4177", "882527", 1599l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yining.yu", "余伊宁", "yining.yu@17zuoye.com", 0f, 0f, 1, "4178", "019679", 1603l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rui.jia", "贾蕊", "rui.jia@17zuoye.com", 0f, 0f, 1, "4179", "389016", 1605l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunda.song", "宋春达", "chunda.song@17zuoye.com", 0f, 0f, 1, "4180", "146836", 1599l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yixuan.zhang", "张轶轩", "yixuan.zhang@17zuoye.com", 0f, 0f, 1, "4181", "741154", 1605l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kaiyue.jing", "井凯悦", "kaiyue.jing@17zuoye.com", 0f, 0f, 1, "4183", "173002", 1603l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hening.hu", "胡贺宁", "hening.hu@17zuoye.com", 0f, 0f, 1, "4185", "060207", 1603l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.lin.a", "林燕A", "yan.lin.a@17zuoye.com", 0f, 0f, 1, "4186", "563644", 1603l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaojuan.zhang ", "张晓娟", "xiaojuan.zhang @17zuoye.com", 0f, 0f, 1, "4187", "291746", 1602l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("changyu.wei", "魏长宇", "changyu.wei@17zuoye.com", 0f, 0f, 1, "4189", "028659", 1605l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiale.guo", "郭佳乐", "jiale.guo@17zuoye.com", 0f, 0f, 1, "4190", "537458", 1603l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jinsong.xiao", "肖劲松", "jinsong.xiao@17zuoye.com", 0f, 0f, 1, "4191", "196714", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shan.zhang", "张珊", "shan.zhang@17zuoye.com", 0f, 0f, 1, "4193", "934762", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mengmeng.guo", "郭盟盟", "mengmeng.guo@17zuoye.com", 0f, 0f, 1, "4197", "418879", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhengwei.hua", "华正伟", "zhengwei.hua@17zuoye.com", 0f, 0f, 1, "4198", "300251", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meng.hao", "郝梦", "meng.hao@17zuoye.com", 0f, 0f, 1, "4199", "563215", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lingli.yin", "尹伶俐", "lingli.yin@17zuoye.com", 0f, 0f, 1, "4200", "298874", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peipei.wang.a", "王培培A", "peipei.wang.a@17zuoye.com", 0f, 0f, 1, "4201", "399841", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xin.zhang.c", "张鑫C", "xin.zhang.c@17zuoye.com", 0f, 0f, 1, "4202", "775905", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xin.li.c", "李欣C", "xin.li.c@17zuoye.com", 0f, 0f, 1, "4203", "307162", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qian.tian", "田倩", "qian.tian@17zuoye.com", 0f, 0f, 1, "4204", "749900", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("keke.wang", "王克克", "keke.wang@17zuoye.com", 0f, 0f, 1, "4205", "066335", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bing.wang", "王兵", "bing.wang@17zuoye.com", 0f, 0f, 1, "4206", "906460", 1602l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yajun.wang", "王雅君", "yajun.wang@17zuoye.com", 0f, 0f, 1, "4207", "579468", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingjing.zhao", "赵京京", "jingjing.zhao@17zuoye.com", 0f, 0f, 1, "4209", "040393", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yong.li.b", "李永B", "yong.li.b@17zuoye.com", 0f, 0f, 1, "4210", "031088", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoying.yan", "闫筱莹", "xiaoying.yan@17zuoye.com", 0f, 0f, 1, "4212", "939021", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiayu.song", "宋嘉钰", "jiayu.song@17zuoye.com", 0f, 0f, 1, "4213", "530110", 1599l, DateUtils.stringToDate("2018-07-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("caiyun.gao", "高彩云", "caiyun.gao@17zuoye.com", 0f, 0f, 1, "4214", "500232", 1602l, DateUtils.stringToDate("2018-07-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhiming.huang", "黄志明", "zhiming.huang@17zuoye.com", 0f, 0f, 1, "4215", "665032", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yongrui.tie", "铁永蕊", "yongrui.tie@17zuoye.com", 0f, 0f, 1, "4216", "282684", 1605l, DateUtils.stringToDate("2018-07-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingru.luan", "栾景茹", "jingru.luan@17zuoye.com", 0f, 0f, 1, "4217", "933729", 1605l, DateUtils.stringToDate("2018-07-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mengting.pang", "庞孟婷", "mengting.pang@17zuoye.com", 0f, 0f, 1, "4218", "842394", 1599l, DateUtils.stringToDate("2018-07-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qianhua.mo", "莫倩华", "qianhua.mo@17zuoye.com", 0f, 0f, 1, "4219", "934392", 1602l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yangyang.li", "李洋洋", "yangyang.li@17zuoye.com", 0f, 0f, 1, "4221", "211074", 1602l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wentao.wei", "魏文涛", "wentao.wei@17zuoye.com", 0f, 0f, 1, "4222", "577008", 1604l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xin.li.e", "李欣E", "xin.li.e@17zuoye.com", 0f, 0f, 1, "4223", "001784", 1599l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hao.feng", "冯浩", "hao.feng@17zuoye.com", 0f, 0f, 1, "4224", "382893", 1603l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("song.wang.a", "王松A", "song.wang.a@17zuoye.com", 0f, 0f, 1, "4225", "203125", 1601l, DateUtils.stringToDate("2018-07-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jie.deng", "邓杰", "jie.deng@17zuoye.com", 0f, 0f, 1, "4227", "764361", 1605l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhilin.liu ", "刘志麟", "zhilin.liu @17zuoye.com", 0f, 0f, 1, "4228", "750244", 1602l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fen.yang.a", "杨芬A", "fen.yang.a@17zuoye.com", 0f, 0f, 1, "4229", "169223", 1605l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yinghui.luan", "栾颖慧", "yinghui.luan@17zuoye.com", 0f, 0f, 1, "4230", "664079", 1599l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinyu.sun", "孙鑫禹", "xinyu.sun@17zuoye.com", 0f, 0f, 1, "4231", "087224", 1605l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianfang.liu", "刘剑芳", "jianfang.liu@17zuoye.com", 0f, 0f, 1, "4232", "449957", 1605l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tianyou.wang", "王天佑", "tianyou.wang@17zuoye.com", 0f, 0f, 1, "4234", "160706", 1605l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("songxue.li", "李松雪", "songxue.li@17zuoye.com", 0f, 0f, 1, "4235", "506417", 1605l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guangqing.liu", "刘广清", "guangqing.liu@17zuoye.com", 0f, 0f, 1, "4236", "372418", 1599l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huan.liu", "刘欢", "huan.liu@17zuoye.com", 0f, 0f, 1, "4237", "849988", 1605l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("min.yang", "杨敏", "min.yang@17zuoye.com", 0f, 0f, 1, "4238", "359611", 1599l, DateUtils.stringToDate("2018-07-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kexi.yuan", "苑克玺", "kexi.yuan@17zuoye.com", 0f, 0f, 1, "4239", "598898", 1603l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("siyao.wang", "王思瑶", "siyao.wang@17zuoye.com", 0f, 0f, 1, "4240", "249532", 1600l, DateUtils.stringToDate("2018-07-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuang.wei", "魏爽", "shuang.wei@17zuoye.com", 0f, 0f, 1, "4242", "944556", 1601l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiqun.zhang", "张西群", "xiqun.zhang@17zuoye.com", 0f, 0f, 1, "4243", "079775", 1599l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingting.tao", "陶京婷", "jingting.tao@17zuoye.com", 0f, 0f, 1, "4244", "157183", 1602l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dan.liu.b", "刘丹B", "dan.liu.b@17zuoye.com", 0f, 0f, 1, "4247", "730610", 1605l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yalin.shi", "石雅琳", "yalin.shi@17zuoye.com", 0f, 0f, 1, "4248", "262116", 1603l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yong.han", "韩咏", "yong.han@17zuoye.com", 0f, 0f, 1, "4249", "165320", 1599l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiatong.bi", "毕嘉桐", "jiatong.bi@17zuoye.com", 0f, 0f, 1, "4250", "482390", 1605l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.wang.e", "王朋E", "peng.wang.e@17zuoye.com", 0f, 0f, 1, "4251", "507414", 1601l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dongning.liu", "刘冬宁", "dongning.liu@17zuoye.com", 0f, 0f, 1, "4253", "847754", 1599l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("baiqiang.wen", "温百强", "baiqiang.wen@17zuoye.com", 0f, 0f, 1, "4254", "037953", 1605l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shiyu.li", "李世宇", "shiyu.li@17zuoye.com", 0f, 0f, 1, "4255", "363605", 1605l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cong.wang", "王聪", "cong.wang@17zuoye.com", 0f, 0f, 1, "4256", "507155", 1603l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weipeng.zhang", "张伟鹏", "weipeng.zhang@17zuoye.con", 0f, 0f, 1, "4257", "274131", 1605l, DateUtils.stringToDate("2018-07-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qiancheng.yan", "闫前程", "qiancheng.yan@17zuoye.com", 0f, 0f, 1, "4258", "607305", 1603l, DateUtils.stringToDate("2018-06-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("le.yang", "杨乐", "le.yang@17zuoye.com", 0f, 0f, 1, "4259", "297960", 1603l, DateUtils.stringToDate("2018-07-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lixian.liu", "刘丽仙", "lixian.liu@17zuoye.com", 0f, 0f, 1, "4261", "212030", 1603l, DateUtils.stringToDate("2018-07-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.yan", "颜彦", "yan.yan@17zuoye.com", 0f, 0f, 1, "4264", "301785", 1604l, DateUtils.stringToDate("2018-07-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.wang.c", "汪巍C", "wei.wang.c@17zuoye.com", 0f, 0f, 1, "4265", "946334", 1599l, DateUtils.stringToDate("2018-07-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("panpan.chen", "陈盼盼", "panpan.chen@17zuoye.com", 0f, 0f, 1, "4267", "560324", 1605l, DateUtils.stringToDate("2018-07-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiaying.du", "杜佳颖", "jiaying.du@17zuoye.com", 0f, 0f, 1, "4268", "915595", 1601l, DateUtils.stringToDate("2018-07-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xingbo.luo", "罗兴博", "xingbo.luo@17zuoye.com", 0f, 0f, 1, "4269", "561332", 1601l, DateUtils.stringToDate("2018-07-11", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenqing.meng", "孟文晴", "wenqing.meng@17zuoye.com", 0f, 0f, 1, "4270", "894158", 1605l, DateUtils.stringToDate("2018-07-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingxin.sun", "孙景欣", "jingxin.sun@17zuoye.com", 0f, 0f, 1, "4271", "243838", 1602l, DateUtils.stringToDate("2018-07-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qingsong.feng", "冯青松", "qingsong.feng@17zuoye.com", 0f, 0f, 1, "4272", "868108", 1599l, DateUtils.stringToDate("2018-07-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.zhou", "周威", "wei.zhou@17zuoye.com", 0f, 0f, 1, "4273", "650690", 1599l, DateUtils.stringToDate("2018-07-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huichao.liu.a", "刘慧超", "huichao.liu.a@17zuoye.com", 0f, 0f, 1, "4274", "861422", 1602l, DateUtils.stringToDate("2018-07-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuo.cao", "曹烁", "shuo.cao@17zuoye.com", 0f, 0f, 1, "4275", "404259", 1602l, DateUtils.stringToDate("2018-07-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liqing.li", "李利清", "liqing.li@17zuoye.com", 0f, 0f, 1, "4276", "983328", 1605l, DateUtils.stringToDate("2018-07-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yifan.zhang.b", "张艺凡", "yifan.zhang.b@17zuoye.com", 0f, 0f, 1, "4277", "164762", 1601l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinyu.liu", "刘新雨", "xinyu.liu@17zuoye.com", 0f, 0f, 1, "4278", "551457", 1603l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("li.wang.d", "王莉D", "li.wang.d@17zuoye.com", 0f, 0f, 1, "4279", "866290", 1603l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhidong.zhang", "张志栋", "zhidong.zhang@17zuoye.com", 0f, 0f, 1, "4280", "261788", 1599l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jia.yao", "姚佳", "jia.yao@17zuoye.com", 0f, 0f, 1, "4281", "906726", 1599l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ping.dong", "董平", "ping.dong@17zuoye.com", 0f, 0f, 1, "4282", "536940", 1599l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jie.lou", "楼界", "jie.lou@17zuoye.com", 0f, 0f, 1, "4283", "979109", 1599l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuchen.li", "李宇晨", "yuchen.li@17zuoye.com", 0f, 0f, 1, "4284", "931070", 1599l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qiubo.zhou", "周秋波", "qiubo.zhou@17zuoye.com", 0f, 0f, 1, "4285", "275625", 1599l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yongli.jia", "贾永利", "yongli.jia@17zuoye.com", 0f, 0f, 1, "4286", "408332", 1603l, DateUtils.stringToDate("2018-07-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liang.ma", "马良", "liang.ma@17zuoye.com", 0f, 0f, 1, "4287", "357897", 1603l, DateUtils.stringToDate("2018-07-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chen.chen.b", "陈晨B", "chen.chen.b@17zuoye.com", 0f, 0f, 1, "4288", "016393", 1605l, DateUtils.stringToDate("2018-07-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lingyun.wang", "王凌云", "lingyun.wang@17zuoye.com", 0f, 0f, 1, "4289", "516373", 1605l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yufei.gong", "龚羽菲", "yufei.gong@17zuoye.com", 0f, 0f, 1, "4291", "238044", 1605l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.huang.a", "黄伟A", "wei.huang.a@17zuoye.com", 0f, 0f, 1, "4292", "718403", 1599l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinling.wang", "王心灵", "xinling.wang@17zuoye.com", 0f, 0f, 1, "4294", "081069", 1604l, DateUtils.stringToDate("2018-07-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yaping.chen.a", "陈雅萍", "yaping.chen.a@17zuoye.com", 0f, 0f, 1, "4295", "417484", 1603l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jinhui.xu", "许金徽", "jinhui.xu@17zuoye.com", 0f, 0f, 1, "4296", "713592", 1599l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hailing.feng", "冯海玲", "hailing.feng@17zuoye.com", 0f, 0f, 1, "4297", "787908", 1602l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shan.he", "何珊", "shan.he@17zuoye.com", 0f, 0f, 1, "4299", "889591", 1599l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuchen.tao", "陶宇宸", "yuchen.tao@17zuoye.com", 0f, 0f, 1, "4300", "723380", 1605l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yingjie.fu", "付英杰", "yingjie.fu@17zuoye.com", 0f, 0f, 1, "4301", "717223", 1605l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yue.zhang", "张越", "yue.zhang@17zuoye.com", 0f, 0f, 1, "4302", "524716", 1605l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yijun.wang", "王一君", "yijun.wang@17zuoye.com", 0f, 0f, 1, "4305", "606370", 1600l, DateUtils.stringToDate("2018-07-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhenjiang.yang", "杨振江", "zhenjiang.yang@17zuoye.com", 0f, 0f, 1, "4307", "613166", 1604l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hongyang.liu", "刘洪洋", "hongyang.liu@17zuoye.com", 0f, 0f, 1, "4308", "584276", 1605l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingsha.luan", "栾明沙", "mingsha.luan@17zuoye.com", 0f, 0f, 1, "4309", "012626", 1601l, DateUtils.stringToDate("2018-07-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhouyang.xue", "薛舟洋", "zhouyang.xue@17zuoye.com", 0f, 0f, 1, "4310", "261706", 1601l, DateUtils.stringToDate("2018-07-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junye.tao", "陶钧冶", "junye.tao@17zuoye.com", 0f, 0f, 1, "4312", "951114", 1603l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lele.cao", "曹乐乐", "lele.cao@17zuoye.com", 0f, 0f, 1, "4313", "251248", 1603l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chenchen.zhou", "周晨晨", "chenchen.zhou@17zuoye.com", 0f, 0f, 1, "4314", "935516", 1603l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jinjin.zhang", "张进进", "jinjin.zhang@17zuoye.com", 0f, 0f, 1, "4315", "991676", 1605l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhaoxia.yan", "闫朝霞", "zhaoxia.yan@17zuoye.com", 0f, 0f, 1, "4316", "421083", 1601l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rongxuan.qi", "齐蓉璇", "rongxuan.qi@17zuoye.com", 0f, 0f, 1, "4317", "673934", 1601l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuan.li.a", "李原A", "yuan.li.a@17zuoye.com", 0f, 0f, 1, "4320", "621223", 1605l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("changsheng.liu", "刘长胜", "changsheng.liu@17zuoye.com", 0f, 0f, 1, "4321", "885436", 1599l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fei.wang", "王飞", "fei.wang@17zuoye.com", 0f, 0f, 1, "4324", "585309", 1602l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peiwen.rao", "饶沛文", "peiwen.rao@17zuoye.com", 0f, 0f, 1, "4325", "180117", 1603l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xing.liang", "梁星", "xing.liang@17zuoye.com", 0f, 0f, 1, "4327", "763824", 1599l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yongping.li", "李永平", "yongping.li@17zuoye.com", 0f, 0f, 1, "4328", "678314", 1601l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("da.feng", "冯达", "da.feng@17zuoye.com", 0f, 0f, 1, "4329", "026662", 1599l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.peng", "彭鹏", "peng.peng@17zuoye.com", 0f, 0f, 1, "4330", "109958", 1601l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zebin.liu", "刘则彬", "zebin.liu@17zuoye.com", 0f, 0f, 1, "4331", "511184", 1605l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yang.liu.h", "刘洋H", "yang.liu.h@17zuoye.com", 0f, 0f, 1, "4332", "254274", 1603l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiuqi.zhao", "赵修起", "xiuqi.zhao@17zuoye.com", 0f, 0f, 1, "4333", "427832", 1603l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuezhen.zhang", "张雪珍", "xuezhen.zhang@17zuoye.com", 0f, 0f, 1, "4334", "318598", 1605l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.chen", "陈晶", "jing.chen@17zuoye.com", 0f, 0f, 1, "4336", "128071", 1603l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yang.gan", "甘洋", "yang.gan@17zuoye.com", 0f, 0f, 1, "4337", "611416", 1603l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ming.tan", "檀明", "ming.tan@17zuoye.com", 0f, 0f, 1, "4339", "123497", 1603l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhuoying.yan", "闫卓英", "zhuoying.yan@17zuoye.com", 0f, 0f, 1, "4340", "709219", 1601l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianbo.ma", "马建波", "jianbo.ma@17zuoye.com", 0f, 0f, 1, "4341", "212698", 1601l, DateUtils.stringToDate("2018-08-04", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shaofeng.chen", "陈少峰", "shaofeng.chen@17zuoye.com", 0f, 0f, 1, "4342", "595793", 1599l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuoqiu.sun", "孙硕秋", "shuoqiu.sun@17zuoye.com", 0f, 0f, 1, "4343", "297038", 1599l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chao.li.d", "李超D", "chao.li.d@17zuoye.com", 0f, 0f, 1, "4344", "627993", 1605l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("caihong.wang", "王彩虹", "caihong.wang@17zuoye.com", 0f, 0f, 1, "4346", "387003", 1605l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.li.b", "李燕B", "yan.li.b@17zuoye.com", 0f, 0f, 1, "4347", "661298", 1599l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dandan.zheng", "郑丹丹", "dandan.zheng@17zuoye.com", 0f, 0f, 1, "4349", "777943", 1603l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("muyuan.wang", "王牧远", "muyuan.wang@17zuoye.com", 0f, 0f, 1, "4350", "509234", 1605l, DateUtils.stringToDate("2018-08-01", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fanke.cheng", "程凡珂", "fanke.cheng@17zuoye.com", 0f, 0f, 1, "4351", "656167", 1603l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinping.liu", "刘新萍", "xinping.liu@17zuoye.com", 0f, 0f, 1, "4352", "788273", 1605l, DateUtils.stringToDate("2018-08-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chengcheng.jiang", "蒋成成", "chengcheng.jiang@17zuoye.com", 0f, 0f, 1, "4353", "195406", 1605l, DateUtils.stringToDate("2018-08-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("man.wu", "邬曼", "man.wu@17zuoye.com", 0f, 0f, 1, "4354", "334656", 1605l, DateUtils.stringToDate("2018-08-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuan.wang.b", "王璇B", "xuan.wang.b@17zuoye.com", 0f, 0f, 1, "4355", "632627", 1605l, DateUtils.stringToDate("2018-08-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("baofu.zhang", "张保福", "baofu.zhang@17zuoye.com", 0f, 0f, 1, "4357", "574873", 1602l, DateUtils.stringToDate("2018-08-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yang.wang.b", "王洋B", "yang.wang.b@17zuoye.com", 0f, 0f, 1, "4358", "169555", 1604l, DateUtils.stringToDate("2018-08-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.zou", "邹禹", "yu.zou@17zuoye.com", 0f, 0f, 1, "4360", "628803", 1599l, DateUtils.stringToDate("2018-08-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.guo", "国威", "wei.guo@17zuoye.com", 0f, 0f, 1, "4361", "342112", 1599l, DateUtils.stringToDate("2018-08-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haoran.ju", "巨浩然", "haoran.ju@17zuoye.com", 0f, 0f, 1, "4362", "443053", 1603l, DateUtils.stringToDate("2018-08-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.zhang.b", "张莹B", "ying.zhang.b@17zuoye.com", 0f, 0f, 1, "4363", "709760", 1602l, DateUtils.stringToDate("2018-08-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("anni.cai", "蔡安妮", "anni.cai@17zuoye.com", 0f, 0f, 1, "4364", "271253", 1603l, DateUtils.stringToDate("2018-08-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhiwei.liu", "刘志伟", "zhiwei.liu@17zuoye.com", 0f, 0f, 1, "4366", "999932", 1602l, DateUtils.stringToDate("2018-08-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("honglei.zuo", "左红雷", "honglei.zuo@17zuoye.com", 0f, 0f, 1, "4370", "514644", 1599l, DateUtils.stringToDate("2018-08-13", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liwu.qiu", "邱立武", "liwu.qiu@17zuoye.com", 0f, 0f, 1, "4371", "954910", 1601l, DateUtils.stringToDate("2018-08-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("Robert", "robert eugene mccormick jr", "Robert@17zuoye.com", 0f, 0f, 1, "4372", "197009", 1599l, DateUtils.stringToDate("2018-08-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("junmei.zhang", "张军梅", "junmei.zhang@17zuoye.com", 0f, 0f, 1, "4373", "573820", 1599l, DateUtils.stringToDate("2018-08-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingchen.xu", "徐铭晨", "mingchen.xu@17zuoye.com", 0f, 0f, 1, "4375", "136492", 1601l, DateUtils.stringToDate("2018-08-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dongfeng.xue", "薛东峰", "dongfeng.xue@17zuoye.com", 0f, 0f, 1, "4376", "831131", 1602l, DateUtils.stringToDate("2018-08-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("changxia.hu", "胡长霞", "changxia.hu@17zuoye.com", 0f, 0f, 1, "4378", "474042", 1599l, DateUtils.stringToDate("2018-08-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoting.zhu", "朱啸婷", "xiaoting.zhu@17zuoye.com", 0f, 0f, 1, "4379", "736060", 1601l, DateUtils.stringToDate("2018-08-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kai.zhou", "周凯", "kai.zhou@17zuoye.com", 0f, 0f, 1, "4384", "886382", 1605l, DateUtils.stringToDate("2018-08-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fan.shi", "师凡", "fan.shi@17zuoye.com", 0f, 0f, 1, "4385", "601842", 1605l, DateUtils.stringToDate("2018-08-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaomin.shi", "师晓敏", "xiaomin.shi@17zuoye.com", 0f, 0f, 1, "4386", "575620", 1605l, DateUtils.stringToDate("2018-08-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jun.wang.a", "王珺A", "jun.wang.a@17zuoye.com", 0f, 0f, 1, "4387", "146194", 1602l, DateUtils.stringToDate("2018-08-20", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shaodong.fan", "范少懂", "shaodong.fan@17zuoye.com", 0f, 0f, 1, "4388", "888147", 1603l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenlong.meng", "孟文龙", "wenlong.meng@17zuoye.com", 0f, 0f, 1, "4389", "073969", 1603l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mengyu.jia", "贾孟雨", "mengyu.jia@17zuoye.com", 0f, 0f, 1, "4390", "955260", 1599l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhenyang.wang", "王振阳", "zhenyang.wang@17zuoye.com", 0f, 0f, 1, "4391", "808304", 1605l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xueyuan.li", "李雪原", "xueyuan.li@17zuoye.com", 0f, 0f, 1, "4392", "612766", 1603l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiakang.chen", "陈嘉康", "jiakang.chen@17zuoye.com", 0f, 0f, 1, "4393", "147106", 1601l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuanxiang.chang", "常远翔", "yuanxiang.chang@17zuoye.com", 0f, 0f, 1, "4394", "257452", 1603l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kun.wu", "吴坤", "kun.wu@17zuoye.com", 0f, 0f, 1, "4395", "614336", 1604l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cuixia.wang", "王翠霞", "cuixia.wang@17zuoye.com", 0f, 0f, 1, "4397", "254065", 1599l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huan.liu.a", "刘欢A", "huan.liu.a@17zuoye.com", 0f, 0f, 1, "4398", "321721", 1601l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhiqian.ren", "任芝乾", "zhiqian.ren@17zuoye.com", 0f, 0f, 1, "4400", "592494", 1603l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zaihui.li", "李再会", "zaihui.li@17zuoye.com", 0f, 0f, 1, "4401", "716931", 1601l, DateUtils.stringToDate("2018-08-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ting.wang.a", "王婷A", "ting.wang.a@17zuoye.com", 0f, 0f, 1, "4402", "330362", 1603l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meiling.deng", "邓美玲", "meiling.deng@17zuoye.com", 0f, 0f, 1, "4403", "181473", 1603l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rui.hou", "侯瑞", "rui.hou@17zuoye.com", 0f, 0f, 1, "4404", "032233", 1603l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lulu.fan", "范露露", "lulu.fan@17zuoye.com", 0f, 0f, 1, "4405", "726117", 1603l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhihua.he", "贺志华", "zhihua.he@17zuoye.com", 0f, 0f, 1, "4406", "390631", 1603l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yueming.wang", "王明月", "yueming.wang@17zuoye.com", 0f, 0f, 1, "4407", "623992", 1603l, DateUtils.stringToDate("2018-07-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xichuan.zheng", "郑希川", "xichuan.zheng@17zuoye.com", 0f, 0f, 1, "4408", "834590", 1603l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoxue.men", "门小雪", "xiaoxue.men@17zuoye.com", 0f, 0f, 1, "4411", "256226", 1603l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuaikang.chang", "常帅康", "shuaikang.chang@17zuoye.com", 0f, 0f, 1, "4412", "862503", 1603l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hailu.ma", "马路", "hailu.ma@17zuoye.com", 0f, 0f, 1, "4413", "660998", 1603l, DateUtils.stringToDate("2018-08-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qinghua.zhao", "赵庆华", "qinghua.zhao@17zuoye.com", 0f, 0f, 1, "4415", "182573", 1601l, DateUtils.stringToDate("2018-08-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rundong.cao", "曹润东", "rundong.cao@17zuoye.com", 0f, 0f, 1, "4416", "064449", 1599l, DateUtils.stringToDate("2018-08-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lingming.kong", "孔令明", "lingming.kong@17zuoye.com", 0f, 0f, 1, "4417", "890309", 1600l, DateUtils.stringToDate("2018-08-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tao.song.b", "宋涛B", "tao.song.b@17zuoye.com", 0f, 0f, 1, "4418", "532698", 1601l, DateUtils.stringToDate("2018-08-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("you.kang", "康有", "you.kang@17zuoye.com", 0f, 0f, 1, "4419", "201440", 1603l, DateUtils.stringToDate("2018-08-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shiyan.sun", "孙世岩", "shiyan.sun@17zuoye.com", 0f, 0f, 1, "4420", "674630", 1603l, DateUtils.stringToDate("2018-08-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ru.bai", "白茹", "ru.bai@17zuoye.com", 0f, 0f, 1, "4423", "138206", 1601l, DateUtils.stringToDate("2018-07-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hongyan.wei", "魏虹妍", "hongyan.wei@17zuoye.com", 0f, 0f, 1, "4424", "369121", 1605l, DateUtils.stringToDate("2018-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("miao.du", "杜渺", "miao.du@17zuoye.com", 0f, 0f, 1, "4425", "345572", 1599l, DateUtils.stringToDate("2018-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuo.ma", "马硕", "shuo.ma@17zuoye.com", 0f, 0f, 1, "4426", "639805", 1599l, DateUtils.stringToDate("2018-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoyuan.dong", "董晓源", "xiaoyuan.dong@17zuoye.com", 0f, 0f, 1, "4427", "099336", 1603l, DateUtils.stringToDate("2018-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("boxin.wang", "王伯鑫", "boxin.wang@17zuoye.com", 0f, 0f, 1, "4428", "656695", 1605l, DateUtils.stringToDate("2018-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.guo", "郭磊", "lei.guo@17zuoye.com", 0f, 0f, 1, "4430", "153481", 1601l, DateUtils.stringToDate("2018-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meiting.wang", "王美婷", "meiting.wang@17zuoye.com", 0f, 0f, 1, "4431", "533022", 1603l, DateUtils.stringToDate("2018-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lina.dong", "董丽娜", "lina.dong@17zuoye.com", 0f, 0f, 1, "4432", "680071", 1603l, DateUtils.stringToDate("2018-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("pengfei.huang", "黄鹏飞", "pengfei.huang@17zuoye.com", 0f, 0f, 1, "4435", "078073", 1603l, DateUtils.stringToDate("2018-08-27", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiaxin.xie", "谢佳鑫", "jiaxin.xie@17zuoye.com", 0f, 0f, 1, "4436", "952184", 1603l, DateUtils.stringToDate("2018-08-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaotong.ma", "马晓彤", "xiaotong.ma@17zuoye.com", 0f, 0f, 1, "4437", "921935", 1603l, DateUtils.stringToDate("2018-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guijiao.jia", "贾桂姣", "guijiao.jia@17zuoye.com", 0f, 0f, 1, "4439", "418584", 1603l, DateUtils.stringToDate("2018-08-30", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weiqiang.zhao", "赵伟强", "weiqiang.zhao@17zuoye.com", 0f, 0f, 1, "4443", "208458", 1601l, DateUtils.stringToDate("2018-12-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xinchao.zhang", "张新潮", "xinchao.zhang@17zuoye.com", 0f, 0f, 1, "4446", "308967", 1605l, DateUtils.stringToDate("2018-09-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiahan.yang", "杨夏浛", "xiahan.yang@17zuoye.com", 0f, 0f, 1, "4447", "089461", 1599l, DateUtils.stringToDate("2018-09-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("youyu.rong", "荣佑钰", "youyu.rong@17zuoye.com", 0f, 0f, 1, "4448", "076873", 1601l, DateUtils.stringToDate("2018-09-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shaobai.xu", "许少波", "shaobai.xu@17zuoye.com", 0f, 0f, 1, "4450", "107334", 1603l, DateUtils.stringToDate("2018-09-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hui.wang.a", "王会A", "hui.wang.a@17zuoye.com", 0f, 0f, 1, "4451", "395023", 1599l, DateUtils.stringToDate("2018-09-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ziyu.yang", "杨子郁", "ziyu.yang@17zuoye.com", 0f, 0f, 1, "4452", "241226", 1604l, DateUtils.stringToDate("2018-09-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qi.guo.a", "郭旗A", "qi.guo.a@17zuoye.com", 0f, 0f, 1, "4454", "915346", 1603l, DateUtils.stringToDate("2018-08-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("minghong.liu", "刘铭宏", "minghong.liu@17zuoye.com", 0f, 0f, 1, "4460", "217521", 1605l, DateUtils.stringToDate("2018-09-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cong.wang.b", "王聪B", "cong.wang.b@17zuoye.com", 0f, 0f, 1, "4461", "859882", 1605l, DateUtils.stringToDate("2018-09-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ruimin.huai", "淮瑞敏", "ruimin.huai@17zuoye.com", 0f, 0f, 1, "4464", "201937", 1599l, DateUtils.stringToDate("2018-09-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guanying.wang", "王冠英", "guanying.wang@17zuoye.com", 0f, 0f, 1, "4465", "417222", 1599l, DateUtils.stringToDate("2018-09-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yiwei.bo", "柏义伟", "yiwei.bo@17zuoye.com", 0f, 0f, 1, "4466", "547582", 1599l, DateUtils.stringToDate("2018-09-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yong.wang", "王勇", "yong.wang@17zuoye.com", 0f, 0f, 1, "4467", "023777", 1605l, DateUtils.stringToDate("2018-09-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("siyou.liu", "刘思佑", "siyou.liu@17zuoye.com", 0f, 0f, 1, "4468", "682622", 1603l, DateUtils.stringToDate("2018-09-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hao.miao", "苗昊", "hao.miao@17zuoye.com", 0f, 0f, 1, "4469", "113484", 1601l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yang.hai", "海阳", "yang.hai@17zuoye.com", 0f, 0f, 1, "4470", "391771", 1601l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("pengyue.yang", "杨鹏跃", "pengyue.yang@17zuoye.com", 0f, 0f, 1, "4472", "418428", 1602l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huijia.ye", "叶慧嘉", "huijia.ye@17zuoye.com", 0f, 0f, 1, "4473", "871910", 1605l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qing.liu.a", "刘青A", "qing.liu.a@17zuoye.com", 0f, 0f, 1, "4474", "239618", 1604l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yaqiang.li", "李亚强", "yaqiang.li@17zuoye.com", 0f, 0f, 1, "4475", "188827", 1600l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuqing.mu", "穆树青", "shuqing.mu@17zuoye.com", 0f, 0f, 1, "4476", "639327", 1603l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lan.jia", "贾岚", "lan.jia@17zuoye.com", 0f, 0f, 1, "4477", "124397", 1601l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shilei.liu", "刘石磊", "shilei.liu@17zuoye.com", 0f, 0f, 1, "4479", "211303", 1605l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dongyang.wang", "王东阳", "dongyang.wang@17zuoye.com", 0f, 0f, 1, "4480", "037994", 1605l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhongyuan.qu", "曲忠元", "zhongyuan.qu@17zuoye.com", 0f, 0f, 1, "4481", "573873", 1602l, DateUtils.stringToDate("2018-09-06", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ke.li.a", "李可A", "ke.li.a@17zuoye.com", 0f, 0f, 1, "4482", "442598", 1604l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("su.yang", "杨素", "su.yang@17zuoye.com", 0f, 0f, 1, "4483", "887494", 1601l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huanhuan.zhu", "祝环环", "huanhuan.zhu@17zuoye.com", 0f, 0f, 1, "4484", "385167", 1599l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wenqiang.shi", "施文强", "wenqiang.shi@17zuoye.com", 0f, 0f, 1, "4485", "489689", 1602l, DateUtils.stringToDate("2018-09-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("weimin.shang", "尚卫民", "weimin.shang@17zuoye.com", 0f, 0f, 1, "4486", "303752", 1599l, DateUtils.stringToDate("2018-09-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoru.wu", "吴晓如", "xiaoru.wu@17zuoye.com", 0f, 0f, 1, "4487", "857672", 1601l, DateUtils.stringToDate("2018-09-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chang.liu.a", "刘畅A", "chang.liu.a@17zuoye.com", 0f, 0f, 1, "4489", "193072", 1605l, DateUtils.stringToDate("2018-09-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guan.wang", "王官", "guan.wang@17zuoye.com", 0f, 0f, 1, "4491", "202910", 1605l, DateUtils.stringToDate("2018-09-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yunye.zhang", "张云野", "yunye.zhang@17zuoye.com", 0f, 0f, 1, "4492", "825796", 1599l, DateUtils.stringToDate("2018-09-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoqiang.shen", "申小强", "xiaoqiang.shen@17zuoye.com", 0f, 0f, 1, "4494", "871631", 1599l, DateUtils.stringToDate("2018-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shihui.hou", "侯诗慧", "shihui.hou@17zuoye.com", 0f, 0f, 1, "4496", "137498", 1599l, DateUtils.stringToDate("2018-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.li.c", "李延C", "yan.li.c@17zuoye.com", 0f, 0f, 1, "4498", "531469", 1602l, DateUtils.stringToDate("2018-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fangfang.wang", "王方芳", "fangfang.wang@17zuoye.com", 0f, 0f, 1, "4499", "883406", 1604l, DateUtils.stringToDate("2018-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianqiang.zhao", "赵建强", "jianqiang.zhao@17zuoye.com", 0f, 0f, 1, "4500", "530904", 1599l, DateUtils.stringToDate("2018-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.yuan", "袁磊", "lei.yuan@17zuoye.com", 0f, 0f, 1, "4501", "077377", 1603l, DateUtils.stringToDate("2018-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("man.hou", "侯曼", "man.hou@17zuoye.com", 0f, 0f, 1, "4502", "989127", 1601l, DateUtils.stringToDate("2018-09-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.tang", "唐英", "ying.tang@17zuoye.com", 0f, 0f, 1, "4503", "683473", 1601l, DateUtils.stringToDate("2018-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("menghua.wang", "王梦华", "menghua.wang@17zuoye.com", 0f, 0f, 1, "4504", "518524", 1599l, DateUtils.stringToDate("2018-09-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chengqi.yu", "余承琪", "chengqi.yu@17zuoye.com", 0f, 0f, 1, "4505", "196940", 1605l, DateUtils.stringToDate("2018-09-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fengtao.wu", "吴峰涛", "fengtao.wu@17zuoye.com", 0f, 0f, 1, "4507", "520755", 1603l, DateUtils.stringToDate("2018-09-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jinghong.liao", "廖京红", "jinghong.liao@17zuoye.com", 0f, 0f, 1, "4508", "310457", 1601l, DateUtils.stringToDate("2018-09-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("min.guo", "郭敏", "min.guo@17zuoye.con", 0f, 0f, 1, "4517", "584727", 1603l, DateUtils.stringToDate("2018-09-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bing.yan", "闫冰", "bing.yan@17zuoye.com", 0f, 0f, 1, "4519", "869054", 1603l, DateUtils.stringToDate("2018-09-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ling.cui", "崔玲", "ling.cui@17zuoye.com", 0f, 0f, 1, "4520", "835915", 1603l, DateUtils.stringToDate("2018-09-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("na.li.e", "李娜E", "na.li.e@17zuoye.com", 0f, 0f, 1, "4521", "596017", 1603l, DateUtils.stringToDate("2018-09-18", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiaqi.feng", "冯佳琦", "jiaqi.feng@17zuoye.com", 0f, 0f, 1, "4522", "854376", 1603l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kai.sun", "孙凯", "kai.sun@17zuoye.com", 0f, 0f, 1, "4524", "564225", 1602l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jian.wang.a", "王健A", "jian.wang.a@17zuoye.com", 0f, 0f, 1, "4525", "045520", 1602l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yulong.ma", "马玉龙", "yulong.ma@17zuoye.com", 0f, 0f, 1, "4526", "417645", 1602l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tongshuai.ren", "任统帅", "tongshuai.ren@17zuoye.com", 0f, 0f, 1, "4527", "514897", 1599l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("nanxin.lin", "林楠昕", "nanxin.lin@17zuoye.com", 0f, 0f, 1, "4528", "983488", 1603l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hongjian.wang", "王洪舰", "hongjian.wang@17zuoye.com", 0f, 0f, 1, "4529", "594472", 1599l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ahai.wang", "王阿海", "ahai.wang@17zuoye.com", 0f, 0f, 1, "4530", "300194", 1602l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jie.zhang", "张洁", "jie.zhang@17zuoye.com", 0f, 0f, 1, "4531", "926005", 1601l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liangbo.zhao", "赵良博", "liangbo.zhao@17zuoye.com", 0f, 0f, 1, "4532", "480888", 1599l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chunbaixue.yang", "杨春白雪", "chunbaixue.yang@17zuoye.com", 0f, 0f, 1, "4533", "353188", 1600l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hui.li.a", "黎晖A", "hui.li.a@17zuoye.com", 0f, 0f, 1, "4534", "066697", 1605l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("peng.yin", "尹鹏", "peng.yin@17zuoye.com", 0f, 0f, 1, "4535", "193443", 1603l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("manlong.li", "李满龙", "manlong.li@17zuoye.com", 0f, 0f, 1, "4536", "733060", 1603l, DateUtils.stringToDate("2018-09-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hongying.mu", "母红英", "hongying.mu@17zuoye.com", 0f, 0f, 1, "4537", "040500", 1603l, DateUtils.stringToDate("2018-09-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ke.xu", "许可", "ke.xu@17zuoye.com", 0f, 0f, 1, "4538", "797353", 1601l, DateUtils.stringToDate("2018-09-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ruhan.zhang", "张汝晗", "ruhan.zhang@17zuoye.com", 0f, 0f, 1, "4539", "026870", 1602l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("youzi.ge", "葛幼姿", "youzi.ge@17zuoye.com", 0f, 0f, 1, "4541", "658902", 1603l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kaijun.wang", "王凯军", "kaijun.wang@17zuoye.com", 0f, 0f, 1, "4542", "385908", 1601l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuantao.wu", "吴愿涛", "yuantao.wu@17zuoye.com", 0f, 0f, 1, "4543", "499261", 1599l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("le.bo", "薄乐", "le.bo@17zuoye.com", 0f, 0f, 1, "4544", "745212", 1601l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hui.liu.a", "刘辉A", "hui.liu.a@17zuoye.com", 0f, 0f, 1, "4545", "862374", 1603l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.fang", "方伟", "wei.fang@17zuoye.com", 0f, 0f, 1, "4546", "508829", 1605l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wanling.lu", "陆万玲", "wanling.lu@17zuoye.com", 0f, 0f, 1, "4547", "808075", 1604l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dengshuai.yang", "杨登帅", "dengshuai.yang@17zuoye.com", 0f, 0f, 1, "4548", "272054", 1603l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoying.han", "韩小影", "xiaoying.han@17zuoye.com", 0f, 0f, 1, "4549", "452205", 1602l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("simin.zhang", "张思敏", "simin.zhang@17zuoye.com", 0f, 0f, 1, "4550", "626033", 1601l, DateUtils.stringToDate("2018-10-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuai.wang", "王帅", "shuai.wang@17zuoye.com", 0f, 0f, 1, "4551", "670518", 1601l, DateUtils.stringToDate("2018-10-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuewu.zhu", "朱学武", "xuewu.zhu@17zuoye.com", 0f, 0f, 1, "4552", "204478", 1601l, DateUtils.stringToDate("2018-10-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yi.zheng.a", "郑艺A", "yi.zheng.a@17zuoye.com", 0f, 0f, 1, "4553", "187872", 1603l, DateUtils.stringToDate("2018-10-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanjiao.wu", "吴雁娇", "yanjiao.wu@17zuoye.com", 0f, 0f, 1, "4554", "395990", 1601l, DateUtils.stringToDate("2018-10-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jianhai.wang", "王建海", "jianhai.wang@17zuoye.com", 0f, 0f, 1, "4555", "759940", 1605l, DateUtils.stringToDate("2018-10-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.dai", "代妍", "yan.dai@17zuoye.com", 0f, 0f, 1, "4556", "916015", 1605l, DateUtils.stringToDate("2018-10-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guoqiang.zhang", "张国强", "guoqiang.zhang@17zuoye.com", 0f, 0f, 1, "4557", "903723", 1605l, DateUtils.stringToDate("2018-10-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jingzheng.zhang", "张竟争", "jingzheng.zhang@17zuoye.com", 0f, 0f, 1, "4558", "064458", 1603l, DateUtils.stringToDate("2018-10-15", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuwei.wang", "王雨薇", "yuwei.wang@17zuoye.com", 0f, 0f, 1, "4559", "970191", 1601l, DateUtils.stringToDate("2018-10-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yongqiang.pang", "庞永强", "yongqiang.pang@17zuoye.com", 0f, 0f, 1, "4560", "885265", 1599l, DateUtils.stringToDate("2018-10-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kang.yang", "杨康", "kang.yang@17zuoye.com", 0f, 0f, 1, "4561", "357830", 1599l, DateUtils.stringToDate("2018-10-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qi.wagn.c", "王琦C", "qi.wagn.c@17zuoye.com", 0f, 0f, 1, "4562", "113076", 1602l, DateUtils.stringToDate("2018-10-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoyin.lan", "兰小银", "xiaoyin.lan@17zuoye.com", 0f, 0f, 1, "4563", "679335", 1599l, DateUtils.stringToDate("2018-10-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("teng.li", "李腾", "teng.li@17zuoye.com", 0f, 0f, 1, "4564", "635622", 1601l, DateUtils.stringToDate("2018-10-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haiou.liu", "刘海鸥", "haiou.liu@17zuoye.com", 0f, 0f, 1, "4565", "730562", 1605l, DateUtils.stringToDate("2018-10-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yizhao.luo", "罗亦昭", "yizhao.luo@17zuoye.com", 0f, 0f, 1, "4566", "389023", 1604l, DateUtils.stringToDate("2018-10-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("cou.wang", "王凑", "cou.wang@17zuoye.com", 0f, 0f, 1, "4567", "724762", 1601l, DateUtils.stringToDate("2018-10-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiangbin.cheng", "程向彬", "xiangbin.cheng@17zuoye.com", 0f, 0f, 1, "4569", "806411", 1601l, DateUtils.stringToDate("2018-10-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zheng.lu", "路征", "zheng.lu@17zuoye.com", 0f, 0f, 1, "4570", "477122", 1603l, DateUtils.stringToDate("2018-10-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jichao.tian", "田继超", "jichao.tian@17zuoye.com", 0f, 0f, 1, "4571", "298332", 1599l, DateUtils.stringToDate("2018-10-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xintian.wang", "王欣田", "xintian.wang@17zuoye.com", 0f, 0f, 1, "4572", "052495", 1602l, DateUtils.stringToDate("2018-10-29", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jun.deng", "邓军", "jun.deng@17zuoye.com", 0f, 0f, 1, "4573", "349444", 1602l, DateUtils.stringToDate("2018-10-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yannan.wang", "王彦楠", "yannan.wang@17zuoye.com", 0f, 0f, 1, "4574", "527375", 1601l, DateUtils.stringToDate("2018-10-31", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("li.wen", "温力", "li.wen@17zuoye.com", 0f, 0f, 1, "4576", "983089", 1603l, DateUtils.stringToDate("2018-11-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanfang.li", "李艳芳", "yanfang.li@17zuoye.com", 0f, 0f, 1, "4577", "225406", 1601l, DateUtils.stringToDate("2018-11-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huanghuang.zhang", "张焕焕", "huanghuang.zhang@17zuoye.com", 0f, 0f, 1, "4578", "657618", 1603l, DateUtils.stringToDate("2018-11-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yue.qiu", "全海波", "yue.qiu@17zuoye.com", 0f, 0f, 1, "4579", "446386", 1599l, DateUtils.stringToDate("2018-11-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("feng.guo", "卿小海", "feng.guo@17zuoye.com", 0f, 0f, 1, "4580", "471569", 1601l, DateUtils.stringToDate("2018-11-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yun.huang", "黄云", "yun.huang@17zuoye.com", 0f, 0f, 1, "4581", "208362", 1601l, DateUtils.stringToDate("2018-11-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haitao.guo", "郭海滔", "haitao.guo@17zuoye.com", 0f, 0f, 1, "4582", "350939", 1599l, DateUtils.stringToDate("2018-11-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("min.chai", "柴敏", "min.chai@17zuoye.com", 0f, 0f, 1, "4583", "069663", 1605l, DateUtils.stringToDate("2018-11-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liying.zhang", "张丽影", "liying.zhang@17zuoye.com", 0f, 0f, 1, "4584", "071856", 1605l, DateUtils.stringToDate("2018-11-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ruiqi.liu", "刘瑞琪", "ruiqi.liu@17zuoye.com", 0f, 0f, 1, "4585", "650643", 1605l, DateUtils.stringToDate("2018-11-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("meng.wang.a", "王萌A", "meng.wang.a@17zuoye.com", 0f, 0f, 1, "4586", "666387", 1599l, DateUtils.stringToDate("2018-11-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yue.qiu", "仇玥", "yue.qiu@17zuoye.com", 0f, 0f, 1, "4587", "647054", 1603l, DateUtils.stringToDate("2018-11-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("feng.guo", "赵玥", "feng.guo@17zuoye.com", 0f, 0f, 1, "4588", "273640", 1603l, DateUtils.stringToDate("2018-11-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuting.men", "门玉婷", "yuting.men@17zuoye.com", 0f, 0f, 1, "4589", "932593", 1604l, DateUtils.stringToDate("2018-11-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("chen.fu", "傅晨", "chen.fu@17zuoye.com", 0f, 0f, 1, "4590", "220768", 1599l, DateUtils.stringToDate("2018-11-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuming.sun", "孙旭明", "xuming.sun@17zuoye.com", 0f, 0f, 1, "4591", "354357", 1599l, DateUtils.stringToDate("2018-11-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lei.hou", "侯磊", "lei.hou@17zuoye.com", 0f, 0f, 1, "4592", "524131", 1603l, DateUtils.stringToDate("2018-11-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jinlong.liang", "李丹B", "jinlong.liang@17zuoye.com", 0f, 0f, 1, "4593", "790115", 1602l, DateUtils.stringToDate("2018-11-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jinlong.liang", "梁金龙", "jinlong.liang@17zuoye.com", 0f, 0f, 1, "4594", "596649", 1603l, DateUtils.stringToDate("2018-11-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shulia.mao", "毛书来", "shulia.mao@17zuoye.com", 0f, 0f, 1, "4595", "080260", 1600l, DateUtils.stringToDate("2018-11-21", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bo.li.a", "李波A", "bo.li.a@17zuoye.com", 0f, 0f, 1, "4596", "201879", 1599l, DateUtils.stringToDate("2018-11-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("caoli.niu", "牛草丽", "caoli.niu@17zuoye.com", 0f, 0f, 1, "4597", "422778", 1603l, DateUtils.stringToDate("2018-11-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haizhou.wang", "王海舟", "haizhou.wang@17zuoye.com", 0f, 0f, 1, "4598", "122563", 1601l, DateUtils.stringToDate("2018-11-22", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("li.feng", "丰丽", "li.feng@17zuoye.com", 0f, 0f, 1, "4599", "771408", 1603l, DateUtils.stringToDate("2018-11-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiumei.xie", "谢秀梅", "xiumei.xie@17zuoye.com", 0f, 0f, 1, "4600", "680441", 1601l, DateUtils.stringToDate("2018-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.zhao.b", "赵静B", "jing.zhao.b@17zuoye.com", 0f, 0f, 1, "4601", "137319", 1602l, DateUtils.stringToDate("2018-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("anqi.chen", "陈安琪", "anqi.chen@17zuoye.com", 0f, 0f, 1, "4602", "812803", 1601l, DateUtils.stringToDate("2018-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("feng.guo", "郭锋", "feng.guo@17zuoye.com", 0f, 0f, 1, "4603", "883347", 1603l, DateUtils.stringToDate("2018-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("aihua.chen", "陈爱华", "aihua.chen@17zuoye.com", 0f, 0f, 1, "4605", "314311", 1604l, DateUtils.stringToDate("2018-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yunzhu.li", "李云竹", "yunzhu.li@17zuoye.com", 0f, 0f, 1, "4606", "072415", 1601l, DateUtils.stringToDate("2018-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shaozi.wang", "王邵姿", "shaozi.wang@17zuoye.com", 0f, 0f, 1, "4607", "835470", 1603l, DateUtils.stringToDate("2018-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huikun.dang", "党会坤", "huikun.dang@17zuoye.com", 0f, 0f, 1, "4608", "745490", 1601l, DateUtils.stringToDate("2018-12-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhiwei.xu", "徐志伟", "zhiwei.xu@17zuoye.com", 0f, 0f, 1, "4609", "109128", 1601l, DateUtils.stringToDate("2018-12-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("teng.xue", "薛腾", "teng.xue@17zuoye.com", 0f, 0f, 1, "4610", "022676", 1603l, DateUtils.stringToDate("2018-12-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhengke.chang", "常正克", "zhengke.chang@17zuoye.com", 0f, 0f, 1, "4611", "789053", 1602l, DateUtils.stringToDate("2018-12-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ke.li.b", "李柯B", "ke.li.b@17zuoye.com", 0f, 0f, 1, "4612", "226195", 1601l, DateUtils.stringToDate("2018-12-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yi.liu", "刘毅", "yi.liu@17zuoye.com", 0f, 0f, 1, "4613", "843368", 1603l, DateUtils.stringToDate("2018-12-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("aliyan", "阿丽艳·阿布拉哈提", "aliyan@17zuoye.com", 0f, 0f, 1, "4614", "600731", 1601l, DateUtils.stringToDate("2018-11-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xin.pan", "潘曦", "xin.pan@17zuoye.com", 0f, 0f, 1, "4616", "130352", 1601l, DateUtils.stringToDate("2018-12-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yong.yang", "杨永", "yong.yang@17zuoye.com", 0f, 0f, 1, "4617", "162090", 1603l, DateUtils.stringToDate("2018-12-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingjin.li", "李明进", "mingjin.li@17zuoye.com", 0f, 0f, 1, "4618", "575789", 1605l, DateUtils.stringToDate("2018-12-05", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yilin.yao", "姚依琳", "yilin.yao@17zuoye.com", 0f, 0f, 1, "4619", "423270", 1603l, DateUtils.stringToDate("2018-12-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("pengfei.fan", "樊鹏飞", "pengfei.fan@17zuoye.com", 0f, 0f, 1, "4620", "465310", 1601l, DateUtils.stringToDate("2018-12-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fangfang.wang.a", "王芳芳A", "fangfang.wang.a@17zuoye.com", 0f, 0f, 1, "4621", "705145", 1601l, DateUtils.stringToDate("2018-12-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuechao.mu", "穆学超", "xuechao.mu@17zuoye.com", 0f, 0f, 1, "4622", "040937", 1601l, DateUtils.stringToDate("2018-12-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.zheng", "郑莹", "ying.zheng@17zuoye.com", 0f, 0f, 1, "4623", "238021", 1601l, DateUtils.stringToDate("2018-12-10", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ye.lian", "连晔", "ye.lian@17zuoye.com", 0f, 0f, 1, "4624", "754843", 1603l, DateUtils.stringToDate("2018-12-03", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yanchao.liu", "刘彦超", "yanchao.liu@17zuoye.com", 0f, 0f, 1, "4625", "769083", 1601l, DateUtils.stringToDate("2018-12-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bing.he", "何冰", "bing.he@17zuoye.com", 0f, 0f, 1, "4626", "156882", 1605l, DateUtils.stringToDate("2018-12-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tongtong.chang", "常彤彤", "tongtong.chang@17zuoye.com", 0f, 0f, 1, "4627", "372349", 1601l, DateUtils.stringToDate("2018-12-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("rongtian.liu", "刘容天", "rongtian.liu@17zuoye.com", 0f, 0f, 1, "4628", "916209", 1603l, DateUtils.stringToDate("2018-12-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ming.wang.a", "王明A", "ming.wang.a@17zuoye.com", 0f, 0f, 1, "4629", "737654", 1601l, DateUtils.stringToDate("2018-12-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lan.ma", "马兰", "lan.ma@17zuoye.com", 0f, 0f, 1, "4630", "209554", 1603l, DateUtils.stringToDate("2018-12-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("boliang.li", "李伯亮", "boliang.li@17zuoye.com", 0f, 0f, 1, "4631", "950614", 1602l, DateUtils.stringToDate("2018-12-12", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhimeng.ji", "纪执萌", "zhimeng.ji@17zuoye.com", 0f, 0f, 1, "4632", "221578", 1599l, DateUtils.stringToDate("2018-12-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xiaoqian.liu", "刘晓倩", "xiaoqian.liu@17zuoye.com", 0f, 0f, 1, "4633", "334427", 1601l, DateUtils.stringToDate("2018-12-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuxi.wang", "王煜熙", "yuxi.wang@17zuoye.com", 0f, 0f, 1, "4634", "777460", 1601l, DateUtils.stringToDate("2018-12-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ying.zhou", "周颖", "ying.zhou@17zuoye.com", 0f, 0f, 1, "4635", "356339", 1602l, DateUtils.stringToDate("2018-12-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("hui.zhao", "赵晖", "hui.zhao@17zuoye.com", 0f, 0f, 1, "4636", "192174", 1599l, DateUtils.stringToDate("2018-12-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("libing.chen", "陈利兵", "libing.chen@17zuoye.com", 0f, 0f, 1, "4637", "239644", 1599l, DateUtils.stringToDate("2018-12-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("nan.li", "李南", "nan.li@17zuoye.com", 0f, 0f, 1, "4638", "593526", 1602l, DateUtils.stringToDate("2018-12-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("panfeng.gao", "高潘峰", "panfeng.gao@17zuoye.com", 0f, 0f, 1, "4639", "966878", 1599l, DateUtils.stringToDate("2018-12-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shenghua.tu", "涂生华", "shenghua.tu@17zuoye.com", 0f, 0f, 1, "4640", "855541", 1605l, DateUtils.stringToDate("2018-12-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guilin.wang", "王桂林", "guilin.wang@17zuoye.com", 0f, 0f, 1, "4641", "305005", 1601l, DateUtils.stringToDate("2018-12-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yuan.liu.a", "刘元A", "yuan.liu.a@17zuoye.com", 0f, 0f, 1, "4642", "665615", 1603l, DateUtils.stringToDate("2018-12-19", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("qin.chen", "陈琴", "qin.chen@17zuoye.com", 0f, 0f, 1, "4643", "806666", 1604l, DateUtils.stringToDate("2018-12-17", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("pengfei.li", "李鹏飞", "pengfei.li@17zuoye.com", 0f, 0f, 1, "4644", "908886", 1601l, DateUtils.stringToDate("2018-12-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shan.wang.a", "江珊A", "shan.wang.a@17zuoye.com", 0f, 0f, 1, "4645", "683678", 1601l, DateUtils.stringToDate("2018-12-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huaman.fu", "付华曼", "huaman.fu@17zuoye.com", 0f, 0f, 1, "4646", "215261", 1603l, DateUtils.stringToDate("2018-12-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yu.gao.a", "高雨A", "yu.gao.a@17zuoye.com", 0f, 0f, 1, "4647", "422973", 1601l, DateUtils.stringToDate("2018-12-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fei.gao.a", "高菲A", "fei.gao.a@17zuoye.com", 0f, 0f, 1, "4648", "932439", 1601l, DateUtils.stringToDate("2018-12-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuejiao.gong", "宫雪娇", "xuejiao.gong@17zuoye.com", 0f, 0f, 1, "4649", "524940", 1601l, DateUtils.stringToDate("2018-12-26", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.liu.a", "刘伟A", "wei.liu.a@17zuoye.com", 0f, 0f, 1, "4650", "500989", 1603l, DateUtils.stringToDate("2018-12-25", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tian.xia", "夏天", "tian.xia@17zuoye.com", 0f, 0f, 1, "4651", "585927", 1601l, DateUtils.stringToDate("2019-01-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xue.wang.d", "王雪D", "xue.wang.d@17zuoye.com", 0f, 0f, 1, "4652", "905100", 1601l, DateUtils.stringToDate("2019-01-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shaodong.yan", "严少东", "shaodong.yan@17zuoye.com", 0f, 0f, 1, "4653", "496106", 1599l, DateUtils.stringToDate("2019-01-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhicong.lin", "林志聪", "zhicong.lin@17zuoye.com", 0f, 0f, 1, "4654", "529961", 1601l, DateUtils.stringToDate("2019-01-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xue.chen", "陈雪", "xue.chen@17zuoye.com", 0f, 0f, 1, "4655", "474188", 1603l, DateUtils.stringToDate("2019-01-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kezhi.jiao", "焦可智", "kezhi.jiao@17zuoye.com", 0f, 0f, 1, "4656", "784338", 1603l, DateUtils.stringToDate("2018-12-24", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tiantian.wang.a", "王甜甜A", "tiantian.wang.a@17zuoye.com", 0f, 0f, 1, "4657", "909532", 1603l, DateUtils.stringToDate("2019-01-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.feng.a", "冯艳A", "yan.feng.a@17zuoye.com", 0f, 0f, 1, "4658", "728947", 1603l, DateUtils.stringToDate("2019-01-02", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("tian.qiu", "邱天", "tian.qiu@17zuoye.com", 0f, 0f, 1, "4660", "678889", 1601l, DateUtils.stringToDate("2019-01-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuai.zhang.b", "张帅B", "shuai.zhang.b@17zuoye.com", 0f, 0f, 1, "4661", "269998", 1603l, DateUtils.stringToDate("2019-01-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhanxue.zhi", "职占学", "zhanxue.zhi@17zuoye.com", 0f, 0f, 1, "4662", "137682", 1603l, DateUtils.stringToDate("2019-01-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fan.lin", "林樊", "fan.lin@17zuoye.com", 0f, 0f, 1, "4663", "782080", 1603l, DateUtils.stringToDate("2019-01-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("kan.wang", "王衎", "kan.wang@17zuoye.com", 0f, 0f, 1, "4664", "201044", 1603l, DateUtils.stringToDate("2019-01-07", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("huihui.hu", "胡会会", "huihui.hu@17zuoye.com", 0f, 0f, 1, "4666", "972918", 1600l, DateUtils.stringToDate("2019-01-08", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuheng.liu", "刘序珩", "xuheng.liu@17zuoye.com", 0f, 0f, 1, "4667", "507918", 1601l, DateUtils.stringToDate("2019-01-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jing.tian", "田静", "jing.tian@17zuoye.com", 0f, 0f, 1, "4668", "985948", 1601l, DateUtils.stringToDate("2019-01-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guilin.zhao", "赵桂琳", "guilin.zhao@17zuoye.com", 0f, 0f, 1, "4669", "866192", 1605l, DateUtils.stringToDate("2019-01-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lan.ouyang", "欧阳岚", "lan.ouyang@17zuoye.com", 0f, 0f, 1, "4670", "665870", 1601l, DateUtils.stringToDate("2019-01-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yinngkai.li", "李英凯", "yinngkai.li@17zuoye.com", 0f, 0f, 1, "4671", "925105", 1601l, DateUtils.stringToDate("2019-01-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("mingying.wan", "万明英", "mingying.wan@17zuoye.com", 0f, 0f, 1, "4672", "800576", 1601l, DateUtils.stringToDate("2019-01-09", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("miao.wang", "王淼", "miao.wang@17zuoye.com", 0f, 0f, 1, "4673", "676714", 1603l, DateUtils.stringToDate("2019-01-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("ning.yan", "闫宁", "ning.yan@17zuoye.com", 0f, 0f, 1, "4674", "308301", 1599l, DateUtils.stringToDate("2019-01-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jincheng.wang", "王进成", "jincheng.wang@17zuoye.com", 0f, 0f, 1, "4675", "349869", 1599l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("long.wang", "王龙A", "long.wang@17zuoye.com", 0f, 0f, 1, "4676", "090186", 1602l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiaqi.yan", "闫佳奇", "jiaqi.yan@17zuoye.com", 0f, 0f, 1, "4677", "223179", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("haiyan.jiang", "姜海燕", "haiyan.jiang@17zuoye.com", 0f, 0f, 1, "4678", "744992", 1599l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fan.yang", "杨帆A", "fan.yang@17zuoye.com", 0f, 0f, 1, "4679", "502957", 1601l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("jiaojiao.qu", "曲娇娇", "jiaojiao.qu@17zuoye.com", 0f, 0f, 1, "4680", "879150", 1601l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhiwei.zhang", "张志伟", "zhiwei.zhang@17zuoye.com", 0f, 0f, 1, "4681", "568468", 1601l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("shuo.chen", "陈硕", "shuo.chen@17zuoye.com", 0f, 0f, 1, "4682", "940558", 1601l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhimeng.zhao", "赵志猛", "zhimeng.zhao@17zuoye.com", 0f, 0f, 1, "4683", "351912", 1599l, DateUtils.stringToDate("2019-01-16", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yibo.zhang", "张一博", "yibo.zhang@17zuoye.com", 0f, 0f, 1, "4685", "106002", 1599l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yue.shi.a", "石悦A", "yue.shi.a@17zuoye.com", 0f, 0f, 1, "4687", "806046", 1602l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yubo.li", "李宇博", "yubo.li@17zuoye.com", 0f, 0f, 1, "4690", "723673", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("lianzhao.li", "李连昭", "lianzhao.li@17zuoye.com", 0f, 0f, 1, "4691", "100966", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xu.lou", "罗旭", "xu.lou@17zuoye.com", 0f, 0f, 1, "4692", "298328", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.wang.c", "王燕C", "yan.wang.c@17zuoye.com", 0f, 0f, 1, "4693", "253365", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yutong.liu", "刘育彤", "yutong.liu@17zuoye.com", 0f, 0f, 1, "4694", "704677", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("liangwei.yan", "严良维", "liangwei.yan@17zuoye.com", 0f, 0f, 1, "4695", "809331", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("bingying.wang", "王斌英", "bingying.wang@17zuoye.com", 0f, 0f, 1, "4696", "002248", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("panjin.chen", "陈攀鑫", "panjin.chen@17zuoye.com", 0f, 0f, 1, "4697", "763734", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("luyang.zhang", "张路洋", "luyang.zhang@17zuoye.com", 0f, 0f, 1, "4698", "831760", 1601l, DateUtils.stringToDate("2019-01-28", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yao.zhang", "张瑶", "yao.zhang@17zuoye.com", 0f, 0f, 1, "4699", "862815", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("xuefei.zhang", "张雪菲", "xuefei.zhang@17zuoye.com", 0f, 0f, 1, "4700", "319108", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("zhengzhong.wang", "王正忠", "zhengzhong.wang@17zuoye.com", 0f, 0f, 1, "4701", "131313", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yan.li", "李岩", "yan.li@17zuoye.com", 0f, 0f, 1, "4702", "220756", 1603l, DateUtils.stringToDate("2019-01-14", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("fei.wu", "吴菲", "fei.wu@17zuoye.com", 0f, 0f, 1, "4703", "399672", 1601l, DateUtils.stringToDate("2019-01-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("john.m", "John Moore M", "john.m@17zuoye.com", 0f, 0f, 1, "4704", "513389", 1599l, DateUtils.stringToDate("2019-01-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("wei.ma", "马玮", "wei.ma@17zuoye.com", 0f, 0f, 1, "4705", "850871", 1599l, DateUtils.stringToDate("2019-01-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("guohui.sun", "孙国辉", "guohui.sun@17zuoye.com", 0f, 0f, 1, "4706", "655902", 1601l, DateUtils.stringToDate("2019-01-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("dan.liu", "刘丹C", "dan.liu@17zyoye.com", 0f, 0f, 1, "4707", "580664", 1603l, DateUtils.stringToDate("2019-01-23", "yyyy-MM-dd")));
        list.add(new CreateUserAccount("yaxin.li", "李亚鑫", "yaxin.li@17zyoye.com", 0f, 0f, 1, "4708", "336794", 1601l, DateUtils.stringToDate("2019-01-29", "yyyy-MM-dd")));
        MapMessage mapMessage = createUserOrSendEmail(list, type);
        deleteCountryDayOrderCount(2);
        return mapMessage;
    }

    public MapMessage createUserOrSendEmail(List<CreateUserAccount> list, Integer type) {
        List<String> failList = new ArrayList<>();
        list.forEach(p -> {
            try {
                if (Objects.equals(type, 1)) {


                    MapMessage userMessage = orgConfigService.addAgentUser(p.getRealName(), p.getAccountName(), p.getPassword(), null,
                            0, null, null, null,
                            p.getContractStartDate(), null, null, null,
                            null, null, 1f, p.getAccountNumber());
                    if (!userMessage.isSuccess()) {
                        String str = "工号：" + p.getAccountNumber() + "账号：" + p.getAccountName() + " 创建失败了 " + " 原因：" + userMessage.getInfo() + "/br";
                        failList.add(str);
                        return;
                    }
                    Long userId = (Long) userMessage.get("userId");
                    MapMessage groupUserMessage = orgConfigService.addGroupUser(p.getGroupId(), userId, AgentRoleType.CompanyEmployee);
                } else {
                    emailServiceClient.createPlainEmail().body("账号：" + p.getAccountName() + " 密码:" + p.getPassword()).subject("天玑账号密码").to(p.getEmail()).send();
                }
            } catch (Exception ex) {
                emailServiceClient.createPlainEmail().body("工号：" + p.getAccountNumber() + "账号：" + p.getAccountName() + "创建失败了").subject("天玑创建账号失败").to("xianlong.zhang@17zuoye.com").send();
                logger.error(String.format("批量创建账号异常  accountNumber=", p.getAccountNumber()), ex);
            }

        });
        return MapMessage.successMessage().add("failList", failList);
    }

    public Integer getCountryDayOrderCount(Integer method) {
        return agentCacheSystem.CBS.unflushable.load("agent_batch_create_account" + method);
    }

    public void updateCountryDayOrderCount(Integer method) {
        agentCacheSystem.CBS.unflushable.set("agent_batch_create_account" + method, SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), 1);
    }

    public void deleteCountryDayOrderCount(Integer method) {
        agentCacheSystem.CBS.unflushable.delete("agent_batch_create_account" + method);
    }
}
