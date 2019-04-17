package com.voxlearning.utopia.agent.service.budget;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.datareport.KpiBudgetReportData;
import com.voxlearning.utopia.agent.constants.AgentKpiType;
import com.voxlearning.utopia.agent.dao.mongo.AgentKpiBudgetDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentKpiBudgetRecordDao;
import com.voxlearning.utopia.agent.persist.entity.AgentKpiBudget;
import com.voxlearning.utopia.agent.persist.entity.AgentKpiBudgetRecord;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.view.AgentKpiBudgetItem;
import com.voxlearning.utopia.agent.view.AgentKpiBudgetView;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 *
 * @author song.wang
 * @date 2018/2/12
 */
@Named
public class AgentKpiBudgetService extends AbstractAgentService{

    @Inject
    private AgentKpiBudgetDao agentKpiBudgetDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentKpiBudgetRecordDao agentKpiBudgetRecordDao;

    public MapMessage importBudget(XSSFWorkbook workbook){
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if(sheet == null){
            return MapMessage.errorMessage();
        }

        List<AgentKpiBudget> dataList = new ArrayList<>();
        Map<String, AgentGroup> groupMap = new HashMap<>();
        Map<String, AgentUser> userMap = new HashMap<>();
        List<Integer> errorRowList = new ArrayList<>();
        boolean checkResult = true;
        int rowNo = 1;
        while(true){
            XSSFRow row = sheet.getRow(rowNo++);
            if(row == null){
                break;
            }

            Integer month = XssfUtils.getIntCellValue(row.getCell(0));
            String groupOrUserStr = XssfUtils.getStringCellValue(row.getCell(1));
            String businessUnitName = XssfUtils.getStringCellValue(row.getCell(2));  // 业务部名称
            String regionName = XssfUtils.getStringCellValue(row.getCell(3));     // 大区名称
            String areaName = XssfUtils.getStringCellValue(row.getCell(4));     // 区域名称
            String cityName = XssfUtils.getStringCellValue(row.getCell(5));     // 分区名称
            String userName = XssfUtils.getStringCellValue(row.getCell(6));     // 用户名
            String kpiType = XssfUtils.getStringCellValue(row.getCell(7));      // 考核指标
            Integer budget = SafeConverter.toInt(XssfUtils.getIntCellValue(row.getCell(8)));         // 指标对应的预算

            // 整行没有数据的情况下结束
            if(month == null && StringUtils.isBlank(groupOrUserStr) && StringUtils.isBlank(businessUnitName)
                    && StringUtils.isBlank(regionName) && StringUtils.isBlank(areaName)
                    && StringUtils.isBlank(cityName) && StringUtils.isBlank(userName)
                    && StringUtils.isBlank(kpiType)){
                break;
            }

            Date date = DateUtils.stringToDate(String.valueOf(month), "yyyyMM");
            if(month == null || month < 201801 || month > 202001 || date == null){          // 判断月份是否正确
                checkResult = false;
                errorRowList.add(rowNo);
            }else {
                if(!Objects.equals(groupOrUserStr, "部门") && !Objects.equals(groupOrUserStr, "用户")){   // 判断部门/用户 是否正确
                    checkResult = false;
                    errorRowList.add(rowNo);
                }else {
                    Integer groupOrUser = Objects.equals(groupOrUserStr, "部门")? AgentKpiBudget.GROUP_OR_USER_GROUP : AgentKpiBudget.GROUP_OR_USER_USER;
                    String groupName = fetchGroupName(new String[]{businessUnitName, regionName, areaName, cityName});
                    if(StringUtils.isBlank(groupName)){
                        checkResult = false;
                        errorRowList.add(rowNo);
                    }else {
                        // 判断部门是否存在
                        AgentGroup group = groupMap.get(groupName);
                        if(group == null){
                            group = baseOrgService.getGroupByName(groupName);
                            if(group != null){
                                groupMap.put(groupName, group);
                            }
                        }

                        if(group == null){
                            checkResult = false;
                            errorRowList.add(rowNo);
                        }else {

                            // 判断指标类型是否正确
                            if(AgentKpiType.descOf(kpiType) == null || !fetchKpiTypeList(group.getId()).contains(AgentKpiType.descOf(kpiType))){
                                checkResult = false;
                                errorRowList.add(rowNo);
                            }else {
                                // 判断预算是否大于 0
                                if(budget < 0){
                                    checkResult = false;
                                    errorRowList.add(rowNo);
                                }else {
                                    if(Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_USER)){
                                        // 判断用户是否存在
                                        AgentUser user = userMap.get(userName);
                                        if(user == null){
                                            user = baseOrgService.getUserByRealName(userName).stream().findFirst().orElse(null);
                                            if(user != null){
                                                userMap.put(userName, user);
                                            }
                                        }
                                        if(user == null){
                                            checkResult = false;
                                            errorRowList.add(rowNo);
                                        }else {
                                            AgentGroupUser groupUser = baseOrgService.getGroupUser(group.getId(), user.getId());
                                            if(groupUser == null){ // 判断部门和用户的关系
                                                checkResult = false;
                                                errorRowList.add(rowNo);
                                            }else {
                                                // 全部校验通过
                                                if (checkResult) {
                                                    AgentKpiBudget item = new AgentKpiBudget();
                                                    item.setMonth(month);
                                                    item.setGroupOrUser(groupOrUser);

                                                    item.setGroupId(group.getId());
                                                    item.setGroupName(group.getGroupName());
                                                    item.setGroupRoleType(group.fetchGroupRoleType());
                                                    item.setParentGroupId(group.getParentId());

                                                    item.setUserId(user.getId());
                                                    item.setUserName(user.getRealName());

                                                    item.setKpiType(AgentKpiType.descOf(kpiType));
                                                    item.setBudget(budget);
                                                    item.setConfirmed(judgeConfirmed(group.getId(), month));
                                                    item.setDisabled(false);
                                                    dataList.add(item);
                                                }
                                            }
                                        }
                                    }else {
                                        // 全部校验通过
                                        if(checkResult){
                                            AgentKpiBudget item = new AgentKpiBudget();
                                            item.setMonth(month);
                                            item.setGroupOrUser(groupOrUser);

                                            item.setGroupId(group.getId());
                                            item.setGroupName(group.getGroupName());
                                            item.setGroupRoleType(group.fetchGroupRoleType());
                                            item.setParentGroupId(group.getParentId());

                                            item.setKpiType(AgentKpiType.descOf(kpiType));
                                            item.setBudget(budget);
                                            item.setConfirmed(judgeConfirmed(group.getId(), month));
                                            item.setDisabled(false);
                                            dataList.add(item);
                                        }
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

        // excel无有效数据
        if(CollectionUtils.isEmpty(dataList)){
            return MapMessage.errorMessage("请导入有效的数据文件！");
        }

        int replaceCount = 0;
        int insertCount = 0;

        List<AgentKpiBudget> insertList = new ArrayList<>();
        Map<String, List<AgentKpiBudget>> dbKpiBudgetMap = new HashMap<>();
        for(AgentKpiBudget kpiBudget : dataList){
            String key = StringUtils.join(kpiBudget.getMonth(), "_", kpiBudget.getGroupOrUser(), "_", kpiBudget.getGroupId());
            List<AgentKpiBudget> dbDataList = dbKpiBudgetMap.get(key);
            if(dbDataList == null){
                if(Objects.equals(kpiBudget.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_GROUP)){
                    dbDataList = fetchGroupBudget(kpiBudget.getGroupId(), kpiBudget.getMonth());
                }else {
                    // 获取部门下所有用户的数据
                    List<AgentKpiBudget> tmpList = new ArrayList<>();
                    List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroup(kpiBudget.getGroupId());
                    if(CollectionUtils.isNotEmpty(groupUserList)){
                        groupUserList.forEach(p -> tmpList.addAll(fetchUserBudget(p.getUserId(), kpiBudget.getGroupId(), kpiBudget.getMonth())));
                    }
                    dbDataList = tmpList;
                }
                dbKpiBudgetMap.put(key, CollectionUtils.isEmpty(dbDataList) ? new ArrayList<>() : dbDataList);
            }

            List<AgentKpiBudget> dbKpiBudgetList = dbDataList.stream().filter(p -> p.getKpiType() == kpiBudget.getKpiType()
                    && (Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_GROUP) || (Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_USER) && Objects.equals(p.getUserId(), kpiBudget.getUserId()))))
                    .collect(Collectors.toList());

            if(CollectionUtils.isNotEmpty(dbKpiBudgetList)){ // 数据库已存在
                // 更新到第一条已存在的记录里面
                kpiBudget.setId(dbKpiBudgetList.get(0).getId());
                agentKpiBudgetDao.replace(kpiBudget);
                replaceCount++;
                // 删除多余的数据记录
                for(int i = 1; i < dbKpiBudgetList.size(); i++){
                    AgentKpiBudget tmpBudget = dbKpiBudgetList.get(i);
                    tmpBudget.setDisabled(true);
                    agentKpiBudgetDao.replace(tmpBudget);
                }
            }else {   // 数据库不存在
                insertList.add(kpiBudget);
            }
        }

        // 将insertList 去重后插入
        if(CollectionUtils.isNotEmpty(insertList)){
            // 过滤出部门预算
            Map<String, List<AgentKpiBudget>> groupBudgetListMap = insertList.stream()
                    .filter(p -> Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_GROUP))
                    .collect(Collectors.groupingBy(p -> StringUtils.join(p.getMonth(), "_", p.getGroupOrUser(), "_", p.getGroupId(), "_", p.getKpiType()), Collectors.toList()));
            List<AgentKpiBudget> groupBudgetList = groupBudgetListMap.values().stream().map(p -> p.get(0)).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(groupBudgetList)){
                agentKpiBudgetDao.inserts(groupBudgetList);
                insertCount += groupBudgetList.size();
            }

            // 过滤出User预算
            Map<String, List<AgentKpiBudget>> userBudgetListMap = insertList.stream()
                    .filter(p -> Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_USER))
                    .collect(Collectors.groupingBy(p -> StringUtils.join(p.getMonth(), "_", p.getGroupOrUser(), "_", p.getGroupId(), "_", p.getKpiType(), "_", p.getUserId()), Collectors.toList()));
            List<AgentKpiBudget> userBudgetList = userBudgetListMap.values().stream().map(p -> p.get(0)).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userBudgetList)){
                agentKpiBudgetDao.inserts(userBudgetList);
                insertCount += userBudgetList.size();
            }
        }
        MapMessage message = MapMessage.successMessage();
        message.put("replaceCount", replaceCount);
        message.put("insertCount", insertCount);
        return message;
    }

