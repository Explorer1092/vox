package com.voxlearning.enanalyze.view;

import com.voxlearning.utopia.enanalyze.model.PageInfo;
import com.voxlearning.utopia.enanalyze.model.SentenceRankResult;
import lombok.Data;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排行分页视图
 *
 * @author xiaolei.li
 * @version 2018/7/6
 */
@Data
public class RankSentencePageView implements Serializable {
    private int page;
    private int size;
    private List<RankSentenceView> data;


    public static class Builder {

        public static RankSentencePageView build(List<SentenceRankResult> list, PageInfo pageInfo) {
            RankSentencePageView view = new RankSentencePageView();
            view.setPage(pageInfo.getPage());
            view.setSize(pageInfo.getSize());
            // 这里其实是个假分页，数据一次获取，然后截取
            // 基于微信的群成员不会太多，所以性能没问题
            view.setData(
                    list.stream()
                            .sorted(Comparator.comparing(SentenceRankResult::getRank))
                            .skip(pageInfo.getSize() * pageInfo.getPage())
                            .limit(pageInfo.getSize())
                            .map(RankSentenceView.Builder::build)
                            .collect(Collectors.toList()));
            return view;
        }
    }
}
