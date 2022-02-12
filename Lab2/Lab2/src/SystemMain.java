

/**
 * @(#)SystemMain.java
 *
 * Copyright: Copyright (c) 2003,2004 Carnegie Mellon University
 *
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class to hold system main method.
 */
class SystemMain {

	/**
	 * Creates components and starts the system. Two parameters are expected, the name of the file
	 * containing the student data and the name of the file containing the course data.
	 *
	 * @param args array of input parameters
	 */
	public static void main(String args[]) {
		String studentFileName, courseFileName;
		// Check the number of parameters.
		if (args.length == 2) {
			studentFileName = args[0];
			courseFileName = args[1];
		} else {
//			studentFileName = "bin/Students.txt";
//			courseFileName = "bin/Courses.txt";
			studentFileName = "Students.txt";
			courseFileName = "Courses.txt";
		}

		// Check if input files exists.
		if (new File(studentFileName).exists() == false) {
			System.err.println("Could not find " + studentFileName);
			System.exit(1);
		}
		if (new File(courseFileName).exists() == false) {
			System.err.println("Could not find " + courseFileName);
			System.exit(1);
		}

		// Initialize event bus.
		EventBus.initialize();

		// Create components.
		try {
			DataBase db;
			db = new DataBase(studentFileName, courseFileName);

			ListAllStudentsHandler objCommandEventHandler1 =
				new ListAllStudentsHandler(
					db,
					EventBus.EV_LIST_ALL_STUDENTS,
					EventBus.EV_SHOW);
			ListAllCoursesHandler objCommandEventHandler2 =
				new ListAllCoursesHandler(
					db,
					EventBus.EV_LIST_ALL_COURSES,
					EventBus.EV_SHOW);
			ListStudentsRegisteredHandler objCommandEventHandler3 =
				new ListStudentsRegisteredHandler(
					db,
					EventBus.EV_LIST_STUDENTS_REGISTERED,
					EventBus.EV_SHOW);
			ListCoursesRegisteredHandler objCommandEventHandler4 =
				new ListCoursesRegisteredHandler(
					db,
					EventBus.EV_LIST_COURSES_REGISTERED,
					EventBus.EV_SHOW);
			ListCoursesCompletedHandler objCommandEventHandler5 =
				new ListCoursesCompletedHandler(
					db,
					EventBus.EV_LIST_COURSES_COMPLETED,
					EventBus.EV_SHOW);
			/**
			 * The input event is modified to separate conflict check from registering student handler.
			 * Some codes of RegisterStudentHandler were changed so that RegisterStudentHandler to receive
			 * param and conflict flag from CourseConflictHandler to determine the output result for ClientOutput.
			 */
			RegisterStudentHandler objCommandEventHandler6 =
				new RegisterStudentHandler(
					db,
					EventBus.EV_REGISTER_STUDENT,
					EventBus.EV_SHOW);
			/**
			 * new added CourseConflictHandler
			 * It receives conflict check event and send the original param with conflict flag
			 * to RegisterStudentHandler by setting output event EV_REGISTER_STUDENT
			 */
			CourseConflictHandler objCommandEventHandler7 =
				new CourseConflictHandler(
						db,
						EventBus.EV_CONFLICT_CHECK,
						EventBus.EV_REGISTER_STUDENT);
			/**
			 * new added OverBookClassHandler
			 * It receives overbook check event and publish show event to ClientOutput.
			 * If the course is not overbooked, the param for output is empty.
			 * Else. the result will be "This course is overbooked"
			 */
			OverBookClassHandler objCommandEventHandler8 =
				new OverBookClassHandler(
						db,
						EventBus.EV_OVERBOOK_CHECK,
						EventBus.EV_SHOW);

			ClientInput objClientInput = new ClientInput();
			ClientOutput objClientOutput = new ClientOutput();
			ClientLogger objectClientLogger = new ClientLogger();

			// Start the system.
			objClientInput.start();
		} catch (FileNotFoundException e) {
			// Dump the exception information for debugging.
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			// Dump the exception information for debugging.
			e.printStackTrace();
			System.exit(1);
		}
	}


}
