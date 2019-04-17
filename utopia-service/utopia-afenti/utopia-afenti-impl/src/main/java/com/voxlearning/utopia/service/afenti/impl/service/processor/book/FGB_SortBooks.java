package com.voxlearning.utopia.service.afenti.impl.service.processor.book;

import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.afenti.api.context.FetchGradeBookContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.Collections;

/**
 * @author Ruib
 * @since 2016/7/13
 */
@Named
public class FGB_SortBooks extends SpringContainerSupport implements IAfentiTask<FetchGradeBookContext> {

    @Override
    public void execute(FetchGradeBookContext context) {
        Term term = SchoolYear.newInstance().currentTerm();

        Collections.sort(context.getBooks(), (o1, o2) -> {
            int t1 = Term.of(o1.getTermType()) == term ? 1 : 0;
            int t2 = Term.of(o2.getTermType()) == term ? 1 : 0;
            return Integer.compare(t2, t1);
        });
    }
}
