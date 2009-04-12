(ns state
  (:import (clojure.lang Agent Ref))
  (:use [clojure.contrib.test-is]
	[clojure.contrib.def]))

(defmacro defstate
  "Defines a state variable."
  ([sym] `(defstate ~sym nil))
  ([sym val]
     `(def ~sym (ref {::state ~val}))))

(defn add-event
  "Adds an event to a state and value.
  Used by various def macros.  Shouldn't 
  be called directly."
  [state value event args]
  (let [old-set (or (state value) {})
	new-set (assoc old-set event args)]
    (dosync
     (alter state assoc value new-set))))

(defn defaction
  "Causes the given function to be called with
  the specifed arguments when the given state is set."
  [state value action & args]
  (add-event state value action args))

(defmacro defref
  "Defines a ref that can be adjutsed by state."
  ([state value ref-name ref-val docstring]
     `(do (when-not ~((ns-map *ns*) ref-name)
	    (defvar ~ref-name (ref ~ref-val) ~docstring))
	  ~(list `add-event state value ref-name ref-val)))
  ([state value ref-name ref-val]
     `(do (when-not ~((ns-map *ns*) ref-name)
	    (def ~ref-name (ref ~ref-val)))
	  ~(list `add-event state value ref-name ref-val)))
  ([state value ref-name] 
     (defref state value ref-name nil)))

(defn- update
  "Updates a single element of a state."
  [[key val]]
  (let [class (class key)]
    (cond 
     (fn? key)     (apply key val),
     (= class Ref) (dosync (ref-set key val)),)))

(defn set-state
  "Sets a state."
  [state val]
  (when-not (= (state ::state) val)
    (when-let [val-map (state val)]
      (doseq [item val-map]
	(update item))
      (dosync
       (alter state assoc ::state val)))))

(when *load-tests*
  
  (defn fixture [f]
    (defstate holiday)
    (defref holiday :thanksgiving food :turkey)
    (defref holiday :easter food :candy)
    (defref holiday :christmas food :ham)
    (defref holiday :birthday food :cake)
    (defaction holiday :halloween #(def msg "Boo!"))
    (defaction holiday :birthday #(def msg (str "Happy " %)) 24)

    (f)

    (ns-unmap 'state 'food))

  (deftest test-states
    (is (= @food :turkey) 
	"Ref doesn't initialize to first provided value.")
    (set-state holiday :easter)
    (is (= @food :candy)
	"Setting state doesn't work.")
    (set-state holiday :halloween)
    (is (= @food :candy)
	"State changes when not specified for new position.")
    (is (= msg "Boo!")
	"Action is not correctly performed.")
    (set-state holiday :birthday)
    (is (= msg "Happy 24")
	"Action does not take argument correctly.")
    (is (= @food :cake)
	"Setting state of ref doesn't work.")
    (def msg "Gone!")
    (is (= msg "Gone!")
	"Def isn't working!?!?")
    (set-state holiday :birthday)
    (is (= msg "Gone!")
	"Performed actions when state was not changed."))

  (use-fixtures :each fixture))


