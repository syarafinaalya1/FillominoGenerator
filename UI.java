import javax.swing.*;
import java.awt.*;

public class UI extends JFrame {

    private JPanel panelGrid; // panel u/ grif
    private JLabel labelScore; // label u/score
    private JComboBox<String> pilihan1Box;

    public UI() {
        setTitle("Generator Fillomino");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new GridLayout(1, 2));

        // 1/2

        JPanel panelKiri = new JPanel(new BorderLayout());
        panelKiri.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel labelPapan = new JLabel("Papan Array 2D", SwingConstants.CENTER);
        labelPapan.setFont(new Font("Arial", Font.BOLD, 18));

        // grid awal
        panelGrid = new JPanel();
        panelGrid.setLayout(new GridLayout(5, 5, 5, 5));
        generateDummyGrid(5); // u/ awalan

        panelKiri.add(labelPapan, BorderLayout.NORTH);
        panelKiri.add(panelGrid, BorderLayout.CENTER);

        // 2/2

        JPanel panelKanan = new JPanel();
        panelKanan.setLayout(new BoxLayout(panelKanan, BoxLayout.Y_AXIS));
        panelKanan.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Dropdown ukuran
        JPanel panelDropdown = new JPanel(new GridLayout(1, 2, 10, 10));
        panelDropdown.add(new JLabel("Ukuran Board:"));

        pilihan1Box = new JComboBox<>(new String[] {
                "4 x 4 ", "6 x 6", "8 x 8", "10 x 10", "12 x 12", "14 x 14", "16 x 16", "18 x 18", "20 x 20", "22 x 22"
        });
        panelDropdown.add(pilihan1Box);

        // Btn Generate Board
        JButton btnGenerate = new JButton("Generate New Board");
        btnGenerate.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnGenerate.addActionListener(e -> {
            String selected = (String) pilihan1Box.getSelectedItem();
            int size = Integer.parseInt(selected.split("x")[0].trim());

            generateBoard(size);
        });

        // Panel Difficulty
        JPanel panelDifficulty = new JPanel(new FlowLayout(FlowLayout.LEFT));
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

    // Logic u/ main
    private void generateBoard(int size) {

        int[][] papan = new int[size][size];

        Board b = new Board(size);

        papan = b.generateBoard();
        printBoard(papan);

        papan = b.pruneBoard(papan);
        Difficulty d = new Difficulty(papan);
        System.out.println("k1:" + d.scoreK1(d.board));
        System.out.println("k2:" + d.scoreK2(d.board));
        System.out.println("k3:" + d.scoreK3(d.board));
        System.out.println("k4:" + d.scoreK4(d.board));
        double score = d.getScore();
        System.out.println(score);

        // UI update
        updateGrid(papan);
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
