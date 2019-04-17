package com.voxlearning.utopia.schedule.support.userquery;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * @author changyuan.liu
 * @since 2017/8/3
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDataQueryStatement {
    private String whereSql;
    private Set<String> queryFields;
    private List<Object> params;

    public static UserDataQueryStatement build() {
        return new UserDataQueryStatement();
    }

    public UserDataQueryStatement where(String whereSql) {
        this.whereSql = whereSql;
        return this;
    }

    public UserDataQueryStatement queryFileds(String... queryFields) {
        this.queryFields = new HashSet<>(queryFields.length);
        this.queryFields.addAll(Arrays.asList(queryFields));
        return this;
    }

    public UserDataQueryStatement params(Object... params) {
        this.params = new ArrayList<>(params.length);
        this.params.addAll(Arrays.asList(params));
        return this;
    }
}
