import java.util.Arrays;

public class Difficulty {
    int[][] board;
    int size;

    public Difficulty(int[][] board) {
        this.size = board.length;

        this.board = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.board[i][j] = board[i][j];
            }
        }
    }

    public double getScore() {
        double scoreTotal;

        if (this.board.length == 4) {
            scoreTotal = (scoreK3(board) * 0.5) + (scoreK4(board) * 0.5);
        } else {
            scoreTotal = (scoreK1(board) * 0.25) + (scoreK2(board) * 0.25) + (scoreK3(board) * 0.25)
                    + (scoreK4(board) * 0.25);
        }

        return scoreTotal;
    }

    public double scoreK1(int[][] board) {
        double res;
        double invalidCnt = 0;
        double potentialCnt = 0;

        int n = board.length;

        // hitung depan bawah , both
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int val = board[i][j];
                if (val == 0)
                    continue;

                for (int x = i; x < n; x++) {
                    int startCol = 0;
                    if (x == i)
                        startCol = j + 1;

                    for (int y = startCol; y < n; y++) {
                        if (board[x][y] == val) {
                            int dist = Math.abs(x - i) + Math.abs(y - j);
                            if (dist <= val)
                                potentialCnt++;
                            if (dist == val)
                                invalidCnt++;
                        }
                    }
                }
            }
        }
        if (potentialCnt == 0)
            return 0;

        res = invalidCnt / potentialCnt;
        return res;
    }

    public int scoreK2(int[][] board) {
        int psgAdder;
        double psgDivider;

        int count = 0;

        if (board.length == 4) {
            psgAdder = 1;
            psgDivider = 4.0;
        } else {
            psgAdder = 2;
            psgDivider = 9.0;
        }

        // simpan sel yang sdh dipakai
        boolean[][] skipped = new boolean[board.length][board[0].length];

        // loop per 2x2 / 3x3
        for (int i = 0; i < board.length - psgAdder; i++) {
            for (int j = 0; j < board.length - psgAdder; j++) {

                // jika sdh di cek langsung ke index berikutnya
                if (skipped[i][j])
                    continue;

                int awalRow = i;
                int akhirRow = i + psgAdder;
                int awalCol = j;
                int akhirCol = j + psgAdder;
                double highest;

                // jk indeks out of bounds ke index berikutnya
                if (akhirRow >= board.length || akhirCol >= board.length) {
                    continue;
                }

                // hitung "tetangga" yang berisi
                int countPSG = countBigNumber(board, awalRow, akhirRow, awalCol, akhirCol);
                highest = countPSG / psgDivider;

                if (highest >= 0.75) {
                    count++;

                    // ganti status tetangga
                    for (int r = awalRow; r <= akhirRow && r < board.length; r++) {
                        for (int c = awalCol; c <= akhirCol && c < board[0].length; c++) {
                            skipped[r][c] = true;
                        }
                    }
                }
            }
        }

        return count;
    }

    // hitung "tetangga" yang besar
    public static int countBigNumber(int[][] board, int awalRow, int akhirRow, int awalCol, int akhirCol) {
        int count = 0;
        for (int r = awalRow; r <= akhirRow && r < board.length; r++) {
            for (int c = awalCol; c <= akhirCol && c < board[0].length; c++) {
                if (r >= 0 && c >= 0 && board[r][c] > 4) {
                    count++;
                }
            }
        }
        return count;
    }

    public double scoreK3(int[][] board) {
        double cntSudut = 0;
        double cntTotal = 0;

        int n = board.length;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] != 0) {
                    cntTotal++;
                    // dekat sudut = baris pertama/terakhir ATAU kolom pertama/terakhir
                    if (i == 0 || i == n - 1 || j == 0 || j == n - 1) {
                        cntSudut++;
                    }
                }
            }
        }

        if (cntTotal == 0)
            return 0; // hindari div 0

        return cntSudut / cntTotal;
    }

    public int scoreK4(int[][] board) {
        int psgAdder;
        double psgDivider;

        int count = 0;

        if (board.length == 4) {
            psgAdder = 1;
            psgDivider = 4.0;
        } else {
            psgAdder = 2;
            psgDivider = 9.0;
        }

        for (int i = 0; i < board.length - psgAdder; i++) {
            for (int j = 0; j < board.length - psgAdder; j++) {

                int awalRow = i;
                int akhirRow = i + psgAdder;
                int awalCol = j;
                int akhirCol = j + psgAdder;
                double highest;

                if (akhirRow >= board.length || akhirCol >= board.length) {
                    continue;
                }

                // Hitung "tetangga" pada 2x2
                int count2x2 = countGreaterThanZero(board, awalRow, akhirRow, awalCol, akhirCol);
                highest = count2x2 / psgDivider;

                if (highest >= 0.75) {
                    count++;
                    i = akhirRow;
                    j = akhirCol;
                }
            }
        }
        return count;
    }

    // Hitung "tetangga"
    public static int countGreaterThanZero(int[][] board, int awalRow, int akhirRow, int awalCol, int akhirCol) {
        int count = 0;
        for (int r = awalRow; r <= akhirRow && r < board.length; r++) {
            for (int c = awalCol; c <= akhirCol && c < board[0].length; c++) {
                if (r >= 0 && c >= 0 && board[r][c] > 0) {
                    count++;
                }
            }
        }
        return count;
    }

}
