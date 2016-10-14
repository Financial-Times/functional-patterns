# Functional Patterns

Some useful functional patterns, and utility functions for use with ```Stream```, ```Optional``` and ```Map```.


## Memorize

Memorize a (possibly expensive) operation, called on demand:

    // call a client at most once:
    Supplier<List<Things>> thingFetcher = Memorize.memorize( () -> client.fetchAll() );

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

## Cache
Returns a function which caches results in the given Guava Cache, hiding the use of the cache:

    com.google.common.cache.Cache<Integer, Integer> cache =
        CacheBuilder.newBuilder().maximumSize(200).build();

    Function<Integer,String> f = x -> ... ;
    Function<Integer,String> cachedF = Cache.cached(cache, f);

    cachedF.apply(10); // f(10) calculated and cached
    cachedF.apply(10); // potentially used cached result of f(10)

## MapFunctions

Re-calculate values of map:--

    // keep all keys, multiply each value by 10
    final ImmutableMap<String, Integer> input = ImmutableMap.of("a", 1, "b", 2, "c", 3);
    final Map<String, Integer> output = MapFunctions.mapValues(input, e -> e.getValue() * 10);

Remove all the keys with empty Optional values:

    // only retains keys "a" and "c"
    final ImmutableMap<String, Optional<String>> input = ImmutableMap.of("a", Optional.of("A"), "b", Optional.empty(), "c", Optional.of("C"));
    final Map<String, String> output = MapFunctions.flattenValues(input);

## OptionalFunctions

First non-empty of a list of optionals in order:

    OptionalFunctions.firstPresentOf(Optional.of(1), Optional.of(2));

First non-empty result of evaluating a list of Suppliers in order:

    OptionalFunctions.firstPresentOf(() -> 1, () -> 2);

## StreamFunctions

Provide a stream of non-null values:

    StreamFunctions.safeStreamOf(1, null, 23, null).map(x -> x * x);
