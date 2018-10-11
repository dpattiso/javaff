package javaff.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javaff.data.GroundProblem;
import javaff.data.TimeStampedPlan;
import javaff.data.TotalOrderPlan;
import javaff.data.UngroundProblem;
import javaff.parser.PDDL21parser;
import javaff.parser.ParseException;
import javaff.parser.SolutionParser;
import javaff.scheduling.STRIPSScheduler;
import javaff.scheduling.SchedulingException;
import javaff.scheduling.STRIPSScheduler.PlanValidationResult;

public class SchedulerTester
{

	public static void main(String[] args)
	{
		
		File domain = new File(args[0]); 
		File pfile = new File(args[1]);
		File soln = new File(args[2]);
		
		UngroundProblem uproblem = PDDL21parser.parseFiles(domain, pfile);
		GroundProblem gproblem = uproblem.ground();
		
		TotalOrderPlan top = null;
		try
		{
			top = SolutionParser.parse(uproblem, soln);

			STRIPSScheduler scheduler = new STRIPSScheduler(gproblem.getSTRIPSInitialState());
			TimeStampedPlan tsp = scheduler.schedule(top);
			PlanValidationResult result = scheduler.validate(tsp, gproblem.getSTRIPSInitialState(), gproblem.getGoal());
			if (result != PlanValidationResult.Valid)
				throw new SchedulingException("Scheduled plan did not validate");
			
			
			tsp.print(System.out);
			
			File scheduled = new File(args[3]);

			scheduled.delete();
			scheduled.createNewFile();
			
			FileOutputStream outputStream = new FileOutputStream(scheduled);
			PrintWriter printWriter = new PrintWriter(outputStream);
			tsp.print(printWriter);
			
			printWriter.close();
			outputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		catch (SchedulingException e)
		{
			e.printStackTrace();
		}
	}
}
