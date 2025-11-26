os，sys, os.path for cross platform , 
datetime module(datetime, date, time, timedelta, timezone), 
math, 
random

power-user modules or class:
	contextlib(for with statements): @contextmanager, suppress: Ignore specific exceptions (cleaner than try/except pass)
    operator(lambda killer): itemgetter, attrgetter, methodcaller
	collections: deque, Counter, OrderedDict, defaultdict,namedtuple 
	functools module: 
	more-itertools: is the community-standard extension maintained by the Python authorities
	itertools module:  
		- next funciton
		- infinite iterators(count, cycle, repeat ) 
		- terminating iterators: chain, zip_longest, product 
		- combinatoric iterators: permutations, combinations,combinations_with_replacement 

    heapq (Priority Queue): heapq is much faster than sorting the list every time. It implements a Min-Heap
		- heappush: Add item while keeping order.
		- heappop: Pop the smallest item (O(log n)).
		- nlargest / nsmallest: Get top N items efficiently
		
	bisect (Binary Search & Sorted Inserts):
		- insort: Insert item into correct position (O(n)).
		- bisect: Find the index where an item should go (Binary Search, O(log n)).
		
	dataclasses (The Boilerplate Killer): 
	  - Introduced in Python 3.7, this is the modern way to create classes that just hold data. It automatically writes __init__, __repr__, and __eq__ for you
	 
	pathlib (Object-Oriented Paths): Stop using os.path.join and string manipulation for file paths. pathlib treats paths as objects
	
	concurrent.futures (Easy Parallelism): 
		- If you need to run tasks in parallel (threading or multiprocessing), 
		- don't use the low-level threading or multiprocessing modules directly. Use this high-level abstraction.
		
	secrets (Secure Randomness): 
		- Never use the standard random module for passwords, tokens, or auth keys. It is not cryptographically secure. Use secrets instead
		```
			# Generate a secure token for a password reset link
			token = secrets.token_urlsafe(16) 
			print(token) # e.g. 'D8rD8_7i9s-9s8d7'

			# Securely pick a winner
			winner = secrets.choice(['Alice', 'Bob', 'Charlie'])
		```
	enum (Better Constants):
		- Stop using "magic strings" or integers to represent states. Enums make your code safer and self-documenting
		- Enum, auto

  Idioms: 
  
	- 1. Extended Iterable Unpacking (*): data = [1, 2, 3, 4, 5]   first, *middle, last = data
	- 2. Transposing a Matrix with zip(*): A classic one-liner to swap rows and columns: matrix = [[1, 2, 3],[4, 5, 6]]  transposed = list(zip(*matrix)) get [(1, 4), (2, 5), (3, 6)]
	- 3. __slots__ (Memory Optimization): standard Python objects use a lot of memory because they store attributes in a dictionary (__dict__).
	- 4. The else block in Loops
	- 5. @functools.lru_cache (Instant Memoization): 
		- automatically caches the results of a function based on its arguments. It’s a "magic wand" for optimizing recursive algorithms or expensive I/O operations that repeat inputs.
		- case: 计算斐波那契
	- 6. yield from (Generator Delegation)：
		- If you are writing a generator that calls another generator, don't loop over it manually. 
		- Use yield from to delegate directly. It also handles sending values back into the generator (bi-directional).
	- 7. F-String Debugging (=)： old flavor：print(f"x={x}, y={y}, sum={x+y}") , new flavor: print(f"{x=}, {y=}, {x+y=}")
	- 8. collections.ChainMap (Logical Merging):
		- If you have multiple dictionaries (e.g., user config, default config, env vars) and want to search them in priority order, 
		- don't merge them into a new dictionary (which copies data). Use ChainMap to create a logical view over them.
	- 9. functools.partial (Pre-filling Arguments)
		 - This lets you "freeze" some arguments of a function, creating a new function with fewer arguments. Great for callbacks or simplifying APIs.
	- 10. The | Operator for Dicts (Python 3.9+): The cleanest way to merge dictionaries. dic1 = 
	- 11. typing.Protocol (Structural Typing / Duck Typing)
			- In static languages like Java, you implement interfaces explicitly. 
			- In Python, we often just expect an object to "have a method." Protocol lets you formalize this for type checkers without inheritance
	- 12. functools.singledispatch (Function Overloading)
		- Python doesn't support traditional function overloading (defining the same function twice with different arguments). 
		- Instead, use singledispatch to create a function that behaves differently based on the type of the first argument
		- 
	- 13. difflib.get_close_matches (Fuzzy Matching): If you are building a CLI or search feature and want to offer "Did you mean...?" suggestions, this is built-in.
	- 14. timeit (Professional Benchmarking)
		- Stop using time.time() to measure code performance. It is imprecise for small snippets. 
		- timeit runs your code thousands of times to get an accurate average and avoids common pitfalls (like garbage collection interference).
	- 15. types.SimpleNamespace (Quick Dot-Access Objects): 
		- If you want an object where you can access attributes with . (dot) notation, but don't want to define a full class or use a dictionary (which requires ['key'])
		- create object on fly: config = SimpleNamespace(host='localhost', port=8080, active=True)   print(config.host)
	- 16. slice Objects (Reusable Slicing)
	- 17. __call__ (Stateful Functions):
		- If you want a function that "remembers" state but you don't want to use global variables or closures, 
		- you can make a class callable. It looks like a function but acts like an object.
	- 18. pprint (Pretty Print)
	- 19. The Walrus Operator := (Assignment Expressions): 
		- allows you to assign a value to a variable inside an expression. It is incredibly useful for shrinking code where you need to calculate a value, check it, and then use it.
		- while (data := input("Enter value: ")) != "quit":
	- 20. weakref (Memory-Safe Caching):
		- If you are building a cache or referencing heavy objects (like open images or DB connections), 
		- standard references prevent Python's garbage collector from deleting them. 
		- weakref allows you to hold a reference that doesn't stop the object from being deleted if nothing else is using it.
	- 21. queue.Queue (Thread-Safe Communication):
		- If you are using threads, never use a standard list to pass data between them. 
		- It is not thread-safe. queue.Queue handles all the locking for you automatically
	- 22. __missing__ (Custom Dictionary Defaults):
		- You know defaultdict, but sometimes you need more control. 
		- By implementing __missing__ in a dict subclass, you can define exactly what happens when a key isn't found (e.g., fetch it from a database, log a warning, or calculate it)
	- 23. tempfile (Secure Temporary Files): Creating temporary files manually is risky (naming collisions, security holes, forgetting to delete them). tempfile handles this securely and ensures cleanup.
	- 24. uuid (Universally Unique IDs)
	- 25. atexit (Cleanup Scripts): If you need to run code when your program ends (normally or via crash), atexit is the standard hook. Great for closing database connections or saving state.
	- 26. decimal (Financial Math): Never use standard floats for money. Floats have precision errors (e.g., 0.1 + 0.2 != 0.3). The decimal module provides exact arithmetic.
	- 27. abc (Abstract Base Classes): ensure a subclass must implement certain methods (like an interface in Java), use abc. It prevents the class from being instantiated if methods are missing
	- 28. __getattr__ (Dynamic Attributes): This "magic method" is called only when an attribute is not found. It allows you to create "proxy" objects or handle dynamic API calls gracefully.
	- 29. signal (Handling Ctrl+C): If you have a long-running script and want to clean up gracefully when the user hits Ctrl+C, use signal.
	- 30. glob (File Pattern Matching): If you just want to find files matching *.jpg, glob is much simpler than os.walk.
	- 31. statistics (Built-in Stats): You don't always need numpy or pandas for simple math. Python has a built-in module for averages and medians.
	- 32. zoneinfo (Modern Timezones): Added in Python 3.9, this replaces the need for the external pytz library for most use cases. It uses the system's timezone database.
	- 33. Structural Pattern Matching (match / case):
		- This is not just a switch statement; 
		- it allows you to match data structures, unpack them, and bind variables in one move. 
		- It is a "killer feature" for parsing data or handling complex states.
	- 34. __init_subclass__ (The Plugin Magic): 
		- The Modern Metaclass replacement. 
		- If you want to automatically register plugins or validate subclasses when they are defined, 
		- you used to need complex Metaclasses. Now, you just add this method to the parent class.
	- 35. graphlib.TopologicalSorter (Dependency Resolution)
		- If you have a set of tasks with dependencies (A needs B, B needs C) and need to know the order to run them,
		- this solves it instantly. No need to write your own graph algorithm.
	- 36. importlib.metadata (Package Info)
		- Added in Python 3.8. Stop using the slow and deprecated pkg_resources to check installed versions of libraries. This is the modern standard.
	- 37. enum.Flag (Bitwise Enums): If you need to store multiple states in a single variable (like permissions: READ | WRITE), Flag handles the bitwise logic for you.
	- 37. asyncio.TaskGroup (Safer Concurrency): 
		- Added in Python 3.11. 
		- If you write async code, managing multiple tasks used to be tricky (what if one fails?). 
		- TaskGroup ensures that if one task crashes, the others are cancelled properly. It's the "structured concurrency" model.
	- 38. mmap (Read Huge Files Instantly)
	- 39. ipaddress (Network Math): Stop parsing IP strings with regex. This module handles CIDR notation, subnet calculations, and IPv4/IPv6 conversion.
	- 39. inspect (Code That Reads Code):
		- This is the ultimate metaprogramming tool. 
		- You can inspect the stack, check function arguments at runtime, or even get the source code of a function as a string.
	- 40. textwrap (Beautiful CLI Output)
		- If you are building a CLI tool and printing long strings, they often break in ugly places. textwrap.dedent and fill make output look professional.
	- 41. fractions (Exact Rational Math)
	- 42. array (Efficient Lists): 
		- If you need to store 10 million integers and don't want to install numpy, use array. 
		- It stores data as C-style types (compact bytes) rather than Python objects (heavy pointers).
	- 42. shlex (Safe Shell Parsing)
		- If you are writing a tool that takes "shell-like" commands (e.g., command "some argument with spaces"), 
		- string.split() fails because it splits the quotes. shlex handles it correctly.
	- 43. sqlite3 (The Pocket Database)
		- Python comes with a full SQL database built-in. 
		- It's serverless (just a file) and perfect for prototypes, local tools, or caching
	- 44. fileinput (The CLI Pipe Master)
		- If you want to write a script that works like standard Unix tools (reading from files if provided, OR reading from standard input/pipes if not),
	- 45. breakpoint() (Instant Debugger)
		- Stop writing print("HERE") to debug. Since Python 3.7, you can just write breakpoint(). 
		- It pauses execution and drops you into an interactive PDB shell where you can inspect variables, step through code, and run commands.
	- 46. contextvars (Async-Safe Globals)
		- Standard threading.local() doesn't work well with asyncio because one thread runs many async tasks. 
		- contextvars allows you to store state (like a Request ID or User ID) that is unique to the current task, even if they share a thread
	- 47. ctypes (Call C Code Directly)
		- You can load shared libraries (.dll on Windows, .so on Linux) and call C functions directly from Python without writing any C extension code
	- 48. zipapp (Single-File Executables)
		- You can bundle an entire Python project (with multiple files) into a single .pyz file that is executable. It’s like a .jar file for Python
		- python -m zipapp myapp -o app.pyz     python app.pyz
	- 49. Descriptors (The Magic Behind Properties)
		- If you ever wondered how @property works, it uses the Descriptor Protocol (__get__, __set__). 
		- You can write your own to create reusable behaviors for attributes (like validation or type checking).
	- 50. subprocess (The Right Way to Run Commands)
		- We mentioned os.system is bad. subprocess.run is the modern, secure, and flexible replacement for executing shell commands.
	- 51. shutil (High-Level File Operations)
		- os module is for low-level OS calls. shutil is for "shell utilities" like copying, moving, or archiving entire directory trees
	- 51. argparse (Professional CLIs)
		- If you are parsing sys.argv manually, stop. argparse generates help messages, handles type conversion, and validates input automatically.
	- 52. logging (Beyond Print)
		- print is fine for scripts, but for applications, you need logging. 
		- It allows you to toggle verbosity, write to files, and format timestamps without changing your code
	- 53. json (Custom Encoders)
		- The standard json module fails on custom objects (like datetime or your own classes). You can fix this by subclassing JSONEncoder
	- 54. __new__ (The Real Constructor): __init__ initializes an object, but __new__ creates it. e.g. for Singleton
	- 55. dis (Disassembler): Want to know exactly what Python is doing under the hood? dis shows you the bytecode. 
		- This is great for understanding performance or "is this thread-safe?" questions
	- 56. http.server (Instant Web Server): python -m http.server 8000
	- 57. getpass (Secure Password Input): If you use input("Password: "), the password shows on the screen. getpass hides it.
	- 58. copy (Deep vs. Shallow Copies): copy#copy vs copy#deepcopy
	- 59. urllib.parse (URL Surgery)
	- 60. hashlib (Hashing Data)
	- 61. traceback (Printing Exceptions Properly): traceback.print_exc() 
		- When you catch an exception, printing e often isn't enough—you lose the line number and stack trace. 
		- traceback lets you print the full error report even inside a try/except block.
	- 62. html (Escaping User Input): html.escape(user_input)
		- If you are generating HTML manually (e.g., for an email body), you must escape user input to prevent XSS attacks.
	- 63. cmd (Interactive Shells)
		- If you want to build a tool that works like a REPL (Read-Eval-Print Loop) where the user types commands like help, list, get <id>, cmd handles the loop and command parsing for you.
	- 64. try ... except ... else:
		- Most people know finally (runs always), but else runs only if NO exception occurred. 
		- It is great for separating "code that might fail" from "code that should run only on success."
	- 65. platform (System Info): Need to know if you are running on Windows, Linux, or Mac? Or what Python version?
	- 66. csv (Stop Splitting Strings):
		- Parsing CSVs with line.split(',') is a beginner trap. It fails on quoted fields (e.g., "New York, NY"). 
		- The csv module handles quotes, newlines, and dialects automatically.
	- 67. multiprocessing.shared_memory (Zero-Copy Performance): This is massive for image processing or heavy numpy work.
	- 68. ast (Safe Code Evaluation)
		- eval() is dangerous because it executes arbitrary code. 
		- ast.literal_eval() is a safe alternative that only evaluates Python data structures (lists, dicts, numbers) and crashes on functions or logic.
	- 69. webbrowser (Simple Automation): webbrowser.open_new_tab("https://google.com")
	- 70. calendar (Date Logic)
		- datetime handles time, but calendar handles "calendar logic". Like "Get all Tuesdays in a month" or "Is this year a leap year?".
	- 71. shelve (Persistent Dictionary)
		- If you want to save a Python dictionary to disk so it survives a restart,but sqlite feels too complex and json is too slow (because you have to load the whole file), 
		- shelve acts like a dictionary that is backed by a file.
	- 72. fnmatch (Unix Filename Matching)
		- glob searches the disk. fnmatch checks if a string matches a pattern. 
		- It's lighter and great for filtering lists of filenames you already have
	- 73. base64 (Binary to Text): Essential for embedding images in HTML, sending binary data over JSON, or basic encoding
	- 74. __repr__ vs __str__ (The Debugging Idiom):
		- __str__: For the user (pretty).
		- __repr__: For the developer (unambiguous, ideally valid Python code to recreate the object)
	- 75. os.scandir (Faster Directory Traversal): 
		- os.listdir() is slow because it only gives you filenames, forcing you to call os.stat() on each one to check  it's a file or directory
	- 76. warnings (Professional Deprecation)
		- If you are writing a library or shared code and want to tell users "Stop using this function, it will be removed soon" without crashing their code, use warnings.
	- 77. time.monotonic (Robust Timing): 
		- time.time() relies on the system clock, which can change (e.g., NTP updates, user changing the time). 
		- time.monotonic() is a clock that always moves forward and is unaffected by system time updates
	- 78. hmac (Signing Requests)
	- 79. importlib.resources (Reading Package Data)
		- Stop using __file__ and path hacking to find data files (like templates or config) inside your package. 
		- This breaks if your package is zipped (like in a .pex or .zipapp). importlib.resources is the correct way.
	- 80. weakref.WeakValueDictionary (Auto-Cleaning Cache)
	- 81. __debug__ (Optimization Flag)
		- Python has a built-in constant __debug__ which is True by default. 
		- If you run python with the -O (optimize) flag (python -O script.py), it becomes False. 
		- This allows you to write debug code that is completely removed (zero performance cost) in production.
	- 82. unittest.mock (Fake It 'Til You Make It): from unittest.mock import MagicMock
	- 83. contextlib.redirect_stdout (Capture Print Output)
	- 84. gzip / zipfile (Compression): Reading and writing compressed files is as easy as normal files. gzip.open works exactly like open.
	- 85. sys.getsizeof (Memory Inspection)
		- A quick way to check how much memory an object is consuming (in bytes). Note that for containers (lists/dicts), 
		- it only counts the container structure, not the objects inside it
	- 86. difflib.HtmlDiff (Visual Reports)
		- If you need to show the difference between two text files to a human, 
		- this generates a standalone HTML file with a side-by-side comparison (colors and all).
	- 87. sched (Event Scheduler)
		- If you need to run tasks at specific times or after a delay, but don't want the complexity of a full task queue (like Celery) or cron,
		- sched is a built-in, in-process scheduler.
	- 88. xml.etree.ElementTree (Standard XML)
	- 89. smtplib (Sending Emails): You don't need SendGrid or Mailgun for simple notifications. Python can speak SMTP natively 
	- 90. lzma (Extreme Compression)
		- You know gzip and zipfile. 
		- lzma (used by .xz files) often provides much higher compression ratios at the cost of slower CPU speed. Great for archiving logs
	- 91. runpy (Execute Modules)
		- This is what python -m <module> uses internally. 
		- You can use it to execute a Python script or module dynamically from within Python code, capturing its globals.
	- 92. gc (Garbage Collector Interface): gc.collect()
	- 93. cProfile (Find Slow Code)
		- timeit is for testing small snippets. 
		- cProfile is for profiling your entire program to see exactly which functions are taking the most time.
	- 94. pickle (Object Serialization): pickle.dump(data, file)
		- If you need to save a complex Python object (like a trained Machine Learning model or a custom class instance) to a file and load it back later, 
		- pickle is the standard tool. Warning: Never unpickle data from untrusted sources (security risk).
	- 95. threading.Event (Thread Signaling)
		- How do you tell a thread to "stop" or "wait" without using messy while True loops? Use an Event.
	- 96. sys.setrecursionlimit (Deep Recursion)
		- Python defaults to a recursion limit of 1000 frames to prevent stack overflows. 
		- If you are running a deep recursive algorithm (like DFS on a huge graph), you might hit this. You can increase it safely.
	- 97. colorsys (Color Conversion): convert between RGB (Red-Green-Blue) and HLS (Hue-Lightness-Saturation) or HSV
	- 98. math.isclose (Float Comparison)
		- Comparing floats with == is a bug waiting to happen due to precision errors. math.isclose checks if they are "close enough" (within a tolerance).
	- 99. io.StringIO / io.BytesIO (In-Memory Files)
		- If an API expects a file object (like f.read()) but you have the data in a string or bytes in memory, don't write it to disk just to read it back
	- 100. sys.excepthook (Global Crash Handler)
	- 101. asyncio.to_thread (Async/Sync Bridge)
		- Added in Python 3.9. If you are in an async function but need to call a slow, blocking library (like requests or image processing), 
		- this runs it in a separate thread so it doesn't freeze the event loop.
	- 102. threading.Barrier (Synchronizing Threads)
	- 103. tarfile (Archives)
		- We covered zipfile, but on Linux/Unix, .tar.gz is standard. tarfile handles creating and reading these
