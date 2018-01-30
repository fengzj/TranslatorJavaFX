package com.maple;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Speech {
    // Text properties
    private String text;
    private Languages language;

    // Audio properties
    private byte[] bytes;
    private boolean isProcessed;

    public Speech() {
        this.isProcessed = false;
    }

    public Speech(String text, Languages language) {
        this.text = text;
        this.language = language;
        this.isProcessed = false;
    }

    public byte[] getAudioByteArray() {
        // Process if the request is not already processed
        if (!isProcessed)
            fetchAudio();

        return bytes;
    }

    public void downloadAudio(File mp3File) {
        // Process if the request is not already processed
        if (!isProcessed)
            fetchAudio();

        // Save the MP3 file
        if (bytes != null) {
            try {
                FileOutputStream output = new FileOutputStream(mp3File);
                output.write(bytes);
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchAudio() {
        int length;
        byte[] buffer = new byte[4096];
        String encodedText = null;

        // URL enconding the text
        try {
            encodedText = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String tk = GoogleTK.TL(text);
        
        // Creating the URL
//        String urlString = "https://translate.google.com/translate_tts?ie=UTF-8&q=" + encodedText + "&tl="
//                + language.getValue() + "&total=1&idx=0&client=t";
        String urlString = "https://translate.google.cn/translate_tts?ie=UTF-8"
                + "&q=" + encodedText
                + "&tl=" + language.getValue()
                + "&total=1&idx=0"
                + "&textlen=" + text.length()
                + "&tk=" + tk
                + "&client=t&prev=input";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Fake the User-Agent
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Check the HTTP response code
            if (conn.getResponseCode() == 200) {
                InputStream in = conn.getInputStream();

                // Reading the bytes
                while ((length = in.read(buffer)) != -1) {
                    ByteArrayOutputStream concat = new ByteArrayOutputStream();
                    if (bytes != null)
                        concat.write(bytes, 0, bytes.length);
                    concat.write(buffer, 0, length);

                    bytes = concat.toByteArray();
                }

                in.close();
            }

            // Mark the request as Processed
            isProcessed = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Languages getLanguage() {
        return language;
    }

    public void setLanguage(Languages language) {
        this.language = language;
        this.isProcessed = false;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.isProcessed = false;
    }
}