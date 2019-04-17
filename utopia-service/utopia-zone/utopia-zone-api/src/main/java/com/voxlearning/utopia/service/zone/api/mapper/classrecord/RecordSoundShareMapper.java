package com.voxlearning.utopia.service.zone.api.mapper.classrecord;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/3/1
 * Time: 10:20
 * 获取记录留言分享
 */
@Getter
@Setter
public class RecordSoundShareMapper  extends RecordMapper implements Serializable {


    private static final long serialVersionUID = 419997287939960599L;

    private String uri;
    //录音时长
    private Long time;
    //作业id
    private String hwId;

}
