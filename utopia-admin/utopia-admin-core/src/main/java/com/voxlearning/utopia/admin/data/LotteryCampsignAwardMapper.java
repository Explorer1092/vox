package com.voxlearning.utopia.admin.data;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2018-12-29 14:32
 **/
@Data
public class LotteryCampsignAwardMapper {
    private Long lotteryCampaignId;     //抽奖活动id
    private String name;                //奖项名称
    private String imgUrl;              //奖项图片
    private String describeContent;     //奖项描述
    private Boolean bigAward;           //大奖标志
    private Boolean minAward;           //最小奖项标志（如果大奖没有了，再抽中就发这个）
    private Integer awardRate;          //中奖率
    private Integer displayOrder;       //显示排序
    private Long totalAwardNum;      //总共可发放数量（-1不限）
    private Long alreadyIssuedNum;   //已发放数量
    private List<Map<String, Object>> awardList;      //奖品信息列表
}
