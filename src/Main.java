import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import java.util.List;

import java.util.*;

public class Main extends JFrame {
    private static final int CELL_SIZE = 20;
    private int gridSize;
    private Cell[][] grid;
    private JPanel gridPanel;

    public Main(int gridSize) {
        this.gridSize = gridSize;
        this.grid = new Cell[gridSize][gridSize];
        this.gridPanel = new JPanel(new GridLayout(gridSize, gridSize));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(gridSize * CELL_SIZE, gridSize * CELL_SIZE);
        setTitle("Maze");

        initializeGrid();

        dfsGenerateMaze(grid[0][0]);

        add(gridPanel);
        setVisible(true);
        setResizable(false);
        setLocationRelativeTo(null);
        gridPanel.repaint();
        gridPanel.revalidate();
    }

    private void initializeGrid() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                grid[row][col] = new Cell(row, col);
                gridPanel.add(grid[row][col]);
            }
        }
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Cell current = grid[row][col];
                if (row > 0) current.neighbors.add(grid[row - 1][col]);
                if (row < gridSize - 1) current.neighbors.add(grid[row + 1][col]);
                if (col > 0) current.neighbors.add(grid[row][col - 1]);
                if (col < gridSize - 1) current.neighbors.add(grid[row][col + 1]);
            }
        }
    }

    private void dfsGenerateMaze(Cell start) {
        Stack<Cell> stack = new Stack<>();
        start.setVisited(true);
        stack.push(start);
        Timer timer = new Timer(0, null);

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!stack.isEmpty()) {
                    Cell current = stack.peek();

                    List<Cell> unvisitedNeighbors = current.getUnvisitedNeighbors();
                    if (!unvisitedNeighbors.isEmpty()) {
                        Cell next = unvisitedNeighbors.get(new Random().nextInt(unvisitedNeighbors.size()));
                        next.setVisited(true);
                        removeWall(current, next);
                        stack.push(next);
                        gridPanel.repaint();
                    } else {
                        stack.pop();
                    }
                } else {
                    timer.stop();
                    grid[0][0].removeLeftWall();
                    grid[0][0].removeTopWall();

                    grid[gridSize - 1][gridSize - 1].removeRightWall();
                    grid[gridSize - 1][gridSize - 1].removeBottomWall();

                    gridPanel.repaint();

                    List<Cell> path = findPath();
                    drawPath(path);
                }
            }
        });

        timer.start();
    }

    private List<Cell> findPath() {
        Stack<Cell> stack = new Stack<>();
        Map<Cell, Cell> cameFrom = new HashMap<>();
        List<Cell> path = new ArrayList<>();

        stack.push(grid[0][0]);
        cameFrom.put(grid[0][0], null);

        while (!stack.isEmpty()) {
            Cell current = stack.pop();

            if (current == grid[gridSize - 1][gridSize - 1]) {
                Cell step = current;
                while (step != null) {
                    path.add(step);
                    step = cameFrom.get(step);
                }
                Collections.reverse(path);
                return path;
            }

            for (Cell neighbor : current.neighbors) {
                if (!cameFrom.containsKey(neighbor) && !isWallBetween(current, neighbor)) {
                    stack.push(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }

        return path;
    }

    private boolean isWallBetween(Cell current, Cell neighbor) {
        int dx = neighbor.col - current.col;
        int dy = neighbor.row - current.row;

        if (dx == 1) {
            return current.rightWall;
        } else if (dx == -1) {
            return current.leftWall;
        } else if (dy == 1) {
            return current.bottomWall;
        } else if (dy == -1) {
            return current.topWall;
        }
        return true;
    }

    private void drawPath(List<Cell> path) {
        for (Cell cell : path) {
            cell.setBackground(Color.magenta);
        }
    }

    private void removeWall(Cell current, Cell next) {
        int dx = next.col - current.col;
        int dy = next.row - current.row;

        if (dx == 1) {
            current.removeRightWall();
            next.removeLeftWall();
        } else if (dx == -1) {
            current.removeLeftWall();
            next.removeRightWall();
        } else if (dy == 1) {
            current.removeBottomWall();
            next.removeTopWall();
        } else if (dy == -1) {
            current.removeTopWall();
            next.removeBottomWall();
        }
    }

    public class Cell extends JPanel {
        int row, col;
        boolean visited = false;
        List<Cell> neighbors = new ArrayList<>();
        boolean topWall = true, rightWall = true, bottomWall = true, leftWall = true;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
            setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            setBackground(Color.WHITE);
            updateWalls();
        }

        public void setVisited(boolean visited) {
            this.visited = visited;
            setBackground(visited ? Color.CYAN : Color.WHITE);
        }

        public List<Cell> getUnvisitedNeighbors() {
            List<Cell> unvisitedNeighbors = new ArrayList<>();
            for (Cell neighbor : neighbors) {
                if (!neighbor.visited) {
                    unvisitedNeighbors.add(neighbor);
                }
            }
            return unvisitedNeighbors;
        }

        public void removeTopWall() {
            topWall = false;
            updateWalls();
        }

        public void removeRightWall() {
            rightWall = false;
            updateWalls();
        }

        public void removeBottomWall() {
            bottomWall = false;
            updateWalls();
        }

        public void removeLeftWall() {
            leftWall = false;
            updateWalls();
        }

        private void updateWalls() {
            int top = topWall ? 1 : 0;
            int left = leftWall ? 1 : 0;
            int bottom = bottomWall ? 1 : 0;
            int right = rightWall ? 1 : 0;
            setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String input = JOptionPane.showInputDialog("Enter the grid size:");
                int gridSize;
                try {
                    gridSize = Integer.parseInt(input);
                    new Main(gridSize);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.");
                }
            }
        });
    }
}
