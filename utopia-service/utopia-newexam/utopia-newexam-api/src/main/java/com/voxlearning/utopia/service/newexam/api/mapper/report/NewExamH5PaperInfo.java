package com.voxlearning.utopia.service.newexam.api.mapper.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class NewExamH5PaperInfo implements Serializable {
    private static final long serialVersionUID = 3590034229166910121L;
    private String paperId;
    private String paperName;
}
