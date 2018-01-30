package com.maple;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Controller {
    
    @FXML
    public TextArea textArea;
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    final double maxX = bounds.getMaxX();
    final double maxY = bounds.getMaxY();
    
    private String text = getClipboard();
    
    public void exit(ActionEvent event) {
        System.exit(0);
    }
    
    public void setUnvisible(ActionEvent event)
    {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.hide();
        while (true) {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!text.equals(getClipboard())) {
                text = getClipboard();
                translate();
                Point point = MouseInfo.getPointerInfo().getLocation();
                double x = point.getX();
                double y = point.getY();
                if ((x + 10 + stage.getWidth()) > maxX) {
                    stage.setX(x - stage.getWidth() - 10);
                } else {
                    stage.setX(x + 10);
                }
                if ((y + 10 + stage.getHeight()) > maxY) {
                    stage.setY(y - stage.getHeight() - 10);
                } else {
                    stage.setY(y + 10);
                }
                stage.show();
                break;
            }
        }
    }
    
    public String getClipboard()
    {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        return (String)clipboard.getContent(DataFormat.PLAIN_TEXT);
    }
    
    public void speak()
    {
        Speech speech = new Speech(text, Languages.ENGLISH);
        File mp3File = new File("speech.mp3");
        speech.downloadAudio(mp3File);
        Media media = new Media(mp3File.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }
    
    private void translate()
    {
        Translator translator = new Translator();
        translator.setSourceLang(Languages.ENGLISH);
        translator.setDestLang(Languages.CHINESE_SIMPLE);
        translator.setSourceText(text);
        String result = translator.execute();
        textArea.setWrapText(true);
        textArea.setText(result);
    }
}
