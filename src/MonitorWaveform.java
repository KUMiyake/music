import java.io.File;
import java.util.stream.IntStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Plot;
import jp.ac.kyoto_u.kuis.le4music.SingleXYArrayDataset;
import jp.ac.kyoto_u.kuis.le4music.Player;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;
import org.apache.commons.cli.ParseException;

public final class MonitorWaveform {

    public static final void main(final String[] args)
            throws IOException,
            ParseException,
            LineUnavailableException,
            UnsupportedAudioFileException {
        final Options options = new Options();
        options.addOption("h", "help", false, "Display this help and exit");
        options.addOption("m", "mixer", true,
                "Index of the Mixer object that supplies a SourceDataLine object. " +
                        "To check the proper index, use CheckAudioSystem");
        options.addOption("u", "u1pdate-interval", true,
                "Interval of frame update [milliseconds]");
        options.addOption("b", "buffer", true, "duration of buffer [seconds]");
        options.addOption("f", "frame", true, "Duration of frame [seconds]");
        final String helpMessage = "PlayMonitorWaveform [OPTION]... WAVFILE";

        final CommandLine cmd = new DefaultParser().parse(options, args);
        if (cmd.hasOption("h")) {
            new HelpFormatter().printHelp(helpMessage, options);
            return;
        }
        final Mixer.Info mixerInfo = cmd.hasOption("m") ?
                AudioSystem.getMixerInfo()[Integer.parseInt(cmd.getOptionValue("m"))] :
                null;
        final double bufferDuration = cmd.hasOption("b") ?
                Double.parseDouble(cmd.getOptionValue("b")) : Player.Default.bufferDuration;
        final double frameDuration = cmd.hasOption("f") ?
                Double.parseDouble(cmd.getOptionValue("f")) : Le4MusicUtils.frameDuration;
        final String[] pargs = cmd.getArgs();
        if (pargs.length < 1) {
            System.out.println("WAVFILE is not given.");
            new HelpFormatter().printHelp(helpMessage, options);
            return;
        }
        final File wavFile = new File(pargs[0]);
        final Player player = Player.newPlayer(wavFile, bufferDuration, frameDuration, mixerInfo);

        final JFreeChart wfChart = ChartFactory.createXYLineChart(
      /* title      = */ "Waveform",
      /* xAxisLabel = */ "Time (sec)",
      /* yAxisLabel = */ "Amplitude",
      /* dataset    = */ null
        );
        wfChart.removeLegend();
        final XYPlot wfPlot = wfChart.getXYPlot();
        wfPlot.getDomainAxis().setRange(-player.getFrameDuration(), 0.0);
        wfPlot.getRangeAxis().setRange(-1.0, 1.0);

        final JFrame wfFrame = new JFrame("Waveform");
        wfFrame.add(Plot.createChartPanel(wfChart));
        wfFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        wfFrame.pack();
        wfFrame.setVisible(true);

    /* 時間軸 */
        final double[] times =
                IntStream.rangeClosed(- player.getFrameSize() + 1, 0)
                        .mapToDouble(i -> i / player.getSampleRate())
                        .toArray();

        final ScheduledExecutorService executor =
                Le4MusicUtils.newSingleDaemonThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(
                () -> {
                    if (!player.isUpdated()) return;
        /* 最新フレームの波形を描画 */
                    final double[] frame = player.latestFrame();
                    final XYDataset wfDataset = new SingleXYArrayDataset(times, frame);
                    wfPlot.setDataset(wfDataset);
                },
      /* initialDelay = */ 0L,
      /* delay = */ 1L,
                TimeUnit.MILLISECONDS
        );
    }
}