/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.utils;

import com.voxlearning.alps.core.util.IOUtils;
import com.voxlearning.alps.http.client.factory.HttpClientFactory;
import com.voxlearning.alps.lang.mapper.xml.XmlUtils;
import com.voxlearning.alps.random.RandomProvider;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.utopia.service.business.impl.support.Certification;
import com.voxlearning.utopia.service.wechat.api.constants.RedPackCategory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * Created by xiaopeng.yang on 2015/6/4.
 * 微信红包工具类
 * fixme 目前支持微信红包的发放，其他调用请注意。
 */
public class HongBaoUtils {
    //校园大使微信号配置
    private static final String AMBASSADOR_MCH_ID = "1244143702";      //商户号 校园大使微信号
    private static final String AMBASSADOR_WXAPPID = "wxee0a289557fba8c5";     //公众账号appid
    private static final String AMBASSADOR_NICK_NAME = "一起作业";   //提供方名称
    private static final String AMBASSADOR_SEND_NAME = "一起作业";   //商户名称
    private static final int AMBASSADOR_TOTAL_NUM = 1;         //红包发放人数
    private static final String AMBASSADOR_CLIENT_IP = "192.168.0.1";   //调用接口的机器IP
    private static final String AMBASSADOR_ACT_NAME = "大抽奖";    //活动名称
    private static final String AMBASSADOR_REMARK = "大抽奖";      //备注
    private static final String AMBASSADOR_KEY = "ec68cc2d2b81bfe6fee9978395fdf517";         //秘钥

    //老师微信号配置
    private static final String TEACHER_MCH_ID = "1233396702";      //商户号 校园大使微信号
    private static final String TEACHER_WXAPPID = "wx00f649e90a1737b1";     //公众账号appid
    private static final String TEACHER_NICK_NAME = "一起作业";   //提供方名称
    private static final String TEACHER_SEND_NAME = "一起作业";   //商户名称
    private static final int TEACHER_TOTAL_NUM = 1;         //红包发放人数
    private static final String TEACHER_CLIENT_IP = "192.168.0.1";   //调用接口的机器IP
    private static final String TEACHER_ACT_NAME = "大抽奖";    //活动名称
    private static final String TEACHER_REMARK = "大抽奖";      //备注
    private static final String TEACHER_KEY = "ec68cc2d2b81bfe6fee9978395fdf518";         //秘钥

    //薯条英语微信配置
    private static final String CHIPS_MCH_ID = "1503058101";      //商户号
    private static final String CHIPS_WXAPPID = "wx2785e95bc961cc3d";     //公众账号appid
    private static final String CHIPS_SEND_NAME = "薯条英语";   //商户名称
    private static final int CHIPS_TOTAL_NUM = 1;         //红包发放人数
    private static final String CHIPS_CLIENT_IP = "192.168.0.1";   //调用接口的机器IP
    private static final String CHIPS_ACT_NAME = "邀请发红包";    //活动名称
    private static final String CHIPS_REMARK = "邀请发红包";      //备注
    private static final String CHIPS_KEY = "jAR2QMsf8eI0gyxOB5aMTFMOEu7ejzr6";


    public static final String FAIL = "FAIL";              //返回失败
    public static final String SUCCESS = "SUCCESS";           //返回成功

    private static final String inputCharset = "UTF-8";

