/*
    Automatic JavaScript Invariants is a plugin for Crawljax that can be
    used to derive JavaScript invariants automatically and use them for
    regressions testing.
    Copyright (C) 2010  crawljax.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/
package com.crawljax.plugins.aji;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.ForLoop;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.IfStatement;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.WhileLoop;

import com.crawljax.core.CrawljaxController;
import com.crawljax.plugins.aji.executiontracer.ProgramPoint;

/**
 * Abstract class that is used to define the interface and some functionality for the NodeVisitors
 * that modify JavaScript.
 * 
 * @author Frank Groeneveld
 * @version $Id: JSASTModifier.java 6161 2009-12-16 13:47:15Z frank $
 */
public abstract class JSASTModifier implements NodeVisitor {

	private final Map<String, String> mapper = new HashMap<String, String>();

	protected static final Logger LOGGER = Logger.getLogger(CrawljaxController.class.getName());

	/**
	 * This is used by the JavaScript node creation functions that follow.
	 */
	private CompilerEnvirons compilerEnvirons = new CompilerEnvirons();

	/**
	 * Contains the scopename of the AST we are visiting. Generally this will be the filename
	 */
	private String scopeName = null;

	/**
	 * @param scopeName
	 *            the scopeName to set
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	/**
	 * @return the scopeName
	 */
	public String getScopeName() {
		return scopeName;
	}

	/**
	 * Abstract constructor to initialize the mapper variable.
	 */
	protected JSASTModifier() {
		/* add -<number of arguments> to also make sure number of arguments is the same */
		mapper.put("addClass", "attr('class')");
		mapper.put("removeClass", "attr('class')");

		mapper.put("css-2", "css(%0)");
		mapper.put("attr-2", "attr(%0)");
		mapper.put("append", "html()");
	}

	/**
	 * Parse some JavaScript to a simple AST.
	 * 
	 * @param code
	 *            The JavaScript source code to parse.
	 * @return The AST node.
	 */
	public AstNode parse(String code) {
		Parser p = new Parser(compilerEnvirons, null);
		return p.parse(code, null, 0);
	}

	/**
	 * Find out the function name of a certain node and return "anonymous" if it's an anonymous
	 * function.
	 * 
	 * @param f
	 *            The function node.
	 * @return The function name.
	 */
	protected String getFunctionName(FunctionNode f) {
		Name functionName = f.getFunctionName();

		if (functionName == null) {
			return "anonymous" + f.getLineno();
		} else {
			return functionName.toSource();
		}
	}

	/**
	 * Creates a node that can be inserted at a certain point in function.
	 * 
	 * @param function
	 *            The function that will enclose the node.
	 * @param postfix
	 *            The postfix function name (enter/exit).
	 * @param lineNo
	 *            Linenumber where the node will be inserted.
	 * @return The new node.
	 */
	protected abstract AstNode createNode(FunctionNode function, String postfix, int lineNo);

	/**
	 * Creates a node that can be inserted before and after a DOM modification statement (such as
	 * jQuery('#test').addClass('bla');).
	 * 
	 * @param shouldLog
	 *            The variable that should be logged (for example jQuery('#test').attr('style'))
	 * @param lineNo
	 *            The line number where this will be inserted.
	 * @return The new node.
	 */
	protected abstract AstNode createPointNode(String shouldLog, int lineNo);

	/**
	 * Create a new block node with two children.
	 * 
	 * @param node
	 *            The child.
	 * @return The new block.
	 */
	private Block createBlockWithNode(AstNode node) {
		Block b = new Block();

		b.addChild(node);

		return b;
	}

