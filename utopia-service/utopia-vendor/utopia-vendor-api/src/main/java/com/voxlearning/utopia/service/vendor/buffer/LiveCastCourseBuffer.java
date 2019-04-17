package com.voxlearning.utopia.service.vendor.buffer;

import com.voxlearning.alps.api.buffer.NearBuffer;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastCourse;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2018-09-21 下午7:24
 **/
public class LiveCastCourseBuffer extends NearBuffer<List<LiveCastCourse>> {

    @Getter
    private final Map<String, LiveCastCourse> map = new HashMap<>();

    @Override
    public int estimateSize() {
        return dump().getData().size();
    }

    @Override
    protected void doAttachDataUnderWriteLock(List<LiveCastCourse> data) {
        map.clear();
        data.forEach(p -> map.put(p.getCourseId(), p));
    }
}
