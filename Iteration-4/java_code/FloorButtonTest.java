import java.net.*;
import java.text.*;
import java.util.*;
import org.junit.*;
import org.junit.Assert.*;

public class FloorButtonTest extends junit.framework.TestCase {
    
  
    FloorLamp floorlamp = new FloorLamp();
		FloorButton floorButton = new FloorButton(5);
		floorButton.buttonLamp = floorlamp;
		assertFalse(floorButton.getButtonState());
		floorButton.pressUp();
		assertTrue(floorButton.getButtonState());
		assertTrue(floorButton.getButtonDirectionUp());
  
  
}
