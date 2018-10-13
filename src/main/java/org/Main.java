package org;


import com.pty4j.PtyProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[]args) throws IOException, InterruptedException {
        // The command to run in a PTY...
        String[] cmd = { "/bin/sh", "-l" };
// The initial environment to pass to the PTY child process...
        String[] env = { "TERM=xterm" };
        Map<String, String> env1=new HashMap<>();

        env1.put("TERM", "xterm");

        PtyProcess pty = PtyProcess.exec(cmd, env1);

        OutputStream os = pty.getOutputStream();
        InputStream is = pty.getInputStream();

// ... work with the streams ...

// wait until the PTY child process terminates...
        int result = pty.waitFor();

// free up resources.
       // pty.close();
        pty.destroy();
    }
}