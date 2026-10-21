import java.util.Arrays;
import java.util.Comparator;

public class BinarySearchSortingDemo {
    public static void main(String[] args) {
        runAndPrint();
    }

    public static void runAndPrint() {
        Integer[] values = { 42, 7, 19, 3, 27, 12 };
        Integer target = 19;
        Comparator<Integer> comparator = Comparator.naturalOrder();

        Sorters.SearchContext<Integer> searchContext =
                new Sorters().new SearchContext<>(Sorters.mergeSort());
        int iterativeIndex = searchContext.search(values, target, comparator, true);
        int recursiveIndex = searchContext.search(values, target, comparator, false);

        System.out.println("Sorted values: " + Arrays.toString(values));
        System.out.println("Iterative search index for " + target + ": " + iterativeIndex);
        System.out.println("Recursive search index for " + target + ": " + recursiveIndex);
    }
}