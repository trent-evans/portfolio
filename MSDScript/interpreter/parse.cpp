//
// Created by Trent Evans on 1/21/20.
//

#include <iostream>
#include <sstream>

#include "parse.h"
#include "catch.hpp"
#include "expr.h"
#include "value.h"
#include "env.h"

// Forward declarations of the functions so they can be called elsewhere as necessary
static PTR(Expr)  parseExpr(std::istream &in);
static PTR(Expr)  parseAddend(std::istream &in);
static PTR(Expr)  parseMulticand(std::istream &in);
static PTR(Expr)  parseInner(std::istream &in);
static PTR(Expr)  parseNumber(std::istream &in);
static PTR(Expr)  parseVariable(std::istream &in);
static PTR(Expr)  parseLet(std::istream &in);
static PTR(Expr)  parseIf(std::istream &in);
static PTR(Expr)  parseFun(std::istream &in);
static char peekNextCharacter(std::istream &in);
static std::string parseKeyword(std::istream &in);
static std::string parseWord(std::istream &in);
static PTR(Expr)  parseString(std::string s);

/**
 * parseInStream is the driver function for parsing the instream expression provided
 * @param in - an instream of a complete mathematical expression
 * @return an Expr object that represents the expression tree of the instream
 */
PTR(Expr)  parseInStream(std::istream &in) {
    PTR(Expr)  expression = parseExpr(in);

    char c = peekNextCharacter(in);
    if ((c != '\n') && !in.eof()) {
        std::cerr << "expected an end of line\n";
        return NULL;
    }
    return expression;
}


/**
 * parseExpr takes in an input stream that begins with an expression and returns the interp of that expression contained
 * inside of an Expr object.  It consumes the largest initial expression possible to preserve the order of operations.
 * @param in - The instream beginning with an arithmetic expression
 * @return an Expr object of the current expression
 */
static PTR(Expr)  parseExpr(std::istream &in) {
    PTR(Expr)  expression = parseAddend(in);
    char c = peekNextCharacter(in); // = ...
    if(c == '='){
        in.get(); // Consume the '='
        if(peekNextCharacter(in) == '='){
            in.get(); // Consume the next '='
            return NEW(EqualsExpr)(expression,parseExpr(in));
        }else{
            std::cerr << "Expected == to determine an equivalence expression\n Please enter a valid equivalence expression\n";
            return NULL;
        }
    }else if (c == '+') {
        in.get();
        return NEW(AddExpr)(expression, parseExpr(in));
    }
    return expression;
}

/**
 * parseAddend takes in an input stream that begins with an addend and returns the interp of that addend contained inside
 * of an Expr class
 * @param in - The instream beginning with an arithmetic addend
 * @return the value of the parsed addend inside an Expr object
 */
static PTR(Expr)  parseAddend(std::istream &in) {
    PTR(Expr)  addend = parseMulticand(in);
    char c = peekNextCharacter(in); // = ...

    if (c == '*') {
        in.get();
        return NEW(MultExpr)(addend, parseAddend(in));
    }
    return addend;
}


/**
 * parseMulticand is used to parse out CallExpr objects in parsing.  In has a default output of a single inner expression
 * depnding on the input.
 * @param in - the instream containing the arithmetic sequence
 * @return a single expression OR a CallExpr, if such is the case
 */
static PTR(Expr)  parseMulticand(std::istream &in){
    PTR(Expr)  inner = parseInner(in);
    while(peekNextCharacter(in) == '('){
        in.get(); // Consume the '('
        PTR(Expr)  actual_arg = parseExpr(in);
        inner = NEW(CallExpr)(inner, actual_arg);
        if(peekNextCharacter(in) == ')'){
            in.get(); // Consume the close parenthesis
        }else{
            throw std::runtime_error("No close parenthesis in multicand\n");
        }
    }
    return inner;
}

/**
 * parseInner parses out either the next number, variable, addend, or keyword depending on the next character found
 * in the instream
 * Invalid inputs will trigger return NULL letting the program know that the arithmetic expression is invalid
 * @param in - The instream containing the arithmetic expression
 * @return an Expr that represents the next Number, Variable, or addend (parenthesis expression) in the instream
 */
