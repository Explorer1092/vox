package com.voxlearning.utopia.admin.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author malong
 * @since 2018/06/02
 */
@Getter
@Setter
public class CoinHistoryMapper implements Serializable {
    private static final long serialVersionUID = 8342467768446281702L;
    private String coinType;
    private String count;
    private String createTime;
    private String operator;
}
