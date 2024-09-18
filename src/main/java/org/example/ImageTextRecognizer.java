package org.example;

import com.jhlabs.image.GaussianFilter;
import net.sourceforge.tess4j.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.awt.image.Kernel;
import java.awt.image.ConvolveOp;
import java.util.Set;

//"Los Robles No. 1000 ","Capellania Ramos Arizpe,", "Coahuila, 25903 Mexico"
                    //"Los Robles No. 1000 Capellania Ramos Arizpe, Coahuila, 25903 Mexico"



public class ImageTextRecognizer {
    public static void main(String[] args) {
        String imagePath = "C:\\Users\\laads\\Downloads\\WhatsApp Image 2024-09-02 at 1.43.38 PM (1).jpeg";  // Update this path as needed

        // Define multiple search phrases
        String[] phrases = {
                /* "Los Robles No. 1000",
                 "Capellania Ramos Arizpe,",
                 "25903",
                 "Coahuila, 25903 Mexico"*/
                //"11 to 50",
                //"Mexico",
                // Add more phrases here
                //"Los Robles No. 1000 Capellania Ramos Arizpe, Coahuila, 25903 Mexico"
                 "Los Robles No 1000 ",
                 "Capellania Ramos Arizpe",
                 "Coahuila, 25903 Mexico"

        };

        try {
            highlightTextInImage(imagePath, phrases);
        } catch (TesseractException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void highlightTextInImage(String imagePath, String[] phrases) throws TesseractException, IOException {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");  // Set Tesseract data path
        tesseract.setLanguage("eng");  // Set the language
        tesseract.setPageSegMode(6);  // Set PSM mode to 6 (assume a single block of text)

        File imageFile = new File(imagePath);
        BufferedImage image = ImageIO.read(imageFile);

        if (image == null) {
            throw new IOException("Could not read image file: " + imageFile.getAbsolutePath());
        }

        List<Word> words = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD);

        if (words.isEmpty()) {
            System.out.println("No words detected.");
            return;
        }

        // Debug: Print all recognized words
        System.out.println("Recognized Words:");
        for (Word word : words) {
            System.out.println(word.getText());
        }

        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(230, 0, 255, 147));  // Color with transparency

        for (String phrase : phrases) {
            boolean found = highlightPhrase(g2d, words, phrase);
            if (!found) {
                System.out.println("Phrase not found: " + phrase);
            }
        }

        g2d.dispose();

        File outputImage = new File("C:\\Users\\laads\\Pictures\\highlighted_image.jpeg");  // Update the path as needed
        ImageIO.write(image, "jpeg", outputImage);

        System.out.println("Highlighted image saved as highlighted_image.jpeg");
    }

    private static boolean highlightPhrase(Graphics2D g2d, List<Word> words, String phrase) {
        String[] searchWords = phrase.split("\\s+");
        int searchIndex = 0;
        Rectangle combinedBoundingBox = null;
        int maxWordDistance = 10;  // Maximum distance between words to be considered part of the same phrase

        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            String wordText = word.getText().toLowerCase().trim();

            if (wordText.equals(searchWords[searchIndex].toLowerCase())) {
                Rectangle wordRect = word.getBoundingBox();

                if (combinedBoundingBox == null) {
                    combinedBoundingBox = wordRect;
                } else {
                    combinedBoundingBox = combinedBoundingBox.union(wordRect);
                }

                searchIndex++;

                if (searchIndex == searchWords.length) {
                    g2d.fillRect(combinedBoundingBox.x, combinedBoundingBox.y, combinedBoundingBox.width, combinedBoundingBox.height);
                    return true;
                }
            } else {
                // Check if the next word is within maxWordDistance
                if (searchIndex > 0 && i < words.size() - 1) {
                    Word nextWord = words.get(i + 1);
                    Rectangle nextWordRect = nextWord.getBoundingBox();
                    Rectangle currentWordRect = word.getBoundingBox();
                    int distance = nextWordRect.x - (currentWordRect.x + currentWordRect.width);

                    if (distance <= maxWordDistance) {
                        // Continue searching for the next word in the sequence
                        continue;
                    }
                }

                // Reset the search if the sequence is broken or words are too far apart
                searchIndex = 0;
                combinedBoundingBox = null;
            }
        }

        return false;
    }
}

























