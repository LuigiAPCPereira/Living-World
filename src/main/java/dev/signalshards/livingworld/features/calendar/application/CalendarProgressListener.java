package dev.signalshards.livingworld.features.calendar.application;

@FunctionalInterface
public interface CalendarProgressListener {
    void onProgress(CalendarProgress progress);
}
