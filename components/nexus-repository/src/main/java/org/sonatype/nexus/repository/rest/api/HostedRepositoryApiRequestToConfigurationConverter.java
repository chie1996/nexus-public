/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.rest.api;

import javax.inject.Named;

import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.rest.api.model.HostedRepositoryApiRequest;

import com.google.common.collect.Sets;

import static org.sonatype.nexus.repository.storage.StorageFacetConstants.BLOB_STORE_NAME;
import static org.sonatype.nexus.repository.storage.StorageFacetConstants.STORAGE;
import static org.sonatype.nexus.repository.storage.StorageFacetConstants.STRICT_CONTENT_TYPE_VALIDATION;
import static org.sonatype.nexus.repository.storage.StorageFacetConstants.WRITE_POLICY;

/**
 * @since 3.20
 */
@Named
public class HostedRepositoryApiRequestToConfigurationConverter<T extends HostedRepositoryApiRequest>
    extends AbstractRepositoryApiRequestToConfigurationConverter<T>
{
  public Configuration convert(final T request) {
    Configuration configuration = super.convert(request);
    configuration.attributes(STORAGE).set(BLOB_STORE_NAME, request.getStorage().getBlobStoreName());
    configuration.attributes(STORAGE)
        .set(STRICT_CONTENT_TYPE_VALIDATION, request.getStorage().getStrictContentTypeValidation());
    configuration.attributes(STORAGE).set(WRITE_POLICY, request.getStorage().getWritePolicy());
    configuration.attributes("cleanup")
        .set("policyName",
            request.getCleanup() != null ? Sets.newHashSet(request.getCleanup().getPolicyNames()) : null);
    return configuration;
  }
}
