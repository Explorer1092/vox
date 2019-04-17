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

package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.SchoolType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.dao.CrmSchoolClueDao;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.view.school.SchoolGradeBasicData;
import com.voxlearning.utopia.api.constant.CrmSchoolClueStatus;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.entity.crm.constants.SchoolOperationType;
import com.voxlearning.utopia.entity.crm.constants.UserPlatformType;
import com.voxlearning.utopia.entity.crm.schoolrecord.SchoolServiceRecord;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmSchoolExtInfoCheckServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolServiceRecordServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.consumer.DeprecatedSchoolServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/11/9
 */
@Named
public class SchoolClueService extends AbstractAgentService {
    public static final Date END_DATE = DateUtils.stringToDate("2016-9-1 23:59:59");
    public static final String REVIEWER_NAME = "【系统自动审核】";

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Deprecated
    @Inject private DeprecatedSchoolServiceClient deprecatedSchoolServiceClient;

    @Inject private CrmSchoolClueDao crmSchoolClueDao;
    @Inject private SchoolServiceRecordServiceClient schoolServiceRecordServiceClient;
    @Inject private CrmSchoolExtInfoCheckServiceClient crmSchoolExtInfoCheckServiceClient;
    @Inject private AgentNotifyService agentNotifyService;
    @Inject private BaseOrgService baseOrgService;
    @Inject private SchoolResourceService schoolResourceService;

    private final static Set<String> SCHOOL_EXT_INFO_UNSET_FILED = new HashSet<>();

