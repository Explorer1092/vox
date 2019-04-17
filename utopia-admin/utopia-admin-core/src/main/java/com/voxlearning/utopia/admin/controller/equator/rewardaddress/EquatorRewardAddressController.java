package com.voxlearning.utopia.admin.controller.equator.rewardaddress;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.equator.service.configuration.client.GeneralConfigServiceClient;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.cache.AdminCacheSystem;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import com.voxlearning.utopia.service.user.api.entities.UserShippingAddress;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2018/10/15.
 */
@Controller
@RequestMapping("/equator/reward/address/")
public class EquatorRewardAddressController extends AbstractEquatorController {

    @Inject
    private AdminCacheSystem adminCacheSystem;
    @Inject
    private GeneralConfigServiceClient generalConfigServiceClient;

    private static final String keyPrefix = "user_query_total_times_";
    private static final int totalQueryLimit = 10000;//一个人一天累计查询量的上限
    private static final int oneQueryLimit = 500;//一次查询的上限

    //1. url访问权限有：管理员  开发-常富贵  产品-施文强,  正式线上未部署，预发布环境可以访问
    //2. 特定的时间段内可以访问，例如设置'2018-10-16 00:00:00' ~ '2018-10-16 23:59:59’，10-16号当天能访问，其他时间不可以访问。
    //3. 每次访问上限500，每个人每天累计访问上限10000
    //4. log记录

