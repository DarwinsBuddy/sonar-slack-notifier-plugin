package com.koant.sonar.slacknotifier.common.component;

import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.sonar.api.config.Configuration;

import java.util.Objects;

public class ProjectConfig {
    private final String projectKey;
    private final String slackChannel;
    private final boolean qgFailOnly;

    public ProjectConfig(String projectKey, String slackChannel, boolean qgFailOnly) {
        this.projectKey = projectKey;
        this.slackChannel = slackChannel;
        this.qgFailOnly = qgFailOnly;
    }

    /**
     * Cloning constructor
     *
     * @param c config
     */
    public ProjectConfig(ProjectConfig c) {
        this.projectKey = c.getProjectKey();
        this.slackChannel = c.getSlackChannel();
        this.qgFailOnly = c.isQgFailOnly();
    }

    static ProjectConfig create(Configuration configuration, String configurationId) {
        String configurationPrefix = SlackNotifierProp.CONFIG.property() + "." + configurationId + ".";
        String projectKey = configuration.get(configurationPrefix + SlackNotifierProp.PROJECT.property()).orElse(null);
        String slackChannel = configuration.get(configurationPrefix + SlackNotifierProp.CHANNEL.property()).orElse(null);
        boolean qgFailOnly = configuration.getBoolean(configurationPrefix + SlackNotifierProp.QG_FAIL_ONLY.property()).orElse(false);
        return new ProjectConfig(projectKey, slackChannel, qgFailOnly);
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getSlackChannel() {
        return slackChannel;
    }

    public boolean isQgFailOnly() {
        return qgFailOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectConfig that = (ProjectConfig) o;
        return qgFailOnly == that.qgFailOnly &&
                Objects.equals(projectKey, that.projectKey) &&
                Objects.equals(slackChannel, that.slackChannel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectKey, slackChannel, qgFailOnly);
    }

    @Override
    public String toString() {
        return "ProjectConfig{" + "projectKey='" + projectKey + '\'' +
            ", slackChannel='" + slackChannel + '\'' +
            ", qgFailOnly=" + qgFailOnly +
            '}';
    }
}
