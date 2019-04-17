package com.voxlearning.utopia.agent.bean.export;

import java.io.Serializable;
import java.util.List;

/**
 * 可用于导出的 暂且用于导出Excel
 * Created by yaguang.wang on 2017/2/15.
 */
public interface ExportAble extends Serializable {
    List<Object> getExportAbleData();
}
