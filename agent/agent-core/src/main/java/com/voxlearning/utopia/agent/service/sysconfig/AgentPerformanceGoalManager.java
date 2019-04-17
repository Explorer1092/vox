package com.voxlearning.utopia.agent.service.sysconfig;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.AgentPerformanceGoalVO;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.entity.crm.constants.UserPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentPerformanceGoalType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentPerformanceGoalLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentPerformanceServiceRecordLoderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentPerformanceGoalServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentPerformanceServiceRecordServiceClient;
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
 * Agent业绩目标处理类
 *
 * @author chunlin.yu
 * @create 2017-10-26 16:05
 **/
@Named
public class AgentPerformanceGoalManager extends AbstractAgentService {

    @Inject
    AgentPerformanceGoalLoaderClient agentPerformanceGoalLoaderClient;

    @Inject
    AgentPerformanceGoalServiceClient agentPerformanceGoalServiceClient;

    @Inject
    AgentPerformanceServiceRecordLoderClient agentPerformanceServiceRecordLoderClient;

    @Inject
    AgentPerformanceServiceRecordServiceClient agentPerformanceServiceRecordServiceClient;

    @Inject
    BaseOrgService baseOrgService;

    /**
     * 导入业绩目标
     * @param workbook
     * @return
     */
    public MapMessage importAgentPerformanceGoal(XSSFWorkbook workbook){
        List<AgentPerformanceGoalVO> agentPerformanceGoalVOList = convert2AgentPerformanceGoalVO(workbook);
        MapMessage mapMessage = validateAndFillAgentPerformanceGoalVOData(agentPerformanceGoalVOList);
        if (mapMessage.isSuccess()){
            mapMessage = upserts(agentPerformanceGoalVOList);
        }
        return mapMessage;
    }


    private MapMessage upserts(List<AgentPerformanceGoalVO> agentPerformanceGoalVOList){
        List<AgentPerformanceGoal> agentPerformanceGoalList = new ArrayList<>();
        agentPerformanceGoalVOList.forEach(item -> {
            agentPerformanceGoalList.add(item.toAgentPerformanceGoal());
        });
        return upsertAgentPerformanceGoals(agentPerformanceGoalList);
    }

    private MapMessage upsertAgentPerformanceGoals(List<AgentPerformanceGoal> agentPerformanceGoalList){
        MapMessage mapMessage = MapMessage.successMessage();
        if (CollectionUtils.isNotEmpty(agentPerformanceGoalList)){
            Map<Integer,List<AgentPerformanceGoal>> monthMap = new HashMap<>();
            //总插入
            int allOperateInsert = 0;
            //总更新
            int allOperateUpdate = 0;
            //全国插入条数
            int countryOperateInsert = 0;
            //全国更新条数
            int countryOperateUpdate = 0;
            //大区插入条数
            int regionGroupOperateInsert = 0;
            //大区更新条数
            int regionGroupOperateUpdate = 0;
            //分区插入条数
            int subRegionGroupOperateInsert = 0;
            //分区更新条数
            int subRegionGroupOperateUpdate = 0;
            //专员插入条数
            int businessDeveloperOperateInsert = 0;
            //专员更新条数
            int businessDeveloperOperateUpdate = 0;
            for (int i = 0; i < agentPerformanceGoalList.size(); i++) {
                AgentPerformanceGoal item = agentPerformanceGoalList.get(i);
                if (!monthMap.containsKey(item.getMonth())){
                    List<AgentPerformanceGoal> tempMonthList = agentPerformanceGoalLoaderClient.loadByMonth(item.getMonth());
                    monthMap.put(item.getMonth(),tempMonthList);
                }
                List<AgentPerformanceGoal> oldAgentPerformanceGoalList = monthMap.get(item.getMonth());
                Map<AgentPerformanceGoalType, List<AgentPerformanceGoal>> agentPerformanceGoalTypeListMap = oldAgentPerformanceGoalList.stream().collect(Collectors.groupingBy(AgentPerformanceGoal::getAgentPerformanceGoalType, Collectors.toList()));
                if (!agentPerformanceGoalTypeListMap.containsKey(item.getAgentPerformanceGoalType())){
                    agentPerformanceGoalTypeListMap.put(item.getAgentPerformanceGoalType(),new ArrayList<>());
                }
                List<AgentPerformanceGoal> typeAgentPerformanceGoalList = agentPerformanceGoalTypeListMap.get(item.getAgentPerformanceGoalType());
                List<AgentPerformanceGoal> exsitGoals = typeAgentPerformanceGoalList.stream().filter(p ->
                        Objects.equals(p.getRegionGroupId(), item.getRegionGroupId())
                                && Objects.equals(p.getSubRegionGroupId(), item.getSubRegionGroupId())
                                && Objects.equals(p.getUserId(), item.getUserId())).collect(Collectors.toList());

                if (null != item.getRegionGroupId() && null != item.getMonth()){
                    AgentPerformanceGoal regionGroupPerformanceGoal = getRegionGroupPerformanceGoal(item.getMonth(), item.getRegionGroupId());
                    if (null != regionGroupPerformanceGoal){
                        item.setConfirm(regionGroupPerformanceGoal.getConfirm());
                    }else {
                        item.setConfirm(false);
                    }
                }
                if (CollectionUtils.isNotEmpty(exsitGoals)){
                    AgentPerformanceGoal agentPerformanceGoal = exsitGoals.get(0);
                    item.setId(agentPerformanceGoal.getId());
                    allOperateUpdate++;
                    switch (item.getAgentPerformanceGoalType()){
                        case COUNTRY:
                            item.setConfirm(true);
                            countryOperateUpdate++;
                            break;
                        case REGION_GROUP:
                            regionGroupOperateUpdate++;
                            break;
                        case SUB_REGION_GROUP:
                            subRegionGroupOperateUpdate++;
                            break;
                        case USER:
                            businessDeveloperOperateUpdate++;
                            break;
                        default:
                            break;
                    }
                    agentPerformanceGoalServiceClient.replace(item);
                }else {
                    allOperateInsert++;
                    switch (item.getAgentPerformanceGoalType()){
                        case COUNTRY:
                            item.setConfirm(true);
                            countryOperateInsert++;
                            break;
                        case REGION_GROUP:
                            regionGroupOperateInsert++;
                            break;
                        case SUB_REGION_GROUP:
                            subRegionGroupOperateInsert++;
                            break;
                        case USER:
                            businessDeveloperOperateInsert++;
                            break;
                        default:
                            break;
                    }
                    AgentPerformanceGoal agentPerformanceGoal = agentPerformanceGoalServiceClient.insert(item);
                    typeAgentPerformanceGoalList.add(agentPerformanceGoal);
                }
            }
            Map<String,Integer> allOperateMap = new HashMap<>();
            Map<String,Integer> countryOperateMap = new HashMap<>();
            Map<String,Integer> regionGroupOperateMap = new HashMap<>();
            Map<String,Integer> subRegionGroupOperateMap = new HashMap<>();
            Map<String,Integer> businessDeveloperOperateMap = new HashMap<>();
            allOperateMap.put("insert",allOperateInsert);
            allOperateMap.put("update",allOperateUpdate);
            countryOperateMap.put("insert",countryOperateInsert);
            countryOperateMap.put("update",countryOperateUpdate);
            regionGroupOperateMap.put("insert",regionGroupOperateInsert);
            regionGroupOperateMap.put("update",regionGroupOperateUpdate);
            subRegionGroupOperateMap.put("insert",subRegionGroupOperateInsert);
            subRegionGroupOperateMap.put("update",subRegionGroupOperateUpdate);
            businessDeveloperOperateMap.put("insert",businessDeveloperOperateInsert);
            businessDeveloperOperateMap.put("update",businessDeveloperOperateUpdate);
            mapMessage.add("allOperateMap",allOperateMap)
                    .add("countryOperateMap",countryOperateMap)
                    .add("regionGroupOperateMap",regionGroupOperateMap)
                    .add("subRegionGroupOperateMap",subRegionGroupOperateMap)
                    .add("businessDeveloperOperateMap",businessDeveloperOperateMap);

        }
        return mapMessage;
    }

