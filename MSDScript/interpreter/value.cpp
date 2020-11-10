//
// Created by Trent Evans on 2/4/20.
//

#include "value.h"
#include "expr.h"
#include "env.h"
#include <stdexcept>

/**
  _   _             __      __   _
 | \ | |            \ \    / /  | |
 |  \| |_   _ _ __ __\ \  / /_ _| |
 | . ` | | | | '_ ` _ \ \/ / _` | |
 | |\  | |_| | | | | | \  / (_| | |
 |_| \_|\__,_|_| |_| |_|\/ \__,_|_|
 */

NumVal::NumVal(int rep) {
    this->rep = rep;
}

bool NumVal::equals(PTR(Val) val) {
    PTR(NumVal) other_num_val = CAST(NumVal)(val);
    if (other_num_val == nullptr)
        return false;
    else
        return rep == other_num_val->rep;
}

PTR(Val) NumVal::add_to(PTR(Val) other_val) {
    PTR(NumVal) other_num_val = CAST(NumVal)(other_val);
    if (other_num_val == nullptr)
        throw std::runtime_error("not a number\n");
    else
        return NEW(NumVal)((unsigned)rep + (unsigned)other_num_val->rep);
}

PTR(Val) NumVal::mult_with(PTR(Val) other_val) {
    PTR(NumVal) other_num_val = CAST(NumVal)(other_val);
    if (other_num_val == nullptr)
        throw std::runtime_error("not a number\n");
    else
        return NEW(NumVal)((unsigned)rep * (unsigned)other_num_val->rep);
}

PTR(Expr) NumVal::to_expr() {
    return NEW(NumExpr)(rep);
}

std::string NumVal::printToString() {
    return std::to_string(rep);
}

bool NumVal::is_true() {
    throw std::runtime_error("NumVals cannot be true or false\n");
}

PTR(Val) NumVal::call(PTR(Val) actual_arg){
    throw std::runtime_error("Can't call a NumVal\n");
}

void NumVal::call_step(PTR(Val) actual_arg_val, PTR(Cont) rest){
    throw std::runtime_error("Can't call_step a NumVal\n");
}

/**
  ____              ___      __   _
 |  _ \            | \ \    / /  | |
 | |_) | ___   ___ | |\ \  / /_ _| |
 |  _ < / _ \ / _ \| | \ \/ / _` | |
 | |_) | (_) | (_) | |  \  / (_| | |
 |____/ \___/ \___/|_|   \/ \__,_|_|
 */

BoolVal::BoolVal(bool rep) {
    this->rep = rep;
}

bool BoolVal::equals(PTR(Val) val) {
    PTR(BoolVal) other_bool_val = CAST(BoolVal)(val);
    if (other_bool_val == nullptr)
        return false;
    else
        return rep == other_bool_val->rep;
}

PTR(Val) BoolVal::add_to(PTR(Val) other_val) {
    throw std::runtime_error("no adding booleans\n");
}

PTR(Val) BoolVal::mult_with(PTR(Val) other_val) {
    throw std::runtime_error("no multiplying booleans\n");
}

PTR(Expr) BoolVal::to_expr() {
    return NEW(BoolExpr)(rep);
}

std::string BoolVal::printToString() {
    if (rep)
        return "true";
    else
        return "false";
}

bool BoolVal::is_true() {
    return rep;
}

PTR(Val) BoolVal::call(PTR(Val) actual_arg){
    throw std::runtime_error("Can't call on a BoolVal\n");
}

void BoolVal::call_step(PTR(Val) actual_arg_val, PTR(Cont) rest){
    throw std::runtime_error("Can't call_step a BoolVal\n");
}

/**
  ______       __      __   _
 |  ____|      \ \    / /  | |
 | |__ _   _ _ _\ \  / /_ _| |
 |  __| | | | '_ \ \/ / _` | |
 | |  | |_| | | | \  / (_| | |
 |_|   \__,_|_| |_|\/ \__,_|_|
 */

FunVal::FunVal(std::string formal_arg, PTR(Expr) body, PTR(Env) env){
    this->formal_arg = formal_arg;
    this->body = body;
    this->env = env;
}

bool FunVal::equals(PTR(Val) val){
    PTR(FunVal) secondVal = CAST(FunVal)(val);
    if(secondVal == nullptr){
        return false;
    } else{
        return (formal_arg == secondVal->formal_arg) && (body == secondVal->body);
    }
}

PTR(Val) FunVal::add_to(PTR(Val) other_val) {
    throw std::runtime_error("no adding functions\n");
}

PTR(Val) FunVal::mult_with(PTR(Val) other_val) {
    throw std::runtime_error("no multiplying functions\n");
}

PTR(Expr) FunVal::to_expr(){
    return NEW(FunExpr)(formal_arg, body);
}

std::string FunVal::printToString(){
    return "_fun (" + formal_arg + ") " + body->printToString();
}

bool FunVal::is_true(){
    throw std::runtime_error("No truth or lies in functions\n");
}

PTR(Val) FunVal::call(PTR(Val) actual_arg){
    return body->interp(NEW(ExtendedEnv)(formal_arg,actual_arg,env));
}

void FunVal::call_step(PTR(Val) actual_arg_val, PTR(Cont) rest){
    Step::mode = Step::interp_mode;
    Step::expr = body;
    Step::env = NEW(ExtendedEnv)(formal_arg,actual_arg_val,env); /** formal_arg_val ?? */
    Step::cont = rest;
}