    /**
     * 对请求参数名ASCII码从小到大排序后签名
     */
    public static void sign(SortedMap<String, String> params) {
        Set<Map.Entry<String, String>> entrys = params.entrySet();
        Iterator<Map.Entry<String, String>> it = entrys.iterator();
        StringBuffer result = new StringBuffer();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            result.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
        }
        result.append("key=").append(AMBASSADOR_KEY);
        try {
            params.put("sign", DigestUtils.md5Hex(result.toString().getBytes(inputCharset)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对请求参数名ASCII码从小到大排序后签名
     */
    public static void teacherSign(SortedMap<String, String> params) {
        Set<Map.Entry<String, String>> entrys = params.entrySet();
        Iterator<Map.Entry<String, String>> it = entrys.iterator();
        StringBuffer result = new StringBuffer();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            result.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
        }
        result.append("key=").append(TEACHER_KEY);
        try {
            params.put("sign", DigestUtils.md5Hex(result.toString().getBytes(inputCharset)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void chipsSign(SortedMap<String, String> params) {
        Set<Map.Entry<String, String>> entrys = params.entrySet();
        Iterator<Map.Entry<String, String>> it = entrys.iterator();
        StringBuffer result = new StringBuffer();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            result.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
        }
        result.append("key=").append(CHIPS_KEY);
        try {
            params.put("sign", DigestUtils.md5Hex(result.toString().getBytes(inputCharset)));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成提交给微信服务器的xml格式参数
     */
    public static String getRequestXml(SortedMap<String, String> params) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = params.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if ("nick_name".equalsIgnoreCase(k) || "send_name".equalsIgnoreCase(k) || "wishing".equalsIgnoreCase(k) || "act_name".equalsIgnoreCase(k) || "remark".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
                sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
            } else {
                sb.append("<" + k + ">" + v + "</" + k + ">");
            }
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 创建map
     */
    public static SortedMap<String, String> createMap(String billNo, String openid, int amount, String wishing) {
        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("wxappid", AMBASSADOR_WXAPPID);
        params.put("nonce_str", createNonceStr());
        params.put("mch_billno", billNo);
        params.put("mch_id", AMBASSADOR_MCH_ID);
        params.put("nick_name", AMBASSADOR_NICK_NAME);
        params.put("send_name", AMBASSADOR_SEND_NAME);
        params.put("re_openid", openid);
        params.put("total_amount", amount + "");
        params.put("min_value", amount + "");
        params.put("max_value", amount + "");
        params.put("total_num", AMBASSADOR_TOTAL_NUM + "");
        params.put("wishing", wishing);
        params.put("client_ip", AMBASSADOR_CLIENT_IP);
        params.put("act_name", AMBASSADOR_ACT_NAME);
        params.put("remark", AMBASSADOR_REMARK);
        return params;
    }

    /**
     * 创建map
     */
    public static SortedMap<String, String> createTeacherMap(String billNo, String openid, int amount, String wishing) {
        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("wxappid", TEACHER_WXAPPID);
        params.put("nonce_str", createNonceStr());
        params.put("mch_billno", billNo);
        params.put("mch_id", TEACHER_MCH_ID);
        params.put("nick_name", TEACHER_NICK_NAME);
        params.put("send_name", TEACHER_SEND_NAME);
        params.put("re_openid", openid);
        params.put("total_amount", amount + "");
        params.put("min_value", amount + "");
        params.put("max_value", amount + "");
        params.put("total_num", TEACHER_TOTAL_NUM + "");
        params.put("wishing", wishing);
        params.put("client_ip", TEACHER_CLIENT_IP);
        params.put("act_name", TEACHER_ACT_NAME);
        params.put("remark", TEACHER_REMARK);
        return params;
    }

    public static SortedMap<String, String> createChipsMap(String billNo, String openid, int amount, String wishing) {
        SortedMap<String, String> params = new TreeMap<>();
        params.put("wxappid", CHIPS_WXAPPID);
        params.put("nonce_str", createNonceStr());
        params.put("mch_billno", billNo);
        params.put("mch_id", CHIPS_MCH_ID);
        params.put("send_name", CHIPS_SEND_NAME);
        params.put("re_openid", openid);
        params.put("total_amount", amount + "");
        params.put("total_num", CHIPS_TOTAL_NUM + "");
        params.put("wishing", wishing);
        params.put("client_ip", CHIPS_CLIENT_IP);
        params.put("act_name", CHIPS_ACT_NAME);
        params.put("remark", CHIPS_REMARK);
        return params;
    }

    /**
     * 生成随机字符串
     */
    public static String createNonceStr() {
        return UUID.randomUUID().toString().toUpperCase().replace("-", "");
    }

    /**
     * 生成商户订单号
     */
    public static String createBillNo(String userId) {
        //组成： mch_id+yyyymmdd+10位一天内不能重复的数字
        //10位一天内不能重复的数字实现方法如下:
        //因为每个用户绑定了userId,他们的userId不同,加上随机生成的(10-length(userId))可保证这10位数字不一样
        Date dt = new Date();
        FastDateFormat df = FastDateFormat.getInstance("yyyymmdd");
        String nowTime = df.format(dt);
        int length = 10 - userId.length();
        return AMBASSADOR_MCH_ID + nowTime + userId + getRandomNum(length);
    }

    /**
     * 生成商户订单号
     */
    public static String createTeacherBillNo(String userId) {
        //组成： mch_id+yyyymmdd+10位一天内不能重复的数字
        //10位一天内不能重复的数字实现方法如下:
        //因为每个用户绑定了userId,他们的userId不同,加上随机生成的(10-length(userId))可保证这10位数字不一样
        Date dt = new Date();
        FastDateFormat df = FastDateFormat.getInstance("yyyymmdd");
        String nowTime = df.format(dt);
        int length = 10 - userId.length();
        return TEACHER_MCH_ID + nowTime + userId + getRandomNum(length);
    }

    public static String createChipsBillNo(String userId) {
        //组成： mch_id+yyyymmdd+10位一天内不能重复的数字
        //10位一天内不能重复的数字实现方法如下:
        //因为每个用户绑定了userId,他们的userId不同,加上随机生成的(10-length(userId))可保证这10位数字不一样
        Date dt = new Date();
        FastDateFormat df = FastDateFormat.getInstance("yyyymmdd");
        String nowTime = df.format(dt);
        int length = 10 - userId.length();
        return CHIPS_MCH_ID + nowTime + userId + getRandomNum(length);
    }
    /**
     * 生成特定位数的随机数字
     */
    private static String getRandomNum(int length) {
        String val = "";
        Random random = RandomProvider.getInstance().getRandom();
        for (int i = 0; i < length; i++) {
            val += String.valueOf(random.nextInt(10));
        }
        return val;
    }

    /**
     * post提交到微信服务器
     */
    public static String post(String requestXML, InputStream instream) throws NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try {
            keyStore.load(instream, AMBASSADOR_MCH_ID.toCharArray());
        } finally {
            instream.close();
        }
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, AMBASSADOR_MCH_ID.toCharArray()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        HttpClientBuilder builder = HttpClients.custom().setSSLSocketFactory(sslsf);

        String result = "";
        HttpClient httpClient = null;
        HttpResponse response = null;
        try {
            // Use HttpClientFactory to build HttpClient (monitor-able)
            httpClient = HttpClientFactory.instance().build(builder);
            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack");
            StringEntity reqEntity = new StringEntity(requestXML, "utf-8"); //如果此处编码不对，可能导致客户端签名跟微信的签名不一致
            reqEntity.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(reqEntity);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
                String text;
                while ((text = bufferedReader.readLine()) != null) {
                    result += text;
                }
            }
            EntityUtils.consume(entity);
        } finally {
            if (response instanceof Closeable) {
                IOUtils.closeQuietly((Closeable) response);
            }
            HttpClientFactory.instance().destroy(httpClient);
        }
        return result;
    }

    /**
     * post提交到微信服务器
     */
    public static String postTeacher(String requestXML, InputStream instream) throws NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try {
            keyStore.load(instream, TEACHER_MCH_ID.toCharArray());
        } finally {
            instream.close();
        }
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, TEACHER_MCH_ID.toCharArray()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        HttpClientBuilder builder = HttpClients.custom().setSSLSocketFactory(sslsf);

        String result = "";
        HttpClient httpClient = null;
        HttpResponse response = null;
        try {
            // Use HttpClientFactory to build HttpClient (monitor-able)
            httpClient = HttpClientFactory.instance().build(builder);

            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack");
            StringEntity reqEntity = new StringEntity(requestXML, "utf-8"); //如果此处编码不对，可能导致客户端签名跟微信的签名不一致
            reqEntity.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(reqEntity);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
                String text;
                while ((text = bufferedReader.readLine()) != null) {
                    result += text;
                }
            }
            EntityUtils.consume(entity);
        } finally {
            if (response instanceof Closeable) {
                IOUtils.closeQuietly((Closeable) response);
            }
            HttpClientFactory.instance().destroy(httpClient);
        }
        return result;
    }

    public static void main(String[] args) throws FileNotFoundException {
        SortedMap<String, String> sortedMap = createTeacherMap(createTeacherBillNo("1585006"), "okHWijimu2QM5QX0AU57SJynOFPc", 200, RedPackCategory.TEACHER_TERM_BEGIN_LOTTERY.getWishingText());
        //设置签名
        teacherSign(sortedMap);
        try {
            String result = postTeacher(getRequestXml(sortedMap), Certification.openTeacherCertificationInputStream());
            Document document = XmlUtils.parseDocument(new ByteArrayInputStream(result.getBytes()));
            String return_code = XmlUtils.getChildElementText(document.getDocumentElement(), "return_code");
            String return_msg = XmlUtils.getChildElementText(document.getDocumentElement(), "return_msg");
            if (SUCCESS.equals(return_code)) {
                String result_code = XmlUtils.getChildElementText(document.getDocumentElement(), "result_code");
                String err_code = XmlUtils.getChildElementText(document.getDocumentElement(), "err_code");
                String err_code_des = XmlUtils.getChildElementText(document.getDocumentElement(), "err_code_des");
                if (SUCCESS.equals(result_code)) {
                    System.out.println("发送成功");
                } else {
                    System.out.println("发送失败, 原因：" + err_code_des + ",code:" + err_code);
                }
            } else {
                System.out.println(return_msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}