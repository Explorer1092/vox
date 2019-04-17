package com.voxlearning.utopia.admin.controller.studyTogether.content;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.studycourse.api.CrmCourseStructSpuLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmLearnLinkLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmNewTemplateLoader;
import com.voxlearning.galaxy.service.studycourse.api.CrmNewTemplateService;
import com.voxlearning.galaxy.service.studycourse.api.entity.component.content.LearnLink;
import com.voxlearning.galaxy.service.studycourse.api.entity.component.template.*;
import com.voxlearning.galaxy.service.studycourse.constant.ChangeLogType;
import com.voxlearning.galaxy.service.studycourse.constant.NewTemplateType;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.admin.controller.studyTogether.AbstractStudyTogetherController;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.wonderland.api.data.WonderlandResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wei.jiang
 * @since 19-3-5 下午4:27
 */
@Controller
@RequestMapping(value = "opmanager/studyTogether/newTemplate/")
@Slf4j
public class CrmNewTemplateController extends AbstractStudyTogetherController {

    @ImportService(interfaceClass = CrmNewTemplateLoader.class)
    private CrmNewTemplateLoader crmNewTemplateLoader;

    @ImportService(interfaceClass = CrmNewTemplateService.class)
    private CrmNewTemplateService crmNewTemplateService;

    @ImportService(interfaceClass = CrmCourseStructSpuLoader.class)
    private CrmCourseStructSpuLoader crmCourseStructSpuLoader;

    @ImportService(interfaceClass = CrmLearnLinkLoader.class)
    private CrmLearnLinkLoader learnLinkLoader;

    @Inject
    private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @RequestMapping(value = "newTemplate_list_page.vpage", method = RequestMethod.GET)
    public String newTemplateListPage() {
        return "/opmanager/studyTogether/temlate/component/newtemplatelist";
    }


