package com.voxlearning.utopia.business.api.constant.teachingdiagnosis;

public enum ExperimentType {
    COMMON,//普通实验
    INTERNAL;//作业内部实验
    public static ExperimentType safe(String name) {
        try{
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
