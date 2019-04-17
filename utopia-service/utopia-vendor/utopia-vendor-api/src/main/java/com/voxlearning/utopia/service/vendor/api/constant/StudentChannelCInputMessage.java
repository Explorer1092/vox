package com.voxlearning.utopia.service.vendor.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shiwe.liao
 * @since 2016-7-19
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum StudentChannelCInputMessage {
    SCHOOL(1, "学校", Boolean.TRUE),
    CLAZZ_LEVEL(2, "年级", Boolean.TRUE),
    BIRTHDAY(3, "出生年月", Boolean.TRUE),
    GENDER(4, "性别", Boolean.TRUE),
    NAME(5, "孩子姓名", Boolean.TRUE),
    MOBILE(6, "新手机号", Boolean.FALSE);

    private final int type;
    private final String desc;
    private final Boolean status;

    public static List<StudentChannelCInputMessage> getAllOnline() {
        List<StudentChannelCInputMessage> list = new ArrayList<>();
        for (StudentChannelCInputMessage inputMessage : StudentChannelCInputMessage.values()) {
            if (inputMessage.getStatus()) {
                list.add(inputMessage);
            }
        }
        return list;
    }
}
