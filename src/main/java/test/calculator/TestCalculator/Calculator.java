package test.calculator.TestCalculator;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * Sample Calculator for Integers
 * •	Numbers: integers between Integer.MIN_VALUE and Integer.MAX_VALUE
 * •	Variables: strings of characters, where each character is one of a-z, A-Z
 * •	Arithmetic functions: add, sub, mult, div, each taking two arbitrary expressions as arguments.  In other words, each argument may be any of the expressions on this list.
 * •	A “let” operator for assigning values to variables:
 *  	let(<variable name>, <value expression>, <expression where variable is used>)
 * 		As with arithmetic functions, the value expression and the expression where the variable is used may be an arbitrary expression from this list. 
 * e.g.
 *  add(1, 2) 
 *  add(1, mult(2, 3)) 
 *  mult(add(2, 2), div(9, 3)) 
 *  let(a, 5, add(a, a)) 
 *  let(a, 5, let(b, mult(a, 10), add(b, a)))
 *  let(a, let(b, 10, add(b, b)), let(b, 20, add(a, b)))
 */
public class Calculator {

	private static final Pattern inputExp = Pattern.compile("^[a-zA-Z]([a-zA-Z]|[0-9]|[,]|[(]|[)])*[)]$");
	private static final Pattern intExp = Pattern.compile("\\d+");
	private static final Pattern varExp = Pattern.compile("([a-zA-Z])+");
	private static final HashMap<Object, Long> letFirstVariabStorage = new HashMap<Object, Long>();
	private static final String ROOT = "root";
	private static final String ADD = "add";
	private static final String SUB = "sub";
	private static final String MULT = "mult";
	private static final String DIV = "div";
	private static String LET = "let";
	final static Logger logger = LogManager.getRootLogger();
	
