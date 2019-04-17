package com.voxlearning.wechat.service;

import java.util.Map;

/**
 * @author guangqing
 * @since 2018/8/6
 */
public interface DailyLessonService {

    /**
     * 今日学习录入时，预览数据接口
     * @param unitId
     * @return
     */
    Map<String, Object> buildDataMapForPreview(String bookId, String unitId);

    /**
     * 今日学习消息推送的数据接口
     * @param unitId
     * @return
     */
    Map<String, Object> buildDataMap(String bookId, Long clazzId,String unitId);
}
