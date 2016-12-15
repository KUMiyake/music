import jp.ac.kyoto_u.kuis.le4music.HotPaintScale;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;

/**
 * Created by a-rusi on 2016/12/02.
 */
public class ChartGenerator {

    private static final MatrixSeries buildMatrixSeries(
            final double[][] z,
            final int size1,
            final int size2,
            final boolean transpose
    ) {
        MatrixSeries ms;
        if (!transpose) {
            ms = new MatrixSeries("", size1, size2);
            for (int i = 0; i < size1; i++)
                for (int j = 0; j < size2; j++)
                    ms.update(i, j, z[i][j]);
        } else {
            ms = new MatrixSeries("", size2, size1);
            for (int i = 0; i < size1; i++)
                for (int j = 0; j < size2; j++)
                    ms.update(j, i, z[i][j]);
        }
        return ms;
    }

    public static final JFreeChart chartGenerate(
            final double[][] z,
            final double[] x,
            final double[] y,
            final String xAxisTitle,
            final String yAxisTitle,
            final double zMin,
            final double zMax,
            final boolean transpose
    ) {
        final int size1 = x.length;
        final int size2 = y.length;
        final MatrixSeries ms = buildMatrixSeries(z, size1, size2, transpose);
        final MatrixSeriesCollection msc = new MatrixSeriesCollection(ms);

    /* プロットの軸 */
        final NumberAxis axis1a = new NumberAxis(xAxisTitle + "-index");
        axis1a.setLowerMargin(0.0);
        axis1a.setUpperMargin(0.0);

        final NumberAxis axis2a = new NumberAxis(yAxisTitle + "-index");
        axis2a.setLowerMargin(0.0);
        axis2a.setUpperMargin(0.0);

        final NumberAxis axis1b = new NumberAxis(xAxisTitle);
        axis1b.setLowerBound(x[0]);
        axis1b.setUpperBound(x[size1 - 1]);

        final NumberAxis axis2b = new NumberAxis(yAxisTitle);
        axis2b.setLowerBound(y[0]);
        axis2b.setUpperBound(y[size2 - 1]);

    /* レンダラ（値と色の対応関係） */
        final XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setPaintScale(new HotPaintScale(zMin, zMax));

    /* JFreeChartの軸表示に関するバグへの対策 */
        final XYPlot plot = new XYPlot(msc, axis1a, axis2a, renderer);
        final String axisType = "NORMAL"; /* NORMAL | INDEX | BOTH */
        switch (axisType) {
            case "INDEX":
                break;
            case "BOTH":
                plot.setDomainAxis(1, axis1b);
                plot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
                plot.setRangeAxis(1, axis2b);
                plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
                break;
            case "NORMAL":
                plot.setDomainAxis(1, axis1b);
                plot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
                plot.setRangeAxis(1, axis2b);
                plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
                plot.getDomainAxis().setVisible(false);
                plot.getRangeAxis().setVisible(false);
                break;
        }

        final JFreeChart chart = new JFreeChart(plot);
        chart.removeLegend();
        return chart;
    }
}
