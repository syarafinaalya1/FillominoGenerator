import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

class Board {

    int size;
    List<ArrayList<Pair>> groups = new ArrayList<>();
    Boolean[] checkedNB = new Boolean[4];
    int[][] board;

    public Board(int size) {
        this.size = size;
        this.board = new int[size][size];
    }

    private void generateBoard() {// implement board generation
        Random rd = new Random(size);
        int row, col;
        int rowNB, colNB;

        while (isEmpty(board)) {
            row = rd.nextInt();
            col = rd.nextInt();
            int total = 0;

            // jika tidak punya tetangga
            if (countNB(row, col, board) == 0) {
                board[row][col] = 1;
            }

            // jika punya 1 tetangga
            else if (countNB(row, col, board) == 1) {

                // lakukan pencarian neighbor dan dapatkan posisinya
                if (board[row - 1][col] > 0) {
                    rowNB = row - 1;
                    colNB = col;
                } else if (board[row + 1][col] > 0) {
                    rowNB = row + 1;
                    colNB = col;
                } else if (board[row][col - 1] > 0) {
                    rowNB = row;
                    colNB = col - 1;
                } else {
                    rowNB = row;
                    colNB = col + 1;
                }

                total = board[rowNB][colNB] + 1;

                board[rowNB][colNB] = total;
                board[row][col] = total;

            }

            // jika tetangga lebih dari 1

        }
    }

    private boolean isEmpty(int[][] board) {
        boolean res = false;
        int i = 0;
        int j = 0;

        while (res == false && i <= board.length) {
            while (res == false && j <= board.length) {
                if (board[i][j] > 0) {
                    res = true;
                    break;
                } else {
                    j++;
                }
            }
            i++;
        }

        return res;
    }

    private void mergeGroup(int row, int col, int rowNB, int colNB, int total, List<ArrayList<Pair>> groups) {
        // implement merging logic

        int NBidx = -1;

        for (int i = 0; i < groups.size(); i++) {
            for (Pair p : groups.get(i)) {
                if (p.row == rowNB && p.col == colNB) {
                    NBidx = i;
                    break;
                }
            }
            if (NBidx != -1)
                break;
        }

        if (NBidx == -1) {
            System.out.println("No group contains (" + rowNB + "," + colNB + ")");
            return;
        }

        ArrayList<Pair> NBGroup = groups.get(NBidx);

        // add row1,col1 into the group if not exists
        Pair newPair = new Pair(row, col, total);
        if (!NBGroup.contains(newPair)) {
            NBGroup.add(newPair);
        }

        // update all members in the group with new total
        for (Pair p : NBGroup) {
            p.value = total;
        }
    }

    // mencari grup neighbor, lalu update semua grup dengan nilai total yang baru

    private boolean haveNB() {
        boolean res = false;
        return res;
    }

    private void findNB(int row, int col) {
        // lakukan loop atas, kanan, kiri, bawah, mengembalikan angka petunjuk dimana
        // tetangga itu berada
        Random rd = new Random(4);

        // random neighbor, lalu cek jika dia sudah di cek atau belum

        int rowNB = rd.nextInt(board.length);
        int colNB = rd.nextInt(board.length);

        if (rowNB == row || colNB == col) {
            while (rowNB == row || colNB == col) {
                rowNB = rd.nextInt(board.length);
                colNB = rd.nextInt(board.length);
            }
        }

    }

    private int countNB(int row, int col, int[][] board) {
        // untuk mengecek jika sel mempunyai tetangga atau tidak sekaligus
        // menghitungjika iya, berapa tetangga yang dimilikinya
        int res = 0;

        if (board[row - 1][col] > 0) {
            res++;
        } else if (board[row + 1][col] > 0) {
            res++;
        } else if (board[row][col - 1] > 0) {
            res++;
        } else if (board[row + 1][col + 1] > 0) {
            res++;
        }

        return res;
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Board b = new Board(5);
        sc.close();
    }
}
