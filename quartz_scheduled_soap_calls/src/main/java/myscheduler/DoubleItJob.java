package myscheduler;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.example.contract.doubleit.DoubleItPortType;
import org.example.contract.doubleit.DoubleItService;

public class DoubleItJob implements Job {

   @Override
   public void execute(JobExecutionContext context) throws JobExecutionException {
        DoubleItService service = new DoubleItService();
        DoubleItPortType port = service.getDoubleItPort();           
      
      try {
         // get value from Job element in schedule.xml
         JobDataMap dataMap = context.getMergedJobDataMap();
         Integer numToDoubleJD = dataMap.getIntegerFromString("JNumberToDouble");
         if (numToDoubleJD != null) {
             makeSOAPCall(port, numToDoubleJD);
         }
         
         // get value from Trigger element in schedule.xml
         Integer numToDoubleTrg = dataMap.getIntegerFromString("TNumberToDouble");
         if (numToDoubleTrg != null) {
             makeSOAPCall(port, numToDoubleTrg);
         }
      } catch (Exception e) {
         throw new JobExecutionException(e);
      }
   }

   private void makeSOAPCall (DoubleItPortType port, int numToDouble) throws Exception {
        int resp = port.doubleIt(numToDouble);
        System.out.println("The number " + numToDouble + " doubled is " + resp);
   }

}
