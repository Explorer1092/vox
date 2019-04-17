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

package com.voxlearning.utopia.admin.controller;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseBlackWidowServiceClient;
import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.cache.AdminCacheSystem;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import com.voxlearning.utopia.admin.service.crm.CrmRegionService;
import com.voxlearning.utopia.admin.service.crm.CrmTeacherSystemClazzService;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.consumer.BusinessTeacherServiceClient;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.config.client.AdvertisementServiceClient;
import com.voxlearning.utopia.service.config.consumer.AdvertisementLoaderClient;
import com.voxlearning.utopia.service.content.consumer.*;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.integral.client.IntegralServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.*;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.RewardManagementClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.sms.consumer.SmsLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.service.integral.ClazzIntegralService;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsTag;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Longlong Yu
 * @since 下午6:03,13-11-22.
 */
@AdminAcceptRoles(getRoles = {AdminPageRole.GET_ACCESSOR}, postRoles = {AdminPageRole.POST_ACCESSOR})
public abstract class AbstractAdminSystemController extends AbstractAdminController {
    @StorageClientLocation(storage = "homework")
    protected StorageClient storageClient;

    @Deprecated
    @Inject protected AdminCacheSystem adminCacheSystem;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

    protected UtopiaSql utopiaSql;
    protected UtopiaSql utopiaSqlOrder;
    protected UtopiaSql utopiaSqlAdmin;
    protected UtopiaSql utopiaSqlReward;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("order");
        utopiaSqlAdmin = utopiaSqlFactory.getUtopiaSql("admin");
        utopiaSqlReward = utopiaSqlFactory.getUtopiaSql("hs_reward");
    }

    /* ImportServices */
    @ImportService(interfaceClass = ClazzIntegralService.class) protected ClazzIntegralService clazzIntegralService;
    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;
    @Inject protected IntegralServiceClient integralServiceClient;

    /* PLEASE Inject In Alphabetical Order */
    @Inject protected AdvertisementLoaderClient advertisementLoaderClient;
    @Inject protected AdvertisementServiceClient advertisementServiceClient;
    @Inject protected AppMessageServiceClient appMessageServiceClient;

    @Inject protected BusinessTeacherServiceClient businessTeacherServiceClient;

    @Inject protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject protected ClazzServiceClient clazzServiceClient;
    @Inject protected CrmSummaryServiceClient crmSummaryServiceClient;
    @Inject protected CrmRegionService crmRegionService;
    @Inject protected CrmTeacherSystemClazzService crmTeacherSystemClazzService;

    @Inject protected EnglishContentLoaderClient englishContentLoaderClient;

    @Inject protected DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject protected GroupServiceClient groupServiceClient;

    @Inject protected MathContentLoaderClient mathContentLoaderClient;

    @Inject protected NewAccomplishmentLoaderClient newAccomplishmentLoaderClient;
    @Inject protected NewContentLoaderClient newContentLoaderClient;
    @Inject protected NewHomeworkCrmLoaderClient newHomeworkCrmLoaderClient;
    @Inject protected NewHomeworkCrmServiceClient newHomeworkCrmServiceClient;
    @Inject protected NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject protected NewHomeworkPartLoaderClient newHomeworkPartLoaderClient;
    @Inject protected NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject protected NewHomeworkServiceClient newHomeworkServiceClient;

    @Inject protected ParentLoaderClient parentLoaderClient;
    @Inject protected ParentServiceClient parentServiceClient;
    @Inject protected PracticeLoaderClient practiceLoaderClient;

    @Inject protected QuestionLoaderClient questionLoaderClient;

    @Inject protected RegionBookLoaderClient regionContentLoaderClient;
    @Inject protected ResearchStaffLoaderClient researchStaffLoaderClient;
    @Inject protected RewardLoaderClient rewardLoaderClient;
    @Inject protected RewardManagementClient rewardManagementClient;
    @Inject protected RewardServiceClient rewardServiceClient;

    @Inject protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Deprecated
    @Inject protected DeprecatedSchoolServiceClient deprecatedSchoolServiceClient;
    @Inject protected SmsLoaderClient smsLoaderClient;
    @Inject protected StudentLoaderClient studentLoaderClient;
    @Inject protected StudentServiceClient studentServiceClient;
    @Inject protected AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject protected TeacherAlterationServiceClient teacherAlterationServiceClient;
    @Inject protected TeacherLoaderClient teacherLoaderClient;
    @Inject protected TeacherServiceClient teacherServiceClient;
    @Inject protected TeacherSystemClazzServiceClient teacherSystemClazzServiceClient;
    @Inject protected SpecialTeacherLoaderClient specialTeacherLoaderClient;
    @Inject protected UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected UserLoginServiceClient userLoginServiceClient;
    @Inject protected UserOrderLoaderClient userOrderLoaderClient;
    @Inject protected UserServiceClient userServiceClient;

    @Inject protected VacationHomeworkServiceClient vacationHomeworkServiceClient;
    @Inject protected VendorLoaderClient vendorLoaderClient;
    @Inject protected VendorServiceClient vendorServiceClient;

    @Inject protected WechatLoaderClient wechatLoaderClient;
    @Inject protected WechatServiceClient wechatServiceClient;
    @Inject protected AsyncFootprintServiceClient asyncFootprintServiceClient;

    @Inject protected OutsideReadingLoaderClient outsideReadingLoaderClient;
    @Inject protected OutsideReadingServiceClient outsideReadingServiceClient;

    @Inject protected StudyCourseBlackWidowServiceClient studyCourseBlackWidowServiceClient;

    protected Map<String, Object> generateTagTree(long currentTagId, String currentTagName, List<JxtNewsTag> jxtNewsTags, boolean expanded) {
        Map<String, Object> tree = new HashMap<>();
        List<JxtNewsTag> childTags = jxtNewsTags.stream().filter(p -> p.getParentId() == currentTagId).collect(Collectors.toList());
        if (childTags.size() > 0) {
            // 有孩子
            Collection<Map<String, Object>> children = childTags.stream().map(t -> MiscUtils.m("name", t.getTagName(), "expanded", expanded, "id", t.getId(), "title", t.getTagName(), "key", t.getId(), "children", generateTagTree(t.getId(), t.getTagName(), jxtNewsTags, expanded).get("children"))).collect(Collectors.toCollection(ArrayList::new));
            tree.put("id", currentTagId);
            tree.put("key", currentTagId);
            tree.put("name", currentTagName);
            tree.put("title", currentTagName);
            tree.put("children", children);
            tree.put("expanded", expanded);
        } else {
            // 无孩子
            tree.put("id", currentTagId);
            tree.put("key", currentTagId);
            tree.put("name", currentTagName);
            tree.put("title", currentTagName);
            tree.put("children", CollectionUtils.emptyCollection());
            tree.put("expanded", expanded);
        }
        return tree;
    }

    protected String getMainHostBaseUrl() {
        return ProductConfig.getMainSiteBaseUrl();
    }

}
