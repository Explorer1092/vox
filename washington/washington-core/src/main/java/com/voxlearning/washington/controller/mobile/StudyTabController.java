package com.voxlearning.washington.controller.mobile;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.business.consumer.BusinessVendorServiceClient;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 家长通学习Tab--1.9.0
 * Created by jiang wei on 2017/5/11.
 */
@Controller
@Slf4j
@RequestMapping(value = "/userMobile/studyTab")
public class StudyTabController extends AbstractMobileJxtController {

    private static final String[] APP_KEYS = {"AfentiExam", "PicListenBook", "AfentiMath", "AfentiChinese"};

    //为了兼容设计的字数，这里单独写死一套副标题。。。坑。。。
    private static final HashMap<String, String> descMap = new HashMap<String, String>() {
        {
            put("AfentiExam", "听说读写天天练");
            put("PicListenBook", "点读复读练口语");
            put("AfentiMath", "同步教材，个性化练习");
            put("AfentiChinese", "打牢基础，巩固提高");
        }
    };


    @Inject
    private BusinessVendorServiceClient businessVendorServiceClient;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private VendorLoaderClient vendorLoaderClient;
    @Inject
    private UserBlacklistServiceClient userBlacklistServiceClient;

    /**
     * 自学产品
     */
    @RequestMapping(value = "getSelfStudyList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getSelfStudyList() {
        Long sid = getRequestLong("sid");
        User user = currentUser();
        if (user == null || sid == 0L) {
            return MapMessage.successMessage().add("selfStudy_list", Collections.emptyList());
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        List<Map<String, Object>> selfStudyList = new ArrayList<>();
        if (studentDetail != null) {
            selfStudyList = generateSelfStudyList(user, studentDetail);
        }
        return MapMessage.successMessage().add("selfStudy_list", selfStudyList);
    }


    private List<Map<String, Object>> generateSelfStudyList(User user, StudentDetail studentDetail) {
        if (user == null || studentDetail == null) {
            return Collections.emptyList();
        }
        Map<Long, Boolean> blackListByStudent = userBlacklistServiceClient.isInBlackListByStudent(Collections.singleton(studentDetail));
        if ((MapUtils.isNotEmpty(blackListByStudent) && blackListByStudent.get(studentDetail.getId()) != null && blackListByStudent.get(studentDetail.getId())) || studentDetail.getClazzLevel() == ClazzLevel.PRIMARY_GRADUATED) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<FairylandProduct> fairylandProducts = businessVendorServiceClient.getParentAvailableFairylandProducts(user, studentDetail, FairyLandPlatform.PARENT_APP, null);
        List<String> appKeys = Arrays.asList(APP_KEYS);
        List<FairylandProduct> products = fairylandProducts.stream().filter(e -> appKeys.contains(e.getAppKey())).sorted(Comparator.comparingInt(o -> appKeys.indexOf(o.getAppKey()))).collect(Collectors.toList());
        Map<String, AppPayMapper> userAppPaidStatus = userOrderLoaderClient.getUserAppPaidStatus(appKeys, studentDetail.getId(), false);
        Map<String, VendorApps> vendorAppsMap = vendorLoaderClient.loadVendorAppsIncludeDisabled()
                .values()
                .stream()
                .collect(Collectors.toMap(VendorApps::getAppKey, e -> e));
        Map<String, String> userUseNumDesc = businessVendorServiceClient.fetchUserUseNumDesc(appKeys, studentDetail);
        if (CollectionUtils.isNotEmpty(products)) {
            products.forEach(e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("icon_url", getCdnBaseUrlAvatarWithSep() + "gridfs/" + e.getProductRectIcon());
                if (StringUtils.equals(e.getAppKey(), "PicListenBook")) {
                    map.put("main_title", SelfStudyType.PICLISTEN_ENGLISH.getDesc());
                } else {
                    map.put("main_title", e.getProductName());
                }
                map.put("sub_title", StringUtils.isNotBlank(descMap.get(e.getAppKey())) ? descMap.get(e.getAppKey()) : "");
                SelfStudyType selfStudyType = SelfStudyType.fromOrderType(OrderProductServiceType.valueOf(e.getAppKey()));
                if (selfStudyType != null && selfStudyType != SelfStudyType.PICLISTEN_ENGLISH) {
                    if (userAppPaidStatus.get(e.getAppKey()).getAppStatus() != null) {
                        Integer appStatus = userAppPaidStatus.get(e.getAppKey()).getAppStatus();
                        switch (appStatus) {
                            case 0:
                                map.put("jump_type", "H5");
                                map.put("jump_key", generateJumpUrl(selfStudyType.getH5Url(), studentDetail.getId(), e));
                                break;
                            case 1:
                                map.put("jump_type", "H5");
                                map.put("jump_key", generateJumpUrl(selfStudyType.getH5Url(), studentDetail.getId(), e));
                                break;
                            case 2:
                                map.put("jump_type", "NATIVE");
                                map.put("jump_key", generateJumpKey(e, vendorAppsMap));
                                break;
                        }
                        map.put("status", appStatus);
                    }
                } else if (StringUtils.equals(e.getAppKey(), "PicListenBook")) {
                    map.put("jump_type", "NATIVE");
                    map.put("jump_key", generateJumpKey(e, vendorAppsMap));
                    map.put("status", 2);
                }
                map.put("use_desc", userUseNumDesc.get(e.getAppKey()));
                mapList.add(map);
            });
        }
        return mapList;
    }


    private String generateJumpUrl(String h5Url, Long sid, FairylandProduct product) {
        if (product == null) {
            return fetchMainsiteUrlByCurrentSchema() + h5Url + "?sid=" + sid + "&rel=myselfstudy";
        }
        return fetchMainsiteUrlByCurrentSchema() + h5Url + "?sid=" + sid + "&productType=" + product.getAppKey() + "&rel=myselfstudy";
    }


    private String generateJumpKey(FairylandProduct fairylandProduct, Map<String, VendorApps> vendorAppsMap) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (fairylandProduct == null) {
            return "";
        }
        if (fairylandProduct.getAppKey().equals("PicListenBook")) {
            map.put("appKey", "PICLISTEN_ENGLISH");

        } else {
            VendorApps vendorApps = vendorAppsMap.get(fairylandProduct.getAppKey());
            String url = fairylandProduct.fetchRedirectUrl(RuntimeMode.current());
            map.put("appKey", fairylandProduct.getAppKey());
            map.put("launchUrl", url);
            map.put("orientation", vendorApps.getOrientation());
            map.put("browser", vendorApps.getBrowser());
        }
        return JsonUtils.toJson(map);
    }
}
