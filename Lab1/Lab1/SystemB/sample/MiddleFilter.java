import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;

/******************************************************************************************************************
* File:MiddleFilter.java
* Project: Lab 1
* Copyright:
*   Copyright (c) 2020 University of California, Irvine
*   Copyright (c) 2003 Carnegie Mellon University
* Versions:
*   1.1 January 2020 - Revision for SWE 264P: Distributed Software Architecture, Winter 2020, UC Irvine.
*   1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
* This class serves as an example for how to use the FilterRemplate to create a standard filter. This particular
* example is a simple "pass-through" filter that reads data from the filter's input port and writes data out the
* filter's output port.
* Parameters: None
* Internal Methods: None
******************************************************************************************************************/

public class MiddleFilter extends FilterFramework
{
	public void run()
    {
		try {
			int bytesread = 0;                    // Number of bytes read from the input file.
			int byteswritten = 0;                // Number of bytes written to the stream.
			byte databyte = 0;                    // The byte of data read from the file
			int MeasurementLength = 8;        // This is the length of all measurements (including time) in bytes
			int IdLength = 4;                // This is the length of IDs in the byte stream
			long measurement;                // This is the word used to store all measurements - conversions are illustrated.
			int id;                            // This is the measurement id
			int i;                            // This is a loop counter

			/**
			 * Here I add some data structures to record altitudes and current frame data.
			 * In this way can I compare the altitudes with previous data and output the frame to
			 * SinkFilter in one time after I save the wild_jump original data and change the altitudes to
			 * average value.
			 */

			byte changed=0;						//This is a signal telling whether the attitude is changed, it will be sent as a byte to the sinkFilter
			LinkedList<Long> list = new LinkedList<Long>();		//save the previous altitude data for comparison
			SimpleDateFormat format = new SimpleDateFormat("yyyy:DD:HH:mm:ss");	//format time stamps
			BufferedWriter writer = new BufferedWriter(new FileWriter("WildPoints.csv")); //writer for WildPoints.csv
			ArrayList<Integer> idCache = new ArrayList<>();			//save current frame id in a loop, save the ids for current frame
			ArrayList<Long> cahce = new ArrayList<>();				//save current frame in a loop, save the measurements for current frame
			ArrayList<Byte> outputCache = new ArrayList<>();		//save all the bytes and signal in this cache, when the finish dealing with current frame, it will write all the bytes to the sinkerFilter in one time
			DecimalFormat df = new DecimalFormat("#.00000");
			df.setRoundingMode(RoundingMode.FLOOR);

			// Next we write a message to the terminal to let the world know we are alive...
			System.out.print("\n" + this.getName() + "::Middle Reading ");
			writer.write("Time,Velocity,Altitude,Pressure,Temperature\n");
			while (true) {
				// Here we read a byte and write a byte
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
						outputCache.add(databyte);
						//here I delete WriteFilterOutputPort and used outputCache to save the data
						//So it won't send the data immediately
					}
					idCache.add(id);						//save current id

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
							// measurement
						}
						bytesread++;                                    // Increment the byte count
						if (id!=2){
							outputCache.add(databyte);		//because altitude may be changed, for id==2, we don't add the bytes immediately.
							//We will add the bytes into cache after changing the altitude
						}
					}
					cahce.add(measurement);			//save current measurement

					/**
					 * The part is used to assert whether the altitude needs to be changed.
					 */

					if (id==2){
						if (list.size()==3){//we only needs three altitudes to calculate average
							list.removeFirst();//so remove the first one
							list.add(measurement);//add current value to the list
							Double pre = Double.longBitsToDouble(list.get(1));
							Double last = Double.longBitsToDouble(list.getLast());
							if (Math.abs(last-pre)>100.0){//if it's a wild jump
								changed=1;		//means changed
								measurement = Double.doubleToLongBits((Double.longBitsToDouble(list.get(1))+Double.longBitsToDouble(list.get(0)))/2);
								//calculate new value
								list.removeLast();
								list.add(measurement);//add the new value to the list
							}else{
								changed = 0;//not changed
							}

						}else if (list.size()==2){ //similar
							list.add(measurement);
							Double pre = Double.longBitsToDouble(list.get(1));
							Double last = Double.longBitsToDouble(list.getLast());
							if (Math.abs(last-pre)>100.0){
								changed=1;
								measurement = Double.doubleToLongBits((Double.longBitsToDouble(list.get(1))+Double.longBitsToDouble(list.get(0)))/2);
								list.removeLast();
								list.add(measurement);
							}else{
								changed = 0;
							}

						}else if (list.size()==1){	//similar
							list.add(measurement);
							Double pre = Double.longBitsToDouble(list.get(0));
							Double last = Double.longBitsToDouble(list.getLast());
							if (Math.abs(last-pre)>100.0){
								changed=1;
								measurement = list.get(0);	//no need to calculate average
								list.removeLast();
								list.add(measurement);
							}else{
								changed = 0;
							}

						}else if (list.size()==0){
							list.add(measurement);		// the first frame
						}
					}


					/**
					 * This part of codes are added to write wild jumps to the WildPoints.csv and send the bytes to sinkedFilter
					 * When id == 4, it means current frame will end.
					 */
					if (id==4){
						if (changed==1) {	//if changed, write the point to the file
							for (int j = 0; j < idCache.size(); j++) {
								if (j == 0) {
									writer.write(format.format(cahce.get(0)) + ",");//cache stores every measurements for current frame
								} else if (j == 4) {
									writer.write(("" + df.format(Double.longBitsToDouble(cahce.get(4))) + "\n"));
								}
								else{
									writer.write("" + df.format(Double.longBitsToDouble(cahce.get(j))) + ",");
								}
							}
						}

						//clear cache for next loop
						cahce.clear();
						idCache.clear();

						//no matter whether the data is changed, for each loop we need to send it to sinkedFilter
						for (byte b:outputCache){
							WriteFilterOutputPort(b);
							byteswritten++;
						}
						outputCache.clear();
					}


					//how to change LongBits into Bytes
					//refer to https://javadeveloperzone.com/java-basic/java-convert-long-to-byte-array/
					//here we deal with id==2, and add the altitude after change to outputCache
					//In this way, can data with right format sent to sinkFilter
					if (id==2){
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						DataOutputStream dos = new DataOutputStream(bos);
						dos.writeLong(measurement); //change altitude to bytes
						dos.flush();
						byte[] bytes= bos.toByteArray();//get the bytes
						for (byte b:bytes){
							outputCache.add(b);//add to the cache
						}
						outputCache.add(changed);//and add one byte to signal sinkFilter whether the value is changed
					}
				} catch (EndOfStreamException e) {
					ClosePorts();
					System.out.print("\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten);
					break;
				}
			}
			writer.close();
		}catch (IOException e){
			System.out.println("Write to files failed");
		}
   }
}