package exporters.nodeStore;

public class Node
{

	public static final Long INVALID_ADDRESS = Long.MAX_VALUE;

	private Boolean isPermanent = false; // feature currently not used.
	private long address;
	private String type;

	public Boolean isPermanent()
	{
		return isPermanent;
	}

	public void setIsPermanent(Boolean isPermanent)
	{
		this.isPermanent = isPermanent;
	}

	public void setAddr(long addr)
	{
		address = addr;
	}

	public Long getAddress()
	{
		return address;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getKey()
	{
		return getType() + "_" + getAddress().toString();
	}

}