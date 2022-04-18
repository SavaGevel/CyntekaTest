import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    private final static Path INPUT_FILE_PATH = Path.of("src/main/resources/input.txt");
    private final static Path OUTPUT_FILE_PATH = Path.of("src/main/resources/output.txt");

    public static void main(String[] args) {

        try {
            List<String> lines = Files.readAllLines(INPUT_FILE_PATH);
            Files.write(OUTPUT_FILE_PATH, getUniquePares(lines));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static List<String> getUniquePares(List<String> lines) throws IOException {

        int n = Integer.parseInt(lines.get(0));
        int m = Integer.parseInt(lines.get(n + 1));


        List<String> firstGroup = new LinkedList<>(lines.subList(1, n + 1));
        List<String> secondGroup = new LinkedList<>(lines.subList(n + 2, n + 2 + m));

        List<String> sortedPares = getAllPossibleParesSortedBySimilarity(firstGroup, secondGroup);
        List<String> result = new LinkedList<>();

        for(String line : firstGroup) {
            if(!secondGroup.isEmpty()) {
                String pare = sortedPares.stream().filter(l -> l.contains(line)).findFirst().get();
                result.add(pare);
                sortedPares = sortedPares.stream()
                        .filter(l -> !l.contains(line))
                        .filter(l -> !l.contains(pare.split(":")[1]))
                        .toList();
                secondGroup.remove(pare.split(":")[1]);
            } else {
                result.add(line + ":?");
            }
        }

        if(!secondGroup.isEmpty()) {
            for(String line : secondGroup) {
                result.add(line + ":?");
            }
        }
        return result;
    }

    private static List<String> getAllPossibleParesSortedBySimilarity(List<String> firstGroup, List<String> secondGroup) throws IOException {
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();

        Map<String, Integer> pares = new HashMap<>();
        for(String lineFromFirstGroup : firstGroup) {
            for(String lineFromSecondGroup : secondGroup) {
                int relevance = lineComparator(lineFromFirstGroup, lineFromSecondGroup, luceneMorphology);
                pares.put(lineFromFirstGroup + ":" + lineFromSecondGroup, relevance);
            }
        }
        return pares.keySet().stream().sorted(Comparator.comparing(pares::get).reversed()).toList();
    }

    private static int lineComparator(String firstLine, String secondLine, LuceneMorphology luceneMorphology) {

        String NOT_RUSSIAN_SYMBOL = "[^А-Яа-я]+";

        int count = 0;

        List<String> firstLineLemmas = Arrays.stream(firstLine.split("\\s"))
                .map(String::toLowerCase)
                .map(word -> word.replaceAll(NOT_RUSSIAN_SYMBOL, ""))
                .filter(word -> !word.isEmpty())
                .map(luceneMorphology::getNormalForms)
                .flatMap(List::stream)
                .toList();

        List<String> secondLineLemmas = Arrays.stream(secondLine.split("\\s"))
                .map(String::toLowerCase)
                .map(word -> word.replaceAll(NOT_RUSSIAN_SYMBOL, ""))
                .filter(word -> !word.isEmpty())
                .map(luceneMorphology::getNormalForms)
                .flatMap(List::stream)
                .toList();

        for(String lemmaFromFirstLine : firstLineLemmas) {
            if(secondLineLemmas.contains(lemmaFromFirstLine)) {
                count++;
            }
        }

        return count;
    }


}
