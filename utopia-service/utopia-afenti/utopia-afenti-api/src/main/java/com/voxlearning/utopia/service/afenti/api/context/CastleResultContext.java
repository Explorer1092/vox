package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiType;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/7/21
 */
@Getter
@Setter
@NoArgsConstructor
public class CastleResultContext extends AbstractAfentiContext<CastleResultContext> {
    private static final long serialVersionUID = 3073723083430028370L;

    // in
    private StudentDetail student; // 学生
    private Subject subject; // 学科
    private String bookId; // 教材id
    private String unitId; // 单元id
    private Integer rank; // 第几关
    private String questionId; // 试题id
    private Boolean master; // 是否做对了
    private AfentiType learningType; // Afenti的四种类型
    private Boolean finished; // 是否是最后一题
    private Integer wkpc; //  错误知识点数
    private AfentiLearningType afentiLearningType;  // 区分预习和城堡
    private Boolean isNewRankBook = false; // 是否是新生成关卡的教材
    private String newRankBookId;  // 新生成关卡的教材ID  带前缀

    // middle
    private List<AfentiLearningPlanPushExamHistory> histories = new ArrayList<>();
    private AfentiLearningPlanUserRankStat stat;
    private List<Integer> correspondence = new ArrayList<>();
    private Integer star = 0;
    private Integer silver = 0;
    private Integer creditCount = 0;    // 自学积分数量[45497]
    private Integer bonus = 0;
    private boolean authorized = false;  //是否是会员用户
    private boolean boughtAfenti = false; //是否有阿分题会员（数学，语文，英语有任何一科就算）
    private List<UserActivatedProduct> AfentiVideoActivatedProducts = new ArrayList<>();
    private BigDecimal multiple = new BigDecimal(1); // 倍数

    // out
    private Map<String, Object> result = new HashMap<>();
}
