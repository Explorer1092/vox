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

package com.voxlearning.ucenter.support;

import com.aspose.words.SaveFormat;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.data.DownloadContent;
import com.voxlearning.utopia.mapper.ClazzInfoMapper;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.GroupStudentNameRef;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import freemarker.template.Template;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named("com.voxlearning.ucenter.support.TeacherResourceDownloadHelper")
public class TeacherResourceDownloadHelper extends SpringContainerSupport {

    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private RaikouSDK raikouSDK;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    public DownloadContent downloadNewNumber(TeacherDetail teacher, List<Clazz> clazzs, Integer count, String mobile, String method) {
        String realname = StringUtils.defaultString(teacher.fetchRealname());
        String filename = realname + "老师_";
        for (int i = 0; i < clazzs.size(); i++) {
            Clazz clazz = clazzs.get(i);
            filename += clazz.formalizeClazzName();
            if (i != clazzs.size() - 1) {
                filename += "_";
            }
        }
        filename += ".doc";
        byte[] content;
        try {
            List<Long> clazzIdList = clazzs.stream()
                    .filter(e -> e != null && e.getId() != null)
                    .map(Clazz::getId)
                    .collect(Collectors.toList());
            content = batchDownloadUnStudentInfo(teacher, clazzIdList, count, mobile, method);
        } catch (Exception ex) {
            logger.error("FAILED TO DOWNLOAD TEACHER '{}' STUDENT INFORMATION", teacher.getId(), ex);
            return null;
        }
        DownloadContent downloadContent = new DownloadContent();
        downloadContent.setContent(content);
        downloadContent.setFilename(filename);
        downloadContent.setContentType("application/msword");
        return downloadContent;
    }

    public DownloadContent downloadClazzStudentInformation(TeacherDetail teacher, List<Clazz> clazzs, String mobile, String method) {
        String realname = StringUtils.defaultString(teacher.fetchRealname());
        String filename = realname + "老师_";
        for (int i = 0; i < clazzs.size(); i++) {
            Clazz clazz = clazzs.get(i);
            filename += clazz.formalizeClazzName();
            if (i != clazzs.size() - 1) {
                filename += "_";
            }
        }
        filename += ".doc";
        byte[] content;
        try {
            List<Long> clazzIdList = clazzs.stream()
                    .filter(e -> e != null && e.getId() != null)
                    .map(Clazz::getId)
                    .collect(Collectors.toList());
            content = batchDownload(teacher, clazzIdList, mobile, method);
        } catch (Exception ex) {
            logger.error("FAILED TO DOWNLOAD TEACHER '{}' STUDENT INFORMATION", teacher.getId(), ex);
            return null;
        }
        DownloadContent downloadContent = new DownloadContent();
        downloadContent.setContent(content);
        downloadContent.setFilename(filename);
        downloadContent.setContentType("application/msword");
        return downloadContent;
    }

    private byte[] batchDownload(TeacherDetail teacher, List<Long> clazzIds, String mobile, String method) throws Exception {
        StringBuilder content = new StringBuilder();
        content.append("<html>");
        // 已存在学生账号
        Map<Long, List<ClazzInfoMapper>> mappers = deprecatedClazzLoaderClient.getRemoteReference().loadClazzInfoMappers(teacher.getId(), clazzIds);
        // 老师名单
        Map<Long, GroupMapper> groups = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacher.getId(), clazzIds, false);
        Map<Long, List<GroupStudentNameRef>> studentNameList = groupLoaderClient.loadGroupStudentNameList(groups.values().stream().map(GroupMapper::getId).collect(Collectors.toList()));
        for (int i = 0; i < clazzIds.size(); i++) {
            Long clazzId = clazzIds.get(i);
            List<ClazzInfoMapper> clazzInfoMappers = mappers.get(clazzId);
            if (clazzInfoMappers == null) {
                clazzInfoMappers = Collections.emptyList();
            }
            Map<String, Object> map_top = new HashMap<>();
            List<Map<String, Object>> map_top_list = new ArrayList<>();
            Set<String> studentNameSet = new HashSet<>();
            for (ClazzInfoMapper mapper : clazzInfoMappers) {
                map_top_list.add(generateMapContent(mapper));
                studentNameSet.add(mapper.getUserName());
            }

            GroupMapper group = groups.get(clazzId);
            List<String> studentNames = new LinkedList<>();
            if (group != null) {
                List<GroupStudentNameRef> groupStudentNameRefs = studentNameList.get(group.getId());
                if (CollectionUtils.isNotEmpty(groupStudentNameRefs)) {
                    for (GroupStudentNameRef ref : groupStudentNameRefs) {
                        if (!studentNameSet.contains(ref.getStudentName())) {
                            studentNames.add(ref.getStudentName());
                        }
                    }

                }
            }

            map_top.put("map_top_list", map_top_list);
            map_top.put("studentNameList", studentNames);
            map_top.put("teacherName", teacher.fetchRealname());
            map_top.put("method", method);
            map_top.put("mobile", mobile);
            map_top.put("teacherId", teacher.getId());
            map_top.put("subject", teacher.getSubject().getValue());
            map_top.put("clazzId", clazzId);

            // Feature #46231 小学老师班级管理，下载学生名单优化，格式同中学 Update: 2017-06-01
            Template headerTemplate = FreemarkerTemplateParser.parse("/letter/formsstudent.ftl");
//            if (teacher.isPrimarySchool()) {
//                headerTemplate = FreemarkerTemplateParser.parse("/letter/forstudent.ftl");
//            } else {
//                headerTemplate = FreemarkerTemplateParser.parse("/letter/formsstudent.ftl");
//            }
            if (headerTemplate != null) {
                content.append(FreeMarkerTemplateUtils.processTemplateIntoString(headerTemplate, map_top));
                if (i != clazzIds.size() - 1) {
                    content.append("<br style=\"page-break-before: always\">");
                }
            }
        }
        content.append("</html>");

