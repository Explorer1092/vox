package com.voxlearning.utopia.agent.service.table;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.agent.bean.DataTableInfo;
import com.voxlearning.utopia.agent.bean.showtable.ShowTableInfoService;
import com.voxlearning.utopia.agent.bean.showtable.TableShowAble;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaguang.wang
 * on 2017/3/28.
 */
@Setter
@Getter
public class ShowTableInfoServiceImpl implements ShowTableInfoService {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private List<TableShowAble> tableLines;

    public ShowTableInfoServiceImpl(List lines) {
        tableLines = lines;
    }

    @Override
    public String tableInfoToJson() {
        if (tableLines == null) {
            return null;
        }
        DataTableInfo dataTableInfo = new DataTableInfo();
        if (tableLines.size() > 0) {
            dataTableInfo.setSuccess(true);
            dataTableInfo.setDraw(1);
            dataTableInfo.setRecordsFiltered(tableLines.size());
            dataTableInfo.setRecordsTotal(tableLines.size());
            List<List<Object>> dataContentList = new ArrayList<>();
            tableLines.forEach(p -> dataContentList.add(p.getShowTableInfo()));
            dataTableInfo.setData(dataContentList);
        } else {
            dataTableInfo.setSuccess(false);
            dataTableInfo.setDraw(1);
            dataTableInfo.setInfo("未找到相关数据");
        }
        return JsonUtils.toJson(dataTableInfo);
    }
}
