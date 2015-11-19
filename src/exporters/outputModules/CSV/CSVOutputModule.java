package exporters.outputModules.CSV;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import exporters.nodeStore.NodeTypes;
import exporters.outputModules.OutputModule;
import exporters.structures.BasicBlock;
import exporters.structures.DisassemblyLine;
import exporters.structures.Function;
import exporters.structures.FunctionContent;
import exporters.structures.Instruction;
import exporters.structures.VariableOrArgument;
import exporters.structures.edges.DirectedEdge;
import exporters.structures.edges.EdgeTypes;
import exporters.structures.edges.ResolvedCFGEdge;


public class CSVOutputModule implements OutputModule
{

	Function currentFunction = null;

	public void initialize(String outputDir)
	{
		CSVWriter.changeOutputDir(outputDir);
	}

	@Override
	public void clearCache()
	{
		CSVWriter.clear();
	}

	public void finish()
	{
		CSVWriter.finish();
	}

	@Override
	public void writeFunctionInfo(Function function)
	{
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(CSVFields.ADDR, function.getAddress().toString());
		properties.put(CSVFields.TYPE, function.getType());
		properties.put(CSVFields.REPR, function.getName());
		CSVWriter.addNode(function, properties);
	}

	public void writeFunctionContent(Function function)
	{
		setCurrentFunction(function);

		writeArgumentsAndVariables();
		writeBasicBlocks();
		writeCFGEdges();

		setCurrentFunction(null);
	}

	private void writeArgumentsAndVariables()
	{
		FunctionContent content = currentFunction.getContent();
		List<VariableOrArgument> varsAndArgs = content
				.getVariablesAndArguments();

		for (VariableOrArgument varOrArg : varsAndArgs)
		{
			createNodeForVarOrArg(varOrArg);
			connectNodeToFunction(varOrArg);
		}

	}

	private void connectNodeToFunction(VariableOrArgument varOrArg)
	{
		Function function = currentFunction;
		Long srcId = CSVWriter.getIdForNode(varOrArg);
		Long dstId = CSVWriter.getIdForNode(function);
		if (varOrArg.getType().equals(CSVFields.VAR))
			CSVWriter.addEdge(srcId, dstId, null, EdgeTypes.IS_VAR_OF);
		else
			CSVWriter.addEdge(srcId, dstId, null, EdgeTypes.IS_ARG_OF);
	}

	private void createNodeForVarOrArg(VariableOrArgument varOrArg)
	{
		Map<String, Object> properties = new HashMap<String, Object>();
		String type = varOrArg.getType();
		if (type.equals(CSVFields.VAR))
			properties.put(CSVFields.TYPE, NodeTypes.LOCAL_VAR);
		else
			properties.put(CSVFields.TYPE, NodeTypes.ARG);

		properties.put(CSVFields.NAME, varOrArg.getVarName());
		properties.put(CSVFields.REPR, varOrArg.getVarType());
		properties.put(CSVFields.CODE, varOrArg.getRegPlusOffset());

		// TODO: Watchout: We have not set the address on VariableOrArgument
		// nodes yet.
		CSVWriter.addNode(varOrArg, properties);
	}

	private void setCurrentFunction(Function function)
	{
		currentFunction = function;
	}

	private void writeBasicBlocks()
	{
		Function function = currentFunction;

		Collection<BasicBlock> basicBlocks = function.getContent()
				.getBasicBlocks();
		for (BasicBlock block : basicBlocks)
		{
			writeBasicBlock(block);
		}
	}

	public void writeBasicBlock(BasicBlock block)
	{
		writeNodeForBasicBlock(block);
		writeInstructions(block);
	}

	private void writeInstructions(BasicBlock block)
	{
		Collection<Instruction> instructions = block.getInstructions();
		Iterator<Instruction> it = instructions.iterator();

		int childNum = 0;
		while (it.hasNext())
		{
			Instruction instr = it.next();
			writeInstruction(block, instr, childNum);
			writeEdgeFromBlockToInstruction(block, instr);
			childNum++;
		}

	}

	private void writeEdgeFromBlockToInstruction(BasicBlock block,
			Instruction instr)
	{
		Map<String, Object> properties = new HashMap<String, Object>();
		long srcId = CSVWriter.getIdForNode(block);
		long dstId = CSVWriter.getIdForNode(instr);
		CSVWriter.addEdge(srcId, dstId, properties, EdgeTypes.IS_BB_OF);
	}

	private void writeInstruction(BasicBlock block, Instruction instr,
			int childNum)
	{
		Map<String, Object> properties = new HashMap<String, Object>();

		Long instrAddress = instr.getAddress();

		properties.put(CSVFields.ADDR, instrAddress.toString());
		properties.put(CSVFields.TYPE, instr.getType());
		properties.put(CSVFields.REPR, instr.getStringRepr());
		properties.put(CSVFields.CHILD_NUM, String.format("%d", childNum));

		addDisassemblyProperties(properties, instrAddress);

		CSVWriter.addNode(instr, properties);
	}

	private void addDisassemblyProperties(Map<String, Object> properties,
			Long address)
	{
		FunctionContent content = currentFunction.getContent();
		if (content == null)
			return;
		DisassemblyLine line = content.getDisassemblyLineForAddr(address);
		if (line == null)
			return;
		properties.put(CSVFields.CODE, line.getInstruction());
		properties.put(CSVFields.COMMENT, line.getComment());
	}

	private void writeNodeForBasicBlock(BasicBlock block)
	{
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(CSVFields.ADDR, block.getAddress().toString());
		properties.put(CSVFields.TYPE, block.getType());
		CSVWriter.addNode(block, properties);
	}

	private void writeCFGEdges()
	{
		Function function = currentFunction;
		List<ResolvedCFGEdge> edges = function.getContent().getEdges();
		for (ResolvedCFGEdge edge : edges)
		{
			BasicBlock from = edge.getFrom();
			BasicBlock to = edge.getTo();

			long srcId = CSVWriter.getIdForNode(from);
			long dstId = CSVWriter.getIdForNode(to);
			Map<String, Object> properties = new HashMap<String, Object>();
			String edgeType = edge.getType();
			CSVWriter.addEdge(srcId, dstId, properties, edgeType);
		}
	}

	@Override
	public void writeUnresolvedContentEdges(Function function)
	{
		FunctionContent content = function.getContent();

		List<DirectedEdge> edges = content.getUnresolvedEdges();
		for (DirectedEdge edge : edges)
		{
			writeUnresolvedEdge(edge);
		}

	}

	@Override
	public void writeReferencesToFunction(Function function)
	{
		List<DirectedEdge> edges = function.getUnresolvedEdges();
		for (DirectedEdge edge : edges)
		{
			writeUnresolvedEdge(edge);
		}
	}

	private void writeUnresolvedEdge(DirectedEdge edge)
	{
		String sourceKey = edge.getSourceNode().getKey();
		String destKey = edge.getDestNode().getKey();
		String type = edge.getType();
		Map<String, Object> properties = new HashMap<String, Object>();
		// TODO: add edge properties.
		CSVWriter.addUnresolvedEdge(sourceKey, destKey, properties, type);
	}

}