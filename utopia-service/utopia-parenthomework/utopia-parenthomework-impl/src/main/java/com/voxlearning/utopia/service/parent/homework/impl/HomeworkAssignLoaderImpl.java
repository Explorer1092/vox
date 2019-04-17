package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkAssignLoader;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.QuestionPackage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SupportType;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkQuestionPackageTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.TemplateProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.bookList.BookListTemplate;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.IntelligentTeachingServiceImpl;
import com.voxlearning.utopia.service.parent.homework.util.SubjectUtils;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.ChannelCUserAttribute;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 布置作业查询接口实现
 *
 * @author Wenlong Meng
 * @version 20181111
 */
@Named
@ExposeService(interfaceClass = HomeworkAssignLoader.class)
@Log4j
public class HomeworkAssignLoaderImpl extends SpringContainerSupport implements HomeworkAssignLoader {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private HomeworkQuestionPackageTemplate homeworkQuestionPackageTemplate;
    @Inject
    private BookListTemplate bookTemplate;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private IntelligentTeachingServiceImpl intelligentTeachingService;
    @Inject
    private HomeworkUserProgressLoaderImpl homeworkUserProgressLoader;

    /**
     * 查询教材
     *
     * @param subject    科目
     * @param userId     用户id
     * @param clazzLevel 年级
     * @param regionCode 区域id
     * @param bizType    业务类型
     * @return
     */
    @Override
    public MapMessage loadBooks(String subject, Long userId, Integer clazzLevel, Integer regionCode, String bizType) {
        HomeworkParam param = new HomeworkParam();
        param.setSubject(subject);
        param.setStudentId(userId);
        param.setBizType(bizType);
        Map<String, Object> data = new HashMap<>();
        data.put("clazzLevel", clazzLevel);
        data.put("regionCode", regionCode);
        param.setData(data);
        HomeworkContext hc = new HomeworkContext();
        hc.setHomeworkParam(param);
        getBookListTemplate(bizType).process(hc);
        return hc.getMapMessage();
    }

    /**
     * 查询题包
     *
     * @return 题包信息
     */
    @Override
    public MapMessage loadQuestionBoxes(HomeworkParam param) {
        if (param.getStudentId() == null) {
            return MapMessage.errorMessage("参数校验失败");
        }
        if (StringUtils.isBlank(param.getSubject())) {
            param.setSubject(SubjectUtils.BASIC_SUBJECTS.get(0).name());
        }
        HomeworkContext hc = new HomeworkContext();
        hc.setHomeworkParam(param);
        getQuestionPackageTemplate(param.getBizType()).process(hc);
        return hc.getMapMessage();
    }

    /**
     * 查询学生信息
     *
     * @param studentId 学生id
     * @return 学生信息
     */
    @Override
    public StudentInfo loadStudentInfo(Long studentId) {
        if (studentId == null) {
            return null;
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null) {
            return null;
        }
        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setStudentId(studentId);
        studentInfo.setStudentName(studentDetail.fetchRealname());
        Clazz clazz = studentDetail.getClazz();
        Integer regionCode = null;
        int clazzLevel = 0;
        if (clazz == null) {
            // 获取c端用户信息
            ChannelCUserAttribute channelCUserAttribute = studentLoaderClient.loadStudentChannelCAttribute(studentId);
            if (channelCUserAttribute != null) {
                clazzLevel = ObjectUtils.get(() -> ChannelCUserAttribute.getClazzCLevelByClazzJie(channelCUserAttribute.getClazzJie()).getLevel(), 0);
                regionCode = channelCUserAttribute.getRegionCode();
            }
        } else {
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(clazz.getSchoolId()).getUninterruptibly();
            if (school != null) {
                regionCode = school.getRegionCode();
                studentInfo.setSchoolId(school.getId());
                studentInfo.setSchoolName(school.getCname());
            }
            clazzLevel = Integer.valueOf(clazz.getClassLevel());
            studentInfo.setClazzId(clazz.getId());
            studentInfo.setClazzName(clazz.getClassName());
        }
        if (clazzLevel != 0) {
            studentInfo.setClazzLevel(clazzLevel);
        }
        ExRegion exRegion;
        if (regionCode != null && (exRegion = raikouSystem.loadRegion(regionCode)) != null) {
            studentInfo.setRegionCode(regionCode);
            studentInfo.setCityCode(exRegion.getCityCode());
        }
        return studentInfo;
    }

    @Override
    public List<String> loadQuestionDocIdByBoxId(Collection<String> boxIds, String bizType) {
        List<String> docIds = new ArrayList<>();
        for (String boxId : boxIds) {
            QuestionPackage boxes = HomeWorkCache.load(CacheKey.BOX, bizType, boxId);
            if (boxes == null) {
                return new ArrayList<>();
            }
            docIds.addAll(boxes.getDocIds());
        }
        return questionLoaderClient.loadQuestionByDocIds(docIds).stream().map(question -> {
            // 返回口算内容
            String content = question.getContent().getSubContents().get(0).getContent();
            if (content.contains("<p")) {
                content = content.replaceAll("<p>|</p>", "");
            }
            return content;
        }).collect(Collectors.toList());
    }

    /**
     * 初始化模板
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        applicationContext.getBeansOfType(TemplateProcessor.class).values()
                .forEach(p -> {
                            SupportType annotation = p.getClass().getAnnotation(SupportType.class);
                    if (annotation == null) {
                                return;
                            }
                            switch (annotation.op()) {
                                case "bookList":
                                    bookListTemplateMap.put(annotation.bizType(), p);
                                    break;
                                case "questionPackage":
                                    questionPackageTemplate.put(annotation.bizType(), p);
                                    break;
                                default:
                                    break;
                            }
                        }
                );
    }

    private static Map<String, TemplateProcessor> bookListTemplateMap = new HashMap<>();
    private static Map<String, TemplateProcessor> questionPackageTemplate = new HashMap<>();

    private TemplateProcessor getBookListTemplate(String bizType) {
        return bookListTemplateMap.getOrDefault(bizType, bookListTemplateMap.get("*"));
    }

    private TemplateProcessor getQuestionPackageTemplate(String bizType) {
        return questionPackageTemplate.getOrDefault(bizType, questionPackageTemplate.get("*"));
    }

}
