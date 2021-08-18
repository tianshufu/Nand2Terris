// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.
// Let i = 0
@i
M=0
//intial the basci param screen
@SCREEN
D=A
@addr
M=D 

//get the val from the key board
(LOOP)
@KBD
D=M
//if there is pressed button
@Black
D;JGT // if key>0 jump to black
@white
D;JEQ // if key==0 jump to white


(Black)
// Let n = 8192  the num of rows
@8192
D=A
@n
M=D

//update  D-i 
@i
D=M
@n
D=D-M
@LOOP
D;JEQ
// RAM[arr+i] = -1
@addr
D=M
@i
A=D+M
M=-1
// i++
@i
M=M+1
//back to the entry
@LOOP
0;JMP

(white)
//get the value of i to see if need to reset
@i 
D=M
@Reset
D;JLT
//set the value to 0

@addr
D=M
@i
A=D+M
M=0
// i--
@i
M=M-1
//get back to the entry
@LOOP
0;JMP


// set i back to 0
(Reset)
@i 
M=0
@LOOP
0;JMP




