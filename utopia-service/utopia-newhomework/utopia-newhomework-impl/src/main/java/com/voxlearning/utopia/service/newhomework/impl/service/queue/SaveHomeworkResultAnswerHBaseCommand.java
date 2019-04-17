package com.voxlearning.utopia.service.newhomework.impl.service.queue;

import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultAnswer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/8/21
 */
@Getter
@Setter
public class SaveHomeworkResultAnswerHBaseCommand implements Serializable {

    private static final long serialVersionUID = -985364214616668851L;

    private List<SubHomeworkResultAnswer> results;

    /**
     * 这个属性很重要，因为并不能保证先生产的数据，一定先消费
     * 所以这个时间在生产的时候赋值，由此时间在HBase中确定先后顺序
     */
    private long currentTime;
}
