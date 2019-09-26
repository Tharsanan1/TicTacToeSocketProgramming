# TicTacToeSocketProgramming

The program has two main part, the server and the client. The server
handles the game. It controls moves and events and checks moves and result. The server is available
through a specified TCP port on specified machine by TicTacToe client. The server communicates the
clients through socket connection and prints information such as player connected, player move and so
on, to standard output. Only two player can connect to server at same time and they can play with each
other. If any additional player try to connect to server, he will be refused. After the game each player
going to be disconnected and they have to connect again to play another game. The first connected
player’s mark is ‘X’ and the second one’s is ‘O’. The game starts when both player ready for game,
therefore both is connected. The game goes on 6x6 grid board and each player put down own mark to
one free square in each round. The winner is who has three mark side by side down, across or diagonal.
Player ‘X’ begins game after that they take turns at moving until someone win the game or the board is
full.
