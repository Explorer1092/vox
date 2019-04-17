package com.voxlearning.utopia.admin.controller.equator.newwonderland.reporteddata;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.athena.api.UctUserService;
import com.voxlearning.equator.common.api.data.history.StudentHistoryInfo;
import com.voxlearning.equator.common.api.enums.history.ModuleType;
import com.voxlearning.equator.common.api.enums.material.MaterialProcessorType;
import com.voxlearning.equator.service.configuration.api.entity.material.Material;
import com.voxlearning.equator.service.configuration.client.ResourceConfigServiceClient;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceStaticFileInfo;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author JiaLu.Li
 * @since 2018/8/13
 */
@Controller
@RequestMapping("/equator/student/history/")
public class SomaliaReportedBigDataManagerController extends AbstractEquatorController {

    @Getter
    @ImportService(interfaceClass = UctUserService.class)
    private UctUserService uctUserService;

    @Inject
    private ResourceConfigServiceClient resourceConfigServiceClient;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String loadHistory(Model model) {
        Map<String, String> moduleTypes = new LinkedHashMap<>();
        moduleTypes.put("All", "-");
        moduleTypes.putAll(Stream.of(ModuleType.values()).filter(e -> !StringUtils.equals(e.name(), ModuleType.Invisible.name())).collect(Collectors.toMap(ModuleType::name, ModuleType::getDesc)));
        model.addAttribute("moduleTypes", moduleTypes);

        String time = getRequestString("time");
        if (StringUtils.isBlank(time)) {
            time = DateUtils.dateToString(new Date(), "yyyy-MM-dd");
        }
        model.addAttribute("time", time);
        time = StringUtils.removeAll(time, "-");

        Long studentId = getRequestLong("studentId");
        if (studentId == 0L) {
            return "equator/reporteddata/index";
        }
        model.addAttribute("studentId", studentId);

        String moduleType = getRequestString("moduleType");
        model.addAttribute("moduleType", moduleType);
        if (StringUtils.equals(moduleType, "All")) {
            moduleType = null;
        }

        List<String> jsonList = uctUserService.queryEquatorBehaviorLog(time, studentId, moduleType);

        List<Map<String, Object>> historyInfoList = new ArrayList<>();
        String finalType = moduleType;
        try {
            historyInfoList = jsonList.stream()
                    .filter(Objects::nonNull)
                    .map(e -> JsonUtils.fromJson(e, StudentHistoryInfo.class))
                    .filter(e -> StringUtils.isBlank(finalType) || StringUtils.equals(e.fetchModuleType(), finalType))
                    .filter(e -> !StringUtils.equals(e.fetchModuleType(), ModuleType.Invisible.name()))
                    .sorted(Comparator.comparing(StudentHistoryInfo::fetchCreateDate).reversed())
                    .map(e -> {
                                Map<String, Object> info = new HashMap<>();
                                info.put("moduleType", e.fetchModuleType());
                                info.put("behaviorDesc", e.getBehaviorDesc());
                                info.put("extInfo", MapUtils.isEmpty(e.getExtInfo()) ? "" : JsonUtils.toJson(e.getExtInfo()));
                                info.put("createTime", DateUtils.dateToString(new Date(e.getCreateTime()), DateUtils.FORMAT_SQL_DATETIME));
                                info.put("mode", e.getMode());
                                info.put("operator", e.getOperator());
                                return info;
                            }
                    ).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("SomaliaReportedBigDataManagerController index error", e);
        }

        model.addAttribute("historyInfoList", historyInfoList);

        return "equator/reporteddata/index";
    }

