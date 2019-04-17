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

package com.voxlearning.washington.controller.mobile;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.ParentSelfStudyTypeH5Mapper;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsTag;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.athena.ParentReportLoaderClient;
import com.voxlearning.washington.mapper.AlbumAbilityTagConfig;
import com.voxlearning.washington.mapper.AlbumAbilityTagPlanBConfig;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * app端webview家校通父类
 * 包括家长通和老师端app
 * Created by Shuai Huan on 2016/4/21.
 */
public class AbstractMobileJxtController extends AbstractMobileController {

    protected static final String OSS_IMAGE_HOST;
    protected static final String OSS_HOST;
    //新版家长能力标签根标签ID
    protected static final Long BASIC_ABILITY_TAG_ID = 432L;

    @Inject
    protected ParentReportLoaderClient parentReportLoaderClient;



    static {
        Map<String, String> configs = ConfigManager.instance().getCommonConfig().getConfigs();
        OSS_IMAGE_HOST = StringUtils.defaultString(configs.get("oss_homework_image_host"));
        OSS_HOST = StringUtils.defaultString(configs.get("oss_homework_host"));
    }


    //生成大数据标签和newsTag的对应关系
    protected List<AlbumAbilityTagConfig> generateParentAlbumAbilityTagConfig(Map<Integer, String> subjectTagMap) {
        if (MapUtils.isEmpty(subjectTagMap)) {
            return Collections.emptyList();
        }
        List<AlbumAbilityTagConfig> parentAlbumAbilityTagConfigList =
                pageBlockContentServiceClient.loadConfigList("parentAlbumAbilityTagConfig", "parentAlbumAbilityTag", AlbumAbilityTagConfig.class);
        if (CollectionUtils.isEmpty(parentAlbumAbilityTagConfigList)) {
            return Collections.emptyList();
        }
        List<AlbumAbilityTagConfig> parentAlbumTagBySubject = new ArrayList<>();

        for (Map.Entry<Integer, String> subjectAndTag : subjectTagMap.entrySet()) {
            parentAlbumTagBySubject.addAll(parentAlbumAbilityTagConfigList.stream().filter(e -> Objects.equals(e.getSubjectId(), subjectAndTag.getKey()) && e.getDataTags().keySet().contains(subjectAndTag.getValue())).collect(Collectors.toList()));
        }

        return parentAlbumTagBySubject;
    }

    protected Map<Integer, Set<Long>> getFitJxtNewsAlbumWithBigData(Long studentId) {
        List<GroupMapper> groupMappers = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        //group对应的subject
        Map<Long, Integer> groupSubject = new HashMap<>();
        groupMappers.forEach(group -> groupSubject.put(group.getId(), group.getSubject().getId()));
        //大数据接口返回每个学科的薄弱点
        Map<Integer, String> bigDataWeaknessMap = parentReportLoaderClient.getParentReportLoader().getStudyProgressWeakKp(studentId, groupSubject);
        if (MapUtils.isEmpty(bigDataWeaknessMap)) {
            return Collections.emptyMap();
        }
        //大数据的薄弱标签转化为平台的tagId
        List<AlbumAbilityTagConfig> tagConfigList = generateParentAlbumAbilityTagConfig(bigDataWeaknessMap);
        if (CollectionUtils.isEmpty(tagConfigList)) {
            return Collections.emptyMap();
        }
        Map<Integer, Set<Long>> subjectTags = new HashMap<>();
        Map<Integer, List<AlbumAbilityTagConfig>> subjectTagIds = tagConfigList.stream().collect(Collectors.groupingBy(AlbumAbilityTagConfig::getSubjectId));
        //这里之所以不能把大数据没返回的学科的兜底方案拿到这里来是因为：
        //没有学科的兜底方案是直接给的albumId。所以跟这里的逻辑不兼容了。只能在外层业务去处理了。
        for (Integer subjectId : subjectTagIds.keySet()) {
            if (CollectionUtils.isEmpty(subjectTagIds.get(subjectId))) {
                continue;
            }
            List<AlbumAbilityTagConfig> configList = subjectTagIds.get(subjectId);
            bigDataWeaknessMap.values()
                    .forEach(bigDataTag -> configList.stream().filter(p -> p.getDataTags().get(bigDataTag) != null).forEach(e -> {
                                Set<Long> tags;
                                if (subjectTags.containsKey(subjectId)) {
                                    tags = subjectTags.get(subjectId);
                                } else {
                                    tags = new HashSet<>();
                                    subjectTags.put(subjectId, tags);
                                }
                                tags.addAll(e.getDataTags().get(bigDataTag));
                            }
                    ));
        }
        return subjectTags;
    }

    //专辑标签体系兜底方案
    protected List<AlbumAbilityTagPlanBConfig> getFitJxtNewsAlbumWithConfig(Collection<Integer> clazzLevels) {
        List<AlbumAbilityTagPlanBConfig> abilityTagPlanBConfigs =
                pageBlockContentServiceClient.loadConfigList("parentAlbumAbilityTagPlanBConfig", "parentAlbumAbilityTagPlanB", AlbumAbilityTagPlanBConfig.class);
        if (CollectionUtils.isEmpty(abilityTagPlanBConfigs)) {
            return Collections.emptyList();
        }
        //不限制年级。全部返回
        if (CollectionUtils.isEmpty(clazzLevels)) {
            return abilityTagPlanBConfigs;
        }

        return abilityTagPlanBConfigs.stream()
                .filter(p -> CollectionUtils.isNotEmpty(p.getClazzLevels()))
                .filter(p -> clazzLevels.stream().anyMatch(e -> p.getClazzLevels().contains(e)))
                .collect(Collectors.toList());
    }


