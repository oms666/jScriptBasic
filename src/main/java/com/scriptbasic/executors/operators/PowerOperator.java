package com.scriptbasic.executors.operators;

import com.scriptbasic.executors.rightvalues.BasicDoubleValue;
import com.scriptbasic.spi.RightValue;

public class PowerOperator extends AbstractBinaryFullCircuitFullDoubleOperator {

    @Override
    protected RightValue operateOnDoubleDouble(final Double a, final Double b) {
        return new BasicDoubleValue(Math.pow(a, b));
    }

    @Override
    protected String operatorName() {
        return "Power";
    }

}
