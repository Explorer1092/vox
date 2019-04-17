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
import com.voxlearning.galaxy.service.studycourse.api.entity.changelog.ContentChangeLog;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSpu;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.ClassicalChineseAppreciateModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.ClassicalChineseExpoundModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.ClassicalChineseFunModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.model.ClassicalChineseReciteModelContent;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.ClassicalChineseLessonTemplate;
import com.voxlearning.galaxy.service.studycourse.api.entity.template.StudyModel;
import com.voxlearning.galaxy.service.studycourse.api.mapper.ClassicalChineseLessonTemplateLogMapper;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.galaxy.service.studycourse.constant.StudyModelType;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.controller.studyTogether.AbstractStudyTogetherController;
import com.voxlearning.utopia.service.parent.api.CrmStudyTogetherService;
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

/**
 * @author shiwei.liao
 * @since 2018-7-30
 */
@Controller
@RequestMapping(value = "opmanager/studyTogether/template/")
@Slf4j
public class CrmLessonTemplateController extends AbstractStudyTogetherController {

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

    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;

    /**
     * 语文古文模板列表
     */
    @RequestMapping(value = "classical_chinese_list.vpage", method = RequestMethod.GET)
    public String classicalChineseList(Model model) {
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
        model.addAttribute("course_type", 1);


        List<ClassicalChineseLessonTemplate> templateList = new ArrayList<>();
        if (templateId != 0) {
            ClassicalChineseLessonTemplate template = lessonTemplateLoader.loadClassicalChineseTemplate(templateId);
            if (template != null) {
                templateList.add(template);
            }
        } else {
            templateList = lessonTemplateLoader.loadAllClassicalChineseTemplate();
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
        Page<ClassicalChineseLessonTemplate> templatePage = PageableUtils.listToPage(templateList, new PageRequest(page - 1, 10));
        List<ClassicalChineseLessonTemplate> returnTemplateList = templatePage.getContent();

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
        model.addAttribute("classical_chinese_template_list", mapList);
        return "/opmanager/studyTogether/temlate/templatelist";
    }


    @RequestMapping(value = "classical_chinese_template_info.vpage", method = RequestMethod.GET)
    public String classicalChineseInfo(Model model) {
        Long templateId = getRequestLong("template_id");


        //是否允许编辑
        model.addAttribute("edit", getRequestInt("edit"));
        model.addAttribute("admin_user", getCurrentAdminUser().getAdminUserName());

        //所有古文的spu
        List<CourseStructSpu> spuList = studyCourseStructLoaderClient.loadAllCourseStructSpu();
        spuList = spuList.stream().filter(t -> t.getParent().getCourseType() == 1).collect(Collectors.toList());
        model.addAttribute("spu_list", spuList);

        if (templateId == 0) {
            return "/opmanager/studyTogether/temlate/classicalchineseinfo";
        }
        ClassicalChineseLessonTemplate template = lessonTemplateLoader.loadClassicalChineseTemplate(templateId);
        if (template == null) {
            getAlertMessageManager().addMessageError("您要查处理的古文模板不存在");
            return "/opmanager/studyTogether/temlate/classicalchineseinfo";
        }

        if (CollectionUtils.isNotEmpty(template.getStudyModelList())) {
            template.getStudyModelList().forEach(p -> {
                if (p.getModelType() == StudyModelType.EXPOUND) {
                    ClassicalChineseExpoundModelContent content = modelContentLoader.loadClassicalChineseExpoundModelContent(p.getTypeId());
                    model.addAttribute("expound_content", content);
                    model.addAttribute("expound_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.APPRECIATE) {
                    ClassicalChineseAppreciateModelContent content = modelContentLoader.loadClassicalChineseAppreciateModelContent(p.getTypeId());
                    model.addAttribute("appreciate_content", content);
                    model.addAttribute("appreciate_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.RECITE) {
                    ClassicalChineseReciteModelContent content = modelContentLoader.loadClassicalChineseReciteModelContent(p.getTypeId());
                    model.addAttribute("recite_content", content);
                    model.addAttribute("recite_title", p.getTitle());
                } else if (p.getModelType() == StudyModelType.FUN) {
                    ClassicalChineseFunModelContent content = modelContentLoader.loadClassicalChineseFunModelContent(p.getTypeId());
                    model.addAttribute("fun_content", content);
                    model.addAttribute("fun_title", p.getTitle());
                }
            });
        }

        model.addAttribute("template", template);
        model.addAttribute("cdn_host", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")));

        return "/opmanager/studyTogether/temlate/classicalchineseinfo";
    }

    @RequestMapping(value = "save_classical_chinese.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveClassicalChineseTemplate() {
        MapMessage mapMessage = validateAndGenerateClassicalChineseInfo();
        if (!mapMessage.isSuccess()) {
            return mapMessage;
        }

        //日志支持
        ClassicalChineseLessonTemplateLogMapper oldTemplateMapper = new ClassicalChineseLessonTemplateLogMapper();
        ClassicalChineseLessonTemplateLogMapper newTemplateMapper = new ClassicalChineseLessonTemplateLogMapper();

        ClassicalChineseLessonTemplate template = (ClassicalChineseLessonTemplate) (mapMessage.get("classical_chinese_template"));
        ClassicalChineseExpoundModelContent expoundModelContent = (ClassicalChineseExpoundModelContent) (mapMessage.get("expound_model_content"));
        if (StringUtils.isNotBlank(expoundModelContent.getId())) {
            ClassicalChineseExpoundModelContent oldExpoundModel = modelContentLoader.loadClassicalChineseExpoundModelContent(expoundModelContent.getId());
            oldTemplateMapper.setExpoundModelContent(oldExpoundModel);
        }
        ClassicalChineseAppreciateModelContent appreciateModelContent = (ClassicalChineseAppreciateModelContent) (mapMessage.get("appreciate_model_content"));
        if (StringUtils.isNotBlank(appreciateModelContent.getId())) {
            ClassicalChineseAppreciateModelContent oldAppreciateModel = modelContentLoader.loadClassicalChineseAppreciateModelContent(appreciateModelContent.getId());
            oldTemplateMapper.setAppreciateModelContent(oldAppreciateModel);
        }
        ClassicalChineseReciteModelContent reciteModelContent = (ClassicalChineseReciteModelContent) (mapMessage.get("recite_model_content"));
        if (StringUtils.isNotBlank(reciteModelContent.getId())) {
            ClassicalChineseReciteModelContent oldReciteModel = modelContentLoader.loadClassicalChineseReciteModelContent(reciteModelContent.getId());
            oldTemplateMapper.setReciteModelContent(oldReciteModel);
        }
        ClassicalChineseFunModelContent funModelContent = (ClassicalChineseFunModelContent) (mapMessage.get("fun_model_content"));
        if (StringUtils.isNotBlank(funModelContent.getId())) {
            ClassicalChineseFunModelContent oldFunModel = modelContentLoader.loadClassicalChineseFunModelContent(funModelContent.getId());
            oldTemplateMapper.setFunModelContent(oldFunModel);
        }
        //更新学习流程
        ClassicalChineseExpoundModelContent updateExpoundModelContent = modelContentService.saveClassicalChineseExpoundModelContent(expoundModelContent);
        newTemplateMapper.setExpoundModelContent(updateExpoundModelContent);
        ClassicalChineseAppreciateModelContent updateAppreciateModelContent = modelContentService.saveClassicalChineseAppreciateModelContent(appreciateModelContent);
        newTemplateMapper.setAppreciateModelContent(updateAppreciateModelContent);
        ClassicalChineseReciteModelContent updateReciteModelContent = modelContentService.saveClassicalChineseReciteModelContent(reciteModelContent);
        newTemplateMapper.setReciteModelContent(updateReciteModelContent);
        ClassicalChineseFunModelContent updateFunModelContent = modelContentService.saveClassicalChineseFunModelContent(funModelContent);
        newTemplateMapper.setFunModelContent(updateFunModelContent);
        template.getStudyModelList().forEach(p -> {
            if (p.getModelType() == StudyModelType.EXPOUND) {
                p.setTypeId(updateExpoundModelContent.getId());
            } else if (p.getModelType() == StudyModelType.APPRECIATE) {
                p.setTypeId(updateAppreciateModelContent.getId());
            } else if (p.getModelType() == StudyModelType.RECITE) {
                p.setTypeId(updateReciteModelContent.getId());
            } else if (p.getModelType() == StudyModelType.FUN) {
                p.setTypeId(updateFunModelContent.getId());
            }
        });
        AuthCurrentAdminUser adminUser = getCurrentAdminUser();
        if (template.getId() == null) {
            template.setCreateUser(adminUser.getAdminUserName());
        } else {
            ClassicalChineseLessonTemplate oldTemplate = lessonTemplateLoader.loadClassicalChineseTemplate(template.getId());
            oldTemplateMapper.setLessonTemplate(oldTemplate);
        }
        template.setUpdateUser(adminUser.getAdminUserName());
        ClassicalChineseLessonTemplate updatedTemplate = lessonTemplateService.saveClassicalChineseTemplate(template);
        newTemplateMapper.setLessonTemplate(updatedTemplate);
        //实时把修改同步给php
        lessonTemplateService.syncClassicalChineseTemplateToPhp(template, updateExpoundModelContent, updateAppreciateModelContent, updateReciteModelContent, updateFunModelContent);
        //CRM 修改日志
        studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldTemplateMapper, newTemplateMapper, adminUser.getAdminUserName(), ChangeLogType.ClassicChineseTemplate, updatedTemplate.getId().toString());
        return MapMessage.successMessage().add("id", updatedTemplate.getId());
    }

    @RequestMapping(value = "change_log_list.vpage", method = RequestMethod.GET)
    public String getChangeLogList(Model model) {
        Long templateId = getRequestLong("template_id");
        String changeLogType = getRequestString("change_log_type");
        Integer page = getRequestInt("page", 1);
        PageRequest request = new PageRequest(page - 1, 10);

        model.addAttribute("template_id", templateId);
        model.addAttribute("change_log_type", changeLogType);
        model.addAttribute("page", page);

        ChangeLogType logType;
        try {
            logType = ChangeLogType.valueOf(changeLogType);
        } catch (Exception e) {
            getAlertMessageManager().addMessageError("日志类型错误");
            return "/opmanager/studyTogether/temlate/changeloglist";
        }
        if (templateId == 0) {
            getAlertMessageManager().addMessageError("ID错误");
            return "/opmanager/studyTogether/temlate/changeloglist";
        }
        Page<ContentChangeLog> changeLogs = studyCourseBlackWidowServiceClient.getContentChangeLogService().loadChangeLogListByPage(templateId.toString(), logType, request);
        model.addAttribute("total_page", changeLogs.getTotalPages());
        model.addAttribute("change_log_list", changeLogs.getContent());
        return "/opmanager/studyTogether/temlate/changeloglist";
    }

    /**
     * 上传图片到aliyun
     */
    @RequestMapping(value = "/upload_signal_file_to_oss.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadImgToOss(MultipartFile inputFile) {
        String activityName = "study_course";
        if (inputFile == null || StringUtils.isBlank(activityName)) {
            return MapMessage.errorMessage("没有上传的文件");
        }
        try {
            return uploadFileTo17pmcOss(inputFile, activityName);
        } catch (Exception e) {
            logger.error("课程文件上传失败{}", e);
            return MapMessage.errorMessage("课程文件上传失败");
        }
    }

    @RequestMapping(value = "/batch_upload_file_to_oss.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchUploadFileToOss() {
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) getRequest();
        List<MultipartFile> inputFiles = multipartHttpServletRequest.getFiles("inputFiles");
        String activityName = "study_course";
        if (inputFiles == null) {
            return MapMessage.errorMessage("没有上传的文件");
        }
        try {
            List<Map<String, Object>> mapList = new ArrayList<>();
            for (MultipartFile inputFile : inputFiles) {
                MapMessage message = uploadFileTo17pmcOss(inputFile, activityName);
                if (message.isSuccess()) {
                    mapList.add(new HashMap<>(message));
                }
            }
            return MapMessage.successMessage().add("file_list", mapList);
        } catch (Exception e) {
            logger.error("课程文件上传失败{}", e);
            return MapMessage.errorMessage("课程文件上传失败");
        }
    }

    /**
     * 上传完成之后要按照文件名对图片和音频进行分组
     */
    @RequestMapping(value = "/batch_upload_file_to_oss_and_group.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchUploadFileToOssAndGroup() {
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) getRequest();
        List<MultipartFile> inputFiles = multipartHttpServletRequest.getFiles("inputFiles");
        String activityName = "study_course";
        if (inputFiles == null) {
            return MapMessage.errorMessage("没有上传的文件");
        }
        try {
            Map<String, List<Map<String, Object>>> fileMap = new TreeMap<>(String::compareTo);
            for (MultipartFile inputFile : inputFiles) {
                String originalFilename = inputFile.getOriginalFilename().replaceAll(" ", "");
                String fileName = StringUtils.substringBeforeLast(originalFilename, ".");
                String suffix = StringUtils.substringAfterLast(originalFilename, ".");
                MapMessage message = uploadFileTo17pmcOss(inputFile, activityName);
                if (message.isSuccess()) {
                    List<Map<String, Object>> mapList = fileMap.computeIfAbsent(fileName, k -> new ArrayList<>());
                    if (mapList.size() == 0) {
                        mapList.add(new HashMap<>());
                        mapList.add(new HashMap<>());
                    } else if (mapList.size() == 1) {
                        mapList.add(new HashMap<>());
                    }
                    //这里目前音频格式全部是mp3的。后续有调整的可能。暂时只能这么实现了。
                    if (StringUtils.equals("mp3", suffix)) {
                        mapList.set(1, new HashMap<>(message));
                    } else {
                        mapList.set(0, new HashMap<>(message));
                    }
                }
            }
            return MapMessage.successMessage().add("file_map", fileMap);
        } catch (Exception e) {
            logger.error("课程文件上传失败{}", e);
            return MapMessage.errorMessage("课程文件上传失败");
        }
    }

    @RequestMapping(value = "get_spu_name.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSpuName() {
        Long id = getRequestLong("spu_id");
        CourseStructSpu courseStructSpu = studyCourseStructLoaderClient.loadCourseStructSpuById(id);
        return courseStructSpu == null ? MapMessage.errorMessage() : MapMessage.successMessage().add("spu_name", courseStructSpu.getName());
    }


    private MapMessage uploadFileTo17pmcOss(MultipartFile inputFile, String activityName) throws IOException {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
        if (StringUtils.isBlank(suffix)) {
            suffix = "jpg";
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = activityName + "/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = activityName + "/test/";
        }
        String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
        String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
        String realName = storageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
        String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        return MapMessage.successMessage().add("fileName", realName).add("fileUrl", fileUrl);
    }


    private MapMessage validateAndGenerateClassicalChineseInfo() {
        //基本信息
        String templateInfoStr = getRequestString("template_info");
        //名师精讲
        String expoundInfoStr = getRequestString("expound_info");
        //名句赏析
        String appreciateInfoStr = getRequestString("appreciate_info");
        //每日朗读
        String reciteInfoStr = getRequestString("recite_info");
        //趣味练习
        String funInfoStr = getRequestString("fun_info");
        if (StringUtils.isBlank(templateInfoStr)) {
            return MapMessage.errorMessage("古文基本信息不能为空");
        }
        if (StringUtils.isBlank(expoundInfoStr)) {
            return MapMessage.errorMessage("名师精讲信息不能为空");
        }
        if (StringUtils.isBlank(appreciateInfoStr)) {
            return MapMessage.errorMessage("名句赏析信息不能为空");
        }
        if (StringUtils.isBlank(reciteInfoStr)) {
            return MapMessage.errorMessage("每日朗读信息不能为空");
        }
        if (StringUtils.isBlank(funInfoStr)) {
            return MapMessage.errorMessage("趣味练习信息不能为空");
        }
        //古文模版
        ClassicalChineseLessonTemplate template = JsonUtils.fromJson(templateInfoStr, ClassicalChineseLessonTemplate.class);
        if (template == null) {
            return MapMessage.errorMessage("古文基本信息不能为空");
        }
        if (template.getSpuId() == null) {
            return MapMessage.errorMessage("古文SPU_ID不能为空");
        }
        if (StringUtils.isBlank(template.getName())) {
            return MapMessage.errorMessage("古文模板名称不能为空");
        }
        if (StringUtils.isBlank(template.getTitle())) {
            return MapMessage.errorMessage("古文标题不能为空");
        }
        if (StringUtils.isBlank(template.getAuthor())) {
            return MapMessage.errorMessage("古文作者不能为空");
        }
        if (StringUtils.isBlank(template.getAudioUrl())) {
            return MapMessage.errorMessage("古文音频不能为空");
        }
        if (template.getAudioSeconds() == null || template.getAudioSeconds() == 0) {
            return MapMessage.errorMessage("古文音频时长不能为0");
        }
        if (CollectionUtils.isEmpty(template.getContentList())) {
            return MapMessage.errorMessage("古文正文不能为空");
        }
        //名师精讲
        ClassicalChineseExpoundModelContent expoundModelContent = JsonUtils.fromJson(expoundInfoStr, ClassicalChineseExpoundModelContent.class);
        if (expoundModelContent == null) {
            return MapMessage.errorMessage("名师精讲不能为空");
        }
        if (StringUtils.isBlank(expoundModelContent.getAudioUrl())) {
            return MapMessage.errorMessage("名师精讲音频不能为空");
        }
        if (expoundModelContent.getAudioSeconds() == null || expoundModelContent.getAudioSeconds() == 0) {
            return MapMessage.errorMessage("名师精讲音频时长不能为0");
        }
        if (StringUtils.isBlank(expoundModelContent.getBackgroundImgUrl())) {
            return MapMessage.errorMessage("名师精讲背景图不能为空");
        }
        Map<String, Object> expoundMap = JsonUtils.fromJson(expoundInfoStr);
        if (MapUtils.isEmpty(expoundMap)) {
            return MapMessage.errorMessage("名师精讲不能为空");
        }
        String expoundTitle = SafeConverter.toString(expoundMap.get("expound_title"));
        if (StringUtils.isBlank(expoundTitle)) {
            return MapMessage.errorMessage("名师精讲模块标题不能为空");
        }
        //json直接转换的存在id=""的情况
        if (StringUtils.isBlank(expoundModelContent.getId())) {
            expoundModelContent.setId(null);
        }
        List<StudyModel> modelList = new ArrayList<>();
        StudyModel expoundModel = new StudyModel();
        expoundModel.setModelType(StudyModelType.EXPOUND);
        expoundModel.setTitle(expoundTitle);
        expoundModel.setTypeId(expoundModelContent.getId());
        modelList.add(expoundModel);
        //名句赏析
        ClassicalChineseAppreciateModelContent appreciateModelContent = JsonUtils.fromJson(appreciateInfoStr, ClassicalChineseAppreciateModelContent.class);
        if (appreciateModelContent == null) {
            return MapMessage.errorMessage("名句赏析不能为空");
        }
        if (StringUtils.isBlank(appreciateModelContent.getAudioUrl())) {
            return MapMessage.errorMessage("名句赏析音频不能为空");
        }
        if (appreciateModelContent.getAudioSeconds() == null || appreciateModelContent.getAudioSeconds() == 0) {
            return MapMessage.errorMessage("名句赏析音频时长不能为0");
        }
        if (StringUtils.isBlank(appreciateModelContent.getBackgroundImgUrl())) {
            return MapMessage.errorMessage("名句赏析背景图不能为空");
        }
        Map<String, Object> appreciateMap = JsonUtils.fromJson(appreciateInfoStr);
        if (MapUtils.isEmpty(appreciateMap)) {
            return MapMessage.errorMessage("名句赏析不能为空");
        }
        String appreciateTitle = SafeConverter.toString(appreciateMap.get("appreciate_title"));
        if (StringUtils.isBlank(appreciateTitle)) {
            return MapMessage.errorMessage("名句赏析模块标题不能为空");
        }
        //json直接转换的存在id=""的情况
        if (StringUtils.isBlank(appreciateModelContent.getId())) {
            appreciateModelContent.setId(null);
        }
        StudyModel appreciateModel = new StudyModel();
        appreciateModel.setModelType(StudyModelType.APPRECIATE);
        appreciateModel.setTitle(appreciateTitle);
        appreciateModel.setTypeId(appreciateModelContent.getId());
        modelList.add(appreciateModel);
        //每日朗读
        ClassicalChineseReciteModelContent reciteModelContent = JsonUtils.fromJson(reciteInfoStr, ClassicalChineseReciteModelContent.class);
        if (reciteModelContent == null) {
            return MapMessage.errorMessage("每日朗读不能为空");
        }
        if (CollectionUtils.isEmpty(reciteModelContent.getSentenceList())) {
            return MapMessage.errorMessage("每日朗读句子不能为空");
        }
        reciteModelContent.getSentenceList().stream().filter(p -> p.getRank() == null).forEach(p -> p.setRank(0));
        reciteModelContent.getSentenceList().sort(Comparator.comparing(ClassicalChineseReciteModelContent.ReciteSentence::getRank));
        Map<String, Object> reciteMap = JsonUtils.fromJson(reciteInfoStr);
        if (MapUtils.isEmpty(reciteMap)) {
            return MapMessage.errorMessage("每日朗读不能为空");
        }
        String reciteTitle = SafeConverter.toString(reciteMap.get("recite_title"));
        if (StringUtils.isBlank(reciteTitle)) {
            return MapMessage.errorMessage("每日朗读模块标题不能为空");
        }
        //json直接转换的存在id=""的情况
        if (StringUtils.isBlank(reciteModelContent.getId())) {
            reciteModelContent.setId(null);
        }
        StudyModel reciteModel = new StudyModel();
        reciteModel.setModelType(StudyModelType.RECITE);
        reciteModel.setTitle(reciteTitle);
        reciteModel.setTypeId(reciteModelContent.getId());
        modelList.add(reciteModel);
        //趣味练习
        ClassicalChineseFunModelContent funModelContent = JsonUtils.fromJson(funInfoStr, ClassicalChineseFunModelContent.class);
        if (funModelContent == null) {
            return MapMessage.errorMessage("趣味练习不能为空");
        }
        if (CollectionUtils.isEmpty(funModelContent.getQuestionIds())) {
            return MapMessage.errorMessage("趣味练习题目ID不能为空");
        }
        Map<String, NewQuestion> questionMap = questionLoaderClient.loadLatestQuestionByDocIds(funModelContent.getQuestionIds());
        Set<String> funMissedQuestionIds = funModelContent.getQuestionIds().stream().filter(p -> questionMap.get(p) == null)
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(funMissedQuestionIds)) {
            return MapMessage.errorMessage("趣味练习中如下题目ID错误: " + JsonUtils.toJson(funMissedQuestionIds));
        }
        Map<String, Object> funMap = JsonUtils.fromJson(funInfoStr);
        if (MapUtils.isEmpty(funMap)) {
            return MapMessage.errorMessage("趣味练习不能为空");
        }
        String funTitle = SafeConverter.toString(funMap.get("fun_title"));
        if (StringUtils.isBlank(funTitle)) {
            return MapMessage.errorMessage("趣味练习模块标题不能为空");
        }
        //json直接转换的存在id=""的情况
        if (StringUtils.isBlank(funModelContent.getId())) {
            funModelContent.setId(null);
        }
        StudyModel funModel = new StudyModel();
        funModel.setModelType(StudyModelType.FUN);
        funModel.setTitle(funTitle);
        funModel.setTypeId(funModelContent.getId());
        modelList.add(funModel);
        template.setStudyModelList(modelList);
        return MapMessage.successMessage()
                .add("classical_chinese_template", template)
                .add("expound_model_content", expoundModelContent)
                .add("appreciate_model_content", appreciateModelContent)
                .add("recite_model_content", reciteModelContent)
                .add("fun_model_content", funModelContent);
    }
}