static PTR(Expr)  parseInner(std::istream &in) {
    PTR(Expr)  value;

    char c = peekNextCharacter(in);

    if (c == '(') {
        in.get(); // Strip open parend
        value = parseExpr(in);
        // After that, there should be a `)`, and we're done
        if (peekNextCharacter(in) == ')') {
            in.get(); // Strip close parend
        } else {
            std::cerr << "expected a close parenthesis\n";
            return NULL;
        }
    } else if (c == '-' || isdigit(c)) {
        value = parseNumber(in);
    }else if (isalpha(c)) {
        value = parseVariable(in);
    }else if (c == '_'){
        in.get(); // Consume the underscore
        std::string keyword = parseKeyword(in);
        if(keyword == "let") {
            value = parseLet(in);
        }else if(keyword == "true"){
            value = NEW(BoolExpr)(true);
        }else if(keyword == "false") {
            value = NEW(BoolExpr)(false);
        }else if(keyword == "if") {
            value = parseIf(in);
        }else if(keyword == "fun"){
            value = parseFun(in);
        }else{
            std::cerr << "Unexpected keyword: " + keyword + "\nPlease enter a valid keyword\n";
        }
    } else {
        std::cerr << "expected a digit or open parenthesis at " << c << "\n";
        return NULL;
    }
    return value;
}

/**
 * parseNumber parses out the next number in from a valid arithmetic expression.  The number can only consist of numeric
 * characters and not contain any spaces between the digits.
 * @param in - The instream containing the arithmetic expresion
 * @return Expected return in a NumExpr with the single value parsed out
 */
static PTR(Expr) parseNumber(std::istream &in) {
    int number;
    in >> number;
    return NEW(NumExpr)(number);
}

/**
 * parseVariable calls parseWord which parses out the variable name from a valid arithmetic expression.  The
 * variable name can only consist of alphabetic characters without any spaces
 * @param in - The instream containing the arithmetic expresion
 * @return Expected return is a VarExpr containing the name parsed out in this function
 */
static PTR(Expr)  parseVariable(std::istream &in) {
    return NEW(VarExpr)(parseWord(in));
}

/**
 * parseKeyword calls parseWord which parses out the keyword from a valid arithmetic expression.
 * @param in - The instream containing the arithmetic expresion
 * @return Expected return is a std::string which should be a keyword
 */
static std::string parseKeyword(std::istream &in){
    return parseWord(in);
}


/**
 * parseWord which parses out the variable name from a valid arithmetic expression.  The
 * variable name can only consist of alphabetic characters without any spaces
 * @param in - The instream containing the arithmetic expresion
 * @return Expected return is a std::string which could be a keyword or a variable name
 */
static std::string parseWord(std::istream &in){
    std::string varName = "";
    char c = in.peek();
    while(isalpha(c)){
        varName += c;
        in.get();
        c = in.peek();
    }
    return varName;
}


/**
 * peekNextCharacter scans forward in the instream provided and removes all whitespace (including spaces, newlines, tabs
 *  etc.) and returns the next character without stripping it from the instream
 * @param in - The instream containing the arithmetic expression
 * @return Expected return is the character representation of a number, +, *, (, or )
 */
static char peekNextCharacter(std::istream &in) {
    char c;
    while (1) {
        c = in.peek();
        if (!isspace(c))
            break;
        c = in.get();
    }
    return c;
}


/**
 * parseLet is triggered by parsing out the keyword _let from the instream in parseInner
 *  The expected format is _let variable = Expr _in Expr
 *  Anything different will throw an error and return NULL
 * @param in - the instream containing the arithmetic subtitution expression.
 * @return An *Expr which represents the valid expression tree for a substitution expression
 */
static PTR(Expr)  parseLet(std::istream &in){
    peekNextCharacter(in); // Strip any possible whitespace before the next character
    std::string subsVarStr = parseWord(in);
    if(peekNextCharacter(in) != '='){
        std::cerr << "Expected '=' after _let variable \nPlease enter a valid _let sequence\n";
        return NULL;
    }
    in.get(); // Strip the = sign
    peekNextCharacter(in); // Strip any possible whitespace
    PTR(Expr)  subsExpr = parseExpr(in);
    if(peekNextCharacter(in) !='_'){
        std::cerr << "Expected '_in' after variable following '=' \nPlease enter a valid _let sequence\n";
        return NULL;
    }
    in.get(); // Consume the underscore
    if(parseKeyword(in) != "in"){
        std::cerr << "Expected '_in' keyword following '=' \nPlease enter a valid _let sequence\n";
        return NULL;
    }
    peekNextCharacter(in);
    PTR(Expr)  baseExpr = parseExpr(in);
    return NEW(LetExpr)(NEW(VarExpr)(subsVarStr),subsExpr,baseExpr);
}

