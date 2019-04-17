package com.voxlearning.utopia.service.ai.data;

import com.voxlearning.utopia.service.ai.constant.ChipsEnglishLevel;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author xuan.zhu
 * @date 2018/8/23 20:04
 * 用户详细信息
 */
@Setter
@Getter
public class AIUserInfoDetail implements Serializable {
    private static final long serialVersionUID = 5426497314762375473L;

    private Long id;                 //用户id

    private String name;             //姓名

    private String wxCode;           //微信号
    private String phone;            //电话
    private String studyDuration;   //学习年限
    private Boolean buyCompetitor;   //购买竞品
    private ChipsEnglishLevel level; //定级
    private Boolean showPlay;        //是否显示电子教材
    private String province;         //省份
    private BigDecimal chipsConsume; //薯条总消费
    private BigDecimal jztConsume;   //家长通总消费
    private String lastActive;         //最后活跃时间
    //    private Long lastActiveMils;     //最后活跃时间(时间戳，毫秒)
    private Integer buyTimes;        //购课次数

    private Integer successfulRecommendTimes; //推荐成功次数

    private List<OrderProduct> userBoughtProducts; //用户购买的产品，按时间先后顺序排序
}
