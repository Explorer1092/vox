package com.voxlearning.utopia.service.ai.impl.context;

import com.voxlearning.utopia.service.ai.context.AbstractAIContext;
import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTask;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ChipsDrawingTaskLoadContext extends AbstractAIContext<ChipsDrawingTaskLoadContext> {

    private static final long serialVersionUID = 1L;
    //in
    private Long userId;
    private String labelCode;
    private int page;
    private int pageSize;

    //middle
    private List<String> bookList;
    private List<ChipsUserDrawingTask> userDrawingTasks;


    //out
    private int totalPage = 1;
    private List<Map<String, String>> labelList = new ArrayList<>();
    private List<Map<String, Object>> drawingList = new ArrayList<>();
    private List<Map<String, Object>> popData = new ArrayList<>();

    public ChipsDrawingTaskLoadContext(Long userId, String labelCode, int page, int pageSize) {
        this.setUserId(userId);
        this.setLabelCode(labelCode);
        this.setPage(page);
        this.setPageSize(pageSize);
    }
}
