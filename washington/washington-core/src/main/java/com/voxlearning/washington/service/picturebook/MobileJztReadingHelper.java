package com.voxlearning.washington.service.picturebook;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.question.api.entity.PictureBookSearchable;
import com.voxlearning.utopia.service.question.api.mapper.PictureBookQuery;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zhiqian.ren
 * @date 2018-09-26
 */
public class MobileJztReadingHelper {


    public static <T extends PictureBookSearchable> Stream<T> filterPictureBooks(Stream<T> stream, PictureBookQuery queryParam, NewBookProfile bookProfile) {
        Stream<T> pictureBookStream = stream.filter(item -> CollectionUtils.isNotEmpty(item.pbClassLevels()) || CollectionUtils.isNotEmpty(item.pbNewClassLevels()));

        if (bookProfile != null && bookProfile.getSubjectId() != null) {
            pictureBookStream = pictureBookStream.filter(pb -> Objects.equals(pb.pbSubjectId(), bookProfile.getSubjectId()));
        }

        if (queryParam != null) {
            if (StringUtils.isNotEmpty(queryParam.getApplyTo())) {
                String applyTo = queryParam.getApplyTo().trim();
                if (StringUtils.isNotEmpty(applyTo)) {
                    pictureBookStream = pictureBookStream.filter(pb -> CollectionUtils.isNotEmpty(pb.pbApplyTo()) && pb.pbApplyTo().contains(applyTo));
                }
            }

            if (StringUtils.isNotEmpty(queryParam.getName())) {
                String name = queryParam.getName().trim();
                if (StringUtils.isNotEmpty(name)) {
                    pictureBookStream = pictureBookStream.filter(pb -> StringUtils.containsIgnoreCase(pb.pbName(), name));
                }
            }

            if (CollectionUtils.isNotEmpty(queryParam.getClazzLevels())) {
                pictureBookStream = pictureBookStream.filter(pb -> CollectionUtils.isNotEmpty(pb.pbClassLevels()))
                        .filter(pb -> CollectionUtils.containsAny(pb.pbClassLevels(), queryParam.getClazzLevels()));
            }

            if (CollectionUtils.isNotEmpty(queryParam.getNewClazzLevels())) {
                List<PictureBookNewClazzLevel> levels = queryParam.getNewClazzLevels().stream().map(PictureBookNewClazzLevel::safeValueOf).collect(Collectors.toList());
                pictureBookStream = pictureBookStream.filter(pb -> CollectionUtils.isNotEmpty(pb.pbNewClassLevels()))
                        .filter(pb -> CollectionUtils.containsAny(pb.pbNewClassLevels(), levels));
            }

            if (CollectionUtils.isNotEmpty(queryParam.getTopicIds())) {
                pictureBookStream = pictureBookStream.filter(pb -> CollectionUtils.isNotEmpty(pb.pbTopicIds()))
                        .filter(pb -> CollectionUtils.containsAny(pb.pbTopicIds(), queryParam.getTopicIds()));
            }

            if (CollectionUtils.isNotEmpty(queryParam.getSeriesIds())) {
                pictureBookStream = pictureBookStream.filter(pb -> StringUtils.isNotBlank(pb.pbSeriesId()))
                        .filter(pb -> queryParam.getSeriesIds().contains(pb.pbSeriesId()));
            }
        }

        return pictureBookStream;
    }

}
