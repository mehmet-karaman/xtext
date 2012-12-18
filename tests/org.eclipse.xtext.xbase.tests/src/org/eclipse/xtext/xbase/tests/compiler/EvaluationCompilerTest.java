/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.tests.compiler;

import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.junit4.util.ParseHelper;
import org.eclipse.xtext.junit4.validation.ValidationTestHelper;
import org.eclipse.xtext.util.IResourceScopeCache;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.compiler.OnTheFlyJavaCompiler.EclipseRuntimeDependentJavaCompiler;
import org.eclipse.xtext.xbase.compiler.XbaseCompiler;
import org.eclipse.xtext.xbase.compiler.output.FakeTreeAppendable;
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable;
import org.eclipse.xtext.xbase.junit.evaluation.AbstractXbaseEvaluationTest;
import org.eclipse.xtext.xbase.lib.Functions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.tests.XbaseInjectorProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(XbaseInjectorProvider.class)
public class EvaluationCompilerTest extends AbstractXbaseEvaluationTest {
	
	@Inject
	private Provider<XbaseCompiler> compilerProvider;
	
	@Inject
	private ParseHelper<XExpression> parseHelper;
	
	@Inject
	private ValidationTestHelper validationHelper;

	@Inject
	private EclipseRuntimeDependentJavaCompiler javaCompiler;
	
	@Inject
	private TypeReferences typeReferences;
	
	@Inject
	private IResourceScopeCache cache;
	
	@Before
	public void setUp() throws Exception {
		javaCompiler.clearClassPath();
		javaCompiler.addClassPathOfClass(getClass());
		javaCompiler.addClassPathOfClass(AbstractXbaseEvaluationTest.class);
		javaCompiler.addClassPathOfClass(Functions.class);
		javaCompiler.addClassPathOfClass(Provider.class);
		javaCompiler.addClassPathOfClass(javax.inject.Provider.class);
		javaCompiler.addClassPathOfClass(Supplier.class);
	}

	@Override
	protected void assertEvaluatesTo(Object object, String string) {
		final String compileToJavaCode = compileToJavaCode(string);
		try {
			assertEquals("Java code was " + compileToJavaCode, object, compile(string).apply());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(compileToJavaCode);
			fail("Exception thrown " + e + ".Java code was " + compileToJavaCode);
		}
	}

	@Override
	protected void assertEvaluatesWithException(Class<? extends Throwable> class1, String string) {
		try {
			Function0<Object> compile = null;
			try {
				compile = compile(string);
			} catch (Exception e) {
				throw new WrappedException(e);
			}
			compile.apply();
			fail("expected exception " + class1.getCanonicalName() + ". Java code was " + compileToJavaCode(string));
		} catch (Throwable e) {
			if (!class1.isInstance(e))
				e.printStackTrace();
			assertTrue("expected " + class1.getCanonicalName() + " but was " + e, class1.isInstance(e));
		}
	}

	protected Functions.Function0<Object> compile(String code) {
		String javaCode = compileToJavaCode(code);
		try {
			Function0<Object> function = javaCompiler.createFunction(javaCode, Object.class);
			return function;
		} catch (Exception e) {
			throw new RuntimeException("Java compilation failed. Java code was : \n" + javaCode, e);
		}
	}
	
	protected String compileToJavaCode(String xtendCode) {
		XExpression model = null;
		ITreeAppendable appendable = new FakeTreeAppendable();
		try {
			model = expression(xtendCode, true);
			XbaseCompiler compiler = compilerProvider.get();
			compiler.compile(model, appendable, typeReferences.getTypeForName(Object.class, model));
		} catch (Exception e) {
			throw new RuntimeException("Xtend compilation failed", e);
		} finally {
			if (model != null)
				cache.clear(model.eResource());
		}
		return appendable.getContent();
	}

	protected XExpression expression(String string, boolean resolve) throws Exception {
		XExpression result = parseHelper.parse(string);
		if (resolve) {
			validationHelper.assertNoErrors(result);
		}
		return result;
	}
	
	@Override
	@Test
	@Ignore("Produces code that cannot be compiled with old type system")
	public void testBlock_01() throws Exception {
		super.testBlock_01();
	}
	
	@Override
	@Test
	@Ignore("Produces code that cannot be compiled with old type system")
	public void testBlock_02() throws Exception {
		super.testBlock_02();
	}

}
