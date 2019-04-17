package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.Setter;

/**
 * 阿分题飞船相关动态处理
 *
 * @author 陈司南
 * @since 2018/11/30
 */
@Getter
@Setter
public class AfentiSpacecraftHeadlineMapper extends StudentInteractiveHeadline {
    private static final long serialVersionUID = -7030077182875687765L;

    private String level;         // 等级
    private String appName;       // 应用名称
    private String contentText;       // 展示文本

    @Override
    public boolean valid() {
        return StringUtils.isNoneBlank(level, appName);
    }

    public void generateContentText() {
        Integer level = SafeConverter.toInt(this.level);
        if (level == 2 || level == 3) {
            contentText = StringUtils.formatMessage("飞船升级到{}级，快来{}和我一起遨游太空吧！", level, appName);
        } else if (level == 4 || level == 5) {
            contentText = StringUtils.formatMessage("飞船升级到{}级，快来{}和我一起探索宇宙吧！", level, appName);
        } else if (level == 6 || level == 7 || level == 8 || level == 9) {
            contentText = StringUtils.formatMessage("飞船升级到{}级，快来{}比比谁的飞船更炫酷！", level, appName);
        } else if (level == 10) {
            contentText = "飞船升级到10级，我已经找不到对手了，你行吗？";
        }

    }
}
