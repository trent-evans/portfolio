//
// Created by Trent Evans on 1/21/20.
//

#include "expr.h"
#include "value.h"
#include "cont.h"
#include "step.h"

/**
  _   _                 ______
 | \ | |               |  ____|
 |  \| |_   _ _ __ ___ | |__  __  ___ __  _ __
 | . ` | | | | '_ ` _ \|  __| \ \/ / '_ \| '__|
 | |\  | |_| | | | | | | |____ >  <| |_) | |
 |_| \_|\__,_|_| |_| |_|______/_/\_\ .__/|_|
                                   | |
                                   |_|
**/

NumExpr::NumExpr(int val) {
    this->val = val;
}

bool NumExpr::equals(PTR(Expr) e) {
    PTR(NumExpr) n = CAST(NumExpr)(e);
    if (n == NULL)
        return false;
    else
        return val == n->val;
}

PTR(Val) NumExpr::interp(PTR(Env) env){
    return NEW(NumVal)(val);
}

PTR(Expr) NumExpr::substitute(std::string varName, PTR(Val) subsVal){
    return NEW(NumExpr)(val);
}

bool NumExpr::containsVariable() {
    return false;
}

PTR(Expr) NumExpr::optimize(){
    return NEW(NumExpr)(val);
}

std::string NumExpr::printToString() {
    return std::to_string(val);
}

void NumExpr::step_interp(){
    Step::mode = Step::continue_mode;
    Step::val = NEW(NumVal)(val);
    Step::cont = Step::cont;
}

/**
              _     _ ______
     /\      | |   | |  ____|
    /  \   __| | __| | |__  __  ___ __  _ __
   / /\ \ / _` |/ _` |  __| \ \/ / '_ \| '__|
  / ____ \ (_| | (_| | |____ >  <| |_) | |
 /_/    \_\__,_|\__,_|______/_/\_\ .__/|_|
                                 | |
                                 |_|
 */

AddExpr::AddExpr(PTR(Expr) lhs, PTR(Expr) rhs) {
    this->lhs = lhs;
    this->rhs = rhs;
}

bool AddExpr::equals(PTR(Expr) e) {
    PTR(AddExpr) a = CAST(AddExpr)(e);
    if (a == NULL)
        return false;
    else
        return (lhs->equals(a->lhs)
                && rhs->equals(a->rhs));
}

PTR(Val) AddExpr::interp(PTR(Env) env){
    return lhs->interp(env)->add_to(rhs->interp(env));
}

PTR(Expr) AddExpr::substitute(std::string varName, PTR(Val) subsVal) {
    return NEW(AddExpr)(lhs->substitute(varName,subsVal), rhs->substitute(varName,subsVal));
}

bool AddExpr::containsVariable() {
    return (lhs->containsVariable() || rhs->containsVariable());
}

PTR(Expr) AddExpr::optimize() {
    PTR(Expr) lhsSimplify = lhs->optimize();
    PTR(Expr) rhsSimplify = rhs->optimize();
    if(!lhsSimplify->containsVariable() && !rhsSimplify->containsVariable()){
        return lhsSimplify->interp(NEW(EmptyEnv)())->add_to(rhsSimplify->interp(NEW(EmptyEnv)()))->to_expr();
    }
    return NEW(AddExpr)(lhsSimplify,rhsSimplify);
}

std::string AddExpr::printToString() {
    return lhs->printToString() + " + " + rhs->printToString();
}

void AddExpr::step_interp(){
    Step::mode = Step::interp_mode;
    Step::expr = lhs;
    Step::env = Step::env;
    Step::cont = NEW(RightThenAddCont)(rhs,Step::env,Step::cont);
}

/**
  __  __       _ _   ______
 |  \/  |     | | | |  ____|
 | \  / |_   _| | |_| |__  __  ___ __  _ __
 | |\/| | | | | | __|  __| \ \/ / '_ \| '__|
 | |  | | |_| | | |_| |____ >  <| |_) | |
 |_|  |_|\__,_|_|\__|______/_/\_\ .__/|_|
                                | |
                                |_|
 */

MultExpr::MultExpr(PTR(Expr) lhs, PTR(Expr) rhs) {
    this->lhs = lhs;
    this->rhs = rhs;
}

bool MultExpr::equals(PTR(Expr) e) {
    PTR(MultExpr) m = CAST(MultExpr)(e);
    if (m == NULL)
        return false;
    else
        return (lhs->equals(m->lhs)
                && rhs->equals(m->rhs));
}

PTR(Val) MultExpr::interp(PTR(Env) env){
    return lhs->interp(env)->mult_with(rhs->interp(env));
}

PTR(Expr) MultExpr::substitute(std::string varName, PTR(Val) subsVal) {
    return NEW(MultExpr)(lhs->substitute(varName,subsVal), rhs->substitute(varName,subsVal));
}

