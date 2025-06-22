all:
	javac -d bin src/com/JLox/**/*.java src/com/JLox/*.java

clean:
	del bin & mkdir bin