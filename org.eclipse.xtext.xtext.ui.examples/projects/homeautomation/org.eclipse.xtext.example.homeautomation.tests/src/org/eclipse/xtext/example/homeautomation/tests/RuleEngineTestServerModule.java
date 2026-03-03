/*******************************************************************************
 * Copyright (c) 2026 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mehmet Karaman (mehmet.karaman@advantest.com) - initial Rule Engine Example lsp server module for testing
 *******************************************************************************/
package org.eclipse.xtext.example.homeautomation.tests;

import com.google.inject.AbstractModule;
import java.util.concurrent.ExecutorService;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.xtext.ide.ExecutorServiceProvider;
import org.eclipse.xtext.ide.server.LanguageServerImpl;
import org.eclipse.xtext.ide.server.IMultiRootWorkspaceConfigFactory;
import org.eclipse.xtext.ide.server.MultiRootWorkspaceConfigFactory;
import org.eclipse.xtext.ide.server.IProjectDescriptionFactory;
import org.eclipse.xtext.ide.server.DefaultProjectDescriptionFactory;
import org.eclipse.xtext.ide.server.concurrent.IRequestManager;
import org.eclipse.xtext.ide.server.concurrent.RequestManager;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.containers.ProjectDescriptionBasedContainerManager;

/**
 * Custom ServerModule for testing that uses the global IResourceServiceProvider.Registry.INSTANCE
 * instead of creating a new registry via ServiceLoader. This ensures that languages registered
 * in tests are available to the LanguageServer.
 */
public class RuleEngineTestServerModule extends AbstractModule {

	@Override
	protected void configure() {
		bindExecutorService();
		bindLanguageServer();
		bindWorkspaceFactories();
		bindContainerManager();
		bindRequestManager();
		bindResourceRegistry();
	}

	protected void bindExecutorService() {
		binder().bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class);
	}

	protected void bindLanguageServer() {
		bind(LanguageServer.class).to(LanguageServerImpl.class);
	}

	protected void bindResourceRegistry() {
		// Use the global IResourceServiceProvider registry instance
		// This is crucial - all lookups to the injected registry will use the global INSTANCE
		// which should have been populated by RuleEngineStandaloneSetup before this injector was created
		IResourceServiceProvider.Registry globalRegistry = IResourceServiceProvider.Registry.INSTANCE;
		System.err.println("DEBUG: RuleEngineTestServerModule binding registry instance. Registry has " 
			+ globalRegistry.getExtensionToFactoryMap().size() + " extensions registered");
		bind(IResourceServiceProvider.Registry.class).toInstance(globalRegistry);
		
	}

	protected void bindWorkspaceFactories() {
		bind(IMultiRootWorkspaceConfigFactory.class).to(MultiRootWorkspaceConfigFactory.class);
		bind(IProjectDescriptionFactory.class).to(DefaultProjectDescriptionFactory.class);
	}

	protected void bindContainerManager() {
		bind(IContainer.Manager.class).to(ProjectDescriptionBasedContainerManager.class);
	}

	protected void bindRequestManager() {
		bind(IRequestManager.class).to(RequestManager.class);
	}
	
}

