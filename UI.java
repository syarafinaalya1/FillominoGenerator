import javax.swing.*;
import java.awt.*;

public class UI extends JFrame {

    private JPanel panelGrid; // panel untuk menampilkan grid papan
    private JLabel labelScore; // label untuk menampilkan score
    private JComboBox<String> pilihan1Box;

    public UI() {
        setTitle("Contoh UI Dua Kolom (Papan di Kiri)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new GridLayout(1, 2));

        // ===================== KOLOM KIRI =====================
        JPanel panelKiri = new JPanel(new BorderLayout());
        panelKiri.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel labelPapan = new JLabel("Papan Array 2D", SwingConstants.CENTER);
        labelPapan.setFont(new Font("Arial", Font.BOLD, 18));

        // panel grid awal (dummy 5x5)
        panelGrid = new JPanel();
        panelGrid.setLayout(new GridLayout(5, 5, 5, 5));
        generateDummyGrid(5);

        panelKiri.add(labelPapan, BorderLayout.NORTH);
        panelKiri.add(panelGrid, BorderLayout.CENTER);

        // ===================== KOLOM KANAN =====================
        JPanel panelKanan = new JPanel();
        panelKanan.setLayout(new BoxLayout(panelKanan, BoxLayout.Y_AXIS));
        panelKanan.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Dropdown hanya untuk ukuran
        JPanel panelDropdown = new JPanel(new GridLayout(1, 2, 10, 10));
        panelDropdown.add(new JLabel("Ukuran Board:"));

        pilihan1Box = new JComboBox<>(new String[] {
                "4", "6", "8", "10", "12", "14", "16", "18", "20", "22"
        });
        panelDropdown.add(pilihan1Box);

        // Tombol Generate Board
        JButton btnGenerate = new JButton("Generate New Board");
        btnGenerate.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnGenerate.addActionListener(e -> {
            int size = Integer.parseInt((String) pilihan1Box.getSelectedItem());
            generateBoard(size);
        });

        // Panel Difficulty
        JPanel panelDifficulty = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelDifficulty.add(new JLabel("Difficulty : "));
        labelScore = new JLabel("0");
        labelScore.setFont(new Font("Arial", Font.BOLD, 14));
        panelDifficulty.add(labelScore);

        // Masukkan ke panel kanan
        panelKanan.add(panelDropdown);
        panelKanan.add(Box.createVerticalStrut(20));
        panelKanan.add(btnGenerate);
        panelKanan.add(Box.createVerticalStrut(20));
        panelKanan.add(panelDifficulty);
        panelKanan.add(Box.createVerticalGlue());

        add(panelKiri);
        add(panelKanan);

        setVisible(true);
    }

    // ===================== GENERATE BOARD =====================

    private void generateBoard(int size) {

        int[][] papan = new int[size][size];

        // LOGIKA EXACT seperti Main.java Anda
        Board b = new Board(size);

        papan = b.generateBoard();
        printBoard(papan);

        papan = b.pruneBoard(papan);
        Difficulty d = new Difficulty(papan);
        System.out.println(d.scoreK1(papan));
        System.out.println(d.scoreK2(papan));
        System.out.println(d.scoreK3(papan));
        System.out.println(d.scoreK4(papan));
        System.out.println(d.scoreK1(d.board));
        System.out.println(d.scoreK2(d.board));
        System.out.println(d.scoreK3(d.board));
        System.out.println(d.scoreK4(d.board));
        double score = d.getScore();
        System.out.println(score);
        // TAMPILKAN BOARD KE UI
        updateGrid(papan);

        // UPDATE SCORE
        labelScore.setText(String.valueOf(score));
    }

    void printBoard(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    // ===================== DRAW GRID =====================

    private void generateDummyGrid(int size) {
        panelGrid.removeAll();
        for (int i = 1; i <= size * size; i++) {
            JButton cell = new JButton(String.valueOf(i));
            cell.setEnabled(false);
            panelGrid.add(cell);
        }
        panelGrid.revalidate();
        panelGrid.repaint();
    }

    private void updateGrid(int[][] papan) {
        panelGrid.removeAll();

        int size = papan.length;
        panelGrid.setLayout(new GridLayout(size, size, 5, 5));

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {

                String text = (papan[i][j] == 0) ? "" : String.valueOf(papan[i][j]);
                JButton cell = new JButton(text);

                cell.setEnabled(false);
                cell.setBackground(Color.WHITE);

                panelGrid.add(cell);
            }
        }

        panelGrid.revalidate();
        panelGrid.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UI::new);
    }
}
