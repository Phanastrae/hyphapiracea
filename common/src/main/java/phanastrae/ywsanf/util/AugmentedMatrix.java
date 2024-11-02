package phanastrae.ywsanf.util;

import org.jetbrains.annotations.Nullable;

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
        matrix.swapRows(row1, row2);

        double v1 = vector.get(row1);
        double v2 = vector.get(row2);
        vector.set(v2, row1);
        vector.set(v1, row2);
    }

    public void multiply(int row, double value) {
        matrix.scaleRow(value, row);
        double v = vector.get(row);
        vector.set(v * value, row);
    }

    public void add(int rowTarget, int rowDest, double scale) {
        MatrixRow target = matrix.getRow(rowTarget);
        MatrixRow dest = matrix.getRow(rowDest);
        dest.addRow(target, scale);
        double v = vector.get(rowTarget);
        vector.add(v * scale, rowDest);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(this.rows + " by " + this.columns + "(+1) sized augmented matrix");
        for(int i = 0; i < this.rows; i++) {
            s.append("\n[ ");
            MatrixRow row = this.matrix.getRow(i);
            IndexValuePair ivp = row.first;
            for(int j = 0; j < this.columns; j++) {
                double v;
                if(ivp != null && ivp.index == j) {
                    v = ivp.getValue();
                    ivp = ivp.next;
                } else {
                    v = 0;
                }
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
        private MatrixRow[] rows;

        public Matrix(int rows, int columns) {
            this.rows = new MatrixRow[rows];
            for(int i = 0; i < rows; i++) {
                this.rows[i] = new MatrixRow(columns);
            }
        }

        public void swapRows(int row1, int row2) {
            MatrixRow row = this.rows[row1];
            this.rows[row1] = this.rows[row2];
            this.rows[row2] = row;
        }

        public void scaleRow(double value, int row) {
            this.rows[row].scale(value);
        }

        public void add(double value, int row, int column) {
            this.getRow(row).add(value, column);
        }

        public double get(int row, int column) {
            return this.getRow(row).get(column);
        }

        public MatrixRow getRow(int row) {
            return this.rows[row];
        }
    }

    public static class MatrixRow {

        @Nullable
        private IndexValuePair first;
        @Nullable
        private IndexValuePair diagonal;
        @Nullable
        private IndexValuePair marked;
        private int row;

        public MatrixRow(int row) {
            this.row = row;
        }

        public void add(double value, int column) {
            IndexValuePair pair = first;
            if(this.diagonal != null && this.diagonal.index <= column) {
                pair = this.diagonal;
            }
            IndexValuePair lastPair = null;
            while(pair != null) {
                if(pair.index < column) {
                    lastPair = pair;
                    pair = pair.next;
                } else if(pair.index == column) {
                    pair.add(value);
                    return;
                } else {
                    // pair.index > column
                    break;
                }
            }

            IndexValuePair newPair = new IndexValuePair(column);
            if(newPair.index == this.row) {
                this.diagonal = newPair;
            }
            newPair.setValue(value);
            if(lastPair != null) {
                lastPair.setNext(newPair);
            } else {
                this.first = newPair;
            }
            newPair.setNext(pair);
        }

        public double get(int column) {
            if(column == this.row) {
                return this.getDiagonalValue();
            }

            IndexValuePair pair = first;
            if(this.diagonal != null && this.diagonal.index <= column) {
                pair = this.diagonal;
            }
            while(pair != null) {
                if (pair.index == column) {
                    return pair.getValue();
                }

                pair = pair.getNext();
            }
            return 0;
        }

        public double getDiagonalValue() {
            if(this.diagonal != null) {
                return this.diagonal.value;
            } else {
                return 0;
            }
        }

        public void scale(double value) {
            IndexValuePair pair = first;
            while(pair != null) {
                pair.scale(value);
                pair = pair.getNext();
            }
        }

        public void addRow(MatrixRow row, double scale) {
            IndexValuePair otherPair = row.first;
            if(otherPair == null) {
                return;
            }
            IndexValuePair thisPair = this.first;
            if(this.diagonal != null && this.diagonal.index <= otherPair.index) {
                thisPair = this.diagonal;
            }
            IndexValuePair thisLastPair = null;
            while(otherPair != null) {
                if(thisPair == null) {
                    IndexValuePair newPair = new IndexValuePair(otherPair.index);
                    if(newPair.index == this.row) {
                        this.diagonal = newPair;
                    }
                    newPair.setValue(otherPair.value * scale);
                    if(thisLastPair != null) {
                        thisLastPair.setNext(newPair);
                    } else {
                        this.first = newPair;
                    }

                    thisLastPair = newPair;
                    otherPair = otherPair.next;
                } else if(thisPair.index < otherPair.index) {
                    thisLastPair = thisPair;
                    thisPair = thisPair.next;
                } else if(thisPair.index == otherPair.index) {
                    thisPair.add(otherPair.value * scale);

                    thisLastPair = thisPair;
                    thisPair = thisPair.next;
                    otherPair = otherPair.next;
                } else {
                    // thisPair.index > otherPair.index
                    IndexValuePair newPair = new IndexValuePair(otherPair.index);
                    if(newPair.index == this.row) {
                        this.diagonal = newPair;
                    }
                    newPair.setValue(otherPair.value * scale);
                    if(thisLastPair != null) {
                        thisLastPair.setNext(newPair);
                    } else {
                        this.first = newPair;
                    }
                    newPair.setNext(thisPair);

                    thisLastPair = newPair;
                    otherPair = otherPair.next;
                }
            }
        }

        @Nullable
        public IndexValuePair getMarked() {
            return marked;
        }

        public void setMarked(@Nullable IndexValuePair marked) {
            this.marked = marked;
        }

        public @Nullable IndexValuePair getFirst() {
            return first;
        }
    }

    public static class IndexValuePair {
        private final int index;
        private double value;
        @Nullable
        private IndexValuePair next;

        public IndexValuePair(int index) {
            this.index = index;
        }

        public double getValue() {
            return value;
        }

        public int getIndex() {
            return index;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public void add(double value) {
            this.value += value;
        }

        public void scale(double scale) {
            this.value *= scale;
        }

        public void setNext(@Nullable IndexValuePair next) {
            this.next = next;
        }

        @Nullable
        public IndexValuePair getNext() {
            return next;
        }
    }

    public static class Vector {
        private final double[] data;

        public Vector(int length) {
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
