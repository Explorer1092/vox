package com.voxlearning.utopia.service.campaign.api.enums;

public enum CourseStatus {
    DEFAULT(0),
    ONLINE(1),
    OFFLINE(2);
    private int status;
    private CourseStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
