import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sn = new Scanner(System.in);
        // double score;

        System.out.println("Masukkan ukuran:");
        int size = sn.nextInt();
        int[][] papan = new int[size][size];

        Board b = new Board(size);
        // Difficulty d = new Difficulty(papan, size);

        papan = b.generateBoard();

        // papan = b.pruneBoard(papan);
        // score = d.getScore();

        printBoard(papan);
        // System.out.println(score);
        // System.out.println(d.scoreK1(papan));
        // System.out.println(d.scoreK2(papan));
        // System.out.println(d.scoreK3(papan));
        // System.out.println(d.scoreK4(papan));

    }

    static void printBoard(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
}