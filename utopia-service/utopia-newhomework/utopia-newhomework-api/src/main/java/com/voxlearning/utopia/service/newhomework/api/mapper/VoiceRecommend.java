package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.BaseVoiceRecommend;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class VoiceRecommend extends BaseVoiceRecommend implements Serializable {
    private static final long serialVersionUID = -3851875398608638076L;

    private String id;                                  // 与作业id一致
    private Date createTime;                            // 记录创建时间
    private Date updateTime;                            // 记录更新时间
}
