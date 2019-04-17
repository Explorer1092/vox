package com.voxlearning.utopia.service.newhomework.api.mapper.response;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 轻交互课程
 * @author majianxin
 * @version V1.0
 * @date 2018/5/28
 */
@Getter
@Setter
public class LightInteractionCourseResp extends BaseResp {

	private static final long serialVersionUID = 8324727212002703238L;

	private String id;				//课程ID
	private String name;			//课程名称
	private List<CoursePage> pages;	//课程详情
	private Map<String, Object> theme;

	@Getter
	@Setter
	public static class CoursePage implements Serializable{

		private static final long serialVersionUID = -6709094864382815182L;

		private String id;
		private Integer pageRank;
		private String title;
		private String subtitle;
		private String feedbackText;
		private String feedbackAudioUrl;
		private Integer feedbackAudioDuration;

		private List<String> answer;
		private String audioUrl;
		private Integer audioDuration;

		private List<RowContent> contents;

		private List<RowOption> options;

		public CoursePage(EmbedPage embedPage) {
			this.id = embedPage.getId();
			this.pageRank = embedPage.getPageRank();
			this.title = embedPage.getTitle();
			this.subtitle = embedPage.getSubtitle();
			this.feedbackText = embedPage.getFeedbackText();
			EmbedAudioFile feedBackAudio = embedPage.getFeedbackAudio();
			if (feedBackAudio != null) {
				this.feedbackAudioUrl = feedBackAudio.getFileUrl();
				this.feedbackAudioDuration = feedBackAudio.getDuration();
			}
			this.answer = embedPage.getAnswers();
			EmbedAudioFile asideAudio = embedPage.getAsideAudio();
			if (asideAudio != null) {
				this.audioUrl = asideAudio.getFileUrl();
				this.audioDuration = asideAudio.getDuration();
			}
			this.contents = embedPage.getContents();
			this.options = embedPage.getOptionList();
		}
	}
}
