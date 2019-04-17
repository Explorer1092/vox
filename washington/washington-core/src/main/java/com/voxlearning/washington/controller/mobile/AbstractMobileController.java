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

package com.voxlearning.washington.controller.mobile;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.utopia.core.cdn.url2.config.CdnConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardBufferLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardBusinessType;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardCategory;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardItem;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardLog;
import com.voxlearning.utopia.service.parentreward.api.mapper.ParentRewardRecordWrapper;
import com.voxlearning.utopia.service.piclisten.api.PicListenCommonService;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.utopia.service.reward.constant.RewardProductPriceUnit;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
import com.voxlearning.utopia.service.userlevel.api.UserLevelLoader;
import com.voxlearning.utopia.service.userlevel.api.UserLevelService;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.service.parent.ParentSelfStudyPublicHelper;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.WashingtonRequestContext;

import javax.inject.Inject;
import java.util.*;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-12-5
 */
public class AbstractMobileController extends AbstractController {

    public static MapMessage noLoginResult = MapMessage.errorMessage().setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
    protected static MapMessage go2LoginPageResult = MapMessage.errorMessage().setErrorCode("666");
    @StorageClientLocation(storage = "homework")
    protected StorageClient storageClient;

    @Inject
    protected ParentSelfStudyPublicHelper parentSelfStudyPublicHelper;
    @ImportService(interfaceClass = PicListenCommonService.class)
    protected PicListenCommonService picListenCommonService;
    @Inject
    protected TextBookManagementLoaderClient textBookManagementLoaderClient;
    @Inject
    private ParentRewardBufferLoaderClient parentRewardBufferLoaderClient;
    @Inject
    private AsyncStudentServiceClient asyncStudentServiceClient;
    @Inject
    protected CommonConfigServiceClient commonConfigServiceClient;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;

    @ImportService(interfaceClass = UserLevelService.class)
    protected UserLevelService userLevelService;
    @ImportService(interfaceClass = UserLevelLoader.class)
    protected UserLevelLoader userLevelLoader;

    protected static MapMessage activityExpiryMsg = MapMessage.errorMessage("活动已过期，请关注其他活动，谢谢~");

    protected boolean studentUnLogin() {
        User user = currentUser();
        if (user == null || UserType.STUDENT != UserType.of(user.getUserType())) {
            return true;
        }
        return false;
    }

    /**
     * 为奖品中心获取当前的用户。会自动根据用户类型获取其相应的扩展。
     * 如果是老师帐户返回TeacherDetail
     * 如果是学生帐户返回StudentDetail
     * 如果是教研员帐户返回ResearchStaffDetail
     * TODO: 这个方法可以继续向基类移动，不过需要修改方法名
     */
    protected User currentRewardUser() {
        User user = currentUser();
        if (user == null) {
            return null;
        }
        switch (user.fetchUserType()) {
            case TEACHER: {
                if (user instanceof TeacherDetail) {
                    return user;
                }
                return currentTeacherDetail();
            }
            case STUDENT: {
                if (user instanceof StudentDetail) {
                    return user;
                }
                return currentStudentDetail();
            }
            case RESEARCH_STAFF: {
                if (user instanceof ResearchStaffDetail) {
                    return user;
                }
                return currentResearchStaffDetail();
            }
            default: {
                return null;
            }
        }
    }

    protected boolean studentIsParentChildren(Long parentId, Long studentId) {
        List<StudentParent> parents = parentLoaderClient.loadStudentParents(studentId);
        if (CollectionUtils.isEmpty(parents)) {
            return false;
        }

        return parents.stream().anyMatch(p -> p.getParentUser().getId().equals(parentId));
    }

    protected String fetchUnit(User user) {
        if (UserType.TEACHER.equals(user.fetchUserType())) {
            String unit = RewardProductPriceUnit.学豆.name();
            Teacher teacher = (Teacher) user;
            if (teacher.isPrimarySchool() || teacher.isInfantTeacher()) {
                unit = RewardProductPriceUnit.园丁豆.name();
            }
            return unit;
        } else if (UserType.STUDENT.equals(user.fetchUserType())) {
            return RewardProductPriceUnit.学豆.name();
        } else
            return RewardProductPriceUnit.学豆.name();
    }

