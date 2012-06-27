package orego.wls;
/*
	DESCRIPTION:
	============

	This is a dummy program that only builds WLS tables and writes them to the
	console for verification.

	It uses the implementation in the source file WLStables.cpp


	WLS is described in:
	--------------------

	Win/Loss States: An efficient model of success rates for simulation-based
	functions	Jacques Basaldúa and J. Marcos Moreno Vega

	Since the arrays are small, static allocation is used. You can call the
	method BuildTables() to create the arrays any number of times.


	Multilingual: This file exists in Pascal (Delphi), C++ and Java.
	-------------


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
		WinLossStates wt = new WinLossStates();
		int i;

		System.out.println ("  WLS tables: dummy program");
		System.out.println ("");

		wt.BuildTables(21, 1, 1);

		System.out.println ("wt.BuildTables(21, 1, 1);");
		System.out.println ("");

		System.out.print ("WIN[] = " + wt.WIN[0]);
		for (i = 1; i <= wt.wls_State_Best; i++) 
		{	
			System.out.print (", " + wt.WIN[i]);
			if ((i % 10) == 9) System.out.println ("");
		};
		System.out.println ("");
		System.out.println ("");

		System.out.print ("LOSS[] = " + wt.LOSS[0]);
		for (i = 1; i <= wt.wls_State_Best; i++) 
		{	
			System.out.print (", " + wt.LOSS[i]);
			if ((i % 10) == 9) System.out.println ("");
		};
		System.out.println ("");
		System.out.println ("");
		System.out.println ("Done.");
    };
    
}
