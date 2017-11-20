/**
 * 
 */
package com.scriptbasic.executors;

import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.scriptbasic.errors.BasicInterpreterInternalError;
import com.scriptbasic.executors.commands.CommandSub;
import com.scriptbasic.hooks.NullHook;
import com.scriptbasic.interfaces.BasicRuntimeException;
import com.scriptbasic.interfaces.BuildableProgram;
import com.scriptbasic.interfaces.Command;
import com.scriptbasic.interfaces.Configuration;
import com.scriptbasic.interfaces.ExecutionException;
import com.scriptbasic.interfaces.ExtendedInterpreter;
import com.scriptbasic.interfaces.Factory;
import com.scriptbasic.interfaces.HierarchicalVariableMap;
import com.scriptbasic.interfaces.InterpreterHook;
import com.scriptbasic.interfaces.MethodRegistry;
import com.scriptbasic.interfaces.RightValue;
import com.scriptbasic.memory.MixedBasicVariableMap;
import com.scriptbasic.utility.CastUtility;
import com.scriptbasic.utility.ExpressionUtility;
import com.scriptbasic.utility.FactoryUtility;
import com.scriptbasic.utility.HookRegisterUtility;
import com.scriptbasic.utility.MethodRegisterUtility;
import com.scriptbasic.utility.RightValueUtility;
import com.scriptbasic.utility.functions.BasicRuntimeFunctionRegisterer;

/**
 * @author Peter Verhas date June 22, 2012
 * 
 */
public final class BasicExtendedInterpreter implements ExtendedInterpreter {

	private BuildableProgram program;
	private java.io.Reader reader;
	private java.io.Writer writer;
	private java.io.Writer errorWriter;
	private InterpreterHook hook = null;
	private InterpreterHook hookedHook = null;

