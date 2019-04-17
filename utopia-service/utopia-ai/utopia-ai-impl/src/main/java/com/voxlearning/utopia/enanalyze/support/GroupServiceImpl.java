package com.voxlearning.utopia.enanalyze.support;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.enanalyze.ErrorCode;
import com.voxlearning.utopia.enanalyze.MessageBuilder;
import com.voxlearning.utopia.enanalyze.api.GroupService;
import com.voxlearning.utopia.enanalyze.entity.UserGroupEntity;
import com.voxlearning.utopia.enanalyze.exception.BusinessException;
import com.voxlearning.utopia.enanalyze.model.GroupWithLike;
import com.voxlearning.utopia.enanalyze.model.SentenceRankResult;
import com.voxlearning.utopia.enanalyze.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import javax.inject.Named;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 群组服务实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Named
@Slf4j
@ExposeService(interfaceClass = GroupService.class)
public class GroupServiceImpl implements GroupService, InitializingBean {

    @Resource
    GroupDao groupDao;

    @Resource
    UserGroupDao userGroupDao;

    @Resource
    SentenceRankCache sentenceRankCache;

    @Resource
    SentenceLikeCache sentenceLikeCache;

    @Resource
    SentenceCache sentenceCache;

    /**
     * 线程池
     */
    ExecutorService executorService;

    @Override
    public MapMessage list(String openId) {
        // 获取当前用户所属的所有群
        List<UserGroupEntity> userGroups = userGroupDao.findByOpenId(openId);
        List<String> openGroupIds = userGroups.stream().map(UserGroupEntity::getOpenGroupId).collect(Collectors.toList());
        // 查询当前用户在所有群的排名
        Table<String, String, Long> rankTable = computer(openId, openGroupIds);
        // 获取当前用户在所有群的点赞数
        Map<String, Long> likeMap = sentenceLikeCache.queryLikes(openGroupIds, openId);
        // 组装
        List<GroupWithLike> groups = userGroups.stream()
                .map(i -> {
                    String openGroupId = i.getOpenGroupId();
                    GroupWithLike group = new GroupWithLike();
                    group.setOpenGroupId(openGroupId);
                    group.setOpenId(openId);
                    if (likeMap.containsKey(openGroupId))
                        group.setLikes(likeMap.get(openGroupId));
                    group.setRank(0);
                    if (rankTable.contains(i.getOpenGroupId(), openId))
                        group.setRank(rankTable.get(i.getOpenGroupId(), openId));
                    return group;
                })
                // 排序 : 按照排名正序
                .sorted(Comparator.comparing(GroupWithLike::getRank).reversed()
                        // 排序 : 按照点赞数倒序
                        .thenComparing(GroupWithLike::getLikes).reversed())
                .collect(Collectors.toList());
        return MessageBuilder.success(groups);
    }

    /**
     * 根据用户和多个组获取对应的排名
     *
     * @param openId       用户id
     * @param openGroupIds 多个组
     * @return 表
     */
    private Table<String, String, Long> computer(String openId, List<String> openGroupIds) {
        HashBasedTable<String, String, Long> table = HashBasedTable.create();
        openGroupIds.forEach(openGroupId -> {
            // 查询组里所有的用户
            List<UserGroupEntity> userGroups = userGroupDao.findByGroupId(openGroupId);
            List<String> openIds = userGroups.stream().map(UserGroupEntity::getOpenId).collect(Collectors.toList());
            // 查询所有用户的好句子
            Map<String, SentenceCache.Sentence> sentenceMap = sentenceCache.queryMap(openIds);
            // 查询用户在当前组内的点赞数
            Map<String, Long> likesMap = sentenceLikeCache.queryLikes(openGroupId, openIds);
            // 组装结果
            List<SentenceRankResult> results = openIds.stream().map(i -> {
                SentenceRankResult _r = new SentenceRankResult();
                _r.setOpenId(i);
                _r.setOpenGroupId(openGroupId);
                if (likesMap.containsKey(i))
                    _r.setLikes(likesMap.get(i));
                if (sentenceMap.containsKey(i)) {
                    _r.setSentence(sentenceMap.get(i).getText());
                    _r.setSentenceScore(sentenceMap.get(i).getScore());
                }

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
            results.stream().forEach(i -> {
                table.put(i.getOpenGroupId(), i.getOpenId(), i.getRank());
            });
        });
        return table;
    }

    @Override
    public MapMessage remove(String openId, String openGroupId) {
        try {
            // 清空群对应的点赞数
            sentenceLikeCache.purge(Lists.newArrayList(openGroupId), openId);
            // 删除用户和群的关联
            userGroupDao.delete(openId, openGroupId);
            return MessageBuilder.success(true);
        } catch (BusinessException e) {
            return MessageBuilder.error(ErrorCode.BIZ_ERROR.CODE, "删除群时发生错误");
        } catch (Exception e) {
            log.error(String.format("删除群时发生了错误,openId=%s, openGroupId=%s", openId, openGroupId), e);
            return MessageBuilder.error(ErrorCode.BIZ_ERROR.CODE, e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService = Executors.newCachedThreadPool();
    }
}
