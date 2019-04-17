package com.voxlearning.utopia.agent.service.trainingcenter;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.bean.group.GroupData;
import com.voxlearning.utopia.agent.dao.mongo.trainingcenter.AgentArticleDao;
import com.voxlearning.utopia.agent.dao.mongo.trainingcenter.AgentArticleUserDao;
import com.voxlearning.utopia.agent.dao.mongo.trainingcenter.AgentTitleColumnDao;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentArticle;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentArticleUser;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentTitleColumn;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 培训中心-文章service
 * @author deliang.che
 * @since  2018/7/6
 */
@Named
public class AgentArticleService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private AgentArticleDao agentArticleDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentTitleColumnDao agentTitleColumnDao;
    @Inject
    private AgentArticleUserDao agentArticleUserDao;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private AgentTitleColumnService agentTitleColumnService;

    /**
     * 保存文章
     * @param id
     * @param title
     * @param oneLevelColumnId
     * @param twoLevelColumnId
     * @param coverImgUrl
     * @param roleTypeList
     * @param groupIds
     * @param openInAPP
     * @param content
     * @return
     */
    public MapMessage saveArticle(String id,String title, String oneLevelColumnId, String twoLevelColumnId, String coverImgUrl, List<AgentRoleType> roleTypeList, List<Long> groupIds,
                                  Boolean openInAPP, String content){

        AgentArticle agentArticle = new AgentArticle();
        //编辑
        if (StringUtils.isNotBlank(id)){
            agentArticle = agentArticleDao.load(id);
            //新增
        }else {
            agentArticle.setRoleTypeList(roleTypeList);
            agentArticle.setGroupIdList(groupIds);
            agentArticle.setIsPublish(false);
            agentArticle.setViewsNumAll(0);//总浏览次数
            agentArticle.setViewsNumTj(0);//天玑内浏览次数
            agentArticle.setDisabled(false);
            //设置天玑内送达人数
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroups(groupIds);
            Set<Long> userIds = groupUserList.stream().filter(item -> null != item && roleTypeList.contains(item.getUserRoleType())).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            agentArticle.setServicePersonNum(userIds.size());
        }
        agentArticle.setTitle(title);
        agentArticle.setOneLevelColumnId(oneLevelColumnId);
        agentArticle.setTwoLevelColumnId(twoLevelColumnId);
        agentArticle.setCoverImgUrl(coverImgUrl);
        agentArticle.setOpenInAPP(openInAPP);
        agentArticle.setContent(content);
        //编辑
        if (StringUtils.isNotBlank(id)){
            agentArticleDao.replace(agentArticle);
            //新增
        }else {
            agentArticleDao.insert(agentArticle);
            id = agentArticle.getId();

            String jumpUrl = "/view/mobile/crm/study/training_detail.vpage?articleId=" + id;
            agentArticle.setJumpUrl(jumpUrl);
            agentArticleDao.replace(agentArticle);

        }
        return MapMessage.successMessage().add("id",id);
    }

    /**
     * 文章列表
     * @param oneLevelColumnId
     * @param twoLevelColumnId
     * @param title
     * @return
     */
    public List<Map<String,Object>> getArticleList(String oneLevelColumnId,String twoLevelColumnId,String title){
        List<Map<String,Object>> dataList = new ArrayList<>();
        List<AgentArticle> agentArticleList = agentArticleDao.loadArticleByCondition(oneLevelColumnId,twoLevelColumnId,title);
        agentArticleList.forEach(item -> {
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("id",item.getId());
            dataMap.put("title",item.getTitle());
            //一级栏目
            AgentTitleColumn titleColumn = agentTitleColumnDao.load(item.getOneLevelColumnId());
            dataMap.put("oneLevelColumnName",null != titleColumn ? titleColumn.getName() : "");
            //二级栏目
            titleColumn = agentTitleColumnDao.load(item.getTwoLevelColumnId());
            dataMap.put("twoLevelColumnName",null != titleColumn ? titleColumn.getName() : "");

            if (item.getIsPublish()){
                AgentUser user = baseOrgService.getUser(item.getPublisherId());
                dataMap.put("publisherName",null != user ? user.getRealName() : "");
                dataMap.put("publishTime", DateUtils.dateToString(item.getPublishTime(),"MM-dd HH:mm"));
            }else {
                dataMap.put("publisherName","");
                dataMap.put("publishTime", "");
            }
            dataMap.put("viewsNumAll",item.getViewsNumAll());
            dataMap.put("publishStatus",item.getIsPublish() ? "已发布" : "未发布");
            dataList.add(dataMap);
        });

        return dataList;
    }

    /**
     * 删除文章
     * @param id
     * @return
     */
    public MapMessage deleteArticle(String id){
        AgentArticle agentArticle = agentArticleDao.load(id);
        if (null == agentArticle){
            return MapMessage.errorMessage("该文章不存在");
        }
        agentArticle.setDisabled(true);
        agentArticleDao.replace(agentArticle);
        return MapMessage.successMessage();
    }

    /**
     * 发布/下线
     * @param id
     * @param flag
     * @return
     */
    public MapMessage publishArticle(String id,Integer flag,Long currentUserId){
        AgentArticle agentArticle = agentArticleDao.load(id);
        if (null == agentArticle){
            return MapMessage.errorMessage("该文章不存在");
        }
        //发布
        if (Objects.equals(flag, AgentArticle.ONLINE)){
            agentArticle.setPublisherId(currentUserId);
            agentArticle.setPublishTime(new Date());
            agentArticle.setIsPublish(true);
            //下线
        }else {
            agentArticle.setIsPublish(false);
        }
        agentArticleDao.replace(agentArticle);
        return MapMessage.successMessage();
    }

    /**
     * 获取文章详情
     * @param id
     * @return
     */
    public Map<String,Object> getArticleDetail(String id){
        AgentArticle agentArticle = agentArticleDao.load(id);
        //更新总浏览次数
        agentArticle.setViewsNumAll((null == agentArticle.getViewsNumAll() ? 0 : agentArticle.getViewsNumAll()) + 1);
        agentArticleDao.replace(agentArticle);

        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("id",agentArticle.getId());
        dataMap.put("title",agentArticle.getTitle());
        //一级栏目
        AgentTitleColumn titleColumn = agentTitleColumnDao.load(agentArticle.getOneLevelColumnId());
        dataMap.put("oneLevelColumnId",agentArticle.getOneLevelColumnId());
        dataMap.put("oneLevelColumnName",null != titleColumn ? titleColumn.getName() : "");
        //二级栏目
        titleColumn = agentTitleColumnDao.load(agentArticle.getTwoLevelColumnId());
        dataMap.put("twoLevelColumnId",agentArticle.getTwoLevelColumnId());
        dataMap.put("twoLevelColumnName",null != titleColumn ? titleColumn.getName() : "");

        dataMap.put("coverImgUrl",agentArticle.getCoverImgUrl());

        //总浏览次数
        dataMap.put("viewsNumAll",null == agentArticle.getViewsNumAll() ? 0 : agentArticle.getViewsNumAll());
        //天玑内浏览次数
        dataMap.put("viewsNumTj",null == agentArticle.getViewsNumTj() ? 0 : agentArticle.getViewsNumTj());
        //天玑内送达人数
        dataMap.put("servicePersonNum",null == agentArticle.getServicePersonNum() ? 0 : agentArticle.getServicePersonNum());

        //天玑内浏览人数
        List<AgentArticleUser> articleUserList = agentArticleUserDao.loadByArticleId(id);
        Set<Long> userIds = articleUserList.stream().filter(Objects::nonNull).map(AgentArticleUser::getUserId).collect(Collectors.toSet());
        dataMap.put("viewsPersonNumTj",userIds.size());
        //天玑内阅读率
        dataMap.put("viewsRateTj",MathUtils.doubleDivide(userIds.size() * 100,(null == agentArticle.getServicePersonNum() ? 0 : agentArticle.getServicePersonNum())));

        dataMap.put("roleTypeList",agentArticle.getRoleTypeList());
        dataMap.put("groupIdList",agentArticle.getGroupIdList());

        dataMap.put("openInAPP",agentArticle.getOpenInAPP() ? "是" : "否");
        dataMap.put("content",agentArticle.getContent());
        dataMap.put("jumpUrl",getPlatformDomain() + agentArticle.getJumpUrl());
        return dataMap;
    }

    /**
     * 获取文章浏览情况导出数据
     * @param id
     * @return
     */
    public Map<String,Object> getArticleExportData(String id){
        Map<String,Object> resultMap = new HashMap<>();
        List<Map<String,Object>> dataList = new ArrayList<>();

        AgentArticle agentArticle = agentArticleDao.load(id);
        if (null == agentArticle){
            return new HashMap<>();
        }

        //组装各部门之间的等级对应关系
        List<GroupData> groupDataList = new ArrayList<>();
        //部门级别为“市场部”的所有下属人员信息
        List<AgentGroupUser> groupUserList = new ArrayList<>();

        //获取该文章在天玑内送达人员
        List<Long> groupIdList = agentArticle.getGroupIdList();
        List<AgentRoleType> roleTypeList = agentArticle.getRoleTypeList();
        List<AgentGroupUser> serviceGroupUserList = baseOrgService.getGroupUserByGroups(groupIdList);
        Set<Long> userIds = serviceGroupUserList.stream().filter(item -> null != item && roleTypeList.contains(item.getUserRoleType())).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        Map<Long, AgentUser> userMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));

        //获取级别为“市场部”的部门及其子部门与用户关系，并且组装各部门与其所有父级部门之间的等级对应关系
        List<AgentGroup> marketingGroupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.Marketing);
        marketingGroupList.forEach(item -> {
            groupUserList.addAll(baseOrgService.getAllGroupUsersByGroupIdWithGroupData(item.getId(),groupDataList));
        });
        //用户与部门对应关系
        Map<Long, Long> userGroupIdMap = groupUserList.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, AgentGroupUser::getGroupId, (o1, o2) -> o1));
        //用户与角色对应关系
        Map<Long, Integer> userRoleMap = groupUserList.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, AgentGroupUser::getUserRoleId, (o1, o2) -> o1));
        //部门之间对应关系
        Map<Long, GroupData> groupDataMap = groupDataList.stream().collect(Collectors.toMap(GroupData::getGroupId, Function.identity(), (o1, o2) -> o1));

        //获取用户与文章浏览关系信息
        Map<Long, AgentArticleUser> userArticleMap = agentArticleUserDao.loadByArticleId(id).stream().collect(Collectors.toMap(AgentArticleUser::getUserId, Function.identity(), (o1, o2) -> o1));
        userIds.forEach(userId -> {
            Map<String,Object> dataMap = new HashMap<>();
            //设置“姓名”
            AgentUser agentUser = userMap.get(userId);
            if (null != agentUser){
                dataMap.put("userName",agentUser.getRealName());
            }
            //设置“浏览次数”“首次浏览时间”
            AgentArticleUser agentArticleUser = userArticleMap.get(userId);
            if (null != agentArticleUser){
                dataMap.put("viewsNum",null == agentArticleUser.getViewsNum() ? 0 : agentArticleUser.getViewsNum());
                dataMap.put("firstViewsTime",null == agentArticleUser.getFirstViewsTime() ? "" : DateUtils.dateToString(agentArticleUser.getFirstViewsTime(),"yyyy-MM-dd HH:mm"));
            }else {
                dataMap.put("viewsNum",0);
            }
            //设置“角色”
            Integer roleId = userRoleMap.get(userId);
            if (null != roleId){
                dataMap.put("userRole", AgentRoleType.of(roleId) != null ? AgentRoleType.of(roleId).getRoleName() : "");
            }

            //设置“市场”“大区”“区域”“分区”
            if (userGroupIdMap.containsKey(userId)) {
                Long groupId = userGroupIdMap.get(userId);
                GroupData groupData = groupDataMap.get(groupId);
                if (null != groupData) {
                    dataMap.put("marketingName", groupData.getMarketingName());
                    dataMap.put("regionName", groupData.getRegionName());
                    dataMap.put("areaName", groupData.getAreaName());
                    dataMap.put("cityName", groupData.getCityName());
                }
            }
            //发布时间
            if (agentArticle.getIsPublish()){
                dataMap.put("publishTime",DateUtils.dateToString(agentArticle.getPublishTime(),"yyyy-MM-dd HH:mm"));
            }
            dataList.add(dataMap);

        });
        resultMap.put("title",agentArticle.getTitle());
        resultMap.put("dataList",dataList);
        return resultMap;
    }



    public void exportArticleDetail(SXSSFWorkbook workbook, List<Map<String,Object>> dataList){
        try {
            Sheet sheet = workbook.createSheet("文章浏览情况");
            sheet.createFreezePane(0, 1, 0, 1);
            Font font = workbook.createFont();
            font.setFontName("宋体");
            font.setFontHeightInPoints((short) 10);
            CellStyle firstRowStyle = workbook.createCellStyle();
            firstRowStyle.setFont(font);
            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
            Row firstRow = sheet.createRow(0);
            HssfUtils.setCellValue(firstRow, 0, firstRowStyle, "姓名");
            HssfUtils.setCellValue(firstRow, 1, firstRowStyle, "浏览次数");
            HssfUtils.setCellValue(firstRow, 2, firstRowStyle, "角色");
            HssfUtils.setCellValue(firstRow, 3, firstRowStyle, "市场");
            HssfUtils.setCellValue(firstRow, 4, firstRowStyle, "大区");
            HssfUtils.setCellValue(firstRow, 5, firstRowStyle, "区域");
            HssfUtils.setCellValue(firstRow, 6, firstRowStyle, "分区");
            HssfUtils.setCellValue(firstRow, 7, firstRowStyle, "首次浏览时间");
            HssfUtils.setCellValue(firstRow, 8, firstRowStyle, "发布时间");

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

            if(CollectionUtils.isNotEmpty(dataList)){
                Integer index = 1;
                for(Map<String,Object> dataMap : dataList){
                    Row row = sheet.createRow(index++);
                    HssfUtils.setCellValue(row,0,cellStyle,ConversionUtils.toString(dataMap.get("userName")));
                    HssfUtils.setCellValue(row,1,cellStyle,ConversionUtils.toLong(dataMap.get("viewsNum")));
                    HssfUtils.setCellValue(row,2,cellStyle,ConversionUtils.toString(dataMap.get("userRole")));
                    HssfUtils.setCellValue(row,3,cellStyle,ConversionUtils.toString(dataMap.get("marketingName")));
                    HssfUtils.setCellValue(row,4,cellStyle,ConversionUtils.toString(dataMap.get("regionName")));
                    HssfUtils.setCellValue(row,5,cellStyle,ConversionUtils.toString(dataMap.get("areaName")));
                    HssfUtils.setCellValue(row,6,cellStyle,ConversionUtils.toString(dataMap.get("cityName")));
                    HssfUtils.setCellValue(row,7,cellStyle,ConversionUtils.toString(dataMap.get("firstViewsTime")));
                    HssfUtils.setCellValue(row,8,cellStyle,ConversionUtils.toString(dataMap.get("publishTime")));
                }
            }
        } catch (Exception ex) {
            logger.error("error info: ",ex);
            emailServiceClient.createPlainEmail()
                    .body("error info: "+ex)
                    .subject("导出文章浏览异常【" + RuntimeMode.current().getStageMode() + "】")
                    .to("deliang.che@17zuoye.com")
                    .send();
        }
    }

    /**
     * 获取全部一级栏目
     * @return
     */
    public List<Map<String,Object>> getAllOneLevelColumn(){
        List<AgentTitleColumn> list = agentTitleColumnDao.query();
        List<Map<String,Object>> oneLevelColumnList = agentTitleColumnService.findFirstColumnList(list,1);
        return oneLevelColumnList;
    }

    /**
     * 根据一级栏目ID获取二级栏目及文章
     * @param oneLevelColumnId
     * @return
     */
    public List<Map<String,Object>> getArticleByOneLevelColumn(String oneLevelColumnId,Long userId){
        List<Map<String,Object>> dataList = new ArrayList<>();

        AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
        AgentRoleType userRole = baseOrgService.getUserRole(userId);

        //根据一级栏目ID获取二级栏目
        List<AgentTitleColumn> titleColumnList = agentTitleColumnDao.findByParentId(oneLevelColumnId);
//        Map<String, AgentTitleColumn> towLevelColumnMap = titleColumnList.stream().collect(Collectors.toMap(AgentTitleColumn::getId, Function.identity(), (o1, o2) -> o1));
        //按照栏目创建时间由近到远排序
        List<AgentTitleColumn> towLevelColumnListSorted = titleColumnList.stream().sorted(Comparator.comparing(AgentTitleColumn::getSortId)).collect(Collectors.toList());

        Set<String> towLevelColumnIds = towLevelColumnListSorted.stream().filter(Objects::nonNull).map(AgentTitleColumn::getId).collect(Collectors.toSet());
        //二级栏目与文章关系
        Map<String, List<AgentArticle>> towLevelColumnIdArticleMap = agentArticleDao.loadArticleByTwoLevelColumnIds(towLevelColumnIds);

        //过滤出已发布的、用户权限范围内的文章
        Map<String,List<AgentArticle>> authorizedArticleMap = new HashMap<>();
        towLevelColumnIdArticleMap.forEach((k,v) -> {
            if (null != k && CollectionUtils.isNotEmpty(v)){
                List<AgentArticle> authorizedArticleList = v.stream().filter(item -> null != item && item.getIsPublish() && item.getGroupIdList().contains(groupUser.getGroupId()) && item.getRoleTypeList().contains(userRole)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(authorizedArticleList)){
                    //按照文章发布时间由近到远排序
                    List<AgentArticle> authorizedArticleListSorted = authorizedArticleList.stream().sorted(Comparator.comparing(AgentArticle::getPublishTime).reversed()).collect(Collectors.toList());
                    authorizedArticleMap.put(k,authorizedArticleListSorted);
                }
            }
        });
        towLevelColumnListSorted.forEach(item -> {
            List<AgentArticle> agentArticleList = authorizedArticleMap.get(item.getId());
            if (null != agentArticleList && CollectionUtils.isNotEmpty(agentArticleList)){
                Map<String,Object> dataMap = new HashMap<>();
                dataMap.put("towLevelColumnId",item.getId());
                dataMap.put("towLevelColumnName",item.getName());
                List<Map<String,Object>> articleList = new ArrayList<>();
                agentArticleList.forEach(article -> {
                    Map<String,Object> articleMap = new HashMap<>();
                    articleMap.put("id",article.getId());
                    articleMap.put("coverImgUrl",article.getCoverImgUrl());
                    articleMap.put("title",article.getTitle());
                    articleMap.put("publishTime",article.getPublishTime());
                    articleList.add(articleMap);
                });
                dataMap.put("articleList",articleList);
                dataList.add(dataMap);
            }
        });
        return dataList;
    }

    /**
     * 获取文章详情
     * @param articleId
     * @param userId
     * @return
     */
    public MapMessage getArticleDetail(String articleId,Long userId) {
        MapMessage mapMessage = MapMessage.successMessage();
        AgentArticle article = agentArticleDao.load(articleId);
        if (null == article){
            return MapMessage.errorMessage("文章不存在！");
        }
        //在天玑内打开
        if (null != userId){
            List<AgentArticleUser> articleUserList = agentArticleUserDao.loadByArticleId(articleId);
            articleUserList = articleUserList.stream().filter(item -> null != item && Objects.equals(item.getUserId(), userId)).collect(Collectors.toList());
            //如果当前用户浏览过该文章，更新浏览次数
            if (CollectionUtils.isNotEmpty(articleUserList)){
                AgentArticleUser agentArticleUser = articleUserList.get(0);
                agentArticleUser.setViewsNum(agentArticleUser.getViewsNum() + 1);
                agentArticleUserDao.replace(agentArticleUser);
                //如果没有浏览过，新增浏览记录
            }else {
                AgentArticleUser agentArticleUser = new AgentArticleUser();
                agentArticleUser.setArticleId(articleId);
                agentArticleUser.setUserId(userId);
                agentArticleUser.setViewsNum(1);
                agentArticleUser.setFirstViewsTime(new Date());
                agentArticleUser.setDisabled(false);
                agentArticleUserDao.insert(agentArticleUser);
            }
            article.setViewsNumTj((null == article.getViewsNumTj() ? 0 : article.getViewsNumTj()) + 1);
        }
//        //在天玑外打开，并且要跳至APP内打开
//        if (null == userId && article.getOpenInAPP()){
//            return MapMessage.errorMessage("请在天玑内打开！");
//        }
        //更新总浏览次数
        article.setViewsNumAll((null == article.getViewsNumAll() ? 0 : article.getViewsNumAll()) + 1);
        agentArticleDao.replace(article);
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put("id",article.getId());
        dataMap.put("title",article.getTitle());
        dataMap.put("content",article.getContent());
        dataMap.put("publishTime",article.getPublishTime());
        dataMap.put("openInAPP",article.getOpenInAPP());
        mapMessage.put("dataMap",dataMap);
        return mapMessage;
    }

    /**
     * 根据二级栏目ID获取文章列表
     * @param towLevelColumnId
     * @param userId
     * @return
     */
    public List<Map<String,Object>> getArticleByTowLevelColumn(String towLevelColumnId,Long userId){
        List<Map<String,Object>> dataList = new ArrayList<>();
        AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        //二级栏目文章
        List<AgentArticle> towLevelColumnArticleList = agentArticleDao.loadArticleByTwoLevelColumnId(towLevelColumnId);
        //过滤出已发布的、用户权限范围内的文章
        List<AgentArticle> authorizedArticleList = towLevelColumnArticleList.stream().filter(item -> null != item && item.getIsPublish() && item.getGroupIdList().contains(groupUser.getGroupId()) && item.getRoleTypeList().contains(userRole)).collect(Collectors.toList());

        //根据发布时间分组
//        Map<String, List<AgentArticle>> publishTimeArticleMap = authorizedArticleList.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(p -> DateUtils.dateToString(p.getPublishTime(), DateUtils.FORMAT_SQL_DATE)));
        //根据发布时间，有近到远排序
        List<AgentArticle> articleListSorted = authorizedArticleList.stream().sorted(Comparator.comparing(AgentArticle::getPublishTime).reversed()).collect(Collectors.toList());

        Map<String, List<AgentArticle>> publishTimeArticleMap = articleListSorted.stream().collect(Collectors.groupingBy(p -> DateUtils.dateToString(p.getPublishTime(), DateUtils.FORMAT_SQL_DATE)));

        List<String> publishTimeList = publishTimeArticleMap.keySet().stream().sorted((o1, o2) -> o2.compareTo(o1)).collect(Collectors.toList());

        publishTimeList.forEach(item -> {
            List<AgentArticle> agentArticleList = publishTimeArticleMap.get(item);
            Map<String,Object> dataMap = new HashMap<>();
            dataMap.put("publishTime",item);
            List<Map<String,Object>> articleList = new ArrayList<>();
            agentArticleList.forEach(article -> {
                Map<String,Object> articleMap = new HashMap<>();
                articleMap.put("id",article.getId());
                articleMap.put("title",article.getTitle());
                articleMap.put("coverImgUrl",article.getCoverImgUrl());
                articleMap.put("publishTime",article.getPublishTime());
                articleList.add(articleMap);
            });
            dataMap.put("articleList",articleList);
            dataList.add(dataMap);
        });

        return dataList;
    }

    /**
     * 获取运行环境
     * @return
     */
    private String getPlatformDomain() {
        if (RuntimeMode.isDevelopment()) {
            return "https://localhost:8083";
        } else if (RuntimeMode.isTest()) {
            return "https://marketing.test.17zuoye.net";
        } else if (RuntimeMode.isStaging()) {
            return "https://marketing.staging.17zuoye.net";
        } else if (RuntimeMode.isProduction()) {
            return "https://marketing.17zuoye.com";
        }
        return "http://marketing.17zuoye.com";
    }


}
