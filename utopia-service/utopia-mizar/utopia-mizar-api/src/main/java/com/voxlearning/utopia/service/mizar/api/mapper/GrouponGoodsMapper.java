package com.voxlearning.utopia.service.mizar.api.mapper;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xiang.lv
 *         <p>
 *         商品对外映射
 */
@Data
public class GrouponGoodsMapper implements Serializable {

    private static final long serialVersionUID = 1559355259683496377L;
    private String id;
    private String outerGoodsId;

    private String categoryCode;

    private String shortTitle;                       //短标题

    private String title;                           //标题

    private Double price;                           //当前价格

    private Double originalPrice;                   //原价

    private String image;                           //当前需要显示的图片,从imagesList选取一个

    private Integer saleCount;                      //销量

    private Boolean postFree;                       //是否包邮,true-是,false-否

    private String goodsSource;                     //商品来源,天猫、淘宝、京东等

    private Date currentTime;                       //服务器当前时间,用于客户端倒计时

    private Date beginTime;                          //开始时间

    private Date endTime;                           //结束时间

    private Boolean oos;                            //是否卖光 true-是,false-否

    private String dataSource;                      //数据来源,折800采集、什么值得买采集、人工编辑

    private Integer orderIndex;                     //推荐排序值

    private String specialTag ;                      //特色标签,多个以逗号分隔

    private String recommend ;                       //推荐文本

    private String goodsTag ;                       //商品标签,多个以逗号分隔

    private String url;                             //跳转url

    private Date deployTime;                       // 发布时间

    private String deployDay;                       //截取的发布时间的月和天

}
