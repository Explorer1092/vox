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

package com.voxlearning.utopia.agent.support;

import com.aspose.words.SaveFormat;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.data.DownloadContent;
import com.voxlearning.utopia.mapper.ClazzInfoMapper;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.GroupStudentNameRef;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import freemarker.template.Template;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named("com.voxlearning.utopia.agent.support.TeacherResourceDownloadHelper")
public class TeacherResourceDownloadHelper extends SpringContainerSupport {

    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;

    public DownloadContent downloadClazzStudents(TeacherDetail teacher, List<Clazz> clazzs) {
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
            content = crmBatchDownload(teacher, clazzIdList, "", "ACCOUNT");
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

    private byte[] crmBatchDownload(TeacherDetail teacher, List<Long> clazzIds, String mobile, String method) throws Exception {
        StringBuilder content = new StringBuilder();
        content.append("<html>");
        // 已存在学生账号
        Map<Long, List<ClazzInfoMapper>> mappers = deprecatedClazzLoaderClient.getRemoteReference().loadClazzInfoMappers(teacher.getId(), clazzIds);
        // 老师名单
        Map<Long, GroupMapper> groups = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzIds(teacher.getId(), clazzIds, false);
        Map<Long, List<GroupStudentNameRef>> studentNameList = groupLoaderClient.loadGroupStudentNameList(
                groups.values().stream().map(GroupMapper::getId).collect(Collectors.toList()));
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

            Template headerTemplate = FreemarkerTemplateParser.parse("/letter/clazzstudentids.ftl");
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
        return map;
    }

    private byte[] writeWordDocument(String content) throws Exception {
        WordBuilder wordBuilder = new WordBuilder();
        wordBuilder.builder();
        wordBuilder.insertHtml(content);
        return wordBuilder.saveAsByteArray(SaveFormat.DOC);
    }
}
