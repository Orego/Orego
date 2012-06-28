package orego.wls;
/**
	@author Jacques Basald'a
	 
	LICENSE:
	========

		Copyright (c) 2011 Jacques Basaldúa.
		All rights reserved.

	Redistribution and use in source and binary forms are permitted
	provided that the above copyright notice and this paragraph are
	duplicated in all such forms and that any documentation,
	advertising materials, and other materials related to such
	distribution and use acknowledge that the software was developed
	by the <organization>.  The name of the author may not be used to
	endorse or promote products derived from this software without
	specific prior written permission.

	THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.

*/



import java.util.*;

public class WLSout {

    public static void main (String[] args) 
    {
		WinLossStates wls = new WinLossStates();

		System.out.println ("  WLS tables: dummy program\n");

		System.out.print ("Win Table: " + wt.WIN[0]);
		for (i = 0; i < wls.NUM_STATES; i++) 
		{	
			System.out.print (", " + wt.[i]);
			if ((i % 10) == 9) System.out.println ("");
		};
		System.out.println ("");
		System.out.println ("");

		System.out.print ("LOSS[] = " + wt.LOSS[0]);
		for (i = 1; i <= wt.bestState; i++) 
		{	
			System.out.print (", " + wt.LOSS[i]);
			if ((i % 10) == 9) System.out.println ("");
		};
		System.out.println ("");
		System.out.println ("");
		System.out.println ("Done.");
    };
    
}
