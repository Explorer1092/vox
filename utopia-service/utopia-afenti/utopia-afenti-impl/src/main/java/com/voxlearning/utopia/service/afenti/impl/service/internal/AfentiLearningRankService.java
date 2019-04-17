package com.voxlearning.utopia.service.afenti.impl.service.internal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.alps.spi.cache.KeyGenerator;
import com.voxlearning.athena.bean.AfentiRank;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.afenti.api.context.RankResultContext;
import com.voxlearning.utopia.service.afenti.cache.AfentiCache;
import com.voxlearning.utopia.service.afenti.impl.athena.AfentiRankServiceClient;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType.national;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType.school;

/**
 * @author peng.zhang.a
 * @since 16-7-29
 */
@Named
public class AfentiLearningRankService extends UtopiaAfentiSpringBean {

    @Inject private AfentiRankServiceClient afentiRankServiceClient;

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Inject private UserLoaderClient userLoaderClient;

    /**
     * 全国榜单排名只显示前200 存在与最后一名并列的用户没有加载出来
     * 读取全校榜单，一旦发现有做题数量与全国榜单对比
     */
    public Integer getUserNationRank(Map<AfentiRankType, List<Map<String, Object>>> rankList, Map<AfentiRankType, Map<Long, Integer>> userRankFlag, Long userId) {
        Integer selfSchoolRank = userRankFlag.getOrDefault(school, Collections.emptyMap()).getOrDefault(userId, 0);
        Integer selfNationalRank = userRankFlag.getOrDefault(national, Collections.emptyMap()).getOrDefault(userId, 0);
        if (selfSchoolRank != 0 && selfNationalRank == 0) {
            Map<String, Object> userRankInfo = rankList.get(school).stream()
                    .filter(p -> Objects.equals(p.get("userId"), userId))
                    .findFirst()
                    .orElse(null);
            if (userRankInfo != null && CollectionUtils.isNotEmpty(rankList.get(national))) {
                Long rightQuestionNum = (Long) userRankInfo.getOrDefault("rightQuestionNum", 0);
                int nationRankSize = rankList.get(national).size();
                Long lastNationRankUserRQN = (Long) rankList.get(national).get(nationRankSize - 1).getOrDefault("rightQuestionNum", 0);
                Integer lastNationUserRank = (Integer) rankList.get(national).get(nationRankSize - 1).get("rank");
                //学生在学校排行榜中，由于全国排行榜只会显示前100，如果用户与第100名作对题目数量相同，则将用户加入到排行榜中
                if (Objects.equals(rightQuestionNum, lastNationRankUserRQN)) {
                    selfNationalRank = lastNationUserRank;
                    Map<String, Object> userNationRankInfo = new HashMap<>();
                    userNationRankInfo.putAll(userRankInfo);
                    userNationRankInfo.put("rank", selfNationalRank);
                    rankList.get(national).add(userNationRankInfo);
                }
            }
        }
        return selfNationalRank;
    }

    public Map<AfentiRankType, Map<Long, Integer>> getUserRankFlag(Subject subject, Date calculateDate, Long schoolId) {
        try {
            CacheObjectLoader.Loader<AfentiRankType, Map<Long, Integer>> loader = AfentiCache.getAfentiCache()
                    .getCacheObjectLoader().createLoader(new KeyGenerator<AfentiRankType>() {
                        @Override
                        public String generate(AfentiRankType afentiRankType) {
                            return asyncAfentiCacheService.UserLearningRankCacheManager_generateKey(afentiRankType, schoolId, subject, calculateDate)
                                    .take();
                        }
                    });
            return loader.loads(Arrays.asList(AfentiRankType.national, AfentiRankType.school))
                    .loadsMissed(missedSources ->
                            missedSources.stream()
                                    .collect(Collectors.toMap(t -> t, e -> fetchAndUpdateRankList(schoolId, e, subject, calculateDate).getUserRankFlag())))
                    .getResult();
        } catch (Exception e) {
            logger.error("Failed query afentiRankService");
            return Collections.emptyMap();
        }
    }

