JC = javac
RUN = java
RMRF = rm -rf

default: build run

build: clean
	@find -name "*.java" > javaSources
	@$(JC) @javaSources
	@$(RMRF) javaSources

run: build
	@$(RUN) TeamGetNameACoolName

clean:
	@$(RMRF) *.class