package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.koant.sonar.slacknotifier.common.component.AbstractSlackNotifyingComponent;
import com.koant.sonar.slacknotifier.common.component.ProjectConfig;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.LocalizedMessages;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

public class SlackPostProjectAnalysisTask extends AbstractSlackNotifyingComponent implements PostProjectAnalysisTask {

    private static final Logger LOG = Loggers.get(SlackPostProjectAnalysisTask.class);

    private final LocalizedMessages l10n;
    private final Slack slackClient;

    public SlackPostProjectAnalysisTask(Configuration configuration) {
        this(Slack.getInstance(), configuration, new LocalizedMessages(Locale.ENGLISH, "core"));
    }

    public SlackPostProjectAnalysisTask(Slack slackClient, Configuration configuration, LocalizedMessages l10n) {
        super(configuration);
        this.slackClient = slackClient;
        this.l10n = l10n;
    }

    @Override
    public void finished(Context context) {
        ProjectAnalysis analysis = context.getProjectAnalysis();
        refreshSettings();
        if (!isPluginEnabled()) {
            LOG.info("Slack notifier plugin disabled, skipping. Settings are [{}]", logRelevantSettings());
            return;
        }
        LOG.info("Analysis ScannerContext: [{}]", analysis.getScannerContext().getProperties());
        String projectKey = analysis.getProject().getKey();

        Optional<ProjectConfig> projectConfigOptional = getProjectConfig(projectKey);
        if (projectConfigOptional.isEmpty()) {
            return;
        }

        ProjectConfig projectConfig = projectConfigOptional.get();
        if (shouldSkipSendingNotification(projectConfig, analysis.getQualityGate())) {
            return;
        }

        LOG.info("Slack notification will be sent: " + analysis);

        Payload payload = ProjectAnalysisPayloadBuilder.of(analysis)
                .l10n(l10n)
                .projectConfig(projectConfig)
                .projectUrl(projectUrl(projectKey))
                .username(getSlackUser())
                .build();

        try {
            // See https://github.com/seratch/jslack
            WebhookResponse response = slackClient.send(getSlackIncomingWebhookUrl(), payload);
            if (!Integer.valueOf(200).equals(response.getCode())) {
                LOG.error("Failed to post to slack, response is [{}]", response);
            }
        } catch (IOException e) {
            LOG.error("Failed to send slack message", e);
        }
    }

    private String projectUrl(String projectKey) {
        return getSonarServerUrl() + "dashboard?id=" + projectKey;
    }


}