    /**
     * 校验信息，校验通过的字段会填充其他相关字段信息
     * @param agentPerformanceGoalVOList
     * @return
     */
    private MapMessage validateAndFillAgentPerformanceGoalVOData(List<AgentPerformanceGoalVO> agentPerformanceGoalVOList){
        MapMessage resultMessage = MapMessage.errorMessage();
        if (CollectionUtils.isNotEmpty(agentPerformanceGoalVOList)){

            List<String> errorList = new ArrayList<>();
            List<String> tempContains = new ArrayList<>();
            int nowYearMonth = getNowYearMonth();
            for (int i = 0; i < agentPerformanceGoalVOList.size(); i++) {
                int rows = i + 2;
                AgentPerformanceGoalVO item = agentPerformanceGoalVOList.get(i);
                if (null == item.getMonth() || item.getMonth() == 0 || item.getMonth() < 200000 || item.getMonth()>210000){
                    errorList.add(rows + "行:月份格式不正确。");
                    continue;
                } else if (nowYearMonth > item.getMonth()){
                    errorList.add(rows + "行:月份不正确，不能导入之前月份的数据。");
                    continue;
                } else {
                    int month = item.getMonth() % 100;
                    if (month <= 0 || month > 12){
                        errorList.add(rows + "行:月份格式不正确。");
                        continue;
                    }
                }
                if (null == item.getAgentPerformanceGoalTypeDesc() || null == AgentPerformanceGoalType.of(item.getAgentPerformanceGoalTypeDesc())){
                    errorList.add(rows + "行:角色名称为空或错误。");
                    continue;
                }
                AgentPerformanceGoalType goalType = AgentPerformanceGoalType.of(item.getAgentPerformanceGoalTypeDesc());
                //检验大区
                AgentGroup regionGroup =null;
                if (AgentPerformanceGoalType.REGION_GROUP.equals(goalType) || AgentPerformanceGoalType.SUB_REGION_GROUP.equals(goalType) || AgentPerformanceGoalType.USER.equals(goalType)){
                    regionGroup = baseOrgService.getGroupByName(item.getRegionGroupName());
                    if (null == regionGroup){
                        errorList.add(rows + "行:大区名称为空或错误。");
                        continue;
                    }
                    item.setRegionGroupId(regionGroup.getId());
                }
                //校验分区
                AgentGroup subRegionGroup = null;
                if (AgentPerformanceGoalType.SUB_REGION_GROUP.equals(goalType) || AgentPerformanceGoalType.USER.equals(goalType)){
                    subRegionGroup = baseOrgService.getGroupByName(item.getSubRegionGroupName());
                    if (null == subRegionGroup || !Objects.equals(subRegionGroup.getParentId(),regionGroup.getId())){
                        errorList.add(rows + "行:分区名称为空或错误或分区与大区不对应。");
                        continue;
                    }
                    item.setSubRegionGroupId(subRegionGroup.getId());
                }
                //校验专员
                if (AgentPerformanceGoalType.USER.equals(goalType)){
                    List<AgentUser> agentUserList = baseOrgService.getUserByRealName(item.getBusinessDeveloperName());
                    List<Long> userIds = baseOrgService.getGroupUserByGroup(subRegionGroup.getId()).stream().filter(p -> p.getUserRoleType() == AgentRoleType.BusinessDeveloper).map(AgentGroupUser::getUserId).collect(Collectors.toList());

                    AgentUser agentUser = null;
                    if (CollectionUtils.isNotEmpty(agentUserList) && CollectionUtils.isNotEmpty(userIds)){
                        for (int j = 0; j < agentUserList.size(); j++) {
                            if (userIds.contains(agentUserList.get(j).getId())){
                                agentUser = agentUserList.get(j);
                            }
                        }
                    }
                    if (null == agentUser ){
                        errorList.add(rows + "行:专员名称为空或错误或分区与分区不对应。");
                        continue;
                    }
                    item.setBusinessDeveloperId(agentUser.getId());
                }

                if (null == item.getSglSubjIncGoal()|| item.getSglSubjIncGoal() < 0 || null == item.getSglSubjLtBfGoal() || item.getSglSubjLtBfGoal() < 0 || null == item.getSglSubjStBfGoal() || item.getSglSubjStBfGoal() < 0){
                    errorList.add(rows + "行:小单新增目标、小单长回目标、小单短回目标数据都必填且为不小于0的整数。");
                    continue;
                }

                String key = StringUtils.formatMessage(
                        "{}{}{}{}{}",
                        item.getMonth(),
                        item.getAgentPerformanceGoalTypeDesc(),
                        item.getRegionGroupName(),
                        item.getSubRegionGroupName(),
                        item.getBusinessDeveloperName());
                if (tempContains.contains(key)){
                    errorList.add(rows + "行:与前面有相同的内容。");
                    continue;
                }else {
                    tempContains.add(key);
                }

            }
            if (CollectionUtils.isNotEmpty(errorList)){
                resultMessage.put("errorList",errorList);
            }else {
                resultMessage = MapMessage.successMessage();
            }
        }
        return resultMessage;
    }

