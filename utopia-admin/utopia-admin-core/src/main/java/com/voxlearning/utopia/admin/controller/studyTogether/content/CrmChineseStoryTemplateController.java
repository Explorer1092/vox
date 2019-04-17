package com.voxlearning.utopia.admin.controller.studyTogether.content;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.module.config.CommonConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.galaxy.service.studycourse.api.CrmLessonTemplateLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmLessonTemplateService;
import com.voxlearning.galaxy.service.studycourse.api.CrmModelContentLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmModelContentService;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseBlackWidowServiceClient;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.ChineseStoryExpandStudyModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.ChineseStoryKnowledgeModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.ChineseStoryModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.ChineseStoryLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.StudyModel;
import com.voxlearning.galaxy.service.studycourse.api.mapper.ChineseStoryLessonTemplateLogMapper;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.galaxy.service.studycourse.constant.StudyModelType;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.CourseSeries;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.CourseSpu;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "opmanager/studyTogether/template/chinesestory")
@Slf4j
public class CrmChineseStoryTemplateController extends AbstractAdminController {


    @StorageClientLocation(storage = "17-pmc")
    private StorageClient imgStorageClient;
    @ImportService(interfaceClass = CrmLessonTemplateLoader.class)
    private CrmLessonTemplateLoader lessonTemplateLoader;
    @ImportService(interfaceClass = CrmLessonTemplateService.class)
    private CrmLessonTemplateService lessonTemplateService;
    @ImportService(interfaceClass = CrmModelContentService.class)
    private CrmModelContentService modelContentService;
    @ImportService(interfaceClass = CrmModelContentLoader.class)
    private CrmModelContentLoader modelContentLoader;
    @ImportService(interfaceClass = CrmStudyTogetherService.class)
    private CrmStudyTogetherService crmStudyTogetherService;
    @Inject
    private QuestionLoaderClient questionLoaderClient;
    @Inject
    private StudyCourseBlackWidowServiceClient studyCourseBlackWidowServiceClient;