    // 获取家长端的GPS信息 取不到返回(0,0)
    protected Map<String, String> getUserPosition() {
        String longitude = getRequestParameter("longitude", "0");       //用户GPS经度
        String latitude = getRequestParameter("latitude", "0");
        //如果没有传经纬度,则取学校的经纬度
        if (StringUtils.equals(longitude, "0") || StringUtils.equals(longitude, "0")) { // no user gps pos found
            // 获取家长的第一个孩子的学校
            School school = null;
            List<User> childList = studentLoaderClient.loadParentStudents(currentUserId());
            User child = MiscUtils.firstElement(childList);
            if (child != null) {
                school = asyncStudentServiceClient.getAsyncStudentService()
                        .loadStudentSchool(child.getId())
                        .getUninterruptibly();
            }
            if (school != null) {
                SchoolExtInfo extInfo = schoolExtServiceClient.getSchoolExtService()
                        .loadSchoolExtInfo(school.getId())
                        .getUninterruptibly();
                if (null != extInfo && StringUtils.isNotBlank(extInfo.getLatitude()) && StringUtils.isNotBlank(extInfo.getLongitude())) {
                    longitude = extInfo.getLongitude();
                    latitude = extInfo.getLatitude();
                }
            }
        }
        Map<String, String> positionMap = new HashMap<>();
        positionMap.put("longitude", longitude);
        positionMap.put("latitude", latitude);

        return positionMap;
    }

