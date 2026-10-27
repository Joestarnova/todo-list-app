import { useTodos } from "@/hooks/useTodos";
import type { Todo } from "@/types";

function getSummary(todos: Todo[] | undefined) {
  if (!todos) return "";
  if (todos.length === 0) return "Nothing on your list";

  const remaining = todos.filter((todo) => !todo.done).length;
  return `${remaining} of ${todos.length} remaining`;
}

export function Header() {
  const { data: todos } = useTodos();

  return (
    <header className="mb-8">
      <h1 className="mb-1.5 text-[28px] font-semibold tracking-[-0.02em]">
        Tasks
      </h1>
      <p className="text-sm text-muted">{getSummary(todos)}</p>
    </header>
  );
}
