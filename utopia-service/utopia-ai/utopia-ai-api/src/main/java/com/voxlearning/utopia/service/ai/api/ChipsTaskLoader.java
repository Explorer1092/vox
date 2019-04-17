package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190403")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsTaskLoader extends IPingable {

    MapMessage loadMyDrawingTask(Long userId, Long drawingTaskId);

    MapMessage loadDrawingTabList(Long userId, String labelCode, int page, int pageSize);

    MapMessage loadDrawingTask(Long drawingTaskId);

    MapMessage loadDrawingShareInfo(Long drawingTaskId);

    MapMessage loadUserPageRedDot(Long userId, String pageCode);

    List<List<String>> calUserUnitEngeryByBook(List<Long> userList, String bookId);

    MapMessage loadUserDrawingInfo(Long userId, String openId);
}

