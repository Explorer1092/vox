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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Longlong Yu
 * @since 下午8:39,13-6-25.
 */
@Controller
@RequestMapping("/crm/pk")
public class CrmPkController extends CrmAbstractController {

//    @Inject
//    private PkLoaderClient pkLoaderClient;
//    @Inject
//    private PkServiceClient pkServiceClient;
//    @Inject
//    private PkLogManagerClient pkLogManagerClient;
//
//    /**
//     * ***********************查询相关*****************************************************************
//     */
//    @RequestMapping(value = "pkdetail.vpage", method = RequestMethod.GET)
//    public String getVitalityDetail(@RequestParam("userId") Long userId, Model model) {
//        Date end = new Date();
//        Date start = DateUtils.nextDay(new Date(), -6);
//        User user = new User();
//        user.setId(userId);
//        user.setUserType(UserType.STUDENT.getType());
//
//        List<VitalityMapper> mappers = new ArrayList<>();
//        MapMessage response = pkLoaderClient.getVitalityLog(user.getId(), start.getTime(), end.getTime());
//        if (response.isSuccess()) {
//            for (VitalityLog log : (List<VitalityLog>) response.get("vitalityLogs")) {
//                VitalityMapper mapper = new VitalityMapper();
//                mapper.setId(log.getId());
//                mapper.setStudentId(log.getUserId());
//                mapper.setType(log.getType().name());
//                mapper.setVitalityQty(SafeConverter.toInt(log.getQuantity()));
//                mapper.setCreateTime(log.getCreateTime());
//                mappers.add(mapper);
//            }
//        }
//
//        //查询上周周冠军是不是这个学生
//        long clazzId = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(userId).getId();
//        boolean championflag = false;
//        WeekRange previous = WeekRange.current().previous();
//        MapMessage mapMessage = pkLoaderClient.getClazzWeekRank(clazzId, previous.getStartTime());
//        if (mapMessage.isSuccess()) {
//            for (WeekRank each : (List<WeekRank>) mapMessage.get("weekRank")) {
//                long weekBattleWinCount = each.getWin();
//                Integer rank = each.getRank();
//                if (rank == 1 && weekBattleWinCount > 0) {
//                    if (Objects.equals(each.getUserId(), userId)) {
//                        championflag = true;
//                        break;
//                    }
//                }
//            }
//        }
//        model.addAttribute("vitalityLogList", mappers);
//        model.addAttribute("userId", userId);
//        model.addAttribute("championflag", championflag);
//        User student = userLoaderClient.loadUser(userId, UserType.STUDENT);
//        if (null != student.getProfile() && StringUtils.isNotEmpty(student.getProfile().getRealname())) {
//            model.addAttribute("userName", student.getProfile().getRealname());
//        }
//        return "crm/pk/pkdetail";
//    }
//
//    /**
//     * ***********************查询PK记录*****************************************************************
//     */
//    @RequestMapping(value = "battlereportlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
//    public String battleReportList(Model model) {
//
//        long studentId = getRequestLong("studentId", -1L);
//        String pkDateStr = getRequestParameter("pkDate", "");
//        Date pkDate = null;
//        Map conditionMap = new HashMap();
//
//        try {
//            FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd");
//            pkDate = sdf.parse(pkDateStr);
//        } catch (Exception ignored) {
//            pkDate = new Date();
//        }
//        conditionMap.put("pkDate", pkDate);
//
//        if (studentId < 0) {
//            getAlertMessageManager().addMessageError(getRequestParameter("studentId", "") + "不是有效的学生ID");
//            return redirect("/crm/student/studentlist.vpage");
//        }
//        conditionMap.put("studentId", studentId);
//
//        MapMessage response = pkLoaderClient.queryBattleReports(studentId, pkDate.getTime());
//        if (response.isSuccess()) {
//            model.addAttribute("battleReportList", response.get("battleReports"));
//        } else {
//            getAlertMessageManager().addMessageError("查询${studentId}PK记录失败");
//        }
//
//        model.addAttribute("conditionMap", conditionMap);
//        return "crm/pk/battlereportlist";
//    }
//
//    /**
//     * ***********************查询经验记录*****************************************************************
//     */
//    @RequestMapping(value = "exphistory.vpage", method = {RequestMethod.GET, RequestMethod.POST})
//    public String expHistory(Model model) {
//        long studentId = getRequestLong("userId", -1L);
//        String qDate = getRequestParameter("qDate", "");
//        if (StringUtils.isEmpty(qDate)) {
//            qDate = DateUtils.getTodaySqlDate();
//        }
//        List<PkExpLog> logList = pkLoaderClient.getPkExpLog(studentId, qDate);
//        model.addAttribute("logList", logList);
//        model.addAttribute("userId", studentId);
//        model.addAttribute("username", userLoaderClient.loadUser(studentId).fetchRealname());
//        model.addAttribute("qDate", qDate);
//        return "crm/pk/exphistory";
//    }
//
//    /**
//     * ***********************查询PK战胜全班情况*****************************************************************
//     */
//    @RequestMapping(value = "studentpkinfo.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage studentPkWeek() {
//        long studentId = getRequestLong("studentId");
//        User user = new User();
//        user.setId(studentId);
//        user.setUserType(UserType.STUDENT.getType());
//        MapMessage response = pkLoaderClient.getFoe(user.getId());
//        if (!response.isSuccess()) {
//            return MapMessage.errorMessage("学生" + studentId + "尚未创建角色");
//        }
//        boolean allFoesBeated = (boolean) response.get("allFoesBeated");
//        if (allFoesBeated) {
//            return MapMessage.successMessage("学生" + studentId + "已经获得PK-战胜全班奖励");
//        }
//
//        List<Long> classmateList = new ArrayList<>();
//        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
//        if (clazz != null) {
//            classmateList = asyncGroupServiceClient.findStudentIdsByClazzIdWithCache(clazz.getId())
//                    .stream()
//                    .filter(o -> !o.equals(studentId))
//                    .collect(Collectors.toList());
//        }
//
//        int clazzSize = classmateList.size() + 1;
//        for (Object each : (List) response.get("beatedFoes")) {
//            classmateList.remove(Long.parseLong(String.valueOf(each)));
//        }
//        if (classmateList.isEmpty()) {
//            return MapMessage.successMessage("学生" + studentId + "已经战胜全班学生，该班共有" + clazzSize +
//                    "名学生（包括学生" + studentId + "），如果班级人数少于20, 没有战胜全班奖励");
//        } else {
//            String classmateIds = "";
//            for (long id : classmateList) {
//                classmateIds += id + ",";
//            }
//            classmateIds = classmateIds.substring(0, classmateIds.length() - 1);
//            return MapMessage.errorMessage("学生" + studentId + "尚未战胜的学生有" + classmateList.size() +
//                    "名[" + classmateIds + "]，该班共有" + clazzSize + "名学生（包括学生" + studentId +
//                    "），如果班级人数少于20, 没有战胜全班奖励");
//        }
//    }
//
//    /**
//     * ***********************增加活力值*****************************************************************
//     */
//    @RequestMapping(value = "addvitality.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage addVitality(@RequestParam Long userId,
//                                  @RequestParam Integer type,
//                                  @RequestParam String vitalityQty,
//                                  @RequestParam String vitalityDesc) {
//
//        VitalityType vitalityType;
//        Integer vitality;
//
//        vitalityDesc = vitalityDesc.replaceAll("\\s", "");
//        try {
//            vitalityType = VitalityType.of(type);
//            vitality = Integer.parseInt(vitalityQty.replaceAll("\\s", ""));
//        } catch (Exception ignored) {
//            return MapMessage.errorMessage();
//        }
//        if (StringUtils.isEmpty(vitalityDesc)) {
//            return MapMessage.errorMessage();
//        }
//
//        User target = new User();
//        target.setId(userId);
//        target.setUserType(UserType.STUDENT.getType());
//        if (!pkServiceClient.addVitality(target, vitality, vitalityType)) {
//            return MapMessage.errorMessage();
//        }
//
//        return MapMessage.successMessage();
//    }
//
//    /**
//     * ***********************增加经验值*****************************************************************
//     */
//    @RequestMapping(value = "addexperience.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage addExperience(@RequestParam Long userId, @RequestParam Integer experience) {
//        if (null == experience || experience < 0) {
//            return MapMessage.errorMessage();
//        }
//
//        User target = userLoaderClient.loadUser(userId);
//        if (null == target) {
//            return MapMessage.errorMessage();
//        }
//
//        if (!pkServiceClient.upgrade(target, experience)) {
//            return MapMessage.errorMessage();
//        }
//
//        return MapMessage.successMessage();
//    }
//
//    /**
//     * ***********************修改PK性别*****************************************************************
//     */
//    @RequestMapping(value = "changepkgender.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage changePkGender() {
//        long userId = getRequestLong("userId");
//        String changePkGenderDesc = getRequestParameter("changePkGenderDesc", "");
//        if (StringUtils.isEmpty(changePkGenderDesc))
//            return MapMessage.errorMessage("请先填写备注");
//
//        User user = new User();
//        user.setId(userId);
//        user.setUserType(UserType.STUDENT.getType());
//        MapMessage response = pkLoaderClient.getInformation(user.getId());
//        if (response.isSuccess()) {
//            RoleInfo roleInfo = (RoleInfo) response.get("roleInfo");
//            Gender original = roleInfo.getGender();
//            Gender gender = null;
//            if (original == Gender.FEMALE) {
//                gender = Gender.MALE;
//            } else if (original == Gender.MALE) {
//                gender = Gender.FEMALE;
//            }
//            if (gender == null) {
//                return MapMessage.errorMessage("修改PK性别失败，无法识别角色的原有性别信息");
//            }
//            if (pkServiceClient.changeGender(user.getId(), gender).isSuccess()) {
//                return MapMessage.successMessage("修改PK性别成功");
//            }
//        }
//        return MapMessage.errorMessage("修改PK性别失败");
//    }
//
//    /**
//     * ***********************清空角色背包装备*****************************************************************
//     */
//    @RequestMapping(value = "clearrolebag.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage clearRoleBag() {
//
//        String clearRoleBagDesc = getRequestParameter("clearRoleBagDesc", "");
//        if (clearRoleBagDesc == "")
//            return MapMessage.errorMessage("描述信息不能为空");
//
//        long studentId = getRequestLong("studentId", -1L);
//        if (studentId < 0) {
//            String errId = getRequestParameter("studentId", "");
//            return MapMessage.errorMessage(errId + "不是有效的学生ID");
//        }
//
//        User student = userLoaderClient.loadUser(studentId);
//        if (null == student) {
//            String errId = getRequestParameter("studentId", "");
//            return MapMessage.errorMessage(errId + "不是有效的学生ID");
//        }
//
//        MapMessage response = pkLoaderClient.getBag(student.getId(), student.getUserType());
//        if (!response.isSuccess())
//            return MapMessage.errorMessage("获取用户背包失败，请联系管理员");
//
//        List<Fashion> fashions = (List<Fashion>) response.get("fashions");
//        List<String> fashionIds = new ArrayList<>();
//        for (Fashion fashion : fashions) {
//            fashionIds.add(fashion.getId());
//        }
//
//        MapMessage deleteResponse = pkServiceClient.deleteFashion(student.getId(), fashionIds, Boolean.FALSE);
//        if (deleteResponse.isSuccess()) {
//            return MapMessage.successMessage("清空用户装备成功");
//        } else {
//            return MapMessage.successMessage("清空用户装备失败");
//        }
//    }
//
//    @RequestMapping(value = "pktransfercareerinfo.vpage", method = RequestMethod.GET)
//    public String pkTransferCareerInfo(@RequestParam("userId") Long userId, Model model) {
//
//        Student student = studentLoaderClient.loadStudent(userId);
//        model.addAttribute("logs", pkLogManagerClient.getRemoteReference().findRoleTransferCareerLogs(userId));
//        model.addAttribute("userId", userId);
//        model.addAttribute("userName", student.getProfile().getRealname());
//        return "crm/pk/pktransfercareerinfo";
//    }
//
//    @RequestMapping(value = "pkequipmentlog.vpage", method = RequestMethod.GET)
//    public String pkequipmentlog(@RequestParam("userId") Long userId, Model model) {
//        Student student = studentLoaderClient.loadStudent(userId);
//        model.addAttribute("logs", pkLoaderClient.getEquipmentLogs(userId));
//        model.addAttribute("userId", userId);
//        model.addAttribute("userName", student.getProfile().getRealname());
//        return "crm/pk/pkequipmentlog";
//    }
//
//    @RequestMapping(value = "pkprizeexchangelog.vpage", method = RequestMethod.GET)
//    public String pkPrizeExchangeLog(@RequestParam("userId") Long userId, Model model) {
//        Student student = studentLoaderClient.loadStudent(userId);
//        model.addAttribute("logs", pkLoaderClient.getPrizeExchangeLog(userId, 30));
//        model.addAttribute("userId", userId);
//        model.addAttribute("userName", student.getProfile().getRealname());
//        return "crm/pk/pkprizeexchangelog";
//    }
//
//    @RequestMapping(value = "pkpetlog.vpage", method = RequestMethod.GET)
//    public String pkPetLog(@RequestParam("userId") Long userId, Model model) {
//        Student student = studentLoaderClient.loadStudent(userId);
//        model.addAttribute("logs", pkLoaderClient.getPetLogList(userId, 50));
//        model.addAttribute("userId", userId);
//        model.addAttribute("userName", student.getProfile().getRealname());
//        return "crm/pk/pkpetlog";
//    }
}