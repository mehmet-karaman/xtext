package org.eclipse.xtext.xbase.tests.linking

import com.google.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.common.types.JvmFormalParameter
import org.eclipse.xtext.common.types.util.TypeReferences
import org.eclipse.xtext.junit.validation.ValidationTestHelper
import org.eclipse.xtext.xbase.XFeatureCall
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.eclipse.xtext.xbase.tests.AbstractXbaseTestCase

import static junit.framework.Assert.*

class JvmModelBasedLinkingTest extends AbstractXbaseTestCase {
	
	@Inject extension JvmTypesBuilder
	@Inject TypeReferences refs
	@Inject extension ValidationTestHelper
	
	def void testLinkToParameter() {
		val expr = expression("x", false)
		val resource = expr.eResource
		resource.eSetDeliver(false)
		resource.contents += expr.toClazz("Foo") [
			members += expr.toMethod("doStuff", expr.stringType) [
				parameters += expr.toParameter("x", expr.stringType)
				expr.associate(it)
				null as Void
			]
			null as Void
		]
		
		expr.assertNoErrors
	}
	
	def void testLinkToParameter_1() {
		val expr = expression("x", false) as XFeatureCall
		val resource = expr.eResource
		resource.eSetDeliver(false)
		resource.contents += expr.toClazz("Foo") [
			members += expr.toField("x", expr.stringType)
			members += expr.toMethod("doStuff", expr.stringType) [
				parameters += expr.toParameter("x", expr.stringType)
				expr.associate(it)
				null as Void
			]
			null as Void
		]
		expr.assertNoErrors
		assertTrue( expr.feature instanceof JvmFormalParameter)
	}
	
	def void testLinkToField() {
		val expr = expression("x", false) as XFeatureCall
		val resource = expr.eResource
		resource.eSetDeliver(false)
		resource.contents += expr.toClazz("Foo") [
			members += expr.toField("x", expr.stringType)
			members += expr.toMethod("doStuff", expr.stringType) [
				parameters += expr.toParameter("y", expr.stringType)
				expr.associate(it)
				null as Void
			]
			null as Void
		]
		expr.assertNoErrors
		assertTrue( expr.feature instanceof JvmField)
	}
	
	def void testLinkToField_1() {
		val expr = expression("x", false) as XFeatureCall
		val resource = expr.eResource
		resource.eSetDeliver(false)
		resource.contents += expr.toClazz("Foo") [
			members += expr.toField("x", expr.stringType)
			members += expr.toMethod("getX", expr.stringType) [
				expr.associate(it)
				null as Void
			]
			null as Void
		]
		expr.assertNoErrors
		assertTrue( expr.feature instanceof JvmField)
	}
	
	def stringType(EObject ctx) {
		refs.getTypeForName(typeof(String), ctx)
	}
}