package com.voxlearning.utopia.service.crm.api.bean;

import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 申请详情及处理结果
 *
 * @author song.wang
 * @date 2017/1/5
 */
@Getter
@Setter
public class ApplyWithProcessResultData implements Serializable{
    private static final long serialVersionUID = -2323916699277680118L;

    AbstractBaseApply apply; // 申请
    List<ApplyProcessResult> processResultList; // 申请的处理结果
}
