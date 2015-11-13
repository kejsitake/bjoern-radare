package tools.bjoernImport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.ParseException;

public class BjoernImport {

	static CommandLineInterface cmdLine = new CommandLineInterface();

	public static void main(String[] args) throws MalformedURLException {
		parseCommandLine(args);
		invokeImportPlugin();
	}

	private static void invokeImportPlugin() throws MalformedURLException {

		String pathToBinary = cmdLine.getCodedir();
		pathToBinary = pathToBinary.replace("/", "|");

		try {
			URL url = new URL("http://localhost:2480/importcode/"
					+ pathToBinary);
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setReadTimeout(0);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				System.out.println("Hello " + line);
			}
			rd.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void parseCommandLine(String[] args) {
		try {
			cmdLine.parseCommandLine(args);
		} catch (RuntimeException | ParseException e) {
			printHelpAndTerminate(e);
		}
	}

	private static void printHelpAndTerminate(Exception e) {
		System.err.println(e.getMessage());
		cmdLine.printHelp();
		System.exit(0);
	}

}
