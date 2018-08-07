package evaluationbasics;

import java.io.*;
import java.util.concurrent.TimeoutException;

/**
 * Created by ilias on 06.10.16.
 */
public class TestProcesses {
    public static void main(String... args) {
        try {
            System.out.println(ProcessClass.exec("myTestStringToPass"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}


class ProcessClass {

    private static final int TIMEOUT = 20000;

    public static String exec(String bla) throws IOException {
        int GRANULARITY = 50;
        String JAVA_HOME = System.getProperty("java.home");
        String SEPARATOR = System.getProperty("file.separator");
        String CLASSPATH = System.getProperty("java.class.path");

        ProcessBuilder builder = new ProcessBuilder(
                JAVA_HOME + SEPARATOR + "bin" + SEPARATOR + "java",
                "-cp", CLASSPATH,
                "evaluationbasics.ProcessClass");
        Process child = builder.start();
//        try {
//            Thread.sleep(10000);
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//        child.destroy();
        try {
            OutputStream output = child.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(output);
            oos.writeObject(bla);
            oos.flush();



            InputStream input = child.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(input);
            int total = 0;
            while (total < TIMEOUT && input.available() == 0) {
                try {
                    Thread.sleep(GRANULARITY);
                } catch (InterruptedException e) {
                }
                total = total + GRANULARITY;
            }
            if (input.available() != 0) {
                return (String) ois.readObject();
            } else {
                throw new TimeoutException("timed out after " + TIMEOUT + "ms");
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            child.destroy();
//            return "done";
        }
    }

    public static void main(String... args) {
        ProcessBuilder builder = new ProcessBuilder("eog");
        try {
            Process p = builder.start();
            ObjectInputStream ois = new ObjectInputStream(System.in);
            ObjectOutputStream oos = new ObjectOutputStream(System.out);

//            while(true) {
                try {
                    FileWriter fw = new FileWriter(new File("/tmp/mylog.txt"));
                    String request = (String) ois.readObject();
                    fw.write("Read element: " + request + "\n");
                    fw.flush();

                    String response = "MyResponse";
                    oos.writeObject(response);

                    fw.write("write answer\n");

                    fw.close();
                } catch (Exception e) {
//                    break;
                }
//            }
        } catch (IOException e) {

//        } catch (ClassNotFoundException e) {

        } finally {
        }

    }
}