	/**
	 * Main
	 * @param args
	 *      option 1: type 'exit' and click enter to exit the loop; 
	 *      option 2: type 'log4j' & the next input line is for log level, valid log level [OFF FATAL ERROR WARN INFO DEBUG TRACE ALL]; 
	 *      option 3: type an expression and click enter to view the result;
	 */
	public static void main(String[] args) {
		
		final Scanner scanner = new Scanner(System.in);
		String expIn = "";
		System.out.println("*** Sample Calculator ***");
		System.out.println("option 1: type 'exit' and click enter to exit the loop");
		System.out.println("option 2: type 'log4j' & the next input line is for log level, valid log level [OFF FATAL ERROR WARN INFO DEBUG TRACE ALL]");
		System.out.println("option 3: type an expression and click enter to view the result;");

		// type exit to end the loop
		while (!(expIn = scanner.nextLine()).equalsIgnoreCase("exit")) {
			long result = 0;
			try {
				expIn = expIn.replaceAll("\\s+", "");
				
				if (expIn.equalsIgnoreCase("log4j")) {
					System.out.println("Please enter a valid log level [OFF FATAL ERROR WARN INFO DEBUG TRACE ALL]");
					final String newLogLvl = scanner.nextLine().toUpperCase();
					if (setLogLevel(newLogLvl.toUpperCase())) {
						logger.info("The new log level is " + newLogLvl);
					}
					else {
						logger.info("The new log level setup failed: " + newLogLvl + " **** Type log4j to reset the log level again.");
					}
				} 
				else {
					if (inputExp.matcher(expIn).matches()) {
						result = evaluateObjectListToRetrieveResult(transToObjectList(expIn, ROOT, 0)[0]);

						if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
							System.out
									.println("Operation results is not in [" + Integer.MIN_VALUE +", " + Integer.MAX_VALUE+"]");
							logger.warn("Expression = " + result);
						} 
						else {
							System.out.println(result);
							logger.info("Expression = " + result);
						}
					}
					else {
						System.out
						.println("Invalid expression - can only contains character, number, ',' ,'(', or ')', also starting with valid operator(let/add/sub/mult/div) and end with ')'");
						logger.warn("Expression = " + result);
					}
				}
				
								
			} 
			catch (Exception e) {
				System.out.println("Invalid Expression");
				logger.error("Invalid Expression - " + expIn + " - " + e.getStackTrace());				
			}
			System.out.println("option 1: type 'exit' and click enter to exit the loop");
			System.out.println("option 2: type 'log4j' & the next input line is for log level, valid log level [OFF FATAL ERROR WARN INFO DEBUG TRACE ALL]");
			System.out.println("option 3: type an expression and click enter to view the result;");
		}
	}

	/**
	 * Convert the input expression to an Object list.
	 * @param expression full/partial of the input expression 
	 * @param nodeDesc can be root/add/sub/mult/div/let
	 * @param index the starting char position of the initial input expression
	 * @return returns an object list, e.g. input: sub(1,2) after conversion:[[root, [sub, 1, 2]], 8]
	 * @throws Exception
	 */
	private static Object[] transToObjectList(String expression,
			String nodeDesc, int index) throws Exception {
		final ArrayList<Object> expList = new ArrayList<Object>();
		expList.add(nodeDesc);
		String tmp = "";
		try {
			while (true) {
				final char currentChar = expression.charAt(index);
				if (currentChar == '(') {
					final Object[] returned = transToObjectList(expression, tmp,
							index + 1);
					index = Integer.valueOf(returned[1].toString());
					expList.add(returned[0]);
					tmp = "";
					if (nodeDesc.equalsIgnoreCase(ROOT)) {
						break;
					}
				} 
				else if (currentChar == ')') {
					if (tmp.length() > 0) {
						expList.add(tmp);
					}
					break;
				} 
				else if (currentChar == ',') {
					if (tmp.length() > 0) {
						expList.add(tmp);
					}
					tmp = "";
				} 
				else {
					tmp += expression.charAt(index);
				}
				++index;
			}
		} 
		catch (Exception e) {
			throw new InvalidObjectException(expression);
		}

		return new Object[] { expList, index };
	}

	/**
	 * Traversing the expList object list to calculate the result.
	 * @param expList is the object list returned from transToObjectList.
	 * @return result
	 */
	private static long evaluateObjectListToRetrieveResult(Object expList) {
		
		if (expList instanceof String) {
			if (intExp.matcher(expList.toString()).matches()) {
				return Integer.parseInt(expList.toString());
			} 
			else {
				return letFirstVariabStorage.get(expList.toString());
			}
		} else {
			final ArrayList<?> expListIn = (ArrayList<?>) expList;
			final String operator = expListIn.get(0).toString();
			final Object secondExp = expListIn.get(1);
			if (operator.equalsIgnoreCase(ROOT)) {
				return evaluateObjectListToRetrieveResult(secondExp);
			}
			else if (operator.equalsIgnoreCase(ADD)) {
				return evaluateObjectListToRetrieveResult(secondExp)
						+ evaluateObjectListToRetrieveResult(expListIn.get(2));
			}
			else if (operator.equalsIgnoreCase(SUB)) {
				return evaluateObjectListToRetrieveResult(secondExp)
						- evaluateObjectListToRetrieveResult(expListIn.get(2));
			} 
			else if (operator.equalsIgnoreCase(MULT)) {
				return evaluateObjectListToRetrieveResult(secondExp)
						* evaluateObjectListToRetrieveResult(expListIn.get(2));
			} 
			else if (operator.equalsIgnoreCase(DIV)) {
				return evaluateObjectListToRetrieveResult(secondExp)
						/ evaluateObjectListToRetrieveResult(expListIn.get(2));
			} 
			else if (operator.equalsIgnoreCase(LET)) {
				letFirstVariabStorage.put(secondExp,
						evaluateObjectListToRetrieveResult(expListIn.get(2)));
				return evaluateObjectListToRetrieveResult(expListIn.get(3));
			}
		}
		return 0;
	}
	
	/**
	 * This is to reset log level.
	 * 
	 * @param level Valid log level [OFF FATAL ERROR WARN INFO DEBUG TRACE ALL]
	 */
	private static boolean setLogLevel(String level) {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		final LoggerConfig loggerConfig = config
				.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

		switch (level.charAt(0)) {
		case 'O':
			loggerConfig.setLevel(Level.OFF);
			ctx.updateLoggers();
			return true;
		case 'F':
			loggerConfig.setLevel(Level.FATAL);
			ctx.updateLoggers();
			return true;
		case 'E':
			loggerConfig.setLevel(Level.ERROR);
			ctx.updateLoggers();
			return true;
		case 'W':
			loggerConfig.setLevel(Level.WARN);
			ctx.updateLoggers();
			return true;
		case 'I':
			loggerConfig.setLevel(Level.INFO);
			ctx.updateLoggers();
			return true;
		case 'D':
			loggerConfig.setLevel(Level.DEBUG);
			ctx.updateLoggers();
			return true;
		case 'T':
			loggerConfig.setLevel(Level.TRACE);
			ctx.updateLoggers();
			return true;
		case 'A':
			loggerConfig.setLevel(Level.ALL);
			ctx.updateLoggers();
			return true;
		default:
			return false;
		}
	}

	
}
