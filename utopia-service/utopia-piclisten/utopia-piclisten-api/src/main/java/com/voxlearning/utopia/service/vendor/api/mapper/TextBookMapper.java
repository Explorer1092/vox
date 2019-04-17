package com.voxlearning.utopia.service.vendor.api.mapper;

import com.voxlearning.alps.annotation.meta.Term;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by jiang wei on 2017/4/10.
 */
@Data
public class TextBookMapper implements Serializable {
    private static final long serialVersionUID = 5192940252929935095L;

    private String publisherShortName;
    private String publisherName;
    private Integer rank;
    private Set<ClazzAndTerm> clazzAndTerms;


    @Getter
    @Setter
    @EqualsAndHashCode(of = {"clazzLevel", "termType"})
    public static class ClazzAndTerm implements Serializable{

        private static final long serialVersionUID = -1016909393337422056L;
        private Integer clazzLevel;
        private Integer termType;

        public List<ClazzAndTerm> chaiQuannian(){
            List<ClazzAndTerm> list = new ArrayList<>(2);
            ClazzAndTerm clazzAndTerm1 = new ClazzAndTerm();
            clazzAndTerm1.setClazzLevel(this.getClazzLevel());
            clazzAndTerm1.setTermType(Term.上学期.getKey());
            list.add(clazzAndTerm1);
            ClazzAndTerm clazzAndTerm2 = new ClazzAndTerm();
            clazzAndTerm2.setClazzLevel(this.getClazzLevel());
            clazzAndTerm2.setTermType(Term.下学期.getKey());
            list.add(clazzAndTerm2);
            return list;
        }

        public ClazzAndTerm next(){
            if (clazzLevel == 6 && ( termType == 2 || termType == 0) )
                return null;
            if (termType == 1)
                return ClazzAndTerm.newInstance(clazzLevel, 2);
            return ClazzAndTerm.newInstance(clazzLevel + 1, 1);
        }

        public static ClazzAndTerm newInstance(Integer clazzLevel, Integer term){
            ClazzAndTerm instance = new ClazzAndTerm();
            instance.setTermType(term);
            instance.setClazzLevel(clazzLevel);
            return instance;
        }
    }

    public static class ClazzAndTermComparator implements Comparator<ClazzAndTerm> {
        @Override
        public int compare(ClazzAndTerm o1, ClazzAndTerm o2) {
            int compare = Integer.compare(o1.getClazzLevel(), o2.getClazzLevel());
            if (compare != 0)
                return compare;
            else
                return Integer.compare(o1.getTermType(), o2.getTermType());
        }
    }
}