    // 判断部门指标的确认状态
    public boolean judgeConfirmed(Long groupId, Integer month){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return false;
        }
        if(group.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit){
            return true;
        }else {
            Long parentGroupId = fetchParentGroupIdUnderBU(groupId);
            if(parentGroupId == null){
                return false;
            }else {
                // groupId 可能与 parentGroupId 相等，也可能不相等
                // groupId == parentGroupId 判断该部门之前导入的指标的确认状态
                // groupId != parentGroupId 判断业务部下直接子部门的指标确认状态
                List<AgentKpiBudget> budgetList = fetchGroupBudget(parentGroupId, month);
                return budgetList.stream().anyMatch(AgentKpiBudget::getConfirmed);
            }
        }
    }

    // 获取指定部门在业务部直接子部门中的上级部门ID
    private Long fetchParentGroupIdUnderBU(Long groupId){
        AgentGroup parentGroup = baseOrgService.getParentGroup(groupId);
        if(parentGroup == null){
            return null;
        }
        if(parentGroup.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit){
            return groupId;
        }else {
            return fetchParentGroupIdUnderBU(parentGroup.getId());
        }
    }

    private String fetchGroupName(String[] nameList){
        if(nameList == null || nameList.length < 1){
            return null;
        }
        for(int i = nameList.length - 1; i > -1; i--){
            if(StringUtils.isNotBlank(nameList[i])){
                return nameList[i];
            }
        }
        return null;
    }

    public List<AgentKpiType> fetchKpiTypeList(Long groupId){
        List<AgentKpiType> kpiTypeList = new ArrayList<>();
        AgentGroup businessUnit = baseOrgService.getParentGroupByRole(groupId, AgentGroupRoleType.BusinessUnit);
        if(businessUnit != null ){
            List<AgentServiceType> serviceTypeList = businessUnit.fetchServiceTypeList();
            if(serviceTypeList.contains(AgentServiceType.JUNIOR_SCHOOL)){
                kpiTypeList.add(AgentKpiType.JUNIOR_ENG_ADD);
                kpiTypeList.add(AgentKpiType.JUNIOR_MATH_ADD);
                kpiTypeList.add(AgentKpiType.JUNIOR_CHN_ADD);
                kpiTypeList.add(AgentKpiType.JUNIOR_SGL_SUBJ_ADD);
                kpiTypeList.add(AgentKpiType.STU_PARENT_ACTIVE);
            }
            if(serviceTypeList.contains(AgentServiceType.MIDDLE_SCHOOL) || serviceTypeList.contains(AgentServiceType.SENIOR_SCHOOL)) {
                kpiTypeList.add(AgentKpiType.MIDDLE_ENG_ADD);
                kpiTypeList.add(AgentKpiType.MIDDLE_MATH_ADD);
                kpiTypeList.add(AgentKpiType.MIDDLE_ENG_BF);
                kpiTypeList.add(AgentKpiType.MIDDLE_SGL_SUBJ_ADD);
            }
        }
        return kpiTypeList;
    }

    // 根据部门ID 和 月份获取预算数据（包括该部门及部门下用户的数据）
    public List<AgentKpiBudget> fetchByGroupAndMonth(Long groupId, Integer month){
        List<AgentKpiBudget> result = new ArrayList<>();
        List<AgentKpiBudget> budgetList = agentKpiBudgetDao.loadByMonth(month);
        budgetList = budgetList.stream().filter(p -> Objects.equals(p.getGroupId(), groupId)).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(budgetList)){
            result.addAll(budgetList);
        }
        return result;
    }

    // 获取部门的预算
    public List<AgentKpiBudget> fetchGroupBudget(Long groupId, Integer month){
        List<AgentKpiBudget> result = new ArrayList<>();
        List<AgentKpiBudget> budgetList = fetchByGroupAndMonth(groupId, month);
        budgetList = budgetList.stream().filter(p -> Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_GROUP)).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(budgetList)){
            result.addAll(budgetList);
        }
        return result;
    }

    // 获取部门下用户的预算
    public Map<Long, List<AgentKpiBudget>> fetchGroupUserBudget(Long groupId, Integer month){
        Map<Long, List<AgentKpiBudget>> userBudgetListMap = new HashMap<>();

        List<AgentKpiBudget> budgetList = fetchByGroupAndMonth(groupId, month);
        budgetList = budgetList.stream().filter(p -> Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_USER)).collect(Collectors.toList());

        if(CollectionUtils.isNotEmpty(budgetList)){
            userBudgetListMap.putAll(budgetList.stream().collect(Collectors.groupingBy(AgentKpiBudget::getUserId, Collectors.toList())));
        }
        return userBudgetListMap;
    }

    // 获取指定部门，指定用户的预算
    public List<AgentKpiBudget> fetchUserBudget(Long userId, Long groupId, Integer month){
        List<AgentKpiBudget> result = new ArrayList<>();
        List<AgentKpiBudget> budgetList = fetchByGroupAndMonth(groupId, month);
        budgetList = budgetList.stream().filter(p -> Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_USER) && Objects.equals(p.getUserId(), userId)).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(budgetList)){
            result.addAll(budgetList);
        }
        return result;
    }

    // 获取子部门及子部门下用户的预算
    private List<AgentKpiBudget> fetchByParentGroupId(Long groupId, Integer month){
        List<AgentKpiBudget> result = new ArrayList<>();
        List<AgentKpiBudget> budgetList = agentKpiBudgetDao.loadByMonth(month);
        budgetList = budgetList.stream().filter(p -> Objects.equals(p.getParentGroupId(), groupId)).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(budgetList)){
            result.addAll(budgetList);
        }
        return result;
    }


    public List<AgentKpiBudgetView> generateGroupBudgetList(Collection<Long> groupList, Integer month){
        return groupList.stream().map(p -> generateGroupBudget(p, month)).collect(Collectors.toList());
    }


    // 生成部门的预算数据
    public AgentKpiBudgetView generateGroupBudget(Long groupId, Integer month){
        AgentKpiBudgetView result = new AgentKpiBudgetView();
        result.setMonth(month);
        result.setGroupOrUser(AgentKpiBudget.GROUP_OR_USER_GROUP);
        result.setGroupId(groupId);
        AgentGroup group = baseOrgService.getGroupById(groupId);
        result.setGroupName(group == null ? "" : group.getGroupName());
        result.setConfirmed(judgeConfirmed(groupId, month));

        List<AgentKpiType> kpiTypeList = fetchKpiTypeList(groupId);
        if(CollectionUtils.isNotEmpty(kpiTypeList)){
            List<AgentKpiBudget> budgetList = fetchGroupBudget(groupId, month);
            result.setKpiBudgetList(generateKpiBudgetMap(kpiTypeList, budgetList));
        }
        return result;
    }

    // 生成指标预算数据
    private List<AgentKpiBudgetItem> generateKpiBudgetMap(List<AgentKpiType> kpiTypeList, List<AgentKpiBudget> budgetList){
        List<AgentKpiBudgetItem> kpiBudgetItemList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(kpiTypeList)){
            Map<AgentKpiType, AgentKpiBudget> kpiTypeBudget = new HashMap<>();
            if(CollectionUtils.isNotEmpty(budgetList)){
                kpiTypeBudget.putAll(budgetList.stream().collect(Collectors.toMap(AgentKpiBudget::getKpiType, Function.identity(), (o1, o2) -> o1)));
            }

            kpiTypeList.forEach(p -> {
                AgentKpiBudgetItem kpiBudgetItem = new AgentKpiBudgetItem();
                kpiBudgetItem.setKpiType(p.getType());
                kpiBudgetItem.setKpiTypeDesc(p.getDesc());
                AgentKpiBudget budget = kpiTypeBudget.get(p);
                if(budget != null){
                    kpiBudgetItem.setBudget(SafeConverter.toInt(budget.getBudget()));
                    kpiBudgetItem.setConfirmed(SafeConverter.toBoolean(budget.getConfirmed()));
                }
                kpiBudgetItemList.add(kpiBudgetItem);
            });
        }
        return kpiBudgetItemList;
    }


    // 生成部门下面用户的预算数据列表
    public List<AgentKpiBudgetView> generateGroupUserBudgetList(Long groupId, Integer month){
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroup(groupId);
        Set<Long> userIds = groupUserList.stream().filter(p -> p.getUserRoleType() == AgentRoleType.BusinessDeveloper).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        if(CollectionUtils.isEmpty(userIds)){
            return Collections.emptyList();
        }
        Map<Long, List<AgentKpiBudget>> userBudgetListMap = new HashMap<>();
        List<AgentKpiType> kpiTypeList = fetchKpiTypeList(groupId);
        if(CollectionUtils.isNotEmpty(kpiTypeList)){
            userBudgetListMap.putAll(fetchGroupUserBudget(groupId, month));
        }
        return userIds.stream().map(p -> generateUserBudget(p, groupId, month, userBudgetListMap.get(p))).collect(Collectors.toList());
    }

    public AgentKpiBudgetView generateUserBudget(Long userId, Long groupId, Integer month){
        List<AgentKpiType> kpiTypeList = fetchKpiTypeList(groupId);
        List<AgentKpiBudget> userBudgetList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(kpiTypeList)){
            userBudgetList.addAll(fetchUserBudget(userId, groupId, month));
        }
        return generateUserBudget(userId, groupId, month, userBudgetList);
    }




    // 生成用户预算数据
    private AgentKpiBudgetView generateUserBudget(Long userId, Long groupId, Integer month, List<AgentKpiBudget> budgetList){
        AgentKpiBudgetView result = new AgentKpiBudgetView();
        result.setMonth(month);
        result.setGroupOrUser(AgentKpiBudget.GROUP_OR_USER_USER);

        result.setGroupId(groupId);
        AgentGroup group = baseOrgService.getGroupById(groupId);
        result.setGroupName(group == null ? "" : group.getGroupName());

        result.setUserId(userId);
        AgentUser user = baseOrgService.getUser(userId);
        result.setUserName(user == null ? "" : user.getRealName());
        result.setConfirmed(judgeConfirmed(groupId, month));
        List<AgentKpiType> kpiTypeList = fetchKpiTypeList(groupId);
        if(CollectionUtils.isNotEmpty(kpiTypeList)){
            result.setKpiBudgetList(generateKpiBudgetMap(kpiTypeList, budgetList));
        }
        return result;
    }


    public MapMessage updateGroupBudget(Long groupId, Integer month, Map<AgentKpiType, Integer> kpiBudgetMap, String comment){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return MapMessage.errorMessage("部门不存在");
        }
        List<AgentKpiBudget> budgetList = fetchGroupBudget(groupId, month);
        Map<AgentKpiType, List<AgentKpiBudget>> dbKpiBudgetListMap = budgetList.stream().collect(Collectors.groupingBy(AgentKpiBudget::getKpiType, Collectors.toList()));
        boolean confirmed = judgeConfirmed(groupId, month);
        kpiBudgetMap.forEach((k, v) -> {
            List<AgentKpiBudget> dbBudgetList = dbKpiBudgetListMap.get(k);
            if(CollectionUtils.isNotEmpty(dbBudgetList)){
                // 数据库存在该指标，并且预算有修改
                AgentKpiBudget dbBudget = dbBudgetList.get(0);
                if(!Objects.equals(dbBudget.getBudget(), v)){
                    Integer beforeChange = dbBudget.getBudget();
                    dbBudget.setBudget(v);
                    agentKpiBudgetDao.replace(dbBudget);
                    saveKpiBudgetRecord(dbBudget.getId(), k, beforeChange, v, comment);
                }
                // 删除多余的数据记录
                for(int i = 1; i < dbBudgetList.size(); i++){
                    AgentKpiBudget tmpBudget = dbBudgetList.get(i);
                    tmpBudget.setDisabled(true);
                    agentKpiBudgetDao.replace(tmpBudget);
                }
            }else {
                AgentKpiBudget item = new AgentKpiBudget();
                item.setMonth(month);
                item.setGroupOrUser(AgentKpiBudget.GROUP_OR_USER_GROUP);

                item.setGroupId(group.getId());
                item.setGroupName(group.getGroupName());
                item.setGroupRoleType(group.fetchGroupRoleType());
                item.setParentGroupId(group.getParentId());

                item.setKpiType(k);
                item.setBudget(v);
                item.setConfirmed(confirmed);      // 只要该有一个指标确认过，则设置为确认状态
                item.setDisabled(false);
                agentKpiBudgetDao.insert(item);
            }
        });

        return MapMessage.successMessage();
    }

    private void saveKpiBudgetRecord(String kpiBudgetId, AgentKpiType kpiType, Integer beforeChange, Integer afterChange, String comment){
        AgentKpiBudgetRecord record = new AgentKpiBudgetRecord();
        record.setKpiBudgetId(kpiBudgetId);
        record.setKpiType(kpiType);
        record.setBeforeChange(beforeChange);
        record.setAfterChange(afterChange);
        record.setComment(comment);
        AuthCurrentUser user = getCurrentUser();
        record.setOperatorId(user.getUserId());
        record.setOperatorName(user.getRealName());
        agentKpiBudgetRecordDao.insert(record);
    }


    public MapMessage updateUserBudget(Long userId, Long groupId, Integer month, Map<AgentKpiType, Integer> kpiBudgetMap, String comment){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return MapMessage.errorMessage("部门不存在");
        }

        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return MapMessage.errorMessage("用户不存在");
        }

        List<AgentKpiBudget> budgetList = fetchUserBudget(userId, groupId, month);
        Map<AgentKpiType, List<AgentKpiBudget>> dbKpiBudgetListMap = budgetList.stream().collect(Collectors.groupingBy(AgentKpiBudget::getKpiType, Collectors.toList()));
        boolean confirmed = judgeConfirmed(groupId, month);
        kpiBudgetMap.forEach((k, v) -> {
            List<AgentKpiBudget> dbBudgetList = dbKpiBudgetListMap.get(k);
            if(CollectionUtils.isNotEmpty(dbBudgetList)){
                // 数据库存在该指标，并且预算有修改
                AgentKpiBudget dbBudget = dbBudgetList.get(0);
                if(!Objects.equals(dbBudget.getBudget(), v)){
                    Integer beforeChange = dbBudget.getBudget();
                    dbBudget.setBudget(v);
                    agentKpiBudgetDao.replace(dbBudget);
                    saveKpiBudgetRecord(dbBudget.getId(), k, beforeChange, v, comment);
                }
                // 删除多余的数据记录
                for(int i = 1; i < dbBudgetList.size(); i++){
                    AgentKpiBudget tmpBudget = dbBudgetList.get(i);
                    tmpBudget.setDisabled(true);
                    agentKpiBudgetDao.replace(tmpBudget);
                }
            }else {
                AgentKpiBudget item = new AgentKpiBudget();
                item.setMonth(month);
                item.setGroupOrUser(AgentKpiBudget.GROUP_OR_USER_USER);

                item.setGroupId(group.getId());
                item.setGroupName(group.getGroupName());
                item.setGroupRoleType(group.fetchGroupRoleType());
                item.setParentGroupId(group.getParentId());

                item.setUserId(user.getId());
                item.setUserName(user.getRealName());

                item.setKpiType(k);
                item.setBudget(v);
                item.setConfirmed(confirmed);      // 只要该有一个指标确认过，则设置为确认状态
                item.setDisabled(false);
                agentKpiBudgetDao.insert(item);
            }
        });

        return MapMessage.successMessage();
    }

    public MapMessage confirmBudget(Long groupId, Integer month){
        List<AgentKpiBudget> currentGroupKpiBudgetList = fetchByGroupAndMonth(groupId, month);
        if(CollectionUtils.isEmpty(currentGroupKpiBudgetList)){
            return MapMessage.errorMessage("当前部门没有预算！");
        }
        List<AgentGroup> groupList = new ArrayList<>();
        groupList.addAll(baseOrgService.getSubGroupList(groupId));
        groupList.add(baseOrgService.getGroupById(groupId));
        groupList.forEach(p -> {
            List<AgentKpiBudget> kpiBudgetList = fetchByGroupAndMonth(p.getId(), month);
            kpiBudgetList = kpiBudgetList.stream().filter(k -> !k.getConfirmed()).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(kpiBudgetList)){
                kpiBudgetList.forEach(t -> {
                    t.setConfirmed(true);
                    agentKpiBudgetDao.replace(t);
                });
            }
        });
        return MapMessage.successMessage();
    }

    public List<KpiBudgetReportData> generateReportData(Long groupId, Integer month){
        List<KpiBudgetReportData> resultList = new ArrayList<>();

        List<AgentKpiBudget> allKpiBudget = agentKpiBudgetDao.loadByMonth(month);
        Map<Long, AgentKpiBudget> groupInfoMap = allKpiBudget.stream().filter(p -> Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_GROUP)).collect(Collectors.toMap(AgentKpiBudget::getGroupId, Function.identity(), (o1, o2) -> o1));

        // 生成当前部门及人员的预算数据
        resultList.addAll(generateGroupAndUserReportData(groupId, month, groupInfoMap));
        // 生成子部门及子部门人员的预算数据
        resultList.addAll(generateSubGroupAndUserReportData(groupId, month, groupInfoMap));
        return resultList;
    }


    // groupInfoMap缓存历史部门信息
    public List<KpiBudgetReportData> generateSubGroupAndUserReportData(Long groupId, Integer month, Map<Long, AgentKpiBudget> groupInfoMap){

        List<KpiBudgetReportData> resultList = new ArrayList<>();
        List<AgentKpiBudget> kpiBudgetList = fetchByParentGroupId(groupId, month);
        if(CollectionUtils.isNotEmpty(kpiBudgetList)){

            resultList.addAll(generateGroupAndUserReportData(kpiBudgetList, groupInfoMap));

            // 部门下子部门的指标预算数据
            Set<Long> groupIdList = kpiBudgetList.stream().filter(p -> Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_GROUP)).map(AgentKpiBudget::getGroupId).collect(Collectors.toSet());
            if(CollectionUtils.isNotEmpty(groupIdList)){
                // 对每个子部门进行递归
                for(Long subGroupId : groupIdList){
                    resultList.addAll(generateSubGroupAndUserReportData(subGroupId, month, groupInfoMap));
                }
            }
        }
        return resultList;
    }

    private List<KpiBudgetReportData> generateGroupAndUserReportData(Long groupId, Integer month, Map<Long, AgentKpiBudget> groupInfoMap){
        List<KpiBudgetReportData> resultList = new ArrayList<>();
        List<AgentKpiBudget> kpiBudgetList = fetchByGroupAndMonth(groupId, month);
        if(CollectionUtils.isNotEmpty(kpiBudgetList)){
            resultList.addAll(generateGroupAndUserReportData(kpiBudgetList, groupInfoMap));
        }

        return resultList;
    }

    private List<KpiBudgetReportData> generateGroupAndUserReportData(List<AgentKpiBudget> kpiBudgetList, Map<Long, AgentKpiBudget> groupInfoMap){
        List<KpiBudgetReportData> resultList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(kpiBudgetList)){

            // 部门下人员的指标预算数据
            List<AgentKpiBudget> userKpiBudgetList = kpiBudgetList.stream().filter(p -> Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_USER)).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userKpiBudgetList)){
                // 根据userId分组
                Map<Long, List<AgentKpiBudget>> userBudgetMap = userKpiBudgetList.stream().collect(Collectors.groupingBy(AgentKpiBudget::getUserId, Collectors.toList()));
                userBudgetMap.values().forEach(p -> resultList.addAll(p.stream().map(t -> convertToReportData(t, groupInfoMap)).collect(Collectors.toList())));
            }

            // 部门下子部门的指标预算数据
            List<AgentKpiBudget> groupKpiBudgetList = kpiBudgetList.stream().filter(p -> Objects.equals(p.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_GROUP)).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(groupKpiBudgetList)){
                // 根据groupId分组
                Map<Long, List<AgentKpiBudget>> groupBudgetMap = groupKpiBudgetList.stream().collect(Collectors.groupingBy(AgentKpiBudget::getGroupId, Collectors.toList()));
                groupBudgetMap.values().forEach(p -> resultList.addAll(p.stream().map(t -> convertToReportData(t, groupInfoMap)).collect(Collectors.toList())));
            }
        }

        return resultList;
    }

    private KpiBudgetReportData convertToReportData(AgentKpiBudget kpiBudget, Map<Long, AgentKpiBudget> groupInfoMap){
        KpiBudgetReportData reportData = new KpiBudgetReportData();
        reportData.setMonth(kpiBudget.getMonth());
        reportData.setGroupOrUser(kpiBudget.getGroupOrUser());

        Long groupId = kpiBudget.getParentGroupId();

        if(kpiBudget.getGroupRoleType() == AgentGroupRoleType.BusinessUnit){
            reportData.setBusinessUnitName(kpiBudget.getGroupName());
        }else if(kpiBudget.getGroupRoleType() == AgentGroupRoleType.Region){
            reportData.setBusinessUnitName(getParentGroupNameByRole(groupId, AgentGroupRoleType.BusinessUnit, groupInfoMap));
            reportData.setRegionName(kpiBudget.getGroupName());
        }else if(kpiBudget.getGroupRoleType() == AgentGroupRoleType.Area){
            reportData.setBusinessUnitName(getParentGroupNameByRole(groupId, AgentGroupRoleType.BusinessUnit, groupInfoMap));
            reportData.setRegionName(getParentGroupNameByRole(groupId, AgentGroupRoleType.Region, groupInfoMap));
            reportData.setAreaName(kpiBudget.getGroupName());
        }else if(kpiBudget.getGroupRoleType() == AgentGroupRoleType.City){
            reportData.setBusinessUnitName(getParentGroupNameByRole(groupId, AgentGroupRoleType.BusinessUnit, groupInfoMap));
            reportData.setRegionName(getParentGroupNameByRole(groupId, AgentGroupRoleType.Region, groupInfoMap));
            reportData.setAreaName(getParentGroupNameByRole(groupId, AgentGroupRoleType.Area, groupInfoMap));
            reportData.setCityName(kpiBudget.getGroupName());
        }

        if(Objects.equals(kpiBudget.getGroupOrUser(), AgentKpiBudget.GROUP_OR_USER_USER)){
            reportData.setUserName(kpiBudget.getUserName());
        }

        reportData.setKpiType(kpiBudget.getKpiType().getDesc());
        reportData.setBudget(kpiBudget.getBudget());
        return reportData;
    }

    private String getParentGroupNameByRole(Long groupId, AgentGroupRoleType roleType, Map<Long, AgentKpiBudget> groupInfoMap){
        if(MapUtils.isEmpty(groupInfoMap) || !groupInfoMap.containsKey(groupId)){
            return "";
        }

        AgentKpiBudget kpiBudget = groupInfoMap.get(groupId);
        if(kpiBudget == null || kpiBudget.getGroupRoleType() == null){
            return "";
        }

        if(roleType == kpiBudget.getGroupRoleType()){
            return kpiBudget.getGroupName();
        }else {
            return getParentGroupNameByRole(kpiBudget.getParentGroupId(), roleType, groupInfoMap);
        }
    }

    public List<AgentKpiBudgetRecord> generateBudgetRecordList(Integer month, Integer groupOrUser, Long groupId, Long userId){

        List<String> kpiBudgetIds = new ArrayList<>();
        if(Objects.equals(groupOrUser, AgentKpiBudget.GROUP_OR_USER_GROUP)){
            kpiBudgetIds.addAll(fetchGroupBudget(groupId, month).stream().map(AgentKpiBudget::getId).collect(Collectors.toList()));
        }else {
            kpiBudgetIds.addAll(fetchUserBudget(userId, groupId, month).stream().map(AgentKpiBudget::getId).collect(Collectors.toList()));
        }
        if(CollectionUtils.isEmpty(kpiBudgetIds)){
            return Collections.emptyList();

        }
        Map<String, List<AgentKpiBudgetRecord>> kpiBudgetRecordMap = agentKpiBudgetRecordDao.loadByKpiBudgetIds(kpiBudgetIds);
        List<AgentKpiBudgetRecord> list = kpiBudgetRecordMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
        Collections.sort(list, (o1, o2) -> {
            if(o1.getCreateTime().before(o2.getCreateTime())){
                return 1;
            }else {
                return -1;
            }
        });
        return list;
    }


    // 删除部门及部门下用户的预算数据
    public void disableGroupBudget(Long groupId, Integer month){
        List<AgentKpiBudget> kpiBudgetList = this.fetchByGroupAndMonth(groupId, month);
        disableBudgetList(kpiBudgetList);
    }

    public void disableUserBudget(Long userId, Long groupId, Integer month){
        List<AgentKpiBudget> kpiBudgetList = this.fetchUserBudget(userId, groupId, month);
        disableBudgetList(kpiBudgetList);
    }

    private void disableBudgetList(List<AgentKpiBudget> kpiBudgetList){
        if(CollectionUtils.isNotEmpty(kpiBudgetList)){
            kpiBudgetList.forEach(t -> {
                t.setDisabled(true);
                agentKpiBudgetDao.replace(t);
            });
        }
    }




}
