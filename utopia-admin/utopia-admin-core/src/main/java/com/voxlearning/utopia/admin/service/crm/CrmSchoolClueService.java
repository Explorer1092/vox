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

package com.voxlearning.utopia.admin.service.crm;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.dao.CrmSchoolClueDao;
import com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext;
import com.voxlearning.utopia.admin.persist.entity.AdminLog;
import com.voxlearning.utopia.admin.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.CrmSchoolClueStatus;
import com.voxlearning.utopia.entity.crm.CrmSchoolClue;
import com.voxlearning.utopia.entity.crm.constants.SchoolOperationType;
import com.voxlearning.utopia.entity.crm.constants.UserPlatformType;
import com.voxlearning.utopia.entity.crm.schoolrecord.SchoolServiceRecord;
import com.voxlearning.utopia.service.crm.client.AdminLogServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolServiceRecordServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import lombok.Cleanup;
import org.springframework.beans.BeanUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/11/9
 */
@Named
public class CrmSchoolClueService extends AbstractAdminService {
    private static final Date END_DATE = DateUtils.stringToDate("2016-9-1 23:59:59"); //FIXME : 时间要修改@王亚光

    @Inject private RaikouSystem raikouSystem;
    @Inject private AdminLogServiceClient adminLogServiceClient;
    @Inject private CrmSchoolClueDao crmSchoolClueDao;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject private SchoolServiceRecordServiceClient schoolServiceRecordServiceClient;
    @Inject private AgentCommandQueueProducer agentCommandQueueProducer;
    @Inject private SchoolServiceClient schoolServiceClient;

