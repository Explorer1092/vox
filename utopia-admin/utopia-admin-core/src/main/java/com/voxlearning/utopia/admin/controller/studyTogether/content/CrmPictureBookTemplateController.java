package com.voxlearning.utopia.admin.controller.studyTogether.content;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.galaxy.service.studycourse.api.CrmLessonTemplateLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmLessonTemplateService;
import com.voxlearning.galaxy.service.studycourse.api.CrmModelContentLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmModelContentService;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseBlackWidowServiceClient;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.entity.PictureBookSentence;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSpu;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.PictureBookExpoundModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.PictureBookFunModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.PictureBookSentenceModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.PictureBookWordModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.PictureBookLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.StudyModel;
import com.voxlearning.galaxy.service.studycourse.api.mapper.PictureBookLessonTemplateLogMapper;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.galaxy.service.studycourse.constant.StudyModelType;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.admin.controller.studyTogether.AbstractStudyTogetherController;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.CourseSeries;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.CourseSpu;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2018-9-11
 */
@Controller
@RequestMapping(value = "opmanager/studyTogether/template/picturebook/")
@Slf4j
public class CrmPictureBookTemplateController extends AbstractAdminController {
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
    private PictureBookLoaderClient pictureBookLoaderClient;

    @Inject
    private StudyCourseBlackWidowServiceClient studyCourseBlackWidowServiceClient;

