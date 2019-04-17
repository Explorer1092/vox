package com.voxlearning.utopia.enanalyze.support;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.enanalyze.ErrorCode;
import com.voxlearning.utopia.enanalyze.MessageBuilder;
import com.voxlearning.utopia.enanalyze.api.UserService;
import com.voxlearning.utopia.enanalyze.assemble.WxCode2SessionClient;
import com.voxlearning.utopia.enanalyze.assemble.WxUtil;
import com.voxlearning.utopia.enanalyze.assemble.model.WxGroupInfo;
import com.voxlearning.utopia.enanalyze.entity.GroupEntity;
import com.voxlearning.utopia.enanalyze.entity.UserEntity;
import com.voxlearning.utopia.enanalyze.entity.UserGroupEntity;
import com.voxlearning.utopia.enanalyze.exception.BusinessException;
import com.voxlearning.utopia.enanalyze.exception.support.ThirdPartyServiceException;
import com.voxlearning.utopia.enanalyze.model.UserLoginParams;
import com.voxlearning.utopia.enanalyze.model.UserLoginResult;
import com.voxlearning.utopia.enanalyze.model.User;
import com.voxlearning.utopia.enanalyze.model.UserTotalCountParams;
import com.voxlearning.utopia.enanalyze.persistence.UserCache;
import com.voxlearning.utopia.enanalyze.persistence.GroupDao;
import com.voxlearning.utopia.enanalyze.persistence.UserDao;
import com.voxlearning.utopia.enanalyze.persistence.UserGroupDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Resource;
import javax.inject.Named;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Named
@Slf4j
@ExposeService(interfaceClass = UserService.class)
public class UserServiceImpl implements UserService {

    @Resource
    UserDao userDao;

    @Resource
    GroupDao groupDao;

    @Resource
    UserGroupDao userGroupDao;

    @Resource
    UserCache cacheDao;

    @Resource
    WxCode2SessionClient wxClient;

    @Override
    public MapMessage login(UserLoginParams input) {
        MapMessage result = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            WxCode2SessionClient.Result response = wxClient.getSession(APP_ID, APP_SECRET, input.getCode());
            final String openId = response.getOpenId();
            final String sessionKey = response.getSessionKey();
            UserEntity user = userDao.findByOpenId(openId);
            if (null == user) {
                // 新用户 => 创建一个新的用户
                user = new UserEntity();
                user.setOpenId(openId);
                Date now = new Date();
                if (StringUtils.isNotBlank(input.getNickName()))
                    user.setNickName(input.getNickName());
                if (StringUtils.isNotBlank(input.getAvatarUrl()))
                    user.setAvatarUrl(input.getAvatarUrl());
                user.setCreateDate(now);
                user.setUpdateDate(now);
                user.setSessionKey(sessionKey);
                user.setType(UserEntity.Type.EN_ANALYZE.name());
                userDao.insert(user);
            } else {
                // 老用户 => 更新用户信息
                user.setUpdateDate(new Date());
                user.setSessionKey(sessionKey);
                userDao.update(user);
            }

            // 创建会话，把openid当做会话令牌
            cacheDao.saveSession(openId, user);
            UserLoginResult output = new UserLoginResult();
            output.setToken(openId);
            output.setOpenId(openId);

            // 关联群
            if (StringUtils.isNotBlank(input.getEncryptedData())
                    && StringUtils.isNotBlank(input.getIv())) {
                // 群信息不为空
                String jsonStr = WxUtil.decrypt(input.getEncryptedData(), input.getIv(), sessionKey);
                WxGroupInfo groupInfo = JSON.parseObject(jsonStr, WxGroupInfo.class);
                String openGroupId = groupInfo.getOpenGId();
                output.setOpenGroupId(openGroupId);
                // 查看是否是一个新的群
                GroupEntity groupEntity = groupDao.findByOpenGroupId(openGroupId);
                if (null == groupEntity) {
                    // 没有当前组 => 创建组
                    groupEntity = new GroupEntity();
                    groupEntity.setOpenGroupId(openGroupId);
                    groupDao.insert(groupEntity);
                }
                // 创建用户和组的关系
                UserGroupEntity userGroupEntity = new UserGroupEntity();
                userGroupEntity.setOpenId(openId);
                userGroupEntity.setOpenGroupId(openGroupId);
                try {
                    userGroupDao.insert(userGroupEntity);
                } catch (DuplicateKeyException e) {
                    // 已经存在 => 不需要处理
                }
            }

            result = MessageBuilder.success(output);
        } catch (ThirdPartyServiceException e) {
            log.error("登录|第三方异常, input:{}", input);
            result = MessageBuilder.error(ErrorCode.WX_LOGIN_ERROR.CODE, e.getMessage());
        } catch (BusinessException e) {
            log.error("登录|业务异常 input:{}", input, e);
            result = MessageBuilder.error(ErrorCode.BIZ_ERROR.CODE, e.getMessage());
        } catch (Exception e) {
            log.error("登录|未知异常", e);
            result = MessageBuilder.error(ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public MapMessage isValid(String token) {
        return MessageBuilder.success(cacheDao.isSessionValid(token));
    }


    @Override
    public MapMessage getTotalCount(UserTotalCountParams input) {
        long count = userDao.totalCount();
        return MessageBuilder.success(count);
    }

    @Override
    public MapMessage update(User user) {
        MapMessage message;
        UserEntity entity = UserEntity.Builder.build(user);
        entity.setUpdateDate(new Date());
        try {
            userDao.update(entity);
            message = MessageBuilder.success(true);
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            message = MessageBuilder.error(ErrorCode.DAO_EXE_ERROR);
        }
        return message;
    }
}
