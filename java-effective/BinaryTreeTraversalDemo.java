public class BinaryTreeTraversalDemo {
    public static void main(String[] args) {
        runAndPrint();
    }

    public static void runAndPrint() {
        BinaryTreeTraversal.Node<Integer> root = new BinaryTreeTraversal.Node<>(4);
        root.left = new BinaryTreeTraversal.Node<>(2);
        root.right = new BinaryTreeTraversal.Node<>(6);
        root.left.left = new BinaryTreeTraversal.Node<>(1);
        root.left.right = new BinaryTreeTraversal.Node<>(3);
        root.right.left = new BinaryTreeTraversal.Node<>(5);
        root.right.right = new BinaryTreeTraversal.Node<>(7);

        BinaryTreeTraversal<Integer> traversal = new BinaryTreeTraversal<>();
        System.out.println("Inorder traversal: " + traversal.orderTraversal(root));
    }
}