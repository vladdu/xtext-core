/*******************************************************************************
 * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.xtext.parsetree.reconstr;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.generator.tests.AbstractGeneratorTest;
import org.eclipse.xtext.testlanguages.SimpleExpressions;
import org.eclipse.xtext.testlanguages.SimpleExpressionsParseTreeConstructor;

public class SimpleReconstrTest extends AbstractGeneratorTest {
	public void testSimple() throws Exception {
		with(getClass());
		String model = "( a b ) !";
		assertEquals(model,parseAndSerialize(model));
	}
	
	public void testComplex() throws Exception {
		with(getClass());
		String model = "( ( a b ) ! c d e f ( x s ) ( ( a b ) ! c ) ! ) !";
		assertEquals(model,parseAndSerialize(model));
	}

	private String parseAndSerialize(String model) throws Exception {
		EObject result = (EObject) getModel(model);
		SimpleReconstrTestParseTreeConstructor con = new SimpleReconstrTestParseTreeConstructor();
		con.update(result);
		String resultString = con.getText();
		return resultString;
	}
	
	public void testSimpleExpressions() throws Exception {
		with(SimpleExpressions.class);
		String model = "a + b - c * d / e";
		EObject result = (EObject) getModel(model);
		SimpleExpressionsParseTreeConstructor con = new SimpleExpressionsParseTreeConstructor();
		con.update(result);
		String resultString = con.getText();
		assertEquals(model,resultString);
	}
}
