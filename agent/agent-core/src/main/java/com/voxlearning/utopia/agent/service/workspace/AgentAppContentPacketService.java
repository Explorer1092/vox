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

package com.voxlearning.utopia.agent.service.workspace;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.bean.CityInfo;
import com.voxlearning.utopia.agent.constants.*;
import com.voxlearning.utopia.agent.dao.mongo.AgentAppContentPacketDao;
import com.voxlearning.utopia.agent.persist.entity.AgentAppContentPacket;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseGroupService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 市场app内容管理
 * Created by yaguang.wang on 2016/8/2.
 */
@Named
public class AgentAppContentPacketService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AgentAppContentPacketDao agentAppContentPacketDao;
    @Inject private BaseGroupService baseGroupService;
    @Inject private BaseOrgService baseOrgService;

    public boolean insertAppContentPacket(AgentAppContentPacket content) {
        if (content == null) {
            return false;
        }
        agentAppContentPacketDao.insert(content);
        return true;
    }

    public boolean removeAppContentPacket(String id) {
        return !StringUtils.isBlank(id) && agentAppContentPacketDao.deleteAgentAppContentPacket(id);
    }

    public boolean updateAppContentPacket(AgentAppContentPacket content) {
        content = agentAppContentPacketDao.replace(content);
        return content != null;
    }

    public AgentAppContentPacket loadById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return agentAppContentPacketDao.load(id);
    }

    //按照内容的类型来查询
    public List<AgentAppContentPacket> loadByContentType(AgentAppContentType contentType) {
        List<AgentAppContentPacket> result = loadByContentTypeIncludeDisabled(contentType);
        result = result.stream().filter(p -> !SafeConverter.toBoolean(p.getDisabled())).collect(Collectors.toList());
        return result;
    }

    //按照内容的类型来查询包含被禁用的
    public List<AgentAppContentPacket> loadByContentTypeIncludeDisabled(AgentAppContentType contentType) {
        if (contentType == null) {
            return Collections.emptyList();
        }
        return agentAppContentPacketDao.findByContentType(contentType);
    }

    //按照资料包类型查询
    public List<AgentAppContentPacket> loadByDatumType(AgentDataPacketType datumType) {
        List<AgentAppContentPacket> result = loadByDatumTypeIncludeDisabled(datumType);
        result = result.stream().filter(p -> !SafeConverter.toBoolean(p.getDisabled())).collect(Collectors.toList());
        return result;
    }

    //按照资料包类型查询包含被禁用的
    public List<AgentAppContentPacket> loadByDatumTypeIncludeDisabled(AgentDataPacketType datumType) {
        if (datumType == null) {
            return Collections.emptyList();
        }
        List<AgentAppContentPacket> result = agentAppContentPacketDao.findByDatumType(datumType);
        return result;
    }


    // ----------------------------------------业务相关----------------------------------------------

    //添加市场活动
    public MapMessage addMarketingActivity(String activityName, Date startDate, Date endDate, List<AgentUsedProductType> activityEntrances,
                                           List<SchoolLevel> scope, List<CityInfo> citys, String content) {
        MapMessage msg = checkMarketingActivity(activityName, startDate, endDate, activityEntrances,
                scope, content);
        if (!msg.isSuccess()) {
            return msg;
        }
        AgentAppContentPacket marketingActivity = new AgentAppContentPacket();
        marketingActivity.setContentType(AgentAppContentType.MARKETING_ACTIVITY);
        marketingActivity.setActivityName(activityName);
        marketingActivity.setActivityScope(scope);
        marketingActivity.setActivityCity(citys);
        marketingActivity.setActivityEntrance(activityEntrances);
        marketingActivity.setActivityStartDate(startDate);
        marketingActivity.setActivityEndDate(endDate);
        marketingActivity.setContent(content);
        marketingActivity.setDisabled(false);
        marketingActivity.setState(AppContentStateType.TEMPORARY_STORAGE);
        insertAppContentPacket(marketingActivity);
        return MapMessage.successMessage();
    }

    //更新活动
    public MapMessage editMarketingActivity(String id, String activityName, Date startDate, Date endDate, List<AgentUsedProductType> activityEntrances,
                                            List<SchoolLevel> scope, List<CityInfo> citys, String content) {
        AgentAppContentPacket marketingActivity = loadById(id);
        if (marketingActivity == null) {
            return MapMessage.errorMessage("所要更新的活动不存在");
        }
        if (marketingActivity.getDisabled()) {
            return MapMessage.errorMessage("已删除的活动无法编辑");
        }
        MapMessage msg = checkMarketingActivity(activityName, startDate, endDate, activityEntrances,
                scope, content);
        if (!msg.isSuccess()) {
            return msg;
        }
        marketingActivity.setContentType(AgentAppContentType.MARKETING_ACTIVITY);
        marketingActivity.setActivityName(activityName);
        marketingActivity.setActivityScope(scope);
        marketingActivity.setActivityCity(citys);
        marketingActivity.setActivityEntrance(activityEntrances);
        marketingActivity.setActivityStartDate(startDate);
        marketingActivity.setActivityEndDate(endDate);
        marketingActivity.setContent(content);
        marketingActivity.setState(AppContentStateType.TEMPORARY_STORAGE);
        marketingActivity.setDisabled(false);
        boolean success = updateAppContentPacket(marketingActivity);
        if (success) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("更新活动失败");
        }
    }

    public MapMessage checkMarketingActivity(String activityName, Date startDate, Date endDate, List<AgentUsedProductType> activityEntrances,
                                             List<SchoolLevel> scope, String content) {
        if (StringUtils.isBlank(activityName)) {
            return MapMessage.errorMessage("活动名称不能为空");
        }
        if (activityName.length() > 20) {
            return MapMessage.errorMessage("活动名称不能超过20个字符");
        }
        if (startDate == null || endDate == null) {
            return MapMessage.errorMessage("活动的开始和结束时间不能为空");
        }
        if (endDate.before(startDate)) {
            return MapMessage.errorMessage("活动的结束时间不能早于开始时间");
        }
        if (CollectionUtils.isEmpty(scope)) {
            return MapMessage.errorMessage("请选择活动范围");
        }
        if (CollectionUtils.isEmpty(activityEntrances)) {
            return MapMessage.errorMessage("请选择活动入口");
        }
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("活动的内容不能为空");
        }
        return MapMessage.successMessage();
    }

    public List<AgentAppContentPacket> loadUserActivity(Long userId) {
        List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(userId);
        List<AgentGroupRegion> manageRegion = baseOrgService.getGroupRegionsByGroupSet(groupIdList);
        Set<Integer> regionCode = manageRegion.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toSet());
        Map<Integer, ExRegion> regionMap = raikouSystem.getRegionBuffer().loadRegions(regionCode);
        List<ExRegion> resultRegion = new ArrayList<>(regionMap.values());
        Set<Integer> resultRegionCode = resultRegion.stream().map(ExRegion::getCityCode).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(resultRegionCode)) {
            return Collections.emptyList();
        }
        List<AgentAppContentPacket> activity = agentAppContentPacketDao.findByContentType(AgentAppContentType.MARKETING_ACTIVITY);
        return activity.stream().filter(p -> {
            List<CityInfo> cityInfos = p.getActivityCity();
            if (CollectionUtils.isEmpty(cityInfos)) {
                return true;
            }
            for (CityInfo cityInfo : cityInfos) {
                if (resultRegionCode.contains(cityInfo.getCityCode())) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }


    //添加资料包
    public MapMessage addDataPacket(String id, AgentDataPacketType type, String contentTitle, String content, Set<AgentDataPacketRole> applyRoleType) {

        if (type == null) {
            return MapMessage.errorMessage("请选择资料包类型");
        }
        if (CollectionUtils.isEmpty(applyRoleType)) {
            return MapMessage.errorMessage("请选择适用角色");
        }
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("请填写资料包详情");
        }
        if (StringUtils.isBlank(contentTitle)) {
            return MapMessage.errorMessage("请填写资料包的标题");
        }

        AgentAppContentPacket datePacket = new AgentAppContentPacket();
        if (StringUtils.isNotBlank(id)) {
            datePacket = loadById(id);
            if (SafeConverter.toBoolean(datePacket.getDisabled())) {
                return MapMessage.errorMessage("该资料包已删除不能更新");
            }
        }
        datePacket.setContentType(AgentAppContentType.DATA_PACKET);
        datePacket.setContent(content);
        datePacket.setContentTitle(contentTitle);
        datePacket.setState(AppContentStateType.TEMPORARY_STORAGE);
        datePacket.setDatumType(type);
        datePacket.setApplyRole(applyRoleType);
        datePacket.setDisabled(false);
        if (StringUtils.isNotBlank(id)) {
            updateAppContentPacket(datePacket);
        } else {
            insertAppContentPacket(datePacket);
        }
        return MapMessage.successMessage();
    }

    //平台更新日志
    public MapMessage addUpdateLog(String id, List<AgentUsedProductType> referProduct, String content, String contentTitle) {
        if (CollectionUtils.isEmpty(referProduct)) {
            return MapMessage.errorMessage("请选择涉及产品");
        }
        if (StringUtils.isBlank(contentTitle)) {
            return MapMessage.errorMessage("日志标题不能为空");
        }
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("日志内容不能为空");
        }
        AgentAppContentPacket updateLog = new AgentAppContentPacket();
        if (StringUtils.isNotBlank(id)) {
            updateLog = loadById(id);
        }
        updateLog.setContentType(AgentAppContentType.UPDATE_LOG);
        updateLog.setReferProduct(referProduct);
        updateLog.setContent(content);
        updateLog.setContentTitle(contentTitle);
        updateLog.setState(AppContentStateType.TEMPORARY_STORAGE);
        updateLog.setDisabled(false);
        if (StringUtils.isNotBlank(id)) {
            updateAppContentPacket(updateLog);
        } else {
            insertAppContentPacket(updateLog);
        }
        return MapMessage.successMessage();
    }

    //添加推荐书籍
    public MapMessage addRecommendBook(AgentRecommendBookRoleType role, String bookName, String bookCoverUrl) {
        if (role == null) {
            return MapMessage.errorMessage("请选择推荐对象角色");
        }
        if (StringUtils.isBlank(bookName)) {
            return MapMessage.errorMessage("请填写书籍名称");
        }
        if (StringUtils.isBlank(bookCoverUrl)) {
            return MapMessage.errorMessage("请上传缩略图");
        }
        List<AgentAppContentPacket> oldContent = loadByContentType(AgentAppContentType.RECOMMEND_BOOK);
        oldContent = oldContent.stream().filter(p -> p.getRole() == role).collect(Collectors.toList());
        AgentAppContentPacket recommendBook = new AgentAppContentPacket();
        recommendBook.setContentType(AgentAppContentType.RECOMMEND_BOOK);
        recommendBook.setRole(role);
        recommendBook.setBookName(bookName);
        recommendBook.setBookCoverUrl(bookCoverUrl);
        recommendBook.setDisabled(false);
        if (CollectionUtils.isNotEmpty(oldContent)) {
            //正常来讲只会有一个被更新掉
            oldContent.forEach(p -> removeAppContentPacket(p.getId()));
        }
        insertAppContentPacket(recommendBook);
        return MapMessage.successMessage();
    }

    public List<Map<String, Object>> loadSelectCity(Set<Integer> cityCodeIds) {
        List<AgentGroup> marketingGroup = baseGroupService.getAgentGroupByRoleId(AgentGroupRoleType.Region.getId());
        Set<Long> groupIds = marketingGroup.stream().map(AgentGroup::getId).collect(Collectors.toSet());
        List<AgentGroupRegion> groupRegions = baseOrgService.getGroupRegionsByGroupSet(groupIds);
        List<Map<String, Object>> result = new ArrayList<>();
        groupRegions.forEach(p -> {
            Map<String, Object> regionItemMap = new HashMap<>();
            regionItemMap.put("title", p.getRegionName());
            regionItemMap.put("key", String.valueOf(p.getRegionCode()));
            if (cityCodeIds.contains(p.getRegionCode())) {
                regionItemMap.put("selected", true);
            }
            result.add(regionItemMap);
        });
        return result;
    }

    //传要取书籍的月份中的开始时间和结束时间
    public AgentAppContentPacket loadRecommendBookByTime(AgentRecommendBookRoleType role, Date startDate, Date endDate) {
        if (startDate == null || endDate == null || startDate.after(endDate)) {
            return null;
        }
        List<AgentAppContentPacket> recommendBooks = loadByContentTypeIncludeDisabled(AgentAppContentType.RECOMMEND_BOOK);
        recommendBooks = recommendBooks.stream().filter(p -> role == p.getRole() && p.getCreateTime().after(startDate) && p.getCreateTime().before(endDate)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(recommendBooks)) {
            return null;
        }
        AgentAppContentPacket recommendBook = recommendBooks.get(0);
        recommendBook.setDisabled(true);
        return recommendBook;
    }

    /**
     * 2018年6月7日21:02:36  添加 在自助查询设置资料包时用到
     *
     * @return
     */
    public List<AgentAppContentPacket> findAllPackets() {
        return agentAppContentPacketDao.query();
    }

    public Map<String, AgentAppContentPacket> findPacketsByIds(List<String> ids) {
        return agentAppContentPacketDao.loads(ids);
    }

}
