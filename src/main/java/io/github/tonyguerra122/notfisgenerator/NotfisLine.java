package io.github.tonyguerra122.notfisgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;

public final class NotfisLine implements Comparable<NotfisLine> {
    private final int registration;
    private final List<NotfisField> field;

    public NotfisLine(String registration, List<NotfisField> field) {
        this.registration = Integer.parseInt(registration);
        this.field = field;
    }

    public int getRegistration() {
        return registration;
    }

    public List<NotfisField> getField() {
        return field;
    }

    @Override
    public int compareTo(NotfisLine other) {
        return Integer.compare(this.registration, other.registration);
    }

    public static List<NotfisLine> orderLines(List<NotfisLine> lines) {
        final List<NotfisLine> priorityLines = new ArrayList<>();
        final List<NotfisLine> remainingLines = new ArrayList<>(lines);

        final int minRegistration = remainingLines.stream()
                .mapToInt(NotfisLine::getRegistration)
                .min()
                .orElse(Integer.MAX_VALUE);

        for (final NotfisLine line : new ArrayList<>(remainingLines)) {
            if (line.getRegistration() == minRegistration) {
                priorityLines.add(line);
                remainingLines.remove(line);
            }
        }

        final HashMap<Integer, Queue<NotfisLine>> registrationMap = new HashMap<>();
        final List<NotfisLine> uniqueLines = new ArrayList<>();

        for (final NotfisLine line : remainingLines) {
            final int registration = line.getRegistration();
            registrationMap
                    .computeIfAbsent(registration, k -> new LinkedList<>())
                    .add(line);
        }

        registrationMap.entrySet().removeIf(entry -> {
            if (entry.getValue().size() == 1) {
                uniqueLines.add(entry.getValue().poll());
                return true;
            }
            return false;
        });

        final List<NotfisLine> intercalatedRepeatedLines = new ArrayList<>();
        while (!registrationMap.isEmpty()) {
            List<Integer> keysToRemove = new ArrayList<>();

            for (final Integer key : registrationMap.keySet()) {
                final Queue<NotfisLine> queue = registrationMap.get(key);
                if (queue != null && !queue.isEmpty()) {
                    intercalatedRepeatedLines.add(queue.poll());
                }

                if (queue.isEmpty()) {
                    keysToRemove.add(key);
                }
            }

            for (final Integer key : keysToRemove) {
                registrationMap.remove(key);
            }
        }

        final List<NotfisLine> rearrangedLines = new ArrayList<>();

        rearrangedLines.addAll(priorityLines);

        final List<NotfisLine> lowerLines = new ArrayList<>();
        final List<NotfisLine> higherLines = new ArrayList<>();

        final int minRepeatedRegistration = intercalatedRepeatedLines.stream()
                .mapToInt(NotfisLine::getRegistration)
                .min()
                .orElse(Integer.MAX_VALUE);

        final int maxRepeatedRegistration = intercalatedRepeatedLines.stream()
                .mapToInt(NotfisLine::getRegistration)
                .max()
                .orElse(Integer.MIN_VALUE);

        for (final NotfisLine line : uniqueLines) {
            if (line.getRegistration() < minRepeatedRegistration) {
                lowerLines.add(line);
            } else if (line.getRegistration() > maxRepeatedRegistration) {
                higherLines.add(line);
            } else {
                intercalatedRepeatedLines.add(line);
            }
        }

        Collections.sort(lowerLines);

        Collections.sort(higherLines);

        rearrangedLines.addAll(lowerLines);

        rearrangedLines.addAll(intercalatedRepeatedLines);

        rearrangedLines.addAll(higherLines);

        return rearrangedLines;
    }
}