package com.kernelcrew.moodapp.data;

import androidx.annotation.NonNull;

import com.kernelcrew.moodapp.R;

public enum Emotion {
    ANGER {
        @NonNull
        @Override
        public int getIconRes() {
            return R.drawable.ic_anger;
        }

        @NonNull
        @Override
        public int getColorRes() {
            return R.color.anger;
        }

        @NonNull
        @Override
        public String toString() {
            return "Anger";
        }
    },

    CONFUSION {
        @NonNull
        @Override
        public int getIconRes() {
            return R.drawable.ic_confused;
        }

        @NonNull
        @Override
        public int getColorRes() {
            return R.color.confusion;
        }

        @NonNull
        @Override
        public String toString() {
            return "Confusion";
        }
    },

    DISGUST {
        @NonNull
        @Override
        public int getIconRes() {
            return R.drawable.ic_disgust;
        }

        @NonNull
        @Override
        public int getColorRes() {
            return R.color.disgust;
        }

        @NonNull
        @Override
        public String toString() {
            return "Disgust";
        }
    },

    FEAR {
        @NonNull
        @Override
        public int getIconRes() {
            return R.drawable.ic_fear;
        }

        @NonNull
        @Override
        public int getColorRes() {
            return R.color.fear;
        }

        @NonNull
        @Override
        public String toString() {
            return "Fear";
        }
    },

    HAPPY {
        @NonNull
        @Override
        public int getIconRes() {
            return R.drawable.ic_happy;
        }

        @NonNull
        @Override
        public int getColorRes() {
            return R.color.happiness;
        }

        @NonNull
        @Override
        public String toString() {
            return "Happiness";
        }
    },

    SAD {
        @NonNull
        @Override
        public int getIconRes() {
            return R.drawable.ic_sad;
        }

        @NonNull
        @Override
        public int getColorRes() {
            return R.color.sadness;
        }

        @NonNull
        @Override
        public String toString() {
            return "Sadness";
        }
    },

    SHAME {
        @NonNull
        @Override
        public int getIconRes() {
            return R.drawable.ic_shame;
        }

        @NonNull
        @Override
        public int getColorRes() {
            return R.color.shame;
        }

        @NonNull
        @Override
        public String toString() {
            return "Shame";
        }
    },

    SURPRISE {
        @NonNull
        @Override
        public int getIconRes() {
            return R.drawable.ic_surprise;
        }

        @NonNull
        @Override
        public int getColorRes() {
            return R.color.surprise;
        }

        @NonNull
        @Override
        public String toString() {
            return "Surprise";
        }
    };

    @NonNull
    public abstract int getIconRes();

    @NonNull
    public abstract int getColorRes();
}
