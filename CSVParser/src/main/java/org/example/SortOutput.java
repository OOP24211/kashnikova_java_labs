import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SortOutput {
    private static int compare(Map.Entry<String, Integer> word1,
                               Map.Entry<String, Integer> word2) {
        if (!word1.getValue().equals(word2.getValue())) {
            return Integer.compare(word2.getValue(), word1.getValue()); // по убыванию
        }
        return word1.getKey().compareTo(word2.getKey());
    }

    public static void sort(Map<String, Integer> words,
                            String outputFileName, int totalCount) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(words.entrySet());
        list.sort(SortOutput::compare);

        try (PrintWriter writer = new PrintWriter(outputFileName, StandardCharsets.UTF_8.name())) {
            writer.println("Слово; Частота; Частота(%)");

            for (Map.Entry<String, Integer> entry : list) {
                double percentage = (totalCount > 0) ?
                        ((double) entry.getValue() / totalCount * 100) : 0.0;
                writer.printf("%s;%d;%.2f%%%n",
                        entry.getKey(), entry.getValue(), percentage);
            }
        } catch (IOException e) {
            System.out.println("Not open output");
        }
    }
}