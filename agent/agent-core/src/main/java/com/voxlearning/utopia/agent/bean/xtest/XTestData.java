/**
 * Author:   xianlong.zhang
 * Date:     2018/9/21 17:14
 * Description: X测相关指标数据
 * History:
 */
package com.voxlearning.utopia.agent.bean.xtest;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class XTestData implements Serializable {

    //参与X测学生数
    private Integer stuNumSglSubj;
    private Integer stuNumMath;
    private Integer stuNumEng;
    private Integer stuNumChn;

    //X测沉默老学生
    private Integer silenceSettlementNumSglSubj;
    private Integer silenceSettlementNumEng;
    private Integer silenceSettlementNumMath;
    private Integer silenceSettlementNumChn;

    //统考参与人数
    private Integer xtestNumSglSubj; //  统考总数  （省、市、区）
    private Integer xtestNumEng;        //统考英语
    private Integer xtestNumChn;        //统考语文
    private Integer xtestNumMath;       //统考数学

    //未新增结算X测学生数
    private Integer newSettlementNumSglSubj;
    private Integer newSettlementNumEng;
    private Integer newSettlementNumMath;
    private Integer newSettlementNumChn;

    //结算X测学生数
    private Integer settleStuNumSglSubj;
    private Integer settleStuNumEng;
    private Integer settleStuNumMath;
    private Integer settleStuNumChn;

    //X测非沉默老学生
    private Integer unsilenceSettlementNumSglSubj;    //总数
    private Integer unsilenceSettlementNumEng;
    private Integer unsilenceSettlementNumMath;
    private Integer unsilenceSettlementNumChn;
}
