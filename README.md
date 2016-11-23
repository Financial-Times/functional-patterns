# Functional Patterns

[![CircleCI](https://circleci.com/gh/Financial-Times/functional-patterns.svg?style=svg&circle-token=f993e93121c3784901f270d2b3cc6e90a2fdbe99)](https://circleci.com/gh/Financial-Times/functional-patterns) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ft/functional-patterns/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ft/functional-patterns)

Some useful functional patterns, and utility functions for use with ```Stream```, ```Optional``` and ```Map```.


## Either
A monad to make exceptional condition handling more functional:

    Either<Exception, Double> result =
            trying(() -> Integer.parseInt(someInputString))
                .map(x -> 2 * x)
                .flatMap(x -> right(x % 10))
                .map(this::toDouble);

    if(result.isRight()) {
        // happy right() Double
    } else {
        // sad left() Exception
    }

## Case
Evaluate a lambda when list of values matches list of patterns:

     // simple calculator
     Integer result = Case.<Integer>match( op, a, b )
             .when( "square", Ref, Null ).apply(() -> a * a)
             .when( "cube",   Ref, Null ).apply( () -> a * a * a )
             .when( "add",    Ref, Ref  ).apply( () -> a + b )
             .when( "sub",    Ref, Ref  ).apply( () -> a - b )
             .when( "div",    Ref, 0    ).thenThrow(() -> new IllegalArgumentException("divide by zero"))
             .when( "div",    Ref, Ref  ).apply( () -> a / b )
             .map(r -> {
                 System.out.println("Result: " + r);
                 return r;
             })
             .orElseThrow( () -> new IllegalArgumentException("no matching operation" ) );

## Memoize

[Memoize](https://en.wikipedia.org/wiki/Memoization) a (possibly expensive) operation, called on demand:

    // a supplier which will call a client at most once:
    Supplier<List<Things>> thingFetcher = Memoize.memorize( () -> client.fetchAll() );
    ...
    thingFetcher.get();

The resulting `Supplier` is thread-safe, and guarantees that the operation will be called 
only once, even if shared between threads.

## Cache
Returns a function which caches results in the given Guava Cache, hiding the use of the cache:

    com.google.common.cache.Cache<Integer, Integer> cache =
        CacheBuilder.newBuilder().maximumSize(200).build();

    Function<Integer,String> f = x -> ... ;
    Function<Integer,String> cachedF = Cache.cached(cache, f);
    ...
    cachedF.apply(10); // f(10) calculated and cached
    cachedF.apply(10); // potentially using cached result of f(10)

## Curry
['Curry'](https://en.wikipedia.org/wiki/Currying) a parameter into a function, returning a function with 
one less parameter:

    BiFunction<Double, Double, Double> pow = (x,y) -> Math.pow(x, y);
    
    Function<Double, Double> powerOfTwo = Curry.curry(pow, 2);
    
    powerOfTwo.apply(4); // 16 
    
Maps `Function` -> `Supplier`; `BiFunction` ->`Function` and `BiConsumer` -> `Consumer`.

## MapFunctions

Re-calculate values of map:

    // keep all keys, multiply each value by 10
    final ImmutableMap<String, Integer> input = ImmutableMap.of("a", 1, "b", 2, "c", 3);
    final Map<String, Integer> output = MapFunctions.mapValues(input, e -> e.getValue() * 10);

Remove all the keys with empty Optional values:

    // only retains keys "a" and "c"
    final ImmutableMap<String, Optional<String>> input = ImmutableMap.of("a", Optional.of("A"), "b", Optional.empty(), "c", Optional.of("C"));
    final Map<String, String> output = MapFunctions.flattenValues(input);

## OptionalFunctions

First non-empty of a list of optionals in order:

    OptionalFunctions.firstPresentOf(Optional.of(1), Optional.of(2)); // Optional.of(1)

First non-empty result of evaluating a list of Suppliers in order:

    OptionalFunctions.firstPresentOf(Optional::empty, () -> Optional.of(2)); // Optional.of(2)

## StreamFunctions

Provide a stream of non-null values:

    StreamFunctions.safeStreamOf(1, null, 23, null).map(x -> x * x); // 1, 529
