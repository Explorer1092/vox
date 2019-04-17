package com.voxlearning.enanalyze.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除组的请求参数
 *
 * @author xiaolei.li
 * @version 2018/8/10
 */
@Data
public class GroupRemoveRequest implements Serializable {

    /**
     * openGroupId
     */
    private String openGroupId;
}