/**
 * parseIf is triggeed by parsing _if out of the instream.  It returns an IfExpr object that contains each of the three
 * pieces of a conditional expression, assuming they are in the correct format
 *  The correct format is:
 *      _if PTR(Expr)  _then PTR(Expr)  _else PTR(Expr)
 * @param in - the instream containing the conditional expression
 * @return An *IfExpr object that contains the conditional expression, the then expression, and the else expression
 */
static PTR(Expr)  parseIf(std::istream &in){
    peekNextCharacter(in); // Strip any whitespace between _if and the Expr
    PTR(Expr)  testExpr = parseExpr(in);
    if(peekNextCharacter(in) != '_'){
        std::cerr << "Expected \"_then\" after expression following \"_if\" \n Please enter a valid conditional sequence\n";
        return NULL;
    }
    in.get(); // Consume the underscore
    if(parseKeyword(in) != "then"){
        std::cerr << "Expected \"_then\" after expression following \"_if\" \n Please enter a valid conditional sequence\n";
        return NULL;
    }
    peekNextCharacter(in); // Consume whitespace
    PTR(Expr)  thenExpr = parseExpr(in);
    if(peekNextCharacter(in) != '_'){
        std::cerr << "Expected \"_else\" after expression following \"_then\" \n Please enter a valid conditional sequence\n";
        return NULL;
    }
    in.get(); // Consume the underscore
    if(parseKeyword(in) != "else"){
        std::cerr << "Expected \"_else\" after expression following \"_then\" \n Please enter a valid conditional sequence\n";
        return NULL;
    }
    peekNextCharacter(in); // Consume whitespace
    PTR(Expr)  elseExpr = parseExpr(in);
    return NEW(IfExpr)(testExpr,thenExpr,elseExpr);
}

/**
 * parseFun parses out a FunExpr from a valid function sequence.  The expected sequence is:
 *                                  _fun (variable) expr
 * _fun being the keyword
 * a variable name being contained in parenthesis
 * and an expr representing the body of the function
 * @param in - the instream containing a valid function sequence
 * @return a *FunExpr containing the formal arg (functional variable) and the body of the function
 */
static PTR(Expr)  parseFun(std::istream &in){
    char c = peekNextCharacter(in);
    if(c != '('){
        std::cerr << "Expected parenthesis sequence containing a variable after _fun keyword \n Please enter a valid function sequence\n";
        return NULL;
    }
    in.get(); // Consume the (
    std::string formal_arg = parseWord(in);
    c = peekNextCharacter(in);
    if(c !=')'){
        std::cerr << "Expected parenthesis sequence containing a variable after _fun keyword \n Please enter a valid function sequence\n";
        return NULL;
    }
    in.get(); // Consume the )
    c = peekNextCharacter(in);
    PTR(Expr)  body = parseExpr(in);
    return NEW(FunExpr)(formal_arg,body);
}


/**
 * parseString makes testing of parseInStream much easier by taking in a string, converting it to an inStream, and then
 * passing the inStream to parseInStream
 * @param s - a string containing a complete mathematical expression
 * @return An *Expr that is the expression tree for the input stream
 */
static PTR(Expr)  parseString(std::string s) {
    std::istringstream in(s);
    return parseInStream(in);
}


// Tests currently live in this file, because they work here for some reason.

/*
 // Gave sufficient proof that the calculator works
 // But these must be commented out to make sure that the other ones run
 // Otherwise I get a fatal error and it's really sad :(
 // I do copy a lot of the tests down below and expand them though
TEST_CASE( "See if this works" ) {

    CHECK (parseStringError(" ( 1 ") == "expected a close parenthesis\n");

    CHECK(parseString("10") == 10);
    CHECK(parseString("(10)") == 10);
    CHECK(parseString("10+1") == 11);
    CHECK(parseString("(10+1)") == 11);
    CHECK(parseString("(10)+1") == 11);
    CHECK(parseString("10+(1)") == 11);
    CHECK(parseString("1+2*3") == 7);
    CHECK(parseString("1*2+3") == 5);
    CHECK(parseString("4*2*3") == 24);
    CHECK(parseString("4+2+3") == 9);
    CHECK(parseString("4*(2+3)") == 20);
    CHECK(parseString("(2+3)*4") == 20);

    CHECK (parseStringError("x") == "expected a digit or open parenthesis at x\n");
    CHECK (parseStringError("(1") == "expected a close parenthesis\n");

    CHECK(parseString(" 10 ") == 10);
    CHECK(parseString(" (  10 ) ") == 10);
    CHECK(parseString(" 10  + 1") == 11);
    CHECK(parseString(" ( 10 + 1 ) ") == 11);
    CHECK(parseString(" 11 * ( 10 + 1 ) ") == 121);
    CHECK(parseString(" ( 11 * 10 ) + 1 ") == 111);
    CHECK(parseString(" 1 + 2 * 3 ") == 7);

    CHECK (parseStringError(" x ") == "expected a digit or open parenthesis at x\n");
    CHECK (parseStringError(" ( 1 ") == "expected a close parenthesis\n");

    CHECK (parseString("(10+10*2+1)*(4+(2+1*2))+1") == 249);
}
*/

