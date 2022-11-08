package org.parboiled.json;

import java.util.List;

import org.parboiled.Node;
import org.parboiled.support.ParsingResult;
import org.parboiled.util.ParseUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PegJS2JavaTranspiler {

	public static void transpile(ParsingResult<?> result, Node<?> parentNode, int parentLevel) {

		ParseUtils.visitTree((List) parentNode.getChildren(), parentLevel, (childNode, level) -> {

//			if ("Statement".equals(childNode.getLabel())) {
//
////				for (int i = 0; i < level; i++) {
////					System.out.print("   ");
////				}
//
//				transpile(result, childNode, level + 1);
//
////				if (!value.trim().endsWith(";")) {
////					System.out.print(";");
////				}
//
//				return false;
//
//			}  else 
			if ("VarToken".equals(childNode.getLabel())) {

				System.out.print("Object");

			} else if ("FunctionExpression".equals(childNode.getLabel())) {

				System.out.print("new BiConsumer() { public void accept");
				transpile(result, childNode, level + 1);
				System.out.print("}");
				return false;

			} else if ("FunctionToken".equals(childNode.getLabel())) {
				// NoOp
			} else if ("FormalParameterList".equals(childNode.getLabel())) {

				ParseUtils.applyChildren((List) childNode.getChildren(), parentLevel, (n, l) -> {
					String value = result.inputBuffer.extract(n.getStartIndex(), n.getEndIndex());
					if ("Identifier".equals(n.getLabel())) {
						System.out.print("Object ");
					}
					System.out.print(value);
					return false;
				});
				return false;

			} else if ("StringLiteral".equals(childNode.getLabel())) {

				String value = result.inputBuffer.extract(childNode.getStartIndex() + 1, childNode.getEndIndex() - 1);
				System.out.print("\"" + value + "\"");

			} else if ("ObjectLiteral".equals(childNode.getLabel())) {

				String value = result.inputBuffer.extract(childNode.getStartIndex(), childNode.getEndIndex());
				System.out.print("\"" + value + "\"");

			} else if (childNode.getChildren().isEmpty()) {

				String value = result.inputBuffer.extract(childNode.getStartIndex(), childNode.getEndIndex());
				System.out.print(value);
			}

			return true;
		});

	}

}