    public MapMessage reviewSchoolClue(String id, Long time, CrmSchoolClueStatus reviewStatus, String reviewNote, AuthCurrentAdminUser adminUser, String longitude, String latitude, String address) {
        if (StringUtils.isBlank(id) || reviewStatus == null || time == null) {
            return MapMessage.errorMessage("请求参数有误");
        }
        if (CrmSchoolClueStatus.已驳回 == reviewStatus && StringUtils.isBlank(reviewNote)) {
            return MapMessage.errorMessage("学校审核记录不能为空");
        }
        CrmSchoolClue schoolClue = load(id);
        if (schoolClue == null) {
            return MapMessage.errorMessage("学校线索记录不存在");
        }

        if (!isTimeEqual(time, schoolClue.getUpdateTime())) {
            return MapMessage.errorMessage("学校线索已被修改，请刷新页面后再操作");
        }
        String actionUrl = ((AdminHttpRequestContext) HttpRequestContextUtils.currentRequestContext()).getRelativeUriPath();
        schoolClue.setReviewer(adminUser.getAdminUserName());
        schoolClue.setReviewerName(adminUser.getRealName());
        schoolClue.setReviewNote(reviewNote);
        schoolClue.setReviewTime(new Date());
        if (schoolClue.getAuthenticateType() != null && CrmSchoolClueStatus.已通过 == reviewStatus) {
            schoolClue.setStatus(reviewStatus.getCode());
            if (StringUtils.isNotBlank(longitude) && StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(address)) {
                schoolClue.setLongitude(longitude);
                schoolClue.setLatitude(latitude);
                schoolClue.setAddress(address);
                schoolClue.setCoordinateType("autonavi");
            }
            School school = raikouSystem.loadSchool(schoolClue.getSchoolId());
            SchoolServiceRecord record = new SchoolServiceRecord();
            if (school.getSchoolAuthenticationState() == AuthenticationState.WAITING) {
                school.setAuthenticationState(1);
                //更新学校的状态
                schoolServiceClient.getSchoolService().updateSchool(school);
                record.setSchoolOperationType(SchoolOperationType.AUTHENTICATE_SCHOOL);
            } else {
                record.setSchoolOperationType(SchoolOperationType.MODIFICATION_SCHOOL_LOCATION);
            }
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(school.getId())
                    .getUninterruptibly();
            // 更新schoolExtInfo 中的位置信息的值
            touchSchoolExtInfo(schoolClue, adminUser, actionUrl);
            // 记SchoolServiceRecord
            record.setSchoolId(school.getId());
            record.setSchoolName(school.loadSchoolFullName());
            record.setOperatorId(SafeConverter.toString(schoolClue.getRecorderId()));
            record.setOperatorName(schoolClue.getRecorderName());
            record.setUserPlatformType(UserPlatformType.AGENT);
            record.setOperationContent(schoolExtInfoTouchLog(schoolExtInfo, schoolClue));
            record.setAdditions("学校线索通过审核，同步修改关联的学校扩展信息");
            schoolServiceRecordServiceClient.addSchoolServiceRecord(record);
        }
        if (schoolClue.getAuthenticateType() != null && CrmSchoolClueStatus.已驳回 == reviewStatus) {
            schoolClue.setStatus(reviewStatus.getCode());
        }
        // 更新审核记录的状态
        crmSchoolClueDao.update(schoolClue.getId(), schoolClue);
        // 发送消息到Agent
        Map<String, Object> command = new HashMap<>();
        command.put("command", "crm_review_school_clue");
        command.put("schoolId", schoolClue.getSchoolId());
        command.put("schoolName", schoolClue.loadSchoolFullName());
        command.put("reviewStatus", reviewStatus.getCode());
        command.put("reviewerName", schoolClue.getReviewerName());
        command.put("reviewNote", StringUtils.isBlank(schoolClue.getReviewNote()) ? "" : schoolClue.getReviewNote());
        command.put("receiverId", schoolClue.getRecorderId());
        Message message = Message.newMessage();
        message.withPlainTextBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);
        return MapMessage.successMessage();
    }

    private boolean isTimeEqual(long timestamp, Date targetTime) {
        return targetTime != null && timestamp == targetTime.getTime();
    }

    public CrmSchoolClue load(String id) {
        return id == null ? null : crmSchoolClueDao.load(id);
    }

    public List<CrmSchoolClue> loadSchoolByIdIncludeDisabled(Long schoolId) {
        if (schoolId == null) {
            return Collections.emptyList();
        }
        return crmSchoolClueDao.findSchoolIdIncludeDisabled(schoolId);
    }

    public List<CrmSchoolClue> loadLocationSchoolClues(String schoolKey, Date updateStartTime, Date updateEndTime, Integer checkStatus) {
        List<Integer> status = new ArrayList<>();
        if (checkStatus == 2) {
            status.add(-1);
            status.add(2);
        }
        if (checkStatus == 1) {
            status.add(1);
        }
        return crmSchoolClueDao.findLocationSchoolClues(schoolKey, updateStartTime, updateEndTime, status);
    }

    public List<CrmSchoolClue> loadAuthSchoolClues(Integer status, String schoolName, String provinceName, String cityName, String recorderName, Date createStart, Date createEnd, String reviewerName) {
        return crmSchoolClueDao.findAuthClues(status, schoolName, provinceName, cityName, recorderName, createStart, createEnd, reviewerName).stream().filter(p -> !Boolean.TRUE.equals(p.getDisabled())).collect(Collectors.toList());
    }

    public List<CrmSchoolClue> loadInfoSchoolClues(Integer status, String schoolName, String provinceName, String cityName, String recorderName, Date createStart, Date createEnd, String reviewerName) {
        return crmSchoolClueDao.findInfoClues(status, schoolName, provinceName, cityName, recorderName, createStart, createEnd, reviewerName).stream().filter(p -> !Boolean.TRUE.equals(p.getDisabled())).collect(Collectors.toList());
    }

    public List<CrmSchoolClue> loadCriticalSchoolClues(Integer status, String schoolName, String provinceName, String cityName, String recorderName, Date createStart, Date createEnd, String reviewerName) {
        return crmSchoolClueDao.findCriticalClues(status, schoolName, provinceName, cityName, recorderName, createStart, createEnd, reviewerName).stream().filter(p -> !Boolean.TRUE.equals(p.getDisabled())).collect(Collectors.toList());
    }

    public List<CrmSchoolClue> loadSignInSchoolClues(Integer status, String schoolName, String provinceName, String cityName, String recorderName, Date createStart, Date createEnd, String reviewerName) {
        return crmSchoolClueDao.findSignInClues(status, schoolName, provinceName, cityName, recorderName, createStart, createEnd, reviewerName).stream().filter(p -> !Boolean.TRUE.equals(p.getDisabled())).collect(Collectors.toList());
    }


    public CrmSchoolClue updateBaseClue(String id, Integer regionCode, String cmainName, String schoolDistrct, String address,
                                        Integer schoolPhase) {
        CrmSchoolClue schoolClue = load(id);
        if (schoolClue == null) {
            return null;
        }

        schoolClue.setCountyCode(regionCode);
        ExRegion region = raikouSystem.loadRegion(regionCode);
        if (region != null) {
            schoolClue.setCountyName(region.getCountyName());
            schoolClue.setCityCode(region.getCityCode());
            schoolClue.setCityName(region.getCityName());
            schoolClue.setProvinceCode(region.getProvinceCode());
            schoolClue.setProvinceName(region.getProvinceName());
        }
        // 跟 SchoolSeriveImpl.upsertSchool 一致, 不是很必要的样子
        if (StringUtils.isNotBlank(cmainName)) {
            if (StringUtils.isNotBlank(schoolDistrct)) {
                schoolClue.setSchoolName(cmainName + "(" + schoolDistrct + ")");
            } else {
                schoolClue.setSchoolName(cmainName);
            }
        }
        schoolClue.setCmainName(cmainName);
        schoolClue.setSchoolDistrict(schoolDistrct == null ? "" : schoolDistrct);
        schoolClue.setSchoolPhase(schoolPhase);
        schoolClue.setAddress(address);
        crmSchoolClueDao.update(id, schoolClue);
        return schoolClue;
    }

    private SchoolExtInfo touchSchoolExtInfo(CrmSchoolClue schoolClue, AuthCurrentAdminUser adminUser, String actionUrl) {
        Long schoolId = schoolClue.getSchoolId();
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
        SchoolExtInfo source = new SchoolExtInfo();
        BeanUtils.copyProperties(schoolExtInfo, source);
        BeanUtils.copyProperties(schoolClue, schoolExtInfo, "id", "walkingClazzs", "schoolSize");
        /*if (!Objects.equals(1, schoolClue.getAuthenticateType())) {
            schoolExtInfo.setSchoolSize(countSchoolSize(schoolClue));
        }
        List<AgentDictSchool> dictSchools = agentDictSchoolPersistence.findBySchoolId(schoolId);
        // 类型为紧急创建学校的学校线索审核不改变学校的信息 3 类型为照片签到的学校线索不影响锁状态
        if (CollectionUtils.isNotEmpty(dictSchools) && schoolClue.getCreateTime().after(END_DATE) && !Objects.equals(schoolClue.getAuthenticateType(), 4) && !Objects.equals(schoolClue.getAuthenticateType(), 3)) {
            schoolExtInfo.setLocked(true);
        }*/
        if (newRecord) {
            schoolExtServiceClient.getSchoolExtService()
                    .upsertSchoolExtInfo(schoolExtInfo)
                    .awaitUninterruptibly();
        } else {
            schoolExtInfo.setId(schoolId);
            schoolExtServiceClient.getSchoolExtService()
                    .upsertSchoolExtInfo(schoolExtInfo)
                    .awaitUninterruptibly();
        }
        schoolExtInfoTouchLog(source, schoolExtInfo, adminUser, actionUrl);
        return schoolExtInfo;
    }

    private void schoolTouchLog(School source, School target, AuthCurrentAdminUser adminUser, String actionUrl) {
        StringBuilder builder = new StringBuilder();
//        if (!target.getCname().equals(source.getCname())) {
//            builder.append("cname:").append(source.getCname()).append("->").append(target.getCname()).append("; ");
//        }
        if (!Objects.equals(target.getCmainName(), source.getCmainName())) {
            builder.append("cmainName:").append(source.getCmainName()).append("->").append(target.getCmainName()).append("; ");
        }
        if (!Objects.equals(target.getSchoolDistrict(), source.getSchoolDistrict())) {
            builder.append("schoolDistrict:").append(source.getSchoolDistrict()).append("->").append(target.getSchoolDistrict()).append("; ");
        }
        if (!Objects.equals(target.getShortName(), source.getShortName())) {
            builder.append("shortName:").append(source.getShortName()).append("->").append(target.getShortName()).append("; ");
        }
        if (!Objects.equals(target.getRegionCode(), source.getRegionCode())) {
            builder.append("regionCode:").append(source.getRegionCode()).append("->").append(target.getRegionCode()).append("; ");
        }
       /* if (!Objects.equals(target.getLevel(), source.getLevel())) {
            builder.append("level:").append(source.getLevel()).append("->").append(target.getLevel()).append("; ");
        }
        if (!Objects.equals(target.getType(), source.getType())) {
            builder.append("type:").append(source.getType()).append("->").append(target.getType()).append("; ");
        }*/
        if (!Objects.equals(target.getAuthenticationState(), source.getAuthenticationState())) {
            builder.append("authenticationState:").append(source.getAuthenticationState()).append("->").append(target.getAuthenticationState()).append("; ");
        }
        if (!Objects.equals(target.getAuthenticationSource(), source.getAuthenticationSource())) {
            builder.append("authenticationSource:").append(source.getAuthenticationSource()).append("->").append(target.getAuthenticationSource()).append("; ");
        }

        Long schoolId = source.getId();
        String operation = "管理员[" + adminUser.getAdminUserName() + "] 修改学校[" + schoolId + "]";
        addAdminLog(adminUser, actionUrl, operation, schoolId, builder.toString(), "学校线索通过审核，同步修改关联的学校信息");
    }

    private void schoolExtInfoTouchLog(SchoolExtInfo source, SchoolExtInfo target, AuthCurrentAdminUser adminUser, String actionUrl) {
    }

    private String schoolExtInfoTouchLog(SchoolExtInfo schoolExtInfo, CrmSchoolClue clue) {
        StringBuilder builder = new StringBuilder();
        if (schoolExtInfo == null) {
            builder.append("latitude:").append(clue.getLatitude()).append(";");
            builder.append("longitude:").append(clue.getLongitude()).append(";");
            builder.append("coordinateType:").append(clue.getCoordinateType()).append(";");
            builder.append("address").append(clue.getAddress()).append(";");
            //builder.append("make:").append(clue.getMake()).append(";");
            //builder.append("model:").append(clue.getModel()).append(";");
        } else {
            builder.append("latitude:").append(schoolExtInfo.getLatitude()).append("-->").append(clue.getLatitude()).append(";");
            builder.append("longitude:").append(schoolExtInfo.getLongitude()).append("-->").append(clue.getLongitude()).append(";");
            builder.append("coordinateType:").append(schoolExtInfo.getCoordinateType()).append("-->").append(clue.getCoordinateType()).append(";");
            builder.append("address:").append(schoolExtInfo.getAddress()).append("-->").append(clue.getAddress()).append(";");
            //builder.append("make:").append(schoolExtInfo.getLatitude()).append("-->").append(clue.getMake()).append(";");
            //builder.append("model:").append(schoolExtInfo.getLatitude()).append("-->").append(clue.getModel()).append(";");
        }
        return builder.toString();
    }

    private void addAdminLog(AuthCurrentAdminUser adminUser, String actionUrl, String operation, Long targetId, String targetData, String comment) {
        if (StringUtils.isNotBlank(targetData)) {
            AdminLog adminLog = new AdminLog();
            String admin = adminUser.getAdminUserName();
            adminLog.setAdminUserName(admin);
            adminLog.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
            adminLog.setOperation(operation);
            adminLog.setWebActionUrl(actionUrl);
            adminLog.setTargetId(targetId);
            adminLog.setTargetData(targetData);
            adminLog.setComment(comment);
            adminLogServiceClient.getAdminLogService()
                    .persistAdminLog(adminLog)
                    .awaitUninterruptibly();
        }
    }

    public Map<String, String> photoMeta(String photoUrl) {
        if (StringUtils.isBlank(photoUrl)) {
            return null;
        }
        Map<String, String> photoMeta = new HashMap<>();
        try {
            URL url = new URL(photoUrl);
            @Cleanup InputStream in = url.openStream();
            Metadata metadata = JpegMetadataReader.readMetadata(in);
            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.getTagCount() > 0) {
                Collection<Tag> tags = directory.getTags();
                for (Tag tag : tags) {
                    photoMeta.put(tag.getTagName(), tag.getDescription());
                }
            }
        } catch (Exception e) {
            return photoMeta;
        }
        return photoMeta;
    }

    public CrmSchoolClue findLastestAuthedSchoolClue(Long schoolId) {
        List<CrmSchoolClue> schoolClues = crmSchoolClueDao.findClueListBySchoolIdAndStatus(schoolId, 2);
        if (CollectionUtils.isEmpty(schoolClues)) {
            return null;
        }
        return schoolClues.get(0);
    }
}
