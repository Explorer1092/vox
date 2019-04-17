package com.voxlearning.utopia.service.afenti.api.constant;

import lombok.Getter;

/**
 * @author peng.zhang.a
 * @since 2016/5/16
 */
@Getter
public enum AfentiPracticeAchievement {
    NUM_MIN_0(0,0,"继续努力"),
    NUM_1_9(1,9,"小有进步$值得鼓励$持续提高$坚持不懈"),
    NUM_10_50(10,50,"进步神速$聪明伶俐$融会贯通$快速提高$勤学苦练"),
    NUM_51_99(51,99,"出类拔萃$力争上游$孜孜不倦$突飞猛进"),
    NUM_100_MAX(100,Integer.MAX_VALUE,"名列榜首$独占鳌头"),
    NUM_ERROR(Integer.MIN_VALUE,-1,"没有称号");

    AfentiPracticeAchievement(Integer min, Integer max, String desc) {
        this.min = min;
        this.max = max;
        this.desc = desc;
    }
    private Integer min;
    private Integer max;
    private String desc;

    public static String getTitle(int num) {
        for (AfentiPracticeAchievement afentiPracticeAchievement : AfentiPracticeAchievement.values()) {
            if (num >= afentiPracticeAchievement.min && num <= afentiPracticeAchievement.max) {
                String cols[] = afentiPracticeAchievement.desc.split("[$]");
                int index = num % cols.length;
                return cols[index];
            }
        }
        return NUM_ERROR.desc;
    }
}
