package com.voxlearning.utopia.agent.controller.trainingcenter;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentArticle;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.trainingcenter.AgentArticleService;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import lombok.Cleanup;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 培训中心-文章管理
 * @author deliang.che
 * @since 2018/7/6
 */
@Controller
@RequestMapping(value = "/trainingcenter/article")
public class ArticleController extends AbstractAgentController {
    @Inject
    private AgentArticleService agentArticleService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private EmailServiceClient emailServiceClient;
    @RequestMapping("index.vpage")
    public String articleListPage(){
        return "trainingcenter/article/list";
    }

    @RequestMapping("add.vpage")
    public String articleAddPage(Model model){
        model.addAttribute("allRoleTypeList",AgentRoleType.values());
        return "trainingcenter/article/add";
    }

    @RequestMapping("edit.vpage")
    public String articleEditPage(Model model){
        String id = getRequestString("id");
        if (StringUtils.isNotBlank(id)){
            model.addAttribute("articleInfo",agentArticleService.getArticleDetail(id));
        }
        return "trainingcenter/article/edit";
    }

    @RequestMapping("detail.vpage")
    public String articleDetailPage(Model model){
        String id = getRequestString("id");
        if (StringUtils.isNotBlank(id)){
            model.addAttribute("articleInfo",agentArticleService.getArticleDetail(id));
        }
        return "trainingcenter/article/detail";
    }

    /**
     * 获取发布对象（所有角色列表）
     * @return
     */
    @RequestMapping(value = "get_all_role_type_list.vpage")
    @ResponseBody
    public MapMessage getAllRoleTypeList(){
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.put("allRoleTypeList",AgentRoleType.values());
        return mapMessage;
    }

    /**
     * 获取发布部门（所有部门树）
     * @return
     */
    @RequestMapping(value = "get_all_department_tree.vpage" , method = RequestMethod.GET)
    @ResponseBody
    public String getAllDepartmentTree(){
        Set<Long> selectedGroupIds = requestLongSet("selectedGroupIds");
        Map<String, Map<String, Object>> allGroupTree = baseOrgService.buildAllGroupTree();
        List<Map<String, Object>> allGroupList = new ArrayList<>();

        //获取admin管理员权限范围的部门
        List<Long> groupIdList = new ArrayList<>();
        List<AgentGroup> groupList = baseOrgService.getRootAgentGroups();
        if (CollectionUtils.isNotEmpty(groupList)) {
            groupIdList.addAll(groupList.stream().map(AbstractDatabaseEntity::getId).collect(Collectors.toSet()));
        }
        for (Long groupId : groupIdList) {
            CollectionUtils.addNonNullElement(allGroupList, allGroupTree.get(String.valueOf(groupId)));
        }

        if(CollectionUtils.isNotEmpty(selectedGroupIds) && CollectionUtils.isNotEmpty(allGroupList)){
            baseOrgService.markSelectedGroup(allGroupList, selectedGroupIds);
        }
        return JsonUtils.toJson(allGroupList);
    }

