package com.voxlearning.utopia.agent.service.sysconfig;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.bean.AgentPaymentsData;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.dao.AgentPerformanceConfigDao;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseGroupService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.entity.agent.AgentPerformanceConfig;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agent 结算指标
 * Created by Administrator on 2016/9/23.
 */
@Named
public class AgentPerformanceConfigService extends AbstractAgentService {
    private static final String CACHE_KEY_DATA_UPDATE_TIME = "AgentPaymentsService:DATA_UPDATE_TIME:";
    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private AgentPerformanceConfigDao agentPerformanceConfigDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private BaseGroupService baseGroupService;
    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;

    private List<AgentPerformanceConfig> allData;
    private Map<String, AgentPerformanceConfig> payData;
    private Map<String, AgentPerformanceConfig> userMonthData;
    private Map<Integer, List<AgentPerformanceConfig>> monthData;
    private Map<Integer, List<AgentPerformanceConfig>> typeData;
    private long latestDataLoadTime = 0L;

    private List<AgentPerformanceConfig> loadAllAgentPayments() {
        loadDataIfNecessary();
        return allData;
    }

    private synchronized void loadDataIfNecessary() {
        // 本地JVM缓存处理，通过CACHE_KEY_DATA_UPDATE_TIME在数据发生变化的时候重新加载数据
       /* Long dataUpdateTime = agentCacheSystem.CBS.flushable.load(CACHE_KEY_DATA_UPDATE_TIME);
        if (CollectionUtils.isEmpty(allData) || (dataUpdateTime != null && latestDataLoadTime < dataUpdateTime)) {*/
        latestDataLoadTime = System.currentTimeMillis();
        allData = agentPerformanceConfigDao.findAllDictSchool();
        payData = allData.stream().collect(Collectors.toMap(AgentPerformanceConfig::getId, Function.identity()));
        userMonthData = allData.stream().collect(Collectors.toMap(p -> SafeConverter.toString(p.getUserId() + "_" + p.getSettlementMonth()), t -> t, (o1, o2) -> o2));
        monthData = allData.stream().collect(Collectors.groupingBy(AgentPerformanceConfig::getSettlementMonth, Collectors.toList()));
        typeData = allData.stream().collect(Collectors.groupingBy(AgentPerformanceConfig::getPaymentsType, Collectors.toList()));
       // }
    }

    private List<AgentPerformanceConfig> loadAgentPayments(Integer type) {
        loadDataIfNecessary();
        if (typeData.containsKey(type)) {
            return typeData.get(type);
        }
        return Collections.emptyList();
    }

    public List<AgentPaymentsData> loadAgentPaymentsDataByType(Integer type) {
        return burildAgentPaymentsData(loadAgentPayments(type));
    }

    public List<AgentPaymentsData> loadAllAgentPaymentsData() {
        return burildAgentPaymentsData(loadAllAgentPayments());
    }

    private List<AgentPaymentsData> burildAgentPaymentsData(List<AgentPerformanceConfig> agentPayments) {
        if (CollectionUtils.isEmpty(agentPayments)) {
            return Collections.emptyList();
        }
        Set<Long> userIds = agentPayments.stream().map(AgentPerformanceConfig::getUserId).collect(Collectors.toSet());
        List<AgentUser> users = baseOrgService.getUsers(userIds);
        Map<Long, AgentUser> userMap = users.stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
        Map<Long, List<AgentGroup>> userGroupInfos = loadUserGroupInfo(userIds);
        List<AgentPaymentsData> result = new ArrayList<>();
        agentPayments.forEach(p -> {
            AgentPaymentsData data = new AgentPaymentsData();
            Long userId = p.getUserId();
            if (!userMap.containsKey(userId)) {
                return;
            }
            List<AgentGroup> groupList = userGroupInfos.get(userId);
            if (CollectionUtils.isEmpty(groupList)) {
                data.setDepartmentName("");
            } else {
                data.setDepartmentName(StringUtils.join(groupList.stream().map(AgentGroup::getGroupName).collect(Collectors.toList()), ","));
            }
            AgentUser user = userMap.get(userId);
            data.setId(p.getId());
            data.setUserName(user.getRealName());
            data.setAccount(user.getAccountName());
            data.setRole(AgentRoleType.of(p.getIdentity()) == null ? "" : AgentRoleType.of(p.getIdentity()).getRoleName());
            data.setSettlementMonth(p.getSettlementMonth());
            data.setCityJuniorMeet(p.getCityJuniorMeet());
            data.setCityMiddleMeet(p.getCityMiddleMeet());
            data.setCountyJuniorMeet(p.getCountyJuniorMeet());
            data.setCountyMiddleMeet(p.getCountyMiddleMeet());
            data.setInterCutJuniorMeet(p.getInterCutJuniorMeet());
            data.setInterCutMiddleMeet(p.getInterCutMiddleMeet());
            data.setJuniorTheMothClue(p.getJuniorTheMothClue());
            data.setMiddleTheMothClue(p.getMiddleTheMothClue());
            data.setIndicator1(p.getIndicator1());
            data.setIndicator2(p.getIndicator2());
            data.setIndicator1Name(p.getIndicator1Name());
            data.setIndicator2Name(p.getIndicator2Name());
            result.add(data);
        });
        return result;
    }


