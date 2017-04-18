package tds.score.configuration;

public class ItemScoreSettings {
    private boolean enabled = true;
    private boolean debug = false;
    private String serverUrl;
    private String callbackUrl;
    private boolean timerEnabled = true;
    private boolean alwaysLoadRubric = false;
    private int timerInterval = 5;
    private int timerPendingMinutes = 15;
    private int timerMaxAttempts = 10;
    private boolean encryptionEnabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public boolean isTimerEnabled() {
        return timerEnabled;
    }

    public boolean isAlwaysLoadRubric() {
        return alwaysLoadRubric;
    }

    public int getTimerInterval() {
        return timerInterval;
    }

    public int getTimerPendingMinutes() {
        return timerPendingMinutes;
    }

    public int getTimerMaxAttempts() {
        return timerMaxAttempts;
    }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public static final class ItemScoreSettingsBuilder {
        private boolean enabled = true;
        private boolean debug = false;
        private String serverUrl;
        private String callbackUrl;
        private boolean timerEnabled = true;
        private boolean alwaysLoadRubric = false;
        private int timerInterval = 5;
        private int timerPendingMinutes = 15;
        private int timerMaxAttempts = 10;
        private boolean encryptionEnabled = false;

        public ItemScoreSettingsBuilder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ItemScoreSettingsBuilder withDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public ItemScoreSettingsBuilder withServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public ItemScoreSettingsBuilder withCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        public ItemScoreSettingsBuilder withTimerEnabled(boolean timerEnabled) {
            this.timerEnabled = timerEnabled;
            return this;
        }

        public ItemScoreSettingsBuilder withAlwaysLoadRubric(boolean alwaysLoadRubric) {
            this.alwaysLoadRubric = alwaysLoadRubric;
            return this;
        }

        public ItemScoreSettingsBuilder withTimerInterval(int timerInterval) {
            this.timerInterval = timerInterval;
            return this;
        }

        public ItemScoreSettingsBuilder withTimerPendingMinutes(int timerPendingMinutes) {
            this.timerPendingMinutes = timerPendingMinutes;
            return this;
        }

        public ItemScoreSettingsBuilder withTimerMaxAttempts(int timerMaxAttempts) {
            this.timerMaxAttempts = timerMaxAttempts;
            return this;
        }

        public ItemScoreSettingsBuilder withEncryptionEnabled(boolean encryptionEnabled) {
            this.encryptionEnabled = encryptionEnabled;
            return this;
        }

        public ItemScoreSettings build() {
            ItemScoreSettings itemScoreSettings = new ItemScoreSettings();
            itemScoreSettings.callbackUrl = this.callbackUrl;
            itemScoreSettings.timerEnabled = this.timerEnabled;
            itemScoreSettings.timerInterval = this.timerInterval;
            itemScoreSettings.serverUrl = this.serverUrl;
            itemScoreSettings.alwaysLoadRubric = this.alwaysLoadRubric;
            itemScoreSettings.enabled = this.enabled;
            itemScoreSettings.timerPendingMinutes = this.timerPendingMinutes;
            itemScoreSettings.timerMaxAttempts = this.timerMaxAttempts;
            itemScoreSettings.debug = this.debug;
            itemScoreSettings.encryptionEnabled = this.encryptionEnabled;
            return itemScoreSettings;
        }
    }
}