    /**
     * 保存文章
     * @return
     */
    @RequestMapping(value = "save_article.vpage" , method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveArticle() {
        String id = getRequestString("id");
        String title = getRequestString("title");
        if (StringUtils.isBlank(title)) {
            return MapMessage.errorMessage("标题不可为空");
        }
        String oneLevelColumnId = getRequestString("oneLevelColumnId");
        String twoLevelColumnId = getRequestString("twoLevelColumnId");
        if (StringUtils.isBlank(oneLevelColumnId) || StringUtils.isBlank(twoLevelColumnId)) {
            return MapMessage.errorMessage("栏目不可为空");
        }
        String coverImgUrl = getRequestString("coverImgUrl");
        if (StringUtils.isBlank(coverImgUrl)){
            return MapMessage.errorMessage("封面不可为空");
        }
        Set<Integer> roleIds = requestIntegerSet("roleIds");
        Set<Long> groupIds = requestLongSet("groupIds");
        if (CollectionUtils.isEmpty(roleIds)){
            return MapMessage.errorMessage("发布对象不可为空");
        }
        if (CollectionUtils.isEmpty(groupIds)){
            return MapMessage.errorMessage("发布部门不可为空");
        }

        Boolean openInAPP = requestBoolean("openInAPP");
        if(openInAPP == null){
            return MapMessage.errorMessage("请确定是否跳至APP内打开");
        }
        String content = getRequestString("content");
        if (StringUtils.isBlank(content)){
            return MapMessage.errorMessage("正文不可为空");
        }

        List<AgentRoleType> roleTypeList = roleIds.stream().map(AgentRoleType::of).filter(Objects::nonNull).collect(Collectors.toList());
        return agentArticleService.saveArticle(id,title,oneLevelColumnId,twoLevelColumnId,coverImgUrl,roleTypeList,new ArrayList<>(groupIds),openInAPP,content);
    }

    /**
     * 文章列表
     * @return
     */
    @RequestMapping(value = "article_list.vpage" , method = RequestMethod.GET)
    @ResponseBody
    public MapMessage articleList(){
        MapMessage mapMessage = MapMessage.successMessage();
        String oneLevelColumnId = getRequestString("oneLevelColumnId");
        String twoLevelColumnId = getRequestString("twoLevelColumnId");
        String title = getRequestString("title");
        return mapMessage.add("dataList",agentArticleService.getArticleList(oneLevelColumnId,twoLevelColumnId,title));
    }

    /**
     * 文章详情
     * @return
     */
    @RequestMapping(value = "article_detail.vpage" , method = RequestMethod.GET)
    @ResponseBody
    public MapMessage articleDetail(){
        MapMessage mapMessage = MapMessage.successMessage();
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("文章ID不正确");
        }
        return mapMessage.add("dataMap",agentArticleService.getArticleDetail(id));
    }

    /**
     * 删除文章
     * @return
     */
    @RequestMapping(value = "delete_article.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage deleteArticle() {
        String id = requestString("id");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("该文章不存在");
        }
        return agentArticleService.deleteArticle(id);
    }

    /**
     * 发布文章
     * @return
     */
    @RequestMapping(value = "publish_article.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage publishArticle() {
        String id = requestString("id");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("该文章不存在");
        }
        Long currentUserId = getCurrentUserId();
        return agentArticleService.publishArticle(id,AgentArticle.ONLINE,currentUserId);
    }


    /**
     * 下线文章
     * @return
     */
    @RequestMapping(value = "offline_article.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage offlineArticle() {
        String id = requestString("id");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("该文章不存在");
        }
        Long currentUserId = getCurrentUserId();
        return agentArticleService.publishArticle(id,AgentArticle.OFFLINE,currentUserId);
    }


    /**
     * 文章浏览情况导出
     */
    @RequestMapping(value = "export_article_views_info.vpage", method = RequestMethod.GET)
    public void exportArticleDetail() {
        String id = getRequestString("id");
        try {
            Map<String, Object> resultMap = agentArticleService.getArticleExportData(id);
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) resultMap.get("dataList");
            //获取当前时间
            String nowTime = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME);

            //设置导出文件名
            String fileName = resultMap.get("title") + "_文章浏览情况_"+ nowTime +".xlsx";
            //导出Excel文件
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            agentArticleService.exportArticleDetail(workbook,dataList);
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            outStream.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile(
                    fileName,
                    "application/vnd.ms-excel",
                    outStream.toByteArray());
            outStream.close();
            workbook.dispose();
        }catch (Exception e){
            logger.error("error info: ",e);
            emailServiceClient.createPlainEmail()
                    .body("error info: "+e)
                    .subject("导出文章浏览情况异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("deliang.che@17zuoye.com")
                    .send();

        }
    }

}