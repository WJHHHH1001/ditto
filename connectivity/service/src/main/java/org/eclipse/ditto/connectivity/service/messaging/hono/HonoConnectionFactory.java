/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.connectivity.service.messaging.hono;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.ditto.connectivity.model.Connection;
import org.eclipse.ditto.connectivity.model.ConnectivityModelFactory;
import org.eclipse.ditto.connectivity.model.HonoAddressAlias;
import org.eclipse.ditto.connectivity.model.Source;
import org.eclipse.ditto.connectivity.model.Target;
import org.eclipse.ditto.connectivity.model.Topic;
import org.eclipse.ditto.connectivity.model.UserPasswordCredentials;
import org.eclipse.ditto.connectivity.service.config.DefaultHonoConfig;
import org.eclipse.ditto.connectivity.service.config.HonoConfig;

import akka.actor.ActorSystem;

public abstract class HonoConnectionFactory {

    protected final ActorSystem actorSystem;
    protected final Connection connection;
    protected final HonoConfig honoConfig;

    protected HonoConnectionFactory(ActorSystem actorSystem, Connection connection) {
        this.actorSystem = actorSystem;
        this.connection = connection;
        this.honoConfig = new DefaultHonoConfig(actorSystem);
    }

    protected abstract UserPasswordCredentials getCredentials();

    protected abstract String getTenantId();

    private static String getBootstrapServerUrisAsCommaSeparatedListString(final HonoConfig honoConfig) {
        return honoConfig.getBootstrapServerUris()
                .stream()
                .map(URI::toString)
                .collect(Collectors.joining(","));
    }

    public Connection enrichConnection() {
        final var connectionId = connection.getId();
        return ConnectivityModelFactory.newConnectionBuilder(connection)
                .uri(honoConfig.getBaseUri().toString())
                .validateCertificate(honoConfig.isValidateCertificates())
                .specificConfig(Map.of(
                        "saslMechanism", honoConfig.getSaslMechanism().toString(),
                        "bootstrapServers", getBootstrapServerUrisAsCommaSeparatedListString(honoConfig),
                        "groupId", (getTenantId().isEmpty() ? "" : getTenantId() + "_") + connectionId)
                )
                .credentials(getCredentials())
                .setSources(connection.getSources()
                        .stream()
                        .map(source -> resolveSourceAliases(source, getTenantId()))
                        .toList())
                .setTargets(connection.getTargets()
                        .stream()
                        .map(target -> resolveTargetAlias(target, getTenantId()))
                        .toList())
                .build();
    }

    @SuppressWarnings("unchecked")
    private static Source resolveSourceAliases(final Source source, final String tenantId) {
        final var sourceBuilder = ConnectivityModelFactory.newSourceBuilder(source)
                .addresses(source.getAddresses()
                        .stream()
                        .map(address -> HonoAddressAlias.resolve(address, tenantId))
                        .filter(address -> !address.isEmpty())
                        .collect(Collectors.toSet()));
        if (source.getAddresses().contains(HonoAddressAlias.TELEMETRY.getName())
                || source.getAddresses().contains(HonoAddressAlias.EVENT.getName())) {
            source.getReplyTarget().ifPresent(replyTarget -> {
                var headerMapping = replyTarget.getHeaderMapping().toJson()
                        .setValue("correlation-id", "{{ header:correlation-id }}");
                if (HonoAddressAlias.COMMAND.getName().equals(replyTarget.getAddress())) {
                    headerMapping = headerMapping
                            .setValue("device_id", "{{ thing:id }}")
                            .setValue("subject",
                                    "{{ header:subject | fn:default(topic:action-subject) | fn:default(topic:criterion) }}-response");
                }
                sourceBuilder.replyTarget(replyTarget.toBuilder()
                        .address(HonoAddressAlias.resolve(replyTarget.getAddress(), tenantId, true))
                        .headerMapping(ConnectivityModelFactory.newHeaderMapping(headerMapping))
                        .build());
            });
        }
        if (source.getAddresses().contains(HonoAddressAlias.COMMAND_RESPONSE.getName())) {
            sourceBuilder.headerMapping(ConnectivityModelFactory
                    .newHeaderMapping(source.getHeaderMapping().toJson()
                            .setValue("correlation-id", "{{ header:correlation-id }}")
                            .setValue("status", "{{ header:status }}")));
        }
        return sourceBuilder.build();
    }

    private static Target resolveTargetAlias(final Target target, final String tenantId) {
        final var resolvedAddress = HonoAddressAlias.resolve(target.getAddress(), tenantId, true);
        final var targetBuilder = ConnectivityModelFactory.newTargetBuilder(target);
        if (!resolvedAddress.isEmpty()) {
            targetBuilder.address(resolvedAddress);
        }

        var headerMapping = target.getHeaderMapping().toJson()
                .setValue("device_id", "{{ thing:id }}")
                .setValue("correlation-id", "{{ header:correlation-id }}")
                .setValue("subject", "{{ header:subject | fn:default(topic:action-subject) }}");
        if (target.getTopics().stream()
                .anyMatch(topic -> topic.getTopic() == Topic.LIVE_MESSAGES ||
                        topic.getTopic() == Topic.LIVE_COMMANDS)) {
            headerMapping = headerMapping.setValue("response-required", "{{ header:response-required }}");
        }
        targetBuilder.headerMapping(ConnectivityModelFactory.newHeaderMapping(headerMapping));
        return targetBuilder.build();
    }

}
