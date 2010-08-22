package com.crawljax.plugins.aji.executiontracer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;

/**
 * Tester for ast instrumenter.
 * 
 * @author frankg
 */
public class AstInstrumenterTest {

	private static AstInstrumenter instrumenter;

	private final CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	private final String simpleSource =
	        "var simple = 1; function test() { var something = false; }; simple++; "
	                + "simple = 10; window.test = 0; window.test++;"
	                + "function() { while(true) return false;" + "if(false) return true; }";

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
		instrumenter = new AstInstrumenter();
		instrumenter.setScopeName("test");
	}

	/**
	 * Tear down done after all tests finish.
	 */
	@AfterClass
	public static void oneTimeTearDown() {

	}

	/**
	 * Test instrumentation initialization with empty input.
	 */
	@Test
	public void finishEmpty() {
		AstRoot node = new AstRoot();

		instrumenter.finish(node);

		assertFalse("Should not be empty", node.toSource().equals(""));
	}

	/**
	 * Test instrumentation initialization with simple input.
	 */
	@Test
	public void finishSimple() {
		AstRoot node = (AstRoot) createAst(simpleSource);

		instrumenter.finish(node);

		assertFalse("Should contain instrumentation initalization", equalSources(node.toSource(),
		        simpleSource));
	}

	/**
	 * Test visit with empty input.
	 */
	@Test
	public void visitEmpty() {
		AstRoot node = new AstRoot();

		node.visit(instrumenter);

		assertTrue("Should be empty", node.toSource().equals(""));
	}

	/**
	 * Test visit with all kinds of input.
	 */
	@Test
	public void visitSimple() {
		/* all of these have weird instrumentation code like adding a pair of parenthesis */
		String[] sources =
		        new String[] { "function(a) { while(true) return false; }",
		                "function test(a) { if(true) return true; }",
		                "function(a) { if(true) return true; else return false; }",
		                "function(a) { return false; }",
		                "function(a) { for(i = 0; i < 1; i++) return false; }" };

		for (int i = 0; i < sources.length; i++) {
			AstNode node = createAst(sources[i]);

			node.visit(instrumenter);

			assertFalse("Should contain instrumentation code source " + i, equalSources(node
			        .toSource(), sources[i]));

		}

	}

	/**
	 * Check if the excludes work.
	 */
	@Test
	public void visitExcludes() {
		String source = "function(b) { return b; }";
		AstNode node = createAst(source);
		List<String> excludes = new ArrayList<String>();
		excludes.add("b");
		AstInstrumenter excludeInstrumenter = new AstInstrumenter(excludes);
		excludeInstrumenter.setScopeName("testscope");

		node.visit(excludeInstrumenter);

		assertTrue("Should not contain instrumentation code", equalSources(node.toSource(),
		        source));
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