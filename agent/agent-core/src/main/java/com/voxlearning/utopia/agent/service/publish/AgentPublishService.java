package com.voxlearning.utopia.agent.service.publish;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.dao.mongo.publish.AgentPublishControlDao;
import com.voxlearning.utopia.agent.dao.mongo.publish.AgentPublishDao;
import com.voxlearning.utopia.agent.dao.mongo.publish.AgentPublishDataDao;
import com.voxlearning.utopia.agent.persist.entity.publish.AgentPublish;
import com.voxlearning.utopia.agent.persist.entity.publish.AgentPublishControl;
import com.voxlearning.utopia.agent.persist.entity.publish.AgentPublishData;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AgentPublishService
 *
 * @author song.wang
 * @date 2018/4/23
 */
@Named
public class AgentPublishService extends AbstractAgentService {

    @Inject
    private AgentPublishDao agentPublishDao;
    @Inject
    private AgentPublishDataDao agentPublishDataDao;
    @Inject
    private AgentPublishControlDao agentPublishControlDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private BaseUserService baseUserService;

    public MapMessage uploadWorkBook(XSSFWorkbook workbook, String publishId, String title, boolean isGrouped){
        MapMessage analysisResult = analysisWorkbook(workbook, isGrouped);
        if(!analysisResult.isSuccess()){
            return analysisResult;
        }

        List<String> titleList = (List<String>)analysisResult.get("titleList");
        List<AgentPublishData> dataList = (List<AgentPublishData>)analysisResult.get("dataList");

        // 创建或更新publish记录
        AgentPublish publish = null;
        if(StringUtils.isNotBlank(publishId)){
            publish = agentPublishDao.load(publishId);
        }

        AuthCurrentUser user = getCurrentUser();
        if(publish != null) {
            publish.setTitle(title);
            publish.setDataTitleList(titleList);
            publish.setOperatorId(user.getUserId());
            publish.setOperatorName(user.getRealName());
            agentPublishDao.replace(publish);
        }else {
            publish = new AgentPublish();
            publish.setTitle(title);
            publish.setUserId(user.getUserId());
            publish.setUserName(user.getRealName());
            publish.setOperatorId(user.getUserId());
            publish.setOperatorName(user.getRealName());
            publish.setStatus(AgentPublish.STATUS_OFFLINE);
            publish.setDataTitleList(titleList);
            publish.setDisabled(false);
            agentPublishDao.insert(publish);
        }
        publishId = publish.getId();

        // 更新数据部分
        agentPublishDataDao.deleteByPublishId(publishId);
        for(AgentPublishData publishData : dataList){
            publishData.setPublishId(publishId);
        }
        agentPublishDataDao.inserts(dataList);

        // 更新权限部分
        AgentPublishControl agentPublishControl = agentPublishControlDao.loadByPublishId(publishId);
        if(agentPublishControl == null){
            agentPublishControl = new AgentPublishControl();
            agentPublishControl.setPublishId(publishId);
            agentPublishControl.setDisabled(false);
        }
        agentPublishControl.setIsGrouped(isGrouped);
        agentPublishControlDao.upsert(agentPublishControl);
        return MapMessage.successMessage().add("publishId", publishId);
    }


