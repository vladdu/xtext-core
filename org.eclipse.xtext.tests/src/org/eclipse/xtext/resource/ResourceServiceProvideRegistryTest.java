/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.resource;

import static org.junit.Assert.*;

import org.apache.log4j.Level;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.resource.impl.ResourceServiceProviderRegistryImpl;
import org.eclipse.xtext.testing.logging.LoggingTester;
import org.junit.Test;

/**
 * @author efftinge - Initial contribution and API
 */
public class ResourceServiceProvideRegistryTest {

	@Test
	public void testUninstallBadProvider() {
		final IResourceServiceProvider.Provider provider = new IResourceServiceProvider.Provider() {
			@Override
			public IResourceServiceProvider get(URI uri, String contentType) {
				throw new NullPointerException();
			}
		};
		
		final IResourceServiceProvider.Registry reg = new ResourceServiceProviderRegistryImpl();
		reg.getExtensionToFactoryMap().put("foo", provider);
		
		assertEquals(1, reg.getExtensionToFactoryMap().size());
		LoggingTester.captureLogging(Level.ERROR, ResourceServiceProviderRegistryImpl.class, new Runnable() {
			@Override
			public void run() {
				assertNull(reg.getResourceServiceProvider(URI.createURI("hubba.foo")));
			}
		}).assertLogEntry("Erroneous resource service provider registered for 'hubba.foo'. Removing it from the registry.");
		
		assertEquals(0, reg.getExtensionToFactoryMap().size());
	}
}
