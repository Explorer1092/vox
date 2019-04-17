package com.voxlearning.utopia.service.afenti.api.afentichinese.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.Serializable;

import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.service.afenti.api.afentichinese.type.PlayerVO;
	
/**
 * 英文绘本首页信息
 * 
 * @author TemplateGenerator
 * @serial
 */
public class TestResponse implements Serializable {
	private static final long serialVersionUID = 0L;

	/** 用户信息 */
	public PlayerVO player;
	
	/**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}