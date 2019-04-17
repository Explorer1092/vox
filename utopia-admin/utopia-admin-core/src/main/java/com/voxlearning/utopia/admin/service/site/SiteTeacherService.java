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

package com.voxlearning.utopia.admin.service.site;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.admin.service.site.SiteBatchInputHandler.FailedData;
import com.voxlearning.utopia.admin.service.site.SiteBatchInputHandler.UnitTransformer;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.ClazzServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author changyuan
 * @since 2016/3/31
 */
@Named
public class SiteTeacherService {

    private final static String DEFAULT_WORD_SEPARATOR = "\\t";

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private UserLoaderClient userLoaderClient;
    @Inject private ClazzServiceClient clazzServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Getter
    @Setter
    private static class AddTeacherToClazzObj {
        long schoolId;
        int grade;
        String clazzName;
        String userToken;   // 用户名或手机号

        @Override
        public String toString() {
            return schoolId + " " + grade + " " + clazzName + " " + userToken;
        }

        public static final UnitTransformer<String, AddTeacherToClazzObj> rowTransformer = row -> {
            String[] words = row.split(DEFAULT_WORD_SEPARATOR);
            if (words.length != 4) {
                return null;
            }
            AddTeacherToClazzObj addTeacherToClazzObj = new AddTeacherToClazzObj();
            addTeacherToClazzObj.setSchoolId(SafeConverter.toLong(words[0]));
            addTeacherToClazzObj.setGrade(SafeConverter.toInt(words[1]));
            addTeacherToClazzObj.setClazzName(words[2]);
            addTeacherToClazzObj.setUserToken(words[3]);
            return addTeacherToClazzObj;
        };
    }

    public MapMessage batchAddTeacherToClazz(String content) {
        DefaultExcelStringSiteBatchInputHandler<AddTeacherToClazzObj> defaultExcelStringInputHandler = new DefaultExcelStringSiteBatchInputHandler<>();
        List<FailedData<String>> failedRows = new ArrayList<>();
        List<AddTeacherToClazzObj> addTeacherToClazzObjs = defaultExcelStringInputHandler.handleInput(
                content, failedRows, AddTeacherToClazzObj.rowTransformer);

        // 检查学校
        Set<Long> schoolIds = addTeacherToClazzObjs.stream().map(AddTeacherToClazzObj::getSchoolId).collect(Collectors.toSet());
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();

        // 读取学校班级
        Set<Long> validateSchoolIds = schoolMap.values().stream().filter(s -> s != null).map(School::getId).collect(Collectors.toSet());
        Map<Long, List<Clazz>> schoolClazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadSchoolClazzs(validateSchoolIds).toGroup(Clazz::getSchoolId);

        for (AddTeacherToClazzObj addTeacherToClazzObj : addTeacherToClazzObjs) {
            // 读取老师账号
            // TODO 批量统一处理，需要同时支持id和手机号
            List<User> users = userLoaderClient.loadUserByToken(addTeacherToClazzObj.getUserToken());
            if (users.size() != 1) {
                failedRows.add(new FailedData<>(addTeacherToClazzObj.toString(), "找不到该老师"));
                continue;
            }

            // 检查学校
            if (schoolMap.get(addTeacherToClazzObj.getSchoolId()) == null) {
                failedRows.add(new FailedData<>(addTeacherToClazzObj.toString(), "学校不存在"));
                continue;
            }

            School school = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(users.get(0).getId())
                    .getUninterruptibly();
            if (school == null) {
                failedRows.add(new FailedData<>(addTeacherToClazzObj.toString(), "老师未选择学校学科"));
                continue;
            }
            if (school.getId() != addTeacherToClazzObj.getSchoolId()) {
                failedRows.add(new FailedData<>(addTeacherToClazzObj.toString(), "老师不在该学校中"));
                continue;
            }

            // 读取班级
            List<Clazz> clazzs = schoolClazzs.get(addTeacherToClazzObj.getSchoolId());
            Clazz clazz = clazzs.stream()
                    .filter(c -> c.getClazzLevel().getLevel() == addTeacherToClazzObj.getGrade())
                    .filter(c -> StringUtils.equals(c.getClassName(), addTeacherToClazzObj.getClazzName()))
                    .findFirst()
                    .orElse(null);
            if (clazz == null) {
                failedRows.add(new FailedData<>(addTeacherToClazzObj.toString(), "找不到对应班级"));
                continue;
            }

            // 老师加入班级
            MapMessage message = clazzServiceClient.teacherJoinSystemClazzForce(users.get(0).getId(), clazz.getId());
            if (!message.isSuccess()) {
                failedRows.add(new FailedData<>(addTeacherToClazzObj.toString(), message.getInfo()));
            }
        }

        return MapMessage.successMessage().add("failed", failedRows);
    }

}