	@Override
	public InterpreterHook getHook() {
		return hook;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.ExtendedInterpreter#disableHook()
	 */
	@Override
	public void disableHook() {
		if (hook != null) {
			hookedHook = hook;
			hook = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.ExtendedInterpreter#enableHook()
	 */
	@Override
	public void enableHook() {
		if (hookedHook != null) {
			hook = hookedHook;
			hookedHook = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.Interpreter#registerHook(com.scriptbasic.
	 * interfaces.InterpreterHook)
	 */
	@Override
	public void registerHook(final InterpreterHook hook) {
		hook.setNext(this.hook);
		hook.setInterpreter(this);
		this.hook = hook;
	}

	/**
	 * @return the reader
	 */
	@Override
	public java.io.Reader getReader() {
		return reader;
	}

	/**
	 * @param reader
	 *            the reader to set
	 */
	@Override
	public void setReader(final java.io.Reader reader) {
		this.reader = reader;
	}

	/**
	 * @return the writer
	 */
	@Override
	public java.io.Writer getWriter() {
		return writer;
	}

	/**
	 * @param writer
	 *            the writer to set
	 */
	@Override
	public void setWriter(final java.io.Writer writer) {
		this.writer = writer;
	}

	/**
	 * @return the errorWriter
	 */
	@Override
	public Writer getErrorWriter() {
		return errorWriter;
	}

	/**
	 * @param errorWriter
	 *            the errorWriter to set
	 */
	@Override
	public void setErrorWriter(final Writer errorWriter) {
		this.errorWriter = errorWriter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.scriptbasic.interfaces.Interpreter#setProgram(com.scriptbasic.interfaces
	 * .BuildableProgram)
	 */
	@Override
	public void setProgram(final BuildableProgram buildableProgram) {
		this.program = buildableProgram;
	}

	private final MixedBasicVariableMap variables = new MixedBasicVariableMap();

	@Override
	public HierarchicalVariableMap getVariables() {
		return variables;
	}

	private Command nextCommand;
	private Command currentCommand;

	@Override
	public CommandSub getSubroutine(final String name) {
		final Command command = program.getNamedCommand(name);
		if (command instanceof CommandSub) {
			return (CommandSub) command;
		}
		return null;
	}

	@Override
	public void registerFunctions(final Class<?> klass) {
		try {
			MethodRegisterUtility.registerFunctions(klass, this);
		} catch (BasicRuntimeException e) {
			throw new BasicInterpreterInternalError(
					"Registering functions from class '"
							+ klass
							+ "' caused exception. Probably double defining a function alias. "
							+ "Since this declared in Java code and not in BASIC this is an internal error of "
							+ "the embedding application. For more detail have a look at the exception that caused this.",
					e);
		}
	}

	private boolean executePreTask = true;

	private void preExecuteTask() throws ExecutionException {
		if (executePreTask) {
			if (program == null) {
				throw new BasicRuntimeException("Program code was not loaded");
			}
			BasicRuntimeFunctionRegisterer.registerBasicRuntimeFunctions(this);
			registerHook(new NullHook());
			HookRegisterUtility.registerHooks(this);
			if (hook != null) {
				hook.init();
			}
			executePreTask = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.Interpreter#execute()
	 */
	@Override
	public void execute() throws ExecutionException {
		preExecuteTask();
		final Command command = program.getStartCommand();
		execute(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.scriptbasic.interfaces.ExtendedInterpreter#execute(com.scriptbasic
	 * .interfaces.Command)
	 */
	@Override
	public void execute(final Command startCommand) throws ExecutionException {
		preExecuteTask();
		Command command = startCommand;
		while (command != null) {
			nextCommand = command.getNextCommand();
			currentCommand = command;
			if (hook != null) {
				hook.beforeExecute(command);
			}
			command.checkedExecute(this);
			if (hook != null) {
				hook.afterExecute(command);
			}
			currentCommand = null;
			command = nextCommand;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.Interpreter#setVariable(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void setVariable(final String name, final Object value)
			throws ExecutionException {
		final RightValue rightValue = RightValueUtility.createRightValue(value);
		getVariables().setVariable(name, rightValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.Interpreter#getVariable(java.lang.String)
	 */
	@Override
	public Object getVariable(final String name) throws ExecutionException {
		return CastUtility.toObject(getVariables().getVariableValue(name));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.Interpreter#call(java.lang.String,
	 * java.lang.Object[])
	 */
	@Override
	public Object call(final String functionName, final Object[] arguments)
			throws ExecutionException {
		preExecuteTask();
		final CommandSub commandSub = getSubroutine(functionName);
		if (commandSub == null) {
			throw new BasicRuntimeException("There is no such subroutine '"
					+ functionName + "'");
		}
		return CastUtility.toObject(ExpressionUtility.callBasicFunction(this,
				RightValueUtility.createRightValues(arguments), commandSub,
				functionName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.ExtendedInterpreter#getProgram()
	 */
	@Override
	public BuildableProgram getProgram() {
		return this.program;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.scriptbasic.interfaces.ExtendedInterpreter#delayedSetProgramCounter
	 * (java.lang.Integer)
	 */
	@Override
	public void setNextCommand(final Command nextCommand) {
		this.nextCommand = nextCommand;
	}

	@Override
	public Command getCurrentCommand() {
		return currentCommand;
	}

	private final Map<String, Object> interpreterStateMap = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.ExtendedInterpreter#getMap()
	 */
	@Override
	public Map<String, Object> getMap() {
		return interpreterStateMap;
	}

	private Factory factory;

	/**
	 * @return the factory
	 */
	public Factory getFactory() {
		return factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.scriptbasic.interfaces.FactoryManaged#setFactory(com.scriptbasic.
	 * interfaces.Factory)
	 */
	@Override
	public void setFactory(final Factory factory) {
		this.factory = factory;
	}

	private final Map<String, Class<?>> useMap = new HashMap<String, Class<?>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.ExtendedInterpreter#getUseMap()
	 */
	@Override
	public Map<String, Class<?>> getUseMap() {
		return useMap;
	}

	private final MethodRegistry basicMethodRegistry = new BasicMethodRegistry();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.scriptbasic.interfaces.ExtendedInterpreter#getJavaMethod(java.lang
	 * .Class, java.lang.String)
	 */
	@Override
	public Method getJavaMethod(final Class<?> klass, final String methodName)
			throws ExecutionException {
		return basicMethodRegistry.getJavaMethod(klass, methodName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.scriptbasic.interfaces.ExtendedInterpreter#registerJavaMethod(java
	 * .lang.String, java.lang.Class, java.lang.String, java.lang.Class<?>[])
	 */
	@Override
	public void registerJavaMethod(final String alias, final Class<?> klass,
			final String methodName, final Class<?>[] argumentTypes)
			throws BasicRuntimeException {
		if (hook != null) {
			hook.beforeRegisteringJavaMethod(alias, klass, methodName,
					argumentTypes);
		}
		basicMethodRegistry.registerJavaMethod(alias, klass, methodName,
				argumentTypes);

	}

	private final Stack<Command> commandStack = new Stack<Command>();
	private final Stack<Command> nextCommandStack = new Stack<Command>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.ExtendedInterpreter#push(com.scriptbasic.
	 * interfaces.Command)
	 */
	@Override
	public void push(final Command command) {
		if (hook != null) {
			hook.beforePush(command);
		}
		commandStack.push(command);
		nextCommandStack.push(nextCommand);
		getVariables().newFrame();
		if (hook != null) {
			hook.afterPush(command);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.ExtendedInterpreter#push()
	 */
	public void push() {
		push(currentCommand);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.ExtendedInterpreter#pop()
	 */
	@Override
	public Command pop() {
		if (hook != null) {
			hook.beforePop();
		}
		getVariables().dropFrame();
		nextCommand = nextCommandStack.pop();
		final Command command = commandStack.pop();
		if (hook != null) {
			hook.afterPop(command);
		}
		return command;
	}

	private RightValue returnValue;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.scriptbasic.interfaces.ExtendedInterpreter#setReturnValue(com.scriptbasic
	 * .interfaces.RightValue)
	 */
	@Override
	public void setReturnValue(final RightValue returnValue) {
		this.returnValue = returnValue;
		if (hook != null) {
			hook.setReturnValue(returnValue);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.ExtendedInterpreter#getReturnValue()
	 */
	@Override
	public RightValue getReturnValue() {
		return returnValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.scriptbasic.interfaces.ExtendedInterpreter#getConfiguration()
	 */
	@Override
	public Configuration getConfiguration() {
		return FactoryUtility.getConfiguration(getFactory());
	}

}
