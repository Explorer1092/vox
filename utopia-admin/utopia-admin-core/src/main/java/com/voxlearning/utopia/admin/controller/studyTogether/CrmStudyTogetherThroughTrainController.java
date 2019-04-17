package com.voxlearning.utopia.admin.controller.studyTogether;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.utopia.admin.cache.AdminCacheSystem;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ThroughTrainConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2018-10-22 下午3:36
 **/
@Controller
@RequestMapping("opmanager/studyTogether/through_train")
@Slf4j
public class CrmStudyTogetherThroughTrainController extends AbstractAdminSystemController {
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;
    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Inject
    private AdminCacheSystem adminCacheSystem;

    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    /**
     * 进入列表
     */
    @RequestMapping(value = "/config.vpage", method = RequestMethod.GET)
    public String joinUpPageList(Model model) {
        PageBlockContent throughTrainContent = null;
        List<PageBlockContent> contentList = pageBlockContentServiceClient
                .getPageBlockContentBuffer().findByPageName("studyTogether");
        if (CollectionUtils.isNotEmpty(contentList)){
            throughTrainContent = contentList.stream().filter(t -> t.getBlockName().equals("throughTrain") && !SafeConverter.toBoolean(t.getDisabled()))
                    .findFirst().orElse(null);
            if (throughTrainContent != null){
                List<ThroughTrainConfig> throughTrainConfigs = JsonUtils.fromJsonToList(throughTrainContent.getContent(), ThroughTrainConfig.class);
                List<Map<String, String>> collect = throughTrainConfigs.stream().map(t -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("lessonId", SafeConverter.toString(t.getFromSkuId()));
                    map.put("lessonIdList", StringUtils.join(t.getSkuIdList().toArray(), ","));
                    return map;
                }).collect(Collectors.toList());
                model.addAttribute("content", collect);
            }
        }
        String lastSaveFileUrl = getLastSaveFileUrl();
        if (lastSaveFileUrl != null){
            model.addAttribute("lastUrl", lastSaveFileUrl);
        }
        model.addAttribute("scheme", getScheme());
        return "opmanager/studyTogether/throughTrain";
    }

    private String getScheme() {
        if (RuntimeMode.isUsingTestData()){
            return "https://www.test.17zuoye.net";
        }else if (RuntimeMode.isStaging()){
            return "https://www.staging.17zuoye.net";
        }else if (RuntimeMode.isProduction()){
            return "https://www.17zuoye.com";
        }
        return "https://www.test.17zuoye.net";
    }

    @ResponseBody
    @RequestMapping(value = "/upload.vpage", method = RequestMethod.POST)
    public MapMessage upload(){
        HttpServletRequest request = getRequest();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        try {
            MultipartFile file = multipartRequest.getFile("file");
            if (file.isEmpty()) {
                logger.error("readRequestWorkbook - Empty MultipartFile with name = {}", "file");
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            if (!"csv".equals(fileExt)){
                return MapMessage.errorMessage("请上传 csv 格式文件！");
            }
            String content = new String(file.getBytes());
            String[] split = content.split("\n");
            List<ThroughTrainConfig> configList = new ArrayList<>(split.length);
            for (int j = 1; j< split.length ; j++) {
                String line = split[j];
                String[] lineArray = line.split(",");
                String cellValue = lineArray[0];
                if (StringUtils.isBlank(cellValue)){
                    continue;
                }
                int fromId = SafeConverter.toInt(cellValue);
                if (!checkSkuId(fromId)){
                    return MapMessage.errorMessage("错误的来源课程 id");
                }
                ThroughTrainConfig config = new ThroughTrainConfig();
                config.setFromSkuId(fromId);
                List<Integer> skuIdList = new ArrayList<>();
                for (int i = 1; i< lineArray.length ; i++){
                    String id = lineArray[i];
                    if (StringUtils.isBlank(id)){
                        continue;
                    }
                    int skuid = SafeConverter.toInt(id);
                    if (!checkSkuId(skuid)){
                        return MapMessage.errorMessage("错误的目标课程id");
                    }
                    skuIdList.add(skuid);
                }
                if (CollectionUtils.isEmpty(skuIdList)){
                    return MapMessage.errorMessage("没有合格的目标课程 id");
                }
                config.setSkuIdList(skuIdList);
                configList.add(config);
            }
            if (CollectionUtils.isEmpty(configList)){
                return MapMessage.errorMessage("一个课程配置都没有！");
            }
            String json = JsonUtils.toJson(configList);
            PageBlockContent throughTrainContent = null;

            List<PageBlockContent> contentList = pageBlockContentServiceClient
                    .getPageBlockContentBuffer().findByPageName("studyTogether");
            if (CollectionUtils.isNotEmpty(contentList)){
                throughTrainContent = contentList.stream().filter(t -> t.getBlockName().equals("throughTrain") && !SafeConverter.toBoolean(t.getDisabled()))
                        .findFirst().orElse(null);
                if (throughTrainContent != null){
                    throughTrainContent.setContent(json);
                }
            }
            if (throughTrainContent == null){
                throughTrainContent = new PageBlockContent();
                throughTrainContent.setContent(json);
                throughTrainContent.setPageName("studyTogether");
                throughTrainContent.setBlockName("throughTrain");
                throughTrainContent.setDisplayOrder(0);
                throughTrainContent.setStartDatetime(new Date());
                throughTrainContent.setEndDatetime(DateUtils.stringToDate("2038-12-31 23:59:59"));
                throughTrainContent.setMemo("一起学-直通车课程配置");
            }
            PageBlockContent pageBlockContent = crmConfigService.$upsertPageBlockContent(throughTrainContent);
            if (pageBlockContent != null){
                String url = AdminOssManageUtils.upload(file, "studytogether");
                saveLastSaveFileUrl(url);
                return MapMessage.successMessage();
            }else {
                return MapMessage.errorMessage(" 保存失败！");
            }

        }catch (Exception e){
            logger.error("上传一起学直通车配置失败：", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    private static String key = "StudytogetherThroughTrainFileUrl";
    private void saveLastSaveFileUrl(String url){
        adminCacheSystem.CBS.persistence.set(key, 0, url);
    }
    private String getLastSaveFileUrl(){
        return adminCacheSystem.CBS.persistence.load(key);
    }

    private boolean checkSkuId(Integer skuId){
        StudyLesson studyLesson = studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(skuId));
        return studyLesson != null;
    }
}
