package com.JLox;

import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import com.JLox.scanner.TokenType;
import com.JLox.parser.Expr;
import com.JLox.parser.Stmt;
import com.JLox.scanner.Token;
import static com.JLox.scanner.TokenType.*;

public class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    // try {
      //   return expression();
      // } catch (ParseError error) {
        //   return null;
        // }
        
      List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
          statements.add(declaration());
        }
        return statements;
      }

  private Expr expression() {
    // return equality();
    return assignment();
  }

  private Stmt declaration() {
    try {
      if (match(Var)) return varDeclaration();
      
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt statement() {
    if(match(If)) return ifStatement();
    if (match(Print)) return printStatement();
    if (match(While)) return whileStatement();
    if (match(For)) return forStatement(); 
    if (match(LeftBrace)) return new Stmt.Block(block());

    return expressionStatement();
  }

  private Stmt ifStatement() {
    consume(LeftParen, "Expect '(' after 'if'.");
    Expr condition = expression();
    consume(RightParen, "Expect ')' after if condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(Else)) {
      elseBranch = statement();
    }
    return new Stmt.If(condition, thenBranch, elseBranch);
  }
  
  private Stmt whileStatement() {
    consume(LeftParen, "Expext '(' after 'while'.");
    Expr condition = expression();
    consume(RightParen, "Expect ')' after condition.");

    Stmt body = statement();
    return new Stmt.While(condition, body);
  }

  private Stmt forStatement() {
    consume(LeftParen, "Expect '(' after 'for'.");
    
    Stmt initializer;
    if (match(Semicolon)) {
      initializer = null;
    } else if (match(Var)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(Semicolon)) {
      condition = expression();
    }
    consume(Semicolon, "Expect ';' after loop condition.");

    Expr increment  = null;
    if (!check(RightParen)) {
      increment = expression();
    }
    consume(RightParen, "Expect ')' after for clauses.");

    Stmt body = statement();

    if (increment != null) {
      body = new Stmt.Block(
        Arrays.asList(
          body,
          new Stmt.Expression(increment))
        );
    }

    if (condition == null) condition = new Expr.Literal(true);
    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }
    
    return body;
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(Semicolon, "Expect ';' after value.");
    return new Stmt.Print(value);
  }

  private Stmt varDeclaration() {
    Token name = consume(TokenType.Identifier, "Expect variable name");

    Expr initializer = null;
    if(match(Equal)) {
      initializer = expression();
    }

    consume(Semicolon, "Expect ';' after variable declaration");
    return new Stmt.Var(name, initializer);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(Semicolon, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while(!check(RightBrace) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(RightBrace, "Expect '}' after block");
    return statements;
  }

  private Expr assignment() {
    Expr expr = or();

    if(match(Equal)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable)expr).name;
        return new Expr.Assign(name, value);
      }

    error(equals, "Invalid assignment target.");
    }

    return expr;
  }

  private Expr or() {
    Expr expr = and();

    if(match(Or)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }
    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    if(match(And)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }
    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();
    
    while (match(BangEqual, EqualEqual))  {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while(match(Greater, GreaterEqual, Less, LessEqual)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while(match(Plus, Minus)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while(match(Slash, Star)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr unary() {
    if(match(Bang, Minus)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }
    return primary();
  }

  private Expr primary() {
    if(match(False)) return new Expr.Literal(false);
    if(match(True)) return new Expr.Literal(true);
    if(match(Nil)) return new Expr.Literal(null);

    if(match(Number, String)) {
      return new Expr.Literal(previous().literal);
    }

    if(match(Identifier)) {
      return new Expr.Variable(previous());
    }

    if(match(LeftParen)) {
      Expr expr = expression();
      consume(RightParen, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if(check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private Token consume(TokenType type, String message) {
    if(check(type)) return advance();

    throw error(peek(), message);
  }

  private ParseError error(Token token, String message) {
    Main.error(token, message);
    return new ParseError();
  }

  
  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == Semicolon) return;

      switch (peek().type) {
        case Class:
        case Fun:
        case Var:
        case For:
        case If:
        case While:
        case Print:
        case Return:
          return;
      }

      advance();
    }
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == Eof;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current-1);
  }
}
