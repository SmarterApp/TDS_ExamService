package tds.score.configuration;

public class ItemScoreSettings {
    private boolean enabled = true;
    private boolean debug = false;
    private String serverUrl;
    private String callbackUrl;
    private boolean timerEnabled = true;
    private int timerInterval = 5;
    private int timerPendingMinutes = 15;
    private int timerMaxAttempts = 10;
    private boolean encryptionEnabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(final String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(final String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public boolean isTimerEnabled() {
        return timerEnabled;
    }

    public void setTimerEnabled(final boolean timerEnabled) {
        this.timerEnabled = timerEnabled;
    }

    public int getTimerInterval() {
        return timerInterval;
    }

    public void setTimerInterval(final int timerInterval) {
        this.timerInterval = timerInterval;
    }

    public int getTimerPendingMinutes() {
        return timerPendingMinutes;
    }

    public void setTimerPendingMinutes(final int timerPendingMinutes) {
        this.timerPendingMinutes = timerPendingMinutes;
    }

    public int getTimerMaxAttempts() {
        return timerMaxAttempts;
    }

    public void setTimerMaxAttempts(final int timerMaxAttempts) {
        this.timerMaxAttempts = timerMaxAttempts;
    }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public void setEncryptionEnabled(final boolean encryptionEnabled) {
        this.encryptionEnabled = encryptionEnabled;
    }
}
