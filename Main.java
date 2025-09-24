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
        Pair cell;
        Pair NB = new Pair(0, 0, 0);

        while (isEmpty(board)) {
            int row, col;
            row = rd.nextInt();
            col = rd.nextInt();
            cell = new Pair(row, col, 0); // inisiasi 0

            int total = 0;

            // jika tidak punya tetangga
            if (countNB(cell, board) == 0) {
                cell.value = 1;
                board[cell.col][cell.row] = cell.value;
            }

            // jika punya 1 tetangga
            else if (countNB(cell, board) == 1) {

                // lakukan pencarian neighbor dan dapatkan posisinya
                if (board[cell.row - 1][cell.col] > 0) {
                    NB = new Pair(cell.row - 1, cell.col, board[cell.row - 1][cell.col]);
                } else if (board[cell.row + 1][cell.col] > 0) {
                    NB = new Pair(cell.row + 1, cell.col, board[cell.row + 1][cell.col]);
                } else if (board[cell.row][cell.col - 1] > 0) {
                    NB = new Pair(cell.row, cell.col - 1, board[cell.row][cell.col - 1]);
                } else {
                    NB = new Pair(cell.row, cell.col + 1, board[cell.row][cell.col + 1]);
                }

                total = NB.value + 1;

                board[NB.col][NB.row] = total;
                board[cell.col][cell.row] = total;
            }

            // jika tetangga lebih dari 1
            else if (countNB(cell, board) > 1) {
                checkedNB = initiateCheckedNB(cell, null);

                int check = rd.nextInt(4);// mendapatkan tetangga yang belum di cek

                boolean direction = true;

                while (direction == true) { // direction untuk menyimpan state NB sudah di dapatkan atau belum
                    // jika NB tersebut belum di check
                    if (checkedNB[check] == false) {
                        checkedNB[check] = true; // ubah state nb jadi sudah di cek 1.1
                        NB = mapDirection(check + 1, cell); // dapatkan NB
                        direction = false;

                        // proses nb baru
                        total = NB.value + cell.value;

                        if (total <= 9) {
                            mergeGroup(NB.row, NB.col, cell.row, cell.col, total, groups);

                            while (haveXNeighbour(checkedNB, cell, total, board) == true) {// selama masih ada ttg yang
                                                                                           // memiliki nilai = total..
                                int arahNB = getNewNB(checkedNB); // mendapatkan koordinat nb baru yang == total
                                NB = mapDirection(arahNB, cell); // dapatlkan nb baru dari arah yang baru (1-4)
                                checkedNB[arahNB - 1] = true;// update checked neighbor jadi true (di denormalisasikan
                                                             // lagi kaarena arah nb 1-4)

                                if (total + NB.value <= 9) {// iff conditional jika dapat di merge maka merge
                                    total = total + NB.value; // total diperbaharui
                                    mergeGroup(NB.row, NB.col, cell.row, cell.col, total, groups); // lalu merge
                                                                                                   // dilakukan
                                } else {// else ganti satu grup jadi 0
                                    deleteGroup(NB, groups, board);
                                }

                                arahNB = getNewNB(checkedNB); // cari neihbor lain ((sudah auto jika di while))
                                NB = mapDirection(arahNB, cell);
                            }

                        } else {
                            // cari nb baru
                            check = rd.nextInt(4);
                            direction = true;

                        }
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
    }

    private void deleteGroup(Pair x, List<ArrayList<Pair>> groups, int[][] board) {
        ArrayList<Pair> targetGroup = null;

        for (ArrayList<Pair> group : groups) {
            if (group.contains(x)) { // dapatkan grup dengan x
                for (Pair p : group) {

                    p.value = 0; // ubah value dari pair

                    board[p.row][p.col] = 0; // update board
                }
                break; // selesai, keluar loop groups
            }
        }

        groups.remove(targetGroup);
    }

    private int getNewNB(Boolean[] checkedNb) { // return angka yang di normalisasi (1-4)
        Random rd = new Random();
        int count = 0;

        // menghitung berapa yang belum dicek
        for (boolean b : checkedNb) {
            if (!b)
                count++;
        }

        if (count == 0) {
            return -1; // return jika semua sudah di cek
        }

        int nb;

        do {
            nb = rd.nextInt(checkedNb.length) + 1; // 1-4.
        } while (checkedNb[nb - 1]); // di sesuaikan hingga 0-3

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

    private Pair mapDirection(int index, Pair cell) {

        Pair NB;

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

    private int countNB(Pair p, int[][] board) {
        // untuk mengecek jika sel mempunyai tetangga atau tidak sekaligus
        // menghitungjika iya, berapa tetangga yang dimilikinya
        int res = 0;

        if (board[p.row - 1][p.col] > 0) {
            res++;
        } else if (board[p.row + 1][p.col] > 0) {
            res++;
        } else if (board[p.row][p.col - 1] > 0) {
            res++;
        } else if (board[p.row + 1][p.col + 1] > 0) {
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
