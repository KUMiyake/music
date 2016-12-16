import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JFrame;

import jp.ac.kyoto_u.kuis.le4music.SingleXYArrayDataset;
import org.apache.commons.math3.complex.Complex;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Plot;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class PlotSpectrum {
    /*波形をフーリエ変換し，スペクトルを対数(dB)スケールでプロットする*/

    public static final JFreeChart GenerateSpectrumChart(final double[] waveform, final double sampleRate) {
         /* fftSize = 2ˆp >= waveform.lengthを満たすfftSizeを求める * 2ˆpはシフト演算で求められる*/
        final int fftSize = 1 << Le4MusicUtils.nextPow2(waveform.length);
         /* 信号の長さをfftSizeに伸ばし，長さが足りない部分は0で埋める．  *振幅を信号長で正規化する． */
        final double[] src = Arrays.stream(Arrays.copyOf(waveform, fftSize)).map(w -> w / waveform.length)
                .toArray();
         /*高速フーリエ変換を行う*/
        final Complex[] spectrum = Le4MusicUtils.rfft(src);
         /*対数振幅スペクトルを求める*/
        final double[] specLog = Arrays.stream(spectrum)
                .mapToDouble(c -> 20.0 * Math.log10(c.abs())).toArray();
         /*周波数を求める．以下を満たすように線型に*freqs[0] = 0Hz 48 * freqs[fftSize2 - 1] = sampleRate / 2 (= Nyquist周波数) */
        final double[] freqs = IntStream.rangeClosed(0, fftSize >> 1)
                .mapToDouble(i -> i * sampleRate / fftSize).toArray();
        return ChartFactory.createXYLineChart(
                null,null,null,new SingleXYArrayDataset(freqs, specLog));
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
        final JFrame frame = Plot.createJFrame(GenerateSpectrumChart(waveform, sampleRate),"plot");

    }
}
