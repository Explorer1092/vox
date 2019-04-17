/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.schedule.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author RuiBao
 * @version 0.1
 * @since 5/14/2015
 */
@Named
public class TeacherAuthFollowupRewardHelper extends SpringContainerSupport {
    private static final Map<Integer, Integer> PHASE_PHONEFEE_MAP = new HashMap<>();

    static {
        PHASE_PHONEFEE_MAP.put(0, 0);
        PHASE_PHONEFEE_MAP.put(1, 5);
        PHASE_PHONEFEE_MAP.put(2, 10);
    }

    public int getExceptedPhase(int count) {
        int expectedPhase;
        if (RuntimeMode.le(Mode.STAGING)) {
            if (count < 9) {
                expectedPhase = 0;
            } else if (count >= 9 && count < 10) {
                expectedPhase = 1;
            } else {
                expectedPhase = 2;
            }
            return expectedPhase;
        }
        if (count < 30) {
            expectedPhase = 0;
        } else if (count >= 30 && count < 90) {
            expectedPhase = 1;
        } else {
            expectedPhase = 2;
        }
        return expectedPhase;
    }

    public int getExceptedPhoneFeeToAdd(int current, int expected, boolean wechatBinded) {
        if (current >= expected) return 0;

        int amount = 0;
        if (PHASE_PHONEFEE_MAP.containsKey(current) && PHASE_PHONEFEE_MAP.containsKey(expected)) {
            for (int i = current + 1; i <= expected; i++) {
                amount += PHASE_PHONEFEE_MAP.get(i);
            }
        }
        return wechatBinded ? amount * 2 : amount;
    }

    public Map<String, Integer> getRewardDetail(int current, int expected, boolean wechatBinded) {
        if (current >= expected) return Collections.emptyMap();

        if (!PHASE_PHONEFEE_MAP.containsKey(current) || !PHASE_PHONEFEE_MAP.containsKey(expected)) {
            return Collections.emptyMap();
        }
        Map<String, Integer> result = new HashMap<>();
        while (current < expected) {
            String key = "" + current + "-" + ++current;
            Integer value = PHASE_PHONEFEE_MAP.get(current);
            result.put(key, wechatBinded ? value * 2 : value);
        }
        return result;
    }
}
