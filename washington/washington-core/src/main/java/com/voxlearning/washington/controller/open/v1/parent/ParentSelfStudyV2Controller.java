package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.PicListenFunction;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.piclisten.cache.PiclistenCache;
import com.voxlearning.utopia.service.question.api.entity.PicListenResourcesPackage;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadCollection;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;
import com.voxlearning.utopia.temp.GrindEarActivity;
import com.voxlearning.washington.controller.open.AbstractSelfStudyApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * @author jiangpeng
 * @since 2017-04-10 下午9:06
 **/
@Controller
@RequestMapping(value = "/v2/parent/selfstudy/")
@Slf4j
public class ParentSelfStudyV2Controller extends AbstractSelfStudyApiController {


    private static Map<String, Object> ALL_PUBLISHER_MAP = new LinkedHashMap<>();

    static {
        ALL_PUBLISHER_MAP.put(RES_PUBLISHER_ID, "全部");
        ALL_PUBLISHER_MAP.put(RES_PUBLISHER_NAME, "全部教材");
    }

    /**
     *
     */
    @RequestMapping(value = "/clazz_level_term.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzLevelTermList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        StudentDetail student = studentLoaderClient.loadStudentDetail(getRequestLong(REQ_STUDENT_ID));
        Integer clazzLevel;
        if (student == null)
            clazzLevel = 3;
        else {
            if (student.getClazz() == null || student.getClazzLevelAsInteger() == null || student.isJuniorStudent() || student.getClazz().isTerminalClazz())
                clazzLevel = 3;
            else
                clazzLevel = student.getClazzLevelAsInteger();
        }

        List<TextBookMapper> textBookMapperList = textBookManagementLoaderClient.getPublisherList();
        if (CollectionUtils.isEmpty(textBookMapperList))
            return successMessage().add(RES_CLAZZ_LEVEl_LIST, new ArrayList<>());
        Set<TextBookMapper.ClazzAndTerm> clazzAndTermSet = new HashSet<>();
        Map<TextBookMapper.ClazzAndTerm, List<TextBookMapper>> levelTerm2BookIdListMap = new HashMap<>();
        for (TextBookMapper textBookMapper : textBookMapperList) {
            Set<TextBookMapper.ClazzAndTerm> clazzAndTerms = textBookMapper.getClazzAndTerms();
            if (CollectionUtils.isEmpty(clazzAndTerms))
                continue;
            //去掉 全年 的 年级-学期
            clazzAndTermSet.addAll(clazzAndTerms.stream().filter(t -> t.getTermType() != Term.全年.getKey()).collect(Collectors.toSet()));
            for (TextBookMapper.ClazzAndTerm clazzAndTerm : clazzAndTerms) {
                List<TextBookMapper> textBookMappers = levelTerm2BookIdListMap.get(clazzAndTerm);
                if (CollectionUtils.isEmpty(textBookMappers))
                    textBookMappers = new ArrayList<>();

                if (clazzAndTerm.getTermType() == Term.全年.getKey()) {
                    List<TextBookMapper.ClazzAndTerm> clazzAndTermsList = clazzAndTerm.chaiQuannian();
                    for (TextBookMapper.ClazzAndTerm andTerm : clazzAndTermsList) {
                        List<TextBookMapper> textBookMappers1 = levelTerm2BookIdListMap.get(andTerm);
                        if (textBookMappers1 == null)
                            textBookMappers1 = new ArrayList<>();
                        textBookMappers1.add(textBookMapper);
                        levelTerm2BookIdListMap.put(andTerm, textBookMappers1);
                    }
                } else {
                    textBookMappers.add(textBookMapper);
                    levelTerm2BookIdListMap.put(clazzAndTerm, textBookMappers);
                }
            }
        }
        List<TextBookMapper.ClazzAndTerm> clazzAndTermList = clazzAndTermSet.stream().sorted(new TextBookMapper.ClazzAndTermComparator()).collect(Collectors.toList());

        List<Map<String, Object>> clazzLevelMapList = new ArrayList<>();
        for (TextBookMapper.ClazzAndTerm clazzAndTerm : clazzAndTermList) {
            List<TextBookMapper> textBookMappers = levelTerm2BookIdListMap.get(clazzAndTerm);
            if (CollectionUtils.isEmpty(textBookMappers))
                continue;
            textBookMappers.sort((o1, o2) -> Integer.compare(o1.getRank(), o2.getRank()));
            Map<String, Object> clazzLevelMap = new LinkedHashMap<>();
            clazzLevelMap.put(RES_CLAZZ_LEVEL, clazzAndTerm.getClazzLevel());
            clazzLevelMap.put(RES_TERM, clazzAndTerm.getTermType());
            clazzLevelMap.put(RES_NAME, clazzLevelName(clazzAndTerm));

            List<Map<String, Object>> publisherMapList = new ArrayList<>();
//            publisherMapList.add(ALL_PUBLISHER_MAP);
            Set<String> publisherIdSet = new HashSet<>();
            for (TextBookMapper textBookMapper : textBookMappers) {
                if (publisherIdSet.contains(textBookMapper.getPublisherShortName()))
                    continue;
                Map<String, Object> publisherMap = new LinkedHashMap<>();
                publisherMap.put(RES_PUBLISHER_ID, textBookMapper.getPublisherShortName());
                publisherMap.put(RES_PUBLISHER_NAME, textBookMapper.getPublisherName());
                publisherMapList.add(publisherMap);
                publisherIdSet.add(textBookMapper.getPublisherShortName());
            }
            clazzLevelMap.put(RES_PUBLISHER_LIST, publisherMapList);
            clazzLevelMapList.add(clazzLevelMap);
        }


        return successMessage().add(RES_CLAZZ_LEVEl_LIST, clazzLevelMapList).add(RES_STUDENT_CLAZZ_LEVEL, clazzLevel == null ? 0 : clazzLevel).add(RES_CURRENT_TERM, Term.ofMonth(MonthRange.current().getMonth()).getKey());

    }


