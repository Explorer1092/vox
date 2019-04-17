package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.api.data.BookUnit;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author ruib
 * @since 16/7/13
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchBookUnitsContext extends AbstractAfentiContext<FetchBookUnitsContext> {
    private static final long serialVersionUID = 235122514269459059L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;
    @NonNull private AfentiLearningType learningType;

    // middle
    private AfentiBook book; // 当前正在使用的教材
    private Boolean isNewRankBook = false; // 是否是新生成关卡的教材
    private String newRankBookId;  // 新生成关卡的教材ID  带前缀
    private Map<String, Integer> unit_asc_map = new LinkedHashMap<>(); // 每个单元获得星星的数量
    private Map<String, Integer> unit_asrc_map = new LinkedHashMap<>(); // 每个单元至少获得一颗星的关卡数量
    private Map<String, Integer> unit_footprint_map = new LinkedHashMap<>(); // 每个单元的足迹数量

    // out
    private List<BookUnit> units = new LinkedList<>();
    private String bookName = "";
}