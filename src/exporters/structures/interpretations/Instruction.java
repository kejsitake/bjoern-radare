package exporters.structures.interpretations;

import exporters.nodeStore.Node;
import exporters.nodeStore.NodeTypes;

public class Instruction extends Node
{
	private String stringRepr;
	private String bytes;

	public Instruction()
	{
		this.setType(NodeTypes.INSTRUCTION);
	}

	public String getStringRepr()
	{
		return stringRepr;
	}

	public void setStringRepr(String stringRepr)
	{
		this.stringRepr = stringRepr;
	}

	public Object getBytes()
	{
		return this.bytes;
	}

	public void setBytes(String bytes)
	{
		this.bytes = bytes;
	}

}
