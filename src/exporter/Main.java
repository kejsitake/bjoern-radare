package exporter;

import inputModules.InputModule;
import inputModules.radare.RadareInputModule;

import java.util.List;

import nodeStore.NodeStore;
import outputModules.CSV.CSVOutputModule;
import structures.Function;

public class Main
{

	static InputModule inputModule = new RadareInputModule();
	static CSVOutputModule outputModule = new CSVOutputModule();

	static List<Function> functions;

	public static void main(String[] args)
	{

		inputModule.initialize("/bin/ls");
		outputModule.initialize();

		loadAndOutputFunctionInfo();
		loadAndOutputFunctionContent();

		outputModule.finish();
	}

	private static void loadAndOutputFunctionInfo()
	{
		functions = inputModule.getFunctions();
		for (Function function : functions)
		{
			outputModule.writeFunctionInfo(function);
		}
	}

	private static void loadAndOutputFunctionContent()
	{
		for (Function function : functions)
		{
			clearCaches();
			inputModule.initializeFunctionContents(function);
			processFunction(function);
		}

	}

	private static void clearCaches()
	{
		NodeStore.clearCache();
		outputModule.clearCache();
	}

	private static void processFunction(Function function)
	{
		if (function == null)
			return;

		outputModule.writeFunctionContent(function);
		outputModule.writeUnresolvedContentEdges(function);

		// we clear the function content after writing it to free up some
		// memory.
		function.deleteContent();
	}

}