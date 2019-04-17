package com.voxlearning.utopia.service.afenti.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.afenti.api.ReadingBookSummaryLoader;
import com.voxlearning.utopia.service.afenti.api.entity.PicBookOrderStat;
import com.voxlearning.utopia.service.afenti.api.entity.ReadingBookSummary;
import com.voxlearning.utopia.service.afenti.impl.dao.PicBookOrderStatDao;
import com.voxlearning.utopia.service.afenti.impl.dao.ReadingBookSummaryDao;
import com.voxlearning.utopia.service.question.api.entity.PictureBookSeries;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = ReadingBookSummaryLoader.class)
public class ReadingBookSummaryLoaderImpl implements ReadingBookSummaryLoader{

    @Inject private PictureBookLoaderClient pictureBookLoaderCli;
    @Inject private ReadingBookSummaryDao readingBookSummaryDao;
    @Inject private PicBookOrderStatDao picBookOrderStatDao;
    private static List<String> blackPics;

    static {
        blackPics = new ArrayList<>();
        blackPics.add("PBP_10300000924556");
        blackPics.add("PBP_10300000901390");
        blackPics.add("PBP_10300000945126");
        blackPics.add("PBP_10300000916039");
        blackPics.add("PBP_10300000910355");
    }

    private PictureBookSeries loadPicBookSeriesByName(String seriesName){
        return pictureBookLoaderCli.loadAllPictureBookSeries()
                .stream()
                .filter(pbs -> Objects.equals(pbs.getName(),seriesName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ReadingBookSummary> loadReadingBookSummaries(String seriesName,Date start,Date end) {
        PictureBookSeries series = loadPicBookSeriesByName(seriesName);
        return Optional.ofNullable(series)
                .map(s -> readingBookSummaryDao.loadBySeriesId(s.getId(), start, end)
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(readingBookSummary -> !blackPics.contains(readingBookSummary.getBookId()))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<PicBookOrderStat> loadPicBookOrderStat(String seriesName, Date start, Date end) {
        PictureBookSeries series = loadPicBookSeriesByName(seriesName);
        return Optional.ofNullable(series)
                .map(s -> picBookOrderStatDao.loadBySeriesId(s.getId(), start, end).stream()
                        .filter(Objects::nonNull)
                        .filter(e -> !blackPics.contains(e.getBookId()))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
