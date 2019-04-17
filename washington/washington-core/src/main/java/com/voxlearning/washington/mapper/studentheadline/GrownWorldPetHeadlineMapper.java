package com.voxlearning.washington.mapper.studentheadline;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 成长世界-宠物相关消息
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */
@Getter
@Setter
public class GrownWorldPetHeadlineMapper extends StudentInteractiveHeadline {
    private static final long serialVersionUID = 1506090387488768560L;

    private String petName;   // 伙伴名称
    private String petImage;  // 伙伴形象
    private String petRank;   // 伙伴等级

    @Override
    public boolean valid() {
        return StringUtils.isNotBlank(petImage);
    }
}
