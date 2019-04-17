package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.constant.PicBookRankCategory;
import com.voxlearning.utopia.service.afenti.api.constant.PicBookRankType;
import com.voxlearning.utopia.service.afenti.api.data.PicBookPurchaseProp;
import com.voxlearning.utopia.service.afenti.api.data.PicBookRankReward;
import com.voxlearning.utopia.service.afenti.api.entity.PicBookStat;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBook;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBookResult;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookAchieve;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookProgress;
import com.voxlearning.utopia.service.afenti.cache.UserPicBookCache;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author haitian.gan
 */
@ServiceVersion(version = "20181106")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface UserPicBookService {

    Map<String, Subject> TYPE_SUBJECT_MAP = new HashMap<String, Subject>() {
        private static final long serialVersionUID = -3672364825431352016L;

        {
            put(OrderProductServiceType.ELevelReading.name(), Subject.ENGLISH);
            put(OrderProductServiceType.CLevelReading.name(), Subject.CHINESE);
            put(OrderProductServiceType.MLevelReading.name(), Subject.MATH);
        }
    };


    @CacheMethod(type = UserPicBook.class, writeCache = false)
    List<UserPicBook> loadAllUserPicBooks(@CacheParameter("userId") Long userId);

    default UserPicBook loadUserPicBook(Long userId, String bookId) {
        if (StringUtils.isBlank(bookId))
            return null;

        return loadAllUserPicBooks(userId)
                .stream()
                .filter(pb -> Objects.equals(pb.getBookId(), bookId))
                .filter(pb -> !SafeConverter.toBoolean(pb.getDisabled()))
                .findFirst()
                .orElse(null);
    }

    MapMessage report(List<UserPicBookResult> record);

    MapMessage createUserPicBook(Long userId, String bookId, String OrderProductServiceType);

    MapMessage updateUserPicBook(UserPicBook userPicBook);

    MapMessage saveUserPicBookHistory(UserPicBookResult history);

    List<String> loadShopinCartBookIds(Long userId, String OrderProductServiceType);

    MapMessage addShopinCartItem(Long userId, String OrderProductServiceType, String bookId);

    List<String> removeShopingCartItem(Long userId, String OrderProductServiceType, List<String> bookIds);

    @CacheMethod(type = UserPicBookAchieve.class, expiration = @UtopiaCacheExpiration(value = UserPicBookCache.ACHIEVE_CACHE_TTL))
    UserPicBookAchieve getThisWeekAchieve(@CacheParameter("USER_ID") Long userId);

    default Subject loadTypeSubject(String OrderProductServiceType) {
        return TYPE_SUBJECT_MAP.get(OrderProductServiceType);
    }

    @CacheMethod(type = PicBookStat.class, key = "ALL")
    List<PicBookStat> loadAllPicBookStat();

    MapMessage savePicBookStat(PicBookStat stat);

    /**
     * 进度放在CBS的Persistence里面，维度是到绘本ID。Client端的缓存维度是到用户ID
     *
     * @param userId
     * @return
     */
    @CacheMethod(type = UserPicBookProgress.class, expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today))
    Map<String, UserPicBookProgress> loadAllUserPicBookProgress(@CacheParameter("USER_ID") Long userId);

    /**
     * 查询用户拥有的绘本阅读状态,true是已读过
     *
     * @param userId
     * @param bookIds
     * @return
     */
    Map<String, Boolean> loadReadStatus(Long userId, Collection<String> bookIds);

    MapMessage loadUserRanksInfo(Long userId, PicBookRankCategory rankCategory, PicBookRankType rankType, String cdnBaseUrl);

    Map<String,List<PicBookPurchaseProp>> getRewardListByBookIds(List<String> bookIds);

    PicBookRankReward loadUserWeekRankReward(Long stuId, int week);
}
