package com.voxlearning.utopia.agent.service.sysconfig;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.bean.AgentSchoolBudgetData;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentSchoolBudget;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentSchoolBudgetLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentSchoolBudgetServiceClient;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author chunlin.yu
 * @create 2017-08-10 10:42
 **/
@Named
public class AgentSchoolBudgetService   extends AbstractAgentService {

    @Inject
    AgentSchoolBudgetServiceClient agentSchoolBudgetServiceClient;

    @Inject
    AgentSchoolBudgetLoaderClient agentSchoolBudgetLoaderClient;

    @Inject
    AgentDictSchoolService agentDictSchoolService;
    @Inject
    private BaseDictService baseDictService;


    /**
     * 业绩数据月
     */
    private final static Integer[] DEFAULT_BUDGET_MONTH = new Integer[]{201709, 201710, 201711, 201712};

    public MapMessage importBudget(XSSFWorkbook workbook){
        List<AgentSchoolBudgetData> agentSchoolBudgetDataList = convert2AgentSchoolBudget(workbook);
        MapMessage mapMessage = validateAgentSchoolBudget(agentSchoolBudgetDataList);
        if (mapMessage.isSuccess()){
            mapMessage = upserts(agentSchoolBudgetDataList);
        }
        return mapMessage;
    }

    public MapMessage upserts(List<AgentSchoolBudgetData> agentSchoolBudgetDataList){
        List<AgentSchoolBudget> agentSchoolBudgetList = new ArrayList<>();
        agentSchoolBudgetDataList.forEach(item -> {
            agentSchoolBudgetList.add(item.toAgentSchoolBudget());
        });
        return upsertAgentSchoolBudgets(agentSchoolBudgetList);
    }

