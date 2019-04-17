package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2015/10/15
 */
@Getter
@Setter
@NoArgsConstructor
public class UserWorkRecordSummary implements Serializable, Comparable {

    private Long userId;
    private String userName;
    private int count;

    public UserWorkRecordSummary(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public void increase() {
        this.count++;
    }

    @Override
    public int compareTo(Object other) {
        if (other == null || !(other instanceof UserWorkRecordSummary)) {
            return -1;
        }
        UserWorkRecordSummary bean = (UserWorkRecordSummary) other;
        return bean.count - this.count;
    }
}
