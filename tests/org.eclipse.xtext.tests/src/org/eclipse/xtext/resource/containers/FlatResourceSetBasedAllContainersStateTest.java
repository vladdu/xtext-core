/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.resource.containers;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.junit4.util.ParseHelper;
import org.eclipse.xtext.resource.FlatResourceSetBasedAllContainersStateTestLanguageInjectorProvider;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.flatResourceSetBasedAllContainersStateTest.FlatResourceSetBasedAllContainersStateTestPackage;
import org.eclipse.xtext.resource.flatResourceSetBasedAllContainersStateTest.Model;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.scoping.impl.DefaultGlobalScopeProvider;
import org.eclipse.xtext.util.StringInputStream;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Joiner;
import com.google.inject.Inject;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(FlatResourceSetBasedAllContainersStateTestLanguageInjectorProvider.class)
public class FlatResourceSetBasedAllContainersStateTest {

	@Inject
	private IContainer.Manager containerManager;

	@Inject
	private IResourceDescription.Manager descriptionManager;

	@Inject
	private ResourceDescriptionsProvider descriptionsProvider;

	@Inject
	private ParseHelper<Model> parser;

	@Inject
	private DefaultGlobalScopeProvider scopeProvider;

	// we need a normalized URI
	protected URI computeUnusedUri(ResourceSet resourceSet) {
		String name = "__synthetic";
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			URI syntheticUri = URI.createURI(name + i + "." + parser.fileExtension);
			syntheticUri = resourceSet.getURIConverter().normalize(syntheticUri);
			if (resourceSet.getResource(syntheticUri, false) == null)
				return syntheticUri;
		}
		throw new IllegalStateException();
	}

	public Model parse(CharSequence text, ResourceSet resourceSetToUse) throws Exception {
		return parser.parse(text, computeUnusedUri(resourceSetToUse), resourceSetToUse);
	}

	@Test
	public void testContainerAddRemove() throws Exception {
		EReference ref = EcoreFactory.eINSTANCE.createEReference();
		EcoreFactory.eINSTANCE.createEClass().getEStructuralFeatures().add(ref);
		ref.setEType(FlatResourceSetBasedAllContainersStateTestPackage.Literals.MODEL);

		ResourceSet resourceSet = new XtextResourceSet();
		Resource res = parse("local", resourceSet).eResource();
		parse("other", resourceSet);
		IResourceDescription resourceDescription = descriptionManager.getResourceDescription(res);
		IResourceDescriptions resourceDescriptions = descriptionsProvider.getResourceDescriptions(res);
		List<IContainer> containers = containerManager.getVisibleContainers(resourceDescription, resourceDescriptions);
		assertEquals(1, containers.size());
		IContainer container = containers.get(0);

		assertEquals("local, other", Joiner.on(", ").join(container.getExportedObjects()));

		Resource foo = parse("foo", resourceSet).eResource();
		assertEquals("local, other, foo", Joiner.on(", ").join(container.getExportedObjects()));

		resourceSet.getResources().remove(foo);
		assertEquals("local, other", Joiner.on(", ").join(container.getExportedObjects()));
	}

	@Test
	public void testContainerLoadUnload() throws Exception {
		EReference ref = EcoreFactory.eINSTANCE.createEReference();
		EcoreFactory.eINSTANCE.createEClass().getEStructuralFeatures().add(ref);
		ref.setEType(FlatResourceSetBasedAllContainersStateTestPackage.Literals.MODEL);

		ResourceSet resourceSet = new XtextResourceSet();
		Resource res = parse("local", resourceSet).eResource();
		Resource foo = resourceSet.createResource(computeUnusedUri(resourceSet));
		IResourceDescription resourceDescription = descriptionManager.getResourceDescription(res);
		IResourceDescriptions resourceDescriptions = descriptionsProvider.getResourceDescriptions(res);
		List<IContainer> containers = containerManager.getVisibleContainers(resourceDescription, resourceDescriptions);
		assertEquals(1, containers.size());
		IContainer container = containers.get(0);

		//		assertEquals("local", Joiner.on(", ").join(container.getExportedObjects()));

		foo.load(new StringInputStream("foo"), null);
		assertEquals("foo, local", Joiner.on(", ").join(container.getExportedObjects()));

		foo.unload();
		assertEquals("local", Joiner.on(", ").join(container.getExportedObjects()));
	}

	@Test
	public void testGlobalScope() throws Exception {
		EReference ref = EcoreFactory.eINSTANCE.createEReference();
		EcoreFactory.eINSTANCE.createEClass().getEStructuralFeatures().add(ref);
		ref.setEType(FlatResourceSetBasedAllContainersStateTestPackage.Literals.MODEL);

		ResourceSet resourceSet = new XtextResourceSet();
		Resource res = parse("local", resourceSet).eResource();
		parse("other", resourceSet);
		assertEquals("other", Joiner.on(", ").join(scopeProvider.getScope(res, ref).getAllElements()));

		Resource foo = parse("foo", resourceSet).eResource();
		assertEquals("other, foo", Joiner.on(", ").join(scopeProvider.getScope(res, ref).getAllElements()));

		resourceSet.getResources().remove(foo);
		assertEquals("other", Joiner.on(", ").join(scopeProvider.getScope(res, ref).getAllElements()));
	}

}