    public MapMessage upsertAgentSchoolBudgets(Collection<AgentSchoolBudget> agentSchoolBudgets){
        MapMessage mapMessage = new MapMessage();
        if (CollectionUtils.isNotEmpty(agentSchoolBudgets)){
            int allDealSchoolBudgetCount = agentSchoolBudgets.size();
            Map<Integer,Integer> monthAddMap = new HashMap<>();
            Map<Integer,Integer> monthUpdateMap = new HashMap<>();
            Set<Integer> monthSet = new TreeSet<>(Integer::compareTo);
            List<AgentSchoolBudget> insertList = new ArrayList<>();
            List<AgentSchoolBudget> updateList = new ArrayList<>();
            agentSchoolBudgets.forEach(item -> {
                List<AgentSchoolBudget> tempList = agentSchoolBudgetLoaderClient.loadBySchoolId(item.getSchoolId());
                String documentId = null;
                if (null != tempList ){
                    AgentSchoolBudget agentSchoolBudget = tempList.stream().filter(p -> Objects.equals( p.getMonth(),item.getMonth())).findFirst().orElse(null);
                    if (null != agentSchoolBudget){
                        documentId = agentSchoolBudget.getId();
                    }
                }
                if (!monthAddMap.containsKey(item.getMonth())){
                    monthAddMap.put(item.getMonth(),0);
                }
                if (!monthUpdateMap.containsKey(item.getMonth())){
                    monthUpdateMap.put(item.getMonth(),0);
                }
                if (null == item.getDisabled()){
                    item.setDisabled(false);
                }
                if (null ==documentId){
                    insertList.add(item);
                    monthAddMap.put(item.getMonth(),monthAddMap.get(item.getMonth())+1);
                }else{
                    item.setId(documentId);
                    updateList.add(item);
                    monthUpdateMap.put(item.getMonth(),monthUpdateMap.get(item.getMonth())+1);
                }
                monthSet.add(item.getMonth());
            });
            if (CollectionUtils.isNotEmpty(insertList)){
                Map<Integer, List<AgentSchoolBudget>> listMap = insertList.stream().collect(Collectors.groupingBy(p -> insertList.indexOf(p) / 200, Collectors.toList()));
                listMap.forEach((k,v) ->{
                    if (CollectionUtils.isNotEmpty(v)){
                        agentSchoolBudgetServiceClient.inserts(v);
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(updateList)){
                Map<Integer, List<AgentSchoolBudget>> listMap = updateList.stream().collect(Collectors.groupingBy(p -> updateList.indexOf(p) / 200, Collectors.toList()));
                listMap.forEach((k,v) ->{
                    if (CollectionUtils.isNotEmpty(v)){
                        agentSchoolBudgetServiceClient.updates(v);
                    }
                });
            }
            mapMessage.setSuccess(true);
            mapMessage.put("allDealSchoolBudgetCount",allDealSchoolBudgetCount);
            mapMessage.put("monthAddMap",monthAddMap);
            mapMessage.put("monthUpdateMap",monthUpdateMap);
            mapMessage.put("monthSet", monthSet);
        }
        return mapMessage;
    }

    public List<AgentSchoolBudgetData> loadBySchoolId(Long schoolId) {
        List<AgentSchoolBudget> agentSchoolBudgetList = agentSchoolBudgetLoaderClient.loadBySchoolId(schoolId);
        List<AgentSchoolBudgetData> agentSchoolBudgetDataList = AgentSchoolBudgetData.fromAgentSchoolBudget(agentSchoolBudgetList);
        List<AgentSchoolBudgetData> resultMap = new ArrayList<>();
        Map<Integer, AgentSchoolBudgetData> temp = agentSchoolBudgetDataList.stream().collect(Collectors.toMap(AgentSchoolBudgetData::getMonth, Function.identity()));
        for (int i = 0; i < DEFAULT_BUDGET_MONTH.length; i++) {
            if (temp.get(DEFAULT_BUDGET_MONTH[i]) == null) {
                AgentSchoolBudgetData data = new AgentSchoolBudgetData();
                data.setMonth(DEFAULT_BUDGET_MONTH[i]);
                resultMap.add(data);
            } else {
                resultMap.add(temp.get(DEFAULT_BUDGET_MONTH[i]));
            }
        }
        return resultMap;
    }

    private MapMessage validateAgentSchoolBudget(List<AgentSchoolBudgetData> agentSchoolBudgetDataList){
        MapMessage resultMessage = MapMessage.errorMessage();
        if (CollectionUtils.isNotEmpty(agentSchoolBudgetDataList)){
            Map<Long, AgentDictSchool> agentDictSchoolMap = baseDictService.loadAllSchoolDictData().stream().collect(Collectors.toMap(AgentDictSchool::getSchoolId, Function.identity()));
            Set<Long> schoolIds = agentSchoolBudgetDataList.stream().filter(item -> null != item.getSchoolId()).map(AgentSchoolBudgetData::getSchoolId).distinct().collect(Collectors.toSet());
            Map<Long, CrmSchoolSummary> crmSchoolSummaryMap = agentDictSchoolService.batchLoadCrmSchoolSummaryAndSchool(schoolIds);
            List<String> errorList = new ArrayList<>();
            for (int i = 0; i < agentSchoolBudgetDataList.size(); i++) {
                int rows = i + 2;
                AgentSchoolBudgetData item = agentSchoolBudgetDataList.get(i);
                AgentDictSchool agentDictSchool = agentDictSchoolMap.get(item.getSchoolId());
                CrmSchoolSummary crmSchoolSummary = crmSchoolSummaryMap.get(item.getSchoolId());
                if (item.getSchoolId() == null || null ==crmSchoolSummary) {
                    errorList.add(rows + "行:学校ID不存在。");
                    continue;
                }
                if (null == agentDictSchool){
                    errorList.add(rows + "行:学校不在字典表中。");
                    continue;
                }

                if (null == item.getMonth() || item.getMonth() == 0 || item.getMonth() < 200000 || item.getMonth()>210000){
                    errorList.add(rows + "行:月份格式不正确。");
                    continue;
                }
                if (Objects.equals(crmSchoolSummary.getSchoolLevel(),SchoolLevel.JUNIOR)){
                    if (null == item.getPermeability()){
                        errorList.add(rows + "行:渗透情况为空，或内容不正确");
                        continue;
                    }
                    if (null == item.getSglSubjIncBudget() || null == item.getSglSubjLtBfBudget() || null == item.getSglSubjStBfBudget()){
                        errorList.add(rows + "行:小单新增目标、小单长回目标、小单短回目标数据都必填。");
                        continue;
                    }
                    item.setEngBudget(null);
                    item.setMathAnshBfBudget(null);
                    item.setMathAnshIncBudget(null);
                }else if (SchoolLevel.MIDDLE.equals(crmSchoolSummary.getSchoolLevel()) || SchoolLevel.HIGH.equals(crmSchoolSummary.getSchoolLevel())){
                    if (null == item.getPermeability() && null != item.getPermeabilityString()){
                        errorList.add(rows + "行:渗透情况内容不正确。");
                        continue;

                    }
                    if (null == item.getEngBudget() || null == item.getMathAnshBfBudget() || null == item.getMathAnshIncBudget()){
                        errorList.add(rows + "行:英语月活预算、数扫的新增预算、数扫的回流预算数据都必填。");
                        continue;
                    }
                    item.setSglSubjIncBudget(null);
                    item.setSglSubjLtBfBudget(null);
                    item.setSglSubjStBfBudget(null);

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


    private List<AgentSchoolBudgetData> convert2AgentSchoolBudget(XSSFWorkbook workbook){
        List<AgentSchoolBudgetData> resultList = new ArrayList<>();
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        int rows =1 ;
        if (null != sheet){
            while (true) {
                try {
                    XSSFRow row = sheet.getRow(rows++);
                    if (row == null) {
                        break;
                    }
                    Long schoolId = XssfUtils.getLongCellValue(row.getCell(0));
                    String schoolName = XssfUtils.getStringCellValue(row.getCell(1));
                    Integer month = XssfUtils.getIntCellValue(row.getCell(2));
                    String permeability = XssfUtils.getStringCellValue(row.getCell(3));
                    Integer sglSubjIncBudget = XssfUtils.getIntCellValue(row.getCell(4));        // 预算1
                    Integer sglSubjLtBfBudget = XssfUtils.getIntCellValue(row.getCell(5));          // 预算2
                    Integer sglSubjStBfBudget = XssfUtils.getIntCellValue(row.getCell(6));
                    Integer engBudget = XssfUtils.getIntCellValue(row.getCell(7));
                    Integer mathAnshIncBudget = XssfUtils.getIntCellValue(row.getCell(8));
                    Integer mathAnshBfBudget = XssfUtils.getIntCellValue(row.getCell(9));
                    AgentSchoolBudgetData agentSchoolBudgetData = new AgentSchoolBudgetData();
                    agentSchoolBudgetData.setSchoolId(schoolId);
                    agentSchoolBudgetData.setSchoolName(schoolName);
                    agentSchoolBudgetData.setMonth(month);
                    if (StringUtils.isNotEmpty(permeability)){
                        agentSchoolBudgetData.setPermeability(AgentSchoolPermeabilityType.of(permeability));
                        agentSchoolBudgetData.setPermeabilityString(permeability);
                    }
                    agentSchoolBudgetData.setSglSubjIncBudget(sglSubjIncBudget);
                    agentSchoolBudgetData.setSglSubjLtBfBudget(sglSubjLtBfBudget);
                    agentSchoolBudgetData.setSglSubjStBfBudget(sglSubjStBfBudget);
                    agentSchoolBudgetData.setEngBudget(engBudget);
                    agentSchoolBudgetData.setMathAnshIncBudget(mathAnshIncBudget);
                    agentSchoolBudgetData.setMathAnshBfBudget(mathAnshBfBudget);
                    resultList.add(agentSchoolBudgetData);
                } catch (Exception ex) {
                    logger.error("read excel failed", ex);
                    break;
                }
            }
        }
        return resultList;
    }
}