    private List<AgentPerformanceGoalVO> convert2AgentPerformanceGoalVO(XSSFWorkbook workbook){
        List<AgentPerformanceGoalVO> resultList = new ArrayList<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        int rows =1 ;
        if (null != sheet){
            while (true) {
                try {
                    XSSFRow row = sheet.getRow(rows++);
                    if (row == null) {
                        break;
                    }
                    Integer month = XssfUtils.getIntCellValue(row.getCell(0));
                    String agentPerformanceGoalTypeDesc = XssfUtils.getStringCellValue(row.getCell(1));
                    String regionGroupName = XssfUtils.getStringCellValue(row.getCell(2));
                    String subRegionGroupName = XssfUtils.getStringCellValue(row.getCell(3));
                    String businessDeveloperName = XssfUtils.getStringCellValue(row.getCell(4));
                    String sglSubjIncGoalStr = XssfUtils.getStringCellValue(row.getCell(5));
                    Integer sglSubjIncGoal = null;
                    if (null != sglSubjIncGoalStr && !sglSubjIncGoalStr.contains(".")){
                        sglSubjIncGoal = XssfUtils.getIntCellValue(row.getCell(5));
                    }
                    String sglSubjLtBfGoalStr = XssfUtils.getStringCellValue(row.getCell(6));
                    Integer sglSubjLtBfGoal = null;
                    if (null != sglSubjLtBfGoalStr && !sglSubjLtBfGoalStr.contains(".")){
                        sglSubjLtBfGoal = XssfUtils.getIntCellValue(row.getCell(6));
                    }
                    String sglSubjStBfGoalStr = XssfUtils.getStringCellValue(row.getCell(7));
                    Integer sglSubjStBfGoal = null;
                    if (null != sglSubjStBfGoalStr && !sglSubjStBfGoalStr.contains(".")){
                        sglSubjStBfGoal = XssfUtils.getIntCellValue(row.getCell(7));
                    }
                    AgentPerformanceGoalVO agentPerformanceGoalVO = new AgentPerformanceGoalVO();
                    agentPerformanceGoalVO.setMonth(month);
                    agentPerformanceGoalVO.setAgentPerformanceGoalTypeDesc(agentPerformanceGoalTypeDesc);
                    agentPerformanceGoalVO.setRegionGroupName(regionGroupName);
                    agentPerformanceGoalVO.setSubRegionGroupName(subRegionGroupName);
                    agentPerformanceGoalVO.setBusinessDeveloperName(businessDeveloperName);
                    agentPerformanceGoalVO.setSglSubjIncGoal(sglSubjIncGoal);
                    agentPerformanceGoalVO.setSglSubjLtBfGoal(sglSubjLtBfGoal);
                    agentPerformanceGoalVO.setSglSubjStBfGoal(sglSubjStBfGoal);
                    resultList.add(agentPerformanceGoalVO);
                } catch (Exception ex) {
                    logger.error("read excel failed", ex);
                    break;
                }
            }
        }
        return resultList;
    }

    public MapMessage confirmPerformanceGoal(int month, Long groupId){
        List<AgentPerformanceGoalVO> regoinGroupGoalVOS = getAgentPerformanceGoalVOByMonthAndGroupId(month, groupId);
        AgentPerformanceGoalVO regionGroupGoalVO = regoinGroupGoalVOS.stream().filter(item -> Objects.equals(item.getAgentPerformanceGoalTypeDesc(), AgentPerformanceGoalType.REGION_GROUP.getDesc()) && Objects.equals(item.getRegionGroupId(), groupId)).findFirst().orElse(null);
        if (null == regionGroupGoalVO){
            return MapMessage.errorMessage("不存在该大区业绩目标数据");
        }
        regoinGroupGoalVOS.forEach(item ->{
            AgentPerformanceGoal agentPerformanceGoal = item.toAgentPerformanceGoal();
            agentPerformanceGoal.setConfirm(true);
            agentPerformanceGoalServiceClient.replace(agentPerformanceGoal);
        });
        return MapMessage.successMessage();
    }

    /**
     * 按类型删除
     * @param targetId
     * @param performanceGoalType
     */
    public void removeCurrentMonthPerformanceGoal(Long targetId,AgentPerformanceGoalType performanceGoalType){
        int month =getNowYearMonth();
        List<AgentPerformanceGoal> needDeleteList = agentPerformanceGoalLoaderClient.loadByIdAndTypeAndBeginMonth(targetId,performanceGoalType,month);
        if (CollectionUtils.isEmpty(needDeleteList)){
            return;
        }
        if (CollectionUtils.isNotEmpty(needDeleteList)){
            needDeleteList.forEach(item -> {
                item.setDisabled(true);
                agentPerformanceGoalServiceClient.replace(item);
            });
        }
    }

