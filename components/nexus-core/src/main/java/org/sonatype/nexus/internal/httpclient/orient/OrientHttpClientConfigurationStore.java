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
package org.sonatype.nexus.internal.httpclient.orient;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.app.FeatureFlag;
import org.sonatype.nexus.common.app.ManagedLifecycle;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.httpclient.config.HttpClientConfiguration;
import org.sonatype.nexus.internal.httpclient.HttpClientConfigurationStore;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.DatabaseInstanceNames;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.app.ManagedLifecycle.Phase.SCHEMAS;
import static org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport.State.STARTED;
import static org.sonatype.nexus.orient.transaction.OrientTransactional.inTx;
import static org.sonatype.nexus.orient.transaction.OrientTransactional.inTxRetry;

/**
 * Orient {@link HttpClientConfigurationStore}.
 *
 * @since 3.0
 */
@FeatureFlag(name = "nexus.orient.store.config")
@Named("orient")
@ManagedLifecycle(phase = SCHEMAS)
@Singleton
public class OrientHttpClientConfigurationStore
  extends StateGuardLifecycleSupport
  implements HttpClientConfigurationStore
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final OrientHttpClientConfigurationEntityAdapter entityAdapter;

  @Inject
  public OrientHttpClientConfigurationStore(@Named(DatabaseInstanceNames.CONFIG) final Provider<DatabaseInstance> databaseInstance,
                                            final OrientHttpClientConfigurationEntityAdapter entityAdapter)
  {
    this.databaseInstance = checkNotNull(databaseInstance);
    this.entityAdapter = checkNotNull(entityAdapter);
  }

  @Override
  protected void doStart() {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      entityAdapter.register(db);
    }
  }

  @Override
  @Nullable
  @Guarded(by = STARTED)
  public HttpClientConfiguration load() {
    return inTx(databaseInstance).call(entityAdapter::get);
  }

  @Override
  @Guarded(by = STARTED)
  public void save(final HttpClientConfiguration configuration) {
    checkArgument(configuration instanceof OrientHttpClientConfiguration,
        "HttpClientConfiguration does not match backing store");
    inTxRetry(databaseInstance).run(db -> entityAdapter.set(db, (OrientHttpClientConfiguration)configuration));
  }

  @Override
  public HttpClientConfiguration newConfiguration() {
    return new OrientHttpClientConfiguration();
  }
}
