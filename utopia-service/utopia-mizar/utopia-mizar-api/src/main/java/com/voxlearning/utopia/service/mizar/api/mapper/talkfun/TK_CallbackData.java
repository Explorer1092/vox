package com.voxlearning.utopia.service.mizar.api.mapper.talkfun;

import lombok.Data;

import java.io.Serializable;

/**
 * 欢拓回调接口返回参数
 *
 * @author yuechen.wang 2017/01/10
 */
@Data
public class TK_CallbackData implements Serializable {

    private static final Long serialVersionUID = 1559355259683496377L;

    @TkField("course_id") private String courseId;         // 课程ID
    @TkField("title") private String title;                // 直播主题
    @TkField("liveid") private String liveId;              // 直播记录ID
    @TkField("bid") private String bid;                    // 主播账号
    @TkField("thirdAccount") private String thirdAccount;  // 第三方绑定账号
    @TkField("roomid") private String roomId;              // 房间ID
    @TkField("duration") private Integer duration;         // 直播时长
    @TkField("url") private String replay;                 // 直播时长
    @TkField(value = "startTime", timestamp = true) private String startTime;  // 直播开始时间
    @TkField(value = "endTime", timestamp = true) private String endTime;      // 直播结束时间
}
