package com.voxlearning.utopia.admin.controller.equator.blacklist;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.service.configuration.api.constant.blacklist.EquatorBlackListModuleType;
import com.voxlearning.equator.service.configuration.api.constant.blacklist.EquatorBlackListType;
import com.voxlearning.equator.service.configuration.api.entity.blacklist.EquatorBlackListInfo;
import com.voxlearning.equator.service.configuration.client.EquatorBlackListClient;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author JiaLu.Li
 * @createTime 2018/12/21
 */
@Controller
@RequestMapping(value = "equator/config/blacklist")
public class EquatorBlackListController extends AbstractEquatorController {

    // 学生类型黑名单
    private static Map<String, String> STUDENT_BLACK_TYPES = Stream.of(EquatorBlackListType.Black_Student, EquatorBlackListType.White_Student).collect(Collectors.toMap(Enum::name, EquatorBlackListType::getDecs));
    // 学校类型黑名单
    private static Map<String, String> SCHOOL_BLACK_TYPES = Stream.of(EquatorBlackListType.Black_School).collect(Collectors.toMap(Enum::name, EquatorBlackListType::getDecs));
    // 地区类型黑名单
    private static Map<String, String> REGION_BLACK_TYPES = Stream.of(EquatorBlackListType.Black_Region).collect(Collectors.toMap(Enum::name, EquatorBlackListType::getDecs));
    // 黑名单模块类型
    private static Map<String, String> MODULE_TYPES = Stream.of(EquatorBlackListModuleType.values()).collect(Collectors.toMap(Enum::name, EquatorBlackListModuleType::getDecs));

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private EquatorBlackListClient equatorBlackListClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("studentTypeList", STUDENT_BLACK_TYPES);
        model.addAttribute("schoolTypeList", SCHOOL_BLACK_TYPES);

        model.addAttribute("typeList", new HashMap<String, String>() {{
            putAll(STUDENT_BLACK_TYPES);
            putAll(SCHOOL_BLACK_TYPES);
        }});
        model.addAttribute("moduleList", MODULE_TYPES);

        String type = getRequestString("type").trim();
        String module = getRequestString("module").trim();
        String tagValue = getRequestString("tagValue").trim();

        if (StringUtils.isBlank(type)) {
            return "equator/blacklist/index";
        }

        model.addAttribute("specType", type);
        model.addAttribute("specModule", module);
        model.addAttribute("specTagValue", tagValue);

        List<EquatorBlackListInfo> blackLists = fetchSpecEquatorBlackLists(type, module, tagValue);

        List<Map<String, Object>> blackListDetails;
        if (STUDENT_BLACK_TYPES.containsKey(type)) {
            List<Long> taggedStudentIds = blackLists.stream().map(v -> SafeConverter.toLong(v.fetchTagValue())).collect(Collectors.toList());
            Map<Long, StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(taggedStudentIds);

            blackListDetails = blackLists
                    .stream()
                    .map(v -> {
                        StudentDetail studentDetail = studentDetails.get(SafeConverter.toLong(v.fetchTagValue()));
                        EquatorBlackListModuleType moduleType = EquatorBlackListModuleType.safeParse(v.fetchBlackListModuleType());

                        return MapUtils.m(
                                "id", v.fetchTagValue(),
                                "name", studentDetail == null ? "" : studentDetail.fetchRealname(),
                                "type", v.fetchBlackListType(),
                                "module", MapUtils.m("value", v.fetchBlackListModuleType(), "desc", moduleType.getDecs()),
                                "createTime", v.getCreateTime()
                        );
                    })
                    .collect(Collectors.toList());
            model.addAttribute("isStudent", true);
        } else {
            List<Long> taggedSchoolIds = blackLists.stream().map(v -> SafeConverter.toLong(v.fetchTagValue())).collect(Collectors.toList());
            Map<Long, School> schoolDetails = raikouSystem.loadSchools(taggedSchoolIds);

            blackListDetails = blackLists
                    .stream()
                    .map(v -> {
                        School school = schoolDetails.get(SafeConverter.toLong(v.fetchTagValue()));
                        EquatorBlackListModuleType moduleType = EquatorBlackListModuleType.safeParse(v.fetchBlackListModuleType());

                        return MapUtils.m(
                                "id", v.fetchTagValue(),
                                "name", school == null ? "" : school.getCname(),
                                "type", v.fetchBlackListType(),
                                "module", MapUtils.m("value", v.fetchBlackListModuleType(), "desc", moduleType.getDecs()),
                                "createTime", v.getCreateTime()
                        );
                    })
                    .collect(Collectors.toList());

        }

