package exporters.radare.inputModule;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exporters.radare.inputModule.exceptions.EmptyDisassembly;
import exporters.structures.annotations.VariableOrArgument;
import exporters.structures.interpretations.DisassembledFunction;
import exporters.structures.interpretations.DisassemblyLine;

public class RadareDisassemblyParser
{

	String lines[];
	int currentLine;

	static Pattern varAndArgPattern = Pattern
			.compile("^; (var|arg) (\\w+?) (\\w+?)[ ]+?@ (.*)$");

	static Pattern instructionPattern = Pattern
			.compile("0x(.*?)[ ]+?(.*?)( ;(.*))?$");

	/**
	 * Receives a disassembly for a function obtained via `pdf`,
	 * along with the corresponding start address of the function.
	 * @param functionAddr
	 * */

	public DisassembledFunction parseFunction(String disassembly, long functionAddr) throws EmptyDisassembly
	{
		DisassembledFunction retval = new DisassembledFunction();
		retval.setFuncAddress(functionAddr);

		initializeLines(disassembly);
		parseLines(retval);
		return retval;
	}

	public VariableOrArgument parseVarOrArg(Matcher matcher)
	{
		VariableOrArgument parsedVarOrArg = new VariableOrArgument();

		parsedVarOrArg.setType(matcher.group(1));
		parsedVarOrArg.setVarType(matcher.group(2));
		parsedVarOrArg.setName(matcher.group(3));
		parsedVarOrArg.setRegPlusOffset(matcher.group(4));

		return parsedVarOrArg;
	}

	/**
	 * Parses a single line of the disassembly output, which
	 * is expected to be of the format
	 * Address	Instruction	Comment
	 * */

	public DisassemblyLine parseInstruction(String line)
	{

		Matcher matcher = instructionPattern.matcher(line);
		if (!matcher.matches())
			return null;

		Long addr = new BigInteger(matcher.group(1), 16).longValue();
		String instruction = matcher.group(2);
		String comment = matcher.group(3);

		DisassemblyLine disasmLine = new DisassemblyLine();
		disasmLine.setAddr(addr);
		disasmLine.setInstruction(instruction.trim());
		if (comment != null)
			disasmLine.setComment(comment.trim());

		return disasmLine;
	}


	private void initializeLines(String disassembly) throws EmptyDisassembly
	{
		currentLine = 0;
		lines = disassembly.split("\\r?\\n");
		if (lines.length == 0)
			throw new EmptyDisassembly();
		// we skip the first line as it only contains the function name.
		skipLine();
	}

	private void parseLines(DisassembledFunction retval)
	{
		String line;
		while ((line = nextLine()) != null)
		{
			parseLine(retval, line);
		}
	}

	private void parseLine(DisassembledFunction retval, String line)
	{
		if (isLineInstruction(line)){
			DisassemblyLine disasmLine = parseInstruction(line);
			if(disasmLine != null)
				retval.addLine(disasmLine);
		}
		else if (isLineComment(line)){
			VariableOrArgument varOrArg = handleComment(line);
			if(varOrArg != null){
				varOrArg.setAddr(retval.getAddress());
				retval.addVarOrArg(varOrArg);
			}
		}
	}

	private boolean isLineInstruction(String line)
	{
		return line.startsWith("0x");
	}

	private boolean isLineComment(String line)
	{
		return line.startsWith(";");
	}

	private VariableOrArgument handleComment(String line)
	{
		Matcher matcher = varAndArgPattern.matcher(line);
		if (matcher.matches())
		{
			return parseVarOrArg(matcher);
		}
		return null;
	}

	private String nextLine()
	{
		if (currentLine == lines.length)
			return null;
		return lines[currentLine++].trim();
	}

	private void skipLine()
	{
		currentLine++;
	}

}
