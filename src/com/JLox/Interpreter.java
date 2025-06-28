package com.JLox;

import java.util.List;

import com.JLox.interpreter.Environment;
import com.JLox.parser.Expr;
import com.JLox.scanner.Token;
import com.tool.RuntimeError;
import com.JLox.parser.Stmt;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  private Environment environment = new Environment();

  void interpret(List<Stmt> statements) {
    // try {
    //   Object value = evaluate(expression);
    //   System.out.println(stringify(value));
    // } catch (RuntimeError error) {
    //    Main.runtimeError(error);
    // }

    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Main.runtimeError(error);
    }
  }
  
  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case Bang:
        return !isTruthy(right);
      case Minus:
        checkNumberOperand(expr.operator, right);
        return -(double)right;
    }

    // Unreachable
    return null;
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return environment.get(expr.name);
  }

  private void checkNumberOperand(Token operator, Object operand) {
    if(operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number");
  }
  
  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }
  
  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;
    try {
      this.environment = environment;

      for (Stmt statememt : statements) {
        execute(statememt);
      }
    } finally {
      this.environment = previous;
    }
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override 
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }

  @Override
    public Void visitVarStmt(Stmt.Var stmt) {
      Object value = null;
      if (stmt.initializer != null) {
        value = evaluate(stmt.initializer);
      }
      environment.define(stmt.name.lexeme, value);
      return null;
  }

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);
    environment.assign(expr.name, value);

    return value;
  }

  
  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);
    
    switch (expr.operator.type) {
      case Greater:
      checkNumberOperand(expr.operator, left, right);
      return (double)left > (double)right;
      case GreaterEqual:
      checkNumberOperand(expr.operator, left, right);
      return (double)left >= (double)right;
      case Less:
      checkNumberOperand(expr.operator, left, right);
      return (double)left < (double)right;
      case LessEqual:
      checkNumberOperand(expr.operator, left, right);
      return (double)left <= (double)right;
      case Plus:
      if (left instanceof Double && right instanceof Double) {
        return (double)left + (double)right;
      }
      if (left instanceof String && right instanceof String) {
        return (String)left + (String)right;
      }
      throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
      case Minus:
      checkNumberOperand(expr.operator, left, right);
      return (double)left - (double)right;
      case Star:
      checkNumberOperand(expr.operator, left, right);
      return (double)left * (double)right;
      case Slash:
      checkNumberOperand(expr.operator, left, right);
      return (double)left / (double)right;
      case BangEqual: return !isEqual(left,right);
      case EqualEqual: return isEqual(left,right);
    }
    
    // Unreachable
    return null;
  }

  private void checkNumberOperand(Token operator, Object left, Object right) {
    if(left instanceof Double && right instanceof Double) return;
    throw new RuntimeError(operator, "Operands must be numbers.");
  }

  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if(a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  private String stringify(Object object) {
    if(object == null) return "nil";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length()-2);
      }
      return text;
    }
    return object.toString();
  }
}