    static {
        for (Integer grade : SchoolExtInfo.GRADE_LIST) {
            SCHOOL_EXT_INFO_UNSET_FILED.add(StringUtils.formatMessage("grade{}StudentCount", grade));
            SCHOOL_EXT_INFO_UNSET_FILED.add(StringUtils.formatMessage("newGrade{}ClassCount", grade));
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    //学校鉴定
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    // 新建学校信息
    ///////////////////////////////////////////////////////////////////////////
    public MapMessage addNewSchool(CrmSchoolClue newSchool) {
        if (newSchool.getCountyCode() == null) {
            return MapMessage.errorMessage("请选择学校所在地");
        }
        if (StringUtils.isBlank(newSchool.getCmainName())) {
            return MapMessage.errorMessage("学校全称不能为空");
        }
        if (newSchool.getSchoolPhase() != SchoolLevel.INFANT.getLevel() && newSchool.fetchEduSystem() == null) {
            return MapMessage.errorMessage("学校学制不能为空");
        }

        School school = new School();
        school.setCname(SafeConverter.toString(newSchool.loadSchoolFullName()).trim());
        school.setCmainName(SafeConverter.toString(newSchool.getCmainName()).trim());
        school.setSchoolDistrict(SafeConverter.toString(newSchool.getSchoolDistrict(), "").trim());
        school.setCode(ConversionUtils.toString(newSchool.getCountyCode()));
        school.setType(newSchool.getSchoolType());
        school.setLevel(newSchool.getSchoolPhase());
        school.setRegionCode(newSchool.getCountyCode());
        school.setAuthenticationState(0);
        school.setLogo("");
        school.setOld(false);
        school.setPayOpen(false);
        school.setShortName(StringUtils.trim(newSchool.getShortName()));
        school.setPayOpen(false);
        String operatorId = ConversionUtils.toString(newSchool.getRecorderId());
        MapMessage msg = deprecatedSchoolServiceClient.getRemoteReference().upsertSchool(school, operatorId);
        if (!msg.isSuccess()) {
            logger.warn("Failed create school schoolInfo={} ", newSchool);
            return MapMessage.errorMessage("创建学校失败");
        }
        Long schoolId = SafeConverter.toLong(msg.get("id"));
        school.setId(schoolId);
        newSchool.setSchoolId(schoolId);
        Boolean appraisalSchool = false;
        if (newSchool.checkSchoolLocationInfo()) {
            upsertSchoolClueBySchoolId(schoolId, newSchool.getLatitude(), newSchool.getLongitude(), newSchool.getRecorderId(), newSchool.getRecorderName(),
                    newSchool.getRecorderPhone(), newSchool.getPhotoUrl(), newSchool.getCoordinateType(), newSchool.getAddress());
            appraisalSchool = true;
        }
        SchoolServiceRecord record = new SchoolServiceRecord();
        record.setSchoolOperationType(SchoolOperationType.CREATE_NEW_SCHOOL);
        record.setOperationContent(createNewSchoolOperationContent(school, newSchool));
        initRecord(record, newSchool);
        record.setAdditions("天玑新建学校，自动更新到基础表中。");
        schoolServiceRecordServiceClient.addSchoolServiceRecord(record);
        touchSchoolExtInfo(newSchool);
        return MapMessage.successMessage().add("schoolId", schoolId).add("appraisalSchool", appraisalSchool);
    }


    private String createNewSchoolOperationContent(School school, CrmSchoolClue newSchool) {
        StringBuilder builder = new StringBuilder();
        if (school != null) {
            builder.append("cmainName:").append(school.getCmainName()).append(",");
            builder.append("schoolDistrict:").append(school.getSchoolDistrict()).append(",");
            builder.append("shortName:").append(school.getShortName()).append(",");
            builder.append("regionCode:").append(school.getRegionCode()).append(",");
            builder.append("authenticationState:").append(school.getAuthenticationState()).append(",");
            builder.append("authenticationSource:").append(school.getAuthenticationSource()).append(",");
        }
        if (newSchool.getGrade1StudentCount() != null) { // 一年级班级人数
            builder.append("grade1StudentCount:").append(newSchool.getGrade1StudentCount()).append(",");
        }
        if (newSchool.getGrade2StudentCount() != null) { // 二年级班级人数
            builder.append("grade2StudentCount:").append(newSchool.getGrade2StudentCount()).append(",");
        }
        if (newSchool.getGrade3StudentCount() != null) { // 三年级班级人数
            builder.append("grade3StudentCount:").append(newSchool.getGrade3StudentCount()).append(",");
        }
        if (newSchool.getGrade4StudentCount() != null) { // 四年级班级人数
            builder.append("grade4StudentCount:").append(newSchool.getGrade4StudentCount()).append(",");
        }
        if (newSchool.getGrade5StudentCount() != null) { // 五年级班级人数
            builder.append("grade5StudentCount:").append(newSchool.getGrade5StudentCount()).append(",");
        }
        if (newSchool.getGrade6StudentCount() != null) { // 六年级班级人数
            builder.append("grade6StudentCount:").append(newSchool.getGrade6StudentCount()).append(",");
        }
        if (newSchool.getGrade7StudentCount() != null) { // 七年级班级人数
            builder.append("grade7StudentCount:").append(newSchool.getGrade7StudentCount()).append(",");
        }
        if (newSchool.getGrade8StudentCount() != null) { // 八年级班级人数
            builder.append("grade8StudentCount:").append(newSchool.getGrade8StudentCount()).append(",");
        }
        if (newSchool.getGrade9StudentCount() != null) { // 九年级班级人数
            builder.append("grade9StudentCount:").append(newSchool.getGrade9StudentCount()).append(",");
        }

        if (newSchool.getGrade11StudentCount() != null) { // 高一班级人数
            builder.append("grade11StudentCount:").append(newSchool.getGrade11StudentCount()).append(",");
        }
        if (newSchool.getGrade12StudentCount() != null) { // 高二班级人数
            builder.append("grade12StudentCount:").append(newSchool.getGrade12StudentCount()).append(",");
        }
        if (newSchool.getGrade13StudentCount() != null) { // 高三班级人数
            builder.append("grade13StudentCount:").append(newSchool.getGrade13StudentCount()).append(",");
        }

        if (newSchool.getNewGrade1ClassCount() != null) { // 一年级班级数
            builder.append("newGrade1ClassCount:").append(newSchool.getNewGrade1ClassCount()).append(",");
        }
        if (newSchool.getNewGrade2ClassCount() != null) { // 二年级班级数
            builder.append("newGrade2ClassCount:").append(newSchool.getNewGrade2ClassCount()).append(",");
        }
        if (newSchool.getNewGrade3ClassCount() != null) { // 三年级班级数
            builder.append("newGrade3ClassCount:").append(newSchool.getNewGrade3ClassCount()).append(",");
        }
        if (newSchool.getNewGrade4ClassCount() != null) { // 四年级班级数
            builder.append("newGrade4ClassCount:").append(newSchool.getNewGrade4ClassCount()).append(",");
        }
        if (newSchool.getNewGrade5ClassCount() != null) { // 五年级班级数
            builder.append("newGrade5ClassCount:").append(newSchool.getNewGrade5ClassCount()).append(",");
        }
        if (newSchool.getNewGrade6ClassCount() != null) { // 六年级班级数
            builder.append("newGrade6ClassCount:").append(newSchool.getNewGrade6ClassCount()).append(",");
        }
        if (newSchool.getNewGrade7ClassCount() != null) { // 七年级班级数
            builder.append("newGrade7ClassCount:").append(newSchool.getNewGrade7ClassCount()).append(",");
        }
        if (newSchool.getNewGrade8ClassCount() != null) { // 八年级班级数
            builder.append("newGrade8ClassCount:").append(newSchool.getNewGrade8ClassCount()).append(",");
        }
        if (newSchool.getNewGrade9ClassCount() != null) {  // 九年级班级数
            builder.append("newGrade9ClassCount:").append(newSchool.getNewGrade9ClassCount()).append(",");
        }

        if (newSchool.getNewGrade11ClassCount() != null) { // 高一班级数
            builder.append("newGrade11ClassCount:").append(newSchool.getNewGrade11ClassCount()).append(",");
        }
        if (newSchool.getNewGrade12ClassCount() != null) { // 高二班级数
            builder.append("newGrade12ClassCount:").append(newSchool.getNewGrade12ClassCount()).append(",");
        }
        if (newSchool.getNewGrade13ClassCount() != null) { // 高三班级数
            builder.append("newGrade10ClassCount:").append(newSchool.getNewGrade13ClassCount()).append(",");
        }
        if (CollectionUtils.isNotEmpty(newSchool.getInfantGrade())) {
            builder.append("infantGrade:").append(newSchool.getInfantGrade()).append(",");
        }
        if (newSchool.getEduSystem() != null) {
            builder.append("eduSystem:").append(newSchool.getEduSystem()).append(",");
        }
        if (newSchool.getEnglishStartGrade() != null) {
            builder.append("englishStartGrade:").append(newSchool.getEnglishStartGrade()).append(",");
        }
        if (newSchool.getSchoolSize() != null) {
            builder.append("schoolSize:").append(newSchool.getSchoolSize()).append(",");
        }
        if (CollectionUtils.isNotEmpty(newSchool.getBranchSchoolIds())) {
            builder.append("branchSchoolIds:").append(newSchool.getBranchSchoolIds()).append("。");
        }
        return builder.toString();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 编辑学校信息
    ///////////////////////////////////////////////////////////////////////////
    public MapMessage updateSchool(CrmSchoolClue updateSchool, List<SchoolGradeBasicData> gradeDataList) {
        return updateSchool(updateSchool, false, gradeDataList);
    }

    public MapMessage updateSchool(CrmSchoolClue updateSchool, Boolean confirm, List<SchoolGradeBasicData> gradeDataList) {
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(updateSchool.getSchoolId()).getUninterruptibly();
        EduSystemType eduSystemType = updateSchool.fetchEduSystem();
        if (eduSystemType == null) {
            return MapMessage.errorMessage("新学制不能为空");
        }
        String oldEduSystem = "";
        MapMessage msg = MapMessage.successMessage();
        // before  处理校验学制更改是否合理
        if (schoolExtInfo != null) {
            msg = crmSchoolExtInfoCheckServiceClient.beforeUpdateSchoolExtInfoEduSystem(updateSchool.getSchoolId(), updateSchool.fetchEduSystem());
            if (!confirm && !msg.isSuccess()) {
                return msg;
            } else if (msg.get("needUpdate") != null) {
                oldEduSystem = (String) msg.get("oldEduSystem");
            }
        }
        Boolean appraisalSchool = false;
        if (updateSchool.checkSchoolLocationInfo()) {
            if (schoolExtInfo == null || !Objects.equals(schoolExtInfo.getPhotoUrl(), updateSchool.getPhotoUrl())) {
                msg = upsertSchoolClueBySchoolId(updateSchool.getSchoolId(), updateSchool.getLatitude(), updateSchool.getLongitude(), updateSchool.getRecorderId(), updateSchool.getRecorderName(),
                        updateSchool.getRecorderPhone(), updateSchool.getPhotoUrl(), updateSchool.getCoordinateType(), updateSchool.getAddress());
                if (!msg.isSuccess()) {
                    return msg;
                }
                School school = raikouSystem.loadSchool(updateSchool.getSchoolId());
                if (school.getSchoolAuthenticationState() == AuthenticationState.WAITING) {
                    appraisalSchool = true;
                }
            }
        }
        SchoolServiceRecord record = new SchoolServiceRecord();
        record.setSchoolOperationType(SchoolOperationType.UPDATE_SCHOOL_INFO_NEW);
        initRecord(record, updateSchool);
        record.setOperationContent(createUpdateSchoolOperationContent(updateSchool));
        record.setAdditions("天玑学校信息更新，自动更新到基础表中。");
        record = schoolServiceRecordServiceClient.addSchoolServiceRecord(record);
        boolean isUpdateDiffOldSchoolInfo = isUpdateDiffOldSchoolInfo(updateSchool);
        touchSchoolExtInfo(updateSchool);
        // 更新学校年级基础信息
        schoolResourceService.updateSchoolGradeData(updateSchool.getSchoolId(), gradeDataList);
        if (isUpdateDiffOldSchoolInfo) {
            sendMessage(updateSchool.getCmainName(), updateSchool.getSchoolId(), updateSchool.getRecorderName(), record.getId());
        }
        if (msg.get("needUpdate") != null) {
            crmSchoolExtInfoCheckServiceClient.afterUpdateSchoolExtInfoEduSystem(updateSchool.getSchoolId(), oldEduSystem, eduSystemType, "天玑市场专员修改", "agent:" + getCurrentUser().getRealName() + "(" + getCurrentUser().getUserId() + ")", msg.get("needEmail") != null);
        }
        return MapMessage.successMessage().add("appraisalSchool", appraisalSchool);
    }

    private void sendMessage(String schoolName, Long schoolId, String operatorName, String recordId) {
        String content = StringUtils.formatMessage("您负责的学校：“{}（{}）”,基础信息被{}进行了修改。", schoolName, schoolId, operatorName);
        List<AgentUser> schoolManager = baseOrgService.getSchoolManager(schoolId);
        AgentUser schoolChargePerson = null;
        if (CollectionUtils.isNotEmpty(schoolManager)) {
            schoolChargePerson = schoolManager.get(0);
        }
        if (null != schoolChargePerson && !Objects.equals(getCurrentUserId(), schoolChargePerson.getId())) {
            agentNotifyService.sendNotify(AgentNotifyType.SCHOOL_INFO_MODIFY.getType(), "学校基础信息变更", content,
                    Collections.singleton(schoolChargePerson.getId()), "/mobile/school_clue/school_info_diff.vpage?recordId=" + recordId);
        }
    }

    public void addConfirmSchoolInfoLog(CrmSchoolClue updateSchool, SchoolOperationType operationType) {
        SchoolServiceRecord record = new SchoolServiceRecord();
        record.setSchoolOperationType(operationType);
        initRecord(record, updateSchool);
        record.setOperationContent("确认学校信息");
        schoolServiceRecordServiceClient.addSchoolServiceRecord(record);
    }

    private Map<String, Object> toMapFromObjectByProperties(Object object, String[] propertyNames) {
        Map<String, Object> dataMap = new HashMap<>();
        if (null != object) {
            if (propertyNames.length > 0) {
                for (int i = 0; i < propertyNames.length; i++) {
                    String item = propertyNames[i];
                    Object data = getDataByProperty(object, item);
                    if (null == data) {
                        continue;
                    }
                    dataMap.put(item, data);
                }
            }
        }
        return dataMap;
    }

    private Object getDataByProperty(Object object, String propertyName) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, object.getClass());
            Method getMethod = pd.getReadMethod();//获得get方法
            Object data = getMethod.invoke(object);//执行get方法返回一个Object
            return data;
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException ignored) {

        }
        return null;
    }


