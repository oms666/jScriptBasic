package com.scriptbasic.syntax.expression;

import com.scriptbasic.exceptions.AnalysisException;
import com.scriptbasic.executors.GenericExpressionList;
import com.scriptbasic.interfaces.Expression;
import com.scriptbasic.interfaces.ExpressionAnalyzer;
import com.scriptbasic.interfaces.ExpressionList;
import com.scriptbasic.interfaces.ExpressionListAnalyzer;
import com.scriptbasic.interfaces.Factory;
import com.scriptbasic.syntax.AbstractGenericListAnalyzer;
import com.scriptbasic.utility.FactoryUtilities;

public class BasicExpressionListAnalyzer
		extends
		AbstractGenericListAnalyzer<ExpressionList, GenericExpressionList, Expression, ExpressionAnalyzer> implements ExpressionListAnalyzer {
	private Factory factory;

	private BasicExpressionListAnalyzer() {
	}

	@Override
	public Factory getFactory() {
		return factory;
	}

	@Override
	public void setFactory(Factory factory) {
		this.factory = factory;
	}

	@Override
	public ExpressionList analyze() throws AnalysisException {
		setList(new GenericExpressionList());
		setAnalyzer(FactoryUtilities.getExpressionAnalyzer(getFactory()));
		return super.analyze();
	}

}