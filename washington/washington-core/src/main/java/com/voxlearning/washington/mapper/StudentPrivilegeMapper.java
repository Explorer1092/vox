package com.voxlearning.washington.mapper;

import com.voxlearning.utopia.service.reward.entity.RewardSku;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xinxin @since 11/8/2016
 */
@Getter
@Setter
public class StudentPrivilegeMapper implements Serializable {
    private static final long serialVersionUID = -5815517562935518588L;

    private String privilegeId; //特权ID
    private String name;    //特权名称
    private String imgUrl;  //图标地址
    private String type;    //类型
    private Boolean current;    //是否当前选用
    private Integer leftValidTime;// 剩余有效天数
    private Boolean effective;// 状态，true为有效
    private Long relateProductId;// 特权关联的奖品id
    private String origin;// 来源
    private String acquireCondition;// 获取条件
    private String code;// 编码
    private Boolean have;   // 是否已经拥有, 此字段跟随 effective, 前端在用先不删除

    private List<RewardSku> skus;
    private List<PriceDay> priceDay;


    @Getter
    @Setter
    public static class PriceDay implements java.io.Serializable {
        private static final long serialVersionUID = -3784757236490499754L;

        private Integer day;
        private Double price;
        private Integer quantity;
    }
}
