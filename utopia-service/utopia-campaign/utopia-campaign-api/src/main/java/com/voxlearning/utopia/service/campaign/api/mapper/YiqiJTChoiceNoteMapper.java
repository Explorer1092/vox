package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class YiqiJTChoiceNoteMapper implements java.io.Serializable {
    private static final long serialVersionUID = -1265676885288478120L;

    private Long id;
    private Long courseId;
    private String questionerUserName;
    private String questionerPictureUrl;
    private String question;
    private String answerUserName;
    private String answerPictureUrl;
    private String answer;
}
