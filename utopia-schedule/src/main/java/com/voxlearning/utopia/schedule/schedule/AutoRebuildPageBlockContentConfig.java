package com.voxlearning.utopia.schedule.schedule;

/**
 * @author jiangpeng
 * @since 2018-07-25 下午3:34
 **/

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "重建配置",
        jobDescription = "重建配置",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 30 4 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoRebuildPageBlockContentConfig  extends ScheduledJobWithJournalSupport {

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;
    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;


    String text = "parentAlbumAbilityTagPlanBConfig,parentAlbumAbilityTagPlanB;\n" +
            "parentAlbumAbilityTagConfig,parentAlbumAbilityTag;\n" +
            "selfStudyAdConfig,selfStudyAd;\n" +
            "studyTogetherTextList,studyTogetherTextList;\n" +
            "parentAlbumAbilityTag,parentAlbumTagComment;\n" +
            "parentChannelAppConfig,parentChannelAppConfig;\n" +
            "teacherLearningAlbumTopConfig,teacherLearningAlbumTopConfig;\n" +
            "teacherLearningAlbumConfig,teacherLearningAlbumConfig;\n" +
            "parentHotSearchWordsConfig,parentHotSearchWords;\n" +
            "parentTabConfig,parentTabList_New;\n" +
            "parentLearnGrowthConfig,growth_content_config_list;\n" +
            "SelfStudyConfig,SelfStudyBasicConfig;\n" +
            "SelfStudyConfig,SelfStudyOpConfig;\n" +
            "parentLearnGrowthConfig,learn_content_config_list;\n" +
            "parentUserCenterConfig,studentFunctionList;\n" +
            "parentUserCenterConfig,operativeFunctionList;\n" +
            "parentUserCenterConfig,advertisementFunctionList";

    @Data
    private static class ConfigKey{
        private String pageName;
        private String blockName;
    }

     /**
     * @param jobJournalLogger
     * @param startTimestamp
     * @param parameters
     * @param progressMonitor
     * @throws Exception
     */
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String data = SafeConverter.toString(parameters.get("data"));
        if (StringUtils.isBlank(data)) {
            data = text;
        }
        List<ConfigKey> keyList = Lists.newArrayList();
        String[] split = data.replace("\n", "").trim().split(";");
        for (String s : split) {
            String[] split1 = s.split(",");
            if (split1.length != 2)
                continue;
            ConfigKey configKey = new ConfigKey();
            configKey.setPageName(split1[0]);
            configKey.setBlockName(split1[1]);
            keyList.add(configKey);
        }
        for (ConfigKey configKey : keyList) {
            if (RuntimeMode.isUsingTestData()){
                String oldKey = configKey.getBlockName() + "_TEST";
                PageBlockContent oldContent = getBlockContent(configKey.getPageName(), oldKey);
                if (oldContent == null){
                    logger.error("rebuildConfig- loadOld 没有获取到，pageName:{}, blockName:{}", configKey.getPageName(), oldKey);
                    continue;
                }
                String content = oldContent.getContent();
                if (StringUtils.isBlank(content)){
                    logger.error("rebuildConfig- loadOld 获取到内容未 null，pageName:{}, blockName:{}", configKey.getPageName(), oldKey);
                    continue;
                }
                content = "{\"TEST\":" + content.replaceAll("\n|\r|\t", "").trim() + "}";
                Map<String, Object> map = JsonUtils.fromJson(content);
                if (map == null){
                    logger.error("rebuildConfig- 新内容 转换成 map 失败 ：json = {}", content);
                    continue;
                }
                content = JsonUtils.toJson(map);

                PageBlockContent upsertContent = null;
                PageBlockContent exsited = getBlockContent(configKey.getPageName(), configKey.getBlockName());
                if (exsited != null){
                    upsertContent = exsited;
                }else {
                    upsertContent = oldContent;
                    upsertContent.setId(null);
                }
                upsertContent.setContent(content);
                upsertContent.setBlockName(configKey.getBlockName());

                PageBlockContent pageBlockContent = crmConfigService.$upsertPageBlockContent(upsertContent);
                if (pageBlockContent == null){
                    logger.error("rebuildConfig- 存储新的 content 失败！pageName = {}, blockName = {}", configKey.getPageName(), configKey.getBlockName());
                }
            }else if (RuntimeMode.isStaging()){
                String stagingKey = configKey.getBlockName() + "_STAGING";
                String prodKey = configKey.getBlockName() + "_PRODUCTION";
                PageBlockContent stagingContent = getBlockContent(configKey.getPageName(), stagingKey);
                if (stagingContent == null){
                    logger.warn("rebuildConfig- loadOld 没有获取到，pageName:{}, blockName:{}", configKey.getPageName(), stagingKey);
                }
                PageBlockContent prodContent = getBlockContent(configKey.getPageName(), prodKey);
                if (prodContent == null){
                    logger.warn("rebuildConfig- loadOld 没有获取到，pageName:{}, blockName:{}", configKey.getPageName(), prodKey);
                }
                if (stagingContent == null && prodContent == null){
                    logger.error("rebuildConfig- 为毛线staging 和 prod 都没这个配置？pageName:{}, blockName:{}", configKey.getPageName(), configKey.getBlockName());
                    continue;
                }
                Map<String, Object> newConfigMap = new HashMap<>();
                if (stagingContent != null){
                    String content = stagingContent.getContent().replaceAll("\n|\r|\t", "").trim();
                    List<Object> objects = JsonUtils.fromJsonToList(content, Object.class);
                    if (CollectionUtils.isNotEmpty(objects)){
                        newConfigMap.put("STAGING", objects);
                    }else {
                        newConfigMap.put("STAGING", Lists.newArrayList());
                    }
                }else {
                    newConfigMap.put("STAGING", Lists.newArrayList());
                }

                if (prodContent != null){
                    String content = prodContent.getContent().replaceAll("\n|\r|\t", "").trim();
                    List<Object> objects = JsonUtils.fromJsonToList(content, Object.class);
                    if (CollectionUtils.isNotEmpty(objects)){
                        newConfigMap.put("PRODUCTION", objects);
                    }else {
                        newConfigMap.put("PRODUCTION", Lists.newArrayList());
                    }
                }else {
                    newConfigMap.put("PRODUCTION", Lists.newArrayList());
                }

                String newContent = JsonUtils.toJson(newConfigMap);

                PageBlockContent newPageBlockContent = null;
                PageBlockContent existedContent = getBlockContent(configKey.getPageName(), configKey.getBlockName());
                if (existedContent != null){
                    newPageBlockContent = existedContent;
                }else {
                    if (prodContent != null) {
                        newPageBlockContent = prodContent;
                    } else {
                        newPageBlockContent = stagingContent;
                    }
                    newPageBlockContent.setId(null);
                }

                newPageBlockContent.setBlockName(configKey.getBlockName());
                newPageBlockContent.setContent(newContent);

                PageBlockContent pageBlockContent = crmConfigService.$upsertPageBlockContent(newPageBlockContent);
                if (pageBlockContent == null){
                    logger.error("rebuildConfig- 存储新的 content 失败！pageName = {}, blockName = {}", configKey.getPageName(), configKey.getBlockName());
                }
            }
        }

    }


    private PageBlockContent getBlockContent(String pageName, String blockName){
        List<PageBlockContent> byPageName = pageBlockContentServiceClient.getPageBlockContentBuffer().findByPageName(pageName);
        if (CollectionUtils.isEmpty(byPageName))
            return null;
        return byPageName.stream().filter(t -> t.getBlockName().equals(blockName)).findFirst().orElse(null);
    }
}
