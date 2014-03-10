package baconGraph;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import baconGraph.Actor.ActorType;

public class ActorTest {

	//All the tests in this file worked on my laptop. I had to replace offending characters
	//with questions marks in order for it to compile.
	//Testing constructors
	/*@Test
	public void actorConstructorTest() throws IOException {
		Actor root = new Actor("Ahmet Çadirci", "D", ActorType.NODE, new Resources());
		root.getName().equals("Ahmet Çadirci");
		assertTrue(root.getFirstInitial().equals("A"));
	}//NON -ASCII
	
	
	//Testing hashcodes - we want the hashcode for an actors name
	//to be the same as the hashcode for the whole actor.
	@Test
	public void hashcodeTest() {
		Resources r = null;
		try {
			r = new Resources();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Actor hash = new Actor("Hash", "/m/01vvyc_", r);
		assertFalse("hash".hashCode() == hash.hashCode()); //Note - case sensitive
		assertTrue("Hash".hashCode() == hash.hashCode());
	}
	
	//Testing getting initials
	@Test
	public void numInitialsTest() {
		Actor fifty = new Actor("50 Cent", "/m/01vvyc_");
		assertTrue("50".equals(fifty.getFirstInitial()));
		assertTrue(fifty.getFirstInitial().equals(fifty.getLastInitial()));
	}
	/* 
	@Test
	public void initialsTest() {
		Actor yonce = new Actor("Beyonc?", "/m/01mpq7s");
		assertTrue("B".equals(yonce.getFirstInitial()));
		assertTrue(yonce.getLastInitial().equals(yonce.getFirstInitial()));
	}  //NON-ASCII
	
	//Testing guys with middle names.
	@Test
	public void lastInitialTest() {
		Actor rdj = new Actor("Robert Downey Jr.", "/m/016z2j");
		Actor slj = new Actor("Samuel L. Jackson", "/m/0f5xn");
		assertTrue(rdj.getLastInitial().equals(slj.getLastInitial()));
		assertTrue(rdj.getFirstInitial().equals("R"));
		assertTrue(slj.getFirstInitial().equals("S"));
	}
	/*
	@Test
	public void strangeInitialTest() {
		Actor who = new Actor("???", "/m/05dbmt4");
		assertTrue(who.getFirstInitial().equals("?"));
	}*/ //NON ASCII
	
	/*
	@Test
	public void hyphenatedInitialTest() { 
		Actor hyphens = new Actor("?-Miyavi-", "/m/01qkwf8");
		assertTrue(hyphens.getFirstInitial().equals(hyphens.getLastInitial()));
		assertTrue(hyphens.getFirstInitial().equals("?"));
	}*/ // NON ASCII
}
