/*
 * generated by Xtext
 */
package org.eclipse.xtext.example.css;

import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.util.IJvmTypeConformanceComputer;
import org.eclipse.xtext.xbase.typing.XbaseTypeProvider;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
public class XcssRuntimeModule extends org.eclipse.xtext.example.css.AbstractXcssRuntimeModule {
	public Class<? extends org.eclipse.xtext.typing.ITypeConformanceComputer<JvmTypeReference>> bindITypeService() {
		return IJvmTypeConformanceComputer.class;
	}
	
	public Class<? extends org.eclipse.xtext.typing.ITypeProvider<JvmTypeReference>> bindITypeProvider() {
		return XbaseTypeProvider.class;
	}
}
