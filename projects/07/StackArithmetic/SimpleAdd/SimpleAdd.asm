//Add two num

//push constant 7
@7
D=A 
@SP 
A=M
M=D 
@SP
M=M+1
//push constant 8
@8
D=A 
@SP 
A=M
M=D 
@SP
M=M+1
//add 7+8
//get the pointer value : 8
@SP 
//update the pointer
AM=M-1
//Get the value of the pointer currently points at 
D=M 
//Keep moving the pointer to get the next value 
@SP 
AM=M-1 
M=D+M
//move up the pointer 
@SP
M=M+1



