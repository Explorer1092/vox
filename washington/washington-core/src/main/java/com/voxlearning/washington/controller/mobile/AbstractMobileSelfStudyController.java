package com.voxlearning.washington.controller.mobile;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.ChineseArticle;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewChineseContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewWordStockLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.piclisten.api.PicListenCommonService;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.controller.open.v1.content.ContentApiConstants;
import com.voxlearning.washington.service.parent.ParentSelfStudyPublicHelper;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * @author jiangpeng
 * @since 2017-03-01 下午2:12
 **/
public class AbstractMobileSelfStudyController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    protected NewWordStockLoaderClient newWordStockLoaderClient;

    @Inject
    protected ParentSelfStudyPublicHelper parentSelfStudyPublicHelper;

    @Inject
    protected TextBookManagementLoaderClient textBookManagementLoaderClient;

    @Inject
    protected NewChineseContentLoaderClient newChineseContentLoaderClient;

    @ImportService(interfaceClass = ParentSelfStudyService.class)
    protected ParentSelfStudyService parentSelfStudyService;

    @ImportService(interfaceClass = PicListenCommonService.class)
    protected PicListenCommonService picListenCommonService;


    @Override
    protected User currentParent(){
        if (RuntimeMode.isDevelopment())
            return raikouSystem.loadUser(getRequestLong("uid"));
        User user = super.currentParent();
        if (user != null && user.isParent())
            return user;
        else
            return null;
    }

    protected List<ChineseSentence> getArticleSentenceList(String lessonId){
        List<ChineseArticle> chineseArticles = newChineseContentLoaderClient.loadChineseArticlesByLessonId(lessonId);
        if (CollectionUtils.isEmpty(chineseArticles))
            return Collections.emptyList();
        List<ChineseSentence> chineseSentenceList = chineseArticles.get(0).getChineseSentences();
        if (CollectionUtils.isEmpty(chineseSentenceList))
            return Collections.emptyList();
        return chineseSentenceList;
    }

    protected Map<String, Object> addUnitInfo2BookMap(Map<String, Object> bookMap , NewBookProfile newBookProfile){
        if (MapUtils.isEmpty(bookMap))
            bookMap = new LinkedHashMap<>();
        Map<String, List<NewBookCatalog>> bookId2ModuleMap = newContentLoaderClient
                .loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.MODULE);
        List<NewBookCatalog> moduleList = bookId2ModuleMap == null ? new ArrayList<>() : bookId2ModuleMap.get(newBookProfile.getId());
        Boolean unitGroupFlag = !CollectionUtils.isEmpty(moduleList);

        bookMap.put(RES_GROUP_FLAG, unitGroupFlag);
        if (!unitGroupFlag) {
            List<Map> unitMapList = new LinkedList<>();
            Map<String, List<NewBookCatalog>> bookId2UnitListMap = newContentLoaderClient
                    .loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.UNIT);
            List<NewBookCatalog> unitList = bookId2UnitListMap == null ? new ArrayList<>() : bookId2UnitListMap.get(newBookProfile.getId());
            if (!CollectionUtils.isEmpty(unitList)) {
                for (NewBookCatalog unit : unitList) {
                    Map<String, Object> unitMap = new LinkedHashMap<>();
                    unitMap.put(RES_UNIT_ID, unit.getId());
                    unitMap.put(RES_RANK, unit.getRank());
                    unitMap.put(RES_UNIT_CNAME, unit.getName());
                    unitMapList.add(unitMap);
                }

                bookMap.put(RES_UNIT_LIST, unitMapList);
                bookMap.put(RES_GROUP_LIST, null);
            }

        } else {
            List<Map> groupList = new LinkedList<>();
            Set<String> moduleIdList = moduleList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());
            Map<String, List<NewBookCatalog>> moduleId2UnitListMap = newContentLoaderClient
                    .loadChildren(moduleIdList, BookCatalogType.UNIT);
            if (moduleId2UnitListMap == null) {
                moduleId2UnitListMap = new LinkedHashMap<>();
            }

            for (NewBookCatalog module : moduleList) {
                Map<String, Object> groupInfo = new LinkedHashMap<>();

                String moduleId = module.getId();
                List<NewBookCatalog> unitList = moduleId2UnitListMap.get(moduleId);
                if (unitList == null)
                    unitList = new ArrayList<>();
                List<Map<String, Object>> groupUnitList = new LinkedList<>();
                for (NewBookCatalog unit : unitList) {
                    Map<String, Object> unitGroupInfo = new LinkedHashMap<>();
                    addIntoMap(unitGroupInfo, RES_UNIT_ID, unit.getId());
                    addIntoMap(unitGroupInfo, RES_UNIT_CNAME, unit.getName());
                    addIntoMap(unitGroupInfo, RES_RANK, unit.getRank());
                    groupUnitList.add(unitGroupInfo);
                }

                groupInfo.put(RES_GROUP_CNAME, module.getName());
                groupInfo.put(RES_GROUP_INFO_LIST, groupUnitList);
                groupList.add(groupInfo);

            }

            bookMap.put(RES_UNIT_LIST, null);
            bookMap.put(RES_GROUP_LIST, groupList);
        }

        return bookMap;
    }

    protected Map<String, Object> convert2BookMap(NewBookProfile bookProfile, Boolean isPicListen, DayRange lastDayRange, Boolean hitGreyTest){
        return convert2BookMap(bookProfile, isPicListen, lastDayRange, hitGreyTest, new HashSet<>());
    }

    /**
     * 对于点读机,需要返回付费状态  免费 已购买 为购买
     * 需要sdk的教材加标识,返回对应的sdk的教材id
     * @param bookProfile
     * @param isPicListen
     * @param lastDayRange
     * @param hitGreyTest
     * @param alreadyAddedBookIdSet
     * @return
     */
    protected Map<String, Object> convert2BookMap(NewBookProfile bookProfile, Boolean isPicListen, DayRange lastDayRange, Boolean hitGreyTest, Set<String> alreadyAddedBookIdSet) {
        Map<String, Object> map = new LinkedHashMap<>();
        addIntoMap(map, ContentApiConstants.RES_BOOK_ID, bookProfile.getId());
        addIntoMap(map, RES_BOOK_NAME, bookProfile.getShortName());
        addIntoMap(map, RES_CLAZZ_LEVEL, bookProfile.getClazzLevel());
        addIntoMap(map, RES_CLAZZ_LEVEL_NAME, ClazzLevel.parse(bookProfile.getClazzLevel()).getDescription());
        addIntoMap(map, RES_BOOK_TERM, Term.of(bookProfile.getTermType()).name());
        addIntoMap(map, RES_BOOK_COVER_URL, StringUtils.isBlank(bookProfile.getImgUrl()) ? "" : getCdnBaseUrlStaticSharedWithSep() + bookProfile.getImgUrl());

        //付费支持
        if (isPicListen){
            //教材状态 已购买  免费  为购买
            Boolean bookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(bookProfile);
            String bookStatus;
            String expireDateStr = null;//到期时间
            if (!bookNeedPay){
                bookStatus = "free";
            }else {
                if (lastDayRange == null || lastDayRange.getEndDate().before(new Date()))
                    bookStatus = "not_purchased";
                else {
                    bookStatus = "purchased";
                    expireDateStr = DateUtils.dateToString(lastDayRange.getEndDate(), DateUtils.FORMAT_SQL_DATE); //到期时间
                }
            }
            addIntoMap(map, RES_STATUS, bookStatus);
            addIntoMap(map, RES_END_DATE, expireDateStr);//到期时间
            addIntoMap(map, RES_ALREADY_ADDED, alreadyAddedBookIdSet.contains(bookProfile.getId()));

            //是否需要sdk,以及对应的sdk的教材id
            TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(bookProfile.getId());
            //付费连接 （学生端购买 ,h5没有使用这个字段）
            addIntoMap(map, RES_PURCHASE_URL, payProductDetailPage(bookProfile.getId(), sdkInfo.getSdkType().name(), sdkInfo.getSdkBookIdV2(getRequestString(ApiConstants.REQ_APP_NATIVE_VERSION))));

        }
        return map;
    }

    private String payProductDetailPage(String bookId, String sdk, String sdkBookId){
        if (!"none".equals(sdk))
            return "";
        return fetchMainsiteUrlByCurrentSchema() + "/parentMobile/ucenter/shoppinginfo.vpage?sid="+getRequestLong(REQ_STUDENT_ID)+"&productType="+ OrderProductServiceType.PicListenBook.name()+"&book_id=" + bookId +
                "&sdk="+sdk +"&sdk_book_id="+sdkBookId;
    }
}
