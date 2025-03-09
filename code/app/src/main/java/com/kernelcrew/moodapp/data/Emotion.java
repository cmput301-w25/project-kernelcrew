package com.kernelcrew.moodapp.data;

import androidx.annotation.NonNull;

import com.kernelcrew.moodapp.R;

public enum Emotion {
    ANGER {
        @Override
        public int getIconRes() {
            return R.drawable.ic_anger;
        }

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
        @Override
        public int getIconRes() {
            return R.drawable.ic_confused;
        }

        @Override
        public int getColorRes() {
            return R.color.confusion;
        }

        @NonNull
        @Override
        public String toString() {
            return "Confused";
        }
    },

    DISGUST {
        @Override
        public int getIconRes() {
            return R.drawable.ic_disgust;
        }

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
        @Override
        public int getIconRes() {
            return R.drawable.ic_fear;
        }

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

    HAPPINESS {
        @Override
        public int getIconRes() {
            return R.drawable.ic_happy;
        }

        @Override
        public int getColorRes() {
            return R.color.happiness;
        }

        @NonNull
        @Override
        public String toString() {
            return "Happy";
        }
    },

    SADNESS {
        @Override
        public int getIconRes() {
            return R.drawable.ic_sad;
        }

        @Override
        public int getColorRes() {
            return R.color.sadness;
        }

        @NonNull
        @Override
        public String toString() {
            return "Sad";
        }
    },

    SHAME {
        @Override
        public int getIconRes() {
            return R.drawable.ic_shame;
        }

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
        @Override
        public int getIconRes() {
            return R.drawable.ic_surprise;
        }

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

    /**
     * Get the resource ID of the icon drawable for this emotion
     * @return Drawable resource ID
     */
    public abstract int getIconRes();

    /**
     * Get the resource ID for the color of this emotion
     * @return Color resource ID
     */
    public abstract int getColorRes();
}
