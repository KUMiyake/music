/**
 * Created by a-rusi on 2016/12/01.
 */

import jp.ac.kyoto_u.kuis.le4music.Le4MusicUtils;
import jp.ac.kyoto_u.kuis.le4music.Player;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.*;
import java.io.IOException;

public final class SoundGUI extends JFrame implements ActionListener{

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
    File currentFile;

    public static void main(final String[] args){
        SoundGUI soundGUI = new SoundGUI();


        soundGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        soundGUI.setBounds(10, 10, 300, 200);
        soundGUI.setExtendedState(JFrame.MAXIMIZED_BOTH);
        soundGUI.setTitle("タイトル");
        soundGUI.setVisible(true);
    }

    SoundGUI(){
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
    public void actionPerformed(ActionEvent event){
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

            try {
                player = Player.newPlayer(currentFile);
            }
            catch(IOException | UnsupportedAudioFileException | javax.sound.sampled.LineUnavailableException e){

            }
            player.start();
            consolePanel.removeAll();
            consolePanel.add(stopButton);
            consolePanel.add(playCountLabel);
//            consolePanel.add(spectrumPanel);
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
    void LoadSoundData(File wavFile){
		/* 音響信号読み込み */
		try {
            final AudioInputStream stream = AudioSystem
                    .getAudioInputStream(wavFile);
            final double[] waveform = Le4MusicUtils.readWaveformMonaural(stream);
            final AudioFormat format = stream.getFormat();
            final double sampleRate = format.getSampleRate();
            stream.close();

            JFreeChart waveformChart = PlotWaveform.GenerateWaveformChart(waveform, sampleRate);
            JFreeChart spectrogramChart = PlotSpectrogram.GenerateSpectrogramChart(
                    waveform, sampleRate, Le4MusicUtils.frameDuration, Le4MusicUtils.shiftDuration);
            JFreeChart volumeChart = PlotVolume.GenerateLoudnessChart(waveform, sampleRate);
            monitorPanel.removeAll();

            consolePanel = new JPanel();
            consolePanel.setLayout(null);
            consolePanel.add(playButton);
            playButton.setBounds(0,0,120,40);

            monitorPanel.add(consolePanel);
            monitorPanel.add(new ChartPanel(waveformChart));
            monitorPanel.add(new ChartPanel(spectrogramChart));
            monitorPanel.add(new ChartPanel(volumeChart));

            invalidate();
            validate();
        }
        catch(IOException | UnsupportedAudioFileException e){

        }
    }
}
