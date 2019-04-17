package com.voxlearning.utopia.agent.service.material;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.bean.AgentMaterialBudgetVO;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.agent.dao.mongo.material.AgentMaterialBalanceChangeRecordDao;
import com.voxlearning.utopia.agent.dao.mongo.material.AgentMaterialBudgetDao;
import com.voxlearning.utopia.agent.dao.mongo.material.AgentMaterialCostDao;
import com.voxlearning.utopia.agent.dao.mongo.material.AgentOrderCityCostDao;
import com.voxlearning.utopia.agent.persist.entity.material.*;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentCityLevelService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderPaymentMode;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupRegionLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chunlin.yu
 * @create 2018-02-06 14:28
 **/
@Named
public class AgentMaterialBudgetService extends AbstractAgentService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    AgentMaterialBudgetDao agentMaterialBudgetDao;

    @Inject
    AgentMaterialBalanceChangeRecordDao agentMaterialBalanceChangeRecordDao;

    @Inject
    BaseOrgService baseOrgService;

    @Inject
    AgentGroupRegionLoaderClient agentGroupRegionLoaderClient;

    @Inject private AgentOrderCityCostDao agentOrderCityCostDao;
    @Inject private AgentCityLevelService agentCityLevelService;
    @Inject
    private AgentMaterialCostDao agentMaterialCostDao;
    @Inject
    private AgentGroupUserLoaderClient agentGroupUserLoaderClient;

    /**
     * 查询用户正使用的预算数据
     *
     * @param userId
     * @return
     */
    public AgentMaterialBudget getUserMaterialBudget(Long userId) {
        List<AgentMaterialBudget> agentMaterialBudgetList = agentMaterialBudgetDao.getMaterialBudgetsByUserId(Collections.singleton(userId));
        if (CollectionUtils.isNotEmpty(agentMaterialBudgetList)) {
            agentMaterialBudgetList.sort((o1, o2) -> {
                return 0 - o1.getCreateTime().compareTo(o2.getCreateTime());
            });
            return agentMaterialBudgetList.get(0);
        }
        return null;
    }

    /**
     * 获取近6个月的
     *
     * @param groupId
     * @param regionCode
     * @return
     */
    public List<AgentMaterialBudget> getLatest6MonthCityBudget(long groupId, int regionCode) {
        String dateStr = DateUtils.dateToString(DateUtils.addMonths(new Date(), -6), "yyyyMM");
        int nowYearMonth = getNowYearMonth();
        int dateInt = Integer.parseInt(dateStr);
        List<AgentMaterialBudget> cityBudgets = agentMaterialBudgetDao.getCityBudgets(groupId, regionCode, dateInt).stream().filter(item -> item.getMonth() <= nowYearMonth).collect(Collectors.toList());
        cityBudgets.sort((o1, o2) -> {
            return o1.getMonth().compareTo(o2.getMonth());
        });
        return cityBudgets;
    }

    public List<AgentMaterialBudget> getLatest6MonthCityBudget(Collection<Long> groupIds) {
        String dateStr = DateUtils.dateToString(DateUtils.addMonths(new Date(), -6), "yyyyMM");
        int nowYearMonth = getNowYearMonth();
        int dateInt = Integer.parseInt(dateStr);
        List<AgentMaterialBudget> cityBudgets = agentMaterialBudgetDao.getCityBudgets(groupIds, dateInt).stream().filter(item -> item.getMonth() <= nowYearMonth).collect(Collectors.toList());
        ;
        cityBudgets.sort((o1, o2) -> {
            return o1.getMonth().compareTo(o2.getMonth());
        });
        return cityBudgets;
    }

    public AgentMaterialBudget getAgentMaterialBudget(String id) {
        return agentMaterialBudgetDao.load(id);
    }

    /**
     * 导入城市预算
     */
    public MapMessage importCityBudget(XSSFWorkbook workbook, Integer templateType) {
        List<AgentMaterialBudgetVO> agentMaterialBudgetVOList = convert2AgentMaterialBudgetVO(workbook, 1);
        MapMessage result = validateCityBudgetAndFillUpField(agentMaterialBudgetVOList, templateType);
        if (result.isSuccess()) {
            result = upserts(agentMaterialBudgetVOList, templateType);
        }
        return result;
    }


    /**
     * 导入物料预算   应该已废弃了
     */
    public MapMessage importMaterialBudget(XSSFWorkbook workbook) {
        List<AgentMaterialBudgetVO> agentMaterialBudgetVOList = convert2AgentMaterialBudgetVO(workbook, 2);
        MapMessage result = validateMaterialBudgetAndFillUpField(agentMaterialBudgetVOList);
        if (result.isSuccess()) {
//            result = upserts(agentMaterialBudgetVOList);
        }
        return result;
    }


    public List<AgentMaterialBudgetVO> getMaterialBudgetsByGroup(String groupName) {
        List<AgentMaterialBudget> agentMaterialBudgetList = new ArrayList<>();
        if (null == groupName) {
            Map<Long, List<AgentMaterialBudget>> allMaterialBudgets = agentMaterialBudgetDao.getAllMaterialBudgets();
            for (Long key : allMaterialBudgets.keySet()) {
                agentMaterialBudgetList.addAll(allMaterialBudgets.get(key));
            }
        } else {
            AgentGroup agentGroup = baseOrgService.getGroupByName(groupName);
            if (null != agentGroup) {
                List<AgentGroupUser> agentGroupUserList = baseOrgService.getAllGroupUsersByGroupId(agentGroup.getId());
                agentMaterialBudgetList = agentMaterialBudgetDao.getMaterialBudgetsByUserId(agentGroupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet()));
            }
        }
        List<AgentMaterialBudgetVO> result = new ArrayList<>();
        agentMaterialBudgetList.forEach(item -> {
            AgentMaterialBudgetVO agentMaterialBudgetVO = toAgentBudgetVO(item);
            if (null != agentMaterialBudgetVO) {
                result.add(agentMaterialBudgetVO);
            }
        });
        return result;
    }

    public List<AgentMaterialBudgetVO> getCityBudgets(String groupName, String cityName, int beginMonth, int endMonth) {
        List<AgentMaterialBudget> agentMaterialBudgetList = agentMaterialBudgetDao.searchCityBudgets(groupName, cityName, beginMonth, endMonth);
        List<AgentMaterialBudgetVO> result = new ArrayList<>();
        agentMaterialBudgetList.forEach(item -> {
            AgentMaterialBudgetVO agentMaterialBudgetVO = toAgentBudgetVO(item);
            if (null != agentMaterialBudgetVO) {
                result.add(agentMaterialBudgetVO);
            }
        });
        return result;
    }

    /**
     * 费用
     *
     * @param agentMaterialBudget
     * @return
     */
    private AgentMaterialBudgetVO toAgentBudgetVO(AgentMaterialBudget agentMaterialBudget) {
        if (null == agentMaterialBudget) {
            return null;
        }
        if (agentMaterialBudget.getBudgetType() == 2) {
            AgentMaterialBudgetVO vo = new AgentMaterialBudgetVO();
            vo.setSemester(agentMaterialBudget.getSemester());
            vo.setUserId(agentMaterialBudget.getUserId());
            AgentUser user = baseOrgService.getUser(agentMaterialBudget.getUserId());
            if (null == user) {
                return null;
            }
            vo.setUserName(user.getRealName());
            List<AgentGroupUser> groupUsers = baseOrgService.getGroupUserByUser(user.getId());
            if (CollectionUtils.isNotEmpty(groupUsers)) {
                Long groupId = groupUsers.get(0).getGroupId();
                vo.setGroupId(groupId);
                AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
                if (null != agentGroup) {
                    vo.setGroupName(agentGroup.getGroupName());
                }
            }
            vo.setBudget(agentMaterialBudget.getBudget());
            vo.setBalance(agentMaterialBudget.getBalance());
            AgentRoleType userRole = baseOrgService.getUserRole(user.getId());
            if (null != userRole) {
                vo.setAgentRoleType(userRole.getRoleName());
            }
            vo.setBudgetType(2);
            vo.setId(agentMaterialBudget.getId());
            return vo;
        } else if (agentMaterialBudget.getBudgetType() == 1) {
            AgentMaterialBudgetVO vo = new AgentMaterialBudgetVO();
            vo.setBudgetType(1);
            vo.setBudget(agentMaterialBudget.getBudget());
            vo.setBalance(agentMaterialBudget.getBalance());
            vo.setGroupId(agentMaterialBudget.getGroupId());
            AgentGroup agentGroup = baseOrgService.getGroupById(agentMaterialBudget.getGroupId());
            if (null != agentGroup) {
                //判断部门负责的城市是否删除
                boolean flag = false;
                List<AgentGroupRegion> groupRegions = agentGroupRegionLoaderClient.findByGroupId(agentGroup.getId());
                if (CollectionUtils.isNotEmpty(groupRegions)) {
                    List<Integer> regionCodeList = groupRegions.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(regionCodeList)) {
                        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodeList);
                        for (Integer key : exRegionMap.keySet()) {
                            ExRegion region = exRegionMap.get(key);
                            if (region.fetchRegionType() == RegionType.PROVINCE) {
                                List<ExRegion> cityRegionList = region.getChildren().stream().filter(t -> t.fetchRegionType() == RegionType.CITY).collect(Collectors.toList());
                                List<Integer> cityCodeList = cityRegionList.stream().map(ExRegion::getCityCode).collect(Collectors.toList());
                                if (cityCodeList.contains(agentMaterialBudget.getRegionCode())) {
                                    flag = true;
                                    break;
                                }
                            } else {
                                if (region.getCityCode() == agentMaterialBudget.getRegionCode()) {
                                    flag = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!flag) {
                    vo.setGroupStatus("城市删除-无效");
                } else {
                    vo.setGroupStatus("有效");
                }
                getGroupServiceType(agentGroup, vo);
            } else {
                AgentGroup disabledGroup = baseOrgService.loadDisabledGroup(agentMaterialBudget.getGroupId());
                vo.setGroupDisableTime(DateUtils.dateToString(disabledGroup.getUpdateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
                getGroupServiceType(disabledGroup, vo);
                vo.setGroupStatus("部门删除-无效");

            }
            vo.setRegionCode(agentMaterialBudget.getRegionCode());
            vo.setRegionName(agentMaterialBudget.getRegionName());
            vo.setMonth(agentMaterialBudget.getMonth());
            vo.setId(agentMaterialBudget.getId());
            Map<Integer, AgentCityLevelType> agentCityLevelTypeMap = agentCityLevelService.loadCityLevelTypeMap(Collections.singleton(agentMaterialBudget.getRegionCode()));
            if (MapUtils.isNotEmpty(agentCityLevelTypeMap) && agentCityLevelTypeMap.containsKey(agentMaterialBudget.getRegionCode())) {
                AgentCityLevelType agentCityLevelType = agentCityLevelTypeMap.get(agentMaterialBudget.getRegionCode());
                if (null != agentCityLevelType) {
                    vo.setRegionLevel(agentCityLevelType.getValue());
                }
            }
            Long managerId = baseOrgService.loadGroupUserByGroupId(vo.getGroupId(), AgentRoleType.CityManager).stream().findFirst().orElse(null);
            if (null != managerId) {
                AgentUser user = baseOrgService.getUser(managerId);
                if (null != user) {
                    vo.setCityManager(user.getRealName());
                }
            }
            return vo;
        }
        return null;
    }

    private void getGroupServiceType(AgentGroup agentGroup, AgentMaterialBudgetVO vo) {
        String serviceType = agentGroup.getServiceType();
        if (StringUtils.isNotBlank(serviceType)) {
            String[] arr = serviceType.split(",");
            for (int i = 0; i < arr.length; i++) {
                AgentServiceType agentServiceType = AgentServiceType.nameOf(arr[i]);
                arr[i] = agentServiceType.getTypeName();
            }
            vo.setServiceType(StringUtils.join(arr, ","));
        }
        vo.setGroupName(agentGroup.getGroupName());
    }

    private String getGroupServiceType(AgentGroup agentGroup) {
        if (agentGroup == null) {
            return "";
        }
        if (StringUtils.isNotBlank(agentGroup.getServiceType())) {
            String[] arr = agentGroup.getServiceType().split(",");
            for (int i = 0; i < arr.length; i++) {
                AgentServiceType agentServiceType = AgentServiceType.nameOf(arr[i]);
                arr[i] = agentServiceType.getTypeName();
            }
            return StringUtils.join(arr, ",");
        }
        return "";
    }

    /**
     * 查询余额修改记录
     *
     * @param budgetId
     * @return
     */
    public List<AgentMaterialBalanceChangeRecord> getBalanceChangeRecords(String budgetId) {
        List<AgentMaterialBalanceChangeRecord> changeRecords = agentMaterialBalanceChangeRecordDao.getByBudgetId(budgetId, null);
        changeRecords.sort((o1, o2) -> {
            return 0 - o1.getCreateTime().compareTo(o2.getCreateTime());
        });
        return changeRecords;
    }

    /**
     * 查询预算修改记录
     *
     * @param budgetId
     * @return
     */
    public List<AgentMaterialBalanceChangeRecord> getBudgetChangeRecords(String budgetId) {
        List<AgentMaterialBalanceChangeRecord> changeRecords = agentMaterialBalanceChangeRecordDao.getByBudgetId(budgetId, 1);
        changeRecords.sort((o1, o2) -> {
            return 0 - o1.getCreateTime().compareTo(o2.getCreateTime());
        });
        return changeRecords;
    }

    private MapMessage upserts(List<AgentMaterialBudgetVO> agentMaterialBudgetVOList, Integer templateType) {
//        List<AgentMaterialBudget> agentMaterialBudgetList = new ArrayList<>();
//        agentMaterialBudgetVOList.forEach(item -> {
//            agentMaterialBudgetList.add(item.toAgentMaterialBudget());
//        });
        if (Objects.equals(1, templateType)) {
            return upsertAgentMaterialBudgets(agentMaterialBudgetVOList);
        } else {
            return upsertAgentMaterialBalance(agentMaterialBudgetVOList);
        }

    }

    private MapMessage upsertAgentMaterialBudgets(List<AgentMaterialBudgetVO> agentMaterialBudgetVoList) {
        MapMessage mapMessage = MapMessage.successMessage();
        List<AgentMaterialBudget> allBuget = agentMaterialBudgetDao.query();
        Map<String, List<AgentMaterialBudget>> map = allBuget.stream().collect(Collectors.groupingBy(AgentMaterialBudget::getId, Collectors.toList()));
        List<AgentMaterialBalanceChangeRecord> records = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(agentMaterialBudgetVoList)) {
            int insertCount = 0;
            int updateCount = 0;
            for (int i = 0; i < agentMaterialBudgetVoList.size(); i++) {
                AgentMaterialBudgetVO itemVo = agentMaterialBudgetVoList.get(i);
                if (null != itemVo.getId()) {
                    List<AgentMaterialBudget> list = map.get(itemVo.getId());
                    if (CollectionUtils.isNotEmpty(list)) {
                        AgentMaterialBudget oldBudget = list.stream().findFirst().orElse(null);
                        if (oldBudget != null) {
                            double preCash = oldBudget.getBudget();
                            double budget = itemVo.getBudget();
                            double balance = oldBudget.getBalance();
                            if ("增加".equals(itemVo.getUpdateType())) {
                                oldBudget.setBudget(MathUtils.doubleAdd(oldBudget.getBudget(), budget));//增加预算
                                oldBudget.setBalance(MathUtils.doubleAdd(oldBudget.getBalance(), budget));//增加余额
                            } else {//减少预算时
                                if (oldBudget.getBalance() >= itemVo.getBudget()) {
                                    oldBudget.setBudget(MathUtils.doubleSub(oldBudget.getBudget(), budget));
                                    oldBudget.setBalance(MathUtils.doubleSub(oldBudget.getBalance(), budget));//减少余额
                                }
                            }
                            AgentMaterialBalanceChangeRecord budgetRecord = generateChangeRecord(preCash, oldBudget.getBudget(), budget, itemVo.getContent(), 1, oldBudget.getId(), 1, getCurrentUserId());
                            records.add(budgetRecord);
                            AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(balance, oldBudget.getBalance(), budget, itemVo.getContent(), 2, oldBudget.getId(), 1, getCurrentUserId());
                            records.add(balanceRecord);
                        }
                        agentMaterialBudgetDao.replace(oldBudget);
                        updateCount++;
                    }

                } else {
                    AgentMaterialBudget newBudget = itemVo.toAgentMaterialBudget();
                    newBudget.setBalance(newBudget.getBudget());
                    agentMaterialBudgetDao.insert(newBudget);
                    AgentMaterialBalanceChangeRecord budgetRecord = generateChangeRecord(0d, newBudget.getBudget(), newBudget.getBudget(), itemVo.getContent(), 1, newBudget.getId(), 1, getCurrentUserId());
                    records.add(budgetRecord);
                    AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(0d, newBudget.getBudget(), newBudget.getBudget(), itemVo.getContent(), 2, newBudget.getId(), 1, getCurrentUserId());
                    records.add(balanceRecord);
                    insertCount++;
                }
            }
            if (CollectionUtils.isNotEmpty(records)) {
                agentMaterialBalanceChangeRecordDao.inserts(records);
            }
            mapMessage.add("insertCount", insertCount);
            mapMessage.add("updateCount", updateCount);
        }
        return mapMessage;
    }

    private MapMessage upsertAgentMaterialBalance(List<AgentMaterialBudgetVO> agentMaterialBudgetVoList) {
        MapMessage mapMessage = MapMessage.successMessage();
        List<AgentMaterialBudget> allBuget = agentMaterialBudgetDao.query();
        Map<String, List<AgentMaterialBudget>> map = allBuget.stream().collect(Collectors.groupingBy(AgentMaterialBudget::getId, Collectors.toList()));

        if (CollectionUtils.isNotEmpty(agentMaterialBudgetVoList)) {
            int updateCount = 0;
            List<AgentMaterialBudgetVO> addList = agentMaterialBudgetVoList.stream().filter(p -> "增加".equals(p.getUpdateType())).collect(Collectors.toList());
            List<AgentMaterialBudgetVO> reduceList = agentMaterialBudgetVoList.stream().filter(p -> "减少".equals(p.getUpdateType())).collect(Collectors.toList());

            updateCount = updateCount + updateCityBalance(addList, map);
            updateCount = updateCount + updateCityBalance(reduceList, map);
            mapMessage.add("insertCount", 0);
            mapMessage.add("updateCount", updateCount);
        }
        return mapMessage;
    }

    private int updateCityBalance(List<AgentMaterialBudgetVO> agentMaterialBudgetVoList, Map<String, List<AgentMaterialBudget>> map) {
        int updateCount = 0;
        List<AgentMaterialBalanceChangeRecord> records = new ArrayList<>();
        for (int i = 0; i < agentMaterialBudgetVoList.size(); i++) {
            AgentMaterialBudgetVO itemVo = agentMaterialBudgetVoList.get(i);
            if (null != itemVo.getId()) {
                List<AgentMaterialBudget> list = map.get(itemVo.getId());
                if (CollectionUtils.isNotEmpty(list)) {
                    AgentMaterialBudget oldBudget = list.stream().findFirst().orElse(null);
                    if (oldBudget != null) {
                        double updateBalance = itemVo.getBalance();
                        double balance = oldBudget.getBalance();
                        if ("增加".equals(itemVo.getUpdateType()) && MathUtils.doubleAdd(oldBudget.getBalance(), updateBalance) <= oldBudget.getBudget()) {
                            oldBudget.setBalance(MathUtils.doubleAdd(balance, updateBalance));//增加余额
                        } else if ("减少".equals(itemVo.getUpdateType())) {//减少余额
                            if (oldBudget.getBalance() >= itemVo.getBalance()) {
                                oldBudget.setBalance(MathUtils.doubleSub(balance, updateBalance));//减少余额
                            }
                        }
                        AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(balance, oldBudget.getBalance(), updateBalance, itemVo.getContent(), 2, oldBudget.getId(), 1, getCurrentUserId());
                        records.add(balanceRecord);
                    }
                    agentMaterialBudgetDao.replace(oldBudget);
                    updateCount++;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(records)) {
            agentMaterialBalanceChangeRecordDao.inserts(records);
        }
        return updateCount;
    }

    private List<AgentMaterialBudgetVO> convert2AgentMaterialBudgetVO(XSSFWorkbook workbook, Integer budgetType) {
        List<AgentMaterialBudgetVO> resultList = new ArrayList<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        int rows = 1;
        if (null != sheet) {
            while (true) {
                try {
                    XSSFRow row = sheet.getRow(rows++);
                    if (row == null) {
                        break;
                    }
                    AgentMaterialBudgetVO agentMaterialBudgetVO = new AgentMaterialBudgetVO();
                    if (budgetType == 2) {
                        String semester = XssfUtils.getStringCellValue(row.getCell(0));
                        String userName = XssfUtils.getStringCellValue(row.getCell(1));
                        String groupName = XssfUtils.getStringCellValue(row.getCell(2));
                        String cityName = XssfUtils.getStringCellValue(row.getCell(3));
                        Double budget = XssfUtils.getDoubleCellValue(row.getCell(4));
                        agentMaterialBudgetVO.setSemester(semester);
                        agentMaterialBudgetVO.setGroupName(groupName);
                        agentMaterialBudgetVO.setRegionName(cityName);
                        agentMaterialBudgetVO.setUserName(userName);
                        agentMaterialBudgetVO.setBudget(budget);
                    } else if (budgetType == 1) {
                        Integer month = XssfUtils.getIntCellValue(row.getCell(0));
                        String groupName = XssfUtils.getStringCellValue(row.getCell(1));
                        String cityName = XssfUtils.getStringCellValue(row.getCell(2));
//                        String regionLevel = XssfUtils.getStringCellValue(row.getCell(3));
                        String updateType = XssfUtils.getStringCellValue(row.getCell(3));
                        Double budget = XssfUtils.getDoubleCellValue(row.getCell(4));
                        String content = XssfUtils.getStringCellValue(row.getCell(5));
                        agentMaterialBudgetVO.setMonth(month);
                        agentMaterialBudgetVO.setGroupName(groupName);
                        agentMaterialBudgetVO.setRegionName(cityName);
                        agentMaterialBudgetVO.setUpdateType(updateType);
//                        agentMaterialBudgetVO.setRegionLevel(regionLevel);
                        agentMaterialBudgetVO.setBudget(budget);
                        agentMaterialBudgetVO.setContent(content);
                        //导入模板是更新城市余额时要用这个值
                        agentMaterialBudgetVO.setBalance(budget);
                    }
                    agentMaterialBudgetVO.setBudgetType(budgetType);
                    resultList.add(agentMaterialBudgetVO);
                } catch (Exception ex) {
                    break;
                }
            }
        }
        return resultList;
    }

    private MapMessage validateCityBudgetAndFillUpField(List<AgentMaterialBudgetVO> agentMaterialBudgetVOList, Integer templateType) {
//        List<String> errorList = new ArrayList<>();
        Map<String, List<String>> errorMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(agentMaterialBudgetVOList)) {
//            List<String> tempContains = new ArrayList<>();
            Map<Integer, List<AgentMaterialBudget>> allCityBudgets = agentMaterialBudgetDao.getAllCityBudgets();

            Map<String, List<AgentMaterialBudgetVO>> rowMap = new HashMap<>();
            int nowYearMonth = getNowYearMonth();
            for (int i = 0; i < agentMaterialBudgetVOList.size(); i++) {
                int rows = i + 2;
                AgentMaterialBudgetVO item = agentMaterialBudgetVOList.get(i);
                String rowKey = com.voxlearning.alps.core.util.StringUtils.formatMessage(
                        "{}_{}_{}",
                        item.getMonth(),
                        item.getGroupName(),
                        item.getRegionName());
                cacheRows(rowMap, rowKey, item);
                if (null == item.getMonth() || item.getMonth() <= 0) {
                    String errorMsg = rows + "行:月份不正确。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                }
                if (null == item.getMonth() || item.getMonth() == 0 || item.getMonth() < 200000 || item.getMonth() > 210000) {
                    String errorMsg = rows + "行:月份格式不正确。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                } else {
                    int month = item.getMonth() % 100;
                    if (month <= 0 || month > 12) {
                        String errorMsg = rows + "行:月份格式不正确。";
                        addErrorInfo(errorMap, rowKey, errorMsg);
                        continue;
                    }
                }

                if (StringUtils.isEmpty(item.getGroupName())) {
                    String errorMsg = rows + "行:部门为空。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                }
                AgentGroup agentGroup = baseOrgService.getGroupByName(item.getGroupName());
                if (agentGroup == null) {
                    String errorMsg = rows + "行:部门不存在。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                }

                item.setGroupId(agentGroup.getId());
                if (agentGroup.fetchGroupRoleType() != AgentGroupRoleType.City) {
                    String errorMsg = rows + "行:部门角色不是分区。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                }
                if (StringUtils.isEmpty(item.getRegionName())) {
                    String errorMsg = rows + "行:城市不能为空。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                }

                List<AgentGroupRegion> groupRegions = agentGroupRegionLoaderClient.findByGroupId(agentGroup.getId());
                List<Integer> regionCodeList = groupRegions.stream().map(AgentGroupRegion::getRegionCode).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(regionCodeList)) {
                    Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadRegions(regionCodeList);
                    for (Integer key : exRegionMap.keySet()) {
                        ExRegion region = exRegionMap.get(key);
                        if (region.fetchRegionType() == RegionType.PROVINCE) {
                            List<ExRegion> cityRegionList = region.getChildren().stream().filter(t -> t.fetchRegionType() == RegionType.CITY).collect(Collectors.toList());
                            cityRegionList.forEach(cityRegion -> {
                                if (cityRegion.getCityName().equals(item.getRegionName())) {
                                    item.setRegionCode(cityRegion.getCityCode());
                                }
                            });
                        } else {
                            if (region.getCityName().equals(item.getRegionName())) {
                                item.setRegionCode(region.getCityCode());
                                break;
                            }
                        }
                    }
                }
                if (null == item.getRegionCode()) {
                    String errorMsg = rows + "行:城市不存在或者城市与分区对应关系错误。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                }


                List<AgentMaterialBudget> agentMaterialBudgets = allCityBudgets.get(item.getRegionCode());
                if (CollectionUtils.isEmpty(agentMaterialBudgets)) {
                    agentMaterialBudgets = new ArrayList<>();
                }
                List<AgentMaterialBudget> materialBudgets = agentMaterialBudgets.stream().filter(p -> Objects.equals(p.getMonth(), item.getMonth()) && Objects.equals(p.getGroupId(), item.getGroupId())).collect(Collectors.toList());
                AgentMaterialBudget agentMaterialBudget = materialBudgets.stream().findFirst().orElse(null);

                if (StringUtils.isEmpty(item.getUpdateType())) {
                    String errorMsg = rows + "行:更新类型为空。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                }
                if (!"增加".equals(item.getUpdateType()) && !"减少".equals(item.getUpdateType())) {
                    String errorMsg = rows + "行:更新类型不正确。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                }

                if (null != agentMaterialBudget) {
                    item.setId(agentMaterialBudget.getId());
                    if (Objects.equals(2, templateType)) { //更新余额
                        if (!checkForUpdates(rowMap, rowKey, agentMaterialBudget, templateType)) {
                            String errorMsg = rows + "行:预算不足或余额不足不能修改。";
                            addErrorInfo(errorMap, rowKey, errorMsg);
                            continue;
                        } else { //前一条验证不符合 后一条验证又符合 最终符合的情况下  移除错误提示
                            errorMap.remove(rowKey);
                        }
                    } else {//更新预算
                        if ("减少".equals(item.getUpdateType())) {
                            if (!checkForUpdates(rowMap, rowKey, agentMaterialBudget, templateType)) {
                                String errorMsg = rows + "行:剩余金额小于修改金额不能更改。";
                                addErrorInfo(errorMap, rowKey, errorMsg);
                                continue;
                            } else {
                                errorMap.remove(rowKey);
                            }
                        }
                    }
                } else {
                    if ("减少".equals(item.getUpdateType())) {
                        String errorMsg = rows + "行:不存在该城市费用无法减少。";
                        addErrorInfo(errorMap, rowKey, errorMsg);
                        continue;
                    }
                    if ("增加".equals(item.getUpdateType()) && Objects.equals(2, templateType)) {
                        String errorMsg = rows + "行:不存在该城市费用无法增加。";
                        addErrorInfo(errorMap, rowKey, errorMsg);
                        continue;
                    }
                }
                if (item.getBudget() == null || item.getBudget() < 0) {
                    String errorMsg = rows + "行:预算为空或者小于0。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                }

//                if (tempContains.contains(rowKey)){
//
//                    String errorMsg = rows + "行:与前面有相同的内容。";
//                    addErrorInfo( errorMap,rowKey, errorMsg);
//                    continue;
//                }else {
//                    tempContains.add(rowKey);
//                }
                if (StringUtils.isEmpty(item.getContent())) {
                    String errorMsg = rows + "行:更新原因不能为空。";
                    addErrorInfo(errorMap, rowKey, errorMsg);
                    continue;
                }
            }
        } else {
            String errorMsg = "Excel中没有内容";
            addErrorInfo(errorMap, "all", errorMsg);
        }
        if (errorMap.values().size() > 0) {
            MapMessage resultMessage = MapMessage.errorMessage().add("errorList", errorMap.values());
            return resultMessage;
        }
        return MapMessage.successMessage();
    }


    private MapMessage validateMaterialBudgetAndFillUpField(List<AgentMaterialBudgetVO> agentMaterialBudgetVOList) {
        List<String> errorList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(agentMaterialBudgetVOList)) {
            List<String> tempContains = new ArrayList<>();
            Map<Long, List<AgentMaterialBudget>> allMaterialBudgets = agentMaterialBudgetDao.getAllMaterialBudgets();
            for (int i = 0; i < agentMaterialBudgetVOList.size(); i++) {
                int rows = i + 2;
                AgentMaterialBudgetVO item = agentMaterialBudgetVOList.get(i);
                if (StringUtils.isEmpty(item.getSemester())) {
                    errorList.add(rows + "行:学期为空。");
                    continue;
                }
                if (item.getSemester().length() != 7) {
                    errorList.add(rows + "行:请输入形如‘2018年春季’或 ‘2018年秋季’格式的学期。");
                    continue;
                }
                int year = SafeConverter.toInt(item.getSemester().substring(0, 4));
                if (year < 2000 || year > 2050) {
                    errorList.add(rows + "行:请输入形如‘2018年春季’或 ‘2018年秋季’格式的学期。");
                    continue;
                }
                String endStr = item.getSemester().substring(4, 7);
                if (!endStr.equals("年春季") && !endStr.equals("年秋季")) {
                    errorList.add(rows + "行:请输入形如‘2018年春季’或 ‘2018年秋季’格式的学期。");
                    continue;
                }

                if (StringUtils.isEmpty(item.getGroupName())) {
                    errorList.add(rows + "行:分区为空。");
                    continue;
                }
                AgentGroup agentGroup = baseOrgService.getGroupByName(item.getGroupName());
                if (agentGroup == null) {
                    errorList.add(rows + "行:分区不存在。");
                    continue;
                }

                if (StringUtils.isEmpty(item.getUserName())) {
                    errorList.add(rows + "行:姓名为空。");
                    continue;
                }
                List<AgentUser> agentUsers = baseOrgService.getUserByRealName(item.getUserName());
                if (CollectionUtils.isEmpty(agentUsers)) {
                    errorList.add(rows + "行:用户不存在。");
                    continue;
                }
                AgentUser agentUser = null;
                for (int j = 0; j < agentUsers.size(); j++) {
                    AgentUser p = agentUsers.get(j);
                    AgentGroupUser groupUser = baseOrgService.getGroupUser(agentGroup.getId(), p.getId());
                    if (null != groupUser) {
                        agentUser = p;
                        break;
                    }
                }

                if (null == agentUser) {
                    errorList.add(rows + "行:姓名与分区对应关系错误。");
                    continue;
                }
                item.setUserId(agentUser.getId());

                AgentRoleType userRole = baseOrgService.getUserRole(agentUser.getId());
                if (Objects.equals(userRole, AgentRoleType.Country)) {
                    if (agentGroup.fetchGroupRoleType() != AgentGroupRoleType.Country) {
                        errorList.add(rows + "行:全国总监部门应为 市场部");
                    }
                } else {
                    if (agentGroup.fetchGroupRoleType() != AgentGroupRoleType.City) {
                        errorList.add(rows + "行:分区角色不是分区。");
                        continue;
                    }
                }
                List<AgentMaterialBudget> agentMaterialBudgets = allMaterialBudgets.get(agentUser.getId());
                if (CollectionUtils.isNotEmpty(agentMaterialBudgets)) {
                    List<AgentMaterialBudget> materialBudgets = agentMaterialBudgets.stream().filter(p -> Objects.equals(p.getSemester(), item.getSemester())).collect(Collectors.toList());
                    AgentMaterialBudget agentMaterialBudget = materialBudgets.stream().filter(p -> p.getBudget() == 0).findFirst().orElse(null);
                    if (null == agentMaterialBudget) {
                        if (CollectionUtils.isNotEmpty(materialBudgets)) {
                            errorList.add(rows + "行:用户已经导入过预算。");
                            continue;
                        }
                    } else {
                        item.setId(agentMaterialBudget.getId());
                        item.setBalance(item.getBudget());
                    }
                }
                if (item.getBudget() == null || item.getBudget() < 0) {
                    errorList.add(rows + "行:预算为空或者小于0。");
                    continue;
                }
                String key = com.voxlearning.alps.core.util.StringUtils.formatMessage(
                        "{}{}",
                        item.getSemester(),
                        item.getUserName());
                if (tempContains.contains(key)) {
                    errorList.add(rows + "行:与前面有相同的内容。");
                    continue;
                } else {
                    tempContains.add(key);
                }
            }
        } else {
            errorList.add("Excel中没有内容");
        }
        if (errorList.size() > 0) {
            MapMessage resultMessage = MapMessage.errorMessage().add("errorList", errorList);
            return resultMessage;
        }
        return MapMessage.successMessage();
    }

    public MapMessage changeBudget(String id, int modifyType, double modifyCount, String modifyReason) {
        if (StringUtils.isEmpty(modifyReason)) {
            return MapMessage.errorMessage("请填写修改原因");
        }
        AgentMaterialBudget agentMaterialBudget = agentMaterialBudgetDao.load(id);
        if (agentMaterialBudget == null) {
            return MapMessage.errorMessage("没有该记录");
        }
        double beforeBudget = agentMaterialBudget.getBudget();
        double beforeBalance = agentMaterialBudget.getBalance();
        //增加预算
        if (modifyType == 1) {
            double budgetResult = MathUtils.doubleAdd(agentMaterialBudget.getBudget(), modifyCount);
            double balanceResult = MathUtils.doubleAdd(agentMaterialBudget.getBalance(), modifyCount);
            agentMaterialBudget.setBudget(budgetResult);
            agentMaterialBudget.setBalance(balanceResult);
        } else if (modifyType == 2) {
            if (modifyCount > agentMaterialBudget.getBalance()) {
                return MapMessage.errorMessage("减少的预算不能大于余额");
            }
            double budgetResult = MathUtils.doubleSub(agentMaterialBudget.getBudget(), modifyCount);
            double balanceResult = MathUtils.doubleSub(agentMaterialBudget.getBalance(), modifyCount);
            agentMaterialBudget.setBudget(budgetResult);
            agentMaterialBudget.setBalance(balanceResult);
        }
        agentMaterialBudgetDao.replace(agentMaterialBudget);
        AgentMaterialBalanceChangeRecord budgetRecord = generateChangeRecord(beforeBudget, agentMaterialBudget.getBudget(), modifyCount, modifyReason, 1, agentMaterialBudget.getId(), 1, getCurrentUserId());
        AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(beforeBalance, agentMaterialBudget.getBalance(), modifyCount, modifyReason, 2, agentMaterialBudget.getId(), 1, getCurrentUserId());
        agentMaterialBalanceChangeRecordDao.insert(budgetRecord);
        agentMaterialBalanceChangeRecordDao.insert(balanceRecord);
        return MapMessage.successMessage();
    }

    /**
     * 扣减物料余额
     *
     * @param agentOrder
     * @return
     */
    public MapMessage deductMaterialBalance(AgentOrder agentOrder) {
        AgentMaterialCost userMaterialCost = getUserMaterialCostByUserId(agentOrder.getCreator());
        if (null == userMaterialCost) {
            return MapMessage.errorMessage("未设置过余额");
        }
        if (userMaterialCost.getBalance() < SafeConverter.toDouble(agentOrder.getOrderAmount())) {
            return MapMessage.errorMessage("余额不足");
        }
        String reason = StringUtils.formatMessage("减少余额，购买物料，订单编号：{}，申请人：{}", agentOrder.getId(), agentOrder.getCreatorName());
        return doChangeMaterialBalance(userMaterialCost.getId(), 2, SafeConverter.toDouble(agentOrder.getOrderAmount()), reason, agentOrder.getCreator());
    }

    /**
     * 扣减城市余额
     *
     * @param agentOrder
     * @return
     */
    public MapMessage deductCityBalance(AgentOrder agentOrder, Integer regionCode, List<String> materialBudgetIdList) {
        if (!AgentOrderPaymentMode.CITY_COST.getPayId().equals(agentOrder.getPaymentMode())) {
            return MapMessage.errorMessage("订单支付方式不是城市费用。");
        }
        if (null == regionCode) {
            return MapMessage.errorMessage("请选择费用城市。");
        }
        StringBuffer sbf = new StringBuffer();
        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(agentOrder.getCreator());
        if (CollectionUtils.isNotEmpty(groupUserList)) {
            List<AgentMaterialBudget> latest6MonthCityBudget = getLatest6MonthCityBudget(groupUserList.get(0).getGroupId(), regionCode);
            if (CollectionUtils.isNotEmpty(latest6MonthCityBudget)) {
                Double balanceSum = 0d;
                if (CollectionUtils.isNotEmpty(latest6MonthCityBudget)) {
                    for (int i = 0; i < latest6MonthCityBudget.size(); i++) {
                        AgentMaterialBudget item = latest6MonthCityBudget.get(i);
                        balanceSum = MathUtils.doubleAdd(balanceSum, item.getBalance());
                    }
                }
                if (SafeConverter.toDouble(agentOrder.getOrderAmount()) > balanceSum) {
                    return MapMessage.errorMessage("余额不足");
                }
            } else {
                return MapMessage.errorMessage("暂无该城市近六个月预算。");
            }

            //勾选的物料余额
            List<AgentMaterialBudget> selectedAgentMaterialBudgetList = new ArrayList<>();
            //剩余的物料余额
            List<AgentMaterialBudget> lastAgentMaterialBudgetList = new ArrayList<>();
            //重新拼装
            List<AgentMaterialBudget> finalAgentMaterialBudgetList = new ArrayList<>();
            latest6MonthCityBudget.forEach(item -> {
                if (materialBudgetIdList.contains(item.getId())) {
                    selectedAgentMaterialBudgetList.add(item);
                } else {
                    lastAgentMaterialBudgetList.add(item);
                }
            });
            finalAgentMaterialBudgetList.addAll(selectedAgentMaterialBudgetList);
            finalAgentMaterialBudgetList.addAll(lastAgentMaterialBudgetList);

            double costSum = agentOrder.getOrderAmount();
            Map<String, Double> costs = new LinkedHashMap<>();
            for (int i = 0; i < finalAgentMaterialBudgetList.size(); i++) {
                if (costSum <= 0) {
                    break;
                }
                AgentMaterialBudget agentMaterialBudget = finalAgentMaterialBudgetList.get(i);
                double minus = 0d;
                if (agentMaterialBudget.getBalance() >= costSum) {
                    minus = costSum;
                } else {
                    minus = agentMaterialBudget.getBalance();
                }
                if (i == 0) {
                    sbf.append(agentMaterialBudget.getRegionName());
                }
                sbf.append(agentMaterialBudget.getMonth()).append(" ").append(MathUtils.doubleAdd(minus)).append(" 元");
                if (i < finalAgentMaterialBudgetList.size() - 1) {
                    sbf.append(", ");
                }
                costs.put(agentMaterialBudget.getId(), minus);
                costSum = MathUtils.doubleSub(costSum, minus);
                String reason = StringUtils.formatMessage("减少余额，购买物料，订单编号：{}，申请人：{}", agentOrder.getId(), agentOrder.getCreatorName());
                doChangeBalance(agentMaterialBudget.getId(), 2, MathUtils.doubleAdd(minus), reason, agentOrder.getCreator());
            }
            if (MapUtils.isNotEmpty(costs)) {
                AgentOrderCityCost oldData = agentOrderCityCostDao.load(agentOrder.getId());
                if (null != oldData) {
                    oldData.setCosts(costs);
                    oldData.setRegionCode(regionCode);
                    agentOrderCityCostDao.replace(oldData);
                } else {
                    AgentOrderCityCost agentOrderCityCost = new AgentOrderCityCost();
                    agentOrderCityCost.setCosts(costs);
                    agentOrderCityCost.setRegionCode(regionCode);
                    agentOrderCityCost.setOrderId(agentOrder.getId());
                    agentOrderCityCostDao.insert(agentOrderCityCost);
                }
            }

        } else {
            return MapMessage.errorMessage("用户机构对应不正确。");
        }
        return MapMessage.successMessage().add("costMonthStr", sbf.toString());
    }


    public void upsertAgentOrderCityCost(AgentOrderCityCost agentOrderCityCost) {
        AgentOrderCityCost load = agentOrderCityCostDao.load(agentOrderCityCost.getOrderId());
        if (null == load) {
            agentOrderCityCostDao.insert(agentOrderCityCost);
        } else {
            agentOrderCityCost.setOrderId(load.getOrderId());
            agentOrderCityCostDao.replace(agentOrderCityCost);
        }
    }

    public void removeAgentOrderCityCost(Long orderId) {
        agentOrderCityCostDao.remove(orderId);
    }

    public AgentOrderCityCost getAgentOrderCityCost(Long orderId) {
        return agentOrderCityCostDao.load(orderId);
    }


    /**
     * 退回城市余额
     *
     * @param agentOrder
     * @return
     */
    public MapMessage returnCityBalance(AgentOrder agentOrder) {
        if (!AgentOrderPaymentMode.CITY_COST.getPayId().equals(agentOrder.getPaymentMode())) {
            return MapMessage.errorMessage("订单支付方式不是城市费用。");
        }
        AgentOrderCityCost agentOrderCityCost = agentOrderCityCostDao.load(agentOrder.getId());
        Map<String, Double> costs = agentOrderCityCost.getCosts();
        if (MapUtils.isNotEmpty(costs)) {
            costs.forEach((k, v) -> {
                String reason = StringUtils.formatMessage("增加余额，订单退回或者被拒绝，订单编号：{}，申请人：{}", agentOrder.getId(), agentOrder.getCreatorName());
                doChangeBalance(k, 1, v, reason, agentOrder.getCreator());
            });
        }
        return MapMessage.successMessage();
    }

    /**
     * 退回物料余额
     *
     * @param agentOrder
     * @return
     */
    public MapMessage returnMaterialBalance(AgentOrder agentOrder) {
        AgentMaterialCost userMaterialCost = getUserMaterialCostByUserId(agentOrder.getCreator());
        if (null == userMaterialCost) {
            return MapMessage.errorMessage("未设置过预算");
        }
        String reason = StringUtils.formatMessage("增加余额，订单退回或者被拒绝，订单编号：{}，申请人：{}", agentOrder.getId(), agentOrder.getCreatorName());
        return doChangeMaterialBalance(userMaterialCost.getId(), 1, SafeConverter.toDouble(agentOrder.getOrderAmount()), reason, agentOrder.getCreator());
    }


    public MapMessage changeBalance(String id, int modifyType, double modifyCount, String modifyReason, Long operatorId) {
        AgentMaterialBudget agentMaterialBudget = agentMaterialBudgetDao.load(id);
        if (Objects.equals(agentMaterialBudget.getBudgetType(), 1)) {
            if (!getCurrentUser().isCountryManager() && !getCurrentUser().isFinance()) {
                return MapMessage.errorMessage("只有是全国总监或财务才能调整城市余额");
            }
        } else {
            if (!getCurrentUser().isCountryManager()) {
                return MapMessage.errorMessage("只有是全国总监才能调整物料余额");
            }
        }
        if (StringUtils.isEmpty(modifyReason)) {
            return MapMessage.errorMessage("请填写修改原因");
        }
        if (agentMaterialBudget == null) {
            return MapMessage.errorMessage("没有该记录");
        }
        //增加余额
        if (modifyType == 1) {
            double balanceResult = MathUtils.doubleAdd(agentMaterialBudget.getBalance(), modifyCount);
            agentMaterialBudget.setBalance(balanceResult);
        } else if (modifyType == 2) {
            double balanceResult = MathUtils.doubleSub(agentMaterialBudget.getBalance(), modifyCount);
            if (balanceResult < 0) {
                return MapMessage.errorMessage("减少的数量不能大于余额");
            }
            agentMaterialBudget.setBalance(balanceResult);
        }
        if (agentMaterialBudget.getBudget() < agentMaterialBudget.getBalance()) {
            return MapMessage.errorMessage("修改后余额不能大于预算");
        }

        return doChangeBalance(id, modifyType, modifyCount, modifyReason, operatorId);
    }

    private MapMessage doChangeBalance(String id, int modifyType, double modifyCount, String modifyReason, Long operatorId) {
        if (modifyCount != 0) {
            AgentMaterialBudget agentMaterialBudget = agentMaterialBudgetDao.load(id);
            double beforeBalance = agentMaterialBudget.getBalance();
            //增加余额
            if (modifyType == 1) {
                double balanceResult = MathUtils.doubleAdd(agentMaterialBudget.getBalance(), modifyCount);
                agentMaterialBudget.setBalance(balanceResult);
            } else if (modifyType == 2) {
                double balanceResult = MathUtils.doubleSub(agentMaterialBudget.getBalance(), modifyCount);
                agentMaterialBudget.setBalance(balanceResult);
            }
            agentMaterialBudgetDao.replace(agentMaterialBudget);
            AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(beforeBalance, agentMaterialBudget.getBalance(), modifyCount, modifyReason, 2, agentMaterialBudget.getId(), 1, operatorId);
            agentMaterialBalanceChangeRecordDao.insert(balanceRecord);
        }
        return MapMessage.successMessage();
    }

    private MapMessage doChangeMaterialBalance(String id, Integer modifyType, Double modifyCount, String modifyReason, Long operatorId) {
        if (modifyCount != 0) {
            AgentMaterialCost agentMaterialCost = agentMaterialCostDao.load(id);
            double beforeBalance = agentMaterialCost.getBalance();
            //增加余额
            if (modifyType == 1) {
                double balanceResult = MathUtils.doubleAdd(agentMaterialCost.getBalance(), modifyCount);
                agentMaterialCost.setBalance(balanceResult);
            } else if (modifyType == 2) {
                double balanceResult = MathUtils.doubleSub(agentMaterialCost.getBalance(), modifyCount);
                agentMaterialCost.setBalance(balanceResult);
            }
            agentMaterialCostDao.replace(agentMaterialCost);
            AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(beforeBalance, agentMaterialCost.getBalance(), modifyCount, modifyReason, 2, agentMaterialCost.getId(), 2, operatorId);
            agentMaterialBalanceChangeRecordDao.insert(balanceRecord);
        }
        return MapMessage.successMessage();
    }

    private AgentMaterialBalanceChangeRecord generateChangeRecord(Double preCash, Double afterCash, Double quantity, String comment, Integer recordType, String agentMaterialBudgetId, Integer operateType, Long operatorUserId) {
        AgentMaterialBalanceChangeRecord record = new AgentMaterialBalanceChangeRecord();
        record.setPreCash(preCash);
        record.setAfterCash(afterCash);
        record.setQuantity(quantity);
        record.setComment(comment);
        record.setRecordType(recordType);
        record.setAgentMaterialBudgetId(agentMaterialBudgetId);
        if (null != operatorUserId) {
            AgentUser operatorUser = baseOrgService.getUser(operatorUserId);
            if (null != operatorUser) {
                record.setOperatorId(operatorUser.getId());
                record.setOperatorName(operatorUser.getRealName());
            }
        }
        record.setOperateType(operateType);
        return record;
    }

    /**
     * 获取当前年月int值
     *
     * @return
     */
    public int getNowYearMonth() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
        String nowMonthStr = formatter.format(currentTime);
        return Integer.valueOf(nowMonthStr).intValue();
    }


    //物料余额检查
    public MapMessage checkWorkboook(XSSFWorkbook workbook) {
        MapMessage mapMessage = MapMessage.successMessage();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet == null) {
            return MapMessage.errorMessage();
        }
        List<AgentMaterialCostVo> allUserMaterialCostList = new ArrayList<>();
        List<AgentMaterialCostVo> userMaterialCostList = new ArrayList<>();
        List<AgentMaterialCostVo> userUpdateMaterialCostList = new ArrayList<>();//用户余额需更新list
        //获取部门物料费用
        Map<Long, List<AgentMaterialCost>> groupMaterialCostMap = agentMaterialCostDao.getGroupMaterialCost().stream().collect(Collectors.groupingBy(AgentMaterialCost::getGroupId));
        //获取人员物料费用
        Map<Long, List<AgentMaterialCost>> userMaterialCostMap = agentMaterialCostDao.getUserMaterialCost().stream().collect(Collectors.groupingBy(AgentMaterialCost::getUserId));

        List<String> errorInfoList = new ArrayList<>();
        boolean checkFlag = true;
        int rowNo = 1;
        Map<String, Object> groupMaterialCostSumMap = new HashMap<>();
        Map<String, Object> userMaterialCostSumMap = new HashMap<>();
        while (true) {
            XSSFRow row = sheet.getRow(rowNo++);
            if (row == null) {
                break;
            }
            String schoolTerm = SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(0)));//学期
            String groupName = SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(1)));//部门
            String userName = XssfUtils.getStringCellValue(row.getCell(2));//姓名
            String updateType = XssfUtils.getStringCellValue(row.getCell(3));//更新类型 增加/减少
            Double money = XssfUtils.getDoubleCellValue(row.getCell(4));//金额
            String content = XssfUtils.getStringCellValue(row.getCell(5));//备注
            if (StringUtils.isBlank(schoolTerm)) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行，无学期，请检查后重新上传。");
                continue;
            }
            if (schoolTerm.length() != 7) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行:请输入形如‘2018年春季’或 ‘2018年秋季’格式的学期。");
                continue;
            }
            int year = SafeConverter.toInt(schoolTerm.substring(0, 4));
            if (year < 2000 || year > 2050) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行:请输入形如‘2018年春季’或 ‘2018年秋季’格式的学期。");
                continue;
            }
            String endStr = schoolTerm.substring(4, 7);
            if (!endStr.equals("年春季") && !endStr.equals("年秋季")) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行:请输入形如‘2018年春季’或 ‘2018年秋季’格式的学期。");
                continue;
            }

            if (StringUtils.isEmpty(groupName)) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行,部门为空。");
                continue;
            }
            AgentGroup agentGroup = baseOrgService.getGroupByName(groupName);
            if (agentGroup == null) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行,部门不存在。");
                continue;
            }

            Long userId = 0l;
            if (StringUtils.isEmpty(userName)) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行，姓名为空。");
                continue;
            }
            List<AgentUser> agentUsers = baseOrgService.getUserByRealName(userName);
            if (CollectionUtils.isEmpty(agentUsers)) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行，用户不存在。");
                continue;
            }
            AgentUser agentUser = null;
            for (int j = 0; j < agentUsers.size(); j++) {
                AgentUser p = agentUsers.get(j);
                AgentGroupUser groupUser = baseOrgService.getGroupUser(agentGroup.getId(), p.getId());
                if (null != groupUser) {
                    agentUser = p;
                    break;
                }
            }
            if (null == agentUser) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行,姓名与部门对应关系错误。");
                continue;
            }


            if (null == money) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行，金额为空。");
                continue;
            }
            if (money <= 0D) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行，金额必须大于0。");
                continue;
            }

            if (StringUtils.isBlank(updateType) || !("增加".equals(updateType) || "减少".equals(updateType))) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行，数据根据类型（增加/减少）不正确，请检查后重新上传。");
                continue;
            }
            userId = agentUser.getId();
            if (checkFlag) {
                //去重
                List<AgentMaterialCost> haveUserMaterialCostList = new ArrayList<>();
                for (AgentMaterialCost materialCost : userMaterialCostList) {
                    if (null != materialCost && Objects.equals(materialCost.getGroupId(), agentGroup.getId()) && materialCost.getSchoolTerm().equals(schoolTerm) && Objects.equals(materialCost.getUserId(), userId)) {
                        haveUserMaterialCostList.add(materialCost);
                    }
                }
                if (CollectionUtils.isEmpty(haveUserMaterialCostList)) {
                    AgentMaterialCostVo materialCostVo = constractAgentMaterialCostVo(2, schoolTerm, agentGroup.getId(), money, userId, updateType, content);


                    //判断该部门该学期是否导入过人员预算
                    List<AgentMaterialCost> materialCostList = userMaterialCostMap.get(agentUser.getId());
                    if (CollectionUtils.isNotEmpty(materialCostList)) {
                        materialCostList = materialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(schoolTerm)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(materialCostList)) {
                            userUpdateMaterialCostList.add(materialCostVo);
                        } else {
                            userMaterialCostList.add(materialCostVo);
                        }
                    } else {
                        userMaterialCostList.add(materialCostVo);
                    }
                    allUserMaterialCostList.add(materialCostVo);
                    String key = agentGroup.getId() + "-" + schoolTerm;
                    Object value = userMaterialCostSumMap.get(key);
                    if (null != value) {
                        if ("增加".equals(updateType)) {
                            userMaterialCostSumMap.put(key, MathUtils.doubleAdd(SafeConverter.toDouble(value), money));
                        } else {
                            userMaterialCostSumMap.put(key, MathUtils.doubleSub(SafeConverter.toDouble(value), money));
                        }
                    } else {
                        if ("增加".equals(updateType)) {
                            userMaterialCostSumMap.put(key, money);
                        } else {
                            userMaterialCostSumMap.put(key, -money);
                        }

                    }
                } else {//有重复的提示也不让导入了
                    checkFlag = false;
                    errorInfoList.add("第" + rowNo + "行,存在多条记录，无法导入。");
                    continue;
                }

            }
        }
        //判断每个部门+学期，人员余额是否大于部门预算
        for (String key : userMaterialCostSumMap.keySet()) {
            if (StringUtils.isNotBlank(key)) {
//                Object groupValue = groupMaterialCostSumMap.get(key);
                Object userValue = userMaterialCostSumMap.get(key);
                //部门+学期
                String[] keyArray = StringUtils.split(key, "-");
//                if (null == groupValue){
                //新导入没有该部门费用的情况下，判断是否已经存在该部门费用
                List<AgentMaterialCost> isHaveGroupMaterialCostList = agentMaterialCostDao.getGroupMaterialCostByGroupId(SafeConverter.toLong(keyArray[0]));
                if (CollectionUtils.isNotEmpty(isHaveGroupMaterialCostList)) {
                    AgentMaterialCost groupMaterialCost = isHaveGroupMaterialCostList.stream().filter(item -> item.getSchoolTerm().equals(ConversionUtils.toString(keyArray[1]))).findFirst().orElse(null);
                    if (null != groupMaterialCost) {
                        Double undistributedCost = groupMaterialCost.getUndistributedCost() == null ? 0 : groupMaterialCost.getUndistributedCost();
                        //部门下的所有人员的数据
                        List<AgentMaterialCostVo> workbookMaterialCostList = allUserMaterialCostList.stream().filter(item -> item.getSchoolTerm().equals(ConversionUtils.toString(keyArray[1]))).collect(Collectors.toList());
                        for (AgentMaterialCostVo workbookMaterialCost : workbookMaterialCostList) {
                            if ("增加".equals(workbookMaterialCost.getUpdateType())) {
                                if (SafeConverter.toDouble(userValue) > undistributedCost) {
                                    checkFlag = false;
                                    errorInfoList.add(keyArray[1] + ",用户" + workbookMaterialCost.getUserId() + "余额大于部门未分配，不可导入。");
                                }
                            } else {
                                List<AgentMaterialCost> materialCostList = userMaterialCostMap.get(workbookMaterialCost.getUserId());
                                if (CollectionUtils.isNotEmpty(materialCostList)) {
                                    materialCostList = materialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(workbookMaterialCost.getSchoolTerm())).collect(Collectors.toList());

                                    AgentMaterialCost userSavedMaterialCost = materialCostList.get(0);
                                    if (workbookMaterialCost.getBalance() > userSavedMaterialCost.getBalance()) {
                                        checkFlag = false;
                                        errorInfoList.add(keyArray[1] + ",用户" + workbookMaterialCost.getUserId() + "调整余额大于用户余额，不可导入。");
                                    }
                                } else {
                                    checkFlag = false;
                                    errorInfoList.add(keyArray[1] + ",用户" + workbookMaterialCost.getUserId() + "余额不存在，不可导入。");
                                }

                            }
                        }


                    } else {
                        checkFlag = false;
                        errorInfoList.add(keyArray[1] + ",没有部门预算，不可导入用户余额。");
                    }
                } else {
                    checkFlag = false;
                    errorInfoList.add(keyArray[1] + ",没有部门预算，不可导入用户余额。");
                }
            }
        }
        if (!checkFlag) {
            return MapMessage.errorMessage().add("errorInfoList", errorInfoList);
        }
        if (CollectionUtils.isEmpty(userMaterialCostList) && CollectionUtils.isEmpty(userUpdateMaterialCostList)) {
            errorInfoList.add("文件无有效数据！");
            return MapMessage.errorMessage().add("errorInfoList", errorInfoList);
        }

        mapMessage.add("userMaterialCostList", userMaterialCostList);
        mapMessage.add("userUpdateMaterialCostList", userUpdateMaterialCostList);
        return mapMessage;
    }

    //检查物料费用数据
    public MapMessage checkGroupWorkboook(XSSFWorkbook workbook) {
        MapMessage mapMessage = MapMessage.successMessage();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet == null) {
            return MapMessage.errorMessage();
        }
        List<AgentMaterialCostVo> groupMaterialCostList = new ArrayList<>();
        List<AgentMaterialCostVo> groupInsertMaterialCostList = new ArrayList<>();
        List<AgentMaterialCostVo> groupUpdateMaterialCostList = new ArrayList<>();//部门预算需更新list

        //获取部门物料费用
        Map<Long, List<AgentMaterialCost>> groupMaterialCostMap = agentMaterialCostDao.getGroupMaterialCost().stream().collect(Collectors.groupingBy(AgentMaterialCost::getGroupId));

        List<String> errorInfoList = new ArrayList<>();
        boolean checkFlag = true;
        int rowNo = 1;
        Map<String, Object> groupMaterialCostSumMap = new HashMap<>();
        while (true) {
            XSSFRow row = sheet.getRow(rowNo++);
            if (row == null) {
                break;
            }
            String schoolTerm = SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(0)));//学期
            String groupName = SafeConverter.toString(XssfUtils.getCellStringValue(row.getCell(1)));//部门
            String updateType = XssfUtils.getStringCellValue(row.getCell(2));//更新类型 增加/减少
            Double money = XssfUtils.getDoubleCellValue(row.getCell(3));//金额
            String content = XssfUtils.getStringCellValue(row.getCell(4));//备注
            if (StringUtils.isBlank(schoolTerm)) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行，无学期，请检查后重新上传。");
                continue;
            }
            if (schoolTerm.length() != 7) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行:请输入形如‘2018年春季’或 ‘2018年秋季’格式的学期。");
                continue;
            }
            int year = SafeConverter.toInt(schoolTerm.substring(0, 4));
            if (year < 2000 || year > 2050) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行:请输入形如‘2018年春季’或 ‘2018年秋季’格式的学期。");
                continue;
            }
            String endStr = schoolTerm.substring(4, 7);
            if (!endStr.equals("年春季") && !endStr.equals("年秋季")) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行:请输入形如‘2018年春季’或 ‘2018年秋季’格式的学期。");
                continue;
            }

            if (StringUtils.isEmpty(groupName)) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行,部门为空。");
                continue;
            }
            AgentGroup agentGroup = baseOrgService.getGroupByName(groupName);
            if (agentGroup == null) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行,部门不存在。");
                continue;
            }
            if (null == money) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行，金额为空。");
                continue;
            }
            if (money <= 0D) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行，金额必须大于0。");
                continue;
            }

            if (StringUtils.isBlank(updateType) || !("增加".equals(updateType) || "减少".equals(updateType))) {
                checkFlag = false;
                errorInfoList.add("第" + rowNo + "行，数据根据类型（增加/减少）不正确，请检查后重新上传。");
                continue;
            }

            boolean insertFlag = true;
            //如果是部门物料预算
            //判断该部门该学期是否导入过预算
            List<AgentMaterialCost> materialCostList = groupMaterialCostMap.get(agentGroup.getId());
            if (CollectionUtils.isNotEmpty(materialCostList)) {
                materialCostList = materialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(schoolTerm)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(materialCostList)) {
                    AgentMaterialCost groupMaterialCost = materialCostList.get(0);
                    if ("减少".equals(updateType) && money > groupMaterialCost.getUndistributedCost()) {//修改的金额大于未分配
                        checkFlag = false;
                        errorInfoList.add("第" + rowNo + "行,减少金额不能小于未分配金额。");
                        continue;
                    }
                    insertFlag = false;
                    AgentMaterialCostVo updateCostVo = constractAgentMaterialCostVo(1, schoolTerm, agentGroup.getId(), money, null, updateType, content);
                    groupUpdateMaterialCostList.add(updateCostVo);
//insertFlag
                } else {
                    if ("减少".equals(updateType)) {//数据库中不存在 不能减少
                        checkFlag = false;
                        errorInfoList.add("第" + rowNo + "行,数据库中没有对应部门记录不能减少预算。");
                        continue;
                    }
                }
            } else {
                if ("减少".equals(updateType)) {//数据库中不存在 不能减少
                    checkFlag = false;
                    errorInfoList.add("第" + rowNo + "行,数据库中没有对应部门记录不能减少预算。");
                    continue;
                }
            }
            if (checkFlag) {
                List<AgentMaterialCost> haveGroupMaterialCostList = groupMaterialCostList.stream().filter(item -> null != item && Objects.equals(item.getGroupId(), agentGroup.getId()) && item.getSchoolTerm().equals(schoolTerm)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(haveGroupMaterialCostList)) {
                    AgentMaterialCostVo materialCostVo = constractAgentMaterialCostVo(1, schoolTerm, agentGroup.getId(), money, null, updateType, content);
                    if (insertFlag) {
                        groupInsertMaterialCostList.add(materialCostVo);
                    }
                    groupMaterialCostList.add(materialCostVo);
                    String key = agentGroup.getId() + "-" + schoolTerm;
                    Object value = groupMaterialCostSumMap.get(key);
                    if (null != value) {
                        if ("增加".equals(updateType)) {
                            groupMaterialCostSumMap.put(key, MathUtils.doubleAdd(SafeConverter.toDouble(value), money));
                        } else {
                            groupMaterialCostSumMap.put(key, MathUtils.doubleSub(SafeConverter.toDouble(value), money));
                        }

                    } else {
                        if ("增加".equals(updateType)) {
                            groupMaterialCostSumMap.put(key, -money);
                        } else {
                            groupMaterialCostSumMap.put(key, -money);
                        }

                    }
                } else {
                    checkFlag = false;
                    errorInfoList.add("第" + rowNo + "行,存在多条记录，无法导入。");
                    continue;
                }
            }
        }
        if (!checkFlag) {
            return MapMessage.errorMessage().add("errorInfoList", errorInfoList);
        }
        if (CollectionUtils.isEmpty(groupInsertMaterialCostList) && CollectionUtils.isEmpty(groupUpdateMaterialCostList)) {
            errorInfoList.add("文件无有效数据！");
            return MapMessage.errorMessage().add("errorInfoList", errorInfoList);
        }
        mapMessage.add("groupMaterialCostList", groupInsertMaterialCostList);
        mapMessage.add("groupUpdateMaterialCostList", groupUpdateMaterialCostList);
        return mapMessage;
    }

    /**
     * 导入物料费用
     */
    public MapMessage importMaterialCost(XSSFWorkbook workbook, Integer templateType) {
        //更新预算
        Map<Long, List<AgentMaterialCost>> groupMaterialCostMap = agentMaterialCostDao.getGroupMaterialCost().stream().collect(Collectors.groupingBy(AgentMaterialCost::getGroupId));
        //获取人员物料费用
        Map<Long, List<AgentMaterialCost>> userMaterialCostMap = agentMaterialCostDao.getUserMaterialCost().stream().collect(Collectors.groupingBy(AgentMaterialCost::getUserId));
        if (Objects.equals(1, templateType)) {
            MapMessage checkResult = checkGroupWorkboook(workbook);
            if (!checkResult.isSuccess()) {
                return checkResult;
            }
            List<AgentMaterialCostVo> groupMaterialCostList = (List<AgentMaterialCostVo>) checkResult.get("groupMaterialCostList");
            List<AgentMaterialCostVo> groupUpdateMaterialCostList = (List<AgentMaterialCostVo>) checkResult.get("groupUpdateMaterialCostList");
            //部门物料预算数目
            Integer groupMaterialCostNum = groupMaterialCostList.size() + groupUpdateMaterialCostList.size();
//            List<AgentMaterialCost> insertList = new ArrayList<>();
            groupMaterialCostList.forEach(p -> {
                AgentMaterialCost insertMaterialCost = p.toAgentMaterialCost();
                agentMaterialCostDao.upsert(insertMaterialCost);
                AgentMaterialBalanceChangeRecord budgetRecord = generateChangeRecord(0d, p.getBudget(), p.getBudget(), "导入预算" + (StringUtils.isBlank(p.getComment()) ? "" : "/" + p.getComment()), 1, insertMaterialCost.getId(), 1, getCurrentUserId());
                agentMaterialBalanceChangeRecordDao.insert(budgetRecord);
                //部门未分配费用变动日志
                AgentMaterialBalanceChangeRecord undistributedCostRecord = generateChangeRecord(0d, p.getBudget(), p.getBudget(), "导入预算，未分配费用变动" + (StringUtils.isBlank(p.getComment()) ? "" : "/" + p.getComment()), 3, insertMaterialCost.getId(), 1, getCurrentUserId());
                agentMaterialBalanceChangeRecordDao.insert(undistributedCostRecord);
            });

            List<AgentMaterialCost> updateList = new ArrayList<>();
            List<AgentMaterialBalanceChangeRecord> balanceRecordList = new ArrayList<>();
            List<AgentMaterialBalanceChangeRecord> undistributedCostRecordList = new ArrayList<>();
            //更新物料预算
            groupUpdateMaterialCostList.forEach(p -> {
                Double preCash = 0d;
                Double afterCash = 0d;
                Double preUndistributedCost = 0D;
                Double afterUndistributedCost = 0D;
                Long groupId = p.getGroupId();
                Double budget = p.getBudget() == null ? 0 : p.getBudget();
                List<AgentMaterialCost> materialCostList = groupMaterialCostMap.get(groupId);
                if (CollectionUtils.isNotEmpty(materialCostList)) {
                    AgentMaterialCost agentMaterialCost = materialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(p.getSchoolTerm())).findFirst().orElse(null);
                    if (agentMaterialCost != null) {
                        if ("减少".equals(p.getUpdateType())) {
                            if (budget <= agentMaterialCost.getUndistributedCost()) {
                                preCash = agentMaterialCost.getBudget();
                                afterCash = MathUtils.doubleSub(agentMaterialCost.getBudget(), budget);

                                preUndistributedCost = agentMaterialCost.getUndistributedCost();
                                afterUndistributedCost = MathUtils.doubleSub(preUndistributedCost, budget);

                                agentMaterialCost.setBudget(afterCash);
                                agentMaterialCost.setUndistributedCost(MathUtils.doubleSub(agentMaterialCost.getUndistributedCost(), budget));
                                AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(preCash, afterCash, budget, "导入预算" + (StringUtils.isBlank(p.getComment()) ? "" : "/" + p.getComment()), 1, agentMaterialCost.getId(), 1, getCurrentUserId());
                                balanceRecordList.add(balanceRecord);
                                //部门未分配费用变动日志
                                AgentMaterialBalanceChangeRecord undistributedCostRecord = generateChangeRecord(preUndistributedCost, afterUndistributedCost, budget, "导入预算，部门未分配费用变动" + (StringUtils.isBlank(p.getComment()) ? "" : "/" + p.getComment()), 3, agentMaterialCost.getId(), 1, getCurrentUserId());
                                undistributedCostRecordList.add(undistributedCostRecord);
                            }
                        } else {
                            preCash = agentMaterialCost.getBudget();
                            afterCash = MathUtils.doubleAdd(agentMaterialCost.getBudget(), budget);

                            preUndistributedCost = agentMaterialCost.getUndistributedCost();
                            afterUndistributedCost = MathUtils.doubleAdd(preUndistributedCost, budget);

                            agentMaterialCost.setBudget(MathUtils.doubleAdd(agentMaterialCost.getBudget(), budget));
                            agentMaterialCost.setUndistributedCost(MathUtils.doubleAdd(agentMaterialCost.getUndistributedCost(), budget));

                            AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(preCash, afterCash, budget, "导入预算" + (StringUtils.isBlank(p.getComment()) ? "" : "/" + p.getComment()), 1, agentMaterialCost.getId(), 1, getCurrentUserId());
                            balanceRecordList.add(balanceRecord);
                            //部门未分配费用变动日志
                            AgentMaterialBalanceChangeRecord undistributedCostRecord = generateChangeRecord(preUndistributedCost, afterUndistributedCost, budget, "导入预算，部门未分配费用变动" + (StringUtils.isBlank(p.getComment()) ? "" : "/" + p.getComment()), 3, agentMaterialCost.getId(), 1, getCurrentUserId());
                            undistributedCostRecordList.add(undistributedCostRecord);
                        }
                        updateList.add(agentMaterialCost);
                    }

                }
            });
            updateList.forEach(u -> {
                agentMaterialCostDao.upsert(u);
            });
            balanceRecordList.forEach(p -> {
                agentMaterialBalanceChangeRecordDao.insert(p);
            });
            undistributedCostRecordList.forEach(item -> {
                agentMaterialBalanceChangeRecordDao.insert(item);
            });
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("userMaterialCostNum", 0);
            dataMap.put("groupMaterialCostNum", groupMaterialCostNum);
            return MapMessage.successMessage().add("dataMap", dataMap);
        } else {//更新人员余额
            //检查excel文件
            MapMessage checkResult = checkWorkboook(workbook);
            if (!checkResult.isSuccess()) {
                return checkResult;
            }
            List<AgentMaterialCostVo> userMaterialCostList = (List<AgentMaterialCostVo>) checkResult.get("userMaterialCostList");
            List<AgentMaterialCostVo> userUpdateMaterialCostList = (List<AgentMaterialCostVo>) checkResult.get("userUpdateMaterialCostList");
//            List<AgentMaterialCost> groupMaterialCostListFinal = new ArrayList<>();
//
//            Map<String,AgentMaterialCost> groupSchoolTermMaterialCostMap = new HashMap<>();
            //部门物料预算数目
            Integer groupMaterialCostNum = 0;
            Integer userMaterialCostNum = 0;
            //如果导入有人员费用信息
            if (CollectionUtils.isNotEmpty(userMaterialCostList)) {
                for (AgentMaterialCostVo item : userMaterialCostList) {
                    userMaterialCostNum += updateUserBalance(item, groupMaterialCostMap, userMaterialCostMap);
                }
            }
            if (CollectionUtils.isNotEmpty(userUpdateMaterialCostList)) {
                for (AgentMaterialCostVo vo : userUpdateMaterialCostList) {
                    userMaterialCostNum += updateUserBalance(vo, groupMaterialCostMap, userMaterialCostMap);
                }
            }
            //人员物料余额数目
            userMaterialCostList.size();
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("groupMaterialCostNum", 0);
            dataMap.put("userMaterialCostNum", userMaterialCostNum);
            return MapMessage.successMessage().add("dataMap", dataMap);
        }

    }


    public List<Map<String, Object>> getGroupMaterialCostByGroup(String groupName) {
        List<AgentMaterialCost> agentMaterialCostList = new ArrayList<>();
        if (StringUtils.isBlank(groupName)) {
            agentMaterialCostList.addAll(agentMaterialCostDao.getGroupMaterialCost());
        } else {
            AgentGroup agentGroup = baseOrgService.getGroupByName(groupName);
            if (null != agentGroup) {
                agentMaterialCostList.addAll(agentMaterialCostDao.getGroupMaterialCostByGroupId(agentGroup.getId()));
            }
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        Set<Long> groupIds = agentMaterialCostList.stream().map(AgentMaterialCost::getGroupId).collect(Collectors.toSet());
        Map<Long, AgentGroup> groupMap = baseOrgService.getGroupByIds(groupIds).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
        agentMaterialCostList.forEach(item -> {
            if (null != item) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("id", item.getId());
                dataMap.put("schoolTerm", item.getSchoolTerm());
                AgentGroup group = groupMap.get(item.getGroupId());
                if (null != group) {
                    dataMap.put("groupId", group.getId());
                    dataMap.put("groupName", group.getGroupName());
                    if (StringUtils.isNotBlank(group.getServiceType())) {
                        String[] arr = group.getServiceType().split(",");
                        for (int i = 0; i < arr.length; i++) {
                            AgentServiceType agentServiceType = AgentServiceType.nameOf(arr[i]);
                            arr[i] = agentServiceType.getTypeName();
                        }
                        dataMap.put("serviceType", StringUtils.join(arr, ","));
                    } else
                        dataMap.put("serviceType", "");

                    dataMap.put("groupStatus", group.getDisabled() == Boolean.FALSE ? "有效" : "无效");
                    if (group.getDisabled() == Boolean.TRUE) {
                        dataMap.put("groupDisableTime", (DateUtils.dateToString(group.getUpdateDatetime(), DateUtils.FORMAT_SQL_DATETIME)));
                    } else {
                        dataMap.put("groupDisableTime", "");
                    }
                } else {
                    AgentGroup disabledGroup = baseOrgService.loadDisabledGroup(item.getGroupId());
                    if (disabledGroup != null) {
                        dataMap.put("groupId", disabledGroup.getId());
                        dataMap.put("groupName", disabledGroup.getGroupName());
                        String serviceType = disabledGroup.getServiceType();
                        if (StringUtils.isNotBlank(serviceType)) {
                            String[] arr = serviceType.split(",");
                            for (int i = 0; i < arr.length; i++) {
                                AgentServiceType agentServiceType = AgentServiceType.nameOf(arr[i]);
                                arr[i] = agentServiceType.getTypeName();
                            }
                            dataMap.put("serviceType", StringUtils.join(arr, ","));
                        } else {
                            dataMap.put("serviceType", "");
                        }
                        dataMap.put("groupDisableTime", DateUtils.dateToString(disabledGroup.getUpdateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
                    } else {
                        dataMap.put("groupId", 0L);
                        dataMap.put("groupName", "");
                        dataMap.put("serviceType", "");
                        dataMap.put("groupDisableTime", "");
                    }
                    dataMap.put("groupStatus", "部门删除-无效");
                }
                dataMap.put("budget", null == item.getBudget() ? 0 : item.getBudget());

                //人员余额
                Double userMaterialBalance = getUserMaterialBalanceByGroupIdAndAchoolTerm(item.getGroupId(), item.getSchoolTerm());
                //部门未分配余额
                Double undistributedCost = item.getUndistributedCost() == null ? 0 : item.getUndistributedCost();
                //部门余额=人员余额+部门未分配余额
                dataMap.put("balance", MathUtils.doubleAdd(userMaterialBalance, undistributedCost));
                dataList.add(dataMap);
            }
        });
        return dataList;
    }

    public Map<String, Object> getUserMaterialCost(Long groupId, String schoolTerm) {
        AgentGroup group = baseOrgService.getGroupById(groupId);
        List<Map<String, Object>> dataList = new ArrayList<>();
        //人员物料费用信息
        List<AgentMaterialCost> userMaterialCostList = agentMaterialCostDao.getUserMaterialCostByGroupId(groupId);
        userMaterialCostList = userMaterialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(schoolTerm)).collect(Collectors.toList());
        Map<Long, AgentMaterialCost> userMaterialCostMap = userMaterialCostList.stream().collect(Collectors.toMap(AgentMaterialCost::getUserId, Function.identity()));
        //部门物料费用信息
        List<AgentMaterialCost> groupMaterialCostList = agentMaterialCostDao.getGroupMaterialCostByGroupId(groupId);
        AgentMaterialCost materialCost = groupMaterialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(schoolTerm)).findFirst().orElse(null);
        Map<String, Object> dataMap = new HashMap<>();
        if (null != group) {
            dataMap.put("groupId", group.getId());
            dataMap.put("groupName", group.getGroupName());
        }
        dataMap.put("schoolTerm", materialCost.getSchoolTerm());
        dataMap.put("groupBudget", null == materialCost.getBudget() ? 0 : materialCost.getBudget());
        //该部门该学期下，人员余额总额
        Double userBalance = 0D;
        for (AgentMaterialCost userMaterialCost : userMaterialCostList) {
            userBalance = MathUtils.doubleAdd(userBalance, (null == userMaterialCost.getBalance() ? 0 : userMaterialCost.getBalance()));
        }
        //部门余额=人员余额+部门未分配
        Double undistributedCost = (null == materialCost.getUndistributedCost() ? 0 : materialCost.getUndistributedCost());
        dataMap.put("groupBalance", MathUtils.doubleAdd(userBalance, undistributedCost));

        List<Long> groupUserIds = baseOrgService.getGroupUserIds(groupId);
        Map<Long, AgentUser> userMap = baseOrgService.getUsers(groupUserIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
        groupUserIds.forEach(item -> {
            if (null != item) {
                Map<String, Object> materialCostMap = new HashMap<>();
                AgentMaterialCost agentMaterialCost = userMaterialCostMap.get(item);
                if (null != agentMaterialCost) {
                    materialCostMap.put("id", agentMaterialCost.getId());
                    materialCostMap.put("userBalance", null == agentMaterialCost.getBalance() ? 0 : agentMaterialCost.getBalance());
                } else {
                    materialCostMap.put("userId", item);
                    materialCostMap.put("userBalance", 0);
                }
                AgentUser agentUser = userMap.get(item);
                materialCostMap.put("userName", agentUser.getRealName());
                dataList.add(materialCostMap);
            }
        });
        Map<String, Object> materialCostMap = new HashMap<>();
        materialCostMap.put("userName", "未分配");
        materialCostMap.put("userBalance", undistributedCost);
        dataList.add(materialCostMap);
        dataMap.put("dataList", dataList);
        return dataMap;
    }

    public MapMessage changeGroupBudget(String id, int modifyType, double modifyCount, String modifyReason) {
        if (StringUtils.isEmpty(modifyReason)) {
            return MapMessage.errorMessage("请填写修改原因");
        }
        AgentMaterialCost agentMaterialCost = agentMaterialCostDao.load(id);
        if (agentMaterialCost == null) {
            return MapMessage.errorMessage("没有该记录");
        }
        double beforeBudget = null == agentMaterialCost.getBudget() ? 0 : agentMaterialCost.getBudget();
        double beforeUndistributedCost = null == agentMaterialCost.getUndistributedCost() ? 0 : agentMaterialCost.getUndistributedCost();
        //增加预算
        if (modifyType == 1) {
            double budgetResult = MathUtils.doubleAdd(beforeBudget, modifyCount);
            double undistributedCostResult = MathUtils.doubleAdd(beforeUndistributedCost, modifyCount);
            agentMaterialCost.setBudget(budgetResult);
            agentMaterialCost.setUndistributedCost(undistributedCostResult);
        } else if (modifyType == 2) {
            //部门预算减少时，减少金额不可大于部门未分配的物料经费
            //人员总余额
            Double undistributedCost = (null == agentMaterialCost.getUndistributedCost() ? 0 : agentMaterialCost.getUndistributedCost());

//            if (modifyCount > MathUtils.doubleAdd(undistributedCost,userBalance)){
            if (modifyCount > undistributedCost) {
                return MapMessage.errorMessage("减少的预算不能大于余额");
            }
            //如果减少金额<=未分配
//            if (modifyCount <= undistributedCost){
            double budgetResult = MathUtils.doubleSub(beforeBudget, modifyCount);
            double undistributedCostResult = MathUtils.doubleSub(beforeUndistributedCost, modifyCount);
            agentMaterialCost.setBudget(budgetResult);
            agentMaterialCost.setUndistributedCost(undistributedCostResult);
            //如果减少金额大于未分配，小于部门余额，人员余额都调整到未分配
//            }else {
//                //人员物料费用信息
//                List<AgentMaterialCost> userMaterialCostList = agentMaterialCostDao.getUserMaterialCostByGroupId(agentMaterialCost.getGroupId());
//                userMaterialCostList = userMaterialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(agentMaterialCost.getSchoolTerm())).collect(Collectors.toList());
//                //该部门该学期下，人员余额都置零
//                for (AgentMaterialCost userMaterialCost : userMaterialCostList){
//                    if (null != userMaterialCost && null != userMaterialCost.getBalance() && userMaterialCost.getBalance() != 0D){
//                        AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(userMaterialCost.getBalance(), 0D, userMaterialCost.getBalance(), modifyReason, 2, userMaterialCost.getId(),1,getCurrentUserId());
//                        agentMaterialBalanceChangeRecordDao.insert(balanceRecord);
//                        userMaterialCost.setBalance(0D);
//                        agentMaterialCostDao.replace(userMaterialCost);
//                    }
//                }
//
//                double budgetResult = MathUtils.doubleSub(beforeBudget, modifyCount);
//                //未分配=之前未分配+人员余额-减少费用
//                double undistributedCostResult = MathUtils.doubleSub(MathUtils.doubleAdd(beforeUndistributedCost,userBalance), modifyCount);
//                agentMaterialCost.setBudget(budgetResult);
//                agentMaterialCost.setUndistributedCost(undistributedCostResult);
//
//            }
        }
        agentMaterialCostDao.replace(agentMaterialCost);
        AgentMaterialBalanceChangeRecord budgetRecord = generateChangeRecord(beforeBudget, agentMaterialCost.getBudget(), modifyCount, modifyReason, 1, agentMaterialCost.getId(), 1, getCurrentUserId());
        AgentMaterialBalanceChangeRecord undistributedCostRecord = generateChangeRecord(beforeUndistributedCost, agentMaterialCost.getUndistributedCost(), modifyCount, modifyReason, 3, agentMaterialCost.getId(), 1, getCurrentUserId());
        agentMaterialBalanceChangeRecordDao.insert(budgetRecord);
        agentMaterialBalanceChangeRecordDao.insert(undistributedCostRecord);
        return MapMessage.successMessage();
    }

    public MapMessage changeUserBalance(String id, Long userId, int modifyType, double modifyCount, String modifyReason, Long operatorId, String schoolTerm) {
        if (!getCurrentUser().isCountryManager()) {
            return MapMessage.errorMessage("只有是全国总监才能调整物料余额");
        }
        if (StringUtils.isEmpty(modifyReason)) {
            return MapMessage.errorMessage("请填写修改原因");
        }

        Double beforeBalance = 0D;
        Double afterBalance = 0D;

        Double beforeUndistributedCost = 0D;
        Double afterUndistributedCost = 0D;
        String groupMaterialCostId = "";
        //修改人员余额
        if (StringUtils.isNotBlank(id)) {
            AgentMaterialCost userMaterialCost = agentMaterialCostDao.load(id);
            if (userMaterialCost == null) {
                return MapMessage.errorMessage("没有该记录");
            }
            Long groupId = userMaterialCost.getGroupId();
            List<AgentMaterialCost> groupMaterialCostList = agentMaterialCostDao.getGroupMaterialCostByGroupId(groupId);
            AgentMaterialCost groupMaterialCost = groupMaterialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(userMaterialCost.getSchoolTerm())).findFirst().orElse(null);
            groupMaterialCostId = groupMaterialCost.getId();
            //部门未分配
            beforeUndistributedCost = null == groupMaterialCost.getUndistributedCost() ? 0 : groupMaterialCost.getUndistributedCost();

            beforeBalance = null == userMaterialCost.getBalance() ? 0 : userMaterialCost.getBalance();

            //增加余额
            if (modifyType == 1) {
                //增加的余额大于部门未分配余额
                if (modifyCount > beforeUndistributedCost) {
                    return MapMessage.errorMessage("增加的余额大于部门未分配余额！");
                }
                //人员余额+增加余额
                afterBalance = MathUtils.doubleAdd(beforeBalance, modifyCount);
                userMaterialCost.setBalance(afterBalance);
                //部门未分配余额-增加余额
                afterUndistributedCost = MathUtils.doubleSub(beforeUndistributedCost, modifyCount);
                groupMaterialCost.setUndistributedCost(afterUndistributedCost);
                //减少余额
            } else if (modifyType == 2) {
                //人员余额
                if (modifyCount > beforeBalance) {
                    return MapMessage.errorMessage("减少的余额不能大于余额");
                }
                afterBalance = MathUtils.doubleSub(beforeBalance, modifyCount);
                //人员余额-减少余额
                userMaterialCost.setBalance(afterBalance);
                //部门未分配余额+减少余额
                afterUndistributedCost = MathUtils.doubleAdd(beforeUndistributedCost, modifyCount);
                groupMaterialCost.setUndistributedCost(afterUndistributedCost);
            }
            agentMaterialCostDao.replace(userMaterialCost);
            agentMaterialCostDao.replace(groupMaterialCost);
            //增加人员余额
        } else {
            if (userId == 0L) {
                return MapMessage.errorMessage("该人员不存在！");
            }
            //增加余额
            if (modifyType == 1) {
                AgentGroupUser agentGroupUser = baseOrgService.getGroupUserByUser(userId).stream().findFirst().orElse(null);
                List<AgentMaterialCost> groupMaterialCostList = agentMaterialCostDao.getGroupMaterialCostByGroupId(agentGroupUser.getGroupId());
                AgentMaterialCost groupMaterialCost = groupMaterialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(schoolTerm)).findFirst().orElse(null);
                groupMaterialCostId = groupMaterialCost.getId();
                //部门未分配
                beforeUndistributedCost = null == groupMaterialCost.getUndistributedCost() ? 0 : groupMaterialCost.getUndistributedCost();
                //增加的余额大于部门未分配余额
                if (modifyCount > beforeUndistributedCost) {
                    return MapMessage.errorMessage("增加的余额大于部门未分配余额！");
                }
                afterBalance = modifyCount;
                AgentMaterialCost agentMaterialCost = new AgentMaterialCost();
                agentMaterialCost.setMaterialType(2);
                agentMaterialCost.setSchoolTerm(schoolTerm);
                agentMaterialCost.setBalance(modifyCount);
                agentMaterialCost.setUserId(userId);
                agentMaterialCost.setGroupId(agentGroupUser.getGroupId());
                agentMaterialCost.setDisabled(false);
                agentMaterialCostDao.insert(agentMaterialCost);
                id = agentMaterialCost.getId();
                //部门未分配余额-增加余额
                afterUndistributedCost = MathUtils.doubleSub(beforeUndistributedCost, modifyCount);
                groupMaterialCost.setUndistributedCost(afterUndistributedCost);
                agentMaterialCostDao.replace(groupMaterialCost);
            } else if (modifyType == 2) {
                return MapMessage.errorMessage("不可减少余额！");
            }
        }
        //人员余额变动日志
        AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(beforeBalance, afterBalance, modifyCount, modifyReason, 2, id, 1, operatorId);
        agentMaterialBalanceChangeRecordDao.insert(balanceRecord);
        //部门未分配变动日志
        AgentMaterialBalanceChangeRecord undistributedCostRecord = generateChangeRecord(beforeUndistributedCost, afterUndistributedCost, modifyCount, modifyReason, 3, groupMaterialCostId, 1, operatorId);
        agentMaterialBalanceChangeRecordDao.insert(undistributedCostRecord);
        return MapMessage.successMessage();
    }

    /**
     * 根据groupId和schoolTerm获取人员总余额
     *
     * @param groupId
     * @param schoolTerm
     * @return
     */
    public Double getUserMaterialBalanceByGroupIdAndAchoolTerm(Long groupId, String schoolTerm) {
        //人员物料费用信息
        List<AgentMaterialCost> userMaterialCostList = agentMaterialCostDao.getUserMaterialCostByGroupId(groupId);
        userMaterialCostList = userMaterialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(schoolTerm)).collect(Collectors.toList());
        //该部门该学期下，人员余额总额
        Double userBalance = 0D;
        for (AgentMaterialCost userMaterialCost : userMaterialCostList) {
            userBalance = MathUtils.doubleAdd(userBalance, (null == userMaterialCost.getBalance() ? 0 : userMaterialCost.getBalance()));
        }
        return userBalance;
    }

    public List<AgentMaterialCostExportData> getMaterialCostExportData(String groupName) {
        List<AgentMaterialCost> agentMaterialCostList = new ArrayList<>();
        if (StringUtils.isBlank(groupName)) {
            agentMaterialCostList.addAll(agentMaterialCostDao.getGroupMaterialCost());
        } else {
            AgentGroup agentGroup = baseOrgService.getGroupByName(groupName);
            if (null != agentGroup) {
                agentMaterialCostList.addAll(agentMaterialCostDao.getGroupMaterialCostByGroupId(agentGroup.getId()));
            }
        }
        List<AgentMaterialCostExportData> dataList = new ArrayList<>();
        //部门信息
        Set<Long> groupIds = agentMaterialCostList.stream().map(AgentMaterialCost::getGroupId).collect(Collectors.toSet());
        Map<Long, AgentGroup> groupMap = baseOrgService.getGroupByIds(groupIds).stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
        //部门费用
        Map<Long, List<AgentMaterialCost>> groupMaterialCostMap = agentMaterialCostList.stream().collect(Collectors.groupingBy(AgentMaterialCost::getGroupId));
        Map<Long, List<AgentGroupUser>> groupUserMap = agentGroupUserLoaderClient.findByGroupIds(groupIds);


        //人员费用
        Map<Long, List<AgentMaterialCost>> groupUserMaterialCostMap = agentMaterialCostDao.getUserMaterialCostByGroupIds(groupIds);
        groupIds.forEach(groupId -> {
            List<AgentMaterialCost> materialCostList = groupMaterialCostMap.get(groupId);
            if (CollectionUtils.isNotEmpty(materialCostList)) {
                materialCostList.forEach(item -> {
                    AgentGroup group = groupMap.get(item.getGroupId());
                    AgentMaterialCostExportData agentMaterialCostExportData = new AgentMaterialCostExportData();
                    agentMaterialCostExportData.setGroupBudget(item.getBudget());
                    agentMaterialCostExportData.setSchoolTerm(item.getSchoolTerm());
                    //人员余额
                    Double userMaterialBalance = getUserMaterialBalanceByGroupIdAndAchoolTerm(item.getGroupId(), item.getSchoolTerm());
                    //部门未分配余额
                    Double undistributedCost = item.getUndistributedCost() == null ? 0 : item.getUndistributedCost();
                    //部门余额=人员余额+部门未分配余额
                    agentMaterialCostExportData.setGroupBalance(MathUtils.doubleAdd(userMaterialBalance, undistributedCost));
                    if (null != group) {
                        agentMaterialCostExportData.setGroupName(group.getGroupName());
                        if (StringUtils.isNotBlank(group.getServiceType())) {
                            String[] arr = group.getServiceType().split(",");
                            for (int i = 0; i < arr.length; i++) {
                                AgentServiceType agentServiceType = AgentServiceType.nameOf(arr[i]);
                                arr[i] = agentServiceType.getTypeName();
                            }
                            agentMaterialCostExportData.setServiceType(StringUtils.join(arr, ","));
                        }
                        agentMaterialCostExportData.setGroupStatus(group.getDisabled() == Boolean.FALSE ? "有效" : "无效");
                        if (group.getDisabled() == Boolean.TRUE) {
                            agentMaterialCostExportData.setGroupDisableTime(DateUtils.dateToString(group.getUpdateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
                        }

                        List<Long> groupUserIds = baseOrgService.getGroupUserIds(groupId);
                        Map<Long, AgentUser> userMap = baseOrgService.getUsers(groupUserIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
                        List<AgentMaterialCost> userMaterialCostList = groupUserMaterialCostMap.get(groupId);
                        if (CollectionUtils.isNotEmpty(userMaterialCostList)) {
                            userMaterialCostList = userMaterialCostList.stream().filter(p -> p.getSchoolTerm().equals(item.getSchoolTerm())).collect(Collectors.toList());
                            userMaterialCostList.forEach(p -> {
                                AgentMaterialCostExportData materialCostExportData = new AgentMaterialCostExportData();
                                try {
                                    BeanUtils.copyProperties(materialCostExportData, agentMaterialCostExportData);
                                } catch (Exception e) {
                                }
                                AgentUser agentUser = userMap.get(p.getUserId());
                                if (agentUser == null) {
                                    agentUser = baseOrgService.getUserIncludeDel(p.getUserId());
                                }
                                materialCostExportData.setUserName(agentUser == null ? "" : agentUser.getRealName());

                                List<AgentGroupUser> agentGroupUsers = groupUserMap.get(groupId);
                                Map<Long, AgentGroupUser> agentGroupUserMap = agentGroupUsers.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, Function.identity()));

                                AgentGroupUser agentGroupUser = agentGroupUserMap.get(p.getUserId());
                                if (null != agentGroupUser) {
                                    materialCostExportData.setAgentRoleType(agentGroupUser.getUserRoleType());
                                }
                                materialCostExportData.setUserBalance(p.getBalance());
                                dataList.add(materialCostExportData);
                            });
                        }
                    } else {
                        group = baseOrgService.loadDisabledGroup(item.getGroupId());
                        if (group != null) {
                            agentMaterialCostExportData.setGroupName(group.getGroupName());
                            agentMaterialCostExportData.setServiceType(getGroupServiceType(group));
                            agentMaterialCostExportData.setGroupStatus(group.getDisabled() == Boolean.FALSE ? "有效" : "部门删除-无效");
                            if (group.getDisabled() == Boolean.TRUE) {
                                agentMaterialCostExportData.setGroupDisableTime(DateUtils.dateToString(group.getUpdateDatetime(), DateUtils.FORMAT_SQL_DATETIME));
                            }
                            List<Long> groupUserIds = baseOrgService.getGroupUserIds(groupId);
                            Map<Long, AgentUser> userMap = baseOrgService.getUsers(groupUserIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
                            List<AgentMaterialCost> userMaterialCostList = groupUserMaterialCostMap.get(groupId);
                            if (CollectionUtils.isNotEmpty(userMaterialCostList)) {
                                userMaterialCostList = userMaterialCostList.stream().filter(p -> p.getSchoolTerm().equals(item.getSchoolTerm())).collect(Collectors.toList());
                                userMaterialCostList.forEach(p -> {
                                    AgentMaterialCostExportData materialCostExportData = new AgentMaterialCostExportData();
                                    try {
                                        BeanUtils.copyProperties(materialCostExportData, agentMaterialCostExportData);
                                    } catch (Exception e) {
                                    }
                                    AgentUser agentUser = userMap.get(p.getUserId());
                                    if (agentUser == null) {
                                        agentUser = baseOrgService.getUserIncludeDel(p.getUserId());
                                    }
                                    materialCostExportData.setUserName(agentUser == null ? "" : agentUser.getRealName());
                                    materialCostExportData.setUserName(agentUser != null ? agentUser.getRealName() : "");
                                    List<AgentGroupUser> agentGroupUsers = groupUserMap.get(groupId);
                                    if (CollectionUtils.isNotEmpty(agentGroupUsers)) {
                                        Map<Long, AgentGroupUser> agentGroupUserMap = agentGroupUsers.stream().collect(Collectors.toMap(AgentGroupUser::getUserId, Function.identity()));
                                        AgentGroupUser agentGroupUser = agentGroupUserMap.get(p.getUserId());
                                        if (null != agentGroupUser) {
                                            materialCostExportData.setAgentRoleType(agentGroupUser.getUserRoleType());
                                        }
                                    }

                                    materialCostExportData.setUserBalance(p.getBalance());
                                    dataList.add(materialCostExportData);
                                });
                            }
                        } else {
                            agentMaterialCostExportData.setGroupName("");
                            agentMaterialCostExportData.setServiceType("");
                            agentMaterialCostExportData.setGroupStatus("部门不存在-无效");
                            agentMaterialCostExportData.setGroupDisableTime("");
                            dataList.add(agentMaterialCostExportData);
                        }
                    }
                    //未分配
                    AgentMaterialCostExportData materialCostExportData = new AgentMaterialCostExportData();
                    try {
                        BeanUtils.copyProperties(materialCostExportData, agentMaterialCostExportData);
                    } catch (Exception e) {
                    }
                    materialCostExportData.setUserName("未分配");
                    materialCostExportData.setUserBalance(undistributedCost);
                    dataList.add(materialCostExportData);
                });
            }

        });
        return dataList;
    }

    public AgentMaterialCost getUserMaterialCostByUserId(Long userId) {
        List<AgentMaterialCost> materialCostList = agentMaterialCostDao.getUserMaterialCostByUserId(userId);
        if (CollectionUtils.isNotEmpty(materialCostList)) {
            materialCostList.sort((o1, o2) -> {
                return 0 - o1.getCreateTime().compareTo(o2.getCreateTime());
            });
            return materialCostList.get(0);
        }
        return null;
    }

    public AgentMaterialCost getGroupMaterialCostByGroupId(Long groupId) {
        List<AgentMaterialCost> materialCostList = agentMaterialCostDao.getGroupMaterialCostByGroupId(groupId);
        if (CollectionUtils.isNotEmpty(materialCostList)) {
            materialCostList.sort((o1, o2) -> {
                return 0 - o1.getCreateTime().compareTo(o2.getCreateTime());
            });
            return materialCostList.get(0);
        }
        return null;
    }


    public List<Map<String, Object>> getUserMaterialCostByGroup(String materialCostId) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        AgentMaterialCost materialCost = agentMaterialCostDao.load(materialCostId);
        //部门余额=人员余额+部门未分配
        Double undistributedCost = (null == materialCost.getUndistributedCost() ? 0 : materialCost.getUndistributedCost());
        //人员物料费用信息
        List<AgentMaterialCost> userMaterialCostList = agentMaterialCostDao.getUserMaterialCostByGroupId(materialCost.getGroupId());
        userMaterialCostList = userMaterialCostList.stream().filter(item -> null != item && item.getSchoolTerm().equals(materialCost.getSchoolTerm())).collect(Collectors.toList());
        Map<Long, AgentMaterialCost> userMaterialCostMap = userMaterialCostList.stream().collect(Collectors.toMap(AgentMaterialCost::getUserId, Function.identity()));
        List<Long> groupUserIds = baseOrgService.getGroupUserIds(materialCost.getGroupId());
        Map<Long, AgentUser> userMap = baseOrgService.getUsers(groupUserIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity()));
        groupUserIds.forEach(item -> {
            if (null != item) {
                Map<String, Object> materialCostMap = new HashMap<>();
                AgentMaterialCost agentMaterialCost = userMaterialCostMap.get(item);
                if (null != agentMaterialCost) {
                    materialCostMap.put("id", agentMaterialCost.getId());
                    materialCostMap.put("userId", agentMaterialCost.getUserId());
                    materialCostMap.put("userBalance", null == agentMaterialCost.getBalance() ? 0 : agentMaterialCost.getBalance());
                } else {
                    materialCostMap.put("userId", item);
                    materialCostMap.put("userBalance", 0);
                }
                AgentUser agentUser = userMap.get(item);
                materialCostMap.put("userName", agentUser.getRealName());
                dataList.add(materialCostMap);
            }
        });
        Map<String, Object> materialCostMap = new HashMap<>();
        materialCostMap.put("userName", "未分配");
        materialCostMap.put("userBalance", undistributedCost);
        dataList.add(materialCostMap);
        return dataList;
    }


    public MapMessage distributeMaterialCost(String materialCostId, Long userId, Double distributeMaterialCost) {
        if (StringUtils.isBlank(materialCostId)) {
            return MapMessage.errorMessage("物料费用ID不正确！");
        }
        if (userId == 0L) {
            return MapMessage.errorMessage("请选择分配人员！");
        }
        if (distributeMaterialCost == 0D) {
            return MapMessage.errorMessage("请填写分配金额！");
        }
        AgentMaterialCost groupMaterialCost = agentMaterialCostDao.load(materialCostId);
        if (null == groupMaterialCost) {
            return MapMessage.errorMessage("物料费用不存在！");
        }
        //部门未分配
        Double undistributedCost = (null == groupMaterialCost.getUndistributedCost() ? 0 : groupMaterialCost.getUndistributedCost());
        if (distributeMaterialCost > undistributedCost) {
            return MapMessage.errorMessage("分配金额大于部门未分配余额！");
        }
        Double beforeBalance = 0D;
        Double afterBalance = 0D;
        //人员物料费用信息
        AgentMaterialCost userMaterialCost = agentMaterialCostDao.getUserMaterialCostByUserId(userId)
                .stream()
                .filter(item -> null != item && Objects.equals(item.getGroupId(), groupMaterialCost.getGroupId()) && item.getSchoolTerm().equals(groupMaterialCost.getSchoolTerm()))
                .findFirst().orElse(null);
        String materialBudgetId = "";
        //修改人员物料余额
        if (null != userMaterialCost) {
            materialBudgetId = userMaterialCost.getId();
            //分配前余额
            beforeBalance = userMaterialCost.getBalance();
            //人员余额+增加余额
            afterBalance = MathUtils.doubleAdd(beforeBalance, distributeMaterialCost);
            userMaterialCost.setBalance(afterBalance);

            agentMaterialCostDao.replace(userMaterialCost);
            //增加人员物料费用信息
        } else {
            afterBalance = distributeMaterialCost;
            AgentMaterialCost agentMaterialCost = new AgentMaterialCost();
            agentMaterialCost.setMaterialType(2);
            agentMaterialCost.setSchoolTerm(groupMaterialCost.getSchoolTerm());
            agentMaterialCost.setBalance(distributeMaterialCost);
            agentMaterialCost.setUserId(userId);
            agentMaterialCost.setGroupId(groupMaterialCost.getGroupId());
            agentMaterialCost.setDisabled(false);
            agentMaterialCostDao.insert(agentMaterialCost);

            materialBudgetId = agentMaterialCost.getId();
        }
        //部门未分配余额-增加余额
        groupMaterialCost.setUndistributedCost(MathUtils.doubleSub(undistributedCost, distributeMaterialCost));
        agentMaterialCostDao.replace(groupMaterialCost);
        //调整记录
        AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(beforeBalance, afterBalance, distributeMaterialCost, "", 2, materialBudgetId, 1, getCurrentUserId());
        agentMaterialBalanceChangeRecordDao.insert(balanceRecord);

        return MapMessage.successMessage();
    }

    /**
     * 部门中的人员账号变更，不在此部门时，将该人员名下剩余的物料费用划归到部门的“未分配”费用中
     *
     * @param oldGroupId
     * @param userId
     */
    public void changeUserBalanceToGroup(Long oldGroupId, Long userId, String modifyReason) {
        List<AgentMaterialCost> groupMaterialCostList = agentMaterialCostDao.getGroupMaterialCostByGroupId(oldGroupId);
        List<AgentMaterialCost> userMaterialCostList = agentMaterialCostDao.getUserMaterialCostByUserId(userId);

        if (CollectionUtils.isNotEmpty(groupMaterialCostList) && CollectionUtils.isNotEmpty(userMaterialCostList)) {
            userMaterialCostList = userMaterialCostList.stream().filter(p -> Objects.equals(p.getGroupId(), oldGroupId)).collect(Collectors.toList());
            userMaterialCostList.forEach(item -> {
                if (null != item) {
                    AgentMaterialCost groupMaterialCost = groupMaterialCostList.stream().filter(p -> Objects.equals(p.getGroupId(), item.getGroupId()) && p.getSchoolTerm().equals(item.getSchoolTerm())).findFirst().orElse(null);
                    if (null != groupMaterialCost) {
                        Double undistributedCost = groupMaterialCost.getUndistributedCost() == null ? 0 : groupMaterialCost.getUndistributedCost();
                        double afterCash = MathUtils.doubleAdd(undistributedCost, item.getBalance());

                        //部门未分配费用变动日志
                        AgentMaterialBalanceChangeRecord undistributedCostRecord = generateChangeRecord(undistributedCost, afterCash, item.getBalance(), modifyReason, 3, groupMaterialCost.getId(), 1, getCurrentUserId());
                        agentMaterialBalanceChangeRecordDao.insert(undistributedCostRecord);

                        //人员余额变动日志
                        AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(item.getBalance(), 0D, item.getBalance(), modifyReason, 2, item.getId(), 1, getCurrentUserId());
                        agentMaterialBalanceChangeRecordDao.insert(balanceRecord);

                        //将该人员名下剩余的物料费用划归到部门的“未分配”费用中
                        groupMaterialCost.setUndistributedCost(afterCash);
                        agentMaterialCostDao.replace(groupMaterialCost);

                        //人员名下剩余物料费用清零，并删除
                        item.setBalance(0D);
                        item.setDisabled(true);
                        agentMaterialCostDao.replace(item);
                    }
                }
            });
        }
    }

    //    /**
