package com.voxlearning.utopia.service.afenti.api.afentichinese.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

import com.voxlearning.alps.spi.common.JsonStringDeserializer;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.afenti.api.afentichinese.response.TestResponse;
	
/**
 * 英文绘本首页信息
 * Test消息体
 * 
 * @author TemplateGenerator
 * @serial
 */
public class TestRequest implements Serializable {
	private static final long serialVersionUID = 0L;

	/** 测试数字 */
	public Integer testInt;
	/** 测试文字 */
	public String testString;
	/** 测试布尔 */
	public Boolean testBool;

    /**
     * 将请求过来的JSON字符串解析为类对象
     *
     * @param 请求过来的JSON字符串
     * @return 解析好的对象
     */
    @SuppressWarnings("unchecked")
    public static TestRequest parseRequest(String input) {
        TestRequest req = JsonStringDeserializer.getInstance().deserialize(input, TestRequest.class);
        if (null == req) {
            throw new NullPointerException();
        }
        return req;
    }
}