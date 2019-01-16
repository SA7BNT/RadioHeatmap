import java.awt.*;
import java.io.*;
import java.util.Scanner;

public class Main {

    /**
     * (c) Jakob Maier (2019)
     */

    private static boolean DEBUGGING_MODE = false;

    private static int getFreqStep(String[][] dataArray){
        int rec = 1;
        for (int y = 0; y < dataArray.length - 1; y++){
            if (dataArray[y][1].equals(dataArray[y+1][1])){
                rec++;
            } else {
                break;
            }
        }
        if (DEBUGGING_MODE){
            System.out.println("Frequency Step: " + rec);
        }
        return rec;
    }

    private static void printArray(String[][] array){
        for (int i = 0; i < array.length; i++){
            for (int j = 0; j < array[0].length; j++){
                System.out.print(array[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static void printArray(double[][] array){
        for (int i = 0; i < array.length; i++){
            for (int j = 0; j < array[0].length; j++){
                System.out.print(array[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static void copyArray(String[][] raw, double[][] helper) {
        for (int i = 0; i < raw.length; i++){
            for (int j = 6; j < raw[0].length; j++){
                if (raw[i][j] == null){
                    raw[i][j] = "0.0";
                }
                helper[i][j-6] = Double.parseDouble(raw[i][j]);
            }
        }
    }

    private static void parseArray(String[][] raw, double[][] data, boolean print){
        int freqStep = getFreqStep(raw);

        double[][] helper = new double[raw.length][raw[0].length - 6];

        copyArray(raw, helper);

        int index = 0;
        for (int y = 0; y < helper.length; y++){
            for (int x  = 0; x < helper[0].length; x++){
                if (index == data[0].length)
                    index = 0;
                data[y / freqStep][index] = helper[y][x];
                index++;
            }
        }

        if (print || DEBUGGING_MODE)
            printArray(data);

        normalizeValues(getMax(data), getMin(data), data);
    }

    private static void normalizeValues(double max, double min, double[][] data){
        for (int y = 0; y < data.length; y++){
            for (int x = 0; x < data[0].length; x++){
                data[y][x] = (data[y][x] - min) / (max - min);
            }
        }
    }

    private static void drawScale(String[][] raw, double[][] data, int height, int border) {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.002);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 12));

        StdDraw.line(0, height-border, data[0].length, height-border);

        double mhzStep = 1e6 / Double.parseDouble(raw[0][4]);
        int mhz = 0;
        int step = 1;

        if (mhzStep < 50){
            step = 100;
            mhzStep *= 100;
        }

        for (int x = 0; x < data[0].length; x += mhzStep){
            StdDraw.line(x, height-border, x, height-6);
            StdDraw.textLeft(x + 4, height-border/2, Double.parseDouble(raw[0][2]) / 1e6 + (mhz += step) - step + " MHz");
        }
    }

    private static void drawHeatmap(double[][] data, String[][] raw, boolean scale, boolean label, float saturation, float brightness){
        int height = data.length;
        int width = data[0].length;
        int border = 20;

        if (!scale){
            border = 0;
        }

        height += border;

        StdDraw.setCanvasSize(width, height);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);

        StdDraw.enableDoubleBuffering();

        long startTime;
        long endTime;

        for (int y = 0; y < data.length; y++){
            startTime = System.nanoTime();
            for (int x = 0; x < data[0].length; x++){
                float hue = (float) data[y][x];
                StdDraw.setPenColor(Color.getHSBColor(hue, saturation, brightness));
                StdDraw.point(x, y);
            }
            endTime = System.nanoTime();
            if (y == 0){
                double duration = (endTime - startTime) * data.length / 1e9;
                System.out.println("Estimated time to completion: " + (int) duration + " seconds.");
            }
        }

        if (label){
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setFont(new Font("Arial", Font.BOLD, 10));
            StdDraw.textLeft(15, 40, " "+raw[0][0]);
            StdDraw.textLeft(15, 25,  raw[0][1] + " to" + raw[raw.length - 1][1]);
            StdDraw.textLeft(15, 10, raw[0][2] + " -" + raw[raw.length - 1][3] + " Hz");
        }

        if (scale)
            drawScale(raw, data, height, border);

        StdDraw.show();
    }

    private static void printHelp(){
        System.out.println("Usage: java -jar RadioHeatmap.jar -f file -i image -t filetype [OPTIONS]");
        System.out.println();
        System.out.println("    -f      path to csv source file [-f example.csv]");
        System.out.println("    -i      name of target image file [-i example]");
        System.out.println("    -t      image file type [-t png/jpeg]");
        System.out.println("    -p      Print raw data");
        System.out.println("    -h      help");
        System.out.println("    -s      draw scale on heatmap");
        System.out.println("    -l      draw label on heatmap");
        System.out.println("    -sa     saturation [0.0 - 1.0]");
        System.out.println("    -br     brightness [0.0 - 1.0]");
        System.out.println("    -deb    debugging mode");
        System.out.println();
        System.out.println("Example: java -jar RadioHeatmap.jar -f survey.csv -i survey -t png");
    }

    private static void readFile(String path, String[][] raw) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        Scanner dataScanner = null;

        String data = "";

        int x = 0;
        while (scanner.hasNextLine()){
            dataScanner = new Scanner(scanner.nextLine());
            dataScanner.useDelimiter(",");
            int y = 0;
            while (dataScanner.hasNext()){
                data = dataScanner.next();
                raw[x][y] = data;
                y++;
            }
            x++;
        }
        scanner.close();
    }

    private static double getMax(double[][] array){
        double max = Double.MIN_VALUE;
        for (int i = 0; i < array.length; i++){
            for (int j = 0; j < array[0].length; j++){
                if (array[i][j] > max){
                    max = array[i][j];
                }
            }
        }
        return max;
    }

    private static double getMin(double[][] array){
        double min = Double.MAX_VALUE;
        for (int i = 0; i < array.length; i++){
            for (int j = 0; j < array[0].length; j++){
                if (array[i][j] < min){
                    min = array[i][j];
                }
            }
        }
        return min;
    }

    private static String getLinesAndColums(String path) throws FileNotFoundException{
        int lines = 0;
        int colums = 0;

        Scanner lineScanner = new Scanner(new File(path));
        Scanner columnScanner = null;

        while (lineScanner.hasNextLine()){
            columnScanner = new Scanner(lineScanner.nextLine());
            columnScanner.useDelimiter(",");
            colums = 0;
            while (columnScanner.hasNext()){
                columnScanner.next();
                colums++;
            }
            lines++;
        }

        columnScanner.close();
        lineScanner.close();
        if (DEBUGGING_MODE){
            System.out.println("Lines: " + lines);
            System.out.println("Columns: " + colums);
        }
        return colums + "-" + lines;
    }

    private static String[] cli(String[] args){
        String[] arguments = new String[8];

        arguments[2] = "png";
        arguments[6] = "1.0";
        arguments[7] = "0.8";

        if (args.length == 0){
            System.out.println("Not enough arguments");
            printHelp();
            System.exit(1);
        }
        for (int i = 0; i < args.length; i++){
            switch (args[i]){
                case  "-h":
                case "--help":
                    printHelp();
                    System.exit(1);
                    break;
                case "-v":
                case "--version":
                    System.out.println("Version 1.0 (14.01.2019)");
                    System.exit(1);
                    break;
                case "-f":
                    arguments[0] = args[i+1];
                    break;
                case "-i":
                    if (args[i].contains(".")){
                        arguments[1] = args[i].split(".")[0];
                    }
                    arguments[1] = args[i+1];
                    break;
                case "-t":
                    if (!(args[i].equals("png") || !(args[i].equals("jpeg"))))
                        arguments[2] = "png";
                    arguments[2] = args[i+1];
                    break;
                case "-s":
                    arguments[3] = "true";
                    break;
                case "-l":
                    arguments[4] = "true";
                    break;
                case "-p":
                    arguments[5] = "true";
                    break;
                case "-sa":
                    arguments[6] = args[i + 1];
                    break;
                case "-br":
                    arguments[7] = args[i + 1];
                    break;
                case "-deb":
                    DEBUGGING_MODE = true;
                    break;
            }
        }
        return arguments;
    }

    public static void main(String[] args) throws FileNotFoundException {
        //For testing purposes
        //String[] testArgs = new String[]{"-f", "examples/survey_1.csv", "-i", "examples/image_1", "-t", "png", "-s", "-l"};

        boolean scale, label, print;
        String[] arguments = cli(args);
        String PATH = arguments[0];
        String filename = arguments[1];
        String fileExtension = "." + arguments[2];
        scale = Boolean.parseBoolean(arguments[3]);
        label = Boolean.parseBoolean(arguments[4]);
        print = Boolean.parseBoolean(arguments[5]);
        float saturation = Float.parseFloat(arguments[6]);
        float brightness = Float.parseFloat(arguments[7]);

        System.out.println("Free Software by Jakob Maier (2019)");
        System.out.println("https://github.com/gue-ni/heatmap.git");
        System.out.println();

        String[] linesAndColumns = getLinesAndColums(PATH).split("-");
        int lines = Integer.parseInt(linesAndColumns[1]);
        int columns = Integer.parseInt(linesAndColumns[0]);
        String[][] raw = new String[lines][columns];

        System.out.println("Reading file... (" + lines + " Lines)");
        System.out.println();
        readFile(PATH, raw);

        int freqStep = getFreqStep(raw);
        double[][] data = new double[lines / freqStep][(columns - 6) * freqStep];

        System.out.println(raw[0][0] + " from" + raw[0][1] + " to" + raw[raw.length - 1][1]);
        System.out.println("Frequency Range: " + Double.parseDouble(raw[0][2]) / 1e6 + " -" + Double.parseDouble(raw[raw.length - 1][3]) / 1e6 + " MHz");
        System.out.println("Step size: " + raw[0][4] + " Hz");
        System.out.println("Image dimensions: " + data[0].length + " x " + data.length);
        System.out.println();
        System.out.println("Parsing Data...");
        parseArray(raw, data, print);

        System.out.println("Drawing Heatmap...");
        drawHeatmap(data, raw, scale, label, saturation, brightness);

        if (print)
            printArray(raw);

        System.out.println("Finished.");
        StdDraw.save(filename+fileExtension);
        System.out.println(filename+fileExtension + " saved sucessfully");
        System.exit(0);
    }
}
