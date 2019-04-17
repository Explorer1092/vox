package com.voxlearning.utopia.enanalyze.support;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.enanalyze.ErrorCode;
import com.voxlearning.utopia.enanalyze.MessageBuilder;
import com.voxlearning.utopia.enanalyze.api.SentenceLikeService;
import com.voxlearning.utopia.enanalyze.entity.UserGroupEntity;
import com.voxlearning.utopia.enanalyze.exception.BusinessException;
import com.voxlearning.utopia.enanalyze.persistence.SentenceLikeCache;
import com.voxlearning.utopia.enanalyze.persistence.UserGroupDao;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 好句子点赞服务实现
 *
 * @author xiaolei.li
 * @version 2018/7/24
 */
@Named
@Slf4j
@ExposeService(interfaceClass = SentenceLikeService.class)
public class SentenceLikeServiceImpl implements SentenceLikeService {

    @Resource
    SentenceLikeCache sentenceLikeCache;

    @Resource
    UserGroupDao userGroupDao;

    @Override
    public MapMessage purge(String openId) {
        try {
            List<UserGroupEntity> userGroups = userGroupDao.findByOpenId(openId);
            List<String> groupIds = userGroups.stream()
                    .map(UserGroupEntity::getOpenGroupId)
                    .collect(Collectors.toList());
            sentenceLikeCache.purge(groupIds, openId);
            return MessageBuilder.success(null);
        } catch (BusinessException e) {
            return MessageBuilder.error(ErrorCode.BIZ_ERROR.CODE, e.getMessage());
        } catch (Exception e) {
            return MessageBuilder.error(ErrorCode.UNKNOWN.CODE, e.getMessage());
        }
    }

    @Override
    public MapMessage like(Params params) {
        return MessageBuilder.success(sentenceLikeCache.like(params));
    }
}
