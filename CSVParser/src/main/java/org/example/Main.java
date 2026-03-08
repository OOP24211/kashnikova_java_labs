import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Validator.validateArgsCount(args);

            String inputFileName = args[0];
            String outputFileName = args[1];

            Path inputPath = Validator.validateInputFile(inputFileName);

            Validator.validateOutputFile(outputFileName);

            WordCounter counter = new WordCounter();
            processFile(inputPath, counter);
            SortOutput.sort(counter.getWords(), outputFileName, counter.getTotalWords());

        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка валидации: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Причина: " + e.getCause().getMessage());
            }
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void processFile(Path inputPath, WordCounter counter) {
        try (Scanner scanner = new Scanner(inputPath.toFile(), StandardCharsets.UTF_8.name())) {
            while (scanner.hasNext()) {
                String word = scanner.next();
                counter.addRawWord(word);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Файл не найден во время чтения: " + inputPath, e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении файла: " + e.getMessage(), e);
        }
    }
}