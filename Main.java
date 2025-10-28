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

        while (haveEmpty(notChecked)) {

            cell = notChecked.remove(idx); // mengambil index ke 0, langsung remove karena case ini sudah pasti terjadi
                                           // dan tidak akan dibatalkan
            int total = 0;

            // CASE 1 : jika tidak punya tetangga
            if (countNB(cell, board) == 0) {

                System.out.println("0 tetangga");

                cell.value = 1;
                board[cell.row][cell.col] = cell.value;

                ArrayList<Pair> newGroup = new ArrayList<>(); // tambahkan ke dalam grup
                newGroup.add(new Pair(cell.row, cell.col, cell.value));
                groups.add(newGroup);

                printBoard(board);
            }

            // CASE 2 : 1 tetangga
            else if (countNB(cell, board) == 1) {
                System.out.println("1 tetangga");
                cell.value = 1;
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

                total = NB.value + cell.value; // ditambah 1 karena ini iterasi pertama jadi pasti ditambah 1 (sel
                                               // berisi 1)

                // 2. update group baru dengan tetangga

                // CASE 1.1 : 1 tetanga & bisa merge
                if (total <= 9 && canMerge(NB, total, board, groups)) { // belum update cell | harunya new group cell
                                                                        // board

                    // cek dulu jika bisa merge setelah digrup

                    // 1. merge Group
                    groups = mergeGroup(NB, cell, total, groups);

                    // 2. remove Tetangga dari List
                    notChecked = removeFromNotChecked(cell, groups, notChecked);

                    // 3. Update board dari grup baru
                    board = updateBoard(groups, NB);

                    printBoard(board);
                }
                // CASE 1.2 : 1 tetanga & tidak bisa merge
                else {

                    board[cell.row][cell.col] = 1; // isi dengan 1

                    ArrayList<Pair> newGroup = new ArrayList<>(); // inisiasi grup baru dengan isi 1
                    newGroup.add(new Pair(cell.row, cell.col, 1));
                    groups.add(newGroup);

                    printBoard(board);
                }
            }

            // CASE 3 : Multiple Tetangga
            else if (countNB(cell, board) > 1) {

                System.out.println("Multi Tetangga");

                int arah = rd.nextInt(4);// mendapatkan tetangga yang belum di cek (0-3)
                boolean direction = true; // menyimpan arah, jika sudah di cek ini akan berhenti sel tidak akan di cek
                                          // lagi

                checkedNB = initiateCheckedNB(cell, checkedNB); // disini akan menfalse kan semua, sekaligus men-true
                                                                // kan yang out of bound agar tidak di cek

                cell.value = 1;
                board[cell.row][cell.col] = 1; // isi dengan 1

                ArrayList<Pair> newCell = new ArrayList<>(); // inisiasi grup baru dengan isi 1
                newCell.add(new Pair(cell.row, cell.col, 1));
                groups.add(newCell);

                printBoard(board);

                while (direction == true) {

                    // CASE 3.1 : Multi tetangga & !checked
                    if (checkedNB[arah] == false) {

                        checkedNB[arah] = true; // ubah state nb jadi sudah di cek
                        NB = mapDirection(arah, cell); // dapatkan NB
                        total = NB.value + cell.value;
                        // proses nb baru

                        if (total <= 9 && canMerge(NB, total, board, groups)) { // CASE 3.1.1 Multi tetangga & !checked
                                                                                // & Merge

                            // 1. Merge Group
                            groups = mergeGroup(NB, cell, total, groups);

                            // 2. Remove Tetangga dari List
                            notChecked = removeFromNotChecked(cell, groups, notChecked);

                            // 3. Update board dari grup baru
                            board = updateBoard(groups, NB);

                            cell.value = board[cell.row][cell.col];

                            printBoard(board);

                            while (haveXNeighbour(checkedNB, cell, total, board)) {

                                // 1 Ambil arah neighbor yang punya nilai sama dengan total
                                int arahNB = getNewNB(checkedNB, total, cell);

                                // 2️ Jika tidak ada neighbor valid, hentikan loop
                                if (arahNB == -1) {
                                    break;
                                }

                                // 3️ Dapatkan koordinat neighbor dari arah tersebut
                                NB = mapDirection(arahNB, cell);

                                // 4️ Tandai arah ini sudah dicek
                                checkedNB[arahNB] = true;

                                // 5️ Jika bisa digabung
                                if ((total + NB.value <= 9) && canMerge(NB, total, board, groups)) {
                                    total = total + NB.value;

                                    // 1. Merge Group
                                    groups = mergeGroup(NB, cell, total, groups);

                                    // 2. Remove Tetangga dari List
                                    notChecked = removeFromNotChecked(cell, groups, notChecked);

                                    // 3. Update board dari grup baru
                                    board = updateBoard(groups, NB);
                                    cell.value = total;

                                    printBoard(board);
                                }
                                // 6️ Jika tidak bisa digabung
                                else {
                                    int indx = getGroupIdx(NB, groups);
                                    ArrayList<Pair> removedGroup = groups.get(indx);

                                    groups = deleteGroup(NB, groups, board);

                                    if (removedGroup != null) {
                                        for (int i = 0; i < removedGroup.size(); i++) {
                                            Pair p = removedGroup.get(i);
                                            board[p.row][p.col] = 0;
                                            notChecked.add(p);
                                        }
                                        notChecked = shuffleOrder(notChecked);
                                    }

                                    printBoard(board);
                                }
                            }

                        } else { // CASE 3.1.2 Multi tetangga & !checked & !Merge

                            // 1. Cek dahulu jika semua sudah di cek
                            boolean semuaSudahDicek = true;

                            for (boolean b : checkedNB) {
                                if (b == false) {
                                    semuaSudahDicek = false;
                                    break;
                                }
                            }

                            // 2. Jika semua sudah di cek, loop berhenti. cell sudah beres.
                            // CASE 3.1.2.1 Multi tetangga & !checked & !Merge & Finish
                            if (semuaSudahDicek == true) {

                                direction = false; // pemberhentian loop, sudah tidak memiliki arah
                                if (cell.value == 0) {
                                    cell.value = 1;
                                    board[cell.row][cell.col] = 1;
                                }

                                ArrayList<Pair> newGroup = new ArrayList<>(); // tambahkan ke dalam grup
                                newGroup.add(new Pair(cell.row, cell.col, cell.value));
                                groups.add(newGroup);

                                printBoard(board);

                                total = 1;

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
                            if (cell.value == 0) {
                                cell.value = 1;
                                board[cell.row][cell.col] = 1;
                            }

                            ArrayList<Pair> newGroup = new ArrayList<>(); // tambahkan ke dalam grup
                            newGroup.add(new Pair(cell.row, cell.col, cell.value));
                            groups.add(newGroup);

                            printBoard(board);

                            total = 1;
                        }

                        // CASE 3.1.2.1 Multi tetangga & !checked & !Merge & !Finish
                        else { // cari NB baru
                            arah = rd.nextInt(4);
                        }
                    }

                }

            }

        }
        return board;

    }

    private boolean canMerge(Pair cell, int total, int[][] board, List<ArrayList<Pair>> groups) {
        // cari grup tempat cell berada
        int groupIdx = getGroupIdx(cell, groups);
        if (groupIdx == -1)
            return true; // belum punya grup -> aman

        boolean res = true;
        int[][] arah = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

        // loop setiap anggota grup
        for (Pair member : groups.get(groupIdx)) {
            for (int[] d : arah) {
                int r = member.row + d[0];
                int c = member.col + d[1];

                // skip jika keluar batas
                if (r < 0 || c < 0 || r >= board.length || c >= board[0].length)
                    continue;

                // skip jika tetangga masih bagian dari grup yang sama

                boolean sameGroup = false;
                for (Pair p : groups.get(groupIdx)) {
                    if (p.row == r && p.col == c) {
                        sameGroup = true;
                        break;
                    }
                }
                if (sameGroup == true) {
                    continue;
                }

                // kalau tetangga nilainya == total -> tidak boleh merge
                if (board[r][c] == total) {
                    res = false;
                    break;
                }
            }
        }

        return res;
    }

    private int getGroupIdx(Pair cell, List<ArrayList<Pair>> groups) {
        int updatedIdx = -1;
        for (int i = 0; i < groups.size(); i++) {
            for (Pair p : groups.get(i)) {
                if (p.row == cell.row && p.col == cell.col) {
                    updatedIdx = i;
                    break;
                }
            }
            if (updatedIdx != -1)
                break;
        }

        return updatedIdx;
    }

    private void printBoard(int[][] board) {
        System.out.println();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private int[][] updateBoard(List<ArrayList<Pair>> groups, Pair NB) {
        // cari index group tetangga (nb) setelah merge untuk sinkronisasi board
        int updatedIdx = getGroupIdx(NB, groups);
        // sinkronisasi board dengan group yang baru diupdate
        if (updatedIdx != -1) {
            for (Pair p : groups.get(updatedIdx)) {
                board[p.row][p.col] = p.value;
            }
        }

        return board;

    }

    private List<Pair> removeFromNotChecked(Pair target, List<ArrayList<Pair>> groups, List<Pair> notChecked) {
        for (int i = 0; i < groups.size(); i++) {
            ArrayList<Pair> group = groups.get(i);
            if (group.contains(target)) { // asumsinya Pair sudah override equals()
                notChecked.removeAll(group);
                break;
            }
        }
        return notChecked;
    }

    private List<Pair> shuffleOrder(List<Pair> order) {

        Collections.shuffle(order);

        return order;
    }

    private List<ArrayList<Pair>> deleteGroup(Pair x, List<ArrayList<Pair>> groups, int[][] board) {
        for (int i = 0; i < groups.size(); i++) {
            ArrayList<Pair> group = groups.get(i);
            for (int j = 0; j < group.size(); j++) {
                Pair p = group.get(j);
                if (p.row == x.row && p.col == x.col) {
                    groups.remove(i);
                    return groups;
                }
            }
        }
        return groups;
    }

    private int getNewNB(Boolean[] checkedNb, int total, Pair cell) {
        int n = board.length;
        List<Integer> candidates = new ArrayList<>();

        int[][] dirs = { { -1, 0 }, { 0, -1 }, { 0, 1 }, { 1, 0 } }; // atas, kiri, kanan, bawah

        for (int i = 0; i < 4; i++) {
            if (checkedNb[i])
                continue;

            int nr = cell.row + dirs[i][0];
            int nc = cell.col + dirs[i][1];

            if (nr >= 0 && nr < n && nc >= 0 && nc < n && board[nr][nc] == total)
                candidates.add(i);
        }

        return candidates.isEmpty() ? -1 : candidates.get((int) (Math.random() * candidates.size()));
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

    private Pair mapDirection(int index, Pair cell) { // index: 0=up,1=left,2=right,3=down
        int r = cell.row;
        int c = cell.col;

        if (index == 0)
            r = r - 1; // up
        else if (index == 1)
            c = c - 1; // left
        else if (index == 2)
            c = c + 1; // right
        else if (index == 3)
            r = r + 1; // down
        else
            return new Pair(-1, -1, 0);

        return new Pair(r, c, board[r][c]);
    }

    private Boolean[] initiateCheckedNB(Pair cell, Boolean[] checkedNB) {
        for (int i = 0; i < checkedNB.length; i++) {
            checkedNB[i] = true;
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

        // tandai NB yang tidak ada value
        if (cell.row > 0 && board[cell.row - 1][cell.col] > 0) // idx 0
            checkedNB[0] = false;
        if (cell.row < board.length - 1 && board[cell.row + 1][cell.col] > 0)
            checkedNB[3] = false;
        if (cell.col > 0 && board[cell.row][cell.col - 1] > 0)
            checkedNB[1] = false;
        if (cell.col < board[0].length - 1 && board[cell.row][cell.col + 1] > 0)
            checkedNB[2] = false;

        return checkedNB;
    }

    private boolean haveEmpty(List<Pair> notChecked) {
        return !notChecked.isEmpty();
    }

    private List<ArrayList<Pair>> mergeGroup(Pair NB, Pair cell, int total, List<ArrayList<Pair>> groups) {
        int idx1 = getGroupIdx(cell, groups);
        int idx2 = getGroupIdx(NB, groups);

        // sama
        if (idx1 != -1 && idx1 == idx2)
            return groups;

        // hanya NB yang punya grup
        else if (idx1 == -1 && idx2 != -1) {
            ArrayList<Pair> g2 = groups.get(idx2);
            g2.add(new Pair(cell.row, cell.col, total));
            for (int i = 0; i < g2.size(); i++) {
                g2.get(i).value = total;
            }
        }

        // keduanya punya grup berbeda mk gabungkan
        else if (idx1 != -1 && idx2 != -1) {
            ArrayList<Pair> g1 = groups.get(idx1);
            ArrayList<Pair> g2 = groups.get(idx2);

            // tmbhkan g2 ke g1
            for (int i = 0; i < g2.size(); i++) {
                g1.add(g2.get(i));
            }

            // update
            for (int i = 0; i < g1.size(); i++) {
                g1.get(i).value = total;
            }
            groups.remove(idx2);
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

    public int[][] simplePruning() {
        Random rd = new Random();
        boolean selesai = false;

        while (!selesai) {
            // cek apakah semua grup ukurannya <= 2
            boolean semuaKecil = true;
            for (ArrayList<Pair> group : groups) {
                if (group.size() > 2) {
                    semuaKecil = false;
                    break;
                }
            }

            if (semuaKecil) {
                selesai = true; // hentikan loop
                continue;
            }

            // pilih grup secara acak
            int gIdx = rd.nextInt(groups.size());
            ArrayList<Pair> group = groups.get(gIdx);

            // kalau grup sisa 1, lewati
            if (group.size() <= 1)
                continue;

            // pilih anggota acak
            int pIdx = rd.nextInt(group.size());
            Pair target = group.get(pIdx);

            // hapus dari board dan grup
            board[target.row][target.col] = 0;
            group.remove(pIdx);
        }

        return board;
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
        papan = b.simplePruning();

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
