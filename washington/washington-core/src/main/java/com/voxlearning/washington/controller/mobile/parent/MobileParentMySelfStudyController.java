package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyData;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 我的自学
 *
 * @author jiangpeng
 * @since 2016-10-21 下午3:39
 **/
@Controller
@RequestMapping(value = "/parentMobile/myselfstudy")
@Slf4j
public class MobileParentMySelfStudyController extends AbstractMobileParentController {

    @Inject private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;

    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;

    @RequestMapping(value = "/data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage mySelfStudyDataList() {
        User user = currentParent();
        if (user == null) {
            return noLoginResult;
        }
        String version = getRequestString("app_version");
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return MapMessage.successMessage().add("my_self_study_list", new ArrayList<>()).add("user_count", 12322332);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return noLoginResult;
        List<MySelfStudyData> mySelfStudyDatas = mySelfStudyService.loadMySelfStudyDateBySId(studentId);
        if (mySelfStudyDatas == null)
            mySelfStudyDatas = new ArrayList<>();

        Map<SelfStudyType, MySelfStudyData> mySelfStudyDataMap = mySelfStudyDatas.stream().collect(Collectors.toMap(MySelfStudyData::getSelfStudyType, Function.identity()));

        Map<OrderProductServiceType, List<UserActivatedProduct>> map = userOrderLoaderClient.loadUserActivatedProductList(studentId)
                .stream().collect(Collectors.groupingBy(o -> OrderProductServiceType.safeParse(o.getProductServiceType())));

        Map<SelfStudyType, UserActivatedProduct> selfStudyTypeAfentiActivationHistoryMap = new HashMap<>();
        for (Map.Entry<OrderProductServiceType, List<UserActivatedProduct>> entry : map.entrySet()) {
            SelfStudyType selfStudyType = SelfStudyType.fromOrderType(entry.getKey());
            if (selfStudyType == null)
                continue;
            List<UserActivatedProduct> list = entry.getValue();

            UserActivatedProduct product = list.stream()
                    .sorted((o1, o2) -> Long.compare(o2.getCreateDatetime().getTime(), o1.getCreateDatetime().getTime()))
                    .findFirst()
                    .orElse(null);
            if (product != null)
                selfStudyTypeAfentiActivationHistoryMap.put(selfStudyType, product);
        }
        List<Map<String, Object>> resultMapList = new ArrayList<>();

        List<MySelfStudyData> finalUpperDataList = new ArrayList<>();
        SelfStudyType.upperList.forEach(st -> {
            MySelfStudyData mySelfStudyData = mySelfStudyDataMap.get(st);
            UserActivatedProduct activatedProduct = selfStudyTypeAfentiActivationHistoryMap.get(st);
            if (mySelfStudyData == null && activatedProduct == null) { //没用过也没买过
                return;
            } else if (mySelfStudyData != null && activatedProduct == null) { //试用中

            } else if (mySelfStudyData == null) {  //购买过,还没数据
                mySelfStudyData = MySelfStudyData.newInstance(st, studentId);
                mySelfStudyData.setExpireDate(activatedProduct.getServiceEndTime());
            } else { // 都不为空 ,购买过,有数据
                mySelfStudyData.setExpireDate(activatedProduct.getServiceEndTime());
            }
            finalUpperDataList.add(mySelfStudyData);
        });
        List<MySelfStudyData> upperDataList = finalUpperDataList.stream().sorted((o1, o2) -> {
            Date date1 = o1.getLastUseDate();
            Date date2 = o2.getLastUseDate();
            if (date1 == null && date2 == null) {
                return 0;
            } else if (date1 == null) {
                return 1;
            } else if (date2 == null) {
                return -1;
            } else {
                return date2.compareTo(date1);
            }
        }).collect(Collectors.toList());


        add2MapList(upperDataList, resultMapList, true, user, studentDetail);

        List<MySelfStudyData> finalLowerDataList = new ArrayList<>();
        SelfStudyType.lowerList.forEach(st -> {
            UserActivatedProduct activatedProduct = selfStudyTypeAfentiActivationHistoryMap.get(st);
            if (activatedProduct == null)
                return;
            MySelfStudyData mySelfStudyData = MySelfStudyData.newInstance(st, studentId);
            mySelfStudyData.setExpireDate(activatedProduct.getServiceEndTime());
            finalLowerDataList.add(mySelfStudyData);
        });
        List<MySelfStudyData> lowerDataList = finalLowerDataList.stream().sorted((o1, o2) -> Integer.compare(
                o1.getSelfStudyType().getType(), o2.getSelfStudyType().getType()
        )).collect(Collectors.toList());

        add2MapList(lowerDataList, resultMapList, false, user, studentDetail);

        /*
         * 奥数 入口 #38063 特殊处理了
         */
        Map<String, Object> aoshuMap = new LinkedHashMap<>();
        aoshuMap.put("name", SelfStudyType.AOSHU_MATH.getDesc());
        aoshuMap.put("jump_type", "H5");
        aoshuMap.put("jump_key", generateAoshuUrl(user.getId(), version, studentId)); // FIXME: 2017/1/4 version取不到 必须么?
        aoshuMap.put("icon_url", getCdnBaseUrlStaticSharedWithSep() + SelfStudyType.AOSHU_MATH.getIconUrl());
        resultMapList.add(aoshuMap);
        return MapMessage.successMessage().add("my_self_study_list", resultMapList);
    }

