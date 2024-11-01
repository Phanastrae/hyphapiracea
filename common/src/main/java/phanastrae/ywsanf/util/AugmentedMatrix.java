package phanastrae.ywsanf.util;

public class AugmentedMatrix {

    private int rows;
    private int columns;
    private Matrix matrix;
    private Vector vector;

    public AugmentedMatrix(int rows, int columns) {
        this.matrix = new Matrix(rows, columns);
        this.vector = new Vector(rows);
        this.rows = rows;
        this.columns = columns;
    }

    public int getRows() {
        return this.rows;
    }

    public int getColumns() {
        return columns;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public Vector getVector() {
        return vector;
    }

    public void swap(int row1, int row2) {
        for(int i = 0; i < columns; i++) {
            double v1 = matrix.get(row1, i);
            double v2 = matrix.get(row2, i);
            matrix.set(v2, row1, i);
            matrix.set(v1, row2, i);
        }

        double v1 = vector.get(row1);
        double v2 = vector.get(row2);
        vector.set(v2, row1);
        vector.set(v1, row2);
    }

    public void multiply(int row, double value) {
        for(int i = 0; i < columns; i++) {
            double v = matrix.get(row, i);
            matrix.set(v * value, row, i);
        }
        double v = vector.get(row);
        vector.set(v * value, row);
    }

    public void add(int rowTarget, int rowDest, double scale) {
        for(int i = 0; i < columns; i++) {
            double v = matrix.get(rowTarget, i);
            matrix.add(v * scale, rowDest, i);
        }
        double v = vector.get(rowTarget);
        vector.add(v * scale, rowDest);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(this.rows + " by " + this.columns + "(+1) sized augmented matrix");
        for(int i = 0; i < this.rows; i++) {
            s.append("\n[ ");
            for(int j = 0; j < this.columns; j++) {
                double v = this.matrix.get(i, j);
                String vs = String.format("%1$,04.4f", v);
                s.append(vs).append(" ");
            }
            s.append("| ");
            double v = this.vector.get(i);
            String vs = String.format("%1$,04.4f", v);
            s.append(vs).append(" ]");
        }
        return s.toString();
    }

    public static class Matrix {
        private final int rows;
        private final int columns;

        public double[] data;

        public Matrix(int rows, int columns) {
            this.rows = rows;
            this.columns = columns;
            this.data = new double[rows * columns];
        }

        public void add(double value, int row, int column) {
            this.data[index(row, column)] += value;
        }

        public void set(double value, int row, int column) {
            this.data[index(row, column)] = value;
        }

        public double get(int row, int column) {
            return this.data[index(row, column)];
        }

        private int index(int row, int column) {
            return column + row * this.columns;
        }
    }

    public static class Vector {
        private final int length;
        private final double[] data;

        public Vector(int length) {
            this.length = length;
            this.data = new double[length];
        }

        public void add(double value, int row) {
            this.data[row] += value;
        }

        public void set(double value, int row) {
            this.data[row] = value;
        }

        public double get(int row) {
            return this.data[row];
        }
    }
}
