/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.protocoladapter.signals;

import java.util.Optional;

import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.messages.MessageHeaders;
import org.eclipse.ditto.signals.commands.messages.MessageCommandResponse;

final class MessageCommandResponseSignalMapper extends AbstractMessageSignalMapper<MessageCommandResponse<?, ?>> {

    @Override
    String extractSubject(final MessageCommandResponse<?, ?> command) {
        return command.getMessage().getSubject();
    }

    @Override
    Optional<HttpStatusCode> extractStatusCode(final MessageCommandResponse<?, ?> command) {
        return command.getMessage().getStatusCode();
    }

    @Override
    MessageHeaders extractMessageHeaders(final MessageCommandResponse<?, ?> commandResponse) {
        return commandResponse.getMessage().getHeaders();
    }
}