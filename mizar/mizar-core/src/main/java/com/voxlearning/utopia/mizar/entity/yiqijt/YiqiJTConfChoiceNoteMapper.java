package com.voxlearning.utopia.mizar.entity.yiqijt;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class YiqiJTConfChoiceNoteMapper {
    private Long id;
    private Long courseId;
    private Long questionerUserId;
    private String questionerUserName;
    private String questionerPictureUrl;
    private String question;
    private Long answerUserId;
    private String answerUserName;
    private String answerPictureUrl;
    private String answer;
}
