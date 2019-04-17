package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Deprecated
public class UserExamEnWrongContent implements Serializable {

    private static final long serialVersionUID = 5037963447487289156L;

    private Long userId;
    private int ver;
    /** 历史错题列表 */
    private List<UserExamEnWrongItem> wrongW2WList;
    /** 历史错题验证列表 */
    private List<UserExamEnWrongItem> wrongW2RList;
}

