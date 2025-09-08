package com.JLox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.nio.charset.StandardCharsets;

import com.JLox.parser.Expr;
import com.JLox.parser.Stmt;
import com.JLox.scanner.Token;
import com.JLox.scanner.TokenType;
import com.tool.AstPrinter;
import com.tool.RuntimeError;


public class Main {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    public static void main(String args[]) throws IOException {

        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte bytes[] = Files.readAllBytes(Paths.get(path));
        // run(new String(bytes, Charset.defaultCharset()));
        run(new String(bytes, StandardCharsets.UTF_8));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    // private static void runPrompt() throws IOException {
    //     InputStreamReader input = new InputStreamReader(System.in);
    //     BufferedReader reader = new BufferedReader(input);
      
    //     StringBuilder multilineBuffer = new StringBuilder();
    //     int openBraces = 0;
      
    //     while (true) {
    //       if (openBraces == 0) {
    //         System.out.print(">>> "); // new statement
    //       } else {
    //         System.out.print("... "); // inside a block
    //       }
      
    //       String line = reader.readLine();
    //       if (line == null) break; // Ctrl+D / EOF
    //       if (line.trim().equalsIgnoreCase("exit();"))
    //         {
    //             // break;
    //             System.exit(0);
    //         }
      
    //       multilineBuffer.append(line).append("\n");
      
    //       // Count '{' and '}' to detect if we're inside a block
    //       for (char c : line.toCharArray()) {
    //         if (c == '{') openBraces++;
    //         else if (c == '}') openBraces--;
    //       }
      
    //       // Only interpret when braces are balanced
    //       if (openBraces <= 0) {
    //         run(multilineBuffer.toString());
    //         multilineBuffer.setLength(0); // clear buffer
    //         openBraces = 0;
    //       }

    //     }
    //   }

    private static void runPrompt() throws IOException {
      Terminal terminal = TerminalBuilder.builder()
              .system(true)
              .build();
  
      // JLox keywords for autocompletion
      LineReader reader = LineReaderBuilder.builder()
              .terminal(terminal)
              .completer(new StringsCompleter(
                      "and", "class", "else", "false", "for",
                      "fun", "if", "nil", "or", "print",
                      "return", "super", "this", "true", "var",
                      "while", "break", "continue"
              ))
              .build();
  
      StringBuilder multilineBuffer = new StringBuilder();
      int openBraces = 0;
  
      while (true) {
          try {
              String prompt = (openBraces == 0) ? "jlox> " : "... ";
              String line = reader.readLine(prompt);
  
              if (line == null) break; // EOF (Ctrl+D)
              if (line.trim().equalsIgnoreCase("exit") || line.trim().equalsIgnoreCase("exit();")) {
                  System.out.println("Goodbye!");
                  break;
              }
  
              multilineBuffer.append(line).append("\n");
  
              // Count braces to detect blocks
              for (char c : line.toCharArray()) {
                  if (c == '{') openBraces++;
                  else if (c == '}') openBraces--;
              }
  
              // Only interpret when braces are balanced
              if (openBraces <= 0) {
                  run(multilineBuffer.toString());
                  multilineBuffer.setLength(0); // clear buffer
                  openBraces = 0;
              }
  
          } catch (UserInterruptException e) {
              // Ctrl+C → don’t quit immediately
              System.out.println("^C (use 'exit' to quit)");
          } catch (EndOfFileException e) {
              // Ctrl+D
              break;
          }
      }
  }
  

      

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;

        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.Eof) {
        report(token.line, " at end", message);
        } else {
        report(token.line, " at '" + token.lexeme + "'",  message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