    private final static String secretKey;
    private final static String aoshuUrl;
    private final static String appKey = "17Parent";

    static {
        if (RuntimeMode.isProduction()) {
            secretKey = "rQRw0I6j09ZA";
            aoshuUrl = "http://www.17xueaoshu.com/auth/parent/login.vpage?version=%s&studentId=%d&app_key=%s&session_key=%s&sig=%s";
        } else if (RuntimeMode.isStaging()) {
            secretKey = "rQRw0I6j09ZA";
            aoshuUrl = "http://aoshu-student.staging.17zuoye.net/auth/parent/login.vpage?version=%s&studentId=%d&app_key=%s&session_key=%s&sig=%s";
        } else {
            secretKey = "iMMrxI3XMQtd";
            aoshuUrl = "http://aoshu-student.test.17zuoye.net/auth/parent/login.vpage?version=%s&studentId=%d&app_key=%s&session_key=%s&sig=%s";
        }
    }

    private String generateAoshuUrl(Long parentId, String version, Long studentId) {
        VendorAppsUserRef vendorAppsUserRef = vendorLoaderClient.loadVendorAppUserRef("17Parent", parentId);
        if (vendorAppsUserRef == null)
            return null;
        String sessionkey = vendorAppsUserRef.getSessionKey();
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("session_key", sessionkey);
        paramsMap.put("version", version);
        paramsMap.put("studentId", SafeConverter.toString(studentId));
        paramsMap.put("app_key", appKey);
        String sig = DigestSignUtils.signMd5(paramsMap, secretKey);

        return String.format(aoshuUrl, version, studentId, appKey, sessionkey, sig);
    }

