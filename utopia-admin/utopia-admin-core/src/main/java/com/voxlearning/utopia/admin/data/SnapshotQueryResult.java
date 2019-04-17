package com.voxlearning.utopia.admin.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xinxin
 * @since 6/14/17.
 */
@Getter
@Setter
public class SnapshotQueryResult implements Serializable {
    private static final long serialVersionUID = 8941939493148588855L;

    private String state;
    private List<String> snapshots;
}
