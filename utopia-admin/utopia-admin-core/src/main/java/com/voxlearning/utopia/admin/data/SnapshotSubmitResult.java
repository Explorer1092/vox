package com.voxlearning.utopia.admin.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class SnapshotSubmitResult implements Serializable {
    private static final long serialVersionUID = -1251859511252579381L;

    private String state;
    private List<String> snapshots;
}