# HollingBerries Solution in Clojure

A couple of comments:
*   I am very new to clojure so any comments on doing things better would be welcomed.
*   I re-used(read copied...) quite a few things from [mschristiansen](https://github.com/mschristiansen)'s implementation. 
*   The approach I used here is similar to the my example in ABAP where I placed the Supplier and Product rules in records. I believe this will give a bit more flexibility should the business rules(e.g. vary markups, expiry dates, etc) vary by either product or supplier).

## Update

I did a second version which does a lazy read of the input file. This leads to huge performance gains when processing a very large file. 

## Usage

You'll need [Leiningen](https://github.com/technomancy/leiningen), a
build tool for Clojure.

Leiningen will fetch project dependencies described in the project.clj file.
> lein deps

To run 1st Example holling.core which generates pricefile1.txt.
> lein run -m holling.core

To run the 2nd Example(holling.second) which generates pricefile2.txt.
> lein run -m holling.second

Or start a repl (is it called a prompt in Python?)
> lein repl

To run the tests:
> lein test

## License

Distributed under the Eclipse Public License, the same as Clojure.