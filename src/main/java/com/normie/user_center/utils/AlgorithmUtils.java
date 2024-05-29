package com.normie.user_center.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlgorithmUtils {
    // 将用户标签列表转换为二进制向量
    public static int[] vectorizeTags(List<String> tags, List<String> allTags) {
        int[] vector = new int[allTags.size()];
        for (int i = 0; i < allTags.size(); i++) {
            vector[i] = tags.contains(allTags.get(i)) ? 1 : 0;
        }
        return vector;
    }

    // 计算余弦相似度
    private static double cosineSimilarity(int[] vectorA, int[] vectorB) {
        int dotProduct = 0;
        int normA = 0;
        int normB = 0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        return (normA == 0 || normB == 0) ? 0 : dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // 推荐相似用户
    public static List<Long> recommendUsers(long userId, Map<Long, int[]> userVectors, int num) {
        List<Map.Entry<Long, Double>> similarityList = new ArrayList<>();
        int[] userVector = userVectors.get(userId);
        for (Map.Entry<Long, int[]> entry : userVectors.entrySet()) {
            if (!(entry.getKey() == userId)) {
                double similarity = cosineSimilarity(userVector, entry.getValue());
                similarityList.add(new AbstractMap.SimpleEntry<>(entry.getKey(), similarity));
            }
        }

        similarityList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<Long> recommendations = new ArrayList<>();

        int count = 0;
        for (Map.Entry<Long, Double> entry : similarityList) {
            if(count >= num) break;
            System.out.println(entry.getKey() + ": " + entry.getValue());
            recommendations.add(entry.getKey());
            count++;
        }

        return recommendations;
    }
}
