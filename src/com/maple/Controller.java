package com.maple;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Controller {

    @FXML
    public TextArea textArea;

    @FXML
    public ChoiceBox<String> sourceLangCbox;

    @FXML
    public ChoiceBox<String> destLangCbox;

    private Screen screen = Screen.getPrimary();
    private Rectangle2D bounds = screen.getVisualBounds();
    private final double maxX = bounds.getMaxX();
    private final double maxY = bounds.getMaxY();

    private double mainWidth;
    private double mainHeight;
    private double mainScreenX;
    private double mainScreenY;
    private boolean isFirstShow = true;

    private Languages sourceLang;
    private Languages destLang;
    private String hostname;
    private int port;
    private String text = getClipboard();

    public void initialize() {
        getProperties();
        initComponent();
    }

    private void getProperties() {
        Properties properties = new Properties();
        if (!(new File("config.properties").exists())) {
            try (OutputStream fos = new FileOutputStream("config.properties")) {
                properties.setProperty("sourceLang", "English");
                properties.setProperty("destLang", "简体中文");
                properties.setProperty("hostname", "");
                properties.setProperty("port", "");
                properties.store(fos, "Create a config file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (InputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sourceLang = changeToLanguages(properties.getProperty("sourceLang"));
        destLang = changeToLanguages(properties.getProperty("destLang"));
        if (Pattern.matches("([1-9]+.){3}[1-9]+", properties.getProperty("hostname"))) {
            hostname = properties.getProperty("hostname");
        } else {
            hostname = "";
        }
        if (Pattern.matches("[0-9]+", properties.getProperty("port"))) {
            port = Integer.parseInt(properties.getProperty("port"));
        } else {
            port = -1;
        }
    }

    private void initComponent() {
        switch (sourceLang) {
        case ENGLISH:
            sourceLangCbox.getSelectionModel().select(0);
            break;
        case CHINESE_SIMPLE:
            sourceLangCbox.getSelectionModel().select(1);
            break;
        case AUTO:
            sourceLangCbox.getSelectionModel().select(2);
            break;
        case NOTSET:
            sourceLangCbox.getSelectionModel().select(3);
            break;
        default:
            sourceLangCbox.getSelectionModel().select(3);
        }

        switch (destLang) {
        case ENGLISH:
            destLangCbox.getSelectionModel().select(0);
            break;
        case CHINESE_SIMPLE:
            destLangCbox.getSelectionModel().select(1);
            break;
        case NOTSET:
            destLangCbox.getSelectionModel().select(2);
            break;
        default:
            destLangCbox.getSelectionModel().select(2);
        }

        sourceLangCbox.getSelectionModel().selectedItemProperty().addListener((selected, oldValue, newValue) -> {
            sourceLang = changeToLanguages(newValue);
            text = getClipboard();
            if (!isFirstShow) {
                translate();
            }
            setProperties("sourceLang", newValue);
        });

        destLangCbox.getSelectionModel().selectedItemProperty().addListener((selected, oldValue, newValue) -> {
            destLang = changeToLanguages(newValue);
            text = getClipboard();
            if (!isFirstShow) {
                translate();
            }
            setProperties("destLang", newValue);
        });
    }

    private void setProperties(String key, String value) {
        Properties properties = new Properties();
        try (InputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            try (OutputStream fos = new FileOutputStream("config.properties")) {
                properties.setProperty(key, value);
                properties.store(fos, "Change configuration");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Languages changeToLanguages(String value) {
        switch (value) {
        case "English":
            return Languages.ENGLISH;
        case "简体中文":
            return Languages.CHINESE_SIMPLE;
        case "自动检测":
            return Languages.AUTO;
        case "未设置":
            return Languages.NOTSET;
        default:
            return Languages.NOTSET;
        }
    }

    public void exit(ActionEvent event) {
        System.exit(0);
    }

    public void setUnvisible(ActionEvent event) {
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
                    mainScreenX = x - stage.getWidth() - 10;
                } else {
                    mainScreenX = x + 10;
                }
                stage.setX(mainScreenX);
                if ((y + 10 + stage.getHeight()) > maxY) {
                    mainScreenY = y - stage.getHeight() - 10;
                } else {
                    mainScreenY = y + 10;
                }
                stage.setY(mainScreenY);
                stage.show();
                mainWidth = stage.getWidth();
                mainHeight = stage.getHeight();
                isFirstShow = false;
                break;
            }
        }
    }

    public void showSettingPanel(ActionEvent event) throws IOException {
        Stage settingStage = new Stage();
        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root, 250, 150);
        settingStage.setScene(scene);
        Button saveButton = new Button("保存");
        saveButton.setPrefSize(50, 20);
        saveButton.setLayoutX(50);
        saveButton.setLayoutY(116);
        Button cancelButton = new Button("取消");
        cancelButton.setPrefSize(50, 20);
        cancelButton.setLayoutX(150);
        cancelButton.setLayoutY(116);
        Label IPLabel = new Label("IP地址：");
        IPLabel.setPrefSize(50, 20);
        IPLabel.setLayoutX(24);
        IPLabel.setLayoutY(35);
        Label portLabel = new Label("端口：");
        portLabel.setPrefSize(50, 20);
        portLabel.setLayoutX(35);
        portLabel.setLayoutY(68);
        TextField IPTextField = new TextField();
        IPTextField.setPrefSize(150, 20);
        IPTextField.setLayoutX(67);
        IPTextField.setLayoutY(35);
        TextField portTextField = new TextField();
        portTextField.setPrefSize(150, 20);
        portTextField.setLayoutX(67);
        portTextField.setLayoutY(68);
        IPTextField.setText(hostname);
        portTextField.setText(String.valueOf(port));
        cancelButton.setOnAction(e -> {
            settingStage.close();
        });
        saveButton.setOnAction(e -> {
            if (Pattern.matches("([1-9]+.){3}[1-9]+", IPTextField.getText())) {
                hostname = IPTextField.getText();
            } else {
                hostname = "";
            }
            if (Pattern.matches("[0-9]+", portTextField.getText())) {
                port = Integer.parseInt(portTextField.getText());
            } else {
                port = -1;
            }
            setProperties("hostname", hostname);
            setProperties("port", String.valueOf(port));
            settingStage.close();
        });
        root.getChildren().addAll(saveButton, cancelButton, IPLabel, portLabel, IPTextField, portTextField);
        settingStage.initStyle(StageStyle.TRANSPARENT);
        settingStage.setAlwaysOnTop(true);
        if (!isFirstShow) {
            settingStage.setX(mainScreenX + (mainWidth - 250) / 2);
            settingStage.setY(mainScreenY + (mainHeight - 150) / 2);
        }
        settingStage.show();
    }

    public String getClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        return (String) clipboard.getContent(DataFormat.PLAIN_TEXT);
    }

    public void speak() {
        if (isFirstShow) {
            return;
        }
        Speech speech = new Speech();
        speech.setText(text);
        speech.setLanguage(sourceLang);
        speech.setHostname(hostname);
        speech.setPort(port);
        File mp3File = new File("speech.mp3");
        speech.downloadAudio(mp3File);
        Media media = new Media(mp3File.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }

    private void translate() {
        Translator translator = new Translator();
        translator.setSourceLang(sourceLang);
        translator.setDestLang(destLang);
        translator.setSourceText(text);
        translator.setHostname(hostname);
        translator.setPort(port);
        String result = translator.execute();
        textArea.setWrapText(true);
        textArea.setText(result);
    }
}
