package com.voxlearning.utopia.service.wechat.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by xinxin on 17/2/2016.
 */
@Getter
@Setter
public class BindResultMapper implements Serializable {
    private static final long serialVersionUID = -8425983320812027851L;

    public BindResultMapper(Long userId, Boolean runTask) {
        this.userId = userId;
        this.runTask = runTask;
    }

    private Long userId;
    private Boolean runTask;
}