    /**
     * 新老数据是否相同
     *
     * @param updateSchool
     * @return
     */
    private boolean isUpdateDiffOldSchoolInfo(CrmSchoolClue updateSchool) {
        return false;
    }


    private String createUpdateSchoolOperationContent(CrmSchoolClue target) {
        SchoolExtInfo source = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(target.getSchoolId()).getUninterruptibly();
        if (null != source && null == source.getEduSystem()) {
            School school = raikouSystem.loadSchool(target.getSchoolId());
            String eduSystem = schoolExtServiceClient.getSchoolExtService().getSchoolEduSystem(school).getUninterruptibly();
            source.setEduSystem(eduSystem);
        }
        String[] properties = {
                "schoolingLength",
                "englishStartGrade",
                "eduSystem",
                "branchSchoolIds",
                "recorderId",
                "recorderName"

        };
        Map<String, Map<String, Object>> contentMap = new HashMap<>();
        Map<String, Object> oldDataMap = toMapFromObjectByProperties(source, properties);
        Map<String, Object> newDataMap = toMapFromObjectByProperties(target, properties);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateString = formatter.format(new Date());
        newDataMap.put("updateTime", dateString);
        contentMap.put("newData", newDataMap);
        contentMap.put("oldData", oldDataMap);
        return JsonUtils.toJson(contentMap);
    }

