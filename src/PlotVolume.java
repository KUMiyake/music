import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JFrame;

import jp.ac.kyoto_u.kuis.le4music.SingleXYArrayDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Plot;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class PlotVolume {
    /*波形をフーリエ変換し，スペクトルを対数(dB)スケールでプロットする*/

    static int DIVIDE_SIZE = 1024;

    //和を計算する
    private static double getSigma(double[] spectrum){
        return Arrays.stream(spectrum).map(c -> c * c).sum();
    }

    private static double getVokumeValue(final double[] waveform, final double sampleRate){
         /* 信号の長さをfftSizeに伸ばし，長さが足りない部分は0で埋める．  *振幅を信号長で正規化する． */
        final double[] src = Arrays.stream(Arrays.copyOf(waveform, DIVIDE_SIZE)).map(w -> w / waveform.length)
                .toArray();
         /*対数振幅スペクトルを求める*/
        return 20.0 * Math.log10(Math.sqrt(getSigma(src) / DIVIDE_SIZE));
    }

    public static final JFreeChart GenerateLoudnessChart(final double[] waveform, final double sampleRate) {
         /* fftSize = 2ˆp >= waveform.lengthを満たすfftSizeを求める * 2ˆpはシフト演算で求められる*/
        final int fftSize = 1 << Le4MusicUtils.nextPow2(waveform.length);
         /* 信号の長さをfftSizeに伸ばし，長さが足りない部分は0で埋める．  *振幅を信号長で正規化する． */
        final double[] src = Arrays.stream(Arrays.copyOf(waveform, fftSize)).map(w -> w / waveform.length)
                .toArray();
        int divide_num = fftSize / DIVIDE_SIZE;
        double[] loudness = new double[divide_num];
        for(int i = 0; i < divide_num ;i++){
            loudness[i] = getVokumeValue(Arrays.copyOfRange(src,i * DIVIDE_SIZE, (i + 1) * DIVIDE_SIZE),sampleRate);
        }

        /* フレーム数と各フレーム先頭位置の時刻*/
        final double[] times = IntStream.range(0, loudness.length).mapToDouble(i -> i * DIVIDE_SIZE / sampleRate).toArray();

         /*プロットする*/
        final JFreeChart chart = ChartFactory.createXYLineChart(
                null,null,null,new SingleXYArrayDataset(times, loudness));

        chart.removeLegend();
        chart.setTitle("Loudness");
        final XYPlot plot = chart.getXYPlot();
        final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setRange(0.0, (waveform.length - 1) / sampleRate);
        xAxis.setLabel("Time[sec.]");
        final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(-220.0, -150.0);
        yAxis.setLabel("Amplitude");
        return chart;
    }

    public static final void main(final String[] args) throws IOException, UnsupportedAudioFileException

    {
        if (args.length == 0) {
            System.out.println("no input files");
            return;

        }
        final File wavFile = new File(args[0]);
        /*音響信号読み込み*/
        final AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
        final double[] waveform = Le4MusicUtils.readWaveformMonaural(stream);
        final AudioFormat format = stream.getFormat();
        final double sampleRate = format.getSampleRate();
        stream.close();
        final JFrame frame = Plot.createJFrame(GenerateLoudnessChart(waveform, sampleRate),"plot");
    }
}
