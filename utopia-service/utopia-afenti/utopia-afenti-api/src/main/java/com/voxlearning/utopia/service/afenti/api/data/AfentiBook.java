/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2014 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.afenti.api.data;

import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.builder.EqualsBuilder;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

public class AfentiBook implements Serializable {
    private static final long serialVersionUID = -6909587195425116388L;

    public NewBookProfile book;
    public Date createTime;
    public Long userId;
    public boolean active;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AfentiBook that = (AfentiBook) o;
        return new EqualsBuilder().append(userId, that.userId)
                .append(book.getId(), that.book.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(userId).append(book.getId()).toHashCode();
    }

    // Don't change this toString method, it's necessary to generate cache key.
    @Override
    public String toString() {
        return userId + "-" + book.getId();
    }

    public static void validate(AfentiBook afentiBook) {
        if (null == afentiBook || null == afentiBook.userId || null == afentiBook.book)
            throw new IllegalArgumentException("AFENTI book must not be null");
    }
}