/*
TEST_CASE( "equals" ) {
    CHECK( (NEW(NumExpr)(1))->equals(NEW(NumExpr)(1)));
    CHECK( ! (NEW(NumExpr)(1))->equals(NEW(NumExpr)(2)));
    CHECK( ! (NEW(NumExpr)(1))->equals(NEW(MultExpr)(NEW(NumExpr)(2), NEW(NumExpr)(4))));
}

TEST_CASE("Single numbers"){
    CHECK(parseString("10")->equals(NEW(NumExpr)(10)));
    CHECK(parseString("(10)")->equals(NEW(NumExpr)(10)));
}

TEST_CASE("Basic addition"){
    CHECK(parseString("10+1")->equals(NEW(AddExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("42+2")->equals(NEW(AddExpr)(NEW(NumExpr)(42), NEW(NumExpr)(2))));
    CHECK(parseString("(10+1)")->equals(NEW(AddExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("(42+2)")->equals(NEW(AddExpr)(NEW(NumExpr)(42), NEW(NumExpr)(2))));
    CHECK(parseString("(10)+1")->equals(NEW(AddExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("10+(1)")->equals(NEW(AddExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("1+2+3")->equals(NEW(AddExpr)(NEW(NumExpr)(1), NEW(AddExpr)(NEW(NumExpr)(2), NEW(NumExpr)(3)))));
    // Now with spaces
    CHECK(parseString("10+ 1")->equals(NEW(AddExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("42 +  2")->equals(NEW(AddExpr)(NEW(NumExpr)(42), NEW(NumExpr)(2))));
    CHECK(parseString("( 10+ 1)")->equals(NEW(AddExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("(42 +2 )")->equals(NEW(AddExpr)(NEW(NumExpr)(42), NEW(NumExpr)(2))));
    CHECK(parseString(" (10)+1")->equals(NEW(AddExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("10  + ( 1)")->equals(NEW(AddExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
}

TEST_CASE("Basic multiplication"){
    CHECK(parseString("10*1")->equals(NEW(MultExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("42*2")->equals(NEW(MultExpr)(NEW(NumExpr)(42), NEW(NumExpr)(2))));
    CHECK(parseString("(10*1)")->equals(NEW(MultExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("(42*2)")->equals(NEW(MultExpr)(NEW(NumExpr)(42), NEW(NumExpr)(2))));
    CHECK(parseString("(10)*1")->equals(NEW(MultExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("10*(1)")->equals(NEW(MultExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    // Now with spaces
    CHECK(parseString("10* 1")->equals(NEW(MultExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("42 *  2")->equals(NEW(MultExpr)(NEW(NumExpr)(42), NEW(NumExpr)(2))));
    CHECK(parseString("( 10* 1)")->equals(NEW(MultExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("(42 *2 )")->equals(NEW(MultExpr)(NEW(NumExpr)(42), NEW(NumExpr)(2))));
    CHECK(parseString(" (10)*1")->equals(NEW(MultExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
    CHECK(parseString("10  * ( 1)")->equals(NEW(MultExpr)(NEW(NumExpr)(10), NEW(NumExpr)(1))));
}

TEST_CASE("Mixed statements"){
    CHECK(parseString("1 *2+  3")->equals( NEW(AddExpr)( NEW(MultExpr)( NEW(NumExpr)(1), NEW(NumExpr)(2)), NEW(NumExpr)(3))));
    CHECK(parseString("1* (2 +3)")->equals( NEW(MultExpr)( NEW(NumExpr)(1), NEW(AddExpr)( NEW(NumExpr)(2), NEW(NumExpr)(3)))));
    CHECK(parseString("(1+2) *(2*3)")->equals(NEW(MultExpr)(NEW(AddExpr)( NEW(NumExpr)(1), NEW(NumExpr)(2)), NEW(MultExpr)( NEW(NumExpr)(2), NEW(NumExpr)(3)))));
    CHECK(parseString("1+( 2*(3 +  2)  )")->equals(NEW(AddExpr)(NEW(NumExpr)(1), NEW(MultExpr)(NEW(NumExpr)(2), NEW(AddExpr)( NEW(NumExpr)(3), NEW(NumExpr)(2))))));
}

TEST_CASE("With variables"){
    // Simple adding and multiplying with variables
    CHECK(parseString("trent+msd")->equals(NEW(AddExpr)(NEW(VarExpr)("trent"), NEW(VarExpr)("msd"))));
    CHECK(parseString("trent*riches")->equals(NEW(MultExpr)(NEW(VarExpr)("trent"), NEW(VarExpr)("riches"))));
    // Going full bore for kicks
    CHECK(parseString("10*Zelda+Mario*9")->equals(NEW(AddExpr)(NEW(MultExpr)(NEW(NumExpr)(10), NEW(VarExpr)("Zelda")), NEW(MultExpr)(NEW(VarExpr)("Mario"), NEW(NumExpr)(9)))));
    CHECK(parseString("10  *Zelda +Mario* 9")->equals(NEW(AddExpr)(NEW(MultExpr)(NEW(NumExpr)(10), NEW(VarExpr)("Zelda")), NEW(MultExpr)(NEW(VarExpr)("Mario"), NEW(NumExpr)(9)))));
    // Epic proportions
    CHECK(parseString("(Pam +Jim  )* Dwight +  Michael*(Holly +10)")->equals
            (NEW(AddExpr)(NEW(MultExpr)(NEW(AddExpr)(NEW(VarExpr)("Pam"),NEW(VarExpr)("Jim")),NEW(VarExpr)("Dwight")),
                     NEW(MultExpr)(NEW(VarExpr)("Michael"),NEW(AddExpr)(NEW(VarExpr)("Holly"),NEW(NumExpr)(10))))));
}

TEST_CASE("Using interp"){
    CHECK(parseString("3")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(3)));
    CHECK(parseString("( 3  )")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(3)));
    CHECK(parseString("1+3")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(4)));
    CHECK(parseString("2*3")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(6)));
    CHECK(parseString("(1+2)*3")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(9)));
    CHECK(parseString("(1+2)*(2+3)")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(15)));
    CHECK(parseString("(1+2)*2+3")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(9)));
    CHECK(parseString("(1+3)+2*3")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(10)));
}

TEST_CASE("Substitute tests"){
    CHECK(parseString("3+Jim")->substitute("Jim", NEW(NumVal)(2))->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(5)));
    CHECK(parseString("2*Pam")->substitute("Pam", NEW(NumVal)(3))->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(6)));
    CHECK(parseString("4+Jim")->substitute("Dwight", NEW(NumVal)(2))->equals(NEW(AddExpr)(NEW(NumExpr)(4), NEW(VarExpr)("Jim"))));
    CHECK(parseString("Pam+Jim")->substitute("Pam", NEW(NumVal)(5))->equals(NEW(AddExpr)(NEW(NumExpr)(5), NEW(VarExpr)("Jim"))));
    CHECK(parseString("Pam*Jim")->substitute("Pam", NEW(NumVal)(5))->equals(NEW(MultExpr)(NEW(NumExpr)(5), NEW(VarExpr)("Jim"))));
    CHECK(parseString("Pam+Jim")->substitute("Dwight", NEW(NumVal)(5))->equals(NEW(AddExpr)(NEW(VarExpr)("Pam"), NEW(VarExpr)("Jim"))));
    CHECK(parseString("Pam*Jim")->substitute("Dwight", NEW(NumVal)(5))->equals(NEW(MultExpr)(NEW(VarExpr)("Pam"), NEW(VarExpr)("Jim"))));
}

TEST_CASE("Contains variable"){
    CHECK(!parseString("1+2")->containsVariable());
    CHECK(parseString("1+x")->containsVariable());
    CHECK(!parseString("1*2")->containsVariable());
    CHECK(parseString("1*x")->containsVariable());
}

TEST_CASE("simple _let examples"){
    CHECK(parseString("_let x = 5 _in x * 3")->equals(NEW(LetExpr)(NEW(VarExpr)("x"),NEW(NumExpr)(5),NEW(MultExpr)(NEW(VarExpr)("x"),NEW(NumExpr)(3)))));
    CHECK(parseString("_let x = 5 _in x * 3")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(15)));
    CHECK(parseString("_let x = 5 _in x + 3")->equals(NEW(LetExpr)(NEW(VarExpr)("x"),NEW(NumExpr)(5),NEW(AddExpr)(NEW(VarExpr)("x"),NEW(NumExpr)(3)))));
    CHECK(parseString("_let x = 5 _in x + 3")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(8)));
    CHECK(parseString("_let x = 5 _in x + x")->equals(NEW(LetExpr)(NEW(VarExpr)("x"),NEW(NumExpr)(5),NEW(AddExpr)(NEW(VarExpr)("x"),NEW(VarExpr)("x")))));
    CHECK(parseString("_let x = 5 _in x + x")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(10)));
    CHECK(parseString("_let x = 5 _in x + y")->equals(NEW(LetExpr)(NEW(VarExpr)("x"),NEW(NumExpr)(5),NEW(AddExpr)(NEW(VarExpr)("x"),NEW(VarExpr)("y")))));
    CHECK(parseString("_let x = 5 _in x * y")->equals(NEW(LetExpr)(NEW(VarExpr)("x"),NEW(NumExpr)(5),NEW(MultExpr)(NEW(VarExpr)("x"),NEW(VarExpr)("y")))));

}

TEST_CASE("basic optimize tests"){
    CHECK(parseString("10")->optimize()->equals(NEW(NumExpr)(10)));
    CHECK(parseString("x")->optimize()->equals(NEW(VarExpr)("x")));
    CHECK(parseString("10+5")->optimize()->equals(NEW(NumExpr)(15)));
    CHECK(parseString("10*5")->optimize()->equals(NEW(NumExpr)(50)));
    CHECK(parseString("10+x")->optimize()->equals(NEW(AddExpr)(NEW(NumExpr)(10), NEW(VarExpr)("x"))));
    CHECK(parseString("10*x")->optimize()->equals(NEW(MultExpr)(NEW(NumExpr)(10), NEW(VarExpr)("x"))));
    CHECK(parseString("10*3+x")->optimize()->equals(NEW(AddExpr)(NEW(NumExpr)(30), NEW(VarExpr)("x"))));
}

TEST_CASE("printToString() tests"){
    CHECK(parseString("10")->printToString() == "10");
    CHECK(parseString("test")->printToString() == "test");
    CHECK(parseString("10+20")->printToString() == "10 + 20");
    CHECK(parseString("10*20")->printToString() == "10 * 20");
    CHECK(parseString("1*2+3")->printToString() == "1 * 2 + 3");
    CHECK(parseString("1+2*3")->printToString() == "1 + 2 * 3");
    CHECK(parseString("_let x = 5 _in x*y")->printToString() == "_let x = 5 _in x * y");
}

TEST_CASE("optimize using _let"){

    CHECK(parseString("_let x = 5 _in 2 * 10")->optimize()->printToString() == "_let x = 5 _in 20");
    CHECK(parseString("(_let x = 5 _in x) + 3")->optimize()->equals(NEW(NumExpr)(8)));
    CHECK(parseString("(_let x = 5 _in x) + 3")->optimize()->printToString() == "8");
    CHECK(parseString("_let x = 5 _in 2 * x")->optimize()->equals(NEW(NumExpr)(10)));
    CHECK(parseString("_let x = 5 _in 2 * x")->optimize()->printToString() == "10");
    CHECK(parseString("_let x = 5 _in 2 * z")->optimize()->equals(NEW(MultExpr)(NEW(NumExpr)(2), NEW(VarExpr)("z"))));
    CHECK(parseString("_let x = 5 _in 2 * z")->optimize()->printToString() == "2 * z");
    CHECK(parseString("_let x = z _in x + 2")->optimize()->equals(NEW(LetExpr)(NEW(VarExpr)("x"), NEW(VarExpr)("z"), NEW(AddExpr)(NEW(VarExpr)("x"), NEW(NumExpr)(2)))));

}

TEST_CASE("Matthew's tests for optimize and substitute for let"){
    // More complicated optimize tests to make sure everything condenses as it should
    CHECK(parseString("_let x = 5 _in _let y = z + 2 _in x + y + (2 * 3)")->optimize()->printToString() ==
          "_let y = z + 2 _in 5 + y + 6");
    CHECK(parseString("_let z =(_let x = 5 _in x) _in (z+1)")->optimize()->printToString() == "6");
    CHECK(parseString("_let x = 10 _in _let x = 5 _in _let y = z _in x + y")->optimize()->printToString() == "_let y = z _in 5 + y");

    // Tests for lexical scope
    CHECK(parseString("_let y = y+1 _in y")->substitute("y",NEW(NumVal)(7))->printToString() == "_let y = 7 + 1 _in y");
    CHECK(parseString("_let y = 7 _in _let y = y+2 _in y")->optimize()->equals(NEW(NumExpr)(9)));
    CHECK(parseString("_let y = 7 _in _let y = y+2 _in y")->optimize()->printToString() == "9");
}

TEST_CASE("IfExpr tests"){
    // Value tests
    CHECK(parseString("_if _true _then 5 _else 6")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(5)));
    CHECK(parseString("_if _false _then 5 _else 6")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(6)));
    // Simplify tests
    CHECK(parseString("_if _true _then 5 _else 6")->optimize()->equals(NEW(NumExpr)(5)));
    CHECK(parseString("_if _false _then 5 _else 6")->optimize()->equals(NEW(NumExpr)(6)));
    // Substitute test
    CHECK(parseString("_if x _then y _else z")->substitute("x",NEW(NumVal)(5))->equals(NEW(IfExpr)(NEW(NumExpr)(5),NEW(VarExpr)("y"),NEW(VarExpr)("z"))));
    // Contains variable
    CHECK(parseString("_if x _then y _else z")->containsVariable());
    CHECK(!parseString("_if _true _then 5 _else 6")->containsVariable());
}

TEST_CASE("== tests"){
    // Basic == tests
    CHECK(parseString("5 == x")->equals(NEW(EqualsExpr)(NEW(NumExpr)(5),NEW(VarExpr)("x"))));
    CHECK(parseString("_false == 3 * x")->equals(NEW(EqualsExpr)(NEW(BoolExpr)(false),NEW(MultExpr)(NEW(NumExpr)(3),NEW(VarExpr)("x")))));
    CHECK(parseString("5 == 5")->interp(NEW(EmptyEnv)())->equals(NEW(BoolVal)(true)));
    CHECK(parseString("5 == 6")->interp(NEW(EmptyEnv)())->equals(NEW(BoolVal)(false)));
    // Expanded == tests
     CHECK(parseString("_if 5 == 2+3 _then _true _else _false")->interp(NEW(EmptyEnv)())->equals(NEW(BoolVal)(true)));
     CHECK(parseString("_if 5 == 2*3+1 _then _true _else _false")->interp(NEW(EmptyEnv)())->equals(NEW(BoolVal)(false)));
     CHECK(parseString("_if x == 5 _then z+2 _else y*7")->substitute("x", NEW(NumVal)(5))->optimize()->equals(NEW(AddExpr)(NEW(VarExpr)("z"), NEW(NumExpr)(2))));
     CHECK(parseString("_if x == 5 _then z+2 _else y*7")->substitute("x", NEW(NumVal)(6))->optimize()->equals(NEW(MultExpr)(NEW(VarExpr)("y"), NEW(NumExpr)(7))));
     CHECK(parseString("_if x == 5 _then x+2 _else y+7")->substitute("x", NEW(NumVal)(5))->optimize()->equals(NEW(NumExpr)(7)));
}

TEST_CASE("Negative tests"){
    CHECK(parseString("-1 + 2")->equals(NEW(AddExpr)(NEW(NumExpr)(-1),NEW(NumExpr)(2))));
    CHECK(parseString("-1 + 2")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(1)));
    CHECK(parseString("-1 * 2")->equals(NEW(MultExpr)(NEW(NumExpr)(-1),NEW(NumExpr)(2))));
    CHECK(parseString("-1 * 2")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(-2)));
}

TEST_CASE("Function tests"){

    // Parse test
    CHECK(parseString("_let f = _fun (x) x+1 _in f(10)")
    ->equals(NEW(LetExpr)(NEW(VarExpr)("f"),NEW(FunExpr)("x",NEW(AddExpr)(NEW(VarExpr)("x"),NEW(NumExpr)(1))),NEW(CallExpr)(NEW(VarExpr)("f"),NEW(NumExpr)(10)))));

    // Value tests with a single substitution
    CHECK(parseString("_let f = _fun (x) x+1 _in f(10)")->interp(NEW(EmptyEnv)())->printToString() == "11");
    CHECK(parseString("_let f = _fun (x) x+1 _in f(10)")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(11)));
    CHECK(parseString("_let f = _fun (x) x+1 _in f(10)")->interp(NEW(EmptyEnv)())->to_expr()->equals(NEW(NumExpr)(11)));

    // Value tests with a double substitution
    CHECK(parseString("_let f = _fun (x) x*x _in f(2)")->interp(NEW(EmptyEnv)())->printToString() == "4");
    CHECK(parseString("_let f = _fun (x) x+x _in f(3)")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(6)));

    CHECK(parseString("_let y = 8 _in _let f = _fun (x) x*y _in f(2)")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(16)));
    CHECK(parseString("f(10)(1)")->equals( NEW(CallExpr)( NEW(CallExpr)( NEW(VarExpr)("f"), NEW(NumExpr)(10)), NEW(NumExpr)(1))));

    // Double substitution on different variables => fails for segmentation fault
    CHECK(parseString("_let f = _fun (x) _fun (y) x*x + y*y _in f(2)(3)")->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(13)));
}

TEST_CASE("If optimize"){
    CHECK(parseString("_if x == 0 _then 5 _else 7")->optimize()->equals(NEW(IfExpr)(NEW(EqualsExpr)(NEW(VarExpr)("x"),NEW(NumExpr)(0)),NEW(NumExpr)(5),NEW(NumExpr)(7))));
    CHECK(parseString("_if x == 0 _then 5 _else 7")->substitute("y",NEW(NumVal)(9))->equals(NEW(IfExpr)(NEW(EqualsExpr)(NEW(VarExpr)("x"),NEW(NumExpr)(0)),NEW(NumExpr)(5),NEW(NumExpr)(7))));
}

TEST_CASE("Matthew's Fibbonacci Test"){
    CHECK(parseString(" _let fib = _fun (fib)\n"
                      " _fun (x)\n"
                      " _if x == 0\n"
                      " _then 1\n"
                      " _else _if x == 2 + -1\n"
                      " _then 1\n"
                      " _else fib(fib)(x + -1)\n"
                      " + fib(fib)(x + -2)\n"
                      " _in fib(fib)(10)")
    ->interp(NEW(EmptyEnv)())->equals(NEW(NumVal)(89)));
}

TEST_CASE("Simple Step tests"){

    // Basic addition
    CHECK(Step::interp_by_steps(parseString("2+3"))->equals(NEW(NumVal)(5)));

    // Basic multiplication
    CHECK(Step::interp_by_steps(parseString("2*3"))->equals(NEW(NumVal)(6)));

}

TEST_CASE("Counting tests"){
    // Counting test
    CHECK(Step::interp_by_steps(parseString("_let count = _fun (count)\n"
                      " _fun (n)\n"
                      " _if n == 0\n"
                      " _then 0\n"
                      " _else 1 + count (count) (n + -1)\n"
                      " _in count (count)(100000)")
                      )->equals(NEW(NumVal)(100000)));

    // Countdown test
    CHECK(Step::interp_by_steps(parseString("_let count = _fun (count)\n"
                                            " _fun (n)\n"
                                            " _if n == 0\n"
                                            " _then 0\n"
                                            " _else count (count) (n + -1)\n"
                                            " _in count (count)(100000)")
                                            )->equals(NEW(NumVal)(0)));
    // Fibbonacci test
    CHECK(Step::interp_by_steps(parseString(" _let fib = _fun (fib)\n"
                      " _fun (x)\n"
                      " _if x == 0\n"
                      " _then 1\n"
                      " _else _if x == 2 + -1\n"
                      " _then 1\n"
                      " _else fib(fib)(x + -1)\n"
                      " + fib(fib)(x + -2)\n"
                      " _in fib(fib)(10)")
                  )->equals(NEW(NumVal)(89)));
}

TEST_CASE("Purposeful overflow"){
    CHECK(NEW(NumVal)(INT_MAX)->mult_with(NEW(NumVal)(2))->equals(NEW(NumVal)(2*INT_MAX)));
}

*/

