/******************************************************************************************************************
* File:Plumber.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example to illstrate how to use the PlumberTemplate to create a main thread that
* instantiates and connects a set of filters. This example consists of three filters: a source, a middle filter
* that acts as a pass-through filter (it does nothing to the data), and a sink filter which illustrates all kinds
* of useful things that you can do with the input stream of data.
*
* Parameters: 		None
*
* Internal Methods:	None
*
******************************************************************************************************************/
public class Plumber
{
   public static void main( String argv[])
   {
		/****************************************************************************
		* Here we instantiate three filters.
		****************************************************************************/

		SourceFilter Filter1 = new SourceFilter();
		TemperatureFilter temperatureFilter = new TemperatureFilter();
	   	AltitudeFilter altitudeFilter = new AltitudeFilter();
	   	PressureFilter pressureFilter = new PressureFilter();
		SinkFilter Filter3 = new SinkFilter();

		/****************************************************************************
		* Here we connect the filters starting with the sink filter (Filter 1) which
		* we connect to Filter2 the middle filter. Then we connect Filter2 to the
		* source filter (Filter3).
		****************************************************************************/

		Filter3.Connect(pressureFilter); // This esstially says, "connect Filter3 input port to temperatureFilter output port
	   pressureFilter.Connect(altitudeFilter);// This esstially says, "connect temperatureFilter intput port to Filter1 output port
	    altitudeFilter.Connect(temperatureFilter);
	   temperatureFilter.Connect(Filter1);
		/****************************************************************************
		* Here we start the filters up. All-in-all,... its really kind of boring.
		****************************************************************************/

		Filter1.start();
	   	temperatureFilter.start();
	   	altitudeFilter.start();
	   	pressureFilter.start();
		Filter3.start();

   } // main

} // Plumber