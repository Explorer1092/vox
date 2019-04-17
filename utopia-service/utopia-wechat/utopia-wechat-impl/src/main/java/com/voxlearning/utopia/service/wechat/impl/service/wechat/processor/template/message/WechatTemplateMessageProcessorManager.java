package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Named
public class WechatTemplateMessageProcessorManager {
    private final Map<WechatTemplateMessageType, WechatTemplateMessageProcessor> processors;

    public WechatTemplateMessageProcessorManager() {
        processors = Collections.synchronizedMap(new HashMap<>());
    }

    protected void register(WechatTemplateMessageProcessor processor) {
        processors.put(processor.type(), processor);
    }

    public WechatTemplateMessageProcessor get(WechatTemplateMessageType type) {
        return processors.get(type);
    }
//    private static String URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
//    public static void main(String[] args) {
//        Map<String, Object> params = new HashMap<>();
//
//        Map<String, Object> dataMap = new HashMap<>();
//        dataMap.put("first", new DataValue("2018-01-01"));
//        dataMap.put("keyword1", new DataValue("2018-01-01"));
//        dataMap.put("keyword2", new DataValue("2018-01-01"));
//        dataMap.put("remark", new DataValue("2018-01-01"));
//
//        params.put("data", dataMap);
//        params.put("touser", "ox6gFs3M1RGbpNYsI3zmZhKRbGec");
//        params.put("template_id", "j16Jei2LrLNYbyMusT32AegF5QvaI02Dy0-HOuFmAcw");
//        params.put("url", "https://wechat.test.17zuoye.net/chips/center/myteacher.vpage");
//        String response = HttpRequestExecutor.defaultInstance()
//                .post(URL + "11_hn9IbMKq7tOXan8EeiqTxYTxfhfD0E45S36ZOVcNEK8brY4KEJs6XXPFkmxZtQenNtGUaOkMjThu8xs3_Sm0-pbG1BtVYSkHNWVNhf_XGVsorboOKqeIr2W_GGjcLBJvVjZbve07pKlKb_toGAHdADAYSZ")
//                .json(params)
//                .execute().getResponseString();
//        System.out.println(response);
//
//    }
//    @Setter
//    @Getter
//    public static class DataValue implements Serializable {
//        private static final long serialVersionUID = -2516471208766637997L;
//        private String value;
//        private String color;
//        public DataValue() {
//            this.value = "";
//            this.color = null;
//        }
//        public DataValue(String value) {
//            this.value = value;
//            this.color = null;
//        }
//    }
}
