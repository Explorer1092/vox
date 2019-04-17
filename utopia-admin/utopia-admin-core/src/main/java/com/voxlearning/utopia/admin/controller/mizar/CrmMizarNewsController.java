package com.voxlearning.utopia.admin.controller.mizar;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.dao.CrmJxtNewsDao;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.vendor.api.CRMVendorService;
import com.voxlearning.utopia.service.vendor.api.constant.JxtNewsStyleType;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNews;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsTag;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowContext;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiang wei
 * @since 2016/12/19.
 */
@Controller
@RequestMapping("/mizar/news")
@Slf4j
public class CrmMizarNewsController extends AbstractAdminSystemController {

    @Inject private CrmJxtNewsDao crmJxtNewsDao;

    @Inject private WorkFlowLoaderClient workFlowLoaderClient;
    @Inject private WorkFlowServiceClient workFlowServiceClient;

    @ImportService(interfaceClass = CRMVendorService.class) private CRMVendorService crmVendorService;

    /**
     * CRM外部文章审核列表
     */
    @RequestMapping(value = "checkNewsList.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String checkNewsList() {
        return "mizar/news/checknewslist";
    }


    /**
     * 获取外部文章审核列表
     */
    @RequestMapping(value = "getCheckNewsList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getCheckNewsList() {
        int currentPage = getRequestInt("currentPage");
        if (currentPage == 0) {
            currentPage = 1;
        }
        //审核状态
        String status = getRequestString("status");
        // 内容类型
        String styleType = getRequestString("styleType");
        String startTime = getRequestString("startTime");
        String endTime = getRequestString("endTime");

        Date startDatetime = StringUtils.isNotEmpty(startTime) ? DateUtils.stringToDate(startTime) : null;
        Date endDatetime = StringUtils.isNotEmpty(endTime) ? DateUtils.stringToDate(endTime) : null;
        List<JxtNews> jxtNewsList = crmJxtNewsDao.loadAllFromSecondary();
        Set<Long> workFlowRecordIds = jxtNewsList.stream().map(JxtNews::getWorkFlowRecordId).collect(Collectors.toSet());
        Map<Long, WorkFlowRecord> workFlowRecordMap = workFlowLoaderClient.loadWorkFlowRecords(workFlowRecordIds);
        jxtNewsList = jxtNewsList.stream().filter(p -> StringUtils.isBlank(styleType) || p.getJxtNewsStyleType() == JxtNewsStyleType.parse(styleType))
                .filter(p -> p.getWorkFlowRecordId() != null)
                .filter(p -> startDatetime == null || (workFlowRecordMap.get(p.getWorkFlowRecordId()) != null && workFlowRecordMap.get(p.getWorkFlowRecordId()).getCreateDatetime().after(startDatetime)))
                .filter(p -> endDatetime == null || (workFlowRecordMap.get(p.getWorkFlowRecordId()) != null && workFlowRecordMap.get(p.getWorkFlowRecordId()).getCreateDatetime().before(endDatetime)))
                .sorted((o1, o2) -> o1.getUpdateTime().compareTo(o2.getUpdateTime()))
                .collect(Collectors.toList());
        //从process中过滤出相应的
        jxtNewsList = jxtNewsList.stream().filter(p -> StringUtils.isBlank(status) || (workFlowRecordMap.get(p.getWorkFlowRecordId()) != null && StringUtils.equalsIgnoreCase(workFlowRecordMap.get(p.getWorkFlowRecordId()).getStatus(), status)))
                .collect(Collectors.toList());
        Pageable request = new PageRequest(currentPage - 1, 10);
        Page<JxtNews> newsPage = PageableUtils.listToPage(jxtNewsList, request);
        List<Map<String, Object>> list = generateNewsMapList(newsPage.getContent(), workFlowRecordMap);
        return MapMessage.successMessage().add("jxtNewsList", list).add("totalPage", newsPage.getTotalPages()).add("currentPage", currentPage);
    }

    /**
     * 获取外部文章审核的详细信息
     */
    @RequestMapping(value = "getCheckNewsDetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String getCheckNewsDetail(Model model) {
        String newsId = getRequestString("newsId");
        JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
        if (jxtNews != null) {
            //ZyParentNewsArticle zyParentNewsArticle = parentNewsService.loadArticleById(jxtNews.getArticleId());
            Map<String, Object> map = generateNewsMap(jxtNews);
            model.addAttribute("newsMap", map);
        }
        return "mizar/news/checknews";
    }


