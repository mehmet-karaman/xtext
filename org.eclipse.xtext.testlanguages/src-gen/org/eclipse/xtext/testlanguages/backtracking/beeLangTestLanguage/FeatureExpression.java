/**
 * Copyright (c) 2010, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.testlanguages.backtracking.beeLangTestLanguage;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Feature Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.xtext.testlanguages.backtracking.beeLangTestLanguage.FeatureExpression#getObjExpr <em>Obj Expr</em>}</li>
 *   <li>{@link org.eclipse.xtext.testlanguages.backtracking.beeLangTestLanguage.FeatureExpression#getFeatureName <em>Feature Name</em>}</li>
 * </ul>
 *
 * @see org.eclipse.xtext.testlanguages.backtracking.beeLangTestLanguage.BeeLangTestLanguagePackage#getFeatureExpression()
 * @model
 * @generated
 */
public interface FeatureExpression extends Expression
{
  /**
   * Returns the value of the '<em><b>Obj Expr</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the value of the '<em>Obj Expr</em>' containment reference.
   * @see #setObjExpr(Expression)
   * @see org.eclipse.xtext.testlanguages.backtracking.beeLangTestLanguage.BeeLangTestLanguagePackage#getFeatureExpression_ObjExpr()
   * @model containment="true"
   * @generated
   */
  Expression getObjExpr();

  /**
   * Sets the value of the '{@link org.eclipse.xtext.testlanguages.backtracking.beeLangTestLanguage.FeatureExpression#getObjExpr <em>Obj Expr</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Obj Expr</em>' containment reference.
   * @see #getObjExpr()
   * @generated
   */
  void setObjExpr(Expression value);

  /**
   * Returns the value of the '<em><b>Feature Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the value of the '<em>Feature Name</em>' attribute.
   * @see #setFeatureName(String)
   * @see org.eclipse.xtext.testlanguages.backtracking.beeLangTestLanguage.BeeLangTestLanguagePackage#getFeatureExpression_FeatureName()
   * @model
   * @generated
   */
  String getFeatureName();

  /**
   * Sets the value of the '{@link org.eclipse.xtext.testlanguages.backtracking.beeLangTestLanguage.FeatureExpression#getFeatureName <em>Feature Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Feature Name</em>' attribute.
   * @see #getFeatureName()
   * @generated
   */
  void setFeatureName(String value);

} // FeatureExpression