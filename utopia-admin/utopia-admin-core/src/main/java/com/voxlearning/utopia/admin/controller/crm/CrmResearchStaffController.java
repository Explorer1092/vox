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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.RecordType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.constants.RSManagedRegionType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Longlong Yu
 * @since 上午10:58,13-9-29.
 */
@Controller
@RequestMapping("/crm/researchstaff")
public class CrmResearchStaffController extends CrmAbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private IntegralLoaderClient integralLoaderClient;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    /**
     * ********************* 查询相关 *****************************************************************
     */
    @RequestMapping(value = "researchstafflist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String rStaffList(Model model) {
        Map<String, Object> conditionMap = new HashMap<>();

        Long rstfId = getRequestLong("researchStaffId", -1);
        if (rstfId != -1)
            conditionMap.put("researchStaffId", rstfId);

        conditionMap.put("provCode", getRequestInt("provCode"));
        conditionMap.put("cityCode", getRequestInt("cityCode"));
        conditionMap.put("countyCode", getRequestInt("countyCode"));

        List<Map<String, Object>> researchStaffInfoList = getResearchStaffSnapShot(conditionMap);

        if (researchStaffInfoList.size() <= 0 && isRequestPost())
            getAlertMessageManager().addMessageError("未找到符合条件的教研员");

        model.addAttribute("provinces", crmUserService.getAllProvince());
        model.addAttribute("conditionMap", conditionMap);
        model.addAttribute("researchStaffInfoList", researchStaffInfoList);

        return "crm/researchstaff/researchstafflist";
    }

    @RequestMapping(value = "researchstaffhomepage.vpage", method = RequestMethod.GET)
    public String rStaffHomepage(Model model) {

        Long researchStaffId = getRequestLong("researchStaffId", 0);
        Map<String, Object> researchStaffInfoMap = getResearchStaffInfoMap(researchStaffId);

        if (researchStaffInfoMap == null || researchStaffInfoMap.size() == 0) {
            getAlertMessageManager().addMessageError("ID为" + getRequestParameter("researchStaffId", "") + "的教研员不存在");
            return redirect("researchstafflist.vpage");
        }

        model.addAttribute("researchStaffInfoMap", researchStaffInfoMap);
        model.addAttribute("provinces", crmUserService.getAllProvince());
        model.addAttribute("recordTypeList", RecordType.toKeyValuePairs());

        return "crm/researchstaff/researchstaffhomepage";
    }

    /************************ 编辑相关 ******************************************************************/
    // 修改教研员姓名，密码,权限，是否可用

    /************************ 新建教研员 ******************************************************************/
    //
    //

    /**
     * ********************* private method ***************************************************************
     */
    private List<Map<String, Object>> getResearchStaffSnapShot(Map<String, Object> conditionMap) {
        List<User> users = new ArrayList<>();
        // 教研员id和地区数据的映射
        Map<Long, Integer> allRsRegionMap = new HashMap<>();
        Map<Long, String> rsSchoolMap = new HashMap<>();

        Object rstaffId = conditionMap.get("researchStaffId");
        if (rstaffId != null && !rstaffId.toString().equals("-1")) {
            // 根据id直接返回
            User user = userLoaderClient.loadUser(SafeConverter.toLong(rstaffId));
            if (user != null) {
                users.add(user);

                ExRegion exRegion = userLoaderClient.loadUserRegion(user);
                if (exRegion != null)
                    allRsRegionMap.put(user.getId(), exRegion.getCode());
            }
        }

        boolean withSchool = false;
        Integer regionCode = 0;

        Integer pCode = MapUtils.getInteger(conditionMap, "provCode");
        if (pCode != null && pCode != -1)
            regionCode = pCode;

        Integer cCode = MapUtils.getInteger(conditionMap, "cityCode");
        if (cCode != null && cCode != -1)
            regionCode = cCode;

        Integer cyCode = MapUtils.getInteger(conditionMap, "countyCode");
        if (cyCode != null && cyCode != -1) {
            regionCode = cyCode;
            withSchool = true;
        }

        // 处理教研员的地区信息
        Consumer<ResearchStaffManagedRegion> peekConsumer = t -> {
            if (t.getManagedRegionType() != RSManagedRegionType.SCHOOL) {
                allRsRegionMap.put(t.getRstaffId(), t.getManagedRegionCode().intValue());
            } else {
                Long schoolId = t.getManagedRegionCode();
                School school = raikouSystem.loadSchool(schoolId);

                if (school != null) {
                    allRsRegionMap.put(t.getRstaffId(), school.getRegionCode());
                    rsSchoolMap.put(t.getRstaffId(), school.getShortName());
                }
            }
        };

        List<Long> searchRegionUserIds = new ArrayList<>();
        if (regionCode != 0) {

            // 地区查询的教研员列表
            searchRegionUserIds = researchStaffLoaderClient.loadResearchStaffUnderRegions(regionCode)
                    .stream()
                    .peek(peekConsumer)
                    .map(ResearchStaffManagedRegion::getRstaffId)
                    .distinct() // 去重
                    .collect(Collectors.toList());
        }

        List<Long> searchSchoolUserIds = new ArrayList<>();
        if (withSchool) {
            List<Long> schoolIds = raikouSystem.querySchoolLocations(regionCode)
                    .asList()
                    .stream()
                    .map(School.Location::getId)
                    .distinct()
                    .collect(Collectors.toList());

            searchSchoolUserIds = researchStaffLoaderClient.loadResearchStaffUnderSchools(schoolIds)
                    .stream()
                    .peek(peekConsumer)
                    .map(ResearchStaffManagedRegion::getRstaffId)
                    .distinct()
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> researchStaffInfoList = new ArrayList<>();

        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        userIds.addAll(searchRegionUserIds);
        userIds.addAll(searchSchoolUserIds);

        Collection<ResearchStaffDetail> rstaffs = researchStaffLoaderClient.loadResearchStaffDetails(userIds).values();
        if (StringUtils.isNotBlank((String) conditionMap.get("subject"))) {
            Subject subject = Subject.safeParse((String) conditionMap.get("subject"));
            rstaffs = rstaffs.stream()
                    .filter(e -> e.getSubject() == subject)
                    .collect(Collectors.toList());
        }

        // 一次先查出来，省点儿力气
        // 教研员只有收货地址里面有录入手机的口，其它没有。所以不应该显示UserAuthentication里面的号
        // Map<Long,UserAuthentication> uaMap = userLoaderClient.loadUserAuthentications(userIds);
        Map<Long, UserShippingAddress> userAdressMap = userLoaderClient.loadUserShippingAddresses(userIds);
        Map<Long, UserIntegral> uiMap = integralLoaderClient.getIntegralLoader().loadResearchStaffIntegrals(userIds);

        // 转成输出
        rstaffs.forEach(u -> {
            Map<String, Object> it = new HashMap<>();

            UserShippingAddress address = userAdressMap.get(u.getId());
            String phone = "";
            if (address != null) {
                phone = sensitiveUserDataServiceClient.loadUserShippingAddressPhone(address.getId());
            }

            it.put("userId", u.getId());
            it.put("phone", phone);
            it.put("userName", u.fetchRealname());

            ExRegion userRegion = raikouSystem.loadRegion(allRsRegionMap.get(u.getId()));

            if (userRegion != null) {
                it.put("regionCode", SafeConverter.toString(userRegion.getCode()));
                it.put("regionName", userRegion.toString("/"));

                // 如果是学校区域的，则显示学校名称
                if (rsSchoolMap.containsKey(u.getId()))
                    it.put("regionName", rsSchoolMap.get(u.getId()));
            }

            it.put("disabled", u.getDisabled());
            it.put("subject", u.getSubject());

            /*int iCode = ConversionUtils.toInt(it.get("regionCode"));
            if (iCode != 0) {
                ExRegion region = regionServiceClient.getExRegionBuffer().loadRegion(iCode);
                it.put("regionName", (region == null) ? null : region.toString("/"));
            }*/

            Subject subject = Subject.safeParse(String.valueOf(it.get("subject")));
            it.put("subjectName", subject == null ? null : subject.getValue());

            long userId = ConversionUtils.toLong(it.get("userId"));
            UserIntegral userIntegral = uiMap.get(userId);
            it.put("goldCoin", (userIntegral == null) ? null : userIntegral.getUsable());

            researchStaffInfoList.add(it);
        });


        return researchStaffInfoList;
    }

    private Map<String, Object> getResearchStaffInfoMap(Long researchStaffId) {

        if (null == researchStaffId) return null;

        Map<String, Object> researchStaffInfoMap;

//        String query = "SELECT uu.ID as userId, uu.REALNAME as userName, uu.DATA_AUTHORITY_REGION as regionCode, " +
//                " uu.DISABLED as disabled, uu.REAL_CODE as realCode" +
//                " FROM UCT_USER uu WHERE uu.USER_TYPE = :userType AND uu.ID = :researchStaffId ";

//        Map<String, Object> queryParamMap = new HashMap<>();
//        queryParamMap.put("userType", UserType.RESEARCH_STAFF.getType());
//        queryParamMap.put("researchStaffId", researchStaffId);

        ResearchStaff staff = researchStaffLoaderClient.loadResearchStaff(researchStaffId); //刷新缓存
        if (staff == null) {
            return null;
        }

//        List<Map<String, Object>> researchStaffInfoMapList = utopiaSql.withSql(query).useParams(queryParamMap).queryAll();
//        if (CollectionUtils.isEmpty(researchStaffInfoMapList)) return null;

//        researchStaffInfoMap = researchStaffInfoMapList.get(0);
        researchStaffInfoMap = new HashMap<>();
        researchStaffInfoMap.put("userId", staff.getId());
        researchStaffInfoMap.put("userName", staff.fetchRealname());
        researchStaffInfoMap.put("regionCode", userLoaderClient.loadUserRegion(staff));
        researchStaffInfoMap.put("disabled", staff.getDisabled());
        researchStaffInfoMap.put("realCode", "");
        if (staff.isAffairTeacher()) {
            researchStaffInfoMap.put("affairTeacher", true);
            Long schoolId = MiscUtils.firstElement(staff.getManagedRegion().getSchoolIds());
            School school = raikouSystem.loadSchool(schoolId);
            researchStaffInfoMap.put("schoolId", school.getId());
            researchStaffInfoMap.put("schoolName", school.getShortName());
        }

        if (!StringUtils.isEmpty(ConversionUtils.toString(researchStaffInfoMap.get("regionCode")))) {
            ExRegion region = raikouSystem.loadRegion(NumberUtils.toInt(String.valueOf(researchStaffInfoMap.get("regionCode"))));
            researchStaffInfoMap.put("regionName", (region == null) ? null : region.toString("/"));
        }

        ResearchStaff rs = researchStaffLoaderClient.loadResearchStaff((Long) researchStaffInfoMap.get("userId"));
        Subject rsSubject = rs == null ? null : rs.getSubject();
        researchStaffInfoMap.put("subjectName", rsSubject == null ? null : rsSubject.getValue());

        UserIntegral userIntegral = integralLoaderClient.getIntegralLoader()
                .loadResearchStaffIntegral((Long) researchStaffInfoMap.get("userId"));
        researchStaffInfoMap.put("goldCoin", (userIntegral == null) ? null : userIntegral.getUsable());

        List<UserServiceRecord> customerServiceRecordList = userLoaderClient.loadUserServiceRecords(researchStaffId);
        researchStaffInfoMap.put("customerServiceRecordList", customerServiceRecordList);

        //查找被邀请人
//        String queryInvitee = "SELECT a.INVITE_ID as inviteeId, a.CREATETIME as acceptTime, " +
//                " a.DISABLED as success, b.REALNAME as inviteeName " +
//                " FROM VOX_INVITE_HISTORY as a, UCT_USER as b WHERE a.USER_ID = ? " +
//                " AND b.ID = a.INVITE_ID AND b.DISABLED = 0 ";
//        List<Map<String, Object>> inviteeList = utopiaSql.withSql(queryInvitee).useParamsArgs(researchStaffId).queryAll();
//        researchStaffInfoMap.put("inviteeList", inviteeList);
        List<InviteHistory> inviteHistories = asyncInvitationServiceClient.loadByInviter(researchStaffId).enabled().toList();
        List<Map<String, Object>> inviteeList = new ArrayList<>();
        for (InviteHistory e : inviteHistories) {
            User user = userLoaderClient.loadUser(e.getInviteeUserId());
            if (user == null) {
                continue;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("inviteeId", e.getInviteeUserId());
            m.put("acceptTime", e.getCreateTime());
            m.put("success", e.getDisabled());
            m.put("inviteeName", user.fetchRealname());
            inviteeList.add(m);
        }
        researchStaffInfoMap.put("inviteeList", inviteeList);

        return researchStaffInfoMap;
    }
}
