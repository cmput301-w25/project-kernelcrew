package com.kernelcrew.moodapp;

import de.ohmesoftware.classestoplantuml.Converter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class PlantUMLGenerator {
    public static void main(String[] args) {
        try {
            // Set the package name to scan
            String packageName = "com.kernelcrew.moodapp";

            // Set the output file path
            String outputFilePath = "code/app/src/main/resources/output.pu";

            // Adjusted constructor call based on what `javap` showed
            Converter converter = new Converter(packageName, Arrays.asList(".*"), Arrays.asList());

            // Convert Java classes to PlantUML
            String plantUmlCode = converter.convert();

            // Write the output to a file
            File outputFile = new File(outputFilePath);
            outputFile.getParentFile().mkdirs(); // Ensure directories exist
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(plantUmlCode);
            }

            System.out.println("PlantUML file generated at: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing PlantUML file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
