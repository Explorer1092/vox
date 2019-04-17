/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.builder.EqualsBuilder;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.builder.HashCodeBuilder;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;

import java.io.Serializable;

/**
 * 用于定位关卡，由userId，bookId，unitId，rank和examId组成
 */
public class AfentiRank implements Serializable {
    private static final long serialVersionUID = -6422295794150823634L;

    public Long userId;
    public String newBookId;
    public String newUnitId;
    public Integer rank;

    @JsonIgnore
    public boolean isUltimateUnit() {
        return UtopiaAfentiConstants.isUltimateUnit(newUnitId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AfentiRank that = (AfentiRank) o;
        return new EqualsBuilder()
                .append(userId, that.userId)
                .append(newBookId, that.newBookId)
                .append(newUnitId, that.newUnitId)
                .append(rank, that.rank).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(userId)
                .append(newBookId)
                .append(newUnitId)
                .append(rank).toHashCode();
    }

    public static void validate(AfentiRank afentiRank) {
        if (afentiRank == null) {
            throw new IllegalArgumentException("AfentiRank must not be null");
        }
        if (afentiRank.userId == null) {
            throw new IllegalArgumentException("User id must not be null");
        }
        if (StringUtils.isBlank(afentiRank.newBookId)) {
            throw new IllegalArgumentException("Book id must not be null");
        }
        if (StringUtils.isBlank(afentiRank.newUnitId)) {
            throw new IllegalArgumentException("Unit id must not be null");
        }
        if (afentiRank.rank == null) {
            throw new IllegalArgumentException("Rank must not be null");
        }
    }
}
