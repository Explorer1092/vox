package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.shop.*;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Created by Summer Yang on 2016/8/15.
 */
@ServiceVersion(version = "20160815")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarService {
    //------------------------------------------------------------------------------
    //-------------------------          BRAND        ------------------------------
    //------------------------------------------------------------------------------
    MapMessage saveMizarBrand(MizarBrand brand);

    //------------------------------------------------------------------------------
    //-------------------------           SHOP        ------------------------------
    //------------------------------------------------------------------------------
    MapMessage saveMizarShop(MizarShop shop);

    // FIXME 这两个方法是不是重复了
    void updateMizarShop(MizarShop mizarShop);

    MapMessage likedShop(Long parentId, String shopId, Integer activityId);

    //------------------------------------------------------------------------------
    //-------------------------          GOODS        ------------------------------
    //------------------------------------------------------------------------------
    MapMessage saveMizarShopGoods(MizarShopGoods goods);

    //------------------------------------------------------------------------------
    //-------------------------         RATING        ------------------------------
    //------------------------------------------------------------------------------
    MapMessage saveMizarRating(MizarRating rating);

    //------------------------------------------------------------------------------
    //-------------------------     RESERVE RECORD    ------------------------------
    //------------------------------------------------------------------------------

    MapMessage saveMizarReserve(MizarReserveRecord reserveRecord);

    MapMessage updateReservationStatus(Collection<Long> records, MizarReserveRecord.Status status);

    //------------------------------------------------------------------------------
    //-------------------------          COURSE       ------------------------------
    //------------------------------------------------------------------------------

    MapMessage saveMizarCourse(MizarCourse course);

    MapMessage saveCourseTargets(String courseId, Integer type, Collection<String> regionList, Boolean isAppend);

    MapMessage clearCourseTargets(String courseId, Integer type);

    MapMessage removeMizarCourse(String courseId);

    MapMessage saveMizarCategory(MizarCategory category);

    MapMessage removeMizarCategory(String categoryId);
}