        return writeWordDocument(content.toString());
    }

    private Map<String, Object> generateMapContent(ClazzInfoMapper mapper) {
        Map<String, Object> map = new HashMap<>();
        map.put("schoolName", StringUtils.defaultString(mapper.getSchoolName()));
        int clazzLevel = mapper.getClazzLevel() == null ? 0 : mapper.getClazzLevel();
        if (clazzLevel < 10) {
            map.put("clazzName", ClazzLevel.getDescription(clazzLevel) + StringUtils.defaultString(mapper.getClazzName()));
        } else {
            map.put("clazzName", StringUtils.defaultString(mapper.getClazzName()));
        }
        map.put("name", StringUtils.defaultString(mapper.getUserName()));
        map.put("userId", SafeConverter.toString(mapper.getUserId(), ""));
        map.put("pwd", StringUtils.defaultString(mapper.getPwd()));
        map.put("mobile", sensitiveUserDataServiceClient.loadUserMobileObscured(mapper.getUserId()));
        if (StringUtils.isBlank(SafeConverter.toString(map.get("mobile")))) {
            List<StudentParent> parentList = parentLoaderClient.loadStudentParents(mapper.getUserId());
            map.put("mobile", getStudentParentMobileObscure(parentList));
        }
        return map;
    }

    private byte[] writeWordDocument(String content) throws Exception {
        WordBuilder wordBuilder = new WordBuilder();
        wordBuilder.builder();
        wordBuilder.insertHtml(content);
        return wordBuilder.saveAsByteArray(SaveFormat.DOC);
    }

    private byte[] batchDownloadUnStudentInfo(TeacherDetail teacher, List<Long> clazzIds, Integer count, String mobile, String method) throws Exception {
        StringBuilder content = new StringBuilder();
        content.append("<html>");
        for (int i = 0; i < clazzIds.size(); i++) {
            Long clazzId = clazzIds.get(i);
            Map<String, Object> map_top = new HashMap<>();
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            map_top.put("count", count);
            map_top.put("teacherName", teacher.fetchRealname());
            map_top.put("method", method);
            map_top.put("mobile", mobile);
            map_top.put("teacherId", teacher.getId());
            map_top.put("subject", teacher.getSubject().getValue());
            map_top.put("clazzId", clazzId);
            map_top.put("clazzName", clazz.formalizeClazzName());

            Template headerTemplate = FreemarkerTemplateParser.parse("/letter/forstudentnew.ftl");
            if (headerTemplate != null) {
                content.append(FreeMarkerTemplateUtils.processTemplateIntoString(headerTemplate, map_top));
                if (i != clazzIds.size() - 1) {
                    content.append("<br style=\"page-break-before: always\">");
                }
            }
        }
        content.append("</html>");

        return writeWordDocument(content.toString());
    }

    private String getStudentParentMobileObscure(List<StudentParent> studentParents) {
        // 如果有多个家长，优先显示关键家长，如果没有则任选一个；
        if (CollectionUtils.isEmpty(studentParents)) {
            return null;
        }

        // 优先关键家长的手机
        StudentParent keyParent = studentParents.stream()
                .filter(StudentParent::isKeyParent)
                .findFirst()
                .orElse(null);

        if (keyParent != null) {
            String phone = sensitiveUserDataServiceClient.loadUserMobileObscured(keyParent.getParentUser().getId());
            if (StringUtils.isNoneBlank(phone)) {
                return phone;
            }
        }

        // 没有关键家长，或关键家长手机未绑定，则随便一个绑定手机家长的手机
        for (StudentParent studentParent : studentParents) {
            String phone = sensitiveUserDataServiceClient.loadUserMobileObscured(studentParent.getParentUser().getId());
            if (StringUtils.isNoneBlank(phone)) {
                return phone;
            }
        }

        return null;
    }

}
