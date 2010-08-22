package com.crawljax.plugins.aji.assertionchecker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;

import com.crawljax.util.Helper;

/**
 * Tester for ast instrumenter.
 * 
 * @author frankg
 */
public class AstAssertionInserterTest {

	private static AstAssertionInserter inserter;

	private final CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	private final String simpleSource = "function test() { var something = false; };";

	/**
	 * Generate a simple JavaScript AST.
	 * 
	 * @return The AST.
	 */
	private AstNode createAst(String source) {
		Parser p = new Parser(compilerEnvirons, null);

		return p.parse(source, null, 0);
	}

	/**
	 * Setup done before tests are started.
	 */
	@BeforeClass
	public static void oneTimeSetUp() {

		readInput();

		inserter = new AstAssertionInserter("daikon.assertions");
		inserter.setScopeName("filename");

		inserter.start();
	}

	/**
	 * Tear down done after all tests finish.
	 */
	@AfterClass
	public static void oneTimeTearDown() {
		/* clean up directories and files */

		File f = new File("daikon.assertions");
		if (f.exists()) {
			f.delete();
		}

	}

	/**
	 * Test instrumentation initialization with empty input.
	 */
	@Test
	public void finishEmpty() {
		AstRoot node = new AstRoot();

		inserter.finish(node);

		assertFalse("Should not be empty", node.toSource().equals(""));
	}

	/**
	 * Test instrumentation initialization with simple input.
	 */
	@Test
	public void finishSimple() {
		AstRoot node = (AstRoot) createAst(simpleSource);

		inserter.finish(node);

		assertFalse("Should contain instrumentation initalization", equalSources(node.toSource(),
		        simpleSource));
	}

	/**
	 * Test visit with empty input.
	 */
	@Test
	public void visitEmpty() {
		AstRoot node = new AstRoot();

		node.visit(inserter);

		assertTrue("Should be empty", node.toSource().equals(""));
	}

	/**
	 * Test visit with all kinds of input.
	 */
	@Test
	public void visitSimple() {
		AstNode node = createAst(simpleSource);

		node.visit(inserter);

		assertFalse("Should contain instrumentation code", equalSources(simpleSource, node
		        .toSource()));

	}

	/**
	 * Start method of the ast assertion inserter.
	 */
	public static void readInput() {

		String file = "daikon.assertions";

		String daikonResult =
		        "=================================================================="
		                + "=========\nfilename.test:::ENTER\n"
		                + "typeof(window) == \"undefined\"\n";
		try {
			Helper.writeToFile(file, daikonResult, false);
		} catch (IOException e) {
			fail("Unable to set up input file for test.");
		}
	}

	private boolean equalSources(String s1, String s2) {
		return parse(s1).toSource().equals(parse(s2).toSource());
	}

	/**
	 * Create an AST from the passed js.
	 * 
	 * @param js
	 *            The javascript to be parsed.
	 * @return The AST.
	 */
	private AstNode parse(String js) {
		Parser p = new Parser(compilerEnvirons, null);

		return p.parse(js, null, 0);
	}
}