        model.addAttribute("blackLists", blackListDetails);

        return "equator/blacklist/index";
    }

    @RequestMapping(value = "insert.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage insert() {
        String type = getRequestString("type").trim();
        String module = getRequestString("module").trim();
        String tagValue = getRequestString("tagValue").trim();

        if (isIllegalParam(type, module, tagValue)) {
            return MapMessage.errorMessage("输入的参数有误！");
        }

        if (CollectionUtils.isNotEmpty(fetchSpecEquatorBlackLists(type, module, tagValue))) {
            return MapMessage.errorMessage("已存在！");
        }

        if (StringUtils.equals(type, EquatorBlackListType.White_Student.name())) {
            // 如果是白名单, 会把同类型、同模块下的黑名单删掉
            List<EquatorBlackListInfo> otherValues = fetchSpecEquatorBlackLists(EquatorBlackListType.Black_Student.name(), module, tagValue);
            if (CollectionUtils.isNotEmpty(otherValues)) {
                equatorBlackListClient.getRemoteReference().removeBlackList(otherValues);
            }
        } else if (StringUtils.equals(type, EquatorBlackListType.Black_Student.name())) {
            // 如果是黑名单, 同类型、同模块存在白名单, 需要先将白名单删掉
            List<EquatorBlackListInfo> otherValues = fetchSpecEquatorBlackLists(EquatorBlackListType.White_Student.name(), module, tagValue);
            if (CollectionUtils.isNotEmpty(otherValues)) {
                return MapMessage.errorMessage("用户对应类型、对应模块下已存在白名单，请先删除白名单！");
            }
        }

        return equatorBlackListClient.getRemoteReference().insertBlackList(new EquatorBlackListInfo(type, module, tagValue)).getUninterruptibly();
    }

    @RequestMapping(value = "remove.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage remove() {
        String type = getRequestString("type").trim();
        String module = getRequestString("module").trim();
        String tagValue = getRequestString("tagValue").trim();

        if (isIllegalParam(type, module, tagValue)) {
            return MapMessage.errorMessage("输入的参数有误！");
        }

        return equatorBlackListClient.getRemoteReference().removeBlackList(new EquatorBlackListInfo(type, module, tagValue)).getUninterruptibly();
    }

    @RequestMapping(value = "regionindex.vpage", method = RequestMethod.GET)
    public String regionIndex(Model model) {
        model.addAttribute("typeList", REGION_BLACK_TYPES);
        model.addAttribute("moduleList", MODULE_TYPES);

        String type = getRequestString("type").trim();
        String module = getRequestString("module").trim();
        if (StringUtils.isAnyBlank(type, module)) {
            type = "Black_Region";
            module = "Entrance";
        }

        if (isIllegalParamForRegion(type, module, new ArrayList<>())) {
            return "equator/blacklist/regionindex";
        }

        List<EquatorBlackListInfo> equatorBlackLists = fetchSpecEquatorBlackLists(type, module, "");
        // 指定类型、模块的所有地区Code
        List<String> regionCodes = equatorBlackLists.stream().map(EquatorBlackListInfo::fetchTagValue).collect(Collectors.toList());

        model.addAttribute("allBuildRegionTrees", JsonUtils.toJson(buildRegionTree(regionCodes)));

        return "equator/blacklist/regionindex";
    }

    @RequestMapping(value = "saveregions.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRegions() {
        String type = getRequestString("type").trim();
        String module = getRequestString("module").trim();
        String tagValues = getRequestString("values").trim();

        List<String> regionCodes;
        try {
            regionCodes = Arrays.asList(StringUtils.split(tagValues, ","));
        } catch (Exception e) {
            return MapMessage.errorMessage("地区信息解析异常！");
        }

        if (isIllegalParamForRegion(type, module, regionCodes)) {
            return MapMessage.errorMessage("输入的参数有误！");
        }

        // 获取之前对应类型、模块下的所有地区
        List<EquatorBlackListInfo> blackLists = fetchSpecEquatorBlackLists(type, module, "");

        // 从oldBlackLists中排除的地区
        List<EquatorBlackListInfo> untaggedBlackLists = blackLists
                .stream()
                .filter(v -> !regionCodes.contains(v.fetchTagValue()))
                .collect(Collectors.toList());

        if (untaggedBlackLists.size() > 0) {
            equatorBlackListClient.getRemoteReference().removeBlackList(untaggedBlackLists);
        }

        if (regionCodes.size() > 0) {
            List<EquatorBlackListInfo> taggedBlackLists = new ArrayList<EquatorBlackListInfo>() {{
                addAll(regionCodes.stream().map(v -> new EquatorBlackListInfo(type, module, v)).collect(Collectors.toList()));
            }};
            equatorBlackListClient.getRemoteReference().insertBlackList(taggedBlackLists);
        }

        return MapMessage.successMessage();
    }

    @RequestMapping(value = "check.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage checkOtherValue() {
        String type = getRequestString("type").trim();
        String module = getRequestString("module").trim();
        String tagValue = getRequestString("tagValue").trim();
        if (StringUtils.isAnyBlank(type, module, tagValue)) {
            return MapMessage.errorMessage("输入的参数有误");
        }

        String theOtherType;
        if (StringUtils.equals(EquatorBlackListType.White_Student.name(), type)) {
            theOtherType = EquatorBlackListType.Black_Student.name();
        } else if (StringUtils.equals(EquatorBlackListType.Black_Student.name(), type)) {
            theOtherType = EquatorBlackListType.White_Student.name();
        } else {
            return MapMessage.successMessage().add("result", false);
        }

        boolean result = false;
        List<EquatorBlackListInfo> theOtherValues = fetchSpecEquatorBlackLists(theOtherType, module, tagValue);
        if (CollectionUtils.isNotEmpty(theOtherValues)) {
            result = true;
        }

        return MapMessage.successMessage().add("result", result);
    }

    // 参数检测, 非法返回true
    private boolean isIllegalParam(String type, String module, String tagValue) {
        if (StringUtils.isAnyBlank(type, module, tagValue)) {
            return true;
        }

        EquatorBlackListType blackListType = EquatorBlackListType.safeParse(type);
        EquatorBlackListModuleType blackListModuleType = EquatorBlackListModuleType.safeParse(module);
        if (blackListType == null || blackListModuleType == null) {
            return true;
        }

        // 如果是学生类型黑名单
        if (STUDENT_BLACK_TYPES.containsKey(type)) {
            Long studentId = SafeConverter.toLong(tagValue);
            User user = raikouSystem.loadUser(studentId);
            return user == null || !user.isStudent();
        } else if (SCHOOL_BLACK_TYPES.containsKey(type)) {
            // 如果是学校类型黑名单
            Long schoolId = SafeConverter.toLong(tagValue);
            School school = raikouSystem.loadSchool(schoolId);
            return school == null;
        }

        return true;
    }

    private boolean isIllegalParamForRegion(String type, String module, List<String> tagValues) {
        if (StringUtils.isAnyBlank(type, module)) {
            return true;
        }

        EquatorBlackListType blackListType = EquatorBlackListType.safeParse(type);
        EquatorBlackListModuleType blackListModuleType = EquatorBlackListModuleType.safeParse(module);
        if (blackListType == null || blackListModuleType == null) {
            return true;
        }

        for (String tagValue : tagValues) {
            if (StringUtils.isBlank(tagValue)) {
                return true;
            }
            Integer regionCode = SafeConverter.toInt(tagValue);
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            if (exRegion == null) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> buildAllRegionTree() {
        // 获取所有地区信息
        Map<Integer, ExRegion> allRegions = raikouSystem.getRegionBuffer().loadAllRegions();

        // 初始化所有地区信息
        Map<String, Map<String, Object>> transformedRegions = allRegions.entrySet()
                .stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getKey() != null && v.getValue() != null)
                .map(v -> {
                    ExRegion exRegion = v.getValue();
                    return new HashMap<String, Object>() {{
                        put("key", SafeConverter.toString(v.getKey()));
                        put("title", exRegion.getName());
                        put("children", new ArrayList<>());
                        put("icon", false);

                        int pCode = SafeConverter.toInt(exRegion.getPcode());
                        if (pCode != 0) {
                            put("pcode", pCode);
                        }
                    }};
                })
                .collect(Collectors.toMap(v -> SafeConverter.toString(v.get("key")), v -> v));

        // 根据parentCode构建父子关系
        transformedRegions.values().forEach(v -> {
            if (v.containsKey("pcode")) {
                Map<String, Object> parentRegion = transformedRegions.get(SafeConverter.toString(v.get("pcode")));

                // 如果父节点存在，将此结点加入到父结点的子节点中
                if (parentRegion != null) {
                    ((List) (parentRegion.get("children"))).add(v);
                }
            }
        });

        return transformedRegions;
    }

    private List<Map<String, Object>> buildRegionTree(Collection<String> regionCodes) {
        Map<String, Map<String, Object>> allRegionTree = buildAllRegionTree();

        if (CollectionUtils.isNotEmpty(regionCodes)) {
            regionCodes.forEach(v -> {
                Map<String, Object> regionInfo = allRegionTree.get(v);
                if (regionInfo != null) {
                    regionInfo.put("selected", true);
                }
            });
        }

        return allRegionTree.values()
                .stream()
                .filter(v -> !v.containsKey("pcode"))
                .collect(Collectors.toList());
    }

    // 获取指定类型、模块、目标值的地区列表
    private List<EquatorBlackListInfo> fetchSpecEquatorBlackLists(String type, String module, String tagValue) {
        List<EquatorBlackListInfo> equatorBlackLists = equatorBlackListClient.getRemoteReference().loadAllBlackListsFromDb().getUninterruptibly();

        if (STUDENT_BLACK_TYPES.containsKey(type) || SCHOOL_BLACK_TYPES.containsKey(type)) {
            equatorBlackLists = equatorBlackLists.stream()
                    .filter(Objects::nonNull)
                    .filter(v -> StringUtils.equals(type, v.fetchBlackListType()))
                    .filter(v -> StringUtils.isBlank(module) || StringUtils.equals(module, v.fetchBlackListModuleType()))
                    .filter(v -> StringUtils.isBlank(tagValue) || StringUtils.equals(tagValue, v.fetchTagValue()))
                    .sorted(Comparator.comparing(EquatorBlackListInfo::getCreateTime).reversed())
                    .collect(Collectors.toList());
        } else {
            equatorBlackLists = equatorBlackLists.stream()
                    .filter(Objects::nonNull)
                    .filter(v -> StringUtils.equals(type, v.fetchBlackListType()))
                    .filter(v -> StringUtils.isBlank(module) || StringUtils.equals(module, v.fetchBlackListModuleType()))
                    .filter(v -> StringUtils.isBlank(tagValue) || StringUtils.equals(tagValue, v.fetchTagValue()))
                    .collect(Collectors.toList());
        }

        return equatorBlackLists;
    }
}

