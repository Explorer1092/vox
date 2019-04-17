package com.voxlearning.utopia.service.vendor.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author jiangpeng
 * @since 2017-03-16 下午7:41
 **/
@Getter
@Setter
public class PicListenReportDayResult implements Serializable {
    private static final long serialVersionUID = -4330622737082473776L;


    private Long learnTime;   //学习时长 毫秒数

    private Long playSentenceCount;   //播放句子时长

    private Long followReadSentenceCount;   //跟读时长

    private Long reportScore;    //总和得分

    private Boolean hasFollowRead;   //是否有点读




    @JsonIgnore
    public Boolean ifHasFollowRead(){
        return SafeConverter.toBoolean(hasFollowRead);
    }

    public static PicListenReportDayResult emptyResult() {
        PicListenReportDayResult reportDayResult = new PicListenReportDayResult();
        reportDayResult.setLearnTime(0L);
        reportDayResult.setPlaySentenceCount(0L);
        reportDayResult.setFollowReadSentenceCount(0L);
        reportDayResult.setReportScore(0L);
        reportDayResult.setHasFollowRead(false);
        return reportDayResult;
    }

    @Getter
    @Setter
    public static class DayScoreMapper implements Serializable{
        private static final long serialVersionUID = -6469673376173013006L;
        private DayRange day;
        private Long score;


        public static class DayRangeCompartor implements Comparator<DayScoreMapper>{

            @Override
            public int compare(DayScoreMapper o1, DayScoreMapper o2) {
                return o1.getDay().toString().compareTo(o2.getDay().toString());
            }
        }
    }

}
