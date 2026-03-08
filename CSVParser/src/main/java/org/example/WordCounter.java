import java.util.HashMap;
import java.util.Map;

public class WordCounter {
    private Map<String, Integer> words = new HashMap<>();
    private int totalWords = 0;

    public void addWord(String word) {
        if (WordCleaner.isCleanWord(word)) {
            words.put(word, words.getOrDefault(word, 0) + 1);
            totalWords++;
        }
    }

    public void addRawWord(String rawWord) {
        String cleanedWord = WordCleaner.clean(rawWord);
        addWord(cleanedWord);
    }

    public Map<String, Integer> getWords() {
        return words;
    }

    public int getTotalWords() {
        return totalWords;
    }
}