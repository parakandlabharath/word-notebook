import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WordService {
    private final List<WordEntry> words = new ArrayList<>();
    private final Set<String> wordKeys = new HashSet<>();
    private int nextId = 1;

    public void init() throws IOException {
        words.clear();
        wordKeys.clear();
        List<WordEntry> loaded = FileManager.loadWords();
        int maxId = 0;
        for (WordEntry w : loaded) {
            words.add(w);
            wordKeys.add(normalize(w.getWord()));
            if (w.getId() > maxId) maxId = w.getId();
        }
        nextId = maxId + 1;
    }

    public static String normalize(String word) {
        return word == null ? "" : word.trim().toLowerCase(Locale.ROOT);
    }

    public boolean isWordDuplicate(String word) { return wordKeys.contains(normalize(word)); }

    public WordEntry addWord(int typeChoice, String word, String meaning, String ex1, String ex2) throws IOException {
        String key = normalize(word);
        if (wordKeys.contains(key)) throw new IllegalArgumentException("Word already exists: " + word);
        String[] examples = new String[]{ex1.trim(), ex2.trim()};
        int assignedId = nextId;
        WordEntry newEntry;
        if (typeChoice == 1) newEntry = new NounEntry(assignedId, word.trim(), meaning.trim(), examples, "NEW");
        else if (typeChoice == 2) newEntry = new VerbEntry(assignedId, word.trim(), meaning.trim(), examples, "NEW");
        else throw new IllegalArgumentException("Invalid type choice. Choose 1 for Noun or 2 for Verb.");
        words.add(newEntry);
        wordKeys.add(key);
        nextId++;
        try {
            FileManager.saveWords(words);
        } catch (IOException e) {
            words.remove(words.size() - 1);
            wordKeys.remove(key);
            nextId--;
            throw e;
        }
        return newEntry;
    }

    public List<WordEntry> getAllWords() { return new ArrayList<>(words); }

    public List<WordEntry> search(String query) {
        List<WordEntry> results = new ArrayList<>();
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) return results;
        for (WordEntry entry : words) if (normalize(entry.getWord()).contains(normalizedQuery)) results.add(entry);
        return results;
    }

    public WordEntry findById(int id) {
        for (WordEntry entry : words) if (entry.getId() == id) return entry;
        return null;
    }

    public int updateStatus(int id, String newStatus) throws IOException {
        WordEntry entry = findById(id);
        if (entry == null) return -1;
        String formattedStatus = newStatus.trim().toUpperCase(Locale.ROOT);
        if (entry.getStatus().equalsIgnoreCase(formattedStatus)) return 0;
        String oldStatus = entry.getStatus();
        entry.setStatus(formattedStatus);
        try {
            FileManager.saveWords(words);
        } catch (IOException e) {
            entry.setStatus(oldStatus);
            throw e;
        }
        return 1;
    }

    public String generateReport() {
        int nounCount = 0, verbCount = 0, newCount = 0, learnedCount = 0;
        for (WordEntry entry : words) {
            if ("NOUN".equals(entry.getTypeCode())) nounCount++;
            else if ("VERB".equals(entry.getTypeCode())) verbCount++;
            if ("LEARNED".equals(entry.getStatus())) learnedCount++;
            else newCount++;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Total Words: ").append(words.size()).append("\n");
        sb.append("Nouns: ").append(nounCount).append("\n");
        sb.append("Verbs: ").append(verbCount).append("\n");
        sb.append("Learned: ").append(learnedCount).append("\n");
        sb.append("New: ").append(newCount).append("\n");
        return sb.toString();
    }
}
