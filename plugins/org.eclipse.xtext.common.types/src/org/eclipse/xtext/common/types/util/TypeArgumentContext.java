/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.common.types.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.common.types.JvmAnyTypeReference;
import org.eclipse.xtext.common.types.JvmGenericArrayTypeReference;
import org.eclipse.xtext.common.types.JvmLowerBound;
import org.eclipse.xtext.common.types.JvmMultiTypeReference;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmSynonymTypeReference;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeConstraint;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmUnknownTypeReference;
import org.eclipse.xtext.common.types.JvmUpperBound;
import org.eclipse.xtext.common.types.JvmWildcardTypeReference;
import org.eclipse.xtext.common.types.TypesFactory;

import com.google.common.collect.Lists;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class TypeArgumentContext implements ITypeArgumentContext {
	
	private final Map<JvmTypeParameter, JvmTypeReference> boundParameters;
	private boolean forcedRawType;
	
	private final TypeReferences typeReferences;
	private final TypesFactory typesFactory;
	private final IRawTypeHelper rawTypeHelper;

	public TypeArgumentContext(
			Map<JvmTypeParameter, JvmTypeReference> boundParameters, 
			TypeReferences typeReferences,
			TypesFactory typesFactory,
			IRawTypeHelper rawTypeHelper) {
		this.boundParameters = boundParameters;
		this.typeReferences = typeReferences;
		this.typesFactory = typesFactory;
		this.rawTypeHelper = rawTypeHelper;
	}
	
	public JvmTypeReference getBoundArgument(JvmTypeParameter parameter) {
		if (isRawTypeContext()) {
			JvmParameterizedTypeReference demandCreated = typesFactory.createJvmParameterizedTypeReference();
			demandCreated.setType(parameter);
			JvmTypeReference result = rawTypeHelper.getRawTypeReference(demandCreated, parameter.eResource());
			return result;
		}
		JvmTypeReference result = boundParameters.get(parameter);
		if (result == null) {
			JvmParameterizedTypeReference demandCreated = typesFactory.createJvmParameterizedTypeReference();
			demandCreated.setType(parameter);
			return demandCreated;
		}
		return result;
	}
	
	protected Collection<JvmTypeParameter> getBoundParameters() {
		if (isRawTypeContext())
			return Collections.emptyList();
		return boundParameters.keySet();
	}
	
	protected JvmTypeReference internalGetBoundArgument(JvmTypeParameter parameter) {
		return boundParameters.get(parameter);
	}
	
	protected abstract class CopyingTypeReferenceVisitor extends AbstractTypeReferenceVisitorWithParameter.InheritanceAware<Boolean, JvmTypeReference> {
		@Override
		protected JvmTypeReference handleNullReference(Boolean parameter) {
			return null;
		}
		@Override
		public JvmTypeReference doVisitParameterizedTypeReference(JvmParameterizedTypeReference reference, Boolean replaceWildcards) {
			JvmType type = reference.getType();
			if (type instanceof JvmTypeParameter) {
				if (isRawTypeContext() || boundParameters.containsKey(type)) {
					JvmTypeReference bound = getBoundArgument((JvmTypeParameter) type);
					if (bound.getType() == type) {
						// TODO find the reason for this recursion
//						System.out.println("Recursion2");
						return bound;
					}
					return visit(bound, replaceWildcards);
				}
			}
			JvmParameterizedTypeReference result = typesFactory.createJvmParameterizedTypeReference();
			result.setType(type);
			if (!isRawTypeContext()) {
				for(JvmTypeReference argument: reference.getArguments()) {
					result.getArguments().add(visit(argument, replaceWildcards));
				}
			}
			return result;
		}
		
		@Override
		public JvmTypeReference doVisitWildcardTypeReference(JvmWildcardTypeReference reference, Boolean replaceWildcards) {
			JvmWildcardTypeReference result = typesFactory.createJvmWildcardTypeReference();
			for(JvmTypeConstraint constraint: reference.getConstraints()) {
				JvmTypeReference bound = visit(constraint.getTypeReference(), replaceWildcards);
				if (bound instanceof JvmWildcardTypeReference) {
					result.getConstraints().addAll(((JvmWildcardTypeReference) bound).getConstraints());
				} else {
					JvmTypeConstraint copiedConstraint = (JvmTypeConstraint) EcoreUtil.create(constraint.eClass());
					copiedConstraint.setTypeReference(bound);
					result.getConstraints().add(copiedConstraint);
				}
			}
			return result;
		}
		
		@Override
		public JvmTypeReference doVisitGenericArrayTypeReference(JvmGenericArrayTypeReference reference, Boolean replaceWildcards) {
			JvmTypeReference copiedComponent = visit(reference.getComponentType(), replaceWildcards);
			JvmGenericArrayTypeReference result = typesFactory.createJvmGenericArrayTypeReference();
			result.setComponentType(copiedComponent);
			return result;
		}
		@Override
		public JvmTypeReference doVisitAnyTypeReference(JvmAnyTypeReference reference, Boolean replaceWildcards) {
			return typesFactory.createJvmAnyTypeReference();
		}
		
		@Override
		public JvmTypeReference doVisitMultiTypeReference(JvmMultiTypeReference reference, Boolean replaceWildcards) {
			JvmMultiTypeReference result = typesFactory.createJvmMultiTypeReference();
			for(JvmTypeReference component: reference.getReferences()) {
				result.getReferences().add(visit(component, replaceWildcards));
			}
			return result;
		}

		@Override
		public JvmTypeReference doVisitSynonymTypeReference(JvmSynonymTypeReference reference, Boolean replaceWildcards) {
			JvmSynonymTypeReference result = typesFactory.createJvmSynonymTypeReference();
			for(JvmTypeReference component: reference.getReferences()) {
				result.getReferences().add(visit(component, replaceWildcards));
			}
			return result;
		}

		@Override
		public JvmTypeReference doVisitUnknownTypeReference(JvmUnknownTypeReference reference, Boolean replaceWildcards) {
			return typesFactory.createJvmUnknownTypeReference();
		}
	}
	
	public JvmTypeReference getLowerBound(JvmTypeReference element) {
		JvmTypeReference result = new CopyingTypeReferenceVisitor() {
			@Override
			public JvmTypeReference doVisitParameterizedTypeReference(JvmParameterizedTypeReference reference, Boolean replaceWildcards) {
				if (replaceWildcards) {
					JvmType type = reference.getType();
					if (type instanceof JvmTypeParameter) {
						if (isRawTypeContext() || boundParameters.containsKey(type)) {
							JvmTypeReference bound = getBoundArgument((JvmTypeParameter) type);
							if (bound.getType() == type) {
								// TODO find the reason for this recursion
//								System.out.println("Recursion2");
								return bound;
							}
							return visit(bound, replaceWildcards);
						}
					}
					JvmParameterizedTypeReference result = typesFactory.createJvmParameterizedTypeReference();
					result.setType(type);
					if (!isRawTypeContext()) {
						for(JvmTypeReference argument: reference.getArguments()) {
							result.getArguments().add(visit(argument, Boolean.FALSE));
						}
					}
					return result;
				} else {
					return super.doVisitParameterizedTypeReference(reference, replaceWildcards);
				}
			}
			
			@Override
			public JvmTypeReference doVisitWildcardTypeReference(JvmWildcardTypeReference reference, Boolean replaceWildcards) {
				if (replaceWildcards) {
					for(JvmTypeConstraint constraint: reference.getConstraints()) {
						if (constraint instanceof JvmLowerBound) {
							return visit(constraint.getTypeReference(), false);
						}
					}
					return typesFactory.createJvmAnyTypeReference();
				} else {
					JvmWildcardTypeReference result = typesFactory.createJvmWildcardTypeReference();
					for(JvmTypeConstraint constraint: reference.getConstraints()) {
						if (constraint.getTypeReference() != null) {
							JvmTypeReference bound = visit(constraint.getTypeReference(), replaceWildcards);
							if (bound instanceof JvmWildcardTypeReference) {
								if (constraint instanceof JvmUpperBound) {
									for(JvmTypeConstraint newConstraint: ((JvmWildcardTypeReference) bound).getConstraints()) {
										if (newConstraint instanceof JvmUpperBound) {
											result.getConstraints().add(newConstraint);
										} else {
											JvmUpperBound upperBound = typesFactory.createJvmUpperBound();
											upperBound.setTypeReference(newConstraint.getTypeReference());
											result.getConstraints().add(upperBound);
										}
									}
								} else {
									result.getConstraints().addAll(((JvmWildcardTypeReference) bound).getConstraints());
								}
							} else {
								JvmTypeConstraint copiedConstraint = (JvmTypeConstraint) EcoreUtil.create(constraint.eClass());
								copiedConstraint.setTypeReference(bound);
								result.getConstraints().add(copiedConstraint);
							}
						}
					}
					return result;
				}
			}
		}.visit(element, true);
		return result;
	}
	
	public JvmTypeReference getUpperBound(JvmTypeReference element, final Notifier context) {
		JvmTypeReference result = new CopyingTypeReferenceVisitor() {
			@Override
			public JvmTypeReference doVisitParameterizedTypeReference(JvmParameterizedTypeReference reference, Boolean replaceWildcards) {
				if (replaceWildcards) {
					JvmType type = reference.getType();
					if (type instanceof JvmTypeParameter) {
						if (isRawTypeContext() || boundParameters.containsKey(type)) {
							JvmTypeReference bound = getBoundArgument((JvmTypeParameter) type);
							if (bound.getType() == type) {
								// TODO find the reason for this recursion
//								System.out.println("Recursion2");
								return bound;
							}
							return visit(bound, replaceWildcards);
						}
					}
					JvmParameterizedTypeReference result = typesFactory.createJvmParameterizedTypeReference();
					result.setType(type);
					if (!isRawTypeContext()) {
						List<JvmTypeReference> copiedArguments = Lists.newArrayListWithCapacity(reference.getArguments().size());
						boolean wasNull = false;
						for(JvmTypeReference argument: reference.getArguments()) {
							JvmTypeReference copiedReference = visit(argument, Boolean.FALSE);
							if (copiedReference == null) {
								wasNull = true;
								break;
							}
							copiedArguments.add(copiedReference);
						}
						if (!wasNull && !copiedArguments.isEmpty())
							result.getArguments().addAll(copiedArguments);
					}
					return result;
				} else {
					return super.doVisitParameterizedTypeReference(reference, replaceWildcards);
				}
			}
			
			@Override
			public JvmTypeReference doVisitWildcardTypeReference(JvmWildcardTypeReference reference, Boolean replaceWildcards) {
				if (replaceWildcards) {
					List<JvmTypeReference> upperBounds = Lists.newArrayList();
					for(JvmTypeConstraint constraint: reference.getConstraints()) {
						if (constraint instanceof JvmUpperBound) {
							upperBounds.add(visit(constraint.getTypeReference(), false));
						}
					}
					if (upperBounds.isEmpty()) {
						JvmTypeReference result = typeReferences.getTypeForName("java.lang.Object", context);
						return result;
					} else if (upperBounds.size() == 1) {
						return upperBounds.get(0);
					} else {
						JvmMultiTypeReference result = typesFactory.createJvmMultiTypeReference();
						result.getReferences().addAll(upperBounds);
						return result;
					}
				} else {
					return super.doVisitWildcardTypeReference(reference, replaceWildcards);
				}
			}
		}.visit(element, true);
		return result;
	}
	
	public JvmTypeReference resolve(JvmTypeReference element) {
		JvmTypeReference result = new CopyingTypeReferenceVisitor() {
		}.visit(element, false);
		return result;
	}
	
	public boolean isRawTypeContext() {
		return boundParameters == null || forcedRawType;
	}
	
	@Override
	public String toString() {
		return "TypeArgumentContext [boundParameters=" + boundParameters + ", forcedRawType="
				+ forcedRawType + "]";
	}

}