    // 创建学校操作记录的记录内容
    private void initRecord(SchoolServiceRecord record, CrmSchoolClue clue) {
        record.setSchoolId(clue.getSchoolId());
        record.setSchoolName(clue.loadSchoolFullName());
        record.setOperatorId(SafeConverter.toString(clue.getRecorderId()));
        record.setOperatorName(clue.getRecorderName());
        record.setUserPlatformType(UserPlatformType.AGENT);
    }
    //---------------------------------------------- 添加学校----------------------------------------------------------

    private SchoolExtInfo touchSchoolExtInfo(CrmSchoolClue schoolBasicInfo) {
        Long schoolId = schoolBasicInfo.getSchoolId();
        if (schoolId == null) {
            return null;
        }
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        boolean newRecord = false;
        if (schoolExtInfo == null) {
            schoolExtInfo = new SchoolExtInfo();
            schoolExtInfo.setId(schoolId);
            newRecord = true;
        }
//        BeanUtils.copyProperties(schoolBasicInfo, schoolExtInfo, "id", "walkingClazzs", "photoUrl", "latitude", "longitude", "coordinateType", "address");

        schoolExtInfo.setBranchSchoolIds(schoolBasicInfo.getBranchSchoolIds());
        schoolExtInfo.setEduSystem(schoolBasicInfo.getEduSystem());
        schoolExtInfo.setEnglishStartGrade(schoolBasicInfo.getEnglishStartGrade());

        if (Objects.equals(schoolBasicInfo.getSchoolPhase(), SchoolLevel.INFANT.getLevel())) {
            schoolExtInfo.setEduSystem(EduSystemType.I4.name());
        }
        //走读方式
        if (schoolBasicInfo.getExternOrBoarder() != null) {
            schoolExtInfo.setExternOrBoarder(schoolBasicInfo.getExternOrBoarder());
        }
//        if (newRecord) {
        schoolExtServiceClient.getSchoolExtService()
                .upsertSchoolExtInfo(schoolExtInfo)
                .awaitUninterruptibly();
//        } else {
//            schoolExtInfo.setId(schoolId);
//            schoolExtServiceClient.getSchoolExtService()
//                    .replaceSchoolExtInfoById(schoolExtInfo, SCHOOL_EXT_INFO_UNSET_FILED)
//                    .awaitUninterruptibly();
//        }
        return schoolExtInfo;
    }

