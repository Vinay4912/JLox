# FILE ?= 

# all: compile run

# run:
# ifeq ($(FILE),)
# 	java -cp bin com.JLox.Main
# else
# 	java -cp bin com.JLox.Main $(FILE)
# endif

# ast-printer:
# 	java -cp bin com.tool.AstPrinter

# compile: 
# 	javac -d bin src/com/JLox/**/*.java src/com/JLox/*.java src/com/tool/*.java

# clean:
# 	del bin & mkdir bin


FILE ?= 
JLINE_JAR = lib/jline-3.25.1.jar
BIN = bin

all: compile run

run:
ifeq ($(FILE),)
	java -cp "bin;lib/*" com.JLox.Main
else
	java -cp "bin;lib/*" com.JLox.Main $(FILE)
endif



ast-printer:
	java -cp "$(BIN);$(JLINE_JAR)" com.tool.AstPrinter

compile:
	javac -cp "lib/*;." -d bin src/com/JLox/**/*.java src/com/JLox/*.java src/com/tool/*.java

clean:
	del /Q $(BIN) & mkdir $(BIN)

