//
// Created by Trent Evans on 1/21/20.
//

#ifndef BETTERMATHPARSER_PARSE_H
#define BETTERMATHPARSER_PARSE_H

#include "pointer.h"

class Expr;

PTR(Expr) parseInStream(std::istream &in);
static PTR(Expr) parseString(std::string s);

#endif //BETTERMATHPARSER_PARSE_H