bool MultExpr::containsVariable() {
    return (lhs->containsVariable() || rhs->containsVariable());
}

PTR(Expr) MultExpr::optimize() {
    PTR(Expr) lhsSimplify = lhs->optimize();
    PTR(Expr) rhsSimplify = rhs->optimize();
    if(!lhsSimplify->containsVariable() && !rhsSimplify->containsVariable()){
        return lhsSimplify->interp(NEW(EmptyEnv)())->mult_with(rhsSimplify->interp(NEW(EmptyEnv)()))->to_expr();
    }
    return NEW(MultExpr)(lhsSimplify,rhsSimplify);
}

std::string MultExpr::printToString() {
    return lhs->printToString() + " * " + rhs->printToString();
}

void MultExpr::step_interp(){
    Step::mode = Step::interp_mode;
    Step::expr = lhs;
    Step::env = Step::env;
    Step::cont = NEW(RightThenMultCont)(rhs,Step::env,Step::cont);
}

/**
 __      __        ______
 \ \    / /       |  ____|
  \ \  / /_ _ _ __| |__  __  ___ __  _ __
   \ \/ / _` | '__|  __| \ \/ / '_ \| '__|
    \  / (_| | |  | |____ >  <| |_) | |
     \/ \__,_|_|  |______/_/\_\ .__/|_|
                              | |
                              |_|
 */


VarExpr::VarExpr(std::string var){
    this->var = var;
}

bool VarExpr::equals(PTR(Expr) e) {
    PTR(VarExpr) n = CAST(VarExpr)(e);
    if (n == NULL)
        return false;
    else
        return var == n->var;
}

PTR(Val) VarExpr::interp(PTR(Env) env){
    return env->lookup(var);
}

PTR(Expr) VarExpr::substitute(std::string varName, PTR(Val) subsVal) {
    if(varName == var){
        return subsVal->to_expr();
    }
    return NEW(VarExpr)(var);
}

bool VarExpr::containsVariable() {
    return true;
}

PTR(Expr) VarExpr::optimize() {
    return NEW(VarExpr)(var);
}

std::string VarExpr::printToString() {
    return var;
}

void VarExpr::step_interp(){
    Step::mode = Step::continue_mode;
    Step::val = Step::env->lookup(var);
    Step::cont = Step::cont;
}

/**
  _          _   ______
 | |        | | |  ____|
 | |     ___| |_| |__  __  ___ __  _ __
 | |    / _ \ __|  __| \ \/ / '_ \| '__|
 | |___|  __/ |_| |____ >  <| |_) | |
 |______\___|\__|______/_/\_\ .__/|_|
                            | |
                            |_|
 */

LetExpr::LetExpr(PTR(VarExpr) subsVariable, PTR(Expr) subsExpression, PTR(Expr) baseExpression){
    this->name        = subsVariable;
    this->rhs = subsExpression;
    this->body = baseExpression;
}

bool LetExpr::equals(PTR(Expr) e){
    PTR(LetExpr) l = CAST(LetExpr)(e);
    if(l == NULL){
        return false;
    }
    return (name->equals(l->name) &&
            rhs->equals(l->rhs) &&
            body->equals(l->body));
}

PTR(Val) LetExpr::interp(PTR(Env) env){
    PTR(Val) rhs_val = rhs->interp(env);
    PTR(Env) new_env = NEW(ExtendedEnv)(name->var,rhs_val,env);
    return body->interp(new_env);
}

PTR(Expr) LetExpr::substitute(std::string varName, PTR(Val) subsVal){
    // Consider lexical scope => if the variable to substitute is the same as name, don't substitute into the body
    if(varName == name->var){
        return NEW(LetExpr)(name,
                            rhs->substitute(varName, subsVal),
                            body);
    } // Otherwise, do substitute
    return NEW(LetExpr)(name,
                        rhs->substitute(varName, subsVal),
                        body->substitute(varName, subsVal));
}

bool LetExpr::containsVariable(){
    return true;
}

PTR(Expr) LetExpr::optimize() {
    PTR(Expr) newBase = body->optimize();
    PTR(Expr) newSubsExpr = rhs->optimize();
    if(!newBase->containsVariable() | newSubsExpr->containsVariable()){  // If there's no way to substitute, return a let
        return NEW(LetExpr)(name, newSubsExpr, newBase);
    }
    return newBase->substitute(name->var, newSubsExpr->interp(NEW(EmptyEnv)()))->optimize();
}

std::string LetExpr::printToString() {
    return ("_let " + name->printToString() + " = " +
            rhs->printToString() + " _in " +
            body->printToString());
}

