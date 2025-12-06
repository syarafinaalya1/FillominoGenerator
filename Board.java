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

        while (!idt) {
            while (haveEmpty(notChecked)) {

                cell = notChecked.remove(idx);
                int total = 0;

                int nbCount = countNB(cell, board);

                // CASE 1 : no neighbour
                if (nbCount == 0) {
                    cell.value = 1;
                    board[cell.row][cell.col] = cell.value;

                    ArrayList<Pair> newGroup = new ArrayList<>();
                    newGroup.add(cell);
                    groups.add(newGroup);
                }

                // CASE 2 : one neighbour
                else if (nbCount == 1) {
                    cell.value = 1;
                    NB = null;

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

                    if (total <= 9 && canMerge(NB, total, board, groups)) {
                        groups = mergeGroup(NB, cell, total, groups);
                        notChecked = removeFromNotChecked(cell, groups, notChecked);
                        board = updateBoard(groups, NB);
                    } else {
                        cell.value = 1;
                        board[cell.row][cell.col] = 1;
                        ArrayList<Pair> newGroup = new ArrayList<>();
                        newGroup.add(cell);
                        groups.add(newGroup);
                    }
                }
                // CASE 3 : lebih dari 1 tetangga
                else if (countNB(cell, board) > 1) {

                    boolean[] checked = initiateCheckedNB(cell, new boolean[4]);
                    cell.value = 1; // provisional value (may be updated / merged later)

                    // buat grup provisional: tapi simpan sebagai objek asli (jika perlu nanti
                    // dihapus)
                    ArrayList<Pair> provisional = new ArrayList<>();
                    provisional.add(cell);
                    groups.add(provisional);

                    boolean placedOrHandled = false; // true kalau sudah merge / set / pending dibuat
                    Random rnd = new Random();

                    while (true) {
                        // apakah masih ada arah yang belum dicek?
                        boolean semuaSudahDicek = true;
                        for (boolean b : checked)
                            if (!b) {
                                semuaSudahDicek = false;
                                break;
                            }

                        int arah;
                        if (!semuaSudahDicek) {
                            // pilih arah acak yang belum dicek
                            do {
                                arah = rnd.nextInt(4);
                            } while (checked[arah]);
                        } else {
                            // semua sudah dicek
                            arah = -1;
                        }

                        if (arah != -1) {
                            checked[arah] = true;
                            NB = mapDirection(arah, cell);

                            // skip OOB atau kosong
                            if (NB.row < 0 || NB.col < 0 || NB.row >= board.length || NB.col >= board.length
                                    || NB.value <= 0) {
                                // lanjut ke arah lain
                                continue;
                            }

                            total = NB.value + 1;

                            // jika bisa merge dengan tetangga ini
                            if (total <= 9 && canMerge(NB, total, board, groups)) {

                                // CASE: ada neighbour lain (yang sudah dicek atau belum) dengan nilai == total
                                // → conflict
                                if (haveXNeighbour(checked, cell, total, board)) {

                                    // belum pernah pending → jadikan pending dan rollback grup provisional
                                    if (!inPending.contains(cell)) {
                                        inPending.add(cell);

                                        // kembalikan nilai cell dan grup ke notChecked / board kosong untuk revisit
                                        cell.value = 0;
                                        board[cell.row][cell.col] = 0;
                                        if (!notChecked.contains(cell))
                                            notChecked.add(cell);

                                        int gidx = getGroupIdx(cell, groups);
                                        if (gidx != -1) {
                                            ArrayList<Pair> grp = groups.get(gidx);
                                            for (Pair p : grp) {
                                                p.value = 0;
                                                board[p.row][p.col] = 0;
                                                if (!notChecked.contains(p))
                                                    notChecked.add(p);
                                                order.removeIf(k -> k.row == p.row && k.col == p.col);
                                            }
                                            grp.clear();
                                            groups.remove(gidx);
                                        }
                                        placedOrHandled = true;
                                        break; // keluar CASE3, sel akan diproses kembali nanti
                                    } else {
                                        // sudah pending sebelumnya
                                        if (semuaSudahDicek) {
                                            // coba cari neighbour dengan nilai 1 dan merge jadi 2
                                            if (haveXNeighbour(checked, cell, 1, board)) {
                                                int idxNB = getNB(cell, 1, board);
                                                Pair nb1 = mapDirection(idxNB, cell);
                                                if (nb1.row >= 0 && nb1.col >= 0 && canMerge(nb1, 2, board, groups)) {
                                                    groups = mergeGroup(nb1, cell, 2, groups);
                                                    notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                    board = updateBoard(groups, nb1);
                                                    cell.value = board[cell.row][cell.col];
                                                    placedOrHandled = true;
                                                    break;
                                                } else {
                                                    // tetap pending (tidak diubah), leave for next pass
                                                    placedOrHandled = true;
                                                    break;
                                                }
                                            } else {
                                                // tidak ada neighbour=1 → safe untuk set 1 (tapi double-check
                                                // neighbourHasOne)
                                                if (neighbourHasOne(cell, board)) {
                                                    // kalau masih ada neighbour 1 (meskipun haveXNeighbour bilang
                                                    // tidak),
                                                    // coba merge ke 2 jika memungkinkan, kalau tidak -> biarkan pending
                                                    int idxNB = getNB(cell, 1, board);
                                                    Pair nb1 = mapDirection(idxNB, cell);
                                                    if (nb1.row >= 0 && nb1.col >= 0
                                                            && canMerge(nb1, 2, board, groups)) {
                                                        groups = mergeGroup(nb1, cell, 2, groups);
                                                        notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                        board = updateBoard(groups, nb1);
                                                        cell.value = board[cell.row][cell.col];
                                                        placedOrHandled = true;
                                                        break;
                                                    } else {
                                                        inPending.add(cell);
                                                        cell.value = 0;
                                                        board[cell.row][cell.col] = 0;
                                                        if (!notChecked.contains(cell))
                                                            notChecked.add(cell);
                                                        placedOrHandled = true;
                                                        break;
                                                    }
                                                } else {
                                                    // benar-benar tidak ada neighbour 1 → set 1
                                                    int gidx = getGroupIdx(cell, groups);
                                                    if (gidx == -1) {
                                                        ArrayList<Pair> g = new ArrayList<>();
                                                        g.add(new Pair(cell.row, cell.col, 1));
                                                        groups.add(g);
                                                    } else {
                                                        ArrayList<Pair> g = groups.get(gidx);
                                                        boolean found = false;
                                                        for (Pair p : g) {
                                                            if (p.row == cell.row && p.col == cell.col) {
                                                                p.value = 1;
                                                                found = true;
                                                                break;
                                                            }
                                                        }
                                                        if (!found)
                                                            g.add(new Pair(cell.row, cell.col, 1));
                                                        for (Pair p : groups.get(gidx))
                                                            p.value = 1;
                                                    }
                                                    board[cell.row][cell.col] = 1;
                                                    cell.value = 1;
                                                    updateBoard(groups, cell);
                                                    placedOrHandled = true;
                                                    break;
                                                }
                                            }
                                        } else {
                                            // belum semua dicek → pilih arah lain
                                            continue;
                                        }
                                    }
                                } else {
                                    // merge normal tanpa conflict → commit merge
                                    groups = mergeGroup(NB, cell, total, groups);
                                    notChecked = removeFromNotChecked(cell, groups, notChecked);
                                    board = updateBoard(groups, NB);
                                    cell.value = board[cell.row][cell.col];
                                    placedOrHandled = true;
                                    break;
                                }

                            } else {
                                // tidak bisa merge ke arah ini
                                if (semuaSudahDicek) {
                                    // semua sudah dicek, lakukan fallback: cek neighbour=1 → coba merge jadi 2,
                                    // jika tidak bisa -> pending atau set 1 bila aman
                                    if (haveXNeighbour(checked, cell, 1, board)) {
                                        int idxNB = getNB(cell, 1, board);
                                        Pair nb1 = mapDirection(idxNB, cell);
                                        if (nb1.row >= 0 && nb1.col >= 0 && canMerge(nb1, 2, board, groups)) {
                                            groups = mergeGroup(nb1, cell, 2, groups);
                                            notChecked = removeFromNotChecked(cell, groups, notChecked);
                                            board = updateBoard(groups, nb1);
                                            cell.value = board[cell.row][cell.col];
                                            placedOrHandled = true;
                                            break;
                                        } else {
                                            if (!inPending.contains(cell)) {
                                                inPending.add(cell);
                                                cell.value = 0;
                                                board[cell.row][cell.col] = 0;
                                                if (!notChecked.contains(cell))
                                                    notChecked.add(cell);
                                                placedOrHandled = true;
                                                break;
                                            } else {
                                                // sudah pending; final fallback: jika aman set 1
                                                if (!neighbourHasOne(cell, board)) {
                                                    int gidx = getGroupIdx(cell, groups);
                                                    if (gidx == -1) {
                                                        ArrayList<Pair> g = new ArrayList<>();
                                                        g.add(new Pair(cell.row, cell.col, 1));
                                                        groups.add(g);
                                                    } else {
                                                        ArrayList<Pair> g = groups.get(gidx);
                                                        boolean found = false;
                                                        for (Pair p : g) {
                                                            if (p.row == cell.row && p.col == cell.col) {
                                                                p.value = 1;
                                                                found = true;
                                                                break;
                                                            }
                                                        }
                                                        if (!found)
                                                            g.add(new Pair(cell.row, cell.col, 1));
                                                        for (Pair p : groups.get(gidx))
                                                            p.value = 1;
                                                    }
                                                    board[cell.row][cell.col] = 1;
                                                    cell.value = 1;
                                                    updateBoard(groups, cell);
                                                    placedOrHandled = true;
                                                    break;
                                                } else {
                                                    // tetap pending
                                                    placedOrHandled = true;
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        // tidak ada neighbour 1 → safe set 1 (double-check)
                                        if (!neighbourHasOne(cell, board)) {
                                            int gidx = getGroupIdx(cell, groups);
                                            if (gidx == -1) {
                                                ArrayList<Pair> g = new ArrayList<>();
                                                g.add(new Pair(cell.row, cell.col, 1));
                                                groups.add(g);
                                            } else {
                                                ArrayList<Pair> g = groups.get(gidx);
                                                boolean found = false;
                                                for (Pair p : g) {
                                                    if (p.row == cell.row && p.col == cell.col) {
                                                        p.value = 1;
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                                if (!found)
                                                    g.add(new Pair(cell.row, cell.col, 1));
                                                for (Pair p : groups.get(gidx))
                                                    p.value = 1;
                                            }
                                            board[cell.row][cell.col] = 1;
                                            cell.value = 1;
                                            updateBoard(groups, cell);
                                            placedOrHandled = true;
                                            break;
                                        } else {
                                            // ada neighbour1 (meskipun haveXNeighbour bilang tidak) -> coba merge or
                                            // pending
                                            int idxNB = getNB(cell, 1, board);
                                            Pair nb1 = mapDirection(idxNB, cell);
                                            if (nb1.row >= 0 && nb1.col >= 0 && canMerge(nb1, 2, board, groups)) {
                                                groups = mergeGroup(nb1, cell, 2, groups);
                                                notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                board = updateBoard(groups, nb1);
                                                cell.value = board[cell.row][cell.col];
                                                placedOrHandled = true;
                                                break;
                                            } else {
                                                if (!inPending.contains(cell)) {
                                                    inPending.add(cell);
                                                    cell.value = 0;
                                                    board[cell.row][cell.col] = 0;
                                                    if (!notChecked.contains(cell))
                                                        notChecked.add(cell);
                                                }
                                                placedOrHandled = true;
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    // belum semua dicek → coba arah lain
                                    continue;
                                }
                            }
                        } else {
                            // semuaSudahDicek true dan kita belum melakukan tindakan -> final fallback:
                            if (haveXNeighbour(checked, cell, 1, board)) {
                                int idxNB = getNB(cell, 1, board);
                                Pair nb1 = mapDirection(idxNB, cell);
                                if (nb1.row >= 0 && nb1.col >= 0 && canMerge(nb1, 2, board, groups)) {
                                    groups = mergeGroup(nb1, cell, 2, groups);
                                    notChecked = removeFromNotChecked(cell, groups, notChecked);
                                    board = updateBoard(groups, nb1);
                                    cell.value = board[cell.row][cell.col];
                                    placedOrHandled = true;
                                    break;
                                } else {
                                    if (!inPending.contains(cell)) {
                                        inPending.add(cell);
                                        cell.value = 0;
                                        board[cell.row][cell.col] = 0;
                                        if (!notChecked.contains(cell))
                                            notChecked.add(cell);
                                    } else {
                                        // final forced set 1 only if safe
                                        if (!neighbourHasOne(cell, board)) {
                                            int gidx = getGroupIdx(cell, groups);
                                            if (gidx == -1) {
                                                ArrayList<Pair> g = new ArrayList<>();
                                                g.add(new Pair(cell.row, cell.col, 1));
                                                groups.add(g);
                                            } else {
                                                ArrayList<Pair> g = groups.get(gidx);
                                                boolean found = false;
                                                for (Pair p : g) {
                                                    if (p.row == cell.row && p.col == cell.col) {
                                                        p.value = 1;
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                                if (!found)
                                                    g.add(new Pair(cell.row, cell.col, 1));
                                                for (Pair p : groups.get(gidx))
                                                    p.value = 1;
                                            }
                                            board[cell.row][cell.col] = 1;
                                            cell.value = 1;
                                            updateBoard(groups, cell);
                                        } else {
                                            // tetap pending
                                        }
                                    }
                                    placedOrHandled = true;
                                    break;
                                }
                            } else {
                                // safe set 1
                                int gidx = getGroupIdx(cell, groups);
                                if (gidx == -1) {
                                    ArrayList<Pair> g = new ArrayList<>();
                                    g.add(new Pair(cell.row, cell.col, 1));
                                    groups.add(g);
                                } else {
                                    ArrayList<Pair> g = groups.get(gidx);
                                    boolean found = false;
                                    for (Pair p : g) {
                                        if (p.row == cell.row && p.col == cell.col) {
                                            p.value = 1;
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found)
                                        g.add(new Pair(cell.row, cell.col, 1));
                                    for (Pair p : groups.get(gidx))
                                        p.value = 1;
                                }
                                board[cell.row][cell.col] = 1;
                                cell.value = 1;
                                updateBoard(groups, cell);
                                placedOrHandled = true;
                                break;
                            }
                        }
                    } // end while CASE3 loop

                    // Pastikan order hanya berisi satu instance
                    if (!notChecked.contains(cell) && !order.contains(cell)) {
                        order.add(cell);
                    }
                } // end else if CASE3

                // add to order if not duplicate and not in notChecked
                if (!notChecked.contains(cell)) {
                    if (!order.contains(cell)) {
                        order.add(cell);
                    }
                }
            } // end while(haveEmpty)

            // scan board for zeros and re-add to notChecked
            for (int r = 0; r < board.length; r++) {
                for (int c = 0; c < board[r].length; c++) {
                    if (board[r][c] == 0) {
                        idt = false;
                        Pair p = new Pair(r, c, 0);
                        if (!notChecked.contains(p)) {
                            notChecked.add(p);
                        }
                    }
                }
            }

            if (notChecked.isEmpty()) {
                idt = true;
            }
        } // end while(!idt)

        return board;
    }

    private boolean neighbourHasOne(Pair cell, int[][] board) {
        int r = cell.row;
        int c = cell.col;
        int n = board.length;
        if (r > 0 && board[r - 1][c] == 1)
            return true;
        if (r < n - 1 && board[r + 1][c] == 1)
            return true;
        if (c > 0 && board[r][c - 1] == 1)
            return true;
        if (c < n - 1 && board[r][c + 1] == 1)
            return true;
        return false;
    }

    static void printBoard(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    public int getNB(Pair cell, int target, int[][] board) {

        int r = cell.row;
        int c = cell.col;

        // return index arah yang punya nilai target

        // atas
        if (r > 0 && board[r - 1][c] == target)
            return 0;

        // bawah
        if (r < size - 1 && board[r + 1][c] == target)
            return 1;

        // kiri
        if (c > 0 && board[r][c - 1] == target)
            return 2;

        // kanan
        if (c < size - 1 && board[r][c + 1] == target)
            return 3;

        return -1;
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

    public boolean haveXNeighbour(boolean[] checkedNB, Pair cell, int target, int[][] board) {
        // checkedNB TIDAK DIJADIKAN FILTER LAGI
        // hanya cek 4 arah langsung

        int r = cell.row;
        int c = cell.col;

        // atas
        if (r > 0 && board[r - 1][c] == target)
            return true;

        // bawah
        if (r < size - 1 && board[r + 1][c] == target)
            return true;

        // kiri
        if (c > 0 && board[r][c - 1] == target)
            return true;

        // kanan
        if (c < size - 1 && board[r][c + 1] == target)
            return true;

        return false;
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

        // acak urutan groups (opsional, boleh tetap)
        Collections.shuffle(groups);

        for (List<Pair> group : groups) {

            int sisa = 1;
            if (group.size() > 2) {
                sisa = random.nextBoolean() ? 1 : 2;
            }

            List<Pair> copy = new ArrayList<>(group);
            Collections.shuffle(copy); // acak posisi
            int batas = Math.min(sisa, copy.size()); // jaga jangan lebih dari ukuran grup

            for (int k = 0; k < batas; k++) {
                Pair cell = copy.get(k); // cell dijamin unik
                board[cell.row][cell.col] = cell.value;
            }
            // --------------------
        }

        return board;
    }

}