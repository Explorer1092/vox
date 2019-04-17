package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.galaxy.service.mall.api.DPCommodityLoader;
import com.voxlearning.galaxy.service.mall.api.DPCommodityService;
import com.voxlearning.galaxy.service.mall.api.constant.CommodityCategory;
import com.voxlearning.galaxy.service.mall.api.constant.CommodityColumn;
import com.voxlearning.galaxy.service.mall.api.constant.CommodityUserType;
import com.voxlearning.galaxy.service.mall.api.data.CommodityDetail;
import com.voxlearning.galaxy.service.mall.api.entity.Commodity;
import com.voxlearning.galaxy.service.mall.api.entity.CommodityLog;
import com.voxlearning.galaxy.service.mall.api.entity.CommoditySub;
import com.voxlearning.galaxy.service.mall.api.support.CommodityConvert;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.CrmImageUploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author malong
 * @since 2018/06/12
 */
@Controller
@RequestMapping(value = "opmanager/commodity")
@Slf4j
public class CommodityManager extends AbstractAdminSystemController {
    @ImportService(interfaceClass = DPCommodityService.class)
    private DPCommodityService dpCommodityService;
    @ImportService(interfaceClass = DPCommodityLoader.class)
    private DPCommodityLoader dpCommodityLoader;
    @StorageClientLocation(storage = "17-pmc")
    private StorageClient storageClient;
    @Inject
    private CrmImageUploader crmImageUploader;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String getList(Model model) {
        int page = getRequestInt("page", 1);
        if (page <= 0) {
            page = 1;
        }
        Pageable pageable = new PageRequest(page - 1, 10);
        List<Commodity> commodities = dpCommodityLoader.loadCommoditiesFromDB().getUninterruptibly();
        //被删除的商品数量
        long deleteCount = commodities.stream().filter(commodity -> SafeConverter.toBoolean(commodity.getDisabled())).count();

        //查询符合条件的商品
        commodities = filterCommodity(commodities, model).stream()
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .filter(commodity -> !SafeConverter.toBoolean(commodity.getDisabled()))
                .collect(Collectors.toList());

        List<Integer> ids = commodities.stream().map(Commodity::getId).collect(Collectors.toList());
        Map<Integer, CommoditySub> commoditySubMap = dpCommodityLoader.loadAllCommoditySub(ids);
        List<CommodityDetail> details = new ArrayList<>();
        commodities.forEach(commodity -> {
            //分类
            commodity.setCategory(getCategoryDesc(commodity.getCategory()));
            commodity.setColumn(getColumnDesc(commodity.getColumn()));
            commodity.setUserTypes(getUserTypesDesc(commodity.getUserTypes()));
            CommoditySub commoditySub = commoditySubMap.get(commodity.getId());
            CommodityDetail detail = CommodityConvert.toDetail(commodity, commoditySub);
            if (detail != null) {
                details.add(detail);
            }
        });

        Map<String, String> userTypeMap = new HashMap<>();
        for (CommodityUserType userType : CommodityUserType.values()) {
            userTypeMap.put(userType.name(), userType.getDesc());
        }

        Page<CommodityDetail> commodityPage = PageableUtils.listToPage(details, pageable);
        Map<String, String> categoryMap = new LinkedHashMap<>();
        for (CommodityCategory category : CommodityCategory.values()) {
            categoryMap.put(category.name(), category.getDesc());
        }
        model.addAttribute("deleteCount", deleteCount);
        model.addAttribute("commodityPage", commodityPage);
        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("userTypeMap", userTypeMap);
        model.addAttribute("columnMap", getColumnMap());
        model.addAttribute("currentPage", commodityPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", commodityPage.getTotalPages());
        model.addAttribute("hasPrev", commodityPage.hasPrevious());
        model.addAttribute("hasNext", commodityPage.hasNext());
        return "opmanager/commodity/list";
    }

    @RequestMapping(value = "recycleList.vpage", method = RequestMethod.GET)
    public String recycleList(Model model) {
        int page = getRequestInt("page", 1);
        if (page <= 0) {
            page = 1;
        }
        String categoryName = getRequestString("category");
        String commodityName = getRequestString("name");
        Pageable pageable = new PageRequest(page - 1, 10);
        List<Commodity> commodities = dpCommodityLoader.loadCommoditiesFromDB().getUninterruptibly();
        //只显示九十天之内删除的
        Date limitDate = DateUtils.addDays(new Date(), -90);
        commodities = commodities.stream()
                .filter(commodity -> SafeConverter.toBoolean(commodity.getDisabled()))
                .filter(commodity -> commodity.getUpdateTime().after(limitDate))
                .filter(commodity -> StringUtils.isBlank(categoryName) || categoryName.equals(commodity.getCategory()))
                .filter(commodity -> StringUtils.isBlank(commodityName) || commodityName.equals(commodity.getName()))
                .collect(Collectors.toList());
        List<Map<String, Object>> commodityList = new ArrayList<>();
        commodities.forEach(commodity -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", commodity.getId());
            map.put("name", commodity.getName());
            map.put("category", getCategoryDesc(commodity.getCategory()));
            long deleteDay = DateUtils.dayDiff(new Date(), commodity.getUpdateTime());
            map.put("leftDay", 90 - deleteDay);
            commodityList.add(map);
        });
        Page<Map<String, Object>> commodityPage = PageableUtils.listToPage(commodityList, pageable);

        Map<String, String> categoryMap = new LinkedHashMap<>();
        for (CommodityCategory category : CommodityCategory.values()) {
            categoryMap.put(category.name(), category.getDesc());
        }
        model.addAttribute("commodityPage", commodityPage);
        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("currentPage", commodityPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", commodityPage.getTotalPages());
        model.addAttribute("hasPrev", commodityPage.hasPrevious());
        model.addAttribute("hasNext", commodityPage.hasNext());
        return "opmanager/commodity/recycleBin";
    }


    @RequestMapping(value = "commodityLog.vpage", method = RequestMethod.GET)
    public String commodityLog(Model model) {
        int page = getRequestInt("page", 1);
        if (page <= 0) {
            page = 1;
        }
        Integer commodityId = getRequestInt("id");
        Pageable pageable = new PageRequest(page - 1, 10);
        List<CommodityLog> commodityLogs = dpCommodityLoader.loadCommodityLogs(commodityId);
        List<Map<String, Object>> list = new ArrayList<>();
        commodityLogs.stream().sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .forEach(log -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("opList", log.getOpList());
                    map.put("date", DateUtils.dateToString(log.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
                    map.put("operator", log.getOperator());
                    list.add(map);
                });
        Page<Map<String, Object>> logPage = PageableUtils.listToPage(list, pageable);
        model.addAttribute("logPage", logPage);
        model.addAttribute("currentPage", logPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", logPage.getTotalPages());
        model.addAttribute("hasPrev", logPage.hasPrevious());
        model.addAttribute("hasNext", logPage.hasNext());
        model.addAttribute("commodityId", commodityId);
        return "opmanager/commodity/log";
    }

    @RequestMapping(value = "addCommodity.vpage", method = RequestMethod.GET)
    public String addCommodity(Model model) {
        Map<String, String> categoryMap = new LinkedHashMap<>();
        for (CommodityCategory category : CommodityCategory.values()) {
            categoryMap.put(category.name(), category.getDesc());
        }

        String author = getCurrentAdminUser().getAdminUserName();
        Map<String, String> userTypeMap = new LinkedHashMap<>();
        for (CommodityUserType userType : CommodityUserType.values()) {
            userTypeMap.put(userType.name(), userType.getDesc());
        }
        model.addAttribute("userTypeMap", userTypeMap);
        model.addAttribute("author", author);
        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("columnMap", getColumnMap());
        return "opmanager/commodity/add";
    }

    @RequestMapping(value = "editCommodity.vpage", method = RequestMethod.GET)
    public String editCommodity(Model model) {
        Integer id = getRequestInt("id");
        Commodity commodity = dpCommodityLoader.loadCommodityFromDB(id).getUninterruptibly();
        CommoditySub commoditySub = dpCommodityLoader.loadCommoditySub(id);
        if (commodity == null || commoditySub == null) {
            return "opmanager/commodity/list";
        }
        Map<String, String> categoryMap = new HashMap<>();
        for (CommodityCategory category : CommodityCategory.values()) {
            categoryMap.put(category.name(), category.getDesc());
        }
        List<String> images = commodity.getImages();
        Map<String, String> imageMap = new HashMap<>();
        Map<String, String> imageNameMap = new HashMap<>();
        int i = 1;
        for (String image : images) {
            imageMap.put("img" + i, getOssImgUrl(image));
            imageNameMap.put("img" + i, image);
            i++;
        }
        String userType = "";
        if (commodity.getUserTypes().size() > 1) {
            userType = "all";
        } else if (commodity.getUserTypes().size() == 1) {
            userType = commodity.getUserTypes().get(0);
        }

        Map<String, String> userTypeMap = new LinkedHashMap<>();
        for (CommodityUserType type : CommodityUserType.values()) {
            userTypeMap.put(type.name(), type.getDesc());
        }
        model.addAttribute("userTypeMap", userTypeMap);
        model.addAttribute("columnMap", getColumnMap());
        model.addAttribute("userType", userType);
        model.addAttribute("imageMap", imageMap);
        model.addAttribute("imageNameMap", imageNameMap);
        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("commodity", CommodityConvert.toDetail(commodity, commoditySub));
        if (commodity.getRecommendStartDate() != null) {
            model.addAttribute("startDate", DateUtils.dateToString(commodity.getRecommendStartDate(), "yyyy-MM-dd HH:mm"));
        }
        if (commodity.getRecommendEndDate() != null) {
            model.addAttribute("endDate", DateUtils.dateToString(commodity.getRecommendEndDate(), "yyyy-MM-dd HH:mm"));
        }
        if (StringUtils.isNotBlank(commodity.getRecommendImage())) {
            Map<String, String> recommendImgMap = new HashMap<>();
            recommendImgMap.put("imageName", commodity.getRecommendImage());
            recommendImgMap.put("imageUrl", getOssImgUrl(commodity.getRecommendImage()));
            model.addAttribute("recommendImgMap", recommendImgMap);
        }

        return "opmanager/commodity/edit";
    }

    @RequestMapping(value = "addCommodity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addCommodity() {
        MapMessage message = validateCommodity();
        if (!message.isSuccess()) {
            return message;
        }
        CommodityDetail detail = (CommodityDetail) message.get("commodityDetail");
        try {
            return dpCommodityService.upsertCommodity(detail, getCurrentAdminUser().getAdminUserName());
        } catch (Exception ex) {
            return MapMessage.errorMessage("保存商品失败");
        }
    }

    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    public String detail(Model model) {
        Integer id = getRequestInt("id");
        Commodity commodity = dpCommodityLoader.loadCommodityFromDB(id).getUninterruptibly();
        CommoditySub commoditySub = dpCommodityLoader.loadCommoditySub(id);
        String category = "";
        String userType = "";
        String column = "";
        if (commodity != null && commoditySub != null) {
            category = getCategoryDesc(commodity.getCategory());
            column = getColumnDesc(commodity.getColumn());
            if (commodity.getUserTypes().size() > 1) {
                userType = "all";
            } else if (commodity.getUserTypes().size() == 1) {
                userType = commodity.getUserTypes().get(0);
            }
        }
        Map<String, String> userTypeMap = new HashMap<>();
        for (CommodityUserType type : CommodityUserType.values()) {
            userTypeMap.put(type.name(), type.getDesc());
        }
        model.addAttribute("userTypeMap", userTypeMap);
        model.addAttribute("column", column);
        CommodityDetail detail = CommodityConvert.toDetail(commodity, commoditySub);

        List<String> images = new ArrayList<>();
        if (detail != null && CollectionUtils.isNotEmpty(detail.getImages())) {
            for (String image : detail.getImages()) {
                images.add(getOssImgUrl(image));
            }
            detail.setImages(images);
            if (detail.getRecommendStartDate() != null) {
                model.addAttribute("startDate", DateUtils.dateToString(commodity.getRecommendStartDate(), "yyyy-MM-dd HH:mm"));
            }
            if (detail.getRecommendEndDate() != null) {
                model.addAttribute("endDate", DateUtils.dateToString(commodity.getRecommendEndDate(), "yyyy-MM-dd HH:mm"));
            }
            if (StringUtils.isNotBlank(detail.getRecommendImage())) {
                detail.setRecommendImage(getOssImgUrl(detail.getRecommendImage()));
            }
        }

        model.addAttribute("commodity", detail);
        model.addAttribute("category", category);
        model.addAttribute("userType", userType);
        return "opmanager/commodity/detail";
    }

    @RequestMapping(value = "onSale.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage onSale() {
        Integer id = getRequestInt("id");
        try {
            dpCommodityService.onSaleCommodity(Collections.singletonList(id), Boolean.TRUE, getCurrentAdminUser().getAdminUserName());
        } catch (Exception ex) {
            return MapMessage.errorMessage("上架失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "offSale.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage offSale() {
        Integer id = getRequestInt("id");
        try {
            dpCommodityService.onSaleCommodity(Collections.singletonList(id), Boolean.FALSE, getCurrentAdminUser().getAdminUserName());
        } catch (Exception ex) {
            return MapMessage.errorMessage("下架失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "batchOnSale.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchOnSale() {
        String idStr = getRequestString("commodityIds");
        String[] idArr = idStr.split(",");
        List<Integer> ids = new ArrayList<>();
        for (String id : idArr) {
            ids.add(SafeConverter.toInt(id));
        }
        try {
            dpCommodityService.onSaleCommodity(ids, Boolean.TRUE, getCurrentAdminUser().getAdminUserName());
        } catch (Exception ex) {
            return MapMessage.errorMessage("批量上架商品失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "batchOffSale.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchOffSale() {
        String idStr = getRequestString("commodityIds");
        String[] idArr = idStr.split(",");
        List<Integer> ids = new ArrayList<>();
        for (String id : idArr) {
            ids.add(SafeConverter.toInt(id));
        }
        try {
            dpCommodityService.onSaleCommodity(ids, Boolean.FALSE, getCurrentAdminUser().getAdminUserName());
        } catch (Exception ex) {
            return MapMessage.errorMessage("批量下架商品失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "batchDelete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchDelete() {
        String idStr = getRequestString("commodityIds");
        String[] idArr = idStr.split(",");
        List<Integer> ids = new ArrayList<>();
        for (String id : idArr) {
            ids.add(SafeConverter.toInt(id));
        }
        try {
            dpCommodityService.deleteCommodity(ids, getCurrentAdminUser().getAdminUserName());
        } catch (Exception ex) {
            return MapMessage.errorMessage("批量删除商品失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "batchRecover.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage batchRecover() {
        String idStr = getRequestString("commodityIds");
        String[] idArr = idStr.split(",");
        List<Integer> ids = new ArrayList<>();
        for (String id : idArr) {
            ids.add(SafeConverter.toInt(id));
        }
        try {
            dpCommodityService.recoverCommodity(ids, getCurrentAdminUser().getAdminUserName());
        } catch (Exception ex) {
            return MapMessage.errorMessage("批量恢复商品失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "recover.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recover() {
        Integer id = getRequestInt("id");
        try {
            dpCommodityService.recoverCommodity(Collections.singletonList(id), getCurrentAdminUser().getAdminUserName());
        } catch (Exception ex) {
            return MapMessage.errorMessage("恢复商品失败");
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "uploadImg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadImg(MultipartFile file) {
        if (file == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        try {
            return upload(file);
        } catch (Exception ex) {
            return MapMessage.errorMessage("上传图片失败");
        }

    }

    /**
     * ueditor controller
     */
    @RequestMapping(value = "ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorController() {
        String action = getRequestString("action");
        switch (action) {
            case "config":
                return MapMessage.successMessage()
                        .add("imageActionName", "uploadimage")
                        .add("imageFieldName", "upfile")
                        .add("imageInsertAlign", "none")
                        .add("imageMaxSize", 2048000)
                        .add("imageUrlPrefix", "");
            case "uploadimage":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");
                if (imgFile.isEmpty()) {
                    return MapMessage.errorMessage("没有文件上传");
                }
                String originalFileName = imgFile.getOriginalFilename();
                try {
                    MapMessage mapMessage = upload(imgFile);
                    if (!mapMessage.isSuccess()) {
                        return mapMessage;
                    }
                    return MapMessage.successMessage()
                            .add("url", mapMessage.get("url"))
                            .add("title", mapMessage.get("fileName"))
                            .add("state", "SUCCESS")
                            .add("original", originalFileName);
                } catch (Exception ex) {
                    logger.error("上传图片异常： " + ex.getMessage(), ex);
                    return MapMessage.errorMessage("上传图片异常： " + ex.getMessage());
                }
            default:
                return MapMessage.successMessage();
        }
    }

    private MapMessage upload(MultipartFile inputFile) throws IOException {
        if (inputFile == null) {
            return MapMessage.errorMessage("没有可上传的文件");
        }
        String suffix = StringUtils.substringAfterLast(inputFile.getOriginalFilename(), ".");
        if (StringUtils.isBlank(suffix)) {
            suffix = "jpg";
        }
        StorageMetadata storageMetadata = new StorageMetadata();
        storageMetadata.setContentLength(inputFile.getSize());
        String env = "commodity/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "commodity/test/";
        }
        String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
        String fileName = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
        String realName = storageClient.upload(inputFile.getInputStream(), fileName, path, storageMetadata);
        String fileUrl = StringUtils.defaultString(ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host")) + realName;
        return MapMessage.successMessage().add("fileName", realName).add("url", fileUrl);
    }

    private List<Commodity> filterCommodity(List<Commodity> commodities, Model model) {
        Integer id = getRequestInt("id");
        String name = getRequestString("name");
        String categoryName = getRequestString("category");
        String column = getRequestString("column");
        String onSale = getRequestString("onSale");
        String recommendFlag = getRequestString("recommendFlag");
        String userType = getRequestString("userType");
        Stream<Commodity> stream = commodities.stream();
        if (id > 0) {
            stream = stream.filter(commodity -> id.equals(commodity.getId()));
            model.addAttribute("id", id);
        }
        if (StringUtils.isNotBlank(name)) {
            stream = stream.filter(commodity -> commodity.getName().contains(name));
            model.addAttribute("name", name);
        }
        if (StringUtils.isNotBlank(categoryName)) {
            stream = stream.filter(commodity -> categoryName.equals(commodity.getCategory()));
            model.addAttribute("categoryName", categoryName);
        }
        if (StringUtils.isNotBlank(column)) {
            stream = stream.filter(commodity -> column.equals(commodity.getColumn()));
            model.addAttribute("column", column);
        }
        if (StringUtils.isNotBlank(onSale)) {
            stream = stream.filter(commodity -> onSale.equals(commodity.getOnSale() ? "yes" : "no"));
            model.addAttribute("onSale", onSale);
        }
        if (StringUtils.isNotBlank(recommendFlag)) {
            stream = stream.filter(commodity -> recommendFlag.equals(SafeConverter.toBoolean(commodity.getRecommendFlag()) ? "yes" : "no"));
            model.addAttribute("recommendFlag", recommendFlag);
        }
        if (StringUtils.isNotBlank(userType)) {
            stream = stream.filter(commodity -> CollectionUtils.isNotEmpty(commodity.getUserTypes()))
                    .filter(commodity -> ("all".equals(userType) && commodity.getUserTypes().size() > 1) || (commodity.getUserTypes().size() == 1 && commodity.getUserTypes().contains(userType)));
            model.addAttribute("userType", userType);
        }
        return stream.collect(Collectors.toList());
    }

    private MapMessage validateCommodity() {
        Integer id = getRequestInt("id", -1);
        String name = getRequestString("name");
        String category = getRequestString("category");
        String column = getRequestString("column");
        String imgStr = getRequestString("imgStr");
        String status = getRequestString("status");
        Integer stock = getRequestInt("stock");
        Double purchase = getRequestDouble("purchase", 0D);
        Double dispatchPrice = getRequestDouble("dispatchPrice", 0D);
        String userType = getRequestString("userType");
        Integer monitorCoin = getRequestInt("monitorCoin");
        Integer monitorCoinS = getRequestInt("monitorCoinS");
        Integer ordinaryCoin = getRequestInt("ordinaryCoin");
        Integer ordinaryCoinS = getRequestInt("ordinaryCoinS");
        String allowRepeat = getRequestString("allowRepeat");
        Integer order = getRequestInt("order");
        String description = getRequestString("description");

        if (StringUtils.isBlank(name)) {
            return MapMessage.errorMessage("商品名称不能为空");
        }
        if (StringUtils.isBlank(imgStr)) {
            return MapMessage.errorMessage("商品图片不能为空");
        }
        if (stock < 0) {
            return MapMessage.errorMessage("库存量必须大于等于0");
        }

        if (CommodityColumn.MONITOR.name().equals(column) && !"MONITOR".equals(userType)) {
            return MapMessage.errorMessage("展示栏目为KOL，用户专享也必须是KOL");
        }
        if (CommodityColumn.EXCHANGE_GIFT.name().equals(column) && "MONITOR".equals(userType)) {
            return MapMessage.errorMessage("展示栏目为兑换好礼，用户专享不能是KOL");
        }
        Commodity commodity = new Commodity();
        Boolean recommendFlag = getRequestBool("recommendFlag");
        commodity.setRecommendFlag(recommendFlag);
        if (recommendFlag) {
            String startDateStr = getRequestString("startDate");
            Date startDate = null;
            if (StringUtils.isNotBlank(startDateStr)) {
                startDate = DateUtils.stringToDate(startDateStr, "yyyy-MM-dd HH:mm");
            }
            if (startDate == null) {
                return MapMessage.errorMessage("推荐开始时间不能为空");
            }
            String endDateStr = getRequestString("endDate");
            Date endDate = null;
            if (StringUtils.isNotBlank(endDateStr)) {
                endDate = DateUtils.stringToDate(endDateStr, "yyyy-MM-dd HH:mm");
            }
            if (endDate == null) {
                return MapMessage.errorMessage("推荐结束时间不能为空");
            }
            if (startDate.after(endDate)) {
                return MapMessage.errorMessage("推荐开始时间必须小于推荐结束时间");
            }
            String recommendImg = getRequestString("recommendImg");
            if (StringUtils.isBlank(recommendImg)) {
                return MapMessage.errorMessage("推荐图片不能为空");
            }
            Integer recommendOrder = getRequestInt("recommendOrder");
            String weekDays = getRequestString("weekDays");
            if (StringUtils.isBlank(weekDays)) {
                return MapMessage.errorMessage("推荐星期不能为空");
            }
            String[] weekDayArr = weekDays.split(",");
            List<Integer> weekDayList = new ArrayList<>();
            for (String weekDay : weekDayArr) {
                weekDayList.add(SafeConverter.toInt(weekDay));
            }
            commodity.setRecommendStartDate(startDate);
            commodity.setRecommendEndDate(endDate);
            commodity.setRecommendImage(recommendImg);
            commodity.setRecommendOrder(recommendOrder);
            commodity.setWeekDayList(weekDayList);
        }

        String createUser = "";
        if (id < 0) {
            List<Commodity> commodities = dpCommodityLoader.loadCommoditiesFromDB().getUninterruptibly();
            if (commodities.stream().anyMatch(e -> name.equals(e.getName()))) {
                return MapMessage.errorMessage("商品已经存在");
            }
            Commodity latestCommodity = commodities.stream()
                    .sorted((o1, o2) -> o2.getId().compareTo(o1.getId()))
                    .findFirst()
                    .orElse(null);
            if (latestCommodity == null) {
                id = 1;
            } else {
                id = latestCommodity.getId() + 1;
            }
            createUser = getCurrentAdminUser().getAdminUserName();
        }

        commodity.setId(id);
        commodity.setName(name);
        commodity.setCategory(category);
        commodity.setColumn(column);
        String[] imgArr = imgStr.split(",");
        List<String> images = Arrays.asList(imgArr);
        commodity.setImages(images);
        commodity.setOnSale("yes".equals(status) ? Boolean.TRUE : Boolean.FALSE);
        commodity.setPurchase(purchase);
        commodity.setDispatchPrice(dispatchPrice);
        List<String> userTypes = new ArrayList<>();
        if ("all".equals(userType)) {
            for (CommodityUserType commodityUserType : CommodityUserType.values()) {
                userTypes.add(commodityUserType.name());
            }
        } else {
            userTypes.add(userType);
        }
        commodity.setUserTypes(userTypes);
        commodity.setMonitorCoin(monitorCoin);
        commodity.setMonitorCoinS(monitorCoinS);
        commodity.setOrdinaryCoin(ordinaryCoin);
        commodity.setOrdinaryCoinS(ordinaryCoinS);
        commodity.setAllowRepeat("yes".equals(allowRepeat) ? Boolean.TRUE : Boolean.FALSE);
        commodity.setOrder(order);
        commodity.setDescription(description);
        if (StringUtils.isNotBlank(createUser)) {
            commodity.setAuthor(createUser);
        }
        commodity.setDisabled(Boolean.FALSE);

        CommoditySub commoditySub = new CommoditySub();
        commoditySub.setStock(stock);

        CommodityDetail detail = CommodityConvert.toDetail(commodity, commoditySub);
        return MapMessage.successMessage().add("commodityDetail", detail);
    }

    private String getCategoryDesc(String categoryName) {
        CommodityCategory category = CommodityCategory.parse(categoryName);
        return category != null ? category.getDesc() : "";
    }

    private String getColumnDesc(String columnName) {
        CommodityColumn column = CommodityColumn.parse(columnName);
        return column != null ? column.getDesc() : "";
    }

    private List<String> getUserTypesDesc(List<String> userTypes) {
        List<String> userTypesDesc = new ArrayList<>();
        for (String userType : userTypes) {
            CommodityUserType commodityUserType = CommodityUserType.parse(userType);
            if (commodityUserType != null) {
                userTypesDesc.add(commodityUserType.getDesc());
            }
        }
        return userTypesDesc;
    }

    private String getOssImgUrl(String relativeUrl) {
        if (StringUtils.isBlank(relativeUrl)) {
            return "";
        }
        return ConfigManager.instance().getCommonConfig().getConfigs().get("oss_pmc_host") + relativeUrl;
    }

    private Map<String, String> getColumnMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (CommodityColumn column : CommodityColumn.values()) {
            if (column == CommodityColumn.RECOMMEND) {
                continue;
            }
            map.put(column.name(), column.getDesc());
        }
        return map;
    }

}
