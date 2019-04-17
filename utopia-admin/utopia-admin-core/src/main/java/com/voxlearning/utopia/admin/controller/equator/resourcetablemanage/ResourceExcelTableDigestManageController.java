package com.voxlearning.utopia.admin.controller.equator.resourcetablemanage;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.service.configuration.api.entity.blacklist.EquatorBlackListInfo;
import com.voxlearning.equator.service.configuration.api.entity.generalconfig.GeneralConfig;
import com.voxlearning.equator.service.configuration.api.entity.popup.GlobalPopup;
import com.voxlearning.equator.service.configuration.client.ResourceExcelTableServiceClient;
import com.voxlearning.equator.service.configuration.resourcetablemanage.annotation.ResourceDownloadType;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceStaticFileInfo;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceTableCategory;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceTableDigest;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2018/6/25.
 */
@Controller
@RequestMapping("/equator/config/resourcetablemanage/")
public class ResourceExcelTableDigestManageController extends AbstractEquatorController {
    @Inject
    private ResourceExcelTableServiceClient resourceExcelTableServiceClient;


    //各类资源表的摘要信息-页面展示
    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET})
    public String index(Model model) {
        String category = getRequestString("category");
        if (!StringUtils.isBlank(getRequestString("error"))) {
            model.addAttribute("error", getRequestString("error"));
        }

        //所有没有disable的摘要信息表
        List<ResourceTableDigest> resourceTableDigestList = resourceExcelTableServiceClient.getResourceExcelTableService()
                .loadResourceTableDigestListNotDisabledFromDb().getUninterruptibly();

        //一级分类汇总
        Set<String> firstCategorySet = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableCategoryListFromDb().getUninterruptibly()
                .stream().map(ResourceTableCategory::getFirstCategory).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        firstCategorySet.addAll(resourceTableDigestList.stream().map(ResourceTableDigest::getCategory).collect(Collectors.toSet()));
        List<String> categoryList = new ArrayList<>(firstCategorySet);

        //category分类下的摘要表
        if (StringUtils.isBlank(category) && categoryList.size() > 0) {
            category = categoryList.get(0);
        }
        if (StringUtils.isNotBlank(category)) {
            String finalCategory = category;
            resourceTableDigestList = resourceTableDigestList.stream().filter(s -> StringUtils.equals(s.getCategory(), finalCategory)).collect(Collectors.toList());
        }


        String dbType = ResourceDownloadType.DATABASE.name();
        String cdnType = ResourceDownloadType.CDN.name();
        List<ResourceTableDigest> dbResourceTableDigestList = resourceTableDigestList.stream().filter(r -> StringUtils.equals(r.getResourceType(), dbType)).sorted(Comparator.comparing(ResourceTableDigest::getTableName)).collect(Collectors.toList());
        List<ResourceTableDigest> cdnResourceTableDigestList = resourceTableDigestList.stream().filter(r -> StringUtils.equals(r.getResourceType(), cdnType)).sorted(Comparator.comparing(ResourceTableDigest::getTableName)).collect(Collectors.toList());
        resourceTableDigestList = new ArrayList<>();
        resourceTableDigestList.addAll(dbResourceTableDigestList);
        resourceTableDigestList.addAll(cdnResourceTableDigestList);

        model.addAttribute("category", category);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("resourceTableDigestList", resourceTableDigestList);

        return "equator/config/resourcetablemanage/index";
    }


    //新增资源表摘要信息-页面展示
    @RequestMapping(value = "upsertdigestindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String upsertDigestIndex(Model model) {
        String digestId = getRequestString("digestId");

        //所有没有disable的摘要信息
        List<ResourceTableDigest> resourceTableDigestList = resourceExcelTableServiceClient.getResourceExcelTableService()
                .loadResourceTableDigestListNotDisabledFromDb()
                .getUninterruptibly();
        ResourceTableDigest resourceTableDigest = null;
        if (StringUtils.isNotBlank(digestId)) {
            resourceTableDigest = resourceTableDigestList.stream().filter(r -> StringUtils.equals(digestId, r.getId())).findFirst().orElse(null);
        }

        //所有的分类
        Set<String> firstCategorySet = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableCategoryListFromDb().getUninterruptibly()
                .stream().map(ResourceTableCategory::getFirstCategory).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        firstCategorySet.addAll(resourceTableDigestList.stream().map(ResourceTableDigest::getCategory).collect(Collectors.toSet()));
        List<String> categoryList = new ArrayList<>(firstCategorySet);

        //DATABASE类型下可以使用的实体类型(过滤掉已经使用过的实体配置)
        List<String> filterConfigClass = new ArrayList<>();
        filterConfigClass.addAll(Arrays.asList(ResourceTableDigest.class.getTypeName(), ResourceStaticFileInfo.class.getTypeName(), ResourceTableCategory.class.getTypeName(), GeneralConfig.class.getTypeName(), GlobalPopup.class.getTypeName(), EquatorBlackListInfo.class.getTypeName()));
        filterConfigClass.addAll(resourceTableDigestList.stream().filter(r -> StringUtils.equals(r.getResourceType(), ResourceDownloadType.DATABASE.name())).map(ResourceTableDigest::getClassTypeName).collect(Collectors.toList()));
        if (resourceTableDigest != null && StringUtils.isNotBlank(resourceTableDigest.getClassTypeName())) {
            filterConfigClass.remove(resourceTableDigest.getClassTypeName());
        }
        Set<Class> classSet;
        try {
            classSet = resourceExcelTableServiceClient.fetchAllowedConfigClasses();
        } catch (Exception e) {
            model.addAttribute("error", "获取配置有误，请升级依赖的jar版本");
            return "/equator/config/resourcetablemanage/upsertdigestindex";
        }
        List<Map<String, Object>> configClassList = classSet.stream()
                .filter(aClass -> !filterConfigClass.contains(aClass.getTypeName()))
                .map(aClass -> MapUtils.m("classSimpleName", aClass.getSimpleName(), "classTypeName", aClass.getTypeName()))
                .collect(Collectors.toList());

        model.addAttribute("resourceTypeList", Arrays.asList(ResourceDownloadType.DATABASE.name(), ResourceDownloadType.CDN.name()));
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("configClassList", configClassList);


        if (resourceTableDigest != null) {
            model.addAttribute("resourceTableDigest", resourceTableDigest);
        }
        if (!StringUtils.isBlank(getRequestString("error"))) {
            model.addAttribute("error", getRequestString("error"));
        }

        return "/equator/config/resourcetablemanage/upsertdigestindex";
    }

    //upsert各类资源表的摘要信息
    @RequestMapping(value = "upsertresourcetabledigest.vpage", method = {RequestMethod.POST})
    public String upsertResourceTableDigest(Model model) {
        String path = "redirect:/equator/config/resourcetablemanage/index.vpage";

        String id = getRequestString("id").trim();
        String category = getRequestString("category").trim();
        String tableName = getRequestString("tableName").trim();
        String tableExcelName = getRequestString("tableExcelName").trim();
        String resourceType = getRequestString("resourceType").trim();
        String url = getRequestString("url").trim();
        String classTypeName = getRequestString("classTypeName").trim();
        String tableModifier = getRequestString("tableModifier").trim();
        String tableUploader = getRequestString("tableUploader").trim();

        //参数校验
        if (StringUtils.isAnyBlank(category, tableName, tableExcelName, resourceType, tableModifier, tableUploader)) {
            model.addAttribute("error", "增加摘要时,参数不能为空");
            return path;
        }
        if (!Arrays.asList(ResourceDownloadType.DATABASE.name(), ResourceDownloadType.CDN.name()).contains(resourceType)) {
            model.addAttribute("error", "增加摘要时,资源类型有误");
            return path;
        }
        //校验tableName
        Pattern p = Pattern.compile("[a-zA-Z0-9_]");
        Matcher matcher = p.matcher(tableName);
        String checkTableName = matcher.replaceAll("");
        if (StringUtils.isNotBlank(checkTableName)) {
            model.addAttribute("error", "资源表唯一标识包含特殊字符" + checkTableName);
            return path;
        }
        //校验category
        Set<String> firstCategorySet = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableCategoryListFromDb().getUninterruptibly()
                .stream().map(ResourceTableCategory::getFirstCategory).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        if (!firstCategorySet.contains(category)) {
            model.addAttribute("error", "增加摘要时,一级分类中没有分类" + category + ",需要先添加一级分类");
            return path;
        }
        //校验classTypeName
        Class configClass = null;
        if (StringUtils.equals(ResourceDownloadType.DATABASE.name(), resourceType)) {
            if (StringUtils.isBlank(resourceType)) {
                model.addAttribute("error", "增加摘要时,DATABASE资源类型时，实体类没有选择");
                return path;
            }
            try {
                configClass = resourceExcelTableServiceClient.fetchAllowedConfigClasses()
                        .stream()
                        .filter(aClass -> StringUtils.equals(aClass.getTypeName(), classTypeName))
                        .findFirst()
                        .orElse(null);
            } catch (Exception e) {
                model.addAttribute("error", "获取配置有误，请升级依赖的jar版本");
                return path + "?category=" + category;
            }
            if (configClass == null) {
                model.addAttribute("error", "增加摘要时,DATABASE资源类型时，实体类匹配不到");
                return path;
            }
            url = "";
        }


        //两条必须约束的条件  1:保证相同分类下tableName tableExcelName是唯一的, 2:DATABASE类型时保证不同分类下configClass只能匹配一个
        String errorInfo;
        if (StringUtils.isBlank(id)) {
            //插入新的摘要
            errorInfo = insertResourceTableDigest(category, tableName, tableExcelName, resourceType, configClass, tableModifier, tableUploader);
        } else {
            //更新已有的摘要
            errorInfo = replaceResourceTableDigest(id, category, tableName, tableExcelName, resourceType, url, configClass, tableModifier, tableUploader);
        }


        if (StringUtils.isNotBlank(errorInfo)) {
            model.addAttribute("error", errorInfo);
            return path;
        }
        return path + "?category=" + category;
    }

    @RequestMapping(value = "increasetableversion.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String increaseTableVersion(Model model) {
        String path = "redirect:/equator/config/resourcetablemanage/index.vpage";
        String digestId = getRequestString("digestId").trim();
        if (StringUtils.isBlank(digestId)) {
            model.addAttribute("error", "刷新版本时,digestId参数不能为空");
            return path;
        }

        ResourceTableDigest resourceTableDigest = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableDigestByIdFromDb(digestId).getUninterruptibly();
        if (resourceTableDigest == null) {
            model.addAttribute("error", "digestId对应的实体不存在");
            return path;
        }

        MapMessage mapMessage = resourceExcelTableServiceClient.getResourceExcelTableService().increaseTableVersion(digestId);
        if (!mapMessage.isSuccess()) {
            model.addAttribute("error", "刷新版本失败," + mapMessage.getInfo());
        }

        return path + "?category=" + resourceTableDigest.getCategory();
    }

    //插入新的摘要
    private String insertResourceTableDigest(String category, String tableName, String tableExcelName, String resourceType, Class configClass, String tableModifier, String tableUploader) {
        List<ResourceTableDigest> resourceTableDigestList = resourceExcelTableServiceClient.getResourceExcelTableService()
                .loadResourceTableDigestListNotDisabledFromDb()
                .getUninterruptibly();

        //保证相同分类下tableName tableExcelName是唯一的
        boolean isNameWrong = null != resourceTableDigestList.stream()
                .filter(s -> StringUtils.equals(category, s.getCategory()))
                .filter(s -> StringUtils.equals(s.getTableName(), tableName) || StringUtils.equals(s.getTableExcelName(), tableExcelName))
                .findFirst()
                .orElse(null);
        if (isNameWrong) {
            return "新增摘要时,同分类下,资源表名或excel名不能重复";
        }

        //DATABASE类型时保证不同分类下configClass只能匹配一个
        if (StringUtils.equals(ResourceDownloadType.DATABASE.name(), resourceType) && configClass == null) {
            return "增加摘要时,DATABASE资源类型时，实体类没有选择";
        }
        if (configClass != null) {
            ResourceTableDigest wrongDigest = resourceTableDigestList.stream()
                    .filter(s -> StringUtils.equals(s.getClassTypeName(), configClass.getTypeName()))
                    .findFirst()
                    .orElse(null);
            if (wrongDigest != null) {
                return "新增摘要时,分类" + wrongDigest.getCategory() + "下已经选择了" + configClass.getSimpleName() + "。因此分类" + category + "下不能重复选择";
            }
        }

        //插入一条新的摘要
        ResourceTableDigest digest = new ResourceTableDigest();
        digest.setCategory(category);
        digest.setResourceType(resourceType);
        digest.setTableName(tableName);
        digest.setTableExcelName(tableExcelName);
        digest.setUrl("");
        digest.setVersion(0L);
        digest.setTableModifier(tableModifier);
        digest.setTableUploader(tableUploader);
        if (StringUtils.equals(resourceType, ResourceDownloadType.DATABASE.name()) && configClass != null) {
            digest.setClassSimpleName(configClass.getSimpleName());
            digest.setClassTypeName(configClass.getTypeName());
        } else {
            digest.setClassSimpleName("");
            digest.setClassTypeName("");
        }
        MapMessage mapMessage = resourceExcelTableServiceClient.getResourceExcelTableService().insertResourceTableDigest(digest);
        if (!mapMessage.isSuccess()) {
            return "新增摘要失败，" + mapMessage.getInfo();
        }

        return "";
    }

    private String replaceResourceTableDigest(String id, String category, String tableName, String tableExcelName, String resourceType, String url, Class configClass, String tableModifier, String tableUploader) {
        List<ResourceTableDigest> resourceTableDigestList = resourceExcelTableServiceClient.getResourceExcelTableService()
                .loadResourceTableDigestListNotDisabledFromDb()
                .getUninterruptibly();

        ResourceTableDigest digest = resourceTableDigestList.stream().filter(s -> StringUtils.equals(s.getId(), id)).findFirst().orElse(null);
        if (digest == null) {
            return "更新摘要失败，id=" + id + "的数据不存在";
        }
        if (!StringUtils.equals(resourceType, digest.getResourceType())) {
            return "更新摘要时,资源表的类型不能变更";
        }


        //保证相同分类下tableName tableExcelName是唯一的
        if (!StringUtils.equals(digest.getTableName(), tableName)) {
            boolean isTableNameWrong = null != resourceTableDigestList.stream()
                    .filter(s -> StringUtils.equals(category, s.getCategory()))
                    .filter(s -> StringUtils.equals(s.getTableName(), tableName))
                    .findFirst()
                    .orElse(null);
            if (isTableNameWrong) {
                return "更新摘要时,同分类下,资源表名不能重复";
            }
        }
        if (!StringUtils.equals(digest.getTableExcelName(), tableExcelName)) {
            boolean isTableExcelNameWrong = null != resourceTableDigestList.stream()
                    .filter(s -> StringUtils.equals(category, s.getCategory()))
                    .filter(s -> StringUtils.equals(s.getTableExcelName(), tableExcelName))
                    .findFirst()
                    .orElse(null);
            if (isTableExcelNameWrong) {
                return "更新摘要时,同分类下,excel名不能重复";
            }
        }

        //DATABASE类型时保证不同分类下configClass只能匹配一个
        if (StringUtils.equals(resourceType, ResourceDownloadType.DATABASE.name()) && configClass != null && !StringUtils.equals(configClass.getTypeName(), digest.getClassTypeName())) {
            ResourceTableDigest wrongDigest = resourceTableDigestList.stream()
                    .filter(s -> StringUtils.equals(s.getClassTypeName(), configClass.getTypeName()))
                    .findFirst()
                    .orElse(null);
            if (wrongDigest != null) {
                return "更新摘要时,分类" + wrongDigest.getCategory() + "下已经选择了" + configClass.getSimpleName() + "。因此分类" + category + "下不能重复选择";
            }
        }

        digest.setCategory(category);
        digest.setTableName(tableName);
        digest.setTableExcelName(tableExcelName);
        digest.setUrl(url);
        digest.setTableModifier(tableModifier);
        digest.setTableUploader(tableUploader);
        if (StringUtils.equals(resourceType, ResourceDownloadType.DATABASE.name()) && configClass != null) {
            digest.setClassTypeName(configClass.getTypeName());
            digest.setClassSimpleName(configClass.getSimpleName());
        } else {
            digest.setClassTypeName("");
            digest.setClassSimpleName("");
        }
        MapMessage mapMessage = resourceExcelTableServiceClient.getResourceExcelTableService().replaceResourceTableDigest(digest);
        if (!mapMessage.isSuccess()) {
            return "更新摘要失败，" + mapMessage.getInfo();
        }

        return "";
    }


    @RequestMapping(value = "fetchresourcetabledigest.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchResourceTableDigest() {
        String digestId = getRequestString("digestId");
        if (StringUtils.isBlank(digestId)) {
            return MapMessage.errorMessage("数据不存在");
        }


        ResourceTableDigest resourceTableDigest = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableDigestByIdFromDb(digestId).getUninterruptibly();
        if (resourceTableDigest == null) {
            return MapMessage.errorMessage("数据不存在");
        }

        return MapMessage.successMessage().add("digest", resourceTableDigest);
    }


    @RequestMapping(value = "deleteresourcetabledigest.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteResourceTableDigest() {
        String digestId = getRequestString("digestId");
        if (StringUtils.isBlank(digestId)) {
            return MapMessage.errorMessage("参数digestId为空");
        }

        return resourceExcelTableServiceClient.getResourceExcelTableService().deleteResourceTableDigest(digestId);
    }
}
