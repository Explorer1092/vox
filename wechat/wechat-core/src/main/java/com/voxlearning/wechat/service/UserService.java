package com.voxlearning.wechat.service;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;

import java.util.Optional;

/**
 * @author Xin Xin
 * @since 10/23/15
 */
public interface UserService {
    /**
     * 将家长与学生关联
     *
     * @param studentId 学生ID
     * @param parentId  家长ID
     * @param callName  身份
     * @return
     */
    MapMessage bindStudentToParent(Long studentId, Long parentId, String callName);

    /**
     * 注册家长号
     *
     * @param mobile    家长手机号，可为null
     * @return 注册成功返回user对象，注册失败返回null
     */
    MapMessage registParentByMobile(String mobile);

    /**
     * 将家长号与微信绑定，差发奖励
     * studentId可以传null
     *
     * @param parentId
     * @param studentId
     * @param openId
     */
    void bindParentWithReward(Long parentId, Long studentId, String openId, String source);

    /**
     * 将家长号与微信解绑
     *
     * @param openId
     */
    void unbindParent(String openId);

    /**
     * 验证手机验证码
     *
     * @param mobile
     * @param code
     * @param type
     * @return
     */
    MapMessage verifySmsCode(String mobile, String code, WechatType type);

    /**
     * 查询学生指定身份的家长号
     *
     * @param studentId 学生ID
     * @param callName  身份
     * @return
     */
    Optional<Long> getParentByCallName(Long studentId, String callName);

    /**
     * 比较家长当前选择身份与曾经选择身份性别
     * By Wyc 2016-01-04
     * @param parentId 家长ID
     * @param callName 身份
     * @return 性别没有差异--successMessage;性别不同--errorMessage
     */
    MapMessage checkCallNameGender(Long parentId, CallName callName);

}
