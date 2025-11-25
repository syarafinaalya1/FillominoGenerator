import java.util.ArrayList;
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
        boolean idt = false;

        boolean[] checkedNB = new boolean[4];
        // Boolean[][] pending = new Boolean[board.length][board.length];
        List<Pair> notChecked = new ArrayList<>();
        List<Pair> order = new ArrayList<>();
        List<Pair> inPending = new ArrayList<>();

        // initiate notchecked
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                notChecked.add(new Pair(r, c, 0));
            }
        }
        notChecked = shuffleOrder(notChecked);

        // for (int row = 0; row < pending.length; row++) {
        // for (int col = 0; col < pending.length; col++) {
        // pending[row][col] = false;
        // }
        // }
        while (idt == false) {
            while (haveEmpty(notChecked)) {

                cell = notChecked.remove(idx);// mengambil index ke 0, langsung remove karena case ini sudah pasti
                                              // terjadi
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

                    total = NB.value + cell.value; // sudah di update tambah 1

                    // 2. update group baru dengan tetangga

                    // CASE 1.1 : 1 tetanga & bisa merge
                    if (total <= 9 && canMerge(NB, total, board, groups)) { // belum update cell | harunya new group
                                                                            // cell
                                                                            // board
                        // cek dulu jika bisa merge setelah digrup

                        groups = mergeGroup(NB, cell, total, groups);
                        notChecked = removeFromNotChecked(cell, groups, notChecked);
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

                    System.out.println("Multi Tetangga : " + cell.col + " " + cell.row);
                    int arah = rd.nextInt(4);// mendapatkan tetangga yang belum di cek (0-3)
                    boolean direction = true; // menyimpan arah, jika sudah di cek ini akan berhenti sel tidak akan di
                                              // cek
                                              // lagi
                    checkedNB = initiateCheckedNB(cell, checkedNB); // disini akan menfalse kan semua, sekaligus
                                                                    // men-true
                                                                    // kan yang out of bound agar tidak di cek
                    cell.value = 1;

                    ArrayList<Pair> newCell = new ArrayList<>(); // inisiasi grup baru dengan isi 1
                    newCell.add(new Pair(cell.row, cell.col, 1));
                    groups.add(newCell);

                    while (direction == true) {

                        // CASE 3.1 : Multi tetangga & !checked
                        if (checkedNB[arah] == false) {

                            checkedNB[arah] = true; // ubah state nb jadi sudah di cek
                            NB = mapDirection(arah, cell); // dapatkan NB
                            total = NB.value + cell.value;

                            // proses nb baru
                            if (total <= 9 && canMerge(NB, total, board, groups)) { // CASE 3.1.1 Multi tetangga &
                                                                                    // !checked
                                                                                    // & Merge

                                if (haveXNeighbour(checkedNB, cell, total, board)) { // jika ada neighbor yang mnemiliki
                                                                                     // nilai baru (total diisi dengan
                                                                                     // nilai
                                                                                     // baru bukan nilai neighbor)
                                                                                     // backtracking

                                    if (inPending.indexOf(cell) != -1) { // belum pernah di pending
                                        notChecked.add(cell);
                                        inPending.add(cell);
                                        cell.value = 0;

                                        int index = getGroupIdx(cell, groups);
                                        groups.remove(index);
                                        break;
                                    } else {

                                        boolean semuaSudahDicek = true;

                                        for (boolean b : checkedNB) {
                                            if (b == false) {
                                                semuaSudahDicek = false;
                                                break;
                                            }
                                        }

                                        if (semuaSudahDicek == true) {
                                            if (haveXNeighbour(checkedNB, cell, 1, board)) {

                                                NB = mapDirection(getNB(cell, 1, board), cell);

                                                if (canMerge(NB, 2, board, groups)) {

                                                    groups = mergeGroup(NB, cell, total, groups);
                                                    notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                    board = updateBoard(groups, NB);
                                                    board[cell.col][cell.row] = 2; // pemastian ulang
                                                    cell.value = board[cell.row][cell.col];

                                                } else {

                                                    Pair targetNB = NB;
                                                    int gidx = getGroupIdx(targetNB, groups);
                                                    int nbIndex = order.indexOf(targetNB);

                                                    while (!canMerge(NB, 2, board, groups)) {

                                                        if (nbIndex == -1) {
                                                            order.add(0, NB);
                                                            nbIndex = 0;

                                                            if (gidx != -1) {

                                                                ArrayList<Pair> grp = groups.get(gidx);

                                                                for (int i = 0; i < grp.size(); i++) {

                                                                    Pair p = grp.get(i);

                                                                    board[p.row][p.col] = 0;
                                                                    if (!notChecked.contains(p)) {
                                                                        notChecked.add(p);

                                                                    }

                                                                    if (order.indexOf(p) != -1) {
                                                                        order.remove(order.indexOf(order));
                                                                    }
                                                                }

                                                                notChecked = shuffleOrder(notChecked);
                                                                groups.remove(gidx);
                                                            }
                                                            break;
                                                        }

                                                        // ============= CASE: order sudah menyentuh NB
                                                        // ======================
                                                        if (order.size() - 1 <= nbIndex) {

                                                            ArrayList<Pair> grp = groups
                                                                    .get(getGroupIdx(targetNB, groups));
                                                            notChecked.add(targetNB);
                                                            order.remove(nbIndex);

                                                            for (int i = 0; i < grp.size(); i++) {
                                                                Pair p = grp.get(i);
                                                                p.value = 0;

                                                                board[p.row][p.col] = 0;
                                                                if (!notChecked.contains(p)) {
                                                                    notChecked.add(p);
                                                                }

                                                                if (order.indexOf(p) != -1) {
                                                                    order.remove(order.indexOf(p));
                                                                }
                                                            }

                                                            notChecked = shuffleOrder(notChecked);
                                                            groups.remove(nbIndex);

                                                            cell.value = 1;
                                                            board[cell.row][cell.col] = cell.value;

                                                            ArrayList<Pair> newGroup = new ArrayList<>(); // tambahkan
                                                                                                          // ke dalam
                                                                                                          // grup
                                                            newGroup.add(new Pair(cell.row, cell.col, cell.value));
                                                            groups.add(newGroup);

                                                            break;
                                                        }

                                                        // ======================= BACKTRACK NORMAL
                                                        // ===========================

                                                        Pair back = order.get(order.size() - 1);
                                                        board[back.row][back.col] = 0;

                                                        int gBidx = getGroupIdx(back, groups);

                                                        if (gBidx != -1) {

                                                            // kosongin grup, tambahin ke notcheck,remover order, remove
                                                            // board
                                                            List<Pair> grp = groups.get(gidx);
                                                            for (int i = 0; i < grp.size(); i++) {

                                                                Pair p = grp.get(i);
                                                                p.value = 0;

                                                                board[p.row][p.col] = 0;
                                                                if (!notChecked.contains(p)) {
                                                                    notChecked.add(p);
                                                                }

                                                                if (order.indexOf(p) != -1) {
                                                                    order.remove(order.indexOf(p));
                                                                }
                                                            }

                                                            groups.remove(gidx);
                                                        }

                                                        if (!notChecked.contains(back)) {
                                                            notChecked.add(back);
                                                            notChecked = shuffleOrder(notChecked);
                                                        }
                                                        if (order.indexOf(back) != -1) {
                                                            order.remove(order.indexOf(back));
                                                        }

                                                        // ============== CEK APAKAH SUDAH ADA TETANGGA VALID
                                                        // =============
                                                        int nbCheck = hasValidNeighbourForMerge(cell, board, groups);
                                                        if (nbCheck > 0) {

                                                            Pair p = mapDirection(nbCheck, cell);

                                                            groups = mergeGroup(p, cell, total, groups);
                                                            notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                            board = updateBoard(groups, p);
                                                            cell.value = board[cell.row][cell.col];

                                                            break;
                                                        } // else if gapunya tetangga 1
                                                        else if ((haveXNeighbour(checkedNB, cell, 1, board)) == false) {
                                                            cell.value = 1;
                                                            board[cell.row][cell.col] = cell.value;

                                                            ArrayList<Pair> g = new ArrayList<>(); // inisiasi grup
                                                                                                   // baru dengan isi
                                                                                                   // 1
                                                            g.add(new Pair(cell.row, cell.col, 1));
                                                            groups.add(g);

                                                            break;

                                                        } else if (canMerge(NB, 2, board, groups)) {
                                                            NB = mapDirection(getNB(cell, 1, board), cell);

                                                            groups = mergeGroup(NB, cell, total, groups);
                                                            notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                            board = updateBoard(groups, cell);
                                                            cell.value = board[cell.row][cell.col];

                                                            break;
                                                        }

                                                    }
                                                }

                                            } else {

                                                // ================ BUAT GRUP BARU DENGAN REUSE PAIR
                                                // ====================
                                                cell.value = 1;
                                                board[cell.row][cell.col] = 1;

                                                ArrayList<Pair> newGroup = new ArrayList<>();
                                                newGroup.add(cell); // ⬅️ tidak membuat Pair baru
                                                groups.add(newGroup);
                                            }
                                        }

                                    }

                                } else {

                                    groups = mergeGroup(NB, cell, total, groups);
                                    notChecked = removeFromNotChecked(cell, groups, notChecked);
                                    board = updateBoard(groups, NB);

                                    cell.value = board[cell.row][cell.col];

                                    direction = false;
                                    printBoard(board);

                                }

                            }

                            else { // CASE 3.1.2 Multi tetangga & !checked & !Merge

                                // 1. cek dahulu jika semua sudah di cek
                                boolean semuaSudahDicek = true;

                                for (boolean b : checkedNB) {
                                    if (b == false) {
                                        semuaSudahDicek = false;
                                        break;
                                    }
                                }

                                // 2. jika semua sudah di cek, loop berhenti. cell sudah beres..

                                // CASE 3.1.2.1 Multi tetangga & !checked & !Merge & Finish
                                // CASE 3.1.2.1 Multi tetangga & !checked & !Merge & Finish

                                if (semuaSudahDicek == true) {
                                    if (haveXNeighbour(checkedNB, cell, 1, board)) {

                                        NB = mapDirection(getNB(cell, 1, board), cell);

                                        if (canMerge(NB, 2, board, groups)) {

                                            groups = mergeGroup(NB, cell, total, groups);
                                            notChecked = removeFromNotChecked(cell, groups, notChecked);
                                            board = updateBoard(groups, NB);
                                            board[cell.col][cell.row] = 2; // pemastian ulang
                                            cell.value = board[cell.row][cell.col];

                                        } else {

                                            Pair targetNB = NB;
                                            int gidx = getGroupIdx(targetNB, groups);
                                            int nbIndex = order.indexOf(targetNB);

                                            while (!canMerge(NB, 2, board, groups)) {

                                                if (nbIndex == -1) {
                                                    order.add(0, NB);
                                                    nbIndex = 0;

                                                    if (gidx != -1) {

                                                        ArrayList<Pair> grp = groups.get(gidx);

                                                        for (int i = 0; i < grp.size(); i++) {

                                                            Pair p = grp.get(i);

                                                            board[p.row][p.col] = 0;
                                                            if (!notChecked.contains(p)) {
                                                                notChecked.add(p);

                                                            }

                                                            if (order.indexOf(p) != -1) {
                                                                order.remove(order.indexOf(order));
                                                            }
                                                        }

                                                        notChecked = shuffleOrder(notChecked);
                                                        groups.remove(gidx);
                                                    }
                                                    break;
                                                }

                                                // ============= CASE: order sudah menyentuh NB
                                                // ======================
                                                if (order.size() - 1 <= nbIndex) {

                                                    ArrayList<Pair> grp = groups
                                                            .get(getGroupIdx(targetNB, groups));
                                                    notChecked.add(targetNB);
                                                    order.remove(nbIndex);

                                                    for (int i = 0; i < grp.size(); i++) {
                                                        Pair p = grp.get(i);
                                                        p.value = 0;

                                                        board[p.row][p.col] = 0;
                                                        if (!notChecked.contains(p)) {
                                                            notChecked.add(p);
                                                        }

                                                        if (order.indexOf(p) != -1) {
                                                            order.remove(order.indexOf(p));
                                                        }
                                                    }

                                                    notChecked = shuffleOrder(notChecked);
                                                    groups.remove(nbIndex);

                                                    cell.value = 1;
                                                    board[cell.row][cell.col] = cell.value;

                                                    ArrayList<Pair> newGroup = new ArrayList<>(); // tambahkan
                                                                                                  // ke dalam
                                                                                                  // grup
                                                    newGroup.add(new Pair(cell.row, cell.col, cell.value));
                                                    groups.add(newGroup);

                                                    break;
                                                }

                                                // ======================= BACKTRACK NORMAL
                                                // ===========================

                                                Pair back = order.get(order.size() - 1);
                                                board[back.row][back.col] = 0;

                                                int gBidx = getGroupIdx(back, groups);

                                                if (gBidx != -1) {

                                                    // kosongin grup, tambahin ke notcheck,remover order, remove
                                                    // board
                                                    List<Pair> grp = groups.get(gidx);
                                                    for (int i = 0; i < grp.size(); i++) {

                                                        Pair p = grp.get(i);
                                                        p.value = 0;

                                                        board[p.row][p.col] = 0;
                                                        if (!notChecked.contains(p)) {
                                                            notChecked.add(p);
                                                        }

                                                        if (order.indexOf(p) != -1) {
                                                            order.remove(order.indexOf(p));
                                                        }
                                                    }

                                                    groups.remove(gidx);
                                                }

                                                if (!notChecked.contains(back)) {
                                                    notChecked.add(back);
                                                    notChecked = shuffleOrder(notChecked);
                                                }
                                                if (order.indexOf(back) != -1) {
                                                    order.remove(order.indexOf(back));
                                                }

                                                // ============== CEK APAKAH SUDAH ADA TETANGGA VALID
                                                // =============
                                                int nbCheck = hasValidNeighbourForMerge(cell, board, groups);
                                                if (nbCheck > 0) {

                                                    Pair p = mapDirection(nbCheck, cell);

                                                    groups = mergeGroup(p, cell, total, groups);
                                                    notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                    board = updateBoard(groups, p);
                                                    cell.value = board[cell.row][cell.col];

                                                    break;
                                                } // else if gapunya tetangga 1
                                                else if ((haveXNeighbour(checkedNB, cell, 1, board)) == false) {
                                                    cell.value = 1;
                                                    board[cell.row][cell.col] = cell.value;

                                                    ArrayList<Pair> g = new ArrayList<>(); // inisiasi grup
                                                                                           // baru dengan isi
                                                                                           // 1
                                                    g.add(new Pair(cell.row, cell.col, 1));
                                                    groups.add(g);

                                                    break;

                                                } else if (canMerge(NB, 2, board, groups)) {
                                                    NB = mapDirection(getNB(cell, 1, board), cell);

                                                    groups = mergeGroup(NB, cell, total, groups);
                                                    notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                    board = updateBoard(groups, cell);
                                                    cell.value = board[cell.row][cell.col];

                                                    break;
                                                }

                                            }
                                        }

                                    } else {

                                        // ================ BUAT GRUP BARU DENGAN REUSE PAIR ====================
                                        cell.value = 1;
                                        board[cell.row][cell.col] = 1;

                                        ArrayList<Pair> newGroup = new ArrayList<>();
                                        newGroup.add(cell); // ⬅️ tidak membuat Pair baru
                                        groups.add(newGroup);
                                    }
                                }

                                // CASE 3.1.2.1 Multi tetangga & !checked & !Merge & !Finish
                                else { // cari NB baru
                                    arah = rd.nextInt(4);
                                }
                            }
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
                            if (semuaSudahDicek == true) {
                                if (haveXNeighbour(checkedNB, cell, 1, board)) {

                                    NB = mapDirection(getNB(cell, 1, board), cell);

                                    if (canMerge(NB, 2, board, groups)) {

                                        groups = mergeGroup(NB, cell, total, groups);
                                        notChecked = removeFromNotChecked(cell, groups, notChecked);
                                        board = updateBoard(groups, NB);
                                        board[cell.col][cell.row] = 2; // pemastian ulang
                                        cell.value = board[cell.row][cell.col];

                                    } else {

                                        Pair targetNB = NB;
                                        int gidx = getGroupIdx(targetNB, groups);
                                        int nbIndex = order.indexOf(targetNB);

                                        while (!canMerge(NB, 2, board, groups)) {

                                            if (nbIndex == -1) {
                                                order.add(0, NB);
                                                nbIndex = 0;

                                                if (gidx != -1) {

                                                    ArrayList<Pair> grp = groups.get(gidx);

                                                    for (int i = 0; i < grp.size(); i++) {

                                                        Pair p = grp.get(i);

                                                        board[p.row][p.col] = 0;
                                                        if (!notChecked.contains(p)) {
                                                            notChecked.add(p);

                                                        }

                                                        if (order.indexOf(p) != -1) {
                                                            order.remove(order.indexOf(order));
                                                        }
                                                    }

                                                    notChecked = shuffleOrder(notChecked);
                                                    groups.remove(gidx);
                                                }
                                                break;
                                            }

                                            // ============= CASE: order sudah menyentuh NB
                                            // ======================
                                            if (order.size() - 1 <= nbIndex) {

                                                ArrayList<Pair> grp = groups
                                                        .get(getGroupIdx(targetNB, groups));
                                                notChecked.add(targetNB);
                                                order.remove(nbIndex);

                                                for (int i = 0; i < grp.size(); i++) {
                                                    Pair p = grp.get(i);
                                                    p.value = 0;

                                                    board[p.row][p.col] = 0;
                                                    if (!notChecked.contains(p)) {
                                                        notChecked.add(p);
                                                    }

                                                    if (order.indexOf(p) != -1) {
                                                        order.remove(order.indexOf(p));
                                                    }
                                                }

                                                notChecked = shuffleOrder(notChecked);
                                                groups.remove(nbIndex);

                                                cell.value = 1;
                                                board[cell.row][cell.col] = cell.value;

                                                ArrayList<Pair> newGroup = new ArrayList<>(); // tambahkan
                                                                                              // ke dalam
                                                                                              // grup
                                                newGroup.add(new Pair(cell.row, cell.col, cell.value));
                                                groups.add(newGroup);

                                                break;
                                            }

                                            // ======================= BACKTRACK NORMAL
                                            // ===========================

                                            Pair back = order.get(order.size() - 1);
                                            board[back.row][back.col] = 0;

                                            int gBidx = getGroupIdx(back, groups);

                                            if (gBidx != -1) {

                                                // kosongin grup, tambahin ke notcheck,remover order, remove
                                                // board
                                                List<Pair> grp = groups.get(gidx);
                                                for (int i = 0; i < grp.size(); i++) {

                                                    Pair p = grp.get(i);
                                                    p.value = 0;

                                                    board[p.row][p.col] = 0;
                                                    if (!notChecked.contains(p)) {
                                                        notChecked.add(p);
                                                    }

                                                    if (order.indexOf(p) != -1) {
                                                        order.remove(order.indexOf(p));
                                                    }
                                                }

                                                groups.remove(gidx);
                                            }

                                            if (!notChecked.contains(back)) {
                                                notChecked.add(back);
                                                notChecked = shuffleOrder(notChecked);
                                            }
                                            if (order.indexOf(back) != -1) {
                                                order.remove(order.indexOf(back));
                                            }

                                            // ============== CEK APAKAH SUDAH ADA TETANGGA VALID
                                            // =============
                                            int nbCheck = hasValidNeighbourForMerge(cell, board, groups);
                                            if (nbCheck > 0) {

                                                Pair p = mapDirection(nbCheck, cell);

                                                groups = mergeGroup(p, cell, total, groups);
                                                notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                board = updateBoard(groups, p);
                                                cell.value = board[cell.row][cell.col];

                                                break;
                                            } // else if gapunya tetangga 1
                                            else if ((haveXNeighbour(checkedNB, cell, 1, board)) == false) {
                                                cell.value = 1;
                                                board[cell.row][cell.col] = cell.value;

                                                ArrayList<Pair> g = new ArrayList<>(); // inisiasi grup
                                                                                       // baru dengan isi
                                                                                       // 1
                                                g.add(new Pair(cell.row, cell.col, 1));
                                                groups.add(g);

                                                break;

                                            } else if (canMerge(NB, 2, board, groups)) {
                                                NB = mapDirection(getNB(cell, 1, board), cell);

                                                groups = mergeGroup(NB, cell, total, groups);
                                                notChecked = removeFromNotChecked(cell, groups, notChecked);
                                                board = updateBoard(groups, cell);
                                                cell.value = board[cell.row][cell.col];

                                                break;
                                            }

                                        }
                                    }

                                } else {

                                    // ================ BUAT GRUP BARU DENGAN REUSE PAIR ====================
                                    cell.value = 1;
                                    board[cell.row][cell.col] = 1;

                                    ArrayList<Pair> newGroup = new ArrayList<>();
                                    newGroup.add(cell); // ⬅️ tidak membuat Pair baru
                                    groups.add(newGroup);
                                }
                            }

                            // CASE 3.1.2.1 Multi tetangga & !checked & !Merge & !Finish
                            else { // cari NB baru
                                arah = rd.nextInt(4);
                            }
                        }

                    }

                }

                if (notChecked.contains(cell) == false) {
                    order.add(cell);
                }

            }
            notChecked.clear();
            for (

                    int i = 0; i < board.length; i++) {
                for (int j = 0; j < board.length; j++) {
                    if (board[i][j] == 0) {
                        Pair x = new Pair(i, j, 0);
                        if (!notChecked.contains(x)) {
                            notChecked.add(x);

                        }
                    }
                }
            }

            if (notChecked.isEmpty()) {
                idt = true;
            }
        }

        return board;

    }

    private int hasValidNeighbourForMerge(Pair cell, int[][] board, List<ArrayList<Pair>> groups) {
        int res = -1;

        for (int i = 0; i < 4; i++) {

            Pair nb = mapDirection(i, cell);

            if (nb.value > 0) {
                int total = nb.value + cell.value;

                if (total <= 9 && canMerge(cell, total, board, groups)) {
                    res = i;
                    return res;
                }
            }

        }
        return res;

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

    // private boolean[] initiateCheckedNBSpecial(boolean[] checkedNB, Pair cell) {

    // for (int i = 0; i < checkedNB.length; i++) {
    // checkedNB[i] = true;
    // }

    // int r = cell.row;
    // int c = cell.col;
    // int size = board.length;

    // // tandai arah yang out of bound sebagai sudah dicek
    // if (r == 0)
    // checkedNB[0] = true; // up tidak ada
    // if (c == 0)
    // checkedNB[1] = true; // left tidak ada
    // if (c == size - 1)
    // checkedNB[2] = true; // right tidak ada
    // if (r == size - 1)
    // checkedNB[3] = true; // down tidak ada

    // // tandai NB yang ada value
    // if (cell.row > 0 && board[cell.row - 1][cell.col] > 0)
    // checkedNB[0] = false;
    // if (cell.row < board.length - 1 && board[cell.row + 1][cell.col] > 0)
    // checkedNB[3] = false;
    // if (cell.col > 0 && board[cell.row][cell.col - 1] > 0)
    // checkedNB[1] = false;
    // if (cell.col < board[0].length - 1 && board[cell.row][cell.col + 1] > 0)
    // checkedNB[2] = false;

    // if (cell.row > 0 && board[cell.row - 1][cell.col] == 9)
    // checkedNB[0] = true;
    // if (cell.row < size - 1 && board[cell.row + 1][cell.col] == 9)
    // checkedNB[3] = true;
    // if (cell.col > 0 && board[cell.row][cell.col - 1] == 9)
    // checkedNB[1] = true;
    // if (cell.col < size - 1 && board[cell.row][cell.col + 1] == 9)
    // checkedNB[2] = true;

    // return checkedNB;
    // }

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

    private boolean[] initiateCheckedNB(Pair cell, boolean[] checkedNB) {
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

        // inisiasi board kosong
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = 0;
            }
        }

        Collections.shuffle(groups);

        for (int i = 0; i < groups.size(); i++) {
            List<Pair> group = groups.get(i);
            Collections.shuffle(group);

            int sisa = random.nextBoolean() ? 1 : 2;

            while (group.size() > sisa) {
                group.remove(group.size() - 1);
            }
        }

        for (int i = 0; i < groups.size(); i++) {
            List<Pair> group = groups.get(i);
            for (int j = 0; j < group.size(); j++) {
                Pair cell = group.get(j);
                board[cell.row][cell.col] = cell.value;
            }
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

        printBoard(papan);
        sn.close();

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
