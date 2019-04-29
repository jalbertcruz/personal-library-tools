package org;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

public class Main {

//    public static final String BOOKS_NAMES = "/home/a/src/a/dev-speech-analytics/devOps/books_names.py";
    public static final String BOOKS_NAMES = "./books_names.py";
    public static final String BOOKS_LOCATIONS = "./books_locations.py";
//    public static final String BOOKS_LOCATIONS = "/home/a/src/a/dev-speech-analytics/devOps/books_locations.py";

    private static Tuple2<String, String> obtenerFirma(File file) {

        Tuple2<String, String> res = new Tuple2<>();
        try {
//            MessageDigest messageDigest = MessageDigest.getInstance("MD5"); // Inicializa MD5
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512"); // Inicializa MD5
            MessageDigest messageDigest2 = MessageDigest.getInstance("SHA"); // Inicializa SHA-1

            try {
                try (InputStream archivo = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int fin_archivo = -1;
                    int caracter;
                    //                    messageDigest2.update(buffer, 0, caracter);
                    caracter = archivo.read(buffer);

                    while (caracter != fin_archivo) {
                        //                    c+= 1;
                        //                    System.out.println("Analisis del Mb número: " + c + " del fichero " + file.getName());
                        messageDigest.update(buffer); // Pasa texto claro a la función resumen
                        messageDigest2.update(buffer);
                        //                    messageDigest.update(buffer, 0, caracter); // Pasa texto claro a la función resumen
                        //                    messageDigest2.update(buffer, 0, caracter);
                        caracter = archivo.read(buffer);
                    }
                }
                byte[] resumen = messageDigest.digest(); // Genera el resumen MD5
                byte[] resumen2 = messageDigest2.digest(); // Genera el resumen SHA-1

                //Pasar los resumenes a hexadecimal

                String s = "";
                for (int i = 0; i < resumen.length; i++) {
                    s += Integer.toHexString((resumen[i] >> 4) & 0xf);
                    s += Integer.toHexString(resumen[i] & 0xf);
                }

                //System.out.println("Resumen MD5: " + s);
                res.setA(s);

                String m = "";
                for (int i = 0; i < resumen2.length; i++) {
                    m += Integer.toHexString((resumen2[i] >> 4) & 0xf);
                    m += Integer.toHexString(resumen2[i] & 0xf);
                }

                res.setB(m);
                //System.out.println("Resumen SHA-1: " + m);

            } catch (java.io.FileNotFoundException fnfe) {
            } catch (java.io.IOException ioe) {
            }

        } catch (java.security.NoSuchAlgorithmException nsae) {
        }

        return res;
    }


    public static void main(String[] args) throws Exception, InterruptedException {
        var ends = new HashSet<>();
        var dirs = new ArrayList<>();
        ends.add(".epub");
        ends.add(".pdf");
        ends.add(".mobi");
        ends.add(".azw3");
        ends.add(".djvu");
        ends.add(".ps");
        ends.add(".chm");
        ends.add(".doc");
        ends.add(".rar");
        ends.add(".rtf");

        var get_sign = false;
//        var get_sign = true;



        var f = Files.walk(Path.of(
//                 "/media/a/data/docs/Events/MATECOMPU 2009/fscommand/"
                 "/media/a/data/docs/"
//                "/home/a/Downloads/aa/aa/"
        ))
                .filter(
                        (Path e) -> (e.toFile().isFile()
                                && ends.stream().anyMatch(
                                (Object p) ->
                                        e.getFileName()
                                                .toString().toLowerCase()
                                                .endsWith((String) p)))
                );

                /*.map(e -> {
                    //System.out.println(e.getFileName().toString());
                    return e;
                });*/


        Stream<String> ts = f.map(e -> {

            var firma = get_sign ? obtenerFirma(e.toFile()).a : "";

            var d = e.getParent().toAbsolutePath().toString();
            if (!dirs.contains(d)) {
                dirs.add(d);
                //     System.out.println(d);
            }

            return String.format("%s|%s|%s|%d\n", e.toFile().getName().toLowerCase(), e.toFile().getName(), firma, dirs.indexOf(d));
        });

        OutputStreamWriter os = new FileWriter(BOOKS_NAMES);

        os.write("names = \"\"\"\n");
        ts.forEach(e -> {
            try {
                os.write(e);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            // System.out.println(e);
        });
        os.write("\"\"\"\n");
        os.close();

        OutputStreamWriter pydirs = new FileWriter(BOOKS_LOCATIONS);

        pydirs.write("dirs = {\n");

        for (int i = 0; i < dirs.size(); i++) {
            pydirs.write(" " + i + ": " + "\"" + dirs.get(i) + "/\",\n");
        }

        pydirs.write("}\n");
        pydirs.close();

    }
}