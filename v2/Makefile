JC = javac
RUN = java
RMRF = rm -rf

all: build

build:
	find -name "*.java" > javaSources
	$(JC) @javaSources

clean:
	$(RMRF) *.class