void LetExpr::step_interp(){
    Step::mode = Step::interp_mode;
    Step::expr = rhs;
    Step::env = Step::env;
    Step::cont = NEW(LetBodyCont)(name->var,body,Step::env,Step::cont);
}


/**
  ____              _ ______
 |  _ \            | |  ____|
 | |_) | ___   ___ | | |__  __  ___ __  _ __
 |  _ < / _ \ / _ \| |  __| \ \/ / '_ \| '__|
 | |_) | (_) | (_) | | |____ >  <| |_) | |
 |____/ \___/ \___/|_|______/_/\_\ .__/|_|
                                 | |
                                 |_|
 */
BoolExpr::BoolExpr(bool rep) {
    this->rep = rep;
}

bool BoolExpr::equals(PTR(Expr) e) {
    PTR(BoolExpr) b = CAST(BoolExpr)(e);
    if (b == NULL)
        return false;
    else
        return rep == b->rep;
}

PTR(Val) BoolExpr::interp(PTR(Env) env) {
    return NEW(BoolVal)(rep);
}

PTR(Expr) BoolExpr::substitute(std::string varName, PTR(Val) subsVal) {
    return NEW(BoolExpr)(rep);
}

bool BoolExpr::containsVariable(){
    return false;
}

PTR(Expr) BoolExpr::optimize(){
    return NEW(BoolExpr)(rep);
}

std::string BoolExpr::printToString(){
    if(rep){
        return "true";
    }
    return "false";
}

void BoolExpr::step_interp(){
    Step::mode = Step::continue_mode;
    Step::val = NEW(BoolVal)(rep);
    Step::cont = Step::cont;
}

/**
  _____  __ ______
 |_   _|/ _|  ____|
   | | | |_| |__  __  ___ __  _ __
   | | |  _|  __| \ \/ / '_ \| '__|
  _| |_| | | |____ >  <| |_) | |
 |_____|_| |______/_/\_\ .__/|_|
                       | |
                       |_|
 */
IfExpr::IfExpr(PTR(Expr) testExpr, PTR(Expr) thenExpr, PTR(Expr) elseExpr){
    this->testExpr = testExpr;
    this->thenExpr = thenExpr;
    this->elseExpr = elseExpr;
}

bool IfExpr::equals(PTR(Expr) e){
    PTR(IfExpr) n = CAST(IfExpr)(e);
        if(n == nullptr){
            return false;
        }
        return(testExpr->equals(n->testExpr) &&
                thenExpr->equals(n->thenExpr) &&
                elseExpr->equals(n->elseExpr));

}
PTR(Val) IfExpr::interp(PTR(Env) env){
    if(testExpr->interp(env)->is_true()){
        return thenExpr->interp(env);
    }
    return elseExpr->interp(env);
}

PTR(Expr) IfExpr::substitute(std::string varName, PTR(Val) subsVal){
    return NEW(IfExpr)(testExpr->substitute(varName, subsVal),
                      thenExpr->substitute(varName, subsVal),
                      elseExpr->substitute(varName, subsVal));
}

bool IfExpr::containsVariable(){
    return (testExpr->containsVariable() || thenExpr->containsVariable() || elseExpr->containsVariable());
}

PTR(Expr) IfExpr::optimize(){
    PTR(Expr) testOpt = testExpr->optimize();
    if(testOpt->containsVariable()){
        return NEW(IfExpr)(testOpt,thenExpr->optimize(),elseExpr->optimize());
    }
    else if(testExpr->interp(NEW(EmptyEnv)())->is_true()){
        return thenExpr->optimize();
    }
    return elseExpr->optimize();
}

std::string IfExpr::printToString(){
    return "_if " + testExpr->printToString() + " _then " + thenExpr->printToString() + " _else " + elseExpr->printToString();
}

void IfExpr::step_interp(){
    Step::mode = Step::interp_mode;
    Step::expr = testExpr;
    Step::env = Step::env;
    Step::cont = NEW(IfBranchCont)(thenExpr,elseExpr,Step::env,Step::cont);
}

/**
  ______                  _     ______
 |  ____|                | |   |  ____|
 | |__   __ _ _   _  __ _| |___| |__  __  ___ __  _ __
 |  __| / _` | | | |/ _` | / __|  __| \ \/ / '_ \| '__|
 | |___| (_| | |_| | (_| | \__ \ |____ >  <| |_) | |
 |______\__, |\__,_|\__,_|_|___/______/_/\_\ .__/|_|
           | |                             | |
           |_|                             |_|
 */

EqualsExpr::EqualsExpr(PTR(Expr) lhs, PTR(Expr) rhs){
    this->lhs = lhs;
    this->rhs = rhs;
}