    /**
     * 审核文章
     */
    @RequestMapping(value = "checkMizarAlbumNews.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkMizarAlbumNews() {
        //获取审核工作流的recordId
        Long workFlowRecordId = getRequestLong("workFlowRecordId");
        String newsId = getRequestString("newsId");
        String checkReason = getRequestString("checkReason");
        String tagId = getRequestString("tagStr");
        List<Long> tagList = new ArrayList<>();
        String[] tagSplit = tagId.split(",");
        for (String s : tagSplit) {
            if (StringUtils.isNotBlank(s)) {
                tagList.add(SafeConverter.toLong(s));
            }

        }
        if (StringUtils.isBlank(newsId)) {
            return MapMessage.errorMessage("未传入newsId,不能进行操作");
        }
        //获取工作流审核状态
        String operationType = getRequestString("operationType");
        if (workFlowRecordId == 0L || StringUtils.isBlank(operationType)) {
            return MapMessage.errorMessage("参数错误");
        }
        //获取审核工作流信息
        WorkFlowRecord workFlowRecord = workFlowLoaderClient.loadWorkFlowRecords(Collections.singleton(workFlowRecordId)).values().stream().findFirst().orElse(null);
        if (workFlowRecord == null) {
            return MapMessage.errorMessage("WorkFlowRecord:" + workFlowRecordId + "不存在");
        }

        //工作流记录
        WorkFlowContext workFlowContext = new WorkFlowContext();
        workFlowContext.setWorkFlowRecord(workFlowRecord);
        workFlowContext.setWorkFlowName("mizar_admin_album_news_check");
        workFlowContext.setSourceApp("mizar");
        workFlowContext.setProcessorAccount(getCurrentAdminUser().getAdminUserName());
        workFlowContext.setProcessorName("admin:" + getCurrentAdminUser().getRealName());

        MapMessage mapMessage;
        if (Objects.equals(operationType, "agree")) {//通过
            workFlowContext.setProcessNotes(checkReason);
            JxtNews jxtNews = crmVendorService.$loadJxtNews(newsId);
            jxtNews.setTagList(tagList);
            crmVendorService.$upsertJxtNews(jxtNews);
            mapMessage = workFlowServiceClient.agree(workFlowContext);
        } else if (Objects.equals(operationType, "reject")) {//驳回
            workFlowContext.setProcessNotes(checkReason);
            mapMessage = workFlowServiceClient.reject(workFlowContext);
        } else {
            return MapMessage.errorMessage("操作类型" + operationType + "不存在");
        }
        return mapMessage;
    }


    /**
     * 获取标签列表
     */
    @RequestMapping(value = "gettaglist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTagList() {
        List<JxtNewsTag> jxtNewsTagList = crmVendorService.$loadJxtNewsTagList()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .filter(e -> e.getParentId() != 0)
                .collect(Collectors.toList());
        List<Map<String, Long>> tagList = new ArrayList<>();
        jxtNewsTagList.forEach(e -> {
            Map<String, Long> map = new HashMap<>();
            map.put(e.getTagName(), e.getId());
            tagList.add(map);
        });
        return MapMessage.successMessage().add("tagList", tagList);
    }


