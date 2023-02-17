(ns bootstrap.inline
  (:refer-clojure :exclude [slurp])
  #?(:cljs (:require-macros [bootstrap.inline])))

#?(:clj
   (defmacro inline [file]
     (clojure.core/slurp file)))
