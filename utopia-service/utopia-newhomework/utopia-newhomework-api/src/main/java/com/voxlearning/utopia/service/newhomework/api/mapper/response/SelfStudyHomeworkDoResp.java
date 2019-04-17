package com.voxlearning.utopia.service.newhomework.api.mapper.response;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.Getter;
import lombok.Setter;

/**
 * 订正作业Do接口resp
 * @author majianxin
 * @version V1.0
 * @date 2018/5/28
 */
@Getter
@Setter
public class SelfStudyHomeworkDoResp extends BaseResp {

	private static final long serialVersionUID = 2576015910021115692L;
	private String id;				//课程ID
	private String questionUrl;		//question接口
	private String completedUrl;	//question/answer接口
	private Boolean finished;       // 是否已做完

	private String expGroupId;       //实验组ID
	private String expId;            //实验ID
}
