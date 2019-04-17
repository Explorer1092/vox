package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseExcelService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentCityLevelService;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AgentCityLevelController
 *
 * @author song.wang
 * @date 2018/2/8
 */
@Controller
@RequestMapping("/sysconfig/citylevel")
public class AgentCityLevelController extends AbstractAgentController {


    private static final String CITY_LEVEL_TEMPLATE = "/config/templates/city_level_template.xlsx";

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private BaseExcelService baseExcelService;
    @Inject
    private AgentCityLevelService agentCityLevelService;


    @RequestMapping(value = "index.vpage")
    @OperationCode("41a4dd8dff264cad")
    public String index(Model model) {
        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadAllRegions();
        List<Integer> cityRegions = exRegionMap.values().stream().filter(p -> p.fetchRegionType() == RegionType.CITY).map(ExRegion::getId).collect(Collectors.toList());
        Map<Integer, AgentCityLevelType> cityLevelTypeMap = agentCityLevelService.loadCityLevelTypeMap(cityRegions);

        List<Map<String, Object>> cityList = new ArrayList<>();
        Map<String, Object> itemMap;
        for (Integer cityRegion : cityRegions) {
            ExRegion exRegion = exRegionMap.get(cityRegion);
            itemMap = new HashMap<>();
            itemMap.put("cityName", exRegion.getName());
            itemMap.put("cityCode", exRegion.getId());
            AgentCityLevelType cityLevelType = cityLevelTypeMap.get(cityRegion);
            if (cityLevelType == null) {
                itemMap.put("level", "");
            } else {
                itemMap.put("level", cityLevelType.getValue());
            }
            cityList.add(itemMap);
        }

        model.addAttribute("cityList", cityList);
        return "/sysconfig/citylevel/index";
    }


    @RequestMapping(value = "export_data.vpage")
    public void downloadCityLevel(HttpServletResponse response) {
        boolean includeLevelData = getRequestBool("includeLevelData");
        XSSFWorkbook workbook = baseExcelService.readWorkBookFromTemplate(CITY_LEVEL_TEMPLATE);
        if (workbook == null) {
            try {
                response.setHeader("Content-type", "text/html;charset=UTF-8");
                response.getWriter().write("模板不存在");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Map<Integer, ExRegion> exRegionMap = raikouSystem.getRegionBuffer().loadAllRegions();
        List<ExRegion> regionList = exRegionMap.values().stream().filter(p -> p.fetchRegionType() == RegionType.CITY).collect(Collectors.toList());
        agentCityLevelService.generateWorkbookData(workbook, regionList, includeLevelData);
        try {
            String filename = "城市等级" + DateUtils.dateToString(new Date(), "yyyy-MM-dd") + ".xlsx";
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            workbook.write(outStream);
            try {
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        filename,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
            } catch (IOException ignored) {
                getResponse().getWriter().write("不能下载");
                getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {

        }
    }


    @RequestMapping(value = "import_data.vpage")
    @ResponseBody
    public MapMessage importData() {
        XSSFWorkbook workbook = baseExcelService.readRequestWorkbook(getRequest(), "sourceExcelFile");
        if (workbook == null) {
            return MapMessage.errorMessage();
        }
        return agentCityLevelService.importCityLevel(workbook);
    }


    @RequestMapping(value = "detail.vpage")
    @ResponseBody
    public MapMessage cityLevelDetail() {
        Integer cityCode = requestInteger("cityCode");
        if (cityCode == null) {
            return MapMessage.errorMessage("城市行政代码有误");
        }

        ExRegion exRegion = raikouSystem.loadRegion(cityCode);
        if (exRegion == null || exRegion.fetchRegionType() != RegionType.CITY) {
            return MapMessage.errorMessage("城市行政代码有误");
        }
        MapMessage message = MapMessage.successMessage();
        message.put("cityCode", cityCode);
        message.put("cityName", exRegion.getName());

        Map<String, String> levelList = new LinkedHashMap<>();
        levelList.put(AgentCityLevelType.CityLevelS.name(), AgentCityLevelType.CityLevelS.getValue());
        levelList.put(AgentCityLevelType.CityLevelA.name(), AgentCityLevelType.CityLevelA.getValue());
        levelList.put(AgentCityLevelType.CityLevelB.name(), AgentCityLevelType.CityLevelB.getValue());
        levelList.put(AgentCityLevelType.CityLevelC.name(), AgentCityLevelType.CityLevelC.getValue());
        message.put("levelList", levelList);

        Map<Integer, AgentCityLevelType> cityLevelMap = agentCityLevelService.loadCityLevelTypeMap(Collections.singleton(cityCode));
        AgentCityLevelType level = cityLevelMap.get(cityCode);
        message.put("level", level);

        return message;
    }


    @RequestMapping(value = "update.vpage")
    @ResponseBody
    public MapMessage cityLevelUpdate() {
        Integer cityCode = requestInteger("cityCode");
        String level = requestString("level");
        if (cityCode == null) {
            return MapMessage.errorMessage("城市行政代码有误");
        }

        ExRegion exRegion = raikouSystem.loadRegion(cityCode);
        if (exRegion == null || exRegion.fetchRegionType() != RegionType.CITY) {
            return MapMessage.errorMessage("城市行政代码有误");
        }
        if (StringUtils.isNotBlank(level) && AgentCityLevelType.nameOf(level) == null) {
            return MapMessage.errorMessage("城市级别有误");
        }

        agentCityLevelService.updateCityLevel(cityCode, AgentCityLevelType.nameOf(level));
        return MapMessage.successMessage();
    }


}
