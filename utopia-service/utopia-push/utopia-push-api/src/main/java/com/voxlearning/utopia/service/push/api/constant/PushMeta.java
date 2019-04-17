package com.voxlearning.utopia.service.push.api.constant;

import lombok.Getter;

/**
 * Created by wangshichao on 16/8/23.
 */

@Getter
public enum PushMeta {

    JG("https://bjapi.push.jiguang.cn/v3/push",
            "https://bjapi.push.jiguang.cn/v3/push",
            "https://bjapi.push.jiguang.cn/v3/report/received" + "?msg_ids=", ""),
    //    JG("https://api.jpush.cn/v3/push",
//            "https://api.jpush.cn/v3/push",
//            "https://report.jpush.cn/v3/received" + "?msg_ids=", ""),
    MI("https://api.xmpush.xiaomi.com/v2/message/multi_topic",
            "https://api.xmpush.xiaomi.com/v2/message/alias",
            "https://api.xmpush.xiaomi.com/v1/trace/message/status" + "?msg_id=",
            "com.yiqizuoye.jzt");
    private String topicUrl;
    private String batchSendUrl;
    private String traceUrl;
    private String packageName;

    PushMeta(String topicUrl, String batchSendUrl, String traceUrl, String packageName) {
        this.topicUrl = topicUrl;
        this.batchSendUrl = batchSendUrl;
        this.traceUrl = traceUrl;
        this.packageName = packageName;
    }

    public static PushMeta parse(String name) {
        PushMeta type;
        try {
            type = valueOf(name);
        } catch (Exception e) {
            return PushMeta.JG;
        }
        return type;
    }

}
