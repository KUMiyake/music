import jp.ac.kyoto_u.kuis.le4music.Player;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SoundPlayer {

    static public void main(final String[] args) throws IOException, UnsupportedAudioFileException,javax.sound.sampled.LineUnavailableException

    {
        if (args.length == 0) {
            System.out.println("no input files");
            return;
        }
        File file = new File(args[0]);
        Player player = Player.newPlayer(file);
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(
                () -> {
                    final double[] frame = player.latestFrame();
                    final double rms = Math.sqrt(Arrays.stream(frame).map(x -> x * x).average().orElse(0.0));
                    final double logRms = 20.0 * Math.log10(rms);
                    System.out.printf("RMS %f dB%n", logRms);
                },
                0L, 100L, TimeUnit.MILLISECONDS
        );
        player.start();
    }
}
