/**
 * @(#)CourseConflictHandler.java
 *
 *
 */
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * "Course conflict" event handler.
 */
public class CourseConflictHandler extends CommandEventHandler{
    /**
     * Construct "Course Conflict Check" event handler.
     *
     * @param objDataBase reference to the database object
     * @param iCommandEvCode command event code to receive the commands to process
     * @param iOutputEvCode output event code to send the command processing result
     */
    public CourseConflictHandler(DataBase objDataBase, int iCommandEvCode, int iOutputEvCode) {
        super(objDataBase, iCommandEvCode, iOutputEvCode);
    }

    /**
     * Process "Course Conflict Check" event.
     *
     * @param param a string parameter for command
     * @return a string result of command processing
     */
    @Override
    protected String execute(String param) {
        // Parse the parameters.
        /**
         * This part will receive sSID, sCID and sSection to check whether the courses are in conflicts.
         */
        StringTokenizer objTokenizer = new StringTokenizer(param);
        String sSID     = objTokenizer.nextToken();
        String sCID     = objTokenizer.nextToken();
        String sSection = objTokenizer.nextToken();
        // Get the student and course records.
        Student objStudent = this.objDataBase.getStudentRecord(sSID);
        Course objCourse = this.objDataBase.getCourseRecord(sCID, sSection);
        /**
         * For null course or student, checking conflicts is meaningless.
         * So we set flag to noConflict and publish register event to register handler.
         * The register handler will deal with null cases.
         */
        if (objStudent == null) {
            return param+" noConflict"; //a new token for conflict flag is added and publish to register handler
        }
        if (objCourse == null) {
            return param+" noConflict";
        }

        // Check if the given course conflicts with any of the courses the student has registered.
        ArrayList vCourse = objStudent.getRegisteredCourses();
        for (int i=0; i<vCourse.size(); i++) {
            if (((Course) vCourse.get(i)).conflicts(objCourse)) {
                return param+" conflict"; //a new token for conflict flag is added and publish to register handler
            }
        }

        return param+" noConflict"; //if all cases pass, it means no conflicts.
    }
}