    private void add2MapList(List<MySelfStudyData> dataList, List<Map<String, Object>> resultMapList, Boolean isUpper, User parent, StudentDetail studentDetail) {

        List<FairylandProduct> parentAvailableFairylandProducts = businessVendorServiceClient.getParentAvailableFairylandProducts(parent
                , studentDetail, FairyLandPlatform.PARENT_APP, FairylandProductType.APPS);

        Map<SelfStudyType, FairylandProduct> productMap = new HashMap<>();
        parentAvailableFairylandProducts.forEach(t -> {
            SelfStudyType selfStudyType = SelfStudyType.fromOrderType(OrderProductServiceType.valueOf(t.getAppKey()));
            if (selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN)
                return;
            productMap.put(selfStudyType, t);
        });
        Map<String, VendorApps> vendorAppsMap = vendorLoaderClient.loadVendorAppsIncludeDisabled()
                .values()
                .stream()
                .collect(Collectors.toMap(VendorApps::getAppKey, e -> e));


        dataList.forEach(mySelfStudyData -> {
            SelfStudyType selfStudyType = mySelfStudyData.getSelfStudyType();
            if (selfStudyType == null)
                return;
            FairylandProduct product = productMap.get(selfStudyType); //不支持 黑名单

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", selfStudyType.getDesc());
            Boolean isNative = selfStudyType.getIsNative();
            map.put("jump_type", isNative ? "NATIVE" : "H5");

            if (isNative) {
                if (selfStudyType.getIsFree())
                    map.put("jump_key", generateJumpKey(selfStudyType.name(), null, vendorAppsMap));
                else {
                    if (product == null)
                        return;
                    map.put("jump_key", generateJumpKey(product.getAppKey(), product, vendorAppsMap));
                }

            } else {
                map.put("jump_key", generateJumpUrl(selfStudyType.getH5Url(), studentDetail.getId(), product));
            }

            //点读 语文朗读 随身听 用户所选教材的封面图。
            if (selfStudyType.getIsFree()) {
                NewBookProfile newBookProfile = parentSelfStudyPublicHelper.loadDefaultSelfStudyBook(studentDetail, selfStudyType, false);
                if (newBookProfile != null) {
                    String coverUrl = StringUtils.isBlank(newBookProfile.getImgUrl()) ? "" : getCdnBaseUrlStaticSharedWithSep() + newBookProfile.getImgUrl();
                    if (!StringUtils.isBlank(coverUrl)) {
                        map.put("icon_url", coverUrl);
                    } else
                        map.put("icon_url", getCdnBaseUrlStaticSharedWithSep() + selfStudyType.getIconUrl());
                } else
                    map.put("icon_url", getCdnBaseUrlStaticSharedWithSep() + selfStudyType.getIconUrl());
            } else
                map.put("icon_url", getCdnBaseUrlStaticSharedWithSep() + selfStudyType.getIconUrl());

            if (selfStudyType.getPcSupport() && !selfStudyType.getStudentAppSupport() && !selfStudyType.getParentAppSupport())
                map.put("label", "仅适用电脑");
            if (isUpper) {
                String studyProgress = mySelfStudyData.getStudyProgress();
                if (StringUtils.isNotBlank(studyProgress))
                    map.put("study_progress", "学习进度：" + studyProgress);
            }
            if (!selfStudyType.getIsFree()) {
                Date expireDate = mySelfStudyData.getExpireDate();
                if (expireDate == null) {//没有过期时间,为试用
                    map.put("expire_date", "试用中");
                    map.put("expire_date_color", false);
                } else {
                    Date current = new Date();
                    if (expireDate.before(current)) {//已过期
                        map.put("expire_date", "续费");
                        map.put("expire_date_color", true);
                    } else {
                        long dayDiff = DateUtils.dayDiff(expireDate, current);
                        if (dayDiff > 7) {
                            map.put("expire_date", "有效期至：" + DateUtils.dateToString(expireDate, "yyyy.MM.dd"));
                            map.put("expire_date_color", false);
                        } else {
                            map.put("expire_date", dayDiff + "天后到期");
                            map.put("expire_date_color", true);
                        }
                    }
                }
            }
            // #36307
            if (selfStudyType.getOrderProductServiceType() != null && studentDetail.getClazz() != null) {
                Map<Long, String> studentIdOpTextMap = asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                        .ParentFairylandClassmatesUsageCacheManager_fetch(studentDetail.getClazz().getId(), OrderProductServiceType.safeParse(selfStudyType.getOrderProductServiceType()))
                        .take();
                if (MapUtils.isNotEmpty(studentIdOpTextMap)) {
                    List<Map.Entry<Long, String>> list = studentIdOpTextMap.entrySet().stream().filter(t -> !t.getKey().equals(studentDetail.getId())).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(list)) {
                        Map.Entry<Long, String> entry = RandomUtils.pickRandomElementFromList(list);
                        if (entry != null)
                            map.put("op_text", entry.getValue());
                    }
                }
            }
            resultMapList.add(map);
        });
    }

    private String generateJumpUrl(String h5Url, Long sid, FairylandProduct product) {
        if (product == null) {
            return fetchMainsiteUrlByCurrentSchema() + h5Url + "?sid=" + sid + "&rel=myselfstudy";
        }
        return fetchMainsiteUrlByCurrentSchema() + h5Url + "?sid=" + sid + "&productType=" + product.getAppKey() + "&rel=myselfstudy";
    }

    private String generateJumpKey(String appKey, FairylandProduct fairylandProduct, Map<String, VendorApps> vendorAppsMap) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (fairylandProduct == null) {
            map.put("appKey", appKey);
            return JsonUtils.toJson(map);
        }
        VendorApps vendorApps = vendorAppsMap.get(fairylandProduct.getAppKey());
        String url = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
        map.put("appKey", fairylandProduct.getAppKey());
        map.put("launchUrl", url);
        map.put("orientation", vendorApps.getOrientation());
        map.put("browser", vendorApps.getBrowser());
        return JsonUtils.toJson(map);
    }

    @RequestMapping(value = "/data/mock.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mockData() {
        if (RuntimeMode.current() != Mode.PRODUCTION) {
            Long studentId = getRequestLong("sid");
            SelfStudyType selfStudyType = SelfStudyType.of(getRequestString("st"));
            String progress = getRequestString("pro");
            mySelfStudyService.updateSelfStudyProgress(studentId, selfStudyType, progress);
        }
        return MapMessage.successMessage();
    }
}
