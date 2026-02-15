
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Main <input_file> <output_file>");
            return;

        WordCounter counter = new WordCounter();

        try (Scanner scanner = new Scanner(new File(args[0]), StandardCharsets.UTF_8.name())) {
            while (scanner.hasNext()) {
                String word = scanner.next();
                counter.addRawWord(word);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Not open input");
            return;
        }

        SortOutput.sort(counter.getWords(), args[1], counter.getTotalWords());
    }
}