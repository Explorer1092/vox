package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarRating;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.loader.MizarRatingLoader;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mizar Rating Loader Client
 * Created by Yuechen.Wang on 2016/9/16.
 */
public class MizarRatingLoaderClient {

    @Getter
    @ImportService(interfaceClass = MizarRatingLoader.class)
    private MizarRatingLoader remoteReference;

    public List<MizarRating> loadAllRatingByShop(String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return Collections.emptyList();
        }
        return remoteReference.loadAllRatingByShop(shopId);
    }

    public Map<String, List<MizarRating>> loadAllRatingByShop(Collection<String> shopIds) {
        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadAllRatingByShop(shopIds);
    }

    public List<MizarRating> loadRatingByParam(Integer rating, String status, String content) {
        if (rating == null || status == null || content == null) {
            return Collections.emptyList();
        }
        return remoteReference.loadRatingByParam(rating, status, content);
    }

    public List<MizarShop> loadShopByName(String name, Integer limit) {
        return remoteReference.loadShopByName(name, limit);
    }
}
