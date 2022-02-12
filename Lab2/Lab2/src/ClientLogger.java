/**
 * @(#)ClientLogger.java
 *
 *
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * This class represents a client logger component which is responsible for storing text messages in
 * log files. Show events are expected to carry a
 * <code>String</code> object as its parameter that is to be stored. This component need to
 * subscribe to those events to receive them, which is done at the time of creation.
 *
 */
public class ClientLogger implements Observer {

    BufferedWriter logger;
    /**
     * Constructs a client logger component. A new client logger component subscribes to show events
     * at the time of creation. Since client output and client logger do the same things and just changed
     * output stream object, here we don't need to add a new event for logger. We can use show events to
     * notify logger to record output.
     */
    public ClientLogger(){
        EventBus.subscribeTo(EventBus.EV_SHOW, this);
        try{
            logger = new BufferedWriter(new FileWriter("system.log"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Event handler of this client logger component. On receiving a show event, the attached
     * <code>String</code> object is stored into the log file.
     *
     * @param event an event object. (caution: not to be directly referenced)
     * @param param a parameter object of the event. (to be cast to appropriate data type)
     */
    @Override
    public void update(Observable event, Object param) {
        try {
            logger.write((String) param+"\n");
            logger.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Close the writer before the program exits.
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        logger.close();
    }
}
