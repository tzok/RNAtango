package pl.poznan.put.rnatangoengine.logic.manyManyProcessing;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalMatrix;
import pl.poznan.put.comparison.global.GlobalResult;
import pl.poznan.put.comparison.global.ParallelGlobalComparator.CompareCallable;
import pl.poznan.put.matching.StructureSelection;

public class GlobalAnalysis extends Thread {
  private final GlobalComparator comparator;
  private final List<StructureSelection> structures;
  private ThreadPoolExecutor threadPool = null;
  private ExecutorCompletionService<CompareCallable.SingleResult> executor = null;
  private GlobalMatrix matrix;

  public GlobalAnalysis(GlobalComparator comparator, List<StructureSelection> structures) {
    this.comparator = comparator;
    this.structures = Collections.unmodifiableList(structures);
  }

  public final void run() {
    this.submitAll();
    this.waitForCompletion();
    List<String> names = this.collectNames();
    GlobalResult[][] results = this.fillResultsMatrix();
    this.matrix = new GlobalMatrix(this.comparator, names, results);
  }

  public GlobalMatrix getResult() {
    return this.matrix;
  }

  private void submitAll() {
    this.threadPool =
        (ThreadPoolExecutor)
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    this.executor = new ExecutorCompletionService(this.threadPool);
    int size = this.structures.size();

    for (int i = 0; i < size; ++i) {
      for (int j = i + 1; j < size; ++j) {
        Callable<CompareCallable.SingleResult> task =
            new CompareCallable(this.comparator, this.structures, i, j);
        this.executor.submit(task);
      }
    }
  }

  private void waitForCompletion() {
    this.threadPool.shutdown();
    long size = (long) this.structures.size();
    long all = size * (size - 1L) / 2L;

    long completed;
    while ((completed = this.threadPool.getCompletedTaskCount()) < all) {

      try {
        Thread.sleep(500L);
      } catch (InterruptedException var8) {
        this.interrupt();
      }
    }
  }

  private List<String> collectNames() {
    return (List)
        this.structures.stream().map(StructureSelection::getName).collect(Collectors.toList());
  }

  private GlobalResult[][] fillResultsMatrix() {
    int size = this.structures.size();
    GlobalResult[][] results =
        (GlobalResult[][])
            IntStream.range(0, size)
                .mapToObj(
                    (ix) -> {
                      return new GlobalResult[size];
                    })
                .toArray(
                    (x$0) -> {
                      return new GlobalResult[x$0][];
                    });
    int all = size * (size - 1) / 2;

    for (int i = 0; i < all; ++i) {
      try {
        CompareCallable.SingleResult result =
            (CompareCallable.SingleResult) this.executor.take().get();
        results[result.getI()][result.getJ()] = result.getValue();
        results[result.getJ()][result.getI()] = result.getValue();
      } catch (ExecutionException | InterruptedException var6) {
      }
    }

    return results;
  }

  public static class CompareCallable implements Callable<CompareCallable.SingleResult> {
    private final GlobalComparator comparator;
    private final int row;
    private final int column;
    private final StructureSelection s1;
    private final StructureSelection s2;

    private CompareCallable(
        final GlobalComparator comparator,
        final List<StructureSelection> structures,
        final int row,
        final int column) {
      super();
      this.comparator = comparator;
      s1 = structures.get(row);
      s2 = structures.get(column);
      this.row = row;
      this.column = column;
    }

    @Override
    public final SingleResult call() {
      final GlobalResult comp = comparator.compareGlobally(s1, s2);
      return new SingleResult(row, column, comp);
    }

    @Data
    static class SingleResult {
      private final int i;
      private final int j;
      private final GlobalResult value;
    }
  }
}
