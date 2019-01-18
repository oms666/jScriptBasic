package com.scriptbasic.executors;

import com.scriptbasic.api.ScriptBasicException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Verhas
 * date June 30, 2012
 */

public class TestMethodRegistry {

    @Test
    public void returnsNullForNonRegisteredMethod() throws ScriptBasicException {
        final var mr = new BasicMethodRegistry();
        assertNull(mr.getJavaMethod(Object.class, "alias"));
    }

    @Test
    public void registersMethods() throws ScriptBasicException {
        final var mr = new BasicMethodRegistry();
        mr.registerJavaMethod("alias", Object.class, "noSuchMethod",
                new Class[]{Object.class});
        mr.registerJavaMethod("sinus", java.lang.Math.class, "sin",
                new Class<?>[]{double.class});
        @SuppressWarnings("unused") final Method m = mr.getJavaMethod(java.lang.Math.class, "sinus");
    }

}
