import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class SampleLabelGenerator {
    private static final int WIDTH = 1696;
    private static final int HEIGHT = 2528;
    private static final String WARNING = "GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.";
    private static final String WARNING_TITLE_CASE = "Government Warning: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects. (2) Consumption of alcoholic beverages impairs your ability to drive a car or operate machinery, and may cause health problems.";
    private static final String WARNING_TRUNCATED = "GOVERNMENT WARNING: (1) According to the Surgeon General, women should not drink alcoholic beverages during pregnancy because of the risk of birth defects.";
    private static final List<String> BATCH_FILENAMES = List.of(
        "bourbon-label.png",
        "wrong-abv.png",
        "warning-titlecase.png",
        "missing-net.png",
        "blurry-label.png"
    );

    public static void main(String[] args) throws IOException {
        Path samplesRoot = args.length > 0 ? Path.of(args[0]) : Path.of("frontend", "public", "samples");
        Path singleOut = samplesRoot.resolve("single");
        Path batchOut = samplesRoot.resolve("batch");
        Files.createDirectories(singleOut);
        Files.createDirectories(batchOut);

        write(singleOut.resolve("bourbon-label.png"), label(new LabelSpec(
            "OLD TOM DISTILLERY",
            "Kentucky Straight Bourbon Whiskey",
            "45% Alc./Vol. (90 Proof)",
            "750 mL",
            "Bottled by Old Tom Distillery, Louisville, KY",
            null,
            WARNING,
            new Color(150, 86, 42),
            false
        )));
        write(singleOut.resolve("stone-label.png"), label(new LabelSpec(
            "STONE'S THROW",
            "Straight Bourbon Whiskey",
            "40% Alc./Vol. (80 Proof)",
            "750 mL",
            "Bottled by Stone's Throw Distilling, Richmond, VA",
            null,
            WARNING,
            new Color(66, 101, 132),
            false
        )));
        write(singleOut.resolve("wrong-abv.png"), label(new LabelSpec(
            "OLD TOM DISTILLERY",
            "Kentucky Straight Bourbon Whiskey",
            "42% Alc./Vol. (84 Proof)",
            "750 mL",
            "Bottled by Old Tom Distillery, Louisville, KY",
            null,
            WARNING,
            new Color(150, 86, 42),
            false
        )));
        write(singleOut.resolve("warning-titlecase.png"), label(new LabelSpec(
            "OLD TOM DISTILLERY",
            "Kentucky Straight Bourbon Whiskey",
            "45% Alc./Vol. (90 Proof)",
            "750 mL",
            "Bottled by Old Tom Distillery, Louisville, KY",
            null,
            WARNING_TITLE_CASE,
            new Color(150, 86, 42),
            false
        )));
        write(singleOut.resolve("missing-net.png"), label(new LabelSpec(
            "OLD TOM DISTILLERY",
            "Kentucky Straight Bourbon Whiskey",
            "45% Alc./Vol. (90 Proof)",
            null,
            "Bottled by Old Tom Distillery, Louisville, KY",
            null,
            WARNING,
            new Color(150, 86, 42),
            false
        )));
        write(singleOut.resolve("import-label.png"), label(new LabelSpec(
            "OLD TOM DISTILLERY",
            "Canadian Whisky",
            "45% Alc./Vol. (90 Proof)",
            "750 mL",
            "Imported by Old Tom Imports, Buffalo, NY",
            "Product of Canada",
            WARNING,
            new Color(160, 38, 38),
            false
        )));
        write(singleOut.resolve("truncated-warning.png"), label(new LabelSpec(
            "OLD TOM DISTILLERY",
            "Kentucky Straight Bourbon Whiskey",
            "45% Alc./Vol. (90 Proof)",
            "750 mL",
            "Bottled by Old Tom Distillery, Louisville, KY",
            null,
            WARNING_TRUNCATED,
            new Color(150, 86, 42),
            false
        )));
        write(singleOut.resolve("blurry-label.png"), badPhoto(label(new LabelSpec(
            "OLD TOM DISTILLERY",
            "Kentucky Straight Bourbon Whiskey",
            "45% Alc./Vol. (90 Proof)",
            "750 mL",
            "Bottled by Old Tom Distillery, Louisville, KY",
            null,
            WARNING,
            new Color(75, 75, 75),
            true
        ))));

        copyBatchSubset(singleOut, batchOut);
    }

    private static void copyBatchSubset(Path singleOut, Path batchOut) throws IOException {
        for (String filename : BATCH_FILENAMES) {
            Path source = singleOut.resolve(filename);
            Path target = batchOut.resolve(filename);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copied " + target);
        }
    }

    private static BufferedImage label(LabelSpec spec) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        setup(g);
        g.setColor(new Color(247, 242, 229));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(new Color(255, 252, 243));
        g.fillRoundRect(74, 74, WIDTH - 148, HEIGHT - 148, 24, 24);
        g.setStroke(new BasicStroke(20));
        g.setColor(spec.accent());
        g.drawRect(88, 88, WIDTH - 176, HEIGHT - 176);

        int y = 250;
        g.setColor(Color.BLACK);
        y = centered(g, spec.brand(), "Serif", Font.BOLD, 150, y, 120);
        y = centered(g, spec.type(), "Serif", Font.BOLD, 96, y, 220);
        y = centered(g, spec.alcohol(), "SansSerif", Font.PLAIN, 86, y, 118);
        if (spec.netContents() != null) {
            y = centered(g, spec.netContents(), "SansSerif", Font.PLAIN, 86, y, 120);
        } else {
            y += 80;
        }
        if (spec.origin() != null) {
            y = centered(g, spec.origin(), "Serif", Font.BOLD, 82, y, 115);
        }
        y = centered(g, spec.party(), "SansSerif", Font.PLAIN, 70, y, 145);
        y += 70;

        drawRule(g, y);
        y += 105;

        List<String> warningLines = wrap(g, spec.warning(), new Font("SansSerif", Font.PLAIN, spec.compactWarning() ? 54 : 58), WIDTH - 260);
        for (String line : warningLines) {
            Font font = line.startsWith("GOVERNMENT WARNING:") || line.startsWith("Government Warning:")
                ? new Font("SansSerif", Font.BOLD, spec.compactWarning() ? 58 : 64)
                : new Font("SansSerif", Font.PLAIN, spec.compactWarning() ? 54 : 58);
            g.setFont(font);
            g.drawString(line, 130, y);
            y += g.getFontMetrics().getHeight() + 12;
        }

        g.dispose();
        return image;
    }

    private static BufferedImage badPhoto(BufferedImage source) {
        BufferedImage cropped = source.getSubimage(70, 180, WIDTH - 140, 1460);
        BufferedImage landscape = new BufferedImage(2816, 1536, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = landscape.createGraphics();
        setup(g);
        g.setColor(new Color(56, 38, 24));
        g.fillRect(0, 0, landscape.getWidth(), landscape.getHeight());
        g.rotate(Math.toRadians(-3.5), landscape.getWidth() / 2.0, landscape.getHeight() / 2.0);
        g.drawImage(cropped, 230, 60, 2360, 1450, null);
        g.setColor(new Color(255, 255, 230, 70));
        g.fillOval(1280, 1000, 1400, 520);
        g.dispose();

        BufferedImage small = new BufferedImage(704, 384, BufferedImage.TYPE_INT_RGB);
        Graphics2D sg = small.createGraphics();
        setup(sg);
        sg.drawImage(landscape, 0, 0, small.getWidth(), small.getHeight(), null);
        sg.dispose();

        BufferedImage blurred = new BufferedImage(2816, 1536, BufferedImage.TYPE_INT_RGB);
        Graphics2D bg = blurred.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        bg.drawImage(small, 0, 0, blurred.getWidth(), blurred.getHeight(), null);
        bg.dispose();

        return blurred;
    }

    private static int centered(Graphics2D g, String text, String family, int style, int size, int y, int after) {
        Font font = new Font(family, style, size);
        g.setFont(fit(g, font, text, WIDTH - 260));
        FontMetrics metrics = g.getFontMetrics();
        int x = (WIDTH - metrics.stringWidth(text)) / 2;
        g.drawString(text, Math.max(110, x), y);
        return y + metrics.getHeight() + after;
    }

    private static Font fit(Graphics2D g, Font font, String text, int maxWidth) {
        Font current = font;
        g.setFont(current);
        while (g.getFontMetrics().stringWidth(text) > maxWidth && current.getSize() > 28) {
            current = current.deriveFont((float) current.getSize() - 4);
            g.setFont(current);
        }
        return current;
    }

    private static void drawRule(Graphics2D g, int y) {
        g.setStroke(new BasicStroke(5));
        g.setColor(new Color(190, 120, 65));
        g.drawLine(250, y, WIDTH - 250, y);
        g.fillRect(WIDTH / 2 - 16, y - 16, 32, 32);
        g.setColor(Color.BLACK);
    }

    private static List<String> wrap(Graphics2D g, String text, Font font, int maxWidth) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        String[] words = text.split("\\s+");
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String next = line.isEmpty() ? word : line + " " + word;
            if (metrics.stringWidth(next) > maxWidth && !line.isEmpty()) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(next);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    private static void write(Path path, BufferedImage image) throws IOException {
        ImageIO.write(image, "png", path.toFile());
        System.out.println("Wrote " + path);
    }

    private static void setup(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private record LabelSpec(
        String brand,
        String type,
        String alcohol,
        String netContents,
        String party,
        String origin,
        String warning,
        Color accent,
        boolean compactWarning
    ) {
    }
}
