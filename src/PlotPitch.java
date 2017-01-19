import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Plot;
import jp.ac.kyoto_u.kuis.le4music.Recorder;
import jp.ac.kyoto_u.kuis.le4music.SingleXYArrayDataset;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils.sampleRate;

/**
 * Created by a-rusi on 2017/01/12.
 */
public class PlotPitch {
    static double pitchValue;
    static boolean isPlotPitch= false;
    static public double getPitch(final double[] frame) {

        /* fftSize = 2ˆp >= waveform.lengthを満たすfftSizeを求める * 2ˆpはシフト演算で求められる*/
        final int fftSize = 1 << Le4MusicUtils.nextPow2(frame.length);
        /* 信号の長さをfftSizeに伸ばし，長さが足りない部分は0で埋める．  *振幅を信号長で正規化する． */
        final double[] src = Arrays.stream(Arrays.copyOf(frame, fftSize)).map(w -> w / frame.length)
                .toArray();
        final Complex[] ffted_array = Le4MusicUtils.rfft(src);
        Complex[] powed = ComplexUtils.convertToComplex(Arrays.stream(ffted_array).mapToDouble(i -> i.abs() * i.abs()).toArray());
        double[] irffted_array = Le4MusicUtils.irfft(powed);

        int peak_max = 0;
        boolean initialized = false;

        double[] data = irffted_array;

        ArrayList<Double> peakVals = new ArrayList<Double>();
        ArrayList<Integer> peakIndex= new ArrayList<Integer>();
        for(int i = 2;i < (data.length / 2);i++){
            if(data[i - 1] - data[i - 2] >= 0 && data[i] - data[i-1] < 0){
                peakVals.add(data[i - 1]);
                peakIndex.add(i);
            }
        }
        if(peakIndex.size() == 0){
            return 0;
        }
        else {
            return Le4MusicUtils.sampleRate / (peakIndex.get(peakVals.indexOf(Collections.max(peakVals)))-1);
        }

//
//        for(int i = 1;i < irffted_array.length - 1 ; i++){
//            if(irffted_array[i - 1] < irffted_array[i] &&
//                    irffted_array[i] > irffted_array[i + 1]) {
//                if(!initialized){
//                    initialized = true;
//                }
//                else if(peak_max == 0){
//                     peak_max = i;
//                }
//                else if(irffted_array[peak_max] < irffted_array[i])
//                    peak_max = i;
//            }
//        }
//        if(peak_max == 0)
//            return 0;
//        return Le4MusicUtils.sampleRate / peak_max;
    }
    static public String getNote(double pitch){
        double noteValue[] = {4186.0,
                3951.1, 3729.3, 3520.0, 3322.4, 3136.0, 2960.0, 2793.8, 2637.0, 2489.0, 2349.3, 2217.5, 2093.0,
                1975.5, 1864.7, 1760.0, 1661.2, 1568.0, 1480.0, 1396.9, 1318.5, 1244.5, 1174.7, 1108.7, 1046.5,
                987.77, 932.33, 880.00, 830.61, 783.99, 739.99, 698.46, 659.26, 622.25, 587.33, 554.37, 523.25,
                493.88, 466.16, 440.00, 415.30, 392.00, 369.99, 349.23, 329.63, 311.13, 293.66, 277.18, 261.63,
                246.94, 233.08, 220.00, 207.65, 196.00, 185.00, 174.61, 164.81, 155.56, 146.83, 138.59, 130.81,
                123.47, 116.54, 110.00, 103.83, 97.999, 92.499, 87.307, 82.407, 77.782, 73.416, 69.296, 65.406,
                61.735, 58.270, 55.000, 51.913, 48.999, 46.249, 43.654, 41.203, 38.891, 36.708, 34.648, 32.703,
                30.868, 29.135, 27.500
        };
        String note_name[] = {"B","A#","A","G#","G","F#","F","E","D#","D","C#","C"};
        if(pitch > noteValue[0]) return "C8";
        for(int i = 1,oct = 7,counter = 0;i < noteValue.length;i++){
            if(noteValue[i] < pitch){
                return note_name[counter] + Integer.toString(oct);
            }
            counter++;
            if(counter == 12) {
                oct--;
                counter = 0;
            }
        }
        return "none";
    }
    static public JLabel generatePitchMonitorLabel(){
        JLabel noteMonitorLabel = new JLabel();
        noteMonitorLabel.setFont(new Font("ＭＳ ゴシック", Font.BOLD ,64));
        final ScheduledExecutorService executor =
                Le4MusicUtils.newSingleDaemonThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(
                () -> {
                    if(isPlotPitch)
                        noteMonitorLabel.setText(getNote(pitchValue));
                },
        /* initialDelay = */ 0L,
        /* delay = */ 1L,
                TimeUnit.MILLISECONDS
        );
        return noteMonitorLabel;
    }
    static public JFreeChart generatePitchRecorderMonitor(Recorder recorder){
        final JFreeChart wfChart = ChartFactory.createXYLineChart(
      /* title      = */ "Pitch",
      /* xAxisLabel = */ "Time (sec)",
      /* yAxisLabel = */ "Amplitude",
      /* dataset    = */ null
        );
        wfChart.removeLegend();
        final XYPlot wfPlot = wfChart.getXYPlot();
        wfPlot.getDomainAxis().setRange(-recorder.getFrameDuration(), 0.0);
        wfPlot.getRangeAxis().setRange(0, 1200.0);

    /* 時間軸 */
        final double[] times =
                IntStream.rangeClosed(- recorder.getFrameSize() + 1, 0)
                        .mapToDouble(i -> i / recorder.getSampleRate())
                        .toArray();
        final double [] stock= IntStream.rangeClosed(- recorder.getFrameSize() + 1, 0)
                .mapToDouble(i -> 0)
                .toArray();

        final ScheduledExecutorService executor =
                Le4MusicUtils.newSingleDaemonThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(
                () -> {
                    if (!recorder.isUpdated()) return;
        /* 最新フレームの波形を描画 */
                    int speed = 32;
                    for(int i = 0;i < stock.length - speed;i++){
                        stock[i] =stock[i + speed];
                    }
                    pitchValue = getPitch((recorder.latestFrame()));
                    isPlotPitch = PlotVolume.getVolumeValue(recorder.latestFrame()) > -120;
                    for(int i = 0;i < speed;i++)
                        stock[stock.length - speed + i] = pitchValue;

                    final XYDataset wfDataset = new SingleXYArrayDataset(times, stock);
                    wfPlot.setDataset(wfDataset);
                },
        /* initialDelay = */ 0L,
        /* delay = */ 1L,
                TimeUnit.MILLISECONDS
        );
        SwingUtilities.invokeLater(recorder::start);
        return wfChart;
    }
    static public void main(final String[] args)
        throws IOException ,
            UnsupportedAudioFileException ,
            javax.sound.sampled.LineUnavailableException{
        Mixer.Info[] mixerInfo= AudioSystem.getMixerInfo();
        Recorder recorder = Recorder.newRecorder(16000.0 ,0.4,mixerInfo[3],new File("out_w.wav"));
        Plot.createJFrame(generatePitchRecorderMonitor(recorder));
        recorder.start();
    }
}