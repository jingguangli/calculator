package test.calculator.TestCalculator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class CalculatorTest {
	
	@Test
    public void transToObjectListTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Calculator calculator = new Calculator();
		//calculator.tr
		String METHOD_NAME = "transToObjectList";
		Class[] parameterTypes = new Class[3];
		parameterTypes[0] = String.class;
		parameterTypes[1] = String.class;
		parameterTypes[2] = int.class;

		Method transToObjectList = calculator.getClass().getDeclaredMethod(METHOD_NAME, parameterTypes);
		transToObjectList.setAccessible(true);

		Object[] parameters = new Object[3];

		parameters[0] = "add(1, 2)";
		parameters[1] = "root";
		parameters[2] = 0;
		Object[] results = (Object[]) transToObjectList.invoke(calculator, parameters);

		Assert.assertEquals(2, results.length);
		Assert.assertEquals("8", results[1].toString());
		// Note: this is for sample testing only, not covering all the scenarios.
    }
	
	@Test
    public void evaluateObjectListToRetrieveResultTest() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Calculator calculator = new Calculator();
		//calculator.tr
		String METHOD_NAME = "evaluateObjectListToRetrieveResult";
		Class[] parameterTypes = new Class[1];
		parameterTypes[0] = Object.class;

		Method evaluateObjectListToRetrieveResult = calculator.getClass().getDeclaredMethod(METHOD_NAME, parameterTypes);
		evaluateObjectListToRetrieveResult.setAccessible(true);

		Object[] parameters = new Object[1];
		
		ArrayList<Object> expList = new ArrayList<Object>();
		expList.add("root");
		
		ArrayList<Object> expSubList = new ArrayList<Object>();
		expSubList.add("add");
		expSubList.add("1");
		expSubList.add("2");
		
		expList.add(expSubList);

		parameters[0] = expList;

		Assert.assertEquals("3",  evaluateObjectListToRetrieveResult.invoke(calculator, parameters).toString());
		// Note: this is for sample testing only, not covering all the scenarios.
    }
}
