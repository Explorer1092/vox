package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedTextBookManagementList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by jiang wei on 2017/4/5.
 */
@ServiceVersion(version = "1.5")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TextBookManagementLoader extends IPingable {


    Map<String, TextBookManagement> getTextBookByIds(Collection<String> ids);

    default TextBookManagement getTextBook(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return null;
        }
        return getTextBookByIds(Collections.singleton(bookId)).get(bookId);
    }

    List<TextBookMapper> getPublisherList();

    Map<Integer, List<TextBookManagement>> getClazzLevelMap();

    List<TextBookManagement> getTextBookManagementList();
    // ========================================================================
    // Buffer supported methods
    // ========================================================================


    VersionedTextBookManagementList loadVersionedTextBookManagementList(long version);

    default List<TextBookManagement> getTextBookManagementByClazzLevel(Integer clazzLevel) {
        Map<Integer, List<TextBookManagement>> clazzLevelMap = getClazzLevelMap();
        if (MapUtils.isEmpty(clazzLevelMap))
            return Collections.emptyList();
        List<TextBookManagement> textBookManagements = clazzLevelMap.get(clazzLevel);
        if (textBookManagements == null)
            return Collections.emptyList();
        return textBookManagements;
    }


    default List<TextBookManagement> getTextBookManagementBySubjectClazzLevel(Subject subject, Integer clazzLevel) {
        List<TextBookManagement> textBookManagementByClazzLevel = getTextBookManagementByClazzLevel(clazzLevel);
        if (CollectionUtils.isEmpty(textBookManagementByClazzLevel))
            return Collections.emptyList();
        return textBookManagementByClazzLevel.stream().filter(t -> t.fetchBookSubject() == subject).collect(Collectors.toList());
    }

    //点读机配置相关
    default Boolean hasPicListenContent(NewBookProfile newBookProfile, String sys) {
        if (newBookProfile == null)
            return false;
        return picListenSysSupport(newBookProfile.getId(), sys);
    }

    default Boolean picListenSysSupport(String bookId, String sys) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.picListenSysSupport(sys);
    }

    default Boolean picListenMiniProgramSupport(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.picListenMiniProgramOnline();
    }

    default Boolean walkManMiniProgramSupport(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.walkManMiniProgramOnline();
    }

    default Boolean picListenBookShow(String bookId, Boolean isPreview, String sys) {
        if (bookId == null)
            return false;
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;

        if (isPreview) {
            return textBookManagement.picListenIsPreview();
        } else {
            return textBookManagement.picListenSysSupport(sys);
        }
    }

    default Boolean picListenBookNeedPay(NewBookProfile newBookProfile) {
        if (newBookProfile == null)
            return false;
        return picListenBookNeedPay(newBookProfile.getId());
    }

    default Boolean picListenBookNeedPay(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.picListenBookNeedPay();
    }

    /**
     * instead of picListenSdkInfo
     *
     * @param bookId
     * @return
     */
    @Deprecated
    default String picListenSdkBookId(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return "";
        return textBookManagement.picListenSdkBookId();
    }

    default TextBookManagement.SdkInfo picListenSdkInfo(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return TextBookManagement.SdkInfo.NONE_SDK;
        return textBookManagement.picListenSdkInfo();
    }

    default String picListenBookSdk(NewBookProfile bookProfile) {
        if (bookProfile == null)
            return null;
        return picListenBookSdk(bookProfile.getId());
    }

    default String picListenBookSdk(String bookId) {
        TextBookManagement.SdkInfo sdkInfo = picListenSdkInfo(bookId);
        return sdkInfo.getSdkType().name();
    }

    default Boolean picListenBookAuthOnline(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.picListenAuthOnline();
    }

    default Boolean picListenIsPreview(String bookId) {
        TextBookManagement textBook = getTextBook(bookId);
        if (textBook == null)
            return false;
        return textBook.picListenIsPreview();
    }

    default Boolean picListenShow(String bookId, String sys, Boolean userIsAuth) {
        if (bookId == null)
            return false;
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        if (textBookManagement.picListenSysSupport(sys))
            return true;
        if (userIsAuth && textBookManagement.picListenAuthOnline())
            return true;
        return false;
    }



    default Boolean picListenShow(String bookId, String sys) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.picListenSysSupport(sys);
    }

    //随声听相关
    default Boolean walkManBookShow(String bookId, String sys) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.walkManSysSupport(sys);
    }

    default Boolean walkManLeastSupportVersion(String bookId, String version) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.walkManVersionSupport(version);
    }

    default Boolean walkManNeedPay(String bookId) {
        TextBookManagement textBook = getTextBook(bookId);
        if (textBook == null)
            return false;
        return textBook.walkManNeedPay();
    }


    //语文朗读相关

    default Boolean textReadBookShow(String bookId, String sys) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.textReadSysSupport(sys);
    }

    default Boolean textReadNeedPay(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.textReadBookNeedPay();
    }


    //跟读相关
    default Boolean followReadBookSupport(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.followReadSupport();
    }

    //其他
    default Boolean englishWordListShow(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.englishWordSupport();
    }

    default Boolean chineseWordListShow(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.chineseWordSupport();
    }

    default Boolean readingShow(String bookId) {
        TextBookManagement textBookManagement = getTextBook(bookId);
        if (textBookManagement == null)
            return false;
        return textBookManagement.readingSupport();
    }
}
