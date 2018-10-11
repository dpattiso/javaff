package javaff.test;

import javaff.JavaFF;

/**
 * Abstract class for running batch tests on a domain.
 * @author David Pattison
 *
 */
public abstract class BatchTester
{

	private static final String Usage = "BatchTester Usage: \"java test.BatchTester <input base path> <domain file name> <problem file template name> <start problem number>" +
			"<end problem number (inclusive)> <output base path>";

	/**
	 * Runs JavaFF across a batch of problems for a single domain. Solutions are printed to the
	 * specified directory and will have the name "pfileX.soln", where X is the problem number -- see below.
	 * 
	 *  
	 * @param args -- Of the form [0]=path to problem folder, [1]=name of domain file, 
	 * [2]=template name for problem files, where character 'X' will indicate the problem number, i.e.
	 * "pfileX.pddl", [3]=the start problem number, [4]=the end problem number (inclusive),
	 * [5]=the output directory for solution files.
	 */
	public static void main(String[] args)
	{
		String path = null, domain = null, pfileTemplate = null, outputBasePath = null;
		Integer pStart = -1, pEnd = -1;
		try
		{
			path = args[0];
			domain = args[1];
			pfileTemplate = args[2];
			pStart = Integer.parseInt(args[3]);
			pEnd = Integer.parseInt(args[4]);
			outputBasePath = args[5];
		}
		catch (Exception e)
		{
			System.out.println(BatchTester.Usage);
			System.exit(1);
		}
		
		String domainPath = path+"/"+domain;
		
		for (int i=pStart; i <= pEnd; i++)
		{
			String iStr = (i < 10) ? "0"+i : i+"";
			
			String pfile = pfileTemplate.replace("X", ""+iStr);
			String pfilePath = path+"/"+pfile;
			String solnPath = outputBasePath+"/"+pfile.replace(".pddl", "")+".soln";
			
			String[] javaFFArgs = new String[]{domainPath, pfilePath, solnPath};
			
			try
			{
				JavaFF.main(javaFFArgs);
			}
			catch (OutOfMemoryError e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				
			}
		}
	}
}
