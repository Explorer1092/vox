package com.voxlearning.washington.helpers;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomGenerator;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.consumer.ParentServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2018-4-12
 */
@Named
public class ParentRegisterHelper {

    @Inject
    private UserServiceClient userServiceClient;
    @Inject
    private ParentServiceClient parentServiceClient;

    public MapMessage registerChannelCParent(String mobile, RoleType roleType, String uuid, String webSource) {
        //初始化要注册的用户
        NeonatalUser neonatalUser = initChannelCParent(mobile, roleType, webSource);
        MapMessage message = userServiceClient.registerUserAndSendMessage(neonatalUser);
        if (!message.isSuccess()) {
            return message;
        }
        User newUser = (User) message.get("user");
        //注册的时候同步shippingAddress 电话号码信息
        //绑定手机号
        MapMessage activeResult = userServiceClient.activateUserMobile(newUser.getId(), mobile, true);
        if (!activeResult.isSuccess()) {
            return activeResult;
        }

        ParentExtAttribute parentExtAttribute = new ParentExtAttribute(newUser.getId());
        parentExtAttribute.setUuid(uuid);//关联uuid
        parentExtAttribute.setBrandFlag(true);//默认c端家长同意:家长通产品商业区隔协议书
        MapMessage extResult = parentServiceClient.generateParentExtAttribute(parentExtAttribute);
        //5. 家长邀请注册后处理 新活动要求在绑定孩子后邀请成功
//        deprecatedInvitationServiceClient.parentInviteSuccess(mobile, newUser.getId());
        if (!extResult.isSuccess()) {
            return extResult;
        }
        return message;
    }

    //初始化用户
    public NeonatalUser initChannelCParent(String mobile, RoleType roleType, String webSource) {
        //用户名和昵称为空字符串,密码为随机数,
        //下列数据为空时是否违背底层数据库设计
        String realName = "";
        String nickName = "";
        UserType userType = UserType.PARENT;

        // Save User Info
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setMobile(mobile);
        neonatalUser.setRoleType(roleType);
        neonatalUser.setUserType(userType);
        neonatalUser.setRealname(realName);
        neonatalUser.setNickName(nickName);
        neonatalUser.setPassword(RandomGenerator.generatePlainPassword());
        neonatalUser.setInviter(null);

        neonatalUser.setWebSource(webSource);
        return neonatalUser;
    }


}
