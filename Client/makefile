CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread

# CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude -lpthread -lboost_thread
# LDFLAGS:=-lboost_system -lpthread -lboost_thread

all: BGRSclient
	g++ -o bin/BGRSclient bin/ConnectionHandler.o bin/Main.o bin/ReadFromKeyBoard.o bin/ProtocolImpl.o $(LDFLAGS)

BGRSclient: bin/ConnectionHandler.o bin/Main.o bin/ReadFromKeyBoard.o bin/ProtocolImpl.o

bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/Main.o: src/Main.cpp
	g++ $(CFLAGS) -o bin/Main.o src/Main.cpp

bin/ReadFromKeyBoard.o: src/ReadFromKeyBoard.cpp
	g++ $(CFLAGS) -o bin/ReadFromKeyBoard.o src/ReadFromKeyBoard.cpp

bin/ProtocolImpl.o: src/ProtocolImpl.cpp
	g++ $(CFLAGS) -o bin/ProtocolImpl.o src/ProtocolImpl.cpp

.PHONY: clean
clean:
	rm -f bin/*

