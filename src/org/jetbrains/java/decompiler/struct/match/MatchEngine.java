// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.match;

import org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement.StatementType;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.match.IMatchable.MatchProperties;
import org.jetbrains.java.decompiler.struct.match.MatchNode.RuleValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MatchEngine {
  @SuppressWarnings("SpellCheckingInspection")
  private static final Map<String, MatchProperties> stat_properties;

  @SuppressWarnings("SpellCheckingInspection")
  private static final Map<String, MatchProperties> expr_properties;

  @SuppressWarnings("SpellCheckingInspection")
  private static final Map<String, StatementType> stat_type;

  private static final Map<String, Integer> expr_type;

  private static final Map<String, Integer> expr_func_type;

  private static final Map<String, Integer> expr_exit_type;

  @SuppressWarnings("SpellCheckingInspection")
  private static final Map<String, Integer> stat_if_type;

  private static final Map<String, VarType> expr_const_type;

  static {
    Map<String, MatchProperties> statProperties = new HashMap<>();
    statProperties.put("type", MatchProperties.STATEMENT_TYPE);
    statProperties.put("ret", MatchProperties.STATEMENT_RET);
    statProperties.put("position", MatchProperties.STATEMENT_POSITION);
    statProperties.put("statsize", MatchProperties.STATEMENT_STATSIZE);
    statProperties.put("exprsize", MatchProperties.STATEMENT_EXPRSIZE);
    statProperties.put("iftype", MatchProperties.STATEMENT_IFTYPE);
    stat_properties = Collections.unmodifiableMap(statProperties);

    Map<String, MatchProperties> exprProperties = new HashMap<>();
    exprProperties.put("type", MatchProperties.EXPRENT_TYPE);
    exprProperties.put("ret", MatchProperties.EXPRENT_RET);
    exprProperties.put("position", MatchProperties.EXPRENT_POSITION);
    exprProperties.put("functype", MatchProperties.EXPRENT_FUNCTYPE);
    exprProperties.put("exittype", MatchProperties.EXPRENT_EXITTYPE);
    exprProperties.put("consttype", MatchProperties.EXPRENT_CONSTTYPE);
    exprProperties.put("constvalue", MatchProperties.EXPRENT_CONSTVALUE);
    exprProperties.put("invclass", MatchProperties.EXPRENT_INVOCATION_CLASS);
    exprProperties.put("signature", MatchProperties.EXPRENT_INVOCATION_SIGNATURE);
    exprProperties.put("parameter", MatchProperties.EXPRENT_INVOCATION_PARAMETER);
    exprProperties.put("index", MatchProperties.EXPRENT_VAR_INDEX);
    exprProperties.put("name", MatchProperties.EXPRENT_FIELD_NAME);
    expr_properties = Collections.unmodifiableMap(exprProperties);

    Map<String, StatementType> statType = new HashMap<>();
    statType.put("if", StatementType.IF);
    statType.put("do", StatementType.DO);
    statType.put("switch", StatementType.SWITCH);
    statType.put("trycatch", StatementType.TRY_CATCH);
    statType.put("basicblock", StatementType.BASIC_BLOCK);
    statType.put("sequence", StatementType.SEQUENCE);
    stat_type = Collections.unmodifiableMap(statType);

    Map<String, Integer> exprType = new HashMap<>();
    exprType.put("array", Exprent.EXPRENT_ARRAY);
    exprType.put("assignment", Exprent.EXPRENT_ASSIGNMENT);
    exprType.put("constant", Exprent.EXPRENT_CONST);
    exprType.put("exit", Exprent.EXPRENT_EXIT);
    exprType.put("field", Exprent.EXPRENT_FIELD);
    exprType.put("function", Exprent.EXPRENT_FUNCTION);
    exprType.put("if", Exprent.EXPRENT_IF);
    exprType.put("invocation", Exprent.EXPRENT_INVOCATION);
    exprType.put("monitor", Exprent.EXPRENT_MONITOR);
    exprType.put("new", Exprent.EXPRENT_NEW);
    exprType.put("switch", Exprent.EXPRENT_SWITCH);
    exprType.put("var", Exprent.EXPRENT_VAR);
    exprType.put("annotation", Exprent.EXPRENT_ANNOTATION);
    exprType.put("assert", Exprent.EXPRENT_ASSERT);
    expr_type = Collections.unmodifiableMap(exprType);

    Map<String, Integer> exprFuncType = new HashMap<>();
    exprFuncType.put("eq", FunctionExprent.FUNCTION_EQ);
    expr_func_type = Collections.unmodifiableMap(exprFuncType);

    Map<String, Integer> exprExitType = new HashMap<>();
    exprExitType.put("return", ExitExprent.EXIT_RETURN);
    exprExitType.put("throw", ExitExprent.EXIT_THROW);
    expr_exit_type = Collections.unmodifiableMap(exprExitType);

    Map<String, Integer> statIfType = new HashMap<>();
    statIfType.put("if", IfStatement.IFTYPE_IF);
    statIfType.put("ifelse", IfStatement.IFTYPE_IFELSE);
    stat_if_type = Collections.unmodifiableMap(statIfType);

    Map<String, VarType> exprConstType = new HashMap<>();
    exprConstType.put("null", VarType.VARTYPE_NULL);
    exprConstType.put("string", VarType.VARTYPE_STRING);
    expr_const_type = Collections.unmodifiableMap(exprConstType);
  }

  private final MatchNode rootNode;
  private final Map<String, Object> variables = new HashMap<>();

  public MatchEngine(String description) {
    // each line is a separate statement/expression
    String[] lines = description.split("\n");

    int depth = 0;
    LinkedList<MatchNode> stack = new LinkedList<>();

    for (String line : lines) {
      List<String> properties = new ArrayList<>(Arrays.asList(line.split("\\s+"))); // split on any number of whitespaces
      if (properties.get(0).isEmpty()) {
        properties.remove(0);
      }

      int node_type = "statement".equals(properties.get(0)) ? MatchNode.MATCHNODE_STATEMENT : MatchNode.MATCHNODE_EXPRENT;

      // create new node
      MatchNode matchNode = new MatchNode(node_type);
      for (int i = 1; i < properties.size(); ++i) {
        String[] values = properties.get(i).split(":");

        MatchProperties property = (node_type == MatchNode.MATCHNODE_STATEMENT ? stat_properties : expr_properties).get(values[0]);
        if (property == null) { // unknown property defined
          throw new RuntimeException("Unknown matching property");
        }
        else {
          Object value;
          int parameter = 0;

          String strValue = values[1];
          if (values.length == 3) {
            parameter = Integer.parseInt(values[1]);
            strValue = values[2];
          }

          switch (property) {
            case STATEMENT_TYPE:
              value = stat_type.get(strValue);
              break;
            case STATEMENT_STATSIZE:
            case STATEMENT_EXPRSIZE:
              value = Integer.valueOf(strValue);
              break;
            case STATEMENT_POSITION:
            case EXPRENT_POSITION:
            case EXPRENT_INVOCATION_CLASS:
            case EXPRENT_INVOCATION_SIGNATURE:
            case EXPRENT_INVOCATION_PARAMETER:
            case EXPRENT_VAR_INDEX:
            case EXPRENT_FIELD_NAME:
            case EXPRENT_CONSTVALUE:
            case STATEMENT_RET:
            case EXPRENT_RET:
              value = strValue;
              break;
            case STATEMENT_IFTYPE:
              value = stat_if_type.get(strValue);
              break;
            case EXPRENT_FUNCTYPE:
              value = expr_func_type.get(strValue);
              break;
            case EXPRENT_EXITTYPE:
              value = expr_exit_type.get(strValue);
              break;
            case EXPRENT_CONSTTYPE:
              value = expr_const_type.get(strValue);
              break;
            case EXPRENT_TYPE:
              value = expr_type.get(strValue);
              break;
            default:
              throw new RuntimeException("Unknown matching property");
          }

          matchNode.addRule(property, new RuleValue(parameter, value));
        }
      }

      if (stack.isEmpty()) { // first line, root node
        stack.push(matchNode);
      }
      else {
        // return to the correct parent on the stack
        int new_depth = line.lastIndexOf(' ', depth) + 1;
        for (int i = new_depth; i <= depth; ++i) {
          stack.pop();
        }

        // insert new node
        stack.getFirst().addChild(matchNode);
        stack.push(matchNode);

        depth = new_depth;
      }
    }

    this.rootNode = stack.getLast();
  }

  public boolean match(IMatchable object) {
    variables.clear();
    return match(this.rootNode, object);
  }

  private boolean match(MatchNode matchNode, IMatchable object) {
    if (!object.match(matchNode, this)) {
      return false;
    }

    int expr_index = 0;
    int stat_index = 0;
    for (MatchNode childNode : matchNode.getChildren()) {
      boolean isStatement = childNode.getType() == MatchNode.MATCHNODE_STATEMENT;

      IMatchable childObject = object.findObject(childNode, isStatement ? stat_index : expr_index);
      if (childObject == null || !match(childNode, childObject)) {
        return false;
      }

      if (isStatement) {
        stat_index++;
      }
      else {
        expr_index++;
      }
    }

    return true;
  }

  public boolean checkAndSetVariableValue(String name, Object value) {
    Object old_value = variables.get(name);
    if (old_value != null) {
      return old_value.equals(value);
    }
    else {
      variables.put(name, value);
      return true;
    }
  }

  public Object getVariableValue(String name) {
    return variables.get(name);
  }
}