    public MapMessage analysisWorkbook(XSSFWorkbook workbook, boolean isGrouped){
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if(sheet == null){
            return MapMessage.errorMessage();
        }
        List<String> dataTitleList = new ArrayList<>();

        List<String> errorMessage = new ArrayList<>();
        // 获取标题行
        XSSFRow titleRow = sheet.getRow(0);
        if(titleRow == null){
            errorMessage.add("文件无数据");
            return MapMessage.errorMessage().add("errorList", errorMessage);
        }

        for(int colIndex = titleRow.getFirstCellNum(); colIndex < titleRow.getLastCellNum(); colIndex++){
            String cellValue = XssfUtils.getStringCellValue(titleRow.getCell(colIndex));
            if(StringUtils.isBlank(cellValue)){
                break;
            }
            dataTitleList.add(cellValue);
        }
        if(dataTitleList.size() < 3){
            errorMessage.add("数据列数有误");
            return MapMessage.errorMessage().add("errorList", errorMessage);
        }

        int rowNo = 1;
        int colCount = dataTitleList.size();
        boolean checkResult = true;
        List<AgentPublishData> dataList = new ArrayList<>();
        XSSFFormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        while(true){
            XSSFRow row = sheet.getRow(rowNo++);
            if(row == null){
                break;
            }

            String groupName = XssfUtils.getCellStringValue(row.getCell(0));
            // 如果第一列为空则结束
            if(StringUtils.isBlank(groupName)){
                break;
            }
            AgentGroup group = baseOrgService.getGroupByName(groupName);
            if(group == null){
                checkResult = false;
                errorMessage.add("第" + rowNo + "行，无此部门，请检查后重新上传");
                continue;
            }

            Long userId = null;
            String userName = XssfUtils.getCellStringValue(row.getCell(1));
            if(StringUtils.isNotBlank(userName)){
                AgentUser user = baseOrgService.getUserByRealName(userName).stream().findFirst().orElse(null);
                if(user == null || baseOrgService.getGroupUser(group.getId(), user.getId()) == null){
                    checkResult = false;
                    errorMessage.add("第" + rowNo + "行，姓名错误或不在此部门，请检查后重新上传");
                    continue;
                }
                userId = user.getId();
            }
            String category = XssfUtils.getCellStringValue(row.getCell(2));

            if(checkResult){
                AgentPublishData dataItem = new AgentPublishData();
                dataItem.setGroupId(group.getId());
                dataItem.setGroupName(groupName);
                if(userId != null){
                    dataItem.setUserId(userId);
                    dataItem.setUserName(userName);
                }
                dataItem.setCategory(category);
                List<Object> itemDataList = new ArrayList<>();
                itemDataList.add(groupName);
                itemDataList.add(SafeConverter.toString(userName, ""));
                itemDataList.add(SafeConverter.toString(category, ""));

                for(int i = 3; i < colCount; i++){
                    itemDataList.add(SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(i), evaluator), ""));
                }
                dataItem.setDataList(itemDataList);
                dataItem.setDisabled(false);
                dataList.add(dataItem);
            }
        }

        if(!checkResult){
            return MapMessage.errorMessage().add("errorList", errorMessage);
        }
        if(CollectionUtils.isEmpty(dataList)){
            errorMessage.add("文件无有效数据！");
            return MapMessage.errorMessage().add("errorList", errorMessage);
        }

        MapMessage message = MapMessage.successMessage();
        message.add("titleList", dataTitleList);
        message.add("dataList", dataList);
        return message;
    }


    // 保存数据
    public MapMessage saveData(String publishId, String title, Boolean allowViewSubordinateData, Boolean allowDownload, List<AgentRoleType> roleTypeList, List<Long> groupIds, String comment){
        AgentPublish publish = agentPublishDao.load(publishId);
        if(publish == null){
            return MapMessage.errorMessage("publishId无效!");
        }
        if(!Objects.equals(publish.getTitle(), title)){
            publish.setTitle(title);
        }
        AuthCurrentUser user = getCurrentUser();
        publish.setOperatorId(user.getUserId());
        publish.setOperatorName(user.getRealName());
        publish.setComment(comment);
        agentPublishDao.replace(publish);

        AgentPublishControl publishControl = agentPublishControlDao.loadByPublishId(publishId);
        if(publishControl == null){
            publishControl = new AgentPublishControl();
            publishControl.setPublishId(publishId);
            publishControl.setDisabled(false);
        }
        publishControl.setAllowViewSubordinateData(allowViewSubordinateData);
        publishControl.setAllowDownload(allowDownload);
        publishControl.setRoleTypeList(roleTypeList);
        publishControl.setGroupIdList(groupIds);
        agentPublishControlDao.upsert(publishControl);
        return MapMessage.successMessage();
    }

    /**
     * 获取publish列表数据
     * @return
     */
    public List<AgentPublish> getPublishList(Long userId){
        AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
        if(groupUser == null){
            return Collections.emptyList();
        }
        Long groupId = groupUser.getGroupId();
        AgentRoleType roleType = groupUser.getUserRoleType();

        List<AgentPublish> dataList = agentPublishDao.loadAll();
        //如果是全国总监展示全部数据, 其他角色根据权限进行过滤
        if (roleType != AgentRoleType.Country && CollectionUtils.isNotEmpty(dataList)){
            List<String> publishIds = dataList.stream().map(AgentPublish::getId).collect(Collectors.toList());
            Map<String, AgentPublishControl> agentPublishControlMap = agentPublishControlDao.loadByPublishIds(publishIds);
            List<String> targetPublishIds = agentPublishControlMap.values().stream()
                    .filter(p -> CollectionUtils.isNotEmpty(p.getRoleTypeList()) && p.getRoleTypeList().contains(roleType) && CollectionUtils.isNotEmpty(p.getGroupIdList()) && p.getGroupIdList().contains(groupId))
                    .map(AgentPublishControl::getPublishId).collect(Collectors.toList());
            dataList = dataList.stream().filter(p -> Objects.equals(p.getStatus(), AgentPublish.STATUS_ONLINE) && targetPublishIds.contains(p.getId())).collect(Collectors.toList());
        }
        dataList = dataList.stream().sorted(Comparator.comparing(AgentPublish::getCreateTime).reversed()).collect(Collectors.toList());
        return dataList;
    }


    /**
     * 更新publish数据状态
     * @param id
     * @param status
     */
    public void updatePublishStatus(String id,Integer status){
        AgentPublish agentPublish = agentPublishDao.load(id);
        if (null != agentPublish){
            AuthCurrentUser user = getCurrentUser();
            agentPublish.setOperatorId(user.getUserId());
            agentPublish.setOperatorName(user.getRealName());
            agentPublish.setStatus(status);
            agentPublishDao.replace(agentPublish);
        }
    }

    /**
     * 删除publish数据
     * @param id
     */
    public void deletePublish(String id){
        AgentPublish agentPublish = agentPublishDao.load(id);
        if (null != agentPublish){
            //删除对应下发设置数据
            agentPublishControlDao.deleteByPublishId(id);
            //删除对应明细数据
            agentPublishDataDao.deleteByPublishId(id);

            AuthCurrentUser user = getCurrentUser();
            agentPublish.setOperatorId(user.getUserId());
            agentPublish.setOperatorName(user.getRealName());
            agentPublish.setDisabled(true);
            agentPublishDao.replace(agentPublish);
        }
    }

    /**
     * 获取publish详情信息
     * @return
     */
    public Map<String,Object> getPublishDetail(String publishId){
        Map<String,Object> publishDetailMap = new HashMap<>();
        AgentPublish agentPublish = agentPublishDao.load(publishId);
        if (null != agentPublish){
            publishDetailMap.put("publishId",publishId);
            publishDetailMap.put("title",agentPublish.getTitle());
            publishDetailMap.put("updateTime",agentPublish.getUpdateTime());
            publishDetailMap.put("operatorName",agentPublish.getOperatorName());
            publishDetailMap.put("comment",agentPublish.getComment());
            AgentPublishControl agentPublishControl = agentPublishControlDao.loadByPublishId(publishId);
            if (null != agentPublishControl && null != agentPublishControl.getAllowDownload()){
                publishDetailMap.put("allowDownload",agentPublishControl.getAllowDownload());
            }else {
                publishDetailMap.put("allowDownload",false);
            }
        }
        return publishDetailMap;
    }


    /**
     * 获取publish表头标题数据
     * @param publishId
     * @return
     */
    public List<String> getPublishDataTitleList(String publishId){
        AgentPublish publish = agentPublishDao.load(publishId);
        return publish == null? new ArrayList<>() : publish.getDataTitleList();
    }

    /**
     * 获取publish明细信息
     * @param userId
     * @param publishId
     * @return
     */
    public List<AgentPublishData> getPublishDataList(Long userId, String publishId){
        AgentGroupUser groupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
        if(groupUser == null){
            return Collections.emptyList();
        }
        Long groupId = groupUser.getGroupId();
        AgentRoleType roleType = groupUser.getUserRoleType();

        List<AgentPublishData> publishDataList = new ArrayList<>();
        if(roleType == AgentRoleType.Country) {
            publishDataList = agentPublishDataDao.loadByPublishId(publishId);
        } else {
            AgentPublishControl publishControl = agentPublishControlDao.loadByPublishId(publishId);
            if(publishControl != null && CollectionUtils.isNotEmpty(publishControl.getRoleTypeList()) && publishControl.getRoleTypeList().contains(roleType)
                    && CollectionUtils.isNotEmpty(publishControl.getGroupIdList()) && publishControl.getGroupIdList().contains(groupId) ){

                publishDataList = agentPublishDataDao.loadByPublishId(publishId);

                boolean isGroupManager = baseOrgService.isGroupManager(userId, groupId);
                boolean isDataGrouped = SafeConverter.toBoolean(publishControl.getIsGrouped(), false);
                boolean allowViewSubordinate = SafeConverter.toBoolean(publishControl.getAllowViewSubordinateData(), false);

                String tmpCategory = "";
                if(isDataGrouped){
                    tmpCategory = publishDataList.stream().filter(p -> null != p && StringUtils.isNotBlank(p.getCategory()) && Objects.equals(p.getGroupId(), groupId) && Objects.equals(p.getUserId(), userId)).map(AgentPublishData::getCategory).findFirst().orElse("");
                }

                List<Long> subGroupIds = new ArrayList<>();
                if(isGroupManager && allowViewSubordinate){
                    subGroupIds.addAll(baseOrgService.getSubGroupList(groupId).stream().map(AgentGroup::getId).collect(Collectors.toList()));
                }

                String category = tmpCategory;
                publishDataList = publishDataList.stream()
                        .filter(p -> {
                            if(Objects.equals(p.getGroupId(), groupId)){  // 相同部门
                                if(isGroupManager){ // 当前用户是管理员的情况，显示部门数据和用户的个人数据，
                                    return (p.getUserId() == null || (allowViewSubordinate || Objects.equals(p.getUserId(), userId))) || (isDataGrouped && StringUtils.isNotBlank(category) && Objects.equals(p.getCategory(), category));
                                }else { // 不是管理员，显示自己的数据, 或者同组的数据
                                    return Objects.equals(p.getUserId(), userId) || (isDataGrouped && StringUtils.isNotBlank(category) && Objects.equals(p.getCategory(), category));
                                }
                            }else {
                                // 下属数据或者同分组的数据
                                return subGroupIds.contains(p.getGroupId()) || (isDataGrouped && StringUtils.isNotBlank(category) && Objects.equals(p.getCategory(), category));
                            }
                        })
                        .collect(Collectors.toList());

            }
        }

        return publishDataList;
    }
}
