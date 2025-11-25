
public class tesManhattan {
    public static void main(String[] args) {
        int[][] board = {
                { 1, 0, 0, 1 },
                { 0, 2, 2, 0 },
                { 2, 0, 1, 0 },
                { 0, 0, 1, 0 }
        };

        analyzePairs(board);
    }

    static void analyzePairs(int[][] board) {
        double res;
        double invalidCnt = 0;
        double potentialCnt = 0;

        int n = board.length;

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
        res = invalidCnt / potentialCnt;

        System.out.println("Potential pairs: " + potentialCnt);
        System.out.println("Invalid pairs: " + invalidCnt);
        System.out.println(res);
    }

}
