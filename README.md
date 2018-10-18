# JavaFF2.1
An implementation of the [FF](http://www.loria.fr/~hoffmanj/ff.html) forward search planning system in Java. JavaFF 2.1 is itself a branch of JavaFF planner by Andrew Coles, to allow parsing and solving of PDDL 2.1 problems. Additional improvements include optimisation to enable order-of-magnitude speedups over vanilla JavaFF. This is most likely version 2.1.8, which supports STRIPS and ADL planning, but not metrics and temporal aspects. The code to do this exists within JavaFF but has been disabled (somewhere) as it was unnecessary and introduced additional complexity. 

Older versions of JavaFF are kicking about Github in various places via people who requested the code personally, most likely JavaFF 2.1.5. See http://personal.strath.ac.uk/david.pattison/ (while it is still available) for this and other older versions.

# Usage
The usage of the code is the same as vanilla JavaFF. In other words, run javaff.JavaFF.main() with a domain and problem file.  Be sure to add the contents of the /lib directory to the classpath.

# Citation

For citing JavaFF 2.1 specifically, use 

```
@phdthesis{phdthesis,
  author       = {David Pattison}, 
  title        = {A New Heuristic Based Model of Goal Recognition Without Libraries},
  school       = {University of Strathclyde},
  year         = 2015
}
```

For citing JavaFF generally, use Coles' paper from 2008

```
@INPROCEEDINGS{ac2008001,
	author = "A. I. Coles and M. Fox and D. Long and A. J. Smith",
	title = "Teaching Forward-Chaining Planning with {JavaFF}",
	booktitle = "Colloquium on {AI} Education, Twenty-Third {AAAI} Conference on Artificial Intelligence",
	year = "2008",
	month = "July",
}
```



# Help
As far as I'm concerned, JavaFF can be considered completely obsolete from a planning perspective (15 year old heuristic), but the underlying framework is robust enough to develop from (although I never did get around to decoupling the PlanningGraph from States, which causes me great depression). However, I personally do not maintain this code base, so I release it to the Github ether in the hope others will find it useful. Feel free to send me a message asking questions about the code, but I can't guarantee I will have a useful answer!