    //批量查询用户地址
    @RequestMapping(value = "addresslist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String fetchAddrListBySids(Model model) {
        String userList = getRequestString("userList");
        if (StringUtils.isBlank(userList)) {
            return "equator/rewardaddress/addresslist";
        }
        if (RuntimeMode.isStaging()) {
            model.addAttribute("error", "预发布环境不允许访问");
            return "equator/rewardaddress/addresslist";
        }

        AuthCurrentAdminUser authCurrentAdminUser = getCurrentAdminUser();
        if (authCurrentAdminUser == null) {
            model.addAttribute("error", "需要登录");
            return "equator/rewardaddress/addresslist";
        }

        model.addAttribute("userList", userList);
        String adminUserName = authCurrentAdminUser.getAdminUserName();
        String reason = authCurrentAdminUser.getAdminUserName() + "导出手机号发放奖励";

        //特定的时间段生效    stu_address_select格式 {"beginDate":"2018-10-16 00:00:00","endDate":"2018-10-17 00:00:00"}
        try {
            String flag = generalConfigServiceClient.loadConfigValueFromClientBuffer("stu_address_select");
            Map<String, Object> flagMap = JsonUtils.fromJson(flag);
            Date beginDate = DateUtils.stringToDate(SafeConverter.toString(flagMap.get("beginDate")), DateUtils.FORMAT_SQL_DATETIME);
            Date endDate = DateUtils.stringToDate(SafeConverter.toString(flagMap.get("endDate")), DateUtils.FORMAT_SQL_DATETIME);
            Date now = new Date();
            if (now.before(beginDate) || now.after(endDate)) {
                model.addAttribute("error", "不在生效时间范围内，禁止使用");
                return "equator/rewardaddress/addresslist";
            }
        } catch (Exception e) {
            model.addAttribute("error", "配置解析有误，不在生效时间范围内，禁止使用");
            return "equator/rewardaddress/addresslist";
        }


        //每次访问上限500
        List<Long> userIds = Arrays.stream(userList.split("\n"))
                .map(t -> t.replaceAll("\\s", ""))
                .filter(StringUtils::isNotBlank)
                .map(SafeConverter::toLong)
                .filter(t -> t > 0L)
                .collect(Collectors.toList());
        if (userIds.size() > oneQueryLimit) {
            model.addAttribute("error", "每次访问上限" + oneQueryLimit);
            return "equator/rewardaddress/addresslist";
        }


        //每人每天累计查询量上限 totalQueryLimit
        long oldTotalQueryCount = loadQueryCount(adminUserName);
        if (oldTotalQueryCount + userIds.size() > totalQueryLimit) {
            model.addAttribute("error", "每人每天累计访问上限是" + totalQueryLimit + ",今天已查的数据量是" + oldTotalQueryCount + ",输入的用户量是" + userIds.size());
            return "equator/rewardaddress/addresslist";
        }


        if (CollectionUtils.isNotEmpty(userIds) && userIds.size() <= oneQueryLimit) {
            List<MapMessage> userAddressInfoList = new ArrayList<>();
            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(userIds);
            userIds.forEach(userId -> {
                StudentDetail thisStudent = studentDetailMap.getOrDefault(userId, null);
                if (thisStudent != null) {
                    MapMessage thisUserAddress = new MapMessage();
                    thisUserAddress.add("studentId", userId);
                    String phone = sensitiveUserDataServiceClient.showUserMobile(userId, reason, adminUserName);
                    thisUserAddress.add("studentName", thisStudent.fetchRealname());
                    thisUserAddress.add("studentPhone", phone == null ? "" : phone);


                    // 查询认证老师地址
                    List<ClazzTeacher> creators = teacherLoaderClient.loadClazzTeachers(thisStudent.getClazzId());
                    if (CollectionUtils.isNotEmpty(creators)) {
                        ClazzTeacher clazzTeacher = creators.stream()
                                .sorted(Comparator.comparing(o -> o.getTeacher().getCreateTime()))// 创建时间排序
                                .filter(t -> AuthenticationState.SUCCESS.equals(AuthenticationState.safeParse(t.getTeacher().getAuthenticationState())))
                                .findFirst()// 第一个认证的
                                .orElse(creators.get(0));// 没有直接返回第一个老师
                        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(clazzTeacher.getTeacher().getId());
                        thisUserAddress.add("teacherName", teacherDetail.fetchRealname());

                        //AuthenticatedMobile teacherMobile = sensitiveUserDataServiceClient.loadUserAuthenticationMobile(teacherDetail.getId(), "AddressExport:getUserPhone");
                        //thisUserAddress.add("teacherPhone", teacherMobile == null ? "" : teacherMobile.getMobile());
                        String teacherPhone = sensitiveUserDataServiceClient.showUserMobile(teacherDetail.getId(), reason, adminUserName);
                        thisUserAddress.add("teacherPhone", teacherPhone);

                        UserShippingAddress teacherAddress = userLoaderClient.loadUserShippingAddress(clazzTeacher.getTeacher().getId());

                        if (teacherAddress != null) {
                            thisUserAddress.add("province", teacherAddress.getProvinceName());
                            thisUserAddress.add("city", teacherAddress.getCityName());
                            thisUserAddress.add("country", teacherAddress.getCountyName());
                            thisUserAddress.add("school", teacherAddress.getSchoolName());
                            thisUserAddress.add("detailAddress", teacherAddress.getDetailAddress());
                        }
                    }
                    userAddressInfoList.add(thisUserAddress);
                }
            });


            long newTotalQueryCount = addQueryCount(adminUserName, userIds.size());

            //log记录
            LogCollector.info("user-phone-loaded", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "adminUser", adminUserName,
                    "thisQueryCount", userIds.size(),
                    "totalQueryCount", newTotalQueryCount,
                    "reason", "equatorExportForReward",
                    "operator", adminUserName,
                    "time", System.currentTimeMillis()
            ));


            model.addAttribute("userAddressInfoList", userAddressInfoList);
        }


        return "equator/rewardaddress/addresslist";
    }


    private long loadQueryCount(String adminUser) {
        String key = keyPrefix + adminUser;

        return SafeConverter.toLong(adminCacheSystem.CBS.storage.<Long>get(key).getValue());
    }

    private long addQueryCount(String adminUser, long addCount) {
        String key = keyPrefix + adminUser;
        int endTime = (int) (DayRange.current().getEndTime() / 1000);
        return adminCacheSystem.CBS.storage.incr(key, addCount, 0, endTime);
    }

}
