import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final WordService service = new WordService();

    public static void main(String[] args) {
        try {
            service.init();
        } catch (IOException e) {
            System.err.println("Fatal Error: Could not load data records.");
            System.err.println(e.getMessage());
            return;
        }
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readIntChoice("Choose an option: ", 0, 6);
            switch (choice) {
                case 1 -> handleAddWord();
                case 2 -> handleViewAllWords();
                case 3 -> handleSearchWord();
                case 4 -> handleViewDetails();
                case 5 -> handleMarkStatus();
                case 6 -> handleReport();
                case 0 -> { System.out.println("Goodbye!"); running = false; }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\nWORD NOTEBOOK MENU");
        System.out.println("1. Add word");
        System.out.println("2. View all words");
        System.out.println("3. Search word");
        System.out.println("4. View meaning and examples");
        System.out.println("5. Mark Learned or New");
        System.out.println("6. View and save progress report");
        System.out.println("0. Exit");
    }

    private static void handleAddWord() {
        System.out.println("\nWORD TYPES");
        System.out.println("1. Noun");
        System.out.println("2. Verb");
        int typeChoice = readIntChoice("Select type (1 or 2): ", 1, 2);
        String word;
        while (true) {
            word = readNonBlankString("Word: ");
            if (service.isWordDuplicate(word)) System.out.println("Word already exists. Please choose a different word.");
            else break;
        }
        String meaning = readNonBlankString("Meaning: ");
        String ex1 = readNonBlankString("Example 1: ");
        String ex2 = readNonBlankString("Example 2: ");
        try {
            WordEntry created = service.addWord(typeChoice, word, meaning, ex1, ex2);
            System.out.println("Word " + created.getId() + " saved. Status: " + created.getStatus());
        } catch (IOException e) {
            System.out.println("Error saving word to file: " + e.getMessage());
        }
    }

    private static void handleViewAllWords() {
        System.out.println("\nALL WORDS");
        List<WordEntry> list = service.getAllWords();
        if (list.isEmpty()) { System.out.println("No records found."); return; }
        for (WordEntry w : list) System.out.println(w.getId() + " | " + w.getWord() + " | " + w.getTypeLabel() + " | " + w.getStatus());
    }

    private static void handleSearchWord() {
        System.out.println("\nSEARCH WORD");
        String term = readNonBlankString("Search: ");
        List<WordEntry> results = service.search(term);
        if (results.isEmpty()) { System.out.println("No matching words"); return; }
        for (WordEntry w : results) System.out.println(w.getId() + " | " + w.getWord() + " | " + w.getTypeLabel() + " | " + w.getStatus());
    }

    private static void handleViewDetails() {
        System.out.println("\nVIEW MEANING AND EXAMPLES");
        int id = readPositiveInt("Enter word ID: ");
        WordEntry entry = service.findById(id);
        if (entry == null) { System.out.println("Unknown ID: " + id + ". No record found."); return; }
        System.out.println("\nWord: " + entry.getWord() + " (" + entry.getTypeLabel() + ")");
        System.out.println("Meaning: " + entry.getMeaning());
        System.out.println("Status: " + entry.getStatus());
        String[] examples = entry.getExamples();
        for (int i = 0; i < examples.length; i++) System.out.println("Example " + (i + 1) + ": " + examples[i]);
    }

    private static void handleMarkStatus() {
        System.out.println("\nMARK LEARNED OR NEW");
        int id = readPositiveInt("Enter word ID: ");
        WordEntry entry = service.findById(id);
        if (entry == null) { System.out.println("Unknown ID: " + id + ". No record found."); return; }
        System.out.println("Current status: " + entry.getStatus());
        System.out.println("1. LEARNED");
        System.out.println("2. NEW");
        int choice = readIntChoice("Choose status (1 or 2): ", 1, 2);
        String newStatus = choice == 1 ? "LEARNED" : "NEW";
        try {
            int result = service.updateStatus(id, newStatus);
            if (result == 0) System.out.println("Selected status already matches. Record unchanged.");
            else if (result == 1) System.out.println("Status saved.");
        } catch (IOException e) {
            System.out.println("Error saving status to file: " + e.getMessage());
        }
    }

    private static void handleReport() {
        System.out.println("\nPROGRESS REPORT");
        String report = service.generateReport();
        System.out.print(report);
        System.out.print("Save report to data/report.txt? (y/n): ");
        String answer = scanner.nextLine().trim();
        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
            try {
                FileManager.saveReport(report);
                System.out.println("Report saved to data/report.txt successfully.");
            } catch (IOException e) { System.out.println("Error saving report: " + e.getMessage()); }
        }
    }

    private static String readNonBlankString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) { System.out.println("Field cannot be blank. Please try again."); continue; }
            if (line.contains("|")) { System.out.println("Input cannot contain the '|' delimiter. Please try again."); continue; }
            return line;
        }
    }

    private static int readIntChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) return val;
                System.out.println("Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) { System.out.println("Invalid numeric input. Please try again."); }
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val > 0) return val;
                System.out.println("Please enter a positive ID greater than 0.");
            } catch (NumberFormatException e) { System.out.println("Invalid numeric input. Please try again."); }
        }
    }
}
