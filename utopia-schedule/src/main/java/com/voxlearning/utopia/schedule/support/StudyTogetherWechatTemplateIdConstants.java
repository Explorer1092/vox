package com.voxlearning.utopia.schedule.support;

import com.voxlearning.alps.runtime.RuntimeMode;

/**
 * @author zhiqian.ren
 * @since 2018-10-10
 **/
public class StudyTogetherWechatTemplateIdConstants {

    public static String lessonRemindTemplateId = "";

    static {
        if (RuntimeMode.isProduction()){
            lessonRemindTemplateId = "mtRgKUJ6l-402QmY1a167bFmsHwbcAeC13PORWQA6PM";
        }else if (RuntimeMode.isStaging()) {
            lessonRemindTemplateId = "KtpLVlL3IGFNK53xvUIWAeybcWks6U-hcjoUUAgKMDA";
        }if (RuntimeMode.isUsingTestData()){
            lessonRemindTemplateId = "JAwfhePsRKir4YR-qP9rM-Ix6OrglpKgZDXgyJItDP8";
        }
    }
}
