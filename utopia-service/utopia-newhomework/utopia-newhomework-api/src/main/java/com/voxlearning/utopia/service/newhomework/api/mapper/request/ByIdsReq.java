package com.voxlearning.utopia.service.newhomework.api.mapper.request;

import com.voxlearning.utopia.service.newhomework.api.mapper.request.base.BaseReq;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/7/18
 */
@Getter
@Setter
public class ByIdsReq extends BaseReq {
    private static final long serialVersionUID = 8788491290884314990L;

    public Collection<String> ids;      //请求ids
}
