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

package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.bean.CityInfo;
import com.voxlearning.utopia.agent.constants.*;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentAppContentPacket;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.workspace.AgentAppContentPacketService;
import com.voxlearning.utopia.agent.utils.AgentOssManageUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupRegion;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理和更新天玑榜的内容
 * Created by yaguang.wang on 2016/8/2.
 */

@Controller
@RequestMapping("/workspace/appupdate")
@Slf4j
public class AppContentManageController extends AbstractAgentController {

    @Inject private RaikouSystem raikouSystem;
    @Inject AgentAppContentPacketService agentAppContentPacketService;
    @Inject AgentNotifyService agentNotifyService;
    @Inject BaseOrgService baseOrgService;
    private final AtomicLockManager atomicLockManager = AtomicLockManager.instance();

    //------------------------------------资料包----------------------------------------------------------------------
    //资料包页
    @RequestMapping(value = "data_packet_manage.vpage", method = RequestMethod.GET)
    public String dataPacketManage(Model model) {
        Integer typeId = getRequestInt("typeId");
        String errorMessage = getRequestString("errorMessage");
        Integer showDelete = getRequestInt("showDelete");
        AgentDataPacketType type = AgentDataPacketType.typeOf(typeId);
        if (type == null) {
            type = AgentDataPacketType.POLICY_PAPER;
            typeId = type.getId();
        }
        List<AgentAppContentPacket> data_packets;
        if (showDelete == 1) {
            data_packets = agentAppContentPacketService.loadByDatumTypeIncludeDisabled(type);
        } else {
            data_packets = agentAppContentPacketService.loadByDatumType(type);
        }
        model.addAttribute("dataPacket", createDataPacketList(data_packets));                 // 所选资料包的内容
        model.addAttribute("dataPacketType", AgentDataPacketType.values());             // 资料包的类型
        model.addAttribute("typeId", typeId);// 当前选择的资料包类型
        if (StringUtils.isNotBlank(errorMessage)) {
            model.addAttribute("errorMessage", errorMessage);
        }
        model.addAttribute("showDelete", showDelete);
        return "workspace/appContentManage/data_packet_manage";
    }

