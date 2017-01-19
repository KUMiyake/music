import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Player;

import javax.swing.*;
import java.awt.Font;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlotScore {
    static int position = 0;
    private static class ScorePack{
        public int startTime;
        public String showWords;
        public void init(int time, String words){
            startTime = time;
            showWords = words;
        }
        public ScorePack(){
            startTime = -1;
        }
    }
    static public JLabel generatePositionLabel(){
        JLabel postion_label = new JLabel();
        final ScheduledExecutorService executor =
                Le4MusicUtils.newSingleDaemonThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(
                () -> {
                    postion_label.setText(Integer.toString(position));
                },
        /* initialDelay = */ 0L,
        /* delay = */ 1L,
                TimeUnit.MILLISECONDS
        );
        return postion_label;
    }
    static public JLabel generateLyricsLabel(Player player){
        ScorePack[] pack = new ScorePack[32];
        for(int i = 0;i < pack.length;i++){
            pack[i] = new ScorePack();
        }
        pack[0].init(0,"あああああ");
        pack[1].init(20000,"いいいいい");
        pack[1].init(40000,"う");
        pack[1].init(80000,"おおおお");
        pack[1].init(160000,"かかか");
        JLabel lyrics_label = new JLabel();
        final ScheduledExecutorService executor =
                Le4MusicUtils.newSingleDaemonThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(
                () -> {
                    if (!player.isUpdated()) return;
                    int target_pack = 0;
                    for(int i = 0;i < pack.length;i++){
                        if(player.position() > pack[i].startTime)
                            if(pack[target_pack].startTime < pack[i].startTime)
                                target_pack = i;
                    }
                    position = player.position();
                    lyrics_label.setText(pack[target_pack].showWords);
                },
        /* initialDelay = */ 0L,
        /* delay = */ 1L,
                TimeUnit.MILLISECONDS
        );
        lyrics_label.setFont(new Font("游明朝", Font.BOLD ,24));
        return lyrics_label;
    }
    static public void main(){

    }
}