    public Map<AfentiRankType, List<Map<String, Object>>> getRank(Subject subject, Date calculateDate, Long schoolId) {
        CacheObjectLoader.Loader<AfentiRankType, List<Map<String, Object>>> loader = AfentiCache.getAfentiCache()
                .getCacheObjectLoader().createLoader(new KeyGenerator<AfentiRankType>() {
                    @Override
                    public String generate(AfentiRankType afentiRankType) {
                        return asyncAfentiCacheService.LearningRankListCacheManager_generateKey(afentiRankType, schoolId, subject, calculateDate)
                                .take();
                    }
                });
        return loader.loads(Arrays.asList(AfentiRankType.national, AfentiRankType.school))
                .loadsMissed(missedSources -> missedSources.stream()
                        .collect(Collectors.toMap(t -> t, e -> fetchAndUpdateRankList(schoolId, e, subject, calculateDate).getRankList()))

                )
                .getResult();
    }

    /**
     * 更新排行榜数据
     */
    private void fetchRankList(AfentiRankType afentiRankType, Long schoolId,
                               Subject subject, Date calculateDate,
                               List<Map<String, Object>> rankList, Map<Long, Integer> userRankFlag) {
        if (afentiRankType == AfentiRankType.national) {
            asyncAfentiCacheService.LearningRankListCacheManager_addNationalRank(rankList, subject, calculateDate)
                    .awaitUninterruptibly();
            asyncAfentiCacheService.UserLearningRankCacheManager_setNationalRank(subject, calculateDate, userRankFlag)
                    .awaitUninterruptibly();
        } else {
            asyncAfentiCacheService.LearningRankListCacheManager_addSchoolRank(rankList, schoolId, subject, calculateDate)
                    .awaitUninterruptibly();
            asyncAfentiCacheService.UserLearningRankCacheManager_setSchoolRank(subject, schoolId, calculateDate, userRankFlag)
                    .awaitUninterruptibly();
        }
    }

    /**
     * 获取用户排名
     * 测试环境获取自己mock的数据 staging获取大数据组的数据
     */
    private RankResultContext fetchAndUpdateRankList(Long schoolId, AfentiRankType afentiRankType, Subject subject, Date calculateDate) {

        String calculateDateStr = DateUtils.dateToString(calculateDate, DateUtils.FORMAT_SQL_DATE);
        List<AfentiRank> afentiRanks;
        try {
            if (afentiRankType == AfentiRankType.school) {
                afentiRanks = afentiRankServiceClient.getAfentiRankSerivce().querySchoolRank(schoolId, subject, calculateDateStr);
            } else {
                afentiRanks = afentiRankServiceClient.getAfentiRankSerivce().queryNationalRank(subject, calculateDateStr);
            }
            afentiRanks = afentiRanks == null ? Collections.emptyList() : afentiRanks;
        } catch (Exception e) {
            afentiRanks = Collections.emptyList();
        }


        //获取排名为前20(school)，前100（national)的用户列表
        //计算并列用户，但总榜单不超过200个
        int maxRank = afentiRankType == AfentiRankType.national ? 100 : 20;
        int RANK_MAX_NUM = 200;
        List<Map<String, Object>> rankList = afentiRanks.stream()
                .filter(p -> p.getRank() <= maxRank)
                .map(p -> {
                    Map<String, Object> mid = new HashMap<>();
                    mid.put("userId", p.getUid());
                    mid.put("rank", p.getRank());
                    mid.put("rightQuestionNum", p.getNum());
                    return mid;
                })
                .limit(RANK_MAX_NUM)
                .collect(Collectors.toList());
        List<Long> availableUserIds = rankList.stream().map(p -> (Long) p.get("userId")).collect(Collectors.toList());
        Map<Long, User> userMaps = userLoaderClient.loadUsers(availableUserIds);
        rankList.forEach(p -> {
            Long userId = (Long) p.getOrDefault("userId", 0);
            if (userMaps.containsKey(userId)) {
                p.put("imageUrl", userMaps.get(userId).fetchImageUrl());
                p.put("userName", userMaps.get(userId).fetchRealnameIfBlankId());
            }
        });

        //标记用户排名
        Map<Long, Integer> userRankFlag = new HashMap<>();
        for (AfentiRank afentiRank : afentiRanks) {
            userRankFlag.put(afentiRank.getUid(), afentiRank.getRank());
        }
        RankResultContext rankResultContext = new RankResultContext();
        rankResultContext.setRankList(rankList);
        rankResultContext.setUserRankFlag(userRankFlag);

        fetchRankList(afentiRankType, schoolId, subject, calculateDate, rankList, userRankFlag);
        return rankResultContext;
    }
}
