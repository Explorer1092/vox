package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarRating;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.loader.MizarRatingLoader;
import com.voxlearning.utopia.service.mizar.impl.dao.shop.MizarRatingDao;
import com.voxlearning.utopia.service.mizar.impl.dao.shop.MizarShopDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer Yang on 2016/8/15.
 */
@Named
@Service(interfaceClass = MizarRatingLoader.class)
@ExposeService(interfaceClass = MizarRatingLoader.class)
public class MizarRatingLoaderImpl extends SpringContainerSupport implements MizarRatingLoader {

    @Inject private MizarRatingDao mizarRatingDao;
    @Inject private MizarShopDao mizarShopDao;

    @Override
    public List<MizarRating> loadAllRatingByShop(String shopId) {
        return mizarRatingDao.loadAllByShopId(shopId);
    }

    @Override
    public Map<String, List<MizarRating>> loadAllRatingByShop(Collection<String> shopIds) {
        return mizarRatingDao.loadAllByShopIds(shopIds);
    }

    @Override
    public List<MizarRating> loadRatingByParam(Integer rating, String status, String content) {
        return mizarRatingDao.findByParam(rating, status, content);
    }

    @Override
    public List<MizarShop> loadShopByName(String name, Integer limit) {
        return mizarShopDao.findByName(name, limit);
    }

}
