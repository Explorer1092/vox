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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.api.concurrent.AlpsFutureMap;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.config.api.constant.GlobalTagName;
import com.voxlearning.utopia.service.config.api.entity.GlobalTag;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.Flower;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.mobile.AbstractMobileJxtController;
import com.voxlearning.washington.controller.open.ApiConstants;
import org.springframework.ui.Model;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Created by Hailong Yang on 2015/9/17.
 */
public class AbstractMobileParentController extends AbstractMobileJxtController {
    public static final String FIELD_PICLISTEN_PACKAGE_ID = "piclisten_package_id";   //套餐id
    public static final String FIELD_BOOK_ID = "sdk_book_id";
    public static final String FIELD_SDK_NAME = "sdk";

    private static final String reLoginUrl = "/parentMobile/home/error.vpage?errorCode=" + ApiConstants.RES_RESULT_NEED_RELOGIN_CODE;

    //允许家长与sid没有关联的请求通过
    private static final Set<String> allowedNoRefSidPass = new HashSet<>();

    static {
        allowedNoRefSidPass.add("/parentMobile/study_together/user/validate_identity.vpage");
        allowedNoRefSidPass.add("/parentMobile/study_together/user/bind_identity.vpage");
        allowedNoRefSidPass.add("/parentMobile/parent/bind_identity.vpage");
    }

    protected final String parentMobileProvisionPrefix = "parent_login_provision_";

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private GlobalTagServiceClient globalTagServiceClient;
    @Inject
    private FlowerServiceClient flowerServiceClient;
    @Inject
    protected AsyncFootprintServiceClient asyncFootprintServiceClient;

    private static boolean isAjax(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return header != null && "XMLHttpRequest".equals(header);
    }

    protected String getAppVersion() {
        return getRequestString("app_version");
    }

    protected long currentRequestStudentId() {
        long sid = getRequestLong("sid");
        if (sid == 0L) {
            sid = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"));
        }
        return sid;
    }

    @Override
    protected User currentParent() {
        if (RuntimeMode.isDevelopment() && getRequestLong("pid") != 0)
            return raikouSystem.loadUser(getRequestLong("pid"));
        User user = getWebRequestContext().getCurrentUser();
        if (user != null && user.isParent()) {
            return user;
        }
        return null;
    }

    @Override
    public boolean onBeforeControllerMethod() {
        String version = getAppVersion();
        //小于1.8.2版本。直接去升级页面
        // dirty hack: 这里只对家长通app过来的请求做升级校验
        if (StringUtils.isNotBlank(version) && StringUtils.equals("17Parent", getRequestString("client_name")) && VersionUtil.compareVersion(version, "2.1.8") < 0) {
            String downloadRedirectUrl = "redirect:/parentMobile/ucenter/upgrade.vpage";
            try {
                getResponse().sendRedirect(downloadRedirectUrl);
            } catch (IOException e) {
                logger.error("17Parent redirect error", e);
            }
            return false;
        }
        Long studentId = getRequestLong("sid");
        User parent = currentParent();
        String uri = getRequest().getRequestURI();
        if (parent != null) {
            List<User> students = studentLoaderClient.loadParentStudents(parent.getId());
            if (studentId != 0 && !allowedNoRefSidPass.contains(uri)) {
                //家长与孩子无关联。直接重新登录
                if (!students.stream().map(User::getId).collect(Collectors.toSet()).stream().anyMatch(studentId::equals)) {
                    try {
                        handleRequest();
                    } catch (IOException e) {
                        logger.error("17Parent redirect error", e);
                    }
                    return false;
                }
            }
        }
        return super.onBeforeControllerMethod();
    }

