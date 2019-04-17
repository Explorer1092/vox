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
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSpu;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.*;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.ChineseReadingLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.StudyModel;
import com.voxlearning.galaxy.service.studycourse.api.mapper.ChineseReadingLessonTemplateLogMapper;
import com.voxlearning.galaxy.service.studycourse.api.mapper.ChineseReadingMindMapMapper;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.galaxy.service.studycourse.constant.StudyModelType;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.admin.controller.studyTogether.AbstractStudyTogetherController;
import com.voxlearning.utopia.admin.util.ExcelUtil;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2018-9-11
 */
@Controller
@RequestMapping(value = "opmanager/studyTogether/template/chinesereading")
@Slf4j
public class CrmChineseReadingTemplateController extends AbstractAdminController {
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

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    @Inject
    protected StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    protected List<String> getAllLessonId(){
        return studyCourseStructLoaderClient.loadAllCourseStructSku().stream()
                .map(t -> SafeConverter.toString(t.getId())).sorted(Comparator.comparing(SafeConverter::toString)).collect(Collectors.toList());
    }

    /**
     * 语文阅读模板列表
     */
    @RequestMapping(value = "chinese_reading_list.vpage", method = RequestMethod.GET)
    public String getChineseReadingTemplateList(Model model) {
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
        model.addAttribute("course_type", 3);
        List<ChineseReadingLessonTemplate> templateList = new ArrayList<>();
        if (templateId != 0) {
            ChineseReadingLessonTemplate template = lessonTemplateLoader.loadChineseReadLessonTemplate(templateId);
            if (template != null) {
                templateList.add(template);
            }
        } else {
            templateList = lessonTemplateLoader.loadAllChineseReadLessonTemplate();
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
        Page<ChineseReadingLessonTemplate> templatePage = PageableUtils.listToPage(templateList, new PageRequest(page - 1, 10));
        List<ChineseReadingLessonTemplate> returnTemplateList = templatePage.getContent();

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
        model.addAttribute("chinese_reading_template_list", mapList);
        return "/opmanager/studyTogether/temlate/templatelist";
    }

    @RequestMapping(value = "chinese_reading_template_info.vpage", method = RequestMethod.GET)
    public String getChineseReadingTemplateInfo(Model model) {
        Long templateId = getRequestLong("template_id");

        //是否允许编辑
        model.addAttribute("edit", getRequestInt("edit"));
        model.addAttribute("admin_user", getCurrentAdminUser().getAdminUserName());
        model.addAttribute("cdn_host", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")));
        //全部的spu列表
        List<CourseStructSpu> spuList = studyCourseStructLoaderClient.loadAllCourseStructSpu();
        //所有语文阅读的spu
        spuList = spuList.stream().filter(p -> p.getParent().getCourseType() == 3).collect(Collectors.toList());
        model.addAttribute("spu_list", spuList);

        if (templateId == 0) {
            return "/opmanager/studyTogether/temlate/chinesereadinginfo";
        }
        ChineseReadingLessonTemplate template = lessonTemplateLoader.loadChineseReadLessonTemplate(templateId);
        if (template == null) {
            getAlertMessageManager().addMessageError("您要处理的语文阅读模板不存在");
            return "/opmanager/studyTogether/temlate/chinesereadinginfo";
        }
        if (CollectionUtils.isNotEmpty(template.getStudyModelList())) {
            template.getStudyModelList().forEach(p -> {
                if (p.getModelType() == StudyModelType.PICBOOKREADING) {
                    ChineseReadingPictureBookReadingModelContent content = modelContentLoader.loadChinesePictureBookReadingModelContent(p.getTypeId());
                    model.addAttribute("picture_book_reading_content", content);
                    model.addAttribute("picture_book_reading_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.WORDEXPOUND) {
                    ChineseReadingWordModelContent content = modelContentLoader.loadChineseWordModelContent(p.getTypeId());
                    model.addAttribute("word_content", content);
                    model.addAttribute("word_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.CHINESEFAMOUSBOOK) {
                    ChineseReadingFamousBookModelContent content = modelContentLoader.loadChineseReadingFamousBookModelContent(p.getTypeId());
                    model.addAttribute("famous_book_content", content);
                    model.addAttribute("famous_book_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.CHINESEAPPRECIATE) {
                    ChineseReadingAppreciateModelContent content = modelContentLoader.loadChineseReadingAppreciateModelContent(p.getTypeId());
                    model.addAttribute("appreciate_content", content);
                    model.addAttribute("appreciate_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.MINDMAP) {
                    ChineseReadingMindMapModelContent content = modelContentLoader.loadChineseMindMapModelContent(p.getTypeId());
                    model.addAttribute("mind_map_content", content);
                    model.addAttribute("mind_map_title", p.getTitle());
                    model.addAttribute("mind_map_json", content == null || content.getMindMap() == null ? "" : JsonUtils.toJson(content.getMindMap()));
                } else if (p.getModelType() == StudyModelType.CHINESEFOLLOWREADING) {
                    ChineseReadingFollowReadingModelContent content = modelContentLoader.loadChineseReadingFollowReadingModelContent(p.getTypeId());
                    //TODO 参数名得改
                    model.addAttribute("follow_reading_content", content);
                    model.addAttribute("follow_reading_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.CHINESEEXPANDPRACTICE) {
                    ChineseReadingExpandPracticeModelContent content = modelContentLoader.loadChineseExpandPracticeModelContent(p.getTypeId());
                    model.addAttribute("expand_practice_content", content);
                    model.addAttribute("expand_practice_title", p.getTitle());
                }
            });
        }
        model.addAttribute("template", template);
        return "/opmanager/studyTogether/temlate/chinesereadinginfo";
    }

    @RequestMapping(value = "save_chinese_reading_template.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveChineseReadingTemplate() {
        MapMessage mapMessage = validateAndGenerateChineseReadingInfo();
        if (!mapMessage.isSuccess()) {
            return MapMessage.errorMessage(mapMessage.getInfo());
        }
        ChineseReadingLessonTemplateLogMapper oldTemplateMapper = new ChineseReadingLessonTemplateLogMapper();
        ChineseReadingLessonTemplateLogMapper newTemplateMapper = new ChineseReadingLessonTemplateLogMapper();
        ChineseReadingLessonTemplate template = (ChineseReadingLessonTemplate) mapMessage.get("chinese_reading_template");
        Integer usePictureBook = SafeConverter.toInt(mapMessage.get("user_picture_book"));

        ChineseReadingPictureBookReadingModelContent updatePictureBookModelContent = null;
        ChineseReadingWordModelContent updateWordModelContent = null;
        ChineseReadingFamousBookModelContent updateFamousBookModelContent = null;
        ChineseReadingAppreciateModelContent updateAppreciateModelContent = null;
        ChineseReadingExpandPracticeModelContent updateExpandPracticeModelContent = null;
        ChineseReadingFollowReadingModelContent updateFollowReadingModelContent = null;

        if (usePictureBook == 1) {
            //绘本阅读
            ChineseReadingPictureBookReadingModelContent pictureBookReadingModelContent = (ChineseReadingPictureBookReadingModelContent) mapMessage.get("picture_book_reading_model_content");
            if (StringUtils.isNotBlank(pictureBookReadingModelContent.getId())) {
                ChineseReadingPictureBookReadingModelContent oldPictureBookModelContent = modelContentLoader.loadChinesePictureBookReadingModelContent(pictureBookReadingModelContent.getId());
                oldTemplateMapper.setReadingModelContent(oldPictureBookModelContent);
            }
            updatePictureBookModelContent = modelContentService.saveChinesePictureBookReadingModelContent(pictureBookReadingModelContent);
            newTemplateMapper.setReadingModelContent(updatePictureBookModelContent);
            ChineseReadingPictureBookReadingModelContent finalUpdatePictureBookModelContent = updatePictureBookModelContent;
            template.getStudyModelList().stream().filter(p -> p.getModelType() == StudyModelType.PICBOOKREADING)
                    .forEach(p -> p.setTypeId(finalUpdatePictureBookModelContent.getId()));
            //字词详解
            ChineseReadingWordModelContent wordModelContent = (ChineseReadingWordModelContent) mapMessage.get("word_model_content");
            if (wordModelContent != null && StringUtils.isNotBlank(wordModelContent.getId())) {
                ChineseReadingWordModelContent oldWordModelContent = modelContentLoader.loadChineseWordModelContent(wordModelContent.getId());
                oldTemplateMapper.setWordModelContent(oldWordModelContent);
            }
            updateWordModelContent = modelContentService.saveChineseWordModelContent(wordModelContent);
            newTemplateMapper.setWordModelContent(updateWordModelContent);
            ChineseReadingWordModelContent finalUpdateWordModelContent = updateWordModelContent;
            template.getStudyModelList().stream().filter(p -> p.getModelType() == StudyModelType.WORDEXPOUND)
                    .forEach(p -> p.setTypeId(finalUpdateWordModelContent.getId()));
            //趣味跟读
            ChineseReadingFollowReadingModelContent followReadingModelContent = (ChineseReadingFollowReadingModelContent) mapMessage.get("follow_reading_model_content");
            if (StringUtils.isNotBlank(followReadingModelContent.getId())) {
                ChineseReadingFollowReadingModelContent oldFollowReadingModelContent = modelContentLoader.loadChineseReadingFollowReadingModelContent(followReadingModelContent.getId());
                oldTemplateMapper.setFollowReadingModelContent(oldFollowReadingModelContent);
            }
            updateFollowReadingModelContent = modelContentService.saveChineseReadingFollowReadingModelContent(followReadingModelContent);
            newTemplateMapper.setFollowReadingModelContent(updateFollowReadingModelContent);
            ChineseReadingFollowReadingModelContent finalUpdateFollowReadingModelContent = updateFollowReadingModelContent;
            template.getStudyModelList().stream().filter(p -> p.getModelType() == StudyModelType.CHINESEFOLLOWREADING)
                    .forEach(p -> p.setTypeId(finalUpdateFollowReadingModelContent.getId()));
        } else {
            //名著阅读
            ChineseReadingFamousBookModelContent expoundModelContent = (ChineseReadingFamousBookModelContent) mapMessage.get("famous_book_model_content");
            if (expoundModelContent != null && StringUtils.isNotBlank(expoundModelContent.getId())) {
                ChineseReadingFamousBookModelContent oldExpoundModelContent = modelContentLoader.loadChineseReadingFamousBookModelContent(expoundModelContent.getId());
                oldTemplateMapper.setFamousBookModelContent(oldExpoundModelContent);
            }
            updateFamousBookModelContent = modelContentService.saveChineseReadingFamousBookModelContent(expoundModelContent);
            newTemplateMapper.setFamousBookModelContent(updateFamousBookModelContent);
            ChineseReadingFamousBookModelContent finalUpdateExpoundModelContent = updateFamousBookModelContent;
            template.getStudyModelList().stream().filter(p -> p.getModelType() == StudyModelType.CHINESEFAMOUSBOOK)
                    .forEach(p -> p.setTypeId(finalUpdateExpoundModelContent.getId()));
            //重点赏析
            ChineseReadingAppreciateModelContent appreciateModelContent = (ChineseReadingAppreciateModelContent) mapMessage.get("appreciate_model_content");
            if (appreciateModelContent != null && StringUtils.isNotBlank(appreciateModelContent.getId())) {
                ChineseReadingAppreciateModelContent oldAppreciateModelContent = modelContentLoader.loadChineseReadingAppreciateModelContent(appreciateModelContent.getId());
                oldTemplateMapper.setAppreciateModelContent(oldAppreciateModelContent);
            }
            updateAppreciateModelContent = modelContentService.saveChineseReadingAppreciateModelContent(appreciateModelContent);
            newTemplateMapper.setAppreciateModelContent(updateAppreciateModelContent);
            ChineseReadingAppreciateModelContent finalUpdateAppreciateModelContent = updateAppreciateModelContent;
            template.getStudyModelList().stream().filter(p -> p.getModelType() == StudyModelType.CHINESEAPPRECIATE)
                    .forEach(p -> p.setTypeId(finalUpdateAppreciateModelContent.getId()));
            //拓展练习
            ChineseReadingExpandPracticeModelContent expandPracticeModelContent = (ChineseReadingExpandPracticeModelContent) mapMessage.get("expand_practice_model_content");
            if (StringUtils.isNotBlank(expandPracticeModelContent.getId())) {
                ChineseReadingExpandPracticeModelContent oldFunModelContent = modelContentLoader.loadChineseExpandPracticeModelContent(expandPracticeModelContent.getId());
                oldTemplateMapper.setExpandPracticeModelContent(oldFunModelContent);
            }
            updateExpandPracticeModelContent = modelContentService.saveChineseExpandPracticeModelContent(expandPracticeModelContent);
            newTemplateMapper.setExpandPracticeModelContent(updateExpandPracticeModelContent);
            ChineseReadingExpandPracticeModelContent finalUpdateExpandPracticeModelContent = updateExpandPracticeModelContent;
            template.getStudyModelList().stream().filter(p -> p.getModelType() == StudyModelType.CHINESEEXPANDPRACTICE)
                    .forEach(p -> p.setTypeId(finalUpdateExpandPracticeModelContent.getId()));
        }
        //思维导图
        ChineseReadingMindMapModelContent mindMapModelContent = (ChineseReadingMindMapModelContent) mapMessage.get("mind_map_model_content");
        if (StringUtils.isNotBlank(mindMapModelContent.getId())) {
            ChineseReadingMindMapModelContent oldMindMapModelContent = modelContentLoader.loadChineseMindMapModelContent(mindMapModelContent.getId());
            oldTemplateMapper.setMindMapModelContent(oldMindMapModelContent);
        }
        ChineseReadingMindMapModelContent updateMindMapModelContent = modelContentService.saveChineseMindMapModelContent(mindMapModelContent);
        newTemplateMapper.setMindMapModelContent(updateMindMapModelContent);
        template.getStudyModelList().stream().filter(p -> p.getModelType() == StudyModelType.MINDMAP)
                .forEach(p -> p.setTypeId(updateMindMapModelContent.getId()));
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (template.getId() == null) {
            template.setCreateUser(adminUser.getAdminUserName());
        } else {
            ChineseReadingLessonTemplate oldTemplate = lessonTemplateLoader.loadChineseReadLessonTemplate(template.getId());
            oldTemplateMapper.setLessonTemplate(oldTemplate);
        }
        template.setUpdateUser(adminUser.getAdminUserName());
        ChineseReadingLessonTemplate updateTemplate = lessonTemplateService.saveChineseReadLessonTemplate(template);
        newTemplateMapper.setLessonTemplate(updateTemplate);
        lessonTemplateService.syncChineseReadingLessonTemplateToPhp(updateTemplate, updateAppreciateModelContent, updateFamousBookModelContent, updateExpandPracticeModelContent, updateMindMapModelContent, updatePictureBookModelContent, updateWordModelContent, updateFollowReadingModelContent);
        //CRM 修改日志
        studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldTemplateMapper, newTemplateMapper, adminUser.getAdminUserName(), ChangeLogType.ChineseReading, updateTemplate.getId().toString());
        return MapMessage.successMessage().add("id", updateTemplate.getId());
    }


    public MapMessage validateAndGenerateChineseReadingInfo() {
        //基本信息
        String templateInfoStr = getRequestString("template_info");
        //绘本阅读
        String pictureBookReadingInfoStr = getRequestString("picture_book_reading_info");
        //字词详解
        String wordInfoStr = getRequestString("word_info");
        //名著阅读
        String famousBookInfoStr = getRequestString("famous_book_info");
        //重点赏析
        String appreciateInfoStr = getRequestString("appreciate_info");
        //思维导图
        String mindMapInfoStr = getRequestString("mind_map_info");
        //拓展练习
        String expandPracticeInfoStr = getRequestString("expand_practice_info");
        //趣味跟读
        String followReadingInfoStr = getRequestString("follow_reading_info");

        Long usePictureBook = getRequestLong("use_picture_book");

        MapMessage mapMessage = MapMessage.successMessage();

        //语文阅读模板
        ChineseReadingLessonTemplate template = JsonUtils.fromJson(templateInfoStr, ChineseReadingLessonTemplate.class);
        if (template == null) {
            return MapMessage.errorMessage("语文阅读模板基本信息不能为空");
        }
        if (template.getSpuId() == null) {
            return MapMessage.errorMessage("语文阅读模板SPU不能为空");
        }
        if (StringUtils.isBlank(template.getName())) {
            return MapMessage.errorMessage("语文阅读模板模板名称不能为空");
        }
        if (StringUtils.isBlank(template.getTitle())) {
            return MapMessage.errorMessage("语文阅读模板标题不能为空");
        }
        if (template.getWordCount() == null || template.getWordCount() <= 0) {
            return MapMessage.errorMessage("语文阅读模板字数不能为空");
        }
        if (StringUtils.isBlank(template.getGeneralTimeString())) {
            return MapMessage.errorMessage("语文阅读模板推荐阅读时长不能为空");
        }
        if (template.getKnowledgeCount() == null || template.getKnowledgeCount() == 0) {
            return MapMessage.errorMessage("语文阅读模板知识点数量不能为空");
        }
        if (StringUtils.isBlank(template.getCoverImgUrl())) {
            return MapMessage.errorMessage("语文阅读模板封面图片不能为空");
        }
        if (StringUtils.isBlank(template.getIntroduction())) {
            return MapMessage.errorMessage("语文阅读模板课节导语不能为空");
        }
        //学习模块
        List<StudyModel> studyModelList = new ArrayList<>();
        if (usePictureBook == 1) {
            //绘本阅读
            ChineseReadingPictureBookReadingModelContent pictureBookReadingModelContent = JsonUtils.fromJson(pictureBookReadingInfoStr, ChineseReadingPictureBookReadingModelContent.class);
            if (pictureBookReadingModelContent == null) {
                return MapMessage.errorMessage("语文阅读模板绘本精读模块不能为空");
            }
            if (StringUtils.isBlank(pictureBookReadingModelContent.getVideoUrl())) {
                return MapMessage.errorMessage("语文阅读模板绘本精读模块视频地址不能为空");
            }
            if (pictureBookReadingModelContent.getVideoSeconds() == null || pictureBookReadingModelContent.getVideoSeconds() <= 0) {
                return MapMessage.errorMessage("语文阅读模板绘本精读模块视频时长不能为空");
            }
            //json直接转换的存在id=""的情况
            if (StringUtils.isBlank(pictureBookReadingModelContent.getId())) {
                pictureBookReadingModelContent.setId(null);
            }
            Map<String, Object> pictureBookInfoMap = JsonUtils.fromJson(pictureBookReadingInfoStr);
            if (MapUtils.isEmpty(pictureBookInfoMap)) {
                return MapMessage.errorMessage("语文阅读模板绘本精读模块不能为空");
            }
            String pictureBookTitle = SafeConverter.toString(pictureBookInfoMap.get("picture_book_reading_title"));
            if (StringUtils.isBlank(pictureBookTitle)) {
                return MapMessage.errorMessage("语文阅读模板绘本精读模块标题不能为空");
            }
            StudyModel pictureBookModel = new StudyModel();
            pictureBookModel.setModelType(StudyModelType.PICBOOKREADING);
            pictureBookModel.setTypeId(pictureBookReadingModelContent.getId());
            pictureBookModel.setTitle(pictureBookTitle);
            studyModelList.add(pictureBookModel);
            mapMessage.add("picture_book_reading_model_content", pictureBookReadingModelContent);
            //字词详解
            ChineseReadingWordModelContent wordModelContent = JsonUtils.fromJson(wordInfoStr, ChineseReadingWordModelContent.class);
            if (wordModelContent == null) {
                return MapMessage.errorMessage("语文阅读模板字词详解模块不能为空");
            }
            if (CollectionUtils.isEmpty(wordModelContent.getWordList())) {
                return MapMessage.errorMessage("语文阅读模板字词详解模块字词列表不能为空");
            }
            wordModelContent.getWordList().stream().filter(p -> p.getRank() == null).forEach(p -> p.setRank(0));
            wordModelContent.getWordList().sort(Comparator.comparingInt(ChineseReadingWordModelContent.Word::getRank));
            //json直接转换的存在id=""的情况
            if (StringUtils.isBlank(wordModelContent.getId())) {
                wordModelContent.setId(null);
            }
            Map<String, Object> wordInfoMap = JsonUtils.fromJson(wordInfoStr);
            if (MapUtils.isEmpty(wordInfoMap)) {
                return MapMessage.errorMessage("语文阅读模板字词详解模块不能为空");
            }
            String wordTitle = SafeConverter.toString(wordInfoMap.get("word_title"));
            if (StringUtils.isBlank(wordTitle)) {
                return MapMessage.errorMessage("语文阅读模板字词详解模块标题不能为空");
            }
            StudyModel wordModel = new StudyModel();
            wordModel.setModelType(StudyModelType.WORDEXPOUND);
            wordModel.setTypeId(wordModelContent.getId());
            wordModel.setTitle(wordTitle);
            studyModelList.add(wordModel);
            mapMessage.add("word_model_content", wordModelContent);
            //趣味跟读
            ChineseReadingFollowReadingModelContent followReadingModelContent = JsonUtils.fromJson(followReadingInfoStr, ChineseReadingFollowReadingModelContent.class);
            if (followReadingModelContent == null) {
                return MapMessage.errorMessage("语文阅读模板趣味跟读模块不能为空");
            }
            if (CollectionUtils.isEmpty(followReadingModelContent.getQuestionIdList())) {
                return MapMessage.errorMessage("语文阅读模板趣味跟读模块题目ID不能为空");
            }
            Map<String, NewQuestion> funQuestionMap = questionLoaderClient.loadLatestQuestionByDocIds(followReadingModelContent.getQuestionIdList());
            Set<String> funMissedQuestionIds = followReadingModelContent.getQuestionIdList().stream().filter(p -> funQuestionMap.get(p) == null)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(funMissedQuestionIds)) {
                return MapMessage.errorMessage("语文阅读模板趣味跟读中如下题目ID不存在: " + JsonUtils.toJson(funMissedQuestionIds));
            }
            Set<String> contentTypeErrorQuestionIds = followReadingModelContent.getQuestionIdList().stream().filter(p -> funQuestionMap.get(p).getContentTypeId() == null || (funQuestionMap.get(p).getContentTypeId() != 1010014 && funQuestionMap.get(p).getContentTypeId() != 1010015))
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(contentTypeErrorQuestionIds)) {
                return MapMessage.errorMessage("语文阅读模板趣味跟读中题目类型错误的Id如下: " + JsonUtils.toJson(contentTypeErrorQuestionIds));
            }
            //json直接转换的存在id=""的情况
            if (StringUtils.isBlank(followReadingModelContent.getId())) {
                followReadingModelContent.setId(null);
            }
            Map<String, Object> funInfoMap = JsonUtils.fromJson(followReadingInfoStr);
            if (MapUtils.isEmpty(funInfoMap)) {
                return MapMessage.errorMessage("语文阅读模板趣味跟读模块不能为空");
            }
            String followReadingTitle = SafeConverter.toString(funInfoMap.get("follow_reading_title"));
            if (StringUtils.isBlank(followReadingTitle)) {
                return MapMessage.errorMessage("语文阅读模板趣味跟读模块标题不能为空");
            }
            StudyModel followReadingModel = new StudyModel();
            followReadingModel.setModelType(StudyModelType.CHINESEFOLLOWREADING);
            followReadingModel.setTypeId(followReadingModelContent.getId());
            followReadingModel.setTitle(followReadingTitle);
            studyModelList.add(followReadingModel);
            mapMessage.add("follow_reading_model_content", followReadingModelContent);
        } else if (usePictureBook == 2) {
            ChineseReadingFamousBookModelContent famousBookModelContent = JsonUtils.fromJson(famousBookInfoStr, ChineseReadingFamousBookModelContent.class);
            if (famousBookModelContent == null) {
                return MapMessage.errorMessage("语文阅读模板名著阅读模块不能为空");
            }
            if (StringUtils.isBlank(famousBookModelContent.getTitle())) {
                return MapMessage.errorMessage("语文阅读模板名著阅读模块标题不能为空");
            }
            if (StringUtils.isBlank(famousBookModelContent.getAuthor())) {
                return MapMessage.errorMessage("语文阅读模板名著阅读模块作者不能为空");
            }
            if (StringUtils.isBlank(famousBookModelContent.getBackgroundImgUrl())) {
                return MapMessage.errorMessage("语文阅读模板名著阅读模块背景图片不能为空");
            }
            if (StringUtils.isBlank(famousBookModelContent.getAudioUrl())) {
                return MapMessage.errorMessage("语文阅读模板名著阅读模块音频地址不能为空");
            }
            if (famousBookModelContent.getAudioSeconds() == null || famousBookModelContent.getAudioSeconds() <= 0) {
                return MapMessage.errorMessage("语文阅读模板名著阅读模块音频时长不能为空");
            }
            //http://project.17zuoye.net/redmine/issues/82894
//            if (StringUtils.isBlank(famousBookModelContent.getLrcContent())) {
//                return MapMessage.errorMessage("语文阅读模板名著阅读模块文本内容不能为空");
//            }
            //json直接转换的存在id=""的情况
            if (StringUtils.isBlank(famousBookModelContent.getId())) {
                famousBookModelContent.setId(null);
            }
            Map<String, Object> famousBookInfoMap = JsonUtils.fromJson(famousBookInfoStr);
            if (MapUtils.isEmpty(famousBookInfoMap)) {
                return MapMessage.errorMessage("语文阅读模板名著阅读模块不能为空");
            }
            String famousBookTitle = SafeConverter.toString(famousBookInfoMap.get("famous_book_title"));
            if (StringUtils.isBlank(famousBookTitle)) {
                return MapMessage.errorMessage("语文阅读模板名著阅读模块标题不能为空");
            }
            StudyModel famousBookModel = new StudyModel();
            famousBookModel.setModelType(StudyModelType.CHINESEFAMOUSBOOK);
            famousBookModel.setTypeId(famousBookModelContent.getId());
            famousBookModel.setTitle(famousBookTitle);
            studyModelList.add(famousBookModel);
            mapMessage.add("famous_book_model_content", famousBookModelContent);
            //重点赏析
            ChineseReadingAppreciateModelContent appreciateModelContent = JsonUtils.fromJson(appreciateInfoStr, ChineseReadingAppreciateModelContent.class);
            if (appreciateModelContent == null) {
                return MapMessage.errorMessage("语文阅读模板重点赏析模块不能为空");
            }
            if (StringUtils.isBlank(appreciateModelContent.getThemeImgUrl())) {
                return MapMessage.errorMessage("语文阅读模板重点赏析模块主题图片不能为空");
            }
            if (StringUtils.isBlank(appreciateModelContent.getAudioUrl())) {
                return MapMessage.errorMessage("语文阅读模板重点赏析模块音频地址不能为空");
            }
            if (appreciateModelContent.getAudioSeconds() == null || appreciateModelContent.getAudioSeconds() <= 0) {
                return MapMessage.errorMessage("语文阅读模板重点赏析模块音频时长不能为空");
            }
            if (StringUtils.isBlank(appreciateModelContent.getText())) {
                return MapMessage.errorMessage("语文阅读模板重点赏析模块文本内容不能为空");
            }
            //json直接转换的存在id=""的情况
            if (StringUtils.isBlank(appreciateModelContent.getId())) {
                appreciateModelContent.setId(null);
            }
            Map<String, Object> appreciateInfoMap = JsonUtils.fromJson(appreciateInfoStr);
            if (MapUtils.isEmpty(appreciateInfoMap)) {
                return MapMessage.errorMessage("语文阅读模板重点赏析模块不能为空");
            }
            String appreciateTitle = SafeConverter.toString(appreciateInfoMap.get("appreciate_title"));
            if (StringUtils.isBlank(appreciateTitle)) {
                return MapMessage.errorMessage("语文阅读模板重点赏析模块标题不能为空");
            }
            StudyModel appreciateModel = new StudyModel();
            appreciateModel.setModelType(StudyModelType.CHINESEAPPRECIATE);
            appreciateModel.setTypeId(appreciateModelContent.getId());
            appreciateModel.setTitle(appreciateTitle);
            studyModelList.add(appreciateModel);
            mapMessage.add("appreciate_model_content", appreciateModelContent);
            //拓展练习
            ChineseReadingExpandPracticeModelContent expandPracticeModelContent = JsonUtils.fromJson(expandPracticeInfoStr, ChineseReadingExpandPracticeModelContent.class);
            if (expandPracticeModelContent == null) {
                return MapMessage.errorMessage("语文阅读模板拓展练习模块不能为空");
            }
            if (CollectionUtils.isEmpty(expandPracticeModelContent.getQuestionIdList())) {
                return MapMessage.errorMessage("语文阅读模板拓展练习模块题目ID不能为空");
            }
            Map<String, NewQuestion> funQuestionMap = questionLoaderClient.loadLatestQuestionByDocIds(expandPracticeModelContent.getQuestionIdList());
            Set<String> funMissedQuestionIds = expandPracticeModelContent.getQuestionIdList().stream().filter(p -> funQuestionMap.get(p) == null)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(funMissedQuestionIds)) {
                return MapMessage.errorMessage("语文阅读模板拓展练习中如下题目ID错误: " + JsonUtils.toJson(funMissedQuestionIds));
            }
            //json直接转换的存在id=""的情况
            if (StringUtils.isBlank(expandPracticeModelContent.getId())) {
                expandPracticeModelContent.setId(null);
            }
            Map<String, Object> expandPracticeBookInfoMap = JsonUtils.fromJson(expandPracticeInfoStr);
            if (MapUtils.isEmpty(expandPracticeBookInfoMap)) {
                return MapMessage.errorMessage("语文阅读模板拓展练习模块不能为空");
            }
            String expandPracticeTitle = SafeConverter.toString(expandPracticeBookInfoMap.get("expand_practice_title"));
            if (StringUtils.isBlank(expandPracticeTitle)) {
                return MapMessage.errorMessage("语文阅读模板拓展练习模块标题不能为空");
            }
            StudyModel expandPracticeModel = new StudyModel();
            expandPracticeModel.setModelType(StudyModelType.CHINESEEXPANDPRACTICE);
            expandPracticeModel.setTypeId(expandPracticeModelContent.getId());
            expandPracticeModel.setTitle(expandPracticeTitle);
            studyModelList.add(expandPracticeModel);
            mapMessage.add("expand_practice_model_content", expandPracticeModelContent);
        } else {
            return MapMessage.errorMessage("学习模块类型错误");
        }
        //思维导图
        ChineseReadingMindMapModelContent mindMapModelContent = JsonUtils.fromJson(mindMapInfoStr, ChineseReadingMindMapModelContent.class);
        if (mindMapModelContent == null) {
            return MapMessage.errorMessage("语文阅读模板思维导图模块不能为空");
        }
        if (StringUtils.isBlank(mindMapModelContent.getExcelFileUrl())) {
            return MapMessage.errorMessage("语文阅读模板思维导图模块excel地址不能为空");
        }
        if (mindMapModelContent.getMindMap() == null) {
            return MapMessage.errorMessage("语文阅读模板思维导图模块思维导图不能为空");
        }
        //json直接转换的存在id=""的情况
        if (StringUtils.isBlank(mindMapModelContent.getId())) {
            mindMapModelContent.setId(null);
        }
        Map<String, Object> mindMapInfoMap = JsonUtils.fromJson(mindMapInfoStr);
        if (MapUtils.isEmpty(mindMapInfoMap)) {
            return MapMessage.errorMessage("语文阅读模板思维导图模块不能为空");
        }
        String mindMapTitle = SafeConverter.toString(mindMapInfoMap.get("mind_map_title"));
        if (StringUtils.isBlank(mindMapTitle)) {
            return MapMessage.errorMessage("语文阅读模板思维导图模块标题不能为空");
        }
        StudyModel mindMapModel = new StudyModel();
        mindMapModel.setModelType(StudyModelType.MINDMAP);
        mindMapModel.setTypeId(mindMapModelContent.getId());
        mindMapModel.setTitle(mindMapTitle);
        studyModelList.add(2, mindMapModel);
        template.setStudyModelList(studyModelList);

        return mapMessage
                .add("chinese_reading_template", template)
                .add("mind_map_model_content", mindMapModelContent)
                .add("user_picture_book", usePictureBook);
    }

    @RequestMapping(value = "/upload_mind_map_excel.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadMindMapExcel() {
        MapMessage mapMessage = readExcel("source_file");
        if (mapMessage == null || mapMessage.get("workbook") == null) {
            return MapMessage.errorMessage("不支持的文件类型或者上传失败");
        }
        XSSFWorkbook workbook = (XSSFWorkbook) mapMessage.get("workbook");
        MapMessage message = validateImportGroupAreaData(workbook);
        if (!message.isSuccess()) {
            return message;
        }
        List<String> questionIds = (List) (message.get("questionIds"));
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage().add("error", "sheet读取失败");
        }
        List<ChineseReadingMindMapMapper> mindMapMappers = new ArrayList<>();
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            String firstLevel = "";
            String secondLevel = "";
            String thirdLevel = "";
            String fourthLevel = "";
            String fifthLevel = "";
            String sixthLevel = "";
            int type = 0;
            String content = "";
            String id;
            String parentId;
            Integer levelCount = 0;
            ChineseReadingMindMapMapper mindMapMapper = new ChineseReadingMindMapMapper();
            for (Cell cell : row) {
                switch (cell.getColumnIndex()) {
                    case 0:
                        firstLevel = ExcelUtil.getCellValue(cell);
                        break;
                    case 1:
                        secondLevel = ExcelUtil.getCellValue(cell);
                        break;
                    case 2:
                        thirdLevel = ExcelUtil.getCellValue(cell);
                        break;
                    case 3:
                        fourthLevel = ExcelUtil.getCellValue(cell);
                        break;
                    case 4:
                        fifthLevel = ExcelUtil.getCellValue(cell);
                        break;
                    case 5:
                        sixthLevel = ExcelUtil.getCellValue(cell);
                        break;
                    case 6:
                        type = SafeConverter.toInt(ExcelUtil.getCellValue(cell));
                        break;
                    case 7:
                        content = ExcelUtil.getCellValue(cell);
                        break;
                    default:
                        break;
                }
            }
            if (StringUtils.isBlank(secondLevel)) {
                id = firstLevel;
                parentId = "0";
                levelCount = 1;
            } else if (StringUtils.isBlank(thirdLevel)) {
                id = secondLevel;
                parentId = firstLevel;
                levelCount = 2;
            } else if (StringUtils.isBlank(fourthLevel)) {
                id = thirdLevel;
                parentId = secondLevel;
                levelCount = 3;
            } else if (StringUtils.isBlank(fifthLevel)) {
                id = fourthLevel;
                parentId = thirdLevel;
                levelCount = 4;
            } else if (StringUtils.isBlank(sixthLevel)) {
                id = fifthLevel;
                parentId = fourthLevel;
                levelCount = 5;
            } else {
                id = sixthLevel;
                parentId = fifthLevel;
                levelCount = 6;
            }
            if (StringUtils.isBlank(content)) {
                continue;
            }
            mindMapMapper.setId(id);
            mindMapMapper.setParentId(parentId);
            mindMapMapper.setContent(content);
            mindMapMapper.setWordCount(content.length());
            mindMapMapper.setContentType(type);
            mindMapMapper.setLevelCount(levelCount);
            mindMapMappers.add(mindMapMapper);
        }
        mindMapMappers = mindMapMappers.stream().sorted(Comparator.comparing(ChineseReadingMindMapMapper::getParentId)).collect(Collectors.toList());
        ChineseReadingMindMapMapper treeMap = new ChineseReadingMindMapMapper();
        for (ChineseReadingMindMapMapper mindMapMapper : mindMapMappers) {
            if (StringUtils.equals("0", mindMapMapper.getParentId())) {
                treeMap = mindMapMapper;
            }
            for (ChineseReadingMindMapMapper child : mindMapMappers) {
                if (StringUtils.equals(child.getParentId(), mindMapMapper.getId())) {
                    if (mindMapMapper.getChildrenMindMapList() == null) {
                        mindMapMapper.setChildrenMindMapList(new ArrayList<>());
                    }
                    mindMapMapper.getChildrenMindMapList().add(child);
                }
            }
        }
        return MapMessage.successMessage()
                .add("mapper", treeMap)
                .add("fileName", mapMessage.get("fileName"))
                .add("fileUrl", mapMessage.get("fileUrl"))
                .add("questionIds", questionIds);
    }

    //读excel数据
    private MapMessage readExcel(String name) {
        HttpServletRequest request = getRequest();
        if (!(request instanceof MultipartHttpServletRequest)) {
            logger.error("readRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        try {
            MultipartFile file = multipartRequest.getFile(name);
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", name);
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("readRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            String env = "study_course/";
            if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
                env = "study_course/test/";
            }
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
            StorageMetadata storageMetadata = new StorageMetadata();
            storageMetadata.setContentLength(file.getSize());
            String realName = storageClient.upload(in, fileName, path, storageMetadata);
            String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
            @Cleanup InputStream workbookIn = file.getInputStream();
            return MapMessage.successMessage().add("workbook", new XSSFWorkbook(workbookIn)).add("fileName", realName).add("fileUrl", fileUrl);
        } catch (Exception e) {
            logger.error("readRequestWorkbook - Excp : {}", e);
            return null;
        }
    }

    //校验导入数据
    private MapMessage validateImportGroupAreaData(XSSFWorkbook workbook) {
        if (workbook == null) {
            return MapMessage.errorMessage("文件读取失败");
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return MapMessage.errorMessage("sheet读取失败");
        }
        Row title = sheet.getRow(0);
        List<String> questionIds = new ArrayList<>();
        for (Cell cell : title) {
            if (cell.getColumnIndex() > 7) {
                return MapMessage.errorMessage("表格式错误");
            }
            if (cell.getColumnIndex() == 0 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "一级ID")) {
                return MapMessage.errorMessage("表头错误");
            }
            if (cell.getColumnIndex() == 1 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "二级ID")) {
                return MapMessage.errorMessage("表头错误");
            }
            if (cell.getColumnIndex() == 2 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "三级ID")) {
                return MapMessage.errorMessage("表头错误");
            }
            if (cell.getColumnIndex() == 3 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "四级ID")) {
                return MapMessage.errorMessage("表头错误");
            }
            if (cell.getColumnIndex() == 4 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "五级ID")) {
                return MapMessage.errorMessage("表头错误");
            }
            if (cell.getColumnIndex() == 5 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "六级ID")) {
                return MapMessage.errorMessage("表头错误");
            }
            if (cell.getColumnIndex() == 6 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "类型")) {
                return MapMessage.errorMessage("表头错误");
            }
            if (cell.getColumnIndex() == 7 && !StringUtils.equals(ExcelUtil.getCellValue(cell), "内容")) {
                return MapMessage.errorMessage("表头错误");
            }
        }
        for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
            Row row = sheet.getRow(i);
            int type = 0;
            String content = "";
            for (Cell cell : row) {
                switch (cell.getColumnIndex()) {
                    case 6:
                        type = SafeConverter.toInt(ExcelUtil.getCellValue(cell));
                        break;
                    case 7:
                        content = ExcelUtil.getCellValue(cell);
                        break;
                    default:
                        break;
                }
            }
            if (type == 2) {
                questionIds.add(content);
            }
            if (type == 1 && (content.length() < 2 || content.length() > 40)) {
                return MapMessage.errorMessage("文本内容长度不正确，内容为：" + content);
            }
        }
        if (CollectionUtils.isEmpty(questionIds)) {
            return MapMessage.successMessage().add("questionIds", new ArrayList<>());
        }
        Map<String, NewQuestion> stringNewQuestionMap = questionLoaderClient.loadLatestQuestionByDocIds(questionIds);
        Set<String> notExistQuestionIds = questionIds.stream().filter(p -> stringNewQuestionMap.get(p) == null)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(notExistQuestionIds)) {
            return MapMessage.errorMessage("思维导图中的不存在题目Id如下: " + JsonUtils.toJson(notExistQuestionIds));
        }
        return MapMessage.successMessage().add("questionIds", questionIds);
    }
}
