package com.voxlearning.utopia.admin.data;

import com.voxlearning.utopia.service.ai.data.ScoreSimpleInfo;
import lombok.Data;

import java.util.List;

/**
 * @author guangqing
 * @since 2018/8/22
 */
@Data
public class ClazzCrmOperatingPojo {
    //用户名
    private String userName;
    //用户id
    private Long userId;
    //家长通消费
    private Double consumption_JZT;
    //报名日期
    private String registerDate;
    //购课次数
    private Integer purchaseTimes;
    //薯条总消费
    private Double consumption_Fries;
    //订单来源
    private String buyFrom;
    //电话
    private String tel;
    //省份
    private String province;
    //推荐成功次数
    private Integer recommandTime;
    //是否登录公众号
    private Boolean registeredInWeChatSubscription;
    //微信号
    private String wechatNumber;
    //是否进群
    private Boolean joinedGroup;
    //学习年限
    private String duration;
    //最后活跃时间
    private String latestActive;
    // 服务价值
    private Integer serviceScore;
    //是否加微信
    private Boolean wxAdd;
    //是否加企业微信
    private Boolean epWxAdd;
    // 成绩信息
    private List<ScoreSimpleInfo> scoreSimpleInfos;
    // 微信昵称
    private String wxName;
    //用户活跃 -- 浏览文章的数量
    private Long articleViewNum;
}
