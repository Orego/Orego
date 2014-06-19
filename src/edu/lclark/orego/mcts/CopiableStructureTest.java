package edu.lclark.orego.mcts;

import static org.junit.Assert.*;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;

public class CopiableStructureTest {

	CopiableStructure cs;
	
	W w;
	
	@Before
	public void setUp() throws Exception {
		w = new W();
		w.x = new X();
		w.y = new Y();
		w.x.z = new Z();
		w.y.z = w.x.z;
		cs = new CopiableStructure().add(w).add(w.x.z);
	}

	@Test
	public void testCopy() {
		CopiableStructure cs2 = cs.copy();
		W w2 = cs2.get(W.class);
		Z z2 = cs2.get(Z.class);
		assertNotSame(w2, w);
		assertSame(w2.x.z, w2.y.z);
		assertSame(w2.x.z, z2);
	}

	// The remaining classes are for setting up, e.g., dags for tests
	
	@SuppressWarnings("serial")
	static class W implements Serializable {
		X x;
		Y y;
	}

	@SuppressWarnings("serial")
	static class X implements Serializable {
		Z z;
	}

	@SuppressWarnings("serial")
	static class Y implements Serializable {
		Z z;
	}

	@SuppressWarnings("serial")
	static class Z implements Serializable {
		// No body needed
	}

}
