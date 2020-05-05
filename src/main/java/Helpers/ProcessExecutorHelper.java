package Helpers;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessExecutorHelper {

	/**
	 * Common method. It creates a process builder with a list of commands and
	 * executes it. Each method must give appropriate commands depending on the
	 * functionality.
	 * 
	 * @param commands
	 *            List with commands to pass them as arguments to the process
	 *            builder
	 * @return answer List with inputStream and errorStream of the process builder
	 * @throws IOException
	 * @throws Exception
	 *             if it is not possible to generate the process builder
	 */
	public static List<String> executeProcess(List<String> commands,
											  String commandName) throws IOException, Exception {
		List<String> answer = new ArrayList<String>();
		ProcessBuilder pb = new ProcessBuilder(commands);


		Process spb = pb.start();
		String output = IOUtils.toString(spb.getInputStream(), "UTF-8");
		String err = IOUtils.toString(spb.getErrorStream(), "UTF-8");
		while(err.contains("null root node")) {
			pb = new ProcessBuilder(commands);
			spb = pb.start();
			output = IOUtils.toString(spb.getInputStream(), "UTF-8");
			err = IOUtils.toString(spb.getErrorStream(), "UTF-8");
		}
		answer.add(output);
		answer.add(err);
		answer.add(commandName);
		System.out.println("- - Executing command " + commandName + " - - \n");

		if (!err.equals("")) {
			throw new Exception(err);
		}

		if (!output.startsWith("<?xml")) {
			if (output.startsWith("adb: error") || output.contains("error") || output.contains("Failure")
					|| output.contains("Error")) {
				throw new Exception(output);
			}
		}
		return answer;
	}

	
}
