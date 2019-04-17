package com.voxlearning.utopia.agent.bean.export;

import java.io.IOException;

/**
 * Created by yaguang.wang
 * on 2017/3/29.
 */
public class DataIsEmptyException extends IOException {
    private static final long serialVersionUID = -5363673729356862737L;

    public DataIsEmptyException() {
        super("Export data is empty");
    }

}
