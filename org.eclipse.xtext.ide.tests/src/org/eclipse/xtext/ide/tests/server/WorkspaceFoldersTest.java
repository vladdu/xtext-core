/**
 * Copyright (c) 2020 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.tests.server;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.UriExtensions;
import org.eclipse.xtext.ide.server.WorkspaceManager;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.util.Modules2;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Scopes;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
public class WorkspaceFoldersTest extends AbstractTestLangLanguageServerTest {
	@Inject
	private UriExtensions uriExtensions;

	@Inject
	private WorkspaceManager workspaceManager;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void testInitialize() throws Exception {
		File rootFolder1 = temporaryFolder.newFolder("root1");
		File rootFolder2 = temporaryFolder.newFolder("root2");
		writeFile(rootFolder1, "one.testlang", "type Foo { Bar bar }");
		String twoUri = writeFile(rootFolder2, "two.testlang", "type Bar { Foo foo }");
		initialize((InitializeParams it) -> {
			it.setWorkspaceFolders(
					Lists.newArrayList(new WorkspaceFolder(uriExtensions.toUriString(rootFolder1.toURI()), "root1"),
							new WorkspaceFolder(uriExtensions.toUriString(rootFolder2.toURI()), "root2")));
		});
		Assert.assertEquals(2, getDiagnostics().size());
		Assert.assertEquals(1, getDiagnostics().get(twoUri).size());
		withBuild(() -> {
			DidChangeWorkspaceFoldersParams didChangeWorkspaceFoldersParams = new DidChangeWorkspaceFoldersParams();
			WorkspaceFoldersChangeEvent workspaceFoldersChangeEvent = new WorkspaceFoldersChangeEvent();
			workspaceFoldersChangeEvent.setRemoved(
					Lists.newArrayList(new WorkspaceFolder(uriExtensions.toUriString(rootFolder2.toURI()), "root2")));
			didChangeWorkspaceFoldersParams.setEvent(workspaceFoldersChangeEvent);
			languageServer.didChangeWorkspaceFolders(didChangeWorkspaceFoldersParams);
		});
		Assert.assertEquals(0, getDiagnostics().get(twoUri).size());
		withBuild(() -> {
			DidChangeWorkspaceFoldersParams didChangeWorkspaceFoldersParams = new DidChangeWorkspaceFoldersParams();
			WorkspaceFoldersChangeEvent workspaceFoldersChangeEvent = new WorkspaceFoldersChangeEvent();
			workspaceFoldersChangeEvent.setAdded(
					Lists.newArrayList(new WorkspaceFolder(uriExtensions.toUriString(rootFolder2.toURI()), "root2")));
			didChangeWorkspaceFoldersParams.setEvent(workspaceFoldersChangeEvent);
			languageServer.didChangeWorkspaceFolders(didChangeWorkspaceFoldersParams);
		});
		Assert.assertEquals(1, getDiagnostics().get(twoUri).size());
	}

	protected void withBuild(Procedure0 lambda) throws Exception {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		workspaceManager.addBuildListener(new ILanguageServerAccess.IBuildListener() {
			@Override
			public void afterBuild(List<IResourceDescription.Delta> it) {
				workspaceManager.removeBuildListener(this);
				future.complete(null);
			}
		});
		lambda.apply();
		future.get();
	}

	public String writeFile(File root, String path, CharSequence contents) throws Exception {
		File file = new File(root, path);
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileWriter writer = new FileWriter(file);
		writer.write(contents.toString());
		writer.close();
		return uriExtensions.toUriString(file.toURI().normalize());
	}

	@Override
	protected com.google.inject.Module getServerModule() {
		com.google.inject.Module defaultModule = super.getServerModule();
		com.google.inject.Module customModule = new AbstractModule() {
			@Override
			protected void configure() {
				bind(WorkspaceManager.class).in(Scopes.SINGLETON);
			}
		};
		return Modules2.mixin(defaultModule, customModule);
	}
}
