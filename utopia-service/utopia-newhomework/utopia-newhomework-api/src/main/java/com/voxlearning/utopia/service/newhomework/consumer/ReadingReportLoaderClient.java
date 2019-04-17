package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.ReadingReportLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;

public class ReadingReportLoaderClient implements ReadingReportLoader {
    @ImportService(interfaceClass = ReadingReportLoader.class)
    private ReadingReportLoader hydraRemoteReference;

    @Override
    public MapMessage fetchPictureSemesterReport(Long gid) {
        return hydraRemoteReference.fetchPictureSemesterReport( gid);
    }

    @Override
    public MapMessage fetchPictureSemesterReportFromBigData(Long gid) {
        return hydraRemoteReference.fetchPictureSemesterReportFromBigData(gid);
    }

    @Override
    public MapMessage fetchAbilityAnalysis( Long gid) {
        return hydraRemoteReference.fetchAbilityAnalysis( gid);
    }

    @Override
    public MapMessage fetchAbilityAnalysisFromBigData(Long gid) {
        return hydraRemoteReference.fetchAbilityAnalysisFromBigData(gid);
    }

    @Override
    public MapMessage fetchPictureInfo(Teacher teacher, String hid) {
        return hydraRemoteReference.fetchPictureInfo(teacher, hid);
    }

    @Override
    public MapMessage fetchRecommend(String hid, ObjectiveConfigType type, String pictureId) {
        return hydraRemoteReference.fetchRecommend(hid, type, pictureId);
    }

    @Override
    public MapMessage fetchUserInfo(Long gid) {
        return hydraRemoteReference.fetchUserInfo(gid);
    }

    @Override
    public MapMessage fetchPictureWordCnt(List<String> hids) {
        return hydraRemoteReference.fetchPictureWordCnt(hids);
    }
}
