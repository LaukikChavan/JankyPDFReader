import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.RenderDestination;

import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.synthesis.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class PageImages extends SpeakableAdapter{
    public PDDocument myPdf;
    public PDFRenderer pageRenderer;
    public static int pageCount = 670;
    public static int lineCount = -1;
    public int currentCount = 0;
    public Tesseract tesseract;
    public Image imageToBeDrawn;
    public String stringToBeDrawn = "";
    public static Synthesizer synthesizer;
    public String readingPara = "";
    public KL keyListener;

    public PageImages(KL keyListener) {
        try {
            myPdf = PDDocument.load(new File(Constants.FILE_PATH));
            pageRenderer = new PDFRenderer(myPdf);
            tesseract = new Tesseract();
            tesseract.setLanguage("eng");
            tesseract.setOcrEngineMode(1);
            this.keyListener = keyListener;
            Path dataDirectory = Paths.get(ClassLoader.getSystemResource("data").toURI());
            tesseract.setDatapath(dataDirectory.toString());
            currentCount = pageCount;

            System.setProperty(
                    "freetts.voices",
                    "com.sun.speech.freetts.en.us"
                            + ".cmu_us_kal.KevinVoiceDirectory");

            Central.registerEngineCentral(
                    "com.sun.speech.freetts"
                            + ".jsapi.FreeTTSEngineCentral");

            PageImages.synthesizer
                    = Central.createSynthesizer(
                    new SynthesizerModeDesc(Locale.ENGLISH));

        } catch (IOException | URISyntaxException | EngineException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (PageImages.pageCount < myPdf.getNumberOfPages() && this.currentCount != PageImages.pageCount) {
            try {
                imageToBeDrawn = pageRenderer.renderImage(PageImages.pageCount, 2.5f, ImageType.RGB, RenderDestination.PRINT);
                stringToBeDrawn = tesseract.doOCR((BufferedImage) imageToBeDrawn).replace("\n", "$$").replace("-$$", "").replace("$$", "\n").replace(".", "\n");
                this.currentCount = PageImages.pageCount;
            } catch (IOException | TesseractException e) {
                e.printStackTrace();
            }
        }

        if (this.keyListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
            try {
                PageImages.nextPage();
                System.out.println("Next Page");
            } catch (EngineException e) {
                e.printStackTrace();
            }
        }

    }

    public void draw(Graphics g) {
        g.drawImage(imageToBeDrawn, 10, 50, 550, 650, null);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setBackground(Color.YELLOW);
        g2.setFont(new Font("Segoe Script", Font.PLAIN, 12));
        g2.setPaint(Color.BLACK);
        drawString(g2, stringToBeDrawn, 600, 100);
        read(stringToBeDrawn);
    }

    void drawString(Graphics2D g, String text, int x, int y) {
        int i = 0;
        for (String line : text.split("\n")) {
            StringRect stringRect = new StringRect(line, x, y += g.getFontMetrics().getHeight(), i == (PageImages.lineCount + 1));
            stringRect.draw(g);
            i ++;
        }
    }

    public void read(String text) {
        int size = this.readingPara.split("\n").length;
        System.out.println(size + " " + lineCount);
        if(PageImages.lineCount < size && lineCount != -1) {
            String string = this.readingPara.split("\n")[PageImages.lineCount];
            System.out.println("Line: " + string);
            try {
                PageImages.synthesizer.allocate();
                PageImages.synthesizer.resume();
                PageImages.synthesizer.speakPlainText(
                        string, this);
                PageImages.synthesizer.waitEngineState(
                        Synthesizer.QUEUE_EMPTY);
            } catch (Exception e) {
                e.printStackTrace();
            }
            PageImages.lineCount ++;
        } else if (PageImages.lineCount == -1) {
            PageImages.lineCount ++;
        } else {
            PageImages.lineCount = -1;
            PageImages.pageCount++;
        }

        if (!this.readingPara.equalsIgnoreCase(text)) {
            this.readingPara = text;
        }
    }

    public static void nextPage() throws EngineException {
        pageCount ++;
        PageImages.lineCount = -1;
        PageImages.synthesizer.cancelAll();
    }

    @Override
    public void wordStarted(SpeakableEvent speakableEvent) {
        super.wordStarted(speakableEvent);
        if (keyListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
            try {
                PageImages.nextPage();
                System.out.println("Next Page");
            } catch (EngineException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            synthesizer.deallocate();
            myPdf.close();
        } catch (IOException | EngineException e) {
            e.printStackTrace();
        }
    }
}
