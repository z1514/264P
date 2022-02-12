/**
 * @(#)OverBookClassHandler.java
 *
 *
 */
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * One of the "Register a student for a course" command event handlers.
 * This will be handled at the beginning to check overbook course.
 */
public class OverBookClassHandler extends CommandEventHandler{

    /**
     * Construct "Overbook course check" event handler.
     *
     * @param objDataBase reference to the database object
     * @param iCommandEvCode command event code to receive the commands to process
     * @param iOutputEvCode output event code to send the command processing result
     */
    public OverBookClassHandler(DataBase objDataBase, int iCommandEvCode, int iOutputEvCode) {
        super(objDataBase, iCommandEvCode, iOutputEvCode);
    }

    /**
     * Process "Overbook courses" event.
     *
     * @param param a string parameter for command
     * @return a string result of command processing
     */
    @Override
    protected String execute(String param) {
        // Parse the parameters.
        StringTokenizer objTokenizer = new StringTokenizer(param);
        String sCID     = objTokenizer.nextToken();
        String sSection = objTokenizer.nextToken();
        // Get the course records.
        Course objCourse = this.objDataBase.getCourseRecord(sCID, sSection);
        if (objCourse == null) {
            return "";
        } //not found, return empty and this case will be dealt in conflict check and register handler afterwards
        if (objCourse.vRegistered.size()>3){
            return "This course is overbooked";
        } //overbooked, return result and registering students will be dealt by conflict check and register handler afterwards.

        //The detailed comments are in ClientInput event#6 case.
        return "";

    }
}