    public MapMessage updatePerformanceGoal(AgentPerformanceGoalVO agentPerformanceGoalVO,int sglSubjIncGoal,int sglSubjLtBfGoal,int sglSubjStBfGoal,String reason){
        if (StringUtils.isEmpty(reason)){
            return MapMessage.errorMessage("请填写变更原因");
        }
        AgentPerformanceGoal agentPerformanceGoal = agentPerformanceGoalVO.toAgentPerformanceGoal();
        AgentPerformanceGoal regionGroupPerformanceGoal = getRegionGroupPerformanceGoal(agentPerformanceGoalVO.getMonth(), agentPerformanceGoal.getRegionGroupId());

        if (!getCurrentUser().isCountryManager() && null != regionGroupPerformanceGoal && regionGroupPerformanceGoal.getConfirm()){
            return MapMessage.errorMessage("该大区已经确认过，无法修改");
        }
        agentPerformanceGoal.setSglSubjIncGoal(sglSubjIncGoal);
        agentPerformanceGoal.setSglSubjLtBfGoal(sglSubjLtBfGoal);
        agentPerformanceGoal.setSglSubjStBfGoal(sglSubjStBfGoal);
        if(null != regionGroupPerformanceGoal){
            agentPerformanceGoal.setConfirm(regionGroupPerformanceGoal.getConfirm());
        }else {
            if (agentPerformanceGoal.getAgentPerformanceGoalType() == AgentPerformanceGoalType.COUNTRY){
                regionGroupPerformanceGoal.setConfirm(true);
            }else {
                agentPerformanceGoal.setConfirm(false);
            }
        }
        if (null != agentPerformanceGoal.getId()){
            agentPerformanceGoalServiceClient.replace(agentPerformanceGoal);
        }else {
            agentPerformanceGoalServiceClient.insert(agentPerformanceGoal);
        }
        Map<String,Integer> oldGoalMap = new HashMap<>();
        Map<String,Integer> newGoalMap = new HashMap<>();
        if (!Objects.equals(agentPerformanceGoalVO.getSglSubjIncGoal(),sglSubjIncGoal)){
            oldGoalMap.put("sglSubjIncGoal",agentPerformanceGoalVO.getSglSubjIncGoal());
            newGoalMap.put("sglSubjIncGoal",sglSubjIncGoal);
        }

        if (!Objects.equals(agentPerformanceGoalVO.getSglSubjLtBfGoal(),sglSubjLtBfGoal)){
            oldGoalMap.put("sglSubjLtBfGoal",agentPerformanceGoalVO.getSglSubjLtBfGoal());
            newGoalMap.put("sglSubjLtBfGoal",sglSubjLtBfGoal);
        }

        if (!Objects.equals(agentPerformanceGoalVO.getSglSubjStBfGoal(),sglSubjStBfGoal)){
            oldGoalMap.put("sglSubjStBfGoal",agentPerformanceGoalVO.getSglSubjStBfGoal());
            newGoalMap.put("sglSubjStBfGoal",sglSubjStBfGoal);
        }

        AgentPerformanceServiceRecord agentPerformanceServiceRecord = new AgentPerformanceServiceRecord();
        agentPerformanceServiceRecord.setAgentPerformanceGoalType(agentPerformanceGoal.getAgentPerformanceGoalType());
        agentPerformanceServiceRecord.setMonth(agentPerformanceGoal.getMonth());
        if (MapUtils.isNotEmpty(newGoalMap)){
            Map<String,Map<String,Integer>> contentMap = new HashMap<>();
            contentMap.put("before",oldGoalMap);
            contentMap.put("after",newGoalMap);
            agentPerformanceServiceRecord.setContent(JsonUtils.toJson(contentMap));
        }

        switch (agentPerformanceGoal.getAgentPerformanceGoalType()){
            case REGION_GROUP:
                agentPerformanceServiceRecord.setTargetId(agentPerformanceGoal.getRegionGroupId());
                break;
            case SUB_REGION_GROUP:
                agentPerformanceServiceRecord.setTargetId(agentPerformanceGoal.getSubRegionGroupId());
                break;
            case USER:
                agentPerformanceServiceRecord.setTargetId(agentPerformanceGoal.getUserId());
                break;
            default:
                break;
        }
        agentPerformanceServiceRecord.setUserPlatformType(UserPlatformType.AGENT);
        agentPerformanceServiceRecord.setAdditions(reason);
        saveAgentPerformanceServiceRecord(agentPerformanceServiceRecord);
        return MapMessage.successMessage();
    }

    /**
     * 获取大区业绩目标
     * @param month
     * @param regionGroupId
     * @return
     */
    private AgentPerformanceGoal getRegionGroupPerformanceGoal(int month, Long regionGroupId){
        List<AgentPerformanceGoal> agentPerformanceGoalList = agentPerformanceGoalLoaderClient.loadByMonth(month);
        return agentPerformanceGoalList.stream().filter(item -> Objects.equals(item.getRegionGroupId(),regionGroupId) && Objects.equals(item.getAgentPerformanceGoalType(),AgentPerformanceGoalType.REGION_GROUP)).findFirst().orElse(null);
    }

    /**
     * 保存业绩目标
     * @param agentPerformanceServiceRecord
     */
    private void saveAgentPerformanceServiceRecord(AgentPerformanceServiceRecord agentPerformanceServiceRecord){
        AuthCurrentUser currentUser = getCurrentUser();
        agentPerformanceServiceRecord.setOperatorId(currentUser.getUserId());
        agentPerformanceServiceRecord.setOperatorName(currentUser.getRealName());
        agentPerformanceServiceRecord.setDisabled(false);
        agentPerformanceServiceRecordServiceClient.insert(agentPerformanceServiceRecord);
    }

    public List<AgentPerformanceServiceRecord> loadAgentPerformanceServiceRecords(Integer month,Long targetId,AgentPerformanceGoalType agentPerformanceGoalType){
        if (null == month || null == targetId || null == agentPerformanceGoalType){
            return Collections.emptyList();
        }
        return agentPerformanceServiceRecordLoderClient.load(month,targetId,agentPerformanceGoalType);
    }

