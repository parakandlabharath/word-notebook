import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FileManager {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path WORDS_FILE = DATA_DIR.resolve("words.txt");
    private static final Path REPORT_FILE = DATA_DIR.resolve("report.txt");

    public static void ensureDataDirectory() throws IOException {
        if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
    }

    public static List<WordEntry> loadWords() throws IOException {
        ensureDataDirectory();
        List<WordEntry> loaded = new ArrayList<>();
        if (!Files.exists(WORDS_FILE)) return loaded;
        Set<Integer> seenIds = new HashSet<>();
        Set<String> seenNormalizedWords = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(WORDS_FILE, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split("\\|", -1);
                if (fields.length != 7) throw new IOException("words.txt: line " + lineNumber + ": Expected 7 fields, found " + fields.length);
                int id;
                try {
                    id = Integer.parseInt(fields[0].trim());
                    if (id <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new IOException("words.txt: line " + lineNumber + ": Invalid positive ID '" + fields[0] + "'");
                }
                if (!seenIds.add(id)) throw new IOException("words.txt: line " + lineNumber + ": Duplicate ID " + id);
                String type = fields[1].trim().toUpperCase(Locale.ROOT);
                String word = fields[2].trim();
                String meaning = fields[3].trim();
                String ex1 = fields[4].trim();
                String ex2 = fields[5].trim();
                String status = fields[6].trim().toUpperCase(Locale.ROOT);
                if (word.isEmpty() || meaning.isEmpty() || ex1.isEmpty() || ex2.isEmpty()) throw new IOException("words.txt: line " + lineNumber + ": Word, meaning, and examples cannot be blank.");
                String normKey = word.toLowerCase(Locale.ROOT);
                if (!seenNormalizedWords.add(normKey)) throw new IOException("words.txt: line " + lineNumber + ": Duplicate normalized word: " + word);
                String[] examples = new String[]{ex1, ex2};
                WordEntry entry;
                if ("NOUN".equals(type)) entry = new NounEntry(id, word, meaning, examples, status);
                else if ("VERB".equals(type)) entry = new VerbEntry(id, word, meaning, examples, status);
                else throw new IOException("words.txt: line " + lineNumber + ": Unknown type '" + type + "'. Must be NOUN or VERB.");
                loaded.add(entry);
            }
        }
        return loaded;
    }

    public static void saveWords(List<WordEntry> words) throws IOException {
        ensureDataDirectory();
        Path tempFile = DATA_DIR.resolve("words.txt.tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            for (WordEntry entry : words) {
                String[] examples = entry.getExamples();
                String line = String.join("|", String.valueOf(entry.getId()), entry.getTypeCode(), entry.getWord(), entry.getMeaning(), examples[0], examples[1], entry.getStatus());
                writer.write(line);
                writer.newLine();
            }
        }
        try {
            Files.move(tempFile, WORDS_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(tempFile, WORDS_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void saveReport(String reportContent) throws IOException {
        ensureDataDirectory();
        try (BufferedWriter writer = Files.newBufferedWriter(REPORT_FILE, StandardCharsets.UTF_8)) {
            writer.write(reportContent);
        }
    }
}
