all: compile run

run:
	java -cp bin com.JLox.Main

ast-printer:
	java bin/com/tool/AstPrinter

compile: 
	javac -d bin src/com/JLox/**/*.java src/com/JLox/*.java src/com/tool/*.java

clean:
	del bin & mkdir bin