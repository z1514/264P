/******************************************************************************************************************
 * File:SinkFilter.java
 * Project: Lab 1
 * Copyright:
 *   Copyright (c) 2020 University of California, Irvine
 *   Copyright (c) 2003 Carnegie Mellon University
 * Versions:
 *   1.1 January 2020 - Revision for SWE 264P: Distributed Software Architecture, Winter 2020, UC Irvine.
 *   1.0 November 2008 - Sample Pipe and Filter code (ajl).
 *
 * Description:
 * This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
 * filter reads some input from the filter's input port and does the following:
 *	1) It parses the input stream and "decommutates" the measurement ID
 *	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
 * This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
 * the stream: namely time (long type) and measurements (double type).
 * Parameters: None
 * Internal Methods: None
 ******************************************************************************************************************/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;                        // This class is used to interpret time words
import java.text.SimpleDateFormat;        // This class is used to format and write time in a string format.


/**
 * Some codes are added to receive signal from MiddleFilter. In this way the SinkFilter will know whether
 * to add a * after altitude.
 */
public class SinkFilter extends FilterFramework {
    public void run() {
        /************************************************************************************
         *	TimeStamp is used to compute time using java.util's Calendar class.
         * 	TimeStampFormat is used to format the time value so that it can be easily printed
         *	to the terminal.
         *************************************************************************************/
        try {
            Calendar TimeStamp = Calendar.getInstance();
            SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy MM dd::hh:mm:ss:SSS");
            SimpleDateFormat format = new SimpleDateFormat("yyyy:DD:HH:mm:ss");
            DecimalFormat df = new DecimalFormat("#.00000");
            df.setRoundingMode(RoundingMode.FLOOR);

            int MeasurementLength = 8;        // This is the length of all measurements (including time) in bytes
            int IdLength = 4;                // This is the length of IDs in the byte stream
            byte databyte = 0;                // This is the data byte read from the stream
            int bytesread = 0;                // This is the number of bytes read from the stream
            long measurement;                // This is the word used to store all measurements - conversions are illustrated.
            int id;                            // This is the measurement id
            int i;                            // This is a loop counter

            /**
             * A byte to record whether the altitude changed
             * A writer to OutputB.csv
             */
            byte changed = 0;               //This is the signal if the altitude changes
            BufferedWriter writer = new BufferedWriter(new FileWriter("OutputB.csv"));      //write the bytes into result
            writer.write("Time,Velocity,Altitude,Pressure,Temperature\n");


            // First we announce to the world that we are alive...
            System.out.print("\n" + this.getName() + "::Sink Reading ");

            while (true) {
                try {
                    /***************************************************************************
                     // We know that the first data coming to this filter is going to be an ID and
                     // that it is IdLength long. So we first get the ID bytes.
                     ****************************************************************************/
                    id = 0;
                    for (i = 0; i < IdLength; i++) {
                        databyte = ReadFilterInputPort();    // This is where we read the byte from the stream...
                        id = id | (databyte & 0xFF);        // We append the byte on to ID...
                        if (i != IdLength - 1)                // If this is not the last byte, then slide the
                        {                                    // previously appended byte to the left by one byte
                            id = id << 8;                    // to make room for the next byte we append to the ID
                        }
                        bytesread++;                        // Increment the byte count
                    }

                    /****************************************************************************
                     // Here we read measurements. All measurement data is read as a stream of bytes
                     // and stored as a long value. This permits us to do bitwise manipulation that
                     // is neccesary to convert the byte stream into data words. Note that bitwise
                     // manipulation is not permitted on any kind of floating point types in Java.
                     // If the id = 0 then this is a time value and is therefore a long value - no
                     // problem. However, if the id is something other than 0, then the bits in the
                     // long value is really of type double and we need to convert the value using
                     // Double.longBitsToDouble(long val) to do the conversion which is illustrated below.
                     *****************************************************************************/
                    measurement = 0;
                    for (i = 0; i < MeasurementLength; i++) {
                        databyte = ReadFilterInputPort();
                        measurement = measurement | (databyte & 0xFF);    // We append the byte on to measurement...
                        if (i != MeasurementLength - 1)                    // If this is not the last byte, then slide the
                        {                                                // previously appended byte to the left by one byte
                            measurement = measurement << 8;                // to make room for the next byte we append to the
                        }
                        bytesread++;                                    // Increment the byte count

                    }

                    /**
                     * This code is added to receive the signal, the frame now is 61 bytes.
                     * And the signal byte is just after the altitude id and altitude data.
                     * So if id == 2, we can receive it after dealing with id and measurement.
                     */
                    if (id==2){
                        changed = ReadFilterInputPort();
                        bytesread++;
                    }

                    /****************************************************************************
                     // Here we look for an ID of 0 which indicates this is a time measurement.
                     // Every frame begins with an ID of 0, followed by a time stamp which correlates
                     // to the time that each proceeding measurement was recorded. Time is stored
                     // in milliseconds since Epoch. This allows us to use Java's calendar class to
                     // retrieve time and also use text format classes to format the output into
                     // a form humans can read. So this provides great flexibility in terms of
                     // dealing with time arithmetically or for string display purposes. This is
                     // illustrated below.
                     ****************************************************************************/
                    if (id == 0) {
                        TimeStamp.setTimeInMillis(measurement);
                        writer.write(format.format(measurement)+",");
                    }else if (id==2){
                        writer.write(""+df.format(Double.longBitsToDouble(measurement)));
                        if (changed==1){// if changed, just add a *
                            writer.write("*");
                        }
                        writer.write(",");
                    }else if (id == 4) {
                        writer.write(""+df.format(Double.longBitsToDouble(measurement))+"\n");
                    }else {
                        writer.write(""+df.format(Double.longBitsToDouble(measurement))+",");
                    }

                    /****************************************************************************
                     // Here we pick up a measurement (ID = 4 in this case), but you can pick up
                     // any measurement you want to. All measurements in the stream are
                     // captured by this class. Note that all data measurements are double types
                     // This illustrates how to convert the bits read from the stream into a double
                     // type. Its pretty simple using Double.longBitsToDouble(long value). So here
                     // we print the time stamp and the data associated with the ID we are interested in.
                     ****************************************************************************/

                    System.out.print("\n");
                }
                /*******************************************************************************
                 *	The EndOfStreamExeception below is thrown when you reach end of the input
                 *	stream. At this point, the filter ports are closed and a message is
                 *	written letting the user know what is going on.
                 ********************************************************************************/ catch (EndOfStreamException e) {
                    ClosePorts();
                    System.out.print("\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread);
                    break;
                }
            } // while
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // run
}