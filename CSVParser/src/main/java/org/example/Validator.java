import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Validator {
    private static final int REQUIRED_ARGS_COUNT = 2;
    private static final String USAGE_MESSAGE = "Usage: java Main <input_file> <output_file>";

    private Validator() {
    }

    public static void validateArgsCount(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("Аргументы командной строки не могут быть null");
        }
        if (args.length != REQUIRED_ARGS_COUNT) {
            throw new IllegalArgumentException(
                    String.format("Неверное количество аргументов. Ожидалось: %d, получено: %d. %s",
                            REQUIRED_ARGS_COUNT, args.length, USAGE_MESSAGE)
            );
        }
    }

    public static void validateNotEmpty(String str, String fieldName) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Поле '%s' не может быть пустым", fieldName)
            );
        }
    }

    public static Path validateInputFile(String filePath) {
        validateNotEmpty(filePath, "input file");

        try {
            Path path = Paths.get(filePath);

            if (!Files.exists(path)) {
                throw new IllegalArgumentException(
                        String.format("Входной файл не существует: %s", filePath)
                );
            }

            if (!Files.isRegularFile(path)) {
                throw new IllegalArgumentException(
                        String.format("Указанный путь не является файлом: %s", filePath)
                );
            }

            if (!Files.isReadable(path)) {
                throw new IllegalArgumentException(
                        String.format("Нет прав на чтение файла: %s", filePath)
                );
            }

            return path;

        } catch (InvalidPathException e) {
            throw new IllegalArgumentException(
                    String.format("Некорректный путь к файлу: %s", filePath), e
            );
        }
    }

    public static Path validateOutputFile(String filePath) {
        validateNotEmpty(filePath, "output file");

        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();

            if (parentDir != null && !Files.exists(parentDir)) {
                throw new IllegalArgumentException(
                        String.format("Директория для выходного файла не существует: %s", parentDir)
                );
            }

            if (Files.exists(path)) {
                if (!Files.isWritable(path)) {
                    throw new IllegalArgumentException(
                            String.format("Нет прав на запись в выходной файл: %s", filePath)
                    );
                }
            } else if (parentDir != null && !Files.isWritable(parentDir)) {
                throw new IllegalArgumentException(
                        String.format("Нет прав на создание файла в директории: %s", parentDir)
                );
            }

            return path;

        } catch (InvalidPathException e) {
            throw new IllegalArgumentException(
                    String.format("Некорректный путь к файлу: %s", filePath), e
            );
        }
    }
}