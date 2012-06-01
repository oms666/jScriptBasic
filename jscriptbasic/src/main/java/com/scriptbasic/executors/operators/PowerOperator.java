package com.scriptbasic.executors.operators;

import com.scriptbasic.executors.BasicDoubleValue;
import com.scriptbasic.interfaces.BasicRuntimeException;
import com.scriptbasic.interfaces.RightValue;

public class PowerOperator extends AbstractBinaryFullCircuitFullDoubleOperator {

    protected RightValue operateOnDoubleDouble(Double a, Double b)
            throws BasicRuntimeException {
        return new BasicDoubleValue(Math.pow(a, b));
    }

    @Override
    protected String operatorName() {
        return "Power";
    }

}
