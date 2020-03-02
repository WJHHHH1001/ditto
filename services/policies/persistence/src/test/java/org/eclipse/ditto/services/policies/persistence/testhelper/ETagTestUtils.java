/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.services.policies.persistence.testhelper;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.policies.Label;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.Resource;
import org.eclipse.ditto.model.policies.Subject;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyEntryResponse;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyResponse;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyResourceResponse;
import org.eclipse.ditto.signals.commands.policies.modify.ModifySubjectResponse;
import org.eclipse.ditto.signals.commands.policies.query.RetrievePolicyEntryResponse;
import org.eclipse.ditto.signals.commands.policies.query.RetrievePolicyResponse;
import org.eclipse.ditto.signals.commands.policies.query.RetrieveResourceResponse;
import org.eclipse.ditto.signals.commands.policies.query.RetrieveSubjectResponse;

public class ETagTestUtils {

    private ETagTestUtils() { }

    public static ModifyPolicyEntryResponse modifyPolicyEntryResponse(final PolicyId policyId,
            final PolicyEntry policyEntry, final DittoHeaders dittoHeaders, final boolean created) {
        final DittoHeaders dittoHeadersWithETagHeader = appendETagHeader(policyEntry, dittoHeaders);
        if (created) {
            return ModifyPolicyEntryResponse.created(policyId, policyEntry, dittoHeadersWithETagHeader);
        } else {
            return ModifyPolicyEntryResponse.modified(policyId, policyEntry.getLabel(), dittoHeadersWithETagHeader);
        }
    }

    public static RetrievePolicyEntryResponse retrievePolicyEntryResponse(final PolicyId policyId,
            final PolicyEntry policyEntry, final DittoHeaders dittoHeaders) {
        final DittoHeaders dittoHeadersWithETagHeader = appendETagHeader(policyEntry, dittoHeaders);
        return RetrievePolicyEntryResponse.of(policyId, policyEntry, dittoHeadersWithETagHeader);
    }

    public static ModifyResourceResponse modifyResourceResponse(final PolicyId policyId, final Resource resource,
            final Label label, final DittoHeaders dittoHeaders, final boolean created) {
        final DittoHeaders dittoHeadersWithETagHeader = appendETagHeader(resource, dittoHeaders);
        if (created) {
            return ModifyResourceResponse.created(policyId, label, resource, dittoHeadersWithETagHeader);
        } else {
            return ModifyResourceResponse.modified(policyId, label, resource.getResourceKey(),
                    dittoHeadersWithETagHeader);
        }
    }

    public static RetrieveResourceResponse retrieveResourceResponse(final PolicyId policyId,
            final Label label, final Resource resource, final DittoHeaders dittoHeaders) {
        final DittoHeaders dittoHeadersWithETagHeader = appendETagHeader(resource, dittoHeaders);
        return RetrieveResourceResponse.of(policyId, label, resource, dittoHeadersWithETagHeader);
    }

    public static ModifyPolicyResponse modifyPolicyResponse(final Policy policy, final DittoHeaders dittoHeaders,
            final boolean created) {
        final DittoHeaders dittoHeadersWithETagHeader = appendETagHeader(policy, dittoHeaders);
        if (created) {
            return ModifyPolicyResponse.created(policy.getEntityId().get(), policy, dittoHeadersWithETagHeader);
        } else {
            return ModifyPolicyResponse.modified(policy.getEntityId().get(), dittoHeadersWithETagHeader);
        }
    }

    public static RetrievePolicyResponse retrievePolicyResponse(final Policy policy, final DittoHeaders dittoHeaders) {
        final DittoHeaders dittoHeadersWithETagHeader = appendETagHeader(policy, dittoHeaders);
        return RetrievePolicyResponse.of(policy.getEntityId().get(), policy, dittoHeadersWithETagHeader);
    }

    public static ModifySubjectResponse modifySubjectResponse(final PolicyId policyId, final Label label,
            final Subject subject,
            final DittoHeaders dittoHeaders, final boolean created) {
        final DittoHeaders dittoHeadersWithETagHeader = appendETagHeader(subject, dittoHeaders);
        if (created) {
            return ModifySubjectResponse.created(policyId, label, subject, dittoHeadersWithETagHeader);
        } else {
            return ModifySubjectResponse.modified(policyId, label, subject.getId(), dittoHeadersWithETagHeader);
        }
    }

    public static RetrieveSubjectResponse retrieveSubjectResponse(final PolicyId policyId, final Label label,
            final Subject subject, final DittoHeaders dittoHeaders) {
        final DittoHeaders dittoHeadersWithETagHeader = appendETagHeader(subject, dittoHeaders);
        return RetrieveSubjectResponse.of(policyId, label, subject, dittoHeadersWithETagHeader);
    }

    private static DittoHeaders appendETagHeader(final Object object, final DittoHeaders dittoHeaders) {
        return dittoHeaders.toBuilder().eTag(EntityTag.fromEntity(object).get()).build();
    }
}
