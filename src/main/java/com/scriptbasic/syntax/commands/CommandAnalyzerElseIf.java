package com.scriptbasic.syntax.commands;

import com.scriptbasic.context.Context;
import com.scriptbasic.executors.commands.CommandElseIf;
import com.scriptbasic.interfaces.AnalysisException;
import com.scriptbasic.interfaces.Expression;
import com.scriptbasic.spi.Command;

/**
 * @author Peter Verhas
 * date June 16, 2012
 */
public class CommandAnalyzerElseIf extends AbstractCommandAnalyzerIfKind {

    public CommandAnalyzerElseIf(final Context ctx) {
        super(ctx);
    }

    protected Command createNode(final Expression condition) throws AnalysisException {
        final var node = new CommandElseIf();
        node.setCondition(condition);
        registerAndSwapNode(node);
        return node;
    }

}
