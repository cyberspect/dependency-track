/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.notification.publisher;

import alpine.common.logging.Logger;
import alpine.model.ConfigProperty;
import alpine.notification.Notification;
import org.dependencytrack.exception.PublisherException;
import org.dependencytrack.persistence.QueryManager;
import org.dependencytrack.util.DebugDataEncryption;

import jakarta.json.JsonObject;
import java.util.Map;

import static org.dependencytrack.model.ConfigPropertyConstants.JIRA_PASSWORD;
import static org.dependencytrack.model.ConfigPropertyConstants.JIRA_URL;
import static org.dependencytrack.model.ConfigPropertyConstants.JIRA_USERNAME;

/**
 * Class that handles publishing a ticket to a Jira instance when a new notification is received.
 *
 * @author Mvld3r
 * @since 4.7
 */
public class JiraPublisher extends AbstractWebhookPublisher implements Publisher {

    private static final Logger LOGGER = Logger.getLogger(JiraPublisher.class);

    private String jiraProjectKey;
    private String jiraTicketType;

    @Override
    public void inform(final PublishContext ctx, final Notification notification, final JsonObject config) {
        if (config == null) {
            LOGGER.warn("No publisher configuration provided; Skipping notification (%s)".formatted(ctx));
            return;
        }

        jiraTicketType = config.getString("jiraTicketType", null);
        if (jiraTicketType == null) {
            LOGGER.warn("No JIRA ticket type configured; Skipping notification (%s)".formatted(ctx));
            return;
        }

        jiraProjectKey = config.getString(CONFIG_DESTINATION, null);
        if (jiraProjectKey == null) {
            LOGGER.warn("No JIRA project key configured; Skipping notification (%s)".formatted(ctx));
            return;
        }

        publish(ctx, getTemplate(config), notification, config);
    }

    @Override
    public String getDestinationUrl(final JsonObject config) {
        try (final QueryManager qm = new QueryManager()) {
            final String baseUrl = qm.getConfigProperty(JIRA_URL.getGroupName(), JIRA_URL.getPropertyName()).getPropertyValue();
            return (baseUrl.endsWith("/") ? baseUrl : baseUrl + '/') + "rest/api/2/issue";
        } catch (final Exception e) {
            throw new PublisherException("An error occurred during the retrieval of the Jira URL", e);
        }
    }

    @Override
    protected AuthCredentials getAuthCredentials() {
        try (final QueryManager qm = new QueryManager()) {
            final ConfigProperty jiraUsernameProp = qm.getConfigProperty(JIRA_USERNAME.getGroupName(), JIRA_USERNAME.getPropertyName());
            final String jiraUsername = (jiraUsernameProp == null) ? null : jiraUsernameProp.getPropertyValue();
            final ConfigProperty jiraPasswordProp = qm.getConfigProperty(JIRA_PASSWORD.getGroupName(), JIRA_PASSWORD.getPropertyName());
            final String jiraPassword = (jiraPasswordProp == null || jiraPasswordProp.getPropertyValue() == null) ? null : DebugDataEncryption.decryptAsString(jiraPasswordProp.getPropertyValue());
            return new AuthCredentials(jiraUsername, jiraPassword);
        } catch (final Exception e) {
            throw new PublisherException("An error occurred during the retrieval of Jira credentials", e);
        }
    }

    @Override
    public void enrichTemplateContext(final Map<String, Object> context) {
        context.put("jiraProjectKey", jiraProjectKey);
        context.put("jiraTicketType", jiraTicketType);
    }
}