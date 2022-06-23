package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.config.internal.Settings;
import org.sonar.api.utils.LocalizedMessages;

import java.io.IOException;

import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.*;
import static com.koant.sonar.slacknotifier.extension.task.Analyses.PROJECT_KEY;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Created by 616286 on 3.6.2016.
 * Modified by poznachowski
 */
public class SlackPostProjectAnalysisTaskTest {

    private static final String HOOK = "hook";
    private static final String DIFFERENT_KEY = "different:key";

    CaptorPostProjectAnalysisTask postProjectAnalysisTask;
    SlackPostProjectAnalysisTask task;
    private Slack slackClient;
    private Settings settings;
    LocalizedMessages l10n;

    @Before
    public void before() throws IOException {
        postProjectAnalysisTask = new CaptorPostProjectAnalysisTask();
        settings = new MapSettings();
        settings.setProperty(ENABLED.property(), "true");
        settings.setProperty(SlackNotifierProp.HOOK.property(), HOOK);
        settings.setProperty(CHANNEL.property(), "channel");
        settings.setProperty(USER.property(), "user");
        settings.setProperty(CONFIG.property(), PROJECT_KEY);
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + PROJECT.property(), PROJECT_KEY);
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + CHANNEL.property(), "#random");
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + QG_FAIL_ONLY.property(), "false");
        settings.setProperty("sonar.core.serverBaseURL", "http://your.sonar.com/");
        slackClient = Mockito.mock(Slack.class);
        WebhookResponse webhookResponse = WebhookResponse.builder().code(200).build();
        when(slackClient.send(anyString(), any(Payload.class))).thenReturn(webhookResponse);
        l10n = Mockito.mock(LocalizedMessages.class);
        Mockito.when(l10n.format(anyString())).thenAnswer((Answer<String>) invocation -> (String) invocation.getArguments()[2]);
        task = new SlackPostProjectAnalysisTask(slackClient, settings, l10n);
    }

    @Test
    public void shouldCall() throws Exception {
        Analyses.simple(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verify(slackClient, times(1)).send(eq(HOOK), any(Payload.class));
    }

    @Test
    public void shouldSkipIfPluginDisabled() throws Exception {
        settings.setProperty(ENABLED.property(), "false");
        Analyses.simple(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verifyZeroInteractions(slackClient);
    }

    @Test
    public void shouldSkipIfNoConfigFound() throws Exception {
        Analyses.simpleDifferentKey(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verifyZeroInteractions(slackClient);
    }

    @Test
    public void shouldSkipIfReportFailedQualityGateButOk() throws Exception {
        settings.setProperty(CONFIG.property() + "." + PROJECT_KEY + "." + QG_FAIL_ONLY.property(), "true");
        Analyses.simple(postProjectAnalysisTask);
        task.finished(postProjectAnalysisTask.getProjectAnalysis());
        Mockito.verifyZeroInteractions(slackClient);
    }
}
