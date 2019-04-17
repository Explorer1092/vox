package com.voxlearning.utopia.agent.controller.mobile.competitiveproduct;


import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.PaymentModeType;
import com.voxlearning.utopia.agent.constants.UsageScenarioType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentCompetitiveProduct;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.competitiveproduct.AgentCompetitiveProductService;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学校竞品收集处理类
 *
 * @author deliang.che
 * @date 2018/3/9
 */
@Controller
@RequestMapping("/mobile/competitive_product")
public class AgentCompetitiveProductController extends AbstractAgentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AgentCompetitiveProductService agentCompetitiveProductService;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private BaseOrgService baseOrgService;

    private static final Long OTHER_ID = 99999999L;//定义“未分配”角色ID

//    private static final Integer pageNum = 15;//分页，每页显示记录数

    /**
     * 首页竞品信息未反馈学校数量
     *
     * @return
     */
    @RequestMapping(value = "wait_feedback_num.vpage")
    @ResponseBody
    public MapMessage waitFeedBackNum() {
        MapMessage message = MapMessage.successMessage();
        List<Long> schoolIdList = new ArrayList<>();
        //获取当前登录人信息
        AuthCurrentUser currentUser = getCurrentUser();
        Long userId = currentUser.getUserId();
        // 登录用户为专员，则显示自己管辖的所有学校
        if (currentUser.isBusinessDeveloper()) {
            schoolIdList = baseOrgService.getUserSchools(userId);
            //登录用户为市经理
        } else if (currentUser.isCityManager()) {
            //根据用户ID获取部门ID
            Long groupId = null;
            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(userId);
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                groupId = groupIdList.get(0);
            }
            //获取该市区下的所有学校信息
            schoolIdList = baseOrgService.getManagedSchoolListByGroupId(groupId);
        }
        Integer waitFeedBackNum = 0;//待反馈数量
        Map<Long, SchoolExtInfo> extInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIdList).getUninterruptibly();
        //待反馈
        List<Long> waitFeedBackList = new ArrayList<>();
        schoolIdList.forEach(p -> {
            SchoolExtInfo extInfo = extInfoMap.get(p);
            if (null == extInfo || extInfo.getCompetitiveProductFlag() == null || extInfo.getCompetitiveProductFlag() == 0) {
                waitFeedBackList.add(p);
            }
        });
        waitFeedBackNum = waitFeedBackList.size();
        //存储待反馈数量
        message.add("waitFeedBackNum", waitFeedBackNum);
        return message;
    }

    /**
     * 竞品专员列表
     *
     * @return
     */
    @RequestMapping(value = "user_list.vpage")
    @ResponseBody
    public MapMessage userList() {
        //获取当前登录人信息
        AuthCurrentUser currentUser = getCurrentUser();
        Long userId = currentUser.getUserId();
        MapMessage message = MapMessage.successMessage();
        List<Long> schoolIdList = new ArrayList<>();
        // 登录用户为专员，则显示自己管辖的所有学校
        if (currentUser.isBusinessDeveloper()) {
            schoolIdList = baseOrgService.getUserSchools(userId);
            //登录用户为市经理
        } else if (currentUser.isCityManager()) {
            //根据用户ID获取部门ID
            Long groupId = null;
            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(userId);
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                groupId = groupIdList.get(0);
            }
            List<Map<String, Object>> userList = new ArrayList<>();
            //获取管理的部门中的其他人员（专员列表）
            List<AgentUser> managedUsers = baseOrgService.getManagedGroupUsers(userId, false);
            managedUsers.forEach(p -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userName", p.getRealName());
                userMap.put("userId", p.getId());
                userList.add(userMap);
            });
            // 获取“未分配”的学校
            schoolIdList = baseOrgService.getCityManageOtherSchoolByGroupId(groupId);
            //如果存在“未分配”的学校
            if (CollectionUtils.isNotEmpty(schoolIdList)) {
                // 创建一个其他角色，方便查看未分配专员的学校
                Map<String, Object> otherRole = new HashMap<>();
                otherRole.put("userId", OTHER_ID);
                otherRole.put("userName", "未分配");
                userList.add(otherRole);
            }
            //存储专员列表
            message.put("userList", userList);
            //获取该市区下的所有学校信息

            schoolIdList = baseOrgService.getManagedSchoolListByGroupId(groupId);
        }
        Integer waitFeedBackNum = 0;//待反馈数量
        Integer haveFeedBackNum = 0;//已反馈数量
        Map<Long, SchoolExtInfo> extInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIdList).getUninterruptibly();
        //待反馈
        List<Long> waitFeedBackList = new ArrayList<>();
        //已反馈
        List<Long> haveFeedBackList = new ArrayList<>();
        schoolIdList.forEach(p -> {
            SchoolExtInfo extInfo = extInfoMap.get(p);
            if (extInfo != null && extInfo.getCompetitiveProductFlag() != null && (extInfo.getCompetitiveProductFlag() == 1 || extInfo.getCompetitiveProductFlag() == 2)) {
                haveFeedBackList.add(p);
            } else {
                waitFeedBackList.add(p);
            }
        });
        waitFeedBackNum = waitFeedBackList.size();
        haveFeedBackNum = haveFeedBackList.size();
        //存储待反馈数量
        message.add("waitFeedBackNum", waitFeedBackNum);
        //存储已反馈数量
        message.add("haveFeedBackNum", haveFeedBackNum);
        return message;
    }


    /**
     * 竞品收集学校列表
     *
     * @return
     */
    @RequestMapping(value = "school_list.vpage")
    @ResponseBody
    @OperationCode("15e46a3090264204")
    public MapMessage schoolList() {
        Integer feedbackFlag = getRequestInt("feedbackFlag");//反馈标识
        Long selectUserId = getRequestLong("selectUserId");//选中专员ID
//        Integer pageIndex = getRequestInt("pageIndex");//分页标识
        MapMessage message = MapMessage.successMessage();
        List<Long> schoolIdList = new ArrayList<>();
        if (!selectUserId.equals(OTHER_ID)) {
            schoolIdList = baseOrgService.getUserSchools(selectUserId);
        } else {
            Long groupId = null;
            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(getCurrentUserId());
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                groupId = groupIdList.get(0);
            }
            schoolIdList = baseOrgService.getCityManageOtherSchoolByGroupId(groupId);
        }
        //分页显示（显示第pageIndex+1页，每页显示pageNum条数据）
