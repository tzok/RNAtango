package pl.poznan.put.svg;

import org.apache.commons.math3.util.Precision;
import org.biojava.nbio.structure.jama.EigenvalueDecomposition;
import org.biojava.nbio.structure.jama.Matrix;

/**
 * A utility class implementing a Multidimensional Scaling method.
 *
 * @author tzok
 */
final class MDS {
  private MDS() {
    // empty constructor
    super();
  }

  /**
   * Calculate the Multidimensional Scaling. It gets a distance matrix and creates a map of points
   * in N-dimensions whose mutual distances correspond to the given input matrix.
   *
   * @param distance A distance matrix, NxN.
   * @param dimensions Desired number of dimensions, K.
   * @return A matrix NxK, where for each row there are K coordinates.
   */
  public static double[][] multidimensionalScaling(
      final double[][] distance, final int dimensions) {
    MDS.checkSymmetry(distance);

    /*
     * calculate D as distance_ij^2
     */
    final double[][] d = new double[distance.length][];
    for (int i = 0; i < distance.length; ++i) {
      d[i] = new double[distance.length];
      for (int j = 0; j < distance.length; ++j) {
        d[i][j] = distance[i][j] * distance[i][j];
      }
    }

    /*
     * calculate mean for each row, column and whole matrix
     */
    final double[] meanRow = new double[distance.length];
    final double[] meanColumn = new double[distance.length];
    double meanMatrix = 0;
    for (int i = 0; i < distance.length; ++i) {
      for (int j = 0; j < distance.length; ++j) {
        meanRow[i] += d[i][j];
        meanColumn[j] += d[i][j];
        meanMatrix += d[i][j];
      }
    }
    for (int i = 0; i < distance.length; ++i) {
      meanRow[i] /= distance.length;
      meanColumn[i] /= distance.length;
    }
    meanMatrix = meanMatrix / distance.length * distance.length;

    /*
     * calculate B: b_ij = -1/2 * (d_ij - meanRow[i] - meanColumn[j] +
     * meanMatrix)
     */
    final double[][] b = new double[distance.length][];
    for (int i = 0; i < distance.length; ++i) {
      b[i] = new double[distance.length];
      for (int j = 0; j < distance.length; ++j) {
        b[i][j] = -0.5 * ((d[i][j] - meanRow[i] - meanColumn[j]) + meanMatrix);
      }
    }

    /*
     * decompose B = VDV^T (or else called KLK^T)
     */
    final EigenvalueDecomposition evd = new EigenvalueDecomposition(new Matrix(b));

    /*
     * find maxima in L
     */
    final double[][] l = evd.getD().getArrayCopy();
    final int[] maxima = new int[dimensions];
    for (int i = 0; i < dimensions; ++i) {
      int max = 0;
      for (int j = 1; j < l.length; ++j) {
        if (l[j][j] > l[max][max]) {
          max = j;
        }
      }
      // if L[max][max] < 0, then it's impossible to visualise
      if (l[max][max] < 0) {
        throw new IllegalArgumentException("Cannot visualize specified structures in 2D");
      }
      maxima[i] = max;
      l[max][max] = Double.NEGATIVE_INFINITY;
    }

    /*
     * get sqrt() from those maxima in L
     */
    final double[][] l2 = evd.getD().getArrayCopy();
    for (int i = 0; i < dimensions; ++i) {
      l2[maxima[i]][maxima[i]] = Math.sqrt(l2[maxima[i]][maxima[i]]);
    }

    /*
     * calculate X coordinates for visualisation
     */
    final double[][] x = new double[distance.length][];
    final double[][] k = evd.getV().getArray();
    for (int i = 0; i < distance.length; ++i) {
      x[i] = new double[dimensions];
      for (int j = 0; j < dimensions; ++j) {
        x[i][j] = k[i][maxima[j]] * l2[maxima[j]][maxima[j]];
      }
    }
    return x;
  }

  private static void checkSymmetry(final double[][] distance) {
    /*
     * sanity check (symmetric, square matrix as input)
     */
    final String msg = "Distance matrix is not symmetrical!";
    for (int i = 0; i < distance.length; ++i) {
      if (distance[i].length != distance.length) {
        throw new IllegalArgumentException(msg);
      }
      for (int j = 0; j < distance[i].length; ++j) {
        if (!Precision.equals(distance[i][j], distance[j][i])) {
          throw new IllegalArgumentException(msg);
        }
      }
    }
  }
}
