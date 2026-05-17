package com.labelverifier.service;

import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class NormalizationService {
    public String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.US)
            .replace("&", " and ")
            .replaceAll("[^a-z0-9.%]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
        return normalized;
    }

    public double similarity(String left, String right) {
        String a = normalizeText(left);
        String b = normalizeText(right);
        if (a.isBlank() || b.isBlank()) {
            return 0;
        }
        if (a.equals(b) || a.contains(b) || b.contains(a)) {
            return 1;
        }
        int distance = levenshtein(a, b);
        int max = Math.max(a.length(), b.length());
        return max == 0 ? 1 : 1 - ((double) distance / max);
    }

    private int levenshtein(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];
        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[right.length()];
    }
}
