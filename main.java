import java.io.File;
import java.util.stream.IntStream;

import javax.sound.sampled.AudioSystem;

import javax.sound.sampled.AudioFormat;

import javax.sound.sampled.AudioInputStream;

import javax.swing.JFrame;

import org.jfree.chart.JFreeChart;

import org.jfree.chart.ChartPanel;

import org.jfree.chart.plot.XYPlot;

import org.jfree.chart.axis.NumberAxis;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;

import jp.ac.kyoto_u.kuis.le4music.Plot;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

public final class PlotWaveform {

	/* 信号をプロットする */

	public static final void plotWaveform(

final double [] waveform,

final double sampleRate

) {

/*各サンプルの時刻を求める*/

final double [] times = 
IntStream.range(0, waveform.length).mapToDouble(i -> i / sampleRate).toArray();


/*プロットする*/

final JFrame frame = Plot.plot(times, waveform);

final JFreeChart chart = ((ChartPanel)frame.getContentPane().getComponent(0)).getChart();

chart.setTitle("Waveform");

final XYPlot plot = chart.getXYPlot();

final NumberAxis xAxis = (NumberAxis)plot.getDomainAxis();

xAxis.setRange(0.0, (waveform.length - 1) / sampleRate);

xAxis.setLabel("Time[sec.]");

final NumberAxis yAxis = (NumberAxis)plot.getRangeAxis();

yAxis.setRange(-1.0, 1.0);

yAxis.setLabel("Amplitude");
}

	public static final void main(final String[] args)

	throws IOException, UnsupportedAudioFileException {

		if (args.length == 0) {

			System.out.println("no input files");

			return;

		}

		final File wavFile = new File(args[0]);

		/* 音響信号読み込み */

		final AudioInputStream stream = AudioSystem
				.getAudioInputStream(wavFile);

		final double[] waveform = Le4MusicUtils.readWaveformMonaural(stream);

		final AudioFormat format = stream.getFormat();

		final double sampleRate = format.getSampleRate();

		stream.close();

		plotWaveform(waveform, sampleRate);

	}
}