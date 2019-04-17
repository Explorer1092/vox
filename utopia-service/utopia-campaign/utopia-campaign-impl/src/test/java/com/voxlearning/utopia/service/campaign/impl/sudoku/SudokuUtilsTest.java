package com.voxlearning.utopia.service.campaign.impl.sudoku;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.entity.activity.SudokuDayQuestion;
import com.voxlearning.utopia.enums.ActivityDifficultyLevelEnum;
import com.voxlearning.utopia.service.campaign.impl.support.EternalLifeIterator;
import com.voxlearning.utopia.service.campaign.impl.support.SudokuUtils;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SudokuUtilsTest {

    @Test
    public void test1() throws ParseException {
        Date start = DateUtils.parseDate("2018-09-16 08:05:12", "yyyy-MM-dd HH:mm:ss");
        Date end = DateUtils.parseDate("2018-09-20 08:05:12", "yyyy-MM-dd HH:mm:ss");

        SudokuUtils.AbstractQuestionBuild generatorBuild = SudokuUtils.swichGeneratorBuild(
                new ObjectId().toString(),
                start, end,
                ActivityDifficultyLevelEnum.NORMAL,
                3
        );
        List<SudokuDayQuestion> result = generatorBuild.build();

        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void test2() throws ParseException {
        Date start = DateUtils.parseDate("2018-07-05 08:05:12", "yyyy-MM-dd HH:mm:ss");
        Date end = DateUtils.parseDate("2018-07-20 20:05:12", "yyyy-MM-dd HH:mm:ss");

        List<String> strings = SudokuUtils.generateDayRange(start, end);
        System.out.println(strings);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M月d日");
        System.out.println(dateTimeFormatter.format(LocalDate.now()));
    }

    @Test
    public void test3() {
        List<String> list1 = Arrays.asList("list1-1", "list1-2", "list1-3");
        List<String> list2 = Arrays.asList("list2-1", "list2-2", "list2-3");
        List<String> list3 = Arrays.asList("list3-1", "list3-2", "list3-3");

        EternalLifeIterator<String> lifeIterator = new EternalLifeIterator<>(Arrays.asList(list1, list2, list3));
        for (int j = 0; j < 10; j++) {
            System.out.println(lifeIterator.next());
        }

        System.out.println();
        System.out.println();

        lifeIterator = new EternalLifeIterator<>(Arrays.asList(list2, list3, list1));
        for (int j = 0; j < 10; j++) {
            System.out.println(lifeIterator.next());
        }

        System.out.println();
        System.out.println();

        lifeIterator = new EternalLifeIterator<>(Arrays.asList(list3, list1, list2));
        for (int j = 0; j < 10; j++) {
            System.out.println(lifeIterator.next());
        }
    }


}
