package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.random.RandomUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 点读报告收集数据
 *
 * @author jiangpeng
 * @since 2017-03-15 下午3:41
 **/
@Getter
@Setter
public class PicListenCollectData implements Serializable {
    private static final long serialVersionUID = -1473934366793856041L;

    private Long studentId;
    private String dayRange;
    private List<SentenceResult> sentenceResultList;

    public PicListenCollectData(){}

    public PicListenCollectData(Long studentId, DayRange dayRange){
        this.studentId = studentId;
        this.dayRange = dayRange.toString();
    }


    @Getter
    @Setter
    public static class SentenceResult implements Serializable {
        private static final long serialVersionUID = 6963905065902484732L;

        private String bookId;
        private String unitId;
        private String sentenceId;  //句子id。因为有的没有句子id,用ObjectId代替,所以用String型
        private Long time;   //读的时间, 毫秒



        public static SentenceResult newInstance(String sentenceId) {
            SentenceResult o = new SentenceResult();
            o.sentenceId = sentenceId;
            o.time = 0L;
            return o;
        }
    }

    private static Integer MAX_TIME = 1800000;
    private static Integer MAX_SENTENCE = 500;

    public PicListenCollectData union(){
        if (CollectionUtils.isEmpty(sentenceResultList))
            return this;
        long maxSentenceCount = RandomUtils.nextInt(0, 50) + MAX_SENTENCE;
        long sentenceCounter = 0;
        long timeCounter = 0L ;
        Map<String, SentenceResult> sentenceResultMap = new HashMap<>();
        for (SentenceResult sentenceResult : sentenceResultList) {
            if (sentenceCounter >= maxSentenceCount || timeCounter >= MAX_TIME)
                break;
            String sentenceId = sentenceResult.getSentenceId();
            if (StringUtils.isBlank(sentenceId) || sentenceId.equals("0"))
                sentenceId = RandomUtils.nextObjectId();
            SentenceResult sr = sentenceResultMap.get(sentenceId);
            if (sr == null)
                sr = SentenceResult.newInstance(sentenceId);
            long time = sr.getTime() + sentenceResult.getTime();
            sr.setTime(time);
            timeCounter = timeCounter + time;
            SentenceResult old = sentenceResultMap.put(sentenceId, sr);
            if (old == null)
                sentenceCounter ++;
        }
        this.sentenceResultList = new ArrayList<>(sentenceResultMap.values());
        return this;
    }
}
