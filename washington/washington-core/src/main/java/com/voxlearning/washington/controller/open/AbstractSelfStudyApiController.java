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

package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.piclisten.api.GrindEarService;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.piclisten.api.PicListenCommonService;
import com.voxlearning.utopia.service.piclisten.client.AsyncPiclistenCacheServiceClient;
import com.voxlearning.utopia.service.piclisten.client.MiniProgramBookServiceClient;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.utopia.service.piclisten.support.PiclistenBookImgUtils;
import com.voxlearning.utopia.service.question.consumer.PicListenLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.vendor.api.constant.TextBookSdkType;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.washington.mapper.SelfStudyAdConfig;
import com.voxlearning.washington.mapper.SelfStudyAdInfo;
import com.voxlearning.washington.service.parent.ParentSelfStudyPublicHelper;
import lombok.Getter;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.RES_BOOK_ID;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * 主要支持家长端使用点读机
 * todo 要把业务逻辑移除到一个服务类里
 *
 * @author jiangpeng
 * @since 2016-11-28 下午6:10
 **/
public class AbstractSelfStudyApiController extends AbstractApiController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private UserAdvertisementServiceClient userAdvertisementServiceClient;

    @ImportService(interfaceClass = GrindEarService.class) protected GrindEarService grindEarService;

    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Inject
    protected PicListenLoaderClient picListenLoaderClient;

    @Inject
    protected ParentSelfStudyPublicHelper parentSelfStudyPublicHelper;

    @ImportService(interfaceClass = PicListenCommonService.class)
    protected PicListenCommonService picListenCommonService;

    @Inject protected ActionServiceClient actionServiceClient;

    @ImportService(interfaceClass = ParentSelfStudyService.class)
    protected ParentSelfStudyService parentSelfStudyService;

    @Inject protected AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;


    @Inject
    protected AsyncPiclistenCacheServiceClient asyncPiclistenCacheServiceClient;

    @Inject
    protected TextBookManagementLoaderClient textBookManagementLoaderClient;

    @Inject
    protected MiniProgramBookServiceClient miniProgramBookServiceClient;


    protected enum SelfStudyAdPosition {
        TEXT_READ_UNIT_LIST_BANNER(0, null),

        PIC_LISTEN_UNIT_LIST_BANNER(0, "221202"),

        PIC_LISTEN_BOOK_SHELF_BANNER(0, "221201"), //书架banners

        WALKMAN_UNIT_LIST_BANNER(0, null),

        PIC_LISTEN_PLAYER_AD(1, null),

        PIC_LISTEN_WORD_LIST_AD(1, null),;

        @Getter
        private Integer adCount;

        @Getter
        private String adId;

        SelfStudyAdPosition(Integer adCount, String adId) {
            this.adCount = adCount;
            this.adId = adId;
        }
    }


    private static String selfStudyConfigKey = "selfStudyAd";

    private static final String selfStudyConfigPageName = "selfStudyAdConfig";

    @Override
    public void validateRequest(String... paramKeys) {
        if (!isStudentReqeust() && !isParentReqeust())
            throw new IllegalArgumentException(RES_RESULT_APP_ERROR_MSG);
        if (!RuntimeMode.isDevelopment())
            super.validateRequest(paramKeys);
    }


    @Override
    protected void validateRequired(String paramKey, Object... msgParams) {
        if (isStudentReqeust() && paramKey.equals(REQ_STUDENT_ID)) //如果是学生端请求，无需验证 sid
            return;
        super.validateRequired(paramKey, msgParams);
    }

    public boolean isParentReqeust() {
        VendorApps apiRequestApp = getApiRequestApp();
        return apiRequestApp != null && apiRequestApp.getAppKey().equals("17Parent");
    }

    public boolean isStudentReqeust() {
        VendorApps apiRequestApp = getApiRequestApp();
        return apiRequestApp != null && apiRequestApp.getAppKey().equals("17Student");
    }

    public User getCurrentParent() {
        if (RuntimeMode.isDevelopment() && getRequestLong("pid") != 0L)
            return raikouSystem.loadUser(getRequestLong("pid"));
        else
            return innerGetCurrentParent();
    }

    private User innerGetCurrentParent() {
        User user = getApiRequestUser();
        return user != null && user.fetchUserType() == UserType.PARENT ? user : null;
    }

    public Long getCurrentParentId() {
        if (RuntimeMode.isDevelopment() && getRequestLong("pid") != 0L)
            return getRequestLong("pid");
        else
            return innerGetCurrentParentId();
    }

    private Long innerGetCurrentParentId() {
        User user = getCurrentParent();
        return user != null ? user.getId() : null;
    }


    protected long getRequestStudentId() {
        if (isParentReqeust()) {
            return getRequestLong(REQ_STUDENT_ID);
        } else if (isStudentReqeust()) {
            User apiRequestUser = getApiRequestUser();
            return apiRequestUser == null ? 0 : apiRequestUser.getId();
        }
        return 0;
    }

    @Deprecated
    protected Boolean parentHitOfflineBookGrey(Long parentId) {

        List<PageBlockContent> selfStudyAdConfigPageContentList = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("picListenOffline");
        if (CollectionUtils.isEmpty(selfStudyAdConfigPageContentList))
            return false;
        PageBlockContent configPageBlockContent = selfStudyAdConfigPageContentList.stream().filter(p ->
                "offlineConfig".equals(p.getBlockName())
        ).findFirst().orElse(null);
        String configContent = configPageBlockContent == null ? "" : configPageBlockContent.getContent();

        Map<String, Object> configMap = JsonUtils.convertJsonObjectToMap(configContent);
        if (MapUtils.isEmpty(configMap))
            return false;
        String mainName = SafeConverter.toString(configMap.get("mainName"));
        String subName = SafeConverter.toString(configMap.get("subName"));
        if (StringUtils.isBlank(mainName) || StringUtils.isBlank(subName))
            return false;

        List<User> students = studentLoaderClient.loadParentStudents(parentId);
        if (CollectionUtils.isEmpty(students))
            return true;
        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIds);
        for (StudentDetail studentDetail : studentDetails.values()) {
            boolean hitgrey = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, mainName, subName, true);
            if (hitgrey)
                return true;
        }
        return false;
    }


    protected String payBookProductDetailPage(String bookId, String sdk, String sdkBookId) {
        return fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/learning_app/detail.vpage?sid=" + getRequestLong(REQ_STUDENT_ID) + "&productType=" + OrderProductServiceType.PicListenBook.name() + "&book_id=" + bookId +
                "&sdk=" + sdk + "&sdk_book_id=" + sdkBookId + "&rel=dianduji";
    }

    protected String payFollowReadProductDetailPage() {
        return fetchMainsiteUrlByCurrentSchema() + "/parentMobile/ucenter/shoppinginfo.vpage?sid=" + getRequestLong(REQ_STUDENT_ID) + "&productType=" + OrderProductServiceType.FollowRead.name();
    }

    protected String payWalkManProdcutDetailPage(String bookId) {
        return fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/learning_app/detail.vpage?sid=" + getRequestLong(REQ_STUDENT_ID) + "&productType=" + OrderProductServiceType.WalkerMan.name() + "&book_id=" + bookId + "&rel=dianduji";
    }

    protected Map<String, Object> convert2BookMap(NewBookProfile bookProfile, NewBookCatalog series) {
        Map<String, Object> map = new LinkedHashMap<>();
        addIntoMap(map, RES_BOOK_ID, bookProfile.getId());
        addIntoMap(map, RES_BOOK_NAME, bookProfile.getShortName());
        addIntoMap(map, RES_SUBJECT, Subject.fromSubjectId(bookProfile.getSubjectId()).name());
        addIntoMap(map, RES_SUBJECT_NAME, Subject.fromSubjectId(bookProfile.getSubjectId()).getValue());
        addIntoMap(map, RES_CLAZZ_LEVEL, bookProfile.getClazzLevel());
        addIntoMap(map, RES_CLAZZ_LEVEL_NAME, ClazzLevel.parse(bookProfile.getClazzLevel()).getDescription());
        addIntoMap(map, RES_BOOK_TERM, Term.of(bookProfile.getTermType()).name());
        addIntoMap(map, RES_BOOK_COVER_URL, PiclistenBookImgUtils.getCompressBookImg(bookProfile.getImgUrl()));

        if (series != null) {
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(bookProfile.getSubjectId()), series.getName());
            if (bookPress != null) {
                addIntoMap(map, RES_BOOK_VIEW_CONTENT, bookPress.getViewContent());
                addIntoMap(map, RES_BOOK_COLOR, bookPress.getColor());
                addIntoMap(map, RES_BOOK_IMAGE, MessageFormat.format(bookImgUrlPrefix, bookPress.getColor()));
            }
        }
        return map;
    }

    /**
     * 对于点读机,需要返回付费状态  免费 已购买 为购买
     * 需要sdk的教材加标识,返回对应的sdk的教材id
     *
     * @param bookProfile
     * @param series       教材系列node
     * @param isPicListen
     * @param lastDayRange
     * @return
     */
    protected Map<String, Object> convert2BookMap(NewBookProfile bookProfile, NewBookCatalog series, Boolean isPicListen, DayRange lastDayRange) {
        Map<String, Object> map = convert2BookMap(bookProfile, series);

        //付费支持
        if (isPicListen) {
            //教材状态 已购买  免费  为购买
            Boolean bookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(bookProfile);

            Map<String, String> statusMap = purchaseStatus(bookNeedPay, lastDayRange);
            addIntoMap(map, RES_STATUS, statusMap.get("status"));
            String expire = statusMap.get("expire");
            String expireStr = StringUtils.isNotBlank(expire) ? ("有效期至：" + expire) : "";
            addIntoMap(map, RES_EXPIRE_DATE, expireStr);//到期时间

            //是否需要sdk,以及对应的sdk的教材id
            TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(bookProfile.getId());
            String sdkBookId = sdkInfo.getSdkBookIdV2(getClientVersion());
            String sdk = sdkInfo.getSdkType().name();
            addIntoMapSdk(map, sdkInfo);
            //付费连接
            addIntoMap(map, RES_PURCHASE_URL, payBookProductDetailPage(bookProfile.getId(), sdk, sdkBookId));

        }
        return map;
    }

    protected Map<String, Object> convert2BookMap(NewBookProfile bookProfile, NewBookCatalog series, DayRange lastDayRange, StudentDetail studentDetail) {
        Map<String, Object> map = convert2BookMap(bookProfile, series);
        //教材状态 已购买  免费  为购买
        Boolean bookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(bookProfile);

        Map<String, String> statusMap = purchaseStatus(bookNeedPay, lastDayRange);
        addIntoMap(map, RES_STATUS, statusMap.get("status"));
        String expire = statusMap.get("expire");
        String expireStr = StringUtils.isNotBlank(expire) ? ("有效期至：" + expire) : "";
        addIntoMap(map, RES_EXPIRE_DATE, expireStr);//到期时间

        //是否需要sdk,以及对应的sdk的教材id
        TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(bookProfile.getId());
        String sdkBookId = sdkInfo.getSdkBookIdV2(getClientVersion());
        String sdk = sdkInfo.getSdkType().name();
        addIntoMapSdk(map, sdkInfo, studentDetail);
        //付费连接
        addIntoMap(map, RES_PURCHASE_URL, payBookProductDetailPage(bookProfile.getId(), sdk, sdkBookId));

        return map;
    }

    protected void addIntoMapSdk(Map<String, Object> map, TextBookManagement.SdkInfo sdkInfo) {
        //如果是人教版,只有2.0.3版本以上才返回人教版 sdk
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        if (sdkInfo.getSdkType() == TextBookSdkType.renjiao && VersionUtil.compareVersion(ver, "2.0.5.0") < 0) {
            addIntoMap(map, RES_SDK, TextBookSdkType.none.name());
        } else if (sdkInfo.getSdkType() == TextBookSdkType.hujiao && VersionUtil.compareVersion(ver, "2.1.5.0") < 0) {
            addIntoMap(map, RES_SDK, TextBookSdkType.none.name());
        } else {
            addIntoMap(map, RES_SDK, sdkInfo.getSdkType().name());
            if (sdkInfo.getSdkType().hasSdk()) {
                addIntoMap(map, RES_SDK_BOOK_ID, sdkInfo.getSdkBookIdV2(getClientVersion()));
            }
        }
    }

    protected void addIntoMapSdk(Map<String, Object> map, TextBookManagement.SdkInfo sdkInfo, StudentDetail studentDetail) {
        //如果是人教版,只有2.0.3版本以上才返回人教版 sdk
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        if (sdkInfo.getSdkType() == TextBookSdkType.renjiao && VersionUtil.compareVersion(ver, "2.0.5.0") < 0) {
            addIntoMap(map, RES_SDK, TextBookSdkType.none.name());
        } else if (sdkInfo.getSdkType() == TextBookSdkType.hujiao && VersionUtil.compareVersion(ver, "2.2.3.0") < 0) { // fixme 沪教 sdk暂时不知道哪个版本上，所以暂定3.0之前版本不使用 sdk，后面再改
            addIntoMap(map, RES_SDK, TextBookSdkType.none.name());
        } else {
            if (sdkInfo.getSdkType() == TextBookSdkType.renjiao) { //人教 sdk
                boolean noSdk = studentDetail != null && grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jzt", "noRenjiaoSdk", true);
                if (!noSdk) {
                    addIntoMap(map, RES_SDK, sdkInfo.getSdkType().name());
                    addIntoMap(map, RES_SDK_BOOK_ID, sdkInfo.getSdkBookIdV2(getClientVersion()));
                } else {
                    addIntoMap(map, RES_SDK, TextBookSdkType.none.name());
                }
            } else if (sdkInfo.getSdkType() == TextBookSdkType.hujiao) { //沪教 sdk
                boolean hujiaoSdk = studentDetail != null && grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail,
                        "jzt", "hujiaoSdk", true);
                if (hujiaoSdk) {
                    boolean noSdk = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jzt", "noHujiaoSdk", true);
                    if (!noSdk) {
                        addIntoMap(map, RES_SDK, sdkInfo.getSdkType().name());
                        addIntoMap(map, RES_SDK_BOOK_ID, sdkInfo.getSdkBookIdV2(getClientVersion()));
                    } else {
                        addIntoMap(map, RES_SDK, TextBookSdkType.none.name());
                    }
                } else {
                    addIntoMap(map, RES_SDK, TextBookSdkType.none.name());
                }
            } else {
                addIntoMap(map, RES_SDK, sdkInfo.getSdkType().name());
                if (sdkInfo.getSdkType().hasSdk()) {
                    addIntoMap(map, RES_SDK_BOOK_ID, sdkInfo.getSdkBookIdV2(getClientVersion()));
                }
            }
        }
    }

    protected Map<String, String> purchaseStatus(Boolean bookNeedPay, DayRange lastDayRange) {
        Map<String, String> statusMap = new HashMap<>();
        String bookStatus;
        String expireDateStr = "";
        if (!bookNeedPay) {
            bookStatus = "free";
        } else {
            if (lastDayRange == null || lastDayRange.getEndDate().before(new Date()))
                bookStatus = "not_purchased";
            else {
                bookStatus = "purchased";
                expireDateStr = DateUtils.dateToString(lastDayRange.getEndDate(), DateUtils.FORMAT_SQL_DATE); //到期时间
            }
        }
        statusMap.put("status", bookStatus);
        statusMap.put("expire", expireDateStr);
        return statusMap;
    }

    protected List<SelfStudyAdInfo> loadSelfStudyAdConfigListByPosition(SelfStudyAdPosition position, NewBookProfile bookProfile) {
        if (position.getAdId() == null) {

            List<SelfStudyAdConfig> configList = pageBlockContentServiceClient.loadConfigList(selfStudyConfigPageName, selfStudyConfigKey, SelfStudyAdConfig.class);
            if (CollectionUtils.isEmpty(configList))
                return Collections.emptyList();

            configList = configList.stream().filter(t -> {
                if (CollectionUtils.isEmpty(t.getPositionList()))
                    return false;
                if (!t.getPositionList().contains(position.name()))
                    return false;
                if (CollectionUtils.isNotEmpty(t.getBookIdList()) && !t.getBookIdList().contains(bookProfile.getId())) //这里比较奇怪,如果不配置bookidList,意味着所有的教材都显示。
                    return false;
                return true;
            }).collect(Collectors.toList());

            if (position.getAdCount() == 1) {
                SelfStudyAdInfo selfStudyAdInfo = configList.stream().map(SelfStudyAdConfig::getSelfStudyAdInfoList).flatMap(Collection::stream).findFirst().orElse(null);
                if (selfStudyAdInfo != null) {
                    List<SelfStudyAdInfo> adInfoList = new ArrayList<>();
                    adInfoList.add(selfStudyAdInfo);
                    return adInfoList;
                } else
                    return Collections.emptyList();
            } else {
                return configList.stream().map(SelfStudyAdConfig::getSelfStudyAdInfoList).flatMap(Collection::stream).collect(Collectors.toList());
            }
        } else {
            List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                    .loadNewAdvertisementData(getApiRequestUser().getId(), position.getAdId(), getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));
            return convert2SelfStudyAdInfos(newAdMappers);
        }
    }

    private List<SelfStudyAdInfo> convert2SelfStudyAdInfos(List<NewAdMapper> newAdMappers) {
        List<SelfStudyAdInfo> selfStudyAdInfos = new ArrayList<>();
        List<NewAdMapper> adMapperList = newAdMappers.stream().sorted((o1, o2) -> Integer.compare(o1.getPriority().getLevel(), o2.getPriority().getLevel())).collect(Collectors.toList());
        int index = 0;
        for (NewAdMapper newAdMapper : adMapperList) {
            SelfStudyAdInfo selfStudyAdInfo = new SelfStudyAdInfo();
            selfStudyAdInfo.setImgUrl(combineCdbUrl(newAdMapper.getImg()));
            selfStudyAdInfo.setJumpUrl(fetchMainsiteUrlByCurrentSchema() + AdvertiseRedirectUtils.redirectUrl(newAdMapper.getId(), index, getRequestString(REQ_APP_NATIVE_VERSION), getRequestString(REQ_SYS), "", 0L));
            index++;
            selfStudyAdInfos.add(selfStudyAdInfo);
        }
        return selfStudyAdInfos;
    }

    protected boolean needUpgrade(Boolean needPay, String version) {
        if (needPay) {
            if (VersionUtil.compareVersion(version, "1.8.2.0") < 0)
                return true;
        }
        return false;
    }

    /**
     * 三种状态
     *
     * @param bookNeedPay   教材是否需要付费
     * @param userPayedBook 用户是否付费并且有效
     * @param isFirst       是不是第一个单元
     * @return
     */
    protected String unitStatus(Boolean bookNeedPay, Boolean userPayedBook, Boolean isFirst) {
        if (!bookNeedPay) //不用付费的教材随便打开
            return RES_PAY_STATUS_FREE;
        else {
            if (userPayedBook)
                return RES_PAY_STATUS_FREE; //付费的用户随便打开
            else {
                if (isFirst)
                    return RES_PAY_STATUS_EXP; //未付费的第一单元体验
                else
                    return RES_PAY_STATUS_PAY; //其余单元需要付费
            }
        }
    }

}
