FILE ?= 

all: compile run

run:
ifeq ($(FILE),)
	java -cp bin com.JLox.Main
else
	java -cp bin com.JLox.Main $(FILE)
endif

ast-printer:
	java -cp bin com.tool.AstPrinter

compile: 
	javac -d bin src/com/JLox/**/*.java src/com/JLox/*.java src/com/tool/*.java

clean:
	del bin & mkdir bin
