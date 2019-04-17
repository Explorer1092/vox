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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.builder.EqualsBuilder;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.builder.HashCodeBuilder;
import com.voxlearning.utopia.service.user.api.entities.User;

public class AfentiUser extends User {
    private static final long serialVersionUID = -5863604231325326737L;

    public AfentiBook afentiBook;
    public AfentiRank afentiRank;
    public Subject subject;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AfentiUser that = (AfentiUser) o;
        return new EqualsBuilder().append(getId(), that.getId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
    }

    // Don't change this toString method, it's necessary to generate cache key.
    @Override
    public String toString() {
        return String.valueOf(getId());
    }

    public static void validate(AfentiUser user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("AFENTI user must not be null");
        }
    }

    /**
     * Static factory method for building AfentiUser object from specified user id.
     */
    public static AfentiUser newInstance(Long userId, Subject subject) {
        if (userId == null || subject == null || subject == Subject.UNKNOWN) return null;

        AfentiUser afentiUser = new AfentiUser();
        afentiUser.setId(userId);
        afentiUser.setUserType(UserType.STUDENT.getType()); // default user type is STUDENT
        afentiUser.afentiBook = null;
        afentiUser.afentiRank = null;
        afentiUser.subject = subject;
        return afentiUser;
    }


    public User depreactedNarrow() {
        User u = new User();
        u.setId(getId());
        u.setUserType(getUserType());
        return u;
    }

}
