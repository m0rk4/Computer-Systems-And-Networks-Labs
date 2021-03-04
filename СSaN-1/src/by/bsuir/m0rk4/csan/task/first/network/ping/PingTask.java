package by.bsuir.m0rk4.csan.task.first.network.ping;

import java.io.IOException;

public class PingTask implements Runnable {

    private final String ip;

    public PingTask(String ip) {
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            Process exec = Runtime.getRuntime().exec("ping " + ip);
            exec.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