    @RequestMapping(value = "material.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String loadMaterialChangeInfo(Model model) {
        String time = getRequestString("time");
        if (StringUtils.isBlank(time)) {
            time = DateUtils.dateToString(new Date(), "yyyy-MM-dd");
        }
        model.addAttribute("time", time);
        time = StringUtils.removeAll(time, "-");

        Long studentId = getRequestLong("studentId");
        if (studentId == 0L) {
            return "equator/reporteddata/material";
        }
        model.addAttribute("studentId", studentId);

        List<String> jsonList = uctUserService.queryEquatorBehaviorLog(time, studentId, null);

        final Map<String, Map<String, Object>> targetDataMap = new HashMap<>();
        try {
            uctUserService.queryEquatorBehaviorLog(time, studentId, ModuleType.Invisible.name()).stream()
                    .filter(StringUtils::isNotBlank)
                    .map(e -> JsonUtils.fromJson(e, StudentHistoryInfo.class))
                    .filter(Objects::nonNull)
                    .forEach(e -> {
                        Map<String, Object> extInfo = e.getExtInfo();
                        if (MapUtils.isNotEmpty(extInfo)) {
                            List<Map<String, Object>> changeInfoCollection = (List<Map<String, Object>>) extInfo.get("Material_Change_Key");
                            if (CollectionUtils.isNotEmpty(changeInfoCollection)) {
                                changeInfoCollection.stream()
                                        .filter(MapUtils::isNotEmpty)
                                        .forEach(
                                                f -> targetDataMap.put(f.get("targetId") + "-" + f.get("materialId"), f)
                                        );
                            }
                        }
                    });
        } catch (Exception e) {
            logger.error("SomaliaReportedBigDataManagerController loadMaterialChangeInfo error", e);
        }

        List<Material> resource = resourceConfigServiceClient.getMaterialsFromBuffer();
        List<ResourceStaticFileInfo> staticResource = resourceConfigServiceClient.getResourceStaticFileInfoFromBuffer();

        Map<String, String> iconMap = staticResource
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> StringUtils.equals(e.getFirstCategory(), "NewGrowingWorld"))
                .filter(e -> StringUtils.equals(e.getSecondCategory(), "Resource"))
                .collect(Collectors.toMap(ResourceStaticFileInfo::getResourceName, ResourceStaticFileInfo::getUrl));


        List<Map<String, Object>> materialChangeList = new ArrayList<>();
        try {

            materialChangeList = jsonList
                    .stream()
                    .filter(StringUtils::isNotBlank)
                    .map(e -> JsonUtils.fromJson(e, StudentHistoryInfo.class))
                    .filter(Objects::nonNull)
                    .filter(e -> !StringUtils.equals(e.fetchModuleType(), ModuleType.Invisible.name()))
                    .sorted(Comparator.comparing(StudentHistoryInfo::fetchCreateDate).reversed())
                    .map(e -> {
                        Map<String, Object> extInfo = e.getExtInfo();
                        if (MapUtils.isEmpty(extInfo)) {
                            return null;
                        }
                        List<Map<String, Object>> changeInfoCollection = (List<Map<String, Object>>) extInfo.get("Material_Change_Key");
                        if (CollectionUtils.isEmpty(changeInfoCollection)) {
                            return null;
                        }

                        List<Object> groupList = changeInfoCollection
                                .stream()
                                .peek(f -> {
                                            String materialId = SafeConverter.toString(f.get("materialId"));
                                            Material material = resource.stream().filter(j -> StringUtils.equals(j.getId(), materialId))
                                                    .findFirst()
                                                    .orElse(null);
                                            if (material == null) {
                                                return;
                                            }

                                            int before = 0;
                                            int delta = SafeConverter.toInt(f.get("delta"));
                                            int after = 0;

                                            boolean addFlag = SafeConverter.toBoolean(f.get("addFlag"));

                                            //特殊类型
                                            if (StringUtils.equals(MaterialProcessorType.INTERNAL_OTHER.name(), material.getProcessorType())) {
                                                String targetId = f.get("targetId") + "-" + f.get("materialId");
                                                if (StringUtils.isNotBlank(targetId)) {
                                                    Map<String, Object> targetData = targetDataMap.get(targetId);
                                                    if (targetData != null) {
                                                        after = SafeConverter.toInt(targetData.get("after"));
                                                        before = SafeConverter.toInt(targetData.get("after")) - delta;
                                                    }
                                                }
                                            } else {
                                                after = SafeConverter.toInt(f.get("after"));
                                                before = addFlag ? after - delta : after + delta;
                                            }

                                            f.put("delta", Math.abs(delta));
                                            f.put("materialIcon", iconMap.get(material.getIcon()));
                                            f.put("materialName", material.getName());
                                            f.put("before", before);
                                            f.put("after", after);
                                            // f.put("createTime", DateUtils.dateToString(new Date(e.getCreateTime()), DateUtils.FORMAT_SQL_DATETIME));
                                            // f.put("desc", e.getBehaviorDesc());
                                            // f.put("operator", e.getOperator());
                                        }
                                ).collect(Collectors.toList());

                        Map<String, Object> group = new HashMap<>();
                        group.put("createTime", DateUtils.dateToString(new Date(e.getCreateTime()), DateUtils.FORMAT_SQL_DATETIME));
                        group.put("desc", e.getBehaviorDesc());
                        group.put("operator", e.getOperator());
                        group.put("dataList", groupList);

                        return group;

                    })
                    .filter(Objects::nonNull)
                    // .flatMap(List::stream)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("SomaliaReportedBigDataManagerController loadMaterialChangeInfo error", e);
        }

        model.addAttribute("materialChangeList", materialChangeList);
        return "equator/reporteddata/material";
    }
}

