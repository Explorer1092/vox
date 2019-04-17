package com.voxlearning.utopia.service.newhomework.api.util;

import javafx.util.Pair;

import java.util.*;

/**
 * 权重随机工具类
 * @author majianxin
 * @version V1.0
 * @date 2019/3/26
 */
public class WeightRandom<K,V extends Number> {

    private TreeMap<Double, K> weightMap = new TreeMap<>();

    public WeightRandom(List<Pair<K, V>> list) {
        Objects.requireNonNull(list, "list can NOT be null!");
        for (Pair<K, V> pair : list) {
            double lastWeight = this.weightMap.size() == 0 ? 0 : this.weightMap.lastKey();//统一转为double
            this.weightMap.put(pair.getValue().doubleValue() + lastWeight, pair.getKey());//权重累加
        }
    }

    public K random() {
        double randomWeight = this.weightMap.lastKey() * Math.random();
        SortedMap<Double, K> tailMap = this.weightMap.tailMap(randomWeight, false);
        return this.weightMap.get(tailMap.firstKey());
    }

    public static void main(String[] args) {
        ArrayList<Pair<String, Integer>> atomList = new ArrayList<>();
        atomList.add(new Pair<>("a", 10));
        atomList.add(new Pair<>("b", 20));
        atomList.add(new Pair<>("c", 30));
        atomList.add(new Pair<>("d", 40));
        WeightRandom<String, Integer> weightRandom = new WeightRandom<>(atomList);

        Map<String, Integer> countAtom = new HashMap<>();
        for (int i = 0; i < 100000; i++) {
            String random = weightRandom.random();
            if (countAtom.containsKey(random)) {
                countAtom.put(random, countAtom.get(random) + 1);
            } else {
                countAtom.put(random, 1);
            }
        }
        System.out.println("概率统计：");
        for (String id : countAtom.keySet()) {
            System.out.println(" " + id + " 出现了 " + countAtom.get(id) + " 次");
        }
    }

}