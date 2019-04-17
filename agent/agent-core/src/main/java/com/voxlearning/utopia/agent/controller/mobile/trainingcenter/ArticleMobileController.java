package com.voxlearning.utopia.agent.controller.mobile.trainingcenter;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.dao.mongo.trainingcenter.AgentArticleDao;
import com.voxlearning.utopia.agent.interceptor.AgentHttpRequestContext;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentArticle;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentTitleColumn;
import com.voxlearning.utopia.agent.service.trainingcenter.AgentArticleService;
import com.voxlearning.utopia.agent.service.trainingcenter.AgentTitleColumnService;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.agent.constants.AgentConstants.*;

/**
 * 培训中心-文章管理
 * @author deliang.che
 * @since 2018/7/10
 */
@Controller
@RequestMapping(value = "/mobile/trainingcenter/article")
public class ArticleMobileController extends AbstractAgentController {
    @Inject
    private UserAdvertisementServiceClient userAdvertisementServiceClient;
    @Inject
    protected CdnResourceUrlGenerator cdnResourceUrlGenerator;
    @Inject
    private AgentArticleService agentArticleService;
    @Inject
    private AgentTitleColumnService agentTitleColumnService;
    @Inject
    private AgentArticleDao agentArticleDao;

    /**
     * 广告位banner信息
     * @return
     */
    @RequestMapping(value = "ad_info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage adInfo() {
        String slotId = getRequestString(REQ_AD_POSITION);
        Long userId = getRequestLong("userId");
        List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService().loadNewAdvertisementData(userId, slotId, getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));
        if (CollectionUtils.isEmpty(newAdMappers)) {
            MapMessage.successMessage();
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (int i = 0; i < newAdMappers.size(); i++) {
            NewAdMapper newAdMapper = newAdMappers.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put(RES_RESULT_AD_IMG, combineCdbUrl(newAdMapper.getImg()));
//            String link = AdvertiseRedirectUtils.redirectUrl(newAdMapper.getId(), i, getRequestString(REQ_APP_NATIVE_VERSION), getRequestString(REQ_SYS), "", 0L);
//            map.put(RES_RESULT_AD_URL, ProductConfig.getMainSiteBaseUrl() + link);
            map.put(RES_RESULT_AD_URL,newAdMapper.getUrl());
            mapList.add(map);

            if (Boolean.FALSE.equals(newAdMappers.get(i).getLogCollected())) {
                continue;
            }
            //曝光打点
            LogCollector.info("sys_new_ad_show_logs",
                    MiscUtils.map(
                            "user_id", userId,
                            "env", RuntimeMode.getCurrentStage(),
                            "version", getRequestString("version"),
                            "aid", newAdMapper.getId(),
                            "acode", newAdMapper.getCode(),
                            "index", i,
                            "slotId", slotId,
                            "client_ip", getWebRequestContext().getRealRemoteAddress(),
                            "time", DateUtils.dateToString(new Date()),
                            "agent", getRequest().getHeader("User-Agent"),
                            "uuid", UUID.randomUUID().toString(),
                            "system", getRequestString(REQ_SYS),
                            "system_version", getRequestString("sysVer")
                    ));
        }

        Long currentUserId = getCurrentUserId();
        AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(currentUserId).stream().findFirst().orElse(null);
        AgentRoleType userRole = baseOrgService.getUserRole(currentUserId);
        //筛选出所有文章banner、文章ID
        List<String> articleIdList = new ArrayList<>();
        List<Map<String, Object>> articleMapList = new ArrayList<>();
        mapList.forEach(item -> {
            String ad_url = ConversionUtils.toString(item.get(RES_RESULT_AD_URL));
            String firstUrlParam = getFirstUrlParam(ad_url);
            if (StringUtils.isNotBlank(firstUrlParam)){
                String[] strArr = firstUrlParam.split("[=]");
                if (strArr.length > 1){
                    String firstUrlParamKey = strArr[0];
                    String firstUrlParamValue = strArr[1];
                    if ("articleId".equals(firstUrlParamKey)){
                        articleIdList.add(firstUrlParamValue);
                        articleMapList.add(item);
                    }
                }
            }
        });

        //过滤出已发布的、用户权限范围内的文章ID
        Map<String, AgentArticle> articleMap = agentArticleDao.loads(articleIdList);
        List<String> authorizedArticleIdList = articleMap.values().stream().filter(item -> null != item && item.getIsPublish() && item.getGroupIdList().contains(groupUser.getGroupId()) && item.getRoleTypeList().contains(userRole)).map(AgentArticle::getId).collect(Collectors.toList());

        //在文章banner中筛选出用户权限范围内的banner
        List<Map<String, Object>> finalMapList = new ArrayList<>();
        articleMapList.forEach(item -> {
            String ad_url = ConversionUtils.toString(item.get(RES_RESULT_AD_URL));
            String firstUrlParam = getFirstUrlParam(ad_url);
            if (StringUtils.isNotBlank(firstUrlParam)){
                String[] strArr = firstUrlParam.split("[=]");
                if (strArr.length > 1){
                    String firstUrlParamValue = strArr[1];
                    if (authorizedArticleIdList.contains(firstUrlParamValue)){
                        finalMapList.add(item);
                    }
                }
            }
        });
        return MapMessage.successMessage().add(RES_RESULT_AD_INFO, finalMapList);
    }

