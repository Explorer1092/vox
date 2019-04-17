package com.voxlearning.utopia.service.afenti.api.afentichinese.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.Serializable;

import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.service.afenti.api.afentichinese.type.ProfileVO;
	
/**
 * 用户信息
 * 
 * @author TemplateGenerator
 * @serial
 */
public class PlayerVO implements Serializable {
	private static final long serialVersionUID = 0L;

	/** 用户ID */
	public String user_id;
	/** 用户真实姓名 */
	public String real_name;
	/** 用户头像url */
	public String avatar_url;
	/** 用户个性化信息 */
	public ProfileVO profile;
	
	/**
     * 将当前对象转换成返回需要的字符串
     *
     * @return 返回字符串，通常是JSON
     */
    public String toResponse() {
        return JsonStringSerializer.getInstance().serialize(this);
    }
}