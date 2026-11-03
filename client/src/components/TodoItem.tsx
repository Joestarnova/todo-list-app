import type { Todo } from "@/types";
import { useUpdateTodo } from "@/hooks/useTodos";

export function TodoItem({ todo }: { todo: Todo }) {
  const updateTodo = useUpdateTodo();

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

      <span
        className={`flex-1 text-[15px] ${todo.done ? "text-muted line-through" : ""}`}
      >
        {todo.text}
      </span>
    </div>
  );
}
