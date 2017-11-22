package main;

import javafx.util.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class HardDiskDrive {
    private String model;
    private String firmware;
    private String serialNumber;
    private int sizeAllMemory;
    private float sizeFreeMemory;
    private List<String> listSupportedStandarts;
    private List<String> listSupportedPIO;
    private List<String> listSupportedDMA;

    private Process process = null;
    private BufferedReader reader = null;

    public HardDiskDrive() {
        getInfoAboutHardDiskDrive();
    }

    public void getInfoAboutHardDiskDrive() {
        try {
            process = Runtime.getRuntime().exec( new String[]{"/bin/bash","-c","echo "
                    + "PASS" + " | sudo -S hdparm -I /dev/sda "});
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            model = parse(reader, "Model Number:");
            serialNumber = parse(reader, "Serial Number:");
            firmware = parse(reader, "Firmware Revision:");
            listSupportedStandarts = parseList(reader, "Supported");
            sizeAllMemory = Integer.parseInt(parse(reader,
                    "device size with M = 1000*1000:")
                    .split("\\(")[1].split(" ")[0]);
            listSupportedDMA = parseList(reader, "DMA");
            listSupportedPIO = parseList(reader, "PIO");
            process.destroy();
            reader.close();
            process = Runtime.getRuntime().exec( "df -l");
            reader = new BufferedReader((new InputStreamReader(process.getInputStream())));
            reader.readLine();
            Pair<Float, Float> pair = parseMemory(reader);
            sizeFreeMemory = pair.getValue();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if( process != null ) process.destroy();
        }
    }

    public String parse(BufferedReader reader, String entity) throws IOException {
        String parseLine;
        while((parseLine = reader.readLine())!= null) {
            if(parseLine.contains(entity)){
                return parseLine.split(":")[1].trim();
            }
        }
        return "Not found";
    }

    public List<String> parseList(BufferedReader reader, String entity) throws IOException {
        String parseLine;
        while((parseLine = reader.readLine())!= null) {
            if(parseLine.contains(entity)){
                return Arrays.asList(parseLine.split(":")[1].trim().split(" "));
            }
        }
        return null;
    }

    public Pair<Float, Float> parseMemory(BufferedReader reader) throws IOException {
        String parseLine;
        float BusyMemory = 0;
        float FreeMemory = 0;
        while((parseLine = reader.readLine())!=null){
            BusyMemory +=  Integer.parseInt(parseLine.replaceAll("\\s+", " ").split(" ")[2]);
            FreeMemory +=  Integer.parseInt(parseLine.replaceAll("\\s+", " ").split(" ")[3]);
        }
        return new Pair(BusyMemory/1024/1024, FreeMemory/1024/1024);
    }

    @Override
    public String toString() {
        return  "Model: " + model + "\n" +
                "FirmWare: " + firmware + "\n" +
                "SerialNumber: " + serialNumber + "\n" +
                "Memory: " + sizeAllMemory +  " GB\n" +
                "Free memory: " + sizeFreeMemory + " GB\n" +
                "Busy memory: " + (sizeAllMemory - sizeFreeMemory) + " GB\n" +
                "Supported standarts ATA: " + listSupportedStandarts + "\n" +
                "PIO: " + listSupportedPIO + "\n" +
                "DMA: " + listSupportedDMA + "\n";
    }
}