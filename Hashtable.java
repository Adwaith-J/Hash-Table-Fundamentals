import java.util.*;

public class PlagiarismDetector {

    private HashMap<String, Set<String>> index = new HashMap<>();

    private int N = 5; 

    private List<String> extractNGrams(String text) {

        String[] words = text.toLowerCase().split("\\s+");
        List<String> grams = new ArrayList<>();

        for (int i = 0; i <= words.length - N; i++) {

            StringBuilder gram = new StringBuilder();

            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }

            grams.add(gram.toString().trim());
        }

        return grams;
    }

    public void indexDocument(String docId, String text) {

        List<String> grams = extractNGrams(text);

        for (String gram : grams) {

            index.putIfAbsent(gram, new HashSet<>());
            index.get(gram).add(docId);
        }
    }
    public void analyzeDocument(String docId, String text) {

        List<String> grams = extractNGrams(text);

        HashMap<String, Integer> similarityCounter = new HashMap<>();

        for (String gram : grams) {

            if (index.containsKey(gram)) {

                for (String existingDoc : index.get(gram)) {

                    similarityCounter.put(
                        existingDoc,
                        similarityCounter.getOrDefault(existingDoc, 0) + 1
                    );
                }
            }
        }

        System.out.println("Extracted " + grams.size() + " n-grams");

        for (String doc : similarityCounter.keySet()) {

            int matches = similarityCounter.get(doc);

            double similarity = (matches * 100.0) / grams.size();

            System.out.println(
                "Found " + matches + " matching n-grams with " + doc +
                " → Similarity: " + String.format("%.2f", similarity) + "%"
            );
        }
    }

    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        detector.indexDocument(
            "essay_092.txt",
            "machine learning models improve prediction accuracy using training data"
        );

        detector.indexDocument(
            "essay_089.txt",
            "deep learning models improve prediction performance with neural networks"
        );

        detector.analyzeDocument(
            "essay_123.txt",
            "machine learning models improve prediction accuracy using training datasets"
        );
    }
}