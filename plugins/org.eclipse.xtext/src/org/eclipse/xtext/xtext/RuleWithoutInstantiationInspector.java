/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xtext;

import java.util.List;

import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.Alternatives;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Group;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.UnorderedGroup;
import org.eclipse.xtext.XtextPackage;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class RuleWithoutInstantiationInspector extends XtextRuleInspector<Boolean, ParserRule> {

	public RuleWithoutInstantiationInspector(ValidationMessageAcceptor acceptor) {
		super(acceptor);
	}
	
	@Override
	protected boolean canInspect(ParserRule rule) {
		// special treatment of first rule
		if (GrammarUtil.getGrammar(rule).getRules().get(0) == rule)
			return false;
		if (GrammarUtil.isDatatypeRule(rule) || rule.getAlternatives() == null)
			return false;
		return super.canInspect(rule);
	}
	
	@Override
	protected void handleResult(Boolean r, ParserRule rule) {
		if (!r.booleanValue())
			acceptWarning("The rule '" + rule.getName() + "' may be consumed without object instantiation. " +
					"Add an action to ensure object creation, e.g. '{" + getTypeRefName(rule.getType()) + "}'." , 
					rule, XtextPackage.ABSTRACT_RULE__NAME);
	}

	@Override
	public Boolean caseGroup(Group object) {
		return caseGroupOrUnorderedGroup(object, object.getTokens());
	}
	
	@Override
	public Boolean caseUnorderedGroup(UnorderedGroup object) {
		return caseGroupOrUnorderedGroup(object, object.getElements());
	}
	
	public Boolean caseGroupOrUnorderedGroup(AbstractElement object, List<AbstractElement> elements) {
		if (GrammarUtil.isOptionalCardinality(object))
			return Boolean.FALSE;
		for(AbstractElement element: elements) {
			if (doSwitch(element))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	@Override
	public Boolean caseAbstractElement(AbstractElement object) {
		return Boolean.FALSE;
	}
	
	@Override
	public Boolean caseAction(Action object) {
		if (GrammarUtil.isOptionalCardinality(object))
			return Boolean.FALSE;
		return Boolean.TRUE;
	}
	
	@Override
	public Boolean caseAlternatives(Alternatives object) {
		if (GrammarUtil.isOptionalCardinality(object))
			return Boolean.FALSE;
		for(AbstractElement element: object.getGroups()) {
			if (!doSwitch(element))
				return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	@Override
	public Boolean caseAssignment(Assignment object) {
		if (GrammarUtil.isOptionalCardinality(object))
			return Boolean.FALSE;
		return Boolean.TRUE;
	}
	
	@Override
	public Boolean caseRuleCall(RuleCall object) {
		if (GrammarUtil.isOptionalCardinality(object))
			return Boolean.FALSE;
		return object.getRule() == null || (object.getRule() instanceof ParserRule && !GrammarUtil.isDatatypeRule((ParserRule) object.getRule()));
	}

}
