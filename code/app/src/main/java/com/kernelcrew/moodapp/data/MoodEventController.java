package com.kernelcrew.moodapp.data;

public class MoodEventController {
    private static MoodEventController instance;

    private MoodEventController() {}

    public static MoodEventController getInstance() {
        if (instance == null) {
            instance = new MoodEventController();
        }

        return instance;
    }

    public void createMoodEvent(MoodEvent moodEvent) {
        // TODO
    }
}
