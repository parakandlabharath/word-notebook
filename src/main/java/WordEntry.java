import java.util.Locale;
import java.util.Objects;

public abstract class WordEntry {
    private final int id;
    private final String word;
    private final String meaning;
    private String status;
    private final String[] examples;

    public WordEntry(int id, String word, String meaning, String[] examples, String status) {
        if (id <= 0) throw new IllegalArgumentException("ID must be a positive integer.");
        this.id = id;
        this.word = Objects.requireNonNull(word).trim();
        this.meaning = Objects.requireNonNull(meaning).trim();
        if (examples == null || examples.length != 2) throw new IllegalArgumentException("Exactly two example sentences are required.");
        this.examples = examples.clone();
        setStatus(status);
    }

    public int getId() { return id; }
    public String getWord() { return word; }
    public String getMeaning() { return meaning; }
    public String getStatus() { return status; }

    public final void setStatus(String status) {
        String s = Objects.requireNonNull(status).trim().toUpperCase(Locale.ROOT);
        if (!s.equals("NEW") && !s.equals("LEARNED")) throw new IllegalArgumentException("Status must be either 'NEW' or 'LEARNED'.");
        this.status = s;
    }

    public String[] getExamples() { return examples.clone(); }
    public abstract String getTypeLabel();
    public abstract String getTypeCode();
}
