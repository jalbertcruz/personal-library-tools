package org;


import com.pty4j.PtyProcess;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    static Observable<String> risky() {
        return Observable.fromCallable(() -> {
            if (Math.random() < 0.1) {
                Thread.sleep((long) (Math.random() * 2000));
                return "OK";
            } else {
                throw new RuntimeException("Transient");
            }
        });
    }

    // Asynchronously check whether the output of the process is captured
    // properly...
    static Observable<Byte> sha(PtyProcess pty, List<Byte> l, Runnable b) {
        return Observable.create(
                subscriber -> {
                    var tout = Observable.create(
                            subs -> {
                               // subs.onNext((byte) 3);
                            })
                            .timeout(7, SECONDS);

                    Disposable disposable = tout.subscribe(e -> {
                    }, e -> {
                        subscriber.onComplete();
                        b.run();
                    }, () -> {
                    });
                    //tout.subscribe();

                    InputStream is1 = pty.getInputStream();
                    try {
                        int ch;
                        while (pty.isRunning() && (ch = is1.read()) >= 0) {

//                           System.out.println((char) ch);
//                            System.out.flush();
                            l.add((byte) ch);

                            if (ch == 36) {

                                disposable = tout.subscribe(
                                        e -> {
                                }, e -> {

                                    subscriber.onComplete();
                                    b.run();

                                }, () -> {
                                });

                            } else {
                                if (ch != 32 // despues del '$' sh pone un ' '
                                        && disposable != null) {
                                    disposable.dispose();
                                    disposable = null;
                                }


                            }

                            subscriber.onNext((byte) ch);


                        }

                        subscriber.onComplete();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
    }


    public static void main(String[] args) throws IOException, InterruptedException {

//        System.out.println(" ".getBytes()[0]);

//        risky()
//                .timeout(1, SECONDS)
//                .doOnError(th -> log.warn("Will retry", th))
//                .retry()
//                .subscribe(log::info);

        // The command to run in a PTY...
        String[] cmd = {"/bin/sh", "-i"};
        Map<String, String> env1 = new HashMap<>();

        env1.put("TERM", "xterm");

        PtyProcess pty = PtyProcess.exec(cmd, env1);

        // OutputStream os = pty.getOutputStream();
        // InputStream is = pty.getInputStream();

        // static void main(String[] args) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);


        // Asynchronously wait for a little while, then close the Pty, which should
        // force our child process to be terminated...
        Thread t2 = new Thread() {
            public void run() {
                OutputStream os = pty.getOutputStream();
                try {
//                    os.write("ls -a\n".getBytes());
                    os.write(("" +
                            "md5sum /home/a/Desktop/temp/politics/a.sh | awk '{print $1}'\n").getBytes());
                    //os.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t2.start();


        var ch = new ArrayList<Byte>();

        sha(pty, ch, () -> {
            byte[] r = new byte[ch.size()];
            for (int i = 0; i < ch.size(); i++) {
                r[i] = ch.get(i);
            }

            System.out.println("*************");
            System.out.println(new String(r));
        })
//                .buffer(200)

//                .skip(2)
//                .doOnError((Throwable th) -> {
//
//                })
                .subscribe(e -> {

                }, e -> {

                }, () -> {

                });

//                .subscribe((List<Byte> ch) -> {
//                    byte[] r = new byte[ch.size()];
//                    for (int i = 0; i < ch.size(); i++) {
//                        r[i] = ch.get(i);
//                    }
//
//                    System.out.println("*************");
//                    System.out.println(new String(r));
//                });

        latch.await(10, SECONDS);
        // We should've waited long enough to have read some of the input...

        int result = pty.waitFor();

        t2.join();

        System.out.println(-1 == result);
    }
}