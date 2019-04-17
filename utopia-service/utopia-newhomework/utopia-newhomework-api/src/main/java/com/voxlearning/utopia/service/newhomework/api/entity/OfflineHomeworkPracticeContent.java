package com.voxlearning.utopia.service.newhomework.api.entity;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.OfflineHomeworkContentType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author guoqiang.li
 * @since 2016/9/7
 */
@Getter
@Setter
public class OfflineHomeworkPracticeContent implements Serializable {
    private static final long serialVersionUID = 4276314668325233098L;

    private OfflineHomeworkContentType type;
    private Integer practiceCount;                  // 练习遍数
    private String bookId;                          // 课本id
    private String bookName;                        // 课本名称
    private String unitId;                          // 单元id
    private String unitName;                        // 单元名称
    private String customContent;                   // 自定义内容

    @Override
    public String toString() {
        if (type == null) {
            return "";
        }
        String message = "{}({})";
        if (type.isNeedCustomContent()) {
            return StringUtils.formatMessage(message, type.getDescription(), customContent);
        }
        return StringUtils.formatMessage(message, type.getDescription(), type.isNeedPracticeCount()
                ? unitName + "," + practiceCount + "遍" : unitName);
    }
}
