package com.voxlearning.washington.data.view;

import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教材版本视图
 *
 * @Author: peng.zhang
 * @Date: 2018/9/5
 */
@Data
public class SeriesView {

    /**
     * 教材版本类型 Id
     */
    private String bookId;

    /**
     * 教材版本名称,如:人教版、苏教版等等
     */
    private String bookName;

    /**
     * 从几年级年级开始有该教材
     */
    private Object startClazzLevel;

    /**
     * 从哪个学期开始有该教材
     */
    private List<Map<String,Object>> itemTypes;

    public static class Builder {

        public static List<SeriesView> build(List<NewBookCatalog> newBookCatalogList,List<NewBookProfile> bookProfiles){
            List<SeriesView> seriesViewList = new ArrayList<>();
            NewBookCatalog catalog = newBookCatalogList.stream().filter(i->i.getName().contains("人教版")).
                    findFirst().orElse(null);
            newBookCatalogList.remove(catalog);
            // 默认人教版的
            newBookCatalogList.add(0,catalog);
            for (NewBookCatalog newBookCatalog : newBookCatalogList){
                SeriesView seriesView = new SeriesView();
                seriesView.setBookId(newBookCatalog.getId());
                seriesView.setBookName(newBookCatalog.getName());

                List<Integer> itemTypes = bookProfiles.stream().
                        filter(i->newBookCatalog.getId().equals(i.getSeriesId())).map(NewBookProfile::getTermType).
                        distinct().filter(i->i!=0).collect(Collectors.toList());
                List<Map<String,Object>> itemMapList = new ArrayList<>();
                for (Integer itemKey : itemTypes){
                    Map<String,Object> termMap = new HashMap<>();
                    termMap.put("id",itemKey);
                    termMap.put("name",Term.of(itemKey));
                    itemMapList.add(termMap);
                }
                if (newBookCatalog.getExtras() != null){
                    seriesView.setStartClazzLevel(newBookCatalog.getExtras().get("start_clazz_level"));
                }
                seriesView.setItemTypes(itemMapList);
                seriesViewList.add(seriesView);
            }
            return seriesViewList;
        }
    }

}
