package com.voxlearning.enanalyze.aggregate.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.enanalyze.MessageFetcher;
import com.voxlearning.enanalyze.ViewCode;
import com.voxlearning.enanalyze.aggregate.UserAggregator;
import com.voxlearning.enanalyze.exception.BusinessException;
import com.voxlearning.enanalyze.view.UserLoginRequest;
import com.voxlearning.enanalyze.view.UserLoginView;
import com.voxlearning.enanalyze.view.UserRequest;
import com.voxlearning.utopia.enanalyze.api.UserService;
import com.voxlearning.utopia.enanalyze.model.UserLoginParams;
import com.voxlearning.utopia.enanalyze.model.UserLoginResult;
import com.voxlearning.utopia.enanalyze.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户聚合服务实现
 *
 * @author xiaolei.li
 * @version 2018/7/19
 */
@Slf4j
@Service
public class UserAggregatorImpl implements UserAggregator {

    @ImportService(interfaceClass = UserService.class)
    @ServiceVersion(version = "20180701")
    private UserService userService;

    @Override
    public UserLoginView login(UserLoginRequest request) {
        UserLoginParams input = new UserLoginParams();
        input.setCode(request.getCode());
        input.setNickName(request.getNickName());
        input.setAvatarUrl(request.getAvatarUrl());
        input.setEncryptedData(request.getEncryptedData());
        input.setIv(request.getIv());
        MapMessage result = userService.login(input);
        UserLoginView response = new UserLoginView();
        if (result.isSuccess()) {
            UserLoginResult authOutput = MessageFetcher.get(result, UserLoginResult.class);
            response.setToken(authOutput.getToken());
            response.setOpenId(authOutput.getOpenId());
            response.setOpenGroupId(authOutput.getOpenGroupId());
        } else {
            throw new BusinessException(ViewCode.BIZ_ERROR, result.getInfo());
        }
        return response;
    }

    @Override
    public boolean isValid(String token) {
        MapMessage result = userService.isValid(token);
        if (result.isSuccess()) {
            return MessageFetcher.get(result, Boolean.class);
        } else {
            throw new BusinessException(ViewCode.BIZ_ERROR, result.getInfo());
        }
    }

    @Override
    public void update(UserRequest request) {
        User user = UserRequest.Builder.build(request);
        MapMessage message = userService.update(user);
        if (!message.isSuccess()) {
            throw new BusinessException(ViewCode.BIZ_ERROR, message.getInfo());
        }
    }
}
