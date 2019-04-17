package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GoodsCategory;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;
import com.voxlearning.utopia.service.mizar.api.service.GrouponGoodsService;

/**
 * Created by xiang.lv on 2016/9/21.
 * 前台相关接口api
 *
 * @author xiang.lv
 * @date 2016/9/21   11:52
 */
public class GrouponGoodsServiceClient {

    @ImportService(interfaceClass = GrouponGoodsService.class)
    private GrouponGoodsService grouponGoodsService;

    public GoodsCategory saveGoodsCategory(final GoodsCategory goodsCategory) {
        return grouponGoodsService.saveGoodsCategory(goodsCategory);
    }

    public GoodsCategory saveGoodsCategory(final String categoryName, final String categoryCode, final Integer orderIndex) {
        GoodsCategory goodsCategory = new GoodsCategory();
        goodsCategory.setCategoryName(categoryName);
        goodsCategory.setCategoryCode(categoryCode);
        goodsCategory.setOrderIndex(orderIndex);
        return grouponGoodsService.saveGoodsCategory(goodsCategory);
    }

    public void saveGrouponGoods(final GrouponGoods grouponGoods) {
        grouponGoodsService.saveGrouponGoods(grouponGoods);
    }
}
