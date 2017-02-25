/*******************************************************************************
 * Copyright (c) 2017 TypeFox (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.generator.trace.node

import java.util.List
import org.eclipse.xtend.lib.annotations.Data
import org.eclipse.xtext.generator.trace.ILocationData

/**
 * @author Sven Efftinge - initial contribution and API
 */
@Data class TraceNode extends CompositeNode {
	
	final ILocationData sourceLocation
	final List<IGeneratorNode> children = newArrayList
	
}