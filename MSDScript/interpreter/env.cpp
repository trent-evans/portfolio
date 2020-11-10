//
// Created by Trent Evans on 3/4/20.
//

#include "env.h"

/**
  ______                 _         ______
 |  ____|               | |       |  ____|
 | |__   _ __ ___  _ __ | |_ _   _| |__   _ ____   __
 |  __| | '_ ` _ \| '_ \| __| | | |  __| | '_ \ \ / /
 | |____| | | | | | |_) | |_| |_| | |____| | | \ V /
 |______|_| |_| |_| .__/ \__|\__, |______|_| |_|\_/
                  | |         __/ |
                  |_|        |___/
 */

EmptyEnv::EmptyEnv(){
}

PTR(Val) EmptyEnv::lookup(std::string find_name){
    throw std::runtime_error("Free variable: " + find_name + "\n");
}



/**
  ______      _                 _          _ ______
 |  ____|    | |               | |        | |  ____|
 | |__  __  _| |_ ___ _ __   __| | ___  __| | |__   _ ____   __
 |  __| \ \/ / __/ _ \ '_ \ / _` |/ _ \/ _` |  __| | '_ \ \ / /
 | |____ >  <| ||  __/ | | | (_| |  __/ (_| | |____| | | \ V /
 |______/_/\_\\__\___|_| |_|\__,_|\___|\__,_|______|_| |_|\_/
 */

ExtendedEnv::ExtendedEnv(std::string name, PTR(Val) val, PTR(Env) rest){
    this->name = name;
    this->val = val;
    this->rest = rest;
}

PTR(Val) ExtendedEnv::lookup(std::string find_name){
    if(find_name == name){
        return val;
    }
    return rest->lookup(find_name);
}


