package com.voxlearning.washington.data.utils;

import lombok.Data;

import java.io.Serializable;

/**
 * 键值对
 */
@Data
public class KeyValue<Key extends Serializable, Value extends Serializable> implements Serializable {
    private Key key;
    private Value value;
}
