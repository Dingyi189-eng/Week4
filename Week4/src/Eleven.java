import java.io.IOException;
import java.nio.file.*;
import java.util.*;

class EventFramework {
    private List<LoadEvent> loadEventHandlers = new ArrayList<>();
    private List<DoworkEvent> doworkEventHandlers = new ArrayList<>();
    private List<EndEvent> endEventHandlers = new ArrayList<>();

    interface LoadEvent { void call(String path); }
    interface DoworkEvent { void call(); }
    interface EndEvent { void call(); }

    void registerForLoadEvent(LoadEvent handler) { loadEventHandlers.add(handler); }
    void registerForDoworkEvent(DoworkEvent handler) { doworkEventHandlers.add(handler); }
    void registerForEndEvent(EndEvent handler) { endEventHandlers.add(handler); }

    void run(String pathToFile) {
        for (LoadEvent h : loadEventHandlers) h.call(pathToFile);
        for (DoworkEvent h : doworkEventHandlers) h.call();
        for (EndEvent h : endEventHandlers) h.call();
    }
}


class StopWordFilter {
    private Set<String> stopWords = new HashSet<>();

    StopWordFilter(EventFramework wf) {
        wf.registerForLoadEvent(this::load);
    }

    private void load(String ignore) {
        try {
            // 使用当前目录的 stop_words.txt
            String data = Files.readString(Paths.get("stop_words.txt"));
            stopWords.addAll(Arrays.asList(data.split(",")));
            for (char c = 'a'; c <= 'z'; c++) stopWords.add(String.valueOf(c));
        } catch (IOException e) {
            System.err.println("Error reading stop_words.txt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    boolean isStopWord(String word) { return stopWords.contains(word); }
}

class DataStorage {
    private String data = "";
    private StopWordFilter stopFilter;
    private List<WordEvent> wordEventHandlers = new ArrayList<>();

    interface WordEvent { void call(String word); }

    DataStorage(EventFramework wf, StopWordFilter stopFilter) {
        this.stopFilter = stopFilter;
        wf.registerForLoadEvent(this::load);
        wf.registerForDoworkEvent(this::produceWords);
    }

    void registerForWordEvent(WordEvent handler) { wordEventHandlers.add(handler); }

    private void load(String pathToFile) {
        try {
            data = Files.readString(Paths.get(pathToFile));
            data = data.replaceAll("[\\W_]+", " ").toLowerCase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void produceWords() {
        for (String w : data.split("\\s+")) {
            if (!stopFilter.isStopWord(w)) {
                for (WordEvent h : wordEventHandlers) h.call(w);
            }
        }
    }
}

class WordFrequencyCounter {
    private Map<String, Integer> wordFreqs = new HashMap<>();

    WordFrequencyCounter(EventFramework wf, DataStorage ds) {
        ds.registerForWordEvent(this::incrementCount);
        wf.registerForEndEvent(this::printFreqs);
    }

    private void incrementCount(String word) {
        wordFreqs.put(word, wordFreqs.getOrDefault(word, 0) + 1);
    }

    private void printFreqs() {
        wordFreqs.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(25)
            .forEach(e -> System.out.println(e.getKey() + " - " + e.getValue()));
    }
}

public class Eleven {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Eleven <input_file>");
            System.exit(1);
        }

        EventFramework wf = new EventFramework();
        StopWordFilter sw = new StopWordFilter(wf);
        DataStorage ds = new DataStorage(wf, sw);
        WordFrequencyCounter counter = new WordFrequencyCounter(wf, ds);
        wf.run(args[0]);
    }
}
