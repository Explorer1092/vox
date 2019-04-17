package com.voxlearning.utopia.service.mizar.api.mapper.talkfun;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 欢拓课程统计数据
 *
 * @author yuechen.wang 2017/01/10
 */
@Data
public class TK_CourseReport implements Serializable {

    private static final Long serialVersionUID = 1559355259683496377L;

    @TkField("id") private String recordId;                  // 记录ID
    @TkField("roomid") private String roomId;                // 房间ID
    @TkField("uid") private String userId;                   // 用户ID
    @TkField("join_time") private String joinTime;           // 进入房间的时间
    @TkField("leave_time") private String leaveTime;         // 离开房间的时间
    @TkField("ip_address") private String ip;                // ip地址
    @TkField("location") private String location;            // 用户地理位置
    @TkField("os") private String os;                        // 终端类型
    @TkField("useragent") private String userAgent;          // 浏览器
    @TkField("duration") private Integer duration;           // 用户停留的时间(单位：秒)
    @TkField("duration_time") private String durationTime;   // 用户停留时间(时:分:秒)
    @TkField("terminal") private Integer tCode;              // 用户终端类型：TalkFunTerminal
    private TalkFunTerminal terminal;


    public String fetchTerminal() {
        return TalkFunTerminal.parse(tCode).getDesc();
    }

    // 枚举为：1:Windows,2:Mac OS,3:IOS,4:Android,5:Linux,10:其它
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    enum TalkFunTerminal {
        Windows(1, "Windows"),
        Mac_OS(2, "Mac OS"),
        iOS(3, "iOS"),
        Android(4, "Android"),
        Linux(5, "Linux"),
        Others(10, "其他");

        @Getter private final int code;
        @Getter private final String desc;


        private static final Map<Integer, TalkFunTerminal> codeMap;

        static {
            codeMap = Stream.of(values()).collect(Collectors.toMap(TalkFunTerminal::getCode, Function.identity()));
        }

        public static TalkFunTerminal parse(int code) {
            if (codeMap.containsKey(code)) {
                return codeMap.get(code);
            }
            return Others;
        }
    }

}