    private String clazzLevelName(TextBookMapper.ClazzAndTerm clazzAndTerm) {
        String clazzLevelName;
        ClazzLevel clazzLevel = ClazzLevel.parse(clazzAndTerm.getClazzLevel());
        if (clazzLevel == null)
            clazzLevelName = "未知年级";
        else
            clazzLevelName = clazzLevel.getDescription();

        Term term = Term.of(clazzAndTerm.getTermType());
        return clazzLevelName + "（" + term.getBrief() + "）";
    }


    @RequestMapping(value = "/series_book.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage seriesBookList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_TERM, "学期");
            validateRequired(REQ_CLAZZ_LEVEL_PARENT, "年级");
            validateRequired(REQ_PUBLISHER_ID, "出版社");
            validateRequest(REQ_PUBLISHER_ID, REQ_STUDENT_ID, REQ_TERM, REQ_CLAZZ_LEVEL_PARENT);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Integer clazzLevel = getRequestInt(REQ_CLAZZ_LEVEL_PARENT);
        Integer term = getRequestInt(REQ_TERM);
        String shortPublisherName = getRequestString(REQ_PUBLISHER_ID);

        List<TextBookManagement> textBookManagementList =
                textBookManagementLoaderClient.getTextBookManagementByClazzLevel(clazzLevel);
        if (CollectionUtils.isEmpty(textBookManagementList))
            return successMessage().add(RES_SERIES_BOOK_LIST, new ArrayList<>());
        // 这里又被纳米盒子教材坑了
        /*
        纳米盒子教材的坑: 同一个教材会出现两次，一本是自制（语文: 支持 语文朗读，生字词, 英语: 支持 随身听 单词表）
                        一本是纳米盒子教材（支持点读机 包括语文英语）
        所以把所有教材列表中,如果纳米盒教材有对应的自制教材,则只返回自制教材。没有对应的自制教材,则返回纳米教材
        这个坑干掉了 哦耶 2017-07-29
        */

        Map<String, TextBookManagement> textBookManagementMap = textBookManagementList.stream().filter(t ->
                (term.equals(t.getTermType()) || Term.全年.getKey() == t.getTermType())
                        && shortPublisherName.equals(t.getShortPublisherName())
        ).collect(Collectors.toMap(TextBookManagement::getBookId, Function.identity()));
        if (MapUtils.isEmpty(textBookManagementMap))
            return successMessage().add(RES_SERIES_BOOK_LIST, new ArrayList<>());
        Set<String> bookIdSet = textBookManagementMap.keySet();
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdSet);
        Set<String> seriesIdSet = bookProfileMap.values().stream().map(NewBookProfile::getSeriesId).collect(Collectors.toSet());
        Map<String, NewBookCatalog> seriesNodeMap = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIdSet);
        Map<String, List<NewBookProfile>> series2BookListMap = new HashMap<>();
        for (NewBookProfile newBookProfile : bookProfileMap.values()) {
            String seriesId = newBookProfile.getSeriesId();
            NewBookCatalog seriesNode = seriesNodeMap.get(seriesId);
            if (seriesNode == null)
                continue;
            List<NewBookProfile> newBookProfiles = series2BookListMap.get(seriesId);
            if (newBookProfiles == null)
                newBookProfiles = new ArrayList<>();
            newBookProfiles.add(newBookProfile);
            series2BookListMap.put(seriesId, newBookProfiles);
        }
        List<String> sortedSeriesIdList = seriesNodeMap.values().stream().sorted((o1, o2) -> Integer.compare(o1.getRank(), o2.getRank()))
                .map(NewBookCatalog::getId).collect(Collectors.toList());
        List<Map<String, Object>> seriesMapList = new ArrayList<>();
        for (String seriesId : sortedSeriesIdList) {
            NewBookCatalog series = seriesNodeMap.get(seriesId);
            if (series == null)
                continue;
            List<NewBookProfile> newBookProfiles = series2BookListMap.get(seriesId);
            if (CollectionUtils.isEmpty(newBookProfiles))
                continue;
            Map<String, Object> seriesMap = new LinkedHashMap<>();
            seriesMap.put(RES_SERIES_NAME, series.getName());
            List<Map<String, Object>> bookMapList = new ArrayList<>();
            for (NewBookProfile newBookProfile : newBookProfiles) {
                Map<String, Object> map = convert2BookMap(newBookProfile, series);
                bookMapList.add(map);
            }
            seriesMap.put(RES_BOOK_LIST, bookMapList);
            seriesMapList.add(seriesMap);
        }

        return successMessage().add(RES_SERIES_BOOK_LIST, seriesMapList);

    }

    @RequestMapping(value = "/book_detail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bookDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_BOOK_ID, "教材id");
            validateRequest(REQ_BOOK_ID, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String bookId = getRequestString(REQ_BOOK_ID);
        String version = getRequestString(REQ_APP_NATIVE_VERSION);

        TextBookManagement textBookManagement = textBookManagementLoaderClient.getTextBook(bookId);
        if (textBookManagement == null)
            return failMessage(RES_RESULT_BOOK_ERROR);

        //如果穿进来的教材id是纳米盒教材id,并且该教材有对应的自制教材,用自制教材;

        NewBookProfile bookProfile = newContentLoaderClient.loadBook(bookId);
        if (bookProfile == null)
            return failMessage(RES_RESULT_BOOK_ERROR);

        MapMessage successMessage = successMessage();
        successMessage.add(RES_BOOK_NAME, bookProfile.getShortName());

        User user = getApiRequestUser();
        String sys = getRequestString(REQ_SYS);

        Map<String, Object> headMap = generateBookDetailHead(bookProfile, sys, user, version);

        //如果教材点读机需要付费并且未购买,body第一位显示购买页入口
        String status = SafeConverter.toString(headMap.get(RES_STATUS));
        List<Map<String, Object>> bodyListMap = generateBookDetailBody(bookProfile, sys, user, status, version);

        Map<String, Object> footMap = new LinkedHashMap<>();
//        footMap.put(RES_USER_IMG_URL, "");// TODO: 2017/4/17 底图url 底部图暂时没有

        long studentId = getRequestStudentId();
        StudentDetail studentDetail = null;
        if (studentId != 0L) {
            studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            /**
             *  磨耳朵活动期间，如果通过推荐教材第一次判定该学生是外研版教材，则进入此页面（接口）即算该用户今天完成不考虑读时长
             */
            if (GrindEarActivity.isInActivityPeriod()) {
                Boolean isWaiyan = false;
                String key = grindEarService.waiyanKey(studentId);
                CacheObject<Object> cacheObject = VendorCache.getVendorPersistenceCache().get(key);
                if (cacheObject == null || cacheObject.getValue() == null) {
                    studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                    if (studentDetail != null) {
                        List<String> bookIds = parentSelfStudyPublicHelper.getStudentDefaultSubjectBook(studentDetail, getClientVersion(), true,
                                null, Subject.ENGLISH);
                        Map<String, TextBookManagement> textBookByIds = textBookManagementLoaderClient.getTextBookByIds(bookIds);
                        if (MapUtils.isNotEmpty(textBookByIds) && textBookByIds.values().stream().anyMatch(t -> t.getShortPublisherName().equals("外研版"))) {
                            PiclistenCache.getPersistenceCache().set(key, (int) (GrindEarActivity.endDay.getEndTime() / 1000), "true");
                            isWaiyan = true;
                        } else
                            PiclistenCache.getPersistenceCache().set(key, (int) (GrindEarActivity.endDay.getEndTime() / 1000), "false");
                    }
                } else {
                    isWaiyan = SafeConverter.toBoolean(cacheObject.getValue());
                }
                if (isWaiyan) {
                    grindEarService.mockPushRecord(studentId, new Date());
                }
            }
        }
        long picListenPid = SafeConverter.toLong(headMap.get(RES_PICLISTEN_PARENT_ID));
        if (picListenPid == 0) {
            MapMessage mapMessage = parentServiceClient.loadOrRegisteDefaultParentUserByStudentId(studentId);
            if (mapMessage.isSuccess()) {
                Object parentObj = mapMessage.get("parent");
                if (parentObj instanceof User) {
                    User parent = User.class.cast(parentObj);
                    picListenPid = parent.getId();
                }
            }
        }
        return successMessage.add(RES_HEAD, headMap == null ? new HashMap() : headMap).add(RES_BODY, bodyListMap).add(RES_FOOT, footMap).add(RES_PICLISTEN_INFO, piclistenInfo(bookId, studentDetail, picListenPid));

    }

    private Object piclistenInfo(String bookId, StudentDetail studentDetail, Long picListenPid) {
        Map<String, Object> map = new HashMap<>();
        TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(bookId);
        addIntoMapSdk(map, sdkInfo, studentDetail);
        if (picListenPid != null && picListenPid != 0)
            map.put(RES_PICLISTEN_PARENT_ID, picListenPid);
        return map;
    }

    private List<Map<String, Object>> generateBookDetailBody(NewBookProfile bookProfile, String sys, User user, String picListeBuyStatus, String version) {
        List<Map<String, Object>> bodyMapList = new ArrayList<>();
        //自制教材不支持点读,对应纳米盒子教材支持点读,这种要隐藏单词表入口
        String bookId = bookProfile.getId();
        Long picListenParentId = null;

        //如果点读机收费,则其他功能去掉免费收费标签 47211
        Boolean picListenBookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(bookId);
        for (PicListenFunction picListenFunction : PicListenFunction.bodyFunctionList) {
            //如果教材点读机需要付费,并且不是sdk教材,并且未购买,body第一位显示购买页入口
            if (picListenFunction == PicListenFunction.PIC_LISTEN_BUY_PAGE
                    && picListeBuyStatus.equals("not_purchased")) {
                Map<String, Object> purchasePageMap = functionMap(false, null, picListenFunction, picListenFunction.getSmallImageUrl(), null, bookProfile, picListenBookNeedPay, null);
                purchasePageMap.put(RES_FUNCTION_KEY, purchaseUrl(PicListenFunction.PIC_LISTEN, bookProfile.getId()));
                bodyMapList.add(purchasePageMap);
            } else {
                if (!functionSupport(picListenFunction, bookId, sys, user, version))
                    continue;
                Boolean functionNeedPay = bookFunctionNeedPay(picListenFunction, bookId);
                DayRange lastBuyDayRange = null;

                if (picListenFunction == PicListenFunction.PIC_LISTEN) {
                    Map<String, PicListenBookPayInfo> infoMap = picListenCommonService.userBuyBookPicListenLastDayMap(user, false);
                    PicListenBookPayInfo picListenBookPayInfo = infoMap.get(bookId);
                    if (picListenBookPayInfo != null) {
                        lastBuyDayRange = picListenBookPayInfo.getDayRange();
                        picListenParentId = picListenBookPayInfo.getParentId();
                    }
                } else {
                    lastBuyDayRange = buyLastDayRange(picListenFunction, bookId, functionNeedPay, user);
                }
                Map<String, Object> functionMap = functionMap(functionNeedPay, lastBuyDayRange, picListenFunction, picListenFunction.getSmallImageUrl(), null, bookProfile, picListenBookNeedPay, null);
                bodyMapList.add(functionMap);
            }
        }
        bodyMapList.sort(Comparator.comparingInt(o -> SafeConverter.toInt(o.get(RES_FUNCTION_ORDER))));
        if (bodyMapList.size() % 2 != 0)
            bodyMapList.add(functionMap(false, null, PicListenFunction.NONE, PicListenFunction.NONE.getSmallImageUrl(), null, bookProfile, false, null));
        return bodyMapList;
    }

    private static String notSupportPicListenImg = "/public/skin/parentMobile/images/piclisten/not_support_pl.png";

    /**
     * 头图就是点读机
     * 有点读机就显示入口
     * 没有点读机 就显示 不支持点读机的图。
     *
     * @return
     */
    private Map<String, Object> generateBookDetailHead(NewBookProfile bookProfile, String sys, User user, String version) {

        Boolean picListenShow = functionSupport(PicListenFunction.PIC_LISTEN, bookProfile.getId(), sys, user, version);
        Boolean picListenNeedPay = bookFunctionNeedPay(PicListenFunction.PIC_LISTEN, bookProfile.getId());
        Map<String, PicListenBookPayInfo> infoMap = picListenCommonService.userBuyBookPicListenLastDayMap(user, false);
        PicListenBookPayInfo picListenBookPayInfo = infoMap.get(bookProfile.getId());
        DayRange lastDayRange = picListenBookPayInfo != null ? picListenBookPayInfo.getDayRange() : null;
        PicListenResourcesPackage picListenResourcesPackage = picListenLoaderClient.loadPicListenResourcesPackage(PicListenResourcesPackage.PackageType.BOOK, bookProfile.getId());

        if (!picListenShow) { // 如果不支持点读 就显示不支持的图
            return functionMap(picListenNeedPay, lastDayRange,
                    PicListenFunction.NONE, notSupportPicListenImg, picListenResourcesPackage, bookProfile, false, null);
        }

        //恶心的逻辑开始拉
        //语文用小图（其实也是大图,只不过借用的小图字段）
        Boolean bigImage = true;
        if (bookProfile.getSubjectId() == Subject.CHINESE.getId())
            bigImage = false;
        return functionMap(picListenNeedPay, lastDayRange,
                PicListenFunction.PIC_LISTEN, bigImage ? PicListenFunction.PIC_LISTEN.getBigImageUrl() : PicListenFunction.PIC_LISTEN.getSmallImageUrl(),
                picListenResourcesPackage, bookProfile, false, picListenBookPayInfo != null ? picListenBookPayInfo.getParentId() : null);
    }

    private Map<String, Object> functionMap(Boolean bookNeedPay, DayRange lastDayRange,
                                            PicListenFunction picListenFunction, String img,
                                            PicListenResourcesPackage picListenResourcesPackage, NewBookProfile bookProfile, Boolean disableStatus, Long picListenParentId) {

        Map<String, Object> functionMap = new LinkedHashMap<>();
        functionMap.put(RES_USER_IMG_URL, getCdnBaseUrlStaticSharedWithSep() + img);
        Map<String, String> purchaseStatusMap = purchaseStatus(bookNeedPay, lastDayRange);
        if (picListenFunction == PicListenFunction.NONE || picListenFunction == PicListenFunction.PIC_LISTEN_BUY_PAGE || disableStatus) {
            functionMap.put(RES_STATUS, "");
            functionMap.put(RES_EXPIRE_DATE, "");
        } else {
            functionMap.put(RES_STATUS, purchaseStatusMap.get("status"));
            if ("purchased".equals(purchaseStatusMap.get("status"))) {
                functionMap.put(RES_EXPIRE_DATE, purchaseStatusMap.get("expire"));
            }
        }
        functionMap.put(RES_PRODUCT_TYPE, picListenFunction.getProductServiceType());
        functionMap.put(RES_PURCHASE_TEXT, purchaseText(picListenFunction));
        String purchaseUrl = purchaseUrl(picListenFunction, bookProfile.getId());
        functionMap.put(RES_PURCHASE_URL, purchaseUrl);
        functionMap.put(RES_FUNCTION_TYPE, picListenFunction.getFunctionType());
        functionMap.put(RES_FUNCTION_KEY, generateFunctionKey(picListenFunction));
        functionMap.put(RES_FUNCTION_ORDER, picListenFunction.getOrder());
        if (picListenResourcesPackage != null) {
            functionMap.put(RES_ZIP_URL, picListenResourcesPackage.getPackageUrl());
            functionMap.put(RES_ZIP_MD5, picListenResourcesPackage.getPackageMd5());
        }
        if (picListenParentId != null) {
            functionMap.put(RES_PICLISTEN_PARENT_ID, picListenParentId);
        }
        return functionMap;
    }

    private String purchaseText(PicListenFunction picListenFunction) {
        if (PicListenFunction.PIC_LISTEN == picListenFunction || PicListenFunction.WALK_MAN == picListenFunction)
            return "该教材为收费教材，您还未购买。请尝试免费体验，或前往商品详情页面购买后使用。";
        if (PicListenFunction.FOLLOW_READ == picListenFunction)
            return "跟读的评分功能需要付费解锁后才能查看";
        return "";
    }

    private String purchaseUrl(PicListenFunction picListenFunction, String bookId) {
        if (!bookFunctionNeedPay(picListenFunction, bookId))
            return "";
        if (picListenFunction == PicListenFunction.PIC_LISTEN) {
            String resultBookId = bookId;
            TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(resultBookId);
            return payBookProductDetailPage(resultBookId,
                    sdkInfo.getSdkType().name(), sdkInfo.getSdkBookIdV2());
        }
        if (picListenFunction == PicListenFunction.FOLLOW_READ)
            return payFollowReadProductDetailPage();
        if (picListenFunction == PicListenFunction.WALK_MAN)
            return payWalkManProdcutDetailPage(bookId);
        return "";
    }

    private String generateFunctionKey(PicListenFunction function) {
        if (function.getFunctionType().equals("H5"))
            return fetchMainsiteUrlByCurrentSchema() + function.getUrl();
        else
            return function.name();
    }

    private boolean bookFunctionNeedPay(PicListenFunction picListenFunction, String bookId) {
        //目前已知  点读,跟读需要付费,其他都默认不收费。。。
        //加一个随身听的付费配置
        if (picListenFunction == PicListenFunction.PIC_LISTEN) {
            return textBookManagementLoaderClient.picListenBookNeedPay(bookId);
        }
//        if (picListenFunction == PicListenFunction.FOLLOW_READ)
//            return true;
        if (picListenFunction == PicListenFunction.WALK_MAN) {
            return textBookManagementLoaderClient.walkManNeedPay(bookId);
        }
        return false;
    }

    private DayRange buyLastDayRange(PicListenFunction picListenFunction, String bookId, Boolean needPay, User user) {
        if (needPay) {
            if (picListenFunction == PicListenFunction.PIC_LISTEN) {
                Map<String, PicListenBookPayInfo> infoMap = picListenCommonService.userBuyBookPicListenLastDayMap(user, false);
                PicListenBookPayInfo picListenBookPayInfo = infoMap.get(bookId);
                return picListenBookPayInfo != null ? picListenBookPayInfo.getDayRange() : null;
            }
            if (picListenFunction == PicListenFunction.FOLLOW_READ) {
                return picListenCommonService.parentBuyScoreLastDay(user.getId());
            }
            if (picListenFunction == PicListenFunction.WALK_MAN) {
                Map<String, DayRange> dayRangeMap = picListenCommonService.parentBuyWalkManLastDayMap(user.getId(), false);
                return dayRangeMap.get(bookId) != null ? dayRangeMap.get(bookId) : null;
            }
            return null;
        } else
            return null;
    }

    /**
     * 这本教材的某个点读功能是否支持
     * 如果是纳米盒子教材,就看该教材的支持情况
     * 如果是自制教材,要看下对应的纳米盒子教材的支持情况
     *
     * @param picListenFunction
     * @param bookId
     * @param sys
     * @param user
     * @return
     */
    private Boolean functionSupport(PicListenFunction picListenFunction, String bookId, String sys, User user, String version) {

        switch (picListenFunction) {
            case PIC_LISTEN:
                Boolean parentAuth = picListenCommonService.userIsAuthForPicListen(user);
                return textBookManagementLoaderClient.picListenShow(bookId, sys, parentAuth);
            case WALK_MAN:
                return textBookManagementLoaderClient.walkManBookShow(bookId, sys) && (textBookManagementLoaderClient.walkManLeastSupportVersion(bookId, version) || !textBookManagementLoaderClient.walkManNeedPay(bookId));
            case TEXT_READ:
                return textBookManagementLoaderClient.textReadBookShow(bookId, sys);
            case ENGLISH_WORD_LIST:
                return textBookManagementLoaderClient.englishWordListShow(bookId);
            case CHINESE_WORD_LIST:
                return textBookManagementLoaderClient.chineseWordListShow(bookId);
            case FOLLOW_READ:
                //65804:跟读打分功能，在有效期内用户或者教材有跟读功能的才开放，其余用户不可见
                Boolean hasBuyScore = picListenCommonService.parentHasBuyScore(user.getId());
                NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
                return textBookManagementLoaderClient.followReadBookSupport(bookId) || (hasBuyScore && StringUtils.equals("人民教育出版社", newBookProfile.getPublisher()) && newBookProfile.getSubjectId().equals(Subject.ENGLISH.getId()));
            case READING:
                return textBookManagementLoaderClient.readingShow(bookId);
            default:
                return false;
        }
    }


    @RequestMapping(value = "/my_collections.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage collectionList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (studentId == 0)
            return successMessage().add(RES_COLLECTION_LIST, new ArrayList<>());
        List<FollowReadCollection> readCollections = parentSelfStudyService.loadStudentFollowReadCollections(studentId);
        if (CollectionUtils.isEmpty(readCollections))
            return successMessage().add(RES_COLLECTION_LIST, new ArrayList<>());
        Set<String> collectionIds = new HashSet<>();
        List<String> unitIdList = new ArrayList<>();
        for (FollowReadCollection readCollection : readCollections) {
            collectionIds.add(readCollection.getId());
            unitIdList.add(readCollection.getUnitId());
        }

        Map<String, AlpsFuture<Long>> likeCountFutureMap = new HashMap<>();
        for (String collectionId : collectionIds) {
            AlpsFuture<Long> likeCountFuture = parentSelfStudyService.loadFollowReadCollectionLikeCount(collectionId);
            likeCountFutureMap.put(collectionId, likeCountFuture);
        }

        Map<String, NewBookCatalog> unitNodeMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIdList);
        Map<String, UnitWrapper> unitWrapperMap = new HashMap<>();
        unitNodeMap.values().forEach(t -> {
            NewBookCatalogAncestor bookNode = t.getAncestors().stream().filter(f -> BookCatalogType.BOOK.name().equals(f.getNodeType())).findFirst().orElse(null);
            if (bookNode != null) {
                UnitWrapper unitWrapper = new UnitWrapper();
                unitWrapper.setBookId(bookNode.getId());
                unitWrapper.setUnitId(t.getId());
                unitWrapper.setUnitName(t.getName());
                unitWrapperMap.put(t.getId(), unitWrapper);
            }

        });
        List<String> bookIdList = unitWrapperMap.values().stream().map(UnitWrapper::getBookId).collect(Collectors.toList());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);

        List<Map<String, Object>> collectionMapList = new ArrayList<>(readCollections.size());
        readCollections.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
        for (FollowReadCollection readCollection : readCollections) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(RES_COLLECTION_ID, readCollection.getId());
            UnitWrapper unitWrapper = unitWrapperMap.get(readCollection.getUnitId());
            if (unitWrapper == null)
                continue;
            String bookId = unitWrapper.getBookId();
            if (StringUtils.isBlank(bookId))
                continue;
            NewBookProfile newBookProfile = bookProfileMap.get(bookId);
            if (newBookProfile == null)
                continue;
            map.put(RES_BOOK_NAME, newBookProfile.getShortName());
            map.put(RES_UNIT_NAME, unitWrapper.getUnitName());
            AlpsFuture<Long> likeCountFuture = likeCountFutureMap.get(readCollection.getId());
            if (likeCountFuture == null)
                continue;
            map.put(RES_LIKE_COUNT, likeCountFuture.getUninterruptibly());
            map.put(RES_DATE, DateUtils.dateToString(readCollection.getCreateTime(), "yyyy年M月d日"));
            map.put(RES_COLLECTION_URL, generateCollectionUrl(readCollection.getId()));
            collectionMapList.add(map);
        }
        return successMessage().add(RES_COLLECTION_LIST, collectionMapList);
    }

    private String generateCollectionUrl(String id) {
        return fetchMainsiteUrlByCurrentSchema() + "/view/wx/parent/reading/repeat?content_id=" + id;
    }

    @Getter
    @Setter
    private class UnitWrapper {
        private String unitId;
        private String unitName;
        private String bookId;
        private String bookName;
    }

}