    private List<Map<String, Object>> generateNewsMapList(List<JxtNews> jxtNewsList, Map<Long, WorkFlowRecord> workFlowRecordMap) {
        if (CollectionUtils.isEmpty(jxtNewsList)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (MapUtils.isNotEmpty(workFlowRecordMap)) {
            jxtNewsList.forEach(jxtNews -> {
                if (jxtNews != null) {
                    WorkFlowRecord workFlowRecord = workFlowRecordMap.get(jxtNews.getWorkFlowRecordId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("newsId", jxtNews.getId());
                    map.put("workFlowRecordId", jxtNews.getWorkFlowRecordId());
                    //当前审核状态
                    map.put("checkType", workFlowRecord != null ? generateWorkFlowStatus(workFlowRecord) : "");
                    map.put("title", jxtNews.getTitle());
                    map.put("commitUser", workFlowRecord != null ? workFlowRecord.getCreatorName() : null);
                    map.put("updateTime", DateUtils.dateToString(workFlowRecord != null ? workFlowRecord.getCreateDatetime() : null, DateUtils.FORMAT_SQL_DATE));
                    map.put("isOnline", jxtNews.getOnline());
                    mapList.add(map);
                }
            });
        }
        return mapList;
    }


    private Map<String, Object> generateNewsMap(JxtNews jxtNews) {
        if (jxtNews == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> newsMap = new HashMap<>();
        //资讯id
        newsMap.put("newsId", jxtNews.getId());
        //资讯标题
        newsMap.put("newsTitle", jxtNews.getTitle());
        //资讯内容类型
        newsMap.put("newsContentType", jxtNews.getJxtNewsContentType().getDesc());
        //资讯中含有音频或者视频，就传播放时间
        newsMap.put("playTime", StringUtils.isNotBlank(jxtNews.getPlayTime()) ? jxtNews.getPlayTime() : null);
        //资讯封面的预览地址
        newsMap.put("imgUrl", generateAliYunImgUrl(jxtNews.getCoverImgList().stream().findFirst().orElse(null)));
        //资讯来源
        newsMap.put("source", StringUtils.isNotBlank(jxtNews.getSource()) ? jxtNews.getSource() : StringUtils.EMPTY);
        //资讯专辑名称
        newsMap.put("albumName", StringUtils.isNotBlank(jxtNews.getAlbumId()) ? crmVendorService.$loadJxtNewsAlbum(jxtNews.getAlbumId()).getTitle() : StringUtils.EMPTY);
        //标签
        Collection<JxtNewsTag> tags = crmVendorService.$loadJxtNewsTagList().stream()
                .filter(e -> jxtNews.getTagList().contains(e.getId()))
                .collect(Collectors.toList());
        newsMap.put("tags", tags.stream().collect(Collectors.toMap(JxtNewsTag::getId, JxtNewsTag::getTagName)));
        //资讯文章内容
        newsMap.put("articleId", jxtNews.getArticleId());
        String mainSiteBaseUrl = ProductConfig.getMainSiteBaseUrl();
        newsMap.put("mainSiteBaseUrl", mainSiteBaseUrl);
        //资讯工作流的recordId
        Long workFlowRecordId = jxtNews.getWorkFlowRecordId();
        newsMap.put("workFlowRecordId", workFlowRecordId);
        //取资讯的审核历史记录
        List<Map<String, Object>> histories = new ArrayList<>();
        Map<Long, List<WorkFlowProcessHistory>> listMap;
        listMap = workFlowLoaderClient.loadWorkFlowProcessHistoriesByWorkFlowId(Collections.singleton(workFlowRecordId));
        Map<Long, WorkFlowRecord> workFlowRecordMap = workFlowLoaderClient.loadWorkFlowRecords(Collections.singleton(jxtNews.getWorkFlowRecordId()));
        //当前状态
        newsMap.put("status", generateWorkFlowStatus(workFlowRecordMap.get(workFlowRecordId)));
        if (MapUtils.isNotEmpty(listMap) && MapUtils.isNotEmpty(workFlowRecordMap)) {
            List<WorkFlowProcessHistory> processHistoryList = listMap.get(SafeConverter.toLong(jxtNews.getWorkFlowRecordId()));
            processHistoryList.forEach(e -> {
                if (e != null) {
                    Map<String, Object> map = new HashMap<>();
                    WorkFlowRecord workFlowRecord = workFlowRecordMap.get(SafeConverter.toLong(e.getWorkFlowRecordId()));
                    map.put("commitUser", workFlowRecord != null ? workFlowRecordMap.get(SafeConverter.toLong(e.getWorkFlowRecordId())).getCreatorName() : "");
                    map.put("commitTime", workFlowRecord != null ? DateUtils.dateToString(workFlowRecordMap.get(SafeConverter.toLong(e.getWorkFlowRecordId())).getCreateDatetime(), DateUtils.FORMAT_SQL_DATETIME) : "");
                    map.put("checkTime", e.getUpdateDatetime());
                    map.put("checkUser", e.getProcessorName());
                    map.put("checkResult", e.getResult());
                    map.put("checkReason", e.getProcessNotes());
                    histories.add(map);
                }
            });
        }
        newsMap.put("histories", histories);
        return newsMap;
    }


    private String generateWorkFlowStatus(WorkFlowRecord workFlowRecord) {
        if (workFlowRecord == null) {
            return "";
        }
        if (StringUtils.equals(workFlowRecord.getStatus(), "lv1")) {
            return "待审核";
        } else if (StringUtils.equals(workFlowRecord.getStatus(), "processed")) {
            return "通过";
        } else if (StringUtils.equals(workFlowRecord.getStatus(), "rejected")) {
            return "驳回";
        }
        return "";
    }

    private String generateAliYunImgUrl(String url) {
        return StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + url;
    }


}
