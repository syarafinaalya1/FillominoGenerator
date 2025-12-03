import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Board {

    int size;
    List<ArrayList<Pair>> groups = new ArrayList<>();

    int[][] board;

    public Board(int size) {
        this.size = size;
        this.board = new int[size][size];
    }

    public int[][] generateBoard() {
        Random rd = new Random();

        Pair cell;
        Pair NB;
        int idx = 0;
        boolean idt = false;

        boolean[] checkedNB = new boolean[4];
        List<Pair> notChecked = new ArrayList<>();
        List<Pair> order = new ArrayList<>();
        List<Pair> inPending = new ArrayList<>();

        // initiate notChecked (reuse same Pair objects)
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                notChecked.add(new Pair(r, c, 0));
            }
        }
        notChecked = shuffleOrder(notChecked);

        while (idt == false) {
            while (haveEmpty(notChecked)) {

                cell = notChecked.remove(idx); // ambil pertama
                int total = 0;

                // CASE 1 : jika tidak punya tetangga
                if (countNB(cell, board) == 0) {

                    cell.value = 1;
                    board[cell.row][cell.col] = cell.value;

                    ArrayList<Pair> newGroup = new ArrayList<>();
                    newGroup.add(new Pair(cell.row, cell.col, cell.value));
                    groups.add(newGroup);

                }

                // CASE 2 : 1 tetangga
                else if (countNB(cell, board) == 1) {

                    cell.value = 1;
                    NB = null;

                    // dapatkan tetangga (in-bounds handling)
                    if (cell.row > 0 && board[cell.row - 1][cell.col] > 0)
                        NB = new Pair(cell.row - 1, cell.col, board[cell.row - 1][cell.col]);
                    else if (cell.row < size - 1 && board[cell.row + 1][cell.col] > 0)
                        NB = new Pair(cell.row + 1, cell.col, board[cell.row + 1][cell.col]);
                    else if (cell.col > 0 && board[cell.row][cell.col - 1] > 0)
                        NB = new Pair(cell.row, cell.col - 1, board[cell.row][cell.col - 1]);
                    else if (cell.col < size - 1 && board[cell.row][cell.col + 1] > 0)
                        NB = new Pair(cell.row, cell.col + 1, board[cell.row][cell.col + 1]);
                    else
                        NB = new Pair(-1, -1, 0);

                    total = NB.value + cell.value;

                    // CASE 2.1 : merge possible
                    if (total <= 9 && canMerge(NB, total, board, groups)) {
                        groups = mergeGroup(NB, cell, total, groups);
                        notChecked = removeFromNotChecked(cell, groups, notChecked);
                        board = updateBoard(groups, NB);

                    } else {
                        // buat grup baru berisi 1
                        board[cell.row][cell.col] = 1;
                        ArrayList<Pair> newGroup = new ArrayList<>();
                        newGroup.add(new Pair(cell.row, cell.col, 1));
                        groups.add(newGroup);

                    }
                } else if (countNB(cell, board) > 1) {

                    int arah = rd.nextInt(4);
                    boolean direction = true;
                    checkedNB = initiateCheckedNB(cell, checkedNB);
                    cell.value = 1;

                    ArrayList<Pair> newCell = new ArrayList<>();
                    newCell.add(new Pair(cell.row, cell.col, 1));
                    groups.add(newCell);

                    while (direction) {

                        // CASE 3.1 : arah belum dicek
                        if (!checkedNB[arah]) {

                            checkedNB[arah] = true;
                            NB = mapDirection(arah, cell);

                            // jika OOB atau kosong → skip
                            if (NB.row < 0 || NB.col < 0 || NB.row >= board.length || NB.col >= board.length
                                    || NB.value <= 0) {
                                arah = rd.nextInt(4);
                                continue;
                            }

                            total = NB.value + cell.value;

                            // jika bisa merge
                            if (total <= 9 && canMerge(NB, total, board, groups)) {

                                // tapi ada neighbour lain yg conflict → pending
                                if (haveXNeighbour(checkedNB, cell, total, board)) {

                                    if (!inPending.contains(cell)) {

                                        inPending.add(cell);

                                        if (!notChecked.contains(cell)) {
                                            cell.value = 0;
                                            notChecked.add(cell);
                                        }

                                        int gidx = getGroupIdx(cell, groups);
                                        if (gidx != -1) {
                                            ArrayList<Pair> grp = groups.get(gidx);
                                            for (Pair p : grp) {
                                                p.value = 0;
                                                if (!notChecked.contains(p))
                                                    notChecked.add(p);
                                                order.remove(p);
                                            }
                                            grp.clear();
                                            groups.remove(gidx);
                                        }

                                        break;
                                    }

                                    else {
                                        boolean semuaSudahDicek = true;
                                        for (boolean b : checkedNB)
                                            if (!b)
                                                semuaSudahDicek = false;

                                        if (semuaSudahDicek) {

                                            // jika punya neighbour 1 → coba gabungkan
                                            if (haveXNeighbour(checkedNB, cell, 1, board)) {

                                                NB = mapDirection(getNB(cell, 1, board), cell);

                                                if (canMerge(NB, 2, board, groups)) {

                                                    groups = mergeGroup(NB, cell, 2, groups);
                                                    notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                    board = updateBoard(groups, NB);

                                                    cell.value = board[cell.row][cell.col];
                                                }

                                                else {
                                                    // deep backtrack: hapus grup NB
                                                    Pair targetNB = NB;
                                                    int g = getGroupIdx(targetNB, groups);

                                                    if (g != -1) {
                                                        ArrayList<Pair> grp = groups.get(g);
                                                        for (Pair p : grp) {
                                                            p.value = 0;
                                                            board[p.row][p.col] = 0;
                                                            if (!notChecked.contains(p))
                                                                notChecked.add(p);
                                                            order.remove(p);
                                                        }
                                                        grp.clear();
                                                        groups.remove(g);
                                                        notChecked = shuffleOrder(notChecked);
                                                    }
                                                }
                                            }

                                            else {
                                                // tidak punya neighbour 1 → isi sendiri 1
                                                cell.value = 1;
                                                board[cell.row][cell.col] = 1;
                                                ArrayList<Pair> g = new ArrayList<>();
                                                g.add(new Pair(cell.row, cell.col, 1));
                                                groups.add(g);
                                            }

                                            direction = false;

                                        }

                                        else {
                                            arah = rd.nextInt(4);
                                        }
                                    }

                                }

                                else {
                                    // merge normal tanpa conflict
                                    groups = mergeGroup(NB, cell, total, groups);
                                    notChecked = removeFromNotChecked(cell, groups, notChecked);
                                    board = updateBoard(groups, NB);

                                    cell.value = board[cell.row][cell.col];

                                    direction = false;

                                }

                            }

                            else { // tidak bisa merge ke arah ini

                                boolean semuaSudahDicek = true;
                                for (boolean b : checkedNB)
                                    if (!b)
                                        semuaSudahDicek = false;

                                if (semuaSudahDicek) {

                                    if (haveXNeighbour(checkedNB, cell, 1, board)) {

                                        NB = mapDirection(getNB(cell, 1, board), cell);

                                        groups = mergeGroup(NB, cell, 2, groups);
                                        notChecked = removeFromNotChecked(cell, groups, notChecked);
                                        board = updateBoard(groups, NB);
                                        cell.value = board[cell.row][cell.col];
                                    }

                                    else {
                                        cell.value = 1;
                                        board[cell.row][cell.col] = 1;
                                        ArrayList<Pair> g = new ArrayList<>();
                                        g.add(new Pair(cell.row, cell.col, 1));
                                        groups.add(g);
                                    }

                                    direction = false;

                                }

                                else {
                                    arah = rd.nextInt(4);
                                }
                            }
                        }

                        // Jika arah sudah dicek
                        else {

                            boolean semuaSudahDicek = true;
                            for (boolean b : checkedNB)
                                if (!b)
                                    semuaSudahDicek = false;

                            if (semuaSudahDicek) {

                                if (haveXNeighbour(checkedNB, cell, 1, board)) {

                                    NB = mapDirection(getNB(cell, 1, board), cell);

                                    if (canMerge(NB, 2, board, groups)) {
                                        groups = mergeGroup(NB, cell, 2, groups);
                                        notChecked = removeFromNotChecked(cell, groups, notChecked);
                                        board = updateBoard(groups, NB);
                                        cell.value = board[cell.row][cell.col];
                                    }

                                    else {
                                        cell.value = 1;
                                        board[cell.row][cell.col] = 1;
                                        ArrayList<Pair> g = new ArrayList<>();
                                        g.add(new Pair(cell.row, cell.col, 1));
                                        groups.add(g);
                                    }
                                }

                                else {
                                    cell.value = 1;
                                    board[cell.row][cell.col] = 1;
                                    ArrayList<Pair> g = new ArrayList<>();
                                    g.add(new Pair(cell.row, cell.col, 1));
                                    groups.add(g);
                                }

                                direction = false;

                            }

                            else {
                                arah = rd.nextInt(4);
                            }
                        }

                    } // end while(direction)
                }

                // tambahkan ke order hanya jika tidak ada duplikasi
                if (!notChecked.contains(cell)) {
                    if (!order.contains(cell)) {
                        order.add(cell);
                    }
                }
            } // end while(haveEmpty)

            if (notChecked.isEmpty()) {
                idt = true;
            }
            boolean foundConflict = false;

            for (int rr = 0; rr < size; rr++) {
                for (int cc = 0; cc < size; cc++) {
                    Pair tmp = new Pair(rr, cc, 0);
                    boolean[] TcheckedNB = new boolean[4];
                    TcheckedNB = initiateCheckedNB(tmp, TcheckedNB);

                    if (board[rr][cc] == 1) {
                        // cek kanan
                        if (cc + 1 < size && board[rr][cc + 1] == 1) {
                            forceResetCell(rr, cc, board, groups, order, notChecked);
                            foundConflict = true;
                            break;
                        }

                        // cek bawah
                        if (rr + 1 < size && board[rr + 1][cc] == 1) {
                            forceResetCell(rr, cc, board, groups, order, notChecked);
                            foundConflict = true;
                            break;
                        }
                    }
                }
                if (foundConflict)
                    break;
            }

            for (int rr = 0; rr < size; rr++) {
                for (int cc = 0; cc < size; cc++) {
                    Pair tmp = new Pair(rr, cc, 0);
                    boolean[] TcheckedNB = new boolean[4];
                    TcheckedNB = initiateCheckedNB(tmp, TcheckedNB);
                    if (board[rr][cc] == 0) {
                        if (haveXNeighbour(TcheckedNB, tmp, 1, board) == false) {
                            Pair NBT = mapDirection(getNB(tmp, 1, board), tmp);
                            if (canMerge(NBT, 2, board, groups)) {
                                groups = mergeGroup(NBT, tmp, 2, groups);
                                notChecked = removeFromNotChecked(tmp, groups, notChecked);
                                board = updateBoard(groups, tmp);
                                tmp.value = board[tmp.row][tmp.col];

                            } else {
                                forceResetCell(rr, cc, board, groups, order, notChecked);
                                foundConflict = true;
                                break;
                            }
                        } else {
                            tmp.value = 1;
                            board[tmp.row][tmp.col] = 1;
                            notChecked.remove(tmp);
                        }
                    }
                }
                if (foundConflict)
                    break;
            }

            if (foundConflict) {
                // important: make sure the outer while(!idt) will loop again
                idt = false;
                continue; // restart outer while loop immediately with reshuffled notChecked
            }

        }

        return board;

    }

    private void forceResetCell(int r, int c,
            int[][] board,
            List<ArrayList<Pair>> groups,
            List<Pair> order,
            List<Pair> notChecked) {

        int[][] dirs = { { 0, 0 }, { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

        for (int[] d : dirs) {

            int nr = r + d[0];
            int nc = c + d[1];

            if (nr < 0 || nc < 0 || nr >= size || nc >= size)
                continue;

            Pair x = new Pair(nr, nc, 0);

            // hapus dari board
            board[nr][nc] = 0;

            // hapus dari group
            int gidx = getGroupIdx(x, groups);
            if (gidx != -1) {
                ArrayList<Pair> g = groups.get(gidx);
                for (Pair p : g) {
                    board[p.row][p.col] = 0;
                    // remove any matching entries from order
                    order.removeIf(k -> k.row == p.row && k.col == p.col);
                    if (!notChecked.contains(new Pair(p.row, p.col, 0)))
                        notChecked.add(new Pair(p.row, p.col, 0));
                }
                g.clear();
                groups.remove(gidx);
            }

            // hapus dari order yang mungkin tersisa
            order.removeIf(k -> k.row == nr && k.col == nc);

            // masukkan kembali ke notChecked
            if (!notChecked.contains(x))
                notChecked.add(x);
        }

        // reshuffle notChecked in-place so caller sees change
        Collections.shuffle(notChecked);
    }

    private int getNB(Pair cell, int x, int[][] board) {
        int n = board.length;

        if (cell.row > 0 && board[cell.row - 1][cell.col] == x)
            return 0;
        if (cell.col > 0 && board[cell.row][cell.col - 1] == x)
            return 1;
        if (cell.col < n - 1 && board[cell.row][cell.col + 1] == x)
            return 2;
        if (cell.row < n - 1 && board[cell.row + 1][cell.col] == x)
            return 3;

        return 0;
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

    private boolean haveXNeighbour(boolean[] checkedNB, Pair cell, int x, int[][] board) {
        boolean res = false;
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

    private Pair mapDirection(int index, Pair cell) {
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

        // INBOUND CHECK (WAJIB)
        if (r < 0 || c < 0 || r >= size || c >= size) {
            return new Pair(-1, -1, 0);
        }

        return new Pair(r, c, board[r][c]);
    }

    private boolean[] initiateCheckedNB(Pair cell, boolean[] checkedNB) {
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

        // tandai NB yang ada value
        if (cell.row > 0 && board[cell.row - 1][cell.col] > 0) // kalo row lebih dari 0 dan dia memiliki tetangga ats
            checkedNB[0] = false;
        if (cell.row < board.length - 1 && board[cell.row + 1][cell.col] > 0) // kalau row punya bawah
            checkedNB[3] = false;
        if (cell.col > 0 && board[cell.row][cell.col - 1] > 0) // kalau col punya kiri
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

    public int[][] pruneBoard(int[][] board) {
        Random random = new Random();

        // kosongkan papan
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = 0;
            }
        }

        // acak urutan groups
        Collections.shuffle(groups);

        for (List<Pair> group : groups) {

            int sisa;
            if (group.size() > 2) {
                sisa = random.nextBoolean() ? 1 : 2;
            } else {
                sisa = 1;
            }

            // pilih elemen secara acak TANPA menghapus dari group
            for (int k = 0; k < sisa; k++) {
                Pair cell = group.get(random.nextInt(group.size()));
                board[cell.row][cell.col] = cell.value;
            }
        }

        return board;
    }
}