    // ----------------------------------------------添加或更新学校基础信息END -----------------------------------------

    //--------------------------------------------- 字典表导出扩展字段支持---------------------------------------------
    public Map<Long, CrmSchoolClue> findSchoolIdIs(Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyMap();
        }
        Map<Long, List<CrmSchoolClue>> schoolClueMap = crmSchoolClueDao.findSchoolIdIs(schoolIds);
        Map<Long, CrmSchoolClue> result = new HashMap<>();
        schoolIds.forEach(p -> {
            if (!schoolClueMap.containsKey(p)) {
                return;
            }
            List<CrmSchoolClue> schoolClues = schoolClueMap.get(p);
            if (CollectionUtils.isEmpty(schoolClues)) {
                return;
            }
            schoolClues = schoolClues.stream().sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime())).collect(Collectors.toList());
            result.put(p, schoolClues.get(0));
        });
        return result;
    }

    //--------------------------------------------- 字典表导出扩展字段支持END -----------------------------------------

    public CrmSchoolClue load(String id) {
        return id == null ? null : crmSchoolClueDao.load(id);
    }

    public CrmSchoolClue load(Long schoolId) {
        if (schoolId == null) {
            return null;
        }
        List<CrmSchoolClue> schoolClues = crmSchoolClueDao.findSchoolIdIs(schoolId);
        return CollectionUtils.isEmpty(schoolClues) ? null : schoolClues.get(0);
    }

    public List<CrmSchoolClue> loads(Long schoolId) {
        return crmSchoolClueDao.findSchoolIdIs(schoolId);
    }

    public Map<String, List<CrmSchoolClue>> userSchoolClues(Long userId) {
        Map<String, List<CrmSchoolClue>> userSchoolClues = new HashMap<>();
        for (CrmSchoolClueStatus status : CrmSchoolClueStatus.values()) {
            userSchoolClues.put(status.name(), new ArrayList<>());
        }
        if (userId == null) {
            return userSchoolClues;
        }
        List<CrmSchoolClue> schoolClues = crmSchoolClueDao.findRecorderIdIs(userId);
        if (CollectionUtils.isEmpty(schoolClues)) {
            return userSchoolClues;
        }

        Collections.sort(schoolClues, (o1, o2) -> Long.compare(o2.getUpdateTime().getTime(), o1.getUpdateTime().getTime()));

        for (CrmSchoolClue schoolClue : schoolClues) {
            schoolClue.setShowPhase(SchoolLevel.safeParse(schoolClue.getSchoolPhase()));
            schoolClue.setShowType(SchoolType.safeParse(schoolClue.getSchoolType()));
            CrmSchoolClueStatus status = CrmSchoolClueStatus.codeOf(schoolClue.getStatus());
            schoolClue.setShowStatus(status);
            if (status != null && schoolClue.getAuthenticateType() != null && schoolClue.getAuthenticateType() == 5) {
                userSchoolClues.get(status.name()).add(schoolClue);
            }
        }
        return userSchoolClues;
    }

    public List<CrmSchoolClue> getSchoolClueByTime(Date startTime, Date endTime) {
        List<CrmSchoolClue> schoolClueByDate = crmSchoolClueDao.findSchoolClueByDate(startTime, endTime);
        //过滤掉学校ID不存在的学校线索
        schoolClueByDate = schoolClueByDate.stream().filter(p -> StringUtils.isNotBlank(ConversionUtils.toString(p.getSchoolId()))).collect(Collectors.toList());
        //过滤掉学校ID重复的学校
        Set<Long> schoolIds = new HashSet<>();
        List<CrmSchoolClue> resultSchoolClue = new ArrayList<>();
        schoolClueByDate.forEach(p -> {
            if (p.getSchoolId() == null) {
                return;
            }
            if (p.getSchoolName() == null) {
                return;
            }
            if (schoolIds.contains(p.getSchoolId())) {
                return;
            }
            resultSchoolClue.add(p);
        });
        return resultSchoolClue;
    }

    // -------------------------------------------------进校签到功能----------------------------------------------------


    private MapMessage addSchoolClue(CrmSchoolClue schoolClue) {
        if (schoolClue == null) {
            return MapMessage.errorMessage("学校审核申请创建失败");
        }
        try {
            String id = crmSchoolClueDao.insert(schoolClue);
            if (StringUtils.isBlank(id)) {
                return MapMessage.errorMessage("学校审核申请添加失败");
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("add school clue is failed", ex);
            return MapMessage.errorMessage("学校审核申请添加出现错误");
        }
    }

    public MapMessage upsertSchoolClueBySchoolId(Long schoolId, String latitude, String longitude, Long userId, String realName, String phone, String photoUrl, String coordinateType, String address) {
        if (StringUtils.isBlank(latitude) || StringUtils.isBlank(longitude) || StringUtils.isBlank(photoUrl)
                || StringUtils.isBlank(coordinateType) || StringUtils.isBlank(address) || userId == null || realName == null || phone == null) {
            return MapMessage.errorMessage("学校线索信息不全");
        }
        School school = raikouSystem.loadSchool(schoolId);
        if (school == null) {
            return MapMessage.errorMessage("学校信息未找到");
        }
        CrmSchoolClue newClue = new CrmSchoolClue();
        newClue.setSchoolId(school.getId());
        newClue.setCmainName(school.getCmainName());
        newClue.setSchoolDistrict(school.getSchoolDistrict());
        newClue.setRecorderId(userId);
        newClue.setRecorderName(realName);
        newClue.setRecorderPhone(phone);
        newClue.setLatitude(latitude);
        newClue.setLongitude(longitude);
        newClue.setPhotoUrl(photoUrl);
        newClue.setCoordinateType(coordinateType);
        newClue.setAddress(address);
        newClue.markLocationClue();
        return addSchoolClue(newClue);
    }
}
