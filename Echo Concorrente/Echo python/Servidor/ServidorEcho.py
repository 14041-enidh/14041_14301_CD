# ServidorEcho.py

import sys, getopt
import socket

from threading import Thread

DefaultPort = 12345

def ServidorDedicado(cliConnection, cliAddress):
    print( "Starting thread to handle data from {}".format(cliAddress) )

    with cliConnection:
        while True :
            inputData = cliConnection.recv( 1024 )

            if not inputData :
                break

            cliConnection.sendall( inputData.upper() )

        print( "Connection ended!" )
    cliConnection.close()

    print( "Thread to handle data from {} is ending".format(cliAddress) )

def usage() :
    print( "ServidorEcho.py [--port <server port number>] [--interactive]" )

def startServer( portNumber ) :
    print( "Starting Echo server on port {}".format(portNumber) )

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind( ( "localhost" , portNumber ) )
        while True :
            s.listen()
            conn, addr = s.accept()
            print( "New connection ({})".format( addr ) )

            tt = Thread( target=ServidorDedicado, args=(conn, addr,) )
            tt.start()

def parseArguments(argv) :
    print( "Parsing arguments..." )

    try:
        opts, args = getopt.getopt( argv, "h", ["help", "port="] )
    except getopt.GetoptError as err:
        # print help information and exit:
        print( err )
        sys.exit( 2 )

    portNumber = DefaultPort

    for opt, arg in opts:
        if opt in ( "-h", "--help" ) :
            usage()
            sys.exit()
        
        if opt in ( "--port" ) :
            portNumber = int(arg)
    
    startServer( portNumber )

if __name__ == "__main__":
    parseArguments( sys.argv[1:] )
