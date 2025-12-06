import java.util.*;

public class Main {

    public static void main(String[] args) {
        // new UI(); // Jalankan UI
        Scanner sn = new Scanner(System.in);

        int size = sn.nextInt();

        int[][] papan = new int[size][size];

        Board b = new Board(size);

        papan = b.generateBoard();
        printBoard(papan);
        System.out.println();

        papan = b.pruneBoard(papan);
        Difficulty d = new Difficulty(papan);
        System.out.println("k1:" + d.scoreK1(d.board));
        System.out.println("k2:" + d.scoreK2(d.board));
        System.out.println("k3:" + d.scoreK3(d.board));
        System.out.println("k4:" + d.scoreK4(d.board));
        double score = d.getScore();
        System.out.println(score);

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