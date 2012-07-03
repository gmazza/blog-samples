package service;

import java.util.List;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.example.schema.doubleit.DoubleIt;

public class ValueCheckInInterceptor extends AbstractPhaseInterceptor<Message> {

    public ValueCheckInInterceptor() {
        super(Phase.USER_LOGICAL);
    }

    @SuppressWarnings("unchecked")
    public void handleMessage(Message message) throws Fault {
        List<Object> myList = message.getContent(List.class);

        for (Object item : myList) {
            if (item instanceof DoubleIt) {
                DoubleIt req = (DoubleIt) item;
                if (req.getNumberToDouble() == 30) {
                    throw new Fault(
                       new Exception(
                          "Doubling 30 is not allowed by the web service provider."));
                }
            }
        }
    }
}