//        schoolIdList  = schoolIdList.stream().skip(pageIndex*pageNum).limit(pageNum).collect(Collectors.toList());

        Map<Long, SchoolExtInfo> extInfoMap = schoolExtServiceClient.getSchoolExtService().loadSchoolsExtInfoAsMap(schoolIdList).getUninterruptibly();
        List<Long> schoolIdListFinal = new ArrayList<>();
        schoolIdList.forEach(p -> {
            SchoolExtInfo extInfo = extInfoMap.get(p);
            //待反馈
            if (feedbackFlag == 0) {
                if (null != extInfo) {
                    if (extInfo.getCompetitiveProductFlag() == null || extInfo.getCompetitiveProductFlag() == 0) {
                        schoolIdListFinal.add(p);
                    }
                } else {
                    schoolIdListFinal.add(p);
                }
                //已反馈
            } else if (feedbackFlag == 1) {
                if (null != extInfo && null != extInfo.getCompetitiveProductFlag() && (extInfo.getCompetitiveProductFlag() == 1 || extInfo.getCompetitiveProductFlag() == 2)) {
                    schoolIdListFinal.add(p);
                }
            }
        });
        List<Map<String, Object>> schoolList = new ArrayList<>();
        Map<Long, CrmSchoolSummary> schoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(schoolIdListFinal);
        List<Long> tmpList = new ArrayList<>();
        schoolIdListFinal.forEach(p -> {
            CrmSchoolSummary schoolSummary = schoolSummaryMap.get(p);
            if (schoolSummary == null) {
                tmpList.add(p);
            } else {
                // 组装学校数据
                Map<String, Object> schoolMap = new HashMap<>();
                schoolMap.put("schoolId", schoolSummary.getSchoolId());
                schoolMap.put("schoolName", schoolSummary.getSchoolName());
                //获取省市区
                String schoolAddress = schoolSummary.getProvinceName() + "/" + schoolSummary.getCityName() + "/" + schoolSummary.getCountyName();
                schoolMap.put("schoolAddress", schoolAddress);
                SchoolExtInfo schoolExtInfo = extInfoMap.get(p);
                if (null != schoolExtInfo && null != schoolExtInfo.getCompetitiveProductFlag()) {
                    schoolMap.put("competitiveProductFlag", schoolExtInfo.getCompetitiveProductFlag());
                } else {
                    schoolMap.put("competitiveProductFlag", 0);
                }
                schoolList.add(schoolMap);
            }
        });
        Map<Long, School> schoolMaps = schoolLoaderClient.getSchoolLoader().loadSchools(tmpList).getUninterruptibly();
        schoolMaps.values().forEach(p -> {
            // 组装学校数据
            Map<String, Object> schoolMap = new HashMap<>();
            schoolMap.put("schoolId", p.getId());
            schoolMap.put("schoolName", p.getCname());
            //根据区域编码获取省市区
            ExRegion exRegion = raikouSystem.loadRegion(p.getRegionCode());
            String schoolAddress = exRegion.getProvinceName() + "/" + exRegion.getCityName() + "/" + exRegion.getName();
            schoolMap.put("schoolAddress", schoolAddress);
            SchoolExtInfo schoolExtInfo = extInfoMap.get(p.getId());
            if (null != schoolExtInfo && null != schoolExtInfo.getCompetitiveProductFlag()) {
                schoolMap.put("competitiveProductFlag", schoolExtInfo.getCompetitiveProductFlag());
            } else {
                schoolMap.put("competitiveProductFlag", 0);
            }
            schoolList.add(schoolMap);
        });
        //存储学校列表信息
        message.add("schoolList", schoolList);
        message.add("schoolNum", schoolList.size());
        return message;
    }

    /**
     * 学校批量标记为无竞品
     *
     * @return
     */
    @RequestMapping(value = "batch_mark_no_cp.vpage")
    @ResponseBody
    public MapMessage batchMarkNoCp() {
        Set<Long> schoolIds = requestLongSet("schoolIdStr");
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            agentCompetitiveProductService.updateCompetitiveProductFlag(schoolIds, 1);
        }
        return MapMessage.successMessage();
    }

    /**
     * 学校竞品列表接口
     *
     * @return
     */
    @RequestMapping(value = "school_cp_list.vpage")
    @ResponseBody
    public MapMessage schoolCpList() {
        MapMessage message = MapMessage.successMessage();
        Long schoolId = requestLong("schoolId");
        //根据学校ID获取学校扩展信息，获取竞品标识
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (schoolExtInfo != null) {
            if (schoolExtInfo.getCompetitiveProductFlag() == null) {
                message.put("competitiveProductFlag", 0);
            } else {
                message.put("competitiveProductFlag", schoolExtInfo.getCompetitiveProductFlag());
            }
        } else {
            message.put("competitiveProductFlag", 0);
        }
        //获取学校阶段
        CrmSchoolSummary crmSchoolSummary = crmSummaryLoaderClient.loadSchoolSummary(schoolId);
        if (crmSchoolSummary != null) {
            SchoolLevel schoolLevel = crmSchoolSummary.getSchoolLevel();
            if (schoolLevel == null) {
                message.put("schoolLevel", 0);
            } else {
                message.put("schoolLevel", schoolLevel.getLevel());
            }
        } else {
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
            if (school != null) {
                message.put("schoolLevel", school.getLevel());
            }
        }
        //根据学校ID获取竞品列表信息
        List<AgentCompetitiveProduct> competitiveProductList = new ArrayList<>();
        competitiveProductList = agentCompetitiveProductService.loadBySchoolId(schoolId);
        List<Map<String, Object>> competitiveProductListFinal = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(competitiveProductList)) {
            competitiveProductList.forEach(p -> {
                StringBuilder intoTimeStrBuilder = new StringBuilder();
                Map<String, Object> competitiveProductMap = new HashMap<>();
                competitiveProductMap.put("id", p.getId());
                competitiveProductMap.put("name", p.getName());
                competitiveProductMap.put("studentNum", p.getStudentNum());
                String intoTimeStr = p.getIntoTime() == null ? "" : p.getIntoTime().toString();
                if (!"".equals(intoTimeStr)) {
                    intoTimeStrBuilder.append(intoTimeStr.substring(0, 4));
                    intoTimeStrBuilder.append("-");
                    intoTimeStrBuilder.append(intoTimeStr.substring(4, 6));
                }
                competitiveProductMap.put("intoTime", intoTimeStrBuilder);
                competitiveProductMap.put("ifSchoolPay", p.getIfSchoolPay());
                competitiveProductMap.put("remark", p.getRemark());
                competitiveProductMap.put("usageScenario", StringUtils.join(p.getUsageScenario().stream().map(UsageScenarioType::getValue).collect(Collectors.toList()), "、"));
                competitiveProductMap.put("paymentMode", StringUtils.join(p.getPaymentMode().stream().map(PaymentModeType::getValue).collect(Collectors.toList()), "、"));
                competitiveProductListFinal.add(competitiveProductMap);
            });
        }
        message.put("competitiveProductList", competitiveProductListFinal);
        return message;
    }

    /**
     * 学校竞品详情
     *
     * @return
     */
    @RequestMapping(value = "school_cp_detail.vpage")
    @ResponseBody
    public MapMessage schoolCpDetail() {
        MapMessage message = MapMessage.successMessage();
        String id = requestString("id");
        List<String> usageScenarioList = new ArrayList<>();
        List<String> paymentModeList = new ArrayList<>();
        List<Map<String, Object>> usageScenarioListAll = new ArrayList<>();
        List<Map<String, Object>> paymentModeListAll = new ArrayList<>();
        if (StringUtils.isNotBlank(id)) {
            AgentCompetitiveProduct competitiveProduct = agentCompetitiveProductService.loadById(id);
            if (competitiveProduct != null) {
                message.put("id", competitiveProduct.getId());
                message.put("name", competitiveProduct.getName());
                message.put("studentNum", competitiveProduct.getStudentNum());
                String intoTimeStr = competitiveProduct.getIntoTime() == null ? "" : competitiveProduct.getIntoTime().toString();
                if (StringUtils.isNotBlank(intoTimeStr)) {
                    message.put("intoTime", intoTimeStr.substring(0, 4) + "-" + intoTimeStr.substring(4, 6));
                }
                message.put("ifSchoolPay", competitiveProduct.getIfSchoolPay());
                message.put("remark", competitiveProduct.getRemark());
                usageScenarioList.addAll(competitiveProduct.getUsageScenario().stream().map(UsageScenarioType::name).collect(Collectors.toList()));
                paymentModeList.addAll(competitiveProduct.getPaymentMode().stream().map(PaymentModeType::name).collect(Collectors.toList()));
            } else {
                return MapMessage.errorMessage("该竞品信息不存在！");
            }
            message.put("usageScenario", usageScenarioList);
            message.put("paymentMode", paymentModeList);
        }
        Arrays.stream(UsageScenarioType.values()).forEach(p -> {
            Map<String, Object> usageScenarioMap = new HashMap<>();
            usageScenarioMap.put("cp_key", p.name());
            usageScenarioMap.put("cp_value", p.getValue());
            usageScenarioListAll.add(usageScenarioMap);
        });
        Arrays.stream(PaymentModeType.values()).forEach(p -> {
            Map<String, Object> paymentModeMap = new HashMap<>();
            paymentModeMap.put("cp_key", p.name());
            paymentModeMap.put("cp_value", p.getValue());
            paymentModeListAll.add(paymentModeMap);
        });
        message.put("usageScenarioAll", usageScenarioListAll);
        message.put("paymentModeAll", paymentModeListAll);
        return message;
    }

    /**
     * 竞品保存
     *
     * @return
     */
    @RequestMapping(value = "save.vpage")
    @ResponseBody
    public MapMessage save() {
        String id = requestString("id");
        Long schoolId = requestLong("schoolId");
        String name = requestString("name");
        Integer studentNum = requestInteger("studentNum");
        String intoTime = requestString("intoTime");
        String usageScenario = requestString("usageScenario");
        Integer ifSchoolPay = requestInteger("ifSchoolPay");
        String paymentMode = requestString("paymentMode");
        String remark = requestString("remark");
        List<UsageScenarioType> usageScenarioTypeList = new ArrayList<>();
        List<PaymentModeType> paymentModeTypeList = new ArrayList<>();
        AgentCompetitiveProduct agentCompetitiveProduct = new AgentCompetitiveProduct();
        //如果是编辑，先获取该竞品信息
        if (StringUtils.isNotBlank(id)) {
            agentCompetitiveProduct = agentCompetitiveProductService.loadById(id);
        }
        agentCompetitiveProduct.setSchoolId(schoolId);
        agentCompetitiveProduct.setName(name);
        agentCompetitiveProduct.setStudentNum(studentNum);
        agentCompetitiveProduct.setIfSchoolPay(ifSchoolPay);
        agentCompetitiveProduct.setRemark(remark);
        if (StringUtils.isNotBlank(intoTime)) {
            intoTime = intoTime.replace("-", "");
            agentCompetitiveProduct.setIntoTime(Integer.parseInt(intoTime));
        }
        if (StringUtils.isNotBlank(usageScenario)) {
            Arrays.stream(usageScenario.split(",")).forEach(p -> {
                usageScenarioTypeList.add(UsageScenarioType.valueOf(p));
            });
        }
        if (StringUtils.isNotBlank(paymentMode)) {
            Arrays.stream(paymentMode.split(",")).forEach(p -> {
                paymentModeTypeList.add(PaymentModeType.valueOf(p));
            });
        }
        agentCompetitiveProduct.setUsageScenario(usageScenarioTypeList);
        agentCompetitiveProduct.setPaymentMode(paymentModeTypeList);
        //查询该学校是否存在该竞品名称
        List<AgentCompetitiveProduct> competitiveProductList = agentCompetitiveProductService.loadBySchoolId(schoolId);
        //如果是新增，只要判断该竞品名称有没有存在相同的即可
        if (!StringUtils.isNotBlank(id)) {
            competitiveProductList = competitiveProductList.stream().filter(p -> p.getName().equals(name)).collect(Collectors.toList());
            //如果是编辑，不光要判断该竞品名称是否存在相同的，还有判断名称相同的情况下，是否id相同（如果id不相同，说明已经存在该名称的竞品）
        } else {
            competitiveProductList = competitiveProductList.stream().filter(p -> p.getName().equals(name) && !p.getId().equals(id)).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(competitiveProductList)) {
            return MapMessage.errorMessage("该竞品名称已存在！");
        }
        //新增
        if (!StringUtils.isNotBlank(id)) {
            //新增竞品信息
            agentCompetitiveProductService.insert(agentCompetitiveProduct);
            //更新该学校竞品标识
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
            //如果该学校不存在扩展信息，新增学校扩展信息
            if (schoolExtInfo == null) {
                schoolExtInfo = new SchoolExtInfo();
                schoolExtInfo.setId(schoolId);
                schoolExtInfo.setCompetitiveProductFlag(2);
                schoolExtServiceClient.getSchoolExtService().upsertSchoolExtInfo(schoolExtInfo);
                //如果该学校存在扩展信息，直接更新
            } else {
                schoolExtInfo.setCompetitiveProductFlag(2);
                schoolExtServiceClient.getSchoolExtService().updateSchoolExtInfo(schoolExtInfo);
            }
            //编辑
        } else {
            agentCompetitiveProduct.setId(id);
            agentCompetitiveProductService.replace(agentCompetitiveProduct);
        }
        return MapMessage.successMessage();
    }

    /**
     * 竞品删除
     *
     * @return
     */
    @RequestMapping(value = "delete.vpage")
    @ResponseBody
    public MapMessage delete() {
        String id = requestString("id");
        if (StringUtils.isNotBlank(id)) {
            AgentCompetitiveProduct competitiveProduct = agentCompetitiveProductService.loadById(id);
            if (competitiveProduct != null) {
                agentCompetitiveProductService.removeById(id);
            } else {
                return MapMessage.errorMessage("该竞品信息不存在！");
            }
        }
        return MapMessage.successMessage();
    }
}
