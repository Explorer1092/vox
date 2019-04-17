package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.shop.*;
import com.voxlearning.utopia.service.mizar.api.service.MizarService;

import java.util.Collection;
import java.util.List;

/**
 * Created by Summer Yang on 2016/8/15.
 */
public class MizarServiceClient {

    @ImportService(interfaceClass = MizarService.class) private MizarService mizarService;

    //------------------------------------------------------------------------------
    //-------------------------          BRAND        ------------------------------
    //------------------------------------------------------------------------------
    public MapMessage saveMizarBrand(MizarBrand brand) {
        if (brand == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return mizarService.saveMizarBrand(brand);
    }

    //------------------------------------------------------------------------------
    //-------------------------           SHOP        ------------------------------
    //------------------------------------------------------------------------------
    public MapMessage saveMizarShop(MizarShop shop) {
        if (shop == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return mizarService.saveMizarShop(shop);
    }

    public void updateMizarShop(MizarShop mizarShop) {
        mizarService.updateMizarShop(mizarShop);
    }

    public MapMessage likedShop(Long parentId, String shopId, Integer activityId) {
        return mizarService.likedShop(parentId, shopId, activityId);
    }

    //------------------------------------------------------------------------------
    //-------------------------          GOODS        ------------------------------
    //------------------------------------------------------------------------------
    public MapMessage saveMizarShopGoods(MizarShopGoods goods) {
        if (goods == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return mizarService.saveMizarShopGoods(goods);
    }

    //------------------------------------------------------------------------------
    //-------------------------         RATING        ------------------------------
    //------------------------------------------------------------------------------
    public MapMessage saveMizarRating(MizarRating rating) {
        if (rating == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return mizarService.saveMizarRating(rating);
    }

    //------------------------------------------------------------------------------
    //-------------------------     RESERVE RECORD    ------------------------------
    //------------------------------------------------------------------------------

    public MapMessage saveMizarReserve(MizarReserveRecord reserveRecord) {
        if (reserveRecord == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return mizarService.saveMizarReserve(reserveRecord);
    }

    public MapMessage updateReservationStatus(Collection<Long> records, MizarReserveRecord.Status status) {
        if (CollectionUtils.isEmpty(records) || status == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return mizarService.updateReservationStatus(records, status);
    }

    //------------------------------------------------------------------------------
    //-------------------------          COURSE       ------------------------------
    //------------------------------------------------------------------------------
    public MapMessage saveMizarCourse(MizarCourse course) {
        return mizarService.saveMizarCourse(course);
    }

    public MapMessage saveCourseTargets(String courseId, Integer type, List<String> regionList, Boolean isAppend) {
        return mizarService.saveCourseTargets(courseId, type, regionList, isAppend);
    }

    public MapMessage clearCourseTargets(String courseId, Integer type) {
        return mizarService.clearCourseTargets(courseId, type);
    }

    public MapMessage removeMizarCourse(String courseId) {
        return mizarService.removeMizarCourse(courseId);
    }

    public MapMessage saveMizarCategory(MizarCategory category) {
        return mizarService.saveMizarCategory(category);
    }

    public MapMessage removeMizarCategory(String categoryId) {
        return mizarService.removeMizarCategory(categoryId);
    }
}
