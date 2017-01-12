/**
 * Created by a-rusi on 2017/01/05.
 */
import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Player;
import jp.ac.kyoto_u.kuis.le4music.Recorder;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.*;
import java.io.IOException;

public final class Karaoke extends JFrame implements ActionListener{

    JLabel fileNameLabel;
    JPanel globalPanel;
    JPanel consolePanel;
    JPanel spectrumPanel;
    JPanel monitorPanel;
    JButton fileButton;
    JButton playButton;
    JButton stopButton;
    JLabel playCountLabel;
    Player player;
    Recorder recorder;
    File currentFile;

    public static void main(final String[] args){
        Karaoke  karaoke = new  Karaoke();


        karaoke.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        karaoke.setBounds(10, 10, 300, 200);
        karaoke.setExtendedState(JFrame.MAXIMIZED_BOTH);
        karaoke.setTitle("タイトル");
        karaoke.setVisible(true);
    }

    Karaoke(){
        fileButton = new JButton("file select");
        playButton = new JButton("Play");
        stopButton = new JButton("Stop");
        playCountLabel = new JLabel();
        fileButton.addActionListener(this);
        playButton.addActionListener(this);
        stopButton.addActionListener(this);

        globalPanel = new JPanel();
        monitorPanel = new JPanel();
        spectrumPanel = new JPanel();
        globalPanel.setLayout(new BoxLayout(globalPanel, BoxLayout.PAGE_AXIS));
        monitorPanel.setLayout(new GridLayout(2,2));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(fileButton);

        fileNameLabel = new JLabel();
        fileNameLabel.setText("ファイルを選択してください");

        JPanel fileNamePanel = new JPanel();
        fileNamePanel.add(fileNameLabel);

        JPanel filePanel = new JPanel();
        filePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        filePanel.add(buttonPanel);
        filePanel.add(fileNamePanel);

        globalPanel.add(filePanel);
        globalPanel.add(monitorPanel);
        getContentPane().add(globalPanel, BorderLayout.CENTER);
    }
    public void actionPerformed(ActionEvent event)
            {
        if(event.getSource() == fileButton) {
            JFileChooser fileChooser = new JFileChooser();

            int selected = fileChooser.showOpenDialog(this);
            if (selected == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
                fileNameLabel.setText(currentFile.getName());

                monitorPanel.removeAll();
                JLabel loading_label = new JLabel();
                loading_label.setText("読み込み中");
                monitorPanel.add(loading_label);
                monitorPanel.add(loading_label);
                monitorPanel.add(loading_label);
                monitorPanel.add(loading_label);

                try {
                    player = Player.newPlayer(currentFile);
                    Mixer.Info[] mixerInfo=AudioSystem.getMixerInfo();
                    recorder = Recorder.newRecorder(16000.0 ,0.4,mixerInfo[3],new File("out_w.wav"));
                }
                catch(IOException | UnsupportedAudioFileException | javax.sound.sampled.LineUnavailableException e){

                }
                LoadSoundData(currentFile);

            } else if (selected == JFileChooser.CANCEL_OPTION) {
                fileNameLabel.setText("キャンセルされました");
            } else if (selected == JFileChooser.ERROR_OPTION) {
                fileNameLabel.setText("エラーまたは取り消しがありました");
            }
        }
        else if(event.getSource() == playButton){
//            if(!player.isRunning())
//            player.stop();

            consolePanel.removeAll();
            consolePanel.add(stopButton);
            consolePanel.add(playCountLabel);
//            consolePanel.add(spectrumPanel);
//            今後の拡張で実装予定の動的表示部分
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        playCountLabel.setText(String.valueOf(player.position()));
                        try {
                            JFreeChart spectrumChart = PlotSpectrum.GenerateSpectrumChart(player.latestFrame(),player.getSampleRate());
                            spectrumPanel.removeAll();
                            spectrumPanel.add(new ChartPanel(spectrumChart));
                            Thread.sleep(40);
                        }
                        catch(InterruptedException e){

                        }
                    }
                }
            });
//            thread.start();
        }
        else if(event.getSource() == stopButton){
            player.stop();
        }
    }
    void LoadSoundData(File wavFile)
    {
		/* 音響信号読み込み */
        try {
            final AudioInputStream stream = AudioSystem
                    .getAudioInputStream(wavFile);
            final double[] waveform = Le4MusicUtils.readWaveformMonaural(stream);
            final AudioFormat format = stream.getFormat();
            final double sampleRate = format.getSampleRate();
            stream.close();

            JFreeChart waveformChart =
                    MonitorRecorder.GenerateSpectrogramMonitorChart(recorder);
//                    PlotWaveform.GenerateWaveformChart(waveform, sampleRate);
            JFreeChart spectrogramChart =
                    MonitorSpectrogram.GenerateSpectrogramMonitorChart(player);

//                    PlotSpectrogram.GenerateSpectrogramChart(
//                    waveform, sampleRate, Le4MusicUtils.frameDuration, Le4MusicUtils.shiftDuration);
//
            JFreeChart volumeChart = PlotVolume.GenerateLoudnessChart(waveform, sampleRate);
            monitorPanel.removeAll();

            consolePanel = new JPanel();
            consolePanel.setLayout(null);
            consolePanel.add(playButton);
            playButton.setBounds(0,0,120,40);

            monitorPanel.add(consolePanel);
            monitorPanel.add(new ChartPanel(waveformChart));
            monitorPanel.add(new ChartPanel(volumeChart));
            monitorPanel.add(new ChartPanel(spectrogramChart));


            player.start();

            invalidate();
            validate();
        }
        catch(IOException | UnsupportedAudioFileException e){

        }
    }
}
