package com.kobi.smartbot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class Helper {

    private static final Logger LOG = LoggerFactory.getLogger(Helper.class);

    public String getRandomItem(String... words) {
        return words[new Random().nextInt(words.length)];
    }

    public byte[] createImageBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            baos.flush();
            return baos.toByteArray();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                LOG.error("",e);
            }
        }
    }

    public List<String> splitAnswer(String answer) {
        int maxCharLimit = 3500;
        int maxLineLimit = 90;

        List<String> answers = new ArrayList<>();
        String[] lines = answer.split("\n");

        StringBuilder currentAnswer = new StringBuilder();
        int currentCharCount = 0;
        int currentLineCount = 0;

        for (String line : lines) {
            if (currentCharCount + line.length() <= maxCharLimit && currentLineCount + 1 <= maxLineLimit) {
                currentAnswer.append(line).append("\n");
                currentCharCount += line.length() + 1;
                currentLineCount++;
            } else {
                answers.add(currentAnswer.toString());
                currentAnswer = new StringBuilder(line + "\n");
                currentCharCount = line.length() + 1;
                currentLineCount = 1;
            }
        }

        if (currentAnswer.length() > 0) {
            answers.add(currentAnswer.toString());
        }

        return answers;
    }
}
