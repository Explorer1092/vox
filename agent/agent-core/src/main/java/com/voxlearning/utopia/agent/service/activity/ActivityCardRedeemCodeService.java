package com.voxlearning.utopia.agent.service.activity;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.XssfUtils;
import com.voxlearning.utopia.agent.dao.mongo.activity.ActivityCardRedeemCodeDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCardRedeemCode;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Named
public class ActivityCardRedeemCodeService {

    @Inject
    private ActivityCardRedeemCodeDao cardRedeemCodeDao;

    public MapMessage cvExcel2ActivityCardRedeemCode(XSSFWorkbook workbook) {
        final StringBuilder errorList = new StringBuilder();
        List<ActivityCardRedeemCode> cardRedeemCodes = new LinkedList<>();
        Map<String, Integer> cardNumRowMap = new HashMap<>();
        Map<String, Integer> redeemCodeRowMap = new HashMap<>();

        XSSFSheet sheet = workbook != null ? workbook.getSheetAt(0) : null;
        if (sheet == null) {
            return MapMessage.errorMessage("上传失败，请确认数据是否存在");
        }
        int rows = 1;
        while (true) {
            try {
                XSSFRow row = sheet.getRow(rows++);
                if (row == null) {
                    break;
                }

                String cardNo = XssfUtils.getStringCellValue(row.getCell(0));
                String redeemCode = XssfUtils.getStringCellValue(row.getCell(1));
                if (StringUtils.isBlank(cardNo) && StringUtils.isNotBlank(redeemCode)) {
                    errorList.append((rows) + "序列号为空。\r\n");
                    continue;
                }
                if (StringUtils.isBlank(redeemCode) && StringUtils.isNotBlank(cardNo)) {
                    errorList.append((rows) + "兑换码为空。\r\n");
                    continue;
                }
                if (StringUtils.isBlank(cardNo) || StringUtils.isBlank(redeemCode)) {
                    continue;
                }
                if (cardNumRowMap.containsKey(cardNo)) {
                    errorList.append((rows) + "序列号已存在于：" + cardNumRowMap.get(cardNo) + "\r\n");
                    continue;
                }
                if (redeemCodeRowMap.containsKey(redeemCode)) {
                    errorList.append((rows) + "兑换码已存在于：" + redeemCodeRowMap.get(redeemCode) + "\r\n");
                    continue;
                }

                cardNumRowMap.put(cardNo, rows);
                redeemCodeRowMap.put(redeemCode, rows);
                ActivityCardRedeemCode cardRedeemCode = new ActivityCardRedeemCode();
                cardRedeemCode.setCardNo(cardNo);
                cardRedeemCode.setRedeemCode(redeemCode);
                cardRedeemCodes.add(cardRedeemCode);
            } catch (Exception ex) {
                errorList.append((rows) + "行添加失败\r\n");
            }
        }
        if (StringUtils.isNotBlank(errorList)) {
            return MapMessage.errorMessage(errorList.toString());
        }

        Map<String, List<ActivityCardRedeemCode>> existRedeemCodeMap = cardRedeemCodeDao.loadByRds(redeemCodeRowMap.keySet());
        if (MapUtils.isNotEmpty(existRedeemCodeMap)) {
            existRedeemCodeMap.forEach((k, v) -> {
                errorList.append("兑换码已导入！行数：" + redeemCodeRowMap.get(k) + "，兑换码：" + k + "\r\n");
            });
            return MapMessage.errorMessage(errorList.toString());
        }
        if (CollectionUtils.isEmpty(cardRedeemCodes)) {
            return MapMessage.successMessage().add("successRow", 0);
        }

        Consumer<List<ActivityCardRedeemCode>> consumer = list -> cardRedeemCodeDao.inserts(list);
        Integer threshold = 3;
        Integer countDownLatchNum = cardRedeemCodes.size()%threshold>0 ? (cardRedeemCodes.size()/threshold)+1 : cardRedeemCodes.size()/threshold;
        CountDownLatch latch = new CountDownLatch(countDownLatchNum);
        InsertTask task = new InsertTask(cardRedeemCodes, consumer, latch, threshold);
        ForkJoinPool pool = new ForkJoinPool(8);
        pool.submit(task);
        try {
            latch.await(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            pool.shutdown();
        }

        return MapMessage.successMessage().add("successRow", cardRedeemCodes.size());
    }

    public class InsertTask extends RecursiveAction {

        public InsertTask(List<?> list, Consumer consumer, CountDownLatch latch, final int threshold) {
            super();
            this.list = list;
            this.consumer = consumer;
            this.latch = latch;
            this.threshold = threshold;
        }

        private int threshold;
        private List<?> list;
        private CountDownLatch latch;
        private Consumer consumer;
        @Override
        protected void compute() {
            if (list.size() <= threshold) {
                consumer.accept(list);
                latch.countDown();
            } else {
                int middle = list.size() / 2;
                InsertTask left = new InsertTask(list.subList(0, middle), consumer, latch, threshold);
                InsertTask right = new InsertTask(list.subList(middle, list.size()), consumer, latch, threshold);
                left.fork();
                right.fork();
            }
        }
    }

    public static class TestTask extends RecursiveAction {

        public TestTask(List<?> list, Consumer consumer, CountDownLatch latch, final int threshold) {
            super();
            this.list = list;
            this.consumer = consumer;
            this.latch = latch;
            this.threshold = threshold;
        }

        private int threshold;
        private List<?> list;
        private CountDownLatch latch;
        private Consumer consumer;
        @Override
        protected void compute() {
            if (list.size() <= threshold) {
                consumer.accept(list);
                latch.countDown();
            } else {
                int middle = list.size() / 2;
                TestTask left = new TestTask(list.subList(0, middle), consumer, latch, threshold);
                TestTask right = new TestTask(list.subList(middle, list.size()), consumer, latch, threshold);
                left.fork();
                right.fork();
            }
        }
    }

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(4);
        List<Integer> list = new ArrayList();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);
        list.add(8);
//        Consumer<List<Integer>> consumer = l -> System.out.println(l.toString());
        Consumer<List<Integer>> consumer = l -> {
            System.out.println(l.toString());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        TestTask task = new TestTask(list, consumer, latch, 2);
        ForkJoinPool pool = new ForkJoinPool(8);
        pool.submit(task);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();

        }
        System.out.println(3333333);
    }

}
