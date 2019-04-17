package com.voxlearning.utopia.agent.service.platform;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.dao.mongo.activity.AgentPlatformUserInfoDao;
import com.voxlearning.utopia.agent.persist.entity.platform.AgentPlatformUserInfo;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

@Named
public class AgentPlatformUserInfoService {

    @Inject
    private AgentPlatformUserInfoDao platformUserInfoDao;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;

    public boolean isParent(Long platformUserId){
        AgentPlatformUserInfo userInfo = platformUserInfoDao.load(platformUserId);
        boolean insertFlag = false;
        if(userInfo == null){
            User user = userLoaderClient.loadUser(platformUserId);
            if(user == null){
                return false;
            }

            userInfo = new AgentPlatformUserInfo();
            userInfo.setId(platformUserId);
            userInfo.setRegisterTime(user.getCreateTime());
            insertFlag = true;
        }
        if(SafeConverter.toBoolean(userInfo.getIsParent())){
            return true;
        }

        if(userInfo.getIsParentUpdTime() == null || userInfo.getIsParentUpdTime().before(DayRange.current().getStartDate())){
            String mobile = sensitiveUserDataServiceClient.loadUserMobile(platformUserId);
            List<User> users = userLoaderClient.loadUsers(mobile, UserType.PARENT);
            if(CollectionUtils.isNotEmpty(users)){
                userInfo.setIsParent(true);
                userInfo.setBeParentTime(users.get(0).getCreateTime());
            }else {
                userInfo.setIsParent(false);
            }
            userInfo.setIsParentUpdTime(new Date());

            if(insertFlag){
                AgentPlatformUserInfo dbData = platformUserInfoDao.load(platformUserId);
                if (dbData == null) {
                    platformUserInfoDao.insert(userInfo);
                }else {
                    return SafeConverter.toBoolean(dbData.getIsParent());
                }
            }else {
                platformUserInfoDao.replace(userInfo);
            }

        }

        return SafeConverter.toBoolean(userInfo.getIsParent());
    }

}
