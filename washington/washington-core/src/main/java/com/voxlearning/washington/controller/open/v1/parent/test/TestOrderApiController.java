package com.voxlearning.washington.controller.open.v1.parent.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/1/25.
 */
public class TestOrderApiController {

    public static final String PARENT_APP_KEY = "17Parent";
    public static final String PARENT_SECRET_KEY = "iMMrxI3XMQtd";
    public static final String STUDENT_APP_KEY = "17Student";
    public static final String STUDENT_SECRET_KEY ="kuLwGZMJBcQj";
    public static final String imei = "355499060894099";

    public static void main(String[] args){

        orderTail();
    }

    public static void orderTail(){
        try {
            System.out.println("Test Start...");

            final String appKey = "FeeCourse";
            final String secretKey = "br47mVQasfSb";
            final String sessionKey = "ee4ab70f10dafb84f0149bd4136063d3";

//            String orderProductType = "afenti";
//            String oid = "453466136084714";
//            String orderProductType = "order";
//            String oid = "482824052495698_259851";


            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
//            paramMap.put("orderProductType", orderProductType);
//            paramMap.put("oid", oid);

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://192.168.179.1:8081/v2/order/products.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            System.out.println(post.execute().getResponseString(Charset.defaultCharset()));
            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
