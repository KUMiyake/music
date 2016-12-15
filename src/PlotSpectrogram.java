import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JFrame;

import jp.ac.kyoto_u.kuis.le4music.SingleXYArrayDataset;
import jp.ac.kyoto_u.kuis.le4music.HotPaintScale;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.MathArrays;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Plot;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.MatrixSeries;
import org.jfree.data.xy.MatrixSeriesCollection;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class PlotSpectrogram {  /*波形を短時間フーリエ変換し，スペクトログラムをプロットする*/

    public static final JFreeChart GenerateSpectrogramChart(
            final double[] waveform,
            final double sampleRate,
            final double windowDuration,
            final double windowShift) {
         /* 窓関数とFFTのサンプル数 */
        final int windowSize = (int) Math.round(windowDuration * sampleRate);
        final int fftSize = 1 << Le4MusicUtils.nextPow2(windowSize);
         /* シフトのサンプル数 */
        final int shiftSize = (int) Math.round(windowShift * sampleRate);
         /* 窓関数を求め，それを正規化する*/

        final double[] window = MathArrays.normalizeArray(Arrays.copyOf(Le4MusicUtils.hanning(windowSize), fftSize), 1.0);
         /* 各フーリエ変換係数に対応する周波数*/
        final double[] freqs = IntStream.rangeClosed(0, fftSize / 2)
                .mapToDouble(i -> i * sampleRate / fftSize).toArray();
         /*短時間フーリエ変換本体*/
        final Stream<Complex[]> spectrogram =
                Le4MusicUtils.sliding(waveform, window, shiftSize).map(frame -> Le4MusicUtils.rfft(frame));
         /*複素スペクトログラムを対数振幅スペクトログラムに */
        final double[][] specLog =
                spectrogram.map(sp -> Arrays.stream(sp)
                        .mapToDouble(c -> 20.0 * Math.log10(c.abs())).toArray()).toArray(n -> new double[n][]);
        /* フレーム数と各フレーム先頭位置の時刻*/
        final double[] times = IntStream.range(0, specLog.length)
                .mapToDouble(i -> i * windowShift).toArray();
         /* プロット*/

         JFreeChart chart = ChartGenerator.chartGenerate(specLog, times, freqs, "Time [sec.]", "Frequency [Hz]", -100.0, 0.0, true);
         chart.setTitle("Spectrogram");
         return chart;
    }

    public static final void main(final String[] args) throws IOException, UnsupportedAudioFileException

    {
        if (args.length == 0) {
            System.out.println("no input files");
            return;

        }
        final File wavFile = new File(args[0]);
        /* 音響信号読み込み */
        final AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
        final AudioFormat format = stream.getFormat();
        final double sampleRate = format.getSampleRate();
        final double[] waveform = Le4MusicUtils.readWaveformMonaural(stream);
        stream.close();

        final JFrame frame = Plot.createJFrame(GenerateSpectrogramChart(waveform, sampleRate, Le4MusicUtils.frameDuration, Le4MusicUtils.shiftDuration),"plot");
    }
}