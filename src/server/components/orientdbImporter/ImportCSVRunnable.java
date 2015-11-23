package server.components.orientdbImporter;

import java.io.IOException;

import server.DebugPrinter;

public class ImportCSVRunnable implements Runnable
{

	private final ImportJob importJob;

	public ImportCSVRunnable(ImportJob importJob)
	{
		this.importJob = importJob;
	}

	@Override
	public void run()
	{

		CSVImporter csvImporter = new CSVImporter();

		String nodeFilename = importJob.getNodeFilename();
		String edgeFilename = importJob.getEdgeFilename();
		String dbName = importJob.getDbName();

		try
		{
			csvImporter.setDatabase(dbName);
			csvImporter.importCSVFiles(nodeFilename, edgeFilename);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		DebugPrinter.print("Import finished", this);
	}

}