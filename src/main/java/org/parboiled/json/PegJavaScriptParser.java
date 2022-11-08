package org.parboiled.json;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;

import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.DontLabel;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;
import org.parboiled.util.ParseUtils;

@BuildParseTree
public class PegJavaScriptParser extends PegParser {

	private static List<String> HIDE_LABELS = new LinkedList<>();
	static {
		HIDE_LABELS.add("start");

		HIDE_LABELS.add("FirstOf");
		HIDE_LABELS.add("Sequence");

		HIDE_LABELS.add("WhiteSpace");
		HIDE_LABELS.add("LineTerminatorSequence");

		HIDE_LABELS.add("FunctionExpression");
		HIDE_LABELS.add("FormalParameterList");

	}

	private static List<String> SKIP_LABELS = new LinkedList<>();
	static {

		SKIP_LABELS.add("SourceElements");
		SKIP_LABELS.add("SourceElement");

		//
		SKIP_LABELS.add("ExpressionStatement");
		SKIP_LABELS.add("Expression");
		SKIP_LABELS.add("AssignmentExpression");
		SKIP_LABELS.add("ConditionalExpression");
		SKIP_LABELS.add("LogicalORExpression");
		SKIP_LABELS.add("LogicalANDExpression");
		SKIP_LABELS.add("BitwiseORExpression");
		SKIP_LABELS.add("BitwiseXORExpression");
		SKIP_LABELS.add("BitwiseANDExpression");
		SKIP_LABELS.add("EqualityExpression");
		SKIP_LABELS.add("RelationalExpression");
		SKIP_LABELS.add("ShiftExpression");
		SKIP_LABELS.add("AdditiveExpression");
		SKIP_LABELS.add("MultiplicativeExpression");

		//
		SKIP_LABELS.add("PrimaryExpression");
		SKIP_LABELS.add("UnaryExpression");
		SKIP_LABELS.add("PostfixExpression");
		SKIP_LABELS.add("LeftHandSideExpression");
		SKIP_LABELS.add("NewExpression");
		SKIP_LABELS.add("MemberExpression");

		//
		SKIP_LABELS.add("CallExpression");
		SKIP_LABELS.add("Arguments");
		SKIP_LABELS.add("ArgumentList");

		//
		SKIP_LABELS.add("VariableStatement");
		SKIP_LABELS.add("VariableDeclarationList");
		SKIP_LABELS.add("VariableDeclaration");
		SKIP_LABELS.add("Initialiser");

		//
		SKIP_LABELS.add("PropertyNameAndValueList");

	}

	private static List<String> SUPRESS_LABELS = new LinkedList<>();
	static {
	}

	private static List<String> SUPRESS_SUB_LABELS = new LinkedList<>();
	static {

		SUPRESS_SUB_LABELS.add("WhiteSpace");
		SUPRESS_SUB_LABELS.add("LineTerminatorSequence");
		SUPRESS_SUB_LABELS.add("EOS");

		SUPRESS_SUB_LABELS.add("Comment");

		SUPRESS_SUB_LABELS.add("Identifier");
		SUPRESS_SUB_LABELS.add("IdentifierName");
		SUPRESS_SUB_LABELS.add("PropertyName");

		SUPRESS_SUB_LABELS.add("NumericLiteral");
		SUPRESS_SUB_LABELS.add("StringLiteral");
		SUPRESS_SUB_LABELS.add("ObjectLiteral");

		SUPRESS_SUB_LABELS.add("AdditiveOperator");
		SUPRESS_SUB_LABELS.add("MultiplicativeOperator");

		SUPRESS_SUB_LABELS.add("VarToken");
		SUPRESS_SUB_LABELS.add("FunctionToken");

	}

	private static Rule startRule;

	@Override
	public Rule start() {
		if (startRule == null) {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("javascript.peg.json");
			JsonObject json = Json.createReader(inputStream).readObject();
			startRule = super.start("Start", json);
		}
		return startRule;
	}

	@DontLabel
	protected Rule parseRule(JsonObject jsonObj) {

		Rule rule = super.parseRule(jsonObj);

		String type = jsonObj.getString("type");
		if ("rule_ref".equals(type)) {

			String name = jsonObj.getString("name");
			if (SUPRESS_LABELS.contains(name)) {
				rule.suppressNode();
			} else if (SUPRESS_SUB_LABELS.contains(name)) {
				rule.suppressSubnodes();
			} else if (SKIP_LABELS.contains(name)) {
				rule.skipNode();
			}

		}

		return rule;
	}

	private static void parse(String string) throws Exception {

		System.out.println("--------------------------------------------");
		System.out.println(" script : " + string);
		System.out.println("--------------------------------------------");

		ParseRunner<?> runner = ParseUtils.createParseRunner(false, PegJavaScriptParser.class);
//		ParseRunner<?> runner = ParseUtils.createParseRunner(true, PegJavaScriptParser.class);

		ParsingResult<?> result = runner.run(string);

		printTree(result);

		System.out.println("----------------------------------------------");

		System.out.println("public class JavaWrapper { \n");
		System.out.println("  public static void main(String[] args) { \n");

		PegJS2JavaTranspiler.transpile(result, result.parseTreeRoot, 0);

		System.out.println("\n  } ");
		System.out.println("\n}");

	}

	public static void printTree(ParsingResult<?> result) {

		ParseUtils.visitTree(result.parseTreeRoot, (node, level) -> {
			if (HIDE_LABELS.contains(node.getLabel()))
				return true;
			System.out.print(level + " : ");
			for (int i = 0; i < level; i++) {
				System.out.print(" ");
			}
			String value = ParseTreeUtils.getNodeText(node, result.inputBuffer).trim();
			System.out.println(node.getLabel() + " : " + value);
			return true;
		});
	}

	public static void main(String[] args) throws Exception {

//		parse(" 22 + ( 33 * 44 ) / 55 ");
//		parse(" log('Hello', 'World') ");
//		parse(" console.log('Hello', 'World') ");
		
		parse(read("http-demo.js"));
//		parse(read("express-demo.js"));

	}

	public static String read(String file) throws Exception {
		Path path = Paths.get(PegJavaScriptParser.class.getClassLoader().getResource(file).toURI());
		try (Stream<String> lines = Files.lines(path)) {
			return lines.collect(Collectors.joining("\n"));
		}
	}

}