    @RequestMapping(value = "get_newTemplate_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getNewTemplateList(Model model) {
        Integer page = getRequestInt("page", 1);
        long templateId = getRequestLong("template_id");
        String templateName = getRequestString("template_name");
        String createUser = getRequestString("create_user");
        String templateType = getRequestString("template_type");

        MapMessage mapMessage = MapMessage.successMessage();

        //前端输入回显
        mapMessage.add("page", page);
        mapMessage.add("template_id", templateId != 0 ? templateId : "");
        mapMessage.add("create_user", createUser);
        mapMessage.add("template_name", templateName);
        mapMessage.add("template_type", templateType);
        List<NewTemplate> templateList = new ArrayList<>();
        if (templateId != 0) {
            NewTemplate newTemplate = crmNewTemplateLoader.loadNewTemplateById(templateId);
            if (newTemplate != null) {
                templateList.add(newTemplate);
            }
        } else {
            templateList = crmNewTemplateLoader.loadAllNewTemplate();
        }
        if (StringUtils.isNotBlank(templateName)) {
            templateList = templateList.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getName()))
                    .filter(p -> p.getName().contains(templateName))
                    .collect(Collectors.toList());
        }

        if (StringUtils.isNotBlank(templateType)) {
            templateList = templateList.stream()
                    .filter(p -> p.getTemplateType() != null)
                    .filter(p -> p.getTemplateType() == NewTemplateType.valueOf(templateType))
                    .collect(Collectors.toList());
        }

        if (StringUtils.isNotBlank(createUser)) {
            templateList = templateList.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getCreateUser()))
                    .filter(p -> p.getCreateUser().contains(createUser))
                    .collect(Collectors.toList());
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        templateList.sort((o1, o2) -> o2.getId().compareTo(o1.getId()));
        Page<NewTemplate> templatePage = PageableUtils.listToPage(templateList, new PageRequest(page - 1, 10));
        List<NewTemplate> returnTemplateList = templatePage.getContent();

        returnTemplateList.forEach(p -> {
            Map<String, Object> map = new HashMap<>();
            //id
            map.put("id", p.getId());
            //模板名称
            map.put("name", p.getName());
            //创建者
            map.put("create", p.getCreateUser());
            //类型
            map.put("type", p.getTemplateType().name());
            mapList.add(map);
        });

        mapMessage.add("total_page", templatePage.getTotalPages());
        mapMessage.add("total_count", templatePage.getTotalElements());
        mapMessage.add("new_template_list", mapList);
        return mapMessage;
    }

    @RequestMapping(value = "newTemplate_info_page.vpage", method = RequestMethod.GET)
    public String newTemplateInfoPage() {
        return "/opmanager/studyTogether/temlate/component/newtemplateinfo";
    }


    @RequestMapping(value = "get_newTemplate_info.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getNewTemplateInfo() {
        long templateId = getRequestLong("template_id");

        MapMessage mapMessage = MapMessage.successMessage();
        //是否允许编辑
        mapMessage.add("edit", getRequestBool("edit"));
        mapMessage.add("admin_user", getCurrentAdminUser().getAdminUserName());
        mapMessage.add("cdn_host", StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")));

        if (templateId == 0) {
            return mapMessage;
        }

        NewTemplate template = crmNewTemplateLoader.loadNewTemplateById(templateId);
        if (template == null) {
            getAlertMessageManager().addMessageError("您要处理的模板不存在");
            return mapMessage;
        }


        mapMessage.add("template", template);

        return mapMessage;

    }


    @RequestMapping(value = "save_newTemplate.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveNewTemplate() {
        long templateId = getRequestLong("template_id");
        String template = getRequestString("template");
        String templateName = getRequestString("name");
        String templateType = getRequestString("template_type");
        String linkIds = getRequestString("link_ids");
        NewTemplate newTemplate = new NewTemplate();
        TemplateInfo templateInfo = new TemplateInfo();
        NewTemplateType type = NewTemplateType.valueOf(templateType);
        newTemplate.setTemplateType(type);
        newTemplate.setName(templateName);
        switch (type) {
            case SinologyText:
                SinologyTextTemplate sinologyTextTemplate = JsonUtils.fromJson(template, SinologyTextTemplate.class);
                templateInfo.setSinologyTextTemplate(sinologyTextTemplate);
                break;
            case BookAudio:
                BookAudioTemplate content2 = JsonUtils.fromJson(template, BookAudioTemplate.class);
                templateInfo.setBookAudioTemplate(content2);
                break;
            case ImgTextReading:
                ImgTextReadingTemplate content3 = JsonUtils.fromJson(template, ImgTextReadingTemplate.class);
                templateInfo.setImgTextReadingTemplate(content3);
                break;
            case BookImgText:
                BookImgTextTemplate content4 = JsonUtils.fromJson(template, BookImgTextTemplate.class);
                templateInfo.setBookImgTextTemplate(content4);
                break;
            case PicBookId:
                PicBookIdTemplate content5 = JsonUtils.fromJson(template, PicBookIdTemplate.class);
                if (content5 != null && StringUtils.isNotBlank(content5.getPictureBookId())) {
                    PictureBookPlus pictureBookPlus = pictureBookPlusServiceClient.loadById(content5.getPictureBookId());
                    if (pictureBookPlus == null) {
                        return MapMessage.errorMessage("绘本ID不正确");
                    }
                }
                templateInfo.setPicBookIdTemplate(content5);
                break;
            default:
                break;
        }
        NewTemplate oldRecord = null;
        newTemplate.setId(templateId);
        newTemplate.setTemplateInfo(templateInfo);
        List<Long> learnLinkIds = JsonUtils.fromJsonToList(linkIds, Long.class);
        if (CollectionUtils.isEmpty(learnLinkIds)) {
            return MapMessage.errorMessage("环节ID不能为空");
        }
        for (Long linkId : learnLinkIds) {
            LearnLink learnLink = learnLinkLoader.loadLearnLinkById(linkId);
            if (learnLink == null) {
                return MapMessage.errorMessage("环节ID错误：" + linkId);
            }
        }
        newTemplate.setLinkIds(learnLinkIds);
        if (templateId == 0L) {
            newTemplate.setCreateUser(getCurrentAdminUser().getAdminUserName());
            newTemplate.setUpdateUser(getCurrentAdminUser().getAdminUserName());
            try {
                Long currentId = AtomicLockManager.getInstance().wrapAtomic(crmNewTemplateLoader).keyPrefix("NEW_TEMPLATE_INCR_ID").keys(templateId).proxy().loadMaxId();
                newTemplate.setId(currentId + 1);
            } catch (Exception e) {
                logger.error("lock error {}", e.getMessage(), e);
                return WonderlandResult.ErrorType.DUPLICATED_OPERATION.result();
            }
        } else {
            oldRecord = crmNewTemplateLoader.loadNewTemplateById(templateId);
            newTemplate.setUpdateUser(getCurrentAdminUser().getAdminUserName());
        }
        newTemplate = crmNewTemplateService.saveNewTemplate(newTemplate);
        if (newTemplate != null) {
            if (oldRecord == null) {
                studyCourseBlackWidowServiceClient.justAddChangeLog("组件化模板", getCurrentAdminUser().getAdminUserName(),
                        ChangeLogType.NEW_TEMPLATE, newTemplate.getId().toString(), "新增组件化模板");
            } else {
                studyCourseBlackWidowServiceClient.compareAndSaveChangeLog("", oldRecord, newTemplate, getCurrentAdminUser().getAdminUserName(),
                        ChangeLogType.NEW_TEMPLATE, newTemplate.getId().toString());
            }
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage();
    }

}
