/*
 * Copyright (c) 2017 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial contribution
 */
package org.eclipse.ditto.services.things.persistence.actors;

import org.eclipse.ditto.services.models.streaming.EntityIdWithRevision;
import org.eclipse.ditto.services.utils.persistence.mongo.AbstractPersistenceStreamingActor;

import akka.actor.Props;
import org.eclipse.ditto.services.utils.akka.LogUtil;
import org.eclipse.ditto.services.utils.akkapersistence.mongoaddons.DittoJavaDslMongoReadJournal;
import org.eclipse.ditto.services.utils.akkapersistence.mongoaddons.DittoMongoReadJournal;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.DiagnosticLoggingAdapter;
import akka.japi.Creator;
import akka.persistence.query.PersistenceQuery;


/**
 * Actor which executes special persistence queries on the things event store.
 */
public final class PersistenceQueriesActor extends AbstractPersistenceStreamingActor<EntityIdWithRevision> {

    /**
     * The name of this Actor in the ActorSystem.
     */
    public static final String ACTOR_NAME = "persistenceQueries";

    private final DittoJavaDslMongoReadJournal readJournal;

    private PersistenceQueriesActor(final int streamingCacheSize) {
        super(streamingCacheSize);
        this.readJournal = PersistenceQuery.get(getContext().getSystem())
                .getReadJournalFor(DittoJavaDslMongoReadJournal.class, DittoMongoReadJournal.Identifier());
    }

    private PersistenceQueriesActor(final int streamingCacheSize, final DittoJavaDslMongoReadJournal readJournal) {
        super(streamingCacheSize);
        this.readJournal = readJournal;
    }

    /**
     * Creates Akka configuration object Props for this PersistenceQueriesActor.
     *
     * @param streamingCacheSize the size of the streaming cache.
     * @return the Akka configuration Props object.
     */
    public static Props props(final int streamingCacheSize) {
        return Props.create(PersistenceQueriesActor.class, new Creator<PersistenceQueriesActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public PersistenceQueriesActor create() {
                return new PersistenceQueriesActor(streamingCacheSize);
            }
        });
    }

    static Props props(final int streamingCacheSize, final DittoJavaDslMongoReadJournal readJournal) {
        return Props.create(PersistenceQueriesActor.class, new Creator<PersistenceQueriesActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public PersistenceQueriesActor create() {
                return new PersistenceQueriesActor(streamingCacheSize, readJournal);
            }
        });
    }

    @Override
    protected DittoJavaDslMongoReadJournal getJournal() {
        return readJournal;
    }

    @Override
    protected EntityIdWithRevision createElement(final String pid, final long sequenceNumber) {
        return EntityIdWithRevision.of(pid.replaceFirst(ThingPersistenceActor.PERSISTENCE_ID_PREFIX, ""), sequenceNumber);
    }
}
