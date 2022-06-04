/******************************************************************************************************************
* File:MiddleFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for how to use the FilterRemplate to create a standard filter. This particular
* example is a simple "pass-through" filter that reads data from the filter's input port and writes data out the
* filter's output port.
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/

public class MiddleFilter extends FilterFramework
{
	public MiddleFilter(int inPorts) {
		super(inPorts);
	}

	public void run()
	{
		///6*8 -> 48  4 -> 8
		///6*4 -> 24
		int frameLength = 6*8 + 6*4;    //ID + measuremets
		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		byte databyte = 0;					// The byte of data read from the file

		int currentPipe = 0;
		int closePort = 0;

		// Next we write a message to the terminal to let the world know we are alive...

		//System.out.print( "\n" + this.getName() + "::Middle Reading ");

		while (true)
		{
			/*************************************************************
			 *	Here we read a byte and write a byte
			 *************************************************************/

			for(int i = 0; i < frameLength; i++)
			{
				try
				{
					databyte = ReadFilterInputPort(currentPipe);
					bytesread++;
					WriteFilterOutputPort(databyte);
					byteswritten++;

				} // try

				catch (EndOfStreamException e)
				{
					closePort++;
					if(closePort == getInPorts())
					{
						ClosePorts();
						return;
					}
					break;

				} // catch
			}
			currentPipe = (currentPipe==0)?1:0;
//			System.out.println("Pipe is " + currentPipe);

		} // while

	} // run

} // MiddleFilter