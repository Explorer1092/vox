package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.dao.jdbc.DAO;
import com.voxlearning.alps.annotation.dao.jdbc.P;
import com.voxlearning.alps.annotation.dao.jdbc.SQL;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Ruib
 * @since 2016/7/14
 */
@DAO(ds = "hs_afenti", entityClass = AfentiLearningPlanUserRankStat.class)
public interface AfentiLearningPlanUserRankStatDao {

    @SQL("INSERT INTO VOX_AFENTI_LEARNING_PLAN_USER_RANK_STAT (CREATETIME, UPDATETIME, USER_ID, NEW_BOOK_ID, NEW_UNIT_ID, RANK, STAR, SILVER, SUCCESSIVE_SILVER, BONUS, SUBJECT) values (#{createTime}, #{updateTime}, #{userId}, #{newBookId}, #{newUnitId}, #{rank}, #{star}, #{silver}, #{successiveSilver}, #{bonus}, #{subject}) ")
    Long insert(AfentiLearningPlanUserRankStat entity);

    @SQL("SELECT * FROM VOX_AFENTI_LEARNING_PLAN_USER_RANK_STAT WHERE USER_ID = #{userId} AND NEW_BOOK_ID = #{newBookId} ")
    List<AfentiLearningPlanUserRankStat> queryByUserIdAndNewBookId(@P("userId") Long userId, @P("newBookId") String newBookId);

    @SQL("UPDATE VOX_AFENTI_LEARNING_PLAN_USER_RANK_STAT SET UPDATETIME = NOW(), STAR = STAR + #{star}, SILVER = SILVER + #{silver}, SUCCESSIVE_SILVER = SUCCESSIVE_SILVER + #{successiveSilver}, BONUS = BONUS + #{bonus} WHERE USER_ID = #{userId} AND ID = #{id} ")
    int updateStat(@P("userId") Long userId, @P("id") Long id, @P("star") int star, @P("silver") int silver, @P("successiveSilver") int successiveSilver, @P("bonus") int bonus);

    @SQL("SELECT SUM(STAR) FROM VOX_AFENTI_LEARNING_PLAN_USER_RANK_STAT WHERE USER_ID = #{userId} AND SUBJECT = #{subject} ")
    BigDecimal queryTotalStar(@P("userId") Long userId, @P("subject") Subject subject);

    @SQL("SELECT SUM(SILVER+SUCCESSIVE_SILVER+BONUS) FROM VOX_AFENTI_LEARNING_PLAN_USER_RANK_STAT WHERE USER_ID = #{userId}")
    BigDecimal queryTotalIntegarl(@P("userId") Long userId);


}
