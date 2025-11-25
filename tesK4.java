public class tesK4 {

    public static void main(String[] args) {
        int[][] board = {
                { 1, 0, 0, 6 },
                { 2, 1, 0, 2 },
                { 2, 0, 1, 1 },
                { 0, 6, 0, 6 }

        };

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
                int count2x2 = countGreaterThanZero(board, awalRow, akhirRow, awalCol, akhirCol);
                highest = count2x2 / psgDivider;

                if (highest >= 0.75) {
                    count++;
                    i = akhirRow;
                    j = akhirCol;
                }
            }

            System.out.println();
        }

        System.out.println("Jumlah center : " + count);
    }

    // Hitung jumlah elemen > 0 dalam submatrix
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
