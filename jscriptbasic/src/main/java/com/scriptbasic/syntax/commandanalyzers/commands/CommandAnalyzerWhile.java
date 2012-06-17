package com.scriptbasic.syntax.commandanalyzers.commands;

import com.scriptbasic.exceptions.AnalysisException;
import com.scriptbasic.executors.commands.CommandWhile;
import com.scriptbasic.interfaces.Command;
import com.scriptbasic.interfaces.Expression;
import com.scriptbasic.syntax.commandanalyzers.AbstractCommandAnalyzer;

public class CommandAnalyzerWhile extends AbstractCommandAnalyzer {

    @Override
    public Command analyze() throws AnalysisException {
        CommandWhile node = new CommandWhile();
        Expression condition = analyzeExpression();
        assertThereAreNoSuperflouosCharactersOnTheLine();
        node.setCondition(condition);
        pushNodeOnTheAnalysisStack(node);
        return node;
    }

    @Override
    protected String getName() {
        return "WHILE";
    }
}
