import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

class Board {

    int size;
    List<ArrayList<Pair>> groups = new ArrayList<>();

    int[][] board;

    public Board(int size) {
        this.size = size;
        this.board = new int[size][size];
    }

    public int[][] generateBoard() {// implement board generation
        Random rd = new Random();

        Pair cell;
        Pair NB;
        int idx = 0;

        Boolean[] checkedNB = new Boolean[4];
        List<Pair> notChecked = new ArrayList<>();

        // initiate notchecked dengan isi masing masing 0
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                notChecked.add(new Pair(r, c, 0));
            }
        }
        notChecked = shuffleOrder(notChecked);

        while (notFull(notChecked)) {

            cell = notChecked.remove(idx); // mengambil index ke 0, langsung remove karena case ini sudah pasti terjadi
                                           // dan tidak akan dibatalkan
            int total = 0;

            // CASE 1 : jika tidak punya tetangga
            if (countNB(cell, board) == 0) {
                cell.value = 1;
                board[cell.row][cell.col] = cell.value;

                ArrayList<Pair> newGroup = new ArrayList<>(); // tambahkan ke dalam grup
                newGroup.add(new Pair(cell.row, cell.col, cell.value));
                groups.add(newGroup);
            }

            // CASE 2 : 1 tetangga
            else if (countNB(cell, board) == 1) {
                NB = null;

                // 1. dapatkan Letak tetangga (dengan handling inbound)
                if (cell.row > 0 && board[cell.row - 1][cell.col] > 0)
                    NB = new Pair(cell.row - 1, cell.col, board[cell.row - 1][cell.col]);
                else if (cell.row < size - 1 && board[cell.row + 1][cell.col] > 0)
                    NB = new Pair(cell.row + 1, cell.col, board[cell.row + 1][cell.col]);
                else if (cell.col > 0 && board[cell.row][cell.col - 1] > 0)
                    NB = new Pair(cell.row, cell.col - 1, board[cell.row][cell.col - 1]);
                else if (cell.col < size - 1 && board[cell.row][cell.col + 1] > 0) {
                    NB = new Pair(cell.row, cell.col + 1, board[cell.row][cell.col + 1]);
                } else {
                    NB = new Pair(-1, -1, 0);
                }

                total = NB.value + 1;

                // 2. update group baru dengan tetangga

                // CASE 1.1 : 1 tetanga & bisa merge
                if (total <= 9) { // belum update cell | harunya new group cell board

                    groups = mergeGroup(NB.row, NB.col, cell.row, cell.col, total, groups); // udah update board, new
                                                                                            // group
                    notChecked = removeFromNotChecked(cell, groups, notChecked);// tambahkan fungsi remove grup dan
                                                                                // semua yang trgabung dari notCheck
                    board = updateBoard(groups, NB);

                }
                // CASE 1.2 : 1 tetanga & tidak bisa merge
                else {
                    cell.value = 1;
                    board[cell.row][cell.col] = 1; // isi dengan 1

                    ArrayList<Pair> newGroup = new ArrayList<>(); // inisiasi grup baru dengan isi 1
                    newGroup.add(new Pair(cell.row, cell.col, 1));
                    groups.add(newGroup);

                }
            }

            // CASE 3 : Multiple Tetangga
            else if (countNB(cell, board) > 1) {
                int arah = rd.nextInt(4);// mendapatkan tetangga yang belum di cek (0-3)
                boolean direction = true; // menyimpan arah, jika sudah di cek ini akan berhenti sel tidak akan di cek
                                          // lagi

                checkedNB = initiateCheckedNB(cell, checkedNB); // disini akan menfalse kan semua, sekaligus men-true
                                                                // kan yang out of bound agar tidak di cek

                while (direction == true) {

                    // CASE 3.1 : Multi tetangga & !checked
                    if (checkedNB[arah] == false) {

                        checkedNB[arah] = true; // ubah state nb jadi sudah di cek
                        NB = mapDirection(arah, cell); // dapatkan NB

                        total = NB.value + cell.value; // proses nb baru

                        if (total <= 9) { // CASE 3.1.1 Multi tetangga & !checked & Merge

                            // 1. Merge Group
                            groups = mergeGroup(NB.row, NB.col, cell.row, cell.col, total, groups);

                            // 2. Remove Tetangga dari List
                            notChecked = removeFromNotChecked(cell, groups, notChecked);

                            // 3. Update board dari grup baru
                            board = updateBoard(groups, NB);

                            while (haveXNeighbour(checkedNB, cell, total, board)) {

                                // 1️⃣ Ambil arah neighbor yang punya nilai sama dengan total
                                int arahNB = getNewNB(checkedNB, total, cell);

                                // 2️⃣ Jika tidak ada neighbor valid, hentikan loop
                                if (arahNB == -1) {
                                    break;
                                }

                                // 3️⃣ Dapatkan koordinat neighbor dari arah tersebut
                                NB = mapDirection(arahNB, cell);

                                // 4️⃣ Tandai arah ini sudah dicek
                                checkedNB[arahNB] = true;

                                // 5️⃣ Jika bisa digabung
                                if (total + NB.value <= 9) {
                                    total = total + NB.value;
                                    groups = mergeGroup(NB.row, NB.col, cell.row, cell.col, total, groups);
                                    board = updateBoard(groups, NB);
                                    notChecked = removeFromNotChecked(cell, groups, notChecked);
                                }
                                // 6️⃣ Jika tidak bisa digabung
                                else {
                                    ArrayList<Pair> removedGroup = null;

                                    for (ArrayList<Pair> g : groups) { // ambil removed groups
                                        for (Pair p : g) {
                                            if (p.row == NB.row && p.col == NB.col) {
                                                removedGroup = new ArrayList<>();
                                                for (Pair member : g) {
                                                    removedGroup.add(new Pair(member.row, member.col, 0));
                                                }
                                                break;
                                            }
                                        }
                                        if (removedGroup != null)
                                            break;
                                    }

                                    groups = deleteGroup(NB, groups, board);

                                    if (removedGroup != null) {
                                        for (Pair p : removedGroup) {
                                            board[p.row][p.col] = 0;
                                        }

                                        for (Pair p : removedGroup) {
                                            notChecked.add(p);
                                        }

                                        notChecked = shuffleOrder(notChecked);
                                    }
                                }
                            }

                        } else { // CASE 3.1.2 Multi tetangga & !checked & !Merge

                            // 1. Cek dahulu jika semua sudah di cek
                            boolean semuaSudahDicek = true;
                            for (boolean b : checkedNB) {
                                if (!b) {
                                    semuaSudahDicek = false;
                                    break;
                                }
                            }

                            // 2. Jika semua sudah di cek, loop berhenti. cell sudah beres.
                            // CASE 3.1.2.1 Multi tetangga & !checked & !Merge & Finish
                            if (semuaSudahDicek) {

                                direction = false; // pemberhentian loop, sudah tidak memiliki arah
                                cell.value = 1;
                                board[cell.row][cell.col] = 1; // isi dengan satu masuk CASE : Memiliki tetangga &
                                                               // tetangga sudah di cek semua

                                ArrayList<Pair> newGroup = new ArrayList<>(); // tambahkan ke dalam grup
                                newGroup.add(new Pair(cell.row, cell.col, cell.value));
                                groups.add(newGroup);
                            }

                            // CASE 3.1.2.1 Multi tetangga & !checked & !Merge & !Finish
                            else { // cari NB baru
                                arah = rd.nextInt(4);
                            }
                        }
                        direction = false;
                    }

                    // CASE 3.2 : Multi tetangga & checked
                    else {
                        arah = rd.nextInt(4);

                        // 1. Cek dahulu jika semua sudah di cek
                        boolean semuaSudahDicek = true;
                        for (boolean b : checkedNB) {
                            if (!b) {
                                semuaSudahDicek = false;
                                break;
                            }
                        }

                        // 2. Jika semua sudah di cek, loop berhenti. cell sudah beres.
                        // CASE 3.2.1 Multi tetangga & checked & finish
                        if (semuaSudahDicek) {
                            direction = false; // pemberhentian loop, sudah tidak memiliki arah
                            board[cell.row][cell.col] = 1;
                            idx = notChecked.indexOf(cell);

                            ArrayList<Pair> newGroup = new ArrayList<>(); // tambahkan ke dalam grup
                            newGroup.add(new Pair(cell.row, cell.col, cell.value));
                            groups.add(newGroup);
                        }

                        // CASE 3.2.1 Multi tetangga & !checked & !finish
                        else { // cari NB baru
                            arah = rd.nextInt(4);
                        }
                    }

                }

            }

        }
        return board;

    }

    private int[][] updateBoard(List<ArrayList<Pair>> groups, Pair NB) {
        // cari index group tetangga (nb) setelah merge untuk sinkronisasi board
        int updatedIdx = -1;
        for (int i = 0; i < groups.size(); i++) {
            for (Pair p : groups.get(i)) {
                if (p.row == NB.row && p.col == NB.col) {
                    updatedIdx = i;
                    break;
                }
            }
            if (updatedIdx != -1)
                break;
        }

        // sinkronisasi board dengan group yang baru diupdate
        if (updatedIdx != -1) {
            for (Pair p : groups.get(updatedIdx)) {
                board[p.row][p.col] = p.value;
            }
        }

        return board;

    }

    private List<Pair> removeFromNotChecked(Pair target, List<ArrayList<Pair>> groups, List<Pair> notChecked) {
        for (ArrayList<Pair> group : groups) {
            for (Pair p : group) {
                if (p.row == target.row && p.col == target.col) {
                    notChecked.removeIf(np -> group.stream().anyMatch(gp -> gp.row == np.row && gp.col == np.col));
                    return notChecked;
                }
            }
        }
        return notChecked;
    }

    private List shuffleOrder(List<Pair> order) {

        Collections.shuffle(order);

        return order;
    }

    private List<ArrayList<Pair>> deleteGroup(Pair x, List<ArrayList<Pair>> groups, int[][] board) {
        ArrayList<Pair> targetGroup = null;

        for (ArrayList<Pair> group : groups) {
            for (Pair p : group) {
                if (p.row == x.row && p.col == x.col) {
                    targetGroup = group;
                    break;
                }
            }
            if (targetGroup != null)
                break;
        }

        if (targetGroup != null) {
            groups.remove(targetGroup);
        }

        return groups;
    }

    private int getNewNB(Boolean[] checkedNb, int total, Pair cell) {
        // Kembalikan salah satu arah (0..3) yang:
        // - belum dicek (checkedNb[i] == false)
        // - tetangga in-bounds
        // - nilai tetangga == total
        // jika tidak ada kandidat, return -1

        List<Integer> candidates = new ArrayList<>();
        int n = board.length;

        for (int i = 0; i < checkedNb.length; i++) {
            if (checkedNb[i])
                continue; // sudah dicek -> skip

            int nr = cell.row;
            int nc = cell.col;

            switch (i) {
                case 0:
                    nr = cell.row - 1;
                    break; // up
                case 1:
                    nc = cell.col - 1;
                    break; // left
                case 2:
                    nc = cell.col + 1;
                    break; // right
                case 3:
                    nr = cell.row + 1;
                    break; // down
            }

            if (nr < 0 || nr >= n || nc < 0 || nc >= n)
                continue; // OOB -> skip

            if (board[nr][nc] == total) {
                candidates.add(i);
            }
        }

        if (candidates.isEmpty())
            return -1;

        // pilih acak salah satu kandidat (agar tidak deterministik)
        Collections.shuffle(candidates);
        return candidates.get(0);
    }

    private Boolean haveXNeighbour(Boolean[] checkedNB, Pair cell, int x, int[][] board) {
        Boolean res = false;
        Pair NB;

        for (int i = 0; i < checkedNB.length; i++) {
            if (checkedNB[i] == false) {
                NB = (mapDirection(i, cell));

                if (NB.value == x) {
                    res = true; // kalau ada yang isinya sama dengan total
                }
            }
        }

        return res;

    }

    private int[][] makeBoard(List<ArrayList<Pair>> groups, int[][] board) {
        for (List<Pair> group : groups) { // loop setiap group
            for (Pair p : group) { // loop setiap Pair dalam group
                board[p.row][p.col] = p.value; // letakkan ke board
            }
        }

        return board;
    }

    private Pair mapDirection(int index, Pair cell) { // index: 0=up,1=left,2=right,3=down
        int r = cell.row;
        int c = cell.col;
        int n = board.length;

        int nr = r, nc = c;
        if (index == 0)
            nr = r - 1; // up
        else if (index == 1)
            nc = c - 1; // left
        else if (index == 2)
            nc = c + 1; // right
        else if (index == 3)
            nr = r + 1; // down
        else
            return new Pair(-1, -1, 0);

        return new Pair(nr, nc, board[nr][nc]);
    }

    private Boolean[] initiateCheckedNB(Pair cell, Boolean[] checkedNB) {
        for (int i = 0; i < checkedNB.length; i++) {
            checkedNB[i] = false;
        }

        int r = cell.row;
        int c = cell.col;
        int size = board.length;

        // tandai arah yang out of bound sebagai sudah dicek
        if (r == 0)
            checkedNB[0] = true; // up tidak ada
        if (c == 0)
            checkedNB[1] = true; // left tidak ada
        if (c == size - 1)
            checkedNB[2] = true; // right tidak ada
        if (r == size - 1)
            checkedNB[3] = true; // down tidak ada

        return checkedNB;
    }

    private Pair getRandomNB(Pair cell) {
        Random rd = new Random();
        int index = rd.nextInt(4); // 0..3
        return mapDirection(index, cell);
    }

    private boolean notFull(List<Pair> notChecked) {
        return !notChecked.isEmpty();
    }

    private List<ArrayList<Pair>> mergeGroup(int rowNB, int colNB, int row, int col, int total,
            List<ArrayList<Pair>> groups) {

        int idx1 = -1, idx2 = -1;

        // cari index grup untuk cell (row,col) dan neighbor (rowNB,colNB)
        for (int i = 0; i < groups.size(); i++) {
            for (Pair p : groups.get(i)) {
                if (p.row == row && p.col == col)
                    idx1 = i;
                if (p.row == rowNB && p.col == colNB)
                    idx2 = i;
            }
            if (idx1 != -1 && idx2 != -1)
                break;
        }

        // jika hanya salah satu yang punya grup
        if (idx1 != -1 && idx2 == -1) {
            groups.get(idx1).add(new Pair(rowNB, colNB, total));
            for (Pair p : groups.get(idx1))
                p.value = total;
            return groups;

        } else if (idx1 == -1 && idx2 != -1) {
            groups.get(idx2).add(new Pair(row, col, total));
            for (Pair p : groups.get(idx2))
                p.value = total;
            return groups;
        }

        // jika dua-duanya punya grup, gabungkan ke grup tetangga (idx2)
        if (idx1 != -1 && idx2 != -1 && idx1 != idx2) {
            ArrayList<Pair> group1 = groups.get(idx1);
            ArrayList<Pair> group2 = groups.get(idx2);

            group2.addAll(group1);
            for (Pair p : group2)
                p.value = total;

            groups.remove(idx1);
        }

        return groups;
    }

    private int countNB(Pair p, int[][] board) {
        // untuk mengecek jika sel mempunyai tetangga atau tidak sekaligus
        // menghitungjika iya, berapa tetangga yang dimilikinya
        int res = 0;

        if (p.row > 0 && board[p.row - 1][p.col] > 0)
            res++;
        if (p.row < board.length - 1 && board[p.row + 1][p.col] > 0)
            res++;
        if (p.col > 0 && board[p.row][p.col - 1] > 0)
            res++;
        if (p.col < board[0].length - 1 && board[p.row][p.col + 1] > 0)
            res++;

        return res;
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner sn = new Scanner(System.in);

        System.out.println("Masukkan ukuran:");
        int size = sn.nextInt();
        int[][] papan = new int[size][size];

        Board b = new Board(size);

        papan = b.generateBoard();

        printBoard(papan);

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
