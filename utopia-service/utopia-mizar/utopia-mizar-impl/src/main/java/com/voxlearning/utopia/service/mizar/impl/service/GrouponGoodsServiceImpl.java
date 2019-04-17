package com.voxlearning.utopia.service.mizar.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GoodsCategory;
import com.voxlearning.utopia.service.mizar.api.entity.groupon.GrouponGoods;
import com.voxlearning.utopia.service.mizar.api.service.GrouponGoodsService;
import com.voxlearning.utopia.service.mizar.impl.dao.groupon.GoodsCategoryDao;
import com.voxlearning.utopia.service.mizar.impl.dao.groupon.GrouponGoodsDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by xiang.lv on 2016/9/21.
 *
 * @author xiang.lv
 * @date 2016/9/21   11:48
 */
@Named
@Service(interfaceClass = GrouponGoodsService.class)
@ExposeService(interfaceClass = GrouponGoodsService.class)
public class GrouponGoodsServiceImpl extends SpringContainerSupport implements GrouponGoodsService {

    @Inject
    private GoodsCategoryDao goodsCategoryDao;
    @Inject
    private GrouponGoodsDao grouponGoodsDao;

    @Override
    public GoodsCategory saveGoodsCategory(final GoodsCategory goodsCategory) {
        if (null != goodsCategory) {
            goodsCategoryDao.upsert(goodsCategory);
        }
        return goodsCategory;
    }

    @Override
    public GrouponGoods saveGrouponGoods(final GrouponGoods grouponGoods) {
        if (null != grouponGoods) {
            grouponGoodsDao.upsert(grouponGoods);
        }
        return grouponGoods;
    }


}
