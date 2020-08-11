# Jooby
## Router
### Route
1. http method
2. path pattern
3. handle function

### Path Pattern
1. static
2. variable --- {}
3. optional --- ？
4. regex --- {id:[0-9]+}? (id:后面不能有空格)
5. catchall --- *

### Handler
1. Route.Decorator  
横切关注点，response modification,verification,security,tracing .etc.
接收一个handler，并且产生一个handler.
```java
interface Decorator { 
    Handler apply(Handler next);
}
```

2. Before  
the before filter runs before a handler.  usually modifying the http response.  
```java
interface Before { 
    void apply(Context ctx);
}
```

3. After  
the after filter runs after a handler.  
* http context
* result of handler or null for side-effects handler
* exception generates from handler
```java
interface After { 
    void apply(Context ctx, Object result, Throwable failure);
}
```

4. Complete
the complete listener run at the completion of a request/response cycle
```
{
    decorator(next -> ctx -> { 
        long start = System.currentTimeInMillis(); 
        ctx.onComplete(context -> { 
            long end = System.currentTimeInMillis(); 
            System.out.println("Took: " + (end - start));
        });
    });
}
```

### Pipeline
Route pipeline(route stack) is a composition of one or more decorator(s) tied to a single handler.  
1. Order(what you see is what you get)  
Order follows the what you see is what you get approach.
每一个路由只能see定义在它之前的所有decorator

2. Scoped Decorator
route(Runnable), decorator  
```
{ 
    // Increment +1 
    decorator(next -> ctx -> {
        Number n = (Number) next.apply(ctx); return 1 + n.intValue();
    });
    
    routes(() -> { // Multiply by 2
        decorator(next -> ctx -> {
            Number n = (Number) next.apply(ctx); return 2 * n.intValue();
        });
         get("/4", ctx -> 4); // => 9
    }); 

    get("/1", ctx -> 1); // =>2
}
```

### Grouping routes
1. Route operator
the route(Runnable) operators a new route scope.
```
{ 
    routes(() -> { 
        get("/", ctx -> "Hello");
    });
}
```

2. Route with path prefix
```
{ 
    path("/api/user", () -> { 
        get("/{id}", ctx -> ...); 
        get("/", ctx -> ...); 
        post("/", ctx -> ...); 
        ...
    });
}
```

### Composing routes
Composition is a technique for building modular applications. You can compose one or more router/application into a new one.
1. Composing
```java
public class Foo extends Jooby { 
    { 
        get("/foo", Context::getRequestPath); 
    } 
}
public class Bar extends Jooby { 
    {
        get("/bar", Context::getRequestPath); 
    } 
}
public class App extends Jooby { 
    {
        use(new Foo()); 
        use(new Bar()); 
        get("/app", Context::getRequestPath);
    }
}
```

2. Composing with path prefix
```java
public class Foo extends Jooby {
    {
        get("/foo", Context::getRequestPath);
    }   
}
public class App extends Jooby {
    {
        use("/prefix", new Foo());
    }   
}
```

3. Dynamic Routing(动态路由)
Dynamic routing is looks similar to composition but enabled/disabled routes at runtime using a predicate.
```java
public class App extends Jooby { 
    {
        use(ctx -> ctx.header("version").value().equals("v1"), new V1()); 
        use(ctx -> ctx.header("version").value().equals("v2"), new V2());
    }
}
```

### Options
1. Hidden Method
2. Trust Proxy
