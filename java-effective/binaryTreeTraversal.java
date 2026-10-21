import java.util.ArrayList;
import java.util.List;

class BinaryTreeTraversal<T> {
    public static class Node<T> {
        public T data;
        public Node<T> left;
        public Node<T> right;

        public Node(T data) {
            this.data = data;
        }
    }

    public List<T> orderTraversal(Node<T> root) {
        List<T> result = new ArrayList<>();
        orderTraversalRec(root, result);
        return result;
    }

    private void orderTraversalRec(Node<T> node, List<T> result) {
        if (node == null)
            return;
        orderTraversalRec(node.left, result);
        result.add(node.data);
        orderTraversalRec(node.right, result);
    }
}
