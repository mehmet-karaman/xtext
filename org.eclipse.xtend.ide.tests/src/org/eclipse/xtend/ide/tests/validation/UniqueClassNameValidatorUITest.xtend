/*******************************************************************************
 * Copyright (c) 2015, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.validation

import java.util.Map
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IWorkspaceRunnable
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase
import org.eclipse.xtext.builder.impl.XtextBuilder
import org.eclipse.xtext.util.StringInputStream
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.eclipse.xtend.ide.tests.WorkbenchTestHelper.*
import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.*

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
class UniqueClassNameValidatorUITest extends AbstractXtendUITestCase {

	IProject first
	IProject second

	@Test def void testTwoXtendFilesSameProject() {
		val firstFile = createFile('first.p384008/src/acme/A.xtend',
			'''
				package acme
				class A {
				}
			''')
		val secondFile = createFile('first.p384008/src/acme/B.xtend',
			'''
				package acme
				class A {
				}
			''')
		runInWorkspace [
			first.incrementalBuild(XtextBuilder.BUILDER_ID)
		]
		val firstFileMarkers = firstFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)
		assertEquals(printMarker(firstFileMarkers), 1, firstFileMarkers.length)
		assertEquals('The type A is already defined in B.xtend.', firstFileMarkers.head.getAttribute(IMarker.MESSAGE))
		val secondFileMarkers = secondFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).onlyErrors
		assertEquals(printMarker(secondFileMarkers), 1, secondFileMarkers.length)
		assertEquals('The type A is already defined in A.xtend.', secondFileMarkers.head.getAttribute(IMarker.MESSAGE))
	}
	
	@Test def void testTwoXtendFilesDifferentProject() {
		val firstFile = createFile('first.p384008/src/acme/A.xtend',
			'''
				package acme
				class A {
				}
			''')
		val secondFile = createFile('second.p384008/src/acme/B.xtend',
			'''
				package acme
				class A {
				}
			''')
		runInWorkspace [
			first.incrementalBuild(XtextBuilder.BUILDER_ID)
		]
		val firstFileMarkers = firstFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)
		assertEquals(printMarker(firstFileMarkers), 0, firstFileMarkers.length)
		val secondFileMarkers = secondFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)
		assertEquals(printMarker(secondFileMarkers), 0, secondFileMarkers.length)
	}
	
	@Test def void testXtendAndJavaSameProject() {
		createFile('first.p384008/src/acme/A.java',
			'''
				package acme;
				public class A {
				}
			''')
		val secondFile = createFile('first.p384008/src/acme/B.xtend',
			'''
				package acme
				class A {
				}
			''')
		runInWorkspace [
			first.incrementalBuild(XtextBuilder.BUILDER_ID)
			first.incrementalBuild(JavaCore.BUILDER_ID)
		]
		val secondFileMarkers = secondFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).onlyErrors
		assertEquals(printMarker(secondFileMarkers), 1, secondFileMarkers.length)
		assertEquals('The type A is already defined in A.java.', secondFileMarkers.head.getAttribute(IMarker.MESSAGE))
	}
	
	@Test def void testXtendAndJavaDifferentProject() {
		createFile('first.p384008/src/acme/A.java',
			'''
				package acme;
				public class A {
				}
			''')
		val secondFile = createFile('second.p384008/src/acme/B.xtend',
			'''
				package acme
				class A {
				}
			''')
		runInWorkspace [
			first.incrementalBuild(XtextBuilder.BUILDER_ID)
			first.incrementalBuild(JavaCore.BUILDER_ID)
		]
		val secondFileMarkers = secondFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)
		assertEquals(printMarker(secondFileMarkers), 0, secondFileMarkers.length)
	}
	
	@Ignore("Since the name acme.A is considered to be derived, it is filtered from the Java delta")
	@Test def void testXtendAndJavaSameProjectXtendFirst() {
		val firstFile = createFile('first.p384008/src/acme/B.xtend',
			'''
				package acme
				class A {
				}
			''')
		first.incrementalBuild(XtextBuilder.BUILDER_ID)
		val javaFile = createFile('first.p384008/src/acme/A.java',
			'''
				package acme;
				class A2 {
				}
			''')
		
		first.incrementalBuild(JavaCore.BUILDER_ID)
		first.incrementalBuild(XtextBuilder.BUILDER_ID)
		javaFile.setContents(new StringInputStream("package acme; class A{}"), false, false, null)
		first.incrementalBuild(JavaCore.BUILDER_ID)
		first.incrementalBuild(XtextBuilder.BUILDER_ID)
		val markers = firstFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)
		assertEquals(printMarker(markers), 1, markers.length)
		assertEquals('The type A is already defined in A.java.', markers.head.getAttribute(IMarker.MESSAGE))
	}
	
	def static private Map<String,String> emptyStringMap(){
		return <String,String>emptyMap
	}
	
	def onlyErrors(Iterable<IMarker> markers) {
		return markers.filter[
			IMarker.SEVERITY_ERROR == it.getAttribute(IMarker.SEVERITY)
		]
	}
	
	@Before override setUp() throws Exception {
		super.setUp()
		first = createPluginProject('first.p384008')
		second = createPluginProject('second.p384008')
		setReference(second, first)
	}
	
	@After override tearDown() throws Exception {
		deleteProject(first)
		deleteProject(second)
	}
	
	private def runInWorkspace(IWorkspaceRunnable r) {
		ResourcesPlugin.workspace.run(r, new NullProgressMonitor)
	}

	private def incrementalBuild(IProject project, String builderName) {
		waitForJdtIndex
		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, builderName, emptyStringMap, null)
	}
}