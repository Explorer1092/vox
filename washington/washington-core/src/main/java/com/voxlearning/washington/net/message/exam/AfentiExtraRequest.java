package com.voxlearning.washington.net.message.exam;

import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import lombok.Data;

import java.io.Serializable;

@Data
public class AfentiExtraRequest implements Serializable {


    private static final long serialVersionUID = 8388236104563275811L;
    private String bookId;           //课本ID
    private String unitId;           //单元ID
    private Integer rank;            //关卡
    private AfentiLearningType learningType; // afenti 类型 区分城堡和预习
    private AfentiState afentiState; //因子工厂用到
    private int successiveSilver;    //连对奖励
    private String originQuestionId; // 做类题的原题,因子工厂用到
    private String scoreCoefficient; // 算法权重，用于数据上报

    private Integer unitRank;        //单元排序
    private boolean skipped;         //是否跳过, 因子工厂用到
}
