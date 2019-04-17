package com.voxlearning.utopia.service.zone.api.entity.plot;

import com.voxlearning.utopia.service.zone.api.entity.boss.ClazzBossUserAward;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author : kai.sun
 * @version : 2018-11-10
 * @description :
 **/

@Getter
@Setter
public class PlotActivityBizObject {

    private Boolean vip;                                //是否会员

    private String currentPlot;                        //当前剧情

    private Boolean firstEntry;                        //是否第一次看剧情

    private Integer firstBuy;//是否第一次购买 0否，1是

    private Set<Integer> entryNewPlot;

    private Set<Integer> plotPop;

    private Integer currentHighestDiffiCult;                 //当前最高难度,变需求存的星星
//    private Map<String,Boolean> plotUnLock;             //剧情是否解锁

//    private Map<Integer,Integer> nodeRightCount;      //每段剧情答对题数量key：三个值1,2,3
//
//    private Map<Integer,List<String>> doneQuestions;    //每段剧情已做过的题
    /**
     * 购买的app内容
     */
    private String appkey;

    //用户身上奖励 key 奖励id  value 奖励
    private Map<String, ClazzBossUserAward> userAwardMap;

    private Date date; //排名奖励领取日期  用来判断今天是否领取

    private Boolean isReceived;

}
