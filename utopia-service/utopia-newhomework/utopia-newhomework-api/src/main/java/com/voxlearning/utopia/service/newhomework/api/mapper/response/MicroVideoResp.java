package com.voxlearning.utopia.service.newhomework.api.mapper.response;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.video.MicroVideoTask;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 视频课程
 * @author majianxin
 * @version V1.0
 * @date 2018/5/28
 */
@Getter
@Setter
public class MicroVideoResp extends BaseResp {

	private static final long serialVersionUID = 1566287234775729708L;
	private String id;				//微任务ID
	private String subjectId;		//学科ID
	private String name;			//微任务名称
	private String taskType;		//微任务类型
	private String imageUrl;		//微任务图片url
	private String url;				//微任务url
    private Integer threshold;      //阈值
    private Boolean draggable;      //是否允许拖动
    private String correctPhoneme;  //正确音
    private String confusablePhoneme;//易混音
	private List<MicroVideoTask.EmbedQuestion> questions;	//课程详情
}
