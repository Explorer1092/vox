package com.voxlearning.utopia.admin.controller.opmanager.groupon;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.extension.sensitive.codec.SensitiveLib;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.galaxy.service.groupon.api.DPGrouponLoader;
import com.voxlearning.galaxy.service.groupon.api.DPGrouponService;
import com.voxlearning.galaxy.service.groupon.api.constant.GrouponGroupState;
import com.voxlearning.galaxy.service.groupon.api.constant.GrouponJoinState;
import com.voxlearning.galaxy.service.groupon.api.entity.*;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import com.voxlearning.utopia.admin.data.ExcelExportData;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.lang.util.MapMessage.errorMessage;
import static com.voxlearning.alps.lang.util.MapMessage.successMessage;

/**
 * @author xin.xin
 * @since 2019-01-19
 **/
@Controller
@RequestMapping(value = "/opmanager/groupon")
public class GrouponManagerController extends CrmAbstractController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = DPGrouponLoader.class)
    private DPGrouponLoader dpGrouponLoader;
    @ImportService(interfaceClass = DPGrouponService.class)
    private DPGrouponService dpGrouponService;

    @RequestMapping(value = "/index.vpage", method = RequestMethod.GET)
    public String index() {
        return "/opmanager/groupon/index";
    }

    @RequestMapping(value = "/search.vpage", method = RequestMethod.GET)
    public String search(Model model) {
        String grouponId = getRequestString("grouponId");
        List<Groupon> groupons = dpGrouponLoader.getAllGrouponForCRM();
        Groupon gp = groupons.stream().filter(groupon -> groupon.getId().equals(grouponId)).findFirst().orElse(null);

        model.addAttribute("grouponName", gp.getName());
        model.addAttribute("grouponId", grouponId);
        return "/opmanager/groupon/search";
    }

    @RequestMapping(value = "/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage grouponList() {
        List<Groupon> groupons = dpGrouponLoader.getAllGrouponForCRM();
        groupons = groupons.stream().sorted((g1, g2) -> g2.getCreateDatetime().compareTo(g1.getCreateDatetime())).collect(Collectors.toList());
        return successMessage().add("groupons", groupons);
    }

    @RequestMapping(value = "/add.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addGroupon() {
        String name = getRequestString("name");
        String startDateStr = getRequestString("start");
        String endDateStr = getRequestString("end");
        Integer count = getRequestInt("count");
        Integer maxCount = getRequestInt("maxCount");
        Integer expireDays = getRequestInt("expireDays");
        String levels = getRequestString("levels");
        Boolean multiProduct = getRequestBool("multiProduct");
        Boolean multiGroup = getRequestBool("multiGroup");
        Boolean creatorNew = getRequestBool("creatorNew");
        Boolean memberNew = getRequestBool("memberNew");
        Boolean singleShippingAddress = getRequestBool("singleShippingAddress");

        try {
            if (0 == expireDays) {
                expireDays = null;
            }
            if (0 == maxCount) {
                maxCount = count;
            }
            if (maxCount < count) {
                return errorMessage("最大人数不能小于成团人数");
            }

            Date startDate = DateUtils.stringToDate(startDateStr, "yyyy-MM-dd HH:mm:ss");
            if (null == startDate) {
                return errorMessage("开始时间错误");
            }
            Date endDate = DateUtils.stringToDate(endDateStr, "yyyy-MM-dd HH:mm:ss");
            if (null == endDate) {
                return errorMessage("结束时间错误");
            }

            if (startDate.after(endDate)) {
                return errorMessage("开始时间不能大于结束时间");
            }
            if (endDate.before(new Date())) {
                return errorMessage("结束时间不能小于当前时间");
            }

            List<Integer> clazzLevels = new ArrayList<>();
            if (StringUtils.isNotBlank(levels)) {
                String[] clzLevels = levels.split(",");
                for (String level : clzLevels) {
                    clazzLevels.add(SafeConverter.toInt(level));
                }
            }

            String id = dpGrouponService.addGroupon(name, startDate, endDate, count, maxCount, expireDays, clazzLevels, multiProduct, multiGroup, singleShippingAddress, creatorNew, memberNew);
            return MapMessage.successMessage().add("id", id);
        } catch (Exception ex) {
            logger.error("{},{},{},{},{},{},{},{}", name, startDateStr, endDateStr, count, expireDays, levels, multiGroup, multiProduct, ex);
            return errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/update.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateGroupon() {
        String id = getRequestString("id");
        String name = getRequestString("name");
        String startDateStr = getRequestString("start");
        String endDateStr = getRequestString("end");
        Integer count = getRequestInt("count");
        Integer maxCount = getRequestInt("maxCount");
        Integer expireDays = getRequestInt("expireDays");
        String levels = getRequestString("levels");
        Boolean multiProduct = getRequestBool("multiProduct");
        Boolean multiGroup = getRequestBool("multiGroup");
        Boolean creatorNew = getRequestBool("creatorNew");
        Boolean memberNew = getRequestBool("memberNew");
        Boolean singleShippingAddress = getRequestBool("singleShippingAddress");

        try {
            List<Groupon> groupons = dpGrouponLoader.getAllGrouponForCRM();
            Groupon gp = groupons.stream().filter(groupon -> groupon.getId().equals(id)).findFirst().orElse(null);
            if (null == gp) {
                return errorMessage("未查询到项目信息");
            }
            if (gp.getState().equals("ONLINE")) {
                return errorMessage("已上线状态的项目不允许修改信息");
            }

            if (0 == expireDays) {
                expireDays = null;
            }
            if (0 == maxCount) {
                maxCount = count;
            }
            if (maxCount < count) {
                return errorMessage("最大人数不能小于成团人数");
            }

            Date startDate = DateUtils.stringToDate(startDateStr, "yyyy-MM-dd HH:mm:ss");
            if (null == startDate) {
                return errorMessage("开始时间错误");
            }
            Date endDate = DateUtils.stringToDate(endDateStr, "yyyy-MM-dd HH:mm:ss");
            if (null == endDate) {
                return errorMessage("结束时间错误");
            }

            if (startDate.after(endDate)) {
                return errorMessage("开始时间不能大于结束时间");
            }
            if (endDate.before(new Date())) {
                return errorMessage("结束时间不能小于当前时间");
            }

            List<Integer> clazzLevels = null;
            if (StringUtils.isNotBlank(levels)) {
                clazzLevels = new ArrayList<>();
                String[] clzLevels = levels.split(",");
                for (String level : clzLevels) {
                    clazzLevels.add(SafeConverter.toInt(level));
                }
            }

            return dpGrouponService.updateGroupon(id, name, startDate, endDate, count, maxCount, expireDays, clazzLevels, multiProduct, multiGroup, singleShippingAddress, creatorNew, memberNew);
        } catch (Exception ex) {
            logger.error("{},{},{},{},{},{},{},{}", name, startDateStr, endDateStr, count, maxCount, expireDays, levels, multiGroup, multiProduct, ex);
            return errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/online.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage grouponOnline() {
        String grouponId = getRequestString("id");
        if (StringUtils.isBlank(grouponId)) {
            return errorMessage("参数错误");
        }

        try {
            return dpGrouponService.onlineGroupon(grouponId);
        } catch (Exception ex) {
            logger.error("{}", grouponId, ex);
            return errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/offline.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage offlineGroupon() {
        String grouponId = getRequestString("id");
        if (StringUtils.isBlank(grouponId)) {
            return errorMessage("参数错误");
        }

        try {
            return dpGrouponService.offlineGroupon(grouponId);
        } catch (Exception ex) {
            logger.error("{}", grouponId, ex);
            return errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/product.vpage", method = RequestMethod.GET)
    public String product(Model model) {
        String grouponId = getRequestString("grouponId");

        model.addAttribute("grouponId", grouponId);
        return "/opmanager/groupon/product";
    }

    @RequestMapping(value = "/product/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage productList() {
        String grouponId = getRequestString("grouponId");
        if (StringUtils.isBlank(grouponId)) {
            return errorMessage("参数错误");
        }

        try {
            List<GrouponProduct> products = dpGrouponLoader.getGrouponProductByGrouponId(grouponId);
            if (CollectionUtils.isEmpty(products)) {
                return successMessage();
            }

            return successMessage().add("products", products);
        } catch (Exception ex) {
            logger.error("{}", grouponId, ex);
            return errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/product/add.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage addProduct() {
        String grouponId = getRequestString("grouponId");
        String name = getRequestString("name");
        String identifyId = getRequestString("identifyId");
        Integer totalCount = getRequestInt("totalCount");
        String icon = getRequestString("icon");
        String cover = getRequestString("cover");
        String levels = getRequestString("clazzLevels");

        try {
            List<Integer> clazzLevels = new ArrayList<>();
            if (StringUtils.isNotBlank(levels)) {
                String[] clzLevels = levels.split(",");
                for (String level : clzLevels) {
                    clazzLevels.add(SafeConverter.toInt(level));
                }
            }

            dpGrouponService.addGrouponProduct(grouponId, identifyId, name, icon, cover, clazzLevels, totalCount);

            return successMessage();
        } catch (Exception ex) {
            logger.error("{},{},{},{},{}", name, identifyId, totalCount, icon, cover, ex);
            return errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/product/update.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage updateProduct() {
        String id = getRequestString("id");
        String name = getRequestString("name");
        String icon = getRequestString("icon");
        String cover = getRequestString("cover");
        String levels = getRequestString("levels");
        Integer totalCount = getRequestInt("totalCount");
        String identifyId = getRequestString("identifyId");

        try {
            List<Integer> clazzLevels = new ArrayList<>();
            if (StringUtils.isNotBlank(levels)) {
                String[] clzLevels = levels.split(",");
                for (String level : clzLevels) {
                    clazzLevels.add(SafeConverter.toInt(level));
                }
            }

            GrouponProduct grouponProduct = dpGrouponLoader.getGrouponProduct(id);
            Groupon gp = dpGrouponLoader.getAllGrouponForCRM().stream().filter(groupon -> groupon.getId().equals(grouponProduct.getGrouponId())).findFirst().orElse(null);
            if (null == gp) {
                return errorMessage("未查询到团购项目信息");
            }
            if (!gp.getState().equals("OFFLINE")) {
                return errorMessage("已上线状态的团购项目不能修改产品信息");
            }
            if (grouponProduct.getTotalCount() > totalCount) {
                return errorMessage("库存量只能增加");
            }

            MapMessage message = dpGrouponService.updateGrouponProduct(id, identifyId, name, icon, cover, clazzLevels);
            if (message.isSuccess()) {
                int delta = totalCount - grouponProduct.getTotalCount();
                message = dpGrouponService.updateGrouponProductCount(id, totalCount, grouponProduct.getCurrentCount() + delta);
            }

            return message;
        } catch (Exception ex) {
            logger.error("{},{},{},{},{}", id, name, icon, cover, levels, ex);
            return errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage grouponData() {
        String grouponId = getRequestString("grouponId");
        if (StringUtils.isBlank(grouponId)) {
            return errorMessage("参数错误");
        }

        try {
            Long groupCount = dpGrouponLoader.getGrouponGroupCount(grouponId);
            if (null == groupCount) {
                return errorMessage("未查询到拼团数量");
            }
            Long successGroupCount = dpGrouponLoader.getGrouponGroupSuccessCount(grouponId);
            if (null == successGroupCount) {
                return errorMessage("未查询到拼团成功数量");
            }
            Long totalMemberCount = dpGrouponLoader.getTotalMemberCount(grouponId);
            if (null == totalMemberCount) {
                return errorMessage("未查询到总参与人数");
            }

            return successMessage()
                    .add("joinedCount", totalMemberCount)
                    .add("groupCount", groupCount)
                    .add("successGroupCount", successGroupCount);
        } catch (Exception ex) {
            logger.error("{}", grouponId, ex);
            return errorMessage("系统异常");
        }
    }

    @RequestMapping(value = "/query.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage search() {
        Long userId = getRequestLong("uid");
        String grouponId = getRequestString("grouponId");
        if (0 == userId || StringUtils.isBlank(grouponId)) {
            return errorMessage("参数错误");
        }

        try {
            List<GrouponGroup> grouponGroups = dpGrouponLoader.loadGrouponGroup(userId, grouponId);
            if (CollectionUtils.isEmpty(grouponGroups)) {
                return successMessage();
            }

            List<Groupon> groupons = dpGrouponLoader.getAllGrouponForCRM();
            if (CollectionUtils.isEmpty(groupons)) {
                return successMessage();
            }

            Map<String, Groupon> grouponMap = groupons.stream().collect(Collectors.toMap(Groupon::getId, Function.identity()));

            List<Map<String, Object>> infos = new ArrayList<>();
            for (GrouponGroup group : grouponGroups) {
                List<UserGrouponRef> refs = dpGrouponLoader.getUserGrouponRef(group.getId());

                Map<String, Object> info = new HashMap<>();
                info.put("id", group.getId());
                info.put("state", group.getState().name());
                if (grouponMap.containsKey(group.getGrouponId())) {
                    Groupon groupon = grouponMap.get(group.getGrouponId());
                    info.put("name", groupon.getName());
                    info.put("startDateStr", DateUtils.dateToString(groupon.getStartDate(), "yyyyMMdd HH:mm:ss"));
                    info.put("totalCount", group.getTotalCount());
                    info.put("maxTotalCount", group.getMaxTotalCount());
                    info.put("currentCount", group.getCurrentCount());
                    infos.add(info);
                }
            }

            return successMessage().add("datas", infos);
        } catch (Exception ex) {
            logger.error("{},{}", userId, grouponId, ex);
            return errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/group/member.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage groupMember() {
        String groupId = getRequestString("groupId");
        if (StringUtils.isBlank(groupId)) {
            return errorMessage("参数错误");
        }

        try {
            List<UserGrouponRef> refs = dpGrouponLoader.getUserGrouponRef(groupId);
            if (CollectionUtils.isEmpty(refs)) {
                return successMessage();
            }

            List<GrouponProduct> products = dpGrouponLoader.getGrouponProductByGrouponId(refs.get(0).getGrouponId());
            if (CollectionUtils.isEmpty(products)) {
                return successMessage();
            }
            Map<String, GrouponProduct> productMap = products.stream().collect(Collectors.toMap(GrouponProduct::getId, Function.identity()));

            List<Map<String, Object>> infos = new ArrayList<>();
            for (UserGrouponRef ref : refs) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", ref.getId());
                info.put("userId", ref.getUserId());
                info.put("state", ref.getState().name());
                info.put("updateDateStr", DateUtils.dateToString(ref.getUpdateDatetime(), "yyyyMMdd HH:mm:ss"));
                info.put("owner", ref.getOwner());
                info.put("callName", ref.getCallName());

                if (productMap.containsKey(ref.getGrouponProductId())) {
                    GrouponProduct product = productMap.get(ref.getGrouponProductId());
                    info.put("productName", product.getName());
                    info.put("productId", product.getId());
                }

                infos.add(info);
            }

            return successMessage().add("members", infos);
        } catch (Exception ex) {
            logger.error("{}", groupId, ex);
            return errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/address/search.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchAddress() {
        Long userId = getRequestLong("uid");
        String grouponGroupId = getRequestString("groupId");
        if (StringUtils.isBlank(grouponGroupId) || 0 == userId) {
            return errorMessage("参数错误");
        }

        try {
            MapMessage message = successMessage();

            GrouponAddress grouponAddress = dpGrouponLoader.getUserGrouponAddress(grouponGroupId, userId);
            if (null != grouponAddress) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", grouponAddress.getId());
                info.put("name", grouponAddress.getName());
                info.put("addr", grouponAddress.getAddress());

                String grouponMobile = dpGrouponLoader.getGrouponAddressMobileObscured(userId, grouponAddress.getId(), getCurrentAdminUser().getAdminUserName(), "CRM查询用户拼团快递手机号");
                if (StringUtils.isNotBlank(grouponMobile)) {
                    info.put("mobile", grouponMobile);
                }

                ExRegion exRegion = raikouSystem.loadRegion(SafeConverter.toInt(grouponAddress.getDistrict()));
                if (null != exRegion) {
                    info.put("province", exRegion.getProvinceName());
                    info.put("city", exRegion.getCityName());
                    info.put("district", exRegion.getCountyName());
                }
                message.add("grouponAddress", info);
            }

            List<Address> addresses = dpGrouponLoader.getUserAddress(userId);
            if (CollectionUtils.isNotEmpty(addresses)) {
                List<Map<String, Object>> infos = new ArrayList<>();
                for (Address address : addresses) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", address.getId());
                    info.put("name", address.getName());
                    info.put("addr", address.getAddress());

                    String addressMobile = dpGrouponLoader.getUserAddressMobileObscured(userId, address.getId(), getCurrentAdminUser().getAdminUserName(), "CRM查询用户团购地址库手机号");
                    if (StringUtils.isNotBlank(addressMobile)) {
                        info.put("mobile", addressMobile);
                    }

                    ExRegion exRegion = raikouSystem.loadRegion(SafeConverter.toInt(address.getDistrict()));
                    if (null != exRegion) {
                        info.put("province", exRegion.getProvinceName());
                        info.put("city", exRegion.getCityName());
                        info.put("district", exRegion.getCountyName());
                    }
                    infos.add(info);
                }
                message.add("address", infos);
            }

            return message;
        } catch (Exception ex) {
            logger.error("{},{}", userId, grouponGroupId, ex);
            return errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/shipping/export.vpage", method = RequestMethod.POST)
    public void shippingExport() {
        String grouponId = getRequestString("grouponId");
        String startDateStr = getRequestString("startDate");
        String endDateStr = getRequestString("endDate");
        if (StringUtils.isBlank(grouponId) || StringUtils.isBlank(startDateStr) || StringUtils.isBlank(endDateStr)) {
            return;
        }

        String lock = "GALAXY_GROUPON_EXPORT";
        try {
            AtomicLockManager.instance().acquireLock(lock);
        } catch (CannotAcquireLockException ignore) {
            return;
        }

        try {
            Date startDate = DayRange.newInstance(DateUtils.stringToDate(startDateStr, "yyyy-MM-dd").getTime()).getStartDate();
            Date endDate = DayRange.newInstance(DateUtils.stringToDate(endDateStr, "yyyy-MM-dd").getTime()).getEndDate();

            List<Groupon> groupons = dpGrouponLoader.getAllGrouponForCRM();
            if (CollectionUtils.isEmpty(groupons)) {
                return;
            }
            Groupon groupon = groupons.stream().filter(g -> g.getId().equals(grouponId)).findFirst().orElse(null);
            if (null == groupon) {
                return;
            }
            List<GrouponProduct> products = dpGrouponLoader.getGrouponProductByGrouponId(grouponId);
            if (CollectionUtils.isEmpty(products)) {
                return;
            }
            Map<String, GrouponProduct> productMap = products.stream().collect(Collectors.toMap(GrouponProduct::getId, Function.identity()));

            Long startUserId = 0L;
            int pageCount = 1000;

            List<List<String>> excelData = new ArrayList<>();
            List<GrouponShippingInfo> infos = dpGrouponLoader.getGrouponShippingInfos(grouponId, startDate, endDate, startUserId, pageCount);
            while (CollectionUtils.isNotEmpty(infos)) {
                for (GrouponShippingInfo info : infos) {
                    GrouponGroup grouponGroup = dpGrouponLoader.loadGrouponGroup(info.getGroupId());
                    if (grouponGroup.getState() != GrouponGroupState.SUCCESS) {
                        continue;
                    }

                    StringBuilder productName = new StringBuilder();
                    if (groupon.getMultiProduct()) {
                        //允许同一个团拼多个产品
                        List<UserGrouponRef> refs = dpGrouponLoader.getUserGrouponRef(info.getGroupId());
                        refs = refs.stream().filter(ref -> ref.getState() == GrouponJoinState.SUCCESS).collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(refs)) {
                            continue;
                        }

                        if (groupon.getSingleShippingAddress()) {
                            for (UserGrouponRef ref : refs) {
                                GrouponProduct product = productMap.get(ref.getGrouponProductId());
                                productName.append(product.getName())
                                        .append("(")
                                        .append(product.getClazzLevels())
                                        .append(")")
                                        .append("\r\n");
                            }
                        } else {
                            UserGrouponRef userGrouponRef = refs.stream().filter(ref -> ref.getUserId().equals(info.getUserId())).findFirst().orElse(null);
                            if (null == userGrouponRef) {
                                continue;
                            }
                            GrouponProduct product = productMap.get(userGrouponRef.getGrouponProductId());
                            productName.append(product.getName())
                                    .append("(")
                                    .append(product.getClazzLevels())
                                    .append(")");
                        }
                    } else {
                        GrouponProduct product = productMap.get(grouponGroup.getGrouponProductId());
                        productName.append(product.getName())
                                .append("(")
                                .append(product.getClazzLevels())
                                .append(")");
                    }

                    List<String> data = new LinkedList<>();
                    data.add(info.getId());
                    data.add(info.getUserId().toString());

                    String mobile = SensitiveLib.decodeMobile(info.getMobile());
                    LogCollector.info("user-phone-loaded", MiscUtils.map(
                            "env", RuntimeMode.getCurrentStage(),
                            "userId", info.getUserId(),
                            "phone", StringUtils.mobileObscure(mobile),
                            "reason", "CRM导出团购快递地址",
                            "operator", getCurrentAdminUser().getAdminUserName(),
                            "time", System.currentTimeMillis()
                    ));
                    data.add(mobile);
                    data.add(info.getName());
                    data.add(productName.toString());

                    ExRegion province = raikouSystem.loadRegion(Integer.valueOf(info.getProvince()));
                    data.add(null == province ? "" : province.getName());

                    ExRegion city = raikouSystem.loadRegion(Integer.valueOf(info.getCity()));
                    data.add(null == city ? "" : city.getName());

                    ExRegion district = raikouSystem.loadRegion(Integer.valueOf(info.getDistrict()));
                    data.add(null == district ? "" : district.getName());

                    data.add(info.getAddress());

                    CacheObject<Object> cacheObject = CacheSystem.CBS.getCache("persistence").get("GALAXY_GROUPON_CAL_CLS_COUNT_" + grouponGroup.getId());
                    if (null == cacheObject || null == cacheObject.getValue()) {
                        data.add("");
                    } else {
                        data.add(String.valueOf(SafeConverter.toInt(cacheObject.getValue())));
                    }

                    excelData.add(data);
                }
                GrouponShippingInfo info = infos.stream().min((i1, i2) -> i2.getUserId().compareTo(i1.getUserId())).orElse(null);
                if (null == info) {
                    break;
                }
                startUserId = info.getUserId();

                infos = dpGrouponLoader.getGrouponShippingInfos(grouponId, startDate, endDate, startUserId, pageCount);
            }

            List<ExcelExportData> exportData = new LinkedList<>();
            String[] titles = new String[]{"id", "发起人", "手机号", "收货人", "产品名称", "省份", "城市", "地区", "详细地址", "份数"};
            int[] widths = new int[]{4000, 4000, 4000, 4000, 4000, 4000, 4000, 4000, 8000, 4000};
            exportData.add(new ExcelExportData("地址导出", titles, widths, excelData, titles.length));

            // 开始导出咯
            HSSFWorkbook resultFile = createXlsExcelExportData(exportData);
            @Cleanup ByteArrayOutputStream out = new ByteArrayOutputStream();
            resultFile.write(out);
            out.flush();
            HttpRequestContextUtils.currentRequestContext().downloadFile("groupon_shippingInfo.xls", "application/vnd.ms-excel", out.toByteArray());
        } catch (Exception ex) {
            logger.error("{},{},{}", grouponId, startDateStr, endDateStr, ex);
            return;
        } finally {
            AtomicLockManager.instance().releaseLock(lock);
        }
    }
}
