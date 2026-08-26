package com.example.data.repository

import com.example.data.model.Article
import com.example.data.model.Category

object SampleCurriculum {
    fun getInitialArticles(): List<Article> {
        return listOf(
            Article(
                id = "python-decorators-guide",
                title = "Python Decorators: A Beginner-Friendly Masterclass with Code",
                summary = "Learn how Python decorators work under the hood using first-class functions, wrapper functions, and practical logging examples.",
                author = "Short-Study Staff",
                publishedDate = "2026-08-15",
                url = "https://shortstudy999.blogspot.com/2026/08/python-decorators.html",
                labels = listOf("Python", "Advanced Python", "Clean Code"),
                category = Category.PYTHON,
                isFeatured = true,
                readTimeMinutes = 5,
                content = """
                    <h2>Introduction to Python Decorators</h2>
                    <p>In Python, functions are <strong>first-class citizens</strong>. This means functions can be passed as arguments, assigned to variables, and returned from other functions.</p>
                    <p>A decorator is a design pattern in Python that allows you to modify the functionality of a function or class without directly altering its source code.</p>
                    
                    <h3>1. Basic Decorator Syntax</h3>
                    <p>Here is a classic execution timer decorator that measures how long any function takes to run:</p>
                    
                    <pre><code class="language-python">import time

def timing_decorator(func):
    def wrapper(*args, **kwargs):
        start_time = time.time()
        result = func(*args, **kwargs)
        end_time = time.time()
        print(f"[LOG] {func.__name__} executed in {end_time - start_time:.4f}s")
        return result
    return wrapper

@timing_decorator
def calculate_squares(n):
    return [i ** 2 for i in range(n)]

# Execution
calculate_squares(100000)
</code></pre>
                    
                    <h3>2. Decorators with Arguments</h3>
                    <p>When you need to pass parameters to your decorator itself (such as repeat counts), add an extra enclosing function:</p>
                    
                    <pre><code class="language-python">def repeat(num_times):
    def decorator_repeat(func):
        def wrapper(*args, **kwargs):
            for _ in range(num_times):
                result = func(*args, **kwargs)
            return result
        return wrapper
    return decorator_repeat

@repeat(num_times=3)
def greet(name):
    print(f"Hello, {name}!")

greet("Student")
</code></pre>

                    <h3>Summary Key Takeaways</h3>
                    <p>Use decorators to keep your codebase DRY (Don't Repeat Yourself), separate logging/authorization logic, and create clean modular APIs.</p>
                """.trimIndent()
            ),
            Article(
                id = "c-pointers-demystified",
                title = "Pointers & Memory Addresses in C: Demystified Step-by-Step",
                summary = "Master pointer arithmetic, dereferencing operators (& and *), and memory safety in C with visual diagrams and clean code.",
                author = "Short-Study Staff",
                publishedDate = "2026-08-18",
                url = "https://shortstudy999.blogspot.com/2026/08/c-pointers-basics.html",
                labels = listOf("C Basics", "Memory Management", "Pointers"),
                category = Category.C_LANG,
                isFeatured = true,
                readTimeMinutes = 6,
                content = """
                    <h2>Understanding Memory in C</h2>
                    <p>Every variable in a C program is stored in a specific memory cell having a unique hexadecimal address. A <strong>pointer</strong> is simply a variable whose value is the address of another variable.</p>

                    <h3>The Address-of (&) and Dereference (*) Operators</h3>
                    <p>The <code>&amp;</code> operator gets the memory address. The <code>*</code> operator accesses the value at that address.</p>

                    <pre><code class="language-c">#include &lt;stdio.h&gt;

int main() {
    int score = 95;
    int *ptr = &score; // ptr holds address of score

    printf("Value of score: %d\n", score);
    printf("Address of score: %p\n", (void*)&score);
    printf("Value stored in ptr: %p\n", (void*)ptr);
    printf("Value pointed to by ptr (*ptr): %d\n", *ptr);

    // Modifying score via pointer
    *ptr = 100;
    printf("Updated score: %d\n", score);

    return 0;
}
</code></pre>

                    <h3>Pointer Swapping (Pass by Reference)</h3>
                    <p>Because C is pass-by-value, pointers are required to modify caller variables inside functions:</p>

                    <pre><code class="language-c">#include &lt;stdio.h&gt;

void swap(int *a, int *b) {
    int temp = *a;
    *a = *b;
    *b = temp;
}

int main() {
    int x = 10, y = 20;
    printf("Before swap: x=%d, y=%d\n", x, y);
    swap(&x, &y);
    printf("After swap: x=%d, y=%d\n", x, y);
    return 0;
}
</code></pre>
                """.trimIndent()
            ),
            Article(
                id = "c-dynamic-memory-allocation",
                title = "Dynamic Memory Allocation in C: malloc, calloc, realloc & free",
                summary = "Learn heap allocation vs stack memory, memory leak prevention, and best practices for allocating structs dynamically.",
                author = "Short-Study Staff",
                publishedDate = "2026-08-20",
                url = "https://shortstudy999.blogspot.com/2026/08/c-dynamic-memory.html",
                labels = listOf("C Basics", "Heap Memory", "Pointers"),
                category = Category.C_LANG,
                isFeatured = false,
                readTimeMinutes = 5,
                content = """
                    <h2>Stack vs. Heap Memory</h2>
                    <p>Stack memory is managed automatically by the compiler for local variables. Heap memory is allocated dynamically at runtime using <code>stdlib.h</code> functions.</p>

                    <h3>Safe Memory Allocation Pattern</h3>
                    <pre><code class="language-c">#include &lt;stdio.h&gt;
#include &lt;stdlib.h&gt;

int main() {
    int n = 5;
    int *arr = (int*)malloc(n * sizeof(int));

    // ALWAYS check if allocation succeeded!
    if (arr == NULL) {
        fprintf(stderr, "Memory allocation failed!\n");
        return 1;
    }

    for (int i = 0; i < n; i++) {
        arr[i] = (i + 1) * 10;
    }

    printf("Dynamic Array Elements:\n");
    for (int i = 0; i < n; i++) {
        printf("%d ", arr[i]);
    }
    printf("\n");

    // ALWAYS free dynamically allocated memory!
    free(arr);
    arr = NULL; // Prevent dangling pointer

    return 0;
}
</code></pre>
                """.trimIndent()
            ),
            Article(
                id = "web-dev-css-flexbox-grid",
                title = "Modern CSS Layouts: Master Flexbox and CSS Grid Fast",
                summary = "Complete quick reference guide comparing Flexbox for 1D component layouts with CSS Grid for 2D macro screen structures.",
                author = "Short-Study Staff",
                publishedDate = "2026-08-22",
                url = "https://shortstudy999.blogspot.com/2026/08/css-flexbox-grid.html",
                labels = listOf("Web Dev", "CSS", "Frontend"),
                category = Category.WEB_DEV,
                isFeatured = true,
                readTimeMinutes = 4,
                content = """
                    <h2>Flexbox (1-Dimensional) vs Grid (2-Dimensional)</h2>
                    <p>Use <strong>Flexbox</strong> when aligning items along a single row or column (e.g., navigation bars, button groups). Use <strong>CSS Grid</strong> when architecting whole page layouts and multi-row/multi-column cards.</p>

                    <h3>1. Centering Anything with Flexbox</h3>
                    <pre><code class="language-css">.hero-container {
    display: flex;
    justify-content: center; /* Horizontal alignment */
    align-items: center;     /* Vertical alignment */
    min-height: 100vh;
    gap: 1.5rem;
}
</code></pre>

                    <h3>2. Responsive Auto-Fit Grid</h3>
                    <pre><code class="language-css">.card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 1.5rem;
    padding: 1rem;
}
</code></pre>
                """.trimIndent()
            ),
            Article(
                id = "web-dev-js-async-await",
                title = "JavaScript Async/Await vs. Promises: Best Practices & Error Handling",
                summary = "Write clean, readable asynchronous JavaScript code using async/await, try/catch blocks, and Promise.all concurrency.",
                author = "Short-Study Staff",
                publishedDate = "2026-08-24",
                url = "https://shortstudy999.blogspot.com/2026/08/javascript-async-await.html",
                labels = listOf("Web Dev", "JavaScript", "Async"),
                category = Category.WEB_DEV,
                isFeatured = false,
                readTimeMinutes = 5,
                content = """
                    <h2>Why Async/Await?</h2>
                    <p>Async/await is syntactic sugar built on top of JavaScript Promises that makes asynchronous code look and behave like synchronous code, eliminating callback hell.</p>

                    <pre><code class="language-javascript">async function fetchStudentNotes(blogId) {
    try {
        const response = await fetch("https://api.shortstudy.org/posts?id=" + blogId);
        if (!response.ok) {
            throw new Error("HTTP error! Status: " + response.status);
        }
        const data = await response.json();
        console.log("Loaded articles:", data.items);
        return data.items;
    } catch (error) {
        console.error("Failed to load notes:", error.message);
        return [];
    }
}
</code></pre>
                """.trimIndent()
            ),
            Article(
                id = "algo-binary-search",
                title = "Binary Search: O(log N) Time Complexity & Code in C and Python",
                summary = "Deep dive into binary search algorithm, edge cases (integer overflow in mid calculation), and implementations in Python and C.",
                author = "Short-Study Staff",
                publishedDate = "2026-08-25",
                url = "https://shortstudy999.blogspot.com/2026/08/binary-search-algorithm.html",
                labels = listOf("Algorithms", "DSA", "Python", "C Basics"),
                category = Category.ALGORITHMS,
                isFeatured = false,
                readTimeMinutes = 6,
                content = """
                    <h2>How Binary Search Works</h2>
                    <p>Binary search operates on a <strong>sorted array</strong> by repeatedly dividing the search interval in half. Its time complexity is <strong>O(log N)</strong>.</p>

                    <h3>Python Implementation</h3>
                    <pre><code class="language-python">def binary_search(arr, target):
    low = 0
    high = len(arr) - 1

    while low <= high:
        mid = low + (high - low) // 2 # Avoids overflow
        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            low = mid + 1
        else:
            high = mid - 1
            
    return -1

# Example
nums = [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]
print("Index:", binary_search(nums, 23)) # Returns 5
</code></pre>
                """.trimIndent()
            ),
            Article(
                id = "about-short-study",
                title = "Welcome to Short-Study: The Beginner's Coding Companion",
                summary = "Discover our mission, learning tracks for Python, C programming, and Web Development, and how to get the most out of our notes.",
                author = "Short-Study Team",
                publishedDate = "2026-08-01",
                url = "https://shortstudy999.blogspot.com/p/about-us.html",
                labels = listOf("About Short-Study", "Platform", "Community"),
                category = Category.ABOUT,
                isFeatured = true,
                readTimeMinutes = 3,
                content = """
                    <h2>About Short-Study (shortstudy999.blogspot.com)</h2>
                    <p>Short-Study is an educational coding resource built specifically for students, self-taught programmers, and computer science beginners. We break down complex programming concepts into short, digestible lessons accompanied by verified code snippets.</p>

                    <h3>What You Will Learn Here</h3>
                    <ul>
                        <li><strong>Python:</strong> Syntax, control flow, functions, OOP, decorators, data structures.</li>
                        <li><strong>C Basics:</strong> Low-level memory, pointers, dynamic memory, structs, arrays.</li>
                        <li><strong>Web Development:</strong> Modern HTML5, CSS Flexbox & Grid, Modern JavaScript, APIs.</li>
                        <li><strong>Algorithms:</strong> Essential search, sorting, and problem-solving patterns.</li>
                    </ul>

                    <h3>Offline Reading & Bookmarks</h3>
                    <p>Bookmark any article in this app to save it to your local device database for uninterrupted offline learning during commutes or study sessions!</p>
                """.trimIndent()
            )
        )
    }
}
