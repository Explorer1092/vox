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

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.com.alibaba.dubbo.rpc.cluster.merger.BooleanArrayMerger;
import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import com.voxlearning.utopia.api.constant.RecordType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.integral.api.entities.Credit;
import com.voxlearning.utopia.service.integral.api.entities.CreditHistory;
import com.voxlearning.utopia.service.integral.client.CreditLoaderClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardService;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardItem;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Longlong Yu
 * @since 下午6:59,13-7-15.
 */
@Controller
@RequestMapping("/crm/parent")
@SuppressWarnings("GrMethodMayBeStatic")
public class CrmParentController extends CrmAbstractController {

    private static final int MAX_PARENT_AMOUNT = 20;

    @Inject
    private AsyncUserServiceClient asyncUserServiceClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private CreditLoaderClient creditLoaderClient;
    @Inject
    private CouponLoaderClient couponLoaderClient;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @ImportService(interfaceClass = ParentRewardService.class)
    private ParentRewardService parentRewardService;
    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;
    @Inject
    private GlobalTagServiceClient globalTagServiceClient;

    @RequestMapping(value = "parentlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String parentList(Model model) {
        List<String> conditionKeys = Arrays.asList("parentId", "mobile");

        Map<String, String> conditionMap = new HashMap<>();
        for (String key : conditionKeys) {
            String value = getRequestParameter(key, "").replaceAll("\\s", "");
            conditionMap.put(key, value);
        }

        List<Long> parentIdList = getParentIdList(conditionMap);

        List<Map<String, Object>> parentList = getParentSnapshot(parentIdList);
        if (CollectionUtils.isEmpty(parentList)) {
            if (isRequestPost())
                getAlertMessageManager().addMessageError("用户不存在或者用户不是家长用户。");
        }
        model.addAttribute("parentList", parentList);
        model.addAttribute("conditionMap", conditionMap);

        return "crm/parent/parentlist";
    }

    @RequestMapping(value = "parenthomepage.vpage", method = RequestMethod.GET)
    public String parentHomepage(Model model) {

        Long parentId;

        try {
            parentId = Long.parseLong(getRequestParameter("parentId", ""));
        } catch (Exception ignored) {
            getAlertMessageManager().addMessageError("用户ID " + getRequestParameter("parentId", "") + " 不合规范。");
            return redirect("/crm/parent/parentlist.vpage");
        }

        Map<String, Object> parentInfoMap = getParentInfoMap(parentId);

        if (MapUtils.isEmpty(parentInfoMap)) {
            getAlertMessageManager().addMessageError("用户(ID:" + parentId + ")不存在或者用户(ID:" + parentId + ")不是学生用户。");
            return redirect("/crm/parent/parentlist.vpage");
        }

        UserLoginInfo loginInfo = userLoginServiceClient.getUserLoginService().loadUserLoginInfo(parentId).getUninterruptibly();
        if (loginInfo != null) {
            parentInfoMap.put("lastLoginTime", loginInfo.getLoginTime());
        }

        List<KeyValuePair<Integer, String>> recordTypeList = RecordType.toKeyValuePairs();

        model.addAttribute("recordTypeList", recordTypeList);

        model.addAttribute("parentInfoAdminMapper", parentInfoMap);

        Credit credit = creditLoaderClient.getCreditLoader().loadCredit(parentId);
        List<CreditHistory> creditHistories = creditLoaderClient.getCreditLoader().loadCreditHistories(parentId);
        model.addAttribute("credit", credit);
        model.addAttribute("creditHistories", creditHistories);
        List<ParentRewardItem> items = parentRewardLoader.loadParentRewardItemsFromDB().getUninterruptibly()
                .stream()
                .filter(item -> !item.getDisabled())
                .sorted(Comparator.comparing(ParentRewardItem::getRank))
                .collect(Collectors.toList());
        items.forEach(item -> {
            if (item.getKey().contains("HOMEWORK_100")) {
                String title = item.getTitle();
                item.setTitle(title.replace("{score}", "100"));
            }
        });
        model.addAttribute("items", items);
        return "crm/parent/parenthomepage";
    }

    /**
     * 更新家长通的sessionKey,让家长重新登录
     */
    @AdminAcceptRoles(postRoles = {AdminPageRole.POST_ACCESSOR})
    @RequestMapping(value = "kickOutOfApp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage kickOutOfApp() {

        long userId = getRequestLong("userId");
        if (userId > 0) {
            updateUserAppSessionKey(userId, "17Parent");
            updateUserAppSessionKey(userId, "17JuniorPar");
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("userId???");
        }
    }

