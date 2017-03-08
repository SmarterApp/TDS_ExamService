package tds.exam.builder;

import tds.session.ExternalSessionConfiguration;

public class ExternalSessionConfigurationBuilder {
    private String clientName = "SBAC_PT";
    private String environment = "Development";
    private int shiftWindowStart;
    private int shiftWindowEnd;
    private int shiftFormStart;
    private int shiftFormEnd;

    public ExternalSessionConfiguration build() {
        return new ExternalSessionConfiguration(
            clientName,
            environment,
            shiftWindowStart,
            shiftWindowEnd,
            shiftFormStart,
            shiftFormEnd
        );
    }

    public ExternalSessionConfigurationBuilder withClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }

    public ExternalSessionConfigurationBuilder withEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public ExternalSessionConfigurationBuilder withShiftWindowStart(int shiftWindowStart) {
        this.shiftWindowStart = shiftWindowStart;
        return this;
    }

    public ExternalSessionConfigurationBuilder withShiftWindowEnd(int shiftWindowEnd) {
        this.shiftWindowEnd = shiftWindowEnd;
        return this;
    }

    public ExternalSessionConfigurationBuilder withShiftFormStart(int shiftFormStart) {
        this.shiftFormStart = shiftFormStart;
        return this;
    }

    public ExternalSessionConfigurationBuilder withShiftFormEnd(int shiftFormEnd) {
        this.shiftFormEnd = shiftFormEnd;
        return this;
    }
}
