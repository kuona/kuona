package kuona.maven.analyser;

import org.apache.commons.cli.*;

import java.io.OutputStream;
import java.io.PrintWriter;

public class Main {

    public static final String OUTPUT_OPTION = "output";
    public static final String ARTIFACT_OPTION = "artifact";
    public static final String INCLUDE_OPTION = "include";

    public static void main(String[] args) {

        System.out.println("Kuona maven build analyser");

        final MavenPomAnalyser analyser = new MavenPomAnalyser(parseOptions(args));

        analyser.analyseDependencies();
    }

    static AnalyserRuntimeOptions parseOptions(String[] args) {
        final AnalyserRuntimeOptions result = new AnalyserRuntimeOptions();
        try {
            Options options = commandLineOptions();


            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            result.setArgs(cmd.getArgs());
            if (cmd.hasOption("i")) {
                result.setInclude(cmd.getOptionValue(INCLUDE_OPTION));
            }
            if (cmd.hasOption("a")) {
                result.setArtifactFilter(cmd.getOptionValue(ARTIFACT_OPTION));
            }

            if (cmd.hasOption("o")) {
                result.setOutputFilename(cmd.getOptionValue(OUTPUT_OPTION));
            }

            if (cmd.hasOption("h") || cmd.getArgList().size() == 0) {
                printHelp(System.out);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    static void printHelp(OutputStream output) {
        PrintWriter writer = new PrintWriter(output);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(writer, 80, "java -jar kuona-maven-analyser.jar [options] <paths>", "\nOptions", Main.commandLineOptions(), 3, 2, "\n See http://kuona.io");
        writer.close();
    }

    private static Options commandLineOptions() {
        Options options = new Options();

        options.addOption(new Option("i", INCLUDE_OPTION, true, "Pattern used to filter all the dependencies in the output"));
        options.addOption(new Option("a", ARTIFACT_OPTION, true, "Filter the output artifacts based on the supplied filter pattern."));
        options.addOption(new Option("o", OUTPUT_OPTION, true, "Output filename"));
        options.addOption(new Option("h", "help", false, "Output this message"));
        return options;
    }
}
