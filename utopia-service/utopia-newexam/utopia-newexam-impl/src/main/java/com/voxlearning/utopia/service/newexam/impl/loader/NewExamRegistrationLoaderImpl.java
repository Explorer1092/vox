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

package com.voxlearning.utopia.service.newexam.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamRegistration;
import com.voxlearning.utopia.service.newexam.api.loader.NewExamRegistrationLoader;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamForExport;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamRegistrationLoaderMapper;
import com.voxlearning.utopia.service.newexam.impl.support.NewExamSpringBean;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/3/7.
 */
@Named
@Service(interfaceClass = NewExamRegistrationLoader.class)
@ExposeService(interfaceClass = NewExamRegistrationLoader.class)
public class NewExamRegistrationLoaderImpl extends NewExamSpringBean implements NewExamRegistrationLoader {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public NewExamRegistration loadById(String id) {
        return newExamRegistrationDao.load(id);
    }

    @Override
    public Map<String, NewExamRegistration> loadByIds(Collection<String> ids) {
        return newExamRegistrationDao.loads(ids);
    }

    @Override
    public MapMessage loadByNewExamIdAndPage(NewExamRegistrationLoaderMapper newExamRegistrationLoaderMapper) {
        if (newExamRegistrationLoaderMapper == null) {
            return MapMessage.errorMessage("无效的参数");
        }

        String newExamId = newExamRegistrationLoaderMapper.getNewExamId();
        long studentId = SafeConverter.toLong(newExamRegistrationLoaderMapper.getStudentId());
        String studentName = newExamRegistrationLoaderMapper.getStudentName();
        int provinceId = SafeConverter.toInt(newExamRegistrationLoaderMapper.getProvinceId());
        int cityId = SafeConverter.toInt(newExamRegistrationLoaderMapper.getCityId());
        int regionId = SafeConverter.toInt(newExamRegistrationLoaderMapper.getRegionId());
        long schoolId = SafeConverter.toLong(newExamRegistrationLoaderMapper.getSchoolId());
        int currentPage = SafeConverter.toInt(newExamRegistrationLoaderMapper.getCurrentPage());
        int pageSize = SafeConverter.toInt(newExamRegistrationLoaderMapper.getPageSize(), 10);

        if (StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("考试id不能为空");
        }
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return MapMessage.errorMessage("无效的考试id");
        }
        List<NewExamRegistration> newExamRegistrationList = new ArrayList<>();
        List<String> newExamRegistrationIdList = newExamRegistrationDao.findByNewExam(newExam);
        if (CollectionUtils.isNotEmpty(newExamRegistrationIdList)) {
            Map<String, NewExamRegistration> newExamRegistrationMap = newExamRegistrationDao.loads(newExamRegistrationIdList);
            for (String id : newExamRegistrationIdList) {
                NewExamRegistration newExamRegistration = newExamRegistrationMap.get(id);
                if (newExamRegistration != null && !SafeConverter.toBoolean(newExamRegistration.getBeenCanceled())
                        && (studentId == 0 || Objects.equals(newExamRegistration.getUserId(), studentId))
                        && (StringUtils.isBlank(studentName) || Objects.equals(newExamRegistration.getUserName(), studentName))
                        && (provinceId == 0 || Objects.equals(newExamRegistration.getProvinceId(), provinceId))
                        && (cityId == 0 || Objects.equals(newExamRegistration.getCityId(), cityId))
                        && (regionId == 0 || Objects.equals(newExamRegistration.getRegionId(), regionId))
                        && (schoolId == 0 || Objects.equals(newExamRegistration.getSchoolId(), schoolId))
                        && (!SafeConverter.toBoolean(newExamRegistrationLoaderMapper.getFilterNotStart()) || newExamRegistration.getStartAt() != null)) {
                    newExamRegistrationList.add(newExamRegistration);
                }
            }
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("totalCount", newExamRegistrationList.size());
        Set<Long> clazzIds = newExamRegistrationList
                .stream()
                .map(NewExamRegistration::getClazzId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Clazz> clazzMap = raikouSystem.loadClazzes(clazzIds);
        int startIndex = currentPage * pageSize;
        int endIndex = (currentPage + 1) * pageSize;
        int listSize = newExamRegistrationList.size();
        if (startIndex > listSize) {
            startIndex = listSize;
        }
        if (endIndex > listSize) {
            endIndex = listSize;
        }
        mapMessage.put("registrationList", newExamRegistrationList.subList(startIndex, endIndex).stream()
                .map(o -> convertRegistrationToMap(o, clazzMap))
                .collect(Collectors.toList()));
        return mapMessage;
    }

    @Override
    public List<NewExamForExport> loadByNewExam(String newExamId) {
        NewExam newExam = newExamLoaderClient.load(newExamId);
        if (newExam == null) {
            return Collections.emptyList();
        }
        List<String> newExamRegistrationIdList = newExamRegistrationDao.findByNewExam(newExam);
        if (CollectionUtils.isNotEmpty(newExamRegistrationIdList)) {
            Map<String, NewExamRegistration> newExamRegistrationMap = newExamRegistrationDao.loads(newExamRegistrationIdList);
            List<Long> userIds = newExamRegistrationMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(NewExamRegistration::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Set<Long> schoolIds = newExamRegistrationMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(NewExamRegistration::getSchoolId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<Long> clazzIds = newExamRegistrationMap.values()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(NewExamRegistration::getClazzId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, User> userMap = raikouSystem.loadUsers(userIds);
            Map<Long, Clazz> clazzMap = raikouSystem.loadClazzes(clazzIds);
            Map<Long, School> schoolMap = raikouSystem.loadSchools(schoolIds);

            return newExamRegistrationMap.values().stream().filter(Objects::nonNull).map(o -> {
                NewExamForExport examForExport = new NewExamForExport();
                Long userId = o.getUserId();
                examForExport.setStudentId(userId);
                if (userMap.containsKey(userId)) {
                    examForExport.setStudentName(userMap.get(userId).fetchRealname());
                }
                Long clazzId = o.getClazzId();
                if (clazzMap.containsKey(clazzId)) {
                    examForExport.setClazzName(clazzMap.get(clazzId).formalizeClazzName());
                }
                Long schoolId = o.getSchoolId();
                if (schoolMap.containsKey(schoolId)) {
                    examForExport.setSchoolName(schoolMap.get(schoolId).getCname());
                }
                examForExport.setScore(SafeConverter.toDouble(o.getScore()));
                if (o.getStartAt() != null) {
                    examForExport.setBeginTime(DateUtils.dateToString(o.getStartAt()));
                }
                if (o.getSubmitAt() != null) {
                    examForExport.setSubmitTime(DateUtils.dateToString(o.getSubmitAt()));
                }
                return examForExport;
            }).collect(Collectors.toList());

        }
        return Collections.emptyList();
    }

    private Map<String, Object> convertRegistrationToMap(NewExamRegistration newExamRegistration, Map<Long, Clazz> clazzMap) {
        Integer provinceId = newExamRegistration.getProvinceId();
        Integer cityId = newExamRegistration.getCityId();
        Integer regionId = newExamRegistration.getRegionId();
        Long schoolId = newExamRegistration.getSchoolId();
        Region provinceRegion = raikouSystem.loadRegion(provinceId);
        Region cityRegion = raikouSystem.loadRegion(cityId);
        Region regionRegion = raikouSystem.loadRegion(regionId);
        School school = raikouSystem.loadSchoolIncludeDisabled(schoolId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("className", clazzMap.containsKey(newExamRegistration.getClazzId()) ? clazzMap.get(newExamRegistration.getClazzId()).formalizeClazzName() : "");
        resultMap.put("studentId", newExamRegistration.getUserId());
        resultMap.put("studentName", newExamRegistration.getUserName());
        resultMap.put("province", provinceRegion == null ? "" : provinceRegion.getName());
        resultMap.put("city", cityRegion == null ? "" : cityRegion.getName());
        resultMap.put("region", regionRegion == null ? "" : regionRegion.getName());
        resultMap.put("school", school == null ? "" : school.getShortName());
        resultMap.put("registerAt", newExamRegistration.getRegisterAt());
        resultMap.put("startAt", newExamRegistration.getStartAt());
        resultMap.put("submitAt", newExamRegistration.getSubmitAt());
        Double score = newExamRegistration.getCorrectScore() != null ? newExamRegistration.getCorrectScore() :
                newExamRegistration.getScore() != null ? newExamRegistration.getScore() : 0D;
        resultMap.put("score", new BigDecimal(score).setScale(2, BigDecimal.ROUND_HALF_UP));
        return resultMap;
    }
}