    private Map<Long, List<AgentGroup>> loadUserGroupInfo(Collection<Long> userId) {
        if (CollectionUtils.isEmpty(userId)) {
            return Collections.emptyMap();
        }
        Map<Long, List<AgentGroupUser>> userGroupInfo = baseGroupService.getGroupUsersByUserIds(userId);
        List<AgentGroupUser> allGroupInfo = new ArrayList<>();
        userGroupInfo.values().forEach(allGroupInfo::addAll);
        Set<Long> groupId = allGroupInfo.stream().map(AgentGroupUser::getGroupId).collect(Collectors.toSet());
        List<AgentGroup> groups = baseOrgService.getGroupByIds(groupId);
        Map<Long, AgentGroup> groupMap = groups.stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity()));
        Map<Long, List<AgentGroup>> result = new HashMap<>();
        userId.forEach(p -> {
            List<AgentGroup> userGroups = new ArrayList<>();
            if (!userGroupInfo.containsKey(p)) {
                result.put(p, userGroups);
                return;
            }
            List<AgentGroupUser> groupInfo = userGroupInfo.get(p);
            Set<Long> groupIds = groupInfo.stream().map(AgentGroupUser::getGroupId).collect(Collectors.toSet());
            groupIds.forEach(p1 -> {
                AgentGroup agentGroupInfo = groupMap.get(p1);
                userGroups.add(agentGroupInfo);
            });
            result.put(p, userGroups);
        });
        return result;
    }


    public int removeSchoolDictData(String id) {
        int updRecords = agentPerformanceConfigDao.deleteAgentPayments(id);
       /* if (updRecords > 0) {
            forceReloadData();
        }*/
        return updRecords;
    }

   /* private synchronized void forceReloadData() {
        String apAll = AgentPerformanceConfig.ap_all();
        agentCacheSystem.CBS.flushable.delete(apAll);
        agentCacheSystem.CBS.flushable.set(CACHE_KEY_DATA_UPDATE_TIME, 3600, System.currentTimeMillis());
    }*/

    public MapMessage importAgentPayments(XSSFWorkbook workbook, Integer type) {
        MapMessage msg = new MapMessage();
        List<String> errorList = new ArrayList<>();
        List<String> rightList = new ArrayList<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        loadDataIfNecessary();
        Map<String, AgentPerformanceConfig> existsData = new HashMap<>();
        existsData.putAll(userMonthData);
        int userRow = 1;
        int rows = 1;
        int successRow = 0;
        int updateRow = 0;
        if (sheet != null) {
            Set<String> accounts = new HashSet<>();
            while (true) {
                try {
                    XSSFRow row = sheet.getRow(userRow++);
                    if (row == null) {
                        break;
                    }
                    String account = XssfUtils.getStringCellValue(row.getCell(0)).trim();
                    accounts.add(account);
                } catch (Exception ex) {
                    logger.error("read excel failed", ex);
                    break;
                }
            }

            while (true) {
                try {
                    XSSFRow row = sheet.getRow(rows++);
                    if (row == null) {
                        break;
                    }
                    List<AgentUser> users = getAllUserByAccounts(accounts);

                    if (CollectionUtils.isEmpty(users)) {
                        errorList.add("录入的用户ID不存在用户");
                        break;
                    }
                    Set<Long> userIds = users.stream().map(AgentUser::getId).collect(Collectors.toSet());
                    Map<String, AgentUser> userMap = users.stream().collect(Collectors.toMap(AgentUser::getAccountName, Function.identity()));
                    Map<Long, List<AgentGroupUser>> groupUserInfo = baseGroupService.getGroupUsersByUserIds(userIds);
                    List<AgentGroupUser> agentGroupUsers = new ArrayList<>();
                    groupUserInfo.values().forEach(agentGroupUsers::addAll);
                    Set<Integer> roles = agentGroupUsers.stream().map(AgentGroupUser::getUserRoleId).collect(Collectors.toSet());
                    if (CollectionUtils.isEmpty(roles)) {
                        errorList.add("有不符合规定的角色");
                        break;
                    }
                    // FIXME 先给忽略。。。
//                    if (roles.size() > 1) {
//                        errorList.add("有不符合规定的角色");
//                        break;
//                    }
                    if ((type == 1 && !roles.contains(AgentRoleType.Region.getId()))
                            || (type == 2 && !roles.contains(AgentRoleType.CityAgent.getId()) && !roles.contains(AgentRoleType.CityAgentLimited.getId()))) {
                        errorList.add("有不符合规定的角色");
                        break;
                    }
                    AgentPerformanceConfig insertPayments = new AgentPerformanceConfig();
                    String account = XssfUtils.getStringCellValue(row.getCell(0));
                    if (!userMap.containsKey(account)) {
                        errorList.add(rows + "行1列,未找到用户信息");
                        continue;
                    }
                    AgentUser user = userMap.get(account);
                    if (user == null) {
                        errorList.add(rows + "行1列,未找到用户信息");
                        continue;
                    }
                    if (!groupUserInfo.containsKey(user.getId())) {
                        errorList.add(rows + "行1列,未找到用户的部门信息");
                        continue;
                    }

                    String settlementMonthStr = XssfUtils.getStringCellValue(row.getCell(1));
                    if (!NumberUtils.isDigits(settlementMonthStr)) {
                        errorList.add(rows + "行2列预算非数字,");
                        continue;
                    }
                    Integer settlementMonth = SafeConverter.toInt(settlementMonthStr);
                    insertPayments.setUserId(user.getId());
                    insertPayments.setAccount(user.getAccountName());
                    insertPayments.setSettlementMonth(settlementMonth);
                    insertPayments.setDisabled(false);
                    if (Objects.equals(type, 2)) {
                        insertPayments.setIdentity(AgentRoleType.CityAgent.getId());
                        insertPayments.setPaymentsType(2);
                        // 市级小学
                        String cityJuniorMeetStr = XssfUtils.getStringCellValue(row.getCell(2));
                        if (!NumberUtils.isDigits(cityJuniorMeetStr)) {
                            errorList.add(rows + "行3列预算非数字,");
                            continue;
                        }
                        Integer cityJuniorMeet = SafeConverter.toInt(cityJuniorMeetStr);

                        if (cityJuniorMeet > 1) {
                            errorList.add(rows + "行3列市级小学专场会议会议数大于1,");
                            continue;
                        }
                        insertPayments.setCityJuniorMeet(cityJuniorMeet);
                        // 区级小学
                        String countyJuniorMeetStr = XssfUtils.getStringCellValue(row.getCell(3));
                        if (!NumberUtils.isDigits(countyJuniorMeetStr)) {
                            errorList.add(rows + "行4列预算非数字,");
                            continue;
                        }
                        Integer countyJuniorMeet = SafeConverter.toInt(countyJuniorMeetStr);
                        if (countyJuniorMeet > 3) {
                            errorList.add(rows + "行4列区级小学专场会议会议数大于3,");
                            continue;
                        }
                        insertPayments.setCountyJuniorMeet(countyJuniorMeet);
                        // 插播小学
                        String interCutJuniorMeetStr = XssfUtils.getStringCellValue(row.getCell(4));
                        if (!NumberUtils.isDigits(interCutJuniorMeetStr)) {
                            errorList.add(rows + "行5列预算非数字,");
                            continue;
                        }
                        Integer interCutJuniorMeet = SafeConverter.toInt(interCutJuniorMeetStr);
                        if (interCutJuniorMeet > 5) {
                            errorList.add(rows + "行5列区级小学插播会议会议数大于5,");
                            continue;
                        }
                        insertPayments.setInterCutJuniorMeet(interCutJuniorMeet);
                        // 小学线索
                        String juniorTheMothClueStr = XssfUtils.getStringCellValue(row.getCell(5));
                        if (!NumberUtils.isDigits(juniorTheMothClueStr)) {
                            errorList.add(rows + "行6列预算非数字,");
                            continue;
                        }
                        Integer juniorTheMothClue = SafeConverter.toInt(juniorTheMothClueStr);
                        insertPayments.setJuniorTheMothClue(juniorTheMothClue);
                        // 市级中学
                        String cityMiddleMeetStr = XssfUtils.getStringCellValue(row.getCell(6));
                        if (!NumberUtils.isDigits(cityMiddleMeetStr)) {
                            errorList.add(rows + "行7列预算非数字,");
                            continue;
                        }
                        Integer cityMiddleMeet = SafeConverter.toInt(cityMiddleMeetStr);
                        if (cityMiddleMeet > 1) {
                            errorList.add(rows + "行7列市级中学专场会议会议数大于1,");
                            continue;
                        }
                        MapMessage lawful = isLawfulCityMeet(user.getId(), existsData, settlementMonth, cityJuniorMeet, cityMiddleMeet);
                        if (!lawful.isSuccess()) {
                            errorList.add(rows + lawful.getInfo());
                            continue;
                        }
                        insertPayments.setCityMiddleMeet(cityMiddleMeet);
                        // 区级中学
                        String countyMiddleMeetStr = XssfUtils.getStringCellValue(row.getCell(7));
                        if (!NumberUtils.isDigits(countyMiddleMeetStr)) {
                            errorList.add(rows + "行8列预算非数字,");
                            continue;
                        }
                        Integer countyMiddleMeet = SafeConverter.toInt(countyMiddleMeetStr);
                        if (countyMiddleMeet > 3) {
                            errorList.add(rows + "行8列区级中学专场会议会议数大于3,");
                            continue;
                        }
                        insertPayments.setCountyMiddleMeet(countyMiddleMeet);
                        // 插播中学
                        String InterCutMiddleMeetStr = XssfUtils.getStringCellValue(row.getCell(8));
                        if (!NumberUtils.isDigits(InterCutMiddleMeetStr)) {
                            errorList.add(rows + "行9列预算非数字,");
                            continue;
                        }
                        Integer InterCutMiddleMeet = SafeConverter.toInt(InterCutMiddleMeetStr);
                        if (InterCutMiddleMeet > 5) {
                            errorList.add(rows + "行9列区级中学插播会议会议数大于5,");
                            continue;
                        }
                        insertPayments.setInterCutMiddleMeet(InterCutMiddleMeet);
                        // 中学线索
                        String middleTheMothClueStr = XssfUtils.getStringCellValue(row.getCell(9));
                        if (!NumberUtils.isDigits(middleTheMothClueStr)) {
                            errorList.add(rows + "行10列预算非数字,");
                            continue;
                        }
                        Integer middleTheMothClue = SafeConverter.toInt(middleTheMothClueStr);
                        insertPayments.setMiddleTheMothClue(middleTheMothClue);

                    } else if (Objects.equals(type, 1)) {
                        insertPayments.setIdentity(AgentRoleType.Region.getId());// FIXME: 2016/9/26 身份
                        insertPayments.setPaymentsType(1);
                        String indicator1Name = XssfUtils.getStringCellValue(row.getCell(2));
                        if (StringUtils.isBlank(indicator1Name)) {
                            errorList.add(rows + "行3列指标1名称为空,");
                            continue;
                        }
                        String indicator1Str = XssfUtils.getStringCellValue(row.getCell(3));
                        if (StringUtils.isBlank(indicator1Str)) {
                            errorList.add(rows + "行4列指标1值称为空,");
                            continue;
                        }
                        Double indicator1 = SafeConverter.toDouble(XssfUtils.getFloatCellValue(row.getCell(3)));
                        if (indicator1 < 0) {
                            errorList.add(rows + "行4列指标1值小于0,");
                            continue;
                        }
                        String indicator2Name = XssfUtils.getStringCellValue(row.getCell(4));
                        if (StringUtils.isBlank(indicator1Name)) {
                            errorList.add(rows + "行5列指标名1称为空,");
                            continue;
                        }
                        String indicator2Str = XssfUtils.getStringCellValue(row.getCell(5));
                        if (StringUtils.isBlank(indicator2Str)) {
                            errorList.add(rows + "行6列指标2值称为空,");
                            continue;
                        }
                        Double indicator2 = SafeConverter.toDouble(XssfUtils.getFloatCellValue(row.getCell(5)));
                        if (indicator1 < 0) {
                            errorList.add(rows + "行6列指标2值小于0,");
                            continue;
                        }
                        insertPayments.setIndicator2Name(indicator2Name);
                        insertPayments.setIndicator1Name(indicator1Name);
                        insertPayments.setIndicator2(indicator2);
                        insertPayments.setIndicator1(indicator1);
                    } else {
                        continue;
                    }
                    if (existsData.containsKey(user.getId() + "_" + settlementMonth)) {
                        AgentPerformanceConfig performanceConfig = existsData.get(user.getId() + "_" + settlementMonth);
                        if (performanceConfig == null) {
                            errorList.add(rows + "行人员的" + settlementMonth + "月结算信息已经存在，但是取出错误");
                            continue;
                        }
                        insertPayments.setId(performanceConfig.getId());
                        agentPerformanceConfigDao.upsert(insertPayments);
                        updateRow++;
                    } else {
                        agentPerformanceConfigDao.insert(insertPayments);
                    }
                    successRow++;
                    existsData.put(user.getId() + "" + settlementMonth, insertPayments);
                } catch (Exception ex) {
                    errorList.add((rows) + "行后添加失败");
                    logger.error("read excel failed", ex);
                    break;
                }
            }
        }

        if (CollectionUtils.isEmpty(errorList)) {
            rightList.add("共计" + successRow + "条，其中新添加" + (successRow - updateRow) + "条，更新" + updateRow + "条");
            msg.add("right", rightList);
            msg.setSuccess(true);
        } else {
            rightList.add("共计" + successRow + "条，其中新添加" + (successRow - updateRow) + "条，更新" + updateRow + "条");
            msg.add("right", rightList);
            msg.add("error", errorList);
            msg.setSuccess(false);
        }

        // 强制刷新数据
        //forceReloadData();
        return msg;
    }

    private List<AgentUser> getAllUserByAccounts(Collection<String> accounts) {
        if (CollectionUtils.isEmpty(accounts)) {
            return Collections.emptyList();
        }
        List<AgentUser> users = agentUserLoaderClient.findAll();
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        return users.stream().filter(p -> accounts.contains(p.getAccountName())).collect(Collectors.toList());
    }

    private MapMessage isLawfulCityMeet(Long userId, Map<String, AgentPerformanceConfig> existsData, Integer month, Integer cityJuniorMeet, Integer cityMiddleMeet) {
        if (cityJuniorMeet == 0 && cityMiddleMeet == 0) {
            return MapMessage.successMessage();
        }
        Integer[] settlementMonths = new Integer[]{201609, 201610, 201611, 201612};
        for (Integer settlementMonth : settlementMonths) {
            if (Objects.equals(settlementMonth, month)) {
                continue;
            }
            if (existsData.containsKey(userId + "_" + settlementMonth)) {
                AgentPerformanceConfig performanceConfig = existsData.get(userId + "_" + settlementMonth);
                if (performanceConfig == null) {
                    continue;
                }
                if (cityJuniorMeet != 0) {
                    Integer oldCity = performanceConfig.getCityJuniorMeet();
                    if (oldCity == 1) {
                        return MapMessage.errorMessage("行3列市级中学专场会议已存在一场");
                    }
                }
                if (cityMiddleMeet != 0) {
                    Integer oldCity = performanceConfig.getCityMiddleMeet();
                    if (oldCity == 1) {
                        return MapMessage.errorMessage("行5列市级中学专场会议已存在一场");
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }


    public AgentPerformanceConfig findByUserIdAndMonth(Long userId, Integer month){
        return agentPerformanceConfigDao.findByUserIdAndMonth(userId, month);
    }

    public List<AgentPerformanceConfig> findByMonthList(Long userId, List<Integer> monthList){
        if(CollectionUtils.isEmpty(monthList)){
            return Collections.emptyList();
        }
        return agentPerformanceConfigDao.findByMonthList(userId, monthList);
    }
}
