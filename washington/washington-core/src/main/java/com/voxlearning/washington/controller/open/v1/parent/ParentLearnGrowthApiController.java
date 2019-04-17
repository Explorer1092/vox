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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFutureMap;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.ParentConfigNativeKey;
import com.voxlearning.utopia.api.constant.ParentConfigType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.Flower;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.SelfStudyConfigLoader;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyData;
import com.voxlearning.utopia.service.vendor.api.entity.SelfStudyConfigWrapper;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.mapper.ParentFunctionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author jiangpeng
 * @since 2016/10/18
 */
@Controller
@RequestMapping(value = "/v1/parent/learngrowth")
@Slf4j
public class ParentLearnGrowthApiController extends AbstractParentApiController {

    private final static String SELF_STUDY_CONFIG_LIST = "self_study_config_list";
    private final static String LEARN_CONTENT_CONFIG_LIST = "learn_content_config_list";
    private final static String GROWTH_CONTENT_CONFIG_LIST = "growth_content_config_list";
    private final static String DATA_STATISTICS = "data_statistics";


    private static Map<String, ParentLearnGrowthApiController.AbstractConfigProvider> configProviderMap = new HashMap<>();

    @Inject private RaikouSDK raikouSDK;

    @Inject private FlowerServiceClient flowerServiceClient;
    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;