    protected String combineCdbUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return StringUtils.EMPTY;
        }
        return getCdnBaseUrlStaticSharedWithSep() + "gridfs/" + url;
    }

    protected String getCdnBaseUrlStaticSharedWithSep() {
        return cdnResourceUrlGenerator.getCdnBaseUrlStaticSharedWithSep(getWebRequestContext().getRequest());
    }

    public AgentHttpRequestContext getWebRequestContext() {
        return (AgentHttpRequestContext) DefaultContext.get();
    }

    public String getFirstUrlParam(String url){
        if(StringUtils.isNotBlank(url)){
            String[] strArr= url.split("[?]");
            if (strArr.length > 1){
                String[] urlParamArr = strArr[1].split("[&]");
                if (urlParamArr.length > 0){
                    return urlParamArr[0];
                }
            }
        }
        return "";
    }


    /**
     * 获取全部一级栏目
     * @return
     */
    @RequestMapping(value = "one_level_column_list.vpage")
    @ResponseBody
    public MapMessage oneLevelColumnList() {
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("dataList",agentArticleService.getAllOneLevelColumn());
        return mapMessage;
    }

    /**
     * 根据一级栏目获取二级栏目及文章
     * @return
     */
    @RequestMapping(value = "article_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage articleList() {
        MapMessage mapMessage = MapMessage.successMessage();
        String oneLevelColumnId = requestString("oneLevelColumnId");
        if (StringUtils.isBlank(oneLevelColumnId)){
            return MapMessage.errorMessage("一级栏目ID不正确");
        }
        mapMessage.put("dataList",agentArticleService.getArticleByOneLevelColumn(oneLevelColumnId,getCurrentUserId()));
        return mapMessage;
    }

    /**
     * 获取文章详情
     * @return
     */
    @RequestMapping(value = "article_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage articleDetail(HttpServletRequest request) {
        String articleId = requestString("articleId");
        if (StringUtils.isBlank(articleId)){
            return MapMessage.errorMessage("文章ID不正确");
        }
        //判断是否在天玑打开
        Long userId = null;
        Map<String, String> headerMap = getWebRequestContext().getHeaderMap();
        String userAgent = request.getHeader(headerMap.getOrDefault("user-agent", "User-Agent"));
        if (StringUtils.isNotBlank(userAgent)) {
            int index = userAgent.indexOf("Tianji");
            if (index != -1) {
                Cookie[] cookies = request.getCookies();
                if (cookies != null && cookies.length > 0){
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals("userId")) {
                            userId = SafeConverter.toLong(cookie.getValue());
                            break;
                        }
                    }
                }
            }else {
                userId = null;
            }
        }
        return agentArticleService.getArticleDetail(articleId,userId);
    }

    /**
     * 根据二级栏目ID获取文章列表
     * @return
     */
    @RequestMapping(value = "two_level_column_article_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage twoLevelColumnArticleList() {
        MapMessage mapMessage = MapMessage.successMessage();
        String towLevelColumnId = requestString("towLevelColumnId");
        if (StringUtils.isBlank(towLevelColumnId)){
            return MapMessage.errorMessage("二级栏目ID不正确");
        }
        AgentTitleColumn agentTitleColumn = agentTitleColumnService.findColumnById(towLevelColumnId);
        if (null == agentTitleColumn){
            return MapMessage.errorMessage("栏目不存在！");
        }
        mapMessage.put("towLevelColumnName",agentTitleColumn.getName());
        mapMessage.put("dataList",agentArticleService.getArticleByTowLevelColumn(towLevelColumnId,getCurrentUserId()));
        return mapMessage;
    }
}