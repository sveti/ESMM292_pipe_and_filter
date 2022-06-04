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
public class Plumber {
    public static void main(String argv[]) {
        /****************************************************************************
         * Here we instantiate three filters.
         ****************************************************************************/
        FilterFramework[] sources = new FilterFramework[2];
        SourceFilter subA = new SourceFilter(1, "SubSetA.dat");
        sources[0] = subA;

        SourceFilter subB = new SourceFilter(1, "SubSetB.dat");
        sources[1] = subB;

        MiddleFilter middleFilter = new MiddleFilter(2);
        SortFilter sortFilter = new SortFilter(1);
        TemperatureFilter temperatureFilter = new TemperatureFilter(1);
        AltitudeFilter altitudeFilter = new AltitudeFilter(1);
        PressureFilter pressureFilter = new PressureFilter(1, 45, 90);
        AttitudeFilter attitudeFilter = new AttitudeFilter(1, 65, 10);
        SinkFilter Filter3 = new SinkFilter(1);
//
//		/****************************************************************************
//		* Here we connect the filters starting with the sink filter (Filter 1) which
//		* we connect to Filter2 the middle filter. Then we connect Filter2 to the
//		* source filter (Filter3).
//		****************************************************************************/
//
        Filter3.Connect(attitudeFilter);// This essentially says, "connect Filter3 input port to attitudeFilter output port
        attitudeFilter.Connect(pressureFilter);// This essentially says, "connect attitudeFilter input port to pressureFilter output port
        pressureFilter.Connect(altitudeFilter);// This essentially says, "connect pressureFilter input port to altitudeFilter output port
        altitudeFilter.Connect(temperatureFilter);// This essentially says, "connect altitudeFilter input port to temperatureFilter output port
        temperatureFilter.Connect(sortFilter);// This essentially says, "connect temperatureFilter input port to sortFilter output port
        sortFilter.Connect(middleFilter);// This essentially says, "connect sortFilter input port to middleFilter output port
        middleFilter.Connect(sources);// This essentially says, "connect middleFilter input port to sources output port
//		/****************************************************************************
//		* Here we start the filters up. All-in-all,... its really kind of boring.
//		****************************************************************************/
//
        subA.start();
        subB.start();
        middleFilter.start();
        sortFilter.start();
        temperatureFilter.start();
        altitudeFilter.start();
        pressureFilter.start();
        attitudeFilter.start();
        Filter3.start();

    } // main

} // Plumber