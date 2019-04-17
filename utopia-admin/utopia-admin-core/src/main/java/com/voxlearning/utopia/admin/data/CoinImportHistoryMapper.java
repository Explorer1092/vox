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
public class CoinImportHistoryMapper implements Serializable {
    private static final long serialVersionUID = 8918918621329866268L;

    private String opType;
    private String fileName;
    private String startDate;
    private String endDate;
    private String operator;
    private String url;
}
