public class WordCleaner {
    public static String clean(String word) {
        if (word == null || word.isEmpty()) {
            return "";
        }

        String cleaned = word.replaceAll("^[^\p{L}\p{M}'-]+|[^\p{L}\p{M}'-]+$", "");
        return cleaned.toLowerCase();
    }

    public static boolean isCleanWord(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        return word.matches(".*\\p{L}.*");
    }
}
