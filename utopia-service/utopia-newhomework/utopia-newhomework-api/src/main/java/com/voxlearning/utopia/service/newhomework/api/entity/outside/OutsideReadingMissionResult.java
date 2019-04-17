package com.voxlearning.utopia.service.newhomework.api.entity.outside;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;

@Getter
@Setter
public class OutsideReadingMissionResult implements Serializable {
    private static final long serialVersionUID = -7565397757335198570L;

    private LinkedHashMap<String, String> answers;  //<试题id, 答题明细id(outside_reading_process的id)>
    private Integer star;
    private Date finishAt;
    private Date updateAt;

    /**
     * 关卡是否完成
     * @return
     */
    @JsonIgnore
    public boolean isFinished() {
        return finishAt != null;
    }
}