    @RequestMapping(value = "chinese_story_list.vpage", method = RequestMethod.GET)
    public String getChineseStoryTemplateList(Model model) {
        Integer page = getRequestInt("page", 1);
        long templateId = getRequestLong("template_id");
        String templateName = getRequestString("template_name");
        String createUser = getRequestString("create_user");
        long spuId = getRequestLong("template_spu_id");

        //前端输入回显
        model.addAttribute("page", page);
        model.addAttribute("template_id", templateId != 0 ? templateId : "");
        model.addAttribute("template_name", templateName);
        model.addAttribute("create_user", createUser);
        model.addAttribute("template_spu_id", spuId != 0 ? spuId : "");
        model.addAttribute("course_type", 6);
        List<ChineseStoryLessonTemplate> templateList = new ArrayList<>();
        if (templateId != 0) {
            ChineseStoryLessonTemplate template = lessonTemplateLoader.loadChineseStoryLessonTemplate(templateId);
            if (template != null) {
                templateList.add(template);
            }
        } else {
            templateList = lessonTemplateLoader.loadAllChineseStoryLessonTemplate();
        }
        if (StringUtils.isNotBlank(templateName)) {
            templateList = templateList.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getName()))
                    .filter(p -> p.getName().contains(templateName))
                    .collect(Collectors.toList());
        }

        if (StringUtils.isNotBlank(createUser)) {
            templateList = templateList.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getCreateUser()))
                    .filter(p -> p.getCreateUser().contains(createUser))
                    .collect(Collectors.toList());
        }
        if (spuId != 0) {
            templateList = templateList.stream()
                    .filter(p -> p.getSpuId() != null && Objects.equals(p.getSpuId(), spuId))
                    .collect(Collectors.toList());
        }
        templateList.sort((o1, o2) -> o2.getId().compareTo(o1.getId()));
        Page<ChineseStoryLessonTemplate> templatePage = PageableUtils.listToPage(templateList, new PageRequest(page - 1, 10));
        List<ChineseStoryLessonTemplate> returnTemplateList = templatePage.getContent();

        List<Map<String, Object>> mapList = new ArrayList<>();
        returnTemplateList.forEach(p -> {
            Map<String, Object> map = new HashMap<>();
            //id
            map.put("id", p.getId());
            //语文古文模板名称
            map.put("name", p.getName());
            //spuId
            map.put("spu_id", p.getSpuId());
            //创建者
            map.put("create", p.getCreateUser());
            mapList.add(map);
        });

        model.addAttribute("total_page", templatePage.getTotalPages());
        model.addAttribute("chinese_story_template_list", mapList);
        return "/opmanager/studyTogether/temlate/templatelist";
    }


    @RequestMapping(value = "chinese_story_template_info.vpage", method = RequestMethod.GET)
    public String getChineseStoryTemplateInfo(Model model) {

        long templateId = getRequestLong("template_id");


        //是否允许编辑
        model.addAttribute("edit", getRequestInt("edit"));
        model.addAttribute("admin_user", getCurrentAdminUser().getAdminUserName());
        //全部的spu列表
        List<CourseSpu> courseSpus = crmStudyTogetherService.loadAllCourseSpu();
        Set<Long> seriesIds = courseSpus.stream().filter(p -> Objects.nonNull(p.getSeriesId()))
                .map(CourseSpu::getSeriesId)
                .collect(Collectors.toSet());
        Map<Long, CourseSeries> courseSeriesMap = crmStudyTogetherService.loadCourseSeries(seriesIds);
        //所有古文的spu
        courseSpus = courseSpus.stream().filter(p -> Objects.nonNull(p.getSeriesId()))
                .filter(p -> Objects.nonNull(courseSeriesMap.get(p.getSeriesId())))
                .filter(p -> Objects.nonNull(courseSeriesMap.get(p.getSeriesId()).getCourseType()))
                .filter(p -> courseSeriesMap.get(p.getSeriesId()).getCourseType() == 6)
                .collect(Collectors.toList());
        model.addAttribute("spu_list", courseSpus);

        if (templateId == 0) {
            return "/opmanager/studyTogether/temlate/chinesestoryinfo";
        }
        ChineseStoryLessonTemplate template = lessonTemplateLoader.loadChineseStoryLessonTemplate(templateId);
        if (template == null) {
            getAlertMessageManager().addMessageError("您要查处理的语文故事模板不存在");
            return "/opmanager/studyTogether/temlate/chinesestoryinfo";
        }

        if (CollectionUtils.isNotEmpty(template.getStudyModelList())) {
            template.getStudyModelList().forEach(p -> {
                if (p.getModelType() == StudyModelType.CHINESESTORY) {
                    ChineseStoryModelContent content = modelContentLoader.loadChineseStoryModelContent(p.getTypeId());
                    model.addAttribute("story_content", content);
                    model.addAttribute("story_title", p.getTitle());
                    model.addAttribute("story_img", p.getTypeImg());
                } else if (p.getModelType() == StudyModelType.CHINESESTORYKNOWLEDGE) {
                    ChineseStoryKnowledgeModelContent content = modelContentLoader.loadChineseStoryKnowledgeModelContent(p.getTypeId());
                    model.addAttribute("knowledge_content", content);
                    model.addAttribute("knowledge_title", p.getTitle());
                    model.addAttribute("knowledge_img", p.getTypeImg());
                } else if (p.getModelType() == StudyModelType.CHINESESTORYEXPAND) {
                    ChineseStoryExpandStudyModelContent content = modelContentLoader.loadChineseStoryExpandStudyModelContent(p.getTypeId());
                    model.addAttribute("expound_content", content);
                    model.addAttribute("expound_title", p.getTitle());
                    model.addAttribute("expound_img", p.getTypeImg());
                }
            });
        }
        List<String> modelTypeList = template.getStudyModelList().stream().map(e -> e.getModelType().name()).collect(Collectors.toList());
        model.addAttribute("model_rank_list", modelTypeList);
        model.addAttribute("template", template);
        model.addAttribute("cdn_host", StringUtils.defaultString(CommonConfigManager.Companion.getInstance().getCommonConfig().getConfigs().get("oss_pmc_host")));
        return "/opmanager/studyTogether/temlate/chinesestoryinfo";
    }


    @RequestMapping(value = "save_chinese_story.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveChineseStoryTemplate() {
        MapMessage mapMessage = validateAndGenerateChineseStoryInfo();
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        logger.info(mapMessage.toString());
        //日志支持
        ChineseStoryLessonTemplateLogMapper oldTemplateMapper = new ChineseStoryLessonTemplateLogMapper();
        ChineseStoryLessonTemplateLogMapper newTemplateMapper = new ChineseStoryLessonTemplateLogMapper();

        ChineseStoryLessonTemplate template = (ChineseStoryLessonTemplate) (mapMessage.get("chinese_story_template"));
        ChineseStoryExpandStudyModelContent expoundModelContent = (ChineseStoryExpandStudyModelContent) (mapMessage.get("expound_model_content"));
        if (expoundModelContent != null && StringUtils.isNotBlank(expoundModelContent.getId())) {
            ChineseStoryExpandStudyModelContent oldExpoundModel = modelContentLoader.loadChineseStoryExpandStudyModelContent(expoundModelContent.getId());
            oldTemplateMapper.setExpandStudyModelContent(oldExpoundModel);
        }
//        ChineseStoryFollowReadModelContent followReadModelContent = (ChineseStoryFollowReadModelContent) (mapMessage.get("follow_read_model_content"));
//        if (StringUtils.isNotBlank(followReadModelContent.getId())) {
//            ChineseStoryFollowReadModelContent oldfollowReadModel = modelContentLoader.loadChineseStoryFollowReadModelContent(followReadModelContent.getId());
//            oldTemplateMapper.setFollowReadModelContent(oldfollowReadModel);
//        }
        ChineseStoryModelContent storyModelContent = (ChineseStoryModelContent) (mapMessage.get("story_model_content"));
        if (storyModelContent != null && StringUtils.isNotBlank(storyModelContent.getId())) {
            ChineseStoryModelContent oldstoryModel = modelContentLoader.loadChineseStoryModelContent(storyModelContent.getId());
            oldTemplateMapper.setStoryModelContent(oldstoryModel);
        }
        ChineseStoryKnowledgeModelContent knowledgeModelContent = (ChineseStoryKnowledgeModelContent) (mapMessage.get("knowledge_model_content"));
        if (knowledgeModelContent != null && StringUtils.isNotBlank(knowledgeModelContent.getId())) {
            ChineseStoryKnowledgeModelContent oldknowledgeModel = modelContentLoader.loadChineseStoryKnowledgeModelContent(knowledgeModelContent.getId());
            oldTemplateMapper.setKnowledgeModelContent(oldknowledgeModel);
        }
        //更新学习流程
        ChineseStoryExpandStudyModelContent updateExpoundModelContent = modelContentService.saveChineseStoryExpandStudyModelContent(expoundModelContent);
        newTemplateMapper.setExpandStudyModelContent(updateExpoundModelContent);
//        ChineseStoryFollowReadModelContent updateFollowReadModelContent = modelContentService.saveChineseStoryFollowReadModelContent(followReadModelContent);
//        newTemplateMapper.setFollowReadModelContent(updateFollowReadModelContent);
        ChineseStoryModelContent updateStoryModelContent = modelContentService.saveChineseStoryModelContent(storyModelContent);
        newTemplateMapper.setStoryModelContent(updateStoryModelContent);
        ChineseStoryKnowledgeModelContent updateKnowledgeModelCotent = modelContentService.saveChineseStoryKnowledgeModelContent(knowledgeModelContent);
        newTemplateMapper.setKnowledgeModelContent(updateKnowledgeModelCotent);
        template.getStudyModelList().forEach(p -> {
            if (p.getModelType() == StudyModelType.CHINESESTORYEXPAND) {
                p.setTypeId(updateExpoundModelContent.getId());
            } else if (p.getModelType() == StudyModelType.CHINESESTORYKNOWLEDGE) {
                p.setTypeId(updateKnowledgeModelCotent.getId());
            } else if (p.getModelType() == StudyModelType.CHINESESTORY) {
                p.setTypeId(updateStoryModelContent.getId());
            }
        });
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (template.getId() == null) {
            template.setCreateUser(adminUser.getAdminUserName());
        } else {
            ChineseStoryLessonTemplate oldTemplate = lessonTemplateLoader.loadChineseStoryLessonTemplate(template.getId());
            oldTemplateMapper.setLessonTemplate(oldTemplate);
        }
        template.setUpdateUser(adminUser.getAdminUserName());
        ChineseStoryLessonTemplate updatedTemplate = lessonTemplateService.saveChineseStoryLessonTemplate(template);
        newTemplateMapper.setLessonTemplate(updatedTemplate);
//        //实时把修改同步给php
//        lessonTemplateService.syncClassicalChineseTemplateToPhp(template, updateExpoundModelContent, updateAppreciateModelContent, updateReciteModelContent, updateFunModelContent);
        //CRM 修改日志
        studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldTemplateMapper, newTemplateMapper, adminUser.getAdminUserName(), ChangeLogType.ChineseStory, updatedTemplate.getId().toString());
        return MapMessage.successMessage().add("id", updatedTemplate.getId());
    }


    @RequestMapping(value = "/ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorcontroller() throws IOException {
        String action = getRequestString("action");
        switch (action) {
            case "config":
                return MapMessage.successMessage()
                        .add("imageActionName", "uploadimage")
                        .add("imageFieldName", "upfile")
                        .add("imageInsertAlign", "none")
                        .add("imageMaxSize", 2048000)
                        .add("imageUrlPrefix", "");
            case "uploadimage":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");
                if (imgFile.isEmpty()) {
                    return MapMessage.errorMessage("没有文件上传");
                }
                String originalFileName = imgFile.getOriginalFilename();
                try {
                    MapMessage mapMessage = uploadImg(imgFile);
                    if (!mapMessage.isSuccess()) {
                        return mapMessage;
                    }
                    return MapMessage.successMessage()
                            .add("url", mapMessage.get("imgUrl"))
                            .add("title", mapMessage.get("imgName"))
                            .add("state", "SUCCESS")
                            .add("original", originalFileName);
                } catch (Exception ex) {
                    logger.error("上传图片异常： " + ex.getMessage(), ex);
                    return MapMessage.errorMessage("上传图片异常： " + ex.getMessage());
                }
            default:
                return MapMessage.successMessage();
        }
    }


    private MapMessage uploadImg(MultipartFile inputFile) throws IOException {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
        if (StringUtils.isBlank(suffix)) {
            suffix = "jpg";
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = "chineseStoryImg/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "chineseStoryImg/test/";
        }
        String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
        String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
        String realName = imgStorageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
        String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        return MapMessage.successMessage().add("imgName", realName).add("imgUrl", fileUrl);
    }


    private MapMessage validateAndGenerateChineseStoryInfo() {
        //基本信息
        String templateInfoStr = getRequestString("template_info");
        //拓展学习
        String expoundInfoStr = getRequestString("expand_info");
        //知识点
        String knowledgeInfoStr = getRequestString("knowledge_info");
        //故事
        String storyInfoStr = getRequestString("story_info");
        //排序用
        String rankInfo = getRequestString("rank_info");
//        //句子跟读
//        String followReadInfoStr = getRequestString("follow_read_info");
        if (StringUtils.isBlank(templateInfoStr)) {
            return MapMessage.errorMessage("语文故事基本信息不能为空");
        }
        //语文故事模版
        ChineseStoryLessonTemplate template = JsonUtils.fromJson(templateInfoStr, ChineseStoryLessonTemplate.class);
        if (template == null) {
            return MapMessage.errorMessage("语文故事基本信息不能为空");
        }
        if (template.getSpuId() == null) {
            return MapMessage.errorMessage("语文故事SPU_ID不能为空");
        }
        if (StringUtils.isBlank(template.getName())) {
            return MapMessage.errorMessage("语文故事模板名称不能为空");
        }
        if (StringUtils.isBlank(template.getReportTitle())) {
            return MapMessage.errorMessage("语文故事报告标题不能为空");
        }
        if (StringUtils.isBlank(template.getCoverImgUrl())) {
            return MapMessage.errorMessage("语文故事图片不能为空");
        }
        if (StringUtils.isBlank(template.getIntroduction())) {
            return MapMessage.errorMessage("语文故事描述不能为空");
        }
        //拓展学习
        List<StudyModel> modelList = new ArrayList<>();

        Map<String, Object> expoundMap = JsonUtils.fromJson(expoundInfoStr);
        ChineseStoryExpandStudyModelContent expoundModelContent = null;
        if (MapUtils.isNotEmpty(expoundMap)) {
            expoundModelContent = JsonUtils.fromJson(expoundInfoStr, ChineseStoryExpandStudyModelContent.class);
            if (expoundModelContent != null) {
                if (StringUtils.isBlank(expoundModelContent.getCoverImgUrl())) {
                    return MapMessage.errorMessage("拓展学习模块图片不能为空");
                }
                if (CollectionUtils.isEmpty(expoundModelContent.getQuestionIds())) {
                    return MapMessage.errorMessage("趣味练习题目ID不能为空");
                }
                Map<String, NewQuestion> questionMap = questionLoaderClient.loadLatestQuestionByDocIds(expoundModelContent.getQuestionIds());
                Set<String> funMissedQuestionIds = expoundModelContent.getQuestionIds().stream().filter(p -> questionMap.get(p) == null)
                        .collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(funMissedQuestionIds)) {
                    return MapMessage.errorMessage("趣味练习中如下题目ID错误: " + JsonUtils.toJson(funMissedQuestionIds));
                }
                String expoundTitle = SafeConverter.toString(expoundMap.get("expound_title"));
                if (StringUtils.isBlank(expoundTitle)) {
                    return MapMessage.errorMessage("拓展学习模块标题不能为空");
                }
                //json直接转换的存在id=""的情况
                if (StringUtils.isBlank(expoundModelContent.getId())) {
                    expoundModelContent.setId(null);
                }
                StudyModel expoundModel = new StudyModel();
                expoundModel.setModelType(StudyModelType.CHINESESTORYEXPAND);
                expoundModel.setTitle(expoundTitle);
                expoundModel.setTypeId(expoundModelContent.getId());
                expoundModel.setTypeImg(expoundModelContent.getCoverImgUrl());
                modelList.add(expoundModel);
            }
        }

        //知识点
        Map<String, Object> knowledgeMap = JsonUtils.fromJson(knowledgeInfoStr);
        ChineseStoryKnowledgeModelContent chineseStoryKnowledgeModelContent = null;
        if (MapUtils.isNotEmpty(knowledgeMap)) {
            chineseStoryKnowledgeModelContent = JsonUtils.fromJson(knowledgeInfoStr, ChineseStoryKnowledgeModelContent.class);
            if (chineseStoryKnowledgeModelContent != null) {
                if (StringUtils.isBlank(chineseStoryKnowledgeModelContent.getCoverImgUrl())) {
                    return MapMessage.errorMessage("知识点图片不能为空");
                }
                if (CollectionUtils.isEmpty(chineseStoryKnowledgeModelContent.getKnowledgePointList())) {
                    return MapMessage.errorMessage("知识点内容不能为空");
                }
                for (ChineseStoryKnowledgeModelContent.KnowledgePoint knowledgePoint : chineseStoryKnowledgeModelContent.getKnowledgePointList()) {
                    if (knowledgePoint.getAudioSeconds() == null || knowledgePoint.getAudioSeconds() == 0) {
                        return MapMessage.errorMessage("音频时长不能为空");
                    }
                    if (StringUtils.isBlank(knowledgePoint.getAudioUrl())) {
                        return MapMessage.errorMessage("音频链接不能为空");
                    }
                    if (StringUtils.isBlank(knowledgePoint.getBgImgUrl())) {
                        return MapMessage.errorMessage("背景图不能为空");
                    }
                    if (StringUtils.isBlank(knowledgePoint.getContent())) {
                        return MapMessage.errorMessage("内容不能为空");
                    }
                    if (StringUtils.isBlank(knowledgePoint.getTitle())) {
                        return MapMessage.errorMessage("知识点标题不能为空");
                    }
                }
                if (MapUtils.isEmpty(knowledgeMap)) {
                    return MapMessage.errorMessage("知识点模块不能为空");
                }
                String knowledgeTitle = SafeConverter.toString(knowledgeMap.get("knowledge_title"));
                if (StringUtils.isBlank(knowledgeTitle)) {
                    return MapMessage.errorMessage("知识点模块标题不能为空");
                }
                //json直接转换的存在id=""的情况
                if (StringUtils.isBlank(chineseStoryKnowledgeModelContent.getId())) {
                    chineseStoryKnowledgeModelContent.setId(null);
                }
                StudyModel knowledgeModel = new StudyModel();
                knowledgeModel.setModelType(StudyModelType.CHINESESTORYKNOWLEDGE);
                knowledgeModel.setTitle(knowledgeTitle);
                knowledgeModel.setTypeId(chineseStoryKnowledgeModelContent.getId());
                knowledgeModel.setTypeImg(chineseStoryKnowledgeModelContent.getCoverImgUrl());
                modelList.add(knowledgeModel);
            }
        }
        //故事
        Map<String, Object> storyMap = JsonUtils.fromJson(storyInfoStr);
        ChineseStoryModelContent chineseStoryModelContent = null;
        if (MapUtils.isNotEmpty(storyMap)) {
            chineseStoryModelContent = JsonUtils.fromJson(storyInfoStr, ChineseStoryModelContent.class);
            if (chineseStoryModelContent != null) {
                if (StringUtils.isBlank(chineseStoryModelContent.getCoverImgUrl())) {
                    return MapMessage.errorMessage("故事图片不能为空");
                }
                if (chineseStoryModelContent.getVideoSeconds() == null || chineseStoryModelContent.getVideoSeconds() == 0) {
                    return MapMessage.errorMessage("故事视频时长不能为0");
                }
                if (StringUtils.isBlank(chineseStoryModelContent.getVideoUrl())) {
                    return MapMessage.errorMessage("故事视频不能为空");
                }
                String storyTitle = SafeConverter.toString(storyMap.get("story_title"));
                if (StringUtils.isBlank(storyTitle)) {
                    return MapMessage.errorMessage("故事模块标题不能为空");
                }
                //json直接转换的存在id=""的情况
                if (StringUtils.isBlank(chineseStoryModelContent.getId())) {
                    chineseStoryModelContent.setId(null);
                }
                StudyModel storyModel = new StudyModel();
                storyModel.setModelType(StudyModelType.CHINESESTORY);
                storyModel.setTitle(storyTitle);
                storyModel.setTypeId(chineseStoryModelContent.getId());
                storyModel.setTypeImg(chineseStoryModelContent.getCoverImgUrl());
                modelList.add(storyModel);
            }
        }