bool EqualsExpr::equals(PTR(Expr) e){
    PTR(EqualsExpr) n = CAST(EqualsExpr)(e);
        if(n == nullptr){
            return false;
        }
        return(lhs->equals(n->lhs) && rhs->equals(n->rhs));
}

PTR(Val) EqualsExpr::interp(PTR(Env) env){
    return NEW(BoolVal)(lhs->interp(env)->equals(rhs->interp(env)));
}

PTR(Expr) EqualsExpr::substitute(std::string varName, PTR(Val) subsVal) {
    return NEW(EqualsExpr)(lhs->substitute(varName,subsVal),
                          rhs->substitute(varName,subsVal));
}

bool EqualsExpr::containsVariable(){
    return(lhs->containsVariable() || rhs->containsVariable());
}

PTR(Expr) EqualsExpr::optimize(){
    if(lhs->containsVariable() || rhs->containsVariable()){
        return NEW(EqualsExpr)(lhs,rhs);
    }else {
        return NEW(BoolExpr)(lhs->optimize()->equals(rhs->optimize()));
    }
}

std::string EqualsExpr::printToString(){
    return (lhs->printToString() + " == " + rhs->printToString());
}

void EqualsExpr::step_interp(){
    Step::mode = Step::interp_mode;
    Step::expr = lhs;
    Step::env = Step::env;
    Step::cont = NEW(RightThenCompCont)(rhs,Step::env,Step::cont);
}

/**
  ______           ______
 |  ____|         |  ____|
 | |__ _   _ _ __ | |__  __  ___ __  _ __
 |  __| | | | '_ \|  __| \ \/ / '_ \| '__|
 | |  | |_| | | | | |____ >  <| |_) | |
 |_|   \__,_|_| |_|______/_/\_\ .__/|_|
                              | |
                              |_|
 */

FunExpr::FunExpr(std::string formal_arg, PTR(Expr) body) {
    this->formal_arg = formal_arg;
    this->body = body;
}

bool FunExpr::equals(PTR(Expr) e){
    PTR(FunExpr) n = CAST(FunExpr)(e);
    if(n == nullptr){
        return false;
    }
    return(formal_arg == n->formal_arg && body->equals(n->body));
}

PTR(Val) FunExpr::interp(PTR(Env) env){
    return NEW(FunVal)(formal_arg, body, env);
}

PTR(Expr) FunExpr::substitute(std::string varName, PTR(Val) subsVal){
    if(formal_arg == varName){
        return NEW(FunExpr)(formal_arg,body);
    }
    return NEW(FunExpr)(formal_arg,body->substitute(varName,subsVal));
}


bool FunExpr::containsVariable(){
    return true;
}

PTR(Expr) FunExpr::optimize(){
    return NEW( FunExpr)(formal_arg, body->optimize());
}

std::string FunExpr::printToString(){
    return "_fun f(" + formal_arg + ") " + body->printToString();
}

void FunExpr::step_interp(){
    Step::mode = Step::continue_mode;
    Step::val = NEW(FunVal)(formal_arg,body,Step::env);
    Step::cont = Step::cont;
}

/**
   _____      _ _ ______
  / ____|    | | |  ____|
 | |     __ _| | | |__  __  ___ __  _ __
 | |    / _` | | |  __| \ \/ / '_ \| '__|
 | |___| (_| | | | |____ >  <| |_) | |
  \_____\__,_|_|_|______/_/\_\ .__/|_|
                             | |
                             |_|
 */

CallExpr::CallExpr(PTR(Expr) to_be_called, PTR(Expr) actual_arg){
    this->to_be_called = to_be_called;
    this->actual_arg = actual_arg;
}

bool CallExpr::equals(PTR(Expr) e){
    PTR(CallExpr) n = CAST(CallExpr)(e);
    if(n == nullptr){
        return false;
    }
    return(this->to_be_called->equals(n->to_be_called) && actual_arg->equals(n->actual_arg));
}

PTR(Val) CallExpr::interp(PTR(Env) env){
    return to_be_called->interp(env)->call(actual_arg->interp(env));
}

PTR(Expr) CallExpr::substitute(std::string varName, PTR(Val) subsVal){
    return NEW(CallExpr)(to_be_called->substitute(varName,subsVal), actual_arg->substitute(varName, subsVal));
}

bool CallExpr::containsVariable(){
    return actual_arg->containsVariable();
}

PTR(Expr) CallExpr::optimize(){
    return NEW(CallExpr)(to_be_called->optimize(), actual_arg->optimize());
}

std::string CallExpr::printToString(){
    return " " + to_be_called->printToString() + "(" + actual_arg->printToString() + ") ";
}

void CallExpr::step_interp(){
    Step::mode = Step::interp_mode;
    Step::expr = to_be_called;
    Step::cont = NEW(ArgThenCallCont)(actual_arg,Step::env,Step::cont);
}