	/**
	 * @param node
	 *            The node we want to have wrapped.
	 * @return The (new) node parent (the block probably)
	 */
	private AstNode makeSureBlockExistsAround(AstNode node) {
		AstNode parent = node.getParent();

		if (parent instanceof IfStatement) {
			/* the parent is an if and there are no braces, so we should make a new block */
			IfStatement i = (IfStatement) parent;

			/* replace the if or the then, depending on what the current node is */
			if (i.getThenPart().equals(node)) {
				i.setThenPart(createBlockWithNode(node));
			} else {
				i.setElsePart(createBlockWithNode(node));
			}
		} else if (parent instanceof WhileLoop) {
			/* the parent is a while and there are no braces, so we should make a new block */
			/* I don't think you can find this in the real world, but just to be sure */
			WhileLoop w = (WhileLoop) parent;
			w.setBody(createBlockWithNode(node));
		} else if (parent instanceof ForLoop) {
			/* the parent is a for and there are no braces, so we should make a new block */
			/* I don't think you can find this in the real world, but just to be sure */
			ForLoop f = (ForLoop) parent;
			f.setBody(createBlockWithNode(node));
		}
		// else if (parent instanceof SwitchCase) {
		// SwitchCase s = (SwitchCase) parent;
		// List<AstNode> statements = new TreeList(s.getStatements());

		// for (int i = 0; i < statements.size(); i++) {
		// if (statements.get(i).equals(node)) {
		// statements.add(i, newNode);

		/**
		 * TODO: Frank, find a way to do this without concurrent modification exceptions.
		 */
		// s.setStatements(statements);
		// break;
		// }
		// }

		// }
		return node.getParent();
	}

	/**
	 * Actual visiting method.
	 * 
	 * @param node
	 *            The node that is currently visited.
	 * @return Whether to visit the children.
	 */
	@Override
	public boolean visit(AstNode node) {
		FunctionNode func;

		if (node instanceof FunctionNode) {
			func = (FunctionNode) node;

			/* this is function enter */
			AstNode newNode = createNode(func, ProgramPoint.ENTERPOSTFIX, func.getLineno());

			func.getBody().addChildToFront(newNode);

			/* get last line of the function */
			node = (AstNode) func.getBody().getLastChild();
			/* if this is not a return statement, we need to add logging here also */
			if (!(node instanceof ReturnStatement)) {
				newNode = createNode(func, ProgramPoint.EXITPOSTFIX, node.getLineno());
				/* add as last statement */
				func.getBody().addChildToBack(newNode);
			}

		} else if (node instanceof ReturnStatement) {
			func = node.getEnclosingFunction();

			AstNode newNode = createNode(func, ProgramPoint.EXITPOSTFIX, node.getLineno());

			AstNode parent = makeSureBlockExistsAround(node);

			/* the parent is something we can prepend to */
			parent.addChildBefore(newNode, node);

		} else if (node instanceof Name) {

			/* lets detect function calls like .addClass, .css, .attr etc */
			if (node.getParent() instanceof PropertyGet
			        && node.getParent().getParent() instanceof FunctionCall) {

				List<AstNode> arguments =
				        ((FunctionCall) node.getParent().getParent()).getArguments();

				if (mapper.get(node.toSource()) != null
				        || mapper.get(node.toSource() + "-" + arguments.size()) != null) {

					/* this seems to be one! */
					PropertyGet g = (PropertyGet) node.getParent();

					String objectAndFunction = mapper.get(node.toSource());
					if (objectAndFunction == null) {
						objectAndFunction = mapper.get(node.toSource() + "-" + arguments.size());
					}

					objectAndFunction = g.getLeft().toSource() + "." + objectAndFunction;

					/* fill in parameters in the "getter" */
					for (int i = 0; i < arguments.size(); i++) {
						objectAndFunction =
						        objectAndFunction.replace("%" + i, arguments.get(i).toSource());
					}

					AstNode parent = makeSureBlockExistsAround(getLineNode(node));
					/*
					 * TODO: lineno will be off by one. otherwise we might have overlapping problems
					 */

					 parent.addChildBefore(createPointNode(objectAndFunction, node.getLineno()),
					 getLineNode(node));
					 parent.addChildAfter(
					 createPointNode(objectAndFunction, node.getLineno() + 1),
					 getLineNode(node));

				}
			}
		}
		/* have a look at the children of this node */
		return true;
	}

	private AstNode getLineNode(AstNode node) {
		while ((!(node instanceof ExpressionStatement) && !(node instanceof Assignment))
		        || node.getParent() instanceof ReturnStatement) {
			node = node.getParent();
		}
		return node;
	}

	/**
	 * This method is called when the complete AST has been traversed.
	 * 
	 * @param node
	 *            The AST root node.
	 */
	public abstract void finish(AstRoot node);

	/**
	 * This method is called before the AST is going to be traversed.
	 */
	public abstract void start();
}
