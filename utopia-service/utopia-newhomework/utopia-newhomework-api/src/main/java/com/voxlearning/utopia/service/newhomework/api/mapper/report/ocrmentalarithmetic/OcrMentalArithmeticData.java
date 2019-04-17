package com.voxlearning.utopia.service.newhomework.api.mapper.report.ocrmentalarithmetic;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OcrMentalArithmeticData  implements Serializable {
    private static final long serialVersionUID = 6142842799376878352L;
    private String workBookName;
    private String homeworkDetail;
    private String resultUrl;
    private Integer score;
    private Boolean corrected;
}
