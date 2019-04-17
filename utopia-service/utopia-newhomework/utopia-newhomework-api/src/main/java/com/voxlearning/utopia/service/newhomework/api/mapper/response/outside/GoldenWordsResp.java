package com.voxlearning.utopia.service.newhomework.api.mapper.response.outside;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/11/16
 */
@Getter
@Setter
@AllArgsConstructor
public class GoldenWordsResp extends BaseResp {
    private static final long serialVersionUID = -973696216592853964L;

    private String goldenWordsId;        //好词好句收藏ID(studentId-bookId-missionId-goldenWordsIndex)
    private String goldenWordsContent;  //好词好句内容
    private String bookName;
    private Date createAt;
}
