package com.voxlearning.utopia.agent.mockexam.controller.view;

import lombok.Data;

import java.io.Serializable;

/**
 * 键值对
 *
 * @author xiaolei.li
 * @version 2018/8/16
 */
@Data
public class KeyValue<Key extends Serializable, Value extends Serializable> implements Serializable {
    private Key key;
    private Value value;
}