    public List<String> checkAgentPerformanceGoal(){
        List<String> errorList = new ArrayList<>();
        int nowMonth = getNowYearMonth();
        List<AgentPerformanceGoal> agentPerformanceGoalList = agentPerformanceGoalLoaderClient.loadByMonth(nowMonth);
        if (CollectionUtils.isNotEmpty(agentPerformanceGoalList)){
            List<AgentGroup> allGroups = baseOrgService.findAllGroups();
            Map<Long, AgentGroup> agentGroupMap = allGroups.stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
            agentPerformanceGoalList.forEach(item -> {
                switch (item.getAgentPerformanceGoalType()){
                    case COUNTRY:
                        List<AgentPerformanceGoal> regionGroupAgentPerformanceGoal = agentPerformanceGoalList.stream().filter(p -> Objects.equals(p.getAgentPerformanceGoalType(), AgentPerformanceGoalType.REGION_GROUP)).collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(regionGroupAgentPerformanceGoal)){
                            errorList.add("没有导入大区目标");
                        }else {
                            int countGoal = item.fetchSumGoal();
                            int regionGroupGoal = 0;
                            for (int i = 0; i < regionGroupAgentPerformanceGoal.size(); i++) {
                                regionGroupGoal += regionGroupAgentPerformanceGoal.get(i).fetchSumGoal();
                            }
                            if (countGoal != regionGroupGoal){
                                errorList.add("全国目标不等于各大区目标之和");
                            }
                        }

                        break;
                    case REGION_GROUP:
                        List<AgentPerformanceGoal> subRegionGroupAgentPerformanceGoal = agentPerformanceGoalList.stream().filter(p -> Objects.equals(p.getAgentPerformanceGoalType(), AgentPerformanceGoalType.SUB_REGION_GROUP) && Objects.equals(p.getRegionGroupId(), item.getRegionGroupId())).collect(Collectors.toList());
                        AgentGroup agentGroup = agentGroupMap.get(item.getRegionGroupId());
                        if (null == agentGroup){
                            break;
                        }
                        if (CollectionUtils.isEmpty(subRegionGroupAgentPerformanceGoal)){
                            errorList.add("大区【"+agentGroup.getGroupName()+"】没有导入分区目标");
                        }else {
                            int allGoal = item.fetchSumGoal();
                            int subGroupGoal = 0;
                            for (int i = 0; i < subRegionGroupAgentPerformanceGoal.size(); i++) {
                                subGroupGoal += subRegionGroupAgentPerformanceGoal.get(i).fetchSumGoal();
                            }
                            if (allGoal != subGroupGoal){
                                errorList.add("大区【"+agentGroup.getGroupName()+"】总目标不等于各分区目标之和");
                            }
                        }
                        break;
                    case SUB_REGION_GROUP:
                        List<AgentPerformanceGoal> businessDeveloperAgentPerformanceGoal = agentPerformanceGoalList.stream().filter(p -> Objects.equals(p.getAgentPerformanceGoalType(), AgentPerformanceGoalType.USER) && Objects.equals(p.getRegionGroupId(), item.getRegionGroupId()) && Objects.equals(p.getSubRegionGroupId(), item.getSubRegionGroupId())).collect(Collectors.toList());
                        AgentGroup subRegionAgentGroup = agentGroupMap.get(item.getSubRegionGroupId());
                        if (null == subRegionAgentGroup){
                            break;
                        }
                        if (CollectionUtils.isEmpty(businessDeveloperAgentPerformanceGoal)){
                            errorList.add("分区【"+subRegionAgentGroup.getGroupName()+"】没有导入专员目标");
                        }else {
                            int allGoal = item.fetchSumGoal();
                            int subGroupGoal = 0;
                            for (int i = 0; i < businessDeveloperAgentPerformanceGoal.size(); i++) {
                                subGroupGoal += businessDeveloperAgentPerformanceGoal.get(i).fetchSumGoal();
                            }
                            if (allGoal < subGroupGoal){
                                errorList.add("分区【"+subRegionAgentGroup.getGroupName()+"】总目标小于各专员目标之和");
                            }
                        }
                        break;
                }
            });
        }
        return errorList;
    }


    public List<AgentPerformanceGoalVO> getAgentPerformanceGoalVOByMonthAndGroupId(int month, Long groupId) {
        if (month == 0 || groupId ==0L){
            return Collections.emptyList();
        }
        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        if (null == agentGroup.getRoleId()){
            return Collections.emptyList();
        }
        AgentGroupRoleType agentGroupRoleType = AgentGroupRoleType.of(agentGroup.getRoleId());
        if (null != agentGroupRoleType){
            AgentPerformanceGoalType downType = null;
            switch (agentGroupRoleType){
                case Country:
                    downType = AgentPerformanceGoalType.COUNTRY;
                    break;
                case Region:
                    downType = AgentPerformanceGoalType.REGION_GROUP;
                    break;
                case City:
                    downType = AgentPerformanceGoalType.SUB_REGION_GROUP;
                    break;
                default:
                    break;
            }
            if (null != downType){
                List<AgentPerformanceGoal> agentPerformanceGoalList = agentPerformanceGoalLoaderClient.loadByMonth(month);
                if (downType.equals(AgentPerformanceGoalType.REGION_GROUP)){
                    agentPerformanceGoalList = agentPerformanceGoalList.stream().filter(item -> Objects.equals(item.getRegionGroupId(),groupId)).collect(Collectors.toList());
                }else if (downType.equals(AgentPerformanceGoalType.SUB_REGION_GROUP)){
                    agentPerformanceGoalList = agentPerformanceGoalList.stream().filter(item -> Objects.equals(item.getSubRegionGroupId(),groupId)).collect(Collectors.toList());
                }else if (downType.equals(AgentPerformanceGoalType.COUNTRY)){

                }else {
                    agentPerformanceGoalList = null;
                }
                if (CollectionUtils.isNotEmpty(agentPerformanceGoalList)){
                    return toAgentPerformanceGoalVOs(agentPerformanceGoalList);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<AgentPerformanceGoalVO> toAgentPerformanceGoalVOs(List<AgentPerformanceGoal> agentPerformanceGoals){
        if (CollectionUtils.isNotEmpty(agentPerformanceGoals)){
            List<AgentPerformanceGoalVO> resultList = new ArrayList<>();
            List<AgentGroup> allGroups = baseOrgService.findAllGroups();
            List<AgentUser> allAgentUsers = baseOrgService.findAllAgentUsers();
            Map<Long, AgentGroup> agentGroupMap = allGroups.stream().collect(Collectors.toMap(AgentGroup::getId, Function.identity(), (o1, o2) -> o1));
            Map<Long, AgentUser> agentUserMap = allAgentUsers.stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));
            Map<Long, AgentGroup> disabledAgentGroupMapCache = new HashMap<>();
            Map<Long, AgentUser> unValidUserMapCache = new HashMap<>();
            agentPerformanceGoals.forEach(item -> {
                AgentPerformanceGoalVO agentPerformanceGoalVO = new AgentPerformanceGoalVO();
                agentPerformanceGoalVO.setMonth(item.getMonth());
                agentPerformanceGoalVO.setAgentPerformanceGoalTypeDesc(item.getAgentPerformanceGoalType().getDesc());
                if (item.getRegionGroupId() != null ){
                    AgentGroup agentGroup = agentGroupMap.get(item.getRegionGroupId());
                    if (null == agentGroup){
                        if (!disabledAgentGroupMapCache.containsKey(item.getRegionGroupId())){
                            AgentGroup disabledGroup = baseOrgService.loadDisabledGroup(item.getRegionGroupId());
                            if (null != disabledGroup){
                                disabledAgentGroupMapCache.put(disabledGroup.getId(),disabledGroup);
                            }
                        }
                        agentGroup = disabledAgentGroupMapCache.get(item.getRegionGroupId());
                    }
                    if (null != agentGroup){
                        agentPerformanceGoalVO.setRegionGroupId(item.getRegionGroupId());
                        agentPerformanceGoalVO.setRegionGroupName(agentGroup.getGroupName());
                    }
                }
                if (item.getSubRegionGroupId() !=null){
                    AgentGroup agentGroup = agentGroupMap.get(item.getSubRegionGroupId());
                    if (null == agentGroup){
                        if (!disabledAgentGroupMapCache.containsKey(item.getSubRegionGroupId())){
                            AgentGroup disabledGroup = baseOrgService.loadDisabledGroup(item.getSubRegionGroupId());
                            if (null != disabledGroup){
                                disabledAgentGroupMapCache.put(disabledGroup.getId(),disabledGroup);
                            }
                        }
                        agentGroup = disabledAgentGroupMapCache.get(item.getSubRegionGroupId());
                    }
                    if (null != agentGroup){
                        agentPerformanceGoalVO.setSubRegionGroupId(item.getSubRegionGroupId());
                        agentPerformanceGoalVO.setSubRegionGroupName(agentGroup.getGroupName());
                    }
                }
                if (item.getUserId() !=null){
                    AgentUser agentUser = agentUserMap.get(item.getUserId());
                    if (agentUser == null){
                        if (!unValidUserMapCache.containsKey(item.getUserId())){
                            AgentUser unValidUser = baseOrgService.loadUnValidUser(item.getUserId());
                            if (null != unValidUser){
                                unValidUserMapCache.put(unValidUser.getId(),unValidUser);
                            }
                        }
                        agentUser = unValidUserMapCache.get(item.getUserId());
                    }
                    if (null != agentUser){
                        agentPerformanceGoalVO.setBusinessDeveloperId(item.getUserId());
                        agentPerformanceGoalVO.setBusinessDeveloperName(agentUser.getRealName());
                    }
                }
                agentPerformanceGoalVO.setSglSubjStBfGoal(item.getSglSubjStBfGoal());
                agentPerformanceGoalVO.setSglSubjLtBfGoal(item.getSglSubjLtBfGoal());
                agentPerformanceGoalVO.setSglSubjIncGoal(item.getSglSubjIncGoal());
                agentPerformanceGoalVO.setId(item.getId());
                agentPerformanceGoalVO.setConfirm(item.getConfirm());
                resultList.add(agentPerformanceGoalVO);
            });
            return resultList;
        }
        return new ArrayList<>();
    }


    /**
     * 根据groupId生成业绩目标数据，以及其对应子部门或专员的业绩数据，如果没有导入，则会默认生成一条
     * @param agentGroupId
     * @return
     */
    public Map<String,Object> generatePerformanceGoal(Long agentGroupId,int month){
        if (month <= 0){
            return null;
        }
        List<AgentPerformanceGoalVO> agentPerformanceGoalVOList = getAgentPerformanceGoalVOByMonthAndGroupId(month, agentGroupId);
        AgentGroup agentGroup = baseOrgService.getGroupById(agentGroupId);
        AgentGroupRoleType agentGroupRoleType = agentGroup.fetchGroupRoleType();
        if (null != agentGroupRoleType){
            Map<String,Object> performanceGoalMap = new HashMap<>();
            performanceGoalMap.put("agentGroupRoleType",agentGroupRoleType);
            switch (agentGroupRoleType){
                case Country:
                    AgentPerformanceGoalVO countryGoal = agentPerformanceGoalVOList.stream().filter(item -> Objects.equals(item.getAgentPerformanceGoalTypeDesc(), AgentPerformanceGoalType.COUNTRY.getDesc())).findFirst().orElse(null);
                    List<AgentPerformanceGoalVO> regionGroupGoals = agentPerformanceGoalVOList.stream().filter(item -> Objects.equals(item.getAgentPerformanceGoalTypeDesc(), AgentPerformanceGoalType.REGION_GROUP.getDesc())).collect(Collectors.toList());
                    if (null == countryGoal){
                        countryGoal = new AgentPerformanceGoalVO();
                        countryGoal.setAgentPerformanceGoalTypeDesc(AgentPerformanceGoalType.COUNTRY.getDesc());
                        countryGoal.setSglSubjIncGoal(0);
                        countryGoal.setSglSubjLtBfGoal(0);
                        countryGoal.setSglSubjStBfGoal(0);
                        countryGoal.setMonth(month);
                    }
                    if (CollectionUtils.isEmpty(regionGroupGoals)){
                        regionGroupGoals = new ArrayList<>();
                    }
                    Map<Long, AgentPerformanceGoalVO> regionGroupGoalsMap = regionGroupGoals.stream().collect(Collectors.toMap(AgentPerformanceGoalVO::getRegionGroupId, Function.identity(), (o1, o2) -> o1));

                    List<AgentGroup> regionGroupList = baseOrgService.getGroupListByParentId(agentGroupId);
                    if (CollectionUtils.isNotEmpty(regionGroupList)){
                        for (int i = 0; i < regionGroupList.size(); i++) {
                            AgentGroup item = regionGroupList.get(i);
                            if (!regionGroupGoalsMap.containsKey(item.getId())){
                                AgentPerformanceGoalVO agentPerformanceGoalVO = new AgentPerformanceGoalVO();
                                agentPerformanceGoalVO.setAgentPerformanceGoalTypeDesc(AgentPerformanceGoalType.REGION_GROUP.getDesc());
                                agentPerformanceGoalVO.setSglSubjIncGoal(0);
                                agentPerformanceGoalVO.setSglSubjLtBfGoal(0);
                                agentPerformanceGoalVO.setSglSubjStBfGoal(0);
                                agentPerformanceGoalVO.setMonth(month);
                                agentPerformanceGoalVO.setRegionGroupId(item.getId());
                                agentPerformanceGoalVO.setRegionGroupName(item.getGroupName());
                                agentPerformanceGoalVO.setConfirm(false);
                                regionGroupGoals.add(agentPerformanceGoalVO);
                            }
                        }
                    }
                    performanceGoalMap.put("parent",countryGoal);
                    performanceGoalMap.put("sub",regionGroupGoals);

                    break;
                case Region:
                    AgentPerformanceGoalVO regionGroupGoal = agentPerformanceGoalVOList.stream().filter(item -> Objects.equals(item.getRegionGroupId(),agentGroupId) && Objects.equals(item.getAgentPerformanceGoalTypeDesc(), AgentPerformanceGoalType.REGION_GROUP.getDesc())).findFirst().orElse(null);
                    List<AgentPerformanceGoalVO> subRegionGroupGoals = agentPerformanceGoalVOList.stream().filter(item -> Objects.equals(item.getRegionGroupId(),agentGroupId) && Objects.equals(item.getAgentPerformanceGoalTypeDesc(), AgentPerformanceGoalType.SUB_REGION_GROUP.getDesc())).collect(Collectors.toList());
                    if (null == regionGroupGoal){
                        regionGroupGoal = new AgentPerformanceGoalVO();
                        regionGroupGoal.setAgentPerformanceGoalTypeDesc(AgentPerformanceGoalType.REGION_GROUP.getDesc());
                        regionGroupGoal.setSglSubjIncGoal(0);
                        regionGroupGoal.setSglSubjLtBfGoal(0);
                        regionGroupGoal.setSglSubjStBfGoal(0);
                        regionGroupGoal.setMonth(month);
                        regionGroupGoal.setRegionGroupId(agentGroup.getParentId());
                        regionGroupGoal.setSubRegionGroupId(agentGroup.getId());
                        regionGroupGoal.setConfirm(false);
                        regionGroupGoal.setSubRegionGroupName(agentGroup.getGroupName());
                    }
                    List<AgentGroup> subRegionGroupList = baseOrgService.getGroupListByParentId(agentGroupId);
                    if (CollectionUtils.isEmpty(subRegionGroupGoals)){
                        subRegionGroupGoals = new ArrayList<>();
                    }
                    Map<Long, AgentPerformanceGoalVO> subRegionGroupGoalsMap = subRegionGroupGoals.stream().collect(Collectors.toMap(AgentPerformanceGoalVO::getSubRegionGroupId, Function.identity()));
                    for (int i = 0; i < subRegionGroupList.size(); i++) {
                        AgentGroup item = subRegionGroupList.get(i);
                        if (!subRegionGroupGoalsMap.containsKey(item.getId())){
                            AgentPerformanceGoalVO agentPerformanceGoalVO = new AgentPerformanceGoalVO();
                            agentPerformanceGoalVO.setAgentPerformanceGoalTypeDesc(AgentPerformanceGoalType.SUB_REGION_GROUP.getDesc());
                            agentPerformanceGoalVO.setSglSubjIncGoal(0);
                            agentPerformanceGoalVO.setSglSubjLtBfGoal(0);
                            agentPerformanceGoalVO.setSglSubjStBfGoal(0);
                            agentPerformanceGoalVO.setMonth(month);
                            agentPerformanceGoalVO.setRegionGroupId(item.getParentId());
                            agentPerformanceGoalVO.setSubRegionGroupName(item.getGroupName());
                            agentPerformanceGoalVO.setSubRegionGroupId(item.getId());
                            agentPerformanceGoalVO.setConfirm(regionGroupGoal.getConfirm());
                            subRegionGroupGoals.add(agentPerformanceGoalVO);
                        }
                    }
                    performanceGoalMap.put("parent",regionGroupGoal);
                    performanceGoalMap.put("sub",subRegionGroupGoals);


                    break;
                case City:
                    AgentPerformanceGoalVO subRegionGroupGoal = agentPerformanceGoalVOList.stream().filter(item -> Objects.equals(item.getSubRegionGroupId(),agentGroupId) && Objects.equals(item.getAgentPerformanceGoalTypeDesc(), AgentPerformanceGoalType.SUB_REGION_GROUP.getDesc())).findFirst().orElse(null);
                    List<AgentPerformanceGoalVO> businessDeveloperGoals = agentPerformanceGoalVOList.stream().filter(item -> Objects.equals(item.getSubRegionGroupId(),agentGroupId) && Objects.equals(item.getAgentPerformanceGoalTypeDesc(), AgentPerformanceGoalType.USER.getDesc())).collect(Collectors.toList());
                    if (null == subRegionGroupGoal){
                        subRegionGroupGoal = new AgentPerformanceGoalVO();
                        subRegionGroupGoal.setAgentPerformanceGoalTypeDesc(AgentPerformanceGoalType.SUB_REGION_GROUP.getDesc());
                        subRegionGroupGoal.setSglSubjIncGoal(0);
                        subRegionGroupGoal.setSglSubjLtBfGoal(0);
                        subRegionGroupGoal.setSglSubjStBfGoal(0);
                        subRegionGroupGoal.setMonth(month);
                        subRegionGroupGoal.setRegionGroupId(agentGroup.getParentId());
                        subRegionGroupGoal.setSubRegionGroupId(agentGroup.getId());
                        subRegionGroupGoal.setSubRegionGroupName(agentGroup.getGroupName());
                        AgentPerformanceGoal regionGroupPerformanceGoal = getRegionGroupPerformanceGoal(month, agentGroup.getParentId());
                        if (null != regionGroupPerformanceGoal){
                            subRegionGroupGoal.setConfirm(regionGroupPerformanceGoal.getConfirm());
                        }else {
                            subRegionGroupGoal.setConfirm(false);
                        }
                    }
                    List<AgentUser> groupBusinessDevelopers = baseOrgService.getGroupBusinessDevelopers(agentGroupId);
                    Map<Long, AgentPerformanceGoalVO> businessDeveloperGoalsMap = businessDeveloperGoals.stream().collect(Collectors.toMap(AgentPerformanceGoalVO::getBusinessDeveloperId, Function.identity()));
                    if (CollectionUtils.isEmpty(groupBusinessDevelopers)){
                        groupBusinessDevelopers = new ArrayList<>();
                    }
                    for (int i = 0; i < groupBusinessDevelopers.size(); i++) {
                        AgentUser item = groupBusinessDevelopers.get(i);
                        if (!businessDeveloperGoalsMap.containsKey(item.getId())) {
                            AgentPerformanceGoalVO agentPerformanceGoalVO = new AgentPerformanceGoalVO();
                            agentPerformanceGoalVO.setAgentPerformanceGoalTypeDesc(AgentPerformanceGoalType.USER.getDesc());
                            agentPerformanceGoalVO.setSglSubjIncGoal(0);
                            agentPerformanceGoalVO.setSglSubjLtBfGoal(0);
                            agentPerformanceGoalVO.setSglSubjStBfGoal(0);
                            agentPerformanceGoalVO.setMonth(month);
                            agentPerformanceGoalVO.setRegionGroupId(agentGroup.getParentId());
                            agentPerformanceGoalVO.setSubRegionGroupName(agentGroup.getGroupName());
                            agentPerformanceGoalVO.setSubRegionGroupId(agentGroup.getId());
                            agentPerformanceGoalVO.setBusinessDeveloperId(item.getId());
                            agentPerformanceGoalVO.setBusinessDeveloperName(item.getRealName());
                            agentPerformanceGoalVO.setConfirm(subRegionGroupGoal.getConfirm());
                            businessDeveloperGoals.add(agentPerformanceGoalVO);
                        }
                    }

                    performanceGoalMap.put("parent",subRegionGroupGoal);
                    performanceGoalMap.put("sub",businessDeveloperGoals);
                    performanceGoalMap.put("month",month);

                    break;
                default:
                    break;
            }
            return performanceGoalMap;
        }
        return null;
    }


    /**
     * 获取业绩目标，如果没有某些类型目标没有导入，则生成默认业绩目标
     * @param month
     * @param agentPerformanceGoalTypeDesc
     * @param regionGroupId
     * @param subRegionGroupId
     * @param businessDeveloperId
     * @return
     */
    public AgentPerformanceGoalVO generateAgentPerformanceGoalVO(Integer month,String agentPerformanceGoalTypeDesc,Long regionGroupId,Long subRegionGroupId,Long businessDeveloperId){
        AgentPerformanceGoalType agentPerformanceGoalType = AgentPerformanceGoalType.of(agentPerformanceGoalTypeDesc);
        if (null != agentPerformanceGoalType){
            switch (agentPerformanceGoalType){
                case REGION_GROUP:
                    List<AgentPerformanceGoalVO> regoinGroupGoalVOS = getAgentPerformanceGoalVOByMonthAndGroupId(month, regionGroupId);
                    AgentPerformanceGoalVO regionGroupGoalVO = regoinGroupGoalVOS.stream().filter(item -> Objects.equals(item.getAgentPerformanceGoalTypeDesc(), AgentPerformanceGoalType.REGION_GROUP.getDesc()) && Objects.equals(item.getRegionGroupId(), regionGroupId)).findFirst().orElse(null);
                    //如果没有，则生成一条
                    if (regionGroupGoalVO == null){
                        AgentGroup agentGroup = baseOrgService.getGroupById(regionGroupId);
                        regionGroupGoalVO = new AgentPerformanceGoalVO();
                        regionGroupGoalVO.setAgentPerformanceGoalTypeDesc(AgentPerformanceGoalType.REGION_GROUP.getDesc());
                        regionGroupGoalVO.setSglSubjIncGoal(0);
                        regionGroupGoalVO.setSglSubjLtBfGoal(0);
                        regionGroupGoalVO.setSglSubjStBfGoal(0);
                        regionGroupGoalVO.setMonth(month);
                        regionGroupGoalVO.setRegionGroupId(agentGroup.getId());
                        regionGroupGoalVO.setRegionGroupName(agentGroup.getGroupName());
                    }
                    return regionGroupGoalVO;
                case SUB_REGION_GROUP:
                    List<AgentPerformanceGoalVO> subRegoinGroupGoalVOS = getAgentPerformanceGoalVOByMonthAndGroupId(month, subRegionGroupId);
                    AgentPerformanceGoalVO subRegionGroupGoalVO = subRegoinGroupGoalVOS.stream().filter(item -> Objects.equals(item.getAgentPerformanceGoalTypeDesc(), AgentPerformanceGoalType.SUB_REGION_GROUP.getDesc()) && Objects.equals(item.getRegionGroupId(), regionGroupId) && Objects.equals(item.getSubRegionGroupId(), subRegionGroupId)).findFirst().orElse(null);
                    //如果没有，则生成一条
                    if (subRegionGroupGoalVO == null){
                        AgentGroup agentGroup = baseOrgService.getGroupById(subRegionGroupId);
                        subRegionGroupGoalVO = new AgentPerformanceGoalVO();
                        subRegionGroupGoalVO.setAgentPerformanceGoalTypeDesc(AgentPerformanceGoalType.SUB_REGION_GROUP.getDesc());
                        subRegionGroupGoalVO.setSglSubjIncGoal(0);
                        subRegionGroupGoalVO.setSglSubjLtBfGoal(0);
                        subRegionGroupGoalVO.setSglSubjStBfGoal(0);
                        subRegionGroupGoalVO.setMonth(month);
                        subRegionGroupGoalVO.setRegionGroupId(regionGroupId);
                        subRegionGroupGoalVO.setSubRegionGroupId(agentGroup.getId());
                        subRegionGroupGoalVO.setSubRegionGroupName(agentGroup.getGroupName());
                    }
                    return subRegionGroupGoalVO;
                case USER:
                    List<AgentPerformanceGoalVO> businessDeveloperGoalVOS = getAgentPerformanceGoalVOByMonthAndGroupId(month, subRegionGroupId);
                    AgentPerformanceGoalVO businessDeveloperGoalVO = businessDeveloperGoalVOS.stream().filter(item -> Objects.equals(item.getAgentPerformanceGoalTypeDesc(), AgentPerformanceGoalType.USER.getDesc()) && Objects.equals(item.getRegionGroupId(), regionGroupId) && Objects.equals(item.getSubRegionGroupId(), subRegionGroupId) && Objects.equals(item.getBusinessDeveloperId(), businessDeveloperId)).findFirst().orElse(null);
                    //如果没有，则生成一条
                    if (null == businessDeveloperGoalVO){
                        AgentUser agentUser = baseOrgService.getUser(businessDeveloperId);
                        businessDeveloperGoalVO = new AgentPerformanceGoalVO();
                        businessDeveloperGoalVO.setAgentPerformanceGoalTypeDesc(AgentPerformanceGoalType.USER.getDesc());
                        businessDeveloperGoalVO.setSglSubjIncGoal(0);
                        businessDeveloperGoalVO.setSglSubjLtBfGoal(0);
                        businessDeveloperGoalVO.setSglSubjStBfGoal(0);
                        businessDeveloperGoalVO.setMonth(month);
                        businessDeveloperGoalVO.setBusinessDeveloperId(agentUser.getId());
                        businessDeveloperGoalVO.setBusinessDeveloperName(agentUser.getRealName());
                        businessDeveloperGoalVO.setRegionGroupId(regionGroupId);
                        businessDeveloperGoalVO.setSubRegionGroupId(subRegionGroupId);
                    }
                    return businessDeveloperGoalVO;
                default:
                    break;
            }
        }
        return null;
    }

    /**
     * 获取当前年月int值
     * @return
     */
    public int getNowYearMonth(){
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
        String nowMonthStr = formatter.format(currentTime);
        return Integer.valueOf(nowMonthStr).intValue();
    }

    /**
     *
     * 获取下个月的int值.
     *
     * @return
     */
    public int getNexYearMonth() {
        SimpleDateFormat dft = new SimpleDateFormat("yyyyMM");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        String nextMonthStr = dft.format(calendar.getTime());
        return Integer.valueOf(nextMonthStr).intValue();
    }

}
