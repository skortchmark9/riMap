package autocorrect;

import static org.junit.Assert.*;

import org.junit.Test;

public class UtilsTest {

    @Test
    public void lineParseTest() {
        String bad = "HI#*$&SAM     WHOA THIS KEyBoard(02 IS 223132321 CRAY";
        String[] results = {"hi", "sam", "whoa", "this", "keyboard", "is", "cray"};
        boolean outcome = true;
        for(int i =0; i < results.length; i++ ) {
            if(!results[i].equals(Utils.lineParse(bad)[i])) {
                outcome = false;
                System.out.println(results[i]);
            }
        }
        assertTrue(outcome);
    }
    
    @Test
    public void breakpointTest() {
    assertTrue(Utils.findBreakPoint("cat", "catastrophe") == 3);
    }
    
    @Test
    public void arrayParseTest() {
        String[] results = {"hi", "sam", "whoa", "this", "keyboard", "is", "cray"};
        assertTrue(Utils.arrayParse(results).equals("hi sam whoa this keyboard is cray"));
    }
}
