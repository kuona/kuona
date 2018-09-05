package kuona.maven.analyser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Maven {
  private final String path;

  Maven(String path) {
    this.path = path;
  }

  void run(String command, PrintStream out) {

    try {
      Process p = Runtime.getRuntime().exec(command, null, new File(path));
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        out.println(line);
      }
      if (p.isAlive()) {
        p.waitFor();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
