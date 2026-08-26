package com.example.data.backend

import com.example.data.model.Article
import com.example.data.model.Category
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class BackendServerStatus(
    val serverName: String = "Short-Study Host Gateway (v2.4)",
    val apiEndpoint: String = "https://shortstudy-host.internal/api/v1",
    val status: String = "ONLINE",
    val uptimeHours: Int = 148,
    val storageEngine: String = "Embedded Room SQLite + JSON Sync",
    val latencyMs: Int = 24,
    val activeServices: List<String> = listOf(
        "POST /api/v1/posts/publish",
        "GET /api/v1/posts/hosted",
        "DELETE /api/v1/posts/{id}",
        "GET /api/v1/blogger/sync",
        "POST /api/v1/export/curriculum"
    )
)

data class HostLessonTemplate(
    val title: String,
    val category: Category,
    val summary: String,
    val content: String,
    val readTimeMinutes: Int
)

object HostBackendService {

    fun generateUniquePostId(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val randomSuffix = UUID.randomUUID().toString().take(4)
        return "host-$timestamp-$randomSuffix"
    }

    fun getServerStatus(hostedPostCount: Int): BackendServerStatus {
        return BackendServerStatus(
            uptimeHours = 148 + (hostedPostCount * 2),
            latencyMs = (18..32).random()
        )
    }

    val starterTemplates: List<HostLessonTemplate> = listOf(
        HostLessonTemplate(
            title = "FastAPI & Python 3.12: High-Performance Async APIs",
            category = Category.PYTHON,
            summary = "Learn how to construct asynchronous REST APIs with Pydantic validation and automatic OpenAPI docs.",
            readTimeMinutes = 5,
            content = """
                <h3>1. Introduction to FastAPI</h3>
                <p>FastAPI is a modern, high-performance web framework for building APIs with Python 3.8+ based on standard Python type hints.</p>
                <blockquote><strong>Tip:</strong> FastAPI provides native asynchronous request processing with Starlette and Pydantic validation.</blockquote>

                <h3>2. Creating Your First Async Route</h3>
                <p>Here is a complete example of setting up a typed endpoint with path parameters and query filtering:</p>

                <pre><code class="language-python">from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="Short-Study Microservice")

class LessonItem(BaseModel):
    title: str
    category: str
    read_time: int

@app.get("/api/v1/lessons/{lesson_id}")
async def get_lesson(lesson_id: str, detailed: bool = False):
    return {
        "status": "success",
        "lesson_id": lesson_id,
        "detailed": detailed
    }

@app.post("/api/v1/lessons")
async def create_lesson(lesson: LessonItem):
    return {"message": "Lesson published successfully", "data": lesson}
</code></pre>

                <h3>3. Running with Uvicorn</h3>
                <p>Run your service in development mode with automatic hot reloading:</p>
                <pre><code class="language-python"># Command line execution
uvicorn main:app --reload --port 8000
</code></pre>
            """.trimIndent()
        ),
        HostLessonTemplate(
            title = "C Memory Allocation: Mastering malloc, calloc & free",
            category = Category.C_LANG,
            summary = "Deep dive into dynamic heap allocation in C, preventing memory leaks, and managing pointers safely.",
            readTimeMinutes = 6,
            content = """
                <h3>1. Dynamic Memory Overview</h3>
                <p>In C, variables declared in functions live on the stack. Dynamic allocations live on the heap and persist until explicitly freed.</p>

                <h3>2. Allocation Pattern & Safe Check</h3>
                <p>Always verify that pointer returns from <code>malloc</code> are not <code>NULL</code> before accessing memory:</p>

                <pre><code class="language-c">#include &lt;stdio.h&gt;
#include &lt;stdlib.h&gt;

typedef struct {
    int id;
    char name[32];
    double score;
} StudentRecord;

int main() {
    int count = 5;
    StudentRecord *records = (StudentRecord *)malloc(count * sizeof(StudentRecord));

    if (records == NULL) {
        fprintf(stderr, "Fatal: Memory allocation failed!\n");
        return 1;
    }

    for (int i = 0; i < count; i++) {
        records[i].id = 100 + i;
        records[i].score = 95.5 + i;
    }

    printf("Record 0 ID: %d, Score: %.2f\n", records[0].id, records[0].score);

    // Free allocated block to prevent memory leaks
    free(records);
    records = NULL;
    return 0;
}
</code></pre>
            """.trimIndent()
        ),
        HostLessonTemplate(
            title = "Modern CSS Grid & Container Queries",
            category = Category.WEB_DEV,
            summary = "Build fluid, responsive UI card grids without media queries using repeat(auto-fit, minmax()).",
            readTimeMinutes = 4,
            content = """
                <h3>1. The Auto-Fit MinMax Technique</h3>
                <p>Create robust responsive card lists with a single CSS Grid declaration:</p>

                <pre><code class="language-css">.study-card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 1.5rem;
    padding: 1.5rem;
}

.study-card {
    background: #1e293b;
    border-radius: 16px;
    padding: 1.25rem;
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.study-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
}
</code></pre>
            """.trimIndent()
        )
    )
}
