package com.ccmt.template.sh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

class StreamGobbler extends Thread {

    interface OnLineListener {
        void onLine(String line);
    }

    // private String shell = null;
    private BufferedReader reader = null;
    private List<String> writer = null;
    private OnLineListener listener = null;

    StreamGobbler(InputStream inputStream, List<String> outputList) {
        // this.shell = shell;
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = outputList;
    }

    StreamGobbler(InputStream inputStream, OnLineListener onLineListener) {
        // this.shell = shell;
        reader = new BufferedReader(new InputStreamReader(inputStream));
        listener = onLineListener;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (writer != null)
                    writer.add(line);
                if (listener != null)
                    listener.onLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
