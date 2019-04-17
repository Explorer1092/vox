package com.voxlearning.utopia.admin.controller.equator.resourcetablemanage;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.FileUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.equator.service.configuration.client.ResourceExcelTableServiceClient;
import com.voxlearning.equator.service.configuration.resourcetablemanage.annotation.ResourceDownloadType;
import com.voxlearning.equator.service.configuration.resourcetablemanage.annotation.StaticResourceFileType;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceStaticFileInfo;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceTableCategory;
import com.voxlearning.equator.service.configuration.resourcetablemanage.entity.ResourceTableDigest;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2018/6/28.
 * 增值equator-资源表管理中的静态资源(图片 json文件等)管理
 */
@Controller
@RequestMapping("/equator/config/resourcetablemanage/")
public class ResourceExcelTableStaticResourceManagerController extends AbstractEquatorController {
    @Inject
    private ResourceExcelTableServiceClient resourceExcelTableServiceClient;


    //静态资源管理-页面
    @RequestMapping(value = "staticresourceindex.vpage", method = RequestMethod.GET)
    public String staticResourceIndex(Model model) {
        String categoryCombination = getRequestString("categoryCombination");
        List<ResourceTableCategory> resourceTableCategoryList = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableCategoryListFromDb().getUninterruptibly();
        List<ResourceTableDigest> resourceTableDigestList = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableDigestListNotDisabledFromDb().getUninterruptibly();

        //一级分类和二级分类
        Map<String, Set<String>> categoryMap = new HashMap<>();
        for (ResourceTableCategory category : resourceTableCategoryList) {
            if (StringUtils.isNotBlank(category.getFirstCategory()) && CollectionUtils.isNotEmpty(category.getSecondCategory())) {
                categoryMap.putIfAbsent(category.getFirstCategory(), new HashSet<>());
                categoryMap.get(category.getFirstCategory()).addAll(category.getSecondCategory());
            }
        }
        for (ResourceTableDigest digest : resourceTableDigestList) {
            if (StringUtils.equals(digest.getResourceType(), ResourceDownloadType.CDN.name()) && StringUtils.isNotBlank(digest.getCategory()) && StringUtils.isNotBlank(digest.getStaticResourceChildCategory())) {
                categoryMap.putIfAbsent(digest.getCategory(), new HashSet<>());
                categoryMap.get(digest.getCategory()).add(digest.getStaticResourceChildCategory());
            }
        }
        List<Map<String, Object>> categoryList = new ArrayList<>();
        for (Map.Entry<String, Set<String>> stringListEntry : categoryMap.entrySet()) {
            String firstCategory = stringListEntry.getKey();
            for (String secondCategory : stringListEntry.getValue()) {
                categoryList.add(
                        MapUtils.m(
                                "firstCategory", firstCategory,
                                "secondCategory", secondCategory,
                                "value", firstCategory + "," + secondCategory
                        )
                );
            }
        }


        if (!StringUtils.isBlank(getRequestString("error"))) {
            model.addAttribute("error", getRequestString("error"));
        }
        if (!StringUtils.isBlank(getRequestString("successInfo"))) {
            model.addAttribute("successInfo", getRequestString("successInfo"));
        }

        model.addAttribute("categoryList", categoryList);
        if (StringUtils.isNotBlank(categoryCombination)) {
            model.addAttribute("currentCategoryCombination", categoryCombination);
        }

        return "equator/config/resourcetablemanage/staticresourceindex";
    }