//        //句子跟读
//        ChineseStoryFollowReadModelContent chineseStoryFollowReadModelContent = JsonUtils.fromJson(followReadInfoStr, ChineseStoryFollowReadModelContent.class);
//        if (chineseStoryFollowReadModelContent != null) {
//            if (StringUtils.isBlank(chineseStoryFollowReadModelContent.getCoverImgUrl())) {
//                return MapMessage.errorMessage("句子跟读图片不能为空");
//            }
//            if (CollectionUtils.isEmpty(chineseStoryFollowReadModelContent.getSentenceList())) {
//                return MapMessage.errorMessage("句子跟读句子不能为空");
//            }
//            chineseStoryFollowReadModelContent.getSentenceList().stream().filter(p -> p.getRank() == null).forEach(p -> p.setRank(0));
//            chineseStoryFollowReadModelContent.getSentenceList().sort(Comparator.comparing(ChineseStoryFollowReadModelContent.ReciteSentence::getRank));
//            Map<String, Object> followReadMap = JsonUtils.fromJson(followReadInfoStr);
//            if (MapUtils.isEmpty(followReadMap)) {
//                return MapMessage.errorMessage("句子跟读模块不能为空");
//            }
//            String followReadTitle = SafeConverter.toString(followReadMap.get("follow_read_title"));
//            if (StringUtils.isBlank(followReadTitle)) {
//                return MapMessage.errorMessage("知识点模块标题不能为空");
//            }
//            //json直接转换的存在id=""的情况
//            if (StringUtils.isBlank(chineseStoryFollowReadModelContent.getId())) {
//                chineseStoryFollowReadModelContent.setId(null);
//            }
//            ChineseStoryLessonTemplate.StudyModel followReadModel = new ChineseStoryLessonTemplate.StudyModel();
//            followReadModel.setModelType(StudyModelType.CHINESESTORYFOLLOWREAD);
//            followReadModel.setTitle(followReadTitle);
//            followReadModel.setTypeId(chineseStoryFollowReadModelContent.getId());
//            modelList.add(followReadModel);
//        }
        List<StudyModelType> modelTypes = new ArrayList<>();
        if (StringUtils.isNotBlank(rankInfo)) {
            try {
                modelTypes = JsonUtils.fromJsonToList(rankInfo, StudyModelType.class);
            } catch (Exception e) {
                return MapMessage.errorMessage("排序信息有误，请联系开发人员");
            }
        }
        List<StudyModelType> finalModelTypes = modelTypes;
        modelList = modelList.stream().sorted(Comparator.comparingInt(o -> finalModelTypes.indexOf(o.getModelType()))).collect(Collectors.toList());
        template.setStudyModelList(modelList);
        return MapMessage.successMessage()
                .add("chinese_story_template", template)
                .add("expound_model_content", expoundModelContent)
//                .add("follow_read_model_content", chineseStoryFollowReadModelContent)
                .add("story_model_content", chineseStoryModelContent)
                .add("knowledge_model_content", chineseStoryKnowledgeModelContent);
    }

}
