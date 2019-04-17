package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarRating;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Mizar Rating Loader
 * Created by Yuechen.Wang on 2016/9/16.
 */
@ServiceVersion(version = "1.0.DEV")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarRatingLoader extends IPingable {

    @CacheMethod(
            type = MizarRating.class,
            writeCache = false
    )
    List<MizarRating> loadAllRatingByShop(@CacheParameter(value = "shopId") String shopId);

    @CacheMethod(
            type = MizarRating.class,
            writeCache = false
    )
    Map<String, List<MizarRating>> loadAllRatingByShop(@CacheParameter(value = "shopId", multiple = true) Collection<String> shopId);

    List<MizarRating> loadRatingByParam(Integer rating, String status, String content);

    List<MizarShop> loadShopByName(String name, Integer limit);

}
