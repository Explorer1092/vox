package com.voxlearning.utopia.service.vendor.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author jiangpeng
 * @since 2017-03-17 下午1:53
 **/
@Getter
@Setter
public class PicListenReportConfig implements Serializable {
    private static final long serialVersionUID = 86233285800190478L;

    private ScoreParam learnTimeScoreParam;

    private ScoreParam playSentenceCountScoreParam;

    private ScoreParam followReadSentenceCountScoreParam;

    private ScoreParam reportScoreParam;


    @Getter
    @Setter
    public class ScoreParam implements Serializable {
        private static final long serialVersionUID = -57881422323433012L;
        private Long max;   //最高量
        private Long standard; //标准达标量 , 同时也是偏低&良好分界线
        private Long good; // 良好&优秀分界线
        private Long totalScore;  //总分
        private Long standardScore; //标准达标分
    }

}