    /**
     * 非ajax请求,跳转.
     * ajax请求,则返回json;
     *
     * @throws IOException
     */
    private void handleRequest() throws IOException {
        if (!isAjax(getRequest())) {
            getResponse().sendRedirect(reLoginUrl);
        } else {
            getResponse().getWriter().write(JsonUtils.toJson(MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE)));
        }
    }

    protected void setRouteParameter(Model model) {
        try {
            for (Map.Entry<String, String[]> entry : getRequest().getParameterMap().entrySet()) {
                String value = StringUtils.join(entry.getValue(), ",");
                model.addAttribute(entry.getKey(), value);
            }
        } catch (Exception ex) {
            model.addAttribute("result", "error");
        }
    }

    protected boolean isBindClazz(Long studentId) {
        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);

        if (student == null || student.getClazz() == null || deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId) == null) {
            return false;
        }
        return true;
    }

    /**
     * 班级同学的送花列表
     *
     * @param studentId
     * @return
     */
    protected List<Map<String, Object>> flowerRank(Long studentId) {

        if (studentId == null || studentId <= 0) {
            return null;
        }

        List<Map<String, Object>> rankList = new ArrayList<>();

        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, true);

        if (CollectionUtils.isEmpty(groupMappers)) {
            return null;
        }

        List<GroupMapper.GroupUser> userList = new ArrayList<>();
        groupMappers.stream()
                .filter(p -> p != null)
                .filter(p -> p.getStudents() != null)
                .forEach(p -> userList.addAll(p.getStudents()));

        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }

        Set<Long> studentIds = userList.stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
        AlpsFutureMap<Long, List<Flower>> futureMap = new AlpsFutureMap<>();
        for (Long id : studentIds) {
            futureMap.put(id, flowerServiceClient.getFlowerService().loadSenderFlowers(id));
        }
        Map<Long, Student> studentMap = studentLoaderClient.loadStudents(studentIds);
        if (MapUtils.isEmpty(studentMap)) {
            return null;
        }

        MonthRange range = MonthRange.current();
        studentIds.forEach(userId -> {
            Map<String, Object> map = new HashMap<>();
            List<Flower> flowerList = futureMap.getUninterruptibly(userId);

            Student student = studentMap.get(userId);
            if (student != null) {
                map.put("studentId", userId);
                map.put("studentName", student.fetchRealname());

                if (CollectionUtils.isEmpty(flowerList)) {
                    map.put("flowerCount", 0L);
                    map.put("lastTime", 0L);
                } else {
                    Object flowerCount = flowerList.stream().filter(p -> range.contains(p.getCreateDatetime())).count();
                    map.put("flowerCount", flowerCount);

                    Comparator<Flower> c = (o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime());
                    flowerList = flowerList.stream().sorted(c)
                            .collect(Collectors.toList());
                    map.put("lastTime", flowerList.get(0).getCreateDatetime().getTime());
                }
                rankList.add(map);
            }
        });

        return rankList;
    }


    protected MapMessage getOneSelfChoosePractice(Long studentId) {
        User parent = currentParent();
        StudentDetail detail = studentLoaderClient.loadStudentDetail(studentId);
        if (detail == null) {
            return MapMessage.errorMessage("学生账号信息异常");
        }

        Set<String> blackUsers = globalTagServiceClient.getGlobalTagBuffer()
                .findByName(GlobalTagName.AfentiBlackListUsers.name())
                .stream()
                .filter(t -> t != null)
                .filter(t -> t.getTagValue() != null)
                .map(GlobalTag::getTagValue)
                .collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(blackUsers) && blackUsers.contains(studentId.toString())) {
            return MapMessage.errorMessage("该学生在黑名单内");
        }

        List<UserActivatedProduct> activatedProductList = userOrderLoaderClient.loadUserActivatedProductList(studentId);
        // 已支付、未过期的订单
        Set<OrderProductServiceType> paidProductTypeSet = activatedProductList.stream()
                .filter(p -> p != null && p.getServiceEndTime() != null)
                .filter(p -> new Date().before(p.getServiceEndTime()))
                .map(u -> OrderProductServiceType.safeParse(u.getProductServiceType()))
                .collect(Collectors.toSet());

        // 页面随机展示符合筛选条件的一个产品
        Map<String, Object> infoMap = new HashMap<>();

        Integer level = detail.getClazzLevelAsInteger();
        if (level != null && level >= 1 && level <= 6) {
            List<OrderProduct> productInfos = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(detail);
            if (CollectionUtils.isEmpty(productInfos)) {
                return MapMessage.errorMessage("自选练习为空");
            }
            Map<String, VendorApps> availableApps = businessVendorServiceClient.getParentAvailableApps(parent, detail)
                    .stream()
                    .collect(Collectors.toMap(VendorApps::getAppKey, Function.identity()));

            // 洛亚传说 三国 关付费 挂公告 不推荐
            Set<String> appStrings = availableApps.keySet().stream()
                    .filter(s -> !StringUtils.equals(s, A17ZYSPG.name())
                            && !StringUtils.equals(s, SanguoDmz.name())
                            && !StringUtils.equals(s, TravelAmerica.name())
                            && !StringUtils.equals(s, PetsWar.name()))
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(appStrings)) return MapMessage.errorMessage("自选练习为空");

            //为了随机选择一个，所以先乱序
            Collections.shuffle(productInfos);
            for (OrderProduct info : productInfos) {
                if (CollectionUtils.isNotEmpty(paidProductTypeSet) && paidProductTypeSet.contains(OrderProductServiceType.safeParse(info.getProductType()))) {
                    continue;
                }
                if (appStrings.contains(info.getProductType())) {
                    infoMap.put("name", info.getName());
                    infoMap.put("key", info.getProductType());
                    VendorApps vendorApps = availableApps.get(info.getProductType());
                    infoMap.put("info", vendorApps == null ? "" : vendorApps.getDescription());
                    return MapMessage.successMessage().add("product", infoMap);
                }
            }
        }
        return MapMessage.errorMessage("没有趣味学习");
    }


    /**
     * 老师检查完作业，领取学豆的奖励榜（产品改为取该作业id的学霸列表）
     */
    protected List<Map<String, Object>> afterCheckHomeworkIntegralRank(String homeworkId) {
        Map<String, NewHomeworkStudyMaster> homeworkStudyMasterMap = newHomeworkPartLoaderClient.getNewHomeworkStudyMasterMap(Collections.singleton(homeworkId));
        List<Map<String, Object>> list = new ArrayList<>();
        if (MapUtils.isNotEmpty(homeworkStudyMasterMap) && homeworkStudyMasterMap.containsKey(homeworkId)) {
            NewHomeworkStudyMaster studyMaster = homeworkStudyMasterMap.get(homeworkId);
            if (CollectionUtils.isNotEmpty(studyMaster.getMasterStudentList())) {
                studyMaster.getMasterStudentList().stream()
                        .filter(p -> p != null)
                        .forEach(p -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("student", p.getUserName());
                            map.put("integral", 5);
                            list.add(map);
                        });
            }
        }
        return list;
    }

    /**
     * 获取用户名（统一为：XX爸爸；XX妈妈等）(取第一个产生绑定关系的孩子)
     */
    protected String generateUserNameForComment(User user) {
        String userName = "";
        if (user == null) {
            return userName;
        }
        if (UserType.PARENT == user.fetchUserType()) {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                //1.5.3以上支持c端家长评论。是可能没有孩子的.所以名字显示为带星号的手机号
                userName = sensitiveUserDataServiceClient.loadUserMobileObscured(user.getId());
            } else {
                StudentParentRef studentParentRef = studentParentRefs.stream()
                        .sorted((e1, e2) -> Long.compare(e2.getCreateTime().getTime(), e1.getCreateTime().getTime()))
                        .findFirst()
                        .orElse(null);
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentParentRef.getStudentId());
                userName = studentDetail.fetchRealname() + (CallName.其它监护人.name().equals(studentParentRef.getCallName()) ? "家长" : studentParentRef.getCallName());
            }
        } else if (UserType.TEACHER == user.fetchUserType()) {
            Teacher teacher = teacherLoaderClient.loadTeacher(user.getId());
            String subjectName = teacher != null && teacher.getSubject() != null ? teacher.getSubject().getValue() : "";
            userName = subjectName + user.fetchRealname() + "老师";
        }
        return userName;
    }


    /**
     * 判断要处理的产品类型如果创建订单是否是要挂在家长身上
     */
    protected boolean gonnaCreateOrderForParent(OrderProductServiceType orderProductServiceType) {
        return null != orderProductServiceType
                && (OrderProductServiceType.PicListenBook == orderProductServiceType || OrderProductServiceType.FollowRead == orderProductServiceType || OrderProductServiceType.WalkerMan == orderProductServiceType || OrderProductServiceType.ChipsEnglish == orderProductServiceType
                || OrderProductServiceType.StudyMates == orderProductServiceType || OrderProductServiceType.PalaceMuseum == orderProductServiceType
                || OrderProductServiceType.UnclePei == orderProductServiceType
        );
    }

    //登录成功时做额外处理
    protected void doExtThingForLogin(User parent) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
        String userPass = ua.getPassword();
        // 登录成功时记录信息
        Long userId = parent.getId();
        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId, getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.wap);
        getWebRequestContext().saveAuthenticationStates(-1, userId, userPass, RoleType.ROLE_PARENT);
        //如果是短信邀请进来的，默认绑定手机号
        miscServiceClient.bindInvitedTeacherMobile(userId);

        String deviceId = getRequestString(REQ_UUID);
        if (StringUtils.isEmpty(deviceId))
            deviceId = getRequestString(REQ_IMEI);

        String sys = getRequestString(REQ_SYS);
        String model = getRequestString(REQ_MODEL);

        // 登录成功，记录设备号
        asyncFootprintServiceClient.getAsyncFootprintService().recordUserDeviceInfo(parent.getId(), deviceId, sys, model);

        String authenticatedMobile = sensitiveUserDataServiceClient.loadUserMobile(parent.getId());
        if (authenticatedMobile != null) {
            CacheObject<Boolean> objectCacheObject = washingtonCacheSystem.CBS.persistence.get(parentMobileProvisionPrefix + authenticatedMobile);
            if (objectCacheObject != null && objectCacheObject.getValue() != null) {
                if (objectCacheObject.getValue()) {
                    parentServiceClient.agreeParentBrandFlag(parent.getId());
                } else {
                    parentServiceClient.notAgreeParentBrandFlag(parent.getId());
                }
                washingtonCacheSystem.CBS.persistence.delete(parentMobileProvisionPrefix + authenticatedMobile);
            }
        }
    }

}