    //生成所有的能力标签，用于匹配专辑的能力标签展示
    protected List<JxtNewsTag> generateAllAbilityTags() {
        List<JxtNewsTag> subjectTag = jxtNewsLoaderClient.findTagByTagParentId(Collections.singleton(BASIC_ABILITY_TAG_ID));
        return jxtNewsLoaderClient.findTagByTagParentId(subjectTag.stream().map(JxtNewsTag::getId).collect(Collectors.toList()));
    }

    protected Map<SelfStudyType, ParentSelfStudyTypeH5Mapper> loadEntryMapper(Collection<SelfStudyType> selfStudyTypes, User parent, StudentDetail studentDetail, Boolean recIcon, String rel, Boolean forceEnterApp) {
        if (CollectionUtils.isEmpty(selfStudyTypes) || parent == null || studentDetail == null)
            return Collections.emptyMap();
        boolean inUserBlackList = userBlacklistServiceClient.isInUserBlackList(parent);
        if (inUserBlackList || studentDetail.getClazzLevel() == ClazzLevel.PRIMARY_GRADUATED) {
            return Collections.emptyMap();
        }
        Map<SelfStudyType, ParentSelfStudyTypeH5Mapper> map = new HashMap<>();
        List<FairylandProduct> fairylandProducts = businessVendorServiceClient.getParentAvailableFairylandProducts(parent, studentDetail, FairyLandPlatform.PARENT_APP, null);
        Map<String, FairylandProduct> fairylandProductMap = fairylandProducts.stream().collect(Collectors.toMap(FairylandProduct::getAppKey, Function.identity()));
        Set<String> appKeys = fairylandProductMap.keySet();
        List<SelfStudyType> ssts = selfStudyTypes.stream().filter(t -> appKeys.contains(t.getOrderProductServiceType())).collect(Collectors.toList());
        Mode current = RuntimeMode.current();
        Map<String, VendorApps> vendorAppsMap = vendorLoaderClient.loadVendorAppsIncludeDisabled()
                .values()
                .stream()
                .collect(Collectors.toMap(VendorApps::getAppKey, e -> e));
        String iconPrefix = getCdnBaseUrlAvatarWithSep() + "gridfs/";
        if (ssts.contains(SelfStudyType.PICLISTEN_ENGLISH)) {
            String picListenAppKey = SelfStudyType.PICLISTEN_ENGLISH.getOrderProductServiceType();
            FairylandProduct fairylandProduct = fairylandProductMap.get(picListenAppKey);
            if (fairylandProduct != null) {
                ParentSelfStudyTypeH5Mapper mapper = ParentSelfStudyTypeH5Mapper.enterApp(fairylandProduct, vendorAppsMap.get(picListenAppKey), current, recIcon);
                mapper.setIconUrl(iconPrefix + mapper.getIconUrl());
                map.put(SelfStudyType.PICLISTEN_ENGLISH, mapper);
            }
        }
        Map<String, AppPayMapper> userAppPaidStatus = userOrderLoaderClient.getUserAppPaidStatus(new ArrayList<>(appKeys), studentDetail.getId(), false);
        ssts.stream().filter(t -> OrderProductServiceType.safeParse(t.getOrderProductServiceType()) != OrderProductServiceType.PicListenBook).forEach(e -> {
            OrderProductServiceType orderProductServiceType = OrderProductServiceType.safeParse(e.getOrderProductServiceType());
            if (orderProductServiceType == null)
                return;
            String appKey = orderProductServiceType.name();
            AppPayMapper appPayMapper = userAppPaidStatus.get(appKey);
            if (appPayMapper == null)
                return;
            Integer appStatus = appPayMapper.getAppStatus();
            FairylandProduct fairylandProduct = fairylandProductMap.get(appKey);
            if (fairylandProduct == null)
                return;
            VendorApps vendorApps = vendorAppsMap.get(appKey);
            if (vendorApps == null)
                return;
            ParentSelfStudyTypeH5Mapper mapper = null;
            if (forceEnterApp){
                mapper = ParentSelfStudyTypeH5Mapper.enterApp(fairylandProduct, vendorApps, current, recIcon);
            }else {
                switch (appStatus) {
                    case 0:
                    case 1:
                        mapper = ParentSelfStudyTypeH5Mapper.enterPurchasePage(fetchMainsiteUrlByCurrentSchema(), fairylandProduct, studentDetail.getId(), rel, recIcon);
                        break;
                    case 2:
                        mapper = ParentSelfStudyTypeH5Mapper.enterApp(fairylandProduct, vendorApps, current, recIcon);
                        break;
                }
            }
            if (mapper != null) {
                mapper.setIconUrl(iconPrefix + mapper.getIconUrl());
                map.put(SelfStudyType.fromOrderType(OrderProductServiceType.safeParse(appKey)), mapper);
            }
        });

        return map;

    }

}
