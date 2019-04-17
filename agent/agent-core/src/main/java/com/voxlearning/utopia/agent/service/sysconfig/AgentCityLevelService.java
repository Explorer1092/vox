package com.voxlearning.utopia.agent.service.sysconfig;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.agent.dao.mongo.AgentCityLevelDao;
import com.voxlearning.utopia.agent.persist.entity.AgentCityLevel;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
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
 * AgentCityLevelService
 *
 * @author song.wang
 * @date 2018/2/8
 */
@Named
public class AgentCityLevelService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private BaseExcelService baseExcelService;
    @Inject
    private AgentCityLevelDao agentCityLevelDao;

    public Map<Integer, AgentCityLevelType> loadCityLevelTypeMap(Collection<Integer> regionCodes) {
        Map<Integer, AgentCityLevelType> resultMap = new HashMap<>();
        Map<Integer, AgentCityLevel> cityLevelMap = agentCityLevelDao.loads(regionCodes);
        cityLevelMap.forEach((k, v) -> resultMap.put(k, v.getLevel()));
        return resultMap;
    }

    public void updateCityLevel(Integer cityCode, AgentCityLevelType level) {
        AgentCityLevel cityLevel = agentCityLevelDao.load(cityCode);
        if (cityLevel == null) {
            if (level != null) {
                cityLevel = new AgentCityLevel();
                cityLevel.setId(cityCode);
                cityLevel.setLevel(level);
                agentCityLevelDao.insert(cityLevel);
            }
        } else {
            if (level != null) {
                cityLevel.setLevel(level);
                agentCityLevelDao.replace(cityLevel);
            } else {
                agentCityLevelDao.remove(cityCode);
            }
        }
    }


    public void generateWorkbookData(XSSFWorkbook workbook, List<ExRegion> regionList, boolean includeLevelData) {
        if (workbook == null || CollectionUtils.isEmpty(regionList)) {
            return;
        }
        Map<Integer, AgentCityLevelType> cityLevelTypeMap = new HashMap<>();
        if (includeLevelData) {
            cityLevelTypeMap.putAll(this.loadCityLevelTypeMap(regionList.stream().map(ExRegion::getId).collect(Collectors.toList())));
        }
        XSSFSheet sheet = workbook.getSheetAt(0);
        CellStyle cellStyle = baseExcelService.createCellStyle(workbook);
        int index = 1;
        for (ExRegion exRegion : regionList) {
            XSSFRow row = sheet.createRow(index++);
            XssfUtils.setCellValue(row, 0, cellStyle, exRegion.getCityName());
            XssfUtils.setCellValue(row, 1, cellStyle, exRegion.getId());
            if (includeLevelData) {
                AgentCityLevelType levelType = cityLevelTypeMap.get(exRegion.getId());
                XssfUtils.setCellValue(row, 2, cellStyle, levelType == null ? "" : levelType.getValue());
            } else {
                XssfUtils.setCellValue(row, 2, cellStyle, "");
            }
        }
    }


    public MapMessage importCityLevel(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet == null) {
            return MapMessage.errorMessage();
        }

        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadAllRegions();
        exRegionMap = exRegionMap.values().stream().filter(p -> p.fetchRegionType() == RegionType.CITY).collect(Collectors.toMap(ExRegion::getId, Function.identity(), (o1, o2) -> o1));

        Map<Integer, AgentCityLevel> cityLevelMap = new HashMap<>();
        List<Integer> errorRowList = new ArrayList<>();
        boolean checkResult = true;
        int rowNo = 1;
        while (true) {
            XSSFRow row = sheet.getRow(rowNo++);
            if (row == null) {
                break;
            }

            String cityName = XssfUtils.getStringCellValue(row.getCell(0));
            Integer cityCode = XssfUtils.getIntCellValue(row.getCell(1));
            String level = XssfUtils.getStringCellValue(row.getCell(2));
            // 整行没有数据的情况下结束
            if (StringUtils.isBlank(cityName) && cityCode == null && StringUtils.isBlank(level)) {
                break;
            }

            if (StringUtils.isBlank(cityName) || cityCode == null) { // 城市名称和行政代码为空
                checkResult = false;
                errorRowList.add(rowNo);
            } else {
                ExRegion exRegion = exRegionMap.get(cityCode);
                if (exRegion == null || !Objects.equals(exRegion.getName(), cityName)) {  // 城市名称和行政代码不匹配
                    checkResult = false;
                    errorRowList.add(rowNo);
                } else {
                    if (checkResult) {   // 校验成功的情况下
                        if (StringUtils.isNotBlank(level)) {   // 判断级别是否为空
                            if (AgentCityLevelType.descOf(StringUtils.trim(level)) == null) {  // 判断枚举值是否正确
                                checkResult = false;
                                errorRowList.add(rowNo);
                            } else {
                                AgentCityLevel cityLevel = new AgentCityLevel();
                                cityLevel.setId(cityCode);
                                cityLevel.setLevel(AgentCityLevelType.descOf(StringUtils.trim(level)));
                                cityLevelMap.put(cityCode, cityLevel);
                            }
                        }
                    }
                }
            }
        }

        // 数据有误的情形， 返回有误的数据信息
        if (!checkResult) {
            List<String> errorList = errorRowList.stream().map(errorRow -> "第" + errorRow + "行数据有误！").collect(Collectors.toList());
            return MapMessage.errorMessage().add("errorList", errorList);
        }

        Set<Integer> deleteRegions = exRegionMap.keySet().stream().filter(p -> !cityLevelMap.containsKey(p)).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(deleteRegions)) {
            agentCityLevelDao.removes(deleteRegions);
        }
        if (MapUtils.isNotEmpty(cityLevelMap)) {
            cityLevelMap.values().forEach(agentCityLevelDao::upsert);
        }
        return MapMessage.successMessage();
    }

}
