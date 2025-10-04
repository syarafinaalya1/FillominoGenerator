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

        while (isEmpty(board)) {

            cell = notChecked.remove(idx); // inisiasi 0

            int total = 0;

            // jika tidak punya tetangga
            if (countNB(cell, board) == 0) {
                cell.value = 1;
                board[cell.row][cell.col] = cell.value;

                ArrayList<Pair> newGroup = new ArrayList<>(); // tambahkan ke dalam grup
                newGroup.add(new Pair(cell.row, cell.col, cell.value));
                groups.add(newGroup);
            }

            // jika punya 1 tetangga
            else if (countNB(cell, board) == 1) {

                // lakukan pencarian neighbor dan dapatkan posisinya
                if (cell.row > 0 && board[cell.row - 1][cell.col] > 0)
                    NB = new Pair(cell.row - 1, cell.col, board[cell.row - 1][cell.col]);
                if (cell.row < size - 1 && board[cell.row + 1][cell.col] > 0)
                    NB = new Pair(cell.row + 1, cell.col, board[cell.row + 1][cell.col]);
                if (cell.col > 0 && board[cell.row][cell.col - 1] > 0)
                    NB = new Pair(cell.row, cell.col - 1, board[cell.row][cell.col - 1]);
                if (cell.col < size - 1 && board[cell.row][cell.col + 1] > 0) {
                    NB = new Pair(cell.row, cell.col + 1, board[cell.row][cell.col + 1]);
                } else {
                    NB = new Pair(-1, -1, 0);
                }

                total = NB.value + 1;

                // update group baru dengan tetangga
                if (total <= 9) {

                    groups = mergeGroup(NB.row, NB.col, cell.row, cell.col, total, groups); // udah update board

                    // tambahkan fungsi remove grup dan semua yang trgabung dari notCheck
                    // hapus semua pair dalam grup hasil merge dari notChecked
                    notChecked = removeFromNotChecked(cell, groups, notChecked);

                    // remove tetangga dan grupnya dari list !!

                } else {
                    board[cell.col][cell.row] = 1; // isi dengan 1

                    ArrayList<Pair> newGroup = new ArrayList<>();
                    newGroup.add(new Pair(cell.row, cell.col, 1));
                    groups.add(newGroup);

                    notChecked.remove(idx);
                }
            }

            // jika tetangga lebih dari 1
            else if (countNB(cell, board) > 1) {
                checkedNB = initiateCheckedNB(cell, null);

                int check = rd.nextInt(4);// mendapatkan tetangga yang belum di cek (0-3)

                boolean direction = true;

                while (direction == true) { // direction untuk menyimpan state NB sudah di dapatkan atau belum
                    // jika NB tersebut belum di check
                    if (checkedNB[check] == false) {
                        checkedNB[check] = true; // ubah state nb jadi sudah di cek 1.1
                        NB = mapDirection(check + 1, cell); // dapatkan NB

                        // proses nb baru
                        total = NB.value + cell.value;

                        if (total <= 9) { // jika dengan tetangga bisa di merge
                            groups = mergeGroup(NB.row, NB.col, cell.row, cell.col, total, groups);

                            // remove tetangga !!
                            notChecked = removeFromNotChecked(cell, groups, notChecked);

                            while (haveXNeighbour(checkedNB, cell, total, board) == true) {// selama masih ada ttg yang
                                                                                           // memiliki nilai = total..
                                int arahNB = getNewNB(checkedNB, total, cell) + 1; // mendapatkan koordinat nb baru yang
                                                                                   // ==
                                // total,
                                // kembalikan (0-3)

                                NB = mapDirection(arahNB, cell); // dapatlkan nb baru dari arah yang baru (1-4)
                                checkedNB[arahNB - 1] = true;// update checked neighbor jadi true (di denormalisasikan
                                                             // lagi kaarena arah nb 1-4)

                                if (total + NB.value <= 9) {// iff conditional jika dapat di merge maka merge
                                    total = total + NB.value; // total diperbaharui
                                    groups = mergeGroup(NB.row, NB.col, cell.row, cell.col, total, groups); // lalu
                                                                                                            // merge
                                                                                                            // dilakukan

                                    // remove tetangga !!
                                    notChecked = removeFromNotChecked(cell, groups, notChecked);

                                } else {// else ganti satu grup jadi 0
                                    deleteGroup(NB, groups, board);

                                    // // tambahkan lagi tetangga ke belum di cek !! atau engga karena ga diremove
                                    for (ArrayList<Pair> group : groups) {
                                        for (Pair p : group) {
                                            if (p.row == NB.row && p.col == NB.col) {
                                                int indexGroup = notChecked.indexOf(p);
                                                if (indexGroup != -1) {
                                                    notChecked.remove(idx);
                                                }
                                            }
                                        }
                                    }

                                }

                                arahNB = getNewNB(checkedNB); // cari neihbor lain ((sudah auto jika di while))
                                NB = mapDirection(arahNB, cell);

                            }

                        } else { // jika dengan tetangga tidak berpotensi di merge

                            // cari nb baru
                            check = rd.nextInt(4);
                            boolean semuaSudahDicek = true;

                            for (boolean b : checkedNB) {
                                if (!b) {
                                    semuaSudahDicek = false;
                                    break;
                                }
                            }

                            if (semuaSudahDicek) { // else if jika semua tetangga sudah dicoba, keluar dari loop

                                direction = false; // pemberhentian loop

                                board[cell.row][cell.col] = 1; // isi dengan satu masuk case pertama

                                // remove cell!!
                                idx = notChecked.indexOf(cell);
                                groups.remove(idx);
                            }
                            continue;
                            // else lakukan ulang

                        }

                        direction = false;
                    }

                    else { // jika tetangga sudah di cek
                        check = rd.nextInt(4);
                        // dimulai darri cari tetannga baru

                        // dapatkan tetangga baru, lalu checked == true

                        // update total

                        // while masih ada yang sebanyak total ,
                        // cek jika dia sudah di cek atau nbelum, kalau sudah cari tetangga yang lain
                        // (while semua belum di cek)
                    }

                    // sudah dapat NB , lalu cek merge

                }

            }

        }
        return board;

    }

    private List<Pair> removeFromNotChecked(Pair target, List<ArrayList<Pair>> groups, List<Pair> notChecked) {
        // cari grup yang mengandung pair target
        for (ArrayList<Pair> group : groups) {
            for (Pair p : group) {
                if (p.row == target.row && p.col == target.col) {
                    // cari index di notChecked secara manual
                    for (int i = 0; i < notChecked.size(); i++) {
                        Pair np = notChecked.get(i);
                        if (np.row == target.row && np.col == target.col) {
                            notChecked.remove(i); // hapus berdasarkan index
                            return notChecked; // kembalikan list yang sudah diupdate
                        }
                    }
                }
            }
        }
        return notChecked; // kalau tidak ditemukan tetap kembalikan list asli
    }

    private List shuffleOrder(List<Pair> order) {

        Collections.shuffle(order);

        return order;
    }

    private void deleteGroup(Pair x, List<ArrayList<Pair>> groups, int[][] board) {
        ArrayList<Pair> targetGroup = null;

        for (ArrayList<Pair> group : groups) {
            if (group.contains(x)) { // dapatkan grup dengan x
                for (Pair p : group) {

                    p.value = 0; // ubah value dari pair

                    board[p.row][p.col] = 0; // update board
                }
                targetGroup = group;
                break; // selesai, keluar loop groups
            }
        }

        groups.remove(targetGroup);
    }

    private int getNewNB(Boolean[] checkedNb, int total, Pair cell) { // return angka yang di normalisasi
                                                                      // (1-4)
        Random rd = new Random();
        int count = 0;

        int nb = -1;
        int arah;

        // menghitung berapa yang belum dicek
        for (boolean b : checkedNb) {
            if (!b)
                count++;
        }

        if (count == 0) {
            return -1; // return jika semua sudah di cek
        }

        for (int i = 0; i < checkedNb.length; i++) {
            if (mapDirection(i + 1, cell).value == total) {
                nb = i;
                break;
            }
        }

        return nb; // hasil tetap 1..length

    }

    private Boolean haveXNeighbour(Boolean[] checkedNB, Pair cell, int x, int[][] board) {
        Boolean res = false;
        Pair NB;

        for (int i = 0; i < checkedNB.length; i++) {
            if (checkedNB[i] == false) {
                NB = (mapDirection((i + 1), cell));

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

    private Pair mapDirection(int index, Pair cell) { // terima 1-4

        Pair NB;

        // mapping
        if (index == 1) {
            NB = new Pair(cell.row - 1, cell.col, board[cell.row - 1][cell.col]);
        } else if (index == 2) {
            NB = new Pair(cell.row, cell.col - 1, board[cell.row][cell.col - 1]);
        } else if (index == 3) {
            NB = new Pair(cell.row, cell.col + 1, board[cell.row][cell.col + 1]);
        } else {
            NB = new Pair(cell.row + 1, cell.col, board[cell.row + 1][cell.col]);
        }

        return NB;
    }

    private Boolean[] initiateCheckedNB(Pair cell, Boolean[] checkedNB) {
        for (int i = 0; i < checkedNB.length; i++) {
            checkedNB[i] = false;
        }

        return checkedNB;
    }

    private Pair getRandomNB(Pair cell) {

        Random rd = new Random();
        Pair NB;
        int index = rd.nextInt(4);

        // mapping
        if (index == 1) {
            NB = new Pair(cell.row - 1, cell.col, board[cell.row - 1][cell.col]);
        } else if (index == 2) {
            NB = new Pair(cell.row + 1, cell.col, board[cell.row + 1][cell.col]);
        } else if (index == 3) {
            NB = new Pair(cell.row, cell.col - 1, board[cell.row][cell.col - 1]);
        } else {
            NB = new Pair(cell.row, cell.col + 1, board[cell.row][cell.col + 1]);
        }

        return NB;

    }

    private boolean isEmpty(int[][] board) {
        boolean res = false;
        int i = 0;

        while (res == false && i < board.length) {
            int j = 0;
            while (res == false && j < board.length) {
                if (board[i][j] == 0) { // jika board masih belum terisi penuh
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

    private List<ArrayList<Pair>> mergeGroup(int rowNB, int colNB, int row, int col, int total,
            List<ArrayList<Pair>> groups) {

        int idx1 = -1, idx2 = -1;

        // cari index grup pertama (row,col) dan grup kedua (rowNB,colNB)
        for (int i = 0; i < groups.size(); i++) {
            for (Pair p : groups.get(i)) {
                if (p.row == row && p.col == col) {
                    idx1 = i;
                }
                if (p.row == rowNB && p.col == colNB) {
                    idx2 = i;
                }
            }
            // jika sudah ketemu keduanya, hentikan loop
            if (idx1 != -1 && idx2 != -1)
                break;
        }

        // ambil grup
        ArrayList<Pair> group1 = groups.get(idx1);
        ArrayList<Pair> group2 = groups.get(idx2);

        // gabungkan semua anggota group2 ke group1
        group1.addAll(group2);

        // update nilai semua anggota di group1 dengan total
        for (Pair p : group1) {
            p.value = total;

            // update board di posisi row,col milik pair
            board[p.row][p.col] = p.value;
        }

        // hapus group2
        groups.remove(idx2);

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
        if (board[p.row - 1][p.col] > 0) {
            res++;
        }
        if (board[p.row + 1][p.col] > 0) {
            res++;
        }
        if (board[p.row][p.col - 1] > 0) {
            res++;
        }
        if (board[p.row][p.col + 1] > 0) {
            res++;
        }

        return res;
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner sn = new Scanner(System.in);

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
