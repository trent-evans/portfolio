//
// Created by Trent Evans on 3/2/20.
//

#ifndef ARITHMETICINTERPRETER_POINTER_H
#define ARITHMETICINTERPRETER_POINTER_H

#if 0

#define NEW(T) new T
#define PTR(T) T*
#define CAST(T) dynamic_cast<T>
#define THIS this /* Except in the constructor */
#define ENABLE_THIS(T) /* nada */

#else
#include <memory>
#define NEW(T) std::make_shared<T>
#define PTR(T) std::shared_ptr<T>
#define CAST(T) std::dynamic_pointer_cast<T>
#define THIS shared_from_this()
#define ENABLE_THIS(T) : public std::enable_shared_from_this<T>

#endif


#endif //ARITHMETICINTERPRETER_POINTER_H
