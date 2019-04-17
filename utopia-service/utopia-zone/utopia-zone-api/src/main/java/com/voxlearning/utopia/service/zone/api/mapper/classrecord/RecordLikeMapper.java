package com.voxlearning.utopia.service.zone.api.mapper.classrecord;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/3/1
 * Time: 10:20
 * 记录点赞
 */
@Getter
@Setter
public class RecordLikeMapper extends RecordMapper implements Serializable {

    private static final long serialVersionUID = 5014406810155647642L;
    private String text;
    private String homeworkId;

}
