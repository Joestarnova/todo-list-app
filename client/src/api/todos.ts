import type { Todo } from "@/types";

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new Error(body?.error ?? `Request failed with status ${res.status}`);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  return res.json();
}

export function listTodos() {
  return request<Todo[]>("/api/todos");
}

export function createTodo(text: string) {
  return request<Todo>("/api/todos", {
    method: "POST",
    body: JSON.stringify({ text }),
  });
}

export function updateTodo(id: number, text: string, done: boolean) {
  return request<Todo>(`/api/todos/${id}`, {
    method: "PUT",
    body: JSON.stringify({ text, done }),
  });
}

export function deleteTodo(id: number) {
  return request<void>(`/api/todos/${id}`, { method: "DELETE" });
}

export function clearCompleted() {
  return request<{ deleted: number }>("/api/todos?done=true", {
    method: "DELETE",
  });
}