    //批量上传静态资源图片等
    @RequestMapping(value = "uploadmultistaticresource.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String addMultipleFileResource(Model model) {
        String path = "redirect:/equator/config/resourcetablemanage/staticresourceindex.vpage";
        String categoryCombination = getRequestString("categoryCombination").trim();
        String fileTypeStr = getRequestString("fileType").trim();
        MultipartHttpServletRequest request = (MultipartHttpServletRequest) getRequest();
        List<MultipartFile> files = request.getFiles("files");


        //参数校验
        String[] category = StringUtils.split(categoryCombination, ",");
        if (category.length != 2) {
            model.addAttribute("error", "分类有误,分类组合是" + categoryCombination);
            return path;
        }
        StaticResourceFileType fileType = StaticResourceFileType.safeParse(fileTypeStr);
        if (fileType == null || fileType == StaticResourceFileType.UNKNOWN) {
            model.addAttribute("error", "文件类型有误");
            return path;
        }
        if (CollectionUtils.isEmpty(files)) {
            model.addAttribute("error", "上传的文件列表是空的");
            return path;
        }
        //校验分类
        String firstCategory = category[0];
        String secondCategory = category[1];
        ResourceTableCategory wrongCategory = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableCategoryListFromDb().getUninterruptibly()
                .stream()
                .filter(r -> StringUtils.equals(r.getFirstCategory(), firstCategory))
                .filter(r -> r.getSecondCategory() != null && r.getSecondCategory().contains(secondCategory))
                .findFirst()
                .orElse(null);
        if (wrongCategory == null) {
            model.addAttribute("error", "一级" + firstCategory + "二级分类" + secondCategory + ",不存在");
            return path;
        }
        path = path + "?categoryCombination=" + categoryCombination;

        //校验上传的文件列表
        files = files.stream().filter(f -> !f.isEmpty()).collect(Collectors.toList());
        if (files.size() == 0) {
            model.addAttribute("error", "上传的文件列表是空的");
            return path;
        }


        String lockKey = "ResourceExcelTableStaticResourceManagerController_addMultipleFileResource_" + firstCategory + "_" + secondCategory;
        try {
            AtomicLockManager.getInstance().acquireLock(lockKey);

            //上传文件到cdn
            List<String> failedFileList = new ArrayList<>();
            Map<String, String> fileNameUrlMap = new HashMap<>();
            for (MultipartFile file : files) {
                if (file.getSize() > 0) {
                    String originalFileName = file.getOriginalFilename();
                    String fileUrl = "";
                    try {
                        if (!file.isEmpty() && file.getSize() > 0) {
                            fileUrl = uploadFile(file);
                        }
                    } catch (Exception ignored) {
                        logger.error("ResourceExcelTableManageController upload img error");
                    }

                    if (StringUtils.isBlank(fileUrl) || StringUtils.isBlank(originalFileName)) {
                        failedFileList.add(originalFileName);
                    } else {
                        fileNameUrlMap.put(originalFileName, fileUrl);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(failedFileList)) {
                model.addAttribute("error", "全部上传失败，有问题的文件名是" + JsonUtils.toJson(failedFileList));
                return path;
            }


            //确保图片资源在全局的唯一
            List<ResourceStaticFileInfo> resourceStaticFileInfoList = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceStaticFileInfoListFromDb().getUninterruptibly();
            List<ResourceStaticFileInfo> checkResourceStaticFileInfoList = resourceStaticFileInfoList.stream()
                    .filter(r -> fileNameUrlMap.containsKey(r.getResourceName()))
                    .filter(r -> !StringUtils.equals(r.getFirstCategory(), firstCategory) || !StringUtils.equals(r.getSecondCategory(), secondCategory))
                    .filter(r -> !r.isDisabledTrue())
                    .collect(Collectors.toList());
            if (checkResourceStaticFileInfoList.size() > 0) {
                List<String> checkErrorList = new ArrayList<>();
                for (ResourceStaticFileInfo resource : checkResourceStaticFileInfoList) {
                    String errorInfo = resource.getFirstCategory() + "_" + resource.getSecondCategory() + "静态资源表中包含有静态资源" + resource.getResourceName();
                    checkErrorList.add(errorInfo);
                }
                model.addAttribute("error", "图片资源需要全局唯一，错误信息是" + JsonUtils.toJson(checkErrorList));
                return path;
            }


            //保存上传后的url到静态资源表
            Map<String, ResourceStaticFileInfo> resourceStaticFileInfoMap = resourceStaticFileInfoList.stream()
                    .filter(r -> StringUtils.equals(r.getFirstCategory(), firstCategory))
                    .filter(r -> StringUtils.equals(r.getSecondCategory(), secondCategory))
                    .filter(r -> !r.isDisabledTrue())
                    .collect(Collectors.toMap(ResourceStaticFileInfo::getResourceName, e -> e));
            for (Map.Entry<String, String> nameUrl : fileNameUrlMap.entrySet()) {
                String resourceName = nameUrl.getKey();
                String url = nameUrl.getValue();
                if (resourceStaticFileInfoMap.containsKey(resourceName)) {//更新
                    ResourceStaticFileInfo resourceStaticFileInfo = resourceStaticFileInfoMap.get(resourceName);
                    resourceStaticFileInfo.setFileType(fileType.name());
                    resourceStaticFileInfo.setUrl(url);
                    MapMessage mapMessage = resourceExcelTableServiceClient.getResourceExcelTableService().replaceResourceStaticFileInfo(resourceStaticFileInfo);
                    if (!mapMessage.isSuccess()) {
                        failedFileList.add(resourceName);
                    }
                } else {//插入新的
                    ResourceStaticFileInfo resourceStaticFileInfo = new ResourceStaticFileInfo();
                    resourceStaticFileInfo.setFirstCategory(firstCategory);
                    resourceStaticFileInfo.setSecondCategory(secondCategory);
                    resourceStaticFileInfo.setResourceName(resourceName);
                    resourceStaticFileInfo.setFileType(fileType.name());
                    resourceStaticFileInfo.setUrl(url);
                    MapMessage mapMessage = resourceExcelTableServiceClient.getResourceExcelTableService().insertResourceStaticFileInfo(resourceStaticFileInfo);
                    if (!mapMessage.isSuccess()) {
                        failedFileList.add(resourceName);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(failedFileList)) {
                model.addAttribute("error", "部分上传失败，有问题的文件名是" + JsonUtils.toJson(failedFileList));
            }
            model.addAttribute("successInfo", "上传成功" + fileNameUrlMap.size() + "个文件");

        } catch (CannotAcquireLockException e) {
            model.addAttribute("error", "一级分类" + firstCategory + ",二级分类" + secondCategory + "。其他人正在操作，请稍后重试");
            return path;
        } catch (Exception e) {
            model.addAttribute("error", "异常" + e.getMessage());
            return path;
        } finally {
            AtomicLockManager.getInstance().releaseLock(lockKey);
        }

        return path;
    }


    //打包静态资源
    @RequestMapping(value = "packstaticfileresource.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String packStaticFileResource(Model model) {
        String path = "redirect:/equator/config/resourcetablemanage/staticresourceindex.vpage";
        String categoryCombination = getRequestString("categoryCombinationForPack").trim();

        //参数校验
        if (StringUtils.isBlank(categoryCombination)) {
            model.addAttribute("error", "参数为空");
            return path;
        }
        String[] category = StringUtils.split(categoryCombination, ",");
        if (category.length != 2) {
            model.addAttribute("error", "分类有误,分类组合是" + categoryCombination);
            return path;
        }
        String firstCategory = category[0].trim();
        String secondCategory = category[1].trim();
        if (StringUtils.isAnyBlank(firstCategory, secondCategory)) {
            model.addAttribute("error", "分类有误,一级分类是" + firstCategory + ",二级分类是" + secondCategory);
            return path;
        }

        //获取firstCategory secondCategory对应的静态资源列表
        List<Map<String, Object>> staticResourceList = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceStaticFileInfoListFromDb().getUninterruptibly()
                .stream()
                .filter(s -> StringUtils.equals(firstCategory, s.getFirstCategory()))
                .filter(s -> StringUtils.equals(secondCategory, s.getSecondCategory()))
                .filter(s -> !s.isDisabledTrue())
                .map(r -> MapUtils.m("resourceName", r.getResourceName(), "fileType", r.getFileType(), "url", r.getUrl()))
                .collect(Collectors.toList());

        String lockKey = "ResourceExcelTableStaticResourceManagerController_packStaticFileResource_" + firstCategory + "_" + secondCategory;
        try {
            AtomicLockManager.getInstance().acquireLock(lockKey);

            //静态资源列表转成json文件，上传到cdn
            File file = null;
            String url;
            try {
                file = new File(System.currentTimeMillis() + ".json");
                FileUtils.writeStringToFile(file, JsonUtils.toJson(staticResourceList), "UTF-8", false);
                url = uploadFile(file);
            } catch (Exception e) {
                logger.error("pack static file resource failed", e);
                model.addAttribute("error", "一级分类是" + firstCategory + ",二级分类是" + secondCategory + "，打包失败");
                return path;
            } finally {
                if (file != null) {
                    file.delete();
                }
            }
            if (StringUtils.isBlank(url)) {
                model.addAttribute("error", "一级分类是" + firstCategory + ",二级分类是" + secondCategory + "，上传json文件失败");
                return path;
            }


            //把已上传的静态资源url打包到摘要信息表中
            MapMessage mapMessage = packUploadedStaticUrl(firstCategory, secondCategory, url);
            if (!mapMessage.isSuccess()) {
                model.addAttribute("error", mapMessage.getInfo());
                return path;
            }
        } catch (CannotAcquireLockException e) {
            model.addAttribute("error", "一级分类" + firstCategory + ",二级分类" + secondCategory + "。其他人正在操作，请稍后重试");
            return path;
        } catch (Exception e) {
            model.addAttribute("error", "异常:" + e.getMessage());
            return path;
        } finally {
            AtomicLockManager.getInstance().releaseLock(lockKey);
        }
        model.addAttribute("successInfo", firstCategory + secondCategory + "SR静态资源,打包成功");
        return path;
    }


    private MapMessage packUploadedStaticUrl(String firstCategory, String secondCategory, String url) {
        //把已上传的静态资源url打包到摘要信息表中
        if (StringUtils.isBlank(url)) {
            return MapMessage.errorMessage("一级分类是" + firstCategory + ",二级分类是" + secondCategory + ",打包的url是空");
        }
        ResourceTableDigest digest = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableDigestListNotDisabledFromDb().getUninterruptibly()
                .stream()
                .filter(r -> StringUtils.equals(r.getCategory(), firstCategory))
                .filter(r -> StringUtils.equals(r.getStaticResourceChildCategory(), secondCategory))
                .findFirst()
                .orElse(null);

        List<String> keys = new ArrayList<>(Arrays.asList("resourceName", "fileType", "url"));
        Map<String, String> fieldDesc = new HashMap<>();
        fieldDesc.put("resourceName", "资源名(唯一标识)");
        fieldDesc.put("fileType", "文件类型");
        fieldDesc.put("url", "文件地址");

        if (digest == null) {
            digest = new ResourceTableDigest();
            digest.setCategory(firstCategory);
            digest.setStaticResourceChildCategory(secondCategory);
            digest.setResourceType(ResourceDownloadType.CDN.name());

            //打包后的表唯一标识 excel名
            digest.setTableName(firstCategory + secondCategory + "SR");
            digest.setTableExcelName(firstCategory + "_" + secondCategory + "静态资源");

            digest.setVersion(1L);
            digest.setUrl(url);
            digest.setFieldDesc(fieldDesc);
            digest.setTableKeys(keys);
            return resourceExcelTableServiceClient.getResourceExcelTableService().insertResourceTableDigest(digest);
        } else {
            digest.setVersion(SafeConverter.toLong(digest.getVersion()) + 1);
            digest.setUrl(url);
            digest.setFieldDesc(fieldDesc);
            digest.setTableKeys(keys);
            return resourceExcelTableServiceClient.getResourceExcelTableService().replaceResourceTableDigest(digest);
        }

    }

    @RequestMapping(value = "staticresourcepicture.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String staticResourcePicture(Model model) {
        String firstCategory = "";
        String secondCategory = "";
        String categoryCombination = getRequestString("categoryCombination").trim();
        if (StringUtils.isNotBlank(categoryCombination)) {
            String[] category = StringUtils.split(categoryCombination, ",");
            if (category.length == 2) {
                firstCategory = category[0];
                secondCategory = category[1];
            }
        }


        //一级分类和二级分类
        List<ResourceTableCategory> resourceTableCategoryList = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableCategoryListFromDb().getUninterruptibly();
        List<ResourceTableDigest> resourceTableDigestList = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceTableDigestListNotDisabledFromDb().getUninterruptibly();
        Map<String, Set<String>> categoryMap = new HashMap<>();
        for (ResourceTableCategory category : resourceTableCategoryList) {
            if (StringUtils.isNotBlank(category.getFirstCategory()) && CollectionUtils.isNotEmpty(category.getSecondCategory())) {
                categoryMap.putIfAbsent(category.getFirstCategory(), new HashSet<>());
                categoryMap.get(category.getFirstCategory()).addAll(category.getSecondCategory());
            }
        }
        for (ResourceTableDigest digest : resourceTableDigestList) {
            if (StringUtils.equals(digest.getResourceType(), ResourceDownloadType.CDN.name()) && StringUtils.isNotBlank(digest.getCategory()) && StringUtils.isNotBlank(digest.getStaticResourceChildCategory())) {
                categoryMap.putIfAbsent(digest.getCategory(), new HashSet<>());
                categoryMap.get(digest.getCategory()).add(digest.getStaticResourceChildCategory());
            }
        }
        List<Map<String, Object>> categoryList = new ArrayList<>();
        for (Map.Entry<String, Set<String>> stringListEntry : categoryMap.entrySet()) {
            String tempFirstCategory = stringListEntry.getKey();
            for (String tempSecondCategory : stringListEntry.getValue()) {
                categoryList.add(
                        MapUtils.m("firstCategory", tempFirstCategory,
                                "secondCategory", tempSecondCategory,
                                "value", tempFirstCategory + "," + tempSecondCategory
                        )
                );
            }
        }

        if (StringUtils.isNotBlank(firstCategory) && StringUtils.isNotBlank(secondCategory)) {
            String finalFirstCategory = firstCategory;
            String finalSecondCategory = secondCategory;
            List<ResourceStaticFileInfo> resourceStaticFileInfoList = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceStaticFileInfoListFromDb().getUninterruptibly()
                    .stream().filter(r -> !r.isDisabledTrue())
                    .filter(r -> StringUtils.equals(r.getFirstCategory(), finalFirstCategory))
                    .filter(r -> StringUtils.equals(r.getSecondCategory(), finalSecondCategory))
                    .sorted(Comparator.comparing(info -> info.getResourceName().toLowerCase()))
                    .collect(Collectors.toList());

            model.addAttribute("currentCategory", MapUtils.m("firstCategory", firstCategory, "secondCategory", secondCategory,
                    "value", firstCategory + "," + secondCategory));
            model.addAttribute("resourceStaticFileInfoList", resourceStaticFileInfoList);
        }


        if (!StringUtils.isBlank(getRequestString("error"))) {
            model.addAttribute("error", getRequestString("error"));
        }
        if (!StringUtils.isBlank(getRequestString("successInfo"))) {
            model.addAttribute("successInfo", getRequestString("successInfo"));
        }
        model.addAttribute("categoryList", categoryList);


        return "equator/config/resourcetablemanage/staticresourcepicture";
    }


    @RequestMapping(value = "deleteonestaticfile.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteOneStaticFile() {
        String id = getRequestString("id").trim();
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("id不能为空");
        }
        ResourceStaticFileInfo resourceStaticFileInfo = resourceExcelTableServiceClient.getResourceExcelTableService().loadResourceStaticFileInfoByIdFromDb(id).getUninterruptibly();
        if (resourceStaticFileInfo == null) {
            return MapMessage.errorMessage("id对应的数据不存在");
        }

        resourceStaticFileInfo.setDisabled(true);
        return resourceExcelTableServiceClient.getResourceExcelTableService().replaceResourceStaticFileInfo(resourceStaticFileInfo);
    }
}