    @RequestMapping(value = "bindstudentparent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindStudentParent() {

        long studentId = getRequestLong("studentId", -1L);
        long parentId = getRequestLong("parentId", -1L);
        String parentCallName = getRequestParameter("parentCallName", "");
        String bindDesc = getRequestParameter("bindDesc", "");
        String bindExtraDesc = getRequestParameter("bindExtraDesc", "");

        MapMessage message = new MapMessage();
        if (studentId < 0 || parentId < 0) {
            message.setSuccess(false);
            message.setInfo("绑定失败，请正确填写孩子学号");
            return message;
        }
        if (StringUtils.isBlank(bindDesc) || ("其他".equals(bindDesc) && StringUtils.isBlank(bindExtraDesc))) {
            message.setSuccess(false);
            message.setInfo("绑定失败，请正确填写各参数");
            return message;
        }
        try {
            bindDesc += "（" + bindExtraDesc + "）";
            User user = userLoaderClient.loadUser(studentId, UserType.STUDENT);
            if (user == null) {
                message.setSuccess(false);
                message.setInfo("绑定失败，不存在输入学号的学生！");
                return message;
            }
            boolean isKeyParent = false;
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(parentId);
            if ((userAuthentication != null && userAuthentication.isMobileAuthenticated())
                    && parentLoaderClient.loadStudentKeyParent(studentId) == null) {
                // parent has mobile and student has no key parent
                isKeyParent = true;
            }
            MapMessage mapMessage = parentServiceClient.bindExistingParent(studentId, parentId, isKeyParent, parentCallName);
            if (!mapMessage.isSuccess()) {
                if (StringUtils.isBlank(mapMessage.getInfo())) mapMessage.setInfo("绑定失败!");
                return mapMessage;
            }
            String operation = "管理员" + getCurrentAdminUser().getAdminUserName() + "给家长(" + parentId + ")绑定孩子(" + studentId + "),孩子称呼家长:" + parentCallName + "。";
            addAdminLog(operation, parentId, bindDesc);

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(parentId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("绑定孩子");
            userServiceRecord.setComments(operation + "；说明[" + bindDesc + "]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            message.setSuccess(true);
            message.setInfo("绑定成功");
        } catch (Exception ex) {
            message.setSuccess(false);
            message.setInfo("绑定失败，" + ex.getMessage());
        }
        return message;
    }


    @RequestMapping(value = "bindmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bindmobile() {
        Long parentId = getRequestLong("parentId");
        String mobile = getRequestString("mobile").trim();
        String desc = getRequestString("desc").trim();

        if (StringUtils.isBlank(desc)) {
            return MapMessage.errorMessage("备注描述为空,请输入");
        }

        if (StringUtils.isBlank(mobile) || !MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效的手机号码");
        }

        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(parentId);
        if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {
            return MapMessage.errorMessage("该用户不需要绑定手机");
        }

        userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
        if (userAuthentication != null) {
            return MapMessage.errorMessage("手机号已被占用，请重新输入");
        }

        MapMessage msg = userServiceClient.activateUserMobile(parentId, mobile, false, getCurrentAdminUser().getAdminUserName(), desc);
        if (!msg.isSuccess()) {
            return MapMessage.errorMessage("绑定手机失败");
        }

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "generatereward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateReward() {
        Long studentId = getRequestLong("studentId");
        String itemKey = getRequestString("itemKey");
        String ext = getRequestString("ext");
        Map<String, Object> extMap = new HashMap<>();
        if (studentId == 0L) {
            return MapMessage.errorMessage("学生id错误");
        }
        ParentRewardItem item = parentRewardLoader.loadParentRewardItemFromDBByKey(itemKey).getUninterruptibly();
        if (item == null) {
            return MapMessage.errorMessage("奖励类型错误");
        }
        if (StringUtils.isNotBlank(ext)) {
            String[] paramArr = ext.split(":");
            if (paramArr.length != 2) {
                return MapMessage.errorMessage("请正确配置参数信息");
            } else {
                extMap.put(paramArr[0], paramArr[1]);
            }
        }

        if (item.getKey().contains("100")) {
            extMap.put("score", 100);
        }
        parentRewardService.generateParentReward(studentId, itemKey, extMap);

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "del_parent_ext_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delParentExtInfo() {
        long parentId = getRequestLong("parentId");
        if (parentId == 0L) {
            return MapMessage.errorMessage("家长id错误");
        }
        ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(parentId);
        parentExtAttribute.setProfession("");
        parentExtAttribute.setEducationDegree("");
        MapMessage mapMessage = parentServiceClient.updateParentExtAttribute(parentExtAttribute);
        if (mapMessage.isSuccess()) {
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("删除失败");
    }

    @RequestMapping(value = "unbind_wechat_app_login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage unbindWechatLogin() {
        Long parentId = getRequestLong("parentId");
        if (parentId == 0L) {
            return MapMessage.errorMessage("家长id错误");
        }
        MapMessage message = wechatServiceClient.getWechatService().unbindUserAndWechatWithUserIdAndType(parentId, WechatType.PARENT_APP.getType());
        if (message.isSuccess()) {
            addAdminLog("解绑家长APP登录微信", parentId, "管理员" + getCurrentAdminUser().getAdminUserName() + "解绑用户微信");
        }
        return message;
    }

    @RequestMapping(value = "allowPay.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage allowPay() {
        Long parentId = getRequestLong("parentId");
        if (parentId == 0L) {
            return MapMessage.errorMessage("家长id错误");
        }
        try {
            Date todayEnd = DateUtils.getTodayEnd();
            Boolean result = CacheSystem.CBS.getCache("unflushable").set("allowPay:" + SafeConverter.toString(parentId), (int) (todayEnd.getTime() / 1000), true);
            if(SafeConverter.toBoolean(result)){
                addAdminLog("解除家长支付限制", parentId, "管理员" + getCurrentAdminUser().getAdminUserName() + "解除家长支付限制");
            }
            return MapMessage.successMessage();
        }catch(Exception e){
            logger.error("家长设置支付限制异常",e);
            return MapMessage.errorMessage().setInfo("支付限制设置异常");
        }
    }



    /**
     * *********************private method*****************************************************************
     */
    private List<Long> getParentIdList(Map<String, String> conditionMap) {
        List<Long> parentIdList = new ArrayList<>();

        if (StringUtils.isNotBlank(conditionMap.get("parentId"))) {
            Long parentId;
            try {
                parentId = conversionService.convert(conditionMap.get("parentId"), Long.class);
            } catch (Exception ignored) {
                return Collections.emptyList();
            }

            User parent = userLoaderClient.loadUser(parentId, UserType.PARENT);
            if (parent != null)
                parentIdList.add(parentId);
        } else if (StringUtils.isNotBlank(conditionMap.get("mobile"))) {
            String mobile = StringUtils.trim(conditionMap.get("mobile"));
            UserAuthentication authentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
            if (authentication != null) {
                parentIdList.add(authentication.getId());
            }

        }

        return parentIdList;
    }

    private List<Map<String, Object>> getParentSnapshot(List<Long> parentIdList) {

        if (CollectionUtils.isEmpty(parentIdList))
            return Collections.emptyList();

        List<Map<String, Object>> parentList = new ArrayList<>();

        for (Long parentId : parentIdList) {

            Map<String, Object> parentMap = new HashMap<>();

            User parent = userLoaderClient.loadUser(parentId, UserType.PARENT);
            if (parent != null) {
                parentMap.put("parentId", parent.getId());
                if (parent.getProfile() != null) {
                    UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
                    parentMap.put("parentName", parent.getProfile().getRealname());
                    parentMap.put("parentMobile", ua.getSensitiveMobile());
//                    parentMap.put("parentEmail", parent.getProfile().getSensitiveEmail());

                    parentMap.put("verifyMobile", ua.isMobileAuthenticated());
//                    parentMap.put("verifyEmail", userLoaderClient.loadEmailAuthentication(parent.getProfile().getSensitiveEmail()) != null);
                }
            }

            List<Map<String, Object>> childInfoList = new ArrayList<>();

            List<User> childList = studentLoaderClient.loadParentStudents(parentId);
            for (User child : childList) {

                Map<String, Object> childMap = new HashMap<>();

                childMap.put("childId", child.getId());
                if (child.getProfile() != null)
                    childMap.put("childName", child.getProfile().getRealname());

                Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(child.getId());
                if (clazz != null) {
                    childMap.put("clazzId", clazz.getId());
                    childMap.put("clazzLevel", clazz.getClassLevel());
                    childMap.put("clazzName", clazz.getClassName());
                    childMap.put("schoolId", clazz.getSchoolId());

                    School school = schoolLoaderClient.getSchoolLoader()
                            .loadSchool(clazz.getSchoolId())
                            .getUninterruptibly();
                    childMap.put("schoolName", (school == null) ? null : school.getCname());
                }

                List<StudentParentRef> studentParentRefs = new ArrayList<>();
                for (StudentParentRef ref : studentLoaderClient.loadStudentParentRefs(child.getId())) {
                    if (Objects.equals(ref.getParentId(), parentId)) {
                        studentParentRefs.add(ref);
                    }
                }
                if (!CollectionUtils.isEmpty(studentParentRefs))
                    childMap.put("keyParent", studentParentRefs.get(0).isKeyParent());

                childInfoList.add(childMap);
            }

            parentMap.put("childList", childInfoList);

            parentList.add(parentMap);
        }

        return parentList;
    }

    private Map<String, Object> getParentInfoMap(Long parentId) {

        if (parentId == null)
            return new HashMap<>();

        User parent = userLoaderClient.loadUser(parentId, UserType.PARENT);

        if (parent == null || parent.getDisabled())
            return new HashMap<>();

        Map<String, Object> parentInfoMap = new HashMap<>();

        parentInfoMap.put("parent", parent);

        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Parent", parentId);
        parentInfoMap.put("vendorAppsUserRef", vendorAppsUserRef);

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
        parentInfoMap.put("verifyMobile", ua.isMobileAuthenticated());

        List<Map<String, Object>> childInfoList = new ArrayList<>();

        List<User> childList = studentLoaderClient.loadParentStudents(parent.getId());
        for (User child : childList) {
            Map<String, Object> childMap = new HashMap<>();
            childMap.put("childId", child.getId());
            if (child.getProfile() != null)
                childMap.put("childName", child.getProfile().getRealname());

            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(child.getId());
            if (clazz != null) {
                childMap.put("clazzId", clazz.getId());
                childMap.put("clazzLevel", clazz.getClassLevel());
                childMap.put("clazzName", clazz.getClassName());
                childMap.put("schoolId", clazz.getSchoolId());

                School school = schoolLoaderClient.getSchoolLoader()
                        .loadSchool(clazz.getSchoolId())
                        .getUninterruptibly();
                childMap.put("schoolName", (school == null) ? null : school.getCname());
            }

            List<StudentParentRef> studentParentRefs = new ArrayList<>();
            for (StudentParentRef ref : studentLoaderClient.loadStudentParentRefs(child.getId())) {
                if (Objects.equals(ref.getParentId(), parentId)) {
                    studentParentRefs.add(ref);
                }
            }
            if (!CollectionUtils.isEmpty(studentParentRefs))
                childMap.put("keyParent", studentParentRefs.get(0).isKeyParent());

            childInfoList.add(childMap);
        }
        parentInfoMap.put("childList", childInfoList);

        List<UserServiceRecord> customerServiceRecordList = userLoaderClient.loadUserServiceRecords(parent.getId());
        parentInfoMap.put("customerServiceRecordList", customerServiceRecordList);

        // user order
        List<UserOrder> tempOrderList = userOrderLoaderClient.loadUserOrderList(parentId).stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.Paid)
                .filter(o -> o.getOrderStatus() == OrderStatus.Confirmed)
                .collect(Collectors.toList());
        parentInfoMap.put("latestOrderList", tempOrderList.size() > 10 ? tempOrderList.subList(0, 10) : tempOrderList);

        // user coupon
        List<CouponShowMapper> couponShowMappers = couponLoaderClient.loadUserCoupons(parentId);
        parentInfoMap.put("couponList", couponShowMappers);

        //家长黑名单
        String blackStatus = "无黑名单";
        List<GlobalTag> blackParents = globalTagServiceClient.getGlobalTagBuffer().findByName(GlobalTagName.ParentBlackListUsers.name());
        Set<String> blackParentList = CollectionUtils.toLinkedList(blackParents).stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getTagValue() != null)
                .map(GlobalTag::getTagValue)
                .collect(Collectors.toSet());
        if (blackParentList.contains(parentId.toString())) {
            blackStatus = "家长黑名单";
        }
        parentInfoMap.put("blackStatus", blackStatus);
        //登录过App的微信列表==暂时这里只要WechatType.PARENT_APP的
        List<UserWechatRef> wechatRefList = wechatLoaderClient.getWechatLoader().loadUserWechatRefsIncludeDisabled(parentId, WechatType.PARENT_APP.getType());
        parentInfoMap.put("wechatRefList", wechatRefList);
        return parentInfoMap;
    }
}
