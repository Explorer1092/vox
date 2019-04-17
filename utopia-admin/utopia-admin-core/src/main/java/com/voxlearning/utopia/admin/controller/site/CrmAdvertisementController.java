/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.CrmImageUploader;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementCoType;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementPositionType;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementStatus;
import com.voxlearning.utopia.service.config.api.constant.AdvertisementType;
import com.voxlearning.utopia.service.config.api.entity.Advertisement;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementPosition;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementRegion;
import com.voxlearning.utopia.service.config.api.entity.Advertiser;
import com.voxlearning.utopia.service.config.client.AdvertisementServiceClient;
import com.voxlearning.utopia.service.region.api.entities.Region;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 广告中心后台
 * Created by Shuai Huan on 2014/9/10.
 */
@Controller
@RequestMapping("/site/advertisement")
@Slf4j
public class CrmAdvertisementController extends AbstractAdminSystemController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AdvertisementServiceClient advertisementServiceClient;

    @Inject private CrmImageUploader crmImageUploader;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @RequestMapping(value = "advertiserindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String advertiserIndex(Model model) {
        String name = getRequestParameter("name", "");
        String startDate = getRequestParameter("startDate", "");
        String endDate = getRequestParameter("endDate", "");

        Date s = null;
        if (StringUtils.isNotBlank(startDate)) {
            s = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE);
        }
        Date e = null;
        if (StringUtils.isNotBlank(endDate)) {
            e = DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE);
        }
        if (e != null) {
            e = DateUtils.nextDay(e, 1);
        }

        List<Advertiser> advertiserList = queryAdvertisers(name, s, e);
        model.addAttribute("advertiserList", advertiserList);
        model.addAttribute("name", name);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "site/advertisement/advertiserindex";
    }

    @RequestMapping(value = "saveadvertiser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAdvertiser() {
        long id = SafeConverter.toLong(getRequestParameter("id", null));
        String name = getRequestParameter("name", null);
        String contactUser = getRequestParameter("contactUser", null);
        String contactPhone = getRequestParameter("contactPhone", null);
        String contactQQ = getRequestParameter("contactQQ", null);
        String personInCharge = getRequestParameter("personInCharge", null);

        if (id != 0 && crmConfigService.$loadAdvertiser(id) == null) {
            // make sure id exists
            return MapMessage.errorMessage();
        }

        Advertiser advertiser = new Advertiser();
        advertiser.setName(name);
        advertiser.setContactUser(contactUser);
        advertiser.setContactPhone(contactPhone);
        advertiser.setContactQQ(contactQQ);
        advertiser.setPersonInCharge(personInCharge);
        if (id != 0) {
            advertiser.setId(id);
        }

        advertiser = crmConfigService.$upsertAdvertiser(advertiser);
        if (advertiser == null) {
            return MapMessage.errorMessage("保存广告主信息失败!");
        }
        return MapMessage.successMessage("保存成功!");
    }

    @RequestMapping(value = "deladvertiser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delAdvertiser() {
        long id = SafeConverter.toLong(getRequestParameter("id", null), -1);
        if (id < 0) {
            return MapMessage.errorMessage("参数信息不全!");
        }

        long count = advertisementServiceClient.getAdvertisementService()
                .loadAllAdvertisementsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .filter(e -> Objects.equals(id, e.getAdvertiserId()))
                .count();
        if (count > 0) {
            return MapMessage.errorMessage("该广告主已经投放了广告,不能删除!");
        }

        boolean ret = crmConfigService.$removeAdvertiser(id);
        if (!ret) {
            return MapMessage.errorMessage("广告主不存在!");
        } else {
            return MapMessage.successMessage("删除成功!");
        }
    }

    @RequestMapping(value = "advertisementindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String advertisementIndex(Model model) {
        long id = SafeConverter.toLong(getRequestParameter("id", null));
        String advertiserName = getRequestParameter("advertiserName", "");
        String adName = getRequestParameter("adName", "");
        String adCooperationType = getRequestParameter("adCooperationType", "");
        String startDate = getRequestParameter("startDate", "");
        String endDate = getRequestParameter("endDate", "");

        List<Advertisement> advertisements = new ArrayList<>();
        if (id != 0) {
            Advertisement advertisement = loadEnabledAdvertisementFromDB(id);
            if (advertisement != null) {
                advertisements.add(advertisement);
            }
        } else {
            Date s = null;
            if (StringUtils.isNotBlank(startDate)) {
                s = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE);
            }
            Date e = null;
            if (StringUtils.isNotBlank(endDate)) {
                e = DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE);
            }
            if (e != null) {
                e = DateUtils.nextDay(e, 1);
            }
            List<Advertisement> list = advertisementLoaderClient.findAdvertisements(adName, adCooperationType, s, e);
            if (StringUtils.isBlank(advertiserName)) {
                advertisements.addAll(list);
            } else {
                List<Advertisement> L = new ArrayList<>();

                Advertiser advertiser = crmConfigService.$loadAdvertisers().stream()
                        .filter(a -> !SafeConverter.toBoolean(a.getDisabled()))
                        .filter(a -> StringUtils.equals(advertiserName, a.getName()))
                        .sorted((o1, o2) -> {
                            long c1 = SafeConverter.toLong(o1.getCreateDatetime());
                            long c2 = SafeConverter.toLong(o2.getCreateDatetime());
                            return Long.compare(c2, c1);
                        })
                        .findFirst()
                        .orElse(null);
                if (advertiser != null) {
                    L.addAll(advertisementServiceClient.getAdvertisementService()
                            .loadAllAdvertisementsFromDB()
                            .getUninterruptibly()
                            .stream()
                            .filter(a -> Objects.equals(a.getAdvertiserId(), advertiser.getId()))
                            .collect(Collectors.toList()));
                }
                Set<Long> ids = L.stream().map(Advertisement::getId)
                        .collect(Collectors.toSet());
                list = list.stream().filter(a -> ids.contains(a.getId())).collect(Collectors.toList());
                advertisements.addAll(list);
            }
        }

        List<Map<String, Object>> advertisementList = advertisements.stream()
                .map(a -> {
                    Advertiser advertiser = crmConfigService.$loadAdvertiser(a.getAdvertiserId());
                    if (advertiser != null && SafeConverter.toBoolean(advertiser.getDisabled())) {
                        advertiser = null;
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("name", a.getName());
                    map.put("description", a.getDescription());
                    map.put("advertiserName", advertiser == null ? null : advertiser.getName());
                    map.put("type", a.getType());
                    map.put("cooperationType", a.getCooperationType());
                    map.put("createDatetime", a.getCreateDatetime());
                    map.put("personCountLimit", a.getPersonCountLimit());
                    map.put("budget", a.getBudget());
                    map.put("status", AdvertisementStatus.parse(a.getStatus()).getName());
                    return map;
                })
                .collect(Collectors.toList());

        model.addAttribute("id", id == 0 ? "" : id);
        model.addAttribute("advertiserName", advertiserName);
        model.addAttribute("adName", adName);
        model.addAttribute("adCooperationType", adCooperationType);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("cooperationTypes", AdvertisementCoType.values());
        model.addAttribute("advertisementList", advertisementList);
        return "site/advertisement/advertisementindex";
    }

    @RequestMapping(value = "addadvertisement.vpage", method = RequestMethod.GET)
    public String toAddAdvertisementPage(Model model) {
        Advertisement advertisement;
        Long adId = getRequestLong("adId");
        if (adId == 0) {
            advertisement = new Advertisement();
            advertisement.setDisabled(false);
            advertisement.setId(0L);
        } else {
            advertisement = loadEnabledAdvertisementFromDB(adId);
            if (advertisement == null) {
                advertisement = new Advertisement();
                advertisement.setDisabled(false);
                advertisement.setId(0L);
            }
        }
        model.addAttribute("ad", advertisement);
        model.addAttribute("advertiserList", crmConfigService.$loadAdvertisers().stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .sorted((o1, o2) -> {
                    long c1 = SafeConverter.toLong(o1.getCreateDatetime());
                    long c2 = SafeConverter.toLong(o2.getCreateDatetime());
                    return Long.compare(c2, c1);
                })
                .collect(Collectors.toList()));
        model.addAttribute("cooperationTypes", AdvertisementCoType.values());
        model.addAttribute("adTypes", AdvertisementType.values());
        return "site/advertisement/addadvertisement";
    }

    @RequestMapping(value = "saveadvertisement.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAdvertisement() {
        long id = SafeConverter.toLong(getRequestParameter("id", null));
        Long advertiserId = getRequestLong("advertiserId");
        String resourceUrl = getRequestString("resourceUrl");
        String name = getRequestString("name");
        String description = getRequestString("description");
        String cooperationType = getRequestString("cooperationType");
        String type = getRequestString("type");
        String showTimeStart = getRequestString("showTimeStart");
        String showTimeEnd = getRequestString("showTimeEnd");
        Integer gradeStart = getRequestInt("gradeStart");
        Integer gradeEnd = getRequestInt("gradeEnd");
        String schoolIds = getRequestString("schoolIds");
        Integer personCountLimit = getRequestInt("personCountLimit");
        Double budget = getRequestDouble("budget", 0);
        Ktwelve ktwelve = Ktwelve.of(getRequestInt("ktwelve", 100));
        Subject subject = Subject.ofWithUnknown(getRequestString("subject"));
        Integer needAuth = getRequestInt("needAuth");
        String specialKey = getRequestParameter("specialKey", "");

        Advertiser advertiser = crmConfigService.$loadAdvertiser(advertiserId);
        if (advertiser != null && SafeConverter.toBoolean(advertiser.getDisabled())) {
            advertiser = null;
        }
        if (advertiser == null) {
            return MapMessage.errorMessage("无此广告主:" + advertiserId + "!");
        }
        MapMessage message;
        if (id == 0) {
            message = advertisementServiceClient.getAdvertisementService().createAdvertisement(advertiserId, resourceUrl, name,
                    description, type, cooperationType,
                    DateUtils.stringToDate(showTimeStart, DateUtils.FORMAT_SQL_DATE),
                    DateUtils.stringToDate(showTimeEnd, DateUtils.FORMAT_SQL_DATE),
                    gradeStart, gradeEnd, schoolIds, personCountLimit, budget, ktwelve, subject, needAuth, specialKey);
        } else {
            message = advertisementServiceClient.getAdvertisementService().updateAdvertisement(id, advertiserId, resourceUrl, name,
                    description, type, cooperationType,
                    DateUtils.stringToDate(showTimeStart, DateUtils.FORMAT_SQL_DATE),
                    DateUtils.stringToDate(showTimeEnd, DateUtils.FORMAT_SQL_DATE),
                    gradeStart, gradeEnd, schoolIds, personCountLimit, budget, ktwelve, subject, needAuth, specialKey);
        }
        message.setInfo(message.isSuccess() ? "保存成功！" : "保存广告信息失败!");
        return message;
    }

    @RequestMapping(value = "deladvertisement.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delAdvertisement() {
        long id = SafeConverter.toLong(getRequestParameter("id", null), -1);
        if (id < 0) {
            return MapMessage.errorMessage("参数信息不全!");
        }
        Advertisement advertisement = loadEnabledAdvertisementFromDB(id);
        if (advertisement != null && advertisement.getStatus().equals(AdvertisementStatus.ONLINE.getStatus())) {
            return MapMessage.errorMessage("不能删除上线状态的广告!");
        }
        crmConfigService.$disableAdvertisement(id);
        return MapMessage.successMessage("删除成功!");
    }

    @RequestMapping(value = "setonline.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setOnline() {
        long id = SafeConverter.toLong(getRequestParameter("id", null), -1);
        int status = SafeConverter.toInt(getRequestParameter("status", null), -1);
        if (id < 0 || status < 0) {
            return MapMessage.errorMessage();
        }
        if (advertisementServiceClient.getAdvertisementService()
                .loadAdvertisementFromDB(id)
                .getUninterruptibly() == null) {
            return MapMessage.errorMessage();
        }
        Advertisement a = new Advertisement();
        a.setId(id);
        a.setStatus(status);
        a = crmConfigService.$upsertAdvertisement(a);

        MapMessage message = new MapMessage().setSuccess(a != null);
        message.setInfo(message.isSuccess() ? "保存成功！" : "上线/下线操作失败!");
        return message;
    }

    @RequestMapping(value = "adregionindex.vpage", method = RequestMethod.GET)
    public String adRegionIndex(Model model) {
        Long id = getRequestLong("adId");
        List<Map<String, Object>> result = new ArrayList<>();
        List<AdvertisementRegion> advertisementRegions = crmConfigService.$loadAdvertisementRegions().stream()
                .filter(e -> Objects.equals(id, e.getAdvertisementId()))
                .collect(Collectors.toList());
        for (AdvertisementRegion region : advertisementRegions) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", region.getId());
            map.put("contactPhone", region.getContactPhone());
            map.put("name", region.getDistrictName());
            map.put("price", region.getPrice());
            map.put("province", region.getProvince());
            map.put("city", region.getCity());
            map.put("county", region.getCounty());
            result.add(map);
        }
        model.addAttribute("ad", loadEnabledAdvertisementFromDB(id));
        model.addAttribute("adRegionList", result);
        return "site/advertisement/adregionindex";
    }

    @RequestMapping(value = "addadregion.vpage", method = RequestMethod.GET)
    public String addAdRegion(Model model) {
        long id = getRequestLong("id");
        long adId = getRequestLong("adId");

        AdvertisementRegion region = null;
        if (id != 0) {
            region = crmConfigService.$loadAdvertisementRegion(id);
        }
        if (region == null) {
            // advertisement region not found, create a mock one
            region = new AdvertisementRegion();
            region.setDisabled(false);
            region.setId(0L);
        }
        model.addAttribute("id", id == 0 ? "" : id);
        model.addAttribute("ad", loadEnabledAdvertisementFromDB(adId));
        model.addAttribute("region", region);
        return "site/advertisement/addadregion";
    }

    @RequestMapping(value = "loadregion.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String loadRegion() {
        try {
            return buildAllRegionTree();
        } catch (Exception ex) {
            log.error("加载区域失败", ex);
            return "加载区域失败" + ex.getMessage();
        }
    }

    @RequestMapping(value = "saveadregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAdRegion() {
        Long adId = getRequestLong("adId");
        String name = getRequestString("name");
        Double price = getRequestDouble("price", 0);
        String contactPhone = getRequestString("contactPhone");
        String regionCodes = getRequestString("regionCode");
        List<Integer> regionCodeList = StringUtils.toIntegerList(regionCodes);

        for (Integer regionCode : regionCodeList) {
            AdvertisementRegion document = new AdvertisementRegion();
            document.setAdvertisementId(adId);
            document.setDistrictName(name);
            document.setPrice(price.floatValue());
            document.setContactPhone(contactPhone);
            document.setRegionCode(regionCode);
            crmConfigService.$upsertAdvertisementRegion(document);
        }
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "updateadregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateAdRegion() {
        long id = SafeConverter.toLong(getRequestParameter("id", null), -1);
        if (id < 0) {
            return MapMessage.errorMessage();
        }
        if (crmConfigService.$loadAdvertisementRegion(id) == null) {
            return MapMessage.errorMessage();
        }

        Long adId = getRequestLong("adId");
        String name = getRequestString("name");
        Double price = getRequestDouble("price", 0);
        String contactPhone = getRequestString("contactPhone");
        Integer regionCode = getRequestInt("regionCode");

        AdvertisementRegion document = new AdvertisementRegion();
        document.setId(id);
        document.setAdvertisementId(adId);
        document.setDistrictName(name);
        document.setPrice(price.floatValue());
        document.setContactPhone(contactPhone);
        document.setRegionCode(regionCode);
        document = crmConfigService.$upsertAdvertisementRegion(document);

        MapMessage message = new MapMessage().setSuccess(document != null);
        message.setInfo(message.isSuccess() ? "更新成功！" : "更新广告城市地区信息失败!");
        return message;
    }

    @RequestMapping(value = "deladregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delAdRegion() {
        long id = SafeConverter.toLong(getRequestParameter("id", null), -1);
        if (id < 0) {
            return MapMessage.errorMessage("参数信息不全!");
        }
        boolean ret = crmConfigService.$disableAdvertisementRegion(id);
        MapMessage message = new MapMessage().setSuccess(ret);
        message.setInfo(ret ? "删除成功!" : "删除广告城市地区信息失败!");
        return message;
    }


    @RequestMapping(value = "admaterialindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String adMaterialIndex(Model model) {
        Long id = getRequestLong("adId");
        List<AdvertisementPosition> advertisementPositions = crmConfigService.$loadAdvertisementPositions().stream()
                .filter(e -> Objects.equals(e.getAdvertisementId(), id))
                .sorted((o1, o2) -> {
                    int p1 = SafeConverter.toInt(o1.getPosition());
                    int p2 = SafeConverter.toInt(o2.getPosition());
                    return Integer.compare(p1, p2);
                })
                .collect(Collectors.toList());
        String prePath = RuntimeMode.isUsingProductionData() ? "http://www.17zuoye.com" : "http://www.test.17zuoye.net";
        model.addAttribute("ad", loadEnabledAdvertisementFromDB(id));
        model.addAttribute("positionTypes", AdvertisementPositionType.toKeyValuePairs());
        model.addAttribute("positionMap", AdvertisementPositionType.toMap());
        model.addAttribute("advertisementPositions", advertisementPositions);
        model.addAttribute("prePath", prePath + "/gridfs/");
        return "site/advertisement/admaterialindex";
    }

    @RequestMapping(value = "saveposition.vpage", method = RequestMethod.POST)
    @SneakyThrows(IOException.class)
    public String savePosition(MultipartFile file,
                               @RequestParam("adId") Long adId,
                               @RequestParam("position") Integer position) {
        if (file.isEmpty()) {
            return "redirect:admaterialindex.vpage?adId=" + adId;
        }
        Boolean isTop = getRequestBool("isTop");
        Boolean isDefault = getRequestBool("isDefault");
        String originalFileName = file.getOriginalFilename();
        //ad开头的图可能会被拦截，各往后串一个字母=>be
        String prefix = "be-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + adId;
        try (InputStream inStream = file.getInputStream()) {
            String filename = crmImageUploader.upload(prefix, originalFileName, inStream);

            AdvertisementPosition ap = crmConfigService.$loadAdvertisementPositions().stream()
                    .filter(e -> Objects.equals(e.getAdvertisementId(), adId))
                    .filter(e -> Objects.equals(e.getPosition(), position))
                    .findFirst()
                    .orElse(null);
            if (ap != null) {
                Long id = ap.getId();
                ap = new AdvertisementPosition();
                ap.setId(id);
            } else {
                ap = new AdvertisementPosition();
            }
            ap.setAdvertisementId(adId);
            ap.setImgUrl(filename);
            ap.setPosition(position);
            ap.setIsTop(isTop);
            ap.setIsDefault(isDefault);
            crmConfigService.$upsertAdvertisementPosition(ap);
        }
        return "redirect:admaterialindex.vpage?adId=" + adId;
    }

    @RequestMapping(value = "deladposition.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteAdPosition() {
        long id = SafeConverter.toLong(getRequestParameter("id", null), -1);
        if (id < 0) {
            return MapMessage.errorMessage();
        }
        boolean ret = crmConfigService.$disableAdvertisementPosition(id);
        return new MapMessage().setSuccess(ret);
    }

    @RequestMapping(value = "settop.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setAdTop(Long id, Boolean isTop) {
        AdvertisementPosition ap = crmConfigService.$loadAdvertisementPosition(id);
        if (ap == null) {
            return MapMessage.errorMessage();
        }

        ap = new AdvertisementPosition();
        ap.setId(id);
        ap.setIsTop(isTop);
        ap = crmConfigService.$upsertAdvertisementPosition(ap);

        return new MapMessage().setSuccess(ap != null);
    }

    @RequestMapping(value = "setdefault.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage setAdDefault(Long id, Boolean isDefault) {
        AdvertisementPosition ap = crmConfigService.$loadAdvertisementPosition(id);
        if (ap == null) {
            return MapMessage.errorMessage();
        }

        ap = new AdvertisementPosition();
        ap.setId(id);
        ap.setIsDefault(isDefault);
        ap = crmConfigService.$upsertAdvertisementPosition(ap);

        return new MapMessage().setSuccess(ap != null);
    }

    // FIXME: 这个方法有点重复，region tree 都是构建好的了
    private String buildAllRegionTree() {
        Map<Integer, ExRegion> originalRegions = raikouSystem.getRegionBuffer().loadAllRegions();
        List<Region> regions = new ArrayList<>(originalRegions.values());

        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Map<String, Object>> retMap = new HashMap<>();
        for (Region region : regions) {
            // 转换成要使用的HashMap对象
            Map<String, Object> regionItemMap = new HashMap<>();
            regionItemMap.put("title", region.getName());
            regionItemMap.put("key", String.valueOf(region.getCode()));
            regionItemMap.put("children", new ArrayList());

            if (region.getPcode() == 0) {
                result.add(regionItemMap);
            }
            retMap.put(String.valueOf(region.getCode()), regionItemMap);
        }

        // 第二次循环，根据Id和ParentID构建父子关系
        for (Region region : regions) {
            Integer pcode = region.getPcode();
            if (pcode == 0) {
                continue;
            }

            Map<String, Object> parentObj = retMap.get(String.valueOf(pcode));
            Map<String, Object> childObj = retMap.get(String.valueOf(region.getCode()));

            if (parentObj == null) {
                if (!result.contains(childObj)) {
                    result.add(childObj);
                }
            } else {
                List children = (List) parentObj.get("children");
                if (!children.contains(childObj)) {
                    children.add(childObj);
                }
            }
        }
        return JsonUtils.toJson(result);
    }

    private List<Advertiser> queryAdvertisers(String name, Date start, Date end) {
        List<Advertiser> list = crmConfigService.$loadAdvertisers().stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .sorted((o1, o2) -> {
                    long c1 = SafeConverter.toLong(o1.getCreateDatetime());
                    long c2 = SafeConverter.toLong(o2.getCreateDatetime());
                    return Long.compare(c2, c1);
                })
                .collect(Collectors.toList());
        if (name != null && name.trim().length() > 0) {
            list = list.stream()
                    .filter(a -> a.getName() != null && a.getName().contains(name))
                    .collect(Collectors.toList());
        }
        if (start != null) {
            list = list.stream()
                    .filter(a -> {
                        long c = a.getCreateDatetime() == null ? 0 : a.getCreateDatetime().getTime();
                        return c >= start.getTime();
                    })
                    .collect(Collectors.toList());
        }
        if (end != null) {
            list = list.stream()
                    .filter(a -> {
                        long c = a.getCreateDatetime() == null ? 0 : a.getCreateDatetime().getTime();
                        return c <= end.getTime();
                    })
                    .collect(Collectors.toList());
        }
        return list;
    }

    private Advertisement loadEnabledAdvertisementFromDB(Long id) {
        Advertisement advertisement = advertisementServiceClient.getAdvertisementService()
                .loadAdvertisementFromDB(id)
                .getUninterruptibly();
        if (advertisement == null || SafeConverter.toBoolean(advertisement.getDisabled())) {
            return null;
        }
        return advertisement;
    }
}