    List<Map<String, Object>> createDataPacketList(List<AgentAppContentPacket> data_packets) {
        List<Map<String, Object>> result = new ArrayList<>();
        Integer roleLength = AgentDataPacketRole.values().length;
        data_packets.forEach(p -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", p.getId());                                                        // ID
            info.put("disabled", SafeConverter.toBoolean(p.getDisabled()));                   // 已被删除
            info.put("createDate", p.getCreateTime());                                        // 创建时间
            info.put("datumType", p.getDatumType() != null ? p.getDatumType().getDesc() : "");// 类型
            info.put("contentTitle", p.getContentTitle());
            info.put("state", p.getState());
            info.put("fileUrl", p.getFileUrl());                                              // 文件下载路劲
            Set<AgentDataPacketRole> applyRole = p.getApplyRole();
            info.put("applyRole", CollectionUtils.isEmpty(applyRole) ? "全部角色" :
                    Objects.equals(applyRole.size(), roleLength) ? "全部角色" : StringUtils.join(applyRole.stream().map(AgentDataPacketRole::getRoleName).collect(Collectors.toSet()), ","));                                                         // 使用角色
            result.add(info);
        });
        return result;
    }

    //跳转到添加资料包页
    @RequestMapping(value = "data_packet_detail.vpage", method = RequestMethod.GET)
    public String dataPacketInstall(Model model) {
        String id = getRequestString("id");
        model.addAttribute("dataPacketType", AgentDataPacketType.values());
        model.addAttribute("id", id);
        model.addAttribute("applyRole", AgentDataPacketRole.values());
        return "workspace/appContentManage/data_packet_detail";
    }

    @RequestMapping(value = "save_data_packet.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveDataPacket() {
        String id = getRequestString("id");
        String contentTitle = getRequestString("contentTitle");
        String content = getRequestString("content");
        Integer datumTypeId = getRequestInt("datumType");
        String applyRole = getRequestString("applyRole");
        Set<AgentDataPacketRole> applyRoleType = strToApplyRole(applyRole);
        AgentDataPacketType datumType = AgentDataPacketType.typeOf(datumTypeId);
        return agentAppContentPacketService.addDataPacket(id, datumType, contentTitle, content, applyRoleType);
    }

    private Set<AgentDataPacketRole> strToApplyRole(String applyRole) {
        if (StringUtils.isBlank(applyRole)) {
            return Collections.emptySet();
        }
        Set<AgentDataPacketRole> result = new HashSet<>();
        String[] roleTypes = applyRole.split(",");
        Set<String> roleIds = new HashSet<>(Arrays.asList(roleTypes));
        roleIds.forEach(p -> {
            Integer typeId = SafeConverter.toInt(p);
            AgentDataPacketRole type = AgentDataPacketRole.typeOf(typeId);
            if (type != null) {
                result.add(type);
            }
        });
        return result;
    }

    @RequestMapping(value = "edit_data_packet.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editDataPacket() {
        MapMessage msg = MapMessage.successMessage();
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.successMessage();
        }
        AgentAppContentPacket content = agentAppContentPacketService.loadById(id);
        msg.add("contentTitle", content.getContentTitle());
        msg.add("content", content.getContent());
        msg.add("datumType", content.getDatumType().getId());
        msg.add("state", content.getState().getStateCode());
        msg.add("disabled", content.getDisabled());
        Set<AgentDataPacketRole> applyRole = content.getApplyRole();
        msg.add("applyRoles", CollectionUtils.isEmpty(applyRole) ? AgentDataPacketRole.roleTypeMap.keySet() : applyRole.stream().map(AgentDataPacketRole::getId).collect(Collectors.toSet()));
        return msg;
    }


    //------------------------------------资料包END--------------------------------------------------------------------

    //------------------------------------最新活动--------------------------------------------------------------------

    //活动页面
    @RequestMapping(value = "marketing_activity_manage.vpage", method = RequestMethod.GET)
    public String marketingActivityManage(Model model) {
        Integer showDelete = getRequestInt("showDelete");
        String message = getRequestString("errorMessage");
        List<AgentAppContentPacket> activity;
        if (showDelete == 1) {
            activity = agentAppContentPacketService.loadByContentTypeIncludeDisabled(AgentAppContentType.MARKETING_ACTIVITY);
        } else {
            activity = agentAppContentPacketService.loadByContentType(AgentAppContentType.MARKETING_ACTIVITY);
            activity = activity.stream().filter(p -> p.getActivityEndDate().after(DateUtils.getTodayStart()) || Objects.equals(p.getActivityEndDate().getTime(), DateUtils.getTodayStart().getTime())).collect(Collectors.toList());
        }
        model.addAttribute("activity", createActivityList(activity));
        if (StringUtils.isNotBlank(message)) {
            model.addAttribute("errorMessage", message);
        }
        model.addAttribute("showDelete", showDelete);
        return "workspace/appContentManage/activity_manage";
    }

    private List<Map<String, Object>> createActivityList(List<AgentAppContentPacket> activity) {
        List<Map<String, Object>> result = new ArrayList<>();
        activity.forEach(p -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", p.getId());
            info.put("activityName", p.getActivityName());
            info.put("startDate", p.getActivityStartDate());
            info.put("endDate", p.getActivityEndDate());
            List<SchoolLevel> activityScope = p.getActivityScope();
            info.put("activityScope", activityScope == null ? "" : StringUtils.join(activityScope.stream().map(SchoolLevel::getDescription).collect(Collectors.toList()), ","));
            List<AgentUsedProductType> agentUsedEntrance = p.getActivityEntrance();
            info.put("activityEntrance", agentUsedEntrance == null ? "" : StringUtils.join(agentUsedEntrance.stream().map(AgentUsedProductType::getEntranceName).collect(Collectors.toList()), ","));
            List<CityInfo> activityCity = p.getActivityCity();
            info.put("activityCity", activityCity == null ? "全国" : CollectionUtils.isEmpty(activityCity) ? "全国" : StringUtils.join(activityCity.stream().map(CityInfo::getCityName).collect(Collectors.toList()), ","));
            info.put("disabled", p.getDisabled());
            info.put("content", p.getContent());
            info.put("state", p.getState());
            result.add(info);
        });
        return result;
    }


    //保存活动页 修改活动页
    @RequestMapping(value = "activity_presentation_detail.vpage", method = RequestMethod.GET)
    public String activityPresentationInstall(Model model) {
        String errorMessage = getRequestString("errorMessage");
        String id = getRequestString("id");
        model.addAttribute("activityScope", SchoolLevel.values());
        model.addAttribute("activityEntrance", AgentUsedProductType.values());
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("id", id);
        return "workspace/appContentManage/activity_detail";
    }

    @RequestMapping(value = "edit_activity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editActivity() {
        MapMessage msg = MapMessage.successMessage();
        String id = getRequestString("id");
        AgentAppContentPacket activity = agentAppContentPacketService.loadById(id);
        if (activity == null) {
            return MapMessage.successMessage();
        }
        List<CityInfo> cityInfos = activity.getActivityCity();
        List<SchoolLevel> activityScope = activity.getActivityScope();
        List<AgentUsedProductType> activityEntrance = activity.getActivityEntrance();
        msg.add("selectCityName", StringUtils.join(cityInfos.stream().map(CityInfo::getCityName).collect(Collectors.toList()), ","));
        msg.add("selectCityCode", StringUtils.join(cityInfos.stream().map(CityInfo::getCityCode).collect(Collectors.toList()), ","));
        msg.add("id", id);
        msg.add("activityName", activity.getActivityName());
        msg.add("startDate", DateUtils.dateToString(activity.getActivityStartDate(), DateUtils.FORMAT_SQL_DATE));
        msg.add("endDate", DateUtils.dateToString(activity.getActivityEndDate(), DateUtils.FORMAT_SQL_DATE));
        msg.add("content", activity.getContent());
        if (CollectionUtils.isNotEmpty(activityScope)) {     //正常来讲不会为空
            msg.add("scopeIds", StringUtils.join(activityScope.stream().map(SchoolLevel::getLevel).collect(Collectors.toSet()), ","));
        }
        if (CollectionUtils.isNotEmpty(activityEntrance)) {  //正常来讲不会为空
            msg.add("entranceIds", StringUtils.join(activityEntrance.stream().map(AgentUsedProductType::getId).collect(Collectors.toSet()), ","));
        }
        msg.add("disabled", activity.getDisabled());
        msg.add("state", activity.getState());
        return msg;
    }

    //新增 and 编辑
    @RequestMapping(value = "save_activity_presentation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveActivityPresentation() {
        String id = getRequestString("id");                                             // 保存的时候可以为空
        String activityName = getRequestString("activityName");
        Date startDate = getRequestDate("startDate");
        Date endDate = getRequestDate("endDate");
        String scopeIds = getRequestString("scopeIds");
        List<SchoolLevel> scope = strToActivityScope(scopeIds);
        String entranceIds = getRequestString("entranceIds");
        List<AgentUsedProductType> entrance = strToProductType(entranceIds);
        Set<Integer> cityCodes = requestIntegerSet("cityCodes");
        List<CityInfo> citys = createCityInfo(cityCodes);
        String content = getRequestString("content");
        if (StringUtils.isBlank(id)) {
            return agentAppContentPacketService.addMarketingActivity(activityName, startDate, endDate, entrance, scope, citys, content);
        } else {
            return agentAppContentPacketService.editMarketingActivity(id, activityName, startDate, endDate, entrance, scope, citys, content);
        }
    }

    private List<CityInfo> createCityInfo(Set<Integer> cityCodes) {
        if (CollectionUtils.isEmpty(cityCodes)) {
            return Collections.emptyList();
        }
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(cityCodes);
        List<CityInfo> result = new ArrayList<>();
        for (ExRegion region : exRegionMap.values()) {
            CityInfo info = new CityInfo();
            info.setCityCode(region.getCityCode());
            info.setCityName(region.getCityName());
            result.add(info);
        }
        return result;
    }

    //选择城市
    @RequestMapping(value = "select_city.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String selectCity() {
        String id = getRequestString("id");
        Set<Integer> cityCodeIds = new HashSet<>();
        if (StringUtils.isNotBlank(id)) {
            AgentAppContentPacket activity = agentAppContentPacketService.loadById(id);
            List<CityInfo> cityInfos = activity.getActivityCity();
            cityCodeIds = cityInfos.stream().map(CityInfo::getCityCode).collect(Collectors.toSet());
        }
        try {
            return JsonUtils.toJson(agentAppContentPacketService.loadSelectCity(cityCodeIds));
        } catch (Exception ex) {
            log.error("加载区域失败", ex);
            return "加载区域失败" + ex.getMessage();
        }
    }
    //------------------------------------最新活动 END----------------------------------------------------------------

    //------------------------------------推荐书籍 -------------------------------------------------------------------
    @RequestMapping(value = "recommend_book_manage.vpage", method = RequestMethod.GET)
    public String recommendBookManage(Model model) {
        Integer showDelete = getRequestInt("showDelete");
        List<AgentAppContentPacket> recommendBooks;
        if (showDelete == 1) {
            recommendBooks = agentAppContentPacketService.loadByContentTypeIncludeDisabled(AgentAppContentType.RECOMMEND_BOOK);
        } else {
            recommendBooks = agentAppContentPacketService.loadByContentType(AgentAppContentType.RECOMMEND_BOOK);
        }
        model.addAttribute("recommendBooks", createRecommendBooksList(recommendBooks));
        model.addAttribute("showDelete", showDelete);
        return "workspace/appContentManage/recommend_book_manage";
    }

    List<Map<String, Object>> createRecommendBooksList(List<AgentAppContentPacket> recommendBooks) {
        List<Map<String, Object>> result = new ArrayList<>();
        recommendBooks.forEach(p -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", p.getId());                                                                  // 条目的ID
            info.put("disabled", SafeConverter.toBoolean(p.getDisabled()));                             // 是历史记录
            info.put("role", p.getRole() != null ? p.getRole().getRoleName() : "");                     // 角色
            info.put("bookName", p.getBookName());                                                      // 书籍名称
            info.put("coverUrl", p.getBookCoverUrl());                                                  // 封面地址
            info.put("createTime", p.getCreateTime());                                                  // 创建时间
            result.add(info);
        });
        return result;
    }

    @RequestMapping(value = "recommend_book_detail.vpage", method = RequestMethod.GET)
    public String recommendBookDetail(Model model) {
        model.addAttribute("roleList", AgentRecommendBookRoleType.values());
        return "workspace/appContentManage/recommend_book_detail";
    }

    @RequestMapping(value = "save_recommend_book.vpage", method = RequestMethod.POST)
    public String saveRecommendBook(Model model, MultipartFile bookCover) {
        Long userId = getCurrentUserId();
        String bookCoverUrl = atomicLockManager.wrapAtomic(this).keyPrefix("agent_user").keys(userId).proxy().uploadFile(bookCover);
        String bookName = getRequestString("bookName");
        Integer roleId = requestInteger("role");
        AgentRecommendBookRoleType role = AgentRecommendBookRoleType.typeOf(roleId);
        MapMessage msg = agentAppContentPacketService.addRecommendBook(role, bookName, bookCoverUrl);
        if (!msg.isSuccess()) {
            model.addAttribute("errorMessage", msg.getInfo());
        }
        return redirect("recommend_book_manage.vpage");
    }

    //------------------------------------推荐书籍 END----------------------------------------------------------------

    //------------------------------------平台更新日志----------------------------------------------------------------
    @RequestMapping(value = "update_log_manage.vpage", method = RequestMethod.GET)
    public String updateLogManage(Model model) {
        Integer showDelete = getRequestInt("showDelete");
        List<AgentAppContentPacket> updateLog;
        if (showDelete == 1) {
            updateLog = agentAppContentPacketService.loadByContentTypeIncludeDisabled(AgentAppContentType.UPDATE_LOG);
        } else {
            updateLog = agentAppContentPacketService.loadByContentType(AgentAppContentType.UPDATE_LOG);
        }
        model.addAttribute("updateLog", createUpdateLogList(updateLog));
        model.addAttribute("showDelete", showDelete);
        return "workspace/appContentManage/update_log_manage";
    }

    private List<Map<String, Object>> createUpdateLogList(List<AgentAppContentPacket> updateLog) {
        List<Map<String, Object>> result = new ArrayList<>();
        updateLog.forEach(p -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", p.getId());                                                       // ID
            info.put("createDate", p.getCreateTime());                                       // 条目创建时间
            info.put("updateContent", p.getContentTitle());                                  // 更新内容
            info.put("fileUrl", p.getFileUrl());                                             // 文件下载路劲
            List<AgentUsedProductType> productType = p.getReferProduct();
            List<String> productName = productType.stream().map(AgentUsedProductType::getEntranceName).collect(Collectors.toList());
            info.put("productName", StringUtils.join(productName, ","));                                            // 涉及产品名称
            info.put("disabled", p.getDisabled());
            info.put("state", p.getState());
            result.add(info);
        });
        return result;
    }

    @RequestMapping(value = "update_log_detail.vpage", method = RequestMethod.GET)
    public String updateLogDetail(Model model) {
        String id = getRequestString("id");
        model.addAttribute("referProduct", AgentUsedProductType.values());
        model.addAttribute("id", id);
        return "workspace/appContentManage/update_log_detail";
    }

    @RequestMapping(value = "save_update_log.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveUpdateLog() {
        String id = getRequestString("id");
        String content = getRequestString("content");
        String contentTitle = getRequestString("contentTitle");
        String referProductId = getRequestString("referProduct");                               // 已逗号分割的选项
        List<AgentUsedProductType> referProduct = strToProductType(referProductId);
        return agentAppContentPacketService.addUpdateLog(id, referProduct, content, contentTitle);
    }

    @RequestMapping(value = "edit_update_log.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editUpdateLog() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.successMessage();
        }
        AgentAppContentPacket updateLog = agentAppContentPacketService.loadById(id);
        MapMessage msg = MapMessage.successMessage();
        msg.add("referProduct", updateLog.getReferProduct().stream().map(AgentUsedProductType::getId).collect(Collectors.toSet()));
        msg.add("content", updateLog.getContent());
        msg.add("contentTitle", updateLog.getContentTitle());
        msg.add("status", updateLog.getState());
        msg.add("disabled", updateLog.getDisabled());
        return msg;
    }


    //------------------------------------平台更新日志 END------------------------------------------------------------


    //-------------------------------------公共功能    ---------------------------------------------------------------
    @RequestMapping(value = "remove_app_content_packet.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeAppContentPacket() {
        String id = getRequestString("id");
        boolean success = agentAppContentPacketService.removeAppContentPacket(id);
        if (success) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("要删除的条目不存在");
        }
    }

    private List<SchoolLevel> strToActivityScope(String scopeIds) {
        if (StringUtils.isBlank(scopeIds)) {
            return Collections.emptyList();
        }
        List<SchoolLevel> result = new ArrayList<>();
        String[] levelIds = scopeIds.split(",");
        Set<String> levelSet = new HashSet<>(Arrays.asList(levelIds));
        levelSet.forEach(p -> {
            Integer typeId = SafeConverter.toInt(p);
            SchoolLevel type = SchoolLevel.safeParse(typeId, null);
            if (type != null) {
                result.add(type);
            }
        });
        return result;
    }

    private List<AgentUsedProductType> strToProductType(String entranceIds) {
        if (StringUtils.isBlank(entranceIds)) {
            return Collections.emptyList();
        }
        List<AgentUsedProductType> result = new ArrayList<>();
        String[] productIds = entranceIds.split(",");
        Set<String> productISet = new HashSet<>(Arrays.asList(productIds));
        productISet.forEach(p -> {
            Integer typeId = SafeConverter.toInt(p);
            AgentUsedProductType type = AgentUsedProductType.typeOf(typeId);
            if (type != null) {
                result.add(type);
            }
        });
        return result;
    }

    @RequestMapping(value = "edituploadimage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage editUploadImage(MultipartFile imgFile) {
        MapMessage resultMap = new MapMessage();
        Long userId = getCurrentUserId();
        String fileUrl = "";
        String filename = "";
        try {
            if (imgFile != null && !imgFile.isEmpty()) {
                fileUrl = atomicLockManager.wrapAtomic(this).keyPrefix("agent_user").keys(userId).proxy().uploadFile(imgFile);
                filename = imgFile.getName();
            }
        } catch (Exception ex) {
            logger.error("edit upload image is failed to ali oss");
        }
        if (StringUtils.isNotBlank(fileUrl)) {
            resultMap.setSuccess(true);
            resultMap.add("url", fileUrl);
            resultMap.add("fileName", filename);
            resultMap.setInfo("文件上传成功");
        } else {
            resultMap.setSuccess(false);
            resultMap.setInfo("文件上传失败，请重新上传");
        }
        return resultMap;
    }

    private String uploadFile(MultipartFile file) {
        return AgentOssManageUtils.upload(file);
    }

    @RequestMapping(value = "ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorController() {
        Long userId = getCurrentUserId();
        String action = getRequestString("action");
        MapMessage mapMessage = new MapMessage();
        switch (action) {
            case "config":
                mapMessage.set("imageActionName", "uploadimage");
                mapMessage.set("imageFieldName", "upfile");
                mapMessage.set("imageInsertAlign", "none");
                mapMessage.set("imageMaxSize", 2048000);
                mapMessage.set("imageUrlPrefix", "");
                mapMessage.setSuccess(true);
                break;
            case "uploadimage":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");
                if (imgFile.isEmpty()) {
                    return MapMessage.errorMessage("没有文件上传");
                }
                String originalFileName = imgFile.getOriginalFilename();
                try {
                    String url = atomicLockManager.wrapAtomic(this).keyPrefix("agent_user").keys(userId).proxy().uploadFile(imgFile);
                    mapMessage.add("url", url);
                    mapMessage.add("title", imgFile.getName());
                    mapMessage.add("state", "SUCCESS");
                    mapMessage.add("original", originalFileName);
                    mapMessage.setSuccess(true);
                } catch (Exception ex) {
                    mapMessage.setSuccess(false);
                    log.error("上传咨询图片异常： " + ex.getMessage());
                }
        }
        return mapMessage;
    }

    @RequestMapping(value = "publish_content.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage publishContent() {
        MapMessage msg = MapMessage.successMessage();
        String id = getRequestString("id");
        AgentAppContentPacket content = agentAppContentPacketService.loadById(id);
        if (content == null) {
            return MapMessage.errorMessage("未找到该条内容");
        }
        if (SafeConverter.toBoolean(content.getDisabled())) {
            return MapMessage.errorMessage("该数据已删除");
        }
        if (content.getState() == AppContentStateType.RELEASE) {
            return MapMessage.errorMessage("该信息已发布");
        }
        List<AgentGroupUser> allUser = baseOrgService.getAllMarketDepartmentUsers();
        Set<Long> receivers = allUser.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        if (content.getContentType() == AgentAppContentType.DATA_PACKET) {
            Set<AgentDataPacketRole> applyRoleType = content.getApplyRole();
            if (!CollectionUtils.isEmpty(applyRoleType)) {
                Set<Integer> applyRole = applyRoleType.stream().map(AgentDataPacketRole::getId).collect(Collectors.toSet());
                receivers = allUser.stream().filter(p -> applyRole.contains(SafeConverter.toInt(p.getUserRoleId()))).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            }
            agentNotifyService.sendNotify(
                    AgentNotifyType.IMPORTANT_NOTICE.getType(),
                    content.getDatumType().getDesc() + "更新",
                    "点此查看",
                    receivers,
                    "/mobile/my/data_packet.vpage?datumType=" + content.getDatumType().getId()
            );
        }
        if (content.getContentType() == AgentAppContentType.UPDATE_LOG) {
            agentNotifyService.sendNotify(
                    AgentNotifyType.PLATFORM_UPDATE.getType(),
                    StringUtils.join(content.getReferProduct().stream().map(AgentUsedProductType::getEntranceName).collect(Collectors.toList()), "、"),
                    "点此查看",
                    receivers,
                    "/mobile/notice/noticeReader.vpage?contentId=" + content.getId()
            );
        }
        if (content.getContentType() == AgentAppContentType.MARKETING_ACTIVITY) {
            if (content.getActivityEndDate().before(new Date())) {
                return MapMessage.errorMessage("该活动已过期，请修改活动时间后发布");
            }
            if (CollectionUtils.isNotEmpty(content.getActivityCity())) {
                receivers = new HashSet<>();
                Set<Integer> cityCodes = content.getActivityCity().stream().map(CityInfo::getCityCode).collect(Collectors.toSet());
                Set<Long> groups = new HashSet<>();
                for (Integer cityCode : cityCodes) {
                    groups.addAll(baseOrgService.getGroupRegionByRegion(cityCode).stream().map(AgentGroupRegion::getGroupId).collect(Collectors.toList()));
                }
                for (Long groupId : groups) {
                    receivers.addAll(baseOrgService.getAllGroupUsersByGroupId(groupId)
                            .stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet()));
                }
            }
            agentNotifyService.sendNotify(
                    AgentNotifyType.IMPORTANT_NOTICE.getType(),
                    "最新活动:" + content.getActivityName(),
                    "点此查看",
                    receivers,
                    "/mobile/notice/noticeReader.vpage?contentId=" + content.getId()
            );
        }
        content.setState(AppContentStateType.RELEASE);
        boolean success = agentAppContentPacketService.updateAppContentPacket(content);
        if (success) {
            return msg;
        } else {
            return MapMessage.errorMessage("发布失败");
        }
    }
}
