package org;


import com.pty4j.PtyProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        // The command to run in a PTY...
        String[] cmd = {"/bin/sh", "-i"};
// The initial environment to pass to the PTY child process...
        String[] env = {"TERM=xterm"};
        Map<String, String> env1 = new HashMap<>();

        env1.put("TERM", "xterm");

        PtyProcess pty = PtyProcess.exec(cmd, env1);

        // OutputStream os = pty.getOutputStream();
        // InputStream is = pty.getInputStream();

        // static void main(String[] args) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        // Asynchronously check whether the output of the process is captured
        // properly...
        Thread t1 = new Thread(() -> {
            InputStream is1 = pty.getInputStream();

            try {
                int ch;
                while (pty.isRunning() && (ch = is1.read()) >= 0) {
                    System.out.write(ch);
                    System.out.flush();
                }

                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t1.start();


        // Asynchronously wait for a little while, then close the Pty, which should
        // force our child process to be terminated...
        Thread t2 = new Thread() {
            public void run() {
                OutputStream os = pty.getOutputStream();
                try {
                    os.write("ls -a\n".getBytes());
                    os.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t2.start();

        latch.await(1, TimeUnit.SECONDS);
        // We should've waited long enough to have read some of the input...

        int result = pty.waitFor();

        t1.join();
        t2.join();

        System.out.println(-1 == result);
    }
}