package com.voxlearning.utopia.agent.service.sysconfig;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtilsBean2;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.constants.PerformanceGroupType;
import com.voxlearning.utopia.agent.dao.mongo.AgentPerformanceGroupDao;
import com.voxlearning.utopia.agent.persist.entity.AgentPerformanceGroup;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.view.AgentPerformanceGroupView;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentPerformanceGroupService
 *
 * @author song.wang
 * @date 2018/2/9
 */
@Named
public class AgentPerformanceGroupService {

    @Inject
    private AgentPerformanceGroupDao agentPerformanceGroupDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject
    private BaseExcelService baseExcelService;

    public MapMessage importPerformanceGroupData(XSSFWorkbook workbook){
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if(sheet == null){
            return MapMessage.errorMessage();
        }

        Map<Long, AgentGroupUser> userGroupUserMap = new HashMap<>();
        List<AgentGroupUser> agentGroupUsers = agentGroupUserLoaderClient.findAll();
        if(CollectionUtils.isNotEmpty(agentGroupUsers)){
            userGroupUserMap.putAll(agentGroupUsers.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, Function.identity(), (o1, o2) -> o1)));
        }

        List<AgentPerformanceGroup> performanceGroupList = new ArrayList<>();
        Set<Integer> monthSet = new HashSet<>();
        List<Integer> errorRowList = new ArrayList<>();
        boolean checkResult = true;
        int rowNo = 1;
        while (true){
            XSSFRow row = sheet.getRow(rowNo++);
            if(row == null){
                break;
            }

            Integer month = XssfUtils.getIntCellValue(row.getCell(0));
            String userName = XssfUtils.getStringCellValue(row.getCell(1));
            String groupName = XssfUtils.getStringCellValue(row.getCell(2));
            String performanceGroupType = XssfUtils.getStringCellValue(row.getCell(3));
            // 整行没有数据的情况下结束
            if(month == null && StringUtils.isBlank(userName) && StringUtils.isBlank(groupName) && StringUtils.isBlank(performanceGroupType)){
                break;
            }

            if(month == null || StringUtils.isBlank(userName) || StringUtils.isBlank(groupName)){
                checkResult = false;
                errorRowList.add(rowNo);
            }else {
                Date monthDate = DateUtils.stringToDate(String.valueOf(month), "yyyyMM");
                if(month < 201801 || month > 202001 || monthDate == null){   // 判断月份
                    checkResult = false;
                    errorRowList.add(rowNo);
                }else {
                    AgentUser user = baseOrgService.getUserByRealName(userName).stream().findFirst().orElse(null);
                    if(user == null){  // 判断是否存在该用户
                        checkResult = false;
                        errorRowList.add(rowNo);
                    }else {
                        AgentGroupUser groupUser = userGroupUserMap.get(user.getId());
                        if(groupUser == null || groupUser.getUserRoleType() != AgentRoleType.BusinessDeveloper){ // 判断是否是专员
                            checkResult = false;
                            errorRowList.add(rowNo);
                        }else {
                            AgentGroup group = baseOrgService.getGroupById(groupUser.getGroupId());
                            if(group == null || !Objects.equals(group.getGroupName(), groupName)){  // 判断专员和部门的关系
                                checkResult = false;
                                errorRowList.add(rowNo);
                            }else {
                                if(StringUtils.isNotBlank(performanceGroupType) && PerformanceGroupType.descOf(performanceGroupType) == null){ // 判断分组名称是否正确
                                    checkResult = false;
                                    errorRowList.add(rowNo);
                                }else {
                                    if(checkResult){
                                        monthSet.add(month);
                                        AgentPerformanceGroup item = new AgentPerformanceGroup();
                                        item.setMonth(month);
                                        item.setUserId(user.getId());
                                        item.setUserName(userName);
                                        item.setGroupId(group.getId());
                                        item.setGroupName(group.getGroupName());
                                        item.setPerformanceGroupType(PerformanceGroupType.descOf(performanceGroupType));
                                        item.setDisabled(false);
                                        performanceGroupList.add(item);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 数据有误的情形， 返回有误的数据信息
        if(!checkResult){
            List<String> errorList = errorRowList.stream().map(errorRow -> "第" + errorRow + "行数据有误！").collect(Collectors.toList());
            return MapMessage.errorMessage().add("errorList", errorList);
        }

        Map<Integer, List<AgentPerformanceGroup>> monthPerformanceGroupMap = new HashMap<>();
        monthSet.forEach(p -> monthPerformanceGroupMap.put(p, agentPerformanceGroupDao.findByMonth(p)));

        // 根据月份和UserId去除重复数据
        Map<String, List<AgentPerformanceGroup>> tmpMap = performanceGroupList.stream().collect(Collectors.groupingBy(p -> p.getMonth() + "_" + p.getUserId(), Collectors.toList()));
        List<AgentPerformanceGroup> insertList = new ArrayList<>();
        tmpMap.values().forEach(k -> {
            AgentPerformanceGroup p = k.get(0);
            List<AgentPerformanceGroup> monthDataList = monthPerformanceGroupMap.get(p.getMonth());
            if(CollectionUtils.isNotEmpty(monthDataList)){
                AgentPerformanceGroup existItem = monthDataList.stream().filter(t -> Objects.equals(p.getUserId(), t.getUserId())).findFirst().orElse(null);
                if(existItem == null){ // 分组不为空时插入
                    if(p.getPerformanceGroupType() != null){
                        insertList.add(p);
                    }
                }else {
                    // 如果导入的分组为空，则删除数据
                    if(p.getPerformanceGroupType() != null){
                        existItem.setUserName(p.getUserName());
                        existItem.setGroupId(p.getGroupId());
                        existItem.setGroupName(p.getGroupName());
                        existItem.setPerformanceGroupType(p.getPerformanceGroupType());
                    }else {
                        existItem.setDisabled(true);
                    }
                    agentPerformanceGroupDao.replace(existItem);
                }
            }else {
                if(p.getPerformanceGroupType() != null) { // 分组不为空时插入
                    insertList.add(p);
                }
            }
        });

        if(CollectionUtils.isNotEmpty(insertList)){
            agentPerformanceGroupDao.inserts(insertList);
        }
        return MapMessage.successMessage();
    }

    public void generateWorkbookData(XSSFWorkbook workbook, List<AgentPerformanceGroupView> performanceGroupList){
        if(workbook == null || CollectionUtils.isEmpty(performanceGroupList)){
            return;
        }

        XSSFSheet sheet = workbook.getSheetAt(0);
        CellStyle cellStyle = baseExcelService.createCellStyle(workbook);
        int index = 1;
        for (AgentPerformanceGroupView item : performanceGroupList) {
            XSSFRow row = sheet.createRow(index++);
            XssfUtils.setCellValue(row, 0, cellStyle, item.getMonth());
            XssfUtils.setCellValue(row, 1, cellStyle, item.getUserName());
            XssfUtils.setCellValue(row, 2, cellStyle, item.getGroupName());
            XssfUtils.setCellValue(row, 3, cellStyle, item.getPerformanceGroupType() == null ? "" : item.getPerformanceGroupType().getDesc());
        }

    }

    public AgentPerformanceGroup fetchByMonthAndUser(Integer month, Long userId){
        List<AgentPerformanceGroup> performanceGroupList = agentPerformanceGroupDao.findByMonth(month);
        return performanceGroupList.stream().filter(p -> Objects.equals(p.getUserId(), userId)).findFirst().orElse(null);
    }

    public List<AgentPerformanceGroupView> generateAllUserGroupData(Integer month){
        List<AgentPerformanceGroupView> resultList = new ArrayList<>();

        Set<Long> userIds = baseOrgService.findAllGroupUserIds(AgentRoleType.BusinessDeveloper);
        List<AgentPerformanceGroup> performanceGroupList = agentPerformanceGroupDao.findByMonth(month);
        Set<Long> existIds = new HashSet<>();
        for (AgentPerformanceGroup performanceGroup : performanceGroupList) {
            AgentPerformanceGroupView view = convertToPerformanceGroupView(performanceGroup);
            if(view != null){
                resultList.add(view);
            }
            existIds.add(performanceGroup.getUserId());
        }

        Set<Long> unExistUserIds = userIds.stream().filter(p -> !existIds.contains(p)).collect(Collectors.toSet());
        unExistUserIds.forEach(p -> {
            AgentPerformanceGroupView view = new AgentPerformanceGroupView();
            AgentUser user = baseOrgService.getUser(p);
            AgentGroup group = baseOrgService.getUserGroupsFirstOne(p, AgentRoleType.BusinessDeveloper);
            if(user == null || group == null){
                return;
            }
            view.setMonth(month);
            view.setUserId(user.getId());
            view.setUserName(user.getRealName());
            view.setGroupId(group.getId());
            view.setGroupName(group.getGroupName());
            resultList.add(view);
        });
        return resultList;
    }

    private AgentPerformanceGroupView convertToPerformanceGroupView(AgentPerformanceGroup performanceGroup){
        if(performanceGroup == null){
            return null;
        }
        AgentPerformanceGroupView view = new AgentPerformanceGroupView();
        view.setMonth(performanceGroup.getMonth());
        view.setUserId(performanceGroup.getUserId());
        view.setUserName(performanceGroup.getUserName());
        view.setGroupId(performanceGroup.getGroupId());
        view.setGroupName(performanceGroup.getGroupName());
        view.setPerformanceGroupType(performanceGroup.getPerformanceGroupType());
        return view;
    }

    public AgentPerformanceGroupView generateUserPerformanceGroupView(Integer month, Long userId){
        AgentPerformanceGroup performanceGroup = fetchByMonthAndUser(month, userId);
        if(performanceGroup != null){
            return convertToPerformanceGroupView(performanceGroup);
        }else {
            AgentPerformanceGroupView view = new AgentPerformanceGroupView();
            AgentUser user = baseOrgService.getUser(userId);
            AgentGroup group = baseOrgService.getUserGroupsFirstOne(userId, AgentRoleType.BusinessDeveloper);
            if(user == null || group == null){
                return null;
            }
            view.setMonth(month);
            view.setUserId(user.getId());
            view.setUserName(user.getRealName());
            view.setGroupId(group.getId());
            view.setGroupName(group.getGroupName());
            return view;
        }
    }

    public MapMessage upsertPerformanceGroup(Integer month, Long userId, PerformanceGroupType groupType){
        AgentPerformanceGroup performanceGroup = fetchByMonthAndUser(month, userId);
        if(performanceGroup != null){
            if(groupType != null){
                performanceGroup.setPerformanceGroupType(groupType);
            }else {
                performanceGroup.setDisabled(true);
            }
            agentPerformanceGroupDao.replace(performanceGroup);
        }else {
            if(groupType == null){
                return MapMessage.successMessage();
            }
            performanceGroup = new AgentPerformanceGroup();
            AgentUser user = baseOrgService.getUser(userId);
            AgentGroup group = baseOrgService.getUserGroupsFirstOne(userId, AgentRoleType.BusinessDeveloper);
            if(user == null || group == null){
                return MapMessage.errorMessage("用户不存在或用户角色有误");
            }
            performanceGroup.setMonth(month);
            performanceGroup.setUserId(user.getId());
            performanceGroup.setUserName(user.getRealName());
            performanceGroup.setGroupId(group.getId());
            performanceGroup.setGroupName(group.getGroupName());
            performanceGroup.setPerformanceGroupType(groupType);
            performanceGroup.setDisabled(false);
            agentPerformanceGroupDao.insert(performanceGroup);
        }
        return MapMessage.successMessage();
    }


}
