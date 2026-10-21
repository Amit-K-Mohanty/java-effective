import java.util.Comparator;

class Algo {
    // 0(N^2) Sorting

    public static <T> void insertionSort(T[] array, Comparator<? super T> comparator) {
        if (array == null || array.length <= 1)
            return;
        for (int i = 0; i < array.length; i++) {
            T key = array[i];
            int j = i - 1;
            while (j >= 0 && comparator.compare(array[j], key) > 0) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }

    // Merge Sort
    public static <T> void mergeSort(T[] array, Comparator<? super T> comparator) {
        if (array == null || array.length <= 1)
            return;
        mergeSortRecc(array, 0, array.length - 1, comparator);
    }

    public static <T> void mergeSortRecc(T[] array, int left, int right, Comparator<? super T> comparator) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortRecc(array, left, mid, comparator);
            mergeSortRecc(array, mid + 1, right, comparator);
            merge(array, left, mid, right, comparator);

        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void merge(T[] array, int left, int mid, int right, Comparator<? super T> comparator) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        T[] leftArray = (T[]) new Object[n1];
        T[] rightArray = (T[]) new Object[n2];

        System.arraycopy(array, left, leftArray, 0, n1);
        System.arraycopy(array, mid + 1, rightArray, 0, n2);

        int i = 0;
        int j = 0;
        int k = left;

        while (i < n1 && j < n2) {
            if (comparator.compare(leftArray[i], rightArray[j]) <= 0) {
                array[k++] = leftArray[i++];
            } else {
                array[k++] = rightArray[j++];
            }
        }
        while (i < n1)
            array[k++] = leftArray[i++];
        while (j < n2)
            array[k++] = rightArray[j++];

    }

    // Binary Search

    public static <T> int binarySearch(T[] array, T target, Comparator<? super T> comparator) {
        if (array == null)
            return -1;
        int low = 0;
        int high = array.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = comparator.compare(array[mid], target);
            if (cmp == 0)
                return mid;
            if (cmp < 0)
                low = mid + 1;
            else
                high = mid - 1;
        }
        return -1;

    }

    // Binary Recursive

    public static <T> int binarySearchRec(T[] array, T target, Comparator<? super T> comparator) {
        if (array == null)
            return -1;
        return binarySearchHelper(array, target, 0, array.length - 1, comparator);
    }

    public static <T> int binarySearchHelper(T[] array, T target, int low, int high, Comparator<? super T> comparator) {
        if (low > high)
            return -1;
        int mid = low + (high - low) / 2;
        int cmp = comparator.compare(array[mid], target);
        if (cmp == 0)
            return mid;
        if (cmp < 0)
            return binarySearchHelper(array, target, mid + 1, high, comparator);
        return binarySearchHelper(array, target, low, mid - 1, comparator);
    }

}

@FunctionalInterface
interface Sorter<T> {
    void sort(T[] array, Comparator<? super T> comparator);
}

class Sorters{
    public  static <T> Sorter<T> insertionSort(){
        return Algo::insertionSort;
    }

    public static <T> Sorter<T> mergeSort(){
       return Algo::mergeSort; 
    }

    class SearchContext<T> {
        private final Sorter<T> sorter;

        public SearchContext(Sorter<T> sorter){
            this.sorter = sorter;
        }

        public int search(T[] array, T target, Comparator<? super T> comparator, boolean iter){
            sorter.sort(array,comparator);

            if(iter){
                return Algo.binarySearch(array,target,comparator);
            }else{
                return  Algo.binarySearchRec(array,target,comparator);
            }
        }
    }
}
