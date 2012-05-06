package service;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService
public interface DoubleItPortType {
   public int doubleIt(int numberToDouble);
}

