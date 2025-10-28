import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.Function;  

public class Ten {

    static class TheOne<T> {
        private T value;

        public TheOne(T v) {
            this.value = v;
        }

        public <R> TheOne<R> bind(Function<T, R> func) {
            return new TheOne<>(func.apply(value));
        }

        public void printme() {
            System.out.println(value);
        }
    }

    static String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String filterChars(String text) {
        return text.replaceAll("[\\W_]+", " ");
    }

    static String normalize(String text) {
        return text.toLowerCase();
    }

    static List<String> scan(String text) {
        return Arrays.asList(text.split("\\s+"));
    }

    static List<String> removeStopWords(List<String> words) {
        try {
            List<String> stopWords = Arrays.asList(
                    Files.readString(Paths.get("stop_words.txt")).split(","));
            Set<String> stopSet = new HashSet<>(stopWords);
            for (char c = 'a'; c <= 'z'; c++) stopSet.add(String.valueOf(c));

            return words.stream()
                    .filter(w -> !stopSet.contains(w))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Map<String, Integer> frequencies(List<String> words) {
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words)
            freq.put(w, freq.getOrDefault(w, 0) + 1);
        return freq;
    }

    static List<Map.Entry<String, Integer>> sort(Map<String, Integer> freq) {
        return freq.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());
    }

    static String top25Freqs(List<Map.Entry<String, Integer>> entries) {
        StringBuilder sb = new StringBuilder();
        int n = Math.min(25, entries.size());
        for (int i = 0; i < n; i++) {
            Map.Entry<String, Integer> e = entries.get(i);
            sb.append(e.getKey()).append(" - ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Ten <input_file>");
            System.exit(1);
        }

        new TheOne<>(args[0])
                .bind(Ten::readFile)
                .bind(Ten::filterChars)
                .bind(Ten::normalize)
                .bind(Ten::scan)
                .bind(Ten::removeStopWords)
                .bind(Ten::frequencies)
                .bind(Ten::sort)
                .bind(Ten::top25Freqs)
                .printme();
    }
}