//     * 迁移物料费用历史数据到新表
//     */
//    public MapMessage moveHistoryData(){
//        List<AgentMaterialBudget> userMaterialBudgetList = new ArrayList<>();
//        Map<Long, List<AgentMaterialBudget>> allMaterialBudgets = agentMaterialBudgetDao.getAllMaterialBudgets();
//        for (Long userId : allMaterialBudgets.keySet()){
//            //拼装人员物料费用
//            userMaterialBudgetList.addAll(allMaterialBudgets.get(userId));
//        }
//
//        List<AgentMaterialCost> userMaterialCostList = new ArrayList<>();
//        List<AgentMaterialCost> groupMaterialCostList = new ArrayList<>();
//
//        Map<String,Double> groupSchoolTermBudgetMap = new HashMap<>();
//        Map<String,Double> groupSchoolTermUndistributedCostMap = new HashMap<>();
//
//        userMaterialBudgetList.forEach(item -> {
//            //获取人员对应部门ID
//            AgentGroupUser agentGroupUser = baseOrgService.getGroupUserByUser(item.getUserId()).stream().findFirst().orElse(null);
//            Long groupId = 0L;
//            if (null != agentGroupUser){
//                groupId = agentGroupUser.getGroupId();
//            }
//            //学期
//            String semester = item.getSemester();
//            //人员物料预算
//            Double userBudget = item.getBudget();
//            //当前物料预算
//            Double userBudgetCurrent = userBudget;
//            //人员物料余额
//            Double userBalance = item.getBalance();
//            //当前物料余额
//            Double userBalanceCurrent = userBalance;
//
//            //拼装人员物料费用信息
//            AgentMaterialCost userMaterialCost = new AgentMaterialCost();
//            userMaterialCost.setId(item.getId());
//            userMaterialCost.setMaterialType(2);
//            userMaterialCost.setSchoolTerm(semester);
//            userMaterialCost.setGroupId(groupId);
//            userMaterialCost.setUserId(item.getUserId());
//            userMaterialCost.setBalance(userBalance);//物料余额
//            userMaterialCost.setDisabled(false);
//            userMaterialCostList.add(userMaterialCost);
//
//
//            //拼装部门学期目前预算map(groupId+schoolTerm : groupBudget)
//            String key = groupId + "-" + semester;
//            Double groupBudget = groupSchoolTermBudgetMap.get(key);
//            if (null != groupBudget){
//                groupSchoolTermBudgetMap.put(key,MathUtils.doubleAdd(groupBudget,userBudget));
//            }else {
//                groupSchoolTermBudgetMap.put(key,userBudget);
//            }
//
//            //获取该物料费用，预算调整记录，拼装人员最初预算
//            List<AgentMaterialBalanceChangeRecord> materialBudgetChangeRecordList = agentMaterialBalanceChangeRecordDao.getByBudgetId(item.getId(), 1);
//            if (CollectionUtils.isNotEmpty(materialBudgetChangeRecordList)){
//                for (AgentMaterialBalanceChangeRecord materialBalanceChangeRecord : materialBudgetChangeRecordList){
//                    //之前增加预算，现在相应减少
//                    if (materialBalanceChangeRecord.getPreCash() < materialBalanceChangeRecord.getAfterCash()){
//                        userBudget = MathUtils.doubleSub(userBudget,materialBalanceChangeRecord.getQuantity());
//                        //之前减少预算，现在相应增加
//                    }else {
//                        userBudget = MathUtils.doubleAdd(userBudget,materialBalanceChangeRecord.getQuantity());
//                    }
//                }
//            }
//            //最初人员物料预算
//            Double userBudgetBefore = userBudget;
//
//            //最初未分配
//            Double userUndistributedCost = 0D;
//
//            //获取该物料费用，人工修改余额调整记录
//            List<AgentMaterialBalanceChangeRecord> materialBalanceChangeRecordList = agentMaterialBalanceChangeRecordDao.getByBudgetId(item.getId(), 2)
//                    .stream().filter(p -> null != p && p.getOperateType() == 1).collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(materialBalanceChangeRecordList)){
//                for (AgentMaterialBalanceChangeRecord materialBalanceChangeRecord : materialBalanceChangeRecordList){
//                    //之前增加余额，现在余额相应减少
//                    if (materialBalanceChangeRecord.getPreCash() < materialBalanceChangeRecord.getAfterCash()){
//                        userBalance = MathUtils.doubleSub(userBalance,materialBalanceChangeRecord.getQuantity());
//                        //之前减少余额，现在余额相应增加
//                    }else {
//                        userBalance = MathUtils.doubleAdd(userBalance,materialBalanceChangeRecord.getQuantity());
//                    }
//                }
//            }
//
//            //最初人员物料余额
//            Double userBalanceBefore = userBalance;
//
//
//            //人员余额变化
//            Double userBalanceSub = MathUtils.doubleSub(userBalanceCurrent, userBalanceBefore);
//            //人员预算变化
//            Double userBudgetSub = MathUtils.doubleSub(userBudgetCurrent, userBudgetBefore);
//
//            //未分配金额 = 最初未分配金额 + （人员余额变化 - 人员预算变化）
//            //未分配金额 = 最初未分配金额 + 人员余额单独变化
//            userUndistributedCost = MathUtils.doubleAdd(userUndistributedCost,MathUtils.doubleSub(userBalanceSub,userBudgetSub));
//
//            //拼装部门学期未分配金额map(groupId+schoolTerm : groupUndistributedCost)
//            String undistributedCostKey = groupId + "-" + semester;
//            Double groupUndistributedCost = groupSchoolTermUndistributedCostMap.get(undistributedCostKey);
//            if (null != groupUndistributedCost){
//                groupSchoolTermUndistributedCostMap.put(undistributedCostKey,MathUtils.doubleAdd(groupUndistributedCost,userUndistributedCost));
//            }else {
//                groupSchoolTermUndistributedCostMap.put(undistributedCostKey,userUndistributedCost);
//            }
//        });
//
//        //拼装部门物料费用
//        groupSchoolTermBudgetMap.forEach((k,v) -> {
//            //部门未分配金额
//            Double groupUndistributedCost = groupSchoolTermUndistributedCostMap.get(k);
//            //如果未分配金额小于0，设置为0
//            if (groupUndistributedCost < 0){
//                groupUndistributedCost = 0D;
//            }
//            //如果未分配金额大于物料预算，设置为预算
//            if (groupUndistributedCost > v){
//                groupUndistributedCost = v;
//            }
//
//            String[] keyArray = StringUtils.split(k,"-");
//            AgentMaterialCost groupMaterialCost = new AgentMaterialCost();
//            groupMaterialCost.setMaterialType(1);
//            groupMaterialCost.setSchoolTerm(keyArray[1]);
//            groupMaterialCost.setGroupId(SafeConverter.toLong(keyArray[0]));
//            groupMaterialCost.setBudget(v);//物料预算
//            groupMaterialCost.setUndistributedCost(groupUndistributedCost);//未分配金额
//            groupMaterialCost.setDisabled(false);
//            groupMaterialCostList.add(groupMaterialCost);
//        });
//        //插入部门物料费用
//        agentMaterialCostDao.inserts(groupMaterialCostList);
//        //插入人员物料费用
//        agentMaterialCostDao.inserts(userMaterialCostList);
//        return MapMessage.successMessage();
//    }
    private AgentMaterialCostVo constractAgentMaterialCostVo(Integer materialType, String schoolTerm, Long groupId, Double money, Long userId, String updateType, String content) {
        AgentMaterialCostVo materialCostVo = new AgentMaterialCostVo();
        materialCostVo.setMaterialType(materialType);
        materialCostVo.setSchoolTerm(schoolTerm);
        materialCostVo.setGroupId(groupId);
        if (Objects.equals(materialType, 1)) {
            materialCostVo.setBudget(money);//物料预算
            materialCostVo.setUndistributedCost(money);//未分配
        } else if (Objects.equals(materialType, 2)) {
            materialCostVo.setBalance(money);//物料余额
            materialCostVo.setUserId(userId);
        }
        materialCostVo.setDisabled(false);
        materialCostVo.setUpdateType(updateType);
        materialCostVo.setComment(content);
        return materialCostVo;
    }

    private int updateUserBalance(AgentMaterialCostVo item, Map<Long, List<AgentMaterialCost>> groupMaterialCostMap, Map<Long, List<AgentMaterialCost>> userMaterialCostMap) {
        int updateFlag = 0;
        Double balance = item.getBalance() == null ? 0 : item.getBalance();
        //
        List<AgentMaterialCost> isHaveGroupMaterialCostList = groupMaterialCostMap.get(item.getGroupId());
        List<AgentMaterialCost> isHaveUserMaterialCostList = userMaterialCostMap.get(item.getUserId());
        AgentMaterialCost isHaveUserMaterialCost = null;
        if (CollectionUtils.isNotEmpty(isHaveUserMaterialCostList)) {
            isHaveUserMaterialCost = isHaveUserMaterialCostList.stream().filter(p -> Objects.equals(p.getUserId(), item.getUserId()) && p.getSchoolTerm().equals(item.getSchoolTerm())).findFirst().orElse(null);
        }

        if (CollectionUtils.isNotEmpty(isHaveGroupMaterialCostList)) {
            //用户数据

            //部门数据
            AgentMaterialCost isHaveGroupMaterialCost = isHaveGroupMaterialCostList.stream().filter(p -> p.getSchoolTerm().equals(item.getSchoolTerm())).findFirst().orElse(null);
            if (null != isHaveGroupMaterialCost) {
                Double undistributedCost = isHaveGroupMaterialCost.getUndistributedCost() == null ? 0 : isHaveGroupMaterialCost.getUndistributedCost();
                Double afterCash = 0d;
                Double preCash = 0d;
                if ("增加".equals(item.getUpdateType())) {
                    String agentMaterialBudgetId;
                    if (item.getBalance() <= undistributedCost) { //分配金额小于未分配
                        if (isHaveUserMaterialCost != null) {
                            preCash = MathUtils.doubleAdd(isHaveUserMaterialCost.getBalance(), 0);
                            afterCash = MathUtils.doubleAdd(isHaveUserMaterialCost.getBalance(), balance);
                            isHaveUserMaterialCost.setBalance(afterCash);
                            agentMaterialCostDao.upsert(isHaveUserMaterialCost);
                            agentMaterialBudgetId = isHaveUserMaterialCost.getId();
                        } else {
                            //添加人员余额
                            agentMaterialCostDao.insert(item);
                            afterCash = MathUtils.doubleAdd(balance, 0);
                            agentMaterialBudgetId = item.getId();
                        }
                        //添加人员余额记录
                        AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(preCash, afterCash, balance, "导入余额" + (StringUtils.isBlank(item.getComment()) ? "" : "/" + item.getComment()), 2, agentMaterialBudgetId, 1, getCurrentUserId());
                        agentMaterialBalanceChangeRecordDao.insert(balanceRecord);
                        updateFlag = 1;
                        double afterUndistributedCost = MathUtils.doubleSub(undistributedCost, balance);
                        isHaveGroupMaterialCost.setUndistributedCost(afterUndistributedCost);
                        agentMaterialCostDao.upsert(isHaveGroupMaterialCost);
                        //添加部门未分配费用日志记录
                        AgentMaterialBalanceChangeRecord undistributedCostRecord = generateChangeRecord(undistributedCost, afterUndistributedCost, balance, "导入余额，部门未分配费用变动" + (StringUtils.isBlank(item.getComment()) ? "" : "/" + item.getComment()), 3, isHaveGroupMaterialCost.getId(), 1, getCurrentUserId());
                        agentMaterialBalanceChangeRecordDao.insert(undistributedCostRecord);
                    }

                } else {//减少余额
                    if (isHaveUserMaterialCost != null) {
                        if (balance <= isHaveUserMaterialCost.getBalance()) {
                            preCash = MathUtils.doubleAdd(isHaveUserMaterialCost.getBalance(), 0);
                            afterCash = MathUtils.doubleSub(isHaveUserMaterialCost.getBalance(), balance);
                            isHaveUserMaterialCost.setBalance(afterCash);
                            agentMaterialCostDao.upsert(isHaveUserMaterialCost);
                            //修改部门未分配
                            double afterUndistributedCost = MathUtils.doubleAdd(undistributedCost, balance);
                            isHaveGroupMaterialCost.setUndistributedCost(afterUndistributedCost);
                            agentMaterialCostDao.upsert(isHaveGroupMaterialCost);
                            //添加人员余额记录
                            AgentMaterialBalanceChangeRecord balanceRecord = generateChangeRecord(preCash, afterCash, balance, "导入余额" + (StringUtils.isBlank(item.getComment()) ? "" : "/" + item.getComment()), 2, isHaveUserMaterialCost.getId(), 1, getCurrentUserId());
                            agentMaterialBalanceChangeRecordDao.insert(balanceRecord);
                            updateFlag = 1;

                            //添加部门未分配费用日志记录
                            AgentMaterialBalanceChangeRecord undistributedCostRecord = generateChangeRecord(undistributedCost, afterUndistributedCost, balance, "导入余额，部门未分配费用变动" + (StringUtils.isBlank(item.getComment()) ? "" : "/" + item.getComment()), 3, isHaveGroupMaterialCost.getId(), 1, getCurrentUserId());
                            agentMaterialBalanceChangeRecordDao.insert(undistributedCostRecord);
                        }

                    }
                }

            }
        }
        return updateFlag;
    }

    public boolean checkForUpdates(Map<String, List<AgentMaterialBudgetVO>> rowMap, String rowKey, AgentMaterialBudget agentMaterialBudget, Integer templateType) {
        List<AgentMaterialBudgetVO> voList = rowMap.get(rowKey);
        if (CollectionUtils.isEmpty(voList)) {
            return false;
        }
        List<AgentMaterialBudgetVO> addList = voList.stream().filter(p -> "增加".equals(p.getUpdateType())).collect(Collectors.toList());
        List<AgentMaterialBudgetVO> reduceList = voList.stream().filter(p -> "减少".equals(p.getUpdateType())).collect(Collectors.toList());
        if (Objects.equals(1, templateType)) {//更新预算时
            double addBudget = 0.0, reduceBudget = 0.0;
            for (AgentMaterialBudgetVO vo : addList) {
                addBudget = MathUtils.doubleAdd(addBudget, vo.getBudget());
            }
            for (AgentMaterialBudgetVO vo : reduceList) {
                reduceBudget = MathUtils.doubleAdd(reduceBudget, vo.getBudget());
            }
            if (MathUtils.doubleAdd(addBudget, agentMaterialBudget.getBalance()) < reduceBudget) {
                return false;
            } else {
                return true;
            }
        } else { //更新余额
            double addBalance = 0.0, reduceBalance = 0.0;
            for (AgentMaterialBudgetVO vo : addList) {
                addBalance = MathUtils.doubleAdd(addBalance, vo.getBalance());
            }
            for (AgentMaterialBudgetVO vo : reduceList) {
                reduceBalance = MathUtils.doubleAdd(reduceBalance, vo.getBalance());
            }
            //增加的金额超过预算金额了
            if (MathUtils.doubleAdd(addBalance, agentMaterialBudget.getBalance()) > agentMaterialBudget.getBudget()) {
                return false;
            }

            if (MathUtils.doubleAdd(addBalance, agentMaterialBudget.getBalance()) < reduceBalance) {
                return false;
            } else {
                return true;
            }
        }
    }

    //每个月份_城市_部门  做key  临时缓存 对应记录的条数
    public void cacheRows(Map<String, List<AgentMaterialBudgetVO>> rowMap, String rowKey, AgentMaterialBudgetVO item) {
        if (rowMap.containsKey(rowKey)) {
            rowMap.get(rowKey).add(item);
        } else {
            List<AgentMaterialBudgetVO> itemList = new ArrayList<>();
            itemList.add(item);
            rowMap.put(rowKey, itemList);
        }
    }

    public void addErrorInfo(Map<String, List<String>> errorMap, String key, String errorMsg) {
        List<String> errorList = errorMap.get(key);
        if (CollectionUtils.isEmpty(errorList)) {
            errorList = new ArrayList<>();
        }
        errorList.add(errorMsg);
        errorMap.put(key, errorList);
    }
}
