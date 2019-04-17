package com.voxlearning.utopia.enanalyze.support;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.enanalyze.MessageBuilder;
import com.voxlearning.utopia.enanalyze.api.SentenceRankService;
import com.voxlearning.utopia.enanalyze.entity.UserEntity;
import com.voxlearning.utopia.enanalyze.entity.UserGroupEntity;
import com.voxlearning.utopia.enanalyze.facade.SentencePersistenceFacade;
import com.voxlearning.utopia.enanalyze.model.SentenceRankResult;
import com.voxlearning.utopia.enanalyze.persistence.SentenceCache;
import com.voxlearning.utopia.enanalyze.persistence.SentenceLikeCache;
import com.voxlearning.utopia.enanalyze.persistence.UserDao;
import com.voxlearning.utopia.enanalyze.persistence.UserGroupDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 服务实现 - 好句子排行榜
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Named
@Slf4j
@ExposeService(interfaceClass = SentenceRankService.class)
public class SentenceRankServiceImpl implements SentenceRankService, InitializingBean {

    @Resource
    SentencePersistenceFacade sentenceCacheFacade;

    @Resource
    SentenceLikeCache sentenceLikeCache;

    @Resource
    UserDao userDao;

    @Resource
    UserGroupDao userGroupDao;

    @Resource
    SentenceCache sentenceCache;

    /**
     * 线程池服务
     */
    ExecutorService executorService;


    @Override
    public MapMessage getRank(String openGroupId, String openId) {
        List<SentenceRankResult> results = compute(openGroupId, openId);
        Optional<SentenceRankResult> target = results.stream().filter(i -> openId.equals(i.getOpenId())).findFirst();
        if (target.isPresent())
            return MessageBuilder.success(target.get());
        else {
            SentenceRankResult result = new SentenceRankResult();
            result.setOpenGroupId(openGroupId);
            result.setOpenId(openId);
            UserEntity userEntity = userDao.findByOpenId(openId);
            result.setAvatarUrl(userEntity.getAvatarUrl());
            result.setNickName(userEntity.getNickName());
            result.setSentenceScore(0f);
            result.setSentence(null);
            result.setLikes(0);
            result.setRank(0);
            result.setLikeStatus(false);
            return MessageBuilder.success(result);
        }
    }

    @Override
    public MapMessage getRanks(String openGroupId, String openId) {
        return MessageBuilder.success(compute(openGroupId, openId));
    }

    List<SentenceRankResult> compute(String openGroupId, String openId) {
        // 获取该群下所有的成员
        List<UserGroupEntity> userGroups = userGroupDao.findByGroupId(openGroupId);
        if (null == userGroups || 0 == userGroups.size()) {
            return new ArrayList<>();
        }
        // 获取用户所在的所有群id
        List<String> openIds = userGroups.stream().map(UserGroupEntity::getOpenId).collect(Collectors.toList());
        // 获取用户信息
        Future<Map<String, UserEntity>> future1 = executorService.submit(() -> userDao.findByOpenIds(openIds));
        // 获取所有人的句子
        Future<Map<String, SentenceCache.Sentence>> future2 = executorService.submit(() -> sentenceCache.queryMap(openIds));
        // 获取所有人的点赞数
        Future<Map<String, Long>> future3 = executorService.submit(() -> sentenceLikeCache.queryLikes(openGroupId, openIds));
        // 获取所有人的点赞状态
        Future<Map<String, Boolean>> future4 = executorService.submit(() -> sentenceLikeCache.batchGetLikeStatus(openGroupId, openId, openIds));
        try {
            Map<String, UserEntity> userMap = future1.get();
            Map<String, SentenceCache.Sentence> sentenceMap = future2.get();
            Map<String, Long> likesMap = future3.get();
            Map<String, Boolean> likeStatusMap = future4.get();
            // 组装结果
            List<SentenceRankResult> results = openIds.stream().map(i -> {
                SentenceRankResult _r = new SentenceRankResult();
                _r.setOpenId(i);
                _r.setOpenGroupId(openGroupId);
                if (userMap.containsKey(i)) {
                    UserEntity user = userMap.get(i);
                    _r.setNickName(user.getNickName());
                    _r.setAvatarUrl(user.getAvatarUrl());
                }
                if (likesMap.containsKey(i)) {
                    _r.setLikes(likesMap.get(i));
                }
                if (sentenceMap.containsKey(i)) {
                    SentenceCache.Sentence sentence = sentenceMap.get(i);
                    _r.setSentence(sentence.getText());
                    _r.setSentenceScore(sentence.getScore());
                }
                if (likeStatusMap.containsKey(i))
                    _r.setLikeStatus(likeStatusMap.get(i));
                return _r;

            })
                    // 句子不能为空 && 句子得分不能为0
                    .filter(i -> StringUtils.isNotBlank(i.getSentence()) && 0f != i.getSentenceScore())
                    // 排序 1 : 按照点赞数倒序
                    .sorted(Comparator.comparing(SentenceRankResult::getLikes)
                            // 排序 2 : 按照句子得分倒序
                            .thenComparing(SentenceRankResult::getSentenceScore).reversed())
                    .collect(Collectors.toList());
            // 填充排名
            for (int i = 0; i < results.size(); i++) {
                results.get(i).setRank(i + 1);
            }
            // 相同名次处理
            if (null != results && results.size() >= 2) {
                for (int i = 0, j = i + 1; (i < results.size() - 1) && (j < results.size()); i++, j++) {
                    SentenceRankResult pre = results.get(i);
                    SentenceRankResult cur = results.get(j);
                    if (pre.getLikes() == cur.getLikes() && pre.getSentenceScore() == cur.getSentenceScore()) {
                        cur.setRank(pre.getRank());
                    }
                }
            }
            return results;
        } catch (InterruptedException e) {
            log.error("查询排行榜时发生异常", e);
        } catch (ExecutionException e) {
            log.error("查询排行榜时发生异常", e);
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService = Executors.newCachedThreadPool();
    }
}
