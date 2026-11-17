import type { Todo } from "@/types";
import { useDeleteTodo, useUpdateTodo } from "@/hooks/useTodos";
import { useState } from "react";

export function TodoItem({ todo }: { todo: Todo }) {
  const updateTodo = useUpdateTodo();
  const deleteTodo = useDeleteTodo();
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState("");

  function startEdit() {
    setDraft(todo.text);
    setEditing(true);
  }

  function save() {
    setEditing(false);

    const text = draft.trim();
    if (!text) {
      deleteTodo.mutate(todo.id);
      return;
    }
    if (text === todo.text) return;

    updateTodo.mutate({ ...todo, text });
  }

  return (
    <div className="flex items-center gap-3 rounded-[10px] border border-border bg-surface px-3.5 py-3">
      <button
        type="button"
        aria-label={todo.done ? "Mark as not done" : "Mark as done"}
        onClick={() => updateTodo.mutate({ ...todo, done: !todo.done })}
        className={`flex size-5 flex-none cursor-pointer items-center justify-center rounded-md border-[1.5px] ${
          todo.done
            ? "border-accent bg-accent"
            : "border-[oklch(0.42_0.008_260)]"
        }`}
      >
        {todo.done && (
          <span className="text-xs leading-none text-white">✓</span>
        )}
      </button>

      {editing ? (
        <input
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") save();
            if (e.key === "Escape") setEditing(false);
          }}
          onBlur={save}
          autoFocus
          className="flex-1 border-b border-accent bg-transparent py-1 text-[15px] outline-none"
        />
      ) : (
        <span
          onClick={startEdit}
          className={`flex-1 cursor-text text-[15px] ${todo.done ? "text-muted line-through" : ""}`}
        >
          {todo.text}
        </span>
      )}

      {!editing && (
        <button
          type="button"
          onClick={startEdit}
          className="flex-none cursor-pointer px-1.5 py-1 text-[13px] text-muted"
        >
          Edit
        </button>
      )}

      <button
        type="button"
        aria-label="Delete"
        onClick={() => deleteTodo.mutate(todo.id)}
        disabled={deleteTodo.isPending}
        className="flex-none cursor-pointer px-1.5 py-1 text-[13px] text-muted"
      >
        Delete
      </button>
    </div>
  );
}
