import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Nine {

    @FunctionalInterface
    interface Cont<T> {
        void apply(T value);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java Nine <input_file>");
            System.exit(1);
        }
        readFile(args[0], Nine::filterChars);
    }

    static void readFile(String path, Cont<String> next) {
        try {
            String data = Files.readString(Paths.get(path));
            next.apply(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void filterChars(String text) {
        Pattern pattern = Pattern.compile("[\\W_]+");
        String cleaned = pattern.matcher(text).replaceAll(" ");
        normalize(cleaned);
    }

    static void normalize(String text) {
        scan(text.toLowerCase());
    }

    static void scan(String text) {
        List<String> words = Arrays.asList(text.split("\\s+"));
        removeStopWords(words);
    }

    static void removeStopWords(List<String> words) {
        try {
            List<String> stopWords = Arrays.asList(
                Files.readString(Paths.get("stop_words.txt")).split(",")
            );

            Set<String> stopSet = new HashSet<>(stopWords);
            for (char c = 'a'; c <= 'z'; c++) stopSet.add(String.valueOf(c));

            List<String> filtered = new ArrayList<>();
            for (String w : words)
                if (!stopSet.contains(w)) filtered.add(w);

            frequencies(filtered);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void frequencies(List<String> words) {
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words)
            freq.put(w, freq.getOrDefault(w, 0) + 1);
        sort(freq);
    }

    static void sort(Map<String, Integer> freq) {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(freq.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        printText(sorted);
    }

    static void printText(List<Map.Entry<String, Integer>> entries) {
        for (int i = 0; i < Math.min(25, entries.size()); i++) {
            Map.Entry<String, Integer> e = entries.get(i);
            System.out.println(e.getKey() + " - " + e.getValue());
        }
        noOp();
    }

    static void noOp() {
        
    }
}