    @ImportService(interfaceClass = SelfStudyConfigLoader.class)
    private SelfStudyConfigLoader selfStudyConfigLoader;


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        new SelfStudyConfigProvider(SELF_STUDY_CONFIG_LIST);
        new LearnContentConfigProvider(LEARN_CONTENT_CONFIG_LIST);
        new GrowthContentConfigProvider(GROWTH_CONTENT_CONFIG_LIST);
    }


    /**
     * 学习成长页配页
     * http://wiki.17zuoye.net/pages/viewpage.action?pageId=29665114#id-学习成长Tab-c－学习工具模块
     *
     * @return
     */
    @RequestMapping(value = "config.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage config() {
        Boolean isLogin = false;
        Boolean hasChild = false;
        Boolean isGraduated = false; //前提是有孩子
        try {
            validateRequired(REQ_STUDENT_ID, "学生ID");
            validateRequired(REQ_CONFIG_KEYS, "配置key");
            String sessionKey = getRequestString(REQ_SESSION_KEY);
            if (StringUtils.isNotBlank(sessionKey)) {
                validateRequest(REQ_STUDENT_ID, REQ_CONFIG_KEYS);
                isLogin = true;
            } else
                validateRequestNoSessionKey(REQ_STUDENT_ID, REQ_CONFIG_KEYS);
        } catch (IllegalArgumentException e) {
            return failMessage(e);
        }
        List<FairylandProduct> fairylandProducts = new ArrayList<>();
        StudentDetail studentDetail = null;
        Integer clazzLevel = null;
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (isLogin && studentId != 0L) { // 没有孩子为0;
            Boolean checkRelation = checkStudentParentRef(studentId, getCurrentParentId());
            if (!checkRelation) {
                return failMessage(RES_RESULT_STUDENT_NOT_RELATION_TO_PARENT);
            }
            studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            fairylandProducts = businessVendorServiceClient.getParentAvailableFairylandProducts(
                    getCurrentParent(), studentDetail, FairyLandPlatform.PARENT_APP, FairylandProductType.APPS);
            hasChild = true;
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            isGraduated = clazz == null || clazz.isTerminalClazz();
            clazzLevel = studentDetail.getClazzLevelAsInteger();
        }
        List<String> keyList = JsonUtils.fromJsonToList(getRequestString(REQ_CONFIG_KEYS), String.class);
        if (keyList == null)
            return failMessage("error key");
        Boolean hasStatics = false;
        if (keyList.contains(DATA_STATISTICS)) {
            keyList.remove(DATA_STATISTICS);
            hasStatics = true;
        }


        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        MapMessage resultMap = successMessage();

        final ConfigContext context = new ConfigContext();
        context.setVersion(version);
        context.setIsLogin(isLogin);
        context.setClazzLevel(SafeConverter.toInt(clazzLevel));
        context.setStudentId(studentId);
        context.setHasChild(hasChild);
        context.setFairylandProducts(fairylandProducts);
        context.setIsGraduated(isGraduated);
        context.setStudentDetail(studentDetail);
        keyList.forEach(p -> {
            if (configProviderMap.containsKey(p)) {
                List<ParentFunctionConfig> parentFunctionConfigList1 = configProviderMap.get(p)
                        .generateConfig(context);
                resultMap.add(p, toResultMap(parentFunctionConfigList1, context));
            }
        });
        //他么的马后炮又要加上一个b统计数据 什么狗屁排行榜,彻底打乱了设计。有排行行的直接处理了。
        if (hasStatics) {
            resultMap.add(DATA_STATISTICS, rankMap(studentId));
        }

        return resultMap;
    }


    private Map<String, Object> rankMap(Long studentId) {

        String rRank = "- -";
        String pRank = "- -";
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        if (studentId != null && studentId != 0 && (clazz != null && !clazz.isTerminalClazz())) {
            //奖励榜
            List<GroupMapper> studentGroups = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
            if (CollectionUtils.isNotEmpty(studentGroups)) {
                List<Long> studentIds = raikouSDK.getClazzClient()
                        .getGroupStudentTupleServiceClient()
                        .findByGroupIds(studentGroups.stream().map(GroupMapper::getId).collect(Collectors.toSet()))
                        .stream()
                        .sorted(Comparator.comparing(GroupStudentTuple::getCreateTime))
                        .map(GroupStudentTuple::getStudentId)
                        .distinct()
                        .collect(Collectors.toList());
                List<Long> parentStudentIds = new ArrayList<>();
                parentStudentIds.addAll(studentIds);

                int rank = 1;
                int integralRank = 1;
                for (Long sid : studentIds) {
                    if (studentId.equals(sid)) {
                        integralRank = rank;
                    }
                    rank++;
                }

                rRank = "No." + integralRank;

                //家长榜
                List<Map<String, Object>> flowerRank = flowerRank(studentId);
                if (CollectionUtils.isNotEmpty(flowerRank)) {
                    int parentRank = 0;
                    Comparator<Map<String, Object>> c = (a, b) -> ((Long) SafeConverter.toLong(b.get("flowerCount"))).compareTo(SafeConverter.toLong(a.get("flowerCount")));
                    c = c.thenComparing((a, b) -> ((Long) SafeConverter.toLong(a.get("lastTime"))).compareTo(SafeConverter.toLong(b.get("lastTime"))));
                    c = c.thenComparing(Comparator.comparingInt(a -> parentStudentIds.indexOf(SafeConverter.toLong(a.get("studentId")))));
                    flowerRank = flowerRank
                            .stream()
                            .sorted(c)
                            .collect(Collectors.toList());

                    for (Map<String, Object> map : flowerRank) {
                        parentRank++;
                        if (studentId.equals(map.get("studentId"))) {
                            break;
                        }
                    }
                    pRank = "No." + parentRank;
                }
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(RES_MONTH_INTEGRAL_RANK, rRank);
        map.put(RES_PARENT_RANK, pRank);
        return map;
    }

    /**
     * 班级同学的送花列表
     *
     * @param studentId
     * @return
     */
    protected List<Map<String, Object>> flowerRank(Long studentId) {

        if (studentId == null || studentId <= 0) {
            return null;
        }

        List<Map<String, Object>> rankList = new ArrayList<>();

        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, true);

        if (CollectionUtils.isEmpty(groupMappers)) {
            return null;
        }

        List<GroupMapper.GroupUser> userList = new ArrayList<>();
        groupMappers.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getStudents() != null)
                .forEach(p -> userList.addAll(p.getStudents()));

        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }

        Set<Long> studentIds = userList.stream().map(GroupMapper.GroupUser::getId).collect(Collectors.toSet());
        AlpsFutureMap<Long, List<Flower>> futureMap = new AlpsFutureMap<>();
        for (Long id : studentIds) {
            futureMap.put(id, flowerServiceClient.getFlowerService().loadSenderFlowers(id));
        }
        Map<Long, Student> studentMap = studentLoaderClient.loadStudents(studentIds);
        if (MapUtils.isEmpty(studentMap)) {
            return null;
        }

        MonthRange range = MonthRange.current();
        studentIds.forEach(userId -> {
            Map<String, Object> map = new HashMap<>();
            List<Flower> flowerList = futureMap.getUninterruptibly(userId);

            Student student = studentMap.get(userId);
            if (student != null) {
                map.put("studentId", userId);
                map.put("studentName", student.fetchRealname());

                if (CollectionUtils.isEmpty(flowerList)) {
                    map.put("flowerCount", 0L);
                    map.put("lastTime", 0L);
                } else {
                    Object flowerCount = flowerList.stream().filter(p -> range.contains(p.getCreateDatetime())).count();
                    map.put("flowerCount", flowerCount);

                    Comparator<Flower> c = (o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime());
                    flowerList = flowerList.stream().sorted(c)
                            .collect(Collectors.toList());
                    map.put("lastTime", flowerList.get(0).getCreateDatetime().getTime());
                }
                rankList.add(map);
            }
        });

        return rankList;
    }

    /**
     * 转map
     * 如果配置,不支持未登录显示,则跳登录页。
     * 如果 不支持没有孩子不显示,则跳绑孩子页
     *
     * @return
     */
    private List<Map<String, Object>> toResultMap(List<ParentFunctionConfig> parentFunctionConfigs, ConfigContext context) {

        Boolean isLogin = context.getIsLogin();
        Long studentId = context.getStudentId();
        Boolean hasChild = context.getHasChild();
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        parentFunctionConfigs.forEach(config -> {
            Map<String, Object> map = new HashMap<>();
            addIntoMap(map, RES_CONFIG_NAME, config.getConfigName());
            addIntoMap(map, RES_CONFIG_ORDER, config.getConfigOrder());
            if (!isLogin) {
                if (!config.getNoLoginSupport()) {
                    config.setConfigType(ParentConfigType.NATIVE.name());
                    config.setConfigKey(ParentConfigNativeKey.LOGIN_PAGE.name());
                }
            } else {
                if (!hasChild) {
                    if (!config.getNoChildSupport()) {
                        config.setConfigType(ParentConfigType.NATIVE.name());
                        config.setConfigKey(ParentConfigNativeKey.BIND_CHILD_PAGE.name());
                    }
                }
            }
            addIntoMap(map, RES_CONFIG_TYPE, config.getConfigType());
            String configKey = config.getConfigKey();
            if (config.getConfigType().equals(ParentConfigType.H5.name())) {
                if (!configKey.contains("sid=")) {
                    if (!configKey.contains("?")) {
                        config.setConfigKey(configKey + "?sid=" + studentId + "&rel=learngrowth");
                    } else
                        config.setConfigKey(configKey + "&sid=" + studentId + "&rel=learngrowth");
                }
            }

            addIntoMap(map, RES_CONFIG_KEY, configKey);
            addIntoMap(map, RES_CONFIG_DEFAULT_LABEL, config.getConfigDefaultLabel());
            addIntoMap(map, RES_CONFIG_REMINDING_ID, config.getConfigRemindingId());
            addIntoMap(map, RES_CONFIG_OPERATION_LABEL, config.getConfigOperationLabel());
            addIntoMap(map, RES_CONFIG_REMINDING_TYPE, config.getConfigRemindingType());
            addIntoMap(map, RES_CONFIG_REMINDING_NUMBER, config.getConfigRemindingNumber());
            addIntoMap(map, RES_CONFIG_ICON_URL, config.getIconUrl());
            resultMapList.add(map);
        });
        return resultMapList;
    }


    abstract class AbstractConfigProvider {
        protected final String pageName = "parentLearnGrowthConfig";

        String configKey;

        AbstractConfigProvider(String key) {
            this.configKey = key;
            configProviderMap.put(key, this);
        }

        Boolean showControl(ParentFunctionConfig config, ConfigContext context) {
            if (config == null)
                return true;

            //登录 没孩子显示控制
            if (config.getNoLoginShow() != null) {
                if (!context.getIsLogin() && !config.getNoLoginShow())
                    return false;
            }
            if (config.getNoChildShow() != null) {
                if (!context.getHasChild() && !config.getNoChildShow())
                    return false;
            }

            //版本控制  版本不符合就直接不显示
            if (StringUtils.isNotBlank(config.getStartVersion()) && StringUtils.isNotBlank(context.getVersion())) {
                if (VersionUtil.compareVersion(context.getVersion(), config.getStartVersion()) < 0) {
                    return false;
                }
            }

            //灰度控制
            //没有学生的,后面会过滤
            if (context.getStudentDetail() != null && StringUtils.isNotBlank(config.getGreyMain()) && StringUtils.isNotBlank(config.getGreySub())) {
                Boolean hitGrey = grayFunctionManagerClient.getStudentGrayFunctionManager()
                        .isWebGrayFunctionAvailable(context.getStudentDetail(), config.getGreyMain(), config.getGreySub(), true);
                if (!hitGrey)
                    return false;
            }

            //非灰度控制
            //这个灰度中了,则不显示这个配置
            if (context.getStudentDetail() != null && StringUtils.isNotBlank(config.getReverseGreyMain()) && StringUtils.isNotBlank(config.getReverseGreySub())) {
                Boolean hitGrey = grayFunctionManagerClient.getStudentGrayFunctionManager()
                        .isWebGrayFunctionAvailable(context.getStudentDetail(), config.getReverseGreyMain(), config.getReverseGreySub(), true);
                if (hitGrey)
                    return false;
            }
            //年级控制
            if (CollectionUtils.isNotEmpty(config.getClazzLevels()) && context.getClazzLevel() != null && context.getClazzLevel() != 0) {
                if (!config.getClazzLevels().contains(context.getClazzLevel()))
                    return false;
            }

            return true;
        }

        /**
         * @param context@return
         */
        abstract List<ParentFunctionConfig> generateConfig(ConfigContext context);
    }

    /**
     * 成长内容模块配置
     */
    private class GrowthContentConfigProvider extends AbstractConfigProvider {

        GrowthContentConfigProvider(String key) {
            super(key);
        }

        @Override
        List<ParentFunctionConfig> generateConfig(ConfigContext context) {

            List<ParentFunctionConfig> configList =
                    pageBlockContentServiceClient.loadConfigList(pageName, configKey, ParentFunctionConfig.class);

            //TODO 接下来处理一些 运营消息神马的
            configList = configList == null ? new ArrayList<>() : configList;
            return configList.stream().sorted(
                    Comparator.comparingInt(o -> SafeConverter.toInt(o.getConfigOrder())))
                    .filter(t -> showControl(t, context)).collect(Collectors.toList());

        }
    }


    /**
     * 学习内容模块配置
     */
    private class LearnContentConfigProvider extends AbstractConfigProvider {

        LearnContentConfigProvider(String key) {
            super(key);
        }

        @Override
        List<ParentFunctionConfig> generateConfig(ConfigContext context) {
            if (context == null)
                return Collections.emptyList();
            Long studentId = context.getStudentId();
            List<ParentFunctionConfig> configList = pageBlockContentServiceClient.loadConfigList(pageName, configKey, ParentFunctionConfig.class);
            if (CollectionUtils.isEmpty(configList))
                configList = new ArrayList<>();

            for (ParentFunctionConfig config : configList) {
                if (!showControl(config, context))
                    continue;
                //若最近30天有作业，显示最新一份被检查的作业提示（即老师检查作业的时间为最新）
                //显示内容为：【布置日期】【科目】作业已检查，例 8月12日英语作业已检查
                //        点击后恢复默认副标题
                // 按group-group-group 缓存一天的最新作业
                if (StringUtils.equals("zybg", config.getConfigAlias())) {
                    NewHomework.Location lastCheckedHomework = getLastCheckedHomeworkWrapCache(studentId);
                    if (lastCheckedHomework != null) {
                        String text = DateUtils.dateToString(new Date(lastCheckedHomework.getCreateTime()), "MM月dd日")
                                + lastCheckedHomework.getSubject().getValue() + "作业已检查";
                        config.setConfigOperationLabel(text);
                        config.setConfigRemindingId("zybg-" + lastCheckedHomework.getId());
                    }
                }
                if (StringUtils.equals("wdzx", config.getConfigAlias())) {
                    List<MySelfStudyData> mySelfStudyDatas = mySelfStudyService.loadMySelfStudyDateBySId(studentId);
                    if (CollectionUtils.isNotEmpty(mySelfStudyDatas)) {
                        MySelfStudyData lastNewData = mySelfStudyDatas.stream()
                                .filter(data -> SelfStudyType.upperList.contains(data.getSelfStudyType()))
                                .sorted((o1, o2) -> o2.getLastUseDate().compareTo(o1.getLastUseDate())).findFirst().orElse(null);
                        if (lastNewData != null && StringUtils.isNotBlank(lastNewData.getStudyProgress())) {
                            String text;
                            String productName = lastNewData.getSelfStudyType().getDesc();
                            if (SelfStudyType.KUPAOWORD_ENGLISH == lastNewData.getSelfStudyType()) {
                                text = productName + "，" + lastNewData.getStudyProgress();
                            } else
                                text = "上次学到" + productName + lastNewData.getStudyProgress();
                            config.setConfigDefaultLabel(text);
                        }
                    }

                }
            }
            return configList.stream().sorted(
                    Comparator.comparingInt(o -> SafeConverter.toInt(o.getConfigOrder())))
                    .collect(Collectors.toList());

        }

        private NewHomework.Location getLastCheckedHomeworkWrapCache(Long studentId) {
            List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
            List<Long> sortedGroupIdList = groupMappers.stream().map(GroupMapper::getId).sorted(Long::compare).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(sortedGroupIdList))
                return null;

            String key = "last_checked_hwl_" + StringUtils.join(sortedGroupIdList.toArray(), ",");

            NewHomework.Location newHomework;
            CacheObject<NewHomework.Location> objectCacheObject = washingtonCacheSystem.CBS.flushable.get(key);
            if (objectCacheObject == null || objectCacheObject.getValue() == null) {
                newHomework = lastCheckedHomework(sortedGroupIdList);
                if (newHomework != null)
                    washingtonCacheSystem.CBS.flushable.set(key, 86400, newHomework);
            } else
                newHomework = objectCacheObject.getValue();
            return newHomework;
        }


        private NewHomework.Location lastCheckedHomework(Collection<Long> groupIds) {
            Date current = new Date();
            List<NewHomework.Location> locations = newHomeworkPartLoaderClient.loadNewHomeworkByClazzGroupId(groupIds, DateUtils.calculateDateDay(current, -30), current);
            if (CollectionUtils.isEmpty(locations)) {
                return null;
            }
            return locations.stream().filter(NewHomework.Location::isChecked).sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime())).findFirst().orElse(null);
        }
    }


    /**
     * 学习内容模块配置
     */
    private class SelfStudyConfigProvider extends AbstractConfigProvider {

        SelfStudyConfigProvider(String key) {
            super(key);
        }

        @Override
        List<ParentFunctionConfig> generateConfig(ConfigContext context) {
            if (context == null)
                return Collections.emptyList();
            String version = context.getVersion();
            Boolean isLogin = context.getIsLogin();
            Integer clazzLevel = context.getClazzLevel();
            Long studentId = context.getStudentId();
            Boolean hasChild = context.getHasChild();
            List<FairylandProduct> fairylandProductList = context.getFairylandProducts();
            Boolean isGraduated = context.getIsGraduated();
            Set<String> appKeySet = fairylandProductList.stream().map(FairylandProduct::getAppKey).collect(Collectors.toSet());

            List<SelfStudyConfigWrapper> selfStudyConfigWrappers = selfStudyConfigLoader.loadSelfStudyConfig4LearnGrowth(clazzLevel, version);

            LinkedList<ParentFunctionConfig> parentFunctionConfigList = new LinkedList<>();
            SelfStudyType the7thSelfStudyType = SelfStudyType.UNKNOWN; //不知道为什么要取第7项的type，还是保留原逻辑吧
            for (SelfStudyConfigWrapper sc : selfStudyConfigWrappers) {
                if (sc.getOrder() == 99)
                    continue;
                if (!isLogin && !sc.getNoLoginShow()) // 当前未登录 并且该配置不支持未登录显示,则返回
                    continue;
                if (isLogin && !hasChild && !sc.getNoChildShow())  //登录状态下,没有孩子,并且该配置不支持没有孩子显示,则返回;
                    continue;
                SelfStudyType selfStudyType = SelfStudyType.of(sc.getSelfStudyType());
                if (isLogin && hasChild && isGraduated && !selfStudyType.getIsFree())   // 有孩子, 没班级或者毕业班只显示 免费的3个
                    continue;
                //该应用对该学生不可用。
                if (!selfStudyType.getIsFree() && selfStudyType != SelfStudyType.UNKNOWN && !appKeySet.contains(selfStudyType.getOrderProductServiceType()))
                    continue;
                if (sc.getToolType() == ParentConfigType.H5) {
                    sc.setToolKey(generateUrlByType(sc.getToolKey(), OrderProductServiceType.safeParse(selfStudyType.getOrderProductServiceType()), studentId));
                }
                ParentFunctionConfig parentFunctionConfig = ParentFunctionConfig.fromSelfStudyConfigWrapper(sc);
                if (!showControl(parentFunctionConfig, context))
                    continue;
                parentFunctionConfig.setIconUrl(getCdnBaseUrlStaticSharedWithSep() + parentFunctionConfig.getIconUrl());
                parentFunctionConfigList.add(parentFunctionConfig);
                if (parentFunctionConfigList.size() == 7) {
                    the7thSelfStudyType = selfStudyType;
                } else if (parentFunctionConfigList.size() > 8) {
                    break;
                }
            }
            if (parentFunctionConfigList.size() > 8) {
                //如果大于8个，显示7个外加一个"更多"
                List<ParentFunctionConfig> parentFunctionConfigs = parentFunctionConfigList.subList(0, 7);
                ParentFunctionConfig more = selfStudyConfigWrappers.stream().filter(sc -> sc.getOrder() == 99).map(ParentFunctionConfig::fromSelfStudyConfigWrapper).findFirst().orElse(null);
                more.setConfigKey(generateUrlByType(more.getConfigKey(), OrderProductServiceType.safeParse(the7thSelfStudyType.getOrderProductServiceType()), studentId));
                more.setIconUrl(getCdnBaseUrlStaticSharedWithSep() + more.getIconUrl());
                parentFunctionConfigs.add(more);

                return parentFunctionConfigs;
            }

            return parentFunctionConfigList;

        }

        private String generateUrlByType(String h5Url, OrderProductServiceType orderProductServiceType, Long sid) {
            if (orderProductServiceType != null) {
                String productType = "";
                if (OrderProductServiceType.FeeCourse != orderProductServiceType) {
                    productType = "&productType=" + orderProductServiceType.name();
                }
                if (h5Url.contains("?")) {
                    return h5Url + "&sid=" + sid + productType + "&rel=learngrowth";
                } else
                    return h5Url + "?sid=" + sid + productType + "&rel=learngrowth";
            } else {
                if (h5Url.contains("?")) {
                    return h5Url + "&sid=" + sid + "&rel=learngrowth";
                } else
                    return h5Url + "?sid=" + sid + "&rel=learngrowth";
            }
        }
    }


}