    @Inject
    protected StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    /**
     * 绘本模板列表
     */
    @RequestMapping(value = "picture_book_list.vpage", method = RequestMethod.GET)
    public String templateList(Model model) {
        Integer page = getRequestInt("page", 1);
        Long templateId = getRequestLong("template_id");
        String templateName = getRequestString("template_name");
        String createUser = getRequestString("create_user");
        Long spuId = getRequestLong("template_spu_id");

        //前端输入回显
        model.addAttribute("page", page);
        model.addAttribute("template_id", templateId != 0 ? templateId : "");
        model.addAttribute("template_name", templateName);
        model.addAttribute("create_user", createUser);
        model.addAttribute("template_spu_id", spuId != 0 ? spuId : "");
        model.addAttribute("course_type", 2);


        List<PictureBookLessonTemplate> templateList = new ArrayList<>();
        if (templateId != 0) {
            PictureBookLessonTemplate template = lessonTemplateLoader.loadPictureBookTemplate(templateId);
            if (template != null) {
                templateList.add(template);
            }
        } else {
            templateList = lessonTemplateLoader.loadAllPictureBookTemplate();
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
        Page<PictureBookLessonTemplate> templatePage = PageableUtils.listToPage(templateList, new PageRequest(page - 1, 10));
        List<PictureBookLessonTemplate> returnTemplateList = templatePage.getContent();

        List<Map<String, Object>> mapList = new ArrayList<>();
        returnTemplateList.forEach(p -> {
            Map<String, Object> map = new HashMap<>();
            //id
            map.put("id", p.getId());
            //英语绘本模板名称
            map.put("name", p.getName());
            //spuId
            map.put("spu_id", p.getSpuId());
            //创建者
            map.put("create", p.getCreateUser());
            mapList.add(map);
        });

        model.addAttribute("total_page", templatePage.getTotalPages());
        model.addAttribute("picture_book_template_list", mapList);
        return "/opmanager/studyTogether/temlate/templatelist";
    }


    @RequestMapping(value = "picture_book_template_info.vpage", method = RequestMethod.GET)
    public String pictureBookInfo(Model model) {
        Long templateId = getRequestLong("template_id");

        //是否允许编辑
        model.addAttribute("edit", getRequestInt("edit"));
        model.addAttribute("admin_user", getCurrentAdminUser().getAdminUserName());

        //所有绘本的spu
        List<CourseStructSpu> spuList = studyCourseStructLoaderClient.loadAllCourseStructSpu()
                .stream().filter(t -> t.getParent().getCourseType() == 2).collect(Collectors.toList());
        model.addAttribute("spu_list", spuList);

        if (templateId == 0) {
            return "/opmanager/studyTogether/temlate/picturebookinfo";
        }
        PictureBookLessonTemplate template = lessonTemplateLoader.loadPictureBookTemplate(templateId);
        if (template == null) {
            getAlertMessageManager().addMessageError("您要处理的绘本模板不存在");
            return "/opmanager/studyTogether/temlate/picturebookinfo";
        }
        if (CollectionUtils.isNotEmpty(template.getStudyModelList())) {
            template.getStudyModelList().forEach(p -> {
                if (p.getModelType() == StudyModelType.ENGEXPOUND) {
                    PictureBookExpoundModelContent content = modelContentLoader.loadPictureBookExpoundModelContent(p.getTypeId());
                    model.addAttribute("expound_content", content);
                    model.addAttribute("expound_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.ENGWORD) {
                    PictureBookWordModelContent content = modelContentLoader.loadPictureBookWordModelContent(p.getTypeId());
                    model.addAttribute("word_content", content);
                    model.addAttribute("word_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.ENGSENTENCE) {
                    PictureBookSentenceModelContent content = modelContentLoader.loadPictureBookSentenceModelContent(p.getTypeId());
                    model.addAttribute("sentence_content", content);
                    model.addAttribute("sentence_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.ENGFUN) {
                    PictureBookFunModelContent content = modelContentLoader.loadPictureBookFunModelContent(p.getTypeId());
                    model.addAttribute("fun_content", content);
                    model.addAttribute("fun_title", p.getTitle());
                }
            });
        }

        model.addAttribute("template", template);
        model.addAttribute("cdn_host", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")));
        return "/opmanager/studyTogether/temlate/picturebookinfo";
    }

    @RequestMapping(value = "save_picture_book_template.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage savePictureBookTemplate() {
        MapMessage mapMessage = validateAndGeneratePictureBookInfo();
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }
        //日志支持
        PictureBookLessonTemplateLogMapper oldTemplateMapper = new PictureBookLessonTemplateLogMapper();
        PictureBookLessonTemplateLogMapper newTemplateMapper = new PictureBookLessonTemplateLogMapper();
        PictureBookLessonTemplate template = (PictureBookLessonTemplate) (mapMessage.get("picture_book_template"));
        PictureBookExpoundModelContent expoundModelContent = (PictureBookExpoundModelContent) (mapMessage.get("expound_model_content"));
        if (StringUtils.isNotBlank(expoundModelContent.getId())) {
            PictureBookExpoundModelContent oldExpoundModel = modelContentLoader.loadPictureBookExpoundModelContent(expoundModelContent.getId());
            oldTemplateMapper.setExpoundModelContent(oldExpoundModel);
        }
        PictureBookWordModelContent wordModelContent = (PictureBookWordModelContent) (mapMessage.get("word_model_content"));
        if (StringUtils.isNotBlank(wordModelContent.getId())) {
            PictureBookWordModelContent oldWordModel = modelContentLoader.loadPictureBookWordModelContent(wordModelContent.getId());
            oldTemplateMapper.setWordModelContent(oldWordModel);
        }
        PictureBookSentenceModelContent sentenceModelContent = (PictureBookSentenceModelContent) (mapMessage.get("sentence_model_content"));
        if (StringUtils.isNotBlank(sentenceModelContent.getId())) {
            PictureBookSentenceModelContent oldSentenceModel = modelContentLoader.loadPictureBookSentenceModelContent(sentenceModelContent.getId());
            oldTemplateMapper.setSentenceModelContent(oldSentenceModel);
        }
        PictureBookFunModelContent funModelContent = (PictureBookFunModelContent) (mapMessage.get("fun_model_content"));
        if (StringUtils.isNotBlank(funModelContent.getId())) {
            PictureBookFunModelContent oldFunModel = modelContentLoader.loadPictureBookFunModelContent(funModelContent.getId());
            oldTemplateMapper.setFunModelContent(oldFunModel);
        }
        PictureBookExpoundModelContent updateExpoundModelContent = modelContentService.savePictureBookExpoundModelContent(expoundModelContent);
        newTemplateMapper.setExpoundModelContent(updateExpoundModelContent);
        PictureBookWordModelContent updateWordModelContent = modelContentService.savePictureBookWordModelContent(wordModelContent);
        newTemplateMapper.setWordModelContent(updateWordModelContent);
        PictureBookSentenceModelContent updateSentenceModelContent = modelContentService.savePictureBookSentenceModelContent(sentenceModelContent);
        newTemplateMapper.setSentenceModelContent(updateSentenceModelContent);
        PictureBookFunModelContent updateFunModelContent = modelContentService.savePictureBookFunModelContent(funModelContent);
        newTemplateMapper.setFunModelContent(updateFunModelContent);
        template.getStudyModelList().forEach(p -> {
            if (p.getModelType() == StudyModelType.ENGEXPOUND) {
                p.setTypeId(updateExpoundModelContent.getId());
            } else if (p.getModelType() == StudyModelType.ENGWORD) {
                p.setTypeId(updateWordModelContent.getId());
            } else if (p.getModelType() == StudyModelType.ENGSENTENCE) {
                p.setTypeId(updateSentenceModelContent.getId());
            } else if (p.getModelType() == StudyModelType.ENGFUN) {
                p.setTypeId(updateFunModelContent.getId());
            }
        });
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (template.getId() == null) {
            template.setCreateUser(adminUser.getAdminUserName());
        } else {
            PictureBookLessonTemplate oldTemplate = lessonTemplateLoader.loadPictureBookTemplate(template.getId());
            oldTemplateMapper.setLessonTemplate(oldTemplate);
        }
        template.setUpdateUser(adminUser.getAdminUserName());
        PictureBookLessonTemplate updatedTemplate = lessonTemplateService.savePictureBookTemplate(template);
        newTemplateMapper.setLessonTemplate(updatedTemplate);
        //实时同步给php
        lessonTemplateService.syncPictureBookTemplateToPhp(template, expoundModelContent, wordModelContent, sentenceModelContent, funModelContent);
        //CRM 修改日志
        studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldTemplateMapper, newTemplateMapper, adminUser.getAdminUserName(), ChangeLogType.PictureBookTemplate, updatedTemplate.getId().toString());
        return MapMessage.successMessage().add("id", updatedTemplate.getId());
    }

    private MapMessage validateAndGeneratePictureBookInfo() {
        //基本信息
        String templateInfoStr = getRequestString("template_info");
        //名师精讲
        String expoundInfoStr = getRequestString("expound_info");
        //名句赏析
        String wordInfoStr = getRequestString("word_info");
        //每日朗读
        String sentenceInfoStr = getRequestString("sentence_info");
        //趣味练习
        String funInfoStr = getRequestString("fun_info");
        //英语绘本模版
        PictureBookLessonTemplate template = JsonUtils.fromJson(templateInfoStr, PictureBookLessonTemplate.class);
        if (template == null) {
            return MapMessage.errorMessage("绘本模板基本信息不能为空");
        }
        if (template.getSpuId() == null) {
            return MapMessage.errorMessage("绘本模板SPU_ID不能为空");
        }
        if (StringUtils.isBlank(template.getName())) {
            return MapMessage.errorMessage("绘本模板名称不能为空");
        }
        if (StringUtils.isBlank(template.getTitle())) {
            return MapMessage.errorMessage("绘本模板标题不能为空");
        }
        if (StringUtils.isBlank(template.getGoalDetail())) {
            return MapMessage.errorMessage("绘本模板的目标描述不能为空");
        }
        if (StringUtils.isBlank(template.getPictureBookId())) {
            //绘本ID为空的时候在判断其他信息不能为空
            if (StringUtils.isBlank(template.getCoverImgUrl())) {
                return MapMessage.errorMessage("绘本模板-课节目标封面不能为空");
            }
            if (CollectionUtils.isEmpty(template.getSentenceList())) {
                return MapMessage.errorMessage("绘本模板-课节目标句子列表不能为空");
            }
        } else {
            PictureBookPlus pictureBookPlus = pictureBookLoaderClient.loadPictureBookPlusByIds(Collections.singletonList(template.getPictureBookId())).get(template.getPictureBookId());
            if (pictureBookPlus == null) {
                return MapMessage.errorMessage("绘本模板-课节目标所填绘本ID不存在");
            }
        }
        template.getSentenceList().stream().filter(p -> p.getRank() == null).forEach(p -> p.setRank(0));
        template.getSentenceList().sort(Comparator.comparing(PictureBookSentence::getRank));
        //绘本名师精讲
        PictureBookExpoundModelContent expoundModelContent = JsonUtils.fromJson(expoundInfoStr, PictureBookExpoundModelContent.class);
        if (expoundModelContent == null) {
            return MapMessage.errorMessage("绘本名师精讲内容不能空");
        }
        if (StringUtils.isBlank(expoundModelContent.getVideoUrl())) {
            return MapMessage.errorMessage("绘本名师精讲视频地址不能空");
        }
        if (expoundModelContent.getVideoSeconds() == null || expoundModelContent.getVideoSeconds() <= 0) {
            return MapMessage.errorMessage("绘本名师精讲视频时长错误");
        }
        //json直接转换的存在id=""的情况
        if (StringUtils.isBlank(expoundModelContent.getId())) {
            expoundModelContent.setId(null);
        }
        Map<String, Object> expoundMap = JsonUtils.fromJson(expoundInfoStr);
        if (MapUtils.isEmpty(expoundMap)) {
            return MapMessage.errorMessage("绘本名师精讲不能为空");
        }
        String expoundTitle = SafeConverter.toString(expoundMap.get("expound_title"));
        if (StringUtils.isBlank(expoundTitle)) {
            return MapMessage.errorMessage("绘本名师精讲模块标题不能为空");
        }
        List<StudyModel> modelList = new ArrayList<>();
        StudyModel expoundModel = new StudyModel();
        expoundModel.setModelType(StudyModelType.ENGEXPOUND);
        expoundModel.setTitle(expoundTitle);
        expoundModel.setTypeId(expoundModelContent.getId());
        modelList.add(expoundModel);
        //绘本核心单词练习
        PictureBookWordModelContent wordModelContent = JsonUtils.fromJson(wordInfoStr, PictureBookWordModelContent.class);
        if (wordModelContent == null) {
            return MapMessage.errorMessage("核心单词练习内容不能为空");
        }
        if (CollectionUtils.isEmpty(wordModelContent.getQuestionIds())) {
            return MapMessage.errorMessage("核心单词练习题目ID不能为空");
        }
        if (CollectionUtils.isEmpty(wordModelContent.getWords())) {
            return MapMessage.errorMessage("核心单词练习单词列表不能为空");
        }
        Map<String, NewQuestion> wordQuestionMap = questionLoaderClient.loadLatestQuestionByDocIds(wordModelContent.getQuestionIds());
        Set<String> wordMissedQuestionIds = wordModelContent.getQuestionIds().stream().filter(p -> wordQuestionMap.get(p) == null)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(wordMissedQuestionIds)) {
            return MapMessage.errorMessage("核心单词练习中如下题目ID错误: " + JsonUtils.toJson(wordMissedQuestionIds));
        }
        //json直接转换的存在id=""的情况
        if (StringUtils.isBlank(wordModelContent.getId())) {
            wordModelContent.setId(null);
        }
        Map<String, Object> wordInfoMap = JsonUtils.fromJson(wordInfoStr);
        if (MapUtils.isEmpty(wordInfoMap)) {
            return MapMessage.errorMessage("核心单词练习内容不能为空");
        }
        String wordTitle = SafeConverter.toString(wordInfoMap.get("word_title"));
        if (StringUtils.isBlank(wordTitle)) {
            return MapMessage.errorMessage("核心单词练习模块标题不能为空");
        }
        wordModelContent.getWords().stream().filter(p -> p.getRank() == null).forEach(p -> p.setRank(0));
        wordModelContent.getWords().sort(Comparator.comparing(PictureBookWordModelContent.Word::getRank));
        StudyModel wordModel = new StudyModel();
        wordModel.setModelType(StudyModelType.ENGWORD);
        wordModel.setTitle(wordTitle);
        wordModel.setTypeId(wordModelContent.getId());
        modelList.add(wordModel);
        //重点句子跟读
        PictureBookSentenceModelContent sentenceModelContent = JsonUtils.fromJson(sentenceInfoStr, PictureBookSentenceModelContent.class);
        if (sentenceModelContent == null) {
            return MapMessage.errorMessage("重点句子跟读模块不能为空");
        }
        if (CollectionUtils.isEmpty(sentenceModelContent.getQuestionIds())) {
            return MapMessage.errorMessage("重点句子跟读模块题目ID不能为空");
        }
        Map<String, NewQuestion> sentenceQuestionMap = questionLoaderClient.loadLatestQuestionByDocIds(sentenceModelContent.getQuestionIds());
        Set<String> sentenceMissedQuestionIds = sentenceModelContent.getQuestionIds().stream().filter(p -> sentenceQuestionMap.get(p) == null)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(sentenceMissedQuestionIds)) {
            return MapMessage.errorMessage("重点句子跟读中如下题目ID错误: " + JsonUtils.toJson(sentenceMissedQuestionIds));
        }
        //json直接转换的存在id=""的情况
        if (StringUtils.isBlank(sentenceModelContent.getId())) {
            sentenceModelContent.setId(null);
        }
        Map<String, Object> sentenceMap = JsonUtils.fromJson(sentenceInfoStr);
        if (MapUtils.isEmpty(sentenceMap)) {
            return MapMessage.errorMessage("重点句子跟读模块不能为空");
        }
        String sentenceTitle = SafeConverter.toString(sentenceMap.get("sentence_title"));
        if (StringUtils.isBlank(sentenceTitle)) {
            return MapMessage.errorMessage("重点句子跟读模块标题不能为空");
        }
        StudyModel sentenceModel = new StudyModel();
        sentenceModel.setModelType(StudyModelType.ENGSENTENCE);
        sentenceModel.setTitle(sentenceTitle);
        sentenceModel.setTypeId(sentenceModelContent.getId());
        modelList.add(sentenceModel);
        //每日绘本配音
        PictureBookFunModelContent funModelContent = JsonUtils.fromJson(funInfoStr, PictureBookFunModelContent.class);
        if (funModelContent == null) {
            return MapMessage.errorMessage("每日绘本配音模块内容不能为空");
        }
        if (StringUtils.isBlank(funModelContent.getPictureBookId())) {
            //绘本ID为空的时候在判断其他信息不能为空
            if (StringUtils.isBlank(funModelContent.getCoverImgUrl())) {
                return MapMessage.errorMessage("每日绘本配音模块封面不能为空");
            }
            if (StringUtils.isBlank(funModelContent.getGeneralTimeString())) {
                return MapMessage.errorMessage("每日绘本配音模块预计时长不能为空");
            }
            if (funModelContent.getKeyWordCount() == null || funModelContent.getKeyWordCount() <= 0) {
                return MapMessage.errorMessage("每日绘本配音模块关键词数量错误");
            }
            if (funModelContent.getSentenceCount() == null || funModelContent.getSentenceCount() <= 0) {
                return MapMessage.errorMessage("每日绘本配音模块句子数量错误");
            }
            if (CollectionUtils.isEmpty(funModelContent.getSentenceList())) {
                return MapMessage.errorMessage("每日绘本配音模块句子列表不能为空");
            }
            funModelContent.getSentenceList().stream().filter(p -> p.getRank() == null).forEach(p -> p.setRank(0));
            funModelContent.getSentenceList().sort(Comparator.comparing(PictureBookSentence::getRank));
        } else {
            PictureBookPlus pictureBookPlus = pictureBookLoaderClient.loadPictureBookPlusByIds(Collections.singletonList(funModelContent.getPictureBookId())).get(funModelContent.getPictureBookId());
            if (pictureBookPlus == null) {
                return MapMessage.errorMessage("绘本模板-每日配音所填绘本ID不存在");
            }
        }
        //json直接转换的存在id=""的情况
        if (StringUtils.isBlank(funModelContent.getId())) {
            funModelContent.setId(null);
        }
        Map<String, Object> funMap = JsonUtils.fromJson(funInfoStr);
        if (MapUtils.isEmpty(funMap)) {
            return MapMessage.errorMessage("每日绘本配音模块内容不能为空");
        }
        String funTitle = SafeConverter.toString(funMap.get("fun_title"));
        if (StringUtils.isBlank(funTitle)) {
            return MapMessage.errorMessage("每日绘本配音模块标题不能为空");
        }
        StudyModel funModel = new StudyModel();
        funModel.setModelType(StudyModelType.ENGFUN);
        funModel.setTitle(funTitle);
        funModel.setTypeId(funModelContent.getId());
        modelList.add(funModel);
        template.setStudyModelList(modelList);
        return MapMessage.successMessage()
                .add("picture_book_template", template)
                .add("expound_model_content", expoundModelContent)
                .add("word_model_content", wordModelContent)
                .add("sentence_model_content", sentenceModelContent)
                .add("fun_model_content", funModelContent);
    }
}
