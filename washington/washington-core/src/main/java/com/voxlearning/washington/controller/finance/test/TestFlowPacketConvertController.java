package com.voxlearning.washington.controller.finance.test;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;

/**
 * 测试流量包充值回调接口
 */
public class TestFlowPacketConvertController {

    private String prefix = "http://localhost:8081";

    public static void main(String[] args) {
        TestFlowPacketConvertController monitor = new TestFlowPacketConvertController();
//        System.out.println("加加流量测试开始...");
//        monitor.jjllTest();
//        System.out.println("加加流量测试完成...");

        System.out.println("安信捷流量测试开始...");
        monitor.axjTest();
        System.out.println("安信捷流量测试完成...");
    }

    private void jjllTest() {
        String jjllCallback = "/finance/flowpacket/callback.vpage?result={}&msg={}&transactionId={}&pass={}";

        String result = "";
        String msg = "";
        String transactionId = "";
        String pass = "";

        String reqUrl = prefix + StringUtils.formatMessage(jjllCallback, result, msg, transactionId, pass);

        System.out.println("Request Url : " + reqUrl);


        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .post(reqUrl).execute();

        System.out.println("Response : " + response.getResponseString());
    }

    private void axjTest() {
        String axjCallback = "/finance/flowpacket/deliver.vpage?seqnum={}&rcode={}";

        String seqnum = "";
        String rcode = "";

        String reqUrl = prefix + StringUtils.formatMessage(axjCallback, seqnum, rcode);

        System.out.println("Request Url : " + reqUrl);

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .post(reqUrl).execute();

        System.out.println("Response : " + response.getResponseString());
        System.out.println("Has NO-RESPONSE......");
    }

}
