package com.voxlearning.utopia.enanalyze.mq;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 业务消息
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
@AllArgsConstructor
public class Message implements Serializable {

    /**
     * 主题
     */
    private Topic topic;

    /**
     * 消息体
     */
    private String body;


}