    /**
     * 获取用户名（统一为：XX爸爸；XX妈妈等）(取第一个产生绑定关系的孩子)
     */
    protected String generateUserName(User user) {
        String userName = "";
        if (user == null) {
            return userName;
        }
        if (UserType.PARENT == user.fetchUserType()) {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(user.getId());
            if (CollectionUtils.isEmpty(studentParentRefs)) {
                return userName;
            }
            StudentParentRef studentParentRef = studentParentRefs.stream()
                    .sorted((e1, e2) -> Long.compare(e2.getCreateTime().getTime(), e1.getCreateTime().getTime()))
                    .findFirst()
                    .orElse(null);
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentParentRef.getStudentId());
            userName = studentDetail.fetchRealname() + (CallName.其它监护人.name().equals(studentParentRef.getCallName()) ? "家长" : studentParentRef.getCallName());
        } else if (UserType.TEACHER == user.fetchUserType()) {
            Teacher teacher = teacherLoaderClient.loadTeacher(user.getId());
            String subjectName = teacher != null && teacher.getSubject() != null ? teacher.getSubject().getValue() : "";
            userName = subjectName + user.fetchRealname() + "老师";
        }
        return userName;
    }

    /**
     * 获取家长孩子信息(取第一个产生绑定关系的孩子)
     */
    protected Map<String, Object> generateParentFirstStudentShowInfo(Long parentId) {
        if (parentId == null) {
            return Collections.emptyMap();
        }
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
        if (CollectionUtils.isEmpty(studentParentRefs)) {
            return Collections.emptyMap();
        }
        StudentParentRef studentParentRef = studentParentRefs.stream()
                .sorted((e1, e2) -> Long.compare(e2.getCreateTime().getTime(), e1.getCreateTime().getTime()))
                .findFirst()
                .orElse(null);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentParentRef.getStudentId());
        String userName = studentDetail.fetchRealname() + (CallName.其它监护人.name().equals(studentParentRef.getCallName()) ? "家长" : studentParentRef.getCallName());
        Map<String, Object> data = new HashMap<>();
        data.put("showName", userName);
        String avatar = "";
        if (StringUtils.isNotBlank(studentDetail.fetchImageUrl())) {
            avatar = CdnConfig.getAvatarDomain().getValue() + "/gridfs/" + studentDetail.fetchImageUrl();
        }
        data.put("avatar", avatar);
        return data;
    }

    protected void addIntoMap(Map<String, Object> dataMap, String key, Object value) {
        if (value != null) {
            dataMap.put(key, value);
        }
    }

    protected boolean isParentCloseFairyland() {
        StudentDetail student = fetchStudent();
        if (student != null) {
            StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(student.getId());
            if (studentExtAttribute != null) {
                return studentExtAttribute.fairylandClosed() || studentExtAttribute.vapClosed();
            }
        }
        return false;
    }

    protected boolean isStudentInActivityBlacklist() {
        StudentDetail student = fetchStudent();
        if (null == student) return false;

        if (isParentCloseFairyland()) return true;

        if (userBlacklistServiceClient.isInBlackListByStudent(Collections.singletonList(student))
                .getOrDefault(student.getId(), false)) return true;

        String ACTIVITY_BLACKLIST = "activity_userIds_blacklist";
        try {
            String result = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), ACTIVITY_BLACKLIST);
            if (StringUtils.isBlank(result)) {
                return false;
            }
            String[] split = StringUtils.split(result, ",");
            List<String> strings = Arrays.asList(split);
            Long userId = currentUserId();
            if (userId != 0 && CollectionUtils.isNotEmpty(strings) && strings.contains(SafeConverter.toString(userId))) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    protected ParentRewardRecordWrapper convert(ParentRewardLog source, List<StudentParentRef> studentParentRefs, boolean showLevel) {
        ParentRewardRecordWrapper wrapper = new ParentRewardRecordWrapper();
        Long parentId = source.getParentId();
        if (parentId != null && parentId != 0) {
            StudentParentRef studentParentRef = studentParentRefs
                    .stream()
                    .filter(s -> Objects.equals(s.getParentId(), parentId))
                    .findFirst()
                    .orElse(null);
            if (studentParentRef != null) {
                String callName = CallName.其它监护人.name().equals(studentParentRef.getCallName()) ? "“其他”家长" : studentParentRef.getCallName();
                wrapper.setSendUser(callName);
            } else {
                wrapper.setSendUser("“其他”家长");
            }
        }

        //奖励发放时间
        if (source.getSendTime() != null) {
            wrapper.setSendTime(DateUtils.dateToString(source.getSendTime(), "yyyy-MM-dd HH:mm"));
        }
        //奖励产生时间
        wrapper.setGenerateTime(DateUtils.dateToString(source.getCreateTime(), "yyyy-MM-dd HH:mm"));
        ParentRewardItem item = parentRewardBufferLoaderClient.getParentRewardItem(source.getKey());
        if (item != null) {
            ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(item.getCategoryId());
            if (category != null) {
                wrapper.setRewardText(getParentRewardDescription(source, item, category, showLevel));
            }
        }
        wrapper.setStatus(source.getStatus());
        if (source.getStatus() != 0) {
            //奖励领取时间
            wrapper.setSrTime(DateUtils.dateToString(source.getUpdateTime(), "yyyy-MM-dd HH:mm"));
        }
        wrapper.setShowStone("FINISH_WONDERLAND_MISSION".equals(source.getKey()));
        wrapper.setType(source.getType());
        wrapper.setCount(source.getCount());
        return wrapper;
    }

    protected String getParentRewardTitle(ParentRewardLog rewardLog, ParentRewardItem item, ParentRewardCategory category, boolean showScoreLevel) {
        String title;
        ParentRewardBusinessType businessType = ParentRewardBusinessType.of(item.getBusiness());
        if ("QUALITY".equals(category.getKey()) || (businessType != null && ParentRewardBusinessType.VACATION_HOMEWORK == businessType)) {
            title = item.getDescription();
        } else {
            title = showScoreLevel ? item.getLevelTitle() : item.getTitle();
            Map<String, Object> ext = rewardLog.getExt();
            if (MapUtils.isNotEmpty(ext)) {
                for (Map.Entry<String, Object> entry : ext.entrySet()) {
                    title = title.replace("{" + entry.getKey() + "}", entry.getValue().toString());
                }
            }
        }
        return title;
    }

    protected String getParentRewardDescription(ParentRewardLog log, ParentRewardItem item, ParentRewardCategory category, Boolean showScoreLevel) {
        String description = item.getDescription();
        if ("HOMEWORK".equals(category.getKey()) && StringUtils.isNotEmpty(description)) {
            String[] descArr = description.split("_");
            if (descArr.length == 2) {
                if (showScoreLevel) {
                    return descArr[1];
                } else {
                    return log.realTitle(descArr[0]);
                }
            }
        }
        return description;
    }

    protected String getParentRewardRealBusinessName(ParentRewardLog log, ParentRewardItem item, ParentRewardCategory category, Boolean showScoreLevel) {
        String businessName = "";
        if ("HOMEWORK".equals(category.getKey())) {
            if (showScoreLevel && StringUtils.isNotBlank(item.getLevelTitle())) {
                businessName = log.realTitle(item.getLevelTitle());
            } else {
                businessName = log.realTitle(item.getTitle());
            }
        } else {
            ParentRewardBusinessType businessType = ParentRewardBusinessType.of(item.getBusiness());
            if (businessType != null) {
                businessName = businessType.getValue();
            }
        }
        return businessName;
    }

    protected StudentDetail fetchStudent() {
        User user = currentUser();
        if (user == null || (!user.isParent() && !user.isStudent())) return null;

        StudentDetail student;
        if (user.isStudent()) {
            student = user instanceof StudentDetail ? (StudentDetail) user : currentStudentDetail();
        } else {
            Long studentId = SafeConverter.toLong(getCookieManager().getCookie("sid", "0"), 0L);
            if (studentId == 0L) studentId = getRequestLong("sid");
            if (studentId == 0L) return null;
            student = studentLoaderClient.loadStudentDetail(studentId);
        }
        return student;
    }

    public void addCrossHeaderForXdomain() {
        WashingtonRequestContext context = getWebRequestContext();
        if (RuntimeModeLoader.getInstance().isProduction()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://x.17zuoye.com");
        } else if (RuntimeModeLoader.getInstance().isStaging()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://x.staging.17zuoye.net");
        } else if (RuntimeModeLoader.getInstance().isTest()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://x.test.17zuoye.net");
        }
        context.getResponse().addHeader("Access-Control-Allow-Methods", "GET");
        context.getResponse().addHeader("Access-Control-Allow-Headers", "x-requested-with");
        context.getResponse().addHeader("Access-Control-Max-Age", "1800");
        context.getResponse().addHeader("Access-Control-Allow-Credentials", "true");
    }
}
